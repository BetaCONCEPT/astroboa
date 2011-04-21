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

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;

/**
 * Secure Taxonomy Service API.
 * 
 * <p>
 * It contains methods provided by 
 * {@link TaxonomyService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in an Astroboa repository and
 * therefore has been granted access to further use Astroboa services
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface TaxonomyServiceSecure {

	/**
	 * Same semantics with {@link TaxonomyService#saveTaxonomy(Taxonomy)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_TAXONOMY_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param taxonomy
	 *            {@link Taxonomy Taxonomy} to be saved.  
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * @deprecated Use method {@link #save(Object)} instead
	 * 
	 * @return Newly created or updated Taxonomy
	 */
	@Deprecated
	Taxonomy saveTaxonomy(Taxonomy taxonomy, String authenticationToken);

	/**
	 * Same semantics with {@link TaxonomyService#deleteTaxonomyTree(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_TAXONOMY_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param taxonomyId Taxonomy identifier
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 */
	void deleteTaxonomyTree(String taxonomyId, String authenticationToken);

	/**
	 * Same semantics with {@link TaxonomyService#getTaxonomy(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param taxonomyName
	 *            {@link Taxonomy#getName() Taxonomy name}.
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @deprecated User {@link #getTaxonomy(String, ResourceRepresentationType, FetchLevel)}
	 * @return {@link Taxonomy} with the specified name or null if not found.
	 */
	@Deprecated
	Taxonomy getTaxonomy(String taxonomyName, String locale, String authenticationToken);

	/**
	 * Same semantics with {@link TaxonomyService#getTaxonomies(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 *  
	 * @deprecated Use {@link #getAllTaxonomies(ResourceRepresentationType, FetchLevel, boolean)} instead
	 * 
	 * @return A list of all taxonomies in content repository model.
	 */
	@Deprecated
	List<Taxonomy> getTaxonomies(String locale, String authenticationToken);

	/**
	 * Same semantics with {@link TaxonomyService#getBuiltInSubjectTaxonomy(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Built in {@link Taxonomy} with name {@link Taxonomy#SUBJECT_TAXONOMY_NAME}. Always not null.
	 */
	Taxonomy getBuiltInSubjectTaxonomy(String locale, String authenticationToken);

	/**
	 * Same semantics with {@link TaxonomyService#getTaxonomy(String, ResourceRepresentationType, FetchLevel)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param <T> {@link String}, {@link Taxonomy} or {@link CmsOutcome}
	 * @param taxonomyIdOrName {@link Taxonomy#getName() taxonomy name} or {@link Taxonomy#getId() taxonomy id }
	 * @param output Taxonomy representation output, one of XML, JSON or {@link Taxonomy}. Default is {@link ResourceRepresentationType#TAXONOMY_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Taxonomy}'s only properties, its children as well or the whole {@link Taxonomy} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * 
	 * @return A taxonomy as XML, JSON or {@link Taxonomy}, or <code>null</code> of none is found.
	 */
	<T> T getTaxonomy(String taxonomyIdOrName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint, String authenticationToken);
	
	/**
	 * Same semantics with {@link TaxonomyService#getAllTaxonomiesAsXML()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param <T> {@link String}, or {@link CmsOutcome}
	 * @param output Taxonomy representation output, one of XML, JSON or {@link Taxonomy}. Default is {@link ResourceRepresentationType#TAXONOMY_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Taxonomy}'s only properties, its children as well or the whole {@link Taxonomy} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return All taxonomies as XML, JSON or {@link CmsOutcome}
	 */
	<T> T getAllTaxonomies(ResourceRepresentationType<T> output,  FetchLevel fetchLevel, boolean prettyPrint, String authenticationToken);
	
	/**
	 * Same semantics with {@link TaxonomyService#save(Object)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_TAXONOMY_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param taxonomySource
	 *            {@link Taxonomy Taxonomy} to be saved.  
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Newly created or updated Taxonomy
	 */
	Taxonomy save(Object taxonomySource, String authenticationToken);
	/**
	 * Same semantics with {@link TaxonomyService#getTaxonomyById(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param taxonomyId
	 *            {@link Taxonomy#getId() Taxonomy id}.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return {@link Taxonomy} with the specified identifier or null if not found.
	 */
	Taxonomy getTaxonomyById(String taxonomyId, String authenticationToken);

}
