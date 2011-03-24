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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.SpaceProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.jaxb.AstroboaValidationEventHandler;
import org.betaconceptframework.astroboa.model.jaxb.type.BinaryChannelType;
import org.betaconceptframework.astroboa.model.jaxb.type.CmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.ComplexCmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.ContentObjectType;
import org.betaconceptframework.astroboa.model.jaxb.type.SimpleCmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.SpaceType;
import org.betaconceptframework.astroboa.model.jaxb.type.TopicType;
import org.betaconceptframework.astroboa.model.jaxb.visitor.ContentObjectMarshalVisitor;
import org.betaconceptframework.astroboa.util.SchemaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible to marshal a content object
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectAdapter extends XmlAdapter<ContentObjectType, ContentObject>{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	//Used for unmarshaling. 
	//Does not follow the specs but there is no other
	//way for JAXB to decide which JAXBElement to create for cms properties
	//since ObjectFactory does not contain any creation method for these cms properties
	private Unmarshaller unmarshaller;

	private DatatypeFactory df ;

	private Marshaller marshaller;

	private ContentObjectMarshalVisitor marshallerVisitor;
	private boolean serializeBinaryContent;
	private boolean exportAllProperties;

	@Override
	public ContentObjectType marshal(ContentObject contentObject) throws Exception {

		String contentObjectTypeName = retrieveContentObjectType(contentObject);

		ContentObjectTypeDefinition contentObjectTypeDefinition = contentObject.getTypeDefinition();

		if (contentObjectTypeDefinition == null){
			throw new CmsException("Unable to locate definition for content object "+ contentObject.getId() + " with type "+ contentObjectTypeName);
		}

		JAXBElement<ContentObjectType> contentObjectTypeJaxb = 	
			new JAXBElement<ContentObjectType>(contentObjectTypeDefinition.getQualifiedName(), ContentObjectType.class, null, new ContentObjectType());

		ContentObjectType contentObjectType = contentObjectTypeJaxb.getValue();

		if (marshallerVisitor == null){
			marshallerVisitor = new ContentObjectMarshalVisitor();
		}

		marshallerVisitor.initialize(contentObject, contentObjectType, marshaller, serializeBinaryContent,exportAllProperties);

		contentObjectTypeDefinition.accept(marshallerVisitor);

		if (CollectionUtils.isNotEmpty(contentObject.getComplexCmsRootProperty().getAspects())){

			marshallerVisitor.enableAspectDefinitionVisit();

			Collection<ComplexCmsPropertyDefinition> aspectDefinitions = contentObject.getComplexCmsRootProperty().getAspectDefinitions().values();

			for (ComplexCmsPropertyDefinition aspectDefinition : aspectDefinitions){
				aspectDefinition.accept(marshallerVisitor);
			}
		}

		appendSchemaLocationToMarshaller();


		return contentObjectTypeJaxb.getValue();
	}




	private void appendSchemaLocationToMarshaller() throws PropertyException {

		if (MapUtils.isNotEmpty(marshallerVisitor.getXsiSchemaLocationMap())){

			for (Map.Entry<QName, String> xsiSchemaLocationEntry: marshallerVisitor.getXsiSchemaLocationMap().entrySet()){

				SchemaUtils.appendSchemaLocationToMarshaller(marshaller, 
						xsiSchemaLocationEntry.getKey().getNamespaceURI(),
						xsiSchemaLocationEntry.getValue(),
						xsiSchemaLocationEntry.getKey().getPrefix());
			}

			marshallerVisitor.getXsiSchemaLocationMap().clear();

		}
	}




	private String retrieveContentObjectType(ContentObject contentObject) {

		if (contentObject.getContentObjectType() == null){
			throw new CmsException("ContentObject "+contentObject.getSystemName()+" does not have content object type name");	
		}

		return contentObject.getContentObjectType();
	}


	public void setMarshaller(Marshaller marshaller, boolean exportBinaryContent,boolean exportAllProperties) {
		this.marshaller = marshaller;

		this.serializeBinaryContent = exportBinaryContent;
		this.exportAllProperties = exportAllProperties;
	}


	@Override
	public ContentObject unmarshal(ContentObjectType contentObjectType) throws Exception {
		return null;
 /*
		ContentObject contentObject = null;

		if (contentObjectType !=null ){

			contentObject = (ContentObject) contentObjectType.getCmsRepositoryEntityFromContextUsingCmsIdentifierOrReference();

			if (contentObject != null){
				return contentObject;
			}
			
			//Try with systemName
			if (contentObjectType.getSystemName() != null){
				
				contentObject = (ContentObject) contentObjectType.getCmsRepositoryEntityFromContextUsingKey(contentObjectType.getSystemName());

				if (contentObject != null){
					return contentObject;
				}
			}

			if (unmarshaller == null){
				createUnmarshaller();
			}

			if (df == null){
				try {
					df = DatatypeFactory.newInstance();
				} catch (DatatypeConfigurationException e) {
					throw new CmsException(e);
				}
			}

			//Create content object
			contentObject = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newContentObjectForType(contentObjectType.getContentObjectTypeName(), null);

			//Unmarshall basic attributes
			contentObject.setId(contentObjectType.getId());
			contentObject.setSystemBuiltinEntity(contentObjectType.isSystemBuiltinEntity());
			contentObject.setSystemName(contentObjectType.getSystemName());

			contentObject.setOwner(contentObjectType.getOwner());
			unmarshallCmsProperties(contentObject.getComplexCmsRootProperty(), contentObjectType.getCmsProperties());

			contentObjectType.registerCmsRepositoryEntityToContext(contentObject);
		}

		return contentObject;
		*/
	}

	private void unmarshallCmsProperties(
			ComplexCmsProperty parentComplexCmsProperty,
			List<CmsPropertyType> cmsProperties) throws Exception {

		if (CollectionUtils.isNotEmpty(cmsProperties) && parentComplexCmsProperty != null){

			List<String> propertyPathsWhoseDefaultValueHasBeenRemoved = new ArrayList<String>();

			for (CmsPropertyType cmsPropertyType : cmsProperties){

				if (cmsPropertyType.getQname() == null){
					logger.warn("Could not find name for cms property type {} which belongs to {}. Unmarshalling will ignore this property",
							cmsPropertyType.toString(), parentComplexCmsProperty.getFullPath());
					continue;
				}

				//Retrieve property name
				String cmsPropertyName = cmsPropertyType.getQname().getLocalPart();

				//Find its definition
				CmsProperty<?,?> cmsProperty = null;

				if (parentComplexCmsProperty.isChildPropertyDefined(cmsPropertyName)){

					CmsPropertyDefinition childPropertyDefinition = parentComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition(cmsPropertyName);

					//If property is a simple property or a single value complex loaded
					if (childPropertyDefinition.getValueType() != ValueType.Complex  || !childPropertyDefinition.isMultiple()){
						cmsProperty = parentComplexCmsProperty.getChildProperty(cmsPropertyName);
					}
					else{
						//Property is multiple complex property. Create a new instance
						cmsProperty = parentComplexCmsProperty.createNewValueForMulitpleComplexCmsProperty(cmsPropertyName);
					}

					//Removed default value rendered by Astroboa
					if (cmsProperty != null 
							&& childPropertyDefinition instanceof SimpleCmsPropertyDefinition 
							&& ((SimpleCmsPropertyDefinition)childPropertyDefinition).isSetDefaultValue() 
							&& ! propertyPathsWhoseDefaultValueHasBeenRemoved.contains(cmsPropertyName)){

						((SimpleCmsProperty)cmsProperty).removeValues();
						propertyPathsWhoseDefaultValueHasBeenRemoved.add(cmsPropertyName);
					}
				}
				else{
					//Create an aspect. This is done only in first level 
					if (parentComplexCmsProperty instanceof ComplexCmsRootProperty){
						cmsProperty = parentComplexCmsProperty.getChildProperty(cmsPropertyName);
					}
				}

				if (cmsProperty == null){
					throw new CmsException("Could not find definition for property '"+cmsPropertyName+"'");
				}

				unmarshallCmsProperty(cmsPropertyType, cmsProperty);
			}
		}

	}

	private void unmarshallCmsProperty(CmsPropertyType cmsPropertyType, CmsProperty<?, ?> cmsProperty) throws Exception {

		String content = null;

		switch(cmsProperty.getValueType()){
		case Complex:

			final ComplexCmsProperty<?, ?> complexCmsProperty = ((ComplexCmsProperty<?,?>)cmsProperty);

			if (! (cmsPropertyType instanceof ComplexCmsPropertyType)){
				logger.warn("CmsPropertyType {} is a simple type where as corresponding cms property is a complex property {}. Unmarhsalling will ignore this property",
						cmsPropertyType.getQname().toString(), complexCmsProperty.getFullPath());
				return ;
			}

			ComplexCmsPropertyType complexCmsPropertyType = (ComplexCmsPropertyType)cmsPropertyType;

			complexCmsProperty.setId(complexCmsPropertyType.getCmsIdentifier());

			unmarshallCmsProperties(complexCmsProperty, complexCmsPropertyType.getCmsProperties());

			break;
		case Boolean:

			content = retrieveContentFromSimpleCmsPropertyType(cmsPropertyType,	cmsProperty);

			if (content != null){
				((BooleanProperty)cmsProperty).addSimpleTypeValue(Boolean.valueOf(content));
			}

			break;
		case Date:

			content = retrieveContentFromSimpleCmsPropertyType(cmsPropertyType,	cmsProperty);

			if (content != null){
				((CalendarProperty)cmsProperty).addSimpleTypeValue(df.newXMLGregorianCalendar(content).toGregorianCalendar());
			}

			break;
		case Double:

			content = retrieveContentFromSimpleCmsPropertyType(cmsPropertyType,	cmsProperty);

			if (content != null){
				((DoubleProperty)cmsProperty).addSimpleTypeValue(Double.valueOf(content));
			}

			break;
		case Long:

			content = retrieveContentFromSimpleCmsPropertyType(cmsPropertyType,	cmsProperty);

			if (content != null){
				((LongProperty)cmsProperty).addSimpleTypeValue(Long.valueOf(content));
			}

			break;
		case String:

			content = retrieveContentFromSimpleCmsPropertyType(cmsPropertyType,	cmsProperty);

			if (content != null){
				((StringProperty)cmsProperty).addSimpleTypeValue(content);
			}

			break;

		case Binary:

			if (! (cmsPropertyType instanceof BinaryChannelType)){
				logger.warn("CmsPropertyType {} is not a binary type where as corresponding cms property is a binary property {}. Unmarhsalling will ignore this property",
						cmsPropertyType.getQname().toString(), cmsProperty.getFullPath());
				return;
			}

			//Further unmarshall binary channel type to BinaryChannel
			BinaryChannel binaryChannel = getBinaryChannelAdapterFromUnmarshaller().unmarshal(((BinaryChannelType)cmsPropertyType));

			binaryChannel.setName(cmsProperty.getName());

			((BinaryProperty)cmsProperty).addSimpleTypeValue(binaryChannel);

			break;

		case ContentObject:

			if (! (cmsPropertyType instanceof ContentObjectType)){
				logger.warn("CmsPropertyType {} is not a content object type where as corresponding cms property is a content object property {}. Unmarhsalling will ignore this property",
						cmsPropertyType.getQname().toString(), cmsProperty.getFullPath());
				return;
			}

			//Ignore URL and use only type and identifier to create an empty content object
			//Use this instance to further unmarhsall ContentObjectType but do not proceed to its children
			ContentObject contentObject = unmarshal((ContentObjectType) cmsPropertyType);

			/*String contentObjectId = ((ContentObjectReferenceType)cmsPropertyType).getId();
			String contentObjectType = ((ContentObjectReferenceType)cmsPropertyType).getContentObjectTypeName();
			String contentObjectSystemName = ((ContentObjectReferenceType)cmsPropertyType).getSystemName();
			String title = ((ContentObjectReferenceType)cmsPropertyType).getTitle();

			ContentObject contentObject = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newContentObjectForType(contentObjectType, "en"); //Locale plays no role

			contentObject.setId(contentObjectId);
			contentObject.setSystemName(contentObjectSystemName);

			if (title != null){
				StringProperty contentObjectTitleProperty = (StringProperty) contentObject.getCmsProperty("profile.title");
				if (contentObjectTitleProperty != null){
					contentObjectTitleProperty.addSimpleTypeValue(title);
				}
			}*/
			
			((ContentObjectProperty)cmsProperty).addSimpleTypeValue(contentObject);

			break;
		case RepositoryUser:
			/*RepositoryUser repositoryUser = cmsRepositoryEntityConverter.unMarshallAnotherCmsRepositoryEntity(reader, RepositoryUser.class, context, repositoryUserConverter);

			((RepositoryUserProperty)cmsProperty).addSimpleTypeValue(repositoryUser);
			break;*/
			break;
		case Topic:

			if (! (cmsPropertyType instanceof TopicType)){
				logger.warn("CmsPropertyType {} is not a topic property type where as corresponding cms property is a topic property {}. Unmarhsalling will ignore this property",
						cmsPropertyType.getQname().toString(), cmsProperty.getFullPath());
				return;
			}


			//Further unmarshall topicType to TopicImpl
			Topic topic = getTopicAdapterFromUnmarshaller().unmarshal((TopicType)cmsPropertyType);

			if (StringUtils.isBlank(topic.getId()) && StringUtils.isBlank(topic.getName())){
				throw new CmsException("Unable to further unmarshal element "+ ((TopicType)cmsPropertyType).getName().toString()+ " Found no " +
				" topic cmsIdentifier or  topic name");
			}

			((TopicProperty)cmsProperty).addSimpleTypeValue(topic);

			break;
		case Space:

			if (! (cmsPropertyType instanceof SpaceType)){
				logger.warn("CmsPropertyType {} is not a topic property type where as corresponding cms property is a topic property {}. Unmarhsalling will ignore this property",
						cmsPropertyType.getQname().toString(), cmsProperty.getFullPath());
				return;
			}


			//Further unmarshall spaceType to SpaceImpl
			Space space = getSpaceAdapterFromUnmarshaller().unmarshal((SpaceType)cmsPropertyType);

			if (StringUtils.isBlank(space.getId()) && StringUtils.isBlank(space.getName())){
				throw new CmsException("Unable to further unmarshal element "+ ((SpaceType)cmsPropertyType).getName().toString()+ " Found no " +
				" space cmsIdentifier or  topic name");
			}

			((SpaceProperty)cmsProperty).addSimpleTypeValue(space);
			break;

		default:
			throw new CmsException("Unsupported ValueType "+cmsProperty.getValueType()+" for unmarshalling ");
		}

	}

	private String retrieveContentFromSimpleCmsPropertyType(
			CmsPropertyType cmsPropertyType, CmsProperty<?, ?> cmsProperty) {
		if (! (cmsPropertyType instanceof SimpleCmsPropertyType)){
			logger.warn("CmsPropertyType {} is not a simple type where as corresponding cms property is a simple property {}. Unmarhsalling will ignore this property",
					cmsPropertyType.getQname().toString(), cmsProperty.getFullPath());
			return null;
		}

		SimpleCmsPropertyType simpleCmsPropertyType = (SimpleCmsPropertyType)cmsPropertyType;

		return simpleCmsPropertyType.getContent();
	}

	private BinaryChannelAdapter getBinaryChannelAdapterFromUnmarshaller() {
		if (unmarshaller.getAdapter(BinaryChannelAdapter.class) == null){
			unmarshaller.setAdapter(new BinaryChannelAdapter(false));
		}

		return unmarshaller.getAdapter(BinaryChannelAdapter.class);
	}

	private TopicAdapter getTopicAdapterFromUnmarshaller() {
		if (unmarshaller.getAdapter(TopicAdapter.class) == null){
			unmarshaller.setAdapter(new TopicAdapter());
		}

		return unmarshaller.getAdapter(TopicAdapter.class);
	}

	private SpaceAdapter getSpaceAdapterFromUnmarshaller() {
		if (unmarshaller.getAdapter(SpaceAdapter.class) == null){
			unmarshaller.setAdapter(new SpaceAdapter());
		}

		return unmarshaller.getAdapter(SpaceAdapter.class);
	}

	private void createUnmarshaller() throws JAXBException{

		//unmarshaller = CmsEntitySerialization.Context.createUnmarshaller();

		//Must specify our own event handler in order to propagate SAX exceptions 
		unmarshaller.setEventHandler(new AstroboaValidationEventHandler());

		unmarshaller.setAdapter(this);
	}

}
