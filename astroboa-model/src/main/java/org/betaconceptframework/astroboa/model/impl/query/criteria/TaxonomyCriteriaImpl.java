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
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyCriteriaImpl extends CmsCriteriaImpl implements TaxonomyCriteria , Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6831186030681491063L;

	public TaxonomyCriteriaImpl(){
		nodeType = CmsBuiltInItem.Taxonomy;
	}

	public void addNameEqualsCriterion(String taxonomyName) {
		if (StringUtils.isNotBlank(taxonomyName)){
			setPathCriterion(XPathUtils.getRelativeTaxonomyPath(taxonomyName, true));
		}
		
	}
	
	@Override
	protected String getAncestorQuery() {
		return null;
	}
}
