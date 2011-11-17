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
package org.betaconceptframework.astroboa.console.jsf.edit;


import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class CmsPropertyWrapper<T extends CmsProperty> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected T cmsProperty;
	
	private Boolean readGranted;
	private Boolean writeGranted;
	
	//The following values could be retrieved directly from cmsPropertyDefinition
	//but since JSF is calling many times their getter methods
	//'caching' the value, prevents from calling many times corresponding methods from cmsPropertyDefinition
	//i.e. cmsPropertyDefinition.isMandatory
	private Boolean mandatory;
	private Boolean multiple;
	private String localizedLabelForCurrentLocale;
	private String definitionName;
	private ValueType valueType;
	private String path;
	protected int wrapperIndex;
	protected int indexOfPropertyValueToBeProcessed = -1;
	
	private CmsPropertyDefinition cmsPropertyDefinition;
	private String parentCmsPropertyPath;

	protected CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	private ContentObject contentObject;
	
	protected ComplexCmsPropertyEdit complexCmsPropertyEdit; 
	
	public CmsPropertyWrapper(CmsPropertyDefinition cmsPropertyDefinition,
			String parentCmsPropertyPath, 
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory, 
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		
		this.cmsPropertyDefinition = cmsPropertyDefinition;
		this.parentCmsPropertyPath = parentCmsPropertyPath;
		
		readGranted = Boolean.TRUE;
		writeGranted = Boolean.TRUE;
		
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
		this.contentObject = contentObject;
		this.wrapperIndex = wrapperIndex;
		
		this.complexCmsPropertyEdit = complexCmsPropertyEdit;
		
	}
	
	public abstract void addBlankValue_UIAction();

	//Practically path relative to parent property is the
	// property name plus its index, in case property is 
	//multiple and complex
	public String getPathRelativeToCmsPropertyParent(){
		
		getPath();
		
		String relativePath = PropertyPath.getLastDescendant(path);
		
		//Special case. If property is COMPLEX and MULTIPLE
		//and path does not contains index
		//0 is added to relative path
		if (ValueType.Complex == getValueType() &&
				BooleanUtils.isTrue(isMultiple()) && 
				PropertyPath.extractIndexFromPath(relativePath) == PropertyPath.NO_INDEX
				)
		{
			relativePath += CmsConstants.LEFT_BRACKET+"0"+CmsConstants.RIGHT_BRACKET;
		}
		
		return relativePath;
	}
	public String getPath(){
		if (path == null){
			if (cmsProperty != null){
				path = cmsProperty.getPath();
			}
			else{
				//Full path is its parent full path plus its definition Name
				path = PropertyPath.createFullPropertyPath(parentCmsPropertyPath, getDefinitionName());
			}
		}
		
		return path;
	}
	public ValueType getValueType(){
		if (valueType == null){
			
			if (cmsProperty != null){
				valueType = cmsProperty.getValueType();
			}
			else if (cmsPropertyDefinition != null){
				valueType = cmsPropertyDefinition.getValueType();
			}
		}
		
		return valueType;
	}
	
	public boolean isMultiple(){
		if (multiple == null){
			
			if (cmsProperty != null){
				if (cmsProperty.getPropertyDefinition() != null)
					multiple = cmsProperty.getPropertyDefinition().isMultiple();
			}
			
			if (multiple == null){
				if (cmsPropertyDefinition != null){
					multiple = cmsPropertyDefinition.isMultiple();
				}
				else{
					multiple = false;
				}
			}
		}
		
		return multiple;
	}
	
	public boolean isMandatory(){
		if (mandatory == null){
			if (cmsProperty != null){
				if (cmsProperty.getPropertyDefinition() != null)
					mandatory = cmsProperty.getPropertyDefinition().isMandatory();
			}
			
			if (mandatory == null){
				if (cmsPropertyDefinition != null){
					mandatory = cmsPropertyDefinition.isMandatory();
				}
				else{
					mandatory = false;
				}
			}
		}
		
		return mandatory;
	}
	
	public String getDefinitionName(){
		if (definitionName == null){
			if (cmsProperty != null){
				if (cmsProperty.getPropertyDefinition() != null)
					definitionName = cmsProperty.getPropertyDefinition().getName();
			}
			
			if (definitionName == null){
				if (cmsPropertyDefinition != null){
					definitionName = cmsPropertyDefinition.getName();
				}
			}
		}
		
		return definitionName;
	}
	
	public String getLocalizedLabelForCurrentLocale(){
		if (localizedLabelForCurrentLocale == null)
			
			if (cmsProperty == null){
				if (cmsPropertyDefinition == null){
					localizedLabelForCurrentLocale = null;
				}
				else{
					localizedLabelForCurrentLocale  = cmsPropertyDefinition.getDisplayName().getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
				}
			}
			else{
				localizedLabelForCurrentLocale = cmsProperty.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
				
				if (ValueType.Complex == cmsProperty.getValueType() && cmsPropertyDefinition != null && cmsPropertyDefinition.isMultiple()){
					String labelForSpecifiedProperty = ((ComplexCmsProperty<?,?>)cmsProperty).getPropertyLabel(JSFUtilities.getLocaleAsString());
					
					if (StringUtils.isNotBlank(labelForSpecifiedProperty)){
						localizedLabelForCurrentLocale = labelForSpecifiedProperty; 
					}
				}
			}
		
		return localizedLabelForCurrentLocale;
	}
	
	
	
	public Boolean getReadGranted() {
		return readGranted;
	}

	protected String getRestrictReadToRoles() {
		if (cmsProperty != null && cmsProperty.getPropertyDefinition() != null)
			return cmsProperty.getPropertyDefinition().getRestrictReadToRoles();
		
		return null;
	}
	protected String getRestrictToWriteRoles() {
		if (cmsProperty != null && cmsProperty.getPropertyDefinition() != null)
			return  cmsProperty.getPropertyDefinition().getRestrictWriteToRoles();
		
		return null;
	}

	public Boolean getWriteGranted() {
		return writeGranted;
	}

	


	public T getCmsProperty() {
		return cmsProperty;
	}


	public CmsPropertyDefinition getCmsPropertyDefinition(){
		return cmsPropertyDefinition;
	}

	public ContentObject getContentObject() {
		return contentObject;
	}

	public void setContentObject(ContentObject contentObject) {
		this.contentObject = contentObject;
	}

	public int getWrapperIndex() {
		return wrapperIndex;
	}

	public ComplexCmsPropertyEdit getComplexCmsPropertyEdit() {
		return complexCmsPropertyEdit;
	}

	public int getIndexOfPropertyValueToBeProcessed() {
		return indexOfPropertyValueToBeProcessed;
	}

	public void setIndexOfPropertyValueToBeProcessed(
			int indexOfPropertyValueToBeProcessed) {
		this.indexOfPropertyValueToBeProcessed = indexOfPropertyValueToBeProcessed;
	}
}
