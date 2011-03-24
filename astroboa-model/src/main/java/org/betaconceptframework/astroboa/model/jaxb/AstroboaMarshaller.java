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
package org.betaconceptframework.astroboa.model.jaxb;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * This is a wrapper of a JAXB Marshaller. 
 * 
 * It keeps several properties which are used during marshalling Astroboa Entities.
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaMarshaller implements Marshaller{

	private Marshaller marshaller;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * This property represents all property paths that will be marshaled
	 * in cases where marshaling entire content object is not desired
	 */
	public final static String CMS_PROPERTIES_TO_BE_MARSHALLED = "cmsPropertiesToBeMarshalled";
	
	/**
	 * Object holding all property paths which should be marshaled
	 */
	private List<String> cmsPropertiesToBeMarshalled;
	
	private ResourceRepresentationType<?>  resourceRepresentationType;
	
	public AstroboaMarshaller(Marshaller marshaller, ResourceRepresentationType<?>  resourceRepresentationType) {
		this.marshaller = marshaller;
		this.resourceRepresentationType = resourceRepresentationType;
		
		if (marshaller == null){
			throw new CmsException("No marshaller provided. Cannot instantiate Astroboa Marshaller");
		}
		
	}

	
	@Override
	public <A extends XmlAdapter> A getAdapter(Class<A> arg0) {
		return marshaller.getAdapter(arg0);
	}

	@Override
	public AttachmentMarshaller getAttachmentMarshaller() {
		
		return marshaller.getAttachmentMarshaller();
	}

	@Override
	public ValidationEventHandler getEventHandler() throws JAXBException {
		
		return marshaller.getEventHandler();
	}

	@Override	
	public Listener getListener() {
		
		return marshaller.getListener();
	}

	@Override
	public Node getNode(Object arg0) throws JAXBException {
		
		return marshaller.getNode(arg0);
	}

	@Override	
	public Object getProperty(String arg0) throws PropertyException {
		
		if (StringUtils.isBlank(arg0)){
			return null;
		}
		
		if (CMS_PROPERTIES_TO_BE_MARSHALLED.equals(arg0)){
			return cmsPropertiesToBeMarshalled;
		}
		
		return marshaller.getProperty(arg0);
	}

	@Override
	public Schema getSchema() {
		
		return marshaller.getSchema();
	}

	public boolean outputTypeIsJSON() {
		return resourceRepresentationType != null && resourceRepresentationType != ResourceRepresentationType.XML ;
	}

	@Override
	public void marshal(Object arg0, Result arg1) throws JAXBException {
			marshaller.marshal(arg0, arg1);
	}
	
	@Override	
	public void marshal(Object arg0, OutputStream arg1) throws JAXBException {
		if (outputTypeIsJSON()){
			marshaller.marshal(arg0, new OutputStreamWriter(arg1, Charset.forName("UTF-8")));
		}
		else{
			marshaller.marshal(arg0, arg1);
		}
	}



	@Override
	public void marshal(Object arg0, Writer arg1) throws JAXBException {
		if (outputTypeIsJSON()){
			
			XMLStreamWriter jsonXmlStreamWriter = null;
			try {
				
				jsonXmlStreamWriter = CmsEntitySerialization.Context.createJsonXmlStreamWriter(arg1, true,BooleanUtils.isTrue((Boolean)marshaller.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)));
				
				marshal(arg0, jsonXmlStreamWriter );
			} catch (Exception e) {
				try{
					if (jsonXmlStreamWriter != null){
						jsonXmlStreamWriter.flush();
						logger.error("JSON Export so far {}", arg1);
					}
				}
				catch(Exception e1){
					//Ignore it
				}
				throw new JAXBException(e);
			}
		}
		else{
			marshaller.marshal(arg0, arg1);
		}
		
		
	}


	@Override
	public void marshal(Object arg0, ContentHandler arg1) throws JAXBException {
		marshaller.marshal(arg0, arg1);	
		
	}

	@Override	
	public void marshal(Object arg0, Node arg1) throws JAXBException {
		marshaller.marshal(arg0, arg1);
		
	}

	@Override
	public void marshal(Object arg0, XMLStreamWriter arg1) throws JAXBException {
		marshaller.marshal(arg0, arg1);		
		
	}

	@Override	
	public void marshal(Object arg0, XMLEventWriter arg1) throws JAXBException {
		marshaller.marshal(arg0, arg1);
		
	}

	@Override
	public void setAdapter(XmlAdapter arg0) {
		marshaller.setAdapter(arg0);	
		
	}

	@Override
	public <A extends XmlAdapter> void setAdapter(Class<A> arg0, A arg1) {
		marshaller.setAdapter(arg0, arg1);
		
	}

	@Override
	public void setAttachmentMarshaller(AttachmentMarshaller arg0) {
		marshaller.setAttachmentMarshaller(arg0);
		
	}

	@Override
	public void setEventHandler(ValidationEventHandler arg0)
			throws JAXBException {
		marshaller.setEventHandler(arg0);
		
	}

	@Override
	public void setListener(Listener arg0) {
		marshaller.setListener(arg0);		
	}

	@Override
	public void setProperty(String arg0, Object arg1) throws PropertyException {
		if (StringUtils.isNotBlank(arg0)){
			if (CMS_PROPERTIES_TO_BE_MARSHALLED.equals(arg0)){
				
				if (! (arg1 instanceof List) ){
					throw new PropertyException("Marshaller property "+CMS_PROPERTIES_TO_BE_MARSHALLED+ " must be of type List<String>");
				}
				
				cmsPropertiesToBeMarshalled = (List<String>) arg1;
			}
			else if (Marshaller.JAXB_SCHEMA_LOCATION.equals(arg0) && outputTypeIsJSON())
			{
				//Do not provide schema location when marshaling to JSON
			}
			else 
			{
				marshaller.setProperty(arg0, arg1);
			}
		}
		
	}

	@Override
	public void setSchema(Schema arg0) {
		marshaller.setSchema(arg0);
		
	}


	@Override
	public void marshal(Object jaxbElement, File output) throws JAXBException {
		marshaller.marshal(jaxbElement, output);
		
	}
}
