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
package org.betaconceptframework.astroboa.console.jsf;


import java.io.IOException;
import java.io.InputStream;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BinaryChannelLoader extends AbstractUIBean {
	
	private ContentService contentService;
	
	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void downloadBinaryChannel() {

		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			
			String selectedBinaryChannelId = JSFUtilities.getRequestParameter("binaryChannelId");
			
			
			
			BinaryChannel binaryChannel;
			//Retrieve the binary data of the Primary Binary Channel selected Content Object. Remember that for efficiency the binary data of binary channels are not retreived when a content object is retreived
			try {
				binaryChannel = getContentService().getBinaryChannelById(selectedBinaryChannelId);
			} catch (CmsException e) {
				throw new RuntimeException(e);
			}


			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

			response.setCharacterEncoding("UTF-8");
			response.setContentType(binaryChannel.getMimeType());
			response.setHeader("Content-Disposition", "attachment;filename=" + binaryChannel.getSourceFilename());	

			InputStream contentAsStream = binaryChannel.getContentAsStream();
			
			response.setContentLength((int)binaryChannel.getSize());

			try {
				
				if (contentAsStream != null)
				{
					ServletOutputStream servletOutputStream = response.getOutputStream();

					IOUtils.copy(contentAsStream, servletOutputStream);
					
					servletOutputStream.flush();

					facesContext.responseComplete();

				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally
			{
				binaryChannel = null;
				
				//Close Stream
				if (contentAsStream != null)
					try {
						contentAsStream.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			}
			
		}

	}
}
