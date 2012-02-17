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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicCriteriaImpl  extends CmsCriteriaImpl implements TopicCriteria, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6769105250768596934L;

	private TopicCriteria ancestorCriteria;
	
	public TopicCriteriaImpl(){
		nodeType = CmsBuiltInItem.Topic;
	}

	/**
	 * Criteria for parent topics 
	 * @param ancestorCriteria
	 */
	public void setAncestorCriteria(TopicCriteria ancestorCriteria)
	{
		this.ancestorCriteria = ancestorCriteria;
	}
	
	public TopicCriteria getAncestorCriteria()
	{
		return ancestorCriteria;
	}

	public void addAllowsReferrerContentObjectsCriterion(boolean allowsReferrerContentObjects)
	{
		addCriterion(CriterionFactory.equals(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName(), allowsReferrerContentObjects));
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
	
	
	
	public void addAncestorTopicIdEqualsCriterion(String parentTopicId) {
		if (getAncestorCriteria() == null){
			
			createAncestorCriteria();
		}
		
		if (getAncestorCriteria() != null){
			getAncestorCriteria().addCriterion(CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), parentTopicId));
		}

	}
	
	public void addAnyAncestorTopicIdEqualsCriterion(
			List<String> ancestorTopicIds) {
		if (CollectionUtils.isEmpty(ancestorTopicIds)){
			return ;
		}
		
		if (getAncestorCriteria() == null){
			createAncestorCriteria();
		}

		if (getAncestorCriteria() != null){
			getAncestorCriteria().addCriterion(CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), Condition.OR, ancestorTopicIds));
		}
		
	}

	private void createAncestorCriteria() {
		ancestorCriteria = new TopicCriteriaImpl();
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
	
	
	
	/**
	 * @param taxonomyName the taxonomy to set
	 */
	public void addTaxonomyCriterion(Taxonomy taxonomy) {
		if (taxonomy != null)
		{
			if (CmsBuiltInItem.Folksonomy.getJcrName().equals(taxonomy.getName()))
				addFolksonomyCriterion(null, taxonomy.getId());
			else{
				if (taxonomy.getName() != null){
					//	For all other taxonomies only name is adequate
					addTaxonomyNameEqualsCriterion(taxonomy.getName());
				}
				else if (taxonomy.getId() != null){
					addTaxonomyIdEqualsCriterion(taxonomy.getId());
				}
			}
			
		}
	}


	public void addFolksonomyCriterion(String repositoryUserId,	String folksonomyId) {
		
		Criterion repositoryIdCriterion = null;
		if (StringUtils.isNotBlank(repositoryUserId))
			repositoryIdCriterion = CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), repositoryUserId);
		
		Criterion folksonomyCriterion = null;
		if (StringUtils.isNotBlank(folksonomyId)){
			folksonomyCriterion = CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), folksonomyId);
		}
		
		setPathCriterion(XPathUtils.getRelativeFolksonomyPath(repositoryIdCriterion, folksonomyCriterion));
	}


	public void addTaxonomyNameEqualsCriterion(String taxonomyName) {
		if (StringUtils.isNotBlank(taxonomyName))
			setPathCriterion(XPathUtils.getRelativeTaxonomyPath(taxonomyName, true));
		
	}
	
	public void addTaxonomyIdEqualsCriterion(String taxonomyId) {
		if (StringUtils.isNotBlank(taxonomyId)){
			TaxonomyCriteria taxonomyCriteria = CmsCriteriaFactory.newTaxonomyCriteria();
			taxonomyCriteria.addIdEqualsCriterion(taxonomyId);
			taxonomyCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			
			setPathCriterion(taxonomyCriteria.getXPathQuery());
		}
		
	}

	@Override
	public void addOrderProperty(String propertyPath, Order order) {
		if (StringUtils.isNotBlank(propertyPath)){
			//Reserved words
			//Label corresponds to CmsBuiltInItem.Localization.getJcrName() property
			if (StringUtils.equals("label", propertyPath)){
				super.addOrderProperty(CmsBuiltInItem.Localization.getJcrName(), order);
			}
			else if (propertyPath.startsWith("label.")){
				//user may have provided specific lang in order, for example label.en 
				//Remove label prefix and use only lang code
				addOrderByLocale(StringUtils.removeStart(propertyPath, "label."), order);
			}
			else if (StringUtils.equals("name", propertyPath)){
				//user requested ordering by topic name
				super.addOrderProperty(CmsBuiltInItem.Name.getJcrName(), order);
			}
			else if (StringUtils.equals("id", propertyPath)){
				//user requested ordering by topic name
				super.addOrderProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), order);
			}
			else {
				super.addOrderProperty(propertyPath, order);
			}
		}

	}

	
}
