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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Filter responsible to serve binary channel contents from jcr repository.
 * 
 * It serves request url of name pattern <context-root>/binaryChannel/<binary-channel-content-relative-path-to-repository-home-directory>
 * 
 * In order to provide stream for binary channel it replaces <context-root>/binaryChannel/ with
 * absolute path of repository home directory which must be provided by extended classes
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */

public abstract class BinaryChannelDynamicFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String BINARY_CHANNEL_PATH = "/binaryChannel";
	
	protected String repositoryHomeDir;

	public void destroy() {
				
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		
		String absoluteBinaryChannelPath = createAbsoluteBinaryChannelPath(httpServletRequest.getContextPath(), httpServletRequest.getRequestURI());
		
		if (StringUtils.isNotBlank(absoluteBinaryChannelPath))
		{
		
		InputStream contentAsStream = null;

		try {
			contentAsStream = new FileInputStream(absoluteBinaryChannelPath);
			
			if (contentAsStream != null)
			{
				ServletOutputStream servletOutputStream = response.getOutputStream();

				IOUtils.copy(contentAsStream, servletOutputStream);
				
				servletOutputStream.flush();

			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			
			//Close Stream
			if (contentAsStream != null)
			{
				try {
					contentAsStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			else
				chain.doFilter(request, response);
		}
		}
		else
			chain.doFilter(request, response);
		
	}

	private String createAbsoluteBinaryChannelPath(String contextPath, String requestURI) {
		
		if (requestURI.equals(contextPath+ BINARY_CHANNEL_PATH+ "/"))
			return null; //No path is specified. Request contains no info about binary channel path 

		//Remove binrayChannel prefix
		return requestURI.replaceAll(contextPath+ BINARY_CHANNEL_PATH+ "/", repositoryHomeDir +File.separator);
		
	}

	
	
	
	public void init(FilterConfig filterConfig) throws ServletException {
		
		//Retrieve JCR home directory
		getRepositoryHomeDir();
		
		if (StringUtils.isBlank(repositoryHomeDir))
			logger.warn("Repository home directory is not specified.");
	}

	protected abstract void getRepositoryHomeDir();

}
