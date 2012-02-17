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
package org.betaconceptframework.astroboa.context;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;

/**
 * 
 * Class providing information about repository associated with the
 * current thread of execution.
 *
 * <p>
 * The repository context is stored in a {@link RepositoryContextHolder}.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class RepositoryContext implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6044852928661674641L;
	
	
	private final CmsRepository cmsRepository;
	private final SecurityContext securityContext;
	
	public RepositoryContext(CmsRepository cmsRepository, SecurityContext securityContext){
		if (cmsRepository == null || cmsRepository.getId() == null || cmsRepository.getId().trim().length() == 0){
			throw new CmsException("Try to associate an empty repository to repository context");
		}
		
		this.cmsRepository = cmsRepository;
		
		this.securityContext = securityContext;
		
	}

	public String toString(){
		return "RepositoryContext["+cmsRepository.toString()+ 
		 (securityContext != null ? " , "+ securityContext.toString():"")+ "]";
	}

	/**
	 * Retrieve information about current repository
	 * 
	 * @return Information about associated repository.
	 */
	public CmsRepository getCmsRepository() {
		return cmsRepository;
	}
	
	public SecurityContext getSecurityContext(){
		return securityContext;
	}
	
}
