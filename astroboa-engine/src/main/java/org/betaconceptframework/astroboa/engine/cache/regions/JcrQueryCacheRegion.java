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
package org.betaconceptframework.astroboa.engine.cache.regions;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache region responsible to manage jcr queries and their results
 * 
 * Queries in cache are stored in a specific region named 'jcr-query'.
 * 
 * Each region node's name is the query string and for each node
 * there is a map whose key is the result row range of the query and 
 * value is the outcome of the query.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JcrQueryCacheRegion extends CmsRepositoryCacheRegion{


	private final Logger logger = LoggerFactory.getLogger(JcrQueryCacheRegion.class);

	public final static Fqn<String> JCR_QUERY_NODE_FQN = Fqn.fromString("jcr-query");

	public JcrQueryCacheRegion(boolean enabled, boolean propagateExceptions) {
		super(enabled, propagateExceptions);
	}

	public Object getJcrQueryResults(CmsCriteria cmsCriteria) throws Exception{
		return getJcrQueryResults(cmsCriteria, null);
	}
	
	public Object getJcrQueryResults(CmsCriteria cmsCriteria, String cacheKeyPrefix) throws Exception{

		
		if (!enabled) {
			logger.debug("Query Cache is not Enabled");
			return null;
		}

		try{
			if (cmsCriteria == null || StringUtils.isBlank(cmsCriteria.getXPathQuery()) ) {
				logger.debug("Provided JCR Query is empty or null. Query cache will not be searched.");
				return null;
			}
			
			if (!cmsCriteria.isCacheable()){
				logger.debug("Jcr query {} is not cacheable. Query cache will not be searched", cmsCriteria.getXPathQuery());
				return null;
			}

			String jcrQuery = cmsCriteria.getXPathQuery();
			
			String cacheKey = constructCacheKey(cacheKeyPrefix, cmsCriteria.getOffset(), cmsCriteria.getLimit(), cmsCriteria.getRenderProperties());

			Fqn jcrQueryFQN = constructJcrQueryFQN(jcrQuery, cmsCriteria.getCacheRegion());

			Node rootNode = cmsRepositoryCache.getCache().getRoot();
			
			if (!rootNode.hasChild(jcrQueryFQN)) {
				logger.debug("The following jcr query node does not exist in Query Cache and no results can be retrieved through cache: {}", jcrQueryFQN);
				return null;
			}
			
			Object cachedItem = rootNode.getChild(jcrQueryFQN).get(cacheKey);

			if (cachedItem == null) {
				logger.debug("The following jcr query node exists in Query Cache but no results for the given row range and render properties exist in cache. JcrQueryNode = {} ,Cache Key = {}", jcrQueryFQN, cacheKey);
			}
			else {
				logger.debug("The following jcr query node exists in Query Cache and results for the given row range and render properties also exist in cache and will be returned. JcrQueryNode = {} ,Cache Key = {}",  jcrQueryFQN , cacheKey);
			}
				
			return cachedItem;  
		}
		catch(Exception e){
			if (propagateExceptions)
				throw e;
			else{
				logger.error("Exception in cache ", e);
				return null;
			}

		}
	}

	public static Fqn<String> constructJcrQueryFQN(String jcrQuery, CacheRegion cacheRegion) {

		//replace all '/'  and spaces with empty string
		String jcrQueryWithoutForwardSlashes = StringUtils.remove(jcrQuery, CmsConstants.FORWARD_SLASH);
		
		if (cacheRegion == null){
			cacheRegion = CmsCriteria.DEFAULT_CACHE_REGION;
		}
		
		//Create  Cache Region FQN
		Fqn<String> cacheRegionFqn = Fqn.fromRelativeElements(JCR_QUERY_NODE_FQN, cacheRegion.getRegionName());
		
		//Create FQN for authentication token
		String authenticationToken = AstroboaClientContextHolder.getActiveAuthenticationToken();
		
		if (authenticationToken == null){
			throw new CmsException("No active authenticationToken found. Could not construct appropriate FQN for query "+ jcrQuery);
		}
		
		Fqn<String> autheticationTokenFqn = Fqn.fromRelativeElements(cacheRegionFqn, authenticationToken);


		return Fqn.fromRelativeElements(autheticationTokenFqn, StringUtils.deleteWhitespace(jcrQueryWithoutForwardSlashes));

	}

	public static String constructCacheKey(String cacheKeyPrefix, int offset, int limit, RenderProperties renderProperties) {

		StringBuilder key = new StringBuilder("");
		
		if (cacheKeyPrefix!=null){
			key.append(cacheKeyPrefix);
			key.append("#");
		}
		
		key.append(offset);
		key.append("#");
		key.append(limit);
		
		if (renderProperties != null && MapUtils.isNotEmpty(renderProperties.getRenderInstructions())){

			Map<RenderInstruction, Object> renderInstructions = renderProperties.getRenderInstructions();

			//Get RenderInstructions by specific order
			for (RenderInstruction renderInstruction : RenderInstruction.values()){

				if (renderInstructions.containsKey(renderInstruction)){
					key.append("#");
					key.append(renderInstructions.get(renderInstruction).toString());
				}

			}
		}
		return key.toString();
	}


	public void cacheJcrQueryResults(CmsCriteria cmsCriteria, 
			Object outcome, RenderProperties renderProperties) throws Exception {
		
		cacheJcrQueryResults(cmsCriteria, outcome, renderProperties, null);
		
	}
	
	public void cacheJcrQueryResults(CmsCriteria cmsCriteria, 
			Object outcome, RenderProperties renderProperties, String cacheKeyPrefix) throws Exception {
		//Cache only if a query is provided and outcome is not null
		try{
			String jcrQuery = cmsCriteria.getXPathQuery();
			
			if (enabled && !StringUtils.isBlank(jcrQuery)){

				if (! cmsCriteria.isCacheable()){
					logger.debug("Jcr query {} is not cacheable. Query will not be cached.", jcrQuery);
					return ;
				}

				Cache cache = cmsRepositoryCache.getCache();
				
				Node rootNode = cache.getRoot();

				String cacheKey = constructCacheKey(cacheKeyPrefix, cmsCriteria.getOffset(), cmsCriteria.getLimit(),renderProperties);

				Fqn<String> jcrQueryFQN = constructJcrQueryFQN(jcrQuery, cmsCriteria.getCacheRegion());

				if (!rootNode.hasChild(jcrQueryFQN)) {
					if (outcome != null) {
						logger.debug("The following jcr query node does not exist and will be added in Query Cache along with the row range and render properties key (not null query results). The Jcr Query node to be cached is {} and the cache key is ", jcrQueryFQN , cacheKey);
						
						//Put query results to cache
						cache.put(jcrQueryFQN,cacheKey, outcome);
						
					}
					else {
						logger.debug("The following jcr query node does not exist in Query Cache but WILL NOT CACHED because query result is null. The Jcr Query node is {} and the cache key is {}", jcrQueryFQN ,cacheKey);
					}
				}	
				else{
					//JcrQuery already exists
					Node jcrQueryNode = rootNode.getChild(jcrQueryFQN);
					
					//Write results only if there is no matching rowRangeAndRenderProperties query
					if (jcrQueryNode.get(cacheKey) == null){
						
						if (outcome != null) {
							logger.debug("The following jcr query node exists in Query Cache. The existing Jcr Query node is {}. The row range and properties key does not exist in cache. The key is {}. The query outcome is not null. So a map entry with this key and the query results will be created", 
									jcrQueryFQN, cacheKey);
							jcrQueryNode.put(cacheKey, outcome);
						}
						else{
							//Remove jcrQueryFqn
							// cache.remove(jcrQueryFQN);
							logger.debug("The following jcr query node exists in Query Cache. The existing Jcr Query node is {}. The row range and properties key does not exist in cache. The key is {} . The query outcome IS NULL. So NO map entry with this key and the query results will be created",
									jcrQueryFQN, cacheKey);
						}
					}
					else {
						logger.debug("The following jcr query node exists in Query Cache. The existing Jcr Query node is {}. The cache key {} does also exist in cache. So NO map entry with this key and the query results need to be created",
								jcrQueryFQN, cacheKey);
					}
				}

			}
		}
		catch(Exception e){
			if (propagateExceptions)
				throw e;
			else
				logger.error("Exception in cache ", e);

		}

	}

	public void removeCacheEntriesForAuthenticationToken(String authenticationToken) throws Exception{
		if (enabled){
			try{
				
				if (StringUtils.isNotBlank(authenticationToken)){
					logger.debug("All query results for authentication token {} will be removed",authenticationToken);
					
					Cache cache = cmsRepositoryCache.getCache();
					
					Node rootNode = cache.getRoot();
					
					if (rootNode.hasChild(JCR_QUERY_NODE_FQN)){
						
						//Get all regions under this node. For each region search 
						//for node named after authentication token and remove it
						Set<Node> cacheRegions = rootNode.getChild(JCR_QUERY_NODE_FQN).getChildren();
						
						if (CollectionUtils.isNotEmpty(cacheRegions)){
							
   						   for ( Iterator<Node> iter = cacheRegions.iterator(); iter.hasNext(); ) {
   							   Node cacheRegion = iter.next();
   							   
								if (cacheRegion.hasChild(authenticationToken)){
									
									if (logger.isDebugEnabled()){
										logger.debug("Removing results cached under node {}", cacheRegion.getChild(authenticationToken).getFqn());
									}
									
									cacheRegion.removeChild(authenticationToken);
								}
							}
						}
					}
				}
				else{
					logger.debug("Authentication token is null or empty. Nothing will be removed from cache");
				}
			}
			catch(Exception e){
				if (propagateExceptions)
					throw e;
				else {
					logger.error("Exception in cache ", e);
				}
			}

		}
	}
	/*public void removeRegion() throws Exception {
		if (enabled){
			try{
				logger.debug("The following Query Cache Region will be removed: {} ",JCR_QUERY_NODE_FQN.toString());
				cmsRepositoryCache.getCache().remove(JCR_QUERY_NODE_FQN);
			}
			catch(Exception e){
				if (propagateExceptions)
					throw e;
				else {
					logger.error("Exception in cache ", e);
				}
			}

		}
	}*/

	public Object getContentObjectFromCache(String contentObjectId, CacheRegion cacheRegion) throws Exception {
		return getContentObjectFromCache(contentObjectId, cacheRegion, null);
	}
	
	public Object getContentObjectFromCache(String contentObjectId, CacheRegion cacheRegion, String cacheKey) throws Exception {
		if (!enabled) {
			logger.debug("Query Cache is not Enabled");
			return null;
		}

		try{
			if (StringUtils.isBlank(contentObjectId) ) {
				logger.debug("Provided contentObject id is empty or null. Query cache will not be searched.");
				return null;
			}
			
			if(CacheRegion.NONE == cacheRegion){
				logger.debug("Provided cache region is NONE. Query cache will not be searched.");
				return null;
			}
			
			if (cacheKey == null){
				cacheKey = contentObjectId;
			}
			
			Fqn<String> contentObjectIdFQN = constructJcrQueryFQN(contentObjectId, cacheRegion);

			Node rootNode = cmsRepositoryCache.getCache().getRoot();
			
			if (!rootNode.hasChild(contentObjectIdFQN)) {
				logger.debug("The following cache node for content object id does not exist in Query Cache and no results can be retrieved through cache: {}", contentObjectIdFQN);
				return null;
			}
			
			Object cachedItem = rootNode.getChild(contentObjectIdFQN).get(cacheKey);

			if (logger.isDebugEnabled()){
				if (cachedItem == null) {
					logger.debug("The following cache node for content onbject id does not exist in Query Cache {} under the key {} ", contentObjectIdFQN, cacheKey);
				}
				else {
					logger.debug("The following cache node for content onbject id exists in Query Cache {} under the key {} and will be returned", contentObjectIdFQN, cacheKey);
				}
			}
				
			return cachedItem;  
		}
		catch(Exception e){
			if (propagateExceptions)
				throw e;
			else{
				logger.error("Exception in cache ", e);
				return null;
			}

		}
	}

	public void cacheContentObject(String contentObjectId,
			ContentObject contentObject, CacheRegion cacheRegion) throws Exception {
		cacheContentObject(contentObjectId, contentObject, cacheRegion, null);
	}
	
	public void cacheContentObject(String contentObjectId,
			Object contentObject, CacheRegion cacheRegion, String cacheKey) throws Exception {
		try{
			
			if (enabled && StringUtils.isNotBlank(contentObjectId) && contentObject != null && CacheRegion.NONE != cacheRegion){

				Cache cache = cmsRepositoryCache.getCache();
				
				Node rootNode = cache.getRoot();
				
				if (cacheKey == null){
					cacheKey = contentObjectId;
				}

				Fqn<String> contentObjectIdFQN = constructJcrQueryFQN(contentObjectId, cacheRegion);

				if (!rootNode.hasChild(contentObjectIdFQN)) {
					
					logger.debug("The following content object cache node does not exist and will be added in Query Cache. The content object cache node to be cached is {} under key {}", contentObjectIdFQN, cacheKey);
						
					//Put query results to cache
					cache.put(contentObjectIdFQN, cacheKey, contentObject);

				}	
				else{
					//Content Object already exists
					Node contentOjectCacheNode = rootNode.getChild(contentObjectIdFQN);
					
					//Write results only if there is no matching rowRangeAndRenderProperties query
					if (contentOjectCacheNode.get(cacheKey) == null){
						
						if (contentObject != null) {
							logger.debug("The following content object cache node exists in Query Cache. The existing Jcr Query node is {} . The key is {} and content object is not null. So a map entry with this key and the content object will be created", 
									contentObjectIdFQN, cacheKey);
							
							contentOjectCacheNode.put(cacheKey, contentObject);
						}
						else{
							//Remove Content Object Cache Node
							// cache.remove(jcrQueryFQN);
							logger.debug("The following content object cache node exists in Query Cache. The existing Jcr Query node is {}.The key is {}.The content objec IS NULL. So NO map entry with this key and the content object will be created",
									contentObjectIdFQN, cacheKey);
						}
					}
					else {
						logger.debug("The following content object cache node exists in Query Cache. The existing Jcr Query node is {}. The key is {}. So NO map entry with this key and the content object need to be created",
								contentObjectIdFQN, cacheKey);
					}
				}

			}
		}
		catch(Exception e){
			if (propagateExceptions)
				throw e;
			else
				logger.error("Exception in cache ", e);

		}
		
	}
}
