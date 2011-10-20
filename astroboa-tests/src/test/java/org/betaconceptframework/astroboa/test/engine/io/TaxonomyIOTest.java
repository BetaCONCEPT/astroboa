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

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyIOTest extends AbstractRepositoryTest{

	public TaxonomyIOTest() throws JAXBException {
		super();

	}

	@Test  
	public void testCompleteTaxonomyIO() throws Throwable {

		Topic rootTopic1 = createRootTopicForSubjectTaxonomy("testFullTopicTaxonomyJAXB1");
		Topic rootTopic2 = createRootTopicForSubjectTaxonomy("testFullTopicTaxonomyJAXB2");

		Topic childTopic1 = createTopic("testFullTopicTaxonomyJAXBChild1", rootTopic1);
		createTopic("testFullTopicTaxonomyJAXBChild2", childTopic1);

		createTopic("testFullTopicTaxonomyJAXBChild3", rootTopic2);

		assertTaxonomyIO(Taxonomy.SUBJECT_TAXONOMY_NAME);

	}

	private void assertTaxonomyIO(String taxonomyName) throws Throwable {

		Taxonomy taxonomy = null;

		String xml = null;
		String json = null;

		FetchLevel fetchLevelForLog = null;
		PersistMode persistModeForLog = null;

		try{
			for (FetchLevel fetchLevel : FetchLevel.values()){

				fetchLevelForLog = fetchLevel;

				boolean compareRootTopics = fetchLevel != FetchLevel.ENTITY;

				taxonomy = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, fetchLevel, false);
				
				for (PersistMode persistMode : PersistMode.values()){
					
					persistModeForLog = persistMode;
					
					ImportConfiguration configuration = ImportConfiguration.taxonomy()
							.persist(persistMode)
							.build();

					xml = taxonomy.xml(prettyPrint);
					
					Taxonomy taxonomyUnMarshalledFromXML = importDao.importTaxonomy(xml, configuration); 

					repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromXML, true, compareRootTopics);

					json = taxonomy.json(prettyPrint);

					Taxonomy taxonomyUnMarshalledFromJSON = importDao.importTaxonomy(json, configuration); 

					repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromJSON, true, compareRootTopics);
					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXML, taxonomyUnMarshalledFromJSON, true, compareRootTopics);

					//Now create XML and JSON from Service and compare each other
					json = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.JSON, fetchLevel, prettyPrint);
					Taxonomy taxonomyUnMarshalledFromJSONService = importDao.importTaxonomy(json, configuration); 

					repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromJSONService, true, compareRootTopics);
					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromJSON, taxonomyUnMarshalledFromJSONService, true, compareRootTopics);
					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXML, taxonomyUnMarshalledFromJSONService, true, compareRootTopics);

					xml = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.XML, fetchLevel, prettyPrint);
					Taxonomy taxonomyUnMarshalledFromXMLService = importDao.importTaxonomy(xml,configuration); 

					repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromXMLService, true, compareRootTopics);
					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromJSON, taxonomyUnMarshalledFromXMLService, true, compareRootTopics);
					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXML, taxonomyUnMarshalledFromXMLService, true, compareRootTopics);

					repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXMLService, taxonomyUnMarshalledFromJSONService, true, compareRootTopics);

				}
			}
		}
		catch(Throwable e){
			logger.error("Fetch Level {}", fetchLevelForLog);
			logger.error("Import Mode {}", persistModeForLog);
			logger.error("XML {}", xml);
			logger.error("JSON {}", json);


			throw e;
		}

	}

	@Test  
	public void testCustomTaxonomyIO() throws Throwable {

		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("topicTaxonomy",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());

		Topic topic = JAXBTestUtils.createTopic("firstChild2", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());

		topic.setTaxonomy(taxonomy);

		Topic secondTopic = JAXBTestUtils.createTopic("secondChild", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());

		secondTopic.setTaxonomy(taxonomy);
		taxonomy.addRootTopic(secondTopic);
		
		String xml = taxonomy.xml(prettyPrint);

		//Export to json
		String json = taxonomy.json(prettyPrint);

		try{

			ImportConfiguration configuration = ImportConfiguration.taxonomy()
					.persist(PersistMode.DO_NOT_PERSIST)
					.build();

			//Create a new instance for the same user using importService and its xml
			Taxonomy taxonomyFromXml = importDao.importTaxonomy(xml, configuration);

			//Compare two instances
			repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyFromXml, true, true);

			//Create a new instance for the same user using importService and its xml
			Taxonomy taxonomyFromJson = importDao.importTaxonomy(json, configuration);

			//Compare two instances
			repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyFromJson, true, true);

			//Compare
			repositoryContentValidator.compareTaxonomies(taxonomyFromXml, taxonomyFromJson, true, true);

			JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyFromXml.getRootTopics().get(0));

			JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyFromJson.getRootTopics().get(0));
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


	@Test  
	public void testSubjectTaxonomyImportFromXml() throws Throwable {
		
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();
		
		String xml = subjectTaxonomy.xml(prettyPrint);
		
		String json = subjectTaxonomy.json(prettyPrint);

		try{
		
			ImportConfiguration configuration = ImportConfiguration.taxonomy()
					.persist(PersistMode.PERSIST_ENTITY_TREE)
					.build();

			//Check plain Taxonomy
			//We expect to have the same id with Subject Taxonomy
			Taxonomy taxonomyFromXml = importService.importTaxonomy(xml, configuration);
			repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromXml, true, false);
			
			json = taxonomyFromXml.json(prettyPrint);
			
			Taxonomy taxonomyFromJson = importService.importTaxonomy(json, configuration);
			repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromJson, true, false);
			
			repositoryContentValidator.compareTaxonomies(taxonomyFromXml, taxonomyFromJson, true, false);
		
			//Create a new taxonomy with the same name
			//add one topic and resave
			Taxonomy taxonomy = JAXBTestUtils.createTaxonomy(Taxonomy.SUBJECT_TAXONOMY_NAME,
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
			Topic topic = JAXBTestUtils.createTopic("topicName3", 
					CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
					CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
			
			Topic childTopic1 = JAXBTestUtils.createTopic("firstChild", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
			childTopic1.setOwner(topic.getOwner());
			topic.addChild(childTopic1);
		
			topic.setTaxonomy(taxonomy);
			taxonomy.addRootTopic(topic);

			xml = taxonomy.xml(prettyPrint);
			taxonomyFromXml = importService.importTaxonomy(xml,configuration);
			
			//Load subject taxonomy root topics
			subjectTaxonomy = taxonomyService.getBuiltInSubjectTaxonomy("en");
			subjectTaxonomy.getRootTopics();
			repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromXml, true, false);
			
			//Same with JSON
			json = taxonomyFromXml.json(prettyPrint);
			taxonomyFromJson = importService.importTaxonomy(json, configuration);
			repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromJson, true, false);
			
			repositoryContentValidator.compareTaxonomies(taxonomyFromXml, taxonomyFromJson, true, false);
		
			//Finally check that topics have System User as Owner
			assertTopicOwnerIsSystem(subjectTaxonomy.getRootTopics(), getSystemUser());
			assertTopicOwnerIsSystem(taxonomyFromXml.getRootTopics(), getSystemUser());
			assertTopicOwnerIsSystem(taxonomyFromJson.getRootTopics(), getSystemUser());

			markTopicForRemoval(taxonomyFromXml.getRootTopics().get(0));
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
	
	
	private void assertTopicOwnerIsSystem(List<Topic> topics, RepositoryUser systemUser) {
		if (CollectionUtils.isNotEmpty(topics)){
			for (Topic topic : topics){
				Assert.assertEquals(topic.getOwner().getId(), systemUser.getId());
				Assert.assertEquals(topic.getOwner().getExternalId(), systemUser.getExternalId());

				if (topic.isChildrenLoaded()){
					assertTopicOwnerIsSystem(topic.getChildren(), systemUser);
				}
			}
		}
	}



}
