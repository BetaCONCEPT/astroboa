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
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class SpaceRenderer extends AbstractRenderer{


	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;
	@Autowired
	private RepositoryUserRenderer repositoryUserRenderer;
	@Autowired
	private CmsLocalizationRenderer cmsLocalizationRenderer;
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;


	public Space renderSpace(Node spaceJcrNode, RenderProperties renderProperties, Session session, Space space, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{

		if (spaceJcrNode == null)
			return null;

		//Get Type of TaxononmyTreeJcrNode

		if (! spaceJcrNode.isNodeType(CmsBuiltInItem.Space.getJcrName()))
			throw new CmsException("Try to render a " + spaceJcrNode.getPrimaryNodeType().getName() + " to a Space");

		if (renderProperties == null)
			//Default values
			renderProperties = getDefaultRenderProperties();

		String locale = (String) renderProperties.getFirstLocaleUsedForRender();

		//Render Id
		cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(spaceJcrNode, space);

		//Name is not mandatory. Perform a check for existence
		if (spaceJcrNode.hasProperty(CmsBuiltInItem.Name.getJcrName()))
			space.setName(spaceJcrNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());

		//Order
		if (spaceJcrNode.hasProperty(CmsBuiltInItem.Order.getJcrName()))
			space.setOrder(spaceJcrNode.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong());

		//Parent. 
		if (renderProperties.isParentEntityRendered()){
			renderParent(spaceJcrNode.getParent(), space,renderProperties, cachedCmsRepositoryEntities, session);
			}

		//OwnerId
		renderOwner(spaceJcrNode, session, space, cachedCmsRepositoryEntities, renderProperties);

		//Locale
		renderLocalizedLabels(spaceJcrNode, space, locale);

		//Content Object References only for space
		renderContentObjectReferences(spaceJcrNode, space);

//		Number of Children
		renderNumberOfChildren(spaceJcrNode, space);

		return space;

	}


	private  void renderOwner(Node spaceJcrNode,  Session session, Space space,
			Map<String, CmsRepositoryEntity> cachedRepositoryEntities, RenderProperties renderProperties) throws RepositoryException, ValueFormatException, PathNotFoundException {

		//Render owner if it is not there
		if (space.getOwner() == null)
		{
			String spaceOwnerId = spaceJcrNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();

			Space spaceParent = space.getParent();
			//		Check if owner is the same with its parent
			if (spaceParent != null && spaceParent.getOwner() != null)
			{
				//If owner is the same with its parent, then there is no need to rerender parent
				if (spaceOwnerId.equals(spaceParent.getOwner().getId()))
					space.setOwner(spaceParent.getOwner());
			}

			// Check if owner is cached
			if (space.getOwner() == null)
			{
				if (cachedRepositoryEntities.containsKey(spaceOwnerId))
					space.setOwner((RepositoryUser)cachedRepositoryEntities.get(spaceOwnerId));
			}

			//If still no owner has been specified then render owner
			if (space.getOwner() == null)
				space.setOwner(repositoryUserRenderer.renderRepositoryUserNode(spaceOwnerId, renderProperties, session, cachedRepositoryEntities));

			//Finally update cache if necessary
			if (!cachedRepositoryEntities.containsKey(spaceOwnerId))
				cachedRepositoryEntities.put(spaceOwnerId, space.getOwner());

		}
	}

	private RenderProperties getDefaultRenderProperties() {
		RenderProperties renderProperties = new RenderPropertiesImpl();

		renderProperties.renderParentEntity(true);
		
		return renderProperties;
	}


	private   void renderNumberOfChildren(Node spaceJcrNode, Space space) throws  RepositoryException{

		int numberOfChildren = 0;

		if (spaceJcrNode.hasNode(CmsBuiltInItem.Space.getJcrName()))
			numberOfChildren = (int)spaceJcrNode.getNodes(CmsBuiltInItem.Space.getJcrName()).getSize();

		space.setNumberOfChildren(numberOfChildren);

	}


	private  void renderLocalizedLabels(Node spaceJcrNode, Space space, String locale) throws RepositoryException {

		cmsLocalizationRenderer.renderCmsLocalization(spaceJcrNode, space);

		space.setCurrentLocale(locale);

	}


	private   void renderContentObjectReferences(Node spaceJcrNode, Space space) throws RepositoryException {

		if (spaceJcrNode.hasProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName()))
			((Space)space).setNumberOfContentObjectReferences(spaceJcrNode.getProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName()).getValues().length);
		else
			((Space)space).setNumberOfContentObjectReferences(0);
	}

	private  void renderParent(Node parentSpaceJcrNode, Space space, RenderProperties renderProperties, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session) throws  AccessDeniedException, RepositoryException  {

		if (parentSpaceJcrNode.isNodeType(CmsBuiltInItem.Space.getJcrName())){
//			Check if parent Space has already been rendered
			String parentSpaceId = cmsRepositoryEntityUtils.getCmsIdentifier(parentSpaceJcrNode);

			if (cachedCmsRepositoryEntities.containsKey(parentSpaceId))
				space.setParent((Space)cachedCmsRepositoryEntities.get(parentSpaceId));
			else
			{
				space.setParent(cmsRepositoryEntityFactoryForActiveClient.newSpace());

				boolean renderParentIsEnabled = (renderProperties != null && renderProperties.isParentEntityRendered());

				if (renderParentIsEnabled){
					//Disable render its parent
					renderProperties.renderParentEntity(false);
				}
				
				
				renderSpace(parentSpaceJcrNode, renderProperties, session, space.getParent() , cachedCmsRepositoryEntities);
				
				if (renderParentIsEnabled){
					//Re enable render parent flag
					renderProperties.renderParentEntity(true);
				}
				
				cachedCmsRepositoryEntities.put(parentSpaceId, space.getParent());
			}
		}
	}

	public  Space renderSpaceWithoutItsParent(Node spaceJcrNode, RenderProperties renderProperties, Session session, 
			Space newSubSpace, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{
		if (renderProperties == null)
			renderProperties = getDefaultRenderProperties();

		renderProperties.renderParentEntity(false);

		return renderSpace(spaceJcrNode, renderProperties, session, newSubSpace, cachedCmsRepositoryEntities);
	}

	public   Space renderSpaceAndItsParent(Node spaceJcrNode, RenderProperties renderProperties, Session session,
			Space newSubSpace, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws  RepositoryException{
		if (renderProperties == null)
			renderProperties = getDefaultRenderProperties();

		renderProperties.renderParentEntity(true);

		return renderSpace(spaceJcrNode, renderProperties, session, newSubSpace, cachedCmsRepositoryEntities);
	}

	public  Space renderNode(Session session, Node node, RenderProperties renderProperties, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws RepositoryException {

			//Be careful row may be an node named localization and not a space
			//TODO Probably this code will be deprecated
			if (node.getName().equals(CmsBuiltInItem.Localization.getJcrName()))
				node = node.getParent();
			
			if (! node.isNodeType(CmsBuiltInItem.Space.getJcrName()))
				throw new CmsException("Unable to render node type "+ node.getPrimaryNodeType().getName());

		return renderSpaceAndItsParent(node, renderProperties, session,	cmsRepositoryEntityFactoryForActiveClient.newSpace(), cachedRepositoryUsers);
	}

	public  List<Space> render(NodeIterator spaceNodes, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws  RepositoryException{
		if (spaceNodes == null)
			throw new CmsException("Null space node list. Could not render");

		List<Space> spaces = new ArrayList<Space>();

		while (spaceNodes.hasNext())
			spaces.add(renderNode(session, spaceNodes.nextNode(), renderProperties, cachedRepositoryUsers));

		return spaces;
	}


	public  Space renderSpace(String spaceId, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryUsers) throws RepositoryException {

		Node spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, spaceId);
		
		return renderNode(session, spaceNode, renderProperties, cachedRepositoryUsers);
	}


	






	
}
