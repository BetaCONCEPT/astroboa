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

package org.betaconceptframework.astroboa.cache.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.cache.DefinitionCacheManager;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition cache using its own CacheManager
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaDefinitionCacheRegion implements DefinitionCacheRegion{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private DefinitionCacheManager definitionCacheManager;
	
	//First Key is repository id
	//Second Key is taxonomy name
	private ConcurrentMap<String, Map<String, List<String>>> topicPropertyPathsPerTaxonomies = new ConcurrentHashMap<String, Map<String,List<String>>>();	
	
	//First Key is repository id
	//Second Key is schema file name
	private ConcurrentMap<String,Map<String, byte[]>> xmlSchemaDefinitionsPerFilenamePerRepository = new ConcurrentHashMap<String, Map<String,byte[]>>();
	
	//First Key is repository id
	private ConcurrentMap<String,List<String>> multivaluePropertiesPerRepository = new ConcurrentHashMap<String, List<String>>();
	
	//First Key is repository id
	//Second Key is base content type
	private ConcurrentMap<String, Map<String, List<String>>> contentTypeHierarchyPerRepository = new ConcurrentHashMap<String, Map<String,List<String>>>();

	//First Key is repository id
	//Second Key is definition QNamr
	private ConcurrentMap<String,Map<QName, String>> xmlSchemaDefinitionURLPerQNamePerRepository = new ConcurrentHashMap<String, Map<QName,String>>();
	
	public void setDefinitionCacheManager(
			DefinitionCacheManager definitionCacheManager) {
		this.definitionCacheManager = definitionCacheManager;
	}

	public ContentObjectTypeDefinition getContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName) throws Exception {
		
		if (StringUtils.isBlank(contentObjectTypeDefinitionName))
			return null;
		
		return (ContentObjectTypeDefinition) getDefinition(ValueType.ContentType, contentObjectTypeDefinitionName);
		
	}

	public Map<String, ContentObjectTypeDefinition> getAllContentObjectTypeDefinitions() throws Exception {
		
		return Collections.unmodifiableMap(getAllDefinitionsForDefinitionTypeFqn(ValueType.ContentType));
	}
	
	public Map<String, ComplexCmsPropertyDefinition> getAllComplexCmsPropertyDefinitions() throws Exception {
		
		return Collections.unmodifiableMap(getAllDefinitionsForDefinitionTypeFqn(ValueType.Complex));
	}

	protected Map getAllDefinitionsForDefinitionTypeFqn(ValueType definitionType)
			throws Exception {
		
		return definitionCacheManager.getPropertyDefinitionsForValueType(definitionType);
		
	}

	public ComplexCmsPropertyDefinition getComplexCmsDefinition(String complexCmsPropertyName) throws Exception {
		if (StringUtils.isBlank(complexCmsPropertyName))
			return null;
		
		return (ComplexCmsPropertyDefinition) getDefinition(ValueType.Complex,complexCmsPropertyName);
	}

	protected CmsDefinition getDefinition(ValueType definitionType, String definitionName)
			throws Exception {
		
		return definitionCacheManager.getPropertyDefinition(definitionType, definitionName); 
		
	}

	public boolean hasComplexTypeDefinition(String complexCmsPropertyDefinitionName) throws Exception {

		return definitionCacheManager.definitionExistsForProperty(ValueType.Complex, complexCmsPropertyDefinitionName);
		
	}

	public void putDefinition(ValueType valueType, String name,
			LocalizableCmsDefinition definition) throws Exception {
		
		if (valueType !=null && name !=null && definition != null){
			
			//Simple debug info
			logger.debug("Adding definition {} to cache", definition.getQualifiedName() + " " + 
					(definition instanceof CmsPropertyDefinition ? ((CmsPropertyDefinition)definition).getFullPath(): ""));
			
			definitionCacheManager.addPropertyDefinition(valueType, name, definition);
		}
		
	}

	

	public void removeRegion() throws Exception {
		definitionCacheManager.clearAllDefinitionsForCurrentRepository();
		
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		if (StringUtils.isNotBlank(currentRepositoyId)){
			topicPropertyPathsPerTaxonomies.remove(currentRepositoyId);
			xmlSchemaDefinitionsPerFilenamePerRepository.remove(currentRepositoyId);
			multivaluePropertiesPerRepository.remove(currentRepositoyId);
			contentTypeHierarchyPerRepository.remove(currentRepositoyId);
			xmlSchemaDefinitionURLPerQNamePerRepository.remove(currentRepositoyId);
		}
	}

	@Override
	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies() {

		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		if (StringUtils.isNotBlank(currentRepositoyId)){
			
			return topicPropertyPathsPerTaxonomies.get(currentRepositoyId);

		}
		
		return new HashMap<String, List<String>>();
	}

	public void putTopicPropertyPathsPerTaxonomy(
			Map<String, List<String>> topicPropertyPathsPerTaxonomiesForCurrentRepository) throws Exception {
		
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && topicPropertyPathsPerTaxonomiesForCurrentRepository != null){
			
			Map<String, List<String>> topicPropertyPathsMap = new HashMap<String, List<String>>(topicPropertyPathsPerTaxonomiesForCurrentRepository);
			
			topicPropertyPathsPerTaxonomies.put(currentRepositoyId, Collections.unmodifiableMap(topicPropertyPathsPerTaxonomiesForCurrentRepository));
			
		
			logger.debug("Set TopicPropertyPaths {} for repository id {}", 
					(topicPropertyPathsMap == null ? " Not created" : 
						topicPropertyPathsMap), currentRepositoyId);
		}
	}
	
	public void printDefinitionCacheToLog(){
		definitionCacheManager.printDefinitionsToLog();
	}

	@Override
	public byte[] getXMLSchemaForDefinitionFilename(String definitionFileName) {
		
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && StringUtils.isNotBlank(definitionFileName) && xmlSchemaDefinitionsPerFilenamePerRepository.containsKey(currentRepositoyId)){
			
			return xmlSchemaDefinitionsPerFilenamePerRepository.get(currentRepositoyId).get(definitionFileName);

		}
		
		return null;
	}
	

	@Override
	public void putXMLSchemaDefinitionsPerFilename(
			Map<String, byte[]> xmlSchemaDefinitionsPerFilename) {
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && xmlSchemaDefinitionsPerFilename != null){
			
			Map<String,byte[]> xmlSchemaDefintionsMap = new HashMap<String,byte[]>(xmlSchemaDefinitionsPerFilename);
			
			xmlSchemaDefinitionsPerFilenamePerRepository.put(currentRepositoyId, Collections.unmodifiableMap(xmlSchemaDefintionsMap));
		
			logger.debug("Set XML Schema Definitions File name map {} for repository id {}",
					xmlSchemaDefinitionsPerFilenamePerRepository.get(currentRepositoyId),
					currentRepositoyId);
			

		}
	}

	@Override
	public void putMultivalueProperties(List<String> multivalueProperties) {
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && multivalueProperties != null){
			
			multivaluePropertiesPerRepository.put(currentRepositoyId, Collections.unmodifiableList(new ArrayList<String>(multivalueProperties)));
		
			logger.debug("Set Multi value Properties {} for repository id {}",
					multivaluePropertiesPerRepository.get(currentRepositoyId),
					currentRepositoyId);
			

		}
		
	}

	@Override
	public List<String> getMultivalueProperties() {
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		if (StringUtils.isNotBlank(currentRepositoyId)){
			
			return multivaluePropertiesPerRepository.get(currentRepositoyId);
			
		}
		
		return null;

	}

	@Override
	public Map<String, List<String>> getContentTypeHierarchy() {
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		if (StringUtils.isNotBlank(currentRepositoyId) && contentTypeHierarchyPerRepository.containsKey(currentRepositoyId)){
			
			return contentTypeHierarchyPerRepository.get(currentRepositoyId);
			
		}
		
		return new HashMap<String, List<String>>();
	}

	@Override
	public void putContentTypeHierarchy(
			Map<String, List<String>> contentTypeHierarchy) {
		
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId)  && contentTypeHierarchy != null){
			
			Map<String,List<String>> contentTypeHierarchyMap = new HashMap<String,List<String>>(contentTypeHierarchy);
			
			contentTypeHierarchyPerRepository.put(currentRepositoyId, Collections.unmodifiableMap(contentTypeHierarchyMap));
		
			logger.debug("Set Content Type Hierarchy {} for repository id {}.", contentTypeHierarchyPerRepository.get(currentRepositoyId));
		}
		
	}

	@Override
	public void clearCacheForRepository(String repositoryIdToBeUndeployed) {
		
		if (StringUtils.isNotBlank(repositoryIdToBeUndeployed)){
			
			definitionCacheManager.clearAllDefinitionsForRepository(repositoryIdToBeUndeployed);
		
			if (StringUtils.isNotBlank(repositoryIdToBeUndeployed)){
				topicPropertyPathsPerTaxonomies.remove(repositoryIdToBeUndeployed);
				xmlSchemaDefinitionsPerFilenamePerRepository.remove(repositoryIdToBeUndeployed);
				contentTypeHierarchyPerRepository.remove(repositoryIdToBeUndeployed);
				multivaluePropertiesPerRepository.remove(repositoryIdToBeUndeployed);
				xmlSchemaDefinitionsPerFilenamePerRepository.remove(repositoryIdToBeUndeployed);
			}
		}
		
	}


	@Override
	public Map<QName, String> getLocationURLForDefinitions() {
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && xmlSchemaDefinitionURLPerQNamePerRepository.containsKey(currentRepositoyId)){
			
			return xmlSchemaDefinitionURLPerQNamePerRepository.get(currentRepositoyId);
		}
		
		return null;

	}

	@Override
	public void putLocationURLForDefinition(Map<QName,String> definitionSchemaURLPerQName) {
		
		String currentRepositoyId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoyId) && definitionSchemaURLPerQName != null){
			
			ConcurrentMap<QName,String> xmlSchemaDefintionURLMap = new ConcurrentHashMap<QName,String>(definitionSchemaURLPerQName);
			
			xmlSchemaDefinitionURLPerQNamePerRepository.put(currentRepositoyId, Collections.unmodifiableMap(xmlSchemaDefintionURLMap));
		
			logger.debug("Set XML Schema Definitions File name map {} for repository id {}",
					xmlSchemaDefinitionURLPerQNamePerRepository.get(currentRepositoyId),
					currentRepositoyId);
			

		}		
	}
}
