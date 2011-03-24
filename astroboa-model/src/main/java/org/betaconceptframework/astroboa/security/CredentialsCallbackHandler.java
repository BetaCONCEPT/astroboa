/*
 * Copyright (C) 2005-2008 BetaCONCEPT LP.
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
package org.betaconceptframework.astroboa.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CredentialsCallbackHandler implements CallbackHandler{
		
	private AstroboaCredentials credentials;

	public CredentialsCallbackHandler(AstroboaCredentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		
		for (int i=0; i < callbacks.length; i++)
        {
		   if (callbacks[i] instanceof NameCallback){
              ( (NameCallback) callbacks[i] ).setName(credentials.getUsername());
           }
           else if (callbacks[i] instanceof PasswordCallback){
              ( (PasswordCallback) callbacks[i] ).setPassword( credentials.getPassword() != null ? 
            		  credentials.getPassword() : null );
           }
           else if (callbacks[i] instanceof AstroboaAuthenticationCallback){
        	   
        	   if (credentials.getIdentityStoreRepositoryJNDIName() != null){
        		   ( (AstroboaAuthenticationCallback) callbacks[i] ).setIdentityStoreLocation(credentials.getIdentityStoreRepositoryJNDIName(), true);
        	   }
        	   else{
        		   ( (AstroboaAuthenticationCallback) callbacks[i] ).setIdentityStoreLocation(credentials.getIdentityStoreRepositoryId(), false);
        	   }
        	   
        	   ( (AstroboaAuthenticationCallback) callbacks[i] ).setRepositoryId(credentials.getRepositoryId());
        	   ( (AstroboaAuthenticationCallback) callbacks[i] ).setSecretKey(credentials.getSecretKey());
           }
           else{
              throw new UnsupportedCallbackException(callbacks[i]);
           }
        }
		
	}
	
}
