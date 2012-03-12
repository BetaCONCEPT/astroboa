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
package org.betaconceptframework.astroboa.console.jsf.dashboard;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.DataPage;
import org.betaconceptframework.ui.jsf.PagedListDataModel;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.core.Events;

/**
 * It is the backing bean of a list of content objects as a
 * result of specific criteria. Extended subclasses only have to provide
 * the necessary content object criteria and to perform specific ordering to results
 * if necessary.
 * 
 * Class also provides a method for deleting a selected content object.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class ContentObjectListBean  extends AbstractUIBean{

	private static final long serialVersionUID = 1L;
	
	protected ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	protected ContentService contentService;
	
	protected ContentObjectCriteria contentObjectCriteria;
	
	// holds the total size of results which were returned by the query. It is
	// required in order to built the result pages
	private int searchResultSetSize;

	private ContentObjectDataModel returnedContentObjects;
	

	protected void searchForContentObjectWithPagedResultsUsingQuery(String query){
		
		contentObjectCriteria.setXPathQuery(query);
		
		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		processContentObjectQueryOutcome(cmsOutcome);
	}
	
	//Executes content object criteria set by subclass and creates appropriate data model
	protected void searchForContentObjectWithPagedResults(){
		
		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		processContentObjectQueryOutcome(cmsOutcome);
	}
	
	private void processContentObjectQueryOutcome(CmsOutcome<ContentObject> cmsOutcome){
		if (cmsOutcome.getCount() > 0) { // create the Lazy loading Data
			// Model only if there are results

//			we reset instead of nullify the results variable since nullification causes data scroller to not synchronize with the new data model 
			if (returnedContentObjects != null)
				returnedContentObjects.reset();
			else
				returnedContentObjects = new ContentObjectDataModel(100);

			searchResultSetSize = (int) cmsOutcome.getCount();


			List<ContentObject> cmsOutcomeRowList = orderResults(cmsOutcome.getResults());

			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			
			for (ContentObject cmsOutcomeRow : cmsOutcomeRowList) {
				wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(cmsOutcomeRow));
			}

			DataPage<ContentObjectUIWrapper> dataPage = new DataPage<ContentObjectUIWrapper>(getSearchResultSetSize(), 0, wrappedContentObjects);

			returnedContentObjects.setPage(dataPage);
		}
		else{
			returnedContentObjects = null; // if no results are found we nullify the results variable to prevent the data scroller from trying to iterate through the data model

		}
	}
	
	
	protected abstract List<ContentObject> orderResults(List<ContentObject> results) ;


	//Deletes content object and refreshes list
	public void permanentlyRemoveSelectedContentObject_UIAction(String selectedContentObjectIdentifier, String contentObjectType, Calendar dayToRefresh) {
		
		try {
			contentService.deleteContentObject(selectedContentObjectIdentifier);
			
			Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_DELETED,new Object[]{contentObjectType, selectedContentObjectIdentifier, dayToRefresh});
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "object.edit.contentObjectCouldNotBePermanentlyRemovedError", null, FacesMessage.SEVERITY_ERROR);
			getLogger().error("The content object could not be permanently deleted. The error is: " , e);
			e.printStackTrace();
			return;
		}
		
		// generate a success message, reset the browsing trees to accommodate the change and finally change the view to show the conentObjectListPanel 
		JSFUtilities.addMessage(null, "object.edit.successful.delete.info.message", null, FacesMessage.SEVERITY_INFO);
		
	}

	//Each time a Content object is deleted this method will run for each subclass
	//and will reset its current content object data model
	@Observer({SeamEventNames.CONTENT_OBJECT_DELETED})
	public void resetAfterObjectRemoval() {
		searchResultSetSize -= 1;

		if (returnedContentObjects != null){
			
			if (searchResultSetSize > 0){
				returnedContentObjects.reset();
			}
			else{
				returnedContentObjects = null;
			}

		}
		
	}
	
	public void clear() {
		returnedContentObjects = null;
	}
	
	public int getSearchResultSetSize() {
		return searchResultSetSize;
	}
	
	public class ContentObjectDataModel extends PagedListDataModel<ContentObjectUIWrapper> {
		public ContentObjectDataModel(int pageSize) {
			super(pageSize);
		}

		public DataPage<ContentObjectUIWrapper> fetchPage(int startRow,
				int pageSize) {
			try {
				List<ContentObjectUIWrapper> wrappedContentObjects;
				//Limit now is always the same
				//getLocalContentObjectCriteria().getResultRowRange().setRange(startRow, startRow + pageSize - 1);
				//contentObjectCriteria.getResultRowRange().setRange(startRow, pageSize);
				contentObjectCriteria.setOffsetAndLimit(startRow, pageSize);

				long startTime = System.currentTimeMillis();
				CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
				long endTime = System.currentTimeMillis();
				getLogger().debug(
						"Content Object Results Pager fetched the next 100 objects in: "
								+ (endTime - startTime) + "ms");

				if (cmsOutcome.getCount() > 0) {
					List<ContentObject> cmsOutcomeRowList = cmsOutcome.getResults();
					// List<ContentObject> contentObjects =
					// getContentManager().getContentObjectByTopicUUID(selectedTopics,
					// getResultsOrder(), new RowRange(startRow, startRow +
					// pageSize -1), false, getOwnerUUIDsFilterList(),
					// Condition.OR, getLocaleAsString()).getResults();
					wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
					for (ContentObject cmsOutcomeRow : cmsOutcomeRowList) {
						wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(cmsOutcomeRow));
					}
					DataPage<ContentObjectUIWrapper> dataPage = new DataPage<ContentObjectUIWrapper>(
							getSearchResultSetSize(), startRow,
							wrappedContentObjects);
					return dataPage;
				} else {
					JSFUtilities
							.addMessage(
									null,
									"The results pager retreived zero content objects for the requested result page. This should not happen and indicates an application problem. Please report the error to help desk",
									FacesMessage.SEVERITY_WARN);
					return null;
				}

			} catch (Exception e) {
				JSFUtilities.addMessage(null,
						"object.list.message.contentObjectRetrievalError",
						null,
						FacesMessage.SEVERITY_WARN);
				logger.error("Error while loading content objects ",e);
				return null;
			}
		}

	}

	
	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	public ContentObjectDataModel getReturnedContentObjects() {
		return returnedContentObjects;
	}

	
}
