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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.api.service.secure.TopicServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTopicServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({TopicServiceSecure.class})
@Remote({RemoteTopicServiceSecure.class})
@Stateless(name="TopicServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class TopicServiceSecureImpl extends AbstractSecureAstroboaService implements TopicServiceSecure{

	private TopicService topicService;
	
	@Override
	void initializeOtherRemoteServices() {
		topicService = (TopicService) springManagedRepositoryServicesContext.getBean("topicService");
	}
	
	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public CmsOutcome<Topic> searchTopics(TopicCriteria topicCriteria, String authenticationToken) {
		return topicService.searchTopics(topicCriteria);
	}


	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public boolean deleteTopicTree(String topicIdOrName, String authenticationToken) {
		return topicService.deleteTopicTree(topicIdOrName);
	}
	
	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public CmsOutcome<Topic> getMostlyUsedTopics(String taxonomyName,
			String locale, int offset, int limit, String authenticationToken) {
		return topicService.getMostlyUsedTopics(taxonomyName, locale, offset, limit);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	@Deprecated
	public Topic getTopic(String topicId, String locale, String authenticationToken) {
		return topicService.getTopic(topicId, locale);
	}

	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public Topic saveTopic(Topic topic, String authenticationToken) {
		return topicService.saveTopic(topic);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<String> getContentObjectIdsWhichReferToTopic(String topicId, String authenticationToken) {
		return topicService.getContentObjectIdsWhichReferToTopic(topicId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public int getCountOfContentObjectIdsWhichReferToTopic(String topicId, String authenticationToken) {
		return topicService.getCountOfContentObjectIdsWhichReferToTopic(topicId);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getTopic(String topicIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel, boolean prettyPrint, String authenticationToken) {
		return topicService.getTopic(topicIdOrName, output, fetchLevel, prettyPrint);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public String searchTopicsAndExportToXml(TopicCriteria topicCriteria,
			String authenticationToken) {
		return topicService.searchTopicsAndExportToXml(topicCriteria);
	}

	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public Topic save(Object topic, String authenticationToken) {
		return topicService.save(topic);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T searchTopics(TopicCriteria topicCriteria,
			ResourceRepresentationType<T> output, String authenticationToken) {
		return topicService.searchTopics(topicCriteria, output);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public String searchTopicsAndExportToJson(TopicCriteria topicCriteria,
			String authenticationToken) {
		return topicService.searchTopicsAndExportToJson(topicCriteria);
	}

}
