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
package org.betaconceptframework.astroboa.resourceapi.filter;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Filter responsible to serve XML Schema files of definitions from astroboa repository.
 * 
 * It serves request url of name pattern 
 * 
 * <context-root>/f/definitionSchema/repository/<repository-id>/definitionFullPath/<forwardslash-delimited-definition-path>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class CmsDefinitionFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(CmsDefinitionFilter.class);

	private static final String CONTENT_OBJECT_TYPE_DEFINITION_FILTER_PREFIX = "/f/definitionSchema";

	private RepositoryService repositoryService;

	private DefinitionService definitionService;


	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		String repositoryId = null;
		String definitionFullPath = null;

		String requiredURIRegularExpression = "^" +
		httpServletRequest.getContextPath()+CONTENT_OBJECT_TYPE_DEFINITION_FILTER_PREFIX+
		"/"+
		"repository/(.+)" + // group 1
		"/"+
		"definitionFullPath/(.+{1,300})"  // group 2
		; 

		Pattern requiredURIPattern = Pattern.compile(requiredURIRegularExpression);
		Matcher uriMatcher = requiredURIPattern.matcher(httpServletRequest.getRequestURI());

		if (!uriMatcher.matches()) {
			logger.warn("Invalid request "+ httpServletRequest.getRequestURI());
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		else{

			repositoryId = uriMatcher.group(1);

			definitionFullPath = uriMatcher.group(2);
			
			if (StringUtils.isBlank(definitionFullPath)){
				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			
			String filename = (definitionFullPath.endsWith(".xsd") ? definitionFullPath : definitionFullPath+".xsd");

			repositoryService.loginAsAnonymous(repositoryId);

			String definitionSchema = definitionService.getCmsDefinition(definitionFullPath, ResourceRepresentationType.XSD);

			if (definitionSchema == null){
				logger.warn("Definition service retuned null for "+ httpServletRequest.getRequestURI());
				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else{

				try {

					ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

					httpServletResponse.setCharacterEncoding("UTF-8");
					httpServletResponse.setContentType("text/xml");
					httpServletResponse.setHeader("Content-Disposition", "attachment;filename="+filename);

					servletOutputStream.write(definitionSchema.getBytes());

					servletOutputStream.flush();

				}
				catch (Exception e) {
					logger.error("", e);
					httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}

	}

	public void init(FilterConfig filterConfig) throws ServletException {


	}

}
