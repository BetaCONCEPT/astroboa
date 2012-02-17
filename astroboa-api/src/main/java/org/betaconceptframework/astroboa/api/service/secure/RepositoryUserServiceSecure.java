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

package org.betaconceptframework.astroboa.api.service.secure;



import java.util.List;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;

/**
 * Secure RepositoryUser Service API. 
 * 
 * <p>
 * It contains methods provided by 
 * {@link RepositoryUserService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in an Astroboa repository and
 * therefore has been granted access to further use Astroboa services
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryUserServiceSecure {
	/**
	 * Same semantics with {@link RepositoryUserService#searchRepositoryUsers(RepositoryUserCriteria)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param repositoryUserCriteria
	 *            Repository user criteria.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of repository users.
	 */
	List<RepositoryUser> searchRepositoryUsers(RepositoryUserCriteria repositoryUserCriteria, String authenticationToken);

	/**
	 * Same semantics with {@link RepositoryUserService#saveRepositoryUser(RepositoryUser)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param repositoryUser
	 *            Repository user to save or update.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * @deprecated Use method {@link #save(Object, String)}
	 * @return Newly created or updated RepositoryUser
	 */
	@Deprecated
	RepositoryUser saveRepositoryUser(RepositoryUser repositoryUser, String authenticationToken);

	/**
	 * Same semantics with {@link RepositoryUserService#deleteRepositoryUserAndOwnedObjects(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param repositoryUserId
	 *            {@link RepositoryUser#getId() Repository user id}.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 */
	void deleteRepositoryUserAndOwnedObjects(String repositoryUserId, String authenticationToken);

	/**
	 * Same semantics with {@link RepositoryUserService#deleteRepositoryUser(String, RepositoryUser)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param repositoryUserId
	 *            {@link RepositoryUser#getId() Repository user id}.
	 * @param alternativeUser Repository user substitute.
	 *	@param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 */
	void deleteRepositoryUser(String repositoryUserId,	RepositoryUser alternativeUser, String authenticationToken);

	/**
	 * Same semantics with {@link RepositoryUserService#getRepositoryUser(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param externalId RepositoryUser externalId
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return RepositoryUser for <code>externalId</code>, null if none exists 
	 */
	RepositoryUser getRepositoryUser(String externalId, String authenticationToken);

	/**
	 * Same semantics with {@link RepositoryUserService#getSystemRepositoryUser()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Repository SYSTEM user
	 */
	RepositoryUser getSystemRepositoryUser(String authenticationToken) ;

	/**
	 * Same semantics with {@link RepositoryUserService#save(Object)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param repositoryUser
	 *            Repository user to save or update.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 *  
	 * @return Newly created or updated RepositoryUser
	 */
	RepositoryUser save(Object repositoryUser, String authenticationToken);
}
