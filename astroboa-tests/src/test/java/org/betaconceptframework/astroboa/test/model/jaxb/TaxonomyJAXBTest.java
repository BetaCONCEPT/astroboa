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
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyJAXBTest extends AbstractRepositoryTest{

	@Test
	public void testJSONExportOfRootTopics() throws Throwable{
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("topicTaxonomy",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());

		Topic childTopic1 = JAXBTestUtils.createTopic("test-child-topic-json-export-first-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		taxonomy.addRootTopic(childTopic1);
		
		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);

		//Taxonomy has one child topic
		String json  = taxonomy.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"rootTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a taxonomy with one child topic "+json);
			
			//Retrieve taxonomy from repository
			taxonomy = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL);
			json  = taxonomy.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"rootTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a taxonomy with one child topic "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//add one child
		Topic childTopic2 = JAXBTestUtils.createTopic("test-child-topic-json-export-second-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		taxonomy.addRootTopic(childTopic2);
		
		taxonomy = taxonomyService.save(taxonomy);

		json  = taxonomy.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"rootTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a taxonomy with 2 child topics "+json);

			//Retrieve taxonomy from repository
			taxonomy = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL);
			json  = taxonomy.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"rootTopics\":{\"topic\":[{"), 
					"Invalid JSON export of a taxonomy with 2 child topics "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		
	}

	@Test
	public void testJSONExportOfLocalizedLabel() throws Throwable{
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("test-localized-label-json-export",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());

		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);

		//Taxonomy has 2 localized labels
		String json  = taxonomy.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}}"), 
					"Invalid JSON export of a taxonomy with 2 localized labels "+json);
			
			//Retrieve taxonomy from repository
			taxonomy = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL);
			json  = taxonomy.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}}"), 
					"Invalid JSON export of a taxonomy with 2 localized labels "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//remove one label
		taxonomy.getLocalizedLabels().remove("fr");
		taxonomy = taxonomyService.save(taxonomy);

		
		json  = taxonomy.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}}"), 
					"Invalid JSON export of a taxonomy with 1 localized label "+json);
			
			//Retrieve taxonomy from repository
			taxonomy = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL);
			json  = taxonomy.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}}"), 
					"Invalid JSON export of a taxonomy with 1 localized label "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
	}
	
	@Test  
	public void testTaxonomyJAXBMarshallingUnMarshalling() throws Throwable {
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("topicTaxonomyMarshallingUnMarshalling",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		Topic topic = JAXBTestUtils.createTopic("firstChild", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		topic.setTaxonomy(taxonomy);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondChild", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		secondTopic.setTaxonomy(taxonomy);
		taxonomy.addRootTopic(secondTopic);
		
		String xml = null;
		String json = null;
		
		ImportMode importMode = ImportMode.DO_NOT_SAVE;
		
		long start = System.currentTimeMillis();
		
			try{
				start = System.currentTimeMillis();
				xml = taxonomy.xml(prettyPrint);
				
				
				logTimeElapsed("Export Taxonomy XML using xml() method in {}", start);
				
				start = System.currentTimeMillis();
				Taxonomy taxonomyUnMarshalledFromXML = importDao.importTaxonomy(xml, importMode);
				logTimeElapsed("Import Taxonomy XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyUnMarshalledFromXML.getRootTopics().get(0));
				
				repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromXML, true, true);
				
				start = System.currentTimeMillis();
				json = taxonomy.json(prettyPrint);
				logTimeElapsed("Export Taxonomy JSON using json() method in {}", start);
				
				start = System.currentTimeMillis();
				Taxonomy taxonomyUnMarshalledFromJSON = importDao.importTaxonomy(json, importMode); 
				logTimeElapsed("Import Taxonomy JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyUnMarshalledFromJSON.getRootTopics().get(0));
				
				repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromJSON, true,true);
				repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXML, taxonomyUnMarshalledFromJSON, true,true);
				
				//Now create XML and JSON from Service and compare each other
				taxonomy = taxonomyService.save(taxonomy);
				
				start = System.currentTimeMillis();
				json = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL);
				
				logTimeElapsed("Export Taxonomy JSON using service in {}", start);
				
				start = System.currentTimeMillis();
				Taxonomy taxonomyUnMarshalledFromJSONService = importDao.importTaxonomy(json, importMode); 
				logTimeElapsed("Import Taxonomy JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyUnMarshalledFromJSONService.getRootTopics().get(0));

				
				repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromJSONService, true, true);
				repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromJSONService, taxonomyUnMarshalledFromJSON, false, true);
				repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromJSONService, taxonomyUnMarshalledFromXML, false, true);

				start = System.currentTimeMillis();
				xml = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);
				
				logTimeElapsed("Export Taxonomy XML using service in {}", start);
				
				start = System.currentTimeMillis();
				Taxonomy taxonomyUnMarshalledFromXMLService = importDao.importTaxonomy(xml, importMode); 
				logTimeElapsed("Import Taxonomy XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(taxonomyUnMarshalledFromXMLService.getRootTopics().get(0));
				
				repositoryContentValidator.compareTaxonomies(taxonomy, taxonomyUnMarshalledFromXMLService, true, true);
				repositoryContentValidator.compareTaxonomies( taxonomyUnMarshalledFromXMLService, taxonomyUnMarshalledFromJSON,false, true);
				repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXMLService, taxonomyUnMarshalledFromXML, false, true);
				
				repositoryContentValidator.compareTaxonomies(taxonomyUnMarshalledFromXMLService, taxonomyUnMarshalledFromJSONService, true, true);

				
			}
			catch(Throwable e){
				logger.error("Created XML :\n {}", xml);
				logger.error("Second JSON :\n{} ", json);
				throw e;
			}
		}


	@Test  
	public void testXmlLangNamespaceDeclarationAppearsInXmlExport() throws Throwable {
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomyTestXmlLang",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy.addLocalizedLabel("en", "taxonomyTestXmlLang");
		
		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);
		
		String xsiNamesplaceDeclaration = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
		String xmlNamespaceDeclaration = "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"";
		String astroboaModelScehmaLocation =removeWhitespacesIfNecessary("http://www.betaconceptframework.org/schema/astroboa/model http://localhost:8080/content-api/repository/definition/astroboa-model");
		String xmlScehmaLocation = removeWhitespacesIfNecessary("http://www.w3.org/XML/1998/namespace http://www.w3.org/2001/03/xml.xsd");
		
		String xmlFromApi = taxonomy.xml(prettyPrint);
		
		jaxbValidationUtils.validateUsingSAX(xmlFromApi);
		
		String xmlWithoutIndentation = removeWhitespacesIfNecessary(xmlFromApi);
		
		Assert.assertTrue(xmlWithoutIndentation.contains(xsiNamesplaceDeclaration), "Xsi Namespace Declaration "+xsiNamesplaceDeclaration+" not found in taxonomy xml export from api "+ xmlFromApi);
		Assert.assertTrue(xmlWithoutIndentation.contains(astroboaModelScehmaLocation), "Astroboa Model Schema Location "+astroboaModelScehmaLocation+" not found in taxonomy xml export from api "+ xmlFromApi);
		Assert.assertFalse(xmlWithoutIndentation.contains(xmlNamespaceDeclaration), "XML Namespace Declaration "+xmlNamespaceDeclaration+" not found in taxonomy xml export from api "+ xmlFromApi);
		Assert.assertFalse(xmlWithoutIndentation.contains(xmlScehmaLocation), "XML Schema Location "+xmlScehmaLocation+" not found in taxonomy xml export from api "+ xmlFromApi);
		
		String xmlFromService = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);
		
		jaxbValidationUtils.validateUsingSAX(xmlFromService);
		
		xmlWithoutIndentation = removeWhitespacesIfNecessary(xmlFromService);
		
		Assert.assertTrue(xmlWithoutIndentation.contains(xsiNamesplaceDeclaration), "Xsi Namespace Declaration "+xsiNamesplaceDeclaration+" not found in taxonomy xml export from service "+ TestUtils.prettyPrintXml(xmlFromService));
		Assert.assertTrue(xmlWithoutIndentation.contains(astroboaModelScehmaLocation), "Astroboa Model Schema Location "+astroboaModelScehmaLocation+" not found in taxonomy xml export from service "+ TestUtils.prettyPrintXml(xmlFromService));
		Assert.assertFalse(xmlWithoutIndentation.contains(xmlNamespaceDeclaration), "XML Namespace Declaration "+xmlNamespaceDeclaration+" not found in taxonomy xml export from service "+ TestUtils.prettyPrintXml(xmlFromService));
		Assert.assertFalse(xmlWithoutIndentation.contains(xmlScehmaLocation), "XML Schema Location "+xmlScehmaLocation+" not found in taxonomy xml export from service "+ TestUtils.prettyPrintXml(xmlFromService));

	}

	
	@Test
	public void testNumberOfChildrenExport(){

		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomyTestNumberOfChildren",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy.addLocalizedLabel("en", "taxonomyTestNumberOfChildren");
		
		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);
		
		String xmlFromApi = taxonomy.xml(prettyPrint);
		String xmlFromService = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);

		String jsonFromApi = taxonomy.json(prettyPrint);
		String jsonFromService = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL);

		xmlFromApi = removeWhitespacesIfNecessary(xmlFromApi);
		jsonFromApi = removeWhitespacesIfNecessary(jsonFromApi);
		xmlFromService = removeWhitespacesIfNecessary(xmlFromService);
		jsonFromService = removeWhitespacesIfNecessary(jsonFromService);
		
		final String expectedValueInXML = "numberOfChildren=\"0\"";
		final String expectedValueInJSON = "\"numberOfChildren\":\"0\"";

		Assert.assertTrue(xmlFromApi.contains(expectedValueInXML), "Taxonomy XML export from API "+xmlFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(xmlFromService.contains(expectedValueInXML), "Taxonomy XML export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");

		Assert.assertTrue(jsonFromApi.contains(expectedValueInJSON), "Taxonomy JSON export from API "+jsonFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(jsonFromService.contains(expectedValueInJSON), "Taxonomy JSON export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");
	}
}
