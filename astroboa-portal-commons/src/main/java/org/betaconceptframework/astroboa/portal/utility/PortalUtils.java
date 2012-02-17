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
package org.betaconceptframework.astroboa.portal.utility;


import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;

/**
 * Commonly used search queries and methods possibly useful to all developers utilizing the repository to get / write content for / from the portal  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Scope(ScopeType.APPLICATION)
@Name("portalUtils")
public class PortalUtils {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public boolean resourceExists(String resourceRelativeFilePath){
		try{

			ServletContextResource resource = 
				new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), resourceRelativeFilePath);

			return resource.exists();

		}
		catch(Exception e){
			logger.warn("Could no find resource "+ resourceRelativeFilePath,e);
			return false;
		}
	}
	
	/*
	public Subject createSubjectForSystemUserAndItsRoles(String cmsRepositoryId){
		
		Subject subject = new Subject();
		
		//System identity
		subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));
		
		//Load default roles for SYSTEM USER
		//Must return at list one group named "Roles" in order to be 
		Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());
		
		for (CmsRole cmsRole : CmsRole.values()){
			rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, cmsRepositoryId)));
		}

		subject.getPrincipals().add(rolesPrincipal);
		
		return subject;
	}*/
	
}