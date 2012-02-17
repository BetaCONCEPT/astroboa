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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.util.SchemaUtils;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

/**
 * @author gchomatas
 *
 */
@Name("importTaxonomyXMLDialogue")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportTaxonomyXMLDialogue extends AbstractUIBean {

	public ImportTaxonomyXMLDialogue() {

	}
	private AstroboaClient astroboaClient;

	private List<FacesMessage> messages;

	public void completeDialogue_UIAction() {
		//	upLoadFile_UIAction();
		resetAfterDialogueCompletion();
	}

	public void cancelDialogue_UIAction() {
		resetAfterDialogueCompletion();
	}

	public void resetAfterDialogueCompletion() {
		
		if (messages != null){
			messages.clear();
		}

	}

	public void displayMessages_UIAction(){
		
		if (CollectionUtils.isNotEmpty(messages)){
			for (FacesMessage facesMessage : messages){
				JSFUtilities.addMessage(null,facesMessage.getDetail(), facesMessage.getSeverity());
			}
		}
		
		resetAfterDialogueCompletion();
		
	}
	public void fileUpload_Listener(UploadEvent event) {
		try {
			processUploadedFile(event.getUploadItem());

			//	resetAfterDialogueCompletion();
			//resetAfterDialogueCompletion();
		}
		catch (Exception e) {
			addMessage("Συνέβη κάποιο σφάλμα. Δεν μεταφορτώθηκε το αρχείο", FacesMessage.SEVERITY_WARN);
		}
	}

	private void addMessage(String message, Severity severity) {
		
		if (messages == null){
			messages = new ArrayList<FacesMessage>();
		}
		
		FacesMessage facesMessage = new FacesMessage();
    	facesMessage.setSeverity(severity);
    	facesMessage.setDetail(message);
    	
    	messages.add(facesMessage);
		
	}

	private void processUploadedFile(UploadItem uploadItem) throws IOException{

		String filedata = null;

		if (uploadItem.isTempFile()) { // if upload was set to use temp files
			File file = uploadItem.getFile();
			filedata = FileUtils.readFileToString(file, "UTF-8");
			logger.debug("file uploaded " + file.getName());
		}
		else if (uploadItem.getData() != null){ // if upload was done in memory
			filedata = new String(uploadItem.getData(), "UTF-8");
			logger.debug("file uploaded " + uploadItem.getFileName());
		}


		if (StringUtils.isBlank(filedata)){
			addMessage("Δεν μεταφορτώθηκε σωστά το αρχείο. Προσπαθήστε πάλι", FacesMessage.SEVERITY_WARN);
			return;
		}

		try {
			
			//astroboaClient.getTaxonomyService().importTaxonomyFromXml(filedata);
			astroboaClient.getImportService().importTaxonomy(filedata,true);

			//All went well . Raise event to reload taxomomy tree
			Events.instance().raiseEvent(SeamEventNames.NEW_TAXONOMY_TREE);
		}
		catch (Exception e) {
			addMessage("Σφάλμα κατά την ανάγνωση του αρχείου. Είναι πιθανόν τα περιεχόμενα του αρχείου να μην ακολουθούν το Xml Schema (XSD) που έχει " +
					" ορισθεί για την ταξινομία (δες " + SchemaUtils.buildSchemaLocationForAstroboaModelSchemaAccordingToActiveClient()+") :" + e.toString(), FacesMessage.SEVERITY_ERROR);
			logger.error("", e);
		}
	}


	public void setAstroboaClient(
			AstroboaClient astroboaClient) {
		this.astroboaClient = astroboaClient;
	}


}
