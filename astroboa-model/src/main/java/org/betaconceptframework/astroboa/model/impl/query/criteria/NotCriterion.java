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

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Criterion that negates the provided criterion.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class NotCriterion implements Criterion, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1369232282817125565L;
	
	private Criterion criterion;
	
	public NotCriterion(Criterion criterion){
		this.criterion = criterion;
	}
	
	
	@Override
	public String getXPath() {
		if (criterion == null){
			return "";
		}
		
		String xpath = criterion.getXPath();
		
		if (StringUtils.isBlank(xpath)){
			return "";
		}
		
		return " "+CmsConstants.NOT + CmsConstants.LEFT_PARENTHESIS + xpath + CmsConstants.RIGHT_PARENTHESIS;
	}

}
