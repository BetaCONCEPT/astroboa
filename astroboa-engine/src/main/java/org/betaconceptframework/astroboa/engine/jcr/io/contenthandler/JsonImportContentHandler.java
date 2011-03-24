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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.IOUtils;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportContext;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * ContentHandler responsible to listen to SAX events during 
 * JSON import.
 * 
 * Its aim is to identify which elements represent attributes
 * in order to feed ImportContentHandler with the correct information
 * 
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JsonImportContentHandler<T> implements ContentHandler{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Deque<XMLElement> elementQueue = new ArrayDeque<XMLElement>();
	
	private ImportContentHandler<T> importContentHandler;

	private StringBuilder elementContent = new StringBuilder();

	private ImportContext importContext;
	
	private Deque<Boolean> currentElementRepresentsAnAttribute = new ArrayDeque<Boolean>();
	
	public JsonImportContentHandler(Class<T> resultType, Deserializer deserializer) {
		importContext = new ImportContext();
		importContentHandler = new ImportContentHandler<T>(importContext, resultType, deserializer);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		elementContent.append(ch, start, length);
		
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	
		if (currentElementRepresentsAnAttribute.poll()){
			IOUtils.addAttribute(elementQueue.peek().atts, localName, elementContent.toString());
		}
		else{

			XMLElement finishedElement = elementQueue.poll();
			
			if (finishedElement != null){
				
				finishedElement.content = elementContent.toString();
				
				//Forward Element to be imported normally
				if (elementQueue.isEmpty()){
					importElement(uri, localName, qName, finishedElement);
				}
				
				logger.debug("Removed element {} from the queue", localName);
				
				//Special case
				finishedElement.changeLocalNameIfItRepresentsAnItemOfAResourceCollection();
			}
		}
		
		clearElementContent();
	}

	private void importElement(String uri, String localName, String qName,
			XMLElement element) throws SAXException {
		
		importContentHandler.startElement(uri, localName, qName, element.atts);
		
		if (! element.childElements.isEmpty()){
			for (XMLElement childElement : element.childElements){
				importElement(uri, childElement.localName, childElement.localName, childElement);
			}
		}
		
		if (element.content != null){
			importContentHandler.characters(element.content.toCharArray(), 0, element.content.length());
		}
		
		importContentHandler.endElement(uri, localName, qName);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		String parentElementName = elementQueue.peek() != null ? elementQueue.peek().localName : null;
		
		if ( nameCorrespondsToAnElement(localName, parentElementName)){

			XMLElement element = new XMLElement();
			element.localName = localName;
			
			if (! elementQueue.isEmpty()){

				XMLElement parentElement = elementQueue.peek();
				
				parentElement.childElements.add(element);
				
			}
			
			elementQueue.push(element);
			
			currentElementRepresentsAnAttribute.push(false);

			logger.debug("Pushed element {} to queue",localName);
			
		}
		else{
			currentElementRepresentsAnAttribute.push(true);
		}
		
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		importContentHandler.endPrefixMapping(prefix);
		
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		importContentHandler.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		importContentHandler.processingInstruction(target, data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		importContentHandler.setDocumentLocator(locator);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		importContentHandler.skippedEntity(name);
	}

	@Override
	public void startDocument() throws SAXException {
		importContentHandler.startDocument();
	}
	
	@Override
	public void endDocument() throws SAXException {
		
		elementQueue.clear();
		
		
		importContentHandler.endDocument();
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		importContentHandler.startPrefixMapping(prefix, uri);
		
	}
	
	public T getResult(){
		return importContentHandler.getImportResult();
	}
	
	private class XMLElement{
		
		private String localName;
		
		private AttributesImpl atts = new AttributesImpl();
		
		private String content;

		private List<XMLElement> childElements = new ArrayList<XMLElement>();

		public void changeLocalNameIfItRepresentsAnItemOfAResourceCollection() {
			if (StringUtils.equals(CmsConstants.CONTENT_OBJECTS_ELEMENT_NAME, localName) ||
				(StringUtils.equals(CmsConstants.RESOURCE_COLLECTION, localName) && atts.getIndex(CmsBuiltInItem.ContentObjectTypeName.getLocalPart())!=-1)
			){
				localName = atts.getValue(CmsBuiltInItem.ContentObjectTypeName.getLocalPart());
			}
		}
	}
	
	private void clearElementContent() {
		elementContent.delete(0, elementContent.length());
	}


	private boolean elementNameIsLabel(String element) {
		return element != null && StringUtils.equals(element, CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME);
	}

	private boolean nameCorrespondsToAnElement(String elementName, String parentElementName){
		
		//Checking if elementName is one of the built in attribute names
		boolean nameCorrespondsToAnAttribute = importContext.nameCorrespondsToAnAttribute(elementName);

		if (nameCorrespondsToAnAttribute && elementNameIsLabel(elementName)){
			//Checking if elementName == 'label' and parentElementName == 'localization'
			//There is an attribute 'label' of the 'repositoryUser' element. We need to distinguish this 
			//from the 'label' element of the 'localization' element.
			if (StringUtils.equals(CmsBuiltInItem.Localization.getLocalPart(),parentElementName)){
				return true;
			}
		}

		//Checking if parentElementName == 'label' which means that elementName is a lang code , 'el', 'en' ,etc
		if (! nameCorrespondsToAnAttribute && elementNameIsLabel(parentElementName)){
			return false;
		}
		
		return ! nameCorrespondsToAnAttribute;
	}
}
