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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.console.jsf.portal.PortalTree.PortalTreeNodeType;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingPortalTreeRootNode  extends LazyLoadingTreeNodeRichFaces{

	private ContentService contentService;

	public LazyLoadingPortalTreeRootNode(LocaleSelector localeSelector){
		super("0","PortalRootNode", null, PortalTreeNodeType.PORTAL_ROOT_NODE.toString(), false);
		this.contentService = (ContentService) JSFUtilities.getBeanFromSpringContext("contentService");
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve portals: " + this.getIdentifier());

			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.addContentObjectTypeEqualsCriterion(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE);
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			contentObjectCriteria.doNotCacheResults();

			CmsOutcome<ContentObject> portalOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			if (portalOutcome != null && portalOutcome.getCount() > 0){

				List<ContentObject> portals = portalOutcome.getResults();

				//TODO Sort results according to localized label
				//Collections.sort(portals, new PortalContentObjectLocalizedLabelComparator());
				for (ContentObject portal: portals){

					LazyLoadingPortalTreeNode portalNode = new LazyLoadingPortalTreeNode(portal.getId(), portal.getId(), this, contentService);

					children.put(portalNode.getIdentifier(), portalNode);
				}
			}
		}

		return children.entrySet().iterator();
	}


	public void refreshPortalSection(String parentPortalSectionId) {
		if (StringUtils.isNotBlank(parentPortalSectionId)) {

			if (! children.isEmpty()){

				Collection<TreeNode> portalTreeNodes = children.values();

				for (TreeNode portalTreeNode : portalTreeNodes){
					((LazyLoadingPortalTreeNode)portalTreeNode).refreshPortalSectionEventRaised(parentPortalSectionId);
				}
			}
		}
		
	}

	public void contentObjectUpdateEventRaised(String contentObjectType,
			String contentObjectId) {
		if (StringUtils.isNotBlank(contentObjectId)) {
			
			if (StringUtils.equals(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE, contentObjectType)){
				children.clear();
			}
			
			else if (! children.isEmpty()){

				Collection<TreeNode> portalTreeNodes = children.values();

				for (TreeNode portalTreeNode : portalTreeNodes){
					((LazyLoadingPortalTreeNode)portalTreeNode).contentObjectUpdateEventRaised(contentObjectType, contentObjectId);
				}
			}
		}
		
	}

	public void newPortalAddedEventRaised(String portalId) {
		if (StringUtils.isNotBlank(portalId)){

			if (!children.containsKey(portalId)){
				
					LazyLoadingPortalTreeNode portalNode = new LazyLoadingPortalTreeNode(portalId, portalId, this, contentService);

					children.put(portalNode.getIdentifier(), portalNode);
				
			}
		}
		
	}
	
	
}