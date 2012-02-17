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

import java.util.List;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class  ResourceResponse<T,P extends ResourceContext> {
	private List<T> resourceRepresentation;
	private P resourceContext;
	
	public List<T> getResourceRepresentation() {
		return resourceRepresentation;
	}
	public void setResourceRepresentation(List<T> resourceRepresentation) {
		this.resourceRepresentation = resourceRepresentation;
	}
	public P getResourceContext() {
		return resourceContext;
	}
	public void setResourceContext(P resourceContext) {
		this.resourceContext = resourceContext;
	}
	
	/**
	 * Convenient method which returns the first  resource found in resource representation
	 * or null if none is found
	 * @return
	 */
	public T getFirstResource(){
		
		if (resourceRepresentation != null && resourceRepresentation.size()>0){
			return resourceRepresentation.get(0);
		}
		
		return null;
	}
	
	
}
