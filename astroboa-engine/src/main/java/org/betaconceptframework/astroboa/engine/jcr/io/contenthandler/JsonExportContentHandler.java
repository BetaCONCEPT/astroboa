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
package org.betaconceptframework.astroboa.engine.jcr.io.contenthandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.xml.sax.Attributes;


/**
 * ContentHandler responsible to write content to JSON format
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JsonExportContentHandler implements ExportContentHandler{

	private static final String PREFIX_SEPARATOR = ":";
	private XMLStreamWriter writer;
	private OutputStreamWriter internalWriter;

	public JsonExportContentHandler(OutputStream outputStream, boolean stripRoot,boolean prettyPrint) throws IOException {
		
		internalWriter = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
		writer = CmsEntitySerialization.Context.createJsonXmlStreamWriter(internalWriter, stripRoot,prettyPrint);
	}

	@Override
	public void closeOpenElement() throws Exception {
		
	}

	@Override
	public void end() throws Exception {
		writer.writeEndDocument();
		writer.flush();
	}

	@Override
	public void endElement(String elementQName) throws Exception {
		writer.writeEndElement();
	}

	@Override
	public Writer getWriter() {
		return internalWriter;
	}

	@Override
	public void start() throws Exception {
		writer.writeStartDocument();
	}

	@Override
	public void startElement(String elementQName,
			Attributes attributes) throws Exception {
		
		writer.writeStartElement(stripPrefixFromName(elementQName));
		
		if (attributes != null && attributes.getLength() > 0){
			for (int i=0; i<attributes.getLength(); i++){
				writeAttribute(attributes.getLocalName(i), attributes.getValue(i));
			}
		}
	}

	@Override
	public void startElementWithNoAttributes(String elementQName)
			throws Exception {
		
		startElement(elementQName, null);
		
	}

	@Override
	public void startElementWithOnlyOneAttribute(String elementQName,
			String attributeName, String attributeValue) throws Exception {
		
		writer.writeStartElement(stripPrefixFromName(elementQName));
		
		writeAttribute(attributeName, attributeValue);
		
	}

	@Override
	public void writeAttribute(String attributeName, String attributeValue)
			throws Exception {
		writer.writeAttribute(stripPrefixFromName(attributeName), attributeValue);
	}

	@Override
	public void writeContent(char[] ch, int start, int length) throws Exception {
		writer.writeCharacters(ch, start, length);
		
	}
	
	private String stripPrefixFromName(String name){
		if (name != null && name.contains(PREFIX_SEPARATOR)){
			return StringUtils.substringAfter(name, PREFIX_SEPARATOR);
		}
		
		return name;
	}
	
}
