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
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SecurityResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public SecurityResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}
	
	// The methods which produce JSON or XML allow "callback" as one extra query parameter 
	// in order to support XML with Padding or JSON with Padding (JSONP) and overcome the SPO restriction of browsers
	// This means that if a "callback" query parameter is provided then the XML or JSON result will be wrapped inside a "callback" script
	@POST
	@Produces("*/*")
	public Response encrypt(String requestContent,  @QueryParam("output") String output,
			@QueryParam("callback") String callback){

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.JSON);

		return encryptInternal(requestContent, outputEnum, callback);

	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response encryptAsJson(String requestContent,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback) {

		Output outputEnum = ContentApiUtils.getOutputType(output, Output.JSON);

		return encryptInternal(requestContent, outputEnum, callback);

	}

	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response encryptAsXml(String requestContent,
			@QueryParam("output") String output, 
			@QueryParam("callback") String callback) {
		
		Output outputEnum = ContentApiUtils.getOutputType(output, Output.XML);

		return encryptInternal(requestContent, outputEnum, callback);
	}
	
	private Response encryptInternal(String requestContent, Output output, String callback){
		
		try {
			
			if (StringUtils.isBlank(requestContent)){
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			checkUserIsAuthorizedToUseEncryptionUtility();
			
			Map<String, Object> userData = ContentApiUtils.parse(requestContent);
			
			String propertyPath = (String) userData.get("fullPropertyPath");
			
			if (StringUtils.isBlank(propertyPath)){
				logger.error("No property path found. Request {} does not contain field {} or its value is blank. User info {}", 
						new Object[]{requestContent, "fullPropertyPath", astroboaClient.getInfo()});
				
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			String password = (String) userData.get("password");
			
			if (StringUtils.isBlank(password)){
				logger.error("No value for password found. Request {} does not contain field {} or its value is blank. User info {}", 
						new Object[]{requestContent, "password", astroboaClient.getInfo()});
				
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			DefinitionService definitionService = astroboaClient.getDefinitionService();

			CmsDefinition definition = definitionService.getCmsDefinition(propertyPath, ResourceRepresentationType.DEFINITION_INSTANCE, false);
			
			if (definition == null){
				logger.error("No definition found for property {}. Request {} - User info {}", 
						new Object[]{propertyPath, requestContent, astroboaClient.getInfo()});
				
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			if ( !(definition instanceof StringPropertyDefinition) || ! ((StringPropertyDefinition)definition).isPasswordType()){
				logger.error("Property {}'s type  is not password type. Request {} - User info {}", 
						new Object[]{propertyPath, requestContent, astroboaClient.getInfo()});
				
				throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			String encryptedPassword = StringEscapeUtils.escapeJava(((StringPropertyDefinition)definition).getPasswordEncryptor().encrypt(password));
			
			StringBuilder encryptedPasswordBuidler = new StringBuilder();

			switch (output) {
			case XML:{

				encryptedPassword = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><encryptedPassword>"+encryptedPassword+"</encryptedPassword>";
				
				if (StringUtils.isBlank(callback)) {
					encryptedPasswordBuidler.append(encryptedPassword);
				}
				else {
					ContentApiUtils.generateXMLP(encryptedPasswordBuidler, encryptedPassword, callback);
				}
				break;
			}
			case JSON:

				encryptedPassword = "{\"encryptedPassword\" : \""+encryptedPassword+"\"}";
				if (StringUtils.isBlank(callback)) {
					encryptedPasswordBuidler.append(encryptedPassword);
				}
				else {
					ContentApiUtils.generateJSONP(encryptedPasswordBuidler, encryptedPassword, callback);
				}
				break;
			}
			
			return ContentApiUtils.createResponse(encryptedPasswordBuidler, output, callback, null);
		
		}
		catch (WebApplicationException wae){
			throw wae;
		}
		catch(Exception e){
			logger.error("Encrytpion failed. Request "+requestContent+" - User info "+astroboaClient.getInfo(), e);
			throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	private void checkUserIsAuthorizedToUseEncryptionUtility() {
		
		//Anonymous user is not authorized to use encryption utility
		if (astroboaClient.isUserAnonymous()){
			logger.warn("Anonymous User tried to use the Resource API encrypt utility for repository "+ astroboaClient.getConnectedRepositoryId());
			throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
		}
		
	}

}
