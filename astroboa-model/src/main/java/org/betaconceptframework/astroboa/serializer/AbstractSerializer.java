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


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public abstract class AbstractSerializer {

	private StringWriter writer = new StringWriter();

	private boolean jsonOutput = true;
	
	private Serializer internalSerializer;

	public AbstractSerializer(boolean prettyPrint, boolean jsonOutput) {

		this.jsonOutput = jsonOutput;
		
		if (jsonOutput){
			internalSerializer = new JsonSerializer(writer, prettyPrint);
		}
		else{
			internalSerializer = new XmlSerializer(writer, prettyPrint);
		}

	}

	public void endArray(String name){
		
		internalSerializer.endArray(name);
	}
	
	public void startArray(String name) {

		internalSerializer.startArray(name);		
	}
	
	public void endElement(String name, boolean closeStartTag, boolean createCloseTag) {

		internalSerializer.endElement(name, closeStartTag, createCloseTag);
	}

	public void writeContent(String content, boolean escape){
		
		internalSerializer.writeContent(content, escape);
		
	}
	
	public void writeAttribute(String name, String value){
		internalSerializer.writeAttribute(name, value);
	}
	
	
	public void startElement(String name, boolean elementHasAttributes, boolean writeName) {
		internalSerializer.startElement(name, elementHasAttributes, writeName);
	}
	
	public String serialize() {
		return internalSerializer.serialize();
	}

	public boolean outputIsJSON(){
		return jsonOutput;
	}
	
	public boolean prettyPrintEnabled(){
		return internalSerializer.prettyPrintEnabled();
	}

	public String getCurrentValue(){
		return internalSerializer.getCurrentValue();
	}
}
