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
package org.betaconceptframework.astroboa.resourceapi.utility;

import java.util.concurrent.ConcurrentHashMap;

import org.betaconceptframework.astroboa.client.AstroboaClient;


/**
 * This class serves as a cache of AstroboaClients, so that
 * clients won't have to login in every request.
 * 
 * We use enum, as it is a nice solution when you want a Singleton...
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public enum AstroboaClientCache {

	Instance;
	
	private ConcurrentHashMap<String, AstroboaClient> clients = new ConcurrentHashMap<String, AstroboaClient>();

	public AstroboaClient get(String token){
		
		AstroboaClient astroboaClient = clients.get(token);
		
		if (astroboaClient != null){
			//Client's session may have been expired
			if (astroboaClient.sessionHasExpired()){
				clients.remove(token);
				return null;
			}
			return astroboaClient.copy();
		}
		
		return null;
	}

	public AstroboaClient cache(AstroboaClient newClient, String token){
		
		AstroboaClient astroboaClient = clients.putIfAbsent(token, newClient);
		
		if (astroboaClient == null){
			return newClient;
		}
		else{
			//Nullify to ease garbage collection
			newClient = null;
			
			return astroboaClient.copy();
		}
		
	}
}
