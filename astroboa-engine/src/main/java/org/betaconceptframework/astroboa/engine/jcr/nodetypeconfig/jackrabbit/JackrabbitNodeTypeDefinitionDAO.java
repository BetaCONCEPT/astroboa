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

package org.betaconceptframework.astroboa.engine.jcr.nodetypeconfig.jackrabbit;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.nodetypeconfig.CmsNodeTypeDefinitionDao;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsLocalizationUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JackrabbitDependentUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrValueUtils;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
class JackrabbitNodeTypeDefinitionDao extends JcrDaoSupport implements CmsNodeTypeDefinitionDao{

	private final Logger  logger = LoggerFactory.getLogger(JackrabbitNodeTypeDefinitionDao.class);

	private Resource repositoryNodeTypeDefinitionFile;
	
	/**
	 * This file contains new node type definitions needed
	 * for Astroboa v3.x.x . It is used when initialising 
	 * repositories which were created by Astroboa v2.x.x
	 */
	private Resource newNodeTypeDefinitionFile;
	
	/**
	 * This file contains node type definitions which must
	 * be updated for Astroboa v3.x.x . It is used when initialising 
	 * repositories which were created by Astroboa v2.x.x
	 */
	private Resource updatedNodeTypeDefinitionFile;

	private CmsLocalizationUtils cmsLocalizationUtils;
	
	private RepositoryUserDao repositoryUserDao;
	
	/**
	 * Provides localized labels for Astroboa System taxonomy
	 */
	private Localization subjectTaxonomyLocalization;
	
	/**
	 * Provides localized labels for Organization Space
	 */
	private Localization organizationSpaceLocalization;

	public void setOrganizationSpaceLocalization(
			Localization organizationSpaceLocalization) {
		this.organizationSpaceLocalization = organizationSpaceLocalization;
	}


	public void setRepositoryUserDao(RepositoryUserDao repositoryUserDao) {
		this.repositoryUserDao = repositoryUserDao;
	}

	public void setSubjectTaxonomyLocalization(Localization subjectTaxonomyLocalization) {
		this.subjectTaxonomyLocalization = subjectTaxonomyLocalization;
	}
	

	public void setCmsLocalizationUtils(CmsLocalizationUtils cmsLocalizationUtils) {
		this.cmsLocalizationUtils = cmsLocalizationUtils;
	}

	public void setRepositoryNodeTypeDefinitionFile(
			Resource repositoryNodeTypeDefinitionFile) {
		this.repositoryNodeTypeDefinitionFile = repositoryNodeTypeDefinitionFile;
	}

	
	public void setNewNodeTypeDefinitionFile(Resource newNodeTypeDefinitionFile) {
		this.newNodeTypeDefinitionFile = newNodeTypeDefinitionFile;
	}


	public void setUpdatedNodeTypeDefinitionFile(
			Resource updatedNodeTypeDefinitionFile) {
		this.updatedNodeTypeDefinitionFile = updatedNodeTypeDefinitionFile;
	}


	public void saveOrUpdateTypeAndNodeHierarchy(String repositoryId){


		try{
			
			//Create or update node type hierarchy 
			Session session = getSession();

			JackrabbitDependentUtils.setupCacheManager(session.getRepository(), repositoryId);

			Workspace workspace = session.getWorkspace();
			NamespaceRegistry namespaceRegistry = workspace.getNamespaceRegistry();
			
			//Check if node Types have already been registered
			if ( ! isAstroboaUriRegistered(namespaceRegistry)){
				registerRepositoryCND(session, workspace, false);

				createSystemNode(session);

				updateLocalizedLabelsForSubjectTaxonomy(session);	

				String systemUserId = getRepositoryUserForSystemUser().getId();
				
				createOrganizationSpace(session, systemUserId);
			
			}
			else{
				//Ensure backwards compatibility. In repositories prior to v 3.x.x
				//Upload some new types
				
				try{
					workspace.getNodeTypeManager().getNodeType(CmsBuiltInItem.GenericHourFolder.getJcrName());
				}
				catch(NoSuchNodeTypeException nsnte){
					registerAdditionalNodeTypes(session, workspace, newNodeTypeDefinitionFile, false);
					registerAdditionalNodeTypes(session, workspace, updatedNodeTypeDefinitionFile, true);
				}
				
			}
		}
		catch (Exception e)
		{
			throw new CmsException(e);
		}

	}

	


	private void createOrganizationSpace(Session session, String systemUserId) {
		try{
		//Create organization space
		Node systemNode = JcrNodeUtils.getCMSSystemNode(session);
		if (!systemNode.hasNode(CmsBuiltInItem.OrganizationSpace.getJcrName())){
			
			Node organizationSpaceNode = JcrNodeUtils.addSpaceNode(systemNode, CmsBuiltInItem.OrganizationSpace.getJcrName());
			
			//Create new CmsIdentifier
			organizationSpaceNode.setProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), organizationSpaceNode.getUUID());
			
			//Populate Node with default values
			organizationSpaceNode.setProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName(), systemUserId);
			organizationSpaceNode.setProperty(CmsBuiltInItem.Order.getJcrName(), Long.valueOf(1));
			organizationSpaceNode.setProperty(CmsBuiltInItem.Name.getJcrName(), "OrganizationSpace" );
			
