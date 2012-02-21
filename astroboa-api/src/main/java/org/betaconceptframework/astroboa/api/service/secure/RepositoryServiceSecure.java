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
package org.betaconceptframework.astroboa.api.service.secure;

import javax.security.auth.Subject;

import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteRepositoryServiceSecure;

/**
 * Secure Repository Service API.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryServiceSecure extends RemoteRepositoryServiceSecure {

	/**
	 * Connect to a cms repository using the provided Subject.
	 * 
	 * <p>
	 * This method should be used in cases where a set of specific actions must take place
	 * and user authentication is impossible. One example is Astroboa JAAS where there is a
	 * need to login to the IdentityStore provided by Astroboa but admin user credentials
	 * are not available. 
	 * </p>
	 * 
	 * 
	 * @param repositoryId Repository identifier as provided
	 * in astroboa-conf.xml
	 * 
	 * @param subject Subject instance containing all necessary roles.
	 *  
	 * @param permanentKey  representing a trusted client whose token is never expired
	 * 
	 * @return Authentication Token created by successful registration of provided subject to Astroboa
	 */
	String login(String repositoryId, Subject subject, String permanentKey);

}
