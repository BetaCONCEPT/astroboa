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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;



import java.util.List;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

@Name("taxonomyTree")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyTree extends AbstractUIBean{

	private static final long serialVersionUID = 1L;

	public enum TaxonomyTreeNodeType{
		TAXONOMY_ROOT_NODE,
		TAXONOMY,
		TOPIC
	}
	
	private TreeNode taxonomyRootNodeForBrowsing;
	
	private TreeNode taxonomyRootNodeForTopicInput;
	
	private List<String> acceptedTaxonomies;
	
	private Boolean topicSelectionDialogueActive;
	
	@In
	private LocaleSelector localeSelector;
	
	
	public TreeNode getTaxonomyRootNodeForBrowsing() {
		if (taxonomyRootNodeForBrowsing == null){
			
			taxonomyRootNodeForBrowsing = new LazyLoadingTaxonomyTreeRootNode(localeSelector);
		}
		
		return taxonomyRootNodeForBrowsing;
	}
	
	/*
	 * Call this method to exclude from the returned tree all but the accepted taxonomies.
	 * This is useful if the tree is to be used for adding values to a topic property that
	 * accepts values only from specific taxonomies
	 */
	public TreeNode getTaxonomyRootNodeForTopicInput() {
		if (taxonomyRootNodeForTopicInput == null){
			
			taxonomyRootNodeForTopicInput = new LazyLoadingTaxonomyTreeRootNode(acceptedTaxonomies, localeSelector);
		}
		
		return taxonomyRootNodeForTopicInput;
	}
	

	@Observer({SeamEventNames.NEW_TAXONOMY_TREE})
	public void clearTree() {
		taxonomyRootNodeForBrowsing = null;
		taxonomyRootNodeForTopicInput = null;
	}

	@Observer({SeamEventNames.TOPIC_SAVED})
	public void topicSaved(Topic topicSaved) {
		
		if (taxonomyRootNodeForBrowsing != null && topicSaved != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).topicSavedEventRaised(topicSaved);
		}
		
		if (taxonomyRootNodeForTopicInput != null && topicSaved != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).topicSavedEventRaised(topicSaved);
		}
		 
	}
	
	@Observer({SeamEventNames.UPDATE_NO_OF_CONTENT_OBJECT_REFERRERS})
	public void updateNoOfContentObjectReferrers(List<String> topicIds) {
		
		if (taxonomyRootNodeForBrowsing != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).updateNoOfContentObjectReferrersEventRaised(topicIds);
		}
		
		if (taxonomyRootNodeForTopicInput != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).updateNoOfContentObjectReferrersEventRaised(topicIds);
		}
		 
	}
	
	@Observer({SeamEventNames.RELOAD_TOPIC_TREE_NODE})
	public void reloadTopicTreeNode(String topicId) {
		
		if (taxonomyRootNodeForBrowsing != null && topicId != null) {
				((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).reloadTopicTreeNodeEventRaised(topicId);
		}
		
		if (taxonomyRootNodeForTopicInput != null && topicId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).reloadTopicTreeNodeEventRaised(topicId);
		}
		 
	}

	@Observer({SeamEventNames.RELOAD_TAXONOMY_TREE_NODE})
	public void reloadTaxonomyTreeNode(String taxonomyId) {
		
		if (taxonomyRootNodeForBrowsing != null && taxonomyId != null) {
				((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).reloadTaxonomyTreeNodeEventRaised(taxonomyId);
		}
		
		if (taxonomyRootNodeForTopicInput != null && taxonomyId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).reloadTaxonomyTreeNodeEventRaised(taxonomyId);
		}
	}

	@Observer({SeamEventNames.TAXONOMY_ADDED})
	public void taxonomyAdded() {
		
		if (taxonomyRootNodeForBrowsing != null) {
				((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).taxonomyAddedEventRaised();
		}
		
		if (taxonomyRootNodeForTopicInput != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).taxonomyAddedEventRaised();
		}
		 
	}
	
	@Observer({SeamEventNames.TAXONOMY_SAVED})
	public void taxonomySaved(String taxonomySavedId) {
		
		if (taxonomyRootNodeForBrowsing != null && taxonomySavedId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).taxonomySavedEventRaised(taxonomySavedId);
		}
		
		if (taxonomyRootNodeForTopicInput != null && taxonomySavedId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).taxonomySavedEventRaised(taxonomySavedId);
		}
		 
	}
	
	@Observer({SeamEventNames.TAXONOMY_DELETED})
	public void taxonomyDeleted(String taxonomyId) {
		
		if (taxonomyRootNodeForBrowsing != null && taxonomyId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).taxonomyDeletedEventRaised(taxonomyId);
		}
		
		if (taxonomyRootNodeForTopicInput != null && taxonomyId != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).taxonomyDeletedEventRaised(taxonomyId);
		}
		 
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_DELETED})
	public void refreshNavigation() {
		
		if (taxonomyRootNodeForBrowsing != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForBrowsing).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
		}
		
		if (taxonomyRootNodeForTopicInput != null) {
			((LazyLoadingTaxonomyTreeRootNode)taxonomyRootNodeForTopicInput).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
		}
	}

	public List<String> getAcceptedTaxonomies() {
		return acceptedTaxonomies;
	}
	
	/*
	 * Every time this method is called we should also nullify the taxonomyRootNodeForTopicInput
	 * so that the taxonomy tree will be recreated. We also set the dialogue to be active
	 */
	public void setAcceptedTaxonomies(List<String> acceptedTaxonomies) {
		taxonomyRootNodeForTopicInput = null;
		topicSelectionDialogueActive = true;
		this.acceptedTaxonomies = acceptedTaxonomies;
	}

	public Boolean getTopicSelectionDialogueActive() {
		return topicSelectionDialogueActive;
	}

	public void deactivateTopicSelectionDialog() {
		topicSelectionDialogueActive = false;
		taxonomyRootNodeForTopicInput = null;
	}
}
