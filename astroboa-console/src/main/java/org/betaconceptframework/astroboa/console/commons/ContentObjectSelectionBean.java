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
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.ui.jsf.PagedListDataModel;
import org.richfaces.component.html.HtmlDataTable;

/**
 * 
 * Represents a holder of all selected contentObjects.
 * 
 * It is agnostic of the way contentObjects are displayed (i.e. in a list of in a table).
 * 
 * It contains all necessary methods to manage selected content objects, 
 * i.e. clear list, append list, etc.
 * 
 * Due to implementation reasons class representing contentObjects is
 * {@link ContentObjectUIWrapper}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSelectionBean {

	private List<ContentObjectUIWrapper> selectedContentObjects;
	
	// Convenient variable that is set when at least one row is selected
	// It is used from xhtml page to show or hide actions on selected rows
	private boolean atLeastOneContentObjectIsSelected;
	
	//In cases where all objects in result set are selected (i.e. not all in current page but all in query result set)
	// it is useful to know the total result size. Since we do not actually copy objects in 
	// "selectedContentObjects" list when ALL are selected we cannot get the total result count from the "selectedContentObjects" list
	private int countOfAllPossibleContentObjects;
	
	private int selectedObjectIndex;
	

	public void clearAllSelectedContentObjects_UIAction(){
		selectedContentObjects = null;
		atLeastOneContentObjectIsSelected = false;
	}
	

	public void addSelectedObject_UIAction(PagedListDataModel pagedListDataModel){
		
		ContentObjectUIWrapper selectedObject= ((List<ContentObjectUIWrapper>) pagedListDataModel.getWrappedData()).get(selectedObjectIndex);
		
		if (selectedContentObjects == null) {
			selectedContentObjects = new ArrayList<ContentObjectUIWrapper>();
		}
		
		selectedContentObjects.add(selectedObject);
		
		atLeastOneContentObjectIsSelected = true;

	}
	
	public void removeDeselectedObject_UIAction(PagedListDataModel pagedListDataModel){
		
		ContentObjectUIWrapper deSelectedObject= ((List<ContentObjectUIWrapper>) pagedListDataModel.getWrappedData()).get(selectedObjectIndex);
		
		if (CollectionUtils.isNotEmpty(selectedContentObjects)) {
			selectedContentObjects.remove(deSelectedObject);
			
			if (selectedContentObjects.isEmpty()) {
				atLeastOneContentObjectIsSelected = false;
			}
			else {
				atLeastOneContentObjectIsSelected = true;
			}
		
		}

	}

	
	public void addAllObjectsOfCurrentPage_UIAction(PagedListDataModel pagedListDataModel, HtmlDataTable htmlDataTable){
		
		// shallow copy the list of all objects in the page to the list of selected objects
		selectedContentObjects = new ArrayList<ContentObjectUIWrapper>((List<ContentObjectUIWrapper>) pagedListDataModel.getWrappedData());
		
		atLeastOneContentObjectIsSelected = true;

	}
	
	public void removeAllObjectsOfCurrentPage_UIAction(){
		
		if (CollectionUtils.isEmpty(selectedContentObjects)){
			return ;
		}
		
		selectedContentObjects = null;
		
		atLeastOneContentObjectIsSelected = false;
	}
	
	
	public Object getSubWrappedDataFromHtmlDataTable(PagedListDataModel pagedListDataModel, HtmlDataTable htmlDataTable ){
		if (pagedListDataModel == null || htmlDataTable == null)
			return Collections.EMPTY_LIST;
		
		//First row currently displayed in data table
		int startIndex = htmlDataTable.getFirst();
		
		//Number of rows specified in xhtml
		int endIndex = startIndex + htmlDataTable.getRows();
		
		Object subWrappedData = pagedListDataModel.getSubWrappedData(startIndex, endIndex);
		if (subWrappedData == null)
			return Collections.EMPTY_LIST;
		
		return subWrappedData;
	}

	
	/**
	 * @return the selectedContentObjects
	 */
	public List<ContentObjectUIWrapper> getSelectedContentObjects() {
		return selectedContentObjects;
	}

	/**
	 * @param selectedContentObjects the selectedContentObjects to set
	 */
	public void setSelectedContentObjects(
			List<ContentObjectUIWrapper> selectedContentObjects) {
		this.selectedContentObjects = selectedContentObjects;
	}

	public boolean isAtLeastOneContentObjectIsSelected() {
		return atLeastOneContentObjectIsSelected;
	}

	public void setAtLeastOneContentObjectIsSelected(
			boolean atLeastOneContentObjectIsSelected) {
		this.atLeastOneContentObjectIsSelected = atLeastOneContentObjectIsSelected;
	}
	
	public void setSelectedObjectIndex(int selectedObjectIndex) {
		this.selectedObjectIndex = selectedObjectIndex;
	}
	
}
