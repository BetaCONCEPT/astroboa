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
package org.betaconceptframework.astroboa.engine.jcr.renderer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class TopicRenderer extends AbstractRenderer {


	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;
	@Autowired
	private TaxonomyRenderer taxonomyRenderer;
	@Autowired
	private RepositoryUserRenderer repositoryUserRenderer;
	@Autowired
	private CmsLocalizationRenderer cmsLocalizationRenderer;
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;


	public Topic renderTopic(Node topicJcrNode, RenderProperties renderProperties, Session session, Topic topic, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{

		if (topicJcrNode == null)
			return null;

		//Get Type of TaxononmyTreeJcrNode

		if (! topicJcrNode.isNodeType(CmsBuiltInItem.Topic.getJcrName()))
			throw new CmsException("Try to render a " + topicJcrNode.getPrimaryNodeType().getName() + " to a Topic");

		if (renderProperties == null)
			//Default values
			renderProperties = getDefaultRenderProperties();

		//Render Id
		cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(topicJcrNode, topic);

		//AllowsReferrerContentObjects
		if (topicJcrNode.hasProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()))
				topic.setAllowsReferrerContentObjects(topicJcrNode.getProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()).getBoolean());

		//Name is not mandatory. Perform a check for existence
		if (topicJcrNode.hasProperty(CmsBuiltInItem.Name.getJcrName()))
			topic.setName(topicJcrNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());

		//Order
		if (topicJcrNode.hasProperty(CmsBuiltInItem.Order.getJcrName()))
			topic.setOrder(topicJcrNode.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong());

		boolean parentIsRendered = false;
		
		//Parent. 
		if (renderProperties.isParentEntityRendered()){
			renderParent(topicJcrNode.getParent(), topic,renderProperties, cachedCmsRepositoryEntities, session);
			parentIsRendered = true; 
		}
		else{
			//At least render its parent id so that in could be lazy rendered when needed
			if (topicJcrNode.getParent().isNodeType(CmsBuiltInItem.Topic.getJcrName())){
				String parentTopicId = cmsRepositoryEntityUtils.getCmsIdentifier(topicJcrNode.getParent());
				((TopicImpl)topic).setParentId(parentTopicId);
			}
		}

		//OwnerId
		renderOwner(topicJcrNode, session, topic, cachedCmsRepositoryEntities, renderProperties, parentIsRendered);

		//Locale
		renderLocalizedLabels(topicJcrNode, topic);

		//Taxonomy
		if (parentIsRendered && topic.getParent() != null && topic.getParent().getTaxonomy() != null){
			topic.setTaxonomy(topic.getParent().getTaxonomy());
		}
		else{
			taxonomyRenderer.renderTaxonomyToTopic(topicJcrNode, topic);
		}

//		Number of Children
		renderNumberOfChildren(topicJcrNode, topic);

		return topic;

	}


	private  void renderOwner(Node topicJcrNode,  Session session, Topic topic,
			Map<String, CmsRepositoryEntity> cachedRepositoryEntities, RenderProperties renderProperties, boolean parentIsRendered) throws RepositoryException, ValueFormatException, PathNotFoundException {

		//Render owner if it is not there
		if (topic.getOwner() == null){
			String topicOwnerId = topicJcrNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();

			if (parentIsRendered){
				Topic topicParent = topic.getParent();
				//		Check if owner is the same with its parent
				if (topicParent != null && topicParent.getOwner() != null){
					//If owner is the same with its parent, then there is no need to rerender parent
					if (topicOwnerId.equals(topicParent.getOwner().getId()))
						topic.setOwner(topicParent.getOwner());
				}
			}

			// Check if owner is cached
			if (topic.getOwner() == null){
				if (cachedRepositoryEntities.containsKey(topicOwnerId))
					topic.setOwner((RepositoryUser)cachedRepositoryEntities.get(topicOwnerId));
			}

			//If still no owner has been specified then render owner
			if (topic.getOwner() == null){
				topic.setOwner(repositoryUserRenderer.renderRepositoryUserNode(topicOwnerId, renderProperties, session, cachedRepositoryEntities));
			}

			//Finally update cache if necessary
			if (!cachedRepositoryEntities.containsKey(topicOwnerId)){
				cachedRepositoryEntities.put(topicOwnerId, topic.getOwner());
			}

		}
	}

	private RenderProperties getDefaultRenderProperties() {
		RenderProperties renderProperties = new RenderPropertiesImpl();

		renderProperties.renderParentEntity(true);
		
		return renderProperties;
	}


	private   void renderNumberOfChildren(Node topicJcrNode, Topic topic) throws  RepositoryException{

		int numberOfChildren = 0;

		if (topicJcrNode.hasNode(CmsBuiltInItem.Topic.getJcrName()))
			numberOfChildren = (int)topicJcrNode.getNodes(CmsBuiltInItem.Topic.getJcrName()).getSize();

		topic.setNumberOfChildren(numberOfChildren);

	}


	private  void renderLocalizedLabels(Node topicJcrNode, Topic topic) throws RepositoryException {

		cmsLocalizationRenderer.renderCmsLocalization(topicJcrNode, topic);


	}


	private  void renderParent(Node parentTopicJcrNode, Topic topic, RenderProperties renderProperties, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session) throws  AccessDeniedException, RepositoryException  {

		if (parentTopicJcrNode.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
//			Check if parent Topic has already been rendered
			String parentTopicId = cmsRepositoryEntityUtils.getCmsIdentifier(parentTopicJcrNode);

			if (cachedCmsRepositoryEntities.containsKey(parentTopicId))
				topic.setParent((Topic)cachedCmsRepositoryEntities.get(parentTopicId));
			else
			{
				topic.setParent(cmsRepositoryEntityFactoryForActiveClient.newTopic());

				boolean renderParentIsEnabled = (renderProperties != null && 
						renderProperties.isParentEntityRendered());

				if (renderParentIsEnabled){
					//Disable render its parent
					renderProperties.renderParentEntity(false);
				}
				
				renderTopic(parentTopicJcrNode, renderProperties, session, topic.getParent() , cachedCmsRepositoryEntities);
				
				if (renderParentIsEnabled){
					//Re enable render parent flag
					renderProperties.renderParentEntity(true);
				}
				
				cachedCmsRepositoryEntities.put(parentTopicId, topic.getParent());
			}
		}
	}

	public  Topic renderTopicWithoutItsParent(Node topicJcrNode, RenderProperties renderProperties, Session session, 
			Topic newSubTopic, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{
		if (renderProperties == null)
			renderProperties = getDefaultRenderProperties();

		renderProperties.renderParentEntity(false);

		return renderTopic(topicJcrNode, renderProperties, session, newSubTopic, cachedCmsRepositoryEntities);
	}

	public   Topic renderTopicAndItsParent(Node topicJcrNode, RenderProperties renderProperties, Session session,
			Topic newSubTopic, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{
		if (renderProperties == null)
			renderProperties = getDefaultRenderProperties();

		renderProperties.renderParentEntity(true);

		return renderTopic(topicJcrNode, renderProperties, session, newSubTopic, cachedCmsRepositoryEntities);
	}

	public  Topic renderNode(Session session, Node node, RenderProperties renderProperties, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws RepositoryException {

			if (node == null){
				throw new CmsException("Found null topic node to render");
			}
			
			//Be careful row may be an node named localization and not a topic
			//TODO Probably this code will be deprecated
			if (node.getName().equals(CmsBuiltInItem.Localization.getJcrName()))
				node = node.getParent();
			
			if (! node.isNodeType(CmsBuiltInItem.Topic.getJcrName()))
				throw new CmsException("Unable to render node type "+ node.getPrimaryNodeType().getName());

		return renderTopicAndItsParent(node, renderProperties, session,	cmsRepositoryEntityFactoryForActiveClient.newTopic(), cachedRepositoryUsers);
	}

	public  List<Topic> render(NodeIterator topicNodes, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws  RepositoryException{
		if (topicNodes == null)
			throw new CmsException("Null topic node list. Could not render");

		List<Topic> topics = new ArrayList<Topic>();

		while (topicNodes.hasNext())
			topics.add(renderNode(session, topicNodes.nextNode(), renderProperties, cachedRepositoryUsers));

		return topics;
	}


	public  Topic renderTopic(String topicId, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws RepositoryException {

		Node topicNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, topicId);
		
		return renderNode(session, topicNode, renderProperties, cachedRepositoryUsers);
	}


	






	
}
