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
package org.betaconceptframework.astroboa.test.engine.service;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryServiceTest extends AbstractRepositoryTest{
	
	@Test 
	public void testRepositoryConfigurationReloading() throws IOException{
	
		File configuration = retrieveConfigurationFile();
		
		FileUtils.touch(configuration);
		
		Assert.assertTrue(RepositoryRegistry.INSTANCE.configurationHasChanged(), "Configuration file "+ configuration.getAbsolutePath() + " was not reloaded. Parent directory "+ System.getProperty("jboss.server.config.url"));
		
		String configurationContent = FileUtils.readFileToString(configuration);
		
		String currentServerURL = "serverURL=\"http://localhost:8080\"";
		String newServerURL = "serverURL=\"http://newServer:8080\"";
		
		configurationContent = configurationContent.replaceFirst(currentServerURL, newServerURL);
		
		FileUtils.writeStringToFile(configuration, configurationContent);
		
		Assert.assertTrue(RepositoryRegistry.INSTANCE.configurationHasChanged());
		
		//force registry to reload changes
		repositoryService.getAvailableCmsRepositories();
		
		Assert.assertEquals(RepositoryRegistry.INSTANCE.getDefaultServerURL(), "http://newServer:8080", "Repository Registry has not been updated");

		configurationContent = configurationContent.replaceFirst(newServerURL, currentServerURL );
		
		FileUtils.writeStringToFile(configuration, configurationContent);
		
	}

	private File retrieveConfigurationFile() {
		String configurationHomeDir = System.getProperty("jboss.server.config.url");
		
		if (configurationHomeDir.startsWith("file:")){
			configurationHomeDir = StringUtils.removeStart(configurationHomeDir, "file:");
		}
		
		File configuration = new File(configurationHomeDir+File.separator+"astroboa-conf.xml");
		return configuration;
	}
	
	@Test
	public void testCreateNewRepository() throws NamingException, Exception{
		
		final String repositoryId = "newRepositoryAddedAtRuntime";
		File newRepositoryConfigurationFile = new ClassPathResource("new-astroboa-conf.xml").getFile();
		
		File configuration = retrieveConfigurationFile();
		
		//Copy File
		System.out.println("BEFORE COPY New configuration last Modified  "+newRepositoryConfigurationFile.lastModified()+ " Old Configuration last modified "+configuration.lastModified());
		FileUtils.copyFile(newRepositoryConfigurationFile, configuration, false);
		//update last modified date to force Registry to reload configuration
		
		Calendar lastModified = Calendar.getInstance();
		lastModified.add(Calendar.MILLISECOND, 400);
		configuration.setLastModified(lastModified.getTimeInMillis());
		
		System.out.println("AFTER COPY New configuration last Modified  "+newRepositoryConfigurationFile.lastModified()+ " Old Configuration last modified "+configuration.lastModified());
		
		//Configure files in test context
		AstroboaTestContext.INSTANCE.configureRepository(repositoryId, new InitialContext());
		
		//Login to the repository. We expect to load the new repository at runtime
		loginToRepository(repositoryId, "SYSTEM", "betaconcept", false);
		
		CmsRepository newRepository = repositoryService.getCmsRepository(repositoryId);
		
		Assert.assertNotNull(newRepository);
		
		Assert.assertEquals(newRepository.getId(), repositoryId);
	
		loginToTestRepositoryAsSystem();
		
		checkExpression("contentTypeName=\""+TEST_CONTENT_TYPE+"Type\"", 
				CriterionFactory.equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), Condition.OR, Arrays.asList(EXTENDED_TEST_CONTENT_TYPE,DIRECT_EXTENDED_TEST_CONTENT_TYPE,TEST_CONTENT_TYPE)));
		
		checkExpression("contentTypeName=\""+EXTENDED_TEST_CONTENT_TYPE+"Type\"", 
				CriterionFactory.equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), Condition.OR, Arrays.asList(EXTENDED_TEST_CONTENT_TYPE,DIRECT_EXTENDED_TEST_CONTENT_TYPE)));
		 
		
	}
	
	@Test
	public void testCmsRepositoryConfigurationSettings(){
		
		loginToTestRepositoryAsSystem();
		
		AstroboaClientContext  clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		Assert.assertNotNull(clientContext, "No active AstroboaClientContext found");
		
		Assert.assertNotNull(clientContext.getRepositoryContext(), "No active Repository Context found");
		
		Assert.assertNotNull(clientContext.getRepositoryContext().getSecurityContext(), "No active Security Context found");
		
		Assert.assertNotNull(clientContext.getAuthenticationToken(), "No authentication token found");
		
		Assert.assertNotNull(clientContext.getRepositoryContext().getSecurityContext().getAuthenticationToken(), "No active authentication token time out found");
		
		Assert.assertNotNull(clientContext.getRepositoryContext().getCmsRepository(), "No  active cms repository found");
		
		Assert.assertNotNull(clientContext.getRepositoryContext().getCmsRepository().getApplicationPolicyName(), "No  active jaas application policy name found");
		
		Assert.assertEquals(clientContext.getRepositoryContext().getCmsRepository().getAdministratorUserId(), IdentityPrincipal.SYSTEM);
		
	}

	@Test
	public void testLoginPermanentKey(){
		
		String permanentKey = "fakeKey";
			
		//Connect to test repository with fake key
		try{
			authenticationToken = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, new AstroboaCredentials(TestConstants.TEST_USER_NAME, 
					"betaconcept".toCharArray()), permanentKey);
			
			Assert.assertEquals(1,2,"Login succeded with false key");
		}
		catch(Exception e){ 
			Assert.assertEquals(e.getMessage(), "Invalid permanent key "+ permanentKey +" for user "+TestConstants.TEST_USER_NAME+" in repository "+TestConstants.TEST_REPOSITORY_ID,
					"Login did not take place with false key nevertheless exception is thrown "+ e.getMessage());
		}
		
		//Connect with correct key
		permanentKey = "keyForTest";
		try{
			authenticationToken = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, new AstroboaCredentials(
					TestConstants.TEST_USER_NAME, "betaconcept".toCharArray()), permanentKey);
			
			//Connect again and check that authentication token is the same
			String authenticationToken2 = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, 
					new AstroboaCredentials(TestConstants.TEST_USER_NAME, "betaconcept".toCharArray()), permanentKey);
			
			Assert.assertEquals(authenticationToken, authenticationToken2, "Login with trusted keys produced two different authentication tokens");
			
		}
		catch(Exception e){
			throw new CmsException(e);
		}
		
		//Connect with anonymous using subject
		permanentKey = "specialKey";
		try{
			
			Subject subject = new Subject();
			subject.getPrincipals().add(new IdentityPrincipal("anonymous"));
			
			authenticationToken = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
			
			//Connect again and check that authentication token is the same
			String authenticationToken2 = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
			
			Assert.assertEquals(authenticationToken, authenticationToken2, "Login with trusted keys produced two different authentication tokens");
			
		}
		catch(Exception e){
			throw new CmsException(e);
		}
		
		//Connect with * using subject
		permanentKey = "globalKey";
		try{
			
			Subject subject = new Subject();
			subject.getPrincipals().add(new IdentityPrincipal("anyUser"));
			
			authenticationToken = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
			
			//Connect again and check that authentication token is the same
			String authenticationToken2 = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
			
			Assert.assertEquals(authenticationToken, authenticationToken2, "Login with trusted keys produced two different authentication tokens");
			
		}
		catch(Exception e){
			throw new CmsException(e);
		}

		
		//Back to normal
		loginToTestRepositoryAsTestUser();
		cmsRepositoryEntityFactory = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory();
		
	}

	private void checkExpression(String expression, Criterion expectedCriterion) throws Exception {

		try{
			ContentObjectCriteria parserContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			
			ContentObjectCriteria expectedContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			expectedContentOjectCriteria.addCriterion(expectedCriterion);
			
			
			CriterionFactory.parse(expression, parserContentOjectCriteria);
			
			assertCriterionEquals(parserContentOjectCriteria, expectedContentOjectCriteria);

			logger.info("Expression : "+ expression + " produced XPATH : "+ parserContentOjectCriteria.getXPathQuery());
		}
		catch (RuntimeException e){
			logger.error(expression);
			throw e;
		}
	}
	
	private void assertCriterionEquals(ContentObjectCriteria parserContentOjectCriteria, ContentObjectCriteria expectedContentOjectCriteria){

		Assert.assertNotNull(parserContentOjectCriteria, "No criteria provided by parser");
		Assert.assertNotNull(expectedContentOjectCriteria, "No criteria provided by user");

		Assert.assertEquals(StringUtils.deleteWhitespace(parserContentOjectCriteria.getXPathQuery()), 
				StringUtils.deleteWhitespace(expectedContentOjectCriteria.getXPathQuery()));
	}
}
