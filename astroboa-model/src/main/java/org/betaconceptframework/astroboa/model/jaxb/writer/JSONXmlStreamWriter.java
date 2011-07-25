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
package org.betaconceptframework.astroboa.model.jaxb.writer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.impl.item.CmsDefinitionItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JSONXmlStreamWriter implements XMLStreamWriter{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private JsonGenerator generator;

	private Deque<JsonObject> objectQueue = new ArrayDeque<JsonObject>(); 
	
	private boolean stripRoot;

	public JSONXmlStreamWriter(JsonGenerator generator, boolean stripRoot) {
		
		this.generator = generator;
		this.stripRoot = stripRoot;
	}

	@Override
	public void close() throws XMLStreamException {
		try {
			generator.close();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
		
	}

	@Override
	public void flush() throws XMLStreamException {
		try {
			generator.flush();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
		
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return null;
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return null;
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
	}

	@Override
	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
		try {
			
			if (! objectQueue.isEmpty()){
				if (StringUtils.equals(CmsConstants.EXPORT_AS_AN_ARRAY_INSTRUCTION, localName)){
					objectQueue.peek().exportAsAnArray = BooleanUtils.isTrue(BooleanUtils.toBoolean(value));
				}
				else{
					objectQueue.peek().addAttribute(localName, value);
				}

			}
			
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}
		
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
		writeAttribute(localName, value);
		
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
		writeAttribute(localName, value);
		
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		try {
			
			if (! objectQueue.isEmpty()){
				objectQueue.peek().addValue(data);
			}
			else{
				logger.warn("Cannot find Json Object to write cdata");
			}
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {

		try {
			
			if (! objectQueue.isEmpty()){
				objectQueue.peek().addValue(text);
			}
			else{
				logger.warn("Cannot find Json Object to write characters {}", text);
			}
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}

	}

	@Override
	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		
		try {
			
			writeCharacters(new String(text, start, len));
			
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}
	}


	@Override
	public void writeComment(String data) throws XMLStreamException {
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		
		try {
			
			if (! objectQueue.isEmpty()){
				objectQueue.peek().addChild(localName, new NullJsonObject(localName));
			}
			else{
				logger.warn("Cannot find Json Object to write empty element {}", localName);
			}

		} catch (Exception e) {
			throw new XMLStreamException(e);
		}		
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		writeEmptyElement(localName);
		
	}

	@Override
	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		writeEmptyElement(localName);
		
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {

		try {
			
			if (objectQueue.isEmpty()){
				throw new XMLStreamException("Could not find root JsonObject, queue is empty");
			}
			else{
				
				//Get last element in queue and export
				objectQueue.peekLast().toJson(! stripRoot);
			}

			//Free resources
			objectQueue.clear();
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}	

	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		try {
			
			//Do not remove the first object in the queue
			//which is the root object
			if (objectQueue.size()>1){
				objectQueue.pop();
			}
			
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}	
		
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
	}

	@Override
	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
	}

	@Override
	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		
		try {
			
			if (! objectQueue.isEmpty()){
				objectQueue.clear();
			}
			
			//At this point we do not know the root name
			objectQueue.push(new JsonObject(null));
			
		} catch (Exception e) {
				throw new XMLStreamException(e);
		}			
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument();
	}

	@Override
	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		writeStartDocument();
		
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		
		try {
			
			if (objectQueue.size() == 1 && objectQueue.peekFirst().name == null){
				//This is the root object. Append its name
				objectQueue.peekFirst().name  = localName;
			}
			else{
				JsonObject jsonObject = new JsonObject(localName);
				
				if (! objectQueue.isEmpty()){
					objectQueue.peek().addChild(localName, jsonObject);
				}
				
				objectQueue.push(jsonObject);
			}
			
		} catch (Exception e) {
			throw new XMLStreamException(e);
		}			
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		writeStartElement(localName);
	}

	@Override
	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		writeStartElement(localName);
	}
	

	private class JsonObject{
		
		protected String name;
		
		private Map<String,String> attributes = new LinkedHashMap<String, String>();
		
		private List<String> values = new ArrayList<String>();
		
		private Map<String, List<JsonObject>> childObjects = new LinkedHashMap<String, List<JsonObject>>();
		
		private boolean exportAsAnArray = false;
		
		private JsonObject(String name) {
			super();
			this.name = name;
		}

		public void addChild(String localName, JsonObject child) {
			
			if (localName == null){
				return ;
			}
			
			if (! childObjects.containsKey(localName)){
				childObjects.put(localName, new ArrayList<JsonObject>());
			}
			
			if (child != null){

				childObjects.get(localName).add(child);
				
				if (objectIsTheParentOfASingleChildObjectWhichRepresentsAnArray()){
					child.exportAsAnArray = true;
				}
			}
		}

		public void dispose(){
			attributes = null;
			childObjects = null;
		}
		
		public void addValue(String text) {
			values.add(text);
		}

		public void addAttribute(String localName, String value) {
				attributes.put(localName, value);
		}

		public void toJson(boolean exportName) throws Exception {
			
			
			//Element has no attributes, no child elements and no values do not serialize
			//It is an empty element and therefore it should have a null value
			if (hasNoAttributeNorAnyChildNorAnyValueToSerialize()){
				new NullJsonObject(name).toJson(exportName);
				return;
			}
			
			if (exportName){
				generator.writeFieldName(name);
			}
			
			boolean hasAttributes = hasAttributes();

			final boolean objectHasAtMostOneValue = ! hasAttributes && childObjects.isEmpty() && values.size() <= 1;
			
			//Special case
			if (!exportAsAnArray && childObjects.isEmpty() && objectIsTheParentOfASingleChildObjectWhichRepresentsAnArray()){
				//JSON Object has no children, represents an array but such instruction
				//has not been provided.
				//It must be one of the few built in elements which may have zero or more children.
				//Export As an array
				exportAsAnArray = true;
			}
			
			//If child should be exported as array and its name is exported
			//then start an array
			if (exportAsAnArray && exportName){
				generator.writeStartArray();
			}
			else if (! objectHasAtMostOneValue){
				//Start Object (write char '{') if object has attributes, or children
				//or has more than 1 values
				generator.writeStartObject();
			}
			
			//Write attributes
			exportAttributes();
			
			//Write child objects or its values
			if (! values.isEmpty()){
				
				for (String value : values){
					if (hasAttributes){
						generator.writeStringField("$", value);
					}
					else{
						generator.writeString(value);
					}
				}
			}
			
			if (! childObjects.isEmpty()){
				for (Entry<String,List<JsonObject>> childEntry : childObjects.entrySet()){
					
					String childObjectName = childEntry.getKey();
					
					List<JsonObject> childJsonObjects = childEntry.getValue();
					
					if (CollectionUtils.isEmpty(childJsonObjects)){
						generator.writeStringField(childObjectName, "");
					}
					else{

						boolean childIsArray = (childJsonObjects.size() > 1 || ( childJsonObjects.size() == 1 && childJsonObjects.get(0).exportAsAnArray));
							
						if (childIsArray){
							generator.writeArrayFieldStart(childObjectName);
						}
						
						for (JsonObject childJsonObject : childJsonObjects){
							//Do not export name if child is an array
							childJsonObject.toJson(!childIsArray);
						}
						
						if (childIsArray){
							generator.writeEndArray();
						}
					}
				}
			}
			
			//Write } or ]
			if (exportAsAnArray && exportName){
				generator.writeEndArray();
			}
			else if (! objectHasAtMostOneValue){
				generator.writeEndObject();
			}
			
			dispose();
		}

		private boolean hasNoAttributeNorAnyChildNorAnyValueToSerialize() {
			return  ! hasAttributes() && childObjects.isEmpty() && values.size() == 0;
		}

		protected boolean objectIsTheParentOfASingleChildObjectWhichRepresentsAnArray() {
			return StringUtils.equals(CmsConstants.ROOT_TOPICS, name) ||
			StringUtils.equals(CmsConstants.CHILD_TOPICS, name) ||
			StringUtils.equals(CmsConstants.CHILD_SPACES, name) ||
			StringUtils.equals(CmsConstants.RESOURCE_COLLECTION, name) ||
			StringUtils.equals(CmsConstants.CHILD_DEFINITIONS, name) ||
			StringUtils.equals(CmsDefinitionItem.acceptedContentTypes.getLocalPart(), name) ||
			StringUtils.equals(CmsDefinitionItem.acceptedTaxonomies.getLocalPart(), name) ||
			StringUtils.equals(CmsConstants.SUPER_TYPES, name);
		}

		private void exportAttributes() throws IOException, JsonGenerationException {
			
			if (hasAttributes()){
				for (Entry<String,String> attributeEntry : attributes.entrySet()){
					generator.writeStringField(attributeEntry.getKey(), attributeEntry.getValue());
				}
			}
		}

		private boolean hasAttributes() {
			return ! attributes.isEmpty();
		}
	}

	private class NullJsonObject extends JsonObject {
		private NullJsonObject(String name) {
			super(name);
		}

		@Override
		public void addChild(String localName, JsonObject child) {
			throw new CmsException("Null Json Object cannot have child objects");
		}

		@Override
		public void dispose() {
			super.dispose();
		}

		@Override
		public void addValue(String text) {
			throw new CmsException("Null Json Object cannot have value");
		}

		@Override
		public void addAttribute(String localName, String value) {
			throw new CmsException("Null Json Object cannot have Attributes");
		}

		@Override
		public void toJson(boolean exportName) throws Exception {

			if (exportName && ! objectIsTheParentOfASingleChildObjectWhichRepresentsAnArray()){
				generator.writeNullField(name);
			}
		}
	}
}
