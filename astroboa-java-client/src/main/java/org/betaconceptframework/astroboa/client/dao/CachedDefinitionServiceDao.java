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
package org.betaconceptframework.astroboa.client.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.cache.DefinitionCacheManager;
import org.betaconceptframework.astroboa.cache.region.AstroboaDefinitionCacheRegion;
import org.betaconceptframework.astroboa.service.dao.DefinitionServiceDao;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CachedDefinitionServiceDao extends DefinitionServiceDao {

	protected  final Logger logger = LoggerFactory.getLogger(getClass());
	
	//key is comprised of the following values
	// path#output#prettyPrint For example
	// eventObject.profile.title#XML#true
	private ConcurrentMap<String, Object> definitionsPerPath = new ConcurrentHashMap<String, Object>();
	
	
	public CachedDefinitionServiceDao(){
		AstroboaDefinitionCacheRegion definitionCacheRegion = new AstroboaDefinitionCacheRegion();
		definitionCacheRegion.setDefinitionCacheManager(new DefinitionCacheManager());
		setDefinitionCacheRegion(definitionCacheRegion);
	}

	public void addAspectDefinitionToCache(
			ComplexCmsPropertyDefinition aspectDefinition) throws Exception {

		definitionCacheRegion.putDefinition(ValueType.Complex, aspectDefinition.getName(), aspectDefinition);
	}

	public void addContentTypeDefinitionToCache(
			ContentObjectTypeDefinition contentTypeDefinition) throws Exception {
		
		definitionCacheRegion.putDefinition(ValueType.ContentType, contentTypeDefinition.getName(), contentTypeDefinition);
		
	}
	
	public void cacheParentDefinition(String fullPropertyDefinitionPath,
			CmsPropertyDefinition cmsPropertyDefinition) throws Exception {
		
		CmsDefinition parentDefinition = cmsPropertyDefinition.getParentDefinition();
		
		if (parentDefinition == null){
			if (cmsPropertyDefinition instanceof ComplexCmsPropertyDefinition){
				addAspectDefinitionToCache((ComplexCmsPropertyDefinition)cmsPropertyDefinition);
			}
			else{
				throw new CmsException("Property "+fullPropertyDefinitionPath + " is simple and returned from remote server without a parent");
			}
		}
		else{
			
			if (parentDefinition instanceof ContentObjectTypeDefinition){
				addContentTypeDefinitionToCache((ContentObjectTypeDefinition)parentDefinition);
			}
			else{
				if (parentDefinition instanceof ComplexCmsPropertyDefinition){
					cacheParentDefinition(fullPropertyDefinitionPath, (ComplexCmsPropertyDefinition)parentDefinition);
				}
				else{
					throw new CmsException("Property "+parentDefinition.getQualifiedName() + " is "+parentDefinition.getValueType()+" where it should be Complex");
				}
			}
		}
	}
	
	public <T> T getCmsDefinition(String fullPropertyDefinitionPath, ResourceRepresentationType<T> output, boolean prettyPrint) throws Exception{
	
		if (StringUtils.isBlank(fullPropertyDefinitionPath) || output == null){
			return null;
		}
		
		//If a definition instance is requested, use the methods provided by the DefinitionServiceDao
		if (ResourceRepresentationType.DEFINITION_INSTANCE.equals(output)){
			
			if (StringUtils.isBlank(fullPropertyDefinitionPath)){
				return null;
			}

			PropertyPath propertyPath = new PropertyPath(fullPropertyDefinitionPath);

			String firstPart = propertyPath.getPropertyName();
			String restOfPath = propertyPath.getPropertyDescendantPath();

			ContentObjectTypeDefinition contentObjectTypeDefinition = getContentObjectTypeDefinition(firstPart);

			if (contentObjectTypeDefinition == null){
				ComplexCmsPropertyDefinition aspectDefinition = getAspectDefinition(firstPart);

				if (aspectDefinition != null){	
					if (StringUtils.isBlank(restOfPath)){
						return (T) aspectDefinition;
					}
					else{
						return (T) aspectDefinition.getChildCmsPropertyDefinition(restOfPath);
					}
				}
			}
			else{
				if (StringUtils.isBlank(restOfPath)){
					return (T) contentObjectTypeDefinition;
				}
				
				return (T) contentObjectTypeDefinition.getCmsPropertyDefinition(restOfPath);
			}
		}
		
		//Search for definition in the local map
		String key = constructKey(fullPropertyDefinitionPath, output, prettyPrint);
		
		return (T) definitionsPerPath.get(key);
	}
	
	public <T> void cacheCmsDefinition(String fullPropertyDefinitionPath, T cmsDefinition, ResourceRepresentationType<T> output, boolean prettyPrint) throws Exception{

		if (StringUtils.isBlank(fullPropertyDefinitionPath) || cmsDefinition == null || output == null){
			return;
		}
		
		if (cmsDefinition instanceof CmsPropertyDefinition){
			cacheParentDefinition(fullPropertyDefinitionPath, (CmsPropertyDefinition)cmsDefinition);
		}
		else if (cmsDefinition instanceof ContentObjectTypeDefinition){
			addContentTypeDefinitionToCache((ContentObjectTypeDefinition)cmsDefinition);
		}
		else{
			String key = constructKey(fullPropertyDefinitionPath, output, prettyPrint);
			
			if (!definitionsPerPath.containsKey(key)){
				definitionsPerPath.put(key, cmsDefinition);
			}
		}
		
		
		
	}
	
	private <T> String constructKey(String fullPropertyDefinitionPath, ResourceRepresentationType<T> output, boolean prettyPrint){
		return fullPropertyDefinitionPath+"#"+output.toString()+"#"+prettyPrint;
	}
}
