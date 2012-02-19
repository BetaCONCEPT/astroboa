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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.model.impl.ContentObjectImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class SpaceUtils {

	final Logger logger = LoggerFactory.getLogger(SpaceUtils.class);

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private CmsLocalizationUtils cmsLocalizationUtils;

	@Autowired 
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;

	public  Node addNewSpaceJcrNode(Node parentSpaceJcrNode, Space space, Session session, boolean useProvidedId, Context context) throws RepositoryException {

		Node spaceJcrNode = JcrNodeUtils.addSpaceNode(parentSpaceJcrNode, CmsBuiltInItem.Space.getJcrName());

		cmsRepositoryEntityUtils.createCmsIdentifier(spaceJcrNode, space, useProvidedId);

		return populateSpaceJcrNode(space, session, spaceJcrNode,useProvidedId, context);
	}

	private Node populateSpaceJcrNode(Space space, Session session, Node spaceJcrNode, boolean useProvidedId, Context context) throws RepositoryException {

		//Update OwnerId
		updateOwner(space.getOwner(), spaceJcrNode, session, context);

		//Update Localized Labels
		updateLocalizedLabels(space, spaceJcrNode);

		//Update order
		updateOrder(space, spaceJcrNode);

		//		Update 
		updateName(space, spaceJcrNode);

		updateContentObjectReferencesForSpace(spaceJcrNode, (Space)space, session, context);

		//		Save or update children
		saveOrUpdateChildren(space, session, spaceJcrNode, context);

		return spaceJcrNode;
	}

	private  void saveOrUpdateChildren(Space space, Session session, Node spaceJcrNode, Context context) throws RepositoryException {

		if (space.isChildrenLoaded()){
			List<Space> children = space.getChildren();

			//Now insert new subTaxonomyNode
			if (CollectionUtils.isNotEmpty(children)){
				for (Space child: children){
					if (child.getId() != null){
						updateSpace(session, child, spaceJcrNode, context);
					}
					else{
						addNewSpaceJcrNode(spaceJcrNode, child, session, false, context);
					}
				}
			}
		}
	}


	public  void updateName(Space space, Node spaceJcrNode) throws     RepositoryException  {
		if (space.getName() != null && 
				! StringUtils.equals(CmsBuiltInItem.OrganizationSpace.getLocalPart(), space.getName()))
		{
			
			if (!cmsRepositoryEntityUtils.isValidSystemName(space.getName())){
				throw new RepositoryException("Space name '"+space.getName()+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP);
			}
			
			spaceJcrNode.setProperty(CmsBuiltInItem.Name.getJcrName(), space.getName());
		}
		else{
			spaceJcrNode.setProperty(CmsBuiltInItem.Name.getJcrName(), JcrValueUtils.getJcrNull());
		}
	}

	public  void updateOrder(Space space, Node spaceJcrNode) throws  RepositoryException  {
		if (space.getOrder() != null)
			spaceJcrNode.setProperty(CmsBuiltInItem.Order.getJcrName(), space.getOrder());
		else
			spaceJcrNode.setProperty(CmsBuiltInItem.Order.getJcrName(), JcrValueUtils.getJcrNull());
	}

	public void updateLocalizedLabels(Localization localization, Node spaceJcrNode) throws RepositoryException {
		cmsLocalizationUtils.updateCmsLocalization(localization, spaceJcrNode);
	}

	public void updateOwner(RepositoryUser spaceOwner, Node spaceJcrNode, Session session, Context context) throws RepositoryException {

		//Owner is expected to exist
		//Therefore no further search in repository is necessary
		//Just update OwnerId value
		if (spaceOwner == null || StringUtils.isBlank(spaceOwner.getId())) 
			throw new CmsException("TaxonomyNode must have an Owner defined"); 

		//Update owner id only if existing owner id is not the same
		String newOwnerId = spaceOwner.getId();

		if (!spaceJcrNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()) ||
				!spaceJcrNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString().equals(newOwnerId)){
			//Check that owner id does correspond to existing user
			if (cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, newOwnerId) ==null){
				throw new CmsException("No repository user found with cms identifier "+ newOwnerId + ". ExternalId : "+ spaceOwner.getExternalId() + ", Label : "+
						spaceOwner.getLabel());
			}

			EntityAssociationUpdateHelper<RepositoryUser> repositoryUserAssociationUpdateHelper = 
				new EntityAssociationUpdateHelper<RepositoryUser>(session,cmsRepositoryEntityAssociationDao, context);
			repositoryUserAssociationUpdateHelper.setReferrerCmsRepositoryEntityNode(spaceJcrNode);
			repositoryUserAssociationUpdateHelper.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier);
			repositoryUserAssociationUpdateHelper.setValuesToBeAdded(Arrays.asList(spaceOwner));
			repositoryUserAssociationUpdateHelper.update();
		}
	}

	public  Node updateSpace(Session session, Space space, Node parentSpaceJcrNode, Context context) throws RepositoryException {

		Node spaceJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, space.getId());

		if (spaceJcrNode == null){
			if (StringUtils.equalsIgnoreCase(CmsBuiltInItem.OrganizationSpace.getLocalPart(), space.getName())){
				spaceJcrNode = JcrNodeUtils.getOrganizationSpaceNode(session); 
			}
			else if (space.getId() != null){
				//User has specified an id for TaxonomyNode. Create a new one
				if(parentSpaceJcrNode == null){
					parentSpaceJcrNode = retrieveParentSpaceNode(session, space);
				}

				return addNewSpaceJcrNode(parentSpaceJcrNode, space, session, true, context);
			}
			else
				throw new CmsException("Found no space with id "+space.getId());
		}
		
		updateOwner(space.getOwner(), spaceJcrNode, session, context);

		updateLocalizedLabels(space, spaceJcrNode);

		updateName(space, spaceJcrNode);

		if (space.getParent() != null){
			updateSpaceParent(space.getParent(), spaceJcrNode, session);
		}

		updateOrder(space, spaceJcrNode);

		updateContentObjectReferencesForSpace(spaceJcrNode, (Space) space, session, context);

		return spaceJcrNode;
	}

	private void updateContentObjectReferencesForSpace(Node spaceNode , Space space, Session session, Context context) throws RepositoryException {

		if (space.isContentObjectReferencesLoaded())
		{
			List<ContentObject> contentObjects = new ArrayList<ContentObject>();
			List<String> contentObjectReferences = space.getContentObjectReferences();
			if (CollectionUtils.isNotEmpty(contentObjectReferences))
			{
				for (String contentObjectReference: contentObjectReferences)
				{
					ContentObject contentObject = new ContentObjectImpl();
					contentObject.setId(contentObjectReference);
					contentObjects.add(contentObject);
				}
			}

			EntityAssociationUpdateHelper<ContentObject> spaceUpdateHelper = 
				new EntityAssociationUpdateHelper<ContentObject>(session,cmsRepositoryEntityAssociationDao, context);
			spaceUpdateHelper.setReferrerCmsRepositoryEntityNode(spaceNode);
			spaceUpdateHelper.setReferrerPropertyName(CmsBuiltInItem.ContentObjectReferences);
			spaceUpdateHelper.setReferrerPropertyNameMultivalue(true);

			spaceUpdateHelper.setValuesToBeAdded(contentObjects);

			spaceUpdateHelper.update();
		}
	}


	public void updateSpaceParent(Space parentSpace, Node spaceJcrNode, Session session) throws  RepositoryException {
		if (parentSpace == null)
			throw new CmsException("Invalid parent taxonomy tree node.");

		Node newParentSpaceJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, parentSpace.getId());

		if (newParentSpaceJcrNode == null)
		{
			if (StringUtils.equalsIgnoreCase(CmsBuiltInItem.OrganizationSpace.getLocalPart(), parentSpace.getName()))
			{
				newParentSpaceJcrNode = JcrNodeUtils.getOrganizationSpaceNode(session); 
			}
			else
			{
				throw new CmsException("Invalid parent space with id "+parentSpace.getId());
			}
		}

		//New Parent's Primary Type can only be Space
		if (!newParentSpaceJcrNode.isNodeType(spaceJcrNode.getPrimaryNodeType().getName()))
		{
			throw new CmsException("Parent space can only be a space");
		}

		//Change Parent only if current parent is not the same
		if (!spaceJcrNode.getParent().getUUID().equals(newParentSpaceJcrNode.getUUID()))
			session.move(spaceJcrNode.getPath(), newParentSpaceJcrNode.getPath()+CmsConstants.FORWARD_SLASH+spaceJcrNode.getName());
	}

	public void removeSpaceJcrNode(Node spaceJcrNode, Session session, boolean removeJcrNode, Context context) throws RepositoryException{
		EntityAssociationDeleteHelper<Space> entityAssociationDeleteHelper = 
			new EntityAssociationDeleteHelper<Space>(session,cmsRepositoryEntityAssociationDao, context);


		removeSpaceFromAssociations(entityAssociationDeleteHelper, spaceJcrNode);

		NodeIterator children = null;
		if (spaceJcrNode.isNodeType(CmsBuiltInItem.Space.getJcrName()))
			children = spaceJcrNode.getNodes(CmsBuiltInItem.Space.getJcrName());

		if (children != null){
			while (children.hasNext()){
				removeSpaceJcrNode(children.nextNode(), session, false, context); //Do not remove child jcr node. Its parent may be removed
			}
		}

		if (removeJcrNode){
			spaceJcrNode.remove();
		}
	}

	private void removeSpaceFromAssociations(EntityAssociationDeleteHelper<Space> entityAssociationDeleteHelper, Node spaceJcrNode)
	throws RepositoryException {
		entityAssociationDeleteHelper.setCmsRepositoryEntityIdToBeRemoved(cmsRepositoryEntityUtils.getCmsIdentifier(spaceJcrNode));
		entityAssociationDeleteHelper.removeOrReplaceAllReferences(Space.class);


	}

	public  Node retrieveParentSpaceNode(Session session, Space space) throws RepositoryException {

		Node spaceParent = null;
		if (space.getParent() != null){
			if (StringUtils.equalsIgnoreCase(CmsBuiltInItem.OrganizationSpace.getLocalPart(), space.getParent().getName())){
				spaceParent = JcrNodeUtils.getOrganizationSpaceNode(session); 
			}
			else{
				spaceParent =  cmsRepositoryEntityUtils.retrieveUniqueNodeForSpace(session, space.getParent().getId());
			}
		}

		if (spaceParent == null){
			throw new CmsException("No parent is defined for space id :"+space.getId() + ", name : "+ space.getName());
		}

		return spaceParent;
	}
	
	public void checkSpaceOwner(Space space, RepositoryUser systemUser, boolean replaceOwner) {
		if (space != null){
			if (
					replaceOwner ||
					space.getOwner() == null ||
					 ( 
						StringUtils.equals(space.getOwner().getExternalId(), CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID) &&
						! StringUtils.equals(space.getOwner().getId(), systemUser.getId())
					 )
				){
				space.setOwner(systemUser);
			}

			if (space.isChildrenLoaded()){
				List<Space> childSpaces = space.getChildren();
				
				if (CollectionUtils.isNotEmpty(childSpaces)){
					for (Space childSpace : childSpaces){
						checkSpaceOwner(childSpace, systemUser,replaceOwner);
					}
				}	
			}
		}
	}
}
