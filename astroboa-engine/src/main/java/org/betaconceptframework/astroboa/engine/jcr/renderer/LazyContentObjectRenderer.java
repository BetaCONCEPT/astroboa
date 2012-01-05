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


import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.definition.visitor.ContentObjectFullRendererVisitor;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.model.lazy.local.LazyComplexCmsPropertyLoader;
import org.betaconceptframework.astroboa.model.impl.AspectDefinitionManager;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class LazyContentObjectRenderer extends JcrDaoSupport{

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;
	@Autowired
	private RepositoryUserRenderer repositoryUserRenderer;
	@Autowired
	private LazyComplexCmsPropertyLoader lazyComplexCmsPropertyLoader;
	
	public ContentObject renderContentObject(String contentObjectType, Node contentObjectNode, RenderProperties renderProperties,
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session) {

		try{
			

			if (renderProperties == null){
				renderProperties = new RenderPropertiesImpl();
			}

			ContentObject contentObject  = cmsRepositoryEntityFactoryForActiveClient.newObjectForType(contentObjectType); 

			//Content Object Id
			renderContentObjectId(contentObjectNode, contentObject);

			//Owner
			renderOwner(contentObjectNode, cachedCmsRepositoryEntities, renderProperties, contentObject);
			
			//SystemName
			renderSystemName(contentObjectNode, contentObject);

			//Lock
			renderLock(contentObject, contentObjectNode);

			//	Type
			renderContentObjectType(contentObject.getTypeDefinition(), contentObjectNode, contentObject);

			//Attach PropertyContainer necessary values for initiating lazy loading
			contentObject.getComplexCmsRootProperty().setId(contentObject.getId());

			//Provide values that will be used for lazy rendering in case this is not a new content object
			if (contentObjectNode != null){
				((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).setPropertyContainerNodeUUID(contentObjectNode.getIdentifier());
				((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).setContentObjectNodeUUID(contentObjectNode.getIdentifier());
			}

			renderAspects(contentObjectNode, contentObject);
			
			//It may be the case that lazy loading has been disabled
			//In that case load all properties for contentObject
			boolean lazyLoadingHasBeenDisabled = renderProperties.allContentObjectPropertiesAreRendered();
			
			if (lazyLoadingHasBeenDisabled){
				ContentObjectFullRendererVisitor fullRendererVisitor = new ContentObjectFullRendererVisitor(contentObject, 
						lazyComplexCmsPropertyLoader, contentObjectNode.getIdentifier(), session,renderProperties, cachedCmsRepositoryEntities);
				
				contentObject.getTypeDefinition().accept(fullRendererVisitor);
				
				if (CollectionUtils.isNotEmpty(contentObject.getComplexCmsRootProperty().getAspects())){
					
					Collection<ComplexCmsPropertyDefinition> aspectDefinitions = contentObject.getComplexCmsRootProperty().getAspectDefinitions().values();
					
					for (ComplexCmsPropertyDefinition aspectDefinition : aspectDefinitions){
						aspectDefinition.accept(fullRendererVisitor);
					}
				}
			}
			return contentObject;

		}catch (Exception e){
			throw new CmsException(e);
		}
	}

	private void renderSystemName(Node contentObjectNode,
			ContentObject contentObject) throws RepositoryException {
		if (contentObjectNode != null && contentObjectNode.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){
			contentObject.setSystemName(contentObjectNode.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString());
		}
		
	}

	private void renderAspects(Node contentObjectNode, ContentObject contentObject) throws Exception {

		if (contentObjectNode != null){

			if (contentObjectNode.hasProperty(CmsBuiltInItem.Aspects.getJcrName())){

				Value[] aspectValues = contentObjectNode.getProperty(CmsBuiltInItem.Aspects.getJcrName()).getValues();

				for (Value aspectValue : aspectValues){
					String aspect = aspectValue.getString();

					((AspectDefinitionManager)contentObject.getComplexCmsRootProperty()).loadAspectDefinition(aspect);
				}
			}
		}
	}

	private void renderContentObjectType(ContentObjectTypeDefinition contentObjectTypeDefinition, Node contentObjectNode, ContentObject contentObject) throws ValueFormatException, RepositoryException, PathNotFoundException {
		if (contentObjectNode != null){
			//	Normally this would be the same with contentObjectTypeDefinition name
			String contentObjectTypeNameFromContentObjectNode = contentObjectNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString();

			if (StringUtils.isBlank(contentObjectTypeDefinition.getName()))
				throw new CmsException("Undefined contentObjectDefinition type");

			if ( ! contentObjectTypeDefinition.getName().equals(contentObjectTypeNameFromContentObjectNode))
				throw new CmsException("ContentObjectDefinitionType "+ contentObjectTypeDefinition.getName() + 
						" is not the same with content object type " + contentObjectTypeNameFromContentObjectNode+ 
				" saved in content object node");


			//In any case type value is the same
			contentObject.setContentObjectType(contentObjectTypeDefinition.getName());
		}
		else{
			contentObject.setContentObjectType(contentObjectTypeDefinition.getName());
		}
	}


	private void renderContentObjectId(Node contentObjectNode, CmsRepositoryEntity contentObject) throws  RepositoryException  {
		if (contentObjectNode != null){
			if (!cmsRepositoryEntityUtils.hasCmsIdentifier(contentObjectNode))
				throw new CmsException("No Id found for content object "+ contentObjectNode.getPath());

			cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(contentObjectNode, contentObject);
		}
	}

	private void renderLock(ContentObject contentObject, Node contentObjectNode) throws RepositoryException {
		if (contentObjectNode != null){
			contentObject.setLocked(contentObjectNode.isLocked());
		}
		else{
			contentObject.setLocked(false);
		}

	}



	private void renderOwner(Node contentObjectNode, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties, ContentObject contentObject) throws RepositoryException, ValueFormatException, PathNotFoundException {
		if (contentObjectNode != null){
			String contentObjectOwnerId = contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();

			//Retrieve OwnerId from repository
			RepositoryUser owner = null;
			if (!cachedCmsRepositoryEntities.containsKey(contentObjectOwnerId))
			{
				owner = repositoryUserRenderer.renderRepositoryUserNode(contentObjectOwnerId, renderProperties, getSession(), cachedCmsRepositoryEntities);

				// Put repository user in cache
				cachedCmsRepositoryEntities.put(contentObjectOwnerId, owner);
			}
			else
				owner = (RepositoryUser) cachedCmsRepositoryEntities.get(contentObjectOwnerId);

			contentObject.setOwner(owner);
		}
		else{
			contentObject.setOwner(cmsRepositoryEntityFactoryForActiveClient.newRepositoryUser());
		}

	}
}
