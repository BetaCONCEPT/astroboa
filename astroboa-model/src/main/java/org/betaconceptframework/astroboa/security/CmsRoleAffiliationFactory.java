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
package org.betaconceptframework.astroboa.security;

import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Factory responsible to create appropriate role affiliation
 * for built in roles. Role Affiliation is comprised of
 * role name and active repository id
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsRoleAffiliationFactory {

	INSTANCE;


	public String getCmsRoleAffiliationForActiveRepository(CmsRole cmsRole){

		return getCmsRoleAffiliationForRepository(cmsRole, AstroboaClientContextHolder.getActiveRepositoryId());	
	}
	
	public String getCmsRoleAffiliationForRepository(CmsRole cmsRole, String repositoryId){

		if (cmsRole == null){
			return "";
		}

		return cmsRole.toString() + CmsConstants.AT_CHAR + 
		(repositoryId == null? "" :	repositoryId );
	}

}
