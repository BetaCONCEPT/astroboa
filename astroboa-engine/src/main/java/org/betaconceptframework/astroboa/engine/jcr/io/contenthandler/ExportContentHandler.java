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

import java.io.Writer;

import org.xml.sax.Attributes;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ExportContentHandler {

	/**
	 * Begin export process
	 */
	void start() throws Exception;
	
	/**
	 * End export process
	 */
	void end() throws Exception;
	
	/**
	 * Close open start tag.
	 * 
	 * This is useful in XML where you want to close
	 * the open element in order to write its text
	 */
	void closeOpenElement() throws Exception;
	
	/**
	 * Retrieve the internal writer used to write
	 * content
	 */
	Writer getWriter();
	
	/**
	 * Method used to write content without
	 * any other previous process by internal 
	 * content handler
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 */
	void writeContent(char[] ch, int start, int length) throws Exception;

	/**
	 * Write attribute
	 * @param attributeName
	 * @param attributeValue
	 */
	void writeAttribute(String attributeName, String attributeValue)  throws Exception;

	/**
	 * Start a new element
	 * 
	 * @param elementQName
	 * @param rootElementAttributes
	 */
	void startElement(String elementQName, Attributes attributes) throws Exception;
	
	/**
	 * Start an element with no attributes
	 * 
	 * @param elementQName
	 */
	void startElementWithNoAttributes(String elementQName) throws Exception;

	/**
	 * Start element with one attribute
	 * 
	 * @param elementQName
	 * @param attributeName
	 * @param attributeValue
	 */
	void startElementWithOnlyOneAttribute(String elementQName,
			String attributeName, String attributeValue) throws Exception ;

	/**
	 * End element
	 * 
	 * @param elementQName
	 */
	void endElement(String elementQName) throws Exception;
}
