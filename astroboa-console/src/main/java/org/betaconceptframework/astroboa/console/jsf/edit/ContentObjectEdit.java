/**
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
package org.betaconceptframework.astroboa.console.jsf.edit;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsConcurrentModificationException;
import org.betaconceptframework.astroboa.api.model.exception.CmsNonUniqueContentObjectSystemNameException;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.commons.Utils;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.LoggedInRepositoryUserSettings;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.jsf.clipboard.Clipboard;
import org.betaconceptframework.astroboa.console.jsf.edit.draft.DraftItem;
import org.betaconceptframework.astroboa.console.jsf.space.UserSpaceNavigation;
import org.betaconceptframework.astroboa.console.jsf.taxonomy.LazyLoadingTopicTreeNode;
import org.betaconceptframework.astroboa.console.jsf.visitor.CmsPropertyValidatorVisitor;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsConstants.ContentObjectStatus;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.ImageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;
import org.richfaces.event.DropEvent;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.web.context.support.ServletContextResource;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Name("contentObjectEdit")
@Scope(ScopeType.CONVERSATION)
public class ContentObjectEdit extends AbstractUIBean {

	private static final long serialVersionUID = 1L;

	private static final String CLEAR_CONTENT_OBJECT_EDIT_AND_LOAD_AGAIN_XHTML = "/clearContentObjectEditAndLoadAgain.xhtml"; 

	// injected beans
	private ContentObjectList contentObjectList;
	private DefinitionService definitionService;
	private ContentService contentService;
	private SpaceService spaceService;
	private UserSpaceNavigation userSpaceNavigation;
	private CMSUtilities cmsUtilities;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private LoggedInRepositoryUser loggedInRepositoryUser;

	private PageController pageController;
	private CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor;
	private TopicService topicService;

	@In(create=true)
	private IdentityStoreRunAsSystem identityStoreRunAsSystem;

	@In(create=true)
	private LoggedInRepositoryUserSettings loggedInRepositoryUserSettings;

	@In(required=false)
	UIComponentBinding uiComponentBinding;

	@In
	private LocaleSelector  localeSelector;
	
	@In(create=true)
	private LanguageSelector  languageSelector;
	
	private String contentObjectTitle;
	private String contentObjectTypeLocalizedName;

	// if an error occurs when saving a new content object there is a possibility that it has been assigned an id despite the error during saving.
	//therefore the existence of an id is not a safe criterion to find out if a content object is new and we explicitly keep a boolean variable for this.
	private boolean newContentObject;

	private String contentObjectTypeForNewObject;

	//private TreeNode contentObjectAsTreeData;

	private String selectedContentObjectIdentifier;
	private ContentObjectUIWrapper selectedContentObjectForEdit;

	private final Integer DO_NOT_ATTACH_TO_SPACE = 1;
	private final Integer ATTACH_TO_MY_ROOT_SPACE = 2;
	private final Integer ATTACH_TO_SELECTED_SPACE = 3;

	public enum AccessRight{
		canBeReadBy, 
		canBeUpdatedBy, 
		canBeDeletedBy, 
		canBeTaggedBy,
	}

	private String selectedUserOrGroupIdToBeRemovedFromAccessRightList;

	private Space spaceToCopyNewObject;

	private String selectedTopicIdForRemovalFromContentObjectSubject;
	private String selectedLoggedInUserTagId;
	
	private List<SelectItem> selectedLanguages;

	@In(create=true)
	private ComplexCmsPropertyEdit complexCmsPropertyEdit;

	private String newTagLabel;

	/* The following four properties hold the security setting that the user has selected
	 * (through radio buttons) for each of the four types of access rights defined for content objects (read, update, delete, tag).
	 * The are 3 possible security settings for each type of access right: ALL(1), NONE(0), ONLY_THE_SPECIFIED_GROUPS_AND_USERS(3)
	 * 
	 */
	//private Integer radioButtonSelectionForCanBeReadBy;
	//private Integer radioButtonSelectionForCanBeUpdatedBy;
	//private Integer radioButtonSelectionForCanBeDeletedBy;
	//private Integer radioButtonSelectionForCanBeTaggedBy;
	
	
	//Used by accessibility-tab.xhtml and is a replacement of the above individual 
	// Integer values. This map contains four entries
	//one for each access list. Since JSF and EL parser does not handle map paremters whose key is an
	//enum, map's key is an enum but take values from the AccessRight enum
	private Map<String, Integer> radioButtonSelectionMap = new HashMap<String, Integer>();

	/* If the user selects the security setting: ONLY_THE_SPECIFIED_GROUPS_AND_USERS(3), then
	 * for the 4 types of access rights we use the following 4 lists of HashMaps to hold for each type of access rights 
	 * the corresponding set of user groups and individual users that are granted with the specific access right.
	 * The HashMaps have 2 keys: "userOrGroupId" and  "userOrGroupName" and their respective values hold the group or user id (the external id in a crm or DB which holds info about users and groups) and the
	 * group or user name which is used by the UI to present meaningful names instead of ids
	 */
	//private List<Map<String,String>> canBeReadByUserList = new ArrayList<Map<String,String>>();
	//private List<Map<String,String>> canBeUpdatedByUserList = new ArrayList<Map<String,String>>();
	//private List<Map<String,String>> canBeDeletedByUserList = new ArrayList<Map<String,String>>();
	//private List<Map<String,String>> canBeTaggedByUserList = new ArrayList<Map<String,String>>();
	
	//Group above variables to a Map by AccessRight
	//oSince JSF and EL parser does not handle map paremters whose key is an
	//enum, map's key is an enum but take values from the AccessRight enum
	private Map<String, List<Map<String,String>>> userListByAccessRight = new HashMap<String, List<Map<String,String>>>();
	
	
	private ConfigurableMimeFileTypeMap mimeTypesMap;

	private List<String> listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated = new ArrayList<String>();

	//Corresponds to input text for adding new creator to a content object
	private String newCreatorName;

	private String newLanguageName;
	
	private List<ComplexCmsPropertyBreadCrumb> complexCmsPropertyBreadCrumbs = new ArrayList<ComplexCmsPropertyBreadCrumb>();

	private final static String defaultComplexCmsPropertyEditorPagePath = "/WEB-INF/pageComponents/edit/editorForCmsPropertiesOfAComplexCmsProperty.xhtml"; 

	private String complexCmsPropertyEditorPagePath = defaultComplexCmsPropertyEditorPagePath;

	private ContentObject explicitlySetContentObjectForEdit;
	
	private String selectedPersonOrRoleTextFilter;
	
	@In(create=true)
	private Clipboard clipboard;
	
	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;

	protected String editContentObjectFromDraft_UIAction(ContentObject contentObject)
	{
		if (contentObject == null)
		{
			getLogger().error("Error while loading content object from draft. Content Object is null"); 
			JSFUtilities.addMessage(null, "content.object.edit.load.error", null, FacesMessage.SEVERITY_ERROR); 
			return null;
		}
		
		if (contentObject.getId() != null)
		{
			setSelectedContentObjectIdentifier(contentObject.getId());
			explicitlySetContentObjectForEdit(contentObject);

			return editContentObjectAndDisplayDefaultPage_UIAction();
		}
		else
		{
			//Content object without id
			setSelectedContentObjectIdentifier(null);
			explicitlySetContentObjectForEdit(contentObject);

			//We do not want to create new content
			newContentObject = true;
			contentObjectTypeForNewObject = null;
			selectedContentObjectForEdit = contentObjectUIWrapperFactory.getInstance(contentObject);
			contentObjectTypeLocalizedName = selectedContentObjectForEdit.getContentObject().getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
			contentObjectTitle = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue(); 
			
			/* initialize the security input components (i.e. radio buttons which allow the user to select if ALL, NONE or SELECTED GROUPS/USERS have a specific access right) 
			 * Also initialize the lists which are used by the UI to present the groups/users granted with each type of access right (i.e. read / update / delete / tag).
			 * The access right lists in the content object hold only user / group ids which are not very meaningful to the end user.
			 */
			initializeInputAndOutputUIComponentsRelatedtoSecurityProperties();

			return initializeContentObjectEdit();
		}
	}
	
	
	protected String editContentObject_UIAction() {
		explicitlySetContentObjectForEdit = null;
		return editContentObjectAndDisplayDefaultPage_UIAction();
	}
	
	protected String editContentObjectAndDisplayDefaultPage_UIAction() {

		if (getSelectedContentObjectIdentifier() != null) {
			try {
				ContentObject selectedContentObject = getSelectedContentObject();

				selectedContentObjectForEdit = contentObjectUIWrapperFactory.getInstance(selectedContentObject);
				contentObjectTypeLocalizedName = selectedContentObjectForEdit.getContentObject().getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
				contentObjectTitle = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue(); 
				
				/* initialize the security input components (i.e. radio buttons which allow the user to select if ALL, NONE or SELECTED GROUPS/USERS have a specific access right) 
				 * Also initialize the lists which are used by the UI to present the groups/users granted with each type of access right (i.e. read / update / delete / tag).
				 * The access right lists in the content object hold only user / group ids which are not very meaningful to the end user. 
				 */
				initializeInputAndOutputUIComponentsRelatedtoSecurityProperties();
				
			}
			catch (Exception e) {
				getLogger().error("Error while loading content objects ",  e); 
				JSFUtilities.addMessage(null, "content.object.edit.load.error", null, FacesMessage.SEVERITY_ERROR); 
				//pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.ContentObjectList.getDynamicUIAreaPageComponent());
				return null;
			}
		}
		else { // it is a new content object. Lets initialize it
			try {
				initializeNewContentObject();
			}
			catch (Exception e) {
				getLogger().error("Error while generating template for new content object ", e); 
				JSFUtilities.addMessage(null, "content.object.edit.error.creating.new.content.object", null, FacesMessage.SEVERITY_ERROR); 
				//pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.ContentObjectList.getDynamicUIAreaPageComponent());
				return null;
			}
		}

		//generateCmsPropertiesTree();

		return initializeContentObjectEdit();

	}


	private String initializeContentObjectEdit() {
		complexCmsPropertyEdit.setEditedContentObject(selectedContentObjectForEdit.getContentObject());

		loadComplexCmsPropertyToComplexCmsPropertyEditor(selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty());


		listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated.clear();

		if (cmsPropertyValidatorVisitor == null){
			cmsPropertyValidatorVisitor = new CmsPropertyValidatorVisitor(definitionService);
		}
		else{
			cmsPropertyValidatorVisitor.reset(true);
		}

		complexCmsPropertyEdit.setCmsPropertyValidatorVisitor(cmsPropertyValidatorVisitor);
		
		return null;
	}


	private ContentObject getSelectedContentObject() {
		if (explicitlySetContentObjectForEdit != null){
			return explicitlySetContentObjectForEdit;
		}
		else{
			return (ContentObject) contentService.getContentObjectByIdAndLocale(selectedContentObjectIdentifier, JSFUtilities.getLocaleAsString(), null);
		}
	}


	private void createComplexCmsPropertyToBreadCrumd(
			ComplexCmsProperty complexCmsProperty) {

		if (complexCmsPropertyBreadCrumbs != null)
			complexCmsPropertyBreadCrumbs.clear();
		else
			complexCmsPropertyBreadCrumbs = new ArrayList<ComplexCmsPropertyBreadCrumb>();

		ComplexCmsProperty currentProperty = complexCmsProperty;

		do{
			String localizedLabel = (currentProperty instanceof ComplexCmsRootProperty ? 
					selectedContentObjectForEdit.getContentObject().getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(localeSelector.getLocaleString()) :
						currentProperty.getLocalizedLabelForLocale(localeSelector.getLocaleString()));

			if (StringUtils.isBlank(localizedLabel))
				localizedLabel = currentProperty.getName(); 

			//Add index to label
			if( !(currentProperty instanceof ComplexCmsRootProperty) && currentProperty instanceof ComplexCmsProperty 
					&& currentProperty.getPropertyDefinition().isMultiple()){
				int index = PropertyPath.extractIndexFromPath(currentProperty.getPath());
				if (index == PropertyPath.NO_INDEX)
					index =0;
				localizedLabel += CmsConstants.LEFT_BRACKET+index+
				CmsConstants.RIGHT_BRACKET;
			}

			if (currentProperty instanceof ComplexCmsProperty){
				String propertyLabel =  ((ComplexCmsProperty)currentProperty).getPropertyLabel(localeSelector.getLocaleString());
				
				if (StringUtils.isNotBlank(propertyLabel)){
					localizedLabel += " - "+propertyLabel;
				}
				
			}
			
			if (currentProperty instanceof ComplexCmsRootProperty){
				//Root property does not have a path
				complexCmsPropertyBreadCrumbs.add(new ComplexCmsPropertyBreadCrumb(localizedLabel,selectedContentObjectForEdit.getContentObject().getTypeDefinition().getName()));
			}
			else{
				complexCmsPropertyBreadCrumbs.add(new ComplexCmsPropertyBreadCrumb(localizedLabel,currentProperty.getPath()));
			}

			currentProperty = currentProperty.getParentProperty();

		}
		while( currentProperty != null);

		if (complexCmsPropertyBreadCrumbs.size()> 1)
			Collections.reverse(complexCmsPropertyBreadCrumbs);

	}




	//private void generateCmsPropertiesTree() {

	/*List<String> aspects = selectedContentObjectForEdit.getContentObject().getAspects();

		Events.instance().raiseEvent(SeamEventNames.NEW_CMS_DEFINITION_TREE, 
				selectedContentObjectForEdit.getContentObject().getContentObjectType(), 
				aspects);

		if (CollectionUtils.isNotEmpty(aspects)){
			selectedAspects = new String[aspects.size()];
			selectedContentObjectForEdit.getContentObject().getAspects().toArray(selectedAspects);
		}*/

	/*Events.instance().raiseEvent(SeamEventNames.NEW_CMS_PROPERTIES_TREE, 
				selectedContentObjectForEdit.getContentObject());

		List<String> aspects = selectedContentObjectForEdit.getContentObject().getAspects();
		if (CollectionUtils.isNotEmpty(aspects)){
			selectedAspects = new String[aspects.size()];
			selectedContentObjectForEdit.getContentObject().getAspects().toArray(selectedAspects);
		}

	}	*/


	private void initializeNewContentObject() throws Exception{
		newContentObject = true;
		ContentObject contentObject;

		// get a content object template for the user selected content type. Profile and Accessibility properties are also initialized
		if (contentObjectTypeForNewObject == null) {
			//Check in pageController
			contentObjectTypeForNewObject = pageController.getContentObjectTypeToUseWhenLoadingClearedContentObjectEditForm();

			//Found no content object type found throw new Exception
			if (contentObjectTypeForNewObject == null){
				newContentObject = false;
				JSFUtilities.addMessage(null, "content.object.edit.error.no.content.type", null, FacesMessage.SEVERITY_ERROR); 
				throw new Exception("ContentObject Type for the generation of a new content object has not been provided"); 
			}
			else{
				//Clear value
				pageController.setContentObjectTypeToUseWhenLoadingClearedContentObjectEditForm(null);
			}
		}

		contentObject = cmsRepositoryEntityFactory.newContentObjectForType(contentObjectTypeForNewObject, JSFUtilities.getLocaleAsString());


		selectedContentObjectForEdit = contentObjectUIWrapperFactory.getInstance(contentObject);
		
		if (contentObject.getComplexCmsRootProperty().isChildPropertyDefined("profile.language")){ 
			((StringProperty)contentObject.getCmsProperty("profile.language")).addSimpleTypeValue(JSFUtilities.getLocaleAsString()); 
		}

		// get the current user and set the owner of this new content object accordingly
		contentObject.setOwner(loggedInRepositoryUser.getRepositoryUser());

		// initialize the creator with the name of the owner
		if (contentObject.getComplexCmsRootProperty().isChildPropertyDefined("profile.creator")){ 
			((StringProperty)contentObject.getCmsProperty("profile.creator")).addSimpleTypeValue(loggedInRepositoryUser.getRepositoryUser().getLabel()); 
		}

		// initialize content type localized name for presentation at the form
		contentObjectTypeLocalizedName = contentObject.getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());

		// initialize the default accessibility values setting appropriately the corresponding input components in the form (i.e. the radio button default values)

		clearUIAccessRightLists();
		
		radioButtonSelectionMap.put(AccessRight.canBeReadBy.toString(), 1);// READ BY ALL
		radioButtonSelectionMap.put(AccessRight.canBeUpdatedBy.toString(), 2); // UPDATED BY NONE (only owner is allowed)
		radioButtonSelectionMap.put(AccessRight.canBeDeletedBy.toString(), 2); // DELETED BY NONE (only owner is allowed)
		radioButtonSelectionMap.put(AccessRight.canBeTaggedBy.toString(), 1); // TAGGED BY ALL
		 

		contentObjectTitle = null;
	}


	private void loadComplexCmsPropertyToComplexCmsPropertyEditor(ComplexCmsProperty complexCmsProperty) {

		//Find appropriate template to load
		String cmsPropertyTemplatePath = "/WEB-INF/pageComponents/";    

		String definitionFullPath = complexCmsProperty.getPropertyDefinition().getFullPath();

		complexCmsPropertyEditorPagePath = cmsPropertyTemplatePath+"edit/propertyTemplates/"+generateCmsPropertyTemplateName(definitionFullPath)+"-editor.xhtml"; 

		try{

			ServletContextResource templateResource = 
				new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), complexCmsPropertyEditorPagePath);

			if (!templateResource.exists()){
				
				//Template for property was not found. 
				//Check if there is a template for this complex property as a global one
				//and not in terms of its type
				String definitionPath = complexCmsProperty.getPropertyDefinition().getPath();
				
				complexCmsPropertyEditorPagePath = cmsPropertyTemplatePath+"edit/propertyTemplates/"+generateCmsPropertyTemplateName(definitionPath)+"-editor.xhtml"; 

				templateResource = 
					new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), complexCmsPropertyEditorPagePath);
				
				if (!templateResource.exists()){
					throw new Exception();
				}
			}


		}
		catch(Exception e){
			logger.debug("Template '"+complexCmsPropertyEditorPagePath+"' for cms property '"+definitionFullPath + "' not found. Loading default "+defaultComplexCmsPropertyEditorPagePath); 

			//Return default template
			complexCmsPropertyEditorPagePath = defaultComplexCmsPropertyEditorPagePath;
		}
		finally{
			//Render with default editor
			complexCmsPropertyEdit.editComplexCmsProperty(complexCmsProperty);
		}


		//Create bread crumbs
		createComplexCmsPropertyToBreadCrumd(complexCmsProperty);

	}


	private String generateCmsPropertyTemplateName(String definitionFullPath) {
		return definitionFullPath.replaceAll("\\.", "-"); 
	}

	public void removeAspectFromEditedContentObject_UIAction(String aspect) {

		if (selectedContentObjectForEdit != null && selectedContentObjectForEdit.getContentObject() != null){
			try{
				//	Remove aspect from list in ContentObject
				if (!selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().hasAspect(aspect)){
					logger.warn("Unable to remove aspect '"+ aspect+ "' from content object "+ selectedContentObjectForEdit.getContentObject().getId()  
							+ " because aspect does not exist in content object"); 
				}
				else{
					//selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().removeAspect(aspect);
					selectedContentObjectForEdit.getContentObject().removeCmsProperty(aspect);

					complexCmsPropertyEdit.reloadEditedCmsProperties();
				}
			}
			catch (Exception e){
				logger.error("",e); 
				JSFUtilities.addMessage(null, "content.object.edit.remove.aspect.error", null, FacesMessage.SEVERITY_ERROR); 
			}
		}

	}

	public void addAspectToComplexProperty_UIAction(String newlyAddedAspect){
		if (selectedContentObjectForEdit != null){
			try{
				//Aspect is new. Add to content object
				//selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().addAspect(newlyAddedAspect);
				selectedContentObjectForEdit.getContentObject().getCmsProperty(newlyAddedAspect);

				//Aspect path now contains content object type
				showSelectedComplexPropertyForPath_UIAction(newlyAddedAspect);

			}
			catch (Exception e){
				logger.error("",e); 
				JSFUtilities.addMessage(null, "content.object.edit.add.aspect.error", null, FacesMessage.SEVERITY_ERROR); 
			}
		}
	}

	@Observer({SeamEventNames.NEW_COMPLEX_CMS_PROPERTY_ADDED})
	public void showSelectedComplexPropertyForPath_UIAction(String complexCmsPropertyPath){
		
		//BE AWARE
		//complexCmsPropertyPath is the path of property WIHTOUT the content type
		if (StringUtils.isNotBlank(complexCmsPropertyPath)){

			try{

				ComplexCmsProperty complexCmsProperty = null;
				
				if (selectedContentObjectForEdit.getContentObject().getTypeDefinition().getName().equals(complexCmsPropertyPath)){
					//Load root property
					complexCmsProperty = selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty();
				}
				else{
					complexCmsProperty = (ComplexCmsProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty(complexCmsPropertyPath);
				}

				if (complexCmsProperty == null){
					//Complex Cms Property is either not defined or
					//is a multiple complex cms property
					if (selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined(complexCmsPropertyPath)){

						//Create a template for complex cms property
						complexCmsProperty = (ComplexCmsProperty)complexCmsPropertyEdit.getEditedComplexCmsProperty().createNewValueForMulitpleComplexCmsProperty(complexCmsPropertyPath);
						
					}
				}
				
				if (complexCmsProperty == null){
						logger.warn("Attempt to create and load complex cms Property "+ complexCmsPropertyPath + " failed"); 
				}
				else{
					//Load complex cms property editor only if complex cms property was found
					loadComplexCmsPropertyToComplexCmsPropertyEditor(complexCmsProperty);
				}
			}
			catch(Exception e){
				logger.error("",e); 
				JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN); 
			}
		}
	}

	//Save a content object without version
	//Temporarily sets loggedInRepositoryUserSetting for creatingVersion on save to false
	//performs save and then restores previous value
	private String disableCreateVersionAndSaveContentObject() {

		boolean previousValue = Boolean.valueOf(loggedInRepositoryUserSettings.isCreateVersionUponSuccessfulSave());

		loggedInRepositoryUserSettings.setCreateVersionUponSuccessfulSave(false);

		String saveOutcome = saveContentObject();

		loggedInRepositoryUserSettings.setCreateVersionUponSuccessfulSave(previousValue);

		return saveOutcome;
	}


	public String saveContentObject_UIAction() {
		//We care about the outcome only if it is about
		//clear
		String saveOutcome = saveContentObject();

		if (StringUtils.isNotBlank(saveOutcome) &&
				CLEAR_CONTENT_OBJECT_EDIT_AND_LOAD_AGAIN_XHTML.equals(saveOutcome)){
			return saveOutcome;
		}
		else{
			//Return null to stay in the same page
			return null;
		}
		
	}

	private String saveContentObject() {


		String contentObjectPreparationOutcome = "error"; 

		boolean contentObjectWasSavedInRepository = false;
		
		String providedSystemName = selectedContentObjectForEdit.getContentObject().getSystemName() != null ?
				new String(selectedContentObjectForEdit.getContentObject().getSystemName()) : null;
				
		// we will need this to know if we have been required to use admin privileges  in order to successfully save the object 
		boolean adminRoleHasBeenAdded = false;
		// and these in order to add admin privileges if we are required to do so
		SecurityContext securityContext = null;
		String roleAdminForActiveRepository = null;
				
		try {
			// first do some checks and prepare content object for save
			contentObjectPreparationOutcome = prepareContentObjectForSave();

			// if checks and preparation were successful we now we may proceed to save the content object
			if (contentObjectPreparationOutcome.equals("contentObjectReadyForSave")) { 
				
				//Generate draftItemId before save as during save an id is created
				String draftItemId = DraftItem.generateDraftItemIdForCmsRepositoryEntity(selectedContentObjectForEdit.getContentObject());
				
				// Sent an event just before saving to allow custom observers to perform actions or change the object
				// We are providing a default observer that looks for a specially named script objects in the repository and
				// runs the script. This allows users to dynamically add code for this event directly through astroboa console
				// The script object should have a system name equal to the event name, i.e. "org.betaconceptframework.astroboa.console.contentObjectReadyForSave".
				// Additionally the script object should be owned by SYSTEM to prevent unauthorized users from changing the behaviour of content object editing.
				// Finally the script itself should be a GROOVY class implementing the "PreSaveGroovyActionInterface"
				// Example:
				//	import org.betaconceptframework.astroboa.api.model.*;
				//	import org.betaconceptframework.astroboa.console.jsf.edit.PreSaveGroovyAction;
				//	import org.slf4j.Logger;
				//
				//	public class PreSaveAction implements PreSaveGroovyAction {
				//
				//		public void run(ContentObject contentObject, Logger logger) {
				//			// do some actions
				//		}
				//	}
				//
				// A common case for adding custom script code is to automatically alter 
				// object accessibility settings according to some common rules set by each organization
				if (!selectedContentObjectForEdit
						.getContentObject()
						.getSystemName()
						.equals(SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE)) { // We do not want to sent the event if the edited object is the script object itself that handles the event
					Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE, new Object[]{
						selectedContentObjectForEdit.getContentObject()});
				}
				
				// we save the content object
				// but before we should check if the object type is a subclass of personType and the logged in username is the same as the username stored in the person object 
				// "personAuthentication.username" property. In other words we should detect the case that a user tries to save her user profile. 
				// If such a case exists then we should check if the user is the owner of the object (usually she is not because an admin will create the accounts). 
				// If not then we should perform the save as an admin on behalf of the user.
				if (selectedContentObjectForEdit.isLoggedInUsersPersonProfile() &&
					!selectedContentObjectForEdit.getContentObject().getOwner().getExternalId().equals(loggedInRepositoryUser.getIdentity())) {
					
					// we will emulate a run as functionality adding the admin role into user roles and after the save we will remove it
					securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
					roleAdminForActiveRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN);
					
					if (securityContext != null && ! securityContext.hasRole(roleAdminForActiveRepository)) {
						adminRoleHasBeenAdded = securityContext.addRole(roleAdminForActiveRepository);
					}
				}
				
				contentService.save(selectedContentObjectForEdit.getContentObject(), loggedInRepositoryUserSettings.isCreateVersionUponSuccessfulSave(), true, null);

				//Flag indicating that object was saved in repository
				contentObjectWasSavedInRepository = true;

				// make necessary resets in views and objects before leaving edit mode
				//	resetViewAndState();

				// change the header message of the content object list panel
				contentObjectList.setContentObjectListHeaderMessage(JSFUtilities.getStringI18n("object.list.message.contentObjectListHeaderMessageAfterContentObjectSave")); 
				
				// Update the accessibility UI components in the case the pre-save actions 
				// (any code triggered by the CONTENT_OBJECT_READY_FOR_SAVE event) have altered accessibility property
				initializeInputAndOutputUIComponentsRelatedtoSecurityProperties();
				
				// generate a success message
				JSFUtilities.addMessage(null, "content.object.edit.successful.save.info.message", null, FacesMessage.SEVERITY_INFO); 

				if (listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated.size() > 0)
					Events.instance().raiseEvent(SeamEventNames.UPDATE_NO_OF_CONTENT_OBJECT_REFERRERS, listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated);

				listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated.clear();

				//Reset loggedinRepositoryUser's root topics
				//in order to force aspect to reload them from database
				resetLoggedInRepositoryUserFolksonomyRootTopics();


				//Notify content type tree if content object is new
				if (newContentObject){
					Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_ADDED, new Object[]{
							selectedContentObjectForEdit.getContentObject().getContentObjectType(), selectedContentObjectForEdit.getContentObject().getId(), 
							(selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("profile.created") ? 
							((CalendarProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.created")).getSimpleTypeValue() : Calendar.getInstance())}); 
				}
				else{
					Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_UPDATED, 
								new Object[]{selectedContentObjectForEdit.getContentObject().getContentObjectType(),
												selectedContentObjectForEdit.getContentObject().getId()});
				}
				
				//Raise a special event for draft. LoggedInRepositoryUser is needed
				Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_MODIFIED, new Object[]{loggedInRepositoryUser.getRepositoryUser().getId(), 
						draftItemId});
				

			} 
			else{
				return contentObjectPreparationOutcome;
			}

		}
		catch(CmsNonUniqueContentObjectSystemNameException e)
		{
			if (! contentObjectWasSavedInRepository){
				//Produce error message only if content object was not saved at all
				JSFUtilities.addMessage(null, "content.object.edit.non.unique.system.name", new String[]{selectedContentObjectForEdit.getContentObject().getSystemName()}, FacesMessage.SEVERITY_WARN); 

				if (newContentObject) {
					selectedContentObjectForEdit.getContentObject().setId(null);
				}
			}
			
			return "error"; 
			
		}
		catch(CmsConcurrentModificationException e)
		{
			JSFUtilities.addMessage(null, "content.object.edit.save.concurrentModificationError", null, FacesMessage.SEVERITY_WARN); 

			if (newContentObject) {
				selectedContentObjectForEdit.getContentObject().setId(null);
			}
			
			return "error"; 
			
		}
		catch (Exception e) {
			getLogger().error("Content Object could not be saved",e); 

			if (! contentObjectWasSavedInRepository){
				//Produce error message only if content object was not saved at all
				JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR); 
				if (newContentObject) {
					selectedContentObjectForEdit.getContentObject().setId(null);
				}
			}
			
			return "error"; 

		}
		finally {
			if (adminRoleHasBeenAdded) {
				securityContext.removeRole(roleAdminForActiveRepository);
			}
		}
		
		
		//check if content object's system name was not unique 
		if (newContentObject && selectedContentObjectForEdit.getContentObject().getSystemName() != null && selectedContentObjectForEdit.getContentObject().getId() != null && 
				selectedContentObjectForEdit.getContentObject().getSystemName().endsWith(selectedContentObjectForEdit.getContentObject().getId()))
		{
			JSFUtilities.addMessage(null, "content.object.edit.save.system.name.provided",
					providedSystemName != null ? new String[]{providedSystemName}:
						new String[]{""}, FacesMessage.SEVERITY_WARN);
		}

		// check if user wishes to copy the content object to a SPACE_INSTANCE
		
		try {
			
			if (spaceToCopyNewObject != null) {
				
				String contentObjectId = selectedContentObjectForEdit.getContentObject().getId();
				
				spaceToCopyNewObject.addContentObjectReference(contentObjectId);
				spaceService.saveSpace(spaceToCopyNewObject);
				// recreate space items
				userSpaceNavigation.getUserSpaceItems();
				
				// reset selected space to prevent coping it in successive saves of the same object
				spaceToCopyNewObject = null;
				
				// generate a success message
				JSFUtilities.addMessage(null, "content.object.edit.sucessfully.attach.content.object.to.space", null, FacesMessage.SEVERITY_INFO); 
			}
				
				

		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "content.object.edit.space.attach.error", null, FacesMessage.SEVERITY_ERROR); 
			getLogger().error("Content Object could not be attached to requested SPACE_INSTANCE",e); 
			return "error"; 

		}
		

		//Decide if editor should be closed
		/*
		if (contentObjectPreparationOutcome.equals("contentObjectReadyForSave")) {
			
			//Copy to clipboard
			if (loggedInRepositoryUserSettings.isCopyContentObjectToClipboardUponSuccessfulSave()){
				clipboard.copyContentObjectToClipboard_UIAction(
						selectedContentObjectForEdit.getContentObject().getId(), 
						selectedContentObjectForEdit.getContentObject().getSystemName(), 
						((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.title")).getSimpleTypeValue(),
						selectedContentObjectForEdit.getContentObjectTypeForCurrentLocale(),
						selectedContentObjectForEdit.getContentObject().getContentObjectType());
			}
			
			if (newContentObject && loggedInRepositoryUserSettings.isOpenNewContentObjectEditorUponSuccessfulSave()){
				pageController.setContentObjectTypeToUseWhenLoadingClearedContentObjectEditForm(selectedContentObjectForEdit.getContentObject().getContentObjectType());
				return CLEAR_CONTENT_OBJECT_EDIT_AND_LOAD_AGAIN_XHTML;
			}
		}
		*/
		
		//Remained in the same page. Set newContentObject flag to false
		if (contentObjectWasSavedInRepository){
			newContentObject = false;
		}
		return contentObjectPreparationOutcome;

	}


	private void resetLoggedInRepositoryUserFolksonomyRootTopics() {
		loggedInRepositoryUser.getRepositoryUser().getFolksonomy().setRootTopics(null);
	}


	private String prepareContentObjectForSave() throws Exception {

		// generate the accessibility properties from the user provided input values (through the accessibility tab in the content object UI form)
		String methodOutcome = generateContentObjectAccessibilityPropertiesFromUIFormInputValues();
		if (methodOutcome.equals("error")) 
			return "error"; 
		
		// the following is a fix for profile.language. If it is an empty list we should nullify it because the pick list UI component 
		// generates an immutable ArrayList that prevents the repository engine to add the default value (as is set in XSD)
		//StringProperty language = (StringProperty)selectedContentObjectForEdit.getContentObjectProperty().get("profile.language");
		//if (language != null && CollectionUtils.isEmpty(language.getSimpleTypeValues())) {
		//	language.setSimpleTypeValues(null);
		//}
		
		//Validate ContentObject properties
		validateProperties();

		// check if all mandatory properties have been provided. The checking method produces the appropriate UI messages when a property is missing 
		//if (mandatoryPropertiesMissing()) {
		if (cmsPropertyValidatorVisitor.mandatoryPropertiesAreMissing()) {
			JSFUtilities.addMessage(null, "content.object.edit.error.required", null, FacesMessage.SEVERITY_WARN); 
			return "error"; 
		}

		if (cmsPropertyValidatorVisitor.hasErrors())
			return "error"; 


		// Perform a specific validation in case content object is of 'portal' type
		if (CmsConstants.PORTAL_CONTENT_OBJECT_TYPE.equals(selectedContentObjectForEdit.getContentObject().getContentObjectType())){
			
			String portalSystemName = selectedContentObjectForEdit.getContentObject().getSystemName(); 

			//Look for duplicate portalSystem name
			ContentObjectCriteria portalCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			
			portalCriteria.addContentObjectTypeEqualsCriterion(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE);
			portalCriteria.addSystemNameEqualsCriterion(portalSystemName);
			
			if (selectedContentObjectForEdit.getContentObject().getId() != null){
				portalCriteria.addIdNotEqualsCriterion(selectedContentObjectForEdit.getContentObject().getId());
			}
			portalCriteria.doNotCacheResults();
			portalCriteria.setOffsetAndLimit(0, 1);
			
			CmsOutcome<CmsRankedOutcome<ContentObject>> portalOutcome = contentService.searchContentObjects(portalCriteria);
			
			if (portalOutcome !=null && portalOutcome.getCount() > 0){
				JSFUtilities.addMessage(null, "content.object.edit.already.existing.portal.system.name", new String[]{portalSystemName}, FacesMessage.SEVERITY_WARN); 
				return "error"; 
			}
		}
		
		//Perform one final check. In case content object status is scheduledForPublication but no
		//webPublication.webPublicationStartDate is provided throw a mandatory error message
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		if (contentObjectStatusProperty != null && contentObjectStatusProperty.hasValues())
		{
			String contentObjectStatus = contentObjectStatusProperty.getSimpleTypeValue();
			
			if (StringUtils.equals(ContentObjectStatus.scheduledForPublication.toString(), contentObjectStatus))
			{
				//Check webPublicationStartDate property
				CalendarProperty webPublicationStartDateProperty = ((CalendarProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("webPublication.webPublicationStartDate"));
				
				if (webPublicationStartDateProperty == null || webPublicationStartDateProperty.hasNoValues())
				{
					String propertyLocalizedLabel = (webPublicationStartDateProperty == null? 
							JSFUtilities.getLocalizedMessage("content.object.edit.web.publication.start.date", null) :
							webPublicationStartDateProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(JSFUtilities.getLocaleAsString(), " > ")
							);
					
					JSFUtilities.addMessage("webPublication.webPublicationStartDate", "errors.required", 
							new String[]{propertyLocalizedLabel}, FacesMessage.SEVERITY_WARN);
					
					return "error";
					
				}
			}
		}
		
		
		return "contentObjectReadyForSave"; 

	}



	private void validateProperties() {

		ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(selectedContentObjectForEdit.getContentObject().getContentObjectType());

		cmsPropertyValidatorVisitor.reset(false);

		cmsPropertyValidatorVisitor.validateContentObjectProperties(selectedContentObjectForEdit.getContentObject(), contentObjectTypeDefinition);
	}


	/*private boolean mandatoryPropertiesMissing() throws Exception {

		boolean mandatoryPropertiesMissing = false;

		// check for mandatory fields in Administrative Bundles
	//	if (mandatoryPropertiesOfAdministrativeBundlesMissing())
		//	mandatoryPropertiesMissing = true;

		// check if all mandatory content type properties have been provided
		if (mandatoryPropertiesOfContentTypeMissing(selectedContentObjectForEdit.getContentObject().getContentObjectType()))
			mandatoryPropertiesMissing = true;


		// if content type is textBased, image, audio or video
		//if (selectedContentObjectForEdit.isFile()) {

			//Check if  binary channels exist or have been uploaded
			//if (selectedContentObjectForEdit.getContentObject().getBinaryChannels() == null) {
				//JSFUtilities.addMessage(null, "contentObjectEdit.noFileHasBeenUploadedError", null, FacesMessage.SEVERITY_ERROR);
			//}
		//}

		return mandatoryPropertiesMissing;
	}


	private boolean mandatoryPropertiesOfContentTypeMissing(String contentType) throws Exception {

		boolean mandatoryPropertiesMissing = false;

			MandatoryPropertyDefinitionVisitor mandatoryPropertyDefinitionVisitor = new MandatoryPropertyDefinitionVisitor(selectedContentObjectForEdit.getContentObject());
			ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(contentType);

			contentObjectTypeDefinition.accept(mandatoryPropertyDefinitionVisitor);
			mandatoryPropertiesMissing = mandatoryPropertyDefinitionVisitor.mandatoryPropertiesAreMissing();

		return mandatoryPropertiesMissing;
	}*/


	public void grantAccessRightToDraggedAndDroppedUserOrRole_Listener(DropEvent dropEvent) {
		String typeOfAccessRight = (String) dropEvent.getDropValue();
		String typeOfDraggedObject = dropEvent.getDragType();
		
		Map<String,String> userOrGroupMap = new HashMap<String,String>();
		if ("user".equals(typeOfDraggedObject)) { 
			Person user = (Person) dropEvent.getDragValue();
			userOrGroupMap.put("userOrGroupId", user.getUsername()); 
			userOrGroupMap.put("userOrGroupName", StringUtils.isBlank(user.getDisplayName())? user.getUsername() : user.getDisplayName()); 
		}
		else if ("role".equals(typeOfDraggedObject)) { 
			String	personGroup = (String) dropEvent.getDragValue();
			userOrGroupMap.put("userOrGroupId", personGroup); 
			userOrGroupMap.put("userOrGroupName", Utils.retrieveDisplayNameForRoleOrPerson(identityStoreRunAsSystem, personGroup)); 
		}

		grantAccessRightToPersonOrRole(AccessRight.valueOf(typeOfAccessRight), userOrGroupMap);
	}
	
	public void grantAccessRightToPerson(Person person, AccessRight typeOfAccessRight){
		
		Map<String,String> userOrGroupMap = new HashMap<String,String>();
		userOrGroupMap.put("userOrGroupId", person.getUsername()); 
		userOrGroupMap.put("userOrGroupName", StringUtils.isBlank(person.getDisplayName())? person.getUsername() : person.getDisplayName()); 
		
		grantAccessRightToPersonOrRole(typeOfAccessRight, userOrGroupMap);
		
	}


	public void grantAccessRightToPersonOrRole(AccessRight typeOfAccessRight,
			Map<String, String> userOrRoleMap) {
		
		
		if (typeOfAccessRight != null){
			
			if (!userListByAccessRight.containsKey(typeOfAccessRight.toString())){
				userListByAccessRight.put(typeOfAccessRight.toString(), new ArrayList<Map<String,String>>());
			}
			
			List<Map<String, String>> userList = userListByAccessRight.get(typeOfAccessRight.toString());
			
			if (userList != null){
				if (userOrGroupIsNotInAccessRightList(userList, userOrRoleMap.get("userOrGroupName"))) 
					userList.add(userOrRoleMap);
				else {
					logger.warn("User already in access right list. selection is ignored"); 
					JSFUtilities.addMessage(null, "content.object.edit.accessibility.value.already.exists.warning", null, FacesMessage.SEVERITY_WARN); 
				} 
			}
		}
	}


	public void removeUserOrGroupFromAccessRightList_UIAction(List<HashMap<String,String>> accessRightList, HashMap<String,String> userOrGroup) {
		accessRightList.remove(userOrGroup);
	}

	private boolean userOrGroupIsNotInAccessRightList(List<Map<String,String>> accessRightList, String userOrGroupName) {
		if (CollectionUtils.isEmpty(accessRightList))
			return true;
		for (Map<String, String> userHashMap : accessRightList) {
			if (userHashMap.get("userOrGroupName").equals(userOrGroupName)) 
				return false;
		}
		return true;
	}

	private String generateContentObjectAccessibilityPropertiesFromUIFormForAccessRight(AccessRight accessRight, StringProperty accessibilityProperty){
		if (accessRight ==null){
			return "error";
		}
		
		if (radioButtonSelectionMap.containsKey(accessRight.toString())) {
			
			switch (radioButtonSelectionMap.get(accessRight.toString())) {
			case 1:
				
				if (accessibilityProperty.hasValues()){
					accessibilityProperty.getSimpleTypeValues().clear();
				}
				
				accessibilityProperty.addSimpleTypeValue(ContentAccessMode.ALL.toString());
				
				break;
			case 2:
				
				if (accessibilityProperty.hasValues()){
					accessibilityProperty.getSimpleTypeValues().clear();
				}
				
				accessibilityProperty.addSimpleTypeValue(ContentAccessMode.NONE.toString());
				
				break;
			case 3:
				
				List<Map<String, String>> userList = userListByAccessRight.get(accessRight.toString());
				if (userList == null || userList.size() == 0) {
					JSFUtilities.addMessage(null, "content.object.edit.accessibility.value.user.group."+accessRight.toString()+".empty", null, FacesMessage.SEVERITY_ERROR); 
					return "error"; 
				}
				copyUIAccessRightListToContentObjectAccessRightList(userList, accessibilityProperty.getSimpleTypeValues());
				break;
			
			default:
				// we do not support any other value. reset radio button to default, generate an error message and return
				JSFUtilities.addMessage(null, "content.object.edit.invalid.accessibility.value", 
						new String[] {String.valueOf(radioButtonSelectionMap.get(accessRight.toString()))}, FacesMessage.SEVERITY_WARN); 
			
				radioButtonSelectionMap.put(accessRight.toString(), 1);
			
				return "error";
				
			}
			
			return "success";
		}
		
		return "success";
		
	}
	
	private String generateContentObjectAccessibilityPropertiesFromUIFormInputValues() {
		// do nothing if no accessibility properties exist
		if (!selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("accessibility")) 
			return "success"; 
		// Load Accessibility properties
		StringProperty canBeReadBy = (StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeReadBy"); 
		StringProperty canBeUpdatedBy = (StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeUpdatedBy"); 
		StringProperty canBeDeletedBy = (StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeDeletedBy"); 
		StringProperty canBeTaggedBy = (StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeTaggedBy"); 

		String outcome = generateContentObjectAccessibilityPropertiesFromUIFormForAccessRight(AccessRight.canBeReadBy, canBeReadBy);
		
		if (!"error".equals(outcome)){
		
			outcome = generateContentObjectAccessibilityPropertiesFromUIFormForAccessRight(AccessRight.canBeUpdatedBy, canBeUpdatedBy);
			
			if (!"error".equals(outcome)){
				outcome = generateContentObjectAccessibilityPropertiesFromUIFormForAccessRight(AccessRight.canBeDeletedBy, canBeDeletedBy);
				
				if (!"error".equals(outcome)){
					return generateContentObjectAccessibilityPropertiesFromUIFormForAccessRight(AccessRight.canBeTaggedBy, canBeTaggedBy);
				}
				else{
					return "error";
				}
			}
			else{
				return "error";
			}
		}
		else{
			return "error";
		}
		
	}



	/* Copy an access right list from content object into a list which is more appropriate for UI presentation. Actually turn user and group ids to meaningful names.
	 * The access right lists in the content object holds only user / group ids which are not very meaningful to the end user. So we connect to the crm database, we read the
	 * user / group names and generate the access rights lists which are used by the UI to present meaningful info about access rights 
	 */
	private void copyContentObjectAccessRightListToUIAccessRightList(List<String> contentObjectAccessRightList, List<Map<String,String>> uiAccessRightList) {
		for (String prefixedUserOrGroupId :  contentObjectAccessRightList) {		
				Map<String,String> userOrGroup = new HashMap<String,String>();
				userOrGroup.put("userOrGroupId", prefixedUserOrGroupId);
				
				userOrGroup.put("userOrGroupName", Utils.retrieveDisplayNameForRoleOrPerson(identityStoreRunAsSystem, prefixedUserOrGroupId));
				
				uiAccessRightList.add(userOrGroup);
		}
	}




	/* This method does the opposite function of "copyContentObjectAccessRightListToUIAccessRightList" method
	 * It gets an access right list genetated by the UI after user interaction and produces the appropriate access right list for insertion into the content object
	 */
	private void copyUIAccessRightListToContentObjectAccessRightList(List<Map<String,String>> uiAccessRightList, List<String> contentObjectAccessRightList) {
		
		//Remove old values and add new ones
		contentObjectAccessRightList.clear();
		for (Map<String,String> userOrGroup : uiAccessRightList) {
			contentObjectAccessRightList.add(userOrGroup.get("userOrGroupId")); 
		}
	}


	private void initializeInputAndOutputUIComponentsRelatedtoSecurityProperty(AccessRight accessRight, List<String> accessValuesList, String defaultAccessRightValue){

		if (CollectionUtils.isNotEmpty(accessValuesList)) {

			if(accessValuesList.get(0).equals(ContentAccessMode.ALL.toString())) {
				radioButtonSelectionMap.put(accessRight.toString(), 1);
			}
			else if (accessValuesList.get(0).equals(ContentAccessMode.NONE.toString())) {
				radioButtonSelectionMap.put(accessRight.toString(), 2);
			}
			else {
				radioButtonSelectionMap.put(accessRight.toString(), 3);
				copyContentObjectAccessRightListToUIAccessRightList(accessValuesList, userListByAccessRight.get(accessRight.toString()));
			}
		}
		else{
			if (StringUtils.isBlank(defaultAccessRightValue))
			{
				//Default value for access right
				radioButtonSelectionMap.put(accessRight.toString(), 1);
			}
			else
			{
				if(StringUtils.equals(ContentAccessMode.ALL.toString(), defaultAccessRightValue)) {
					radioButtonSelectionMap.put(accessRight.toString(), 1);
				}
				else if (StringUtils.equals(ContentAccessMode.NONE.toString(), defaultAccessRightValue)) {
					radioButtonSelectionMap.put(accessRight.toString(), 2);
				}
			}
		}
	
	}
	
	private void initializeInputAndOutputUIComponentsRelatedtoSecurityProperties() {
		// do nothing if no accessibility properties exist
		if (!selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("accessibility")) 
			return;

		// Create Accessibility Lists
		List<String> canBeReadByList = ((StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeReadBy")).getSimpleTypeValues(); 
		List<String> canBeUpdatedByList = ((StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeUpdatedBy")).getSimpleTypeValues(); 
		List<String> canBeDeletedByList = ((StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeDeletedBy")).getSimpleTypeValues(); 
		List<String> canBeTaggedByList = ((StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("accessibility.canBeTaggedBy")).getSimpleTypeValues(); 

		clearUIAccessRightLists();
		
		initializeInputAndOutputUIComponentsRelatedtoSecurityProperty(AccessRight.canBeReadBy, canBeReadByList, ContentAccessMode.ALL.toString());
		initializeInputAndOutputUIComponentsRelatedtoSecurityProperty(AccessRight.canBeUpdatedBy, canBeUpdatedByList, ContentAccessMode.NONE.toString());
		initializeInputAndOutputUIComponentsRelatedtoSecurityProperty(AccessRight.canBeDeletedBy, canBeDeletedByList, ContentAccessMode.NONE.toString());
		initializeInputAndOutputUIComponentsRelatedtoSecurityProperty(AccessRight.canBeTaggedBy, canBeTaggedByList,ContentAccessMode.ALL.toString());
	}


	private void clearUIAccessRightLists() {
		
		if (radioButtonSelectionMap == null){
			radioButtonSelectionMap = new HashMap<String, Integer>();
		}
		else{
			radioButtonSelectionMap.clear();
		}
		
		if (userListByAccessRight == null){
			userListByAccessRight = new HashMap<String, List<Map<String,String>>>();
		}
		else{
			userListByAccessRight.clear();
		}
		
		for (AccessRight accessRight : AccessRight.values()){
			userListByAccessRight.put(accessRight.toString(), new ArrayList<Map<String,String>>());
		}
		
	}


	public void removeTopicFromContentObjectSubject_UIAction() {
		try {
			getSelectedContentObjectForEdit().removeTopicFromContentObjectSubject(selectedTopicIdForRemovalFromContentObjectSubject);
			updateListOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated(selectedTopicIdForRemovalFromContentObjectSubject);
		}
		catch (Exception e) {
			logger.error("",e); 
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN);
			return;
		}
	}

	public void addNewTagToContentObjectSubject_UIAction() {
		if (StringUtils.isNotBlank(newTagLabel)) {
			//	check if the new tag exists in user tags
			Topic newUserTag = cmsUtilities.createNewUserTag(newTagLabel, JSFUtilities.getLocaleAsString(), loggedInRepositoryUser.getRepositoryUser());
			if (cmsUtilities.findTopicInTopicListByLocalizedTopicLabel(loggedInRepositoryUser.getRepositoryUser().getFolksonomy().getRootTopics(), 
					newTagLabel) != null) {
				logger.warn("The user tag is not a new tag since it is already in user tags"); 
				JSFUtilities.addMessage(null, "content.object.edit.tag.already.exists", null, FacesMessage.SEVERITY_WARN); 
				return;
			}

			// check if the new tag is already in contentObject subject. Remember that the new tags are saved with the object and are not yet in the list of user tags, so the above check is not enough 
			if (cmsUtilities.findTopicInTopicListByLocalizedTopicLabel(((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).getSimpleTypeValues(),  
					newTagLabel) != null) {
				logger.warn("The user tag is already in content object subject."); 
				JSFUtilities.addMessage(null, "content.object.edit.tag.already.selected", null, FacesMessage.SEVERITY_WARN); 
				return;
			}

			// if we have reached this point everything is ok. Let proceed to add the new  tag
			//Save Tag
			topicService.saveTopic(newUserTag);

			//Attach tag to content object
			((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).addSimpleTypeValue(newUserTag); 

			// reset tag value
			newTagLabel = null;

			//Reset loggedinRepositoryUser's root topics
			//in order to force aspect to reload them from database
			resetLoggedInRepositoryUserFolksonomyRootTopics();

		}
		else {
			logger.error("An empty label has been provided. User Tag cannot be created with an empty label"); 
			JSFUtilities.addMessage(null, "content.object.edit.empty.tag.error", null, FacesMessage.SEVERITY_WARN); 

		}
	}

	public void addExistingUserTagToContentObjectSubject_UIAction() {
		Topic selectedLoggedInUserTag = cmsUtilities.findTopicInTopicListByTopicId(loggedInRepositoryUser.getRepositoryUser().getFolksonomy().getRootTopics(), selectedLoggedInUserTagId);
		if (selectedLoggedInUserTag != null) {
			//	check if the selected tag is already in contentObject subject 
			if (cmsUtilities.findTopicInTopicListByLocalizedTopicLabel(((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).getSimpleTypeValues(),  
					selectedLoggedInUserTag.getLocalizedLabelForCurrentLocale()) != null) {
				logger.warn("The selected tag is already in content object subject."); 
				JSFUtilities.addMessage(null, "content.object.edit.tag.already.selected", null, FacesMessage.SEVERITY_WARN); 
				return;
			}

			// if we reached this point everything is ok. Let proceed to add the selected tag
			((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).addSimpleTypeValue(selectedLoggedInUserTag); 
		}
		else {
			logger.error("Some strange problem occured. Selected tag is not present in logged in user Tags List (loggedInUserTags)"); 
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN);
			return;

		}
		setSelectedLoggedInUserTagId(null);

	}


	public SimpleCmsPropertyValueWrapper getSimpleCmsPropertyValueWrapper(
			Integer indexOfSimpleCmsPropertyWrapper,
			Integer indexOfSimpleCmsPropertyValueWrapper) {
		if (complexCmsPropertyEdit == null)
			return null;

		try{
			return complexCmsPropertyEdit.getSimpleCmsPropertyValueWrapper(indexOfSimpleCmsPropertyWrapper, indexOfSimpleCmsPropertyValueWrapper);
		}
		catch(Exception e){
			logger.error("While trying to get simple cms property value wrapper with index "+indexOfSimpleCmsPropertyValueWrapper + 
					" from simple cms property wrapper with index "+ indexOfSimpleCmsPropertyWrapper, e); 
			return null;
		}

	}


	public void addDraggedAndDroppedSpace_Listener(DropEvent dropEvent) {
		spaceToCopyNewObject = (Space) dropEvent.getDragValue();
	}

	@Observer({SeamEventNames.UPDATE_LIST_OF_TOPICS_WHOSE_NUMBER_OF_CO_REFERENCES_SHOULD_CHANGE})
	public void updateListOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated(String topicId) {
		listOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated.add(topicId);
	}

	public void addDraggedAndDroppedTopicToContentObjectProfileSubject_Listener(DropEvent dropEvent) {
		LazyLoadingTopicTreeNode topicTreeNode = (LazyLoadingTopicTreeNode) dropEvent.getDragValue();

		//Check that dropped Topic belongs to Subject Taxonomy
		Topic droppedTopic = topicTreeNode.getTopic();

		//Check that Dropped Topic belongs to an accepted taxonomy
		List<String> acceptedTaxonomies = selectedContentObjectForEdit.getAcceptedTaxonomiesForProfileSubject();
		if (CollectionUtils.isNotEmpty(acceptedTaxonomies)){
			String droppedTopicTaxonomyName = null;
			if (droppedTopic != null && droppedTopic.getTaxonomy() != null){ 
				droppedTopicTaxonomyName = droppedTopic.getTaxonomy().getName();
			}

			if (StringUtils.isEmpty(droppedTopicTaxonomyName) || !acceptedTaxonomies.contains(droppedTopicTaxonomyName)){
				JSFUtilities.addMessage(null, "content.object.edit.invalid.topic.does.not.belong.to.accepted.taxonomies", null , FacesMessage.SEVERITY_WARN); 
				return;
			}
		}

		//Load existing subject topics
		List<Topic> subjectTopics= ((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).getSimpleTypeValues(); 

		String selectedTopicId = droppedTopic.getId();

		// check if selected topic is already in subject
		boolean topicExists = false;
		for (Topic subjectTopic : subjectTopics) {
			// we check first if topic Id exists because there may be new user tags in the list and new tags do not have an id yet 
			if (subjectTopic.getId() != null && subjectTopic.getId().equals(selectedTopicId)) {
				topicExists = true;
				break;
			}
		}

		if (!topicExists) {
			((TopicReferenceProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.subject")).addSimpleTypeValue(topicTreeNode.getTopic()); 

			JSFUtilities.addMessage(null, "content.object.edit.topic.added.to.content.object", null , FacesMessage.SEVERITY_INFO); 

			updateListOfTopicIdsWhoseNumberOfContentObjectReferrersShouldBeUpdated(topicTreeNode.getTopic().getId());

		}
		else
			JSFUtilities.addMessage(null, "content.object.edit.topic.already.exists", null , FacesMessage.SEVERITY_WARN); 


	}


	public void removeCreator_UIAction(Integer index){
		if (index != null){
			StringProperty creator = (StringProperty)selectedContentObjectForEdit.getContentObjectProperty().get("profile.creator"); 
			if (creator != null)
				creator.removeSimpleTypeValue(index);
		}
	}
	
	public void removeLanguage_UIAction(Integer index){
		if (index != null){
			StringProperty language = (StringProperty)selectedContentObjectForEdit.getContentObjectProperty().get("profile.language"); 
			if (language != null)
				language.removeSimpleTypeValue(index);
		}
	}


	public void addNewCreator_UIAction(){
		if (StringUtils.isNotBlank(newCreatorName)){
			StringProperty creator = (StringProperty)selectedContentObjectForEdit.getContentObjectProperty().get("profile.creator"); 
			if (creator != null)
				creator.addSimpleTypeValue(newCreatorName);
		}

		newCreatorName = null;
	}


	public boolean isThumbnailPropertyDefinedForEditedContentObject(){
		return selectedContentObjectForEdit.getContentObject() != null &&
		selectedContentObjectForEdit.getContentObject().getTypeDefinition() != null &&
		selectedContentObjectForEdit.getContentObject().getTypeDefinition().hasCmsPropertyDefinition("thumbnail"); 
	}
	/*
	 * This method generates a thumbnail from a binary channel of mime type jpeg and
	 * saves outcome to property 'thumbnail', if there is any property defined for this type
	 */
	public void generateThumbnailFromSelectedBinaryChannel_UIAction(Integer indexOfSimpleCmsPropertyWrapper,
			Integer indexOfSimpleCmsPropertyValueWrapper){

		//Check if content object type definition has a 'thumbnail' property
		if (selectedContentObjectForEdit.getContentObject().getTypeDefinition().hasCmsPropertyDefinition("thumbnail")){ 

			//Retrieve simple cms property wrapper which contains binary channel
			SimpleCmsPropertyValueWrapper binaryPropertyValueWrapper = complexCmsPropertyEdit.getSimpleCmsPropertyValueWrapper(indexOfSimpleCmsPropertyWrapper, indexOfSimpleCmsPropertyValueWrapper);
			BinaryChannel binaryChannelWhoseThumbnailWillBeCreated = binaryPropertyValueWrapper.getBinaryChannelValue();

			if (binaryChannelWhoseThumbnailWillBeCreated == null || binaryChannelWhoseThumbnailWillBeCreated.getContent() == null){
				logger.error("Could not find selected binary channel in order to create thumbnail"); 
				JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN); 
				return;
			}

			String mimeType = binaryChannelWhoseThumbnailWillBeCreated.getMimeType();

			//Generate Thumbnail only for specific mimetTypes
			if (mimeType != null && 
					( mimeType.equals("image/jpeg")	|| mimeType.equals("image/gif")	|| mimeType.equals("image/png") || 
							mimeType.equals("image/x-png"))){ 

				BinaryProperty thumbnailProperty = (BinaryProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("thumbnail"); 
				if (thumbnailProperty == null){
					logger.error("Could not find 'thumbnail' property for content object "+selectedContentObjectForEdit.getContentObject().getId()); 
					JSFUtilities.addMessage(null, "application.unknown.error.message",  null, FacesMessage.SEVERITY_WARN); 
					return;
				}

				try {
					// the method name for generating the thumbnail is misleading. It gets a JPEG, PNG or GIF image and generated a thumbnail in PNG format  
					byte[] thumbnailContent = ImageUtils.generateJpegThumbnailHQ(binaryChannelWhoseThumbnailWillBeCreated.getContent(), 128, 256);

					BinaryChannel thumbnailBinaryChannel = thumbnailProperty.getSimpleTypeValue();
					if (thumbnailBinaryChannel == null){
						thumbnailBinaryChannel = cmsRepositoryEntityFactory.newBinaryChannel();
						thumbnailBinaryChannel.setName(thumbnailProperty.getName());
						thumbnailProperty.setSimpleTypeValue(thumbnailBinaryChannel);
					}
					
					// we will append the suffix "-Thumbnail" after the original image file name to generate a name for the (supposed to be) filename of the thumbnail
					thumbnailBinaryChannel.setSourceFilename(binaryChannelWhoseThumbnailWillBeCreated.getSourceFilename().split("\\.")[0] + "-Thumbnail.png");
					thumbnailBinaryChannel.setMimeType("image/png"); 
					thumbnailBinaryChannel.setModified(Calendar.getInstance());
					thumbnailBinaryChannel.setContent(thumbnailContent);
					thumbnailBinaryChannel.setEncoding(binaryChannelWhoseThumbnailWillBeCreated.getEncoding());

					JSFUtilities.addMessage(null, "content.object.edit.thubnail.created", null, FacesMessage.SEVERITY_INFO); 

				} catch (Exception e) {
					logger.error("Could not generate thumbnail ", e); 
					JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN); 
					return;
				}
			}




		}

	}

	public String permanentlyRemoveContentObjectUIAction() {
		final String contentObjectId = selectedContentObjectForEdit.getContentObject().getId();
		// Check if the object has an identifier i.e. it has already been  saved in the repository. We can not remove a non saved object
		if (contentObjectId != null) {
			try {

				Calendar createdDate = ((CalendarProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.created")).getSimpleTypeValue(); 

				contentService.deleteContentObject(contentObjectId);

				Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_DELETED, 
						new Object[]{selectedContentObjectForEdit.getContentObject().getContentObjectType(), contentObjectId, 
						createdDate});
			}
			catch (Exception e) {
				JSFUtilities.addMessage(null, "content.object.edit.contentObjectCouldNotBePermanentlyRemovedError", null, FacesMessage.SEVERITY_ERROR); 
				getLogger().error("The content object could not be permanently deleted. The error is: " , e); 
				return null; 
			}
		}
		else {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN); 
			getLogger().error("permanentlyRemoveContentObjectUIAction(): The content object could not be removed because it has not an identifier. Possibly you have tried to remove an object that has not yet been saved into the repository. This is UI problem that allowed to access this method");	 
			return null; 
		}
		// generate a success message, reset the browsing trees to accommodate the change and finally change the view to show the conentObjectListPanel 
		JSFUtilities.addMessage(null, "content.object.edit.successful.delete.info.message", null, FacesMessage.SEVERITY_INFO); 

		// reset search results table
		ContentObjectStatefulSearchService contentObjectStatefulSearchService = (ContentObjectStatefulSearchService) JSFUtilities.getBeanFromSpringContext("contentObjectStatefulSearchService"); 
		ContentObjectList contentObjectList = (ContentObjectList) JSFUtilities.getBeanFromSpringContext("contentObjectList");
		
		// reset data page and decrease search results count
		contentObjectStatefulSearchService.setSearchResultSetSize(contentObjectStatefulSearchService.getSearchResultSetSize() - 1);
		if (contentObjectStatefulSearchService.getSearchResultSetSize() > 0) {
			contentObjectStatefulSearchService.getReturnedContentObjects().reset();
		}
		else {
			
			contentObjectStatefulSearchService.setReturnedContentObjects(null);
			UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
			if (uiComponentBinding != null){
				uiComponentBinding.setListViewContentObjectTableComponent(null);
				uiComponentBinding.setListViewContentObjectTableScrollerComponent(null);
			}
		}
		
		contentObjectList.setContentObjectListHeaderMessage(null);
		
		// remove object from conversation context
		Contexts.getConversationContext().remove("contentObjectEdit");
		Contexts.getConversationContext().flush();
		return null;
	}

	public void handleWorkFlowActivation(ValueChangeEvent vce) {
		String workflow = (String)vce.getNewValue();
		StringProperty contentObjectStatus = (StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus"); 
		if (workflow != null && workflow.equals("webPublishing")) { 

			//Normally this property should exist.

			if (contentObjectStatus == null){
				//Create property
				contentObjectStatus = (StringProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus"); 
			}

			// set content object status to authored
			contentObjectStatus.setSimpleTypeValue("authored"); 

		}
	}

	public void activatePublicationScheduling_UIAction() {
		// change content object status to "scheduledForPublication"
		// but before save old status in the case the save is not successful in order to put status back in the old value
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		String previousStatus = contentObjectStatusProperty.getSimpleTypeValue();
		contentObjectStatusProperty.setSimpleTypeValue("scheduledForPublication"); 

		// remove it from workflow
		// but before save workflow in the case the save is not successful in order to put workflow back
		StringProperty workflowProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("workflow.managedThroughWorkflow")); 
		String workflow = workflowProperty.getSimpleTypeValue();
		workflowProperty.setSimpleTypeValue(null);

		//Flag to indicate whether publication start date was not found and thus created automatically
		Calendar today = null;
		
		CalendarProperty webPublicationStartDateProperty;

		try{
			if (selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("webPublication")) {
				webPublicationStartDateProperty = (CalendarProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("webPublication.webPublicationStartDate");
			}
			else {
				//Create webPublication aspect
				webPublicationStartDateProperty = (CalendarProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("webPublicationType.webPublicationStartDate"); 
			}
			

			//Check if webPublicationStartDate has a date

			if (webPublicationStartDateProperty.getSimpleTypeValue() == null){
				today = Calendar.getInstance(TimeZoneSelector.instance().getTimeZone(), LocaleSelector.instance().getLocale());
				webPublicationStartDateProperty.setSimpleTypeValue(today);
			}

			String saveActionOutcome= disableCreateVersionAndSaveContentObject();

			if (StringUtils.equals(saveActionOutcome, "error")){ 
				// save was NOT successful restore previous state
				contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
				workflowProperty.setSimpleTypeValue(workflow);
			}
			else{
				JSFUtilities.addMessage(null, "content.object.edit.status.changed.info.message", new String[] {JSFUtilities.getParameterisedStringI18n("content.object.profile.status.scheduledForPublication", null)}, FacesMessage.SEVERITY_INFO); 

				if (today != null)
					JSFUtilities.addMessage(null, "content.object.edit.webpublicationdate.set.info", 
					new String[]{DateUtils.format(today, "dd/MM/yyyy HH:mm")}, FacesMessage.SEVERITY_INFO); 

				// refresh the list of content objects which are submitted for publication
				//ContentObjectSearchByStatus searchByStatusAndPresentation = (ContentObjectSearchByStatus) JSFUtilities.getBeanFromFacesContext("contentObjectSearchByStatusAndPresentation");
				//if (searchByStatusAndPresentation != null)
				//searchByStatusAndPresentation.findContentObjectsSubmittedForWebPublishing_ExpandListener(null);

			}
		}
		catch(Exception e){
			// save was NOT successful restore previous state
			contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			workflowProperty.setSimpleTypeValue(workflow);
			
			getLogger().error("Error while activating publication scheduling ",  e); 
			JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR);
			
		}

	}

	public void submitContentObjectForWebPublication_UIAction() {
		// change content object status to submitted
		// but before save old status in the case the save is not successful in order to put status back in the old value
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		String previousStatus = contentObjectStatusProperty.getSimpleTypeValue();
		contentObjectStatusProperty.setSimpleTypeValue("submitted"); 

		// Check if workflow has been activated
		// but before save workflow in the case the save is not successful in order to put workflow back
		StringProperty workflowProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("workflow.managedThroughWorkflow")); 
		String previousWorkflow = workflowProperty.getSimpleTypeValue();

		if (previousWorkflow == null){
			workflowProperty.setSimpleTypeValue("webPublishing"); 
		}

		try{
			String saveActionOutcome = disableCreateVersionAndSaveContentObject();
			if (StringUtils.equals(saveActionOutcome, "error")) { 
				contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			}
			else {
				// the save was successful
				JSFUtilities.addMessage(null, "content.object.edit.status.changed.info.message", 
						new String[] {JSFUtilities.getParameterisedStringI18n("content.object.profile.status.submitted", null)}, FacesMessage.SEVERITY_INFO); 
			}
		}
		catch(Exception e){
			contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			workflowProperty.setSimpleTypeValue(previousWorkflow);
			getLogger().error("Error while submitting content object for web publication ",  e); 
			JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR);

		}
	}

	public void temporarilyRejectContentObjectForReauthoring_UIAction() {
		// change content object status to temporarilyRejectedForReauthoring
		// but before save old status in the case the save is not successful in order to put status back in the old value
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		String previousStatus = contentObjectStatusProperty.getSimpleTypeValue();
		contentObjectStatusProperty.setSimpleTypeValue(CmsConstants.ContentObjectStatus.temporarilyRejectedForReauthoring.toString()); 

		try{
			String saveActionOutcome = disableCreateVersionAndSaveContentObject();
			if (StringUtils.equals(saveActionOutcome, "error")) { 
				contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			}
			else {
				// the save was successful
				JSFUtilities.addMessage(null, "content.object.edit.status.changed.info.message", new String[] {JSFUtilities.getParameterisedStringI18n("content.object.profile.status.temporarilyRejectedForReauthoring", null)}, FacesMessage.SEVERITY_INFO); 
			}
		}
		catch(Exception e){
			contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			
			getLogger().error("Error while temporarily reject content object for reauthoring",  e); 
			JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR);

		}
	}




	public void publishContentObject_UIAction() {
		// change content object status to null
		// but before save old status in the case the save is not successfull in order to put status back in the old value
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		String previousStatus = contentObjectStatusProperty.getSimpleTypeValue();
		contentObjectStatusProperty.setSimpleTypeValue(CmsConstants.ContentObjectStatus.published.toString());
		
		// remove it from workflow if it is already in a workflow
		// but before save workflow in the case the save is not successful in order to put workflow back
		StringProperty workflowProperty = null;
		String workflow = null;
		if (selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("workflow")) {
			workflowProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("workflow.managedThroughWorkflow")); 
			workflow = workflowProperty.getSimpleTypeValue();
			workflowProperty.setSimpleTypeValue(null);
		}
		
		// if there is a web publication property then set the start date
		boolean publicationStartDateHasBeenSet = false;
		Calendar now = null;
		if (selectedContentObjectForEdit.getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("webPublication")) {
			now = Calendar.getInstance(TimeZoneSelector.instance().getTimeZone(), LocaleSelector.instance().getLocale());
			CalendarProperty webPublicationStartDateProperty = (CalendarProperty)selectedContentObjectForEdit.getContentObject().getCmsProperty("webPublication.webPublicationStartDate");
			webPublicationStartDateProperty.setSimpleTypeValue(now);
			publicationStartDateHasBeenSet = true;
		}
		
		
		try{
			String saveActionOutcome = disableCreateVersionAndSaveContentObject();
			if (StringUtils.equals(saveActionOutcome, "error")) { 
				contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			}
			else {
				// the save was successful
				JSFUtilities.addMessage(null, "content.object.edit.status.changed.info.message", new String[] {JSFUtilities.getParameterisedStringI18n("content.object.profile.status.published", null)}, FacesMessage.SEVERITY_INFO);
				
				if (publicationStartDateHasBeenSet) {
					JSFUtilities.addMessage(null, "content.object.edit.webpublicationdate.set.info", 
							new String[]{DateUtils.format(now, "dd/MM/yyyy HH:mm")}, FacesMessage.SEVERITY_INFO);
				}
			}
		}
		catch(Exception e){
			contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			if (workflowProperty != null) {
				workflowProperty.setSimpleTypeValue(workflow);
			}
			getLogger().error("Error while un-publishing content object ",  e); 
			JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR);

		}

	}
	
	public void unPublishContentObject_UIAction() {
		// change content object status to null
		// but before save old status in the case the save is not successfull in order to put status back in the old value
		StringProperty contentObjectStatusProperty = ((StringProperty) selectedContentObjectForEdit.getContentObject().getCmsProperty("profile.contentObjectStatus")); 
		String previousStatus = contentObjectStatusProperty.getSimpleTypeValue();
		contentObjectStatusProperty.setSimpleTypeValue(null);

		try{
			String saveActionOutcome = disableCreateVersionAndSaveContentObject();
			if (StringUtils.equals(saveActionOutcome, "error")) { 
				contentObjectStatusProperty.setSimpleTypeValue(previousStatus);
			}
			else {
				// the save was successful
				JSFUtilities.addMessage(null, "content.object.edit.status.changed.info.message", new String[] {JSFUtilities.getParameterisedStringI18n("content.object.profile.status.not.defined", null)}, FacesMessage.SEVERITY_INFO); 
			}
		}
		catch(Exception e){
			contentObjectStatusProperty.setSimpleTypeValue(previousStatus);		
			
			getLogger().error("Error while un-publishing content object ",  e); 
			JSFUtilities.addMessage(null, "content.object.edit.save.error", null, FacesMessage.SEVERITY_ERROR);

		}

	}
	
	public String abbreviateString(String stringToAbbreviate, int maxWidth) {
		if (stringToAbbreviate != null) {
			return StringUtils.abbreviate(stringToAbbreviate, maxWidth);
		}
		else {
			return ""; 
		}
	}
	
	
	public List<Map<String, String>> getMatchedPersonsOrRolesUIAction(Object event) {
		try {
			
			List<Map<String,String>> userOrGroupListMap = new ArrayList<Map<String,String>>();
			
			

			String selectedPersonOrRoleTextFilter = event.toString();
			
			//get persons
			List<Person> persons = (List<Person>) identityStoreRunAsSystem.execute("findUsers", new Class<?>[]{String.class}, new Object[]{selectedPersonOrRoleTextFilter});
			
			if (CollectionUtils.isNotEmpty(persons)){
			
				for (Person user : persons){
					Map<String,String> userOrGroupMap = new HashMap<String,String>();
					userOrGroupMap.put("userOrGroupId", user.getUsername()); 
					userOrGroupMap.put("userOrGroupName", StringUtils.isBlank(user.getDisplayName())? user.getUsername() : user.getDisplayName());
					userOrGroupMap.put("label", user.getDisplayName() + " ( "+ user.getUsername() + " ) ");
					
					userOrGroupListMap.add(userOrGroupMap);
				}
			}
			
			//get roles
			List<String> roles = (List<String>) identityStoreRunAsSystem.execute("listRoles", new Class<?>[]{String.class}, new Object[]{selectedPersonOrRoleTextFilter}); 
				
			
			
			if (CollectionUtils.isNotEmpty(roles)){
				
				for (String role : roles){ 
					String displayName = Utils.retrieveDisplayNameForRoleOrPerson(identityStoreRunAsSystem, role);

					Map<String,String> userOrGroupMap = new HashMap<String,String>();
					userOrGroupMap.put("userOrGroupId", role); 
					userOrGroupMap.put("userOrGroupName", displayName); 
					userOrGroupMap.put("label",displayName);
					userOrGroupListMap.add(userOrGroupMap);
				}
			}
			
			
			return userOrGroupListMap;
		} catch (Exception e) {
			logger.error("Error while loading Persons ",e);
			return null;
		}
	
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


	public String getSelectedContentObjectIdentifier() {
		return selectedContentObjectIdentifier;
	}


	public void setSelectedContentObjectIdentifier(
			String selectedContentObjectIdentifier) {
		this.selectedContentObjectIdentifier = selectedContentObjectIdentifier;
	}


	public String getContentObjectTitle() {
		return contentObjectTitle;
	}


	public ContentObjectUIWrapper getSelectedContentObjectForEdit() {
		return selectedContentObjectForEdit;
	}


	/*public TreeNode getContentObjectAsTreeData() {
		return contentObjectAsTreeData;
	}*/

	public String getNewTagLabel() {
		return newTagLabel;
	}


	public void setNewTagLabel(String newTagLabel) {
		this.newTagLabel = newTagLabel;
	}


	public String getContentObjectTypeLocalizedName() {
		return contentObjectTypeLocalizedName;
	}
	
	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}


	/**
	 * @return the radioButtonSelectionMap
	 */
	public Map<String, Integer> getRadioButtonSelectionMap() {
		return radioButtonSelectionMap;
	}

	/**
	 * @return the userListByAccessRight
	 */
	public Map<String, List<Map<String, String>>> getUserListByAccessRight() {
		return userListByAccessRight;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}



	public void setSelectedTopicIdForRemovalFromContentObjectSubject(
			String selectedTopicIdForRemovalFromContentObjectSubject) {
		this.selectedTopicIdForRemovalFromContentObjectSubject = selectedTopicIdForRemovalFromContentObjectSubject;
	}



	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}



	public void setSelectedLoggedInUserTagId(String selectedLoggedInUserTagId) {
		this.selectedLoggedInUserTagId = selectedLoggedInUserTagId;
	}


	public void setContentObjectTypeForNewObject(
			String contentObjectTypeForNewObject) {
		this.contentObjectTypeForNewObject = contentObjectTypeForNewObject;
	}

	public String getContentObjectTypeForNewObject() {
		return contentObjectTypeForNewObject;
	}


	public ConfigurableMimeFileTypeMap getMimeTypesMap() {
		return mimeTypesMap;
	}


	public void setMimeTypesMap(ConfigurableMimeFileTypeMap mimeTypesMap) {
		this.mimeTypesMap = mimeTypesMap;
	}



	public ComplexCmsPropertyEdit getComplexCmsPropertyEdit() {
		return complexCmsPropertyEdit;
	}



	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

	
	public String getNewCreatorName() {
		return newCreatorName;
	}



	public void setNewCreatorName(String newCreatorName) {
		this.newCreatorName = newCreatorName;
	}



	public Space getSpaceToCopyNewObject() {
		return spaceToCopyNewObject;
	}



	public void setSpaceToCopyNewObject(
			Space spaceToCopyNewObject) {
		this.spaceToCopyNewObject = spaceToCopyNewObject;
	}


	public void setSelectedUserOrGroupIdToBeRemovedFromAccessRightList(
			String selectedUserOrGroupIdToBeRemovedFromAccessRightList) {
		this.selectedUserOrGroupIdToBeRemovedFromAccessRightList = selectedUserOrGroupIdToBeRemovedFromAccessRightList;
	}

	public String getComplexCmsPropertyEditorPagePath(){
		return complexCmsPropertyEditorPagePath;
	}


	public List<ComplexCmsPropertyBreadCrumb> getComplexCmsPropertyBreadCrumbs() {
		return complexCmsPropertyBreadCrumbs;
	}


	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}

	public void explicitlySetContentObjectForEdit(
			ContentObject explicitlySetContentObjectForEdit) {
		
		this.explicitlySetContentObjectForEdit = explicitlySetContentObjectForEdit;
		
		if (explicitlySetContentObjectForEdit!= null){
			this.selectedContentObjectIdentifier = explicitlySetContentObjectForEdit.getId();
		}
	}


	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}


	/**
	 * @return the selectedRepositoryUserExternalIdOrLabel
	 */
	public String getSelectedPersonOrRoleTextFilter() {
		return selectedPersonOrRoleTextFilter;
	}


	/**
	 * @param selectedPersonTextFilter the selectedPersonTextFilter to set
	 */
	public void setSelectedPersonOrRoleTextFilter(
			String selectedPersonOrRoleTextFilter) {
		this.selectedPersonOrRoleTextFilter = selectedPersonOrRoleTextFilter;
	}



	public List<SelectItem> getSelectedLanguages() {
		return selectedLanguages;
	}


	public void setSelectedLanguages(List<SelectItem> selectedLanguages) {
		this.selectedLanguages = selectedLanguages;
	}


	public void setUserSpaceNavigation(UserSpaceNavigation userSpaceNavigation) {
		this.userSpaceNavigation = userSpaceNavigation;
	}
	
}

