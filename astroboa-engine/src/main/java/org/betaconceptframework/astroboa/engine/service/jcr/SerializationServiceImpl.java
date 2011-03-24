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


import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
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
	public SerializationReport serializeObjects(boolean serializeBinaryContent) {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.CONTENT_OBJECT, serializeBinaryContent);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Override
	public SerializationReport serializeRepository(boolean serializeBinaryContent) {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.REPOSITORY, serializeBinaryContent);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}

	@Override
	public SerializationReport serializeOrganizationSpace() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.ORGANIZATION_SPACE, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public SerializationReport serializeRepositoryUsers() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.REPOSITORY_USER, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	public SerializationReport serializeTaxonomies() {
		try{ 
			return serializationDao.serializeAllInstancesOfEntity(CmsEntityType.TAXONOMY, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}
}