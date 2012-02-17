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
package org.betaconceptframework.astroboa.console.jsf.identity;

import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsRoleTreeNodeRichFaces;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("cmsUsersRolesTree")
@Scope(ScopeType.CONVERSATION)
public class CmsUsersRolesTree {

	public enum UserRoleTreeNodeType{
		ROLE,
		USER
	}
	
	private TreeNode cmsUsersRolesRootNode;
	
	@Unwrap
	public TreeNode getCmsUsersRolesRootNode() {
		if (cmsUsersRolesRootNode == null){
			
			cmsUsersRolesRootNode = new LazyLoadingCmsRoleTreeNodeRichFaces("0", 
					JSFUtilities.getLocalizedMessage("users.roles.navigation.tree.root.node.label", null), 
					null, false, null);
		}
		
		return cmsUsersRolesRootNode;
	}
}
