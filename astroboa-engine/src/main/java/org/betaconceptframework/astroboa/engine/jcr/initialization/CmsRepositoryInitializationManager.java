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

package org.betaconceptframework.astroboa.engine.jcr.initialization;


import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.engine.jcr.identitystore.CmsIdentityStoreManager;
import org.betaconceptframework.astroboa.engine.jcr.nodetypeconfig.CmsNodeTypeDefinitionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsRepositoryInitializationManager {

	private  final Logger logger = LoggerFactory.getLogger(CmsRepositoryInitializationManager.class);
	
	@Autowired
	private CmsNodeTypeDefinitionManager cmsNodeTypeDefinitionManager;
	
	@Autowired
	private CmsIdentityStoreManager cmsIdentityStoreManager; 
	
	public void initialize(CmsRepository cmsRepository) {
		
		logger.info("Initializing repository {}", cmsRepository.getId() );
				
		//	Initialize Jcr Repository
		cmsNodeTypeDefinitionManager.saveOrUpdateTypeAndNodeHierarchy(cmsRepository.getId());
	}

	
	public void initializeIdentityStore(String cmsRepositoryId, CmsRepository identityStore) {
		
		cmsIdentityStoreManager.initializeIdentityStore(cmsRepositoryId, identityStore);
			
		//Create System Person 
		cmsIdentityStoreManager.createSystemPerson(cmsRepositoryId, identityStore.getId());
		
	}

}
