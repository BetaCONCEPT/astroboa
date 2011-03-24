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
import org.jboss.ejb3.annotation.Management;


/**
 * This interface provides various methods for authentication token
 * 
 * @Management This annotation will wrap the bean as an MBean and install 
 * it in the JBoss MBean Server. The operations and attributes defined in the @Management 
 * interfaces become MBean operations and attributes for the installed MBean.. 
 * The underlying bean instance is the same as the one accessed via the @Local or @Remote interfaces.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Management
public interface SecurityService {

	void start() throws Exception;

	/**
	 * Searches AstroboaClientContext for provided authentication token, registers it to current execution Thread
	 * and activates it.
	 * @param authenticationToken
	 * @throwsContextForAuthenticationTokenNotFoundException If no AstroboaClientContext could be found for the provided authentication token
	 */
	void registerAndActivateClientContextForAuthenticationToken(String authenticationToken);
	
	/**
	 * Adds provided AstroboaClientContext to context map in order to be available
	 * in the following requests until it is expired
	 * 
	 * @param clientContext
	 * @param b 
	 */
	void addClientContexToValidContextMap(AstroboaClientContext clientContext, boolean authenticactionTokenNeverExpires);

	/**
	 * Remove AstroboaClientContext corresponding to provided authentication token from the context map
	 * 
	 * @param authenticationToken
	 */
	void purgeAuthenticationToken(String authenticationToken);

	/**
	 * Reset authentication timeout for provided authentication token 
	 * @param authenticationToken
	 */
	void resetAuthenticationTokenTimeout(String authenticationToken);

	/**
	 * Check whether token has expired.
	 * 
	 * @param authenticationToken Authentication Token created upon successful login to Astroboa repository
	 * @return <code>true</code> if token has expired, <code>false</code> otherwise
	 */

	boolean tokenHasExpired(String authenticationToken);
}
