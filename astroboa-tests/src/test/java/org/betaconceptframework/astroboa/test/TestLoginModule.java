/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.test;

import java.io.IOException;
import java.security.acl.Group;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import javax.security.auth.spi.LoginModule;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.DisplayNamePrincipal;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.PersonUserIdPrincipal;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.security.AstroboaAuthenticationCallback;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.security.management.CmsPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for AstroboaLoginModule located in JAAS.
 * 
 * This class contains the same code with the Login Module provided 
 * in astroboa-jaas with the difference that it uses an IdentityStoreTest 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TestLoginModule implements LoginModule{

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	private IdentityStore identityStore;
	
	private boolean useExternalIdentity;

	private Person loggedInPerson = null;

	private Subject subject;

	private CallbackHandler callbackHandler;

	//private Map sharedState;

	//private Map options;

	private IdentityPrincipal identity;

	
	/**
	 * Override login to provide extra checks in case user credentials are
	 * correct
	 */
	public boolean login() throws LoginException {

		boolean loginIsSuccessful =  internalLogin();

		if ( loginIsSuccessful == true ){

			//Load Person and execute some extra checks
			//Normally loggedInPerson must have already been initialized

			/*if (BooleanUtils.isFalse(loggedInPerson.getUserData().getAccountNonLocked())) {
				throw new AccountLockedException(getUsername());
			}*/

			if (!loggedInPerson.isEnabled()) {
				throw new AccountNotFoundException(getUsername());
			}

			/*if (BooleanUtils.isFalse(loggedInPerson.getUserData().getAccountNonExpired())) {
				throw new AccountExpiredException(getUsername());
			}

			if (BooleanUtils.isFalse(loggedInPerson.getUserData().getCredentialsNonExpired())) {
				throw new CredentialExpiredException(getUsername());
			}*/

			return true;
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
		String repositoryId = credentialsInfo[4];


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
		
		if (StringUtils.isBlank(identityStoreLocation)){
			throw new FailedLoginException("No identity store location is provided");
		}

		initializeIdentityStore(identityStoreLocation);

		//All went well
		//Authenticate using IdentityStore
		//If no password is provided then check only if user exists 
		//This must be reviewed as we allow anyone to obtain a user's roles
		if (password == null)
		{
			//User must exist
			if (! identityStore.userExists(username))
			{
				throw new FailedLoginException(username);
			}
			
			//Also to allow login a secret key must be provided and it must match for this user
			if (! RepositoryRegistry.INSTANCE.isSecretKeyValidForUser(repositoryId, username, secretKey))
			{
				throw new FailedLoginException(username);
			}
			
		}
		else if (!identityStore.authenticate(username, password))
		{
			throw new FailedLoginException(username);	
		}

		identity = new IdentityPrincipal(username);

		loadPersonByUserName(username);

		return true;
	}

	private void initializeIdentityStore(String identityStoreLocation)
			throws FailedLoginException {
		
		if (useExternalIdentity)
		{
			initializeExternalIdentityStore(identityStoreLocation);
		}
		else
		{
			initializeAstroboaClientForIdentityStore(identityStoreLocation);
		}
		
		if (identityStore == null)
		{
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

						identityStore = (IdentityStore) context.lookup(identityStoreLocation);

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

	private void initializeAstroboaClientForIdentityStore(
			String identityStoreRepositoryId) {

		Subject subject = new Subject();

		//System identity
		subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));

		Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());

		for (CmsRole cmsRole : CmsRole.values()){
			rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, identityStoreRepositoryId)));
		}
		
		subject.getPrincipals().add(rolesPrincipal);

		AstroboaTestContext.INSTANCE.getRepositoryService().login(identityStoreRepositoryId, subject, null); 
				
		identityStore = AstroboaTestContext.INSTANCE.getIdentityStore();
	}

	private void loadPersonByUserName(String username) throws LoginException {

		if (loggedInPerson == null){
			try{

				loggedInPerson = identityStore.retrieveUser(username); 

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



	/* (non-Javadoc)
	 * @see org.jboss.security.auth.spi.AbstractServerLoginModule#commit()
	 */
	@Override
	public boolean commit() throws LoginException {

		//Add identity 
		addIdentityPrincipalToSubject();

		//Add PersonUserIdPrincipal to subject
		addPersonUserIdPrincipalToSubject();

		//Add display name principal
		addDisplayNamePrincipalToSubject();

		//Add roles to subject
		addRolesToSubject();
		
		return true;
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

		if (userIsAnonymous(identity.getName()))
		{
			groupContainingAllRoles.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER)));
		}
		else
		{
			List<String> impliedRoles = identityStore.getImpliedRoles(getUsername());

			//Load all roles in a tree
			if (impliedRoles != null){

				for (String impliedRole : impliedRoles){
					groupContainingAllRoles.addMember(new CmsPrincipal(impliedRole));
				}		
			}
		}

		subject.getPrincipals().add(groupContainingAllRoles);
	}


	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {

		this.subject = subject;
		this.callbackHandler = callbackHandler;
		
		// this.sharedState = sharedState;
		//	this.options = options;

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

		try
		{
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
		catch(IOException e)
		{
			LoginException le = new LoginException("Failed to get username/password");
			le.initCause(e);
			throw le;
		}
		catch(UnsupportedCallbackException e)
		{
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




	@Override
	public boolean abort() throws LoginException {
		return true;
	}
	@Override
	public boolean logout() throws LoginException {
		return true;
	}

}
