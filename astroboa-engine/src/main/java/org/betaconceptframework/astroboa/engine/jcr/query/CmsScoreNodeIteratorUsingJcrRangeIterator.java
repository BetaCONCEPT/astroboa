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



import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RangeIterator;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.util.JackrabbitDependentUtils;

/**
 * This class is a wrapper class over RowIterator. 
 * 
 * It iterates through the row iterator and provides the jcr node which represents the 
 * row and any jcr:score corresponding to that node.
 * 
 * In JCR API v1.0, RowIterator does not provide any method to retrieve the jcr node
 * in contrary to NodeIterator which by its turn does not provide any method for retrieving
 * the jcr:score value of a node. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsScoreNodeIteratorUsingJcrRangeIterator  implements CmsScoreNodeIterator{

	private RangeIterator rangeIterator = null;

	public CmsScoreNodeIteratorUsingJcrRangeIterator(RangeIterator rangeIterator) {

		this.rangeIterator = rangeIterator;

		if (this.rangeIterator == null){
			throw new CmsException("No row iterator provided");
		}
	}

	public CmsScoreNode nextCmsScoreNode() {
		return (CmsScoreNode) next();
	}

	public long getPosition() {

		return rangeIterator.getPosition();
	}

	public long getSize(){
		return rangeIterator.getSize();
	}

	public void skip(long arg0) {
		rangeIterator.skip(arg0);

	}

	public boolean hasNext() {

		return rangeIterator.hasNext();
	}

	public Object next() {
		
		if (rangeIterator instanceof RowIterator){
			final Row nextRow = ((RowIterator)rangeIterator).nextRow();

			final Node nextNode = JackrabbitDependentUtils.getNodeFromRow(nextRow);

			double scoreFromNodeIterator = JackrabbitDependentUtils.getScoreFromRow(nextRow);

			return new CmsScoreNode(nextNode, scoreFromNodeIterator);
		}
		else if (rangeIterator instanceof NodeIterator){
			//Node iterator does not provide any method for retrieving node score
			final Node nextNode = ((NodeIterator)rangeIterator).nextNode();

			return new CmsScoreNode(nextNode, 0);			
		}
		
		return null;

	}

	public void remove() {
		rangeIterator.remove();

	}

	@Override
	public Node nextNode() {
		
		CmsScoreNode nextScoreNode = nextCmsScoreNode();
		
		return nextScoreNode != null ? nextScoreNode.getJcrNode() : null;
		
	}
}
