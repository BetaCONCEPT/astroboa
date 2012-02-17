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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.service.security.aspect.SecureContentObjectDeleteAspect;
import org.betaconceptframework.astroboa.engine.service.security.aspect.SecureContentObjectSaveAspect;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectDeleteSecurityTest extends AbstractRepositoryTest{

	private ArrayList<ContentObjectMethodDeclaration> contentServiceMethodDeclarations;

	@Override
	protected void postSetup() throws Exception {
		
		super.postSetup();

		generateDeleteContentObjectMethodDeclarations();
	}

	@Test
	public void testSaveContentObjectByUserWhichHasTheSameGroupWithTheOwner() throws Throwable
	{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginToTestRepositoryAsSystem();

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByALLAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
			
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).removeValues();
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			//User should be able to save content object as she belongs to the same GROUP
			 Assert.assertTrue((Boolean) executeMethodOnContentService(methodName, contentObject.getId(), true, 	methodArgumentsApartFromContentObject, parameterTypes), "Object "+contentObject+ " has not been deleted");

		}
	}
	@Test
	public void testDeleteBlankContentObjectProvided() throws Exception
	{

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			try{
				executeMethodOnContentService(methodName, null, false,
						methodArgumentsApartFromContentObject, parameterTypes);
			}
			catch(CmsException e)
			{
				Assert.assertEquals(e.getMessage(), "No object id or name provided. User "+AstroboaClientContextHolder.getActiveSecurityContext().getIdentity(), "Invalid exception thrown "+ e.getMessage());
			} catch (Throwable e) {
				throw new CmsException(e);
			}

		}
	}

	@Test
	public void testDeleteContentObjectAsSystem() throws Exception
	{

		loginToTestRepositoryAsSystem();
		
		RepositoryUser systemUser = getSystemUser();

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			try{

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsSystem", true);
				contentObject = contentService.save(contentObject, false, true, null);
				
				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				executeMethodOnContentService(methodName, contentObject.getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);

			}
			catch (Throwable e) {
				throw new CmsException(e);
			}

		}
	}

	@Test
	public void testDeleteContentObjectAsNonSystemButWithRoleAdmin() throws Exception
	{

		RepositoryUser systemUser = getSystemUser();
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsNonSystemButWithRoleAdmin", true);
				contentObject = contentService.save(contentObject, false, true, null);

				loginAsTestUser();
				
				addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

				executeMethodOnContentService(methodName, contentObject.getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);


			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally
			{
				loginToTestRepositoryAsSystem();
			}

		}
	}

	

	

	@Test
	public void testDeleteContentObjectAsNonSystemButWithRoleCmsEditorButNotTheOwner() throws Exception
	{

		RepositoryUser systemUser = getSystemUser();
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();


			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsNonSystemButWithRoleCmsEditorButNotTheOwner", true);
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				loginAsTestUser();
				
				addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
				removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));


				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				executeMethodOnContentService(methodName, contentObject.getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" deleted content object with provided id although she was not the owner");

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e, 
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object", true, methodName);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally{
				loginToTestRepositoryAsSystem();
			}

		}
	}

	@Test
	public void testDeleteContentObjectWithAccessibilityCanBeDeletedByALLAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable{
		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));


			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeDeletedByALLAsNonSystemButWithRoleCmsEditorAndNotTheOwner", true);
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(ContentAccessMode.ALL.toString());

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			executeMethodOnContentService(methodName, contentObject.getId(), false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}
	
	@Test
	public void testDeleteContentObjectWithAccessibilityCanBeDeletedByUserOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable
	{
		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeDeletedByUserOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner", true);
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(TestConstants.TEST_USER_NAME);

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			executeMethodOnContentService(methodName, contentObject.getId(), false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}
	
	@Test
	public void testDeleteContentObjectWithAccessibilityCanBeDeletedByGroupOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable
	{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			String group = "group@"+AstroboaClientContextHolder.getActiveRepositoryId();
			addRoleToActiveSubject(group);

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeDeletedByGroupOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner", true);
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(group);

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			executeMethodOnContentService(methodName, contentObject.getId(), false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}

	@Test
	public void testDeleteContentObjectWithAccessibilityCanBeDeletedByDifferentGroupAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Exception
	{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations)
		{
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+methodName, true);
				String group = "group@"+AstroboaClientContextHolder.getActiveRepositoryId();
				((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(group);
				
				//First save content object. The first time content object will be saved
				//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
				//the owner of the content object (do we want that to happen?)
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				executeMethodOnContentService(methodName, contentObject.getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" deleted content object although she was not the owner and had no valid roles");

			}
			catch(CmsUnauthorizedAccessException e)
			{
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e, 
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object", true, methodName);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally{
				loginToTestRepositoryAsSystem();
			}

		}
	}
	
	@Test
	public void testDeleteContentObjectWithAccessibilityCanBeDeletedByDifferentUserAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeDeletedByDifferentUserAsNonSystemButWithRoleCmsEditorAndNotTheOwner", true);
				String user = "otherUser";
				((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).addSimpleTypeValue(user);
				
				//First save content object. The first time content object will be saved
				//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
				//the owner of the content object (do we want that to happen?)
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				executeMethodOnContentService(methodName, contentObject.getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" saved content object although she was not the owner and had no valid roles");

			}
			catch(CmsUnauthorizedAccessException e)
			{
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e, 
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to delete content object", true, methodName);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally{
				loginToTestRepositoryAsSystem();
			}

		}
	}

	@Test
	public void testDeletePersonObjectRepresentingSystem() throws Exception{

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();


			try{
				
				loginToRepositoryRepresentingIdentityStoreAsSystem();
				
				ContentObjectCriteria personCriteria = CmsCriteriaFactory.newContentObjectCriteria("personObject");
				personCriteria.addCriterion(CriterionFactory.equals("personAuthentication.username",IdentityPrincipal.SYSTEM));
				personCriteria.setOffsetAndLimit(0, 1);
				personCriteria.doNotCacheResults();
				personCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

				CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(personCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
				
				TestLogPolicy.setLevelForLogger(Level.FATAL, SecureContentObjectDeleteAspect.class.getName());
				executeMethodOnContentService(methodName, outcome.getResults().get(0).getId(), false,
						methodArgumentsApartFromContentObject, parameterTypes);
				TestLogPolicy.setDefaultLevelForLogger(SecureContentObjectDeleteAspect.class.getName());
				
				Assert.assertTrue(false,"User SYSTEM deleted corresponding person object");
				
				loginToTestRepositoryAsSystem();

			}
			catch(CmsUnauthorizedAccessException e)
			{
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User SYSTEM is not authorized to delete person content object " +
								" which represent her self"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e, 
						"User SYSTEM is not authorized to delete person content object " +
						" which represent her self", true, methodName);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally{
				loginToTestRepositoryAsSystem();
			}

		}
	}

	
	private Object executeMethodOnContentService(String methodName,
			String contentObjectId, boolean logException,
			Object[] methodArgumentsApartFromContentObjectId,
			Class<?>... parameterTypes 
	) throws Throwable {

		List<Object> objectParameters = new ArrayList<Object>();

		objectParameters.add(contentObjectId);

		if (! ArrayUtils.isEmpty(methodArgumentsApartFromContentObjectId)){
			objectParameters.addAll(Arrays.asList(methodArgumentsApartFromContentObjectId));
		}

		Method method = contentService.getClass().getMethod(methodName, parameterTypes);


		try{
			if (! logException){
				TestLogPolicy.setLevelForLogger(Level.FATAL, SecureContentObjectDeleteAspect.class.getName());
			}
			
			return method.invoke(contentService, objectParameters.toArray());
			
		}
		catch(Exception t)
		{
			if (logException){
				throw new CmsException(methodName + " "+ parameterTypes+ objectParameters.toArray().toString(),t);
			}
			else{
				if (t instanceof InvocationTargetException){
					throw t.getCause();
				}
				else{
					throw t;
				}
			}
		}
		finally{
			if (! logException){
				TestLogPolicy.setDefaultLevelForLogger(SecureContentObjectSaveAspect.class.getName());
			}
		}

	}




	private List<ContentObjectMethodDeclaration> generateDeleteContentObjectMethodDeclarations()
	{

		if (contentServiceMethodDeclarations == null)
		{
			contentServiceMethodDeclarations = new ArrayList<ContentObjectMethodDeclaration>();

			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("deleteContentObject", new Object[]{}, String.class));

		}

		return contentServiceMethodDeclarations;

	}

	private void loginAsTestUser()
	{
		super.loginToTestRepositoryAsTestUser();
	}


}
