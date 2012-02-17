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
package org.betaconceptframework.astroboa.resourceapi.locator;

import java.net.HttpURLConnection;

import javax.servlet.ServletContext;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.AstroboaClientCache;
import org.betaconceptframework.astroboa.resourceapi.utility.RepositoryConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.core.util.Base64;

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
public class RepositoryLocator {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Path("{repositoryId}")
	public ResourceLocator connectToAstroboaRepository(
			@HeaderParam("Authorization") String authorization, 
			@PathParam("repositoryId") String repositoryId,
			@Context ServletContext servletContext){
	
		if (StringUtils.isBlank(repositoryId)) {
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		try {
			
			AstroboaClient astroboaClient = null;
			
			long start = System.currentTimeMillis();
			
			if (authorization != null) {
				
				String cacheKey = authorization+repositoryId;
				
				astroboaClient = AstroboaClientCache.Instance.get(cacheKey);
				
				if (astroboaClient == null){
					String encodedUsernamePass = authorization.substring(5);
					String usernamePass = Base64.base64Decode(encodedUsernamePass);

					String[] usernamePassSplitted = usernamePass.split(":");

					if (usernamePassSplitted.length == 2) {
						astroboaClient = new AstroboaClient(AstroboaClient.INTERNAL_CONNECTION);
						AstroboaCredentials credentials = new AstroboaCredentials(usernamePassSplitted[0], usernamePassSplitted[1]);
						astroboaClient.login(repositoryId, credentials);
						
						astroboaClient = AstroboaClientCache.Instance.cache(astroboaClient, cacheKey);
						
					}
					else {
						logger.error("provided authorization in header (BASIC AUTH) cannot be decoded to username and password. Encoded Authorization String found in header is: " + authorization);
						throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
					}
				}
			}
			else { // login as anonymous
				
				final String anonymousCacheKey = repositoryId+IdentityPrincipal.ANONYMOUS;
				
				astroboaClient = AstroboaClientCache.Instance.get(anonymousCacheKey);
				
				if (astroboaClient == null){
				
					astroboaClient = new AstroboaClient(AstroboaClient.INTERNAL_CONNECTION);

					String permanentKey = retrievePermanentKeyForAnonymousUser(repositoryId, servletContext);
					
					astroboaClient.loginAsAnonymous(repositoryId, permanentKey);
			
					astroboaClient = AstroboaClientCache.Instance.cache(astroboaClient, anonymousCacheKey);

				}
			}
			
			logger.debug("Retrieve/Create astroboa repository client {} in  {}", System.identityHashCode(astroboaClient), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
			
			return new ResourceLocator(astroboaClient);
		
		}
		catch (CmsInvalidPasswordException e) {
			logger.error("Login to Astroboa Repository was not successfull");
			throw new WebApplicationException(HttpURLConnection.HTTP_UNAUTHORIZED);
		}
		catch (Exception e) {
			logger.error("A problem occured while connecting repository client to Astroboa Repository", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}

	private String retrievePermanentKeyForAnonymousUser(String repositoryId,ServletContext servletContext) {
		
		// get the key for permanent connections
		String anonymousUserPermanentKey = null;
		
		ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		if (springContext != null && springContext.containsBean("repositoryConfigurationBean")) {
			RepositoryConfigurationBean repositoryConfigurationBean = 
				(RepositoryConfigurationBean) springContext.getBean("repositoryConfigurationBean");
				anonymousUserPermanentKey = repositoryConfigurationBean.getAnonymousPermanentKeyPerRepository().get(repositoryId);
		}
		else {
			logger.warn("Could not find the repositoryConfigurationBean is Spring Context. The anonymous user permanent key cannot be retrieved. " +
					"Login will be performed without a permanent key. This is not a problem. However it results in creating a new user token per connection and consumes more server memory");
		}
		
		if (anonymousUserPermanentKey != null) {
			return anonymousUserPermanentKey;
		}
		else {
			logger.warn("The anonymous user permanent key cannot be retrieved in repository configuration file. " +
			"Login will be performed without a permanent key. This is not a problem. However it results in creating a new user token per connection and consumes more server memory");
			return null;
		}
	}
}
