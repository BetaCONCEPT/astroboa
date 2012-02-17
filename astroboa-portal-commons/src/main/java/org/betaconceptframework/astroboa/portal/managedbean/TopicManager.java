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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.portal.utility.CmsUtils;
import org.betaconceptframework.astroboa.portal.utility.LazyLoadingTopicTreeNode;
import org.betaconceptframework.astroboa.portal.utility.PortalCacheConstants;
import org.betaconceptframework.astroboa.portal.utility.TopicComparator;
import org.betaconceptframework.astroboa.portal.utility.TopicComparator.OrderByProperty;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("topicManager")
@Scope(ScopeType.SESSION)
public class TopicManager extends AbstractUIBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6311222629082658672L;

	@In(create=true)
	private AstroboaClient astroboaClient;
	
	@In(create=true)
	private CmsUtils cmsUtils;
	
	private Map<String, LazyLoadingTopicTreeNode> lazyLoadingTopicTreeNodesPerTaxonomyAndParentName = new HashMap<String, LazyLoadingTopicTreeNode>();
	
	
	public LazyLoadingTopicTreeNode getTopicTreeForParentTopic(String taxonomyName, String parentTopicName, boolean orderByPosition){
		
		String topicTreeNodeId = taxonomyName+parentTopicName;
		
		if (StringUtils.isBlank(topicTreeNodeId)){
			return null;
		}


		if (lazyLoadingTopicTreeNodesPerTaxonomyAndParentName != null && lazyLoadingTopicTreeNodesPerTaxonomyAndParentName.containsKey(topicTreeNodeId)){
			return lazyLoadingTopicTreeNodesPerTaxonomyAndParentName.get(topicTreeNodeId);
		}
		
		List<Topic> childTopics = getChildTopicsByParentTopicName(taxonomyName, parentTopicName, orderByPosition, PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
		
		LazyLoadingTopicTreeNode rootNode = new LazyLoadingTopicTreeNode();
		
		if (CollectionUtils.isNotEmpty(childTopics)){
			
			for (Topic childTopic : childTopics){
				LazyLoadingTopicTreeNode childTopicTreeNode = new LazyLoadingTopicTreeNode(childTopic.getId(),childTopic, rootNode, 
						orderByPosition ? OrderByProperty.POSITION : OrderByProperty.LABEL);
				
				rootNode.addChild(childTopicTreeNode.getIdentifier(), childTopicTreeNode);
			}
		}
		
		if (lazyLoadingTopicTreeNodesPerTaxonomyAndParentName == null){
			lazyLoadingTopicTreeNodesPerTaxonomyAndParentName = new HashMap<String, LazyLoadingTopicTreeNode>();
		}
		
		lazyLoadingTopicTreeNodesPerTaxonomyAndParentName.put(topicTreeNodeId, rootNode);
		
		return rootNode;
	}
	
	
	public List<Topic> getChildTopicsByParentTopicName(String taxonomyName, String parentTopicName, boolean orderByPosition) {
		return getChildTopicsByParentTopicName(taxonomyName, parentTopicName, orderByPosition, PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
	}
	
	public List<Topic> getRootTopicsOfTaxonomyOrderedByPosition(String taxonomyName) {
		return getChildTopicsByParentTopicName(taxonomyName, null, true);
		
	}
	
	public List<Topic> getRootTopicsOfTaxonomy(String taxonomyName) {
		return getChildTopicsByParentTopicName(taxonomyName, null);
		
	}
	/**
	 * Find child topics of a topic defined under a taxonomy
	 * If a parent topic is not provided then the root topics of the taxonomy are returned 
	 * The topics are returned alphabetically ordered 
	 * @param taxonomyName
	 * @param parentTopicName
	 * @return
	 */
	public List<Topic> getChildTopicsByParentTopicName(String taxonomyName, String parentTopicName) {
		return getChildTopicsByParentTopicName(taxonomyName, parentTopicName, false, PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
	}
	
	/**
	 * Find child topics of a topic defined under a taxonomy
	 * If a parent topic is not provided then the root topics of the taxonomy are returned.
	 * If the taxonomy name is empty or null an empty list is returned. 
	 * if "orderByPosition" is true the topics are returned ordered according to their order ranking,
	 * otherwise the topics are returned alphabetically ordered.
	 * 
	 * If a non null cache region is provided then the query is cached in the specified region
	 * Otherwise it is cached in the default regions for Topic queries as specified in portal settings
	 * 
	 * @param taxonomyName
	 * @param parentTopicName
	 * @param orderByPosition
	 * @param cacheRegion
	 * @return
	 */
	public List<Topic> getChildTopicsByParentTopicName(String taxonomyName, String parentTopicName, boolean orderByPosition, 
			CacheRegion cacheRegion) {
			
			if (cacheRegion == null) {
				cacheRegion = PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION;
			}
		
			if (StringUtils.isBlank(parentTopicName)) {
				if (StringUtils.isBlank(taxonomyName))
				{
					return new ArrayList<Topic>();
				}
				Taxonomy taxonomy = 
					astroboaClient.getTaxonomyService().getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
				
				if (taxonomy == null) {
					return new ArrayList<Topic>();
				}
				
				if (CollectionUtils.isNotEmpty(taxonomy.getRootTopics())){
					List<Topic> rootTopics = taxonomy.getRootTopics();
					if (orderByPosition){
						Collections.sort(rootTopics, new TopicComparator(JSFUtilities.getLocaleAsString(), OrderByProperty.POSITION));
					}
					return rootTopics;
				}
				else{
					return new ArrayList<Topic>();
				}
			}
			
			List<Topic> childTopics =
				cmsUtils.findChildTopicsByParentTopicName(parentTopicName, JSFUtilities.getLocaleAsString(), orderByPosition, cacheRegion);
			
			if (childTopics != null && CollectionUtils.isNotEmpty(childTopics)) {
				return childTopics;
			}
			else {
				return new ArrayList<Topic>();
			}
	}
	
	public List<Topic> getChildTopicsByParentTopicName(String taxonomyName, String parentTopicName, Integer numberOfResults) {
		List<Topic> childTopics = getChildTopicsByParentTopicName(taxonomyName, parentTopicName);
		
		if (numberOfResults != null && numberOfResults > 0 && childTopics.size() >= numberOfResults) {
			return childTopics.subList(0, numberOfResults + 1);
		}
		else {
			return childTopics;
		}
	}
	
	
	
	/**
	 * Find child topics of a topic defined under a taxonomy
	 * The topics are returned ordered according to their order ranking 
	 * @param parentTopicId
	 * @return
	 */
	public List<Topic> getChildTopicsByParentTopicId(String parentTopicId, boolean orderByPosition, boolean cacheable, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(parentTopicId)) {
			return null;
		}
		
		List<Topic> childTopics =
			cmsUtils.findChildTopicsByParentTopicId(parentTopicId, JSFUtilities.getLocaleAsString(), orderByPosition, cacheRegion);
		
		if (childTopics != null) {
			return childTopics;
		}
		else {
			return new ArrayList<Topic>();
		}
	}
	
	
	/**
	 * find the topics in subject Taxonomy that their label CONTAINS the provided text
	 * @param containingText
	 * @return
	 */
	public List<Topic> findTopicsByTextSearch(String textContainedInLabel) {
		return findTopicsByTextSearch(Taxonomy.SUBJECT_TAXONOMY_NAME, textContainedInLabel);
	}
	
	/**
	 * find the topics in Taxonomy with the specified system name "taxonomySystemName" that their label CONTAINS the provided text
	 * if "taxonomySystemName" is null or empty then search in all available taxonomies
	 * 
	 * @param taxonomySystemName
	 * @param containingText
	 * @return
	 */
	public List<Topic> findTopicsByTextSearch(String taxonomySystemName, String textContainedInLabel) {
		try {

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(textContainedInLabel))
				return null;
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			if (StringUtils.isNotBlank(taxonomySystemName)) {
				topicCriteria.addTaxonomyNameEqualsCriterion(Taxonomy.SUBJECT_TAXONOMY_NAME);
			}

			topicCriteria.getRenderProperties().renderValuesForLocales(Arrays.asList(JSFUtilities.getLocaleAsString()));
			topicCriteria.addOrderByLocale(JSFUtilities.getLocaleAsString(), Order.ascending);

			//Localized Label criterion
			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			localizationCriterion.setLocale(JSFUtilities.getLocaleAsString());
			localizationCriterion.addLocalizedLabel(textContainedInLabel+CmsConstants.ANY_NAME);
			//localizationCriterion.enableContainsQueryOperator();
			localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);

			//localizationCriterion.addLocalizedLabel(CmsConstants.LIKE + textContainedInLabel + CmsConstants.LIKE);  
			topicCriteria.addCriterion(localizationCriterion);
			
			topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);

			CmsOutcome<Topic> cmsOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			return cmsOutcome.getResults();

		} 
		catch (Exception e) {
			logger.error("Error while loading Topics ",e);
			return null;
		}
	}
	
	public List<Topic> findSuggestedTopics_UIAction(Object event) {
		try {

			String selectedTopicLabel = event.toString();

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(selectedTopicLabel))
				return null;
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			
			topicCriteria.getRenderProperties().renderValuesForLocale(JSFUtilities.getLocaleAsString());
			topicCriteria.setOffsetAndLimit(0, 30);
			topicCriteria.addOrderByLocale(JSFUtilities.getLocaleAsString(), Order.ascending);
			
			
			//Localized Label criterion
			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			localizationCriterion.setLocale(JSFUtilities.getLocaleAsString());
			localizationCriterion.addLocalizedLabel(selectedTopicLabel + CmsConstants.ANY_NAME);
			//localizationCriterion.enableContainsQueryOperator();
			localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);

			topicCriteria.addCriterion(localizationCriterion);

			topicCriteria.addTaxonomyNameEqualsCriterion(Taxonomy.SUBJECT_TAXONOMY_NAME);
			
			topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);
			
			CmsOutcome<Topic> cmsOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			return cmsOutcome.getResults();
				
		} catch (Exception e) {
			logger.error("Error while loading Topics ",e);
			return null;
		}

	}
	
	
}
