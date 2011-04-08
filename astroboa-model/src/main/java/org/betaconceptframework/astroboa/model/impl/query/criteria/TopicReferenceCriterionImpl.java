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
package org.betaconceptframework.astroboa.model.impl.query.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicReferenceCriterion;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicReferenceCriterionImpl extends SimpleCriterionImpl implements TopicReferenceCriterion,Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6034509558499684454L;
	
	private boolean includeSubTopics;

	
	public TopicReferenceCriterionImpl() {
		//InternalCondition must be nullified
		setInternalCondition(null);
	}
	
	@Override
	public void propertyIsComplex() {
		//Properties of these type are never complex
		//Do nothing
	}

	@Override
	public void setCaseMatching(CaseMatching caseMatching) {
		//Case matching does not play any role in this context
	}

	@Override
	public void setOperator(QueryOperator operator) {
		//Only QueryOperator.EQUALS and QueryOperator.NOT_EQUALS are allowed
		if (operator != null && QueryOperator.EQUALS != operator && QueryOperator.NOT_EQUALS != operator 
				&& QueryOperator.IS_NULL != operator && QueryOperator.IS_NOT_NULL != operator){
			throw new CmsException("Invalid query operator "+operator + " for topic reference criterion");
		}
		
		super.setOperator(operator);
	}

	@Override
	public void addTopicAsAValue(Topic topicReference) { 
		if (topicReference != null){
			if (StringUtils.isNotBlank(topicReference.getId())){
				addValue(topicReference.getId());
			}
			else if (StringUtils.isNotBlank(topicReference.getName())){
				addValue(CmsConstants.TOPIC_REFERENCE_CRITERION_VALUE_PREFIX+topicReference.getName());
			}
			else{
				LoggerFactory.getLogger(getClass()).warn("Topic {} has neither an identifier nor a name and therefore cannot be used as a criterion value ", topicReference.toString());
			}
		}
		else{
			LoggerFactory.getLogger(getClass()).warn("Topic is null and therefore cannot be used as a criterion value ");
		}
		
	}

	@Override
	public void addTopicsAsValues(List<Topic> topicReferences) {
		
		if (CollectionUtils.isNotEmpty(topicReferences)){
			for (Topic topicReference: topicReferences){
				addTopicAsAValue(topicReference);
			}
		}
		
	}

	@Override
	public String getXPath() {

		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();

		String activeAuthenticationToken = AstroboaClientContextHolder.getActiveAuthenticationToken();

		Map<String, List<String>> topicPropertyPathsPerTaxonomies = null;
		
		if (lazyLoaderForActiveClient != null){
			topicPropertyPathsPerTaxonomies = lazyLoaderForActiveClient.getTopicPropertyPathsPerTaxonomies(activeAuthenticationToken);
		}
		
		Condition internalCondition = getInternalCondition();

		if (internalCondition == null){
			internalCondition = Condition.OR;
		}

		Criterion criterion = null;
		

		if (CollectionUtils.isEmpty(getValues())){
			Set<String> possiblePropertyPaths = loadTopicPropertyPaths(null, topicPropertyPathsPerTaxonomies);
			
			for (String topicPropertyPath : possiblePropertyPaths){
				criterion = createCriterionForProperty(internalCondition, topicPropertyPath, criterion, null);
			}

		}
		else{
			for (Object value : getValues()){

				List parentAndChildIds = new ArrayList();

				String taxonomyName = null;

				String topicId = checkIfValueIsAReferenceAndLoadReferenceId((String)value); 

				if (topicId != null){
					parentAndChildIds.add(topicId);

					if (lazyLoaderForActiveClient != null){
							
						if (includeSubTopics){
							lazyLoaderForActiveClient.loadChildTopicIdsForSpecifiedProfileSubjectIds(parentAndChildIds, activeAuthenticationToken, CacheRegion.NONE);
						}
							
						try{
							Taxonomy taxonomy = lazyLoaderForActiveClient.lazyLoadTaxonomyForTopic(topicId, activeAuthenticationToken);
							
							if (taxonomy != null){
								taxonomyName = taxonomy.getName();
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
				}
					
				Set<String> possiblePropertyPaths = loadTopicPropertyPaths(taxonomyName, topicPropertyPathsPerTaxonomies);

				for (String topicPropertyPath : possiblePropertyPaths){
					criterion = createCriterionForProperty(internalCondition, topicPropertyPath, criterion, parentAndChildIds);
				}
				
			}

		}
		
		if (criterion == null){
			return "";
		}
		else{
			return criterion.getXPath();
		}
	}

	private Criterion createCriterionForProperty(Condition internalCondition,
			String topicPropertyPath, Criterion topicCriterion, List parentAndChildIds) {
		
		
		Criterion parentAndChildCriterion = createSimpleCriterionForItem(getOperator(), parentAndChildIds, Condition.OR, topicPropertyPath); 

		if (topicCriterion == null){
			topicCriterion = parentAndChildCriterion;
		}
		else{
			switch (internalCondition) {
			case AND:
				topicCriterion = CriterionFactory.and(topicCriterion, parentAndChildCriterion);
				break;
			case OR:
				topicCriterion = CriterionFactory.or(topicCriterion, parentAndChildCriterion);
				break;
			default:
				break;
			}
		}
		return topicCriterion;
	}


	private Set<String> loadTopicPropertyPaths(String taxonomyName, Map<String, List<String>> topicPropertyPathsPerTaxonomies){
		
		Set<String> possiblePropertyPaths = new HashSet<String>();

		if (getProperty() != null){
			possiblePropertyPaths.add(getProperty());
		}
		else{

			if (MapUtils.isNotEmpty(topicPropertyPathsPerTaxonomies)){

				//Build one criterion for each topic property that belongs to the specified taxonomy
				//If no taxonomy is provided, all topic properties will be included
				if (StringUtils.isBlank(taxonomyName)){
					for (List<String> topicProperyPaths : topicPropertyPathsPerTaxonomies.values()){
						if (CollectionUtils.isNotEmpty(topicProperyPaths)){
							for (String topicPropertyPath: topicProperyPaths){
								possiblePropertyPaths.add(topicPropertyPath);
							}
						}
					}
				}
				else{
					//Get all topicProperties for provided taxonomy
					List<String> topicPropertyPathsForTaxonomy = topicPropertyPathsPerTaxonomies.get(taxonomyName);
					if (CollectionUtils.isNotEmpty(topicPropertyPathsForTaxonomy)){
						for (String topicPropertyPath: topicPropertyPathsForTaxonomy){
								possiblePropertyPaths.add(topicPropertyPath);
						}
					}

					//Finally add all topic properties that are not connected to any taxonomy
					List<String> topicPropertyPathsWithoutTaxonomy = topicPropertyPathsPerTaxonomies.get(CmsConstants.ANY_TAXONOMY);
					if (CollectionUtils.isNotEmpty(topicPropertyPathsWithoutTaxonomy)){
						for (String topicPropertyPath: topicPropertyPathsWithoutTaxonomy){
								possiblePropertyPaths.add(topicPropertyPath);
						}
					}
				}	
			}
		}

		return possiblePropertyPaths;
	}
	
	private SimpleCriterion createSimpleCriterionForItem(QueryOperator queryOperator, List values, Condition internalCondition, String property) {
		SimpleCriterion criterion = CriterionFactory.newSimpleCriterion();
		criterion.setValues(values);
		criterion.setInternalCondition(internalCondition);
		criterion.setProperty(property);
		criterion.setOperator(queryOperator);

		return criterion;
	}

	@Override
	public void expandCriterionToIncludeSubTopics() {
		includeSubTopics = true;
	}

	@Override
	public void addValue(Object value) {
		
		if (value != null){
			if (value instanceof Topic){
				addTopicAsAValue((Topic)value);
			}
			else{
				if (value instanceof String && ((String) value).endsWith(CmsConstants.INCLUDE_CHILDREN_EXPRESSION)){
					expandCriterionToIncludeSubTopics();
					super.addValue(StringUtils.substringBeforeLast((String)value, CmsConstants.INCLUDE_CHILDREN_EXPRESSION));
				}
				else{
					super.addValue(value);
				}
			}
		}
	}

	@Override
	public void setValues(List<Object> values) {
		if (values != null){
			for (Object value : values){
				addValue(value);
			}
		}
	}
	
	
}
