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

package org.betaconceptframework.astroboa.serializer;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class XmlSerializer implements Serializer{

	private boolean prettyPrint = false;

	private Deque<Integer> number_of_spaces_to_use_for_identation = new ArrayDeque<Integer>();

	private StringWriter writer;

	public XmlSerializer(StringWriter writer,  boolean prettyPrint) {

		this.prettyPrint = prettyPrint;
		
		this.writer = writer;

		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");

		number_of_spaces_to_use_for_identation.push(1);

	}

	public void endArray(String name){
		endElement(name, false, true);
		
	}
	
	public void startArray(String name) {
		
		startElement(name, false, true);
		
	}
	
	public void endElement(String name, boolean closeStartTag, boolean createCloseTag) {

		if (closeStartTag && createCloseTag){

			writer.append("/>");
			
			if (prettyPrint){
				number_of_spaces_to_use_for_identation.poll();
			}

		}
		else{
			//In XML, closing an element can mean either close the start tag or end element

			//When the element needs to end, user must specify if she wants to print a close tag

			if (closeStartTag){
				writer.append(">");
			}

			if (createCloseTag){

				if (prettyPrint){
					writeIdentation(true);
					number_of_spaces_to_use_for_identation.poll();
				}

				writer.append("</");
				writer.append(name);
				writer.append(">");
			}
		}
	}

	private void write(char[] ch, int start, int length, boolean attribute){
		for (int i = start; i < start + length; i++) {
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
		}
	}

	private void writeIdentation(boolean addNewLine) {

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

	public void writeAttribute(String name, String value){

		if (prettyPrint){
			writeIdentation(true);
			writer.write(" ");
		}

		writer.write(" ");
		writer.write(name);
		writer.write("=\"");
		char[] ch = value.toCharArray();
		write(ch, 0, ch.length, true);
		writer.write("\"");
	}
	
	
	public void startElement(String name, boolean elementHasAttributes, boolean writeName) {

		if (prettyPrint){
			increaseNumberOfSpacesToUseForIndentation();
			writeIdentation(true);
		}

		writer.append("<");
		writer.append(name);
		
		if (! elementHasAttributes){
			writer.append(">");
		}

	}
	
	private void increaseNumberOfSpacesToUseForIndentation() {
		if (number_of_spaces_to_use_for_identation.isEmpty()){
			number_of_spaces_to_use_for_identation.push(1);
		}
		else{
			number_of_spaces_to_use_for_identation.push(number_of_spaces_to_use_for_identation.peek()+3);
		}
	}


	public String serialize() {
		
		return writer.toString();
	}

	public boolean prettyPrintEnabled(){
		return prettyPrint;
	}

	public String getCurrentValue(){
		
		return writer.toString();
	}
	
	public void writeContent(String content, boolean escape){
		
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
