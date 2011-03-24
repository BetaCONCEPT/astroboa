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

package org.betaconceptframework.astroboa.engine.service.jcr;


import java.util.List;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class RepositoryUserServiceImpl implements RepositoryUserService {

	@Autowired
	private RepositoryUserDao repositoryUserDao;

	public List<RepositoryUser> searchRepositoryUsers(RepositoryUserCriteria repositoryUserCriteria)  {
		try{
			return repositoryUserDao.searchRepositoryUsers(repositoryUserCriteria);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public RepositoryUser saveRepositoryUser(RepositoryUser repositoryUser)  {
		try{
			return repositoryUserDao.saveRepositoryUser(repositoryUser);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void deleteRepositoryUserAndOwnedObjects(String repositoryUserId)   {
		try{
			repositoryUserDao.removeRepositoryUserAndOwnedObjects(repositoryUserId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}


	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void deleteRepositoryUser(String repositoryUserId, RepositoryUser alternativeUser) {
		try{
			repositoryUserDao.removeRepositoryUser(repositoryUserId, alternativeUser);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public RepositoryUser getRepositoryUser(String externalId) {
		try{
			return repositoryUserDao.getRepositoryUser(externalId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public RepositoryUser getSystemRepositoryUser() {
		try{
			return repositoryUserDao.getSystemRepositoryUser();
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public RepositoryUser save(Object repositoryUser) {
		try{
			return repositoryUserDao.saveRepositoryUser(repositoryUser);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

}
