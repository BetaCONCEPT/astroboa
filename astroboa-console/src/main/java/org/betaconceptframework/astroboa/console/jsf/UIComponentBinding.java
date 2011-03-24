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

import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlDataTable;
import org.richfaces.component.html.HtmlDatascroller;
import org.richfaces.component.html.HtmlTree;

@Name("uiComponentBinding")
@Scope(ScopeType.EVENT)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UIComponentBinding extends AbstractUIBean {
	
	// The JSF Table and Scroller objects through which the list of content objects returned by each search is presented to the user
	private HtmlDataTable listViewContentObjectTableComponent;
	private HtmlDatascroller listViewContentObjectTableScrollerComponent;
	
	private HtmlDataTable recentlyAddedOrModifiedPublishedContentObjectsTableComponent;
	private HtmlDatascroller recentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent;
	
	private HtmlDataTable fullViewContentObjectTableComponent;
	private HtmlDatascroller fullViewContentObjectTableScrollerComponent;
	
	private UITree contentObjectComplexPropertiesTreeComponent;
	
	private HtmlTree contentObjectFolderTreeRichFaces;
	
	private UITree contentObjectTreeComponent;
	
	public void resetContentObjectTableScrollerComponent() {
		if (listViewContentObjectTableComponent != null && listViewContentObjectTableScrollerComponent != null) {
			listViewContentObjectTableScrollerComponent.setPage(1);
		}
		
		if (fullViewContentObjectTableComponent != null && fullViewContentObjectTableScrollerComponent != null) {
			fullViewContentObjectTableScrollerComponent.setPage(1);
		}
	}
	
	
	
	public HtmlDataTable getListViewContentObjectTableComponent() {
		return listViewContentObjectTableComponent;
	}
	public void setListViewContentObjectTableComponent(
			HtmlDataTable listViewContentObjectTableComponent) {
		this.listViewContentObjectTableComponent = listViewContentObjectTableComponent;
	}
	public HtmlDataTable getFullViewContentObjectTableComponent() {
		return fullViewContentObjectTableComponent;
	}
	public void setFullViewContentObjectTableComponent(
			HtmlDataTable fullViewContentObjectTableComponent) {
		this.fullViewContentObjectTableComponent = fullViewContentObjectTableComponent;
	}
	public HtmlDatascroller getListViewContentObjectTableScrollerComponent() {
		return listViewContentObjectTableScrollerComponent;
	}
	public void setListViewContentObjectTableScrollerComponent(
			HtmlDatascroller listViewContentObjectTableScrollerComponent) {
		this.listViewContentObjectTableScrollerComponent = listViewContentObjectTableScrollerComponent;
	}
	public HtmlDatascroller getFullViewContentObjectTableScrollerComponent() {
		return fullViewContentObjectTableScrollerComponent;
	}
	public void setFullViewContentObjectTableScrollerComponent(
			HtmlDatascroller fullViewContentObjectTableScrollerComponent) {
		this.fullViewContentObjectTableScrollerComponent = fullViewContentObjectTableScrollerComponent;
	}
	public HtmlDataTable getRecentlyAddedOrModifiedPublishedContentObjectsTableComponent() {
		return recentlyAddedOrModifiedPublishedContentObjectsTableComponent;
	}
	public void setRecentlyAddedOrModifiedPublishedContentObjectsTableComponent(
			HtmlDataTable recentlyAddedOrModifiedPublishedContentObjectsTableComponent) {
		this.recentlyAddedOrModifiedPublishedContentObjectsTableComponent = recentlyAddedOrModifiedPublishedContentObjectsTableComponent;
	}
	public HtmlDatascroller getRecentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent() {
		return recentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent;
	}
	public void setRecentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent(
			HtmlDatascroller recentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent) {
		this.recentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent = recentlyAddedOrModifiedPublishedContentObjectsTableScrollerComponent;
	}
	
	public UITree getContentObjectComplexPropertiesTreeComponent() {
		return contentObjectComplexPropertiesTreeComponent;
	}
	public void setContentObjectComplexPropertiesTreeComponent(
			UITree contentObjectComplexPropertiesTreeComponent) {
		this.contentObjectComplexPropertiesTreeComponent = contentObjectComplexPropertiesTreeComponent;
	}


	public HtmlTree getContentObjectFolderTreeRichFaces() {
		return contentObjectFolderTreeRichFaces;
	}

	public void setContentObjectFolderTreeRichFaces(
			HtmlTree contentObjectFolderTreeRichFaces) {
		this.contentObjectFolderTreeRichFaces = contentObjectFolderTreeRichFaces;
	}



	public UITree getContentObjectTreeComponent() {
		return contentObjectTreeComponent;
	}



	public void setContentObjectTreeComponent(UITree contentObjectTreeComponent) {
		this.contentObjectTreeComponent = contentObjectTreeComponent;
	}
	

}
