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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Holds various settings for logged in repository user.
 * Ultimately these settings should be saved in repository.
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("loggedInRepositoryUserSettings")
@Scope(ScopeType.SESSION)
public class LoggedInRepositoryUserSettings {

	//ContentObject Save specific settings
	boolean createVersionUponSuccessfulSave;
	//This is valid only when a new content object is saved
	boolean openNewContentObjectEditorUponSuccessfulSave;
	//This is valid only when content object is updated
	boolean closeEditorAfterSave;
	//Copy content object to clipboard upon successful save
	boolean copyContentObjectToClipboardUponSuccessfulSave;
	
	
	public boolean isCreateVersionUponSuccessfulSave() {
		return createVersionUponSuccessfulSave;
	}
	public void setCreateVersionUponSuccessfulSave(
			boolean createVersionUponSuccessfulSave) {
		this.createVersionUponSuccessfulSave = createVersionUponSuccessfulSave;
	}
	public boolean isOpenNewContentObjectEditorUponSuccessfulSave() {
		return openNewContentObjectEditorUponSuccessfulSave;
	}
	public void setOpenNewContentObjectEditorUponSuccessfulSave(
			boolean openNewContentObjectEditorUponSuccessfulSave) {
		this.openNewContentObjectEditorUponSuccessfulSave = openNewContentObjectEditorUponSuccessfulSave;
	}
	public boolean isCloseEditorAfterSave() {
		return closeEditorAfterSave;
	}
	public void setCloseEditorAfterSave(boolean closeEditorAfterSave) {
		this.closeEditorAfterSave = closeEditorAfterSave;
	}
	public boolean isCopyContentObjectToClipboardUponSuccessfulSave() {
		return copyContentObjectToClipboardUponSuccessfulSave;
	}
	public void setCopyContentObjectToClipboardUponSuccessfulSave(
			boolean copyContentObjectToClipboardUponSuccessfulSave) {
		this.copyContentObjectToClipboardUponSuccessfulSave = copyContentObjectToClipboardUponSuccessfulSave;
	}
	
	
	
	
}
