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
package org.betaconceptframework.astroboa.model.jaxb.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.jaxb.AstroboaMarshaller;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.betaconceptframework.astroboa.model.jaxb.MarshalUtils;
import org.betaconceptframework.astroboa.model.jaxb.adapter.BinaryChannelAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.ContentObjectAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.SpaceAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.TopicAdapter;
import org.betaconceptframework.astroboa.model.jaxb.type.BinaryChannelType;
import org.betaconceptframework.astroboa.model.jaxb.type.CmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.CmsPropertyTypeJAXBElement;
import org.betaconceptframework.astroboa.model.jaxb.type.ComplexCmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.ContentObjectType;
import org.betaconceptframework.astroboa.model.jaxb.type.SimpleCmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.SpaceType;
import org.betaconceptframework.astroboa.model.jaxb.type.TopicType;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Responsible to visit all content object properties
 * and provide corresponding JAXB Elements
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectMarshalVisitor extends AbstractCmsPropertyDefinitionVisitor{

	final Logger logger = LoggerFactory.getLogger(getClass());

	protected ContentObjectMarshalContext contentObjectMarshalContext;
	
	private DatatypeFactory df ;

	protected Map<QName, String> xsiSchemaLocationMap;

	protected Marshaller marshaller;

	//When a recursion is detected then 
	//it counts how many times recursion is happening
	private int recursionLevelCount;
	
	private List<String> cmsPropertyPathsToMarshall = null;
	
	protected Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();
	
	protected Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions = new HashMap<String, ContentObjectTypeDefinition>();

	private boolean marshallBinaryContent;
	
	private boolean marshallAllProperties;
	
	public ContentObjectMarshalVisitor(){
		setVisitType(VisitType.Self);
	}
	
	public void setCachedCmsRepositoryEntities(
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) {
		
		if (cachedCmsRepositoryEntities!= null){
			this.cachedCmsRepositoryEntities.putAll(cachedCmsRepositoryEntities);
		}
	}

	public void initialize(ContentObject contentObject, ContentObjectType contentObjectType, 
			Marshaller marshaller, boolean marshallBinaryContent,boolean marshallAllProperties){
		
		this.marshaller = marshaller;

		contentObjectType = populateContentObjectType(contentObject, contentObjectType);
		
		initialize(contentObjectType, marshallBinaryContent,marshallAllProperties);
		
		contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().setCmsProperty(contentObject.getComplexCmsRootProperty());
	}
	
	public void initialize(ContentObjectType contentObjectType, boolean marshallBinaryContent,boolean marshallAllProperties) {

		logger.debug("Initializing marshalling visitor.");
		
		this.marshallBinaryContent = marshallBinaryContent;
		this.marshallAllProperties = marshallAllProperties;

		if (this.xsiSchemaLocationMap == null){
			this.xsiSchemaLocationMap = new HashMap<QName, String>();
		}
		
		logger.debug("Current schema locations {}", this.xsiSchemaLocationMap);

		
		try {
			this.cmsPropertyPathsToMarshall = (List<String>) marshaller.getProperty(AstroboaMarshaller.CMS_PROPERTIES_TO_BE_MARSHALLED);
		} catch (PropertyException e1) {
			throw new CmsException(e1);
		}

		if (df == null){
			try {
				df = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new CmsException(e);
			}
		}

		contentObjectMarshalContext = 
			new ContentObjectMarshalContext(contentObjectType);
		
		contentObjectMarshalContext.addComplexCmsPropertyInfoToQueue(createRootComplexCmsPropertyInfo());
		
	}
	
	private ContentObjectType populateContentObjectType(ContentObject contentObject, ContentObjectType contentObjectType){
		contentObjectType.setId(contentObject.getId());
		contentObjectType.setContentObjectTypeName(contentObject.getContentObjectType());
		contentObjectType.setSystemName(contentObject.getSystemName());
		
		//TODO: Check whether user may have more control on whether a friendly url is generated or not
		contentObjectType.setUrl(contentObject.getResourceApiURL(marshalOutputTypeIsJSON() ? ResourceRepresentationType.JSON : ResourceRepresentationType.XML, false, contentObject.getSystemName()!=null));
		
		if (cmsPropertyPathsToMarshall == null || cmsPropertyPathsToMarshall.contains(CmsConstants.OWNER_ELEMENT_NAME)){
			contentObjectType.setOwner(contentObject.getOwner());
		}
		
		return contentObjectType;
	}

	private boolean marshalOutputTypeIsJSON() {
		return marshaller instanceof AstroboaMarshaller && ((AstroboaMarshaller)marshaller).outputTypeIsJSON();
	}
	
	private CmsPropertyInfo createRootComplexCmsPropertyInfo(){
		CmsPropertyInfo rootComplexPropertyInfo = new CmsPropertyInfo();
		rootComplexPropertyInfo.setId(contentObjectMarshalContext.getContentObjectType().getId());
		rootComplexPropertyInfo.setFullPath(contentObjectMarshalContext.getContentObjectType().getContentObjectTypeName());
		rootComplexPropertyInfo.setName(contentObjectMarshalContext.getContentObjectType().getContentObjectTypeName());
		
		
		return rootComplexPropertyInfo;

	}

	@Override
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {

		if (! CollectionUtils.isEmpty(cmsPropertyPathsToMarshall)){
			//Marshal owner only if full content object is marshaled
			contentObjectMarshalContext.getContentObjectType().setOwner(null);
		}

		if (contentObjectTypeDefinition != null){

			xsiSchemaLocationMap.put(contentObjectTypeDefinition.getQualifiedName(), 
					contentObjectTypeDefinition.url(ResourceRepresentationType.XSD));
		
			if (contentObjectTypeDefinition.hasCmsPropertyDefinitions()){
				for (CmsPropertyDefinition propertyDefinition: contentObjectTypeDefinition.getPropertyDefinitions().values()){
					//if (complexCmsPropertyHasChildProperty(contentObjectMarshalContext.getFirstComplexCmsPropertyInfo(), propertyDefinition.getName())){
					if (shouldMarshallDefinition(propertyDefinition)){
						propertyDefinition.accept(this);
					}
				}
			}
		}

	}

	@Override
	public void visitComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {

		if (complexPropertyDefinition != null){

			long start = System.currentTimeMillis();
			
			if (! shouldMarshallDefinition(complexPropertyDefinition)){
				return;
			}
			
			//Check recursion
			if (recursionLoopDetected(complexPropertyDefinition)){
				return;
			}
			

			if (aspectsAreVisited()){

				//Provide schemaLocation information only for aspect properties
				xsiSchemaLocationMap.put(complexPropertyDefinition.getQualifiedName(), 
						complexPropertyDefinition.url(ResourceRepresentationType.XSD));
			}

			if (propertyHasBeenMarkedForRemoval(complexPropertyDefinition)){
				marshallNullValueForComplexProperty(complexPropertyDefinition);
				return ;
			}

			List<CmsPropertyInfo> complexCmsPropertyInfos = loadComplexCmsPropertyInfos(complexPropertyDefinition);

			if (CollectionUtils.isNotEmpty(complexCmsPropertyInfos)){
				for (CmsPropertyInfo complexCmsPropertyInfo : complexCmsPropertyInfos){

					if (logger.isDebugEnabled()){
						logger.debug("\t Marshalling complex cms property {}", complexCmsPropertyInfo.getFullPath());
						logger.debug("\t Current context {}", contentObjectMarshalContext.getContextInfo());
					}

					CmsPropertyTypeJAXBElement<ComplexCmsPropertyType> complexCmsPropertyTypeJAXBElement = new CmsPropertyTypeJAXBElement(
							//If aspects are marshaled then we need namespace and local part info.
							//Otherwise only local part info is adequate for the element
							aspectsAreVisited() ? complexPropertyDefinition.getQualifiedName():
								new QName(complexPropertyDefinition.getQualifiedName().getLocalPart()),
								ComplexCmsPropertyType.class, null, new ComplexCmsPropertyType()
					);
					
					if (marshalOutputTypeIsJSON() && complexPropertyDefinition.isMultiple()){
						complexCmsPropertyTypeJAXBElement.getValue().setExportAsAnArray(true);
					}

					if (((ComplexCmsPropertyDefinitionImpl)complexPropertyDefinition).commonAttributesAreDefined()){
						complexCmsPropertyTypeJAXBElement.getValue().setCmsIdentifier(complexCmsPropertyInfo.getId());
					}

					//When aspects are added to a content object xml
					//we need to define attribute xsi:type in order to give a hint to a potential 
					//validator where to find the type definition for this element.
					//Since JAXB does not automatically generates this attribute if both declared type and
					//instance are of the same type (ComplexCmsPropertyType)
					if (aspectsAreVisited()){
						
						if (logger.isDebugEnabled()){
							logger.debug("Created JAXBElement for complex property {} with qualified name {}",
								complexCmsPropertyInfo.getFullPath(), 
								complexCmsPropertyTypeJAXBElement.getName().getPrefix() + " " +complexCmsPropertyTypeJAXBElement.getName().toString());
						}
						
						complexCmsPropertyTypeJAXBElement.getValue().setXsiType(complexPropertyDefinition.getQualifiedName().getPrefix()+":"+
								complexPropertyDefinition.getQualifiedName().getLocalPart());
					}

					//Add complexCmsPropertyType to its parent
					addJaxbElementToCurrentParentComplexCmsPropertyType(complexCmsPropertyTypeJAXBElement);


					if (complexPropertyDefinition.hasChildCmsPropertyDefinitions()){

						Collection<CmsPropertyDefinition> childPropertyDefinitions = complexPropertyDefinition.getChildCmsPropertyDefinitions().values();

						//Change current parent

						contentObjectMarshalContext.pushComplexCmsPropertyInfo(complexCmsPropertyInfo);
						contentObjectMarshalContext.pushComplexCmsPropertyType(complexCmsPropertyTypeJAXBElement.getValue());
						
						
						//When aspects are visited we are interested only on root elements and not 
						//on their children as it is there where xsi information will be printed
						//Therefore, disable this flag when visiting aspect children
						contentObjectMarshalContext.pushAspectsAreVisited(false);

						//Visit its children
						if (logger.isDebugEnabled()){
							logger.debug("Start marshalling for children of {} for Thread {}", complexPropertyDefinition.getFullPath(), Thread.currentThread().getName());
						}

						for (CmsPropertyDefinition cmsPropertyDefinition : childPropertyDefinitions){
							
							//Visit child definition only if there is a corresponding value
							//if (complexCmsPropertyHasChildProperty(complexCmsPropertyInfo, cmsPropertyDefinition.getName()) || 
								//	(CollectionUtils.isNotEmpty(cmsPropertyPathsToMarshall) && shouldMarshallDefinition(cmsPropertyDefinition))){
							if (shouldMarshallDefinition(cmsPropertyDefinition)){
								cmsPropertyDefinition.accept(this);
							}
						}

						if (logger.isDebugEnabled()){
							logger.debug("Finished marshalling children for {} for Thread {}", complexPropertyDefinition.getFullPath(), Thread.currentThread().getName());
						}

						contentObjectMarshalContext.pollComplexCmsPropertyInfo();
						contentObjectMarshalContext.pollComplexCmsPropertyType();
						contentObjectMarshalContext.pollAspectsVisited();

					}

					//In case complex property is empty and no ID is defined there is no point in showing in xml
					if (StringUtils.isBlank(complexCmsPropertyTypeJAXBElement.getValue().getCmsIdentifier()) && 
							CollectionUtils.isEmpty(complexCmsPropertyTypeJAXBElement.getValue().getCmsProperties())){
						if (contentObjectMarshalContext.getFirstComplexCmsPropertyType() == null){
							contentObjectMarshalContext.getContentObjectType().getCmsProperties().remove(complexCmsPropertyTypeJAXBElement);
						}
						else{
							contentObjectMarshalContext.getFirstComplexCmsPropertyType().getCmsProperties().remove(complexCmsPropertyTypeJAXBElement);
						}
					}
				}
				
				if (logger.isDebugEnabled()){
					logger.debug("Visited complex cms definition {} in {}", complexPropertyDefinition.getPath(), 
						DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-start));
				}

			}

		}
	}

	private void marshallNullValueForComplexProperty(ComplexCmsPropertyDefinition complexPropertyDefinition) {
		
		//Although this is a complex property we use the simple property type in order to generate an emtpy tag
		//which is what we really want
		final SimpleCmsPropertyType simpleCmsPropertyType = new SimpleCmsPropertyType();
		
		CmsPropertyTypeJAXBElement<SimpleCmsPropertyType> simpleCmsPropertyTypeJaxbElement = new CmsPropertyTypeJAXBElement(
				aspectsAreVisited() ? complexPropertyDefinition.getQualifiedName():
					new QName(complexPropertyDefinition.getQualifiedName().getLocalPart()),
				SimpleCmsPropertyType.class, null, simpleCmsPropertyType);
		
		addJaxbElementToCurrentParentComplexCmsPropertyType(simpleCmsPropertyTypeJaxbElement);

	}

	protected List<CmsPropertyInfo> loadComplexCmsPropertyInfos(CmsPropertyDefinition complexPropertyDefinition) {
		
		List<CmsPropertyInfo> complexCmsPropertyInfos = new ArrayList<CmsPropertyInfo>();
		
		//To avoid the case of loading recursive properties,
		//which are new 
		//we check the existence of a property directly on the provided map
		//and not through method getChildPropertyList or getChildProperty
		//which will cause a property to be lazy loaded (if not found in the parent property) 
		//and thus to be created if not found in the repository
		ComplexCmsProperty<?, ?> parentComplexCmsProperty = (ComplexCmsProperty<?, ?>) contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().getCmsProperty();

		if (parentComplexCmsProperty == null){
			throw new CmsException("Found no parent complex cms property for property "+ complexPropertyDefinition.getFullPath());
		}

		List<CmsProperty<?, ?>> complexCmsProperties = parentComplexCmsProperty.getChildProperties().get(complexPropertyDefinition.getName());

		if (CollectionUtils.isEmpty(complexCmsProperties)){
			//No properties exist. 
			//Check if any value exists in the repository
			if (parentComplexCmsProperty.hasValueForChildProperty(complexPropertyDefinition.getName())){
				//Try usual method using getChildProperty or getChildPropertyList
				//methods. 
				if (complexPropertyDefinition.isMultiple()){
					complexCmsProperties = parentComplexCmsProperty.getChildPropertyList(complexPropertyDefinition.getName());
				}
				else{
					CmsProperty<?, ?> complexCmsProperty = parentComplexCmsProperty.getChildProperty(complexPropertyDefinition.getName());
	
					if (complexCmsProperty != null){
						complexCmsProperties = new ArrayList<CmsProperty<?,?>>();
						complexCmsProperties.add(complexCmsProperty);
					}
				}
			}
		}
		
		if (CollectionUtils.isNotEmpty(complexCmsProperties)){
			for (CmsProperty<?,?> complexCmsProperty : complexCmsProperties){

				CmsPropertyInfo complexCmsPropertyInfo = new CmsPropertyInfo();
				complexCmsPropertyInfo.setId(complexCmsProperty.getId());
				complexCmsPropertyInfo.setFullPath(complexCmsProperty.getFullPath());
				complexCmsPropertyInfo.setCmsProperty(complexCmsProperty);
				complexCmsPropertyInfo.setName(complexCmsProperty.getName());
				
				complexCmsPropertyInfos.add(complexCmsPropertyInfo);
			}
		}
		
		return complexCmsPropertyInfos;
	}

	private Boolean aspectsAreVisited() {
		return contentObjectMarshalContext.aspectsAreVisited();
	}

	protected boolean shouldMarshallDefinition(CmsPropertyDefinition cmsPropertyDefinition) {
		
		if (marshallAllProperties){
			return true;
		}
		
		//Marshal Property if its path or its parent path exist in list
		//Get full path in case this definition is about an aspect.
		String propertyPath = cmsPropertyDefinition.getFullPath();
			
		if (propertyPath.startsWith(contentObjectMarshalContext.getContentObjectType().getContentObjectTypeName())){
			propertyPath = StringUtils.substringAfter(propertyPath, CmsConstants.PERIOD_DELIM);
		}
			
		return MarshalUtils.propertyShouldBeMarshalled(cmsPropertyPathsToMarshall, cmsPropertyDefinition.getName(), propertyPath);
			
	}

	private boolean recursionLoopDetected(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {

		if (contentObjectMarshalContext.getFirstComplexCmsPropertyInfo() != null){
			if (StringUtils.equals(contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().getName(), complexPropertyDefinition.getName())){
				recursionLevelCount++;
			}
			else{
				recursionLevelCount = 0;
				return false;
			}
		}

		if (recursionLevelCount > 500){
			logger.warn("Detected recusrion loop. Reached level of 500 for property "+ complexPropertyDefinition.getName());
			return true;
		}


		return false;
	}

	@Override
	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {


		if (simplePropertyDefinition != null){
			
			if (! shouldMarshallDefinition(simplePropertyDefinition)){
				return;
			}
			
			if (propertyHasBeenMarkedForRemoval(simplePropertyDefinition)){
				marshallNullValueForSimpleProperty(simplePropertyDefinition);
				return ;
			}
			
			//First retrieve CmsProperty
			CmsPropertyInfo simpleCmsProperty = getCmsPropertyInfoForChildProperty(simplePropertyDefinition);


			if (simpleCmsProperty == null){
				if (logger.isDebugEnabled()){
					logger.debug("Simple cms property {} does not have a value in repository and therefore will not be marshalled", simplePropertyDefinition.getFullPath());
				}
				return;
			}

			if (logger.isDebugEnabled()){
				logger.debug("\t Marshalling simple cms property {}", simpleCmsProperty.getFullPath());
			}

			List<?> values = simpleCmsProperty.getValues();

			//For each value generate a JAXB element of type SimpleCmsPropertyType
			if (CollectionUtils.isNotEmpty(values)){
				for (Object value : values){
					marshallValueForSimpleProperty(simplePropertyDefinition,
							simpleCmsProperty, value);
				}
			}
		}
	}

	protected void marshallNullValueForSimpleProperty(SimpleCmsPropertyDefinition simpleCmsPropertyDefinition){
		
		final SimpleCmsPropertyType simpleCmsPropertyType = new SimpleCmsPropertyType();
		
		CmsPropertyTypeJAXBElement<SimpleCmsPropertyType> simpleCmsPropertyTypeJaxbElement = new CmsPropertyTypeJAXBElement(
				new QName(simpleCmsPropertyDefinition.getQualifiedName().getLocalPart()),
				SimpleCmsPropertyType.class, null, simpleCmsPropertyType);
		
		addJaxbElementToCurrentParentComplexCmsPropertyType(simpleCmsPropertyTypeJaxbElement);
	}
	
	protected <T> void marshallValueForSimpleProperty(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition,
			CmsPropertyInfo simpleCmsProperty, Object value) {
		
		if (value != null){


			switch (simplePropertyDefinition.getValueType()) {
			case String:
			case Boolean:
			case Double:
			case Long:
			case Date:
				
				final SimpleCmsPropertyType simpleCmsPropertyType = new SimpleCmsPropertyType();
				
				if (marshalOutputTypeIsJSON() && simplePropertyDefinition.isMultiple()){
					simpleCmsPropertyType.setExportAsAnArray(true);
				}
				
				CmsPropertyTypeJAXBElement<SimpleCmsPropertyType> simpleCmsPropertyTypeJaxbElement = new CmsPropertyTypeJAXBElement(
						new QName(simplePropertyDefinition.getQualifiedName().getLocalPart()),
						SimpleCmsPropertyType.class, null, simpleCmsPropertyType);

				
				if (value instanceof String){
					simpleCmsPropertyTypeJaxbElement.getValue().setContent((String)value);
				}
				else if (value instanceof Boolean){
					simpleCmsPropertyTypeJaxbElement.getValue().setContent(((Boolean)value).toString());
				}
				else if (value instanceof Double){
					simpleCmsPropertyTypeJaxbElement.getValue().setContent(((Double)value).toString());
				}
				else if (value instanceof Long){
					simpleCmsPropertyTypeJaxbElement.getValue().setContent(((Long)value).toString());
				}
				else if (value instanceof Calendar){
					Calendar calendar = (Calendar) value;

					try{
						if (((CalendarPropertyDefinition)simplePropertyDefinition).isDateTime()){
							GregorianCalendar gregCalendar = new GregorianCalendar(calendar.getTimeZone());
							gregCalendar.setTimeInMillis(calendar.getTimeInMillis());

							simpleCmsPropertyTypeJaxbElement.getValue().setContent(df.newXMLGregorianCalendar(gregCalendar).toXMLFormat());
						}
						else{
							simpleCmsPropertyTypeJaxbElement.getValue().setContent(df.newXMLGregorianCalendarDate(
									calendar.get( Calendar.YEAR ),
									calendar.get( Calendar.MONTH )+1,  	// Calendar.MONTH is zero based, XSD Date datatype's month field starts
																		//	with JANUARY as 1.
									calendar.get( Calendar.DAY_OF_MONTH ),
									DatatypeConstants.FIELD_UNDEFINED ).toXMLFormat());
						}
					}
					catch(Exception e){
						throw new CmsException("Property "+simpleCmsProperty.getFullPath()+ " Calendar value "+ DateUtils.format(calendar), e);
					}
				}
				else{
					throw new CmsException("Property "+simpleCmsProperty.getFullPath() + " has value type "+ simplePropertyDefinition.getValueType()+ 
							" but contains value of type "+ value.getClass().getName());
				}

				addJaxbElementToCurrentParentComplexCmsPropertyType(simpleCmsPropertyTypeJaxbElement);

				break;
			case TopicReference:

				try {
					TopicType topicType = marshalTopicReference(value);
					
					if (marshalOutputTypeIsJSON() && simplePropertyDefinition.isMultiple()){
						topicType.setExportAsAnArray(true);
					}

					CmsPropertyTypeJAXBElement<TopicType> topicTypeJaxbElement = new CmsPropertyTypeJAXBElement(
							new QName(simplePropertyDefinition.getQualifiedName().getLocalPart()),
							TopicType.class, null, topicType);


					addJaxbElementToCurrentParentComplexCmsPropertyType(topicTypeJaxbElement);

				} catch (Exception e) {
					throw new CmsException("Unable to marshal topic "+ ((Topic)value).getName(), e);
				}


				break;
			case Binary:
				try{
					BinaryChannelType binaryChannelType = getBinaryChannelAdapter().marshal((BinaryChannel)value);

					if (marshalOutputTypeIsJSON() && simplePropertyDefinition.isMultiple()){
						binaryChannelType.setExportAsAnArray(true);
					}

					CmsPropertyTypeJAXBElement<BinaryChannelType> binaryChannelTypeJaxbElement = new CmsPropertyTypeJAXBElement(
							new QName(simplePropertyDefinition.getQualifiedName().getLocalPart()),
							BinaryChannelType.class, null, binaryChannelType);

					addJaxbElementToCurrentParentComplexCmsPropertyType(binaryChannelTypeJaxbElement);

				} catch (Exception e) {
					throw new CmsException("Unable to marshal binary channel "+ ((BinaryChannel)value).getName(), e);
				}


				break;
			case ObjectReference:
				try{
					
					logger.debug("\t Property is a reference to another object");

					Marshaller objectReferenceMarshaller = CmsEntitySerialization.Context.createMarshaller(marshalOutputTypeIsJSON()? ResourceRepresentationType.JSON:ResourceRepresentationType.XML, 
							prettyPrintIsEnabled());

					//For now only porifle.title is provided. 
					objectReferenceMarshaller.setProperty(AstroboaMarshaller.CMS_PROPERTIES_TO_BE_MARSHALLED, Arrays.asList("profile.title"));
					
					ContentObjectAdapter adapter = new ContentObjectAdapter();
					adapter.setMarshaller(objectReferenceMarshaller, marshallBinaryContent,false);
					objectReferenceMarshaller.setAdapter(adapter);

					ContentObjectType contentObjectType = 
						objectReferenceMarshaller.getAdapter(ContentObjectAdapter.class).marshal((ContentObject) value);

					if (marshalOutputTypeIsJSON() && simplePropertyDefinition.isMultiple()){
						contentObjectType.setExportAsAnArray(true);
					}
					
					CmsPropertyTypeJAXBElement<ContentObjectType> contentObjectReferenceTypeJaxbElement = new CmsPropertyTypeJAXBElement(
							new QName(simplePropertyDefinition.getQualifiedName().getLocalPart()),
							ContentObjectType.class, null, contentObjectType);
					
					addJaxbElementToCurrentParentComplexCmsPropertyType(contentObjectReferenceTypeJaxbElement);
					

				} catch (Exception e) {
					throw new CmsException("Unable to marshal contentObject "+ ((ContentObject)value).getId(), e);
				}

				break;
			default:
				break;
			}

		}
	}

	private boolean prettyPrintIsEnabled() throws PropertyException {
		return BooleanUtils.isTrue((Boolean)marshaller.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT));
	}

	private ResourceRepresentationType<?> retrieveOutput() {
		ResourceRepresentationType<?>  resourceRepresentationType = ResourceRepresentationType.XML;
		
		if (marshaller != null && marshaller instanceof AstroboaMarshaller && 
				((AstroboaMarshaller)marshaller).outputTypeIsJSON()){
			resourceRepresentationType = ResourceRepresentationType.JSON;
		}
		return resourceRepresentationType;
	}
	
	protected SpaceType marshalSpaceReference(Object value) throws Exception {
		return getSpaceAdapter().marshal((Space)value, retrieveOutput());
	}

	protected TopicType marshalTopicReference(Object value) throws Exception {
		return getTopicAdapter().marshal((Topic)value, retrieveOutput());
	}

	protected boolean propertyHasBeenMarkedForRemoval(CmsPropertyDefinition cmsPropertyDefinition){
		
		return ((ComplexCmsPropertyImpl)contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().getCmsProperty()).cmsPropertyHasBeenLoadedAndRemoved(cmsPropertyDefinition.getName());
		
	}
	
	protected <T> CmsPropertyInfo getCmsPropertyInfoForChildProperty(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {
		
		if (((ComplexCmsProperty<?,?>)contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().getCmsProperty()).hasValueForChildProperty(simplePropertyDefinition.getName())){

			CmsProperty<?, ?> simpleCmsProperty = ((ComplexCmsProperty<?,?>)contentObjectMarshalContext.getFirstComplexCmsPropertyInfo().getCmsProperty()).getChildProperty(simplePropertyDefinition.getName());

			CmsPropertyInfo cmsPropertyInfo = new CmsPropertyInfo();
			cmsPropertyInfo.setName(simpleCmsProperty.getName());
			cmsPropertyInfo.setFullPath(simpleCmsProperty.getFullPath());
			cmsPropertyInfo.setCmsProperty(simpleCmsProperty);

			//Then retrieve all its values
			if (simplePropertyDefinition.isMultiple()){
				cmsPropertyInfo.getValues().addAll(((SimpleCmsProperty<?, ?, ?>)simpleCmsProperty).getSimpleTypeValues());
			}
			else{
				cmsPropertyInfo.addValue(((SimpleCmsProperty<?, ?, ?>)simpleCmsProperty).getSimpleTypeValue());
			}

			return cmsPropertyInfo;
		}
		
		return null;
	}

	private BinaryChannelAdapter getBinaryChannelAdapter() {
		if (marshaller.getAdapter(BinaryChannelAdapter.class) == null){
			marshaller.setAdapter(new BinaryChannelAdapter(marshallBinaryContent));
		}

		return marshaller.getAdapter(BinaryChannelAdapter.class);
	}

	private TopicAdapter getTopicAdapter() {
		if (marshaller.getAdapter(TopicAdapter.class) == null){
			marshaller.setAdapter(new TopicAdapter());
		}

		return marshaller.getAdapter(TopicAdapter.class);
	}

	private SpaceAdapter getSpaceAdapter() {
		if (marshaller.getAdapter(SpaceAdapter.class) == null){
			marshaller.setAdapter(new SpaceAdapter());
		}

		return marshaller.getAdapter(SpaceAdapter.class);
	}

	private void addJaxbElementToCurrentParentComplexCmsPropertyType(CmsPropertyType cmsPropertyType) {
		if (contentObjectMarshalContext.getFirstComplexCmsPropertyType() == null){
			contentObjectMarshalContext.getContentObjectType().getCmsProperties().add(cmsPropertyType);
		}
		else{
			contentObjectMarshalContext.getFirstComplexCmsPropertyType().getCmsProperties().add(cmsPropertyType);
		}
	}

	/**
	 * If definition visited is or belongs to an aspect definition
	 * then full path is required in contrary to other case where relative path to
	 * content object type is required
	 */
	public void enableAspectDefinitionVisit(){
		contentObjectMarshalContext.pushAspectsAreVisited(true);
	}

	public ContentObjectType getContentObjectType() {
		return contentObjectMarshalContext.getContentObjectType();
	}

	public Map<QName, String> getXsiSchemaLocationMap() {
		return xsiSchemaLocationMap;
	}
	
	
}
