/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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
package org.betaconceptframework.astroboa.test.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JAXBValidationUtils  {

	private EntityResolver  entityResolver;

	private SAXParserFactory parserFactory;

	private DefaultErrorHandler errorHandler;

	private String astroboaVersion  = null;
	
	public JAXBValidationUtils(EntityResolver entityResolver, String astroboaVersion) throws Exception{

		this.entityResolver = entityResolver;
		this.astroboaVersion = astroboaVersion;

		//Create SAX Parser
		parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(false);
		parserFactory.setFeature("http://apache.org/xml/features/validation/schema",true);
		parserFactory.setFeature("http://xml.org/sax/features/validation", true);
		parserFactory.setFeature("http://xml.org/sax/features/namespaces",true);
		//parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true); This feature is not recognized
		
		/*
		 * 
		 * A true value for this feature allows the encoding of the file to be specified as a 
		 * Java encoding name as well as the standard ISO encoding name. Be aware that other 
		 * parsers may not be able to use Java encoding names. If this feature is set to false, 
		 * an error will be generated if Java encoding names are used.  
		 */
		parserFactory.setFeature("http://apache.org/xml/features/allow-java-encodings",true);

		errorHandler = new DefaultErrorHandler();
		
	}

	public void validateUsingSAX(String xml) throws Exception {
	
		if (astroboaVersion != null && xml.contains(CmsConstants.ASTROBOA_VERSION)){
			xml = xml.replaceAll("\\$\\{project\\.version\\}", astroboaVersion);
		}

		InputStream is = IOUtils.toInputStream(xml, "UTF-8");
		
		validateUsingSAX(is);
		
		IOUtils.closeQuietly(is);
		
	}

	public void validateUsingSAX(InputStream is) throws Exception
	{
		SAXParser saxParser = parserFactory.newSAXParser();
		
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setEntityResolver(entityResolver);
		xmlReader.setErrorHandler(errorHandler);
		
		errorHandler.setIgnoreInvalidElementSequence(false);
		
		is = encodeURLsFoundInXML(is);
		
		xmlReader.parse(new InputSource(is));
	}
	
	private InputStream encodeURLsFoundInXML(InputStream is) throws IOException {
		String xml = IOUtils.toString(is);
		
		xml = xml.replaceAll("\\[", "%5B");
		xml = xml.replaceAll("\\]", "%5D");

		return IOUtils.toInputStream(xml);
	}

	private class DefaultErrorHandler implements ErrorHandler{

		private boolean ignoreInvalidElementSequence;
		
		public void setIgnoreInvalidElementSequence(boolean ignoreInvalidElementSequence) {
			this.ignoreInvalidElementSequence = ignoreInvalidElementSequence;
		}


		@Override
		public void error(SAXParseException exception) throws SAXException {
			
			if (ignoreInvalidElementSequence && exception.getMessage() != null &&
					exception.getMessage().contains("cvc-complex-type.2.4.a"))
			{
				//Do nothing ignore
			}
			else
			{
				exception.printStackTrace();
				throw exception;
			}

		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			exception.printStackTrace();
			throw exception;

		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			exception.printStackTrace();
			throw exception;

		}


	}

}
