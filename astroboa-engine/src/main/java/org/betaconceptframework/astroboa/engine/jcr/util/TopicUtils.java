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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.database.model.CmsRepositoryEntityAssociation;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDefinitionDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TaxonomyDao;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class TopicUtils {

	final Logger logger = LoggerFactory.getLogger(TopicUtils.class);

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private CmsLocalizationUtils cmsLocalizationUtils;

	@Autowired 
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;

	@Autowired
	private TaxonomyDao taxonomyDao;
	
	@Autowired
	private ContentDefinitionDao contentDefinitionDao;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	@Autowired
	private RepositoryUserDao  repositoryUserDao;

	public  Node addNewTopicJcrNode(Node parentTopicJcrNode, Topic topic, Session session, boolean useProvidedId, Context context) throws RepositoryException {

		Node topicJcrNode = JcrNodeUtils.addTopicNode(parentTopicJcrNode, CmsBuiltInItem.Topic.getJcrName());

		cmsRepositoryEntityUtils.createCmsIdentifier(topicJcrNode, topic, useProvidedId);

		return populateTopicJcrNode(topic, session, topicJcrNode,useProvidedId, context);
	}


	private Node populateTopicJcrNode(Topic topic, Session session, Node topicJcrNode, boolean useProvidedId, Context context) throws RepositoryException {

		updateSystemBuiltin(topic, topicJcrNode);

		//Update OwnerId
		updateOwner(topic, topicJcrNode, session, context);

		//Update Localized Labels
		updateLocalizedLabels(topic, topicJcrNode);

		//Update order
		updateOrder(topic, topicJcrNode);

//		Update 
		updateName(session, topic, topicJcrNode, context);

//		Update ContentObjectReferenceable
		updateAllowsReferrerContentObject(topic, topicJcrNode);

		//		Save or update children
		saveOrUpdateChildren(topic, session, topicJcrNode, context);

		return topicJcrNode;
	}


	private void updateSystemBuiltin(Topic topic, Node topicJcrNode)
			throws RepositoryException {
		cmsRepositoryEntityUtils.setSystemProperties(topicJcrNode, topic);
	}

	private  void saveOrUpdateChildren(Topic topic, Session session, Node topicJcrNode, Context context) throws RepositoryException {

		if (topic.isChildrenLoaded()){
			List<Topic> children = topic.getChildren();

			//Add topic node to cache
			if (topicJcrNode != null){
				context.cacheTopicNode(topicJcrNode, true);
			}

			//Now insert new subTaxonomyNode
			if (CollectionUtils.isNotEmpty(children)){
				for (Topic child: children){

					((TopicImpl)child).detectCycle(null);
					
					Node childNode = null;
					if (child.getId() != null){
						childNode = updateTopic(session, child, topicJcrNode, context);
					}
					else{
						childNode = addNewTopicJcrNode(topicJcrNode, child, session, false, context);
					}
					
					if (childNode != null){
						context.cacheTopicNode(childNode, true);
					}
					
				}
			}
		}
	}

	private  void updateAllowsReferrerContentObject(Topic topic, Node topicJcrNode) throws  RepositoryException {

		if ( ((TopicImpl)topic).allowsReferrerContentObjectsHasBeenSet() || ! topicJcrNode.hasProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName())){
			topicJcrNode.setProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName(),	topic.isAllowsReferrerContentObjects());
		}
		else{
			topic.setAllowsReferrerContentObjects(topicJcrNode.getProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()).getBoolean());
		}
	}

	private  void updateName(Session session, Topic topic, Node topicJcrNode, Context context) throws     RepositoryException  {
		
		if (StringUtils.isBlank(topic.getName())){
			//User has provided no name.
			
			if (topicJcrNode.hasProperty(CmsBuiltInItem.Name.getJcrName())){
				//Topic already has a name. Update entity and return. There is no need to check for a duplicate
				topic.setName(topicJcrNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
				return;
			}
				
			//Topic does not have a name. Search for an english localized label
			String possibleSystemName = null;
			
			if (topic.hasLocalizedLabels()){
				
				possibleSystemName = topic.getLocalizedLabelForLocale(Locale.ENGLISH.toString());
				
				if (StringUtils.isBlank(possibleSystemName)){
					
					//Get the first valid localized label
					for (String label : topic.getLocalizedLabels().values()){
						possibleSystemName = cmsRepositoryEntityUtils.fixSystemName(label);
						
						if (StringUtils.isNotBlank(possibleSystemName)){
							break;
						}
					}
				}
				else{
					possibleSystemName = cmsRepositoryEntityUtils.fixSystemName(possibleSystemName);
				}
			}
			
			if (StringUtils.isNotBlank(possibleSystemName)){
				topic.setName(possibleSystemName);
			}
		}
		
		if (topic.getName() != null){
			
			if (topicJcrNode.hasProperty(CmsBuiltInItem.Name.getJcrName())){
				//Topic already has a name. If name has not changed, return
				if (StringUtils.equals(topicJcrNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString(), topic.getName())){
					return;
				}
			}

			if (!cmsRepositoryEntityUtils.isValidSystemName(topic.getName())){
				throw new RepositoryException("Topic name '"+topic.getName()+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP);
			}
			
			// Make sure that no other topic exists with same system name
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addNameEqualsCriterion(topic.getName());
			
			if (topic.getId() != null) {
				topicCriteria.addIdNotEqualsCriterion(topic.getId());
			}
			
			//Normally it should not be more than one
			topicCriteria.setOffsetAndLimit(0, 2);
			
			CmsQueryResult sameNameTopicsInTaxonomy = cmsQueryHandler.getNodesFromXPathQuery(session, topicCriteria);
					
			if (sameNameTopicsInTaxonomy.getTotalRowCount() != 0) {
				
				//Retrieve taxonomy path
				StringBuilder topicPaths = new StringBuilder();
				NodeIterator nodeIterator = sameNameTopicsInTaxonomy.getNodeIterator();
				while (nodeIterator.hasNext()){
					topicPaths.append("\n");
					topicPaths.append(nodeIterator.nextNode().getPath());
				}
				
				throw new RepositoryException("Topic name '" + topic.getName() + "' is used by the following topic :"+ topicPaths.toString());
			}
			else{
				//Query is conducted in persistent data and not in nodes which exist in the session
				//In this case, look if another topic jcr node with the same name in the context with the same name
				Node cachedTopicNode = context.getNodeFromCache(topic.getName());
				
				if (cachedTopicNode != null && ! cachedTopicNode.isSame(topicJcrNode)){
					throw new RepositoryException("Topic name '" + topic.getName() + "' already exists. Probably you are importing many " +
							" topics at once and you have provided more than one topic with name "+ topic.getName());
				}
			}
						
			topicJcrNode.setProperty(CmsBuiltInItem.Name.getJcrName(), topic.getName());
		}
	}

	private  void updateOrder(Topic topic, Node topicJcrNode) throws  RepositoryException  {
		if (topic.getOrder() != null)
			topicJcrNode.setProperty(CmsBuiltInItem.Order.getJcrName(), topic.getOrder());
		else
			topicJcrNode.setProperty(CmsBuiltInItem.Order.getJcrName(), JcrValueUtils.getJcrNull());
	}

	public void updateLocalizedLabels(Localization localization, Node topicJcrNode) throws RepositoryException {
		cmsLocalizationUtils.updateCmsLocalization(localization, topicJcrNode);
	}

	public void updateOwner(Topic topic, Node topicJcrNode, Session session, Context context) throws RepositoryException {

		//Topic owner is always System user or a Repository User if topic is under user's folksonomy
		RepositoryUser topicOwner = retrieveOwnerForTopic(topic, topicJcrNode);

		if (topicOwner == null){
			setSystemUserAsTopicOwner(topic, repositoryUserDao.getSystemRepositoryUser());
		}
		else{
			//Set or replace owner. We ignore the owner provided by the user.
			if (topic.getOwner() == null){
				topic.setOwner(topicOwner);
			}
			else if (! usersAreTheSame(topic.getOwner(), topicOwner)){
				logger.info("Topic {}'s owner will be replaced with repository user {}", topic.toString(), topicOwner);
				topic.setOwner(topicOwner);
			}
		}
		
		//Update owner id only if existing owner id is not the same
		String newOwnerId = topicOwner.getId();

		if (!topicJcrNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()) ||
				!topicJcrNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString().equals(newOwnerId)){
			
			EntityAssociationUpdateHelper<RepositoryUser> repositoryUserAssociationUpdateHelper = 
				new EntityAssociationUpdateHelper<RepositoryUser>(session,cmsRepositoryEntityAssociationDao, context);

			repositoryUserAssociationUpdateHelper.setReferrerCmsRepositoryEntityNode(topicJcrNode);
			repositoryUserAssociationUpdateHelper.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier);
			repositoryUserAssociationUpdateHelper.setValuesToBeAdded(Arrays.asList(topicOwner));
			repositoryUserAssociationUpdateHelper.update();
		}
	}


	private RepositoryUser retrieveOwnerForTopic(Topic topic, Node topicJcrNode)
			throws ItemNotFoundException, AccessDeniedException,
			RepositoryException, ValueFormatException, PathNotFoundException {
		
		//Locate the taxonomy of the topic. If the taxonomy is a folksonomy then set the owner to be the
		//repository user of this folksonomy, otherwise set the owner to be the system user
		Node taxonomyNode = topicJcrNode.getParent();
		
		while (taxonomyNode != null){

			if (StringUtils.equals(CmsBuiltInItem.Folksonomy.getJcrName(), taxonomyNode.getName())){
				//Topic belongs to a repository user's folksonomy
				Node repositoryUserNode = taxonomyNode.getParent();
				
				if (repositoryUserNode == null){
					throw new CmsException("Topic "+topic.toString()+ " corresponds to jcr node "+topicJcrNode.getPath() + " which belongs to a folksonomy "+
							taxonomyNode.getPath() + " but cannot retrieve parent node");
				}
				
				if (repositoryUserNode.hasProperty(CmsBuiltInItem.ExternalId.getJcrName())){
					RepositoryUser topicOwner = repositoryUserDao.getRepositoryUser(repositoryUserNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString());

					if (topicOwner == null){
						throw new CmsException("Topic "+topic.toString()+ " corresponds to jcr node "+topicJcrNode.getPath() + " which belongs to a folksonomy "+
								taxonomyNode.getPath() + " of the user "+ repositoryUserNode.getPath()+ " but could not create a RepositoryUser instance with externalId "+
								repositoryUserNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString());
					}
					
					return topicOwner;
				}
				else{
					throw new CmsException("Topic "+topic.toString()+ " corresponds to jcr node "+topicJcrNode.getPath() + " which belongs to a folksonomy "+
							taxonomyNode.getPath() + " of the user "+ repositoryUserNode.getPath()+ " who does not have an externalId");
				}
			}
			else if (taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
				return repositoryUserDao.getSystemRepositoryUser();
			}			
			
			taxonomyNode = taxonomyNode.getParent();
		}
		
		return null;
	}

	public  Node updateTopic(Session session, Topic topic, Node parentTopicJcrNode, Context context) throws RepositoryException {

		Node topicJcrNode = context.retrieveNodeForTopic(topic.getId());

		if (topicJcrNode == null){
			if (topic.getId() != null){
				//User has specified an id for TaxonomyNode. Create a new one
				if(parentTopicJcrNode == null){
					parentTopicJcrNode = retrieveParentTopicNode(session, topic, context);
				}

				return addNewTopicJcrNode(parentTopicJcrNode, topic, session, true, context);
			}
			else
				throw new CmsException("Found no topic with id "+topic.getId());
		}
		
		updateSystemBuiltin(topic, topicJcrNode);

		updateOwner(topic, topicJcrNode, session,context);

		updateLocalizedLabels(topic, topicJcrNode);

		updateName(session, topic, topicJcrNode, context);

		
		//Retrieve taxonomy name
		Node currentTaxonomyNode = JcrNodeUtils.getTaxonomyJcrNode(topicJcrNode, true);
		String currentTaxonomyName = currentTaxonomyNode.getName();
		
		if (topic.getParent() != null){
			updateTopicParent(currentTaxonomyName, topic, topicJcrNode, session, context);
		}
		else{
			//Topic has become a root Node.
			
			if (topic.getTaxonomy() == null || ( StringUtils.isBlank(topic.getTaxonomy().getId()) && StringUtils.isBlank(topic.getTaxonomy().getName()))){

				//User provided no taxonomy info. Thus simply check that topic jcr node is under a taxonomy node
				if (! currentTaxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
					throw new CmsException("Topic "+topic.getId()+ ":" + topic.getName() + " cannot be saved under node "+topicJcrNode.getParent().getPath()+ " because this node does not correspond to a taxonomy");
				}
				
				//At this point topic has not changed its taxonomy, therefore there is no need
				//to search all objects which relate to this topic if their property accepts 
				//topic's taxonomy
				//checkThatReferrerContentObjectsAcceptNewTaxonomy(currentTaxonomyName, topic, session ,currentTaxonomyName);
			}
			else{

				//Get taxonomy Node	
				Node taxonomyNode = retrieveTaxonomyJcrNodeForTopic(session, topic);

				if (taxonomyNode == null){
					throw new CmsException("Unable to locate JCR node for taxonomy "+ topic.getTaxonomy().getName());
				}

				//Since topic is a root topic then its parent must be a taxonomy node
				if (!topicJcrNode.getParent().getIdentifier().equals(taxonomyNode.getIdentifier())){
					checkThatReferrerContentObjectsAcceptNewTaxonomy(currentTaxonomyName, topic, session ,topic.getTaxonomy().getName());
					session.move(topicJcrNode.getPath(), taxonomyNode.getPath()+CmsConstants.FORWARD_SLASH+topicJcrNode.getName());
				}
			}

		}
		

		updateOrder(topic, topicJcrNode);

		updateAllowsReferrerContentObject(topic, topicJcrNode);

		return topicJcrNode;
	}

	private void checkThatReferrerContentObjectsAcceptNewTaxonomy(
			String currentTaxonomyName, Topic topic, Session session, String newTaxonomyName) throws RepositoryException {
		
			//Get all content object associations to this topic
			List<CmsRepositoryEntityAssociation> contentObjectAssociationsToTopic = cmsRepositoryEntityAssociationDao.getAllAssociationsOfReferencedEntity(topic.getId(), Topic.class);
			
			Map<String, List<String>> topicPropertyPathsPerTaxonomies = contentDefinitionDao.getTopicPropertyPathsPerTaxonomies();
			
			
			if (CollectionUtils.isNotEmpty(contentObjectAssociationsToTopic)){
				
				for (CmsRepositoryEntityAssociation contentObjectAssociationToTopic : contentObjectAssociationsToTopic){
					Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectAssociationToTopic.getReferrerCmsRepositoryEntityId());
					
					if (contentObjectNode == null){
						throw new CmsException("Found no content object node for "+contentObjectAssociationToTopic.getReferrerCmsRepositoryEntityId());
					}
					
					if (!contentObjectNode.hasProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName())){
						throw new CmsException("Content object node for "+contentObjectNode.getPath() + " contains no content object type");
					}
					
					String contentObjectTypeName = contentObjectNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString();
					
					List<String> topicPropertyPathsForCurrentTaxonomy = topicPropertyPathsPerTaxonomies.get(currentTaxonomyName);
					
					String propertyPath = null;
					if (topicPropertyPathsForCurrentTaxonomy != null){
						for (String topicPropertyPath : topicPropertyPathsForCurrentTaxonomy){
							if (topicPropertyPath.endsWith(contentObjectAssociationToTopic.getReferrerPropertyName())){
								propertyPath = topicPropertyPath;
								break;
							}
						}
					}
					
					if (propertyPath == null){
						topicPropertyPathsForCurrentTaxonomy = topicPropertyPathsPerTaxonomies.get(CmsConstants.ANY_TAXONOMY);
						if (topicPropertyPathsForCurrentTaxonomy != null){
							for (String topicPropertyPath : topicPropertyPathsForCurrentTaxonomy){
								if (topicPropertyPath.endsWith(contentObjectAssociationToTopic.getReferrerPropertyName())){
									propertyPath = topicPropertyPath;
									break;
								}
							}
						}
					}
					
					if (propertyPath == null){
						throw new CmsException("Found no topic property path for property "+ contentObjectAssociationToTopic.getReferrerPropertyName());
					}
					
					CmsPropertyDefinition topicPropertyDefinition = null;
					try {
						topicPropertyDefinition = contentDefinitionDao.getCmsPropertyDefinition(propertyPath, contentObjectTypeName);
					} catch (Exception e) {
						throw new CmsException();
					}
					
					if (topicPropertyDefinition == null){
						throw new CmsException("Found no topic property definition for "+PropertyPath.createFullPropertyPath(contentObjectTypeName, propertyPath));
					}
					
					if (topicPropertyDefinition.getValueType() != ValueType.TopicReference){
						throw new CmsException("Property "+PropertyPath.createFullPropertyPath(contentObjectTypeName, propertyPath)+ " does not correspond to a topic definition "+ topicPropertyDefinition.getFullPath());
					}
					
					final List<String> acceptedTaxonomies = ((TopicReferencePropertyDefinition)topicPropertyDefinition).getAcceptedTaxonomies();
					
					if (CollectionUtils.isNotEmpty(acceptedTaxonomies) && ! acceptedTaxonomies.contains(newTaxonomyName)){
						throw new CmsException("Cannot move topic "+ topic.getName() + " to taxonomy "+ newTaxonomyName + " because topic is referenced by content object "+ contentObjectAssociationToTopic.getReferrerCmsRepositoryEntityId() +
								" whose topic property "+ topicPropertyDefinition.getFullPath() + " does not accept taxonomy "+ newTaxonomyName);
					}
					
				}
				
			}
			
			if (topic.getNumberOfChildren() > 0){
				final List<Topic> children = topic.getChildren();
				for (Topic child : children){
					checkThatReferrerContentObjectsAcceptNewTaxonomy(currentTaxonomyName, child, session, newTaxonomyName);
				}
			}
	}


	private void updateTopicParent(String currentTaxonomyName, Topic topic, Node topicJcrNode, Session session, Context context) throws  RepositoryException {
		if (topic == null || topic.getParent() ==null){
			throw new CmsException("No parent topic provided.");
		}

		Topic parentTopic = topic.getParent();
		
		Node newParentTopicJcrNode = context.retrieveNodeForTopic(parentTopic.getId());

		if (newParentTopicJcrNode == null){
			
			//check cache with parent's name
			newParentTopicJcrNode = context.retrieveNodeForTopic(parentTopic.getName());

			if (newParentTopicJcrNode == null){
				throw new CmsException("Unable to retrieve jcr node for parent topic "+parentTopic.toString()+" of topic "+topic.toString()+". Topic jcr node "+topicJcrNode.getPath());
			}
		}

		//New Parent's Primary Type can only be Topic or a Taxonomy 
		if (!newParentTopicJcrNode.isNodeType(topicJcrNode.getPrimaryNodeType().getName())){
			if (! newParentTopicJcrNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
				throw new CmsException("Parent topic can only be a topic or a taxonomy");
		}

		//Change Parent only if current parent is not the same
		if (!topicJcrNode.getParent().getIdentifier().equals(newParentTopicJcrNode.getIdentifier())){
			Node parentTaxonomyNode = JcrNodeUtils.getTaxonomyJcrNode(newParentTopicJcrNode, true);
			
			String parentTaxonomyName = parentTaxonomyNode.getName();
			
			session.move(topicJcrNode.getPath(), newParentTopicJcrNode.getPath()+CmsConstants.FORWARD_SLASH+topicJcrNode.getName());

			if (!StringUtils.equals(currentTaxonomyName, parentTaxonomyName)){
				checkThatReferrerContentObjectsAcceptNewTaxonomy(currentTaxonomyName, topic, session, parentTaxonomyName);
			}
		}
	}

	public void removeTopicJcrNode(Node topicJcrNode, Session session, boolean removeJcrNode, Context context) throws RepositoryException{
		EntityAssociationDeleteHelper<Topic> entityAssociationDeleteHelper = 
			new EntityAssociationDeleteHelper<Topic>(session,cmsRepositoryEntityAssociationDao, context);


		removeTopicFromAssociations(entityAssociationDeleteHelper, topicJcrNode);

		NodeIterator children = null;
		if (topicJcrNode.isNodeType(CmsBuiltInItem.Topic.getJcrName()))
			children = topicJcrNode.getNodes(CmsBuiltInItem.Topic.getJcrName());

		if (children != null){
			while (children.hasNext()){
				removeTopicJcrNode(children.nextNode(), session, false, context); //Do not remove child jcr node as its parent will be removed
			}
		}

		if (removeJcrNode){
			topicJcrNode.remove();
		}
	}

	private void removeTopicFromAssociations(EntityAssociationDeleteHelper<Topic> entityAssociationDeleteHelper, Node topicJcrNode)
	throws RepositoryException {
		entityAssociationDeleteHelper.setCmsRepositoryEntityIdToBeRemoved(cmsRepositoryEntityUtils.getCmsIdentifier(topicJcrNode));
		entityAssociationDeleteHelper.removeOrReplaceAllReferences(Topic.class);


	}

	/**
	 * retrieve topic's parent. If no id is specified, taxonomy node is returned instead
	 * @param session
	 * @param parent
	 * @return
	 * @throws CMSDaoException 
	 * @throws Exception 
	 */
	public  Node retrieveParentTopicNode(Session session, Topic topic, Context context) throws RepositoryException {

		Node parentTopicNode = null;
		
		Topic parentTopic = topic.getParent();
		
		if (parentTopic != null){

			
			if (parentTopic.getId()!=null){
				parentTopicNode = context.retrieveNodeForTopic(parentTopic.getId());
			}
			
			if (parentTopicNode==null && parentTopic.getName()!=null){
				parentTopicNode = context.retrieveNodeForTopic(parentTopic.getName());
			}
			
			if (parentTopicNode == null){
				throw new CmsException("Could not find jcr node for topic "+parentTopic.toString());
			}
		}

		if (parentTopicNode != null){
			return parentTopicNode;
		}
		else{
			return retrieveTaxonomyJcrNodeForTopic(session, topic);
		}
	}


	private Node retrieveTaxonomyJcrNodeForTopic(Session session, Topic topic)
			throws RepositoryException, PathNotFoundException {
		//Check taxonomy
		String taxonomyName = (topic.getTaxonomy() == null)? null : topic.getTaxonomy().getName();

		//Parent Topic is null. Topic is a root Topic. Search for its Taxonomy
		if (StringUtils.isBlank(taxonomyName)){
			//Check if id is provided
			String taxonomyId = (topic.getTaxonomy() == null)? null : topic.getTaxonomy().getId();

			if (StringUtils.isNotBlank(taxonomyId)){
				//Look for taxonomy node using its id
				try{
					return cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntityId(session, taxonomyId);
				}catch(Exception e){
					logger.error("",e);
					throw new CmsException("No taxonomy is provided and no parent is defined");
				}
			}

			throw new CmsException("No taxonomy is provided and no parent is defined");
		}

		if (taxonomyName.equals(CmsBuiltInItem.Folksonomy.getJcrName()))
		{
			// Topic's Taxonomy is Topic Owner's Folksonomy
			if (topic.getOwner() == null || StringUtils.isBlank(topic.getOwner().getId()))
				throw new CmsException("Topic Owner has not been preprocessed. No id found ");

			RepositoryUser topicOwner = topic.getOwner();

			// Get repository user node
			// TODO Test this case as it is possible that for a new repository user
			// the following method should return null since query does not take into account
			// uncommitted changes made so far in session
			Node repositoryUserNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, topicOwner.getId());

			if (repositoryUserNode == null)
				throw new CmsException("Repository User "+ topic.getOwner().toString() + " does not exist in repository");

			return repositoryUserNode.getNode(CmsBuiltInItem.Folksonomy.getJcrName());

		}
		else 
			//Retrieve Taxonomy Jcr Node
			return taxonomyDao.getTaxonomyJcrNode(session, taxonomyName, true);
	}

	public void setSystemUserAsTopicOwner(Topic topic, RepositoryUser systemUser) {
		if (topic != null){
			
			//Set system user as topic owner if topic does not have any user
			//or it happens to have another user which is not permitted
			if (! usersAreTheSame(topic.getOwner(), systemUser)){
				
				if (!StringUtils.equals(systemUser.getExternalId(), CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID)){
					throw new CmsException("Cannot set topic owner repository User "+systemUser.getExternalId());
				}
				
				topic.setOwner(systemUser);
			}

			if (topic.isChildrenLoaded()){
				List<Topic> childTopics = topic.getChildren();
				
				if (CollectionUtils.isNotEmpty(childTopics)){
					for (Topic child : childTopics){
						RepositoryUser childOwner = child.getOwner();
						
						//One of them is null therefore change child's taxonomy 
						if ( childOwner == null){
							setSystemUserAsTopicOwner(child, systemUser);
						}
						else{
							//Child does have an owner
							
							//Proceed only if they are not the same
							if (childOwner != systemUser || ! StringUtils.equals(childOwner.getId(), systemUser.getId())){
								setSystemUserAsTopicOwner(child, systemUser);
							}
						}
					}
				}	
			}
		}
	}

	
	private boolean usersAreTheSame(RepositoryUser user1 , RepositoryUser user2){
		
		if (user1 == null || user2 == null){
			return false;
		}
		
		if (user1 == user2){
			return true;
		}
		
		if (user1.getId() != null && StringUtils.equals(user1.getId(), user2.getId())){
			return true;
		}
		
		if (user1.getExternalId() != null && StringUtils.equals(user1.getExternalId(), user2.getExternalId())){
			return true;
		}
		
		
		return false;
	}
}
