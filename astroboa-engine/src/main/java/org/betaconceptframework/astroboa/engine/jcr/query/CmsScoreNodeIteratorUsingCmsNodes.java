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


import java.util.NoSuchElementException;

import javax.jcr.Node;

import org.apache.commons.lang.ArrayUtils;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsQueryContext;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsScoreNodeIteratorUsingCmsNodes  implements CmsScoreNodeIterator{

	private CmsScoreNode[] rows = null;
	
	private long position = -1;
	private int limit = CmsQueryContext.UNLIMITED;
	private int offset = 0;

	private void reset() {
		rows = new CmsScoreNode[0];
		position = -1;
		limit = CmsQueryContext.UNLIMITED;
		offset = 0;
	}

	public CmsScoreNodeIteratorUsingCmsNodes(CmsScoreNode[] rows, int offset, int limit) {
		
		if (ArrayUtils.isEmpty(rows))
			reset();
		else
		{
			this.rows = (CmsScoreNode[]) ArrayUtils.addAll(rows,null);
			this.limit = limit;
			this.offset = offset;
			
			position = -1;
			skip(offset);
		}
	}


	public CmsScoreNode nextCmsScoreNode() {
		return (CmsScoreNode) next();
	}

	public long getPosition() {
		 
		return position;
	}

	public long getSize() {
		 
		return (long)rows.length;
	}

	public void skip(long arg0) {
		position = position+arg0;
		
	}

	public boolean hasNext() {
		 
		//If no limit is specified check position in respect to size
			return getSize() > 0 
				&& (position +1) < getSize() //Next Position must not be more than size
				&& (limit == CmsQueryContext.UNLIMITED ? true : (position-offset +1 ) < limit); //If limit is specified then limit is the upper bound
	}

	public Object next() {
		 
		int nextPosition = (int)++position;
		if (nextPosition >= getSize())
			throw new NoSuchElementException("Length "+getSize()+" nextPosition "+nextPosition);
		
		return rows[nextPosition];
	}

	public void remove() {
		 
		
	}

	@Override
	public Node nextNode() {
		
		CmsScoreNode nextScoreNode = nextCmsScoreNode();
		
		return nextScoreNode != null ? nextScoreNode.getJcrNode() : null;
		
	}


}
