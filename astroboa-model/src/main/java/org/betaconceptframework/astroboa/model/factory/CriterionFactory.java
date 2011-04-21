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

package org.betaconceptframework.astroboa.model.factory;


import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ConditionalCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectReferenceCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion.CaseMatching;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicReferenceCriterion;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.criteria.ConditionalCriterionImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.ContentObjectReferenceCritetionImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.CriterionUtils;
import org.betaconceptframework.astroboa.model.impl.query.criteria.LocalizationCriterionImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.NotCriterion;
import org.betaconceptframework.astroboa.model.impl.query.criteria.RangeCriterion;
import org.betaconceptframework.astroboa.model.impl.query.criteria.SimpleCriterionImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.TopicReferenceCriterionImpl;
import org.betaconceptframework.astroboa.model.impl.query.parser.CriterionParser;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Provides convenient methods for creating simple criteria for most common
 * operators.
 * 
 * <p>
 * Use of this factory is preferred as it offers the developer all necessary 
 * methods to create fast any criterion for any operator by just providing only 
 * the necessary information. For example, an equals criterion for property
 * <code>title</code> could be created by the following two ways:
 * </p>
 * 
 * <pre>
 *   
 *   // 1. The hard way
 *   Criterion titleCriterion = CriterionFactory.newSimpleCriterion();
 *   titleCriterion.setProperty("title");
 *   titleCriterion.setOperator(QueryOperator.EQUALS);
 *   titleCriterion.addValue("MySearchExpression");
 *   
 *   //2. The easy way
 *   Criterion titleCriterion = CriterionFactory.equals("title", "MySearchExpression");
 * </pre>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CriterionFactory  {

	CriterionFactory(){
		//Use private static constructor so that it cannot be instantiated
	}

	/**
	 * Creates a simple criterion.
	 * 
	 * <p>
	 * This method creates an empty criterion. It should be used only when
	 * this factory does not provide an alternative method for
	 * a criterion to be created.
	 * </p>
	 * 
	 * @return An empty criterion.
	 */
	public static SimpleCriterion newSimpleCriterion(){
		return new SimpleCriterionImpl();
	}
	
	/**
	 * Associates two criteria with the {@link Condition#AND and} expression.
	 * 
	 * @param leftHandSide
	 *            Left hand side of association.
	 * @param rightHandSide
	 *            Right hand side of association.
	 * @return An {@link ConditionalCriterion ANDed} conditional criterion.
	 */
	public static Criterion and(Criterion leftHandSide, Criterion rightHandSide) {
		return newConditionalCriterion(leftHandSide, rightHandSide, Condition.AND);
	}

	/**
	 * Creates a "between" restriction for property. 
	 * 
	 * <p>
	 *  It is equivalent to a {@link #lessThanOrEquals(String, Object)} criterion 
	 *  and a {@link #greaterThanOrEquals(String, Object)} criterion, associated with
	 *  logical expression {@link Condition#AND}. 
	 * </p>
	 * 
	 * <p>
	 * Note that multi valued simple properties,
	 *  will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>8, 11</code> and  
	 * the following criterion is set <code>between("amounts", "9","10")</code>, this property 
	 * will match the specified criterion because there is at least one value greater than <code>9</code>
	 * and less than <code>10</code>.
	 * </p>	 
	 * 
	 * <p>
	 *  In general, range criteria on multi valued simple properties should be 
	 *  avoided if possible as there is high possibility that "unwanted" results
	 *  may be included to the overall outcome. The result in the above example from user's perspective,
	 *  is wrong as property's values are not ALL between <code>9</code> and
	 *  <code>10</code> where as from JCR's perspective, is correct as there is at least 
	 *  one value which is less than <code>9</code> and at least one value which is 
	 *  greater than <code>10</code>. 
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param lowerLimit
	 *            Lower constraint value.
	 * @param upperLimit
	 *            Upper constraint value.
	 * @return A <code>lessThanOrEquals AND greaterThanOrEquals</code> constraint.
	 */
	public static Criterion between(String propertyPath, Object lowerLimit,
			Object upperLimit) {
		return createRangeCriterion(propertyPath, lowerLimit, upperLimit);
	}

	/**
	 * Contains criterion enables the use of full-text search. 
	 * 
	 * <p>
	 * All types
	 * of properties are searched including binary types. The following rules
	 * apply :
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Search expression can contain one or more terms separated by whitespace.A
	 * term can be a single word or a phrase delimited by double quotes (").
	 * <li>Search expression for all terms should contain whitespace (implicit AND) between 
	 * terms and for either of terms should contain OR.
	 * <li>Search expression can contain both AND-ed and OR-ed terms with AND having higher
	 * precedence.
	 * <li>Any term prefixed with - (minus sign) will not be included in the
	 * results.
	 * <li>Any term containing '*' character will result in matching values
	 * containing any string of zero or more characters in place of '*'. Note
	 * this behavior is disabled if term is a phrase.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Search expression examples :
	 * 
	 * <ul>
	 * <li><code>contains('title', 'java jdk')</code>
	 * <p>
	 * Matches all values containing <code>java</code> AND <code>jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '\"java jdk\"')</code>
	 * <p>
	 * Matches all values containing the phrase <code>java jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', 'java OR jdk')</code>
	 * <p>
	 * Matches all values containing <code>java</code> OR <code>jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '\"java jdk\" OR 1.5')</code>
	 * <p>
	 * Matches all values which contain phrase <code>java jdk</code> OR
	 * <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '-\"java jdk\" 1.5')</code>
	 * <p>
	 * Matches all values which DO NOT contain phrase <code>java jdk</code>
	 * and contain <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', 'java* 1.5')</code>
	 * <p>
	 * Matches all values which contain strings that start with
	 * <code>java</code> and also contain string <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '*java* 1.5')</code>
	 * <p>
	 * Matches all values which contain string <code>java</code> and also
	 * contain string <code>1.5</code>.
	 * </p>
	 * 
	 * </ul>
	 * </p>
	 * 
	 * @deprecated Use {@link #contains(String, String)} instead.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param searchExpression
	 *            Search value.
	 * @return A <code>contains</code> constraint.
	 */
	public static Criterion simpleCmsPropertycontains(String propertyPath, String searchExpression) {
		return createSimpleCriterion(propertyPath, searchExpression, QueryOperator.CONTAINS);
	}

	/**
	 *  Same semantics with {@link #simpleCmsPropertycontains(String, String)} as
	 *  far as search expression concerns  but it is applies to {@link ComplexCmsProperty complex cms properties}.
	 *  
	 *  <p>
	 *  This method searches all child properties of provided complex property, 
	 *  whereas {@link #simpleCmsPropertycontains(String, String)} searches only specified
	 *  property path. For example, if a complex property <code>profile</code> has two 
	 *  child properties <code>title</code> and <code>description</code> then
	 *  a call to method 
	 *  <pre>
	 *   contains("profile", "*bar*")
	 *  </pre>
	 *  
	 *  will match all <code>profile</code> properties which contain at least one child
	 *  property whose value(s) contains word "bar".
	 *  </p>
	 *  
	 *  @deprecated Use {@link #contains(String, String)} instead.
	 *  
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link ComplexCmsProperty complex cms properties}.
	 *            
	 * @param searchExpression
	 *            Search value. Same semantics with {@link #simpleCmsPropertycontains(String, String)}
	 * @return A <code>contains</code> constraint.
	 */
	public static Criterion complexCmsPropertycontains(String propertyPath, String 	searchExpression) {
		SimpleCriterionImpl criterion = (SimpleCriterionImpl) createSimpleCriterion(propertyPath, searchExpression, QueryOperator.CONTAINS);
		criterion.propertyIsComplex();
		
		return criterion;
	}

	/**
	 * Creates an "equals" restriction for property.
	 *  
	 * <p>
	 * Note that multi valued simple properties,
	 *  will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>equals("amounts", "26")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * 
	 * <p>
	 * In cases where this criterion represents a reference criterion then value should be prefixed as follows :
	 * 
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * content object then value should be prefixed with {@link CmsConstants#CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * topic then value should be prefixed with {@link CmsConstants#TOPIC_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value. 
	 * @return A <code>not equals</code> constraint.
	 */
	public static Criterion equals(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.EQUALS);
	}

	/**
	 * Creates an "equals" restriction for property but for a list of values.
	 * Example :
	 * 
	 * <pre>equals("index", Condition.OR, Arrays.asList(1, 2))</pre> 
	 * <p>
	 * will constrain query results to entities whose <code>index</code>
	 * property has value <code>1</code> or <code>2</code>.
	 * </p>
	 *  
	 * <p>
	 * Note that multi valued simple properties,
	 *  will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>equals("amounts", Condition.OR, Arrays.asList(1, 2))</code>, this property 
	 * will not match the specified criterion.  
	 * </p>
	 * 
	 * <p>
	 * In cases where this criterion represents a reference criterion then values should be prefixed as follows :
	 * 
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * content object then each value should be prefixed with {@link CmsConstants#CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * topic then each values should be prefixed with {@link CmsConstants#TOPIC_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param internalCondition
	 *            {@link Condition#AND} or {@link Condition#OR}.
	 * @param values
	 *            Constraint values.
	 *            
	 * @return An <code>equals</code> constraint.
	 */
	public static Criterion equals(String propertyPath, Condition internalCondition,
			List values) {
		return createSimpleCriterion(propertyPath, values, internalCondition, QueryOperator.EQUALS);
	}
	
	/**
	 * Same semantics with {@link #equals(String, Object)} but ignores case. Examples :
	 * 
	 * <pre>equals("name", "bar")</pre>
	 * <p>
	 * will match all properties named <code>name</code> and have value(s) : 
	 * <code>Bar</code>, <code>BAR</code>, etc.
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return An <code>equals</code> constraint.
	 */
	public static Criterion equalsCaseInsensitive(String propertyPath, Object value) {
		return CriterionUtils.createSimpleCriterion(propertyPath, value, QueryOperator.EQUALS, CaseMatching.LOWER_CASE);
	}

	/**
	 * Creates a "greater than" restriction for property. 
	 * 
	 * <p>
	 * Note that multi valued simple properties
	 * will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>greaterThan("amounts", "25")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>greater than</code> constraint.
	 */
	public static Criterion greaterThan(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.GREATER);
	}

	/**
	 * Creates a "greater than or equals" restriction for property. 
	 *
	 * <p>
	 * Note that multi valued simple properties,
	 * will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>greaterThanOrEquals("amounts", "23.4")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>greater than or equals</code> constraint.
	 */
	public static Criterion greaterThanOrEquals(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.GREATER_EQUAL);
	}

	/**
	 * Creates a restriction for property existence. For example, 
	 * 
	 * <p>
	 * <code>isNotNull("title")</code> will constrain query results to entities 
	 * whose <code>title</code> property exists.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 *            
	 * @return A <code>not null</code> constraint.
	 */
	public static Criterion isNotNull(String propertyPath) {
		return createSimpleCriterion(propertyPath, null, null,QueryOperator.IS_NOT_NULL);
	}

	/**
	 * Creates a restriction for property nonexistence. Examples :
	 * 
	 * <p>
	 * <code>isNull("title")</code> will constrain query results to entities
	 * whose <code>title</code> property does not exist.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 *            
	 * @return A <code>null</code>  constraint.
	 */
	public static Criterion isNull(String propertyPath) {
		return createSimpleCriterion(propertyPath, null, null, QueryOperator.IS_NULL);
	}

	/**
	 * Creates a "less than" restriction for property. 
	 * 
	 * <p>
	 * Note that multi valued simple properties,
	 * will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>lessThan("amounts", "26")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>less than</code> constraint.
	 */
	public static Criterion lessThan(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.LESS);
	}

	/**
	 * Creates a "less than or equals" restriction for property.
	 *  
	 * <p>
	 * Note that multi valued simple properties,
	 *  will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>lessThanOrEquals("amounts", "26")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>less than</code> constraint.
	 */
	public static Criterion lessThanOrEquals(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.LESS_EQUAL);
	}

	/**
	 * Like criterion is based on LIKE in SQL. 
	 * 
	 * <p>
	 * Use character <code>%</code> before and/or
	 * after value to search for any string of zero or more characters or
	 * character <code>_</code> for any single character. Examples :
	 * </p>
	 * <ul>
	 * <li> <code>like('title', '%val')</code>
	 * <p>
	 *	will match all
	 * <code>title</code> properties whose values end with the string
	 * <code>val</code>
	 * </p>
	 * <li> <code>like('title', 'val%')</code>
	 * <p>
	 * will match all
	 * <code>title</code> properties whose values start with the string
	 * <code>val</code>
	 * </p>
	 * <li> <code>like('title', '%val%')</code>
	 * <p>
	 * will match all
	 * <code>title</code> properties whose values contain with the substring
	 * <code>val</code>
	 * </p>
	 * </ul>
	 * 
	 * Use of this criterion is case sensitive.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param searchExpression
	 *            Search value.
	 * @return A <code>like</code> constraint.
	 */
	public static Criterion like(String propertyPath, String searchExpression) {
		return createSimpleCriterion(propertyPath, searchExpression, QueryOperator.LIKE);
	}
	
	/**
	 * Same semantics with {@link #like(String, String)} but ignores case. Examples :
	 * 
	 * <pre>like("name", "bar")</pre>
	 * <p>
	 * will match all properties named <code>name</code> and have value(s) like: 
	 * <code>Bar</code>, <code>BAR</code>, etc.
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>like</code> constraint with no case sensitivity
	 */
	public static Criterion likeCaseInsensitive(String propertyPath, String value) {
		return CriterionUtils.createSimpleCriterion(propertyPath, value, QueryOperator.LIKE, CaseMatching.LOWER_CASE);
	}

	/**
	 * Same semantics with {@link #like(String, String)} but for multiple search
	 * expressions and a condition.
	 * 
	 * <p>
	 * Example : <code>like('title', Arrays.asList("Larry", "Moe"), Condition.OR )</code>
	 * will match all <code>title</code> properties whose values contain <code>Larry</code>
	 * or <code>Moe</code>. 
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param searchExpressions
	 *            Search values.
	 * @param internalCondition
	 *            Default value is {@link Condition#AND}.
	 * @return A <code>like</code> constraint.
	 */
	public static Criterion like(String propertyPath, List<String> searchExpressions,
			Condition internalCondition) {
		return createSimpleCriterion(propertyPath, searchExpressions, internalCondition, QueryOperator.LIKE);
	}

	/**
	 * Associates two criteria with the {@link Condition#OR or} expression.
	 * 
	 * @param leftHandSide
	 *            Left hand side of association.
	 * @param rightHandSide
	 *            Right hand side of association.
	 * @return An {@link ConditionalCriterion ORed} conditional criterion.
	 */
	public static Criterion or(Criterion leftHandSide, Criterion rightHandSide) {
		return newConditionalCriterion(leftHandSide, rightHandSide, Condition.OR);
	}

	public static  Criterion createSimpleCriterion(String propertyPath, List values, Condition internalCondition, QueryOperator operator){

		//Special case
		if (values != null && operator != null && ( operator == QueryOperator.EQUALS || operator == QueryOperator.NOT_EQUALS)){
			
			for (Object value : values){
				if (topicReferenceCriterionMustBeCreated(value)){
					return newTopicReferenceCriterion(propertyPath, values, internalCondition, operator, ((String)value).endsWith(CmsConstants.INCLUDE_CHILDREN_EXPRESSION));
				}
			}
		}

		
		return CriterionUtils.createSimpleCriterion(propertyPath, values, internalCondition, operator, CaseMatching.NO_CASE);
	}
	
	public static  Criterion createSimpleCriterion(String propertyPath, Object value, QueryOperator operator){
		
		//Special case
		if (operator != null && ( operator == QueryOperator.EQUALS || operator == QueryOperator.NOT_EQUALS) &&  topicReferenceCriterionMustBeCreated(value)){
			
			return newTopicReferenceCriterion(propertyPath, (String)value, operator, ((String)value).endsWith(CmsConstants.INCLUDE_CHILDREN_EXPRESSION));
		}

		return CriterionUtils.createSimpleCriterion(propertyPath, value, operator, CaseMatching.NO_CASE);
	}

	private static boolean topicReferenceCriterionMustBeCreated(Object value) {
		
		 return value != null &&
		 		value instanceof String && 
		 		( ((String)value).startsWith(CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX) ||
		 				((String)value).endsWith(CmsConstants.INCLUDE_CHILDREN_EXPRESSION) );
	}

	private static  RangeCriterion createRangeCriterion(String propertyPath, Object lowerLimit, Object upperLimit)
	{
		RangeCriterion rangeCriteria = new RangeCriterion();
		rangeCriteria.setProperty(propertyPath);
		rangeCriteria.setLowerLimit(lowerLimit);
		rangeCriteria.setUpperLimit(upperLimit);
		
		return rangeCriteria;
	}
	private static ConditionalCriterion newConditionalCriterion(Criterion leftHandSide, Criterion rightHandSide, Condition condition)
	{
		ConditionalCriterion conditionalCriterion = new ConditionalCriterionImpl();
		conditionalCriterion.setCondition(condition);
		conditionalCriterion.setLeftHandSide(leftHandSide);
		conditionalCriterion.setRightHandSide(rightHandSide);
		
		return conditionalCriterion;	
	}


	/**
	 * Creates a range criterion between two values.
	 * 
	 * @return An empty criterion.
	 */
	public static RangeCriterion newRangeCriterion(String property, Object lowerLimit, Object upperLimit) {

		RangeCriterion rangeCriterion = new RangeCriterion();
		rangeCriterion.setProperty(property);
		rangeCriterion.setLowerLimit(lowerLimit);
		rangeCriterion.setUpperLimit(upperLimit);
		
		return rangeCriterion;
	}

	/**
	 * Creates a "notEquals" restriction for property. 
	 *  
	 * <p>
	 * Note that multi valued simple properties,
	 * will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>notEquals("amounts", "26")</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * <p>
	 * In cases where this criterion represents a reference criterion then value should be prefixed as follows :
	 * 
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * content object then value should be prefixed with {@link CmsConstants#CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * topic then value should be prefixed with {@link CmsConstants#TOPIC_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * </p>
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param value
	 *            Constraint value.
	 *            
	 * @return A <code>not equals</code> constraint.
	 */
	public static Criterion notEquals(String propertyPath, Object value) {
		return createSimpleCriterion(propertyPath, value, QueryOperator.NOT_EQUALS);
	}


	/**
	 * Creates a "not equals" restriction for property for a list of values.
	 * Example :
	 * 
	 * <pre>notEquals("index", Condition.AND, Arrays.asList(1, 2)</pre> 
	 * <p>
	 * will constrain query results to entities whose <code>index</code>
	 * property does not have values <code>1</code>  and <code>2</code>.
	 * </p>
	 *  
	 * <p>
	 * Note that multi valued simple properties,
	 *  will match a criterion if at least ONE of their
	 * values satisfies the criterion. For example if there is a multi valued property called
	 * 	<code>amounts</code> and has values <code>23.4, 30.5</code> and  
	 * the following criterion is set <code>notEquals("amounts", Condition.AND, Arrays.asList(1, 2))</code>, this property will match 
	 * the specified criterion.  
	 * </p>
	 * 
	 * <p>
	 * In cases where this criterion represents a reference criterion then values should be prefixed as follows :
	 * 
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * content object then each value should be prefixed with {@link CmsConstants#CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * If criterion's property path corresponds to a property whose type is a reference to a 
	 * topic then each value should be prefixed with {@link CmsConstants#TOPIC_REFERENCE_CRITERION_VALUE_PREFIX}.
	 * </p>
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)}.
	 * @param internalCondition
	 *            {@link Condition#AND} or {@link Condition#OR}.
	 * @param values
	 *            Constraint values.
	 *            
	 * @return A <code>not equals</code> constraint.
	 */
	public static Criterion notEquals(String propertyPath,
			Condition internalCondition, List values) {
		return createSimpleCriterion(propertyPath, values, internalCondition, QueryOperator.NOT_EQUALS);
	}

	/**
	 * Creates a localization criterion.
	 * 
	 * @return A constraint about localized labels for an entity.
	 */
	public static LocalizationCriterion newLocalizationCriterion() {
		return new LocalizationCriterionImpl();
	}

	/**
	 * Create a new criterion which negates the provided criterion.
	 * 
	 * @param criterion
	 * @return
	 */
	public static Criterion not(Criterion criterion){
		return new NotCriterion(criterion);
	}
	
	/**
	 * Create a {@link Criterion criterion} representing all query restrictions contained in the 
	 * provided expression and add it to provided {@link ContentObjectCriteria}.
	 * 
	 * <p>
	 * This method parses any string which contains query restrictions of the form 
	 * <code>propertyPath operator value</code> combined together with one or more
	 * condition operators ({@link Condition#AND AND}, {@link Condition#OR OR}) with <code>AND</code>
	 * having higher precedence than <code>OR</code>.It also supports 
	 * the use of parenthesis in order to define a different precedence among the 
	 * restrictions.
	 * </p>
	 * 
	 * <p>
	 * <code>propertyPath</code> has the same semantics as {@link SimpleCriterion#setProperty(String)}, 
	 * except that it does not support index information, that is <code>profile.title[1]</code> is not 
	 * acceptable.
	 * </p>
	 * 
	 * <p>
	 * For convenience , there are several reserved property paths:
	 * <ul>
	 * <li><b>contentType</b>, which refers to built in property {@link CmsBuiltInItem#ContentObjectTypeName}</li>
	 * <li><b>textSearched</b>, which enabled full text search by calling {@link ContentObjectCriteria#addFullTextSearchCriterion(String)}</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <code>operator</code> can be one of 
	 * <ul>
	 * 	<li>{@link QueryOperator#EQUALS =}</li>
	 * <li>{@link QueryOperator#NOT_EQUALS !=}</li>
	 * <li>{@link QueryOperator#LESS <}</li>
	 * <li>{@link QueryOperator#LESS_EQUAL <=}</li>
	 * <li>{@link QueryOperator#GREATER >}</li>
	 * <li>{@link QueryOperator#GREATER_EQUAL >=}</li>
	 * <li>{@link QueryOperator#IS_NOT_NULL IS_NOT_NULL}</li>
	 * <li>{@link QueryOperator#IS_NULL IS_NULL}</li>
	 * <li>{@link QueryOperator#CONTAINS CONTAINS}</li>
	 * <li>{@link QueryOperator#LIKE LIKE} but using <code>%%</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Finally <code>value</code> can be any string literal between single (') or double quotes(").
	 * In case you enclose value inside single quotes, then you may use double quotes within the search expression, 
	 * as long as these quotes are not place right after the first single quote or right before the last one.<br/>
	 * 
	 * For Boolean values accepted entries are <code>true, TRUE, false, FALSE</code>.<br/>
	 * 
	 * In cases where operator is <code>CONTAINS</code> ({@link QueryOperator#LIKE}) then value
	 * can contain special character '*' as described in {@link #like(String, String)}.
	 * Also, in cases where operator is <code>%%</code> ({@link QueryOperator#LIKE}) then value
	 * can contain special character '%' as described in {@link #like(String, String)}.
	 * 
	 * As far as date values concern, ISO8601 international standard representation is used. That means
	 * that date values must follow the pattern
	 * <pre>
	 * 		yyyy-MM-ddTHH:mm:ss.SSSTZD
	 * </pre>
	 * 
	 * where
	 * <pre>
	 *   yyyy represent four-digit year 
	 *   MM   represent two-digit month starting with 01 for January
	 *   dd   represent two-digit day of month. First day is 01, last is 31
	 *   T	  is a single character denoting that Time will follow
	 *   HH   represent two digits of hour starting from 00 till 23 
	 *   mm   represent two digits of minute starting from 00 till 59
	 *   ss   represent two digits of second starting from 00 till 59
	 *   SSS  represent three digits of milliseconds starting from 000 till 999
	 *   TZD  represent time zone designator, Z for UTC or an offset from UTC
	 *           in the form of +hh:mm or -hh:mm
	 * </pre>
	 * 
	 * For user convenience, date value contain only date information, that is yyyy-MM-dd
	 * or may have date time information without milliseconds, that is yyyy-MM-ddTHH:mm.ssTZD
	 * or may have no time zone as well, that is yyyy-MM-ddTHH:mm.ss. For all of these formats
	 * default values will apply to the element missing.
	 * </p>
	 * 
	 * <p>
	 * Here some several examples of valid expressions
	 * <ul>
	 * <li><code> (title="true") </code></li>
	 * <li><code> (title="false") </code></li>
	 * <li><code> (title="TRUE") </code></li>
	 * <li><code> (title="FALSE") </code></li>
	 * <li><code> (title="news") </code></li>
	 * <li><code> (title!="news") </code></li>
	 * <li><code> (title CONTAINS "news*") </code></li>
	 * <li><code> (profile CONTAINS "news*") </code>, support for complex properties as well. This restriction instructs Astroboa to search all values of the child properties 
	 * of "profile" property.</li>
	 * <li><code> (startDate="2009-04-14T19:21:51.000+03:00")  </code></li>  
	 * <li><code> (startDate="2009-04-14T19:21:51+03:00") </code></li>
	 * <li><code> (startDate="2009-04-14T19:21:51Z") </code></li>
	 * <li><code> (startDate="2009-04-14T19:21:51-01:00") </code></li>  
	 * <li><code> (startDate="2009-04-14")</code></li>
	 * <li><code> (title%%"news") </code></li>
	 * * <li><code> (title%%"news%") </code></li>
	 * <li><code> (title="ne'ws") </code></li>
	 * <li><code> (title='ne"ws') </code></li>
	 * <li><code> title IS_NOT_NULL </code></li>
	 * <li><code> title IS_NULL </code></li>
	 * <li><code> title IS_NULL AND profile.subject="news" </code></li>
	 * <li><code> (title IS_NULL) AND (profile.subject="news") </code></li>
	 * <li><code> ( (title IS_NULL) AND (profile.subject="news") ) </code></li>
	 * <li><code> ( title IS_NULL AND profile.subject="news" ) </code></li>
	 * <li><code> ( (title IS_NULL) AND profile.subject="news" ) </code></li>
	 * <li><code> title="news" AND profile.subject="news" </code></li>
	 * <li><code> (title="news") AND (profile.subject="news") </code></li>
	 * <li><code> ( (title="news") AND (profile.subject="news") ) </code></li>
	 * <li><code> ( title="news" AND profile.subject="news" ) </code></li>
	 * <li><code> ( (title="news") AND profile.subject="news" ) </code></li>
	 * <li><code> title="news" AND profile.subject="news" AND profile.title.en="article" </code></li>
	 * <li><code> (title="news") AND (profile.subject="news") AND (profile.title.en="article") </code></li>
	 * <li><code> ( (title="news") AND (profile.subject="news") AND (profile.title.en="article") ) </code></li>
	 * <li><code> ( title="news" AND profile.subject="news" AND profile.title.en="article" ) </code></li>
	 * <li><code> ( (title="news" AND profile.subject="news") AND profile.title.en="article" ) </code></li>
	 * <li><code> ( (title="news" AND (profile.subject="news")) AND profile.title.en="article" ) </code></li>
	 * <li><code> title="news" OR profile.subject="news" </code></li>
	 * <li><code> (title="news") OR (profile.subject="news") </code></li>
	 * <li><code> ( (title="news") OR (profile.subject="news") ) </code></li>
	 * <li><code> ( title="news" OR profile.subject="news" ) </code></li>
	 * <li><code> ( (title="news") OR profile.subject="news" ) </code></li>
	 * <li><code> title="news" OR profile.subject="news" OR profile.title.en="article" </code></li>
	 * <li><code> (title="news") OR (profile.subject="news") OR (profile.title.en="article") </code></li>
	 * <li><code> ( (title="news") OR (profile.subject="news") OR (profile.title.en="article") ) </code></li>
	 * <li><code> ( title="news" OR profile.subject="news" OR profile.title.en="article" ) </code></li>
	 * <li><code> ( (title="news" OR profile.subject="news") OR profile.title.en="article" ) </code></li>
	 * <li><code> ( (title="news" OR (profile.subject="news")) OR profile.title.en="article" ) </code></li>
	 * <li><code> title="news" AND profile.subject="news" OR profile.title.en="article" </code></li>
	 * <li><code> (title="news" AND profile.subject="news") OR profile.title.en="article" </code></li>
	 *</ul>
	 * </p>
	 * 
 	 * <p>
	 * Here some several examples of INVALID expressions
	 * <ul>
	 * <li><code> (title="ne'ws'") </code></li>
	 * <li><code> (title='"news') </code></li>
	 * </ul> 
	 * </p>
	 * 
	 * 
	 * @param expression A string representing query restrictions
	 * @param contentObjectCriteria Criteria where the constructed criterion will be adjusted 
	 * @return {@link Criterion Criterion} representing the provided expression
	 */
	public static void parse(String expression, ContentObjectCriteria contentObjectCriteria){
		
		if (StringUtils.isBlank(expression)){
			return;
		}
		
		InputStream expressionStream = null;
		
		try{
			
			expressionStream = IOUtils.toInputStream(expression);
			CriterionParser criterionParser = new CriterionParser(expressionStream);
			
			criterionParser.parseExpressionAndAppendCriteria(contentObjectCriteria);
		}
		catch(Exception e){
			throw new CmsException("Expression "+expression, e);
		}
		finally{
			IOUtils.closeQuietly(expressionStream);
		}
	}
	
	/**
	 *
	 * @deprecated Use {@link #newTopicReferenceCriterion(String, Object, QueryOperator, boolean)} instead
	 *  
	 *  
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topicId
	 * 			Topic Id to match
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicPropertyCriterion(String propertyPath, Object topicId, QueryOperator operator, boolean includeSubTopics){
		return newTopicReferenceCriterion(propertyPath, (String)topicId, operator, includeSubTopics);
	}
	
	/**
	 * @deprecated Use {@link #newTopicReferenceCriterion(String, List, Condition, QueryOperator, boolean)} instead.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topicIds
	 * 			List of topic Ids to match
	 * @param internalCondition
	 * 			Whether to look for properties which contain ALL provided <code>topicIds</code> {@link Condition#AND} or
	 * 			they contain either of the provided <code>topicIds<code>
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicPropertyCriterion(String propertyPath, List topicIds, Condition internalCondition, QueryOperator operator, boolean includeSubTopics){
		return newTopicReferenceCriterion(propertyPath, topicIds, internalCondition, operator, includeSubTopics);
	}
	
	/**
	 * Criterion used to match all content objects related to <code>topicIds</code> via property <code>propertyPath</code>.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link Topic} and their property path is unknown or does not matter. This criterion will create several constraints,
	 * one for each property of type {@link Topic} defined in content model which accepts topics belonging to 
	 * the same {@link Taxonomy taxonomy} with provided <code>topicId</code> or accepts topics from 
	 * any {@link Taxonomy taxonomy}.
	 * 
	 *  
	 *  
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topicIdOrName
	 * 			Topic id or name to match
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicReferenceCriterion(String propertyPath, String topicIdOrName, QueryOperator operator, boolean includeSubTopics){
		
		TopicReferenceCriterion topicReferenceCriterion = new TopicReferenceCriterionImpl();
		
		if (includeSubTopics){
			topicReferenceCriterion.expandCriterionToIncludeSubTopics();
		}
		
		topicReferenceCriterion.setProperty(propertyPath);
		topicReferenceCriterion.setOperator(operator);

		processValueAndAddItToCriterion(topicIdOrName, topicReferenceCriterion,CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX);
		
		return topicReferenceCriterion;

	}
	
	/**
	 * Criterion used to match all content objects related to <code>topicIds</code> via property <code>propertyPath</code>.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link Topic}. In cases where property is unknown or does not matter,  this criterion will create several constraints,
	 * one for each property of type {@link Topic} defined in content model which accepts topics belonging to 
	 * the same {@link Taxonomy taxonomy} with provided <code>topicId</code> or accepts topics from 
	 * any {@link Taxonomy taxonomy}.

	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topicsOrTopicIdsOrTopicNames
	 * 			List of topic Ids to match
	 * @param internalCondition
	 * 			Whether to look for properties which contain ALL provided <code>topicIds</code> {@link Condition#AND} or
	 * 			they contain either of the provided <code>topicIds<code>
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicReferenceCriterion(String propertyPath, List topicsOrTopicIdsOrTopicNames, Condition internalCondition, QueryOperator operator, boolean includeSubTopics){
		TopicReferenceCriterion topicReferenceCriterion = new TopicReferenceCriterionImpl();
		
		if (includeSubTopics){
			topicReferenceCriterion.expandCriterionToIncludeSubTopics();
		}
		
		topicReferenceCriterion.setProperty(propertyPath);
		topicReferenceCriterion.setInternalCondition(internalCondition);
		topicReferenceCriterion.setOperator(operator);
		
		if (topicsOrTopicIdsOrTopicNames != null){
			for (Object topicsOrTopicIdsOrTopicName : topicsOrTopicIdsOrTopicNames){
				
				if (topicsOrTopicIdsOrTopicName != null){ 
					if (topicsOrTopicIdsOrTopicName instanceof Topic){
						topicReferenceCriterion.addTopicAsAValue((Topic) topicsOrTopicIdsOrTopicName);
					}
					else if (topicsOrTopicIdsOrTopicName instanceof String){
						processValueAndAddItToCriterion((String) topicsOrTopicIdsOrTopicName, topicReferenceCriterion,CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX);
					}
					else{
						topicReferenceCriterion.addValue(topicsOrTopicIdsOrTopicName);
					}
				}
			}
		}
		
		
		return topicReferenceCriterion;
	}
	
	/**
	 * Criterion used to match all content objects related to <code>topic</code> via property <code>propertyPath</code>.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link Topic} and their property path is unknown or does not matter. This criterion will create several constraints,
	 * one for each property of type {@link Topic} defined in content model which accepts topics belonging to 
	 * the same {@link Taxonomy taxonomy} with provided <code>topicId</code> or accepts topics from 
	 * any {@link Taxonomy taxonomy}.
	 * 
	 *  
	 *  
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topic
	 * 			Topic to match
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicReferenceCriterion(String propertyPath, Topic topic, QueryOperator operator, boolean includeSubTopics){
		
		TopicReferenceCriterion topicReferenceCriterion = new TopicReferenceCriterionImpl();
		
		if (includeSubTopics){
			topicReferenceCriterion.expandCriterionToIncludeSubTopics();
		}
		
		topicReferenceCriterion.setProperty(propertyPath);
		topicReferenceCriterion.setOperator(operator);
		topicReferenceCriterion.addTopicAsAValue(topic);
		
		return topicReferenceCriterion;

	}
	
	/**
	 * Criterion used to match all content objects related to <code>topics</code> via property <code>propertyPath</code>.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link Topic}. In cases where property is unknown or does not matter,  this criterion will create several constraints,
	 * one for each property of type {@link Topic} defined in content model which accepts topics belonging to 
	 * the same {@link Taxonomy taxonomy} with provided <code>topicId</code> or accepts topics from 
	 * any {@link Taxonomy taxonomy}.

	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param topics
	 * 			List of topics to match
	 * @param internalCondition
	 * 			Whether to look for properties which contain ALL provided <code>topicIds</code> {@link Condition#AND} or
	 * 			they contain either of the provided <code>topicIds<code>
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @param includeSubTopics
	 * 			Whether to look for content objects which refers to any of topic children as well as topic itself
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newTopicReferenceListCriterion(String propertyPath, List<Topic> topics, Condition internalCondition, QueryOperator operator, boolean includeSubTopics){
		TopicReferenceCriterion topicReferenceCriterion = new TopicReferenceCriterionImpl();
		
		if (includeSubTopics){
			topicReferenceCriterion.expandCriterionToIncludeSubTopics();
		}
		
		topicReferenceCriterion.setProperty(propertyPath);
		topicReferenceCriterion.setInternalCondition(internalCondition);
		topicReferenceCriterion.setOperator(operator);
		topicReferenceCriterion.addTopicsAsValues(topics);
		
		return topicReferenceCriterion;
	}
	/**
	 * Criterion used to match all content object properties of type {@link ContentObject} ( {@link ObjectReferenceProperty}) which 
	 * refer to a {@link ContentObject}.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link ContentObject}.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param contentObjectsOrContentObjectIdsOrContentObjectSystemNames
	 * 				List of content objects whose identifiers or system names will be used in criterion values or
	 * list of content object identifiers or content object system names
	 * @param internalCondition
	 * 			Whether to look for properties which refer to ALL provided <code>contentObjectReferences</code> {@link Condition#AND} or
	 * 			they refer to any of the provided <code>contentObjectReferences<code>
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newContentObjectReferenceCriterion(String propertyPath, List contentObjectsOrContentObjectIdsOrContentObjectSystemNames,Condition internalCondition, QueryOperator operator){
		
		ContentObjectReferenceCriterion contentObjectReferenceCriterion = new ContentObjectReferenceCritetionImpl();
		contentObjectReferenceCriterion.setProperty(propertyPath);
		contentObjectReferenceCriterion.setOperator(operator);
		contentObjectReferenceCriterion.setInternalCondition(internalCondition);

		if (contentObjectsOrContentObjectIdsOrContentObjectSystemNames != null){
			
			for (Object contentObjectsOrContentObjectIdsOrContentObjectSystemName : contentObjectsOrContentObjectIdsOrContentObjectSystemNames){
				
				if (contentObjectsOrContentObjectIdsOrContentObjectSystemName != null){
					if (contentObjectsOrContentObjectIdsOrContentObjectSystemName instanceof ContentObject){
						contentObjectReferenceCriterion.addContentObjectAsAValue((ContentObject)contentObjectsOrContentObjectIdsOrContentObjectSystemName);
					}
					else if (contentObjectsOrContentObjectIdsOrContentObjectSystemName instanceof String){
						processValueAndAddItToCriterion((String)contentObjectsOrContentObjectIdsOrContentObjectSystemName,contentObjectReferenceCriterion,CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX);
					}
					else{
						contentObjectReferenceCriterion.addValue(contentObjectsOrContentObjectIdsOrContentObjectSystemName);
					}
				}
			}
		}

		
		return contentObjectReferenceCriterion;
	}
	/**
	 * Criterion used to match all content object properties of type {@link ContentObject} ( {@link ObjectReferenceProperty}) which 
	 * refer to a {@link ContentObject}.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link ContentObject}.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param contentObjectReference
	 * 				Content object whose identifier or system name will be used in criterion values
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newContentObjectReferenceCriterion(String propertyPath, ContentObject contentObjectReference,QueryOperator operator){
		
		ContentObjectReferenceCriterion contentObjectReferenceCriterion = new ContentObjectReferenceCritetionImpl();
		contentObjectReferenceCriterion.setProperty(propertyPath);
		contentObjectReferenceCriterion.addContentObjectAsAValue(contentObjectReference);
		contentObjectReferenceCriterion.setOperator(operator);
		
		return contentObjectReferenceCriterion;
	}
	
	/**
	 * Criterion used to match all content object properties of type {@link ContentObject} ( {@link ObjectReferenceProperty}) which 
	 * refer to a {@link ContentObject}.
	 * 
	 * This is a useful method when searching for {@link ContentObject contentObjects} which refer to a specific
	 * {@link ContentObject}.
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param contentObjectReferenceIdOrSystemName
	 * 				Content object identifier or system name 
	 * @param operator
	 * 			{@link QueryOperator}. Mainly, {@link QueryOperator#equals(Object)}
	 * 
	 * @return Criterion object ready to be used inside and ORed or ANDed criterion or to be added directly to
	 * 		{@link ContentObjectCriteria}
	 */
	public static Criterion newContentObjectReferenceCriterion(String propertyPath, String contentObjectReferenceIdOrSystemName,QueryOperator operator){
		
		ContentObjectReferenceCriterion contentObjectReferenceCriterion = new ContentObjectReferenceCritetionImpl();
		contentObjectReferenceCriterion.setProperty(propertyPath);
		contentObjectReferenceCriterion.setOperator(operator);
		
		processValueAndAddItToCriterion(contentObjectReferenceIdOrSystemName,contentObjectReferenceCriterion, CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX);
		
		return contentObjectReferenceCriterion;
	}

	private static void processValueAndAddItToCriterion(String referenceIdOrSystemName,
			SimpleCriterion referenceCriterion, String expectedPrefix) {
		
		if (referenceIdOrSystemName != null && referenceCriterion != null){
			
			//1. Reference Id or system name may end with /*
			//   This is taken into account only when criterion
			//   corresponds to a Topic Reference
			if (referenceCriterion instanceof TopicReferenceCriterion && referenceIdOrSystemName.endsWith(CmsConstants.INCLUDE_CHILDREN_EXPRESSION)){
				
				((TopicReferenceCriterion)referenceCriterion).expandCriterionToIncludeSubTopics();
				
				//Remove suffix and continue processing
				referenceIdOrSystemName  =StringUtils.substringBeforeLast(referenceIdOrSystemName, CmsConstants.INCLUDE_CHILDREN_EXPRESSION);
			}

			//2. If value represents a system name , add the proper prefix
			if  (CmsConstants.SystemNamePattern.matcher(referenceIdOrSystemName).matches()){
				referenceCriterion.addValue(expectedPrefix+referenceIdOrSystemName);
			}
			//3. In all other cases, add value as is
			else {
				referenceCriterion.addValue(referenceIdOrSystemName);
			}
		}
	}

	/**
	 * Create a {@link Criterion criterion} representing all query restrictions contained in the 
	 * provided expression and add it to provided {@link TopicCriteria}.
	 * 
	 * <p>
	 * This method parses any string which contains query restrictions of the form 
	 * <code>propertyPath operator value</code> combined together with one or more
	 * condition operators ({@link Condition#AND AND}, {@link Condition#OR OR}) with <code>AND</code>
	 * having higher precedence than <code>OR</code>.It also supports 
	 * the use of parenthesis in order to define a different precedence among the 
	 * restrictions.
	 * </p>
	 * 
	 * <p>
	 * Users can search topic by name, by label in a locale or in any locale, by its ancestor's name, 
	 * by its ancestor's label or by its taxonomy's name. Search by taxonomy's label will be supported 
	 * in the next release.
	 * </p>
	 * 
	 * <p>
	 * Therefore <code>propertyPath</code> can have one of the following values :
	 * 
	 * <ul>
	 * <li><code>id</code> represents the id of a topic</li>
	 * <li><code>name</code> represents the name of a topic</li>
	 * <li><code>label</code> represents a label of a topic in any locale</li>
	 * <li><code>label.ANY_LOCALE</code> where ANY_LOCALE, can be replaced with "en", "el", etc and represents a label of a topic in a specific locale</li>
	 * <li><code>ancestor.id</code> represents the id of a topic's ancestor</li>
	 * <li><code>ancestor.name</code> represents the name of a topic's ancestor</li>
	 * <li><code>ancestor.label</code> represents a label of a topic's ancestor in any locale</li>
	 * <li><code>ancestor.label.ANY_LOCALE</code> where ANY_LOCALE, can be replaced with "en", "el", etc and represents a label of a topic's ancestor in a specific locale</li>
	 * <li><code>taxonomy</code> represents the name of a topic's taxonomy</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <code>operator</code> can be one of 
	 * <ul>
	 * 	<li>{@link QueryOperator#EQUALS =}</li>
	 * <li>{@link QueryOperator#NOT_EQUALS !=}</li>
	 * <li>{@link QueryOperator#CONTAINS CONTAINS}</li>
	 * <li>{@link QueryOperator#LIKE LIKE} but using <code>%%</code></li>
	 * </ul>
	 * 
	 * All other operators are ignored.
	 * </p>
	 * 
	 * <p>
	 * Finally <code>value</code> can be any string literal between single (') or double quotes(").
	 * In case you enclose value inside single quotes, then you may use double quotes within the search expression, 
	 * as long as these quotes are not place right after the first single quote or right before the last one.<br/>
	 * 
	 * In cases where operator is <code>CONTAINS</code> ({@link QueryOperator#LIKE}) then value
	 * can contain special character '*' as described in {@link #like(String, String)}.
	 * Also, in cases where operator is <code>%%</code> ({@link QueryOperator#LIKE}) then value
	 * can contain special character '%' as described in {@link #like(String, String)}.
	 * 
	 * <p>
	 * Here some several examples of valid expressions
	 * <ul>
	 * <li><code> (name="sports") </code></li>
	 * <li><code> (name!="sports") </code></li>
	 * <li><code> (name%%"news") </code></li>
	 * <li><code> (name%%"news%") </code></li>
	 * <li><code> (name="ne'ws") </code></li>
	 * <li><code> (name='ne"ws') </code></li>
	 * <li><code> name="sports" AND ancestor.name="news" </code></li>
	 * <li><code> (name="sports") AND (ancestor.name="news") </code></li>
	 * <li><code> ( (name="sports") AND (ancestor.name="news") ) </code></li>
	 * <li><code> ( name="sports" AND ancestor.name="news" ) </code></li>
	 * <li><code> ( (name="sports") AND ancestor.name="news" ) </code></li>
	 * <li><code> name="sport" AND ancestor.label CONTAINS "ne*" </code></li>
	 * <li><code> name="sport" AND ancestor.label.en CONTAINS "ne*" </code></li>
	 * <li><code> name CONTAINS "sport" AND ancestor.label.en CONTAINS "ne*" </code></li>
	 * <li><code> label CONTAINS "sport*" AND ancestor.label.en CONTAINS "ne*" </code></li>
	 * <li><code> taxonomy="sport" AND label CONTAINS "sport*" AND ancestor.label.en CONTAINS "ne*" </code></li>
	 *</ul>
	 * </p>
	 * 
 	 * <p>
	 * Here some several examples of INVALID expressions
	 * <ul>
	 * <li><code> (name="ne'ws'") </code></li>
	 * <li><code> (name='"news') </code></li>
	 * </ul> 
	 * </p>
	 * 
	 * 
	 * @param expression A string representing query restrictions
	 * @param contentObjectCriteria Criteria where the constructed criterion will be adjusted 
	 * @return {@link Criterion Criterion} representing the provided expression
	 */
	public static void parse(String expression, TopicCriteria topicCriteria){
		
		if (StringUtils.isBlank(expression)){
			return;
		}
		
		InputStream expressionStream = null;
		
		try{
			
			expressionStream = IOUtils.toInputStream(expression);
			CriterionParser criterionParser = new CriterionParser(expressionStream);
			
			criterionParser.parseExpressionAndAppendTopicCriteria(topicCriteria);
		}
		catch(Exception e){
			throw new CmsException("Expression "+expression, e);
		}
		finally{
			IOUtils.closeQuietly(expressionStream);
		}
	}

	/**
	 * Contains criterion enables the use of full-text search. 
	 * 
	 * <p>
	 * All types
	 * of properties are searched including binary types. The following rules
	 * apply :
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Search expression can contain one or more terms separated by whitespace.A
	 * term can be a single word or a phrase delimited by double quotes (").
	 * <li>Search expression for all terms should contain whitespace (implicit AND) between 
	 * terms and for either of terms should contain OR.
	 * <li>Search expression can contain both AND-ed and OR-ed terms with AND having higher
	 * precedence.
	 * <li>Any term prefixed with - (minus sign) will not be included in the
	 * results.
	 * <li>Any term containing '*' character will result in matching values
	 * containing any string of zero or more characters in place of '*'. Note
	 * this behavior is disabled if term is a phrase.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Search expression examples :
	 * 
	 * <ul>
	 * <li><code>contains('title', 'java jdk')</code>
	 * <p>
	 * Matches all values containing <code>java</code> AND <code>jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '\"java jdk\"')</code>
	 * <p>
	 * Matches all values containing the phrase <code>java jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', 'java OR jdk')</code>
	 * <p>
	 * Matches all values containing <code>java</code> OR <code>jdk</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '\"java jdk\" OR 1.5')</code>
	 * <p>
	 * Matches all values which contain phrase <code>java jdk</code> OR
	 * <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '-\"java jdk\" 1.5')</code>
	 * <p>
	 * Matches all values which DO NOT contain phrase <code>java jdk</code>
	 * and contain <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', 'java* 1.5')</code>
	 * <p>
	 * Matches all values which contain strings that start with
	 * <code>java</code> and also contain string <code>1.5</code>.
	 * </p>
	 * 
	 * <li><code>contains('title', '*java* 1.5')</code>
	 * <p>
	 * Matches all values which contain string <code>java</code> and also
	 * contain string <code>1.5</code>.
	 * </p>
	 * 
	 * </ul>
	 * </p>
	 * 
	 * @param propertyPath
	 *            Same semantics as {@link SimpleCriterion#setProperty(String)} 
	 *            but it is restricted to {@link SimpleCmsProperty simple cms properties}.
	 * @param searchExpression
	 *            Search value.
	 * @return A <code>contains</code> constraint.
	 */
	public static Criterion contains(String propertyPath, String searchExpression) {
		return createSimpleCriterion(propertyPath, searchExpression, QueryOperator.CONTAINS);
	}
}
 