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
package org.betaconceptframework.astroboa.console.jsf.edit;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringFormat;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.console.jsf.visitor.CmsPropertyValidatorVisitor;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.PdfToTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;

/**
 * Wraps a value of a SimpleCmsProperty in order
 * to be successfully rendered and updated from JSF
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SimpleCmsPropertyValueWrapper {

	private final Logger logger = LoggerFactory.getLogger(getClass());


	private int valueIndex = -1;

	private SimpleCmsProperty simpleCmsProperty;

	private BinaryChannel binaryChannelValue;

	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;

	private String mimeTypeIconFilePath;

	private String unmanagedBinaryChannelRelativePath;

	private PasswordVerifier passwordVerifier = null;

	private CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor;
	
	private Object tempValue;

	private boolean valueIsNull;
	
	public SimpleCmsPropertyValueWrapper(SimpleCmsProperty simpleCmsProperty, int valueIndex,CmsRepositoryEntityFactory cmsRepositoryEntityFactory, 
			CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor){
		this.simpleCmsProperty = simpleCmsProperty;
		this.valueIndex = valueIndex;

		mimeTypeIconFilePath = null;

		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;

		this.cmsPropertyValidatorVisitor = cmsPropertyValidatorVisitor;

		if (cmsPropertyValidatorVisitor != null && simpleCmsProperty instanceof StringProperty && 
				((StringProperty)simpleCmsProperty).getPropertyDefinition() != null && 
				((StringProperty)simpleCmsProperty).getPropertyDefinition().isPasswordType()){

			if (this.cmsPropertyValidatorVisitor != null){
				this.cmsPropertyValidatorVisitor.registerSimpleCmsPropertyOfPasswordType(this);
			}
			
			passwordVerifier = new PasswordVerifier(getLocalizedLabelOfFullPathFromCmsProperty());
		}
	}



	public void reset() {
		binaryChannelValue = null;
		mimeTypeIconFilePath = null;

		if (passwordVerifier!=null){
			passwordVerifier.resetPasswords();
		}

	}


	public Object getValue(){
		
		if (tempValue != null){
			return tempValue;
		}
		
		if (simpleCmsProperty != null){

			Object value = null;
			if (simpleCmsProperty.getPropertyDefinition().isMultiple()){
				if (valueIndex >= simpleCmsProperty.getSimpleTypeValues().size()){
					//Probably a blank value has been inserted. Return null value
					//This is the normal case for primitive values.
					//When it comes for non-primitive values (Topic, Space ,etc)
					//it is possible that a new object will be created
					return null;
				}
				else
					value = simpleCmsProperty.getSimpleTypeValues().get(valueIndex);
			}
			else{
				//Single value property.
				value = simpleCmsProperty.getSimpleTypeValue();
			}

			if (value != null){
				if (value instanceof Calendar){
					return ((Calendar)value).getTime();
				}
				else if (value instanceof BinaryChannel){
					binaryChannelValue = (BinaryChannel)value;
					if (simpleCmsProperty.getPropertyDefinition() != null && 
							simpleCmsProperty.getPropertyDefinition() instanceof BinaryPropertyDefinition && 
							((BinaryPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).isBinaryChannelUnmanaged() ){
						unmanagedBinaryChannelRelativePath = binaryChannelValue.getRelativeFileSystemPath();
					}
				}
			}
			return value;
		}

		return null;
	}

	public void setValue(Object value){
		if (simpleCmsProperty != null){

			if (value != null){
				
				valueIsNull = false;
				
				if (value instanceof Date){

					//Special case. Must create calendar instances
					Calendar cal = Calendar.getInstance();
					cal.setTime((Date)value);
					value = cal;
				}
				else if (value instanceof String){

					if (StringUtils.isBlank((String)value)){
						value = null;
						valueIsNull = true;
					}
					else{
						if (ValueType.Long == simpleCmsProperty.getValueType()){
							try {
								value = Long.valueOf((String)value);
							} catch (NumberFormatException e) {
								setTemporaryValue(value);
								return;
							}
						}
						else if (ValueType.Double == simpleCmsProperty.getValueType()){
							try {
								value = Double.valueOf((String)value);
							} catch (NumberFormatException e) {
								
								setTemporaryValue(value);
								
								return;
							}
						}
					}
				}
				
			}
			else{
				valueIsNull = true;
			}

			if (simpleCmsProperty.getPropertyDefinition().isMultiple()){
				if (valueIndex >= simpleCmsProperty.getSimpleTypeValues().size()){
					//User has added a new value to list. Append list regardless of value index
					//This is done to cover the following case
					//Suppose list has already two values with indexes 0 and 1 and user
					//adds a new value. The property value list has two values (index = 1) but 
					//value index of the value wrapper becomes 2.
					//If we call method list.set(index, value)
					//it will throw an exception. Thus it is safe to just add value to the
					//end of the list if the value is not null
					if (value != null) {
						simpleCmsProperty.addSimpleTypeValue(value);
					}
				}
				else{
					simpleCmsProperty.getSimpleTypeValues().set(valueIndex, value);
				}
			}
			else{
				//Single value. Ignore value index and replace value
				simpleCmsProperty.setSimpleTypeValue(value);
			}
			
			tempValue = null;
		}
	}



	private void setTemporaryValue(Object value) {
		
		tempValue = value;
		
		cmsPropertyValidatorVisitor.registerInvalidSimpleCmsPropertyWithTempValue(this);
	}


	public boolean isImageMimeType(){
		return binaryChannelValue != null && binaryChannelValue.getMimeType() != null && 
		binaryChannelValue.getMimeType().startsWith("image");
	}
	
	// we need this in order to determine if we can show the image inline
	public boolean isJPGorPNGorGIFImage(){
		return binaryChannelValue != null && binaryChannelValue.getMimeType() != null && 
		(binaryChannelValue.getMimeType().equals("image/jpeg") || 
				binaryChannelValue.getMimeType().equals("image/png") || 
				binaryChannelValue.getMimeType().equals("image/x-png") ||
				binaryChannelValue.getMimeType().equals("image/gif"));
	}
	
	// we need this in order to determine if we can show the text of the pdf file inline
	public boolean isPdfFile(){
		return binaryChannelValue != null && binaryChannelValue.getMimeType() != null && 
		(binaryChannelValue.getMimeType().equals("application/pdf") || 
				binaryChannelValue.getMimeType().equals("application/x-pdf"));
	}
	
	// we need this in order to determine if we can show the text of the word file inline
	public boolean isWordFile(){
		return binaryChannelValue != null && binaryChannelValue.getMimeType() != null && 
		(binaryChannelValue.getMimeType().equals("application/msword"));
	}
	
	public String getPlainTextPreviewOfPdfFile() {
		PdfToTextExtractor pdfToTextExtractor = new PdfToTextExtractor();
		try {
			return pdfToTextExtractor.extractPdfToText(binaryChannelValue.getContent(), 1, 0, false, false, null);
		}
		catch (Exception e) {
			logger.error("Error while converting pdf file to text" , e);
			return null;
		}
	}

	public String getMimeTypeIconPath() {

		if (mimeTypeIconFilePath != null)
			return mimeTypeIconFilePath;

		// The Default Icon if we do not find a more appropriate one
		String mimeTypeIconPath = "images/cms-icons/text1.png";

		if (binaryChannelValue != null && binaryChannelValue.getMimeType() != null){
			try{
				mimeTypeIconFilePath = "images/mime-type_icons/"+binaryChannelValue.getMimeType()+".png";
				ServletContextResource mimeTypeIconResource = new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), mimeTypeIconFilePath);
				if (mimeTypeIconResource.exists()) 
					return mimeTypeIconFilePath;
				else{
					logger.warn("No icon found for mime type "+ binaryChannelValue.getMimeType() + " for file "+ binaryChannelValue.getSourceFilename());
					mimeTypeIconFilePath = mimeTypeIconPath;
				}
			}
			catch(Exception e){
				logger.error("No icon found for mime type "+ binaryChannelValue.getMimeType() + " for file "+ binaryChannelValue.getSourceFilename(), e);
				mimeTypeIconFilePath = mimeTypeIconPath;
			}

		}

		return	mimeTypeIconFilePath;	
	}

	public boolean isRichTextCmsProperty() {
		return simpleCmsProperty != null 
		&& ValueType.String == simpleCmsProperty.getValueType() 
		&& StringFormat.RichText == ((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getStringFormat();
	}

	public String getSimpleCmsPropertyLocalizedLabelOfFullPathforLocale() {
		if (simpleCmsProperty !=null)
			return simpleCmsProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(JSFUtilities.getLocaleAsString(), " > ");

		return "";
	}

	public String getFirstSentenceOfRichTextValue(){
		if (!isRichTextCmsProperty())
			return "";

		String richText = (String) getValue();
		if (StringUtils.isBlank(richText))
			return null;

		//Get first 200 characters and strip html tags
		String firstSentence = StringUtils.substring(richText.replaceAll("<(.|\n)+?>", ""), 0, 200);

		if (firstSentence != null){
			return firstSentence + "...";
		}

		return firstSentence;
	}
	public BinaryChannel getBinaryChannelValue() {
		if (binaryChannelValue == null)
			getValue();

		return binaryChannelValue;
	}

	public boolean isContentAvailable(){

		return getBinaryChannelValue() != null  //BinaryChannel value must exist 
		&& (
				binaryChannelValue.contentExists() // BinaryChannel value has a relative system path 
				|| binaryChannelValue.isNewContentLoaded() //BinaryChannel is new and content exists
		);
	}


	public BinaryChannel getNewBinaryChannelValue() {

		binaryChannelValue = cmsRepositoryEntityFactory.newBinaryChannel();
		binaryChannelValue.setName(simpleCmsProperty.getName());
		return binaryChannelValue;
	}



	public String getLocalizedLabelForCurrentLocaleForContentObjectTypeValue(){
		if (simpleCmsProperty != null && simpleCmsProperty.getValueType() == ValueType.ObjectReference){
			Object contentObject = getValue();

			if (contentObject != null){
				try{
					return ((ContentObject)contentObject).getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
				}
				catch(Exception e){
					logger.error("", e);
					return "";
				}
			}
		}

		return "";
	}

	public boolean isThumbnailPropertyValueWrapper(){
		return "thumbnail".equals(simpleCmsProperty.getName()) //Property should be named after 'thumbnail'
		&&  ( simpleCmsProperty.getParentProperty() instanceof ComplexCmsRootProperty);

	}


	public void setMimeTypeIconFilePath(String mimeTypeIconFilePath) {
		this.mimeTypeIconFilePath = mimeTypeIconFilePath;
	}

	public void addDefaultValue() {

		if (simpleCmsProperty != null && simpleCmsProperty.getValueType() != null){
			Object defaultValue = null;

			if (simpleCmsProperty.getPropertyDefinition() != null && ((SimpleCmsPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).isSetDefaultValue() ){
				defaultValue = ((SimpleCmsPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getDefaultValue();
			}

			/*if (defaultValue == null){

				switch (simpleCmsProperty.getValueType()) {
				case Binary:
					defaultValue = getOrCreateNewBinaryChannelValue();
					break;
				case Boolean:
					defaultValue = Boolean.TRUE;
					break;
				case Date:
					defaultValue = Calendar.getInstance(JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
					break;
				case Double:
				case Long:
				case String:
					break;
				case Topic:
				case ContentObject:
					logger.warn("Try to add blank value from SimpleCmsPropertyValueWrapper where there is a specific value wrapper for "+simpleCmsProperty.getValueType());
					break;
				case RepositoryUser:
				case Space:
					logger.warn("Try to add blank value for unsupported simple cms property type "+simpleCmsProperty.getValueType());
					break;
				default:
					logger.warn("Try to add blank value for invalid simple cms property type "+simpleCmsProperty.getValueType());
					break;
				}
			}*/

			if (defaultValue != null){
				setValue(defaultValue);
			}
		}
	}

	public void moveUp() {
		valueIndex--;
	}

	public void moveDown() {
		valueIndex++;
	}

	/**
	 * @return the unmanagedBinaryChannelRelativePath
	 */
	public String getUnmanagedBinaryChannelRelativePath() {
		return unmanagedBinaryChannelRelativePath;
	}

	/**
	 * @param unmanagedBinaryChannelRelativePath the unmanagedBinaryChannelRelativePath to set
	 */
	public void setUnmanagedBinaryChannelRelativePath(
			String unmanagedBinaryChannelRelativePath) {
		this.unmanagedBinaryChannelRelativePath = unmanagedBinaryChannelRelativePath;

		binaryChannelValue = cmsRepositoryEntityFactory.newUnmanagedBinaryChannel(unmanagedBinaryChannelRelativePath);
		binaryChannelValue.setName(simpleCmsProperty.getName());

		setValue(binaryChannelValue);
	}


	public List<String> verifyPassword(){  
		if (simpleCmsProperty instanceof StringProperty && 
				((StringProperty)simpleCmsProperty).getPropertyDefinition() != null && 
				((StringProperty)simpleCmsProperty).getPropertyDefinition().isPasswordType()){

			if (passwordVerifier == null){
				return Arrays.asList(JSFUtilities.getLocalizedMessage("application.unknown.error.message",  null));
			}
			else{

				String encryptedValue = passwordVerifier.newPasswordVerified(((StringProperty)simpleCmsProperty).getPropertyDefinition().getPasswordEncryptor(), 
						(String)getValue());

				if (encryptedValue != null){
					setValue(encryptedValue);
					
					return new ArrayList<String>();
				}
				else{
					return passwordVerifier.getValidatorMessages();
				}
			}
		}
		
		return new ArrayList<String>();
	}


	/**
	 * @return the passwordVerifier
	 */
	public PasswordVerifier getPasswordVerifier() {
		return passwordVerifier;
	}


	private String getLocalizedLabelOfFullPathFromCmsProperty(){
		return (simpleCmsProperty == null ? "" : 
			simpleCmsProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(JSFUtilities.getLocaleAsString(), " > "));
	}



	public String getFullPropertyPath() {
		return  ( simpleCmsProperty != null && simpleCmsProperty.getPropertyDefinition() != null ? 
					simpleCmsProperty.getPropertyDefinition().getFullPath() : 
						"" );
	}



	public void clearPasswords() {
		if (passwordVerifier!= null){
			passwordVerifier.resetPasswords();
		}
		
	}

	public boolean isMultiple(){
	
		return simpleCmsProperty!=null && simpleCmsProperty.getPropertyDefinition() !=null &&
		simpleCmsProperty.getPropertyDefinition().isMultiple();
	}
	
	public int getValueIndex(){
		return valueIndex;
	}

	public SimpleCmsProperty getSimpleCmsProperty() {
		return simpleCmsProperty;
	}


	public boolean hasTempValue(){
		return tempValue != null;
	}



	/**
	 * @param indexOf
	 */
	public void changeIndex(int index) {
		valueIndex = index;
	}
	
	public boolean isValueSetNull(){
		return valueIsNull;
	}
	
}
