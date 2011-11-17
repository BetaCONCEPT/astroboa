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
package org.betaconceptframework.astroboa.console.jsf.edit;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.jsf.visitor.CmsPropertyValidatorVisitor;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.security.Identity;

/**
 * Class responsible for crud operations on simple cms properties of
 * a complex cms property of a content object
 *
 */

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ComplexCmsPropertyEdit extends AbstractUIBean {

	private static final long serialVersionUID = 1L;

	//ComplexCmsProperty whose SimpleCmsProperties are edited
	private ComplexCmsProperty<?,?> editedComplexCmsProperty; 

	//Edited CmsProperties
	private List<CmsPropertyWrapper<?>> editedCmsProperties;
	private Map<String, List<CmsPropertyWrapper<?>>> childPropertiesOfSingleValueComplexProperties = new HashMap<String, List<CmsPropertyWrapper<?>>>(); 

	private String fullLocalisedName;

	private ContentService contentService;
	private DefinitionService definitionService; 
	private TaxonomyService taxonomyService;
	private TopicService topicService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	
	private ContentObject editedContentObject;
	
	// different ComplexCmsEditorInstances handle different object editor tabs
	// we use this enum to define the different tabs and use it to define in the 
	// following variable which tab this instance handles
	public enum EditorTab {
		ADMIN_PROPERTY,
		FIXED_PROPERTIES,
		EXTRA_PROPERTIES
	}
	
	private EditorTab editorTab; 
	
	// The list of extra properties that have been added to the edited object instance
	private List<String> extraProperties = null;
	
	// The map holds the property prototypes from which the user may create extra properties
	// per object instance. The extra properties are not defined in object type schema and can be dynamically added per
	// object instance. The property prototypes correspond to complex properties in the schema that are defined outside of object definitions (reusable property types)
	//Key in the map is the prototype property name 
	//the value is a select item with i18n labels for the property name
	private Map<String, SelectItem> propertyPrototypesMap;

	//Exactly the same as above but used for creating the select menu for the UI
	private List<SelectItem> propertyPrototypes;

	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;

	private CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor;
	
	// holds the indexes of property wrappers to be updated when form is re-rendered
	// the indexes of wrappers correspond to indexes of the related UI component that renders the property in the object form. 
	// So the wrapper indexes are the ajaxKeys in the rich faces a4j:repeat component that renders the form   
	private Set<Integer> wrapperIndexesToUpdate;
	
	//@In("ruleEngine")
	//private RuleEngineBean ruleEngine;
	
	private SelectItemComparator selectItemComparator;
	
	public void reloadEditedCmsProperties() {
		//Nullify so that in first visit they will be reloaded
		editedCmsProperties = null;
		propertyPrototypesMap = null;
		propertyPrototypes = null;
		childPropertiesOfSingleValueComplexProperties.clear();
	}

	public String getFullLocalizedName(){
		if (fullLocalisedName == null){
			if (editedComplexCmsProperty != null)
				fullLocalisedName = editedComplexCmsProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(JSFUtilities.getLocaleAsString(), " > "); 
		}

		return fullLocalisedName;
	}

	public void editComplexCmsProperty(ComplexCmsProperty<?,?> editedComplexCmsProperty){

		this.editedComplexCmsProperty = editedComplexCmsProperty; 
		editedCmsProperties = null;
		fullLocalisedName = null;
		propertyPrototypesMap = null;
		propertyPrototypes = null;
		childPropertiesOfSingleValueComplexProperties.clear();
	}

	public List<CmsPropertyWrapper<?>> getEditedCmsProperties(){

		if (editedCmsProperties == null){

			try{
				String localeAsString = JSFUtilities.getLocaleAsString();
				editedCmsProperties = new ArrayList<CmsPropertyWrapper<?>>();
				List<CmsPropertyDefinition> propertyDefinitionList = new ArrayList<CmsPropertyDefinition>();
				
				if (editedComplexCmsProperty != null) {
				
					// We should first assemble a list of all edited properties i.e. the child properties of the edited ComplexCmsProperty 
					Map<String, CmsPropertyDefinition> propertyDefinitions = editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinitions();
	
					
					if (MapUtils.isNotEmpty(propertyDefinitions)){
						propertyDefinitionList.addAll(propertyDefinitions.values());	
					}
					
					// if the root property is edited we should remove the profile and accessibility properties which have dedicated edit forms
					if (editedComplexCmsProperty instanceof ComplexCmsRootProperty && editedContentObject !=null) {
						
						CmsPropertyDefinition profileDefinition = editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile");
						CmsPropertyDefinition accessibilityDefinition = editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("accessibility");
						
						if (profileDefinition != null) {
							propertyDefinitionList.remove(profileDefinition);
						}
							
						if (accessibilityDefinition != null) {
							propertyDefinitionList.remove(accessibilityDefinition);
						}
					}
					
				}
				else if (editorTab.equals(EditorTab.EXTRA_PROPERTIES)) { // in the top level view of extra properties there is no edited property yet
					// lets see if the object instance has any extra properties 
					extraProperties = editedContentObject.getComplexCmsRootProperty().getAspects();

					if (CollectionUtils.isNotEmpty(extraProperties)){
						List<ComplexCmsPropertyDefinition> aspectDefinitionsSortedByLocale = 
							definitionService.getAspectDefinitionsSortedByLocale(extraProperties, localeAsString);
						if (CollectionUtils.isEmpty(aspectDefinitionsSortedByLocale))
							logger.warn("Found no definitions for aspects " + extraProperties); 
						else{
							propertyDefinitionList.addAll(aspectDefinitionsSortedByLocale);

						}
					}
					
					// since we are at the extra properties form we should create the list of available property prototypes
					// so that the user may choose from this list and create a new extra propererty
					loadPropertyPrototypes(localeAsString);
				}
				else {
					logger.error("The edited cms property is null"); 
					JSFUtilities.addMessage(null, "object.edit.load.complex.property.error", null, FacesMessage.SEVERITY_ERROR);
					return null;
				}
				
				//Here we filter the properties according to some rules (security and presentation rules)
				// This filtering is disabled for now since drools engine has some problems to be resolved
				//propertyDefinitionList = ruleEngine.filterDefinitionsForEdit(propertyDefinitionList);
				
				// we should create wrappers for each child property of edited ComplexCmsProperty
				// wrapperIndex is used to partially update the related UI component in the object form
				int wrapperIndex = -1;
				for (CmsPropertyDefinition cmsPropertyDefinition: propertyDefinitionList){
					
					++wrapperIndex;
					
					if (! cmsPropertyDefinition.isObsolete()) {
						createCmsPropertyWrapperForCmsPropertyDefinition(cmsPropertyDefinition, wrapperIndex);
					}
				}

				//sortCmsPropertyWrappers(localeAsString);
				
				// If we are at the extra properties tab, during the creation of wrappers the prototype properties map has been filterd and some prototypes have been excluded. 
				// We have excluded the prototypes that have been already used for creation of an extra property. 
				// So now we will use this filtered map to create a list of the available property prototypes (as select items) 
				// in order to present a selection menu to the user and allow her to create more extra properties
				if (editorTab.equals(EditorTab.EXTRA_PROPERTIES) && propertyPrototypesMap != null){
					
					propertyPrototypes = new ArrayList<SelectItem>(propertyPrototypesMap.values());
					
					if (selectItemComparator == null){
						selectItemComparator = new SelectItemComparator(JSFUtilities.getLocaleAsString());
					}
					
					Collections.sort(propertyPrototypes, selectItemComparator);
				}

				
			}
			catch (Exception e){
				logger.error("An error occured while property wrappers were created for property:" + editedComplexCmsProperty.getPath(), e); 
				JSFUtilities.addMessage(null, "object.edit.load.complex.property.error", null, FacesMessage.SEVERITY_ERROR); 
			}
		}   

		return editedCmsProperties;
	}

	private void sortCmsPropertyWrappers(String localeAsString) {
		//Sort cms property wrappers
		Collections.sort(editedCmsProperties, new CmsPropertyWrapperComparator(localeAsString));
	}

	private void createCmsPropertyWrapperForCmsPropertyDefinition(CmsPropertyDefinition cmsPropertyDefinition, int wrapperIndex) {

		//Check if this property should not be displayed
		if (! shouldCreateAPropertyWrapper(cmsPropertyDefinition))
		{
			return;
		}
		
		switch (cmsPropertyDefinition.getValueType()) {
		case ContentType:
			logger.warn("Found Cms property of type '"+ValueType.ContentType+"' inside complex cms property "+ editedComplexCmsProperty.getFullPath()); 
			break;
		case Complex:{

			if (!cmsPropertyDefinition.isMultiple()){

				boolean isAspect = extraProperties != null && extraProperties.contains(cmsPropertyDefinition.getName());

				CmsProperty<?,?> complexCmsProperty = null;
				//if (editedComplexCmsProperty.isChildPropertyLoaded(cmsPropertyDefinition.getName()))
				// we load the property always since we need to get its child properties
				if (editedComplexCmsProperty != null) {
					complexCmsProperty = editedComplexCmsProperty.getChildProperty(cmsPropertyDefinition.getName());
				}
				else {
					complexCmsProperty = editedContentObject.getCmsProperty(cmsPropertyDefinition.getName());
				}

				//if (complexCmsProperty == null && 
				//	(cmsPropertyDefinition.isMandatory() || isAspect)){
				//	logger.warn("MandatoryComplexCmsProperty "+ cmsPropertyDefinition.getName() + " does not exist in complex cms property "+ editedComplexCmsProperty.getFullPath());
				//}

				//Create complex cms property wrapper.
				//It may be the case that complex cms property is null which happens
				//when complex cms property is a SINGLE, OPTIONAL property
				//Do not call get for single complex cms property. It will be loaded
				//when user wants to edit it
				
				String propertyPath;
				if (editedComplexCmsProperty != null) {
					propertyPath = editedComplexCmsProperty.getPath();
				}
				else { // extra properties does not have a parent cms property
					propertyPath = cmsPropertyDefinition.getName();
				}
				
				editedCmsProperties.add(
						new ComplexCmsPropertyWrapper(
								complexCmsProperty,  
								isAspect,
								cmsPropertyDefinition, 
								propertyPath, 
								cmsRepositoryEntityFactory,
								editedContentObject, wrapperIndex, this));
				
				// create wrappers for child properties
				Map<String, CmsPropertyDefinition> childPropertyDefinitionsMap = ((ComplexCmsProperty<?,?>)complexCmsProperty).getPropertyDefinition().getChildCmsPropertyDefinitions();
				List<CmsPropertyWrapper<?>> singleValueComplexPropertyChildPropertyWrappers = new ArrayList<CmsPropertyWrapper<?>>();
				childPropertiesOfSingleValueComplexProperties.put(complexCmsProperty.getName(), singleValueComplexPropertyChildPropertyWrappers);
				// the wrapper index will be the parent's wrapper index
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitionsMap.values()) {
					createWrapperForChildPropertyOfSingleValueComplexProperty((ComplexCmsProperty<?,?>)complexCmsProperty, childPropertyDefinition, singleValueComplexPropertyChildPropertyWrappers, wrapperIndex);
				}
			}
			else {
				editedCmsProperties.add(new ComplexCmsPropertyParentWrapper(editedComplexCmsProperty,  
						cmsPropertyDefinition, cmsRepositoryEntityFactory, editedContentObject, wrapperIndex, this));
			}
			
			// Filter the prototypes map to remove the prototypes that have been already used 
			// (i.e. there is already an extra property named after the name of the prototype)
			if (editorTab.equals(EditorTab.EXTRA_PROPERTIES) && MapUtils.isNotEmpty(propertyPrototypesMap)) {
				logger.warn("Find existing property '"+cmsPropertyDefinition.getName()+"' having the same name with one of the available property prototypes. Prototype will be removed from list of available prototypes"); 
				propertyPrototypesMap.remove(cmsPropertyDefinition.getName());
			}
			
			break;
		}
		default:{
			//Add simple cms property to list
			//In case the simple property is one of the following use a different way to get or create property template
			CmsProperty<?,?> simpleCmsProperty = null;
			String parentPath = editedComplexCmsProperty.getPath();
			
			simpleCmsProperty = editedComplexCmsProperty.getChildProperty(cmsPropertyDefinition.getName());
			
			if (simpleCmsProperty == null)
				logger.warn("SimpleCmsProperty "+ cmsPropertyDefinition.getName() + " does not exist in complex cms property "+ editedComplexCmsProperty.getFullPath()); 
			else{
				if (cmsPropertyDefinition.getValueType() == ValueType.TopicReference){
					editedCmsProperties.add(
							new TopicPropertyWrapper(
									(TopicReferenceProperty)simpleCmsProperty, 
									CmsCriteriaFactory.newTopicCriteria(),
									cmsPropertyDefinition, 
									parentPath, 
									taxonomyService, 
									topicService, 
									cmsRepositoryEntityFactory,
									editedContentObject, wrapperIndex, this));
				}
				else if (cmsPropertyDefinition.getValueType() == ValueType.ObjectReference){
					editedCmsProperties.add(
							new ContentObjectPropertyWrapper(
									(ObjectReferenceProperty)simpleCmsProperty, 
									CmsCriteriaFactory.newContentObjectCriteria(),
									cmsPropertyDefinition, 
									parentPath, 
									contentService,
									contentObjectUIWrapperFactory, 
									definitionService, 
									cmsRepositoryEntityFactory,
									editedContentObject, wrapperIndex, this));
				}
				else{
					editedCmsProperties.add(new SimpleCmsPropertyWrapper((SimpleCmsProperty)simpleCmsProperty,  
							cmsPropertyDefinition, parentPath, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor, editedContentObject, wrapperIndex, this));
				}
			}
			
			// Perform a check to see if there is a prototype with the same name with this
			// simple property
			if (editorTab.equals(EditorTab.EXTRA_PROPERTIES) && MapUtils.isNotEmpty(propertyPrototypesMap)){
				if (propertyPrototypesMap.containsKey(cmsPropertyDefinition.getName())){
					logger.warn("Find simple property '"+cmsPropertyDefinition.getName()+"' having the same name with one of the available property prototypes. Prototype will be removed from list of available prototypes"); 
					propertyPrototypesMap.remove(cmsPropertyDefinition.getName());
				}
				
			}
			break;
		}
		}
	}
	
	
	private void createWrapperForChildPropertyOfSingleValueComplexProperty(
			ComplexCmsProperty<?,?> parentProperty, 
			CmsPropertyDefinition childPropertyDefinition, 
			List<CmsPropertyWrapper<?>> singleValueComplexPropertyChildPropertyWrappers, int wrapperIndex) {
		
		if (
			parentProperty.getName().equals("accessibility")) {
			return;
		}
		
		//Check if this propert should not be displayed
		if (! shouldCreateAPropertyWrapper(childPropertyDefinition))
		{
			return;
		}
		
		switch (childPropertyDefinition.getValueType()) {
		case ContentType:
			logger.warn("Found Cms property of type '"+ValueType.ContentType+"' inside complex cms property "+ editedComplexCmsProperty.getFullPath()); 
			break;
		case Complex:{

			if (!childPropertyDefinition.isMultiple()){

				CmsProperty<?,?> complexCmsProperty = null;
				if (parentProperty.isChildPropertyLoaded(childPropertyDefinition.getName()))
					complexCmsProperty = parentProperty.getChildProperty(childPropertyDefinition.getName());

				singleValueComplexPropertyChildPropertyWrappers.add(
						new ComplexCmsPropertyWrapper(
								complexCmsProperty,  
								false,
								childPropertyDefinition, 
								parentProperty.getPath(), 
								cmsRepositoryEntityFactory,
								editedContentObject, wrapperIndex, this));

			}
			else {
				singleValueComplexPropertyChildPropertyWrappers.add(new ComplexCmsPropertyParentWrapper(parentProperty,  
						childPropertyDefinition, cmsRepositoryEntityFactory, editedContentObject, wrapperIndex, this));
			}
			
			break;
		}
		default:{
			CmsProperty<?,?> simpleCmsProperty = parentProperty.getChildProperty(childPropertyDefinition.getName());
			String parentPath = parentProperty.getPath();
			
			if (simpleCmsProperty == null)
				logger.warn("SimpleCmsProperty "+ childPropertyDefinition.getName() + " does not exist in complex cms property "+ editedComplexCmsProperty.getFullPath()); 
			else{
				if (childPropertyDefinition.getValueType() == ValueType.TopicReference){
					singleValueComplexPropertyChildPropertyWrappers.add(
							new TopicPropertyWrapper(
									(TopicReferenceProperty)simpleCmsProperty, 
									CmsCriteriaFactory.newTopicCriteria(),
									childPropertyDefinition, 
									parentPath, 
									taxonomyService, 
									topicService, 
									cmsRepositoryEntityFactory,
									editedContentObject, wrapperIndex, this));
				}
				else if (childPropertyDefinition.getValueType() == ValueType.ObjectReference){
					singleValueComplexPropertyChildPropertyWrappers.add(
							new ContentObjectPropertyWrapper(
									(ObjectReferenceProperty)simpleCmsProperty, 
									CmsCriteriaFactory.newContentObjectCriteria(),
									childPropertyDefinition, 
									parentPath, 
									contentService,
									contentObjectUIWrapperFactory, 
									definitionService, 
									cmsRepositoryEntityFactory,
									editedContentObject, wrapperIndex, this));
				}
				else{
					singleValueComplexPropertyChildPropertyWrappers.add(new SimpleCmsPropertyWrapper((SimpleCmsProperty)simpleCmsProperty,  
							childPropertyDefinition, parentPath, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor, editedContentObject, wrapperIndex, this));
				}
			}
				
			break;
		}
		}
	}

	private boolean shouldCreateAPropertyWrapper(
			CmsPropertyDefinition cmsPropertyDefinition) {
		/*
		 * When the following rules apply DO not create a property wrapper for definition. 
		 * That is no input component will be displayed in edit form
		 * 
		 * 1. Logged in user DOES NOT have role CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR
		 * 2. Object Type is "personType" or a subclass of "personType" and property is personAuthorization or any child property of the above
		 * with the namespace http://www.betaconceptframework.org/schema/astroboa/identity/person (built in XSD person-1.0.sxsd)
		 * 3. IdentityStore repository is the same with the repository
		 *  
		 */
		if (! Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR)))
		{
			if (cmsPropertyDefinition.getFullPath() != null &&
					editedContentObject.getTypeDefinition().isTypeOf("personType") &&
					cmsPropertyDefinition.getPath().startsWith("personAuthorization") &&
					cmsPropertyDefinition.getQualifiedName() != null &&
					StringUtils.equals(cmsPropertyDefinition.getQualifiedName().getNamespaceURI(), "http://www.betaconceptframework.org/schema/astroboa/identity/person")
					)
			{
				CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();
				
				if (activeRepository != null &&
						 ( activeRepository.getIdentityStoreRepositoryId() == null ||
								 StringUtils.equals(activeRepository.getIdentityStoreRepositoryId(), activeRepository.getId())
						 )
				 )
				{
					return false;
				}
			}
		}
		
		return true;
	}

	private void loadPropertyPrototypes(String locale) {
		propertyPrototypesMap = new HashMap<String, SelectItem>();

		List<ComplexCmsPropertyDefinition> availablePropertyPrototypes = definitionService.getAvailableAspectDefinitionsSortedByLocale(locale);

		if (CollectionUtils.isNotEmpty(availablePropertyPrototypes)){
			for (ComplexCmsPropertyDefinition propertyPrototype : availablePropertyPrototypes){
				if (!propertyPrototype.isSystemTypeDefinition() && !propertyPrototype.isObsolete() && isUserViewablePropertyPrototype(propertyPrototype.getName())){
					String localizedLabelForLocale = propertyPrototype.getDisplayName().getLocalizedLabelForLocale(locale);
					if (StringUtils.isBlank(localizedLabelForLocale)){
						localizedLabelForLocale = propertyPrototype.getName(); 
					}

					SelectItem availableAspect = new SelectItem(propertyPrototype.getName(), localizedLabelForLocale);
					propertyPrototypesMap.put(propertyPrototype.getName(),  availableAspect);
				}
			}
		}
	}

	private boolean isUserViewablePropertyPrototype(String propertyPrototypeName) {
		if (
			propertyPrototypeName.equals("ruleType") ||
			propertyPrototypeName.equals("arrayOfRuleTypeType") ||
			propertyPrototypeName.equals("arrayOfRuleObjectType") ||
			propertyPrototypeName.equals("administrativeMetadataType") ||
			propertyPrototypeName.equals("accessibilityType") ||
			propertyPrototypeName.equals("cascadingStyleSheetType") ||
			propertyPrototypeName.equals("arrayOfCascadingStyleSheetTypeType") ||
			propertyPrototypeName.equals("arrayOfCascadingStyleSheetObjectType") ||
			propertyPrototypeName.equals("statisticType") ||
			propertyPrototypeName.equals("arrayOfGenericContentResourceObjectType") ||
			propertyPrototypeName.equals("arrayOfScriptTypeType") ||
			propertyPrototypeName.equals("arrayOfScriptObjectType") ||
		//	propertyPrototypeName.equals("arrayOfEmbeddedMultimediaResourceObjectType") ||
		//	propertyPrototypeName.equals("arrayOfFileResourceObjectType") ||
			propertyPrototypeName.equals("arrayOfGeoTagObjectType") ||
		//	propertyPrototypeName.equals("arrayOfOrganizationObjectType") ||
		//	propertyPrototypeName.equals("arrayOfPersonObjectType") ||
		//	propertyPrototypeName.equals("arrayOfWebResourceLinkObjectType") ||
			propertyPrototypeName.equals("organizationJobPositionType") ||
			propertyPrototypeName.equals("arrayOfOrganizationJobPositionTypeType") ||
			propertyPrototypeName.equals("arrayOfOrganizationObjectType") ||
			propertyPrototypeName.equals("departmentType") ||
			propertyPrototypeName.equals("arrayOfDepartmentTypeType") ||
			propertyPrototypeName.equals("personJobPositionType") ||
			propertyPrototypeName.equals("arrayOfPersonJobPositionTypeType")
			) {
			return false;
		}
		
		return true;
	}
	
	public void setEditedComplexCmsProperty(
			ComplexCmsProperty<?,?> editedComplexCmsProperty) {
		this.editedComplexCmsProperty = editedComplexCmsProperty;
	}

	public SimpleCmsPropertyValueWrapper getSimpleCmsPropertyValueWrapper(
			Integer indexOfSimpleCmsPropertyWrapper,
			Integer indexOfSimpleCmsPropertyValueWrapper) {

		if (editedCmsProperties == null){
			getEditedCmsProperties();
		}

		if (editedCmsProperties == null){
			return null;
		}

		SimpleCmsPropertyWrapper simpleCmsPropertyWrapper = (SimpleCmsPropertyWrapper)editedCmsProperties.get(indexOfSimpleCmsPropertyWrapper);

		if (simpleCmsPropertyWrapper == null)
			return null;

		List<SimpleCmsPropertyValueWrapper> simpleCmsPropertyValueWrappers = simpleCmsPropertyWrapper.getSimpleCmsPropertyValueWrappers();

		if (indexOfSimpleCmsPropertyValueWrapper == null || indexOfSimpleCmsPropertyValueWrapper == -1){
			//New value must be added to the end of list
			simpleCmsPropertyWrapper.addBlankValue_UIAction();
			//Return the newly added element
			return simpleCmsPropertyWrapper.getSimpleCmsPropertyValueWrappers().get(simpleCmsPropertyWrapper.getSimpleCmsPropertyValueWrappers().size()-1);
		}
		else if (indexOfSimpleCmsPropertyValueWrapper == 0 && CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			//Create a blank value since simple cms property has not been initialized
			simpleCmsPropertyWrapper.addBlankValue_UIAction();
		}

		return simpleCmsPropertyWrapper.getSimpleCmsPropertyValueWrappers().get(indexOfSimpleCmsPropertyValueWrapper);


	}
	
	public Integer getIndexOfPropertyWrapper(String propertyPath) {
		if (StringUtils.isBlank(propertyPath)) {
			return -1;
		}
		
		if (editedCmsProperties == null){
			getEditedCmsProperties();
		}

		if (editedCmsProperties == null){
			return -1;
		}
		
		for (CmsPropertyWrapper<?> cmsPropertyWrapper : editedCmsProperties) {
			if (cmsPropertyWrapper.getPath().equals(propertyPath)) {
				return cmsPropertyWrapper.wrapperIndex;
			}
		}
		
		return -1;
	}

	public ComplexCmsProperty<?,?> getEditedComplexCmsProperty() {
		return editedComplexCmsProperty;
	}



	public List<SelectItem> getPropertyPrototypes() {
		if (propertyPrototypes == null) {
			// we need to process the edited  extra properties in order to calculate
			// which prototypes should be available to the user
			getEditedCmsProperties();
		}
		return propertyPrototypes;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}


	public void setEditedContentObject(ContentObject editedContentObject) {
		this.editedContentObject = editedContentObject;
	}

	public void setTaxonomyService(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}

	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}

	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}

	/**
	 * @param cmsPropertyValidatorVisitor the cmsPropertyValidatorVisitor to set
	 */
	public void setCmsPropertyValidatorVisitor(
			CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor) {
		this.cmsPropertyValidatorVisitor = cmsPropertyValidatorVisitor;
	}

	public Map<String, List<CmsPropertyWrapper<?>>> getChildPropertiesOfSingleValueComplexProperties() {
		return childPropertiesOfSingleValueComplexProperties;
	}

	public Set<Integer> getWrapperIndexesToUpdate() {
		return wrapperIndexesToUpdate;
	}

	public void setWrapperIndexesToUpdate(Set<Integer> wrapperIndexesToUpdate) {
		this.wrapperIndexesToUpdate = wrapperIndexesToUpdate;
	}

	public EditorTab getEditorTab() {
		return editorTab;
	}

	public void setEditorTab(EditorTab editorTab) {
		this.editorTab = editorTab;
	}


	
}
