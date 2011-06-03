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

import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportedEntity{
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private String name;
	
	private Object entity;
	
	private String locale;
	
	private DatatypeFactory df ;
	
	private boolean entityRepresentsAContentObjectReference;
	
	public ImportedEntity(String name, Object entity, DatatypeFactory df, boolean entityRepresentsAContentObjectReference) {
		this.name = name;
		this.entity = entity;
		this.df = df;
		this.entityRepresentsAContentObjectReference = entityRepresentsAContentObjectReference;
	}

	public String getName() {
		return name;
	}

	public Object getEntity() {
		return entity;
	}

	public boolean hasOwnerElement() {
		return entity != null && ( 
				entity instanceof ContentObject ||
				entity instanceof Topic ||
				entity instanceof Space
	);
	}

	public boolean addAttribute(String attributeName, String attributeValue) {
		
		if (entity == null){
			logger.warn("Attribute {} and its value {} are not imported because no entity exists", name, attributeValue);
		}
		
		if (StringUtils.isEmpty(attributeName)){
			logger.warn("Attribute value {} provided with no attribute name for element {}", attributeValue, name);
			return false;
		}
		
		//We could use reflection
		if (StringUtils.equals(attributeName, CmsBuiltInItem.CmsIdentifier.getLocalPart())){
			if (entity instanceof CmsRepositoryEntity){
				((CmsRepositoryEntity)entity).setId(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.URL_ATTRIBUTE_NAME)){
			if (entity instanceof BinaryChannel){
				((BinaryChannelImpl)entity).setExternalLocationOfTheContent(attributeValue);
			}
			//Attribute URL is ignored
			return true;
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.Name.getLocalPart())){
			
			if (entity instanceof Taxonomy){
				((Taxonomy)entity).setName(attributeValue);
				return true;
			}
			else if (entity instanceof Topic){
				((Topic)entity).setName(attributeValue);
				return true;
			}
			else if (entity instanceof Space){
				((Space)entity).setName(attributeValue);
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsConstants.LANG_ATTRIBUTE_NAME) ||
				StringUtils.equals(attributeName, CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX)){
			
			locale = attributeValue;
			
			return true;
			
		}
		else if (CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME.equals(attributeName)){
			
			try{
				if (entity instanceof Taxonomy){
					((Taxonomy)entity).setNumberOfRootTopics(Integer.valueOf(attributeValue));
					return true;
				}
				else if (entity instanceof Topic){
					((Topic)entity).setNumberOfChildren(Integer.valueOf(attributeValue));
					return true;
				}
				else if (entity instanceof Space){
					((Space)entity).setNumberOfChildren(Integer.valueOf(attributeValue));
					return true;
				}
			}
			catch(Exception e){
				logger.error("",e);
				return false;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.MimeType.getLocalPart())){
			if (entity instanceof BinaryChannel){
				((BinaryChannel)entity).setMimeType(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.Encoding.getLocalPart())){
			if (entity instanceof BinaryChannel){
				((BinaryChannel)entity).setEncoding(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.SourceFileName.getLocalPart())){
			if (entity instanceof BinaryChannel){
				((BinaryChannel)entity).setSourceFilename(attributeValue);
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsConstants.LAST_MODIFICATION_DATE_ATTRIBUTE_NAME)){
			if (entity instanceof BinaryChannel){
				((BinaryChannel)entity).setModified(df.newXMLGregorianCalendar(attributeValue).toGregorianCalendar());
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.AllowsReferrerContentObjects.getLocalPart())){
			
			if (entity instanceof Topic){
				((Topic)entity).setAllowsReferrerContentObjects(BooleanUtils.isTrue(Boolean.valueOf(attributeValue)));
				return true;
			}

		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.Order.getLocalPart())){
			if (entity instanceof Topic){
				((Topic)entity).setOrder(Long.valueOf(attributeValue));
				return true;
			}
			else if (entity instanceof Space){
				((Space)entity).setOrder(Long.valueOf(attributeValue));
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.ExternalId.getLocalPart())){

			if (entity instanceof RepositoryUser){
				((RepositoryUser)entity).setExternalId(attributeValue);
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.Label.getLocalPart())){
			if (entity instanceof RepositoryUser){
				((RepositoryUser)entity).setLabel(attributeValue);
				return true;
			}
			
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.SystemName.getLocalPart())){
			if (entity instanceof ContentObject){
				((ContentObject)entity).setSystemName(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.ContentObjectTypeName.getLocalPart())){
			if (entity instanceof ContentObject){
				((ContentObject)entity).setContentObjectType(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsBuiltInItem.SystemBuiltinEntity.getLocalPart())){
			if (entity instanceof CmsRepositoryEntity){
				((CmsRepositoryEntity)entity).setSystemBuiltinEntity(BooleanUtils.isTrue(Boolean.valueOf(attributeValue)));
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.REPOSITORY_ID_ATTRIBUTE_NAME)){
			if (entity instanceof Repository){
				((Repository)entity).setId(attributeValue);
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.REPOSITORY_SERIALIZATION_CREATION_DATE_ATTRIBUTE_NAME)){
			if (entity instanceof Repository){
				((Repository)entity).setCreated(df.newXMLGregorianCalendar(attributeValue).toGregorianCalendar());
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.OFFSET)){
			if (entity instanceof List){
				//Ignore this attribute. Its about resource collection
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.LIMIT)){
			if (entity instanceof List){
				//Ignore this attribute. Its about resource collection
				return true;
			}
		}
		else if (StringUtils.equals(attributeName, CmsConstants.TOTAL_RESOURCE_COUNT)){
			if (entity instanceof List){
				//Ignore this attribute. Its about resource collection
				return true;
			}
		}
		
		return false;
		
	}

	public void addLocalizedLabel(String locale, String localizedLabel) {
		if (locale != null) {
			if (entity instanceof Localization){
				((Localization)entity).addLocalizedLabel(locale, localizedLabel);
			}
			else{
				logger.warn("Cannot add localized label {} for locale {} to entity {}", 
						new Object[]{localizedLabel, locale, entity.toString()});
			}
		}
	}
	
	public void addLocalizedLabel(String localizedLabel) {
		//A localized label has ended. Add locale and label to localization entity
		if (locale != null){
			
			if (entity instanceof Localization){
				((Localization)entity).addLocalizedLabel(locale, localizedLabel);
			}
			else{
				logger.warn("Cannot add localized label {} for locale {} to entity {}", 
						new Object[]{localizedLabel, locale, entity.toString()});
			}

			locale = null;
		}
		else{
			logger.debug("Localized label {} does not have a corresponding locale", localizedLabel);
		}
		
	}

	public void completeEntityImport(String content) {
		
		if (entity instanceof CmsProperty){
		
			final CmsProperty cmsProperty = (CmsProperty)entity;
			
			switch (cmsProperty.getValueType()) {
			case Boolean:
				
				if (content != null){
					((BooleanProperty)cmsProperty).addSimpleTypeValue(Boolean.valueOf(content));
				}

				break;
			case Date:

				if (content != null){
					((CalendarProperty)cmsProperty).addSimpleTypeValue(df.newXMLGregorianCalendar(content).toGregorianCalendar());
				}

				break;
			case Double:

				if (content != null){
					((DoubleProperty)cmsProperty).addSimpleTypeValue(Double.valueOf(content));
				}

				break;
			case Long:

				if (content != null){
					((LongProperty)cmsProperty).addSimpleTypeValue(Long.valueOf(content));
				}

				break;
			case String:

				if (content != null){
					((StringProperty)cmsProperty).addSimpleTypeValue(content);
				}
		
				break;
			default:
				break;
			
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImportedEntity [");
		if (entity != null) {
			builder.append("entity=");
			builder.append(entity);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
		}
		builder.append("]");
		return builder.toString();
	}
	
	
	public boolean representsAReferenceToAContentObject(){
		return entityRepresentsAContentObjectReference;
	}
	
}

