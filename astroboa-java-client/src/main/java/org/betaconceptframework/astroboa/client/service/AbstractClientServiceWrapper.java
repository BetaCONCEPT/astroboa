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

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
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

	final static String DEFAULT_PORT = "4447";
	final static String ASTROBOA_EAR_APPLICATION_NAME = "astroboa";
	final static String ASTROBOA_EJB_MODULE_NAME = "astroboa-ejb3";
	
	/*
	 *	distinct-name : This is a JBoss AS7 specific name which can be optionally assigned to the deployments that are deployed on the server. 
	 *				More about the purpose and usage of this will be explained in a separate chapter. If a deployment doesn't use distinct-name then, 
	 *				use an empty string in the JNDI name, for distinct-name.
	 */
	final static String ASTROBOA_EAR_DEPLOYMENT_DISTINCT_NAME = "";
	
	final Logger logger = LoggerFactory.getLogger(getClass());

	private String serverHostNameOrIp;
	private String port;
	
	String serverHostNameOrIpAndPortDelimitedWithSemiColon;
	
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
		this.serverHostNameOrIp = StringUtils.substringBefore(serverHostNameAndorPortToConnect, ":");
		this.port = StringUtils.substringAfter(serverHostNameAndorPortToConnect, ":");
		
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
				if (StringUtils.isBlank(jndiName)){
					jndiName = createJNDIBindingNameForService(serviceClass, true);
				}
				
				try{
					return new InitialContext().lookup(jndiName);
				}
				catch (Exception e){
					logger.warn("Could not connect to local service "+ serviceClass.getSimpleName(), e);
					//try with '#' instead of '!'
					if (jndiName.contains("!")){
						return new InitialContext().lookup(jndiName.replace("!", "#"));
					}
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
			
			/*
			 * According to the documentation 
			 * https://docs.jboss.org/author/display/AS71/EJB+invocations+from+a+remote+client+using+JNDI
			 * 
			 * and to this thread https://community.jboss.org/message/647202#647202
			 * 
			 * JBoss AS7 has changed the procedure for a remote client invocation.
			 * We have to use these JBoss Specific API in order to be able to dynamically provide information
			 * about the server host name or ip and the port.
			 * 
			 * Bear in mind that in these properties no username and password is provided.
			 * This means that the JBoss AS7 which hosts the Astroboa must have its 
			 * security-realm for the subsystem remoting disabled.
			 * 
			 * This code must be reviewed as not only it contains JBoss Specific API but also
			 * remoting subsystem must be active without security
			 */
			Properties ejbClientConfigurationProperties = new Properties();
			ejbClientConfigurationProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED","false");
			ejbClientConfigurationProperties.put("remote.connections", "default");
			ejbClientConfigurationProperties.put("remote.connection.default.host", serverHostNameOrIp);
			ejbClientConfigurationProperties.put("remote.connection.default.port", port);
			ejbClientConfigurationProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

			final EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(ejbClientConfigurationProperties);
			 
			// EJB client context selection is based on selectors. So let's create a ConfigBasedEJBClientContextSelector which uses our EJBClientConfiguration created in previous step
			final ContextSelector<EJBClientContext> ejbClientContextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
			// Now let's setup the EJBClientContext to use this selector
			final ContextSelector<EJBClientContext> previousSelector = EJBClientContext.setSelector(ejbClientContextSelector);
			////////////////
			
			
			//Context environmental properties specific to JBoss Naming Context
			Properties jndiEnvironmentProperties = new Properties();
			//jndiEnvironmentProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			jndiEnvironmentProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			//jndiEnvironmentProperties.put(Context.PROVIDER_URL, "jnp://"+serverHostNameOrIpAndPortDelimitedWithSemiColon);

			InitialContext context = new InitialContext(jndiEnvironmentProperties);
			
			if (StringUtils.isBlank(jndiName)){
				jndiName = createJNDIBindingNameForService(serviceClass, false);
			}

			try{
				return context.lookup(jndiName);
			}
			catch (Exception e){
				logger.warn("Could not connect to local service "+ serviceClass.getSimpleName(), e);
				//try with '#' instead of '!'
				if (jndiName.contains("!")){
					return context.lookup(jndiName.replace("!", "#"));
				}
				return null;
			}
			
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
	
	private <R> String createJNDIBindingNameForService(Class<R> serviceClass, boolean local){

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
		 *  we use the simple name of the class suffixed with "/local" or "remote"
		 *    
		 */
		//return serviceClass.getSimpleName()+"/"+ (local?"local":"remote");
		
		/*
		 * In EJB 3.1, the global JNDI names would follow a standard pattern that is portable across all the different application servers: 
		 *
		 *	java:global[/<application-name>]/<module-name>/<bean-name>#<interface-name>
		 *  
		 *  In JBoss AS 7 the JNDI bindings for session bean named TaxonomyServiceSecure in deployment unit subdeployment "astroboa-ejb3.jar" of deployment "astroboa.ear" 
		 *  are as follows:
			 *
		 *	java:global/astroboa/astroboa-ejb3/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTaxonomyServiceSecure
		 *	java:app/astroboa-ejb3/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTaxonomyServiceSecure
		 *	java:module/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTaxonomyServiceSecure
		 *	java:jboss/exported/astroboa/astroboa-ejb3/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTaxonomyServiceSecure
		 *	java:global/astroboa/astroboa-ejb3/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.TaxonomyServiceSecure
		 *	java:app/astroboa-ejb3/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.TaxonomyServiceSecure
		 *	java:module/TaxonomyServiceSecure!org.betaconceptframework.astroboa.api.service.secure.TaxonomyServiceSecure
		 *
		 * Note the '!' instead of the '#' character in JBoss
	     * 
	     * For remote JNDI connections see more info in 
	     * https://docs.jboss.org/author/display/AS71/EJB+invocations+from+a+remote+client+using+JNDI
		*/ 
		if (local){
			return "java:global/"+ASTROBOA_EAR_APPLICATION_NAME+"/"+ASTROBOA_EJB_MODULE_NAME+"/"+serviceClass.getSimpleName()+"!"+ serviceClass.getName();
		}
		else{
			/*
			 *  For remote JNDI connections see more info in 
		     *  https://docs.jboss.org/author/display/AS71/EJB+invocations+from+a+remote+client+using+JNDI
		     *  
		     *  Stateless beans
		     *  ejb:<app-name>/<module-name>/<distinct-name>/<bean-name>!<fully-qualified-classname-of-the-remote-interface>
		     *  
		     *  Statefull beans
		     *  ejb:<app-name>/<module-name>/<distinct-name>/<bean-name>!<fully-qualified-classname-of-the-remote-interface>?stateful
		     *  
		     *  
		     *   app-name : This is the name of the .ear (without the .ear suffix) that you have deployed on the server and contains your EJBs.
    		 *				Java EE 6 allows you to override the application name, to a name of your choice by setting it in the application.xml. 
    		 *				If the deployment uses uses such an override then the app-name used in the JNDI name should match that name.
    		 *				EJBs can also be deployed in a .war or a plain .jar. In such cases where the deployment isn't an .ear file, 
    		 *				then the app-name must be an empty string, while doing the lookup.
    		 *	module-name : This is the name of the .jar (without the .jar suffix) that you have deployed on the server and the contains your EJBs. 
    		 *				If the EJBs are deployed in a .war then the module name is the .war name (without the .war suffix). Java EE 6 allows you to 
    		 *				override the module name, by setting it in the ejb-jar.xml/web.xml of your deployment. If the deployment uses such an override 
    		 *				then the module-name used in the JNDI name should match that name. Module name part cannot be an empty string in the JNDI name.
    		 *	distinct-name : This is a JBoss AS7 specific name which can be optionally assigned to the deployments that are deployed on the server. 
    		 *				More about the purpose and usage of this will be explained in a separate chapter. If a deployment doesn't use distinct-name then, 
    		 *				use an empty string in the JNDI name, for distinct-name.
    		 *	bean-name : This is the name of the bean for which you are doing the lookup. The bean name is typically the unqualified classname 
    		 *				of the bean implementation class, but can be overriden through either ejb-jar.xml or via annotations. The bean name part cannot 
    		 *				be an empty string in the JNDI name.
    		 *  fully-qualified-classname-of-the-remote-interface : This is the fully qualified class name of the interface for which you are doing the lookup. 
    		 *  			The interface should be one of the remote interfaces exposed by the bean on the server. The fully qualified class name part cannot be 
    		 *  			an empty string in the JNDI name. 
		     *  
		     *  All Astroboa EJB3 beans are stateless
		     *  
			*/ 
			String distinctName = "";
			return "ejb:"+ASTROBOA_EAR_APPLICATION_NAME+"/"+ASTROBOA_EJB_MODULE_NAME+"/"+distinctName+"/"+serviceClass.getSimpleName()+"!"+ serviceClass.getName();
		}
	}
}
