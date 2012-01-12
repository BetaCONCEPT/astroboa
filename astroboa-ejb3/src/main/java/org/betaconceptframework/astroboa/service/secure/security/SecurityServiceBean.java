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
package org.betaconceptframework.astroboa.service.secure.security;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.exception.TokenExpirationException;
import org.betaconceptframework.astroboa.api.service.CacheService;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Service providing methods for managing security assets, like authentication token
 * 
 * @Service An extension offered by JBoss EJB 3.0 is the notion of a @org.jboss.annotation.ejb.Service annotated bean. 
 * They are singleton beans and are not pooled, so only one instance of the bean exists in the server. 
 * They can have both @Remote and @Local interfaces so they can be accessed by java clients. 
 * When different clients look up the interfaces for @Service beans, all clients will work on 
 * the same instance of the bean on the server. When installing the bean it gets given a 
 * JMX ObjectName in the MBean server it runs on. The default is
 *
 *	jboss.j2ee:service=EJB3,name=<Fully qualified name of @Service bean>,type=service
 *	You can override this default ObjectName by specifying the objectName attribute of the @Service annotation.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Singleton(name="SecurityService")
@Local({SecurityService.class})
public class SecurityServiceBean implements SecurityService {

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	private ConcurrentHashMap<String, AuthenticationTokenEntry> authenticationTokenEntries = new ConcurrentHashMap<String, AuthenticationTokenEntry>();

	private final Timer authenticationTokenRemovalTimer = new Timer();

	@Resource(name="astroboa.engine.context", mappedName="java:jboss/astroboa.engine.context")
	private ApplicationContext springManagedRepositoryServicesContext;

	private CacheService cacheService;	

	@PostConstruct
	public void start() throws Exception {
		cacheService = (CacheService) springManagedRepositoryServicesContext.getBean("cacheService");
	}

	public void registerAndActivateClientContextForAuthenticationToken(String authenticationToken){

		if (StringUtils.isBlank(authenticationToken)){
			throw new CmsException("Authentication token is null");
		}

		if (authenticationTokenEntries.containsKey(authenticationToken)){

			AuthenticationTokenEntry authenticationTokenEntry = authenticationTokenEntries.get(authenticationToken);

			if (authenticationTokenEntry == null){
				throw new TokenExpirationException("No Client Context found for authentication token "+authenticationToken);
			}
			
			//Place repository context to current thread
			AstroboaClientContext clientContext = authenticationTokenEntry.getClientContext();
			
			AstroboaClientContextHolder.registerClientContext(clientContext, true);
			
			if (logger.isDebugEnabled()){
				logger.debug("Successfully activate in {} {}", Thread.currentThread().getName(), clientContext);
			}	

			resetIdleTime(authenticationToken, authenticationTokenEntry);
		}
		else{
			throw new TokenExpirationException("No Client Context found for authentication token "+authenticationToken);
		}
	}

	private void resetIdleTime(String authenticationToken,
			AuthenticationTokenEntry authenticationTokenEntry) {
		//Reset idle time
		if (authenticationTokenEntry.getAuthenticationTokenTimeout() > 0){
			RemoveAuthenticationTokenTask newTask = new RemoveAuthenticationTokenTask(authenticationToken, this);
			authenticationTokenRemovalTimer.schedule(newTask, authenticationTokenEntry.getAuthenticationTokenTimeout());
			authenticationTokenEntry.resetIdleTime(newTask);
		}
		else{
			authenticationTokenEntry.resetIdleTime(null);
		}
	}

