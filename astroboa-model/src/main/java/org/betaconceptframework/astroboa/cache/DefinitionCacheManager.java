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

package org.betaconceptframework.astroboa.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionCacheManager {

	private final Logger logger = LoggerFactory.getLogger(DefinitionCacheManager.class);
	
	//This map holds definitions for all repositories
	//First key is repository id
	//Second Key is ValueType of Definition
	//Third key is property name
	private Map<String, Map<ValueType, Map<String, CmsDefinition>>> definitionCache = 
		new ConcurrentHashMap<String, Map<ValueType, Map<String,CmsDefinition>>>();

	public void clearAllDefinitionsForCurrentRepository() {
		clearAllDefinitionsForRepository(AstroboaClientContextHolder.getActiveRepositoryId());
	}

	public CmsDefinition getPropertyDefinition(ValueType valueType, String propertyName) {

		Map<String, CmsDefinition> map = getPropertyDefinitionsForValueType(valueType);

		if (map != null)
			return map.get(propertyName);

		return null;
	}

	public Map<String, CmsDefinition> getPropertyDefinitionsForValueType(ValueType valueType) {
		
		String currentRepositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoryId) && definitionCache.containsKey(currentRepositoryId)){
			
			Map<ValueType, Map<String, CmsDefinition>> currentRepositoryCache = definitionCache.get(currentRepositoryId);
			
			if (!currentRepositoryCache.containsKey(valueType))
				return null;

			return currentRepositoryCache.get(valueType);
		}
		
		return null;
	}

	public void addPropertyDefinitionsForValueType(ValueType valueType, Map<String, CmsDefinition> propertyDefinitions) {

		if (MapUtils.isNotEmpty(propertyDefinitions))
		{
			for (Entry<String,CmsDefinition> propertyDefinition: propertyDefinitions.entrySet())
			{
				addPropertyDefinition(valueType, propertyDefinition.getKey(), propertyDefinition.getValue());
			}
		} 

	}

	public void addPropertyDefinition(ValueType valueType, String propertyName, CmsDefinition propertyDefinition) {
		
		String currentRepositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoryId)){
			
			if (!definitionCache.containsKey(currentRepositoryId)){
				definitionCache.put(currentRepositoryId, new ConcurrentHashMap<ValueType, Map<String,CmsDefinition>>());
			}
			
			Map<ValueType, Map<String, CmsDefinition>> currentRepositoryCache = definitionCache.get(currentRepositoryId);
		
			if (!currentRepositoryCache.containsKey(valueType))
				currentRepositoryCache.put(valueType, new ConcurrentHashMap<String,CmsDefinition>());

			currentRepositoryCache.get(valueType).put(propertyName, propertyDefinition);
		}
	}

	public boolean atLeastOneDefinitionExistsForValueType(ValueType valueType) {

		String currentRepositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoryId)){
			return definitionCache.containsKey(currentRepositoryId) && definitionCache.get(currentRepositoryId).containsKey(valueType);
		}

		return false;
	}

	public boolean definitionExistsForProperty(ValueType valueType, String propertyName) {
		
		String currentRepositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
		
		if (StringUtils.isNotBlank(currentRepositoryId)){
			return definitionCache.containsKey(currentRepositoryId) //Definitions for repository must exist 
				&& definitionCache.get(currentRepositoryId).containsKey(valueType) //Definitions for provided value type must exist 
				&& definitionCache.get(currentRepositoryId).get(valueType).containsKey(propertyName); //Definition for provided property must exist
		}

		return false;
		
	}
	
	public void printDefinitionsToLog(){
		if (logger.isDebugEnabled()){
			
			StringBuilder sb = new StringBuilder();
			
			//Detailed debug info for all definition tree
			Set<Entry<String, Map<ValueType, Map<String, CmsDefinition>>>> definitionsPerRepository = definitionCache.entrySet();
			
			for (Entry<String, Map<ValueType, Map<String, CmsDefinition>>> repositoryDefinitions : definitionsPerRepository){
				sb.append("Definitions for repository ").append(repositoryDefinitions.getKey());
				
				Map<ValueType, Map<String, CmsDefinition>> definitions = repositoryDefinitions.getValue();
				
				for (Entry<ValueType, Map<String, CmsDefinition>> definitionForValueType : definitions.entrySet()){
					if (ValueType.ContentType == definitionForValueType.getKey() || ValueType.Complex == definitionForValueType.getKey()){
						printDefinitions(definitionForValueType.getValue(), 0, sb);
					}
				}
			}
			
			logger.debug("{}", sb.toString());
		}
	}
	
	private void printDefinitions(Map<String, CmsDefinition> cmsPropertyDefinitions, int depth, StringBuilder sb) {
		
		String tabs = generateTabs(depth);
		
		if (MapUtils.isNotEmpty(cmsPropertyDefinitions)){
			for (CmsDefinition def : cmsPropertyDefinitions.values()){
				
				sb.append(tabs).append(def.getName()).append(", instance ").append(System.identityHashCode(def));
				
				if (def instanceof ContentObjectTypeDefinition){
					printPropertyDefinitions(((ContentObjectTypeDefinition)def).getPropertyDefinitions(), depth+1, sb);
				}
				else if (def instanceof ComplexCmsPropertyDefinition){
					printPropertyDefinitions(((ComplexCmsPropertyDefinition)def).getChildCmsPropertyDefinitions(), depth+1, sb);
				}
			}
		}
		
	}
	
		
	private String generateTabs(int depth) {
		StringBuilder tabs = new StringBuilder();

		for (int i=0;i<depth;i++){
			tabs.append("\t");
		}
		
		return tabs.toString();
	}
	
	private void printPropertyDefinitions(Map<String, CmsPropertyDefinition> cmsPropertyDefinitions, int depth, StringBuilder sb) {
		
		String tabs = generateTabs(depth);
		
		if (MapUtils.isNotEmpty(cmsPropertyDefinitions)){
			for (CmsPropertyDefinition def : cmsPropertyDefinitions.values()){
				
				sb.append(tabs).append(def.getName()).append(", instance ").append(System.identityHashCode(def));
				
				if (def instanceof ContentObjectTypeDefinition){
					printPropertyDefinitions(((ContentObjectTypeDefinition)def).getPropertyDefinitions(), depth+1, sb);
				}
				else if (def instanceof ComplexCmsPropertyDefinition){
					if (def.getName().equals(def.getParentDefinition().getName())){
						//Child definition is the same. Detected a recursion
						sb.append(tabs).append("\t Detecting recursion ");
					}
					else{
						printPropertyDefinitions(((ComplexCmsPropertyDefinition)def).getChildCmsPropertyDefinitions(), depth+1, sb);
					}
				}
			}
		}
		
	}

	public void clearAllDefinitionsForRepository(String repositoryId) {
		if (StringUtils.isNotBlank(repositoryId) && definitionCache.containsKey(repositoryId)){
			definitionCache.get(repositoryId).clear();
		}
	}
}
