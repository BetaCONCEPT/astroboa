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

import org.betaconceptframework.astroboa.api.model.Space;
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
public class SpaceJAXBTest extends AbstractRepositoryTest{

	@Test
	public void testJSONExportOfRootSpacesOfOrganizationSpace() throws Throwable{
		
		Space space = getOrganizationSpace();
		
		Space childSpace1 = JAXBTestUtils.createSpace("test-child-space-json-export-first-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		space.addChild(childSpace1);
		
		childSpace1 = spaceService.save(childSpace1);
		addEntityToBeDeletedAfterTestIsFinished(childSpace1);

		//Space has one child space
		String json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with one child space "+json);
			
			//Retrieve space from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with one child space "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//add one child
		Space childSpace2 = JAXBTestUtils.createSpace("test-child-space-json-export-second-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		space.addChild(childSpace2);
		
		space = spaceService.save(space);
		
		json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with 2 child spaces "+json);
		
			//Retrieve space from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with 2 child spaces "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		
	}
	@Test
	public void testJSONExportOfChildSpaces() throws Throwable{
		
		Space space = JAXBTestUtils.createSpace("test-child-space-json-export", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		Space childSpace1 = JAXBTestUtils.createSpace("test-child-space-json-export-first-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		space.addChild(childSpace1);
		space.setParent(getOrganizationSpace());
		
		space = spaceService.save(space);
		addEntityToBeDeletedAfterTestIsFinished(space);

		//Space has one child space
		String json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with one child space "+json);

			//Retrieve space from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with one child space "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//add one child
		Space childSpace2 = JAXBTestUtils.createSpace("test-child-space-json-export-second-child", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		space.addChild(childSpace2);
		
		space = spaceService.save(space);
		
		json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with 2 child spaces "+json);

			//Retrieve space from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"childSpaces\":{\"space\":[{"), 
					"Invalid JSON export of a space with one child space "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		
	}

	@Test
	public void testJSONExportOfLocalizedLabel() throws Throwable{
		
		Space space = JAXBTestUtils.createSpace("test-localized-label-json-export", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		space.setParent(getOrganizationSpace());
		
		space = spaceService.save(space);
		addEntityToBeDeletedAfterTestIsFinished(space);

		//Space has 2 localized labels
		String json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a space with 2 localized labels "+json);
			
			//Retrieve topic from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);

			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"fr\":\"test-localized-label-json-export\",\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a space with 2 localized labels "+json);

		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
		
		//remove one label
		space.getLocalizedLabels().remove("fr");
		space = spaceService.save(space);

		
		json  = space.json(prettyPrint);
		
		try{
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a space with 1 localized label "+json);
			
			//Retrieve topic from repository
			space = spaceService.getSpace(space.getId(), ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
			json  = space.json(prettyPrint);
			
			Assert.assertTrue(removeWhitespacesIfNecessary(json).contains("\"localization\":{\"label\":{\"en\":\"test-localized-label-json-export\"}}"), 
					"Invalid JSON export of a space with 1 localized label "+json);


		}
		catch(Throwable e){
			logger.error(json, e);
			throw e;
		}
	}
	
	@Test  
	public void testSpaceJAXBMarshllingUnMarshalling() throws Throwable {
		
		
		Space space = JAXBTestUtils.createSpace("spaceName", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		Space childSpace1 = JAXBTestUtils.createSpace("firstChild",
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		
		space.addChild(childSpace1);
		space.setParent(getOrganizationSpace());
		
		String xml = null;
		String json = null;
		
		ImportMode importMode = ImportMode.DO_NOT_SAVE;
		
		long start = System.currentTimeMillis();
		
			try{
				start = System.currentTimeMillis();
				xml = space.xml(prettyPrint);
				
				logTimeElapsed("Export Space XML using xml() method in {}", start);
				
				start = System.currentTimeMillis();
				Space spaceUnMarshalledFromXML = importDao.importSpace(xml, importMode);
				logTimeElapsed("Import Space XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentSpaceIsTheSameObjectAmongSpaceChildren(spaceUnMarshalledFromXML);
				
				repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromXML, true, true, true, true, true);
				
				start = System.currentTimeMillis();
				json = space.json(prettyPrint);
				logTimeElapsed("Export Space JSON using json() method in {}", start);
				
				start = System.currentTimeMillis();
				Space spaceUnMarshalledFromJSON = importDao.importSpace(json, importMode); 
				logTimeElapsed("Import Space JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentSpaceIsTheSameObjectAmongSpaceChildren(spaceUnMarshalledFromJSON);
				
				repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromJSON, true,true,true, true, true);
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXML, spaceUnMarshalledFromJSON, true,true,true, true, true);
				
				//Now create XML and JSON from Service and compare each other
				space = spaceService.save(space);
				addEntityToBeDeletedAfterTestIsFinished(space);
				
				start = System.currentTimeMillis();
				json = spaceService.getSpace(space.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL);
				
				logTimeElapsed("Export Space JSON using service in {}", start);
				
				start = System.currentTimeMillis();
				Space spaceUnMarshalledFromJSONService = importDao.importSpace(json, importMode); 
				logTimeElapsed("Import Space JSON in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentSpaceIsTheSameObjectAmongSpaceChildren(spaceUnMarshalledFromJSONService);

				
				repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromJSONService, true, true, true, true, true);
				//Order of spaces does matter since spaceUnMarshalledFromJSON and spaceUnMarshalledFromXML do not have identifier
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromJSONService, spaceUnMarshalledFromJSON, true, true, true, false, true);
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromJSONService,spaceUnMarshalledFromXML,  true, true, true, false, true);

				start = System.currentTimeMillis();
				xml = spaceService.getSpace(space.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);
				
				logTimeElapsed("Export Space XML using service in {}", start);
				
				start = System.currentTimeMillis();
				Space spaceUnMarshalledFromXMLService = importDao.importSpace(xml, importMode); 
				logTimeElapsed("Import Space XML in {}, ImportMode {}, ", start, importMode.toString());
				JAXBTestUtils.assertParentSpaceIsTheSameObjectAmongSpaceChildren(spaceUnMarshalledFromXMLService);
				
				repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromXMLService, true, true, true, true, true);
				//Order of spaces does matter since spaceUnMarshalledFromJSON and spaceUnMarshalledFromXML do not have identifier
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXMLService, spaceUnMarshalledFromJSON, true, true, true, false, true);
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXMLService, spaceUnMarshalledFromXML, true, true, true, false, true);
				
				repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXMLService, spaceUnMarshalledFromJSONService, true, true, true, true, true);
				
			}
			catch(Throwable e){
				logger.error("Created XML :\n {}", xml);
				logger.error("Second JSON :\n{} ", json);
				throw e;
			}
		}


	@Test
	public void testNumberOfChildrenExport(){

		Space space = JAXBTestUtils.createSpace("spaceTestNumberOfChildren", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		space.setParent(getOrganizationSpace());

		space = spaceService.save(space);
		
		addEntityToBeDeletedAfterTestIsFinished(space);
		
		String xmlFromApi = space.xml(prettyPrint);
		String xmlFromService = spaceService.getSpace(space.getId(), ResourceRepresentationType.XML, FetchLevel.FULL);

		String jsonFromApi = space.json(prettyPrint);
		String jsonFromService = spaceService.getSpace(space.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL);

		xmlFromApi = removeWhitespacesIfNecessary(xmlFromApi);
		jsonFromApi = removeWhitespacesIfNecessary(jsonFromApi);
		xmlFromService = removeWhitespacesIfNecessary(xmlFromService);
		jsonFromService = removeWhitespacesIfNecessary(jsonFromService);
		
		final String expectedValueInXML = "numberOfChildren=\"0\"";
		final String expectedValueInJSON = "\"numberOfChildren\":\"0\"";

		Assert.assertTrue(xmlFromApi.contains(expectedValueInXML), "Space XML export from API "+xmlFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(xmlFromService.contains(expectedValueInXML), "Space XML export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");

		Assert.assertTrue(jsonFromApi.contains(expectedValueInJSON), "Space JSON export from API "+jsonFromApi+ " should not contain numberOfChildren attribute");
		Assert.assertTrue(jsonFromService.contains(expectedValueInJSON), "Space JSON export from Service "+xmlFromApi+ " should not contain numberOfChildren attribute");
	}

}
