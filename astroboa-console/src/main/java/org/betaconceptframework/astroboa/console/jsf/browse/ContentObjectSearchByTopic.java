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

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
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
/* handles searching of content objects by topic */
@Name("contentObjectSearchByTopic")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByTopic extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;
	
	// Injected objects
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	private PageController pageController;

	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;
	
	// holds the UUID of the user selected topic by which we search content objects
	private String selectedTopicId;
	
	// holds the label of the user selected topic by which we search content objects
	private String selectedTopicLabel;
	
	//Holds the name of selectedTopic;s taxonomy
	private String selectedTopicTaxonomy;
	
	
	public void findContentObjectsByTopicAndPresent_UIAction(Topic topic) {	
		selectedTopicId = topic.getId();
		selectedTopicLabel = topic.getLocalizedLabelForCurrentLocale();
		
		if (topic.getTaxonomy() != null){
			selectedTopicTaxonomy = topic.getTaxonomy().getName();
		}
		else{
			selectedTopicTaxonomy = null;
		}
		findContentObjectsByTopicIdAndPresent_UIAction();
	}
	
	public String findContentObjectsByTopicIdAndPresent_UIAction() {
		// reset search criteria to begin a new search
		setContentObjectCriteria(null);
		setContentObjectCriteria(CmsCriteriaFactory.newContentObjectCriteria());
		
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		
		try {
			
			if (selectedTopicId == null) {
				throw new Exception("Δεν ήταν δυνατόν να ανακτηθεί το id της θεματικής ενότητας που επιλέξατε. Πρόκειται για σφάλμα της Διεπαφής Χρήστη");
			}
						
			// We add the selected topic into search criteria
			//Criterion topicCriterion = CriterionFactory.equals("profile.subject", selectedTopicId);
			//contentObjectCriteria.addCriterion(topicCriterion);
			contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, selectedTopicId, QueryOperator.EQUALS, false));
			
			logger.debug("Looking for content object which relate to topic {} and belong to taxonomy {}", selectedTopicId, selectedTopicTaxonomy);
			
			// We add a date range for searching back in order to minimize search time.
			// if thousand content objects refer to the selected topic then ordering them will add a very high time cost.
			// The common case is that the user requires to see just a few of the most recently added or modified objects.
			// So we use a default number of days to look back and let the user decide if she wants to go more days back adding more overhead in the query.
			// Criterion dateCriterion = CriterionFactory.greaterThanOrEquals("profile.modified", date);

			/* we set the result set size so that the fist 100 objects are returned.
			 * We do this search to get the number of matched content objects and fill the first page of results. 
			*/
			//contentObjectCriteria.getResultRowRange().setRange(0,100);
			//It should be 99 as it is zero based
			contentObjectCriteria.setOffsetAndLimit(0, pageController.getRowsPerDataTablePage());
			contentObjectCriteria.doNotCacheResults();
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			
			// set required ordering
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
			
			// now we are ready to run the query
			long startTime = System.currentTimeMillis();
			int resultSetSize = contentObjectStatefulSearchService
				.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), pageController.getRowsPerDataTablePage());
			long endTime = System.currentTimeMillis();
			getLogger().info("Find Content Objects by Topic UUID:FIRST EXECUTION to get object count took: " + (endTime - startTime) + "ms");
							
			if (resultSetSize > 0) {
				//JSFUtilities.addMessage(null, "contentSearch.searchByTopicInfo", new String[] {getSelectedTopicLabel(), String.valueOf(numberOfReturnedContentObjects)}, FacesMessage.SEVERITY_INFO);
				contentObjectList
				.setContentObjectListHeaderMessage(
						JSFUtilities.getParameterisedStringI18n("content.search.contentObjectListHeaderMessageForSearchByTopic", 
								new String[] {selectedTopicLabel, String.valueOf(resultSetSize)}));
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(selectedTopicLabel);
			}
			else {
				JSFUtilities.addMessage(null, "content.search.noContentObjectsRelatedToThisTopicInfo", null, FacesMessage.SEVERITY_INFO);
			}
		} catch (Exception e) {
			logger.error("Error while loading content objects ",e);
			JSFUtilities.addMessage(null, "content.search.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
		
		return null;
	}
	
	

	public void setContentObjectCriteria(ContentObjectCriteria contentObjectCriteria) {
		this.contentObjectCriteria = contentObjectCriteria;
	}
	
		
	//	 all properties related to user selections in order to perfom the search 
	public void setSelectedTopicId(String selectedTopicId) {
		this.selectedTopicId = selectedTopicId;
	}
	
	public void setSelectedTopicLabel(String selectedTopicLabel) {
		this.selectedTopicLabel = selectedTopicLabel;
	}
	
	// jsf injected objects setters and getters 
	
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


	public void setSelectedTopicTaxonomy(String selectedTopicTaxonomy) {
		this.selectedTopicTaxonomy = selectedTopicTaxonomy;
	}
	
	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}
	
}
