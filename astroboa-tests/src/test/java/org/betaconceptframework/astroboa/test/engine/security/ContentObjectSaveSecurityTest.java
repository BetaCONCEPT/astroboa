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
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.service.security.aspect.SecureContentObjectSaveAspect;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSaveSecurityTest extends AbstractRepositoryTest{

	private ArrayList<ContentObjectMethodDeclaration> contentServiceMethodDeclarations;

	@Override
	protected void postSetup() throws Exception {
		
		super.postSetup();

		generateSaveContentObjectMethodDeclarations();
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
			
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).removeValues();
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			//User should be able to save content object as she belongs to the same GROUP
			//although this group has been saved in accessibility with the prefix GRP_
			contentObject = executeMethodOnContentService(methodName, contentObject, true,
					methodArgumentsApartFromContentObject, parameterTypes);

		}
	}
	
	@Test
	public void testSaveBlankContentObjectProvided() throws Exception
	{

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			try{
				executeMethodOnContentService(methodName, null, false,
						methodArgumentsApartFromContentObject, parameterTypes);
			}
			catch(CmsException e)
			{
				Assert.assertEquals(e.getMessage(), "Cannot save null content object for user "+AstroboaClientContextHolder.getActiveSecurityContext().getIdentity(), getContentObjectMethod.getName()+ " - Invalid exception thrown "+ e.getMessage());
			} catch (Throwable e) {
				throw new CmsException(e);
			}

		}
	}

	@Test
	public void testSaveContentObjectAsSystem() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsSystem"+
						contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);

				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}

		}
	}

	@Test
	public void testSaveContentObjectAsNonSystemButWithRoleAdmin() throws Exception{

		RepositoryUser systemUser = getSystemUser();

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsNonSystemButWithRoleAdmin"+
						contentServiceMethodDeclarations.indexOf(getContentObjectMethod),true);

				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				Assert.assertNotNull(contentObject.getId(),"User "+TestConstants.TEST_USER_NAME+" could not saved content object although it had "+CmsRole.ROLE_ADMIN);

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
	public void testSaveContentObjectAsNonSystemButWithNoRoleAdmin() throws Exception
	{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();
			
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsNonSystemButWithNoRoleAdmin"
						+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);

				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertNull(contentObject.getId(),"User "+TestConstants.TEST_USER_NAME+" saved content object although it had not role "+
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertEquals(e.getMessage(), "User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object as she has not been assigned role "+
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e,
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object as she has not been assigned role "+
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR), false, methodName);

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
	public void testSaveContentObjectWithProvidedIdAsNonSystemButWithNoRoleAdmin() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithProvidedIdAsNonSystemButWithNoRoleAdmin"
						+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
				contentObject.setId(TestUtils.generateRandomUUID());

				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" saved content object with provided id although it had not "+CmsRole.ROLE_CMS_EDITOR);

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertEquals(e.getMessage(), 
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object as she has not been assigned role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR),
						"Invalid exception thrown "+ e.getMessage());
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e,
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object as she has not been assigned role "+
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR), false, methodName);

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
	public void testSaveContentObjectAsNonSystemButWithRoleCmsEditorButNotTheOwner() throws Exception{
		
		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"AsNonSystemButWithRoleCmsEditorButNotTheOwner"
						+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
				
				//First save content object. The first time content object will be saved
				//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
				//the owner of the content object (do we want that to happen?)
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" saved content object with provided id although she was not the owner");

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e,
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object", true, methodName);

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
	public void testSaveContentObjectWithAccessibilityCanBeUpdatedByALLAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByALLAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
			
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(ContentAccessMode.ALL.toString());

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			contentObject = executeMethodOnContentService(methodName, contentObject, false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}
	
	@Test
	public void testSaveContentObjectWithAccessibilityCanBeUpdatedByUserOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));

			

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByUserOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
			
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(TestConstants.TEST_USER_NAME);

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			contentObject = executeMethodOnContentService(methodName, contentObject, false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}
	
	@Test
	public void testSaveContentObjectWithAccessibilityCanBeUpdatedByGroupOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Throwable{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			String group = "group@"+AstroboaClientContextHolder.getActiveRepositoryId();
			addRoleToActiveSubject(group);

			

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByGroupOnlyAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
			
			((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(group);

			//First save content object. The first time content object will be saved
			//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
			//the owner of the content object (do we want that to happen?)
			contentObject = contentService.save(contentObject, false, true, null);

			addEntityToBeDeletedAfterTestIsFinished(contentObject);

			//but the second time, especially since accessibility are the defaults
			//testUser should not be able to re-save it
			contentObject = executeMethodOnContentService(methodName, contentObject, false,
					methodArgumentsApartFromContentObject, parameterTypes);

			loginToTestRepositoryAsSystem();

		}
	}

	@Test
	public void testSaveContentObjectWithAccessibilityCanBeUpdatedByDifferentGroupAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByDifferentGroupAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
						+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
				
				String group = "group@"+AstroboaClientContextHolder.getActiveRepositoryId();
				((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(group);
				
				//First save content object. The first time content object will be saved
				//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
				//the owner of the content object (do we want that to happen?)
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" saved content object ("+methodName+") with provided id although she was not the owner and had no valid roles");

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e,
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object", true, methodName);
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
	public void testSaveContentObjectWithAccessibilityCanBeUpdatedByDifferentUserAsNonSystemButWithRoleCmsEditorAndNotTheOwner() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();
			Object[] methodArgumentsApartFromContentObject = getContentObjectMethod.getParameterValues();
			Class<?>[] parameterTypes = getContentObjectMethod.getParameterTypes();

			loginAsTestUser();

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
			
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

			try{
				

				ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"WithAccessibilityCanBeUpdatedByDifferentUserAsNonSystemButWithRoleCmsEditorAndNotTheOwner"
						+contentServiceMethodDeclarations.indexOf(getContentObjectMethod), true);
				
				String user = "otherUser";
				((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).addSimpleTypeValue(user);
				
				//First save content object. The first time content object will be saved
				//as test user has role ROLE_CMS_EDITOR although she has provided SYSTEM as
				//the owner of the content object (do we want that to happen?)
				contentObject = contentService.save(contentObject, false, true, null);

				addEntityToBeDeletedAfterTestIsFinished(contentObject);

				//but the second time, especially since accessibility are the defaults
				//testUser should not be able to re-save it
				contentObject = executeMethodOnContentService(methodName, contentObject, false,
						methodArgumentsApartFromContentObject, parameterTypes);

				Assert.assertTrue(false,"User "+TestConstants.TEST_USER_NAME+" saved content object with provided id although she was not the owner and had no valid roles");

			}
			catch(CmsUnauthorizedAccessException e){
				Assert.assertTrue(e.getMessage()!= null && 
						e.getMessage().startsWith("User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object"), 
						"Invalid exception thrown "+ e.getMessage()
				);
			}
			catch(CmsException e){
				assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(e,
						"User "+TestConstants.TEST_USER_NAME+" is not authorized to save content object", true, methodName);
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
			finally{
				loginToTestRepositoryAsSystem();
			}

		}
	}


	private ContentObject executeMethodOnContentService(String methodName,
			ContentObject contentObject, boolean logException,
			Object[] methodArgumentsApartFromContentObjectId,
			Class<?>... parameterTypes 
	) throws Throwable {

		List<Object> objectParameters = new ArrayList<Object>();

		objectParameters.add(contentObject);

		if (! ArrayUtils.isEmpty(methodArgumentsApartFromContentObjectId)){
			objectParameters.addAll(Arrays.asList(methodArgumentsApartFromContentObjectId));
		}

		Method method = contentService.getClass().getMethod(methodName, parameterTypes);


		try{
			if (! logException){
				TestLogPolicy.setLevelForLogger(Level.ERROR, SecureContentObjectSaveAspect.class.getName());
			}
			return (ContentObject) method.invoke(contentService, objectParameters.toArray());
		}
		catch(Exception t){
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




	private List<ContentObjectMethodDeclaration> generateSaveContentObjectMethodDeclarations(){

		if (contentServiceMethodDeclarations == null){
			contentServiceMethodDeclarations = new ArrayList<ContentObjectMethodDeclaration>();


			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("save", new Object[]{true, true, null}, Object.class, boolean.class, boolean.class, String.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("save", new Object[]{true, false, null}, Object.class, boolean.class, boolean.class, String.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("save", new Object[]{false, false, null}, Object.class, boolean.class, boolean.class, String.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("save", new Object[]{false, true, null}, Object.class, boolean.class, boolean.class, String.class));
		}

		return contentServiceMethodDeclarations;

	}

	private void loginAsTestUser(){
		super.loginToTestRepositoryAsTestUser();
	}


}
