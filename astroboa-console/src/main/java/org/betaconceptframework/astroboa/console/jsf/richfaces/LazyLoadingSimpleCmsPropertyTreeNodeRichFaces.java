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
import java.util.Map.Entry;

import org.richfaces.model.TreeNode;

/**
 * Class representing a leaf in tree of Native Properties of a Content Object
 * 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingSimpleCmsPropertyTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces{

	private boolean mandatory;


	public LazyLoadingSimpleCmsPropertyTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean mandatory) {
		super(identifier, description, parent, type, true);
		this.mandatory = mandatory;
	}
	
	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		//Normally this should always return an empty iterator because this class
		//represents simple cms properties.
		return children.entrySet().iterator();
	}
	
	public boolean isMandatory(){
		return mandatory;
	}
}
