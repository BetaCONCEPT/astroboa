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


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.SearchResultsFilterAndOrdering;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingContentObjectFolderTreeNodeRichFaces;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * @author gchomatas
 * Created on Nov 1, 2006
 */
@Name("contentObjectSearchByContentType")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByContentType extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	//Statically Injected Beans
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;
	private PageController pageController;
	
	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	
	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;

	/*
	 * Retrieve the content objects owned by a specific user 
	*/
	public String findContentObjectsByContentTypeAndPresent_UIAction() {
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		uiComponentBinding.resetContentObjectTableScrollerComponent();
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		
		// We add the selected content type into search criteria
		HtmlTree contentObjectFolderTree = uiComponentBinding.getContentObjectFolderTreeRichFaces();
		TreeNode selectedTreeNodeObject = contentObjectFolderTree.getTreeNode();
		LazyLoadingContentObjectFolderTreeNodeRichFaces selectedNode;
		
		// There is possibly a bug in Rich Faces Tree Implementation. The first time a tree node is selected in the tree the selected tree node
		// that is returned has type <LazyLoadingContentObjectFolderTreeNodeRichFaces> as it should be. However any subsequent selected tree node
		// is returned as a <TreeNodeImpl> object which should not happen. 
		// So we should check the returned type to decide how we will access the selected tree node.
		if (LazyLoadingContentObjectFolderTreeNodeRichFaces.class.isInstance(selectedTreeNodeObject)) {
			selectedNode = (LazyLoadingContentObjectFolderTreeNodeRichFaces) contentObjectFolderTree.getTreeNode();
		}
		else if (TreeNodeImpl.class.isInstance(selectedTreeNodeObject)) {
			selectedNode =
				(LazyLoadingContentObjectFolderTreeNodeRichFaces) selectedTreeNodeObject.getData();
		}
		else 
			throw new RuntimeException("Cannot determine the class of the selected tree node");
		
		contentObjectCriteria.addContentObjectTypeEqualsCriterion(selectedNode.getContentType());
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();
		
		// We should find and add the selected date into search criteria if a date folder has been selected
		String message = "";
		
		if (!selectedNode.getType().equals("contentTypeFolder")) {
			String fullContentObjectFolderPath = selectedNode.getContentObjectFolder().getFullPath(); //the full path contains date information. Its format is the following: YYYY/M/D eg. 2006/5/2
			String[] dateComponents = StringUtils.split(fullContentObjectFolderPath, "/"); // we split the path to the year, month and day components

			if ( dateComponents == null || dateComponents.length == 0 || dateComponents.length > 3){
				logger.error("Error while loading content objects. Invalid path for content object folder {}",  fullContentObjectFolderPath);
				JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError",null, FacesMessage.SEVERITY_ERROR);
				return null;
			}

			GregorianCalendar fromDate = new GregorianCalendar(TimeZone.getDefault());
			GregorianCalendar toDate = new GregorianCalendar(TimeZone.getDefault());

			//Find out whether user has clicked a year, a month or a day
			if (dateComponents.length == 1){
				//User has selected year.
				//Find all content objects created the selected year
				//from 01/01/selectedYear
				//to   31/12/selectedYear
				fromDate.set(Integer.valueOf(dateComponents[0]), 0, 1, 0, 0, 0);
				toDate.set(Integer.valueOf(dateComponents[0]), 11, 31, 23, 59, 59);

				message = DateUtils.format(fromDate, "dd/MM/yyyy") + " - "+ DateUtils.format(toDate, "dd/MM/yyyy"); 
			}
			else if (dateComponents.length == 2){
				//User has selected month.
				//Find all content objects created the selected year
				//from 01/selectedMonth/selectedYear
				//to   31/selectedMonth/selectedYear

				//Fix Month information
				//In  CmsBackEnd. month enumeration starts from 1 for January, 2 for February .etc
				//Where as in Java Calendar month enumeration is zero based. Thus we need a conversion
				String month = dateComponents[1];
				int javaMonth = Integer.valueOf(month) - 1;

				fromDate.set(Integer.valueOf(dateComponents[0]), javaMonth, 1, 0, 0, 0);
				toDate.set(Integer.valueOf(dateComponents[0]), javaMonth, fromDate.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

				message = DateUtils.format(fromDate, "dd/MM/yyyy") + " - "+ DateUtils.format(toDate, "dd/MM/yyyy");
			} 
			else {
				//User has selected a day
				//from selectedDay at midnight
				//to selectedDay at 23:59:59.999
				//Fix Month information
				//In  CmsBackEnd. month enumeration starts from 1 for January, 2 for February .etc
				//Where as in Java Calendar month enumeration is zero based. Thus we need a conversion
				String month = dateComponents[1];
				int javaMonth = Integer.valueOf(month) - 1;

				// we create a date range for the selected date (24 hours)
				fromDate.set(Integer.valueOf(dateComponents[0]), javaMonth, Integer.valueOf(dateComponents[2]), 0, 0, 0);
				toDate.set(Integer.valueOf(dateComponents[0]), javaMonth, Integer.valueOf(dateComponents[2]), 23, 59, 59);


				message = DateUtils.format(fromDate, "dd/MM/yyyy");
			}

			//Above method is setting 0 to hour, minute and second but we also need to clear milliseconds
			fromDate.clear(Calendar.MILLISECOND);
			//System.out.println(DateUtils.format(fromDate, "dd/MM/yyyy HH:mm"));

			toDate.set(Calendar.MILLISECOND, 999);
			//System.out.println(DateUtils.format(toDate, "dd/MM/yyyy HH:mm"));

			contentObjectCriteria.addCriterion(CriterionFactory.between("profile.created", fromDate, toDate));

		}
		
		contentObjectCriteria.setOffsetAndLimit(0, pageController.getRowsPerDataTablePage());
		
		// set required ordering
		if (searchResultsFilterAndOrdering.getSelectedResultsOrder() != null) {
			contentObjectCriteria.addOrderProperty(
					"profile.created",
					Order.valueOf(searchResultsFilterAndOrdering.getSelectedResultsOrder()));
		}
		else {
			contentObjectCriteria.addOrderProperty(
					"profile.created",
					Order.descending);
		}
		
		
		//	now we are ready to run the query
		try {
			long startTime = System.currentTimeMillis();
			int resultSetSize = contentObjectStatefulSearchService
				.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), pageController.getRowsPerDataTablePage());
			long endTime = System.currentTimeMillis();
			getLogger().debug(
					"Find Objects by ContentObject Type and Date: "
							+ (endTime - startTime) + "ms");

			if (resultSetSize > 0) {
				
				
				String localisedNameOfBrowsedContentType = selectedNode.getLocalizedLabelOfContentType();
				if ("".equals(message)) {
					contentObjectList
						.setContentObjectListHeaderMessage(JSFUtilities.getParameterisedStringI18n("object.list.message.contentObjectListHeaderMessageForSearchByContentTypeWithoutDate", 
							new String[] {localisedNameOfBrowsedContentType, String.valueOf(resultSetSize)}));
				}
				else {
					contentObjectList
						.setContentObjectListHeaderMessage(JSFUtilities.getParameterisedStringI18n("object.list.message.contentObjectListHeaderMessageForSearchByContentType", 
								new String[] {localisedNameOfBrowsedContentType, message, String.valueOf(resultSetSize)}));
				}
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(localisedNameOfBrowsedContentType);
			}
			else
				JSFUtilities.addMessage(null, "object.list.message.noContentObjectsInThisContentObjectFolderInfo", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			logger.error("Error while loading content objects ", e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError",null,FacesMessage.SEVERITY_ERROR);
		}
		
		return null;
		
	}
			
	//injected objects setters 
	
	public void setContentObjectList(
			ContentObjectList contentObjectList) {
		this.contentObjectList = contentObjectList;
	}

	public void setSearchResultsFilterAndOrdering(
			SearchResultsFilterAndOrdering searchResultsFilterAndOrdering) {
		this.searchResultsFilterAndOrdering = searchResultsFilterAndOrdering;
	}

	public void setContentObjectStatefulSearchService(
			ContentObjectStatefulSearchService contentObjectStatefulSearchService) {
		this.contentObjectStatefulSearchService = contentObjectStatefulSearchService;
	}
	
	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

}
