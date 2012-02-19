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


import java.text.Normalizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.criteria.BaseCmsCriteria;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.utility.GreekToEnglish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class CmsRepositoryEntityUtils {
	
	private  final Logger logger = LoggerFactory.getLogger(CmsRepositoryEntityUtils.class);

	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	
	
	public Node retrieveUniqueNodeForCmsRepositoryEntityId(Session session, String cmsRepositoryEntityId) throws RepositoryException
	{
		if (StringUtils.isBlank(cmsRepositoryEntityId))
		{
			return null;
		}
		
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntityId, null);
		
		if (cmsRepositoryEntityNode != null)
		{
			return cmsRepositoryEntityNode;
		}

		
		return  getUniqueNodeForCriteria(session, cmsRepositoryEntityId,new BaseCmsCriteria());
	}
	
	public Node retrieveUniqueNodeForBinaryChannel(Session session, String binaryChannelId) throws RepositoryException
	{
		if (StringUtils.isBlank(binaryChannelId))
		{
			return null;
		}
		
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, binaryChannelId, BinaryChannel.class);
		
		if (cmsRepositoryEntityNode != null)
		{
			return cmsRepositoryEntityNode;
		}

		
		return  getUniqueNodeForCriteria(session, binaryChannelId, CmsCriteriaFactory.newBinaryChannelCriteria());
	}
	
	public Node retrieveUniqueNodeForSpace(Session session, String spaceId) throws RepositoryException
	{
		if (StringUtils.isBlank(spaceId))
		{
			return null;
		}
	
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, spaceId, Space.class);
		
		if (cmsRepositoryEntityNode != null)
		{
			return cmsRepositoryEntityNode;
		}

		return  getUniqueNodeForCriteria(session, spaceId,CmsCriteriaFactory.newSpaceCriteria());
	}
	
	public Node retrieveUniqueNodeForTopic(Session session, String topicId) throws RepositoryException
	{
		if (StringUtils.isBlank(topicId))
		{
			return null;
		}
	
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, topicId, Topic.class);
		
		if (cmsRepositoryEntityNode != null)
		{
			return cmsRepositoryEntityNode;
		}

		return  getUniqueNodeForCriteria(session, topicId,CmsCriteriaFactory.newTopicCriteria());
	}
	
	public Node retrieveUniqueNodeForContentObject(Session session, String contentObjectId) throws RepositoryException
	{
		if (StringUtils.isBlank(contentObjectId)){
			return null;
		}
		
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, contentObjectId, ContentObject.class);
		
		if (cmsRepositoryEntityNode != null){
			return cmsRepositoryEntityNode;
		}

		return  getUniqueNodeForCriteria(session, contentObjectId,CmsCriteriaFactory.newContentObjectCriteria());
	}
	
	public Node retrieveUniqueNodeForRepositoryUser(Session session, String repositoryUserId) throws RepositoryException
	{
		if (StringUtils.isBlank(repositoryUserId)){
			return null;
		}
		
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, repositoryUserId, RepositoryUser.class);
		
		if (cmsRepositoryEntityNode != null){
			return cmsRepositoryEntityNode;
		}
		
		return getUniqueNodeForCriteria(session, repositoryUserId,CmsCriteriaFactory.newRepositoryUserCriteria());
		
	}
	/**
	 * Retrieve object from repository using its id
	 * @param session
	 * @param queryNewRepository 
	 * @param topic
	 * @return
	 * @throws RepositoryException
	 */
	public  Node retrieveUniqueNodeForCmsRepositoryEntity(Session session, CmsRepositoryEntity cmsRepositoryEntity) throws    RepositoryException{
		if (cmsRepositoryEntity == null || StringUtils.isBlank(cmsRepositoryEntity.getId())){
			return null;
		}
		
		CmsCriteria cachedCmsCriteria  = new BaseCmsCriteria();
		
		if (cmsRepositoryEntity instanceof Topic){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), Topic.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newTopicCriteria();
		}
		else if (cmsRepositoryEntity instanceof Space){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), Space.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newSpaceCriteria();
		}
		else if (cmsRepositoryEntity instanceof ContentObject){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), ContentObject.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newContentObjectCriteria();
		}
		else if (cmsRepositoryEntity instanceof RepositoryUser){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), RepositoryUser.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newRepositoryUserCriteria();
		}
		else if (cmsRepositoryEntity instanceof BinaryChannel){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), BinaryChannel.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newBinaryChannelCriteria();
		}
		else if (cmsRepositoryEntity instanceof Taxonomy){
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), Taxonomy.class);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}

			cachedCmsCriteria  = CmsCriteriaFactory.newTaxonomyCriteria();
		}
		else{
			Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, cmsRepositoryEntity.getId(), null);
			
			if (cmsRepositoryEntityNode != null){
				return cmsRepositoryEntityNode;
			}
			
			cachedCmsCriteria = new BaseCmsCriteria();
		}

		if (cachedCmsCriteria != null){
			return getUniqueNodeForCriteria(session, cmsRepositoryEntity.getId(), cachedCmsCriteria);
		}
		else{
			logger.warn("Try to find unique node for type "+ cmsRepositoryEntity.getClass().getName());
			return null;
		}
	}

	private Node checkIdInJcrUUID(Session session, String cmsRepositoryEntityId, 
			Class cmsRepositoryEntityClass) {
		//In most cases cmsRepositoryEntityId is the same with JCR Node UUID
		//Instead of building a query to search for a node with some property
		//we can use session.getNodeByUUID
		//if found just a check that node type is the expected . 
		//If so then procceed with that node
		//otherwise proceed as usual
		
		try{
			Node cmsRepositoryEntityNode = session.getNodeByUUID(cmsRepositoryEntityId);
			
			
			if (cmsRepositoryEntityNode != null){
				
				if (cmsRepositoryEntityClass == null){
					if (cmsRepositoryEntityNode.isNodeType(JcrBuiltInItem.NtBase.getJcrName())){
							//final check. it must have the same value in the property for identifier
							if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
									StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
											cmsRepositoryEntityId)){
								return cmsRepositoryEntityNode;
							}
						}
					}
				else if (cmsRepositoryEntityClass ==  Topic.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
				else if (cmsRepositoryEntityClass ==  Space.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.Space.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
				else if (cmsRepositoryEntityClass ==  ContentObject.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
				else if (cmsRepositoryEntityClass ==  RepositoryUser.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.RepositoryUser.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
				else if (cmsRepositoryEntityClass ==  BinaryChannel.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.BinaryChannel.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
				else if (cmsRepositoryEntityClass ==  Taxonomy.class){
					if (cmsRepositoryEntityNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
						//final check. it must have the same value in the property for identifier
						if (hasCmsIdentifier(cmsRepositoryEntityNode) && 
								StringUtils.equals(getCmsIdentifier(cmsRepositoryEntityNode), 
										cmsRepositoryEntityId)){
							return cmsRepositoryEntityNode;
						}
					}
				}
			}
							
		}
		catch(Exception e){
			//No node found for provided id. Ignore exception and proceed as usual
		}
		
		return null;
	}
	
	private Node getUniqueNodeForCriteria(Session session,String cmsRepositoryEntityId, CmsCriteria cachedCmsCriteria) throws RepositoryException {

		cachedCmsCriteria.addIdEqualsCriterion(cmsRepositoryEntityId);
		cachedCmsCriteria.setOffsetAndLimit(0, 1);
		
		return JcrNodeUtils.uniqueNode(cmsQueryHandler.getNodesFromXPathQuery(session, cachedCmsCriteria));
	}

	public void createCmsIdentifier(Node newCmsRepositoryNode, CmsRepositoryEntity cmsRepositoryEntity, boolean useProvidedIdentifierIfAny) throws RepositoryException {
		
		if (!newCmsRepositoryNode.isNew() || hasCmsIdentifier(newCmsRepositoryNode))
			throw new CmsException("Unable to create Id. Node "+newCmsRepositoryNode.getPath()+" is not new");
		
		if (cmsRepositoryEntity.getId() != null){
			if (!useProvidedIdentifierIfAny)
				throw new CmsException("CmsRepositoryEntity "+ cmsRepositoryEntity.getId() + " already has an Id. Unable to define a new one.");
			else{
				//User has provided with identifier. use this one
				//Provided id must follow a specific pattern
				if (JackrabbitDependentUtils.isValidUUID(cmsRepositoryEntity.getId())){
					newCmsRepositoryNode.setProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), cmsRepositoryEntity.getId());
				}
				else{
					throw new CmsException("CmsRepositoryEntity Id '"+ cmsRepositoryEntity.getId() + "' is invalid. ");
				}
			}
		}
		else{
			//Just a check in case node has not mixin referenceable. Add this mixin and log a warning
			if (!newCmsRepositoryNode.isNodeType(JcrBuiltInItem.MixReferenceable.getJcrName())){
				newCmsRepositoryNode.addMixin(JcrBuiltInItem.MixReferenceable.getJcrName());
				logger.warn("Tried to create an Astroboa identitifier on a node which did not have a mixin referenceable. Node Type {}", newCmsRepositoryNode.getPrimaryNodeType().getName());
			}
				
			final String newCmsIdentifier = newCmsRepositoryNode.getUUID();
			newCmsRepositoryNode.setProperty(CmsBuiltInItem.CmsIdentifier.getJcrName(), newCmsIdentifier);

			cmsRepositoryEntity.setId(newCmsIdentifier);
		}
	}

	public SaveMode determineSaveMode(CmsRepositoryEntity cmsRepositoryEntiy) throws RepositoryException {
		if (StringUtils.isNotBlank(cmsRepositoryEntiy.getId()))
			return SaveMode.UPDATE;
		
		return SaveMode.INSERT;
	}

	public String getCmsIdentifier(Node node) throws RepositoryException {
		return node.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString();
	}
	
	public boolean hasCmsIdentifier(Node node) throws RepositoryException {
		return node.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName());
	}
	
	public boolean isValidSystemName(String systemName){
		if (StringUtils.isBlank(systemName)){
			return true;
		}
		
		return CmsConstants.SystemNamePattern.matcher(systemName).matches();
		
	}
	
	/**
	 * Converts greek characters to English and 
	 * replaces all characters not supported in regular expression
	 * {@link CmsConstants#SYSTEM_NAME_ACCEPTABLE_CHARACTERS} with dash (-)
	 * 
	 * @return
	 */
	public String fixSystemName(String systemName){
		
		if (systemName == null){
			return null;
		}
		
		//Remove leading and trailing whitespace
		systemName = systemName.trim();
		
		//Replace all accented characters
		systemName = deAccent(systemName);
		
		//Replace all Greek Characters with their Latin equivalent
		systemName = GreekToEnglish.convertString(systemName);
		
		//Replace all invalid characters with '-'
		systemName = systemName.replaceAll("[^"+CmsConstants.SYSTEM_NAME_ACCEPTABLE_CHARACTERS+"]", "-");
		
		//Replace all '--' with '-'
		while (systemName.contains("--")){
			systemName = systemName.replaceAll("--","-");
		}
		
		//Remove leading and trailing '-'
		if (systemName.startsWith("-")){
			systemName = systemName.replaceFirst("-", "");
		}
		
		if (systemName.endsWith("-")){
			systemName = StringUtils.removeEnd(systemName,"-");
		}
		
		return systemName;
		
	}
	
	public String deAccent(String systemName) {

		String nfdNormalizedString = Normalizer.normalize(systemName, Normalizer.Form.NFD); 
		   
		return CmsConstants.DIACRITICAL_MARKS.matcher(nfdNormalizedString).replaceAll("");
	}

	public Node retrieveUniqueNodeForTaxonomy(Session session, String taxonomyId) throws RepositoryException{
		if (StringUtils.isBlank(taxonomyId)){
			return null;
		}
	
		Node cmsRepositoryEntityNode = checkIdInJcrUUID(session, taxonomyId, Taxonomy.class);
		
		if (cmsRepositoryEntityNode != null){
			return cmsRepositoryEntityNode;
		}

		return  getUniqueNodeForCriteria(session, taxonomyId,CmsCriteriaFactory.newTaxonomyCriteria());
	}

	public String nodeIdentity(Node node) throws RepositoryException{
		
		if (node == null){
			return "";
		}
		
		String nodeIdentity = node.getPath();
		
		if (node.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){
			nodeIdentity += "["+node.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString() +"]";
		}
		else if (node.hasProperty(CmsBuiltInItem.Name.getJcrName())){
			nodeIdentity += "["+node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString() +"]";
		}
		else if (node.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName())){
			nodeIdentity += "["+node.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString() +"]";
		}
		
		return nodeIdentity;
		
	}
	
	public void addDefaultTaxonomyToTopic(Topic topic){
		
		if (topic != null){
			Taxonomy defaultTaxonomy = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy();
			defaultTaxonomy.setName(CmsBuiltInItem.SubjectTaxonomy.getJcrName());
			topic.setTaxonomy(defaultTaxonomy);
		}

	}
}
