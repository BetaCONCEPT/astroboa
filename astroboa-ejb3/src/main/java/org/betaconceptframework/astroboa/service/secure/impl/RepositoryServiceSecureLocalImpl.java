/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.service.secure.impl;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.service.secure.RepositoryServiceSecure;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.service.secure.security.SecurityService;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({RepositoryServiceSecure.class})
@Stateless(name="RepositoryServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
public class RepositoryServiceSecureLocalImpl extends RepositoryServiceSecureImpl implements RepositoryServiceSecure{

	@Resource(name="SecurityService", mappedName="SecurityService/local")
	private SecurityService securityService;

	@Override
	protected SecurityService getSecurityService() {
		return securityService;
	}


	public String login(String repositoryId, Subject subject, String permanentKey) {
		String authenticationToken =  repositoryService.login(repositoryId, subject, permanentKey);
		
		securityService.addClientContexToValidContextMap(AstroboaClientContextHolder.getActiveClientContext(), 
				StringUtils.isNotBlank(permanentKey));
		
		return authenticationToken;

	}
}
