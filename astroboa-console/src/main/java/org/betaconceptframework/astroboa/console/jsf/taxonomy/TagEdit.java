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
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.commons.comparator.TopicLocalizedLabelComparator;
import org.betaconceptframework.astroboa.commons.comparator.TopicNameComparator;
import org.betaconceptframework.astroboa.console.jsf.LocalizationEdit;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.richfaces.event.DropEvent;

/**
 * @author gchomatas
 * Created on Oct 26, 2007
 */
@Name("tagEdit")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TagEdit extends LocalizationEdit {

	private static final long serialVersionUID = 1L;

	private LoggedInRepositoryUser loggedInRepositoryUser;
	private TopicService topicService;

	private String labelSortOrder = "ASC";
	private String nameSortOrder = "ASC";
	private Topic toBeDeletedTag;

	private Topic editedTag;

	private boolean editedTagBelongsToAFolksonomy;
	private Topic newParent;
	private boolean editedTagBecomeRootTopic = false;
	private boolean editedTagChangedTaxonomy = false;
	
	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	private String validateEditedTag() {

		//Validate Topic Name
		if (StringUtils.isNotBlank(editedTag.getName())){
			if (!CmsConstants.SystemNamePattern.matcher(editedTag.getName()).matches()){
				JSFUtilities.addMessage(null, "topic.edit.invalid.system.name", new String[]{editedTag.getName()}, FacesMessage.SEVERITY_WARN);
			    return "error";
			}
		}

		//Finally check is there is another topic with the same name
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());

		//Create all possible names with lower and upper case
		//Nevertheless if topic name is 'TesT' and there is already a Topic named
		//'teST' this query will miss it
		/*List<String> names = new ArrayList<String>();
		names.add(editedTag.getName().toLowerCase());
		names.add(editedTag.getName().toUpperCase());
		names.add(editedTag.getName());
		 */
		//topicCriteria.setMultipleNamesEqualsCriterion(names, Condition.OR);
		topicCriteria.addNameEqualsCaseInsensitiveCriterion(editedTag.getName().toLowerCase());
		topicCriteria.setOffsetAndLimit(0,1);

		if (editedTag.getId() != null){
			topicCriteria.addIdNotEqualsCriterion(editedTag.getId());
		}

		CmsOutcome<Topic> otherTopics = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		if (otherTopics != null && otherTopics.getCount() > 0){
			Topic topicWithSameSystemName = otherTopics.getResults().get(0);
			String topicPath = topicWithSameSystemName.getAvailableLocalizedLabel(localeSelector.getLocaleString());
			Topic parent = topicWithSameSystemName.getParent();
			while (parent != null) {
				topicPath = parent.getAvailableLocalizedLabel(localeSelector.getLocaleString()) + "->" + topicPath;
				parent = parent.getParent();
			}
			
			topicPath = topicWithSameSystemName.getTaxonomy().getAvailableLocalizedLabel(localeSelector.getLocaleString()) + "->" + topicPath;
			JSFUtilities.addMessage(null, "topic.non.unique.system.name", new String[]{topicPath}, FacesMessage.SEVERITY_WARN);
			return "error";
		}

		// validate Localized Labels
		return updateLocalizedLabelsMapInEditedLocalizationEntity(editedTag);
	}

	public void saveTag_UIAction() {
		//Check if this is a new topic/tag
		boolean isNewTag = (editedTag.getId() == null)?true : false;
		String saveResult = null;
		try {
			saveResult = validateEditedTag();
			if ("success".equals(saveResult)) {

				//Get topic parent's previous id
				String previousParentId = editedTag.getParent() != null ? editedTag.getParent().getId() : null;

				//Change parent if a new one is provided and topic is not a tag
				if (!editedTagBelongsToAFolksonomy && newParent != null){
					editedTag.setParent(newParent);
					
					//Get its parent taxonomy if not the same
					if (!newParent.getTaxonomy().getId().equals(editedTag.getTaxonomy().getId())){
						editedTag.setTaxonomy(newParent.getTaxonomy());
						editedTagChangedTaxonomy = true;
					}
				}

				//Save topic/tag
				topicService.save(editedTag);
				

				//Inform UI only if topic is a tag and a new one
				if (isNewTag && editedTagBelongsToAFolksonomy) // add tag into user folksonomy so that it is visible from the UI 
					loggedInRepositoryUser.getRepositoryUser().getFolksonomy().addRootTopic(editedTag);

				JSFUtilities.addMessage(null, "topic.save.successful", null, FacesMessage.SEVERITY_INFO);


				try{
					//In case topic belongs to a regular taxonomy raise events 
					//for taxonomy tree to be updated
					if (!editedTagBelongsToAFolksonomy){
						//Notify that a topic has been updated
						if (!isNewTag)
							Events.instance().raiseEvent(SeamEventNames.TOPIC_SAVED, editedTag); 
						
						if (editedTagChangedTaxonomy){
							//Force tree to rebuild again
							Events.instance().raiseEvent(SeamEventNames.NEW_TAXONOMY_TREE);	
						}
						else if (isNewTag || newParent != null || editedTagBecomeRootTopic){

							if (previousParentId != null)
								//Events.instance().raiseEvent(SeamEventNames.REMOVE_TOPIC_TREE_NODE, editedTag.getId());
								Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, previousParentId);
							else
								//Previous parent was taxonomy
								Events.instance().raiseEvent(SeamEventNames.RELOAD_TAXONOMY_TREE_NODE, editedTag.getTaxonomy().getId());

							//We send new parent's id in order to reload its children
							if (newParent != null)
								Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, newParent.getId());

							if (editedTagBecomeRootTopic && previousParentId != null)
								Events.instance().raiseEvent(SeamEventNames.RELOAD_TAXONOMY_TREE_NODE, editedTag.getTaxonomy().getId());
						}
					}
				}
				catch (Exception e) {
					JSFUtilities.addMessage(null, "topic.taxonomy.refresh.error", null, FacesMessage.SEVERITY_ERROR);
					getLogger().error("Seam events processing on topic or tag save",e);

					//Since an exception is thrown raise event to reload topic if it exists in tree
					if (!isNewTag)
						Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, editedTag.getId());

				}

			}
		}
		catch (Exception e) {
			if (e instanceof CmsException && e.getMessage() != null && e.getMessage().contains("Cannot move topic")){
				JSFUtilities.addMessage(null, "topic.relocate.error", null, FacesMessage.SEVERITY_ERROR);
			}
			else{
				JSFUtilities.addMessage(null, "topic.save.error", null, FacesMessage.SEVERITY_ERROR);
			}
			
			getLogger().error("Topic could not be saved",e);

			//Since an exception is thrown raise event to reload topic if it exists in tree
			if (!isNewTag)
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, editedTag.getId());

		}
		
	}

	public void reset() {
		editedTag = null;
		editedLocalizedLabels = null;
		labelSortOrder = "NONE";
		nameSortOrder = "NONE";
		editedTagBelongsToAFolksonomy =false;
		newParent = null;
		editedTagBecomeRootTopic = false;
		editedTagChangedTaxonomy = false;
	}

	public void addTag_UIAction() {
		createNewEditedTag(loggedInRepositoryUser.getRepositoryUser().getFolksonomy(), null);

	}

	public void createNewEditedTag(Taxonomy taxonomy, Topic parent){
		reset();
		Topic tag = cmsRepositoryEntityFactory.newTopic();
		tag.setTaxonomy(taxonomy);
		tag.setCurrentLocale(localeSelector.getLocaleString());
		tag.setOwner(loggedInRepositoryUser.getRepositoryUser());
		tag.setAllowsReferrerContentObjects(true);
		tag.setParent(parent);
		editedTag = tag;
		editedLocalizedLabels = new ArrayList<LocalizedLabel>();
		//create the first label
		addLocalizedLabel_UIAction();
		editedTagBelongsToAFolksonomy = taxonomy != null && taxonomy.getId() != null && taxonomy.getId().equals(loggedInRepositoryUser.getRepositoryUser().getFolksonomy().getId());
		newParent = null;
		labelSortOrder = "NONE";
		nameSortOrder = "NONE";
		editedTagBecomeRootTopic =false;
		editedTagChangedTaxonomy = false;
	}


	private String getLocalizedNameForTopicEntity(){
		if (editedTagBelongsToAFolksonomy)
			return JSFUtilities.getLocalizedMessage("Tag", null);

		return JSFUtilities.getLocalizedMessage("Topic", null);

	}
	public void markToBeDeletedTag_UIAction(Topic tag) {
		toBeDeletedTag = tag;
	}

	public void deleteTag_UIAction(Topic toBeDeletedTag) {
		try {
			topicService.deleteTopicTree(toBeDeletedTag.getId());
			
			//Nullify to force reload
			loggedInRepositoryUser.getRepositoryUser().getFolksonomy().setRootTopics(null);

			JSFUtilities.addMessage(null, "topic.cascadingDelete.succesful", new String[]{getLocalizedNameForTopicEntity()}, FacesMessage.SEVERITY_INFO);
			
			reset();
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "topic.cascadingDelete.error", FacesMessage.SEVERITY_WARN);
			getLogger().error("Tag could not be saved",e);
		}
	}


	public void editTag_UIAction(Topic tag) {
		reset();
		editedTag = tag;

		editedTagBelongsToAFolksonomy = tag.getTaxonomy() != null && 
		tag.getTaxonomy().getName() != null && 
		Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME.equals(tag.getTaxonomy().getName());

		fillLocalizedLabelBuffer(editedTag);

	}

	public void sortByLabel() {
		List<Topic> userTags = loggedInRepositoryUser.getRepositoryUser().getFolksonomy().getRootTopics();
		if ("ASC".equals(labelSortOrder)) {
			Collections.reverse(userTags);
			labelSortOrder = "DESC";
		}
		else if ("DESC".equals(labelSortOrder)) {
			Collections.reverse(userTags);
			labelSortOrder = "ASC";
		}
		else {
			Collections.sort(userTags, new TopicLocalizedLabelComparator(JSFUtilities.getLocaleAsString()));

			labelSortOrder = "ASC";
		}

	}

	public void sortByName() {
		List<Topic> userTags = loggedInRepositoryUser.getRepositoryUser().getFolksonomy().getRootTopics();
		if ("ASC".equals(nameSortOrder)) {
			Collections.reverse(userTags);
			nameSortOrder = "DESC";
		}
		else if ("DESC".equals(nameSortOrder)) {
			Collections.reverse(userTags);
			nameSortOrder = "ASC";
		}
		else {
			Collections.sort(userTags, new TopicNameComparator());

			nameSortOrder = "ASC";
		}

	}



	public void changeParentDraggedAndDropped_Listener(DropEvent dropEvent){

		Object draggedObject = dropEvent.getDragValue();

		if (draggedObject == null){
			JSFUtilities.addMessage(null, "topic.draggedAndDroppedObjectIsNull" , FacesMessage.SEVERITY_WARN);
			return;
		}

		if ( draggedObject instanceof Taxonomy){
			final Taxonomy draggedTaxonomy = (Taxonomy)draggedObject;

			//Edited Tag become root topic. Check if its taxonomy is changed as well
			String previousTaxonomyId = editedTag.getTaxonomy().getId();
			String draggedTaxonomyId = draggedTaxonomy.getId();
			
			if (StringUtils.isBlank(previousTaxonomyId)){
				JSFUtilities.addMessage(null, "topic.changeParentTaxonomy.oldParentTaxonomyHasNoId", FacesMessage.SEVERITY_WARN);
				getLogger().error("Tag parent could not be saved as its taxonomy does not have an id",editedTag.getTaxonomy());
			}
			
			if (StringUtils.isBlank(draggedTaxonomyId)){
				JSFUtilities.addMessage(null, "topic.changeParentTaxonomy.draggedAndDroppedTaxonomyHasNoId", FacesMessage.SEVERITY_WARN);
				getLogger().error("Tag parent could not be saved as dragged taxonomy does not have an id",draggedTaxonomy);
			}
			
			editedTagChangedTaxonomy =  ! previousTaxonomyId.equals(draggedTaxonomyId);
			
			//Nullify parent
			editedTag.setParent(null);
			editedTag.setTaxonomy(draggedTaxonomy);

			this.newParent = null;
			editedTagBecomeRootTopic = true;
		}
		else{
			if ( !(draggedObject instanceof Topic)){
				JSFUtilities.addMessage(null, "topic.changeParentTopic.draggedAndDroppedEntityIsNotATopic" , FacesMessage.SEVERITY_WARN);
				return;
			}

			Topic newParentTopic = (Topic)draggedObject;

			//Check that new parent is the same with editedTag
			if (editedTag.getId() != null && newParentTopic.getId().equals(editedTag.getId())){
				JSFUtilities.addMessage(null, "topic.changeParentTopic.topicCannotBeTheParentOfItself"  , FacesMessage.SEVERITY_ERROR);
				return;
			}

			//Check that new parent is not descendant of editedTag
			Topic parentOfNewParent = newParentTopic.getParent();
			while (parentOfNewParent != null){
				if (parentOfNewParent.getId().equals(editedTag.getId())){
					JSFUtilities.addMessage(null, "topic.changeParentTopic.childTopicCannotBeAParentTopicToo"  , FacesMessage.SEVERITY_ERROR);
					return;
				}
				parentOfNewParent = parentOfNewParent.getParent();
			}
			
			this.newParent = newParentTopic;
			editedTagBecomeRootTopic = false;
			editedTagChangedTaxonomy = false;
		}
	}

	public Topic getNewParent() {
		return newParent;
	}

	public List<LocalizedLabel> getEditedTagLabels() {
		return editedLocalizedLabels;
	}

	public Topic getEditedTag() {
		return editedTag;
	}


	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}

	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}


	public void setToBeDeletedTag(Topic toBeDeletedTag) {
		this.toBeDeletedTag = toBeDeletedTag;
	}

	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}
	
	
}
