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
import org.betaconceptframework.astroboa.api.model.query.Order;
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

	private SearchMode searchMode = SearchMode.SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES;

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
			
			//Due to the automatic addition of order by clause we have to remove any 
			//order by clause created
			if (ancestorXPathQuery.contains(CmsConstants.ORDER_BY)){
				ancestorXPathQuery = StringUtils.substringBeforeLast(ancestorXPathQuery, CmsConstants.ORDER_BY);
			}
			
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
				if (currentCriterion == null)
					currentCriterion = criterion;
				else
					currentCriterion = CriterionFactory.and(currentCriterion,
							criterion);
			}
		}


		String orderBy = "";
		if (MapUtils.isNotEmpty(getOrderProperties())){
			OrderByClauseHelper orderByClauseHelper = new OrderByClauseHelper(getOrderProperties());
			orderBy = orderByClauseHelper.generateOrderBy();
		}
		else{
			//Special case. Due to the fact that the JCR spec does not provide a method 
			//for retrieving the total number of hits regardless of the limit, 
			//we have to use the method provided by Jackrabbit (the JCR reference implementation).
			//Unfortunately, since version 2.x , this method returns -1 in cases where the size
			//is unknown even when the queries do not match any object at all. In order to 
			//force the implementation to return the number of total hits we have to provide
			//an order by property. Therefore, in criteria where no order by property is provided
			//we explicitly instruct Jackrabbit to order the results by the jcr:score property.
			OrderByClauseHelper orderByClauseHelper = new OrderByClauseHelper(JcrBuiltInItem.JcrScore.getJcrName(), Order.descending);
			orderBy = orderByClauseHelper.generateOrderBy();
		}

		String criteriaPath = (currentCriterion == null)? "": currentCriterion.getXPath();

		xpathQuery = xpathselect
		+ ((StringUtils.isBlank(criteriaPath) ? "" : CmsConstants.LEFT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE	+ criteriaPath	+ CmsConstants.RIGHT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE)
				 + CmsConstants.EMPTY_SPACE + orderBy);

		return xpathQuery;
	}

	private Criterion generateSystemBuiltinEntityCriterion() {

		switch (searchMode) {
		case SEARCH_ALL_ENTITIES:
			//Since we want all entities do not create any criterion
			return null;
		case SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES:
			//search for all entities which DO not have property (backwards compatibility)
			//or those which have the property but its value is false
			return CriterionFactory.or(
					CriterionFactory.isNull(CmsBuiltInItem.SystemBuiltinEntity.getJcrName()),
					CriterionFactory.equals(CmsBuiltInItem.SystemBuiltinEntity.getJcrName(), false));
		case SEARCH_ONLY_SYSTEM_BUILTIN_ENTITIES:
			return CriterionFactory.equals(CmsBuiltInItem.SystemBuiltinEntity.getJcrName(), true);
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
		if (searchMode == null){
			this.searchMode = SearchMode.SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES;
		}
		else{
			this.searchMode = searchMode;
		}

		resetXpathQuery();
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}



}
