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
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.astroboa.resourceapi.utility.ModelSerializer;
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
		
		try {
			
			if (StringUtils.isBlank(propertyPath)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			DefinitionService definitionService = astroboaClient.getDefinitionService();
			
			StringBuilder definitionAsXMLOrJSONorXSD = new StringBuilder();

			switch (output) {
			case XML:{

				String xml = definitionService.getCmsDefinition(propertyPath, ResourceRepresentationType.XML, prettyPrint);

				if (StringUtils.isBlank(xml)){
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}

				if (StringUtils.isBlank(callback)) {
					definitionAsXMLOrJSONorXSD.append(xml);
				}
				else {
					ContentApiUtils.generateXMLP(definitionAsXMLOrJSONorXSD, xml, callback);
				}
				break;
			}
			case JSON:

				String json = definitionService.getCmsDefinition(propertyPath, ResourceRepresentationType.JSON, prettyPrint);

				if (StringUtils.isBlank(json)){
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}

				if (StringUtils.isBlank(callback)) {
					definitionAsXMLOrJSONorXSD.append(json);
				}
				else {
					ContentApiUtils.generateJSONP(definitionAsXMLOrJSONorXSD, json, callback);
				}
				break;
			case XSD:

				String xsd = definitionService.getCmsDefinition(propertyPath, ResourceRepresentationType.XSD, prettyPrint);

				if (StringUtils.isBlank(xsd)){
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}

				definitionAsXMLOrJSONorXSD.append(xsd);

				if (StringUtils.isNotBlank(callback)) {
					logger.warn("Callback {} is ignored in {} output", callback, output);
				}
				break;
			}
		
			return ContentApiUtils.createResponse(definitionAsXMLOrJSONorXSD, output, callback, null);
		
		}
		catch(Exception e){
			logger.error("Definition For property path: " + propertyPath, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
    public Response getModelAsXml(
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint){
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return getModelInternal(callback, prettyPrintEnabled, outputEnum);

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
    public Response getModelAsJson(
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint){

		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return getModelInternal(callback, prettyPrintEnabled, outputEnum);

	}

	@GET
	@Produces("*/*")
    public Response getModel(
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback, 
			@QueryParam("prettyPrint") String prettyPrint){

		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return getModelInternal(callback, prettyPrintEnabled, outputEnum);

	}

	private Response getModelInternal(String callback,
			boolean prettyPrintEnabled, Output outputEnum) {
		try{
			
			if (outputEnum != Output.XML && outputEnum != Output.JSON){
				throw new Exception("All definitions are exported only in XML and JSON format. "+outputEnum + " is not supported");
			}
			
			String allDefinitions = new ModelSerializer(prettyPrintEnabled, outputEnum == Output.JSON, astroboaClient.getDefinitionService(), astroboaClient.getConnectedRepositoryId()).serialize();
			
			if (StringUtils.isBlank(allDefinitions)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			StringBuilder definitionAsXMLOrJSONorXSD = new StringBuilder();
			if (StringUtils.isBlank(callback)) {
				definitionAsXMLOrJSONorXSD.append(allDefinitions);
			}
			else {
				switch (outputEnum) {
				case XML:{
					ContentApiUtils.generateXMLP(definitionAsXMLOrJSONorXSD, allDefinitions, callback);
					break;
				}
				case JSON:
					ContentApiUtils.generateJSONP(definitionAsXMLOrJSONorXSD, allDefinitions, callback);
					break;
				}
			}


			return ContentApiUtils.createResponse(definitionAsXMLOrJSONorXSD, outputEnum, callback, null);

		}
		catch(Exception e){
			logger.error("When exporting all Definitions", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}
}
