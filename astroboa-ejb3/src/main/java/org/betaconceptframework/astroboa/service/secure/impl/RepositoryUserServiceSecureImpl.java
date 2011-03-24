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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.secure.RepositoryUserServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteRepositoryUserServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({RepositoryUserServiceSecure.class})
@Remote({RemoteRepositoryUserServiceSecure.class})
@Stateless(name="RepositoryUserServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class RepositoryUserServiceSecureImpl extends AbstractSecureAstroboaService implements RepositoryUserServiceSecure{

	private RepositoryUserService repositoryUserService;
	
	@Override
	void initializeOtherRemoteServices() {
		repositoryUserService = (RepositoryUserService) springManagedRepositoryServicesContext.getBean("repositoryUserService");
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public void deleteRepositoryUser(String repositoryUserId,
			RepositoryUser alternativeUser,String authenticationToken) {
		
		repositoryUserService.deleteRepositoryUser(repositoryUserId, alternativeUser);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public void deleteRepositoryUserAndOwnedObjects(String repositoryUserId,String authenticationToken) {
		
		repositoryUserService.deleteRepositoryUserAndOwnedObjects(repositoryUserId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public RepositoryUser getRepositoryUser(String externalId,String authenticationToken) {
		
		return repositoryUserService.getRepositoryUser(externalId);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public RepositoryUser saveRepositoryUser(RepositoryUser repositoryUser,String authenticationToken) {
		
		return repositoryUserService.saveRepositoryUser(repositoryUser);
		
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<RepositoryUser> searchRepositoryUsers(
			RepositoryUserCriteria repositoryUserCriteria,String authenticationToken) {
		
		return repositoryUserService.searchRepositoryUsers(repositoryUserCriteria);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public RepositoryUser getSystemRepositoryUser(String authenticationToken) {
		return repositoryUserService.getSystemRepositoryUser();
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public RepositoryUser save(Object repositoryUser, String authenticationToken) {
		return repositoryUserService.save(repositoryUser);
	}


}
