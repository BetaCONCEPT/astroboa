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
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.portal.PortalTree.PortalTreeNodeType;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingPortalSectionTreeNode extends LazyLoadingTreeNodeRichFaces{

	
	private String portalSectionObjectId;
	private String portalContentObjectId;

	private ContentService contentService;

	public LazyLoadingPortalSectionTreeNode(String portalSectionObjectId,
			LazyLoadingTreeNodeRichFaces parentTreeNode, String portalContentObjectId, 
			ContentService contentService) {
		super(portalSectionObjectId, retrieveSectionTitle(portalSectionObjectId, contentService), parentTreeNode, PortalTreeNodeType.PORTAL_SECTION_NODE.toString(), false);
		
		this.portalSectionObjectId = portalSectionObjectId;
		this.portalContentObjectId = portalContentObjectId;
		this.contentService = contentService;
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve portal sub sections: " + this.getIdentifier());
			
			ContentObject portalSection = contentService.getContentObjectById(portalSectionObjectId, CacheRegion.NONE);
			
			if (portalSection != null)
			{
				//Retrieve Sub Portal Sections
				ObjectReferenceProperty subPortalSectionProperty = (ObjectReferenceProperty) portalSection.getCmsProperty("subPortalSection");

				if (! subPortalSectionProperty.hasNoValues()){

					for (ContentObject subPortalSection : subPortalSectionProperty.getSimpleTypeValues()){

						LazyLoadingPortalSectionTreeNode portalSectionTreeNode = new LazyLoadingPortalSectionTreeNode(subPortalSection.getId(), this, portalContentObjectId, contentService);

						children.put(portalSectionTreeNode.getIdentifier(), portalSectionTreeNode);
					}
				}
			}
		}
		
		return children.entrySet().iterator();
	}
	
	
	private static String retrieveSectionTitle(String portalSectionObjectId, ContentService contentService){
		
		ContentObject portalSection = contentService.getContentObjectById(portalSectionObjectId, CacheRegion.NONE);
		
		return ((StringProperty)portalSection.getCmsProperty("profile.title")).getSimpleTypeValue();
	}
	
	public void updateDescription() {
		this.description = retrieveSectionTitle(portalSectionObjectId, contentService);
		
	}
	
	public boolean refreshPortalSectionEventRaised(String parentPortalSectionId) {
		if (StringUtils.isNotBlank(parentPortalSectionId)) {

			if (! children.isEmpty()){

				if (children.containsKey(parentPortalSectionId)){
					((LazyLoadingPortalSectionTreeNode)children.get(parentPortalSectionId)).resetChildren();
				}
				else{
					Collection<TreeNode> portalTreeNodes = children.values();

					for (TreeNode portalTreeNode : portalTreeNodes){
					
						boolean parentPortalSectionReseted = ((LazyLoadingPortalSectionTreeNode)portalTreeNode).refreshPortalSectionEventRaised(parentPortalSectionId);
						
						if (parentPortalSectionReseted){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public void resetChildren() {
		children.clear();
	}

	public boolean contentObjectUpdateEventRaised(String contentObjectId) {
		if (StringUtils.isNotBlank(contentObjectId)) {

			if (! children.isEmpty()){

				if (children.containsKey(contentObjectId)){
					resetChildren();
					return true;
					//((LazyLoadingPortalSectionTreeNode)children.get(contentObjectId)).updateDescription();
				}
				else{
					Collection<TreeNode> portalTreeNodes = children.values();

					for (TreeNode portalTreeNode : portalTreeNodes){
					
						boolean parentPortalSectionReseted = ((LazyLoadingPortalSectionTreeNode)portalTreeNode).refreshPortalSectionEventRaised(contentObjectId);
						
						if (parentPortalSectionReseted){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public String getPortalSectionObjectId() {
		return portalSectionObjectId;
	}

	public String getPortalContentObjectId() {
		return portalContentObjectId;
	}

	
}
