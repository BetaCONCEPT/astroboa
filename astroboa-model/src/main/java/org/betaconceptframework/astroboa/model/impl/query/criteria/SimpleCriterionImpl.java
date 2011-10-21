/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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

package org.betaconceptframework.astroboa.model.impl.query.criteria;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrNamespaceConstants;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SimpleCriterionImpl implements SimpleCriterion, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6095562929021369438L;

	private String property;
	
	private QueryOperator operator = QueryOperator.EQUALS;
	
	private List<Object> values;
	
	private Condition internalCondition = Condition.AND;
	
	private CaseMatching caseMatching;
	
	private boolean propertyIsSimple = true;
	
	private int numberOfNodeLevelsToSearchInTheModelHierarchy = -1;
	
	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	public Condition getInternalCondition() {
		return internalCondition;
	}

	public void setInternalCondition(Condition condition) {
		this.internalCondition = condition;
	}
	
	public void addValue(Object value)
	{
		if (values == null)
			values = new ArrayList<Object>();
		
		if (value != null)
			values.add(value);
	}

	public QueryOperator getOperator() {
		return operator;
	}

	public void setOperator(QueryOperator operator) {
		this.operator = operator;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String propertyPath)
	{
		this.property = propertyPath;
	}
	
	public void clearValues()
	{
		if (values != null)
			values.clear();
	}

	
	public String getXPath() {
 
		//No property path is provided. Return empty criterion
		if (StringUtils.isEmpty(property))
			return "";
		
		
		
		StringBuilder criterion = new StringBuilder();
		
		if (CollectionUtils.isNotEmpty(values) && values.size() > 1)
				criterion.append(CmsConstants.LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE); 
		
		criterion.append(CmsConstants.EMPTY_SPACE);
		
		if (operator == null)
			operator = QueryOperator.EQUALS;
		
		if (QueryOperator.IS_NULL == operator){
			checkThatPropertyPathRefersToSimpleProperty();
			criterion.append(XPathUtils.createNullCriterion(property,propertyIsSimple));
		}
		else if (QueryOperator.IS_NOT_NULL == operator){
			checkThatPropertyPathRefersToSimpleProperty();
			criterion.append(XPathUtils.createNotNullCriterion(property,propertyIsSimple));
		}
		else if (CollectionUtils.isEmpty(values)){
			
			checkThatPropertyPathRefersToSimpleProperty();
			
			if (operator == QueryOperator.EQUALS)
				criterion.append(XPathUtils.createNullCriterion(property,propertyIsSimple));
			else if (operator == QueryOperator.NOT_EQUALS)
				criterion.append(XPathUtils.createNotNullCriterion(property, propertyIsSimple));
			else
				//No value has been provided. No need to create an empty criterion
				return "";
		}
		else
		{
			String propertyPath = property;
			
			if (QueryOperator.EQUALS == operator || QueryOperator.NOT_EQUALS == operator ||
					QueryOperator.LIKE == operator){
				//Check for case matching to activate appropriate method.
				if (CaseMatching.LOWER_CASE == caseMatching){
					propertyPath = CmsConstants.FN_LOWER_CASE+CmsConstants.LEFT_PARENTHESIS+property+CmsConstants.RIGHT_PARENTHESIS;
				}
				else if (CaseMatching.UPPER_CASE == caseMatching){
					propertyPath = CmsConstants.FN_UPPER_CASE+CmsConstants.LEFT_PARENTHESIS+property+CmsConstants.RIGHT_PARENTHESIS;
				}
			}
			//Values exist. Create appropriate xpath criteria
			if (internalCondition == null)
				internalCondition = Condition.AND;
			
			performSpecialOperationsInCaseOfContainsOperator();

			for (Object value : values)
			{
				if (value instanceof String){
					//Check for case matching and transform the value accordingly.
					value = transformValueIfCaseMatchingIsEnabled(value);
					
					value = checkIfValueIsAReferenceAndLoadReferenceId((String)value);
					
					value = checkIfPropertyIsOfTypeLongIntegerOrDoubleAndConvertValueAccordingly(propertyPath, (String)value);
				}

				
				criterion.append(CmsConstants.EMPTY_SPACE +XPathUtils.createObjectCriteria(propertyPath, operator, value, propertyIsSimple, caseMatching, numberOfNodeLevelsToSearchInTheModelHierarchy)
						+ CmsConstants.EMPTY_SPACE + internalCondition.toString().toLowerCase());
			}
			
			//Remove the last internal condition
			criterion.replace(criterion.length()-internalCondition.toString().length(), criterion.length() ,"");
		}
		
		criterion.append(CmsConstants.EMPTY_SPACE);
		
		if (CollectionUtils.isNotEmpty(values) && values.size() > 1)
			criterion.append(CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE);
		
		return criterion.toString() ;
	}

	private Object checkIfPropertyIsOfTypeLongIntegerOrDoubleAndConvertValueAccordingly(String property, String value) {
		
		if (QueryOperator.EQUALS == operator || QueryOperator.NOT_EQUALS == operator ||
				QueryOperator.GREATER == operator || QueryOperator.GREATER_EQUAL == operator ||
				QueryOperator.LESS == operator || QueryOperator.LESS_EQUAL == operator){
			
			if (property == null || property.trim().length() == 0 ||  //Property is blank
					property.startsWith(BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":") || 
					property.startsWith(JcrNamespaceConstants.JCR_PREFIX+":") ||  // All JCR native properties that are used are not of type long or double
					property.startsWith(JcrNamespaceConstants.MIX_PREFIX+":") ||  // All mixin properties that are used are not of type long or double
					property.startsWith(JcrNamespaceConstants.NT_PREFIX+":") ||   // All properties with nt: prefix that are used are not of type long or double
					property.startsWith(CmsConstants.FN_LOWER_CASE) ||
					property.startsWith(CmsConstants.FN_UPPER_CASE)){
				return value;
			}

			String propertyLastPart = PropertyPath.getLastDescendant(property);

			if (propertyLastPart!=null && 
					! propertyLastPart.equals(property) && 
					propertyLastPart.startsWith(JcrNamespaceConstants.JCR_PREFIX+":") ||  // All JCR native properties that are used are not of type long or double
					propertyLastPart.startsWith(JcrNamespaceConstants.MIX_PREFIX+":") ||  // All mixin properties that are used are not of type long or double
					propertyLastPart.startsWith(JcrNamespaceConstants.NT_PREFIX+":")  // All properties with nt: prefix that are used are not of type long or double
				){
				return value;
			}
			
			LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
			
			if (lazyLoaderForActiveClient !=null){
				ValueType propertyType = lazyLoaderForActiveClient.getDefinitionService().getTypeForProperty(null, property);

				if (propertyType != null){
					switch (propertyType) {
					case Long:
						//this type corresponds to long as well as to the integers
						try {
							return Long.parseLong(value);
						}
						catch(Exception e){
							e.printStackTrace();
						}
						break;
					case Double:
						try {
							return Double.parseDouble(value);
						}
						catch(Exception e){
							e.printStackTrace();
						}
						break;
					default:
						break;
					}
				}
			}
		}
		
		return value;
	}

	private void performSpecialOperationsInCaseOfContainsOperator() {
		if (QueryOperator.CONTAINS == operator){
			//Special case .Check if property corresponds to a complex one.
			//	This check is done when operator is CONTAINS , flag proeprtyIsSimple has not changed (default is true)
			//and property name is not * and does not start with bccms:localization. The last check implies that 
			//this criterion has been created in the context of a Topic search and thus, checking if property is a complex
			//or not is not necessary
			if (propertyIsSimple && ! CmsConstants.ANY_NAME.equals(property) &&	! property.startsWith(CmsBuiltInItem.Localization.getJcrName())){ 
				checkThatPropertyPathRefersToSimpleProperty();
			}
			
			if (CmsConstants.ANY_NAME.equals(property) &&  numberOfNodeLevelsToSearchInTheModelHierarchy == -1){
				//Load max definition hierarchy depth
				loadMaxDefinitionHierarchyDepth();
			}
			
			if (numberOfNodeLevelsToSearchInTheModelHierarchy<0){
				numberOfNodeLevelsToSearchInTheModelHierarchy = 0;
			}
		}
	}

	private void loadMaxDefinitionHierarchyDepth() {
		
		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
		
		if (lazyLoaderForActiveClient !=null){
			numberOfNodeLevelsToSearchInTheModelHierarchy = lazyLoaderForActiveClient.getDefinitionService().getDefinitionHierarchyDepthForContentType(CmsConstants.ANY_NAME);
		}
		else{
			numberOfNodeLevelsToSearchInTheModelHierarchy = 0;
		}
		
	}

	private void checkThatPropertyPathRefersToSimpleProperty() {
		
		if (property == null || property.startsWith(BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":") || 
				property.startsWith(JcrNamespaceConstants.JCR_PREFIX+":") ||
				property.startsWith(JcrNamespaceConstants.MIX_PREFIX+":") ||
				property.startsWith(JcrNamespaceConstants.NT_PREFIX+":") ||
				property.startsWith(CmsConstants.FN_LOWER_CASE) ||
				property.startsWith(CmsConstants.FN_UPPER_CASE)){
			propertyIsSimple = true;
			return;
		}
		
		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
		
		if (lazyLoaderForActiveClient !=null){
			ValueType propertyType = lazyLoaderForActiveClient.getDefinitionService().getTypeForProperty(null, property);

			if (propertyType != null && ValueType.Complex.equals(propertyType)){
				propertyIsSimple = false;
			}
			else{
				propertyIsSimple = true;
			}
		}
	}

	/**
	 * @param value
	 * @return
	 */
	protected String checkIfValueIsAReferenceAndLoadReferenceId(String value) {
		if (QueryOperator.EQUALS == operator || QueryOperator.NOT_EQUALS == operator){
			
			if (valueIsATopicReference(value)){
				return loadTopicIdentifier(value.substring(1, value.length()));
			}
			else if (valueIsAContentObjectReferece(value)){
				return loadContentObjectIdentifier(value.substring(1, value.length()));
			}
			
		}
		
		return value;
	}

	/**
	 * @param substring
	 * @return
	 */
	private String loadContentObjectIdentifier(String systemName) {
		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();

		if (lazyLoaderForActiveClient != null){
			String activeAuthenticationToken = AstroboaClientContextHolder.getActiveAuthenticationToken();

			ContentObject contentObject = lazyLoaderForActiveClient.getContentObjectBySystemName(systemName, activeAuthenticationToken);

			if (contentObject == null || contentObject.getId() == null){
				return systemName;
			}

			return contentObject.getId();
		}
		
		return systemName;
	}

	protected boolean valueIsAContentObjectReferece(String value) {
		return value!= null &&  value.startsWith(CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX);
	}

	protected boolean valueIsATopicReference(String value) {
		return value != null && value.startsWith(CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX);
	}

	/**
	 * @param substring
	 * @return
	 */
	protected String loadTopicIdentifier(String topicName) {
		
		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();

		if (lazyLoaderForActiveClient != null){
			String activeAuthenticationToken = AstroboaClientContextHolder.getActiveAuthenticationToken();

			Topic topic = lazyLoaderForActiveClient.getTopicByName(topicName, activeAuthenticationToken);

			if (topic == null || topic.getId() == null){
				return topicName;
			}
			return topic.getId();
		}
		
		return topicName;
		
	}

	private Object transformValueIfCaseMatchingIsEnabled(Object value) {
		if (CaseMatching.LOWER_CASE == caseMatching){
			value = ((String)value).toLowerCase();
		}
		else if (CaseMatching.UPPER_CASE == caseMatching){
			value = ((String)value).toUpperCase();
		}
		return value;
	}

	public CaseMatching getCaseMatching() {
		return caseMatching;
	}

	public void setCaseMatching(CaseMatching caseMatching) {
		if (caseMatching == null){
			this.caseMatching = CaseMatching.NO_CASE;
		}
		else{
			this.caseMatching = caseMatching;
		}
		
	}
	
	public void propertyIsComplex(){
		propertyIsSimple = false;
	}

	public void setNumberOfNodeLevelsToSearchInTheModelHierarchy(int numberOfNodeLevelsToSearchInTheModelHierarchy){
		this.numberOfNodeLevelsToSearchInTheModelHierarchy = numberOfNodeLevelsToSearchInTheModelHierarchy;
	}
}
