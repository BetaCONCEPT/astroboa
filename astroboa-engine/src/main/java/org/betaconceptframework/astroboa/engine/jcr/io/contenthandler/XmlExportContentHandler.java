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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.jackrabbit.commons.xml.ToXmlContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is considered a patch to {@link ToXmlContentHandler}
 * class in order to make it more extensible and to adjust it to 
 * Astroboa requirements.
 * 
 * Therefore most of the code is taken from {@link ToXmlContentHandler}.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class XmlExportContentHandler implements ExportContentHandler{

	private static final int SPACES_FOR_IDENTATION = 3;
	
	private final AttributesImpl emptyAttributes = new AttributesImpl();
	/**
	 * The XML stream.
	 */
	private final Writer writer;

	/**
	 * The data part of the &lt;?xml?&gt; processing instruction included
	 * at the beginning of the XML stream.
	 */
	private final String declaration;

	/**
	 * Flag variable that is used to track whether a start tag has had it's
	 * closing ">" appended. Set to <code>true</code> by the
	 * {@link #startElement(String, String, String, Attributes)} method that
	 * <em>does not</em> output the closing ">". If this flag is still set
	 * when the {#link {@link #endElement(String, String, String)}} method
	 * is called, then the method knows that the element is empty and can
	 * close it with "/>". Any other SAX event will cause the open start tag
	 * to be closed with a normal ">".
	 *
	 * @see #closeStartTagIfOpen()
	 */
	private boolean startTagIsOpen = false;

	private boolean xmlDeclarationHasBeenWritten = false;

	/**
	 * Flag variable that is used to enable/disable pretty print. 
	 */
	private boolean prettyPrint = false;
	private Deque<Integer> number_of_spaces_to_use_for_identation = new ArrayDeque<Integer>();
	private boolean addNewLine = true;
	private boolean elementHasJustEnded = false;
	
	//--------------------------------------------------------< constructors >

	/**
	 * Creates an XML serializer that writes the serialized XML stream
	 * to the given output stream using the given character encoding.
	 *
	 * @param stream XML output stream
	 * @param encoding character encoding
	 * @throws UnsupportedEncodingException if the encoding is not supported
	 */
	public XmlExportContentHandler(OutputStream stream, String encoding, boolean prettyPrint)
	throws UnsupportedEncodingException {
		
		this.writer = new OutputStreamWriter(stream, encoding);
		
		this.declaration = "version=\"1.0\" encoding=\"" + encoding + "\"";
		
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Creates an XML serializer that writes the serialized XML stream
	 * to the given output stream using the UTF-8 character encoding.
	 *
	 * @param stream XML output stream
	 * @throws UnsupportedEncodingException 
	 */
	public XmlExportContentHandler(OutputStream stream, boolean prettyPrint) throws UnsupportedEncodingException {
		this(stream, "UTF-8", prettyPrint);
	}

	//-------------------------------------------------------------< private >

	private void write(char[] ch, int start, int length, boolean attribute)
	throws SAXException {
		for (int i = start; i < start + length; i++) {
			try {
				if (ch[i] == '>') {
					writer.write("&gt;");
				} else if (ch[i] == '<') {
					writer.write("&lt;");
				} else if (ch[i] == '&') {
					writer.write("&amp;");
				} else if (attribute && ch[i] == '"') {
					writer.write("&quot;");
				} else if (attribute && ch[i] == '\'') {
					writer.write("&apos;");
				} else {
					writer.write(ch[i]);
				}
			} catch (IOException e) {
				throw new SAXException(
						"Failed to output XML character: " + ch[i], e);
			}
		}
	}

	private void closeStartTagIfOpen() throws SAXException {
		if (startTagIsOpen) {
			try {
				writer.write(">");
				
				if (prettyPrint){
					
					if (number_of_spaces_to_use_for_identation.isEmpty()){
						number_of_spaces_to_use_for_identation.push(1);
					}
					else{
						number_of_spaces_to_use_for_identation.push(number_of_spaces_to_use_for_identation.peek()+SPACES_FOR_IDENTATION);
					}
					addNewLine = true;
				}
				
			} catch (IOException e) {
				throw new SAXException(
						"Failed to output XML bracket: >", e);
			}
			startTagIsOpen = false;
		}
		else{
			if (prettyPrint){
				addNewLine = true;
			}

		}
	}

	//------------------------------------------------------< ContentHandler >

	/**
	 * Starts the XML serialization by outputting the &lt;?xml?&gt; header.
	 */
	public void startDocument() throws SAXException {
		if (! xmlDeclarationHasBeenWritten)
		{
			processingInstruction("xml", declaration);

			xmlDeclarationHasBeenWritten = true;
			
			number_of_spaces_to_use_for_identation.push(1);
		}

	}

	/**
	 * Ends the XML serialization by flushing the output stream.
	 */
	public void endDocument() throws SAXException {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new SAXException("Failed to flush XML output", e);
		}
	}

	/**
	 * Serializes a processing instruction.
	 */
	public void processingInstruction(String target, String data)
	throws SAXException {
		closeStartTagIfOpen();
		try {
			writer.write("<?");
			writer.write(target);
			if (data != null) {
				writer.write(" ");
				writer.write(data);
			}
			writer.write("?>");
		} catch (IOException e) {
			throw new SAXException(
					"Failed to output XML processing instruction: " + target, e);
		}
	}

	/**
	 * Outputs the specified start tag with the given attributes.
	 */
	public void startElement(
			String namespaceURI, String localName, String qName,
			Attributes atts) throws SAXException {
		closeStartTagIfOpen();
		try {
			
			if (prettyPrint){
				elementHasJustEnded = false;
				writeIdentation(addNewLine);
			}
			
			writer.write("<");
			writer.write(qName);

			for (int i = 0; i < atts.getLength(); i++) {
				writeAttribute(atts.getQName(i), atts.getValue(i));
			}

			startTagIsOpen = true;
		} catch (Exception e) {
			throw new SAXException(
					"Failed to output XML end tag: " + qName, e);
		}
	}

	private void writeIdentation(boolean addNewLine) throws IOException {
		
		if (addNewLine){
			writer.write("\n");
		}
		
		Integer numberOfSpaces = number_of_spaces_to_use_for_identation.peek();
		
		if (numberOfSpaces == null){
			writer.write(" ");
		}
		else{
			String spaces = String.format("%"+numberOfSpaces+"s", "");
			writer.write(spaces);
		}
		
	}

	public void writeAttribute(String attributeQName, String attributeValue) throws Exception{
		
		if (attributeValue != null){
			
			if (prettyPrint){
				writeIdentation(true);
				writer.write(" ");
			}

			writer.write(" ");
			writer.write(attributeQName);
			writer.write("=\"");
			char[] ch = attributeValue.toCharArray();
			write(ch, 0, ch.length, true);
			writer.write("\"");
		}
	}

	/**
	 * Escapes and outputs the given characters.
	 */
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		closeStartTagIfOpen();
		write(ch, start, length, false);
	}

	/**
	 * Escapes and outputs the given characters.
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException {
		characters(ch, start, length);
	}

	/**
	 * Outputs the specified end tag.
	 */
	public void endElement(
			String namespaceURI, String localName, String qName)
	throws SAXException {
		try {

			if (startTagIsOpen) {
				if (prettyPrint){
					elementHasJustEnded = true;
				}

				writer.write("/>");
				startTagIsOpen = false;
			
			} else {
				
				if (prettyPrint){
					number_of_spaces_to_use_for_identation.poll();
					if (elementHasJustEnded){
						writeIdentation(true);
					}
					addNewLine = true;
					elementHasJustEnded = true;
				}
				
				writer.write("</");
				writer.write(qName);
				writer.write(">");

			}
		} catch (IOException e) {
			throw new SAXException(
					"Failed to output XML end tag: " + qName, e);
		}
	}

	//--------------------------------------------------------------< Object >

	/**
	 * Returns the serialized XML document (assuming the default no-argument
	 * constructor was used).
	 *
	 * @param serialized XML document
	 */
	public String toString() {
		return writer.toString();
	}

	public void startElementWithOnlyOneAttribute(String qName, String attQName, String attValue) throws Exception 
	{
		closeStartTagIfOpen();
		try {
			
			if (prettyPrint){
				elementHasJustEnded = false;
				writeIdentation(addNewLine);
			}

			writer.write("<");
			writer.write(qName);

			writeAttribute(attQName, attValue);

			startTagIsOpen = true;
		} catch (IOException e) {
			throw new SAXException(
					"Failed to output XML end tag: " + qName, e);
		}
	}
	
	public Writer getWriter(){
		return writer;
	}

	@Override
	public void closeOpenElement() throws Exception {
		closeStartTagIfOpen();
		
	}

	@Override
	public void end() throws Exception{
		endDocument();
		
	}

	@Override
	public void endElement(String elementQName) throws Exception {
		endElement(null, elementQName, elementQName);
		
	}

	@Override
	public void start() throws Exception{
		startDocument();
	}

	@Override
	public void startElement(String elementQName,
			Attributes attributes) throws Exception{
		startElement(null, elementQName, elementQName, attributes);
		
	}

	@Override
	public void startElementWithNoAttributes(String elementQName) throws Exception {
		startElement(null, elementQName, elementQName, emptyAttributes);
		
	}

	@Override
	public void writeContent(char[] ch, int start, int length) throws Exception{
		characters(ch, start, length);
		
	}
}
