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


import java.io.IOException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserServiceTest extends AbstractRepositoryTest{
	
	
	@Test
	public void testDeleteRepositoryUserAndReplaceWithSystem() throws IOException, RepositoryException{
		
		//Create user
		final String externalId = "test-user-delete-and-replace-with-system";
		
		createUser(externalId);
		
		RepositoryUser user = repositoryUserService.getRepositoryUser(externalId);
		
		//Create topic
		Topic topic = JAXBTestUtils.createTopic("test-user-delete-and-replace-with-system-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				user);

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Create space
		Space space = JAXBTestUtils.createSpace("test-user-delete-and-replace-with-system-space", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				user);
		space.setParent(getOrganizationSpace());
		space = spaceService.save(space);
		addEntityToBeDeletedAfterTestIsFinished(space);
		
		//Create content object
		ContentObject contentObject = createContentObject(user, "test-user-delete-and-replace-with-system-contentObject", false);
		contentObject = contentService.save(contentObject, false, true, null);
	
		//Delete user
		repositoryUserService.deleteRepositoryUser(user.getId(), getSystemUser());
		
		//Check for user, topic, space and contentObject existence in either jcr or Astroboa level
		try{
			Node userNode = getSession().getNodeByUUID(user.getId());
			Assert.assertNull(userNode, "Repository User "+user.getExternalId() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), user.getId(), "Invalid ItemNotFoundException message");
		}
		
		//Topic is not deleted because its owner is ALWAYS SYSTEM USER
		Node topicNode = getSession().getNodeByUUID(topic.getId());
		Assert.assertNotNull(topicNode, "Topic "+topic.getName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(topicNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString(), getSystemUser().getId(), 
				"Topic "+topic.getName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");

		Node spaceNode = getSession().getNodeByUUID(space.getId());
		Assert.assertNotNull(spaceNode, "Space "+space.getName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(spaceNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString(), getSystemUser().getId(), 
					"Space "+space.getName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");

		Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
		Assert.assertNotNull(contentObjectNode, "ContentObject "+contentObject.getSystemName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString(), getSystemUser().getId(), 
				"ContentObject "+contentObject.getSystemName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");
		
		//Astroboa
		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		Assert.assertNotNull(contentObjectReloaded, "ContentObject "+contentObject.getSystemName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(contentObjectReloaded.getOwner().getId(), getSystemUser().getId(), 
				"ContentObject "+contentObject.getSystemName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");

		//Check with Space entity 
		Space spaceReloaded = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNotNull(spaceReloaded, "Space "+space.getName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(spaceReloaded.getOwner().getId(), getSystemUser().getId(), 
					"Space "+space.getName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");

		//Check with Astroboa Service
		spaceService.getContentObjectIdsWhichResideInSpace(space.getId());
		spaceService.getCountOfContentObjectIdsWhichResideInSpace(space.getId());

		//Check with Topic entity 
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNotNull(topicReloaded, "Topic "+topic.getName() + " was deleted when user "+user.getExternalId() + " was deleted");
		Assert.assertEquals(topicReloaded.getOwner().getId(), getSystemUser().getId(), 
				"Topic "+topic.getName() + " owner is not SYSTEM user when user "+user.getExternalId() + " was deleted");
		
		
		RepositoryUser userReloaded = repositoryUserService.getRepositoryUser(externalId);
		
		Assert.assertNull(userReloaded, "Repository User "+user.getExternalId() + " was not deleted");
		
		//Finally delete content object of type personObject which represents the user
		loginToRepositoryRepresentingIdentityStoreAsSystem();
		deleteUser(externalId);
		loginToTestRepositoryAsSystem();

	}
	
	@Test
	public void testDeleteRepositoryUserAndAllAssociatedEntities() throws IOException, RepositoryException{
		
		//Create user
		final String externalId = "test-user-delete-all";
		
		createUser(externalId);
		
		RepositoryUser user = repositoryUserService.getRepositoryUser(externalId);
		
		//Create topic which will be saved under the Subject taxonomy
		Topic topicInSubjectTaxonomy = JAXBTestUtils.createTopic("test-user-delete-all-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				user);

		topicInSubjectTaxonomy = topicService.save(topicInSubjectTaxonomy);
		addEntityToBeDeletedAfterTestIsFinished(topicInSubjectTaxonomy);

		//Create topic which will be saved under user folskonomy
		Topic topicInFolksonomy = JAXBTestUtils.createTopic("test-user-delete-all-topic-folskonomy", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				user);
		topicInFolksonomy.setTaxonomy(user.getFolksonomy());

		topicInFolksonomy = topicService.save(topicInFolksonomy);

		//Create space which will be saved under the Organization Space
		Space spaceInOrganizationSpace = JAXBTestUtils.createSpace("test-user-delete-all-space", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				user);
		spaceInOrganizationSpace.setParent(getOrganizationSpace());
		spaceInOrganizationSpace = spaceService.save(spaceInOrganizationSpace);
		addEntityToBeDeletedAfterTestIsFinished(spaceInOrganizationSpace);

		//Create space which will be saved under the User Space
		Space spaceInUserSpace = JAXBTestUtils.createSpace("test-user-delete-all-user-space", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				user);
		
		spaceInUserSpace.setParent(user.getSpace());
		spaceInUserSpace = spaceService.save(spaceInUserSpace);

		//Create content object
		ContentObject contentObject = createContentObject(user, "test-user-delete-all-contentObject", false);
		contentObject = contentService.save(contentObject, false, true, null);
	
		//Delete user
		repositoryUserService.deleteRepositoryUserAndOwnedObjects(user.getId());
		
		/*
		 * Check that no JCR node exists for RepositoryUser and that no RepositoryUser instance
		 * is returned by Astroboa Services
		 */
		try{
			Node userNode = getSession().getNodeByUUID(user.getId());
			Assert.assertNull(userNode, "Repository User "+user.getExternalId() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), user.getId(), "Invalid ItemNotFoundException message");
		}
		
		RepositoryUser userReloaded = repositoryUserService.getRepositoryUser(externalId);
		Assert.assertNull(userReloaded, "Repository User "+user.getExternalId() + " was not deleted");

		/*
		 * Check that no JCR node exists for Space created in OrganizationSpace or in User's Space and that no Space instance
		 * is returned by Astroboa Services
		 */
		//Space in Organization Space should be deleted
		try{
			Node jcrNodeOfSpaceInOrganizationSpace = getSession().getNodeByUUID(spaceInOrganizationSpace.getId());
			Assert.assertNotNull(jcrNodeOfSpaceInOrganizationSpace, "Space "+spaceInOrganizationSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), spaceInOrganizationSpace.getId(), "Invalid ItemNotFoundException message");
		}

		//Space in User Space should be deleted
		try{
			Node jcrNodeOfSpaceInUserSpace = getSession().getNodeByUUID(spaceInUserSpace.getId());
			Assert.assertNotNull(jcrNodeOfSpaceInUserSpace, "Space "+spaceInUserSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), spaceInUserSpace.getId(), "Invalid ItemNotFoundException message");
		}

		//Check with Space in Organization Space with Astroboa Services 
		Space spaceInOrganizationSpaceReloaded = spaceService.getSpace(spaceInOrganizationSpace.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInOrganizationSpaceReloaded, "Space "+spaceInOrganizationSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		CmsOutcome<Space> spaceInOrganizationSpaceReloadedOutcome = spaceService.getSpace(spaceInOrganizationSpace.getId(), ResourceRepresentationType.SPACE_LIST, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertTrue(spaceInOrganizationSpaceReloadedOutcome != null && spaceInOrganizationSpaceReloadedOutcome.getCount()==0 && 
				CollectionUtils.isEmpty(spaceInOrganizationSpaceReloadedOutcome.getResults()), "Space "+spaceInOrganizationSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		String spaceInOrganizationSpaceReloadedAsString = spaceService.getSpace(spaceInOrganizationSpace.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInOrganizationSpaceReloadedAsString, "Space "+spaceInOrganizationSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		spaceInOrganizationSpaceReloadedAsString = spaceService.getSpace(spaceInOrganizationSpace.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInOrganizationSpaceReloadedAsString, "Space "+spaceInOrganizationSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		//Check with Space in User Space with Astroboa Services
		Space spaceInUserSpaceReloaded = spaceService.getSpace(spaceInUserSpace.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInUserSpaceReloaded, "Space "+spaceInUserSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		CmsOutcome<Space> spaceInUserSpaceReloadedOutcome = spaceService.getSpace(spaceInUserSpace.getId(), ResourceRepresentationType.SPACE_LIST, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertTrue(spaceInUserSpaceReloadedOutcome != null && spaceInUserSpaceReloadedOutcome.getCount()==0 && 
				CollectionUtils.isEmpty(spaceInUserSpaceReloadedOutcome.getResults()), "Space "+spaceInUserSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		String spaceInUserSpaceReloadedAsString = spaceService.getSpace(spaceInUserSpace.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInUserSpaceReloadedAsString, "Space "+spaceInUserSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		spaceInUserSpaceReloadedAsString = spaceService.getSpace(spaceInUserSpace.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(spaceInUserSpaceReloadedAsString, "Space "+spaceInUserSpace.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		try{
			spaceService.getContentObjectIdsWhichResideInSpace(spaceInOrganizationSpace.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+spaceInOrganizationSpace.getId()+" not found", "Invalid exception message");
		}

		try{
			spaceService.getCountOfContentObjectIdsWhichResideInSpace(spaceInOrganizationSpace.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+spaceInOrganizationSpace.getId()+" not found", "Invalid exception message");
		}

		try{
			spaceService.getContentObjectIdsWhichResideInSpace(spaceInUserSpace.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+spaceInUserSpace.getId()+" not found", "Invalid exception message");
		}

		try{
			spaceService.getCountOfContentObjectIdsWhichResideInSpace(spaceInUserSpace.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+spaceInUserSpace.getId()+" not found", "Invalid exception message");
		}


		/*
		 * Check that no JCR node exists for Topic created in User's Folksonomy and that no Topic instance
		 * is returned by Astroboa Services. 
		 * 
		 * However Topic created in Subject Taxonomy should exist.
		 */
		//Topic in Subject Taxonomy is not deleted because its owner is ALWAYS SYSTEM USER
		Node jcrNodeOfTopicInSubjectTaxonomy = getSession().getNodeByUUID(topicInSubjectTaxonomy.getId());
		Assert.assertNotNull(jcrNodeOfTopicInSubjectTaxonomy, "Topic "+topicInSubjectTaxonomy.getName() + " was deleted when user "+user.getExternalId() + " was deleted");

		//Topic in User Folksonomy should be deleted
		try{
			Node jcrNodeOfTopicInUserFolksonomy = getSession().getNodeByUUID(topicInFolksonomy.getId());
			Assert.assertNotNull(jcrNodeOfTopicInUserFolksonomy, "Space "+topicInFolksonomy.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), topicInFolksonomy.getId(), "Invalid ItemNotFoundException message");
		}

		//Check with Topic in Subject Taxonomy 
		Topic topicInSubjectTaxonomyReloaded = topicService.getTopic(topicInSubjectTaxonomy.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNotNull(topicInSubjectTaxonomyReloaded, "Topic "+topicInSubjectTaxonomy.getName() + " was deleted when user "+user.getExternalId() + " was deleted");

		CmsOutcome<Topic> topicInSubjectTaxonomyReloadedOutcome = topicService.getTopic(topicInSubjectTaxonomy.getId(), ResourceRepresentationType.TOPIC_LIST, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertTrue(topicInSubjectTaxonomyReloadedOutcome != null && topicInSubjectTaxonomyReloadedOutcome.getCount()==1 && 
				topicInSubjectTaxonomyReloadedOutcome.getResults().size()==1 && 
				topicInSubjectTaxonomyReloadedOutcome.getResults().get(0).getId().equals(topicInSubjectTaxonomy.getId()), "Topic "+topicInSubjectTaxonomy.getName() + " was deleted when user "+user.getExternalId() + " was deleted");

		String topicInSubjectTaxonomyReloadedAsString = topicService.getTopic(topicInSubjectTaxonomy.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNotNull(topicInSubjectTaxonomyReloadedAsString, "Topic "+topicInSubjectTaxonomy.getName() + " was deleted when user "+user.getExternalId() + " was deleted");

		topicInSubjectTaxonomyReloadedAsString = topicService.getTopic(topicInSubjectTaxonomy.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNotNull(topicInSubjectTaxonomyReloadedAsString, "Topic "+topicInSubjectTaxonomy.getName() + " was deleted when user "+user.getExternalId() + " was deleted");

		//Check with Topic in User Folksonomy	
		Topic topicInUserFolksonomyReloaded = topicService.getTopic(topicInFolksonomy.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(topicInUserFolksonomyReloaded, "Topic "+topicInFolksonomy.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		CmsOutcome<Topic> topicInUserFolksonomyReloadedOutcome = topicService.getTopic(topicInFolksonomy.getId(), ResourceRepresentationType.TOPIC_LIST, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertTrue(topicInUserFolksonomyReloadedOutcome != null && topicInUserFolksonomyReloadedOutcome.getCount()==0 && 
				CollectionUtils.isEmpty(topicInUserFolksonomyReloadedOutcome.getResults()), "Topic "+topicInFolksonomy.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		String topicInUserFolksonomyReloadedAsString = topicService.getTopic(topicInFolksonomy.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(topicInUserFolksonomyReloadedAsString, "Topic "+topicInFolksonomy.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		topicInUserFolksonomyReloadedAsString = topicService.getTopic(topicInFolksonomy.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
		Assert.assertNull(topicInUserFolksonomyReloadedAsString, "Topic "+topicInFolksonomy.getName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		/*
		 * Check that no JCR node exists for ContentObject created and that no ContentObject instance
		 * is returned by Astroboa Services. 
		 * 
		 */
		try{
			Node contentObjectNode = getSession().getNodeByUUID(contentObject.getId());
			Assert.assertNull(contentObjectNode, "ContentObject "+contentObject.getSystemName() + " was not deleted when user "+user.getExternalId() + " was deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), contentObject.getId(), "Invalid ItemNotFoundException message");
		}
		
		ContentObject contentObjectReloaded = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		Assert.assertNull(contentObjectReloaded, "ContentObject "+contentObject.getSystemName() + " was not deleted");

		CmsOutcome<ContentObject> contentObjectReloadedOutcome = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_LIST, FetchLevel.ENTITY_AND_CHILDREN, null, null, false);
		Assert.assertTrue(contentObjectReloadedOutcome != null && contentObjectReloadedOutcome.getCount()==0 && 
				CollectionUtils.isEmpty(contentObjectReloadedOutcome.getResults()), "ContentObject "+contentObject.getSystemName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		String contentObjectReloadedAsString = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY_AND_CHILDREN, null, null, false);
		Assert.assertNull(contentObjectReloadedAsString, "ContentObject "+contentObject.getSystemName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		contentObjectReloadedAsString = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN, null, null, false);
		Assert.assertNull(contentObjectReloadedAsString, "ContentObject "+contentObject.getSystemName() + " was not deleted when user "+user.getExternalId() + " was deleted");

		//Finally delete content object of type personObject which represents the user
		loginToRepositoryRepresentingIdentityStoreAsSystem();
		deleteUser(externalId);
		loginToTestRepositoryAsSystem();
		
	}

	@Test
	public void testSystemRepositoryUser(){
		
		
		RepositoryUser systemUser = repositoryUserService.getSystemRepositoryUser();
		
		RepositoryUser systemUserUsingAlternativeMethod = repositoryUserService.getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID);
		
		Assert.assertNotNull(systemUser, "System user not returned from method repositoryUserService.getSystemRepositoryUser");
		Assert.assertNotNull(systemUserUsingAlternativeMethod, "System user not returned from method repositoryUserService.getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID)");
		
		Assert.assertEquals(systemUser.getId(), systemUserUsingAlternativeMethod.getId(), "System users do not have the same id ");
		Assert.assertEquals(systemUser.getExternalId(), systemUserUsingAlternativeMethod.getExternalId(), "System users do not have the same id ");
		Assert.assertEquals(systemUser.getLabel(), systemUserUsingAlternativeMethod.getLabel(), "System users do not have the same id ");
		
		JAXBTestUtils.assertRepositoryUsersAreTheSame(systemUser, systemUserUsingAlternativeMethod);
		
		RepositoryUser systemUserWithOldExternalId = repositoryUserService.getRepositoryUser("1");
		
		Assert.assertNull(systemUserWithOldExternalId, "System user with old external id still exists");
	}

}
