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


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * Created on Sept 30, 2007
 */
public class LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private LocalizableCmsDefinition complexCmsPropertyDefinition;
	private ComplexCmsProperty complexCmsProperty;
	
	public LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, 
			boolean leaf, LocalizableCmsDefinition complexCmsPropertyDefinition, ComplexCmsProperty complexCmsProperty) {
		super(identifier, description, parent, type, leaf);
		this.complexCmsPropertyDefinition = complexCmsPropertyDefinition;
		this.complexCmsProperty = complexCmsProperty;
	}
	
	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve children of node: " + identifier);
			
			// we assume it has no complex properties and assign it as a leaf. If we find child complex properties we will remove it from leaf
			//leaf = true;
			leaf = false; //Complex property has always at least one child property
			
			// get the child property definitions and generate nodes for each complex property
			Collection<CmsPropertyDefinition> childPropertyDefinitions = null;
			
			if (complexCmsPropertyDefinition instanceof ContentObjectTypeDefinition)
				childPropertyDefinitions = ((ContentObjectTypeDefinition)complexCmsPropertyDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(JSFUtilities.getLocaleAsString()).values();
			else
				childPropertyDefinitions = ((ComplexCmsPropertyDefinition)complexCmsPropertyDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(JSFUtilities.getLocaleAsString()).values();

			if (childPropertyDefinitions != null && !childPropertyDefinitions.isEmpty()) {
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions) {

					// we are looking for complex type properties excluding "profile" and "accessibility" which have a special treatment at the UI
					if (!childPropertyDefinition.getName().equals("profile") && 
							!childPropertyDefinition.getName().equals("accessibility") &&
							!childPropertyDefinition.getName().equals("workflow"))
					{
						switch (childPropertyDefinition.getValueType()) {
						case Complex:	
							// we found a child complex property so the current parent node is not a leaf as we assumed earlier
							//leaf = false;
							
							// two sub cases: either Single Occurrence or Multiple Occurrences Complex Type property
							if (childPropertyDefinition.isMultiple()) {
								// since definition indicates the property can have multiple occurrences we should retrieve the occurrences under this property path
								// and generate for each one a tree node of type ComplexTypeMultipleOccurrencesPropertyNode
								List<CmsProperty> childProperties = complexCmsProperty.getChildPropertyList(childPropertyDefinition.getName());
								int index = 0;
								for (CmsProperty childProperty : childProperties) {
									childProperty.setCurrentLocale(JSFUtilities.getLocaleAsString());
									addComplexTypeMultipleOccurrencesPropertyNode((ComplexCmsProperty)childProperty, (ComplexCmsPropertyDefinition)childPropertyDefinition, index);
									index++;
								}
							}
							else { // single occurrence 
								CmsProperty childProperty = complexCmsProperty.getChildProperty(childPropertyDefinition.getName());
								childProperty.setCurrentLocale(JSFUtilities.getLocaleAsString());
								addComplexTypeSingleOccurrencePropertyNode((ComplexCmsProperty)childProperty, (ComplexCmsPropertyDefinition)childPropertyDefinition);
							}
							break;
						case ContentType:
							//Do nothing. Log this as warning as normally
							//no cms property with value type as content type should exist
							logger.warn("Found Cms property "+ childPropertyDefinition.getFullPath() + " of value type "+ ValueType.ContentType);
							break;
						default:
							//All other cases are considered simple cms properties
							addSimpleCmsPropertyNode((SimpleCmsPropertyDefinition)childPropertyDefinition, PropertyPath.createFullPropertyPath(complexCmsProperty.getFullPath(), childPropertyDefinition.getName()));
							break;
						}
					
					}
				}
			}
		}


		return children.entrySet().iterator();
	}

	
	private void addSimpleCmsPropertyNode(SimpleCmsPropertyDefinition childPropertyDefinition, String fullPath) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingSimpleCmsPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingSimpleCmsPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					this,
					"SimpleTypePropertyNode",
					childPropertyDefinition.isMandatory()
					);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
		
	}


	public void addComplexTypeMultipleOccurrencesPropertyNode(ComplexCmsProperty childProperty,ComplexCmsPropertyDefinition childPropertyDefinition, int index) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName() + ":" + index,
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale) + "[" + index + "]",
					this,
					"ComplexTypeMultipleOccurrencesPropertyNode",
					false,
					childPropertyDefinition,
					childProperty);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	}
	
	public void addComplexTypeSingleOccurrencePropertyNode(ComplexCmsProperty childProperty, ComplexCmsPropertyDefinition childPropertyDefinition) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingComplexContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					this,
					"ComplexTypeSingleOccurrencePropertyNode",
					false,
					childPropertyDefinition,
					childProperty);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	}
	
	
	public ComplexCmsProperty getComplexCmsProperty() {
		return complexCmsProperty;
	}

	public LocalizableCmsDefinition getComplexCmsPropertyDefinition() {
		return complexCmsPropertyDefinition;
	}

	
}
