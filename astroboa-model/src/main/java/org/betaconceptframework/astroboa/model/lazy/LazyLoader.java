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
package org.betaconceptframework.astroboa.model.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.slf4j.LoggerFactory;



/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class LazyLoader  {

	private SpaceService spaceService;

	private TopicService topicService;

	private DefinitionService definitionService;

	private ContentService contentService;
	
	public LazyLoader(SpaceService spaceService, TopicService topicService,
			DefinitionService definitionService, ContentService contentService) {
		super();
		this.spaceService = spaceService;
		this.topicService = topicService;
		this.definitionService = definitionService;
		this.contentService = contentService;
	}


	public void lazyLoadTaxonomyRootTopics(Taxonomy taxonomy, String authenticationToken) {

		try{
			
			activateClientContextForAuthenticationToken(authenticationToken);
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.searchInDirectAncestorOnly();

			topicCriteria.addTaxonomyCriterion(taxonomy);

			CmsOutcome<Topic> rootTopics = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			if (rootTopics != null && CollectionUtils.isNotEmpty(rootTopics.getResults())){

				for (Topic rootTopic: rootTopics.getResults()){
					taxonomy.addRootTopic(rootTopic);
				}

				taxonomy.setNumberOfRootTopics((int)rootTopics.getCount());

			}
			else{
				taxonomy.setRootTopics(new ArrayList<Topic>());
				taxonomy.setNumberOfRootTopics(0);
			}
		}
		catch (Exception e)
		{
			throw new CmsException(e);
		}
	}


	public void activateClientContextForAuthenticationToken(String authenticationToken) {
		
		if (! StringUtils.equals(AstroboaClientContextHolder.getActiveAuthenticationToken(), authenticationToken)){
			AstroboaClientContextHolder.activateClientContextForAuthenticationToken(authenticationToken);
		}
		
	}


	public void lazyLoadSpaceChildren(Space space, String authenticationToken) {

		try {

			if (StringUtils.isBlank(space.getId()))
				throw new CmsException("Space "+ space.getName() + "'s id was not found");

			activateClientContextForAuthenticationToken(authenticationToken);
			
			SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
			spaceCriteria.searchInDirectAncestorOnly();
			spaceCriteria.addAncestorSpaceIdEqualsCriterion(space.getId());

			CmsOutcome<Space> children = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.SPACE_LIST);

			if (children != null && children.getCount() > 0){
				space.setNumberOfChildren((int)children.getCount());

				for (Space child : children.getResults())
					space.addChild(child);
			}
			else{
				space.setNumberOfChildren(0);
			}

		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public  void lazyLoadTopicChildren(Topic topic, String authenticationToken) {

		try {

			if (StringUtils.isBlank(topic.getId())){
				throw new CmsException("Topic "+ topic.getName() + " was not found");
			}

			activateClientContextForAuthenticationToken(authenticationToken);
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.searchInDirectAncestorOnly();
			topicCriteria.addAncestorTopicIdEqualsCriterion(topic.getId());

			CmsOutcome<Topic> children = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			if (children != null && children.getCount() > 0){
				topic.setNumberOfChildren((int)children.getCount());

				for (Topic child : children.getResults()){
					topic.addChild(child);
				}
			}
			else
				topic.setNumberOfChildren(0);

		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}



	public void lazyLoadReferrerContentObjectsForSpace(Space space, String authenticationToken) {
		try {

			activateClientContextForAuthenticationToken(authenticationToken);
			
			List<String> cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity = spaceService.getContentObjectIdsWhichReferToSpace(space.getId());

			space.setReferrerContentObjects(cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity);
			space.setNumberOfReferrerContentObjects(cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity.size());
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public void lazyLoadContentObjectIdsWhichReferToTopic(Topic topic, String authenticationToken) {
		try {
			
			activateClientContextForAuthenticationToken(authenticationToken);

			List<String> cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity = topicService.getContentObjectIdsWhichReferToTopic(topic.getId());

			((TopicImpl)topic).setContentObjectIdsWhichReferToThisTopic(cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity);
			((TopicImpl)topic).setNumberOfContentObjectIdsWhichReferToThisTopic(cmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity.size());
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public void lazyLoadNumberOfContentObjectIdsWhichReferToTopic(Topic topic, String authenticationToken) {
		try {

			activateClientContextForAuthenticationToken(authenticationToken);
			
			final int countOfContentObjectIdsWhichReferToTopic = topicService.getCountOfContentObjectIdsWhichReferToTopic(topic.getId());

			((TopicImpl)topic).setNumberOfContentObjectIdsWhichReferToThisTopic(countOfContentObjectIdsWhichReferToTopic);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}
	public void lazyLoadNumberOfReferrerContentObjectsForSpace(Space space, String authenticationToken) {
		try {
			
			activateClientContextForAuthenticationToken(authenticationToken);

			space.setNumberOfReferrerContentObjects(spaceService.getCountOfContentObjectIdsWhichReferToSpace(space.getId()));
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public void lazyLoadContentObjectReferences(Space space, String authenticationToken) {
		try {
			
			activateClientContextForAuthenticationToken(authenticationToken);
			
			List<String> contentObjectReferences = spaceService.getContentObjectIdsWhichResideInSpace(space.getId());

			space.setContentObjectReferences(contentObjectReferences);
			space.setNumberOfContentObjectReferences(CollectionUtils.isEmpty(contentObjectReferences)? 0 : contentObjectReferences.size());
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}

	public void loadChildTopicIdsForSpecifiedProfileSubjectIds(List<String> subjectIds, String authenticationToken, CacheRegion cacheRegion) {
		try {

			List<String> newSubjectIds = new ArrayList<String>();

			if (CollectionUtils.isNotEmpty(subjectIds)){

				activateClientContextForAuthenticationToken(authenticationToken);
				
				TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
				topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
				topicCriteria.addAnyAncestorTopicIdEqualsCriterion(subjectIds);
				topicCriteria.getRenderProperties().renderParentEntity(false);
				
				if (cacheRegion != null){
					topicCriteria.setCacheable(cacheRegion);
				}

				CmsOutcome<Topic> topicResults = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

				if (topicResults != null && topicResults.getCount() > 0){

					for(Topic topic: topicResults.getResults()){
						newSubjectIds.add(topic.getId());
					}
				}

				//Just augment existing list
				subjectIds.addAll(newSubjectIds);
			}
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies(String authenticationToken) {
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		return definitionService.getTopicPropertyPathsPerTaxonomies();
	}


	public List<String> getMultivalueProperties() {
		
		List<String> multivalueProperties = definitionService.getMultivalueProperties();
		
		if (multivalueProperties == null)
		{
			return new ArrayList<String>();
		}
		
		return new ArrayList<String>(multivalueProperties);
	}


	public List<CmsProperty<?,?>> loadChildCmsProperty(String childPropertyName, String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties, String authenticationToken){
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		return contentService.loadChildCmsProperty(childPropertyName, parentComplexCmsPropertyDefinitionFullPath, 
				jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct, 
				renderProperties);
	}

	public DefinitionService getDefinitionService(){
		return definitionService;
	}


	public void lazyLoadParentTopic(Topic topic, String parentId, String authenticationToken) {
		try{
			if (StringUtils.isNotBlank(parentId)){
				
				activateClientContextForAuthenticationToken(authenticationToken);
				
				Topic parentTopic = topicService.getTopic(parentId, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
				
				if (parentTopic != null){
					topic.setParent(parentTopic);
				}
			}
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public Map<String, List<String>> getContentTypeHierarchy(String authenticationToken) {
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		return definitionService.getContentTypeHierarchy();
	}

	public Taxonomy lazyLoadTaxonomyForTopic(String topicId, String authenticationToken){
		
		if (StringUtils.isNotBlank(topicId)){
			
			activateClientContextForAuthenticationToken(authenticationToken);
			
			Topic topic = topicService.getTopic(topicId,ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
			
			if (topic != null){
				return topic.getTaxonomy();
			}
		}
		
		return null;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public boolean hasValueForProperty(String propertyPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty,String authenticationToken){
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		return contentService.hasValueForProperty(propertyPath,
				jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
	}

	/**
	 * @param topicName
	 * @param activeAuthenticationToken
	 * @return
	 */
	public Topic getTopicByName(String topicName,	String authenticationToken) {
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		Topic topic = topicService.getTopic(topicName, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
		
		if (topic == null ){
			
			LoggerFactory.getLogger(getClass()).warn("No topic found with name "+topicName);
			
			return null;
		}
		
		return topic;
		
	}

	public ContentObject getContentObjectBySystemName(String systemName, String authenticationToken) {
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		ContentObject object = contentService.getContentObject(systemName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		if (object == null ){
			LoggerFactory.getLogger(getClass()).info("No content object found with system name "+systemName);
			
			return null;
		}
		
		return object;
		
	}

	public Map<String, Integer> getDefinitionHierarchyDepthPerContentType(String authenticationToken) {
		
		activateClientContextForAuthenticationToken(authenticationToken);
		
		return definitionService.getDefinitionHierarchyDepthPerContentType();
	}


	public byte[] lazyLoadBinaryValue(String binaryChannelId, String authenticationToken) {

		activateClientContextForAuthenticationToken(authenticationToken);

		return contentService.getBinaryChannelContent(binaryChannelId);
	}

}
