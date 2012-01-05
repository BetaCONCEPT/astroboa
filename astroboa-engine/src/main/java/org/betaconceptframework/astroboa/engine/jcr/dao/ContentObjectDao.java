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


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNode;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIterator;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIteratorUsingJcrRangeIterator;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.EntityAssociationDeleteHelper;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateContentObject;
import org.betaconceptframework.astroboa.engine.jcr.util.RendererUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.CmsReadOnlyItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.CmsOutcomeImpl;
import org.betaconceptframework.astroboa.model.jaxb.MarshalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectDao {

	private final Logger logger = LoggerFactory.getLogger(ContentObjectDao.class);

	@Autowired
	private ContentDefinitionDao contentDefinitionDao;

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils; 

	@Autowired
	private ContentObjectRenderer contentObjectRenderer;

	@Autowired
	private RendererUtils rendererUtils;


	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Autowired
	private PrototypeFactory prototypeFactory;

	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;



	public void populateContentObjectNode(Session session, ContentObject contentObject, Node contentObjectNode, SaveMode saveMode, Context context) throws Exception{

		ContentObjectTypeDefinition contentObjectTypeDefinition = contentDefinitionDao.getContentObjectTypeDefinition(contentObject.getContentObjectType());

		PopulateContentObject populateContentObject = prototypeFactory.newPopulateContentObject();
		populateContentObject.setContentObject(contentObject);
		populateContentObject.setContentObjectNode(contentObjectNode);
		populateContentObject.setSaveMode(saveMode);
		populateContentObject.setSession(session);
		populateContentObject.setContext(context);
		populateContentObject.populate(contentObjectTypeDefinition);
		
	}

	public  void removeContentObjectNode(Node contentObjectNode, boolean deleteVersionHistory, Session session, Context context) throws RepositoryException  {
		//Remove References
		EntityAssociationDeleteHelper<ContentObject> contentObjectRemover = 
			new EntityAssociationDeleteHelper<ContentObject>(session,cmsRepositoryEntityAssociationDao, context);

		contentObjectRemover.setCmsRepositoryEntityIdToBeRemoved(cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode));
		contentObjectRemover.removeOrReplaceAllReferences(ContentObject.class);

		//Get a reference to content object's history
		VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(contentObjectNode.getPath());

		deleteVersionHistory(versionHistory);

		//		 remove node
		contentObjectNode.remove();
	}

	private  void deleteVersionHistory(VersionHistory versionHistory) throws RepositoryException {
		VersionIterator versions = versionHistory.getAllVersions();
		//Version History is a protected node. Therefore cannot be removed
		//versionHistory.remove(); 

		//Try to remove as much versions as we can
		while (versions.hasNext())
		{
			Version currentVersion = versions.nextVersion();
			String versionName = currentVersion.getName();

			//Jcr:rootVersion is a protected node. Therefore cannot be removed
			//Base Version cannot be removed as well as it is referenced bu VersionHistory.jcr:baseVersion
			if (! versionName.equals(JcrBuiltInItem.JcrRootVersion.getJcrName()) && currentVersion.getReferences().getSize() == 0)
				versionHistory.removeVersion(versionName);
		}
	}



	public ContentObjectCriteria copyContentObjectCriteria(Session session, ContentObjectCriteria contentObjectCriteria) throws Exception {

		ContentObjectCriteria localContentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		if (contentObjectCriteria != null){
			contentObjectCriteria.copyTo(localContentObjectCriteria);
		}

		return localContentObjectCriteria;
	}


	public void increaseViewCounter(Node contentObjectNode, long counter) throws RepositoryException{


		Node statisticNode = null;
		if (contentObjectNode.hasNode("statistic"))
			statisticNode = contentObjectNode.getNode("statistic");
		else{
			statisticNode = JcrNodeUtils.addNodeForComplexCmsProperty(contentObjectNode, "statistic");
			statisticNode.setProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), statisticNode.getIdentifier());
		}

		if (statisticNode == null)
			logger.warn("Could not find or create statistic node for contentObject {}",contentObjectNode.getPath());
		else{

			if (statisticNode.hasProperty(CmsReadOnlyItem.ViewCounter.getJcrName())){
				counter = statisticNode.getProperty(CmsReadOnlyItem.ViewCounter.getJcrName()).getLong() + counter;
			}

			statisticNode.setProperty(CmsReadOnlyItem.ViewCounter.getJcrName(), counter);
		}

	}
	
	
	private <T> CmsOutcome<T> createCmsOutcome(Session session, ContentObjectCriteria contentObjectCriteria, 
			Class<T> outcomeType) throws  Exception {

		//Keep render properties before rendering since during render they may change
		RenderProperties renderPropertiesFromCriteria = contentObjectCriteria.getRenderProperties();
		RenderProperties renderPropertiesForCache = rendererUtils.copyRenderPropertiesFromCriteria(contentObjectCriteria);


		//CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = new CmsQueryResultSecurityHandler(cmsQueryHandler, 
		//	contentObjectCriteria,session, accessManager);
		CmsQueryResult cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, contentObjectCriteria, false);

		//Create outcome
		CmsOutcome<T> outcome = new CmsOutcomeImpl<T>(
				cmsQueryResult.getTotalRowCount(), 
				contentObjectCriteria.getOffset(),
				contentObjectCriteria.getLimit()
		);

		//Limit is zero, that is user has requested no content object
		//JCR implementation (Jackrabbit) takes limit into account only if it is
		//a value greater than 0. Since for Astroboa value -1 stands for unlimited 
		// and value 0 stands for no returned value
		//we have to stop rendering if limit is 0 as Jackrabbit
		//will return a result when calling iterator.hasNext()
		if (contentObjectCriteria.getLimit() != 0){


			// Now order row iterator using order properties
			CmsScoreNodeIterator orderedResults = new CmsScoreNodeIteratorUsingJcrRangeIterator(cmsQueryResult.getRowIterator());

			Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions = new HashMap<String, ContentObjectTypeDefinition>();
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();

			List<String> projections = contentObjectCriteria.getPropertyPathsWhichWillBePreLoaded();
			
			//Render result. 
			while (orderedResults.hasNext()){
				CmsScoreNode nextScoreNode = orderedResults.nextCmsScoreNode();

				T contentObject = null;
				if (outcomeType == CmsRankedOutcome.class){
					contentObject = (T) contentObjectRenderer.renderScoreNode(session, nextScoreNode, renderPropertiesFromCriteria,
							cachedContentObjectTypeDefinitions,cachedCmsRepositoryEntities);
					
					//Preload properties
					if (CollectionUtils.isNotEmpty(projections)){
						loadProjectedPaths(projections, ((CmsRankedOutcome<ContentObject>)contentObject).getCmsRepositoryEntity());
					}
					
					outcome.getResults().add(contentObject);
				}
				else{
					contentObject = (T) contentObjectRenderer.render(session, nextScoreNode.getJcrNode(), 
							renderPropertiesFromCriteria, cachedContentObjectTypeDefinitions, cachedCmsRepositoryEntities);
					
					//Preload properties
					if (CollectionUtils.isNotEmpty(projections)){
						loadProjectedPaths(projections, (ContentObject) contentObject);
					}
					
					((CmsOutcomeImpl)outcome).addResult(contentObject, nextScoreNode.getScore());
				}
				
			}

		}
		//((CmsOutcomeImpl<CmsRankedOutcome<ContentObject>>)outcome).setCount(cmsQueryResultSecurityHandler.getSize());

		//Cache results
		if (contentObjectCriteria.isCacheable()){
			String xpathQuery = contentObjectCriteria.getXPathQuery();
			if (!StringUtils.isBlank(xpathQuery) && outcome != null && outcome.getCount() > 0){
				jcrQueryCacheRegion.cacheJcrQueryResults(contentObjectCriteria, 
						outcome, renderPropertiesForCache);
			}
		}

		return outcome;
	}

	private void loadProjectedPaths(List<String> projections,	ContentObject contentObject) {
		
		for (String projectedPath : projections){

			if (contentObject.getComplexCmsRootProperty().isChildPropertyDefined(projectedPath)) {
				
				CmsPropertyDefinition propertyDefinition = contentObject.getTypeDefinition().getCmsPropertyDefinition(projectedPath);
				
				if (propertyDefinition == null){
					//Look for aspect
					propertyDefinition = contentObject.getComplexCmsRootProperty().getAspectDefinitions().get(projectedPath);
				}
				
				if (propertyDefinition == null){
					throw new CmsException("ComplexCmsRootProperty.isChildPRopertyDefined returned true for property "+projectedPath
							+" but its definition could not " +
							" be retrieved neither from type definition nor from aspect definitions for content object " +
							"(SystemName / Id / ContentType) ("+
							contentObject.getSystemName() + " / "+ contentObject.getId() + " / "+contentObject.getContentObjectType()+")");
				}

				contentObject.getCmsProperty(projectedPath);
				
				if (ValueType.Complex == propertyDefinition.getValueType() && 
						((ComplexCmsPropertyDefinition)propertyDefinition).hasChildCmsPropertyDefinitions()){

					Collection<CmsPropertyDefinition> childPropertyDefinitions = ((ComplexCmsPropertyDefinition)propertyDefinition).getChildCmsPropertyDefinitions().values();

					for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions){
						if (MarshalUtils.propertyShouldBeMarshalled(projections, childPropertyDefinition.getName(), childPropertyDefinition.getPath())){
							contentObject.getCmsProperty(childPropertyDefinition.getPath());
						}
					}
				}
			}
			else{
				logger.warn("Cannot pre load property {} for contentObject (SystemName / Id) ({}) as no such property " +
						"is defined in content object's type '{}'. Also no aspect found with that name as well", 
						new Object[]{projectedPath, contentObject.getSystemName() + " / "+ contentObject.getId(), contentObject.getContentObjectType()});
			}
		}
	}

	public CmsOutcome searchContentObjects(ContentObjectCriteria contentObjectCriteria, Session session, Class<?> outcomeType) throws Exception  {

		ContentObjectCriteria newContentObjectCriteria = copyContentObjectCriteria(session, contentObjectCriteria);

		return createCmsOutcome(session, newContentObjectCriteria, outcomeType);

	}

	/*	Not used due to Jackrabbit pproblems
	 * public CmsOutcome<CmsRankedOutcome<ContentObject>> findContentObjectWhichReferToTopicIdAndTaxonomy(String topicId, String taxonomyName,
			ContentObjectCriteria contentObjectCriteria, Session session) throws Exception  {

		if (StringUtils.isBlank(topicId)){
			return prototypes.newCmsOutcome(0);
		}

		//First use cmsRepositoryEntityAssociationDao to retrieve all topics
		//Do not use taxonomyName for now
		List<String> contentObjectIds = cmsRepositoryEntityAssociationDao.getContentObjectIdsWhichReferToTopicAndTaxonomy(topicId, null);

		if (CollectionUtils.isEmpty(contentObjectIds)){
			return prototypes.newCmsOutcome(0);
		}
		//Now use these ids to query repository to do the proper ordering
		contentObjectCriteria.addCriterion(CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), Condition.OR, contentObjectIds));

		return searchContentObject(contentObjectCriteria, session);

	}*/

}