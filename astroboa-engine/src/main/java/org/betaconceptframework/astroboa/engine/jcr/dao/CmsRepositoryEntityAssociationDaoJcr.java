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

package org.betaconceptframework.astroboa.engine.jcr.dao;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.database.model.CmsRepositoryEntityAssociation;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
class CmsRepositoryEntityAssociationDaoJcr extends JcrDaoSupport implements CmsRepositoryEntityAssociationDao, InitializingBean, ApplicationListener {


	private static final String REFERENCED_ENTITY_ID = "referencedEntityIdKeyword";

	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	private TopicCriteria repositoryUserAssociatedWithTopicsCriteria;

	private SpaceCriteria repositoryUserAssociatedWithSpacesCriteria;

	private ContentObjectCriteria repositoryUserAssociatedWithContentObjectsCriteria;

	private ContentObjectCriteria contentObjectAssociatedWithContentObjectsCriteria;

	private List<String> emptyEntityIds = new ArrayList<String>();

	private List<CmsRepositoryEntityAssociation> emptyEntityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();

	private SpaceCriteria contentObjectAssociatedWithSpacesCriteria;
	
	/*
	 * TO BE REMOVED 
	 */

	
	public List<String> getReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedTaxonomyNodeOfTaxonomy(String taxonomy) {
		return new ArrayList<String>();
	}


	/*
	 * TO BE REFACTORED 
	 */
	
	
	public <T  extends CmsRepositoryEntity> List<CmsRepositoryEntityAssociation> getAllAssociationsOfReferencedEntity(String referencedEntityId, Class<T> entityType) {
		
		
		if (StringUtils.isBlank(referencedEntityId) || entityType == null){
			return emptyEntityAssociations;
		}
		
		if (entityType == Taxonomy.class){
			//Taxonomy does not participate in any relationship
			return emptyEntityAssociations;
		}
		else if (entityType == Space.class){
			//Space does not participate in any relationship
			return emptyEntityAssociations;
		}
		else if (entityType == RepositoryUser.class){
			//A RepositoryUser can be the owner of a TOPIC_INSTANCE, the owner of a SPACE_INSTANCE 
			//and the owner of a ContentObject
			List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
			
			entityAssociations.addAll(retrieveAssociationsOfRepositoryUserWithTopics(referencedEntityId));
			
			entityAssociations.addAll(retrieveAssociationsOfRepositoryUserWithSpaces(referencedEntityId));
			
			entityAssociations.addAll(retrieveAssociationsOfRepositoryUserWithContentObjects(referencedEntityId));
			
			return entityAssociations;
		}
		else if (entityType == Topic.class){
			//A Topic can be associated only with ContentObjects
			return retrieveAssociationsOfTopicWithContentObjects(referencedEntityId);
		}
		else if (entityType == ContentObject.class){
			//A ContentObject can be associated with ContentObjects and Spaces
			List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
			
			entityAssociations.addAll(retrieveAssociationsOfContentObjectWithContentObjects(referencedEntityId));
			
			entityAssociations.addAll(retrieveAssociationsOfContentObjectWithSpaces(referencedEntityId));
			
			return entityAssociations;
			
		}
		else {
			logger.warn("Entity type "+entityType + " is be associated with any other type");
			return emptyEntityAssociations;
		}

	}

	private List<CmsRepositoryEntityAssociation> retrieveAssociationsOfContentObjectWithContentObjects(
			String referencedEntityId) {
		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		/* NO QUERY FOR THE MOMENT. IT WILL BE ADDED LATER
		
		String query = contentObjectAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);

		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),query,0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					CmsRepositoryEntityAssociation contentObjectAssociation = new CmsRepositoryEntityAssociation();
					contentObjectAssociation.setReferencedCmsRepositoryEntityId(referencedEntityId);
					contentObjectAssociation.setReferrerCmsRepositoryEntityId(cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode));
					contentObjectAssociation.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName());
					
					entityAssociations.add(contentObjectAssociation);
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		*/
		
		
		
		return entityAssociations;
	}
	
	private List<String> retrieveEntityIdsOfContentObjectWithContentObjects(String referencedEntityId) {
		List<String> entityIds = new ArrayList<String>();
		
		
		/* NO QUERY FOR THE MOMENT. IT WILL BE ADDED LATER
		
		String query = contentObjectAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);

		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),query,0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					final String id = cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode);
					if (! entityIds.contains(id)){
						entityIds.add(id);
					}
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		*/
		
		
		
		return entityIds;
	}
	
