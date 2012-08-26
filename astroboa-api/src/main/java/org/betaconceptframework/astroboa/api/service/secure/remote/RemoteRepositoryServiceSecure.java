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
package org.betaconceptframework.astroboa.api.service.secure.remote;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.RepositoryService;

/**
 * Secure Repository Service API.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RemoteRepositoryServiceSecure   {
	/**
	 * Login to a cms repository with the provided credentials.
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @param credentials Credentials used to authenticate user that wants to connect to repository.
	 * @param permanentKey  representing a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String login(String repositoryId, AstroboaCredentials credentials, String permanentKey);
	
	
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
	 * in repositories-conf.xml
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
	 * in repositories-conf.xml
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
	 * Instead she must provide a secret key which is predefined in repositories-conf.xml.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @param username Name of user who wants to login. 
	 * 
	 * @param key A predefined value located in repositories-conf.xml
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String login(String repositoryId, String username, String key);

	/**
	 * Login method which allows a user to login without providing its password. 
	 * 
	 * <p>
	 * Instead she must provide a secret key which is predefined in repositories-conf.xml.
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @param username Name of user who wants to login. 
	 * 
	 * @param key A predefined value located in repositories-conf.xml
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
	 * User must provide a key which is predefined in repositories-conf.xml.
	 * Administrator username is automatically obtained from repositories-conf.xml
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @param key A predefined value located in repositories-conf.xml
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAdministrator(String repositoryId, String key);
	
	/**
	 * Login method which allows a user to login as an administrator. 
	 * 
	 * <p>
	 * User must provide a key which is predefined in repositories-conf.xml.
	 * Administrator username is automatically obtained from repositories-conf.xml
	 * </p>
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @param key A predefined value located in repositories-conf.xml
	 * 
	 * @param permanentKey  Represents a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created upon successful login to Astroboa repository
	 */
	String loginAsAdministrator(String repositoryId, String key, String permanentKey);
	
	/**
	 * Same semantics with {@link RepositoryService#getCmsRepository(String)}
	 * 
	 * @param repositoryId Repository Id
	 * 
	 * @return Information about a {@link CmsRepository}
	 */
	CmsRepository getCmsRepository(String repositoryId);

	/**
	 * Same semantics with {@link RepositoryService#isRepositoryAvailable(String)}
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in repositories-conf.xml
	 * 
	 * @return <code>true</code> if cms repository is available , <code>false</code> otherwise.
	 */
	boolean isRepositoryAvailable(String repositoryId);

	/**
	 * Same semantics with {@link RepositoryService#getAvailableCmsRepositories()}
	 * 
	 * @return A list of all available repositories
	 */
	List<CmsRepository> getAvailableCmsRepositories();

	/**
	 * Retrieve current connected repository, null if none is connected
	 * 
	 * @return Connected {@link CmsRepository repository}.
	 */
	CmsRepository getCurrentConnectedRepository(String authenticationToken);
	
	/**
	 * Reset authentication timeout for provided authentication token
	 * @param authenticationToken Authentication Token created upon successful login to Astroboa repository
	 */
	void resetAuthenticationTokenTimeout(String authenticationToken);
	
	/**
	 * Check whether token has expired.
	 * 
	 * @param authenticationToken Authentication Token created upon successful login to Astroboa repository
	 * @return <code>true</code> if token has expired, <code>false</code> otherwise
	 */
	boolean tokenHasExpired(String authenticationToken);
}
