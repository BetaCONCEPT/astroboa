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

package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * This ejb3 bean does not implement the content service.
 * Repository services have been implemented through spring
 * The sole purpose of this class is to expose the content service as an ejb3 bean. 
 * It acquires the content service from JNDI where we have deployed a shared spring context with the beans that implement the repository services
 * Through the @Resource annotation we get the shared spring context from the JNDI and then we get the content service bean from the spring context.
 * The methods then use the spring bean to call the required functionality.
 * Another option would be to not separately deploy the spring context but have the ejb3 bean to bootstrap the spring and get the required beans.
 * This can be done through the following interceptor: @Interceptors(SpringBeanAutowiringInterceptor.class) and a refParentContext.xml file in repository module classpath that loads the 
 * spring bean files under a parent context.
 * We have chosen no to do so in order to allow applications to access the repository in both ways. Either as spring service beans or as ejb3 service beans.  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Local({ContentServiceSecure.class})
@Remote({RemoteContentServiceSecure.class})
@Stateless(name="ContentServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class ContentServiceSecureImpl extends AbstractSecureAstroboaService implements ContentServiceSecure {
	
	private ContentService contentService;
	
	void initializeOtherRemoteServices() {
		contentService = (ContentService) springManagedRepositoryServicesContext.getBean("contentService");
	}
	
	@RolesAllowed("ROLE_CMS_EDITOR")
	public boolean deleteContentObject(String objectIdOrSystemName,String authenticationToken) {
		
		return contentService.deleteContentObject(objectIdOrSystemName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public BinaryChannel getBinaryChannelById(String binaryChannelId,String authenticationToken) {
		
		return contentService.getBinaryChannelById(binaryChannelId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ContentObject getContentObject(String contentObjectId,
			RenderProperties contentObjectRenderProperties,
			CacheRegion cacheRegion,String authenticationToken) {
		
		return contentService.getContentObject(contentObjectId, contentObjectRenderProperties, cacheRegion);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ContentObject getContentObjectById(String contentObjectId,
			CacheRegion cacheRegion,String authenticationToken) {
		
		return contentService.getContentObjectById(contentObjectId,cacheRegion);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ContentObject getContentObjectByIdAndLocale(String contentObjectId,
			String locale,
			CacheRegion cacheRegion,String authenticationToken) {
		
		return contentService.getContentObjectByIdAndLocale(contentObjectId, locale, cacheRegion);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ContentObject getContentObjectByVersionName(String contentObjectId,
			String versionName, String locale,
			CacheRegion cacheRegion,String authenticationToken) {
		
		return contentService.getContentObjectByVersionName(contentObjectId, versionName, locale, cacheRegion);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public ContentObjectFolder getContentObjectFolderTree(
			String contentObjectFolderId, int depth,
			boolean renderContentObjectIds, String locale,String authenticationToken) {
		
		return contentService.getContentObjectFolderTree(contentObjectFolderId, depth, renderContentObjectIds, locale);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<ContentObjectFolder> getRootContentObjectFolders(String locale,String authenticationToken) {
		
		return contentService.getRootContentObjectFolders(locale);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public void increaseContentObjectViewCounter(String contentObjectId,long counter,String authenticationToken) {
		
		contentService.increaseContentObjectViewCounter(contentObjectId, counter);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public boolean isContentObjectLocked(String contentObjectId,String authenticationToken) {
		
		return contentService.isContentObjectLocked(contentObjectId);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public String lockContentObject(String contentObjectId,String authenticationToken) {
		
		return contentService.lockContentObject(contentObjectId);
	}


	@RolesAllowed("ROLE_CMS_EDITOR")
	public void removeLockFromContentObject(String contentObjectId,
			String lockToken,String authenticationToken) {
		
		contentService.removeLockFromContentObject(contentObjectId, lockToken);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject saveAndVersionContentObject(ContentObject contentObject,String authenticationToken) {
		
		return contentService.saveAndVersionContentObject(contentObject);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject saveAndVersionLockedContentObject(
			ContentObject contentObject, String lockToken,String authenticationToken) {
		
		return contentService.saveAndVersionLockedContentObject(contentObject, lockToken);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject saveContentObject(ContentObject contentObject, boolean version,String authenticationToken) {
		
		return contentService.saveContentObject(contentObject, version);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject saveLockedContentObject(ContentObject contentObject,
			boolean version, String lockToken,String authenticationToken) {
		
		return contentService.saveLockedContentObject(contentObject, version, lockToken);
	}

	//This annotation will create some interceptors if this module runs inside Jboss
	//but according to Jboss unless @SecutiryDomain is provided
	//these interceptors will not run.Therefore AstroboaSecurityAuthenticationInterceptor
	//performs role base authorization independent of Jboss interceptors
	@RolesAllowed({"ROLE_CMS_EXTERNAL_VIEWER"})
	public CmsOutcome<CmsRankedOutcome<ContentObject>> searchContentObjects(
			ContentObjectCriteria contentObjectCriteria,String authenticationToken) {
		
		return contentService.searchContentObjects(contentObjectCriteria);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<CmsProperty<?, ?>> loadChildCmsProperty(
			String childPropertyName,
			String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties,String authenticationToken) {
		
		return contentService.loadChildCmsProperty(childPropertyName, parentComplexCmsPropertyDefinitionFullPath, 
				jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct, 
				renderProperties);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public void moveAspectToNativePropertyForAllContentObjectsOFContentType(
			String aspect, String newPropertyName, String contentType,
			String authenticationToken) {
		contentService.moveAspectToNativePropertyForAllContentObjectsOFContentType(aspect, newPropertyName, contentType);
		
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public String searchContentObjectsAndExportToXml(
			ContentObjectCriteria contentObjectCriteria,
			String authenticationToken) {
		return contentService.searchContentObjectsAndExportToXml(contentObjectCriteria);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject saveContentObject(ContentObject contentObject,
			boolean version, boolean updateLastModificationDate,
			String authenticationToken) {
		return contentService.saveContentObject(contentObject, version, updateLastModificationDate);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public String searchContentObjectsAndExportToJson(
			ContentObjectCriteria contentObjectCriteria,
			String authenticationToken) {
		return contentService.searchContentObjectsAndExportToJson(contentObjectCriteria);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getContentObject(String contentObjectIdOrSystemName,
			ResourceRepresentationType<T> output, FetchLevel fetchLevel,
			CacheRegion cacheRegion,
			List<String> propertyPathsToInclude,
			boolean serializeBinaryContent,
			String authenticationToken) {
		return contentService.getContentObject(contentObjectIdOrSystemName, output, fetchLevel, cacheRegion, propertyPathsToInclude, serializeBinaryContent);
	}


	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject save(Object contentObject, boolean version,
			boolean updateLastModificationTime, String lockToken,
			String authenticationToken) {
		
		return contentService.save(contentObject, version, updateLastModificationTime, lockToken);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T searchContentObjects(
			ContentObjectCriteria contentObjectCriteria,
			ResourceRepresentationType<T> output, String authenticationToken) {
		return contentService.searchContentObjects(contentObjectCriteria, output);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public ContentObject copyContentObject(String contetObjectId,
			String authenticationToken) {

		return contentService.copyContentObject(contetObjectId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public boolean hasValueForProperty(String propertyPath,
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty,
			String authenticationToken) {
		return contentService.hasValueForProperty(propertyPath, jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public List<ContentObject> saveContentObjectResourceCollection(
			Object contentSource, boolean version,
			boolean updateLastModificationTime, String lockToken,
			String authenticationToken) {
		return contentService.saveContentObjectResourceCollection(contentSource, version, updateLastModificationTime, lockToken);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public byte[] getBinaryChannelContent(
			String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel,
			String authenticationToken) {

		return contentService.getBinaryChannelContent(jcrNodeUUIDWhichCorrespondsToTheBinaryChannel);
	}
}
