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
package org.betaconceptframework.astroboa.model.jaxb;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.ArrayUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor.VisitType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.jaxb.adapter.ContentObjectAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.LocalizationAdapter;
import org.betaconceptframework.astroboa.model.jaxb.listener.AstroboaMarshalListener;
import org.betaconceptframework.astroboa.model.jaxb.type.ContentObjectType;
import org.betaconceptframework.astroboa.model.jaxb.writer.JSONXmlStreamWriter;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.serializer.CmsDefinitionSerializer;
import org.betaconceptframework.astroboa.util.SchemaUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for exporting a {@link CmsRepositoryEntity} to Xml or Json.
 * 
 * <p>
 * Exporting a {@link CmsRepositoryEntity} to xml or JSON
 * is done through methods {@link #toXml(CmsRepositoryEntity)}, 
 *  {@link #toJson(CmsRepositoryEntity)} for full exporting and
 *  methods {@link #toXml(CmsRepositoryEntity, String...)} and 
 *  {@link #toJson(CmsRepositoryEntity, String...)} for partial exporting, 
 *  i.e. only specified properties will be exported.
 * </p>
 *  
 * <p>
 *  However it is recommended to use method {@link CmsRepositoryEntity#xml()} 
 *  or {@link CmsRepositoryEntity#json()} instead.  
 * </p>
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsEntitySerialization {

	Context;

	private static final JAXBContext jaxbContext = initContext();
	
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JsonFactory jsonFactory = new JsonFactory();
	
	private static JAXBContext initContext()  {
		try {
			return JAXBContext.newInstance("org.betaconceptframework.astroboa.model.impl");
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Marshaller createMarshaller(ResourceRepresentationType<?>  resourceRepresentationType, boolean prettyPrint) throws JAXBException{

		boolean jsonResourceRepresentationType = resourceRepresentationType!=null&&ResourceRepresentationType.JSON.equals(resourceRepresentationType);
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setListener(new AstroboaMarshalListener(jsonResourceRepresentationType));
		
		if (jsonResourceRepresentationType){
			LocalizationAdapter localizationAdapter = new LocalizationAdapter();
			localizationAdapter.useJsonVersion();
			marshaller.setAdapter(localizationAdapter);
		}
		
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);

		return new AstroboaMarshaller(marshaller, resourceRepresentationType);
	}
	
	public String toJson(CmsRepositoryEntity cmsRepositoryEntity,boolean exportBinaryContent,boolean prettyPrint,String... propertyPathsToBeMarshalled){
		
		return marshalEntity(cmsRepositoryEntity, ResourceRepresentationType.JSON,  exportBinaryContent, prettyPrint, propertyPathsToBeMarshalled);
	}
	
	public String toXml(CmsRepositoryEntity cmsRepositoryEntity,boolean exportBinaryContent ,boolean prettyPrint, String... propertyPathsToBeMarshalled){
		
		return marshalEntity(cmsRepositoryEntity, ResourceRepresentationType.XML,  exportBinaryContent, prettyPrint, propertyPathsToBeMarshalled);
	}

	private String marshalEntity(CmsRepositoryEntity cmsRepositoryEntity, ResourceRepresentationType<?>  resourceRepresentationType, 
			boolean exportBinaryContent, boolean prettyPrint, String... propertyPathsToBeMarshalled){

		if (cmsRepositoryEntity != null){

			if (cmsRepositoryEntity instanceof ContentObject ||
					cmsRepositoryEntity instanceof Topic ||
					cmsRepositoryEntity instanceof Space ||
					cmsRepositoryEntity instanceof RepositoryUser ||
					cmsRepositoryEntity instanceof Taxonomy ){

				StringWriter writer = new StringWriter();
				

				Marshaller marshaller = null;
				try {
					marshaller = createMarshaller(resourceRepresentationType, prettyPrint);

					if (cmsRepositoryEntity instanceof ContentObject){
						
						if (! ArrayUtils.isEmpty(propertyPathsToBeMarshalled)){
							marshaller.setProperty(AstroboaMarshaller.CMS_PROPERTIES_TO_BE_MARSHALLED, Arrays.asList(propertyPathsToBeMarshalled));
						}
						
						//ContentObject needs special marshaling as JAXB does not have
						//enough information in order to initialize appropriate adapter for
						//ContentObject
						ContentObjectAdapter adapter = new ContentObjectAdapter();
						adapter.setMarshaller(marshaller, exportBinaryContent,ArrayUtils.isEmpty(propertyPathsToBeMarshalled));

						marshaller.setAdapter(adapter);

						ContentObjectType contentObjectType = 
							marshaller.getAdapter(ContentObjectAdapter.class).marshal((ContentObject) cmsRepositoryEntity);

						JAXBElement<ContentObjectType> contentObjectTypeJaxb = 	
							new JAXBElement<ContentObjectType>(((ContentObject) cmsRepositoryEntity).getTypeDefinition().getQualifiedName(), 
									ContentObjectType.class, null, contentObjectType);
						
						marshaller.marshal(contentObjectTypeJaxb, writer);
					}
					else{

						//Provide schema location 
						marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,  
								" "+BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI+" "+
								SchemaUtils.buildSchemaLocationForAstroboaModelSchemaAccordingToActiveClient());
						
						marshaller.marshal(cmsRepositoryEntity, writer);
					}
					

				} catch (Exception e) {
					throw new CmsException(e);
				}
				finally{
					
					marshaller = null;
				}


				return writer.toString();
			}
			else{
				throw new CmsException("Creating XML for entity type "+ cmsRepositoryEntity.getClass().getName() + " is not supported");
			}
		}

		throw new CmsException("No entity is provided. Unable to create xml");
	}

	public XMLStreamWriter createJsonXmlStreamWriter(Writer writer, boolean stripRoot, boolean prettyPrint) throws IOException{
		
        final JsonGenerator jsonGenerator = createJsonGenerator(writer,	prettyPrint);

       	return new JSONXmlStreamWriter(jsonGenerator, stripRoot);

	}

	public JsonGenerator createJsonGenerator(Writer writer, boolean prettyPrint)
			throws IOException {
		final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);
        
        if (prettyPrint){
        	jsonGenerator.useDefaultPrettyPrinter();
        }
		return jsonGenerator;
	}

	public String toJson(CmsDefinition cmsDefinition, boolean prettyPrint) {
		return marshalDefinition(cmsDefinition, ResourceRepresentationType.JSON, prettyPrint);
	}

	public String toXml(CmsDefinition cmsDefinition, boolean prettyPrint) {
		return marshalDefinition(cmsDefinition, ResourceRepresentationType.XML,prettyPrint);
	}
	
	private String marshalDefinition(CmsDefinition cmsDefinition, ResourceRepresentationType<?>  resourceRepresentationType,boolean prettyPrint){

		if (cmsDefinition != null){

			CmsDefinitionSerializer cmsDefinitionExportVisitor = new CmsDefinitionSerializer(prettyPrint,ResourceRepresentationType.JSON.equals(resourceRepresentationType));
			
			cmsDefinitionExportVisitor.setVisitType(VisitType.Full);
			
			cmsDefinition.accept(cmsDefinitionExportVisitor);
			
			return cmsDefinitionExportVisitor.exportOutcome();

		}
		else{
			throw new CmsException("Definition is null. Cannot serialize to "+resourceRepresentationType);
		}

	}

	public String toXsd(CmsDefinition cmsDefinition) {
		
		 LazyLoader lazyLoader = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
		  
		  if (lazyLoader != null){
			  String fullPropertyDefinitionPath = cmsDefinition.getName();
			  
			  if (cmsDefinition instanceof CmsPropertyDefinition){
				  fullPropertyDefinitionPath = ((CmsPropertyDefinition)cmsDefinition).getFullPath();
			  }
			  
			  return lazyLoader.getDefinitionService().getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.XSD,true);
			  
		  }
		  
		  return null;
	}

}
