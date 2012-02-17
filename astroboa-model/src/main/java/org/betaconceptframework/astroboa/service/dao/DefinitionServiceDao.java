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
package org.betaconceptframework.astroboa.service.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.cache.region.DefinitionCacheRegion;
import org.betaconceptframework.astroboa.commons.comparator.CmsPropertyDefinitionLocalizedLabelComparator;
import org.betaconceptframework.astroboa.model.impl.item.ContentObjectProfileItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base definition service dao. 
 * 
 * It provides implementation for all methods of DefinitionService except from 
 * method getXMLSchemaFileForDefinition.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class DefinitionServiceDao {

	protected  final Logger logger = LoggerFactory.getLogger(getClass());

	protected DefinitionCacheRegion definitionCacheRegion;

	public void setDefinitionCacheRegion(DefinitionCacheRegion definitionCacheRegion) {
		this.definitionCacheRegion = definitionCacheRegion;
	}

	public boolean hasContentObjectTypeDefinition(String contentObjectTypeDefinitionName) throws Exception {

		return ( getContentObjectTypeDefinition(contentObjectTypeDefinitionName) != null );

	}

	public ContentObjectTypeDefinition getContentObjectTypeDefinition(String contentObjectTypeDefinitionName) throws Exception  {
		return (ContentObjectTypeDefinition) definitionCacheRegion.getContentObjectTypeDefinition(contentObjectTypeDefinitionName);
	}

	public CmsPropertyDefinition getHasVersionContentObjectPropertyDefinition(String contentObjectTypeName) throws Exception  
	{
		return getCmsPropertyDefinition(ContentObjectProfileItem.HasVersion.getLocalPart(),	contentObjectTypeName);
	}

	public List<String> getContentObjectTypes() throws Exception {

		Map<String, ContentObjectTypeDefinition> types = definitionCacheRegion.getAllContentObjectTypeDefinitions();

		if (MapUtils.isNotEmpty(types))
			return new ArrayList<String>(types.keySet());

		return Collections.EMPTY_LIST;

	}

	public CmsPropertyDefinition getCmsPropertyDefinition(String propertyPath, String contentObjectTypeName) throws Exception {
		ContentObjectTypeDefinition typeDefinition = getContentObjectTypeDefinition(contentObjectTypeName);

		if (typeDefinition != null && typeDefinition.hasCmsPropertyDefinitions()){
			return typeDefinition.getCmsPropertyDefinition(propertyPath);
		}
		else
			return null;

	}


	public ComplexCmsPropertyDefinition getAspectDefinition(String complexCmsPropertyName) throws Exception {
		if (StringUtils.isBlank(complexCmsPropertyName))
			return null;

		return (ComplexCmsPropertyDefinition) definitionCacheRegion.getComplexCmsDefinition(complexCmsPropertyName);
	}

	public List<ComplexCmsPropertyDefinition> getAspectDefinitionsSortedByLocale(List<String> aspects, String locale) throws Exception {
		List<ComplexCmsPropertyDefinition> sortedAspectDefinitions = new ArrayList<ComplexCmsPropertyDefinition>();

		if (CollectionUtils.isNotEmpty(aspects))
		{
			CmsPropertyDefinitionLocalizedLabelComparator cmsPropertyDefinitionLocalizedLabelComparator = new CmsPropertyDefinitionLocalizedLabelComparator();
			cmsPropertyDefinitionLocalizedLabelComparator.setLocale(locale);

			for (String aspect: aspects)
			{
				ComplexCmsPropertyDefinition aspectDefinition = getAspectDefinition(aspect);
				if (aspectDefinition != null)
					sortedAspectDefinitions.add(aspectDefinition);
			}

			Collections.sort(sortedAspectDefinitions, cmsPropertyDefinitionLocalizedLabelComparator);

			return sortedAspectDefinitions;
		}

		return sortedAspectDefinitions;
	}

	public List<ComplexCmsPropertyDefinition> getAvailableAspectDefinitionsSortedByLocale(String locale) throws Exception {

		//Get All Aspects
		Map<String, ComplexCmsPropertyDefinition> aspects = (Map<String, ComplexCmsPropertyDefinition>) definitionCacheRegion.getAllComplexCmsPropertyDefinitions();

		List<String> aspectList = new ArrayList<String>();
		if (MapUtils.isNotEmpty(aspects))
			aspectList.addAll(aspects.keySet());

		return getAspectDefinitionsSortedByLocale(aspectList, locale);
	}

	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies() {
		try {
			return definitionCacheRegion.getTopicPropertyPathsPerTaxonomies();
		} catch (Exception e) {
			logger.warn("Could not load topic property paths per taxonomy ", e);
			return new HashMap<String, List<String>>();
		}
	}

	public CmsPropertyDefinition getCmsPropertyDefinition(String fullPropertyDefinitionPath) throws Exception {

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
					return aspectDefinition;
				}
				else{
					return aspectDefinition.getChildCmsPropertyDefinition(restOfPath);
				}
			}

			return null;
		}
		
		if (StringUtils.isBlank(restOfPath)){
			return null;
		}
			
		return contentObjectTypeDefinition.getCmsPropertyDefinition(restOfPath);
	}

	public List<String> getMultivalueProperties() {
		try {
			return definitionCacheRegion.getMultivalueProperties();
		} catch (Exception e) {
			logger.warn("Could not load multivalue properties ", e);
			return new ArrayList<String>();
		}
	}

	public Map<String, List<String>> getContentTypeHierarchy() {
		try {
			return definitionCacheRegion.getContentTypeHierarchy();
		} catch (Exception e) {
			logger.warn("Could not load content type hierarchy ", e);
			return new HashMap<String, List<String>>();
		}
	}
	
	public Map<QName, String> getLocationURLForDefinitions() {
		try {
			return definitionCacheRegion.getLocationURLForDefinitions();
		} catch (Exception e) {
			logger.warn("Could not load content type hierarchy ", e);
			return new HashMap<QName, String>();
		}
	}

	public Map<String, Integer> getDefinitionHierarchyDepthPerContentType() {
		try {

			Map<String, ContentObjectTypeDefinition> typeDefinitions = definitionCacheRegion.getAllContentObjectTypeDefinitions(); 

			Map<String, Integer> depthPerContentType = new HashMap<String, Integer>();

			int max = 0;

			for (ContentObjectTypeDefinition typeDefinition: typeDefinitions.values()){

				int depth = typeDefinition.getDepth();

				depthPerContentType.put(typeDefinition.getName(), depth);

				if (depth > max){
					max = depth;
				}
			}

			depthPerContentType.put(CmsConstants.ANY_NAME, max);

			return depthPerContentType;
			//return definitionCacheRegion.getDefinitionHierarchDepthPerContentType();
		} catch (Exception e) {
			logger.warn("Could not load content type hierarchy depth", e);
			return new HashMap<String, Integer>();
		}
	}

	public Integer getDefinitionHierarchyDepthForContentType(String contentType) {
		
		if (StringUtils.isBlank(contentType)){
			return 0;
		}

		try{
			if (CmsConstants.ANY_NAME.equals(contentType)){
				Map<String, ContentObjectTypeDefinition> typeDefinitions = definitionCacheRegion.getAllContentObjectTypeDefinitions(); 

				int max = 0;

				for (ContentObjectTypeDefinition typeDefinition: typeDefinitions.values()){

					int depth = typeDefinition.getDepth();

					if (depth > max){
						max = depth;
					}
				}

				return max;
			}
			else{
				ContentObjectTypeDefinition typeDefinition = definitionCacheRegion.getContentObjectTypeDefinition(contentType);

				if (typeDefinition != null){
					return typeDefinition.getDepth();
				}
				else{
					return 0;
				}
			}
		} catch (Exception e) {
			logger.warn("Could not find definition hierarchy depth for content type "+contentType, e);
			return 0;
		}
	}

	public ValueType getTypeForProperty(String contentType, String property) {
		
		if (StringUtils.isBlank(property)){
			return null;
		}
		
		if (StringUtils.isNotBlank(contentType)){
			try {
				CmsPropertyDefinition propertyDefinition = getCmsPropertyDefinition(property, contentType);
				
				if (propertyDefinition!=null){
					return propertyDefinition.getValueType();
				}
				
				return null;
			} catch (Exception e) {
				logger.error("Exception thrown when requesting value type of property "+property + " and content type "+contentType +" Null is returned and exception is not forwarded", e);
				return null;
			}
		}
		else{
			
			//user did not provide a content type. First search in all global complex properties
			PropertyPath propertyPath = new PropertyPath(property);
			
			String parentName = propertyPath.getPropertyName();
			
			try {
				
				if (hasContentObjectTypeDefinition(parentName)){
					
					//may be the first part in the property corresponds to a content type
					CmsPropertyDefinition propertyDefinition = getCmsPropertyDefinition(propertyPath.getPropertyDescendantPath(),parentName);
					
					if (propertyDefinition!=null){
						return propertyDefinition.getValueType();
					}
					
				}
				else{
					ComplexCmsPropertyDefinition parentDefinition = getAspectDefinition(propertyPath.getPropertyName());

					if (parentDefinition!=null){
						
						if (propertyPath.getPropertyDescendantPath() != null){
						
							CmsPropertyDefinition definition = parentDefinition.getChildCmsPropertyDefinition(propertyPath.getPropertyDescendantPath());
							
							if (definition != null){
								return definition.getValueType();
							}

							return null;
						}
						else{
							return parentDefinition.getValueType();
						}
					}
				}
			} catch (Exception e) {
				//Do nothing. Search for the definition will continue in all content types
			}
			
			
			//Check in all content types
			try {
				
				Map<String, ContentObjectTypeDefinition> contentTypeDefinitions = definitionCacheRegion.getAllContentObjectTypeDefinitions();
				
				if (contentTypeDefinitions != null && ! contentTypeDefinitions.isEmpty()){
					for (ContentObjectTypeDefinition contentObjectTypeDefinition : contentTypeDefinitions.values()){
						
						CmsPropertyDefinition propertyDefinition = contentObjectTypeDefinition.getCmsPropertyDefinition(property);
						
						if (propertyDefinition!=null){
							return propertyDefinition.getValueType();
						}
					}
				}
			} catch (Exception e) {
				logger.error("Unable to load definitions form cache", e);
				return null;
			}
		}
		
		return null;
	}

}
