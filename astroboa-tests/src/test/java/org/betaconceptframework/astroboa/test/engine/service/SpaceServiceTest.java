/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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

import java.util.Arrays;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceServiceTest extends AbstractRepositoryTest {

	@Test
	public void testDeleteSpace() throws ItemNotFoundException, RepositoryException{
		
		Space space =  createRootSpaceForOrganizationSpace("space-test-delete");
		
		ContentObject contentObject = createContentObject(getSystemUser(),  "test-space-delete-reference");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);
		
		space.addContentObjectReference(contentObject.getId());
		
		space = spaceService.save(space);
		
		//Now delete space
		Assert.assertTrue(spaceService.deleteSpace(space.getId()), "Space was not deleted");
		
		//Check with Jcr
		try{
			Node spaceNode = getSession().getNodeByIdentifier(space.getId());
			
			Assert.assertNull(spaceNode, "Space "+space.getName() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), space.getId(), "Invalid ItemNotFoundException message");
		}
		
		//Check with Space entity 
		Space spaceReloaded = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		
		Assert.assertNull(spaceReloaded, "Space "+space.getName() + " was not deleted");

		//Check with Astroboa Service
		try{
			spaceService.getContentObjectIdsWhichResideInSpace(space.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+space.getId()+" not found", "Invalid exception message");
		}

		try{
			spaceService.getCountOfContentObjectIdsWhichResideInSpace(space.getId());
		}
		catch(CmsException e){
			Assert.assertEquals(e.getMessage(), "Space "+space.getId()+" not found", "Invalid exception message");
		}
		
	}
	
	@Test
	public void testSaveSpace() throws ItemNotFoundException, RepositoryException{
		
		ContentObject contentObject = createContentObject(getSystemUser(),  "test-space-save-reference");
		contentObject = contentService.save(contentObject, false, true, null);
		markObjectForRemoval(contentObject);

		
		Space space =  createRootSpaceForOrganizationSpace("space-test-save");
		space.addContentObjectReference(contentObject.getId());
		space = spaceService.save(space);
		
		
		//Check with Jcr
		Node spaceNode = getSession().getNodeByIdentifier(space.getId());
		
		Assert.assertNotNull(spaceNode, "Space "+space.getName() + " was not saved at all");
		
		Assert.assertTrue(spaceNode.hasProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName()), "Space "+space.getName() + " was saved but reference to content object was not");

		Value[] contentObjectReferences = spaceNode.getProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName()).getValues();
		
		Assert.assertTrue(contentObjectReferences.length == 1 && contentObjectReferences[0].getString().equals(contentObject.getId()), "Space "+space.getName() + " was saved but reference to content object is not valid."
				+ " Expected "+contentObject.getId()+ " but found "+contentObjectReferences[0].getString());
		
		//Check with Space entity 
		Space spaceReloaded = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN);
		
		Assert.assertNotNull(spaceReloaded, "Space "+space.getName() + " was not saved at all");
		
		Assert.assertNotNull(spaceReloaded.getContentObjectReferences(), "Space "+space.getName() + " was saved but reference to content object was not");

		Assert.assertEquals(spaceReloaded.getNumberOfContentObjectReferences(), 1, "Space "+space.getName() + " should have been saved with only 1 content object reference ");

		List<String> contentObjectReferencesList = spaceReloaded.getContentObjectReferences();
		
		Assert.assertTrue(contentObjectReferencesList.size() == 1 && contentObjectReferencesList.get(0).equals(contentObject.getId()), "Space "+space.getName() + " was saved but reference to content object is not valid."
				+ " Expected "+contentObject.getId()+ " but found "+contentObjectReferencesList.get(0));

		//Check with Astroboa Service
		contentObjectReferencesList = spaceService.getContentObjectIdsWhichResideInSpace(spaceReloaded.getId());
		Assert.assertTrue(contentObjectReferencesList.size() == 1 && contentObjectReferencesList.get(0).equals(contentObject.getId()), "Space "+space.getName() + " was saved but reference to content object is not valid."
				+ " Expected "+contentObject.getId()+ " but found "+contentObjectReferencesList);

		Assert.assertEquals(spaceService.getCountOfContentObjectIdsWhichResideInSpace(spaceReloaded.getId()), 1, "Space "+space.getName() + " should have been saved with only 1 content object reference ");
		
	}
	
	@Test
	public void testDeleteOrganizationSpace(){
		Space organizationSpace = spaceService.getOrganizationSpace();
		
		try{
			spaceService.deleteSpace(organizationSpace.getId());
			
			Assert.assertTrue(1==2, "Mehod SpaceService.deleteSpace did not throw an excpetion when test tried to delete Organization Space ");
		}
		catch(CmsException e){
			
			Assert.assertEquals(e.getMessage(), CmsBuiltInItem.OrganizationSpace.getJcrName()+" cannot be deleted.");
		}
		
	}
	
	@Test
	public void testGetSpaceAsSpaceOutcome() throws Throwable{
		
		Space space =  createRootSpaceForOrganizationSpace("spaceTestExportAsSpaceOutcome");
		
		CmsOutcome<Space> outcome = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_LIST, FetchLevel.ENTITY);
		
		Assert.assertNotNull(outcome, "SpaceService.getSpace returned null with Outcome returned type");
		
		Assert.assertEquals(outcome.getCount(), 1, "SpaceService.getSpace returned invalid count with Outcome returned type");
		Assert.assertEquals(outcome.getLimit(), 1, "SpaceService.getSpace returned invalid limit with Outcome returned type");
		Assert.assertEquals(outcome.getOffset(), 0, "SpaceService.getSpace returned invalid offset with Outcome returned type");
		
		
		Assert.assertEquals(outcome.getResults().size(), 1, "SpaceService.getSpace returned invalid number of Spaces with Outcome returned type");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), space.getId(), "SpaceService.getSpace returned invalid space with Outcome returned type");
	}
	
	@Test
	public void testGetSpaceXmlorJSON() throws Throwable{
		
		Space space =  createRootSpaceForOrganizationSpace("spaceTestExportXmlJSON");
		Space childSpace = createSpace("spaceTestExportXMLJSONChild", space);
		createSpace("grandChildSpaceTestExportXMLJSONChild", childSpace);

		String spaceXml = null;
		String spaceXmlFromServiceUsingId = null;

		List<ResourceRepresentationType<String>> outputs = Arrays.asList(ResourceRepresentationType.JSON, ResourceRepresentationType.XML);
		
		try{
			
			ImportConfiguration configuration = ImportConfiguration.space()
					.persist(PersistMode.DO_NOT_PERSIST)
					.build();

			for (ResourceRepresentationType<String> output : outputs){
				//Reload space without its children
				space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY);

				//	First check export of space only
				if (output.equals(ResourceRepresentationType.XML)){
					spaceXml = space.xml(prettyPrint);
					spaceXmlFromServiceUsingId = spaceService.getSpace(space.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY);
				}
				else{
					spaceXml = space.json(prettyPrint);
					spaceXmlFromServiceUsingId = spaceService.getSpace(space.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY);
				}
				
				Space spaceFromServiceWithId = importDao.importSpace(spaceXmlFromServiceUsingId, configuration); 

				repositoryContentValidator.compareSpaces(space, spaceFromServiceWithId, false, true, true,true, true);

				//Now check export of space children
				space.getChildren();
				if (output.equals(ResourceRepresentationType.XML)){
					spaceXml = space.xml(prettyPrint);
					spaceXmlFromServiceUsingId = spaceService.getSpace(space.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);
				}
				else{
					spaceXml = space.json(prettyPrint);
					spaceXmlFromServiceUsingId = spaceService.getSpace(space.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL);
				}

				spaceFromServiceWithId = importDao.importSpace(spaceXmlFromServiceUsingId, configuration); 

				repositoryContentValidator.compareSpaces(space, spaceFromServiceWithId, true, true, true,true, true);
			
			}			
		}
		catch(Throwable e){
			logger.error("Initial \n{}",TestUtils.prettyPrintXml(spaceXml));
			logger.error("Using Id \n{}",TestUtils.prettyPrintXml(spaceXmlFromServiceUsingId));
			throw e;
		}	
	}
	
	@Test
	public void testSaveWithVariousNames(){
		
		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		
		Space space = JAXBTestUtils.createSpace("testSpaceWithVariousNames", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				systemUser);
		
		//Provide valid system name
		space.setName("validSystemName");
		space.setParent(systemUser.getSpace());
		
		space = spaceService.save(space);
		markSpaceForRemoval(space);
		
		
		//Now provide invalid system name
		checkInvalidSystemNameSave(space, "invalid)SystemName");
		checkInvalidSystemNameSave(space, "invalid((SystemName");
		checkInvalidSystemNameSave(space, "invalid)SystemNa&me");
		checkInvalidSystemNameSave(space, "ςδςδ");
		checkInvalidSystemNameSave(space, "invaliδName+");
		
		checkValidSystemNameSave(space, "09092");
		checkValidSystemNameSave(space, "09sasas");
		checkValidSystemNameSave(space, "09_sdds-02");
		checkValidSystemNameSave(space, "----");
		checkValidSystemNameSave(space, "____");
		checkValidSystemNameSave(space, "sdsds");
		checkValidSystemNameSave(space, "090..92");
		checkValidSystemNameSave(space, "090.92");
		checkValidSystemNameSave(space, "090..__--92");
		checkValidSystemNameSave(space, "090..92");
		
		
	}


	private void checkInvalidSystemNameSave(Space space,
			String systemName) {
		
		try{
			space.setName(systemName);
			
			space = spaceService.save(space);
			
			
			Assert.assertEquals(1, 2, 
					"Space was saved with invalid system name "+systemName);
			
		}
		catch(CmsException e){
		
			String message = e.getMessage();
			
			Throwable t = e;
			
			while (t.getCause() != null){
				message = t.getCause().getMessage();
				
				t = t.getCause();
			}
			
			Assert.assertEquals(message, "Space name '"+systemName+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP, 
					"Invalid exception "+ e.getMessage());
		}
	}
	
	private void checkValidSystemNameSave(Space space,
			String systemName) {
		
		space.setName(systemName);
			
		space = spaceService.save(space);
			
	}
	
	

}
