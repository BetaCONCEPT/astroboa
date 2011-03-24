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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.secure.RepositoryUserServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Remote RepositoryUser Service Wrapper responsible to connect to the provided repository
 * before any of tis method is called. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserServiceClientWrapper extends AbstractClientServiceWrapper implements RepositoryUserService {




	private RepositoryUserServiceSecure repositoryUserServiceSecure;

	public RepositoryUserServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	protected void resetService() {
		repositoryUserServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				repositoryUserServiceSecure = (RepositoryUserServiceSecure)connectToLocalService(RepositoryUserServiceSecure.class);
			}
			else{
				repositoryUserServiceSecure = (RepositoryUserServiceSecure)connectToRemoteService(RepositoryUserServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			repositoryUserServiceSecure = null;
		}

		return repositoryUserServiceSecure !=null;
	}


	public void deleteRepositoryUser(String repositoryUserId,
			RepositoryUser alternativeUser) {

		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			repositoryUserServiceSecure.deleteRepositoryUser(repositoryUserId, alternativeUser, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}


	}



	public void deleteRepositoryUserAndOwnedObjects(String repositoryUserId) {

		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			repositoryUserServiceSecure.deleteRepositoryUserAndOwnedObjects(repositoryUserId, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}

	}


	public RepositoryUser getRepositoryUser(String externalId) {

		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryUserServiceSecure.getRepositoryUser(externalId, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}
	}

	public RepositoryUser saveRepositoryUser(RepositoryUser repositoryUser) {

		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryUserServiceSecure.saveRepositoryUser(repositoryUser, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}

	}



	public List<RepositoryUser> searchRepositoryUsers(
			RepositoryUserCriteria repositoryUserCriteria) {

		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryUserServiceSecure.searchRepositoryUsers(repositoryUserCriteria, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}

	}

	public RepositoryUser getSystemRepositoryUser() {
		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryUserServiceSecure.getSystemRepositoryUser(getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}

	}

	@Override
	public RepositoryUser save(Object repositoryUser) {
		if (repositoryUserServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryUserServiceSecure.save(repositoryUser, getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryUserService reference was not found");
		}
	}


}
