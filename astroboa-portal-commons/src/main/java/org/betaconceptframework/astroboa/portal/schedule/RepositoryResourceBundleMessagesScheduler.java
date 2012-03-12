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
package org.betaconceptframework.astroboa.portal.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.portal.managedbean.RepositoryResourceBundle;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.APPLICATION)
@Name("repositoryResourceBundleMessagesScheduler")
public class RepositoryResourceBundleMessagesScheduler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@In(create=true)
	private AstroboaClient astroboaClient;

	@In
	private RepositoryResourceBundle repositoryResourceBundle;
	
	
	private String taxonomyResourceBundleName;
	
	@Asynchronous
	public QuartzDispatcher loadRepositoryResourceBundle(@Expiration Date when, @IntervalCron String cronInterval) {
		loadRepositoryResourceBundleFromTaxonomy();
		return null;
	}
	
	@Asynchronous
	public QuartzTriggerHandle loadRepositoryResourceBundle(@Expiration Date when, @IntervalDuration Long interval) {
		loadRepositoryResourceBundleFromTaxonomy();
		return null;
	}
	
	
	private void loadRepositoryResourceBundleFromTaxonomy(){
		
		try{
			
			createTaxonomyResourceBundleName();
			
			if (taxonomyResourceBundleName == null){
				logger.warn("Repository {} : Unable to create a valid taxonomy name for repository resource bundle. Either portal.properties file does not exist or it does not contain any value for property {}", 
						(astroboaClient == null ? " null" : astroboaClient.getConnectedRepositoryId()), 
						PortalStringConstants.REPOSITORY_RESOURCE_BUNDLE_TAXONOMY_NAME);
				repositoryResourceBundle.clearResources();
				return;
			}
			
			if (astroboaClient == null){
				logger.warn("Unable to load taxonomy {} which represents repository resource bundle. Astroboa client is not available",taxonomyResourceBundleName);
				repositoryResourceBundle.clearResources();
				return ;
			}
			
			List<Topic> topics = findAllTopics();
			
			if (CollectionUtils.isEmpty(topics)){
				logger.info("Repository {} : Found no topics in taxonomy {} or this taxonomy does not exist. Repository resource bundle will contain no locale-specific resources",
						astroboaClient.getConnectedRepositoryId(), taxonomyResourceBundleName);
				repositoryResourceBundle.clearResources();
				return;
			}
			
			Map<String, Map<String,String>> resourcesPerKey = new HashMap<String, Map<String,String>>();
			
			for (Topic topic : topics){
				
				Map<String, String> localizedLabels = new HashMap<String, String>();
				localizedLabels.putAll(topic.getLocalizedLabels());
				
				resourcesPerKey.put(topic.getName(), localizedLabels);
			}
			
			repositoryResourceBundle.setResources(resourcesPerKey);
			logger.info("Repository {} : Finished loading repository resource bundle from taxonomy {}",
					astroboaClient.getConnectedRepositoryId(), taxonomyResourceBundleName);
		}
		
		catch (Exception e) {
			logger.error("An Error Occured during the loading of repository resource bundle from taxonomy "+taxonomyResourceBundleName, e);
		}
		
	}

	/**
	 * @return
	 */
	private List<Topic> findAllTopics() {

			if (StringUtils.isBlank(taxonomyResourceBundleName)){
				return new ArrayList<Topic>();
			}

			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.addTaxonomyNameEqualsCriterion(taxonomyResourceBundleName);
			
			//We do not want to cache query
			topicCriteria.setCacheable(CacheRegion.NONE);

			CmsOutcome<Topic> outcome = astroboaClient.getTopicService().searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			if (outcome == null || outcome.getCount() == 0){
				return new ArrayList<Topic>();
			}

			return outcome.getResults();

	}

	private void createTaxonomyResourceBundleName() {
		if (taxonomyResourceBundleName == null){
			PropertiesConfiguration portalConfiguration = null;
			try{
				portalConfiguration  = new PropertiesConfiguration("portal.properties");
			}
			catch(Exception e){
				logger.error("",e);
				portalConfiguration=null;
			}
			
			if (portalConfiguration != null){
				taxonomyResourceBundleName = portalConfiguration.getString(PortalStringConstants.REPOSITORY_RESOURCE_BUNDLE_TAXONOMY_NAME);
			}
		}
	}

}
