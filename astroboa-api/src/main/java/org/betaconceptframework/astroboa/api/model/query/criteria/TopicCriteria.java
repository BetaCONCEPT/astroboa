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

package org.betaconceptframework.astroboa.api.model.query.criteria;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Criteria API for building queries when 
 * searching for {@link Topic topics}.
 * 
 * Provides helper methods for creating criteria mainly for
 * topic properties.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface TopicCriteria extends CmsCriteria{

	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator or
	 * a criterion with {@link QueryOperator#NOT_EQUALS not equals} operator 
	 * criterion for topic property {@link Topic#isAllowsReferrerContentObjects()}.
	 * 
	 * @param allowsReferrerContentObjects
	 *            <code>true</code> for searching topics which allow
	 *            referrer content objects, <code>false</code> otherwise.
	 */
	void addAllowsReferrerContentObjectsCriterion(
			boolean allowsReferrerContentObjects);

	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for topic owner id.
	 * 
	 * @param ownerId
	 *            Topic owner id.
	 */
	void addOwnerIdEqualsCriterion(String ownerId);

	/**
	 * Create criterion for topic owner ids.
	 * 
	 * @param queryOperator
	 *            Query operator for criterion.
	 * @param ownerIds
	 *            A list of owner ids.
	 * @param internalCondition
	 *            Condition to concatenate internal criteria in case 
	 *            list contains more than one value.
	 */
	void addOwnerIdsCriterion(QueryOperator queryOperator,
			List<String> ownerIds, Condition internalCondition);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 * for ancestor topic id.
	 * 
	 * @param ancestorTopicId
	 *            Ancestor topic id.
	 */
	void addAncestorTopicIdEqualsCriterion(String ancestorTopicId);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 * for ancestor topic id to match
	 * ANY of the provided values.
	 * 
	 * @param ancestorTopicIds
	 *            List of Ancestor topic ids.
	 */
	void addAnyAncestorTopicIdEqualsCriterion(List<String> ancestorTopicIds);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 * for topic name.
	 * 
	 * @param name
	 *            Topic name.
	 */
	void addNameEqualsCriterion(String name);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 * for topic name.
	 * 
	 * Disables case sensitivity.
	 * 
	 * @param name
	 *            Topic name.
	 */
	void addNameEqualsCaseInsensitiveCriterion(String name);

	/**
	 * Adds order property for specified locale.
	 * 
	 * @param locale
	 *            Locale as defined in {@link Localization}.
	 * @param order
	 *            Ascending or descending order.
	 */
	void addOrderByLocale(String locale, Order order);
	
	/**
	 * Sets criteria for ancestor entity.
	 * 
	 * <p>
	 * This method serves the need to specify criteria for an
	 * ancestor entity.
	 * </p>
	 * 
	 * <p>
	 * By default, all ancestors are queried. In order to query direct ancestor (parent) 
	 * call method {@link #searchInDirectAncestorOnly()}.
	 * </p>
	 * 
	 * @param ancestorCriteria
	 *            Criteria for ancestor entity.
	 */
	void setAncestorCriteria(TopicCriteria ancestorCriteria);

	/**
	 * Returns criteria for ancestor entity.
	 * 
	 * @return Criteria for ancestor entity.
	 */
	TopicCriteria getAncestorCriteria();
	
	/**
	 * Constraints query to direct ancestor if any ancestor criteria has been
	 * applied.
	 */
	void searchInDirectAncestorOnly();
	/**
	 * Creates criterion for topic's taxonomy.
	 * 
	 * If taxonomy is a {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy}, then 
	 * it has the same semantics with {@link #addFolksonomyCriterion(String, String)}, otherwise
	 * its semantics are the same with {@link #addTaxonomyNameEqualsCriterion(String)}.
	 *  
	 * @param taxonomy
	 *            Topic's taxonomy.
	 */
	void addTaxonomyCriterion(Taxonomy taxonomy);

	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * taxonomy name.
	 * 
	 * @param taxonomyName
	 *            Taxonomy name.
	 */
	void addTaxonomyNameEqualsCriterion(String taxonomyName);


	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator for a
	 * {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy} id of a specified repository user.
	 * 
	 * @param repositoryUserId
	 *            Repository user id.
	 * @param folksonomyId
	 *            {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy} Id.
	 */
	void addFolksonomyCriterion(String repositoryUserId, String folksonomyId);

	

}
