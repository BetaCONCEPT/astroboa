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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.richfaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class LazyLoadingTreeNodeRichFaces implements TreeNode {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected String identifier;
	protected String description;
	protected String type;
	private TreeNode parent;
	protected boolean leaf;
	protected Map<String,TreeNode> children = new LinkedHashMap<String, TreeNode>();
	
	public LazyLoadingTreeNodeRichFaces() {
		
	}

	public LazyLoadingTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean leaf) {
		this.identifier = identifier;
		this.description = description;
		this.parent = parent;
		this.type = type;
		this.leaf = leaf;
	}

	


	public abstract Iterator<Map.Entry<String, TreeNode>> getChildren();


	public String getIdentifier() {
		return identifier;
	}


	public String getType() {
		return type;
	}


	public int getChildCount() {
		return children.size();
	}


	public void addChild(Object identifier, TreeNode childNode) {
		children.put((String)identifier, childNode);
	}




	public TreeNode getChild(Object identifier) {
		
		return children.get(identifier);
	}




	public Object getData() {
		return this;
	}




	public TreeNode getParent() {
		return parent;
	}




	public boolean isLeaf() {
		return leaf;
	}




	public void removeChild(Object identifier) {
		children.remove(identifier);
	}




	public void setData(Object arg0) {
		
		
	}


	public void setParent(TreeNode parentNode) {
		parent = parentNode;
	}




	public String getDescription() {
		return description;
	}

}
