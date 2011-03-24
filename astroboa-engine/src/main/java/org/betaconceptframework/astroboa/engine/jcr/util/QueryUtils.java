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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsResultSort;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIterator;
import org.betaconceptframework.astroboa.model.impl.query.xpath.OrderByClauseHelper;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class QueryUtils{

	public CmsScoreNodeIterator orderIterator(CmsScoreNodeIterator cmsScoreNodeIterator, Map<String, Order> orderProperties, int offset, int limit) {

		if (MapUtils.isEmpty(orderProperties)){
			return cmsScoreNodeIterator;
		}

		//Check to see if ordering took place in query 
		OrderByClauseHelper orderByClauseHelper = new OrderByClauseHelper(orderProperties);
		orderByClauseHelper.generateOrderBy();

		if (orderByClauseHelper.astroboaEngineWillOrderResults())
		{
			//Ordering in result did not took place in query
			CmsResultSort cmsResultSort = new CmsResultSort(cmsScoreNodeIterator, orderProperties);
			
			//return cmsResultSort.sortUsingShellSort();
			return cmsResultSort.sortUsingArrays(offset, limit);
		}
		else{
			//Query returned ordered results
			return cmsScoreNodeIterator;
		}

	}
}
