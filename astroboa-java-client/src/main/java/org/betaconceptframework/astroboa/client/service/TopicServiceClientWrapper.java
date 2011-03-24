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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.api.service.secure.TopicServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Remote Topic Service Wrapper responsible to connect to the provided repository
 * before any of tis method is called. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicServiceClientWrapper extends AbstractClientServiceWrapper implements TopicService{

	private TopicServiceSecure topicServiceSecure;

	public TopicServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	void resetService() {
		topicServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			
			if (loadLocalService){
				topicServiceSecure = (TopicServiceSecure) connectToLocalService(TopicServiceSecure.class);
			}
			else{
				topicServiceSecure = (TopicServiceSecure) connectToRemoteService(TopicServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			topicServiceSecure = null;
		}

		return topicServiceSecure != null;
	}


	public void deleteTopicTree(String topicId) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			topicServiceSecure.deleteTopicTree(topicId, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}


	public List<String> getContentObjectIdsWhichReferToTopic(String topicId) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.getContentObjectIdsWhichReferToTopic(topicId, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}


	public int getCountOfContentObjectIdsWhichReferToTopic(String topicId) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.getCountOfContentObjectIdsWhichReferToTopic(topicId, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}


	public CmsOutcome<Topic> getMostlyUsedTopics(String taxonomyName,
			String locale, int offset, int limit) {

		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.getMostlyUsedTopics(taxonomyName, locale, offset, limit, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}


	@Override
	@Deprecated
	public Topic getTopic(String topicId, String locale) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.getTopic(topicId, locale, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

	public Topic saveTopic(Topic topic) {

		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.saveTopic(topic, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}


	public CmsOutcome<Topic> searchTopics(TopicCriteria topicCriteria) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.searchTopics(topicCriteria, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

	@Override
	public <T> T getTopic(String topicIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.getTopic(topicIdOrName, output, fetchLevel, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}

	}

	@Override
	public Topic save(Object topic) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.save(topic, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

	@Override
	public <T> T searchTopics(TopicCriteria topicCriteria, ResourceRepresentationType<T> output) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.searchTopics(topicCriteria, output, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

	@Override
	public String searchTopicsAndExportToXml(TopicCriteria topicCriteria) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.searchTopicsAndExportToXml(topicCriteria, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

	@Override
	public String searchTopicsAndExportToJson(TopicCriteria topicCriteria) {
		if (topicServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return topicServiceSecure.searchTopicsAndExportToJson(topicCriteria, getAuthenticationToken());
		}
		else{
			throw new CmsException(" TopicService reference was not found");
		}
	}

}
