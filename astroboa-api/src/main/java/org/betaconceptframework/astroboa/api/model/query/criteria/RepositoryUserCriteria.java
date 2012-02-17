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

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Criteria API for building queries when 
 * searching for {@link RepositoryUser repository users}.
 * 
 * Provides helper methods for creating criteria mainly for
 * repository user properties.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryUserCriteria extends CmsCriteria {

	/**
	 * Adds a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * {@link RepositoryUser#getExternalId() external id}.
	 * 
	 * @param externalId
	 *            RepositoryUser external id.
	 */
	void addExternalIdEqualsCriterion(String externalId);

	/**
	 * Adds a criterion with {@link QueryOperator#NOT_EQUALS not equals} operator for
	 * {@link RepositoryUser#getExternalId() external id}.
	 * 
	 * @param externalId
	 *            RepositoryUser external id.
	 */
	void addExternalIdNotEqualsCriterion(String externalId);
	
	/**
	 * Adds a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * {@link RepositoryUser#getLabel() label}.
	 * 
	 * @param label
	 *            RepositoryUser label.
	 */
	void addLabelEqualsCriterion(String label);

	/**
	 * Order results using external id.
	 * 
	 * @param order
	 *    	{@link Order#ascending ascending} (default) or  {@link Order#descending descending} order
	 */
	void addOrderByExternalId(Order order);
	
	/**
	 * Order results using label.
	 * 
	 * @param order
	 *    	{@link Order#ascending ascending} (default) or  {@link Order#descending descending} order.
	 */
	void addOrderByLabel(Order order);
}
