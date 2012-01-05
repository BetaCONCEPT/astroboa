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
import java.util.Locale;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder.Type;
import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;

/**
 * Service providing methods for managing {@link ContentObject content objects}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ContentService {

	/**
	 * Same semantics with {@link #saveContentObject(ContentObject, boolean)}.
	 * 
	 * In addition, in case content object is locked, use <code>lockToken</code> to unlock
	 * it.
	 * 
	 * @param contentObject
	 *           {@link ContentObject} to be saved or updated.
	 * @param lockToken
	 *            Token created when content object was locked.
	 *            
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String)} instead
	 * 
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveAndVersionLockedContentObject(ContentObject contentObject,String lockToken);

	/**
	 * Same semantics with {@link #saveContentObject(ContentObject, boolean)}.
	 * 
	 * In case content object is locked, use <code>lockToken</code> to unlock
	 * it
	 * 
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String)} instead
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveLockedContentObject(	ContentObject contentObject,
			boolean version,
			String lockToken);

	/**
	 * Saves or updates a content object.
	 * 
	 * In case content object is locked an exception is thrown.
	 * 
	 * <p>
	 * If no {@link ContentObject#setContentObjectType(String) content object type} is provided or type 
	 * provided does not correspond to a {@link ContentObjectTypeDefinition definition}
	 * an exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * In case of an update, if {@link ContentObject#setContentObjectType(String) content object type}
	 * is different than the one existed, an exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * If {@link ContentObject#getOwner() owner} does not have an id or it does not exist, 
	 * an exception is thrown. 
	 * </p>
	 * <p>
	 * Whether save or update process is followed depends on whether <code>contentObject</code>
	 *  is a new contentObject or not. <code>contentObject</code> is considered new if there is no 
	 * {@link ContentObject#getId() id} or <code>id</code> is provided but there is
	 * no {@link ContentObject contentObject} in repository for the provided <code>id</code>. In this case
	 * <code>contentObject</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>contentObject</code> is considered new)
	 * <ul>
	 * <li> Create a new {@link ContentObject#getId() id} or use the provided <code>id</code>.
	 * <li> Create value for property <code>profile.created</code> to denote content object's creation date.
	 * If property <code>profile.created</code> is defined and a value for that property is provided, 
	 * then that value is used. Otherwise (if no value exists or that property is not defined) 
	 * current date time will be used to denote content object's creation date. 
	 * <li> Relate <code>contentObject</code> with the provided {@link ContentObject#getOwner() owner}.
	 * 		ContentObject's owner MUST already exist. If not, an exception is thrown.
	 * <li> Inserts all provided {@link CmsProperty properties} found under {@link ComplexCmsRootProperty root property}
	 * into repository. 
	 * <li> A {@link SimpleCmsProperty simple property} with no {@link SimpleCmsProperty#getSimpleTypeValues() values}
	 * at all, is not saved. If this property is mandatory an exception is thrown.
	 * <li> A {@link ComplexCmsProperty complex property} with no {@link ComplexCmsProperty#getChildProperties() child properties}
	 * at all, is not saved. If this property is mandatory an exception is thrown. The same occurs if this property contains
	 * child properties but ALL of them on their part are considered empty, that is have no values.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>topic</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>contentObject</code> with the provided {@link ContentObject#getOwner() owner}.
	 * 		ContentObject's owner MUST already exist. If not, an exception is thrown.
	 * 	<li> Updates all provided {@link CmsProperty properties} found under {@link ComplexCmsRootProperty root property}
	 * 		into repository.
	 *  <li> A {@link SimpleCmsProperty simple property} with no {@link SimpleCmsProperty#getSimpleTypeValues() values}
	 * at all, is not saved. If this property is mandatory an exception is thrown.
	 * <li> A {@link ComplexCmsProperty complex property} with no {@link ComplexCmsProperty#getChildProperties() child properties}
	 * at all, is not saved. If this property is mandatory an exception is thrown. The same occurs if this property contains
	 * child properties but ALL of them on their part are considered empty, that is have no values.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Finally, in order to remove a {@link CmsProperty property}, one must call
	 * {@link ContentObject#removeCmsProperty(String)} or {@link ComplexCmsProperty#removeChildProperty(String)}
	 * and then save content object. Again if this property is mandatory an exception is thrown. 
	 * </p>
	 * 
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String)} instead
 	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveContentObject(
			ContentObject contentObject,
			boolean version);

	/**
	 * Same semantics with method {@link #saveContentObject(ContentObject, boolean)}
	 * with the addition of controlling whether last modification date will be changed 
	 * or not.
	 * 
	 * <p>
	 * Every time a content object is saved, unless specified otherwise,
	 * Astroboa updates value of built in property <code>profile.modified</code>
	 * to current datetime. 
	 * </p>
	 * 
	 * This method provides user control over this behavior.
	 * 
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise
	 * 
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String)} instead
	 * 
 	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveContentObject(
			ContentObject contentObject,
			boolean version, boolean updateLastModificationDate);
	/**
	 * Same semantics with {@link #saveContentObject(ContentObject, boolean)}.
	 * 
	 * In case content object is locked an exception is thrown.
	 * 
	 * @param contentObject
	 *             {@link ContentObject} to be saved or updated.
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 * @deprecated Use method {@link #save(Object, boolean, boolean, String)} instead
	 * @return Newly created or updated {@link ContentObject}
	 */
	@Deprecated
	ContentObject saveAndVersionContentObject(
			ContentObject contentObject);

	/**
	 * Returns content object for specified content object id.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param contentObjectRenderProperties
	 *            A set of instructions on how to render content object.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * 
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List)}
	 * 
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObject(
			String contentObjectId,
			RenderProperties contentObjectRenderProperties, 
			CacheRegion cacheRegion);

	/**
	 * Returns content object for specified content object id.
	 * {@link Locale#ENGLISH English}
	 * locale is used for rendering. 
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param cacheRegion Specify how much time object will remain in cache
	 * 
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List)}
	 * 
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObjectById(String contentObjectId, 
			CacheRegion cacheRegion);
	
	/**
	 * Returns content object for specified content object id.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for content 
	 *            {@link CmsRepositoryEntity entities}.
	 * @param cacheRegion Specify how much time object will remain in cache
     *
	 * @deprecated use {@link #getContentObject(String, ResourceRepresentationType, FetchLevel, CacheRegion, List)}
	 * 
	 * @return A {@link ContentObject}.
	 */
	@Deprecated
	ContentObject getContentObjectByIdAndLocale(String contentObjectId, 
			String locale,CacheRegion cacheRegion);

	/**
	 * Returns a binary channel using specified id.
	 * 
	 * Useful method to retrieve binary data of a binary property without having
	 * to load content object which contains binary property.
	 * 
	 * @param binaryChannelId
	 *            {@link BinaryChannel#getId() Binary channel id}.
	 * @return A binary channel for specified id.
	 */
	BinaryChannel getBinaryChannelById(
			String binaryChannelId);

	/**
	 * Returns root content object folders.
	 * 
	 * <p>
	 * All {@link ContentObject content objects} are stored in content
	 * repository in {@link ContentObjectFolder folders} according to their
	 * {@link ContentObject#getContentObjectType()} type and their creation
	 * date.
	 * </p>
	 * 
	 * <p>
	 * Initially a folder for content object type is created (if not already
	 * there). This folder is considered a root content object folder. Then
	 * another folder is created representing the year of creation of content
	 * object which by its turn contains a folder representing the month of
	 * creation of content object which finally contains a folder representing
	 * the day of creation of content object.In the last folder, newly created
	 * content object is stored. For example if a content object of type
	 * <code>article</code> was created at 30/12/2007 the following folder
	 * hierarchy will be created
	 * 
	 * <pre>
	 * articleTypeFolder
	 *   |
	 *   + 2007
	 *      |
	 *      + 12
	 *        |
	 *        + 30
	 *           - Content object
	 * </pre>
	 * 
	 * </p> *
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned content object folders.
	 *            
	 * @return A list of all content object folders, one for each content object
	 *         type.
	 * 
	 */
	List<ContentObjectFolder> getRootContentObjectFolders(
			String locale);

	/**
	 * Returns a content object folder.
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
	 *            
	 * @return A Content object folder.
	 */
	ContentObjectFolder getContentObjectFolderTree(
			String contentObjectFolderId,
			int depth,
			boolean renderContentObjectIds,
			String locale);

	/**
	 * Delete a content object from content repository.
	 * 
	 * @param objectIdOrSystemName 
	 *            Object {@link ContentObject#getId() id} or {@link ContentObject#getSystemName() system name}.
	 *            
	 * @return <code>true</code> if object has been successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteContentObject(
			String objectIdOrSystemName);

	/**
	 * Retrieves a specific version of a content object.
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
	 * 
	 * @return A content object of specific version.
	 */
	ContentObject getContentObjectByVersionName(
			String contentObjectId,
			String versionName,
			String locale,
			CacheRegion cacheRegion);

	/**
	 * Returns all content objects matching specified criteria.
	 * 
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @deprecated Use {@link #searchContentObjects(ContentObjectCriteria, ResourceRepresentationType)}
	 * @return A list of content object satisfying criteria.
	 */
	@Deprecated
	CmsOutcome<CmsRankedOutcome<ContentObject>> searchContentObjects(
			ContentObjectCriteria contentObjectCriteria);

	/**
	 * Returns all content objects matching specified criteria in XML.
	 * 
	 * <p>
	 * Use method {@link ContentObjectCriteria#addPropertyPathWhoseValueWillBePreLoaded(String)} 
	 * if you want specific properties to be returned. Otherwise, all content object 
	 * properties will be rendered. 
	 * </p>
	 * 
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @deprecated Use {@link #searchContentObjects(ContentObjectCriteria, ResourceRepresentationType)}
	 * @return XML representation of query results following XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd
	 */
	@Deprecated
	String searchContentObjectsAndExportToXml(
			ContentObjectCriteria contentObjectCriteria);
	

	/**
	 * Returns all content objects matching specified criteria in JSON
	 * following Mapped convention.
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
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @deprecated Use {@link #searchContentObjects(ContentObjectCriteria, ResourceRepresentationType)}
	 * @return JSON representation of query results according to XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd  following Mapped convention
	 */
	@Deprecated
	String searchContentObjectsAndExportToJson(
			ContentObjectCriteria contentObjectCriteria);
	
	/**
	 * Locks a content object. Lock is deep and open-scoped that is unlock must
	 * be done explicitly.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @return Lock token necessary to unlock content object.
	 */
	String lockContentObject(
			String contentObjectId);

	/**
	 * Removes a lock from a content object.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param lockToken
	 *            Lock token necessary to unlock content object.
	 */
	void removeLockFromContentObject(
			String contentObjectId,
			String lockToken);

	/**
	 * Checks if a content object is locked.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @return <code>true</code> if content object is locked,
	 *         <code>false</code> otherwise.
	 */
	boolean isContentObjectLocked(
			String contentObjectId);

	/**
	 * Increase {@link ContentObject content objects's} view counter by <code>counter</code>.
	 * 
	 * ViewCounter is an internal property of Content Object but not a built in.
	 * Therefore its property path is <code>statistic.viewCounter</code> and
	 * if it exists it will appear in native properties of a
	 * {@link ContentObject content objects}.
	 * 
	 * @param contentObjectId
	 *            {@link ContentObject#getId() content object id}.
	 * @param counter Number of views 
	 */
	void increaseContentObjectViewCounter(
			String contentObjectId, long counter);

	/**
	 * Retrieve child cms property.
	 * 
	 * Used to lazy load a child cms property for a specific parent complex cms property.
	 * 
	 * Used mainly when calling method {@link ComplexCmsProperty#createNewValueForMulitpleComplexCmsProperty(String)} or
	 * {@link ComplexCmsProperty#getChildProperty(String)} or {@link ComplexCmsProperty#getChildPropertyList(String)}.
	 * 
	 * Users are STRONLGY encouraged to use the aforementioned methods for retrieving child cms properties.
	 * 
	 * @param childPropertyName The name of the property to load
	 * @param parentComplexCmsPropertyDefinitionFullPath Parent definition full path
	 * @param jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty UUID of Jcr Node which corresponds to parent property
	 * @param jcrNodeUUIDWhichCorrespondsToContentObejct UUID of Jcr Node which corresponds to content object
	 * @param renderProperties Instructions to follow while rendering
	 * 
	 * @return A list of all loaded child cms properties. A list is used in cases where multiple complex cms properties
	 * are loaded for the specified childPropertyName
	 */
	List<CmsProperty<?,?>> loadChildCmsProperty(String childPropertyName, String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties);
	
	/**
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
	 */
	void moveAspectToNativePropertyForAllContentObjectsOFContentType(String aspect, String newPropertyName, String contentType);
	
	/**
	 * Single entry point for retrieving an {@link ContentObject object} from a repository.
	 * 
	 * <p>
	 * A contentObject can be retrieved as XML, as JSON or as a {@link ContentObject} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String contentObjectXML = contentObjectService.getContentObject("id", ResourceRepresentationType.XML, FetchLevel.ENTITY, CacheRegion.ONE_MINUTE, null, false);
	 *  		 
	 *  String contentObjectJSON = contentObjectService.getContentObject("id", ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN, CacheRegion.ONE_MINUTE, Arrays.asList("profile.title","webPublication.webPublicationStartDate"), false);
	 *  		 
	 *  ContentObject contentObject = contentObjectService.getContentObject("id", ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL, CacheRegion.ONE_MINUTE, null, false);
	 *  		
	 *  CmsOutcome<ContentObject> contentObjectOutcome = contentObjectService.getContentObject("id", ResourceRepresentationType.CONTENT_OBJECT_LIST, FetchLevel.FULL, CacheRegion.ONE_MINUTE, null, false);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#CONTENT_OBJECT_LIST} represents a list of
	 * contentObjects, rather than one and therefore its use in this context is not recommended.
	 * Nevertheless, if used, a list containing one contentObject will be provided.
	 * </p>
	 * 
	 * <p>
	 * Users have also the option to pre-fetch specific or all contentObject's properties.
	 * By default, regardless of the requested <code>output</code>, only the following properties are returned
	 * (i.e. default value for <code>fetchLevel</code> is {@link FetchLevel#ENTITY})
	 * 
	 *   <ul>
	 *     <li>Identifier</li>
	 *     <li>System Name</li>
	 *     <li>Content Type</li>
	 *     <li>Owner (external id, label, etc)</li>
	 *     <li>Url</li>
	 *     <li>Title (property profile.title)</li>
	 *   </ul>
	 *   
	 *  
	 *  </p>
	 *  
	 *  <p>
	 *  This is very convenient in cases where the user requested a {@link ContentObject} instance
	 *  or a list of {@link ContentObject} instances ({@link CmsOutcome}) and would like to take 
	 *  advantage of the lazy loading mechanism, i.e. load a property only when its value is requested.
	 *  </p>
	 *  
	 *  <p>
	 *  However, in cases of XML or JSON representation, lazy loading is not available and thus, users
	 *  must be able to explicitly define which properties should be preloaded. This can be done with 
	 *  the combination of <code>fetchLevel</code> and <code>propertyPathsToInclude</code> : 
	 *  (The following rules apply for all resource representation types of the result)
     *  
     *  <ul>
     *  	<li> {@link FetchLevel#FULL} : Loads all properties of a {@link ContentObject}. 
     *  		Value of <code>propertyPathsToInclude</code> is ignored</li>
     *  	<li> {@link FetchLevel#ENTITY} : Loads the aforementioned properties of a 
     *  		{@link ContentObject} plus any property specified in <code>propertyPathsToInclude</code>. If <code>propertyPathsToInclude</code> is empty 
     *  properties <code>owner</code> and <profile.title</code> are not loaded.  
     *  	<li> {@link FetchLevel#ENTITY_AND_CHILDREN} : Loads the aforementioned properties (except owner and title) of a 
     *  {@link ContentObject} plus any property specified in <code>propertyPathsToInclude</code>. It should be stated that
     *  if {@link ResourceRepresentationType#CONTENT_OBJECT_INSTANCE} or {@link ResourceRepresentationType#CONTENT_OBJECT_LIST} resource representation is selected then
     *  the 'owner' is always returned. Also, if <code>propertyPathsToInclude</code> is null or empty, the behavior is exactly the same with {@link FetchLevel#ENTITY}</li>
     *  </ul>
     *  </p>
	 * 
	 * <p>
	 * Also, in cases where no output type is defined, a {@link ContentObject} instance is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myContentObject","localization":{"label":{"en":"My first contentObject"}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"contentObject":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myContentObject","localization":{"label":{"en":"My first contentObject"}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, in case no contentObject is found for provided <code>contentObjectId</code>, 
	 * <code>null</code>is returned.
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link ContentObject} or {@link CmsOutcome}
	 * @param contentObjectIdOrSystemName {@link ContentObject#getId() contentObject id} or {@link ContentObject#getSystemName() contentObject system name} 
	 * @param output ContentObject representation output, one of XML, JSON or {@link ContentObject}. Default is {@link ResourceRepresentationType#CONTENT_OBJECT_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link ContentObject}'s only properties, its children as well or the whole {@link ContentObject}.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param cacheRegion Specify how much time object will remain in cache
	 * @param propertyPathsToInclude A list of property paths whose values will be included (i.e. preloaded) in the export
	 * @param serializeBinaryContent <code>true</code> to export binary content, <code>false</code> otherwise. In any case 
	 * URL of binary content is provided. This property is valid for output formats {@link ResourceRepresentationType#XML} or 
	 * {@link ResourceRepresentationType#JSON} only
	 * 
	 * @return A contentObject as XML, JSON or {@link ContentObject}, or <code>null</code> of none is found.
	 */
	<T> T getContentObject(String contentObjectIdOrSystemName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, CacheRegion cacheRegion, List<String> propertyPathsToInclude, boolean serializeBinaryContent);
	
	/**
	 * Saves or updates a content object.
	 * 
	 * <p>
	 * This method expects either a {@link ContentObject} instance
	 * or a {@link String} instance which corresponds to an XML or
	 * JSON representation of the entity to be saved.
	 * </p>
	 * 
	 * In case content object is locked, use <code>lockToken</code> to unlock it.
	 * 
	 * <p>
	 * If no {@link ContentObject#setContentObjectType(String) content object type} is provided or type 
	 * provided does not correspond to a {@link ContentObjectTypeDefinition definition}
	 * an exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * In case of an update, if {@link ContentObject#setContentObjectType(String) content object type}
	 * is different than the one existed, an exception is thrown.
	 * </p>
	 * 
	 * <p>
	 * If {@link ContentObject#getOwner() owner} does not have an id or it does not exist, 
	 * an exception is thrown. 
	 * </p>
	 * <p>
	 * Whether save or update process is followed depends on whether <code>contentObject</code>
	 *  is a new contentObject or not. <code>contentObject</code> is considered new if there is no 
	 * {@link ContentObject#getId() id} or <code>id</code> is provided but there is
	 * no {@link ContentObject contentObject} in repository for the provided <code>id</code>. In this case
	 * <code>contentObject</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>contentObject</code> is considered new)
	 * <ul>
	 * <li> Create a new {@link ContentObject#getId() id} or use the provided <code>id</code>.
	 * <li> Create value for property <code>profile.created</code> to denote content object's creation date.
	 * If property <code>profile.created</code> is defined and a value for that property is provided, 
	 * then that value is used. Otherwise (if no value exists or that property is not defined) 
	 * current date time will be used to denote content object's creation date. 
	 * <li> Relate <code>contentObject</code> with the provided {@link ContentObject#getOwner() owner}.
	 * 		ContentObject's owner MUST already exist. If not, an exception is thrown.
	 * <li> Inserts all provided {@link CmsProperty properties} found under {@link ComplexCmsRootProperty root property}
	 * into repository. 
	 * <li> A {@link SimpleCmsProperty simple property} with no {@link SimpleCmsProperty#getSimpleTypeValues() values}
	 * at all, is not saved. If this property is mandatory an exception is thrown.
	 * <li> A {@link ComplexCmsProperty complex property} with no {@link ComplexCmsProperty#getChildProperties() child properties}
	 * at all, is not saved. If this property is mandatory an exception is thrown. The same occurs if this property contains
	 * child properties but ALL of them on their part are considered empty, that is have no values.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>topic</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>contentObject</code> with the provided {@link ContentObject#getOwner() owner}.
	 * 		ContentObject's owner MUST already exist. If not, an exception is thrown.
	 * 	<li> Updates all provided {@link CmsProperty properties} found under {@link ComplexCmsRootProperty root property}
	 * 		into repository.
	 *  <li> A {@link SimpleCmsProperty simple property} with no {@link SimpleCmsProperty#getSimpleTypeValues() values}
	 * at all, is not saved. If this property is mandatory an exception is thrown.
	 * <li> A {@link ComplexCmsProperty complex property} with no {@link ComplexCmsProperty#getChildProperties() child properties}
	 * at all, is not saved. If this property is mandatory an exception is thrown. The same occurs if this property contains
	 * child properties but ALL of them on their part are considered empty, that is have no values.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Finally, in order to remove a {@link CmsProperty property}, one must call
	 * {@link ContentObject#removeCmsProperty(String)} or {@link ComplexCmsProperty#removeChildProperty(String)}
	 * and then save content object. Again if this property is mandatory an exception is thrown. 
	 * </p>
	 * 
	 * <p>
	 * Every time a content object is saved, unless specified otherwise,
	 * Astroboa updates value of built in property <code>profile.modified</code>
	 * to current datetime. Set value <code>false</code> for <code>updateLastModificationDate</code>
	 * to disable above behavior.
	 * </p>
	 * 
	 * <p>
	 * Please note in all cases, property {@link ContentObject#setContentObjectType(String) contentObjectType}
	 * must be provided. 
	 * </p>
	 * 
	 * @param contentObject
	 *            {@link ContentObject} to be saved or updated.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise.
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise
	 * @param lockToken
	 *            Token created when content object was locked.
	 * 
	 * @throws {@link CmsException}
	 *             in case content object is locked.
	 *             
 	 * @return Newly created or updated {@link ContentObject} instance.
	 */
	ContentObject save(Object contentObject, boolean version, boolean updateLastModificationTime, String lockToken);
	
	/**
	 * Query content using {@link ContentObjectCriteria} and specifying 
	 * result output representation.
	 *  
	 * <p>
	 * Query results can be retrieved as XML, as JSON or as a {@link CmsOutcome<ContentObject>} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String resultAsXML = contentObjectService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
	 *  		 
	 *  String resultAsJSON = contentObjectService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
	 *  		 
	 *  ContentObject contentObject = contentObjectService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	 *  		
	 *  CmsOutcome<ContentObject> resultAsOutcome = contentObjectService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#CONTENT_OBJECT_INSTANCE} represents one content object
	 * only, rather than a list and therefore its use in this context is not recommended. However in the following
	 * cases a single content object or null is returned, instead of throwing an exception.
	 * 
	 * <ul>
	 * <li>User specified limit to be 1 ({@link ContentObjectCriteria#setLimit(1)}).
	 * 	In this case the first content object matching criteria is returned, or null if none matched criteria.<li>
	 * <li>User specified no limit or limit greater than 1. In this case if more than one content objects match
	 * criteria an exception is thrown</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Also, in cases where no output type is defined a {@link CmsOutcome<ContentObject>} instance is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myContentObject","localization":{"label":{"en":"My first contentObject"}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"contentObject":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myContentObject","localization":{"label":{"en":"My first contentObject"}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, if no result is found, 
	 * 
	 * <ul>
	 * <li><code>null</code> is returned if <code>output</code> {@link ResourceRepresentationType#CONTENT_OBJECT_INSTANCE}</li>
	 * <li><pre>{
     *				"totalResourceCount" : "0",
  	 *				"offset" : "0"
	 *			}
	 *	</pre> is returned if <code>output</code> {@link ResourceRepresentationType#JSON}</li>
	 * <li><pre><?xml version="1.0" encoding="UTF-8"?>
	 * 				<bccmsapi:resourceResponse 
	 * 					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	 * 					xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model" 
	 * 					xmlns:bccmsapi="http://www.betaconceptframework.org/schema/astroboa/api" 
	 * 				 	offset="0"
	 * 					totalResourceCount="0"
	 * 				/>
	 * </pre> is returned if <code>output</code> {@link ResourceRepresentationType#XML}</li>
	 * <li>empty {@link CmsOutcome<ContentObject>} is returned if <code>output</code> {@link ResourceRepresentationType#CONTENT_OBJECT_LIST}</li>
	 * </ul>
	 * 
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link ContentObject} or {@link CmsOutcome}
	 * @param contentObjectCriteria
	 *            Restrictions for content object and render instructions for
	 *            query results.
	 * @param output ContentObject representation output, one of XML, JSON or {@link ContentObject}. 
	 * 	Default is {@link ResourceRepresentationType#CONTENT_OBJECT_LIST}
	 * 
	 * @return ContentObjects as XML, JSON or {@link CmsOutcome<ContentObject>}
	 */
	<T> T  searchContentObjects(ContentObjectCriteria contentObjectCriteria, ResourceRepresentationType<T> output);

	/**
	 * Support for deep copy of a contentObject. 
	 * 
	 * <p>
	 * This method is responsible to return an exact copy of the specified {@link ContentObject}.
	 * It does not save fresh copy. 
	 * </p>
	 * 
	 * <p>
	 * In cases where logged in user is not the owner of the content object, copy is allowed only if
	 * user has read permissions on that content object. 
	 * In these cases, copied {@link ContentObject}'s owner is the user currently logged in.
	 * </p>
	 * 
	 * <p>
	 * During copy operation, content object system name and title change in order to 
	 * make apparent that this content object is a fresh copy. Users, of course, can change
	 * them according to their needs. 
	 * </p>
	 * 
	 * <p>
	 * Prefix 'copy' plus the index of the copy is added to contentObject's system name.
	 * Copy index is added at the end of the title as well. 
	 * </p>
	 * 
	 * <p>
	 * Copy index is calculated by retrieving all content objects whose system name ends with
	 * source content object's system name. These most likely have been copies of the content object.
	 * Of course, index cannot always be correct but in most cases it is a good approach
	 * to calculate copy index. 
	 * </p>
	 *  
	 * 
	 * @param contetObjectId Identifier of ContentObject to be copied
	 * @return Cloned {@link ContentObject} or an exception if no content object is found or user is not authorized to 
	 * perform this operation 
	 */
	ContentObject copyContentObject(String contetObjectId);

	/**
	 * Check that provided property has a value.
	 * 
	 * <p>
	 * This method does not load property and its values. It just checks whether there is a value
	 * for this property in the repository.
	 * </p>
	 * 
	 * <p>
 	 * Used mainly when calling method {@link ComplexCmsProperty#hasValueForChildProperty(String)} or
 	 * {@link ContentObject#hasValueForProperty(String)} 
	 * 
	 * Users are STRONLGY encouraged to use the aforementioned methods for checking value existence of child cms properties.
	 * </p>
	 * 
	 * @param propertyPath The name of the property to load
	 * @param jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty UUID of Jcr Node which corresponds to parent property
	 * 
	 * @return <code>true</code> if value exists for the provided property, <code>false</code> otherwise
	 */
	boolean hasValueForProperty(String propertyPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);

	/**
	 * Import content object resource collection (XML, JSON or {@link List} of {@link ContentObject}) to the repository. 
	 * 
	 * <p>
	 * Very convenient method for batch saving. Users may use this method to save 
	 * modified query results which were returned by method {@link #searchContentObjects(org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria, org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType)}
	 * </p>
	 * 
	 * <p>
	 * The same security rules with {@link ContentService#save(Object, boolean, boolean, String)}
	 * apply to each resource.
	 * </p>
	 * 
	 * <p>
	 * Once this functionality is stable, it will be removed under the {@link #save(Object, boolean, boolean, String)} method.
	 * Track the progress of this issue at http://jira.betaconceptframework.org/browse/ASTROBOA-118
	 *</p>
	 * @param contentSource Xml or JSON  or {@link ResourceRepresentationType#CONTENT_OBJECT_LIST} representation of a collection of {@link ContentObject}s.
	 * @param version
	 *            <code>true</code> to create a new version for content
	 *            object, <code>false</code> otherwise. Taken into account only if <code>save</code> is <code>true</code>
	 * @param updateLastModificationDate <code>true</code> to change last modification date, <code>false</code> otherwise. 
	 * Taken into account only if <code>save</code> is <code>true</code>
	 * @param lockToken
	 *            Token created when content object was locked.
	 * @return Imported {@link ContentObject}
	 */
	List<ContentObject> saveContentObjectResourceCollection(Object contentSource,boolean version, boolean updateLastModificationTime, String lockToken);

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
	 * 
	 * @return <code>true</code> if value exists for the provided property, <code>false</code> otherwise
	 */
	byte[] getBinaryChannelContent(String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel);
}