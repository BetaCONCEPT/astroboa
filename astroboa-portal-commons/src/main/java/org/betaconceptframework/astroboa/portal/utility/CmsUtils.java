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
package org.betaconceptframework.astroboa.portal.utility;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.portal.utility.TopicComparator.OrderByProperty;
import org.betaconceptframework.ui.jsf.comparator.SelectItemComparator;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commonly used search queries and methods possibly useful to all developers utilizing the repository to get / write content for / from the portal  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Scope(ScopeType.APPLICATION)
@Name("cmsUtils")
public class CmsUtils {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	@In(create=true)
	private AstroboaClient astroboaClient;
	
	
	
	public Topic findTopicByTopicNameAndTaxonomyName(String topicName, String taxonomyName, String locale, CacheRegion cacheRegion){
		if (StringUtils.isBlank(topicName)) {
			logger.warn("The provided topic name is blank. A NULL topic will be returned");
			return null;
		}
		
		if (StringUtils.isBlank(taxonomyName)) {
			logger.warn("The provided taxonomy name is blank. A NULL topic will be returned");
			return null;
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. The default locale will be used");
			locale = PortalStringConstants.DEFAULT_LOCALE;
		}
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addNameEqualsCriterion(topicName);
			topicCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			
			if (cacheRegion != null){
				topicCriteria.setCacheable(cacheRegion);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);

			CmsOutcome<Topic> topicsFound = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
				Topic firstTopic = topicsFound.getResults().get(0); 
				// if more than one topics correspond to the same name then we choose the first one but we generate a warning
				if (topicsFound.getResults().size() > 1)
					logger.warn("More than one topics correspond to name: " + topicName + " The first from  list will be returned. Topic names should be unique. Please fix it !!");
				return firstTopic;
			}
			else {
				logger.info("The provided topic name "+topicName+" does not exist.");
				return null;
			}
				
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving a topic through the topicName. The error was: ",e);
			
