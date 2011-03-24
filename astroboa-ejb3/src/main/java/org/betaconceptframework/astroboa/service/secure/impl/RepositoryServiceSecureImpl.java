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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;

import javax.interceptor.Interceptors;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;
import org.betaconceptframework.astroboa.service.secure.security.SecurityService;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
abstract class RepositoryServiceSecureImpl {
	
	protected RepositoryService repositoryService;
	
	public String login(String repositoryId,
			AstroboaCredentials credentials, String permanentKey) {
		
		String authenticationToken =  repositoryService.login(repositoryId, credentials, permanentKey);
		
		getSecurityService().addClientContexToValidContextMap(AstroboaClientContextHolder.getActiveClientContext(), StringUtils.isNotBlank(permanentKey));
		
		return authenticationToken;
		
	}
	
	public List<CmsRepository> getAvailableCmsRepositories() {
		return repositoryService.getAvailableCmsRepositories();
	}

	public boolean isRepositoryAvailable(String repositoryId) {
		return repositoryService.isRepositoryAvailable(repositoryId);
	}

	public CmsRepository getCmsRepository(String repositoryId) {
		return repositoryService.getCmsRepository(repositoryId);
		
	}

	@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
	public CmsRepository getCurrentConnectedRepository(String authenticationToken) {
		return repositoryService.getCurrentConnectedRepository();
	}

	public void resetAuthenticationTokenTimeout(String authenticationToken) {
		getSecurityService().resetAuthenticationTokenTimeout(authenticationToken);
		
	}

	public String loginAsAnonymous(String repositoryId) {
		return loginAsAnonymous(repositoryId, null);
	}

	public String loginAsAnonymous(String repositoryId, String permanentKey) {
		
		String authenticationToken =  repositoryService.loginAsAnonymous(repositoryId, permanentKey);
		
		getSecurityService().addClientContexToValidContextMap(AstroboaClientContextHolder.getActiveClientContext(), StringUtils.isNotBlank(permanentKey));
		
		return authenticationToken;
	}

	public String login(String repositoryId, String username, String key) {
		return login(repositoryId, username, key, null);
	}

	public String login(String repositoryId, String username, String key,
			String permanentKey) {
		
		String authenticationToken =  repositoryService.login(repositoryId, username, key, permanentKey);
		
		getSecurityService().addClientContexToValidContextMap(AstroboaClientContextHolder.getActiveClientContext(), StringUtils.isNotBlank(permanentKey));
		
		return authenticationToken;

	}

	public String loginAsAdministrator(String repositoryId, String key) {
		return loginAsAdministrator(repositoryId, key, null);
	}

	public String loginAsAdministrator(String repositoryId, String key,
			String permanentKey) {

		String authenticationToken =  repositoryService.loginAsAdministrator(repositoryId, key, permanentKey);
		
		getSecurityService().addClientContexToValidContextMap(AstroboaClientContextHolder.getActiveClientContext(), StringUtils.isNotBlank(permanentKey));
		
		return authenticationToken;
	}

	public boolean tokenHasExpired(String authenticationToken) {
		return getSecurityService().tokenHasExpired(authenticationToken);
	}

	protected abstract SecurityService getSecurityService();

	
}
