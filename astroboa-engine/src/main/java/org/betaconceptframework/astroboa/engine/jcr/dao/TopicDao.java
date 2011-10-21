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
package org.betaconceptframework.astroboa.engine.jcr.dao;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.renderer.TopicRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.RendererUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.TopicUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.query.CmsOutcomeImpl;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class TopicDao extends JcrDaoSupport {

	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private TopicRenderer topicRenderer;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Autowired
	private TopicUtils topicUtils;

	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;

	@Autowired
	private RendererUtils rendererUtils;

	@Autowired
	private SerializationDao serializationDao;

	@Autowired
	private ImportDao importDao;

	public boolean deleteTopicTree(String topicIdOrName) {
		
		if (StringUtils.isBlank(topicIdOrName))
			throw new CmsException("Blank (empty or null) topic id. Could not delete topic tree. ");

		Context context = null;
		
		try {

			Session session = getSession(); 

			Node topicNode = getTopicNodeByIdOrName(topicIdOrName);
			
			if (topicNode == null){
				logger.info("Topic {} does not exist and therefore cannot be deleted", topicIdOrName);
				return false;
			}

			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);

			removeTopicTree(topicNode, context);

			session.save();
			return true;
			//jcrQueryCacheRegion.removeRegion();

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			if (context != null){
				context.dispose();
				context = null;
			}
		}

	}

	public CmsOutcome<Topic> getMostlyUsedTopics(String taxonomy, String locale, int offset, int limit)  {

		try{
			List<String> mostlyUsedTopicIds = cmsRepositoryEntityAssociationDao.getReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedTaxonomyNodeOfTaxonomy(taxonomy);

			Session session = getSession();

			RenderProperties topicRenderProperties = newRenderProperties(locale);

			CmsOutcome<Topic> outcome = new CmsOutcomeImpl<Topic>(mostlyUsedTopicIds.size(), offset, limit);

			//Render only specified range
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();

			if (offset < 0)
				offset = 0;

			if (limit < 0)
				limit = mostlyUsedTopicIds.size();

			for (int i=offset; i< limit && i < mostlyUsedTopicIds.size(); i++ )
			{
				Node topicNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, mostlyUsedTopicIds.get(i));

				renderTopicNode(session, outcome, topicRenderer,
						cachedCmsRepositoryEntities, topicRenderProperties,
						topicNode);
			}

			return outcome;

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}


	private RenderProperties newRenderProperties(String locale) {
		RenderProperties topicRenderProperties = new RenderPropertiesImpl();
		topicRenderProperties.renderValuesForLocale(locale);
		return topicRenderProperties;
	}

	public Topic getTopic(String topicId, String locale) {

		if (StringUtils.isBlank(topicId))
			throw new CmsException("Found no topic to render");

		try {

			Session session = getSession();

			//Retrieve topic
			Node topicNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, topicId);

			RenderProperties renderProperties = newRenderProperties(locale);

			return topicRenderer.renderTopicAndItsParent(topicNode, renderProperties, session, 
					cmsRepositoryEntityFactoryForActiveClient.newTopic(), new HashMap<String, CmsRepositoryEntity>());

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public Topic saveTopic(Object topicSource, Context context) throws CmsException  {

		if (topicSource == null){
			throw new CmsException("Cannot save an empty Topic !");
		}

		if (topicSource instanceof String){
			//Use importer to unmarshal String to Topic
			//and to save it as well.
			//What is happened is that importDao will create a Topic
			//and will pass it here again to save it. 
			ImportConfiguration configuration = ImportConfiguration.topic()
				  .persist(PersistMode.PERSIST_MAIN_ENTITY)
				  .build();

			return importDao.importTopic((String)topicSource, configuration);
		}

		if (! (topicSource instanceof Topic)){
			throw new CmsException("Expecting either String or Topic and not "+topicSource.getClass().getName());
		}

		Topic topic = (Topic) topicSource;

		SaveMode saveMode = null;
		
		
		
		try {

			//Determine SaveMode
			saveMode = cmsRepositoryEntityUtils.determineSaveMode(topic);

			Session session = getSession();

			if (context == null){
				context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
			}

			Node topicNode = null;
			
			//Save Topic
			switch (saveMode) {

			case UPDATE:
				topicNode = topicUtils.updateTopic(session, topic, null, context);
				break;
			case INSERT:
				topicNode = insertTopicNode(session, topic, context);
				break;
			default:
				break;
			}

			session.save();

			if (topicNode != null){
				context.cacheTopicNode(topicNode, true);
			}
			
			//jcrQueryCacheRegion.removeRegion();

			return topic;
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			if (context != null){
				context.dispose();
				context = null;
			}
		}
	}


	private CmsOutcome<Topic> createTopicOutcome(Session session, TopicCriteria topicCriteria) throws Exception  {

		RenderProperties renderPropertiesForCache = rendererUtils.copyRenderPropertiesFromCriteria(topicCriteria);

		//CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = new CmsQueryResultSecurityHandler(cmsQueryHandler, 
		//	topicCriteria,session, accessManager);

		CmsQueryResult cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, topicCriteria);

		CmsOutcome<Topic> outcome = new CmsOutcomeImpl<Topic>(cmsQueryResult.getTotalRowCount(), topicCriteria.getOffset(), topicCriteria.getLimit());

		if (topicCriteria.getLimit() != 0){
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();
			RenderProperties topicRenderProperties = topicCriteria.getRenderProperties();

			NodeIterator nodeIterator = cmsQueryResult.getNodeIterator();

			while (nodeIterator.hasNext())
			{
				Node nextNode = nodeIterator.nextNode();

				renderTopicNode(session, outcome, topicRenderer,
						cachedCmsRepositoryEntities, topicRenderProperties,
						nextNode);
			}
		}

		//((CmsOutcomeImpl<Topic>)outcome).setCount(nodeIterator.getSize());

		//Cache results
		if (topicCriteria.isCacheable()){
			String xpathQuery = topicCriteria.getXPathQuery();
			if (!StringUtils.isBlank(xpathQuery) && outcome != null && outcome.getCount() > 0){
				jcrQueryCacheRegion.cacheJcrQueryResults(topicCriteria, 
						outcome, renderPropertiesForCache);
			}
		}

		return outcome;
	}


	private void renderTopicNode(Session session, CmsOutcome<Topic> outcome,
			TopicRenderer topicRenderer,
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities,
			RenderProperties topicRenderProperties, Node topicNode)
	throws RepositoryException {
		Topic renderRow = topicRenderer.renderNode(session, topicNode, topicRenderProperties, cachedCmsRepositoryEntities);

		outcome.getResults().add(renderRow);
	}




	public  Node insertTopicNode(Session session, Topic topic, Context context) throws RepositoryException {

		//Set Default taxonomy if none exists
		setDefaultTaxonomyIfNoneIsProvided(topic);
		
		Node parentTopicNode = topicUtils.retrieveParentTopicNode(session, topic, context);

		return topicUtils.addNewTopicJcrNode(parentTopicNode, topic, session, false, context);
	}

	private void setDefaultTaxonomyIfNoneIsProvided(Topic topic) {
		if (topic.getTaxonomy() == null){
			if (topic.getParent()!= null && topic.getParent().getTaxonomy()!=null){
				topic.setTaxonomy(topic.getParent().getTaxonomy());
			}
			else{
				cmsRepositoryEntityUtils.addDefaultTaxonomyToTopic(topic);
			}
		}
	}

	public void removeTopicTree(Node topicNode, Context context) throws RepositoryException{
		topicUtils.removeTopicJcrNode(topicNode, getSession(), true, context);
	}

	public Topic saveTopicTree(Topic topicToBeSaved, Context context){
		
		if (context == null){
			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, getSession());
		}
		
		//Save topic
		topicToBeSaved = saveTopic(topicToBeSaved, context);

		//Save its children
		if (topicToBeSaved.isChildrenLoaded()){
			List<Topic> children = topicToBeSaved.getChildren();
			if (CollectionUtils.isNotEmpty(children)){

				for (Topic childTopic : children){
					saveTopicTree(childTopic, context);
				}
			}
		}

		return topicToBeSaved;
	}

	public List<String> getContentObjectIdsWhichReferToTopic(String topicId) {
		try {
			logger.debug("Lazy load referrer content objects for topic {}",topicId);

			return cmsRepositoryEntityAssociationDao.getReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(topicId, Topic.class);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public int getCountOfContentObjectIdsWhichReferToTopic(String topicId) {
		try {
			logger.debug("lazy load numnber of referrer content objects for topic {}",topicId);

			return cmsRepositoryEntityAssociationDao.getCountOfReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(topicId, Topic.class);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T getTopic(String topicIdOrName, ResourceRepresentationType<T> topicOutput, FetchLevel fetchLevel, boolean prettyPrint) {

		ByteArrayOutputStream os = null;

		try {

			Session session = getSession();

			Node topicNode = getTopicNodeByIdOrName(topicIdOrName);

			if (topicNode == null){
				
				if (topicOutput != null && ResourceRepresentationType.TOPIC_LIST.equals(topicOutput)){
					return (T) new CmsOutcomeImpl<Topic>(0, 0, 0);
				}
				
				return null;
			}

			if (topicOutput == null || ResourceRepresentationType.TOPIC_INSTANCE.equals(topicOutput)|| 
					ResourceRepresentationType.TOPIC_LIST.equals(topicOutput)){

				RenderProperties renderProperties = new RenderPropertiesImpl();

				Topic topic = topicRenderer.renderTopicAndItsParent(topicNode, renderProperties, session, 
						cmsRepositoryEntityFactoryForActiveClient.newTopic(), new HashMap<String, CmsRepositoryEntity>());

				//Pre fetch children or tree
				if (fetchLevel != null){

					switch (fetchLevel) {
					case ENTITY_AND_CHILDREN:
						//This way lazy loading will be enabled
						topic.getChildren();
						break;
					case FULL:
						loadAllChildren(topic.getChildren());
						break;
					default:
						break;
					}

				}

				//Return appropriate type
				if (topicOutput == null || ResourceRepresentationType.TOPIC_INSTANCE.equals(topicOutput )){
					return (T) topic;
				}
				else{
					//Return type is CmsOutcome.
					CmsOutcome<Topic> outcome = new CmsOutcomeImpl<Topic>(1, 0, 1);
					outcome.getResults().add(topic);

					return (T) outcome;
				}

			}
			else if (ResourceRepresentationType.XML.equals(topicOutput)|| 
					ResourceRepresentationType.JSON.equals(topicOutput)){


				String topic = null;

				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.topic()
						.prettyPrint(prettyPrint)
						.representationType(topicOutput)
						.build();

				serializationDao.serializeCmsRepositoryEntity(topicNode, os, CmsEntityType.TOPIC, null, fetchLevel, true, serializationConfiguration);

				topic = new String(os.toByteArray(), "UTF-8");

				return (T) topic;
			}
			else{
				throw new CmsException("Invalid resource representation type for topic "+topicOutput);
			}


		}catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}
	}
	
	public Node getTopicNodeByIdOrName(String topicIdOrName){
		try{
			Node topicNode = null;
			
			if (StringUtils.isBlank(topicIdOrName)){
				return null;
			}
			else if (CmsConstants.UUIDPattern.matcher(topicIdOrName).matches()){
				topicNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(getSession(), topicIdOrName);

				if (topicNode != null){
					return topicNode;
				}
			}
			else{
				TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
				topicCriteria.addNameEqualsCriterion(topicIdOrName);
				topicCriteria.setOffsetAndLimit(0, 1);
				
				CmsQueryResult nodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), topicCriteria);
				
				if (nodes.getTotalRowCount() > 0){
					return nodes.getNodeIterator().nextNode();
				}
			}
			
			return null;
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	private void loadAllChildren(List<Topic> children) {

		if (children != null && ! children.isEmpty()){
			for (Topic child : children){
				loadAllChildren(child.getChildren());
			}
		}

	}

	private <T> T returnEmptyResultSet(
			ResourceRepresentationType<T> topicOutput) {
		if (topicOutput.equals(ResourceRepresentationType.TOPIC_LIST)){
			return (T) new CmsOutcomeImpl<T>(0, 0, 0);
		}
		else{
			return null;
		}
	}
	
	private Topic returnSingleTopicFromOutcome(TopicCriteria topicCriteria,
			CmsOutcome<Topic> outcome) {
		//User set limit to 1. Return content object found
		//a result is returned
		if (outcome.getLimit() == 1){
			if (CollectionUtils.isNotEmpty(outcome.getResults())){
				return outcome.getResults().get(0);
			}
			else {
				return null;
			}
		}
		else{
			//User specified limit different than 1 (either no  limit or greater than 1)
			if (outcome.getCount() > 1){
				throw new CmsException(outcome.getCount() +" topics matched criteria, user has specified limit "+
						topicCriteria.getLimit() + " but she also requested return type to be a single Topic.");
			}
			else{
				if (CollectionUtils.isNotEmpty(outcome.getResults())){
					return outcome.getResults().get(0);
				}
				else {
					return null;
				}	
			}
		}
	}


	public <T> T searchTopics(TopicCriteria topicCriteria, ResourceRepresentationType<T> topicOutput){

		T queryResult = null;
		boolean queryReturnedAtLeastOneResult = false;

		ByteArrayOutputStream os = null;

		try {

			//Check if criteria is provided
			if (topicCriteria == null){
				return returnEmptyResultSet(topicOutput);
			}

			//Initialize null parameters (if any) 
			if (topicOutput == null){
				topicOutput = (ResourceRepresentationType<T>) ResourceRepresentationType.TOPIC_LIST;
			}

			//Check cache
			if (topicCriteria.isCacheable()){

				queryResult = (T)jcrQueryCacheRegion.getJcrQueryResults(topicCriteria, topicOutput.getTypeAsString());

				if (queryResult != null){
					return queryResult;
				}
			}

			//User requested Objects as return type
			if (ResourceRepresentationType.TOPIC_INSTANCE.equals(topicOutput)|| 
					ResourceRepresentationType.TOPIC_LIST.equals(topicOutput)){

				CmsOutcome<Topic> outcome = createTopicOutcome(getSession(), topicCriteria);

				//User requested one Topic. Throw an exception if more than
				//one returned
				if (ResourceRepresentationType.TOPIC_INSTANCE.equals(topicOutput )){
					queryResult =  (T) returnSingleTopicFromOutcome(topicCriteria, outcome);
					queryReturnedAtLeastOneResult = queryResult != null;
				}
				else{

					//Return type is CmsOutcome.
					queryResult =  (T) outcome;
					queryReturnedAtLeastOneResult = outcome.getCount() > 0;
				}

			}
			else if (ResourceRepresentationType.XML.equals(topicOutput)|| 
					ResourceRepresentationType.JSON.equals(topicOutput)){


				//User requested output to be XML or JSON
				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.topic()
						.prettyPrint(topicCriteria.getRenderProperties().isPrettyPrintEnabled())
						.representationType(topicOutput)
						.build();

				long numberOfResutls  = serializationDao.serializeSearchResults(getSession(), topicCriteria, os, FetchLevel.ENTITY, serializationConfiguration);

				queryReturnedAtLeastOneResult = numberOfResutls > 0;

				queryResult = (T) new String(os.toByteArray(), "UTF-8");

			}
			else{
				throw new CmsException("Invalid resource representation type for topic search results "+topicOutput);
			}



			if (topicCriteria.isCacheable()){
				String xpathQuery = topicCriteria.getXPathQuery();
				if (!StringUtils.isBlank(xpathQuery) && queryReturnedAtLeastOneResult){
					jcrQueryCacheRegion.cacheJcrQueryResults(topicCriteria, 
							queryResult, topicCriteria.getRenderProperties(), topicOutput.getTypeAsString());
				}
			}

			return queryResult;

		}

		catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}	
	}
}
