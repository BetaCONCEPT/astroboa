/**
 * Copyright 2006 BetaCONCEPT
 * Created on Nov 1, 2006
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


import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.SearchResultsFilterAndOrdering;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


/**
 * @author gchomatas
 *
 */
@Name("contentObjectSearchByText")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByText extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;
	
	// Statically Injected objects
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	
	
	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;
	
	
	// holds the text string by which we are searching
	private String searchedText;

	
	/*
	 * search into the repository for objects that their text fields or their binary data contain a certain text string
	 */
	public String findContentObjectsByTextAndPresent_UIAction() {
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		if (uiComponentBinding != null)
			uiComponentBinding.resetContentObjectTableScrollerComponent();
		
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		if (StringUtils.isBlank(searchedText)) {
			//In case a new page is rendered, that is TaxonomyEditpage was loaded and
			//user clicked to search content objects with an empty string this message won't be 
			//loaded to the new page
			JSFUtilities.addMessage(null, "Πρέπει να συμπληρώσετε τις λέξεις που αναζητάτε", FacesMessage.SEVERITY_WARN);
			return null;
		}
			
		
		// add searched text to the criteria
		if (StringUtils.deleteWhitespace(searchedText).equals(searchedText) && ! searchedText.contains("\"") && ! searchedText.contains("'")){
			//Search Text contains only one word.Append with * at the end
			contentObjectCriteria.addFullTextSearchCriterion(searchedText+CmsConstants.ANY_NAME);
		}
		else{
			contentObjectCriteria.addFullTextSearchCriterion(searchedText);
		}
		
		/* we set the result set size so that the fist 100 objects are returned.
		 * We do this search to get the number of matched content objects and fill the first page of results. 
		*/
		//contentObjectCriteria.getResultRowRange().setRange(0,99);
		contentObjectCriteria.setOffsetAndLimit(0,99);
		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		// set required ordering
		if (searchResultsFilterAndOrdering.getSelectedResultsOrder() != null) {
			contentObjectCriteria.addOrderProperty(
					"profile.modified",
					Order.valueOf(searchResultsFilterAndOrdering.getSelectedResultsOrder()));
		}
		else {
			contentObjectCriteria.addOrderProperty(
					"profile.modified",
					Order.descending);
		}
		
		//contentObjectCriteria.addOrderByRelevance(Order.descending);
		
		// now we are ready to run the query
		try {
			long startTime = System.currentTimeMillis();
			int resultSetSize = contentObjectStatefulSearchService
				.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), 100);
			long endTime = System.currentTimeMillis();
			getLogger().debug("Find Content Objects by Topic UUID:FIRST EXECUTION to get object count took: " + (endTime - startTime) + "ms");
			
			if (resultSetSize > 0) {
				
				contentObjectList
				.setContentObjectListHeaderMessage(
						JSFUtilities.getParameterisedStringI18n("content.search.contentObjectListHeaderMessageForTextSearch", 
								new String[] {searchedText, String.valueOf(resultSetSize)}));
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(searchedText);
			}
			else JSFUtilities.addMessage(null, "content.search.noContentObjectsContainingThisTextInfo", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			logger.error("Error while loading content objects ", e);
			JSFUtilities.addMessage(null, "content.search.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
			return null;
		}
		
		return null;
	}
			

	
	public String getSearchedText() {
		return searchedText;
	}

	public void setSearchedText(String searchedText) {
		this.searchedText = searchedText;
	}
	
	
	
	// injected objects setters and getters 

	public void setContentObjectList(
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

}
