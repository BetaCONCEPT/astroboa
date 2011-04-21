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
package org.betaconceptframework.astroboa.test.model.jaxb;

import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsOutcomeJAXBTest extends AbstractRepositoryTest{

	private Logger logger = LoggerFactory.getLogger(getClass());
	

	@Test  
	public void testCmsOutcomeOfContentObjectWithLimitZero() throws Throwable {
		
		for (String testContentType : getTestContentTypes()){
			
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(testContentType);
		
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
			contentObjectCriteria.addSystemNameContainsCriterion("*testCmsOutcomeForJAXB*");
		
			contentObjectCriteria.doNotCacheResults();
		
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.title");
			
			contentObjectCriteria.setOffsetAndLimit(0, 0);
		
			String json =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
		
			try{
				
				final String expectedOutcome = "{\""+CmsConstants.TOTAL_RESOURCE_COUNT+"\":\"2\",\""+CmsConstants.OFFSET+"\":\"0\",\""+CmsConstants.LIMIT+"\":\"0\"}";
				Assert.assertEquals(StringUtils.deleteWhitespace(json), expectedOutcome, "Invalid JSON resource response outcome "+json);
			}
			catch(Throwable e){
				logger.error(json, e);
				throw e;
			}

			String xml =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
			
			try{
				final String expected = CmsConstants.TOTAL_RESOURCE_COUNT+"=\"2\""+CmsConstants.OFFSET+"=\"0\""+CmsConstants.LIMIT+"=\"0\"><"+CmsConstants.RESOURCE_COLLECTION+"/></"+CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME+">";
				Assert.assertTrue(StringUtils.deleteWhitespace(xml).contains(expected), "Invalid XML resource response outcome "+xml + " .Expected \n "+expected);
			}
			catch(Throwable e){
				logger.error(json, e);
				throw e;
			}

		}		
	}
	
	@Test  
	public void testCmsOutcomeForJSON() throws Throwable {
		for (String testContentType : getTestContentTypes()){
			
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(testContentType);
		
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
			contentObjectCriteria.addSystemNameContainsCriterion("*testCmsOutcomeForJAXB*");
		
			contentObjectCriteria.doNotCacheResults();
		
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.title");
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("accessibility.canBeReadBy");
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("stringEnum");
			
			contentObjectCriteria.setOffsetAndLimit(0, 2);
		
			String json =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
		
			try{
				final String jsonWithoutWhitespacesIfNecessary = removeWhitespacesIfNecessary(json);
				
				String expected = "{\""+CmsConstants.TOTAL_RESOURCE_COUNT+"\":\"2\",\""+CmsConstants.OFFSET+"\":\"0\",\""+CmsConstants.LIMIT+"\":\"2\",\""+CmsConstants.RESOURCE_COLLECTION+"\":{\""+CmsConstants.RESOURCE+"\":[{";
				Assert.assertTrue(jsonWithoutWhitespacesIfNecessary.startsWith(expected), "Invalid JSON resource response outcome "+json + " .Expected \n "+expected);
				
				expected = "\"profile\":{\"cmsIdentifier\":";
				Assert.assertTrue(jsonWithoutWhitespacesIfNecessary.contains(expected), "Invalid JSON resource response outcome "+json + " .Expected \n "+expected);

				expected = "\"accessibility\":{\"cmsIdentifier\":";
				Assert.assertTrue(jsonWithoutWhitespacesIfNecessary.contains(expected), "Invalid JSON resource response outcome "+json + " .Expected \n "+expected);

			}
			catch(Throwable e){
				logger.error(json, e);
				throw e;
			}

		}		
	}
	@Test  
	public void testCmsOutcomeForProjectedProperties() throws Throwable {
		
		for (String testContentType : getTestContentTypes()){
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(testContentType);
		
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
			contentObjectCriteria.addSystemNameContainsCriterion("*testCmsOutcomeForJAXB*");
		
			contentObjectCriteria.doNotCacheResults();
		
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("profile.title");
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("accessibility.canBeReadBy");
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("stringEnum");
		
			String xml =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
		
			try{
				Assert.assertTrue(xml.contains("<profile") && xml.contains("<title>"), "Property profile.title has not been marshalled to XML "+ xml);
				Assert.assertTrue(xml.contains("<accessibility") && xml.contains("<canBeReadBy>"), "Property accessibility.canBeReadBy has not been marshalled to XML"+ xml);
				Assert.assertTrue(xml.contains("<stringEnum>"), "Property stringEnum has not been marshalled to XML"+ xml);
				Assert.assertFalse(xml.contains("<viewCounter>") , "Property viewCounter has been marshalled to XML"+ xml);
				Assert.assertFalse(xml.contains("<created>") , "Property created has been marshalled to XML but it shouldnot have been"+ xml);

				validateJsonCmsOutcome(contentObjectCriteria);
			}
			catch(Throwable e){
				logXml(xml, e);
				throw e;
			}

		}		
	}
	
	@Test  
	public void testCmsOutcomeForContentObjectJAXBMarshallingUnMarshalling() throws Throwable {
		
		for (String testContentType : getTestContentTypes()){

			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(testContentType);

			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

			contentObjectCriteria.addSystemNameContainsCriterion("*testCmsOutcomeForJAXB*");

			contentObjectCriteria.doNotCacheResults();

			String xml =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);

			logger.debug("Created XML :\n {}",xml);

			try{
				jaxbValidationUtils.validateUsingSAX(xml);

				validateJsonCmsOutcome(contentObjectCriteria);
			}
			catch(Throwable e){
				logXml(xml, e);
				throw e;
			}
		}		
	}

	private void logXml(String xml, Throwable e) {
		try{
			logger.error(TestUtils.prettyPrintXml(xml), e);
		}
		catch(Exception e1){
			//In case pretty print fails
			logger.error(xml, e1);
		}
	}
	
	@Test  
	public void testCmsOutcomeForTopicJAXBMarshallingUnMarshalling() throws Throwable {
		
		
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.addOrderByLocale("en", Order.ascending);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		String xml = null;
		
		try{
			serializationDao.serializeSearchResults(getSession(), topicCriteria, os, FetchLevel.ENTITY, ResourceRepresentationType.XML, false);

			xml = new String(os.toByteArray(), "UTF-8");

			logger.debug("Created XML :\n {}", 	xml);

			jaxbValidationUtils.validateUsingSAX(xml);
		}
		catch(Throwable e){
			logXml(xml, e);
			throw e;
		}
		finally{
			IOUtils.closeQuietly(os);
		}
		
		
		
	}


	@Test
	public void testReferencesAtCmsOutcome() throws Throwable{
		
		//Create content for test
		RepositoryUser systemUser = getSystemUser();
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();

		//Create Topics
		Topic topic = JAXBTestUtils.createTopic("usedAsAReference", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		topic.setOwner(systemUser);
		topic.setTaxonomy(subjectTaxonomy);
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);

		
		//Create objects which contain only references
		ContentObject simpleContentObject = createContentObjectForType(TEST_CONTENT_TYPE, getSystemUser(), "testReferencesAtCmsOutcomeForJAXB", false);
		
		((StringProperty)simpleContentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Content Object With references for JAXB test");
		simpleContentObject = contentService.save(simpleContentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(simpleContentObject);

		ContentObject contentObjectWithReferences = createContentObjectForType(TEST_CONTENT_TYPE, getSystemUser(), "testReferencesAtCmsOutcomeForJAXB2", false);
		
		((ObjectReferenceProperty)contentObjectWithReferences.getCmsProperty("profile.hasPart")).addSimpleTypeValue(simpleContentObject);
		((ObjectReferenceProperty)contentObjectWithReferences.getCmsProperty("profile.references")).addSimpleTypeValue(simpleContentObject);
		((StringProperty)contentObjectWithReferences.getCmsProperty("profile.title")).setSimpleTypeValue("Content Object With references for JAXB test 2");
		((TopicReferenceProperty)contentObjectWithReferences.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		((TopicReferenceProperty)contentObjectWithReferences.getCmsProperty("simpleTopic")).addSimpleTypeValue(topic);
		contentObjectWithReferences = contentService.save(contentObjectWithReferences, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObjectWithReferences);

		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
	
		contentObjectCriteria.addSystemNameContainsCriterion("testReferencesAtCmsOutcomeForJAXB*");
		contentObjectCriteria.doNotCacheResults();
	
		String xml =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);

		String xmlWithoutWhitespaces = removeWhitespacesIfNecessary(contentObjectCriteria, xml);
		
		try{
			String expectedString = "<references cmsIdentifier=\""+simpleContentObject.getId()+"\" systemName=\""+simpleContentObject.getSystemName()
			+"\" url=\""+simpleContentObject.getResourceApiURL(ResourceRepresentationType.XML, false,simpleContentObject.getSystemName()!=null)+"\" contentObjectTypeName=\""+TEST_CONTENT_TYPE+"\"";
			
			String expectedStringWithoutWhitespaces = removeWhitespacesIfNecessary(contentObjectCriteria, expectedString);
			
			Assert.assertTrue(xmlWithoutWhitespaces.contains(expectedStringWithoutWhitespaces), "ContentObjectReference has not been marshalled to XML. Did not find "+ expectedString+ " in XML "+xmlWithoutWhitespaces);
			
			expectedString = "<simpleTopic cmsIdentifier=\""+topic.getId()+"\" name=\""+topic.getName()+"\""+" url=\""+topic.getResourceApiURL(ResourceRepresentationType.XML, false,topic.getName()!=null)+"\"";
			expectedStringWithoutWhitespaces = removeWhitespacesIfNecessary(contentObjectCriteria, expectedString);

			Assert.assertTrue(xmlWithoutWhitespaces.contains(expectedStringWithoutWhitespaces), "Topic reference has not been marshalled to XML"+expectedString);
		}
		catch(Throwable e){
			logger.error(xml, e);
			throw e;
		}

		String json =  contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);

		String jsonWithoutWhitespaces = removeWhitespacesIfNecessary(contentObjectCriteria, json);

		try{
			String expectedString = "\"simpleTopic\":{\"cmsIdentifier\":\""+topic.getId()+"\"";
			Assert.assertTrue(jsonWithoutWhitespaces.contains(expectedString), "Topic reference has not been marshalled correctly to JSON. Found json output "+expectedString);
			
			expectedString = "\"cmsIdentifier\":\""+simpleContentObject.getId()+"\"";
			Assert.assertTrue(jsonWithoutWhitespaces.contains(expectedString), "ContentObjectReference reference has not been marshalled correctly to JSON. Found json output "+expectedString);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
	}
	
	@Override
	protected void postSetup() throws Exception {
		
		super.postSetup();
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();

		//Create Topics
		Topic topic = JAXBTestUtils.createTopic("zfirstSubject", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		topic.setOwner(systemUser);
		topic.setTaxonomy(subjectTaxonomy);
		
		Topic childTopic1 = JAXBTestUtils.createTopic("secondSubject", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		childTopic1.setTaxonomy(subjectTaxonomy);
		
		topic.addChild(childTopic1);

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		for (String testContentType : getTestContentTypes()){

			ContentObject contentObject = createContentObjectForType(testContentType,systemUser, topic, childTopic1, "1"+testContentType);
		
			contentObject = contentService.save(contentObject, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
			ContentObject contentObject2 = createContentObjectForType(testContentType, systemUser, topic, childTopic1, "2"+testContentType);
		
			//Reference one another
		
			((ObjectReferenceProperty)contentObject2.getCmsProperty("profile.hasPart")).addSimpleTypeValue(contentObject);
			((ObjectReferenceProperty)contentObject2.getCmsProperty("profile.references")).addSimpleTypeValue(contentObject);
		
			contentObject2 = contentService.save(contentObject2, false, true, null);
			addEntityToBeDeletedAfterTestIsFinished(contentObject2);
		}

	}

	private ContentObject createContentObjectForType(String contentType, RepositoryUser systemUser,
			Topic topic, Topic childTopic1, String systemName) {
		ContentObject contentObject = createContentObjectAndPopulateAllPropertiesForType(contentType, systemUser, "testCmsOutcomeForJAXB"+systemName, true);
		
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Content Object for JAXB test");
		((StringProperty)contentObject.getCmsProperty("profile.description")).setSimpleTypeValue("");
		
		((TopicReferenceProperty)contentObject.getCmsProperty("testTopic")).addSimpleTypeValue(childTopic1);
		
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(childTopic1);
		
		return contentObject;
	}
}
