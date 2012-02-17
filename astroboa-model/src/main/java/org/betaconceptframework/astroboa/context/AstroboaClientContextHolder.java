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
package org.betaconceptframework.astroboa.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;


/**
 * Associates one or more AstroboaClientContext with the current execution thread.
 *
 * To provide maximum flexibility, two thread local variables are defined. 
 * A map which contains all AstroboaClientContext available for current execution Thread per
 * authentication token
 * and the one and only active AstroboaClientContext.
 * 
 * This way two Astroboa clients can coexist in the same execution Thread and since inside the same Thread
 * execution is sequential, only one client at the time is active.
 * 
 * This class provides methods for accessing any client's context as well as
 * the active client's context.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaClientContextHolder {

	private static ThreadLocal<Map<String, AstroboaClientContext>> clientContextMap = new ThreadLocal<Map<String, AstroboaClientContext>>();
	private static ThreadLocal<AstroboaClientContext> activeClientContext = new ThreadLocal<AstroboaClientContext>();
	 
	public static RepositoryContext getRepositoryContextForActiveClient(){
		return ( activeClientContext.get() != null ? activeClientContext.get().getRepositoryContext() : null);
	}
	
	public static LazyLoader getLazyLoaderForActiveClient(){
		return ( activeClientContext.get() != null ? activeClientContext.get().getLazyLoader() : null);
	}
	
	public static RepositoryContext getRepositoryContextForClient(String authenticationToken){
		if (authenticationToken != null && clientContextMap.get() != null && clientContextMap.get().containsKey(authenticationToken)){
			return clientContextMap.get().get(authenticationToken).getRepositoryContext();
		}
		else{
			return null;
		}
	}
	
	public static LazyLoader getLazyLoaderForClient(String authenticationToken){
		if (authenticationToken != null && clientContextMap.get() != null && clientContextMap.get().containsKey(authenticationToken)){
			return clientContextMap.get().get(authenticationToken).getLazyLoader();
		}
		else{
			return null;
		}
	}
	
	public String toString(){
		return "RepositoryContextHoder["+
		(clientContextMap.get() == null || clientContextMap.get().isEmpty() ? 
				"No associated client contexts" : "Associated "+  clientContextMap.get().toString())
		+ ", Active Context "+ 
		( activeClientContext.get() != null ? activeClientContext.get().toString() : " none")
		+"]";
	}
	
	public static void setActiveClientContext(AstroboaClientContext clientContext){
		activeClientContext.set(clientContext);
	}
	
	public static void clearActiveClientContext(){
		activeClientContext.set(null);
	}

	
	public static Map<String, AstroboaClientContext> getClientContextMap() {
		return clientContextMap.get();
	}

	public static void setClientContextMap(
			Map<String, AstroboaClientContext> clientContextConcurrentMap) {
			clientContextMap.set(clientContextConcurrentMap);
	}

	public static void removeClientContext(String authenticationToken){
		if (authenticationToken!=null && 
				clientContextMap.get() != null && 
				clientContextMap.get().containsKey(authenticationToken)){
			clientContextMap.get().remove(authenticationToken);
		}
	}
	
	public static synchronized void registerClientContext(AstroboaClientContext clientContext, boolean activate){
		
		if (clientContextMap.get() == null){
			clientContextMap.set(new ConcurrentHashMap<String, AstroboaClientContext>());
		}
		
		clientContextMap.get().put(clientContext.getAuthenticationToken(), clientContext);
		
		if (activate){
			setActiveClientContext(clientContext);
		}
	}
	
	public static String getActiveRepositoryId(){
		
		return (getRepositoryContextForActiveClient() == null ? null :
			(getRepositoryContextForActiveClient().getCmsRepository() == null ? null :
				getRepositoryContextForActiveClient().getCmsRepository().getId()));
	}
	
	public static String getActiveAuthenticationToken(){
		
		return (getRepositoryContextForActiveClient() == null ? null :
			(getRepositoryContextForActiveClient().getSecurityContext() == null ? null :
				getRepositoryContextForActiveClient().getSecurityContext().getAuthenticationToken()));
	}
	
	public static SecurityContext getActiveSecurityContext(){
		
		return (getRepositoryContextForActiveClient() == null ? null :
			 getRepositoryContextForActiveClient().getSecurityContext());
	}

	public static AstroboaClientContext getActiveClientContext() {
		return activeClientContext.get();
	}

	public static void clearContext() {
		activeClientContext.set(null);
		clientContextMap.set(null);
		
	}
	
	public static CmsRepository getActiveCmsRepository(){
		
		return (getRepositoryContextForActiveClient() == null ? null :
			 getRepositoryContextForActiveClient().getCmsRepository());
	}

	public static void activateClientContextForAuthenticationToken(
			String authenticationToken) {
		if (authenticationToken != null && clientContextMap.get() != null && clientContextMap.get().containsKey(authenticationToken)){
			setActiveClientContext(clientContextMap.get().get(authenticationToken));
		}
	}
}
