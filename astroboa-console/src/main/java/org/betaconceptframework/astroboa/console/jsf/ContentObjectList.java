/**
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


import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.edit.ContentObjectEdit;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;

/**
 * Created on Nov 1, 2006
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectList extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	// Injected Beans
	private PageController pageController;
	private ContentService contentService;
	
	// holds the index of the top row in last viewed dataTable page. We use it in order to present the last shown data page each time the table is redrawn
	private int rowIndexOfCurrentPageTopElement;
	
	//	holds the info message which appears above the content list and informs the user about the contents of the list
	private String contentObjectListHeaderMessage; 
	
	// holds the row index of the last selected dataTable row i.e. the last selected content object to edit
	private int selectedRowIndex;
	
	// holds the page number of the content object list table that is currently active (i.e it is shown to the user)
	private int activeContentObjectListPage;
	
	//holds the Id (identifier) of the user selected content object for which will she wishes to do some action
	private String selectedContentObjectIdentifier;
	
	// holds the binary channel Id selected for download
	private String binaryChannelIdSelectedForDownload;
	
	// holds the page component that renders the user selected view type for the displayed list of content objects (i.e. List view, Icon View, Full View)
	private String contentObjectListCurrentViewPageComponent = "contentObjectList_ListViewRichFaces.xhtml";
	
	// holds the user selected width for content object thumbnails. Defaults to 64;
	private int thumbnailWidth = 64;
	
	//holds localized label for xml file in case list is exported to XML
	private String labelForFileGeneratedWhenExportingListToXml;
	
	
	public void changeContentObjectListView_UIAction(String selectedView) {

		
		if (StringUtils.isBlank(selectedView) || ContentObjectListDisplayOptions.ListView.toString().equals(selectedView)) {
			contentObjectListCurrentViewPageComponent = ContentObjectListDisplayOptions.ListView.getPageComponentThatRendersDisplayOption();
		}
		else if (ContentObjectListDisplayOptions.FullView.toString().equals(selectedView)) {
			contentObjectListCurrentViewPageComponent = ContentObjectListDisplayOptions.FullView.getPageComponentThatRendersDisplayOption();
		}
		else if (ContentObjectListDisplayOptions.IconView.toString().equals(selectedView))
			contentObjectListCurrentViewPageComponent = ContentObjectListDisplayOptions.IconView.getPageComponentThatRendersDisplayOption();
	}
	
	
	// Download is supported only for image/audio/video/text content types. It dowloads the primaryBinaryChannel which holds the related file binary data 
	public String downloadFile() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			BinaryChannel binaryChannel;
			// Retrieve the binary data of the Primary Binary Channel selected Content Object. Remember that for efficiency the binary data of binary channels are not retreived when a content object is retreived
			try {
					binaryChannel = contentService.getBinaryChannelById(binaryChannelIdSelectedForDownload);
			} catch (CmsException e) {
				throw new RuntimeException(e);
			}
			
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType(binaryChannel.getMimeType());
			response.setHeader("Content-Disposition", "attachment;filename=" + binaryChannel.getSourceFilename());	
			
			response.setContentLength((int)binaryChannel.getSize());
			
			InputStream contentAsStream = binaryChannel.getContentAsStream();
			
			try {
				
				if (contentAsStream != null)
				{
					ServletOutputStream servletOutputStream = response.getOutputStream();

					IOUtils.copy(contentAsStream, servletOutputStream);
					
					servletOutputStream.flush();

					facesContext.responseComplete();

				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally
			{
				binaryChannel = null;
				
				//Close Stream
				if (contentAsStream != null)
					try {
						contentAsStream.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			}
		}
		return null;
	}
	
	
	
	
	
	public void resetViewAndStateBeforeNewContentSearchResultsPresentation() {
		setContentObjectListHeaderMessage(null); // The message informing the use from which query the current list of content objects has been produced
		
		// reset scroller to show first page of results
		activeContentObjectListPage = 1;
		
		setLabelForFileGeneratedWhenExportingListToXml(null);
	}
	
	public String getContentObjectListHeaderMessage() {
		return contentObjectListHeaderMessage;
	}

	public void setContentObjectListHeaderMessage(
			String contentObjectListHeaderMessage) {
		this.contentObjectListHeaderMessage = contentObjectListHeaderMessage;
	}

	
	
	// all properties related to user selections in order to perform something to a content object
	public String getSelectedContentObjectIdentifier() {
		return selectedContentObjectIdentifier;
	}
	
	public void setSelectedContentObjectIdentifier(String selectedContentObjectIdentifier) {
		this.selectedContentObjectIdentifier = selectedContentObjectIdentifier;
	}
	

	public int getSelectedRowIndex() {
		return selectedRowIndex;
	}

	public void setSelectedRowIndex(int selectedRowIndex) {
		this.selectedRowIndex = selectedRowIndex;
		
		// calculate and set rowIndexOfCurrentPageTopElement according to the selected row index
		int page = getSelectedRowIndex() / pageController.getRowsPerDataTablePage();
		
        rowIndexOfCurrentPageTopElement = page * pageController.getRowsPerDataTablePage();
        
	}

	public int getRowIndexOfCurrentPageTopElement() {
		return rowIndexOfCurrentPageTopElement;
	}

	
	public void permanentlyRemoveSelectedContentObject_UIAction(String selectedContentObjectIdentifier, String selectedContentObjectType, Calendar selectedContentObjectCreatedDate) {
		// check if the object is already edited
		ContentObjectEdit contentObjectEdit = (ContentObjectEdit) Component.getInstance(ContentObjectEdit.class, false);
		if (contentObjectEdit != null && 
				contentObjectEdit.getSelectedContentObjectForEdit() != null &&
				contentObjectEdit.getSelectedContentObjectForEdit().getContentObject().getId() != null && 
				contentObjectEdit.getSelectedContentObjectForEdit().getContentObject().getId().equals(selectedContentObjectIdentifier)) {
			
			JSFUtilities.addMessage(null, "object.edit.objectIsEditedAndCannotBeRemoved", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try {
			contentService.deleteContentObject(selectedContentObjectIdentifier);
			
			Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_DELETED, new Object[]{selectedContentObjectType, selectedContentObjectIdentifier, selectedContentObjectCreatedDate});
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "object.edit.contentObjectCouldNotBePermanentlyRemovedError", null, FacesMessage.SEVERITY_ERROR);
			getLogger().error("The content object could not be permanently deleted." , e);
			return;
		}
		
		// generate a success message, reset the browsing trees to accomodate the change and finally change the view to show the conentObjectListPanel 
		JSFUtilities.addMessage(null, "object.edit.successful.delete.info.message", null, FacesMessage.SEVERITY_INFO);
		
		//contentObjectListHeaderMessage = JSFUtilities.getStringI18n("object.list.message.contentObjectListHeaderMessageAfterContentObjectRemoval");
		contentObjectListHeaderMessage = null;
		
		// remove object from table
		ContentObjectStatefulSearchService contentObjectStatefulSearchService = (ContentObjectStatefulSearchService) JSFUtilities.getBeanFromSpringContext("contentObjectStatefulSearchService");
		//List<ContentObjectUIWrapper> contentObjectUIWrapperList = (List<ContentObjectUIWrapper>) contentObjectStatefulSearchService.getReturnedContentObjects().getWrappedData();
		//contentObjectUIWrapperList.remove(contentObjectStatefulSearchService.getReturnedContentObjects().getRowIndex());
		
		// reset data page and decrease search results count
		contentObjectStatefulSearchService.setSearchResultSetSize(contentObjectStatefulSearchService.getSearchResultSetSize() - 1);
		UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
		if (contentObjectStatefulSearchService.getSearchResultSetSize() > 0)
			contentObjectStatefulSearchService.getReturnedContentObjects().reset();
		else {
			contentObjectStatefulSearchService.setReturnedContentObjects(null);
			uiComponentBinding.setListViewContentObjectTableComponent(null);
			uiComponentBinding.setListViewContentObjectTableScrollerComponent(null);
		}
	}
	
	public String getContentObjectListCurrentViewPageComponent() {
		return contentObjectListCurrentViewPageComponent;
	}

	public void setContentObjectListCurrentViewPageComponent(String contentObjectListCurrentViewPageComponent) {
		this.contentObjectListCurrentViewPageComponent = contentObjectListCurrentViewPageComponent;
	}

	public int getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public void setBinaryChannelIdSelectedForDownload(
			String binaryChannelIdSelectedForDownload) {
		this.binaryChannelIdSelectedForDownload = binaryChannelIdSelectedForDownload;
	}

	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public int getActiveContentObjectListPage() {
		return activeContentObjectListPage;
	}

	public void setActiveContentObjectListPage(int activeContentObjectListPage) {
		this.activeContentObjectListPage = activeContentObjectListPage;
	}


	/**
	 * @return the labelForFileGeneratedWhenExportingListToXml
	 */
	public String getLabelForFileGeneratedWhenExportingListToXml() {
		return labelForFileGeneratedWhenExportingListToXml;
	}


	/**
	 * @param labelForFileGeneratedWhenExportingListToXml the labelForFileGeneratedWhenExportingListToXml to set
	 */
	public void setLabelForFileGeneratedWhenExportingListToXml(
			String labelForFileGeneratedWhenExportingListToXml) {
		this.labelForFileGeneratedWhenExportingListToXml = labelForFileGeneratedWhenExportingListToXml;
	}
	
}


	