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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.xpath.OrderByClauseHelper;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
abstract class CmsCriteriaImpl  extends CmsQueryContextImpl implements CmsCriteria, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1063065223169846029L;

	private String pathCriterion;

	ItemQName nodeType;

	private List<Criterion> criteria;

	private boolean isNodeTypeAChildNode;

	String xpathQuery;

	//Default value is NO cache at all
	private CacheRegion cacheRegion = CacheRegion.NONE;

	private SearchMode searchMode;

	CmsCriteriaImpl() {
		nodeType = JcrBuiltInItem.NtBase;
	}

	/**
	 * Path criterion which is placed at the beginning of the query, for example
	 * find all topics under Default taxonomy Root
	 * /betaconcept:system/betaconcept:taxonomyRoot//element(*, betaconcept:topic)[...] 
	 * 
	 *
	 */
	protected void setPathCriterion(String pathCriterion){
		this.pathCriterion = pathCriterion;
	}

	public List<Criterion> getCriteria() {
		return criteria;
	}

	public CmsCriteria addCriterion(Criterion criterion)
	{
		if (criteria == null)
			criteria = new ArrayList<Criterion>();

		if (criterion != null){
			criteria.add(criterion);

			resetXpathQuery();
		}

		return this;
	}

	private void resetXpathQuery() {
		if (StringUtils.isNotBlank(xpathQuery))
			xpathQuery = null;
	}

	public String getXPathQuery()	 {
		//XPathQuery has been calculated once.
		if (StringUtils.isNotBlank(xpathQuery))
			return xpathQuery;


		String xpathselect = XPathUtils.createXPathSelect(null,nodeType, isNodeTypeAChildNode);

		//Add to xpath select ancestor xpath OR path criterion
		String ancestorXPathQuery = getAncestorQuery();
		if (StringUtils.isNotBlank(ancestorXPathQuery)){
			xpathselect = ancestorXPathQuery+xpathselect;
		}
		else if (StringUtils.isNotBlank(pathCriterion)){
			xpathselect = pathCriterion + xpathselect;
		}

		// Connect each criterion with AND condition
		//Add default criterion for system built in entity
		Criterion currentCriterion = generateSystemBuiltinEntityCriterion();
		if (CollectionUtils.isNotEmpty(getCriteria())) {
			for (Criterion criterion : getCriteria()) {
				if (currentCriterion == null){
					currentCriterion = criterion;
				}
				else {
					currentCriterion = CriterionFactory.and(currentCriterion, criterion);
				}
			}
		}


		String orderBy = "";
		String columnProjectionInXPath = "";
		if (MapUtils.isNotEmpty(getOrderProperties()))
		{
			OrderByClauseHelper orderByClauseHelper = new OrderByClauseHelper(getOrderProperties());
			orderByClauseHelper.generateOrderBy();
			orderBy = orderByClauseHelper.getOrderByClause();
			columnProjectionInXPath = orderByClauseHelper.getColumnProjectionInXPath();

		}

		String criteriaPath = (currentCriterion == null)? "": currentCriterion.getXPath();

		xpathQuery = xpathselect
		+ ((StringUtils.isBlank(criteriaPath) ? "" : CmsConstants.LEFT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE	+ criteriaPath	+ CmsConstants.RIGHT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE)
				 +columnProjectionInXPath+ CmsConstants.EMPTY_SPACE + orderBy);

		return xpathQuery;
	}

	private Criterion generateSystemBuiltinEntityCriterion() {

		if (searchMode == null){
			return null;
		}
		
		switch (searchMode) {
		case SEARCH_ALL_ENTITIES:
			//Since we want all entities do not create any criterion
			return null;
		case SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES:
			//search for all entities which DO not have property (backwards compatibility)
			//or those which have the property but its value is false
			return CriterionFactory.or(
					CriterionFactory.isNull("bccms:systemBuiltinEntity"),
					CriterionFactory.equals("bccms:systemBuiltinEntity", false));
		case SEARCH_ONLY_SYSTEM_BUILTIN_ENTITIES:
			return CriterionFactory.equals("bccms:systemBuiltinEntity", true);
		default:
			return null;
		}
	}

	abstract String getAncestorQuery();

	public void addIdEqualsCriterion(String id) {
		if (StringUtils.isNotBlank(id)){
			addCriterion(CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), id));
		}

	}

	public void setNodeTypeAsAChildNodeOnly() {
		isNodeTypeAChildNode = true;

	}

	@Override
	public void reset() {
		pathCriterion = null;

		criteria = null;

		isNodeTypeAChildNode =false;

		xpathQuery =null;

	}


	public void addIdNotEqualsCriterion(String id) {
		if (StringUtils.isNotBlank(id)){
			addCriterion(CriterionFactory.notEquals(CmsBuiltInItem.CmsIdentifier.getJcrName(), id));
		}


	}

	public void setCacheable(CacheRegion cacheRegion) {
		if (cacheRegion != null && CacheRegion.NONE == cacheRegion)
		{
			doNotCacheResults();
		}
		else
		{
			if (cacheRegion == null){
				this.cacheRegion = CacheRegion.TEN_MINUTES;
			}
			else{
				this.cacheRegion = cacheRegion;
			}
		}
	}

	public boolean isCacheable() {
		return CacheRegion.NONE != cacheRegion;
	}

	public void setXPathQuery(String xpathQuery) {
		this.xpathQuery = xpathQuery;
	}

	public CacheRegion getCacheRegion() {
		return cacheRegion;
	}

	public void doNotCacheResults() {
		this.cacheRegion = CacheRegion.NONE;
	}

	public void setSearchMode(SearchMode searchMode){
		this.searchMode = searchMode;

		resetXpathQuery();
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}



}
