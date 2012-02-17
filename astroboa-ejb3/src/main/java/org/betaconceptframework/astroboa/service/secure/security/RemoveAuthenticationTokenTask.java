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
package org.betaconceptframework.astroboa.service.secure.security;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RemoveAuthenticationTokenTask extends TimerTask {

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String authenticationToken;
	private SecurityService securityService;
	
	public RemoveAuthenticationTokenTask(String authenticationToken, SecurityService securityService) {
		this.authenticationToken = authenticationToken;
		this.securityService = securityService;
		
		logger.debug("Created new authentikation token removal task for token {}", authenticationToken);
	}
	
	public void run() {
		
		if (authenticationToken != null && securityService !=null){
			securityService.purgeAuthenticationToken(authenticationToken);
			
			logger.debug("Removed repository context and lazy loader for authentication token {} from Thread {}", authenticationToken, Thread.currentThread());
			
		}
		
		cancel();
	}
	
}