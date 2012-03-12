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
package org.betaconceptframework.astroboa.test.model.query;

import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JcrUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsCriteriaTest extends AbstractRepositoryTest{

	@Test  
	public void testContainsCriterion() throws Exception {  

		checkExpression("systemName CONTAINS \"1\"", CriterionFactory.contains(CmsBuiltInItem.SystemName.getJcrName(), "1"));
		checkExpression("systemName CONTAINS \"1\"", CriterionFactory.createSimpleCriterion(CmsBuiltInItem.SystemName.getJcrName(), "1", QueryOperator.CONTAINS));
		
		checkExpression("profile.title CONTAINS \"1\"", CriterionFactory.contains("profile.title", "1"));
		checkExpression("profile.title CONTAINS \"1\"", CriterionFactory.createSimpleCriterion("profile.title", "1", QueryOperator.CONTAINS));

		checkExpression("profile CONTAINS \"1\"", CriterionFactory.contains("profile", "1"));
		checkExpression("profile CONTAINS \"1\"", CriterionFactory.createSimpleCriterion("profile", "1", QueryOperator.CONTAINS));

		checkExpression("commentSingle CONTAINS \"1\"", CriterionFactory.contains("commentSingle", "1"));
		checkExpression("commentSingle CONTAINS \"1\"", CriterionFactory.createSimpleCriterion("commentSingle", "1", QueryOperator.CONTAINS));

		checkExpression("comment CONTAINS \"1\"", CriterionFactory.contains("comment", "1"));
		checkExpression("comment CONTAINS \"1\"", CriterionFactory.createSimpleCriterion("comment", "1", QueryOperator.CONTAINS));

	}
	
	@Test  
	public void testBaseContentTypeCriterion() throws Exception {  

		checkExpression("objectType=\""+TEST_CONTENT_TYPE+"Type\"", 
				CriterionFactory.equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), Condition.OR, Arrays.asList(EXTENDED_TEST_CONTENT_TYPE,DIRECT_EXTENDED_TEST_CONTENT_TYPE,TEST_CONTENT_TYPE)));
		
		checkExpression("objectType=\""+EXTENDED_TEST_CONTENT_TYPE+"Type\"", 
				CriterionFactory.equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), Condition.OR, Arrays.asList(EXTENDED_TEST_CONTENT_TYPE,DIRECT_EXTENDED_TEST_CONTENT_TYPE)));
	}
	
	@Test
	public void testSearchText(){
		ContentObject testContentObject = createContentObject(repositoryUserService.getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID), 
				"testSearchText"); 
			
		((StringProperty)testContentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Test Content Object 3");

		((CalendarProperty)testContentObject.getCmsProperty("webPublication.webPublicationStartDate")).setSimpleTypeValue(Calendar.getInstance());
		
		((StringProperty)testContentObject.getCmsProperty("commentSingle.body")).setSimpleTypeValue("Test comment");
		
		((StringProperty)testContentObject.getCmsProperty("commentSingle.body")).setSimpleTypeValue("Test comment");
		((StringProperty)testContentObject.getCmsProperty("comment.body")).setSimpleTypeValue("Test comment");
		((StringProperty)testContentObject.getCmsProperty("comment.comment.body")).setSimpleTypeValue("Test comment");
		
		testContentObject = contentService.save(testContentObject, false, true, null);

		markObjectForRemoval(testContentObject);

		ContentObjectCriteria criteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

		criteria.doNotCacheResults();
		criteria.addCriterion(CriterionFactory.equals("profile.title", "Test Content Object 3"));
		criteria.addFullTextSearchCriterion("*comment*");

		CmsOutcome<ContentObject> results = contentService.searchContentObjects(criteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertTrue(results != null && results.getCount() == 1 &&
				testContentObject.getId().equals(results.getResults().get(0).getId()),
				"Could not find newly created content object of type test which contains text 'comment' on any property\n"+
				" Executed query "+ criteria.getXPathQuery());
		
		assertSearchReturnsNoResult("''commentTest");
		assertSearchReturnsNoResult("''commentTest''");
		assertSearchReturnsNoResult("commentTest AND ASTROBOA");
		assertSearchReturnsNoResult("commentTest OR ASTROBOA");
		assertSearchReturnsNoResult("''commentTest AND ASTROBOA");
		assertSearchReturnsNoResult("commentTest OR ASTROBOA''");
		assertSearchReturnsNoResult("-commentTest ASTROBOA''");
		assertSearchReturnsNoResult("-commentTest ASTROBOA");
		assertSearchReturnsNoResult("''-commentTest ASTROBOA");
		assertSearchReturnsNoResult("-commentTest ASTRO\"\" \"\"BOA");
		assertSearchReturnsNoResult("-commentTest ASTROBOA\"\"");
		assertSearchReturnsNoResult("-commentTest ASTROBOA");
		assertSearchReturnsNoResult("\"\"-commentTest ASTROBOA");
		assertSearchReturnsNoResult("\"\"''-commentTest ASTROBOA");
		assertSearchReturnsNoResult("_commentTest ASTROBOA''");
		assertSearchReturnsNoResult("_commentTest ASTROBOA");
		assertSearchReturnsNoResult("''_commentTest ASTROBOA");
		assertSearchReturnsNoResult("_commentTest ASTROBOA\"\"");
		assertSearchReturnsNoResult("_commentTest ASTROBOA");
		assertSearchReturnsNoResult("_comment\"Test AST\"ROBOA");
		assertSearchReturnsNoResult("\"\"_commentTest ASTROBOA");
		assertSearchReturnsNoResult("\"\"''_commentTest ASTROBOA");
		assertSearchReturnsNoResult("comment\\)Test");
		assertSearchReturnsNoResult("comment\\)\\)Test");
		assertSearchReturnsNoResult("comment\\(Test");
		assertSearchReturnsNoResult("comment\\(\\(Test");
		assertSearchReturnsNoResult("comment\\}Test");
		assertSearchReturnsNoResult("comment\\}\\}Test");
		assertSearchReturnsNoResult("comment\\[Test");
		assertSearchReturnsNoResult("comment\\[\\[Test");
		assertSearchReturnsNoResult("comment\\]Test");
		assertSearchReturnsNoResult("comment\\]\\]Test");
		assertSearchReturnsNoResult("comment\\^Test");
		assertSearchReturnsNoResult("comment\\^\\^Test");
		assertSearchReturnsNoResult("comment\\~\\~Test");
		assertSearchReturnsNoResult("comment\\?Test");
		assertSearchReturnsNoResult("comment\\?\\?Test");
		assertSearchReturnsNoResult("comment\\:Test");
		assertSearchReturnsNoResult("comment\\:\\:Test");
		assertSearchReturnsNoResult("comment\\Test");
		assertSearchReturnsNoResult("comment\\\\Test");

	}
	
	
	private void assertSearchReturnsNoResult(String textSearch){
		
		ContentObjectCriteria criteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

			criteria.doNotCacheResults();
			criteria.addFullTextSearchCriterion(textSearch);

			CmsOutcome<ContentObject> results = contentService.searchContentObjects(criteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			String coNames = "";
			
			if (results!= null && 
					results.getCount() > 0){
				for (ContentObject co : results.getResults()){
					coNames += " "+ co.getSystemName();
				}
			}
			Assert.assertTrue(results == null || results.getCount() == 0,
					"Found " + results.getCount()+ " in a search for text "+textSearch+ " while it should not have returned any result\n"+
					" Executed query "+ criteria.getXPathQuery()+ "\n System name of ContentObjects returned in Query :"+coNames);
	}
	
	
	@Test
	public void testQueryForNullAndNotNullSimpleAndComplexProperties() throws Throwable{

		ContentObject testContentObject = createContentObject(repositoryUserService.getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID), 
		"testQueryForNullAndNotNull"); 


		long value = (long)9999999;
		((LongProperty)testContentObject.getCmsProperty("simpleLong")).addSimpleTypeValue(value);

		((StringProperty)testContentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Test Content Object 2");

		testContentObject = contentService.save(testContentObject, false, true, null);

		markObjectForRemoval(testContentObject);

		ContentObject testContent2Object = contentService.getContentObject(testContentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		testContent2Object.getCmsProperty("profile");
		
		assertQueryReturnsContentObject(testContentObject,"simpleLong", true);
		assertQueryReturnsContentObject(testContentObject,"simpleString", false);

		assertQueryReturnsContentObject(testContentObject,"profile.language", true);
		assertQueryReturnsContentObject(testContentObject,"profile.contributor", false);

		assertQueryReturnsContentObject(testContentObject,"profile.subject", false);
		
		assertQueryReturnsContentObject(testContentObject,"webPublication.webPublicationStartDate", false);
		
		((CalendarProperty)testContentObject.getCmsProperty("webPublication.webPublicationStartDate")).setSimpleTypeValue(Calendar.getInstance());
		testContentObject = contentService.save(testContentObject, false, true, null);
		assertQueryReturnsContentObject(testContentObject,"webPublication.webPublicationStartDate", true);
		
		assertQueryReturnsContentObject(testContentObject,"commentSingle.body", false);
		
		((StringProperty)testContentObject.getCmsProperty("commentSingle.body")).setSimpleTypeValue("Test comment");
		testContentObject = contentService.save(testContentObject, false, true, null);
		assertQueryReturnsContentObject(testContentObject,"commentSingle.body", true);
		
	}


	private void assertQueryReturnsContentObject(
			ContentObject testContentObject, String propertyName, boolean notNull) throws Throwable {

		ContentObjectCriteria criteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

		criteria.doNotCacheResults();
		criteria.addCriterion(CriterionFactory.equals("profile.title", "Test Content Object 2"));

		if (notNull){
			criteria.addCriterion(CriterionFactory.isNotNull(propertyName));
		}
		else{
			criteria.addCriterion(CriterionFactory.isNull(propertyName));
		}

		CmsOutcome<ContentObject> results = contentService.searchContentObjects(criteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		SimpleCmsProperty<?,?,?> property = null;

		try{

			Assert.assertTrue(results != null && results.getCount() == 1 &&
					testContentObject.getId().equals(results.getResults().get(0).getId()),
					"Could not find newly created content object of type test with "+
					(notNull ? " not null" : " null ") + propertyName +" \n"+
					" Executed query "+ criteria.getXPathQuery());


			if (results != null && results.getCount() > 0){
				property = (SimpleCmsProperty<?, ?, ?>) results.getResults().get(0).getCmsProperty(propertyName);
			}

			if (notNull){
				Assert.assertTrue(property != null && property.hasValues(), "Property "+propertyName + " does not have values nevertheless query returned content object containing this property because it had values");
			}
			else{
				Assert.assertTrue(property != null && property.hasNoValues(), "Property "+propertyName + " has values nevertheless query returned content object containing this property because it had no values");
			}
		}
		catch(Throwable t){

			if (property != null && property.hasValues()){
				logger.error("Found values for property "+ propertyName);
				if (property.getPropertyDefinition().isMultiple()){
					for (Object value : property.getSimpleTypeValues()){
						logger.error(value.toString());
					}
				}
				else{
					logger.error(property.getSimpleTypeValue().toString());
				}
			}

			//Now dump jcr node for content object
			Node contentObjectNode = getSession().getNodeByIdentifier(testContentObject.getId());

			logger.error(JcrUtils.dumpNode(contentObjectNode, 0));

			throw t;

		}
	}


	@Test
	public void testQueryUsingLongAndStringAsPropertyValues(){

		ContentObject testContentObject = createContentObject(repositoryUserService.getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID), 
		"testQueryUsingLongAndStringAsPropertyValues"); 
		
		long value = (long)999999;
		((LongProperty)testContentObject.getCmsProperty("simpleLong")).addSimpleTypeValue(value);

		((StringProperty)testContentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Test Content Object");

		testContentObject = contentService.save(testContentObject, false, true, null);

		markObjectForRemoval(testContentObject);

		ContentObjectCriteria criteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

		criteria.doNotCacheResults();

		criteria.addCriterion(CriterionFactory.equals("simpleLong", value));
		criteria.addCriterion(CriterionFactory.equals("profile.title", "Test Content Object"));

		CmsOutcome<ContentObject> results = contentService.searchContentObjects(criteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertTrue(results != null && results.getCount() == 1 &&
				testContentObject.getId().equals(results.getResults().get(0).getId()),
				"Could not find newly created content object of type test with simpleLong="+value+ " \n"+
				" Executed query "+ criteria.getXPathQuery());

		//Now make the query using string value
		ContentObjectCriteria criteria2 = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

		criteria2.doNotCacheResults();

		criteria2.addCriterion(CriterionFactory.equals("simpleLong", String.valueOf(value)));

		CmsOutcome<ContentObject> results2 = contentService.searchContentObjects(criteria2, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertTrue(results2 != null && results2.getCount() == 1 &&
				testContentObject.getId().equals(results2.getResults().get(0).getId()),
				"Could not find newly created content object of type test with simpleLong="+value+ " \n"+
				" Executed query "+ criteria2.getXPathQuery());
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
