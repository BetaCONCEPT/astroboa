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


import java.util.ArrayList;
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
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.taxonomy.TaxonomyTree.TaxonomyTreeNodeType;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingTaxonomyTreeRootNode  extends LazyLoadingTreeNodeRichFaces{

	private static final long serialVersionUID = 1L;

	private List<String> acceptedTaxonomies;
	
	private LocaleSelector localeSelector;
	
	private TaxonomyService taxonomyService;
	
	public LazyLoadingTaxonomyTreeRootNode(LocaleSelector localeSelector){
		super("0","TaxonomyRootNode", null, TaxonomyTreeNodeType.TAXONOMY_ROOT_NODE.toString(), false);
		this.localeSelector = localeSelector;
		this.taxonomyService = (TaxonomyService) JSFUtilities.getBeanFromSpringContext("taxonomyService");
	}
	
	public LazyLoadingTaxonomyTreeRootNode(List<String> acceptedTaxonomies, LocaleSelector localeSelector){
		super("0","TaxonomyRootNode", null, TaxonomyTreeNodeType.TAXONOMY_ROOT_NODE.toString(), false);
		this.localeSelector = localeSelector;
		this.taxonomyService = (TaxonomyService) JSFUtilities.getBeanFromSpringContext("taxonomyService");
		this.acceptedTaxonomies = acceptedTaxonomies;
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve taxonomies: " + this.getIdentifier());

			List<Taxonomy> taxonomies = new ArrayList<Taxonomy>();
			List<Taxonomy> allTaxonomies = new ArrayList<Taxonomy>();
			
			//Load all taxonomies
			allTaxonomies = taxonomyService.getAllTaxonomies(ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.ENTITY, false).getResults();
			
			if (CollectionUtils.isNotEmpty(acceptedTaxonomies)) { 
				for (Taxonomy taxonomy : allTaxonomies) {
					if (acceptedTaxonomies.contains(taxonomy.getName())) {
						taxonomies.add(taxonomy);
					}
				}
			}
			else {
				taxonomies = allTaxonomies;
			}

			//Sort taxonomies
			Collections.sort(taxonomies, new TaxonomyLocalizedLabelComparator());

			//int nodeIndex = 0;
			if (CollectionUtils.isNotEmpty(taxonomies)){
				for (Taxonomy taxonomy : taxonomies){

					LazyLoadingTaxonomyTreeNode taxonomyNode = new LazyLoadingTaxonomyTreeNode(taxonomy.getId(),taxonomy, this);

					children.put(taxonomyNode.getIdentifier(), taxonomyNode);
					//nodeIndex++;
					   
				}
			}

		}
		else if (children.size() > 0){

		}

		return children.entrySet().iterator();
	}

	public void topicSavedEventRaised(Topic topicSaved) {
		// Find topicTreeNode
		LazyLoadingTopicTreeNode topicTreeNode = getLazyLoadingTopicTreeNode(topicSaved.getId());

		if (topicTreeNode == null){
			return;
		}

		Topic topicInsideTopicTreeNode = topicTreeNode.getTopic();
		if (topicInsideTopicTreeNode == null){
			logger.error("Event for saved topic is raised but corresponding topic tree node has no topic");
			return;
		}

		if (topicInsideTopicTreeNode.getId() == null || ! topicInsideTopicTreeNode.getId().equals(topicSaved.getId())){
			logger.error("Event for saved topic is raised but corresponding topic tree node refers to a different topic." +
					" Topic inside tree node '"+topicInsideTopicTreeNode.getId() + " - "+ topicInsideTopicTreeNode.getName() + "'" +
					" Topic saved "+topicSaved.getId() + " - "+ topicSaved.getName());
			return;
		}

		//Change topicTreeNode description 
		topicTreeNode.changeDescription(topicSaved.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString()));



	}

	private LazyLoadingTopicTreeNode getLazyLoadingTopicTreeNode(String topicId) {

		if (StringUtils.isNotBlank(topicId) && ! children.isEmpty()){

			Collection<TreeNode> taxonomyTreeNodes = children.values();

			for (TreeNode taxonomyTreeNode : taxonomyTreeNodes){
				LazyLoadingTopicTreeNode topicTreeNode = ((LazyLoadingTaxonomyTreeNode)taxonomyTreeNode).getTopicTreeNode(topicId);

				if (topicTreeNode != null)
					return topicTreeNode;
			}
		}
		return null;
	}


	public void reloadTopicTreeNodeEventRaised(String parentTopicId) {
		if (parentTopicId != null){
			// Find topicTreeNode
			LazyLoadingTopicTreeNode topicTreeNode = getLazyLoadingTopicTreeNode(parentTopicId);

			if (topicTreeNode == null){
				return;
			}

			topicTreeNode.reloadTopic();
		}		
	}

	public void reloadTaxonomyTreeNodeEventRaised(String taxonomyId) {
		if (taxonomyId != null && !children.isEmpty()){

			LazyLoadingTaxonomyTreeNode taxonomyTreeNode = (LazyLoadingTaxonomyTreeNode)children.get(taxonomyId);
			if (taxonomyTreeNode != null)
				taxonomyTreeNode.reloadTaxonomy();

		}

	}

	public void taxonomyAddedEventRaised() {
		// children is a linkedHashMap which does not permit to insert an element
		// wherever a user wants. Thus the only way to display correctly the new taxonomy is to
		// reload taxonomies
		children.clear();

	}

	public void taxonomySavedEventRaised(String taxonomyId) {
		if (taxonomyId != null && !children.isEmpty()){

			LazyLoadingTaxonomyTreeNode taxonomyTreeNode = (LazyLoadingTaxonomyTreeNode)children.get(taxonomyId);
			if (taxonomyTreeNode != null)
				taxonomyTreeNode.changeDescription();
		}
	}

	public void taxonomyDeletedEventRaised(String taxonomyId) {
		if (taxonomyId != null && !children.isEmpty()){

			children.remove(taxonomyId);

		}
	}

	public void updateNoOfContentObjectReferrersEventRaised(List<String> topicIds) {
		if (CollectionUtils.isNotEmpty(topicIds)) {
			for (String topicId : topicIds){
				if (topicId != null){
					LazyLoadingTopicTreeNode topicTreeNode = getLazyLoadingTopicTreeNode(topicId);

					if (topicTreeNode == null){
						return;
					}

					topicTreeNode.updateNoOfContentObjectReferrers();

				}

			}
		}


	}

	public void contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised() {
		if (! children.isEmpty()){
			Collection<TreeNode> childTopicTreeNodes = children.values();
			for (TreeNode childTopicTreeNode : childTopicTreeNodes){
				((LazyLoadingTaxonomyTreeNode)childTopicTreeNode).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
			}
		}

	}


}