	public void addClientContexToValidContextMap(AstroboaClientContext clientContext, boolean authenticactionTokenNeverExpires) {

		if (clientContext== null ){
			throw new CmsException("Found no active astroboa client context associated with current thread "+Thread.currentThread().toString());
		}

		String authenticationToken = clientContext.getAuthenticationToken();

		if (StringUtils.isBlank(authenticationToken)){
			throw new CmsException("Found no authentication token associated with current thread "+Thread.currentThread().toString());
		}

		AuthenticationTokenEntry newAuthenticationTokenEntry = new AuthenticationTokenEntry(clientContext);
		
		AuthenticationTokenEntry authenticationTokenEntry = authenticationTokenEntries.putIfAbsent(authenticationToken, newAuthenticationTokenEntry);
		
		if (authenticationTokenEntry == null){
			authenticationTokenEntry = newAuthenticationTokenEntry;
		}

		if (logger.isDebugEnabled()){
			logger.debug("AuthenticationToken Map {} entry for authenticationToken {}", 
					(authenticationTokenEntries.containsKey(authenticationToken) ? "contains " : " does not contain " ), 
					authenticationToken);
		}

		if (! authenticactionTokenNeverExpires){
			
			//Default value
			int authenticationTokenTimeout = 30 * 60 * 1000;
			
			//Create task to invalidate authentication token
			if (clientContext.getRepositoryContext() != null && clientContext.getRepositoryContext().getSecurityContext() != null){
				authenticationTokenTimeout = clientContext.getRepositoryContext().getSecurityContext().getAuthenticationTokenTimeout() * 60 * 1000;
			}
			
			authenticationTokenEntry.setAuthenticationTokenTimeout(authenticationTokenTimeout);
			
			if (authenticationTokenTimeout > 0){
				RemoveAuthenticationTokenTask  removeAuthenticationTokenTask = new RemoveAuthenticationTokenTask(authenticationToken, this);
			
				authenticationTokenEntry.resetIdleTime(removeAuthenticationTokenTask);
				authenticationTokenRemovalTimer.schedule(removeAuthenticationTokenTask, authenticationTokenTimeout);
			}
			else{
				authenticationTokenEntry.resetIdleTime(null);
			}
		}
		else{
			authenticationTokenEntry.setAuthenticationTokenTimeout(-1);
		}

		if (logger.isDebugEnabled()){
			logger.debug("Add {} to context map(size:{}) for {}. AuthenticationToken timeout {} ms (-1 stands for non expireable)",
				new Object[]{clientContext, authenticationTokenEntries.size(), Thread.currentThread().getName(), authenticationTokenEntry.getAuthenticationTokenTimeout()});
		}

	}

	public void purgeAuthenticationToken(String authenticationToken) {

		if (authenticationToken != null){

			//Remove all related content from cache
			if (cacheService != null){
				cacheService.clearCacheForAuthenticationToken(authenticationToken);
			}
			else{
				logger.warn("Could not find cache service. Authentication Token related cached items will be expired once their region expires");
			}

			//Remove authentication token from map
			authenticationTokenEntries.remove(authenticationToken);

			logger.debug("Authentication Token {} has been successfully removed from cache and map (size: {})", authenticationToken,authenticationTokenEntries.size());
		}

	}


	public  void resetAuthenticationTokenTimeout(String authenticationToken) {
		if (StringUtils.isBlank(authenticationToken)){
			logger.warn("Authentication token is null. Cannot reset token timeout ");
			return;
		}

		AuthenticationTokenEntry authenticationTokenEntry = authenticationTokenEntries.get(authenticationToken);

		if (authenticationTokenEntry != null){

			resetIdleTime(authenticationToken, authenticationTokenEntry);

			if (logger.isDebugEnabled()){
				logger.debug("Successfully reset authentication timeout in {} {}", Thread.currentThread().getName(), authenticationTokenEntry.getClientContext());
			}
		}
		else{
			logger.warn("No Client Context found for authentication token {}. Cannot reset token timeout ", authenticationToken);
		}


	}

	@Override
	public boolean tokenHasExpired(String authenticationToken) {
		return authenticationToken == null || ( authenticationTokenEntries != null && ! authenticationTokenEntries.containsKey(authenticationToken));
	}


}
