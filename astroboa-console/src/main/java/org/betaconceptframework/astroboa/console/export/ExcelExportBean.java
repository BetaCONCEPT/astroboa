/**
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
package org.betaconceptframework.astroboa.console.export;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectSelectionBean;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Name("excelExportBean")
@Scope(ScopeType.EVENT)
public class ExcelExportBean {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AstroboaClient astroboaClient;
	
	public void exportContentObjectSelection(ContentObjectSelectionBean contentObjectSelection, String locale) {
		
		if (contentObjectSelection == null || CollectionUtils.isEmpty(contentObjectSelection.getSelectedContentObjects())) {
			JSFUtilities.addMessage(null, "content.search.exportResults.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (StringUtils.isEmpty(locale)) {
			locale = "en";
			logger.warn("Provided Locale was empty. The default locale which is 'en' will be used");
		}
		
		List<ContentObjectUIWrapper> contentObjectUiWrapperList = contentObjectSelection.getSelectedContentObjects();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("UTF-8");

			WorkbookBuilder workbookBuilder = new WorkbookBuilder(astroboaClient.getDefinitionService(), locale);
			
			int rowIndex = 2;
			for (ContentObjectUIWrapper contentObjectUiWrapper : contentObjectUiWrapperList) {
				ContentObject contentObject = contentObjectUiWrapper.getContentObject();
				
				//Reload object
				ContentObject object = astroboaClient.getContentService().getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL, CacheRegion.NONE, null, false);
				
				workbookBuilder.addContentObjectToWorkbook(object);

				++rowIndex;
				
				//Limit to the first 5000 content objects
				if (rowIndex > 5000){
					break;
				}
			}
			
			String filename = createFilename(workbookBuilder);
			
			response.setHeader("Content-Disposition", "attachment;filename=" + filename + ".xls");
			
			try {
				ServletOutputStream servletOutputStream = response.getOutputStream();

				//workbook.write(servletOutputStream);
				workbookBuilder.getWorkbook().write(servletOutputStream);

				servletOutputStream.flush();

				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing excel workbook to servlet output stream", e);
				JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				workbookBuilder.clear();
				workbookBuilder = null;
			}


		}
		else {
			JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
		}

	}


	private String createFilename(WorkbookBuilder workbookBuilder) {
		String filename = workbookBuilder.getWorkbookName();
		
		if (filename.length()>50){
			filename = filename.substring(0, 49);
		}
		
		filename = filename + "-"+DateUtils.format(Calendar.getInstance(), "ddMMyyyyHHmm");
		return filename;
	}
	
	
	public void exportContentObjectList(ContentObjectCriteria contentObjectCriteria, String locale) {
		
		// remove offset from criteria and add a limit of 5000
		contentObjectCriteria.setOffset(0);
		contentObjectCriteria.setLimit(5000);
		
		// run the query
		CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		if (cmsOutcome.getCount() == 0) {
			JSFUtilities.addMessage(null, "content.search.exportResults.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (StringUtils.isEmpty(locale)) {
			locale = "en";
			logger.warn("Provided Locale was empty. The default locale which is 'en' will be used");
		}
		
		List<ContentObject> cmsOutcomeRowList = cmsOutcome.getResults();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("UTF-8");

			WorkbookBuilder workbookBuilder = new WorkbookBuilder(astroboaClient.getDefinitionService(), locale);
			
			int rowIndex = 2;
			for (ContentObject contentObject : cmsOutcomeRowList) {
				
				//Reload object
				ContentObject object = astroboaClient.getContentService().getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL, CacheRegion.NONE, null, false);

				workbookBuilder.addContentObjectToWorkbook(object);

				++rowIndex;

			}
			
			String filename = createFilename(workbookBuilder);

			response.setHeader("Content-Disposition", "attachment;filename=" + filename + ".xls");
			//autoSizeColumns(sheet);
			
			try {
				ServletOutputStream servletOutputStream = response.getOutputStream();

				//workbook.write(servletOutputStream);
				workbookBuilder.getWorkbook().write(servletOutputStream);
				
				servletOutputStream.flush();

				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing excel workbook to servlet output stream", e);
				JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				workbookBuilder.clear();
				workbookBuilder = null;
			}


		}
		else {
			JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
		}

	}
	
}
