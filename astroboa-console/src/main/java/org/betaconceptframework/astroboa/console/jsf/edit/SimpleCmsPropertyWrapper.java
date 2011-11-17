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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.betaconceptframework.utility.ImageUtils;
import org.jboss.seam.contexts.Contexts;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SimpleCmsPropertyWrapper  extends CmsPropertyWrapper<SimpleCmsProperty>{

	private List<SimpleCmsPropertyValueWrapper> simpleCmsPropertyValueWrappers = new ArrayList<SimpleCmsPropertyValueWrapper>();
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
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		super(definition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject, wrapperIndex, complexCmsPropertyEdit);
		
		this.cmsProperty = simpleCmsProperty;
		this.cmsPropertyValidatorVisitor = cmsPropertyValidatorVisitor;
		
	}


	/**
	 * Adds a blank value to selected simple cms property
	 */
	public void addBlankValue_UIAction(){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
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
		
		if (indexOfPropertyValueToBeProcessed == -1) {
			logger.error("The index of property value has not been set. Property value url cannot be created.");
			return;
		}
		
		try {
			SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper = simpleCmsPropertyValueWrappers.get(indexOfPropertyValueToBeProcessed);

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
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		//Remove value only if it has not already been deleted in case of null value
		if (indexOfPropertyValueToBeProcessed != -1){

			//Remove value from simple cms property
			//only if indexOfPropertyValueToBeProcessed exists for values
			try{
				if (simpleCmsPropertyValueWrappers.get(indexOfPropertyValueToBeProcessed) != null && 
						! simpleCmsPropertyValueWrappers.get(indexOfPropertyValueToBeProcessed).isValueSetNull() &&
						cmsProperty.getSimpleTypeValues().get(indexOfPropertyValueToBeProcessed) != null){
					cmsProperty.removeSimpleTypeValue(indexOfPropertyValueToBeProcessed);
				}
			}
			catch(Exception e){
				//Ignore exception
			}
				
			removeWrapperAndShortIndexes();

			indexOfPropertyValueToBeProcessed = -1;
		}
	}


	private void removeWrapperAndShortIndexes() {
		try{
			simpleCmsPropertyValueWrappers.remove(indexOfPropertyValueToBeProcessed);
			
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
		// will allow the user to add values.
		// If property is binary and no values yet then no empty wrapper is created since it will be created when the user uploads a file through the upload dialog. 
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers) && cmsProperty != null){
			List values = cmsProperty.getSimpleTypeValues();
			if (CollectionUtils.isNotEmpty(values)) {
				for (int i=0; i<values.size(); i++){
					simpleCmsPropertyValueWrappers.add(new SimpleCmsPropertyValueWrapper(cmsProperty, i, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor));
				}
			}
			else {
				if (!getValueType().equals(ValueType.Binary)) {
					addNewSimpleCmsPropertyValueWrapper();
				}
			}
			
		}

		return simpleCmsPropertyValueWrappers;
	}


	public List<SelectItem> getValueEnumeration(){
		List<SelectItem> valueEnumerationSelectItems = new ArrayList<SelectItem>();
	    
		Map valueEnumeration = ((SimpleCmsPropertyDefinition)cmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueEnumeration != null){
			
			valueEnumerationSelectItems.add(new SelectItem("", JSFUtilities.getLocalizedMessage("object.edit.select.box.no.value", null)));
			
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
			indexOfPropertyValueToBeProcessed = indexOfValueToBeCopied;
			clipboard.setSelectedCmsPropertyWrapper(this);
		}
		else {
			logger.warn("Could not retrieve clipboard from session context.");
		}
	}
	
	
	public void fileUpload_Listener(UploadEvent event) {
		
		try {
			processUploadedFile(event.getUploadItem());

			//	resetAfterDialogueCompletion();
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "object.edit.action.uploadFile.uploadFailed", null, FacesMessage.SEVERITY_WARN);
		}
	}

	private void processUploadedFile(UploadItem uploadItem) throws IOException{

		String filename = null;
		byte[] filedata = null;
		GregorianCalendar lastModified;
		String mimeType = null;
		
		ConfigurableMimeFileTypeMap mimeTypesMap = (ConfigurableMimeFileTypeMap) JSFUtilities.getBeanFromSpringContext("mimeTypesMap");
		
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper;
		
		// We should check if we add a new binary or updating an existing one
		if (indexOfPropertyValueToBeProcessed == -1){
			// add a new value wrapper if property is defined to get multiple values or it is defined to get a single value and no value
			// exists yet
			if (isMultiple() || (!isMultiple() && getSimpleCmsPropertyValueWrappers().isEmpty())) {
				addNewSimpleCmsPropertyValueWrapper();
			}
			simpleCmsPropertyValueWrapper = 
					simpleCmsPropertyValueWrappers.get(simpleCmsPropertyValueWrappers.size()-1);
		}
		else {
			simpleCmsPropertyValueWrapper = 
					simpleCmsPropertyValueWrappers.get(indexOfPropertyValueToBeProcessed);
		}
		
		if (uploadItem.isTempFile()) { // if upload was set to use temp files
			filename = FilenameUtils.getName(uploadItem.getFileName());
			filedata = FileUtils.readFileToByteArray(uploadItem.getFile());
			lastModified = new GregorianCalendar(JSFUtilities.getLocale()); 
			lastModified.setTimeInMillis(uploadItem.getFile().lastModified());
			mimeType = mimeTypesMap.getContentType(filename);
			logger.debug("file uploaded " + filename);
		}
		else if (uploadItem.getData() != null){ // if upload was done in memory
			filename = uploadItem.getFileName();
			filedata = uploadItem.getData();
			lastModified = (GregorianCalendar) GregorianCalendar.getInstance(JSFUtilities.getLocale());
			mimeType = mimeTypesMap.getContentType(filename);
			logger.debug("file uploaded " + filename);
		}


		if (StringUtils.isBlank(mimeType)){
			mimeType = "application/octet-stream";
		}
		
		if (filedata == null || filename == null){
			JSFUtilities.addMessage(null, "object.edit.action.uploadFile.uploadFailed", null, FacesMessage.SEVERITY_WARN);
			return;
		}

		//Check if this property is the 'thumbnail' property
		try {
			byte[] thumbnailContent = null;
			if (simpleCmsPropertyValueWrapper.isThumbnailPropertyValueWrapper() ){
				//Generate thumbnail
				if (mimeType != null && 
						( mimeType.equals("image/jpeg")	|| mimeType.equals("image/gif")	|| mimeType.equals("image/png") ||
								mimeType.equals("image/x-png"))){
					thumbnailContent = ImageUtils.generateJpegThumbnailHQ(filedata, 128, 256);
				}
				else{
					JSFUtilities.addMessage(null, "object.edit.action.uploadFile.thumbnailCanBeCreatedOnlyFromJPGorPNGorGIFFiles", null, FacesMessage.SEVERITY_WARN);
					return;
				}
			}

			BinaryChannel binaryChannelValue = simpleCmsPropertyValueWrapper.getNewBinaryChannelValue();


			//Copy byte[] to a new byte[]
			byte[] newContent = null;
			if (thumbnailContent == null){
				newContent = new byte[filedata.length];
				System.arraycopy(filedata, 0, newContent, 0, filedata.length);
			}
			else{
				newContent = new byte[thumbnailContent.length];
				System.arraycopy(thumbnailContent, 0, newContent, 0, thumbnailContent.length);
			}

			binaryChannelValue.setContent(newContent);
			binaryChannelValue.setMimeType(new String(mimeType));
			binaryChannelValue.setSize(filedata.length);
			binaryChannelValue.setSourceFilename(new String(filename));
			binaryChannelValue.setModified(GregorianCalendar.getInstance(JSFUtilities.getLocale()));

			simpleCmsPropertyValueWrapper.setValue(binaryChannelValue);


		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "object.edit.action.uploadFile.uploadFailed", null, FacesMessage.SEVERITY_ERROR);
			logger.error("File upload failed", e);
		}
		finally
		{
			simpleCmsPropertyValueWrapper.setMimeTypeIconFilePath(null);
			indexOfPropertyValueToBeProcessed = -1;

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
