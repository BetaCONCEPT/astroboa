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
package org.betaconceptframework.astroboa.console.jsf.portal;



import java.util.Calendar;

import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

@Name("portalTree")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PortalTree extends AbstractUIBean{

	private static final long serialVersionUID = 1L;
	
	public enum PortalTreeNodeType{
		PORTAL_ROOT_NODE,
		PORTAL,
		CONTENT_AREA_TAXONOMY, 
		CONTENT_AREA, 
		MENU_ROOT_NODE,
		MENU,
		MENU_ITEM,
		PORTAL_SECTION_ROOT_NODE,
		PORTAL_SECTION_NODE
	}
	
	private TreeNode portalRootNode;
	
	
	@In
	private LocaleSelector localeSelector;
	
	@Unwrap
	public TreeNode getPortalRootNode() {
		if (portalRootNode == null){
			
			portalRootNode = new LazyLoadingPortalTreeRootNode(localeSelector);
		}
		
		return portalRootNode;
	}
	
	@Observer({SeamEventNames.NEW_PORTAL_TREE})
	public void clearTree() {
		portalRootNode = null;
	}

	@Observer({SeamEventNames.CONTENT_OBJECT_DELETED})
	public void portalSectionDeleted(String contentObjectType, String contentObjectId, Calendar dayToBeRefreshed) {
		clearTree();
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_UPDATED})
	public void portalSectionUpdated(String contentObjectType, String contentObjectId){
		if (portalRootNode != null) {
			((LazyLoadingPortalTreeRootNode)portalRootNode).contentObjectUpdateEventRaised(contentObjectType, contentObjectId);
		}
	}
	
	@Observer({SeamEventNames.NEW_PORTAL_ADDED})
	public void newPortalAdded(String portalId){
		if (portalRootNode != null) {
			((LazyLoadingPortalTreeRootNode)portalRootNode).newPortalAddedEventRaised(portalId);
		}
	}
	@Observer({SeamEventNames.NEW_PORTAL_SECTION_ADDED})
	public void newPortalSectionAdded(String parentPortalSectionId){
		if (portalRootNode != null) {
			((LazyLoadingPortalTreeRootNode)portalRootNode).refreshPortalSection(parentPortalSectionId);
		}
	}
}
