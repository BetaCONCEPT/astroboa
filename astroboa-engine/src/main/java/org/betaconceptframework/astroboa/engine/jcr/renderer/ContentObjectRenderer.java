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


import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDefinitionDao;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ContentObjectProfileItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentObjectRenderer {

	@Autowired
	private LazyContentObjectRenderer lazyContentObjectRenderer;
	
	@Autowired
	private ContentDefinitionDao contentDefinitionDao;


	private void renderHasVersion(Version contentObjectVersion, ContentObject renderedContentObject, CmsPropertyDefinition propertyDefinition, String locale) throws RepositoryException {
		
		final String hasVersionPropertyPath = ContentObjectProfileItem.HasVersion.getItemForQuery().getLocalPart();
		
		CmsProperty hasVersionProperty = renderedContentObject.getCmsProperty(hasVersionPropertyPath);
		
		//Set property value
		if (hasVersionProperty != null)
		{
			((SimpleCmsProperty)hasVersionProperty).addSimpleTypeValue(contentObjectVersion.getName());
		}
		
		
	}

	public ContentObject render(Session session, Node contentObjectNode, RenderProperties contentObjectRenderProperties, 
			Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) throws RepositoryException {
		
		//Check if node is actually a content object node and not one of its properties
		contentObjectNode = retrieveContentObjectNodeToBeUsedForRendering(contentObjectNode,contentObjectRenderProperties);
		
		ContentObjectTypeDefinition contentObjectTypeDefinition = getContentObjectTypeDefinition(contentObjectNode, cachedContentObjectTypeDefinitions);
		
		if (contentObjectTypeDefinition == null)
			throw new CmsException("No content object definition type to render template");

		return lazyContentObjectRenderer.renderContentObject(contentObjectTypeDefinition.getName(), 
				contentObjectNode, contentObjectRenderProperties, cachedCmsRepositoryEntities, session);
		
	}

	public Node retrieveContentObjectNodeToBeUsedForRendering(Node contentObjectNode,RenderProperties contentObjectRenderProperties) throws RepositoryException {
		
		if (contentObjectNode instanceof VersionHistory){
			//Rendering must be done for a version of content object.
			//There must be a CONTENT_OBJECT_VERSION render instruction
			if (contentObjectRenderProperties == null || contentObjectRenderProperties.getVersionUsedToRender() == null)
				throw new CmsException("No version specified for rendering. ContentObject version history path "+ contentObjectNode.getPath());
			
			String contentObjectVersionToRender = (String)contentObjectRenderProperties.getVersionUsedToRender();
			
			Version contentObjectVersionNode = ((VersionHistory)contentObjectNode).getVersion(contentObjectVersionToRender);
			
			//Actual information is stored in a child node under name jcr:frozenNode
			return contentObjectVersionNode.getNode(JcrBuiltInItem.JcrFrozenNode.getJcrName());
		}

		//Normal node. Find appropriate content object node
		String path = contentObjectNode.getPath();
		
		while(!contentObjectNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			contentObjectNode = contentObjectNode.getParent();
			
			if (contentObjectNode.isNodeType(CmsBuiltInItem.SYSTEM.getJcrName()))
				throw new CmsException("Reached Astroboa system node but no content object was found "+ path);
		}
		
		return contentObjectNode;
	}


	public ContentObjectTypeDefinition getContentObjectTypeDefinition(Node contentObjectNode, Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions) throws RepositoryException {
		if (contentObjectNode.hasProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()))
		{
			String contentObjectTypeDefinitionName = contentObjectNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString();

			if (!cachedContentObjectTypeDefinitions.containsKey(contentObjectTypeDefinitionName))
			{
				ContentObjectTypeDefinition contentObjectTypeDefinition;
				try {
					contentObjectTypeDefinition = contentDefinitionDao.getContentObjectTypeDefinition(contentObjectTypeDefinitionName);
				} catch (Exception e) {
					throw new CmsException(e);
				}

				if (contentObjectTypeDefinition == null)
					throw new CmsException("Undefined content object definition type "+ contentObjectTypeDefinitionName);
				
				cachedContentObjectTypeDefinitions.put(contentObjectTypeDefinitionName, contentObjectTypeDefinition);
			}
			
			return cachedContentObjectTypeDefinitions.get(contentObjectTypeDefinitionName);
		}
		else
			throw new CmsException("Undefined content object definition type "+ contentObjectNode.getPath());			

	}
}


