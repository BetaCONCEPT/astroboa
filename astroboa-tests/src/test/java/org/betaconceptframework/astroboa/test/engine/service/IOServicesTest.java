/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.test.engine.service;

import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IOServicesTest extends AbstractRepositoryTest{

	
	@Test
	public void testIORepositoryUsers() throws Throwable{
		loginToTestRepositoryAsSystem();
		
		RepositoryUser repUser = cmsRepositoryEntityFactory.newRepositoryUser();
		repUser.setExternalId("testIOUser");
		repUser.setLabel("testIOUser");
		
		repUser = repositoryUserService.save(repUser);
		
		markRepositoryUserForRemoval(repUser);
		
		serializeUsingJCR(CmsEntityType.REPOSITORY_USER);

		ImportConfiguration configuration = ImportConfiguration.repositoryUser()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.repositoryUser()
				.prettyPrint(false)
				.representationType(ResourceRepresentationType.XML)
				.build();

		assertIOOfEntity(CmsEntityType.REPOSITORY_USER, configuration, serializationConfiguration);
		
		
	}
	
	@Test
	public void testIOTaxonomies() throws Throwable{
		loginToTestRepositoryAsSystem();
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("topicExportTaxonomy",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		
		Topic topic = JAXBTestUtils.createTopic("firstChildExport", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),getSystemUser());
		topic.setTaxonomy(taxonomy);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondChildExport", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),getSystemUser());
		secondTopic.setTaxonomy(taxonomy);
		
		taxonomy.addRootTopic(topic);
		topic.addChild(secondTopic);
		
		topic = topicService.save(topic);
		
		Topic firstSubjectTopic = JAXBTestUtils.createTopic("firstSubjectChildExport", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),getSystemUser());
		
		firstSubjectTopic.setTaxonomy(taxonomyService.getBuiltInSubjectTaxonomy("en"));
		
		firstSubjectTopic = topicService.save(firstSubjectTopic);

		markTopicForRemoval(firstSubjectTopic);
		markTaxonomyForRemoval(taxonomy);
		
		
		serializeUsingJCR(CmsEntityType.TAXONOMY);

		ImportConfiguration configuration = ImportConfiguration.taxonomy()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.taxonomy()
				.prettyPrint(false)
				.representationType(ResourceRepresentationType.XML)
				.build();

		assertIOOfEntity(CmsEntityType.TAXONOMY, configuration, serializationConfiguration);
	}
	
	@Test
	public void testIOOrganizationSpace() throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		Space space = JAXBTestUtils.createSpace("spaceName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),getSystemUser());
		
		Space childSpace1 = JAXBTestUtils.createSpace("firstChildIOSpace",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),getSystemUser());
		childSpace1.setOwner(space.getOwner());
		space.addChild(childSpace1);
		space.setParent(spaceService.getOrganizationSpace());
		
		space = spaceService.save(space);
		
		markSpaceForRemoval(space);
		
		serializeUsingJCR(CmsEntityType.ORGANIZATION_SPACE);

		ImportConfiguration configuration = ImportConfiguration.space()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.space()
				.prettyPrint(false)
				.representationType(ResourceRepresentationType.XML)
				.build();

		assertIOOfEntity(CmsEntityType.ORGANIZATION_SPACE, configuration, serializationConfiguration);

		
	}
	
	@Test
	public void testIOContentObjectWithNormalReference() throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		//Create two objects with reference to one another
		ContentObject contentObject = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestWithNormalReference");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		//During the population of this object's properties, any property of type ObjectReference will have for value 
		//the above object
		ContentObject contentObject2 = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestWithNormalReference2");
		contentObject2 = contentService.save(contentObject2, false, true, null);
		markObjectForRemoval(contentObject2);

		//Export and import without reference one another
		serializeUsingJCR(CmsEntityType.OBJECT);
		
		ImportConfiguration configuration = ImportConfiguration.object()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.version(false)
				.updateLastModificationTime(true)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
				.prettyPrint(false)
				.serializeBinaryContent(true)
				.representationType(ResourceRepresentationType.XML)
				.build();

		assertIOOfEntity(CmsEntityType.OBJECT, configuration, serializationConfiguration);
		
	}
	
	@Test
	public void testIOContentObjectAndDoNotIgnoreMissingReference() throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		//Create two objects with reference to one another
		ContentObject contentObject = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestAndDoNotIgnoreMissingReference");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		//During the population of this object's properties, any property of type ObjectReference will have for value 
		//the above object
		ContentObject contentObject2 = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestAndDoNotIgnoreMissingReference2");
		contentObject2 = contentService.save(contentObject2, false, true, null);
		markObjectForRemoval(contentObject2);

		//Relate first object with the second one
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObject")).setSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);

		contentObject = contentService.save(contentObject, false, true, null);
		
		serializeUsingJCR(CmsEntityType.OBJECT);
		
		ImportConfiguration configuration = ImportConfiguration.object()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.version(false)
				.updateLastModificationTime(true)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
				.prettyPrint(false)
				.serializeBinaryContent(true)
				.representationType(ResourceRepresentationType.XML)
				.build();
		
		try{
			
			TestLogPolicy.setLevelForLogger(Level.FATAL, Deserializer.class.getName());
			TestLogPolicy.setLevelForLogger(Level.FATAL, ImportBean.class.getName());
			assertIOOfEntity(CmsEntityType.OBJECT, configuration, serializationConfiguration);
			TestLogPolicy.setDefaultLevelForLogger(Deserializer.class.getName());
			TestLogPolicy.setDefaultLevelForLogger(ImportBean.class.getName());
		}
		catch (Exception e){
			
			Throwable temp = e;
			
			if (! (temp instanceof CmsException)){
				temp = temp.getCause();
			}
			
			Assert.assertEquals(temp.getMessage(), "No content object found in repository with id "+contentObject2.getId()+" and name "+contentObject2.getSystemName(), "Invalid exception message ");
		}
		
	}
	
	@Test
	public void testIOContentObjectAndIgnoreMissingReference() throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		//Create two objects with reference to one another
		ContentObject contentObject = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestAndIgnoreMissingReference");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		//During the population of this object's properties, any property of type ObjectReference will have for value 
		//the above object
		ContentObject contentObject2 = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTestAndIgnoreMissingReference2");
		contentObject2 = contentService.save(contentObject2, false, true, null);
		markObjectForRemoval(contentObject2);

		//Relate first object with the second one
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObject")).setSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);

		contentObject = contentService.save(contentObject, false, true, null);
		
		serializeUsingJCR(CmsEntityType.OBJECT);
		
		ImportConfiguration configuration = ImportConfiguration.object()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.version(false)
				.updateLastModificationTime(true)
				.saveMissingObjectReferences(true)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
				.prettyPrint(false)
				.serializeBinaryContent(true)
				.representationType(ResourceRepresentationType.XML)
				.build();
		
		assertIOOfEntity(CmsEntityType.OBJECT, configuration, serializationConfiguration);
	}
	
	@Test
	public void testIOContentObjectUsingCriteria() throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();

		//Create Topics
		Topic topic = JAXBTestUtils.createTopic("firstTopicForIOContentObjectUsingCriteria", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		topic.setOwner(getSystemUser());
		topic.setTaxonomy(subjectTaxonomy);

		Topic childTopic1 = JAXBTestUtils.createTopic("secondTopicForIOContentObjectUsingCriteria", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		childTopic1.setTaxonomy(subjectTaxonomy);

		topic.addChild(childTopic1);

		topic = topicService.save(topic);
		markTopicForRemoval(topic);
		
		
		//Another taxonomy
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomyForIOContentObjectUsingCriteria", cmsRepositoryEntityFactory.newTaxonomy());
		Topic topicNew = JAXBTestUtils.createTopic("firstTopicOfANewTaxonomyForIOContentObjectUsingCriteria", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		topicNew.setOwner(getSystemUser());
		topicNew.setTaxonomy(taxonomy);
		taxonomy = taxonomyService.save(taxonomy);
		markTaxonomyForRemoval(taxonomy);

		
		//Create two objects with reference to one another
		ContentObject contentObject = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectUsingCriteria");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		//During the population of this object's properties, any property of type ObjectReference will have for value 
		//the above object
		ContentObject contentObject2 = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectUsingCriteria2");
		contentObject2 = contentService.save(contentObject2, false, true, null);
		markObjectForRemoval(contentObject2);

		//Relate first object with the second one
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObject")).setSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
		((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
		
		//Relate to a topic from the new taxonomy
		((TopicReferenceProperty)contentObject.getCmsProperty("simpleTopicMultiple")).addSimpleTypeValue(topicNew);
		((TopicReferenceProperty)contentObject.getCmsProperty("simpleTopicMultiple")).addSimpleTypeValue(childTopic1);

		contentObject = contentService.save(contentObject, false, true, null);
		
		ImportConfiguration configuration = ImportConfiguration.object()
				.persist(PersistMode.PERSIST_ENTITY_TREE)
				.version(false)
				.updateLastModificationTime(true)
				.saveMissingObjectReferences(true)
				.build();

		SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
				.prettyPrint(false)
				.serializeBinaryContent(true)
				.representationType(ResourceRepresentationType.XML)
				.build();
		
		ContentObjectCriteria objectCriteria = CmsCriteriaFactory.newContentObjectCriteria(TEST_CONTENT_TYPE);
		objectCriteria.addSystemNameContainsCriterion("ioContentObjectUsingCriteria*");
		
		assertIOOfObjectsUsingCriteria(objectCriteria, configuration, serializationConfiguration);
	}
	
}
