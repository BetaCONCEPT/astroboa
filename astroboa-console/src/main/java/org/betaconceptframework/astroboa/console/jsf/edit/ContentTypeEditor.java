/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.jsf.DynamicUIAreaPageComponent;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.definition.ContentObjectTypeDefinitionImpl;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.GreekToEnglish;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("contentTypeEditor")
@Scope(ScopeType.CONVERSATION)
public class ContentTypeEditor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private DefinitionService definitionService;
	private PageController pageController;
	
	private Map<String, XSDSchemaContainer> xsdSchemasPerContentType = new HashMap<String, XSDSchemaContainer>();
	
	private String newContentTypeFilename;
	private String newContentTypeSchema;
	
	private String selectedContentTypeForEdit;
	
	public String loadContentTypesForEdit_UIAction(){
		clearXsdSchemaCache();
		newContentTypeFilename = null;
		newContentTypeSchema = null;
		selectedContentTypeForEdit = null;
		
		return pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.OBJECT_TYPE_SELECTOR.getDynamicUIAreaPageComponent());
	}
	
	public boolean shouldEditContentTypeXSD(String contentType){
		
		try{
		if (contentType == null ||  ( ! contentType.endsWith(".xsd") && ! definitionService.hasContentObjectTypeDefinition(contentType))){
			return false;
		}
		
		Object contentDefinition = retrieveFileForDefinition(contentType);
		boolean contentTypeIsBuiltIn =  contentDefinition != null && contentDefinition instanceof byte[];
		
		if (contentTypeIsBuiltIn){
			return false;
		}
		
		//Finally only user with ROLE_ADMIN can edit XSD files
		return Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));
		}
		catch(Exception e){
			logger.error("	",e);
			return false;
		}
		
	}
	
	public void loadXSDSchemaForContentType(String contentType){
		
		if (StringUtils.isBlank(contentType)){
			return;
		}
		
		if (! xsdSchemasPerContentType.containsKey(contentType)){
			
			try{
				Object contentTypeFile = retrieveFileForDefinition(contentType);

				if (contentTypeFile == null){
					return;
				}

				XSDSchemaContainer schemaContainer = new XSDSchemaContainer();
				
				if (contentTypeFile instanceof File){
					schemaContainer.setSchema(FileUtils.readFileToString((File)contentTypeFile,"UTF-8"));
					schemaContainer.setSchemaFile((File)contentTypeFile);
				}
				else if (contentTypeFile instanceof byte[]){
					String schema = new String((byte[])contentTypeFile,"UTF-8");
					schemaContainer.setSchema(schema);
				}
				
				xsdSchemasPerContentType.put(contentType, schemaContainer);
				
			}
			catch(Exception e){
				logger.error("",e);
				return;
			}
		}
		
		selectedContentTypeForEdit = contentType;
		
	}
	
	public String saveContentType_UIAction(String contentType){
		
		if (contentType != null){ 
				
			if (xsdSchemasPerContentType.containsKey(contentType)){

				XSDSchemaContainer schemaContainer = xsdSchemasPerContentType.get(contentType);

				if (schemaContainer.getSchemaFile() == null){
					return null;
				}
				
				if (! validateDefinition(schemaContainer.getSchemaFile().getName(), schemaContainer.getSchema(), contentType)){
					return null;
				}
				
				if (schemaContainer.save()){
					
					return finalizeSuccessfullSave(contentType, true);
				}
			}
			else if ("NEW_CONTENT_TYPE".equals(contentType)){
				
				if (StringUtils.isNotBlank(newContentTypeFilename) && StringUtils.isNotBlank(newContentTypeSchema)){
					try{
						
						if (! validateDefinition(newContentTypeFilename, newContentTypeSchema, newContentTypeSchema)){
							return null;
						}

						File definitionHomeDir = new File(getDefinitionSchemaHomeDirectory());
						
						if (definitionHomeDir.exists()){
							
							if (! newContentTypeFilename.endsWith(".xsd")){
								newContentTypeFilename += ".xsd";
							}
							
							newContentTypeFilename = GreekToEnglish.convertString(newContentTypeFilename);
							
							String[] existingXSDs = definitionHomeDir.list();
							
							if (existingXSDs != null && existingXSDs.length > 0){
								for(String filename : existingXSDs){
									if (StringUtils.equals(filename, newContentTypeFilename)){
										JSFUtilities.addMessage(null, "object.type.select.content.type.exists", new String[]{newContentTypeFilename}, FacesMessage.SEVERITY_INFO);
										return null;	
									}
								}
							}
							
							File newContentTypeFile = new File(definitionHomeDir, newContentTypeFilename);
							
							FileUtils.writeStringToFile(newContentTypeFile, newContentTypeSchema, "UTF-8");
							
							String contentTypeToBeUsedForMessage = new String(newContentTypeFilename);
							
							newContentTypeFilename = null;
							newContentTypeSchema = null;
							
							return finalizeSuccessfullSave(contentTypeToBeUsedForMessage, false);
							
						}
					}
					catch(Exception e){
						logger.error("",e);
					}
				}
				
				else{
					JSFUtilities.addMessage(null, "object.type.select.new.content.type.missing", null, FacesMessage.SEVERITY_WARN);
				}
				
			}
		}
		
		return null;
	}

	private String finalizeSuccessfullSave(String contentType, boolean searchDefinition) {
		ContentObjectTypeDefinition typeDefinition = null;
		
		if (searchDefinition){
			typeDefinition = definitionService.getContentObjectTypeDefinition(contentType);
		}
		
		String label = null;
		if (typeDefinition != null && typeDefinition.getDisplayName() !=null){ 
			label = typeDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
		}

		if (label == null){
			label = contentType;
		}

		JSFUtilities.addMessage(null, "object.type.select.table.save.content.type.success", new String[]{label}, FacesMessage.SEVERITY_INFO);
		
		// if a type has been added or edited then remove "contentObjectTypesAsSelectItems" from page scope so that all menus that
		// show the available contet types will be reloaded
		Contexts.getConversationContext().remove("contentObjectTypesAsSelectItems");
		Contexts.getConversationContext().flush();
		
		return null;
	}


	private boolean validateDefinition(String definitionFilename, String definition, String contentType) {
		
		try{
			if (! definitionService.validateDefinintion(definition, definitionFilename)){
				JSFUtilities.addMessage(null, "content.type.validate.failure", new String[]{contentType, ""}, FacesMessage.SEVERITY_WARN);
				return false;
			}
			
			return true;
		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "content.type.validate.failure", new String[]{contentType, e.getMessage()}, FacesMessage.SEVERITY_WARN);
			return false;
		}
	}

	private Object retrieveFileForDefinition(String contentType) {
		try{
			//Domain specific content type home directory
			String contentDefinitionSchemaPath = getDefinitionSchemaHomeDirectory();

			if( contentType.endsWith(".xsd")){
				//Special case content type represents file name
				File contentTypeFile = new File(contentDefinitionSchemaPath, contentType);

				if (contentTypeFile.exists()){
					return contentTypeFile;
				}
				return null;
			}
			else{
				
				ContentObjectTypeDefinition typeDefinition = definitionService.getContentObjectTypeDefinition(contentType);

				URI contentTypeFileURI = ((ContentObjectTypeDefinitionImpl)typeDefinition).getDefinitionFileURI();

				if (contentTypeFileURI.getScheme() == null || ! contentTypeFileURI.getScheme().startsWith("file")){
					
					//Probably a built in content type. Load XSD in byte array
					return definitionService.getXMLSchemaForDefinition(contentType);
					
					/*if (contentTypeFileURI.getScheme().startsWith("jar")){
						return definitionService.getXMLSchemaForDefinition(contentType);
					}
					return null;
					*/
					
				}

				File contentTypeFile = new File(contentTypeFileURI);

				if (! contentTypeFile.exists() || contentTypeFile.getParentFile() == null || ! contentTypeFile.getParentFile().exists() ||
						contentTypeFile.getParentFile().getAbsolutePath() == null){
					
					//File not found. Return XSD byte array
					return definitionService.getXMLSchemaForDefinition(contentType);
				}


				if (! StringUtils.equals(contentTypeFile.getParentFile().getAbsolutePath(), contentDefinitionSchemaPath)){
					//This way it is ensured that the file is located under the expected directory
					logger.error("File is there but its parent absolute path {} is not the same with definition parent directory {}", 
							contentTypeFile.getParentFile().getAbsolutePath(), contentDefinitionSchemaPath);
					return null;
				}

				return contentTypeFile;
			}
			
		}
		catch(Exception e){
			logger.error("",e);
			return null;
		}
	}

	private String getDefinitionSchemaHomeDirectory() {
		String contentDefinitionSchemaPath = AstroboaClientContextHolder.getActiveCmsRepository().getRepositoryHomeDirectory()+File.separator+
		"astroboa_schemata";
		return contentDefinitionSchemaPath;
	}
	
	
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public String getNewContentTypeFilename() {
		return newContentTypeFilename;
	}

	public void setNewContentTypeFilename(String newContentTypeFilename) {
		this.newContentTypeFilename = newContentTypeFilename;
	}

	public String getNewContentTypeSchema() {
		return newContentTypeSchema;
	}

	public void setNewContentTypeSchema(String newContentTypeSchema) {
		this.newContentTypeSchema = newContentTypeSchema;
	}

	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

	public void clearXsdSchemaCache() {
		xsdSchemasPerContentType.clear();
		
	}

	public String getSelectedContentTypeForEdit() {
		return selectedContentTypeForEdit;
	}

	public void setSelectedContentTypeForEdit(String selectedContentTypeForEdit) {
		this.selectedContentTypeForEdit = selectedContentTypeForEdit;
	}

	public Map<String, XSDSchemaContainer> getXsdSchemasPerContentType() {
		return xsdSchemasPerContentType;
	}

}
