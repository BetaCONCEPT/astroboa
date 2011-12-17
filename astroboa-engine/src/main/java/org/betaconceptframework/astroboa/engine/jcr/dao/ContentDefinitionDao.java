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
package org.betaconceptframework.astroboa.engine.jcr.dao;

import java.io.File;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ContentObjectTypeDefinitionImpl;
import org.betaconceptframework.astroboa.service.dao.DefinitionServiceDao;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentDefinitionDao extends DefinitionServiceDao{

	private  final Logger logger = LoggerFactory.getLogger(ContentDefinitionDao.class);

	public byte[] getXMLSchemaFileForDefinition(
			String definitionFullPath) {

		try {
			if (StringUtils.isBlank(definitionFullPath)){
				return null;
			}

			if (definitionFullPath.endsWith(".dtd")){
				return null;
			}

			if (StringUtils.equals(CmsConstants.ASTROBOA_API_SCHEMA_FILENAME, definitionFullPath)){
				definitionFullPath = CmsConstants.ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION;
			}
			else if (StringUtils.equals(CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME, definitionFullPath)){
				definitionFullPath = CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION;
			}
			
			if (definitionFullPath.endsWith(".xsd")){

				//First try directly
				byte[] schema = definitionCacheRegion.getXMLSchemaForDefinitionFilename(definitionFullPath);

				if (schema == null || schema.length == 0){
					//Probably  an internal complex cms property

					//Replace version info along with xsd suffix
					definitionFullPath = definitionFullPath.replaceAll("-.*\\.xsd", "");

					//In case no version info exists just remove xsd suffix
					definitionFullPath = StringUtils.removeEnd(definitionFullPath, ".xsd");
				}
				else{
					return schema;
				}
			}

			CmsDefinition cmsDefinition = retrieveDefinition(definitionFullPath);

			URI cmsDefinitionFileURI = null;

			if (cmsDefinition instanceof ContentObjectTypeDefinition){
				cmsDefinitionFileURI = ((ContentObjectTypeDefinitionImpl)cmsDefinition).getDefinitionFileURI();
			}
			else if (cmsDefinition instanceof ComplexCmsPropertyDefinition){
				cmsDefinitionFileURI = ((ComplexCmsPropertyDefinitionImpl)cmsDefinition).getDefinitionFileURI();
			}
			else if (cmsDefinition instanceof SimpleCmsPropertyDefinition){
				//It is a simple property. Get its parent to provide with the URI
				CmsDefinition parentDefinition = ((SimpleCmsPropertyDefinition)cmsDefinition).getParentDefinition();
				if (parentDefinition != null && parentDefinition instanceof ComplexCmsPropertyDefinition){
					cmsDefinitionFileURI = ((ComplexCmsPropertyDefinitionImpl)parentDefinition).getDefinitionFileURI();
				}
			}

			if (cmsDefinitionFileURI == null){
				logger.warn("Found no XML schema file for definition "+ definitionFullPath);
				return null;
			}


			logger.debug("Searching XML schema file for definition {}", cmsDefinitionFileURI);
			
			
			String schemaFilename = StringUtils.substringAfterLast(cmsDefinitionFileURI.toString(), File.separator);
			
			if (StringUtils.isBlank(schemaFilename)){
				if (!File.separator.equals(CmsConstants.FORWARD_SLASH)){
					//try with forward slash.
					schemaFilename = StringUtils.substringAfterLast(cmsDefinitionFileURI.toString(), CmsConstants.FORWARD_SLASH);
				}
			}
			
			if (StringUtils.isBlank(schemaFilename)){
				logger.warn("Could not retrieve XML Schema filename from URI {}", cmsDefinitionFileURI.toString());
				return null;
			}
			
			return definitionCacheRegion.getXMLSchemaForDefinitionFilename(schemaFilename);

		} catch (Exception e) {
			logger.error("",e);
			throw new CmsException(e.getMessage());
		}
	}

	public <T> T getCmsDefinition(String fullPropertyDefinitionPath, ResourceRepresentationType<T> output, boolean prettyPrint) throws Exception {
		if (output == null){
			output = (ResourceRepresentationType<T>) ResourceRepresentationType.DEFINITION_INSTANCE;
		}

		if (output.equals(ResourceRepresentationType.XSD)){
			byte[] schema = getXMLSchemaFileForDefinition(fullPropertyDefinitionPath);

			if (schema != null){
				return (T) new String(schema);
			}

			return null;
		}

		CmsDefinition cmsDefinition = retrieveDefinition(fullPropertyDefinitionPath);

		if (cmsDefinition != null){

			if (output.equals(ResourceRepresentationType.DEFINITION_INSTANCE)){
				return (T) cmsDefinition;
			}
			else if (output.equals(ResourceRepresentationType.XML)){
				return (T) cmsDefinition.xml(prettyPrint);
			}
			else if (output.equals(ResourceRepresentationType.JSON)){
				return (T) cmsDefinition.json(prettyPrint);
			}
		}

		return null;
	}


	private CmsDefinition retrieveDefinition(String definitionFullPath) throws Exception{

		CmsDefinition cmsDefinition = null;

		definitionFullPath = definitionFullPath.replaceAll(CmsConstants.FORWARD_SLASH, CmsConstants.PERIOD_DELIM);

		if (!definitionFullPath.contains(CmsConstants.PERIOD_DELIM)){
			//Could either be a content object type or a complex cms property definition
			cmsDefinition = getContentObjectTypeDefinition(definitionFullPath);

			if (cmsDefinition == null){
				//Check for complex cms property definition
				cmsDefinition = getAspectDefinition(definitionFullPath);

				if (cmsDefinition != null){
					return cmsDefinition;
				}
			}
			else{
				return cmsDefinition;
			}
		}
		else{
			//Path contains at least two levels.
			//First level is either a content type or a global complex cms property
			PropertyPath path = new PropertyPath(definitionFullPath);

			String rootPath = path.getPropertyName();
			String restOfPath =path.getPropertyDescendantPath();

			cmsDefinition = getCmsPropertyDefinition(restOfPath, rootPath);

			if (cmsDefinition == null){
				ComplexCmsPropertyDefinition globalComplexCmsPropertyDefinition = getAspectDefinition(rootPath);

				if (globalComplexCmsPropertyDefinition != null){
					cmsDefinition = globalComplexCmsPropertyDefinition.getChildCmsPropertyDefinition(restOfPath);
				}
			}

			if (cmsDefinition != null){
				return cmsDefinition;
			}
		}
		return null;
	}
}
