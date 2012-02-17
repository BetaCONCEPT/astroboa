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
package org.betaconceptframework.astroboa.console.jsf.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountLockedException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidUsernameException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginPasswordExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedRepositoryUseException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.security.CmsCredentials;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser.UserActivityType;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.ui.jsf.comparator.SelectItemComparator;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.security.Identity;
import org.jboss.seam.web.ServletContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("login")
@Scope(ScopeType.EVENT)
public class LoginBean {
	
	@In
	FacesMessages facesMessages;
	
	private String userName;
    private String passWord;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final static String ASTROBOA_SERVER_KEY = "astroboa-server";

	private String repositoryIdToConnectTo;

	private String serverURLToConnectTo;

	private List<CmsRepository> availableCmsRepositoriesForSelectedRemoteServer;

	private AstroboaClient astroboaClient;
	
	private LoggedInRepositoryUser loggedInRepositoryUser;

	@In(create=true)
	private IdentityStoreRunAsSystem identityStoreRunAsSystem;
	
	
	@In(create=true)
	private LocaleSelector localeSelector;
	
	//Injected beans. Check components.xml
	private ContentObjectList contentObjectList;
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;

	public String login()  {

		try{
			
			AstroboaClientContextHolder.clearActiveClientContext();
			
			//Login to Astroboa repository
			astroboaClient.initialize(serverURLToConnectTo);
			astroboaClient.login(repositoryIdToConnectTo, new AstroboaCredentials(getUserName(), getPassWord().toCharArray()));
			
			//Login is successful
			//Instantiate Jboss' Seam Identity module
			//Retrieve JAAS application security domain name
			AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
			
			if (clientContext == null || clientContext.getRepositoryContext() == null ||
					clientContext.getRepositoryContext().getCmsRepository() == null ||
					StringUtils.isBlank(clientContext.getRepositoryContext().getCmsRepository().getApplicationPolicyName())){
				logout(false);
				throw new Exception("No JAAS application policy name is provided for repository "+ repositoryIdToConnectTo);
			}
			else{
				
				loggedInRepositoryUser.reset();
				
				//Provide JAAS policy name
				String jaasApplicationPolicyName = clientContext.getRepositoryContext().getCmsRepository().getApplicationPolicyName();
				
				// Check if logged in repository's identity store is externally managed. If so, record in LoggedInRepositoryUser bean that her id is
				// externally managed. This is required by certain UI modules in order to enable or disable user provisioning functionality.
				loggedInRepositoryUser.setExternallyManagedIdentity(StringUtils.isNotBlank(clientContext.getRepositoryContext().getCmsRepository().getExternalIdentityStoreJNDIName()));
				
				
				//Reset Identity
				Identity.instance().unAuthenticate();
				
				Identity.instance().setJaasConfigName(jaasApplicationPolicyName);
				
				//Set credentials
				Identity.instance().getCredentials().setUsername(getUserName());
				Identity.instance().getCredentials().setPassword(getPassWord());
				
				((CmsCredentials)Identity.instance().getCredentials()).setIdentityStoreRepositoryId(
						clientContext.getRepositoryContext().getCmsRepository().getIdentityStoreRepositoryId());
				
				((CmsCredentials)Identity.instance().getCredentials()).setIdentityStoreRepositoryJNDIName(
						clientContext.getRepositoryContext().getCmsRepository().getExternalIdentityStoreJNDIName());
				
				//Login to SEAM
				String loginOutcome =  Identity.instance().login();
				// clear seam messages
				facesMessages.clear();
				
				identityStoreRunAsSystem.reset();
				
				//Reset content object list
				if (contentObjectList != null)
				{
					contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
				}
				
				if (contentObjectStatefulSearchService != null)
				{
					contentObjectStatefulSearchService.setReturnedContentObjects(null);
					contentObjectStatefulSearchService.setSearchResultSetSize(0);
				}
				
				
				UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
				if (uiComponentBinding != null)
				{
					uiComponentBinding.resetContentObjectTableScrollerComponent();
					uiComponentBinding.setListViewContentObjectTableComponent(null);
					uiComponentBinding.setListViewContentObjectTableScrollerComponent(null);
					uiComponentBinding.setFullViewContentObjectTableComponent(null);
					uiComponentBinding.setFullViewContentObjectTableScrollerComponent(null);
				}
				
				
				// if login was successful we should also check if a Repository User exists for the logged in user 
				// A Repository User is automatically created upon first use of astroboa but we want to catch possible exceptions and prevent rendering the astroboa console page
				if (loginOutcome != null) {
					RepositoryUser repositoryUser = loggedInRepositoryUser.getRepositoryUser();
					if (repositoryUser != null) {
						JSFUtilities.addMessage(null,"login.welcome", null, FacesMessage.SEVERITY_INFO);
						
						// save the password in the session
						// this is required by the ResourceApiProxy in order to construct secured API calls to the Resource API.
						// This is a temporary solution until the new security framework is implemented (through URL signining)
						HttpServletRequest httpServletRequest = ServletContexts.instance().getRequest();
						if (httpServletRequest != null && httpServletRequest.getSession() != null) {
							httpServletRequest.getSession().setAttribute("repositoryPassword", passWord);
						}
						
						// write audit log if identity store is local, i.e. there is a local Person Object connected to the loggedin repository user
						loggedInRepositoryUser.updateConsloleLoginLog(UserActivityType.login);
						
						return loginOutcome;
					}
					else {
						//facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "login.authenticationServiceFailure", (Object[]) null);
						JSFUtilities.addMessage(null,"login.authenticationServiceFailure", null, FacesMessage.SEVERITY_ERROR);
						logout(false);
						return null;
					}
				}
				
				return null;
			}
			
		}
		catch (CmsLoginInvalidUsernameException e) {
			JSFUtilities.addMessage(null,"login.invalidCredentials",null, FacesMessage.SEVERITY_WARN);
			logger.info("User performed a login with invalid user name");
			logout(false);
			return null;
		}
		catch (CmsInvalidPasswordException e) {
			JSFUtilities.addMessage(null,"login.invalidCredentials",null, FacesMessage.SEVERITY_WARN);
			logger.info("User performed a login with invalid password");
			logout(false);
			return null;
		}
		catch (CmsLoginAccountExpiredException e) {
			JSFUtilities.addMessage(null,"login.accountExpired",null, FacesMessage.SEVERITY_WARN);
			logger.info("User performed a login with an account that has expired");
			logout(false);
			return null;
		}
		catch (CmsLoginAccountLockedException e) {
			JSFUtilities.addMessage(null,"login.accountLocked",null, FacesMessage.SEVERITY_WARN);
			logger.info("User performed a login with an account that is locked");
			logout(false);
			return null;
		}
		catch (CmsLoginPasswordExpiredException e) {
			JSFUtilities.addMessage(null,"login.passwordExpired",null, FacesMessage.SEVERITY_WARN);
			logger.info("User performed a login with a password that has expired");
			logout(false);
			return null;
		}
		catch (CmsUnauthorizedRepositoryUseException e) {
			JSFUtilities.addMessage(null,"login.unauthorizedRepositoryAccess",null, FacesMessage.SEVERITY_WARN);
			logger.info("User tried to login into a repository to which she is not authorized");
			logout(false);
			return null;
		}
		catch(Exception e){
			logger.error("An error occured during user authentication",e);
			JSFUtilities.addMessage(null,"login.authenticationServiceFailure", null, FacesMessage.SEVERITY_ERROR);
			logout(false);
			return null;
		}

	}
	
