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

package org.betaconceptframework.astroboa.util;

import java.io.StringWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.codehaus.jackson.JsonGenerator;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class JsonSerializer implements Serializer{

	private boolean prettyPrint = false;

	private StringWriter writer ;

	private JsonGenerator jsonGenerator;


	public JsonSerializer(StringWriter writer, boolean prettyPrint) {

		this.prettyPrint = prettyPrint;
		
		this.writer = writer;

		try{
			jsonGenerator = CmsEntitySerialization.Context.createJsonGenerator(writer, prettyPrint);
				
			jsonGenerator.writeStartObject();
				
		} catch (Exception e) {
			throw new CmsException(e);
		}
	}
	
	public void endArray(String name){
		
		try{
			jsonGenerator.writeEndArray();
		} catch (Exception e) {
			throw new CmsException(e);
		}
		
	}
	
	public void startArray(String name) {

		try{	
			jsonGenerator.writeArrayFieldStart(name);
		} catch (Exception e) {
			throw new CmsException(e);
		}
		
	}
	
	public void endElement(String name, boolean closeStartTag, boolean createCloseTag) {

		try {
				jsonGenerator.writeEndObject();
			} catch (Exception e) {
				throw new CmsException(e);
			}

	}

	public void writeAttribute(String name, String value){

			try {
				jsonGenerator.writeStringField(name, value);
			} catch (Exception e) {
				throw new CmsException(e);
			}
	}
	
	
	public void startElement(String name, boolean elementHasAttributes, boolean writeName) {

			try{
				if (writeName){
					jsonGenerator.writeObjectFieldStart(name);
				}
				else{
					jsonGenerator.writeStartObject();
				}
			} catch (Exception e) {
				throw new CmsException(e);
			}
			
	}
	
	public String serialize() {
		
			try{
				
				jsonGenerator.flush();
				jsonGenerator.close();
			} catch (Exception e) {
				throw new CmsException(e);
			}
		
		return writer.toString();
	}

	
	public boolean prettyPrintEnabled(){
		return prettyPrint;
	}

	public String getCurrentValue(){
		
			try{
				jsonGenerator.flush();
			} catch (Exception e) {
				throw new CmsException(e);
			}
		
		return writer.toString();
	}
	
	public void writeContent(String content, boolean escape){
	
		try{
			jsonGenerator.flush();
		} catch (Exception e) {
			throw new CmsException(e);
		}

		if (content != null){
			if (escape){
				writer.write(StringEscapeUtils.escapeXml(content));
			}
			else{
				writer.write(content);
			}
		}
		
	}
}
