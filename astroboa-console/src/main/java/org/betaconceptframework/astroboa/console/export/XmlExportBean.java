/**
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
package org.betaconceptframework.astroboa.console.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectSelectionBean;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.FilenameUtils;
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

@Name("xmlExportBean")
@Scope(ScopeType.EVENT)
public class XmlExportBean {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String selectedTaxonomyId;
	
	private AstroboaClient astroboaClient;
	
	public void exportTaxonomy(Taxonomy taxonomy) {
		
		if (taxonomy == null || taxonomy.getName() == null){
			JSFUtilities.addMessage(null, "taxonomy.export.nullTaxonomy", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try{
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (!facesContext.getResponseComplete()) {
				HttpServletResponse response = (HttpServletResponse) facesContext
				.getExternalContext().getResponse();
				response.setContentType("text/xml");
				response.setCharacterEncoding("UTF-8");

				response.setHeader("Content-Disposition", "attachment;filename=" + taxonomy.getName() + ".xml");
				
				ServletOutputStream servletOutputStream = response.getOutputStream();

				
				Taxonomy taxonomyToBeExported = astroboaClient.getTaxonomyService().getTaxonomy(taxonomy.getName(), ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL, true);
				
				//Currently this is not supported at back end. 
				//loadTree(taxonomyToBeExported.getRootTopics());
				
				//Remove all unnecessary info. mostly identifiers
				removeUnnecessaryInfo(taxonomyToBeExported);
				
				IOUtils.write(taxonomyToBeExported.xml(true), servletOutputStream);

				servletOutputStream.flush();

				facesContext.responseComplete();
			}
		}
		catch(Exception e){
			logger.error("An error occurred while writing xml to servlet output stream", e);
			JSFUtilities.addMessage(null, "object.action.export.message.error", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	public void exportTaxonomy() {
		if (StringUtils.isBlank(selectedTaxonomyId)){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		Taxonomy taxonomy;
		try {
			taxonomy = astroboaClient.getTaxonomyService().getTaxonomy(selectedTaxonomyId, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
					
			exportTaxonomy(taxonomy);
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
	}
	
	/**
	 * @param taxonomy
	 */
	private void removeUnnecessaryInfo(Taxonomy taxonomy) {
		
		Taxonomy tempTaxonomyWithNameOnly = astroboaClient.getCmsRepositoryEntityFactory().newTaxonomy();
		tempTaxonomyWithNameOnly.setName(taxonomy.getName());
		
		removeUnnecessaryInfoForTopic(taxonomy.getRootTopics(), tempTaxonomyWithNameOnly);
		
		taxonomy.setId(null);
		
	}

    private void removeUnnecessaryInfoForTopic(List<Topic> topics, Taxonomy tempTaxonomyWithNameOnly) {
		
		if (CollectionUtils.isNotEmpty(topics)){
			for (Topic topic : topics){
				
				if (topic.isChildrenLoaded()){
					removeUnnecessaryInfoForTopic(topic.getChildren(), tempTaxonomyWithNameOnly);
				}
				
				topic.setId(null);
				topic.getOwner().setId(null);
				
				if (topic.getParent() == null){
					topic.setTaxonomy(tempTaxonomyWithNameOnly);
				}
			}
		}
		
	}

	private void loadTree(List<Topic> topics) {
		
		if (CollectionUtils.isNotEmpty(topics)){
			for (Topic topic : topics){
				
				if (!topic.isChildrenLoaded()){
					loadTree(topic.getChildren());
				}
			}
		}
		
	}

	public void exportContentObjectSelection(ContentObjectSelectionBean contentObjectSelection, String zipFilename) {
		
		
		if (contentObjectSelection == null || CollectionUtils.isEmpty(contentObjectSelection.getSelectedContentObjects())) {
			JSFUtilities.addMessage(null, "object.action.export.message.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/zip");
			response.setCharacterEncoding("UTF-8");

			zipFilename = generateValidZipFilename(zipFilename);
			
			response.setHeader("Content-Disposition", "attachment;filename=" + zipFilename+ ".zip");
			
			File tempZip = null;
			
			FileOutputStream fos = null;
			
			ZipOutputStream zipOutputStream = null;
			
			FileInputStream zipFileInputStream = null;
			
			try {
				tempZip = File.createTempFile(zipFilename,"zip");
				
				fos =  new FileOutputStream(tempZip);
				zipOutputStream = new ZipOutputStream(fos);

				List<ContentObjectUIWrapper> results = contentObjectSelection.getSelectedContentObjects();
				
				//Keep all filenames created to catch duplicate
				List<String> filenameList = new ArrayList<String>();
				
				//Allow only the first 500
				int numbreOfContentObjects = 500;
				
				long now = System.currentTimeMillis();
				
				for(ContentObjectUIWrapper contentObjectUiWraper : results){
					
					if (numbreOfContentObjects == 0){
						break;
					}
					ContentObject contentObject = contentObjectUiWraper.getContentObject();
					Calendar created = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
					
					String folderPath = DateUtils.format(created, "yyyy/MM/dd");
					
					long nowXml = System.currentTimeMillis();
					String xml = contentObject.xml(false);
					
					//Build filename
					String finalName = buildFilename(folderPath, created, contentObject, filenameList);
					
					filenameList.add(finalName);
					
					long nowZip = System.currentTimeMillis();
					zipOutputStream.putNextEntry(new ZipEntry(finalName));

					IOUtils.write(xml, zipOutputStream);

					zipOutputStream.closeEntry();
				
					
					numbreOfContentObjects--;
				}
				
				zipOutputStream.close();
				fos.close();
				
				long nowFileZip = System.currentTimeMillis();
				zipFileInputStream = new FileInputStream(tempZip);
				IOUtils.copy(zipFileInputStream, response.getOutputStream());
				zipFileInputStream.close();
				
				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing xmlto servlet output stream", e);
				JSFUtilities.addMessage(null, "object.action.export.message.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				if (fos !=null){
					IOUtils.closeQuietly(fos);
				}
				if (zipOutputStream!= null){
					IOUtils.closeQuietly(zipOutputStream);
				}
				
				if (zipFileInputStream!= null){
					IOUtils.closeQuietly(zipFileInputStream);
				}
				
				if (tempZip!=null){
					FileUtils.deleteQuietly(tempZip);
				}
			}


		}
		else {
			JSFUtilities.addMessage(null, "object.action.export.message.error", null, FacesMessage.SEVERITY_WARN);
		}
	}

	private String generateValidZipFilename(String zipFilename) {
		if (StringUtils.isBlank(zipFilename)){
			zipFilename = "listOfContentObjects";
		}
		else{
			zipFilename = FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(zipFilename.trim()); 
		}
		return zipFilename;
	}
	
	
	public void exportContentObjectList(ContentObjectCriteria contentObjectCriteria, String zipFilename) {
		
		// run the query
		CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		if (cmsOutcome == null || cmsOutcome.getCount() == 0) {
			JSFUtilities.addMessage(null, "object.action.export.message.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/zip");
			response.setCharacterEncoding("UTF-8");

			zipFilename = generateValidZipFilename(zipFilename);
			
			response.setHeader("Content-Disposition", "attachment;filename=" +zipFilename+ ".zip");
			
			File tempZip = null;
			
			FileOutputStream fos = null;
			
			ZipOutputStream zipOutputStream = null;
			
			FileInputStream zipFileInputStream = null;
			
			try {
				tempZip = File.createTempFile(zipFilename,"zip");
				
				fos =  new FileOutputStream(tempZip);
				zipOutputStream = new ZipOutputStream(fos);

				List<ContentObject> results = cmsOutcome.getResults();
				
				//Keep all filenames created to catch duplicate
				List<String> filenameList = new ArrayList<String>();
				
				//Allow only the first 500
				int numbreOfContentObjects = 500;
				
				long now = System.currentTimeMillis();
				
				for (ContentObject contentObject : results){
					
					if (numbreOfContentObjects == 0){
						break;
					}

					Calendar created = ((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue();
					
					String folderPath = DateUtils.format(created, "yyyy/MM/dd");
					
					long nowXml = System.currentTimeMillis();
					String xml = contentObject.xml(true);
					
					//Build filename
					String finalName = buildFilename(folderPath, created, contentObject, filenameList);
					
					filenameList.add(finalName);
					
					long nowZip = System.currentTimeMillis();
					zipOutputStream.putNextEntry(new ZipEntry(finalName));

					IOUtils.write(xml, zipOutputStream);

					zipOutputStream.closeEntry();
				
					
					numbreOfContentObjects--;
				}
				
				zipOutputStream.close();
				fos.close();
				
				long nowFileZip = System.currentTimeMillis();
				zipFileInputStream = new FileInputStream(tempZip);
				IOUtils.copy(zipFileInputStream, response.getOutputStream());
				zipFileInputStream.close();
				
				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing xmlto servlet output stream", e);
				JSFUtilities.addMessage(null, "object.action.export.message.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				if (fos !=null){
					IOUtils.closeQuietly(fos);
				}
				if (zipOutputStream!= null){
					IOUtils.closeQuietly(zipOutputStream);
				}
				
				if (zipFileInputStream!= null){
					IOUtils.closeQuietly(zipFileInputStream);
				}
				
				if (tempZip!=null){
					FileUtils.deleteQuietly(tempZip);
				}
			}


		}
		else {
			JSFUtilities.addMessage(null, "object.action.export.message.error", null, FacesMessage.SEVERITY_WARN);
		}

	}

	private String buildFilename(String folderPath, Calendar created, ContentObject contentObject, List<String> filenameList) {
		//StringBuilder contentObjectFilename = new StringBuilder(folderPath);
		StringBuilder contentObjectFilename = new StringBuilder("");
		//contentObjectFilename.append("/");
		contentObjectFilename.append(DateUtils.format(created, "dd-MM-yyyy'T'HHmm"));
		contentObjectFilename.append("_");
		
		if (contentObject.getComplexCmsRootProperty().isChildPropertyDefined("profile.title")){
			String title = ((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue();
			
			if (StringUtils.isNotBlank(title)){
				title = FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(title);
				
				if (title.length() > 150){
					contentObjectFilename.append(title.substring(0, 149));
				}
				else{
					contentObjectFilename.append(title);
				}
			}
		}
		else if (contentObject.getSystemName() != null){
			//Use system name instead 
			contentObjectFilename.append(FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(contentObject.getSystemName()));
		}
		else{
			contentObjectFilename.append(contentObject.getId());
		}
		
		//Add creation date to the filename
		
		contentObjectFilename.append(".xml");
		
		String finalName = contentObjectFilename.toString();
		
		int count = 0;
		while (filenameList.contains(finalName)){
			finalName = StringUtils.replace(finalName, ".xml", "-"+ ++count + ".xml");
		}
		
		return finalName;
	}

	public void setSelectedTaxonomyId(String selectedTaxonomyId) {
		this.selectedTaxonomyId = selectedTaxonomyId;
	}
}
