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
package org.betaconceptframework.astroboa.engine.model.lazy.local;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.renderer.BinaryChannelRenderer;
import org.betaconceptframework.astroboa.engine.jcr.renderer.CmsRepositoryEntityRenderer;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectRenderer;
import org.betaconceptframework.astroboa.engine.jcr.renderer.TopicRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrValueUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.VersionUtils;
import org.betaconceptframework.astroboa.model.impl.BinaryPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.BooleanPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.CalendarPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.CmsRepositoryEntityImpl;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.DoublePropertyImpl;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.LongPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ObjectReferencePropertyImpl;
import org.betaconceptframework.astroboa.model.impl.StringPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.TopicReferencePropertyImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.CmsReadOnlyItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyComplexCmsPropertyLoader {

	private final Logger logger = LoggerFactory.getLogger(LazyComplexCmsPropertyLoader.class);

	@Autowired
	private BinaryChannelRenderer binaryChannelRenderer;
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;

	@Autowired
	private VersionUtils versionUtils;
	@Autowired
	private TopicRenderer topicRenderer;
	@Autowired
	private ContentObjectRenderer contentObjectRenderer;

	public List<CmsProperty<?, ?>> renderChildProperty(CmsPropertyDefinition currentChildPropertyDefinition, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties, Session session, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) {

		try{
			if (currentChildPropertyDefinition == null){
				logger.warn("No cms property definition is provided for parent node UUID {} and content object node UUID {}", 
						jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct);
				return null;
			}

			if (logger.isDebugEnabled()){
				logger.debug("Lazy render property {}",
					currentChildPropertyDefinition.getFullPath());
			}

			//Load property container node if a UUID is provided
			//Otherwise a blank template for this child property will be created
			Node propertyContainerNode = null;
			if (StringUtils.isNotBlank(jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty)){
				propertyContainerNode = session.getNodeByIdentifier(jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
			}

			if (cachedCmsRepositoryEntities == null){
				cachedCmsRepositoryEntities = new HashMap<String, CmsRepositoryEntity>();
			}

			if (currentChildPropertyDefinition instanceof ComplexCmsPropertyDefinition)
				return renderComplexProperty(currentChildPropertyDefinition.getName(), propertyContainerNode, currentChildPropertyDefinition);
			else{
				CmsProperty<?, ?> simpleCmsProperty = renderSimpleProperty(currentChildPropertyDefinition.getName(),
						currentChildPropertyDefinition, propertyContainerNode, 
						session, jcrNodeUUIDWhichCorrespondsToContentObejct, 
						cachedCmsRepositoryEntities, renderProperties);

				List<CmsProperty<?,?>> childCmsProperties = new ArrayList<CmsProperty<?,?>>();

				if (simpleCmsProperty != null){
					childCmsProperties.add(simpleCmsProperty);
				}

				return childCmsProperties;
			}

		}
		catch (Exception e)
		{
			logger.error("While trying to lazy render child property {} with property container jcr node UUID {} " +
					" and content object jcr node UUID {}", new Object[]{currentChildPropertyDefinition.getFullPath(),
							jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty,
							jcrNodeUUIDWhichCorrespondsToContentObejct});
			throw new CmsException(e);
		}
	}

	private CmsProperty createNewCmsProperty(CmsPropertyDefinition propertyDefinition,String propertyName) {
		CmsProperty newProperty = newCmsProperty(propertyDefinition.getValueType());

		if (newProperty instanceof SimpleCmsProperty)
			((SimpleCmsProperty)newProperty).setPropertyDefinition((SimpleCmsPropertyDefinition)propertyDefinition);
		else if (newProperty instanceof ComplexCmsProperty){
			//In case definition refers to its parent
			//all its children must be initialized
			((ComplexCmsPropertyDefinitionImpl)propertyDefinition).checkIfRecursiveAndCloneParentChildDefinitions();
			((ComplexCmsProperty)newProperty).setPropertyDefinition((ComplexCmsPropertyDefinition)propertyDefinition);
		}

		return newProperty;
	}

	private CmsProperty<?,?> newCmsProperty(ValueType valueType) {
		
		CmsProperty newProperty = null;
		
		switch (valueType) {
		case Binary:
			newProperty =  new BinaryPropertyImpl();
			break;
		case Boolean:
			newProperty = new BooleanPropertyImpl();
			break;
		case Date:
			newProperty = new CalendarPropertyImpl();
			break;
		case Complex:{
			newProperty = new ComplexCmsPropertyImpl();
			break;
		}
		case ObjectReference:
			newProperty = new ObjectReferencePropertyImpl();
			break;
		case Double:
			newProperty = new DoublePropertyImpl();
			break;
		case Long:
			newProperty = new LongPropertyImpl();
			break;
		case String:
			newProperty = new StringPropertyImpl();
			break;
		case TopicReference:
			newProperty = new TopicReferencePropertyImpl();
			break;

		default:
			return null;
		}
		
		if (newProperty != null){
			((CmsRepositoryEntityImpl)newProperty).setAuthenticationToken((AstroboaClientContextHolder.getActiveClientContext() != null ? 
					AstroboaClientContextHolder.getActiveClientContext().getAuthenticationToken() : null));
		}
		
		return newProperty;
	}

	private CmsProperty<?,?> renderSimpleProperty(String childPropertyName, CmsPropertyDefinition currentChildPropertyDefinition, Node propertyContainerNode, Session session, String contentObjectNodeUUID, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties) throws RepositoryException {

		CmsProperty<?,?> simpleProperty = createNewCmsProperty(currentChildPropertyDefinition, childPropertyName);

		//Render values
		if (simpleProperty != null){
			if (currentChildPropertyDefinition instanceof BinaryPropertyDefinition) {
				renderBinaryChannels((BinaryProperty)simpleProperty,propertyContainerNode, 
						cachedCmsRepositoryEntities, session,renderProperties);
			}
			else{
				renderValueForSimpleProperty((SimpleCmsProperty<?,?,?>)simpleProperty,session, contentObjectNodeUUID, propertyContainerNode, cachedCmsRepositoryEntities, renderProperties);
			}
		}

		return simpleProperty;
	}

	private void renderValueForSimpleProperty(SimpleCmsProperty<?,?,?> simpleContentObjectProperty, Session session, String contentObjectNodeUUID, 
			Node propertyContainerNode, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties) throws RepositoryException {

		final String propertyName = simpleContentObjectProperty.getName();

		//Special case. In order to render versions and hasVersion we need
		//content object to look in VersionHistory
		if ( (CmsReadOnlyItem.Versions.getJcrName().equals(propertyName) ||	CmsReadOnlyItem.HasVersion.getJcrName().equals(propertyName))){

			Node contentObjectNode = null;

			try{
				//This is meaningful only if contentObject UUID is provided
				if (StringUtils.isNotBlank(contentObjectNodeUUID)){
					contentObjectNode = session.getNodeByIdentifier(contentObjectNodeUUID);

					if (contentObjectNode != null){
						renderVersionNames(simpleContentObjectProperty, contentObjectNode, session);
						return;
					}
					else{
						throw new Exception("Null content object node");
					}
				}
			}
			catch(Exception e){
				logger.warn("Could not render property '"+propertyName+ "' Property Container Node : "+ propertyContainerNode.getPath() +
						" Content Object Node :" + (contentObjectNode != null ? contentObjectNode.getPath() : " no content object node ")+
						" Content object node UUID "+contentObjectNodeUUID, e);
			}

		}
		else if (propertyContainerNode == null || !propertyContainerNode.hasProperty(propertyName)){
			//Check if property is Mandatory
			SimpleCmsPropertyDefinition<?> propertyDefinition = (SimpleCmsPropertyDefinition<?>) simpleContentObjectProperty.getPropertyDefinition();

			//Issue a warning only if there is a container node
			//In cases where content object is new there is no property container node yet
			//therefore warning is misleading
			if (propertyDefinition.isMandatory()){ 
					
				if (propertyContainerNode != null){
					logger.warn("Mandatory property {} does not exist for content object {}", propertyDefinition.getFullPath(), contentObjectNodeUUID);
				}
				//Return default value
				//Property does not exist, it is mandatory and therefore render its default value
				/*
				 * Default value is not set at all during read
				 * It is automatically set upon save or update
				 * 	only when property is mandatory
				
				if (propertyDefinition.getDefaultValue() != null){
					renderSimpleValue(simpleContentObjectProperty, propertyDefinition.getDefaultValue(),session, cachedCmsRepositoryEntities, locale, renderProperties);
				}
				*/
			}

		}
		else{
			renderSimpleCmsProperty(simpleContentObjectProperty, propertyContainerNode.getProperty(propertyName),cachedCmsRepositoryEntities,session, renderProperties);
		}
	}


	private void renderBinaryChannels(BinaryProperty binaryProperty, Node propertyContainerNode, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, RenderProperties renderProperties) throws  RepositoryException   {

		String propertyName = binaryProperty.getName();

		//Binary channels are sub nodes of property node
		if (propertyContainerNode == null || ( 
				(!propertyContainerNode.hasNode(propertyName) 
					&& !propertyContainerNode.hasProperty(propertyName))) //it may be the case that binary channel is unmanaged, thus it is stored as a jcr property
			){ 
				//Issue a warning only if there is a container node
				//In cases where content object is new there is no property container node yet
				//therefore warning is misleading
				if (binaryProperty.getPropertyDefinition().isMandatory() && propertyContainerNode != null){
					logger.warn("Mandatory property {} does not exist", propertyName);
				}
			}
		else{
			
			if (propertyContainerNode.hasProperty(propertyName)){
				//Binary Channel is unmanaged. Only relative system paths are stored as values 
				//of a jcr property
				
				//Check that binary property's definition states that binary property must contain
				//unmanaged binary channels
				if (binaryProperty.getPropertyDefinition() == null ||
						! binaryProperty.getPropertyDefinition().isBinaryChannelUnmanaged()){
					logger.warn("Property {} is not defined as unmanaged binary property but only relative path(s) have been found");
				}
				else{
					
					renderSimpleCmsProperty(binaryProperty, propertyContainerNode.getProperty(propertyName), 
							cachedCmsRepositoryEntities, session, renderProperties);
				}
			}
			else{
				NodeIterator nodeIter = propertyContainerNode.getNodes(propertyName);

				while (nodeIter.hasNext())
				{
					Node node = nodeIter.nextNode();
					if (node.isNodeType(CmsBuiltInItem.BinaryChannel.getJcrName()) ||
							//If it is a frozen Node 
							//it must have a property named jcr:frozenPrimaryType whose
							//value must be CmsBuiltInItem.BinaryChannel.getJcrName()
							(node.isNodeType(JcrBuiltInItem.NtFrozenNode.getJcrName()) && 
									node.hasProperty(JcrBuiltInItem.JcrFrozenPrimaryType.getJcrName()) && 
									node.getProperty(JcrBuiltInItem.JcrFrozenPrimaryType.getJcrName()).getString().equals(CmsBuiltInItem.BinaryChannel.getJcrName()))) 
						binaryProperty.addSimpleTypeValue(binaryChannelRenderer.render(node, false));
					else
						logger.warn("Binary channel {} exists in content object node {} with type other than {} and that is {}", 
								new Object[]{propertyName, propertyContainerNode.getPath(), CmsBuiltInItem.BinaryChannel.getJcrName(),node.getPrimaryNodeType().getName()});
				}
			}
		}
	}


	private List<CmsProperty<?,?>> renderComplexProperty(String childPropertyName, Node propertyContainerNode, CmsPropertyDefinition currentChildPropertyDefinition) throws RepositoryException {

		List<CmsProperty<?,?>> childCmsProperties = new ArrayList<CmsProperty<?,?>>();

		if (propertyContainerNode == null || !propertyContainerNode.hasNode(childPropertyName)){
			//No complex property node exist in repository
			//If mandatory create a new complex property and issue a warning
			//Issue a warning only if there is a container node
			//In cases where content object is new there is no property container node yet
			//therefore warning is misleading
			if (currentChildPropertyDefinition.isMandatory() && propertyContainerNode != null) {
				logger.warn("Mandatory property {} does not exist in repository ", childPropertyName);
			}

			//Create an empty property
			childCmsProperties.add(createNewCmsProperty(currentChildPropertyDefinition, childPropertyName));

		}
		else
		{
			NodeIterator complexNodes = propertyContainerNode.getNodes(childPropertyName);

			if (complexNodes.getSize() > 1 && !currentChildPropertyDefinition.isMultiple())
				throw new CmsException("ComplexCmsProperty '"+ currentChildPropertyDefinition.getName() + "' is single value but there are more " +
						" than one nodes in "+ propertyContainerNode.getPath());


			Map<Integer,CmsProperty<?, ?>> propertiesPerOrder = new TreeMap<Integer, CmsProperty<?,?>>();
			
			int unknownIndex = 10000;
			
			while (complexNodes.hasNext())
			{
				//Locate node for property
				Node nodeOfComplexProperty  = complexNodes.nextNode();

				//Create new CmsProperty
				CmsProperty<?,?> newCmsProperty = createNewCmsProperty(currentChildPropertyDefinition, childPropertyName); 

				if (newCmsProperty instanceof LazyCmsProperty){
					((LazyCmsProperty)newCmsProperty).setPropertyContainerNodeUUID(nodeOfComplexProperty.getIdentifier());
				}

				//Render Complex Property Id
				if (!cmsRepositoryEntityUtils.hasCmsIdentifier(nodeOfComplexProperty)){
					throw new CmsException("Found no id for complex property "+ nodeOfComplexProperty.getPath());
				}

				//Render Id
				cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(nodeOfComplexProperty, newCmsProperty);

				if (nodeOfComplexProperty.hasProperty(CmsBuiltInItem.Order.getJcrName()))
				{
					try{
						
						int index = (int)nodeOfComplexProperty.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong() - 1;
					
						if (propertiesPerOrder.containsKey(index))
						{
							propertiesPerOrder.put(unknownIndex++,newCmsProperty);
						}
						else
						{
							propertiesPerOrder.put(index,newCmsProperty);
						}
						
					}
					catch(Exception e)
					{
						logger.warn("Node "+nodeOfComplexProperty.getPath()+" did not have a valid order value and therefore corresponding cms property will be added at the end of the list", 
								e);
						
						propertiesPerOrder.put(unknownIndex++,newCmsProperty);
					}
				}
				else
				{
					propertiesPerOrder.put(unknownIndex++,newCmsProperty);
				}
			}
			
			childCmsProperties.addAll(propertiesPerOrder.values());
			
			
		}
		
		

		return childCmsProperties;

	}


	private  void renderVersionNames(SimpleCmsProperty<?,?,?> simpleProperty, Node contentObjectNode, Session session) throws RepositoryException {
		VersionHistory versioningHistory = null;

		//Render is about an archived content object, therefore node uuid is under jcr:frozenUUID
		if (contentObjectNode.hasProperty(JcrBuiltInItem.JcrFrozenUUID.getJcrName()))
			versioningHistory = versionUtils.getVersionHistoryForNode(session, cmsRepositoryEntityUtils.getCmsIdentifier(contentObjectNode));
		else
			versioningHistory = session.getWorkspace().getVersionManager().getVersionHistory(contentObjectNode.getPath());

		if (versioningHistory != null)
		{
			VersionIterator versIter = versioningHistory.getAllVersions();
			while (versIter.hasNext())
			{
				Version currentVersion = versIter.nextVersion();

				String versionName = currentVersion.getName();
				if (! versionName.equals(JcrBuiltInItem.JcrRootVersion.getJcrName()))
				{
					//Version with no successors is the base version
					Version[] successors = currentVersion.getSuccessors();

					//RenderHasVersion
					//Current version does not have successors
					if (ArrayUtils.isEmpty(successors))
					{
						if (CmsReadOnlyItem.HasVersion.getJcrName().equals(simpleProperty.getName()))
							renderSimpleValue(simpleProperty, versionName,session, null, null);

					}

					//Render versionName
					if (CmsReadOnlyItem.Versions.getJcrName().equals(simpleProperty.getName()))
					{
						renderSimpleValue(simpleProperty, versionName,session, null, null);
					}
				}
			}
		}
	}

	private  void renderSimpleCmsProperty(SimpleCmsProperty<?,?,?> simpleProperty , Property property, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, RenderProperties renderProperties) throws  RepositoryException   {

		SimpleCmsPropertyDefinition<?> propertyDefinition = (SimpleCmsPropertyDefinition<?>) simpleProperty.getPropertyDefinition();

		//Gather all Value from repository in a list
		//regardless if property is multiple or not
		List<Value> values = new ArrayList<Value>();

		if (propertyDefinition.isMultiple()){
			//It may be the case that property used to be single value.
			//if so an exception is thrown by JCR. 
			//we must check in order to avoid it
			if (property.getDefinition() != null && ! property.getDefinition().isMultiple()){
				values.add(property.getValue());
			}
			else{
				values.addAll(Arrays.asList(property.getValues()));
			}
		}
		else
			values.add(property.getValue());

		setValuesToSimpleProperty(simpleProperty, propertyDefinition.getValueType(), values, cachedCmsRepositoryEntities, session, renderProperties);
	}


	private void setValuesToSimpleProperty(SimpleCmsProperty simpleProperty, ValueType definitionValueType, List<Value> values, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, RenderProperties renderProperties) throws RepositoryException {
		switch (definitionValueType) {
		case Boolean:
			for (Value value : values){
				simpleProperty.addSimpleTypeValue(value.getBoolean());
			}
			break;
		case String:	
			for (Value value : values){
				try{
					simpleProperty.addSimpleTypeValue(value.getString());
				}
				catch(Exception e){
					
					//Backwards compatibility. There was a bug no check was made
					//when entering plain string values. If value is invalid because entered
					//value has more characters than maxLegth, provide maxLength characters of this value 
					//
					if (value.getString() != null && 
						simpleProperty.getPropertyDefinition() != null && 
						((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength() != null &&
						((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength() > 0 && 
						((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength() < value.getString().length()){
						
						logger.warn("Value "+value.getString()+ " contains more characters "+value.getString().length()+" than allowed " +
								((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength()+" . Only the first "+
								((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength()+ " characters will be displayed", e);
						
						simpleProperty.addSimpleTypeValue(value.getString().substring(0, ((StringPropertyDefinition)simpleProperty.getPropertyDefinition()).getMaxLength()));
						
					}
					else{
						logger.warn("Value "+value.getString()+ " is probably invalid. It will not be added to property "+simpleProperty.getFullPath()+
							" This value will remain in repository until property is resaved with different value(s). This error may happen if " +
							"value constraints have been applied recently to property.", e);
					}
				}
			}
			break;
		case Date:
			for (Value value : values){
				try{
					simpleProperty.addSimpleTypeValue(value.getDate());
				}
				catch(Exception e){
					logger.warn("Value "+DateUtils.format(value.getDate(), "dd/MM/yyy HH:mm")+ " is probably invalid. It will not be added to property "+simpleProperty.getFullPath()+
							" This value will remain in repository until property is resaved with different value(s). This error may happen if " +
							"value constraints have been applied recently to property.", e);
				}
			}
			break;
		case Double:
			for (Value value : values){
				try{
					simpleProperty.addSimpleTypeValue(value.getDouble());
				}
				catch(Exception e){
					logger.warn("Value "+value.getDouble()+ " is probably invalid. It will not be added to property "+simpleProperty.getFullPath()+
							" This value will remain in repository until property is resaved with different value(s). This error may happen if " +
							"value constraints have been applied recently to property.", e);
				}
			}
			break;
		case Long:
			for (Value value : values){
				try{
					simpleProperty.addSimpleTypeValue(value.getLong());
				}
				catch(Exception e){
					logger.warn("Value "+value.getLong()+ " is probably invalid. It will not be added to property "+simpleProperty.getFullPath()+
							" This value will remain in repository until property is resaved with different value(s). This error may happen if " +
							"value constraints have been applied recently to property.", e);
				}
			}
			break;
		case ObjectReference:
			renderContentObject((ObjectReferenceProperty)simpleProperty, values,cachedCmsRepositoryEntities, session, renderProperties);
			break;
		case TopicReference: 
			renderTopic(values, session, cachedCmsRepositoryEntities, renderProperties, simpleProperty);
			break;
		case Binary:
			for (Value value : values)
				simpleProperty.addSimpleTypeValue(binaryChannelRenderer.renderUnmanagedBinaryChannel(simpleProperty.getName(), 
						value.getString()));
			break;

		default:
			break;
		}
	}

	private void renderTopic(List<Value> values, Session session, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties, SimpleCmsProperty simpleProperty) throws   RepositoryException {

		for (Value topicIdAsValue : values)
		{	
			//Do not render Topic if it has already been rendered
			Topic topic = null;
			String topicIdAsString = topicIdAsValue.getString();
			if (cachedCmsRepositoryEntities.containsKey(topicIdAsString))
				topic = (Topic) cachedCmsRepositoryEntities.get(topicIdAsString);
			else
			{

				try{
					topic = topicRenderer.renderTopic(topicIdAsString,renderProperties, session, cachedCmsRepositoryEntities);				

					if (!cachedCmsRepositoryEntities.containsKey(topicIdAsString))
						cachedCmsRepositoryEntities.put(topicIdAsString, topic);
				}
				catch(Exception e){
					logger.warn("Unable to render topic with id "+topicIdAsString + " for content object property "+ simpleProperty.getFullPath());
				}

			}

			simpleProperty.addSimpleTypeValue(topic);
		}

	}

	private void renderContentObject(ObjectReferenceProperty contentObjectProperty, List<Value> values, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, RenderProperties renderProperties) throws RepositoryException {

		Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions = new HashMap<String, ContentObjectTypeDefinition>();

		//Disable Full rendering for ContentObjectReferences 
		boolean fullRenderIsEnabled = renderProperties != null &&	renderProperties.allContentObjectPropertiesAreRendered();

		if (fullRenderIsEnabled){
			renderProperties.renderAllContentObjectProperties(false);
		}

		for (Value contentObjectIdValue : values){
			ContentObject contentObject = null;
			String contentObjectIdAsString = contentObjectIdValue.getString();  

			if (cachedCmsRepositoryEntities.containsKey(contentObjectIdAsString)){
				contentObject = (ContentObject)cachedCmsRepositoryEntities.get(contentObjectIdAsString);
			}
			else{
				Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectIdAsString);
				
				if (contentObjectNode == null){
					logger.warn("Content object with id {} does not exist in repository. Value found in property {} and will not be rendered",
							contentObjectIdAsString, contentObjectProperty.getFullPath());
				}
				else{
					
					
					contentObject = contentObjectRenderer.render(session, contentObjectNode, renderProperties, 
						cachedContentObjectTypeDefinitions, cachedCmsRepositoryEntities);

					cachedCmsRepositoryEntities.put(contentObjectIdAsString, contentObject);
				}
			}

			contentObjectProperty.addSimpleTypeValue(contentObject);
		}
		
		if (fullRenderIsEnabled){
			renderProperties.renderAllContentObjectProperties(true);
		}
	}

	private void renderSimpleValue(SimpleCmsProperty simpleProperty, Object value, Session session, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties) throws RepositoryException  {
		if (value == null)
			simpleProperty.addSimpleTypeValue(null);
		else
		{
			final Value jcrValue = JcrValueUtils.getJcrValue(value, simpleProperty.getValueType(), session.getValueFactory());

			setValuesToSimpleProperty(simpleProperty, simpleProperty.getValueType(), Arrays.asList(jcrValue), cachedCmsRepositoryEntities, session, renderProperties);

		}
	}

	/*
	 * Checks to see if there is a value for the provided property path
	 */
	public boolean valueForPropertyExists(String property,
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, Session session) {

		 PropertyPath propertyPath = new PropertyPath(property);
		 
		if (property == null || propertyPath.getPropertyName() == null){
				logger.warn("No property path is provided for parent node UUID {}. Do not know " +
						"which child property to check", 
						jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
				return false;
			}
			
			if (StringUtils.isBlank(jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty)){
				//Since no parent exists, no need to check for child
				if (logger.isDebugEnabled()){
					logger.debug("No id for parent has been provided. No need to check for existence of child property {} ",
						propertyPath.getFullPath());
				}
				
				return false;
			}

			if (logger.isDebugEnabled()){
				logger.debug("Checking property {} existence",	propertyPath.getFullPath());
			}

			//Load property container node if a UUID is provided
			//Otherwise a blank template for this child property will be created
			try{
				Node propertyContainerNode = session.getNodeByIdentifier(jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
				
				return propertyPathContainsValue(propertyPath,	propertyContainerNode);
				
			}
			catch (Exception e){
				logger.warn("Could not locate parent node with UUID "+jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty+
						". Cannot check value existence for property "+ propertyPath.getFullPath(), e);
				return false;
			}
	}

	private boolean propertyPathContainsValue(PropertyPath propertyPath,
			Node propertyContainerNode) throws PathNotFoundException,
			ValueFormatException, RepositoryException {
		
		String propertyName = propertyPath.getPropertyName();
		int index = propertyPath.getPropertyIndex();
		
		if (propertyPath.getPropertyDescendantPath() != null){
			//This property is a complex one.
			//Find it and proceed with the rest of the path
			Node nodeRepresentingProperty = findNodeForProperty(propertyContainerNode, propertyName, index);
			
			return propertyPathContainsValue(new PropertyPath(propertyPath.getPropertyDescendantPath()), nodeRepresentingProperty);
		}
		else{
			//Final path part. Property is either a Jcr property or a jcr node 
			return valueExists(propertyContainerNode, propertyName, index);
		}
	}


	/**
	 * @param propertyContainerNode
	 * @param propertyName
	 * @param index
	 * @return
	 * @throws RepositoryException 
	 * @throws ValueFormatException 
	 * @throws PathNotFoundException 
	 */
	private boolean valueExists(Node propertyContainerNode, String propertyName, int index) throws PathNotFoundException, ValueFormatException, RepositoryException {
		
		//Check jcr properties
		if (propertyContainerNode!= null && propertyContainerNode.hasProperty(propertyName)){
			if (index >-1){
				Property property = propertyContainerNode.getProperty(propertyName);
				
				boolean multivalue = property.getDefinition().isMultiple();
				
				int sizeOfValues = 1;
				
				if (multivalue){
					sizeOfValues = property.getValues().length;
				}
				
				return (index+1) <= sizeOfValues;
				
			}
			else {
				return true;
			}
		}
		else {
			return findNodeForProperty(propertyContainerNode, propertyName, index) != null;
		}
	}

	/**
	 * @param propertyContainerNode
	 * @param propertyName
	 * @param index
	 * @return
	 */
	private Node findNodeForProperty(Node parentNode,String propertyName, int index) {
		
		try {
			if (parentNode == null || ! parentNode.hasNode(propertyName)){
				return null;
			}
			
			NodeIterator nodesRepresentingProperty = parentNode.getNodes(propertyName);
			
			if (nodesRepresentingProperty.getSize() == 1){
				//Only one node exists
				if (index <=0){
					//User has not specified index or index is 0 (zero based)
					return nodesRepresentingProperty.nextNode();
				}
				else{
					//Index provided by the user does not exist
					if (logger.isDebugEnabled()){
						logger.debug("Index {} for property {} in parent property {} does not exist", new Object[]{index, propertyName, parentNode.getPath()});
					}
					return null;
				}
			}
			else{
				//Due to the requirement of keeping complex property's index inside a specific jcr property
				//we have to search all nodes to find a match for the provided index.
				//A negative index is equivalent to 0, that is the first item
				if (index <0){
					index = 0;
				}
				
				boolean atLeastOneNodeFoundWithOrderProperty = false;
				
				Node nodeOfComplexPropertyWhosePositionMatchesIndex = null;
				
				while (nodesRepresentingProperty.hasNext()){
					Node nodeOfComplexProperty = nodesRepresentingProperty.nextNode();
					long position = nodesRepresentingProperty.getPosition();
					
					if (nodeOfComplexProperty.hasProperty(CmsBuiltInItem.Order.getJcrName())){

						atLeastOneNodeFoundWithOrderProperty = true;
						
						int complexPropertyIndex = (int)nodeOfComplexProperty.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong();
						
						if (complexPropertyIndex==index){
							return nodeOfComplexProperty;
						}
					}
					
					if ((int)position == index){
						nodeOfComplexPropertyWhosePositionMatchesIndex = nodeOfComplexProperty;
					}
				}
				
				if (!atLeastOneNodeFoundWithOrderProperty){
					return nodeOfComplexPropertyWhosePositionMatchesIndex;
				}
				else{
					if (nodeOfComplexPropertyWhosePositionMatchesIndex != null){
						//Some or all of the complex nodes contained Order property but none of them matched index.
						//Nevertheless there is a node whose position matched the index (although its Order property either 
						//does not exist or does not match the index
						//Report this as a warning and return value
						long indexValueOfNodeOfComplexPropertyWhosePositionMatchesIndex = -1;
						if (nodeOfComplexPropertyWhosePositionMatchesIndex.hasProperty(CmsBuiltInItem.Order.getJcrName())){
							indexValueOfNodeOfComplexPropertyWhosePositionMatchesIndex = nodeOfComplexPropertyWhosePositionMatchesIndex.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong();
						}
						
						PropertyPath requestedPath = new PropertyPath(parentNode.getPath()+"."+propertyName);
						requestedPath.setPropertyIndex(index);
						
						logger.warn("Requested node for path {}. Found node {} whose position matched requested index {} but it {}.", 
								new Object[]{requestedPath.getFullPath(), nodeOfComplexPropertyWhosePositionMatchesIndex, index, 
								 (indexValueOfNodeOfComplexPropertyWhosePositionMatchesIndex == -1 ? " does not contain an 'order' property. However this node will be returned" 
										 : " contains an 'order' property with value "+indexValueOfNodeOfComplexPropertyWhosePositionMatchesIndex+ " instead. No node will be returned" )});
						if (indexValueOfNodeOfComplexPropertyWhosePositionMatchesIndex == -1){
							return nodeOfComplexPropertyWhosePositionMatchesIndex;
						}
						
					}
					
					return null;
				}
			}
		} catch (RepositoryException e) {
			logger.error("", e);
			return null;
		}
		
		
		
	}
}
