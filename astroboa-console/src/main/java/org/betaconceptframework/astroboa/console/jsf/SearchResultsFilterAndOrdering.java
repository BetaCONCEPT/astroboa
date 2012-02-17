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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.Date;

import org.betaconceptframework.ui.jsf.AbstractUIBean;

/**
 * @author gchomatas
 * Created on Aug 31, 2007
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SearchResultsFilterAndOrdering extends AbstractUIBean {
	
	/*
	 * the following properties hold user selections about filters and ordering
	 * the filters are utilized during browsing and are used to generate extra search criteria besides those set by the browsing action.
	 */
	
	private static final long serialVersionUID = 1L;
	
	// holds the user selection about searching / filtering by content object
	// owner
	private Integer selectedOwnerFilter;

	// holds the user selection about the ordering of search results
	private String selectedResultsOrder;

	// holds the text string by which we are searching
	private String searchedText;

	// holds the selected content object type by which we are searching /
	// filtering
	private String selectedContentObjectType;

	// these two hold the date interval into which modification/creation of
	// searched content objects has happen
	private Date selectedFromDate;

	private Date selectedToDate;

	// holds whether the user has selected to use the above selected values as
	// filters during repository browsing
	private boolean filteringDuringBrowsingEnabled;
	
	private String selectedContentObjectIdentifier;

	//This is the query name used in case search criteria are to be saved
	private String queryName;
	
	//This is the query title used in case search criteria are to be saved
	private String queryTitle;
	
	//This is the query localized label used in case search criteria are to be saved
	private String queryLocalizedLabel;
	
	private String selectedContentObjectSystemName;
	
	public boolean isFilteringDuringBrowsingEnabled() {
		return filteringDuringBrowsingEnabled;
	}

	public void setFilteringDuringBrowsingEnabled(
			boolean filteringDuringBrowsingEnabled) {
		this.filteringDuringBrowsingEnabled = filteringDuringBrowsingEnabled;
	}

	public String getSearchedText() {
		return searchedText;
	}

	public void setSearchedText(String searchedText) {
		this.searchedText = searchedText;
	}

	public String getSelectedContentObjectType() {
		return selectedContentObjectType;
	}

	public void setSelectedContentObjectType(String selectedContentObjectType) {
		this.selectedContentObjectType = selectedContentObjectType;
	}

	public Date getSelectedFromDate() {
		return selectedFromDate;
	}

	public void setSelectedFromDate(Date selectedFromDate) {
		this.selectedFromDate = selectedFromDate;
	}

	public Integer getSelectedOwnerFilter() {
		return selectedOwnerFilter;
	}

	public void setSelectedOwnerFilter(Integer selectedOwnerFilter) {
		this.selectedOwnerFilter = selectedOwnerFilter;
	}

	public String getSelectedResultsOrder() {
		return selectedResultsOrder;
	}

	public void setSelectedResultsOrder(String selectedResultsOrder) {
		this.selectedResultsOrder = selectedResultsOrder;
	}

	public Date getSelectedToDate() {
		return selectedToDate;
	}

	public void setSelectedToDate(Date selectedToDate) {
		this.selectedToDate = selectedToDate;
	}

	public String getSelectedContentObjectIdentifier() {
		return selectedContentObjectIdentifier;
	}

	public void setSelectedContentObjectIdentifier(
			String selectedContentObjectIdentifier) {
		this.selectedContentObjectIdentifier = selectedContentObjectIdentifier;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getQueryTitle() {
		return queryTitle;
	}

	public void setQueryTitle(String queryTitle) {
		this.queryTitle = queryTitle;
	}

	public String getQueryLocalizedLabel() {
		return queryLocalizedLabel;
	}

	public void setQueryLocalizedLabel(String queryLocalizedLabel) {
		this.queryLocalizedLabel = queryLocalizedLabel;
	}

	public String getSelectedContentObjectSystemName() {
		return selectedContentObjectSystemName;
	}

	public void setSelectedContentObjectSystemName(
			String selectedContentObjectSystemName) {
		this.selectedContentObjectSystemName = selectedContentObjectSystemName;
	}
	
}
