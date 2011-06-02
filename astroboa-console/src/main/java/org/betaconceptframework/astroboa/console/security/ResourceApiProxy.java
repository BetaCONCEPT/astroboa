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
package org.betaconceptframework.astroboa.console.security;

import java.net.HttpURLConnection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.jboss.resteasy.client.ClientRequest;
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
@Path("/")
public class ResourceApiProxy {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@GET @PUT @POST @DELETE
	@Produces("*/*")
	public Response dispatchToAstroboaRepository(
			@Context UriInfo uriInfo,
			@Context HttpServletRequest httpServletRequest){
		
		try {
			
			String repositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
			String resourceApiBasePath = AstroboaClientContextHolder.getActiveCmsRepository().getRestfulApiBasePath();
			
			if (StringUtils.isBlank(repositoryId) || StringUtils.isBlank(resourceApiBasePath)) {
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			String httpMethod = httpServletRequest.getMethod();
			
			HttpSession httpSession = httpServletRequest.getSession();
			
			
			
			String requestPath = uriInfo.getPath();
			MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
			
			
			ClientRequest request = new ClientRequest("http://localhost:8080" + resourceApiBasePath + "/" + repositoryId + requestPath);
			
			// add http method
			request.httpMethod(httpMethod);
			
			// add headers
			Enumeration headerNames = httpServletRequest.getHeaderNames();
		    while(headerNames.hasMoreElements()) {
		      String headerName = (String)headerNames.nextElement();
		      request.header(headerName, httpServletRequest.getHeader(headerName));
		    }
		    
		    // add query parameters
		    for (String queryParamName : queryParameters.keySet()) {
		    	request.queryParameter(queryParamName, queryParameters.get(queryParamName));
		    }
			
			return request.execute();
		
		}
		catch (WebApplicationException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while sending request to Astroboa Repository", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

}
