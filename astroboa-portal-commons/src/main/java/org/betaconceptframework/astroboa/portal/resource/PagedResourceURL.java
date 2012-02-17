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

/**
 * It holds the URL to a specific page of results for a resource
 * It is useful for creating links that allow scrolling through paged results when resource requests return a paged list of resources.
 * A list of PagedResourceURLs is created automatically (See the ContentObjectResource class) and put into ResourceResponse when a resource request returns a list of results.
 * The urls point to result pages that precede or follow the requested result page.   
 * generated  
 * @author gchomatas
 * Created on Sep 10, 2008
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PagedResourceURL {
	
	private int pageNumber;
	private String URL;
	
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String url) {
		URL = url;
	}

}
