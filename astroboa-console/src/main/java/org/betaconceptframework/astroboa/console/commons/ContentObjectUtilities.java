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

package org.betaconceptframework.astroboa.console.commons;


import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.betaconceptframework.bean.AbstractBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.springframework.web.context.support.ServletContextResource;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Name("contentObjectUtilities")
@Scope(ScopeType.EVENT)
public class ContentObjectUtilities extends AbstractBean {
	
	private static final long serialVersionUID = 1L;
	
	public final String JPEG = "image/jpeg";
	public final String PNG = "image/png";
	public final String X_PNG = "image/x-png";
	public final String GIF = "image/gif";
	
	public boolean isImageMimeTypeWhichCanBeRenderedByBrowser(String mimeType) {

		if (JPEG.equals(mimeType) ||
				PNG.equals(mimeType) ||
				X_PNG.equals(mimeType) ||
				GIF.equals(mimeType)) {
			return true;
		}
		else
			return false;
		
	}
	
	public String getMimeTypeIconFilePath(String mimeType) {
		
		// The Default Icon if we do not find a more appropriate one
		String defaultMimeTypeIconFilePath = "images/cms-icons/text1.png";
		String mimeTypeIconFilePath;
		
		if (mimeType != null){
			try{
				mimeTypeIconFilePath = "images/mime-type_icons/" + mimeType + ".png";
				ServletContextResource mimeTypeIconResource = new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), mimeTypeIconFilePath);
				if (mimeTypeIconResource.exists()) 
					return mimeTypeIconFilePath;
				else{
					logger.warn("No icon found for mime type "+ mimeType);
					return defaultMimeTypeIconFilePath;
				}
			}
			catch(Exception e){
				logger.error("An error occured while looking the image file resource (icon) for mime type "+ mimeType, e);
				return defaultMimeTypeIconFilePath;
			}
			
		}
		
		logger.error("A null mime type value was provided. The default mime type icon path will be returned");
		return defaultMimeTypeIconFilePath;
	}
}
