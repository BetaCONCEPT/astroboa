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
package org.betaconceptframework.astroboa.engine.definition;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.engine.definition.xsom.EntityResolverForBuiltInSchemas;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDefinitionDao;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity Resolver for REPOSITORY schemas defined in a repository. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryEntityResolver implements EntityResolver{

	@Autowired
	private ContentDefinitionDao contentDefinitionDao;
	
	@Autowired
	private EntityResolverForBuiltInSchemas entityResolverForBuiltInSchemas; 

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
	throws SAXException, IOException {
		
		if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(systemId) ||
				XMLConstants.XML_NS_URI.equals(systemId) ||
				CmsConstants.XML_SCHEMA_LOCATION.equals(systemId) ||
				CmsConstants.XML_SCHEMA_DTD_LOCATION.equals(systemId)){
			return entityResolverForBuiltInSchemas.resolveXmlSchemaRelatedToW3C(publicId, systemId);
		}

		byte[] schema = getSchema(systemId);
		
		if (schema == null){
			return null;
		}
		
		InputSource is = new InputSource(new ByteArrayInputStream(schema));

		is.setSystemId(systemId);
		is.setPublicId(publicId);

		return is;

	}

	private byte[] getSchema(String systemId) {
		if (StringUtils.isBlank(systemId)){
			return null;
		}
		
		
		//We are only interested in content type name or path.
		String schemaFilename = systemId;
		
		//We expect URL of the form
		//http://<server>/resource-api/astroboa/repository/<repository-id>/definition/multilingualStringPropertyType?output=xsd
		//Definition name is located after the last forward slash
		if (schemaFilename.contains(CmsConstants.FORWARD_SLASH)){
			schemaFilename = StringUtils.substringAfterLast(schemaFilename, CmsConstants.FORWARD_SLASH);
		}
		
		if (schemaFilename.contains("?")){
			schemaFilename = StringUtils.substringBefore(schemaFilename, "?");
		}

		byte[] schema = contentDefinitionDao.getXMLSchemaFileForDefinition(schemaFilename);

		if (schema == null || schema.length == 0){
			return null;
		}
		
		return schema;
	}

}
