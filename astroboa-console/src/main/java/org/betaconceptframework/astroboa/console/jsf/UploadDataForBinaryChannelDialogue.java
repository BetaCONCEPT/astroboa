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
package org.betaconceptframework.astroboa.console.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.console.jsf.edit.ContentObjectEdit;
import org.betaconceptframework.astroboa.console.jsf.edit.SimpleCmsPropertyValueWrapper;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.ImageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

/**
 * @author gchomatas
 *
 */
@Name("uploadDataForBinaryChannelDialogue")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UploadDataForBinaryChannelDialogue extends AbstractUIBean {

	public UploadDataForBinaryChannelDialogue() {

	}

	@In
	private ContentObjectEdit contentObjectEdit;

	private byte[] uploadedData;
	private String uploadedDataMimeType;
	private String uploadedDataFilename;
	private int uploadedDataSize;

	private String methodToBeInvokedAfterSelection;
	private String idOfUIComponentToBeUpdatedAfterSelection;
	private boolean dialogueOpen;

	private List<String> idsOfUIComponentsToBeUpdatedAfterSelection;

	private Integer indexOfSimpleCmsPropertyWrapper;
	private Integer indexOfSimpleCmsPropertyValueWrapper;

	// we need the id of the selection panel in order to update it (close it) after the selection
	// if this id is not provided the default value is "uploadDataForBinaryChannelDialoguePanel"
	private String idOfSelectionDialoguePanel = "uploadDataForBinaryChannelDialoguePanel";

	private ConfigurableMimeFileTypeMap mimeTypesMap;

	public void startDialogue_UIAction() {
		setUIComponentsToBeRerenderedAfterDialogueCompletion();

		dialogueOpen = true;

	}

	public void completeDialogue_UIAction() {
		//	upLoadFile_UIAction();
		resetAfterDialogueCompletion();
	}

	public void cancelDialogue_UIAction() {
		resetAfterDialogueCompletion();
	}

	public void resetAfterDialogueCompletion() {

		uploadedData = null;
		uploadedDataFilename = null;
		uploadedDataMimeType = null;
		uploadedDataSize = 0;

		indexOfSimpleCmsPropertyValueWrapper = null;
		indexOfSimpleCmsPropertyWrapper = null;

		methodToBeInvokedAfterSelection = null;
		idOfUIComponentToBeUpdatedAfterSelection = null;

		dialogueOpen = false;

		// !!! NEVER RESET idsOfUIComponentsToBeUpdatedAfterSelection 
		// and idOfSelectionDialoguePanel because they are required for rerendering.
	}

	public void setUIComponentsToBeRerenderedAfterDialogueCompletion() {
		// generate a list of the ui component ids to rerender
		idsOfUIComponentsToBeUpdatedAfterSelection = new ArrayList<String>();
		// add the selection panel itself
		idsOfUIComponentsToBeUpdatedAfterSelection.add(idOfSelectionDialoguePanel);
		// add the component which will get the selected values
		//Check if there are more than one values
		if (StringUtils.contains(idOfUIComponentToBeUpdatedAfterSelection, ",")){
			String[] idArray = StringUtils.split(idOfUIComponentToBeUpdatedAfterSelection, ",");

			idsOfUIComponentsToBeUpdatedAfterSelection.addAll(Arrays.asList(idArray));
		}
		else
			idsOfUIComponentsToBeUpdatedAfterSelection.add(idOfUIComponentToBeUpdatedAfterSelection);

		// selectionButton.setReRender(idsOfUIComponentsToBeUpdatedAfterSelection);
	}

	/**
	 * After the selection dialogue has completed this UIAction is called to do some post actions related to the selection.
	 * Usually we need to set values in a managed bean in order to insert the selected value into some place in a form.
	 * So after the selection is done the property "methodToBeInvokedAfterSelection" is updated with a method expression string (a string of the form "managedBean.method") 
	 * that is used to invoke a method in a managed bean which will do the required post selection actions.
	 * This UIAction creates a method expression from the string and uses it to invoke the required method.     
	 */
	public void invokeMethodAfterDialogueCompletion() {
		if (methodToBeInvokedAfterSelection != null) {

			JSFUtilities
			.invokeMethodFromMethodExpression("#{" + methodToBeInvokedAfterSelection + "}", null, new Class<?>[0], null);
		}

	}

	public void fileUpload_Listener(UploadEvent event) {
		try {
			processUploadedFile(event.getUploadItem());

			//	resetAfterDialogueCompletion();
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "Συνέβη κάποιο σφάλμα. Δεν μεταφορτώθηκε το αρχείο", FacesMessage.SEVERITY_WARN);
		}
	}

	private void processUploadedFile(UploadItem uploadItem) throws IOException{

		SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper = null;

		try{
			simpleCmsPropertyValueWrapper = contentObjectEdit.getSimpleCmsPropertyValueWrapper(indexOfSimpleCmsPropertyWrapper, indexOfSimpleCmsPropertyValueWrapper);
		}catch (Exception e){
			JSFUtilities.addMessage(null, "Αποτυχία μεταφόρτωσης του αρχείου", FacesMessage.SEVERITY_ERROR);
			logger.error(" ", e);
			return;
		}

		if (simpleCmsPropertyValueWrapper == null ) {
			JSFUtilities.addMessage(null, "Αποτυχία μεταφόρτωσης του αρχείου", FacesMessage.SEVERITY_ERROR);
			logger.error("No simple cms property found to upload binary channel");
			return;
		}


		String filename = null;
		byte[] filedata = null;
		GregorianCalendar lastModified;
		String mimeType = null;

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
			JSFUtilities.addMessage(null, "Δεν μεταφορτώθηκε σωστά το αρχείο. Προσπαθήστε πάλι", FacesMessage.SEVERITY_WARN);
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
					JSFUtilities.addMessage(null, "Επιτρεπτά φορμά εικόνας JPEG / PNG / GIF", FacesMessage.SEVERITY_WARN);
					return;
				}
			}

			BinaryChannel binaryChannelValue = simpleCmsPropertyValueWrapper.getOrCreateNewBinaryChannelValue();


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
			JSFUtilities.addMessage(null, "Σφάλμα κατά την ανάγνωση των ιδιοτήτων του ψηφιακού καναλιού:" + e.toString(), FacesMessage.SEVERITY_ERROR);
			logger.error("", e);
		}
		finally
		{
			simpleCmsPropertyValueWrapper.setMimeTypeIconFilePath(null);

		}
	}


	public int getQuantityOfBinaryChannelsToUpload(){
		//If upload dialogue has been instantiated from a specific binary channel then only one binary channel
		//could be uploaded. Otherwise at most ten
		//In the future this number will be available from cms property definition
		return (indexOfSimpleCmsPropertyValueWrapper == null || indexOfSimpleCmsPropertyValueWrapper == -1 ? 10 : 1);
	}
	public String getMethodToBeInvokedAfterSelection() {
		return methodToBeInvokedAfterSelection;
	}

	public void setMethodToBeInvokedAfterSelection(
			String methodToBeInvokedAfterSelection) {
		this.methodToBeInvokedAfterSelection = methodToBeInvokedAfterSelection;
	}

	public String getIdOfUIComponentToBeUpdatedAfterSelection() {
		return idOfUIComponentToBeUpdatedAfterSelection;
	}

	public void setIdOfUIComponentToBeUpdatedAfterSelection(
			String idOfUIComponentToBeUpdatedAfterSelection) {
		this.idOfUIComponentToBeUpdatedAfterSelection = idOfUIComponentToBeUpdatedAfterSelection;
	}

	public boolean isDialogueOpen() {
		return dialogueOpen;
	}

	public void setDialogueOpen(boolean dialogueOpen) {
		this.dialogueOpen = dialogueOpen;
	}

	public List<String> getIdsOfUIComponentsToBeUpdatedAfterSelection() {
		return idsOfUIComponentsToBeUpdatedAfterSelection;
	}

	public void setIdsOfUIComponentsToBeUpdatedAfterSelection(
			List<String> idsOfUIComponentsToBeUpdatedAfterSelection) {
		this.idsOfUIComponentsToBeUpdatedAfterSelection = idsOfUIComponentsToBeUpdatedAfterSelection;
	}

	public String getIdOfSelectionDialoguePanel() {
		return idOfSelectionDialoguePanel;
	}

	public void setIdOfSelectionDialoguePanel(String idOfSelectionDialoguePanel) {
		this.idOfSelectionDialoguePanel = idOfSelectionDialoguePanel;
	}

	public byte[] getUploadedData() {
		return uploadedData;
	}

	public void setUploadedData(byte[] uploadedData) {
		this.uploadedData = uploadedData;
	}

	public String getUploadedDataMimeType() {
		return uploadedDataMimeType;
	}

	public void setUploadedDataMimeType(String uploadedDataMimeType) {
		this.uploadedDataMimeType = uploadedDataMimeType;
	}

	public String getUploadedDataFilename() {
		return uploadedDataFilename;
	}

	public void setUploadedDataFilename(String uploadedDataFilename) {
		this.uploadedDataFilename = uploadedDataFilename;
	}

	public int getUploadedDataSize() {
		return uploadedDataSize;
	}

	public void setUploadedDataSize(int uploadedDataSize) {
		this.uploadedDataSize = uploadedDataSize;
	}

	public void setIndexOfSimpleCmsPropertyWrapper(
			Integer indexOfSimpleCmsPropertyWrapper) {
		this.indexOfSimpleCmsPropertyWrapper = indexOfSimpleCmsPropertyWrapper;
	}

	public void setIndexOfSimpleCmsPropertyValueWrapper(
			Integer indexOfSimpleCmsPropertyValueWrapper) {
		this.indexOfSimpleCmsPropertyValueWrapper = indexOfSimpleCmsPropertyValueWrapper;
	}

	public void setMimeTypesMap(ConfigurableMimeFileTypeMap mimeTypesMap) {
		this.mimeTypesMap = mimeTypesMap;
	}



}
