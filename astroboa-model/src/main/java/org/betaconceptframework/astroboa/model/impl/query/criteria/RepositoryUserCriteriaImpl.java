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

import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserCriteriaImpl extends CmsCriteriaImpl implements RepositoryUserCriteria, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5549906058331259850L;

	public RepositoryUserCriteriaImpl()
	{
		nodeType = CmsBuiltInItem.RepositoryUser;
	}
	
	public void addExternalIdEqualsCriterion(String externalId)
	{
		
		if (externalId != null)
			addCriterion(CriterionFactory.equals(CmsBuiltInItem.ExternalId.getJcrName(), externalId));
	}
	
	public void addLabelEqualsCriterion(String label)
	{
		if(label != null)
			addCriterion(CriterionFactory.like(CmsBuiltInItem.Label.getJcrName(), label));
	}

	@Override
	protected String getAncestorQuery() {
		return null;
	}

	@Override
	public void addOrderByExternalId(Order order) {
		addOrderProperty(CmsBuiltInItem.ExternalId.getJcrName(), 
				(order == null? Order.ascending: order));
		
	}

	@Override
	public void addOrderByLabel(Order order) {
		addOrderProperty(CmsBuiltInItem.Label.getJcrName(), 
				(order == null? Order.ascending: order));

		
	}

	@Override
	public void addExternalIdNotEqualsCriterion(String externalId) {
		if (externalId != null)
			addCriterion(CriterionFactory.notEquals(CmsBuiltInItem.ExternalId.getJcrName(), externalId));
		
	}
}
