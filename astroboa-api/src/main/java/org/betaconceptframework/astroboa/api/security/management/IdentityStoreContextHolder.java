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
package org.betaconceptframework.astroboa.api.security.management;

/**
 * 
 * Identity Store Context holding necessary thread local variables.
 * 
 * It is used mainly when Astroboa needs to pass information about active repository
 * to external Identity Stores.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IdentityStoreContextHolder {

	private static ThreadLocal<String> activeRepositoryId = new ThreadLocal<String>();
	
	public static String getActiveRepositoryId(){
		return activeRepositoryId.get();
	}
	
	public static void setActiveRepositoryId(String repositoryId){
		activeRepositoryId.set(repositoryId);
	}
	
	public static void clear(){
		activeRepositoryId.set(null);
	}
}
