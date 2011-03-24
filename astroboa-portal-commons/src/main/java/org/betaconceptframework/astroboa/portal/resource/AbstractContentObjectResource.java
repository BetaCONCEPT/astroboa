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
package org.betaconceptframework.astroboa.portal.resource;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.portal.managedbean.PortalThemeSelector;
import org.betaconceptframework.astroboa.portal.managedbean.ViewCountAggregator;
import org.betaconceptframework.astroboa.portal.utility.CalendarUtils;
import org.betaconceptframework.astroboa.portal.utility.CmsUtils;
import org.betaconceptframework.astroboa.portal.utility.PortalCacheConstants;
import org.betaconceptframework.astroboa.portal.utility.PortalIntegerConstants;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.astroboa.portal.utility.SyndicationFeedGenerator;
import org.betaconceptframework.astroboa.portal.utility.SyndicationFeedType;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class AbstractContentObjectResource<T extends ContentObjectResourceContext> implements Resource<ContentObject, T> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@In 
	protected LocaleSelector localeSelector;
	
	@In(create=true)
	protected PortalThemeSelector portalThemeSelector;
	
	@In(create=true)
	protected AstroboaClient astroboaClient;

	@In(create=true)
	protected CmsUtils cmsUtils;
	
	@In(create=true)
	protected SyndicationFeedGenerator<T> syndicationFeedGenerator;
	
	@In(create=true)
	protected CalendarUtils calendarUtils;
	
	@In(create=true)
	protected ContentObject portal;
	
	@In(create=true)
	protected ViewCountAggregator viewCountAggregator;

	// holds the outjected response for the requested content object resource. 
	// The outjection is performed manually to avoid overwriting it by multiple requests of this bean during the same page rendering phase 
	// The resource response holds internally the resource representation of the query outcome as a LIST of objects even if the query is performed only for one resource
	protected ResourceResponse<ContentObject, T> resourceResponse;
	
	// All url parameters are outjected manually to be available to the calling page. Manual outjection is required in order to
	// avoid overwriting their values by multiple requests of this bean during the same page rendering phase
	
	// holds the URL that represents the request about a resource
	@RequestParameter
	protected String resourceRequestURL;
	
	// url parameters
	@RequestParameter
	protected String contentObjectId;
	
	@RequestParameter
	protected String contentObjectSystemName;

	@RequestParameter
	protected String contentObjectSystemNameOrId;

	@RequestParameter
	protected String commaDelimitedContentObjectTypes;

	@RequestParameter
	protected String commaOrPlusDelimitedTopicNames;

	@RequestParameter
	protected Boolean searchInSubtopics;

	@RequestParameter
	protected String dateProperty;

	@RequestParameter
	protected String fromDateInclusive;

	@RequestParameter
	protected String toDateInclusive;
	
	@RequestParameter
	protected String predefinedPeriod;
	
	@RequestParameter
	protected Boolean discreteDates;
	
	@RequestParameter
	protected String textSearched;
	
	@RequestParameter
	protected String queryExpression;

	@RequestParameter
	protected String orderByProperty;

	@RequestParameter
	protected String order;

	@RequestParameter
	protected Integer pageNumber;

	@RequestParameter
	protected Integer pageSize;

	@RequestParameter
	protected String resourceRepresentationTemplate;
	
	@RequestParameter
	protected String resourceRepresentationType;
	
	@RequestParameter
	protected String feedTitle;
	
	@RequestParameter
	protected String feedDescription;
	
	
	public AbstractContentObjectResource() {

	}
	
	
	public ResourceResponse<ContentObject, T> findResources(Object... criteriaArguments) {
		String commaDelimitedContentObjectTypes = (String)criteriaArguments[0];
		String commaOrPlusDelimitedTopicNames = (String)criteriaArguments[1];
		boolean searchInSubtopics = convertToBoolean(criteriaArguments[2]);
		String datePropertyPath = (String)criteriaArguments[3];
		
		if (criteriaArguments.length == 14) {
			Object fromDateInclusive = criteriaArguments[4];
			Object toDateInclusive = criteriaArguments[5];
			boolean discreteDates = convertToBoolean(criteriaArguments[6]);
			String textSearched = (String)criteriaArguments[7];
			String queryExpression = (String)criteriaArguments[8];
			String orderByPropertyPath  = (String)criteriaArguments[9];
			String order = (String)criteriaArguments[10];
			int pageNumber = convertToInteger(criteriaArguments[11]);
			int pageSize = convertToInteger(criteriaArguments[12]);
			String locale = (String)criteriaArguments[13];


			return findResourcesInternal(
					commaDelimitedContentObjectTypes,
					commaOrPlusDelimitedTopicNames, 
					searchInSubtopics,
					datePropertyPath,
					fromDateInclusive,
					toDateInclusive,
					discreteDates,
					textSearched,
					queryExpression, 
					orderByPropertyPath, 
					order, 
					pageNumber, 
					pageSize, 
					locale);
		}
		else if (criteriaArguments.length == 13) {
			String predefinedPeriodName = (String)criteriaArguments[4];
			boolean discreteDates = convertToBoolean(criteriaArguments[5]);
			String textSearched = (String)criteriaArguments[6];
			String queryExpression = (String)criteriaArguments[7];
			String orderByPropertyPath = (String)criteriaArguments[8];
			String order = (String)criteriaArguments[9];
			int pageNumber = convertToInteger(criteriaArguments[10]);
			int pageSize = convertToInteger(criteriaArguments[11]);
			String locale = (String)criteriaArguments[12];

			return findResourcesInternal(
					commaDelimitedContentObjectTypes, 
					commaOrPlusDelimitedTopicNames, 
					searchInSubtopics, 
					datePropertyPath, 
					predefinedPeriodName, 
					discreteDates, 
					textSearched,
					queryExpression,
					orderByPropertyPath, 
					order, 
					pageNumber, 
					pageSize, 
					locale);
		}
		else {
			logger.error("Wrong number of arguments");
			return null;
		}
	}
	
	
	public ResourceResponse<ContentObject, T> findRecentlyPublishedResources(Object... criteriaArguments) { 
			
		String commaDelimitedContentObjectTypes = (String)criteriaArguments[0];
		String commaOrPlusDelimitedTopicNames = (String)criteriaArguments[1]; 
		boolean searchInSubtopics = convertToBoolean(criteriaArguments[2]);
		String textSearched = (String)criteriaArguments[3];
		int pageNumber = convertToInteger(criteriaArguments[4]);
		int pageSize = convertToInteger(criteriaArguments[5]);
		Integer daysToLookBack = convertToInteger(criteriaArguments[6]);
		
		return findRecentlyPublishedResourcesInternal(
				commaDelimitedContentObjectTypes, 
				commaOrPlusDelimitedTopicNames, 
				searchInSubtopics, 
				textSearched, 
				pageNumber, 
				pageSize, 
				daysToLookBack);
	}
	
	public ResourceResponse<ContentObject, T> findMostPopularResources(Object... criteriaArguments) {
			String commaDelimitedContentObjectTypes = (String)criteriaArguments[0];
			String commaOrPlusDelimitedTopicNames = (String)criteriaArguments[1];
			boolean searchInSubtopics = convertToBoolean(criteriaArguments[2]);
			String textSearched = (String)criteriaArguments[3];
			int pageNumber =  convertToInteger(criteriaArguments[4]);
			int pageSize = convertToInteger(criteriaArguments[5]);
			Integer daysToLookBack = convertToInteger(criteriaArguments[6]);
		
		return findMostPopularResourcesInternal(
				commaDelimitedContentObjectTypes, 
				commaOrPlusDelimitedTopicNames, 
				searchInSubtopics, 
				textSearched, 
				pageNumber, 
				pageSize, 
				daysToLookBack);
	}
	
	public String showResourceBySystemNameOrIdByGET(){
		if (StringUtils.isBlank(contentObjectSystemNameOrId)){
			return "pageNotFound";
		}
		
		if (CmsConstants.UUIDPattern.matcher(contentObjectSystemNameOrId).matches()){
			resourceResponse = findResourceById(contentObjectSystemNameOrId);
		}
		else{
			resourceResponse = findResourceBySystemName(contentObjectSystemNameOrId);
		}
		
		// update the content object view statistics and create web2 sharing links before providing the resource
		//if (!resourceResponse.getResourceRepresentation().isEmpty()) {
		ContentObject firstResource = resourceResponse.getFirstResource();
		if (firstResource != null) {

			viewCountAggregator.increaseContentObjectViewCounter(firstResource.getId());

			createWeb2SharingLinks(firstResource, resourceResponse.getResourceContext().getWeb2SharingLinks());


			// outject resourceResponse and request parameters to page context. 
			// This is not performed through @Out annotations since we do not want to nullify the resourceResponse and request parameters if some other method in this class is called during the page rendering
			outjectResponseAndSingleResourceRequestParameters();

			return provideResource();
		}
		else {
			return "pageNotFound";
		}
	}
	
	public String showResourceByGET() {
		if (StringUtils.isNotBlank(contentObjectId)){
			resourceResponse = findResourceById(contentObjectId);
		}
		else if (StringUtils.isNotBlank(contentObjectSystemName)){
			//Check by system name
			resourceResponse = findResourceBySystemName(contentObjectSystemName);
		}
		else {
			return "pageNotFound";
		}

		// update the content object view statistics and create web2 sharing links before providing the resource
		//if (!resourceResponse.getResourceRepresentation().isEmpty()) {
		ContentObject firstResource = resourceResponse.getFirstResource();
		if (firstResource != null) {

			viewCountAggregator.increaseContentObjectViewCounter(firstResource.getId());

			createWeb2SharingLinks(firstResource, resourceResponse.getResourceContext().getWeb2SharingLinks());


			// outject resourceResponse and request parameters to page context. 
			// This is not performed through @Out annotations since we do not want to nullify the resourceResponse and request parameters if some other method in this class is called during the page rendering
			outjectResponseAndSingleResourceRequestParameters();

			return provideResource();
		}
		else {
			return "pageNotFound";
		}
	}

	
	
	// this is called when a resource is requested through a URL GET request
	// (i.e. /portal/resource/contentObject/{query_params}
	public String showResourcesByGET() {
		// generate the resourceRequestURL according to the provided parameters
		// the URL is required in order to generate the page scrolling URLs
		// This is not required we get it from the pattern matching
		// generateResourceRequestURLFromRequestParameters();
		
		// if some parameters are missing use defaults
		if (searchInSubtopics == null) {
			searchInSubtopics = false;
		}
		
		if (pageNumber == null) {
			pageNumber = 1;
		}
		
		if (resourceRepresentationTypeIsFeed() && (pageSize == null || pageSize > 30)) {	
			pageSize = PortalIntegerConstants.MAX_FEED_SIZE;
			
		}
		
		if (pageSize == null || pageSize > 30) {
			pageSize = PortalIntegerConstants.DEFAULT_PAGE_SIZE;
		}
		
		if (discreteDates == null) {
			discreteDates = false;
		}
		
		if (StringUtils.isNotBlank(predefinedPeriod)) {
			resourceResponse = findResourcesInternal(
					commaDelimitedContentObjectTypes,
					commaOrPlusDelimitedTopicNames, 
					searchInSubtopics,
					dateProperty, 
					predefinedPeriod,
					discreteDates,
					textSearched,
					queryExpression,
					orderByProperty, 
					order, 
					pageNumber, 
					pageSize, 
					JSFUtilities.getLocaleAsString());
		}
		else {
			resourceResponse = findResourcesInternal(
					commaDelimitedContentObjectTypes,
					commaOrPlusDelimitedTopicNames, 
					searchInSubtopics,
					dateProperty, 
					fromDateInclusive, 
					toDateInclusive,
					discreteDates,
					textSearched,
					queryExpression, 
					orderByProperty, 
					order, 
					pageNumber, 
					pageSize, JSFUtilities.getLocaleAsString());
		}
		
		resourceResponse.getResourceContext().setResourceRequestURL(resourceRequestURL);
		
		// generate the page scrolling URLs
		try {
			generatePageScrollingURLs();
		}
		catch (Exception e) {
			return "error";
		}
		
		// request parameters did not come through a POST but through a GET so copy them to posted parameters
		// to be available to page components that generate post requests
		//copyRequestParametersToPOSTedParameters();
		
		// outject the resourceResponse and request parameters to page context. 
		// This is not performed through @Out annotations since we do not want to nullify the resourceResponse and request parameters if some other method in this class is called during the page rendering
		outjectResponseAndDefaultResourceCollectionRequestParameters();
		
		return provideResources();
	}
	
	public void outjectAttribute(String contextVariable, Object outjectedAttribute) {
		if (outjectedAttribute != null) {
			Contexts.getPageContext().set(contextVariable, outjectedAttribute);
		}
	}
	
	/**
	 * Manually outjects in page scope custom request parameters that may be defined by classes that extend this abstract class.
	 */
	protected abstract void outjectCustomResourceCollectionRequestParameters();
	protected abstract void outjectCustomSingleResourceRequestParameters();
	
	/**
	 * Manually outjects in page scope both the response to URLs requesting resource collections (RESTful portal API calls for content object collections) 
	 * as well as the default request parameters supported by the portal API.
	 * Instead of using Seam @Out annotation, manual outjection is required in order to avoid resetting the response and the parameters if the class is repeatedly called
	 * for rendering a page. This may happen if the resource class generates an html response and the resulting pages has EL expressions that use the resource class to
	 * perform some more content queries. It should be noted that each resource class responds to RESTful calls and at the same time provides utility methods 
	 * utilized inside pages through EL expressions for query the resource.
	 * 
	 * RESOURCE CLASSES WHICH EXTEND THIS ABSTRACT CLASS AND OVERRIDE showResourcesByGET()
	 * SHOULD CALL THIS METHOD INSIDE THE OVERWRITTEN CLASS 
	 */
	protected void outjectResponseAndDefaultResourceCollectionRequestParameters() {	
		outjectAttribute("resourceResponse", resourceResponse);
		outjectAttribute("resourceRequestURL",resourceRequestURL);
		outjectAttribute("commaDelimitedContentObjectTypes", commaDelimitedContentObjectTypes);
		outjectAttribute("commaOrPlusDelimitedTopicNames", commaOrPlusDelimitedTopicNames);
		outjectAttribute("searchInSubtopics", searchInSubtopics);
		outjectAttribute("dateProperty", dateProperty);
		outjectAttribute("fromDateInclusive", fromDateInclusive);
		outjectAttribute("toDateInclusive", toDateInclusive);
		outjectAttribute("predefinedPeriod", predefinedPeriod);
		outjectAttribute("discreteDates", discreteDates);
		outjectAttribute("textSearched", textSearched);
		outjectAttribute("queryExpression", queryExpression);
		outjectAttribute("orderByProperty", orderByProperty);
		outjectAttribute("order", order);
		outjectAttribute("pageNumber", pageNumber);
		outjectAttribute("pageSize", pageSize);
		outjectAttribute("resourceRepresentationTemplate", resourceRepresentationTemplate);
		outjectAttribute("resourceRepresentationType", resourceRepresentationType);
		
		outjectCustomResourceCollectionRequestParameters();
	}
	
	/**
	 * Manually outjects in page scope both the response to URLs requesting a resource (RESTful portal API calls for content object collections) 
	 * as well as the default request parameters supported by the portal API.
	 * Instead of using Seam @Out annotation, manual outjection is required in order to avoid resetting the response and the parameters if the class is repeatedly called
	 * for rendering a page. This may happen if the resource class generates an html response and the resulting pages has EL expressions that use the resource class to
	 * perform some more content queries. It should be noted that each resource class responds to RESTful calls and at the same time provides utility methods 
	 * utilized inside pages through EL expressions for query the resource.
	 * 
	 * RESOURCE CLASSES WHICH EXTEND THIS ABSTRACT CLASS AND OVERRIDE showResourceByGET()
	 * SHOULD CALL THIS METHOD INSIDE THE OVERWRITTEN CLASS 
	 */
	protected void outjectResponseAndSingleResourceRequestParameters() {	
		outjectAttribute("resourceResponse", resourceResponse);
		outjectAttribute("resourceRequestURL",resourceRequestURL);
		outjectAttribute("contentObjectId", contentObjectId);
		outjectAttribute("contentObjectSystemName", contentObjectSystemName);
		outjectAttribute("contentObjectSystemNameOrId", contentObjectSystemNameOrId);
		outjectAttribute("resourceRepresentationTemplate", resourceRepresentationTemplate);
		outjectAttribute("resourceRepresentationType", resourceRepresentationType);
		
		outjectCustomSingleResourceRequestParameters();
	}
	
	
	protected boolean resourceRepresentationTypeIsFeed() {
		if (StringUtils.isNotBlank(resourceRepresentationType) && 
				(resourceRepresentationType.equals(SyndicationFeedType.RSS_VERSION_1_0) ||
						resourceRepresentationType.equals(SyndicationFeedType.RSS_VERSION_2_0) ||
						resourceRepresentationType.equals(SyndicationFeedType.ATOM_VERSION_1_0)
				)
			) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected ResourceResponse<ContentObject, T> findRecentlyPublishedResourcesInternal(
			String commaDelimitedContentObjectTypes,
			String commaOrPlusDelimitedTopicNames, 
			boolean searchInSubtopics,
			String textSearched, 
			int pageNumber, 
			int pageSize,
			Integer daysToLookBack) {

		// is the query limited to a certain publication period?
		Calendar webPublicationDateLimit = null;
		if (daysToLookBack != null) {
			// we are searching only for content objects published the last
			// "daysToLookBack" days
			webPublicationDateLimit = GregorianCalendar.getInstance();
			webPublicationDateLimit.add(Calendar.DAY_OF_MONTH, -daysToLookBack);
		}
		return findResourcesInternal(
				commaDelimitedContentObjectTypes,
				commaOrPlusDelimitedTopicNames, 
				searchInSubtopics, 
				"webPublication.webPublicationStartDate",
				webPublicationDateLimit, 
				null,
				false,
				textSearched,
				null,
				"webPublication.webPublicationStartDate", PortalStringConstants.ORDER_DESCENDING, pageNumber,
				pageSize, JSFUtilities.getLocaleAsString());
	}

	protected ResourceResponse<ContentObject, T> findMostPopularResourcesInternal(
			String commaDelimitedContentObjectTypes,
			String commaOrPlusDelimitedTopicNames, 
			boolean searchInSubtopics,
			String textSearched, 
			int pageNumber, 
			int pageSize,
			Integer daysToLookBack) {

		// is the query limited to a certain publication period?
		Calendar webPublicationDateLimit = null;
		if (daysToLookBack != null) {
			// we are searching only for content objects published the last
			// "daysToLookBack" days
			webPublicationDateLimit = GregorianCalendar.getInstance();
			webPublicationDateLimit.add(Calendar.DAY_OF_MONTH, -daysToLookBack);
		}
		return findResourcesInternal(
				commaDelimitedContentObjectTypes,
				commaOrPlusDelimitedTopicNames, 
				searchInSubtopics,
				"webPublication.webPublicationStartDate",
				webPublicationDateLimit, 
				null,
				false,
				textSearched,
				null,
				"statistic.viewCounter", 
				PortalStringConstants.ORDER_DESCENDING, 
				pageNumber,
				pageSize, JSFUtilities.getLocaleAsString());

	}
	
	
	
	
	/**
	 * Finds the published content objects which meet the following criteria: 
	 * <ul>
	 * <li>
	 * -Their content object type is ANY of the provided in
	 * "commaDelimitedContentObjectTypes" parameter. 
	 * </li>
	 * 
	 * <li>
	 * - Their subject contains
	 * topics whose names are contained in the commaOrPlusDelimitedTopicNames
	 * parameter. If topic names are comma delimited topic criteria are ORed
	 * (searches for ANY topic in subject). If topic names are plus (+)
	 * delimited then topic criteria are ANDed (searches for ALL topics in
	 * subject). 
	 * - If "searchInSubTopics" parameter is true it will also return
	 * content objects which reference topics that are children/ancestors of the
	 * provided topics. 
	 * </li>
	 * 
	 * <li>
	 * - Their "dateProperty" (i.e. profile.created or
	 * webPublication.webPublicationStartDate) has a value that is between
	 * "fromDateInclusive" and "toDateInclusive". 
	 * - if both "fromDateInclusive" and "toDateInclusive" have been provided then the boolean parameter
	 * "discreteDates" determines whether the specified period is interpreted as a continuous time space or as discrete dates.
	 * The usual case is to specify a continuous period with "discreteDates" set to false.
	 * </li>
	 * 
	 * <li>
	 * - Their indexed fields (text fields or binary fields) contain words that start with the provided
	 * "textSearched" - Results are ordered by the "orderByProperty" and the
	 * order (ascending / descending) is provided in the "order" parameter. 
	 * </li>
	 * 
	 * <li>
	 * - The result is returned in pages. The required page and the number of content
	 * objects in each page is specified by the "pageNumber" and "pageSize"
	 * parameters. 
	 * </li>
	 * <li>
	 * - The "locale" parameter is required in order to
	 * appropriately render localized parts of the content object, i.e.
	 * localized topic labels
	 * </li>
	 * @param queryExpression TODO
	 * 
	 * @return
	 */
	protected ResourceResponse<ContentObject, T> findResourcesInternal(
			String commaDelimitedContentObjectTypes,
			String commaOrPlusDelimitedTopicNames, 
			boolean searchInSubtopics,
			String datePropertyPath, 
			Object fromDateInclusive,
			Object toDateInclusive,
			boolean discreteDates,
			String textSearched,
			String queryExpression, 
			String orderByPropertyPath, 
			String order, 
			int pageNumber,
			int pageSize, String locale) {

		ResourceResponse<ContentObject, T> resourceResponse =
			new ResourceResponse<ContentObject, T>();
		
		T resourceContext = newResourceContext();
		resourceResponse.setResourceContext(resourceContext);
		
		List<ContentObject> contentObjects = new ArrayList<ContentObject>();

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		// process commaDelimitedContentObjectTypes
		createCriteriaForContentTypes(
				commaDelimitedContentObjectTypes,
				contentObjectCriteria,
				resourceResponse);

		// process commaOrPlusDelimitedTopicNames, searchInSubtopics
		createCriteriaForTopicNames(
				commaOrPlusDelimitedTopicNames,
				contentObjectCriteria, 
				searchInSubtopics,
				resourceResponse);

		// process dateProperty, fromDateInclusive, toDateInclusive
		createDateCriteria(
				datePropertyPath, 
				fromDateInclusive, 
				toDateInclusive,
				discreteDates,
				contentObjectCriteria,
				resourceResponse);

		// process textSearched
		if (StringUtils.isNotBlank(textSearched)) {
			if (textSearched.split(" ").length == 1) {
				// if only one word is searched then match all words that START with the letters of the searched word 
				contentObjectCriteria
					.addFullTextSearchCriterion(textSearched + "*"); }
			else {
				// else look for an exact match of all words
				// BE AWARE that spaces between the words are treated as an "AND" between the words, 
				// i.e. only fields containing all the words will match. It is not required to find the words in the exact order they are provided or as a phrase.
				// However it is assumed that a higher relevance score is produced by LUCENE when the words are found as a phrase
				// To look for all the worlds as a phrase double quotes around the words are required
				contentObjectCriteria
					.addFullTextSearchCriterion(textSearched);
			}
			
		}
		
		// process criteria expression
		createCriteriaForQueryExpression(
			queryExpression, 
			contentObjectCriteria, 
			resourceResponse);
		
		//add custom criteria
		addCustomCriteria(contentObjectCriteria,resourceResponse);
		
		//add language criterion
		createLanguageCriterion(contentObjectCriteria);
		
		// process orderByProperty, order
		if (StringUtils.isBlank(orderByPropertyPath)) { // set the default which is
													// the
													// "webPublication.webPublicationStartDate"
													// property i.e. order by
													// the date of publication
			orderByPropertyPath = "webPublication.webPublicationStartDate";
		}
		if (StringUtils.isBlank(order)
				|| order.equals(PortalStringConstants.ORDER_DESCENDING)
				|| (!order.equals(PortalStringConstants.ORDER_ASCENDING))) {
			contentObjectCriteria.addOrderProperty(orderByPropertyPath,
					Order.descending);
		} else {
			contentObjectCriteria.addOrderProperty(orderByPropertyPath,
					Order.ascending);
		}

		// process pageNumber and pageSize
		// -1 is specially treated and results to no offset and limit i.e all results are returned
		if (pageSize <= 0 && pageSize != -1) {
			logger
					.warn("A zero or negative result page size was provided. The default page size will be used. Default page size is set to: "
							+ PortalIntegerConstants.DEFAULT_PAGE_SIZE);
			pageSize = PortalIntegerConstants.DEFAULT_PAGE_SIZE;
		}
		
		if (pageSize > 0) {
			int offset = 0;

			if (pageNumber > 0) {
				offset = (pageNumber - 1) * pageSize;
			} else if (pageNumber <= 0) {
				logger
				.warn("A zero or negative results page number was provided. The first results page will be returned");
				pageNumber = 1;
			}
			// *** return the required number of objects starting from offset
			contentObjectCriteria.setOffsetAndLimit(offset, pageSize);
		}
		
		addCustomOffsetAndLimit(contentObjectCriteria);
		
		// process locale
		if (StringUtils.isBlank(locale)) {
			locale = JSFUtilities.getLocaleAsString();
			// locale = PortalStringConstants.DEFAULT_LOCALE.getValue();
		}
		contentObjectCriteria.getRenderProperties().renderValuesForLocale(locale);

		// cache the query
		contentObjectCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_LIST_DEFAULT_CACHE_REGION);

		// now we are ready to run the query
		int totalResourceCount = 0;
		
		try {
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService()
					.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			totalResourceCount = (int) cmsOutcome.getCount();
			
			if (cmsOutcome.getCount() > 0) {
					contentObjects.addAll(cmsOutcome.getResults());
			}
		} catch (Exception e) {
			logger.error(
				"An error occured while searching for content object resources.",
				e);
		}
		
		
		resourceResponse.setResourceRepresentation(contentObjects);
		
		resourceContext.setPageNumber(pageNumber);
		resourceContext.setPageSize(pageSize);
		resourceContext.setTotalResourceCount(totalResourceCount);
		if (totalResourceCount % pageSize == 0) {
			resourceContext.setTotalPages(totalResourceCount / pageSize);
		} else {
			resourceContext.setTotalPages((totalResourceCount / pageSize) + 1);
		}
		// less results than the page size might have been returned
		resourceContext.setCurrentPageSize(contentObjects.size());

		return resourceResponse;
	}


	/*
	 * Specify language criterion to narrow down results
	 * in case an active locale is found
	 */
	protected void createLanguageCriterion(ContentObjectCriteria contentObjectCriteria) {
		
		if (localeSelector != null){
			
			/*
			 * 	Add language criteria only more than one supported locales have been defined.
			 * 	In web applications, it is often the case where only one language is supported.
			 * 	In these cases, we do not want to add a language constraint.
			 * 
			 *  The procedure of defining which locales are supported by a web application is beyond
			 *  the scope of this class. Nevertheless, in a JFS enabled web application locales are defined
			 *  in faces-config.xml file
			 */
			if (moreThanOneLocalesAreSupported()){
				contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.language", localeSelector.getLocaleString()));
			}
		}
		
	}


	private boolean moreThanOneLocalesAreSupported() {
		return localeSelector != null && CollectionUtils.isNotEmpty(localeSelector.getSupportedLocales()) && localeSelector.getSupportedLocales().size() > 1;
	}


	protected abstract T newResourceContext();
	
	// find resources with date criteria as predefined periods
	// look above for an explanation of parameters
	protected ResourceResponse<ContentObject, T> findResourcesInternal(
			String commaDelimitedContentObjectTypes,
			String commaOrPlusDelimitedTopicNames, 
			boolean searchInSubtopics,
			String datePropertyPath, 
			String predefinedPeriodName,
			boolean discreteDates,
			String textSearched,
			String queryExpression,
			String orderByPropertyPath, 
			String order, 
			int pageNumber, 
			int pageSize,
			String locale) {
		
		Calendar fromCalendarInclusive = null;
		Calendar toCalendarInclusive = null;
		if (predefinedPeriodName != null) {
			Calendar[] predefinedCalendarPeriod = calendarUtils.getPredefinedCalendarPeriod(predefinedPeriodName, JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
			fromCalendarInclusive = predefinedCalendarPeriod[0];
			toCalendarInclusive = predefinedCalendarPeriod[1];
			
			// if the predefined period is across a single day and we search for discrete dates the we should nullify the toCalendarInclusive
			if (discreteDates && (predefinedPeriodName.equals("today") || predefinedPeriodName.equals("tomorrow"))) {
				toCalendarInclusive = null;
			}
			
			
		}
		
		return findResourcesInternal(
				commaDelimitedContentObjectTypes, 
				commaOrPlusDelimitedTopicNames, 
				searchInSubtopics, 
				datePropertyPath, 
				fromCalendarInclusive,
				toCalendarInclusive,
				discreteDates, 
				textSearched, 
				queryExpression, 
				orderByPropertyPath, 
				order, 
				pageNumber, 
				pageSize, locale);
	}
	
	
	
	private void createCriteriaForContentTypes(
			String commaDelimitedContentObjectTypes,
			ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {

		if (StringUtils.isNotBlank(commaDelimitedContentObjectTypes)
				&& !commaDelimitedContentObjectTypes
						.equals(PortalStringConstants.ANY_CONTENT_TYPE)) {
			String[] contentObjectTypes = commaDelimitedContentObjectTypes
					.split(",");
			
			// check if provided type definitions exist and add all valid into the response object
			// However the list of type names for use in criteria generation contains both valid and invalid types
			//TODO: the resource context should contain a separate list with invalid types so that the user can be properly informed.
			List<String> contentObjectTypeList = new ArrayList<String>();
			for (String contentObjectType : contentObjectTypes) {
				if (astroboaClient.getDefinitionService().hasContentObjectTypeDefinition(contentObjectType)) {
					resourceResponse.getResourceContext().getContentObjectTypeDefinitions().add((ContentObjectTypeDefinition) astroboaClient.getDefinitionService().getCmsDefinition(contentObjectType, ResourceRepresentationType.DEFINITION_INSTANCE));
				}
				else {
					logger.warn("The provided content object type:" + contentObjectType + " does not exist and will not be added to query criteria");
				}
				contentObjectTypeList.add(contentObjectType); // add it in criteria in any case
			}
			
			if (! contentObjectTypeList.isEmpty()) {
				contentObjectCriteria
					.addContentObjectTypesEqualsAnyCriterion(contentObjectTypeList);
			}
		}

	}

	protected void createCriteriaForTopicNames(
			String commaOrPlusDelimitedTopicNames,
			ContentObjectCriteria contentObjectCriteria,
			boolean searchInSubtopics, 
			ResourceResponse<ContentObject, T> resourceResponse) {
		if (StringUtils.isNotBlank(commaOrPlusDelimitedTopicNames)
				&& !commaOrPlusDelimitedTopicNames
				.equals(PortalStringConstants.ANY_TOPIC)) {

			//It may be the case that topic names are also space delimited
			//This is the result of a utf-8 encoding of the url
			//which replaces + to spaces. 
			//Replace space with plus sign and then proceed with further processing
			//This is safe since topic name may not contain spaces
			commaOrPlusDelimitedTopicNames = StringUtils.replace(commaOrPlusDelimitedTopicNames, " ", "+");

			//Since AND is more powerful than OR first we split topicNames according to +
			//and then we process each entry as usual

			String[] andedTopicNames = StringUtils.split(commaOrPlusDelimitedTopicNames,"+");

			//It cannot be null as commaOrPlusDelimitedTopicNames are not null
			for (String andedTopicName : andedTopicNames){
				
				//In this case, an andedTopicName can either be a comma delimited string
				//or a regular topic 
				
				// if topics names are comma delimited they will be ORed
				// if topic names are delimited by pluses they will be ANDed
				// if there is no comma or plus in the string then only one topic
				// name has been provided and only one criterion will be added to
				// the criteria
				if (StringUtils.contains(andedTopicName, ",")) {
					String[] topicNames = andedTopicName.split(",");
					// outject the topics for use by JSF Pages
					List<Topic> topics = getTopicsForTopicNames(topicNames);
					resourceResponse.getResourceContext().setTopics(topics);
					
					//No need to retrieve topic ids. 
					//TopicReferenceCriterion accepts Topic instances.
					
					// get the ids from topics to create the criterion
					//List<String> topicIds = getTopicIdsFromTopics(topics);
					//contentObjectCriteria.addProfileSubjectIdsCriterion(
					//		QueryOperator.EQUALS, topicIds, Condition.OR,
					//		searchInSubtopics);
					contentObjectCriteria.addCriterion(
							CriterionFactory.newTopicReferenceCriterion(null, topics, Condition.OR, QueryOperator.EQUALS, searchInSubtopics)
							);
				} 

				/*else if (StringUtils
					.contains(commaOrPlusDelimitedTopicNames, "+")) {
				String[] topicNames = commaOrPlusDelimitedTopicNames.split("\\+");
				// outject the topics for use by JSF Pages
				List<Topic> topics = getTopicsForTopicNames(topicNames);
				resourceResponse.getResourceContext().setTopics(topics);
				// get the ids from topics to create the criterion
				List<String> topicIds = getTopicIdsFromTopics(topics);
				contentObjectCriteria.addProfileSubjectIdsCriterion(
						QueryOperator.EQUALS, topicIds, Condition.AND,
						searchInSubtopics);
			}*/
				else { // only one topic name
					Topic topic = cmsUtils.findTopicByTopicName(
							andedTopicName, JSFUtilities
							.getLocaleAsString(), PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);

					if (topic != null) {
						contentObjectCriteria.addCriterion(
								CriterionFactory.newTopicReferenceCriterion(null, topic, QueryOperator.EQUALS, searchInSubtopics)
								);
						resourceResponse.getResourceContext().getTopics().add(topic);
					} else {
						logger
						.warn("No topic could be retrieved with topic name:"
								+ andedTopicName
								+ " This topic will not be added to the search criteria");
					}
				}
			}
		}
	}

	private List<String> getTopicIdsFromTopics(List<Topic> topics) {
		List<String> topicIdsList = new ArrayList<String>();
		for (Topic topic : topics) {
			topicIdsList.add(topic.getId());
		}
		return topicIdsList;
	}

	private List<Topic> getTopicsForTopicNames(String[] topicNames) {
		List<Topic> topicList = new ArrayList<Topic>();
		for (String topicName : topicNames) {
			Topic topic = cmsUtils.findTopicByTopicName(topicName, JSFUtilities
					.getLocaleAsString(), PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);

			if (topic != null) {
				topicList.add(topic);
			} else {
				logger
						.warn("No topic could be retrieved with topic name:"
								+ topicName
								+ " This topic will not be added to the search criteria");
			}
		}

		return topicList;
	}

	private void createDateCriteria(String dateProperty,
			Object fromDateInclusive, Object toDateInclusive, boolean discreteDates,
			ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {

		// if dateProperty is not provided we set the default which is the
		// "webPublication.webPublicationStartDate" property
		if (StringUtils.isBlank(dateProperty)) {
			dateProperty = "webPublication.webPublicationStartDate";
		}

		
		// fromDateInclusive and toDateInclusive can be a calendar object, a date object or a string in the form
		// of YYYY-MM-DD, i.e. 2008-08-22.
		// In the any case we convert it to a calendar object in order to
		// create the search criterion
		Calendar fromCalendarInclusive = calendarUtils.convertObjectToCalendar(fromDateInclusive, JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
		Calendar toCalendarInclusive = calendarUtils.convertObjectToCalendar(toDateInclusive, JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
		
		if (discreteDates) {
			createCriteriaForDiscreteDatesInPeriod(
					dateProperty,
					fromCalendarInclusive,
					toCalendarInclusive,
					contentObjectCriteria,
					resourceResponse);
			return;
		}
		
		createCriteriaForContinuousPeriod(
				dateProperty,
				fromCalendarInclusive,
				toCalendarInclusive,
				contentObjectCriteria,
				resourceResponse);
			
	}
	
	private void createCriteriaForContinuousPeriod(
			String dateProperty,
			Calendar fromCalendarInclusive, 
			Calendar toCalendarInclusive, 
			ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {
		
		// if fromDateInclusive is provided we add the appropriate criterion
		if (fromCalendarInclusive != null) {
			calendarUtils.setCalendarToStartOfDay(fromCalendarInclusive);
			contentObjectCriteria.addCriterion(CriterionFactory.greaterThanOrEquals(dateProperty, fromCalendarInclusive));

			resourceResponse.getResourceContext().setFromCalendar(fromCalendarInclusive);
		}
		

		// if toDateInclusive is provided we add the appropriate criterion
		if (toCalendarInclusive != null) {
			calendarUtils.setCalendarToEndOfDay(toCalendarInclusive);
			contentObjectCriteria.addCriterion(CriterionFactory.lessThanOrEquals(dateProperty, toCalendarInclusive));

			resourceResponse.getResourceContext().setToCalendar(toCalendarInclusive);
		}
	}

	private void createCriteriaForDiscreteDatesInPeriod(
			String dateProperty,
			Calendar fromCalendarInclusive, 
			Calendar toCalendarInclusive, 
			ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {
		
		// get all dates between from fromCalendarInclusive and toCalendarInclusive
		// the returned dates have been cleared from time data (i.e. hours mins, secs, etc.)
		List<Calendar>  discreteDatesInPeriod = calendarUtils.getDiscreteDatesInPeriod(fromCalendarInclusive, toCalendarInclusive);
		
		if (CollectionUtils.isNotEmpty(discreteDatesInPeriod)) {
			contentObjectCriteria.addCriterion(CriterionFactory.equals(dateProperty, Condition.OR, discreteDatesInPeriod));
		}
		
		resourceResponse.getResourceContext().setFromCalendar(fromCalendarInclusive);
		resourceResponse.getResourceContext().setToCalendar(toCalendarInclusive);
	}
	
	
	protected void createCriteriaForQueryExpression(
			String queryExpression,
			ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {
		
		if (StringUtils.isNotBlank(queryExpression)) {
			CriterionFactory.parse(queryExpression, contentObjectCriteria);
			/*
			 * Old simple expression
			 * String[] criteriaExpressions = queryExpression.split(",");
			for (String criterionExpression : criteriaExpressions) {
				String[] propertyValuePair = criterionExpression.split("=");
				contentObjectCriteria.addCriterion(CriterionFactory.equals(propertyValuePair[0], propertyValuePair[1]));
			}*/
		}
	}
	
	
	public ResourceResponse<ContentObject, T> findResourceById(String contentObjectId) {
		
		ResourceResponse<ContentObject, T> resourceResponse =
			new ResourceResponse<ContentObject, T>();

		
		T resourceContext = newResourceContext();
		resourceResponse.setResourceContext(resourceContext);
		
		List<ContentObject> contentObjects = new ArrayList<ContentObject>();
		
		if (contentObjectId == null) {
			logger
					.warn("The provided content object id is null. A null content object will be returned");
			return resourceResponse;
		}
		if (contentObjectId.length() != 36) {
			logger
					.warn("The provided content object id has not the proper length. A null content object will be returned.");
			return resourceResponse;
		}

		// now we may proceed to load the content object
		ContentObject contentObject = null;
		
		try {
			
			contentObject = astroboaClient.getContentService().getContentObject(contentObjectId, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,
					PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION, null, false);
			
			/*ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.addIdEqualsCriterion(contentObjectId);
			
			contentObjectCriteria.getRenderProperties().addRenderInstruction(
					RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE,
					JSFUtilities.getLocaleAsString());
			
			contentObjectCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION);
			
			CmsOutcome<CmsRankedOutcome<ContentObject>> cmsOutcome = astroboaClient.getContentService()
					.searchContentObjects(contentObjectCriteria);
			if (cmsOutcome.getCount() == 1) {
				contentObject = 
				(ContentObject) cmsOutcome.getResults().get(0)
						.getCmsRepositoryEntity();
			}

			if (cmsOutcome.getCount() > 1) {
				logger
						.error("More than one content objects with the same id found. No content object will be returned");
			}
			*/
		} catch (Exception e) {
			logger.error("An error occured while retrievig the content object. A null content object will be returned.", e);
		}
		
		if (contentObject != null) {
			contentObjects.add(contentObject);
			resourceResponse.getResourceContext().setTopics(((TopicProperty)contentObject.getCmsProperty("profile.subject")).getSimpleTypeValues());
		}
		
		resourceResponse.setResourceRepresentation(contentObjects);
		return resourceResponse;
		
	}
	
	
	public ResourceResponse<ContentObject, T> findResourceBySystemName(String contentObjectSystemName, String contentObjectType) {
		ResourceResponse<ContentObject, T> resourceResponse =
			new ResourceResponse<ContentObject, T>();

		
		T resourceContext = newResourceContext();
		resourceResponse.setResourceContext(resourceContext);
		
		List<ContentObject> contentObjects = new ArrayList<ContentObject>();
		
		if (contentObjectSystemName == null) {
			logger
					.warn("The provided content object system name is null. A null content object will be returned");
			return resourceResponse;
		}

		// now we may proceed to load the content object
		ContentObject contentObject = null;
		ContentObjectCriteria contentObjectCriteria;
		
		try {
			if (contentObjectType != null) {
				contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(contentObjectType);
			}
			else {
				contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			}
			
			contentObjectCriteria.addSystemNameEqualsCriterion(contentObjectSystemName);
			
			contentObjectCriteria.getRenderProperties().renderValuesForLocales(Arrays.asList(JSFUtilities.getLocaleAsString()));
			
			contentObjectCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION);
			
			//In cases where there are more than one content objects with the same system name
			//then there is no need to return them all. Just 1 is sufficient
			contentObjectCriteria.setOffsetAndLimit(0, 1);
			
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService()
					.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (cmsOutcome.getCount() >= 1) {
				contentObject = 
				(ContentObject) cmsOutcome.getResults().get(0);
			}

			if (cmsOutcome.getCount() > 1) {
				logger
						.error("More than one content objects with the same system name found. the first will be returned");
			}

		} catch (Exception e) {
			logger.error("An error occured while retrievig the content object. A null content object will be returned.", e);
		}
		
		if (contentObject != null) {
			contentObjects.add(contentObject);
			resourceResponse.getResourceContext().setTopics(((TopicProperty)contentObject.getCmsProperty("profile.subject")).getSimpleTypeValues());
		}
		
		resourceResponse.setResourceRepresentation(contentObjects);
		return resourceResponse;
		
	}
	
	public ResourceResponse<ContentObject, T> findResourceBySystemName(String contentObjectSystemName) {
		return findResourceBySystemName(contentObjectSystemName, null);
	}

	
	
	protected void generatePageScrollingURLs() throws Exception{
		int scrollerStartPage;
		int scrollerEndPage;

		int pagesToPreceedeOrFollow = (PortalIntegerConstants.MAX_PAGES_IN_PAGE_SCROLLER -1) / 2;
		int pagesToPreceed;
		int pagesToFollow;
		if ((PortalIntegerConstants.MAX_PAGES_IN_PAGE_SCROLLER -1) % 2 == 0) {
			pagesToPreceed = pagesToFollow = pagesToPreceedeOrFollow;
		}
		else {
			pagesToPreceed = pagesToPreceedeOrFollow;
			pagesToFollow = pagesToPreceedeOrFollow + 1;
		}
		scrollerStartPage = pageNumber - pagesToPreceed;
		if (scrollerStartPage < 1) {
			scrollerStartPage = 1;
		}
		scrollerEndPage = pageNumber + pagesToFollow;
		
		if (scrollerEndPage < PortalIntegerConstants.MAX_PAGES_IN_PAGE_SCROLLER) {
			scrollerEndPage = PortalIntegerConstants.MAX_PAGES_IN_PAGE_SCROLLER;
		}
		if (scrollerEndPage > resourceResponse.getResourceContext().getTotalPages()) {
			scrollerEndPage = resourceResponse.getResourceContext().getTotalPages();
		}
		

		Pattern pageNumberPatternInRequestURL = Pattern.compile("pageNumber/[0-9]+");		
		
		// if page number is missing from URL we should add it before we proceed to generate the scrolling page URLs
		addMissingPageNumberInRequestURL(pageNumberPatternInRequestURL);
		
		// if text is contained in the request url we should encode it
		if (StringUtils.isNotBlank(textSearched)) {
			Pattern textSearchedPatternInRequestURL = Pattern.compile("textSearched/.*?/");
			Matcher textSearchedMatcher = textSearchedPatternInRequestURL.matcher(resourceRequestURL);
			
			try {
				String encodedTextSearched = URLEncoder.encode(textSearched, "utf-8");
				resourceRequestURL = textSearchedMatcher.replaceFirst("textSearched/" + encodedTextSearched + "/");
			}
			catch (Exception e) {
				logger.error("A problem occured while encoding the searched text", e);
				throw e;
			}
			
		}
		
		Matcher pageNumberMatcher =  pageNumberPatternInRequestURL.matcher(resourceRequestURL);
		
		for (int i = scrollerStartPage; i <= scrollerEndPage; ++i) {
			String scrollerURL = pageNumberMatcher.replaceFirst("pageNumber/" + i);
			PagedResourceURL pagedResourceURL =  new PagedResourceURL();
			pagedResourceURL.setPageNumber(i);
			pagedResourceURL.setURL(scrollerURL);
			resourceResponse.getResourceContext().getPageScrollingURLs().add(pagedResourceURL);
		}
		
		
		resourceResponse.getResourceContext().setFirstPageURL(pageNumberMatcher.replaceFirst("pageNumber/" + 1));
		resourceResponse.getResourceContext().setLastPageURL(pageNumberMatcher.replaceFirst("pageNumber/" + resourceResponse.getResourceContext().getTotalPages()));
	}
	
	private void addMissingPageNumberInRequestURL(Pattern pageNumberPattern) {
		Matcher pageNumberMatcher =  pageNumberPattern.matcher(resourceRequestURL);
		if (pageNumberMatcher.find()) {
			return;
		}
		
		Pattern templateAndTypePatternInRequest = Pattern.compile("(?:/(?:t|template|resourceRepresentationTemplate)/[A-Za-z0-9_\\-]+)?(?:/(?:type|resourceRepresentationType)/(xhtml|rss_2.0|rss_1.0|atom_1.0|pdf|excel))?$");
		Matcher templateAndTypeMatcher = templateAndTypePatternInRequest.matcher(resourceRequestURL);
		
		String pageNumberAndTemplateAndTypeURL = templateAndTypeMatcher.replaceFirst("/pageNumber/1$0");
		resourceRequestURL = pageNumberAndTemplateAndTypeURL;

	}

	protected void createWeb2SharingLinks(ContentObject contentObject, Map<String, String> web2SharingLinks) {
		String portalServerAddress = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestServletPath();
		StringBuilder contentObjectURL = new StringBuilder(100);
		contentObjectURL
			.append(portalServerAddress)
			.append("/resource/contentObject/id/").append(contentObject.getId());

		StringBuilder diggLink = new StringBuilder(500);
		diggLink
			.append("http://digg.com/submit?url=").append(contentObjectURL)
			.append("&amp;title=")
			.append(contentObject.getCmsProperty("profile.title"))
			.append("&amp;bodytext=")
			.append(contentObject.getCmsProperty("profile.description"))
			.append("&amp;media=text&amp;topic=articles");

		web2SharingLinks.put("digg", diggLink.toString());
	}
	
	
	

	protected void forwardToViewId(FacesContext facesContext, String viewId) {
		UIViewRoot viewRoot = facesContext.getApplication().getViewHandler()
		.createView(facesContext, viewId);
		facesContext.setViewRoot(viewRoot);
	}
	
	/**
	 * First checks if the results should be returned as an rss/atom feed without a provided template
	 * In the former case it generates the appropriate default feed output.
	 * In the latter case it finds if a resource representation template exists for the presentation of a list
	 * of content objects (usually returned by a search). 
	 * 
	 * To find the appropriate resource representation template it checks if the "resourceRepresentationTemplate"
	 * has been provided in request parameters.
	 * If not, it tries to guess an appropriate resource representation template to show the results.
	 * By convention "contentObjectListTemplate" is assumed to be the default template name for presenting
	 * resource collections.
	 */
	protected String provideResources() {
		
		//Add language suffix when searching for a template only when
		//user has not specified a template and more than one locales
		//are supported
		boolean addLocaleSuffix = StringUtils.isBlank(resourceRepresentationTemplate) && moreThanOneLocalesAreSupported() && localeSelector.getLocaleString() != null;
		
		Topic activeTheme = portalThemeSelector.getTheme();
		
		// The following is a special case
		// A feed is requested but NO feed template has been provided. The resource list feed will be provided through the default feed algorithm
		if (resourceRepresentationTypeIsFeed() && StringUtils.isBlank(resourceRepresentationTemplate)) {  
			return generateFeedResponce();
		}
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		
		String returnedViewId = "/pageNotFound.xhtml";
		
		// resource list will be presented as an xhtml page by default
		if (StringUtils.isBlank(resourceRepresentationType)) {
			resourceRepresentationType = "xhtml";
		}
		
		// set the resource representation template
		// check if the resource representation template has been provided in request parameters
		boolean useFallbackTemplate = false;

		if (StringUtils.isBlank(resourceRepresentationTemplate)) {
			
			//Special case
			//If collection of resources corresponds to one and onle one content type
			//try to find specific template according to content type's name
			//prior to use the default name
			if (StringUtils.isNotBlank(commaDelimitedContentObjectTypes) 
					&& !commaDelimitedContentObjectTypes.equals(PortalStringConstants.ANY_CONTENT_TYPE) 
					&& !commaDelimitedContentObjectTypes.contains(",")){
				resourceRepresentationTemplate = commaDelimitedContentObjectTypes+"ListTemplate";
				useFallbackTemplate = true; //Enable this flag in order to use the default name if no template is found with this one
			}
			else{
				// 	user has not specified template in Resource Collection URI. Use the default
				resourceRepresentationTemplate = "contentObjectListTemplate";
			}
		}
				
		returnedViewId = locateTemplate(addLocaleSuffix, activeTheme, returnedViewId);
		
		if (useFallbackTemplate && StringUtils.equals("/pageNotFound.xhtml", returnedViewId)){
			//Could not find a template with name commaDelimitedContentObjectTypes+"ListTemplate"
			//therefore try default name. This is done for backwards compatibility
			resourceRepresentationTemplate = "contentObjectListTemplate";
			
			//Try again
			returnedViewId = locateTemplate(addLocaleSuffix, activeTheme, returnedViewId);
		}

		forwardToViewId(facesContext, returnedViewId);
		return null;
		
		
		
	}


	private String locateTemplate(boolean addLocaleSuffix, Topic activeTheme,
			String returnedViewId) {
		// Now check if the resource representation template exists as a resourceRepresentationTemplateObject stored in astroboa repository.
		// The object should have a systemName that equals to the provided template name
		// If we find the object and the "$resourceRepresentationType" property is filled then we outject the systemName 
		// so that the dynamicPage.xhtml can use it to retrieve the template content
		/*
		 * In case first condition fails and more than one locales are supported, append
		 * template with available locale
		 */
		if (addLocaleSuffix && resourceRepresentationTemplateExistsInRepository(resourceRepresentationTemplate+getLocaleSuffix(), resourceRepresentationType)) {
			Contexts.getEventContext().set("templateObjectIdOrSystemName", resourceRepresentationTemplate+getLocaleSuffix());
			returnedViewId = "/dynamicPage.xhtml";
		}
		else if (resourceRepresentationTemplateExistsInRepository(resourceRepresentationTemplate, resourceRepresentationType)) {
			Contexts.getEventContext().set("templateObjectIdOrSystemName", resourceRepresentationTemplate);
			returnedViewId = "/dynamicPage.xhtml";
		}
		else {
			
			boolean templateFound = false;
			
			//Alternatively check if the resource representation template exists as an xhtml file stored inside the web application
			// The resourceRepresentationType is ignored in this case. There is a possibility that the mapped template file is not appropriate for the requested type.
			// This is a response of the developer
			String templatePath;
			if (activeTheme != null) {
				templatePath = "/theme/" + activeTheme.getName() + "/template/" + resourceRepresentationTemplate + ".xhtml";
			}
			else {
				templatePath = "/" + resourceRepresentationTemplate + ".xhtml";
			}
			
			ServletContextResource webPageResource = null;
			
			if (addLocaleSuffix){
				String templatePathWithLocale = templatePath.replace(resourceRepresentationTemplate+".xhtml", resourceRepresentationTemplate+getLocaleSuffix()+".xhtml");
				
				webPageResource = new ServletContextResource(
						(ServletContext) FacesContext.getCurrentInstance()
						.getExternalContext().getContext(), templatePathWithLocale);

				if (webPageResource.exists()) {
					returnedViewId = templatePathWithLocale;
					templateFound = true;
				}

			}
			
			if (!templateFound){
				webPageResource = new ServletContextResource(
						(ServletContext) FacesContext.getCurrentInstance()
						.getExternalContext().getContext(), templatePath);

				if (webPageResource.exists()) {
					returnedViewId = templatePath;
					templateFound = true;
				}
			}
			
			if (!templateFound && logger.isDebugEnabled()){
				if (addLocaleSuffix){
					logger.debug("The requested resource representation template: '{}' does not exist in path {}", resourceRepresentationTemplate,
							templatePath.replace(resourceRepresentationTemplate+".xhtml", resourceRepresentationTemplate+getLocaleSuffix()+".xhtml"));
				}
				else{
					logger.debug("The requested resource representation template: '{}' does not exist in path {}", resourceRepresentationTemplate,templatePath);
				}
			}
		}
		return returnedViewId;
	}


	private String getLocaleSuffix() {
		return "_"+localeSelector.getLocaleString();
	}
	
	protected String provideResource() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String returnedViewId = null;
		boolean templateFound = false;
		
		//Add language suffix when searching for a template only when
		//user has not specified a template and more than one locales
		//are supported
		boolean addLocaleSuffix = StringUtils.isBlank(resourceRepresentationTemplate) && moreThanOneLocalesAreSupported() && localeSelector.getLocaleString() != null;

		Topic activeTheme = portalThemeSelector.getTheme();
		
		// resource will be presented as an xhtml page by default
		if (StringUtils.isBlank(resourceRepresentationType)) {
			resourceRepresentationType = "xhtml";
		}
		
		// first check if the resource representation template has been provided in request parameters
		if (StringUtils.isNotBlank(resourceRepresentationTemplate)) {
			// user has specified template in Resource URI
			// Now check if the page template exists as a resourceRepresentationTemplateObject stored in astroboa repository.
			// The object should have a systemName that equals to the provided template name
			// If we find the object and the "$resourceRepresentationType" property is filled then we outject the systemName 
			// so that the dynamicPage.xhtml can use it to retrieve the template content
			if (resourceRepresentationTemplateExistsInRepository(resourceRepresentationTemplate, resourceRepresentationType)) {
				Contexts.getEventContext().set("templateObjectIdOrSystemName", resourceRepresentationTemplate);
				returnedViewId = "/dynamicPage.xhtml";
				templateFound = true;
			}
			else {
				//Alternatively check if page template exists as an xhtml file stored inside the web application.
				// To locate the template we use portalThemSelector to find out if there is a default or user selected theme active.
				// If there is an active theme ($activeTheme) then the template should be located under $webppContext/theme/$activeTheme/template/$resourceRepresentationTemplate.xhtml
				// If there are no themes enabled for the portal then the template should reside in $webppContext/$resourceRepresentationTemplate.xhtml
				// The resourceRepresentationType is ignored in this case. There is a possibility that the mapped template file is not appropriate for the requested type.
				String templatePath;
				if (activeTheme != null) {
					templatePath = "/theme/" + activeTheme.getName() + "/template/" + resourceRepresentationTemplate + ".xhtml";
				}
				else {
					templatePath = "/" + resourceRepresentationTemplate + ".xhtml";
				}
				
				ServletContextResource webPageResource = new ServletContextResource(
						(ServletContext) FacesContext.getCurrentInstance()
						.getExternalContext().getContext(), templatePath);

				if (webPageResource.exists()) {
					returnedViewId = templatePath;
					templateFound = true;
				} 
			}

		}

		// check if the content object has a value stored in "resourceRepresentationTemplateObjectReference" property
		if (!templateFound) {
			if (resourceResponse.getFirstResource() != null){
				ContentObjectProperty templateObjectProperty = (ContentObjectProperty)resourceResponse.getFirstResource().getCmsProperty("resourceRepresentationTemplateObjectReference");
				if (templateObjectProperty.hasValues()) {
					StringProperty templateProperty = (StringProperty)templateObjectProperty.getSimpleTypeValue().getCmsProperty(resourceRepresentationType);
					if (templateProperty.hasValues()) {
						Contexts.getPageContext().set("templateObjectIdOrSystemName", templateObjectProperty.getSimpleTypeValue().getId());
						returnedViewId = "/dynamicPage.xhtml";
						templateFound = true;
					}	
				}
			}
		}
		
		// check if the content object has a value stored in "resourceRepresentationTemplateName" property
		// The resourceRepresentationType is ignored in this case. There is a possibility that the mapped template file is not appropriate for the requested type.
		if (!templateFound) {
			if ( resourceResponse.getFirstResource() != null && 
				((TopicProperty)((ContentObject) resourceResponse.getFirstResource()).getCmsProperty("resourceRepresentationTemplateName")).hasValues()){

				Topic pageTemplate = ((TopicProperty)((ContentObject) resourceResponse.getFirstResource()).getCmsProperty("resourceRepresentationTemplateName")).getSimpleTypeValue();

				String templatePath;
				if (activeTheme != null) {
					templatePath = "/theme/" + activeTheme.getName() + "/template/" + pageTemplate.getName() + ".xhtml";
				}
				else {
					templatePath = "/" + pageTemplate.getName() + ".xhtml";
				}
				
				ServletContextResource webPageResource = null;
				
				if (addLocaleSuffix){
					//Check using locale
					String templatePathWithLocale = templatePath.replace(pageTemplate.getName() + ".xhtml", pageTemplate.getName() + getLocaleSuffix()+ ".xhtml");
					webPageResource = new ServletContextResource(
							(ServletContext) FacesContext.getCurrentInstance()
							.getExternalContext().getContext(), templatePathWithLocale);
					
					if (webPageResource.exists()) {
						returnedViewId = templatePathWithLocale;
						templateFound = true;
					}
				}

				if (!templateFound){
					//Try without locale
					webPageResource = new ServletContextResource(
							(ServletContext) FacesContext.getCurrentInstance()
							.getExternalContext().getContext(), templatePath);
					
					if (webPageResource.exists()) {
						returnedViewId = templatePath;
						templateFound = true;
					}

				}
			}
		}
		
		// check if a template named after the contentObjectType exists in repository
		// the convention is that the system name of the template should be equal to contentObjectType suffixed with "Template" 
		// e.g. "portalSectionObjectTemplate" for content objects of type "portalSectionObject" 
		if (!templateFound) {
			if (resourceResponse.getFirstResource() != null){
				String contentObjectType = ((ContentObject) resourceResponse.getFirstResource()).getContentObjectType();
				String templateSystemName = contentObjectType+"Template";

				if (addLocaleSuffix && resourceRepresentationTemplateExistsInRepository(templateSystemName+getLocaleSuffix(), resourceRepresentationType)) {
					Contexts.getPageContext().set("templateObjectIdOrSystemName", templateSystemName+getLocaleSuffix());
					returnedViewId = "/dynamicPage.xhtml";
					templateFound = true;
				}
				else if (resourceRepresentationTemplateExistsInRepository(templateSystemName, resourceRepresentationType)) {
					Contexts.getPageContext().set("templateObjectIdOrSystemName", templateSystemName);
					returnedViewId = "/dynamicPage.xhtml";
					templateFound = true;
				}
			}
		}
		
		// Last chance!!!  check if a template named after the contentType exists inside the web app
		// the convention is that the file name of the template should be equal to contentObjectType suffixed with "Template.xhtml" 
		// e.g. "portalSectionObjectTemplate.xhtml" for content objects of type "portalSectionObject"
		// The resourceRepresentationType is ignored in this case. There is a possibility that the mapped template file is not appropriate for the requested type.
		if (!templateFound) {
			if (resourceResponse.getFirstResource() != null){
				String contentObjectType = ((ContentObject) resourceResponse.getFirstResource()).getContentObjectType();
				String templateFileNameWithoutXhtmlSuffix = contentObjectType+"Template";
				String templateFileName = templateFileNameWithoutXhtmlSuffix+".xhtml";

				String templatePath;
				if (activeTheme != null) {
					templatePath = "/theme/" + activeTheme.getName() + "/template/" + templateFileName;
				}
				else {
					templatePath = "/" + templateFileName;
				}

				ServletContextResource webPageResource = null;

				if (addLocaleSuffix){
					String templatePathWithLocale = templatePath.replace(templateFileName, templateFileNameWithoutXhtmlSuffix+getLocaleSuffix()+".xhtml");

					webPageResource = new ServletContextResource(
							(ServletContext) FacesContext.getCurrentInstance()
							.getExternalContext().getContext(), templatePathWithLocale);

					if (webPageResource.exists()) {
						returnedViewId = templatePathWithLocale;
						templateFound = true;
					}
				}

				if (! templateFound){
					webPageResource = new ServletContextResource(
							(ServletContext) FacesContext.getCurrentInstance()
							.getExternalContext().getContext(), templatePath);

					if (webPageResource.exists()) {
						returnedViewId = templatePath;
						templateFound = true;
					}
				}
			}
		}
		
		if (!templateFound) {
			// No luck yet! But still two alternatives: 
			
			// If the type of object is "resourceRepresentationTemplateObject" then return as viewId the dynamicPage and as the template the object itself.
			// In this way we may directly render any template by just calling the through their resource URL.
			
			// In any other case use the default page template for all content objects which should be named "contentObjectTemplate"
				
			// so lets check if the object type is "resourceRepresentationTemplateObject"
			if (resourceResponse.getFirstResource() != null &&
					"resourceRepresentationTemplateObject".equals(((ContentObject) resourceResponse.getFirstResource()).getContentObjectType())){
				Contexts.getPageContext().set("templateObjectIdOrSystemName", ((ContentObject) resourceResponse.getFirstResource()).getSystemName());
				returnedViewId = "/dynamicPage.xhtml";
				templateFound = true;
			}
			else {
				// check if the default template exists in repository
				if (addLocaleSuffix && resourceRepresentationTemplateExistsInRepository("contentObjectTemplate"+getLocaleSuffix(), resourceRepresentationType)) {
					Contexts.getEventContext().set("templateObjectIdOrSystemName", "contentObjectTemplate"+getLocaleSuffix());
					returnedViewId = "dynamicPage";
					templateFound = true;
				}
				else if (resourceRepresentationTemplateExistsInRepository("contentObjectTemplate", resourceRepresentationType)) {
					Contexts.getEventContext().set("templateObjectIdOrSystemName", "contentObjectTemplate");
					returnedViewId = "dynamicPage";
					templateFound = true;
				}
				else {
					//Alternatively check if default page template exists as an xhtml file stored inside the web application
					// The resourceRepresentationType is ignored in this case. There is a possibility that the mapped template file is not appropriate for the requested type.
					String templatePath;
					if (activeTheme != null) {
						templatePath = "/theme/" + activeTheme.getName() + "/template/contentObjectTemplate.xhtml";
					}
					else {
						templatePath = "/contentObjectTemplate.xhtml";
					}
					
					ServletContextResource webPageResource = null;
					
					if (addLocaleSuffix){
						String templatePathWithLocale = templatePath.replace("contentObjectTemplate.xhtml", "contentObjectTemplate"+getLocaleSuffix()+".xhtml");
						
						webPageResource = new ServletContextResource(
								(ServletContext) FacesContext.getCurrentInstance()
								.getExternalContext().getContext(), templatePathWithLocale);
						
						if (webPageResource.exists()) {
							returnedViewId = templatePathWithLocale;
							templateFound = true;
						}
						
					}
					
					if (!templateFound){
						webPageResource = new ServletContextResource(
								(ServletContext) FacesContext.getCurrentInstance()
								.getExternalContext().getContext(), templatePath);
	
						if (webPageResource.exists()) {
							returnedViewId = templatePath;
							templateFound = true;
						}
					}
				}
			}
		}
		
		// bad luck all fallback methods failed
		if (!templateFound) {
			returnedViewId = "/pageNotFound.xhtml";
		}
		
		forwardToViewId(facesContext, returnedViewId);
		return null;
	}
	
	
	
	protected String generateFeedResponce() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/rss+xml");
			response.setCharacterEncoding("UTF-8");
			
			String portalContext = facesContext.getExternalContext().getRequestContextPath();
			
			String portalHost;
			
			/* This code does not always return the external ip of the host and also the ip is not appropriate for generating the feed entries URLs
			String[] portalHostValues = facesContext.getExternalContext().getRequestHeaderValuesMap().get("host");
			
			
			
			if (portalHostValues != null && StringUtils.isNotBlank(portalHostValues[0])) {
				portalHost = portalHostValues[0];
			}
			else {
				return "error";
			}
			*/
			try {
				PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
				portalHost = portalConfiguration.getString(PortalStringConstants.PORTAL_HOST_NAME);
			}
			catch (ConfigurationException e) {
				logger.error("A problem occured while reading portal hostname  from portal configuration file.", e);
				return "error";
			}
			
			if (StringUtils.isBlank(portalHost)) {
				logger.error("The PortalHostName property is not defined inside the portal.properties file. The feed cannot be generated.");
				return "error";
			}
			
			// find if there is a portal thumbnail to be used as the feed image
			String portalThumbnailURL = null;
			BinaryProperty portalThumbnail = (BinaryProperty) portal.getCmsProperty("thumbnail");
			if (portalThumbnail != null && portalThumbnail.getSimpleTypeValue() != null) {
				//portalThumbnailURL = "http://" + portalHost + "/content-api/f/binaryChannel/" + portalThumbnail.getSimpleTypeValue().getFileAccessInfo();
				portalThumbnailURL = "http://" + portalHost + portalThumbnail.getSimpleTypeValue().getRelativeContentApiURL();
			}
			
			try {
				SyndFeed feed = syndicationFeedGenerator.generateFeedForResourceList(resourceResponse, resourceRepresentationType, feedTitle, feedDescription, portalThumbnailURL, portalHost, portalContext, JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
				if (feed != null) {
					SyndFeedOutput output = new SyndFeedOutput();
					output.output(feed, response.getWriter());
				}
				else {
					return "error";
				}
			}
			catch (FeedException fe) {
				logger.error("Error whle generating feed output", fe);
				return "error";
				//response.sendError(
				//		HttpServletResponse.SC_INTERNAL_SERVER_ERROR, fe.getLocalizedMessage());
			}
			catch (IOException ioe) {
				logger.error("Error whle generating feed responce", ioe);
				return "error";
			}

			facesContext.responseComplete();
			return null;
		}
		else {
			return "error";
		}
		
	}
	
	/**
	 * Find if the requested template name exists inside a resourceRepresentationTemplateObject.
	 * 
	 * A resourceRepresentationTemplateObject holds the templates for creating views and representing 
	 * a resource (e.g. content object) or a resource collection in various formats (web page, excel, pdf).
	 * For each different representation format there is a String property in resourceRepresentationTemplateObject 
	 * that holds the template content.
	 * For example the "xhtml" property holds a facelet template for rendering a resource as a web page.
	 *  
	 * The methods gets as input two parameters: 
	 * - the requested template name which corresponds to the system name of a resourceRepresentationTemplateObject
	 * - the type of template to look for that corresponds to a property name that should exist in the resourceRepresentationTemplateObject.
	 * 
	 * With these two parameter the method searches the repository for a resourceRepresentationTemplateObject 
	 * that has a system name as the provided template name and 
	 * also has a property named after the provided template type.
	 * It also checks in the retrieved object if the property value is not blank. 
	 * If all above conditions are met it returns true else it returns false.
	 *  
	 * @param templateName
	 * @param templateType
	 * @return
	 */
	private Boolean resourceRepresentationTemplateExistsInRepository(String templateName, String templateType) {
		if (templateName == null || templateType == null) {
			logger.warn("The provided template name or template type is null. False will be returned");
			return false;
		}

		// find the pageTemplateObject
		ContentObject contentObject = null;
		ContentObjectCriteria contentObjectCriteria;
		
		try {
			
			contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("resourceRepresentationTemplateObject");
			
			
			contentObjectCriteria.addSystemNameEqualsCriterion(templateName);
			// templateType corresponds to the property that should exist inside the object
			contentObjectCriteria.addCriterion(CriterionFactory.isNotNull(templateType));
			
			contentObjectCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION);
			
			//In cases where there are more than one content objects with the same system name
			//then there is no need to return them all. We will get the count to check if more exist with the same systemName
			contentObjectCriteria.setOffsetAndLimit(0, 1);
			
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService()
					.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (cmsOutcome.getCount() >= 1) {
				contentObject = 
				(ContentObject) cmsOutcome.getResults().get(0);
			}

			if (cmsOutcome.getCount() > 1) {
				logger
						.error("More than one content objects with the same system name found. The first will be used to get template content");
			}

		} catch (Exception e) {
			logger.error("An error occured while retrievig the resourceRepresentationTemplateObject. An empty string will be returned as the content of template", e);
		}
		
		if (contentObject != null) {
			String pageTemplateContent = ((StringProperty)contentObject.getCmsProperty(templateType)).getSimpleTypeValue();
			if (StringUtils.isBlank(pageTemplateContent)) {
				logger.warn("The resourceRepresentationTemplateObject with systemName:"+ templateName + "exists but no content has been defined inside property:" + templateType + ". Please fill in the property of this object with the desired template content.");
				return false;
			}
			
			return true;
		}
		else {
			logger.info("No resourceRepresentationTemplateObject Found with systemName:"+ templateName + "that has a non null property named:" + templateType);
			return false;
		}
	}
	
	protected Boolean convertToBoolean(Object methodArgument) {
		Boolean convertedArg;
		
		if (methodArgument == null) {
			return false;
		}
		
		if (methodArgument instanceof Boolean) {
			convertedArg = (Boolean)methodArgument;
		}
		else {
			convertedArg = Boolean.valueOf((String)methodArgument);
		}
		
		return convertedArg;
	}
	
	// method arguments are coming from the UI as Longs, Integers or Strings
	protected Integer convertToInteger(Object methodArgument) {
		Integer convertedArg;
		
		if (methodArgument == null) {
			return 0; // avoid null exceptions
		}
		
		if (methodArgument instanceof Long) {
			convertedArg = ((Long)methodArgument).intValue();
		}
		else if (methodArgument instanceof String){
			convertedArg = Integer.parseInt((String)methodArgument);
		}
		else if (methodArgument instanceof Integer){
			convertedArg = (Integer) methodArgument;
		}
		else {
			return null;
		}
		
		return convertedArg;
	}
	
	//These methods should be abstract. Nevertheless in order to keep
	//backwards compatibility it is declared just as protected with no implementation
	//whatsoever
	protected void addCustomCriteria(ContentObjectCriteria contentObjectCriteria,
			ResourceResponse<ContentObject, T> resourceResponse) {
		
	}

	//Allow user to set their own offset and limit
	//in cases where default algorithm is not sufficient
	//For example there might be cases where different number of results are displayed
	//per page (Very rare case, but it may happen!)
	protected void addCustomOffsetAndLimit(ContentObjectCriteria contentObjectCriteria) {
		
	}



	
}
