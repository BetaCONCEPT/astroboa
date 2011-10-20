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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.renderer.SpaceRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.RendererUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.SpaceUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
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
public class SpaceDao extends JcrDaoSupport {

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private SpaceUtils spaceUtils;

	@Autowired
	private SpaceRenderer spaceRenderer;

	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Autowired
	private RendererUtils rendererUtils;

	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;

	@Autowired
	private RepositoryUserDao repositoryUserDao;
	
	@Autowired
	private SerializationDao serializationDao;
	
	@Autowired
	private ImportDao importDao;

	public Space saveSpace(Object spaceSource){
		
		if (spaceSource == null){
			throw new CmsException("Cannot save an empty Space !");
		}
		
		if (spaceSource instanceof String){
			//Use importer to unmarshal String to Space
			//and to save it as well.
			//What is happened is that importDao will create a Space
			//and will pass it here again to save it. 
			ImportConfiguration configuration = ImportConfiguration.space()
					  .persist(PersistMode.PERSIST_MAIN_ENTITY)
					  .build();

			return importDao.importSpace((String)spaceSource, configuration);
		}
		
		if (! (spaceSource instanceof Space)){
			throw new CmsException("Expecting either String or Space and not "+spaceSource.getClass().getName());
		}
		
		Space space = (Space) spaceSource;

		Session session = getSession();

		SaveMode saveMode = null;
		Context context = null;
		try {

			//Determine SaveMode
			saveMode = cmsRepositoryEntityUtils.determineSaveMode(space);

			//Replace owner
			spaceUtils.checkSpaceOwner(space, repositoryUserDao.getSystemRepositoryUser(), false);
			
			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
			
			//Save Space
			switch (saveMode) {

			case UPDATE:
				spaceUtils.updateSpace(session, space, null, context);
				break;
			case INSERT:
				insertSpaceNode(session, space, context);
				break;
			default:
				break;
			}

			session.save();

			return space;
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

	private  Node insertSpaceNode(Session session, Space space, Context context) throws RepositoryException {

		Node parentSpaceNode = spaceUtils.retrieveParentSpaceNode(session, space);

		return spaceUtils.addNewSpaceJcrNode(parentSpaceNode, space, session, false, context);
	}



	public boolean deleteSpace(String spaceId) {
		if (StringUtils.isBlank(spaceId)){
			throw new CmsException("Undefined space id ");
		}

		Context context = null;
		try {

			Session session = getSession();

			Node spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, spaceId);

			if (spaceNode == null){
				logger.info("Space {} does not exist and therefore cannot be deleted", spaceId);
				return false;
			}
			
			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);

			removeSpaceNode(spaceNode, context);

			session.save();
			
			return true;
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

	public void removeSpaceNode(Node spaceNode, Context context) throws RepositoryException{
		if (Space.ORGANIZATION_SPACE_NAME.equalsIgnoreCase(spaceNode.getName())){
			throw new CmsException(Space.ORGANIZATION_SPACE_NAME +" cannot be deleted.");
		}

		//This way organization space and repository user space cannot be deleted
		if (spaceNode.getParent() == null || !spaceNode.getParent().isNodeType(CmsBuiltInItem.Space.getJcrName()))
			throw new CmsException("Cannot delete space. Space must have parent another space and not "+ spaceNode.getParent().getName());

		spaceUtils.removeSpaceJcrNode(spaceNode, getSession(), true, context);
	}


	public Space getOrganizationSpace() {
		try {

			Session session = getSession();
			Node organizationSpaceNode = JcrNodeUtils.getOrganizationSpaceNode(session);

			RenderProperties renderProperties = new RenderPropertiesImpl();

			return spaceRenderer.renderSpaceWithoutItsParent(organizationSpaceNode, renderProperties, session,
					cmsRepositoryEntityFactoryForActiveClient.newSpace(), new HashMap<String, CmsRepositoryEntity>());

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public Space saveSpaceTree(Space spaceToBeSaved){
		//Save space
		spaceToBeSaved = saveSpace(spaceToBeSaved);

		//Save its children
		if (spaceToBeSaved.isChildrenLoaded())
		{
			List<Space> children = spaceToBeSaved.getChildren();
			if (CollectionUtils.isNotEmpty(children)){

				for (Space childSpace : children){

					saveSpaceTree(childSpace);
				}
			}
		}
		
		return spaceToBeSaved;
	}
	
	public Space getSpace(String spaceId, String locale) {

		if (StringUtils.isBlank(spaceId))
			throw new CmsException("Found no space to render");

		try {

			Session session = getSession();

			//Retrieve space
			Node spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, spaceId);

			RenderProperties renderProperties = newRenderProperties(locale);

			return spaceRenderer.renderSpaceAndItsParent(spaceNode, renderProperties, session, 
					cmsRepositoryEntityFactoryForActiveClient.newSpace(), new HashMap<String, CmsRepositoryEntity>());

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

	private CmsOutcome<Space> createSpaceOutcome(Session session, SpaceCriteria spaceCriteria) throws Exception  {

		RenderProperties renderPropertiesForCache = rendererUtils.copyRenderPropertiesFromCriteria(spaceCriteria);

		//CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = new CmsQueryResultSecurityHandler(cmsQueryHandler, 
		//		spaceCriteria,session, accessManager);

		CmsQueryResult cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, spaceCriteria);

		CmsOutcome<Space> outcome = new CmsOutcomeImpl<Space>(cmsQueryResult.getTotalRowCount(), spaceCriteria.getOffset(), spaceCriteria.getLimit());

		if (spaceCriteria.getLimit() !=0){
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();
			RenderProperties spaceRenderProperties = spaceCriteria.getRenderProperties();

			NodeIterator nodeIterator = cmsQueryResult.getNodeIterator();

			while (nodeIterator.hasNext())
			{
				Node spaceNode = nodeIterator.nextNode();

				Space space = spaceRenderer.renderNode(session, spaceNode, spaceRenderProperties, cachedCmsRepositoryEntities);

				outcome.getResults().add(space);
			}
		}
		//((CmsOutcomeImpl<Space>)outcome).setCount(cmsQueryResultSecurityHandler.getSize());

		//Cache results
		if (spaceCriteria.isCacheable()){
			String xpathQuery = spaceCriteria.getXPathQuery();
			if (!StringUtils.isBlank(xpathQuery) && outcome != null && outcome.getCount() > 0){
				jcrQueryCacheRegion.cacheJcrQueryResults(spaceCriteria, 
						outcome, renderPropertiesForCache);
			}
		}

		return outcome;
	}

	public List<String> getContentObjectIdsWhichReferToSpace(String spaceId) {
		try {
			logger.debug("Lazy load referrer content objects for space {}", spaceId);

			return cmsRepositoryEntityAssociationDao.getReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(spaceId, Space.class);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public int getCountOfContentObjectIdsWhichReferToSpace(String spaceId) {
		try {
			logger.debug("lazy load numnber of referrer content objects for space {}", spaceId);

			return cmsRepositoryEntityAssociationDao.getCountOfReferrerCmsRepositoryEntityIdsOfAllAssociationsOfReferencedEntity(spaceId,Space.class);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public List<String> getContentObjectIdsWhichResideInSpace(String spaceId) {
		try {
			logger.debug("Lazy load content object references");

			Node spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(getSession(), spaceId);


			if (spaceNode == null){
				throw new CmsException("Space "+ spaceId + " not found");
			}
			else{

				List<String> references = new ArrayList<String>();

				//If spaceNode node contains contentObjectReferences
				if (spaceNode.hasProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName())){
					Property contentObjectIdsProperty = spaceNode.getProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName());

					Value[] referencesValues = contentObjectIdsProperty.getValues();
					int refSize = referencesValues.length;

					for (int i=0; i<refSize ; i++){
						references.add(referencesValues[i].getString());
					}
				}

				return references;
			}
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public int getCountOfContentObjectIdsWhichResideInSpace(String spaceId) {
		try {

			Node spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(getSession(), spaceId);


			if (spaceNode == null)
				throw new CmsException("Space "+ spaceId + " not found");
			else{

				//If spaceNode node contains contentObjectReferences
				if (spaceNode.hasProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName())){
					Property contentObjectIdsProperty = spaceNode.getProperty(CmsBuiltInItem.ContentObjectReferences.getJcrName());

					return contentObjectIdsProperty.getValues().length;
				}

				return 0;
			}
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> spaceOutput, FetchLevel fetchLevel) {
		
		ByteArrayOutputStream os = null;
		
		try {

			Session session = getSession();
			
			Node spaceNode = getSpaceNodeByIdOrName(spaceIdOrName);

			if (spaceNode == null){

				if (spaceOutput != null && ResourceRepresentationType.SPACE_LIST.equals(spaceOutput)){
					return (T) new CmsOutcomeImpl<Space>(0, 0, 0);
				}

				return null;
			}
			
			if (spaceOutput == null || ResourceRepresentationType.SPACE_INSTANCE.equals(spaceOutput)|| 
					ResourceRepresentationType.SPACE_LIST.equals(spaceOutput)){
				
				RenderProperties renderProperties = new RenderPropertiesImpl();
				
				Space space = spaceRenderer.renderSpaceAndItsParent(spaceNode, renderProperties, session, 
						cmsRepositoryEntityFactoryForActiveClient.newSpace(), new HashMap<String, CmsRepositoryEntity>());
				
				//Pre fetch children or tree
				if (fetchLevel != null){
					
					switch (fetchLevel) {
					case ENTITY_AND_CHILDREN:
						//This way lazy loading will be enabled
						space.getChildren();
						break;
					case FULL:
						loadAllChildren(space.getChildren());
						break;
					default:
						break;
					}
					
				}
				
				//Return appropriate type
				if (spaceOutput == null || ResourceRepresentationType.SPACE_INSTANCE.equals(spaceOutput )){
					return (T) space;
				}
				else{
					//Return type is CmsOutcome.
					CmsOutcome<Space> outcome = new CmsOutcomeImpl<Space>(1, 0, 1);
					outcome.getResults().add(space);
					
					return (T) outcome;
				}
				
			}
			else if (ResourceRepresentationType.XML.equals(spaceOutput)|| 
					ResourceRepresentationType.JSON.equals(spaceOutput)){

				String space = null;
				
				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.space()
						.prettyPrint(false)
						.representationType(spaceOutput)
						.build();
						
				serializationDao.serializeCmsRepositoryEntity(spaceNode, os, CmsEntityType.SPACE, null, fetchLevel, true, serializationConfiguration);

				space = new String(os.toByteArray(), "UTF-8");

				return (T) space;
			}
			else{
				throw new CmsException("Invalid resource representation type for space  "+spaceOutput);
			}


		}catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch(CmsException ex){
			throw ex;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}
	}

	private void loadAllChildren(List<Space> children) {
		
		if (children != null && ! children.isEmpty()){
			for (Space child : children){
				loadAllChildren(child.getChildren());
			}
		}
		
	}
	private <T> T returnEmptyResultSet(
			ResourceRepresentationType<T> spaceOutput) {
		if (spaceOutput.equals(ResourceRepresentationType.SPACE_LIST)){
			return (T) new CmsOutcomeImpl<T>(0, 0, 0);
		}
		else{
			return null;
		}
	}
	
	private Space returnSingleSpaceFromOutcome(SpaceCriteria spaceCriteria,
			CmsOutcome<Space> outcome) {
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
				throw new CmsException(outcome.getCount() +" spaces matched criteria, user has specified limit "+
						spaceCriteria.getLimit() + " but she also requested return type to be a single Space.");
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


	public <T> T searchSpaces(SpaceCriteria spaceCriteria, ResourceRepresentationType<T> spaceOutput){

		T queryResult = null;
		boolean queryReturnedAtLeastOneResult = false;

		ByteArrayOutputStream os = null;

		try {

			//Check if criteria is provided
			if (spaceCriteria == null){
				return returnEmptyResultSet(spaceOutput);
			}

			//Initialize null parameters (if any) 
			if (spaceOutput == null){
				spaceOutput =  (ResourceRepresentationType<T>) ResourceRepresentationType.SPACE_LIST;
			}

			//Check cache
			if (spaceCriteria.isCacheable()){

				queryResult = (T)jcrQueryCacheRegion.getJcrQueryResults(spaceCriteria, spaceOutput.getTypeAsString());

				if (queryResult != null){
					return queryResult;
				}
			}

			//User requested Objects as return type
			if (ResourceRepresentationType.SPACE_INSTANCE.equals(spaceOutput)|| 
					ResourceRepresentationType.SPACE_LIST.equals(spaceOutput)){

				CmsOutcome<Space> outcome = createSpaceOutcome(getSession(), spaceCriteria);

				//User requested one Space. Throw an exception if more than
				//one returned
				if (ResourceRepresentationType.SPACE_INSTANCE.equals(spaceOutput) ){
					queryResult =  (T) returnSingleSpaceFromOutcome(spaceCriteria, outcome);
					queryReturnedAtLeastOneResult = queryResult != null;
				}
				else{

					//Return type is CmsOutcome.
					queryResult =  (T) outcome;
					queryReturnedAtLeastOneResult = outcome.getCount() > 0;
				}

			}
			else if (ResourceRepresentationType.XML.equals(spaceOutput)|| 
					ResourceRepresentationType.JSON.equals(spaceOutput)){


				//User requested output to be XML or JSON
				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.space()
						.prettyPrint(spaceCriteria.getRenderProperties().isPrettyPrintEnabled())
						.representationType(spaceOutput)
						.build();

				long numberOfResutls  = serializationDao.serializeSearchResults(getSession(), spaceCriteria, os, FetchLevel.ENTITY, serializationConfiguration);

				queryReturnedAtLeastOneResult = numberOfResutls > 0;

				queryResult = (T) new String(os.toByteArray(), "UTF-8");

			}


			if (spaceCriteria.isCacheable()){
				String xpathQuery = spaceCriteria.getXPathQuery();
				if (!StringUtils.isBlank(xpathQuery) && queryReturnedAtLeastOneResult){
					jcrQueryCacheRegion.cacheJcrQueryResults(spaceCriteria, 
							queryResult, spaceCriteria.getRenderProperties(), spaceOutput.getTypeAsString());
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
	
	public Node getSpaceNodeByIdOrName(String spaceIdOrName){
		try{
			Node spaceNode = null;
			
			if (StringUtils.isBlank(spaceIdOrName)){
				return null;
			}
			else if (CmsConstants.UUIDPattern.matcher(spaceIdOrName).matches()){
				spaceNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(getSession(), spaceIdOrName);

				if (spaceNode != null){
					return spaceNode;
				}
			}
			else{
				SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
				spaceCriteria.addNameEqualsCriterion(spaceIdOrName);
				spaceCriteria.setOffsetAndLimit(0, 1);
				
				CmsQueryResult nodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), spaceCriteria);
				
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

}
