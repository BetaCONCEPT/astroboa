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

package org.betaconceptframework.astroboa.engine.jcr.query;

import javax.jcr.NodeIterator;
import javax.jcr.RangeIterator;
import javax.jcr.query.RowIterator;

/**
 * Wraps a JCR Range iterator by including total size of
 * results.
 * 
 * It is often the case that we do not want all nodes matching
 * query criteria but only a subset of these and at the same time
 * to be aware of the total query result size.
 * 
 * This class contains the total number of nodes matching query criteria
 * and range iterator which may contain only a subset of these nodes
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsQueryResult {
	
	private final int totalRowCount;
	
	private final RangeIterator rangeIterator;
	
	public CmsQueryResult(int totalRowount, RangeIterator rangeIterator){
		this.totalRowCount = totalRowount;
		this.rangeIterator = rangeIterator;
	}


	public int getTotalRowCount() {
		return totalRowCount;
	}

	public NodeIterator getNodeIterator() {
		
		if (rangeIterator instanceof NodeIterator){
			return (NodeIterator) rangeIterator;
		}
		else{
			return null;
		}
	}
	
	public RowIterator getRowIterator() {
		if (rangeIterator instanceof RowIterator){
			return (RowIterator) rangeIterator;
		}
		else{
			return null;
		}

	}
	

}
