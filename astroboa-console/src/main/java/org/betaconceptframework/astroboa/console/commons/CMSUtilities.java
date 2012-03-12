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
package org.betaconceptframework.astroboa.console.commons;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.TopicComparator.OrderByProperty;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commonly used search queries and methods possibly useful to all developers utilizing the repository  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class CMSUtilities {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private TopicService topicService;
	private RepositoryUserService repositoryUserService;

	/**
	 * It finds a topic by means of the taxonomy name and the topic system name
	 * The taxonomy name is the name of the taxonomy under which the topic has been defined.
	 * If taxonomy name is blank then the DEFAULT SYSTEM TAXONOMY_INSTANCE is assumed
	 * The provided locale is required in order to render the appropriate localized label for the returned topic
	 * If more than one topics are found with the same system name a warning is generated and the first one is returned
	 * @param topicName
	 * @param locale
	 * @param cacheable 
	 * 
	 * @return
	 */
	public Topic findTopicByTopicName(String topicName, String locale, boolean cacheable) {
		
		if (StringUtils.isBlank(topicName)) {
			logger.warn("The provided topic name is blank. A NULL topic will be returned");
			return null;
		}
		
		
		if (StringUtils.isBlank(locale)) {
			logger.warn("The provided locale is blank. A NULL topic  will be returned");
			return null;
		}
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addNameEqualsCriterion(topicName);
			
			if (cacheable){
				topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			
			CmsOutcome<Topic> topicsFound = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
				Topic firstTopic = topicsFound.getResults().get(0); 
				// if more than one topics correspond to the same name then we choose the first one but we generate a warning
				if (topicsFound.getResults().size() > 1)
					logger.warn("More than one topics correspond to name: " + topicName + " The first from  list will be returned. This is a BUG!! Topic Ids should be unique. Please fix it !!");
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
	 * Use this method instead of taxonomyService.getTopic(topicId) in
	 * order to cache the query.
	 * 
	 * @param topicId
	 * @param locale
	 * @param cacheable
	 * @return
	 */
	public Topic findTopicByTopicId(String topicId, String locale, boolean cacheable) {
		
		if (StringUtils.isBlank(topicId)) {
			logger.warn("The provided topic id is blank. A NULL topic will be returned");
			return null;
		}
		
		
		if (StringUtils.isBlank(locale)) {
			logger.warn("The provided locale is blank. A NULL topic  will be returned");
			return null;
		}
		
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addIdEqualsCriterion(topicId);
			if (cacheable){
				topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			CmsOutcome<Topic> topicsFound = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
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
	 * @param cacheable
	 * @param orderByPosition
	 * @return
	 */
	public List<Topic> findChildTopicsByParentTopicName(String parentTopicName, String locale, boolean cacheable, boolean orderByPosition) {
		
		if (StringUtils.isBlank(parentTopicName)) {
			return null;
		}
		
		List<Topic> childTopics = null;
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			TopicCriteria parentCriteria = CmsCriteriaFactory.newTopicCriteria();
			parentCriteria.addNameEqualsCriterion(parentTopicName);
			topicCriteria.setAncestorCriteria(parentCriteria);
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			topicCriteria.searchInDirectAncestorOnly();
			if (cacheable){
				topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			if (!orderByPosition) {
				topicCriteria.addOrderByLocale(locale, Order.ascending);
			}
			
			CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			childTopics = outcome.getResults();
			
			if (CollectionUtils.isNotEmpty(childTopics) && orderByPosition) {
				Collections.sort(childTopics, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			
			return childTopics;
		}
		catch (Exception e) {
			logger.error("",e);
			return null;
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
	 * @param cacheable
	 * @param orderByPosition
	 * @return
	 */
	public List<Topic> findChildTopicsByParentTopicId(String parentTopicId, String locale, boolean cacheable, boolean orderByPosition) {
		
		if (StringUtils.isBlank(parentTopicId)) {
			return null;
		}
		
		List<Topic> childTopics = null;
		
		try {

			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addAncestorTopicIdEqualsCriterion(parentTopicId);
			topicCriteria.searchInDirectAncestorOnly();
			if (cacheable){
				topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);
			}
			else{
				topicCriteria.doNotCacheResults();
			}
			topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			if (!orderByPosition) {
				topicCriteria.addOrderByLocale(locale, Order.ascending);
			}
			CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			childTopics = outcome.getResults();
			
			if (CollectionUtils.isNotEmpty(childTopics) && orderByPosition) {
				Collections.sort(childTopics, new TopicComparator(locale, OrderByProperty.POSITION));
			}
			
			return childTopics;

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	
	/**
	 * 
	 * @param topicLabel
	 * @param locale
	 * @return
	 * @throws CmsException
	 * @throws DublicateRepositoryUserExternalIdException
	 */
	public List<Topic> findTopicsByLabel(String topicLabel, String locale) throws CmsException, DublicateRepositoryUserExternalIdException {
		
		try {
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			
			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			// we ignore locale to search through all localized labels
			//	localizationCriterion.setLocale(locale);
			localizationCriterion.addLocalizedLabel(topicLabel + "*");
			localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);

			topicCriteria.addCriterion(localizationCriterion);

			// topicCriteria.getRenderProperties().renderValuesForLocale(locale);
			topicCriteria.setOffsetAndLimit(0, 30);
			//topicCriteria.getResultRowRange().setRange(0, 30);


			CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			return cmsOutcome.getResults();
		}
		catch (Exception e) {
			logger.error("There was an error while retreiving topics through the topic label. The error was: ",e);
			return null;
		}
	}
	
	
	public List<Topic> findOthersTags(RepositoryUser loggegInRepositoryUser, String locale) throws CmsException {
		// we set criteria to retrieve all user tags and we will exclude from
		// them the current user tags
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.addTaxonomyNameEqualsCriterion(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME);
		
		topicCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		List<Topic> allTags = cmsOutcome.getResults();
		List<Topic> currentUserTags = new ArrayList<Topic>();
		if (allTags != null && (!allTags.isEmpty())) {
			// lets keep only the tags that are not owned by the current user
			for (Topic userTag : allTags) {
				if (userTag.getOwner().getId().equals(loggegInRepositoryUser.getId()))
					currentUserTags.add(userTag);
			}
			if (!currentUserTags.isEmpty())
				allTags.removeAll(currentUserTags);

		}
		return allTags;	
	}
	
	
	public List<Topic> findMostPopularTags(String locale) throws Exception {
		try{
			return topicService.getMostlyUsedTopics(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME, locale, 0,100).getResults();
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	public Topic createNewUserTag(String userTagLabel, String locale, RepositoryUser userTagOwner) {
		if (StringUtils.isNotBlank(userTagLabel)) {
			Topic userTag = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic();
			userTag.setAllowsReferrerContentObjects(true);
			
			userTag.setTaxonomy(userTagOwner.getFolksonomy());
			userTag.addLocalizedLabel(locale, userTagLabel);
			userTag.setCurrentLocale(locale);
			userTag.setOwner(userTagOwner);
			
			return userTag;
		}
		else {
			logger.warn("An empty label has been provided. User Tag cannot be created with an empty label. A null topic will be returned!!");
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
	
	
	public Topic findTopicInTopicListByLocalizedTopicLabel(List<Topic> topicList, String localizedName) {
		if (topicList != null && localizedName != null)
		{
			for (Topic topic : topicList) {
				if (localizedName.equals(topic.getLocalizedLabelForCurrentLocale()))
					return topic;
				}
		}	
		return null;
	}
	
	public ContentObjectUIWrapper findContentObjectUIWrapperInContentObjectUIWrapperListByContentObjectId(List<ContentObjectUIWrapper> contentObjectUIWrapperList, String contentObjectId) {
		for (ContentObjectUIWrapper contentObjectUIWrapper : contentObjectUIWrapperList) {
			if (contentObjectUIWrapper.getContentObject().getId().equals(contentObjectId))
				return contentObjectUIWrapper;
		}
		return null;
	}
	
	public List<RepositoryUser> findRepositoryUsersByExternalIdOrLabelId(String externalIdOrLabel, String locale) throws CmsException, DublicateRepositoryUserExternalIdException{
		
		if (StringUtils.isBlank(externalIdOrLabel)){
			return new ArrayList<RepositoryUser>();
		}
		
		RepositoryUserCriteria repositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		
		
		Criterion externalIdCriterion = CriterionFactory.contains(CmsBuiltInItem.ExternalId.getJcrName(), 
				CmsConstants.ANY_NAME + externalIdOrLabel + CmsConstants.ANY_NAME);
		
		Criterion labelCriterion = CriterionFactory.contains(CmsBuiltInItem.Label.getJcrName(), 
				CmsConstants.ANY_NAME + externalIdOrLabel + CmsConstants.ANY_NAME);
		
		repositoryUserCriteria.addCriterion(CriterionFactory.or(externalIdCriterion, labelCriterion));
		
		repositoryUserCriteria.addOrderByLabel(Order.ascending);
		
		repositoryUserCriteria.setOffsetAndLimit(0, 30);
		
		repositoryUserCriteria.doNotCacheResults();
		
		if (locale != null)
			repositoryUserCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		return repositoryUserService.searchRepositoryUsers(repositoryUserCriteria);
		
	}
	
	public RepositoryUser findRepositoryUserByUserId(String userId, String locale) throws CmsException, DublicateRepositoryUserExternalIdException{
		RepositoryUserCriteria repositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		List<RepositoryUser> resultRepositoryUsers;
		repositoryUserCriteria.addExternalIdEqualsCriterion(userId);
		
		if (locale != null)
			repositoryUserCriteria.getRenderProperties().renderValuesForLocale(locale);
		
		resultRepositoryUsers = repositoryUserService.searchRepositoryUsers(repositoryUserCriteria);
		
		if (resultRepositoryUsers != null && resultRepositoryUsers.size() == 1)
			return resultRepositoryUsers.get(0);
		else if (resultRepositoryUsers != null && resultRepositoryUsers.size() > 1) { // OOPS!! we found more than one user with the same id. Some problem with the repository exists
			throw new DublicateRepositoryUserExternalIdException(userId);
		}
		else
			return null;
	}
	
	
	
	public RepositoryUser findLoggedInRepositoryUser(String locale) throws CmsException, DublicateRepositoryUserExternalIdException, NonExistentUserIdException {
		String userId = Identity.instance().getPrincipal() != null? Identity.instance().getPrincipal().getName() : null;
		if (StringUtils.isNotEmpty(userId)) {
		 return findRepositoryUserByUserId(userId, locale);
		}
		throw new NonExistentUserIdException();
	}
	
	
	
	
	public String getLocalizedNameOfContentObjectProperty(LocalizableCmsDefinition propertyDefinition, String locale) {
		final String localizedNameForLocale = propertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale);
		if (localizedNameForLocale == null)
			return propertyDefinition.getName() + " (no localized label found for locale: " + locale + ")";
		else return localizedNameForLocale;
				
	}


	public String getLocalizedNameOfContentObjectType(ContentObjectTypeDefinition contentObjectTypeDefinition, String locale) {
		return getLocalizedNameOfContentObjectProperty(contentObjectTypeDefinition, locale);
				
	}
	
	
	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}


	public void setRepositoryUserService(RepositoryUserService repositoryUserService) {
		this.repositoryUserService = repositoryUserService;
	}


}
