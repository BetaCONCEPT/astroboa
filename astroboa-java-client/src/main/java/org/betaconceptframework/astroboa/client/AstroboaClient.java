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
package org.betaconceptframework.astroboa.client;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsClientNotLoggedInException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.client.service.ContentServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.DefinitionServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.IdentityStoreClientWrapper;
import org.betaconceptframework.astroboa.client.service.ImportServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.RepositoryServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.RepositoryUserServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.SerializationServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.SpaceServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.TaxonomyServiceClientWrapper;
import org.betaconceptframework.astroboa.client.service.TopicServiceClientWrapper;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;


/**
 * This is the starting point for accessing a Astroboa repository server and 
 * make use of its services.
 * 
 * <p>
 * In order to successfully use services of a Astroboa repository server, Astroboa repository client
 * must successfully connect to a repository at that server.
 * </p>
 * 
 * <p>
 * This can be done following either ways :
 * <ul>
 * <li>Declaring ONLY the server name on the constructor {@link #AstroboaClient(String)} and at some point of time BEFORE using any of 
 * the services, user must login using one of the provided login methods.
 * <li>Instantiate the client with the default constructor {@link #AstroboaClient()}, supply Astroboa server with 
 * {@link #initialize(String)} and then BEFORE using any of the 
 * services, user must login using one of the provided login methods.
 * </ul>
 * </p>
 * 
 * <p>
 * Astroboa repository client can connect to a local or a remote Astroboa repository server. A local Astroboa repository server
 * runs in the same JVM with this client whereas a remote Astroboa repository server runs in a different JVM from this client's JVM.
 * </p>
 * 
 * <p>
 * Astroboa repository client determines whether to connect to a local or remote Astroboa repository server from the provided
 * server name. If this is BLANK or has the value {@link #INTERNAL_CONNECTION} then it tries to find a Astroboa repository server in 
 * the currently running JVM, otherwise it uses the provided server name or ip and port to connect through TCP/IP to a remote server.
 * </p>
 * 
 * <p>
 * Note that if the specified server name is <code>localhost</code> or <code>127.0.0.1</code> then it will try first to connect locally 
 * and if that is not successful then it will try to connect remotely.
 * </p>
 * 
 * <p>
 * By default all Astroboa repository servers listen to default port 1099. If this is changed then append the server name or ip
 * with the new port separated by ':', e.g. localhost:1098 
 * </p>
 * 
 * <p>
 * Astroboa repository client provides also access to the {@link IdentityStore} for the connected repository.
 * </p>
 * 
 * <p>
 * The main idea is that there could be different users accessing an {@link IdentityStore} which by its turn
 * manages users for a Astroboa repository.
 * </p>
 * 
 * <p>
 * Finally, it should be noted that this class is NOT Thread safe, i.e. user must make sure that the same instance 
 * must not be accessed by more than one threads at the same time.
 * 
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaClient {

	public final static String INTERNAL_CONNECTION = "internalConnection";

	private String serverHostNameOrIpAndPortToConnectTo;

	private ContentService contentService;

	private RepositoryService repositoryService;

	private RepositoryUserService repositoryUserService;

	private TaxonomyService taxonomyService;

	private TopicService topicService;

	private SpaceService spaceService;

	private DefinitionService definitionService;
	
	private SerializationService serializationService;
	
	private ImportService importService;

	private String authenticationToken;

	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	private AstroboaClientContext clientContext;

	private IdentityStore identityStore;

	private String loggedInUser;
	
	private String connectedRepositoryId;

	private String externalIdentityStoreJNDIName;
	
	public AstroboaClient(){

	}

	public AstroboaClient(String serverHostNameOrIpAndPortToConnectTo){
		initialize(serverHostNameOrIpAndPortToConnectTo);
	}

	/**
	 * @see RepositoryService#login(String, AstroboaCredentials)
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param credentials Credentials used to authenticate user that wants to connect to repository.
	 */
	public  void login(String repositoryId, AstroboaCredentials credentials) {
		login(repositoryId, credentials, null);
	}

	/**
	 * 
	 * @see RepositoryService#login(String, AstroboaCredentials, String)
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param credentials Credentials used to authenticate user that wants to connect to repository.
     *
	 * @param permanentKey  representing a trusted client whose token is never expired
	 */
	public  void login(String repositoryId, AstroboaCredentials credentials, String permanentKey) {

		if(getRepositoryService() != null){
			authenticationToken = getRepositoryService().login(repositoryId, credentials, permanentKey);
			cmsRepositoryEntityFactory = new CmsRepositoryEntityFactory(){

				@Override
				public String getAuthenticationToken() {
					return authenticationToken;
				}

			};
			
			if (getIdentityStore()!=null){
				//Login to Identity Store as well with the same credentials
				((IdentityStoreClientWrapper)getIdentityStore()).login(credentials, permanentKey);
			}
			
			if (credentials == null){
				keepRepositoryAndUser(repositoryId, "unknown");
			}
			else{
				keepRepositoryAndUser(repositoryId, credentials.getUsername());
			}
			
		}
	}

	/**
	 * Allows client to login to an Astroboa repository without authenticating
	 * user. This method runs only within Local context for security reasons.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param subject Subject created during authentication by another system. 
	 */
	public  void login(String repositoryId, Subject subject) {
		login(repositoryId, subject, null);
	}

	/**
	 * Allows client to login to an Astroboa repository without authenticating
	 * user. This method runs only within Local context for security reasons.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param subject Subject created during authentication by another system.
	 * 
	 * @param permanentKey  representing a trusted client whose token is never expired
	 */
	public  void login(String repositoryId, Subject subject, String permanentKey) {

		if (getRepositoryService() != null){
			authenticationToken = ((RepositoryServiceClientWrapper)getRepositoryService()).login(repositoryId, subject, permanentKey);
			cmsRepositoryEntityFactory = new CmsRepositoryEntityFactory(){

				@Override
				public String getAuthenticationToken() {
					return authenticationToken;
				}

			};
			
			//Login to Identity Store as well with the same credentials
			if (getIdentityStore()!=null)
			{
				((IdentityStoreClientWrapper)getIdentityStore()).login(subject, permanentKey);
			}
			
			if (subject == null )
			{
				//Normally this should never happen
				keepRepositoryAndUser(repositoryId, "unknown");
			}
			else if (subject.getPrincipals(IdentityPrincipal.class)!= null &&
					! subject.getPrincipals(IdentityPrincipal.class).isEmpty())
			{
				keepRepositoryAndUser(repositoryId, subject.getPrincipals(IdentityPrincipal.class).iterator().next().getName());
			}
			else
			{
				keepRepositoryAndUser(repositoryId, subject.toString());
			}
		}
	}

	/**
	 * Allows client to login to an Astroboa repository without providing 
	 * admin user name and password.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param key Predefined secret key provided in astroboa-conf.xml 
	 * 
	 */
	public  void loginAsAdministrator(String repositoryId, String key) {
		loginAsAdministrator(repositoryId, key, null);
	}
	
	/**
	 * Allows client to login to an Astroboa repository without providing 
	 * admin user name and password.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param key Predefined secret key provided in astroboa-conf.xml 
	 * 
	 * @param permanentKey  representing a trusted client whose token is never expired
	 */
	public  void loginAsAdministrator(String repositoryId, String key, String permanentKey) {

		if (getRepositoryService() != null){
			authenticationToken = ((RepositoryServiceClientWrapper)getRepositoryService()).
			loginAsAdministrator(repositoryId, key, permanentKey);
			cmsRepositoryEntityFactory = new CmsRepositoryEntityFactory(){

				@Override
				public String getAuthenticationToken() {
					return authenticationToken;
				}

			};
			
			//Login to Identity Store as well with the same credentials
			if (getIdentityStore()!=null)
			{
				((IdentityStoreClientWrapper)getIdentityStore()).loginAsAdministrator(key, permanentKey);
			}
			
			keepRepositoryAndUser(repositoryId, "Administrator defined in configuration");
		}
	}
	
	/**
	 * Allows client to login to an Astroboa repository as Anonymous user. 
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 */
	public  void loginAsAnonymous(String repositoryId) {
		loginAsAnonymous(repositoryId, null);
	}
	
	/**
	 * Allows client to login to an Astroboa repository as Anonymous user.  
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param permanentKey  representing a trusted client whose token is never expired
	 */
	public  void loginAsAnonymous(String repositoryId, String permanentKey) {

		if (getRepositoryService() != null){
			authenticationToken = ((RepositoryServiceClientWrapper)getRepositoryService()).
			loginAsAnonymous(repositoryId, permanentKey);
			cmsRepositoryEntityFactory = new CmsRepositoryEntityFactory(){

				@Override
				public String getAuthenticationToken() {
					return authenticationToken;
				}

			};
			
			//Login to Identity Store as well with the same credentials
			if (getIdentityStore()!=null)
			{
				((IdentityStoreClientWrapper)getIdentityStore()).loginAsAnonymous(permanentKey);
			}
			
			keepRepositoryAndUser(repositoryId, IdentityPrincipal.ANONYMOUS);
		}
	}

	/**
	 * Configure client to connect to provided Astroboa Server
	 * 
	 * @param serverHostNameOrIpAndPortToConnectTo The dns name or ip of the server that provides the Astroboa Repository Services
	 */
	public  void initialize(String serverHostNameOrIpAndPortToConnectTo) {

		this.serverHostNameOrIpAndPortToConnectTo = serverHostNameOrIpAndPortToConnectTo;

		resetClientState();

	}

	private void resetClientState(){
		contentService = null;
		repositoryService = null;
		repositoryUserService = null;
		taxonomyService = null;
		topicService = null;
		spaceService = null;
		definitionService = null;
		serializationService = null;
		importService = null;
		identityStore = null;
		
		authenticationToken = null;
		cmsRepositoryEntityFactory = null;
		connectedRepositoryId = null;
		clientContext = null;
		loggedInUser = null;
		externalIdentityStoreJNDIName = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public AstroboaClient copy(){
		
		if (authenticationToken == null){
			throw new CmsClientNotLoggedInException(serverHostNameOrIpAndPortToConnectTo);
		}
		
		AstroboaClient clone = new AstroboaClient(serverHostNameOrIpAndPortToConnectTo);
		clone.authenticationToken = new String(authenticationToken.getBytes());
		clone.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
		clone.clientContext = clientContext;
		clone.externalIdentityStoreJNDIName = externalIdentityStoreJNDIName;
		clone.clientContext = clientContext;
		
		if (connectedRepositoryId != null){
			clone.connectedRepositoryId = new String(connectedRepositoryId);
		}
		
		if (loggedInUser != null){
			clone.loggedInUser = new String(loggedInUser);
			
		}
		
		if (clone.getIdentityStore()!=null){
			
			AstroboaClient identityStoreClient = ((IdentityStoreClientWrapper)getIdentityStore()).getAstroboaClientForIdentityStore();
			
			if (identityStoreClient == null ||
					identityStoreClient == this){
				((IdentityStoreClientWrapper)clone.getIdentityStore()).setAstroboaClientForIdentityStore(clone);
			}
			else{
				((IdentityStoreClientWrapper)clone.getIdentityStore()).setAstroboaClientForIdentityStore(identityStoreClient.copy());
			}
		}
		
		clone.activateClientContext();
		
		return clone;
		
	}

	/**
	 * Retrieve {@link ContentService}
	 * 
	 * @return Content Service
	 */
	public  ContentService getContentService(){
		if (contentService == null){
			contentService = new ContentServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return contentService;
	}

	/**
	 * Retrieve {@link RepositoryService}
	 * 
	 * @return Repository Service
	 */
	public  RepositoryService getRepositoryService(){
		if (repositoryService == null){
			repositoryService = new RepositoryServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return repositoryService;
	}

	/**
	 * Retrieve {@link RepositoryUserService}
	 * 
	 * @return RepositoryUserService
	 */
	public  RepositoryUserService getRepositoryUserService(){
		if (repositoryUserService == null){
			repositoryUserService = new RepositoryUserServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return repositoryUserService;
	}

	/**
	 * Retrieve Astroboa server dns name of ip
	 * 
	 * @return Astroboa server dns name of ip
	 */
	public String getServerHostNameOrIpAndPortToConnectTo() {
		return serverHostNameOrIpAndPortToConnectTo;
	}

	/**
	 * Retrieve {@link TaxonomyService}
	 * 
	 * @return TaxonomyService
	 */
	public  TaxonomyService getTaxonomyService() {
		if (taxonomyService == null){
			taxonomyService = new TaxonomyServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return taxonomyService;
	}

	/**
	 * Retrieve {@link TopicService}
	 * 
	 * @return TopicService
	 */
	public  TopicService getTopicService() {
		if (topicService == null){
			topicService = new TopicServiceClientWrapper(this,serverHostNameOrIpAndPortToConnectTo);
		}

		return topicService;
	}
	
	/**
	 * Retrieve {@link SpaceService}
	 * 
	 * @return SpaceService
	 */
	public  SpaceService getSpaceService() {
		if (spaceService == null){
			spaceService = new SpaceServiceClientWrapper(this,serverHostNameOrIpAndPortToConnectTo);
		}

		return spaceService;

	}
	
	/**
	 * Retrieve {@link DefinitionService}
	 * 
	 * @return DefinitionService
	 */
	public  DefinitionService getDefinitionService() {
		if (definitionService == null){
			definitionService = new DefinitionServiceClientWrapper(this,serverHostNameOrIpAndPortToConnectTo);
		}

		return definitionService;
	}


	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	/**
	 * Retrieve {@link CmsRepositoryEntityFactory}
	 * 
	 * @return CmsRepositoryEntityFactory
	 */
	public  CmsRepositoryEntityFactory getCmsRepositoryEntityFactory(){

		if (cmsRepositoryEntityFactory== null){
			throw new CmsException("Astroboa client must log in before it can use the entity factory");
		}

		return cmsRepositoryEntityFactory;
	}
	
	/**
	 * Used to activate this client's context in current Thread
	 */
	public void activateClientContext(){
		if (clientContext != null){
			AstroboaClientContextHolder.registerClientContext(clientContext, true);
		}
		else if (authenticationToken != null){
			//We have authentication token. Use that
			AstroboaClientContextHolder.activateClientContextForAuthenticationToken(authenticationToken);
		}
	}

	/**
	 * @param clientContext the clientContext to set
	 */
	public void setClientContext(AstroboaClientContext clientContext) {
		this.clientContext = clientContext;
	}
  
	/**
	 * Retrieve {@link IdentityStore}
	 * 
	 * @return IdentityStore
	 */
	public  IdentityStore getIdentityStore() {
		if (identityStore == null){
			
			if (StringUtils.isBlank(authenticationToken)){
				throw new CmsException("You have to login to repository before you can use IdentityStore");
			}
			
			identityStore = new IdentityStoreClientWrapper(this,serverHostNameOrIpAndPortToConnectTo);
		}

		return identityStore;
	}

	/**
	 * Return information about Astroboa server, Astroboa repository and logged in user
	 * @return
	 */
	public String getInfo(){
		StringBuilder builder = new StringBuilder();
		builder.append("AstroboaClient for server ");
		builder.append((StringUtils.isBlank(serverHostNameOrIpAndPortToConnectTo) ||  INTERNAL_CONNECTION.equals(serverHostNameOrIpAndPortToConnectTo) ? "localhost":
				serverHostNameOrIpAndPortToConnectTo));
		builder.append(", Repository : ");
		builder.append(connectedRepositoryId);
		
		builder.append(", User : ");
		builder.append(loggedInUser);

		builder.append(", Authentication Token : ");
		builder.append(authenticationToken);

		return builder.toString();

	}
	
	private void keepRepositoryAndUser(String repositoryId, String username){
		connectedRepositoryId  =repositoryId;
		loggedInUser = username;
	}

	public String getConnectedRepositoryId() {
		return connectedRepositoryId;
	}


	public boolean isConnectedToARemoteServer(){
		if (getRepositoryService() != null){
			return ((RepositoryServiceClientWrapper)getRepositoryService()).isClientConnectedToARemoteServer();
		}
		
		return false;
	}
	
	/**
	 * Retrieve {@link SerializationService}
	 * 
	 * @return Export Service
	 */
	public  SerializationService getSerializationService(){
		if (serializationService == null){
			serializationService = new SerializationServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return serializationService;
	}

	/**
	 * Retrieve {@link ImportService}
	 * 
	 * @return Import Service
	 */
	public  ImportService getImportService(){
		if (importService == null){
			importService = new ImportServiceClientWrapper(this, serverHostNameOrIpAndPortToConnectTo);
		}

		return importService;
	}

	@Override
	public String toString() {
		return getInfo();
	}

	public void setExternalIdentityStoreJNDIName(
			String externalIdentityStoreJNDIName) {
		this.externalIdentityStoreJNDIName = externalIdentityStoreJNDIName;
		
	}

	public String getExternalIdentityStoreJNDIName() {
		return externalIdentityStoreJNDIName;
	}
	

	public boolean sessionHasExpired(){
		return authenticationToken == null || 
		(getRepositoryService() != null && ((RepositoryServiceClientWrapper)getRepositoryService()).tokenHasExpired(authenticationToken));
	}
}
