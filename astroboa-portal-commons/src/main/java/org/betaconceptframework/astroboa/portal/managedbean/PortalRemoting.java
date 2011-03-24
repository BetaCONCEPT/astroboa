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


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.web.ServletContexts;

/**
 * Implements remote methods that can be asynchronously called in javascript
 * to access server functionality, server state, portal variables, etc.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Name("portalRemoting")
@Scope(ScopeType.SESSION)
public class PortalRemoting {
	

	@WebRemote
	public String getPortalContext() {
		String portalContextPath = "no path";
		ServletContexts servletContexts = ServletContexts.instance();
		if (servletContexts != null) {
			portalContextPath = servletContexts.getRequest().getContextPath();
		}
		
		return portalContextPath;
	}
	
}
