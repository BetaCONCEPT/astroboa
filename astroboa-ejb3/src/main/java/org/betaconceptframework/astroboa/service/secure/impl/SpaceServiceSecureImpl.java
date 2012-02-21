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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.secure.SpaceServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteSpaceServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({SpaceServiceSecure.class})
@Remote({RemoteSpaceServiceSecure.class})
@Stateless(name="SpaceServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class SpaceServiceSecureImpl  extends AbstractSecureAstroboaService implements SpaceServiceSecure{

	private SpaceService spaceService;
	
	@Override
	void initializeOtherRemoteServices() {
			spaceService = (SpaceService) springManagedRepositoryServicesContext.getBean("spaceService");
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public boolean deleteSpace(String spaceId, String authenticationToken) {
		return spaceService.deleteSpace(spaceId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<String> getContentObjectIdsWhichReferToSpace(String spaceId, String authenticationToken) {
		return spaceService.getContentObjectIdsWhichReferToSpace(spaceId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<String> getContentObjectIdsWhichResideInSpace(String spaceId, String authenticationToken) {
		return spaceService.getContentObjectIdsWhichResideInSpace(spaceId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public int getCountOfContentObjectIdsWhichReferToSpace(String spaceId, String authenticationToken) {
		return spaceService.getCountOfContentObjectIdsWhichReferToSpace(spaceId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public int getCountOfContentObjectIdsWhichResideInSpace(String spaceId, String authenticationToken) {
		return spaceService.getCountOfContentObjectIdsWhichResideInSpace(spaceId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Space getOrganizationSpace(String authenticationToken) {
		return spaceService.getOrganizationSpace();
	}

	
	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Space getSpace(String spaceId, String locale, String authenticationToken) {
		return spaceService.getSpace(spaceId, locale);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public Space saveSpace(Space space, String authenticationToken) {
		return spaceService.saveSpace(space);
		
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public CmsOutcome<Space> searchSpaces(SpaceCriteria spaceCriteria, String authenticationToken) {

		return spaceService.searchSpaces(spaceCriteria);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel, String authenticationToken) {
		return spaceService.getSpace(spaceIdOrName, output, fetchLevel);
	}

	@RolesAllowed("ROLE_CMS_EDITOR")
	public Space save(Object space, String authenticationToken) {
		return spaceService.save(space);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T searchSpaces(SpaceCriteria spaceCriteria,
			ResourceRepresentationType<T> output, String authenticationToken) {
		return spaceService.searchSpaces(spaceCriteria, output);
	}


}
