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
import java.util.Map.Entry;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingCmsDefinitionTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces{

	public enum Type{
		NO_DEFINITION,
		SIMPLE_CMS_PROPERTY_DEFINITION,
		COMPLEX_CMS_PROPERTY_DEFINITION
	}
	private LocalizableCmsDefinition cmsDefinition;

	private String definitionPathForQuery;
	private boolean mandatory;
	private boolean multiple;
	private String contentType;


	public LazyLoadingCmsDefinitionTreeNodeRichFaces(String identifier, String description, TreeNode parent, String type, LocalizableCmsDefinition cmsDefinition, String contentType){

		//If definition is an instance then it is a leaf node
		super(identifier, description, parent, 
				type, 
				(type == null || Type.SIMPLE_CMS_PROPERTY_DEFINITION.toString().equals(type)) //Leaf if definition is simple
		);


		this.cmsDefinition = cmsDefinition;
		this.contentType = contentType;

		getDefinitionPathForQuery();

		if (cmsDefinition != null && cmsDefinition instanceof CmsPropertyDefinition){
			multiple = ((CmsPropertyDefinition)cmsDefinition).isMultiple();
			mandatory = ((CmsPropertyDefinition)cmsDefinition).isMandatory();  
		}

		//Set default description in case no description is provided
		//Default value is cms definition name if any and an appropriate message in parenthesis
		if (StringUtils.isBlank(this.description))
			this.description = (cmsDefinition != null ? cmsDefinition.getName() : "" );
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0 && cmsDefinition != null) {

			// get the child property definitions and generate nodes for each complex property
			Collection<CmsPropertyDefinition> childPropertyDefinitions = null;

			if (cmsDefinition instanceof ContentObjectTypeDefinition)
				childPropertyDefinitions = ((ContentObjectTypeDefinition)cmsDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(JSFUtilities.getLocaleAsString()).values();
			else
				childPropertyDefinitions = ((ComplexCmsPropertyDefinition)cmsDefinition).getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(JSFUtilities.getLocaleAsString()).values();

			if (childPropertyDefinitions != null && !childPropertyDefinitions.isEmpty()) {
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions) {

					// we are looking for type properties excluding "profile" and "accessibility", "workflow"  which have a special treatment at the UI
					//if (!childPropertyDefinition.getName().equals("profile")  
					// && !childPropertyDefinition.getName().equals("accessibility") 
					// && !childPropertyDefinition.getName().equals("workflow")
					//		)
					//{
					String locale = JSFUtilities.getLocaleAsString();
					LazyLoadingCmsDefinitionTreeNodeRichFaces childPropertyTreeNode =
						new LazyLoadingCmsDefinitionTreeNodeRichFaces(
								PropertyPath.createFullPropertyPath(definitionPathForQuery, childPropertyDefinition.getName()),
								childPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
								this,
								getTypeFromDefinition(childPropertyDefinition),
								childPropertyDefinition, 
								contentType);

					children.put(childPropertyTreeNode.identifier, childPropertyTreeNode);
					//}
				}
			}
		}


		return children.entrySet().iterator();	

	}

	private String getTypeFromDefinition(
			CmsPropertyDefinition childPropertyDefinition) {
		if (childPropertyDefinition == null)
			return Type.NO_DEFINITION.toString();

		if (childPropertyDefinition instanceof ContentObjectTypeDefinition || childPropertyDefinition instanceof ComplexCmsPropertyDefinition)
			return Type.COMPLEX_CMS_PROPERTY_DEFINITION.toString();

		return Type.SIMPLE_CMS_PROPERTY_DEFINITION.toString();

	}

	public LocalizableCmsDefinition getCmsDefinition(){
		return cmsDefinition;



	}

	public void clearChildren() {
		children.clear();

	}

	public String getDefinitionPathForQuery() {

		if (definitionPathForQuery == null){
			if (cmsDefinition == null)
				definitionPathForQuery = null;
			else if (cmsDefinition instanceof ContentObjectTypeDefinition)
				definitionPathForQuery = cmsDefinition.getName();
			else if (cmsDefinition instanceof CmsPropertyDefinition){
				definitionPathForQuery = ((CmsPropertyDefinition)cmsDefinition).getPath();
				
				//If root definition is a global complex definition path for
				//query must be fullPath
				CmsDefinition parentDefinition = ((CmsPropertyDefinition)cmsDefinition).getParentDefinition();
				while (parentDefinition != null && !(parentDefinition instanceof ContentObjectTypeDefinition)){
					if ( parentDefinition instanceof CmsPropertyDefinition){
						parentDefinition = ((CmsPropertyDefinition)parentDefinition).getParentDefinition();
					}
					else{
						break;
					}
				}
				
				if (parentDefinition == null || parentDefinition instanceof ComplexCmsPropertyDefinition){
					definitionPathForQuery = ((CmsPropertyDefinition)cmsDefinition).getFullPath();
				}
			}
		}

		return definitionPathForQuery;
	}

	public boolean getDefinitionMultiple() {
		return multiple;

	}

	public boolean getDefinitionMandatory() {

		return mandatory;

	}



	public boolean hasParentMultiple() {
		if (getParent() != null){
			if (getParent() instanceof LazyLoadingCmsDefinitionTreeNodeRichFaces){
				LazyLoadingCmsDefinitionTreeNodeRichFaces parentDefinitionTreeNode = (LazyLoadingCmsDefinitionTreeNodeRichFaces)getParent();
				Boolean isParentMultiple = (parentDefinitionTreeNode).getDefinitionMultiple();
				if (BooleanUtils.isTrue(isParentMultiple))
					return true;

				return parentDefinitionTreeNode.hasParentMultiple();
			}

			return false;
		}

		return false;
	}

	public boolean isErroneous(){

		boolean existMessagesForControlId = JSFUtilities.existMessagesForControlId(PropertyPath.removeIndexesFromPath(definitionPathForQuery));
		if (!existMessagesForControlId && ! children.isEmpty()){

			Collection<TreeNode> childTreeNodes = children.values();
			for (TreeNode childNode : childTreeNodes){
				if (((LazyLoadingCmsDefinitionTreeNodeRichFaces)childNode).isErroneous())
					return true;
			}
		}
		return existMessagesForControlId;

	}
}
