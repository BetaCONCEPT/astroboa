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
package org.betaconceptframework.astroboa.console.jsf.search;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsDefinitionTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsDefinitionTreeNodeRichFaces.Type;
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

@Name("cmsDefinitionTreeForContentObjectSearch")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsDefinitionTreeForContentObjectSearch extends AbstractUIBean{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TreeNode definitionRootNode;

	@In
	private LocaleSelector localeSelector;
	
	private DefinitionService definitionService;
	
	@Unwrap
	public TreeNode getDefinitionRootNode() {
		return definitionRootNode;
	}
	
	
	@Observer({SeamEventNames.NEW_CMS_DEFINITION_TREE_FOR_SEARCH})
	public void initializeCmsDefinitionTree(String contentObjectType) {
		
		definitionRootNode = 
			new LazyLoadingCmsDefinitionTreeNodeRichFaces(
					"0","RootPropertyNode",null, Type.NO_DEFINITION.toString(),null, null);

		//Build Content Object Type TreeNode
		ContentObjectTypeDefinition contentObjectTypeDefinition = null;
		if (StringUtils.isNotBlank(contentObjectType)){
		
			contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(contentObjectType);
			
			if (contentObjectTypeDefinition == null)
				logger.warn("Content Object Type Definition with name "+ contentObjectType + " was not found. CmsDefinitionTree will not be rendered");
			else{
				
				LazyLoadingCmsDefinitionTreeNodeRichFaces propertiesContainer =	
					new LazyLoadingCmsDefinitionTreeNodeRichFaces(
							contentObjectTypeDefinition.getName(),
							contentObjectTypeDefinition.getDisplayName().getLocalizedLabelForLocale(localeSelector.getLocaleString()),
							definitionRootNode,
							Type.NO_DEFINITION.toString(),
							contentObjectTypeDefinition, 
							contentObjectType);

				definitionRootNode.addChild(propertiesContainer.getIdentifier(), propertiesContainer);
				
			}
		}
		
		//Load all aspects
		if (definitionRootNode != null){
			LazyLoadingCmsDefinitionTreeNodeRichFaces aspectContainer = createAspectContainer();
			
			List<ComplexCmsPropertyDefinition> aspectDefinitions = definitionService.getAvailableAspectDefinitionsSortedByLocale(localeSelector.getLocaleString());
			for (ComplexCmsPropertyDefinition aspectDefinition : aspectDefinitions){
				//Add Aspect Definition only if content object type definition is null
				//or aspect is not defined in this
				if (! aspectDefinition.isSystemTypeDefinition() && 
						( contentObjectTypeDefinition == null || !contentObjectTypeDefinition.hasCmsPropertyDefinition(aspectDefinition.getName()) )
					){
					addAspectDefinitionToTree(aspectDefinition, aspectContainer);
				}
			}
		}
		
	}
	
	private void addAspectDefinitionToTree(
			ComplexCmsPropertyDefinition aspectDefinition, LazyLoadingCmsDefinitionTreeNodeRichFaces aspectContainer) {
		
		LazyLoadingCmsDefinitionTreeNodeRichFaces aspectTreeNode =	
			new LazyLoadingCmsDefinitionTreeNodeRichFaces(
					aspectDefinition.getName(),
					aspectDefinition.getDisplayName().getLocalizedLabelForLocale(localeSelector.getLocaleString()),
					aspectContainer,
					Type.COMPLEX_CMS_PROPERTY_DEFINITION.toString(),
					aspectDefinition,
					null);
		
		aspectContainer.addChild(aspectTreeNode.getIdentifier(), aspectTreeNode);
		
	}

	
	private LazyLoadingCmsDefinitionTreeNodeRichFaces createAspectContainer() {
			//Add root aspect
			LazyLoadingCmsDefinitionTreeNodeRichFaces aspectContainer = new LazyLoadingCmsDefinitionTreeNodeRichFaces(
					"aspectRootNode",
					JSFUtilities.getLocalizedMessage("content.object.aspects", null),
					definitionRootNode,
					Type.NO_DEFINITION.toString(),
					null, 
					null);
			
		
		definitionRootNode.addChild(aspectContainer.getIdentifier(), aspectContainer);
		
		return aspectContainer;
	}

	
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}
	
	

}
