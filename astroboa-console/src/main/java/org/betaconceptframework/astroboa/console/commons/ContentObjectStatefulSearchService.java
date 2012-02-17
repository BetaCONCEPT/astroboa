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
package org.betaconceptframework.astroboa.console.commons;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.DataPage;
import org.betaconceptframework.ui.jsf.PagedListDataModel;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.contexts.Contexts;

/**
 * This class provides stateful as well as stateless content search facilities 
 * for front-end applications (i.e. those which present content to the user through a web interface.)
 * It is a session spring bean and thus can be used in the context of a web application.
 * It is merely a set of convenience methods to be used along the Astroboa ContentService API.
 * The most useful ones are those with the suffix "WithPagedResults" which facilitate the retrieval of
 * arbitrarily big number of results in pages which are lazily loaded and thus do not overload the system
 * memory. Utilizing these methods when building UIs is very useful for the scalability of the solution.
 * As a penalty for the lazy loading comes the statefulness of the class since the state of the criteria and the paged results should
 * be remembered across sessions. In a later release we will turn this object into a conversation scoped Seam bean to allow applications with
 *  multiple windows  to open search conversations in multiple windows (see remarks on localContentObjectCriteria for more details).
 *  Also there are some other stateful methods with the suffix "KeepResults" which do not produce paged results but keep the results in the bean
 *  to allow retrieval by presentation objects.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 */
