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
package org.betaconceptframework.astroboa.console.jsf.portal;

import java.util.Calendar;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.edit.ObjectEditInit;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("portalEditor")
@Scope(ScopeType.CONVERSATION)
public class PortalEditor {

	@In(create=true)
	private ObjectEditInit objectEditInit;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ContentService contentService;

	private PageController pageController;

	private String portalContentObjectId;

	private String parentPortalSectionContentObjectId;


	

	public String addNewPortal_UIAction(){

		this.portalContentObjectId = null;
		this.parentPortalSectionContentObjectId = null;

		return objectEditInit.editNewObject_UIAction(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE);
	}

	public String addNewPortalSectionToPortal_UIContextMenuAction(String portalContentObjectId){

		if (StringUtils.isNotBlank(portalContentObjectId)){

			this.portalContentObjectId = portalContentObjectId;
			parentPortalSectionContentObjectId = null;

			return objectEditInit.editNewObject_UIAction(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);

		}

		return null;
	}

	public String addNewPortalSubSectionToPortalSection_UIContextMenuAction(String portalContentObjectId, String parentPortalSectionContentObjectId){

		if (StringUtils.isNotBlank(portalContentObjectId)){

			this.portalContentObjectId = portalContentObjectId;
			this.parentPortalSectionContentObjectId = parentPortalSectionContentObjectId;

			return objectEditInit.editNewObject_UIAction(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);

		}

		return null;
	}

	public String editPortal_UIAction(String portalContentObjectId){

		if (StringUtils.isNotBlank(portalContentObjectId)){

			this.portalContentObjectId = portalContentObjectId;

			return objectEditInit.editObject_UIAction(portalContentObjectId);

		}

		return null;
	}


	public String editPortalSection_UIAction(String portalContentObjectId, String portalSectionContentObjectId){

		if (StringUtils.isNotBlank(portalContentObjectId)){

			this.portalContentObjectId = portalContentObjectId;

			return objectEditInit.editObject_UIAction(portalSectionContentObjectId);

		}

		return null;
	}

	public void deleteSelectedPortalSection_UIContextMenuAction(String portalSectionToBeDeletedId){
		if (StringUtils.isNotBlank(portalSectionToBeDeletedId)){
			try{
				contentService.deleteContentObject(portalSectionToBeDeletedId);

				Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_DELETED, 
						new Object[]{"portalSection", portalSectionToBeDeletedId,null});

				JSFUtilities.addMessage(null, "Ο τομέας διαγράφηκε με επιτυχία", FacesMessage.SEVERITY_INFO);

			}
			catch (Exception e) {
				JSFUtilities.addMessage(null, "object.edit.contentObjectCouldNotBePermanentlyRemovedError", null, FacesMessage.SEVERITY_ERROR); 
				logger.error("The content object could not be permanently deleted. The error is: " , e); 
			}
		}
		else {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN); 
			logger.error("permanentlyRemoveContentObjectUIAction(): The content object could not be removed because it has not an identifier. Possibly you have tried to remove an object that has not yet been saved into the repository. This is UI problem that allowed to access this method");	 
		}
	}

	@Observer({SeamEventNames.CONTENT_OBJECT_ADDED})
	public void addPortalSectionToPortal(String contentObjectType, String contentObjectId, Calendar dayToBeRefreshed) {

		if (CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE.equals(contentObjectType)){
			ContentObject portalSectionContentObject = contentService.getContentObjectById(contentObjectId, null);

			ObjectReferenceProperty portalSectionProperty = null;

			if (StringUtils.isNotBlank(parentPortalSectionContentObjectId)){
				
				ContentObject parentPortalSectionContentObject = contentService.getContentObjectById(parentPortalSectionContentObjectId, null);
				portalSectionProperty = (ObjectReferenceProperty)parentPortalSectionContentObject.getCmsProperty("subPortalSection");
				portalSectionProperty.addSimpleTypeValue(portalSectionContentObject);
				contentService.saveContentObject(parentPortalSectionContentObject, false);

				Events.instance().raiseEvent(SeamEventNames.NEW_PORTAL_SECTION_ADDED, portalSectionProperty.getParentProperty().getId());
			}
			else if (StringUtils.isNotBlank(portalContentObjectId)){
				ContentObject portalContentObject = contentService.getContentObjectById(portalContentObjectId, null);
				portalSectionProperty = (ObjectReferenceProperty)portalContentObject.getCmsProperty("portalSection");
				portalSectionProperty.addSimpleTypeValue(portalSectionContentObject);
				contentService.saveContentObject(portalContentObject, false);

				Events.instance().raiseEvent(SeamEventNames.NEW_PORTAL_SECTION_ADDED, portalSectionProperty.getParentProperty().getId());
			}

			
		}
		else if (StringUtils.equals(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE, contentObjectType)){
			Events.instance().raiseEvent(SeamEventNames.NEW_PORTAL_ADDED, contentObjectId);
		}
	}


	public String initializeWebSiteTree_UIAction(){

		//Clear portal tree in order to force to generate nodes again
		Events.instance().raiseEvent(SeamEventNames.NEW_PORTAL_TREE);

		portalContentObjectId = null;
		
		parentPortalSectionContentObjectId = null;

		return null;

	}

	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

}