	// this is used by page actions so we want logging to be enabled
	public void logout() {
		logout(true);
	}
	
	private void logout(boolean updateConsoleLoginLog) {
		
		if (updateConsoleLoginLog) {
			try {
				loggedInRepositoryUser.updateConsloleLoginLog(UserActivityType.logout);
			}
			catch (Exception e) {
				logger.error("An error occured during the update of conslole login log to record user logout. The logout will proceed normally without any notification to the user.", e);
			}
		}	
		
		Identity.instance().logout();
		
		identityStoreRunAsSystem.reset();
		
		loggedInRepositoryUser.reset();
	}
	
	@Observer(Identity.EVENT_PRE_AUTHENTICATE)
	public void configSeamPrincipal(){
		
		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		
		//Expect to find a principal of Type IdentityPrincipal
		//inside Subject's Principals
		if (securityContext == null 
				|| securityContext.getSubject() == null 
				|| CollectionUtils.isEmpty(securityContext.getSubject().getPrincipals(IdentityPrincipal.class))){
			throw new SecurityException("Identity Principal is missing");
		}
		else{
			Identity.instance().acceptExternallyAuthenticatedPrincipal(new IdentityPrincipal(securityContext.getSubject().getPrincipals(IdentityPrincipal.class).iterator().next().getName()));
			
		}	
	}

