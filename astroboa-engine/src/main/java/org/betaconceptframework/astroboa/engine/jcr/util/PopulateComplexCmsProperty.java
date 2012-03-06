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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDefinitionDao;
import org.betaconceptframework.astroboa.engine.jcr.visitor.ComplexCmsPropertyNodeRemovalVisitor;
import org.betaconceptframework.astroboa.model.impl.CmsPropertyIndexable;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsRootPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.CmsReadOnlyItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class PopulateComplexCmsProperty{

	private final Logger logger = LoggerFactory.getLogger(PopulateComplexCmsProperty.class);

	@Autowired
	private PrototypeFactory prototypeFactory;
	@Autowired
	private ContentDefinitionDao contentDefinitionDao;
	
	private ComplexCmsPropertyDefinition complexPropertyDefinition;
	private ComplexCmsProperty complexProperty;
	private Node complexPropertyNode;
	
	private Session session;

	private SaveMode saveMode;

	//Collect all exceptions about non existence of mandatory child properties
	//If this complex cms property is optional and no child property has been saved
	//then throw these exceptions
	private List<String> exceptionsForMandatoryProperties = new ArrayList<String>();

	private boolean atLeastOneChildPropertySaved = false;

	private String contentObjectNodeUUID;
	
	private Context context;

	public void setContext(Context context) {
		this.context = context;
	}

	private boolean complexCmsPropertyNodeRepresentsAContentObjectNode;
	/**
	 * @param contentObjectNodeUUID the contentObjectNodeUUID to set
	 */
	public void setContentObjectNodeUUID(String contentObjectNodeUUID) {
		this.contentObjectNodeUUID = contentObjectNodeUUID;
	}

	public List<String> getExceptionsForMandatoryProperties() {
		return exceptionsForMandatoryProperties;
	}

	public boolean atLeastOneChildPropertySaved() {
		return atLeastOneChildPropertySaved;
	}
	
	public void propertyIsPopulated(String propertyName) throws RepositoryException{
		atLeastOneChildPropertySaved = true;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setSaveMode(SaveMode saveMode) {
		this.saveMode = saveMode;
	}

	public void setComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {
		this.complexPropertyDefinition = complexPropertyDefinition;
	}

	public void setComplexProperty(ComplexCmsProperty<?,?> complexProperty) {
		this.complexProperty = complexProperty;

	}


	public void setComplexPropertyNode(Node complexPropertyNode) {
		this.complexPropertyNode = complexPropertyNode;

		if (this.complexPropertyNode == null){
			throw new CmsException("Complex cms property node is null");
		}
		
		try {
			complexCmsPropertyNodeRepresentsAContentObjectNode = complexPropertyNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName());
		} catch (RepositoryException e) {
			throw new CmsException(e);
		} 
	}

	public void populate() throws Exception{
		

		if (logger.isDebugEnabled()){
			logger.debug("Starting complex property {} population.",complexProperty.getFullPath());
		}

		checkDefinitionWithProperty();

		//Visit child properties
		populateChildProperties();

		//Visit aspects
		populateAspects();
		if (logger.isDebugEnabled()){
			logger.debug("Ended complex property {} population. At least one property is saved : {}. Exception messages for mandatory properties {}",
					new Object[]{complexProperty.getFullPath(), atLeastOneChildPropertySaved, exceptionsForMandatoryProperties});
		}


	}

	private void populateAspects() throws Exception {

		if (complexPropertyNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()) && 
				complexProperty instanceof ComplexCmsRootProperty) {

			//Populate Aspects
			List<String> newAspects = ((ComplexCmsRootProperty)complexProperty).getAspects();
			
			boolean newAspectsExist = CollectionUtils.isNotEmpty(newAspects);
			if (newAspectsExist){
				for (String aspect : newAspects){
					//Retrieve aspect definition
					ComplexCmsPropertyDefinition aspectDefinition = contentDefinitionDao.getAspectDefinition(aspect);

					if (aspectDefinition == null)
						throw new CmsException("Aspect "+ aspect + " does not have a definition");

					populateAccordingToChildPropertyDefinition(aspect, aspectDefinition);

				}
			}

			// Check which aspects should be removed
			Value[] existingAspects = null;
			if (complexPropertyNode.hasProperty(CmsBuiltInItem.Aspects.getJcrName())){
				existingAspects = complexPropertyNode.getProperty(CmsBuiltInItem.Aspects.getJcrName()).getValues();
			}

			Map<String, Node> aspectNodesToBeRemoved = new HashMap<String, Node>();

			if (!ArrayUtils.isEmpty(existingAspects)){
				for (Value existingAspect : existingAspects){
					//If aspect list is empty or it does not contain existing aspect, remove aspect node
					String existingAspectName = existingAspect.getString();
					
					//Remove existing aspect only if it has been removed by the user
					if (!newAspects.contains(existingAspectName)){ 
						
						if (((ComplexCmsPropertyImpl)complexProperty).cmsPropertyHasBeenLoadedAndRemoved(existingAspectName)){
									
							//Node may have already been removed
							if (complexPropertyNode.hasNode(existingAspectName)){
								aspectNodesToBeRemoved.put(existingAspectName, complexPropertyNode.getNode(existingAspectName));
							}
						}
						else{
							if (newAspectsExist){
								logger.warn("Property " + existingAspectName +" is not included in the aspect list '"+newAspects+"' of the content object "+contentObjectNodeUUID + 
										" but it has not removed properly from the content object. This is probably a bug, however property will not be removed from the repository.");
							}
							//Load Aspect again
							((ComplexCmsRootPropertyImpl)complexProperty).loadAspectDefinition(existingAspectName);
						}
					}					
				}
			}

			//Remove deleted aspects
			if (MapUtils.isNotEmpty(aspectNodesToBeRemoved)){
				ComplexCmsPropertyNodeRemovalVisitor aspectNodeRemovalVisitor = prototypeFactory.newComplexCmsPropertyNodeRemovalVisitor();
				aspectNodeRemovalVisitor.setParentNode(complexPropertyNode,complexPropertyNode);
				aspectNodeRemovalVisitor.setSession(session);

				for (Node aspectNodeToBeRemoved : aspectNodesToBeRemoved.values()){
					
					//Find aspect definition
					String aspectName = aspectNodeToBeRemoved.getName();
					
					ComplexCmsPropertyDefinition aspectDefinition = contentDefinitionDao.getAspectDefinition(aspectName);
					
					if (aspectDefinition == null)
						throw new CmsException("Unable to find definition for aspect "+ aspectName+". Aspect jcr node "+ aspectNodeToBeRemoved.getPath()+" will not be removed from content object jcr node");
					else{
						aspectNodeRemovalVisitor.loadChildNodesToBeDeleted(aspectName);
						
						aspectDefinition.accept(aspectNodeRemovalVisitor);
						
						aspectNodeToBeRemoved.remove();
					}
				}
			}
			
			//Finally it may be the case that an aspect has been specified but finally
			//no jcr equivalent node exists. For example one provided an aspect but without
			//any value for at least one of its child properties. In that case repository
			//has chosen to delete any previous jcr node for this aspect, or not to create 
			//a jcr node if the aspect was not there at first place.
			List<String> aspectsToBeRemoved = new ArrayList<String>();
			for (String finalAspect: newAspects){
				if (!complexPropertyNode.hasNode(finalAspect)){
					aspectsToBeRemoved.add(finalAspect);
				}
			}
			
			if (aspectsToBeRemoved.size() > 0){
				newAspects = (List<String>) CollectionUtils.disjunction(newAspects, aspectsToBeRemoved);
				
				//Also remove them from complex property
				for (String aspectToBeRemoved : aspectsToBeRemoved){
					if (((ComplexCmsRootProperty)complexProperty).hasAspect(aspectToBeRemoved)){
						((ComplexCmsRootProperty)complexProperty).removeChildProperty(aspectToBeRemoved);
					}
				}
			}
			
			//Update aspects jcr property with new values
			JcrNodeUtils.addMultiValueProperty(complexPropertyNode, CmsBuiltInItem.Aspects, SaveMode.UPDATE, newAspects, ValueType.String, session.getValueFactory());
		}

	}

	private void populateChildProperties()
	throws RepositoryException, Exception {
		Map<String, CmsPropertyDefinition> childPropertyDefinitions = complexPropertyDefinition.getChildCmsPropertyDefinitions();

		if (MapUtils.isNotEmpty(childPropertyDefinitions)){
			for (Entry<String, CmsPropertyDefinition> childPropertyDefinitionEntry : childPropertyDefinitions.entrySet()){
				String childPropertyName = childPropertyDefinitionEntry.getKey();

				//Check that no built in property has been provided for population
				if (complexCmsPropertyNodeRepresentsAContentObjectNode &&  
						(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName().equals(childPropertyName) || 
								CmsBuiltInItem.ContentObjectTypeName.getJcrName().equals(childPropertyName)	||
								CmsBuiltInItem.Aspects.getJcrName().equals(childPropertyName))
				){
					logger.debug("Try to populate cms builtin property {} in content object node {}",childPropertyName , complexPropertyNode.getPath() );
				}
				else{
					populateAccordingToChildPropertyDefinition(childPropertyName, childPropertyDefinitionEntry.getValue());
				}
			}
			
			if (CollectionUtils.isNotEmpty(exceptionsForMandatoryProperties)){
				
				//Special Case 
				clearExceptionMessagesIfTheOnlyPropertiesWhichHaveValuesAreOfTypeBoolean();
				
				//Throw exception if at least one child property is saved
				//or we reach a root complex definition which is mandatory
				if (CollectionUtils.isNotEmpty(exceptionsForMandatoryProperties) && (atLeastOneChildPropertySaved() || 
						(complexPropertyDefinition.isMandatory() &&
								complexProperty.getParentProperty() != null && 
								complexProperty.getParentProperty() instanceof ComplexCmsRootProperty))){
					

						logger.debug("Exceptions for mandatory properties found {}. Exception message is being built.", exceptionsForMandatoryProperties);
						
						StringBuffer sb = new StringBuffer();
						sb.append("There are some mandatory children of the property "+complexProperty.getFullPath() + " which need to be saved but they do not have any value. " +
								(atLeastOneChildPropertySaved()? "There is at least one child property which can be saved without any problem": ""));
						
						for (String exceptionMessage : exceptionsForMandatoryProperties){
							sb.append("\n");
							sb.append(exceptionMessage);
						}

						throw new CmsException(sb.toString());
				}
				else{
					if ((! atLeastOneChildPropertySaved() && !complexPropertyDefinition.isMandatory()) 
						|| (complexProperty.getParentProperty() != null && 
							complexProperty.getParentProperty() instanceof ComplexCmsRootProperty)){
						
						//Clear exception messages if 
						//1. Property is not mandatory and no child property has any value to be saved.
						//2. we have reached a root property
						exceptionsForMandatoryProperties.clear();
					}
				}
			}

		}
		else {
			logger.warn("Complex content object property {} does not have any child property definitions",  complexPropertyDefinition.getName());
		}
	}

	private void clearExceptionMessagesIfTheOnlyPropertiesWhichHaveValuesAreOfTypeBoolean(){
		if (atLeastOneChildPropertySaved()){
			//Iterate all the loaded simple properties of the complex property.
			//In case the only property which has value is a Boolean Property
			//then clear exception messages and mark this property as empty
			Map<String, List<CmsProperty<?,?>>> loadedProperties = complexProperty.getChildProperties();
			
			boolean foundAtLeastOnePropertyWhoseValueMustBeSaved = false;

			for (List<CmsProperty<?,?>> propertyList : loadedProperties.values()){
				
				for (CmsProperty<?,?> property : propertyList){
					if (property instanceof SimpleCmsProperty){
						
						if (((SimpleCmsProperty)property).hasValues()){
							if (property.getValueType() != ValueType.Boolean){
								foundAtLeastOnePropertyWhoseValueMustBeSaved = true;
								break;
							}
							else{
								if (property.getPropertyDefinition().isMultiple() && ((SimpleCmsProperty)property).getSimpleTypeValues().size()>1){
									foundAtLeastOnePropertyWhoseValueMustBeSaved = true;
									break;
								}
								
								if (//Property is a boolean property and its value is not the same with the default value or
										(((BooleanProperty)property).getPropertyDefinition() != null &&
										((BooleanProperty)property).getPropertyDefinition().isSetDefaultValue() && 
										((BooleanProperty)property).getPropertyDefinition().getDefaultValue() != ((BooleanProperty)property).getFirstValue() ) ||  
										//no default value is set and its value is TRUE
										(((BooleanProperty)property).getFirstValue())){
									foundAtLeastOnePropertyWhoseValueMustBeSaved = true;
									break;
								}
							}
						}
					}
					else if (property.getPropertyDefinition() != null && property.getPropertyDefinition().isMandatory()){
						foundAtLeastOnePropertyWhoseValueMustBeSaved = true;
					}
				}
			}
			
			if (! foundAtLeastOnePropertyWhoseValueMustBeSaved){
				atLeastOneChildPropertySaved = false;
				exceptionsForMandatoryProperties.clear();
			}
			
		}
	}



	private void populateAccordingToChildPropertyDefinition(
			String childPropertyName,	CmsPropertyDefinition childPropertyDefinition)
	throws RepositoryException, Exception {

		//Populate only if child property is not obsolete
		//and is not one of HasVersion or Versions
		if (!childPropertyDefinition.isObsolete() && 
				! CmsReadOnlyItem.HasVersion.getJcrName().equals(childPropertyName) &&
				! CmsReadOnlyItem.Versions.getJcrName().equals(childPropertyName)){

				//Populate only if child property is loaded
				if (complexProperty.isChildPropertyLoaded(childPropertyName)){
					
					if (ValueType.Complex == childPropertyDefinition.getValueType()){
						populateComplexChildProperty(childPropertyName, (ComplexCmsPropertyDefinition)childPropertyDefinition);
					}
					else{
						populateSimpleChildProperty(childPropertyName, (SimpleCmsPropertyDefinition)childPropertyDefinition);
					}
				}
				else{
					
					//Special case. If object is a new object and property is accessibility then it must be loaded with the default values
					if ( SaveMode.INSERT == saveMode && childPropertyDefinition instanceof ComplexCmsPropertyDefinition &&
							StringUtils.equals("accessibility", childPropertyName) && 
							complexProperty instanceof ComplexCmsRootProperty){
						
						((ComplexCmsPropertyImpl)complexProperty).createNewChildCmsPropertyTemplate(childPropertyName, false);
						
						populateComplexChildProperty(childPropertyName, (ComplexCmsPropertyDefinition)childPropertyDefinition);
					}
					//In case where property is a MANDATORY simple property with a default value
					//initialize property and populate it
					else if ( childPropertyDefinition instanceof SimpleCmsPropertyDefinition && 
							((SimpleCmsPropertyDefinition)childPropertyDefinition).isMandatory() && 
							((SimpleCmsPropertyDefinition)childPropertyDefinition).isSetDefaultValue()){
						
						((ComplexCmsPropertyImpl)complexProperty).createNewChildCmsPropertyTemplate(childPropertyName, false);
						
						populateSimpleChildProperty(childPropertyName, (SimpleCmsPropertyDefinition)childPropertyDefinition);
					}
				}
				
				//If property is mandatory then it should exist in repository either as a node or as a property
				//depending on its type
				boolean complexPropertyNodeHasChildProperty = false;

				if (ValueType.Binary == childPropertyDefinition.getValueType() || 
						ValueType.Complex == childPropertyDefinition.getValueType()){
					complexPropertyNodeHasChildProperty = complexPropertyNode.hasNode(childPropertyName);
				}
				else{ 
					complexPropertyNodeHasChildProperty = complexPropertyNode.hasProperty(childPropertyName);
				}

				if (! complexPropertyNodeHasChildProperty){
					
					if (childPropertyDefinition.isMandatory()){
						logger.debug("Mandatory property {} has not been saved to corresponding jcr node {}", childPropertyName, complexPropertyNode.getPath());
						exceptionsForMandatoryProperties.add("Mandatory property "+ childPropertyName + " has not been saved to corresponding jcr node "+  complexPropertyNode.getPath());
					}
				}
				else{
					propertyIsPopulated(childPropertyName);
				}
			}
	}

	private void populateComplexChildProperty(String childPropertyName,	ComplexCmsPropertyDefinition childPropertyDefinition) throws Exception {

		//Retrieve all complex child properties
		List<CmsProperty<?,?>> childComplexProperties = new ArrayList<CmsProperty<?,?>>();
		boolean childComplexCmsPropertyLoaded = false;

		if ( ((ComplexCmsPropertyImpl)complexProperty).cmsPropertyHasBeenLoadedAndRemoved(childPropertyName) ){
			childComplexCmsPropertyLoaded = true;
		}
		else if (childPropertyDefinition.isMultiple()){
			List<CmsProperty<?,?>> childPropertyList = complexProperty.getChildPropertyList(childPropertyName);

			//A not null list specifies that child complex cms property has been loaded
			if (childPropertyList != null){

				childComplexCmsPropertyLoaded = true;

				if (CollectionUtils.isNotEmpty(childPropertyList)){
					childComplexProperties.addAll(childPropertyList);
				}
			}

		}
		else{
			CmsProperty<?,?> childProperty = complexProperty.getChildProperty(childPropertyName);
			if (childProperty != null){
				childComplexCmsPropertyLoaded = true;
				childComplexProperties.add(childProperty);
			}
		}

		Map<String, Node> existingChildComplexNodes = retrieveExistingChildComplexNodesWithPath(childPropertyName);

		Map<Node, Integer> indicesForNodesToBePersisted = new HashMap<Node, Integer>();
		
		if (CollectionUtils.isNotEmpty(childComplexProperties)){

			for (CmsProperty<?,?> childComplexContentObjectProperty : childComplexProperties){
				
				String childComplexContentObjectPropertyId = childComplexContentObjectProperty.getId();

				Node childComplexPropertyNode = null;

				//Complex Cms Property is considered new if no id is found or
				//an id is provided but not found here
				if (StringUtils.isBlank(childComplexContentObjectPropertyId)){
					
					//Special case
					//if complex child property is single valued, user did not provide any id
					//and there is one node already existing (that is an update is taking place
					//use that node in order to update it and do not create a new one. This way
					//no node removal will take place
					if (!childPropertyDefinition.isMultiple() && existingChildComplexNodes.size() == 1){
						childComplexContentObjectPropertyId = existingChildComplexNodes.keySet().iterator().next();
						childComplexPropertyNode = existingChildComplexNodes.get(childComplexContentObjectPropertyId);
						
						childComplexContentObjectProperty.setId(childComplexContentObjectPropertyId);
						((LazyCmsProperty) childComplexContentObjectProperty).setPropertyContainerNodeUUID(childComplexPropertyNode.getIdentifier());
						((LazyCmsProperty) childComplexContentObjectProperty).setContentObjectNodeUUID(contentObjectNodeUUID);

					}
					else{
						childComplexPropertyNode = createNewNodeForChildComplexCmsProperty(
							childPropertyName, childPropertyDefinition,
							existingChildComplexNodes,
							childComplexContentObjectProperty, false);
					}
				}
				else{
					childComplexPropertyNode = existingChildComplexNodes.get(childComplexContentObjectPropertyId);

					if (childComplexPropertyNode == null){
						//It may a complex cms property with provided id
						childComplexPropertyNode = createNewNodeForChildComplexCmsProperty(
								childPropertyName, childPropertyDefinition,
								existingChildComplexNodes,
								childComplexContentObjectProperty, true);
						
						if (childComplexPropertyNode == null){
							throw new CmsException("Complex content object property "+ childComplexContentObjectProperty.getFullPath() + " does not exist in repository");
						}
					}

				}
				
				//Create a new PopulateTask
				PopulateComplexCmsProperty childComplexCmsPropertyPopulateTask = populateChildComplexCmsProperty(childPropertyDefinition,	(ComplexCmsProperty)childComplexContentObjectProperty, childComplexPropertyNode);

				//In case child property has no child on its own saved,  node must be removed as well
				String childComplexPropertyNodeIdentifier = context.getCmsRepositoryEntityUtils().getCmsIdentifier(childComplexPropertyNode);
				
				//If no child has been saved remove it 
				if (!childComplexCmsPropertyPopulateTask.atLeastOneChildPropertySaved()){
						existingChildComplexNodes.put(childComplexPropertyNodeIdentifier, childComplexPropertyNode);
						childComplexContentObjectProperty.setId(null);
						
						if (childComplexContentObjectProperty instanceof LazyCmsProperty){
							((LazyCmsProperty)childComplexContentObjectProperty).setPropertyContainerNodeUUID(null);
						}
						
						//Tree may contain empty nodes and properties but there are some exception messages
						//Keep them if child is mandatory
						if (complexPropertyDefinition.isMandatory() && CollectionUtils.isNotEmpty(childComplexCmsPropertyPopulateTask.getExceptionsForMandatoryProperties())){
							this.exceptionsForMandatoryProperties.addAll(childComplexCmsPropertyPopulateTask.getExceptionsForMandatoryProperties());
						}
						
				}
				else{
					//Remove from cache so that this node will not be deleted
					if (StringUtils.isNotBlank(childComplexContentObjectPropertyId)){
						existingChildComplexNodes.remove(childComplexContentObjectPropertyId);
					}
					
					if (childComplexContentObjectProperty instanceof CmsPropertyIndexable){
						indicesForNodesToBePersisted.put(childComplexPropertyNode, ((CmsPropertyIndexable)childComplexContentObjectProperty).getIndex()); //Index is zero based
					}
					
				}
			}
		}

		//Remove all nodes that had not been found only if properties have been loaded
		if (!existingChildComplexNodes.isEmpty() && childComplexCmsPropertyLoaded){
			removeNodesThatCorrespondToUnmatchComplexCmsProperties(existingChildComplexNodes, childPropertyDefinition);
		}
		
		//only meaningful for multi valued complex properties where we want to keep insertion order
		if (!indicesForNodesToBePersisted.isEmpty() && indicesForNodesToBePersisted.size() > 1){
			
			for (Entry<Node, Integer> indexPerNode : indicesForNodesToBePersisted.entrySet()){
				Node childNode = indexPerNode.getKey();
				
				Integer newIndex = indexPerNode.getValue();
				
				childNode.setProperty(CmsBuiltInItem.Order.getJcrName(), newIndex.longValue());
				
			}
		}

	}

	private Node createNewNodeForChildComplexCmsProperty(
			String childPropertyName,
			ComplexCmsPropertyDefinition childPropertyDefinition,
			Map<String, Node> existingChildComplexNodes,
			CmsProperty<?, ?> childComplexContentObjectProperty, boolean useProvidedId)
			throws RepositoryException {
		Node childComplexPropertyNode;
		//Child Property is new. Check if this property is single-value but there are
		//already more than one nodes with the same name. This case should never happen
		//but perform check anyway
		if (!childPropertyDefinition.isMultiple() && existingChildComplexNodes.size() >1)
		{
			StringBuilder pathOfNodes = new StringBuilder("");
			for (Node existingNode: existingChildComplexNodes.values()){
				pathOfNodes.append("\n"+ existingNode.getPath());
			}

			throw new CmsException("Try to add new single value complex property "+ childPropertyName + " " +
					" but there is already at least two jcr node with the same name "+ 
					pathOfNodes.toString() + " Parent Node "+ complexPropertyNode.getPath());
		}

		//Create new node for complex property
		childComplexPropertyNode = JcrNodeUtils.addNodeForComplexCmsProperty(complexPropertyNode, childComplexContentObjectProperty.getName());

		context.getCmsRepositoryEntityUtils().createCmsIdentifier(childComplexPropertyNode, childComplexContentObjectProperty, useProvidedId);
		
		//Also append UUIDs necessary for lazy loading
		((LazyCmsProperty) childComplexContentObjectProperty).setPropertyContainerNodeUUID(childComplexPropertyNode.getIdentifier());
		((LazyCmsProperty) childComplexContentObjectProperty).setContentObjectNodeUUID(contentObjectNodeUUID);
		
		return childComplexPropertyNode;
	}

	private void removeNodesThatCorrespondToUnmatchComplexCmsProperties(Map<String, Node> childComplexNodesToBeRemoved, ComplexCmsPropertyDefinition childPropertyDefinition) throws Exception{
		if (MapUtils.isNotEmpty(childComplexNodesToBeRemoved))
		{
			ComplexCmsPropertyNodeRemovalVisitor childComplexCmsPropertyNodeRemovalVisitor = prototypeFactory.newComplexCmsPropertyNodeRemovalVisitor();
			childComplexCmsPropertyNodeRemovalVisitor.setParentNode(complexPropertyNode,null);
			childComplexCmsPropertyNodeRemovalVisitor.setSession(session);

			for (Node childComplexNodeToBeRemoved : childComplexNodesToBeRemoved.values()){
				
				childComplexCmsPropertyNodeRemovalVisitor.addChildNodeToBeDeleted(childComplexNodeToBeRemoved);
				
				childPropertyDefinition.accept(childComplexCmsPropertyNodeRemovalVisitor);
				
				childComplexNodeToBeRemoved.remove();
			}
		}
	}

	private PopulateComplexCmsProperty populateChildComplexCmsProperty(ComplexCmsPropertyDefinition childPropertyDefinition,ComplexCmsProperty childComplexContentObjectProperty, Node childPropertyNode) throws Exception {

		PopulateComplexCmsProperty populateTask = prototypeFactory.newPopulateComplexCmsProperty();
		
		populateTask.setComplexProperty(childComplexContentObjectProperty);
		populateTask.setComplexPropertyDefinition(childPropertyDefinition);
		populateTask.setSaveMode(saveMode);
		populateTask.setSession(session);
		populateTask.setComplexPropertyNode(childPropertyNode);
		populateTask.setContentObjectNodeUUID(contentObjectNodeUUID);
		populateTask.setContext(context);
		populateTask.populate();

		return populateTask;
	}

	private Map<String, Node> retrieveExistingChildComplexNodesWithPath(String childPropertyName) throws RepositoryException {
		Map<String, Node> existingNodes = new HashMap<String, Node>();

		NodeIterator nodes = complexPropertyNode.getNodes(childPropertyName);
		if (complexPropertyNode.hasNode(childPropertyName)){
			while (nodes.hasNext())	{
				Node nextNode = nodes.nextNode();
				if (context.getCmsRepositoryEntityUtils().hasCmsIdentifier(nextNode)){
					existingNodes.put(context.getCmsRepositoryEntityUtils().getCmsIdentifier(nextNode), nextNode);
				}
				else{
					throw new CmsException("Jcr node "+ nextNode.getPath() + " does not have an identifier as expected");
				}
			}
		}

		return existingNodes;

	}

	private void populateSimpleChildProperty(String childPropertyName, SimpleCmsPropertyDefinition childPropertyDefinition) throws Exception {

		long start = System.currentTimeMillis();
		
		//Get Simple Property
		CmsProperty<?,?> simpleCmsProperty = complexProperty.getChildProperty(childPropertyName);

			if (simpleCmsProperty == null){
				//Normally this should never happen
				throw new CmsException("Complex Content Object Property "+  complexPropertyNode.getPath() 
							+ " does not contain mandatory property "+ childPropertyName+ ". Method ComplexCmsProperty#isChildPropertyLoaded " +
									"returned 'true' but method ComplexCmsProperty#getChildProperty return null...");
			}
			else{
				PopulateSimpleCmsProperty populateSimpleProperty = prototypeFactory.newPopulateSimpleCmsProperty();
				populateSimpleProperty.setCmsPropertyToBeSaved((SimpleCmsProperty)simpleCmsProperty);
				populateSimpleProperty.setPropertyContainerNode(complexPropertyNode);
				populateSimpleProperty.setPropertyDefinition(childPropertyDefinition);
				populateSimpleProperty.setPropertyHasBeenLoadedAndRemoved(((ComplexCmsPropertyImpl)complexProperty).cmsPropertyHasBeenLoadedAndRemoved(childPropertyName));
				populateSimpleProperty.setSaveMode(saveMode);
				populateSimpleProperty.setContext(context);
				populateSimpleProperty.setSession(session);

				populateSimpleProperty.populate();
				
				logger.debug("Simple child Property {} populated in  {}", childPropertyDefinition.getFullPath(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS"));

			}
	}

	private void checkDefinitionWithProperty() {

		if ( !complexProperty.getName().equals(complexPropertyDefinition.getName())){
			throw new CmsException("Content object property name "+ complexProperty.getName() + " does not match with definition name "+
					complexPropertyDefinition.getName());
		}


	}
}

