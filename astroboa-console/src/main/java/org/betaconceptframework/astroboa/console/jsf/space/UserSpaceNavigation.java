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
package org.betaconceptframework.astroboa.console.jsf.space;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.jsf.space.SpaceItem.SpaceItemType;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.event.DropEvent;

@Name("userSpaceNavigation")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UserSpaceNavigation extends AbstractUIBean {

	private static final long serialVersionUID = 1L;
	
	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private LoggedInRepositoryUser loggedInRepositoryUser;
	private SpaceService spaceService;

	private Space currentSpace; 
	
	private String selectedSpaceId;
	
	private String selectedObjectId;
	
	private String newSpaceName;

	@Out(required=false)
	private List<Space> breadCrumbForUserSpace ;
	
	private SpaceItemLocalizeLabelComparator spaceItemLocalizeLabelComparator = new SpaceItemLocalizeLabelComparator();

	@In
	private LocaleSelector localeSelector;
	
	@Out(required=false)
	private List<SpaceItem> userSpaceItems;
	
	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	public String showUserSpace_UIAction(){
		
		return showSpace_UIAction(loggedInRepositoryUser.getRepositoryUser().getSpace());
	}
	
	public String showSpace_UIAction(Space space) {
		currentSpace = space;
		createBreadCrumbSpaces();
		createSpaceItems();
		
		return null;
	}
	
	public String showSpace_UIAction(String spaceId) {
		return showSpace_UIAction(spaceService.getSpace(spaceId, localeSelector.getLocaleString()));
	}

	@Factory("breadCrumbForUserSpace")
	public void getBreadCrumbForUserSpace(){
		if (currentSpace == null)
			currentSpace = loggedInRepositoryUser.getRepositoryUser().getSpace();

		createBreadCrumbSpaces();
	}
	
	private void createBreadCrumbSpaces() {
		breadCrumbForUserSpace = new ArrayList<Space>();
		
		breadCrumbForUserSpace.add(currentSpace);
		
		Space tempSpace = currentSpace.getParent();
		
		while(tempSpace != null){
			breadCrumbForUserSpace.add(tempSpace);
			tempSpace = tempSpace.getParent();
		}
		
		Collections.reverse(breadCrumbForUserSpace);
	}
	
	@Factory("userSpaceItems")
	public void getUserSpaceItems(){
		if (currentSpace == null)
			currentSpace = loggedInRepositoryUser.getRepositoryUser().getSpace();

		createSpaceItems();
	}

	private void createSpaceItems() {
		userSpaceItems = new ArrayList<SpaceItem>();

		//Load all child spaces
		List<Space> childSpaces = currentSpace.getChildren();
		if (CollectionUtils.isNotEmpty(childSpaces)){
			for (Space childSpace: childSpaces){
				addNewSpaceItemForSpace(childSpace);
			}
		}

		//Load all contentObject references
		List<String> contentObjectReferencesInCurrentSpace = currentSpace.getContentObjectReferences();

		if (CollectionUtils.isNotEmpty(contentObjectReferencesInCurrentSpace)) {
			for (String contentObjectReference : contentObjectReferencesInCurrentSpace) {
				ContentObject contentObject = contentService.getContentObjectByIdAndLocale(contentObjectReference, localeSelector.getLocaleString(), null);
				
				if (contentObject != null) {
					addNewSpaceIemForObject(contentObject);
				}
				else {
					logger.warn("A non existent object with id: '" + contentObjectReference + 
						"' is referenced in space with internal name: '" + currentSpace.getName() + 
						"' and localized name: '" + currentSpace.getLocalizedLabelForLocale(localeSelector.getLocaleString()));
				}
			}
		}

		sortSpaceItems();
	}
	
	private void sortSpaceItems() {
		Collections.sort(userSpaceItems, spaceItemLocalizeLabelComparator);
	}

	private void addNewSpaceItemForSpace(Space childSpace) {
		SpaceItem spaceItem = new SpaceItem();
		spaceItem.setType(SpaceItemType.SPACE);
		spaceItem.setLocalizedLabel(childSpace.getLocalizedLabelForCurrentLocale());
		spaceItem.setSpaceItemObject(childSpace);
		spaceItem.setSpaceService(spaceService);
		userSpaceItems.add(spaceItem);
	}
	
	private void addNewSpaceIemForObject(ContentObject object) {
		SpaceItem spaceItem = new SpaceItem();
		spaceItem.setType(SpaceItemType.CONTENTOBJECT);
		spaceItem.setSpaceItemObject(contentObjectUIWrapperFactory.getInstance(object));
		userSpaceItems.add(spaceItem);
	}
	
	// dummy function to call when re-rendering the user space tab
	public void reRenderSpace() {
		
	}
	
	public void removeObjectFromSpace(){
		try{
			
			List<String> contentObjectReferences = currentSpace.getContentObjectReferences();
			if (CollectionUtils.isNotEmpty(contentObjectReferences) && contentObjectReferences.contains(selectedObjectId)){
				contentObjectReferences.remove(selectedObjectId);
				
				spaceService.saveSpace(currentSpace);
				
				// recreate spaceItems
				createSpaceItems();
				
			}
		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN);
			logger.error("",e);
			return;
		}
	}
	
	public void deleteSpace(){
		
		if (StringUtils.isNotBlank(selectedSpaceId)) {
			try {
				
				for (SpaceItem spaceItem : userSpaceItems) {
					if (spaceItem.getType().equals(SpaceItem.SpaceItemType.SPACE)) {

						Space space = (Space) spaceItem.getSpaceItemObject();
						if (space.getId().equals(selectedSpaceId)) {
							spaceService.deleteSpace(space.getId());
							
							/* Removing space from List does not work
							 * because list stores proxy spaces and not space objects
							 * Either the list must be iterated to find the space to remove or we may reset the list in order to force
							 * lazy loading to initialize list again with the expense of one more query
							 * List<Space> children = currentSpace.getChildren();
							if (CollectionUtils.isNotEmpty(children))
									children.remove(space);
							*/
							currentSpace.setChildren(null);
							
							createSpaceItems();
							
							// the event allows the folder tree to be updated too 
							Events.instance().raiseEvent(SeamEventNames.RELOAD_SPACE_TREE_NODE, currentSpace.getId());
							break;
						}

					}
				}

			}
			catch(Exception e){
				JSFUtilities.addMessage(null, "application.unknown.error.message", null , FacesMessage.SEVERITY_WARN);
				logger.error("",e);
				return;
			}
		}
		else {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null , FacesMessage.SEVERITY_WARN);
			logger.error("Could not rename space. Either the space id or the new space name is empty");
		}

	}
	
	public void addNewSpace_UIAction(){
		try{
			if (currentSpace == null)
				currentSpace = loggedInRepositoryUser.getRepositoryUser().getSpace();

			Space newSpace = cmsRepositoryEntityFactory.newSpace();
			newSpace.setCurrentLocale(localeSelector.getLocaleString());
			newSpace.setOwner(loggedInRepositoryUser.getRepositoryUser());
			newSpace.addLocalizedLabel(localeSelector.getLocaleString(), JSFUtilities.getLocalizedMessage("space.administration.new.space.default.localized.label", null));

			currentSpace.addChild(newSpace);

			spaceService.saveSpace(newSpace);

			addNewSpaceItemForSpace(newSpace);

			sortSpaceItems();
			
			// the event allows the folder tree to be updated too
			Events.instance().raiseEvent(SeamEventNames.RELOAD_SPACE_TREE_NODE, currentSpace.getId());
		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_ERROR);
			logger.error("",e);
			return;
		}
	}
	
	public void changeSpaceLocalizedLabel(){

		if (StringUtils.isNotBlank(selectedSpaceId) && StringUtils.isNotBlank(newSpaceName)) {
			try {
				
				for (SpaceItem spaceItem : userSpaceItems) {
					if (spaceItem.getType().equals(SpaceItem.SpaceItemType.SPACE)) {

						Space space = (Space) spaceItem.getSpaceItemObject();
						if (space.getId().equals(selectedSpaceId)) {
							space.addLocalizedLabel(localeSelector.getLocaleString(), newSpaceName);
							space = spaceService.saveSpace(space);
							spaceItem.setLocalizedLabel(newSpaceName);
							sortSpaceItems();
							// the event allows the folder tree to be updated too
							Events.instance().raiseEvent(SeamEventNames.RELOAD_SPACE_TREE_NODE, currentSpace.getId());
							break;
						}

					}
				}

			}
			catch(Exception e){
				JSFUtilities.addMessage(null, "application.unknown.error.message", null , FacesMessage.SEVERITY_WARN);
				logger.error("",e);
				return;
			}
		}
		else {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null , FacesMessage.SEVERITY_WARN);
			logger.warn("Could not rename space. Either the space id or the new space name is empty");
		}

	}
	
	public void handleDraggedAndDroppedItem_Listener(DropEvent dropEvent) {
		if (dropEvent.getDragType().equals("contentObject")) {
			linkDraggedAndDroppedObject(dropEvent);
		}
		else if (dropEvent.getDragType().equals("userSpace")) {
			moveDraggedAndDroppedSpace(dropEvent);
		}
	}
	
	public void moveDraggedAndDroppedSpace(DropEvent dropEvent) {
		try {
			LazyLoadingSpaceTreeNodeRichFaces nodeToBeMoved = (LazyLoadingSpaceTreeNodeRichFaces) dropEvent.getDragValue();
			LazyLoadingSpaceTreeNodeRichFaces newParentNode = (LazyLoadingSpaceTreeNodeRichFaces) dropEvent.getDropValue();
			
			Space spaceToBeMoved = nodeToBeMoved.getSpace();
			Space newSpaceParent = newParentNode.getSpace();
			
			// prevent moving a space into itsself or into its children
			if (!newParentNode.getIdentifier().startsWith(nodeToBeMoved.getIdentifier())) {
				
				String oldSpaceParentId = spaceToBeMoved.getParent().getId();
				
				List<String> spaceIdList = new ArrayList<String>();
				spaceIdList.add(oldSpaceParentId);
				spaceIdList.add(newSpaceParent.getId());
				
				spaceToBeMoved.setParent(newSpaceParent);
				spaceService.saveSpace(spaceToBeMoved);
				
				// the event allows the folder tree to be updated
				Events.instance().raiseEvent(SeamEventNames.RELOAD_SPACE_TREE_NODES, spaceIdList);
				
				// we will also update the folder tab in the case that the user is currently viewing
				// the parent of the space to be moved, the space to be moved or the new parent node
				if (
					spaceToBeMoved.getId().equals(currentSpace.getId()) ||
					oldSpaceParentId.equals(currentSpace.getId()) ||
					newSpaceParent.getId().equals(currentSpace.getId())
					) {
					createBreadCrumbSpaces();
					createSpaceItems();
				}
				
				JSFUtilities.addMessage(null, "space.administration.move.success", null, FacesMessage.SEVERITY_INFO);
			}
			else {
				JSFUtilities.addMessage(null, "space.administration.move.cannotMoveSpaceIntoItselfOrIntoChildSpace", null, FacesMessage.SEVERITY_WARN);
				logger.warn("Could not move the space into itself or into a child space.");
			}
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "space.administration.move.failure", null, FacesMessage.SEVERITY_INFO);
			logger.warn("failed to move space", e);
		}

	}
	
	public void linkDraggedAndDroppedObject(DropEvent dropEvent) {
		try {
			ContentObjectUIWrapper objectToBeCopiedInSpace = (ContentObjectUIWrapper) dropEvent.getDragValue();
			LazyLoadingSpaceTreeNodeRichFaces  spaceTreeNodeIntoWhichObjectIsCopied = (LazyLoadingSpaceTreeNodeRichFaces) dropEvent.getDropValue();
			
			Space spaceIntoWhichObjectIsCopied = spaceTreeNodeIntoWhichObjectIsCopied.getSpace();
	
			List<String> spaceIdList = new ArrayList<String>();
			spaceIdList.add(spaceIntoWhichObjectIsCopied.getId());
			
			spaceIntoWhichObjectIsCopied.addContentObjectReference(objectToBeCopiedInSpace.getContentObject().getId());
			spaceService.saveSpace(spaceIntoWhichObjectIsCopied);
				
			// the event allows the folder tree to be updated
			Events.instance().raiseEvent(SeamEventNames.RELOAD_SPACE_TREE_NODES, spaceIdList);
				
			// we will also update the folder tab in the case that the user is currently viewing
			// the space into which we copied the object
			if (spaceIntoWhichObjectIsCopied.getId().equals(currentSpace.getId())) {
				createBreadCrumbSpaces();
				createSpaceItems();
			}
			
			JSFUtilities.addMessage(null, "space.administration.linkObject.success", null, FacesMessage.SEVERITY_INFO);
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "space.administration.linkObject.failure", null, FacesMessage.SEVERITY_WARN);
			logger.warn("Could not link the dragged object into space.", e);
		}
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}
	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public Space getCurrentSpace() {
		return currentSpace;
	}
	
	public void setSelectedSpaceId(String selectedSpaceId) {
		this.selectedSpaceId = selectedSpaceId;
	}

	public void setNewSpaceName(String newSpaceName) {
		this.newSpaceName = newSpaceName;
	}

	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}
	
	public void setSelectedObjectId(String selectedObjectId) {
		this.selectedObjectId = selectedObjectId;
	}

	
}