	public void setRepositoryIdToConnectTo(String repositoryIdToConnectTo) {
		this.repositoryIdToConnectTo = repositoryIdToConnectTo;
	}


	public String getRepositoryIdToConnectTo() {
		return repositoryIdToConnectTo;
	}


	public String getServerURLToConnectTo() {
		return serverURLToConnectTo;
	}

	public void setServerURLToConnectTo(String serverURLToConnectTo) {
		this.serverURLToConnectTo = serverURLToConnectTo;
	}


	public List<SelectItem> getAvailableServers(){

		List<SelectItem> availableAstroboaServers = new ArrayList<SelectItem>();

		try {

			PropertiesConfiguration consoleConfiguration = new PropertiesConfiguration("astroboa-console.properties");
			consoleConfiguration.setEncoding("UTF-8");

			@SuppressWarnings("unchecked")
			List<Object> astroboaServers = consoleConfiguration.getList(ASTROBOA_SERVER_KEY);

			if(CollectionUtils.isNotEmpty(astroboaServers)){

				for (Object astroboaServer : astroboaServers){
					//Get server host or ip
					String astroboaHostOrIp = consoleConfiguration.getString((String)astroboaServer+".host");

					if (StringUtils.isBlank(astroboaHostOrIp)){
						astroboaHostOrIp = AstroboaClient.INTERNAL_CONNECTION; 
					}

					//Get port
					String astroboaHostPort = consoleConfiguration.getString((String)astroboaServer+".port");

					if (StringUtils.isBlank(astroboaHostPort)){
						astroboaHostPort = "1099"; 
					}

					//Get localized label
					String localizedLabel = consoleConfiguration.getString((String)astroboaServer+"."+ localeSelector.getLocaleString());

					if (StringUtils.isBlank(localizedLabel)){
						localizedLabel = (String)astroboaServer;
					}

					availableAstroboaServers.add(new SelectItem(astroboaHostOrIp+":"+astroboaHostPort, localizedLabel));
				}


				if (availableAstroboaServers.size() > 1){
					SelectItemComparator selectItemComparator = new SelectItemComparator();
					selectItemComparator.setLocale(localeSelector.getLocaleString());
					Collections.sort(availableAstroboaServers, selectItemComparator);
				}
			}

		} catch (ConfigurationException e) {
			logger.error("",e);
		}


		return availableAstroboaServers;
	}


	public void connectToSelectedServer(){

		try{

			astroboaClient.initialize(serverURLToConnectTo);

		}
		catch (Exception e){
			logger.error("", e);
		}
		finally{
			repositoryIdToConnectTo = null;
			availableCmsRepositoriesForSelectedRemoteServer = null;	
		}

	}

	public List<CmsRepository> getAvailableCmsRepositoriesForSelectedServer(){

		if (availableCmsRepositoriesForSelectedRemoteServer != null){
			return availableCmsRepositoriesForSelectedRemoteServer;
		}

		//Check if repository service is available
		if (astroboaClient.getRepositoryService() == null){
			connectToSelectedServer();
		}

		//If repository service is available get all repositories
		if (astroboaClient.getRepositoryService() != null){

			try{
				availableCmsRepositoriesForSelectedRemoteServer =  astroboaClient.getRepositoryService().getAvailableCmsRepositories();
			}
			catch(Exception e){
				logger.error("",e);
			}
		}

		return availableCmsRepositoriesForSelectedRemoteServer == null ? new ArrayList<CmsRepository>() :availableCmsRepositoriesForSelectedRemoteServer;
	}

	public void setAstroboaClient(
			AstroboaClient astroboaClient) {
		this.astroboaClient = astroboaClient;
	}


	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}


	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}


	/**
	 * @return the passWord
	 */
	public String getPassWord() {
		return passWord;
	}


	/**
	 * @param passWord the passWord to set
	 */
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}


	
}
