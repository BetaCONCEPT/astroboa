/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.io.contenthandler.ExportContentHandler;
import org.betaconceptframework.astroboa.engine.jcr.io.contenthandler.JsonExportContentHandler;
import org.betaconceptframework.astroboa.engine.jcr.io.contenthandler.XmlExportContentHandler;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JackrabbitDependentUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.io.SerializationReportImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrNamespaceConstants;
import org.betaconceptframework.astroboa.model.jaxb.MarshalUtils;
import org.betaconceptframework.astroboa.service.dao.DefinitionServiceDao;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.astroboa.util.ResourceApiURLUtils;
import org.betaconceptframework.astroboa.util.UrlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Class responsible to serialize JCR nodes according to 
 * Astroboa content model.
 * 
 * It mimics the behavior of JCR exporter but it traverses
 * jcr nodes and properties according to Astroboa content model.
 * 
 * It uses {@link XmlExportContentHandler} to directly write xml content
 * instead of first constructing attributes list and then passing them
 * to ContentHandler
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class Serializer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ExportContentHandler exportContentHandler;

	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	private List<String> processedCmsRepositoryEntityIdentifiers = new ArrayList<String>();

	private final static String BCCMS_PREFIX_WITH_SEMICOLON = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+CmsConstants.QNAME_PREFIX_SEPARATOR;

	private static final String NT_PREFIX_WITH_SEMI_COLON = JcrNamespaceConstants.NT_PREFIX+CmsConstants.QNAME_PREFIX_SEPARATOR;

	private static final String JCR_MIX_PREFIX_WITH_SEMI_COLON = JcrNamespaceConstants.MIX_PREFIX+CmsConstants.QNAME_PREFIX_SEPARATOR;

	private static final String JCR_PREFIX_WITH_SEMI_COLON = JcrNamespaceConstants.JCR_PREFIX+CmsConstants.QNAME_PREFIX_SEPARATOR;
	
	private DatatypeFactory df ;

	private Map<String, String> prefixesPerType;

	private SerializationReport serializationReport;

	private String qNameOfRootElement = null;

	private Session session;

	private boolean serializeBinaryContent;

	private DefinitionServiceDao definitionServiceDao;

	private Deque<LocalizableCmsDefinition> parentPropertyDefinitionQueue = new ArrayDeque<LocalizableCmsDefinition>();

	private List<String> propertyPathsWhoseValuesWillBeIncludedInTheSerialization = new ArrayList<String>();

	private List<String> propertyPathsWhoseValuesWillBeIncludedInTheSerializationOfObjectReferences = Arrays.asList("profile.title");
	
	private boolean objectReferenceIsSerialized = false;
	
	private AttributesImpl rootElementAttributes;
	
	private boolean useTheSameNameForAllObjects;

	private enum NodeType{
		Taxonomy, 
		ToBeIgnored,
		ContentObject,
		Other,
		Topic, Space
	}

	private ResourceRepresentationType<?>  resourceRepresentationType;


	public Serializer(OutputStream out, CmsRepositoryEntityUtils cmsRepositoryEntityUtils, Session session, ResourceRepresentationType<?>  resourceRepresentationType, boolean prettyPrint) throws Exception{

		this.resourceRepresentationType = resourceRepresentationType;

		createNewExportContentHandler(out,prettyPrint);

		this.cmsRepositoryEntityUtils = cmsRepositoryEntityUtils;

		this.session = session;

		if (this.session == null){
			throw new CmsException("Cannot initialize serializer because no JCR session has been provided");
		}
		
		if (df == null){
			try {
				df = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new CmsException(e);
			}
		}
	}

	private void createNewExportContentHandler(OutputStream out, boolean prettyPrint) throws IOException {

		if (resourceRepresentationType == null || ResourceRepresentationType.XML.equals(resourceRepresentationType)){
			exportContentHandler = new XmlExportContentHandler(out,prettyPrint);
		}
		else if (ResourceRepresentationType.JSON.equals(resourceRepresentationType)){
			exportContentHandler = new JsonExportContentHandler(out, true, prettyPrint);
		}
		else{
			logger.warn("Resource Representation {} is not valid within export context. Export to XML is chosen", resourceRepresentationType);
			exportContentHandler = new XmlExportContentHandler(out,prettyPrint);
			resourceRepresentationType = ResourceRepresentationType.XML;
		}
	}

	/**
	 * Node to be serialized represents a root node, usually root node of all taxonomies, or root node of all users
	 * or root node of all content objects and this method will serialize all its children nodes.
	 * @param node
	 * @throws Exception
	 */
	public void serializeChildrenOfRootNode(Node node) throws Exception{
		serializeNode(node, false, FetchLevel.FULL, false, false);
	}

	/**
	 * Node to be serialized represents a resource item in a collection
	 * @param node
	 * @param shouldVisitChildren(fetchLevel)
	 * @param nodeRepresentsResourceCollectionItem
	 * @throws Exception
	 */
	public void serializeResourceCollectionItem(Node node, FetchLevel fetchLevel, boolean nodeRepresentsRootElement) throws Exception{
		serializeNode(node, false, fetchLevel, true, nodeRepresentsRootElement);
	}

	public void end() throws Exception {
		exportContentHandler.end();
	}

	public void start(AttributesImpl rootElementAttributes) throws Exception {
		exportContentHandler.start();
		this.rootElementAttributes = rootElementAttributes;
	}

	public void start() throws Exception {
		exportContentHandler.start();
	}

	private void serializeNode(Node node, boolean nodeRepresentsCmsProperty, FetchLevel fetchLevel, boolean nodeRepresentsResourceCollectionItem, boolean nodeRepresentsRootElement) throws Exception {

		if (logger.isDebugEnabled()){
			logger.debug("Serializing node {}", node.getPath());
		}
		
		NodeType nodeType = determineNodeType(node);

		switch (nodeType) {
		case ToBeIgnored:

			serializeChildNodes(node, false, fetchLevel) ;

			break;
		case ContentObject:

			serializeContentObjectNode(node, nodeRepresentsResourceCollectionItem);

			break;
		case Taxonomy:

			serializeTaxonomyNode(node, fetchLevel, nodeRepresentsResourceCollectionItem, nodeRepresentsRootElement);

			break;
		case Topic:
			serializeTopicNode(node, fetchLevel, true, nodeRepresentsResourceCollectionItem, nodeRepresentsRootElement, false);

			break;
		case Space:
			serializeSpaceNode(node, fetchLevel, nodeRepresentsResourceCollectionItem, nodeRepresentsRootElement, false);

			break;

		default:

			serializeCustomNode(node, nodeRepresentsCmsProperty, false);

			break;
		}

	}

	public void serializeSpaceNode(Node node, FetchLevel fetchLevel, boolean nodeRepresentsResourceCollectionItem, boolean nodeRepresentsRootElement, boolean parentSpaceNode) throws Exception{

		String spaceQName = CmsBuiltInItem.Space.getLocalPart();

		if (CmsBuiltInItem.OrganizationSpace.getJcrName().equals(node.getName())){
			spaceQName = CmsBuiltInItem.OrganizationSpace.getLocalPart();
		}

		if (nodeRepresentsRootElement){
			spaceQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+spaceQName;
		}
		else if (nodeRepresentsResourceCollectionItem){
			if (outputIsJSON()){
				//When serializing a collection of resources
				//the name of the element which represents the space
				//is CmsConstants.RESOURCE when output is JSON 
				spaceQName = CmsConstants.RESOURCE;
			}
			else{
				//Node represents a space which appears
				//as a root child of a resource representation element.
				//In XML corresponding element should be prefixed
				//as its parent (resourceCollection) does not share the
				//same namespace
				spaceQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+ spaceQName;
			}
		}
		else if (parentSpaceNode){
			spaceQName = CmsConstants.PARENT_SPACE;
		}


		String spaceCmsIdentifier = retrieveCmsIdentifier(node);

		if (cmsIdentifierAlreadyProcessed(spaceCmsIdentifier)){

			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(spaceQName, spaceCmsIdentifier);

			if (node.hasProperty(CmsBuiltInItem.Name.getJcrName())){

				writeAttribute(CmsBuiltInItem.Name.getLocalPart(), 
						node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
			}

			addUrlForEntityRepresentedByNode(node);
			
			processLocalization(node);


		}
		else{
			startedNodeSerialization(spaceQName);

			openEntityWithAttribute(spaceQName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), spaceCmsIdentifier);

			markCmsIdentifierProcessed(spaceCmsIdentifier);

			serializeBuiltInProperties(node, parentSpaceNode);

			if (! parentSpaceNode && node.getParent().isNodeType(CmsBuiltInItem.Space.getJcrName())){
				serializeSpaceNode(node.getParent(), FetchLevel.ENTITY, false, false, true);
			}

			if (! parentSpaceNode && shouldVisitChildren(fetchLevel) && node.hasNode(CmsBuiltInItem.Space.getJcrName())){
				NodeIterator childSpaceNodes = node.getNodes(CmsBuiltInItem.Space.getJcrName());

				if (childSpaceNodes.getSize() > 0){

					FetchLevel visitDepthForChildSpaces = fetchLevel == FetchLevel.FULL ? FetchLevel.FULL : FetchLevel.ENTITY;

					openEntityWithNoAttributes(CmsConstants.CHILD_SPACES);

					while (childSpaceNodes.hasNext()){
						serializeSpaceNode(childSpaceNodes.nextNode(), visitDepthForChildSpaces , false, false, false);
					}

					closeEntity(CmsConstants.CHILD_SPACES);
				}
			}
		}

		closeEntity(spaceQName);

		finishedNodeSerialization(CmsEntityType.SPACE, spaceQName);

	}

	public void serializeTopicNode(Node node, FetchLevel fetchLevel, boolean serializeTaxonomyNode, boolean nodeRepresentsResourceCollectionItem, boolean nodeRepresentsRootElement, boolean parentTopicNode) throws Exception{

		String topicCmsIdentifier = retrieveCmsIdentifier(node);

		String topicQName = CmsBuiltInItem.Topic.getLocalPart();

		if (nodeRepresentsRootElement){
			topicQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+ CmsBuiltInItem.Topic.getLocalPart();
		}
		else if (nodeRepresentsResourceCollectionItem){

			if (outputIsJSON()){
				//When serializing a collection of resources
				//the name of the element which represents the topic
				//is CmsConstants.RESOURCE when output is JSON 
				topicQName = CmsConstants.RESOURCE;
			}
			else{
				//Node represents a topic which appears
				//as a root child of a resource representation element.
				//In XML corresponding element should be prefixed
				//as its parent (resourceCollection) does not share the
				//same namespace
				topicQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+ CmsBuiltInItem.Topic.getLocalPart();
			}
		}
		else if (parentTopicNode){
			topicQName = CmsConstants.PARENT_TOPIC;
		}

		if (cmsIdentifierAlreadyProcessed(topicCmsIdentifier)){

			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(topicQName, topicCmsIdentifier);

			serializeBasicTopicInformation(node);
		}
		else{
			startedNodeSerialization(topicQName);

			openEntityWithAttribute(topicQName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), topicCmsIdentifier);

			markCmsIdentifierProcessed(topicCmsIdentifier);

			serializeBuiltInProperties(node, parentTopicNode);

			if (serializeTaxonomyNode){
				try{
					Node taxonomyNode = JcrNodeUtils.getTaxonomyJcrNode(node.getParent(), false);

					if (taxonomyNode != null){
						serializeTaxonomyNode(taxonomyNode, FetchLevel.ENTITY, false, false);
					}
					else{
						logger.warn("Unable to serialize taxonomy for topic {}",node.getPath());
					}
				}
				catch(Exception e){
					logger.warn("Unable to serialize taxonomy for topic "+ node.getPath(), e);
				}
			}

			//Serialize Parent Node if parent node is not a taxonomy
			if (! parentTopicNode && ! node.getParent().isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
				serializeTopicNode(node.getParent(), FetchLevel.ENTITY, false, false, false, true);
			}

			if (! parentTopicNode && shouldVisitChildren(fetchLevel) && node.hasNode(CmsBuiltInItem.Topic.getJcrName())){
				NodeIterator childTopicNodes = node.getNodes(CmsBuiltInItem.Topic.getJcrName());

				if (childTopicNodes.getSize() > 0){

					FetchLevel visitDepthForChildTopics = fetchLevel == FetchLevel.FULL ? FetchLevel.FULL : FetchLevel.ENTITY;

					openEntityWithNoAttributes(CmsConstants.CHILD_TOPICS);
					
					while (childTopicNodes.hasNext()){
						serializeTopicNode(childTopicNodes.nextNode(), visitDepthForChildTopics, serializeTaxonomyNode, false, false, false);
					}

					closeEntity(CmsConstants.CHILD_TOPICS);
				}
			}
		}

		closeEntity(topicQName);

		finishedNodeSerialization(CmsEntityType.TOPIC, topicQName);

	}

	private void informContentHandlerWhetherEntityIsAnArray(
			boolean entityRepresentsAnArray) throws Exception {
		
		if (entityRepresentsAnArray && outputIsJSON()){
			//This is an indication to JSON Export Content Handler that this object should be exported
			//as an array
			writeAttribute(CmsConstants.EXPORT_AS_AN_ARRAY_INSTRUCTION, "true");
		}
	}

	private boolean shouldVisitChildren(FetchLevel fetchLevel) {
		return fetchLevel != null && fetchLevel != FetchLevel.ENTITY;
	}

	private boolean outputIsJSON() {
		return resourceRepresentationType != null && resourceRepresentationType == ResourceRepresentationType.JSON;
	}

	private void serializeChildNodes(Node node, boolean childNodesAreCmsProperties, FetchLevel fetchLevel) throws Exception {

		final NodeIterator childNodes = node.getNodes();

		while (childNodes.hasNext()){
			serializeNode(childNodes.nextNode(), childNodesAreCmsProperties, fetchLevel, false, false);	
		}
	}

	private void serializeAspects(Node contentObjectJcrNode) throws Exception{
		if (contentObjectJcrNode.hasProperty(CmsBuiltInItem.Aspects.getJcrName())){
			Value[] aspects = contentObjectJcrNode.getProperty(CmsBuiltInItem.Aspects.getJcrName()).getValues();

			for (Value aspect : aspects){
				if (contentObjectJcrNode.hasNode(aspect.getString())){
					serializeCustomNode(contentObjectJcrNode.getNode(aspect.getString()), true, true);
				}
			}
		}

	}

	private void serializeCustomNode(Node node, boolean nodeRepresentsCmsProperty, boolean nodeRepresentsAnAspect) throws Exception {

		final String nodeName = node.getName();
		
		if (nodeRepresentsCmsProperty && ! shouldSerializeProperty(nodeName)){
			return ;
		}

		
		String qName = processQName(nodeName);

		String nodeCmsIdentifier = retrieveCmsIdentifier(node);

		boolean removeDefinitionFromParentPropertyDefinitionQueue = false;
		
		if (cmsIdentifierAlreadyProcessed(nodeCmsIdentifier)){

			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(qName, nodeCmsIdentifier);

			if (nodeRepresentsCmsProperty){
				addToParentPropertyDefinitionQueueDefinitionForName(nodeName,false);
				removeDefinitionFromParentPropertyDefinitionQueue = true;
			}
		}
		else{
			if (! nodeRepresentsCmsProperty){
				startedNodeSerialization(qName);
			}

			boolean exportCommonAttributes = ! nodeRepresentsCmsProperty || propertyDefinitionDefinesCommonAttributes(nodeName);

			if (nodeCmsIdentifier != null && exportCommonAttributes){
				openEntityWithAttribute(qName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), nodeCmsIdentifier);
			}
			else{
				openEntityWithNoAttributes(qName);
			}

			if (nodeRepresentsAnAspect){
				addXsiTypeAttribute(retrievePrefixedQName(qName));
			}

			if (nodeRepresentsCmsProperty){
				
				boolean multiple = propertyCanHaveMultiplevalues(nodeName);
				
				informContentHandlerWhetherEntityIsAnArray(multiple);
				
				if (node.isNodeType(CmsBuiltInItem.BinaryChannel.getJcrName())){
					serializeBinaryChannelNode(node, multiple, nodeName);
				}
				else{
					serializeChildCmsProperties(node);
				}
			}
			else{
				serializeBuiltInProperties(node, false);

				serializeChildNodes(node, nodeRepresentsCmsProperty, FetchLevel.FULL);
			}

		}

		closeEntity(qName);

		if (removeDefinitionFromParentPropertyDefinitionQueue){
			removeTheHeadDefinitionFromParentPropertyDefinitionQueue();
		}
		
		if (! nodeRepresentsCmsProperty){
			if (qName.endsWith("repositoryUser")){
				finishedNodeSerialization(CmsEntityType.REPOSITORY_USER, qName);
			}
		}
	}

	private boolean propertyCanHaveMultiplevalues(String name) throws Exception {
		
		LocalizableCmsDefinition cmsDefinition = retrieveCmsDefinition(name,false);
		
		return cmsDefinition instanceof CmsPropertyDefinition && ((CmsPropertyDefinition)cmsDefinition).isMultiple();
	}

	private boolean propertyDefinitionDefinesCommonAttributes(String name) throws Exception {
		
		LocalizableCmsDefinition cmsDefinition = retrieveCmsDefinition(name,false);
		
		return cmsDefinition.getValueType() == ValueType.Complex && ((ComplexCmsPropertyDefinitionImpl)cmsDefinition).commonAttributesAreDefined();
	}

	private void serializeBinaryChannelNode(Node node, boolean multiple, String propertyName) throws Exception {

		Node contentObjectNode =  retrieveContentObjectNodeFromNode(node);
		
		String mimeType = null;
		String sourceFilename = null;

		if (node.hasProperty(JcrBuiltInItem.JcrEncoding.getJcrName())){
			writeAttribute(CmsBuiltInItem.Encoding.getLocalPart(), node.getProperty(JcrBuiltInItem.JcrEncoding.getJcrName()).getString());
		}

		if (node.hasProperty(JcrBuiltInItem.JcrMimeType.getJcrName())){
			mimeType = node.getProperty(JcrBuiltInItem.JcrMimeType.getJcrName()).getString();
			writeAttribute(CmsBuiltInItem.MimeType.getLocalPart(), mimeType);
		}

		if (node.hasProperty(JcrBuiltInItem.JcrLastModified.getJcrName())){

			String dateTime = convertCalendarToXMLFormat(node.getProperty(JcrBuiltInItem.JcrLastModified.getJcrName()).getDate(), true);

			writeAttribute("lastModificationDate", dateTime);
		}

		if (node.hasProperty(CmsBuiltInItem.SourceFileName.getJcrName())){
			sourceFilename = node.getProperty(CmsBuiltInItem.SourceFileName.getJcrName()).getString();
			writeAttribute(CmsBuiltInItem.SourceFileName.getLocalPart(), sourceFilename);
		}

		String url = createResourceApiURLForBinaryChannel(contentObjectNode, node, multiple, propertyName);
		
		writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, url);

		if (serializeBinaryContent && node.hasProperty(JcrBuiltInItem.JcrData.getJcrName())){
			exportContentHandler.closeOpenElement();

			openEntityWithNoAttributes("content");

			exportContentHandler.closeOpenElement();

			serializeBinaryValue(node.getProperty(JcrBuiltInItem.JcrData.getJcrName()).getValue());

			closeEntity("content");
		}
	}

	private String createResourceApiURLForBinaryChannel(Node contentObjectNode, Node binaryChannelNode, boolean multiple, String propertyName) throws RepositoryException {
		
		//Create a fake BinaryChannel and use its method
		BinaryChannelImpl binaryChannel = new BinaryChannelImpl();

		if (binaryChannelNode.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName())){
			binaryChannel.setId(binaryChannelNode.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString());
		}
		binaryChannel.setAuthenticationToken(AstroboaClientContextHolder.getActiveAuthenticationToken());
		binaryChannel.setRepositoryId(AstroboaClientContextHolder.getActiveRepositoryId());
		
		if (contentObjectNode.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName())){
			binaryChannel.setContentObjectId(contentObjectNode.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString());
		}

		if (contentObjectNode.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){
			binaryChannel.setContentObjectSystemName(contentObjectNode.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString());
		}
		
		
		binaryChannel.setBinaryPropertyPermanentPath(retrievePermanentPathForBinaryNode(binaryChannelNode, propertyName, multiple));
		
		if (multiple){
			binaryChannel.binaryPropertyIsMultiValued();
		}
		
		return binaryChannel.buildResourceApiURL(null, null, null, null, null, false, false);
		
	}

	private String retrievePermanentPathForBinaryNode(Node binaryChannelNode, String propertyName, boolean multiple) throws RepositoryException {
		
		String permanentPath = propertyName;
		
		binaryChannelNode = binaryChannelNode.getParent();
		
		while (binaryChannelNode != null && ! binaryChannelNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			
			if (binaryChannelNode.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName())){
				permanentPath = binaryChannelNode.getName()+CmsConstants.LEFT_BRACKET+ binaryChannelNode.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString() + CmsConstants.RIGHT_BRACKET+ CmsConstants.FORWARD_SLASH+ permanentPath;
			}
			else{
				permanentPath = binaryChannelNode.getName()+CmsConstants.LEFT_BRACKET+ binaryChannelNode.getIndex() + CmsConstants.RIGHT_BRACKET+ CmsConstants.FORWARD_SLASH+ permanentPath;
			}
			
			binaryChannelNode = binaryChannelNode.getParent();
			
		}
		
		return permanentPath;
	}

	private Node retrieveContentObjectNodeFromNode(Node node) throws RepositoryException {
		
		Node contentObjectNode = node;
		
		while(contentObjectNode!= null && !contentObjectNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			contentObjectNode = contentObjectNode.getParent();
			
			if (contentObjectNode == null || contentObjectNode.isNodeType(CmsBuiltInItem.SYSTEM.getJcrName())){
				return null;
			}
		}
		
		return contentObjectNode;
	}

	private void serializeBinaryValue(Value value) throws Exception,
	ValueFormatException, RepositoryException, PathNotFoundException {

		String content = JackrabbitDependentUtils.serializeBinaryValue(value);
		exportContentHandler.writeContent(content.toCharArray(), 0, content.length());
	}

	/*
	 * Checks whether property should be serialized or not
	 */
	private boolean shouldSerializeProperty(String propertyName) {

		//Get property path
		String propertyPath = retrieveFullPathForProperty(propertyName);

		if (objectReferenceIsSerialized){
			return MarshalUtils.propertyShouldBeMarshalled(propertyPathsWhoseValuesWillBeIncludedInTheSerializationOfObjectReferences, propertyName, propertyPath);
		}
		else{
			return MarshalUtils.propertyShouldBeMarshalled(propertyPathsWhoseValuesWillBeIncludedInTheSerialization, propertyName, propertyPath);
		}
	}

	//Method responsible to serialize properties of
	// a complex cms property in the order specified in XSD
	//Child properties can either be a jcr property or a node
	private void serializeChildCmsProperties(Node node) throws Exception{
		addToParentPropertyDefinitionQueueDefinitionForName(node.getName(), node.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()));

		Map<String, CmsPropertyDefinition> orderedChildCmsPropertyNames = retrieveChildPropertyDefinitionsPerName(node);

		if (orderedChildCmsPropertyNames != null && ! orderedChildCmsPropertyNames.isEmpty()){

			long numberOfJcrPropertiesLeftToProcess = node.getProperties().getSize();
			long numberOfJcrNodesLeftToProcess = node.getNodes().getSize();
			
			for (Entry<String, CmsPropertyDefinition> cmsPropertyDefinitionEntry : orderedChildCmsPropertyNames.entrySet()){

				if (numberOfJcrNodesLeftToProcess == 0 && numberOfJcrPropertiesLeftToProcess == 0){
					//No more jcr items left, thus no more properties exist for this node.
					//No point in iterating the rest of Cms Properties 
					break;
				}
				
				String cmsPropertyName = cmsPropertyDefinitionEntry.getKey();

				//TODO : Properties which have not been saved but have default value, 
				//should exist in the serialization? This is the case when serialization is done through
				// xml() and json() methods.These methods use Astroboa API which renders a property
				//and provides the default value if this property is not found in repository
				//This case stands only for optional properties with default values
				
				if (cmsPropertyDefinitionEntry.getValue().getValueType() != ValueType.Complex && 
						cmsPropertyDefinitionEntry.getValue().getValueType() != ValueType.Binary && 
						numberOfJcrPropertiesLeftToProcess > 0){
					
					if (node.hasProperty(cmsPropertyName)){
						serializePropertyAsElement(node.getProperty(cmsPropertyName));
						numberOfJcrPropertiesLeftToProcess--;
					}
				}
				else if (numberOfJcrNodesLeftToProcess > 0){

					if (node.hasNode(cmsPropertyName)){

						NodeIterator childNodes = node.getNodes(cmsPropertyName);

						if (childNodes.getSize() == 1){
							serializeCustomNode(childNodes.nextNode(), true, false);
							numberOfJcrNodesLeftToProcess--;
						}
						else{
							Map<Integer,Node> nodesPerOrder = new TreeMap<Integer, Node>();

							int unknownIndex = 10000;

							while(childNodes.hasNext()){
								Node childNode = childNodes.nextNode();

								if (childNode.hasProperty(CmsBuiltInItem.Order.getJcrName())){
									try{

										int index = (int)childNode.getProperty(CmsBuiltInItem.Order.getJcrName()).getLong() - 1;

										if (nodesPerOrder.containsKey(index)){
											nodesPerOrder.put(unknownIndex++,childNode);
										}
										else{
											nodesPerOrder.put(index,childNode);
										}

									}
									catch(Exception e){
										logger.warn("Node "+childNode.getPath()+" did not have a valid order value and therefore corresponding cms property will be added at the end of the list", 
												e);

										nodesPerOrder.put(unknownIndex++,childNode);
									}
								}
								else{
									nodesPerOrder.put(unknownIndex++,childNode);
								}
							}

							for (Node child : nodesPerOrder.values()){
								serializeCustomNode(child, true, false);
								numberOfJcrNodesLeftToProcess--;
							}
						}
					}
				}
			}
		}
		else{
			//	Could not serialize properties in order. Follow the default procedure
			serializePropertiesAsElements(node);

			serializeChildNodes(node, true, FetchLevel.FULL);
		}

		removeTheHeadDefinitionFromParentPropertyDefinitionQueue();
	}

	private Map<String, CmsPropertyDefinition>  retrieveChildPropertyDefinitionsPerName(Node node) throws Exception {

		LocalizableCmsDefinition currentDefinition = parentPropertyDefinitionQueue.peek();

		if (currentDefinition != null){
			if (currentDefinition instanceof ComplexCmsPropertyDefinition){
				return ((ComplexCmsPropertyDefinition)currentDefinition).getChildCmsPropertyDefinitions();
			}

			if (currentDefinition instanceof ContentObjectTypeDefinition){
				return ((ContentObjectTypeDefinition)currentDefinition).getPropertyDefinitions();
			}
		}

		return null;
	}

	private void removeTheHeadDefinitionFromParentPropertyDefinitionQueue(){
		parentPropertyDefinitionQueue.poll();
	}

	private void addToParentPropertyDefinitionQueueDefinitionForName(String name, boolean nameRefersToAContentType) throws Exception{

		LocalizableCmsDefinition cmsPropertyDefinition = retrieveCmsDefinition(name, nameRefersToAContentType);

		if (cmsPropertyDefinition != null){
			parentPropertyDefinitionQueue.push(cmsPropertyDefinition);
		}

	}

	private LocalizableCmsDefinition retrieveCmsDefinition(String name, boolean nameRefersToAContentType)
			throws Exception {
		LocalizableCmsDefinition currentDefinition = parentPropertyDefinitionQueue.peek();

		LocalizableCmsDefinition cmsPropertyDefinition = null;

		if (currentDefinition != null && ! nameRefersToAContentType){

			if (currentDefinition instanceof ComplexCmsPropertyDefinition){

				cmsPropertyDefinition = ((ComplexCmsPropertyDefinition)currentDefinition).getChildCmsPropertyDefinition(name);
			}
			else if (currentDefinition instanceof ContentObjectTypeDefinition){
				cmsPropertyDefinition = ((ContentObjectTypeDefinition)currentDefinition).getCmsPropertyDefinition(name);
			}
		}
		else{
			if (!nameRefersToAContentType){
				cmsPropertyDefinition = definitionServiceDao.getCmsPropertyDefinition(name);
			}

			if (cmsPropertyDefinition == null){
				cmsPropertyDefinition = definitionServiceDao.getContentObjectTypeDefinition(name);
			}

		}
		return cmsPropertyDefinition;
	}

	protected String processQName(String qName) 
	{
		if (qName != null && qName.startsWith(BCCMS_PREFIX_WITH_SEMICOLON)){
			return qName.replaceFirst(BCCMS_PREFIX_WITH_SEMICOLON, "");
		}

		return qName;
	}

	private void processLocalization(Node node) throws Exception {

		if (node.hasNode(CmsBuiltInItem.Localization.getJcrName())){
			Node localizationJcrNode = node.getNode(CmsBuiltInItem.Localization.getJcrName());

			PropertyIterator locales = localizationJcrNode.getProperties(ItemUtils.createNewBetaConceptItem(CmsConstants.ANY_NAME).getJcrName());

			if (locales.getSize() > 0){
				openEntityWithNoAttributes(CmsBuiltInItem.Localization.getLocalPart());

				if (outputIsJSON()){
					serializeLocalizationForJSONFormat(locales);
				}
				else{
					serializeLocalizationForXMLFormat(locales);
				}

				closeEntity(CmsBuiltInItem.Localization.getLocalPart());
			}
		}
	}

	private void serializeLocalizationForXMLFormat(PropertyIterator locales)
			throws RepositoryException, ValueFormatException, Exception {
		while ( locales.hasNext() ){
			Property localeProperty = locales.nextProperty();

			String locale = localeProperty.getName().replaceAll(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+":", "");

			String localizedLabel = localeProperty.getString();

			openEntityWithAttribute(CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME, CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX, locale);

			char[] charArray = localizedLabel.toCharArray();
			writeElementContent(charArray);

			closeEntity(CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME);
		}
	}

	private void serializeLocalizationForJSONFormat(PropertyIterator locales) throws RepositoryException, ValueFormatException, Exception {
		
		openEntityWithNoAttributes(CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME);
		
		while ( locales.hasNext() ){
			Property localeProperty = locales.nextProperty();
		
			String locale = localeProperty.getName().replaceAll(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+":", "");
		
			String localizedLabel = localeProperty.getString();
			
			writeAttribute(locale, localizedLabel);
		
		}
		closeEntity(CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME);
	}


	public  void serializeTaxonomyNode(Node node, FetchLevel fetchLevel, boolean nodeRepresentsResourceCollectionItem, boolean nodeRepresentsRootElement) throws Exception{

		String taxonomyCmsIdentifier = retrieveCmsIdentifier(node);

		String taxonomyQName = CmsBuiltInItem.Taxonomy.getLocalPart();

		if (nodeRepresentsRootElement){
			taxonomyQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+ CmsBuiltInItem.Taxonomy.getLocalPart();
		}

		if (nodeRepresentsResourceCollectionItem){
			if (outputIsJSON()){
				//When serializing a collection of resources
				//the name of the element which represents the taxonomy
				//is CmsConstants.RESOURCE when output is JSON 
				taxonomyQName = CmsConstants.RESOURCE;
			}
			else{
				//Node represents a taxonomy which appears
				//as a root child of a resource representation element.
				//In XML corresponding element should be prefixed
				//as its parent (resourceCollection) does not share the
				//same namespace
				taxonomyQName = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX +":"+ CmsBuiltInItem.Taxonomy.getLocalPart();
			}
		}


		if (cmsIdentifierAlreadyProcessed(taxonomyCmsIdentifier)){

			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(taxonomyQName, taxonomyCmsIdentifier);

			//Taxonomy name is the name of the node. Very SPECIAL CASE
			writeAttribute(CmsBuiltInItem.Name.getLocalPart(), node.getName());

			addUrlForEntityRepresentedByNode(node);
			
			processLocalization(node);
		}
		else{
			startedNodeSerialization(taxonomyQName);

			openEntityWithAttribute(taxonomyQName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), taxonomyCmsIdentifier);

			markCmsIdentifierProcessed(taxonomyCmsIdentifier);

			//Taxonomy name is the name of the node. Very SPECIAL CASE
			writeAttribute(CmsBuiltInItem.Name.getLocalPart(), node.getName());

			serializeBuiltInProperties(node, false);

			if (shouldVisitChildren(fetchLevel) && node.hasNode(CmsBuiltInItem.Topic.getJcrName())){
				NodeIterator rootTopicNodes = node.getNodes(CmsBuiltInItem.Topic.getJcrName());

				if (rootTopicNodes.getSize() > 0){

					openEntityWithNoAttributes(CmsConstants.ROOT_TOPICS);

					FetchLevel visitDepthForRootTopics = fetchLevel == FetchLevel.FULL ? FetchLevel.FULL : FetchLevel.ENTITY;

					while (rootTopicNodes.hasNext()){
						serializeTopicNode(rootTopicNodes.nextNode(), visitDepthForRootTopics, true, false, false, false);
					}

					closeEntity(CmsConstants.ROOT_TOPICS);
				}
			}
		}

		closeEntity(taxonomyQName);

		finishedNodeSerialization(CmsEntityType.TAXONOMY, taxonomyQName);

	}

	public void serializeContentObjectNode(Node node, boolean nodeRepresentsResourceCollectionItem) throws Exception{

		String cmsIdentifier = retrieveCmsIdentifier(node);

		String contentObjectQName = retrievePrefixedQName(node.getName());

		if (nodeRepresentsResourceCollectionItem){
			//When serializing a collection of resources
			//the name of the element which represents the content object
			//is CmsConstants.RESOURCE when output is JSON or when 
			//user has requested that all xml elements which represent the content object
			//will be named after the same name, which is the name "resource"
			if (outputIsJSON()||useTheSameNameForAllObjects){
				contentObjectQName = CmsConstants.RESOURCE;
			}
		}

		if (cmsIdentifierAlreadyProcessed(cmsIdentifier)){

			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(contentObjectQName, cmsIdentifier);

			serializeBasicContentObjectInformation(cmsIdentifier, node);
		}
		else{
			startedNodeSerialization(contentObjectQName);

			openEntityWithAttribute(contentObjectQName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), cmsIdentifier);

			markCmsIdentifierProcessed(cmsIdentifier);

			//addXsiTypeAttribute(retrievePrefixedQName(contentObjectQName));

			serializeBuiltInProperties(node, false);

			serializeChildCmsProperties(node);

			serializeAspects(node);
		}

		closeEntity(contentObjectQName);

		finishedNodeSerialization(CmsEntityType.CONTENT_OBJECT, contentObjectQName);
	}

	private void finishedNodeSerialization(CmsEntityType cmsEntity, String qName) {
		if (StringUtils.equals(qNameOfRootElement, qName)){
			increaseNumberOfSerializedEntities(cmsEntity);

			qNameOfRootElement = null;
		}
	}

	private void startedNodeSerialization(String qName) {

		if (qNameOfRootElement == null){
			qNameOfRootElement = qName;
		}
	}

	private void addXsiTypeAttribute(String qName) throws Exception {
		if (!outputIsJSON()){
			writeAttribute("xsi:type", qName);
		}

	}

	private void increaseNumberOfSerializedEntities(CmsEntityType entity) {
		if (serializationReport !=null)
		{
			switch (entity) {
			case TAXONOMY:
				((SerializationReportImpl)serializationReport).increaseTaxonomiesSerialized(1);	
				break;
			case CONTENT_OBJECT:
				((SerializationReportImpl)serializationReport).increaseNumberOfObjectsSerialized(1);	
				break;
			case REPOSITORY_USER:
				((SerializationReportImpl)serializationReport).increaseRepositoryUsersSerialized(1);	
				break;
			case SPACE:
				((SerializationReportImpl)serializationReport).increaseSpacesSerialized(1);	
				break;
			default:
				break;
			}

		}
	}

	private String retrievePrefixedQName(String qName) {
		if (prefixesPerType != null && qName != null && prefixesPerType.containsKey(qName)){
			return prefixesPerType.get(qName)+CmsConstants.QNAME_PREFIX_SEPARATOR+qName;
		}

		return qName;
	}

	private String retrieveCmsIdentifier(Node node)
	throws RepositoryException {
		String cmsIdentifier = null;

		if (cmsRepositoryEntityUtils.hasCmsIdentifier(node)){
			cmsIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(node);
		}
		return cmsIdentifier;
	}

	private boolean cmsIdentifierAlreadyProcessed(String cmsIdentifier) {

		return cmsIdentifier != null && processedCmsRepositoryEntityIdentifiers.contains(cmsIdentifier);
	}



	private void serializeBuiltInProperties(Node node, boolean nodeRepresentsParentNodeOfTopicOrSpace) throws Exception {

		PropertyIterator builtInProperties = node.getProperties(BCCMS_PREFIX_WITH_SEMICOLON+CmsConstants.ANY_NAME);

		boolean addOwner = false; 

		while (builtInProperties.hasNext()){
			Property property = builtInProperties.nextProperty();

			if (property.getName().equals(CmsBuiltInItem.Name.getJcrName())){
				writeAttribute(CmsBuiltInItem.Name.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.SystemName.getJcrName())){
				writeAttribute(CmsBuiltInItem.SystemName.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName())){
				writeAttribute(CmsBuiltInItem.ContentObjectTypeName.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.SystemBuiltinEntity.getJcrName()) && property.getBoolean()){
				writeAttribute(CmsBuiltInItem.SystemBuiltinEntity.getLocalPart(), String.valueOf(property.getBoolean()));
			}
			else if (property.getName().equals(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName())){
				if (! nodeRepresentsParentNodeOfTopicOrSpace){
					writeAttribute(CmsBuiltInItem.AllowsReferrerContentObjects.getLocalPart(), String.valueOf(property.getBoolean()));
				}
			}
			else if (property.getName().equals(CmsBuiltInItem.Encoding.getJcrName())){
				writeAttribute(CmsBuiltInItem.Encoding.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.ExternalId.getJcrName())){
				writeAttribute(CmsBuiltInItem.ExternalId.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.Label.getJcrName())){
				writeAttribute(CmsBuiltInItem.Label.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.MimeType.getJcrName())){
				writeAttribute(CmsBuiltInItem.MimeType.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.Order.getJcrName()) && 
					(node.isNodeType(CmsBuiltInItem.Space.getJcrName()) ||
							node.isNodeType(CmsBuiltInItem.Topic.getJcrName()))
			){
				if (!nodeRepresentsParentNodeOfTopicOrSpace){
					writeAttribute(CmsBuiltInItem.Order.getLocalPart(), property.getString());
				}
			}
			else if (property.getName().equals(CmsBuiltInItem.SourceFileName.getJcrName())){
				writeAttribute(CmsBuiltInItem.SourceFileName.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.Size.getJcrName())){
				writeAttribute(CmsBuiltInItem.Size.getLocalPart(), property.getString());
			}
			else if (property.getName().equals(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName())){
				//Do not add owner if entity to be serialized is content object and specific 
				//properties must be serialized
				if (CollectionUtils.isEmpty(propertyPathsWhoseValuesWillBeIncludedInTheSerialization) || propertyPathsWhoseValuesWillBeIncludedInTheSerialization.contains(CmsConstants.OWNER_ELEMENT_NAME)){
					addOwner = true;
				}
			}

		}

		//Add url for this node
		addUrlForEntityRepresentedByNode(node);
		
		if (!nodeRepresentsParentNodeOfTopicOrSpace){
			addNumberOfChildren(node);
		}

		processLocalization(node);

		if (addOwner){
			addOwnerAsElement(node);
		}
	}

	private void addUrlForEntityRepresentedByNode(Node node) throws Exception {
		
		UrlProperties urlProperties = new UrlProperties();
		urlProperties.setRelative(false);
		urlProperties.setResourceRepresentationType(resourceRepresentationType);

		if (node.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			if (node.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){
				
				urlProperties.setFriendly(true);
				urlProperties.setName(node.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString());
				
				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
						ResourceApiURLUtils.generateUrlForType(ContentObject.class, urlProperties));
			}
			else{
				
				urlProperties.setFriendly(false);
				urlProperties.setIdentifier(cmsRepositoryEntityUtils.getCmsIdentifier(node));

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
					ResourceApiURLUtils.generateUrlForType(ContentObject.class, urlProperties));
			}
		}
		else if (node.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
			
			if (node.hasProperty(CmsBuiltInItem.Name.getJcrName())){
				
				urlProperties.setFriendly(true);
				urlProperties.setName(node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
						ResourceApiURLUtils.generateUrlForType(Topic.class, urlProperties));
			}
			else{
				urlProperties.setFriendly(false);
				urlProperties.setIdentifier(cmsRepositoryEntityUtils.getCmsIdentifier(node));

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
					ResourceApiURLUtils.generateUrlForType(Topic.class, urlProperties));
			}
			
		}
		else if (node.isNodeType(CmsBuiltInItem.Space.getJcrName())){
			
			if (node.hasProperty(CmsBuiltInItem.Name.getJcrName())){
				
				urlProperties.setFriendly(true);
				urlProperties.setName(node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
						ResourceApiURLUtils.generateUrlForType(Space.class, urlProperties));
			}
			else{
				
				urlProperties.setFriendly(false);
				urlProperties.setIdentifier(cmsRepositoryEntityUtils.getCmsIdentifier(node));

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
					ResourceApiURLUtils.generateUrlForType(Space.class, urlProperties));
			}
			
		}
		else if (node.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
			
			urlProperties.setFriendly(true);
			urlProperties.setName(node.getName());

			writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME, 
					ResourceApiURLUtils.generateUrlForType(Taxonomy.class, urlProperties));
		}
		
	}

	private void addNumberOfChildren(Node node) throws RepositoryException,
	Exception {
		if (node.isNodeType(CmsBuiltInItem.Topic.getJcrName()) || node.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
			if (node.hasNode(CmsBuiltInItem.Topic.getJcrName())){
				writeAttribute(CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, String.valueOf(node.getNodes(CmsBuiltInItem.Topic.getJcrName()).getSize()));
			}
			else{
				writeAttribute(CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, "0");
			}
		}
		else if ( node.isNodeType(CmsBuiltInItem.Space.getJcrName()) ){ 
			if (node.hasNode(CmsBuiltInItem.Space.getJcrName())){
				writeAttribute(CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, String.valueOf(node.getNodes(CmsBuiltInItem.Space.getJcrName()).getSize()));
			}
			else{
				writeAttribute(CmsConstants.NUMBER_OF_CHILDREN_ATTRIBUTE_NAME, "0");
			}
		}
	}

	private void serializePropertiesAsElements(Node node) throws Exception{

		PropertyIterator properties = node.getProperties();

		while (properties.hasNext()){
			Property property = properties.nextProperty();

			//Do not process any property which starts with jcr, nt or bccms
			String propertyName = property.getName();

			if (propertyName.startsWith(JCR_MIX_PREFIX_WITH_SEMI_COLON) ||
					propertyName.startsWith(JCR_PREFIX_WITH_SEMI_COLON) ||
					propertyName.startsWith(NT_PREFIX_WITH_SEMI_COLON) ||
					propertyName.startsWith(BCCMS_PREFIX_WITH_SEMICOLON) 
			){
				continue;
			}

			serializePropertyAsElement(property);

		}

	}

	private void serializePropertyAsElement(Property property)  throws Exception{
		String propertyName = property.getName();

		if (shouldSerializeProperty(propertyName)){

			CmsPropertyDefinition propertyDefinition = retrieveDefinitionForChildProperty(propertyName);

			if (propertyDefinition == null){
				logger.warn("Could not serialize property {}. No definition found. Property path in JCR is {}", propertyName, 
						property.getPath());
				return;
			}
			
			final ValueType valueType = propertyDefinition.getValueType();
			
			boolean dateTimePattern = 	propertyDefinition != null && ValueType.Date == valueType  && 
						((CalendarPropertyDefinition)propertyDefinition).isDateTime();

			/*
			 * There are some cases where users might change property's cardinality 
			 * from multiple to single value or vice versa.
			 * If user does not perform any kind of migration, there will be an inconsistency in the way 
			 * property values are stored in JCR. 
			 * For example, if a user decides to change property 'language' from single to multi value, 
			 * then new values will be stored inside an array in contrary to the old values.
			 * 
			 * JCR specifies different methods for retrieving values of a property (getValue() for single
			 * and getValues() for multiple) and it throws an exception if you call getValues() on a 
			 * single value property. 
			 * 
			 * In our example, in order to get old values you need to call method getValue() and in order
			 * to retrieve new values you need to call getValues()!!! This is due to the fact that JCR
			 * stores property definition among other things.
			 * 
			 * We must detect this type of inconsistency but instead of throwing an exception, 
			 * we detect which method we should call and just issue a warning. In the case where
			 * cardinality is changed from multivalue to single value, we serialize only the first value
			 * because we must conform to the property' definition in the XSD.
			 */
			
			boolean userHasDefinedPropertyAsMultiple = propertyDefinition != null && propertyDefinition.isMultiple();
			boolean jcrHasMarkedPropertyAsMultiple = property.getDefinition() != null && property.getDefinition().isMultiple();

			if (jcrHasMarkedPropertyAsMultiple){
				
				for (Value value : property.getValues()){
					createElementWithValueForProperty(propertyName, value, valueType, dateTimePattern, userHasDefinedPropertyAsMultiple);
					
					if (!userHasDefinedPropertyAsMultiple){
						logger.warn("Property "+propertyDefinition.getFullPath() + " has been defined as a single value property. Property's instance " +
								property.getPath() + " has been marked as multiple. Probably this property used to be a multi value property but its cardinality" +
										" has changed. Astroboa will serialize only the first value in order to be consistent with current definition in XSD." );
						break;
					}
				}
			}
			else{
				createElementWithValueForProperty(propertyName, property.getValue(), valueType, dateTimePattern, userHasDefinedPropertyAsMultiple);
				
				if (userHasDefinedPropertyAsMultiple){
					logger.warn("Property "+propertyDefinition.getFullPath() + " has been defined as a multi value property. Property's instance " +
							property.getPath() + " has been marked as single. Probably this property used to be a single value property but its cardinality" +
									" has changed. Property value will be serialized normally" );
					
				}
			}
		}
	}

	private String retrieveFullPathForProperty(String propertyName){

		if (!parentPropertyDefinitionQueue.isEmpty()){
			LocalizableCmsDefinition currentDefinition = parentPropertyDefinitionQueue.peek();

			if (currentDefinition instanceof CmsPropertyDefinition){
				return PropertyPath.createFullPropertyPath(((CmsPropertyDefinition)currentDefinition).getPath(), propertyName);
			}
		}

		return propertyName;
	}

	private CmsPropertyDefinition retrieveDefinitionForChildProperty(String propertyName) {

		if (!parentPropertyDefinitionQueue.isEmpty()){
			LocalizableCmsDefinition currentDefinition = parentPropertyDefinitionQueue.peek();

			if (currentDefinition instanceof ComplexCmsPropertyDefinition){
				return ((ComplexCmsPropertyDefinition)currentDefinition).getChildCmsPropertyDefinition(propertyName);
			}
			else if (currentDefinition instanceof ContentObjectTypeDefinition){
				return ((ContentObjectTypeDefinition)currentDefinition).getCmsPropertyDefinition(propertyName);
			}
		}

		return null;
	}

	private String convertCalendarToXMLFormat(Calendar calendar, boolean dateTimePattern){
		if (dateTimePattern){
			GregorianCalendar gregCalendar = new GregorianCalendar(calendar.getTimeZone());
			gregCalendar.setTimeInMillis(calendar.getTimeInMillis());

			return df.newXMLGregorianCalendar(gregCalendar).toXMLFormat();
		}
		else{
			return df.newXMLGregorianCalendarDate(
					calendar.get( Calendar.YEAR ),
					calendar.get( Calendar.MONTH )+1,  	// Calendar.MONTH is zero based, XSD Date datatype's month field starts
					//	with JANUARY as 1.
					calendar.get( Calendar.DAY_OF_MONTH ),
					DatatypeConstants.FIELD_UNDEFINED ).toXMLFormat();
		}
	}

	private void createElementWithValueForProperty(String propertyName, Value value, ValueType valueType, boolean dateTimePattern, boolean propertyMayHaveMultipltValues) throws Exception {
		if (value != null){
			if (ValueType.Topic == valueType){
				addTopicReferenceElement(propertyName, value.getString(), propertyMayHaveMultipltValues);
			}
			else if (ValueType.ContentObject == valueType){
				addContentObjectReferenceElement(propertyName, value.getString(), propertyMayHaveMultipltValues);
			}
			else{

				if (propertyMayHaveMultipltValues && outputIsJSON()){
					openEntityWithAttribute(propertyName, CmsConstants.EXPORT_AS_AN_ARRAY_INSTRUCTION, "true");
				}
				else{
					openEntityWithNoAttributes(propertyName);
				}

				if (ValueType.Binary == valueType){
					exportContentHandler.closeOpenElement();

					serializeBinaryValue(value);

				}
				else if (ValueType.Date == valueType){
					char[] ch = convertCalendarToXMLFormat(value.getDate(), dateTimePattern).toCharArray();
					writeElementContent(ch);
				}
				else {
					char[] ch = value.getString().toCharArray();
					writeElementContent(ch);
				}

				closeEntity(propertyName);
			}
		}
	}

	private void writeElementContent(char[] ch) throws Exception {
		exportContentHandler.writeContent(ch, 0, ch.length);
	}

	private void addTopicReferenceElement(String propertyName, String topicId, boolean referenceMayHaveMultipleValues) throws Exception{

		if (cmsIdentifierAlreadyProcessed(topicId)){
			serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(propertyName, topicId);
		}
		else{
			openEntityWithAttribute(propertyName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), topicId);
		}

		Node topicJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTopic(session, topicId);

		markCmsIdentifierProcessed(topicId);

		if (referenceMayHaveMultipleValues){
			informContentHandlerWhetherEntityIsAnArray(true);
		}

		serializeBasicTopicInformation(topicJcrNode);

		closeEntity(propertyName);
	}

	private void serializeBasicTopicInformation(Node topicJcrNode)
	throws RepositoryException, Exception, ValueFormatException,
	PathNotFoundException {

		if (topicJcrNode != null){

			if (topicJcrNode.hasProperty(CmsBuiltInItem.Name.getJcrName())){

				writeAttribute(CmsBuiltInItem.Name.getLocalPart(), 
						topicJcrNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
			}

			addUrlForEntityRepresentedByNode(topicJcrNode);
			
			processLocalization(topicJcrNode);
		}
	}

	private void addContentObjectReferenceElement(String propertyName, String contentObjectId, boolean referenceMayHaveMultipleValues) throws Exception {

		Node contentObjectJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectId);

		startedNodeSerialization(propertyName);

		openEntityWithAttribute(propertyName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), contentObjectId);

		if (referenceMayHaveMultipleValues){
			informContentHandlerWhetherEntityIsAnArray(true);
		}

		serializeBasicContentObjectInformation(contentObjectId,	contentObjectJcrNode);
		
		objectReferenceIsSerialized = true;
		serializeChildCmsProperties(contentObjectJcrNode);
		serializeAspects(contentObjectJcrNode);
		objectReferenceIsSerialized = false;
		
		closeEntity(propertyName);
		
		finishedNodeSerialization(CmsEntityType.CONTENT_OBJECT, propertyName);

	}

	private void serializeBasicContentObjectInformation(String contentObjectId,
			Node contentObjectJcrNode) throws Exception, RepositoryException,
			ValueFormatException, PathNotFoundException {


		boolean systemNameFound = false;

		UrlProperties urlProperties = new UrlProperties();
		urlProperties.setRelative(false);
		urlProperties.setResourceRepresentationType(resourceRepresentationType);

		if (contentObjectJcrNode != null){
			

			//ContentObject SystemName
			if (contentObjectJcrNode.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){

				final String systemName = contentObjectJcrNode.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString();
				
				writeAttribute(CmsBuiltInItem.SystemName.getLocalPart(), systemName);
				
				urlProperties.setFriendly(true);
				urlProperties.setName(systemName);

				writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME,
						ResourceApiURLUtils.generateUrlForType(ContentObject.class, urlProperties));
				
				systemNameFound = true;
			}

			//ContentObjecType name
			if (contentObjectJcrNode.hasProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName())){

				writeAttribute(CmsBuiltInItem.ContentObjectTypeName.getLocalPart(), 
						contentObjectJcrNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString());
			}
		}
		
		if (!systemNameFound){
			
			urlProperties.setFriendly(false);
			urlProperties.setIdentifier(contentObjectId);

			writeAttribute(CmsConstants.URL_ATTRIBUTE_NAME,
				ResourceApiURLUtils.generateUrlForType(ContentObject.class, urlProperties ));
		}
	}

	private void addOwnerAsElement(Node node) throws Exception {

		String ownerCmsIdentifier = null;

		if (node.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName())){
			ownerCmsIdentifier = node.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();
		}

		if (ownerCmsIdentifier != null){
			if (cmsIdentifierAlreadyProcessed(ownerCmsIdentifier)){

				serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(CmsConstants.OWNER_ELEMENT_NAME, ownerCmsIdentifier);

			}
			else{
				openEntityWithAttribute(CmsConstants.OWNER_ELEMENT_NAME, CmsBuiltInItem.CmsIdentifier.getLocalPart(), ownerCmsIdentifier);

				markCmsIdentifierProcessed(ownerCmsIdentifier);
			}

			Node ownerJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForRepositoryUser(session, ownerCmsIdentifier);

			if (ownerJcrNode != null){

				if (ownerJcrNode.hasProperty(CmsBuiltInItem.ExternalId.getJcrName())){
					writeAttribute(CmsBuiltInItem.ExternalId.getLocalPart(), 
							ownerJcrNode.getProperty(CmsBuiltInItem.ExternalId.getJcrName()).getString());
				}

				if (ownerJcrNode.hasProperty(CmsBuiltInItem.Label.getJcrName())){
					writeAttribute(CmsBuiltInItem.Label.getLocalPart(), 
							ownerJcrNode.getProperty(CmsBuiltInItem.Label.getJcrName()).getString());
				}
			}

			closeEntity(CmsConstants.OWNER_ELEMENT_NAME);
		}
	}


	private void markCmsIdentifierProcessed(String cmsIdentifier) {
		processedCmsRepositoryEntityIdentifiers.add(cmsIdentifier);
	}

	private NodeType determineNodeType(Node node) throws Exception {

		if (node.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			return NodeType.ContentObject;
		}
		else if (
				node.isNodeType(CmsBuiltInItem.GenericYearFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.GenericMonthFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.GenericDayFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.GenericHourFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.GenericMinuteFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.GenericSecondFolder.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.ContentObjectRoot.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.RepositoryUserRoot.getJcrName()) ||
				node.isNodeType(CmsBuiltInItem.TaxonomyRoot.getJcrName())
		){
			return NodeType.ToBeIgnored;
		}
		else if (node.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
			return NodeType.Taxonomy;
		}
		else if (node.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
			return NodeType.Topic;
		}
		else if (node.isNodeType(CmsBuiltInItem.Space.getJcrName())){
			return NodeType.Space;
		}


		if (node.getParent() !=null &&  
				StringUtils.equals(node.getParent().getName(), CmsBuiltInItem.ContentObjectRoot.getJcrName())){
			return NodeType.ToBeIgnored;
		}

		return NodeType.Other;
	}

	private void serializeCmsRepositoryEntityIdentifierForAnAlreadySerializedEntity(String entityName, String entityId) throws Exception{

		openEntityWithAttribute(entityName, CmsBuiltInItem.CmsIdentifier.getLocalPart(), entityId);
		
	}


	public void writeAttribute(String attributeName, String attributeValue) throws Exception{
		exportContentHandler.writeAttribute(attributeName, attributeValue);
	}

	public void openEntityWithNoAttributes(String elementQName) throws Exception{

		if (rootElementAttributes != null){
			exportContentHandler.startElement(elementQName, rootElementAttributes);
			rootElementAttributes = null;
		}
		else{
			exportContentHandler.startElementWithNoAttributes(elementQName);
		}
	}

	public void openEntityWithAttribute(String elementQName, String attributeName, String attributeValue) throws Exception{

		if (rootElementAttributes != null){
			IOUtils.addAttribute(rootElementAttributes, attributeName, attributeValue);
			exportContentHandler.startElement(elementQName,rootElementAttributes);
			rootElementAttributes = null;
		}
		else{
			exportContentHandler.startElementWithOnlyOneAttribute(elementQName, attributeName, attributeValue);
		}
	}

	public void openEntity(String elementQName, AttributesImpl atts) throws Exception{

		if (rootElementAttributes != null){
			for (int i=0;i<atts.getLength(); i++){
				IOUtils.addAttribute(rootElementAttributes, atts.getLocalName(i), atts.getValue(i));
			}
			exportContentHandler.startElement(elementQName, rootElementAttributes);
			rootElementAttributes = null;
		}
		else{
			exportContentHandler.startElement(elementQName, atts);
		}
	}

	public void closeEntity(String elementQName) throws Exception{

		exportContentHandler.endElement(elementQName);
	}

	public void setPrefixesPerType(Map<String, String> prefixesPerType){

		this.prefixesPerType = prefixesPerType;
	}

	public void setSerializationReport(SerializationReport serializationReport){

		this.serializationReport = serializationReport;	
	}

	public void setDefinitionServiceDao(DefinitionServiceDao definitionServiceDao){

		this.definitionServiceDao = definitionServiceDao;
	}

	public void setPropertyPathsWhoseValuesWillBeIncludedInTheSerialization(List<String> propertyPathsWhoseValuesWillBeIncludedInTheSerialization) {
		this.propertyPathsWhoseValuesWillBeIncludedInTheSerialization = propertyPathsWhoseValuesWillBeIncludedInTheSerialization;
	}

	public void serializeBinaryContent(boolean serializeBinaryContent){
		this.serializeBinaryContent = serializeBinaryContent;
	}

	public void useTheSameNameForAllObjects(
			boolean useTheSameNameForAllObjects) {
		this.useTheSameNameForAllObjects = useTheSameNameForAllObjects;
	}
	
	
}
