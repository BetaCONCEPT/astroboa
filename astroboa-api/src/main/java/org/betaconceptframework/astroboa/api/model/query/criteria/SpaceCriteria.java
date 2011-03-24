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

import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Criteria API for building queries when 
 * searching for {@link Space spaces}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SpaceCriteria extends CmsCriteria  {

	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator 
	 * for space owner id.
	 * 
	 * @param ownerId
	 *            Space owner id.
	 */
	void addOwnerIdEqualsCriterion(String ownerId);

	/**
	 * Create criterion for space owner ids.
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
	 * for ancestor space id.
	 * 
	 * @param ancestorSpaceId
	 *            Ancestor space id.
	 */
	void addAncestorSpaceIdEqualsCriterion(String ancestorSpaceId);

	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 *  criterion for space name.
	 * 
	 * @param name
	 *            Space name.
	 */
	void addNameEqualsCriterion(String name);
	
	/**
	 * Create a criterion with {@link QueryOperator#EQUALS equals} operator
	 * for space name.
	 * 
	 * Disables case sensitivity.
	 * 
	 * @param name
	 *            Space name.
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
	void setAncestorCriteria(SpaceCriteria ancestorCriteria);

	/**
	 * Returns criteria for ancestor entity.
	 * 
	 * @return Criteria for ancestor entity.
	 */
	SpaceCriteria getAncestorCriteria();
	
	/**
	 * Constraints query to direct ancestor if any ancestor criteria has been
	 * applied.
	 */
	void searchInDirectAncestorOnly();
}
