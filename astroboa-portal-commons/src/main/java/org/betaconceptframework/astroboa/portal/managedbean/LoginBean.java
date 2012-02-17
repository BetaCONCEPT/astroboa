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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.DisplayNamePrincipal;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountLockedException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidUsernameException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginPasswordExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedRepositoryUseException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
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
	
	@In(required=false)
	private String username;
    
	@In(required=false)
	private String password;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AstroboaClient astroboaClient;
	
	
	
	
	// Logs the user both at the Content Repository and at the Web App.
	// For all the identity operations (log in, log out, prevent page access according to user roles, etc.), the JBoss Seam infrastructure is used (mainly 
	// accessed through the "identity" bean)
	// BE AWARE that logging in the Content Repository is a different thing than logging in the web app that generates the
	// portal pages
	// It is not actually required to have the user logged in the Web App if the web app is not taking security decisions 
	// but just reads and writes content to the repository.
	
	// The traditional approach of Web Apps is to hold an identity store for logging in the users and then take care of all security issues.
	// When they need to read or write content from a database they do it as db administrators and run various rules to determine if the content should be shown to the user
	// or if the use can write content.
	// This means that each web app should again and again code security rules and on the other hand the content itself is not secured if it is accessed outside of the 
	// web app that knows all these rules, for example if another machine needs to mash up content.
	// Using Astroboa Repository, the  web app can be much more light and easier to build. 
	// Astroboa Repository is a service that maintains security rules about each action on the resources that it holds. 
	// The web app and any other software that connects to the repository is just a client and safely 
	// can be agnostic about content security because this is taken care by the repository itself.
	// The repository has an identity store and the client needs to log in as an existing user of the identity store before
	// it can read or write content to the repository.
	// Every time a client (the web app for example) asks for content it gets only the content that the logged in user is allowed to see. 
	// And every time it tries to write content the repository will check if it is allowed.

	// But what about the case where the client (web app) itself needs to maintain some knowledge about the logged in user?
	// To solve this we take the convenient decision that as long as the user is logged in to the repository 
	// then she will be automatically logged in to the web app with the same credentials.
	// So the identity store of the repository acts also as the identity store for the web application.
	// In the login method that follows we tell the seam identity bean (the bean that handles the identity and security for the web app) to use the astroboa jaas module 
	// to login the user (again) for the shake of the web app. 
	// In most cases this approach is quite convenient and you do not have to maintain two user repositories.
	// After all the main functionality of a portal is to read and write content so the users of the content repository are 
	// the users of your web app that merely exposes the repository content to the web.
	// In such a set up the seam identity bean will be loaded with the ROLES stored in the repository identity store 
	// So in order to take security decisions at the level of your Web App you may use the existing roles or
	// add some more roles just for the shake of your web app. You can control user roles through the astroboa console 
	// if you have set up astroboa with an internally managed identity store (the common case) and user identities are stored in astroboa. 
	// The only case that it will be required to keep a different identity store for the web app users is when you do not have admin access to the 
	// identity store of the repository and you cannot freely add / alter user profiles and roles. 
	public String login()  {
		
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			logger.error("An empty User Name or Password has been provided");
			facesMessages.add(Severity.WARN, "An empty User Name or Password has been provided. Please fill in your user name and password", (Object[]) null);
			logout();
			return null;
		}
		
		astroboaClient = null;
		
		// read repository settings from "portal.properties" file in classpath
		String currentlyConnectedRepositoryServer;
		String currentlyConnectedRepository;
		String systemSecretKey;
		
		try {
			PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
			currentlyConnectedRepositoryServer = portalConfiguration.getString(PortalStringConstants.ASTROBOA_SERVER);
			currentlyConnectedRepository = portalConfiguration.getString(PortalStringConstants.REPOSITORY);
			systemSecretKey = portalConfiguration.getString(PortalStringConstants.SYSTEM_SECRET_KEY);
		}
		catch (ConfigurationException e) {
			logger.error("A problem occured while reading repository client settings from portal configuration file.", e);
			facesMessages.add(Severity.WARN, "A problem occured while reading repository client settings from portal configuration file.", (Object[]) null);
			logout();
			return null;
		}
		
		try {
			
			// logout user from the repository by clearing the relevant context stored in this thread and by
			// removing the existing astroboa client stored in her session
			
			// clear previous context  (one is always created if any portal page is visited for the anonymous user 
			// but it may also exist because the user has already login and now tries to login again 
			AstroboaClientContextHolder.clearActiveClientContext();
			
			// remove existing astroboaClient variable from session context
			// if login fails then the AstroboaClientFactory will create a new client for the anonymous
			// If login succeeds the factory will replace the existing client by outjecting the client that we will create in this method   
			Contexts.getSessionContext().remove("astroboaClient");
			
			//logout user from the web app as well (remember that user is logged in bith at the repository level 
			// and as a Seam Framework identity at the level of the web app)
			logout();
			
			// connect client to repository server
			astroboaClient = new AstroboaClient(currentlyConnectedRepositoryServer);
			
			// login client with user's provided credentials
			AstroboaCredentials credentials = new AstroboaCredentials(username, password);
			astroboaClient.login(currentlyConnectedRepository, credentials);
			
			// if we reached here Login to the Repository is successful
			// Now we will set up Jboss Seam infrastructure so that user appears logged in also at the level of the web app
			
			//Instantiate Jboss Seam Identity module
			//Retrieve JAAS application security domain name
			AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
			
			if (clientContext == null || clientContext.getRepositoryContext() == null ||
					clientContext.getRepositoryContext().getCmsRepository() == null ||
					StringUtils.isBlank(clientContext.getRepositoryContext().getCmsRepository().getApplicationPolicyName())){
				logout();
				throw new Exception("No JAAS application policy name is provided for repository "+ currentlyConnectedRepository);
			}
			else{
				
				// loggedInRepositoryUser.reset();
				
				//Provide JAAS policy name to seam identity bean
				String jaasApplicationPolicyName = clientContext.getRepositoryContext().getCmsRepository().getApplicationPolicyName();
				
				// Check if logged in repository's identity store is externally managed. If so, record in LoggedInRepositoryUser bean that her id is
				// externally managed. This is required by certain UI modules in order to enable or disable user provisioning functionality.
				// loggedInRepositoryUser.setExternallyManagedIdentity(StringUtils.isNotBlank(clientContext.getRepositoryContext().getCmsRepository().getExternalIdentityStoreJNDIName()));
				
				
				Identity.instance().setJaasConfigName(jaasApplicationPolicyName);
				
				//Set credentials
				Identity.instance().getCredentials().setUsername(username);
				Identity.instance().getCredentials().setPassword(password);
				
				((CmsCredentials)Identity.instance().getCredentials()).setIdentityStoreRepositoryId(
						clientContext.getRepositoryContext().getCmsRepository().getIdentityStoreRepositoryId());
				
				((CmsCredentials)Identity.instance().getCredentials()).setIdentityStoreRepositoryJNDIName(
						clientContext.getRepositoryContext().getCmsRepository().getExternalIdentityStoreJNDIName());
				
				//Login to SEAM
				String loginOutcome =  Identity.instance().login();
				// clear seam messages
				facesMessages.clear();
				
				
				// if both logins (at the repository and at the web app) were successful 
				// we should also check if a Repository User Object exists in the repository for the logged in user
				// If a Repository User Object does not yet exist we create it.
				
				// For each user (physical person or machine) that needs to read and write content in a repository, 
				// the repository maintains a Repository User Object.
				// The Repository User object is very simple. 
				// It only keeps the Users's Display Name and her id at the identity store.
				// This allows to maintain the security information and rules for the content stored in the repository 
				// (e.g. who is the owner of a content object, who can read the content object, etc.)  and at the same time 
				// being agnostic about the identity store and its specific implementation.
				// Each Repository User corresponds to a real identity stored somewhere and the only thing that 
				// connects the Repository User to its corresponding identity is the external id kept inside each Repository User Object.
				// The external id is the the id by which the user is identified at the identity store. 
				// So every application that needs to know more about the Repository Users or needs to update their corresponding identity profile, 
				// uses the external id together with the identity service methods (the identity service is accessible through the astroboa client) 
				// to retrieve and write information in the identity store. 
				// In the common case the identity store is the repository itself or another Astroboa repository but 
				// the separation of identities from artificial Repository Users allows to also support identity stores which
				// are implemented outside Astroboa, for example use an LDAP, or a database or even a federated identity provider.
				if (loginOutcome != null) {
					RepositoryUser repositoryUser = 
						createOrRetrieveCorrespondingRepositoryUser(currentlyConnectedRepositoryServer, currentlyConnectedRepository, systemSecretKey);
					if (repositoryUser != null) {
						JSFUtilities.addMessage(null,"login.successful", new String[]{username}, FacesMessage.SEVERITY_INFO);
						logger.info("Successful login for user" + username);
						return loginOutcome;
					}
					else {
						JSFUtilities.addMessage(null,"login.authenticationServiceFailure", null, FacesMessage.SEVERITY_WARN);
						logger.warn("Authentication Service Failure for user" + username);
						logout();
						return null;
					}
				}
				else {
					JSFUtilities.addMessage(null,"login.invalidCredentials", null, FacesMessage.SEVERITY_WARN);
					logger.warn("Unable to authenticate user with Seam Identity Bean (at the web app level). User is" + username);
					logout();
					return null;
				}
				
			}
			
		}
		catch (CmsLoginInvalidUsernameException e) {
			JSFUtilities.addMessage(null,"login.invalidCredentials",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User performed a login with invalid user name");
			logout();
			return null;
		}
		catch (CmsInvalidPasswordException e) {
			JSFUtilities.addMessage(null,"login.invalidCredentials",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User performed a login with invalid password");
			logout();
			return null;
		}
		catch (CmsLoginAccountExpiredException e) {
			JSFUtilities.addMessage(null,"login.accountExpired",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User performed a login with an account that has expired");
			logout();
			return null;
		}
		catch (CmsLoginAccountLockedException e) {
			JSFUtilities.addMessage(null,"login.accountLocked",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User performed a login with an account that is locked");
			logout();
			return null;
		}
		catch (CmsLoginPasswordExpiredException e) {
			JSFUtilities.addMessage(null,"login.passwordExpired",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User performed a login with a password that has expired");
			logout();
			return null;
		}
		catch (CmsUnauthorizedRepositoryUseException e) {
			JSFUtilities.addMessage(null,"login.unauthorizedRepositoryAccess",null, FacesMessage.SEVERITY_WARN);
			logger.warn("User tried to login into a repository to which she is not authorized");
			logout();
			return null;
		}
		catch(Exception e){
			logger.warn("Authentication Service Failure", e);
			JSFUtilities.addMessage(null,"login.authenticationServiceFailure", null, FacesMessage.SEVERITY_WARN);
			logout();
			return null;
		}

	}
	
	public void logout() {
		Identity.instance().logout();
	}
	
	// supply seam identity bean with the correct principal
	// The "subject" object returned by default astroboa jaas module contains more than one principal objects that are not instances of class "Group" (i.e. it has both the username and userid) 
	// and seam default code 
	// cannot set the proper one since it expects only one principal object that is not instance of "Group" to exist inside subject 
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
	
	private RepositoryUser createOrRetrieveCorrespondingRepositoryUser(
			String currentlyConnectedRepositoryServer, 
			String currentlyConnectedRepository, 
			String systemSecretKey) {
		String username = Identity.instance().getPrincipal() != null? Identity.instance().getPrincipal().getName() : null;
		if (StringUtils.isEmpty(username)) {
			return null;
		}
		
		try {
			AstroboaClient astroboaClient;
			astroboaClient = new AstroboaClient(currentlyConnectedRepositoryServer);
			astroboaClient.loginAsAdministrator(currentlyConnectedRepository, systemSecretKey);

			RepositoryUserCriteria repositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
			List<RepositoryUser> resultRepositoryUsers;
			repositoryUserCriteria.addExternalIdEqualsCriterion(username);


			resultRepositoryUsers = astroboaClient.getRepositoryUserService().searchRepositoryUsers(repositoryUserCriteria);

			if (resultRepositoryUsers != null && resultRepositoryUsers.size() == 1)
				return resultRepositoryUsers.get(0);
			else if (resultRepositoryUsers != null && resultRepositoryUsers.size() > 1) { // OOPS!! we found more than one user with the same id. Some problem with the repository exists
				return null;
			}
			else {
				// this is the first time the user visits the repository. Lets create her as a repository user
				RepositoryUser userToBeCreated = astroboaClient.getCmsRepositoryEntityFactory().newRepositoryUser();
				// the external id of the Repository User is the Identity Principal by which logged in user is known to the IDP that keeps user identities.
				// The JAAS login module configured for astroboa should provide the user's identity inside a principal object of class IdentityPrincipal

				userToBeCreated.setExternalId(username);

				Set<DisplayNamePrincipal> displayNamePrincipals = Identity.instance().getSubject().getPrincipals(DisplayNamePrincipal.class);
				if (CollectionUtils.isNotEmpty(displayNamePrincipals)) {
					userToBeCreated.setLabel(displayNamePrincipals.iterator().next().getName());
				}
				else {
					userToBeCreated.setLabel(JSFUtilities.getLocalizedMessage("user.account.display.name.not.available", null));
				}

				return astroboaClient.getRepositoryUserService().save(userToBeCreated);
				
			}
		}
		catch (Exception e) {
			return null;
		}
		
	}
	
}