	private Integer retrieveCountOfEntityIdsOfContentObjectWithContentObjects(String referencedEntityId) {
		
		/* NO QUERY FOR THE MOMENT. IT WILL BE ADDED LATER

		String query = contentObjectAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),query,0,0);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				return contentObjectNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		*/
		return 0;
	}

	private List<CmsRepositoryEntityAssociation>  retrieveAssociationsOfTopicWithContentObjects(String referencedEntityId) {

		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		ContentObjectCriteria topicAssociatedWithContentObjectsCriteria = createTopicAssociatedWithContentObjectsCriteria(referencedEntityId);
		
		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),topicAssociatedWithContentObjectsCriteria,0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					final String contentObjectId = cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode);

					//Iterate through all properties in order to find property which contains the referenced id.
					//For each property create a new association
					//TODO : provide a map with topic property paths per type, in order to
					//search more quickly
					entityAssociations.addAll(retrieveAllTopicAssociations(referencedEntityId, contentObjectId, contentObjectNode));
					
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityAssociations;
	}

	private List<CmsRepositoryEntityAssociation> retrieveAllTopicAssociations(String referencedEntityId, final String contentObjectId, Node node) throws RepositoryException {

		List<CmsRepositoryEntityAssociation> associations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		if (node.hasProperties()){
			
			String nodeBetacsmId = cmsRepositoryEntityUtils.getCmsIdentifier(node);
			
			PropertyIterator propertyIt = node.getProperties();
			
			while (propertyIt.hasNext()){
				
				Property property = propertyIt.nextProperty();
				
				if (property.getType() == PropertyType.STRING){
					
					boolean multiple  = property.getDefinition().isMultiple();
					
					if (multiple){
						
						Value[] values = property.getValues();
						for (Value value :values){
							if (StringUtils.equals(value.getString(), referencedEntityId)){
								associations.add(createCmsRepositoryEntityAssociation(referencedEntityId, contentObjectId,nodeBetacsmId, property.getName()));
								break;
							}
						}
					}
					else if (StringUtils.equals(property.getString(), referencedEntityId)){
						associations.add(createCmsRepositoryEntityAssociation(referencedEntityId, contentObjectId,nodeBetacsmId, property.getName()));
					}
				}
			}
		}
		
		if (node.hasNodes()){
			NodeIterator childNodeIt = node.getNodes();
			while(childNodeIt.hasNext()){
				associations.addAll(retrieveAllTopicAssociations(referencedEntityId, contentObjectId, childNodeIt.nextNode()));
			}
		}
		
		return associations;
	}

	private CmsRepositoryEntityAssociation createCmsRepositoryEntityAssociation(
			String referencedEntityId, final String referrerCmsPropertyEntityId,
			String referrerPropertyContainerId, String referrerPropertyName) throws RepositoryException {
		
		CmsRepositoryEntityAssociation association = new CmsRepositoryEntityAssociation();
		
		association.setReferrerCmsRepositoryEntityId(referrerCmsPropertyEntityId);
		association.setReferrerPropertyContainerId(referrerPropertyContainerId);
		association.setReferrerPropertyName(referrerPropertyName);
		association.setReferencedCmsRepositoryEntityId(referencedEntityId);
		
		return association;
	}
	
	private List<String>  retrieveEntityIdsOfTopicWithContentObjects(String referencedEntityId) {

		List<String> entityIds = new ArrayList<String>();
		
		ContentObjectCriteria topicAssociatedWithContentObjectsCriteria = createTopicAssociatedWithContentObjectsCriteria(referencedEntityId);

		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),topicAssociatedWithContentObjectsCriteria,0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					final String id = cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode);
					if (! entityIds.contains(id)){
						entityIds.add(id);
					}
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityIds;
	}

	private Integer  retrieveCountOfEntityIdsOfTopicWithContentObjects(String referencedEntityId) {

		ContentObjectCriteria topicAssociatedWithContentObjectsCriteria = createTopicAssociatedWithContentObjectsCriteria(referencedEntityId);
		
		try {
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(),topicAssociatedWithContentObjectsCriteria,0,0);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				return contentObjectNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return 0;
	}

	private ContentObjectCriteria createTopicAssociatedWithContentObjectsCriteria(String topicId) {
		
		ContentObjectCriteria topicAssociatedWithContentObjectsCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		topicAssociatedWithContentObjectsCriteria.doNotCacheResults();
		topicAssociatedWithContentObjectsCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		Criterion topicCriterion = CriterionFactory.newTopicReferenceCriterion(null, topicId, QueryOperator.EQUALS, false);
		topicAssociatedWithContentObjectsCriteria.addCriterion(topicCriterion);
		
		return topicAssociatedWithContentObjectsCriteria;

	}
	
	private void createContentObjectAssociatedWithContentObjectsCriteria() {
		
		contentObjectAssociatedWithContentObjectsCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectAssociatedWithContentObjectsCriteria.doNotCacheResults();
		contentObjectAssociatedWithContentObjectsCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		//Normally this should be a ContentObjectPropertyCriterion
		//Criterion contentObjectCriterion = CriterionFactory.newTopicPropertyCriterion(null, REFERENCED_ENTITY_ID, QueryOperator.EQUALS, false);
		//contentObjectAssociatedWithContentObjectsCriteria.addCriterion(contentObjectCriterion);

	}


   private void createRepositoryUserAssociatedWithContentObjectsCriteria() {
		
		repositoryUserAssociatedWithContentObjectsCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		repositoryUserAssociatedWithContentObjectsCriteria.doNotCacheResults();
		repositoryUserAssociatedWithContentObjectsCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		repositoryUserAssociatedWithContentObjectsCriteria.addOwnerIdEqualsCriterion(REFERENCED_ENTITY_ID);
	}


	private List<CmsRepositoryEntityAssociation> retrieveAssociationsOfRepositoryUserWithTopics(String referencedEntityId) {
		
		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		String query = repositoryUserAssociatedWithTopicsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			
			CmsQueryResult topicNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (topicNodes != null && topicNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = topicNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node topicNode = iterator.nextNode();
					
					CmsRepositoryEntityAssociation topicAssociation = new CmsRepositoryEntityAssociation();
					topicAssociation.setReferencedCmsRepositoryEntityId(referencedEntityId);
					topicAssociation.setReferrerCmsRepositoryEntityId(cmsRepositoryEntityUtils.getCmsIdentifier(topicNode));
					topicAssociation.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName());
					
					entityAssociations.add(topicAssociation);
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityAssociations;
	}
	
	private List<String> retrieveEntityIdsOfRepositoryUserWithTopics(String referencedEntityId) {
		
		List<String> entityIds = new ArrayList<String>();
		
		String query = repositoryUserAssociatedWithTopicsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			
			CmsQueryResult topicNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (topicNodes != null && topicNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = topicNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node topicNode = iterator.nextNode();
					
					String id = cmsRepositoryEntityUtils.getCmsIdentifier(topicNode);
					if (! entityIds.contains(id)){
						entityIds.add(id);
					}
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityIds;
	}
	
	private Integer retrieveCountOfEntityIdsOfRepositoryUserWithTopics(String referencedEntityId) {
		
		String query = repositoryUserAssociatedWithTopicsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			
			CmsQueryResult topicNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,0);
			
			if (topicNodes != null && topicNodes.getTotalRowCount() > 0){
				return topicNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return 0;
	}

	private void createRepositoryUserAssociatedWithTopicsCriteria() {
		
		repositoryUserAssociatedWithTopicsCriteria = CmsCriteriaFactory.newTopicCriteria();
		
		repositoryUserAssociatedWithTopicsCriteria.doNotCacheResults();
		repositoryUserAssociatedWithTopicsCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		repositoryUserAssociatedWithTopicsCriteria.addOwnerIdEqualsCriterion(REFERENCED_ENTITY_ID);
		
	}
	
	private void createRepositoryUserAssociatedWithSpacesCriteria() {
		
		repositoryUserAssociatedWithSpacesCriteria = CmsCriteriaFactory.newSpaceCriteria();
		
		repositoryUserAssociatedWithSpacesCriteria.doNotCacheResults();
		repositoryUserAssociatedWithSpacesCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		repositoryUserAssociatedWithSpacesCriteria.addOwnerIdEqualsCriterion(REFERENCED_ENTITY_ID);
		
	}
	
	private void createContentObjectAssociatedWithSpacessCriteria() {
		
		contentObjectAssociatedWithSpacesCriteria = CmsCriteriaFactory.newSpaceCriteria();
		
		contentObjectAssociatedWithSpacesCriteria.doNotCacheResults();
		contentObjectAssociatedWithSpacesCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectAssociatedWithSpacesCriteria.addCriterion(CriterionFactory.equals(CmsBuiltInItem.ContentObjectReferences.getJcrName(), REFERENCED_ENTITY_ID));
		
	}
	
	private List<CmsRepositoryEntityAssociation> retrieveAssociationsOfRepositoryUserWithSpaces(String referencedEntityId) {
		
		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		String query = repositoryUserAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = spaceNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node spaceNode = iterator.nextNode();
					
					CmsRepositoryEntityAssociation spaceAssociation = new CmsRepositoryEntityAssociation();
					spaceAssociation.setReferencedCmsRepositoryEntityId(referencedEntityId);
					spaceAssociation.setReferrerCmsRepositoryEntityId(cmsRepositoryEntityUtils.getCmsIdentifier(spaceNode));
					spaceAssociation.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName());
					
					entityAssociations.add(spaceAssociation);
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityAssociations;
	}
	
	private List<CmsRepositoryEntityAssociation> retrieveAssociationsOfContentObjectWithSpaces(String referencedEntityId) {
		
		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		String query = contentObjectAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = spaceNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node spaceNode = iterator.nextNode();
					
					CmsRepositoryEntityAssociation spaceAssociation = new CmsRepositoryEntityAssociation();
					spaceAssociation.setReferencedCmsRepositoryEntityId(referencedEntityId);
					spaceAssociation.setReferrerCmsRepositoryEntityId(cmsRepositoryEntityUtils.getCmsIdentifier(spaceNode));
					spaceAssociation.setReferrerPropertyName(CmsBuiltInItem.ContentObjectReferences.getJcrName());
					
					entityAssociations.add(spaceAssociation);
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityAssociations;
	}

	private List<String> retrieveEntityIdsOfRepositoryUserWithSpaces(String referencedEntityId) {
		
		List<String> entityIds = new ArrayList<String>();
		
		String query = repositoryUserAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = spaceNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node spaceNode = iterator.nextNode();
					
					final String id = cmsRepositoryEntityUtils.getCmsIdentifier(spaceNode);
					if (! entityIds.contains(id)){
						entityIds.add(id);
					}
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityIds;
	}

	private Integer retrieveCountOfEntityIdsOfRepositoryUserWithSpaces(String referencedEntityId) {
		
		String query = repositoryUserAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,0);
			
			if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
				return spaceNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return 0;
	}

	
	private List<CmsRepositoryEntityAssociation> retrieveAssociationsOfRepositoryUserWithContentObjects(String referencedEntityId) {
		
		List<CmsRepositoryEntityAssociation> entityAssociations = new ArrayList<CmsRepositoryEntityAssociation>();
		
		String query = repositoryUserAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		
		try {
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					CmsRepositoryEntityAssociation contentObjectAssociation = new CmsRepositoryEntityAssociation();
					contentObjectAssociation.setReferencedCmsRepositoryEntityId(referencedEntityId);
					contentObjectAssociation.setReferrerCmsRepositoryEntityId(cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode));
					contentObjectAssociation.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName());
					
					entityAssociations.add(contentObjectAssociation);
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityAssociations;
	}

	private List<String> retrieveEntityIdsOfRepositoryUserWithContentObjects(String referencedEntityId) {
		
		List<String> entityIds = new ArrayList<String>();
		
		String query = repositoryUserAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		
		try {
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				
				NodeIterator iterator = contentObjectNodes.getNodeIterator();
				
				while (iterator.hasNext()){
					Node contentObjectNode = iterator.nextNode();
					
					final String id = cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode);
					if (! entityIds.contains(id)){
						entityIds.add(id);
					}
				}
				
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return entityIds;
	}

	private Integer retrieveCountOfEntityIdsOfRepositoryUserWithContentObjects(String referencedEntityId) {
		
		String query = repositoryUserAssociatedWithContentObjectsCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		
		try {
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,0);
			
			if (contentObjectNodes != null && contentObjectNodes.getTotalRowCount() > 0){
				return contentObjectNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return 0;
	}

	public <T extends CmsRepositoryEntity> List<String> getReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(String referencedEntityId, Class<T> entityType) {
														 
		if (StringUtils.isBlank(referencedEntityId) || entityType == null){
			return emptyEntityIds;
		}
		
		if (entityType == Taxonomy.class){
			//Taxonomy does not participate in any relationship
			return emptyEntityIds;
		}
		else if (entityType == Space.class){
			//Space does not participate in any relationship
			return emptyEntityIds;
		}
		else if (entityType == RepositoryUser.class){
			//A RepositoryUser can be the owner of a TOPIC_INSTANCE, the owner of a SPACE_INSTANCE 
			//and the owner of a ContentObject
			List<String> entityIds = new ArrayList<String>();
			
			entityIds.addAll(retrieveEntityIdsOfRepositoryUserWithTopics(referencedEntityId));
			
			entityIds.addAll(retrieveEntityIdsOfRepositoryUserWithSpaces(referencedEntityId));
			
			entityIds.addAll(retrieveEntityIdsOfRepositoryUserWithContentObjects(referencedEntityId));
			
			return entityIds;
		}
		else if (entityType == Topic.class){
			//A Topic can be associated only with ContentObjects
			return retrieveEntityIdsOfTopicWithContentObjects(referencedEntityId);
		}
		else if (entityType == ContentObject.class){
			//A ContentObject can be associated with ContentObjects and Spaces
			List<String> entityIds = new ArrayList<String>();
			
			entityIds.addAll(retrieveEntityIdsOfContentObjectWithContentObjects(referencedEntityId));
			
			entityIds.addAll(retrieveEntityIdsOfContentObjectWithSpaces(referencedEntityId));
			
			return entityIds;

		}
		else {
			logger.warn("Entity type "+entityType + " is be associated with any other type");
			return emptyEntityIds;
		}


	}


	private List<String> retrieveEntityIdsOfContentObjectWithSpaces(String referencedEntityId) {
			
			List<String> entityIds = new ArrayList<String>();
			
			String query = contentObjectAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
			
			try {
				CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,-1);
				
				if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
					
					NodeIterator iterator = spaceNodes.getNodeIterator();
					
					while (iterator.hasNext()){
						Node spaceNode = iterator.nextNode();
						
						final String id = cmsRepositoryEntityUtils.getCmsIdentifier(spaceNode);
						if (!entityIds.contains(id)){
							entityIds.add(id);
						}
					}
					
				}
			} catch (RepositoryException e) {
				throw new CmsException(e);
			}
			
			return entityIds;
	}

	private Integer retrieveCountOfEntityIdsOfContentObjectWithSpaces(String referencedEntityId) {
		
		String query = contentObjectAssociatedWithSpacesCriteria.getXPathQuery().replaceAll(REFERENCED_ENTITY_ID, referencedEntityId);
		
		try {
			CmsQueryResult spaceNodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), query, 0,0);
			
			if (spaceNodes != null && spaceNodes.getTotalRowCount() > 0){
				return spaceNodes.getTotalRowCount();
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}
		
		return 0;
}

	public <T extends CmsRepositoryEntity> Integer getCountOfReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(String referencedEntityId, Class<T> entityType) {
		
		if (StringUtils.isBlank(referencedEntityId) || entityType == null){
			return 0;
		}
		
		if (entityType == Taxonomy.class){
			//Taxonomy does not participate in any relationship
			return 0;
		}
		else if (entityType == Space.class){
			//Space does not participate in any relationship
			return 0;
		}
		else if (entityType == RepositoryUser.class){
			//A RepositoryUser can be the owner of a TOPIC_INSTANCE, the owner of a SPACE_INSTANCE 
			//and the owner of a ContentObject
			Integer count = 0;
			
			count = count + retrieveCountOfEntityIdsOfRepositoryUserWithTopics(referencedEntityId);
			
			count = count + retrieveCountOfEntityIdsOfRepositoryUserWithSpaces(referencedEntityId);
			
			count = count + retrieveCountOfEntityIdsOfRepositoryUserWithContentObjects(referencedEntityId);
			
			return count;
		}
		else if (entityType == Topic.class){
			//A Topic can be associated only with ContentObjects
			return retrieveCountOfEntityIdsOfTopicWithContentObjects(referencedEntityId);
			
		}
		else if (entityType == ContentObject.class){
			//A ContentObject can be associated with ContentObjects and Spaces
			Integer count = 0;
			
			count = count + retrieveCountOfEntityIdsOfContentObjectWithContentObjects(referencedEntityId);
			
			count = count +retrieveCountOfEntityIdsOfContentObjectWithSpaces(referencedEntityId);

			return count;
		}
		else {
			logger.warn("Entity type "+entityType + " is be associated with any other type");
			return 0;
		}

	}


	@Override
	public void afterPropertiesSet() throws Exception {
		createAllNecessaryCriteria();
		
	}

	private void createAllNecessaryCriteria() {
		
		//createTopicAssociatedWithContentObjectsCriteria();
		
		createRepositoryUserAssociatedWithTopicsCriteria();
		
		createRepositoryUserAssociatedWithSpacesCriteria();
		
		createRepositoryUserAssociatedWithContentObjectsCriteria();
		
		createContentObjectAssociatedWithContentObjectsCriteria();
		
		createContentObjectAssociatedWithSpacessCriteria();
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		createAllNecessaryCriteria();
		
	}
}
