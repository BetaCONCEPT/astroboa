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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.portal.PortalTree.PortalTreeNodeType;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingPortalSectionRootNode extends LazyLoadingTreeNodeRichFaces{

	private String portalContentObjectId;
	private ContentService contentService;
	
	public LazyLoadingPortalSectionRootNode(String identifier, String portalContentObjectId,
			LazyLoadingPortalTreeNode parentTreeNode, ContentService contentService) {
		
		super(identifier, JSFUtilities.getStringI18n("navigation.menu.webPages"), parentTreeNode, PortalTreeNodeType.PORTAL_SECTION_ROOT_NODE.toString(), false);
		
		this.portalContentObjectId = portalContentObjectId;
		this.contentService = contentService;
		
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve portal sections: " + this.getIdentifier());
			
			ContentObject portalContentObject = contentService.getContentObject(portalContentObjectId, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
			
			//Retrieve Portal Sections 
			ObjectReferenceProperty portalSectionProperty = (ObjectReferenceProperty) portalContentObject.getCmsProperty("portalSection");
			
			if (portalSectionProperty != null && ! portalSectionProperty.hasNoValues()){
				
				for (ContentObject portalSection : portalSectionProperty.getSimpleTypeValues()){
					
					LazyLoadingPortalSectionTreeNode portalSectionTreeNode = new LazyLoadingPortalSectionTreeNode(portalSection.getId(), this, portalContentObjectId, contentService);
					
					children.put(portalSectionTreeNode.getIdentifier(), portalSectionTreeNode);
				}
			}
		}
		return children.entrySet().iterator();
	}

	public void refreshPortalSectionEventRaised(String parentPortalSectionId) {
		if (StringUtils.isNotBlank(parentPortalSectionId)) {

			if (StringUtils.equals(portalContentObjectId, parentPortalSectionId))
			{
				resetChildren();
			}
			else if (! children.isEmpty()){

				if (children.containsKey(parentPortalSectionId)){
					((LazyLoadingPortalSectionTreeNode)children.get(parentPortalSectionId)).resetChildren();
				}
				else{
					Collection<TreeNode> portalTreeNodes = children.values();

					for (TreeNode portalTreeNode : portalTreeNodes){
					
						boolean parentPortalSectionReseted = ((LazyLoadingPortalSectionTreeNode)portalTreeNode).refreshPortalSectionEventRaised(parentPortalSectionId);
						
						if (parentPortalSectionReseted){
							break;
						}
					}
				}
			}
		}
		
	}

	private void resetChildren() {
		children.clear();
	}

	public void contentObjectUpdateEventRaised(String contentObjectId) {
		if (StringUtils.isNotBlank(contentObjectId)) {

			if (! children.isEmpty()){

				if (children.containsKey(contentObjectId)){
					resetChildren();
					//((LazyLoadingPortalSectionTreeNode)children.get(contentObjectId)).updateDescription();
				}
				else{
					Collection<TreeNode> portalTreeNodes = children.values();

					for (TreeNode portalTreeNode : portalTreeNodes){
					
						boolean parentPortalSectionReseted = ((LazyLoadingPortalSectionTreeNode)portalTreeNode).contentObjectUpdateEventRaised(contentObjectId);
						
						if (parentPortalSectionReseted){
							break;
						}
					}
				}
			}
		}
		
	}

	public String getPortalContentObjectId() {
		return portalContentObjectId;
	}
	
	
}
