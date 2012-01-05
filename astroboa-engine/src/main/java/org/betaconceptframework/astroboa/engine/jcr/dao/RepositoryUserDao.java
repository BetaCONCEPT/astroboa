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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.RepositoryUserType;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.renderer.RepositoryUserRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsLocalizationUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.EntityAssociationDeleteHelper;
import org.betaconceptframework.astroboa.engine.jcr.util.EntityAssociationUpdateHelper;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.RendererUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.SpaceUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class RepositoryUserDao extends JcrDaoSupport {

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;


	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;

	@Autowired
	private RendererUtils rendererUtils;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Autowired
	private RepositoryUserRenderer repositoryUserRenderer;

	@Autowired
	private CmsLocalizationUtils cmsLocalizationUtils;

	@Autowired
	private ContentObjectDao contentObjectDao;

	@Autowired
	private SpaceUtils spaceUtils;

	/**
	 * Provides localized labels for folksonomies
	 */
	private Localization defaultFolksonomyLocalization;

	/**
	 * Provides localized labels for Spaces
	 */
	private Localization defaultSpaceLocalization;

	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;
	
	@Autowired
	private ImportDao importDao;

	public void setDefaultSpaceLocalization(Localization defaultSpaceLocalization) {
		this.defaultSpaceLocalization = defaultSpaceLocalization;
	}

	public void setDefaultFolksonomyLocalization(
			Localization defaultFolksonomyLocalization) {
		this.defaultFolksonomyLocalization = defaultFolksonomyLocalization;
	}

	public RepositoryUser getSystemRepositoryUser() {
		return getRepositoryUser(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID);
	}


	public void removeRepositoryUserAndOwnedObjects(String repositoryUserId)   {

		removeRepositoryUser(repositoryUserId, null, true);
	}

	private void removeRepositoryUser(String repositoryUserId, RepositoryUser alternativeUser, boolean removeObjectsWhoseOwnerIsTheUserToBeDeleted)   {

		Context context = null;
		
		try{
			Session session = getSession();

			if (StringUtils.isBlank(repositoryUserId))
				throw new ItemNotFoundException("Undefined repository user id "+ repositoryUserId);

			//			Retrieve user node to be removed
			Node repositoryUserNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, repositoryUserId);

			if (repositoryUserNode == null)
				throw new CmsException("No repository User found with Id "+ repositoryUserId);

			if (repositoryUserNode.hasProperty(CmsBuiltInItem.ExternalId.getJcrName())){
				String externalId = repositoryUserNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString();

				if (CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID.equals(externalId)){
					throw new CmsException("Invalid delete operation. SYSTEM RepositoryUser cannot be deleted.");
				}
			}

			if (alternativeUser == null && ! removeObjectsWhoseOwnerIsTheUserToBeDeleted){
				throw new CmsException("No alternative user has been provided");
			}

			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
			
			//Save any alternative user
			if (alternativeUser != null){
				updateRepositoryUserNode(session, alternativeUser, context);
			}

			//remove all node References
			removeReferencesForRepositoryUser(session,alternativeUser, removeObjectsWhoseOwnerIsTheUserToBeDeleted, repositoryUserId, context);

			//remove user node
			repositoryUserNode.remove();

			session.save();

			//Notify cache
			//jcrQueryCacheRegion.removeRegion();

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

	public void removeRepositoryUser(String repositoryUserId, RepositoryUser alternativeUser)  {

		removeRepositoryUser(repositoryUserId, alternativeUser, (alternativeUser == null));


	}

	public RepositoryUser saveRepositoryUser(Object repositoryUserSource)  {

		if (repositoryUserSource == null){
			throw new CmsException("Cannot save an empty RepositoryUser !");
		}
		
		if (repositoryUserSource instanceof String){
			//Use importer to unmarshal String to RepositoryUser
			//and to save it as well.
			//What is happened is that importDao will create a RepositoryUser
			//and will pass it here again to save it. 
			return importDao.importRepositoryUser((String)repositoryUserSource, ImportMode.SAVE_ENTITY);
		}
		
		if (! (repositoryUserSource instanceof RepositoryUser)){
			throw new CmsException("Expecting either String or RepositoryUser and not "+repositoryUserSource.getClass().getName());
		}
		
		RepositoryUser repositoryUser = (RepositoryUser) repositoryUserSource;

		Context context = null;
		try {
			Session session = getSession();
			
			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);

			updateRepositoryUserNode(session,repositoryUser, context);

			session.save();

			//Inform cache
			//jcrQueryCacheRegion.removeRegion();

			return repositoryUser;
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

	public List<RepositoryUser> searchRepositoryUsers(RepositoryUserCriteria repositoryUserCriteria)  {
		Session session = null;

		if (repositoryUserCriteria== null)
			throw new CmsException("Null repository user criteria");

		try {

			//For some reason user has asked for 0 limit
			//Returned an empty list. No need to perform the
			//query. this method output must be changed to CmsOutcome
			//in order to provide user with the total result count
			//even when limit is 0 , that is no result should be returned
			if (repositoryUserCriteria.getLimit() == 0){
				return new ArrayList<RepositoryUser>();
			}

			List<RepositoryUser> repositoryUserList = null;

			if (repositoryUserCriteria.isCacheable()){
				//Search in cache

				repositoryUserList  = (List<RepositoryUser>) jcrQueryCacheRegion.getJcrQueryResults(repositoryUserCriteria);
			}

			if (repositoryUserList != null)
				return repositoryUserList;
			else{

				session = getSession();

				repositoryUserList = new ArrayList<RepositoryUser>();

				//Need new instance in case during rendering render instructions are updated
				RenderProperties renderPropertiesForCache = rendererUtils.copyRenderPropertiesFromCriteria(repositoryUserCriteria);

				//CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = new CmsQueryResultSecurityHandler(cmsQueryHandler, 
				//	repositoryUserCriteria,session, accessManager);

				CmsQueryResult cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, repositoryUserCriteria);



				NodeIterator nodeIterator = cmsQueryResult.getNodeIterator();

				Map<String, CmsRepositoryEntity> cachedRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();

				List<String> repositoryUserIdsForCache = new ArrayList<String>();

				while (nodeIterator.hasNext()){
					RepositoryUser repositoryUser = renderRepositoryUserFromNode(nodeIterator.nextNode(), session,
							repositoryUserCriteria.getRenderProperties(),  cachedRepositoryEntities);
					repositoryUserList.add(repositoryUser);
					repositoryUserIdsForCache.add(repositoryUser.getId());
				}

				//Cache results
				if (repositoryUserCriteria.isCacheable()){
					if (!StringUtils.isBlank(repositoryUserCriteria.getXPathQuery()) && CollectionUtils.isNotEmpty(repositoryUserList)){
						jcrQueryCacheRegion.cacheJcrQueryResults(repositoryUserCriteria, 
								repositoryUserList, renderPropertiesForCache);
					}
				}


				return repositoryUserList;
			}

		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public RepositoryUser renderRepositoryUserFromNode(
			Node node, Session session, RenderProperties renderProperties, 
			Map<String, CmsRepositoryEntity> cachedRepositoryEntities)
			throws RepositoryException {
		
		return repositoryUserRenderer.renderRepositoryUserNode(node,
				renderProperties, session, cachedRepositoryEntities);
	}

	private void updateRepositoryUserNode(Session session, RepositoryUser repositoryUser, Context context) throws RepositoryException{
		Node repositoryUserNode = retrieveOrCreateRepositoryUserNode(session, repositoryUser); 

		populateRepositoryUserNode(repositoryUserNode, repositoryUser, session, context);

	}

	private Node retrieveOrCreateRepositoryUserNode(Session session, RepositoryUser repositoryUser) throws RepositoryException{
		//1. ExternalId is not provided. Search user using its identifier
		if (StringUtils.isBlank(repositoryUser.getExternalId())){
			if (repositoryUser.getId() == null){
				throw new CmsException("Repository user has not external id and no cms identifier");
			}
			else{
				//Search for a repository user node with provided identifier
				Node repositoryUserNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, repositoryUser.getId());

				if (repositoryUserNode != null)
				{
					return repositoryUserNode;
				}
				else
				{
					throw new CmsException("Could not update repository user "+repositoryUser.toString()+ " because no repository user with such id is found in repository and externalId is not provided");
				}

			}
		}
		else{
			//2.External Id is provided. 
			//a. Check external Id belongs to SYSTEM user
			if (CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID.equals(repositoryUser.getExternalId())){
				//SYSTEM user is updated. Retrieve system repository user node
				//In case repositoryUser instance has an identifier ignore it
				return  retrieveSystemRepositoryUserNode(session, true);
			}
			else{
				//Check how many users exist with the same external id
				Node existingRepositoryUserNode = checkForDuplicateExternalId(repositoryUser, session);

				if (existingRepositoryUserNode != null){
					return existingRepositoryUserNode;
				}
				else{
					//No node found with provided identifier and/or external Id. Create new node
					return createRepositoryUserNode(repositoryUser, session);
				}
			}
		}

	}

	public RepositoryUser createSystemRepositoryUser(){

		Context context = null;
		try {
			Session session = getSession();

			if (retrieveSystemRepositoryUserNode(session, false) != null){
				throw new Exception("RepositoryUser SYSTEM already exists");
			}

			RepositoryUser systemUser = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser();
			systemUser.setExternalId(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID); 
			systemUser.setLabel(CmsApiConstants.SYSTEM_REPOSITORY_USER_LABEL);

			Node systemUserNode = createRepositoryUserNode(systemUser, session);

			context  = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
			
			populateRepositoryUserNode(systemUserNode, systemUser, session, context);

			session.save();

			return systemUser;
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

	private Node retrieveSystemRepositoryUserNode(Session session, boolean throwExceptionIfNotFound) throws RepositoryException {

		RepositoryUserCriteria userCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		userCriteria.doNotCacheResults();
		userCriteria.setLimit(1);
		userCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		userCriteria.addExternalIdEqualsCriterion(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID);

		CmsQueryResult systemUserQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, userCriteria);

		if (systemUserQueryResult == null || systemUserQueryResult.getTotalRowCount() == 0)
		{
			if (throwExceptionIfNotFound)
			{
				throw new RepositoryException("Problem with locating system repository user node. Query "+ userCriteria.getXPathQuery() + " did not returned 1 node");
			}
			else 
			{
				return null;
			}
		}
		else if (systemUserQueryResult.getTotalRowCount() == 1)
		{
			return systemUserQueryResult.getNodeIterator().nextNode();
		}
		else
		{
			throw new RepositoryException("Problem with locating system repository user node. Found more than one jcr nodes representing system repository user");
		}

	}

	private  Node createRepositoryUserNode(RepositoryUser repositoryUser, Session session) throws RepositoryException {

		Node repositoryUserNode = JcrNodeUtils.addRepositoryUserNode(session);

		//Create CmsIdentifier
		cmsRepositoryEntityUtils.createCmsIdentifier(repositoryUserNode, repositoryUser, true);

		return repositoryUserNode;
	}

	private  void populateRepositoryUserNode(Node repositoryUserNode, RepositoryUser repositoryUser, Session session, Context context) throws RepositoryException {

		String repositoryUserExternalId = repositoryUser.getExternalId();

		//External Id. Update it only if it does not exists
		if (! repositoryUserNode.hasProperty(CmsBuiltInItem.ExternalId.getJcrName()))
		{
			repositoryUserNode.setProperty(CmsBuiltInItem.ExternalId.getJcrName(), repositoryUserExternalId);
		}
		//or it is not SYSTEM RepositoyUser ExternalId and it is not the same with the one existed
		else{
			String existingExternalId = repositoryUserNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString();

			boolean repositoryUserExternalIdIsDifferentThanExisting = ! existingExternalId.equals(repositoryUserExternalId);

			if (repositoryUserExternalIdIsDifferentThanExisting)
			{
				if (CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID.equals(existingExternalId))
				{
					throw new RepositoryException("Invalid operation. Tried to update SYSTEM repository user externalId.");
				}
				else
				{
					repositoryUserNode.setProperty(CmsBuiltInItem.ExternalId.getJcrName(), repositoryUserExternalId);
				}
			}
		}

		cmsRepositoryEntityUtils.setSystemProperties(repositoryUserNode, repositoryUser);

		//Label
		repositoryUserNode.setProperty(CmsBuiltInItem.Label.getJcrName(), repositoryUser.getLabel());

		//User Type
		if (repositoryUser.getUserType() == null){
			repositoryUser.setUserType(RepositoryUserType.User);
		}

		repositoryUserNode.setProperty(CmsBuiltInItem.UserType.getJcrName(), repositoryUser.getUserType().toString());

		populateSpace(repositoryUser, repositoryUserNode, session, context);

		populateFolksonomy(repositoryUser, repositoryUserNode, session);

		// TODO Preferences Implement method
		populatePreferences(repositoryUser, repositoryUserNode);
	}

	private Node checkForDuplicateExternalId(RepositoryUser repositoryUserToBeSaved,
			Session session) throws RepositoryException, ValueFormatException,
			PathNotFoundException 
			{
		RepositoryUserCriteria userCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		userCriteria.doNotCacheResults();
		userCriteria.setLimit(2);
		userCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		userCriteria.addExternalIdEqualsCriterion(repositoryUserToBeSaved.getExternalId());

		CmsQueryResult usersWithTheSameExternalId = cmsQueryHandler.getNodesFromXPathQuery(session, userCriteria);

		if (usersWithTheSameExternalId != null)
		{
			if (usersWithTheSameExternalId.getTotalRowCount() > 1)
			{
				throw new RepositoryException("Found more than one repository users with the same external id "+ repositoryUserToBeSaved.getExternalId());
			}

			if (usersWithTheSameExternalId.getTotalRowCount() == 1)
			{
				if (repositoryUserToBeSaved.getId() == null)
				{
					throw new RepositoryException("A repository user with the same external id "+ repositoryUserToBeSaved.getExternalId() + " already exists.");
				}
				else{
					Node existingRepositoryUserNode = usersWithTheSameExternalId.getNodeIterator().nextNode();

					//Must be the same node
					if (! existingRepositoryUserNode.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()) ||
							! existingRepositoryUserNode.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString().equals(repositoryUserToBeSaved.getId()))
					{
						throw new RepositoryException("A repository user with the same external id "+ repositoryUserToBeSaved.getExternalId() + " already exists.");
					}

					return existingRepositoryUserNode;
				}
			}
		}

		//No other node found with the same external id. in case identifier is provided

		return null;

			}

	private void populateFolksonomy(RepositoryUser repositoryUser, Node repositoryUserNode, Session session) throws RepositoryException {

		Taxonomy repositoryUserFolksonomy = repositoryUser.getFolksonomy();

		if (repositoryUserFolksonomy == null){
			((RepositoryUserImpl)repositoryUser).setFolksonomy(cmsRepositoryEntityFactoryForActiveClient.newTaxonomy());
			repositoryUserFolksonomy = repositoryUser.getFolksonomy();
		}

		//Folksonomy node should have already been created. No need to check for it existence
		Node folksonomyNode = repositoryUserNode.getNode(CmsBuiltInItem.Folksonomy.getJcrName());

		//If repository user folksonomy has no localized labels use the default
		if (! repositoryUserFolksonomy.hasLocalizedLabels() && MapUtils.isNotEmpty(defaultFolksonomyLocalization.getLocalizedLabels())){
			repositoryUserFolksonomy.getLocalizedLabels().putAll(defaultFolksonomyLocalization.getLocalizedLabels());
		}

		//Update localization for repository user folksonomy
		cmsLocalizationUtils.updateCmsLocalization(repositoryUserFolksonomy, folksonomyNode);

		// In case repository user is a new user and its folksonomy does not have a name
		String providedFolksonomyName = repositoryUserFolksonomy.getName();
		if (StringUtils.isBlank(providedFolksonomyName)){
			repositoryUserFolksonomy.setName(folksonomyNode.getName());
		}
		else{
			//Folksonomy Name is a fixed name and should never change
			if (!folksonomyNode.getName().equals(providedFolksonomyName)){
				throw new RepositoryException("Invalid folksonomy name "+ providedFolksonomyName);
			}
		}

		//Create Folksonomy Identifier if folksonomy is a new one
		if (folksonomyNode.isNew()){
			if (cmsRepositoryEntityUtils.hasCmsIdentifier(folksonomyNode)){
				//This should never happen
				throw new CmsException("Folskonomy jcr node is new but an idnetifier property already exists");
			}

			//Create a Astroboa identifier for folskonomy or use the one provided
			cmsRepositoryEntityUtils.createCmsIdentifier(folksonomyNode, repositoryUserFolksonomy, true);
		}
		else{
			//Folksonomy node is not new. Check that an identifier exists
			if (!cmsRepositoryEntityUtils.hasCmsIdentifier(folksonomyNode))
			{
				cmsRepositoryEntityUtils.createCmsIdentifier(folksonomyNode, repositoryUserFolksonomy, true);
			}
			else
			{
				String folksonomyIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(folksonomyNode);

				//User has provided an identifier for folksonomy.
				//Check if it matches with the one in folksonomy jcr node
				if (repositoryUserFolksonomy.getId() != null){
					if (!repositoryUserFolksonomy.getId().equals(folksonomyIdentifier) 
							&& ! StringUtils.equals(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID, repositoryUser.getExternalId()))
					{
						throw new CmsException("Folksonomy identifier "+folksonomyIdentifier + " for repository user "+ repositoryUser.getLabel()+ 
								" does not match with the provided identifier "+repositoryUserFolksonomy.getId());
					}
				}
				else{
					//User does not have an identifier for folksonomy. Update its value
					repositoryUserFolksonomy.setId(folksonomyIdentifier);
				}
			}
		}

	}

	private void populatePreferences(RepositoryUser repositoryUser, Node repositoryUserNode) {


	}

	private void populateSpace(RepositoryUser repositoryUser, Node repositoryUserNode, Session session, Context context) throws RepositoryException {

		Space repositoryUserSpace = repositoryUser.getSpace();
		if (repositoryUserSpace == null){
			((RepositoryUserImpl)repositoryUser).setSpace(cmsRepositoryEntityFactoryForActiveClient.newSpace());
			repositoryUserSpace = repositoryUser.getSpace();
		}

		if (! repositoryUser.getSpace().hasLocalizedLabels() && MapUtils.isNotEmpty(defaultSpaceLocalization.getLocalizedLabels())){
			repositoryUser.getSpace().getLocalizedLabels().putAll(defaultSpaceLocalization.getLocalizedLabels());
		}

		if (repositoryUserSpace.getName() == null){
			repositoryUserSpace.setName("spaceForUser"+ repositoryUser.getExternalId());
		}

		//Since Space name must follow a specific pattern, check if repository user space name is valid.
		//if not then issue a warning and replace value with a default value.
		if (!cmsRepositoryEntityUtils.isValidSystemName(repositoryUserSpace.getName())){
			//First try to remove all white spaces, in case space name has not changed since it was first created by the 
			//following line of code
			//repositoryUserSpace.setName("Space for user"+ repositoryUser.getExternalId());
			String nameWithoutWhiteSpaces = StringUtils.deleteWhitespace(repositoryUserSpace.getName());

			if (!cmsRepositoryEntityUtils.isValidSystemName(nameWithoutWhiteSpaces)){
				//Name is still invalid. Issue a warning and replace name so that save can continue safely
				logger.warn("Repository User's '{}' Space name '{}' does not follow pattern {} and to will change to 'spaceForUser'. " +
						" In order to provide a different name you must explicitly save RepositotyUser Space by using SpaceService.", 
						new Object[]{repositoryUser.getLabel(), repositoryUserSpace.getName(),
						CmsConstants.SYSTEM_NAME_REG_EXP});
			}
			else{
				repositoryUserSpace.setName(nameWithoutWhiteSpaces);
			}
		}


		//Create Space if it does not exist
		Node spaceNode = null;

		if (!repositoryUserNode.hasNode(CmsBuiltInItem.Space.getJcrName())){

			repositoryUserSpace.setOwner(repositoryUser);
			repositoryUserSpace.setOrder(Long.valueOf(1));

			//Create new node
			spaceNode = JcrNodeUtils.addSpaceNode(repositoryUserNode, CmsBuiltInItem.Space.getJcrName());

			//Create a Astroboa identifier for space or use the one provided
			cmsRepositoryEntityUtils.createCmsIdentifier(spaceNode, repositoryUserSpace, true);

			//Set Owner
			//Inform associations about relation between new space and repository user
			EntityAssociationUpdateHelper<RepositoryUser> repositoryUserAssociationUpdateHelper = 
				new EntityAssociationUpdateHelper<RepositoryUser>(session,cmsRepositoryEntityAssociationDao, context);

			repositoryUserAssociationUpdateHelper.setReferrerCmsRepositoryEntityNode(spaceNode);
			repositoryUserAssociationUpdateHelper.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier);
			repositoryUserAssociationUpdateHelper.setValuesToBeAdded(Arrays.asList(repositoryUser));
			repositoryUserAssociationUpdateHelper.update();
		}
		else {
			spaceNode = repositoryUserNode.getNode(CmsBuiltInItem.Space.getJcrName());

			if (! repositoryUser.getSpace().hasLocalizedLabels() && MapUtils.isNotEmpty(defaultSpaceLocalization.getLocalizedLabels())){
				repositoryUser.getSpace().getLocalizedLabels().putAll(defaultSpaceLocalization.getLocalizedLabels());
			}

			//Space node is not new. Check that an identifier exists
			if (!cmsRepositoryEntityUtils.hasCmsIdentifier(spaceNode))
			{
				cmsRepositoryEntityUtils.createCmsIdentifier(spaceNode, repositoryUserSpace, true);
			}
			else
			{

				String spaceIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(spaceNode);

				//User has provided an identifier for space
				//Check if it matches with the one in space jcr node only if space does not represent SYSTEM's space
				if (repositoryUserSpace.getId() != null){
					if (!repositoryUserSpace.getId().equals(spaceIdentifier) && ! StringUtils.equals(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID, repositoryUser.getExternalId()))
					{
						throw new CmsException("Space identifier "+spaceIdentifier + " for repository user "+ repositoryUser.getLabel()+ 
								" does not match with the provided identifier "+repositoryUserSpace.getId());
					}
				}
				else
				{
					//User does not have an identifier for space. Update its value
					repositoryUserSpace.setId(spaceIdentifier);
				}
			}
		}


		//Update localization for repository user space
		spaceUtils.updateLocalizedLabels(repositoryUser.getSpace(), spaceNode);

		//Populate Node with default values
		spaceUtils.updateOrder(repositoryUser.getSpace(), spaceNode);
		spaceUtils.updateName(repositoryUser.getSpace(), spaceNode);



	}

	private void removeReferencesForRepositoryUser(Session session, RepositoryUser alternativeUser, boolean removeObjectsWhoseOwnerIsTheUserToBeDeleted, String repositoryUserId, Context context) throws Exception {

		if (removeObjectsWhoseOwnerIsTheUserToBeDeleted){
			removeObjectsWhoseOwnerIsTheUserToBeDeleted(session, repositoryUserId, context);
		}
		else{

			RepositoryUser userToBeRemoved = cmsRepositoryEntityFactoryForActiveClient.newRepositoryUser();
			userToBeRemoved.setId(repositoryUserId);

			EntityAssociationDeleteHelper<RepositoryUser> repositoryUserRemover = 
				new EntityAssociationDeleteHelper<RepositoryUser>(session,cmsRepositoryEntityAssociationDao, context);

			if (alternativeUser != null){
				repositoryUserRemover.setCmsRepositoryEntityIdReplacement(alternativeUser.getId());
			}

			repositoryUserRemover.setCmsRepositoryEntityIdToBeRemoved(userToBeRemoved.getId());

			repositoryUserRemover.removeOrReplaceAllReferences(RepositoryUser.class);
		}
	}


	private void removeObjectsWhoseOwnerIsTheUserToBeDeleted(Session session, String repositoryUserId, Context context) throws RepositoryException {
		
		removeContentObjectsOwnedByUser(repositoryUserId, session, context);

		removeSpacesOwnedByUser(repositoryUserId, session, context);	

	}

	private void removeContentObjectsOwnedByUser(String repositoryUserId, Session session, Context context) throws RepositoryException {


		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.addOwnerIdEqualsCriterion(repositoryUserId);
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		NodeIterator contentObjectNodesFound = cmsQueryHandler.getNodesFromXPathQuery(session, contentObjectCriteria).getNodeIterator();

		while (contentObjectNodesFound.hasNext()){
			Node contentObjectNode = contentObjectNodesFound.nextNode();

			contentObjectDao.removeContentObjectNode(contentObjectNode, true, session, context);
		}

	}

	private void removeSpacesOwnedByUser(String repositoryUserId, Session session, Context context) throws RepositoryException {
		Map<String, Node> spacesToBeDeleted = new HashMap<String, Node>();

		SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
		spaceCriteria.addOwnerIdEqualsCriterion(repositoryUserId);
		spaceCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		NodeIterator spacesFound = cmsQueryHandler.getNodesFromXPathQuery(session, spaceCriteria).getNodeIterator();

		while (spacesFound.hasNext()){
			Node spaceNode = spacesFound.nextNode();

			//Organization Space is not to be deleted
			if (Space.ORGANIZATION_SPACE_NAME.equalsIgnoreCase(spaceNode.getName())){
				throw new CmsException(Space.ORGANIZATION_SPACE_NAME +" cannot be deleted");
			}

			Node parent = spaceNode.getParent();
			
			if (!parent.isNodeType(CmsBuiltInItem.RepositoryUser.getJcrName()) && !spacesToBeDeleted.containsKey(spaceNode.getIdentifier())){
				boolean found = false;
				//Iterate through space tree to see if its parent is marked for deletion
				while (parent != null && parent.isNodeType(CmsBuiltInItem.Space.getJcrName())){
					if (!spacesToBeDeleted.containsKey(parent.getIdentifier())){
						parent = parent.getParent();
					}
					else{
						found = true;
						break;
					}
				}

				if (!found){
					spacesToBeDeleted.put(spaceNode.getIdentifier(), spaceNode);
				}
			}
		}


		if (MapUtils.isNotEmpty(spacesToBeDeleted)){
			for (Node node : spacesToBeDeleted.values()){
				spaceUtils.removeSpaceJcrNode(node, session, true, context); 
			}
		}
	}


	public RepositoryUser getRepositoryUser(String externalId) {

		if (StringUtils.isBlank(externalId)){
			throw new CmsException("No external id provided");
		}

		RepositoryUserCriteria repositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		repositoryUserCriteria.addExternalIdEqualsCriterion(externalId);
		repositoryUserCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		repositoryUserCriteria.setOffsetAndLimit(0, 1);

		List<RepositoryUser> repositoryUsers = searchRepositoryUsers(repositoryUserCriteria);

		if (CollectionUtils.isNotEmpty(repositoryUsers) && repositoryUsers.size() > 1){
			throw new CmsException("Found more than one repository user with external id "+ externalId);
		}

		return repositoryUsers.isEmpty() ? null : repositoryUsers.get(0);

	}
}
