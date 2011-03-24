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
package org.betaconceptframework.astroboa.test.engine.io;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.AstroboaValidationEventHandler;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicIOTest extends AbstractRepositoryTest{

	public TopicIOTest() throws JAXBException {
		super();

	}

	private Logger logger = LoggerFactory.getLogger(getClass());


	@Test  
	public void testInvalidTopicImportFromXml() throws Exception {

		RepositoryUser systemUser = getSystemUser();
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();

		Topic topic = JAXBTestUtils.createTopic("topicName2", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);

		topic.setTaxonomy(subjectTaxonomy);

		String xml = null;
		try{
			xml = topic.xml(prettyPrint).replaceAll("owner", "ownerTest");

			TestLogPolicy.setLevelForLogger(Level.FATAL, Deserializer.class.getName());
			TestLogPolicy.setLevelForLogger(Level.FATAL, ImportBean.class.getName());
			TestLogPolicy.setLevelForLogger(Level.FATAL, AstroboaValidationEventHandler.class.getName());
			
			importService.importTopic(xml, true);

			TestLogPolicy.setDefaultLevelForLogger(Deserializer.class.getName());
			TestLogPolicy.setDefaultLevelForLogger(ImportBean.class.getName());
			TestLogPolicy.setDefaultLevelForLogger(AstroboaValidationEventHandler.class.getName());

		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Unexpected element ownerTest"), 
					"Invalid exception "+ e.getMessage() + " for wrong xml "+
					xml);
		}
	}

	@Test  
	public void testCompleteTopicIO() throws Throwable {

		Topic rootTopic1 = createRootTopicForSubjectTaxonomy("testFullTopicIO1");
		Topic rootTopic2 = createRootTopicForSubjectTaxonomy("testFullTopicIO2");

		Topic childTopic1 = createTopic("testFullTopicIOChild1", rootTopic1);
		Topic childTopic2 = createTopic("testFullTopicIOChild2", childTopic1);

		Topic childTopic3 = createTopic("testFullTopicIOChild3", rootTopic2);

		assertTopicIO(rootTopic1.getName());
		assertTopicIO(rootTopic2.getName());
		assertTopicIO(childTopic1.getName());
		assertTopicIO(childTopic2.getName());
		assertTopicIO(childTopic3.getName());


	}

	private void assertTopicIO(String topicName) throws Throwable {

		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();

		topicCriteria.doNotCacheResults();
		topicCriteria.addNameEqualsCriterion(topicName);

		CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

		for (Topic topic : cmsOutcome.getResults()){

			String xml = null;
			String json = null;

			FetchLevel fetchLevelForLog = null;
			ImportMode importModeForLog = null;

			try{
				for (FetchLevel fetchLevel : FetchLevel.values()){

					fetchLevelForLog = fetchLevel;

					if (fetchLevel == FetchLevel.ENTITY_AND_CHILDREN){
						topic.getChildren();
					}
					else if (fetchLevel == FetchLevel.FULL){
						loadAllTree(topic);
					}

					boolean compareChildTopics = fetchLevel != FetchLevel.ENTITY;

					for (ImportMode importMode : ImportMode.values()){

						importModeForLog = importMode;
						
						xml = topic.xml(prettyPrint);

						Topic topicUnMarshalledFromXML = importDao.importTopic(xml, importMode); 

						repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromXML, compareChildTopics, compareChildTopics);

						json = topic.json(prettyPrint);

						Topic topicUnMarshalledFromJSON = importDao.importTopic(json, importMode); 

						repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromJSON, compareChildTopics, compareChildTopics);
						repositoryContentValidator.compareTopics(topicUnMarshalledFromXML, topicUnMarshalledFromJSON, compareChildTopics, compareChildTopics);

						//Now create XML and JSON from Service and compare each other
						json = topicService.getTopic(topic.getId(), ResourceRepresentationType.JSON, fetchLevel);

						Topic topicUnMarshalledFromJSONService = importDao.importTopic(json, importMode); 

						repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromJSONService, compareChildTopics, compareChildTopics);
						repositoryContentValidator.compareTopics(topicUnMarshalledFromJSON, topicUnMarshalledFromJSONService, compareChildTopics, compareChildTopics);
						repositoryContentValidator.compareTopics(topicUnMarshalledFromXML, topicUnMarshalledFromJSONService, compareChildTopics, compareChildTopics);

						xml = topicService.getTopic(topic.getId(), ResourceRepresentationType.XML, fetchLevel);

						Topic topicUnMarshalledFromXMLService = importDao.importTopic(xml, importMode); 

						repositoryContentValidator.compareTopics(topic, topicUnMarshalledFromXMLService, compareChildTopics, compareChildTopics);
						repositoryContentValidator.compareTopics(topicUnMarshalledFromJSON, topicUnMarshalledFromXMLService, compareChildTopics, compareChildTopics);
						repositoryContentValidator.compareTopics(topicUnMarshalledFromXML, topicUnMarshalledFromXMLService, compareChildTopics, compareChildTopics);

						repositoryContentValidator.compareTopics(topicUnMarshalledFromXMLService, topicUnMarshalledFromJSONService, compareChildTopics, compareChildTopics);

					}

				}
			}
			catch(Throwable e){

				logger.error("Fetch Level {}", fetchLevelForLog);
				logger.error("Import Mode {}", importModeForLog);
				logPrettyXmlToError(xml, "XML");
				logger.error("JSON {}", json);

				throw e;
			}

		}
	}

	private void loadAllTree(Topic topic) {

		List<Topic> childTopics = topic.getChildren();

		if (childTopics != null && childTopics.size() > 0){
			for (Topic child : childTopics){
				loadAllTree(child);
			}
		}

	}

	@Test  
	public void testTopicJAXBMarshllingUnMarshalling() throws Throwable {

		Topic topic = JAXBTestUtils.createTopic("topicName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		topic.setTaxonomy(getSubjectTaxonomy());

		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildIO", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		childTopic1.setOwner(topic.getOwner());
		topic.addChild(childTopic1);


		String xml = topic.xml(prettyPrint);

		//Export to json
		String json = topic.json(prettyPrint);

		try{

			//Create a new instance for the same user using importService and its xml
			Topic topicFromXml = importDao.importTopic(xml, ImportMode.DO_NOT_SAVE);

			//Compare two instances
			repositoryContentValidator.compareTopics(topic, topicFromXml, true, true);

			//Create a new instance for the same user using importService and its xml
			Topic topicFromJson = importDao.importTopic(json, ImportMode.DO_NOT_SAVE);

			//Compare two instances
			repositoryContentValidator.compareTopics(topic, topicFromJson, true, true);

			//Compare
			repositoryContentValidator.compareTopics(topicFromXml, topicFromJson, true, true);

			JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicFromXml);

			JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(topicFromJson);
		}
		catch(Throwable e){
			try{
				logger.error("XML {}", TestUtils.prettyPrintXml(xml));
			}
			catch(Exception e1){
				logger.error("XML {}", xml);
			}

			logger.error("JSON {}", json);

			throw e;
		}


	}


}
