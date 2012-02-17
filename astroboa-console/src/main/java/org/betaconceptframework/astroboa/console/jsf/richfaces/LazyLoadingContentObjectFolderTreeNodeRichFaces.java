/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf.richfaces;


import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.commons.comparator.ContentObjectFolderComparator;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author gchomatas
 * Created on Sept 5, 2007
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingContentObjectFolderTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private ContentObjectFolder contentObjectFolder;
	private String contentType;
	private String localizedLabelOfContentType;

	public LazyLoadingContentObjectFolderTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean leaf, 
			ContentObjectFolder contentObjectFolder, String contentType, String localizedLabelOfContentType) {
		super(identifier, description, parent, type, leaf);
		this.contentObjectFolder = contentObjectFolder;
		this.contentType = contentType;
		this.localizedLabelOfContentType = localizedLabelOfContentType;
	}


	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {

			logger.debug("retrieve children of node: " + this.getIdentifier());
			ContentService contentService = (ContentService) JSFUtilities.getBeanFromSpringContext("contentService");
			DefinitionService definitionService = (DefinitionService) JSFUtilities.getBeanFromSpringContext("definitionService");

			List<ContentObjectFolder> contentObjectFolderList;
			String contentTypeForNewNodes;
			String localizedLabelOfContentTypeForNewNodes;

			try {
				if (this.getIdentifier().equals("0"))
				{
					contentObjectFolderList = contentService.getRootContentObjectFolders(JSFUtilities.getLocaleAsString());

					//This should be used in case when tree is rendered all the way from start and lazy 
					// loading does not actually happen
					//contentObjectFolderList = contentService.getRootContentObjectFolders(TreeDepth.FULL.asInt());
				}
				else
				{
					// 1 implies to load children as well
					contentObjectFolderList = contentService.getContentObjectFolderTree(contentObjectFolder.getId(), 1, false, JSFUtilities.getLocaleAsString()).getSubFolders();

					

					//This should be used in case when tree is rendered all the way from start and lazy 
					// loading does not actually happen
					//contentObjectFolderList = contentObjectFolder.getSubFolders();
				}

				//Sort list  by locale
				if (contentObjectFolderList != null){
					Collections.sort(contentObjectFolderList, new ContentObjectFolderComparator(JSFUtilities.getLocaleAsString()));
				}
				
				int nodeIndex = 0;
				if (CollectionUtils.isNotEmpty(contentObjectFolderList)) {
					String treeNodeType;
					for (ContentObjectFolder contentObjectFolder : contentObjectFolderList) {
						if (contentObjectFolder.getType() == ContentObjectFolder.Type.DAY) {
							treeNodeType = "dayFolder";
							contentTypeForNewNodes = contentType;
							localizedLabelOfContentTypeForNewNodes = localizedLabelOfContentType;
						}
						else if (contentObjectFolder.getType() == ContentObjectFolder.Type.MONTH) {
							treeNodeType = "monthFolder";
							contentTypeForNewNodes = contentType;
							localizedLabelOfContentTypeForNewNodes = localizedLabelOfContentType;
						}
						else if (contentObjectFolder.getType() == ContentObjectFolder.Type.YEAR) {
							treeNodeType = "yearFolder";
							contentTypeForNewNodes = contentType;
							localizedLabelOfContentTypeForNewNodes = localizedLabelOfContentType;
						}
						else if (contentObjectFolder.getType() == ContentObjectFolder.Type.CONTENT_TYPE) {
							
							treeNodeType = "contentTypeFolder";
							contentTypeForNewNodes = contentObjectFolder.getName();
							localizedLabelOfContentTypeForNewNodes = contentObjectFolder.getLocalizedLabelForCurrentLocale();

							if (StringUtils.isBlank(localizedLabelOfContentTypeForNewNodes)){
								localizedLabelOfContentTypeForNewNodes = "No localized label found for content type "+ contentType;
							}
						}
						else {
							treeNodeType = "yearFolder";
							contentTypeForNewNodes = contentType;
							localizedLabelOfContentTypeForNewNodes = localizedLabelOfContentType;
						}
						
						if (definitionService.hasContentObjectTypeDefinition(contentTypeForNewNodes)){
							

						TreeNode childContentObjectFolderTreeNode = 
							new LazyLoadingContentObjectFolderTreeNodeRichFaces(
									this.getIdentifier() + ":" + String.valueOf(nodeIndex),
									contentObjectFolder.getLocalizedLabelForCurrentLocale(),
									this,
									treeNodeType,
									(treeNodeType.equals("dayFolder")? true : false), 
									contentObjectFolder, 
									contentTypeForNewNodes,
									localizedLabelOfContentTypeForNewNodes);

						children.put(this.getIdentifier() + ":" + String.valueOf(nodeIndex), childContentObjectFolderTreeNode);
						nodeIndex++;
						}
					}
				}
				else {
					leaf = true;
				}
			} catch (Exception e) {
				logger.error("trying to retreive contenfolder children:", e);}


		}

		return children.entrySet().iterator();
	}


	public ContentObjectFolder getContentObjectFolder() {
		return contentObjectFolder;
	}


	public String getContentType() {
		return contentType;
	}


	public String getLocalizedLabelOfContentType() {
		return localizedLabelOfContentType;
	}


	public void contentObjectAddedOrDeletedEventRaised(String contentObjectType, Calendar dayToBeRefreshed) {

		boolean contentTypeIsBlank = StringUtils.isBlank(contentObjectType);

		//Try to locate content type folder
		if (contentObjectFolder == null){
			if (MapUtils.isNotEmpty(children)){
				for (TreeNode childTreeNode : children.values()){
					LazyLoadingContentObjectFolderTreeNodeRichFaces childFolder = (LazyLoadingContentObjectFolderTreeNodeRichFaces)childTreeNode;
					if (childFolder.getContentObjectFolder() != null){
						if (contentTypeIsBlank || contentObjectType.equals(childFolder.getContentObjectFolder().getName())){
							childFolder.refreshContents(dayToBeRefreshed);

							if (!contentTypeIsBlank){
								//Do not continue. Content Type has been provided and corresponding node refreshed
								return;
							}
						}
					}
				}


				if (!contentTypeIsBlank){
					//In this point content type does not correspond to any tree node.
					//Probably a new tree node. Clear children to force reloading
					children.clear();
				}
			}
		}
	}

	private boolean refreshContents(Calendar dayToBeRefreshed) {

		if (contentObjectFolder != null && contentObjectFolder.getType() !=null){
			switch (contentObjectFolder.getType()) {
			case CONTENT_TYPE:

				String currentYear = (dayToBeRefreshed == null ? DateUtils.format(Calendar.getInstance(), "yyyy") :
																		DateUtils.format(dayToBeRefreshed, "yyyy") );

				//Found the type. Continue to current year folder if it is loaded
				boolean foundYearFolder =  findChildTreeNodeWithNameAndRefresh(currentYear,dayToBeRefreshed);

				if (!foundYearFolder && ! children.isEmpty()){
					//Clear children to force reloading
					children.clear();
				}
				
				return true;
				
			case YEAR:
				// Search for month
				//For Java January is 0, but in Astroboa January is 1
				String currentMonth = (dayToBeRefreshed == null ?  String.valueOf(Calendar.getInstance().get(Calendar.MONTH) +1) :
																			String.valueOf(dayToBeRefreshed.get(Calendar.MONTH) +1) );

				boolean foundMonthFolder = findChildTreeNodeWithNameAndRefresh(currentMonth,dayToBeRefreshed);

				if (!foundMonthFolder && ! children.isEmpty()){
					//Clear children to force reloading
					children.clear();
				}

				return true;
				
			case MONTH:
				// Search for day
				String currentDay = (dayToBeRefreshed == null ?  String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) :
																	   String.valueOf(dayToBeRefreshed.get(Calendar.DAY_OF_MONTH)) );

				boolean foundDayFolder =  findChildTreeNodeWithNameAndRefresh(currentDay,dayToBeRefreshed);

				if (!foundDayFolder && ! children.isEmpty()){
					//Clear children to force reloading
					children.clear();
				}
				
				return true;
				

			case DAY:
				// Found correct day. Reload content object folder 
				ContentService contentService = (ContentService) JSFUtilities.getBeanFromSpringContext("contentService");

				contentObjectFolder = contentService.getContentObjectFolderTree(contentObjectFolder.getId(), 1, false, JSFUtilities.getLocaleAsString());

				return true;

			default:
				//Normally code should never reach this point
				return false;
			}
		}
		else{
			return false;
		}


	}


	private boolean findChildTreeNodeWithNameAndRefresh(String childFolderName, Calendar dayToBeRefreshed){
		if (MapUtils.isNotEmpty(children) && StringUtils.isNotBlank(childFolderName)){
			for (TreeNode childTreeNode : children.values()){
				LazyLoadingContentObjectFolderTreeNodeRichFaces childFolder = (LazyLoadingContentObjectFolderTreeNodeRichFaces)childTreeNode;
				if (childFolder.getContentObjectFolder() != null && 
						childFolderName.equals(childFolder.getContentObjectFolder().getName())){
					boolean foundFolder = childFolder.refreshContents(dayToBeRefreshed);
					if (foundFolder){
						return true;
					}
				}
			}
		}

		return false;
	}


}
