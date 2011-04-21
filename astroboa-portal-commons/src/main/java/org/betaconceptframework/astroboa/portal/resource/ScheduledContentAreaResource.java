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

import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.portal.utility.PortalCacheConstants;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.PAGE)
@Name("scheduledContentAreaResource")
public class ScheduledContentAreaResource extends AbstractContentObjectResource<ContentObjectResourceContext>{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestParameter
	protected String scheduledContentAreaName;
	
	
	public ResourceResponse<ContentObject, ContentObjectResourceContext> findResourcesActiveNow(String scheduledContentAreaName){
		
		ResourceResponse<ContentObject, ContentObjectResourceContext> resourceResponse =
			new ResourceResponse<ContentObject, ContentObjectResourceContext>();
		
		//Get Topic representing content area
		Topic scheduledContentAreaTopic = cmsUtils.findTopicByTopicName(scheduledContentAreaName, localeSelector.getLocaleString(), 
				PortalCacheConstants.TOPIC_DEFAULT_CACHE_REGION);
		
		if (scheduledContentAreaTopic == null){
			return null;
		}
		
		//Get current time and add minutes required to round minutes
		//For example if minute is 08 then it must become 10, if minute is 51 it must become 00
		//This way although this method fires every time it gets called
		//created query stays the same every 10 minutes. 
		Calendar today = Calendar.getInstance();
		int currentMinute = today.get(Calendar.MINUTE);
		today.add(Calendar.MINUTE, 10 - currentMinute%10);
		
		//Also clear seconds and millis
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		
		ContentObjectCriteria scheduledContentAreaCriteria = CmsCriteriaFactory.newContentObjectCriteria(CmsConstants.SCHEDULED_CONTENT_AREA_CONTENT_OBJECT_TYPE);
		
		scheduledContentAreaCriteria.addCriterion(CriterionFactory.equals("scheduledContentAreaName",scheduledContentAreaTopic.getId()));
		
		Criterion appearanceStartDateLessEqualsThanToday = CriterionFactory.lessThanOrEquals("appearanceStartDate", today);
		Criterion appearanceEndDateGreaterEqualsThanToday = CriterionFactory.greaterThanOrEquals("appearanceEndDate", today);
		
		scheduledContentAreaCriteria.addCriterion(CriterionFactory.and(appearanceStartDateLessEqualsThanToday, appearanceEndDateGreaterEqualsThanToday));
		
		scheduledContentAreaCriteria.setOffsetAndLimit(0, 1);
		
		scheduledContentAreaCriteria.setCacheable(CacheRegion.TEN_MINUTES);
		
		createLanguageCriterion(scheduledContentAreaCriteria);
		
		try {
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(scheduledContentAreaCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
			if (cmsOutcome.getCount() > 0) {
				
				ContentObject scheduledContentArea = cmsOutcome.getResults().get(0);
				//resourceResponse.setResourceRepresentation(scheduledContentArea);
				resourceResponse.setResourceRepresentation(Arrays.asList(scheduledContentArea));
			}
			else{
				logger.warn("Found no scheduled content areas active now with name: " + scheduledContentAreaName + " An empty response object has been returned");
			}
		} catch (Exception e) {
			logger.error(
				"An error occured while searching for scheduled content areas.",e);
		}
		
		return resourceResponse;
	}
	
	public String showActiveReferencedResourcesByGET() {
		if (StringUtils.isBlank(scheduledContentAreaName)) {
			return "pageNotFound";
		}
		
		ResourceResponse<ContentObject, ContentObjectResourceContext> scheduledContentAreaResource = findResourcesActiveNow(scheduledContentAreaName);
		ContentObject activeScheduledContentArea = scheduledContentAreaResource.getFirstResource();
		if (activeScheduledContentArea == null) {
			return "pageNotFound";
		}
		
		resourceResponse = new ResourceResponse<ContentObject, ContentObjectResourceContext>();
		resourceResponse.setResourceContext(new ContentObjectResourceContext());
		resourceResponse.getResourceContext().setResourceRequestURL(resourceRequestURL);
		resourceResponse.setResourceRepresentation(((ObjectReferenceProperty)activeScheduledContentArea.getCmsProperty("referencedContentObjects")).getSimpleTypeValues());
		resourceResponse.getResourceContext().setResourceContainerObject(activeScheduledContentArea);
		outjectResponseAndDefaultResourceCollectionRequestParameters();
		
		return provideResources();
		
	}
	
	@Override
	protected ContentObjectResourceContext newResourceContext() {
		return new ContentObjectResourceContext();
	}

	@Override
	protected void outjectCustomResourceCollectionRequestParameters() {
		
	}

	@Override
	protected void outjectCustomSingleResourceRequestParameters() {
		
	}
	
}
