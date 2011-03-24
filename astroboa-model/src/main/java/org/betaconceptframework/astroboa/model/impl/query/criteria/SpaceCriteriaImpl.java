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
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceCriteriaImpl extends CmsCriteriaImpl  implements SpaceCriteria, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1495104177364887190L;
	
	private SpaceCriteria ancestorCriteria;
	
	public SpaceCriteriaImpl()
	{
		nodeType = CmsBuiltInItem.Space;
	}

	/**
	 * Criteria for parent spaces 
	 * @param ancestorCriteria
	 */
	public void setAncestorCriteria(SpaceCriteria ancestorCriteria)
	{
		this.ancestorCriteria = ancestorCriteria;
	}
	
	public SpaceCriteria getAncestorCriteria()
	{
		return ancestorCriteria;
	}

	public void addOwnerIdEqualsCriterion(String topicOwnerId)
	{
		if (topicOwnerId != null)
		{
			List<String> topicOwnerIds = new ArrayList<String>();
			topicOwnerIds.add(topicOwnerId);
			
			addOwnerIdsCriterion(QueryOperator.EQUALS, topicOwnerIds, Condition.AND);
		}
	}
	
	public void addOwnerIdsCriterion(QueryOperator queryOperator, List<String> topicOwnerIds, Condition internalCondition){
		if (CollectionUtils.isNotEmpty(topicOwnerIds))
		{
			SimpleCriterion ownerCriterion = CriterionFactory.newSimpleCriterion();
 
			ownerCriterion.setProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName());
			
			//set default condition
			if (internalCondition == null)
				ownerCriterion.setInternalCondition(Condition.OR);
			else 
				ownerCriterion.setInternalCondition(internalCondition);
			
			ownerCriterion.setOperator(queryOperator);
			
			for (String topicOwnerId: topicOwnerIds){
				if (StringUtils.isNotBlank(topicOwnerId))
					ownerCriterion.addValue(topicOwnerId);
			}
			
			addCriterion(ownerCriterion);
		}
	}
	
	
	
	public void addAncestorSpaceIdEqualsCriterion(String parentTopicId) {
		if (getAncestorCriteria() == null){
			createAncestorCriteria();
		}
		
		if (getAncestorCriteria() != null){
			getAncestorCriteria().addCriterion(CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), parentTopicId));
		}

	}

	private void createAncestorCriteria() {
		ancestorCriteria = new SpaceCriteriaImpl();
		ancestorCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
	}

	public void addNameEqualsCriterion(String name) {
		addCriterion(CriterionFactory.equals(CmsBuiltInItem.Name.getJcrName(), name));
		
	}

	public void addNameEqualsCaseInsensitiveCriterion(String name) {
		addCriterion(CriterionFactory.equalsCaseInsensitive(CmsBuiltInItem.Name.getJcrName(), name));
		
	}

	public void addOrderByLocale(String locale, Order order) {
		if (StringUtils.isNotBlank(locale))
			addOrderProperty(CmsBuiltInItem.Localization.getJcrName()+CmsConstants.FORWARD_SLASH+ItemUtils.createNewBetaConceptItem(locale).getJcrName(), order);
		
	}

	@Override
	public void reset() {
		super.reset();
		
		ancestorCriteria = null;

	}

	@Override
	protected String getAncestorQuery() {
		//Create Ancestor Criteria but no order by is required
		if (ancestorCriteria !=null){
			
			if (ancestorCriteria.getOrderProperties() != null)
				ancestorCriteria.resetOrderProperties();
			
			return ancestorCriteria.getXPathQuery();
		}
		
		return null;
	}

	@Override
	public void searchInDirectAncestorOnly() {
		setNodeTypeAsAChildNodeOnly();
	}


}
