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

package org.betaconceptframework.astroboa.api.model.query.criteria;


import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Criteria API for building queries when 
 * searching for {@link ContentObject contentObjects}.
 * 
 * Provides helper methods for creating criteria mainly for
 * built in content object properties.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ContentObjectCriteria extends CmsCriteria {

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for matching ANY of provided content object types.
	 * 
	 * <p>
	 * In case a base content type is provided, then all content types
	 * which inherit from this content type are used in the criterion. For example,
	 * if a base content type <code>personType</code> and a content type <code>employee</code>
	 * are defined, then in case list contains value <code>personType</code> it will
	 * be replaced with value <code>employee</code>
	 * </p>
	 * 
	 * @param contentObjectTypes
	 *            A list of content object types.
	 */
	void addContentObjectTypesEqualsAnyCriterion(List<String> contentObjectTypes);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator for a specific content object type
	 * and add it to criteria list.
	 * 
	 * <p>
	 * In case a base content type is provided, then all content types
	 * which inherit from this content type are used in the criterion. For example,
	 * if a base content type <code>personType</code> and a content type <code>employee</code>
	 * are defined, then in case list contains value <code>personType</code> it will
	 * be replaced with value <code>employee</code>
	 * </p>
	 * 
	 * @param contentObjectType
	 *            Content object type criterion value.
	 */
	void addContentObjectTypeEqualsCriterion(String contentObjectType);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator for a specific content object type
	 * without adding it to criteria list
	 * 
	 * <p>
	 * In case a base content type is provided, then all content types
	 * which inherit from this content type are used in the criterion. For example,
	 * if a base content type <code>personType</code> and a content type <code>employee</code>
	 * are defined, then in case list contains value <code>personType</code> it will
	 * be replaced with value <code>employee</code>
	 * </p>
	 * 
	 * @param contentObjectType
	 *            Content object type criterion value.
	 * @return Created criterion
	 */
	Criterion createContentObjectTypeEqualsCriterion(String contentObjectType);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for matching ANY of provided content object types without adding it to criteria list.
	 *
	 * <p>
	 * In case a base content type is provided, then all content types
	 * which inherit from this content type are used in the criterion. For example,
	 * if a base content type <code>personType</code> and a content type <code>employee</code>
	 * are defined, then in case list contains value <code>personType</code> it will
	 * be replaced with value <code>employee</code>
	 * </p>
	 * 
	 * @param contentObjectTypes
	 *            A list of content object types.
	 * @return Created criterion
	 */
	Criterion createContentObjectTypesEqualsCriterion(List<String> contentObjectTypes);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator  
	 * for matching ANY of provided owner {@link CmsRepositoryEntity#getId() ids}.
	 *
	 * @param ownerIds	
	 *            A list of owner ids.
	 */
	void addOwnerIdsEqualsAnyCriterion(List<String> ownerIds);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator for a specific content object owner.
	 * 
	 * @param ownerId
	 *            Content object owner id.
	 */
	void addOwnerIdEqualsCriterion(String ownerId);

	/**
	 * Create a full text search criterion.
	 * 
	 * Create a criterion with {@link QueryOperator#CONTAINS contains} operator
	 * for every possible property of a content object.
	 * Search text may satisfy one of the following rules :
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
	 * @param searchText
	 *            Text to search in all properties of content object. 
	 */
	void addFullTextSearchCriterion(String searchText);

	/**
	 * Create criterion for content object subject.
	 * 
	 * <p>
	 * In order for the criterion to be valid there must be 
	 * a complex property named <code>profile</code> defined and which
	 * will contain a {@link TopicReferencePropertyDefinition topic} property named
	 * <code>subject</code>.
	 * </p>
	 * 
	 * <p>
	 * This method is used when searching for content objects whose property 
	 * <code>profile.subject</code> contains or not a topic with the specified <code>subjectId</code>
	 * and furthermore if <code>includeChildSubjectIds</code> is <code>true</code>, it will match
	 * all content objects whose property <code>profile.subject</code> contains or not ANY of
	 * topic or its children.
	 * </p> 
	 * 
	 * @param queryOperator
	 *            Query operator for criterion. 
	 *            Only {@link QueryOperator#EQUALS} and
	 *            {@link QueryOperator#NOT_EQUALS} are permitted
	 * @param subjectId
	 *            Subject id to search.
	 * @param includeChildSubjectIds
	 *            <code>true</code> to include subject's children ids in
	 *            criterion, <code>false</code> otherwise.
	 *            In first case all criteria that will be created will 
				  be ORed
	 */
	void addProfileSubjectIdCriterion(QueryOperator queryOperator,
			String subjectId, boolean includeChildSubjectIds);

	/**
	 * Same semantics with {@link #addProfileSubjectIdCriterion(QueryOperator, String, boolean)} but refers
	 * to more than one primary topics.
	 * 
	 * @param queryOperator
	 *            Query operator for criterion.
	 *            Only {@link QueryOperator#EQUALS} and
	 *            {@link QueryOperator#NOT_EQUALS} are permitted
	 * @param subjectIds
	 *            Subject ids to search.
	 * @param internalCondition
	 *            Condition to concatenate internal criteria between subject ids
	 *            In case subject's children are included, criteria created will be ORed
	 *            among them. For example, if there are 2 subject ids 'A' and 'D'
	 *            and contain 2 children each 'B','C' and 'E', 'F' and internal condition is AND
	 *            the following criteria will be created
	 *            <code> ( profile.subject=A OR profile.subject=B OR profile.subject=C) AND ( profile.subject=D OR profile.subject=E OR profile.subject=F)
	 * @param includeChildSubjectIds
	 *            <code>true</code> to include subject's children ids in
	 *            criterion, <code>false</code> otherwise.
	 */
	void addProfileSubjectIdsCriterion(QueryOperator queryOperator,
			List<?> subjectIds, Condition internalCondition,
			boolean includeChildSubjectIds);
	
	/**
	 * Copy all criteria and properties to another content object criteria.
	 * 
	 * @param targetContentObjectCriteria
	 */
	void copyTo(ContentObjectCriteria targetContentObjectCriteria);
	
	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * content object system name
	 * 
	 * @param systemName
	 *            System name value for criterion.
	 */
	void addSystemNameEqualsCriterion(String systemName);
	
	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * content object system name but ignores case.
	 * 
	 * @param systemName
	 *            System name value for criterion.
	 */
	void addSystemNameEqualsCriterionIgnoreCase(String systemName);
	
	/**
	 * Creates a criterion with {@link QueryOperator#NOT_EQUALS not equals} operator for
	 * content object system name.
	 * 
	 * @param systemName
	 *            System name value for criterion.
	 */
	void addSystemNameNotEqualsCriterion(String systemName);
	
	/**
	 * Creates a criterion with {@link QueryOperator#CONTAINS not equals} operator for
	 * content object system name.
	 * 
	 * @param systemName
	 *            System name value for criterion.
	 */
	void addSystemNameContainsCriterion(String systemName);
	
	/**
	 * Define a property which will be pre-loaded.
	 * 
	 * <p>
	 * The default behavior when querying Astroboa is that all content objects which match criteria
	 * do not contain any property at all but rather they are lazy loaded when needed.
	 * 
	 * This method provides a means to define which properties must be loaded in content objects
	 * before results return to user.
	 * </p>
	 * 
	 * <p>
	 * Note that in case results contain content object with various content types, all property paths
	 * must also been defined to ALL content types, otherwise an exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * In cases where the result of the query is XML, only these properties will be marshaled to XML.
	 * </p>
	 * 
	 * @param propertyPath Relative property path as provided by {@link CmsPropertyDefinition#getPath()}
	 */
	void addPropertyPathWhoseValueWillBePreLoaded(String propertyPath);
	
	/**
	 * Return all paths of properties which will be pre-loaded
	 * 
	 * @return A list of property paths 
	 */
	List<String> getPropertyPathsWhichWillBePreLoaded();
	
	/**
	 * Add order by relevance. 
	 * 
	 * Relevance is enabled when full text search criterion ({@link ContentObjectCriteria#addFullTextSearchCriterion(String)})
	 * is provided or a simple criterion with {@link QueryOperator#CONTAINS} operator.
	 *    
	 * @param order {@link Order#ascending Ascending} or {@link Order#ascending Descending} order
	 */
	void addOrderByRelevance(Order order);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for matching ANY of provided content object system names.
	 * 
	 * @param contentObjectSystemNames
	 *            A list of content object system names.
	 */
	void addSystemNameEqualsAnyCriterion(List<String> contentObjectSystemNames);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for matching ANY of provided content object system names without adding it to criteria list.
	 *
	 * @param contentObjectSystemNames
	 *            A list of content object system names.
	 * @return Created criterion
	 */
	Criterion createSystemNameEqualsAnyCriterion(List<String> contentObjectSystemNames);
	
	/**
	 * Add order by system name. 
	 * 
	 *  
	 *    
	 * @param order {@link Order#ascending Ascending} or {@link Order#ascending Descending} order 
	 */
	void addOrderBySystemName(Order order);
}
