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
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.DefinitionService;

/**
 * Secure Definition Service API.
 * 
 * <p>
 * It contains the same methods provided by 
 * {@link DefinitionService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in a Astroboa repository and
 * therefore has been granted access to further use Astroboa services.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface DefinitionServiceSecure {

	/**
	 * Same semantics with {@link DefinitionService#hasContentObjectTypeDefinition(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectTypeDefinitionName
	 *            Content object type definition name.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to an Astroboa repository.
	 * 
	 * @return <code>true</code> if content object type definition exists,
	 *         <code>false</code> otherwise.
	 */
	boolean hasContentObjectTypeDefinition(String contentObjectTypeDefinitionName, String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getContentObjectTypes()}
	 * augmented with the requirement of providing an authentication token
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
	 * @return A list of content object type definition names.
	 */
	List<String> getContentObjectTypes(String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getAspectDefinitionsSortedByLocale(List, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param complexCmsPropertyNames
	 *            List of complex property definition names.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned definitions.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of complex content object property definitions.
	 */
	List<ComplexCmsPropertyDefinition> getAspectDefinitionsSortedByLocale(List<String> complexCmsPropertyNames,	String locale, String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getAvailableAspectDefinitionsSortedByLocale(String)}
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
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned definitions.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of complex content object property definitions.
	 */
	List<ComplexCmsPropertyDefinition> getAvailableAspectDefinitionsSortedByLocale(String locale, String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getTopicPropertyPathsPerTaxonomies()}
	 * augmented with the requirement of providing an authentication token
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
	 * @return All topic property paths per taxonomy, for all taxonomy specified in
	 * the connected respository.
	 */
	Map<String, List<String>> getTopicPropertyPathsPerTaxonomies(String authenticationToken);

	/**
	 * Retrieve all properties (just their names) which are multivalued.
	 *	<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *	 * Used mainly internally when exporting to JSON
	 * 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of all multi valued properties for active repository
	 */
	List<String> getMultivalueProperties(String authenticationToken);
	
	/**
	 * Retrieve content type hierarchy. If no inheritance is found an empty map will be returned
	 * 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A map whose key is base content type and value all sub content types for base content type
	 */
	Map<String, List<String>> getContentTypeHierarchy(String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getCmsDefinition(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param <T> {@link String}, {@link CmsDefinition}
	 * 
	 * @param fullPropertyDefinitionPath
	 * 			A period-delimited string defined in 
	 *            ({@link CmsPropertyDefinition#getPath()}).
	 * @param output {@link CmsDefinition} representation output, one of XML, JSON, XSD or {@link CmsDefinition}.
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if 
	 * <code>output</code> is either {@link ResourceRepresentationType#XML XML} or 
	 * {@link ResourceRepresentationType#JSON JSON}
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
  	 * @return CmsDefinition as XML, JSON, XSD or {@link CmsDefinition}	 
  	 */
	<T> T getCmsDefinition(String fullPropertyDefinitionPath, ResourceRepresentationType<T> output, boolean prettyPrint, String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#validateDefinintion(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_ADMIN} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param definition Definition to be validated
	 * @param definitionFileName Definition file name in case this is an updated definition
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if definition file is valid, <code>false</code> otherwise
	 */
	boolean validateDefinintion(String definition, String definitionFileName, String authenticationToken);
	
	/**
	 * Same semantics with {@link DefinitionService#getDefinitionHierarchyDepthPerContentType()}
	 * augmented with the requirement of providing an authentication token
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
	 * @return A map of the depth per content type. There is also one more entry whose key is the '*' char which represents the max depth.
	 */
	Map<String, Integer> getDefinitionHierarchyDepthPerContentType(String authenticationToken);
	
	/**
	 * Same semantics with {@link DefinitionService#getDefinitionHierarchyDepthForContentType(String)()}
	 * augmented with the requirement of providing an authentication token
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
	 * @param contentType The name of the content type whose definition depth is requested. 
	 * 
	 * @return Content Type's definition hierarchy depth or 0 if not found. If content type is the '*' char, it returns the maximum definition hierarchy depth.
	 */

	Integer getDefinitionHierarchyDepthForContentType(String contentType, String authenticationToken);

	/**
	 * Same semantics with {@link DefinitionService#getTypeForProperty(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param contentType The name of the content type whose definition depth is requested. If a blank (empty or null) string
	 * is provided then the provided property path will be searched in all content types
	 * 
	 * @param propertyPath A period-delimited string defined in 
	 *            ({@link CmsPropertyDefinition#getPath()}).
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * @return One of {@link ValueType} or null if the property is not found 
	 */
	ValueType getTypeForProperty(String contentType, String propertyPath, String authenticationToken);

}