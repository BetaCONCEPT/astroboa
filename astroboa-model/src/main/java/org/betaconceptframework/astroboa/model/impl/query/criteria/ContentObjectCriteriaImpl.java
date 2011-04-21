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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ContentObjectProfileItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Content Object query Criteria
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectCriteriaImpl extends CmsCriteriaImpl implements ContentObjectCriteria, Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 2298044350810792492L;

	private List<String> contentTypes = new ArrayList<String>();

	private String searchText;

	private List<String> projectionPropertyPaths = new ArrayList<String>();

	public ContentObjectCriteriaImpl(){

		nodeType = CmsBuiltInItem.StructuredContentObject;
	}

	@Override
	public void reset() {

		searchText = null;

		super.reset();
	}


	/**
	 * @param searchText the searchText to set
	 */
	public void addFullTextSearchCriterion(String searchText) {

		this.searchText = searchText;

	}

	public void addProfileSubjectIdCriterion(QueryOperator queryOperator, String subjectId, boolean includeChildTopicIds) {
		if (StringUtils.isNotBlank(subjectId)){
			addProfileSubjectIdsCriterion(queryOperator, Arrays.asList(subjectId), Condition.AND, includeChildTopicIds);
		}
	}

	public void addProfileSubjectIdsCriterion(QueryOperator queryOperator, List subjectIds, Condition internalCondition, boolean includeChildTopicIds) {

		if (CollectionUtils.isNotEmpty(subjectIds)){
			addCriterion(CriterionFactory.newTopicReferenceCriterion(ContentObjectProfileItem.Subject.getItemForQuery().getJcrName(), subjectIds, internalCondition, queryOperator, includeChildTopicIds));
		}

	}

	private SimpleCriterion createSimpleCriterionForItem(QueryOperator queryOperator, List values, Condition internalCondition, ItemQName property) {
		SimpleCriterion criterion = CriterionFactory.newSimpleCriterion();
		criterion.setValues(values);
		criterion.setInternalCondition(internalCondition);
		criterion.setProperty(property.getJcrName());
		criterion.setOperator(queryOperator);

		return criterion;
	}

	public void addContentObjectTypesEqualsAnyCriterion(List<String> contentObjectTypes) {

		Criterion criterion = createContentObjectTypesEqualsCriterion(contentObjectTypes);

		addCriterion(criterion);
	}


	private List<String> replaceBaseContentTypesWithActualContentTypes(List<String> contentObjectTypes) {

		if (contentObjectTypes == null || contentObjectTypes.isEmpty())
		{
			return contentObjectTypes;
		}
		
		LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
		
		if (lazyLoaderForActiveClient !=null)
		{
			//Key is the base type, value is the list with all content types extending the base type
			Map<String, List<String>> contentTypeHierarchy = lazyLoaderForActiveClient.getContentTypeHierarchy(AstroboaClientContextHolder.getActiveAuthenticationToken());
			
			if (contentTypeHierarchy == null || contentTypeHierarchy.isEmpty())
			{
				return contentObjectTypes;
			}
			
			List<String> actualContentTypes = new ArrayList<String>();
			
			for (String contentType: contentObjectTypes)
			{
				if (contentTypeHierarchy.containsKey(contentType) && CollectionUtils.isNotEmpty(contentTypeHierarchy.get(contentType)))
				{
					actualContentTypes.addAll(contentTypeHierarchy.get(contentType));
				}
				else
				{
					actualContentTypes.add(contentType);
				}
			}
			
			return actualContentTypes;
		}
		else
		{
			return contentObjectTypes;
		}
	}

	public void addOwnerIdEqualsCriterion(String value) {
		SimpleCriterion criterion = createSimpleCriterionForItem(QueryOperator.EQUALS,Arrays.asList(value), null, CmsBuiltInItem.OwnerCmsIdentifier);
		addCriterion(criterion);
	}

	public void addOwnerIdsEqualsAnyCriterion(List<String> ownerIds) {

		SimpleCriterion criterion = createSimpleCriterionForItem(QueryOperator.EQUALS, 
				ownerIds, Condition.OR, CmsBuiltInItem.OwnerCmsIdentifier); 

		addCriterion(criterion);
	}


	public void addContentObjectTypeEqualsCriterion(String contentObjectType) {
		List<String> values = new ArrayList<String>();

		values.add(contentObjectType);

		addContentObjectTypesEqualsAnyCriterion(values);

	}

	public void copyTo(ContentObjectCriteria targetContentObjectCriteria) {

		if (targetContentObjectCriteria != null){

			if (getCriteria() != null){
				for (Criterion criterion: getCriteria()){
					targetContentObjectCriteria.addCriterion(criterion);
				}
			}

			targetContentObjectCriteria.setOffsetAndLimit(getOffset(), getLimit());

			if (isCacheable()){
				targetContentObjectCriteria.setCacheable(getCacheRegion());
			}
			else{
				targetContentObjectCriteria.doNotCacheResults();
			}

			if (getRenderProperties() != null)
			{
				targetContentObjectCriteria.getRenderProperties().resetRenderInstructions();
				targetContentObjectCriteria.getRenderProperties().getRenderInstructions().putAll(getRenderProperties().getRenderInstructions());
			}

			if (getOrderProperties() != null){
				for (Entry<String, Order> orderProperty : getOrderProperties().entrySet())
					targetContentObjectCriteria.addOrderProperty(orderProperty.getKey(), orderProperty.getValue());
			}

			if (CollectionUtils.isNotEmpty(contentTypes)){
				((ContentObjectCriteriaImpl)targetContentObjectCriteria).setContentTypes(new ArrayList<String>(contentTypes));
			}

			if (searchText != null){
				targetContentObjectCriteria.addFullTextSearchCriterion(searchText);
			}

			if (CollectionUtils.isNotEmpty(projectionPropertyPaths)){
				for (String projectedPropertyPath: projectionPropertyPaths){
					targetContentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded(projectedPropertyPath);
				}
			}

			
			targetContentObjectCriteria.setSearchMode(getSearchMode());

			
			
			//Always copy xpath query last
			if (xpathQuery != null){
				targetContentObjectCriteria.setXPathQuery(xpathQuery);
			}

		}
	}

	private void setContentTypes(List<String> contentTypes) {
		this.contentTypes = contentTypes;

	}


	@Override
	protected String getAncestorQuery() {
		return null;
	}

	public void addSystemNameEqualsCriterion(String systemName) {
		if (StringUtils.isNotBlank(systemName)){
			addCriterion(CriterionFactory.equals(CmsBuiltInItem.SystemName.getJcrName(), systemName));
		}

	}


	public void addSystemNameNotEqualsCriterion(String systemName) {
		if (StringUtils.isNotBlank(systemName)){
			addCriterion(CriterionFactory.notEquals(CmsBuiltInItem.SystemName.getJcrName(), systemName));
		}

	}

	public void addSystemNameContainsCriterion(String systemName) {
		if (StringUtils.isNotBlank(systemName)){
			addCriterion(CriterionFactory.contains(CmsBuiltInItem.SystemName.getJcrName(), systemName));
		}

	}

	/**
	 * Append XPath Query with the criteria corresponding to searchText, profileSubjectIds and any other
	 * criteria which need lazyLoader. This method uses the Active LazyLoader provided in the current execution Thread
	 * 
	 * Therefore whenever called the appropriate LazyLoader must be available. If not then text search will be restricted 
	 * to content object root property level
	 */
	private void appendXPathQuery(){

		//Full Text Search
		if (StringUtils.isNotBlank(searchText)){
			//First level search
			Criterion searchTextCriterion =  CriterionFactory.contains(CmsConstants.ANY_NAME, searchText);

			if (CollectionUtils.isNotEmpty(contentTypes)){
				
				LazyLoader lazyLoaderForActiveClient = AstroboaClientContextHolder.getLazyLoaderForActiveClient();
				
				if (lazyLoaderForActiveClient !=null){
					
					
					List<String> processedContentTypes = replaceBaseContentTypesWithActualContentTypes(contentTypes);
					
					Integer maxDefinitionHierarchyDepth = 0;
					
					DefinitionService definitionService = lazyLoaderForActiveClient.getDefinitionService();
					
					for (String contentType : processedContentTypes){
						
						Integer depth =  definitionService.getDefinitionHierarchyDepthForContentType(contentType);
						
						if (depth>maxDefinitionHierarchyDepth){
							maxDefinitionHierarchyDepth = depth;
						}
					}
					
					if (maxDefinitionHierarchyDepth<=0){
						maxDefinitionHierarchyDepth = definitionService.getDefinitionHierarchyDepthForContentType(CmsConstants.ANY_NAME);
					}
					
					((SimpleCriterionImpl)searchTextCriterion).setNumberOfNodeLevelsToSearchInTheModelHierarchy(maxDefinitionHierarchyDepth);
				}
				
			}

			addCriterion(searchTextCriterion);
			
			//See https://issues.apache.org/jira/browse/JCR-800
			//Oder by jCr:Score is enabled only if no order property is provided
			if (getOrderProperties()==null || getOrderProperties().isEmpty()){
				addOrderByRelevance(Order.descending);
			}
			

			//Nullify search text since criterion has been created
			searchText = null;
		}

	}


	public void addPropertyPathWhoseValueWillBePreLoaded(String propertyPath) {
		
		if (StringUtils.isNotBlank(propertyPath)){
			projectionPropertyPaths.add(propertyPath);
		}
	}

	public List<String> getPropertyPathsWhichWillBePreLoaded() {
		return projectionPropertyPaths;
	}

	@Override
	public void addOrderByRelevance(Order order) {
		addOrderProperty(JcrBuiltInItem.JcrScore.getJcrName(), order);
		
	}

	@Override
	public void addSystemNameEqualsCriterionIgnoreCase(String systemName) {
		if (StringUtils.isNotBlank(systemName)){
			addCriterion(CriterionFactory.equalsCaseInsensitive(CmsBuiltInItem.SystemName.getJcrName(), systemName.toLowerCase()));
		}
	}

	@Override
	public Criterion createContentObjectTypesEqualsCriterion(List<String> contentObjectTypes) {

		if (contentObjectTypes != null){
			contentTypes.addAll(contentObjectTypes);
		}

		List<String> processedContentObjectTypes = replaceBaseContentTypesWithActualContentTypes(contentObjectTypes);
		
		return createSimpleCriterionForItem(QueryOperator.EQUALS, 
				processedContentObjectTypes, Condition.OR, CmsBuiltInItem.ContentObjectTypeName);

		
	}
	
	@Override
	public Criterion createContentObjectTypeEqualsCriterion(String contentObjectType) {
		
		List<String> values = new ArrayList<String>();

		values.add(contentObjectType);

		return createContentObjectTypesEqualsCriterion(values);
	}

	@Override
	public void addSystemNameEqualsAnyCriterion(
			List<String> contentObjectSystemNames) {
		
		if (! CollectionUtils.isEmpty(contentObjectSystemNames)){
			
			Criterion criterion = createSystemNameEqualsAnyCriterion(contentObjectSystemNames);
			
			if (criterion != null){
				addCriterion(criterion);
			}
		}
	}

	@Override
	public Criterion createSystemNameEqualsAnyCriterion(
			List<String> contentObjectSystemNames) {
		
		if (! CollectionUtils.isEmpty(contentObjectSystemNames)){
			return createSimpleCriterionForItem(QueryOperator.EQUALS, 
					contentObjectSystemNames, Condition.OR, CmsBuiltInItem.SystemName);
		}
		
		return null;

	}

	@Override
	public String getXPathQuery() {
		//In case no query has been creates and searchText has been provided
		if (StringUtils.isBlank(xpathQuery) && StringUtils.isNotBlank(searchText)){
			appendXPathQuery();
		}

		return super.getXPathQuery();
	}

	@Override
	public void addOrderBySystemName(Order order) {
		addOrderProperty(CmsBuiltInItem.SystemName.getJcrName(), order);
	}

}


