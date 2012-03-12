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
package org.betaconceptframework.astroboa.console.jsf;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.commons.Utils;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingContentObjectPropertyTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.richfaces.component.html.HtmlModalPanel;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * Created on Sept 10, 2007
 */
@Name("contentObjectViewAsTree")
@Scope(ScopeType.CONVERSATION)
public class ContentObjectViewAsTree extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;
	
	// injected beans
	private ContentObjectList contentObjectList;
	private DefinitionService definitionService;
	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private PageController pageController;
	
	@In(create=true)
	private IdentityStoreRunAsSystem identityStoreRunAsSystem;
	
	@In(required=false)
	UIComponentBinding uiComponentBinding;
	
	@In
	private LocaleSelector localeSelector;
	
	// The JSF pop up panel which acts as the viewer of detailed content object properties
	private HtmlModalPanel contentObjectViewerPanel;
	
	private String contentObjectTitle;
	private TreeNode contentObjectAsTreeData;
	private BinaryChannel selectedBinaryChannel;
	private ContentObjectUIWrapper selectedContentObjectForView;
	
	public void presentContentObject_UIAction(String selectedContentObjectIdentifier) {
		ContentObject selectedContentObject = contentService.getContentObject(
				selectedContentObjectIdentifier, 
				ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
				FetchLevel.ENTITY, 
				CacheRegion.NONE, 
				null,
				false);

		selectedContentObjectForView = contentObjectUIWrapperFactory.getInstance(selectedContentObject);
		
		contentObjectTitle = ((StringProperty) selectedContentObjectForView.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue();
		
		// content object title is used as header for the contentObjectViewerPanel. So abbreviate to 100 chars to fit into one window line
		contentObjectTitle = StringUtils.abbreviate(contentObjectTitle, 100);
		
		// get root content object property which contains all properties
		ComplexCmsRootProperty rootProperty = selectedContentObjectForView.getContentObject().getComplexCmsRootProperty();
		
		ContentObjectTypeDefinition contentObjectTypeDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(selectedContentObjectForView.getContentObject().getContentObjectType(), ResourceRepresentationType.DEFINITION_INSTANCE,false);
		
		String locale = JSFUtilities.getLocaleAsString();
		
		contentObjectAsTreeData = 
			new LazyLoadingContentObjectPropertyTreeNodeRichFaces(
					rootProperty.getName(),
					contentObjectTypeDefinition.getDisplayName().getLocalizedLabelForLocale(locale),
					null,
					"ComplexTypeSingleOccurrencePropertyNode",
					false,
					rootProperty,
					contentObjectTypeDefinition);
	}
	
	
	/**
	 * The method creates an output stream for a byte array containing the data of a binary channel
	 * The output stream is used by a UI object (a4j:mediaOutput) to either render or create a link to the binary data 
	 * @param out
	 * @param data
	 * @throws IOException
	 */
	public void binaryDataOutput(OutputStream out, Object data) throws IOException{
		out.write((byte[]) data);
		//out.write(getThumbnailContent());
		out.close();
	}
	
	public void viewBinaryChannel_UIAction() {

		TreeNode selectedTreeNodeObject = uiComponentBinding.getContentObjectTreeComponent().getTreeNode();
		LazyLoadingContentObjectPropertyTreeNodeRichFaces selectedPropertyNode;
		// There is possibly a bug in Rich Faces Tree Implementation. The first time a tree node is selected in the tree the selected tree node
		// that is returned has type <LazyLoadingContentObjectFolderTreeNodeRichFaces> as it should be. However any subsequent selected tree node
		// is returned as a <TreeNodeImpl> object which should not happen. 
		// So we should check the returned type to decide how we will access the selected tree node.
		if (LazyLoadingContentObjectPropertyTreeNodeRichFaces.class.isInstance(selectedTreeNodeObject)) {
			selectedPropertyNode = (LazyLoadingContentObjectPropertyTreeNodeRichFaces) selectedTreeNodeObject;
		}
		else if (TreeNodeImpl.class.isInstance(selectedTreeNodeObject)) {
			selectedPropertyNode =
				(LazyLoadingContentObjectPropertyTreeNodeRichFaces) selectedTreeNodeObject.getData();
		}
		else 
			throw new RuntimeException("Cannot determine the class of the selected tree node");
		
		selectedBinaryChannel = selectedPropertyNode.getBinaryChannel();
		//pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.BINARY_CHANNEL_VIEWER.getDynamicUIAreaPageComponent());
		
	}


	public String getLocalizedLabelForLocale(String localeName){
		try{
			if (StringUtils.isNotBlank(localeName)){
				//In case locale is the same with localeSelector
				if (localeName.equals(localeSelector.getLocaleString()))
						return localeSelector.getLocale().getDisplayName();
				else{
					//Try to find its name from supported locales
					 Iterator<Locale> locales = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
				      while ( locales.hasNext() )
				      {
				         Locale locale = locales.next();
				         if (localeName.equals(locale.toString()))
				        	 return locale.getDisplayName();
				      }
				}
			}
			
			return localeName;
		}
		catch(Exception e)
		{
			return localeName;
		}
	}
	
	public String processAccessRight(String personOrRoleAccessRight){
		return Utils.retrieveDisplayNameForRoleOrPerson(identityStoreRunAsSystem, personOrRoleAccessRight);
	}
	
	public String getContentObjectTitle() {
		return contentObjectTitle;
	}


	public HtmlModalPanel getContentObjectViewerPanel() {
		return contentObjectViewerPanel;
	}


	public void setContentObjectViewerPanel(HtmlModalPanel contentObjectViewerPanel) {
		this.contentObjectViewerPanel = contentObjectViewerPanel;
	}


	public TreeNode getContentObjectAsTreeData() {
		return contentObjectAsTreeData;
	}


	public ContentObjectUIWrapper getSelectedContentObjectForView() {
		return selectedContentObjectForView;
	}


	public BinaryChannel getSelectedBinaryChannel() {
		return selectedBinaryChannel;
	}


	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}

	
	public void setContentObjectList(
			ContentObjectList contentObjectList) {
		this.contentObjectList = contentObjectList;
	}
	
	
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}


	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}
	
}
