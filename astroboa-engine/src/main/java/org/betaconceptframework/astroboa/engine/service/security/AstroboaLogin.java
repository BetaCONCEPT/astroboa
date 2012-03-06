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
package org.betaconceptframework.astroboa.engine.service.security;

import java.io.IOException;
import java.security.acl.Group;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.DisplayNamePrincipal;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.PersonUserIdPrincipal;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryDao;
import org.betaconceptframework.astroboa.security.AstroboaAuthenticationCallback;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.security.management.CmsPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This class is responsible for the authentication of a user who wants
 * to connect to an Astroboa repository.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaLogin {

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	private IdentityStore identityStore;
	
	private boolean useExternalIdentity;

	private Person loggedInPerson = null;

	private Subject subject;

	private CallbackHandler callbackHandler;

	private IdentityPrincipal identity;

	private String repositoryId;

	private RepositoryDao repositoryDao;

	private String authenticationTokenForSYSTEMofInternalIdentityStore;

	
	public AstroboaLogin(CallbackHandler callbackHandler, IdentityStore internalIdentityStore, RepositoryDao repositoryDao) {

		this.subject = new Subject();
		this.callbackHandler = callbackHandler;
		this.identity = null;
		this.identityStore = internalIdentityStore;
		this.loggedInPerson = null;
		this.useExternalIdentity = false;
		this.repositoryId = null;
		this.repositoryDao = repositoryDao;
		
	}
	
	/**
	 * Override login to provide extra checks in case user credentials are
	 * correct
	 * @return 
	 */
	public Subject login() throws LoginException {

		boolean loginIsSuccessful =  internalLogin();

		if ( loginIsSuccessful == true ){

			if (!loggedInPerson.isEnabled()) {
				throw new AccountNotFoundException(getUsername());
			}

			//Add identity 
			addIdentityPrincipalToSubject();

			//Add PersonUserIdPrincipal to subject
			addPersonUserIdPrincipalToSubject();

			//Add display name principal
			addDisplayNamePrincipalToSubject();

			//Add roles to subject
			addRolesToSubject();
			
			return subject;
		}
		else{
			throw new LoginException(getUsername());
		}
	
	}

	private String getUsername() {
		if (identity != null){
			return identity.getName();
		}
		return null;
	}




	private boolean internalLogin() throws LoginException {

		String[] credentialsInfo = getAuthenticationInformation();

		String username = credentialsInfo[0];
		String password = credentialsInfo[1];
		String secretKey = credentialsInfo[2];
		String identityStoreLocation = credentialsInfo[3];
		repositoryId = credentialsInfo[4];


		/*
		 * Special case user is ANONYMOUS
		 */
		if (userIsAnonymous(username))
		{
			identity = new IdentityPrincipal(IdentityPrincipal.ANONYMOUS);

			loadAnonymousPerson();

			return true;
		}
		
		/*
		 * User is not ANONYMOUS
		 */
		initializeIdentityStore(identityStoreLocation);
		

		//All went well
		//Authenticate using IdentityStore
		//If no password is provided then check only if user exists 
		//This must be reviewed as we allow anyone to obtain a user's roles
		if (password == null)
		{
			//User must exist
			if (! getIdentityStore().userExists(username))
			{
				throw new FailedLoginException(username);
			}
			
			//Also to allow login a secret key must be provided and it must match for this user
			if (! RepositoryRegistry.INSTANCE.isSecretKeyValidForUser(repositoryId, username, secretKey))
			{
				throw new FailedLoginException(username);
			}
			
		}
		else if (!getIdentityStore().authenticate(username, password))
		{
			throw new FailedLoginException(username);	
		}

		loadPersonByUserName(username);
		
		//Identity is username as returned by Identity Store
		//Since username may be case insensitive we do not keep value entered by
		//user but rather we keep value returned from IdentityStore
		//which is responsible to provide username entered during user
		//registration   
		//identity = new IdentityPrincipal(username);
		if (loggedInPerson == null || StringUtils.isBlank(loggedInPerson.getUsername()))
		{
			throw new FailedLoginException("No username returned from IdentityStore during login of user "+username);
		}
		
		identity = new IdentityPrincipal(loggedInPerson.getUsername());

		return true;
	}

	private void initializeIdentityStore(String identityStoreLocation)
			throws FailedLoginException {
		
		if (StringUtils.isBlank(identityStoreLocation)){
			throw new FailedLoginException("No identity store location is provided");
		}
		
		if (useExternalIdentity){
			initializeExternalIdentityStore(identityStoreLocation);
		}
		else{
			setupContextForInternalIdentityStore(identityStoreLocation);
		}
		
		if (identityStore == null){
			throw new FailedLoginException("Could not initialize identity store in location (either a JNDI name or a repository id)"+ identityStoreLocation);
		}
	}

	private void loadAnonymousPerson() {
		
		loggedInPerson = new CmsPerson();
		loggedInPerson.setDisplayName(IdentityPrincipal.ANONYMOUS);
		loggedInPerson.setEnabled(true);
		loggedInPerson.setFamilyName(IdentityPrincipal.ANONYMOUS);
		loggedInPerson.setFatherName(IdentityPrincipal.ANONYMOUS);
		loggedInPerson.setFirstName(IdentityPrincipal.ANONYMOUS);
		loggedInPerson.setUserid(IdentityPrincipal.ANONYMOUS);
		loggedInPerson.setUsername(IdentityPrincipal.ANONYMOUS);
		
	}

	private boolean userIsAnonymous(String username) {
		return StringUtils.equals(IdentityPrincipal.ANONYMOUS, username);
	}

	private void initializeExternalIdentityStore(String identityStoreLocation) throws FailedLoginException {
		try {
			InitialContext context = new InitialContext();

			//First check to see if initial context has been initiated at all
			Hashtable<?, ?> env = context.getEnvironment();
			String initialContextFactoryName = env != null ?
					(String)env.get(Context.INITIAL_CONTEXT_FACTORY) : null;

					if (StringUtils.isNotBlank(initialContextFactoryName)){

						Object serviceReference = context.lookup(identityStoreLocation);
						
						if (! (serviceReference instanceof IdentityStore)){
							if (! identityStoreLocation.endsWith("/local")){
								//JNDIName is provided by the user and the object it references is not an instance of IdentityStore.
								//It is probably an instance of NamingContext which is on top of a local or remote service
								//Since JNDIName does not end with "/local" , try to locate the local service under the returned NamingContext
								identityStore = (IdentityStore) context.lookup(identityStoreLocation+"/local");
							}
							else{
								throw new Exception("JNDI Name "+identityStoreLocation+ " refers to an object whose type is not IdentityStore. Unable to locate. External Identity Store ");
							}
						}
						else{
							identityStore = (IdentityStore) serviceReference;
						}
						//TODO: It may also be the case another login to the identity store must be done

					}
					else{
						throw new Exception("Initial Context Factory Name is blank therefore no initial context is configured, thus any lookup will result in exception." +
								"External Identity Store "+identityStoreLocation);
					}

		} catch (Exception e) {
			logger.error("",e);
			throw new FailedLoginException("During connection to external Identity Store "+ identityStoreLocation);
		}

	}

	private void setupContextForInternalIdentityStore(String identityStoreRepositoryId) {

		//Since we are using the internal identity store, we must setup the security context
		//for the user who will be used to connect to the repository which represents the
		//identity store. This user is the SYSTEM user by default and thus we perform
		//an internal login without the need of the SYSTEM's password
		Subject subject = new Subject();

		//System identity
		subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));

		//Grant SYSTEM all roles
		Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());

		for (CmsRole cmsRole : CmsRole.values()){
			rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, identityStoreRepositoryId)));
		}
		subject.getPrincipals().add(rolesPrincipal);
		
		//Login using the Subject, the provided roles and SYSTEM's permanent key and get the authentication token
		authenticationTokenForSYSTEMofInternalIdentityStore = repositoryDao.login(identityStoreRepositoryId, subject, RepositoryRegistry.INSTANCE.getPermanentKeyForUser(identityStoreRepositoryId, IdentityPrincipal.SYSTEM));

	}

	private void loadPersonByUserName(String username) throws LoginException {

		if (loggedInPerson == null){
			try{

				loggedInPerson = getIdentityStore().retrieveUser(username); 

			}
			catch(Exception e){
				logger.error("Problem when loading person for username "+username, e);
				throw new LoginException("Problem when loading person for username "+username);
			}

			if (loggedInPerson == null){
				throw new AccountNotFoundException(username);
			}

		}
	}

	private void addDisplayNamePrincipalToSubject() {

		if (StringUtils.isNotBlank(loggedInPerson.getDisplayName())){
			subject.getPrincipals().add(new DisplayNamePrincipal(loggedInPerson.getDisplayName()));
		}
	}


	private void addPersonUserIdPrincipalToSubject() {

		String personUserId = loggedInPerson.getUserid();

		if (StringUtils.isNotBlank(personUserId)){
			subject.getPrincipals().add(new PersonUserIdPrincipal(personUserId));
		}
	}


	private void addIdentityPrincipalToSubject() {
		subject.getPrincipals().add(identity);
	}


	private void addRolesToSubject() throws LoginException {


		//Must return at list one group named "Roles" in order to be 
		final Group groupContainingAllRoles = new CmsGroup(AstroboaPrincipalName.Roles.toString());

		if (userIsAnonymous(getUsername())){

			//User ANONYMOUS is a virtual user which must have specific role
			//regardless of whether the identity store is external or internal
			groupContainingAllRoles.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER, repositoryId)));
		}
		else{
			List<String> impliedRoles = getIdentityStore().getImpliedRoles(getUsername());

			//Load all roles in a tree
			if (impliedRoles != null){

				for (String impliedRole : impliedRoles){
					groupContainingAllRoles.addMember(new CmsPrincipal(impliedRole));
				}		
			}
		}

		subject.getPrincipals().add(groupContainingAllRoles);
	}


	/**
	 * 
	 * TAKEN FROM Jboss class
	 *  
	 * org.jboss.security.auth.spi.UsernamePasswordLoginModule
	 * 
	 * and adjust it to Astroboa requirements
	 * 
	 * @return
	 * @throws LoginException
	 */
	private String[] getAuthenticationInformation() throws LoginException
	{
		String[] info = {null, null, null, null, null};
		// prompt for a username and password
		if( callbackHandler == null )
		{
			throw new LoginException("Error: no CallbackHandler available " +
			"to collect authentication information");
		}

		NameCallback nc = new NameCallback("User name: ", "guest");
		PasswordCallback pc = new PasswordCallback("Password: ", false);
		AstroboaAuthenticationCallback authenticationCallback = new AstroboaAuthenticationCallback("Astroboa authentication info");
		

		Callback[] callbacks = {nc, pc, authenticationCallback};
		String username = null;
		String password = null;
		String identityStoreLocation = null;
		String userSecretKey = null;
		String repositoryId = null;

		try{
			callbackHandler.handle(callbacks);
			username = nc.getName();
			char[] tmpPassword = pc.getPassword();
			if( tmpPassword != null )
			{
				char[] credential = new char[tmpPassword.length];
				System.arraycopy(tmpPassword, 0, credential, 0, tmpPassword.length);
				pc.clearPassword();
				password = new String(credential);
			}

			identityStoreLocation = authenticationCallback.getIdentityStoreLocation();

			useExternalIdentity = authenticationCallback.isExternalIdentityStore();
			
			userSecretKey = authenticationCallback.getSecretKey();
			
			repositoryId = authenticationCallback.getRepositoryId();
		}
		catch(IOException e){
			LoginException le = new LoginException("Failed to get username/password");
			le.initCause(e);
			throw le;
		}
		catch(UnsupportedCallbackException e){
			LoginException le = new LoginException("CallbackHandler does not support: " + e.getCallback());
			le.initCause(e);
			throw le;
		}
		info[0] = username;
		info[1] = password;
		info[2] = userSecretKey;
		info[3] = identityStoreLocation;
		info[4] = repositoryId;
		
		return info;
	}
	
	private IdentityStore getIdentityStore(){
		if (!useExternalIdentity){
			if (StringUtils.isBlank(authenticationTokenForSYSTEMofInternalIdentityStore)){
				throw new CmsException("Internal Identity Store must be used but there is no authentication token for the SYSTEM user");
			}
			
			AstroboaClientContextHolder.activateClientContextForAuthenticationToken(authenticationTokenForSYSTEMofInternalIdentityStore);
		}
		
		return identityStore;
	}
}
