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
package org.betaconceptframework.astroboa.api.service;

import java.util.List;

import javax.security.auth.Subject;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;

/**
 * This is the entry point for using all other Astroboa services.
 * 
 * Provides methods for retrieving information about available repositories
 * and for connecting  to a specific repository.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryService {

	/**
	 * Connect to a cms repository with the provided credentials.
	 * 
	 * <p>
	 * If connection and authorization is successful, all necessary repository and security information 
	 * are stored to Thread initiated this connection, in order to use other Astroboa services
	 * without the need to authenticate on every Astroboa service method call.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param credentials Credentials used to authenticate user that wants to connect to repository.
	 * 
	 * @throws {@link CmsLoginAccountExpiredException} if user account is expired
	 * @throws {@link CmsLoginAccountLockedException} if user account is locked
	 * @throws {@link CmsInvalidPasswordException} if invalid password is provided
	 * @throws {@link CmsLoginInvalidUsernameException} if invalid username is provided
	 * @throws {@link CmsLoginPasswordExpiredException} if user password is expired
	 * @throws {@link CmsUnauthorizedRepositoryUseException} if user is not authorized to login to the specified repository
	 * 
	 * @return Authentication Token representing successful access to <code>repositoryId</code>
	 */
	String login(String repositoryId, AstroboaCredentials credentials);
	
	/**
	 * Connect to a cms repository with the provided credentials.
	 * 
	 * <p>
	 * If connection and authorization is successful, all necessary repository and security information 
	 * are stored to Thread initiated this connection, in order to use other Astroboa services
	 * without the need to authenticate on every Astroboa service method call.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param credentials Credentials used to authenticate user that wants to connect to repository.
     *
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @throws {@link CmsLoginAccountExpiredException} if user account is expired
	 * @throws {@link CmsLoginAccountLockedException} if user account is locked
	 * @throws {@link CmsInvalidPasswordException} if invalid password is provided
	 * @throws {@link CmsLoginInvalidPermanentKeyException} if invalid permanent key is provided
	 * @throws {@link CmsLoginInvalidUsernameException} if invalid username is provided
	 * @throws {@link CmsLoginPasswordExpiredException} if user password is expired
	 * @throws {@link CmsUnauthorizedRepositoryUseException} if user is not authorized to login to the specified repository
	 * 
	 * @return Authentication Token created by successful connection to Astroboa repository
	 */
	String login(String repositoryId, AstroboaCredentials credentials, String permanentKey);

	/**
	 * Connect to a cms repository using the provided Subject.
	 * 
	 * <p>
	 * This method should be used in cases where a set of specific actions must take place
	 * and user authentication is impossible. One example is Astroboa JAAS where there is a
	 * need to login to the IdentityStore provided by Astroboa but admin user credentials
	 * are not available. 
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param subject Subject created during authentication by another system.
	 * 
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @throws {@link CmsLoginInvalidPermanentKeyException} if invalid permanent key is provided
	 * @throws {@link CmsUnauthorizedRepositoryUseException} if user is not authorized to login to the specified repository
	 * 
	 * @return Authentication Token created by successful registration of provided subject to Astroboa
	 */
	String login(String repositoryId, Subject subject, String permanentKey);

	/**
	 * Login as ANONYMOUS user to the specified Astroboa repository.
	 * 
	 * <p>
	 * This method logins a user as ANONYMOUS with the right to read/view
	 * only published content. That is this user is assigned role 
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER}. 
	 * </p>
	 * 
	 * <p>
	 * Note that no {@link RepositoryUser} with external id ANONYMOUS
	 * need to exist nor a content object of type <code>personObject</code>
	 * whose username must be ANONYMOUS.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAnonymous(String repositoryId);
	
	/**
	 * Login as ANONYMOUS user to the specified Astroboa repository.
	 * 
	 * <p>
	 * This method logins a user as ANONYMOUS with the right to read/view
	 * only published content. That is this user is assigned role 
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER}. 
	 * </p>
	 * 
	 * <p>
	 * Note that no {@link RepositoryUser} with external id ANONYMOUS
	 * need to exist nor a content object of type <code>personObject</code>
	 * whose username must be ANONYMOUS.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAnonymous(String repositoryId, String permanentKey);
	
	/**
	 * Login method which allows a user to login without providing its password. 
	 * 
	 * <p>
	 * Instead she must provide a key which is predefined in astroboa-conf.xml.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param username Name of user who wants to login. 
	 * 
	 * @param key A predefined value located in astroboa-conf.xml
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String login(String repositoryId, String username, String key);

	/**
	 * Login method which allows a user to login without providing its password. 
	 * 
	 * <p>
	 * Instead she must provide a key which is predefined in astroboa-conf.xml.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param username Name of user who wants to login. 
	 * 
	 * @param key A predefined value located in astroboa-conf.xml
	 * 
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String login(String repositoryId, String username, String key, String permanentKey);
	
	/**
	 * Login method which allows a user to login as an administrator. 
	 * 
	 * <p>
	 * User must provide a key which is predefined in astroboa-conf.xml.
	 * Administrator username is automatically obtained from astroboa-conf.xml
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param key A predefined value located in astroboa-conf.xml
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAdministrator(String repositoryId, String key);
	
	/**
	 * Login method which allows a user to login as an administrator. 
	 * 
	 * <p>
	 * User must provide a key which is predefined in astroboa-conf.xml.
	 * Administrator username is automatically obtained from astroboa-conf.xml
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param key A predefined value located in astroboa-conf.xml
	 * 
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAdministrator(String repositoryId, String key, String permanentKey);

	/**
	 * Check if cms repository is available.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @return <code>true</code> if cms repository is available , <code>false</code> otherwise.
	 */
	boolean isRepositoryAvailable(String repositoryId);
	
	/**
	 * Get all available repositories where a client
	 * can connect to.
	 * 
	 * @return A list of all available repositories
	 */
	List<CmsRepository> getAvailableCmsRepositories();

	/**
	 * Retrieve current connected repository, null if none is connected
	 * 
	 * @return Connected {@link CmsRepository repository}.
	 */
	CmsRepository getCurrentConnectedRepository();
	
	/**
	 * Retrieve information about a repository.
	 * 
	 * 
	 * @param repositoryId Repository Id
	 * 
	 * @return Information about a {@link CmsRepository}, or null if none is found
	 */
	CmsRepository getCmsRepository(String repositoryId);

}
