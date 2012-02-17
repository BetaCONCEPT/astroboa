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
package org.betaconceptframework.astroboa.engine.jcr.dao;

import javax.jcr.Session;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springmodules.jcr.SessionFactory;
import org.springmodules.jcr.SessionFactoryUtils;

/**
 * Provides jcrDaoSupport from RepositoryContextImpl
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class JcrDaoSupport extends AbstractCmsDao {
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final Session getSession(){
		
		
		SessionFactory currentJcrSessionFactory = repositoryDao.getJcrSessionFactoryForAssociatedRepository();
		
		Session session = SessionFactoryUtils.getSession(currentJcrSessionFactory, false);
		if (session == null){
			throw new CmsException("Found no jcr session for associated "+ AstroboaClientContextHolder.getRepositoryContextForActiveClient());
		}
		
		if (logger.isDebugEnabled()){
			logger.debug("Providing Jcr Session {} for {} in Thread {}", 
					new Object[]{session, AstroboaClientContextHolder.getRepositoryContextForActiveClient() , Thread.currentThread()});
		}
		
		return session;
	}
}
