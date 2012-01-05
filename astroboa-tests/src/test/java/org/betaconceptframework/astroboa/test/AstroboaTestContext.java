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
package org.betaconceptframework.astroboa.test;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.jcr.jackrabbit.RepositoryFactoryBean;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum AstroboaTestContext{

	INSTANCE;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	private List<RepositoryFactoryBean> repositoryFactories = new ArrayList<RepositoryFactoryBean>();
	
	private AstroboaTestContext(){

		try {
			
			//System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory");
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.cache.transaction.DummyContextFactory");
			
			System.setProperty(Context.PROVIDER_URL, "localhost");
			
			System.setProperty("jboss.server.config.url", new ClassPathResource("/").getFile().getAbsolutePath());

			InitialContext context = new InitialContext();

			configureRepository(TestConstants.TEST_REPOSITORY_ID, context);
			configureRepository(TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID, context);
			configureRepository(TestConstants.TEST_CLONE_REPOSITORY_ID, context);
			
			//Initialize Spring - Order is SIGNIFICANT. First set all JNDI resources and then initialize Spring context
			applicationContext = new ClassPathXmlApplicationContext(getConfigLocations());
			


		} catch (Exception e) {
			throw new CmsException(e);
		}
	}
	
	public void configureRepository(String repositoryId, InitialContext context) throws Exception
	{
		//In case where following directories are not removed
		//before creation of JCR repository an exception is
		//thrown.
		//Note that if this test runs through Maven and clean goal
		//has been executed then this code finds no directory to delete
		removeRepositoryResources(repositoryId);
		
		RepositoryFactoryBean repositoryFactory = new RepositoryFactoryBean();
		repositoryFactory.setConfiguration(new ClassPathResource(repositoryId+"/repository-test.xml"));
		repositoryFactory.setHomeDir(new ClassPathResource(repositoryId));
		repositoryFactory.afterPropertiesSet();
		
		context.bind(repositoryId+"JcrJNDI", repositoryFactory.getObject());

		repositoryFactories.add(repositoryFactory);

	}



	private void removeRepositoryResources(String repositoryId) throws IOException {
		deleteResource("/"+repositoryId+"/version");
		deleteResource("/"+repositoryId+"/dataStore");
		deleteResource("/"+repositoryId+"/workspaces");
		deleteResource("/"+repositoryId+"/.lock");
		deleteResource("/"+repositoryId+"/UnmanagedDataStore");
		deleteResource("/"+repositoryId+"/exports");
	}
	
	
	
	public void shutdownRepository() throws Exception {

		for (RepositoryFactoryBean repositoryFactory : repositoryFactories)
		{
			if (repositoryFactory != null)
			{
				repositoryFactory.destroy();
			}
		}
	}


	private void deleteResource(String resourceRelativePath) throws IOException {
		Resource versionDirectory = new ClassPathResource(resourceRelativePath);
		
		if (versionDirectory.exists()){
			File file = versionDirectory.getFile();
			
			if (file.isFile()){
				file.delete();
			}
			else{
				FileUtils.deleteDirectory(file);
			}
		}
		
	}


	private void touchResource(String resourceRelativePath) throws IOException {
		Resource resource = new ClassPathResource(resourceRelativePath);
		
		if (resource.exists()){
			File file = resource.getFile();
			
			if (file.isFile()){
				FileUtils.touch(file);
			}
			else{
				File[] files = file.listFiles();
				
				if (files != null && files.length > 0){
					for (File child: files){
						FileUtils.touch(child);
					}
				}
			}
		}
		
	}


	protected String[] getConfigLocations() {


		return new String[]{"classpath:context-for-test.xml"};
	}

	public TaxonomyService getTaxonomyService() {

		return (TaxonomyService) applicationContext.getBean("taxonomyService");
	}

	public RepositoryService getRepositoryService() {
		return (RepositoryService) applicationContext.getBean("repositoryService");
	}

	public RepositoryUserService getRepositoryUserService() {
		return (RepositoryUserService) applicationContext.getBean("repositoryUserService");
	}

	public TopicService getTopicService() {
		return (TopicService) applicationContext.getBean("topicService");
	}

	public DefinitionService getDefinitionService() {
		return (DefinitionService) applicationContext.getBean("definitionService");
	}

	public ContentService getContentService() {
		return (ContentService) applicationContext.getBean("contentService");
	}

	public <T> T getBean(Class<T> type, String name) {
		if (StringUtils.isBlank(name)){
			return (T) applicationContext.getBean(StringUtils.uncapitalize(type.getSimpleName()));
		}
		else{
			return (T) applicationContext.getBean(name);
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}


	public SpaceService getSpaceService() {
		return (SpaceService) applicationContext.getBean("spaceService");
	}


	public IdentityStore getIdentityStore() {
		return	(IdentityStore) applicationContext.getBean("identityStore");
	}

	public SerializationService getSerialzationService() {
		return (SerializationService) applicationContext.getBean("serializationService");
	}

	public ImportService getImportService() {
		return (ImportService) applicationContext.getBean("importService");
	}

	public void reloadDefinitions() throws IOException{
		touchResource(TestConstants.TEST_REPOSITORY_ID+"/astroboa_schemata");
		touchResource(TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID+"/astroboa_schemata");
		touchResource(TestConstants.TEST_CLONE_REPOSITORY_ID+"/astroboa_schemata");
	}





}
