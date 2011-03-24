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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.database.model.CmsRepositoryEntityAssociation;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class EntityAssociationDeleteHelper<T extends CmsRepositoryEntity> extends EntityAssociationHelperBase<T> {

	public EntityAssociationDeleteHelper(
			Session session,
			CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao,
			Context context) {
		
		super(session, cmsRepositoryEntityAssociationDao, context);

	}

	private final Logger logger = LoggerFactory.getLogger(EntityAssociationDeleteHelper.class);

	private String cmsRepositoryEntityIdToBeRemoved;
	private String cmsRepositoryEntityIdReplacement;

	public void setCmsRepositoryEntityIdToBeRemoved(
			String cmsRepositoryEntityIdToBeRemoved) {
		this.cmsRepositoryEntityIdToBeRemoved = cmsRepositoryEntityIdToBeRemoved;
	}

	public void setCmsRepositoryEntityIdReplacement(
			String cmsRepositoryEntityIdReplacement) {
		this.cmsRepositoryEntityIdReplacement = cmsRepositoryEntityIdReplacement;
	}

	public void removeOrReplaceAllReferences(Class<T> entityType) throws RepositoryException{

		//First remove all associations FROM this reference
		//This method may be removed
		//removeOrReplaceAssociationWhereEntityToBeRemovedIsAReferrer(entityType);

		//Then remove Associations TO reference
		removeOrReplaceAssociationsWhereEntityToBERemovedIsReferenced(entityType);

	}

	private void removeOrReplaceAssociationsWhereEntityToBERemovedIsReferenced(Class<T> entityType) throws RepositoryException {

		List<CmsRepositoryEntityAssociation> associationsFromEntity = cmsRepositoryEntityAssociationDao.getAllAssociationsOfReferencedEntity(cmsRepositoryEntityIdToBeRemoved, entityType);

		Value replacementValue = (cmsRepositoryEntityIdReplacement == null)? null: JcrValueUtils.getJcrValue(cmsRepositoryEntityIdReplacement, ValueType.String, session.getValueFactory());

		for (CmsRepositoryEntityAssociation associationFromEntity : associationsFromEntity)
		{
			Node referrerNode = null;
			//Retrieve referrer node for this association
			if (associationFromEntity.getReferrerPropertyContainerId() != null)
				referrerNode = getNode(associationFromEntity.getReferrerPropertyContainerId());
			else
				referrerNode = getNode(associationFromEntity.getReferrerCmsRepositoryEntityId());

			if (referrerNode == null){
				logger.warn("Probably found a cms entity association whose referrer entity id or its referrer property container id do not represent an existing jcr node." +
						" If this is the case this entity assoiation must be deleted manually. {}",associationFromEntity.toString());
			}
			else{	

				ItemQName referrerProperty = ItemUtils.createSimpleItem(associationFromEntity.getReferrerPropertyName());

				Value oldValue = JcrValueUtils.getJcrValue(associationFromEntity.getReferencedCmsRepositoryEntityId(), ValueType.String, session.getValueFactory());

				checkOutNode(referrerNode);

				if (replacementValue == null){
					//Remove Value from JCR
					JcrValueUtils.removeValue(referrerNode, referrerProperty, oldValue, null );
				}
				else{
					//Replace value in JCR
					JcrValueUtils.replaceValue(referrerNode, referrerProperty, replacementValue, oldValue, null);
				}
			}
		}
	}

	private void checkOutNode(Node node) throws RepositoryException  {
		Node versionnedNode = node;
		try {
			while (versionnedNode != null && !versionnedNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()) 
					&& !versionnedNode.isNodeType(CmsBuiltInItem.SYSTEM.getJcrName())){
				versionnedNode = versionnedNode.getParent();
			}

			if (versionnedNode != null && versionnedNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()) && !versionnedNode.isCheckedOut())
				versionnedNode.checkout();
		} catch (Exception e) {
			throw new RepositoryException(e);
		}

	}

}
