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

	public CmsOutcome<Topic> getMostlyUsedTopics(String taxonomy, int offset, int limit) throws CmsException  {
		try{
			return topicDao.getMostlyUsedTopics(taxonomy, offset, limit);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public boolean deleteTopicTree(String topicIdOrName) {
		try{
			return topicDao.deleteTopicTree(topicIdOrName);
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
			FetchLevel fetchLevel, boolean prettyPrint) {
		try{
			return topicDao.getTopic(topicIdOrName, output, fetchLevel,prettyPrint);
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

}
