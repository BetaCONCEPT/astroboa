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

package org.betaconceptframework.astroboa.api.service.secure;


import java.util.List;

import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.SpaceService;

/**
 * Secure Space Service API. 
 * 
 * <p>
 * It contains methods provided by 
 * {@link SpaceService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in an Astroboa repository and
 * therefore has been granted access to further use Astroboa services.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SpaceServiceSecure  {
	/**
	 * Same semantics with {@link SpaceService#saveSpace(Space)}
	 * augmented with the requirement of providing an authentication token.
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param space Space to be saved.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * 	@return Newly created or updated Space.
	 */
	Space saveSpace(Space space, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#deleteSpace(String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId Space's id to be deleted.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * @return <code>true</code> if space has been successfully deleted, <code>false</code> otherwise
	 *  
	 */
	boolean deleteSpace(String spaceId, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getSpace(String, String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId
	 *            {@link Space#getId() Space's id}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned space.
	 *            
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * @deprecated Use {@link #getSpace(String, ResourceRepresentationType, FetchLevel)} instead
	 * 
	 * @return A space corresponding to <code>spaceId</code> 
	 */
	@Deprecated
	Space getSpace(String spaceId, String locale, String authenticationToken);


	/**
	 * Same semantics with {@link SpaceService#getOrganizationSpace()}
	 * augmented with the requirement of providing an authentication token.
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Α {@link Space} instance representing organization's space.
	 */
	Space getOrganizationSpace(String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#searchSpaces(SpaceCriteria)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceCriteria
	 *            Space search criteria.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @deprecated Use {@link #searchSpaces(SpaceCriteria, ResourceRepresentationType, String)}
	 * @return Spaces satisfying specified criteria.
	 */
	@Deprecated
	CmsOutcome<Space> searchSpaces(SpaceCriteria spaceCriteria, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getContentObjectIdsWhichReferToSpace(String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId Space identifier
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of content object identifiers
	 */
	List<String> getContentObjectIdsWhichReferToSpace(String spaceId, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getContentObjectIdsWhichReferToSpace(String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId Space identifier
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Number of content objects referring to that space
	 */
	int getCountOfContentObjectIdsWhichReferToSpace(String spaceId, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getContentObjectIdsWhichResideInSpace(String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId Space identifier
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of content object identifiers
	 */
	List<String> getContentObjectIdsWhichResideInSpace(String spaceId, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getContentObjectIdsWhichResideInSpace(String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param spaceId Space identifier
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Number of content objects residing inside space.
	 */
	int getCountOfContentObjectIdsWhichResideInSpace(String spaceId, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#getSpace(String, ResourceRepresentationType, FetchLevel)}
	 * augmented with the requirement of providing an authentication token.
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param <T> {@link String}, {@link Space} or {@link CmsOutcome}
	 * @param spaceIdOrName {@link Space#getId() space id} or {@link Space#getName() space name}
	 * @param output Space representation output, one of XML, JSON or {@link Space}. Default is {@link ResourceRepresentationType#SPACE_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Space}'s only properties, its children as well or the whole {@link Space} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) 
	 *  to an Astroboa repository.
	 * 
	 * @return A space as XML, JSON or {@link Space}, or <code>null</code> of none is found.
	 */
	<T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#save(Object)}
	 * augmented with the requirement of providing an authentication token.
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param space Space to be saved.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * 	@return Newly created or updated Space.
	 */
	Space save(Object space, String authenticationToken);

	/**
	 * Same semantics with {@link SpaceService#searchSpaces(SpaceCriteria, ResourceRepresentationType)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param <T> {@link String}, {@link Space} or {@link CmsOutcome}
	 * @param spaceCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @param output Space representation output, one of XML, JSON or {@link Space}. 
	 * 	Default is {@link ResourceRepresentationType#SPACE_LIST}
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to an Astroboa repository.
	 *
	 * @return Spaces as XML, JSON or {@link CmsOutcome<Space>}	 
	 */
	<T> T  searchSpaces(SpaceCriteria spaceCriteria, ResourceRepresentationType<T> output, String authenticationToken);

}
