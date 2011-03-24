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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
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
@Name("org.betaconceptframework.astroboa.resourceapi.resource.TaxonomyResource")
*/
public class TaxonomyResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public TaxonomyResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}
	
	// The methods which produce JSON or XML allow "callback" as one extra query parameter 
	// in order to support XML with Padding or JSON with Padding (JSONP) and overcome the SPO restriction of browsers
	// This means that if a "callback" query parameter is provided then the XML or JSON result will be wrapped inside a "callback" script
	
	@GET
	@Produces("*/*")
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getTaxonomyByName(@PathParam("taxonomyIdOrName") String taxonomyIdOrName, @QueryParam("output") String output, @QueryParam("callback") String callback){
		if (output == null) {
			return getTaxonomy(taxonomyIdOrName, Output.XML, callback);
		}

		Output outputEnum = Output.valueOf(output.toUpperCase());

		return getTaxonomy(taxonomyIdOrName, outputEnum, callback);
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getTaxonomyByNameAsJson(@PathParam("taxonomyIdOrName") String taxonomyIdOrName, @QueryParam("output") String output, @QueryParam("callback") String callback){
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.JSON;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		return getTaxonomy(taxonomyIdOrName, outputEnum, callback);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	public Response getTaxonomyByNameAsXml(@PathParam("taxonomyIdOrName") String taxonomyIdOrName, @QueryParam("output") String output, @QueryParam("callback") String callback){
		// URL-based negotiation overrides any Accept header sent by the client
		//i.e. if the url specifies the desired response type in the "output" parameter this method
		// will return the media type specified in "output" request parameter.
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		return getTaxonomy(taxonomyIdOrName, outputEnum, callback);
	}
	
	

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getTaxonomiesAsXML(
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
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		return retrieveTaxonomies(cmsQuery, offset, limit, orderBy, outputEnum, callback, prettyPrintEnabled);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaxonomiesAsJson(
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
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		return retrieveTaxonomies(cmsQuery, offset, limit, orderBy, outputEnum, callback, prettyPrintEnabled);		
	}
	
	@GET
	@Produces("*/*")
	public Response getTaxonomies(
			@QueryParam("cmsQuery") String cmsQuery, 
			@QueryParam("offset") Integer offset, 
			@QueryParam("limit") Integer limit,
			@QueryParam("orderBy") String orderBy, 
			@QueryParam("output") String output,  
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		Output outputEnum = Output.XML;
		if (StringUtils.isNotBlank(output)) {
			outputEnum = Output.valueOf(output.toUpperCase());
		}
		
		boolean prettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);
		
		return retrieveTaxonomies(cmsQuery, offset, limit, orderBy, outputEnum, callback, prettyPrintEnabled);
	}
	
	// **** THE FOLLOWING THREE METHODS SUPPORT RETRIEVING THE TAXONOMY_INSTANCE TOPICS THROUGH THE TaxonomyResource ****
	// **** TOPICS CAN BE ALSO RETRIEVED THROUGH THE TopicResource ****
	// **** READ THE COMMENTS THAT FOLLOW FOR AN EXPLANATION ****
	//
	// BE AWARE! the taxonomyIdOrName is not actually required to retrieve a topic since topic ids or system names are unique across all taxonomies.
	// However since the "taxonomyIdOrName" is provided it should exist and additionally 
	// the provided "topicPathWithIdsOrNames" should exist inside the taxonomy in order to get back your topic.
	// In other words we traverse the provided path from the taxonomy down to the last topic in the path. 
	// So the TaxonomyResource is recommended if you want to traverse/browse taxonomies and their topics going from node to node. 
	// The alternative, easier and more efficient way to directly get topics is to use the "TopicResource"
	// So why do we support this type of resource path for retrieving topics since you can get them directly through the "TopicResource"?. 
	// The answer is because it is natural and most users expect to be able to ask for topics while traversing a taxonomy through the Taxonomy Resource.
	// Imagine a user that implements an AJAX tree for browsing through the topics of the taxonomy. 
	// The URL "/taxonomy" brings back all available taxonomies and then the URL "/taxonomy/{taxonomyIdOrSystemName}" 
	// brings back the requested taxonomy with its root topics 
	// Then the URL "/taxonomy/{taxonomyIdOrSystemName}/{rootTopicIdOrSystemName}" 
	// brings back the requested topic and its child topics. 
	// In the same manner the
	// "/taxonomy/{taxonomyIdOrSystemName}/{rootTopicIdOrSystemName}/{childTopicIdOrSystemName}" brings back the child topic of the root topic and so on.	
	@GET
	@Produces("*/*")
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" +"/{topicPathWithIdsOrNames: " + CmsConstants.TOPIC_PATH_WITH_UUIDS_OR_SYSTEM_NAMES_REG_EXP + "}")
	public Response getTopicInTaxonomy(
			@PathParam("taxonomyIdOrName") String taxonomyIdOrName,
			@PathParam("topicPathWithIdsOrNames") String topicPathWithIdsOrNames,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback,
			@QueryParam("prettyPrint") String prettyPrint){
		
		/*if (output == null)
		{
			return getTopicInTaxonomyInternal(taxonomyIdOrName, topicPathWithIdsOrNames, Output.XML, callback);
		}*/

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);
		
		boolean isPrettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);

		return getTopicInTaxonomyInternal(taxonomyIdOrName, topicPathWithIdsOrNames, outputEnum, callback,isPrettyPrintEnabled);

	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" +"/{topicPathWithIdsOrNames: " + CmsConstants.TOPIC_PATH_WITH_UUIDS_OR_SYSTEM_NAMES_REG_EXP + "}")
	public Response getTopicInTaxonomyAsJson(
			@PathParam("taxonomyIdOrName") String taxonomyIdOrName, 
			@PathParam("topicPathWithIdsOrNames") String topicPathWithIdsOrNames,
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
		
		boolean isPrettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);

		return getTopicInTaxonomyInternal(taxonomyIdOrName, topicPathWithIdsOrNames, outputEnum, callback, isPrettyPrintEnabled);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" +"/{topicPathWithIdsOrNames: " + CmsConstants.TOPIC_PATH_WITH_UUIDS_OR_SYSTEM_NAMES_REG_EXP + "}")
	public Response getTopicInTaxonomyAsXml(
			@PathParam("taxonomyIdOrName") String taxonomyIdOrName, 
			@PathParam("topicPathWithIdsOrNames") String topicPathWithIdsOrNames,
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
		
		boolean isPrettyPrintEnabled = ContentApiUtils.isPrettyPrintEnabled(prettyPrint);

		return getTopicInTaxonomyInternal(taxonomyIdOrName, topicPathWithIdsOrNames, outputEnum, callback, isPrettyPrintEnabled);
	}
	
	 @PUT
	 public Response putTaxonomy(String requestContent) {
		return saveTaxonomySource(requestContent, HttpMethod.PUT);
	  }

	 @POST
	 public Response postTaxonomy(String requestContent) {
		 return saveTaxonomySource(requestContent, HttpMethod.POST);
	  }

  	  @PUT
   	  @Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	  public Response putTaxonomyByIdOrName(
				@PathParam("taxonomyIdOrName") String taxonomyIdOrName,
				String requestContent){
  		  return saveTaxonomyByIdOrName(taxonomyIdOrName, requestContent, HttpMethod.PUT);
  	  }

  	  @POST
   	  @Path("/{taxonomyIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}")
	  public Response postTaxonomyByIdOrName(
				@PathParam("taxonomyIdOrName") String taxonomyIdOrName,
				String requestContent){
  		  return saveTaxonomyByIdOrName(taxonomyIdOrName, requestContent, HttpMethod.POST);
  	  }

	  private Response saveTaxonomyByIdOrName(
				@PathParam("taxonomyIdOrName") String taxonomyIdOrName,
				String requestContent, String httpMethod){
			
		  //Import from xml or json. Taxonomy will not be saved
		  Taxonomy taxonomyToBeSaved = astroboaClient.getImportService().importTaxonomy(requestContent, false);

		  //Bring taxonomy from repository
		  Taxonomy existedTaxonomy  = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyIdOrName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY);

		  boolean taxonomyIdHasBeenProvided = CmsConstants.UUIDPattern.matcher(taxonomyIdOrName).matches();
		  
		  if (taxonomyIdHasBeenProvided){
			  if (taxonomyToBeSaved.getId()==null){
				  taxonomyToBeSaved.setId(taxonomyIdOrName);
			  }
		  }
		  else{
			  if (taxonomyToBeSaved.getName() == null){
				  taxonomyToBeSaved.setName(taxonomyIdOrName);
			  }
		  }
		  
		  if (existedTaxonomy != null){

			  //Taxonomy exists in repository.
			  //Check to see if an id is provided in the payload
			  if (taxonomyToBeSaved.getId()==null){
				  taxonomyToBeSaved.setId(existedTaxonomy.getId());
			  }
			  else{
				  if (!StringUtils.equals(existedTaxonomy.getId(), taxonomyToBeSaved.getId())){
					  logger.warn("Try to {} taxonomy {} which corresponds to an existed taxonomy in repository with id {} but payload contains a different id {}",
							  new Object[]{httpMethod, taxonomyIdOrName, existedTaxonomy.getId(), taxonomyToBeSaved.getId()});
					  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
				  }
			  }
		  }
		  else{
			  //A new taxonomy will be created.
			  //Check to see if system name is provided in the payload
			  if (taxonomyToBeSaved.getName()!=null && ! taxonomyIdHasBeenProvided &&  
					  !StringUtils.equals(taxonomyIdOrName, taxonomyToBeSaved.getName())){
				  logger.warn("Try to {} taxonomy with name {} but payload contains a different name {}",
						  new Object[]{httpMethod, taxonomyIdOrName, taxonomyToBeSaved.getName()});
				  throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			  }
		  }
   		  
   		  //Produce xml representation of imported taxonomy and continue with save
   		  return saveTaxonomySource(taxonomyToBeSaved.xml(false), httpMethod);
  	 }


	 
	 private Response saveTaxonomySource(String taxonomySource, String httpMethod) {
		 
		logger.debug("Want to save a new taxonomy {}",taxonomySource);
		
		try{
			Taxonomy taxonomy = astroboaClient.getImportService().importTaxonomy(taxonomySource, true);
			
			return ContentApiUtils.createResponseForPutOrPostOfACmsEntity(taxonomy,httpMethod, taxonomySource);
			
		}
		catch(Exception e){
			logger.error("",e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}
	 
	private Response getTopicInTaxonomyInternal(String taxonomyIdOrName, String topicPathWithIdsOrSystemNames, Output output, String callback, boolean prettyPrint){
		
		try {
			
			Topic topic = findTopicByTaxonomyIdOrNameAndTopicPathWithIdsOrNames(taxonomyIdOrName, topicPathWithIdsOrSystemNames);

			if (topic == null){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			// Lazy load topic children so they will be rendered in XML or JSON response
			topic.getChildren();
			
			StringBuilder topicAsXMLOrJSON = new StringBuilder();
			
			switch (output) {
			case XML:
			{
				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSON.append(topic.xml(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSON, topic.xml(prettyPrint), callback);
				}
				break;
			}
			case JSON:
				if (StringUtils.isBlank(callback)) {
					topicAsXMLOrJSON.append(topic.json(prettyPrint));
				}
				else {
					ContentApiUtils.generateXMLP(topicAsXMLOrJSON, topic.json(prettyPrint), callback);
				}
				break;
			}
			
			return ContentApiUtils.createResponse(topicAsXMLOrJSON, output, callback, null);
			
		}
		catch(Exception e){
			logger.error("Taxonomy IdOrName: " + taxonomyIdOrName + ", Topic Path with Ids Or Names: " + topicPathWithIdsOrSystemNames, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}
	
	private Response getTaxonomy(String taxonomyIdOrName, Output output, String callback){
		
		if (StringUtils.isBlank(taxonomyIdOrName)){
			logger.warn("No Taxonomy Name provided");
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		try {
			String taxonomyXmlorJson  = retrieveTaxonomyXMLorJSONByIdOrSystemName(taxonomyIdOrName, output);
			
			StringBuilder resourceRepresentation = new StringBuilder();
			
			if (StringUtils.isBlank(callback)) {
				resourceRepresentation.append(taxonomyXmlorJson);
			}
			else {
				switch (output) {
				case XML:
				{
					ContentApiUtils.generateXMLP(resourceRepresentation, taxonomyXmlorJson, callback);
					break;
				}
				case JSON:
					ContentApiUtils.generateJSONP(resourceRepresentation, taxonomyXmlorJson, callback);
					break;
				default:
				{
					ContentApiUtils.generateXMLP(resourceRepresentation, taxonomyXmlorJson, callback);
					break;
				}
				}
				
			}
			
			return ContentApiUtils.createResponse(resourceRepresentation, output, callback, null);
		}
		catch(Exception e){
			logger.error("Taxonomy IdOrName: " + taxonomyIdOrName, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}	
		
	}
	
	
	private Response retrieveTaxonomies(String cmsQuery, 
			Integer offset,Integer limit,
			String orderBy, 
			Output output, 
			String callback,
			boolean prettyPrint) {
		
		
		try {
			
			if (output == null) {
				output = Output.XML;
			}
			
			StringBuilder resourceRepresentation = new StringBuilder();
			
			//List<Taxonomy> taxonomies = new ArrayList<Taxonomy>();
			
			if (!StringUtils.isBlank(cmsQuery)){ 
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
			}

 			switch (output) {
 			case XML:
 			{
 				String resourceCollectionAsXML = astroboaClient.getTaxonomyService().getAllTaxonomies(ResourceRepresentationType.XML, FetchLevel.ENTITY);
 				
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(resourceCollectionAsXML);
 				}
 				else {
	 				ContentApiUtils.generateXMLP(resourceRepresentation, resourceCollectionAsXML, callback);
	 			}
 				break;
 			}
 			case JSON:
 				String resourceCollectionAsJSON = astroboaClient.getTaxonomyService().getAllTaxonomies(ResourceRepresentationType.JSON, FetchLevel.ENTITY);
 				
 				if (StringUtils.isBlank(callback)) {
 					resourceRepresentation.append(resourceCollectionAsJSON);
 				}
 				else {
 					ContentApiUtils.generateJSONP(resourceRepresentation, resourceCollectionAsJSON, callback);
 				}
 				break;
 			}
			
 			return ContentApiUtils.createResponse(resourceRepresentation, output, callback, null);
			
		}
		catch(Exception e){
			logger.error("Cms query " + cmsQuery, e);
			throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
		}	
	}

	private String retrieveTaxonomyXMLorJSONByIdOrSystemName(String taxonomyIdOrName, Output output) {

		if (StringUtils.isBlank(taxonomyIdOrName)){
			logger.warn("The provided Taxonomy IdOrName {} is Blank ", taxonomyIdOrName);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}

		//Default output is XML
		if (output == null) {
			output = Output.XML;
		}
		
		switch (output) {
		case XML:{
			String taxonomyXML = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyIdOrName, ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
			
			if (StringUtils.isBlank(taxonomyXML)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			return taxonomyXML;
		}
		case JSON:{
			String taxonomyJSON = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyIdOrName, ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
			
			if (StringUtils.isBlank(taxonomyJSON)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			return taxonomyJSON;

		}
		default:{
			String taxonomyXML = astroboaClient.getTaxonomyService().getTaxonomy(taxonomyIdOrName, ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
			
			if (StringUtils.isBlank(taxonomyXML)){
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			return taxonomyXML;
		}
		}
	}
	
	/*private void transformTaxonomiesToXML(List<Taxonomy> taxonomies, StringBuilder resourceCollectionAsXML) {
		resourceCollectionAsXML.append("<resourceCollection>");
		
		for (Taxonomy taxonomy : taxonomies) {
			resourceCollectionAsXML
				.append(taxonomy.xml().substring(55));
		}
		
		resourceCollectionAsXML.append("</resourceCollection>");
	}
	
	private void transformTaxonomiesToJSON(List<Taxonomy> taxonomies, StringBuilder resourceCollectionAsJSON) {
		resourceCollectionAsJSON
			.append("{")
			.append("\"total\":").append("\"").append(taxonomies.size()).append("\"");
		
		if (taxonomies.size() > 0) {	
			resourceCollectionAsJSON.append(",")
			.append("\"resourceRepresentation\":").append("[");
			for (Taxonomy taxonomy : taxonomies) {
				resourceCollectionAsJSON.append(taxonomy.json())
					.append(",");
			}
			resourceCollectionAsJSON.append("]");
		}
		resourceCollectionAsJSON.append("}");
	}*/
	
	private Topic findTopicByTaxonomyIdOrNameAndTopicPathWithIdsOrNames(
			String taxonomyIdOrName, 
			String topicPathWithIdsOrNames){

		if (StringUtils.isBlank(topicPathWithIdsOrNames)) {
			logger.warn("The provided topic path is blank. NULL will be returned");
			return null;
		}

		if (StringUtils.isBlank(taxonomyIdOrName)) {
			logger.warn("The provided taxonomy id or name is blank. NULL will be returned");
			return null;
		}
		
		// split topic path to its components
		String[] topicPathComponents = topicPathWithIdsOrNames.split("/");
		
		TopicCriteria finalTopicCriteria = null;
		TopicCriteria parentTopicCriteria = null;
		TopicCriteria rootTopicCriteria = null;
		
		for (String topicIdOrName : topicPathComponents){
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			if (CmsConstants.UUIDPattern.matcher(topicIdOrName).matches()) {
				topicCriteria.addIdEqualsCriterion(topicIdOrName);
			}
			else {
				topicCriteria.addNameEqualsCriterion(topicIdOrName);
			}
			topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			topicCriteria.searchInDirectAncestorOnly();

			if (rootTopicCriteria==null){
				rootTopicCriteria = topicCriteria;
				parentTopicCriteria = topicCriteria;
			}
			else{
				topicCriteria.setAncestorCriteria(parentTopicCriteria);
				
				parentTopicCriteria = topicCriteria;
			}
			
			finalTopicCriteria = topicCriteria;
		}
		
		//Finally add taxonomy criteria for the rootTopic
		if (CmsConstants.UUIDPattern.matcher(taxonomyIdOrName).matches()) {
			Taxonomy taxonomy = astroboaClient.getCmsRepositoryEntityFactory().newTaxonomy();
			taxonomy.setId(taxonomyIdOrName);
			rootTopicCriteria.addTaxonomyCriterion(taxonomy);
		}
		else {
			rootTopicCriteria.addTaxonomyNameEqualsCriterion(taxonomyIdOrName);
		}
		
		CmsOutcome<Topic> topicsFound = astroboaClient.getTopicService().searchTopics(finalTopicCriteria, ResourceRepresentationType.TOPIC_LIST);
		if (CollectionUtils.isNotEmpty(topicsFound.getResults())) {
			Topic firstTopic = topicsFound.getResults().get(0); 
			// if more than one topics correspond to the same name then we choose the first one but we generate a warning
			if (topicsFound.getResults().size() > 1)
				logger.warn("More than one topics correspond to taxonomyIdOrName:topicPathWithIdsOrNames: {}:{}. The first from  list will be returned. Topic id and names should be unique across all taxonomies. Please inform your support team about this bug !!", 
						taxonomyIdOrName,topicPathWithIdsOrNames);
			return firstTopic;
		}
		else {
			logger.info("The provided taxonomyIdOrName:topicPathWithIdsOrNames: {}:{} does not exist.", taxonomyIdOrName,topicPathWithIdsOrNames);
			return null;
		}

	}
}
