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
package org.betaconceptframework.astroboa.console.jsf.richfaces;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * Created on Sept 10, 2007
 */
public class LazyLoadingContentObjectPropertyTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private CmsProperty contentObjectProperty;
	private LocalizableCmsDefinition contentObjectPropertyDefinition;
	private BinaryChannel binaryChannel;
	private Object contentObjectPropertyValue;
	
	public LazyLoadingContentObjectPropertyTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean leaf, 
			CmsProperty contentObjectProperty, LocalizableCmsDefinition contentObjectPropertyDefinition) {
		super(identifier, description, parent, type, leaf);
		this.contentObjectProperty = contentObjectProperty;
		this.contentObjectPropertyDefinition = contentObjectPropertyDefinition;
	}
	
	public LazyLoadingContentObjectPropertyTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean leaf, 
			CmsProperty contentObjectProperty, LocalizableCmsDefinition contentObjectPropertyDefinition, BinaryChannel binaryChannel) {
		super(identifier, description, parent, type, leaf);
		this.contentObjectProperty = contentObjectProperty;
		this.contentObjectPropertyDefinition = contentObjectPropertyDefinition;
		this.binaryChannel = binaryChannel;
	}
	
	public LazyLoadingContentObjectPropertyTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, boolean leaf, 
			CmsProperty contentObjectProperty, LocalizableCmsDefinition contentObjectPropertyDefinition, Object contentObjectPropertyValue) {
		super(identifier, description, parent, type, leaf);
		this.contentObjectProperty = contentObjectProperty;
		this.contentObjectPropertyDefinition = contentObjectPropertyDefinition;
		this.contentObjectPropertyValue = contentObjectPropertyValue;
	}

	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retreive children of node: " + identifier);
			
			// Five cases of tree node types which are not leaves (i.e. have children): 
			// - RootPropertyNode
			// - ComplexTypeMultiValuePropertyNode
			// - ComplexTypeSingleValuePropertyNode
			// - SimpleTypeMultiValuePropertyNode
			// - BinaryChannelMultiValuePropertyNode
			
			
			if ("ComplexTypeSingleOccurrencePropertyNode".equals(type) || "ComplexTypeMultipleOccurrencesPropertyNode".equals(type) || "RootPropertyNode".equals(type)){
				
				// get the child property definitions and generate nodes for each property
				Collection<CmsPropertyDefinition> childPropertyDefinitions;
				
				if (contentObjectPropertyDefinition instanceof ContentObjectTypeDefinition)
					childPropertyDefinitions = ((ContentObjectTypeDefinition)contentObjectPropertyDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(JSFUtilities.getLocaleAsString()).values();
				else
					childPropertyDefinitions = ((ComplexCmsPropertyDefinition)contentObjectPropertyDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(JSFUtilities.getLocaleAsString()).values();
				
				
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions) {
					// profile and accessibility properties have a special treatment while viewing them so we exclude them from the tree.
					if ("profile".equals(childPropertyDefinition.getName()) || "accessibility".equals(childPropertyDefinition.getName()))
						continue;
					// two cases: either simple type or complex type property. Aspect is considered a complex property as well
					if (childPropertyDefinition.getValueType().equals(ValueType.Complex)) {
						// two sub cases: either Single Occurrence or Multiple Occurrences Complex Type property
						if (childPropertyDefinition.isMultiple()) {
							// since definition indicates the property can have multiple occurrences we should retrieve the occurrences under this property path
							// and generate for each one a tree node of type ComplexTypeMultipleOccurrencesPropertyNode
							List<CmsProperty> childProperties = ((ComplexCmsProperty)contentObjectProperty).getChildPropertyList(childPropertyDefinition.getName());
							if (CollectionUtils.isNotEmpty(childProperties)){
								int index = 0;
								for (CmsProperty childProperty : childProperties) {
									addComplexTypeMultipleOccurrencesPropertyNode((ComplexCmsProperty)childProperty, (ComplexCmsPropertyDefinition)childPropertyDefinition, index);
									index++;
								}
							}
						}
						else { // single occurrence 
							CmsProperty childProperty = ((ComplexCmsProperty)contentObjectProperty).getChildProperty(childPropertyDefinition.getName());
							addComplexTypeSingleOccurrencePropertyNode((ComplexCmsProperty)childProperty, (ComplexCmsPropertyDefinition)childPropertyDefinition);
						}
					}
					// A Binary type property is a Simple Type property but we retrieve the values differently. So we consider it another special case
					else if (childPropertyDefinition.getValueType().equals(ValueType.Binary)) {
						CmsProperty childProperty = ((ComplexCmsProperty)contentObjectProperty).getChildProperty(childPropertyDefinition.getName());
						// two sub cases: either Single Value or Multiple values Binary Type property
						if (childPropertyDefinition.isMultiple()) {
							addChildPropertyNode(childProperty, childPropertyDefinition, "BinaryTypeMultiValuePropertyNode", false);
						}
						else {
							addBinaryTypeSingleValuePropertyNode((BinaryProperty)childProperty, childPropertyDefinition);
						}
					}
					else { //simple type property
						CmsProperty childProperty = ((ComplexCmsProperty)contentObjectProperty).getChildProperty(childPropertyDefinition.getName());
						// two sub cases: either Single Value or Multiple Value Simple Type property
						if (childPropertyDefinition.isMultiple())
							addChildPropertyNode(childProperty, childPropertyDefinition, "SimpleTypeMultiValuePropertyNode", false);
						else {
							addSimpleTypeSingleValuePropertyNode((SimpleCmsProperty)childProperty, childPropertyDefinition);
						}
					}
				}
			}
			else if ("SimpleTypeMultiValuePropertyNode".equals(type)) {
				// get property values and generate a OneOfMultipleValues node for each one 
				List<Object> propertyValues = ((SimpleCmsProperty)contentObjectProperty).getSimpleTypeValues();
				if (propertyValues.isEmpty())
					leaf = true;
				else {
					int valueListIndex = 0;
					for (Object propertyValue : propertyValues) {
						addSimplePropertyValueNode((SimpleCmsProperty)contentObjectProperty, (SimpleCmsPropertyDefinition)contentObjectPropertyDefinition, propertyValue, ((CmsPropertyDefinition)contentObjectPropertyDefinition).getValueType(), valueListIndex);
						valueListIndex++;
					}
				}
			}
			else if ("BinaryTypeMultiValuePropertyNode".equals(type)) { // A Binary Type property is a Simple Type property but we retrieve the values differently since its values are Binary Channels. So we consider it another special case
				// get Binary Channels and generate a OneOfMultipleBinaryChannels node  for each one
				List<BinaryChannel> binaryChannels = ((BinaryProperty)contentObjectProperty).getSimpleTypeValues();
				if (binaryChannels.isEmpty())
					leaf = true;
				else {
					int binaryChannelIndex = 0;
					for (BinaryChannel binaryChannel : binaryChannels) {
						addBinaryChannelNode(binaryChannel, binaryChannelIndex);
						binaryChannelIndex++;
					}
				}
			}
		}
				
		return children.entrySet().iterator();
	}

	
	public void addComplexTypeMultipleOccurrencesPropertyNode(ComplexCmsProperty childProperty, ComplexCmsPropertyDefinition childPropertyDefinition, int index) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName() + ":" + index,
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale) + "[" + index + "]",
					this,
					"ComplexTypeMultipleOccurrencesPropertyNode",
					false,
					childProperty,
					childPropertyDefinition);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	}
	
	public void addComplexTypeSingleOccurrencePropertyNode(ComplexCmsProperty childProperty, ComplexCmsPropertyDefinition childPropertyDefinition) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					this,
					"ComplexTypeSingleOccurrencePropertyNode",
					false,
					childProperty,
					childPropertyDefinition);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	}
	
	
	public void addChildPropertyNode(CmsProperty childProperty, CmsPropertyDefinition childPropertyDefinition, String nodeType, boolean leaf) {
		String locale = JSFUtilities.getLocaleAsString();
		LazyLoadingContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					this,
					nodeType,
					leaf,
					childProperty,
					childPropertyDefinition);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	}
	
	public void addSimplePropertyValueNode(SimpleCmsProperty childProperty, SimpleCmsPropertyDefinition childPropertyDefinition, Object propertyValue, ValueType valueType, int valueListIndex) {
		String nodeType;
		// some value types require special handling and thus we introduce a different type of node appending the value type to the default node type
		switch (valueType) {
		case Date:
		case Topic:
		case RepositoryUser:
			nodeType = "OneOfMultipleValuesNode" + ":" + valueType.toString();
			break;
			
		default:
			nodeType = "OneOfMultipleValuesNode";
			break;
		}
		LazyLoadingContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + "[" + valueListIndex +"]",
					"[" + valueListIndex +"]" + valueType,
					this,
					nodeType,
					true,
					childProperty,
					childPropertyDefinition,
					propertyValue);
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	
	}
	
	public void addSimpleTypeSingleValuePropertyNode(SimpleCmsProperty childProperty, CmsPropertyDefinition childPropertyDefinition) {
		String locale = JSFUtilities.getLocaleAsString();
		String nodeType;
		// some value types require special handling and thus we introduce a different type of node appending the value type to the default node type
		switch (childPropertyDefinition.getValueType()) {
		case Date:
		case Topic:
		case RepositoryUser:
			nodeType = "SimpleTypeSingleValuePropertyNode" + ":" + childPropertyDefinition.getValueType().toString();
			break;
			
		case String:
			if ("PlainText".equals(((StringPropertyDefinition)childPropertyDefinition).getStringFormat().toString()))
					nodeType = "SimpleTypeSingleValuePropertyNode" + ":" + ((StringPropertyDefinition)childPropertyDefinition).getStringFormat();
			else if ("RichText".equals(((StringPropertyDefinition)childPropertyDefinition).getStringFormat().toString()))
				nodeType = "SimpleTypeSingleValuePropertyNode" + ":" + ((StringPropertyDefinition)childPropertyDefinition).getStringFormat();
			else 
				nodeType = "SimpleTypeSingleValuePropertyNode" + ":" + "PlainText";
			break;
		default:
			nodeType = "SimpleTypeSingleValuePropertyNode";
			break;
		}
		
		LazyLoadingContentObjectPropertyTreeNodeRichFaces childPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					this,
					nodeType,
					true,
					childProperty,
					childPropertyDefinition,
					childProperty.getSimpleTypeValue());
		
		children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
	
	}
	
	
	public void addBinaryTypeSingleValuePropertyNode(BinaryProperty childProperty, CmsPropertyDefinition childPropertyDefinition) {
		
		LazyLoadingContentObjectPropertyTreeNodeRichFaces binaryPropertyTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + ":" + childPropertyDefinition.getName(),
					childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()),
					this,
					"BinaryTypeSingleValuePropertyNode",
					true,
					childProperty,
					childPropertyDefinition,
					childProperty.getSimpleTypeValue());
		
		children.put(binaryPropertyTreeNode.identifier, binaryPropertyTreeNode);
	}
	
	public void addBinaryChannelNode(BinaryChannel binaryChannel, int binaryChannelIndex) {
		
		LazyLoadingContentObjectPropertyTreeNodeRichFaces binaryChannelTreeNode =
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					identifier + "[" + binaryChannelIndex +"]",
					"[" + binaryChannelIndex +"]",
					this,
					"OneOfMultipleBinaryChannelsNode",
					true,
					null,
					null,
					binaryChannel);
		
		children.put(binaryChannelTreeNode.identifier, binaryChannelTreeNode);
	}
	
	
	
	public CmsProperty getContentObjectProperty() {
		return contentObjectProperty;
	}

	public BinaryChannel getBinaryChannel() {
		return binaryChannel;
	}

	public LocalizableCmsDefinition getContentObjectPropertyDefinition() {
		return contentObjectPropertyDefinition;
	}

	public Object getContentObjectPropertyValue() {
		
		return contentObjectPropertyValue;
	}
	

}
