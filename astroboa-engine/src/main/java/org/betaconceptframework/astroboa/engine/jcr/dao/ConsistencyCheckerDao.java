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
package org.betaconceptframework.astroboa.engine.jcr.dao;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrNamespaceConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ConsistencyCheckerDao extends JcrDaoSupport{

	private final Logger logger = LoggerFactory.getLogger(ConsistencyCheckerDao.class);

	@Autowired
	private ContentDefinitionDao contentDefinitionDao;

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils; 

	@Transactional(readOnly=false, rollbackFor = CmsException.class, propagation = Propagation.REQUIRED)
	public void performReferentialIntegrityCheck() throws Exception{
		
		try{
			
			logger.info("Checking consistency on repository "+ AstroboaClientContextHolder.getActiveRepositoryId());
			
			Session session = getSession();
			
			Node objectRootNode = JcrNodeUtils.getContentObjectRootNode(session);
			
			visitNode(objectRootNode,getSession(), new ArrayList<String>());
			
		}
		catch (Exception e)
		{
			throw new CmsException(e);
		}
	}
	
	private void visitNode(Node node, Session session, List<String> alreadyVisitedIds) throws Exception{

		NodeIterator nodeIt = node.getNodes();
		
		if (nodeIt != null){
			while (nodeIt.hasNext()){
				
				Node childNode = nodeIt.nextNode();
				
				if (childNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
					//Object is considered to be a ROOT complex property 
					visitComplexProperty(childNode, session, childNode.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString(), "",alreadyVisitedIds);
				}
				else if (childNode.hasNodes()){
					visitNode(childNode,session,alreadyVisitedIds);
				}
			}
		}
	}

	private void visitComplexProperty(Node nodeRepresentingComplexProperty, Session session, String objectSystemName, String path, List<String> alreadyVisitedIds) throws Exception {
		
		if (nodeRepresentingComplexProperty != null && nodeRepresentingComplexProperty.hasNodes()){
			
			String propertyPath = PropertyPath.createFullPropertyPath(path, nodeRepresentingComplexProperty.getName());
			
			visitSimpleProperties(nodeRepresentingComplexProperty, session, objectSystemName, propertyPath,alreadyVisitedIds);
			
			NodeIterator nodeIt = nodeRepresentingComplexProperty.getNodes();
			
			if (nodeIt != null){
				while (nodeIt.hasNext()){
					
					Node childNode = nodeIt.nextNode();
					
					visitComplexProperty(childNode, session, objectSystemName, propertyPath,alreadyVisitedIds);
				}
			}
		}
		
	}

	private void visitSimpleProperties(Node nodeRepresentingComplexProperty, Session session, String objectSystemName, String parentPropertyFullPath, List<String> alreadyVisitedIds) throws Exception {
		
		if (nodeRepresentingComplexProperty != null && nodeRepresentingComplexProperty.hasProperties()){
			
			PropertyIterator properties = nodeRepresentingComplexProperty.getProperties();
			
			if (properties!=null){
				
				while (properties.hasNext()){
					
					Property property = properties.nextProperty();
					
					String propertyName = property.getName();
					
					//References are always of type STRING
					if (PropertyType.STRING == property.getType()){ 
						
						if (StringUtils.equals(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName(), propertyName)){
							logger.info("Checking consistency on property "+ property.getPath());
							checkOwnerExists(property.getValue().getString(),session, objectSystemName,alreadyVisitedIds);
							continue;
						}
						
						if (propertyName.startsWith(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+":") || 
								propertyName.startsWith(JcrNamespaceConstants.JCR_PREFIX+":") ||
								propertyName.startsWith(JcrNamespaceConstants.MIX_PREFIX+":") ||
								propertyName.startsWith(JcrNamespaceConstants.NT_PREFIX+":")){
							continue;
						}
						
						String propertyPath = PropertyPath.createFullPropertyPath(parentPropertyFullPath, property.getName());
						
						CmsPropertyDefinition propertyDefinition = contentDefinitionDao.getCmsPropertyDefinition(propertyPath);
						
						
						
						if (propertyDefinition == null){
							logger.warn("Could not locate definition for property "+propertyPath);
						}
						else if (ValueType.ObjectReference == propertyDefinition.getValueType()){
							logger.info("Checking consistency on property "+ property.getPath());
							if (propertyDefinition.isMultiple()){
								checkObjectsExist(property.getValues(), session, propertyPath, objectSystemName, alreadyVisitedIds);
							}
							else{
								checkObjectExist(property.getValue(), session, propertyPath, objectSystemName, alreadyVisitedIds);	
							}
						}
						else if (ValueType.TopicReference == propertyDefinition.getValueType()){
							logger.info("Checking consistency on property "+ property.getPath());
							if (propertyDefinition.isMultiple()){
								checkTopicsExist(property.getValues(), session, propertyPath, objectSystemName, alreadyVisitedIds);
							}
							else{
								checkTopicExist(property.getValue(),session, propertyPath, objectSystemName,alreadyVisitedIds);	
							}
						}
					}
				}
			}
			
		}
		
	}


	private void checkObjectsExist(Value[] objectReferences, Session session, String propertyPath, String objectSystemName, List<String> alreadyVisitedIds) throws RepositoryException {
		if (objectReferences != null){
			for (Value objectReference : objectReferences){
				checkObjectExist(objectReference,session, propertyPath, objectSystemName,alreadyVisitedIds);
			}
		}
		
	}

	private void checkTopicsExist(Value[] topicReferences, Session session, String propertyPath, String objectSystemName, List<String> alreadyVisitedIds) throws RepositoryException {
		
		if (topicReferences != null){
			for (Value topicReference : topicReferences){
				checkTopicExist(topicReference,session, propertyPath, objectSystemName, alreadyVisitedIds);
			}
		}
		
	}

	private void checkOwnerExists(String ownerId, Session session,	String objectSystemName, List<String> alreadyVisitedIds) throws RepositoryException {
		
		if (alreadyVisitedIds.contains(ownerId)){
			return;
		}
		
		if (cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, ownerId) == null){
			logger.warn("No Repository User found with id {}. Object {}'s owner contains an invalid repository user reference", 
					ownerId, objectSystemName);
		}
		
		alreadyVisitedIds.add(ownerId);
	}

	private void checkTopicExist(Value topicReference, Session session, String propertyPath, String objectSystemName, List<String> alreadyVisitedIds) throws RepositoryException {

		
		String topicId = topicReference.getString();
		
		if (alreadyVisitedIds.contains(topicId)){
			return;
		}

		if (cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, topicId) == null){
			logger.warn("No topic found with id {}. Property {} of object {} contains an invalid topic reference", 
					new Object[]{topicId, propertyPath, objectSystemName});
		}
		
		alreadyVisitedIds.add(topicId);
		
	}

	private void checkObjectExist(Value objectReference, Session session, String propertyPath, String objectSystemName, List<String> alreadyVisitedIds) throws RepositoryException {

		String objectId = objectReference.getString();
		
		if (alreadyVisitedIds.contains(objectId)){
			return;
		}

		if (cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, objectId) == null){
			logger.warn("No Object found with id {}. Property {} of object {} contains an invalid object reference", 
					new Object[]{objectId, propertyPath, objectSystemName});
		}

		alreadyVisitedIds.add(objectId);
		
	}

}