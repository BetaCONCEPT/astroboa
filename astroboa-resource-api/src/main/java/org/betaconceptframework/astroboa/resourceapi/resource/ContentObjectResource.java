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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.commons.excelbuilder.WorkbookBuilder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.astroboa.resourceapi.utility.IndexExtractor;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyExtractor;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public class ContentObjectResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public ContentObjectResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}
	
	// The methods which produce JSON or XML allow "callback" as one extra query parameter 
	// in order to support XML with Padding or JSON with Padding (JSONP) and overcome the SPO restriction of browsers
	// This means that if a "callback" query parameter is provided then the XML or JSON result will be wrapped inside a "callback" script
	
	// API URLs for single content object resource
	
	@GET
	@Produces("*/*")
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getContentObjectByIdOrNameAnyResponse(
			@PathParam("contentObjectIdOrName") String contentObjectIdOrName, 
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		/*if (output == null) {
			return getContentObjectByIdOrName(contentObjectIdOrName, Output.XML, callback,prettyPrintEnabled);
		}*/

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return getContentObjectByIdOrName(contentObjectIdOrName, outputEnum, callback,prettyPrintEnabled);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getContentObjectByIdAsJson(
			@PathParam("contentObjectIdOrName") String contentObjectIdOrName, 
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		
		Output outputEnum = Output.JSON;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		return getContentObjectByIdOrName(contentObjectIdOrName, outputEnum, callback, prettyPrintEnabled);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getContentObjectByIdAsXml(
			@PathParam("contentObjectIdOrName") String contentObjectIdOrName, 
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		return getContentObjectByIdOrName(contentObjectIdOrName, outputEnum, callback,prettyPrintEnabled);
	}
	
	// API URLs for content object properties
	//The propertyPath at the end of the URL is the full path to a property value.
	// Property Path segments are separated by dots 
	// For multiple value properties the identifier of the property or the value if it is a binary channel between brackets indicates which one of the values to return.
	
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_WITH_ID_REG_EXP_FOR_RESTEASY + "}")
	public AstroboaResource getContentObjectPropertyUsingIdentifierInsteadOfIndex(
			@PathParam("contentObjectIdOrName") String contentObjectIdOrName, 
			@PathParam("propertyPath") String propertyPath) {
		
		try {

			ContentObject contentObject = retrieveContentObjectByIdOrSystemName(contentObjectIdOrName, FetchLevel.ENTITY);

			if (contentObject == null) {
				logger.warn("The provided content object id / system name {} does not correspond to a content object or you do not have permission to access the requested object", contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			// We allow to put a mime type suffix (i.e. .jpg, .png, .doc) at the end of the property path
			// This is required when accessing binary properties and the programs which consume the 
			// URL binary outcome do not read the mime type and filename from the header but rather depend on the mime type suffix at 
			// the end of the URL in order to determine how to treat the binary content.
			// Additionally we may utilize this suffix in latter version of the API to support the delivery of different representations 
			// of the property contents. 
			// So we need to check if the property path contains a mime type suffix and remove it
			// This may cause problems if a requested property itself is named under the name of a mime type suffix.
			// To resolve this potential problem it is required to always put a mime type suffix at the end of URLs that read property values 
			// if the requested property is named under the name of a mime type suffix 
			// (i.e. .../contentObject/{contentObjectId}/myImageWithMultipleFormats.jpg.jpg this will result in removing the last "jpg" suffix but keep the previous one which corresponds to a 
			// property named "jpg") 
			if (propertyPath != null && !propertyPath.endsWith("]")){
				String candidateMimeTypeSuffix = StringUtils.substringAfterLast(propertyPath, ".");
				if (ContentApiUtils.isKnownMimeTypeSuffix(candidateMimeTypeSuffix)) {
					propertyPath = StringUtils.substringBeforeLast(propertyPath, ".");
				}
			}
			
			//Extract property along with the value identifier or the value index
			PropertyExtractor propertyExtractor = null;

			//Load Property according to property path
			CmsProperty property = null;

			try{
				propertyExtractor = new PropertyExtractor(contentObject, propertyPath);
				property = propertyExtractor.getProperty();
			}
			catch (Exception e){
				logger.warn("Could not load provided property using path '"+ propertyPath+"' from contentObject "+contentObjectIdOrName, e);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			if (property == null) {
				logger.warn("The provided property '{}' for content object with id or system name '{}' does not exist",propertyPath, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			switch (property.getValueType()) {
			case Complex:
				logger.warn("The provided property '{}' for content object with id or system name '{}' is complex. Currently only simple type property values or binary channel content can be returned through this API call",propertyPath, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				
			case Binary:
				if (propertyExtractor.getIdentifierOfTheValueOfTheProperty() == null){
					return new BinaryChannelResource(astroboaClient, contentObject, (BinaryProperty) property, propertyExtractor.getIndexOfTheValueOfTheProperty());
				}
				else{
					return new BinaryChannelResource(astroboaClient, contentObject, (BinaryProperty) property, propertyExtractor.getIdentifierOfTheValueOfTheProperty());
				}
			
			case ContentType:
				logger.error("Astroboa returned value type '"+ValueType.ContentType+"' for property '{}' for content object with id or system name '{}'. This should never happen", propertyPath, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				
			default:
				
				if (propertyExtractor.getIdentifierOfTheValueOfTheProperty() != null){
					logger.warn("The provided property '{}' for content object with id or system name '{}' is a simple non-binary property but user has provided an identifier instead of an index.",propertyPath, contentObjectIdOrName);
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
				
				return new SimplePropertyResource(astroboaClient, contentObject, (SimpleCmsProperty) property, propertyExtractor.getIndexOfTheValueOfTheProperty());
			}
			
		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while retrieving property: '" + propertyPath + "' for content object with id or system name: " + contentObjectIdOrName, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
	}

	// API URLs for content object properties
	//The propertyPath at the end of the URL is the full path to a property value.
	// Property Path segments are separated by dots 
	// For multiple value properties an index between brackets indicates which one of the values to return.
	// For example departments.department[0] or departments.department[0].jobPositions.jobPosition[0]
	// The regular expression for the path is ^[A-Za-z0-9_\\-]+(\\[[0-9]+\\])?(\\.[A-Za-z0-9_\\-]+(\\[[0-9]+\\])?)*$
	
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}")
	public AstroboaResource getContentObjectProperty(
			@PathParam("contentObjectIdOrName") String contentObjectIdOrName, 
			@PathParam("propertyPath") String propertyPath) {
		
		try {

			ContentObject contentObject = retrieveContentObjectByIdOrSystemName(contentObjectIdOrName, FetchLevel.ENTITY);

			if (contentObject == null) {
				logger.warn("The provided content object id / system name {} does not correspond to a content object or you do not have permission to access the requested object", contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			// We allow to put a mime type suffix (i.e. .jpg, .png, .doc) at the end of the property path
			// This is required when accessing binary properties and the programs which consume the 
			// URL binary outcome do not read the mime type and filename from the header but rather depend on the mime type suffix at 
			// the end of the URL in order to determine how to treat the binary content.
			// Additionally we may utilize this suffix in latter version of the API to support the delivery of different representations 
			// of the property contents. 
			// So we need to check if the property path contains a mime type suffix and remove it
			// This may cause problems if a requested property itself is named under the name of a mime type suffix.
			// To resolve this potential problem it is required to always put a mime type suffix at the end of URLs that read property values 
			// if the requested property is named under the name of a mime type suffix 
			// (i.e. .../contentObject/{contentObjectId}/myImageWithMultipleFormats.jpg.jpg this will result in removing the last "jpg" suffix but keep the previous one which corresponds to a 
			// property named "jpg") 
			if (propertyPath != null && !propertyPath.endsWith("]")){
				String candidateMimeTypeSuffix = StringUtils.substringAfterLast(propertyPath, ".");
				if (ContentApiUtils.isKnownMimeTypeSuffix(candidateMimeTypeSuffix)) {
					propertyPath = StringUtils.substringBeforeLast(propertyPath, ".");
				}
			}
			
			//Check if a value index exists and extract it
			IndexExtractor indexExtractor = new IndexExtractor(propertyPath);

			String propertyPathWithoutIndex = indexExtractor.getPropertyPathWithoutIndex();

			int valueIndex = indexExtractor.getIndex();

			//Load Property according to property path
			CmsProperty property = null;

			try{
				property = contentObject.getCmsProperty(propertyPathWithoutIndex);
			}
			catch(Exception e){
				logger.warn("Could not load provided property using path '"+ propertyPathWithoutIndex+"' from contentObject "+contentObjectIdOrName, e);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			if (property == null) {
				logger.warn("The provided property '{}' for content object with id or system name '{}' does not exist",propertyPathWithoutIndex, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			switch (property.getValueType()) {
			case Complex:
				logger.warn("The provided property '{}' for content object with id or system name '{}' is complex. Currently only simple type property values or binary channel content can be returned through this API call",propertyPathWithoutIndex, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				
			case Binary:
				return new BinaryChannelResource(astroboaClient, contentObject, (BinaryProperty) property, valueIndex);
			
			case ContentType:
				logger.error("Astroboa returned value type '"+ValueType.ContentType+"' for property '{}' for content object with id or system name '{}'. This should never happen", propertyPathWithoutIndex, contentObjectIdOrName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				
			default:
				return new SimplePropertyResource(astroboaClient, contentObject, (SimpleCmsProperty) property, valueIndex);
			}
			
		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while retrieving property: '" + propertyPath + "' for content object with id or system name: " + contentObjectIdOrName, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
	}
	
	
	
	// API URLs for content object resource collections
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContentObjectsAsXML(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName,
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback,
				prettyPrint);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getContentObjectsAsJson(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName,
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.JSON;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback,
				prettyPrint);		
	}
	
	@GET
	@Produces("application/vnd.ms-excel")
	public Response getContentObjectsInXls(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName,
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XLS;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback,
				prettyPrint);		
	}

	
	/* Returning html or pdf is based on facelets and seam which have been currently
	 * removed due to problems with seam resource servlet when multiple seam wars are deployed
	@GET
	@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML}) 
	public Response getContentObjectsAsXHTML(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName,
			@QueryParam("callback") String callback){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XHTML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback);
		
	}
	
	@GET
	@Produces({"application/pdf"}) 
	public Response getContentObjectsAsPDF(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy,
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName,
			@QueryParam("callback") String callback){
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.PDF;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback);
		
	}
	*/
	
	@GET
	@Produces("*/*")
	public Response getContentObjects(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("projectionPaths") String commaDelimitedProjectionPaths,
			@QueryParam("orderBy") String orderBy, 
			@QueryParam("output") String output,
			@QueryParam("template") String templateIdOrSystemName, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		
		/*if (output == null) {
			return retrieveContentObjects(cmsQuery, offset, limit, commaDelimitedProjectionPaths, orderBy, Output.XML, templateIdOrSystemName, callback);
		}*/

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return retrieveContentObjects(
				cmsQuery, 
				offset, 
				limit, 
				commaDelimitedProjectionPaths, 
				orderBy, 
				outputEnum, 
				templateIdOrSystemName, 
				callback,
				prettyPrint);
		
	}
	
	
	private Response getContentObjectByIdOrName(String contentObjectIdOrSystemName, Output output, String callback, boolean prettyPrint){
		
		if (StringUtils.isBlank(contentObjectIdOrSystemName)){
			logger.warn("No contentObjectId provided");
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		Date lastModified = null;
		String contentObjectXmlorJson  = retrieveContentObjectXMLorJSONByIdOrSystemName(contentObjectIdOrSystemName, output, lastModified, prettyPrint);
		
		if (StringUtils.isBlank(contentObjectXmlorJson)) {
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		try {
			
			StringBuilder resourceRepresentation = new StringBuilder();
			
			if (StringUtils.isBlank(callback)) {
				resourceRepresentation.append(contentObjectXmlorJson);
			}
			else {
				switch (output) {
				case XML:
				{
					ContentApiUtils.generateXMLP(resourceRepresentation, contentObjectXmlorJson, callback);
					break;
				}
				case JSON:
					ContentApiUtils.generateJSONP(resourceRepresentation, contentObjectXmlorJson, callback);
					break;
				}
				
			}
			
			return ContentApiUtils.createResponse(resourceRepresentation, output, callback, lastModified);
			
		}
		catch(Exception e) {
			logger.error("ContentObejct id/name " + contentObjectIdOrSystemName,e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}	
		
	}
	
	private Response retrieveContentObjects(
			String cmsQuery, 
			Integer offset,Integer limit, 
			String commaDelimitedProjectionPaths,
			String orderBy, 
			Output output,
			String templateIdOrSystemName,
			String callback,
			String prettyPrint) {
		
		if (output == null) {
			output = Output.XML;
		}
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		try {
			//Build ContentObject criteria
			ContentObjectCriteria contentObjectCriteria = buildCriteria(cmsQuery, offset, limit, commaDelimitedProjectionPaths, orderBy, prettyPrintEnabled);
			
 			String queryResult = null;
 			
 			StringBuilder resourceRepresentation = new StringBuilder();

 			switch (output) {
 			case XML:{
 				queryResult = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
 				
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(queryResult);
 				}
 				else {
 					ContentApiUtils.generateXMLP(resourceRepresentation, queryResult, callback);
	 			}
 				break;
 			}
 			case JSON:
 				queryResult = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(queryResult);
 				}
 				else {
 					ContentApiUtils.generateJSONP(resourceRepresentation, queryResult, callback);
 				}
 				break;
 			case XLS :
 				
 				CmsOutcome<ContentObject> outcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
 				
 				WorkbookBuilder workbookBuilder = new WorkbookBuilder(astroboaClient.getDefinitionService(), "en");
 				
 				int rowIndex = 2;
 				for (ContentObject object : outcome.getResults()) {
 					
 					workbookBuilder.addContentObjectToWorkbook(object);

 					++rowIndex;
 					
 					//Limit to the first 5000 content objects
 					if (rowIndex > 5000){
 						break;
 					}
 				}
 				
 				String filename = createFilename(workbookBuilder);
 				
 				return ContentApiUtils.createResponseForExcelWorkbook(
 						workbookBuilder.getWorkbook(), 
 						ContentDispositionType.ATTACHMENT, 
 						filename + ".xls", null);
 			/* This functionality is temporarily removed until the resolution of seam resource servlet problems 
 			 * when multiple wars are deployed	
 			case XHTML:
 			{
 				List<ContentObject> contentObjects = searchContentObjects(contentObjectCriteria);
 				Contexts.getEventContext().set("contentObjects", contentObjects);
 				Contexts.getEventContext().set("repositoryId", AstroboaClientContextHolder.getActiveRepositoryId());
 				Contexts.getEventContext().set("templateObjectIdOrSystemName", templateIdOrSystemName);
 				Contexts.getEventContext().set("templateProperty", "xhtml");
 				Renderer renderer = Renderer.instance();
 				String xhtmlOutput = renderer.render("/dynamicPage.xhtml");
 				if (StringUtils.isNotBlank(xhtmlOutput)) {
 					resourceRepresentation.append(xhtmlOutput);
 				}
 				break;
 			}
 			case PDF:
 			{
 				List<ContentObject> contentObjects = searchContentObjects(contentObjectCriteria);
 				Contexts.getEventContext().set("contentObjects", contentObjects);
 				Contexts.getEventContext().set("repositoryId", AstroboaClientContextHolder.getActiveRepositoryId());
 				Contexts.getEventContext().set("templateObjectIdOrSystemName", templateIdOrSystemName);
 				Contexts.getEventContext().set("templateProperty", "pdf");
 			
 				byte[] pdfBytes = createPDF("");
 				
 				return ContentApiUtils.createBinaryResponse(
 						pdfBytes, 
 						"application/pdf", 
 						ContentDispositionType.ATTACHMENT, 
 						contentObjectCriteria.getXPathQuery() + ".pdf", null);
 			}
 			*/
 			}
 			
			return ContentApiUtils.createResponse(resourceRepresentation, output, callback, null);
			
		}
		catch(Exception e){
			return ContentApiUtils.createResponseForException(Response.Status.BAD_REQUEST, e, true, "Cms Query "+cmsQuery);
		}	
	}
	
	
	private ContentObjectCriteria buildCriteria(String cmsQuery, 
			Integer offset, 
			Integer limit, 
			String commaDelimitedProjectionPaths,
			String orderBy, boolean prettyPrint) {
		
		//Build ContentObject criteria
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		if (offset == null || offset < 0){
			contentObjectCriteria.setOffset(0);
		}
		else{
			contentObjectCriteria.setOffset(offset);
		}

		if (limit == null || limit < 0){
			contentObjectCriteria.setLimit(50);
		}
		else{
			contentObjectCriteria.setLimit(limit);
		}

		if (StringUtils.isNotBlank(commaDelimitedProjectionPaths)){
			contentObjectCriteria.getPropertyPathsWhichWillBePreLoaded().addAll(Arrays.asList(commaDelimitedProjectionPaths.split(CmsConstants.COMMA)));
		}
		else{
			contentObjectCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		}

		contentObjectCriteria.getRenderProperties().prettyPrint(prettyPrint);
		
		//Parse query
		if (StringUtils.isNotBlank(cmsQuery)) {
			CriterionFactory.parse(cmsQuery, contentObjectCriteria);
		}
		else{
			logger.warn("No query parameter was found. All content objects will be returned according to limit {} and offset {}", contentObjectCriteria.getLimit(), contentObjectCriteria.getOffset());
		}

		//Parse order by
		//Order by value expects to follow pattern
		// property.path asc,property.path2 desc,property.path3
		if (StringUtils.isNotBlank(orderBy)) {
			String[] propertyPathsWithOrder = StringUtils.split(orderBy, ",");

			if (!ArrayUtils.isEmpty(propertyPathsWithOrder)) {
				for (String propertyWithOrder : propertyPathsWithOrder) {
					String[] propertyItems = StringUtils.split(propertyWithOrder, " ");

					if (! ArrayUtils.isEmpty(propertyItems) && propertyItems.length == 2) {
						String property = propertyItems[0];
						String order = propertyItems[1];

						//Check to see if order is valid
						if (StringUtils.isNotBlank(property)) {
							if (StringUtils.equals("desc", order)) {
								contentObjectCriteria.addOrderProperty(property, Order.descending);
							}
							else {
								//Any other value (even invalid) set default order which is ascending
								contentObjectCriteria.addOrderProperty(property, Order.ascending);
							}
						}
					}
				}
			}

		}
		
		return contentObjectCriteria;

	}
	
	private ContentObject retrieveContentObjectByIdOrSystemName(String contentObjectIdOrSystemName, FetchLevel fetchLevel) {
		try {
			return astroboaClient.getContentService().getContentObject(contentObjectIdOrSystemName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, fetchLevel, CacheRegion.NONE, null, false);
		}
		catch (Exception e) {
			return null;
		}
	}

	private String retrieveContentObjectXMLorJSONByIdOrSystemName(String contentObjectIdOrSystemName, Output output, Date lastModified, boolean prettyPrint) {
		try {
			ContentObject contentObject = retrieveContentObjectByIdOrSystemName(contentObjectIdOrSystemName, FetchLevel.FULL);

			if (contentObject == null) {
				return null;
			}
			
			lastModified = ((CalendarProperty)contentObject.getCmsProperty("profile.modified")).getSimpleTypeValueAsDate();

			//Default output is XML
			if (output == null) {
				return contentObject.xml(prettyPrint);
			}

			switch (output) {
			case XML:
				return contentObject.xml(prettyPrint);
			case JSON:
				return contentObject.json(prettyPrint);
			default:
				return contentObject.xml(prettyPrint);
			}
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	/* This functionality is temporarily removed until the resolution of seam resource servlet problems 
 	* when multiple wars are deployed
	private byte[] createPDF(String path) {
		// String DATA_STORE =
		// "org.jboss.seam.document.documentStore.dataStore";
		//EmptyFacesContext emptyFacesContext = new EmptyFacesContext();
		byte[] bytes = null;

		try {
			Renderer render = Renderer.instance(); 
			
			render.render("/dynamicPage.xhtml");
			DocumentStore doc = DocumentStore.instance();

			if (doc != null) {
				DocumentData data = doc.getDocumentData("1");
				ByteArrayDocumentData byteData = null;
				if (data instanceof ByteArrayDocumentData) {
					byteData = (ByteArrayDocumentData) data;
				} else {
					throw new IllegalArgumentException("Couldnt get the bytes from the pdf document, unkown class " + data.getClass().getName());
				}
				bytes = byteData.getData();
			}

		} catch (Exception ex) {
			logger.error("Error when trying to get the content of the pdf in bytes with the message #0", ex.getMessage());
			ex.printStackTrace();
		} finally {
			//emptyFacesContext.restore();
		}

		return bytes;
	}
	*/
	
	private List<ContentObject> searchContentObjects(ContentObjectCriteria contentObjectCriteria) {
		CmsOutcome<ContentObject> cmsOutcome = 
			astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (cmsOutcome.getResults() != null){
			return cmsOutcome.getResults();
		}
		
		return  new ArrayList<ContentObject>();
	}
	
	 @POST
	 @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	 public Response postContentObject(String requestContent) {
	
		long start = System.currentTimeMillis();
		 
		Response response = saveContentObjectString(requestContent, HttpMethod.POST, true);
		 
		logger.debug(" POST ContentObject in {}",  DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
			
		return response;

	  }

  	  @PUT
   	  @Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
   	  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	  public Response putContentObjectByIdOrName(
				@PathParam("contentObjectIdOrName") String contentObjectIdOrName,
				String requestContent){
  		  
  		  
  		  if (StringUtils.isBlank(contentObjectIdOrName)){
  			  logger.warn("Use HTTP PUT to save object {} but no id or system name was provided ", requestContent);
  			  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
  		  }

  		  long start = System.currentTimeMillis();
 		
  		  Response response = saveContentObjectByIdOrName(contentObjectIdOrName, requestContent, HttpMethod.PUT);
  		  
  		  logger.debug(" PUT ContentObject {} in {}", contentObjectIdOrName,  DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
		
  		  return response;

  	  }

  	  @DELETE
   	  @Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	  public Response deleteContentObjectByIdOrName(
				@PathParam("contentObjectIdOrName") String contentObjectIdOrName){
  		  
  		  
  		  if (StringUtils.isBlank(contentObjectIdOrName)){
  			  logger.warn("No id or system name was provided. Delete request cannot proceed");
  			  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
  		  }
  		  
  		  try{
  			  boolean objectDeleted = astroboaClient.getContentService().deleteContentObject(contentObjectIdOrName);
  			  
  			  return ContentApiUtils.createResponseForHTTPDelete(objectDeleted,contentObjectIdOrName);
  		  }
  		  catch(CmsUnauthorizedAccessException e){
			throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
  		  }
  		  catch(Exception e){
  			logger.error("",e);
  			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
  		  }
  	  }

	  	@POST
		@Consumes("multipart/related")
		public Response postObjectMultipartRelated(MultipartRelatedInput multipartRelatedInput){
			return saveContentFromMultipartRelatedRequest(null, multipartRelatedInput, HttpMethod.POST);
	  	}

	  	@PUT
		@Consumes("multipart/related")
		@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
		public Response putObjectMultipartRelated(
				@PathParam("contentObjectIdOrName") String contentObjectIdOrName,
				MultipartRelatedInput multipartRelatedInput){
	  		
			return saveContentFromMultipartRelatedRequest(contentObjectIdOrName, multipartRelatedInput, HttpMethod.PUT);
	  	}

		private Response saveContentFromMultipartRelatedRequest(String contentObjectIdOrName, 	MultipartRelatedInput multipartRelatedInput, String httpMethod) {
			
			try {
				
				//Obtain the part which contains the object's JSON/XML
				InputPart partWhichContainsObjectSource = getMessagePartWithObjectSource(multipartRelatedInput);

				//Check that mime type of the content is a valid one
				ResourceRepresentationType resourceRepresentationType = checkMediaTypeIsValid(partWhichContainsObjectSource);

				ContentObject contentObjectToBeSaved = retrieveObjectSourceFromMessageAndImportWithoutSave(multipartRelatedInput, partWhichContainsObjectSource);

				boolean entityIsNew = objectIsNew(contentObjectIdOrName, httpMethod, contentObjectToBeSaved);
		   		  
		   		//Save content object
				try{
					contentObjectToBeSaved = astroboaClient.getContentService().save(
							contentObjectToBeSaved, false, true, null);
					
					return ContentApiUtils.createSuccessfulResponseForPUTOrPOST(contentObjectToBeSaved, httpMethod, resourceRepresentationType, entityIsNew);
					
				}
				catch(CmsUnauthorizedAccessException e){
					throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
				}
				catch(Exception e){
					logger.error("",e);
					throw new WebApplicationException(e, HttpURLConnection.HTTP_INTERNAL_ERROR);
				}


			}
			catch (WebApplicationException e) {	
				throw e;
			}
			catch (Exception e) {
				throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.INTERNAL_SERVER_ERROR, e, true, 
						"A problem occured while saving form data for object with id or system name: " + contentObjectIdOrName));
			}
		}

		private ContentObject retrieveObjectSourceFromMessageAndImportWithoutSave(
				MultipartRelatedInput multipartRelatedInput, InputPart partWhichContainsObjectSource) throws IOException {
			
			Map<String, byte[]> binaryContentMap= new HashMap<String, byte[]>();
			
			//Iterate through the input parts to collect binary data
			for (Entry<String, InputPart> inputPartEntry : multipartRelatedInput.getRelatedMap().entrySet()){
				
				String partId = inputPartEntry.getKey();
				
				if (StringUtils.equals(partId, multipartRelatedInput.getStart())){
					//Do not process root part
					continue;
				}
				

				InputPart inputPart = inputPartEntry.getValue();
				
				boolean base64Encoded = partIsBase64Encoded(inputPart.getHeaders());
				
				byte[] binaryContent = inputPart.getBody(new GenericType<byte[]>() {});
				
				if (base64Encoded){
					binaryContent = Base64.decodeBase64(binaryContent);
				}
				
				binaryContentMap.put(partId, binaryContent);
			}
			
			//Import content but do not save it 
			 ContentObject contentObjectToBeSaved = astroboaClient.getImportService().importContentObject(partWhichContainsObjectSource.getBodyAsString(), false, true, false, binaryContentMap);
				
			 if (logger.isDebugEnabled()){
				  logger.debug("XML output of imported content object \n{}", contentObjectToBeSaved.xml(true));
			 }
			 
			 
			 
			return contentObjectToBeSaved;
		}

		private boolean partIsBase64Encoded(MultivaluedMap<String, String> headers) {
			
			if (headers!=null && headers.isEmpty()){
				for (Entry<String, List<String>> headerEntry: headers.entrySet()){
					if (StringUtils.equalsIgnoreCase(headerEntry.getKey(), "Content-Transfer-Encoding")){
						
						if (headerEntry.getValue() != null && headerEntry.getValue().contains("base64")){
							return true	;
						}
					}
				}
			}
			
			return false;
		}

		private ResourceRepresentationType checkMediaTypeIsValid(InputPart partWhichContainsObjectSource) {
			
			MediaType mediaType = partWhichContainsObjectSource.getMediaType();
			
			if (mediaType == null ){
				throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.BAD_REQUEST, 
						null, false, "No Content-Type Header found for object content"));
			}

			if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)){
				return ResourceRepresentationType.JSON;
			}
			else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)){
				return ResourceRepresentationType.XML;
			}
			else if (mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)){
				return null;
			}
			else{
				throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.BAD_REQUEST, 
						null, false, "Invalid Content-Type Header "+mediaType.toString()+" found for object content"));
			}
		}

		//According to the RestEASY API, method getRootPart()
		//returns the root part of the message. 
		//If a start parameter was set in the message header 
		//the part with that id is returned. 
		//If no start parameter was set the first part is returned.
		private InputPart getMessagePartWithObjectSource(
				MultipartRelatedInput multipartRelatedInput) {
			
			
			InputPart partWhichContainsObjectSource = multipartRelatedInput.getRootPart();
			
			if (partWhichContainsObjectSource == null){
				if (StringUtils.isBlank(multipartRelatedInput.getStart())){
					throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.BAD_REQUEST, 
							null, false, "'start' parameter in Content-Type header is not provided. There is no way ot determine which part of the message contains the object's content"));
				}
				else{
					throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.BAD_REQUEST, 
						null, false, "Could not locate message part with id "+ multipartRelatedInput.getStart()  
						+". Object's content is not provided"));
				}
			}
			
			return partWhichContainsObjectSource;
		}
  	  
	private Response saveContentObjectByIdOrName(
				@PathParam("contentObjectIdOrName") String contentObjectIdOrName,
				String requestContent, String httpMethod){
		
		
		  //Import from xml or json. ContentObject will not be saved
		  ContentObject contentObjectToBeSaved = astroboaClient.getImportService().importContentObject(requestContent, false, true, false, null);
			
		  if (logger.isDebugEnabled()){
			  logger.debug("XML output of imported content object \n{}", contentObjectToBeSaved.xml(true));
		  }

		  boolean entityIsNew = objectIsNew(contentObjectIdOrName, httpMethod,	contentObjectToBeSaved);
   		  
   		  //Save content object
   		  return saveContentObject(contentObjectToBeSaved, httpMethod, requestContent, entityIsNew);
  	 }

	private boolean objectIsNew(String contentObjectIdOrName,
			String httpMethod, ContentObject contentObjectToBeSaved) {
		
		if (contentObjectIdOrName == null){
			return true;
		}
		
		  ContentObject existingObject = astroboaClient.getContentService().getContentObject(contentObjectIdOrName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		  boolean entityIsNew = existingObject == null;
		  
   		  if (CmsConstants.UUIDPattern.matcher(contentObjectIdOrName).matches()){
   			  //Save content object by Id
   			  
   			  if (contentObjectToBeSaved.getId() == null){
   				  contentObjectToBeSaved.setId(contentObjectIdOrName);
   			  }
   			  else{
   				  //Payload contains id. Check if they are the same
   				  if (! StringUtils.equals(contentObjectIdOrName, contentObjectToBeSaved.getId())){
   					  logger.warn("Try to "+httpMethod + " content object with ID "+contentObjectIdOrName + " but payload contains id "+ contentObjectToBeSaved.getId());
   					  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
   				  }
   			  }
   		  }
   		  else{
   			  //Save content object by SystemName
   			  //Check that payload contains id
   			  if (contentObjectToBeSaved.getId() == null){
   				  if (existingObject != null){
   					  //A content object with system name 'contentObjectIdOrName' exists, but in payload no id was provided
   					  //Set this id to ContentObject representing the payload
   					  contentObjectToBeSaved.setId(existingObject.getId());
   				  }
   			  }
   			  else{
   				  
   				  //Payload contains an id. 
   				  
   				  if (existingObject != null){
   					//if this is not the same with the id returned from repository raise an exception
   					  if (!StringUtils.equals(existingObject.getId(), contentObjectToBeSaved.getId())){
   						logger.warn("Try to "+httpMethod + " content object with system name "+contentObjectIdOrName + " which corresponds to an existed content object in repository with id " +
   								existingObject.getId()+" but payload contains a different id "+ contentObjectToBeSaved.getId());
   						throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
   					  }
   				  }
   			  }
   		  }
		return entityIsNew;
	}


	 
	 private Response saveContentObjectString(String contentSource, String httpMethod, boolean entityIsNew) {
		 
		try{
			
			//Must determine whether a single or a collection of objects is saved
			if (contentSource == null){
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			if (contentSource.contains(CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME) || 
					contentSource.contains(CmsConstants.RESOURCE_COLLECTION)){

				List<ContentObject> contentObjects = astroboaClient.getContentService().saveContentObjectResourceCollection(contentSource, false, true, null);

				//TODO : Improve response details.
				
				ResponseBuilder responseBuilder = Response.status(Status.OK);
				
				responseBuilder.header("Content-Disposition", "inline");
				responseBuilder.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
				
				return responseBuilder.build();
	
			}
			else{

				ContentObject contentObject = astroboaClient.getContentService().save(contentSource, false, true, null);

				return ContentApiUtils.createResponseForPutOrPostOfACmsEntity(contentObject,httpMethod, contentSource, entityIsNew);
			}
			
		}
		catch(CmsUnauthorizedAccessException e){
			throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
		}
		catch(Exception e){
			logger.error("",e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}
	 
	 private Response saveContentObject(ContentObject contentObject, String httpMethod, String requestContent, boolean entityIsNew) {
		 
			try{
				contentObject = astroboaClient.getContentService().save(contentObject, false, true, null);
				
				return ContentApiUtils.createResponseForPutOrPostOfACmsEntity(contentObject,httpMethod, requestContent, entityIsNew);
				
			}
			catch(CmsUnauthorizedAccessException e){
				throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
			}
			catch(Exception e){
				logger.error("",e);
				throw new WebApplicationException(e, HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
	 
		private String createFilename(WorkbookBuilder workbookBuilder) {
			String filename = workbookBuilder.getWorkbookName();
			
			if (filename.length()>50){
				filename = filename.substring(0, 49);
			}
			
			filename = filename + "-"+DateUtils.format(Calendar.getInstance(), "ddMMyyyyHHmm");
			return filename;
		}

}
