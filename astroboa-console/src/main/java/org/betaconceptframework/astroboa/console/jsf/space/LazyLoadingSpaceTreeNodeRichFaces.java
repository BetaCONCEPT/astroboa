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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.space.SpaceTree.SpaceTreeNodeType;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

/**
 * 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingSpaceTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private Space space;

	public LazyLoadingSpaceTreeNodeRichFaces(String identifier, String description, TreeNode parent, Space space,
			String type, boolean leaf) {
		super(identifier, description, parent, type, leaf);
		this.space = space;
	}


	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retreive children of node: " + identifier);
			List<Space> spaceList = null;

			try {
				if (space !=null)
					spaceList = space.getChildren();


				if (CollectionUtils.isNotEmpty(spaceList)) { // 
					List<LazyLoadingSpaceTreeNodeRichFaces> childSpaceNodeList = new ArrayList<LazyLoadingSpaceTreeNodeRichFaces>();
					
					int childIndex = 0;
					for (Space childSpace : spaceList) {
						LazyLoadingSpaceTreeNodeRichFaces childSpaceTreeNode = new LazyLoadingSpaceTreeNodeRichFaces(
								getIdentifier() + ":" + childIndex, 
								childSpace.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString()), 
								this, 
								childSpace, 
								getType(), 
								false);

						//	check if childTopic is leaf
						if (childSpace.getNumberOfChildren() == 0) {
							childSpaceTreeNode.leaf = true;
						}

						childSpaceNodeList.add(childSpaceTreeNode);
						++childIndex;
					}

					//Currently Astroboa repository backend does not support ordering in space.getChildren() method
					Collections.sort(childSpaceNodeList, new LazyLoadingSpaceTeeeNodeDescriptionComparator());

					for (LazyLoadingSpaceTreeNodeRichFaces spaceTreeNode : childSpaceNodeList)
						children.put(spaceTreeNode.identifier, spaceTreeNode);
				}
				else 
					leaf = true;
			} catch (Exception e) {logger.error("", e);}

		}


		return children.entrySet().iterator();
	}


	public Space getSpace() {
		return space;
	}

	public String spaceMatches(List<String> spaceIdList){
		if (getType().equals(SpaceTreeNodeType.ROOT.toString())) {
			return null;
		}
		
		for (String spaceId : spaceIdList) {
			if (space.getId().equals(spaceId)) {
				return spaceId;
			}
		}
		return null;
	}

	private boolean reloadSpaceEventRaised(List<String> spaceIdList) {
		String matchedSpaceId = spaceMatches(spaceIdList);
		if (matchedSpaceId != null){
			spaceIdList.remove(matchedSpaceId);
			reloadSpaceNode();
			if (CollectionUtils.isEmpty(spaceIdList)) {
				return true;
			}
			else {
				return false;
			}
		}
		else{
			//Check its children
			if (! children.isEmpty()){
				Collection<TreeNode> childSpaceTreeNodes = children.values();
				for (TreeNode childSpaceTreeNode : childSpaceTreeNodes){
					if (((LazyLoadingSpaceTreeNodeRichFaces)childSpaceTreeNode).reloadSpaceEventRaised(spaceIdList)){
						return true;
					}
				}
			}
			return false;
		}

	}

	public void reloadSpaces(List<String> spaceIdList) {
		reloadSpaceEventRaised(spaceIdList);
	}
	
	public void reloadSpace(String spaceId) {
		List<String> spaceIdList = new ArrayList<String>();
		spaceIdList.add(spaceId);
		reloadSpaceEventRaised(spaceIdList);
	}
	
	private void reloadSpaceNode() {
		if (space != null){
			space.setChildren(null);//Empty children so that lazy loading will fetch children again
			space.getChildren();
			this.children.clear();
			
			this.leaf = space.getNumberOfChildren() <= 0;
			
			// if reload is due to a rename we should change the node description
			//In case space does not have a parent this means that 
			// is the user's space. Do not alter description
			if (space.getParent() != null)
				this.description = space.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString());
		}
		
	}
	
	
}
