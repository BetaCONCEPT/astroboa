
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


import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.portal.PortalTree.PortalTreeNodeType;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingPortalTreeNode  extends LazyLoadingTreeNodeRichFaces{

	private String portalContentObjectId;
	private LazyLoadingPortalSectionRootNode portalSectionRootNode;
	
	private ContentService contentService;

	public LazyLoadingPortalTreeNode(String identifier, String portalContentObjectId, TreeNode parent, ContentService contentService) {
		super(identifier, 
				retrieveLocalizedLabelForPortal(portalContentObjectId, contentService),
				parent, PortalTreeNodeType.PORTAL.toString(), false);
		
		this.portalContentObjectId = portalContentObjectId;
		this.contentService  = contentService;
	}

	private static String retrieveLocalizedLabelForPortal(String portalContentObjectId, ContentService contentService){
		
		if (StringUtils.isBlank(portalContentObjectId))
		{
			return JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null);
		}
		
		ContentObject portalContentObject = contentService.getContentObject(portalContentObjectId, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		
		if (portalContentObject == null)
		{
			return JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null);
		}
		
		StringProperty localizedLabelForPortal = (StringProperty)portalContentObject.getCmsProperty("localizedLabels."+JSFUtilities.getLocaleAsString());
		
		return localizedLabelForPortal == null || localizedLabelForPortal.hasNoValues() ?  
				portalContentObject.getSystemName()	: localizedLabelForPortal.getSimpleTypeValue();
		}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve content areas: " + this.getIdentifier());
			
			//Retrieve Portal Sections
			portalSectionRootNode = new LazyLoadingPortalSectionRootNode(portalContentObjectId+"PortalSectionRoot", portalContentObjectId, this, contentService);

			children.put(portalSectionRootNode.getIdentifier(), portalSectionRootNode);
			
		}
		return children.entrySet().iterator();
	}

	@Override
	public String getDescription() {
		//It may be the case that a description is an empty string
		if (StringUtils.isBlank(description))
			return null;
		return description;
	}
	
	public void refreshPortalSectionEventRaised(String parentPortalSectionId) {
		if (portalSectionRootNode != null){
			portalSectionRootNode.refreshPortalSectionEventRaised(parentPortalSectionId);
				
		}
		
	}

	public void contentObjectUpdateEventRaised(String contentObjectType,
			String contentObjectId) {
		if (portalSectionRootNode != null){
				
			 if (StringUtils.equals(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE, contentObjectType)){
				 portalSectionRootNode.contentObjectUpdateEventRaised(contentObjectId);
			 }
			 else if (StringUtils.equals(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE, contentObjectType)){
				 
				 if (StringUtils.equals(contentObjectId, portalContentObjectId)){
					 description = retrieveLocalizedLabelForPortal(portalContentObjectId, contentService);
				 }
			 }
				
		}
		
	}

	public String getPortalContentObjectId() {
		return portalContentObjectId;
	}
	
	
}
