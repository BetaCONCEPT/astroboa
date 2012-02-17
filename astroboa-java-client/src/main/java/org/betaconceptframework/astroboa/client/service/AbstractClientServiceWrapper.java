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
package org.betaconceptframework.astroboa.client.service;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.jnp.interfaces.NamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class containing common methods for remote services wrappers.
 * 
 * This class assumes that access is done through JBoss JNDI and 
 * that remoteService's JNDI name is remote service's class name plus "/remote".
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class AbstractClientServiceWrapper {

	final Logger logger = LoggerFactory.getLogger(getClass());

	String serverHostNameOrIpAndPortDelimitedWithSemiColon;
	
	final static String DEFAULT_PORT = "1099";
	final static String ASTROBOA_EAR_CONTEXT = "astroboa";

	AstroboaClient client;

	boolean successfullyConnectedToRemoteService;
	
	
	public AbstractClientServiceWrapper(AstroboaClient client, String serverHostNameAndorPortToConnect){
		this.client = client;
		
		initialize(serverHostNameAndorPortToConnect);
	}
	
	
	abstract void resetService();
	abstract boolean loadService(boolean loadLocalService);

	
	
	private void initialize(String serverHostNameAndorPortToConnect){

		if (StringUtils.isBlank(serverHostNameAndorPortToConnect)){
			//Set default URL if none provided
			serverHostNameAndorPortToConnect = AstroboaClient.INTERNAL_CONNECTION+":"+DEFAULT_PORT;
		}
		else {
			//Set port if not provided
			if (!serverHostNameAndorPortToConnect.contains(":")){
				serverHostNameAndorPortToConnect = serverHostNameAndorPortToConnect.concat(":"+DEFAULT_PORT);
			}
			else {
				if (StringUtils.isBlank(StringUtils.substringAfterLast(serverHostNameAndorPortToConnect, ":"))){
					serverHostNameAndorPortToConnect = serverHostNameAndorPortToConnect.concat(DEFAULT_PORT);
				}
			}
		}
		
		resetService();

		this.serverHostNameOrIpAndPortDelimitedWithSemiColon = serverHostNameAndorPortToConnect;
		
		loadService();
	}

	
	private void loadService(){
				
			boolean successfullyConnectedToLocalService  = false;
			successfullyConnectedToRemoteService  = false;
			
			if (clientWantsToConnectToInternalServer()){
				successfullyConnectedToLocalService  = loadService(true);
			}
			else{
				
				//Client does not want to connect  internal server
				//Nevertheless it may be the case that 'localhost' or '127.0.0.1'
				//has been specified. If so check first if an internal server
				//is activated and if not then try a connection to a remote server
				if (clientsWantsToConnectToLocalhost()){
					successfullyConnectedToLocalService  = loadService(true);
				}
				
				if (!successfullyConnectedToLocalService){
					//Client wants to connect to a remote server
					successfullyConnectedToRemoteService = loadService(false);
				}
				
			}

			if (! successfullyConnectedToLocalService && ! successfullyConnectedToRemoteService ){
				throw new CmsException("Unable to connect to local or remote server "+ serverHostNameOrIpAndPortDelimitedWithSemiColon);
			}
			
			logger.debug("Connected to server {} {}",serverHostNameOrIpAndPortDelimitedWithSemiColon, 
					(successfullyConnectedToLocalService? " locally ": " remotely"));
			
	}
	
	private boolean clientsWantsToConnectToLocalhost() {
		return serverHostNameOrIpAndPortDelimitedWithSemiColon != null  && 
		  ( serverHostNameOrIpAndPortDelimitedWithSemiColon.toLowerCase().startsWith("localhost:") || 
				  serverHostNameOrIpAndPortDelimitedWithSemiColon.toLowerCase().startsWith("127.0.0.1:")
				  );
	}
	/*
	 * A Client is connected to Local Server if n server host port is provided
	 * or 'localhost' value is specified
	 */
	private boolean clientWantsToConnectToInternalServer(){
		return serverHostNameOrIpAndPortDelimitedWithSemiColon == null || 
			serverHostNameOrIpAndPortDelimitedWithSemiColon.toLowerCase().startsWith(AstroboaClient.INTERNAL_CONNECTION.toLowerCase()+":");
	}


	
	<R> Object connectToLocalService(Class<R> serviceClass){
		
		return connectToLocalService(serviceClass, null);
	}
	
	<R> Object connectToLocalService(Class<R> serviceClass, String jndiName){
		try {
				InitialContext context = new InitialContext();
				
				//First check to see if initial context has been initiated at all
				Hashtable<?, ?> env = context.getEnvironment();
				 String initialContextFactoryName = env != null ?
					        (String)env.get(Context.INITIAL_CONTEXT_FACTORY) : null;
					        
				if (initialContextFactoryName != null){
					
					if (StringUtils.isBlank(jndiName)){
						/*
						 * In JBoss 5 and onwards all Astroboa EJB beans have been annotated using the following annotations
						 * 
						 * @Local({<local-service-api-name>.class}) 
						 * @Remote({<remote-service-api-name>.class}) 
						 * @Stateless("<service-name>")
						 *  
						 *    where "<local-service-api-name>" and "<remote-service-api-name>" is the name of the local 
						 *    and remote interface respectively and "<service-name>" which is the simple name of the local interface
						 *    
						 * For example, EJB for ContentServiceSecure is annotated as
						 *    
						 *    @Local(ContentServiceSecure.class)
						 *    @Remote(RemoteContentServiceSecure.class)
						 *    @Stateless("ContentServiceSecure")
						 *    
						 * JBoss 5 EJB engine exposes Astroboa EJB beans under the Global JNDI namespace, using the following
						 * JNDI names 
						 *    <service-name>/local
						 *    <service-name>/local-<fully-qualified-name-of-local-interface>
						 *    
						 *    <service-name>/remote
						 *    <service-name>/remote-<fully-qualified-name-of-remote-interface>
						 *    
						 *    Therefore, for the above example the JNDI names of the local and remote ejb beans for ContentServiceSecure are  
						 *    
						 *    ContentServiceSecure/local
						 *    ContentServiceSecure/local-org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure
						 *     
						 *    ContentServiceSecure/remote
						 *    ContentServiceSecure/remote-org.betaconceptframework.astroboa.api.service.secure.RemoteContentServiceSecure
						 *    
						 *    The JNDI view as displayed by JMX-Console for "ContentServiceSecure" EJB Bean is 
						 *    
						 * +- ContentServiceSecure (class: org.jnp.interfaces.NamingContext)
  						 * |   +- local (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure)
  						 * |   +- remote-org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure)
  						 * |   +- local-org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure)
  						 * |   +- remote (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure)
						 * 
						 *   
						 *  So in oder to calculate the JNDI name for the provided service, 
						 *  we use the simple name of the class suffixed with "/local"
						 *    
						 */
						jndiName = serviceClass.getSimpleName()+"/local";
					}
					
					try{
						Object serviceReference = context.lookup(jndiName);
						
						if (serviceReference instanceof NamingContext && ! jndiName.endsWith("/local")){
							//JNDIName is provided by the user and the object it references is an instance of NamingContext
							//Since JNDIName does not end with "/local" , try to locate the local service under the returned NamingContext
							return ((NamingContext)serviceReference).lookup("local");
						}
						
						return  serviceReference;
					}
					catch (Exception e){
						logger.warn("Could not connect to local service "+ serviceClass.getSimpleName(), e);
						//try with ASTROBOA_EAR_CONTEXT
						return context.lookup(ASTROBOA_EAR_CONTEXT+"/"+jndiName);
						
					}
				}
				else{
					//No initial context factory exists. That means that calling
					//lookup will result in exception.
					logger.warn("InitialContext has not been initialized at all. No {} found",Context.INITIAL_CONTEXT_FACTORY);
					return null;
				}
				
		} catch (Exception e) {
			logger.warn("",e);
			return null;
		}

		
	}

	<R> Object connectToRemoteService(Class<R> serviceClass){
		return connectToRemoteService(serviceClass, null);
	}
	
	<R> Object connectToRemoteService(Class<R> serviceClass, String jndiName){
		//Call this method to actually get the referenced object
		try {
			
			//Context environmental properties specific to JBoss Naming Context
			Properties jndiEnvironmentProperties = new Properties();
			jndiEnvironmentProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			jndiEnvironmentProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			jndiEnvironmentProperties.put(Context.PROVIDER_URL, "jnp://"+serverHostNameOrIpAndPortDelimitedWithSemiColon);

			InitialContext context = new InitialContext(jndiEnvironmentProperties);
			
			if (context == null){
				throw new Exception("No Context provided for server "+ serverHostNameOrIpAndPortDelimitedWithSemiColon);
			}
			
			if (StringUtils.isBlank(jndiName)){
				/*
				 * In JBoss 5 and onwards all Astroboa EJB beans have been annotated using the following annotations
				 * 
				 * @Local({<local-service-api-name>.class}) 
				 * @Remote({<remote-service-api-name>.class}) 
				 * @Stateless("<service-name>")
				 *  
				 *    where "<local-service-api-name>" and "<remote-service-api-name>" is the name of the local 
				 *    and remote interface respectively and "<service-name>" which is the simple name of the local interface
				 *    
				 * For example, EJB for ContentServiceSecure is annotated as
				 *    
				 *    @Local(ContentServiceSecure.class)
				 *    @Remote(RemoteContentServiceSecure.class)
				 *    @Stateless("ContentServiceSecure")
				 *    
				 * JBoss 5 EJB engine exposes Astroboa EJB beans under the Global JNDI namespace, using the following
				 * JNDI names 
				 *    <service-name>/local
				 *    <service-name>/local-<fully-qualified-name-of-local-interface>
				 *    
				 *    <service-name>/remote
				 *    <service-name>/remote-<fully-qualified-name-of-remote-interface>
				 *    
				 *    Therefore, for the above example the JNDI names of the local and remote ejb beans for ContentServiceSecure are  
				 *    
				 *    ContentServiceSecure/local
				 *    ContentServiceSecure/local-org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure
				 *     
				 *    ContentServiceSecure/remote
				 *    ContentServiceSecure/remote-org.betaconceptframework.astroboa.api.service.secure.RemoteContentServiceSecure
				 *    
				 *    The JNDI view as displayed by JMX-Console for "ContentServiceSecure" EJB Bean is 
				 *    
				 * +- ContentServiceSecure (class: org.jnp.interfaces.NamingContext)
					 * |   +- local (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure)
					 * |   +- remote-org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure)
					 * |   +- local-org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.ContentServiceSecure)
					 * |   +- remote (class: Proxy for: org.betaconceptframework.astroboa.api.service.secure.remote.RemoteContentServiceSecure)
				 * 
				 *   
				 *  So in oder to calculate the JNDI name for the provided service, 
				 *  we use the simple name of the class suffixed with "/local"
				 *    
				 */
				jndiName = serviceClass.getSimpleName()+"/remote";
			}
			
			//Enable this when astroboa.ear is used
			//return (R) context.lookup(ASTROBOA_EAR_CONTEXT+"/"+serviceClass.getSimpleName()+"/remote");
			Object serviceReference = context.lookup(jndiName);
			
			if (serviceReference instanceof NamingContext && ! jndiName.endsWith("/remote")){
				//JNDIName is provided by the user and the object it references is an instance of NamingContext
				//Since JNDIName does not end with "/remote" , try to locate the remote service under the returned NamingContext
				return ((NamingContext)serviceReference).lookup("remote");
			}
			
			return  serviceReference;
			
		} catch (Exception e) {
			logger.error("",e);
			return null;
		}
	}



	public String getAuthenticationToken() {

		return client.getAuthenticationToken();

	}
	
	public boolean isClientConnectedToARemoteServer()
	{
		return successfullyConnectedToRemoteService;
	}
}
