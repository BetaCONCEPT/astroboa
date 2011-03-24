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

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.api.service.secure.RepositoryServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteRepositoryServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryServiceClientWrapper extends AbstractClientServiceWrapper implements RepositoryService{

	private RemoteRepositoryServiceSecure repositoryServiceSecure;

	private boolean localServiceIsUsed;

	public RepositoryServiceClientWrapper(AstroboaClient client, String serverHostNameOrIpAndPortDelimitedWithSemiColon) {
		super(client, serverHostNameOrIpAndPortDelimitedWithSemiColon);
	}

	@Override
	protected void resetService() {
		repositoryServiceSecure = null;
		localServiceIsUsed = true;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				repositoryServiceSecure = (RepositoryServiceSecure) connectToLocalService(RepositoryServiceSecure.class);
				localServiceIsUsed = true;
			}
			else{
				repositoryServiceSecure = (RemoteRepositoryServiceSecure)connectToRemoteService(RemoteRepositoryServiceSecure.class);
				localServiceIsUsed = false;
			}

		}catch(Exception e){
			//do not rethrow exception.Probably service is not available
			logger.warn("",e);
			repositoryServiceSecure = null;
		}

		return repositoryServiceSecure != null;
	}



	public String login(String repositoryId, AstroboaCredentials credentials,String permanentKey) {

		if (repositoryServiceSecure != null){

			String authenticationToken = repositoryServiceSecure.login(repositoryId, credentials, permanentKey);

			client.setAuthenticationToken(authenticationToken);

			createClientContextIFLoggedInClientIsRemote(repositoryId, authenticationToken);
			
			setExternalIdentityStoreJNDINameToClient();

			return authenticationToken;
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}


	}
	
	private void createClientContextIFLoggedInClientIsRemote(
			String repositoryId, String authenticationToken) {
		//Furthermore if client is using remote service then it should create a RepositoryContext
		//on its own Thread Local
		if (! localServiceIsUsed){
			//Create a repository context and put it to ThreadLocal in order to be available to subsequent
			//service method calls
			CmsRepository connectedCmsRepository = repositoryServiceSecure.getCmsRepository(repositoryId);
			

			//Subject is not needed by this remote client
			//Neither authentication time out
			SecurityContext securityContext = new SecurityContext(authenticationToken, new Subject(), 0, 
					getAvailableRepositoryIds());

			RepositoryContext repositoryContext = new RepositoryContext(connectedCmsRepository,securityContext);
			
			LazyLoader lazyLoader = new LazyLoader(client.getSpaceService(), client.getTopicService(), 
					client.getDefinitionService(), client.getContentService());

			AstroboaClientContext clientContext = new AstroboaClientContext(repositoryContext, lazyLoader);
			
			registerClientToContext(clientContext);
		}
	}

	public List<CmsRepository> getAvailableCmsRepositories() {
		if (repositoryServiceSecure != null){
			return repositoryServiceSecure.getAvailableCmsRepositories();
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}
	}

	public CmsRepository getCurrentConnectedRepository() {

		if (repositoryServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return repositoryServiceSecure.getCurrentConnectedRepository(getAuthenticationToken());
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}

	}

	public boolean isRepositoryAvailable(String repositoryId) {

		if (repositoryServiceSecure != null){
			return repositoryServiceSecure.isRepositoryAvailable(repositoryId);
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}


	}

	public CmsRepository getCmsRepository(String repositoryId) {
		if (repositoryServiceSecure != null){
			return repositoryServiceSecure.getCmsRepository(repositoryId);
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}
	}

	public String login(String repositoryId, Subject subject, String permanentKey) {

		if (repositoryServiceSecure != null){
			
			if ( ! (repositoryServiceSecure instanceof RepositoryServiceSecure))
			{
				throw new CmsException("Remotely Login to Astroboa repository with predefined Subject instance is not allowed ");
			}

			String authenticationToken = ((RepositoryServiceSecure)repositoryServiceSecure).login(repositoryId, subject, permanentKey);

			client.setAuthenticationToken(authenticationToken);

			//Furthermore if client is using remote service then it should create a RepositoryContext
			//on its own Thread Local
			if (! localServiceIsUsed){
				//Create a repository context and put it to ThreadLocal in order to be available to subsequent
				//service method calls
				CmsRepository connectedCmsRepository = repositoryServiceSecure.getCmsRepository(repositoryId);

				//Subject is not needed by this remote client
				//Neither authentication time out
				//TODO : Must write a function to obtain username from subject
				SecurityContext securityContext = new SecurityContext(authenticationToken, subject, 0, 
						getAvailableRepositoryIds());

				RepositoryContext repositoryContext = new RepositoryContext(connectedCmsRepository,securityContext);
				
				LazyLoader lazyLoader = new LazyLoader(client.getSpaceService(), client.getTopicService(), 
						client.getDefinitionService(), client.getContentService());

				AstroboaClientContext clientContext = new AstroboaClientContext(repositoryContext, lazyLoader);
				
				registerClientToContext(clientContext);
			}

			setExternalIdentityStoreJNDINameToClient();
			
			return authenticationToken;
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}

	}

	private void registerClientToContext(AstroboaClientContext clientContext) {
		
		client.setClientContext(clientContext);  
		client.activateClientContext();
		
	}

	private List<String> getAvailableRepositoryIds() {
		
		List<CmsRepository> availableRepositories = repositoryServiceSecure.getAvailableCmsRepositories();
		
		List<String> repositoryIds = new ArrayList<String>();
		
		if (CollectionUtils.isNotEmpty(availableRepositories)){
		
			for (CmsRepository cmsRepository: availableRepositories){
				repositoryIds.add(cmsRepository.getId());
			}
		}
		
		return repositoryIds;
	}

	public String login(String repositoryId, AstroboaCredentials credentials) {
		return login(repositoryId, credentials,	null);
	}

	@Override
	public String loginAsAnonymous(String repositoryId) {
		return loginAsAnonymous(repositoryId, null);
	}

	@Override
	public String loginAsAnonymous(String repositoryId, String permanentKey) {
		
		if (repositoryServiceSecure != null){

			String authenticationToken = repositoryServiceSecure.loginAsAnonymous(repositoryId, permanentKey);

			client.setAuthenticationToken(authenticationToken);

			createClientContextIFLoggedInClientIsRemote(repositoryId, authenticationToken);

			setExternalIdentityStoreJNDINameToClient();
			
			return authenticationToken;
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}

	}

	@Override
	public String login(String repositoryId, String username, String key) {
		return login(repositoryId, username, key, null);
	}

	@Override
	public String login(String repositoryId, String username, String key,
			String permanentKey) {
		if (repositoryServiceSecure != null){

			String authenticationToken = repositoryServiceSecure.login(repositoryId, username, key, permanentKey);

			client.setAuthenticationToken(authenticationToken);

			createClientContextIFLoggedInClientIsRemote(repositoryId, authenticationToken);

			setExternalIdentityStoreJNDINameToClient();
			
			return authenticationToken;
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}
	}

	@Override
	public String loginAsAdministrator(String repositoryId, String key) {
		return loginAsAdministrator(repositoryId, key, null);
	}

	@Override
	public String loginAsAdministrator(String repositoryId, String key,
			String permanentKey) {
		
		if (repositoryServiceSecure != null){

			String authenticationToken = repositoryServiceSecure.loginAsAdministrator(repositoryId, key, permanentKey);

			client.setAuthenticationToken(authenticationToken);

			createClientContextIFLoggedInClientIsRemote(repositoryId, authenticationToken);

			setExternalIdentityStoreJNDINameToClient();
			
			return authenticationToken;
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}	
	}

	private void setExternalIdentityStoreJNDINameToClient(){
			RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(getAuthenticationToken());
			
			CmsRepository cmsRepository = (repositoryContext == null ? null : repositoryContext.getCmsRepository());
			
			if (cmsRepository == null){
				logger.warn("Found no cmsRepository in current thread for token {}",getAuthenticationToken());
			}
			else{
				client.setExternalIdentityStoreJNDIName(cmsRepository.getExternalIdentityStoreJNDIName());
			}

	}

	public boolean tokenHasExpired(String authenticationToken) {
		if (repositoryServiceSecure != null){

			return repositoryServiceSecure.tokenHasExpired(authenticationToken);
		}
		else{
			throw new CmsException("RepositoryService reference was not found");
		}

	}
}
