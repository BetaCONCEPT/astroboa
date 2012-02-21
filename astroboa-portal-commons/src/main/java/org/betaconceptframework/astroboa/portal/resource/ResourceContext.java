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
package org.betaconceptframework.astroboa.portal.resource;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class ResourceContext {
	// holds the request URL for the resource
	private String resourceRequestURL;
	
	// provided in resource query
	private int pageNumber;
	private int pageSize;
	
	// determined by resource query results
	private int currentPageSize;
	private int totalResourceCount;
	
	// calculated
	private int offset;
	private int totalPages;
	private List<PagedResourceURL> pageScrollingURLs = new ArrayList<PagedResourceURL>();
	private String firstPageURL;
	private String lastPageURL;
	
	
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getCurrentPageSize() {
		return currentPageSize;
	}
	public void setCurrentPageSize(int currentPageSize) {
		this.currentPageSize = currentPageSize;
	}
	public int getTotalResourceCount() {
		return totalResourceCount;
	}
	public void setTotalResourceCount(int totalResourceCount) {
		this.totalResourceCount = totalResourceCount;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public String getResourceRequestURL() {
		return resourceRequestURL;
	}
	public void setResourceRequestURL(String resourceRequestURL) {
		this.resourceRequestURL = resourceRequestURL;
	}
	public List<PagedResourceURL> getPageScrollingURLs() {
		return pageScrollingURLs;
	}
	public void setPageScrollingURLs(List<PagedResourceURL> pageScrollingURLs) {
		this.pageScrollingURLs = pageScrollingURLs;
	}
	public String getLastPageURL() {
		return lastPageURL;
	}
	public void setLastPageURL(String lastPageURL) {
		this.lastPageURL = lastPageURL;
	}
	public String getFirstPageURL() {
		return firstPageURL;
	}
	public void setFirstPageURL(String firstPageURL) {
		this.firstPageURL = firstPageURL;
	}
}
