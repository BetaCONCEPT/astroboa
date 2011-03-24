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
package org.betaconceptframework.astroboa.portal.managedbean;

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
 * Replacement class for SEAM Credentials.
 * When authenticating with astroboa application security policy name (astroboa default JAAS module) 
 * besides the username and password 
 * the "identityStoreRepositoryId" 
 * and in certain cases the "identityStoreRepositoryJNDIName" are required.
 * 
 * "identityStoreRepositoryId" is the id of the astroboa repository that holds user identities. The common case is that astroboa itself manages the user identities and 
 * in such a case we just need the id of the content repository that stores the identities.
 * 
 * There is also the case that the user identity store is not a astroboa repository but it is externally managed, e.g. through LDAP. 
 * In this case the JNDI name of the external identity service is required by astroboa JAAS module for accessing the external Identity Store and performing the authentication.
 * The "identityStoreRepositoryJNDIName" property in Credentials will holds this JNDI name.  
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