			//Update localization
			cmsLocalizationUtils.updateCmsLocalization(organizationSpaceLocalization, organizationSpaceNode);
		
			session.save();
			
		}
		else
		{
			Node organizationSpaceNode = systemNode.getNode(CmsBuiltInItem.OrganizationSpace.getJcrName());
			
			if (organizationSpaceNode.hasProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()))
			{
				organizationSpaceNode.setProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName(), JcrValueUtils.getJcrNull());
				
				session.save();
			}
		}
	}
	catch (Exception e){
		logger.error("Problem with creating organization space", e);
	}
		
	}
	
	private RepositoryUser getRepositoryUserForSystemUser() {
		try{
			RepositoryUser systemUser = repositoryUserDao.getSystemRepositoryUser();
			
			if (systemUser == null)
			{
				systemUser = repositoryUserDao.createSystemRepositoryUser();
			}
				
			return systemUser;

		}
		catch (Exception e){
			logger.error("Problem with creating a repository user for system user", e);
			return null;
		}

	}
	

	private void updateLocalizedLabelsForSubjectTaxonomy(Session session) {
		//Update Localized Labels for Subject Taxonomy
		if (subjectTaxonomyLocalization != null && subjectTaxonomyLocalization.hasLocalizedLabels()){

			try{
				Node subjectTaxonomyNode = JcrNodeUtils.getTaxonomyRootNode(session).getNode(CmsBuiltInItem.SubjectTaxonomy.getJcrName());

				cmsLocalizationUtils.updateCmsLocalization(subjectTaxonomyLocalization, subjectTaxonomyNode);
				
				subjectTaxonomyNode.setProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), subjectTaxonomyNode.getUUID());

				session.save();
			}
			catch (Exception e){
				logger.error("Problem with registering localized labels for subject taxonomy", e);
			}
		}
	}


	private void createSystemNode(Session session)
	throws RepositoryException, ItemExistsException,
	PathNotFoundException, NoSuchNodeTypeException, LockException,
	VersionException, ConstraintViolationException,
	AccessDeniedException, InvalidItemStateException {
		//Create csm repository main node
		if (!session.getRootNode().hasNode(CmsBuiltInItem.SYSTEM.getJcrName()))
		{
			session.getRootNode().addNode(CmsBuiltInItem.SYSTEM.getJcrName(), CmsBuiltInItem.SYSTEM.getJcrName());

			session.save();
		}
	}


	private void registerRepositoryCND(Session session,
			Workspace workspace, boolean reregisterExisting) throws FileNotFoundException, IOException,
			RepositoryException, AccessDeniedException, ItemExistsException,
			ConstraintViolationException, InvalidItemStateException,
			VersionException, LockException, NoSuchNodeTypeException {
		InputStream repositoryNodeTypeDefinitionInputStream = null;

		try{
			repositoryNodeTypeDefinitionInputStream = loadRepositoryNodeTypeDefinitionInputStream();

			//Node type definitions are written using Use JackRabbit Compact Namespace AND Node Type Definition (CND) notation

			logger.debug("Loading CND {}", repositoryNodeTypeDefinitionFile.getFilename());

			JackrabbitDependentUtils.register(workspace, repositoryNodeTypeDefinitionInputStream, reregisterExisting);

			session.save();

			logger.debug("CMS Builtin NodeTypes have been registered");

		}
		catch (Exception e)
		{
			throw new CmsException(e);
		}
		finally{
			if (repositoryNodeTypeDefinitionInputStream != null){
				try {
					repositoryNodeTypeDefinitionInputStream.close();
				} catch (IOException e) {
					logger.error("",e);
				}
			}
		}
	}
	
	private void registerAdditionalNodeTypes(Session session,
			Workspace workspace, Resource nodeTypeDefinitionFile, boolean reregisterExisting) throws FileNotFoundException, IOException,
			RepositoryException, AccessDeniedException, ItemExistsException,
			ConstraintViolationException, InvalidItemStateException,
			VersionException, LockException, NoSuchNodeTypeException {
		
		if (nodeTypeDefinitionFile == null){
			logger.info("Could not locate file for node type definitions");
		}
		else{
			//First load new node type definitions
			InputStream nodeTypeDefinitionInputStream = null;

			try{
				nodeTypeDefinitionInputStream = nodeTypeDefinitionFile.getInputStream();

				logger.debug("Loading CND {}", nodeTypeDefinitionFile.getFilename());

				JackrabbitDependentUtils.register(workspace, nodeTypeDefinitionInputStream, reregisterExisting);

				session.save();

			}
			catch (Exception e)
			{
				throw new CmsException(e);
			}
			finally{
				if (nodeTypeDefinitionInputStream != null){
					try {
						nodeTypeDefinitionInputStream.close();
					} catch (IOException e) {
						logger.error("",e);
					}
				}
			}
		}
	}



	private InputStream loadRepositoryNodeTypeDefinitionInputStream() throws Exception {

		if (repositoryNodeTypeDefinitionFile == null)
			throw new FileNotFoundException("repository.cnd in META-INF directory.");

		return repositoryNodeTypeDefinitionFile.getInputStream();
	}


	private boolean isAstroboaUriRegistered(NamespaceRegistry namespaceRegistry) throws RepositoryException {
		try {
			namespaceRegistry.getURI(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX);
		} catch (NamespaceException e) {
			return false;
		}

		return true;
	}
}
