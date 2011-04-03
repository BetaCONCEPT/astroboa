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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

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
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIterator;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.impl.io.SerializationReportImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
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
		CONTENT_OBJECT, 
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

	@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.REQUIRES_NEW)
	public void serialize(CmsEntityType entityType, String filePath, String filename, AstroboaClientContext clientContext, SerializationReport serializationReport, 
			boolean serializeBinaryContent){

		long start = System.currentTimeMillis();
		
		initialize(entityType, clientContext);

		try{

			//Register client context 
			AstroboaClientContextHolder.registerClientContext(clientContext, true);

			//Get Jcr Session
			Session session = getSession();

			//Serialize content and create zip file
			serializeContentToZip(serializationReport, session,  serializeBinaryContent, filePath, filename);


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

			((SerializationReportImpl) serializationReport).setFinished(true);
			
			logger.debug("Serialization {} ({} number of entities, {} repository users, {} taxonomies, " +
					"{} spaces, {} contentObjects ) to {} took {}",
					new Object[]{
						entityType.toString(),
						((SerializationReportImpl)serializationReport).getCountOfSerializedEntities(),
						serializationReport.getNumberOfRepositoryUsersSerialized(),
						serializationReport.getNumberOfTaxonomiesSerialized(),
						serializationReport.getNumberOfSpacesSerialized(),
						serializationReport.getNumberOfObjectsSerialized(),
						filename,
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS")
			}
			);
		}
	}
	
	private void addErrorToReport(SerializationReport serializationReport, Exception e) {
		logger.error("",e);
		((SerializationReportImpl)serializationReport).getErrors().add(e.getMessage());
	}

	private void serializeContentToZip(SerializationReport serializationReport, Session session, boolean serializeBinaryContent, String filePath, String filename) throws Exception {
		
		File zipFile = createSerializationFile(filePath, filename);

		OutputStream out = null;
		ZipArchiveOutputStream os = null; 

		try{
			out = new FileOutputStream(zipFile); 
			os = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream("zip", out);

			os.setFallbackToUTF8(true);

			addContentToZip(os, serializationReport, session, serializeBinaryContent, filename);
			
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
	public void serializeNodesAsResourceCollection(NodeIterator nodeIterator, OutputStream os, CmsCriteria cmsCriteria, FetchLevel fetchLevel, ResourceRepresentationType<?>  resourceRepresentationType, boolean serializeBinaryContent, int totalCount){
		
		Serializer serializer = null;
		
		Session session = null;
		try{
			session = getSession();
			
			serializer = new Serializer(os, cmsRepositoryEntityUtils, session, resourceRepresentationType, cmsCriteria.getRenderProperties().isPrettyPrintEnabled());

			if (cmsCriteria instanceof ContentObjectCriteria){
				entityTypeToSerialize = CmsEntityType.CONTENT_OBJECT;
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
			serializer.serializeBinaryContent(serializeBinaryContent);
			serializer.start();

			createRootElement(serializer, CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME, resourceRepresentationType, cmsCriteria.getLimit() != 0 && nodeIterator.getSize() > 0);

			addResourceContext(cmsCriteria.getOffset(), cmsCriteria.getLimit(), totalCount, serializer);

			serializeResourceCollection(nodeIterator, serializer, cmsCriteria, fetchLevel, resourceRepresentationType);

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
	
	private void addContentToZip(ZipArchiveOutputStream os, SerializationReport serializationReport, Session session, boolean serializeBinaryContent, String filename) throws IOException,
			Exception, SAXException, RepositoryException, PathNotFoundException {
		
		long start = System.currentTimeMillis();

		Serializer serializer = null;
		try{
			//Create entry
			os.putArchiveEntry(new ZipArchiveEntry(filename+".xml"));

			serializer = new Serializer(os, cmsRepositoryEntityUtils, session, ResourceRepresentationType.XML,false);
			serializer.setSerializationReport(serializationReport);
			serializer.serializeBinaryContent(serializeBinaryContent);

			//Create document
			serializer.start();

			//Create root element
			createRootElement(serializer, CmsConstants.REPOSITORY_PREFIXED_NAME, ResourceRepresentationType.XML, 
					entityTypeToSerialize != null &&  (entityTypeToSerialize == CmsEntityType.CONTENT_OBJECT || entityTypeToSerialize == CmsEntityType.REPOSITORY));

			switch (entityTypeToSerialize) {
			case CONTENT_OBJECT:
				serializeAllNodesRepresentingObjects(serializer, session);
				break;
			case REPOSITORY_USER:

				serializeAllNodesRepresentingRepositoryUsers(serializer, session);

				break;
			case TAXONOMY:

				serializeAllNodesRepresentingTaxonomies(serializer, session);

				break;
			case ORGANIZATION_SPACE:
				serializeNodeRepresentingOrganizationSpace(serializer, session);
				break;
			case REPOSITORY:

				serializeAllNodesRepresentingRepositoryUsers(serializer, session);
				serializeAllNodesRepresentingTaxonomies(serializer, session);
				serializeAllNodesRepresentingObjects(serializer, session);
				serializeNodeRepresentingOrganizationSpace(serializer, session);
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

	private void createRootElement(Serializer serializer, String rootEntityName, ResourceRepresentationType<?>  resourceRepresentationType, boolean createContentTypeNamespaces)	throws Exception {
		
		AttributesImpl atts = generateRootElementAttributes(serializer,rootEntityName, resourceRepresentationType, createContentTypeNamespaces);
		
		serializer.openEntity(rootEntityName, atts);
	}

	private AttributesImpl generateRootElementAttributes(
			Serializer serializer, String rootEntityName, ResourceRepresentationType<?>  resourceRepresentationType, boolean createContentTypeNamespaces) {
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
			xsiSchemaLocations.append(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_URI);
			xsiSchemaLocations.append(" ");
			xsiSchemaLocations.append(SchemaUtils.buildSchemaLocationForAstroboaModelSchemaAccordingToActiveClient());
			xsiSchemaLocations.append(" ");
			
			//Add Astroboa Api namespace
			if (StringUtils.equals(CmsConstants.RESOURCE_RESPONSE_PREFIXED_NAME,rootEntityName) ||
					StringUtils.equals(CmsConstants.REPOSITORY_PREFIXED_NAME,rootEntityName)){

				IOUtils.addAstroboaApiNamespaceDeclarationAttribute(atts);
				xsiSchemaLocations.append(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_URI);
				xsiSchemaLocations.append(" ");
				xsiSchemaLocations.append(SchemaUtils.buildSchemaLocationForAstroboaApiSchemaAccordingToActiveClient());
				xsiSchemaLocations.append(" ");
			}


			if (createContentTypeNamespaces && (entityTypeToSerialize == null || CmsEntityType.REPOSITORY == entityTypeToSerialize || CmsEntityType.CONTENT_OBJECT == entityTypeToSerialize)){
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

	private void serializeNodeRepresentingOrganizationSpace(Serializer serializer, Session session) throws Exception{

		serializer.serializeSpaceNode(JcrNodeUtils.getOrganizationSpaceNode(session), FetchLevel.FULL, false, false, false);
	}

	private void serializeAllNodesRepresentingTaxonomies(Serializer serializer, Session session) throws Exception {

		serializer.openEntityWithNoAttributes("taxonomies");
		
		serializer.serializeChildrenOfRootNode(JcrNodeUtils.getTaxonomyRootNode(session));
		
		serializer.closeEntity("taxonomies");
	}

	private void serializeAllNodesRepresentingRepositoryUsers(Serializer serializer, Session session) throws Exception {

		serializer.openEntityWithNoAttributes("repositoryUsers");
		
		serializer.serializeChildrenOfRootNode(JcrNodeUtils.getRepositoryUserRootNode(session));
		
		serializer.closeEntity("repositoryUsers");
	}

	private void serializeAllNodesRepresentingObjects(Serializer serializer, Session session) throws Exception {

		serializer.openEntityWithNoAttributes("contentObjects");
		
		serializer.setDefinitionServiceDao(definitionServiceDao);
		
		serializer.serializeChildrenOfRootNode(JcrNodeUtils.getContentObjectRootNode(session));
		
		serializer.closeEntity("contentObjects");
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

	public void serializeNode(Node nodeRepresentingEntity, OutputStream os, CmsEntityType entityTypeToSerialize,
			ResourceRepresentationType<?>  resourceRepresentationType, List<String> propertyPathsWhoseValuesWillBeIncludedInTheSerialization, FetchLevel fetchLevel, boolean nodeRepresentsRootElement, boolean serializeBinaryContent,
			boolean prettyPrint) throws Exception {
		
		this.entityTypeToSerialize = entityTypeToSerialize;
		
		Serializer serializer = null;
		
		try{
			serializer = new Serializer(os, cmsRepositoryEntityUtils, getSession(), resourceRepresentationType,prettyPrint);
			serializer.setDefinitionServiceDao(definitionServiceDao);
			serializer.setPropertyPathsWhoseValuesWillBeIncludedInTheSerialization(propertyPathsWhoseValuesWillBeIncludedInTheSerialization);
			serializer.serializeBinaryContent(serializeBinaryContent);

			serializer.start(generateRootElementAttributes(serializer, null, resourceRepresentationType, true));

			if (CmsEntityType.TOPIC == entityTypeToSerialize){
				serializer.serializeTopicNode(nodeRepresentingEntity, fetchLevel, true, false, nodeRepresentsRootElement, false);
			}
			else if (CmsEntityType.TAXONOMY == entityTypeToSerialize){
				serializer.serializeTaxonomyNode(nodeRepresentingEntity, fetchLevel, false, nodeRepresentsRootElement);
			}
			else if (CmsEntityType.CONTENT_OBJECT == entityTypeToSerialize){
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
}
