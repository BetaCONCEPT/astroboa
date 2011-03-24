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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsConcurrentModificationException;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectReferenceCriterion;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.engine.jcr.query.CalendarInfo;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateContentObject;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateSimpleCmsProperty;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.criteria.ContentObjectReferenceCritetionImpl;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.engine.CmsPropertyPath;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.test.util.JcrUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsConstants.ContentObjectStatus;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentServiceTest extends AbstractRepositoryTest {


	@Test
	public void testContainsInSearch() throws Throwable{
		
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser,"testContains", false);

		//Add text in a first level property
		((StringProperty)contentObject.getCmsProperty("stringEnum")).addSimpleTypeValue("BLUE");
		
		//Add text in a second level property
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Title for contains search");
		
		//Add text in a third level property
		((StringProperty)contentObject.getCmsProperty("commentSingle.body")).setSimpleTypeValue("New Comment");

		//Add text in a fourth level property
		((StringProperty)contentObject.getCmsProperty("commentSingle.comment.body")).setSimpleTypeValue("Text in a fourth level property");
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
		
		assertContainsInSearch(contentObject, contentObjectNode, "blu");

		assertContainsInSearch(contentObject, contentObjectNode, "titl");

		assertContainsInSearch(contentObject, contentObjectNode, "comm");

		assertContainsInSearch(contentObject, contentObjectNode, "fourth");


	}

	private void assertContainsInSearch(ContentObject contentObject,
			Node contentObjectNode, String searchText)
			throws RepositoryException {

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addFullTextSearchCriterion(searchText+"*");
		
		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid total count for query " + contentObjectCriteria.getXPathQuery()+"\n JcrNode expecting to match "+JcrUtils.dumpNode(contentObjectNode, 0) 
				+ " Matched objects "+ outcomeToXml(outcome));

		ContentObject coReturned= outcome.getResults().get(0);
		
		Assert.assertEquals(coReturned.getSystemName(), contentObject.getSystemName(), "Invalid object in search results");
	}
		
	private String outcomeToXml(CmsOutcome<ContentObject> outcome) {
		
		StringBuilder sb= new StringBuilder();
		
		if (outcome != null && outcome.getCount() >0){
			for (ContentObject co : outcome.getResults()){
				sb.append(co.xml(true)).append("\n");
			}
		}
		else{
			sb.append("Outcome return no results");
		}
		return sb.toString();
	}

	//@Test
	public void testRankingInSearch() throws Throwable{
 
		RepositoryUser systemUser = getSystemUser();

		for (int i=0;i<10;i++){

			ContentObject contentObject = createContentObject(systemUser,"testRankingIn"+i, false);

			((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("testRankingInSearch"+i);
			//Default values will be loaded
			contentObject.getCmsProperty("stringEnum");
			contentObject.getCmsProperty("longEnum");
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		}
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.addCriterion(CriterionFactory.simpleCmsPropertycontains("profile.title", "testRank*"));
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		contentObjectCriteria.addOrderProperty("profile.title", Order.ascending);
		
		
		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		for (ContentObject co: outcome.getResults()){
			Assert.assertEquals(outcome.getRanking(co), 1.0, "Invalid ranking in search results for object "+co.getSystemName());
		}

		
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.title", "testRankingIn1"));
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		contentObjectCriteria.addOrderProperty("profile.title", Order.ascending);
		
		outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		for (ContentObject co: outcome.getResults()){
			Assert.assertEquals(outcome.getRanking(co), 0.0, "Invalid ranking in search results for object "+co.getSystemName());
		}

	}
	
	
	//@Test
	public void testBatchSave(){
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		
		Topic topic = JAXBTestUtils.createTopic("topic-used-for-batch-save", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.setTaxonomy(getSubjectTaxonomy());
		
		ContentObject contentObject1 = createContentObject(systemUser, "test-batch-save-1", true);

		((StringProperty)contentObject1.getCmsProperty("singleComplexNotAspectWithCommonAttributes.additionalName")).setSimpleTypeValue("Test");
		((TopicProperty)contentObject1.getCmsProperty("singleComplexNotAspectWithCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		
		contentObject1 = contentService.save(contentObject1, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject1);
		
		ContentObject contentObject2 = createContentObject(systemUser, "test-batch-save-2", true);

		((StringProperty)contentObject2.getCmsProperty("singleComplexNotAspectWithCommonAttributes.additionalName")).setSimpleTypeValue("Test");
		((TopicProperty)contentObject2.getCmsProperty("singleComplexNotAspectWithCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		
		contentObject2 = contentService.save(contentObject2, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject2);
		
		//Retrieve both objects as resource collection (XML and JSON)
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addSystemNameEqualsAnyCriterion(Arrays.asList("test-batch-save-1","test-batch-save-2"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		String xmlResourceCollection = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);

		//change some values
		String newTitle = "xml-batch-save";
		xmlResourceCollection = xmlResourceCollection.replaceAll("test-batch-save", newTitle);
		
		//Try o save resource collection
		List<ContentObject> contentObjects = contentService.saveContentObjectResourceCollection(xmlResourceCollection, false, true, null); 
		
		assertBatchSaveResults(contentObject1, contentObject2, newTitle, contentObjects);
		
		//Retrieve objects using criteria
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addSystemNameEqualsAnyCriterion(Arrays.asList(newTitle+"-1",newTitle+"-2"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		assertBatchSaveResults(contentObject1, contentObject2, newTitle, outcome.getResults());
		
		//Using JSON
		String newJSONTitle = "json-batch-save";
		String jsonResourceCollection = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);

		jsonResourceCollection = jsonResourceCollection.replaceAll(newTitle, newJSONTitle);
		
		//Try o save resource collection
		contentObjects = contentService.saveContentObjectResourceCollection(jsonResourceCollection, false, true, null);
		
		assertBatchSaveResults(contentObject1, contentObject2, newJSONTitle, contentObjects);
		
		//Retrieve objects using criteria
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addSystemNameEqualsAnyCriterion(Arrays.asList(newJSONTitle+"-1",newJSONTitle+"-2"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		assertBatchSaveResults(contentObject1, contentObject2, newJSONTitle, outcome.getResults());

		//Using Content Object instances
		String newInstanceTitle = "instance-batch-save";
		CmsOutcome<ContentObject> contentObjectList = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		for (ContentObject co: contentObjectList.getResults()){
			co.setSystemName(co.getSystemName().replaceAll(newJSONTitle, newInstanceTitle));
			StringProperty titleProperty = ((StringProperty)co.getCmsProperty("profile.title"));
			
			titleProperty.setSimpleTypeValue(titleProperty.getSimpleTypeValue().replaceAll(newJSONTitle, newInstanceTitle));
		}
		
		//Try o save resource collection
		contentObjects = contentService.saveContentObjectResourceCollection(contentObjectList.getResults(), false, true, null);
		
		assertBatchSaveResults(contentObject1, contentObject2, newInstanceTitle, contentObjects);
		
		//Retrieve objects using criteria
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addSystemNameEqualsAnyCriterion(Arrays.asList(newInstanceTitle+"-1",newInstanceTitle+"-2"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		assertBatchSaveResults(contentObject1, contentObject2, newInstanceTitle, outcome.getResults());

	}

	private void assertBatchSaveResults(ContentObject contentObject1,
			ContentObject contentObject2, String newTitle,
			List<ContentObject> contentObjects) {
		
		Assert.assertEquals(contentObjects.size(), 2, "Invalid batch save result list size");
		
		for (ContentObject contentObject :contentObjects){
			
			if (contentObject.getId().equals(contentObject1.getId())){
				Assert.assertEquals(contentObject.getSystemName(),newTitle+"-1", "Invalid system name.");
				Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue(),newTitle+"-1", "Invalid system name.");
			}
			else if (contentObject.getId().equals(contentObject2.getId())){
				Assert.assertEquals(contentObject.getSystemName(),newTitle+"-2", "Invalid system name.");
				Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue(),newTitle+"-2", "Invalid system name.");
			}
			else{
				Assert.assertTrue(1==2, "Invalid content object"+ contentObject.toString()+". Expected "+contentObject1.toString() +" or "+contentObject2.toString());
			}
		}
	}
	
	//@Test
	public void testDeleteContentObjectWithTopicAndSpaceReference() throws ItemNotFoundException, RepositoryException{
		
		//Create topic
		Topic topic = JAXBTestUtils.createTopic("topic-reference", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Create content object
		ContentObject contentObject = createContentObject(getSystemUser(), "test-delete-with-topic-and-space-reference", false);
		((TopicProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		contentObject = contentService.save(contentObject, false, true, null);
		
		//Create space
		Space space =  createRootSpaceForOrganizationSpace("space-reference");
		space.addContentObjectReference(contentObject.getId());
		space = spaceService.save(space);
		
		//Delete content object
		contentService.deleteContentObject(contentObject.getId());
		
		//Check with Jcr
		try{
			Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
			Assert.assertNull(contentObjectNode, "ContentObject "+contentObject.getSystemName() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), contentObject.getId(), "Invalid ItemNotFoundException message");
		}
		
		//Check with ContentObject entity
		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		Assert.assertNull(contentObjectReloaded, "ContentObject "+contentObject.getSystemName() + " was not deleted");
		
		//Check with Topic entity 
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		
		Assert.assertEquals(topicReloaded.getContentObjectIdsWhichReferToThisTopic().size(), 0, "Topic "+topic.getName() + " should have been updated since content object has been deleted. "+topicReloaded.getContentObjectIdsWhichReferToThisTopic());

		Assert.assertEquals(topicReloaded.getNumberOfContentObjectsWhichReferToThisTopic(), 0, "Topic "+topic.getName() + " should have been updated since content object has been deleted");

		//Check with Astroboa Service
		List<String> contentObjectReferencesList = topicService.getContentObjectIdsWhichReferToTopic(topicReloaded.getId());
		Assert.assertEquals(contentObjectReferencesList.size(), 0, "Topic "+topic.getName() + " should have been updated since content object has been deleted");

		Assert.assertEquals(topicService.getCountOfContentObjectIdsWhichReferToTopic(topicReloaded.getId()), 0, "Topic "+topic.getName() + " should have been updated since content object has been deleted");

		//Check with space entity
		//Check with Jcr
		Node spaceNode = getSession().getNodeByUUID(space.getId());
		
		Assert.assertNotNull(spaceNode, "Space "+space.getName() + " was not saved at all");
		
		Assert.assertFalse(spaceNode.hasProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName()), "Space "+space.getName() + " was saved but reference to content object was not");

		//Check with Space entity 
		Space spaceReloaded = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		
		Assert.assertNotNull(spaceReloaded, "Space "+space.getName() + " was not saved at all");
		
		Assert.assertEquals(spaceReloaded.getContentObjectReferences().size(), 0, "Space "+space.getName() + " should have been updated since content object has been deleted");

		Assert.assertEquals(spaceReloaded.getNumberOfContentObjectReferences(), 0, "Space "+space.getName() + " should have been updated since content object has been deleted");

		//Check with Astroboa Service
		contentObjectReferencesList = spaceService.getContentObjectIdsWhichResideInSpace(spaceReloaded.getId());
		Assert.assertEquals(contentObjectReferencesList.size(), 0, "Space "+space.getName() + " should have been updated since content object has been deleted");

		Assert.assertEquals(spaceService.getCountOfContentObjectIdsWhichResideInSpace(spaceReloaded.getId()), 0, "Space "+space.getName() + " should have been updated since content object has been deleted");
	
	}
	
	//@Test
	public void testContentObjectExportOfComplexWithCommonAttributes() throws Exception{

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		
		Topic topic = JAXBTestUtils.createTopic("co3Topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.setTaxonomy(getSubjectTaxonomy());
		
		ContentObject contentObject = createContentObject(systemUser, "test-export-with-complex-with-id", true);

		((StringProperty)contentObject.getCmsProperty("singleComplexNotAspectWithCommonAttributes.additionalName")).setSimpleTypeValue("Test");
		((TopicProperty)contentObject.getCmsProperty("singleComplexNotAspectWithCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
		//Retrieve content object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.FULL, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithCommonAttributes.additionalName", "singleComplexNotAspectWithCommonAttributes.testTopic"), false);

		ComplexCmsProperty singleComplexNotAspectWithCommonAttributesProperty = (ComplexCmsProperty) contentObject.getCmsProperty("singleComplexNotAspectWithCommonAttributes");
		
		//Export to xml and JSON using all possible methods and check if common attributes are exported
		String xmlExportExpectedToBeFound = "<singleComplexNotAspectWithCommonAttributes cmsIdentifier=\""+singleComplexNotAspectWithCommonAttributesProperty.getId()+"\"";
		String xmlExportExpectedToBeFoundWithoutWhitespaces = removeWhitespacesIfNecessary(xmlExportExpectedToBeFound);
		
		String jsonExportExpectedToBeFound = "\"singleComplexNotAspectWithCommonAttributes\":{\"cmsIdentifier\":\""+singleComplexNotAspectWithCommonAttributesProperty.getId()+"\"";
		String jsonExportExpectedToBeFoundWithoutWhitespaces = removeWhitespacesIfNecessary(jsonExportExpectedToBeFound);
		
		String xmlFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, 
				FetchLevel.FULL, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithCommonAttributes", "singleComplexNotAspectWithCommonAttributes"), false);

		String xmlFromServiceWithoutWhitespaces = removeWhitespacesIfNecessary(xmlFromService);
		
		Assert.assertTrue(xmlFromServiceWithoutWhitespaces.contains(xmlExportExpectedToBeFoundWithoutWhitespaces), "Found common attributes in XML export from the ContentService of a complex property which does not define them in its schema\n"+xmlFromService);
		
		String xmlFromObject = contentObject.xml(prettyPrint);
		String xmlFromObjectWithoutWhitespaces = removeWhitespacesIfNecessary(xmlFromObject);
		
		Assert.assertTrue(xmlFromObjectWithoutWhitespaces.contains(xmlExportExpectedToBeFoundWithoutWhitespaces), "Found common attributes in XML export from method ContentObject.xml() of a complex property which does not define them in its schema\n"+xmlFromObject);
		
		String jsonFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, 
				FetchLevel.FULL, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithCommonAttributes", "singleComplexNotAspectWithCommonAttributes"), false);
		String jsonFromServiceWithoutWhitespaces = removeWhitespacesIfNecessary(jsonFromService);
		
		Assert.assertTrue(jsonFromServiceWithoutWhitespaces.contains(jsonExportExpectedToBeFoundWithoutWhitespaces), "Found common attributes in JSON export from the ContentService of a complex property which does not define them in its schema\n"+jsonFromService);
		
		String jsonFromObject = contentObject.json(prettyPrint);
		Assert.assertTrue(StringUtils.deleteWhitespace(jsonFromObject).contains(jsonExportExpectedToBeFoundWithoutWhitespaces), "Found common attributes in JSON export from method ContentObject.json() of a complex property which does not define them in its schema\n"+jsonFromObject);
		
	}
	
	//@Test
	public void testContentObjectExportOfComplexWithoutCommonAttributes() throws Exception{

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		
		Topic topic = JAXBTestUtils.createTopic("co2Topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.setTaxonomy(getSubjectTaxonomy());
		
		ContentObject contentObject = createContentObject(systemUser, "test-export-with-complex-no-id", true);

		((StringProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes.additionalName")).setSimpleTypeValue("Test");
		((TopicProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
		//Retrieve content object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithNoCommonAttributes.additionalName", "singleComplexNotAspectWithNoCommonAttributes.testTopic"), false);

		ComplexCmsProperty singleComplexNotAspectWithNoCommonAttributesProperty = (ComplexCmsProperty) contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes");
		
		//Export to xml and JSON using all possible methods and check if common attributes are exported
		String xmlExportExpectedNotToBeFound = "<singleComplexNotAspectWithNoCommonAttributes cmsIdentifier=\""+singleComplexNotAspectWithNoCommonAttributesProperty.getId()+"\"";
		String jsonExportExpectedNotToBeFound = "\"singleComplexNotAspectWithNoCommonAttributes\":{\"cmsIdentifier\":\""+singleComplexNotAspectWithNoCommonAttributesProperty.getId()+"\"";
		
		
		String xmlFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, 
				FetchLevel.ENTITY, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithNoCommonAttributes", "singleComplexNotAspectWithNoCommonAttributes"), false);
		
		Assert.assertFalse(xmlFromService.contains(xmlExportExpectedNotToBeFound), "Found common attributes in XML export from the ContentService of a complex property which does not define them in its schema\n"+xmlFromService);
		
		String xmlFromObject = contentObject.xml(prettyPrint);
		Assert.assertFalse(xmlFromObject.contains(xmlExportExpectedNotToBeFound), "Found common attributes in XML export from method ContentObject.xml() of a complex property which does not define them in its schema\n"+xmlFromObject);
		
		String jsonFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, 
				FetchLevel.ENTITY, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithNoCommonAttributes", "singleComplexNotAspectWithNoCommonAttributes"), false);
		Assert.assertFalse(jsonFromService.contains(jsonExportExpectedNotToBeFound), "Found common attributes in JSON export from the ContentService of a complex property which does not define them in its schema\n"+jsonFromService);
		
		String jsonFromObject = contentObject.json(prettyPrint);
		Assert.assertFalse(jsonFromObject.contains(jsonExportExpectedNotToBeFound), "Found common attributes in JSON export from method ContentObject.json() of a complex property which does not define them in its schema\n"+jsonFromObject);
		
	}
	
	//@Test
	public void testContentObjectUpdateWithSingleComplexWithNoId() throws Exception{

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		
		Topic topic = JAXBTestUtils.createTopic("coTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.setTaxonomy(getSubjectTaxonomy());
		
		ContentObject contentObject = createContentObject(systemUser, "test-update-with-complex-no-id", true);

		((StringProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes.additionalName")).setSimpleTypeValue("Test");
		((TopicProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
		//Now retrieve content object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithNoCommonAttributes.additionalName", "singleComplexNotAspectWithNoCommonAttributes.testTopic"), false);
		
		//Remove id from property and try to save it
		String previousId = new String(((ComplexCmsProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes")).getId());
		
		((ComplexCmsProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes")).setId(null);
		
		contentObject = contentService.save(contentObject, false, true, null);
			
		//Check that id has been provided.
		String newId = new String(((ComplexCmsProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes")).getId());
		
		Assert.assertEquals(previousId, newId, "Single value complex property 'singleComplexNotAspectWithNoCommonAttributes' was saved with different id");

		//Now reload content object to ensure that the same id is provided
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
					FetchLevel.ENTITY, CacheRegion.NONE, Arrays.asList("singleComplexNotAspectWithNoCommonAttributes.additionalName", "singleComplexNotAspectWithNoCommonAttributes.testTopic"), false);
			
		newId = new String(((ComplexCmsProperty)contentObject.getCmsProperty("singleComplexNotAspectWithNoCommonAttributes")).getId());
		
		Assert.assertEquals(previousId, newId, "Single value complex property 'singleComplexNotAspectWithNoCommonAttributes' was saved with different id");
	}
	
	//@Test
	public void testNodePathOfContentObjectSave() throws Exception{

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "test-save-in-right-node-path", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		Calendar created = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
		
		CmsRepositoryEntityUtils cmsRepositoryEntityUtils = AstroboaTestContext.INSTANCE.getBean(CmsRepositoryEntityUtils.class, null);
		
		Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(getSession(), contentObject.getId());
		
		
		CalendarInfo calInfo = new CalendarInfo(created);
		
		Assert.assertTrue(contentObjectNode.getParent().getPath().endsWith(calInfo.getFullPath()), "Content object was saved under path "+contentObjectNode.getParent().getPath() +
				" and not under path "+calInfo.getFullPath());
		
		
	}
	
	//@Test
	public void testGetContentObjectAsContentObjectOutcome() throws Throwable{
		
		ContentObject contentObject =  createContentObject(getSystemUser(), "contentObjectTestExportAsContentObjectOutcome", false);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		
		CmsOutcome<ContentObject> outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, FetchLevel.ENTITY, CacheRegion.NONE, null,false);
		
		Assert.assertNotNull(outcome, "ContentService.getContentObject returned null");
		
		Assert.assertEquals(outcome.getCount(), 1, "ContentService.getContentObject returned invalid count");
		Assert.assertEquals(outcome.getLimit(), 1, "ContentService.getContentObject returned invalid limit");
		Assert.assertEquals(outcome.getOffset(), 0, "ContentService.getContentObject returned invalid offset");
		
		
		Assert.assertEquals(outcome.getResults().size(), 1, "ContentService.getContentObject returned invalid number of ContentObjects");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), contentObject.getId(), "ContentService.getContentObject returned invalid contentObject");
	}
	
	//@Test
	public void testGetContentObjectXmlorJSON() throws Throwable{
		
		ContentObject contentObject =  createContentObject(getSystemUser(), "contentObjectTestExportXmlJSON", false);
		contentObject.getCmsProperty("stringEnum");
		contentObject.getCmsProperty("longEnum");
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		String contentObjectString = null;
		String contentObjectStringFromServiceUsingId = null;

		List<ResourceRepresentationType<String>> outputs = Arrays.asList(ResourceRepresentationType.JSON, ResourceRepresentationType.XML);
		
		try{
			
			for (ResourceRepresentationType<String> output : outputs){

				//Check full export of contentObject children
				contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL,	CacheRegion.NONE, null, true);
				
				if (output.equals(ResourceRepresentationType.XML)){
					contentObjectString = contentObject.xml(prettyPrint);
				}
				else{
					contentObjectString = contentObject.json(prettyPrint);
				}
				
				contentObjectStringFromServiceUsingId = contentService.getContentObject(contentObject.getId(), output, FetchLevel.FULL,CacheRegion.NONE, null, true);

				ContentObject contentObjectFromServiceWithId = importDao.importContentObject(contentObjectStringFromServiceUsingId, false, false, ImportMode.DO_NOT_SAVE);
				
				repositoryContentValidator.compareContentObjects(contentObject, contentObjectFromServiceWithId, true);
			
			}			
		}
		catch(Throwable e){
			logger.error("Initial \n{}",TestUtils.prettyPrintXml(contentObjectString));
			logger.error("Using Id \n{}",TestUtils.prettyPrintXml(contentObjectStringFromServiceUsingId));
			throw e;
		}	
	}
	

	@Test
	public void testSearchUsingNumericAndDateCriterion(){

		
		Calendar beforeObjectSave  = Calendar.getInstance();
		beforeObjectSave.add(Calendar.DAY_OF_MONTH, -1);
		
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObjectForType(TEST_CONTENT_TYPE, systemUser, "testSearchUsingNumbericCriterion", false);
		
		((LongProperty)contentObject.getCmsProperty("simpleLong")).setSimpleTypeValue(Long.valueOf(5));
		((LongProperty)contentObject.getCmsProperty("simpleLongMultiple")).addSimpleTypeValue(Long.valueOf(10));
		
		((LongProperty)contentObject.getCmsProperty("integerConstrained")).setSimpleTypeValue(Long.valueOf(100));
		
		((DoubleProperty)contentObject.getCmsProperty("simpleDouble")).setSimpleTypeValue(Double.valueOf(5));
		((DoubleProperty)contentObject.getCmsProperty("simpleDoubleMultiple")).addSimpleTypeValue(Double.valueOf(10));
		
		Calendar simpleDateTimeValue  = Calendar.getInstance();
		((CalendarProperty)contentObject.getCmsProperty("simpleDateTime")).setSimpleTypeValue(simpleDateTimeValue);
		((CalendarProperty)contentObject.getCmsProperty("simpleDateTimeMultiple")).addSimpleTypeValue(simpleDateTimeValue);

		Calendar simpleDateValue  = Calendar.getInstance();
		((CalendarProperty)contentObject.getCmsProperty("simpleDate")).setSimpleTypeValue(simpleDateValue);
		((CalendarProperty)contentObject.getCmsProperty("simpleDateMultiple")).addSimpleTypeValue(simpleDateValue);

		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong=\"5\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong!=\"4\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong>=\"5\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong>\"4\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong<=\"5\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLong<\"6\"");
		
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple=\"10\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple!=\"9\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple>=\"10\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple>\"9\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple<=\"10\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleLongMultiple<\"11\"");

		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble=\"5.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble!=\"4.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble>=\"5.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble>\"4.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble<=\"5.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDouble<\"6.0\"");
		
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple=\"10.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple!=\"9.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple>=\"10.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple>\"9.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple<=\"10.0\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDoubleMultiple<\"11.0\"");

		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained=\"100\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained!=\"4\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained>=\"100\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained>\"99\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained<=\"100\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "integerConstrained<\"101\"");

		Calendar afterObjectSave  = Calendar.getInstance();
		afterObjectSave.add(Calendar.DAY_OF_MONTH, 2);
		
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime>\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime>=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime>=\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime<\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime<=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTime<=\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");

		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple>\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple>=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple>=\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple<\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple<=\""+XPathUtils.formatForQuery(simpleDateTimeValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateTimeMultiple<=\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");

		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate>\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate>=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate>=\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate<\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate<=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDate<=\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");

		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple>\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple>=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple>=\""+XPathUtils.formatForQuery(beforeObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple<\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple<=\""+XPathUtils.formatForQuery(simpleDateValue)+"\"");
		addCriterionForPropertyAndAssertResult(contentObject.getSystemName(), "simpleDateMultiple<=\""+XPathUtils.formatForQuery(afterObjectSave)+"\"");

		
	}
	
	private void addCriterionForPropertyAndAssertResult(String systemName,String expression) {

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.addSystemNameEqualsCriterion(systemName);
		
		CriterionFactory.parse(expression, contentObjectCriteria);
		
		assertResult(contentObjectCriteria, 1, systemName);
		
	}

	@Test
	public void testEmptyComplexCmsPropertySave() throws ItemNotFoundException, RepositoryException{

		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObjectForType("genericContentResourceObject", systemUser, "testEmptyComplexCmsPropertySave", false);
		
		//Create an empty complex property
		ComplexCmsProperty workflow = (ComplexCmsProperty) contentObject.getCmsProperty("workflow");
		
		//Save object
		contentObject = contentService.saveContentObject(contentObject, false);
		
		Assert.assertNull(workflow.getId(), "Empty complex property has an identifier where it should not have" );
		
		((StringProperty)workflow.getChildProperty("managedThroughWorkflow")).addSimpleTypeValue("webPublishing");
		
		contentObject = contentService.saveContentObject(contentObject, false);
		
		Assert.assertNotNull(workflow.getId(), "Complex property 'worklfow' does not have an identifier where it should have" );
		
		//Now delete workflow.managedWorkflow
		((StringProperty)workflow.getChildProperty("managedThroughWorkflow")).removeValues();
		contentObject = contentService.saveContentObject(contentObject, false);
		
		Assert.assertNull(workflow.getId(), "Complex property 'worklfow' has an identifier where it should not have" );
		
	}
	
	@Test
	public void testSaveWithEmptyWebPublication(){

		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObjectForType("genericContentResourceObject", systemUser, "testEmptyWebPublication", false);
		
		((BooleanProperty)contentObject.getCmsProperty("webPublication.publishCreatorName")).setSimpleTypeValue(false);
		
		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		try{
			((BooleanProperty)contentObject.getCmsProperty("webPublication.publishCreatorName")).setSimpleTypeValue(true);
		
			contentObject = contentService.saveContentObject(contentObject, false);
		}
		catch(CmsException e){
			Assert.assertTrue(e.getMessage() != null && e.getMessage().contains("There are some mandatory children of the property genericContentResourceObject.webPublication which need to be saved but they do not have any value. There is at least one child property which can be saved without any problem"));
			contentObject = contentService.getContentObject(contentObject.getId(), null, CacheRegion.NONE);
		}

		((CalendarProperty)contentObject.getCmsProperty("webPublication.webPublicationStartDate")).setSimpleTypeValue(Calendar.getInstance());
		((BooleanProperty)contentObject.getCmsProperty("webPublication.publishCreatorName")).setSimpleTypeValue(false);
		
		contentObject = contentService.saveContentObject(contentObject, false);

	}
	
	//@Test
	public void testCopy(){
		
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser,"testCopy", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		ContentObject thirdContentObject = null;
		
		for (int i=0;i<5;i++){
			ContentObject clonedContentObject = contentService.copyContentObject(contentObject.getId());

			clonedContentObject = contentService.save(clonedContentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(clonedContentObject);
			
			Assert.assertFalse(clonedContentObject.getId().equals(contentObject.getId()), "Identifiers should not be the same");

			assertPropertiesAreTheSame(contentObject.getComplexCmsRootProperty(), clonedContentObject.getComplexCmsRootProperty());
			
			if (i==2){
				thirdContentObject = clonedContentObject;
			}
		}
		
		for (int i=5;i<10;i++){
			ContentObject clonedContentObject = contentService.copyContentObject(thirdContentObject.getId());

			clonedContentObject = contentService.save(clonedContentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(clonedContentObject);

			Assert.assertFalse(clonedContentObject.getId().equals(contentObject.getId()), "Identifiers should not be the same");

			assertPropertiesAreTheSame(contentObject.getComplexCmsRootProperty(), clonedContentObject.getComplexCmsRootProperty());
			
		}
				
	}
	
	private void assertPropertiesAreTheSame(ComplexCmsProperty sourceComplexCmsProperty, ComplexCmsProperty copyComplexCmsProperty){
		
		Assert.assertFalse(sourceComplexCmsProperty.getId().equals(copyComplexCmsProperty.getId()), "Identifiers should not be the same "+ sourceComplexCmsProperty.getPath());

		Assert.assertFalse(((LazyCmsProperty) sourceComplexCmsProperty).getPropertyContainerUUID().equals(((LazyCmsProperty) copyComplexCmsProperty).getPropertyContainerUUID()), "Property Container UUID should not be the same "+ sourceComplexCmsProperty.getPath());
		Assert.assertFalse(((LazyCmsProperty) sourceComplexCmsProperty).getContentObjectNodeUUID().equals(((LazyCmsProperty) copyComplexCmsProperty).getPropertyContainerUUID()), "ContentObject Node UUID should not be the same "+ sourceComplexCmsProperty.getPath());

	}
	
	
	//@Test
	public void testProfileModifiedChangedUponSave()
	{
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser,"testLastModifiedChange", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		Calendar lastModified = ((CalendarProperty)contentObject.getCmsProperty("profile.modified")).getSimpleTypeValue();
		
		//Resaved object without changing lastModified Date
		contentObject = contentService.save(contentObject, false, false, null);
		Calendar newLastModified = ((CalendarProperty)contentObject.getCmsProperty("profile.modified")).getSimpleTypeValue();
		Assert.assertEquals(newLastModified, lastModified, "Content Object profile.modified date has changed.");
		
		//Resave object and change profile modified date
		contentObject = contentService.save(contentObject, false, true, null);
		newLastModified = ((CalendarProperty)contentObject.getCmsProperty("profile.modified")).getSimpleTypeValue();
		Assert.assertFalse(newLastModified.equals(lastModified), "Content Object profile.modified date has not changed.");
		
		//Save new content object with flag set to false
		contentObject = createContentObject(systemUser,"testLastModifiedChange2", true);

		contentObject = contentService.save(contentObject, false, false, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
		//Last modified must exist in newly created object
		CalendarProperty newLastModifiedProperty = ((CalendarProperty)contentObject.getCmsProperty("profile.modified"));
		Assert.assertTrue(newLastModifiedProperty != null && newLastModifiedProperty.hasValues(), "Content Object profile.modified date has not been assigned to newly created content object.");

		
		//Provide a profile.modified date which is before current modified date
		Calendar newModifiedDate = Calendar.getInstance();
		newModifiedDate.setTimeInMillis(newModifiedDate.getTimeInMillis());
		newModifiedDate.add(Calendar.YEAR, -1);
		((CalendarProperty)contentObject.getCmsProperty("profile.modified")).setSimpleTypeValue(newModifiedDate);
		
		//Save with updateLastModifiedDate flag set to false
		try{
			contentObject = contentService.save(contentObject, false, false, null);
		}
		catch(CmsConcurrentModificationException ccme){
			Assert.assertTrue(ccme.getMessage().startsWith("Content Object "+contentObject.getId()+"/"+contentObject.getSystemName()+" has been concurrently modified by another user or current user has tried to  set a value for  profile.modified property."), 
					"Invalid error message "+ ccme.getMessage());
		}
		
		
		//Save with updateLastModifiedDate flag set to true
		try{
			contentObject = contentService.save(contentObject, false, true, null);
		}
		catch(CmsConcurrentModificationException ccme){
			Assert.assertTrue(ccme.getMessage().startsWith("Content Object "+contentObject.getId()+"/"+contentObject.getSystemName()+" has been concurrently modified by another user or current user has tried to  set a value for  profile.modified property."), 
					"Invalid error message "+ ccme.getMessage());
		}
		
		//Provide a profile.modified date which is after current modified date
		newModifiedDate.setTimeInMillis(newModifiedDate.getTimeInMillis());
		newModifiedDate.add(Calendar.YEAR, 3);
		((CalendarProperty)contentObject.getCmsProperty("profile.modified")).setSimpleTypeValue(newModifiedDate);
		
		//Save with updateLastModifiedDate flag set to false
		try{
			contentObject = contentService.save(contentObject, false, false, null);
		}
		catch(CmsConcurrentModificationException ccme){
			Assert.assertTrue(ccme.getMessage().startsWith("Content Object "+contentObject.getId()+"/"+contentObject.getSystemName()+" has been concurrently modified by another user or current user has tried to  set a value for  profile.modified property."), 
					"Invalid error message "+ ccme.getMessage());
		}
		
		
		//Save with updateLastModifiedDate flag set to true
		try{
			contentObject = contentService.save(contentObject, false, true, null);
		}
		catch(CmsConcurrentModificationException ccme){
			Assert.assertTrue(ccme.getMessage().startsWith("Content Object "+contentObject.getId()+"/"+contentObject.getSystemName()+" has been concurrently modified by another user or current user has tried to  set a value for  profile.modified property."), 
					"Invalid error message "+ ccme.getMessage());
		}
		
	}
	
	//@Test
	public void testProjections(){

		RepositoryUser systemUser = getSystemUser();

		int numberOfObjects = 5;
		for (int i=0;i<numberOfObjects;i++){

			ContentObject contentObject = createContentObject(systemUser,"testProjections"+i, true);

			((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("testProjections");
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		}

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.title","testProjections"));
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.title");
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.language");
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("stringEnum");
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("comment.comment");
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.hasPart");

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertEquals(outcome.getCount(), numberOfObjects, "Total count invalid ");

		Assert.assertEquals(outcome.getResults().size(), numberOfObjects, "Results rendered count invalid ");

		for (ContentObject contentObject : outcome.getResults()){
			assertProjectedPathAlreadyLoaded(contentObject, "profile.title");
			assertProjectedPathAlreadyLoaded(contentObject, "profile.language");
			assertProjectedPathAlreadyLoaded(contentObject, "stringEnum");
			assertProjectedPathAlreadyLoaded(contentObject, "profile.hasPart");
			assertProjectedPathAlreadyLoaded(contentObject, "comment[0].comment[0]");
			
			assertPathNotLoaded(contentObject, "accessibility");
		}

	}
	
	private void assertPathNotLoaded(ContentObject contentObject,
			String propertyPath) {
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().isChildPropertyLoaded(propertyPath), 
				"Property "+propertyPath + " has been pre-loaded but it should not be");
		
	}

	private void assertProjectedPathAlreadyLoaded(
			ContentObject contentObject, String propertyPath) {
		
			boolean childPropertyLoaded = contentObject.getComplexCmsRootProperty().isChildPropertyLoaded(propertyPath);
			
			if (! childPropertyLoaded){
				contentObject.getCmsProperty(propertyPath);
			}
			
			Assert.assertTrue(childPropertyLoaded, 
					"Property "+propertyPath + " has not been pre-loaded");
		
	}

	//@Test
	public void testUnmanagedBinaryChannel() throws Exception{
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "unmanagedImage", true);

		BinaryChannel logoBinaryChannel = loadUnManagedBinaryChannel(logo.getName(), "unmanagedImage");
		BinaryChannel logo2BinaryChannel = loadUnManagedBinaryChannel(logo2.getName(), "unmanagedImage");

		//Add two binary channels in property image
		((BinaryProperty)contentObject.getCmsProperty("unmanagedImage")).addSimpleTypeValue(logoBinaryChannel);
		((BinaryProperty)contentObject.getCmsProperty("unmanagedImage")).addSimpleTypeValue(logo2BinaryChannel);

		contentObject = contentService.save(contentObject, false, true, null);

		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		BinaryProperty unmanagedImageProperty = (BinaryProperty)contentObjectReloaded.getCmsProperty("unmanagedImage");

		Assert.assertTrue(unmanagedImageProperty.hasValues(), "No binary channel saved for unmanagedImage property");
		Assert.assertTrue(unmanagedImageProperty.getSimpleTypeValues().size()==2, "Should have saved 2 binary channels for unmanagedImage property");


		for (BinaryChannel unmanagedImageBinaryChannel : unmanagedImageProperty.getSimpleTypeValues()){
			
			String sourceFilename = unmanagedImageBinaryChannel.getSourceFilename();
			
			Assert.assertTrue(StringUtils.isNotBlank(sourceFilename), " BinaryChannel "+ unmanagedImageBinaryChannel.getName() + " does not have a source file name");
			
			File fileWhoseContentsAreSavedInBinaryChannel = null;
			
			if (sourceFilename.equals(logo.getName())){
				fileWhoseContentsAreSavedInBinaryChannel = logo;
			}
			else if (sourceFilename.equals(logo2.getName())){
				fileWhoseContentsAreSavedInBinaryChannel = logo2;
			}
			else {
				throw new Exception("BnaryChannel contains an invalid source file name "+ sourceFilename);
			}
			
			String mimeType = new MimetypesFileTypeMap().getContentType(fileWhoseContentsAreSavedInBinaryChannel);
			
			Assert.assertEquals(unmanagedImageBinaryChannel.getName(), "unmanagedImage");
			Assert.assertEquals(unmanagedImageBinaryChannel.getMimeType(), mimeType);
			Assert.assertEquals(unmanagedImageBinaryChannel.getSourceFilename(), sourceFilename);
			Assert.assertEquals(unmanagedImageBinaryChannel.getSize(), fileWhoseContentsAreSavedInBinaryChannel.length());
			Assert.assertEquals(unmanagedImageBinaryChannel.getModified().getTimeInMillis(), fileWhoseContentsAreSavedInBinaryChannel.lastModified());


			//Now test in jcr to see if the proper node is created
			//Unmanaged Binary property do not have an ID as they represent a jcr property
			Node contentObjectNode = getSession().getNodeByUUID(contentObjectReloaded.getId()); 

			Property unmanagedImageJcrProperty = contentObjectNode.getProperty("unmanagedImage");
			
			//This property is multivalue and should contain
			Value[] relativePaths = unmanagedImageJcrProperty.getValues();
			
			Assert.assertEquals(relativePaths.length, 2, " Jcr property "+unmanagedImageJcrProperty.getPath() + " does not contain 2 values");
			
			boolean foundPath = true;
			for (Value relativePath : relativePaths){
				if (relativePath.getString().equals(fileWhoseContentsAreSavedInBinaryChannel.getName())){
					foundPath = true;
					break;
				}
			}
			
			Assert.assertTrue(foundPath, "Relative Path for unmanaged image "+fileWhoseContentsAreSavedInBinaryChannel.getName() + " was not saved");

		}

	}
	
	//@Test
	public void testManagedBinaryChannel() throws Exception{
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "image", true);

		BinaryChannel logoBinaryChannel = loadManagedBinaryChannel(logo, "image");
		BinaryChannel logo2BinaryChannel = loadManagedBinaryChannel(logo2, "image");

		//Add two binary channels in property image
		((BinaryProperty)contentObject.getCmsProperty("image")).addSimpleTypeValue(logoBinaryChannel);
		((BinaryProperty)contentObject.getCmsProperty("image")).addSimpleTypeValue(logo2BinaryChannel);

		contentObject = contentService.save(contentObject, false, true, null);

		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, null, false);


		BinaryProperty imageProperty = (BinaryProperty)contentObjectReloaded.getCmsProperty("image");

		Assert.assertTrue(imageProperty.hasValues(), "No binary channel saved for image property");
		Assert.assertTrue(imageProperty.getSimpleTypeValues().size()==2, "Should have saved 2 binary channels for image property");


		for (BinaryChannel imageBinaryChannel : imageProperty.getSimpleTypeValues()){
			
			String sourceFilename = imageBinaryChannel.getSourceFilename();
			
			Assert.assertTrue(StringUtils.isNotBlank(sourceFilename), " BinaryChannel "+ imageBinaryChannel.getName() + " does not have a source file name");
			
			File fileWhoseContentsAreSavedInBinaryChannel = null;
			
			if (sourceFilename.equals(logo.getName())){
				fileWhoseContentsAreSavedInBinaryChannel = logo;
			}
			else if (sourceFilename.equals(logo2.getName())){
				fileWhoseContentsAreSavedInBinaryChannel = logo2;
			}
			else {
				throw new Exception("BnaryChannel contains an invalid source file name "+ sourceFilename);
			}
			
			String mimeType = new MimetypesFileTypeMap().getContentType(fileWhoseContentsAreSavedInBinaryChannel);
			
			Assert.assertEquals(imageBinaryChannel.getName(), "image");
			Assert.assertEquals(imageBinaryChannel.getMimeType(), mimeType);
			Assert.assertEquals(imageBinaryChannel.getSourceFilename(), sourceFilename);
			Assert.assertEquals(imageBinaryChannel.getSize(), fileWhoseContentsAreSavedInBinaryChannel.length());
			Assert.assertEquals(imageBinaryChannel.getModified().getTimeInMillis(), fileWhoseContentsAreSavedInBinaryChannel.lastModified());


			//Now test in jcr to see if the proper node is created
			Node binaryChannelNode = getSession().getNodeByUUID(imageBinaryChannel.getId()); 

			//If node is not found then exception has already been thrown
			Assert.assertEquals(binaryChannelNode.getName(), imageBinaryChannel.getName(), " Invalid name for binary data jcr node "+ binaryChannelNode.getPath());

			Assert.assertEquals(binaryChannelNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString(), "image");
			Assert.assertEquals(binaryChannelNode.getProperty(JcrBuiltInItem.JcrMimeType.getJcrName()).getString(), mimeType);
			Assert.assertEquals(binaryChannelNode.getProperty(CmsBuiltInItem.SourceFileName.getJcrName()).getString(), sourceFilename);
			Assert.assertEquals(binaryChannelNode.getProperty(CmsBuiltInItem.Size.getJcrName()).getLong(), fileWhoseContentsAreSavedInBinaryChannel.length());
			Assert.assertEquals(binaryChannelNode.getProperty(JcrBuiltInItem.JcrLastModified.getJcrName()).getDate().getTimeInMillis(), fileWhoseContentsAreSavedInBinaryChannel.lastModified());

		}

	}

	
	//@Test
	public void testSaveCreationDate() throws ItemNotFoundException, RepositoryException{
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "testSaveCreationDate", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		//Retrieve content object node
		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
		
		Calendar creationDate = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
	
		String creationDatePath = DateFormatUtils.format(creationDate, "yyyy/M/d/H/m/s");
		
		Assert.assertTrue(contentObjectNode.getParent().getPath().endsWith(creationDatePath), "Invalid content object creation path "+contentObjectNode.getParent().getPath()+ ". It should end with "+ creationDatePath);
	}
	
	//@Test
	public void testSaveWithProvidedCreationDate() throws ItemNotFoundException, RepositoryException{
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "testSaveWithProvidedCreationDate", true);

		Calendar userCreationDate = Calendar.getInstance();
		userCreationDate.add(Calendar.YEAR, 1);
		
		((CalendarProperty)contentObject.getCmsProperty("profile.created")).setSimpleTypeValue(userCreationDate);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, 
				null, false);
		
		//Retrieve content object node
		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
		
		Calendar creationDate = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
	
		Assert.assertEquals(userCreationDate.getTimeInMillis(), creationDate.getTimeInMillis(), 
				"Invalid creation date. User provided "+DateFormatUtils.format(userCreationDate, "dd/MM/yyy HH:mm:ss")+ " "+userCreationDate.getTimeInMillis()+ 
				" Persisted value "+DateFormatUtils.format(creationDate, "dd/MM/yyy HH:mm:ss")+ " "+creationDate.getTimeInMillis());
		
		String creationDatePath = DateFormatUtils.format(creationDate, "yyyy/M/d/H/m/s");
		
		Assert.assertTrue(contentObjectNode.getParent().getPath().endsWith(creationDatePath), "Invalid content object creation path "+contentObjectNode.getParent().getPath()+ ". It should end with "+ creationDatePath);
	}
	
	//@Test
	public void testUpdateCreationDate() throws ItemNotFoundException, RepositoryException{
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "testUpdateCreationDate", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		//reload content object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, 
				null, false);

		Calendar validCreationDate = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
		
		Calendar userCreationDate = Calendar.getInstance();
		userCreationDate.add(Calendar.YEAR, 1);
		
		((CalendarProperty)contentObject.getCmsProperty("profile.created")).setSimpleTypeValue(userCreationDate);
		
		TestLogPolicy.setLevelForLogger(Level.FATAL, PopulateSimpleCmsProperty.class.getName());
		contentObject = contentService.save(contentObject, false, true, null);
		TestLogPolicy.setDefaultLevelForLogger(PopulateSimpleCmsProperty.class.getName());
		
		//reload content object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, 
				null, false);
		
		
		Calendar persistedCreationDate = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
	
		Assert.assertEquals(validCreationDate.getTimeInMillis(), persistedCreationDate.getTimeInMillis(), 
				"Invalid creation date. Found "+DateFormatUtils.format(validCreationDate, "dd/MM/yyy HH:mm:ss")+ " "+validCreationDate.getTimeInMillis()+ 
				" but should have been "+DateFormatUtils.format(persistedCreationDate, "dd/MM/yyy HH:mm:ss")+ " "+persistedCreationDate.getTimeInMillis());

		
		//Retrieve content object node
		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
		String creationDatePath = DateFormatUtils.format(validCreationDate, "yyyy/M/d/H/m/s");
		
		Assert.assertTrue(contentObjectNode.getParent().getPath().endsWith(creationDatePath), "Invalid content object creation path "+contentObjectNode.getParent().getPath()+ ". It should end with "+ creationDatePath);
	}

	//@Test
	public void testSaveWithVariousSystemNames(){

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "validSystemName", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);


		//Now provide invalid system name
		checkInvalidSystemNameSave(contentObject, "invalid)SystemName");
		checkInvalidSystemNameSave(contentObject, "invalid((SystemName");
		checkInvalidSystemNameSave(contentObject, "invalid)SystemNa&me");
		checkInvalidSystemNameSave(contentObject, "Inv{a}li[dC]ha! ra?ct^e&rs");
		checkInvalidSystemNameSave(contentObject, "Inv{a}li[dC]ha! ra?ct^ers");

		checkInvalidSystemNameSave(contentObject, "ςδςδ");
		checkInvalidSystemNameSave(contentObject, "invaliδName+");
		checkInvalidSystemNameSave(contentObject, "Inv{a}li[dC]ha! ra?ct^e&rs");


		checkInvalidSystemNameSave(contentObject, "Ελληνι?κά");
		checkInvalidSystemNameSave(contentObject, "Ελλ@#ηνικά");
		checkInvalidSystemNameSave(contentObject, "Ελ..λην_ι-κ-ά");
		checkInvalidSystemNameSave(contentObject, "{{{{{");

		checkValidSystemNameSave(contentObject, "09092");
		checkValidSystemNameSave(contentObject, "09sasas");
		checkValidSystemNameSave(contentObject, "09_sdds-02");
		checkValidSystemNameSave(contentObject, "____");
		checkValidSystemNameSave(contentObject, "sdsds");
		checkValidSystemNameSave(contentObject, "090..92");
		checkValidSystemNameSave(contentObject, "090.92");
		checkValidSystemNameSave(contentObject, "090..__--92");
		checkValidSystemNameSave(contentObject, "090..92");

		checkSystemNameTransformation(contentObject, "+leading dash   in  system name  ","leading-dash-in-system-name");
		checkSystemNameTransformation(contentObject, "trailing dash   in  system name  +","trailing-dash-in-system-name");
		checkSystemNameTransformation(contentObject, "+   both leading and trailing dash   in  system name    +","both-leading-and-trailing-dash-in-system-name");
		checkSystemNameTransformation(contentObject, "My    System Name", "My-System-Name");
		checkSystemNameTransformation(contentObject, "  My    System   Name      is very long", "My-System-Name-is-very-long");
		checkSystemNameTransformation(contentObject, "invalid)SystemName","invalid-SystemName");
		checkSystemNameTransformation(contentObject, "invali((SystemName","invali-SystemName");
		checkSystemNameTransformation(contentObject, "invalid)SystemNa&m","invalid-SystemNa-m");
		checkSystemNameTransformation(contentObject, "Inv{a}li[dC]a! ra?ct^e&rs","Inv-a-li-dC-a-ra-ct-e-rs");
		checkSystemNameTransformation(contentObject, "In{a}li[dC]ha! ra?ct^ers","In-a-li-dC-ha-ra-ct-ers");
		checkSystemNameTransformation(contentObject, "ςδςδ","sdsd");
		checkSystemNameTransformation(contentObject, "invaliδName+","invalidName");
		checkSystemNameTransformation(contentObject, "Ianv{a}li[dC]! ra?ct^e&rs","Ianv-a-li-dC-ra-ct-e-rs");
		checkSystemNameTransformation(contentObject, "Ελληνι?κά","Ellhni-ka");
		checkSystemNameTransformation(contentObject, "Ελλ@#ηνικά","Ell-hnika");
		checkSystemNameTransformation(contentObject, "Ελ..λην_ι-κ-ά","El..lhn_i-k-a");
		checkSystemNameTransformation(contentObject, "09092","09092");
		checkSystemNameTransformation(contentObject, "09sasas","09sasas");
		checkSystemNameTransformation(contentObject, "09_sdds-02","09_sdds-02");
		checkSystemNameTransformation(contentObject, "____","____");
		checkSystemNameTransformation(contentObject, "sdsds","sdsds");
		checkSystemNameTransformation(contentObject, "090..92","090..92");
		checkSystemNameTransformation(contentObject, "090.92","090.92");
		checkSystemNameTransformation(contentObject, "090..__--92","090..__-92");
		checkSystemNameTransformation(contentObject, "090..92","090..92");
		checkSystemNameTransformation(contentObject, "a{{{{{{{{{l","a-l");
		
	}

	/**
	 * @param contentObject
	 * @param string
	 * @param string2
	 */
	private void checkSystemNameTransformation(ContentObject contentObject,
			String systemName, String systemNameAfterSave) {
		
		contentObject.setSystemName(null);
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(systemName);
		contentObject = contentService.saveContentObject(contentObject, false);

		Assert.assertEquals(contentObject.getSystemName(), systemNameAfterSave, "System name was not transformed correctly");
		
	}

	//@Test
	public void testSystemName(){
		
		String expectedSystemName = "titleForSystemName";

		ContentObject contentObject = createContentObject(repositoryUserService.getSystemRepositoryUser(), "", false);

		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(expectedSystemName);
		
		contentObject = contentService.save(contentObject, false, true, null);
		
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		Assert.assertEquals(contentObject.getSystemName(), expectedSystemName);
		
		//Re-save without any change and without loading profile.title
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		//Nullify system name to force retrieve systemName name from profile.title
		contentObject.setSystemName(null);
		contentObject = contentService.save(contentObject, false, true, null);
		Assert.assertEquals(contentObject.getSystemName(), expectedSystemName);
		
		expectedSystemName = "Changed";
		loadAccessibilityProperties(contentObject);
		contentObject.setId(null);
		contentObject.setSystemName(null);
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(expectedSystemName);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		Assert.assertEquals(contentObject.getSystemName(), expectedSystemName);

		expectedSystemName = "Inv{a}li[dC]ha! ra?ct^e&rs";
		loadAccessibilityProperties(contentObject);
		contentObject.setId(null);
		contentObject.setSystemName(null);
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(expectedSystemName);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		Assert.assertEquals(contentObject.getSystemName(), "Inv-a-li-dC-ha-ra-ct-e-rs");

		expectedSystemName = "Ελληνικά";
		loadAccessibilityProperties(contentObject);
		contentObject.setId(null);
		contentObject.setSystemName(null);
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(expectedSystemName);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		Assert.assertEquals(contentObject.getSystemName(), "Ellhnika");

		//Test system name is unique
		assertExceptionIsThrownForNonUniqueSystemName("Inv-a-li-dC-ha-ra-ct-e-rs");
		assertExceptionIsThrownForNonUniqueSystemName("inv-a-li-dc-ha-ra-ct-E-rs");
		assertExceptionIsThrownForNonUniqueSystemName("EllhnikA");
		assertExceptionIsThrownForNonUniqueSystemName("CHANGED");

		
		//Create a content object whose system name contains underscore
		ContentObject contentObject2 = createContentObject(repositoryUserService.getSystemRepositoryUser(), "testunder_score", false);
		contentObject2 = contentService.save(contentObject2, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject2);
		
		expectedSystemName = "testunder";
		loadAccessibilityProperties(contentObject);
		contentObject.setId(null);
		contentObject.setSystemName(null);
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(expectedSystemName);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		Assert.assertEquals(contentObject.getSystemName(), "testunder");
		
		assertExceptionIsThrownForNonUniqueSystemName("testUNder_SCORE");
		assertExceptionIsThrownForNonUniqueSystemName("TeStunDER");


	}

	private void loadAccessibilityProperties(ContentObject contentObject) {
		contentObject.getCmsProperty("accessibility.canBeReadBy");
		contentObject.getCmsProperty("accessibility.canBeDeletedBy");
		contentObject.getCmsProperty("accessibility.canBeTaggedBy");
		contentObject.getCmsProperty("accessibility.canBeUpdatedBy");
		
		contentObject.getCmsProperty("profile.language");
	}

	private void assertExceptionIsThrownForNonUniqueSystemName(String systemName) {
		
		ContentObject anotherContentObject = createContentObject(repositoryUserService.getSystemRepositoryUser(), systemName, false);
		
	
		try{
			TestLogPolicy.setLevelForLogger(Level.FATAL, PopulateContentObject.class.getName());
			
			anotherContentObject = contentService.save(anotherContentObject, false, true, null);
			
			//Object was saved. Check if it was saved with the alternative value
			String alternativeSystemName = TestUtils.createValidSystemName(((StringProperty)anotherContentObject.getCmsProperty("profile.title")).getSimpleTypeValue()+"-"+anotherContentObject.getId());
			
			Assert.assertEquals(anotherContentObject.getSystemName(), alternativeSystemName, "A new contentObject was saved with existing system name "+systemName);
			
			TestLogPolicy.setDefaultLevelForLogger(PopulateContentObject.class.getName());
		}
		catch(Exception e){
			//Retrieve the exception cause
			Throwable t = e;
			while (t != null){
				if (t.getCause() == null || t == t.getCause()){
					break;
				}
				else{
					t = t.getCause();
				}
				
			}
			
			Assert.assertEquals("Another content object exists with system name "+ anotherContentObject.getSystemName(), t.getMessage());
		}
	}

	private void checkInvalidSystemNameSave(ContentObject contentObject,
			String systemName) {

		try{
			contentObject.setSystemName(systemName);

			contentObject = contentService.save(contentObject, false, true, null);


			Assert.assertEquals(1, 2, 
					"Content object was saved with invalid system name "+systemName);

		}
		catch(CmsException e){

			String message = e.getMessage();

			Throwable t = e;

			while (t.getCause() != null){
				message = t.getCause().getMessage();

				t = t.getCause();
			}

			Assert.assertEquals(message, "Content Object system name "+systemName+" is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP, 
					"Invalid exception "+ e.getMessage());
		}
	}

	private void checkValidSystemNameSave(ContentObject contentObject,
			String systemName) {

		contentObject.setSystemName(systemName);

		contentObject = contentService.save(contentObject, false, true, null);

	}


	//@Test
	public void testOffsetAndLimitInSearchCriteria(){

		RepositoryUser systemUser = getSystemUser();

		for (int i=0;i<10;i++){

			ContentObject contentObject = createContentObject(systemUser,"testOffsetAndLmit"+i, true);

			((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.published.toString());

			((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("testOffsetAndLmit");
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		}


		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.title", "testOffsetAndLmit"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);


		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 0,0,10,0);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 0,-1,10, 10);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 3,4,10,4);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 10,0,10, 0);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 9,1,10,1);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 2,5,10,5);
		assertTotalCountForOffsetAndLimit(contentObjectCriteria, 4,0,10,0);
	}


	private void assertTotalCountForOffsetAndLimit(
			ContentObjectCriteria contentObjectCriteria, int offset, int limit, int totalCount, 
			int numberOfResultsRendered) {

		contentObjectCriteria.setOffsetAndLimit(offset, limit);

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertEquals(outcome.getCount(), totalCount, "Total count invalid for offset "+offset + " and limit "+ limit);

		Assert.assertEquals(outcome.getResults().size(), numberOfResultsRendered, "Results rendered" +
				" count invalid for offset "+offset + " and limit "+ limit);
	}


	//@Test
	public void testEmptyStringPropertyIsNotSaved() throws Exception{

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, TEST_CONTENT_TYPE, true);

		//Provide empty value for string
		((StringProperty)contentObject.getCmsProperty("simpleString")).setSimpleTypeValue("");

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);


		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());


		Assert.assertFalse(contentObjectNode.hasProperty("simpleString"), "Property simpleString has been saved to JCr even though it is an empty string");

		((StringProperty)contentObject.getCmsProperty("simpleStringMultiple")).addSimpleTypeValue("");
		((StringProperty)contentObject.getCmsProperty("simpleStringMultiple")).addSimpleTypeValue("     ");
		((StringProperty)contentObject.getCmsProperty("simpleStringMultiple")).addSimpleTypeValue("     ");
		((StringProperty)contentObject.getCmsProperty("simpleStringMultiple")).addSimpleTypeValue(null);

		contentObject = contentService.save(contentObject, false, true, null);

		contentObjectNode = getSession().getNodeByUUID(contentObject.getId());

		Assert.assertFalse(contentObjectNode.hasProperty("simpleStringMultiple"), "Property simpleStringMultiple " +
		"has been saved to JCr even though its values is a list of blank strings");


	}


	//@Test
	public void testTopicSearchUsingCriteria(){

		RepositoryUser systemUser = getSystemUser();

		final String taxonomyName = "newTaxonomyForTopicPropertyCriterionTest";
		
		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy(taxonomyName, 
				cmsRepositoryEntityFactory.newTaxonomy());
		
		newTaxonomy = taxonomyService.save(newTaxonomy);
		
		addEntityToBeDeletedAfterTestIsFinished(newTaxonomy);

		Topic topic = JAXBTestUtils.createTopic("firstTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildOfFirstTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.addChild(childTopic1);
		
		topic.setTaxonomy(newTaxonomy);
		
		topic = topicService.save(topic);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		
		Topic childSecondTopic = JAXBTestUtils.createTopic("firstChildOfSecondTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		
		secondTopic.addChild(childSecondTopic);
		
		secondTopic.setTaxonomy(taxonomyService.getBuiltInSubjectTaxonomy("en"));
		
		secondTopic = topicService.save(secondTopic);
		
		ContentObject contentObject = createContentObject(systemUser,"testTopicPropertyCriterion", false);

		((TopicProperty)contentObject.getCmsProperty("testTopic")).addSimpleTypeValue(topic);
		((TopicProperty)contentObject.getCmsProperty("simpleTopic")).setSimpleTypeValue(childTopic1);
			
		((TopicProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(secondTopic);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		String systemName = contentObject.getSystemName();

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);

		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("testTopic", topic.getId(), QueryOperator.EQUALS, false));
		assertResult(contentObjectCriteria, 1,systemName);
		
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("simpleTopic", topic.getId(), QueryOperator.EQUALS, true));
		assertResult(contentObjectCriteria, 1, systemName);
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.equals("testTopic", topic.getId()));
		assertResult(contentObjectCriteria, 1,systemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.equals("simpleTopic", childTopic1.getId()));
		assertResult(contentObjectCriteria, 1,systemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.subject", secondTopic.getId()));
		assertResult(contentObjectCriteria, 1,systemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, secondTopic.getId(), QueryOperator.EQUALS, true));
		assertResult(contentObjectCriteria, 1,systemName);
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addProfileSubjectIdCriterion(QueryOperator.EQUALS, secondTopic.getId(), true);
		assertResult(contentObjectCriteria, 1,systemName);
		
	}
	
	//@Test
	public void testSearchWithTopicReferenceCriterion(){
		
		//Create content
		RepositoryUser systemUser = getSystemUser();

		final String taxonomyName = "newTaxonomyForTopicJoinCriterionTest";
		
		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy(taxonomyName,cmsRepositoryEntityFactory.newTaxonomy());
		newTaxonomy = taxonomyService.saveTaxonomy(newTaxonomy);
		addEntityToBeDeletedAfterTestIsFinished(newTaxonomy);

		Topic topic = JAXBTestUtils.createTopic("firstJoinTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildOfFirstJoinTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		topic.addChild(childTopic1);
		topic.setTaxonomy(newTaxonomy);
		topic = topicService.saveTopic(topic);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondJoinTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		
		Topic childSecondTopic = JAXBTestUtils.createTopic("firstChildOfSecondJoinTopic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),systemUser);
		secondTopic.addChild(childSecondTopic);
		secondTopic.setTaxonomy(taxonomyService.getBuiltInSubjectTaxonomy("en"));
		secondTopic = topicService.saveTopic(secondTopic);
		
		ContentObject contentObject = createContentObject(systemUser,"testTopicJoinCriterion", false);

		((TopicProperty)contentObject.getCmsProperty("testTopic")).addSimpleTypeValue(topic);
		((TopicProperty)contentObject.getCmsProperty("simpleTopic")).setSimpleTypeValue(childTopic1);
		((TopicProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(childSecondTopic);
		((TopicProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(childTopic1);

		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);	
		
		String systemName = contentObject.getSystemName();

		//Assert search by using topic names instead of id and
		//by using both TopicPropertyCriterion and simple criterion
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "testTopic",topic,systemName, false);
		
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "simpleTopic",topic,systemName, true);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "simpleTopic",childTopic1,systemName, false);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "simpleTopic",childTopic1,systemName, true);

		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",secondTopic,systemName, true);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",childSecondTopic,systemName, false);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",childSecondTopic,systemName, true);

		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",topic,systemName, true);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",childTopic1,systemName, false);
		createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(contentObjectCriteria, "profile.subject",childTopic1,systemName, true);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.subject", CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX+childTopic1.getName()));
		contentObjectCriteria.addCriterion(CriterionFactory.equals("simpleTopic", CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX+childTopic1.getName()));
		contentObjectCriteria.addCriterion(CriterionFactory.equals("testTopic", CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX+topic.getName()));
		assertResult(contentObjectCriteria, 1,systemName);
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("testTopic", topic.getName(), QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("simpleTopic", childTopic1.getName(), QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("profile.subject", childTopic1.getName(), QueryOperator.EQUALS, false));
		assertResult(contentObjectCriteria, 1, systemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("testTopic", topic.getId(), QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("simpleTopic", childTopic1.getId(), QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("profile.subject", childTopic1.getId(), QueryOperator.EQUALS, false));
		assertResult(contentObjectCriteria, 1, systemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("testTopic", topic, QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("simpleTopic", childTopic1, QueryOperator.EQUALS, false));
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion("profile.subject", childTopic1, QueryOperator.EQUALS, false));
		assertResult(contentObjectCriteria, 1, systemName);


	}
	
	private void createAllPossibleTopicReferenceCriterionForTopicAndAssertResult(ContentObjectCriteria contentObjectCriteria, 
			String propertyPath, Topic topic, String contentObjectSystemName, boolean includeSubTopics){
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topic.getName(), QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topic.getName(), QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topic.getId(), QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topic.getId(), QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topic, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topic, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		List topics = new ArrayList();
		
		contentObjectCriteria.reset();
		topics.add(topic);
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic);
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic);
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 0, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic);
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getId());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getId());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getId());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 0, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getId());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getName());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getName());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(propertyPath, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getName());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.OR, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 1, contentObjectSystemName);

		contentObjectCriteria.reset();
		topics.clear();
		topics.add(topic.getName());
		contentObjectCriteria.addCriterion(CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.AND, QueryOperator.EQUALS, includeSubTopics));
		assertResult(contentObjectCriteria, 0, contentObjectSystemName);

		if (!includeSubTopics){
			
			contentObjectCriteria.reset();
			contentObjectCriteria.addCriterion(CriterionFactory.equals(propertyPath, CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX+topic.getName()));
			assertResult(contentObjectCriteria, 1,contentObjectSystemName);
			
			contentObjectCriteria.reset();
			contentObjectCriteria.addCriterion(CriterionFactory.equals(propertyPath, topic.getId()));
			assertResult(contentObjectCriteria, 1,contentObjectSystemName);

		}
		
	}
	
	//@Test
	public void testSearchContentObjectsOrderByChildProperty(){
		
		RepositoryUser systemUser = getSystemUser();

		char[] letters = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t'};
		
		//We deliberately set the same number in various positions
		long[] numbers = new long[]{1,2,3,3,5,6,7,8,9,10,10,11,12,12,14,15,16,16,19,20};
		
		List<Integer> indeces = new ArrayList<Integer>();
		for (int i=0;i<20;i++){
			indeces.add(i);
		}
		
		Collections.shuffle(indeces);
		
		//Create content objects
		for (Integer i: indeces){

			ContentObject contentObject = createContentObject(systemUser,"testSearchContentObjectsOrderByChildProperty"+i, true);

			((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.published.toString());

			((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(letters[i]+"testSearchContentObjectsOrderByChildProperty");
			
			((StringProperty)contentObject.getCmsProperty("simpleString")).setSimpleTypeValue(letters[i]+"testSearchContentObjectsOrderByChildProperty");
			
			((StringProperty)contentObject.getCmsProperty("comment.body")).setSimpleTypeValue(letters[i]+"testSearchContentObjectsOrderByChildProperty");
			((StringProperty)contentObject.getCmsProperty("comment.comment.body")).setSimpleTypeValue(letters[i]+"testSearchContentObjectsOrderByChildProperty");
			
			((LongProperty)contentObject.getCmsProperty("allPropertyTypeContainer.simpleLong")).setSimpleTypeValue(numbers[i]);
			
			//Default values will be loaded
			contentObject.getCmsProperty("stringEnum");
			contentObject.getCmsProperty("longEnum");
			
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		}

		assertOrderedResultsWithOnlyOneChildProperty(0,20, letters, numbers);
		assertOrderedResultsWithOnlyOneChildProperty(0,10, letters, numbers);
		assertOrderedResultsWithOnlyOneChildProperty(3,3,letters, numbers);
		assertOrderedResultsWithOnlyOneChildProperty(10,20, letters, numbers);
		assertOrderedResultsWithOnlyOneChildProperty(19,20, letters, numbers);
		assertOrderedResultsWithOnlyOneChildProperty(10,50, letters, numbers);

		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(0,20, letters, numbers);
		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(0,10, letters, numbers);
		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(3,3,letters, numbers);
		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(10,20, letters, numbers);
		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(19,20, letters, numbers);
		assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(10,50, letters, numbers);
		
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(0,20, letters, numbers);
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(0,10, letters, numbers);
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(3,3,letters, numbers);
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(10,20, letters, numbers);
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(19,20, letters, numbers);
		assertOrderedResultsWithThreeChildPropertiesInOrderBy(10,50, letters, numbers);


	}

	private void assertOrderedResultsWithOnlyOneChildProperty(int offset, int limit, char[] letters, long[] numbers) {
		
		
		for (Order order : Order.values()){
			
			//Property simpleString
			ContentObjectCriteria contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("simpleString", order);
			
			long start = System.currentTimeMillis();
			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property simpleString and Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				char letter = order == Order.ascending ? letters[offset+i] : letters[letters.length-i-offset-1];
				
				final String simpleString = ((StringProperty)contentObject.getCmsProperty("simpleString")).getSimpleTypeValue();
				
				logger.debug("SimpleString: {}", simpleString );

				Assert.assertEquals(simpleString, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);
			}
			
			//Property profile.title
			contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("profile.title", order);
			
			start = System.currentTimeMillis();
			outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property profile.title and Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				char letter = order == Order.ascending ? letters[offset+i] : letters[letters.length-i-offset-1];
				
				final String title = ((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue();
				
				logger.debug("Title: {}", title );

				Assert.assertEquals(title, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);
			}
			
			//Property allPropertyTypeContainer.simpleLong
			contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("allPropertyTypeContainer.simpleLong", order);
			
			start = System.currentTimeMillis();
			outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property allPropertyTypeContainer.simpleLong and Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				long number = order == Order.ascending ? numbers[offset+i] : numbers[numbers.length-i-offset-1];
				
				final long simpleLong = ((LongProperty)contentObject.getCmsProperty("allPropertyTypeContainer.simpleLong")).getSimpleTypeValue();
				
				
				logger.debug("allPropertyTypeContainer.simpleLong: {}", simpleLong );

				Assert.assertEquals(simpleLong, 
						number, "offset "+offset +" limit "+limit + " order "+order + " index "+i + " number chosen "+ number);
			}
			
			
			//Property comment.comment.body
			contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("comment.comment.body", order);
			
			start = System.currentTimeMillis();
			outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property comment.comment.body and Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				char letter = order == Order.ascending ? letters[offset+i] : letters[letters.length-i-offset-1];
				
				final String comment = ((StringProperty)contentObject.getCmsProperty("comment.comment.body")).getSimpleTypeValue();
				
				logger.debug("comment.comment.body: {}", comment );

				Assert.assertEquals(comment, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);
			}
		}
		
	}

	
	private void assertOrderedResultsWithThreeChildPropertiesInOrderBy(int offset, int limit, char[] letters, long[] numbers) {
		
		
		for (Order order : Order.values()){
			
			ContentObjectCriteria contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("allPropertyTypeContainer.simpleLong", order);
			contentObjectCriteria.addOrderProperty("profile.title", order);
			contentObjectCriteria.addOrderProperty("comment.comment.body", order);
			
			long start = System.currentTimeMillis();
			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property profile.title and allPropertyTypeContainer.simpleLong and comment.comment.body and  Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				char letter = order == Order.ascending ? letters[offset+i] : letters[letters.length-i-offset-1];
				long number = order == Order.ascending ? numbers[offset+i] : numbers[numbers.length-i-offset-1];
				
				final String title = ((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue();
				final long simpleLong = ((LongProperty)contentObject.getCmsProperty("allPropertyTypeContainer.simpleLong")).getSimpleTypeValue().longValue();
				final String comment = ((StringProperty)contentObject.getCmsProperty("comment.comment.body")).getSimpleTypeValue();
				
				
				logger.debug("Number: {}, Title: {}, Comment: {}", new Object[]{ number, title, comment} );
				
				Assert.assertEquals(title, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);
				
				Assert.assertEquals(simpleLong, 
						number, "offset "+offset +" limit "+limit + " order "+order + " index "+i + " number chosen "+ number);
				
				Assert.assertEquals(comment, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);

			}
		}
		
	}
	
	private void  assertOrderedResultsWithOnlyTwoChildPropertiesInOrderBy(int offset, int limit, char[] letters, long[] numbers) {
		
		
		for (Order order : Order.values()){
			
			ContentObjectCriteria contentObjectCriteria = createContentObjectCriteriaForSearchUsingOrderByChildProperty(offset, limit, order);
			contentObjectCriteria.addOrderProperty("allPropertyTypeContainer.simpleLong", order);
			contentObjectCriteria.addOrderProperty("profile.title", order);
			
			long start = System.currentTimeMillis();
			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			logger.debug("Search with order by child property profile.title and allPropertyTypeContainer.simpleLong and Offset {} Limit {} Order {}, took {} ms", 
					new Object[]{offset, limit, order, (System.currentTimeMillis()-start)});

			Assert.assertNotNull(outcome, "Method testSearchContentObjectsOrderByChildProperty did not return any outcome in query for offset "+offset +" limit "+limit + " order "+order);
			Assert.assertTrue(outcome.getResults()!=null && outcome.getResults().size() > 0, "Method testSearchContentObjectsOrderByChildProperty did not return any results in query for offset "+offset +" limit "+limit + " order "+order);
			
			
			for (int i=0; i<=outcome.getResults().size()-1; i++){
				
				ContentObject contentObject = outcome.getResults().get(i);
				
				char letter = order == Order.ascending ? letters[offset+i] : letters[letters.length-i-offset-1];
				long number = order == Order.ascending ? numbers[offset+i] : numbers[numbers.length-i-offset-1];
				
				final String title = ((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue();
				final long simpleLong = ((LongProperty)contentObject.getCmsProperty("allPropertyTypeContainer.simpleLong")).getSimpleTypeValue().longValue();
				
				logger.debug("Number: {}, Title: {}", number, title );
				
				Assert.assertEquals(title, 
						letter+"testSearchContentObjectsOrderByChildProperty", "offset "+offset +" limit "+limit + " order "+order + " index "+i + " letter chosen "+ letter);
				
				Assert.assertEquals(simpleLong, 
						number, "offset "+offset +" limit "+limit + " order "+order + " index "+i + " number chosen "+ number);
			}
		}
		
	}

	private ContentObjectCriteria createContentObjectCriteriaForSearchUsingOrderByChildProperty(int offset, int limit, Order order) {
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addSystemNameContainsCriterion("testSearchContentObjectsOrderByChildProperty*");
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.setOffsetAndLimit(offset, limit);
		
		return contentObjectCriteria;
	}
	
	//@Test
	public void testSearchWithContentObjectReferenceCriterion(){
		
		RepositoryUser systemUser = getSystemUser();
		
		//Create content
		ContentObject mainContentObject = createAndPublishSpecificContentObject(systemUser,"testContentObjectJoinInSearchMainContentObject", TEST_CONTENT_TYPE);
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		//Create content object references
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObject", "testContentObjectJoinInSearchForSimpleContentObjectPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObject", "testContentObjectJoinInSearchForSimpleContentObjectPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObject", "testContentObjectJoinInSearchForSimpleContentObjectPropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObjectMultiple", "testContentObjectJoinInSearchForSimpleContentObjectMultiplePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObjectMultiple", "testContentObjectJoinInSearchForSimpleContentObjectMultiplePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "simpleContentObjectMultiple", "testContentObjectJoinInSearchForSimpleContentObjectMultiplePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);


		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectPropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectMultiplePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectMultiplePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerSimpleContentObjectMultiplePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);


		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObject", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectPropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectMultiplePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectMultiplePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].simpleContentObjectMultiple", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSimpleContentObjectMultiplePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestTypePropertyReferenceOfType"+DIRECT_EXTENDED_TEST_CONTENT_TYPE, DIRECT_EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);


		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleSingleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);

		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+TEST_CONTENT_TYPE, TEST_CONTENT_TYPE);
		createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(systemUser, mainContentObject, contentObjectCriteria, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", "testContentObjectJoinInSearchForAllPropertyTypeContainerMultipleMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestPropertyReferenceOfType"+EXTENDED_TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);


	}

	private void createContentObjectReferenceForPropertyAndAssertContentObjectReferenceCriterionInSearch(
			RepositoryUser systemUser, ContentObject mainContentObject,
			ContentObjectCriteria contentObjectCriteria, String propertyPath, String referenceSystemName, String referenceType) {
		
		ContentObject contentObjectUsedForReference = createAndPublishSpecificContentObject(systemUser,referenceSystemName, referenceType);
		
		((ContentObjectProperty)mainContentObject.getCmsProperty(propertyPath)).addSimpleTypeValue(contentObjectUsedForReference);
		contentService.saveContentObject(mainContentObject, false);

		String propertyPathWithNoIndeces = propertyPath.replaceAll("\\[.*\\]", "");

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.equals(propertyPathWithNoIndeces, CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX+referenceSystemName));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, contentObjectUsedForReference, QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, contentObjectUsedForReference.getId(), QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, contentObjectUsedForReference.getSystemName(), QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, Arrays.asList(contentObjectUsedForReference), Condition.AND, QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, Arrays.asList(contentObjectUsedForReference.getId()), Condition.AND, QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(CriterionFactory.newContentObjectReferenceCriterion(propertyPathWithNoIndeces, Arrays.asList(contentObjectUsedForReference.getSystemName()), Condition.AND, QueryOperator.EQUALS));
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		ContentObjectReferenceCriterion contentObjectReferenceCriterion = new ContentObjectReferenceCritetionImpl();
		List values = new ArrayList();
		
		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(contentObjectReferenceCriterion);
		contentObjectReferenceCriterion.setProperty(propertyPathWithNoIndeces);
		contentObjectReferenceCriterion.addValue(contentObjectUsedForReference.getId());
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());


		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(contentObjectReferenceCriterion);
		contentObjectReferenceCriterion.clearValues();
		contentObjectReferenceCriterion.addValue(CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX+contentObjectUsedForReference.getSystemName());
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(contentObjectReferenceCriterion);
		contentObjectReferenceCriterion.clearValues();
		values.clear();
		values.add(contentObjectUsedForReference.getId());
		contentObjectReferenceCriterion.setValues(values);
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

		contentObjectCriteria.reset();
		contentObjectCriteria.addCriterion(contentObjectReferenceCriterion);
		contentObjectReferenceCriterion.clearValues();
		values.clear();
		values.add(CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX+contentObjectUsedForReference.getSystemName());
		contentObjectReferenceCriterion.setValues(values);
		assertResult(contentObjectCriteria, 1, mainContentObject.getSystemName());

	}

	private ContentObject createAndPublishSpecificContentObject(RepositoryUser systemUser, String systemName, String type) {
		
		ContentObject contentObject = createContentObjectForType(type,systemUser,systemName, false);

		((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.published.toString());
		
		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		return contentObject;
	}
	
	//@Test
	public void testSearchContentObjects() throws Throwable{
 
		RepositoryUser systemUser = getSystemUser();

		for (int i=0;i<10;i++){

			ContentObject contentObject = createContentObject(systemUser,"testSearchContentObjects"+i, true);

			((StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(ContentObjectStatus.published.toString());

			((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("testSearchContentObjects");
			//Default values will be loaded
			contentObject.getCmsProperty("stringEnum");
			contentObject.getCmsProperty("longEnum");
			
			
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		}


		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.title", "testSearchContentObjects"));
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		contentObjectCriteria.addOrderProperty("profile.title", Order.ascending);
		
		//Export to XML as String
		long time = System.currentTimeMillis();
		String xml = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
		logger.debug("Brought 20 content objects in XML string in {}", (System.currentTimeMillis() - time) +" ms");
		jaxbValidationUtils.validateUsingSAX(xml);

		//Export to XML as String but use the same name for all content object. Do not validate it
		contentObjectCriteria.getRenderProperties().serializeContentObjectsUsingTheSameNameAsCollectionItemName(true);
		String xmlWithSameElementNameForContentObjects = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
		logger.debug("XML with same content object names \n {}", TestUtils.prettyPrintXml(xmlWithSameElementNameForContentObjects));
		
		//Run query again and for each returned content object compare xml export with
		//xml provided by appropriate method
		contentObjectCriteria.getRenderProperties().serializeContentObjectsUsingTheSameNameAsCollectionItemName(false);
		contentObjectCriteria.getRenderProperties().renderParentEntity(true);
		
		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		for (ContentObject co : outcome.getResults()){
			
			ImportMode importModeForLog = null;
			for (ImportMode importMode : ImportMode.values()){
				
				importModeForLog = importMode;
				
				String coXml = co.xml(prettyPrint);

				ContentObject coFromXml = importDao.importContentObject(coXml, false, true, importMode);

				String coXmlFromServiceWithId = contentService.getContentObject(co.getId(), ResourceRepresentationType.XML, FetchLevel.FULL, 
						CacheRegion.NONE, null, false);

				ContentObject coFromServiceWithId = importDao.importContentObject(coXmlFromServiceWithId, false, false, importMode);

				try{
					repositoryContentValidator.compareContentObjects(coFromXml, coFromServiceWithId, true);
				}			
				catch(Throwable e){
					
					logger.error("ImportMode \n{}",importModeForLog);
					logger.error("Initial \n{}",TestUtils.prettyPrintXml(coXml));
					logger.error("Using Id \n{}",TestUtils.prettyPrintXml(coXmlFromServiceWithId));
					throw e;
				}
				
				if (ImportMode.DO_NOT_SAVE != importMode){
					co = coFromServiceWithId;
				}
			}

		}
		
	}

	//@Test
	public void testBinaryChannelURLs() throws Exception{
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "imageURLs", true);

		BinaryChannel logoBinaryChannel = loadManagedBinaryChannel(logo, "image");
		BinaryChannel logo2BinaryChannel = loadManagedBinaryChannel(logo2, "image");

		//Add two binary channels in property image
		BinaryProperty imageProperty = (BinaryProperty)contentObject.getCmsProperty("image");
		
		imageProperty.addSimpleTypeValue(logoBinaryChannel);
		imageProperty.addSimpleTypeValue(logo2BinaryChannel);

		contentObject = contentService.save(contentObject, false, true, null);

		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		//Check that existent instances have been updated with repository and host parameters
		assertBinaryChannelURLs(logoBinaryChannel, imageProperty.getPermanentPath(), contentObject);

		assertBinaryChannelURLs(logo2BinaryChannel, imageProperty.getPermanentPath(), contentObject);

		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		imageProperty = (BinaryProperty)contentObjectReloaded.getCmsProperty("image");

		Assert.assertTrue(imageProperty.hasValues(), "No binary channel saved for image property");
		Assert.assertTrue(imageProperty.getSimpleTypeValues().size()==2, "Should have saved 2 binary channels for image property");


		for (BinaryChannel imageBinaryChannel : imageProperty.getSimpleTypeValues()){
			final int index = imageProperty.getSimpleTypeValues().indexOf(imageBinaryChannel);
			
			String indexAsString = index == 0 ? "" : "["+index+"]";
			
			//Check that existent instances have been updated with repository and host parameters
			assertBinaryChannelURLs(imageBinaryChannel, imageProperty.getPermanentPath(), contentObjectReloaded);
		}
		
		//Add another image
		BinaryChannel logo3BinaryChannel = loadManagedBinaryChannel(logo2, "image");
		imageProperty.addSimpleTypeValue(logo3BinaryChannel);
		
		contentObject = contentService.save(contentObjectReloaded, false, true, null);
		
		//Now recheck urls
		assertBinaryChannelURLs(logo3BinaryChannel, imageProperty.getPermanentPath(), contentObject);

		//remove the first image
		contentObjectReloaded = contentService.getContentObjectById(contentObject.getId(), null);

		imageProperty = (BinaryProperty)contentObjectReloaded.getCmsProperty("image");
		
		imageProperty.removeSimpleTypeValue(0);
		contentObjectReloaded = contentService.saveContentObject(contentObjectReloaded, false);
		imageProperty = (BinaryProperty)contentObjectReloaded.getCmsProperty("image");
		
		for (BinaryChannel imageBinaryChannel : imageProperty.getSimpleTypeValues()){
			final int index = imageProperty.getSimpleTypeValues().indexOf(imageBinaryChannel);
			
			String indexAsString = index == 0 ? "" : "["+index+"]";

			//Check that existent instances have been updated with repository and host parameters
			assertBinaryChannelURLs(imageBinaryChannel, imageProperty.getPermanentPath(), contentObjectReloaded);
		}

		
	}

		
	private void assertBinaryChannelURLs(BinaryChannel binaryChannel, String binaryPropertyPermanentPath, ContentObject contentObject) {
		String contentApiURL = AstroboaClientContextHolder.getActiveCmsRepository().getRestfulApiBasePath();
		String serverBaseURL = AstroboaClientContextHolder.getActiveCmsRepository().getServerURL();
		
		final String expectedBaseUrl = serverBaseURL + contentApiURL+"/"+TestConstants.TEST_REPOSITORY_ID+"/contentObject/";
		final String expectedRelativeBaseUrl =  contentApiURL+"/"+TestConstants.TEST_REPOSITORY_ID+"/contentObject/";

		Assert.assertEquals(binaryChannel.getContentApiURL(), expectedBaseUrl+contentObject.getSystemName()+ "/"+binaryPropertyPermanentPath, "Invalid content api URL");

		Assert.assertEquals(binaryChannel.buildResourceApiURL(null, null, null, null, null, true, false), expectedBaseUrl+contentObject.getSystemName()+ "/"+binaryPropertyPermanentPath+ "["+binaryChannel.getId()+"]", "Invalid resource api URL");
		Assert.assertEquals(binaryChannel.buildResourceApiURL(null, null, null, null, null, false, false), expectedBaseUrl+contentObject.getId()+ "/"+binaryPropertyPermanentPath+ "["+binaryChannel.getId()+"]", "Invalid resource api URL");
		Assert.assertEquals(binaryChannel.buildResourceApiURL(null, null, null, null, null, false, true), expectedRelativeBaseUrl+contentObject.getId()+ "/"+binaryPropertyPermanentPath+ "["+binaryChannel.getId()+"]", "Invalid resource api URL");
		Assert.assertEquals(binaryChannel.buildResourceApiURL(null, null, null, null, null, true, true), expectedRelativeBaseUrl+contentObject.getSystemName()+ "/"+binaryPropertyPermanentPath+ "["+binaryChannel.getId()+"]", "Invalid resource api URL");
		
	}
	
	private void assertResult(ContentObjectCriteria contentObjectCriteria, int totalCount, String expectedContentObjectName) {

		contentObjectCriteria.setOffsetAndLimit(0, 1);

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertEquals(outcome.getCount(), totalCount, "Total count invalid. Query "+ contentObjectCriteria.getXPathQuery());
		
		if (totalCount > 0){
			Assert.assertEquals(outcome.getResults().get(0).getSystemName(), expectedContentObjectName, "Invalid content object name. Query "+
					contentObjectCriteria.getXPathQuery());
		}

	}

	//@Test
	public void testSaveOfContentObjectPropertyWhichAcceptsAnyContentObjectOfSomeType(){

		//Create main content object
		ContentObject mainContentObject = createContentObjectForType(TEST_CONTENT_TYPE, getSystemUser(), "mainContentObject", false);

		mainContentObject = contentService.saveContentObject(mainContentObject, false);
		
		addEntityToBeDeletedAfterTestIsFinished(mainContentObject);
		
		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestType",false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", false);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTest",true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestAndExtendedTest",false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

		assertSaveOfContentObjectReference(mainContentObject, TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", false);
		assertSaveOfContentObjectReference(mainContentObject, DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);


		assertSaveOfContentObjectReference(mainContentObject, "personObject", "referenceOfAnyContentObjectOfTypeTestType", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "multipleReferenceOfAnyContentObjectOfTypeTestType", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestType", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestType", true);

		assertSaveOfContentObjectReference(mainContentObject, "personObject", "referenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "multipleReferenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTest", true);

		assertSaveOfContentObjectReference(mainContentObject, "personObject", "referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].referenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);
		assertSaveOfContentObjectReference(mainContentObject, "personObject", "allPropertyTypeContainerMultiple[0].multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", true);

	}
	
	private void assertSaveOfContentObjectReference(ContentObject mainContentObject, String contentTypeOfContentObjectReference, String propertyPath, boolean shouldThrowException ){
		
		ContentObject contentObject = createContentObjectForType(contentTypeOfContentObjectReference, getSystemUser(), "contentObjectOfType"+contentTypeOfContentObjectReference+propertyPath.replaceAll("\\[","").replaceAll("\\]",""), false);

		contentObject = contentService.saveContentObject(contentObject, false);
		
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		//Save reference.
		ContentObjectProperty contentObjectProperty = (ContentObjectProperty)mainContentObject.getCmsProperty(propertyPath);

		try{
			contentObjectProperty.addSimpleTypeValue(contentObject);
			contentService.saveContentObject(mainContentObject, false);
		}
		catch(CmsException e){
			if (! shouldThrowException){
				throw e;
			}
			else{
				Assert.assertEquals(e.getMessage(),  
						"ContentObject "+contentObject.getId()+" is of type "+contentObject.getContentObjectType()+
						", property "+contentObjectProperty.getFullPath()+" accepts contentObject of the following types :'"
						+contentObjectProperty.getPropertyDefinition().getExpandedAcceptedContentTypes()+
						"' as values");
				
				if (contentObjectProperty.getPropertyDefinition().isMultiple()){
					contentObjectProperty.removeSimpleTypeValue(contentObjectProperty.getSimpleTypeValues().size()-1);
				}
				else{
					contentObjectProperty.removeSimpleTypeValue(0);
				}
			}
		}

	}
	
	//@Test
	public void testGetContentObjectPreFetchedPropertyMechanism() throws Throwable{
		
		ContentObject contentObject =  createContentObjectAndPopulateAllProperties(getSystemUser(), "contentObjectTestExportPreFetchMechanism", false);
		
		((LongProperty)contentObject.getCmsProperty("statisticTypeMultiple.viewCounter")).setSimpleTypeValue((long)1);
		((LongProperty)contentObject.getCmsProperty("statisticType.viewCounter")).setSimpleTypeValue((long)1);
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		List<String> propertyPaths = Arrays.asList("profile.created", "allPropertyTypeContainer");
		
		final ArrayList<String> emptyPropertiesToExportList = new ArrayList<String>();
		
		
		for (FetchLevel fetchLevel : FetchLevel.values()){
			
			try{
			//Resource Representation Type is ContentObject
			ContentObject contentObjectReloaded = (ContentObject) contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, fetchLevel, CacheRegion.NONE, propertyPaths,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,fetchLevel,propertyPaths);
			
			//Resource Representation Type is ContentObject and property paths to export is null
			contentObjectReloaded = (ContentObject) contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, fetchLevel, CacheRegion.NONE, null,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,fetchLevel,null);

			//Resource Representation Type is ContentObject and property paths to export is empty
			contentObjectReloaded = (ContentObject) contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, fetchLevel, CacheRegion.NONE, emptyPropertiesToExportList,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,fetchLevel,emptyPropertiesToExportList);

			//Resource Representation Type is CONTENT_OBJECT_LIST 
			CmsOutcome<ContentObject> outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, fetchLevel, CacheRegion.NONE, propertyPaths,false);
			assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObject, propertyPaths, fetchLevel, outcome);

			//Resource Representation Type is CONTENT_OBJECT_LIST and property paths to export is null
			outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, fetchLevel, CacheRegion.NONE, null,false);
			assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObject, null, fetchLevel, outcome);

			//Resource Representation Type is CONTENT_OBJECT_LIST and property paths to export is empty
			outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, fetchLevel, CacheRegion.NONE, emptyPropertiesToExportList,false);
			assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObject, emptyPropertiesToExportList, fetchLevel, outcome);

			//Resource Representation Type is null 
			contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, fetchLevel, CacheRegion.NONE, propertyPaths,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,contentObjectReloaded,fetchLevel,propertyPaths);

			//Resource Representation Type is null property paths to export is null 
			contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, fetchLevel, CacheRegion.NONE, null,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,contentObjectReloaded,fetchLevel,null);

			//Resource Representation Type is null and property paths to export is empty 
			contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, fetchLevel, CacheRegion.NONE, emptyPropertiesToExportList,false);
			assertContentObjectHasTheProperPropertiesLoaded(contentObject,contentObjectReloaded,fetchLevel,emptyPropertiesToExportList);

			//Resource Representation Type is XML 
			String xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, fetchLevel, CacheRegion.NONE, propertyPaths,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, fetchLevel, propertyPaths);

			//Resource Representation Type is XML and property paths to export is null
			xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, fetchLevel, CacheRegion.NONE, null,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, fetchLevel, null);

			//Resource Representation Type is XML and property paths to export is empty
			xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, fetchLevel, CacheRegion.NONE, emptyPropertiesToExportList,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, fetchLevel, emptyPropertiesToExportList);

			//Resource Representation Type is JSON 
			xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, fetchLevel, CacheRegion.NONE, propertyPaths,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, fetchLevel, propertyPaths);

			//Resource Representation Type is JSON  and property paths to export is null
			xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, fetchLevel, CacheRegion.NONE, null,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, fetchLevel, null);

			//Resource Representation Type is JSON and property paths to export is empty
			xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, fetchLevel, CacheRegion.NONE, emptyPropertiesToExportList,false);
			assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, fetchLevel, emptyPropertiesToExportList);

			}
			catch(Throwable e){
				logger.error("FetchLevel "+fetchLevel);
				throw e;
			}
		}
		
		/*
		 * 
		 */
		//Resource Representation Type is ContentObject and FetchLevel is null
		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, null, CacheRegion.NONE, propertyPaths,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null, propertyPaths);

		//Resource Representation Type is ContentObject , FetchLevel is null and propertyPathsToExport is null
		contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, null, CacheRegion.NONE, null,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null, null);

		//Resource Representation Type is ContentObject , FetchLevel is null and propertyPathsToExport is empty 
		contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, null, CacheRegion.NONE, emptyPropertiesToExportList,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null, emptyPropertiesToExportList);

		//Resource Representation Type is null and FetchLevel is null
		contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, null, CacheRegion.NONE, propertyPaths,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null,propertyPaths);

		//Resource Representation Type is null and FetchLevel is null  and propertyPathsToExport is null 
		contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, null, CacheRegion.NONE, null,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null,null);

		//Resource Representation Type is null and FetchLevel is null  and propertyPathsToExport is empty  
		contentObjectReloaded = contentService.getContentObject(contentObject.getId(), null, null, CacheRegion.NONE, emptyPropertiesToExportList,false);
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,null,emptyPropertiesToExportList);

		//Resource Representation Type is CONTENT_OBJECT_LIST and FetchLevel is null
		CmsOutcome<ContentObject>  outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, null, CacheRegion.NONE, propertyPaths,false);
		assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObjectReloaded, propertyPaths, null, outcome);

		//Resource Representation Type is CONTENT_OBJECT_LIST and FetchLevel is null and property paths to export is null
		outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, null, CacheRegion.NONE, null,false);
		assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObjectReloaded, null, null, outcome);

		//Resource Representation Type is CONTENT_OBJECT_LIST and FetchLevel is null and property paths to export is empty
		outcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, null, CacheRegion.NONE, emptyPropertiesToExportList,false);
		assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(contentObjectReloaded, emptyPropertiesToExportList, null, outcome);

		//Resource Representation Type is XML and FetchLevel is null
		String xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, null, CacheRegion.NONE, propertyPaths,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, null, propertyPaths);

		//Resource Representation Type is XML and FetchLevel is null  and propertyPathsToExport is null
		xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, null, CacheRegion.NONE, null,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, null, null);

		//Resource Representation Type is XML and FetchLevel is null  and propertyPathsToExport is empty 
		xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, null, CacheRegion.NONE, emptyPropertiesToExportList,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.XML, null, emptyPropertiesToExportList);

		//Resource Representation Type is JSON and FetchLevel is null
		xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, null, CacheRegion.NONE, null,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, null, propertyPaths);

		//Resource Representation Type is JSON and FetchLevel is null  and propertyPathsToExport is null
		xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, null, CacheRegion.NONE, null,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, null, null);

		//Resource Representation Type is JSON and FetchLevel is null  and propertyPathsToExport is empty
		xmlOrJson = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, null, CacheRegion.NONE, emptyPropertiesToExportList,false);
		assertStringRepresentationHasTheProperPropertiesLoaded(xmlOrJson, contentObject, ResourceRepresentationType.JSON, null, emptyPropertiesToExportList);

	}

	private void assertThatCmsOutcomeContainsOneContentObjectWithProperPropertiesLoaded(
			ContentObject contentObject, List<String> propertyPaths,
			FetchLevel fetchLevel, CmsOutcome<ContentObject> outcome) {
		ContentObject contentObjectReloaded;
		Assert.assertNotNull(outcome, "ContentService.getContentObject returned null");
		Assert.assertEquals(outcome.getCount(), 1, "ContentService.getContentObject returned invalid count");
		Assert.assertEquals(outcome.getLimit(), 1, "ContentService.getContentObject returned invalid limit");
		Assert.assertEquals(outcome.getOffset(), 0, "ContentService.getContentObject returned invalid offset");
		Assert.assertEquals(outcome.getResults().size(), 1, "ContentService.getContentObject returned invalid number of ContentObjects");
		
		contentObjectReloaded = outcome.getResults().get(0);  
		assertContentObjectHasTheProperPropertiesLoaded(contentObject,	contentObjectReloaded,fetchLevel,propertyPaths);
	}

	private void assertStringRepresentationHasTheProperPropertiesLoaded(
			String xmlOrJson, ContentObject contentObject,
			ResourceRepresentationType<String> resourceRepresentationType, FetchLevel fetchLevel, List<String> propertiesToExport) throws Exception {
		
		xmlOrJson = removeWhitespacesIfNecessary(xmlOrJson);
		
		if (resourceRepresentationType.equals(ResourceRepresentationType.XML)){
			
			String expectedProperty = "cmsIdentifier=\""+contentObject.getId()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object identifier "+contentObject.getId());

			expectedProperty = "systemName=\""+contentObject.getSystemName()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object system name "+contentObject.getSystemName());

			expectedProperty = "contentObjectTypeName=\""+contentObject.getContentObjectType()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object type "+contentObject.getContentObjectType());

			expectedProperty = "url=\""+contentObject.getResourceApiURL(resourceRepresentationType,false)+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object url "+contentObject.getResourceApiURL(resourceRepresentationType,false));

			if (fetchLevel == null || fetchLevel == FetchLevel.ENTITY || 
					fetchLevel == FetchLevel.ENTITY_AND_CHILDREN && CollectionUtils.isEmpty(propertiesToExport)){
				
				expectedProperty = "<profile";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's profile ");
				
				expectedProperty = "<title>"+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"</title>";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's profile.title");

				expectedProperty = "<created>"+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"</created>";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's profile.created");

				expectedProperty = "<accessibility";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's accessibility");

				expectedProperty = "<canBeReadBy>"+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"</canBeReadBy>";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's accessibility.canBeReadBy");

				expectedProperty = "<owner";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object owner ");

				expectedProperty = "cmsIdentifier=\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "externalId=\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				//Since user is SYSTEM User , she has by default the label 'ACCOUNT SYSTEM'. If pretty print
				//has been enabled, then xmlOrJson will processed to remove all whitespaces
				//and therefore label wil be ACCOUNTSYSTEM. In this case we need to remove the whitespaces from the value
				//in contentObject.getOwner().getLabel()
				expectedProperty = "label=\""+removeWhitespacesIfNecessary(contentObject.getOwner().getLabel())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = "<"+PropertyPath.getLastDescendant(propertyName);
					Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object property "+propertyName);
				}
			}
			else if (fetchLevel == FetchLevel.FULL){
				expectedProperty = "<profile";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's profile ");
				
				expectedProperty = "<title>"+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"</title>";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's profile.title");
				
				expectedProperty = "<created>"+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"</created>";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's profile.created");

				expectedProperty = "<accessibility";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's accessibility");

				expectedProperty = "<canBeReadBy>"+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"</canBeReadBy>";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's accessibility.canBeReadBy");

				expectedProperty = "<owner";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object owner ");

				expectedProperty = "cmsIdentifier=\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "externalId=\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				//Since user is SYSTEM User , she has by default the label 'ACCOUNT SYSTEM'. If pretty print
				//has been enabled, then xmlOrJson will processed to remove all whitespaces
				//and therefore label wil be ACCOUNTSYSTEM. In this case we need to remove the whitespaces from the value
				//in contentObject.getOwner().getLabel()
				expectedProperty = "label=\""+removeWhitespacesIfNecessary(contentObject.getOwner().getLabel())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = "<"+PropertyPath.getLastDescendant(propertyName);
					Assert.assertTrue(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n does not contain content object property "+propertyName);
				}
			}
			else{
				expectedProperty = "<title>"+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"</title>";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's profile.title");

				expectedProperty = "<owner";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object owner ");

				expectedProperty = "cmsIdentifier=\""+contentObject.getOwner().getId()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "externalId=\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's owner external id "+contentObject.getOwner().getExternalId());

				expectedProperty = "label=\""+contentObject.getOwner().getLabel()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "XML export \n"+xmlOrJson + " \n contains content object's owner external id "+contentObject.getOwner().getLabel());

				
				if (propertiesToExport != null && propertiesToExport.size() > 0){
					for (String propertyToExport : propertiesToExport){
						String propertyName = PropertyPath.getLastDescendant(propertyToExport);
						Assert.assertTrue(xmlOrJson.contains(propertyName), "XML export \n"+xmlOrJson + " \n does not contain content object property "+propertyName);
					}
				}
			}

		}
		else if (resourceRepresentationType.equals(ResourceRepresentationType.JSON)){
			
			String expectedProperty = "\"cmsIdentifier\":\""+contentObject.getId()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object identifier "+contentObject.getId());

			expectedProperty = "\"systemName\":\""+contentObject.getSystemName()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object system name "+contentObject.getSystemName());

			expectedProperty = "\"contentObjectTypeName\":\""+contentObject.getContentObjectType()+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object type "+contentObject.getContentObjectType());

			expectedProperty = "\"url\":\""+contentObject.getResourceApiURL(resourceRepresentationType,false)+"\"";
			Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object url "+contentObject.getResourceApiURL(resourceRepresentationType,false));

			if (fetchLevel == null || fetchLevel == FetchLevel.ENTITY || 
					fetchLevel == FetchLevel.ENTITY_AND_CHILDREN && CollectionUtils.isEmpty(propertiesToExport)){
				expectedProperty = "\"profile\":{";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile ");
				
				expectedProperty = "\"title\":\""+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile.title");

				expectedProperty = "\"created\":\""+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's profile.created");
				
				expectedProperty = "\"accessibility\":{";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's accessibility");
				
				expectedProperty = "\"canBeReadBy\":\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's accessibility.canBeReadBy");
				
				expectedProperty = "\"owner\":{";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object owner ");

				expectedProperty = "\"cmsIdentifier\":\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "\"externalId\":\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				//Since user is SYSTEM User , she has by default the label 'ACCOUNT SYSTEM'. If pretty print
				//has been enabled, then xmlOrJson will processed to remove all whitespaces
				//and therefore label wil be ACCOUNTSYSTEM. In this case we need to remove the whitespaces from the value
				//in contentObject.getOwner().getLabel()
				expectedProperty = "\"label\":\""+removeWhitespacesIfNecessary(contentObject.getOwner().getLabel())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					final String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = PropertyPath.getLastDescendant(propertyName);
					Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object property "+propertyName);
				}
			}
			else if (fetchLevel == FetchLevel.FULL){
				
				expectedProperty = "\"profile\":{";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile ");
				
				expectedProperty = "\"title\":\""+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile.title");

				expectedProperty = "\"created\":\""+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile.created");
				
				expectedProperty = "\"accessibility\":{";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's accessibility");
				
				expectedProperty = "\"canBeReadBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"\"]";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's accessibility.canBeReadBy");
				
				expectedProperty = "\"owner\":{";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object owner ");

				expectedProperty = "\"cmsIdentifier\":\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "\"externalId\":\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				//Since user is SYSTEM User , she has by default the label 'ACCOUNT SYSTEM'. If pretty print
				//has been enabled, then xmlOrJson will processed to remove all whitespaces
				//and therefore label wil be ACCOUNTSYSTEM. In this case we need to remove the whitespaces from the value
				//in contentObject.getOwner().getLabel()
				expectedProperty = "\"label\":\""+removeWhitespacesIfNecessary(contentObject.getOwner().getLabel())+"\"";
				Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					final String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = PropertyPath.getLastDescendant(propertyName);
					Assert.assertTrue(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object property "+propertyName);
				}
				
			}
			else{
				expectedProperty = "\"title\":\""+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n does not contain content object's profile.title");

				expectedProperty = "\"owner\":{";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object owner ");

				expectedProperty = "\"cmsIdentifier\":\""+contentObject.getOwner().getId()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "\"externalId\":\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's owner external id "+contentObject.getOwner().getExternalId());

				//Since user is SYSTEM User , she has by default the label 'ACCOUNT SYSTEM'. If pretty print
				//has been enabled, then xmlOrJson will processed to remove all whitespaces
				//and therefore label wil be ACCOUNTSYSTEM. In this case we need to remove the whitespaces from the value
				//in contentObject.getOwner().getLabel()
				expectedProperty = "\"label\":\""+removeWhitespacesIfNecessary(contentObject.getOwner().getLabel())+"\"";
				Assert.assertFalse(xmlOrJson.contains(expectedProperty), "JSON export \n"+xmlOrJson + " \n contains content object's owner external id "+contentObject.getOwner().getLabel());

				if (propertiesToExport != null && propertiesToExport.size() > 0){
					for (String propertyToExport : propertiesToExport){
						String propertyName = PropertyPath.getLastDescendant(propertyToExport);
						Assert.assertTrue(xmlOrJson.contains(propertyName), "JSON export \n"+xmlOrJson + " \n does not contain content object property "+propertyName);
					}
				}
			}

		}
		else{
			throw new Exception("Invalid resource representation type "+resourceRepresentationType);
		}
	}

	private void assertContentObjectHasTheProperPropertiesLoaded(ContentObject contentObject, ContentObject contentObjectReloaded, FetchLevel fetchLevel, List<String> propertiesToExport) {
		
		Assert.assertNotNull(contentObjectReloaded, "ContentService.getContentObject did not return ContentObject with name "+contentObject.getSystemName());
		Assert.assertEquals(contentObjectReloaded.getId(), contentObject.getId(), "ContentService.getContentObject returned invalid contentObject");
		Assert.assertEquals(contentObjectReloaded.getSystemName(), contentObject.getSystemName(), "ContentService.getContentObject returned invalid system name");
		
		Assert.assertNotNull(contentObjectReloaded.getContentObjectType(), "ContentService.getContentObject did not return content type for ContentObject with name "+contentObject.getSystemName());
		Assert.assertEquals(contentObjectReloaded.getContentObjectType(), contentObject.getContentObjectType(), "ContentService.getContentObject returned invalid content type");

		Assert.assertNotNull(contentObjectReloaded.getOwner(), "ContentService.getContentObject did not return owner for ContentObject with name "+contentObject.getSystemName());
		Assert.assertEquals(contentObjectReloaded.getOwner().getId(), contentObject.getOwner().getId(), "ContentService.getContentObject returned invalid owner id");
		Assert.assertEquals(contentObjectReloaded.getOwner().getLabel(), contentObject.getOwner().getLabel(), "ContentService.getContentObject returned invalid owner label");
		Assert.assertEquals(contentObjectReloaded.getOwner().getExternalId(), contentObject.getOwner().getExternalId(), "ContentService.getContentObject returned invalid owner externalId");


		if (fetchLevel == null || fetchLevel == FetchLevel.ENTITY || 
				fetchLevel == FetchLevel.ENTITY_AND_CHILDREN && CollectionUtils.isEmpty(propertiesToExport)){
			
			Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("profile.title"), 
			"ContentService.getContentObject did not return property 'profile.title'");

			Assert.assertFalse(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("profile.created"),"ContentService.getContentObject return property 'profile.created'");
			Assert.assertFalse(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("accessibility.canBeReadBy"),"ContentService.getContentObject return property 'accessibility.canBeReadBy'");
			
			for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
				Assert.assertFalse(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded(cmsPropertyPath.getPeriodDelimitedPath()),"ContentService.getContentObject return property '"+cmsPropertyPath.getPeriodDelimitedPath()+"'");
			}
		}
		else if (fetchLevel == FetchLevel.FULL){
			Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("profile.title"), 
			"ContentService.getContentObject did not return property 'profile.title'");

			Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("profile.created"),"ContentService.getContentObject did not return property 'profile.created'");
			Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("accessibility.canBeReadBy"),"ContentService.getContentObject did not return property 'accessibility.canBeReadBy'");
			
			for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
				Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded(cmsPropertyPath.getPeriodDelimitedPath()),"ContentService.getContentObject did not return property '"+cmsPropertyPath.getPeriodDelimitedPath()+"'");
			}
		}
		else{
			
			Assert.assertFalse(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded("profile.title"), 
			"ContentService.getContentObject returned property 'profile.title'");

			if (propertiesToExport != null && propertiesToExport.size() > 0){

				for (String propertyToExport : propertiesToExport){
					Assert.assertTrue(contentObjectReloaded.getComplexCmsRootProperty().isChildPropertyLoaded(propertyToExport),"ContentService.getContentObject did not return property '"+propertyToExport+"'");
				}
			}
		}

	}

}
