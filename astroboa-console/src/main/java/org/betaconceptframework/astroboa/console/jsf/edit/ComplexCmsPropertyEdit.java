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
import org.betaconceptframework.astroboa.console.jsf.rule.RuleEngineBean;
import org.betaconceptframework.astroboa.console.jsf.visitor.CmsPropertyValidatorVisitor;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

/**
 * Class responsible for crud operations on simple cms properties of
 * a complex cms property of a content object
 *
 */
@Name("complexCmsPropertyEdit")
@Scope(ScopeType.CONVERSATION)
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


	//Key is aspect name 
	//Value is aspect localized labels
	private Map<String, SelectItem> availableAspectsPerName;

	//Exactly the same as before but used for displaying reasons
	private List<SelectItem> availableAspects;

	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;

	private CmsPropertyValidatorVisitor cmsPropertyValidatorVisitor;
	
	@In("ruleEngine")
	private RuleEngineBean ruleEngine;
	
	private SelectItemComparator selectItemComparator;
	
	public void reloadEditedCmsProperties() {
		//Nullify so that in first visit they will be reloaded
		editedCmsProperties = null;
		availableAspectsPerName = null;
		availableAspects = null;
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
		availableAspectsPerName = null;
		availableAspects = null;
		childPropertiesOfSingleValueComplexProperties.clear();
	}

	public List<CmsPropertyWrapper<?>> getEditedCmsProperties(){

		if (editedCmsProperties == null && editedComplexCmsProperty != null){

			try{

				String localeAsString = JSFUtilities.getLocaleAsString();

				//Load available Aspects
				if (editedComplexCmsProperty instanceof ComplexCmsRootProperty){
					loadAvailableAspects(localeAsString);
				}

				//create wrappers for child properties of ComplexCmsProperty
				Map<String, CmsPropertyDefinition> propertyDefinitions = editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinitions();

				editedCmsProperties = new ArrayList<CmsPropertyWrapper<?>>();

				List<CmsPropertyDefinition> propertyDefinitionList = new ArrayList<CmsPropertyDefinition>();
				
				CmsPropertyDefinition profileDefinition = null;
				
				//Check for aspects
				List<String> aspects = null;
				if (editedComplexCmsProperty instanceof ComplexCmsRootProperty && editedContentObject !=null) {
					profileDefinition = editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile");
					
					/*
					// We explicitly add the title, description and subject to the root form although they belong to the profile form
					if (editedComplexCmsProperty.isChildPropertyDefined("profile.title")) { 
						propertyDefinitionList.add(editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile.title")); 
					}

					if (editedComplexCmsProperty.isChildPropertyDefined("profile.description")) { 
						propertyDefinitionList.add(editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile.description")); 
					}

					if (editedComplexCmsProperty.isChildPropertyDefined("profile.subject")) { 
						propertyDefinitionList.add(editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile.subject")); 
					}
					*/
					
					/*Just load all other mandatory profile properties
					if(editedComplexCmsProperty.isChildPropertyDefined("profile")) { 
						ComplexCmsPropertyDefinition profileDefinition = (ComplexCmsPropertyDefinition)editedComplexCmsProperty.getPropertyDefinition().getChildCmsPropertyDefinition("profile"); 
						
						if (profileDefinition != null && profileDefinition.getChildCmsPropertyDefinitions() != null)
						for (CmsPropertyDefinition profileChildPropertyDefinition : profileDefinition.getChildCmsPropertyDefinitions().values()){
							if (profileChildPropertyDefinition.isMandatory()){
								editedComplexCmsProperty.getChildProperty(profileChildPropertyDefinition.getPath());
							}
						}
					}
					*/
					// Root element has been chosen to be edited
					aspects = editedContentObject.getComplexCmsRootProperty().getAspects();

					//Append list with aspect definitions, if any
					if (CollectionUtils.isNotEmpty(aspects)){
						//Sorting is not needed at this point but there is no other method available
						List<ComplexCmsPropertyDefinition> aspectDefinitionsSortedByLocale = 
							definitionService.getAspectDefinitionsSortedByLocale(aspects, localeAsString);
						if (CollectionUtils.isEmpty(aspectDefinitionsSortedByLocale))
							logger.warn("Found no definitions for aspects " + aspects); 
						else{
							propertyDefinitionList.addAll(aspectDefinitionsSortedByLocale);

						}
					}

				}
				
				if (MapUtils.isNotEmpty(propertyDefinitions)){
					propertyDefinitionList.addAll(propertyDefinitions.values());
					// remove profile in order to add it as the first wrapper
					if (profileDefinition != null) {
						propertyDefinitionList.remove(profileDefinition);
						
						createCmsPropertyWrapperForCmsPropertyDefinition(
								aspects,
								profileDefinition);
					}
					
				}


				//This is disabled for now
				//propertyDefinitionList = ruleEngine.filterDefinitionsForEdit(propertyDefinitionList);
				
				List<CmsPropertyDefinition> definitionsToDisplayLast = new ArrayList<CmsPropertyDefinition>();
				for (CmsPropertyDefinition cmsPropertyDefinition: propertyDefinitionList){
					
					if (! cmsPropertyDefinition.isObsolete())
					{
					if ("accessibility".equals(cmsPropertyDefinition.getName())){
						definitionsToDisplayLast.add(cmsPropertyDefinition);
					}
					else{
						createCmsPropertyWrapperForCmsPropertyDefinition(
								aspects,
								cmsPropertyDefinition);
					}
					}
				}
				
				for (CmsPropertyDefinition cmsPropertyDefinition: definitionsToDisplayLast){
						createCmsPropertyWrapperForCmsPropertyDefinition(
								aspects,
								cmsPropertyDefinition);
				}

				//sortCmsPropertyWrappers(localeAsString);   

				//Set which aspects will be displayed as Available
				if (availableAspectsPerName != null){
					availableAspects = new ArrayList<SelectItem>(availableAspectsPerName.values());
					
					if (selectItemComparator == null){
						selectItemComparator = new SelectItemComparator(localeAsString);
					}
					
					Collections.sort(availableAspects, selectItemComparator);
				}
			}
			catch (Exception e){
				logger.error("",e); 
				JSFUtilities.addMessage(null, "content.object.edit.load.complex.property.error", null, FacesMessage.SEVERITY_ERROR); 
			}
		}   

		return editedCmsProperties;
	}

	private void sortCmsPropertyWrappers(String localeAsString) {
		//Sort cms property wrappers
		Collections.sort(editedCmsProperties, new CmsPropertyWrapperComparator(localeAsString));
	}

	private void createCmsPropertyWrapperForCmsPropertyDefinition(
			List<String> aspects,
			CmsPropertyDefinition cmsPropertyDefinition) {

		//Check if this propert should not be displayed
		if (! shouldCreateAPropertyWrapper(cmsPropertyDefinition))
		{
			return;
		}
		
		if (aspects == null)
		{
			aspects = editedContentObject.getComplexCmsRootProperty().getAspects();
		}


		switch (cmsPropertyDefinition.getValueType()) {
		case ContentType:
			logger.warn("Found Cms property of type '"+ValueType.ContentType+"' inside complex cms property "+ editedComplexCmsProperty.getFullPath()); 
			break;
		case Complex:{

			if (!cmsPropertyDefinition.isMultiple()){

				boolean isAspect = aspects != null && aspects.contains(cmsPropertyDefinition.getName());

				CmsProperty complexCmsProperty = null;
				//if (editedComplexCmsProperty.isChildPropertyLoaded(cmsPropertyDefinition.getName()))
				// we load the property always since we need to get its child properties
				complexCmsProperty = editedComplexCmsProperty.getChildProperty(cmsPropertyDefinition.getName());

				//if (complexCmsProperty == null && 
				//	(cmsPropertyDefinition.isMandatory() || isAspect)){
				//	logger.warn("MandatoryComplexCmsProperty "+ cmsPropertyDefinition.getName() + " does not exist in complex cms property "+ editedComplexCmsProperty.getFullPath());
				//}

				//Create complex cms property wrapper.
				//It may be the case that complex cms property is null which happens
				//when complex cms property is a SINGLE, OPTIONAL property
				//Do not call get for single complex cms property. It will be loaded
				//when user wants to edit it
				editedCmsProperties.add(
						new ComplexCmsPropertyWrapper(
								complexCmsProperty,  
								isAspect,
								cmsPropertyDefinition, 
								editedComplexCmsProperty.getPath(), 
								cmsRepositoryEntityFactory,
								editedContentObject));
				
				// create wrappers for child properties
				Map<String, CmsPropertyDefinition> childPropertyDefinitionsMap = ((ComplexCmsProperty<?,?>)complexCmsProperty).getPropertyDefinition().getChildCmsPropertyDefinitions();
				List<CmsPropertyWrapper<?>> singleValueComplexPropertyChildPropertyWrappers = new ArrayList<CmsPropertyWrapper<?>>();
				childPropertiesOfSingleValueComplexProperties.put(complexCmsProperty.getName(), singleValueComplexPropertyChildPropertyWrappers);
				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitionsMap.values()) {
					createWrapperForChildPropertyOfSingleValueComplexProperty((ComplexCmsProperty<?,?>)complexCmsProperty, childPropertyDefinition, singleValueComplexPropertyChildPropertyWrappers);
				}
			}
			else {
				editedCmsProperties.add(new ComplexCmsPropertyParentWrapper(editedComplexCmsProperty,  
						cmsPropertyDefinition, cmsRepositoryEntityFactory, editedContentObject));
			}
			
			//TODO: Fix this by checking the type of each property not by checking the name
			//Remove any cms property from aspect selection 
			if (MapUtils.isNotEmpty(availableAspectsPerName)) {
				if (cmsPropertyDefinition.getName().equals("accessibility") ||
						cmsPropertyDefinition.getName().equals("webPublication") ||
						cmsPropertyDefinition.getName().equals("statistic") ||
						cmsPropertyDefinition.getName().equals("workflow")) {
					availableAspectsPerName.remove(cmsPropertyDefinition.getName() + "Type");
				}
				else if (cmsPropertyDefinition.getName().equals("profile")) {
					availableAspectsPerName.remove("administrativeMetadataType");
				}
				else {
					availableAspectsPerName.remove(cmsPropertyDefinition.getName());
				}
			}
			
			
				
			break;
		}
		default:{
			//Add simple cms property to list
			//In case the simple property is one of the following use a different way to get or create property template
			CmsProperty<?,?> simpleCmsProperty = null;
			String parentPath = editedComplexCmsProperty.getPath();
			
			/*
			if ("profile.title".equals(cmsPropertyDefinition.getPath()) || 
					"profile.description".equals(cmsPropertyDefinition.getPath()) || 
					"profile.subject".equals(cmsPropertyDefinition.getPath()) ){ 
				
				simpleCmsProperty = editedComplexCmsProperty.getChildProperty(cmsPropertyDefinition.getPath());
				
				if (simpleCmsProperty != null){
					parentPath = simpleCmsProperty.getParentProperty().getPath();
				}
					
			}
			else{
			
				
			}
			*/
			
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
									editedContentObject));
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
									editedContentObject));
				}
				else{
					editedCmsProperties.add(new SimpleCmsPropertyWrapper((SimpleCmsProperty)simpleCmsProperty,  
							cmsPropertyDefinition, parentPath, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor, editedContentObject));
				}
			}
			
			//Perform a check to see if there is an aspect with the same name with this
			//simple property
			if (MapUtils.isNotEmpty(availableAspectsPerName)){
				if (availableAspectsPerName.containsKey(cmsPropertyDefinition.getName())){
					logger.warn("Find simple property '"+cmsPropertyDefinition.getName()+"' having the same name with an aspect. Aspect will be removed from available list"); 
					availableAspectsPerName.remove(cmsPropertyDefinition.getName());
				}
				
			}
			break;
		}
		}
	}
	
	
	private void createWrapperForChildPropertyOfSingleValueComplexProperty(
			ComplexCmsProperty<?,?> parentProperty, 
			CmsPropertyDefinition childPropertyDefinition, 
			List<CmsPropertyWrapper<?>> singleValueComplexPropertyChildPropertyWrappers) {
		
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
								editedContentObject));

			}
			else {
				singleValueComplexPropertyChildPropertyWrappers.add(new ComplexCmsPropertyParentWrapper(parentProperty,  
						childPropertyDefinition, cmsRepositoryEntityFactory, editedContentObject));
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
									editedContentObject));
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
									editedContentObject));
				}
				else{
					singleValueComplexPropertyChildPropertyWrappers.add(new SimpleCmsPropertyWrapper((SimpleCmsProperty)simpleCmsProperty,  
							childPropertyDefinition, parentPath, cmsRepositoryEntityFactory, cmsPropertyValidatorVisitor, editedContentObject));
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

	private void loadAvailableAspects(String locale) {
		availableAspectsPerName = new HashMap<String, SelectItem>();

		List<ComplexCmsPropertyDefinition> availabelAspectDefinitions = definitionService.getAvailableAspectDefinitionsSortedByLocale(locale);

		if (CollectionUtils.isNotEmpty(availabelAspectDefinitions)){
			for (ComplexCmsPropertyDefinition aspectDefinition : availabelAspectDefinitions){
				if (!aspectDefinition.isSystemTypeDefinition() && !aspectDefinition.isObsolete()){
					String localizedLabelForLocale = aspectDefinition.getDisplayName().getLocalizedLabelForLocale(locale);
					if (StringUtils.isBlank(localizedLabelForLocale)){
						localizedLabelForLocale = aspectDefinition.getName(); 
					}

					SelectItem availableAspect = new SelectItem(aspectDefinition.getName(), localizedLabelForLocale);
					availableAspectsPerName.put(aspectDefinition.getName(),  availableAspect);
				}
			}
		}
	}

	public void setEditedComplexCmsProperty(
			ComplexCmsProperty editedComplexCmsProperty) {
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

	public ComplexCmsProperty getEditedComplexCmsProperty() {
		return editedComplexCmsProperty;
	}



	public List<SelectItem> getAvailableAspects() {
		return availableAspects;
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


	
}
