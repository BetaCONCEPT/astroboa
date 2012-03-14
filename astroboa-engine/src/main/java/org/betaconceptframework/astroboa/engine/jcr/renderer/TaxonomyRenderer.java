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


import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class TaxonomyRenderer extends AbstractRenderer{

	private final Logger logger = LoggerFactory.getLogger(TaxonomyRenderer.class);
	
	@Autowired
	private CmsLocalizationRenderer cmsLocalizationRenderer;
	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;

	public void renderTaxonomyToTopic(Node topicJcrNode, Topic topic) throws  AccessDeniedException, RepositoryException {

		//  Search for Topic
		Node parentTopicJcrNode = topicJcrNode.getParent();

		//Search until Astroboa System node is found or RepositoryUserNode is found
		while (!parentTopicJcrNode.getName().equals(CmsBuiltInItem.SYSTEM.getJcrName()) && 
				!parentTopicJcrNode.getName().equals(CmsBuiltInItem.RepositoryUserRoot.getJcrName())){
			if (parentTopicJcrNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
			{
				Taxonomy taxonomy = renderTaxonomy(parentTopicJcrNode, null);
				
				topic.setTaxonomy(taxonomy);
				break;
			}

			parentTopicJcrNode = parentTopicJcrNode.getParent();
		}

		//If no taxonomy found and taxonomyNode is not a Space under RepositoryUser issue a warning
		if (topic.getTaxonomy() == null &&
				 ( !parentTopicJcrNode.getName().equals(CmsBuiltInItem.RepositoryUserRoot.getJcrName()))){
			logger.warn("Reached "+ parentTopicJcrNode.getPath() + 
					" node but no taxonomy found for topic jcr node "+ topicJcrNode.getPath());
		}

	}

		
	public Taxonomy renderTaxonomy(Node taxonomyNode,  Taxonomy taxonomy)	throws RepositoryException {
		if (taxonomy == null)
			taxonomy = cmsRepositoryEntityFactoryForActiveClient.newTaxonomy();
		
		//Render Id
		cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(taxonomyNode, taxonomy);
		
		//Render Name
		taxonomy.setName(taxonomyNode.getName());
		
		//Render locale
		cmsLocalizationRenderer.renderCmsLocalization(taxonomyNode, taxonomy);

		//Render number of children topics
		if (taxonomyNode.hasNode(CmsBuiltInItem.Topic.getJcrName()))
			taxonomy.setNumberOfRootTopics((int)taxonomyNode.getNodes(CmsBuiltInItem.Topic.getJcrName()).getSize());
		else
			taxonomy.setNumberOfRootTopics(0);
		
		return taxonomy;
	}
}
