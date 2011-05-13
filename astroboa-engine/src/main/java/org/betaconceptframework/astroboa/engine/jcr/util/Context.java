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
package org.betaconceptframework.astroboa.engine.jcr.util;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class Context {

	private  final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	private CmsQueryHandler cmsQueryHandler;
	
	//cache key may be either id or (system name)
	//that is a node may exist twice
	private Map<String, Node> cachedNodes = new HashMap<String, Node>();
	
	private Session session;
	
	public Context(CmsRepositoryEntityUtils cmsRepositoryEntityUtils, CmsQueryHandler cmsQueryHandler,
			Session session) {
		this.cmsRepositoryEntityUtils = cmsRepositoryEntityUtils;
		this.cmsQueryHandler = cmsQueryHandler;
		this.session = session;
	}

	public CmsRepositoryEntityUtils getCmsRepositoryEntityUtils() {
		return cmsRepositoryEntityUtils;
	}
	
	public Node retrieveNodeForBinaryChannel(String id) throws RepositoryException {
		
		Node node = getNodeFromCache(id);
		
		if (node == null){
			node = cmsRepositoryEntityUtils.retrieveUniqueNodeForBinaryChannel(session, id);
			
			cacheNode(node, id);
		}
		
		return node;
	}


	public Node retrieveNodeForEntityId(String cmsRepositoryEntityId) throws RepositoryException {
		Node node = getNodeFromCache(cmsRepositoryEntityId);
		
		if (node == null){
			node = cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntityId(session, cmsRepositoryEntityId);
			
			cacheNode(node, cmsRepositoryEntityId);
		}
		
		return node;
	}
	

	public Node retrieveNodeForEntity(CmsRepositoryEntity cmsRepositoryEntity) throws RepositoryException{
		
		Node node = getNodeFromCache(cmsRepositoryEntity.getId());
		
		if (node == null){
			node = cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntity(session, cmsRepositoryEntity);
			
			cacheNode(node, cmsRepositoryEntity.getId());
		}
		
		return node;

	}
	
	
	public Node retrieveNodeForTopic(String topicIdOrName) throws RepositoryException{
		
		Node node = getNodeFromCache(topicIdOrName);
		
		if (node == null && topicIdOrName != null){
			node = getTopicNodeByIdOrName(topicIdOrName);
			
			cacheNode(node, topicIdOrName);
		}
		
		return node;
		
	}
	
	public Node retrieveNodeForRepositoryUser(String repositoryUserId) throws RepositoryException{
		
		Node node = getNodeFromCache(repositoryUserId);
		
		if (node == null){
			node = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, repositoryUserId);
			
			cacheNode(node, repositoryUserId);
		}
		
		return node;
		
	}

	private void cacheNode(Node node, String id) {
		if (id != null && node != null){
			cachedNodes.put(id, node);
		}
		
	}

	public Node getNodeFromCache(String key) {
		
		if (key == null || ! cachedNodes.containsKey(key)){
			return null;
		}
		
		return cachedNodes.get(key);
	}

	public void dispose() {
		cachedNodes.clear();
	}
	
	private Node getTopicNodeByIdOrName(String topicIdOrName){
		try{
			if (StringUtils.isEmpty(topicIdOrName)){
				return null;
			}
			
			Node topicNode = null;
			
			if (CmsConstants.UUIDPattern.matcher(topicIdOrName).matches()){
				topicNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, topicIdOrName);

				if (topicNode != null){
					return topicNode;
				}
			}
			else{
				TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
				topicCriteria.addNameEqualsCriterion(topicIdOrName);
				topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
				topicCriteria.setOffsetAndLimit(0, 1);
				
				CmsQueryResult nodes = cmsQueryHandler.getNodesFromXPathQuery(session, topicCriteria);
				
				if (nodes.getTotalRowCount() > 1){
					throw new CmsException("There are "+nodes.getTotalRowCount()+" topics with name "+topicIdOrName);
				}
				
				if (nodes.getTotalRowCount() > 0){
					return nodes.getNodeIterator().nextNode();
				}
			}
			
			return null;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public void cacheTopicNode(Node topicNode, boolean useName) throws RepositoryException{
		if (topicNode == null){
			return ;
		}
		
		if (cmsRepositoryEntityUtils.hasCmsIdentifier(topicNode)){
			final String cmsIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(topicNode);
			cachedNodes.put(cmsIdentifier, topicNode);
			logger.debug("Cached jcr node {} for topic by its id {}", topicNode.getPath(), cmsIdentifier);
		}
		
		if (topicNode.hasProperty(CmsBuiltInItem.Name.getJcrName())){
			final String topicName = topicNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString();
			cachedNodes.put(topicName, topicNode);
			logger.debug("Cached jcr node {} for topic by its name {}", topicNode.getPath(), topicName);
		}
		
	}

	public Session getSession() {
		return session;
	}
}
