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
package org.betaconceptframework.astroboa.console.jsf.browse;


import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author gchomatas
 * Created on Nov 1, 2006
 */
/* handles searching of content objects by topic */
@Name("contentObjectSearchByStatus")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByStatus extends AbstractUIBean {
	
	// Statically Injected Beans
	//private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;

	//private PageController pageController;
	private ContentService contentService;
	
	// Dynamically Injected Beans
	//@In(required=false)
	//UIComponentBinding uiComponentBinding;
	
	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;
	
	private List<ContentObjectUIWrapper> contentObjectsSubmittedForWebPublishing;
	private List<ContentObjectUIWrapper> contentObjectsTemporarilyRejectedForReauthoring;
	
	
	public void findContentObjectsSubmittedForWebPublishing_ExpandListener(ActionEvent event) {
		contentObjectsSubmittedForWebPublishing = findContentObjectsByStatus("submitted");
		
		// set dynamic area to content object list presentation
		//pageController.setDynamicUIAreaCurrentPageComponent(DynamicUIAreaPageComponent.ContentObjectList.getDynamicUIAreaPageComponent());
		
		//return null;
	}
	
	public List<ContentObjectUIWrapper> getContentObjectsSubmittedForWebPublishing() {
		return contentObjectsSubmittedForWebPublishing;
	}

	public List<ContentObjectUIWrapper> getContentObjectsTemporarilyRejectedForReauthoring() {
		return contentObjectsTemporarilyRejectedForReauthoring;
	}

	public void findContentObjectsTemporarilyRejectedForReauthoring_ExpandListener(ActionEvent event) {
		contentObjectsTemporarilyRejectedForReauthoring = findContentObjectsByStatus("temporarilyRejectedForReauthoring");
		
		// set dynamic area to content object list presentation
		//pageController.setDynamicUIAreaCurrentPageComponent(DynamicUIAreaPageComponent.ContentObjectList.getDynamicUIAreaPageComponent());
		
		//return null;
	}
	
	public List<ContentObjectUIWrapper> findContentObjectsByStatus(String contentObjectStatus) {
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
		//uiComponentBinding.resetContentObjectTableScrollerComponent();
		//contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		
		try {
			// we are searching for content objects which have their status set to contentObjectStatus
			Criterion statusCriterion = CriterionFactory.equals("profile.contentObjectStatus", contentObjectStatus);
			contentObjectCriteria.addCriterion(statusCriterion);
			
			/* we set the result set size so that the fist 100 objects are returned.
			 * We do this search to get the number of matched content objects and fill the first page of results. 
			*/
			//contentObjectCriteria.getResultRowRange().setRange(0,100);
			//It should be 99 as it is zero based
			contentObjectCriteria.setOffsetAndLimit(0,99);
			contentObjectCriteria.doNotCacheResults();
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			
//			 set required ordering
			//if (searchResultsFilterAndOrdering.getSelectedResultsOrder() != null) {
			//	contentObjectCriteria.addOrderProperty(
			//			"profile.modified",
			//			Order.valueOf(searchResultsFilterAndOrdering.getSelectedResultsOrder()));
			//}
			//else {
			contentObjectCriteria.addOrderProperty("profile.modified",Order.descending);
			//}
			
			// now we are ready to run the query
			long startTime = System.currentTimeMillis();
			//int resultSetSize = contentObjectStatefulSearchService.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), 100);
			CmsOutcome<CmsRankedOutcome<ContentObject>> contentObjectResutls = contentService.searchContentObjects(contentObjectCriteria);
			long endTime = System.currentTimeMillis();
			getLogger().debug("Find Content Objects by Status:FIRST EXECUTION to get object count took: " + (endTime - startTime) + "ms");
			
			
			if (contentObjectResutls != null && contentObjectResutls.getCount() > 0) {
				
				//contentObjectList
				//.setContentObjectListHeaderMessage(
						//JSFUtilities.getParameterisedStringI18n("contentSearch.contentObjectListHeaderMessageForSearchByStatus", 
							//	new String[] {contentObjectStatus, String.valueOf(resultSetSize)}));
				
				List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
				List<CmsRankedOutcome<ContentObject>> cmsOutcomeRowList = contentObjectResutls.getResults();
				
				for (CmsRankedOutcome<ContentObject> cmsOutcomeRow : cmsOutcomeRowList) {
					wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(
							cmsOutcomeRow.getCmsRepositoryEntity()));
				}
				
				// JSFUtilities.addMessage(null,JSFUtilities.getParameterisedStringI18n("contentSearch.contentObjectListHeaderMessageForSearchByStatus",
					//	new String[] {contentObjectStatus, String.valueOf(contentObjectResutls.getCount())}), FacesMessage.SEVERITY_INFO);
				 
				 return wrappedContentObjects;
			}
			else
				return new ArrayList<ContentObjectUIWrapper>();
			//else 
				//JSFUtilities.addMessage(null, "contentSearch.noContentObjectsWithSpecifiedStatusInfo", new String[] {contentObjectStatus}, FacesMessage.SEVERITY_INFO);
			
			
		} catch (Exception e) {
			logger.error("Error while loading content objects ",e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
		
		return null;
		
		
	}

	
	// jsf injected objects setters and getters 
	
	/*public void setContentObjectList(
			ContentObjectList contentObjectList) {
		this.contentObjectList = contentObjectList;
	}

	public void setContentObjectStatefulSearchService(
			ContentObjectStatefulSearchService contentObjectStatefulSearchService) {
		this.contentObjectStatefulSearchService = contentObjectStatefulSearchService;
	}

	public void setSearchResultsFilterAndOrdering(
			SearchResultsFilterAndOrdering searchResultsFilterAndOrdering) {
		this.searchResultsFilterAndOrdering = searchResultsFilterAndOrdering;
	}
	
	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}
	*/
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}

	
}
