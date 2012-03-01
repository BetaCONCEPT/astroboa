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
package org.betaconceptframework.astroboa.resourceapi.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
/**
 * 
 * Extend RestEASY filter in order to serve all static files related to astroboa-explorer.
 * 
 * It by passes RestEASY dispatcher if user wants a resource for astroboa-explorer, that is any file from
 * astroboa-explorer directory
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class AstroboaResourceApiDispatcher extends FilterDispatcher {

	@Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String servletPath = request.getPathInfo();
		String servletPath2 = request.getContextPath();
		String servletPath3 = request.getPathInfo();
		String servletPath4 = request.getQueryString();
		String servletPath5 = request.getRequestURI();
		String servletPath6 = request.getServerName();
		String servletPath7 = request.getRequestURL().toString();
		
		
		if (servletPath != null && servletPath.startsWith("/astroboa-explorer")){
			filterChain.doFilter(servletRequest, servletResponse);	
		}
		else{
			super.doFilter(servletRequest, servletResponse, filterChain);
		}
		
		
	}
	
	
	
}