/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.console.jsf.browse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectSelectionBean;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("deleteContentObjectBean")
@Scope(ScopeType.EVENT)
public class DeleteContentObjectBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AstroboaClient astroboaClient;
	
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	

	public void deleteContentObjectSelection(ContentObjectSelectionBean contentObjectSelection) {
		
		if (contentObjectSelection == null || CollectionUtils.isEmpty(contentObjectSelection.getSelectedContentObjects())) {
			JSFUtilities.addMessage(null, "object.action.bulk.delete.message.no.object.selected", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try{
			List<ContentObjectUIWrapper> contentObjectUiWrapperList = contentObjectSelection.getSelectedContentObjects();
		
			DeletionContext deletionContext = new DeletionContext();
			
			List<ContentObjectUIWrapper> deletedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			
			for (ContentObjectUIWrapper coUI : contentObjectUiWrapperList)
			{
				if (deleteContentObject(coUI, deletionContext))
				{
					deletedContentObjects.add(coUI);
				}
			}
			
			printMessages(deletionContext);
			
			if (deletionContext.atLeastOneContentObjectWasDeletedWithSuccess())
			{
				resetContentObjectList(deletionContext);
				
				contentObjectSelection.clearAllSelectedContentObjects_UIAction();
			}
			
		}
		catch(Exception e) 
		{
			JSFUtilities.addMessage(null, "object.action.bulk.delete.message.error", null, FacesMessage.SEVERITY_WARN);
		}
	}

	private void resetContentObjectList(DeletionContext deletionContext)
	{
		ContentObjectStatefulSearchService contentObjectStatefulSearchService = (ContentObjectStatefulSearchService) JSFUtilities.getBeanFromSpringContext("contentObjectStatefulSearchService");
		
		// reset data page and decrease search results count
		contentObjectStatefulSearchService.setSearchResultSetSize(contentObjectStatefulSearchService.getSearchResultSetSize() - deletionContext.getDeletedContentObjectCounter());
		UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
		if (contentObjectStatefulSearchService.getSearchResultSetSize() > 0)
			contentObjectStatefulSearchService.getReturnedContentObjects().reset();
		else {
			contentObjectStatefulSearchService.setReturnedContentObjects(null);
			uiComponentBinding.setListViewContentObjectTableComponent(null);
			uiComponentBinding.setListViewContentObjectTableScrollerComponent(null);
		}
	
	}
	
	public void deleteContentObjectsInList(ContentObjectCriteria contentObjectCriteria) {
			
			// run the query
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (cmsOutcome.getCount() == 0) {
				JSFUtilities.addMessage(null, "object.action.bulk.delete.message.no.object.selected", null, FacesMessage.SEVERITY_WARN);
				return;
			}
			
			List<ContentObject> contentObjectList = cmsOutcome.getResults();
			
			ContentObjectUIWrapper contentObjectUIWrapper = null;
			
			DeletionContext deletionContext = new DeletionContext();
			
			for (ContentObject contentObject : contentObjectList)
			{
				if (contentObjectUIWrapper == null)
				{
					contentObjectUIWrapper = contentObjectUIWrapperFactory.getInstanceWithoutProxies(contentObject);
				}
				else
				{
					contentObjectUIWrapper.setContentObject(contentObject);
				}
				
				deleteContentObject(contentObjectUIWrapper, deletionContext);
			}
			
			printMessages(deletionContext);
			
			if (deletionContext.atLeastOneContentObjectWasDeletedWithSuccess())
			{
				resetContentObjectList(deletionContext);
			}
	}
	
	private void printMessages(DeletionContext deletionContext)
	{
		if (CollectionUtils.isNotEmpty(deletionContext.getContentObjectsWhichWereNotDeletedDueToUnauthorizedAccess()))
		{
			
			JSFUtilities.addMessage(null, "object.action.bulk.delete.message.unauthorized.delete.message",null, FacesMessage.SEVERITY_WARN);
			
			for (String title : deletionContext.getContentObjectsWhichWereNotDeletedDueToUnauthorizedAccess())
			{
				JSFUtilities.addMessage(null, title, FacesMessage.SEVERITY_WARN);
			}
		}
		
		if (CollectionUtils.isNotEmpty(deletionContext.getContentObjectsWhichWereNotDeletedDueToError()))
		{
			
			JSFUtilities.addMessage(null, "object.action.bulk.delete.message.theFollowingObjectsNotDeletedDueToError",null, FacesMessage.SEVERITY_WARN);
			
			for (String title : deletionContext.getContentObjectsWhichWereNotDeletedDueToError())
			{
				JSFUtilities.addMessage(null, title, FacesMessage.SEVERITY_WARN);
			}
		}
		
		if (deletionContext.atLeastOneContentObjectWasDeletedWithSuccess())
		{
			if (deletionContext.atLeastOneContentObjectWasNotDeleted())
			{
				JSFUtilities.addMessage(null, "object.action.bulk.delete.message.partial.success.info.message", null, FacesMessage.SEVERITY_INFO);
			}
			else
			{
				JSFUtilities.addMessage(null, "object.action.bulk.delete.message.all.success.info.message",null, FacesMessage.SEVERITY_INFO);
			}
		}
	}
	
	private boolean deleteContentObject(ContentObjectUIWrapper contentObjectUIWrapper, DeletionContext deletionContext) {
		
		
		if (contentObjectUIWrapper.isLoggedInUserAuthorizedToDeleteContentObject())
		{
			try{
				final Calendar createdDate = ((CalendarProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.created")).getSimpleTypeValue();

				astroboaClient.getContentService().deleteContentObject(contentObjectUIWrapper.getContentObject().getId());
				
				deletionContext.increaseDeletedContentObjectCounter();

				try{
					Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_DELETED, 
						new Object[]{contentObjectUIWrapper.getContentObject().getContentObjectType(), 
						contentObjectUIWrapper.getContentObject().getId(), 
						createdDate});
				}
				catch(Exception e)
				{
					//Just log exception. Continue with other deletions
					logger.error("Something went wrong when raising content object delete event. ContentObject {} was successfully deleted",
							contentObjectUIWrapper.getContentObject().getId());
				}
		
				return true;
			}
			catch(Exception e)
			{
				logger.error("",e);
				
				deletionContext.addContentObjectsWhichWereNotDeletedDueToError(((StringProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue());
				
				return false;
			}
		}
		else
		{
			deletionContext.addContentObjectsWhichWereNotDeletedDueToUnauthorizedAccess(((StringProperty)contentObjectUIWrapper.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue());
			
			return false;
		}
		
	}

	private class DeletionContext
	{
		private List<String> contentObjectsWhichWereNotDeletedDueToUnauthorizedAccess = new ArrayList<String>();
		
		private List<String> contentObjectsWhichWereNotDeletedDueToError = new ArrayList<String>();
		
		private int deletedContentObjectCounter = 0;;
		private boolean atLeastOneContentObjectWasNotDeleted = false;
		
		public List<String> getContentObjectsWhichWereNotDeletedDueToUnauthorizedAccess() {
			return contentObjectsWhichWereNotDeletedDueToUnauthorizedAccess;
		}
		
		public void addContentObjectsWhichWereNotDeletedDueToUnauthorizedAccess(
				String message) {
			
			if (StringUtils.isNotBlank(message))
			{
				contentObjectsWhichWereNotDeletedDueToUnauthorizedAccess.add(message);
				atLeastOneContentObjectWasNotDeleted = true;
			}
			
		}

		public void addContentObjectsWhichWereNotDeletedDueToError(
				String message) 
		{
			if (StringUtils.isNotBlank(message))
			{
				contentObjectsWhichWereNotDeletedDueToError.add(message);
				atLeastOneContentObjectWasNotDeleted = true;
			}
			
			
		}

		public void increaseDeletedContentObjectCounter() {
			deletedContentObjectCounter++;
			
		}
		public List<String> getContentObjectsWhichWereNotDeletedDueToError() {
			return contentObjectsWhichWereNotDeletedDueToError;
		}
		public boolean atLeastOneContentObjectWasDeletedWithSuccess() {
			return deletedContentObjectCounter > 0;
		}
		public boolean atLeastOneContentObjectWasNotDeleted() {
			return atLeastOneContentObjectWasNotDeleted;
		}

		public int getDeletedContentObjectCounter() {
			return deletedContentObjectCounter;
		}
		
	}
}
