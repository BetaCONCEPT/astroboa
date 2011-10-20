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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.secure.SpaceServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Remote Space Service Wrapper responsible to connect to the provided repository
 * before any of tis method is called. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceServiceClientWrapper extends AbstractClientServiceWrapper implements SpaceService{

	private SpaceServiceSecure spaceServiceSecure;

	public SpaceServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	void resetService() {
		spaceServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				spaceServiceSecure = (SpaceServiceSecure)connectToLocalService(SpaceServiceSecure.class);
			}
			else{
				spaceServiceSecure = (SpaceServiceSecure) connectToRemoteService(SpaceServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			spaceServiceSecure = null;
		}

		return spaceServiceSecure != null;
	}


	public boolean deleteSpace(String spaceId) {

		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.deleteSpace(spaceId, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}

	}


	public List<String> getContentObjectIdsWhichReferToSpace(String spaceId) {

		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getContentObjectIdsWhichReferToSpace(spaceId, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}



	public List<String> getContentObjectIdsWhichResideInSpace(String spaceId) {

		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getContentObjectIdsWhichResideInSpace(spaceId, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}



	public int getCountOfContentObjectIdsWhichReferToSpace(String spaceId) {

		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getCountOfContentObjectIdsWhichReferToSpace(spaceId, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}

	}



	public int getCountOfContentObjectIdsWhichResideInSpace(String spaceId) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getCountOfContentObjectIdsWhichResideInSpace(spaceId, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}

	}



	public Space getOrganizationSpace() {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getOrganizationSpace(getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}

	}



	public Space getSpace(String spaceId, String locale) {

		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getSpace(spaceId, locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}


	public Space saveSpace(Space space) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.saveSpace(space, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}



	public CmsOutcome<Space> searchSpaces(SpaceCriteria spaceCriteria) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.searchSpaces(spaceCriteria, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}

	@Override
	public <T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.getSpace(spaceIdOrName, output, fetchLevel, getAuthenticationToken());
			
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}

	@Override
	public Space save(Object space) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.save(space, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}

	@Override
	public <T> T searchSpaces(SpaceCriteria spaceCriteria, ResourceRepresentationType<T> output) {
		if (spaceServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return spaceServiceSecure.searchSpaces(spaceCriteria, output, getAuthenticationToken());
		}
		else{
			throw new CmsException("SpaceService reference was not found");
		}
	}


}
