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
package org.betaconceptframework.astroboa.test.engine;

import javax.jcr.Session;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryDao;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionFactoryUtils;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TestCmsDao {

	public final Session getSession(){

		SessionFactory currentJcrSessionFactory = AstroboaTestContext.INSTANCE.getBean(RepositoryDao.class, "repositoryDao").getJcrSessionFactoryForAssociatedRepository();

		Session session = SessionFactoryUtils.getSession(currentJcrSessionFactory, true);
		if (session == null){
			throw new CmsException("Found no jcr session for associated "+ AstroboaClientContextHolder.getRepositoryContextForActiveClient());
		}

		return session;
	}

}
