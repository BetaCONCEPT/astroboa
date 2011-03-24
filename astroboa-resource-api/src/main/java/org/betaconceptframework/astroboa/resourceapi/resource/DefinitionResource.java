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
package org.betaconceptframework.astroboa.resourceapi.resource;

import java.net.HttpURLConnection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public DefinitionResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}
	
	// The methods which produce JSON or XML allow "callback" as one extra query parameter 
	// in order to support XML with Padding or JSON with Padding (JSONP) and overcome the SPO restriction of browsers
	// This means that if a "callback" query parameter is provided then the XML or JSON result will be wrapped inside a "callback" script
	@GET
	@Produces("*/*")
	@Path("/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}")
	public Response getDefinition(
			@PathParam("propertyPath") String propertyPath,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint){
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		//This is to server built in Xml Schemas 
		//Output is ignored since these schemata are served only in XSD
		if (StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION) || 
				StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION)){
			return getDefinitionInternal(propertyPath, Output.XSD, callback, prettyPrintEnabled);
		}
		
		if (output == null){
			return getDefinitionInternal(propertyPath, Output.XML, callback, prettyPrintEnabled);
		}

		Output outputEnum = Output.valueOf(output.toUpperCase());

		return getDefinitionInternal(propertyPath, outputEnum, callback,prettyPrintEnabled);

	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}")
	public Response getDefinitionAsJson(
			@PathParam("propertyPath") String propertyPath,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint) {
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		//This is to server built in Xml Schemas 
		//Output is ignored since these schemata are served only in XSD
		if (StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION) || 
				StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION)){
			return getDefinitionInternal(propertyPath, Output.XSD, callback, prettyPrintEnabled);
		}

		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		return getDefinitionInternal(propertyPath, outputEnum, callback, prettyPrintEnabled);
	}

	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}")
	public Response getDefinitionAsXml(
			@PathParam("propertyPath") String propertyPath,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint) {
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		//This is to server built in Xml Schemas 
		//Output is ignored since these schemata are served only in XSD
		if (StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION) || 
				StringUtils.equals(propertyPath, CmsConstants.ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION)){
			return getDefinitionInternal(propertyPath, Output.XSD, callback, prettyPrintEnabled);
		}

		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		return getDefinitionInternal(propertyPath, outputEnum, callback, prettyPrintEnabled);
	}
	
	private Response getDefinitionInternal(String propertyPath, Output output, String callback, boolean prettyPrint){
		
		
		//TODO : When prettyPrint functionality is enabled in DefinitionService.getCmsDefinition
		// this implementation will change
		
		try {
			
			if (StringUtils.isBlank(propertyPath)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			
			DefinitionService definitionService = astroboaClient.getDefinitionService();
			
			if (output != null && output == Output.XSD){
				
				StringBuilder definitionAsXMLOrJSON = new StringBuilder();
				
				definitionAsXMLOrJSON.append(definitionService.getCmsDefinition(propertyPath, ResourceRepresentationType.XSD));
				
				return ContentApiUtils.createResponse(definitionAsXMLOrJSON, output, callback, null);
			}

			CmsDefinition definition = null;
			
			if  (!propertyPath.contains(".") && definitionService.hasContentObjectTypeDefinition(propertyPath)){
				//Property path has only one part. It may well be a content type.
				//Check if a content type with the provided name exists. If so, then use 
				//the appropriate method.
				definition = definitionService.getContentObjectTypeDefinition(propertyPath);
			}
			else{
				definition = definitionService.getCmsPropertyDefinition(propertyPath);
			}
			
			if (definition == null)
			{
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			StringBuilder definitionAsXMLOrJSON = new StringBuilder();
			
			switch (output) {
			case XML:
			{
				if (StringUtils.isBlank(callback)) {
					definitionAsXMLOrJSON.append(definition.xml(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(definitionAsXMLOrJSON, definition.xml(prettyPrint), callback);
				}
				break;
			}
			case JSON:
				if (StringUtils.isBlank(callback)) {
					definitionAsXMLOrJSON.append(definition.json(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(definitionAsXMLOrJSON, definition.json(prettyPrint), callback);
				}
				break;
			}
			
			return ContentApiUtils.createResponse(definitionAsXMLOrJSON, output, callback, null);
			
		}
		catch(Exception e){
			logger.error("Definition For property path: " + propertyPath, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}
	
}
