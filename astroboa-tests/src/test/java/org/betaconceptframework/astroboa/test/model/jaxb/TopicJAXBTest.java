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

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicJAXBTest extends AbstractRepositoryTest{

	@Test
	public void testJSONExportOfChildTopics() throws Throwable{
		
		Topic topic = JAXBTestUtils.createTopic("test-child-topic-json-export", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		
		Topic childTopic1 = JAXBTestUtils.createTopic("test-child-topic-json-export-first-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		topic.addChild(childTopic1);
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Topic has one child topic
		String json  = topic.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a topic with one child topic "+json);
			
			//Retrieve topic from repository
			topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.FULL, false);
			json  = topic.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a topic with one child topic "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//add one child
		Topic childTopic2 = JAXBTestUtils.createTopic("test-child-topic-json-export-second-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		childTopic2.setOwner(topic.getOwner());
		topic.addChild(childTopic2);
		
		topic = topicService.save(topic);
		
		json  = topic.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a topic with 2 child topics "+json);
			
			//Retrieve topic from repository
			topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.FULL, false);
			json  = topic.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a topic with 2 child topics "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		
	}

	@Test
	public void testJSONExportOfLocalizedLabel() throws Throwable{
		
		Topic topic = JAXBTestUtils.createTopic("test-localized-label-json-export", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);

		//Topic has 2 localized labels
		String json  = topic.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a topic with 2 localized labels "+json);
			
			//Retrieve topic from repository
			topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.FULL, false);
			json  = topic.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a topic with 2 localized labels "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//remove one label
		topic.getLocalizedLabels().remove("fr");
		topic = topicService.save(topic);

		
		json  = topic.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a topic with 1 localized label "+json);

			//Retrieve topic from repository
			topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.FULL, false);
			json  = topic.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a topic with 1 localized label "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
	}
	@Test  
	public void testTopicJAXBMarshllingUnMarshalling() throws Throwable {
		
		Topic topic = JAXBTestUtils.createTopic("topicName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		Taxonomy taxonomy = getSubjectTaxonomy();
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChild", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		childTopic1.setOwner(topic.getOwner());
		topic.addChild(childTopic1);
		
		topic.setTaxonomy(taxonomy);
		taxonomy.addRootTopic(topic);
		
		
		String xml = null;
		String json = null;
		
		ImportMode importMode = ImportMode.DO_NOT_SAVE;
		
		long start = System.currentTimeMillis();
		
			try{
				start = System.currentTimeMillis();
				xml = topic.xml(prettyPrint);
				
				logTimeElapsed("Export Topic XML using xml() method in {}", start);
				
				start = System.currentTimeMillis();
				Topic topicUnMarshalledFromXML = importDao.importTopic(xml, importMode);
				logTimeElapsed("Import Topic XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicUnMarshalledFromXML);
				
				repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromXML, true, true, true, true, true);
				
				start = System.currentTimeMillis();
				json = topic.json(prettyPrint);
				logTimeElapsed("Export Topic JSON using json() method in {}", start);
				
				start = System.currentTimeMillis();
				Topic topicUnMarshalledFromJSON = importDao.importTopic(json, importMode); 
				logTimeElapsed("Import Topic JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicUnMarshalledFromJSON);
				
				repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromJSON, true,true,true, true, true);
				repositoryContentValidator.compareTopics(topicUnMarshalledFromXML, topicUnMarshalledFromJSON, true,true,true, true, true);
				
				//Now create XML and JSON from Service and compare each other
				topic = topicService.save(topic);
				addEntityToBeDeletedAfterTestIsFinished(topic);
				
				start = System.currentTimeMillis();
				json = topicService.getTopic(topic.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL,prettyPrint);
				
				logTimeElapsed("Export Topic JSON using service in {}", start);
				
				start = System.currentTimeMillis();
				Topic topicUnMarshalledFromJSONService = importDao.importTopic(json, importMode); 
				logTimeElapsed("Import Topic JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicUnMarshalledFromJSONService);

				
				repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromJSONService, true, true, true, true, true);
				//Order of topics does matter since topicUnMarshalledFromJSON and topicUnMarshalledFromXML do not have identifier
				repositoryContentValidator.compareTopics(topicUnMarshalledFromJSONService, topicUnMarshalledFromJSON, true, true, true, true, false);
				repositoryContentValidator.compareTopics(topicUnMarshalledFromJSONService,topicUnMarshalledFromXML,  true, true, true, true, false);

				start = System.currentTimeMillis();
				xml = topicService.getTopic(topic.getId(), ResourceRepresentationType.XML, FetchLevel.FULL,prettyPrint);
				
				logTimeElapsed("Export Topic XML using service in {}", start);
				
				start = System.currentTimeMillis();
				Topic topicUnMarshalledFromXMLService = importDao.importTopic(xml, importMode); 
				logTimeElapsed("Import Topic XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicUnMarshalledFromXMLService);
				
				repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromXMLService, true, true, true, true, true);
				//Order of topics does matter since topicUnMarshalledFromJSON and topicUnMarshalledFromXML do not have identifier
				repositoryContentValidator.compareTopics(topicUnMarshalledFromXMLService, topicUnMarshalledFromJSON, true, true, true, true, false);
				repositoryContentValidator.compareTopics(topicUnMarshalledFromXMLService, topicUnMarshalledFromXML, true, true, true, true, false);
				
				repositoryContentValidator.compareTopics(topicUnMarshalledFromXMLService, topicUnMarshalledFromJSONService, true, true, true, true, true);

				
			}
			catch(Throwable e){
				logger.error("Created XML :\n {}", xml);
				logger.error("Second JSON :\n{} ", json);
				throw e;
			}
	}

	@Test
	public void testNumberOfChildrenExport(){

		Topic topic = JAXBTestUtils.createTopic("topicTestNumberOfChildren", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		Taxonomy taxonomy = getSubjectTaxonomy();
		topic.setTaxonomy(taxonomy);

		topic = topicService.save(topic);
		
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		String xmlFromApi = topic.xml(prettyPrint);
		String xmlFromService = topicService.getTopic(topic.getId(), ResourceRepresentationType.XML, FetchLevel.FULL,prettyPrint);

		String jsonFromApi = topic.json(prettyPrint);
		String jsonFromService = topicService.getTopic(topic.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL,prettyPrint);

		xmlFromApi = removeWhitespacesIfNecessary(xmlFromApi);
		jsonFromApi = removeWhitespacesIfNecessary(jsonFromApi);
		xmlFromService = removeWhitespacesIfNecessary(xmlFromService);
		jsonFromService = removeWhitespacesIfNecessary(jsonFromService);
		
		final String expectedValueInXML = "numberOfChildren=\"0\"";
		final String expectedValueInJSON = "\"numberOfChildren\":\"0\"";

		Assert.assertTrue(xmlFromApi.contains(expectedValueInXML), "Topic XML export from API "+xmlFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(xmlFromService.contains(expectedValueInXML), "Topic XML export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");

		Assert.assertTrue(jsonFromApi.contains(expectedValueInJSON), "Topic JSON export from API "+jsonFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(jsonFromService.contains(expectedValueInJSON), "Topic JSON export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");
	}
}
