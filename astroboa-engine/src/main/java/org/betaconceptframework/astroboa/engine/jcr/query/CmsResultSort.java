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

package org.betaconceptframework.astroboa.engine.jcr.query;

import java.util.Arrays;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.query.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsResultSort {

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	private CmsScoreNode[] cmsScoreNodes;

	public CmsResultSort(CmsScoreNodeIterator cmsScoreNodeIterator, Map<String, Order> orderProperties) {
		
		cmsScoreNodes = fillArray(cmsScoreNodeIterator, orderProperties);
		
	}

	public CmsScoreNodeIterator sortUsingArrays(int offset, int limit) {
		
		Arrays.sort(cmsScoreNodes, new OrderByComparator());

		return new CmsScoreNodeIteratorUsingCmsNodes(cmsScoreNodes, offset,limit);
	}

	private CmsScoreNode[] fillArray(CmsScoreNodeIterator cmsScoreNodeIterator, Map<String, Order> orderProperties) {
		CmsScoreNode[] cmsRows = new CmsScoreNode[(int)cmsScoreNodeIterator.getSize()];

		int index = 0;
		while (cmsScoreNodeIterator.hasNext())
		{
			CmsScoreNode cmsRow = cmsScoreNodeIterator.nextCmsScoreNode();
			cmsRow.setOrderProperties(orderProperties);
			cmsRows[index++] = cmsRow;
			
		}

		return cmsRows;
	}


}
