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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Content Service Client Wrapper.
 * 
 * Responsible to connect to appropriate content service (either local or remote)
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentServiceClientWrapper extends AbstractClientServiceWrapper implements ContentService  {


	private ContentServiceSecure contentServiceSecure;

	public ContentServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	protected void resetService() {
		contentServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				contentServiceSecure = (ContentServiceSecure) connectToLocalService(ContentServiceSecure.class);
			}
			else{
				contentServiceSecure = (ContentServiceSecure) connectToRemoteService(ContentServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			contentServiceSecure = null;
		}

		return contentServiceSecure != null;
	}

	public boolean deleteContentObject(String objectIdOrSystemName) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.deleteContentObject(objectIdOrSystemName, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public BinaryChannel getBinaryChannelById(String binaryChannelId) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.getBinaryChannelById(binaryChannelId, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}

	}






	public ContentObject getContentObjectByVersionName(String contentObjectId,
			String versionName, String locale,
			CacheRegion cacheRegion) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.getContentObjectByVersionName(contentObjectId, versionName, locale, cacheRegion, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public ContentObjectFolder getContentObjectFolderTree(
			String contentObjectFolderId, int depth,
			boolean renderContentObjectIds, String locale) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.getContentObjectFolderTree(contentObjectFolderId, depth, renderContentObjectIds, locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public List<ContentObjectFolder> getRootContentObjectFolders(String locale) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.getRootContentObjectFolders(locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public void increaseContentObjectViewCounter(String contentObjectId, long counter) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			contentServiceSecure.increaseContentObjectViewCounter(contentObjectId, counter, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public boolean isContentObjectLocked(String contentObjectId) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.isContentObjectLocked(contentObjectId, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public List<CmsProperty<?, ?>> loadChildCmsProperty(
			String childPropertyName,
			String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return  contentServiceSecure.loadChildCmsProperty(childPropertyName, parentComplexCmsPropertyDefinitionFullPath, 
					jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct, 
					renderProperties, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	public String lockContentObject(String contentObjectId) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return contentServiceSecure.lockContentObject(contentObjectId, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}



	public void removeLockFromContentObject(String contentObjectId,
			String lockToken) {

		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			contentServiceSecure.removeLockFromContentObject(contentObjectId, lockToken, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}


	private void disableLazyLoadingIfClientsConnectsToARemoteRepository(
			ContentObjectCriteria contentObjectCriteria) {
		
		if (successfullyConnectedToRemoteService && contentObjectCriteria != null){
			
			boolean lazyLoadingHasNotBeenSet = contentObjectCriteria.getRenderProperties() != null &&
			! contentObjectCriteria.getRenderProperties().allContentObjectPropertiesAreRendered();
	
			//Disable lazy loading only if user has not set it and limit is less than 500
			if (lazyLoadingHasNotBeenSet &&  contentObjectCriteria.getLimit() > 0 && contentObjectCriteria.getLimit() < 500){
				contentObjectCriteria.getRenderProperties().renderAllContentObjectProperties(true);
			}
		}
	}

	public void moveAspectToNativePropertyForAllContentObjectsOFContentType(
			String aspect, String newPropertyName, String contentType) {
		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			 contentServiceSecure.moveAspectToNativePropertyForAllContentObjectsOFContentType(aspect, newPropertyName, contentType, getAuthenticationToken());
			}
		else{
			throw new CmsException("ContentService reference was not found");
		}
		
	}


	@Override
	public <T> T getContentObject(String contentObjectIdOrSystemName,
			ResourceRepresentationType<T> output, FetchLevel fetchLevel,
			CacheRegion cacheRegion, List<String> propertyPathsToInclude, boolean serializeBinaryContent) {
		if (contentServiceSecure != null){
			
			return contentServiceSecure.getContentObject(contentObjectIdOrSystemName, output, fetchLevel, cacheRegion, propertyPathsToInclude, serializeBinaryContent, getAuthenticationToken());
			
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public ContentObject save(Object contentObject, boolean version,
			boolean updateLastModificationTime, String lockToken) {
		if (contentServiceSecure != null){
			return contentServiceSecure.save(contentObject, version, updateLastModificationTime, lockToken, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public <T> T searchContentObjects(
			ContentObjectCriteria contentObjectCriteria,
			ResourceRepresentationType<T> output) {
		if (contentServiceSecure != null){
			disableLazyLoadingIfClientsConnectsToARemoteRepository(contentObjectCriteria);

			return contentServiceSecure.searchContentObjects(contentObjectCriteria, output, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public ContentObject copyContentObject(String contetObjectId) {
		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){
				client.activateClientContext();
			}
			return contentServiceSecure.copyContentObject(contetObjectId, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public boolean hasValueForProperty(String propertyPath,
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty) {
		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			return contentServiceSecure.hasValueForProperty(propertyPath, jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public List<ContentObject> saveContentObjectResourceCollection(
			Object contentSource, boolean version,
			boolean updateLastModificationTime, String lockToken) {
		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			return contentServiceSecure.saveContentObjectResourceCollection(contentSource, version, updateLastModificationTime, lockToken, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}

	@Override
	public byte[] getBinaryChannelContent(
			String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel) {
		if (contentServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			return contentServiceSecure.getBinaryChannelContent(jcrNodeUUIDWhichCorrespondsToTheBinaryChannel, getAuthenticationToken());
		}
		else{
			throw new CmsException("ContentService reference was not found");
		}
	}
}
