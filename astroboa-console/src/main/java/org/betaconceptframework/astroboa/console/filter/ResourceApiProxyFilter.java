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
package org.betaconceptframework.astroboa.console.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
/**
 * 
 * Extend RestEASY filter in order to Proxy Resource API Requests
 * We need to proxy requests in order to allow ajax calls from console javascript to read and write resources 
 * according to the access rights of the logged in user.
 * 
 * In other words the proxy retrieves from the session the repository to which the user is logged in and the user credentials, 
 * constructs an apropriate authorized request, send it to the Resource API and delivers the responce back to client (the browser) as if 
 * the request has been directly sent to the Resource API.   
 * 
 * The request path for proxied Resource API calls is the same as that used for normal Resource API requests but it does not contain the repository name, 
 * e.g. instead of /resource-api/{repository-id}/contentObject?param1=xxx&param2=yyy the request is /console/contentObject?param1=xxx&param2=yyy 
 * 
 * if the request path is not following the pattern {contentObject|taxonomy|topic}/* then the filter proceeds to the chain in order to allow seam and jsf to process the url 
 *  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ResourceApiProxyFilter extends FilterDispatcher {

	@Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String servletPath = request.getServletPath();
		
		if (servletPath != null && 
				(
					servletPath.startsWith("/contentObject") || 
					servletPath.startsWith("/taxonomy") || 
					servletPath.startsWith("/topic")
				)
		) {
			super.doFilter(servletRequest, servletResponse, filterChain);
		}
		else{
			filterChain.doFilter(servletRequest, servletResponse);
			
		}
		
		
	}
	
	
	
}