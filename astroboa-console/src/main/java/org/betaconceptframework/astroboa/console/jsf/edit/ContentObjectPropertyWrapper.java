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
package org.betaconceptframework.astroboa.console.jsf.edit;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.jsf.clipboard.ContentObjectItem;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.event.DropEvent;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectPropertyWrapper extends MultipleSimpleCmsPropertyWrapper<ObjectReferenceProperty>{

	private String contentObjectTitlePattern;

	private ContentObjectCriteria contentObjectCriteria;
	private List<String> acceptedContentTypes;

	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private DefinitionService definitionService;
	private String localizedLabelsForAcceptedTypes = null;

	public ContentObjectPropertyWrapper(ObjectReferenceProperty contentObjectProperty,
			ContentObjectCriteria contentObjectCriteria,
			CmsPropertyDefinition cmsPropertyDefinition,
			String parentCmsPropertyPath, 
			ContentService contentService, 
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory, 
			DefinitionService definitionService, 
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory,
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {

		super(cmsPropertyDefinition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject, wrapperIndex, complexCmsPropertyEdit);

		cmsProperty = contentObjectProperty;
		this.contentService = contentService;
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
		this.definitionService = definitionService;

		//Initialize topic criteria
		this.contentObjectCriteria = contentObjectCriteria;

		if (getCmsPropertyDefinition() != null)
			acceptedContentTypes = ((ObjectReferencePropertyDefinition)getCmsPropertyDefinition()).getExpandedAcceptedContentTypes();

		if (CollectionUtils.isEmpty(acceptedContentTypes)){
			//Load all content object types
			acceptedContentTypes = definitionService.getContentObjectTypes();
			//Add a default
			if (acceptedContentTypes != null){
				acceptedContentTypes.add(ValueType.ObjectReference.toString());
			}
		}

		//this.contentObjectCriteria.getRenderProperties().renderValuesForLocale(JSFUtilities.getLocaleAsString());
		this.contentObjectCriteria.setOffsetAndLimit(0,15);
		this.contentObjectCriteria.addOrderProperty("profile.title", Order.ascending);

	}

	public List<SimpleCmsPropertyValueWrapper> getSimpleCmsPropertyValueWrappers(){
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			//Create wrappers only if there are any values
			if (cmsProperty != null && CollectionUtils.isNotEmpty(cmsProperty.getSimpleTypeValues())){
				List values = cmsProperty.getSimpleTypeValues();
				for (int i=0; i<values.size(); i++){
					simpleCmsPropertyValueWrappers.add(new SimpleCmsPropertyValueWrapper(cmsProperty, i, cmsRepositoryEntityFactory, null));
				}
			}
		}

		return simpleCmsPropertyValueWrappers;
	}

	public void deleteValueFromContentObjectProperty_UIAction(){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		//Remove value only it has not already been deleted in case of null value
		if (indexOfPropertyValueToBeProcessed != -1){

			try{
				if (cmsProperty.getPropertyDefinition().isMultiple()){
					//Remove value from simple cms property
					//only if indexOfPropertyValueToBeProcessed exists for values
					if (indexOfPropertyValueToBeProcessed < cmsProperty.getSimpleTypeValues().size()){
						cmsProperty.removeSimpleTypeValue(indexOfPropertyValueToBeProcessed);
					}
				}
				else{
					cmsProperty.setSimpleTypeValue(null);
				}
			}
			catch (Exception e){
				logger.error("",e);
			}
			finally{
				//Reset first wrapper
				simpleCmsPropertyValueWrappers.clear();

				indexOfPropertyValueToBeProcessed = -1;
			}

		}
	}


	public List<ContentObjectUIWrapper> findContentObjects_UIAction(Object event) {
		try {

			String selectedContentObjectTitle = event.toString();

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(selectedContentObjectTitle))
				return null;

			//Reset criteria
			contentObjectCriteria.reset();

			//Profile Title criterion
			if (StringUtils.deleteWhitespace(selectedContentObjectTitle).equals(selectedContentObjectTitle) && ! selectedContentObjectTitle.contains("\"") && ! selectedContentObjectTitle.contains("'") && ! selectedContentObjectTitle.contains("*")){
				//If Search Text contains only one word and not any special search character then Append * at the end
				contentObjectCriteria.addCriterion(CriterionFactory.contains("profile.title", CmsConstants.ANY_NAME + selectedContentObjectTitle + CmsConstants.ANY_NAME));
			}
			else{
				contentObjectCriteria.addCriterion(CriterionFactory.contains("profile.title", selectedContentObjectTitle));
			}

			//ContentObject Types criterion
			if (CollectionUtils.isNotEmpty(acceptedContentTypes)){
				contentObjectCriteria.addContentObjectTypesEqualsAnyCriterion(acceptedContentTypes);
			}


			CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();

			if (cmsOutcome.getCount() > 0) {
				List<ContentObject> objects = cmsOutcome.getResults();

				for (ContentObject object : objects) {
					wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(object));
				}
			}

			return wrappedContentObjects;

		} catch (Exception e) {
			logger.error("Error while loading Content Objects ",e);
			return null;
		}

	}

	public void addDraggedAndDroppedContentObject_Listener(DropEvent dropEvent) {
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		ContentObjectItem contentObjectItem = (ContentObjectItem) dropEvent.getDragValue();
		String dragType = dropEvent.getDragType();

		//Only content object is supported
		if (getCmsPropertyDefinition() != null && 
				getCmsPropertyDefinition() instanceof ObjectReferencePropertyDefinition && 
				StringUtils.isNotBlank(dragType)){ //Drag type not used for now

			//A Content Object has been dragged
			//Load Content object from repository
			ContentObject contentObject = contentService.getContentObject(contentObjectItem.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, null, null, false);

			if (contentObject == null)
				JSFUtilities.addMessage(null, "Δεν υπάρχει Αντικείμενο με αναγνωριστικό "+ contentObjectItem.getId() , FacesMessage.SEVERITY_WARN);
			else{

				addSelectedContentObject_UIAction(contentObject, false);
			}

		}
	}

	public void addSelectedContentObject_UIAction(ContentObject selectedContentObject, boolean checkAcceptedContentObjectTypes){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		if (cmsProperty != null && getCmsPropertyDefinition() != null && 
				selectedContentObject != null){
			List<ContentObject> contentObjects= new ArrayList<ContentObject>();
			if (getCmsPropertyDefinition().isMultiple()){
				contentObjects = cmsProperty.getSimpleTypeValues();
			}
			else{
				if (cmsProperty.getSimpleTypeValue() != null)
					contentObjects.add(cmsProperty.getSimpleTypeValue());

			}
			String selectedContentObjectId = selectedContentObject.getId();

			// check if selected content object is already inserted
			boolean contentObjectExists = false;
			for (ContentObject contentObject : contentObjects) {
				if (contentObject.getId() != null && contentObject.getId().equals(selectedContentObjectId)) {
					contentObjectExists = true;
					break;
				}
			}

			if (!contentObjectExists) {
				//Check that content object type is accepted
				if (CollectionUtils.isNotEmpty(acceptedContentTypes) && checkAcceptedContentObjectTypes){
					if (selectedContentObject.getContentObjectType() == null ||
							! acceptedContentTypes.contains(selectedContentObject.getContentObjectType())
					){
						JSFUtilities.addMessage(null, "Ο τύπος τoυ Αντικειμένου με αναγνωριστικό "+ selectedContentObjectId  +" δεν ανήκει στους επιτρεπόμενους τύπους του στοιχείου "
								+ acceptedContentTypes , FacesMessage.SEVERITY_WARN);
						return;
					}

				}

				if (isMultiple()){
					cmsProperty.addSimpleTypeValue(selectedContentObject);
					simpleCmsPropertyValueWrappers.clear();
				}
				else{
					//Now replace value
					cmsProperty.setSimpleTypeValue(selectedContentObject);
				}

			}
			else
				JSFUtilities.addMessage(null, "Το αντικείμενο ΔΕΝ ΠΡΟΣΤΕΘΗΚΕ γιατί υπάρχει ήδη στο στοιχείο" , FacesMessage.SEVERITY_WARN);
		}
	}

	@Override
	public void addBlankValue_UIAction() {


	}

	public void setContentObjectTitlePattern(String contentObjectTitlePattern) {
		this.contentObjectTitlePattern = contentObjectTitlePattern;
	}

	public String getContentObjectTitlePattern() {
		return contentObjectTitlePattern;
	}

	public List<String> getAcceptedContentTypes() {
		if (acceptedContentTypes == null){
			return definitionService.getContentObjectTypes();
		}
		return acceptedContentTypes;
	}


	public String getLocalizedLabelsForAcceptedContentTypes(){
		if (StringUtils.isBlank(localizedLabelsForAcceptedTypes)){
			List<String> acceptedContentTypes = null;
			List<String> localizedLabels = new ArrayList<String>();
			// we do not get the accepted object types from the provided class property since we do not need to return to the user all available types 
			// if no restriction has been set in the definition. When there is no rescriction we just return a localized string for "All Object Types are accepted"
			if (getCmsPropertyDefinition() != null) {
				acceptedContentTypes = ((ObjectReferencePropertyDefinition)getCmsPropertyDefinition()).getExpandedAcceptedContentTypes();
			}

			if (CollectionUtils.isNotEmpty(acceptedContentTypes)){

				for (String acceptedContentType : acceptedContentTypes){
					
					ContentObjectTypeDefinition typeDefinition = (ContentObjectTypeDefinition)definitionService.getCmsDefinition(acceptedContentType, ResourceRepresentationType.DEFINITION_INSTANCE, false);
	
					if (typeDefinition == null){
						logger.warn("Try to load accepted content type {} but was not found", acceptedContentType);
						localizedLabels.add(acceptedContentType);
					}
					else{
						if (StringUtils.isNotBlank(typeDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))){
							localizedLabels.add(typeDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString())); 
						}
						else{
							localizedLabels.add(acceptedContentType);
						}
					}
				}
				
				
			}
			else {
				localizedLabels.add(JSFUtilities.getStringI18n("dialog.objectSelection.allObjectTypesAreAccepted"));
			}
			
			if (localizedLabels.size() > 0){
				localizedLabelsForAcceptedTypes = StringUtils.join(localizedLabels, ",");
			}

		}

		return localizedLabelsForAcceptedTypes;
	}

}
