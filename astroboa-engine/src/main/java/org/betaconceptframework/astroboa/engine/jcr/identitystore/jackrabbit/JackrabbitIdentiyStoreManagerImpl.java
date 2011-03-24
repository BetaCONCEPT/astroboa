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
package org.betaconceptframework.astroboa.engine.jcr.identitystore.jackrabbit;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.identitystore.CmsIdentityStoreManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly=false, rollbackFor = CmsException.class, propagation= Propagation.SUPPORTS)
public class JackrabbitIdentiyStoreManagerImpl implements CmsIdentityStoreManager {

	private JackrabbitIdentityStoreDao jackrabbitIdentityStoreDao;
	
	/**
	 * @param jackrabbitIdentityStoreDao the jackrabbitIdentityStoreDao to set
	 */
	public void setJackrabbitIdentityStoreDao(
			JackrabbitIdentityStoreDao jackrabbitIdentityStoreDao) {
		this.jackrabbitIdentityStoreDao = jackrabbitIdentityStoreDao;
	}


	@Transactional(readOnly=false, rollbackFor = CmsException.class, propagation = Propagation.REQUIRED)
	public void initializeIdentityStore(String cmsRepositoryId, CmsRepository identityStoreRepository) {
		
		jackrabbitIdentityStoreDao.initializeIdentityStore(cmsRepositoryId, identityStoreRepository);
		
	}
	
	@Transactional(readOnly=false, rollbackFor = CmsException.class, propagation = Propagation.REQUIRED)
	public void createSystemPerson(String cmsRepositoryId, String identityStoreRepositoryId) {
		
		jackrabbitIdentityStoreDao.createSystemPerson(cmsRepositoryId, identityStoreRepositoryId);
		
	}

}
