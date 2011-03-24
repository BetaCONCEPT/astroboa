/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportContext {

	private Map<String, CmsRepositoryEntity> cmsRepositoriesEntitiesMap = new HashMap<String, CmsRepositoryEntity>();

	private List<String> attributes = Arrays.asList(
			CmsBuiltInItem.CmsIdentifier.getLocalPart(), 
			CmsConstants.URL_ATTRIBUTE_NAME,
			CmsConstants.LANG_ATTRIBUTE_NAME,
			CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX,
			CmsBuiltInItem.Name.getLocalPart(),
			CmsBuiltInItem.SystemName.getLocalPart(),
			CmsBuiltInItem.ContentObjectTypeName.getLocalPart(),
			CmsBuiltInItem.MimeType.getLocalPart(),
			CmsBuiltInItem.Encoding.getLocalPart(),
			CmsBuiltInItem.SourceFileName.getLocalPart(),
			CmsConstants.LAST_MODIFICATION_DATE_ATTRIBUTE_NAME,
			CmsBuiltInItem.ExternalId.getLocalPart(), 
			CmsBuiltInItem.Label.getLocalPart(),
			CmsBuiltInItem.Order.getLocalPart(),
			CmsBuiltInItem.AllowsReferrerContentObjects.getLocalPart(),
			CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME,
			CmsBuiltInItem.SystemBuiltinEntity.getLocalPart(),
			CmsConstants.REPOSITORY_ID_ATTRIBUTE_NAME,
			CmsConstants.TOTAL_RESOURCE_COUNT,
			CmsConstants.OFFSET,
			CmsConstants.LIMIT
			);

	
	public void cacheEntity(String cacheKey, CmsRepositoryEntity  cmsRepositoryEntity){
		cmsRepositoriesEntitiesMap.put(cacheKey, cmsRepositoryEntity);
	}
	
	public void cacheEntity(CmsRepositoryEntity  cmsRepositoryEntity){
		
		if ( ! (cmsRepositoryEntity instanceof BinaryChannel) && 
				! (cmsRepositoryEntity instanceof CmsProperty)){
			
			if (cmsRepositoryEntity.getId() != null){
				cmsRepositoriesEntitiesMap.put(cmsRepositoryEntity.getId(), cmsRepositoryEntity);
			}
			else{
				//If entity is of type Topic or Taxonomy or Space or ContentObject
				//or RepositoryUser then it is safe to use their name (and externalId or systemName)
				//as the key instead of id
				if (cmsRepositoryEntity instanceof Taxonomy){
					final String name = ((Taxonomy)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cmsRepositoriesEntitiesMap.put(((Taxonomy)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof ContentObject){
					final String systemName = ((ContentObject)cmsRepositoryEntity).getSystemName();
					if (systemName != null && ! isEntityCached(systemName)){
						cmsRepositoriesEntitiesMap.put(((ContentObject)cmsRepositoryEntity).getSystemName(), cmsRepositoryEntity);
					}
				}
				//TopicNames are not unique, therefore we use topic name and
				//its taxonomy name (if any) as a key
				else if (cmsRepositoryEntity instanceof Topic){
					final String name = ((Topic)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cmsRepositoriesEntitiesMap.put(name, cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof Space){
					final String name = ((Space)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cmsRepositoriesEntitiesMap.put(((Space)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof RepositoryUser){
					final String externalId = ((RepositoryUser)cmsRepositoryEntity).getExternalId();
					if (externalId != null && ! isEntityCached(externalId)){
						cmsRepositoriesEntitiesMap.put(((RepositoryUser)cmsRepositoryEntity).getExternalId(), cmsRepositoryEntity);
					}
				}
				
			}
		}
	}
	
	public boolean isEntityCached(String identifier){
		return identifier != null && cmsRepositoriesEntitiesMap.containsKey(identifier);
	}
	
	public CmsRepositoryEntity getEntityFromCache(String identifier){
		if (identifier != null){
			return cmsRepositoriesEntitiesMap.get(identifier);
		}
		
		return null;
	}

	public void dispose() {
		cmsRepositoriesEntitiesMap.clear();
	}

	public boolean nameCorrespondsToAnAttribute(String elementName){
		
		if (StringUtils.isBlank(elementName)){
			return false;
		}
		
		return attributes.contains(elementName);
			
	}
	
}
