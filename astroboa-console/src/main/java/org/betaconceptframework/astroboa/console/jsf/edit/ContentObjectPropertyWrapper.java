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
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
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

	
	private int indexOfValueToBeDeleted = -1;

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
			ContentObject contentObject) {

		super(cmsPropertyDefinition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject);

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

		this.contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
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
		//Remove value only it has not already been deleted in case of null value
		if (indexOfValueToBeDeleted != -1){

			try{
				if (cmsProperty.getPropertyDefinition().isMultiple()){
					//Remove value from simple cms property
					//only if indexOfvalueTobeDeleted exists for values
					if (indexOfValueToBeDeleted < cmsProperty.getSimpleTypeValues().size()){
						cmsProperty.removeSimpleTypeValue(indexOfValueToBeDeleted);
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

				indexOfValueToBeDeleted = -1;
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
				this.contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
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
		ContentObjectItem contentObjectItem = (ContentObjectItem) dropEvent.getDragValue();
		String dragType = dropEvent.getDragType();

		//Only content object is supported
		if (getCmsPropertyDefinition() != null && 
				getCmsPropertyDefinition() instanceof ObjectReferencePropertyDefinition && 
				StringUtils.isNotBlank(dragType)){ //Drag type not used for now

			//A Content Object has been dragged
			//Load Content object from repository
			ContentObject contentObject = contentService.getContentObjectByIdAndLocale(contentObjectItem.getId(), JSFUtilities.getLocaleAsString(), null);

			if (contentObject == null)
				JSFUtilities.addMessage(null, "Δεν υπάρχει Αντικείμενο με αναγνωριστικό "+ contentObjectItem.getId() , FacesMessage.SEVERITY_WARN);
			else{

				addSelectedContentObject_UIAction(contentObject, false);
			}

		}
	}

	public void addSelectedContentObject_UIAction(ContentObject selectedContentObject, boolean checkAcceptedContentObjectTypes){

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

	public void setIndexOfValueToBeDeleted(int indexOfValueToBeDeleted) {
		this.indexOfValueToBeDeleted = indexOfValueToBeDeleted;
	}

	public List<String> getAcceptedContentTypes() {
		if (acceptedContentTypes == null){
			return definitionService.getContentObjectTypes();
		}
		return acceptedContentTypes;
	}


	public String getLocalizedLabelsForAcceptedContentTypes(){
		if (StringUtils.isBlank(localizedLabelsForAcceptedTypes)){

			if (CollectionUtils.isNotEmpty(acceptedContentTypes)){

				List<String> localizedLabels = new ArrayList<String>();

				for (String acceptedContentType : acceptedContentTypes){
					if (!ValueType.ObjectReference.toString().equals(acceptedContentType)){
						ContentObjectTypeDefinition typeDefinition = definitionService.getContentObjectTypeDefinition(acceptedContentType);

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

				if (localizedLabels.size() > 0){
					localizedLabelsForAcceptedTypes = StringUtils.join(localizedLabels, ",");
				}

			}
		}

		return localizedLabelsForAcceptedTypes;
	}

}
