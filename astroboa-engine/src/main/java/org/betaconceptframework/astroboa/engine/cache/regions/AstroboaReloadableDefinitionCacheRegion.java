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

package org.betaconceptframework.astroboa.engine.cache.regions;

import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.cache.region.AstroboaDefinitionCacheRegion;
import org.betaconceptframework.astroboa.engine.definition.ContentDefinitionConfiguration;
import org.jboss.cache.CacheException;

/**
 * Definition cache using its own CacheManager. Extends AstroboaDefinitionCacheRegion
 * with the ability to reload definitions if their contents have changed
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaReloadableDefinitionCacheRegion extends AstroboaDefinitionCacheRegion{

	private ContentDefinitionConfiguration contentDefinitionConfiguration;
	
	public void setContentDefinitionConfiguration(
			ContentDefinitionConfiguration contentContentDefinitionConfiguration) {
		this.contentDefinitionConfiguration = contentContentDefinitionConfiguration;
	}

	
	@Override
	protected Map getAllDefinitionsForDefinitionTypeFqn(ValueType definitionType)
			throws Exception, CacheException {
		
		contentDefinitionConfiguration.loadDefinitionToCache();
		
		return super.getAllDefinitionsForDefinitionTypeFqn(definitionType);
		
	}

	@Override
	protected CmsDefinition getDefinition(ValueType definitionType, String definitionName)
			throws Exception {
		
		contentDefinitionConfiguration.loadDefinitionToCache();
		
		return super.getDefinition(definitionType, definitionName); 
		
	}

	@Override
	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies() {
		
		try {
			contentDefinitionConfiguration.loadDefinitionToCache();
		} catch (Exception e) {
			throw new CmsException(e);
		}
		
		return super.getTopicPropertyPathsPerTaxonomies();
	}


	@Override
	public byte[] getXMLSchemaForDefinitionFilename(String definitionFileName) {
		
		try {
			contentDefinitionConfiguration.loadDefinitionToCache();
			
			return super.getXMLSchemaForDefinitionFilename(definitionFileName);
		} catch (Exception e) {
			throw new CmsException(e);
		}
	}


	@Override
	public List<String> getMultivalueProperties() {
		
		try {
			contentDefinitionConfiguration.loadDefinitionToCache();
		} catch (Exception e) {
			throw new CmsException(e);
		}
		
		return super.getMultivalueProperties();
	}


	@Override
	public Map<String, List<String>> getContentTypeHierarchy() {
		
		try {
			contentDefinitionConfiguration.loadDefinitionToCache();
		} catch (Exception e) {
			throw new CmsException(e);
		}
		
		return super.getContentTypeHierarchy();
	}
	
}
