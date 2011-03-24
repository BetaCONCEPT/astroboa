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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;

/**
 * Represents cache region where definitions for content object types is stored.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface DefinitionCacheRegion {

	ContentObjectTypeDefinition getContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName) throws Exception ;

	Map<String, ContentObjectTypeDefinition> getAllContentObjectTypeDefinitions() throws Exception;
	
	Map<String, ComplexCmsPropertyDefinition> getAllComplexCmsPropertyDefinitions() throws Exception;

	ComplexCmsPropertyDefinition getComplexCmsDefinition(String complexCmsPropertyName) throws Exception ;

	boolean hasComplexTypeDefinition(String complexCmsPropertyDefinitionName) throws Exception ;
	
	void putDefinition(ValueType valueType, String name,	LocalizableCmsDefinition definition) throws Exception ;

	public void removeRegion() throws Exception ;

	Map<String, List<String>> getTopicPropertyPathsPerTaxonomies();
	
	void putTopicPropertyPathsPerTaxonomy(
			Map<String, List<String>> topicPropertyPathsPerTaxonomies) throws Exception ;

	void printDefinitionCacheToLog();
	
	byte[] getXMLSchemaForDefinitionFilename(String definitionFileName);
	
	void putXMLSchemaDefinitionsPerFilename(Map<String, byte[]> xmlSchemaDefinitionsPerFilename);

	void putMultivalueProperties(List<String> multivalueProperties);
	
	List<String> getMultivalueProperties();

	void putContentTypeHierarchy(Map<String, List<String>> multivalueProperties);
	
	Map<String, List<String>> getContentTypeHierarchy();

	void clearCacheForRepository(String repositoyIdToBeUndeployed);

	void putLocationURLForDefinition(Map<QName,String> definitionSchemaURLsPerQName);
	
	Map<QName, String> getLocationURLForDefinitions();

}
