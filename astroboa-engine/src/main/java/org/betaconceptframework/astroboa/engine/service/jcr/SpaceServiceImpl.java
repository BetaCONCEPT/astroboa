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

import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.engine.jcr.dao.SpaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class SpaceServiceImpl  implements SpaceService{

	@Autowired
	private SpaceDao spaceDao;
	
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public boolean deleteSpace(String spaceId) {
		try{
			return spaceDao.deleteSpace(spaceId);
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
	public Space saveSpace(Space space) {
		try{
		return spaceDao.saveSpace(space);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
		
	}

	public Space getOrganizationSpace() {
		try{
		return spaceDao.getOrganizationSpace();   
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
	
	@Deprecated
	public Space getSpace(String spaceId, String locale) {
		try{
		return spaceDao.getSpace(spaceId, ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Deprecated
	public CmsOutcome<Space> searchSpaces(SpaceCriteria spaceCriteria) {
		try{
		return spaceDao.searchSpaces(spaceCriteria, ResourceRepresentationType.SPACE_LIST);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public List<String> getContentObjectIdsWhichReferToSpace(String spaceId) {
		try{
		return spaceDao.getContentObjectIdsWhichReferToSpace(spaceId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public int getCountOfContentObjectIdsWhichReferToSpace(String spaceId) {
		try{
		return spaceDao.getCountOfContentObjectIdsWhichReferToSpace(spaceId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public List<String> getContentObjectIdsWhichResideInSpace(String spaceId) {
		try{
		return spaceDao.getContentObjectIdsWhichResideInSpace(spaceId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public int getCountOfContentObjectIdsWhichResideInSpace(String spaceId) {
		try{
		return spaceDao.getCountOfContentObjectIdsWhichResideInSpace(spaceId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public <T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel) {
		try{
			return spaceDao.getSpace(spaceIdOrName, output, fetchLevel);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Space save(Object space) {
		try{
			return spaceDao.saveSpace(space);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Override
	public <T> T searchSpaces(SpaceCriteria spaceCriteria, ResourceRepresentationType<T> output) {
		try{
			return spaceDao.searchSpaces(spaceCriteria, output);
			}
			catch(CmsException e){
				throw e;
			}
			catch (Exception e) { 
				throw new CmsException(e); 		
			}	}


}
