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
package org.betaconceptframework.astroboa.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.configuration.SecurityType.PermanentUserKeyList.PermanentUserKey;
import org.betaconceptframework.astroboa.configuration.SecurityType.SecretUserKeyList.AdministratorSecretKey;
import org.betaconceptframework.astroboa.configuration.SecurityType.SecretUserKeyList.SecretUserKey;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Astroboa Repository Registry.
 * 
 * It loads the configuration for all repositories managed by the Astroboa server.
 * 
 * It expects to find configuration file astroboa-conf.xml in the path
 * specified in the system property with key 'jboss.server.config.url'
 * 
 * It also expects to find Xml Schema astroboa-conf-{version}.xsd in 
 * the classpath inside the META-INF directory.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum RepositoryRegistry{

	INSTANCE;
	
	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String ASTROBOA_CONFIGURATION_FILE = "astroboa-conf.xml";
	private String ASTROBOA_CONFIGURATION_XSD_FILEPATH = CmsConstants.FORWARD_SLASH+"META-INF"+CmsConstants.FORWARD_SLASH+CmsConstants.ASTROBOA_CONFIGURATION_XSD_FILENAME;

	private Unmarshaller configurationUnmarshaller;
	
	private Repositories repositories;

	private Map<String, RepositoryType> configurationsPerRepository = new HashMap<String, RepositoryType>();

	private Map<String,String> adminUsernamePerRepository = new HashMap<String, String>();
	
	private Map<String,Map<String,String>> permanentKeyPerUserPerRepository = new HashMap<String, Map<String,String>>();

	private File configuration = null;

	private long lastModified;
	
	private String configurationHomeDir = System.getProperty("jboss.server.config.url");
	
	private RepositoryRegistry() {

		try {
			initializeUnmarshaller();
			
			loadConfigurationXml();
			
			loadRepositoryConfigurations();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public Repositories getRepositories() {

		return repositories;
	}

	public boolean isRepositoryRegistered(String repositoryId)
	{
		if (repositoryId == null)
		{
			return false;
		}
		
		return configurationsPerRepository.containsKey(repositoryId);
	}
	
	public RepositoryType getRepositoryConfiguration(String repositoryId)
	{
		if (repositoryId == null)
		{
			return null;
		}
		
		return configurationsPerRepository.get(repositoryId);
	}
	
	
	public String retrieveAdminUsernameForRepository(String repositoryId) {

		if (repositoryId != null){
			return adminUsernamePerRepository.get(repositoryId);
		}
		
		return null;
	}
	
	public String getPermanentKeyForUser(String repositoryId, String username) {
		
		if (StringUtils.isNotBlank(repositoryId) && StringUtils.isNotBlank(username)){
			
			if (permanentKeyPerUserPerRepository.containsKey(repositoryId)){
				
				Map<String, String> permanentKeys = permanentKeyPerUserPerRepository.get(repositoryId);
				
				if (permanentKeys.containsKey(username)){
					return permanentKeys.get(username);
				}
			}
		}
		
		return null;
		
	}
	
	public boolean isSecretKeyValidForUser(String repositoryId, String username, String key) {
		
		if (StringUtils.isBlank(repositoryId) || StringUtils.isBlank(key) || StringUtils.isBlank(username)){
			return false;
		}

		if (!isRepositoryRegistered(repositoryId)){
			return false;
		}

		RepositoryType repositoryConfiguration = getRepositoryConfiguration(repositoryId);

		final SecurityType security = repositoryConfiguration.getSecurity();
		
		if (security == null || repositoryConfiguration.getSecurity().getSecretUserKeyList() == null)
		{
			return false;
		}

		//Check if user name is administrator
		AdministratorSecretKey administratorEntry = security.getSecretUserKeyList().getAdministratorSecretKey();
		
		if (administratorEntry != null && StringUtils.equals(administratorEntry.getUserid(), username))
		{
			return StringUtils.equals(administratorEntry.getKey(), key);
		}
		
		List<SecretUserKey> secretKeys = repositoryConfiguration.getSecurity().getSecretUserKeyList().getSecretUserKey();

		for (SecretUserKey secretUserKey : secretKeys){

			if (StringUtils.equals(secretUserKey.getKey(), key)){ 

				List<String> userIds = Arrays.asList(StringUtils.split(secretUserKey.getUserid(), ","));

				return userIds.contains("*") || userIds.contains(username);

			}
		}

		return false;

	}


	
	public Map<String, RepositoryType> getConfigurationsPerRepositoryId() {
		return configurationsPerRepository;
	}


	public void loadRepositoryConfigurations() {
		
		
		InputStream is = null;
		
		try{
			
			if (configurationHasChanged() || repositories == null){

				loadConfigurationXml();
			
				Map<String, RepositoryType> configurationsPerRepository = new HashMap<String, RepositoryType>();
	
				Map<String,String> adminUsernamePerRepository = new HashMap<String, String>();
				
				Map<String,Map<String,String>> permanentKeyPerUserPerRepository = new HashMap<String, Map<String,String>>();
	
				is = new FileInputStream(configuration);
	
				Repositories Repositories = (Repositories)configurationUnmarshaller.unmarshal(new StreamSource(is));
				
				List<RepositoryType> repositoryConfigurations = Repositories.getRepository();
	
				if (repositoryConfigurations != null && ! repositoryConfigurations.isEmpty()){
	
					for (RepositoryType repositoryConfiguration: repositoryConfigurations){
	
						String repositoryId = repositoryConfiguration.getId();
						
						configurationsPerRepository.put(repositoryId, repositoryConfiguration);
						
						extractAdminUsername(repositoryConfiguration, adminUsernamePerRepository);
						
						extractPermanentKeyPerUser(repositoryConfiguration, permanentKeyPerUserPerRepository);
					}
				}
				
				this.configurationsPerRepository = configurationsPerRepository;
				this.adminUsernamePerRepository = adminUsernamePerRepository;
				this.permanentKeyPerUserPerRepository = permanentKeyPerUserPerRepository;
				this.repositories = Repositories;
				
				logger.info("Astroboa Configuration {} has been loaded", configuration.getAbsolutePath());
			}
		}
		catch(Exception e){
			throw new CmsException(e);
		}
		finally {
			if (is != null){
				try{
					is.close();
				}
				catch(Exception e){
					//Close quietly
				}
			}
		}
	}


	private void initializeUnmarshaller() throws Exception, JAXBException {
		
		Schema configurationXmlSchema = loadXmlSchema();

		configurationUnmarshaller = initJaxbContext().createUnmarshaller();

		configurationUnmarshaller.setSchema(configurationXmlSchema);
	}


	/**
	 * @param repositoryConfiguration
	 * @param permanentKeyPerUserPerRepository 
	 */
	private void extractPermanentKeyPerUser(
			RepositoryType repositoryConfiguration, Map<String, Map<String, String>> permanentKeyPerUserPerRepository) {
		
		if (repositoryConfiguration != null && 
				repositoryConfiguration.getSecurity() != null && 
				repositoryConfiguration.getSecurity().getPermanentUserKeyList() != null && 
				repositoryConfiguration.getSecurity().getPermanentUserKeyList().getPermanentUserKey() != null){

			Map<String,String> permanentKeysPerUser = new HashMap<String,String>();
			
			List<PermanentUserKey> permanentUserKeys = repositoryConfiguration.getSecurity().getPermanentUserKeyList().getPermanentUserKey();
			
			for (PermanentUserKey permanentUserKey : permanentUserKeys) {
				
				if (StringUtils.isNotBlank(permanentUserKey.getUserid())){

					String[] userIds =  permanentUserKey.getUserid().split(",");
					String key = permanentUserKey.getKey();
					
					for (String userId : userIds){
						permanentKeysPerUser.put(userId, key);
					}
				}
			}
			
			permanentKeyPerUserPerRepository.put(repositoryConfiguration.getId(), permanentKeysPerUser);
		}
		
		
	}


	private void extractAdminUsername(RepositoryType repositoryConfiguration, Map<String, String> adminUsernamePerRepository) {
		
		final SecurityType security = repositoryConfiguration.getSecurity();
		
		if (security != null &&
				repositoryConfiguration.getSecurity().getSecretUserKeyList() != null &&
				security.getSecretUserKeyList().getAdministratorSecretKey() != null){
			adminUsernamePerRepository.put(repositoryConfiguration.getId(), security.getSecretUserKeyList().getAdministratorSecretKey().getUserid());
		}

	}

	private Schema loadXmlSchema() throws Exception {
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new W3CRelatedSchemaEntityResolver());
		
		//Search in classpath at root level
		URL configurationSchemaURL = this.getClass().getResource(ASTROBOA_CONFIGURATION_XSD_FILEPATH);
		
		/*if (configurationSchemaURL ==null){
				
			//Expect to find configuration xsd in jboss_home/server/default/conf
			if (configurationHomeDir != null){
				//We expect to find xml in JBOSS-HOME/server/default/conf directory
				configurationSchemaURL = new URL(configurationHomeDir+CmsConstants.ASTROBOA_CONFIGURATION_XSD_FILENAME);
			}
		}*/
			
		if (configurationSchemaURL ==null){
			throw new Exception("Could not find "+ASTROBOA_CONFIGURATION_XSD_FILEPATH+ " nor in "+configurationHomeDir+CmsConstants.ASTROBOA_CONFIGURATION_XSD_FILENAME+ " in classpath");
		}

		
		return schemaFactory.newSchema(configurationSchemaURL);
	}


	private static JAXBContext initJaxbContext() throws JAXBException  {

		return JAXBContext.newInstance("org.betaconceptframework.astroboa.configuration");
	}

	private void loadConfigurationXml() throws MalformedURLException,
			IOException, Exception {
		
		if (configurationHomeDir != null){
			//We expect to find xml in JBOSS-HOME/server/default/conf directory
			if (configurationHomeDir.startsWith("file:")){
				configurationHomeDir = StringUtils.removeStart(configurationHomeDir, "file:");
			}
			
			configuration = new File(configurationHomeDir+File.separator+ASTROBOA_CONFIGURATION_FILE);
		}
	
		if (configuration ==null || ! configuration.exists()){
			throw new Exception("Could not find "+ASTROBOA_CONFIGURATION_FILE+ " in path "+configuration.getAbsolutePath());
		}
		
		lastModified = configuration.lastModified();
		
	}


	public boolean configurationHasChanged() {
		
		return configuration == null || lastModified < configuration.lastModified();
	}


	public String getDefaultServerURL() {
		
		if (repositories != null){
			return repositories.getServerURL();
		}
		
		return null;
	}


	public String getDefaultJaasApplicationPolicyName() {
		if (repositories != null){
			return repositories.getJaasApplicationPolicyName();
		}
		
		return null;
	}


	public String getDefaultIdentityStoreId() {
		
		if (repositories != null){
			return repositories.getIdentityStoreRepositoryId();
		}
		
		return null;
	}


	public Integer getDefaultAuthenticationTokenTimeout() {
		
		if (repositories != null){
			return repositories.getAuthenticationTokenTimeout();
		}
		
		return null;
	}


	public File getConfiguration() {
		return configuration;
	}


	public boolean isConsistencyCheckEnabled() {

		if (repositories != null){
			return repositories.isCheckConsistency();
		}

		return false;
	}
	
}
