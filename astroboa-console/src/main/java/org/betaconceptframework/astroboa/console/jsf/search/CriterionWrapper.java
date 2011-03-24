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
package org.betaconceptframework.astroboa.console.jsf.search;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.commons.comparator.TopicLocalizedLabelComparator;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.jsf.clipboard.ContentObjectItem;
import org.betaconceptframework.astroboa.console.jsf.taxonomy.LazyLoadingTopicTreeNode;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.event.DropEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in Advance Search page to wrap criteria defined from user
 * 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CriterionWrapper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String propertyLocalizedLabel;
	private String propertyPath;
	private QueryOperator queryOperator;
	private Object value;
	private ValueType propertyValueType;
	private Order order;

	private List<SelectItem> queryOperatorsAsSelectItems;

	private String suggestionLocalizedLabelPattern;

	private TopicService topicService;
	private ContentService contentService;

	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private List<String> acceptedContentTypes;

	private List<String> acceptedTaxonomies;
	
	
	public CriterionWrapper(TopicService topicService,
			ContentService contentService, 
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory, 
			DefinitionService definitionService){
		this.topicService = topicService;
		this.contentService = contentService;
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
		
		acceptedContentTypes = definitionService.getContentObjectTypes();
		
		//Add a generic one
		if (acceptedContentTypes != null){
			acceptedContentTypes.add(ValueType.ContentObject.toString());
		}
	}

	public String getPropertyPath() {
		return propertyPath;
	}
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}
	public QueryOperator getQueryOperator() {
		return queryOperator;
	}
	public void setQueryOperator(QueryOperator queryOperator) {
		this.queryOperator = queryOperator;
	}
	public Object getValue() {
		if (value == null && ValueType.Boolean == propertyValueType){
			//Specify default value for Boolean type
			value = Boolean.TRUE.toString();
		}
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public void setPropertyValueType(ValueType propertyValueType) {
		this.propertyValueType = propertyValueType;
	}

	public Criterion getCriterion() throws InvalidInputForCmsPropertyTypeException{

		if (StringUtils.isNotBlank(propertyPath) && queryOperator != null){
			SimpleCriterion simpleCriterion  = CriterionFactory.newSimpleCriterion();
			simpleCriterion.setOperator(getQueryOperator());
			
			if ((ValueType.Complex == propertyValueType || ValueType.Binary == propertyValueType)  
					&& ( queryOperator == QueryOperator.IS_NOT_NULL || queryOperator == QueryOperator.IS_NULL)){
				//Looking for existence of a binary property
				//It is the same thing as searching for the identifier of this property
				//This is a dirty way to avoid adding more complexity to 
				//backend repository
				//Due to issue https://issues.apache.org/jira/browse/JCR-1447
				//This criterion may not work as expected
				simpleCriterion.setProperty(PropertyPath.createFullPropertyPath(getPropertyPath(), CmsBuiltInItem.CmsIdentifier.getJcrName()));
			}
			else
				simpleCriterion.setProperty(getPropertyPath());

			 if (value != null){
				 if (ValueType.Topic == propertyValueType && value != null &&
						 value instanceof Topic) {
					 simpleCriterion.addValue(((Topic)value).getId());
				 }
				 else if (ValueType.ContentObject == propertyValueType && value != null &&
						 value instanceof ContentObject) {
					 simpleCriterion.addValue(((ContentObject)value).getId());
				 }
				 else{
					 if (QueryOperator.CONTAINS == queryOperator && value != null){  
						 if (ValueType.Binary == propertyValueType){
							 //Build a new criterion 
							 return CriterionFactory.complexCmsPropertycontains(simpleCriterion.getProperty(), value.toString());
						 }
						 else{
							 return CriterionFactory.simpleCmsPropertycontains(simpleCriterion.getProperty(), value.toString());
						 }
					 }
					 else{
						 if (ValueType.Boolean == propertyValueType){
							 simpleCriterion.addValue(Boolean.valueOf((String)value));	 
						 }
						 else if (ValueType.Long == propertyValueType){
							 try{
								 simpleCriterion.addValue(Long.valueOf((String)value));
							 }catch(Exception e){
								 throw new InvalidInputForCmsPropertyTypeException(
					    					 JSFUtilities.getLocalizedMessage("errors.long", new String[]{propertyLocalizedLabel}) ,e);
							 }
						 }
						 else if (ValueType.Double == propertyValueType){
							 try{
								 simpleCriterion.addValue(Double.valueOf((String)value));
							 }catch(Exception e){
								 throw new InvalidInputForCmsPropertyTypeException(
					    					 JSFUtilities.getLocalizedMessage("errors.double", new String[]{propertyLocalizedLabel}) ,e);
							 }
						 }
						
						 else{
							 simpleCriterion.addValue(value);
						 }
					 }
				 }
			 }
			
			return simpleCriterion;
		}

		return null;
	}
	public void clear() {
		propertyPath = null;
		queryOperator = null;
		value = null;
		propertyLocalizedLabel = null;
		queryOperatorsAsSelectItems = null;
		propertyValueType = null;
		order = null;
		acceptedTaxonomies = null;

	}

	public List<SelectItem> getQueryOperatorsAsSelectItems() {
		
		if (CollectionUtils.isNotEmpty(queryOperatorsAsSelectItems))
			return queryOperatorsAsSelectItems;
		
		queryOperatorsAsSelectItems = new ArrayList<SelectItem>();
		
		if (propertyValueType != null){
			
			

			switch (propertyValueType) {
			
			case Complex:
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
				
				break;
			case Binary:
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.CONTAINS, JSFUtilities.getLocalizedMessage("query.operator.contains", null)));
				
				break;
			case Boolean:
				queryOperatorsAsSelectItems.add(new SelectItem(null, ""));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.EQUALS, QueryOperator.EQUALS.getOp()));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
				break;
			case Topic:
			case ContentObject:
				queryOperator = QueryOperator.NOT_EQUALS;
				
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.NOT_EQUALS, QueryOperator.NOT_EQUALS.getOp()));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.EQUALS, QueryOperator.EQUALS.getOp()));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
				break;
			
			case String:
				queryOperatorsAsSelectItems.add(new SelectItem(null, ""));
				//queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.LIKE, JSFUtilities.getLocalizedMessage("query.operator.like", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.NOT_EQUALS, QueryOperator.NOT_EQUALS.getOp()));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.EQUALS, QueryOperator.EQUALS.getOp()));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
				queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.CONTAINS, JSFUtilities.getLocalizedMessage("query.operator.contains", null)));
				break;
			case Double:
			case Long:
			case Date:
				queryOperatorsAsSelectItems.add(new SelectItem(null, ""));
				for (QueryOperator queryOperator : QueryOperator.values()) {
					if (queryOperator != QueryOperator.LIKE && 
							queryOperator != QueryOperator.CONTAINS){
						if (QueryOperator.IS_NOT_NULL == queryOperator){
							queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NOT_NULL, JSFUtilities.getLocalizedMessage("query.operator.not.null", null)));
						}
						else if (QueryOperator.IS_NULL == queryOperator){
							queryOperatorsAsSelectItems.add(new SelectItem(QueryOperator.IS_NULL, JSFUtilities.getLocalizedMessage("query.operator.null", null)));
						}
						else{
							queryOperatorsAsSelectItems.add(new SelectItem(queryOperator, queryOperator.getOp()));
						}
					}
				}
				break;
			
			default:
				break;
			}
		}

		return queryOperatorsAsSelectItems;
	}

	
	public void addDraggedAndDroppedTopicAsCriterionValue_Listener(DropEvent dropEvent) {
		LazyLoadingTopicTreeNode topicTreeNode = (LazyLoadingTopicTreeNode) dropEvent.getDragValue();
		
		addSelectedTopic_UIAction(topicTreeNode.getTopic());
	}

	
	public void addSelectedTopic_UIAction(Topic selectedTopic){
		
		value = selectedTopic;

	}
	
	public List<Topic> findTopics_UIAction(Object event) {
		try {

			String selectedTopicLabel = event.toString();

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(selectedTopicLabel))
				return null;
			
			List<Topic> results = new ArrayList<Topic>();
			
			//Localized Label criterion
			LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
			localizationCriterion.setLocale(JSFUtilities.getLocaleAsString());
			localizationCriterion.addLocalizedLabel(selectedTopicLabel+ CmsConstants.ANY_NAME);
			//localizationCriterion.enableContainsQueryOperator();
			localizationCriterion.setQueryOperator(QueryOperator.CONTAINS);


			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();

			//Add taxonomy criterion if its definition provides such restriction
			if (CollectionUtils.isNotEmpty(acceptedTaxonomies)){
				//Only one taxonomy
				if (acceptedTaxonomies.size() == 1){
					topicCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
					topicCriteria.setOffsetAndLimit(0,30);
					topicCriteria.addOrderByLocale(JSFUtilities.getLocaleAsString(), Order.ascending);

					topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
					topicCriteria.addCriterion(localizationCriterion);
					topicCriteria.addTaxonomyNameEqualsCriterion(acceptedTaxonomies.get(0));
					
					CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria);

					results = cmsOutcome.getResults();
				}
				else{
					//More than one taxonomies.
					//Perform one query per taxonomy and then sort results
					for (String acceptedTaxonomy : acceptedTaxonomies){
						
						topicCriteria.reset();
						topicCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
						topicCriteria.setOffsetAndLimit(0,30);
						topicCriteria.addOrderByLocale(JSFUtilities.getLocaleAsString(), Order.ascending);

						topicCriteria.addAllowsReferrerContentObjectsCriterion(true);
						topicCriteria.addCriterion(localizationCriterion);
						topicCriteria.addTaxonomyNameEqualsCriterion(acceptedTaxonomy);
						
						CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria);
						
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
				CmsOutcome<Topic> cmsOutcome = topicService.searchTopics(topicCriteria);

				results = cmsOutcome.getResults();
			}

			return results;

		} catch (Exception e) {
			logger.error("Error while loading Topics ",e);
			return null;
		}

	}
	
	public List<ContentObjectUIWrapper> findContentObjects_UIAction(Object event) {
		try {

			String selectedContentObjectTitle = event.toString();

			//Do not proceed if selected topic label is empty
			if (StringUtils.isBlank(selectedContentObjectTitle))
				return null;

			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
			contentObjectCriteria.setOffsetAndLimit(0,15);
			contentObjectCriteria.addOrderProperty("profile.title", Order.ascending);
			
			//Profile Title criterion
			Criterion profileTitleCriterion = CriterionFactory.like("profile.title", "%"+selectedContentObjectTitle+"%");
			contentObjectCriteria.addCriterion(profileTitleCriterion);
			
			CmsOutcome<CmsRankedOutcome<ContentObject>> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria);

			List<ContentObjectUIWrapper> wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();

			if (cmsOutcome.getCount() > 0) {
				List<CmsRankedOutcome<ContentObject>> cmsOutcomeRowList = cmsOutcome.getResults();
				
				for (CmsRankedOutcome<ContentObject> cmsOutcomeRow : cmsOutcomeRowList) {
					wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(cmsOutcomeRow.getCmsRepositoryEntity()));
				}
			}
			
			return wrappedContentObjects;

		} catch (Exception e) {
			logger.error("Error while loading Content Objects ",e);
			return null;
		}

	}
	
	public void addSelectedContentObject_UIAction(ContentObject selectedContentObject){
		
		value = selectedContentObject;
	}
	
	public void addDraggedAndDroppedContentObject_Listener(DropEvent dropEvent) {
		ContentObjectItem contentObjectItem = (ContentObjectItem) dropEvent.getDragValue();

		if (StringUtils.isNotBlank(contentObjectItem.getId())){ 

			//A Content Object has been dragged
			//Load Content object from repository
			ContentObject contentObject = contentService.getContentObjectByIdAndLocale(contentObjectItem.getId(), JSFUtilities.getLocaleAsString(), null);

			if (contentObject == null)
				JSFUtilities.addMessage(null, "Δεν υπάρχει Αντικείμενο με αναγνωριστικό "+ contentObjectItem.getId() , FacesMessage.SEVERITY_WARN);
			else{
				addSelectedContentObject_UIAction(contentObject);
			}

		}
	}
	
	public String getLocalizedLabelForCurrentLocaleForContentObjectTypeValue(){
		if (value != null && propertyValueType == ValueType.ContentObject && value instanceof ContentObject){
			Object contentObject = getValue();

			if (contentObject != null){
				try{
					return ((ContentObject)contentObject).getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
				}
				catch(Exception e){
					logger.error("", e);
					return "";
				}
			}
		}

		return "";
	}

	public void clearOrder_Listener(){
		order = null;
	}
	public void clearOrder_Listener(ActionEvent actionEvent){
		
		Object eventObject = actionEvent.getSource();
		eventObject.toString();
	}

	public String getPropertyLocalizedLabel() {
		return propertyLocalizedLabel;
	}
	public void setPropertyLocalizedLabel(String propertyLocalizedLabel) {
		this.propertyLocalizedLabel = propertyLocalizedLabel;
	}

	public ValueType getPropertyValueType() {
		return propertyValueType;
	}

	public String getSuggestionLocalizedLabelPattern() {
		return suggestionLocalizedLabelPattern;
	}

	public void setSuggestionLocalizedLabelPattern(
			String suggestionLocalizedLabelPattern) {
		this.suggestionLocalizedLabelPattern = suggestionLocalizedLabelPattern;
	}

	public List<String> getAcceptedContentTypes() {
		return acceptedContentTypes;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * @param acceptedTaxonomies
	 */
	public void setAcceptedTaxonomies(List<String> acceptedTaxonomies) {
		this.acceptedTaxonomies = acceptedTaxonomies;
		
	}

	public List<String> getAcceptedTaxonomies() {
		return acceptedTaxonomies;
	}

	/**
	 * @param acceptedContentTypes2
	 */
	public void setAcceptedContentTypes(List<String> acceptedContentTypes) {
		this.acceptedContentTypes = acceptedContentTypes;
	}
	
	
}
