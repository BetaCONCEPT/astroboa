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
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.commons.comparator.TopicLocalizedLabelComparator;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.core.Events;
import org.richfaces.event.DropEvent;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicPropertyWrapper extends MultipleSimpleCmsPropertyWrapper<TopicReferenceProperty>{

	private String topicLabelPattern;

	private TopicCriteria topicCriteria;
	private List<String> acceptedTaxonomies;

	private TopicService topicService;
	
	private String localizedLabelsForAcceptedTaxonomies;

	public TopicPropertyWrapper(TopicReferenceProperty topicProperty,
			TopicCriteria topicCriteria,
			CmsPropertyDefinition cmsPropertyDefinition,
			String parentCmsPropertyPath, 
			TaxonomyService taxonomyService,
			TopicService topicService,
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory,
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		super(cmsPropertyDefinition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject, wrapperIndex, complexCmsPropertyEdit);

		cmsProperty = topicProperty;
		this.topicService = topicService;

		//Initialize topic criteria
		this.topicCriteria = topicCriteria;

		if (getCmsPropertyDefinition() != null){
			acceptedTaxonomies = ((TopicReferencePropertyDefinition)getCmsPropertyDefinition()).getAcceptedTaxonomies();
			
			if (CollectionUtils.isNotEmpty(acceptedTaxonomies)){
				
				List<String> localizedLabels = new ArrayList<String>();
				//Load localized Labels for all taxonomies
				for (String acceptedTaxonomyName : acceptedTaxonomies){
					Taxonomy acceptedTaxonomy = taxonomyService.getTaxonomy(acceptedTaxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
					
					if (acceptedTaxonomy == null){
						logger.warn("Try to load accepted taxonomy {} but was not found", acceptedTaxonomyName);
						localizedLabels.add(acceptedTaxonomyName);
					}
					else{
						if (StringUtils.isNotBlank(acceptedTaxonomy.getLocalizedLabelForCurrentLocale())){
							localizedLabels.add(acceptedTaxonomy.getLocalizedLabelForCurrentLocale()); 
						}
						else{
							localizedLabels.add(acceptedTaxonomyName);
						}
					}
				}
				
				if (localizedLabels.size() > 0){
					localizedLabelsForAcceptedTaxonomies = StringUtils.join(localizedLabels, ",");
				}
			}
		}
		
		this.topicCriteria.getRenderProperties().renderValuesForLocale(JSFUtilities.getLocaleAsString());
		//this.topicCriteria.getResultRowRange().setRange(0, 30);
		this.topicCriteria.setOffsetAndLimit(0,30);
		this.topicCriteria.addOrderByLocale(JSFUtilities.getLocaleAsString(), Order.ascending);

	}

	public List<SimpleCmsPropertyValueWrapper> getSimpleCmsPropertyValueWrappers(){
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			//Create wrappers only if there are any values
			if (cmsProperty != null && CollectionUtils.isNotEmpty(cmsProperty.getSimpleTypeValues())){
				List<Topic> values = cmsProperty.getSimpleTypeValues();
				for (int i=0; i<values.size(); i++){
					simpleCmsPropertyValueWrappers.add(new SimpleCmsPropertyValueWrapper(cmsProperty, i, cmsRepositoryEntityFactory, null));
				}
			}
		}

		return simpleCmsPropertyValueWrappers;
	}

	public void deleteValueFromTopicProperty_UIAction(){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		//Remove value only it has not already been deleted in case of null value
		if (indexOfPropertyValueToBeProcessed != -1){
			
			try{
				if (cmsProperty.getPropertyDefinition().isMultiple()){
					//Remove value from simple cms property
					//only if indexOfPropertyValueToBeProcessed exists for values
					if (indexOfPropertyValueToBeProcessed < cmsProperty.getSimpleTypeValues().size()){
						
						String topicIdToBeRemoved = cmsProperty.getSimpleTypeValues().get(indexOfPropertyValueToBeProcessed).getId();
						
						cmsProperty.removeSimpleTypeValue(indexOfPropertyValueToBeProcessed);

						Events.instance().raiseEvent(SeamEventNames.UPDATE_LIST_OF_TOPICS_WHOSE_NUMBER_OF_CO_REFERENCES_SHOULD_CHANGE, 
								topicIdToBeRemoved);
					}

				}
				else{
					
					String topicIdToBeRemoved = cmsProperty.getSimpleTypeValue().getId();
					
					cmsProperty.setSimpleTypeValue(null);

					Events.instance().raiseEvent(SeamEventNames.UPDATE_LIST_OF_TOPICS_WHOSE_NUMBER_OF_CO_REFERENCES_SHOULD_CHANGE, 
							topicIdToBeRemoved);
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


	public List<Topic> findTopics_UIAction(Object event) {
		try {

			String selectedTopicLabel = event.toString();

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(selectedTopicLabel))
				return null;

			//Reset criteria
			topicCriteria.reset();
			
			//
			
			//Localized Label criterion
			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			localizationCriterion.addLocalizedLabel(selectedTopicLabel+"*");
			//localizationCriterion.enableContainsQueryOperator();
			localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);

			

			//Taxonomy Criterion
			List<Topic> results = new ArrayList<Topic>();
			
			//Add taxonomy criterion if its definition provides such restriction
			if (CollectionUtils.isNotEmpty(acceptedTaxonomies)){
				//Only one taxonomy
				if (acceptedTaxonomies.size() == 1){
					
					topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
					topicCriteria.addCriterion(localizationCriterion);
					topicCriteria.addTaxonomyNameEqualsCriterion(acceptedTaxonomies.get(0));
					
					CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

					results = cmsOutcome.getResults();
				}
				else{
					//More than one taxonomies.
					//Perform one query per taxonomy and then sort results
					for (String acceptedTaxonomy : acceptedTaxonomies){
						
						topicCriteria.reset();
						topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
						topicCriteria.addCriterion(localizationCriterion);
						topicCriteria.addTaxonomyNameEqualsCriterion(acceptedTaxonomy);
						
						CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
						
						if (cmsOutcome != null && cmsOutcome.getCount() > 0){
							results.addAll(cmsOutcome.getResults());
						}
					}
					
					//finally sort results
					if (results.size() > 2){
						Collections.sort(results, new TopicLocalizedLabelComparator(JSFUtilities.getLocaleAsString()));
					}
				}
			}
			else{
				
				topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
				topicCriteria.addCriterion(localizationCriterion);
				
				//No taxonomy criterion proceed normally
				CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

				results = cmsOutcome.getResults();
			}

			
			return results;

		} catch (Exception e) {
			logger.error("Error while loading Topics ",e);
			return null;
		}

	}


	public void addDraggedAndDroppedTopicToTopicProperty_Listener(DropEvent dropEvent) {
		Topic topic = (Topic) dropEvent.getDragValue();
		
		addSelectedTopic_UIAction(topic, true);
	}

	
	public void addSelectedTopic_UIAction(Topic selectedTopic, boolean checkTaxonomy){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		//Check that taxonomy is valid
		if (CollectionUtils.isNotEmpty(acceptedTaxonomies) && checkTaxonomy){
			if (selectedTopic.getTaxonomy() == null ||
					selectedTopic.getTaxonomy().getName() == null ||
					! acceptedTaxonomies.contains(selectedTopic.getTaxonomy().getName())
					) {
						JSFUtilities.addMessage(null, "object.edit.topic.taxonomyOfTopicNotInAllowedTaxonomies", 
								new String[]{selectedTopic.getTaxonomy().getLocalizedLabelForCurrentLocale(), localizedLabelsForAcceptedTaxonomies}, 
								FacesMessage.SEVERITY_WARN);
						return;
			}
				
		}
		
		List<Topic> topics= new ArrayList<Topic>();
		if (getCmsPropertyDefinition().isMultiple()){
			topics = cmsProperty.getSimpleTypeValues();
		}
		else{
			if (cmsProperty.getSimpleTypeValue() != null)
				topics.add(cmsProperty.getSimpleTypeValue());
		
		}
		String selectedTopicId = selectedTopic.getId();

		//check if topic allows content object references
		if (!selectedTopic.isAllowsReferrerContentObjects()){
			JSFUtilities.addMessage(null, "object.edit.topic.doesNotAllowObjectsToUseIt", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		// check if selected topic is already in value list
		boolean topicExists = false;
		for (Topic topic : topics) {
			// we check first if topic Id exists because there may be new user tags in the list and new tags do not have an id yet 
			if (topic.getId() != null && topic.getId().equals(selectedTopicId)) {
				topicExists = true;
				break;
			}
		}

		if (!topicExists) {
			
			
			if (isMultiple()){
				cmsProperty.addSimpleTypeValue(selectedTopic);
				simpleCmsPropertyValueWrappers.clear();
			}
			else{
				//Get topic id that will be replaced in order to update its reference number
				if (cmsProperty.getSimpleTypeValue() != null){
					Events.instance().raiseEvent(SeamEventNames.UPDATE_LIST_OF_TOPICS_WHOSE_NUMBER_OF_CO_REFERENCES_SHOULD_CHANGE, cmsProperty.getSimpleTypeValue().getId());
				}
				
				//Now replace value
				cmsProperty.setSimpleTypeValue(selectedTopic);
			}
			

			Events.instance().raiseEvent(SeamEventNames.UPDATE_LIST_OF_TOPICS_WHOSE_NUMBER_OF_CO_REFERENCES_SHOULD_CHANGE, selectedTopicId); 

		}
		else
			JSFUtilities.addMessage(null, "object.edit.topic.already.exists", null, FacesMessage.SEVERITY_WARN);


	}

	@Override
	public void addBlankValue_UIAction() {


	}

	public void setTopicLabelPattern(String topicLabelPattern) {
		this.topicLabelPattern = topicLabelPattern;
	}

	public String getTopicLabelPattern() {
		return topicLabelPattern;
	}
	
	public List<String> getAcceptedTaxonomies() {
		return acceptedTaxonomies;
	}

	public String getLocalizedLabelsForAcceptedTaxonomies(){
		return localizedLabelsForAcceptedTaxonomies;
	}
}
