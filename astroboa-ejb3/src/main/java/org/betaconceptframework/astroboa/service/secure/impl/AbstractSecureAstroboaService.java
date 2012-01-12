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
package org.betaconceptframework.astroboa.service.secure.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * All Astroboa Secure Services implementations must subclass this class.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class AbstractSecureAstroboaService{

	@Resource(name="astroboa.engine.context", mappedName="java:jboss/astroboa.engine.context")
	ApplicationContext springManagedRepositoryServicesContext;
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	@PostConstruct
	public void initialize() {
		
		System.out.println("Normal "+ System.identityHashCode(springManagedRepositoryServicesContext));
		
		initializeOtherRemoteServices();
		
		RoleRegistry.INSTANCE.registerRolesAllowedPerMethodForClass(this.getClass());
	}
	
	abstract void initializeOtherRemoteServices();

}
