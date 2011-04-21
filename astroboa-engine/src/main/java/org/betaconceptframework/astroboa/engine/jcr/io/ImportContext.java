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
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportContext {

	private Map<String, CmsRepositoryEntity> cmsRepositoriesEntitiesMap = new HashMap<String, CmsRepositoryEntity>();

	private static Map<Class<?>, List<String>> reservedAttributeNamesPerEntityType = new HashMap<Class<?>, List<String>>();
	
	static {
		reservedAttributeNamesPerEntityType.put(ContentObject.class, 
				Arrays.asList(CmsBuiltInItem.SystemName.getLocalPart(),
						CmsConstants.URL_ATTRIBUTE_NAME,
							  CmsBuiltInItem.ContentObjectTypeName.getLocalPart(),
							  CmsBuiltInItem.CmsIdentifier.getLocalPart(), 
							  CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(ComplexCmsProperty.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(Topic.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart(),
						CmsConstants.URL_ATTRIBUTE_NAME,
						CmsConstants.LANG_ATTRIBUTE_NAME,
						      CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX,
						      CmsBuiltInItem.Name.getLocalPart(),
						      CmsBuiltInItem.AllowsReferrerContentObjects.getLocalPart(),
						      CmsBuiltInItem.Order.getLocalPart(),
						      CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, 
						      CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(Space.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart(),
						CmsConstants.URL_ATTRIBUTE_NAME,
						CmsConstants.LANG_ATTRIBUTE_NAME,
						      CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX,
						      CmsBuiltInItem.Name.getLocalPart(),
						      CmsBuiltInItem.Order.getLocalPart(),
						      CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, 
						      CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(Taxonomy.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart(),
						CmsConstants.URL_ATTRIBUTE_NAME,
						CmsConstants.LANG_ATTRIBUTE_NAME,
						      CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX,
						      CmsBuiltInItem.Name.getLocalPart(),
						      CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME,
						      CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(BinaryChannel.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart(),
						CmsBuiltInItem.Name.getLocalPart(),
						CmsConstants.URL_ATTRIBUTE_NAME,
						CmsBuiltInItem.MimeType.getLocalPart(),
						CmsBuiltInItem.Encoding.getLocalPart(),
						CmsBuiltInItem.SourceFileName.getLocalPart(),
						CmsConstants.LAST_MODIFICATION_DATE_ATTRIBUTE_NAME,
						CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(RepositoryUser.class, 
				Arrays.asList(CmsBuiltInItem.CmsIdentifier.getLocalPart(),
						CmsBuiltInItem.ExternalId.getLocalPart(), 
						CmsBuiltInItem.Label.getLocalPart(),
						CmsBuiltInItem.SystemBuiltinEntity.getLocalPart()));

		reservedAttributeNamesPerEntityType.put(Repository.class, 
				Arrays.asList(CmsConstants.REPOSITORY_ID_ATTRIBUTE_NAME,
						CmsConstants.REPOSITORY_SERIALIZATION_CREATION_DATE_ATTRIBUTE_NAME));

	}
		
	public void cacheEntity(String cacheKey, CmsRepositoryEntity  cmsRepositoryEntity){
		cmsRepositoriesEntitiesMap.put(cacheKey, cmsRepositoryEntity);
	}
	
	public void cacheEntity(CmsRepositoryEntity  cmsRepositoryEntity){
		
		if ( ! (cmsRepositoryEntity instanceof BinaryChannel) && 
				! (cmsRepositoryEntity instanceof CmsProperty)){
			
			if (cmsRepositoryEntity.getId() != null){
				cacheEntity(cmsRepositoryEntity.getId(), cmsRepositoryEntity);
			}
			else{
				//If entity is of type Topic or Taxonomy or Space or ContentObject
				//or RepositoryUser then it is safe to use their name (and externalId or systemName)
				//as the key instead of id
				if (cmsRepositoryEntity instanceof Taxonomy){
					final String name = ((Taxonomy)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cacheEntity(((Taxonomy)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof ContentObject){
					final String systemName = ((ContentObject)cmsRepositoryEntity).getSystemName();
					if (systemName != null && ! isEntityCached(systemName)){
						cacheEntity(((ContentObject)cmsRepositoryEntity).getSystemName(), cmsRepositoryEntity);
					}
				}
				//TopicNames are not unique, therefore we use topic name and
				//its taxonomy name (if any) as a key
				else if (cmsRepositoryEntity instanceof Topic){
					final String name = ((Topic)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cacheEntity(name, cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof Space){
					final String name = ((Space)cmsRepositoryEntity).getName();
					if (name != null && ! isEntityCached(name)){
						cacheEntity(((Space)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof RepositoryUser){
					final String externalId = ((RepositoryUser)cmsRepositoryEntity).getExternalId();
					if (externalId != null && ! isEntityCached(externalId)){
						cacheEntity(((RepositoryUser)cmsRepositoryEntity).getExternalId(), cmsRepositoryEntity);
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

	public boolean nameCorrespondsToAnAttribute(String elementName, Object parentEntity){
		
		if (StringUtils.isBlank(elementName) || parentEntity == null){
			return false;
		}
		
		if (parentEntity instanceof ContentObject){
			return reservedAttributeNamesPerEntityType.get(ContentObject.class).contains(elementName);
		}
		else if (parentEntity instanceof ComplexCmsProperty){
			return reservedAttributeNamesPerEntityType.get(ComplexCmsProperty.class).contains(elementName);
		}
		else if (parentEntity instanceof Topic){
			return reservedAttributeNamesPerEntityType.get(Topic.class).contains(elementName);
		}
		else if (parentEntity instanceof Taxonomy){
			return reservedAttributeNamesPerEntityType.get(Taxonomy.class).contains(elementName);
		}
		else if (parentEntity instanceof Space){
			return reservedAttributeNamesPerEntityType.get(Space.class).contains(elementName);
		}
		else if (parentEntity instanceof BinaryChannel){
			return reservedAttributeNamesPerEntityType.get(BinaryChannel.class).contains(elementName);
		}
		else if (parentEntity instanceof RepositoryUser){
			return reservedAttributeNamesPerEntityType.get(RepositoryUser.class).contains(elementName);
		}
		else if (parentEntity instanceof Repository){
			return reservedAttributeNamesPerEntityType.get(Repository.class).contains(elementName);
		}

		return false;
	}
	
}
