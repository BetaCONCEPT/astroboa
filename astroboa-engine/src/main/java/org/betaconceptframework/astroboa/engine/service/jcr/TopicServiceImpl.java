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

package org.betaconceptframework.astroboa.engine.service.jcr;


import java.util.List;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.engine.jcr.dao.TopicDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class TopicServiceImpl  implements TopicService {

	@Autowired
	private TopicDao topicDao;

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Topic saveTopic(Topic topic)   {
		try{
			return topicDao.saveTopic(topic, null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public Topic getTopic(String topicId, String locale) {
		try{
			return topicDao.getTopic(topicId, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public CmsOutcome<Topic> searchTopics(TopicCriteria topicCriteria) throws CmsException  {
		try{
			return topicDao.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public CmsOutcome<Topic> getMostlyUsedTopics(String taxonomy, String locale, int offset, int limit) throws CmsException  {
		try{
			return topicDao.getMostlyUsedTopics(taxonomy, locale, offset, limit);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void deleteTopicTree(String topicId) {
		try{
			topicDao.deleteTopicTree(topicId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public List<String> getContentObjectIdsWhichReferToTopic(String topicId) {
		try{
			return topicDao.getContentObjectIdsWhichReferToTopic(topicId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public int getCountOfContentObjectIdsWhichReferToTopic(String topicId) {
		try{
			return topicDao.getCountOfContentObjectIdsWhichReferToTopic(topicId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public <T> T getTopic(String topicIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel) {
		try{
			return topicDao.getTopic(topicIdOrName, output, fetchLevel);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Topic save(Object topic) {
		try{
			return topicDao.saveTopic(topic, null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Override
	public <T> T searchTopics(TopicCriteria topicCriteria, ResourceRepresentationType<T> output) {
		try{
			return topicDao.searchTopics(topicCriteria, output);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Override
	public String searchTopicsAndExportToJson(TopicCriteria topicCriteria) {
		try{ 
			return topicDao.searchTopics(topicCriteria, ResourceRepresentationType.JSON);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}

	@Override
	public String searchTopicsAndExportToXml(TopicCriteria topicCriteria) {
		
		try{
			return topicDao.searchTopics(topicCriteria, ResourceRepresentationType.XML);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
		
	}

}
