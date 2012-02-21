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
package org.betaconceptframework.astroboa.engine.service.jcr;

import java.util.List;

import javax.security.auth.Subject;

import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryServiceImpl implements RepositoryService{

	@Autowired
	private RepositoryDao repositoryDao;

	public String login(String repositoryId, AstroboaCredentials credentials) {
		try{ 
			return repositoryDao.login(repositoryId, credentials);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	public List<CmsRepository> getAvailableCmsRepositories() {
		try{ 
			return repositoryDao.getAvailableCmsRepositories();
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	public boolean isRepositoryAvailable(String repositoryId) {
		try{ 
			return repositoryDao.isRepositoryAvailable(repositoryId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public CmsRepository getCurrentConnectedRepository() {
		try{ 
			return repositoryDao.getCurrentConnectedRepository();
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public CmsRepository getCmsRepository(String repositoryId) {
		try{ 
			return repositoryDao.getCmsRepository(repositoryId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public String login(String repositoryId, AstroboaCredentials credentials,
			String permanentKey) {
		return repositoryDao.login(repositoryId, credentials, permanentKey,null);
	}

	@Override
	public String loginAsAnonymous(String repositoryId) {
		return loginAsAnonymous(repositoryId, null);
	}

	@Override
	public String loginAsAnonymous(String repositoryId, String permanentKey) {
		return repositoryDao.loginAsAnonymous(repositoryId, permanentKey);
	}

	@Override
	public String login(String repositoryId, Subject subject,
			String permanentKey) {
		return repositoryDao.login(repositoryId, subject, permanentKey);
	}

	@Override
	public String login(String repositoryId, String username, String key) {
		return login(repositoryId, username, key, null);
	}

	@Override
	public String login(String repositoryId, String username, String key,
			String permanentKey) {
		return repositoryDao.login(repositoryId, username, key, permanentKey);
	}

	@Override
	public String loginAsAdministrator(String repositoryId, String key) {
		return loginAsAdministrator(repositoryId, key, null);
	}

	@Override
	public String loginAsAdministrator(String repositoryId, String key,
			String permanentKey) {
		return repositoryDao.loginAsAdministrator(repositoryId, key, permanentKey);
	}

}
