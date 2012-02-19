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
package org.betaconceptframework.astroboa.test.model.jaxb;

import javax.xml.bind.JAXBException;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserJAXBTest extends AbstractRepositoryTest{

	public RepositoryUserJAXBTest() throws JAXBException {
		super();
		
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Test  
	public void testRepositoryUserJAXBMarshallingUnMarshalling() throws Throwable {
		
		//Create a new RepositoryUser
		RepositoryUser repUser = JAXBTestUtils.createRepositoryUser("ExternalId", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		
		//Export to xml
		String xml = repUser.xml(prettyPrint);
		
		//Export to json
		String json = repUser.json(prettyPrint);
		
		try{
			
			ImportConfiguration configuration = ImportConfiguration.repositoryUser()
					.persist(PersistMode.DO_NOT_PERSIST)
					.build();

			
			//Create a new instance for the same user using importService and its xml
			RepositoryUser repositoryUserFromXml = importDao.importRepositoryUser(xml, configuration);
			
			//Compare two instances
			repositoryContentValidator.compareRepositoryUsers(repUser, repositoryUserFromXml, true, true);
			
			//Create a new instance for the same user using importService and its xml
			RepositoryUser repositoryUserFromJson = importDao.importRepositoryUser(json, configuration);
			
			//Compare two instances
			repositoryContentValidator.compareRepositoryUsers(repUser, repositoryUserFromJson, true, true);
			
			//Compare
			repositoryContentValidator.compareRepositoryUsers(repositoryUserFromXml, repositoryUserFromJson, true, true);
		}
		catch(Throwable e){
			logger.error("XML {}", xml);
			
			logger.error("JSON {}", json);
			
			throw e;
		}
	}


}
