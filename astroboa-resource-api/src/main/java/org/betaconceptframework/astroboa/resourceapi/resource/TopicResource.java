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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
/*
@Name("org.betaconceptframework.astroboa.resourceapi.resource.TopicResource")
*/
public class TopicResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public TopicResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}
	
	// The methods which produce JSON or XML allow "callback" as one extra query parameter 
	// in order to support XML with Padding or JSON with Padding (JSONP) and overcome the SPO restriction of browsers
	// This means that if a "callback" query parameter is provided then the XML or JSON result will be wrapped inside a "callback" script
	
	
	// **** THE FOLLOWING THREE METHODS SUPPORT IMEDIATE RETRIEVAL OF TAXONOMY_INSTANCE TOPICS 
	// WITHOUT THE NEED TO PROVIDE THE TAXONOMY_INSTANCE. THIS IS POSSIBLE SINCE TOPIC_INSTANCE IDs OR 
	// SYSTEM NAMES ARE UNIQUE ACROSS TAXONOMIES
	//
	// **** TOPICS CAN BE ALSO RETRIEVED THROUGH THE TaxonomyResource ****
	// **** READ THE RELEVANT COMMENTS IN TaxonomyResource CLASS FOR A MORE DETAILED EXPLANATION ****
	//
	// In short use the following rule to understand the difference of topic retrieval through the 
	// TaxonomyResource or the TopicResource: 
	// When you use the TopicResource you should imagine that you go inside a bucket that keeps
	// all topics flat i.e. without any hierarchy. If you know the topic or system name of the topic, the 
	// bucket will give you back the topic.
	// When you use the TaxonomyResource think that you are in browsing mode. The TaxonomyResource in not a bucket but 
	// a tree. You traverse the tree of taxonomies to find the required taxonomy and then find the root topics inside the taxonomy
	// and then find the child topics inside each root topic and so on.
	@GET
	@Produces("*/*")
	@Path("/{topicIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getTopic(
			@PathParam("topicIdOrName") String topicIdOrName,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		/*if (output == null)
		{
			return getTopicInternal(topicIdOrName, Output.XML, callback);
		}*/

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);
		
		return getTopicInternal(topicIdOrName, outputEnum, callback, prettyPrint);

	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{topicIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getRootTopicsInTaxonomyAsJson(
			@PathParam("topicIdOrName") String topicIdOrName,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint) {
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		/*Output outputEnum = Output.JSON;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}*/
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.JSON);
		
		return getTopicInternal(topicIdOrName, outputEnum, callback, prettyPrint);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{topicIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getRootTopicsInTaxonomyAsXml(
			@PathParam("topicIdOrName") String topicIdOrName,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint) {
		
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		/*Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}*/
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);
		
		return getTopicInternal(topicIdOrName, outputEnum, callback, prettyPrint);
	}
	
	  @PUT
   	  @Path("/{topicIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	  public Response putTopicByIdOrName(
				@PathParam("topicIdOrName") String topicIdOrName,
				String requestContent){
		  
		  if (StringUtils.isBlank(topicIdOrName)){
			  logger.warn("Use HTTP PUT to save topic {} but no id or name was provided ", requestContent);
			  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		  }
		  
  		  return saveTopicByIdOrName(topicIdOrName, requestContent, HttpMethod.PUT);
  	  }

  	  @POST
	  public Response postTopicByIdOrName(String requestContent){
  		  return saveTopicSource(requestContent, HttpMethod.POST, true);
  	  }

	  private Response saveTopicByIdOrName(String topicNameOrId,
				String requestContent, String httpMethod){
			
		  //Import from xml or json. Topic will not be saved
   		  Topic topicToBeSaved = astroboaClient.getImportService().importTopic(requestContent, false);

   		  Topic existingTopic = astroboaClient.getTopicService().getTopic(topicNameOrId, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
   		  
   		  boolean entityIsNew = existingTopic == null;
   		  
   		  if (CmsConstants.UUIDPattern.matcher(topicNameOrId).matches()){
   			  //Save topic by Id
   			  if (topicToBeSaved.getId() == null){
   				  topicToBeSaved.setId(topicNameOrId);
   			  }
   			  else{
   				  //Payload contains id. Check if they are the same
   				  if (! StringUtils.equals(topicNameOrId, topicToBeSaved.getId())){
   					  logger.warn("Try to "+httpMethod + " topic with ID "+topicNameOrId + " but payload contains id "+ topicToBeSaved.getId());
   					  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
   				  }
   			  }
   		  }
   		  else{
   			  //Save content object by SystemName

   			  //Check that payload contains id
   			  if (topicToBeSaved.getId() == null){
   				  if (existingTopic != null){
   					  //A topic with name 'topicIdOrName' exists, but in payload no id was provided
   					  //Set this id to Topic representing the payload
   					  topicToBeSaved.setId(existingTopic.getId());
   				  }
   			  }
   			  else{
   				  
   				  //Payload contains an id. 
   				  if (existingTopic != null){
   					//if this is not the same with the id returned from repository raise an exception
   					  if (!StringUtils.equals(existingTopic.getId(), topicToBeSaved.getId())){
   						logger.warn("Try to "+httpMethod + " topic with name "+topicNameOrId + " which corresponds to an existed topic in repository with id " +
   								existingTopic.getId()+" but payload contains a different id "+ topicToBeSaved.getId());
   						throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
   					  }
   				  }
   			  }
   		  }
   		  
   		  //Save content object
   		  return saveTopic(topicToBeSaved, httpMethod, requestContent, entityIsNew);
  	 }


	 private Response saveTopicSource(String topicSource, String httpMethod, boolean entityIsNew) {
		 
			logger.debug("Want to save a new topic {}",topicSource);
			
			try{
				Topic topic = astroboaClient.getImportService().importTopic(topicSource, true);
				
				return ContentApiUtils.createResponseForPutOrPostOfACmsEntity(topic,httpMethod, topicSource, entityIsNew);
				
			}
			catch(CmsUnauthorizedAccessException e){
				throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
			}
			catch(Exception e){
				logger.error("",e);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
		}

	 private Response saveTopic(Topic topic, String httpMethod, String requestContent, boolean entityIsNew) {
		 
			try{
				topic = astroboaClient.getTopicService().save(topic);
				
				return ContentApiUtils.createResponseForPutOrPostOfACmsEntity(topic,httpMethod, requestContent, entityIsNew);
				
			}
			catch(CmsUnauthorizedAccessException e){
				throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
			}
			catch(Exception e){
				logger.error("",e);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
		}
	

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getTopicsAsXML(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("orderBy") String orderBy,
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
		
		return retrieveTopics(
				cmsQuery, 
				offset, 
				limit, 
				orderBy, 
				outputEnum, 
				callback,
				prettyPrint);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopicsAsJson(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("orderBy") String orderBy,
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
		
		return retrieveTopics(
				cmsQuery, 
				offset, 
				limit, 
				orderBy, 
				outputEnum, 
				callback, 
				prettyPrint);		
	}

	@GET
	@Produces("*/*")
	public Response getTopics(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit, 
			@QueryParam("orderBy") String orderBy, 
			@QueryParam("output") String output,
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		
		if (output == null) {
			return retrieveTopics(cmsQuery, offset, limit, orderBy, Output.XML, callback, prettyPrint);
		}

		Output outputEnum = Output.valueOf(output.toUpperCase());

		return retrieveTopics(
				cmsQuery, 
				offset, 
				limit, 
				orderBy, 
				outputEnum, 
				callback,
				prettyPrint);
		
	}
	
	private Response retrieveTopics(
			String cmsQuery, 
			Integer offset,Integer limit, 
			String orderBy, 
			Output output,
			String callback,
			String prettyPrint) {
		
		if (output == null) {
			output = Output.XML;
		}
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		try {
			//Build Topic criteria
			TopicCriteria topicCriteria = buildCriteria(cmsQuery, offset, limit, orderBy, prettyPrintEnabled);
			
 			String queryResult = null;
 			
 			StringBuilder resourceRepresentation = new StringBuilder();

 			switch (output) {
 			case XML:
 			{
 				queryResult = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.XML);
 				
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(queryResult);
 				}
 				else {
 					ContentApiUtils.generateXMLP(resourceRepresentation, queryResult, callback);
	 			}
 				break;
 			}
 			case JSON:
 				queryResult = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.JSON);
 				
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(queryResult);
 				}
 				else {
 					ContentApiUtils.generateJSONP(resourceRepresentation, queryResult, callback);
 				}
 				break;
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
			logger.error("Cms query "+cmsQuery,e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}	
	}
	
	
	private TopicCriteria buildCriteria(String cmsQuery, 
			Integer offset, 
			Integer limit, 
			String orderBy,
			boolean prettyPrint) {
		
		//Build Topic criteria
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();

		if (offset == null || offset < 0){
			topicCriteria.setOffset(0);
		}
		else{
			topicCriteria.setOffset(offset);
		}

		if (limit == null || limit < 0){
			topicCriteria.setLimit(50);
		}
		else{
			topicCriteria.setLimit(limit);
		}

		topicCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		topicCriteria.getRenderProperties().prettyPrint(prettyPrint);
		
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		//Parse query
		if (StringUtils.isNotBlank(cmsQuery)) {
			CriterionFactory.parse(cmsQuery, topicCriteria);
		}
		else{
			logger.warn("No query parameter was found. All topics will be returned according to limit and offset");
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
								topicCriteria.addOrderProperty(property, Order.descending);
							}
							else {
								//Any other value (even invalid) set default order which is ascending
								topicCriteria.addOrderProperty(property, Order.ascending);
							}
						}
					}
				}
			}

		}
		
		return topicCriteria;

	}
	
	private Response getTopicInternal(String topicIdOrName, Output output, String callback, String prettyPrint){
		
		//OLD Method
		//return generateTopicResponseUsingTopicInstance(topicIdOrName, output,	callback);
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		try {
			
			StringBuilder topicAsXMLOrJSONBuilder = new StringBuilder();
			
			switch (output) {
			case XML:{
				String topicXML = astroboaClient.getTopicService().getTopic(topicIdOrName, ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN, prettyPrintEnabled);

				if (StringUtils.isBlank(topicXML)){
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}

				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSONBuilder.append(topicXML);
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSONBuilder, topicXML, callback);
				}
				break;
			}
			case JSON:{
				String topicJSON = astroboaClient.getTopicService().getTopic(topicIdOrName, ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN, prettyPrintEnabled);

				if (StringUtils.isBlank(topicJSON)){
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSONBuilder.append(topicJSON);
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSONBuilder, topicJSON, callback);
				}
				break;
			}
			}
			
			return ContentApiUtils.createResponse(topicAsXMLOrJSONBuilder, output, callback, null);
			
		}
		catch(Exception e){
			logger.error("Topic IdOrName: " + topicIdOrName, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}

	}

	/*
	 * This is the old version of the code (2.1.2.GA) 
	 * It should be deleted once new code is stable
	 */
	private Response generateTopicResponseUsingTopicInstance(
			String topicIdOrName, Output output, String callback, boolean prettyPrint) {
		try {
			
			Topic topic = findTopicByTopicIdOrName(topicIdOrName);

			if (topic == null)
			{
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			topic.getChildren();
			
			StringBuilder topicAsXMLOrJSONBuilder = new StringBuilder();
			
			switch (output) {
			case XML:
			{
				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSONBuilder.append(topic.xml(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSONBuilder, topic.xml(prettyPrint), callback);
				}
				break;
			}
			case JSON:
				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSONBuilder.append(topic.json(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSONBuilder, topic.json(prettyPrint), callback);
				}
				break;
			}
			
			return ContentApiUtils.createResponse(topicAsXMLOrJSONBuilder, output, callback, null);
			
		}
		catch(Exception e){
			logger.error("Topic IdOrName: " + topicIdOrName, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}
	
	private Topic findTopicByTopicIdOrName(
			String topicIdOrName){

		if (StringUtils.isBlank(topicIdOrName)) {
			logger.warn("The provided topic name is blank. NULL will be returned");
			return null;
		}

		Topic topic = astroboaClient.getTopicService().getTopic(topicIdOrName, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		if (topic == null) {
			logger.info("The provided topicIdOrName: " + topicIdOrName + " does not exist.");
			return null;
		}
		
		return topic;
		
		/*TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		if (CmsConstants.UUIDPattern.matcher(topicIdOrName).matches()) {
			topicCriteria.addIdEqualsCriterion(topicIdOrName);
		}
		else {
			topicCriteria.addNameEqualsCriterion(topicIdOrName);
		}

		CmsOutcome<Topic> topicsFound = astroboaClient.getTopicService().searchTopics(topicCriteria);
		if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
			Topic firstTopic = topicsFound.getResults().get(0); 
			// if more than one topics correspond to the same name then we choose the first one but we generate a warning
			if (topicsFound.getResults().size() > 1)
				logger.warn("More than one topics correspond to topicIdOrName: " + topicIdOrName + " The first from  list will be returned. Topic id and names should be unique across all taxonomies. Please inform your support team about this bug !!");
			return firstTopic;
		}
		else {
			logger.info("The provided topicIdOrName: " + topicIdOrName + " does not exist.");
			return null;
		}*/

	}
}
