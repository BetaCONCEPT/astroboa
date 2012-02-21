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
package org.betaconceptframework.astroboa.console.security;

import javax.security.auth.callback.CallbackHandler;

import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.security.CredentialsCallbackHandler;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Credentials;

/**
 * Replacement class for SEAM Credentials, as more thanusername and password
 * are needed when authenticating at least with astroboa application
 * security policy name
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("org.jboss.seam.security.credentials")
@Scope(ScopeType.SESSION)
@Install(precedence = Install.APPLICATION) //With this annotation BuiltIn Credentials are overridden
@BypassInterceptors
public class CmsCredentials extends Credentials{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3049816361421108426L;

	private String  identityStoreRepositoryId;
	
	private String identityStoreRepositoryJNDIName;
	
	@Override
	public CallbackHandler createCallbackHandler() {
		
		char[] pass = getPassword() == null ? new char[0] : getPassword().toCharArray();
		
		AstroboaCredentials credentials = new AstroboaCredentials(getUsername(), pass,  
				identityStoreRepositoryId,identityStoreRepositoryJNDIName, null, null);
		
		return new CredentialsCallbackHandler(credentials);
		
	}

	/**
	 * @param identityStoreRepositoryId the identityStoreRepositoryId to set
	 */
	public void setIdentityStoreRepositoryId(String identityStoreRepositoryId) {
		this.identityStoreRepositoryId = identityStoreRepositoryId;
	}

	/**
	 * @param identityStoreRepositoryJNDIName the identityStoreRepositoryJNDIName to set
	 */
	public void setIdentityStoreRepositoryJNDIName(
			String identityStoreRepositoryJNDIName) {
		this.identityStoreRepositoryJNDIName = identityStoreRepositoryJNDIName;
	}
	
	
	


	
}
