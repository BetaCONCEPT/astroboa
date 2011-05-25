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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.CropPolicy;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.console.jsf.clipboard.Clipboard;
import org.betaconceptframework.astroboa.console.jsf.clipboard.ContentObjectPropertyUrlItem;
import org.betaconceptframework.astroboa.console.jsf.visitor.CmsPropertyValidatorVisitor;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.contexts.Contexts;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SimpleCmsPropertyWrapper  extends CmsPropertyWrapper<SimpleCmsProperty>{

	private List<SimpleCmsPropertyValueWrapper> simpleCmsPropertyValueWrappers = new ArrayList<SimpleCmsPropertyValueWrapper>();
	private int indexOfValueToBeDeleted = -1;
	private int indexOfValueToBeCopied;
	private CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor;

	private String selectedDispositionType;
	private Integer selectedImageWidth;
	private Integer selectedImageHeight;
	private Double selectedAspectRatio;
	private String selectedCropPolicy;
	private boolean friendlyUrlSelected;
	
	public SimpleCmsPropertyWrapper(SimpleCmsProperty simpleCmsProperty, 
			CmsPropertyDefinition definition, String parentCmsPropertyPath,CmsRepositoryEntityFactory cmsRepositoryEntityFactory, 
			CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor,
			ContentObject contentObject) {
		super(definition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject);
		
		this.cmsProperty = simpleCmsProperty;
		this.cmsPropertyValidatorVisitor = cmsPropertyValidatorVisitor;
		
	}


	/**
	 * Adds a blank value to selected simple cms property
	 */
	public void addBlankValue_UIAction(){

		//Check that content object has property and that this property is a simple one.
		if (cmsProperty == null || ! (cmsProperty instanceof SimpleCmsProperty)){
			logger.error("Invalid selected simple property.Unable to add blank value."+ 
					(cmsProperty != null? cmsProperty.getFullPath() : ""));
			JSFUtilities.addMessage(null, "application.unknown.error.message", null,
					 FacesMessage.SEVERITY_WARN);
			return;
		}

		//Add a value wrapper
		addNewSimpleCmsPropertyValueWrapper();

	}


	private void addNewSimpleCmsPropertyValueWrapper() {

		SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper = 
			new SimpleCmsPropertyValueWrapper(cmsProperty,simpleCmsPropertyValueWrappers.size(),
				cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor);
		
		simpleCmsPropertyValueWrapper.addDefaultValue();
		
		
		simpleCmsPropertyValueWrappers.add(simpleCmsPropertyValueWrapper);
	}


	public void setIndexOfValueToBeDeleted(int indexOfValueToBeDeleted) {
		this.indexOfValueToBeDeleted = indexOfValueToBeDeleted;
	}


	/**
	 * This method should be called only if simpleCmsProperty is Single value
	 * Otherwise an exception is thrown
	 */
	public void deleteSingleValue_UIAction(){
		if (cmsProperty != null){
			try{

				cmsProperty.setSimpleTypeValue(null);

				//Reset first wrapper
				simpleCmsPropertyValueWrappers.clear();

			}
			catch (MultipleOccurenceException m){
				logger.error("Try to remove a single value from a multiple simple cms property "+ cmsProperty.getFullPath());
				//Do nothing
			}
			catch (Exception e){
				logger.error("",e);
			}
		}
	}

	public SimpleCmsPropertyValueWrapper getSingleSimpleCmsPropertyValueWrapper(){

		if (cmsProperty !=null && !cmsProperty.getPropertyDefinition().isMultiple()){
			if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
				simpleCmsPropertyValueWrappers.add(new SimpleCmsPropertyValueWrapper(cmsProperty,0, cmsRepositoryEntityFactory, 
						cmsPropertyValidatorVisitor));
			}

			return simpleCmsPropertyValueWrappers.get(0);

		}

		logger.warn("Try to get Single value Wrapper from simple cms property which "+ 
				(cmsProperty == null ? " is null ": " is multiple and has path "+ cmsProperty.getFullPath()));

		return null;
	}

	
	
	public void createURLForPropertyAndCopyItToClipboard_UIAction() {
		
		try {
			SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper = simpleCmsPropertyValueWrappers.get(indexOfValueToBeCopied);

			if (simpleCmsPropertyValueWrapper != null) { 
				
				if (ValueType.Binary.equals(getValueType()) && 
						simpleCmsPropertyValueWrapper.getBinaryChannelValue() !=null) {

					BinaryChannel binaryChannel = simpleCmsPropertyValueWrapper.getBinaryChannelValue();

					ContentObjectPropertyUrlItem propertyUrlItem = new ContentObjectPropertyUrlItem();
					propertyUrlItem.setId(getContentObject().getId());
					propertyUrlItem.setSystemName(getContentObject().getSystemName());
					propertyUrlItem.setTitle(((StringProperty)getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue());
					propertyUrlItem.setType(getContentObject().getContentObjectType());
					propertyUrlItem.setTypeLabel(getContentObject().getTypeDefinition().getDisplayName().getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()));
					propertyUrlItem.setPropertyName(cmsProperty.getName());
					propertyUrlItem.setPropertyLabel(getLocalizedLabelForCurrentLocale());
					propertyUrlItem.setPropertyPath(getPath());
					propertyUrlItem.setPropertyValueType(getValueType().toString() + " (" + binaryChannel.getSourceFilename()+")");
					
					String objectPropertyUrl;
					
					// if the user has selected INLINE we choose not to explicitly specify it in the URL since INLINE is the default and 
					// can be ommited from the URL. We choose to do so to produce simpler URLs
					ContentDispositionType contentDispositionType = null;
					if (selectedDispositionType != null) {
						if (selectedDispositionType.equals("INLINE")) {
							contentDispositionType = null;
						}
						else {
							contentDispositionType = ContentDispositionType.valueOf(selectedDispositionType);
						}
					}
					
					// if the user has selected "top" as the crop policy we choose not to explicitly specify it in the URL since "top" is the default crop policy 
					// can be ommited from the URL. We choose to do so to produce simpler URLs
					CropPolicy cropPolicy = null;
					if (StringUtils.isNotBlank(selectedCropPolicy)) {
						if(selectedCropPolicy.equals("top")) {
							cropPolicy = null;
						}
						else {
							try{
								cropPolicy = CropPolicy.valueOf(selectedCropPolicy);
							}
							catch(Exception e){
								//Just log the exception 
								logger.warn("Invalid selected crop policy value '"+selectedCropPolicy+"'. No crop policy will be defined", e);
							}
						}
					}
					
					objectPropertyUrl = 
							binaryChannel.buildResourceApiURL(selectedImageWidth, selectedImageHeight, selectedAspectRatio, cropPolicy, contentDispositionType, friendlyUrlSelected, true);
					
					propertyUrlItem.setURL(objectPropertyUrl);
					
					// expose the url to the UI
					Contexts.getEventContext().set("objectPropertyUrl", objectPropertyUrl);
					
					//reset form values
					selectedDispositionType = null;
					selectedImageWidth = null;
					selectedImageHeight = null;
					selectedAspectRatio = null;
					selectedCropPolicy = null;
					friendlyUrlSelected = false;
					
					//Normally this should be available via injection
					Clipboard clipboard  = (Clipboard) JSFUtilities.getBeanFromFacesContext("clipboard");

					clipboard.addContentObjectPropertyUrlItem(propertyUrlItem);

					JSFUtilities.addMessage(null, "clipboard.property.url.copy.to.clipboard.successful", null, FacesMessage.SEVERITY_INFO);
				}
			}
		}
		catch (Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "clipboard.property.url.copy.to.clipboard.failed", null, FacesMessage.SEVERITY_WARN);
		}

			
	}
	
	/**
	 * This method should be called only if simpleCmsProperty is multiple
	 * Otherwise an exception is thrown
	 */
	public void deleteValueFromSelectedSimpleCmsProperty_UIAction(){
		//Remove value only it has not already been deleted in case of null value
		if (indexOfValueToBeDeleted != -1){

			//Remove value from simple cms property
			//only if indexOfvalueTobeDeleted exists for values
			try{
				if (simpleCmsPropertyValueWrappers.get(indexOfValueToBeDeleted) != null && 
						! simpleCmsPropertyValueWrappers.get(indexOfValueToBeDeleted).isValueSetNull() &&
						cmsProperty.getSimpleTypeValues().get(indexOfValueToBeDeleted) != null){
					cmsProperty.removeSimpleTypeValue(indexOfValueToBeDeleted);
				}
			}
			catch(Exception e){
				//Ignore exception
			}
				
			removeWrapperAndShoftIndexes();

			indexOfValueToBeDeleted = -1;
		}
	}


	private void removeWrapperAndShoftIndexes() {
		try{
			simpleCmsPropertyValueWrappers.remove(indexOfValueToBeDeleted);
			
			for (SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper : simpleCmsPropertyValueWrappers){
				simpleCmsPropertyValueWrapper.changeIndex(simpleCmsPropertyValueWrappers.indexOf(simpleCmsPropertyValueWrapper));
			}
			
		}
		catch(Exception e){
			logger.error("",e);
			simpleCmsPropertyValueWrappers.clear();
		}
	}

	public List<SimpleCmsPropertyValueWrapper> getSimpleCmsPropertyValueWrappers(){
		//Create the wrappers if they do not yet exist
		// if the property has already value create as many wrappers as the values
		// if the property has no values yet then create one value wrapper to allow the creation of the UI input component that 
		// will allow the user to add values
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers) && cmsProperty != null){
			List values = cmsProperty.getSimpleTypeValues();
			if (CollectionUtils.isNotEmpty(values)) {
				for (int i=0; i<values.size(); i++){
					simpleCmsPropertyValueWrappers.add(new SimpleCmsPropertyValueWrapper(cmsProperty, i, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor));
				}
			}
			else {
				addNewSimpleCmsPropertyValueWrapper();
			}
			
		}

		return simpleCmsPropertyValueWrappers;
	}

	public void setIndexOfValueToBeCopied(int indexOfValueToBeCopied) {
		this.indexOfValueToBeCopied = indexOfValueToBeCopied;
	}
	
	public int getIndexOfValueToBeCopied() {
		return indexOfValueToBeCopied;
	}


	public List<SelectItem> getValueEnumeration(){
		List<SelectItem> valueEnumerationSelectItems = new ArrayList<SelectItem>();
	    
		Map valueEnumeration = ((SimpleCmsPropertyDefinition)cmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueEnumeration != null){
			
			valueEnumerationSelectItems.add(new SelectItem("", JSFUtilities.getLocalizedMessage("content.object.edit.select.box.no.value", null)));
			
			for (Object value : valueEnumeration.keySet()){
				valueEnumerationSelectItems.add(new SelectItem(value,((Localization)valueEnumeration.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))); 
			}
		}
		
	    return valueEnumerationSelectItems;
	}

	public void addWrapperReferenceToClipboard(Integer indexOfValueToBeCopied) {
		// get clipboard from session context
		Clipboard clipboard =  (Clipboard) Contexts.getSessionContext().get("clipboard");
		if (clipboard != null) {
			setIndexOfValueToBeCopied(indexOfValueToBeCopied);
			clipboard.setSelectedCmsPropertyWrapper(this);
		}
		else {
			logger.warn("Could not retrieve clipboard from session context.");
		}
	}

	public String getSelectedDispositionType() {
		return selectedDispositionType;
	}


	public void setSelectedDispositionType(String selectedDispositionType) {
		this.selectedDispositionType = selectedDispositionType;
	}


	public Integer getSelectedImageWidth() {
		return selectedImageWidth;
	}


	public void setSelectedImageWidth(Integer selectedImageWidth) {
		this.selectedImageWidth = selectedImageWidth;
	}


	public Integer getSelectedImageHeight() {
		return selectedImageHeight;
	}


	public void setSelectedImageHeight(Integer selectedImageHeight) {
		this.selectedImageHeight = selectedImageHeight;
	}


	public Double getSelectedAspectRatio() {
		return selectedAspectRatio;
	}


	public void setSelectedAspectRatio(Double selectedAspectRatio) {
		this.selectedAspectRatio = selectedAspectRatio;
	}


	public String getSelectedCropPolicy() {
		return selectedCropPolicy;
	}


	public void setSelectedCropPolicy(String selectedCropPolicy) {
		this.selectedCropPolicy = selectedCropPolicy;
	}


	public boolean isFriendlyUrlSelected() {
		return friendlyUrlSelected;
	}


	public void setFriendlyUrlSelected(boolean friendlyUrlSelected) {
		this.friendlyUrlSelected = friendlyUrlSelected;
	}
}
