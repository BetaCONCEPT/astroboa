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


import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.SearchResultsFilterAndOrdering;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
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
@Name("contentObjectSpecificSearchQueries")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSpecificSearchQueries extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	// Statically Injected Beans
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	private PageController pageController;
	
	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	/*
	 * Retrieve all content objects ordered by View Counter  
	*/
	public String orderContentObjectsByViewCounterAndPresent_UIAction() {
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		uiComponentBinding.resetContentObjectTableScrollerComponent();
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		
		// we are searching only for content objects which have been viewed at least once
		Criterion viewCounterCriterion = CriterionFactory.greaterThan("statistic.viewCounter", 0);
		
		contentObjectCriteria.addCriterion(viewCounterCriterion);
		
		// we would like to sort returned articles according to view counter in descending order
		contentObjectCriteria.addOrderProperty("viewCounter", Order.descending);
		
		contentObjectCriteria.setOffsetAndLimit(0, pageController.getRowsPerDataTablePage());
		
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
					"Return Content Objects ordered by View Counter: FIRST EXECUTION to get object count took: "
							+ (endTime - startTime) + "ms");
			
			if (resultSetSize > 0) {
				//JSFUtilities.addMessage(null, "contentSearch.searchByTopicInfo", new String[] {getSelectedTopicLabel(), String.valueOf(numberOfReturnedContentObjects)}, FacesMessage.SEVERITY_INFO);
				contentObjectList
				.setContentObjectListHeaderMessage(
						JSFUtilities.getParameterisedStringI18n("object.list.message.contentObjectListHeaderMessageForOrderByViewCounter", 
								new String[] {String.valueOf(resultSetSize)}));
			}
			else JSFUtilities.addMessage(null, "object.list.message.noContentObjectswhichHaveBeenViewedAtLeastOneTime", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			logger.error("Error while loading content objects ",e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
		return null;
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
	
	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

}
