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

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder.Type;
import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.ContentService;

/**
 * Secure Content Service API. 
 * 
 * <p>
 * It contains the same methods provided by 
 * {@link ContentService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in a Astroboa repository and
 * therefore has been granted access to further use Astroboa services
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ContentServiceSecure {

	/**
	 * Same semantics with {@link ContentService#saveAndVersionLockedContentObject(ContentObject, String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String, String)} instead
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveAndVersionLockedContentObject(ContentObject contentObject,String lockToken, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#saveLockedContentObject(ContentObject, boolean, String)}.
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String, String)} instead
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveLockedContentObject(	ContentObject contentObject,
			boolean version,
			String lockToken, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#saveContentObject(ContentObject, boolean)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String, String)} instead
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 *             
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveContentObject(
			ContentObject contentObject,
			boolean version, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#saveContentObject(ContentObject, boolean, boolean)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String, String)} instead
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveContentObject(
			ContentObject contentObject,
			boolean version, boolean updateLastModificationDate, String authenticationToken);


	/**
	 * Same semantics with {@link ContentService#saveAndVersionContentObject(ContentObject)}
	 * augmented with the requirement of providing an authentication token
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObject
	 *             {@link ContentObject} to be saved or updated.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String, String)} instead
	 * 
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 *             
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveAndVersionContentObject(
			ContentObject contentObject, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getContentObject(String, RenderProperties, CacheRegion)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param contentObjectRenderProperties
	 *            A set of instructions on how to render content object.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List, String)}
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObject(
			String contentObjectId,
			RenderProperties contentObjectRenderProperties, 
			CacheRegion cacheRegion,String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getContentObjectById(String, CacheRegion)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List, String)}
	 * 
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObjectById(String contentObjectId, 
			CacheRegion cacheRegion,String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getContentObjectByIdAndLocale(String, String, CacheRegion)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for content 
	 *            {@link CmsRepositoryEntity entities}.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List)}
	 * 
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObjectByIdAndLocale(String contentObjectId, 
			String locale, 
			CacheRegion cacheRegion,String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getBinaryChannelById(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param binaryChannelId
	 *            {@link BinaryChannel#getId() Binary channel id}.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A binary channel for specified id.
	 */
	BinaryChannel getBinaryChannelById(
			String binaryChannelId, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getRootContentObjectFolders(String)}
	 * augmented with the requirement of providing an authentication token.
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
	 *            to retrieve localized label for returned content object folders.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A list of all content object folders, one for each content object
	 *         type.
	 */
	List<ContentObjectFolder> getRootContentObjectFolders(
			String locale, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getContentObjectFolderTree(String, int, boolean, String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectFolderId
	 *            {@link ContentObjectFolder#getId() content object folder id}.
	 * @param depth
	 *            Number of levels of content object folder to return.
	 *            <code>-1</code> denotes that all content object folder
	 *            hierarchy will be returned.
	 * @param renderContentObjectIds
	 *            <code>true</code> to load content object ids, in case a
	 *            content object folder is of type
	 *            {@link Type#DAY} (for 2.x.x versions) or {@link Type#SECOND} (for 3.x.x), <code>false</code>
	 *            otherwise.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned content object folders.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A Content object folder.
	 */
	ContentObjectFolder getContentObjectFolderTree(
			String contentObjectFolderId,
			int depth,
			boolean renderContentObjectIds,
			String locale, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#deleteContentObject(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param objectIdOrSystemName
	 *            Object {@link ContentObject#getId() id} or {@link ContentObject#getSystemName() system name}.
	 *            
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return <code>true</code> if object has been successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteContentObject(
			String objectIdOrSystemName, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#getContentObjectByVersionName(String, String, String, CacheRegion)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param versionName
	 *            Content object version name.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for content 
	 *            {@link CmsRepositoryEntity entities}.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A content object of specific version.
	 */
	ContentObject getContentObjectByVersionName(
			String contentObjectId,
			String versionName,
			String locale,CacheRegion cacheRegion,String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#searchContentObjects(ContentObjectCriteria)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @deprecated
	 * @return A list of content object satisfying criteria.
	 */
	@Deprecated
	CmsOutcome<CmsRankedOutcome<ContentObject>> searchContentObjects(
			ContentObjectCriteria contentObjectCriteria, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#searchContentObjectsAndExportToXml(ContentObjectCriteria)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * Returns all content objects matching specified criteria.
	 * 
	 * 	<p>
	 * Use method {@link ContentObjectCriteria#addPropertyPathWhoseValueWillBePreLoaded(String)} 
	 * if you want specific properties to be returned. Otherwise, all content object 
	 * properties will be rendered. 
	 * </p>
	 * 
	 * 
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @deprecated
	 * @return XML representation of query results following XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd
	 */
	@Deprecated
	String searchContentObjectsAndExportToXml(
			ContentObjectCriteria contentObjectCriteria, String authenticationToken);

	/**
	 * Returns all content objects matching specified criteria in JSON
	 * following Mapped convention.
	 * 
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * <p>
	 * Use method {@link ContentObjectCriteria#addPropertyPathWhoseValueWillBePreLoaded(String)} 
	 * if you want specific properties to be returned. Otherwise, all content object 
	 * properties will be rendered. 
	 * </p>
	 * 
     * The natural JSON notation, leveraging closely-coupled JAXB RI integration.
     * <p>Example JSON expression:<pre>
     * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
     * </pre>
     * </p>
     * 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @deprecated
	 * @return JSON representation of query results according to XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd  following Mapped convention
	 */
	@Deprecated
	String searchContentObjectsAndExportToJson(
			ContentObjectCriteria contentObjectCriteria, String authenticationToken);
	

	/**
	 * Same semantics with {@link ContentService#lockContentObject(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 *  
	 * @return Lock token necessary to unlock content object.
	 */
	String lockContentObject(
			String contentObjectId, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#removeLockFromContentObject(String, String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param lockToken
	 *            Lock token necessary to unlock content object.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 */
	void removeLockFromContentObject(
			String contentObjectId,
			String lockToken, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#isContentObjectLocked(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return <code>true</code> if content object is locked,
	 *         <code>false</code> otherwise.
	 */
	boolean isContentObjectLocked(
			String contentObjectId, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#increaseContentObjectViewCounter(String, long)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param counter Number of views 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 */
	void increaseContentObjectViewCounter(
			String contentObjectId, long counter, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#loadChildCmsProperty(String, String, String, String, RenderProperties)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param childPropertyName The name of the property to load
	 * @param parentComplexCmsPropertyDefinitionFullPath Parent definition full path
	 * @param jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty UUID of Jcr Node which corresponds to parent property
	 * @param jcrNodeUUIDWhichCorrespondsToContentObejct UUID of Jcr Node which corresponds to content object
	 * @param renderProperties Instructions to follow while rendering
	 * 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A list of all loaded child cms properties. A list is used in cases where multiple complex cms properties
	 * are loaded for the specified childPropertyName
	 */
	List<CmsProperty<?,?>> loadChildCmsProperty(String childPropertyName, String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#loadChildCmsProperty(String, String, String, String, RenderProperties)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * Utility method which transforms an aspect to a native property of all content objects of the provided content type.
	 * 
	 *  This is useful in cases where an aspect is needed by all content objects of a specific content type and therefore
	 *  it is more meaningful to be defined as a native property of this content type rather than an aspect.
	 *  
	 *  After this method successfully ends, content type definition must be updated with the definition of the property, 
	 *  so that the property will become available as a normal native property of this content type.
	 *  
	 * @param aspect Aspect name
	 * @param newPropertyName New property name which replace aspect. If null then new property will have the same name with aspect
	 * @param contentType Restrict the change to all content objects of this type only
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 */
	void moveAspectToNativePropertyForAllContentObjectsOFContentType(String aspect, String newPropertyName, String contentType, String authenticationToken);
	
	/**
	 * Same semantics with {@link ContentService#getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *  
	 * @param <T> {@link String}, {@link ContentObject} or {@link CmsOutcome}
	 * @param contentObjectIdOrSystemName {@link ContentObject#getId() contentObject id} or {@link ContentObject#getSystemName() contentObject system name} 
	 * @param output ContentObject representation output, one of XML, JSON or {@link ContentObject}. Default is {@link ResourceRepresentationType#CONTENT_OBJECT_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link ContentObject}'s only properties, its children as well or the whole {@link ContentObject}.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param propertyPathsToInclude A list of property paths to include in export, null for all
	 * @param serializeBinaryContent <code>true</code> to export binary content, <code>false</code> otherwise. In any case 
	 * URL of binary content is provided.  This property is valid for output formats {@link ResourceRepresentationType#XML} or {@link ResourceRepresentationType#JSON} only 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return A contentObject as XML, JSON or {@link ContentObject}, or <code>null</code> of none is found.
	 */
	<T> T getContentObject(String contentObjectIdOrSystemName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, CacheRegion cacheRegion, List<String> propertyPathsToInclude, boolean serializeBinaryContent, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#save(Object, boolean, boolean, String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 *@param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 *             
 	 * @return Newly created or updated {@link ContentObject} instance.
	 */
	ContentObject save(Object contentObject, boolean version, boolean updateLastModificationTime, String lockToken, String authenticationToken);
	
	/**
	 * Same semantics with {@link ContentService#searchContentObjects(ContentObjectCriteria, ResourceRepresentationType)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param <T> {@link String}, {@link ContentObject} or {@link CmsOutcome}
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @param output ContentObject representation output, one of XML, JSON or {@link ContentObject}. 
	 * 	Default is {@link ResourceRepresentationType#CONTENT_OBJECT_LIST}
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 *
	 * @return ContentObjects as XML, JSON or {@link CmsOutcome<ContentObject>}	 
	 */
	<T> T  searchContentObjects(ContentObjectCriteria contentObjectCriteria, ResourceRepresentationType<T> output, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#copyContentObject(String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * @param contetObjectId Identifier of ContentObject to be copied
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * @return Cloned {@link ContentObject} or an exception if no content object is found or user is not authorized to 
	 * perform this operation 
	 */
	ContentObject copyContentObject(String contetObjectId, String authenticationToken);
	/**
	 * Same semantics with {@link ContentService#valueForPropertyExists(String, String)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param propertyPath The name of the property to load
	 * @param jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty UUID of Jcr Node which corresponds to parent property
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.
	 * 
	 * @return <code>true</code> if value exists for the provided property, <code>false</code> otherwise
	 */
	boolean hasValueForProperty(String propertyPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String authenticationToken);

	/**
	 * Same semantics with {@link ContentService#saveContentObjectResourceCollection(Object, boolean, boolean, boolean)}
	 * augmented with the requirement of providing an authentication token.
	 * 
 	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param contentSource Xml or JSON  or {@link ResourceRepresentationType#CONTENT_OBJECT_LIST} representation of a collection of {@link ContentObject}s.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise. Taken into account only if <code>save</code> is <code>true</code>
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise. 
	 * Taken into account only if <code>save</code> is <code>true</code>
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository.

	 * @return Imported {@link ContentObject}
	 */
	List<ContentObject> saveContentObjectResourceCollection(Object contentSource,boolean version, boolean updateLastModificationTime, String lockToken, String authenticationToken );

	/**
	 * Return the content of a binary channel.
	 * 
	 * <p>
 	 * Used mainly for lazy loading the content of a {@link BinaryChannel} when calling method {@link BinaryChannel#getContent()} or
 	 * {@link BinaryChannel#getContentAsStream()} 
	 * 
	 * Users are STRONLGY encouraged to use the aforementioned methods for retrieving the content of a bunary channel.
	 * </p>
	 * 
	 * @param jcrNodeUUIDWhichCorrespondsToTheBinaryChannel UUID of Jcr Node which corresponds to the binary channel
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)}) to a Astroboa repository. 
	 * 
	 * @return <code>true</code> if value exists for the provided property, <code>false</code> otherwise
	 */
	byte[] getBinaryChannelContent(String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel, String authenticationToken);
}