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

package org.betaconceptframework.astroboa.engine.definition.xsom;


import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.engine.definition.XSSchemaItem;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsDefinitionItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsAnnotationHandler  implements ContentHandler{

	private CmsAnnotation cmsAnnotation;
	
	private String currentLocale = null;
	private StringBuilder currentValueForLocale;
	
	
	public CmsAnnotationHandler() {
		cmsAnnotation = new CmsAnnotation();
		currentValueForLocale = new StringBuilder();
	}

	
	public CmsAnnotation getCmsAnnotation() {
		return cmsAnnotation;
	}


	public void characters(char[] ch, int start, int length) throws SAXException {
		String data = new String(ch, start, length);
		
		addData(data);
		
	}


	private void addData(String data) {
		data = StringUtils.trimToNull(data);

		if (data != null && currentLocale != null)
		{
			if (currentValueForLocale == null)
			{
				currentValueForLocale = new StringBuilder();
			}
			
			if (currentValueForLocale.length() >0){
				currentValueForLocale.append(" ");
			}
			
			currentValueForLocale.append(data);
		}
	}

	public void endDocument() throws SAXException {
		 
		
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		ItemQName element = ItemUtils.createNewItem(null, uri, localName);
		
		if (CmsDefinitionItem.displayName.equals(element))
		{
			if (currentLocale != null){
				cmsAnnotation.addDisplayNameForLocale(currentLocale, currentValueForLocale.toString());
			}
			
			currentValueForLocale = null;
		}
		else if (CmsDefinitionItem.description.equals(element))
		{
			if (currentLocale != null){
				cmsAnnotation.addDescriptionForLocale(currentLocale, currentValueForLocale.toString());
			}
			
			currentValueForLocale = null;
		}
		else if (XSSchemaItem.Documentation.equals(element))
		{
			if (currentLocale != null && currentValueForLocale != null){
				
				if (cmsAnnotation.getDisplayName().getLocalizedLabelForLocale(currentLocale) == null)
				{
					cmsAnnotation.addDisplayNameForLocale(currentLocale, currentValueForLocale.toString());
				}
				
				if (cmsAnnotation.getDescription().getLocalizedLabelForLocale(currentLocale) == null)
				{
					cmsAnnotation.addDescriptionForLocale(currentLocale, currentValueForLocale.toString());
				}
			}
			
			currentLocale = null;
			currentValueForLocale = null;
		}
		else{
			//It's probably an xml element inside documentation
			//Load it as is, but we have to prefixed and suffixed it with < and >
			//Use qname in case element is prefixed
			addData("</"+qName+">");
		}

		
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		 
		
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		 
		
	}

	public void processingInstruction(String target, String data) throws SAXException {
		 
		
	}

	public void setDocumentLocator(Locator locator) {
		 
		
	}

	public void skippedEntity(String name) throws SAXException {
		 
	}

	public void startDocument() throws SAXException {
		 
		
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		
		ItemQName element = ItemUtils.createNewItem(null, uri, localName);
		
		if (XSSchemaItem.Annotation.equals(element))
		{
			cmsAnnotation.getDisplayName().clearLocalizedLabels();
			cmsAnnotation.getDescription().clearLocalizedLabels();
			currentLocale = null;
			currentValueForLocale = null;
		}
		else if (CmsDefinitionItem.displayName.equals(element) || 
				CmsDefinitionItem.description.equals(element))
		{
			currentValueForLocale = new StringBuilder();
		}
		else if (XSSchemaItem.Documentation.equals(element))
		{
			//Here only attribute about language can be found
			if (atts != null)
			{
				String language = atts.getValue(XSSchemaItem.Lang.getJcrName());
				if (StringUtils.isNotBlank(language))
				{
					currentLocale = language;
				}
				else
				{
					createDefaultLocale();
				}

			}
			else
			{
				createDefaultLocale();
			}
		}
		else{
			//It's probably an xml element inside documentation
			//Load it as is, but we have to prefixed and suffixed it with < and >
			//Use qname in case element is prefixed
			addData("<"+qName+">");
		}
	}

	private void createDefaultLocale() {
		currentLocale = Locale.ENGLISH.toString();
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		 
		
	}

}
