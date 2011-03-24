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
package org.betaconceptframework.astroboa.engine.jcr.io;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.engine.jcr.dao.ImportDao;
import org.betaconceptframework.astroboa.model.jaxb.AstroboaValidationEventHandler;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.JSONHelper;
import com.sun.jersey.json.impl.reader.Jackson2StaxReader;
import com.sun.jersey.json.impl.reader.JacksonRootAddingParser;

/**
 * Entry point for importing XML or JSON to a {@link CmsRepositoryEntity}.
 * 
 * <p>
 * It provides necessary JAXB unmarshaller and XMLReader to be used
 * for unmarshalling either XML or JSON to a {@link CmsRepositoryEntity}.
 * 
 * </p>
 * 
 * <p>
 * See more at {@link ImportDao}.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsEntityDeserialization {
	
	Context;
	
	private SAXParserFactory saxParserFactory;
	
	private JsonFactory jsonFactory = new JsonFactory();
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ValidationEventHandler validationEventHandler = new AstroboaValidationEventHandler();

	private CmsEntityDeserialization(){
		try{
			
			saxParserFactory = SAXParserFactory.newInstance();
			
			saxParserFactory.setNamespaceAware(true);
			saxParserFactory.setValidating(false);
			
			saxParserFactory.setFeature("http://apache.org/xml/features/validation/schema",true);
			saxParserFactory.setFeature("http://xml.org/sax/features/validation", true);
			saxParserFactory.setFeature("http://xml.org/sax/features/namespaces",true);
			//saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			
			/*
			 * 
			 * A true value for this feature allows the encoding of the file to be specified as a 
			 * Java encoding name as well as the standard ISO encoding name. Be aware that other 
			 * parsers may not be able to use Java encoding names. If this feature is set to false, 
			 * an error will be generated if Java encoding names are used.  
			 */
			saxParserFactory.setFeature("http://apache.org/xml/features/allow-java-encodings",true);

		}
		catch(Exception e){
			logger.error("",e);
		}
	
	}
	
	public XMLReader createXMLReader(EntityResolver entityResolver, boolean enableValidation) throws Exception{
		SAXParser saxParser = saxParserFactory.newSAXParser();
		
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setEntityResolver(entityResolver);
		xmlReader.setFeature("http://apache.org/xml/features/validation/schema",enableValidation);
		xmlReader.setFeature("http://xml.org/sax/features/validation", enableValidation);

		
		//ValidationEventHandler is provided as an ErrorHandler in XMLReader
		// and as ValidationEventHandler in JAXB Unmarshaller (see #createUnMarshaller()).

		//If we set error handler in XMLReader only and use JAXB to unmarshal
		//then this error handler is ignored(!). However if we use XMLReader.parse
		//to unmarshal then it is taken into consideration.
		
		//This is why it is defined in both places so that either way
		//our custom error handler will be used
		xmlReader.setErrorHandler((ErrorHandler) validationEventHandler);
		
		return xmlReader;
	}

	
	public SAXSource createSAXSource(InputStream xml, EntityResolver repositoryEntityResolver, boolean enableValidation) throws Exception {

		XMLReader xmlReader = createXMLReader(repositoryEntityResolver, enableValidation);

		InputSource inputSource = new InputSource(xml);

		return  new SAXSource(xmlReader, inputSource);
	}

	public XMLStreamReader createJSONReader(InputStream source, boolean stripRoot, Class expectedType) throws Exception {
		
		JSONConfiguration configuration = JSONConfiguration.mapped()
															.rootUnwrapping(stripRoot)														
															.build();
			
        final JsonParser rawParser = jsonFactory.createJsonParser(new InputStreamReader(source, "UTF-8"));

        final JsonParser nonListParser = stripRoot ? JacksonRootAddingParser.createRootAddingParser(rawParser, JSONHelper.getRootElementName(expectedType)) 
        		: rawParser;

        return new Jackson2StaxReader(nonListParser, configuration);

	}
}
