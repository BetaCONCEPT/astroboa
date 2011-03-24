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
package org.betaconceptframework.astroboa.console.security;

import static org.jboss.seam.ScopeType.SESSION;

import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

/**
 * Seam main class Identity is overridden as we need to 
 * attach active repository id when checking when a logged in user
 * has a specified Astroboa built in role or not.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = Install.APPLICATION) //With this annotation BuiltIn Identity is overridden
@BypassInterceptors
@Startup
public class CmsIdentity extends Identity{

	
	@Override
	public void checkRole(String role) {
		super.checkRole(processRole(role));
	}


	@Override
	public boolean hasRole(String role) {
		return super.hasRole(processRole(role));
	}

	
	/*
	 * In case role is a Astroboa built in role
	 * we need to attach active repository Id
	 */
	private String processRole(String role) {
		
		if (role == null){
			return role;
		}
		
		try{
			CmsRole cmsRole = CmsRole.valueOf(role);
			
			return CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(cmsRole);
		}
		catch(Exception e){
			//Role is not a built in role continue as is
			return role;
		}
		
		
	}
}
