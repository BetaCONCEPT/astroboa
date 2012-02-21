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
package org.betaconceptframework.astroboa.client.service;

import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.api.service.secure.IdentityStoreSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;

/**
 * Remote Indetity Store Wrapper responsible to connect to the provided repository
 * before any of its method is called. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IdentityStoreClientWrapper extends AbstractClientServiceWrapper implements IdentityStore{

	private IdentityStoreSecure identityStoreSecure;

	private IdentityStore externalIdentityStore;
	
	private AstroboaClient clientForIdentityStore;

	public IdentityStoreClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	
	@Override
	void resetService() {
		identityStoreSecure = null;
		externalIdentityStore = null;
		clientForIdentityStore= null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			
			//Loading identity store is a little more complex than other services
			//since an external to Astroboa IdentityStore implementation may be plugged in.
			
			RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(getAuthenticationToken());
			
			CmsRepository cmsRepository = (repositoryContext == null ? null : repositoryContext.getCmsRepository());
			
			if (cmsRepository == null){
				
				//No active context for provided token. Get repository id and try to get it from Repository Service
				if (client != null && client.getConnectedRepositoryId() != null && client.getRepositoryService() != null){
					cmsRepository = client.getRepositoryService().getCmsRepository(client.getConnectedRepositoryId()); 
				}
				
				if (cmsRepository == null){
					throw new CmsException("Found no cmsRepository in current thread for token "+getAuthenticationToken()+ ". Unable to initialize IdentityStore for client "+ client);
				}
			}
			
			if (StringUtils.isNotBlank(cmsRepository.getExternalIdentityStoreJNDIName())){
				if (loadLocalService){
					externalIdentityStore = (IdentityStore) connectToLocalService(IdentityStoreSecure.class, cmsRepository.getExternalIdentityStoreJNDIName());
				}
				else{
					externalIdentityStore = (IdentityStore) connectToRemoteService(IdentityStoreSecure.class, cmsRepository.getExternalIdentityStoreJNDIName());
				}
			}
			else{
				//Astroboa IdentityStore is provided. Proceed normally
				if (loadLocalService){
					identityStoreSecure = (IdentityStoreSecure) connectToLocalService(IdentityStoreSecure.class);
				}
				else{
					identityStoreSecure = (IdentityStoreSecure) connectToRemoteService(IdentityStoreSecure.class);
				}
			}
			

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			resetService();
		}

		return identityStoreSecure != null || externalIdentityStore !=null;
	}

	@Override
	public boolean addRoleToGroup(String role, String group) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.addRoleToGroup(role, group, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.addRoleToGroup(role, group);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	private String getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository() {
		if (clientForIdentityStore == null || 
				StringUtils.isBlank(clientForIdentityStore.getAuthenticationToken())){
			throw new CmsException("You have to login to IdentityStore first");
		}
		
		return clientForIdentityStore.getAuthenticationToken();   
	}

	@Override
	public boolean authenticate(String username, String password) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.authenticate(username, password, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.authenticate(username, password);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean changePassword(String name, String oldPassword, String newPassword) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.changePassword(name, oldPassword, newPassword, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.changePassword(name, oldPassword, newPassword);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean createRole(String role) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.createRole(role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.createRole(role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean createUser(String username, String password) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.createUser(username, password, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.createUser(username, password);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean createUser(String username, String password,
			String firstname, String lastname) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.createUser(username, password, firstname, lastname, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.createUser(username, password,firstname, lastname);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean deleteRole(String role) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.deleteRole(role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.deleteRole(role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean deleteUser(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.deleteUser(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.deleteUser(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean disableUser(String name) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.disableUser(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.disableUser(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean enableUser(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.enableUser(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.enableUser(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> getGrantedRoles(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.getGrantedRoles(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.getGrantedRoles(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> getImpliedRoles(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.getImpliedRoles(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.getImpliedRoles(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> getRoleGroups(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.getRoleGroups(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.getRoleGroups(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean grantRole(String name, String role) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.grantRole(name, role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.grantRole(name, role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean isUserEnabled(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.isUserEnabled(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.isUserEnabled(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> listGrantableRoles() {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listGrantableRoles(getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listGrantableRoles();
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<Principal> listMembers(String role) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listMembers(role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listMembers(role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> listRoles() {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listRoles(getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listRoles();
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> listUsers() {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listUsers(getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listUsers();
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public List<String> listUsers(String filter) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listUsers(filter, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listUsers(filter);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean removeRoleFromGroup(String role, String group) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.removeRoleFromGroup(role, group, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.removeRoleFromGroup(role, group);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean revokeRole(String name, String role) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.revokeRole(name, role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.revokeRole(name, role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean roleExists(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.roleExists(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.roleExists(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	@Override
	public boolean userExists(String name) {

		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.userExists(name, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.userExists(name);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}

	public Person retrieveUser(String username) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.retrieveUser(username, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.retrieveUser(username);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
	}


	//Login is meaningful only when IdentityStore is a Astroboa repository
	public void login(AstroboaCredentials credentials,
			String permanentKey) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			CmsRepository connectedCmsRepository = client.getRepositoryService().getCmsRepository(
						AstroboaClientContextHolder.getActiveRepositoryId());
				
 			//In case repository has its own identity store
			if (StringUtils.equals(connectedCmsRepository.getIdentityStoreRepositoryId(), connectedCmsRepository.getId())){
					clientForIdentityStore = client;
			}
			else{
				
				clientForIdentityStore = new AstroboaClient(client.getServerHostNameOrIpAndPortToConnectTo());
				clientForIdentityStore.login(connectedCmsRepository.getIdentityStoreRepositoryId(), 
						credentials,permanentKey);
			}
			
					
		}
	}


	public void login(Subject subject, String permanentKey) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			CmsRepository connectedCmsRepository = client.getRepositoryService().getCmsRepository(
						AstroboaClientContextHolder.getActiveRepositoryId());
				
 			//In case repository has its own identity store
			if (StringUtils.equals(connectedCmsRepository.getIdentityStoreRepositoryId(), connectedCmsRepository.getId())){
					clientForIdentityStore = client;
			}
			else{
				
				clientForIdentityStore = new AstroboaClient(client.getServerHostNameOrIpAndPortToConnectTo());
				clientForIdentityStore.login(connectedCmsRepository.getIdentityStoreRepositoryId(), 
						subject,permanentKey);
			}
			
					
		}

		
	}

	@Override
	public void updateUser(Person user) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			identityStoreSecure.updateUser(user, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			externalIdentityStore.updateUser(user);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		
	}


	@Override
	public List<Person> listUsersGrantedForRole(String role) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listUsersGrantedForRole(role, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listUsersGrantedForRole(role);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		

	}


	@Override
	public List<Person> listUsersGrantedNoRoles() {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listUsersGrantedNoRoles(getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listUsersGrantedNoRoles();
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
		

	}


	@Override
	public List<Person> findUsers(String filter) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.findUsers(filter, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.findUsers(filter);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
	}


	@Override
	public List<String> listRoles(String filter) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.listRoles(filter, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.listRoles(filter);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
	}


	public void loginAsAdministrator(String key,
			String permanentKey) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			CmsRepository connectedCmsRepository = client.getRepositoryService().getCmsRepository(
						AstroboaClientContextHolder.getActiveRepositoryId());
				
 			//In case repository has its own identity store
			if (StringUtils.equals(connectedCmsRepository.getIdentityStoreRepositoryId(), connectedCmsRepository.getId())){
					clientForIdentityStore = client;
			}
			else{
				
				clientForIdentityStore = new AstroboaClient(client.getServerHostNameOrIpAndPortToConnectTo());
				clientForIdentityStore.loginAsAdministrator(connectedCmsRepository.getIdentityStoreRepositoryId(), 
						key,permanentKey);
			}
		}

		
	}


	public void loginAsAnonymous(String permanentKey) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			CmsRepository connectedCmsRepository = client.getRepositoryService().getCmsRepository(
						AstroboaClientContextHolder.getActiveRepositoryId());
				
 			//In case repository has its own identity store
			if (StringUtils.equals(connectedCmsRepository.getIdentityStoreRepositoryId(), connectedCmsRepository.getId())){
					clientForIdentityStore = client;
			}
			else{
				
				clientForIdentityStore = new AstroboaClient(client.getServerHostNameOrIpAndPortToConnectTo());
				clientForIdentityStore.loginAsAnonymous(connectedCmsRepository.getIdentityStoreRepositoryId(),	permanentKey);
			}
		}

		
	}


	public String retrieveRoleDisplayName(String role, String language) {
		if (identityStoreSecure != null){
			if (successfullyConnectedToRemoteService){  
				clientForIdentityStore.activateClientContext();
			}
			return identityStoreSecure.retrieveRoleDisplayName(role, language, getAuthenticationTokenForIdentityStoreForCurrentlyConnectedRepository());
		}
		else if (externalIdentityStore != null){
			return externalIdentityStore.retrieveRoleDisplayName(role, language);
		}
		else{
			throw new CmsException("IdentityStore reference was not found");
		}
	}


	public AstroboaClient getAstroboaClientForIdentityStore() {
		return clientForIdentityStore;
	}


	public void setAstroboaClientForIdentityStore(AstroboaClient clientForIdentityStore) {
		
		this.clientForIdentityStore = clientForIdentityStore;
		
	}
}
