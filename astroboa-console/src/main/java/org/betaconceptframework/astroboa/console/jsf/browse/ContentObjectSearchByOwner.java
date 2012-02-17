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
package org.betaconceptframework.astroboa.console.jsf.browse;


import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.SearchResultsFilterAndOrdering;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author gchomatas
 * Created on Nov 1, 2006
 */
@Name("contentObjectSearchByOwner")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByOwner extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	//Statically Injected Managers
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	private PageController pageController;
	
	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	
	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;

	//these two properties hold the selected repository UUID and name
	private String selectedRepositoryUserId;
	private String selectedRepositoryUserName;
	
	/*
	 * Retrieve the content objects owned by a specific user 
	*/
	public String findContentObjectsByOwnerAndPresent_UIAction(RepositoryUser selectedRepositoryUser) {
		
		
		if (selectedRepositoryUser == null || selectedRepositoryUser.getId() == null){
			return null;
		}
		
		selectedRepositoryUserId = selectedRepositoryUser.getId();
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		uiComponentBinding.resetContentObjectTableScrollerComponent();
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		// We add the selected owner into search criteria
		contentObjectCriteria.addOwnerIdEqualsCriterion(selectedRepositoryUserId);
		
		contentObjectCriteria.setOffsetAndLimit(0, pageController.getRowsPerDataTablePage());
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
		
		//	now we are ready to run the query
		try {
			long startTime = System.currentTimeMillis();
			int resultSetSize = contentObjectStatefulSearchService
				.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), pageController.getRowsPerDataTablePage());
			long endTime = System.currentTimeMillis();
			getLogger().debug(
					"Find Content Objects by OWNER UUID:FIRST EXECUTION to get object count took: "
							+ (endTime - startTime) + "ms");
			
			if (resultSetSize > 0) {
				
				contentObjectList
					.setContentObjectListHeaderMessage(
							JSFUtilities.getParameterisedStringI18n("object.list.message.contentObjectListHeaderMessageForSearchByOwner", 
									new String[] {selectedRepositoryUser.getLabel(), String.valueOf(resultSetSize)}));
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(selectedRepositoryUser.getLabel());
			}
			else JSFUtilities.addMessage(null, "object.list.message.noContentObjectsOwnedByThisRepositoryUser", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			logger.error("Error while loading content objects ", e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
		return null;
	}
			
			
	
	public String getSelectedRepositoryUserName() {
		return selectedRepositoryUserName;
	}
	
	public void setSelectedRepositoryUserName(String selectedRepositoryUserName) {
		this.selectedRepositoryUserName = selectedRepositoryUserName;
	}

	public void setSelectedRepositoryUserId(String selectedRepositoryUserId) {
		this.selectedRepositoryUserId = selectedRepositoryUserId;
	}	
	

	//injected objects setters
	
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
	
	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

}
