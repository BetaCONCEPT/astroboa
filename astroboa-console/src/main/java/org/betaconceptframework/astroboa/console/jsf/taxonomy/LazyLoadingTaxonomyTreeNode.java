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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.commons.comparator.TopicLocalizedLabelComparator;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingTaxonomyTreeNode  extends LazyLoadingTreeNodeRichFaces{

	private static final long serialVersionUID = 1L;
	
	private Taxonomy taxonomy;
	private TaxonomyService taxonomyService;

	public LazyLoadingTaxonomyTreeNode(String identifier, Taxonomy taxonomy, TreeNode parent) {
		super(identifier, taxonomy.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()),
				parent, TaxonomyTree.TaxonomyTreeNodeType.TAXONOMY.toString(), false);
		this.taxonomy = taxonomy;
		this.taxonomyService = (TaxonomyService) JSFUtilities.getBeanFromSpringContext("taxonomyService");
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve root topics: " + this.getIdentifier());
			
			List<Topic> rootTopics = taxonomy.getRootTopics();
			
			Collections.sort(rootTopics, new TopicLocalizedLabelComparator(JSFUtilities.getLocaleAsString()));
			
			//int nodeIndex = 0;
			if (CollectionUtils.isNotEmpty(rootTopics)){
				for (Topic topic : rootTopics){
					
					LazyLoadingTopicTreeNode topicTreeNode = new LazyLoadingTopicTreeNode(topic.getId(),topic, this);
					
					children.put(topicTreeNode.getIdentifier(), topicTreeNode);
					//nodeIndex++;
				}
			}
			
		}
		
		return children.entrySet().iterator();
	}

	public Taxonomy getTaxonomy(){
		return taxonomy;
	}

	public LazyLoadingTopicTreeNode getTopicTreeNode(String topicId) {
		if (StringUtils.isBlank(topicId))
			return null;
		
		TreeNode topicTreeNode = getChild(topicId);
		
		if (topicTreeNode == null && !isLeaf() && children.size() > 0){
			//Search its children
			Collection<TreeNode> topicTreeNodes = children.values();
			
			for (TreeNode childTopicTreeNode : topicTreeNodes){
				topicTreeNode = ((LazyLoadingTopicTreeNode)childTopicTreeNode).getTopicTreeNode(topicId);
				
				if (topicTreeNode != null)
					break;
			}
		}
		
		return (LazyLoadingTopicTreeNode) topicTreeNode;
	}

	public void reloadTaxonomy() {
		this.children.clear();
		
		this.taxonomy = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		
	}

	@Override
	public String getDescription() {
		//It may be the case that a description is an empty string
		if (StringUtils.isBlank(description))
			return null;
		return description;
	}

	public void changeDescription() {

		this.description = taxonomy.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
		
	}
	
	public boolean isTaxonomyIsSystemTaxonomy(){
		return taxonomy !=null && taxonomy.getName() != null && ( 
			Taxonomy.SUBJECT_TAXONOMY_NAME.equalsIgnoreCase(taxonomy.getName()));
	}

	public void contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised() {
		if (! children.isEmpty()){
			Collection<TreeNode> childTopicTreeNodes = children.values();
			for (TreeNode childTopicTreeNode : childTopicTreeNodes){
				((LazyLoadingTopicTreeNode)childTopicTreeNode).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
			}
		}

		
	}
	
}
