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
package org.betaconceptframework.astroboa.test.engine.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.cache.CmsRepositoryCache;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**   
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JcrCacheTest extends AbstractRepositoryTest{

	private CmsRepositoryCache cmsRepositoryCache;
	private JcrQueryCacheRegion jcrQueryCacheRegion;


	@Test
	public void testContentObjectCahe() throws Throwable{
		//Create content object for test
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser, "testContentObjectCache",false);

		contentObject = contentService.save(contentObject, false, true, null);

		//Create one version
		contentObject.setSystemName("testContentObjectCache");
		contentObject = contentService.save(contentObject, false, true, null);

		addEntityToBeDeletedAfterTestIsFinished(contentObject);


		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);

		//Double check directly to cache
		Fqn contentObjectCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(contentObject.getId(), CacheRegion.FIVE_MINUTES);

		Assert.assertFalse(cmsRepositoryCache.getCache().getRoot().hasChild(contentObjectCacheFQN), "ContentObject "+ contentObject.getId() + " has  been cached under region "+ contentObjectCacheFQN);

		//Now cache results in every possible region and perform the same tests
		for (CacheRegion cacheRegion : CacheRegion.values()){

			String contentObjectIdentifier = contentObject.getId();
			
			Fqn parentFqn = Fqn.fromRelativeElements(JcrQueryCacheRegion.JCR_QUERY_NODE_FQN, cacheRegion.getRegionName(), authenticationToken);

			Object contentObjectResult = contentService.getContentObject(contentObjectIdentifier, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE,
					FetchLevel.ENTITY, cacheRegion, null, false);

			checkContentObjectCache(contentObjectResult, cacheRegion,	contentObjectIdentifier, parentFqn, "CONTENT_OBJECT_INSTANCE");
			
			contentObjectResult = contentService.getContentObject(contentObjectIdentifier, ResourceRepresentationType.XML,
					FetchLevel.ENTITY, cacheRegion, null, true);

			checkContentObjectCache(contentObjectResult, cacheRegion,	contentObjectIdentifier, parentFqn, "XML");

			contentObjectResult = contentService.getContentObject(contentObjectIdentifier, ResourceRepresentationType.JSON,
					FetchLevel.ENTITY, cacheRegion, null, true);

			checkContentObjectCache(contentObjectResult, cacheRegion,	contentObjectIdentifier, parentFqn,"JSON");
			
			contentObjectResult = contentService.getContentObject(contentObjectIdentifier, ResourceRepresentationType.CONTENT_OBJECT_LIST,
					FetchLevel.ENTITY, cacheRegion, null, false);

			checkContentObjectCache(contentObjectResult, cacheRegion,	contentObjectIdentifier, parentFqn, "CONTENT_OBJECT_LIST");


		}


		//Finally remove all cached results
		jcrQueryCacheRegion.removeCacheEntriesForAuthenticationToken(authenticationToken);

		for (CacheRegion cacheRegion : CacheRegion.values()){

			Fqn authenticationTokenFqn = Fqn.fromRelativeElements(JcrQueryCacheRegion.JCR_QUERY_NODE_FQN,cacheRegion.getRegionName(), authenticationToken);

			Assert.assertTrue(! cmsRepositoryCache.getCache().getRoot().hasChild(authenticationTokenFqn), "Cache node was not removed "+ authenticationTokenFqn.toString()+ " \n"+ dumpCacheToLog());
		}
	}

	private void checkContentObjectCache(Object returnedResult,
			CacheRegion cacheRegion, String contentObjectIdentifier,
			Fqn parentFqn, String cacheKey) throws Throwable {
		
		if (cacheKey == null){
			cacheKey = contentObjectIdentifier;
		}
		
		try{
			
		Fqn contentObjectCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(contentObjectIdentifier, cacheRegion);
		final Object contentObjectFromCache = jcrQueryCacheRegion.getContentObjectFromCache(contentObjectIdentifier, cacheRegion, cacheKey);
		
		Assert.assertNotNull(returnedResult, "Did not returned newly created content object test for jcr cache "+ cacheRegion);

		if (cacheRegion != CacheRegion.NONE){
			//Results should be cached
			Assert.assertNotNull(contentObjectFromCache, "ContentObject "+contentObjectIdentifier+
					" is not cached in region "+ cacheRegion );

			Assert.assertSame(returnedResult, contentObjectFromCache, " Cache did not return the same objects for caching content object "+
					contentObjectIdentifier+" in region "+ cacheRegion+ " under key "+cacheKey);
			
			Assert.assertTrue(cmsRepositoryCache.getCache().getRoot().hasChild(contentObjectCacheFQN),"ContentObject "+contentObjectIdentifier+" has not been cached in region "+ cacheRegion);
			
			final Node node = (Node)cmsRepositoryCache.getCache().getRoot().getChild(contentObjectCacheFQN);
			
			Assert.assertTrue(node.getKeys().contains(cacheKey),"ContentObject "+contentObjectIdentifier+" has been cached in region "+ cacheRegion +
					" but no key "+ cacheKey + " exists inside this region \n"+
					node.getData());

			//Finally FQN should contain CacheRegion and authentication token
			Assert.assertTrue(contentObjectCacheFQN.isChildOf(parentFqn), "Query results are stored under the wrong FQN " + contentObjectCacheFQN + "Its parent FQN should be "+parentFqn.toString() + " but was "
					+ contentObjectCacheFQN.getParent().toString() + " instead.");
		}
		else{
			//Results should not be cached
			Assert.assertNull(contentObjectFromCache, "ContentObject "+contentObjectIdentifier+" is cached in region "+ cacheRegion );

			//Double check directly to cache
			contentObjectCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(contentObjectIdentifier, cacheRegion);

			Assert.assertFalse(cmsRepositoryCache.getCache().getRoot().hasChild(contentObjectCacheFQN), 
					"ContentObject "+contentObjectIdentifier+
					" has been cached in region "+ cacheRegion);
		}
		}
		catch(Throwable t){
			logger.error("Cache dump " + dumpCacheToLog());
			throw t;
		}
	}

	private String dumpCacheToLog() {
		return dumpNodeToLogError(cmsRepositoryCache.getCache().getRoot());
	}

	@Test
	public void testAuthenticationTokenRegion() throws Exception{

		//Create a test topic
		Topic topic = cmsRepositoryEntityFactory.newTopic();
		topic.setName("topicTestForJcrCache");
		topic.addLocalizedLabel("en", "topicTestForJcrCache");
		topic.setOwner(getSystemUser());
		topic.setTaxonomy(taxonomyService.getBuiltInSubjectTaxonomy("el"));

		topic = topicService.save(topic);

		addEntityToBeDeletedAfterTestIsFinished(topic);

		//Create a simple query

		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.addNameEqualsCriterion("topicTestForJcrCache");
		topicCriteria.setOffsetAndLimit(0,1);
		topicCriteria.doNotCacheResults();

		//Execute query
		CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

		Assert.assertTrue(outcome != null && outcome.getCount() == 1, "Did not returned newly created topic test for jcr cache");

		//Results should not be cached
		Assert.assertNull(jcrQueryCacheRegion.getJcrQueryResults(topicCriteria), "Query results are cached but they should not be."+ " \n"+ dumpCacheToLog() );

		//Double check directly to cache
		Fqn queryCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(topicCriteria.getXPathQuery(), topicCriteria.getCacheRegion());

		Assert.assertTrue(! cmsRepositoryCache.getCache().getRoot().hasChild(queryCacheFQN), "Query "+ topicCriteria.getXPathQuery() + " has been cached under region "+ queryCacheFQN+ " \n"+ dumpCacheToLog());


		//Now cache results in every possible region and perform the same tests
		for (CacheRegion cacheRegion : CacheRegion.values()){

			Fqn parentFqn = Fqn.fromRelativeElements(JcrQueryCacheRegion.JCR_QUERY_NODE_FQN,cacheRegion.getRegionName(), authenticationToken);

			topicCriteria.setCacheable(cacheRegion);

			//Execute query
			outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			Assert.assertTrue(outcome != null && outcome.getCount() == 1, "Did not return newly created topic test for jcr cache. "+ " \n"+ dumpCacheToLog());

			if (cacheRegion != CacheRegion.NONE){
				//Results should be cached
				Assert.assertNotNull(jcrQueryCacheRegion.getJcrQueryResults(topicCriteria), "Query results are not cached in region "+ cacheRegion + " \n"+ dumpCacheToLog());

				//Double check directly to cache
				queryCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(topicCriteria.getXPathQuery(), topicCriteria.getCacheRegion());

				Assert.assertTrue(cmsRepositoryCache.getCache().getRoot().hasChild(queryCacheFQN), "Query "+ topicCriteria.getXPathQuery() + " has not been cached under region "+ queryCacheFQN+ " \n"+ dumpCacheToLog());

				//Finally FQN should contain CacheRegion and authentication token
				Assert.assertTrue(queryCacheFQN.isChildOf(parentFqn), "Query results are stored under the wrong FQN " + queryCacheFQN + "Its parent FQN should be "+parentFqn.toString()+ " but was "
						+ queryCacheFQN.getParent().toString() + " instead."+ " \n"+ dumpCacheToLog());
			}
			else{
				//Results should not be cached
				Assert.assertNull(jcrQueryCacheRegion.getJcrQueryResults(topicCriteria), "Query results are cached in region "+ cacheRegion + " \n"+ dumpCacheToLog());

				//Double check directly to cache
				queryCacheFQN = JcrQueryCacheRegion.constructJcrQueryFQN(topicCriteria.getXPathQuery(), topicCriteria.getCacheRegion());

				Assert.assertFalse(cmsRepositoryCache.getCache().getRoot().hasChild(queryCacheFQN), "Query "+ topicCriteria.getXPathQuery() + " has been cached under region "+ queryCacheFQN+ " \n"+ dumpCacheToLog());

			}
		}


		//Finally remove all cached results
		jcrQueryCacheRegion.removeCacheEntriesForAuthenticationToken(authenticationToken);

		for (CacheRegion cacheRegion : CacheRegion.values()){

			if (cacheRegion != CacheRegion.NONE){
				Fqn authenticationTokenFqn = Fqn.fromRelativeElements(JcrQueryCacheRegion.JCR_QUERY_NODE_FQN,cacheRegion.getRegionName(), authenticationToken);

				Assert.assertTrue(! cmsRepositoryCache.getCache().getRoot().hasChild(authenticationTokenFqn), "Cache node was not removed "+ authenticationTokenFqn.toString()+ " \n"+ dumpCacheToLog());
			}
		}

	}

	private String dumpNodeToLogError(Node node){
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(node.getFqn().toString());
		sb.append("\n");
		
		if (node.dataSize() > 0){
			Map data = node.getData();
			
			sb.append("Data\n");
			
			for (Iterator it = data.keySet().iterator(); it.hasNext();) {
				
				Object key = it.next();
				
				sb.append("\tKey: ");
				sb.append(key);
				
				sb.append("\tValue: ");
				sb.append(data.get(key));
				
				sb.append("\n");
			}
		}
		
		Set children = node.getChildren();
		
		if (CollectionUtils.isNotEmpty(children)){
		
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				
				Node childNode = (Node) iterator.next();
				
				sb.append(dumpNodeToLogError(childNode));
			}
		}
		
		return sb.toString();
		
	}
	
	@Override
	protected void postSetup() throws Exception {
		super.postSetup();

		cmsRepositoryCache = (CmsRepositoryCache) AstroboaTestContext.INSTANCE.getApplicationContext().getBean("cmsRepositoryCache");
		jcrQueryCacheRegion = (JcrQueryCacheRegion) AstroboaTestContext.INSTANCE.getApplicationContext().getBean("jcrQueryCacheRegion");

	}
}
