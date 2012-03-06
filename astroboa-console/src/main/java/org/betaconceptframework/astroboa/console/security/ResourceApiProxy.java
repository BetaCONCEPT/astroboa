/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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
package org.betaconceptframework.astroboa.console.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource locator representing the main entry of all 
 * requests for one or more resources from a Astroboa repository.
 * 
 * This class is responsible to login to the provided repository id
 * and then to forward request to the appropriate sub resource.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@ApplicationPath("/")
@Path("/")
public class ResourceApiProxy {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@GET @PUT @POST @DELETE
	@Path("{resourceApiPath:.*}")
	@Produces("*/*")
	public Response dispatchToAstroboaRepository(
			@PathParam("resourceApiPath") String resourceApiPath,
			@Context UriInfo uriInfo,
			@Context HttpServletRequest httpServletRequest){
		
		try {
			
			String repositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
			if (StringUtils.isBlank(repositoryId)) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			CmsRepository activeCmsRepository = AstroboaClientContextHolder.getActiveCmsRepository();
			if (activeCmsRepository == null) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
			if (securityContext == null) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			String username = securityContext.getIdentity();
			
			if (StringUtils.isBlank(username)) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			String resourceApiBasePath = activeCmsRepository.getRestfulApiBasePath();
			
			if (StringUtils.isBlank(resourceApiBasePath)) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			HttpSession httpSession = httpServletRequest.getSession();
			String password = (String) httpSession.getAttribute("repositoryPassword");
			
			if (StringUtils.isBlank(password)) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			String httpMethod = httpServletRequest.getMethod();
			
			String requestPath = uriInfo.getPath();
			MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
			
			ClientRequest request = new ClientRequest("http://localhost:8080" + resourceApiBasePath + "/" + repositoryId + requestPath);
			
			// read the payload if the http method is put or post
			if (httpMethod.equals("POST") || httpMethod.equals("PUT")) {
				request.body(httpServletRequest.getContentType(), getBody(httpServletRequest));
			}
			
			// add authorization header (BASIC AUTH)
		    String basicAuthString = username + ":" + password;
		    String authorization = "BASIC "+ Base64.encodeBytes(basicAuthString.getBytes());
		    request.header(HttpHeaders.AUTHORIZATION, authorization);
		    
			// add headers
			Enumeration headerNames = httpServletRequest.getHeaderNames();
		    while(headerNames.hasMoreElements()) {
		      String headerName = (String)headerNames.nextElement();
		      request.header(headerName, httpServletRequest.getHeader(headerName));
		    }
		    
		    // add query parameters
		    for (Map.Entry queryParamEntry : queryParameters.entrySet()) {
		    	request.queryParameter((String)queryParamEntry.getKey(), ((List<String>)queryParamEntry.getValue()).get(0));
		    }
			
		    String uri = request.getUri();
		    
		    
		    ClientResponse clientResponse = request.httpMethod(httpMethod, new GenericType<InputStream>() {});
		    
		    return clientResponse;
		
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while sending request to Astroboa Repository", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	private String getBody(HttpServletRequest httpServletRequest) throws Exception {
		
		BufferedReader bufferedReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		
		try { 
			bufferedReader = httpServletRequest.getReader();
			
			char[] buffer = new char[4 * 1024]; // 4 KB char buffer  
			
			int len;
		
			while ((len = bufferedReader.read(buffer, 0, buffer.length)) != -1) {  
				stringBuilder.append(buffer, 0, len);  
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {  
			if (bufferedReader != null) {  
				try {  
					bufferedReader.close();  
				} 
				catch (IOException e) {  
					throw e;  
				}  
			}  
		}
		
		return stringBuilder.toString();
	}
		

}
