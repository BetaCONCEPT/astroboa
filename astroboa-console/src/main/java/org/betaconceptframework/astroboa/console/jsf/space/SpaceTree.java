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



import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

@Name("spaceTree")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceTree extends AbstractUIBean{

	private static final long serialVersionUID = 1L;

	@In
	private LocaleSelector localeSelector;
	
	private TreeNode spaceRootNode;
	private SpaceService spaceService;
	private LoggedInRepositoryUser loggedInRepositoryUser;
	
	public enum SpaceTreeNodeType{
		ROOT,
		USER_SPACE,
		SHARED_SPACE
	}

	@Unwrap
	public TreeNode getSpaceRootNode() {
		if (spaceRootNode == null){
			
			try{
			spaceRootNode = new LazyLoadingSpaceTreeNodeRichFaces("0", 
					" ", 
					null, 
					null, 
					SpaceTreeNodeType.ROOT.toString(), 
					false) ;
				
			//Add repository user space
			Space userSpace = loggedInRepositoryUser.getRepositoryUser().getSpace();
			
			LazyLoadingSpaceTreeNodeRichFaces userSpaceTreeNode = new LazyLoadingSpaceTreeNodeRichFaces(
					userSpace.getId(),
					// Currently we get the localized label of the root user space from message bundle.
					// We do not use the stored localized space name 
					// (i.e. userSpace.getLocalizedLabelForLocale(localeSelector.getLocaleString()) ) 
					// since it is not currently possible to change it or add 
					// new localized labels through the Astroboa Console.
					// Additionally the default English and Greek localized names have been changed (Space is called a "Folder" now to be 
					// more comprehensible to end users) and a migration of localized names for old repositories should be added to the 
					// installation package
					JSFUtilities.getStringI18n("space.my.space.root.name"), 
					spaceRootNode, 
					userSpace, 
					SpaceTreeNodeType.USER_SPACE.toString(), 
					false);
			
			spaceRootNode.addChild(userSpaceTreeNode.getIdentifier(), userSpaceTreeNode);
			
			//Add organization space
			Space organizationSpace = spaceService.getOrganizationSpace();
			organizationSpace.setCurrentLocale(localeSelector.getLocaleString());
			
			LazyLoadingSpaceTreeNodeRichFaces organizationSpaceTreeNode = new LazyLoadingSpaceTreeNodeRichFaces(
					organizationSpace.getId(),
					// Currently we get the localized label of the root Organization Space from message bundle.
					// We do not use the stored localized space name 
					// (i.e. organizationSpace.getLocalizedLabelForLocale(localeSelector.getLocaleString()) ) 
					// since it is not currently possible to change it or add 
					// new localized labels through the Astroboa Console.
					// Additionally the default English and Greek localized names have been changed (Space is called a "Folder" now to be 
					// more comprehensible to end users) and a migration of localized names for old repositories should be added to the 
					// installation package
					JSFUtilities.getStringI18n("space.organization.space.root.name"),
					spaceRootNode, 
					organizationSpace, 
					SpaceTreeNodeType.SHARED_SPACE.toString(), 
					false);
			
			spaceRootNode.addChild(organizationSpaceTreeNode.getIdentifier(), organizationSpaceTreeNode);
			
			}
			catch(Exception e){
				logger.warn("Could not create Space tree ",e);
			}
		}
		
		return spaceRootNode;
	}

	@Observer({SeamEventNames.RELOAD_SPACE_TREE_NODE})
	public void reloadSpaceTreeNode(String spaceId) {
		if (spaceRootNode != null && StringUtils.isNotBlank(spaceId))
				((LazyLoadingSpaceTreeNodeRichFaces)spaceRootNode).reloadSpace(spaceId);
	}
	
	@Observer({SeamEventNames.RELOAD_SPACE_TREE_NODES})
	public void reloadSpaceTreeNodes(List<String> spaceIdList) {
		if (spaceRootNode != null && CollectionUtils.isNotEmpty(spaceIdList))
				((LazyLoadingSpaceTreeNodeRichFaces)spaceRootNode).reloadSpaces(spaceIdList);
	}

	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	

}
