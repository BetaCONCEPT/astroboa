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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TopicDao;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.BinaryPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.SimpleCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.TopicReferencePropertyImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.DefinitionReservedName;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class PopulateSimpleCmsProperty {

	private final Logger logger = LoggerFactory.getLogger(PopulateSimpleCmsProperty.class);

	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;
	@Autowired
	private TopicDao topicDao;
	@Autowired
	private BinaryChannelUtils binaryChannelUtils;


	private SaveMode saveMode;
	private Session session;
	private Node propertyContainerNode;
	private SimpleCmsPropertyDefinition propertyDefinition;
	private SimpleCmsProperty simpleCmsPropertyToBeSaved;

	private boolean propertyHasBeenLoadedAndRemoved;

	private Context context;

	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @param propertyHasBeenLoadedAndRemoved the propertyHasBeenLoadedAndRemoved to set
	 */
	public void setPropertyHasBeenLoadedAndRemoved(
			boolean propertyHasBeenLoadedAndRemoved) {
		this.propertyHasBeenLoadedAndRemoved = propertyHasBeenLoadedAndRemoved;
	}

	public void setSaveMode(SaveMode saveMode) {
		this.saveMode = saveMode;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setPropertyContainerNode(Node propertyContainerNode) {
		this.propertyContainerNode = propertyContainerNode;
	}

	public void setPropertyDefinition(
			SimpleCmsPropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public void setCmsPropertyToBeSaved(
			SimpleCmsProperty cmsPropertyToBeSaved) {
		this.simpleCmsPropertyToBeSaved = cmsPropertyToBeSaved;
	}

	public void populate() throws Exception {

		if (logger.isDebugEnabled()){
			logger.debug("Populate simple property {}", simpleCmsPropertyToBeSaved.getFullPath());
		}

		initialCheck();

		final ValueType definitionValueType = propertyDefinition.getValueType();

		final ItemQName propertyAsItem = ItemUtils.createSimpleItem(propertyDefinition.getName());
		final ValueFactory valueFactory = session.getValueFactory();

		dispatchPopulate(definitionValueType, propertyAsItem, valueFactory);

	}

	private void checkSimplePropertyWithDefinition() throws Exception {

		if (propertyDefinition == null)
			throw new Exception("Undefined property definition ");

		if (propertyDefinition.getValueType() != simpleCmsPropertyToBeSaved.getValueType())
			throw new CmsException("Found type " + 	simpleCmsPropertyToBeSaved.getValueType() +
					", expected type "+ propertyDefinition.getValueType() + " for property "+ simpleCmsPropertyToBeSaved.getFullPath()) ;

		if (!propertyDefinition.isMultiple())
		{
			//Property is not multiple. Check its values
			switch (propertyDefinition.getValueType()) {
			case Binary:
			case Boolean:
			case ObjectReference:
			case Date:
			case Double:
			case Long:
			case TopicReference:
			case String:
				//If property contains more than one value a MultipleOccurence exception will be thrown
				//Otherwise nothing will happen
				simpleCmsPropertyToBeSaved.getSimpleTypeValue();
				break;
			default: 
				throw new CmsException(simpleCmsPropertyToBeSaved.getFullPath() + " invalid simple type "+ propertyDefinition.getValueType());

			}
		}
	}


	private void dispatchPopulate(ValueType definitionValueType, ItemQName propertyAsItem, ValueFactory valueFactory) throws Exception{

		if (propertyDefinition.isSetDefaultValue() && !	propertyHasBeenLoadedAndRemoved){
			//Set default value if there are no values at all
			if (simpleCmsPropertyToBeSaved.hasNoValues() && propertyDefinition.isMandatory()){
				simpleCmsPropertyToBeSaved.addSimpleTypeValue(propertyDefinition.getDefaultValue());
			}
		}

		//Check values are valid
		((SimpleCmsPropertyImpl)simpleCmsPropertyToBeSaved).checkValues();

		switch (definitionValueType) {
		case Date:
			//Special case. Check if provided date is profile.created or profile.created
			if (StringUtils.equals("profile.created", propertyDefinition.getPath()) && 
					propertyContainerNode.hasProperty("created")){
				//Property profile.created already exists and therefore it will not change.
				//Issue a warning if value provided is not the same with the one existed
				Calendar providedCreationDate = ((CalendarProperty)simpleCmsPropertyToBeSaved).getSimpleTypeValue();
				Calendar existingCreationDate = propertyContainerNode.getProperty("created").getValue().getDate();

				if (providedCreationDate == null || existingCreationDate == null ||
						providedCreationDate.getTimeInMillis() != existingCreationDate.getTimeInMillis()){
					logger.warn("User has provided '{}' as the value of property 'profile.created' but this property already contains value " +
							"'{}'. Changing the value of this property is not allowed and therefore provided value will be ignored. In fact" +
							" it will be replaced by the persisted value.", 
							(providedCreationDate == null ? "null" : DateFormatUtils.format(providedCreationDate, "dd/MM/yyy HH:mm")+ " "+providedCreationDate.getTimeInMillis()), 
							(existingCreationDate == null ? "null" : DateFormatUtils.format(existingCreationDate, "dd/MM/yyy HH:mm")+ " "+existingCreationDate.getTimeInMillis())
							);
				}
			}
			else if (StringUtils.equals("profile.modified", propertyDefinition.getPath()) && 
					propertyContainerNode.hasProperty("modified")){
				//Property profile.modified already exists and therefore it will not change.
				//Issue a warning if value provided is not the same with the one existed
				Calendar providedModifiedDate = ((CalendarProperty)simpleCmsPropertyToBeSaved).getSimpleTypeValue();
				Calendar existingModifiedDate = propertyContainerNode.getProperty("modified").getValue().getDate();

				if (providedModifiedDate == null || existingModifiedDate == null ||
						providedModifiedDate.getTimeInMillis() != existingModifiedDate.getTimeInMillis()){
					logger.warn("User has provided '{}' as the value of property 'profile.modified' but this property already contains value " +
							"'{}'. Changing the value of this property is not allowed and therefore provided value will be ignored. Values of this " +
							" property are provided by Astroboa only.", 
							(providedModifiedDate == null ? "null" : DateFormatUtils.format(providedModifiedDate, "dd/MM/yyy HH:mm")+ " "+providedModifiedDate.getTimeInMillis()), 
							(existingModifiedDate == null ? "null" : DateFormatUtils.format(existingModifiedDate, "dd/MM/yyy HH:mm")+ " "+existingModifiedDate.getTimeInMillis())
							);
				}
			}
			else{
				populateSimpleProperty(valueFactory, definitionValueType, propertyAsItem);
			}

			break;
		case Boolean:
		case String:
		case Double:
		case Long:
			populateSimpleProperty(valueFactory, definitionValueType, propertyAsItem);
			break;
		case ObjectReference:
			populatePropertyAsContentObject(propertyAsItem);
			break;
		case TopicReference:
			populatePropertyAsTopic(propertyAsItem);
			break;
		case Binary:{

			//If simple cms property corresponds to an unmanaged binary property then
			//we only care about relativeFileSystemPath. 
			if (propertyDefinition instanceof BinaryPropertyDefinition &&
					((BinaryPropertyDefinition)propertyDefinition).isBinaryChannelUnmanaged()){
				populateBinaryChannelAsUnmanaged(valueFactory, definitionValueType, propertyAsItem);
			}
			else{
				populateBinaryChannel();
			}
		}
		default:
			break;
		}
	}

	private void populateBinaryChannelAsUnmanaged(ValueFactory valueFactory, ValueType definitionValueType, ItemQName propertyAsItem) throws RepositoryException {

		List<Object> binaryChannelRelativePaths = new ArrayList<Object>();

		if (propertyDefinition.isMultiple()){
			List simpleTypeValues = simpleCmsPropertyToBeSaved.getSimpleTypeValues();
			for (Object binaryChannel : simpleTypeValues){
				if (binaryChannel instanceof BinaryChannel){

					if ( ((BinaryChannel)binaryChannel).contentExists()){
						binaryChannelRelativePaths.add(((BinaryChannel)binaryChannel).getRelativeFileSystemPath());
					}
					else{
						throw new CmsException("Could not find content for unmanaged binary channel in path "+
								((BinaryChannel)binaryChannel).getRelativeFileSystemPath()+ " (path is relative to repository UnmanagedDataStore directory)");
					}
				}
				else{
					throw new CmsException("While saving values of binary property "+simpleCmsPropertyToBeSaved.getFullPath()+" found a value of type other than BinaryChannel. "+
							binaryChannel.getClass());
				}
			}

			JcrNodeUtils.addMultiValueProperty(propertyContainerNode, propertyAsItem, saveMode, binaryChannelRelativePaths, ValueType.String, valueFactory);
		}
		else{

			Object binaryChannelRelativePath = null;

			if (simpleCmsPropertyToBeSaved.hasValues()){

				if (simpleCmsPropertyToBeSaved.getSimpleTypeValue() instanceof BinaryChannel){

					if ( ((BinaryChannel)simpleCmsPropertyToBeSaved.getSimpleTypeValue()).contentExists()){
						binaryChannelRelativePath  = (((BinaryChannel)simpleCmsPropertyToBeSaved.getSimpleTypeValue()).getRelativeFileSystemPath());
					}
					else{
						throw new CmsException("Could not find content for unmanaged binary channel in path "+
								((BinaryChannel)simpleCmsPropertyToBeSaved.getSimpleTypeValue()).getRelativeFileSystemPath()+ " (path is relative to repository UnmanagedDataStore directory)");
					}
				}
				else{
					throw new CmsException("While saving values of binary property "+simpleCmsPropertyToBeSaved.getFullPath()+" found a value of type other than BinaryChannel. "+
							simpleCmsPropertyToBeSaved.getSimpleTypeValue().getClass());
				}
			}

			JcrNodeUtils.addSimpleProperty(saveMode, propertyContainerNode, propertyAsItem, binaryChannelRelativePath, valueFactory, ValueType.String);
		}


	}

	@SuppressWarnings("unchecked")
	private void populatePropertyAsTopic(ItemQName propertyAsItem) throws RepositoryException {

		List<Topic> topicWithCmsIdentifiers = new ArrayList<Topic>();

		if (simpleCmsPropertyToBeSaved.hasValues()){

			List<Topic> topics = ((TopicReferencePropertyImpl)simpleCmsPropertyToBeSaved).getValues();

			if (CollectionUtils.isNotEmpty(topics)){
				List<String> acceptedTaxonomies = ((TopicReferencePropertyDefinition)propertyDefinition).getAcceptedTaxonomies();

				boolean valuesAreRestrictedToSpecificTaxonomies = CollectionUtils.isNotEmpty(acceptedTaxonomies);

				for (Topic topic : topics){

					Node topicNode = retrieveTopicNode(topic);

					if (topicNode == null){
						
						//Very special case
						//Provided topic has no id, no name and no localized labels
						//Ignore it
						if (StringUtils.isBlank(topic.getId()) && 
								StringUtils.isBlank(topic.getName()) && 
								! topic.hasLocalizedLabels()){
							continue;
						}
						
						topicNode = createJcrNodeForTopicAndCacheJcrNode(topic);
					}
					else{
						appendTopicWithIdIfTopicIsNew(topic, topicNode);
					}

					assertTopicCanBeRelatedToAContentObject(topic, topicNode);

					//Check that it is of valid taxonomy
					if (valuesAreRestrictedToSpecificTaxonomies){
						assertTopicTaxonomyIsOneOfTheAccepted(acceptedTaxonomies, topic, topicNode);
					}

					topicWithCmsIdentifiers.add(topic);
				}
			}
		}

		EntityAssociationUpdateHelper topicUpdateHelper = createDefaultEntityAssociationUpdateHepler(propertyAsItem);
		topicUpdateHelper.setValuesToBeAdded(topicWithCmsIdentifiers);
		topicUpdateHelper.update();


	}

	private void assertTopicTaxonomyIsOneOfTheAccepted(
			List<String> acceptedTaxonomies, Topic topic, Node topicNode)
			throws RepositoryException {
		//Get taxonomy name of topic
		String taxonomyName = retrieveTaxonomyName(topic, topicNode);

		//Before throwing an exception check to see if topic exists. If it does exist
		//then check its taxonomy

		if ( StringUtils.isBlank(taxonomyName)){ //Taxonomy name must not be null
			throw new CmsException("Cannot find the taxonomy of topic "+topic.getName() + ". Cannot add topic because property " +
					propertyDefinition.getFullPath() + "'s values must belong to the following taxonomies "+ acceptedTaxonomies);
		}
		else if ( 
				!acceptedTaxonomies.contains(taxonomyName) &&  //Taxonomy name must belong to accepted Taxonomies OR
				!Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME.equals(taxonomyName)) // it belongs to a folksonomy
		{
			throw new CmsException("Topic "+topic.getName() + " belongs to taxonomy "+taxonomyName+ " where as property " +
					propertyDefinition.getFullPath() + "'s values must belong to the following taxonomies "+ acceptedTaxonomies);
		}

		logger.debug("Topic {} belongs to taxonomy {} which is one of the accepted taxonies {}", 
				new Object[]{topic.getName(), taxonomyName, acceptedTaxonomies});
	}

	private void assertTopicCanBeRelatedToAContentObject(Topic topic,
			Node topicNode) throws RepositoryException, ValueFormatException,
			PathNotFoundException {
		//If Topic does not allows referrer throw an exception
		//		If property does not exist an ItemNotFoundException is thrown
		if (! topicNode.hasProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()) ||
				!topicNode.getProperty(CmsBuiltInItem.AllowsReferrerContentObjects.getJcrName()).getBoolean()){
			throw new RepositoryException("Topic "+ topic.getName() + " "+ topic.getId() +" does not allow referrers");
		}
	}

	private void appendTopicWithIdIfTopicIsNew(Topic topic, Node topicNode)
			throws RepositoryException {
		logger.debug("Found jcr node {} for topic {}", topicNode.getPath(), topic.getName());

		if (topic.getId() == null){
			//Probably an existing topic.
			//Set its id
			topic.setId(context.getCmsRepositoryEntityUtils().getCmsIdentifier(topicNode));
		}
	}

	private Node createJcrNodeForTopicAndCacheJcrNode(Topic topic)
			throws RepositoryException {
		Node topicNode;
		//No topic exists. Create it first
		topicNode = topicDao.insertTopicNode(session, topic, context);

		if (topicNode == null){
			throw new RepositoryException("Could not find or create topic with id "+topic.getId()+" and name "+topic.getName());
		}
		else{
			//Cache topic node
			context.cacheTopicNode(topicNode, false);
			logger.debug("Found and cached jcr node {} for topic {}", topicNode.getPath(), topic.getName());
		}
		return topicNode;
	}

	private Node retrieveTopicNode(Topic topic) throws RepositoryException {

		if (topic.getId()!=null){
			return  context.retrieveNodeForTopic(topic.getId());
		}
		else if (topic.getName()!=null){
			return context.retrieveNodeForTopic(topic.getName());
		}

		return null;
	}

	private String retrieveTaxonomyName(Topic topic, Node topicNode) throws RepositoryException {
		if (topic.getTaxonomy() != null && StringUtils.isNotBlank((topic.getTaxonomy().getName()))){
			return topic.getTaxonomy().getName();
		}
		else{	
			//No taxonomy name found. 
			//Try to find taxonomy from topicNode
			if (topicNode != null){
				Node topicParent = topicNode.getParent();

				while (topicParent != null && ! topicParent.isNodeType(CmsBuiltInItem.TaxonomyRoot.getJcrName())){
					if (topicParent.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
						if (topicParent.getName().equals(CmsBuiltInItem.Folksonomy.getJcrName())){
							return CmsBuiltInItem.Folksonomy.getJcrName()+context.getCmsRepositoryEntityUtils().getCmsIdentifier(topicParent);
						}
						else {
							return topicParent.getName();
						}
					}

					topicParent = topicParent.getParent();
				}
			}

			return null;
		}
	}

	private void populatePropertyAsContentObject(ItemQName propertyAsItem) throws RepositoryException {

		List<ContentObject> contentObjects = new ArrayList<ContentObject>();
		if (propertyDefinition.isMultiple())
			contentObjects.addAll(simpleCmsPropertyToBeSaved.getSimpleTypeValues());
		else {
			Object contentObject = simpleCmsPropertyToBeSaved.getSimpleTypeValue();
			if (contentObject != null){
				contentObjects.add((ContentObject)contentObject);
			}
		}

		if (CollectionUtils.isNotEmpty(contentObjects)){
			for (ContentObject contentObject : contentObjects){

				if (StringUtils.isBlank( ((ContentObject) contentObject).getId() )){
					throw new CmsException("Content Object without id cannot be saved as a reference in property "+simpleCmsPropertyToBeSaved.getLocalizedLabelForCurrentLocale()+". Save content object first");
				}
				else{
					//Check its existence
					logger.debug("Retrieving content object {} from repository", ((ContentObject) contentObject).getId());
						
					if (context.getCmsRepositoryEntityUtils().retrieveUniqueNodeForContentObject(session, ((ContentObject) contentObject).getId()) == null){
						throw new CmsException("No content object found in repository with id "+ ((ContentObject) contentObject).getId() + " and name "+ ((ContentObject) contentObject).getSystemName());
					}
				}
			}
		}

		EntityAssociationUpdateHelper contentObjectAssociationUpdateHelper = createDefaultEntityAssociationUpdateHepler(propertyAsItem);
		contentObjectAssociationUpdateHelper.setValuesToBeAdded(contentObjects);
		contentObjectAssociationUpdateHelper.update();

	}

	private EntityAssociationUpdateHelper<CmsRepositoryEntity> createDefaultEntityAssociationUpdateHepler(ItemQName propertyAsItem) throws RepositoryException {

		//For now only content objects are related to topics.
		//Try to find contentObjectNode
		Node contentObjectNode = JcrNodeUtils.getContentObjectNode(propertyContainerNode);

		EntityAssociationUpdateHelper<CmsRepositoryEntity> contentObjectAssociationUpdateHelper = 
			new EntityAssociationUpdateHelper<CmsRepositoryEntity>(session, cmsRepositoryEntityAssociationDao, context);

		contentObjectAssociationUpdateHelper.setReferrerCmsRepositoryEntityNode(contentObjectNode);
		contentObjectAssociationUpdateHelper.setReferrerPropertyContainerNode(propertyContainerNode);
		contentObjectAssociationUpdateHelper.setReferrerPropertyName(propertyAsItem);
		contentObjectAssociationUpdateHelper.setReferrerPropertyNameMultivalue(propertyDefinition.isMultiple());

		return contentObjectAssociationUpdateHelper;
	}


	private void populateSimpleProperty(ValueFactory valueFactory, ValueType definitionValueType, ItemQName propertyAsItem) throws RepositoryException {

		if (propertyDefinition.isMultiple())
			JcrNodeUtils.addMultiValueProperty(propertyContainerNode, propertyAsItem, saveMode, simpleCmsPropertyToBeSaved.getSimpleTypeValues(), definitionValueType, valueFactory);
		else
			JcrNodeUtils.addSimpleProperty(saveMode, propertyContainerNode, propertyAsItem, simpleCmsPropertyToBeSaved.getSimpleTypeValue(), valueFactory, definitionValueType);
	}

	private void initialCheck() throws Exception  {

		if (simpleCmsPropertyToBeSaved == null || propertyContainerNode == null)
			throw new Exception("Content Object or content object node is null");

		checkSimplePropertyWithDefinition();
	}

	private boolean isReservedName(String propertyName)
	{
		try{
			if (propertyName == null)
				return false;

			//if not found enum method value of throws an IllegalArgumetException or a NullPointer exception
			DefinitionReservedName.valueOf(propertyName.toLowerCase());

			return true;

		}
		catch (IllegalArgumentException e)
		{
			//Name value is not a reserved name
			return false;
		}
	}

	private void populateBinaryChannel() throws Exception {

		//		Collect all existing binary channel node astroboa identifiers for this contentObjectNode under the name of this property
		Map<String, Node> binaryChannelsPerCmsIdentifier = binaryChannelUtils.partitionBinaryChannelNodeWithTheSameNamePerId(propertyContainerNode, propertyDefinition.getName());

		List<BinaryChannel> binaryChannels = ((BinaryPropertyImpl)simpleCmsPropertyToBeSaved).getValues();

		if (CollectionUtils.isNotEmpty(binaryChannels)){
			for (BinaryChannel binaryChannel : binaryChannels){
				final String binaryChannelName = binaryChannel.getName();

				//Do not process a binary channel with a reserved name
				if (!isReservedName(binaryChannelName)){

					//Also check that binary channel has the same name with definition
					if (StringUtils.isBlank(binaryChannelName) || !binaryChannelName.equals(propertyDefinition.getName()))
						throw new RepositoryException("Binary channel "+ binaryChannelName+ " does not have the same name with definition "+ propertyDefinition.getName());

					//Check if binaryChannel has an astroboa identifier
					String  binaryChannelIdentifier = binaryChannel.getId();

					//Populate BinaryChannel only if Content exists
					if (binaryChannel.isNewContentLoaded() || shouldGetContentFromExternalLocation(binaryChannel)){
						binaryChannelUtils.populateBinaryChannelToNode(binaryChannel, propertyContainerNode, session, saveMode, context, true);
					}

					if (binaryChannelIdentifier != null)
						//All went well. Remove identifier from map
						binaryChannelsPerCmsIdentifier.remove(binaryChannelIdentifier);							
				}
			}


			if (simpleCmsPropertyToBeSaved instanceof BinaryPropertyImpl){
				((BinaryPropertyImpl)simpleCmsPropertyToBeSaved).updateBinaryChannelsWithNecessaryInfoForBuildingURLs(true);
			}
		}

		//		Finally remove all binary channels that remained in the map
		for (Node binaryChannelNode : binaryChannelsPerCmsIdentifier.values()){
			binaryChannelNode.remove();
		}
	}

	private boolean shouldGetContentFromExternalLocation(BinaryChannel binaryChannel) {
		
		String externalLocationOfTheContent = ((BinaryChannelImpl)binaryChannel).getExternalLocationOfTheContent();
		
		boolean externalLocationFound = StringUtils.isNotBlank(externalLocationOfTheContent);
		
		boolean binaryChannelIsNew = StringUtils.isBlank(binaryChannel.getId());
		
		String binaryFriendlyUrl = null;
		String binaryPermanentUrl = null;
		
		if (!binaryChannelIsNew){
			binaryFriendlyUrl = binaryChannel.buildResourceApiURL(null, null, null, null, null, true, false);
			binaryPermanentUrl = binaryChannel.buildResourceApiURL(null, null, null, null, null, false, false);
		}
		
		return externalLocationFound && (binaryChannelIsNew || 
				( ! StringUtils.equals(externalLocationOfTheContent, binaryFriendlyUrl) && ! StringUtils.equals(externalLocationOfTheContent, binaryPermanentUrl)));
		
	}
}
