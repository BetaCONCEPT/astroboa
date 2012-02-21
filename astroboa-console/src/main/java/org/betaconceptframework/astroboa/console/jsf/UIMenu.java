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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.jsf.edit.ContentTypeEditor;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.comparator.SelectItemComparator;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

@Name("uiMenu")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UIMenu extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;
	
	private DefinitionService definitionService;
	private CMSUtilities cmsUtilities;
	//private ContentObjectSearchByCriteria contentObjectSearchByCriteria;
	
	@In(create=true)
	private ContentTypeEditor contentTypeEditor; 
	
	@Factory(value="contentObjectTypesAsSelectItems")
	public List<SelectItem> getContentObjectTypesAsSelectItems() {
		try {
			
			String locale = JSFUtilities.getLocaleAsString();
			List<SelectItem> contentObjectTypesAsSelectItems = new ArrayList<SelectItem>();

			List<String> contentObjectTypeNames = null;
			
			try{
				contentObjectTypeNames = definitionService.getContentObjectTypes();

				if (contentObjectTypeNames == null || contentObjectTypeNames.isEmpty()){
					throw new Exception();
				}
				
				for (String contentObjectTypeName : contentObjectTypeNames) {
					ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(contentObjectTypeName);
					if (shouldCreateSelectItem(contentObjectTypeDefinition)){
						String contentObjectTypeLocalisedLabel = cmsUtilities.getLocalizedNameOfContentObjectType(contentObjectTypeDefinition, locale);
						SelectItem selectItem = new SelectItem(contentObjectTypeDefinition.getName(), 
								contentObjectTypeLocalisedLabel, contentObjectTypeDefinition.getDescription().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()));
						contentObjectTypesAsSelectItems.add(selectItem);
					}
				}	
					
				Collections.sort(contentObjectTypesAsSelectItems, new SelectItemComparator());
			}
			catch(Exception e){
				e.printStackTrace();
				
				contentTypeEditor.clearXsdSchemaCache();
				
				//Fill select items with all available XSDs located in XSD directory
				//so that user would have the opportunity to correct invalid XSD
				File definitionHomeDir = new File(getDefinitionSchemaHomeDirectory());
				
				if (definitionHomeDir.exists()){
					String[] xsdFiles = definitionHomeDir.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return (StringUtils.isNotBlank(name) && name.endsWith(".xsd"));
						}
					});
					
					if (xsdFiles != null){
						for (String xsdFile : xsdFiles){
							SelectItem selectItem = new SelectItem(xsdFile, xsdFile, xsdFile);
							selectItem.setDisabled(true);
							contentObjectTypesAsSelectItems.add(selectItem);
						}
					}
				}
				
			}
			
			
			
			return contentObjectTypesAsSelectItems;
		
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "Δεν μπόρεσαν να ανακτηθούν οι Τύποι Περιεχομένου. Το σφάλμα είναι: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
			e.printStackTrace();
			return null;
		}
	}


	private String getDefinitionSchemaHomeDirectory() {
		String contentDefinitionSchemaPath = AstroboaClientContextHolder.getActiveCmsRepository().getRepositoryHomeDirectory()+File.separator+
		"astroboa_schemata";
		return contentDefinitionSchemaPath;
	}

	private boolean shouldCreateSelectItem(
			ContentObjectTypeDefinition contentObjectTypeDefinition) {
		
		if (contentObjectTypeDefinition == null)
		{
			return false;
		}
		
		/*
		 * When the following occur then type should not be available
		 * 
		 * 1. Content type is roleObject with 
		 * 2. Logged in user does not have role CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR
		 * 3. IdentityStore repository is the same with the repository
		 */
		if (contentObjectTypeDefinition.getQualifiedName() != null)
		{
			if (contentObjectTypeDefinition.getQualifiedName().equals(new QName("http://www.betaconceptframework.org/schema/astroboa/identity/role","roleObject")))
			{
				if (! Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR)))
				{
					CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();
					
					if (activeRepository != null &&
							 ( activeRepository.getIdentityStoreRepositoryId() == null ||
									 StringUtils.equals(activeRepository.getIdentityStoreRepositoryId(), activeRepository.getId())
							 )
					 )
					{
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	
	
	/*
	 * NOt used at all
	 * public List<SelectItem> getContentObjectTypePropertiesAsSelectItems() {
		String selectedContentObjectType = contentObjectSearchByCriteria.getSearchResultsFilterAndOrdering().getSelectedContentObjectType();
		SelectItemContentObjectPropertyDefinitionVisitor selectItemVisitor = new SelectItemContentObjectPropertyDefinitionVisitor(selectedContentObjectType);
		
		
		try {
			ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(selectedContentObjectType);
			contentObjectTypeDefinition.accept(selectItemVisitor);
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "Δεν μπόρεσαν να ανακτηθούν οι ιδιότητες του Τύπου Περιεχομένου. Το σφάλμα είναι: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
			e.printStackTrace();
			return null;
		}
		
		return selectItemVisitor.getSelectItems();
	}
	  */
	
	/* Used inside Criterion Wrapper
	public List<SelectItem> getQueryOperatorsAsSelectItems(boolean createOperatorsForBinaryProperty) {
		List<SelectItem> queryOperatorsAsSelectItems = new ArrayList<SelectItem>();
		QueryOperator[] queryOperators = QueryOperator.values();
		for (QueryOperator queryOperator : queryOperators) {
			if (queryOperator != QueryOperator.LIKE){
			
				String operatorLabel = "";
				switch (queryOperator) {
				case IS_NOT_NULL:
					operatorLabel = JSFUtilities.getLocalizedMessage("query.operator.not.null", null);
					break;
				case IS_NULL:
					operatorLabel = JSFUtilities.getLocalizedMessage("query.operator.null", null);
					break;
				case CONTAINS:
					operatorLabel = JSFUtilities.getLocalizedMessage("query.operator.contains", null);
					break;
				default:
					operatorLabel = queryOperator.getOp();
				}
				//Do not show all query operators if it is for binary property
				if (BooleanUtils.isFalse(createOperatorsForBinaryProperty) || 
						(BooleanUtils.isTrue(createOperatorsForBinaryProperty) && 
								(queryOperator == QueryOperator.CONTAINS || queryOperator == QueryOperator.IS_NOT_NULL || queryOperator == QueryOperator.IS_NULL))){
					SelectItem selectItem = new SelectItem(queryOperator, operatorLabel);
					queryOperatorsAsSelectItems.add(selectItem);
				}
			}
		}
		return queryOperatorsAsSelectItems;
	}*/

	
	

	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}

	
	/*public void setContentObjectSearchByCriteria(
			ContentObjectSearchByCriteria contentObjectSearchByCriteria) {
		this.contentObjectSearchByCriteria = contentObjectSearchByCriteria;
	}
	 */
	



}