			return null;
		}
		
	}
	
	/**
	 * It finds a topic by means of the topic system name
	 * The provided locale is required in order to render the appropriate localized label for the returned topic
	 * If more than one topics are found with the same system name a warning is generated and the first one is returned
	 * @param topicName
	 * @param locale
	 * @return
	 */
	public Topic findTopicByTopicName(String topicName, String locale, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(topicName)) {
			logger.warn("The provided topic name is blank. A NULL topic will be returned");
			return null;
		}
		
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. The default locale will be used");
			locale = PortalStringConstants.DEFAULT_LOCALE;
		}
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addNameEqualsCriterion(topicName);
			if (cacheRegion != null){
				topicCriteria.setCacheable(cacheRegion);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			
			CmsOutcome<Topic> topicsFound = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
				Topic firstTopic = topicsFound.getResults().get(0); 
				// if more than one topics correspond to the same name then we choose the first one but we generate a warning
				if (topicsFound.getResults().size() > 1)
					logger.warn("More than one topics correspond to name: " + topicName + " The first from  list will be returned. Topic names should be unique. Please fix it !!");
				return firstTopic;
			}
			else {
				logger.info("The provided topic name '"+topicName+"' does not exist.");
				return null;
			}
				
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving a topic through the topicName. The error was: ",e);
			
			return null;
		}
	}
	
	
	/**
	 * Use this method instead of taxonomyService.getTopic(topicId) in
	 * order to cache the query.
	 * 
	 * @param topicId
	 * @param locale
	 * @param cacheable
	 * @param cacheRegion 
	 * @return
	 */
	public Topic findTopicByTopicId(String topicId, String locale, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(topicId)) {
			logger.warn("The provided topic id is blank. A NULL topic will be returned");
			return null;
		}
		
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. A NULL topic  will be returned");
			return null;
		}
		
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addIdEqualsCriterion(topicId);
			
			if (cacheRegion != null){
				topicCriteria.setCacheable(cacheRegion);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			
			CmsOutcome<Topic> topicsFound = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
				Topic firstTopic = topicsFound.getResults().get(0); 
				// if more than one topics correspond to the same id then we choose the first one but we generate a warning
				if (topicsFound.getResults().size() > 1)
					logger.warn("More than one topics correspond to id: " + topicId + " The first from  list will be returned. This is a BUG!! Topic Ids should be unique. Please fix it !!");
				return firstTopic;
			}
			else {
				logger.info("The provided topic id does not exist.");
				return null;
			}
				
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving a topic through the topicName. The error was: ",e);
			
			return null;
		}
	}
	
	/**
	 * Find child topics of a topic if we know the topic name (i.e system name).
	 * The topics are returned ordered according to their order ranking (it should have been specified inside each returned topic) if orderByPosition is true
	 * Otherwise topics are order by their localized label for the provided locale.
	 * if cacheable is true the queries performed are cached
	 * 
	 * @param parentTopicName
	 * @param locale
	 * @param orderByPosition
	 * @param cacheable
	 * @param cacheRegion
	 * @return
	 */
	public List<Topic> findChildTopicsByParentTopicName(String parentTopicName, String locale, boolean orderByPosition, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(parentTopicName)) {
			logger.warn("The provided parent topic name is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		List<Topic> childTopics = null;
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			TopicCriteria parentCriteria = CmsCriteriaFactory.newTopicCriteria();
			parentCriteria.addNameEqualsCriterion(parentTopicName);
			topicCriteria.setAncestorCriteria(parentCriteria);
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			topicCriteria.searchInDirectAncestorOnly();
			
			if (cacheRegion != null){
				topicCriteria.setCacheable(cacheRegion);
			}
			else{
				topicCriteria.doNotCacheResults();
			}

			if (!orderByPosition) {
				topicCriteria.addOrderByLocale(locale, Order.ascending);
			}
			
			CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			childTopics = outcome.getResults();
			
			if (CollectionUtils.isNotEmpty(childTopics) && orderByPosition) {
				Collections.sort(childTopics, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			
			return childTopics;
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving topics by parent topic id. The error was: ",e);
			return new ArrayList<Topic>();
		}
		
	}
	
	/**
	 * Find child topics of a topic if we know the topic name (i.e system name)
	 * The topics are returned ordered according to their order ranking (it should have been specified inside each returned topic) if orderByPosition is true
	 * Otherwise topics are order by their localized label for the provided locale.
	 * if cacheable is true the queries performed are cached
	 * 
	 * @param parentTopicId
	 * @param locale
	 * @param orderByPosition
	 * @param cacheable
	 * @param cacheRegion
	 * @return
	 */
	public List<Topic> findChildTopicsByParentTopicId(String parentTopicId, String locale, boolean orderByPosition, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(parentTopicId)) {
			logger.warn("The provided parent topic id is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		List<Topic> childTopics = null;
		
		try {

			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addAncestorTopicIdEqualsCriterion(parentTopicId);
			topicCriteria.searchInDirectAncestorOnly();
			
			if (cacheRegion != null){
				topicCriteria.setCacheable(cacheRegion);
			}
			else{
				topicCriteria.doNotCacheResults();
			}

			topicCriteria.getRenderProperties().renderValuesForLocale(locale);

			if (!orderByPosition) {
				topicCriteria.addOrderByLocale(locale, Order.ascending);
			}
			CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			childTopics = outcome.getResults();
			
			if (CollectionUtils.isNotEmpty(childTopics) && orderByPosition) {
				Collections.sort(childTopics, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			
			return childTopics;

		}
		catch (Exception e) {
			logger.error("There was an error while retreiving topics by parent topic id. The error was: ",e);
			return new ArrayList<Topic>();
		}	
	}
	
	
	public List<Topic> findTopicsByLabel(String topicLabel, String locale, boolean cacheable)  {
		return findTopicsByLabel(topicLabel, locale, null, cacheable, false);
		
	}
	
	/**
	 * 
	 * @param topicLabel
	 * @param locale
	 * @return
	 */
	public List<Topic> findTopicsByLabel(String topicLabel, String locale, String taxonomyName, boolean cacheable, boolean caseSensitive)  {
		
		if (StringUtils.isBlank(topicLabel)) {
			logger.warn("The provided topic label is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			
			if (StringUtils.isNotBlank(taxonomyName))
			{
				topicCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			}


			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			localizationCriterion.setLocale(locale);
			localizationCriterion.addLocalizedLabel(topicLabel);

			if (! caseSensitive)
			{
				localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);
			}
			
			topicCriteria.addCriterion(localizationCriterion);
			
			if (cacheable){
				topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
			}
			else{
				topicCriteria.doNotCacheResults();
			}

			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			
			CmsOutcome<Topic> cmsOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			return cmsOutcome.getResults();
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving topics through the topic label. The error was: ",e);
			return new ArrayList<Topic>();
		}
	}
	
	// THIS ONLY WORKS WITH THE ASUMPTION that the parent of a subtree DOES NOT ALLOW content object references
	// returns the first predecessor topic which is a container topic only i.e. it does not allow references from content objects
	// This is useful to find the parent topic of subtrees in a tree assuming the convention that the parent of a tree branch does not allow referrer content objects.
	// However it is more efficient to model such autonomic subtrees in different taxonomies
	// if there is not predecessor that acts as a container then a root topic of the taxonomy is returned (that which holds all the branch) 
	public Topic findSubTreeParentTopic(Topic childTopic) {
		
		/*
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		
		topicCriteria.setOffsetAndLimit(0,1);
		topicCriteria.setCacheable(true, 
				PortalIntegerConstants.TOPIC_DEFAULT_CACHE_TIME_TO_LIVE_SECONDS, 
				PortalIntegerConstants.TOPIC_DEFAULT_CACHE_MAX_AGE_SECONDS);
		
		topicCriteria.setXPathQuery("//element ( *,bccms:topic )  [  (   @bccms:allowsReferrerContentObjects = 'false'   and  " +
				"( bccms:topic/@bccms:identifier = '"+childTopic.getId()+"'  or " +
				" bccms:topic//bccms:topic/@bccms:identifier = '"+childTopic.getId()+"' ) " +
				")  ]  ");
		
		CmsOutcome<Topic> topicOutcome = astroboaClientBean.getTopicService().searchTopics(topicCriteria);
		
		if (topicOutcome == null || topicOutcome.getCount() == 0){
			
			topicCriteria.reset();
			
			//No parent topic exists which is a container as well
			topicCriteria.setXPathQuery("bccms:system/bccms:taxonomyRoot/"+childTopic.getTaxonomy().getName()+
					"/element ( *,bccms:topic )  [  " +
					"	( bccms:topic/@bccms:identifier = '"+childTopic.getId()+"'  or " +
					" 		bccms:topic//bccms:topic/@bccms:identifier = '"+childTopic.getId()+"' ) " +
					"  ]  ");
			
			topicOutcome = astroboaClientBean.getTopicService().searchTopics(topicCriteria);
		}
		
		if (topicOutcome == null || topicOutcome.getCount() == 0){
			//Do we accept null as return value;
			logger.warn("Child topic has no parent. This is should NEVER HAPPEN and indicates a BUG."+ childTopic.getName());
			return null;
		}
		else{
			//Do we want to return an error if more than one topics are returned?
			if (topicOutcome.getCount() > 1){
				logger.warn("Found more than one parent topics for child topic "+ childTopic.getName());
			}
			
			//Return the first match.
			return topicOutcome.getResults().get(0);
			
		}
		*/
		Topic subTreeParentCandidate = childTopic;
		Topic failedParent = null;
		while (subTreeParentCandidate != null && subTreeParentCandidate.isAllowsReferrerContentObjects()) {
			failedParent = subTreeParentCandidate;
			subTreeParentCandidate = findTopicByTopicId(
					subTreeParentCandidate.getParent().getId(), 
					PortalStringConstants.DEFAULT_LOCALE, 
					PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
		}
		
		if (subTreeParentCandidate != null) {
			return subTreeParentCandidate;
		}
		else {
			return failedParent;
		}
		
	}
	
	
	public boolean topicIsDescendantOfAnotherTopic(Topic possibleDescendant, Topic parentTopic){
		
		if (possibleDescendant == null || StringUtils.isBlank(possibleDescendant.getId()) ||
				parentTopic == null ||	StringUtils.isBlank(parentTopic.getId())){
			return false;
		}

		try {
			//In case these are the same topic return true
			if (possibleDescendant.getId().equals(parentTopic.getId())){
				return true;
			}
			
			
			//If parentTopic is the parent of possible Descendant return true
			if (possibleDescendant.getParent() != null){
				if (possibleDescendant.getParent().getId().equals(parentTopic.getId())){
					return true;
				}
			}
			
			//Create a query to find if parentTopic has a child with the provided id
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addIdEqualsCriterion(possibleDescendant.getId());
			topicCriteria.addAncestorTopicIdEqualsCriterion(parentTopic.getId());
			topicCriteria.setOffsetAndLimit(0,2);
			
			topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
			
			CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			List<Topic> topics = outcome.getResults();
			
			return topics != null && topics.size() == 1;
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving topics by parent topic id. The error was: ",e);
			return false;
		}
	}
	
	/**
	 * Given a {@link RepositoryUser repository user} the method returns the tags of all other users
	 * @param repositoryUser The repository user whose tags will be excluded from returned tags
	 * @param locale The locale to be used for rendering the tag labels
	 * @param cacheable Specifies whether to cache the query results
	 * @return Returns the list of all user tags excluding the tags of the provided user
	 */
	public List<Topic> findOthersTags(RepositoryUser repositoryUser, String locale, boolean cacheable)  {
		
		if (repositoryUser == null) {
			logger.warn("The provided repository user is null. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		// we set criteria to retrieve all user tags and we will exclude from
		// them the provided repository user tags
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addTaxonomyNameEqualsCriterion(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME);

			if (cacheable){
				topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
			}
			else{
				topicCriteria.doNotCacheResults();
			}

			topicCriteria.getRenderProperties().renderValuesForLocale(locale);

			CmsOutcome<Topic> cmsOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			List<Topic> allTags = cmsOutcome.getResults();
			List<Topic> currentUserTags = new ArrayList<Topic>();
			if (allTags != null && (!allTags.isEmpty())) {
				// lets keep only the tags that are not owned by the current user
				for (Topic userTag : allTags) {
					if (userTag.getOwner().getId().equals(repositoryUser.getId()))
						currentUserTags.add(userTag);
				}
				if (!currentUserTags.isEmpty())
					allTags.removeAll(currentUserTags);

			}
			return allTags;
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving the tags. The error was: ", e);
			return new ArrayList<Topic>();
		}
	}
	
	
	public List<Topic> findMostPopularTags(String locale) {
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An empty topic list has been returned");
			return new ArrayList<Topic>();
		}
		
		try{
			return astroboaClient.getTopicService().getMostlyUsedTopics(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME, locale, 0,100).getResults();
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving the most popular tags. The error was: ", e);
			return new ArrayList<Topic>();
		}
	}
	
	public Topic createNewUserTag(String userTagLabel, String locale, RepositoryUser userTagOwner) {
		if (StringUtils.isBlank(userTagLabel)) {
			logger.warn("An empty label has been provided. User Tag cannot be created with an empty label. A null topic has been returned!!");
			return null;
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. An null topic has been returned");
			return null;
		}
		
		try{
			Topic userTag = astroboaClient.getCmsRepositoryEntityFactory().newTopic();
			userTag.setAllowsReferrerContentObjects(true);
			
			userTag.setTaxonomy(userTagOwner.getFolksonomy());
			userTag.addLocalizedLabel(locale, userTagLabel);
			userTag.setCurrentLocale(locale);
			userTag.setOwner(userTagOwner);
			
			return userTag;
		}
		catch (Exception e) {
			logger.error("There was an error while creating the tag. The error was: ", e);
			return null;
		}
	}
	
	public Topic findTopicInTopicListByTopicId(List<Topic> topicList, String topicId) {
		if (topicList != null)
			for (Topic topic : topicList) {
				if (topic.getId().equals(topicId))
					return topic;
			}
		return null;
	}
	
	public Topic findTopicInTopicListByTopicTaxonomyName(List<Topic> topicList, String topicTaxonomyName) {
		if (topicList != null)
			for (Topic topic : topicList) {
				if (topic.getTaxonomy().getName().equals(topicTaxonomyName))
					return topic;
			}
		return null;
	}
	
	public Topic findTopicInTopicListByLocalizedTopicLabel(List<Topic> topicList, String localizedName) {
		if (topicList != null && localizedName != null)
		{
			for (Topic topic : topicList) {
				if (StringUtils.equals(localizedName, topic.getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))) {
					return topic;
				}
			}
		}	
		return null;
	}
	
	
	public ContentObject findContentObjectInContentObjectListByContentObjectId(List<ContentObject> contentObjectList, String contentObjectId) {
		if (CollectionUtils.isNotEmpty(contentObjectList) && StringUtils.isNotBlank(contentObjectId)) {
			for (ContentObject contentObject : contentObjectList) {
				if (contentObject.getId().equals(contentObjectId)) {
					return contentObject;
				}
			}
		}
		return null;
	}
	
	
	public RepositoryUser findRepositoryUserByUserId(String repositoryUserId, String locale) {
		if (StringUtils.isBlank(repositoryUserId)) {
			logger.warn("The provided repository user id is null. A null repository user has been returned");
			return null;
		}
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. A null repository user has been returned");
			return null;
		}
		
		RepositoryUserCriteria repositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		List<RepositoryUser> resultRepositoryUsers;
		repositoryUserCriteria.addExternalIdEqualsCriterion(repositoryUserId);
		
		repositoryUserCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		resultRepositoryUsers = astroboaClient.getRepositoryUserService().searchRepositoryUsers(repositoryUserCriteria);
		
		if (resultRepositoryUsers != null && resultRepositoryUsers.size() == 1) {
			return resultRepositoryUsers.get(0);
		}
		else if (resultRepositoryUsers != null && resultRepositoryUsers.size() > 1) { // OOPS!! we found more than one user with the same id. Some problem with the repository exists
			logger.error("There was an error while looking for the repository user. More than one user with the same id was found. Some problem with the repository exists");
			return null;
		}
		else
			return null;
	}
	
	
	
	public String getLocalizedNameFromDefinition(LocalizableCmsDefinition cmsEntityDefinition, String locale) {
		final String localizedNameForLocale = cmsEntityDefinition.getDisplayName().getLocalizedLabelForLocale(locale);
		if (localizedNameForLocale == null)
			return cmsEntityDefinition.getName() + " (no localized label found for locale: " + locale + ")";
		else return localizedNameForLocale;
				
	}

	
	public List<SelectItem> getContentObjectTypesAsSelectItems() {
		try {
			List<String> contentObjectTypeNames = astroboaClient.getDefinitionService().getContentObjectTypes();
			String locale = PortalStringConstants.DEFAULT_LOCALE;
			List<SelectItem> contentObjectTypesAsSelectItems = new ArrayList<SelectItem>();
			
			for (String contentObjectTypeName : contentObjectTypeNames) {
				 ContentObjectTypeDefinition contentObjectTypeDefinition = (ContentObjectTypeDefinition) astroboaClient.getDefinitionService().getCmsDefinition(contentObjectTypeName, ResourceRepresentationType.DEFINITION_INSTANCE,false);
				 String contentObjectTypeLocalisedLabel = getLocalizedNameFromDefinition(contentObjectTypeDefinition, locale);
				 
				 SelectItem selectItem = new SelectItem(contentObjectTypeDefinition.getName(), contentObjectTypeLocalisedLabel, 
						 contentObjectTypeDefinition.getDescription().getLocalizedLabelForLocale(locale));
				 contentObjectTypesAsSelectItems.add(selectItem);
			}
			
			Collections.sort(contentObjectTypesAsSelectItems, new SelectItemComparator());
			
			return contentObjectTypesAsSelectItems;
		
		}
		catch (Exception e) {
			logger.error("", e);
			return new ArrayList<SelectItem>();
		}
	}
	
	
	public List<RatedTopic> findRatedTopics(ContentObjectCriteria contentObjectCriteria, String parentTopicName, String locale, boolean orderByTopicLabel, Integer rateLowerLimit){
		
		List<RatedTopic> ratedTopics = new ArrayList<RatedTopic>();
		
		if (contentObjectCriteria != null){
			
			//get Content Objects
			CmsOutcome<ContentObject> contentObjectOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (contentObjectOutcome != null && contentObjectOutcome.getCount() > 0){
				
				Map<String, RatedTopic> ratedTopicsPerId = new HashMap<String, RatedTopic>();
				
				List<ContentObject> rankedOutcomeList = contentObjectOutcome.getResults();
				
				List<String> alreadyVisitedTopics = new ArrayList<String>();
				
				for (ContentObject contentObject: rankedOutcomeList){
					
					//Retrieve subjects
					TopicReferenceProperty subjectProperty = (TopicReferenceProperty)contentObject.getCmsProperty("profile.subject");
					
					if (subjectProperty != null && ! subjectProperty.hasNoValues()){
							
						for (Topic subject : subjectProperty.getSimpleTypeValues()){
							
							if (ratedTopicsPerId.containsKey(subject.getId())){
								ratedTopicsPerId.get(subject.getId()).increaseRate();
							}
							else if (StringUtils.isBlank(parentTopicName)){
								final RatedTopic ratedTopic = new RatedTopic();
								ratedTopic.setTopic(subject);
								ratedTopic.increaseRate();
								ratedTopicsPerId.put(subject.getId(), ratedTopic);
							}
							else if (!alreadyVisitedTopics.contains(subject.getId())){
								
								//Search if this subject is descendant of any of the provided
								TopicCriteria ancestorCriteria = CmsCriteriaFactory.newTopicCriteria();
								ancestorCriteria.addNameEqualsCriterion(parentTopicName);
								
								TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
								topicCriteria.setAncestorCriteria(ancestorCriteria);
								topicCriteria.setOffsetAndLimit(0,1);
								
								topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
								topicCriteria.addIdEqualsCriterion(subject.getId());
								
								CmsOutcome<Topic> topicOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
								
								if (topicOutcome != null && topicOutcome.getCount() == 1){
									final RatedTopic ratedTopic = new RatedTopic();
									ratedTopic.setTopic(subject);
									ratedTopic.increaseRate();
									ratedTopicsPerId.put(subject.getId(), ratedTopic);
								}
								else{
									alreadyVisitedTopics.add(subject.getId());
								}
							}
						}
					}
				}
				
				ratedTopics.addAll(ratedTopicsPerId.values());
			}
		}
		
		if (CollectionUtils.isNotEmpty(ratedTopics)){
			Collections.sort(ratedTopics, new RatedTopicComparator(locale,!orderByTopicLabel, false));
			
			if (rateLowerLimit !=null){
				//Need to shorten list
				List<RatedTopic> limitedRatedTopics = new ArrayList<RatedTopic>();
				
				for (RatedTopic ratedTopic: ratedTopics){
					if (ratedTopic.getRate() >= rateLowerLimit){
						limitedRatedTopics.add(ratedTopic);
					}
				}
				
				return limitedRatedTopics;
			}

		}
		return ratedTopics;
	}
	
	public List<RatedTopic> findRatedTopics(List<ContentObject> contentObjects, String parentTopicName, String locale, boolean orderByTopicLabel, Integer rateLowerLimit){

		List<RatedTopic> ratedTopics = new ArrayList<RatedTopic>();

		if (CollectionUtils.isNotEmpty(contentObjects)){

			Map<String, RatedTopic> ratedTopicsPerId = new HashMap<String, RatedTopic>();

			List<String> alreadyVisitedTopics = new ArrayList<String>();

			for (ContentObject contentObject: contentObjects){

				//Retrieve subjects
				TopicReferenceProperty subjectProperty = (TopicReferenceProperty)contentObject.getCmsProperty("profile.subject");

				if (subjectProperty != null && ! subjectProperty.hasNoValues()){

					for (Topic subject : subjectProperty.getSimpleTypeValues()){

						if (ratedTopicsPerId.containsKey(subject.getId())){
							ratedTopicsPerId.get(subject.getId()).increaseRate();
						}
						else if (StringUtils.isBlank(parentTopicName)){
							final RatedTopic ratedTopic = new RatedTopic();
							ratedTopic.setTopic(subject);
							ratedTopic.increaseRate();
							ratedTopicsPerId.put(subject.getId(), ratedTopic);
						}
						else if (!alreadyVisitedTopics.contains(subject.getId())){

							//Search if this subject is descendant of any of the provided
							TopicCriteria ancestorCriteria = CmsCriteriaFactory.newTopicCriteria();
							ancestorCriteria.addNameEqualsCriterion(parentTopicName);

							TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
							topicCriteria.setAncestorCriteria(ancestorCriteria);
							topicCriteria.setOffsetAndLimit(0,1);
							
							topicCriteria.setCacheable(PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
							topicCriteria.addIdEqualsCriterion(subject.getId());

							CmsOutcome<Topic> topicOutcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

							if (topicOutcome != null && topicOutcome.getCount() == 1){
								final RatedTopic ratedTopic = new RatedTopic();
								ratedTopic.setTopic(subject);
								ratedTopic.increaseRate();
								ratedTopicsPerId.put(subject.getId(), ratedTopic);
							}
							else{
								alreadyVisitedTopics.add(subject.getId());
							}
						}
					}
				}
			}

			ratedTopics.addAll(ratedTopicsPerId.values());
		}

		if (CollectionUtils.isNotEmpty(ratedTopics)){
			Collections.sort(ratedTopics, new RatedTopicComparator(locale, !orderByTopicLabel, false));

			if (rateLowerLimit !=null){
				//Need to shorten list
				List<RatedTopic> limitedRatedTopics = new ArrayList<RatedTopic>();

				for (RatedTopic ratedTopic: ratedTopics){
					if (ratedTopic.getRate() >= rateLowerLimit){
						limitedRatedTopics.add(ratedTopic);
					}
				}

				return limitedRatedTopics;
			}
		}

		return ratedTopics;
	}

	public List<Topic> getLeafTopicsByParentTopicName(String taxonomyName, String parentTopicName, String locale, boolean orderByPosition,
			CacheRegion cacheRegion) {

		if (StringUtils.isBlank(parentTopicName) && StringUtils.isBlank(taxonomyName)){
			return new ArrayList<Topic>();
		}

		//Find ALL topics whose ancestor has name 'parentTopicName' and /or 
		//belong to taxonomy 'taxonomyName'
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();

		if (StringUtils.isNotBlank(parentTopicName)){
			TopicCriteria parentCriteria = CmsCriteriaFactory.newTopicCriteria();
			parentCriteria.addNameEqualsCriterion(parentTopicName);
			
			//If taxonomy name exists then it should be defined in terms of its parent
			if (StringUtils.isNotBlank(taxonomyName)){
				parentCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			}
			
			topicCriteria.setAncestorCriteria(parentCriteria);
		}
		else{
			//No parent topic name exists. Check for taxonomy name
			if (StringUtils.isNotBlank(taxonomyName)){
				topicCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			}
		}

		/*TopicCriteria parentCriteria = CmsCriteriaFactory.newTopicCriteria();
		if (StringUtils.isNotBlank(parentTopicName)){
			parentCriteria.addNameEqualsCriterion(parentTopicName);
		}

		if (StringUtils.isNotBlank(taxonomyName)){
			parentCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
		}

		topicCriteria.setAncestorCriteria(parentCriteria);
		*/
		topicCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		if (cacheRegion != null){
			topicCriteria.setCacheable(cacheRegion);
		}
		else{
			topicCriteria.doNotCacheResults();
		}

		CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);


		if (outcome == null || outcome.getCount() == 0){
			return new ArrayList<Topic>();
		}

		//For every topic returned check its number of children to determine if it is a leaf
		List<Topic> leafTopics = new ArrayList<Topic>();

		List<Topic> topicsFromOutcome = outcome.getResults();

		for (Topic topicFromOutcome : topicsFromOutcome){
			if (topicFromOutcome.getNumberOfChildren() <= 0){
				//this topic is a leaf
				leafTopics.add(topicFromOutcome);
			}
		}

		//Now order leaf topics
		if (CollectionUtils.isNotEmpty(leafTopics)){
			if (orderByPosition){
				Collections.sort(leafTopics, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			else{
				Collections.sort(leafTopics, new TopicComparator(locale, OrderByProperty.LABEL));
			}
		}
		return leafTopics;

	}
	
	public ContentObject findContentObjectBySystemName(String contentType, String systemName, String locale, CacheRegion cacheRegion) {
		
		if (StringUtils.isBlank(systemName)) {
			logger.warn("The provided system name is blank. A NULL content object will be returned");
			return null;
		}
		
		
		if (StringUtils.isBlank(locale)) {
			logger.info("The provided locale is blank. The default locale will be used");
			locale = PortalStringConstants.DEFAULT_LOCALE;
		}
		
		try {
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			
			if (StringUtils.isNotBlank(contentType)){
				contentObjectCriteria.addContentObjectTypeEqualsCriterion(contentType);
			}
			
			contentObjectCriteria.addSystemNameEqualsCriterion(systemName);
			
			if (cacheRegion != null){
				contentObjectCriteria.setCacheable(cacheRegion);
			}
			else{
				contentObjectCriteria.doNotCacheResults();
			}
			contentObjectCriteria.getRenderProperties().renderValuesForLocale(locale);
			
			//We expect only one. But in case there are more than one there is no need to retrieve all of them. Two 
			//are sufficient
			contentObjectCriteria.setOffsetAndLimit(0, 2);
			
			 CmsOutcome<ContentObject> contentObjectsFound = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			 
			if (CollectionUtils.isNotEmpty(contentObjectsFound.getResults())) {
				 ContentObject firstContentObject = contentObjectsFound.getResults().get(0); 
				// if more than one topics correspond to the same name then we choose the first one but we generate a warning
				if (contentObjectsFound.getCount() > 1)
					logger.warn("More than one content objects correspond to name: " + systemName + " The first from  list will be returned.");
				return firstContentObject;
			}
			else {
				logger.info("No content object found with system name '"+systemName);
				return null;
			}
				
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving a topic through the topicName. The error was: ",e);
			
			return null;
		}
	}
	

	public List<Topic> findChildTopicsByParentTopicNameAndTaxonomyName(String taxonomyName, String parentTopicName, boolean orderByPosition){
		return findChildTopicsByParentTopicNameAndTaxonomyName(taxonomyName, parentTopicName, JSFUtilities.getLocaleAsString(), orderByPosition, PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
	}
	
	public List<Topic> findChildTopicsByParentTopicNameAndTaxonomyName(String taxonomyName, String parentTopicName, String locale, boolean orderByPosition,
			CacheRegion cacheRegion) {

		if (StringUtils.isBlank(parentTopicName) && StringUtils.isBlank(taxonomyName)){
			return new ArrayList<Topic>();
		}

		//Find ALL direct topics whose ancestor has name 'parentTopicName' and /or 
		//belong to taxonomy 'taxonomyName'
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.searchInDirectAncestorOnly();
		
		if (StringUtils.isNotBlank(parentTopicName)){
			TopicCriteria parentCriteria = CmsCriteriaFactory.newTopicCriteria();
			parentCriteria.addNameEqualsCriterion(parentTopicName);
			
			//If taxonomy name exists then it should be defined in terms of its parent
			if (StringUtils.isNotBlank(taxonomyName)){
				parentCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			}
			
			topicCriteria.setAncestorCriteria(parentCriteria);
		}
		else{
			//No parent topic name exists. Check for taxonomy name
			if (StringUtils.isNotBlank(taxonomyName)){
				topicCriteria.addTaxonomyNameEqualsCriterion(taxonomyName);
			}
		}

		topicCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		if (cacheRegion != null){
			topicCriteria.setCacheable(cacheRegion);
		}
		else{
			topicCriteria.doNotCacheResults();
		}

		CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

		if (outcome == null || outcome.getCount() == 0){
			return new ArrayList<Topic>();
		}

		List<Topic> topicsFromOutcome = outcome.getResults();

		//Now order leaf topics
		if (CollectionUtils.isNotEmpty(topicsFromOutcome)){
			if (orderByPosition){
				Collections.sort(topicsFromOutcome, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			else{
				Collections.sort(topicsFromOutcome, new TopicComparator(locale, OrderByProperty.LABEL));
			}
		}
		return topicsFromOutcome;

	}
	
	
	/**
	 * The purpose of this method is to return a list that acts as a pseudo tree of the topics under a specified taxonomy and a specified parent topic.
	 * The topics are added in a list traversing the tree in a depth first search manner so that
	 * in the final list the child topics appear under their parent separately ordered and their labels are appropriately indented.
	 * This kind of list is useful for generating selection boxes without the need to create a separate selection box per tree level.
	 * It could be also useful in generation of menus.
	 *
	 * If a parent topic is not provided then all topics in the taxonomy are returned
	 * 
	 * In each pseudo level the topics are alphabetically ordered. 
	 * 
	 * If a non null cache region is provided then the queries are cached in the specified region
	 * Otherwise they are cached in the default regions for Topic queries as specified in portal settings
	 * 
	 * @param taxonomyName
	 * @param parentTopicName
	 *
	 * 
	 * @return {@link TopicWrapper}
	 */
	public List<TopicWrapper> findAllTopicsInTaxonomyAndReturnInAphabeticalTreeOrder(String taxonomyName, String parentTopicName) {

		List<TopicWrapper> allTopics = new ArrayList<TopicWrapper>();
		
		TopicComparator topicComparator = new TopicComparator(JSFUtilities.getLocaleAsString(),OrderByProperty.LABEL);
		
		if (StringUtils.isBlank(parentTopicName)) {
			Taxonomy taxonomy = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
			if (taxonomy!=null && CollectionUtils.isNotEmpty(taxonomy.getRootTopics())){
				
				List<Topic> alphabeticallyOrderedTopics = taxonomy.getRootTopics();
				Collections.sort(alphabeticallyOrderedTopics, topicComparator);
				for (Topic topic : alphabeticallyOrderedTopics) {
					expandTopic(allTopics, topic, 0, topicComparator);
				}
			}

		}
		else {
			List<Topic> alphabeticallyOrderedChildTopics =
				findChildTopicsByParentTopicNameAndTaxonomyName(taxonomyName, parentTopicName, JSFUtilities.getLocaleAsString(), false, PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);

			if (alphabeticallyOrderedChildTopics != null && CollectionUtils.isNotEmpty(alphabeticallyOrderedChildTopics)) {
				for (Topic topic : alphabeticallyOrderedChildTopics) {
					expandTopic(allTopics, topic, 0, topicComparator);
				}
			}
		}
		
		return allTopics;
	}
	
	public void expandTopic(List<TopicWrapper> topics, Topic topic, int currentLevel, TopicComparator topicComparator) {
		topics.add(new TopicWrapper(topic, currentLevel));
		if (CollectionUtils.isNotEmpty(topic.getChildren())) {
			++currentLevel;
			List<Topic> alphabeticallyOrderedChildTopics = topic.getChildren();
			Collections.sort(alphabeticallyOrderedChildTopics, new TopicComparator(JSFUtilities.getLocaleAsString(),OrderByProperty.LABEL));
			for (Topic childTopic : topic.getChildren()) {
				expandTopic(topics, childTopic, currentLevel, topicComparator);
			}
		}
	}
	
	
	public class TopicWrapper  {
		private final Topic topic;
		private int topicLevel;
		
		public TopicWrapper(Topic topic, int topicLevel) {
			super();
			this.topic = topic;
			this.topicLevel = topicLevel;
		}

		public int getTopicLevel() {
			return topicLevel;
		}

		public void setTopicLevel(int topicLevel) {
			this.topicLevel = topicLevel;
		}

		public Topic getTopic() {
			return topic;
		}
		
		public String getIndentedLocalizedLabel() {
			if (topicLevel > 0) {
				String labelPrefix = "";
				for (int i=1; i<=topicLevel; i++) {
					labelPrefix += "---";
				}
				labelPrefix += "> ";
				return labelPrefix + topic.getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
			}
			else {
				return topic.getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
			}
		}
		
	}
	
	public List<SelectItem> valueRangeAsSelectItems(CmsProperty<?,?> valueRangeCmsProperty) {
		List<SelectItem> valueRangeSelectItems = new ArrayList<SelectItem>();
	    
		Map<?,Localization> valueRange = ((SimpleCmsPropertyDefinition<?>)valueRangeCmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueRange != null){
			
			valueRangeSelectItems.add(new SelectItem("", JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null)));
			
			for (Object value : valueRange.keySet()){
				valueRangeSelectItems.add(new SelectItem(value,((Localization)valueRange.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))); 
			}
		}
		
	    return valueRangeSelectItems;
	}
	
	// cmsPropertyFullPath means that the provided property path should be prefixed by the containing content object type
	public List<SelectItem> valueRangeAsSelectItems(String cmsPropertyFullPath) {
		List<SelectItem> valueRangeSelectItems = new ArrayList<SelectItem>();
	    
		Map<?,Localization> valueRange = ((SimpleCmsPropertyDefinition<?>)astroboaClient.getDefinitionService().getCmsDefinition(cmsPropertyFullPath, ResourceRepresentationType.DEFINITION_INSTANCE,false)).getValueEnumeration();
		
		if (valueRange != null){
			
			valueRangeSelectItems.add(new SelectItem("", JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null)));
			
			for (Object value : valueRange.keySet()){
				valueRangeSelectItems.add(new SelectItem(value,((Localization)valueRange.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))); 
			}
		}
		
	    return valueRangeSelectItems;
	}
	
	public String localizedLabelOfValueInValueRange(CmsProperty<?,?> valueRangeCmsProperty, Object value){

		Map<?,Localization> valueRange = ((SimpleCmsPropertyDefinition<?>)valueRangeCmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueRange != null && valueRange.get(value) != null) {
			
			return	((Localization)valueRange.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()); 
		}
		else {
			return "";
		}
		
	}
	
	public void increaseCounterOfTopicAndAncestors(Topic subject, Map<String, RatedTopic> ratedTopicsPerId) {

		if (ratedTopicsPerId.containsKey(subject.getId())){
			ratedTopicsPerId.get(subject.getId()).increaseRate();
		}
		else {
			final RatedTopic ratedTopic = new RatedTopic(subject, 1);
			ratedTopicsPerId.put(subject.getId(), ratedTopic);
		}

		Topic ancestor = subject.getParent();
		if (ancestor != null) {
			increaseCounterOfTopicAndAncestors(ancestor, ratedTopicsPerId);
		}

	}
	
	public List<RatedTopic> generateTagCloud(List<RatedTopic> ratedTopics, boolean orderByRate, int cloudRatingValues, String locale) {
		List<RatedTopic> tagCloud = new ArrayList<RatedTopic>();
		if (CollectionUtils.isNotEmpty(ratedTopics)){
			copyCollection(ratedTopics, tagCloud);
			int maxContentObjectsInTopic = ratedTopics.get(0).getRate();
			int minContentObjectsInTopic = ratedTopics.get(ratedTopics.size() -1).getRate();
			int minToMaxTopicRatingValues = maxContentObjectsInTopic - minContentObjectsInTopic + 1;
			if (cloudRatingValues <= 10) {
				cloudRatingValues = 10;
			}
			int ratioOfTopicRatingValuesToCloudRatingValues;
			if (minToMaxTopicRatingValues > cloudRatingValues) {
				ratioOfTopicRatingValuesToCloudRatingValues = minToMaxTopicRatingValues / cloudRatingValues;


				for (RatedTopic ratedTopic : tagCloud) {
					ratedTopic.setRate(ratedTopic.getRate() / ratioOfTopicRatingValuesToCloudRatingValues);
				}
			}
			Collections.sort(tagCloud, new RatedTopicComparator(locale, orderByRate, false));
			
		}
		
		return tagCloud;
	}
	
	
	public void copyCollection(Collection<RatedTopic> sourceCollection, Collection<RatedTopic> destinationCollection) {
		if (CollectionUtils.isEmpty(sourceCollection)) {
			logger.warn("Empty source collection was provided. Nothing to copy");
			return;
		}
		if (destinationCollection == null) {
			logger.warn("No destination collection was provided. Cannot copy source collection");
		}
		for (RatedTopic sourceRatedTopic : sourceCollection) {
			destinationCollection.add(new RatedTopic(sourceRatedTopic));
		}
		
	}
	
}
