/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingContentObjectFolderTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.richfaces.event.DropEvent;
import org.richfaces.model.TreeNode;

/**
 * @author gchomatas
 * Created on Nov 1, 2006
 */
@Name("repositoryNavigation")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryNavigation extends AbstractUIBean{
	
	private static final long serialVersionUID = 1L;

	public RepositoryNavigation() {
	
	}
	
	private RepositoryUser selectedRepositoryUser;
	private ContentObjectFolder selectedContentObjectFolder;

	
	// Injected beans
	private CMSUtilities cmsUtilities;
	private ContentService contentService;
	private SpaceService spaceService;
	
	/*
	// The tree models used to build content repository navigation trees with tomahawk
	private TreeModelBase topicTreeModel;
	private TreeModelBase cmsTreeModel;
	private TreeModelBase ownerTreeModel;
	*/
	
	// Rich faces tree components and tree data
	
	//private TreeNode topicTreeNodeData;
	
	private TreeNode contentObjectFolderTreeNodeData;
	
	private String selectedNodeIdentifier;
	private List<ContentObjectUIWrapper> selectedContentObjects;
	
	private String selectedTopicLabel;
	
	private String selectedRepositoryUserExternalIdOrLabel;
	
	
	public TreeNode getContentObjectFolderTreeNodeData() {
		if (contentObjectFolderTreeNodeData == null) {
			LazyLoadingContentObjectFolderTreeNodeRichFaces rootTreeNode =
				new LazyLoadingContentObjectFolderTreeNodeRichFaces("0", "Αντ. Περιεχομένου", null, "rootContentObjectFolder", false, null, null, null);
			contentObjectFolderTreeNodeData = rootTreeNode;
		}
		
		return contentObjectFolderTreeNodeData;
	}
	
	
	public String getSelectedNodeIdentifier() {
		return selectedNodeIdentifier;
	}

	public void setSelectedNodeIdentifier(String selectedNodeIdentifier) {
		this.selectedNodeIdentifier = selectedNodeIdentifier;
	}
	
	
	public List<RepositoryUser> getRepositoryUsersUIAction(Object event) {
		try {
			
			String selectedRepositoryUserExternalIdOrLabel = event.toString();
			
			return cmsUtilities.findRepositoryUsersByExternalIdOrLabelId(selectedRepositoryUserExternalIdOrLabel, JSFUtilities.getLocaleAsString());
		} catch (Exception e) {
			logger.error("Error while loading RepositoryUsers ",e);
			return null;
		}
	
	}
	
	/** Used to retrieve all topics which have topic labels starting with a specific substring (i.e. topicLabel like 'substring%'). 
	*It is used for the AjaxInput Component
	*/
	public List<Topic> getTopicsUIAction(Object event) {
		try {
			
			String selectedTopicLabel = event.toString();
			List<Topic> topics = cmsUtilities.findTopicsByLabel(selectedTopicLabel, JSFUtilities.getLocaleAsString());
			return topics;
		} catch (Exception e) {
			logger.error("Error while loading Topics ",e);
			return null;
		}
	
	}
	
	public void addContentObjectToDraggedAndDroppedSpaceAndSaveListener(DropEvent dropEvent){
		ContentObjectUIWrapper contentObjectUIWrapper = (ContentObjectUIWrapper) dropEvent.getDropValue();
		Space space = (Space) dropEvent.getDragValue();
		
		String selectedContentObjectId = contentObjectUIWrapper.getContentObject().getId();
		
		// check if selected contentObject is already in space
		List<String> contentObjectReferences = space.getContentObjectReferences();
		
		if (CollectionUtils.isNotEmpty(contentObjectReferences) && contentObjectReferences.contains(selectedContentObjectId)){
			JSFUtilities.addMessage(null, "Το Αντικείμενο ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ στον χώρο γιατί υπάρχει ήδη." , FacesMessage.SEVERITY_WARN);
			return ;
		}
		
		try {
			space.addContentObjectReference(selectedContentObjectId);
			spaceService.saveSpace(space);

			JSFUtilities.addMessage(null, "Το Αντικείμενο Προστέθηκε στον Χώρο" , FacesMessage.SEVERITY_INFO);
				
			}
			catch (Exception e) {
				JSFUtilities.addMessage(null, "Το Αντικείμενο ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ στον χώρο" , FacesMessage.SEVERITY_ERROR);
				getLogger().error("Content Object could not be attached to requested SPACE_INSTANCE",e);
			}
	}
	
	public void addDraggedAndDroppedTopicOrSpaceToContentObjectAndSave_Listener(DropEvent dropEvent){
		String dragType  = dropEvent.getDragType();
		
		if ("space".equals(dragType))
			addContentObjectToDraggedAndDroppedSpaceAndSaveListener(dropEvent);
		else if ("topic".equals(dragType))
			addDraggedAndDroppedTopicToContentObjectAndSave_Listener(dropEvent);
	}
	
	private void addDraggedAndDroppedTopicToContentObjectAndSave_Listener(DropEvent dropEvent){
		ContentObjectUIWrapper contentObjectUIWrapper = (ContentObjectUIWrapper) dropEvent.getDropValue();
		Topic topic = (Topic) dropEvent.getDragValue();
		
		//Check that Dropped Topic belongs to an accepted taxonomy
		List<String> acceptedTaxonomies = contentObjectUIWrapper.getAcceptedTaxonomiesForProfileSubject();
		if (CollectionUtils.isNotEmpty(acceptedTaxonomies)){
			String droppedTopicTaxonomyName = null;
			if (topic != null && topic.getTaxonomy() != null){ 
					droppedTopicTaxonomyName = topic.getTaxonomy().getName();
			}
			
			if (StringUtils.isEmpty(droppedTopicTaxonomyName) || !acceptedTaxonomies.contains(droppedTopicTaxonomyName)){
				JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ γιατί δεν ανήκει στις αποδεκτές ταξινομίες" , FacesMessage.SEVERITY_WARN);
				return;
			}
		}
		
		List<Topic> subjectTopics= ((TopicReferenceProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.subject")).getSimpleTypeValues();
		String selectedTopicId = topic.getId();
		
		// check if selected topic is already in subject
		boolean topicExists = false;
		for (Topic subjectTopic : subjectTopics) {
			// we check first if topic Id exists because there may be new user tags in the list and new tags do not have an id yet 
			if (subjectTopic.getId() != null && subjectTopic.getId().equals(selectedTopicId)) {
				topicExists = true;
				break;
			}
		}
		
		if (!topicExists) {
			((TopicReferenceProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
			
			try {
				contentService.saveAndVersionContentObject(contentObjectUIWrapper.getContentObject());
				JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία Προστέθηκε στο Αντικείμενο" , FacesMessage.SEVERITY_INFO);
				
				List<String> topicIds = new ArrayList<String>();
				topicIds.add(topic.getId());
				Events.instance().raiseEvent(SeamEventNames.UPDATE_NO_OF_CONTENT_OBJECT_REFERRERS, topicIds);
			}
			catch (Exception e) {
				JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ στο Αντικείμενο", FacesMessage.SEVERITY_ERROR);
				getLogger().error("Content Object could not be attached to requested TOPIC_INSTANCE",e);
			}
		}
		else
			JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ γιατί υπάρχει ήδη στο Αντικείμενο" , FacesMessage.SEVERITY_WARN);
		
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_ADDED, SeamEventNames.CONTENT_OBJECT_DELETED})
	public void refreshContentTypeBrowserTree(String contentObjectType, String contentObjectId, Calendar dayToBeRefreshed) {
		
		//contentObjectId is not used in this context
		if (contentObjectFolderTreeNodeData != null)
				((LazyLoadingContentObjectFolderTreeNodeRichFaces)contentObjectFolderTreeNodeData).contentObjectAddedOrDeletedEventRaised(contentObjectType, dayToBeRefreshed);
	}
	
	
   /* The other way around is used
	*
	*public void addTopicToDraggedAndDroppedContentObject_Listener(DropEvent dropEvent) {
		ContentObjectUIWrapper contentObjectUIWrapper = (ContentObjectUIWrapper) dropEvent.getDragValue();
		LazyLoadingTopicTreeNodeRichFaces topicTreeNode = 
			(LazyLoadingTopicTreeNodeRichFaces) dropEvent.getDropValue();
		
		String dragType = dropEvent.getDragType();
		
		List<Topic> subjectTopics= ((TopicProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.subject")).getSimpleTypeValues();
		String selectedTopicId = topicTreeNode.getTopic().getId();
		
		// check if selected topic is already in subject
		boolean topicExists = false;
		for (Topic subjectTopic : subjectTopics) {
			// we check first if topic Id exists because there may be new user tags in the list and new tags do not have an id yet 
			if (subjectTopic.getId() != null && subjectTopic.getId().equals(selectedTopicId)) {
				topicExists = true;
				break;
			}
		}
		
		if (!topicExists) {
			((TopicProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.subject")).addSimpleTypeValue(topicTreeNode.getTopic());
			
			if (dragType.equals("contentObject")) {
				try {
					contentService.saveAndVersionContentObject(contentObjectUIWrapper.getContentObject());
					JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία Προστέθηκε στο Αντικείμενο" , FacesMessage.SEVERITY_INFO);
				}
				catch (Exception e) {
					JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ στο Αντικείμενο. Το σφάλμα ήταν: " + e.toString(), FacesMessage.SEVERITY_ERROR);
					getLogger().error("Content Object could not be attached to requested SPACE_INSTANCE",e);
					e.printStackTrace();
				}
			}
			else
				JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία Προστέθηκε στο Αντικείμενο" , FacesMessage.SEVERITY_INFO);
		}
		else
			JSFUtilities.addMessage(null, "Η Θεματική Κατηγορία ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ γιατί υπάρχει ήδη στο Αντικείμενο" , FacesMessage.SEVERITY_WARN);
			
		
	}*/
	
	/*@Observer({SeamEventNames.TOPIC_SAVED})
	public void topicSaved(Topic topicSaved) {
		
		if (topicTreeNodeData != null && topicSaved != null)
			((LazyLoadingTopicTreeNodeRichFaces)topicTreeNodeData).reloadTopicTreeNodeEventRaised(topicSaved.getId());
		 
	}
	
	@Observer({SeamEventNames.UPDATE_NO_OF_CONTENT_OBJECT_REFERRERS})
	public void reloadTopicTreeNode(List<String> topicIds) {
		
		if (topicTreeNodeData != null && CollectionUtils.isNotEmpty(topicIds)) {
			for (String topicId : topicIds){
				if (topicId != null)
					((LazyLoadingTopicTreeNodeRichFaces)topicTreeNodeData).updateNoOfContentObjectReferrersEventRaised(topicId);
			}
		}
		 
	}
	
	@Observer({SeamEventNames.RELOAD_TOPIC_TREE_NODE})
	public void reloadTopicTreeNode(String topicId) {
		
		if (topicTreeNodeData != null && topicId != null) {
				((LazyLoadingTopicTreeNodeRichFaces)topicTreeNodeData).reloadTopicTreeNodeEventRaised(topicId);
		}
		 
	}
	
	@Observer({SeamEventNames.RELOAD_TAXONOMY_TREE_NODE})
	public void reloadTaxonomyTreeNode(String taxonomyId) {

		if (topicTreeNodeData != null && taxonomyId != null) {
				String topicSystemTaxonomyId = ((LazyLoadingTopicTreeNodeRichFaces)topicTreeNodeData).getSystemTaxonomyId();
				if (topicSystemTaxonomyId !=null && topicSystemTaxonomyId.equals(taxonomyId)){
					//Should reload tree
					topicTreeNodeData = null;
				}
		}
		 
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_DELETED})
	public void refreshNavigation() {
		
		if (topicTreeNodeData != null)
				((LazyLoadingTopicTreeNodeRichFaces)topicTreeNodeData).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
	}*/

	public List<ContentObjectUIWrapper> getSelectedContentObjects() {
		return selectedContentObjects;
	}

	public void setSelectedContentObjects(List<ContentObjectUIWrapper> selectedContentObjects) {
		this.selectedContentObjects = selectedContentObjects;
	}
	
	
	public RepositoryUser getSelectedRepositoryUser() {
		return selectedRepositoryUser;
	}

	public void setSelectedRepositoryUser(RepositoryUser selectedRepositoryUser) {
		this.selectedRepositoryUser = selectedRepositoryUser;
	}


	public ContentObjectFolder getSelectedContentObjectFolder() {
		return selectedContentObjectFolder;
	}

	public void setSelectedContentObjectFolder(
			ContentObjectFolder selectedContentObjectFolder) {
		this.selectedContentObjectFolder = selectedContentObjectFolder;
	}


	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}
	
	

	public void setSelectedTopicLabel(String selectedTopicLabel) {
		this.selectedTopicLabel = selectedTopicLabel;
	}

	public String getSelectedTopicLabel() {
		return selectedTopicLabel;
	}

	public void setContentObjectFolderTreeNodeData(
			TreeNode contentObjectFolderTreeNodeData) {
		this.contentObjectFolderTreeNodeData = contentObjectFolderTreeNodeData;
	}


	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}


	/**
	 * @return the selectedRepositoryUserExternalIdOrLabel
	 */
	public String getSelectedRepositoryUserExternalIdOrLabel() {
		return selectedRepositoryUserExternalIdOrLabel;
	}


	/**
	 * @param selectedRepositoryUserExternalIdOrLabel the selectedRepositoryUserExternalIdOrLabel to set
	 */
	public void setSelectedRepositoryUserExternalIdOrLabel(
			String selectedRepositoryUserExternalIdOrLabel) {
		this.selectedRepositoryUserExternalIdOrLabel = selectedRepositoryUserExternalIdOrLabel;
	}


	
}
