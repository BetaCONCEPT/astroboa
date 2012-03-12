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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.jsf.edit.ComplexCmsPropertyEdit;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsDefinitionTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsDefinitionTreeNodeRichFaces.Type;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces.NodeType;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.model.TreeNode;

@Name("cmsDefinitionTree")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsDefinitionTree extends AbstractUIBean{
	
	private TreeNode definitionRootNode;
	
	@In
	private LocaleSelector localeSelector;
	
	private DefinitionService definitionService;
	private String contentType;

	private LazyLoadingCmsDefinitionTreeNodeRichFaces aspectContainer;

	private LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces basicContainer;

	@Unwrap
	public TreeNode getDefinitionRootNode() {
		return definitionRootNode;
	}
	
	@Observer({SeamEventNames.CMS_DEFINITION_TREE_REMOVE_ASPECT})
	public void removeAspectFromTree(String aspect) {
		if (StringUtils.isNotBlank(aspect) && aspectContainer != null){
			
			aspectContainer.removeChild(aspect);

		}
	}
	
	@Observer({SeamEventNames.CMS_DEFINITION_TREE_ADD_ASPECT})
	public void addAspectToTree(String aspect) {
		if (StringUtils.isNotBlank(aspect) && definitionRootNode != null){
			
			createAspectContainer();
			
			addAspect(aspect);
		}
	}

	@Observer({SeamEventNames.CMS_DEFINITION_TREE_CLEAR_ASPECTS})
	public void clearTreeFromAspects() {
		if (aspectContainer != null)
			aspectContainer.clearChildren();
	}
	
	@Observer({SeamEventNames.CMS_DEFINITION_TREE_ASPECT_UPDATE})
	public void updateTreeWithAspects(List<String> aspects) {
		
		if (definitionRootNode != null){
			
			createAspectContainer();
			
			if (CollectionUtils.isNotEmpty(aspects)){
				List<ComplexCmsPropertyDefinition> aspectDefinitions = definitionService.getAspectDefinitionsSortedByLocale(aspects, localeSelector.getLocaleString());
				for (ComplexCmsPropertyDefinition aspectDefinition : aspectDefinitions){
					addAspectDefinitionToTree(aspectDefinition);
				}
			}
			else
				aspectContainer.clearChildren();
		}
		
	}
	
	@Observer({SeamEventNames.NEW_COMPLEX_CMS_PROPERTY_ADDED})
	public void newCmsPropertyAdded(String parentTreeNodeIdentifier, ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		
		TreeNode parentTreeNode = null;
		
		if (basicContainer != null){
			
			parentTreeNode = basicContainer.getChild(parentTreeNodeIdentifier);
			
			if (parentTreeNode == null && aspectContainer != null)
				//Check aspects
				parentTreeNode = aspectContainer.getChild(parentTreeNodeIdentifier);
		}
		
		if (parentTreeNode != null)
			((LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces)parentTreeNode).clearChildren();
	}

	private void addAspect(String aspect) {
		ComplexCmsPropertyDefinition aspectDefinition = (ComplexCmsPropertyDefinition) definitionService.getCmsDefinition(aspect, ResourceRepresentationType.DEFINITION_INSTANCE,false);
		if (aspectDefinition == null)
			logger.warn("Could not find aspect definition with name "+ aspect);
		else{
			addAspectDefinitionToTree(aspectDefinition);
		}
	}

	private void addAspectDefinitionToTree(
			ComplexCmsPropertyDefinition aspectDefinition) {
		LazyLoadingCmsDefinitionTreeNodeRichFaces aspectTreeNode =	
			new LazyLoadingCmsDefinitionTreeNodeRichFaces(
					aspectDefinition.getName(),
					aspectDefinition.getDisplayName().getLocalizedLabelForLocale(localeSelector.getLocaleString()),
					aspectContainer,
					Type.COMPLEX_CMS_PROPERTY_DEFINITION.toString(),
					aspectDefinition,
					contentType
					);
		
		aspectContainer.addChild(aspectTreeNode.getIdentifier(), aspectTreeNode);
	}

	private void createAspectContainer() {
		if (aspectContainer == null){
			//Add root aspect
			aspectContainer =	
				new LazyLoadingCmsDefinitionTreeNodeRichFaces(
						"aspectRootNode",
						JSFUtilities.getLocalizedMessage("content.object.aspects", null),
						definitionRootNode,
						Type.NO_DEFINITION.toString(),
						null, 
						contentType);
			
			definitionRootNode.addChild(aspectContainer.getIdentifier(), aspectContainer);
		}
	}
	
	@Observer({SeamEventNames.NEW_CMS_PROPERTIES_TREE})
	public void initializeCmsDefinitionTree(ContentObject contentObject) {
		
		this.contentType = contentObject.getContentObjectType();
		
		definitionRootNode = 
			new LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(
					"0","RootPropertyNode",null, NodeType.LABEL_NODE.toString(),null, true);

		//Add root for basic properties
		basicContainer = new LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(
				"basicRootNode",
				JSFUtilities.getLocalizedMessage("content.object.basic.properties", null),
				definitionRootNode,
				NodeType.LABEL_NODE.toString(),
				null, 
				true);
		
		definitionRootNode.addChild(basicContainer.getIdentifier(), basicContainer);

		LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces propertiesContainer =
			new LazyLoadingCmsPropertyForContentObjectEditTreeNodeRichFaces(
					contentObject.getContentObjectType(),
					contentObject.getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(localeSelector.getLocaleString()),
					basicContainer,
					NodeType.COMPLEX_CMS_PROPERTY.toString(),
					contentObject.getComplexCmsRootProperty(), 
					true);

		basicContainer.addChild(propertiesContainer.getIdentifier(), propertiesContainer);
			
		//Build Content Object Type TreeNode
		/*if (StringUtils.isNotBlank(contentObjectType)){
			ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(contentObjectType);
			
			if (contentObjectTypeDefinition == null)
				logger.warn("Content Object Type Definition with name "+ contentObjectType + " was not found. CmsDefinitionTree will not be rendered");
			else{
				LazyLoadingCmsDefinitionTreeNodeRichFaces propertiesContainer =	
					new LazyLoadingCmsDefinitionTreeNodeRichFaces(
							contentObjectTypeDefinition.getName(),
							contentObjectTypeDefinition.getLocalizedLabelForLocale(localeSelector.getLocaleString()),
							basicContainer,
							Type.COMPLEX_CMS_PROPERTY_DEFINITION.toString(),
							contentObjectTypeDefinition, 
							contentObjectType);

				basicContainer.addChild(propertiesContainer.getIdentifier(), propertiesContainer);
			}
		}*/
		
		//Build Aspect Tree Nodes
		//updateTreeWithAspects(aspects);
	}
	

	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

}
