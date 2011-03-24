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
package org.betaconceptframework.astroboa.service.secure.security;

import org.betaconceptframework.astroboa.context.AstroboaClientContext;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AuthenticationTokenEntry {

	private RemoveAuthenticationTokenTask removeAuthenticationTokenTask;
	private AstroboaClientContext clientContext;
	private int authenticationTokenTimeout;
	
	public AuthenticationTokenEntry(AstroboaClientContext clientContext) {
		
		this.clientContext = clientContext;
		this.authenticationTokenTimeout = -1;
	}

	
	public void setAuthenticationTokenTimeout(int authenticationTokenTimeout) {
		this.authenticationTokenTimeout = authenticationTokenTimeout;
	}


	public RemoveAuthenticationTokenTask getRemoveAuthenticationTokenTask() {
		return removeAuthenticationTokenTask;
	}

	public AstroboaClientContext getClientContext() {
		return clientContext;
	}

	public int getAuthenticationTokenTimeout() {
		return authenticationTokenTimeout;
	}

	public void resetIdleTime(RemoveAuthenticationTokenTask removeAuthenticationTokenTask) {
		if (this.removeAuthenticationTokenTask != null){
			this.removeAuthenticationTokenTask.cancel();
			
			this.removeAuthenticationTokenTask = null;
		}
		
		this.removeAuthenticationTokenTask = removeAuthenticationTokenTask;
		
	}
	
	
		
}
