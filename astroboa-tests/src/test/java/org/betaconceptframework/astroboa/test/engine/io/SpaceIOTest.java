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

import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceIOTest extends AbstractRepositoryTest{

	public SpaceIOTest() throws JAXBException {
		super();
	}

	@Test  
	public void testCompleteSpaceIO() throws Throwable {

		Space rootSpace1 = createRootSpaceForOrganizationSpace("testFullSpaceJAXB1");
		Space rootSpace2 = createRootSpaceForOrganizationSpace("testFullSpaceJAXB2");

		Space childSpace1 = createSpace("testFullSpaceJAXBChild1", rootSpace1);
		Space childSpace2 = createSpace("testFullSpaceJAXBChild2", childSpace1);

		Space childSpace3 = createSpace("testFullSpaceJAXBChild3", rootSpace2);

		assertSpaceIO(rootSpace1.getName());
		assertSpaceIO(rootSpace2.getName());
		assertSpaceIO(childSpace1.getName());
		assertSpaceIO(childSpace2.getName());
		assertSpaceIO(childSpace3.getName());


	}

	private void assertSpaceIO(String spaceName) throws Throwable {

		SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();

		spaceCriteria.doNotCacheResults();
		spaceCriteria.addNameEqualsCriterion(spaceName);

		CmsOutcome<Space> cmsOutcome = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.SPACE_LIST);

		for (Space space : cmsOutcome.getResults()){

			String xml = null;
			String json = null;

			FetchLevel fetchLevelForLog = null;
			ImportMode importModeForLog = null;

			try{
				for (FetchLevel fetchLevel : FetchLevel.values()){

					fetchLevelForLog = fetchLevel;

					if (fetchLevel == FetchLevel.ENTITY_AND_CHILDREN){
						space.getChildren();
					}
					else if (fetchLevel == FetchLevel.FULL){
						loadAllTree(space);
					}

					boolean compareChildSpaces = fetchLevel != FetchLevel.ENTITY;

					for (ImportMode importMode : ImportMode.values()){

						importModeForLog = importMode;

						xml = space.xml(prettyPrint);

						Space spaceUnMarshalledFromXML = importDao.importSpace(xml, importMode); 

						repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromXML, compareChildSpaces, compareChildSpaces, true,true, true);

						json = space.json(prettyPrint);

						Space spaceUnMarshalledFromJSON = importDao.importSpace(json, importMode); 

						repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromJSON, compareChildSpaces, compareChildSpaces, true,true, true);
						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXML, spaceUnMarshalledFromJSON, compareChildSpaces,compareChildSpaces, true,true, true);

						//Now create XML and JSON from Service and compare each other
						json = spaceService.getSpace(space.getId(), ResourceRepresentationType.JSON, fetchLevel);

						Space spaceUnMarshalledFromJSONService = importDao.importSpace(json, importMode); 

						repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromJSONService, compareChildSpaces, compareChildSpaces, true,true, true);
						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromJSON, spaceUnMarshalledFromJSONService, compareChildSpaces, compareChildSpaces, true,true, true);
						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXML, spaceUnMarshalledFromJSONService, compareChildSpaces, compareChildSpaces, true,true, true);

						xml = spaceService.getSpace(space.getId(), ResourceRepresentationType.XML, fetchLevel);

						Space spaceUnMarshalledFromXMLService = importDao.importSpace(xml, importMode); 

						repositoryContentValidator.compareSpaces(space, spaceUnMarshalledFromXMLService, compareChildSpaces, compareChildSpaces, true,true, true);
						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromJSON, spaceUnMarshalledFromXMLService, compareChildSpaces, compareChildSpaces, true,true, true);
						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXML, spaceUnMarshalledFromXMLService, compareChildSpaces, compareChildSpaces, true,true, true);

						repositoryContentValidator.compareSpaces(spaceUnMarshalledFromXMLService, spaceUnMarshalledFromJSONService, compareChildSpaces, compareChildSpaces, true,true, true);
					}
				}
			}
			catch(Throwable e){

				logger.error("Fetch Level {}", fetchLevelForLog);
				logger.error("Import Mode {}", importModeForLog);
				logger.error("xml {}", space.xml(true));
				logger.error("JSON {}", space.json(true));
				
				throw e;
			}

		}
	}

	private void loadAllTree(Space space) {

		List<Space> childSpaces = space.getChildren();

		if (childSpaces != null && childSpaces.size() > 0){
			for (Space child : childSpaces){
				loadAllTree(child);
			}
		}

	}
}
