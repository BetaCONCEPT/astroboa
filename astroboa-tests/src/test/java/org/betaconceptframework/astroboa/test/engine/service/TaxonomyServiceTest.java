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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyServiceTest extends AbstractRepositoryTest{

	@Test
	/*
	 * Test for http://jira.betaconceptframework.org/browse/ASTROBOA-156
	 */
	public void testUpdateTaxonomyUsingOnlyItsIdentifier(){

		String taxonomyName = "taxonomyUpdateWithoutName";
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy(taxonomyName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		markTaxonomyForRemoval(taxonomy);
		
		//Remove Name
		taxonomy.setName(null);
		
		//Export to XML and json
		String xml = taxonomy.xml(prettyPrint);
		String json = taxonomy.json(prettyPrint);
		
		//Save the instance
		taxonomy = taxonomyService.save(taxonomy);
		
		//Assert update operation
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");

		taxonomy = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		Assert.assertNotNull(taxonomy, "Taxonomy '"+ taxonomyName +"' was not found");
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");
		
		//Save the XML
		ImportConfiguration importConfiguration = ImportConfiguration.taxonomy().persist(PersistMode.PERSIST_MAIN_ENTITY).build();
		
		taxonomy = importService.importTaxonomy(xml, importConfiguration);
		
		//Assert update operation
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");

		taxonomy = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		Assert.assertNotNull(taxonomy, "Taxonomy '"+ taxonomyName +"' was not found");
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");

		//Save the JSON
		taxonomy = importService.importTaxonomy(json, importConfiguration);
		
		//Assert update operation
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");

		taxonomy = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		Assert.assertNotNull(taxonomy, "Taxonomy '"+ taxonomyName +"' was not found");
		Assert.assertEquals(taxonomy.getName(), taxonomyName, "Taxonomy Name was changed");

	}

	@Test
	public void testGetTaxonomyAsTaxonomyOutcome() throws Throwable{
		
		CmsOutcome<Taxonomy> outcome = taxonomyService.getTaxonomy(Taxonomy.SUBJECT_TAXONOMY_NAME, ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.ENTITY, false);
		
		Assert.assertNotNull(outcome, "TaxonomyService.getTaxonomy returned null with Outcome returned type");
		
		Assert.assertEquals(outcome.getCount(), 1, "TaxonomyService.getTaxonomy returned invalid count with Outcome returned type");
		Assert.assertEquals(outcome.getLimit(), 1, "TaxonomyService.getTaxonomy returned invalid limit with Outcome returned type");
		Assert.assertEquals(outcome.getOffset(), 0, "TaxonomyService.getTaxonomy returned invalid offset with Outcome returned type");
		
		
		Assert.assertEquals(outcome.getResults().size(), 1, "TaxonomyService.getTaxonomy returned invalid number of Taxonomys with Outcome returned type");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), taxonomyService.getBuiltInSubjectTaxonomy("en").getId(), "TaxonomyService.getTaxonomy returned invalid taxonomy with Outcome returned type");
	}

	@Test
	public void testGetTaxonomyById(){
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomyTestGetById", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		
		markTaxonomyForRemoval(taxonomy);
		
		Taxonomy taxonomyById = taxonomyService.getTaxonomy(taxonomy.getId(), ResourceRepresentationType.TAXONOMY_INSTANCE,FetchLevel.ENTITY, false);
		
		assertTaxonomiesHaveTheSameIds(taxonomy, taxonomyById);
		
		Taxonomy taxonomyByName = taxonomyService.getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		
		assertTaxonomiesHaveTheSameIds(taxonomy, taxonomyByName);
		assertTaxonomiesHaveTheSameIds(taxonomyByName, taxonomyById);
		
		Taxonomy builtInTaxonomy = taxonomyService.getBuiltInSubjectTaxonomy(null);
		Taxonomy builtInTaxonomyById = taxonomyService.getTaxonomy(builtInTaxonomy.getId(), ResourceRepresentationType.TAXONOMY_INSTANCE,FetchLevel.ENTITY, false);
		Taxonomy builtInTaxonomyByName = taxonomyService.getTaxonomy(builtInTaxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		
		assertTaxonomiesHaveTheSameIds(builtInTaxonomy, builtInTaxonomyById);
		assertTaxonomiesHaveTheSameIds(builtInTaxonomy, builtInTaxonomyByName);
		assertTaxonomiesHaveTheSameIds(builtInTaxonomyById, builtInTaxonomyByName);
	}

	private void assertTaxonomiesHaveTheSameIds(Taxonomy taxonomy,
			Taxonomy taxonomyById) {
		Assert.assertNotNull(taxonomyById, "No taxonomy found for id "+taxonomy.getId());
		Assert.assertNotNull(taxonomyById.getId(), "Taxonomy found for name "+taxonomy.getName() + " but without id");
		
		Assert.assertEquals(taxonomyById.getId(), taxonomy.getId(), "Taxonomy "+taxonomy.getName() + " was saved with id "+taxonomy.getId() + " but method getTaxonomyyId returned "+taxonomyById.getId());
	}

	@Test
	public void testSaveTaxonomyWithSameNameButDifferentCase(){

		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("MyTaxonomy", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		markTaxonomyForRemoval(taxonomy);
		
		//Change case
		taxonomy.setName("myTaxonomy");
		taxonomy = taxonomyService.save(taxonomy);
		
		Assert.assertEquals(taxonomy.getName(), "myTaxonomy", "Taxonomy Name did not change");
		
		taxonomy = taxonomyService.getTaxonomy("myTaxonomy", ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		
		Assert.assertNotNull(taxonomy, "Taxonomy Name did not change case");
		
		Assert.assertEquals(taxonomy.getName(), "myTaxonomy", "Taxonomy Name did not change");
	}

	@Test
	public void testSaveWithVariousNames(){
		
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomyTestName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		markTaxonomyForRemoval(taxonomy);
		
		
		//Now provide invalid system name
		checkInvalidSystemNameSave(taxonomy, "invalid)SystemName");
		checkInvalidSystemNameSave(taxonomy, "invalid((SystemName");
		checkInvalidSystemNameSave(taxonomy, "invalid)SystemNa&me");
		checkInvalidSystemNameSave(taxonomy, "ςδςδ");
		checkInvalidSystemNameSave(taxonomy, "invaliδName+");
		checkInvalidSystemNameSave(taxonomy, "09092");
		checkInvalidSystemNameSave(taxonomy, "09sasas");
		checkInvalidSystemNameSave(taxonomy, "----");
		
		checkValidSystemNameSave(taxonomy, "_09_sdds-02");
		checkValidSystemNameSave(taxonomy, "____");
		checkValidSystemNameSave(taxonomy, "sdsds");
		checkValidSystemNameSave(taxonomy, "S090..92");
		checkValidSystemNameSave(taxonomy, "F090.92");
		checkValidSystemNameSave(taxonomy, "H090..__--92");
		checkValidSystemNameSave(taxonomy, "Y090..92");
		
		
	}
	
	@Test
	public void testGetAllTaxonomiesAsOutcome() throws Throwable{
		
		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy("newTaxonomyForOutcome", cmsRepositoryEntityFactory.newTaxonomy());
	
		newTaxonomy = taxonomyService.save(newTaxonomy);
		
		markTaxonomyForRemoval(newTaxonomy);
		
		CmsOutcome<Taxonomy> outcome = taxonomyService.getAllTaxonomies(ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.ENTITY, false);
		
		Assert.assertNotNull(outcome, "TaxonomyService.getTaxonomy returned null with Outcome returned type");
		
		String outcomeAsXml = taxonomyService.getAllTaxonomies(ResourceRepresentationType.XML, FetchLevel.ENTITY, false);
		
		Assert.assertEquals(outcome.getOffset(), 0, "TaxonomyService.getTaxonomy returned invalid offset with Outcome returned type\n"+TestUtils.prettyPrintXml(outcomeAsXml));
		
		for (Taxonomy tax : outcome.getResults()){
			final Taxonomy taxReloaded = taxonomyService.getTaxonomy(tax.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
			
			Assert.assertEquals(tax.getId(), taxReloaded.getId(),
					"TaxonomyService.getTaxonomy returned invalid taxonomy "+taxReloaded.getName()+" with Outcome returned type\n"+TestUtils.prettyPrintXml(outcomeAsXml));
		}
		
	}
	
	@Test
	public void testGetAllTaxonomiesXmlorJSON() throws Throwable{
		
		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy("newTaxonomyForXML", cmsRepositoryEntityFactory.newTaxonomy());

		newTaxonomy.clearLocalizedLabels();
		newTaxonomy = taxonomyService.save(newTaxonomy);
		
		markTaxonomyForRemoval(newTaxonomy);
		
		String allTaxonomies = null;
		
		ResourceRepresentationType<?>  resourceRepresentationTypeForLogger = null;

		try{
			final List<ResourceRepresentationType<String>> asList = Arrays.asList(ResourceRepresentationType.XML, ResourceRepresentationType.JSON);
			for (ResourceRepresentationType<?>  resourceRepresentationType : asList){
			
				resourceRepresentationTypeForLogger = resourceRepresentationType;
				
				if (resourceRepresentationType.equals(ResourceRepresentationType.JSON)){
					allTaxonomies = taxonomyService.getAllTaxonomies(ResourceRepresentationType.JSON, FetchLevel.ENTITY, false);
				}					
				else if (resourceRepresentationType.equals(ResourceRepresentationType.XML)){
					allTaxonomies = taxonomyService.getAllTaxonomies(ResourceRepresentationType.XML, FetchLevel.ENTITY, false);
				}

				Assert.assertNotNull(allTaxonomies, "Taxonomies were not exported");

				allTaxonomies = StringUtils.deleteWhitespace(allTaxonomies);
				
				final String expectedRepresentationOfNewTaxonomyName = generateExpectedValueForOutputFormat(CmsBuiltInItem.Name.getLocalPart(),newTaxonomy.getName(), resourceRepresentationType);
				Assert.assertTrue(allTaxonomies.contains(expectedRepresentationOfNewTaxonomyName), 
						"Taxonomy "+ newTaxonomy.getName()+" was not exported. Did not find \n "+expectedRepresentationOfNewTaxonomyName+"in result \n"+ allTaxonomies);
				
				final String expectedRepresentationOfSubjectTaxonomyName = generateExpectedValueForOutputFormat(CmsBuiltInItem.Name.getLocalPart(),getSubjectTaxonomy().getName(), resourceRepresentationType);
				
				Assert.assertTrue(allTaxonomies.contains(expectedRepresentationOfSubjectTaxonomyName), 
						"Taxonomy "+ getSubjectTaxonomy().getName()+" was not exported. Did not find \n "+expectedRepresentationOfSubjectTaxonomyName+"in result \n"+ allTaxonomies);
			}
			
		}
		catch(Throwable e){
			try{
				logger.error(resourceRepresentationTypeForLogger + " - Initial \n{}",TestUtils.prettyPrintXml(allTaxonomies));
			}
			catch(Exception e1){
				logger.error(resourceRepresentationTypeForLogger + "Initial \n{}",allTaxonomies);
			}
			
			logger.error("All taxonomies XML \n{}", taxonomyService.getAllTaxonomies(ResourceRepresentationType.XML, FetchLevel.ENTITY, prettyPrint));
			
			throw e;
		}	
	}
	
	@Test
	public void testGetTaxonomyXmlorJSON() throws Throwable{
		
		Topic firstRootTopic =  createRootTopicForSubjectTaxonomy("taxonomyTestExportXmlJSON");
		Topic secondRootTopic =  createRootTopicForSubjectTaxonomy("taxonomyTestExportXmlJSON2");
		Topic thirdTopic =  createTopic("taxonomyTestExportXmlJSON3", secondRootTopic);

		String taxonomyExportFromJAXB = null;
		String taxonomyExportFromService = null;
		
		List<ResourceRepresentationType<String>> outputs = Arrays.asList(ResourceRepresentationType.JSON, ResourceRepresentationType.XML);

		try{
			
			ImportConfiguration configuration = ImportConfiguration.taxonomy()
					.persist(PersistMode.DO_NOT_PERSIST)
					.build();

			for (ResourceRepresentationType<String> output : outputs){
				
				Taxonomy subjectTaxonomy = taxonomyService.getTaxonomy(Taxonomy.SUBJECT_TAXONOMY_NAME, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
				String taxonomyName = subjectTaxonomy.getName();
				
				//Export taxonomy from JAXB
				if (output.equals(ResourceRepresentationType.XML)){
					taxonomyExportFromJAXB =  subjectTaxonomy.xml(prettyPrint);
					taxonomyExportFromService = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN,prettyPrint);
				}
				else if (output.equals(ResourceRepresentationType.JSON)){
					taxonomyExportFromJAXB = subjectTaxonomy.json(prettyPrint);
					taxonomyExportFromService = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN,prettyPrint);
				}

				//Create instance from Service export
				Taxonomy taxonomyFromService = importDao.importTaxonomy(taxonomyExportFromService, configuration);
				
				//Compare two taxonomies
				repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromService, true, false);
			
				//Now check export of taxonomy tree
				subjectTaxonomy.getRootTopics();
			
				if (output.equals(ResourceRepresentationType.XML)){
					taxonomyExportFromJAXB =  subjectTaxonomy.xml(prettyPrint);
					taxonomyExportFromService = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.XML, FetchLevel.FULL,prettyPrint);
				}
				else if (output.equals(ResourceRepresentationType.JSON)){
					taxonomyExportFromJAXB = subjectTaxonomy.json(prettyPrint);
					taxonomyExportFromService = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.JSON, FetchLevel.FULL,prettyPrint);
				}

				//Create instance from Service export
				taxonomyFromService = importDao.importTaxonomy(taxonomyExportFromService, configuration); 

				//Compare two taxonomies
				repositoryContentValidator.compareTaxonomies(subjectTaxonomy, taxonomyFromService, true, true);
				
			}
		}
		catch(Throwable e){
			try{
				logger.error("From JAXB\n{}",TestUtils.prettyPrintXml(taxonomyExportFromJAXB));
				logger.error("From service \n{}",TestUtils.prettyPrintXml(taxonomyExportFromService));

			}
			catch(Exception e1){
				logger.error("From JAXB\n{}",taxonomyExportFromJAXB);
				logger.error("From service \n{}",taxonomyExportFromService);

			}
			throw e;

		}	
	}

	@Test
	public void testImportTaxonomyFromXML() throws Throwable{

		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy("newTaxonomy", 
				cmsRepositoryEntityFactory.newTaxonomy());
		

		Topic topic = JAXBTestUtils.createTopic("topicName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildImport", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		topic.addChild(childTopic1);
		
		topic.setTaxonomy(newTaxonomy);
		newTaxonomy.addRootTopic(topic);
		
		logger.debug("Importing XML {}",newTaxonomy.xml(prettyPrint));
		
		ImportConfiguration configuration = ImportConfiguration.taxonomy()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.build();

		Taxonomy importedTaxonomy = importDao.importTaxonomy(newTaxonomy.xml(prettyPrint), configuration);

		logger.debug("XML of imported taxonomy {}", importedTaxonomy.xml(prettyPrint));
		
		validateImportedTaxonomy(importedTaxonomy, true);

		//Assert that all topic owners are system user
		RepositoryUser systemUser = getSystemUser();
		
		checkTopicOwner(importedTaxonomy.getRootTopics(), systemUser);

		markTaxonomyForRemoval(importedTaxonomy);

	}
	
	private void checkTopicOwner(List<Topic> topics, RepositoryUser systemUser) {
		if (CollectionUtils.isNotEmpty(topics)){
			for (Topic topic : topics){
				
				Assert.assertEquals(topic.getOwner().getId(), systemUser.getId());
				Assert.assertEquals(topic.getOwner().getExternalId(), systemUser.getExternalId());

				if (topic.isChildrenLoaded()){
					checkTopicOwner(topic.getChildren(), systemUser);
				}
			}
		}
	}

	@Test
	public void testImportSubjectTaxonomyFromXML() throws Throwable{

		Taxonomy newTaxonomy = JAXBTestUtils.createTaxonomy(Taxonomy.SUBJECT_TAXONOMY_NAME, 
				cmsRepositoryEntityFactory.newTaxonomy());

		ImportConfiguration configuration = ImportConfiguration.taxonomy()
				.persist(PersistMode.PERSIST_MAIN_ENTITY)
				.build();

		Taxonomy importedTaxonomy = importDao.importTaxonomy(newTaxonomy.xml(prettyPrint), configuration);

		validateImportedTaxonomy(importedTaxonomy, false);

		//Change name to Subject Taxonomy
		try{
			importedTaxonomy.setName("NEWTAXONOMY");

			TestLogPolicy.setLevelForLogger(Level.FATAL, ImportBean.class.getName());
			TestLogPolicy.setLevelForLogger(Level.FATAL, Deserializer.class.getName());
			importedTaxonomy = importDao.importTaxonomy(importedTaxonomy.xml(prettyPrint), configuration);
			TestLogPolicy.setDefaultLevelForLogger(ImportBean.class.getName());
			TestLogPolicy.setDefaultLevelForLogger(Deserializer.class.getName());
			
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Renamimg bccms:subjectTaxonomy is prohibited"), e.getMessage());
			
		}


	}


	private void validateImportedTaxonomy(Taxonomy importedTaxonomy, boolean assertRootTopics) throws Throwable {

		//check that it has been saved
		Taxonomy checkTaxonomy = taxonomyService.getTaxonomy(importedTaxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);

		Assert.assertNotNull(checkTaxonomy, "Taxonomy "+importedTaxonomy.getName()+ " has not been imported at all");

		try{
			JAXBTestUtils.assertTaxonomyiesAreTheSame(importedTaxonomy, checkTaxonomy, assertRootTopics);
		}
		catch(Throwable t){
			logger.error("Source \n{} \n Target \n{}", 
					TestUtils.prettyPrintXml(importedTaxonomy.xml(prettyPrint)), 
					TestUtils.prettyPrintXml(checkTaxonomy.xml(prettyPrint)));
			throw t;
		}
	}


	@Test
	public void testDelete() {

		loginToTestRepositoryAsSystem();

		//1. Check that if system taxonomy and or any folksonomy is provided
		//	 an exception is thrown
		checkExceptionIsThrownIfTaxonomyToBeDeletedIsBuiltIn(Taxonomy.SUBJECT_TAXONOMY_NAME);
		checkExceptionIsThrownIfTaxonomyToBeDeletedIsAFolksonomy();

		// create new taxonomy & delete it using its id 
		// also verify that taxonomy is deleted
		Taxonomy aNewTaxonomy = JAXBTestUtils.createTaxonomy("aTaxonomyToDelete", 
				cmsRepositoryEntityFactory.newTaxonomy());
		aNewTaxonomy = taxonomyService.save(aNewTaxonomy);
		
		boolean isTaxonomyDeleted = taxonomyService.deleteTaxonomyTree(aNewTaxonomy.getId());

		Taxonomy retrievedTaxonomy = taxonomyService.getTaxonomy(
					aNewTaxonomy.getName(),
					ResourceRepresentationType.TAXONOMY_INSTANCE,
					FetchLevel.ENTITY, false);
		
		boolean taxonomySuccessfullyDeleted = isTaxonomyDeleted && (retrievedTaxonomy == null);
		Assert.assertTrue(taxonomySuccessfullyDeleted);
		
		
		// create another new taxonomy & delete it with using its name
		// also verify that taxonomy is deleted
		
		Taxonomy anotherNewTaxonomy = JAXBTestUtils.createTaxonomy("anotherTaxonomyToDelete", 
				cmsRepositoryEntityFactory.newTaxonomy());
		anotherNewTaxonomy = taxonomyService.save(anotherNewTaxonomy);
		
		isTaxonomyDeleted = taxonomyService.deleteTaxonomyTree(anotherNewTaxonomy.getName());
		
		retrievedTaxonomy = taxonomyService.getTaxonomy(anotherNewTaxonomy.getName(), 
					ResourceRepresentationType.TAXONOMY_INSTANCE, 
					FetchLevel.ENTITY, false);
		taxonomySuccessfullyDeleted = isTaxonomyDeleted && (retrievedTaxonomy == null); 
		Assert.assertTrue(taxonomySuccessfullyDeleted);
		
		// try to delete an already deleted taxonomy
		retrievedTaxonomy = taxonomyService.getTaxonomy(anotherNewTaxonomy.getName(), 
					ResourceRepresentationType.TAXONOMY_INSTANCE, 
					FetchLevel.ENTITY, false);
			
		Assert.assertNull(retrievedTaxonomy, "Taxonomy "+anotherNewTaxonomy.getName()+ " was not deleted");
			
		isTaxonomyDeleted = taxonomyService.deleteTaxonomyTree(anotherNewTaxonomy.getName());
		
		Assert.assertFalse(isTaxonomyDeleted, "Taxonomy "+anotherNewTaxonomy.getName()+ " was not deleted");
		
		// create a taxonomy, add a topic, and then delete the taxonomy by its name
		// verify that both the taxonomy and topic are deleted 
		
		Taxonomy taxonomyWithTopic = JAXBTestUtils.createTaxonomy("taxonomyWithTopic", 
				cmsRepositoryEntityFactory.newTaxonomy());
		taxonomyWithTopic = taxonomyService.save(taxonomyWithTopic);
		
		
		Topic aTopic = JAXBTestUtils.createTopic("aTopicOfTaxonomy", cmsRepositoryEntityFactory.newTopic(), getSystemUser());
		aTopic.setTaxonomy(taxonomyWithTopic);
		aTopic = topicService.save(aTopic);
		
		isTaxonomyDeleted = taxonomyService.deleteTaxonomyTree(taxonomyWithTopic.getName());
		
		retrievedTaxonomy = taxonomyService.getTaxonomy(taxonomyWithTopic.getName(), 
					ResourceRepresentationType.TAXONOMY_INSTANCE, 
					FetchLevel.ENTITY, false);
			
		Topic retrievedTopic = topicService.getTopic(aTopic.getName(), ResourceRepresentationType.TOPIC_INSTANCE,
					FetchLevel.ENTITY, false);
			
		taxonomySuccessfullyDeleted = isTaxonomyDeleted && (retrievedTaxonomy == null) && (retrievedTopic == null); 
		Assert.assertTrue(taxonomySuccessfullyDeleted);
	}

	private void checkExceptionIsThrownIfTaxonomyToBeDeletedIsAFolksonomy() {

		List<RepositoryUser> users = repositoryUserService.searchRepositoryUsers(CmsCriteriaFactory.newRepositoryUserCriteria());

		for (RepositoryUser user : users){
			String taxonomyName = user.getFolksonomy().getName();
			try{
				taxonomyService.deleteTaxonomyTree(user.getFolksonomy().getId());

				Assert.assertTrue(1==2, "Taxonomy with the reserved name "+ taxonomyName+ " has been deleted");
			}
			catch(CmsException e){
				Assert.assertTrue(e.getMessage()!= null && e.getMessage().contains("Taxonomy "+ taxonomyName+ " is a reserved taxonomy and cannot be deleted"), 
						" Invalid error message while deleting a reserved taxonomy with name "+taxonomyName);

				continue;
			}
		}

	}


	private void checkExceptionIsThrownIfTaxonomyToBeDeletedIsBuiltIn(String taxonomyName) {

		Taxonomy taxonomyToBeDeleted = taxonomyService.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);

		try{
			taxonomyService.deleteTaxonomyTree(taxonomyToBeDeleted.getId());

			Assert.assertTrue(1==2, "Taxonomy with the reserved name "+ taxonomyName+ " has been deleted");
		}
		catch(CmsException e){
			Assert.assertTrue(e.getMessage()!= null && e.getMessage().contains("Taxonomy "+ taxonomyName+ " is a reserved taxonomy and cannot be deleted"), 
					" Invalid error message while deleting a reserved taxonomy with name "+taxonomyName + "\n"+e.getMessage());

		}

	}


	@Test
	public void testSave() {

		checkExceptionIsThrownIfTaxonomyNameIsReservedAndTaxonomyIsNew(Taxonomy.SUBJECT_TAXONOMY_NAME);
		checkExceptionIsThrownIfTaxonomyNameIsReservedAndTaxonomyIsNew(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME);

		checkExceptionIsThrownIfReservedTaxonomyIsRefactored(taxonomyService.getBuiltInSubjectTaxonomy("en"));

		List<RepositoryUser> users = repositoryUserService.searchRepositoryUsers(CmsCriteriaFactory.newRepositoryUserCriteria());

		for (RepositoryUser user : users){
			checkExceptionIsThrownIfReservedTaxonomyIsRefactored(user.getFolksonomy());
		}


		// 1. Check that taxonomy is saved
		// 2. Check if an exception is thrown if taxonomy name already exists
		// 3. Check if an exception is thrown if a folksonomy name is provided
		// 4. Check if an exception is thrown if no name is provided
		// 5. Check if an exception is thrown if a system taxonomy is provided
		//    without id.
		// 6. Check if an exception is thrown if taxonomy name is not a valid XML name
		// 7. Check that only localized labels are updated if user saves system taxonomy
		// 8. Check that taxonomy is successfully moved in case a new name is provided

	}
	
	@Test
	public void testImportXMLofTaxonomiesProvidedWithTheDistribution() throws IOException{
		Resource taxonomiesHomeDir = new ClassPathResource("/taxonomies");
		
		Assert.assertTrue(taxonomiesHomeDir.exists(), "Home directory of the taxonomies provided in the distribution does not exist in "+taxonomiesHomeDir.getURI().toString());
		
		File[] taxonomyXmls = taxonomiesHomeDir.getFile().listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name != null && name.endsWith(".xml");
			}
		});
		
		Assert.assertTrue(taxonomyXmls.length > 0, "Home directory of the taxonomies provided in the distribution '"+taxonomiesHomeDir.getURI().toString()+"' does not contain any xml file");

		for (File taxonomyXML : taxonomyXmls){
			
			Taxonomy taxonomy = taxonomyService.save(FileUtils.readFileToString(taxonomyXML, "UTF-8"));
			markTaxonomyForRemoval(taxonomy);
		}
		
	}

	private void checkExceptionIsThrownIfReservedTaxonomyIsRefactored(
			Taxonomy taxonomy) {

		String taxonomyName = new String(taxonomy.getName());

		try{

			taxonomy.setName("NewName");

			taxonomyService.save(taxonomy);

			Assert.assertTrue(1==2, "Reserved Taxonomy "+ taxonomyName+" has changed name to 'NewName'");
		}
		catch(CmsException e){
			Assert.assertTrue(e.getMessage()!= null && e.getMessage().contains("Renamimg "+taxonomyName+" is prohibited"), 
					" Invalid error message while saving a new taxonomy with reserved name "+taxonomyName +" \n '"+
					e.getMessage() + "'");

		}


	}

	private void checkExceptionIsThrownIfTaxonomyNameIsReservedAndTaxonomyIsNew(String taxonomyName) {

		Taxonomy taxonomy = cmsRepositoryEntityFactory.newTaxonomy();
		taxonomy.setName(taxonomyName);

		try{
			taxonomy = taxonomyService.save(taxonomy);

			Assert.assertEquals(taxonomy.getId(), getSubjectTaxonomy().getId(), "A new taxonomy is saved with the reserved name "+ taxonomyName);
		}
		catch(CmsException e){
			Assert.assertTrue(e.getMessage()!= null && e.getMessage().contains("Taxonomy name "+ taxonomyName + " already exists"), 
					" Invalid error message while saving a new taxonomy with reserved name "+taxonomyName +  " \n'"+
					e.getMessage()+"'");

		}



	}


	private void checkInvalidSystemNameSave(Taxonomy taxonomy,
			String systemName) {
		
		try{
			taxonomy.setName(systemName);
			
			taxonomy = taxonomyService.save(taxonomy);
			
			
			Assert.assertEquals(1, 2, 
					"Taxonomy was saved with invalid system name "+systemName);
			
		}
		catch(CmsException e){
		
			String message = e.getMessage();
			
			Throwable t = e;
			
			while (t.getCause() != null){
				message = t.getCause().getMessage();
				
				t = t.getCause();
			}
			
			Assert.assertTrue(message.equals("Taxonomy name "+systemName+" is not a valid XML name.Check XML Namespaces recommendation [4]") || 
					message.equals("Taxonomy name '"+systemName+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP), 
					"Invalid exception "+ e.getMessage());
		}
	}
	
	private void checkValidSystemNameSave(Taxonomy taxonomy,
			String systemName) {
		
		taxonomy.setName(systemName);
			
		taxonomy = taxonomyService.save(taxonomy);
			
	}
}
