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
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * A simplified version of {@link LazyLoadingContentObjectPropertyTreeNodeRichFaces} specific
 * to content object edit
 * 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces{

	public enum NodeType{
		LABEL_NODE,
		CMS_PROPERTY_TEMPLATE,
		COMPLEX_CMS_PROPERTY
	}

	private CmsProperty cmsProperty;
	private boolean mandatory;
	private String propertyNameForTemplate;

	public LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, 
			CmsProperty cmsProperty, boolean mandatory){

		//If definition is an instance then it is a leaf node
		super(identifier, description, parent, 
				type, 
				(type == null 
						|| NodeType.CMS_PROPERTY_TEMPLATE.toString().equals(type)) //Leaf if definition is simple
		);


		this.cmsProperty = cmsProperty;
		this.mandatory = mandatory;

		//Set default description in case no description is provided
		//Default value is cms definition name if any and an appropriate message in parenthesis
		if (StringUtils.isBlank(this.description))
			this.description = (cmsProperty != null ? cmsProperty.getName() : "" );
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0 && cmsProperty != null &&
				cmsProperty instanceof ComplexCmsProperty) {

			final ComplexCmsProperty complexCmsProperty = (ComplexCmsProperty)cmsProperty;

			// get the child property definitions and generate nodes for each complex property
			Collection<CmsPropertyDefinition> childPropertyDefinitions = 
				complexCmsProperty.getPropertyDefinition().getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(JSFUtilities.getLocaleAsString()).values();

			if (childPropertyDefinitions != null && !childPropertyDefinitions.isEmpty()) {
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions) {

					// we are looking for type properties excluding "profile" and "accessibility", "workflow"  which have a special treatment at the UI
					if (!childPropertyDefinition.getName().equals("profile") 
							&& ! childPropertyDefinition.isObsolete()
							// && !childPropertyDefinition.getName().equals("accessibility") 
							// && !childPropertyDefinition.getName().equals("workflow")
					)
					{
						String locale = JSFUtilities.getLocaleAsString();
						String localizedLabelForLocale = childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale);
						if (localizedLabelForLocale == null)
							localizedLabelForLocale = childPropertyDefinition.getName();

						if (! (childPropertyDefinition instanceof SimpleCmsPropertyDefinition)){
							//Child Property is a Complex property

							if (childPropertyDefinition.isMultiple()){
								//Create a dummy node to be used for creating a new property template
								//for this property
								LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces childPropertyTreeNode =
									new LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(
											PropertyPath.createFullPropertyPath(cmsProperty.getFullPath(), childPropertyDefinition.getName()+":New"),
											localizedLabelForLocale,
											this,
											NodeType.CMS_PROPERTY_TEMPLATE.toString(),
											null, 
											false);
								
								childPropertyTreeNode.setPropertyNameForTemplate(childPropertyDefinition.getName());

								children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
							}

							//Load all children under this path
							List<CmsProperty> childProperties = complexCmsProperty.getChildPropertyList(childPropertyDefinition.getName());

							if (CollectionUtils.isNotEmpty(childProperties)){

								for (CmsProperty childProperty: childProperties){
									LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces childPropertyTreeNode =
										new LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(
												childProperty.getFullPath(),
												localizedLabelForLocale + extractIndexFromFullPath(childProperty.getFullPath()),
												this,
												NodeType.COMPLEX_CMS_PROPERTY.toString(),
												childProperty, 
												childPropertyDefinition.isMandatory());

									children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
								}
							}
						}
					}
				}
			}

		}
			return children.entrySet().iterator();	

		}

		private String extractIndexFromFullPath(String fullPath) {
			if (StringUtils.isBlank(fullPath))
				return "";
			
			String index = StringUtils.substringAfterLast(fullPath, CmsConstants.LEFT_BRACKET);
			
			if (StringUtils.isBlank(index) || index.startsWith("0"))
				return "";
			
			return CmsConstants.LEFT_BRACKET+index;
	}

		public void clearChildren() {
			children.clear();
		}

		public boolean getMandatory() {
			return mandatory;
		}


		public boolean isErroneous(){

			if (cmsProperty == null)
				return false;
			
			boolean existMessagesForControlId = JSFUtilities.existMessagesForControlId(PropertyPath.removeIndexesFromPath(cmsProperty.getFullPath()));
			if (!existMessagesForControlId && ! children.isEmpty()){

				Collection<TreeNode> childTreeNodes = children.values();
				for (TreeNode childNode : childTreeNodes){
					if (((LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces)childNode).isErroneous())
						return true;
				}
			}
			return existMessagesForControlId;

		}

		public CmsProperty getCmsProperty() {
			return cmsProperty;
		}

		public String getPropertyNameForTemplate() {
			return propertyNameForTemplate;
		}

		public void setPropertyNameForTemplate(String propertyNameForTemplate) {
			this.propertyNameForTemplate = propertyNameForTemplate;
		}
		
		
		
	}
