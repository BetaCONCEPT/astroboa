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
package org.betaconceptframework.astroboa.portal.managedbean;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.APPLICATION)
@Name("cookieManager")
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CookieManager {
		
	private final static int defaultCookieMaxAge = 31536000; //1 year (in seconds)
	private final static String cookiePath= "/";
	
		
	public void setCookie(String cookieName, String cookieValue, int cookieMaxAge) {
		FacesContext ctx = FacesContext.getCurrentInstance();

		if (ctx != null) {
			HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
			Cookie cookie = new Cookie( cookieName, cookieValue );
			cookie.setMaxAge( cookieMaxAge );
			cookie.setPath(cookiePath);
			response.addCookie(cookie);
		}
	}
	
	public void setCookie(String cookieName, String cookieValue) {
		setCookie(cookieName, cookieValue, defaultCookieMaxAge);
	}
	
	public Cookie getCookie(String cookieName) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx != null) {
			return (Cookie) ctx.getExternalContext().getRequestCookieMap()
				.get( cookieName );
		}
		else {
			return null;
		}
	}

	public String getCookieValue(String cookieName) {
		Cookie cookie = getCookie( cookieName );
		return cookie==null ? null : cookie.getValue();
	}

	public void clearCookieValue(String cookieName) {
		Cookie cookie = getCookie(cookieName);
		if ( cookie!=null ) {
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();         
			cookie.setValue(null);
			cookie.setPath(cookiePath);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
}