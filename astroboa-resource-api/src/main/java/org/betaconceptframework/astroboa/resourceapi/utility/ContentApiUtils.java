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
package org.betaconceptframework.astroboa.resourceapi.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.security.auth.Subject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.resource.Output;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.util.ResourceApiURLUtils;
import org.betaconceptframework.astroboa.util.UrlProperties;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentApiUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(ContentApiUtils.class);
	
	private final static MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

	private final static String MANAGED_FILE_ACCESS_INFO_REG_EXP = "^" +
	"([A-Za-z0-9_\\-]+)" + // repository Id (group 1)
	"/"+
	"(datastore/[0-9abcdef]{2})" + // Resource path ( group 2)
	"/" +
	"([0-9abcdef]{2})" + // Resource path ( group 3)
	"/" +
	"([0-9abcdef]{2})" + // Resource path (group 4 )
	"/" +
	"([0-9abcdef]{40})" + // Resource path (group 5)
	"/" +
	"([A-Za-z0-9_\\-\\.]+/[A-Za-z0-9_\\-\\.]+)"+ // mime-type must contain '/': image/jpeg, application/x-texinfo, etc	 ( group 6 )
	"(?:/width/([0-9]+{1,3}))?" + // width ( group 7 )
	"(?:/height/([0-9]+{1,3}))?" + // height  ( group 8 )
	"(?:/contentDispositionType/(attachment|inline))?" + // content disposition type ( group 9 )
	"/" +
	"([^/]+{0,200})" ; // filename  ( group 10 )
	
	private final static String UNMANAGED_FILE_ACCESS_INFO_REG_EXP = "^" +
	"([A-Za-z0-9_\\-]+)" + // repository Id (group 1)
	"/"+
	"(.+?)" + // Resource path ( group 2)
	"(?:/width/([0-9]+{1,3}))?" + // width ( group 3 )
	"(?:/height/([0-9]+{1,3}))?" + // height  ( group 4 )
	"(?:/contentDispositionType/(attachment|inline))?" ; // content disposition type ( group 5 )
	
	
	public final static Pattern MANAGED_FILE_ACCESS_INFO_PATTERN = Pattern.compile(MANAGED_FILE_ACCESS_INFO_REG_EXP);
	public final static Pattern UNMANAGED_FILE_ACCESS_INFO_PATTERN = Pattern.compile(UNMANAGED_FILE_ACCESS_INFO_REG_EXP);
		
	public static ObjectMapper objectMapper;
	
	static {
		objectMapper = new ObjectMapper();
	}
	
	public static String createETag(long lastModified, long contentLength){
		return new StringBuilder()
			.append("\"")
			.append(contentLength)
			.append("-")
			.append(String.valueOf(lastModified))
			.append("\"")
			.toString();
	}
	
	public static Subject createSubjectForSystemUserAndItsRoles(String cmsRepositoryId){

		Subject subject = new Subject();

		//System identity
		subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));

		//Load default roles for SYSTEM USER
		//Must return at list one group named "Roles" in order to be 
		Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());

		for (CmsRole cmsRole : CmsRole.values()){
			rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, cmsRepositoryId)));
		}

		subject.getPrincipals().add(rolesPrincipal);

		return subject;
	}
	
	public static void generateXMLP(StringBuilder resourceAsXMLP, String resourceAsXML, String callback) {
		String escapedResourceAsXML = StringEscapeUtils.escapeJavaScript(resourceAsXML);
		resourceAsXMLP
			.append(callback)
			.append("(")
			.append("'")
			.append(escapedResourceAsXML)
			.append("'")
			.append(")").append(";");
	}
	
	public static void generateJSONP(StringBuilder resourceAsJSONP, String resourceAsJSON, String callback) {
		resourceAsJSONP
			.append(callback)
			.append("(")
			.append(resourceAsJSON)
			.append(")")
			.append(";");
	}
	
	public static Response createResponse(StringBuilder resourceRepresentation, Output output, String callback, Date lastModified) {
		ResponseBuilder responseBuilder = null;
		responseBuilder = Response.ok(resourceRepresentation.toString());
		responseBuilder.header("Content-Disposition", "inline");
		
		if (callback != null) 
		{
			responseBuilder.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
		}
		else if (output == null)
		{
			responseBuilder.type(MediaType.APPLICATION_XML + "; charset=utf-8");
		}
		else
		{
			switch (output) {
			case XML:
				responseBuilder.type(MediaType.APPLICATION_XML + "; charset=utf-8");
				break;
			case JSON:
				responseBuilder.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
				break;
			case XHTML:
				responseBuilder.type(MediaType.APPLICATION_XHTML_XML + "; charset=utf-8");
				break;
			}
		}

		if (lastModified == null) {
			lastModified = Calendar.getInstance().getTime();
		}
		addLastModifiedAndETagHeaderToResponse(responseBuilder, lastModified, resourceRepresentation.length());
		
		return responseBuilder.build();
	}
	
	
	public List<Topic> getTaxonomyRootTopics(AstroboaClient astroboaClient, String taxonomyName) {

		if (StringUtils.isNotBlank(taxonomyName)) {

			Taxonomy taxonomy = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);

			if (taxonomy != null) {
				List<Topic> rootTopics = taxonomy.getRootTopics();
				if (CollectionUtils.isNotEmpty(rootTopics)) {
					return rootTopics;
				}
			}
		}
		
		return new ArrayList<Topic>();
	}

	public static Response createBinaryResponse(byte[] binaryResource, String mimeType, ContentDispositionType contentDispositionType, String filename, Date lastModified) {
		ResponseBuilder responseBuilder = null;
		responseBuilder = Response.ok(binaryResource, mimeType);
		addContentDispositionHeaderToResponse(responseBuilder, contentDispositionType, filename);
		if (lastModified == null) {
			lastModified = Calendar.getInstance().getTime();
		}
		addLastModifiedAndETagHeaderToResponse(responseBuilder, lastModified, binaryResource.length);
		return responseBuilder.build();
	}
	
	public static Response createBinaryResponseFromStream(
			InputStream resourceStream, 
			String mimeType, 
			ContentDispositionType contentDispositionType, 
			String filename, 
			Date lastModified, 
			long contentLength) {
		ResponseBuilder responseBuilder = null;
		responseBuilder = Response.ok(resourceStream, mimeType);
		addContentDispositionHeaderToResponse(responseBuilder, contentDispositionType, filename);
		if (lastModified == null) {
			lastModified = Calendar.getInstance().getTime();
		}
		addLastModifiedAndETagHeaderToResponse(responseBuilder, lastModified, contentLength);
		return responseBuilder.build();
	}
	
	public static void addContentDispositionHeaderToResponse(ResponseBuilder responseBuilder, ContentDispositionType contentDispositionType, String fileName){
		responseBuilder.header("Content-Disposition", contentDispositionType + ";filename="+fileName);
	}
	
	public static void addLastModifiedAndETagHeaderToResponse(
			ResponseBuilder response,
			Date lastModified, 
			long contentLength)  {
		response.lastModified(lastModified);
		response.tag(EntityTag.valueOf(createETag(lastModified.getTime(), contentLength)));

	}
	
	public static boolean isKnownMimeTypeSuffix(String candidateMimeTypeSuffix) {
		
		if ("application/octet-stream".equals(mimetypesFileTypeMap.getContentType("." + candidateMimeTypeSuffix))) {
			return false;
		}
		
		return true;
	
	}

	/*
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
	 * 
	 * PUT
	 * 
	 * The PUT method requests that the enclosed entity be stored 
	 * under the supplied Request-URI. If the Request-URI refers 
	 * to an already existing resource, the enclosed entity SHOULD 
	 * be considered as a modified version of the one residing on 
	 * the origin server. 
	 * 
	 * If the Request-URI does not point to an existing resource, 
	 * and that URI is capable of being defined as a new resource 
	 * by the requesting user agent, the origin server can create 
	 * the resource with that URI. If a new resource is created,
	 * the origin server MUST inform the user agent via the 201 (Created) response. 
	 * 
	 * If an existing resource is modified, either the 200 (OK) or 204 (No Content)
	 * response codes SHOULD be sent to indicate successful completion of the request.
	 * 
	 * POST
	 * 
	 *  The POST method is used to request that the origin server accept the entity enclosed 
	 *  in the request as a new subordinate of the resource identified by the Request-URI in 
	 *  the Request-Line.
	 *   
	 * The actual function performed by the POST method is determined by the server 
	 * and is usually dependent on the Request-URI. The posted entity is subordinate 
	 * to that URI in the same way that a file is subordinate to a directory containing it, 
	 * a news article is subordinate to a newsgroup to which it is posted, or a record is 
	 * subordinate to a database.
	 *
	 * The action performed by the POST method might not result in a resource 
	 * that can be identified by a URI. In this case, either 200 (OK) or 204 (No Content) 
	 * is the appropriate response status, depending on whether or not the response 
	 * includes an entity that describes the result. 
	 */
	public static Response createSuccessfulResponseForPUTOrPOST(CmsRepositoryEntity entity, String httpMethod, ResourceRepresentationType<?> resourceRepresentationType, boolean entityIsNew) {
		
		ResponseBuilder responseBuilder = null;
		
		if (httpMethod == null || httpMethod.equals(HttpMethod.POST)){
			//Entity is a new one. Send CREATED (201) status with location header
			responseBuilder = Response.status(Status.CREATED);
			
			UrlProperties urlProperties = new UrlProperties();
			urlProperties.setResourceRepresentationType(resourceRepresentationType);
			urlProperties.setFriendly(false);
			urlProperties.setRelative(false);
			urlProperties.setIdentifier(entity.getId());
			
			responseBuilder.location(URI.create(ResourceApiURLUtils.generateUrlForEntity(entity, urlProperties)));

		}
		else if (httpMethod.equals(HttpMethod.PUT)) {
			
			if (entityIsNew){
				//Entity is a new one. Send CREATED (201) status
				responseBuilder = Response.status(Status.CREATED);

			}
			else{
				responseBuilder = Response.status(Status.OK);
			}
		}
		else{
			logger.warn("Expected to have either HTTP PUT or HTTP POST but the provided HTTP method is " + httpMethod+ " Will send OK status nevertheless");
			responseBuilder = Response.status(Status.OK);
		}
		
		responseBuilder.header("Content-Disposition", "inline");
		responseBuilder.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
		
		//TODO: It should clarified whether entity identifier or entity's system name is provided 
		if (entity != null){
			responseBuilder.entity(entity.getId());
		}

		
		return responseBuilder.build();
	}

	public static Response createResponseForPutOrPostOfACmsEntity(
			CmsRepositoryEntity cmsRepositoryEntity, String httpMethod,
			String requestContent, boolean entityIsNew) {
		
		if (cmsRepositoryEntity != null && cmsRepositoryEntity.getId() != null){
			
			ResourceRepresentationType<?> resourceRepresentationType = contentIsXML(requestContent) ? ResourceRepresentationType.XML : ResourceRepresentationType.JSON;
			
			return createSuccessfulResponseForPUTOrPOST(cmsRepositoryEntity, httpMethod, resourceRepresentationType, entityIsNew); 
		}
		else {
			logger.error("{} request was not successfull. The entity created when importing the following content {}. \n {} ",
					new Object[]{httpMethod,(cmsRepositoryEntity == null ? " was null" : " had no identifier" ), requestContent}) ;
			
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	 public static Output getOutputType(String output, Output defaultOutput){

		 if (output == null){
			 return defaultOutput;
		 }
		 
		 try{
			 return Output.valueOf(output.toUpperCase());
		 }
		 catch(Exception e){
			 logger.warn("Invalid value '{}' for output parameter. Output.XML will be used");
			 return defaultOutput;
		 }
	 }

	public static boolean isPrettyPrintEnabled(String prettyPrint) {
		
		if (prettyPrint == null){
			return false;
		}
		
		try{
			return BooleanUtils.isTrue(Boolean.valueOf(prettyPrint));
		}catch(Exception e){
			logger.warn("Invalid value '{}' for prettyPrint parameter. Pretty Print will be disabled");
			return false;
		}
	}


	/*
	 * This is a very basic way to determine whether content provided 
	 * with HTTP PUT or POST is an XML or JSON, without the need to check the 
	 * headers. 
	 * 
	 * A more elegant way must be determined. 
	 * 
	 * Currently the need to know the type of the content is needed 
	 * when we need to generate the url which represents the entity or the property saved.
	 * 
	 */
	public static boolean contentIsXML(String content){
		return content!= null && content.startsWith("<?xml version=\"1.0\"");
	}
	
	public static Response createResponseForExcelWorkbook(HSSFWorkbook workbook, ContentDispositionType contentDispositionType, String filename, Date lastModified) {
		ResponseBuilder responseBuilder = null;
		
		responseBuilder = Response.ok(workbook, "application/vnd.ms-excel");
		
		addContentDispositionHeaderToResponse(responseBuilder, contentDispositionType, filename);
		if (lastModified == null) {
			lastModified = Calendar.getInstance().getTime();
		}
		addLastModifiedAndETagHeaderToResponse(responseBuilder, lastModified, -1);
		
		return responseBuilder.build();
	}
	
	public static Map<String, Object> parse(String json) throws JsonParseException, IOException{
		return objectMapper.readValue(json, Map.class);
	}
	
	/*
	 * A successful response SHOULD be 200 (OK) if the response includes an entity describing the status, 
	 * 202 (Accepted) if the action has not yet been enacted, 
	 * or 204 (No Content) if the action has been enacted but the response does not include an entity. 
	 */
	public static Response createResponseForHTTPDelete(boolean entityHasBeenDeleted, String entityIdOrName) {
		
		ResponseBuilder responseBuilder = null;
		
		if (entityHasBeenDeleted){
			responseBuilder = Response.status(Status.OK);
		}
		else{
			responseBuilder = Response.status(Status.BAD_REQUEST);
		}
		
		responseBuilder.header("Content-Disposition", "inline");
		responseBuilder.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
		
		responseBuilder.entity(entityIdOrName);
		
		return responseBuilder.build();
	}
	
	/*
	 * This method creates a response with the provided status and the provided exception message.
	 * 
	 * User may log this exception (on the server) as well. Log level is ERROR
	 * 
	 * This response can be returned as is or can be used as a parameter of a WebApplicationException, like
	 * 
	 * throw new WebApplicationException(ContentApiUtils.createResponseForException(Status.BAD_REQUEST, t, true, "Additional Message"));
	 * 
	 */
	public static Response createResponseForException(Status status, Throwable t, boolean logException, String additionalMessage){
		
		ResponseBuilder response =  Response.status(status);
		response.header("Content-Type", "text/plain; charset=UTF-8");
		
		StringBuilder message = new StringBuilder();
		
		if (additionalMessage != null){
			message.append(additionalMessage);
		}
		
		if (t != null){
			
			if (t.getMessage() != null){
				message.append("-").append(t.getMessage());
			}
			
			if (logException){
				logger.error("",t);
			}
		}
		
		response.entity(message.toString());
		
		return response.build();

		
	}
}
