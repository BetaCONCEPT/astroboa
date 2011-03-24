/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.portal.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.portal.utility.CmsUtils;
import org.betaconceptframework.astroboa.portal.utility.PortalCacheConstants;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.astroboa.portal.utility.RatedTopic;
import org.betaconceptframework.astroboa.portal.utility.RatedTopicComparator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("topicRateScheduler")
public class TopicRateScheduler {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@In(create=true)
	private AstroboaClient astroboaClient;
	
	@In(create=true)
	private CmsUtils cmsUtils;

	@Out
	private List<RatedTopic> ratedSubjectTaxonomyTopics;
	
	@Out
	private List<RatedTopic> subjectTaxonomyCloud;
	
	@Asynchronous
	public QuartzTriggerHandle scheduleSubjectTaxonomyTopicRating(@Expiration Date when, @IntervalDuration Long interval) {
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			
		contentObjectCriteria.getRenderProperties().renderValuesForLocale(PortalStringConstants.DEFAULT_LOCALE);

		// cache the query
		contentObjectCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_LIST_DEFAULT_CACHE_REGION);

		// now we are ready to run the query
		List<ContentObject> relatedContentObjects = new ArrayList<ContentObject>();
		
		try {
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService()
					.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (cmsOutcome.getCount() > 0) {
					relatedContentObjects.addAll(cmsOutcome.getResults());
			}
		} catch (Exception e) {
			logger.error(
				"An error occured while searching for content object resources.",
				e);
		}
			
		ratedSubjectTaxonomyTopics = findRatedTopics(astroboaClient, relatedContentObjects, Taxonomy.SUBJECT_TAXONOMY_NAME, PortalStringConstants.DEFAULT_LOCALE, false, null);
		subjectTaxonomyCloud = cmsUtils.generateTagCloud(ratedSubjectTaxonomyTopics, false, 20, PortalStringConstants.DEFAULT_LOCALE);
		return null;
	}

	
	public List<RatedTopic> findRatedTopics(AstroboaClient astroboaClient, List<ContentObject> contentObjects, String taxonomyName, String locale, boolean orderByTopicLabel, Integer rateLowerLimit){

		List<RatedTopic> ratedTopics = new ArrayList<RatedTopic>();

		if (CollectionUtils.isNotEmpty(contentObjects)){

			Map<String, RatedTopic> ratedTopicsPerId = new HashMap<String, RatedTopic>();

			for (ContentObject contentObject: contentObjects){

				//Retrieve subjects
				TopicProperty subjectProperty = (TopicProperty)contentObject.getCmsProperty("profile.subject");

				if (subjectProperty != null && ! subjectProperty.hasNoValues()){

					for (Topic subject : subjectProperty.getSimpleTypeValues()){

						if (StringUtils.isBlank(taxonomyName) || subject.getTaxonomy().getName().equals(taxonomyName)) {
							cmsUtils.increaseCounterOfTopicAndAncestors(subject, ratedTopicsPerId);
						}
						
					}
				}
			}

			ratedTopics.addAll(ratedTopicsPerId.values());
		}

		return sortRatedTopics(ratedTopics, locale, !orderByTopicLabel, false, rateLowerLimit);

	}
	
	
	private List<RatedTopic> sortRatedTopics(List<RatedTopic> ratedTopics, String locale, boolean orderByRate, boolean ascendingOrderByRate, Integer rateLowerLimit) {
		if (CollectionUtils.isNotEmpty(ratedTopics)){
			Collections.sort(ratedTopics, new RatedTopicComparator(locale, orderByRate, ascendingOrderByRate));

			if (rateLowerLimit !=null){
				//Need to shorten list
				List<RatedTopic> limitedRatedTopics = new ArrayList<RatedTopic>();

				for (RatedTopic ratedTopic: ratedTopics){
					if (ratedTopic.getRate() >= rateLowerLimit){
						limitedRatedTopics.add(ratedTopic);
					}
				}

				return limitedRatedTopics;
			}
			else return ratedTopics;
		}
		
		else return ratedTopics;
	}
	
	
}
