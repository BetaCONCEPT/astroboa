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
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.service.security.exception.NonAuthenticatedOperationException;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsConstants.ContentObjectStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSecurityTest extends AbstractRepositoryTest{

	private ArrayList<ContentObjectMethodDeclaration> contentServiceMethodDeclarations;

	
	@Override
	protected void postSetup() throws Exception {

		super.postSetup();
		
		generateGetContentObjectMethodDeclarations();
	}
	
	@Test
	public void testGetContentObjectBlankIdProvided() throws Exception{
		
		RepositoryUser systemUser = getSystemUser();
		
		loginToTestRepositoryAsSystem();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){

			String methodName = getContentObjectMethod.getName();

			//Create content object for test

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)+"GetContentObjectBlankIdProvided"+
					contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"GetContentObjectBlankIdProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod))));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, null);

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + " for null identifier provided in method parameter");

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, "");

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + " for empty identifier provided in method parameter");

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, " ");

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + " for empty identifier provided in method parameter");


		}
	}
	@Test
	public void testGetContentObjectBlankUserIdInSecurityContextProvided() throws Exception{

		RepositoryUser systemUser = getSystemUser();
		
		loginToTestRepositoryAsSystem();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){

			AstroboaClientContext 	activeContext = AstroboaClientContextHolder.getActiveClientContext();

			IdentityPrincipal systemIdentityPrincipal = 
				activeContext.getRepositoryContext().getSecurityContext().getSubject().getPrincipals(IdentityPrincipal.class).iterator().next();

			String methodName = getContentObjectMethod.getName();

			//Create content object for test

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"BlankUserIdInSecurityContextProvided"+
					contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"BlankUserIdInSecurityContextProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			activeContext.getRepositoryContext().getSecurityContext().getSubject().getPrincipals().remove(systemIdentityPrincipal);

			try{
				executeMethodOnContentService(getContentObjectMethod, " ");
			}
			catch(Exception e){
				Assert.assertTrue(e instanceof NonAuthenticatedOperationException ||
						e.getCause() instanceof NonAuthenticatedOperationException, "Method "+methodName +" did not throw NonAuthenticatedOperationException" + e.getMessage()); 
			}


			activeContext.getRepositoryContext().getSecurityContext().getSubject().getPrincipals().add(systemIdentityPrincipal);

		}
	}

	@Test
	public void testGetContentObjectInvalidIdProvided() throws Exception{

		loginToTestRepositoryAsSystem();

		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			String methodName = getContentObjectMethod.getName();

			//Create content object for test
			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"InvalidIdProvided"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"InvalidIdProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, "some-fake-id");

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + " for invalid identifier provided in method parameter");

		}
		
	}

	@Test
	public void testGetContentObjectFromSYSTEMUserWithNoRoleCmsInternalViewerProvided() throws Exception{

		loginToTestRepositoryAsSystem();
		
		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){
			
			String methodName = getContentObjectMethod.getName();

			//Create content object for test
			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromSYSTEMUserWithNoRoleCmsInternalViewerProvided"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromSYSTEMUserWithNoRoleCmsInternalViewerProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER));

			//		1. Content Object has no status
			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + ". User is SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has no status");

			//		2. Content Object has published status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.published.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has published status");

			//		3. Content Object has publishedAndArchived status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.publishedAndArchived.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has publishedAndArchived status");

			//		4. Content Object has arbitrary status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.authored.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + ". User is SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has authored status");

			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER));

		}
	}

	@Test
	public void testGetContentObjectFromSYSTEMUserWithRoleCmsInternalViewerProvided() throws Exception{

		loginToTestRepositoryAsSystem();
		
		RepositoryUser systemUser = getSystemUser();
		
		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){

			String methodName = getContentObjectMethod.getName();

			//Create content object for test

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromSYSTEMUserWithRoleCmsInternalViewerProvided"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromSYSTEMUserWithRoleCmsInternalViewerProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			//		1. Content Object has no status
			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and  ROLE_CMS_INTERNAL_VIEWER is provided and content object has no status");

			//		2. Content Object has published status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.published.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and  ROLE_CMS_INTERNAL_VIEWER is provided and content object has published status");

			//		3. Content Object has publishedAndArchived status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.publishedAndArchived.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and  ROLE_CMS_INTERNAL_VIEWER is provided and content object has publishedAndArchived status");

			//		4. Content Object has arbitrary status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.authored.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User is SYSTEM and  ROLE_CMS_INTERNAL_VIEWER is provided and content object has authored status");

		}
	}

	@Test
	public void testGetContentObjectFromNonSYSTEMUserWithNoRoleCmsInternalViewerProvided() throws Exception{

		String identity = TestConstants.TEST_USER_NAME;

		RepositoryUser systemUser = getSystemUser();

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){

			loginToTestRepositoryAsSystem();
			
			String methodName = getContentObjectMethod.getName();

			//Create content object for test

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromNonSYSTEMUserWithNoRoleCmsInternalViewerProvided"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromNonSYSTEMUserWithNoRoleCmsInternalViewerProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			loginToTestRepositoryAsTestUser();
			
			//		a. User does not have ROLE_CMS_INTERNAL_VIEWER (the same apply even when user is not SYSTEM)
			removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER));

			//		1. Content Object has no status
			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance "+refreshedContentObject + ". User "+identity+" , is non SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has no status");

			//		2. Content Object has published status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.published.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User "+identity+" , is non SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has published status");

			//		3. Content Object has publishedAndArchived status
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.publishedAndArchived.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object instance. User "+identity+" , is non SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has publishedAndArchived status");

			//		4. Content Object has arbitrary status
			loginToTestRepositoryAsSystem();
			contentObject = addStatusToContentObjectAndSave(ContentObjectStatus.authored.toString(), contentObject);

			loginToTestRepositoryAsTestUser();
			
			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object instance . User "+identity+" , is non SYSTEM and no ROLE_CMS_INTERNAL_VIEWER is provided and content object has authored status");

			
		}

	}

	@Test
	public void testGetContentObjectFromNonSYSTEMUserWithRoleCmsInternalViewerProvided() throws Exception{
		String identity = TestConstants.TEST_USER_NAME;

		RepositoryUser systemUser = getSystemUser();

		for (ContentObjectMethodDeclaration getContentObjectMethod : contentServiceMethodDeclarations){

			loginToTestRepositoryAsSystem();
			
			String methodName = getContentObjectMethod.getName();

			//Create content object for test

			ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromNonSYSTEMUserWithRoleCmsInternalViewerProvided"
					+contentServiceMethodDeclarations.indexOf(getContentObjectMethod));

			contentObject = contentService.save(contentObject, false, true, null);

			//Create one version
			contentObject.setSystemName(TestUtils.createValidSystemName(TEST_CONTENT_TYPE+random.nextInt()+methodName+"FromNonSYSTEMUserWithRoleCmsInternalViewerProvided"+contentServiceMethodDeclarations.indexOf(getContentObjectMethod)));
			contentObject = contentService.save(contentObject, true, true, null);

			markObjectForRemoval(contentObject);

			loginToTestRepositoryAsTestUser();
			
			//		a. User has ROLE_CMS_INTERNAL_VIEWER . 
			addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER));
			
			//			i. User is not the owner of the object but accessibility.canBeReadBy property contains REPOSITORY value
			ContentObject refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object. User "+identity+" , is non SYSTEM " +
			"and ROLE_CMS_INTERNAL_VIEWER is provided and accessibility.canBeReadBy contains REPOSITORY value");

			//			iii. User is not the owner of the object but accessibility.canBeReadBy property contains NONE value
			addValueToAccessibilityCanBeReadBy(ContentAccessMode.NONE.toString(), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object "+ refreshedContentObject+". User "+identity+" , is non SYSTEM " +
			"and ROLE_CMS_INTERNAL_VIEWER is provided and accessibility.canBeReadBy contains NONE value");


			//			iv. User is not the owner of the object but accessibility.canBeReadBy property contains one or more granted roles for user
			addValueToAccessibilityCanBeReadBy(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object. User "+identity+" , is non SYSTEM " +
					"and ROLE_CMS_INTERNAL_VIEWER is provided and accessibility.canBeReadBy contains  "+
					CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER)+ " role");

			//			v. User is not the owner of the object and  accessibility.canBeReadBy property contains no granted roles for user
			addValueToAccessibilityCanBeReadBy(
					CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER), contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNull(refreshedContentObject, "Method "+methodName +" returned content object "+ refreshedContentObject+". User "+identity+" , is non SYSTEM " +
					"and ROLE_CMS_INTERNAL_VIEWER is provided and accessibility.canBeReadBy contains  "+
					CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER)+ " role");

			//			vi. User is not the owner of the object but accessibility.canBeReadBy property contains userId
			addValueToAccessibilityCanBeReadBy(identity, contentObject);

			refreshedContentObject = executeMethodOnContentService(getContentObjectMethod, contentObject.getId());

			Assert.assertNotNull(refreshedContentObject, "Method "+methodName +" did not return content object. User "+identity+" , is non SYSTEM " +
					"and ROLE_CMS_INTERNAL_VIEWER is provided and accessibility.canBeReadBy contains  "+identity+ " value");


			//			vii. User is the owner of the object

			
		}


	}

	private ContentObject addStatusToContentObjectAndSave(String status,
			ContentObject contentObject) {
		
		StringProperty profileContentObjectStatusProperty = (StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus");

		profileContentObjectStatusProperty.setSimpleTypeValue(status);

		return contentService.save(contentObject, false, true, null);

	}

	private ContentObject addValueToAccessibilityCanBeReadBy(String value,
			ContentObject contentObject) {
		//Needed to change accessibility values and save content object because testUser does not own content object
		addRoleToActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

		StringProperty accessibilityCanBeReadyByProperty = (StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy");

		accessibilityCanBeReadyByProperty.removeValues();

		accessibilityCanBeReadyByProperty.addSimpleTypeValue(value);

		contentObject =  contentService.save(contentObject, false, true, null);
		
		removeRoleFromActiveSubject(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));

		return contentObject;

	}


	private ContentObject executeMethodOnContentService(ContentObjectMethodDeclaration contentObjectMethod,
			String contentObjectIdentifier) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		final String methodName = contentObjectMethod.getName();
		final Class<?>[] parameterTypes = contentObjectMethod.getParameterTypes();

		List<Object> objectParameters = new ArrayList<Object>();

		objectParameters.add(contentObjectIdentifier);

		if (! ArrayUtils.isEmpty(contentObjectMethod.getParameterValues())){
			objectParameters.addAll(Arrays.asList(contentObjectMethod.getParameterValues()));
		}

		Method method = contentService.getClass().getMethod(methodName, parameterTypes);

		try{
			Object result = method.invoke(contentService, objectParameters.toArray());
			
			if (result != null) {
				if (result instanceof String){
					//Method may return string. 
					//Create ContentObject from import
					ImportConfiguration configuration = ImportConfiguration.object()
							.persist(PersistMode.DO_NOT_PERSIST)
							.build();

					return importDao.importContentObject((String)result, configuration);
				}
				else if (result instanceof CmsOutcome){
					final long count = ((CmsOutcome)result).getCount();
					if (count == 1){
						return ((CmsOutcome<ContentObject>)result).getResults().get(0);
					}
					else if (count == 0){
						return null;
					}
					else{
						throw new CmsException("Returned more than one content objects");
					}
					
				}
			}
			
			return (ContentObject)result;
		}
		catch(Exception t){
			throw new CmsException(methodName + " "+ parameterTypes+ objectParameters.toArray().toString(),t);
		}
	}


	@Test
	public void testContentObjectAccessForAnonymousUser(){

		loginToTestRepositoryAsSystem();

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "secureContentObject");

		//Provide empty value for string
		((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.submitted.toString());

		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		//Login as anonymous
		loginToTestRepositoryAsAnonymous();

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		contentObjectCriteria.addIdEqualsCriterion(contentObject.getId());
		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertEquals(outcome.getCount(),0, "Found "+outcome.getCount()+" content objects matching criteria where none should have matched");
		
		String resultsExportedAsXml = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
		
		Assert.assertTrue(StringUtils.isNotBlank(resultsExportedAsXml) && 
				StringUtils.contains(resultsExportedAsXml, CmsConstants.TOTAL_RESOURCE_COUNT+"=\"0\""), "Found "+resultsExportedAsXml+" content objects matching criteria where none should have matched");
		
		String resultsExportedAsJson = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
		
		final String expected = "{\""+CmsConstants.TOTAL_RESOURCE_COUNT+"\":\"0\",\""+CmsConstants.OFFSET+"\":\"0\"}";
		Assert.assertTrue(StringUtils.isNotBlank(resultsExportedAsJson) && 
				StringUtils.contains(StringUtils.deleteWhitespace(resultsExportedAsJson), 
						expected), 
						"Search returned the following results "+resultsExportedAsJson+" but no results expected, that is \n"+expected);

		outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		//Change status to published
		loginToTestRepositoryAsSystem();
		((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.published.toString());
		contentObject = contentService.save(contentObject, false, true, null);

		loginToTestRepositoryAsAnonymous();
		
		outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertEquals(outcome.getCount(),1, "Could not find content objects matching criteria for anonymous user");

		loginToTestRepositoryAsSystem();
	}


	private List<ContentObjectMethodDeclaration> generateGetContentObjectMethodDeclarations()
	{

		if (contentServiceMethodDeclarations == null){
			contentServiceMethodDeclarations = new ArrayList<ContentObjectMethodDeclaration>();

			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("getContentObject", new Object[]{ResourceRepresentationType.XML, FetchLevel.FULL, null, null, false}, String.class, ResourceRepresentationType.class, 
					FetchLevel.class, CacheRegion.class, List.class, boolean.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("getContentObject", new Object[]{ResourceRepresentationType.CONTENT_OBJECT_LIST, FetchLevel.FULL, null, null, false}, String.class, ResourceRepresentationType.class, 
					FetchLevel.class, CacheRegion.class, List.class, boolean.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("getContentObject", new Object[]{ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL, null, null, false}, String.class, ResourceRepresentationType.class, 
					FetchLevel.class, CacheRegion.class, List.class, boolean.class));
			contentServiceMethodDeclarations.add(new ContentObjectMethodDeclaration("getContentObject", new Object[]{ResourceRepresentationType.JSON, FetchLevel.FULL, null, null, false}, String.class, ResourceRepresentationType.class, 
					FetchLevel.class, CacheRegion.class, List.class, boolean.class));

			//list.add(new GetContentObjectMethod("getContentObjectByVersionName", new Object[]{"1.0", "en"}, String.class, String.class, String.class));

		}
		return contentServiceMethodDeclarations;

	}

}
