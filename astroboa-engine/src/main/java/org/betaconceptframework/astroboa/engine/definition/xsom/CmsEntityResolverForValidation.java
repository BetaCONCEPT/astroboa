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
package org.betaconceptframework.astroboa.engine.definition.xsom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Used to resolve imports from definition xsd files
 * and mainly to provide the right xsd definition file
 * when validating definition files. 
 * 
 * This resolver must have all definitions inside a map.
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsEntityResolverForValidation implements EntityResolver{

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, String> definitionSources = new HashMap<String, String>();
	
	private List<InputStream> openStreams = new ArrayList<InputStream>();

	private EntityResolverForBuiltInSchemas entityResolverForBuiltInSchemas;
	
	@Override
	/**
	 * According to XSOM library 
	 * By setting EntityResolver to XSOMParser, you can redirect <xs:include>s and <xs:import>s to different resources.
	 * For imports, the namespace URI of the target schema is passed as the public ID,
	 * and the absolutized value of the schemaLocation attribute will be passed as the system ID. 
	 */
	public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException {

		if (systemId != null){

			try {
				
				String schemaFilename = systemId;
				
				//We are only interested in file name
				if (schemaFilename.contains(CmsConstants.FORWARD_SLASH)){
					schemaFilename = StringUtils.substringAfterLast(systemId, CmsConstants.FORWARD_SLASH);
				}
				
				if (definitionSources.containsKey(schemaFilename)){
					InputStream inputStream = IOUtils.toInputStream(definitionSources.get(schemaFilename), "UTF-8");
					openStreams.add(inputStream);
					
					InputSource inputSource = new InputSource(inputStream);
					
					inputSource.setPublicId(publicId);
					inputSource.setSystemId(schemaFilename);
					
					return inputSource;					
				}

				return entityResolverForBuiltInSchemas.resolveEntity(publicId, systemId);

			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		//Returning null allow XSD Parser to continue with default behavior
		//and will try to resolve entity using its own implementation
		return null;
	}

	public void clearDefinitions(){
		
		if (! openStreams.isEmpty()){
			for (InputStream openStream : openStreams){
				IOUtils.closeQuietly(openStream);
			}
		}
		
		definitionSources.clear();
		openStreams.clear();
	}

	public void addExternalDefinition(File definitionFile) throws IOException {
		if (definitionFile != null){
			definitionSources.put(definitionFile.getName(), FileUtils.readFileToString(definitionFile, "UTF-8"));
		}
	}

	public void addDefinition(String definitionName,
			String definition) throws IOException {
		if (definitionName != null && definition != null){
			
			if (definitionName.contains(CmsConstants.FORWARD_SLASH)){
				definitionSources.put(StringUtils.substringAfterLast(definitionName, CmsConstants.FORWARD_SLASH), definition);
			}
			else{
				definitionSources.put(definitionName, definition);
			}
		}
		
	}
	
	public Map<String,String> getDefinitionSources(){
		return definitionSources;
	}

	public void setBuiltInEntityResolver(
			EntityResolverForBuiltInSchemas entityResolverForBuiltInSchemas) {
		this.entityResolverForBuiltInSchemas = entityResolverForBuiltInSchemas;
		
	}
}
