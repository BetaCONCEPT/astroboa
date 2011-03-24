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
package org.betaconceptframework.astroboa.engine.jcr.io.contenthandler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportContext;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportedEntity;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * Main import content handler responsible to import XML.
 * 
 * For JSON import use JsonImportContentHandler
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportContentHandler<T> implements ContentHandler{

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ImportContext importContext;
	
	private StringBuilder elementContent = new StringBuilder();
	
	private Deque<ImportedEntity> cmsRepositoryEntityQueue = new ArrayDeque<ImportedEntity>();
	
	private T importResult;
	
	private Class<T> resultType;
	
	/*
	 * During import some elements may be ignored because they 
	 * have already been processed. This stands for topics or repository users
	 * which may be encountered more than once in side an XML or JSON
	 * 
	 * This variable holds the main element whose content (child elements) are ignored.
	 * Only when this elements is closed, import is back to normal.
	 */
	private String elementNameToBeIgnored = null;
	
	private DatatypeFactory df;

	private Deserializer deserializer;

	
	public ImportContentHandler(Class<T> resultType, Deserializer deserializer){
		this(new ImportContext(), resultType, deserializer);
	}
	
	public ImportContentHandler(ImportContext importContext, Class<T> resultType, Deserializer deserializer){
		this.importContext = importContext;
		this.resultType = resultType;
		this.deserializer = deserializer;
		
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new CmsException(e);
		}
	}
	
	private boolean doNotProcessEvent() {
		return elementNameToBeIgnored != null;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		if (doNotProcessEvent()){
			return;
		}
	
		elementContent.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		elementContent = null;
		
		elementNameToBeIgnored = null;
		
		importContext.dispose();
		
		importContext = null;
		
	}
	
	@Override
	public void startDocument() throws SAXException {
		
		clearElementContent();
		
		elementNameToBeIgnored = null;
		
		cmsRepositoryEntityQueue.clear();
		
		importResult = null;
		
	}

	private void createRootEntity(String uri, String rootElementName, Attributes atts) throws SAXException {
		
		if (Topic.class.isAssignableFrom(resultType)){
			importResult = (T) createNewTopic(atts, rootElementName);
			
		}
		else if (Taxonomy.class.isAssignableFrom(resultType)){
			importResult = (T) createNewTaxonomy(atts, rootElementName);
			
		}
		else if (RepositoryUser.class.isAssignableFrom(resultType)){
			importResult = (T) createNewRepositoryUser(atts, rootElementName);
			
		}
		else if (Repository.class.isAssignableFrom(resultType)){
			importResult = (T) new Repository();
			
		}
		else if (Space.class.isAssignableFrom(resultType)){
			importResult = (T) createNewSpace(atts, rootElementName);
			
		}
		else if (ContentObject.class.isAssignableFrom(resultType)){
			importResult = (T) createNewContentObject(atts, rootElementName, uri);

		}
		else if (List.class.isAssignableFrom(resultType)){
			importResult = (T) new ArrayList();

		}
		else{
			throw new SAXException("Unsupported type of imported Entity "+ resultType.getName());
		}	
		
		pushEntity(rootElementName, importResult, atts);
		
		elementNameToBeIgnored = null;
	}
	
	private void importAttributes(Attributes atts) throws SAXException {
		
		if (atts != null && atts.getLength() >0){
			for (int i=0;i<atts.getLength();i++){
				//Ignore attributes with specific namespaces
				String uri  = atts.getURI(i);
				
				if (StringUtils.equals(uri, XMLConstants.XMLNS_ATTRIBUTE_NS_URI) ||
						StringUtils.equals(uri, XMLConstants.XML_NS_URI) ||
						StringUtils.equals(uri, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI) ||
						StringUtils.equals(uri, XMLConstants.W3C_XML_SCHEMA_NS_URI) ||
						StringUtils.equals(uri, XMLConstants.W3C_XPATH_DATATYPE_NS_URI)){
					continue;
				}
				
				addAttributeToImportedEntity(atts.getLocalName(i), atts.getValue(i));
			}
		}
		
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {

		logger.debug("Importing element name {}, uri {}", localName, uri);
		
		if (doNotProcessEvent()){
			return;
		}
		
		
		if (importResult == null){
			createRootEntity(uri, localName, atts);
		}
		else{
			
			//Queue should not be empty
			throwExceptionIfQeueIsEmpty(uri, localName);
			
			Object currentEntity = cmsRepositoryEntityQueue.peek().getEntity();
			
			//	Element corresponds to a cms property
			if (currentEntity instanceof ContentObject){
				if (CmsConstants.OWNER_ELEMENT_NAME.equals(localName)){
					if (processOwner(atts)){
						return;
					}
				}
				else{
					processCmsProperty(localName, atts, uri);
					return ;
				}
			}
			else if (currentEntity instanceof CmsProperty ){
				processCmsProperty(localName, atts, uri);
				return ;
			}
			else if (currentEntity instanceof BinaryChannel && 
					CmsConstants.CONTENT_ELEMENT_NAME.equals(localName)){
				//Element is content , which is defined in context of BinaryChannel
				return;
			}
			else if (CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME.equals(localName)){
				//Depending on source XML or JSON attribute for lang may appear differently
				String langValue = getValueForAttribute(CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX, atts);
				if (langValue == null){
					langValue = getValueForAttribute(CmsConstants.LANG_ATTRIBUTE_NAME, atts);
				}
				if (langValue != null){
					addAttributeToImportedEntity(CmsConstants.LANG_ATTRIBUTE_NAME_WITH_PREFIX, langValue);
				}
				else{
					//Found no lang attribute. 
					if (atts.getLength() > 0 && currentEntity instanceof Localization){
						addLabelsForLocaleToImportedEntity(atts);
					}
				}
				
				return;
			}
			else if (CmsConstants.OWNER_ELEMENT_NAME.equals(localName)){
				if (processOwner(atts)){
					return;
				}
			}
			else if (shouldIgnoreElement(localName)){
				return ;
			}
			else{
			
				if (currentEntity instanceof Topic){
					
					//Expecting taxonomy, parentTopic or child topic
					final Topic currentlyImportedTopic = (Topic)currentEntity;
					
					if (CmsBuiltInItem.Taxonomy.getLocalPart().equals(localName)){
						Taxonomy taxonomy = createNewTaxonomy(atts, localName);
						currentlyImportedTopic.setTaxonomy(taxonomy);
						pushEntity(localName, taxonomy, atts);
						return;
					}
					else if (CmsBuiltInItem.Topic.getLocalPart().equals(localName)){
						
						//Perform an initial check if topic name already exists
						if (atts!=null &&  importContext.isEntityCached(getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts))){
							throw new SAXException("Topic with name "+getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts) +" has been already imported");
						}
						
						Topic childTopic = createNewTopic(atts, localName);
						currentlyImportedTopic.addChild(childTopic);
						if (childTopic.getTaxonomy() == null && currentlyImportedTopic.getTaxonomy() != null){
							childTopic.setTaxonomy(currentlyImportedTopic.getTaxonomy());
						}
						pushEntity(localName, childTopic, atts);
						
						return;
					}
					else if (CmsConstants.PARENT_TOPIC.equals(localName)){
						Topic parentTopic = createNewTopic(atts, localName);
						currentlyImportedTopic.setParent(parentTopic);
						pushEntity(localName, parentTopic, atts);
						return;
					}
				}
				else if (currentEntity instanceof Space){
					
					//Expecting parentSpace or child space
					if (CmsBuiltInItem.Space.getLocalPart().equals(localName)){
						Space space = createNewSpace(atts, localName);
						((Space)currentEntity).addChild(space);
						pushEntity(localName, space, atts);
						return;
					}
					else if (CmsConstants.PARENT_SPACE.equals(localName)){
						Space space = createNewSpace(atts, localName);
						((Space)currentEntity).setParent(space);
						pushEntity(localName, space, atts);
						return;
					}
				}
				else if (currentEntity instanceof Taxonomy){
					
					//Expecting  child topic
					if (CmsBuiltInItem.Topic.getLocalPart().equals(localName)){
						if (atts!=null &&  importContext.isEntityCached(getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts))){
							throw new SAXException("Topic with name "+getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts) +" has been already imported");
						}

						Topic topic = createNewTopic(atts, localName);
						((Taxonomy)currentEntity).addRootTopic(topic);
						pushEntity(localName, topic, atts);
						return;
					}
				}
				else if (currentEntity instanceof RepositoryUser){
					
					if (CmsBuiltInItem.Space.getLocalPart().equals(localName)){
						Space space = createNewSpace(atts, localName);
						((RepositoryUserImpl)currentEntity).setSpace(space);
						pushEntity(localName, space, atts);
						return;
					}
					else if (CmsBuiltInItem.Taxonomy.getLocalPart().equals(localName)){
						Taxonomy taxonomy = createNewTaxonomy(atts, localName);
						((RepositoryUserImpl)currentEntity).setFolksonomy(taxonomy);
						pushEntity(localName, taxonomy, atts);
						return;
					}
				}
				else if (currentEntity instanceof Repository){
					if (CmsBuiltInItem.Taxonomy.getLocalPart().equals(localName)){
						Taxonomy taxonomy = createNewTaxonomy(atts, localName);
						pushEntity(localName, taxonomy, atts);
						return;
					}
					else if (CmsBuiltInItem.OrganizationSpace.getLocalPart().equals(localName)){
						Space space = createNewSpace(atts, localName);
						pushEntity(localName, space, atts);
						return;
					}
					else if (CmsBuiltInItem.RepositoryUser.getLocalPart().equals(localName)){
						RepositoryUser repositoryUser = createNewRepositoryUser(atts, localName);
						pushEntity(localName, repositoryUser, atts);
						return;
					}
					else{
						ContentObject contentObject = createNewContentObject(atts, localName, uri);
						pushEntity(localName, contentObject, atts);
						return ;
					}
				}
				else if (currentEntity instanceof List){
					if (CmsBuiltInItem.Taxonomy.getLocalPart().equals(localName)){
						Taxonomy taxonomy = createNewTaxonomy(atts, localName);
						((List)currentEntity).add(taxonomy);
						pushEntity(localName, taxonomy, atts);
						return;
					}
					else if (CmsBuiltInItem.Space.getLocalPart().equals(localName)){
						Space space = createNewSpace(atts, localName);
						((List)currentEntity).add(space);
						pushEntity(localName, space, atts);
						return;
					}
					else if (CmsBuiltInItem.RepositoryUser.getLocalPart().equals(localName)){
						RepositoryUser repositoryUser = createNewRepositoryUser(atts, localName);
						((List)currentEntity).add(repositoryUser);
						pushEntity(localName, repositoryUser, atts);
						return;
					}
					else if (CmsBuiltInItem.Topic.getLocalPart().equals(localName)){
						Topic topic = createNewTopic(atts, localName);
						((List)currentEntity).add(topic);
						pushEntity(localName, topic, atts);
						return;
					}
					else{
						ContentObject contentObject = createNewContentObject(atts, localName, uri);
						((List)currentEntity).add(contentObject);
						pushEntity(localName, contentObject, atts);
						return ;
					}
				}
						
			}
			
			
			throw new SAXException("Unexpected element "+localName);
			
		}
	}

	private boolean shouldIgnoreElement(String localName) {
		return StringUtils.equals(localName, CmsBuiltInItem.Localization.getLocalPart()) ||
		StringUtils.equals(localName, CmsConstants.ROOT_TOPICS) ||
		StringUtils.equals(localName, CmsConstants.CHILD_SPACES) ||
		StringUtils.equals(localName, CmsConstants.CHILD_TOPICS) || 
		(cmsRepositoryEntityQueue.peek().getEntity() instanceof Repository && 
				(StringUtils.equals(localName, CmsConstants.CONTENT_OBJECTS_ELEMENT_NAME) ||
						StringUtils.equals(localName, CmsConstants.TAXONOMIES_ELEMENT_NAME) ||
						StringUtils.equals(localName, CmsConstants.REPOSITORY_USERS_ELEMENT_NAME))) ||
						(cmsRepositoryEntityQueue.peek().getEntity() instanceof List &&
				( StringUtils.equals(localName, CmsConstants.RESOURCE_COLLECTION) ||
						StringUtils.equals(localName, CmsConstants.RESOURCE_RESPONSE) ||
						StringUtils.equals(localName, CmsConstants.TOTAL_RESOURCE_COUNT) ||
						StringUtils.equals(localName, CmsConstants.OFFSET) ||
						StringUtils.equals(localName, CmsConstants.LIMIT) )
						);
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		
		logger.debug("Ending element {}",localName);
		
		ImportedEntity entityWhoseImportHasFinished = null;
		
		if (elementNameToBeIgnored != null){
			//Ending element belongs to a part of source which is ignored
			//Check whether this part has ended or not
			//In case ignored part has ended, current imported entity is returned
			//in order to be further processed
			entityWhoseImportHasFinished = checkIgnoredPartHasEnded(localName);
		}
		else if (StringUtils.equals(CmsConstants.LOCALIZED_LABEL_ELEMENT_NAME, localName)){
			
			addLocalizedLabelToImportedEntity(retrieveElementContentValue());
			
			clearElementContent();
			
			return ;
			
		}
		else if (StringUtils.equals(CmsConstants.CONTENT_ELEMENT_NAME, localName) && 
				! cmsRepositoryEntityQueue.isEmpty() && 
				cmsRepositoryEntityQueue.peek().getEntity() instanceof BinaryChannel){
			
			//Content element is ending. Add content to binaryChannel
			addContentToBinaryChannel(retrieveElementContentValue());

			clearElementContent();
			
			return;
		}
		else if (entityHasEnded(localName)){
			entityWhoseImportHasFinished = removeEntityFromQueue();
		}
		
		if (entityWhoseImportHasFinished != null){
			entityWhoseImportHasFinished.completeEntityImport((retrieveElementContentValue()));
			
			if (entityWhoseImportHasFinished.getEntity() instanceof CmsRepositoryEntity){
				CmsRepositoryEntity cmsRepositoryEntity = (CmsRepositoryEntity)entityWhoseImportHasFinished.getEntity();
				
				
				if (! cmsRepositoryEntityQueue.isEmpty() && 
						( cmsRepositoryEntityQueue.peek().getEntity() instanceof Repository ||
								cmsRepositoryEntityQueue.peek().getEntity() instanceof List) ){
					//Repository or Resource Collection import under way. Send entity to importer in order to be saved accordingly
					cmsRepositoryEntity = deserializer.save(cmsRepositoryEntity);
				}
				
				importContext.cacheEntity(cmsRepositoryEntity);
			}
		}
		
		clearElementContent();


	}

	private String retrieveElementContentValue() {
		return StringUtils.trimToNull(elementContent.toString());
	}

	private boolean processOwner(Attributes atts) throws SAXException {
		
		ImportedEntity currentImportedEntity = cmsRepositoryEntityQueue.peek();
		
		if (currentImportedEntity == null){
			throw new CmsException("No parent entity found for element 'owner' with attributes "+ atts.toString());
		}
		
		if (currentImportedEntity.hasOwnerElement()){
			RepositoryUser owner = createNewRepositoryUser(atts, CmsConstants.OWNER_ELEMENT_NAME);
			
			if (currentImportedEntity.getEntity() instanceof ContentObject){
				((ContentObject)currentImportedEntity.getEntity()).setOwner(owner);
			}
			else if (currentImportedEntity.getEntity() instanceof Topic){
				((Topic)currentImportedEntity.getEntity()).setOwner(owner);
			}
			else if (currentImportedEntity .getEntity() instanceof Space){
				((Space)currentImportedEntity.getEntity()).setOwner(owner);
			}
			else{
				logger.debug("Owner {} has not been assigned to parent entity {}", owner.toString(), currentImportedEntity.getEntity());
			}
			
			pushEntity(CmsConstants.OWNER_ELEMENT_NAME, owner, atts);
			
			return true;
		}
		
		return false;
	}

	private RepositoryUser createNewRepositoryUser(Attributes atts, String entityLocalName) {
		
		RepositoryUser repositoryUser = retrieveOrCreateEntity(RepositoryUser.class, atts, entityLocalName);
		
		repositoryUser.setExternalId(getValueForAttribute(CmsBuiltInItem.ExternalId.getLocalPart(), atts));
		repositoryUser.setLabel(getValueForAttribute(CmsBuiltInItem.Label.getLocalPart(), atts));
		
		return repositoryUser;
		
	}
	private <T extends CmsRepositoryEntity> T retrieveOrCreateEntity(Class<T> entityClass, Attributes atts, String entityLocalName){

		T entity = null;
		
		boolean referenceIdNotFoundInMap = false;
		String cacheKey = null;
		boolean cacheKeyIsId = false;
		
		if (atts != null && atts.getLength() > 0){
			cacheKey = getValueForAttribute(CmsBuiltInItem.CmsIdentifier.getLocalPart(), atts);
			
			if (cacheKey == null){
				cacheKey = getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts);
			}
			else{
				cacheKeyIsId =true;
			}
				
			if (cacheKey == null && ContentObject.class.isAssignableFrom(entityClass)){
				cacheKey = getValueForAttribute(CmsBuiltInItem.SystemName.getLocalPart(), atts);
			}
			else if (cacheKey == null && RepositoryUser.class.isAssignableFrom(entityClass)){
				cacheKey = getValueForAttribute(CmsBuiltInItem.ExternalId.getLocalPart(), atts);
			}
			
			if (cacheKey != null){
				
				if (importContext.isEntityCached(cacheKey)){
					elementNameToBeIgnored = entityLocalName;
					return (T) importContext.getEntityFromCache(cacheKey); 
				}
				else{
					referenceIdNotFoundInMap = true;
				}
			}
		}
		
		if (entityClass == RepositoryUser.class){
			entity = (T) CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser();
		}
		else if (entityClass == Topic.class){
			entity = (T) CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic();
		}
		else if (entityClass == Space.class){
			entity = (T) CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace();
		}
		else if (entityClass == Taxonomy.class){
			entity = (T) CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy();
		}
		else if (entityClass == BinaryChannel.class){
			entity = (T) CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newBinaryChannel();
		}
		else {
			throw new CmsException("Unknown entity type "+ entityClass);
		}
		
		if (referenceIdNotFoundInMap){
			if (entity.getId() == null && cacheKeyIsId){
				entity.setId(cacheKey);
				importContext.cacheEntity(entity);
			}
			else if (cacheKey !=null){
				importContext.cacheEntity(cacheKey, entity);
			}
			
		}
		
		return entity;
		
	}
	private void throwExceptionIfQeueIsEmpty(String uri, String localName)
	throws SAXException {
		if (cmsRepositoryEntityQueue.isEmpty()){
			throw new SAXException("Element {"+uri+"}"+localName+" was not imported. Entity queue is empty");
		}
	}

	private ContentObject createNewContentObject(Attributes atts, String localName, String uri) {
		
		String contentType = getValueForAttribute(CmsBuiltInItem.ContentObjectTypeName.getLocalPart(), atts);
		
		if (StringUtils.isBlank(contentType)){
			contentType = localName;
		}
		
		ContentObject contentObject = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newContentObjectForType(contentType, "");
		
		if (contentType == null){
				throw new CmsException("Element {"+ uri+"}"+localName+" does not correspond to a known content type");
		}

		contentObject.setContentObjectType(getValueForAttribute(CmsBuiltInItem.ContentObjectTypeName.getLocalPart(), atts));
		contentObject.setSystemName(getValueForAttribute(CmsBuiltInItem.SystemName.getLocalPart(), atts));
		
		return contentObject;
		
	}

	private String getValueForAttribute(String attributeName, Attributes atts){
		
		final int index = atts.getIndex(attributeName);
		
		if (index > -1){
			return atts.getValue(index);
		}
		
		return null;
	}
	
	private void pushEntity(String entityLocalName, Object entity, Attributes atts) throws SAXException {
		pushEntity(entityLocalName, entity, atts, false);
	}
	
	private void pushEntity(String entityLocalName, Object entity, Attributes atts, boolean entityRepresentsAContentObjectReference) throws SAXException {
		
		ImportedEntity importedEntity = new ImportedEntity(entityLocalName,entity, df,entityRepresentsAContentObjectReference);
		
		cmsRepositoryEntityQueue.push(importedEntity);
		
		logger.debug("Adding to queue name {} entity {}", entityLocalName, entity);
		
		importAttributes(atts);
	}

	
	private boolean entityHasEnded(String localName) {
		return ! cmsRepositoryEntityQueue.isEmpty() && 
			StringUtils.equals(localName, cmsRepositoryEntityQueue.peek().getName());
	}

	private void addContentToBinaryChannel(String content) {
		
		if (content != null){
			
			try {
				((BinaryChannel)cmsRepositoryEntityQueue.peek().getEntity()).setContent(content.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("Error adding content {} to binaryChannel {}", content,cmsRepositoryEntityQueue.peek().getName() );
			}

		} 
		
	}

	private void addLabelsForLocaleToImportedEntity(Attributes atts) {
		
		if (!cmsRepositoryEntityQueue.isEmpty()){
			
			if (atts != null && atts.getLength() >0){
				for (int i=0;i<atts.getLength();i++){
					cmsRepositoryEntityQueue.peek().addLocalizedLabel(atts.getLocalName(i), atts.getValue(i));
				}
			}
		}
		else{
			logger.warn("No entity found in queue. Labels were not imported", atts);
		}
		
	}

	private void addLocalizedLabelToImportedEntity(String localizedLabel) {
		if (!cmsRepositoryEntityQueue.isEmpty()){
			cmsRepositoryEntityQueue.peek().addLocalizedLabel(localizedLabel);
		}
		else{
			logger.warn("No entity found in queue. Label {} was not imported", localizedLabel);
		}
		
	}

	private void addAttributeToImportedEntity(String attributeName, String attributeValue) throws SAXException {
		
		boolean attributeHasBeenAdded  = false;
		
		if (!cmsRepositoryEntityQueue.isEmpty()){
			final ImportedEntity firstEntityInQueue = cmsRepositoryEntityQueue.peek();
			final Object entity = firstEntityInQueue.getEntity();
			
			attributeHasBeenAdded = firstEntityInQueue.addAttribute(attributeName, attributeValue);
			
			if (attributeHasBeenAdded && 
					( entity instanceof ContentObject ||
							entity instanceof Topic ||
							entity instanceof Taxonomy ||
							entity instanceof Space ||
							entity instanceof RepositoryUser
					)
				){
				
				if (CmsBuiltInItem.CmsIdentifier.getLocalPart().equals(attributeName)){
					importContext.cacheEntity((CmsRepositoryEntity) entity);
				}
			}
			
			if (! attributeHasBeenAdded && attributeName != null && (
					entity instanceof ContentObject ||
					entity instanceof CmsProperty )){
				//Probably a cms property
				startElement(null, attributeName, null, null);
				
				if (attributeValue != null){
					characters(attributeValue.toCharArray(), 0, attributeValue.length());
				}
				endElement(null, attributeName, null);
				
				attributeHasBeenAdded = true;
			}
		}
		
		if (! attributeHasBeenAdded){
			logger.warn("Attribute {} with value {} was not imported. Entity in queue {}",
					new Object[]{attributeName, attributeValue, cmsRepositoryEntityQueue.isEmpty()? " NONE ":
						cmsRepositoryEntityQueue.peek().toString()});
		}
		
	}

	private void clearElementContent() {
		elementContent.delete(0, elementContent.length());
		elementContent.trimToSize();
	}
	
	private ImportedEntity checkIgnoredPartHasEnded(String localName) {
		if (StringUtils.equals(elementNameToBeIgnored, localName)){
			elementNameToBeIgnored = null;
			return removeEntityFromQueue();
		}
		else{

			clearElementContent();
			
			return null;
		}

	}
	
	private ImportedEntity removeEntityFromQueue() {
		ImportedEntity removedEntity = cmsRepositoryEntityQueue.poll();
		
		logger.debug("Removing entity name {} {} from queue", removedEntity != null ? removedEntity.getName() : " null", 
				removedEntity != null ? removedEntity.getEntity().toString() : null);
		
		return removedEntity;
	}


	private void processCmsProperty(String localName, Attributes atts, String uri) throws SAXException {
		ImportedEntity importedEntity = cmsRepositoryEntityQueue.peek();
		
		if (importedEntity == null){
			throw new CmsException("No parent property found for element "+localName);
		}
		
		CmsPropertyDefinition cmsPropertyDefinition = null;
		CmsProperty cmsProperty = null;
		ComplexCmsProperty parentComplexCmsProperty = null;
		
		if (importedEntity.getEntity() instanceof ContentObject){
			
			//Special case. Element 'title' of a content object reference might be imported
			if (importedEntity.representsAReferenceToAContentObject() && CmsConstants.TITLE_ELEMENT_NAME.equals(localName)){
				cmsPropertyDefinition = ((ContentObject)importedEntity.getEntity()).getTypeDefinition().getCmsPropertyDefinition("profile.title");
				cmsProperty = ((ContentObject)importedEntity.getEntity()).getCmsProperty("profile.title");
			}
			else{
				cmsPropertyDefinition = ((ContentObject)importedEntity.getEntity()).getTypeDefinition().getCmsPropertyDefinition(localName);
			}
			
			if (cmsPropertyDefinition == null){
				//Property is an aspect. It is safe to call getCmsProperty
				cmsProperty = ((ContentObject)importedEntity.getEntity()).getCmsProperty(localName);
			}
			else{
				parentComplexCmsProperty = ((ContentObject)importedEntity.getEntity()).getComplexCmsRootProperty();
			}
		}
		else if (importedEntity.getEntity() instanceof ComplexCmsProperty){
			cmsPropertyDefinition = ((ComplexCmsProperty)importedEntity.getEntity()).getPropertyDefinition().getChildCmsPropertyDefinition(localName);
			
			if (cmsPropertyDefinition == null){
				throw new CmsException("Invalid property "+localName+ " for parent property "+ 
						((ComplexCmsProperty)importedEntity.getEntity()).getFullPath());
			}
			
			parentComplexCmsProperty = (ComplexCmsProperty)importedEntity.getEntity();
		}
		else if (importedEntity.getEntity() instanceof BinaryProperty){
			//Leave entity as is
			elementNameToBeIgnored = localName;
			return;
		}
		else {
			throw new CmsException("Expected to find either a ComplexCmsProperty or a BinaryProperty or a ContentObject as a parent for element "+ localName + " " +
					" but found "+ importedEntity.getName()+ " as "+ importedEntity.getEntity().toString()+ " instead");
		}
		
		
		if (cmsProperty == null){
			if (cmsPropertyDefinition == null){
				throw new CmsException("Invalid child property "+localName+ " parent entity "+
						importedEntity.getEntity().toString());
			}
			
			if (cmsPropertyDefinition.isMultiple() && cmsPropertyDefinition.getValueType() == ValueType.Complex){
				cmsProperty = parentComplexCmsProperty.createNewValueForMulitpleComplexCmsProperty(localName);
			}
			else{
				boolean propertyAlreadyLoaded = parentComplexCmsProperty.isChildPropertyLoaded(localName);
				cmsProperty = parentComplexCmsProperty.getChildProperty(localName);
				
				if (cmsProperty != null && cmsPropertyDefinition instanceof SimpleCmsPropertyDefinition && 
						((SimpleCmsPropertyDefinition)cmsPropertyDefinition).getDefaultValue() != null && 
						! propertyAlreadyLoaded){
					//remove default value. Property has  been loaded for the first time and contains its default value
					//Default value should be removed
					((SimpleCmsProperty)cmsProperty).removeValues();
				}
			}
			
			if (cmsProperty == null){
				throw new CmsException("Invalid property "+localName+ " for parent property "+ 
						cmsPropertyDefinition.getFullPath());
			}
		}
		
		if (cmsProperty.getValueType() == ValueType.Complex){
			pushEntity(localName, cmsProperty, atts);
		}
		else if (cmsProperty.getValueType() == ValueType.Binary){
			BinaryChannel binaryChannel = populateBinaryChannelProperty(((BinaryProperty)cmsProperty), atts);
			
			pushEntity(localName, binaryChannel, atts);
			
		}
		else if (cmsProperty.getValueType() == ValueType.Topic){
			Topic topic = populateTopicProperty(((TopicProperty)cmsProperty), atts, localName);
			pushEntity(localName, topic, atts);
		}
		else if (cmsProperty.getValueType() == ValueType.ContentObject){
			ContentObject contentObject = populateContentObjectProperty(((ContentObjectProperty)cmsProperty), atts, localName, uri);
			pushEntity(localName, contentObject, atts, true);
		}
		else {
			pushEntity(localName, cmsProperty, atts);
		}
	}

	private ContentObject populateContentObjectProperty(
			ContentObjectProperty contentObjectProperty, Attributes atts,
			String localName, String uri) {
		
		ContentObject contentObject = createNewContentObject(atts, null, uri);
		
		contentObjectProperty.addSimpleTypeValue(contentObject);
		
		return contentObject;
	}

	private Topic populateTopicProperty(TopicProperty topicProperty,
			Attributes atts, String entityLocalName) {
		
		Topic topic = createNewTopic(atts, entityLocalName);
		
		topicProperty.addSimpleTypeValue(topic);
		
		return topic;
	}

	private Topic createNewTopic(Attributes atts, String entityLocalName) {
		
		Topic topic = retrieveOrCreateEntity(Topic.class, atts, entityLocalName);
		
		if (elementNameToBeIgnored == null){
			
			topic.setName(getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts));
			
		}
		
		return topic;
	}
	
	private Space createNewSpace(Attributes atts, String entityLocalName) {
		
		Space space = retrieveOrCreateEntity(Space.class, atts, entityLocalName);
		
		if (elementNameToBeIgnored == null){
			
			space.setName(getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts));
			
		}
		
		return space;
	}

	
	private Taxonomy createNewTaxonomy(Attributes atts, String entityLocalName) {
		
		Taxonomy taxonomy = retrieveOrCreateEntity(Taxonomy.class, atts, entityLocalName);
		
		if (elementNameToBeIgnored == null){
			taxonomy.setName(getValueForAttribute(CmsBuiltInItem.Name.getLocalPart(), atts));
		}
		
		return taxonomy;
	}


	private BinaryChannel populateBinaryChannelProperty(
			BinaryProperty binaryProperty, Attributes atts) {
		
		BinaryChannel binaryChannel = retrieveOrCreateEntity(BinaryChannel.class, atts, binaryProperty.getName());
		
		binaryChannel.setName(binaryProperty.getName());
		
		binaryProperty.addSimpleTypeValue(binaryChannel);
		
		return binaryChannel;
	}
	
	public T getImportResult(){
		return importResult;
	}
	
	
	
	/*
	 * Unused methods  
	 */

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		
	}
}
