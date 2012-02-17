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
package org.betaconceptframework.astroboa.test.engine.security;

import java.security.acl.Group;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidCredentialsException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidUsernameException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryDao;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsLoginTest extends AbstractRepositoryTest{


	@Test 
	public void testAnonymousLogin(){

		repositoryService.login(TestConstants.TEST_REPOSITORY_ID, new AstroboaCredentials(IdentityPrincipal.ANONYMOUS, "".toCharArray()));
		
		repositoryService.loginAsAnonymous(TestConstants.TEST_REPOSITORY_ID);
		
		repositoryService.loginAsAnonymous(TestConstants.TEST_REPOSITORY_ID, "specialKey");
			
	}
	
	@Test 
	public void testSystemLoginWithInvalidSecretKey(){
		final String secretKey = "invalid";

		TestLogPolicy.setLevelForLogger(Level.ERROR, RepositoryDao.class.getName());

		try{
			
			repositoryService.login(TestConstants.TEST_REPOSITORY_ID, IdentityPrincipal.SYSTEM, secretKey);
			
			Assert.assertTrue(false, "User SYSTEM has successfully logged in with secret key "+secretKey);
		}
		catch(CmsLoginInvalidCredentialsException e){
			//Correct exception   
			Assert.assertTrue(StringUtils.isBlank(e.getMessage()), "Valid exception thrown for SYSTEM user login with invalid secret key but invalid excpetion message returned "+ e.getMessage());
		}
		
		try{
			repositoryService.loginAsAdministrator(TestConstants.TEST_REPOSITORY_ID, secretKey);
			
			Assert.assertTrue(false, "User SYSTEM has successfully logged in with secret key "+secretKey);
		}
		catch(CmsLoginInvalidCredentialsException e){
			//Correct exception   
			Assert.assertTrue(StringUtils.isBlank(e.getMessage()), "Valid exception thrown for SYSTEM user login with invalid secret key but invalid excpetion message returned "+ e.getMessage());
		}
		
		TestLogPolicy.setDefaultLevelForLogger(RepositoryDao.class.getName());
	}

	@Test 
	public void testSystemLoginWithValidSecretKey(){
		final String secretKey = "secretSystemKey";

		repositoryService.login(TestConstants.TEST_REPOSITORY_ID, IdentityPrincipal.SYSTEM, secretKey);
		
		repositoryService.loginAsAdministrator(TestConstants.TEST_REPOSITORY_ID, secretKey);
		
		repositoryService.loginAsAdministrator(TestConstants.TEST_REPOSITORY_ID, secretKey, "specialKey");
			
	}

	@Test
	public void testAuthenticationTokenIsSameForPermanentKey(){

		Subject subject = new Subject();
		
		String identity = IdentityPrincipal.ANONYMOUS;
		IdentityPrincipal identityPrincipal = new IdentityPrincipal(identity);
		subject.getPrincipals().add(identityPrincipal);
		
		String permanentKey = "specialKey";
		
		
		String authToken1 = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
		
		String authToken2 = repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
		
		Assert.assertEquals(authToken2, authToken1);
		

	}
	
	@Test
	public void testInvalidPermanentKey(){
		Subject subject = new Subject();
		
		String identity = "testuser";
		IdentityPrincipal identityPrincipal = new IdentityPrincipal(identity);
		subject.getPrincipals().add(identityPrincipal);
		
		String permanentKey = "invalidPermanentKey";
		
		try{
			repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
		}
		catch(Exception e){
			
			Assert.assertTrue((e instanceof CmsException), "Unexpected exception during invalid login");
			
			Assert.assertEquals(e.getMessage(), "Invalid permanent key "+permanentKey+" for user "+identity+ " in repository "+ TestConstants.TEST_REPOSITORY_ID);
		}
		
		identity = TestConstants.TEST_USER_NAME;
		subject.getPrincipals().remove(identityPrincipal);
		identityPrincipal = new IdentityPrincipal(identity);
		subject.getPrincipals().add(identityPrincipal);
		
		try{
			repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, permanentKey);
		}
		catch(Exception e){
			
			Assert.assertTrue((e instanceof CmsException), "Unexpected exception during invalid login");
			
			Assert.assertEquals(e.getMessage(), "Invalid permanent key "+permanentKey+" for user "+identity+ " in repository "+ TestConstants.TEST_REPOSITORY_ID);
		}
		
		
		
	}
	
	
	@Test 
	public void testInvalidRepositoryId(){
		
		String repositoryId = "invalidId";
		
		try{
			
			repositoryService.loginAsAnonymous(repositoryId);
			
			
		}
		catch(Exception e){
			
			Assert.assertTrue((e instanceof CmsException), "Unexpected exception during invalid login");
			
			Assert.assertEquals(e.getMessage(), "Repository with id "+ repositoryId+ " was not deployed.");
		}
		
		try{
			repositoryId = "";
			
			repositoryService.loginAsAnonymous(repositoryId);

			
		}
		catch(Exception e){
			
			Assert.assertTrue((e instanceof CmsException), "Unexpected exception during invalid login");
			
			Assert.assertEquals(e.getMessage(), "Null or empty repository id '"+ repositoryId+"'");
		}
		
	}
	
	@Test
	public void testLoginExceptionWhenNoIdentityProvided(){
		
		try{
			repositoryService.login(TestConstants.TEST_REPOSITORY_ID, null, null, null);
		}
		catch(Exception e){
			
			Assert.assertTrue((e instanceof CmsLoginInvalidUsernameException), "Unexpected exception during invalid login");
			
			Assert.assertEquals("No username provided", e.getMessage(), "Invalid exception message");
		}
		
		
	}
	
	@Test
	public void testAvailableRepositoriesReturnedWhenNoAuthorizedRepositoriesExist(){
		
		Subject subject = new Subject();
		
		String identity = "testuser";
		subject.getPrincipals().add(new IdentityPrincipal(identity));
		
		repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, null);
		
		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		
		Assert.assertNotNull(securityContext, "Found no security context in Thread for logged in user "+ identity);
		
		List<CmsRepository> availableRepositories = repositoryService.getAvailableCmsRepositories();
		
		Assert.assertTrue(CollectionUtils.isNotEmpty(availableRepositories), "No available repositories for test");
		
		List<String> authorizedRepositories = securityContext.getAuthorizedRepositories();
		
		Assert.assertTrue(CollectionUtils.isNotEmpty(authorizedRepositories), "Authorized repositories must not be empty");
		
		for (CmsRepository cmsRepository :availableRepositories){
			Assert.assertTrue(authorizedRepositories.contains(cmsRepository.getId()), "Repository id "+ cmsRepository.getId() +
					" was not found in authorized repositories "+ authorizedRepositories.toString());
		}
		
		
	}
	
	@Test
	public void testAuthorizedRepositoriesAreTheSameFoundInSubject(){
		
		Subject subject = new Subject();
		
		String identity = "testuser";
		subject.getPrincipals().add(new IdentityPrincipal(identity));
		
		Group group = new CmsGroup(AstroboaPrincipalName.AuthorizedRepositories.toString());
		group.addMember(new CmsPrincipal("testRepositoryA"));
		group.addMember(new CmsPrincipal("testRepositoryB"));
		group.addMember(new CmsPrincipal(TestConstants.TEST_REPOSITORY_ID));
		
		subject.getPrincipals().add(group);
		
		repositoryService.login(TestConstants.TEST_REPOSITORY_ID, subject, null);
		
		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		
		Assert.assertNotNull(securityContext, "Found no security context in Thread for logged in user "+ identity);
		
		List<String> authorizedRepositories = securityContext.getAuthorizedRepositories();
		
		Assert.assertTrue(CollectionUtils.isNotEmpty(authorizedRepositories), "Authorized repositories must not be empty");
		
		Assert.assertTrue(authorizedRepositories.size() == 3, "Authorized repositories must be exactly 3. "+ authorizedRepositories.toString());
		
		for (String repositoryId :authorizedRepositories){
			Assert.assertTrue(repositoryId.equals("testRepositoryA") ||
					repositoryId.equals("testRepositoryB") ||
					repositoryId.equals(TestConstants.TEST_REPOSITORY_ID) , "Repository id "+ repositoryId +
					" must not exist in authorized repositories "+ authorizedRepositories.toString());
		}
		
		
	}

	
	@AfterTest
	public void validLoginToTestRepository(){
		super.loginToTestRepositoryAsSystem();
	}
}
