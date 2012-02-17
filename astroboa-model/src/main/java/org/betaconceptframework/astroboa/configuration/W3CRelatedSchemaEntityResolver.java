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
package org.betaconceptframework.astroboa.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * When the repository registry is initialized, Astroboa configuration file is loaded and validated
 * against the configuration XML Schema. 
 * 
 * During the validation process, XSDs are validated by the parser. Part of the validation process 
 * is to import all necessary XSDs or DTDs dependencies.
 * Some of these XSDs (XML Schema's XSD, etc) or DTDs are located externally and thus, can be imported 
 * only when an Internet access
 * is available. When this is not the case, XSD parser cannot complete validation and throws an exception.
 *
 * To avoid this situation (which causes a build to fail or even worse does not allow Astroboa to run), 
 * Astroboa identifies whether these files can be accessed and if they cannot, it feeds the parser with 
 * their copy which is located in this directory. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class W3CRelatedSchemaEntityResolver implements EntityResolver, LSResourceResolver {

	
	private String xmlSchemaHomeDir =  CmsConstants.FORWARD_SLASH+"META-INF"+CmsConstants.FORWARD_SLASH+"xml-schema-dtd";
	
	private DOMImplementationRegistry registry;
	
	private Map<String, URL> schemaURLsPerPublicId = new HashMap<String, URL>();
	
	
	public W3CRelatedSchemaEntityResolver(){
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			throw new CmsException(e);
		}
	}

	/**
	 * Resolves Entities with the following properties
	 * 
	 * publicId = http://www.w3.org/XML/1998/namespace
	 * systemId = http://www.w3.org/2001/03/xml.xsd
	 * 
	 * publicId = datatypes
	 * systemId = http://www.w3.org/2001/03/datatypes.dtd
	 * 
	 * publicId = -//W3C//DTD XMLSCHEMA 200102//EN
	 * systemId = http://www.w3.org/2001/03/XMLSchema.dtd
	 * 
	 * If above schemata are not reachable (probably due to a lack of Internet access),
	 * they are loaded from the package
	 * 
	 * @return
	 * @throws IOException 
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		
		if (StringUtils.equals(systemId, CmsConstants.XML_SCHEMA_LOCATION)){
			return locateEntity(CmsConstants.XML_SCHEMA_LOCATION, XMLConstants.XML_NS_URI);
		}
		
		if (StringUtils.equals(systemId, CmsConstants.XML_SCHEMA_DTD_LOCATION)){
			return locateEntity(CmsConstants.XML_SCHEMA_DTD_LOCATION, "-//W3C//DTD XMLSCHEMA 200102//EN");
		}
		
		if (StringUtils.equals(systemId, CmsConstants.XML_DATATYPES_DTD_LOCATION)){
			return locateEntity(CmsConstants.XML_DATATYPES_DTD_LOCATION, "datatypes");
		}
		
		return null;
	}

	private InputSource locateEntity(String systemId, String publicId) throws IOException{
		
		URL xsdOrDtdLocation = null;

		if (publicId != null && schemaURLsPerPublicId.containsKey(publicId)){
			xsdOrDtdLocation = schemaURLsPerPublicId.get(publicId);
		}

		if (systemId == null){
			return null;
		}
			
		String xsdOrDtdFilename =  
				(systemId.contains(CmsConstants.FORWARD_SLASH) ? StringUtils.substringAfterLast(systemId, CmsConstants.FORWARD_SLASH) : systemId);

		//Check if schema is available locally
		if (xsdOrDtdLocation == null){
			xsdOrDtdLocation = this.getClass().getResource(xmlSchemaHomeDir+CmsConstants.FORWARD_SLASH+xsdOrDtdFilename); 
		}
		
		//Try on the WEB
		if (xsdOrDtdLocation == null){
			xsdOrDtdLocation = new URL(systemId);
		}
		
		try {
			InputSource is = new InputSource( xsdOrDtdLocation.openStream() );
				
			//System Id is the path of this URL
			is.setSystemId(xsdOrDtdLocation.toString());
			is.setPublicId(publicId);
			
			schemaURLsPerPublicId.put(publicId, xsdOrDtdLocation);

			return is;
		}
		catch (Throwable isEx) {
				
			//Log a warning and let the Xerces parser to locate the schema
			LoggerFactory.getLogger(getClass()).warn("Unable to resolve schema for "+publicId + " "+ systemId + " in URL "+xsdOrDtdLocation.toString(), isEx);

			//continue with parser provided by Java. If schema cannot be located, an exception will be thrown
			return null;
		}
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI,
			String publicId, String systemId, String baseURI) {
		
		InputSource entity;
		
		try {
			entity = locateEntity(systemId, publicId);
		} catch (IOException e) {
			throw new CmsException(e);
		}
		
		if (entity == null || entity.getByteStream() == null){
			return null;
		}
		
		DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
		
		LSInput lsInput = domImplementationLS.createLSInput();
		
		lsInput.setByteStream(entity.getByteStream());
		lsInput.setSystemId(systemId);

		return lsInput;
	}
}
