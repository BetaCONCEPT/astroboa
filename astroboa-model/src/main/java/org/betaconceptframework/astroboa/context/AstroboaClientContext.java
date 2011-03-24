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
package org.betaconceptframework.astroboa.context;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;

/**
 * Class containing all necessary structures associated with one Astroboa 
 * client. This class is used by RepositoryContextHolder and is associated
 * with the current Thread of execution.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class AstroboaClientContext{
	
	private final RepositoryContext repositoryContext;
	
	private final LazyLoader lazyLoader;
	
	public AstroboaClientContext(RepositoryContext repositoryContext,
			LazyLoader lazyLoader) {
		
		if (repositoryContext == null){
			throw new CmsException("Unable to initialize AstroboaClientContext as no repository context has been provided");
		}
		
		if (lazyLoader == null){
			throw new CmsException("Unable to initialize AstroboaClientContext as no lazy loader has been provided");
		}
		
		
		if (repositoryContext.getSecurityContext() == null || repositoryContext.getSecurityContext().getAuthenticationToken()== null || 
				repositoryContext.getSecurityContext().getAuthenticationToken().trim().length() == 0){
			throw new CmsException("Unable to initialize AstroboaClientContext as no authentication token has been provided");
		}
		
		this.repositoryContext = repositoryContext;
		this.lazyLoader = lazyLoader;

		
	}

	public RepositoryContext getRepositoryContext() {
		return repositoryContext;
	}

	public LazyLoader getLazyLoader() {
		return lazyLoader;
	}

	public String getAuthenticationToken() {
		return repositoryContext.getSecurityContext().getAuthenticationToken();
	}
	
	public String toString(){
		return "AstroboaClientContext["+repositoryContext.toString()+" ]"; 
	}


}
