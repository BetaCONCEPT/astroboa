/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.betaconceptframework.astroboa.model.impl.query.xpath;


import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion.CaseMatching;
import org.betaconceptframework.astroboa.lucene.analyzer.GreekEnglishAnalyzer;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsUtils;
import org.betaconceptframework.astroboa.util.DateUtils;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class XPathUtils {

	private static final String CONDITION_AND_LOWER_CASE = Condition.AND.toString().toLowerCase();
	private static final String CONDITION_OR_LOWER_CASE = Condition.OR.toString().toLowerCase();

	private static Analyzer greekAnalyzer = new GreekEnglishAnalyzer();

	private static Pattern doubleQuotePattern = Pattern.compile("\"");
	private static Pattern singleQuotePattern = Pattern.compile("'");
	
	public static String createStringCriteria(String property,
			QueryOperator operator, String value) {
		//Escape the following characters
		// ', "
		value = doubleQuotePattern.matcher(value).replaceAll("\"\"");
        value = singleQuotePattern.matcher(value).replaceAll("''");
		
		return createAttributeCriteria(property, operator, "'" + value + "'", true);
	}

	public static String createNullCriterion(String property, boolean lastPropertyInPathRepresentsASimpleProperty) {
		
		/*
		 * Due to a feature request (?) 
		 * https://issues.apache.org/jira/browse/JCR-1447
		 * JCR does not specify the behavior of constraints of type profile/not(@title)
		 * and therefore Jackrabbit does not fully support such constraints leading to unwanted results
		 *
		 * What is happening is that when property is just an attribute i.e. title
		 * then not(@title) works as expected. 
		 * Problems start when property is a child property like profile.title .
		 * In these cases the expected not(profile/@title) works only when 
		 * node profile already exists but it does not contain property title.
		 * Any node which DOES not have child node profile does not match.
		 * 
		 * To cover these cases the following criterion must be created
		 * 	not( profile/@jcr:primaryType=nt:unstructured and profile/@title )
		 * 
		 * which tells Jackrabbit to return all nodes which DO NOT have a child node 
		 * with a property jcr:primaryType=nt:unstructured and at the same time have a property
		 * named title with a value.
		 * 
		 * Note that the first criterion must be created for every child node in the provided path.
		 * That is if property provided is comment.comment.body then the following criterion
		 * is created
		 * 
		 * 
		 * not(comment/@jcr:primaryType=nt:unstructured and comment/comment/@jcr:primaryType=nt:unstructured and comment/comment/@body)
		 */
		String notNullCriterion = createNotNullCriterion(property, lastPropertyInPathRepresentsASimpleProperty);
		
		return CmsConstants.NOT + CmsConstants.LEFT_PARENTHESIS +notNullCriterion + CmsConstants.RIGHT_PARENTHESIS;
	}

	public static String createNotNullCriterion(String property, boolean lastPropertyInPathRepresentsASimpleProperty) {
		
		String propertyPath = generateJcrPathForPropertyPath(property, lastPropertyInPathRepresentsASimpleProperty);
		
		String[] nodes = propertyPath.split(CmsConstants.FORWARD_SLASH);

		if (nodes.length == 1 && nodes[0].startsWith(CmsConstants.AT_CHAR)){
			//Property is a first level simple property
			return propertyPath;
		}
			
		StringBuilder criterion = new StringBuilder(); 
		
		criterion.append(CmsConstants.LEFT_PARENTHESIS);
			
		StringBuilder partialPath = new StringBuilder();
			
		for (int i=0; i<nodes.length;i++){
			
			if (i == nodes.length-1 && nodes[i].startsWith(CmsConstants.AT_CHAR)){
				//Reached the end. If property is a simple property then 
				//its path starts with @ and therefore we do not need to add anything more 
				partialPath.append(nodes[i]);
				criterion.append(partialPath.toString());
			}
			else{

				partialPath.append(nodes[i])
							.append(CmsConstants.FORWARD_SLASH);

				criterion.append(partialPath.toString())
						.append(createStringCriteria(JcrBuiltInItem.JcrPrimaryType.getJcrName(), QueryOperator.EQUALS, JcrBuiltInItem.NtUnstructured.getJcrName())) 
						.append(CmsConstants.EMPTY_SPACE);
				
				if (i != nodes.length-1){
					criterion.append(CONDITION_AND_LOWER_CASE)
							.append(CmsConstants.EMPTY_SPACE);
				}
			}
		}
		
		return criterion.append(CmsConstants.RIGHT_PARENTHESIS).toString();
	}

	public static String generateJcrPathForPropertyPath(String propertyPath, boolean lastPropertyInPathRepresentsASimpleProperty) {

		if (StringUtils.isNotBlank(propertyPath)){
			//It is high likely that property may contain function names. In this case
			//remove functions from property, create appropriate xpath for property and then
			//concatenate function again
			String functionName = getFunctionNameFromProperty(propertyPath);
			
			String tempPropertyPath = propertyPath;
			
			if (StringUtils.isNotBlank(functionName)){
				//Remove function name and its parenthesis
				tempPropertyPath = StringUtils.substringBetween(propertyPath, functionName+CmsConstants.LEFT_PARENTHESIS, CmsConstants.RIGHT_PARENTHESIS);
			}
			
			//Property name is *. No extra manipulation 
			if (CmsConstants.ANY_NAME.equals(tempPropertyPath.trim())){
				return attachFunctionNameToProperty(functionName, tempPropertyPath);
			}
			
			//Since in astroboa api a property path contains one or more
			//property names delimited with '.', these periods must be replaced
			//by path delimiter recognized by JCR
			if (tempPropertyPath.contains(CmsConstants.PERIOD_DELIM)){
				tempPropertyPath = StringUtils.replace(tempPropertyPath, CmsConstants.PERIOD_DELIM, CmsConstants.FORWARD_SLASH);
			}

			//Do not add any '@' character in case the property participates in jcr:contains
			//function
  			if (! lastPropertyInPathRepresentsASimpleProperty){
					return attachFunctionNameToProperty(functionName, ISO9075.encodePath(tempPropertyPath));
  			}
				
			//Finally must add "@" character to the last property in path 
			//to denote that this is an attribute
			if (!tempPropertyPath.contains(CmsConstants.AT_CHAR)){
				//Make sure all property names are valid XML Names
				tempPropertyPath = ISO9075.encodePath(tempPropertyPath);

				if (tempPropertyPath.contains(CmsConstants.FORWARD_SLASH)){
					//Last property in path is always an attribute
					tempPropertyPath = CmsUtils.replaceLast(CmsConstants.FORWARD_SLASH, CmsConstants.FORWARD_SLASH+CmsConstants.AT_CHAR, tempPropertyPath);
				}
				else
					//Property path contains one property
					tempPropertyPath = CmsConstants.AT_CHAR + tempPropertyPath;
			}

			//Finally encode any names in property path which start with a digit 
			return attachFunctionNameToProperty(functionName, tempPropertyPath);
		}
		
		return "";
	}

	private static String attachFunctionNameToProperty(String functionName,
			String tempPropertyPath) {
		if (StringUtils.isNotBlank(functionName)){
			return functionName+CmsConstants.LEFT_PARENTHESIS+tempPropertyPath+CmsConstants.RIGHT_PARENTHESIS;
		}

		return tempPropertyPath;
	}

	private static String getFunctionNameFromProperty(String property) {
		if (property == null){
			return null;
		}
		
		if (property.startsWith(CmsConstants.FN_LOWER_CASE)){
			return CmsConstants.FN_LOWER_CASE;
		}
		else if (property.startsWith(CmsConstants.FN_UPPER_CASE)){
			return CmsConstants.FN_UPPER_CASE;
		}
		
		return null;
	}

	public static String createAttributeCriteria(String property,
			QueryOperator operator, String value,boolean lastPropertyInPathRepresentsASimpleProperty) {
		return generateJcrPathForPropertyPath(property, lastPropertyInPathRepresentsASimpleProperty) + CmsConstants.EMPTY_SPACE
				+ operator.getOp() + CmsConstants.EMPTY_SPACE + value;
	}

	public static String createDateCriteria(String property,
			QueryOperator operator, Calendar calendar) {
		return createAttributeCriteria(property, operator, "xs:dateTime('"
				+ formatForQuery(calendar) + "')", true);
	}

	public static String formatForQuery(Calendar calendar) {
		//Format according to ISO 8601 format
		return ISO8601.format(calendar);
	}

	public static String createXPathSelect(ItemQName nodeName, ItemQName nodeType, boolean isChildNodeType) {
		return createXPathSelect(null, nodeName, nodeType, isChildNodeType);
	}

	public static String createXPathSelect(String elementPrefix, ItemQName nodeName,
			ItemQName nodeType, boolean isChildNodeType) {
		if (StringUtils.isBlank(elementPrefix))
			elementPrefix = "";

		return createSelectClause(elementPrefix,
				(((nodeName == null) ? CmsConstants.ANY_NAME : nodeName.getJcrName())),
				nodeType, isChildNodeType);
	}

	public static String addLikeCriteria(String property, String textToFind) {
		String attributeName = generateJcrPathForPropertyPath(property, true);

		if (StringUtils.isBlank(attributeName))
			attributeName = CmsConstants.PERIOD_DELIM;
		
		return CmsConstants.EMPTY_SPACE + JcrBuiltInItem.JcrLike.getJcrName() + CmsConstants.LEFT_PARENTHESIS  + attributeName + CmsConstants.COMMA
				+"'" +textToFind +"'"+CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE;

	}

	public static String addContainsCriteria(String property, String textToFind, boolean lastPropertyInPathRepresentsASimpleProperty, int numberOfNodeLevelsToSearchInTheModelHierarchy) {

		String propertySelector = null;
		
		boolean buildPropertySelectorForAllProperties = false;
		
		if (StringUtils.isBlank(property) || CmsConstants.ANY_NAME.equals(property.trim())){
			//user has requested a full text search on every property.
			propertySelector = CmsConstants.ANY_NAME;
			buildPropertySelectorForAllProperties = true;
		}
		else {
			propertySelector =  generateJcrPathForPropertyPath(property, lastPropertyInPathRepresentsASimpleProperty);
		}
		
		try {
			
			// Rebuild textToFind with spaces between terms
			String escapedTextToFind = EscapeTextUtil.escape(textToFind);
					
			String analyzedTextTofind = analyzeTextToFind(escapedTextToFind);
			
			analyzedTextTofind = EscapeTextUtil.unescape(analyzedTextTofind).trim();
			
			String jcrContains = JcrBuiltInItem.JcrContains.getJcrName();
			
			if (buildPropertySelectorForAllProperties){
				
				StringBuilder criterion = new StringBuilder();
				criterion.append(CmsConstants.LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE);
				
				for (int i=0; i<=numberOfNodeLevelsToSearchInTheModelHierarchy; i++) {
					criterion.append(CmsConstants.EMPTY_SPACE)
							.append(jcrContains)
							.append(CmsConstants.LEFT_PARENTHESIS);
						
					  if (i==0){
						  // dot (.) corresponds to the node which represents the content object node
						  criterion.append(CmsConstants.PERIOD_DELIM);
					  }
					  else{
						  criterion.append(propertySelector);
						  propertySelector = propertySelector+CmsConstants.FORWARD_SLASH+CmsConstants.ANY_NAME;
					  }
						criterion.append(",'")
								.append(analyzedTextTofind)
								.append("'")
								.append(CmsConstants.RIGHT_PARENTHESIS)
								.append(CmsConstants.EMPTY_SPACE);
					
					if (i<numberOfNodeLevelsToSearchInTheModelHierarchy){
						criterion.append(CONDITION_OR_LOWER_CASE);
					}
				}
				
				criterion.append(CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE);
				
				return criterion.toString();
			}
			else{
				StringBuilder criterion = new StringBuilder();
				criterion.append(CmsConstants.EMPTY_SPACE)
						.append(jcrContains)
						.append(CmsConstants.LEFT_PARENTHESIS)
					  	 .append(propertySelector)
						 .append(",'")
						.append(analyzedTextTofind)
						.append("'")
						.append(CmsConstants.RIGHT_PARENTHESIS)
						.append(CmsConstants.EMPTY_SPACE);
				return criterion.toString();
			}
				
		} catch (IOException e) {
			return CmsConstants.EMPTY_SPACE + JcrBuiltInItem.JcrContains.getJcrName() + CmsConstants.LEFT_PARENTHESIS + propertySelector + ",'" + textToFind + "') ";
		}
	}


	private static String analyzeTextToFind(String textToFind)
			throws IOException {
		// Filter textToFind through GreekAnalyzer
		TokenStream result = greekAnalyzer.tokenStream("", new StringReader(textToFind));
		result.reset();
		
		String analyzedTextTofind = "";
		Token term = null;
		while (result.incrementToken()){
			term = result.getAttributeImplsIterator();
			if (term != null)
				analyzedTextTofind = analyzedTextTofind.concat(" "
						+ new String(term.termBuffer(), 0, term.termLength()));

		}
		result.end();
		result.close();

		analyzedTextTofind = analyzedTextTofind.trim();
		
		if (StringUtils.isBlank(analyzedTextTofind))
			analyzedTextTofind = textToFind;
		
		return analyzedTextTofind;
		
			
	}

	public static String createXPathSelectAllNodesForType(ItemQName nodeType) {
		return createXPathSelect(null, nodeType, false);
	}

	public static String createSelectClause(String elementPrefix,
			String nodeName, ItemQName nodeType, boolean isChildNodeType) {

		return ((StringUtils.isBlank(elementPrefix)) ? "" : elementPrefix)
				+ ((isChildNodeType)? CmsConstants.FORWARD_SLASH : CmsConstants.DOUBLE_FORWARD_SLASH)
				+ "element"
				+ CmsConstants.LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE
				+ (StringUtils.isBlank(nodeName) ? CmsConstants.ANY_NAME : nodeName)
				+ CmsConstants.COMMA
				+ ((nodeType == null) ? JcrBuiltInItem.NtUnstructured
						.getJcrName() : nodeType.getJcrName())
				+ CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE;
	}

	
	public static String createObjectCriteria(String property,QueryOperator operator, Object value, boolean propertyIsASimpleProperty, CaseMatching caseMatching, int numberOfNodeLevelsToSearchInTheModelHierarchy)	{

		if (value == null || QueryOperator.IS_NULL == operator)
			return createNullCriterion(property, propertyIsASimpleProperty);

		if (QueryOperator.CONTAINS == operator)
			return addContainsCriteria(property, value.toString(), propertyIsASimpleProperty, numberOfNodeLevelsToSearchInTheModelHierarchy);

		if (QueryOperator.LIKE == operator)
			return addLikeCriteria(property, value.toString());

		if (QueryOperator.IS_NOT_NULL == operator)
			return createNotNullCriterion(property,propertyIsASimpleProperty);
		
		if (value instanceof String){
			if (caseMatching != null){
				if (CaseMatching.LOWER_CASE == caseMatching){
					value = ((String) value).toLowerCase();
				}
				else if (CaseMatching.UPPER_CASE == caseMatching){
					value = ((String) value).toUpperCase();
				}
			}
			
			return createStringCriteria(property, operator, (String) value);
		}

		if (value instanceof Calendar)
			return createDateCriteria(property, operator, (Calendar) value);

		if (value instanceof Date)
			return createDateCriteria(property, operator, DateUtils.toCalendar((Date) value));

		if (value instanceof Long)
			return createLongCriteria(property, operator, (Long) value);
		if (value instanceof Boolean)
			return createBooleanCriteria(property, operator, (Boolean) value);

		if (value instanceof Double)
			return createDoubleCriteria(property, operator, (Double) value);

		//Unknown type. Just create String criteria
		return createStringCriteria(property, operator, value.toString());
	}

	private static String createDoubleCriteria(String property,
			QueryOperator operator, Double doubleValue) {
		return createAttributeCriteria(property, operator, doubleValue.toString(), true);
	}

	private static String createBooleanCriteria(String property,
			QueryOperator operator, Boolean booleanValue) {
		//Jackrabbit wants boolean value surrounded by quotes
		return createAttributeCriteria(property, operator, "'"+ booleanValue.toString()+ "'", true);
	}

	public static String createLongCriteria(String property,
			QueryOperator operator, Long longValue) {
		return createAttributeCriteria(property, operator, longValue.toString(), true);
	}

	/**
	 * Return relative taxonomy path (without / at the beginning). If taxonomy name
	 * is null or empty returns SubjectTaxonomy relative path
	 * @param taxonomyName
	 * @return
	 */
	public static  String getRelativeTaxonomyPath(String taxonomyName, boolean encodeTaxonomyName) {
		if (StringUtils.isNotBlank(taxonomyName))
			return CmsBuiltInItem.SYSTEM.getJcrName()+CmsConstants.FORWARD_SLASH+ CmsBuiltInItem.TaxonomyRoot.getJcrName() +
			CmsConstants.FORWARD_SLASH+
			(encodeTaxonomyName ?ISO9075.encodePath(taxonomyName) : taxonomyName);
		else
			return CmsBuiltInItem.SYSTEM.getJcrName()+CmsConstants.FORWARD_SLASH+ CmsBuiltInItem.TaxonomyRoot.getJcrName() +
			CmsConstants.FORWARD_SLASH+CmsBuiltInItem.SubjectTaxonomy.getJcrName();
	}

	

	public static String getRelativeFolksonomyPath(Criterion repositoryUserIdCriterion, Criterion folksonomyIdCriterion) {
		
		String repositoryUserPath = CmsBuiltInItem.SYSTEM.getJcrName()+CmsConstants.FORWARD_SLASH+ CmsBuiltInItem.RepositoryUserRoot.getJcrName() +
				CmsConstants.FORWARD_SLASH+CmsBuiltInItem.RepositoryUser.getJcrName();
		
		if (repositoryUserIdCriterion != null && StringUtils.isNotBlank(repositoryUserIdCriterion.getXPath()))
				repositoryUserPath += CmsConstants.LEFT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE + repositoryUserIdCriterion.getXPath() + CmsConstants.RIGHT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE;
		
		String folksonomyPath = CmsConstants.FORWARD_SLASH + CmsBuiltInItem.Folksonomy.getJcrName();
		
		if (folksonomyIdCriterion != null && StringUtils.isNotBlank(folksonomyIdCriterion.getXPath()))
			folksonomyPath += CmsConstants.LEFT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE + folksonomyIdCriterion.getXPath() + CmsConstants.RIGHT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE;
			
		return repositoryUserPath + folksonomyPath;
	}
	

}
