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

package org.betaconceptframework.astroboa.model.impl.query.criteria;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.query.criteria.BinaryChannelCriteria;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;


/**
 * BinaryChannel Criteria
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BinaryChannelCriteriaImpl extends CmsCriteriaImpl implements BinaryChannelCriteria, Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3914501163927581490L;

	public BinaryChannelCriteriaImpl()
	{
		nodeType = CmsBuiltInItem.BinaryChannel;
	}

	@Override
	protected String getAncestorQuery() {
		return null;
	}
	
	

}
