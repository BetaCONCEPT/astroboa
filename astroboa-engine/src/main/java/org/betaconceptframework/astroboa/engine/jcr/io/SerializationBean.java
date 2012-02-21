/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIterator;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIteratorUsingJcrRangeIterator;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.io.SerializationReportImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.model.impl.item.JcrNamespaceConstants;
import org.betaconceptframework.astroboa.service.dao.DefinitionServiceDao;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.SchemaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is responsible to serialize content to a specific directory inside repository home
 * directory.
 * 
 * It spans a new Thread and serializes either all or partial content into a compressed XML file
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRED)
public class SerializationBean extends JcrDaoSupport{

	private static final String ZIP = ".zip";

	private Logger logger = LoggerFactory.getLogger(getClass());

	public enum CmsEntityType{
		OBJECT, 
		TAXONOMY,
		REPOSITORY_USER,
		ORGANIZATION_SPACE,
		REPOSITORY, 
		TOPIC, 
		SPACE;
	}

	private CmsEntityType entityTypeToSerialize;
	
	private AstroboaClientContext clientContext;

	@Autowired
	private DefinitionServiceDao definitionServiceDao;
	
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	@Autowired
	private ContentObjectRenderer contentObjectRenderer;
	
	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRES_NEW)
	public void serializeObjectsUsingCriteria(ContentObjectCriteria contentObjectCriteria, String filePath, String filename, AstroboaClientContext clientContext, SerializationReport serializationReport, 
			SerializationConfiguration serializationConfiguration){

		long start = System.currentTimeMillis();
		
		initialize(CmsEntityType.OBJECT, clientContext);

		try{

			//Register client context 
			AstroboaClientContextHolder.registerClientContext(clientContext, true);

			//Get Jcr Session
			Session session = getSession();

			//Serialize content and create zip file
			serializeContentToZip(serializationReport, session,  serializationConfiguration, filePath, filename, contentObjectCriteria);


		}
		catch (CmsException e) {
			addErrorToReport(serializationReport, e);
			throw e;
		}
		catch (Exception e) {
			addErrorToReport(serializationReport, e);
			throw new CmsException(e);
		}
		finally{

			if (logger.isDebugEnabled()){
				logger.debug("Serialization Type {} \n\t Total number of completed serialized entities {} \n\tObjects {}/{} \n\tTopics {}/{} \n\tTaxonomies {}/{}" +
						"\n\tRepository users {}/{} \n\tSpaces {}/{} ) to {} took {}",
						new Object[]{
							CmsEntityType.OBJECT.toString(),
							((SerializationReportImpl)serializationReport).getCountOfCompletedSerializedEntities(),
							serializationReport.getNumberOfCompletedSerializedObjects(),serializationReport.getTotalNumberOfObjects(),
							serializationReport.getNumberOfCompletedSerializedTopics(),serializationReport.getTotalNumberOfTopics(),
							serializationReport.getNumberOfCompletedSerializedTaxonomies(),serializationReport.getTotalNumberOfTaxonomies(),
							serializationReport.getNumberOfCompletedSerializedRepositoryUsers(),serializationReport.getTotalNumberOfRepositoryUsers(),
							serializationReport.getNumberOfCompletedSerializedSpaces(),serializationReport.getTotalNumberOfSpaces(),
							filename,
							DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS")
				}
				);
			}
		}
	}
	@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRES_NEW)
	public void serialize(CmsEntityType entityType, String filePath, String filename, AstroboaClientContext clientContext, SerializationReport serializationReport, 
			SerializationConfiguration serializationConfiguration){

		long start = System.currentTimeMillis();
		
		initialize(entityType, clientContext);

		try{

			//Register client context 
			AstroboaClientContextHolder.registerClientContext(clientContext, true);

			//Get Jcr Session
			Session session = getSession();

			//Serialize content and create zip file
			serializeContentToZip(serializationReport, session,  serializationConfiguration, filePath, filename, null);


		}
		catch (CmsException e) {
			addErrorToReport(serializationReport, e);
			throw e;
		}
		catch (Exception e) {
			addErrorToReport(serializationReport, e);
			throw new CmsException(e);
		}
		finally{

			if (logger.isDebugEnabled()){
				logger.debug("Serialization Type {} \n\t Total number of completed serialized entities {} \n\tObjects {}/{} \n\tTopics {}/{} \n\tTaxonomies {}/{}" +
						"\n\tRepository users {}/{} \n\tSpaces {}/{} ) to {} took {}",
						new Object[]{
							entityType.toString(),
							((SerializationReportImpl)serializationReport).getCountOfCompletedSerializedEntities(),
							serializationReport.getNumberOfCompletedSerializedObjects(),serializationReport.getTotalNumberOfObjects(),
							serializationReport.getNumberOfCompletedSerializedTopics(),serializationReport.getTotalNumberOfTopics(),
							serializationReport.getNumberOfCompletedSerializedTaxonomies(),serializationReport.getTotalNumberOfTaxonomies(),
							serializationReport.getNumberOfCompletedSerializedRepositoryUsers(),serializationReport.getTotalNumberOfRepositoryUsers(),
							serializationReport.getNumberOfCompletedSerializedSpaces(),serializationReport.getTotalNumberOfSpaces(),
							filename,
							DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS")
				}
				);
			}

		}
	}
	
	private void addErrorToReport(SerializationReport serializationReport, Exception e) {
		logger.error("",e);
		((SerializationReportImpl)serializationReport).getErrors().add(e.getMessage());
	}

	private void serializeContentToZip(SerializationReport serializationReport, Session session, SerializationConfiguration serializationConfiguration, String filePath, String filename, ContentObjectCriteria contentObjectCriteria) throws Exception {
		
		File zipFile = createSerializationFile(filePath, filename);

		OutputStream out = null;
		ZipArchiveOutputStream os = null; 

		try{
			out = new FileOutputStream(zipFile); 
			os = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream("zip", out);

			os.setFallbackToUTF8(true);

			addContentToZip(os, serializationReport, session, serializationConfiguration, filename, contentObjectCriteria);
			
			os.finish(); 
			os.close(); 
		}
		catch(Exception e){
			serializationReport.getErrors().add(e.getMessage());
			
			throw e;
		}
		finally{

			if (out != null){
				org.apache.commons.io.IOUtils.closeQuietly(out);
				
				out = null;
			}

			if (os != null){
				org.apache.commons.io.IOUtils.closeQuietly(os);
				
				os = null;
			}
			
			if (CollectionUtils.isNotEmpty(serializationReport.getErrors())){
				//Delete zipfile
				if (zipFile != null && zipFile.exists()){
					zipFile.delete();
				}
			}
			
			zipFile = null;
			
		}


		
	}

	@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRED)
	public void serializeNodesAsResourceCollection(NodeIterator nodeIterator, OutputStream os, CmsCriteria cmsCriteria, FetchLevel fetchLevel, 
			SerializationConfiguration serializationConfiguration, int totalCount){
		
		Serializer serializer = null;
		
		Session session = null;
		try{
			session = getSession();
			
			serializer = new Serializer(os, cmsRepositoryEntityUtils, session, serializationConfiguration);

			if (cmsCriteria instanceof ContentObjectCriteria){
				entityTypeToSerialize = CmsEntityType.OBJECT;
				serializer.setPropertyPathsWhoseValuesWillBeIncludedInTheSerialization(((ContentObjectCriteria)cmsCriteria).getPropertyPathsWhichWillBePreLoaded());
				serializer.useTheSameNameForAllObjects(cmsCriteria.getRenderProperties().areSerializeContentObjectsUsingTheSameNameAsCollectionItemName());
			}
			else if (cmsCriteria instanceof TopicCriteria){
				entityTypeToSerialize = CmsEntityType.TOPIC;
			}
			else if (cmsCriteria instanceof TaxonomyCriteria){
				entityTypeToSerialize = CmsEntityType.TAXONOMY;
			}
			else if (cmsCriteria instanceof SpaceCriteria){
				entityTypeToSerialize = CmsEntityType.SPACE;
			}
			else if (cmsCriteria instanceof RepositoryUserCriteria){
				entityTypeToSerialize = CmsEntityType.REPOSITORY_USER;
			}

			serializer.setDefinitionServiceDao(definitionServiceDao);
			serializer.start();

			createRootElement(serializer, CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME, serializationConfiguration.getResourceRepresentationType(), cmsCriteria.getLimit() != 0 && nodeIterator.getSize() > 0);

			addResourceContext(cmsCriteria.getOffset(), cmsCriteria.getLimit(), totalCount, serializer);

			serializeResourceCollection(nodeIterator, serializer, cmsCriteria, fetchLevel, serializationConfiguration.getResourceRepresentationType());

			serializer.closeEntity(CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME);

			serializer.end();
		}
		catch (CmsException e) {
			logger.error("Error thrown during serialization of the results of the query "+cmsCriteria.getXPathQuery()
					+". JCR session instance "+ session+". CmsException is rethrown", e);
			throw e;
		}
		catch (Exception e) {
			logger.error("Error thrown during serialization of the results of the query "+cmsCriteria.getXPathQuery()
					+". JCR session instance "+session+". A CmsException is rethrown.", e);
			throw new CmsException(e);
		}
		finally {
			serializer = null;
		}

	}

	private void serializeResourceCollection(NodeIterator nodeIterator, Serializer serializer, CmsCriteria cmsCriteria, FetchLevel fetchLevel, ResourceRepresentationType<?>  resourceRepresentationType) throws Exception {

		if (resourceRepresentationType == null){
			resourceRepresentationType = ResourceRepresentationType.XML; 
		}
		
		serializer.openEntityWithNoAttributes(CmsConstants.RESOURCE_COLLECTION);

		if (cmsCriteria.getLimit() != 0){ // 0 denotes that no resource will be serialized
				
			boolean nodeIteratorIsCmsScoreNodeIterator = nodeIterator instanceof CmsScoreNodeIterator;

			while (nodeIterator.hasNext()){

				Node nextNode = null;

				if (nodeIteratorIsCmsScoreNodeIterator){
					nextNode =  ((CmsScoreNodeIterator)nodeIterator).nextCmsScoreNode().getJcrNode();
				}
				else{
					nextNode = nodeIterator.nextNode();
				}

				//Special cases.
				//In cases of complex queries, primary nodes (contentObject, topic, space, taxonomy, repositoryUser)
				//may not be returned. In these case we must locate the primary node to serialize
				if (CmsBuiltInItem.Localization.getJcrName().equals(nextNode.getName())){
					//Node represents Localization node of a Topic or a Space
					//Serialize its parent
					nextNode = nextNode.getParent();
				}
				else if (cmsCriteria instanceof ContentObjectCriteria && 
						! nextNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
					nextNode = contentObjectRenderer.retrieveContentObjectNodeToBeUsedForRendering(nextNode, 
							cmsCriteria.getRenderProperties());
				}

				serializer.serializeResourceCollectionItem(nextNode, fetchLevel, false);
			}
		}
			
		serializer.closeEntity(CmsConstants.RESOURCE_COLLECTION);

	}

	private void addResourceContext(int offset, int limit, long totalResourceCount, Serializer serializer)
			throws Exception {

		//Follow the order in astroboa-api.xsd
		serializer.writeAttribute(CmsConstants.TOTAL_RESOURCE_COUNT, String.valueOf(totalResourceCount));
		
		if (offset > -1){
			serializer.writeAttribute(CmsConstants.OFFSET, String.valueOf(offset));
		}
		
		if (limit > -1){
			serializer.writeAttribute(CmsConstants.LIMIT, String.valueOf(limit));
		}
		
	}
	
	private void addContentToZip(ZipArchiveOutputStream os, SerializationReport serializationReport, Session session, SerializationConfiguration serializationConfiguration, String filename, 
			ContentObjectCriteria contentObjectCriteria) throws IOException,
			Exception, SAXException, RepositoryException, PathNotFoundException {
		
		long start = System.currentTimeMillis();

		Serializer serializer = null;
		try{
			//Create entry
			os.putArchiveEntry(new ZipArchiveEntry(filename+".xml"));

			serializer = new Serializer(os, cmsRepositoryEntityUtils, session, serializationConfiguration);
			serializer.setSerializationReport(serializationReport);

			//Create document
			serializer.start();

			//Create root element
			createRootElement(serializer, CmsConstants.REPOSITORY_PREFIXED_NAME, ResourceRepresentationType.XML, 
					entityTypeToSerialize != null &&  (entityTypeToSerialize == CmsEntityType.OBJECT || entityTypeToSerialize == CmsEntityType.REPOSITORY));

			switch (entityTypeToSerialize) {
			case OBJECT:
				if (contentObjectCriteria != null){
					serializeObjectsAndTheirDependencies(contentObjectCriteria, serializer, session, serializationReport);
				}
				else{
					serializeAllNodesRepresentingObjects(serializer, session, serializationReport);
				}
				break;
			case REPOSITORY_USER:
				serializeAllNodesRepresentingRepositoryUsers(serializer, session, serializationReport);
				break;
			case TAXONOMY:
				serializeAllNodesRepresentingTaxonomies(serializer, session, serializationReport);
				break;
			case ORGANIZATION_SPACE:
				serializeNodeRepresentingOrganizationSpace(serializer, session, serializationReport);
				break;
			case REPOSITORY:
				serializeAllNodesRepresentingRepositoryUsers(serializer, session, serializationReport);
				serializeAllNodesRepresentingTaxonomies(serializer, session, serializationReport);
				serializeAllNodesRepresentingObjects(serializer, session, serializationReport);
				serializeNodeRepresentingOrganizationSpace(serializer, session,serializationReport);
				break;

			default:
				break;
			}

			serializer.closeEntity(CmsConstants.REPOSITORY_PREFIXED_NAME);

			//End document
			serializer.end();

			//Close archive entry
			os.closeArchiveEntry();

			logger.debug("Added content toy zip in {}", DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS"));

		}
		finally{
			serializer = null;
		}
	}

	private void serializeObjectsAndTheirDependencies(ContentObjectCriteria contentObjectCriteria, Serializer serializer,Session session, SerializationReport serializationReport) throws Exception {
		
		serializer.setDefinitionServiceDao(definitionServiceDao);
		
		CmsQueryResult cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, contentObjectCriteria, false);
		
		CmsScoreNodeIterator nodeIterator = new CmsScoreNodeIteratorUsingJcrRangeIterator(cmsQueryResult.getRowIterator());
		
		if (contentObjectCriteria.getLimit() != 0){ // 0 denotes that no resource will be serialized
			
			Map<String, Node> repositoryUserNodes = new HashMap<String, Node>();
			Map<String, Node> taxonomyNodes = new HashMap<String, Node>();
			Map<String, Node> objectNodes = new HashMap<String, Node>();
			
			//We use a list because we serialize all topic hierarchy up to its taxonomy
			//Therefore we must serialize all root topics and then all first level topics etc. 
			Map<Integer, Map<String, Node>> topicNodesStack = new TreeMap<Integer, Map<String,Node>>();
			
			while (nodeIterator.hasNext()){

				Node objectNode = retrieveNextObjectNode(contentObjectCriteria,nodeIterator);

				findObjectDependencies(objectNode, repositoryUserNodes, taxonomyNodes, topicNodesStack,objectNodes, session);
				
				objectNodes.put(cmsRepositoryEntityUtils.getCmsIdentifier(objectNode), objectNode);
			}
			
			//Set total numbers first and then serialize
			if (!repositoryUserNodes.isEmpty()){
				((SerializationReportImpl)serializationReport).setTotalNumberOfRepositoryUsersToBeSerialized(repositoryUserNodes.size());
				((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized(repositoryUserNodes.size()); //Each user has a folksonomy
				((SerializationReportImpl)serializationReport).setTotalNumberOfSpacesToBeSerialized(repositoryUserNodes.size()); //Each user has one root private space
			}
			
			if (!taxonomyNodes.isEmpty()){
				//If at least one repositoryUser is serialized then at least one folksonomy is serialized as well
				((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized(
						serializationReport.getTotalNumberOfTaxonomies()+taxonomyNodes.size());
			}
			
			if (!topicNodesStack.isEmpty()){
				TopicCounterClosure topicCounter = new TopicCounterClosure();
				
				CollectionUtils.forAllDo(topicNodesStack.values(), topicCounter);
				
				((SerializationReportImpl)serializationReport).setTotalNumberOfTopicsToBeSerialized(topicCounter.getTotalNumberOfTopicsToSerialize());
			}
			
			if (!objectNodes.isEmpty()){
				((SerializationReportImpl)serializationReport).setTotalNumberOfObjectsToBeSerialized(objectNodes.size());
			}

			
			//Serialize all nodes in THIS ORDER
			if (!repositoryUserNodes.isEmpty()){
				serializeNodesRepresentingRepositoryUsers(repositoryUserNodes, serializer, session);
			}
			
			if (!taxonomyNodes.isEmpty()){
				serializeNodesRepresentingTaxonomies(taxonomyNodes, serializer, session);
			}
			
			if (!topicNodesStack.isEmpty()){
				serializeNodesRepresentingTopics(topicNodesStack, serializer, session);
			}
			
			if (!objectNodes.isEmpty()){
				serializeNodesRepresentingObjects(objectNodes, serializer, session);
			}
		}
	}
	
	
	private void serializeNodesRepresentingTaxonomies(
			Map<String, Node> taxonomyNodes, Serializer serializer,
			Session session) throws Exception {
		
		serializer.openEntityWithNoAttributes(CmsConstants.TAXONOMIES_ELEMENT_NAME);
		
		for (Node taxonomyNode : taxonomyNodes.values()){
			serializer.serializeTaxonomyNode(taxonomyNode, FetchLevel.ENTITY, false, false);
		}
		
		serializer.closeEntity(CmsConstants.TAXONOMIES_ELEMENT_NAME);

		
	}
	private void serializeNodesRepresentingObjects(
			Map<String, Node> objectNodes, Serializer serializer,
			Session session) throws Exception {

		serializer.openEntityWithNoAttributes(CmsConstants.OBJECTS_ELEMENT_NAME);
		
		for (Node objectNode : objectNodes.values()){
			serializer.serializeContentObjectNode(objectNode, false);
		}
		
		serializer.closeEntity(CmsConstants.OBJECTS_ELEMENT_NAME);

		
	}
	private void serializeNodesRepresentingTopics(Map<Integer, Map<String, Node>> topicNodes,
			Serializer serializer, Session session) throws Exception {

		serializer.openEntityWithNoAttributes(CmsConstants.TOPICS_ELEMENT_NAME);
		
		Set<Integer> keySet = topicNodes.keySet();
		for (Integer level : keySet){
			
			Map<String, Node> topicNodesForLevel = topicNodes.get(level);
			
			for (Node topicNode : topicNodesForLevel.values()){
				serializer.serializeTopicNode(topicNode, FetchLevel.ENTITY, true, false, false, false);
			}
		}
		
		serializer.closeEntity(CmsConstants.TOPICS_ELEMENT_NAME);

		
	}
	private void serializeNodesRepresentingRepositoryUsers(
			Map<String, Node> repositoryUserNodes, Serializer serializer,
			Session session) throws Exception {
		
		serializer.openEntityWithNoAttributes(CmsConstants.REPOSITORY_USERS_ELEMENT_NAME);
		
		for (Node repositoryUserNode : repositoryUserNodes.values()){
			serializer.serializeNode(repositoryUserNode, false, FetchLevel.ENTITY, false, false);
		}
		
		serializer.closeEntity(CmsConstants.REPOSITORY_USERS_ELEMENT_NAME);
		
	}
	
	/*
	 * Try to identify property values which are reference to other nodes
	 * without the use of a definition.
	 * 
	 * To do this, search only the properties of type STRING
	 * and check if their value is a UUID 
	 */
	private void findObjectDependencies(Node node,
			Map<String, Node> repositoryUserNodes, Map<String, Node> taxonomyNodes,
			Map<Integer, Map<String, Node>> topicNodes, Map<String, Node> objectNodes, Session session) throws RepositoryException {
		
		findDependenciesInProperties(node, repositoryUserNodes, taxonomyNodes, topicNodes, objectNodes, session);
		
		NodeIterator childNodes = node.getNodes();
		
		if (childNodes != null && childNodes.getSize() > 0){
			while (childNodes.hasNext()){
				findObjectDependencies(childNodes.nextNode(), repositoryUserNodes, taxonomyNodes, topicNodes, objectNodes, session);
			}
		}
		
	}
	private void findDependenciesInProperties(Node node,
			Map<String, Node> repositoryUserNodes,  Map<String, Node> taxonomyNodes,
			Map<Integer, Map<String, Node>> topicNodes, Map<String, Node> objectNodes,
			Session session) throws RepositoryException, ValueFormatException {
		
		
		PropertyIterator properties = node.getProperties();
		
		if (properties != null && properties.getSize() > 0){
			while (properties.hasNext()){
				
				Property property = properties.nextProperty();
				
				if (property.getType() != PropertyType.STRING){
					continue;
				}
				
				String propertyName = property.getName();
				
				if (propertyName.startsWith(JcrNamespaceConstants.JCR_PREFIX+":") ||
					propertyName.startsWith(JcrNamespaceConstants.MIX_PREFIX+":") ||
					propertyName.startsWith(JcrNamespaceConstants.NT_PREFIX+":")){
					continue;
				}
				
				Value[] values = null;
				if (property.getDefinition().isMultiple()){
					values = property.getValues();
				}
				else{
					values = new Value[]{property.getValue()};
				}
				
				for (Value value : values){
					String valueAsString = value.getString();
					
					if (CmsConstants.UUIDPattern.matcher(valueAsString).matches()){
						
						Node referencedNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntityId(session, valueAsString);
						
						if (referencedNode != null){
							//Determine node type
							if (referencedNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()) && ! objectNodes.containsKey(valueAsString)){
								objectNodes.put(valueAsString, referencedNode);
							}
							else if (referencedNode.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
								
								List<Node> topicHierarchy = new ArrayList<Node>();
								
								Node topicNode = referencedNode;
								
								while (topicNode != null && topicNode.isNodeType(CmsBuiltInItem.Topic.getJcrName())){
									topicHierarchy.add(topicNode);
									topicNode = topicNode.getParent();
								}
								
								//Normally the last node always corresponds to a taxonomy
								if (topicNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
									taxonomyNodes.put(cmsRepositoryEntityUtils.getCmsIdentifier(topicNode), topicNode);
								}
								
								//Reverse list so that root topic is the first
								Collections.reverse(topicHierarchy);
								for (int i=0; i<topicHierarchy.size(); i++){
									
									Node topicHierarchyNode = topicHierarchy.get(i);
									
									if (! topicNodes.containsKey(i)){
										topicNodes.put(i, new HashMap<String,Node>());
									}
									
									topicNodes.get(i).put(valueAsString, topicHierarchyNode);
									
								}
								
							}
							else if (referencedNode.isNodeType(CmsBuiltInItem.RepositoryUser.getJcrName()) && ! repositoryUserNodes.containsKey(valueAsString)){
								repositoryUserNodes.put(valueAsString, referencedNode);
							}
						}
						
					}
				}
				
			}
		}
	}
	
	
	private Node retrieveNextObjectNode(
			ContentObjectCriteria contentObjectCriteria,
			CmsScoreNodeIterator nodeIterator) throws RepositoryException,
			ItemNotFoundException, AccessDeniedException {
		Node nextNode = nodeIterator.nextCmsScoreNode().getJcrNode();

		//Special cases.
		//In cases of complex queries, primary nodes (contentObject, topic, space, taxonomy, repositoryUser)
		//may not be returned. In these case we must locate the primary node to serialize
		if (CmsBuiltInItem.Localization.getJcrName().equals(nextNode.getName())){
			//Node represents Localization node of a Topic or a Space
			//Serialize its parent
			nextNode = nextNode.getParent();
		}
		else if (! nextNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
			nextNode = contentObjectRenderer.retrieveContentObjectNodeToBeUsedForRendering(nextNode, contentObjectCriteria.getRenderProperties());
		}
		return nextNode;
	}
	
	
	
	private void createRootElement(Serializer serializer, String rootEntityName, ResourceRepresentationType<?>  resourceRepresentationType, boolean createObjectTypeNamespaces)	throws Exception {
		
		AttributesImpl atts = generateRootElementAttributes(serializer,rootEntityName, resourceRepresentationType, createObjectTypeNamespaces);
		
		serializer.openEntity(rootEntityName, atts);
	}

	private AttributesImpl generateRootElementAttributes(
			Serializer serializer, String rootEntityName, ResourceRepresentationType<?>  resourceRepresentationType, boolean createObjectTypeNamespaces) {
		AttributesImpl atts = new AttributesImpl();
		
		//Add repository identifier
		if (StringUtils.equals(CmsConstants.REPOSITORY_PREFIXED_NAME, rootEntityName)){
			IOUtils.addAttribute(atts, ItemUtils.createSimpleItem("id"), clientContext.getRepositoryContext().getCmsRepository().getId());
			IOUtils.addAttribute(atts, ItemUtils.createSimpleItem(CmsConstants.REPOSITORY_SERIALIZATION_CREATION_DATE_ATTRIBUTE_NAME), generateXMLRepresentationOfCalendar(Calendar.getInstance()));


		}
		
		//Add Xsi namespace declaration
		if (resourceRepresentationType == null || ResourceRepresentationType.XML.equals(resourceRepresentationType)){
			IOUtils.addXsiNamespaceDeclarationAttribute(atts);

			StringBuilder xsiSchemaLocations = new StringBuilder();

			//Add Astroboa model namespace
			IOUtils.addAstroboaModelNamespaceDeclarationAttribute(atts);
			xsiSchemaLocations.append(BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI);
			xsiSchemaLocations.append(" ");
			xsiSchemaLocations.append(SchemaUtils.buildSchemaLocationForAstroboaModelSchemaAccordingToActiveClient());
			xsiSchemaLocations.append(" ");
			
			//Add Astroboa Api namespace
			if (StringUtils.equals(CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME,rootEntityName) ||
					StringUtils.equals(CmsConstants.REPOSITORY_PREFIXED_NAME,rootEntityName)){

				IOUtils.addAstroboaApiNamespaceDeclarationAttribute(atts);
				xsiSchemaLocations.append(BetaConceptNamespaceConstants.ASTROBOA_API_URI);
				xsiSchemaLocations.append(" ");
				xsiSchemaLocations.append(SchemaUtils.buildSchemaLocationForAstroboaApiSchemaAccordingToActiveClient());
				xsiSchemaLocations.append(" ");
			}

			if (createObjectTypeNamespaces && (entityTypeToSerialize == null || CmsEntityType.REPOSITORY == entityTypeToSerialize || CmsEntityType.OBJECT == entityTypeToSerialize)){
				Map<String, String> prefixesPerType = new HashMap<String, String>();

				createXSISchemaLocationValueForAllDefinitions(atts, xsiSchemaLocations, prefixesPerType);

				serializer.setPrefixesPerType(prefixesPerType);
			}

			IOUtils.addXsiSchemaLocationAttribute(atts, xsiSchemaLocations.toString());
		}
		
		return atts;
	}

	private void initialize(CmsEntityType entityType, AstroboaClientContext clientContext) {
		if (clientContext == null){
			throw new CmsException("Astroboa client context is not provided. Serialization failed");
		}
		else{
			this.clientContext = clientContext;
		}

		if (entityType == null){
			this.entityTypeToSerialize = CmsEntityType.REPOSITORY;
		}
		else{
			this.entityTypeToSerialize = entityType;
		}
	}


	private File createSerializationFile(String serializationPath, String filename) throws Exception {
		//Filename of zip file
		//File will be store inside repository's home directory
		String repositoryHomeDir = clientContext.getRepositoryContext().getCmsRepository().getRepositoryHomeDirectory();

		StringBuilder serializationFilePath = new StringBuilder();
		
		serializationFilePath.append(repositoryHomeDir)
					.append(File.separator)
					.append(CmsConstants.SERIALIZATION_DIR_NAME)
					.append(File.separator)
					.append(serializationPath.replaceAll("/", File.separator))
					.append(File.separator)
					.append(filename)
					.append(ZIP);
		
		File zipFile = new File(serializationFilePath.toString());

		if (!zipFile.exists()){
			FileUtils.touch(zipFile);
		}

		return zipFile;
	}

	private void serializeNodeRepresentingOrganizationSpace(Serializer serializer, Session session, SerializationReport serializationReport) throws Exception{

		Node organizationSpaceNode = JcrNodeUtils.getOrganizationSpaceNode(session);
		
		setTotalNumberOfAllSpacesOfOrganizationSpace(organizationSpaceNode, serializer, session, serializationReport);

		serializer.serializeSpaceNode(organizationSpaceNode, FetchLevel.FULL, false, false, false);
	}
	
	private void setTotalNumberOfAllSpacesOfOrganizationSpace(
			Node organizationSpaceNode,
			Serializer serializer,
			Session session, SerializationReport serializationReport)
			throws RepositoryException, Exception {
		
		SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
		spaceCriteria.addAncestorSpaceIdEqualsCriterion(getOrganizationSpaceIdentifier(organizationSpaceNode,session));
		
		spaceCriteria.setOffsetAndLimit(0, 0);
		
		CmsQueryResult outcome = cmsQueryHandler.getNodesFromXPathQuery(session, spaceCriteria);
		
		if (outcome!=null && outcome.getTotalRowCount() > 0){
			((SerializationReportImpl)serializationReport).setTotalNumberOfSpacesToBeSerialized(outcome.getTotalRowCount()+1); //Add organization space
		}
		else{
			((SerializationReportImpl)serializationReport).setTotalNumberOfSpacesToBeSerialized(1); //Organization Space is a Space as well
		}
	}
	private String getOrganizationSpaceIdentifier(Node organizationSpaceNode, Session session)
			throws RepositoryException {
		
		if (organizationSpaceNode == null){
			organizationSpaceNode = JcrNodeUtils.getOrganizationSpaceNode(session);
		}
		
		return cmsRepositoryEntityUtils.getCmsIdentifier(organizationSpaceNode);
	}

	private void serializeAllNodesRepresentingTaxonomies(Serializer serializer, Session session, SerializationReport serializationReport) throws Exception {

		serializer.openEntityWithNoAttributes(CmsConstants.TAXONOMIES_ELEMENT_NAME);

		Node taxonomyRootNode = JcrNodeUtils.getTaxonomyRootNode(session);

		setTotalNumberOfAllTaxonomiesAndTheirTopics(taxonomyRootNode, serializer, session, serializationReport);
		
		serializer.serializeChildrenOfRootNode(taxonomyRootNode);
		
		serializer.closeEntity(CmsConstants.TAXONOMIES_ELEMENT_NAME);
	}
	
	private void setTotalNumberOfAllTaxonomiesAndTheirTopics(
			Node taxonomyRootNode, Serializer serializer, Session session,
			SerializationReport serializationReport)
			throws RepositoryException, Exception {
		
		NodeIterator taxonomyNodes = taxonomyRootNode.getNodes();
		
		if (taxonomyNodes!=null && taxonomyNodes.getSize() > 0){
			((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized((int) taxonomyNodes.getSize());
			
			//Also we must specify the number of topics along with the taxonomies
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.setOffsetAndLimit(0, 0);
			
			CmsQueryResult outcome = cmsQueryHandler.getNodesFromXPathQuery(session, topicCriteria);
			
			if (outcome!=null && outcome.getTotalRowCount() > 0){
				((SerializationReportImpl)serializationReport).setTotalNumberOfTopicsToBeSerialized(outcome.getTotalRowCount());
			}
			else{
				((SerializationReportImpl)serializationReport).setTotalNumberOfTopicsToBeSerialized(0);
			}
		}
		else{
			((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized(0);
			((SerializationReportImpl)serializationReport).setTotalNumberOfTopicsToBeSerialized(0);
		}
		
	}

	private void serializeAllNodesRepresentingRepositoryUsers(Serializer serializer, Session session, SerializationReport serializationReport) throws Exception {

		serializer.openEntityWithNoAttributes(CmsConstants.REPOSITORY_USERS_ELEMENT_NAME);
		
		Node repositoryUserRootNode = JcrNodeUtils.getRepositoryUserRootNode(session);

		setTotalNumberOfAllRepositoryUsersTheirFolksonomiesAndTheirSpaces(repositoryUserRootNode, serializer, session, serializationReport);
		
		serializer.serializeChildrenOfRootNode(repositoryUserRootNode);
		
		serializer.closeEntity(CmsConstants.REPOSITORY_USERS_ELEMENT_NAME);
	}
	
	
	private int getNumberOfRepositoryUsersPrivateSpaces(List<String> repositoryUserIds, Session session) throws RepositoryException{
		
		//Each repository user has a root private space which is the parent for all other
		//private spaces. This space does not match the criteria set in the following code
		//and thus must be calculated separately
		int numberOfPrivateRootSpaces = 0;
		
		SpaceCriteria spaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
		
		SpaceCriteria organizationSpaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
		organizationSpaceCriteria.addIdNotEqualsCriterion(getOrganizationSpaceIdentifier(null, session));
		spaceCriteria.setAncestorCriteria(organizationSpaceCriteria);

		if (repositoryUserIds != null && ! repositoryUserIds.isEmpty()){
			spaceCriteria.addOwnerIdsCriterion(QueryOperator.EQUALS, repositoryUserIds, Condition.OR);
			numberOfPrivateRootSpaces = repositoryUserIds.size();
		}
		else{
			//All private spaces are loaded, thus the number of root private spaces
			//equals to the number of repository users
			numberOfPrivateRootSpaces = (int) JcrNodeUtils.getRepositoryUserRootNode(session).getNodes(CmsBuiltInItem.RepositoryUser.getJcrName()).getSize();
		}
		
		spaceCriteria.setOffsetAndLimit(0, 0);
		
		CmsQueryResult outcome = cmsQueryHandler.getNodesFromXPathQuery(session, spaceCriteria);
		
		if (outcome!=null && outcome.getTotalRowCount() > 0){
			return outcome.getTotalRowCount()+numberOfPrivateRootSpaces;
		}
		else{
			return numberOfPrivateRootSpaces;
		}
	
	}
	
	private void setTotalNumberOfAllRepositoryUsersTheirFolksonomiesAndTheirSpaces(
			Node repositoryUserRootNode,
			Serializer serializer,
			Session session, SerializationReport serializationReport)
			throws RepositoryException, Exception {

		//Set number of user spaces
		int numberOfRepositoryUserPrivateSpaces = getNumberOfRepositoryUsersPrivateSpaces(null, session);
		((SerializationReportImpl)serializationReport).setTotalNumberOfSpacesToBeSerialized(numberOfRepositoryUserPrivateSpaces);
		
		//Now set the number of repository users to be serialized 
		NodeIterator repositoryUserNodes = repositoryUserRootNode.getNodes(CmsBuiltInItem.RepositoryUser.getJcrName());
		
		if (repositoryUserNodes!=null && repositoryUserNodes.getSize() > 0){
			((SerializationReportImpl)serializationReport).setTotalNumberOfRepositoryUsersToBeSerialized((int) repositoryUserNodes.getSize());
			//Each user has one folksonomy
			((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized((int) repositoryUserNodes.getSize());
		}
		else{
			((SerializationReportImpl)serializationReport).setTotalNumberOfRepositoryUsersToBeSerialized(0);
			((SerializationReportImpl)serializationReport).setTotalNumberOfTaxonomiesToBeSerialized(0);
		}
		
	}

	private void serializeAllNodesRepresentingObjects(Serializer serializer, Session session, SerializationReport serializationReport) throws Exception {

		serializer.openEntityWithNoAttributes(CmsConstants.OBJECTS_ELEMENT_NAME);
		
		serializer.setDefinitionServiceDao(definitionServiceDao);
		
		setTotalNumberOfAllObjects(serializer, session, serializationReport);

		Node objectRootNode = JcrNodeUtils.getContentObjectRootNode(session);
		
		serializer.serializeChildrenOfRootNode(objectRootNode);
		
		serializer.closeEntity(CmsConstants.OBJECTS_ELEMENT_NAME);
	}
	
	private void setTotalNumberOfAllObjects(Serializer serializer,
			Session session, SerializationReport serializationReport)
			throws RepositoryException, Exception {
		
		
		ContentObjectCriteria objectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		objectCriteria.setOffsetAndLimit(0, 0);
		
		CmsQueryResult outcome = cmsQueryHandler.getNodesFromXPathQuery(session, objectCriteria);
		
		if (outcome!=null && outcome.getTotalRowCount() > 0){
			((SerializationReportImpl)serializationReport).setTotalNumberOfObjectsToBeSerialized(outcome.getTotalRowCount());
		}
		else{
			((SerializationReportImpl)serializationReport).setTotalNumberOfObjectsToBeSerialized(0);
		}
		
	}

	private void createXSISchemaLocationValueForAllDefinitions(AttributesImpl attributes, StringBuilder xsiSchemaLocations, Map<String, String> prefixesPerType){
		
		Map<QName, String> locationMap = definitionServiceDao.getLocationURLForDefinitions();
		
		if (MapUtils.isNotEmpty(locationMap)){
			List<String> processedURIs = new ArrayList<String>();
			
			for (Entry<QName, String> location : locationMap.entrySet()){
				final String namespaceURI = location.getKey().getNamespaceURI();
				
				if (!processedURIs.contains(namespaceURI)){
					xsiSchemaLocations.append(namespaceURI);
					xsiSchemaLocations.append(" ");
					xsiSchemaLocations.append(location.getValue());
					xsiSchemaLocations.append(" ");
				
					IOUtils.addAttribute(attributes, XMLConstants.XMLNS_ATTRIBUTE+":"+location.getKey().getPrefix(), location.getKey().getNamespaceURI());
					
					processedURIs.add(namespaceURI);
				}
				
				prefixesPerType.put(location.getKey().getLocalPart(), location.getKey().getPrefix());
			}
		}
		
		
	}
	@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRED)
	public void serializeNode(Node nodeRepresentingEntity, OutputStream os, CmsEntityType entityTypeToSerialize,
			SerializationConfiguration serializationConfiguration, List<String> propertyPathsWhoseValuesWillBeIncludedInTheSerialization, 
			FetchLevel fetchLevel, boolean nodeRepresentsRootElement) throws Exception {
		
		this.entityTypeToSerialize = entityTypeToSerialize;
		
		Serializer serializer = null;
		
		try{
			serializer = new Serializer(os, cmsRepositoryEntityUtils, getSession(), serializationConfiguration);
			serializer.setDefinitionServiceDao(definitionServiceDao);
			serializer.setPropertyPathsWhoseValuesWillBeIncludedInTheSerialization(propertyPathsWhoseValuesWillBeIncludedInTheSerialization);

			serializer.start(generateRootElementAttributes(serializer, null, serializationConfiguration.getResourceRepresentationType(), true));

			if (CmsEntityType.TOPIC == entityTypeToSerialize){
				serializer.serializeTopicNode(nodeRepresentingEntity, fetchLevel, true, false, nodeRepresentsRootElement, false);
			}
			else if (CmsEntityType.TAXONOMY == entityTypeToSerialize){
				serializer.serializeTaxonomyNode(nodeRepresentingEntity, fetchLevel, false, nodeRepresentsRootElement);
			}
			else if (CmsEntityType.OBJECT == entityTypeToSerialize){
				serializer.serializeContentObjectNode(nodeRepresentingEntity, false);
			}
			else if (CmsEntityType.ORGANIZATION_SPACE == entityTypeToSerialize || CmsEntityType.SPACE == entityTypeToSerialize){
				serializer.serializeSpaceNode(nodeRepresentingEntity, fetchLevel, false, nodeRepresentsRootElement, false);
			}
			else{
				serializer.serializeChildrenOfRootNode(nodeRepresentingEntity);
			}

			serializer.end();
		}
		catch (CmsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			serializer = null;
		}
	}
	
	private String generateXMLRepresentationOfCalendar(Calendar calendar){
		
		if (calendar == null){
			calendar = Calendar.getInstance();
		}
		
		try {
			DatatypeFactory df = DatatypeFactory.newInstance();
			GregorianCalendar gregCalendar = new GregorianCalendar(calendar.getTimeZone());
			gregCalendar.setTimeInMillis(calendar.getTimeInMillis());
			
			return df.newXMLGregorianCalendar(gregCalendar).toXMLFormat();
			
		} catch (DatatypeConfigurationException e) {
			return null;
		}

	}

	private class TopicCounterClosure implements Closure {

		private int totalNumberOfTopicsToSerialize = 0;
		
		@Override
		public void execute(Object input) {
			if (input != null && input instanceof Map){
				totalNumberOfTopicsToSerialize += ((Map)input).size();
			}
		}

		public int getTotalNumberOfTopicsToSerialize() {
			return totalNumberOfTopicsToSerialize;
		}
		
	}
}
