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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.configuration.W3CRelatedSchemaEntityResolver;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Used to resolve imports from definition xsd files
 * and mainly to provide the right xsd definition file
 * for internal astroboa types
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class EntityResolverForBuiltInSchemas implements EntityResolver{

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	private W3CRelatedSchemaEntityResolver w3cRelatedSchemaEntityResolver = new W3CRelatedSchemaEntityResolver();
	
	private String builtInDefinitionSchemaHomeDir;

	public void setBuiltInDefinitionSchemaHomeDir(
			String builtInDefinitionSchemaHomeDir) {
		this.builtInDefinitionSchemaHomeDir = builtInDefinitionSchemaHomeDir;
	}

	public String getBuiltInDefinitionSchemaHomeDir() {
		return builtInDefinitionSchemaHomeDir;
	}

	@Override
	/**
	 * According to XSOM library 
	 * By setting EntityResolver to XSOMParser, you can redirect <xs:include>s and <xs:import>s to different resources.
	 * For imports, the namespace URI of the target schema is passed as the public ID,
	 * and the absolutized value of the schemaLocation attribute will be passed as the system ID. 
	 */
	public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException {

		if (publicId != null && publicId.startsWith(BetaConceptNamespaceConstants.ASTROBOA_SCHEMA_URI) && 
				systemId != null){

			try {
				
				String schemaFilename = systemId;
				
				//We are only interested in file name
				if (schemaFilename.contains(CmsConstants.FORWARD_SLASH)){
					schemaFilename = StringUtils.substringAfterLast(systemId, CmsConstants.FORWARD_SLASH);
				}
				
				
				URL definitionFileURL = locateBuiltinDefinitionURL(schemaFilename);

				if (definitionFileURL != null){
					
					InputSource is = new InputSource( definitionFileURL.openStream() );
					
					
					/*
					 * SystemId is actual the path to the resource
					 * although its content has been loaded to input source.
					 * Provided value (sustemId) is not set because if in XSD file in the corresponding import's
					 * schemaLocation contains only the schema filename, then XSOM parser will
					 * consider it as a relative path and will prefix it with the correct absolute path.
					 * 
					 * For example, in cases where two different schemas, located in two different directories (portal-1.0.xsd and basicText-1.0.xsd),
					 * import schema astroboa-model-1.2.xsd, xs import will look like
					 * 
					 * <xs:import
							namespace="http://www.betaconceptframework.org/schema/astroboa/model"
							schemaLocation="astroboa-model-1.2.xsd" />
					 * 
					 * When XSOM parser will try to load astroboa-model-1.2.xsd, it will append
					 * schemaLocation with the path of the directory where each of the parent XSDs are located. 
					 * 
					 * SchemaLocation value refers to systemId and if it is provided in this input source, XSOM parser
					 * will have two different instances of InputSource referring to the same xsd file  (definitionFileURL),
					 * having the same publiId (namespace) but different systemIds (appended schemaLocation).
					 * 
					 * This situation causes XSOM parser to throw an exception when the second input source is loaded
					 * complaining that it found the same type(s) defined already, which is true since as far as XSOM parser
					 * concerns these input sources are not the same.
					 * 
					 * To overcome this, we set as system id the URL of the XSD file which is the same
					 * in both situations.
					 */
					is.setSystemId(definitionFileURL.toString());
					is.setPublicId(publicId);

					if (systemId.startsWith(BetaConceptNamespaceConstants.ASTROBOA_SCHEMA_URI)){
						logger.warn("Schema Location for XSD Schema "+ schemaFilename + ", which contains built in Astroboa model, is not relative but absolute." +
								" Unless this absolute location really 'serves' XSD, there will be a problem " +
								" when importing XML which contain xml elements derived from this Schema If this location is not real, then you are advised to delete it and leave only" +
								"the schema file name. Nevertheless the contents of "+ schemaFilename+ " have been found internally and have been successfully loaded to Astroboa");
					}
					
					return is;
				}

			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		//Returning null allow XSD Parser to continue with default behavior
		//and will try to resolve entity using its own implementation
		return resolveXmlSchemaRelatedToW3C(publicId, systemId);
	}

	public URL locateBuiltinDefinitionURL(String schemaFilename) throws IOException,
	Exception {
		
		//Any other file will reside in /META-INF/builtin-definition-schemas-directory
		return loadDefinitionFile(builtInDefinitionSchemaHomeDir+CmsConstants.FORWARD_SLASH+schemaFilename);
	}

	public URL loadDefinitionFile(String absolutePath) throws Exception {

		return EntityResolverForBuiltInSchemas.class.getResource(absolutePath);

	}
	
	/**
	 * Resolves Entities with the following properties
	 * 
	 * publicId = http://www.w3.org/XML/1998/namespace
	 * systemId = http://www.w3.org/2001/03/xml.xsd
	 * 
	 * publicId=-//W3C//DTD XMLSCHEMA 200102//EN
	 * systemId=http://www.w3.org/2001/03/XMLSchema.dtd
	 * 
	 * publicId = -//W3C//DTD XMLSCHEMA 200102//EN
	 * systemId = http://www.w3.org/2001/03/XMLSchema.dtd
	 * 
	 * If above schemata are not reachable (probably due to a lack of Internet access),
	 * they are loaded from the package
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public InputSource resolveXmlSchemaRelatedToW3C(String publicId, String systemId) throws IOException, SAXException{
		
		return w3cRelatedSchemaEntityResolver.resolveEntity(publicId, systemId);
	}
}
