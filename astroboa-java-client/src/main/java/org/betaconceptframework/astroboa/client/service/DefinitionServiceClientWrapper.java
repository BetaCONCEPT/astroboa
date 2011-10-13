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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.secure.DefinitionServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.client.dao.CachedDefinitionServiceDao;

/**
 * Remote Definition Service Wrapper responsible to connect to the provided repository
 * before any of this method is called. 
 * 
 * Also this wrapper contains a definition cache in order to avoid the serialization and deserialization
 * of CmsPropertyDefintions.
 * 
 * This cache is enabled only when using remote services and it is maintained by the client which means that
 * for the moment there is no way this client knows whether remote definitions have been modified.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionServiceClientWrapper extends AbstractClientServiceWrapper implements DefinitionService{


	private DefinitionServiceSecure definitionServiceSecure;

	private CachedDefinitionServiceDao cachedDefinitionServiceDao;

	public DefinitionServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	protected void resetService() {
		definitionServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				definitionServiceSecure = (DefinitionServiceSecure)connectToLocalService(DefinitionServiceSecure.class);

				cachedDefinitionServiceDao = null;
			}
			else{
				definitionServiceSecure = (DefinitionServiceSecure) connectToRemoteService(DefinitionServiceSecure.class);

				cachedDefinitionServiceDao = new CachedDefinitionServiceDao();
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			definitionServiceSecure = null;
		}

		return definitionServiceSecure != null;
	}

	public ComplexCmsPropertyDefinition getAspectDefinition(
			String complexCmsPropertyName) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//Check remote definition dao
			ComplexCmsPropertyDefinition aspectDefinition = null;

			try {
				if (cachedDefinitionServiceDao != null){
					aspectDefinition = cachedDefinitionServiceDao.getAspectDefinition(complexCmsPropertyName);
				}

				//Aspect definition was not found in cache. Perform remote call
				if (aspectDefinition == null){
					aspectDefinition = definitionServiceSecure.getAspectDefinition(complexCmsPropertyName, getAuthenticationToken());

					if (aspectDefinition != null && cachedDefinitionServiceDao != null){
						cachedDefinitionServiceDao.addAspectDefinitionToCache(aspectDefinition);
					}
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}


			return aspectDefinition;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}



	public List<ComplexCmsPropertyDefinition> getAspectDefinitionsSortedByLocale(
			List<String> complexCmsPropertyNames, String locale) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			List<ComplexCmsPropertyDefinition> aspectDefinitionsSortedByLocale = null;
			try {
				if (cachedDefinitionServiceDao != null){
					aspectDefinitionsSortedByLocale = cachedDefinitionServiceDao.getAspectDefinitionsSortedByLocale(complexCmsPropertyNames, locale);
				}

				if (CollectionUtils.isEmpty(aspectDefinitionsSortedByLocale)){
					aspectDefinitionsSortedByLocale = definitionServiceSecure.getAspectDefinitionsSortedByLocale(complexCmsPropertyNames, locale, getAuthenticationToken());

					if (CollectionUtils.isNotEmpty(aspectDefinitionsSortedByLocale) && cachedDefinitionServiceDao != null){
						for (ComplexCmsPropertyDefinition aspectDefinition : aspectDefinitionsSortedByLocale){
							//Add them to cache
							cachedDefinitionServiceDao.addAspectDefinitionToCache(aspectDefinition);
						}
					}
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}

			return aspectDefinitionsSortedByLocale;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}



	public List<ComplexCmsPropertyDefinition> getAvailableAspectDefinitionsSortedByLocale(
			String locale) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			List<ComplexCmsPropertyDefinition> availableAspectDefinitionsSortedByLocale = null;
			try {
				if (cachedDefinitionServiceDao != null){
					availableAspectDefinitionsSortedByLocale = cachedDefinitionServiceDao.getAvailableAspectDefinitionsSortedByLocale(locale);
				}

				if (CollectionUtils.isEmpty(availableAspectDefinitionsSortedByLocale) ){
					availableAspectDefinitionsSortedByLocale = definitionServiceSecure.getAvailableAspectDefinitionsSortedByLocale(locale, getAuthenticationToken());

					if (CollectionUtils.isNotEmpty(availableAspectDefinitionsSortedByLocale)&& cachedDefinitionServiceDao != null){
						for (ComplexCmsPropertyDefinition aspectDefinition : availableAspectDefinitionsSortedByLocale){
							//Add them to cache
							cachedDefinitionServiceDao.addAspectDefinitionToCache(aspectDefinition);
						}
					}
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}

			return availableAspectDefinitionsSortedByLocale;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}



	public CmsPropertyDefinition getCmsPropertyDefinition(
			String relativePropertyPath, String contentObjectTypeDefinitionName) {


		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			CmsPropertyDefinition cmsPropertyDefinition = null;
			try {

				if (cachedDefinitionServiceDao != null){
					cmsPropertyDefinition = cachedDefinitionServiceDao.getCmsPropertyDefinition(relativePropertyPath, contentObjectTypeDefinitionName);
				}

				if (cmsPropertyDefinition == null){
					cmsPropertyDefinition = definitionServiceSecure.getCmsPropertyDefinition(relativePropertyPath, contentObjectTypeDefinitionName, getAuthenticationToken());

					if (cmsPropertyDefinition != null && cachedDefinitionServiceDao != null){
						//Get ContentObjectTypeDefinion and put in cache
						//so that other property definitions for this type will be available
						ContentObjectTypeDefinition contentTypeDefinition = getContentObjectTypeDefinition(contentObjectTypeDefinitionName);

						cachedDefinitionServiceDao.addContentTypeDefinitionToCache(contentTypeDefinition);
					}
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}



			return cmsPropertyDefinition;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}



	public ContentObjectTypeDefinition getContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			ContentObjectTypeDefinition contentObjectTypeDefinition = null;
			try {

				if (cachedDefinitionServiceDao != null){
					contentObjectTypeDefinition = cachedDefinitionServiceDao.getContentObjectTypeDefinition(contentObjectTypeDefinitionName);
				}

				if (contentObjectTypeDefinition ==null){
					contentObjectTypeDefinition = definitionServiceSecure.getContentObjectTypeDefinition(contentObjectTypeDefinitionName, getAuthenticationToken());

					if (contentObjectTypeDefinition != null && cachedDefinitionServiceDao != null){
						cachedDefinitionServiceDao.addContentTypeDefinitionToCache(contentObjectTypeDefinition);
					}
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}


			return contentObjectTypeDefinition;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}



	public List<String> getContentObjectTypes() {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since only a list will be serialized
			return definitionServiceSecure.getContentObjectTypes(getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}



	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies() {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since only a map will be serialized
			return definitionServiceSecure.getTopicPropertyPathsPerTaxonomies(getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}



	public byte[] getXMLSchemaForDefinition(String definitionFullPath) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since the actual file resides in the remote server
			return definitionServiceSecure.getXMLSchemaForDefinition(definitionFullPath, getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}

	public boolean hasContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since only a boolean and a string will be serialized
			return definitionServiceSecure.hasContentObjectTypeDefinition(contentObjectTypeDefinitionName, getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}

	public CmsPropertyDefinition getCmsPropertyDefinition(
			String fullPropertyDefinitionPath) {

		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			CmsPropertyDefinition cmsPropertyDefinition = null;
			try {

				if (cachedDefinitionServiceDao != null){
					cmsPropertyDefinition = cachedDefinitionServiceDao.getCmsPropertyDefinition(fullPropertyDefinitionPath);
				}

				if (cmsPropertyDefinition == null){
					cmsPropertyDefinition = definitionServiceSecure.getCmsPropertyDefinition(fullPropertyDefinitionPath, getAuthenticationToken());

					if (cmsPropertyDefinition != null && cachedDefinitionServiceDao != null){
						//Cache its parent which is either a complex cms property definition or a content typ definition
						cachedDefinitionServiceDao.cacheParentDefinition(fullPropertyDefinitionPath, cmsPropertyDefinition);
					}
				}

			} catch (Exception e) {
				throw new CmsException(e);
			}


			return cmsPropertyDefinition;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public List<String> getMultivalueProperties() {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since only a map will be serialized
			return definitionServiceSecure.getMultivalueProperties(getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}

	}

	@Override
	public Map<String, List<String>> getContentTypeHierarchy() {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			//This method does not require caching since only a map will be serialized
			return definitionServiceSecure.getContentTypeHierarchy(getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public <T> T getCmsDefinition(String fullPropertyDefinitionPath,
			ResourceRepresentationType<T> output, boolean prettyPrint) {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			
			T cmsDefinition = null;
			try {

				if (cachedDefinitionServiceDao != null){
					
					logger.debug("Looking in cache for definition {}", fullPropertyDefinitionPath);
					
					cmsDefinition = cachedDefinitionServiceDao.getCmsDefinition(fullPropertyDefinitionPath,output, prettyPrint);
				}

				if (cmsDefinition == null){
					
					logger.debug("Definition Service is used to retrieve definition {} ", fullPropertyDefinitionPath);
					
					cmsDefinition = definitionServiceSecure.getCmsDefinition(fullPropertyDefinitionPath, output, prettyPrint, getAuthenticationToken());

					if (cmsDefinition != null && cachedDefinitionServiceDao != null){
						
						logger.debug("Caching definition {} since client is connected to a remote server", fullPropertyDefinitionPath);
						
						//Cache its parent which is either a complex cms property definition or a content typ definition
						cachedDefinitionServiceDao.cacheCmsDefinition(fullPropertyDefinitionPath, cmsDefinition, output, prettyPrint);
					}
				}

			} catch (Exception e) {
				throw new CmsException(e);
			}

			return cmsDefinition;
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public boolean validateDefinintion(String definition, String definitionFileName) {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}

			return definitionServiceSecure.validateDefinintion(definition, definitionFileName, getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public Map<String, Integer> getDefinitionHierarchyDepthPerContentType() {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}

			return definitionServiceSecure.getDefinitionHierarchyDepthPerContentType(getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public Integer getDefinitionHierarchyDepthForContentType(String contentType) {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}

			return definitionServiceSecure.getDefinitionHierarchyDepthForContentType(contentType, getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}

	@Override
	public ValueType getTypeForProperty(String contentType, String propertyPath) {
		if (definitionServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}

			return definitionServiceSecure.getTypeForProperty(contentType, propertyPath, getAuthenticationToken());
		}
		else{
			throw new CmsException("DefinitionService reference was not found");
		}
	}
}
