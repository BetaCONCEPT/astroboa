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


import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.RepositoryUserType;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class RepositoryUserRenderer extends AbstractRenderer{

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;
	@Autowired
	private TaxonomyRenderer taxonomyRenderer;
	@Autowired
	private SpaceRenderer spaceRenderer;

	
	public  RepositoryUser renderRepositoryUserNode(String repositoryUserId, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryEntities) throws RepositoryException {

		Node repositoryUserNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, repositoryUserId);
		
		return renderRepositoryUserNode(repositoryUserNode, renderProperties, session, cachedRepositoryEntities);

	}
	public  RepositoryUser renderRepositoryUserNode(Node repositoryUserNode,RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryEntities) throws RepositoryException {

		if (repositoryUserNode == null)
			throw new CmsException("No repository user node is specified");
		
		RepositoryUser repositoryUser = cmsRepositoryEntityFactoryForActiveClient.newRepositoryUser();
		
			cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(repositoryUserNode, repositoryUser);

			//External Id
			repositoryUser.setExternalId(repositoryUserNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString());

			//Label
			if (repositoryUserNode.hasProperty(CmsBuiltInItem.Label.getJcrName()))
				repositoryUser.setLabel(repositoryUserNode.getProperty(CmsBuiltInItem.Label.getJcrName()).getString());

			//UserType
			if (repositoryUserNode.hasProperty(CmsBuiltInItem.UserType.getJcrName()))
				repositoryUser.setUserType(RepositoryUserType.valueOf(repositoryUserNode.getProperty(CmsBuiltInItem.UserType.getJcrName()).getString()));

			//Space
			renderSpace(repositoryUserNode, repositoryUser, renderProperties, session, cachedRepositoryEntities);
			
			//Folksonomy
			renderFolksonomy(repositoryUserNode, repositoryUser);
			
			// TODO Preferences
			renderPreferences(repositoryUserNode, repositoryUser, renderProperties);
			
			return repositoryUser;
	}
	
	
	private void renderPreferences(Node repositoryUserNode, RepositoryUser repositoryUser, RenderProperties renderProperties) {
		
		
	}
	
	private void renderFolksonomy(Node repositoryUserNode, RepositoryUser repositoryUser) throws RepositoryException {
		if (repositoryUserNode.hasNode(CmsBuiltInItem.Folksonomy.getJcrName())){
			Node folksonomyNode = repositoryUserNode.getNode(CmsBuiltInItem.Folksonomy.getJcrName());
		
			Map<String, CmsRepositoryEntity> cachedRepositoryUser = new HashMap<String, CmsRepositoryEntity>();
			cachedRepositoryUser.put(repositoryUser.getId(), repositoryUser);

			Taxonomy folskonomy = taxonomyRenderer.renderTaxonomy(folksonomyNode,	repositoryUser.getFolksonomy());
			
			((RepositoryUserImpl)repositoryUser).setFolksonomy(folskonomy);
		}
		
	}
	private void renderSpace(Node repositoryUserNode, RepositoryUser repositoryUser, RenderProperties renderProperties, Session session, Map<String, CmsRepositoryEntity> cachedRepositoryEntities) throws RepositoryException {
		if (repositoryUserNode.hasNode(CmsBuiltInItem.Space.getJcrName()))
		{
			//Set null for render properties in order to get the default render properties
			Space userSpace = repositoryUser.getSpace();
			userSpace.setOwner(repositoryUser);
			
			((RepositoryUserImpl)repositoryUser).setSpace(spaceRenderer.renderSpaceWithoutItsParent(repositoryUserNode.getNode(CmsBuiltInItem.Space.getJcrName()), renderProperties,
					session, userSpace, cachedRepositoryEntities));
			
		}
	}
}
