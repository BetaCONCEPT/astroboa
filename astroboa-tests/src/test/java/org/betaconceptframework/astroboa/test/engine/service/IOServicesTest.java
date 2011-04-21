/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
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
		
		addEntityToBeDeletedAfterTestIsFinished(repUser);
		
		serializeUsingJCR(CmsEntityType.REPOSITORY_USER);

		assertIOOfEntity(CmsEntityType.REPOSITORY_USER);
		
		
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

		addEntityToBeDeletedAfterTestIsFinished(firstSubjectTopic);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);
		
		
		serializeUsingJCR(CmsEntityType.TAXONOMY);

		assertIOOfEntity(CmsEntityType.TAXONOMY);
	}
	
	@Test
	public void testIOOrganizationSpace() throws Throwable{
		Space space = JAXBTestUtils.createSpace("spaceName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),getSystemUser());
		
		Space childSpace1 = JAXBTestUtils.createSpace("firstChildIOSpace",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),getSystemUser());
		childSpace1.setOwner(space.getOwner());
		space.addChild(childSpace1);
		
		Space organizationSpace = spaceService.getOrganizationSpace();
		
		organizationSpace.addChild(space);
		
		space = spaceService.save(space);
		
		addEntityToBeDeletedAfterTestIsFinished(space);
		
		serializeUsingJCR(CmsEntityType.ORGANIZATION_SPACE);

		assertIOOfEntity(CmsEntityType.ORGANIZATION_SPACE);
	}
	
	@Test
	public void testIOContentObject() throws Throwable{
		
		//Create two objects with reference to one another
		ContentObject contentObject = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTest", false);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		ContentObject contentObject2 = createContentObjectAndPopulateAllProperties(getSystemUser(), "ioContentObjectTest2", false);
		contentObject2 = contentService.save(contentObject2, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject2);

		//Export and import without reference one another
		serializeUsingJCR(CmsEntityType.CONTENT_OBJECT);
		assertIOOfEntity(CmsEntityType.CONTENT_OBJECT);
		
		//	Now relate eahc other and try to reimport  		
			/*((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObject")).setSimpleTypeValue(contentObject2);
			((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
			((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(contentObject2);
			contentObject = contentService.save(contentObject, false, true, null);
	
			exportUsingJCR(CmsEntityType.CONTENT_OBJECT);
			assertIOOfEntity(CmsEntityType.CONTENT_OBJECT);
		*/
		
	}
	
	@Test(dependsOnMethods={"testIORepositoryUsers","testIOTaxonomies", "testIOOrganizationSpace", "testIOContentObject"})
	public void testIOContent() throws Throwable{

		serializeUsingJCR(CmsEntityType.REPOSITORY);

		assertIOOfEntity(CmsEntityType.REPOSITORY);
		
	}
}
