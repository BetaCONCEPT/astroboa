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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
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
	
		XMLElement finishedElement = elementQueue.poll();
			
		if (finishedElement != null){
			
			finishedElement.content = elementContent.toString();
			
			//Special cases. These built in attributes must be known when creating the actual
			//repository entity which will represent this element
			if (StringUtils.equalsIgnoreCase(CmsBuiltInItem.ContentObjectTypeName.getLocalPart(),localName) ||
					StringUtils.equalsIgnoreCase(CmsBuiltInItem.CmsIdentifier.getLocalPart(),localName) ||
					StringUtils.equalsIgnoreCase(CmsBuiltInItem.Name.getLocalPart(),localName) ||
					StringUtils.equalsIgnoreCase(CmsBuiltInItem.SystemName.getLocalPart(),localName) ||
					StringUtils.equalsIgnoreCase(CmsBuiltInItem.ExternalId.getLocalPart(),localName)){
				
				if (elementQueue.isEmpty()){
					throw new CmsException("Element "+localName + " represents a built in attribute but no parent element found");
				}
				else{
					if (StringUtils.isBlank(finishedElement.content)){
						if (! StringUtils.equalsIgnoreCase(CmsBuiltInItem.Name.getLocalPart(),localName)){
							throw new CmsException("Element "+localName+ " must have a value");
						}
					}
					else{

						XMLElement parentElement = elementQueue.peek();
	
						IOUtils.addAttribute(parentElement.attributes, localName, finishedElement.content.trim());

						parentElement.childElements.remove(finishedElement);

					}
				}
			}
			else{
				//Forward Element to be imported normally
				if (elementQueue.isEmpty()){
					importElement(uri, localName, qName, finishedElement);
				}
			}
			
			logger.debug("Removed element {} from the queue", localName);
			
		}
		
		clearElementContent();
	}
	
	/*
	private void printAttributes(Attributes atts) throws SAXException {
		
		if (atts != null && atts.getLength() >0){
			for (int i=0;i<atts.getLength();i++){
				
				logger.debug(atts.getLocalName(i)+ ":"+ atts.getValue(i));
			}
		}
		
	}*/


	private void importElement(String uri, String localName, String qName,
			XMLElement element) throws SAXException {
		
		importContentHandler.startElement(uri, localName, qName, element.attributes);
		
		if (! element.childElements.isEmpty()){

			Object entityCurrentlyImported = importContentHandler.getCurrentEntityImported();
			
			boolean elementContainsTheLabelsOfAnEntity = 
				CmsBuiltInItem.Localization.getLocalPart().equalsIgnoreCase(localName) && entityCurrentlyImported != null &&
				entityCurrentlyImported instanceof Localization ;
			
			for (XMLElement childElement : element.childElements){
				
				entityCurrentlyImported = importContentHandler.getCurrentEntityImported();
				
				//Check if element represents a built in attribute
			    if (elementContainsTheLabelsOfAnEntity && CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME.equalsIgnoreCase(childElement.localName) && 
			    		CollectionUtils.isNotEmpty(childElement.childElements)){
			    	
			    	for (XMLElement childElementOfLabel : childElement.childElements){
			    		((Localization) entityCurrentlyImported).addLocalizedLabel(childElementOfLabel.localName, childElementOfLabel.content);
			    	}
			    }
			    else if (childElement.childElements.isEmpty()  && 
						importContext.nameCorrespondsToAnAttribute(childElement.localName, entityCurrentlyImported)){
				
					importContentHandler.addAttributeToImportedEntity(childElement.localName, childElement.content);
				}
			    else if (entityCurrentlyImported != null && entityCurrentlyImported instanceof RepositoryUser && CmsConstants.URL_ATTRIBUTE_NAME.equalsIgnoreCase(childElement.localName)){
			    	continue; //Do not import attribute URL for RepositoryUser entity as it is not yet supported
			    }
				else{
					importElement(uri, childElement.localName, childElement.localName, childElement);
				}
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
		
		XMLElement element = new XMLElement();
		element.localName = localName;
		
		if (! elementQueue.isEmpty()){

			XMLElement parentElement = elementQueue.peek();
			
			parentElement.childElements.add(element);
			
		}
		
		elementQueue.push(element);
		
		logger.debug("Pushed element {} to queue",localName);
		
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
		
		private String content;
		
		//Built in attribute values
		private AttributesImpl attributes = new AttributesImpl();

		private List<XMLElement> childElements = new ArrayList<XMLElement>();
		
		public String toString(){
			return localName; 
		}
	}
	
	private void clearElementContent() {
		elementContent.delete(0, elementContent.length());
	}

}
