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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.jboss.seam.contexts.Contexts;
import org.richfaces.event.NodeSelectedEvent;

/**
* @author gchomatas
* Created on Oct 7, 2007
*/
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PageController extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;
	
	// A DynamicAreaPageComponent is the enumeration object which holds information about the page components (i.e. the xhtml page fragments)
	//which need to be rendered into the web page dynamic area
	// holds the page component which is rendered in the UI dynamic area (menu buttons, searches or browsing determine the appropriate component to be rendered each time)
	private String dynamicUIAreaCurrentPageComponent = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR.getDynamicUIAreaPageComponent();
	private DynamicUIAreaPageComponent dynamicUIAreaCurrentPageComponentEnum = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR;
	
	// holds the default number of rows per page in a multi page dataTable
	private final static int DEFAULT_ROWS = 10;
	
	// holds the rows that we present in each dataTable page
	private int rowsPerDataTablePage = DEFAULT_ROWS;
	
	private String contentObjectTypeToUseWhenLoadingClearedContentObjectEditForm;
	
	public String loadPageComponentInDynamicUIArea(String nextActivePageComponentAsString) {
		
		//Retrieve the enumeration that corresponds to the page component
		DynamicUIAreaPageComponent nextActivePageComponentAsEnum = null;
		try{
			nextActivePageComponentAsEnum = DynamicUIAreaPageComponent.getDynamicUIAreaComponent(nextActivePageComponentAsString);
		}
		catch (Exception e){
			logger.error("Could not find dynamicAreaUIComponent for active page component "+ nextActivePageComponentAsString+ "Setting default values", e);
			
			//Default values
			nextActivePageComponentAsEnum = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR;
			nextActivePageComponentAsString = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR.getDynamicUIAreaPageComponent();
		}
		
		if (nextActivePageComponentAsEnum == null){
			logger.warn("Could not find dynamicAreaUIComponent for next active page component "+ nextActivePageComponentAsString+ "Setting default values");
		
			//Default values
			nextActivePageComponentAsEnum = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR;
			nextActivePageComponentAsString = DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR.getDynamicUIAreaPageComponent();
		}
		
		//Decide whether page has changed
		String newPageName = null; //Null denotes that page stays the same
		if (dynamicUIAreaCurrentPageComponentEnum == null ||
			 ! dynamicUIAreaCurrentPageComponentEnum.getPageWhichContainsComponent().equals(nextActivePageComponentAsEnum.getPageWhichContainsComponent())){
			
			newPageName = nextActivePageComponentAsEnum.getPageWhichContainsComponent();
		}
		
		//Change dynamic component
		dynamicUIAreaCurrentPageComponent = nextActivePageComponentAsString;
		dynamicUIAreaCurrentPageComponentEnum = nextActivePageComponentAsEnum;
		
		//reset beans
		resetInactiveConversationVariables(nextActivePageComponentAsEnum);
		
		return newPageName;
	}
	
	public void navigationTreeNodeSelected_Listener(NodeSelectedEvent event) {
		//resetInactiveConversationVariables(DynamicUIAreaPageComponent.ContentObjectList.getDynamicUIAreaPageComponent());
	}
	
	/**
	 * resets all backing beans in conversation scope which are not required 
	 * by the currently active page component i.e. resets all backing beans related to inactive page components 
	 * @param activePAgeComponent
	 */
	private void resetInactiveConversationVariables(DynamicUIAreaPageComponent activePageComponent) {
		List<String> backingBeansOfInactivePageComponents = DynamicUIAreaPageComponent.getBackingBeanNamesOfInactivePageComponents(activePageComponent);
		
		if (CollectionUtils.isNotEmpty(backingBeansOfInactivePageComponents)) {
			for (String backingBean : backingBeansOfInactivePageComponents) {
				logger.debug("Removing bean: '{}' from conversation context", backingBean );
				Contexts.getConversationContext().remove(backingBean);
			}
		}
	}
	
	public void simulateOpenContentObjectTypeSelectorAction_PageAction(){
		
		//Simulate user action 'Open OBJECT_TYPE_SELECTOR'
		loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR.getDynamicUIAreaPageComponent());
		
		//Reset current dynamic area component
		dynamicUIAreaCurrentPageComponent = null;
		dynamicUIAreaCurrentPageComponentEnum = null;
	}
	
	public void changeRowsPerDataTablePage_UIAction(int rowsPerPage){
		
		if (rowsPerPage <= 0){
			rowsPerDataTablePage = 20;
		}
		else{
			rowsPerDataTablePage = rowsPerPage;
		}
	}
	
	public String getDynamicUIAreaCurrentPageComponent() {
		return dynamicUIAreaCurrentPageComponent;
	}

	public void setDynamicUIAreaCurrentPageComponent(
			String dynamicUIAreaCurrentPageComponent) {
		this.dynamicUIAreaCurrentPageComponent = dynamicUIAreaCurrentPageComponent;
	}

	public int getRowsPerDataTablePage() {
		return rowsPerDataTablePage;
	}

	public void setRowsPerDataTablePage(int rowsPerDataTablePage) {
		this.rowsPerDataTablePage = rowsPerDataTablePage;
	}

	public void setContentObjectTypeToUseWhenLoadingClearedContentObjectEditForm(
			String contentObjectTypeToUseWhenLoadingClearedContentObjectEditForm) {
		this.contentObjectTypeToUseWhenLoadingClearedContentObjectEditForm = contentObjectTypeToUseWhenLoadingClearedContentObjectEditForm;
	}

	public String getContentObjectTypeToUseWhenLoadingClearedContentObjectEditForm() {
		return contentObjectTypeToUseWhenLoadingClearedContentObjectEditForm;
	}

	
}