public class ContentObjectStatefulSearchService extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	// Injected Services
	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	
	/*
	 * This object holds all the search criteria upon which searching/browsing
	 * for content objects in the repository is done This object is a pointer to
	 * the ContentObjectCriteria object that is passed to the
	 * searchForContentWithPagedResults method We need this copy in order to
	 * execute the query again when a new page of results is required IMPORTANT
	 * NOTICE: This object is a spring session bean holding the state of the user query
	 * and allowing to execute the same query along page requests to bring new
	 * pages of query results Therefore multiple simultaneous queries through
	 * different browser windows are not supported. We will always suppose that
	 * the user works in a single window and sequentially goes from a query to a
	 * new query. In the next release we will make it a Seam Conversation Bean to allow multiple
	 * queries in different windows. 
	 * The localContentObjectCriteria is a copy of the initially provided criteria
	 * in order to prevent inconsistent queries if for some reason the initial object is accidentally altered 
	 * while the user browses result pages.
	 */
	private ContentObjectCriteria localContentObjectCriteria;


	// holds the total size of results which were returned by the query. It is
	// required in order to built the result pages
	private int searchResultSetSize;

	// holds the paged search results
	private PagedListDataModel<ContentObjectUIWrapper> returnedContentObjects;

	// holds NON paged search results
	private List<ContentObjectUIWrapper> nonPagedReturnedContentObjects;

	
	//holds selected content objects
	private ContentObjectSelectionBean contentObjectSelection = new ContentObjectSelectionBean();
	

	/**	We return the result to the calling object AND we also keep the results
	 * for further reference by presentation objects.
	 * The method is mainly indented for use by web UI code for retrieval of published objects.
	*/ 
	public List<ContentObjectUIWrapper> searchForContentAndKeepResults(
			ContentObjectCriteria contentObjectCriteria,
			boolean useDefaultRenderProperties, String locale) throws CmsException {
		if (useDefaultRenderProperties)
			setDefaultRenderPropertiesToContentObjectCriteria(contentObjectCriteria, locale);
		CmsOutcome<ContentObject> cmsOutcome = contentService
				.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		if (cmsOutcome.getCount() > 0) {
			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			List<ContentObject> cmsOutcomeRowList = cmsOutcome.getResults();
			
			for (ContentObject contentObject : cmsOutcomeRowList) {
				wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(
						contentObject));
			}
			setNonPagedReturnedContentObjects(wrappedContentObjects);
		} else
			setNonPagedReturnedContentObjects(null);

		return getNonPagedReturnedContentObjects();
	}
	
	
	/**
	 * We just return the result to the calling object. We do not keep the
	 * results for further reference.
	 * @param contentObjectCriteria
	 * @return
	 * @throws CmsException
	 */ 
	public List<ContentObjectUIWrapper> searchForContent(
			ContentObjectCriteria contentObjectCriteria,
			boolean useDefaultRenderProperties, String locale) throws CmsException {
		
		if (useDefaultRenderProperties)
			setDefaultRenderPropertiesToContentObjectCriteria(contentObjectCriteria, locale);
		
		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		if (cmsOutcome.getCount() > 0) {
			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			List<ContentObject> cmsOutcomeRowList = cmsOutcome.getResults();
			
			for (ContentObject contentObject : cmsOutcomeRowList) {
				wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(contentObject));
			}
			
			return wrappedContentObjects;
		} else
			return null;
		
	}

	
	/*
	public CmsOutcome<CmsSearchResult> searchByTopicIdWithUserFiltersUserOrderingAndPagedResults(
			ContentObjectCriteria contentObjectCriteria) throws CmsException {
		setLocalContentObjectCriteria(contentObjectCriteria);
		// check if user filters are enabled
		if (isFilteringDuringBrowsingEnabled())
			// when browsing repository by Topic then all filters can be applied
			setCriteriaFromUserSelection();

		return searchForContentWithSecurityAndWithPagedResults(
				getLocalContentObjectCriteria(), true, null);
	}

	public CmsOutcome<CmsSearchResult> searchByOwnerUUIDWithUserFiltersUserOrderingAndPagedResults(
			ContentObjectCriteria contentObjectCriteria) throws CmsException {
		setLocalContentObjectCriteria(contentObjectCriteria);
		// check if user filters are enabled
		if (isFilteringDuringBrowsingEnabled()) {
			// when browsing repository by Owner then owner filter can not be
			// applied
			// we will apply only content type, text and date filters
			setCriteriaFromContentObjectTypeSelection();
			setCriteriaFromTextSelection();
			setCriteriaFromDateSelection();

			// Inform the user that some filters are not applicable for this
			// type of search
			JSFUtilities
					.addMessage(
							null,
							"application.genericMessage",
							new String[] { "Στην πλοήγηση ανά Ιδιοκτήτη το φίλτρο Ιδιοκτήτη Απενεργοποιείται " },
							FacesMessage.SEVERITY_INFO);
		}

		return searchForContentWithSecurityAndWithPagedResults(
				getLocalContentObjectCriteria(), true, null);
	}

	public CmsOutcome<CmsSearchResult> searchByContentObjectTypeWithUserFiltersUserOrderingAndPagedResults(
			ContentObjectCriteria contentObjectCriteria) throws CmsException {
		setLocalContentObjectCriteria(contentObjectCriteria);
		// check if user filters are enabled
		if (isFilteringDuringBrowsingEnabled()) {
			// when browsing repository by content object type then content
			// object type and date filter can not be applied
			// we will apply only the owner and text filters
			setCriteriaFromOwnerSelection();
			setCriteriaFromTextSelection();

			// Inform the user that some filters are not applicable for this
			// type of search
			JSFUtilities
					.addMessage(
							null,
							"application.genericMessage",
							new String[] { "Στην πλοήγηση ανά Τύπο Περιεχομένου τα Φίλτρα Τύπου Περιεχομένου και Ημερομηνίας Απενεργοποιειούντε " },
							FacesMessage.SEVERITY_INFO);
		}

		return searchForContentWithSecurityAndWithPagedResults(
				getLocalContentObjectCriteria(), true, null);
	}
	*/
	

	
	/** Every search returns the total number of matched objects even if the objects we ask to render back in the result set are less than the total matched
	 * We need the total number of matched objects in order to build the PagedDataModel in the following lines.
	 * The PagedDataModel supports the lazy loading of results and their presentation in pages of a few ones each time so that very large results sets can be presented to the user 
	 * without consuming all the system memory.
	 * The PagedDataModel will automatically set up the result set in each page fetch in order to lazy load pages of content objects as the user iterates 
	 * through the pages of returned content objects
	 */
	public int searchForContentWithPagedResults(
			ContentObjectCriteria contentObjectCriteria,
			boolean useDefaultRenderProperties,
			String locale, int pageSize) throws CmsException {
		
		searchResultSetSize = 0;
		localContentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.copyTo(localContentObjectCriteria);
		
		if (useDefaultRenderProperties)
			setDefaultRenderPropertiesToContentObjectCriteria(localContentObjectCriteria, locale);
		
		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(localContentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		
		if (cmsOutcome.getCount() > 0) { // create the Lazy loading Data
											// Model only if there are results
			
			// we reset instead of nullify the results variable since nullification causes data scroller to not synchronize with the new data model 
			/*
			if (returnedContentObjects != null){
				returnedContentObjects.reset();
				contentObjectSelection.clearAllSelectedContentObjects_UIAction();
			}
			else
				returnedContentObjects = new ContentObjectDataModel(pageSize);
			*/
			returnedContentObjects = new ContentObjectDataModel(pageSize);
			searchResultSetSize = (int) cmsOutcome.getCount();
			
			
			List<ContentObject> cmsOutcomeRowList = cmsOutcome.getResults();
	
			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			for (ContentObject contentObject : cmsOutcomeRowList) {
				wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(contentObject));
			}
			
			DataPage<ContentObjectUIWrapper> dataPage = new DataPage<ContentObjectUIWrapper>(getSearchResultSetSize(), 0, wrappedContentObjects);
			
			returnedContentObjects.setPage(dataPage);
		}
		else{
			returnedContentObjects = null; // if no results are found we nullify the results variable to prevent the data scroller from trying to iterate through the data model
			contentObjectSelection.clearAllSelectedContentObjects_UIAction();
		}
		
		return searchResultSetSize;
	}

	

	
	private void setDefaultRenderPropertiesToContentObjectCriteria(ContentObjectCriteria contentObjectCriteria, String locale) {
		/*
		 * The default render properties when we retrieve content objects are:
		 * the localized labels are retrieved according to the provided locale
		 */
		contentObjectCriteria.getRenderProperties().resetRenderInstructions();
		contentObjectCriteria.getRenderProperties().renderValuesForLocale(locale);
		
	}

	
	public class ContentObjectDataModel extends PagedListDataModel<ContentObjectUIWrapper> {
		public ContentObjectDataModel(int pageSize) {
			super(pageSize);
		}

		public DataPage<ContentObjectUIWrapper> fetchPage(int startRow,
				int pageSize) {
			try {
				// remove all selected objects
				contentObjectSelection.clearAllSelectedContentObjects_UIAction();
				
				List<ContentObjectUIWrapper> wrappedContentObjects;
				//Limit now is always the same
				//getLocalContentObjectCriteria().getResultRowRange().setRange(startRow, startRow + pageSize - 1);
				//localContentObjectCriteria.getResultRowRange().setRange(startRow, pageSize);
				localContentObjectCriteria.setOffsetAndLimit(startRow, pageSize);

				long startTime = System.currentTimeMillis();
				CmsOutcome<ContentObject> cmsOutcome = contentService
						.searchContentObjects(localContentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
				long endTime = System.currentTimeMillis();
				getLogger().debug(
						"Content Object Results Pager fetched the next " + pageSize + " objects in: "
								+ (endTime - startTime) + "ms");

				if (cmsOutcome.getCount() > 0) {
					List<ContentObject> cmsOutcomeRowList = cmsOutcome
							.getResults();
					// List<ContentObject> contentObjects =
					// getContentManager().getContentObjectByTopicUUID(selectedTopics,
					// getResultsOrder(), new RowRange(startRow, startRow +
					// pageSize -1), false, getOwnerUUIDsFilterList(),
					// Condition.OR, getLocaleAsString()).getResults();
					wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
					for (ContentObject contentObject : cmsOutcomeRowList) {
						wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(
								contentObject));
					}
					DataPage<ContentObjectUIWrapper> dataPage = new DataPage<ContentObjectUIWrapper>(
							getSearchResultSetSize(), startRow,
							wrappedContentObjects);
					return dataPage;
				} else {
					logger.error("The results pager retreived zero content objects for the requested result page.");
					JSFUtilities
							.addMessage(
									null,
									"application.unknown.error.message",
									null,
									FacesMessage.SEVERITY_WARN);
					return null;
				}

			} catch (Exception e) {
				JSFUtilities.addMessage(null,
						"application.unknown.error.message",
						null,
						FacesMessage.SEVERITY_WARN); 
				logger.error("Error while loading content objects ", e);
				return null;
			}
		}

	}

	public class ContentObjectDataModelForNonSecuredResults extends PagedListDataModel<ContentObjectUIWrapper> {
		public ContentObjectDataModelForNonSecuredResults(int pageSize) {
			super(pageSize);
		}

		public DataPage<ContentObjectUIWrapper> fetchPage(int startRow,
				int pageSize) {
			try {
				List<ContentObjectUIWrapper> wrappedContentObjects;
				//Limit now is always the same
				//getLocalContentObjectCriteria().getResultRowRange().setRange(startRow, startRow + pageSize - 1);
				//localContentObjectCriteria.getResultRowRange().setRange(startRow, pageSize);
				localContentObjectCriteria.setOffsetAndLimit(startRow, pageSize);

				long startTime = System.currentTimeMillis();
				CmsOutcome<ContentObject> cmsOutcome = contentService
						.searchContentObjects(localContentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
				long endTime = System.currentTimeMillis();
				getLogger().debug(
						"Content Object Results Pager fetched the next " + pageSize + " objects in: "
								+ (endTime - startTime) + "ms");

				if (cmsOutcome.getCount() > 0) {
					List<ContentObject> cmsOutcomeRowList = cmsOutcome
							.getResults();
					// List<ContentObject> contentObjects =
					// getContentManager().getContentObjectByTopicUUID(selectedTopics,
					// getResultsOrder(), new RowRange(startRow, startRow +
					// pageSize -1), false, getOwnerUUIDsFilterList(),
					// Condition.OR, getLocaleAsString()).getResults();
					wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
					for (ContentObject contentObject : cmsOutcomeRowList) {
						wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(
								contentObject));
					}
					DataPage<ContentObjectUIWrapper> dataPage = new DataPage<ContentObjectUIWrapper>(
							getSearchResultSetSize(), startRow,
							wrappedContentObjects);
					return dataPage;
				} else {
					logger.error("The results pager retreived zero content objects for the requested result page");
					JSFUtilities
							.addMessage(
									null,
									"application.unknown.error.message",
									null,
									FacesMessage.SEVERITY_WARN);
					return null;
				}

			} catch (Exception e) {
				JSFUtilities.addMessage(null,
						"application.unknown.error.message",
						null,
						FacesMessage.SEVERITY_WARN);
				logger.error("Error while loading content objects ",e);
				return null;
			}
		}

	}
	
	
	public void changeRowsPerDataTablePage_UIAction(int rowsPerPage){
		PageController pageController = (PageController) JSFUtilities.getBeanFromSpringContext("pageController");
		pageController.changeRowsPerDataTablePage(rowsPerPage);
		// we need to run the query again with a new limit
		localContentObjectCriteria.setOffsetAndLimit(0, rowsPerPage);
		searchForContentWithPagedResults(localContentObjectCriteria, true, JSFUtilities.getLocaleAsString(), rowsPerPage);
		UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
		if (uiComponentBinding != null){
			uiComponentBinding.resetContentObjectTableScrollerComponent();
		}
	}
	
	public void setLocalContentObjectCriteria(
			ContentObjectCriteria localContentObjectCriteria) {
		this.localContentObjectCriteria = localContentObjectCriteria;
	}
	
	public ContentObjectCriteria getLocalContentObjectCriteria() {
		return localContentObjectCriteria;
	}

	public int getSearchResultSetSize() {
		return searchResultSetSize;
	}

	public void setSearchResultSetSize(int searchResultSetSize) {
		this.searchResultSetSize = searchResultSetSize;
	}

	public PagedListDataModel<ContentObjectUIWrapper> getReturnedContentObjects() {
		return returnedContentObjects;
	}

	public void setReturnedContentObjects(
			PagedListDataModel<ContentObjectUIWrapper> returnedContentObjects) {
		this.returnedContentObjects = returnedContentObjects;
	}

	

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public List<ContentObjectUIWrapper> getNonPagedReturnedContentObjects() {
		return nonPagedReturnedContentObjects;
	}

	public void setNonPagedReturnedContentObjects(
			List<ContentObjectUIWrapper> nonPagedReturnedContentObjects) {
		this.nonPagedReturnedContentObjects = nonPagedReturnedContentObjects;
	}

	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}


	/**
	 * @return the contentObjectSelection
	 */
	public ContentObjectSelectionBean getContentObjectSelection() {
		return contentObjectSelection;
	}

	
}
