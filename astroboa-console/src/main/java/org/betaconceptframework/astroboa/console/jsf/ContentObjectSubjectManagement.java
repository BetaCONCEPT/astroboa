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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;

/**
 * @author gchomatas
 * Created on Nov 1, 2006
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSubjectManagement extends AbstractUIBean {
	
	// injected beans
	private CMSUtilities cmsUtilities;
	private LoggedInRepositoryUser loggedInRepositoryUser;
	private ContentService contentService;
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	
	
	// holds the label of the the new tag provided by the user
	private String newTagLabel;
	
	//holds the tags owned by the logged in user
	private List<Topic> loggedInUserTags;
	
	//holds the 100 most used tags
	private List<Topic> mostUsedTags;
	
	// holds the UUID of the content object that the user has selected to tag
	private String selectedContentObjectIdToTag;
	
	// holds the UUID of the selected tag in the case the tag exists
	private String selectedLoggedInUserTagId;
	
	// holds the topic/tag UUID that has been selected for removal
	private String selectedTopicIdForRemovalFromContentObjectSubject;
	
	
	
	
	
	
	
	public String addNewUserTagToContentObjectSubject_UIAction() {

		if (StringUtils.isNotBlank(getNewTagLabel())) {

			ContentObject selectedContentObjectToTag = 
				getCmsUtilities()
				.findContentObjectUIWrapperInContentObjectUIWrapperListByContentObjectId(
						(List<ContentObjectUIWrapper>)contentObjectStatefulSearchService
						.getReturnedContentObjects()
						.getWrappedData(), 
						getSelectedContentObjectIdToTag())
						.getContentObject();
			
			// check if the new tag exists in user tags
			Topic newUserTag = getCmsUtilities().createNewUserTag(getNewTagLabel(), JSFUtilities.getLocaleAsString(), getLoggedInRepositoryUser().getRepositoryUser());
			if (getCmsUtilities().findTopicInTopicListByLocalizedTopicLabel(getLoggedInUserTags(), 
					newUserTag.getLocalizedLabelForCurrentLocale()) != null) {
				logger.warn("The user tag is not a new tag since it is already in user tags");
				JSFUtilities.addMessage(null, "Η ετικέτα που δώσατε υπάρχει ήδη στις ετικέτες σας. Μπορείτε να την επιλέξετε από τις διαθέσιμες ετικέτες σας", FacesMessage.SEVERITY_WARN);
				return null;
			}
			
			// if we have reached this point everything is ok. Let proceed to add and save the new  tag
			((TopicReferenceProperty)selectedContentObjectToTag.getCmsProperty("profile.subject")).getSimpleTypeValues().add(newUserTag);
			saveContentObjectUpdatedTags(selectedContentObjectToTag);
			setNewTagLabel(null);
			
			//Also nullify root topics in folksonomy in current repository user
			resetLoggedInRepositoryUserFoksonomyRootyTopics();
		}
		else {
			logger.error("An empty label has been provided. User Tag cannot be created with an empty label");
			JSFUtilities.addMessage(null, "Η ετικέτα είναι κενή. Παρακαλώ εισάγετε τιμή για την ετικέτα", FacesMessage.SEVERITY_WARN);
		}
		return null;
	}
	
	

	public String addExistingUserTagToContentObjectSubject_UIAction() {
		Topic selectedLoggedInUserTag = getCmsUtilities().findTopicInTopicListByTopicId(getLoggedInUserTags(), getSelectedLoggedInUserTagId());
		ContentObject selectedContentObjectToTag = 
			getCmsUtilities()
			.findContentObjectUIWrapperInContentObjectUIWrapperListByContentObjectId(
					(List<ContentObjectUIWrapper>)contentObjectStatefulSearchService
					.getReturnedContentObjects()
					.getWrappedData(), 
					getSelectedContentObjectIdToTag())
					.getContentObject();
		
		if (selectedLoggedInUserTag == null) {
			logger.error("Some strange problem occured. Selected tag is not present in logged in user Tags List (loggedInUserTags)");
			//TODO: generate a warning JSF message
			return null;
		}
		if (selectedContentObjectToTag == null) {
			logger.error("Some strange problem occured. Selected content Object to tag is not present in content object list (returnedContentObjects)");
			//TODO: generate a warning JSF message
			return null;
		}
		// check if the selected tag is already in contentObject subject 
		if (getCmsUtilities().findTopicInTopicListByLocalizedTopicLabel(((TopicReferenceProperty)selectedContentObjectToTag.getCmsProperty("profile.subject")).getSimpleTypeValues(), 
				selectedLoggedInUserTag.getLocalizedLabelForCurrentLocale()) != null) {
			logger.warn("The selected tag is already in content object subject.");
			JSFUtilities.addMessage(null, "Η ετικέτα που επιλέξατε υπάρχει ήδη στις επιλεγμένες ετικέτες για αυτό το αντικείμενο.", FacesMessage.SEVERITY_WARN);
			return null;
		}
		
		// if we reached this point everything is ok. Let proceed to add and save the selected tag
		((TopicReferenceProperty)selectedContentObjectToTag.getCmsProperty("profile.subject")).addSimpleTypeValue(selectedLoggedInUserTag);
		saveContentObjectUpdatedTags(selectedContentObjectToTag);
		
		setSelectedLoggedInUserTagId(null);
		setSelectedContentObjectIdToTag(null);
		return null;
	}
	
	
	
	
	
	
	public String removeTopicFromContentObjectSubject_UIAction() {
		ContentObjectUIWrapper selectedContentObjectUIWrapperToTag = 
			getCmsUtilities()
			.findContentObjectUIWrapperInContentObjectUIWrapperListByContentObjectId(
					(List<ContentObjectUIWrapper>)contentObjectStatefulSearchService
					.getReturnedContentObjects()
					.getWrappedData(), 
					getSelectedContentObjectIdToTag());
		try {
			selectedContentObjectUIWrapperToTag.removeTopicFromContentObjectSubject(getSelectedTopicIdForRemovalFromContentObjectSubject());
			saveContentObjectUpdatedTags(selectedContentObjectUIWrapperToTag.getContentObject());
		}
		catch (Exception e) {
			//TODO: generate jsf message
		}
			return null;
	}
	
	
	
	
	
	
	private void saveContentObjectUpdatedTags(ContentObject selectedContentObjectToTag) {
		
		try {
			//getContentService().replaceContentObjectSubject(getSelectedContentObjectIdToTag(), ((TopicProperty)selectedContentObjectToTag.getCmsProperty("profile.subject")).getSimpleTypeValues());
			getContentService().saveContentObject(selectedContentObjectToTag, false);
			
			// reload the tags and versions of the newly tagged content object into the existing content object list (returnedContentObjects) in order to refresh changes (i.e. get topic ids and new version information)
			ContentObject reloadedContentObject = getContentService().getContentObjectByIdAndLocale(getSelectedContentObjectIdToTag(), JSFUtilities.getLocaleAsString(), null);
			((TopicReferenceProperty)selectedContentObjectToTag.getCmsProperty("profile.subject")).setSimpleTypeValues(((TopicReferenceProperty)reloadedContentObject.getCmsProperty("profile.subject")).getSimpleTypeValues());
			((StringProperty)selectedContentObjectToTag.getCmsProperty("profile.versions")).setSimpleTypeValues(((StringProperty)reloadedContentObject.getCmsProperty("profile.versions")).getSimpleTypeValues());
			((StringProperty)selectedContentObjectToTag.getCmsProperty("profile.hasVersion")).setSimpleTypeValue(((StringProperty)reloadedContentObject.getCmsProperty("profile.hasVersion")).getSimpleTypeValue());
			
			// reset user tags in navigation panel in order to be refreshed
			setLoggedInUserTags(null);
			
			RepositoryNavigation repositoryNavigation = (RepositoryNavigation) JSFUtilities.getBeanFromFacesContext("repositoryNavigation");
			setMostUsedTags(null);
			//repositoryNavigation.setUserTagTree(null);
			//repositoryNavigation.setUserTagTreeModel(null);
			//repositoryNavigation.setCmsGroupsTreeNodeData(null);
			//repositoryNavigation.setTopicTreeNodeData(null);
			
			// generate success message
			JSFUtilities.addMessage(null, "object.list.message.contentObjectSuccessfulyTaggedInfo", null , FacesMessage.SEVERITY_INFO);
		}
		catch (Exception e) {
			logger.error("content object could not be tagged. The error was:", e);
			//TODO: generate JSF message
		}
		
	}
	
	
	
	private void resetLoggedInRepositoryUserFoksonomyRootyTopics() {
		RepositoryUser currentRepositoryUser = loggedInRepositoryUser.getRepositoryUser();
		if (currentRepositoryUser != null){
			Taxonomy currentRepositoryUserFolksonomy = currentRepositoryUser.getFolksonomy();
			if (currentRepositoryUserFolksonomy != null){
				//Nullify so the next time it will called, aspect will 
				//be enabled
				currentRepositoryUserFolksonomy.setRootTopics(null);
			}
		}
		
	}



	public List<Topic> getLoggedInUserTags() {
		if (loggedInUserTags == null) {
			RepositoryUser currentRepositoryUser = loggedInRepositoryUser.getRepositoryUser();
			Taxonomy currentRepositoryUserFolksonomy = currentRepositoryUser.getFolksonomy();
			loggedInUserTags = currentRepositoryUserFolksonomy.getRootTopics();
		}
		return loggedInUserTags;
	}
	
	public void setLoggedInUserTags(List<Topic> loggedInUserTags) {
		this.loggedInUserTags = loggedInUserTags;
	}
	
	
	
	
	
	public List<Topic> getMostUsedTags() {
		if (this.mostUsedTags == null) {
			try {
				List<Topic> loadedMostUsedTags = getCmsUtilities().findMostPopularTags(JSFUtilities.getLocaleAsString());
				this.mostUsedTags = loadedMostUsedTags;
			} catch (Exception e) {
				logger.error("Most Used Tags could not be retreived the error was: ", e);
			}			
		}
		return this.mostUsedTags;
	}
	
	public void setMostUsedTags(List<Topic> mostUsedTags) {
		this.mostUsedTags = mostUsedTags;
	}
	
	
	
	
	
	
	public String getNewTagLabel() {
		return newTagLabel;
	}

	public void setNewTagLabel(String newTagLabel) {
		this.newTagLabel = newTagLabel;
	}



	public String getSelectedContentObjectIdToTag() {
		return selectedContentObjectIdToTag;
	}

	public void setSelectedContentObjectIdToTag(String selectedContentObjectIdToTag) {
		this.selectedContentObjectIdToTag = selectedContentObjectIdToTag;
	}


	
	public String getSelectedLoggedInUserTagId() {
		return selectedLoggedInUserTagId;
	}

	public void setSelectedLoggedInUserTagId(String selectedLoggedInUserTagId) {
		this.selectedLoggedInUserTagId = selectedLoggedInUserTagId;
	}
	
	
	
	public String getSelectedTopicIdForRemovalFromContentObjectSubject() {
		return selectedTopicIdForRemovalFromContentObjectSubject;
	}

	public void setSelectedTopicIdForRemovalFromContentObjectSubject(
			String selectedTopicIdForRemovalFromContentObjectSubject) {
		this.selectedTopicIdForRemovalFromContentObjectSubject = selectedTopicIdForRemovalFromContentObjectSubject;
	}
	
	
	
	public void setContentObjectStatefulSearchService(
			ContentObjectStatefulSearchService contentObjectStatefulSearchService) {
		this.contentObjectStatefulSearchService = contentObjectStatefulSearchService;
	}

	public CMSUtilities getCmsUtilities() {
		return cmsUtilities;
	}

	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}


	public LoggedInRepositoryUser getLoggedInRepositoryUser() {
		return loggedInRepositoryUser;
	}

	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}


	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	
}
