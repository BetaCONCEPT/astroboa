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


import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.engine.jcr.dao.SerializationDao;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class SerializationServiceImpl  implements SerializationService {

	@Autowired
	private SerializationDao serializationDao;

	@Override
	public Future<SerializationReport> serializeObjects(ContentObjectCriteria objectCriteria, SerializationConfiguration serializationConfiguration) {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.OBJECT, serializationConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Override
	public Future<SerializationReport> serializeRepository(SerializationConfiguration serializationConfiguration) {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.REPOSITORY, serializationConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}

	@Override
	public Future<SerializationReport> serializeOrganizationSpace() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.ORGANIZATION_SPACE, SerializationConfiguration.space().build());
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public Future<SerializationReport> serializeRepositoryUsers() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.REPOSITORY_USER, SerializationConfiguration.repositoryUser().build());
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public Future<SerializationReport> serializeTaxonomies() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.TAXONOMY, SerializationConfiguration.taxonomy().build());
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}
}