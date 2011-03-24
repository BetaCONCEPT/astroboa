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
package org.betaconceptframework.astroboa.console.jsf.clipboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.edit.SimpleCmsPropertyWrapper;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelectionIndex;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("clipboard")
@Scope(ScopeType.SESSION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class Clipboard {
	
	private ContentService contentService;

	@DataModel
	private List<ContentObjectItem> contentObjectItems;
	
	@DataModel
	private List<ContentObjectPropertyUrlItem> contentObjectPropertyUrlItems;
	
	@DataModelSelectionIndex("contentObjectItems")
	private Integer selectedContentObjectItemIndex;
	
	@DataModelSelectionIndex("contentObjectPropertyUrlItems")
	private Integer selectedContentObjectPropertyUrlItemIndex;
	
	private String selectedObjectId;
	
	private SimpleCmsPropertyWrapper selectedCmsPropertyWrapper;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void deleteContentObjectItem_UIAction(){
		if (selectedContentObjectItemIndex != null && contentObjectItems != null)
			contentObjectItems.remove(selectedContentObjectItemIndex.intValue());
			
	}
	
	public void addContentObjectItem(ContentObjectItem contentObjectItem){
		if (contentObjectItems == null)
			contentObjectItems = new ArrayList<ContentObjectItem>();
		
		contentObjectItems.add(contentObjectItem);
	}
	
	public void deleteContentObjectPropertyUrlItem_UIAction(){
		if (selectedContentObjectPropertyUrlItemIndex != null && contentObjectPropertyUrlItems != null)
			contentObjectPropertyUrlItems.remove(selectedContentObjectPropertyUrlItemIndex.intValue());
			
	}
	
	public void addContentObjectPropertyUrlItem(ContentObjectPropertyUrlItem contentObjectPropertyUrlItem){
		if (contentObjectPropertyUrlItems == null)
			contentObjectPropertyUrlItems = new ArrayList<ContentObjectPropertyUrlItem>();
		
		contentObjectPropertyUrlItems.add(contentObjectPropertyUrlItem);
	}
	
	public void clearClipboard(){
		if (contentObjectItems != null) {
			contentObjectItems.clear();
		}
		
		if (contentObjectPropertyUrlItems != null) {
			contentObjectPropertyUrlItems.clear();
		}
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_DELETED})
	public void refreshClipboard(String contentObjectType, String contentObjectId, Calendar dayToBeRefreshed) {
		
		//remove deleted content object from clipboard.
		
		// Remove it from Drag & Dropped items table 
		if (CollectionUtils.isNotEmpty(contentObjectItems) && StringUtils.isNotBlank(contentObjectId)){
			ContentObjectItem clipboardItemtoBeDeleted = null;
			
			for (ContentObjectItem contentObjectItem : contentObjectItems){
				if(contentObjectItem.getId() != null && contentObjectItem.getId().equals(contentObjectId)){
					clipboardItemtoBeDeleted = contentObjectItem;
					break;
				}
			}
			
			if (clipboardItemtoBeDeleted != null){
				contentObjectItems.remove(clipboardItemtoBeDeleted);
			}
		}
		
		// Remove property URLs for properties that belong to the deleted object
		if (CollectionUtils.isNotEmpty(contentObjectPropertyUrlItems) && StringUtils.isNotBlank(contentObjectId)){
			ContentObjectPropertyUrlItem clipboardItemtoBeDeleted = null;
			
			for (ContentObjectPropertyUrlItem contentObjectPropertyUrlItem : contentObjectPropertyUrlItems){
				if(contentObjectPropertyUrlItem.getId() != null && contentObjectPropertyUrlItem.getId().equals(contentObjectId)){
					clipboardItemtoBeDeleted = contentObjectPropertyUrlItem;
					break;
				}
			}
			
			if (clipboardItemtoBeDeleted != null){
				contentObjectPropertyUrlItems.remove(clipboardItemtoBeDeleted);
			}
		}
	}
	
	public void copyContentObjectToClipboard_UIAction(String contentObjectId, String systemName, String contentObjectTitle, String contentObjectTypeLocalized, String contentObjectType){
		try{
	
			if (StringUtils.isBlank(contentObjectId)){
				JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.requiresAnObjectToBeSelected", null, FacesMessage.SEVERITY_WARN);
				return;
			}
			
			ContentObjectItem contentObjectItem = new ContentObjectItem();
			contentObjectItem.setId(contentObjectId);
			contentObjectItem.setSystemName(systemName);
			contentObjectItem.setTitle(contentObjectTitle);
			contentObjectItem.setTypeLabel(contentObjectTypeLocalized);
			
			if (contentObjectType == null){
				contentObjectItem.setType(ValueType.ContentObject.toString());
			}
			else{
				contentObjectItem.setType(contentObjectType);
			}

			addContentObjectItem(contentObjectItem);
			
			JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.successful", null, FacesMessage.SEVERITY_INFO);

		}
		catch (Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	public void copyContentObjectToClipboard_UIAction() {
		try{
	
			if (StringUtils.isBlank(selectedObjectId)){
				JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.requiresAnObjectToBeSelected", null, FacesMessage.SEVERITY_WARN);
				return;
			}
			
			ContentObject contentObject = contentService.getContentObjectById(selectedObjectId, CacheRegion.NONE);
			
			if (contentObject != null) {
				ContentObjectItem contentObjectItem = new ContentObjectItem();
				contentObjectItem.setId(contentObject.getId());
				contentObjectItem.setSystemName(contentObject.getSystemName());

				contentObjectItem.setTitle(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue());
				contentObjectItem.setTypeLabel(contentObject.getTypeDefinition().getDisplayName().getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString()));
				contentObjectItem.setType(contentObject.getContentObjectType());

				addContentObjectItem(contentObjectItem);

				JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.successful", null, FacesMessage.SEVERITY_INFO);
			}
			else {
				logger.warn("Could not find an object in the repository for the selected id: '" + selectedObjectId + "'" );
				JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.failed", null, FacesMessage.SEVERITY_WARN);
			}

		}
		catch (Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "clipboard.dnd.object.copy.to.clipboard.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}

	public SimpleCmsPropertyWrapper getSelectedCmsPropertyWrapper() {
		return selectedCmsPropertyWrapper;
	}

	public void setSelectedCmsPropertyWrapper(
			SimpleCmsPropertyWrapper selectedCmsPropertyWrapper) {
		this.selectedCmsPropertyWrapper = selectedCmsPropertyWrapper;
	}
	
	public void setSelectedObjectId(String selectedObjectId) {
		this.selectedObjectId = selectedObjectId;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
}
