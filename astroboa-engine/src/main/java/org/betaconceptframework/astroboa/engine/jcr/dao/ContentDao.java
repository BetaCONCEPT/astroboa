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


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsConcurrentModificationException;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.query.CalendarInfo;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.renderer.BinaryChannelRenderer;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectFolderRenderer;
import org.betaconceptframework.astroboa.engine.jcr.renderer.ContentObjectRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrValueUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.VersionUtils;
import org.betaconceptframework.astroboa.engine.model.lazy.local.LazyComplexCmsPropertyLoader;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ContentObjectImpl;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ContentObjectProfileItem;
import org.betaconceptframework.astroboa.model.impl.item.DefinitionReservedName;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.CmsOutcomeImpl;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.astroboa.util.TreeDepth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentDao  extends JcrDaoSupport{

	private final Logger logger = LoggerFactory.getLogger(ContentDao.class);

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private ContentObjectDao contentObjectDao;

	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;

	@Autowired
	private ContentDefinitionDao contentDefinitionDao;

	@Autowired
	private BinaryChannelRenderer binaryChannelRenderer;

	@Autowired
	private ContentObjectFolderRenderer contentObjectFolderRenderer;

	@Autowired
	private ContentObjectRenderer contentObjectRenderer;

	@Autowired
	private VersionUtils versionUtils;

	@Autowired
	private LazyComplexCmsPropertyLoader lazyComplexCmsPropertyLoader;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Autowired
	private SerializationDao serializationDao;

	@Autowired
	private ImportDao importDao;
	/**
	 * 
	 * @param contentObject
	 * @param version
	 * @param lockToken
	 * @param updateLastModificationTime Flag to indicate that profile.modified should be updated
	 * @return
	 */
	public ContentObject saveContentObject(Object contentSource, boolean version, String lockToken, 
			boolean updateLastModificationTime, Context context)  {

		long start = System.currentTimeMillis();
		
		
		if (contentSource == null){
			throw new CmsException("Cannot save null content object");
		}
		
		if (contentSource instanceof String){
			
			logger.debug(" Starting saving content object. First import will take place");
			
			//Use importer to unmarshal String to ContentObject
			//and to save it as well.
			//What is happened is that importDao will create a ContentObject
			//and will pass it to ContentServiceImpl to save it. 
			//It will end up in this method again as a ContentObject
			//if it passes the check of SecureContentObjectSaveAspect
			ImportConfiguration configuration = ImportConfiguration.object()
				  .persist(PersistMode.PERSIST_ENTITY_TREE)
				  .version(version)
				  .updateLastModificationTime(updateLastModificationTime)
				  .build();

			return importDao.importContentObject((String)contentSource, configuration);
		}
		
		
		if (! (contentSource instanceof ContentObject)){
			throw new CmsException("Expecting either String or ContentObject and not "+contentSource.getClass().getName());
		}
		
		ContentObject contentObject = (ContentObject) contentSource;
		
		logger.debug(" Starting saving content object {}", contentObject.getSystemName());
		
		SaveMode saveMode = null;
		
		Session session = null;
		
		boolean disposeContextWhenSaveIsFinished = false;
		
		try {

			saveMode = cmsRepositoryEntityUtils.determineSaveMode(contentObject);
			
			//Populate content node with appropriate information
			if (context == null){
				context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, getSession());
				disposeContextWhenSaveIsFinished = true;
			}

			session = context.getSession();

			if (StringUtils.isNotBlank(lockToken)){
				session.getWorkspace().getLockManager().addLockToken(lockToken);
			}

			primaryCheck(contentObject);

			Node contentObjectNode = null;
			Calendar modifiedDateBeforeSave = null;

			switch (saveMode) {
			case INSERT:

				contentObjectNode = createNewContentObjectNode(contentObject, session, false);

				break;
			case UPDATE:
				contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObject.getId());

				if (contentObjectNode == null){
					//User has provided an id for content object. Create a new one with the specified id
					contentObjectNode = createNewContentObjectNode(contentObject, session, true);
				}
				else{

					//If node is locked and no valid lock token is provided then an exception is thrown
					session.getWorkspace().getVersionManager().checkout(contentObjectNode.getPath());

					modifiedDateBeforeSave = retrieveProfileModifiedFromContentObjectNode(contentObjectNode);

					checkModificationDateBeforeSave(contentObject,
							updateLastModificationTime, modifiedDateBeforeSave);
				}

				break;
			default:
				break;
			}

			contentObjectDao.populateContentObjectNode(session, contentObject, contentObjectNode, saveMode, context);

			//Check if someone has already been saved by another user
			checkModificationDateAfterSave(contentObject,
					updateLastModificationTime, contentObjectNode,
					modifiedDateBeforeSave);

			
			session.save();

			if (version){
				session.getWorkspace().getVersionManager().checkin(contentObjectNode.getPath());
			}
			
			//This must run after save was successful because these changes cannot not be rolled back
			((ComplexCmsPropertyImpl)contentObject.getComplexCmsRootProperty()).clearCmsPropertyNameWhichHaveBeenLoadedAndRemovedList();

			return contentObject;

		}
		catch(CmsException e)
		{
			throw e;
		}
		catch(Throwable e){
			throw new CmsException(e);
		}
		finally{
			if (disposeContextWhenSaveIsFinished){
				if (context != null){
					context.dispose();
					context = null;
				}
				
				if (StringUtils.isNotBlank(lockToken)){
					try {
						session.getWorkspace().getLockManager().removeLockToken(lockToken);
					}  catch (RepositoryException e) {
						logger.error("Lock token "+lockToken+" could not be removed", e);
					}
				}
			}
			
      		logger.debug(" Saved ContentObject {} in {}", contentObject.getSystemName(),  DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));

		}


	}

	private void checkModificationDateAfterSave(ContentObject contentObject,
			boolean updateLastModificationTime, Node contentObjectNode,
			Calendar modifiedDateBeforeSave)
	throws RepositoryException, ValueFormatException,
	PathNotFoundException, VersionException, LockException,
	ConstraintViolationException {

		if (modifiedDateBeforeSave != null){
			//Retrieve modified date now
			Calendar modifiedDateAfterSave = retrieveProfileModifiedFromContentObjectNode(contentObjectNode);

			if (modifiedDateAfterSave != null && modifiedDateAfterSave.getTimeInMillis() != modifiedDateBeforeSave.getTimeInMillis()){

				String pattern = "dd/MM/yyyy HH:mm:ss.SSSZ";
				throw new CmsConcurrentModificationException("Content Object "+ contentObject.getId() +"/" +contentObject.getSystemName()+ " has been concurrently modified by another user or current user has tried to " +
						" set a value for  profile.modified property. \n" +
						" profile.modified BEFORE save : "+ DateUtils.format(modifiedDateBeforeSave, pattern)+ "\n" +
						" profile.modified AFTER save : "+ DateUtils.format(modifiedDateAfterSave, pattern)	);
			}
			else{
				//Profile.modified will be updated if updateLastModificationTime is set to true
				//or profile.modified has no values
				if (updateLastModificationTime || modifiedDateAfterSave == null){

					//Update modification date
					Calendar modifiedDate = Calendar.getInstance();

					//Update jcr node
					contentObjectNode.getProperty("profile/modified").setValue(modifiedDate);

					//Update ContentObject
					if (contentObject.getComplexCmsRootProperty().isChildPropertyLoaded("profile.modified")){
						((CalendarProperty)contentObject.getCmsProperty("profile.modified")).setSimpleTypeValue(modifiedDate);
					}
				}
			}
		}
	}

	private Calendar checkModificationDateBeforeSave(ContentObject contentObject,
			boolean updateLastModificationTime, Calendar modifiedDateBeforeSave) {

		if (modifiedDateBeforeSave != null){
			//Make a check if provided ContentObject has a different modified date
			if (contentObject.getComplexCmsRootProperty() != null && contentObject.getComplexCmsRootProperty().isChildPropertyLoaded("profile.modified")){
				CalendarProperty modifiedDateProvidedByUserProperty = (CalendarProperty) contentObject.getCmsProperty("profile.modified");

				boolean throwException = false;

				if (modifiedDateProvidedByUserProperty.hasValues()){

					Calendar modifiedDateProvidedByUser = modifiedDateProvidedByUserProperty.getSimpleTypeValue();

					if (modifiedDateProvidedByUser == null || modifiedDateProvidedByUser.getTimeInMillis() != modifiedDateBeforeSave.getTimeInMillis()){
						throwException = true;
					}
					else{
						return modifiedDateBeforeSave;
					}
				}

				if (throwException){
					String pattern = "dd/MM/yyyy HH:mm:ss.SSSZ";
					throw new CmsConcurrentModificationException("Content Object "+ contentObject.getId() +"/" +contentObject.getSystemName()+ " has been concurrently modified by another user or current user has tried to " +
							" set a value for  profile.modified property. \n" +
							" profile.modified BEFORE save :   \t"+ DateUtils.format(modifiedDateBeforeSave, pattern)+ "(full info "+modifiedDateBeforeSave.toString()+")\n" +
							" profile.modified provided by user save : "+ (modifiedDateProvidedByUserProperty.hasNoValues() ? " No date ":
								DateUtils.format(modifiedDateProvidedByUserProperty.getSimpleTypeValue(), pattern)) + "(full info "+modifiedDateProvidedByUserProperty.getSimpleTypeValue()+")"
					);
				}
			}
		}

		return null;
	}

	private Calendar retrieveProfileModifiedFromContentObjectNode(Node contentObjectNode) throws RepositoryException,
	ValueFormatException, PathNotFoundException {
		if (! contentObjectNode.hasProperty("profile/modified")){
			return null;
		}
		else{
			return contentObjectNode.getProperty("profile/modified").getDate();
		}

	}

	private void primaryCheck(ContentObject contentObject) throws Exception {
		final String type = contentObject.getContentObjectType();
		//Check if this type is defined
		if (StringUtils.isBlank(type))
			throw new CmsException("Invalid type "+ type);

		if (!contentDefinitionDao.hasContentObjectTypeDefinition(type))
			throw new CmsException("Unregistered content object type "+ type+ " in repository "+AstroboaClientContextHolder.getActiveRepositoryId());

	}

	private Node createNewContentObjectNode(ContentObject contentObject, Session session, boolean useProvidedId ) throws Exception  {

		String type = contentObject.getContentObjectType();

		//		Get Type folder node. If it does not exist, it will be Created
		Node contentTypeFolderNode = JcrNodeUtils.retrieveOrCreateContentTypeFolderNode(session, type);

		CalendarInfo calendarInfo = new CalendarInfo(Calendar.getInstance());

		//Profile will provide year/month/day information
		String profileCreatedPropertyPath = ContentObjectProfileItem.Created.getItemForQuery().getLocalPart();

		//Check that property created is defined for type
		ContentObjectTypeDefinition typeDefinition = contentDefinitionDao.getContentObjectTypeDefinition(type);

		if (typeDefinition.hasCmsPropertyDefinition(profileCreatedPropertyPath)){
			CalendarProperty createdProperty = (CalendarProperty) contentObject.getCmsProperty(profileCreatedPropertyPath);
			
			if (createdProperty == null){
				//Should never happen
				throw new CmsException("Content type "+ typeDefinition.getName()+ " defines built in property profile.created but could not create property instance");
			}
			
			Calendar created =  (createdProperty.hasNoValues())? null : (Calendar) createdProperty.getSimpleTypeValue();

			if (created != null){
				//In case create date has unset YEAR, MONTH, OR DAY
				//Calendar Info will set default values
				calendarInfo = new CalendarInfo(created);
			}

			//Update Created date
			createdProperty.setSimpleTypeValue(calendarInfo.getCalendar());

		}
		else{
			//Should never happen
			throw new CmsException("Content type "+ typeDefinition.getName()+ " does not define built in property profile.created");
		}

		//If minute folder does not exist, it will be Created			
		Node contentObjectParentNode = JcrNodeUtils.createContentObjectParentNode(contentTypeFolderNode, calendarInfo );

		//Add new node. 
		//Node's name will be the same with typeName
		Node contentObjectNode = JcrNodeUtils.addContentObjectNode(contentObjectParentNode, type);

		//Create or use provided astroboa identifier
		cmsRepositoryEntityUtils.createCmsIdentifier(contentObjectNode, contentObject, useProvidedId);


		//Add modified date
		String profileModifiedPropertyPath = ContentObjectProfileItem.Modified.getItemForQuery().getLocalPart();

		if (typeDefinition.hasCmsPropertyDefinition(profileModifiedPropertyPath))
		{
			CalendarProperty modifiedProperty = (CalendarProperty) contentObject.getCmsProperty(profileModifiedPropertyPath);

			if (modifiedProperty.hasNoValues()){
				modifiedProperty.setSimpleTypeValue(calendarInfo.getCalendar());
			}
		}
		else
		{
			//Should never happen
			throw new CmsException("Content type "+ typeDefinition.getName()+ " does not define built in property profile.modified");
		}


		return contentObjectNode;
	}


	public BinaryChannel getBinaryChannelById(String binaryChannelId)   {
		Session session = null;

		if (StringUtils.isBlank(binaryChannelId))
			throw new CmsException("Blank binary channel id");

		try {

			session = getSession();

			Node binaryChannelNode =  cmsRepositoryEntityUtils.retrieveUniqueNodeForBinaryChannel(session, binaryChannelId);

			return binaryChannelRenderer.render(binaryChannelNode, true);

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}




	public List<ContentObjectFolder> getRootContentObjectFolders(String locale){
		return getRootContentObjectFolders(TreeDepth.ZERO.asInt(), locale);
	}

	public List<ContentObjectFolder> getRootContentObjectFolders(int depth, String locale)   {
		Session session = null;
		try {

			session = getSession();

			Node contentObjectRootNode =  JcrNodeUtils.getContentObjectRootNode(session);

			NodeIterator contentObjectRootFolderNodes = contentObjectRootNode.getNodes();

			List<ContentObjectFolder> outcome =  new ArrayList<ContentObjectFolder>();

			while (contentObjectRootFolderNodes.hasNext())
				outcome.add(contentObjectFolderRenderer.render(session, contentObjectRootFolderNodes.nextNode(), depth, false, locale));

			return outcome;

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public ContentObjectFolder getContentObjectFolderTree(String parentFolderId, int depth, boolean renderContentObjectIds, String locale)   {
		Session session = null;
		try {
			session = getSession();

			Node contentObjectFolderNode =  JcrNodeUtils.getNodeByNativeRepositoryIdentifier(session, parentFolderId);

			return  contentObjectFolderRenderer.render(session, contentObjectFolderNode, depth, renderContentObjectIds, locale);

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public boolean deleteContentObject(String objectIdOrSystemName)   {
		
		Session session = null;
		Context context = null;
		try {
			session = getSession();

			//Retrieve content object node

			Node contentObjectNode = getContentObjectNodeByIdOrSystemName(objectIdOrSystemName); 

			if (contentObjectNode != null){
				
				context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);

				contentObjectDao.removeContentObjectNode(contentObjectNode, true, session, context);

				session.save();
				
				return true;
			}

			logger.info("Object [] does not exist and therefore cannot be deleted", objectIdOrSystemName);
			return false;
			//Clear cache
			//jcrQueryCacheRegion.removeRegion();

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			if (context != null){
				context.dispose();
				context = null;
			}
		}

	}

	public ContentObject getContentObjectByVersionName(String contentObjectId, String versionName, String locale, CacheRegion cacheRegion)   {

		Session session = null;
		try {
			session = getSession();

			//Retrieve content object version history node
			VersionHistory contentObjectVersionHistory = versionUtils.getVersionHistoryForNode(session, contentObjectId);

			if (contentObjectVersionHistory ==null)
			{
				return null;
			}

			RenderProperties renderProperties = new RenderPropertiesImpl();
			//renderProperties.renderValuesForLocale(locale);
			renderProperties.renderVersionForContentObject(versionName);

			return contentObjectRenderer.render(session, contentObjectVersionHistory, renderProperties, new HashMap<String, ContentObjectTypeDefinition>(),
					new HashMap<String, CmsRepositoryEntity>());

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	public <T> T searchContentObjects(ContentObjectCriteria contentObjectCriteria, ResourceRepresentationType<T> contentObjectOutput){
		
		T queryResult = null;
		boolean queryReturnedAtLeastOneResult = false;
		
		ByteArrayOutputStream os = null;
		
		try {

			//Check if criteria is provided
			if (contentObjectCriteria == null){
				return generateEmptyOutcome(contentObjectOutput);
			}

			//Initialize null parameters (if any) 
			if (contentObjectOutput == null){
				contentObjectOutput = (ResourceRepresentationType<T>) ResourceRepresentationType.CONTENT_OBJECT_LIST;
			}
			
			//Check cache
			if (contentObjectCriteria.isCacheable()){

				queryResult = (T)jcrQueryCacheRegion.getJcrQueryResults(contentObjectCriteria, contentObjectOutput.getTypeAsString());
				
				if (queryResult != null){
					return queryResult;
				}
			}
			
			//User requested Objects as return type
			if (ResourceRepresentationType.CONTENT_OBJECT_INSTANCE.equals(contentObjectOutput)|| 
					ResourceRepresentationType.CONTENT_OBJECT_LIST.equals(contentObjectOutput)){
				
				CmsOutcome<ContentObject> outcome = contentObjectDao.searchContentObjects(contentObjectCriteria, getSession());

				//User requested one ContentObject. Throw an exception if more than
				//one returned
				if (ResourceRepresentationType.CONTENT_OBJECT_INSTANCE.equals(contentObjectOutput)){
					queryResult =  (T) returnSingleContentObjectFromOutcome(contentObjectCriteria, outcome);
					queryReturnedAtLeastOneResult = queryResult != null;
				}
				else{
					
					//Return type is CmsOutcome.
					queryResult =  (T) outcome;
					queryReturnedAtLeastOneResult = outcome.getCount() > 0;
				}
				
			}
			else if (ResourceRepresentationType.XML.equals(contentObjectOutput)|| 
					ResourceRepresentationType.JSON.equals(contentObjectOutput)){

				//User requested output to be XML or JSON
				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
						.prettyPrint(contentObjectCriteria.getRenderProperties().isPrettyPrintEnabled())
						.representationType(contentObjectOutput)
						.serializeBinaryContent(false)
						.build();

				long numberOfResutls  = serializationDao.serializeSearchResults(getSession(), contentObjectCriteria, os, FetchLevel.ENTITY, serializationConfiguration);

				queryReturnedAtLeastOneResult = numberOfResutls > 0;
				
				queryResult = (T) new String(os.toByteArray(), "UTF-8");

			}
			else{
				throw new CmsException("Invalid resource representation type for content object search results "+contentObjectOutput);
			}


			if (contentObjectCriteria.isCacheable()){
				String xpathQuery = contentObjectCriteria.getXPathQuery();
				if (!StringUtils.isBlank(xpathQuery) && queryReturnedAtLeastOneResult){
					jcrQueryCacheRegion.cacheJcrQueryResults(contentObjectCriteria, 
							queryResult, contentObjectCriteria.getRenderProperties(), contentObjectOutput.getTypeAsString());
				}
			}
			
			return queryResult;

		}

	catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}	
	}

	private ContentObject returnSingleContentObjectFromOutcome(ContentObjectCriteria contentObjectCriteria,
			CmsOutcome<ContentObject> outcome) {
		//User set limit to 1. Return content object found
		//a result is returned
		if (outcome.getLimit() == 1){
			if (CollectionUtils.isNotEmpty(outcome.getResults())){
				return outcome.getResults().get(0);
			}
			else {
				return null;
			}
		}
		else{
			//User specified limit different than 1 (either no  limit or greater than 1)
			if (outcome.getCount() > 1){
				throw new CmsException(outcome.getCount() +" content objects matched criteria, user has specified limit "+
						contentObjectCriteria.getLimit() + " but she also requested return type to be a single ContentObject.");
			}
			else{
				if (CollectionUtils.isNotEmpty(outcome.getResults())){
					return outcome.getResults().get(0);
				}
				else {
					return null;
				}	
			}
		}
	}
	
	private <T> T generateEmptyOutcome(
			ResourceRepresentationType<T> contentObjectOutput) {
		if (contentObjectOutput != null && contentObjectOutput.equals(ResourceRepresentationType.CONTENT_OBJECT_LIST)){
			return (T) new CmsOutcomeImpl<T>(0, 0, 0);
		}
		else{
			return null;
		}
	}
	

	public String lockContentObject(String contentObjectId)   {
		Session session = null;
		try {

			session = getSession();

			Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectId);

			if (contentObjectNode == null){
				return null;
			}

			//Deep lock and open-scoped
			if (!contentObjectNode.isLocked())
				session.getWorkspace().getLockManager().lock(contentObjectNode.getPath(), true, false, Long.MAX_VALUE, "");

			String lockToken =  session.getWorkspace().getLockManager().getLock(contentObjectNode.getPath()).getLockToken();

			if (lockToken != null)
			{
				session.getWorkspace().getVersionManager().checkout(contentObjectNode.getPath());
				contentObjectNode.setProperty(DefinitionReservedName.Locktoken.getJcrName(), lockToken);
			}
			else
			{
				//Lock Operation may have failed. Try get locktoken from contentObjectNode
				if (contentObjectNode.hasProperty(DefinitionReservedName.Locktoken.getJcrName()))
					lockToken = contentObjectNode.getProperty(DefinitionReservedName.Locktoken.getJcrName()).getString();
			}

			session.save();

			return lockToken;

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public void removeLockFromContentObject(String contentObjectId, String lockToken)   {
		Session session = null;
		try {
			session = getSession();

			Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectId);

			if (contentObjectNode != null && contentObjectNode.isLocked())
			{
				//Deep lock and open-scoped
				session.getWorkspace().getLockManager().addLockToken(lockToken);

				session.getWorkspace().getLockManager().unlock(contentObjectNode.getPath());

				//Unset contentObject equivalent property
				if (contentObjectNode.hasProperty(DefinitionReservedName.Locktoken.getJcrName()))
				{
					session.getWorkspace().getVersionManager().checkout(contentObjectNode.getPath());
					contentObjectNode.getProperty(DefinitionReservedName.Locktoken.getJcrName()).remove();
				}

				session.save();
			}
		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	public boolean isContentObjectLocked(String contentObjectId)   {
		Session session = null;
		try {
			session = getSession();

			Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectId);

			if (contentObjectNode == null){
				return false;
			}

			return contentObjectNode.isLocked();


		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}


	public void increaseContentObjectViewCounter(String contentObjectId, long counter)   {

		Session session = null;
		try {

			session = getSession();
			
			Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectId);

			if (contentObjectNode == null){
				logger.warn("Unable to find content object with id "+ contentObjectId+". Property 'viewCounter' is not increased");
			}
			else{
				session.getWorkspace().getVersionManager().checkout(contentObjectNode.getPath());

				contentObjectDao.increaseViewCounter(contentObjectNode, counter);

				session.save();
			}


		}catch(RepositoryException ex)
		{
			logger.error("",ex);
			throw new CmsException(ex);
		}
		catch (Exception e) {
			logger.error("",e);
			throw new CmsException(e);
		}
	}

	public List<CmsProperty<?, ?>> loadChildCmsProperty(String childPropertyName,
			String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties) throws Exception {

		CmsPropertyDefinition currentChildPropertyDefinition = contentDefinitionDao.getCmsPropertyDefinition(PropertyPath.createFullPropertyPath(parentComplexCmsPropertyDefinitionFullPath, childPropertyName));

		//Since definition does not exist and its parent is a content object type, which  
		//derives from the fact that parent full path has only one level
		//check if child refers to an aspect
		if (currentChildPropertyDefinition == null && !parentComplexCmsPropertyDefinitionFullPath.contains(CmsConstants.PERIOD_DELIM)){
			currentChildPropertyDefinition = contentDefinitionDao.getAspectDefinition(childPropertyName);

			//At this point if such a definition exists we cannot tell that the aspect found is actually defined
			//for the content object which contains parent complex property.
			//This will be detected once the created template is actually added to the content object

		}

		if (currentChildPropertyDefinition == null){
			logger.warn("No cms property definition is provided for property {}.{} . ( parent node UUID {} and content object node UUID {} )",
					new Object[]{parentComplexCmsPropertyDefinitionFullPath, childPropertyName,
					jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct});
			return null;
		}
		else{
			return lazyComplexCmsPropertyLoader.renderChildProperty(currentChildPropertyDefinition, 
					jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct, 
					renderProperties, getSession(), null);
		}
	}

	public void moveAspectToNativePropertyForAllContentObjectsOFContentType(
			String aspect, String newPropertyName, String contentType) {

		if (StringUtils.isBlank(aspect)){
			throw new CmsException("No aspect is provided");
		}

		if (StringUtils.isBlank(contentType)){
			throw new CmsException("No content type is provided");
		}

		Session session = null;
		try {

			session = getSession();
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria(contentType);


			CmsQueryResult contentObjectResults = cmsQueryHandler.getNodesFromXPathQuery(session, contentObjectCriteria, true);

			if (contentObjectResults.getTotalRowCount() > 0){

				NodeIterator contentObjectNodesIterator = (NodeIterator) contentObjectResults.getNodeIterator();

				int count = 0;
				while (contentObjectNodesIterator.hasNext()){

					Node contentObjectNode = contentObjectNodesIterator.nextNode();

					if (contentObjectNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName())){
						if (contentObjectNode.hasProperty(CmsBuiltInItem.Aspects.getJcrName())){
							Value[] values = contentObjectNode.getProperty(CmsBuiltInItem.Aspects.getJcrName()).getValues();

							logger.info("ContentObjectNode "+contentObjectNode.getPath() + " has aspects "+ 
									printValues(values));

							if (values != null){
								for (Value value : values){
									if (value.getString() != null && value.getString().equals(aspect)){

										//Aspect found. Remove it from array
										JcrValueUtils.removeValue(contentObjectNode, CmsBuiltInItem.Aspects, session.getValueFactory().createValue(aspect), true);

										count++;

										if (contentObjectNode.hasProperty(CmsBuiltInItem.Aspects.getJcrName())){
											logger.info("After removal ContentObjectNode "+contentObjectNode.getPath() + " has aspects "+ 
													printValues(values));
										}
										else{
											logger.info("After removal ContentObjectNode "+contentObjectNode.getPath() + " does not have any aspect ");
										}

										//Refactor the property only if the provided is different
										if (StringUtils.isNotBlank(newPropertyName) && ! newPropertyName.equals(aspect)){

											if (!contentObjectNode.hasNode(aspect)){
												logger.warn("Although aspect "+ aspect + " was found in "+CmsBuiltInItem.Aspects.getJcrName() + " no child property named after aspect "+
												" was found. Check further if there is a bug");
											}
											else{
												NodeIterator aspectNodes = contentObjectNode.getNodes(aspect);

												if (aspectNodes.getSize() > 1){
													logger.warn("There are more than one child properties named after aspect "+aspect+ ". This is however not the case." +
															"Check further if there is a bug. Refactoring will taking place normally as by now this property is a native property" +
															" and therefore can have multiple occurences. Make sure this is depicted in content type definition of "+contentType);
												}

												while (aspectNodes.hasNext()){
													Node aspectNode = aspectNodes.nextNode();

													session.move(aspectNode.getPath(), contentObjectNode.getPath()+CmsConstants.FORWARD_SLASH+newPropertyName);
												}
											}
										}

										break;
									}
								}
							}
						}
					}
					else{
						throw new CmsException("In a query for content objects of type "+ contentType + " this node "+ contentObjectNode.getPath() + " found in results but it is not a structured content object node");
					}
				}

				session.save();

				logger.info("Successfully move aspect in "+count + " content objects");
			}
		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	private String printValues(Value[] values) throws Exception {
		if (values == null){
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (Value value : values){
			stringBuilder.append(value.getString()+ " ");
		}

		return stringBuilder.toString();
	}

	public Node getContentObjectNodeByIdOrSystemName(String contentObjectIdOrSystemName){
		try{
			Node contentObjectNode = null;
			
			if (CmsConstants.UUIDPattern.matcher(contentObjectIdOrSystemName).matches()){
				contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(getSession(), contentObjectIdOrSystemName);

				if (contentObjectNode != null){
					return contentObjectNode;
				}
			}
			else{
				ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
				contentObjectCriteria.addSystemNameEqualsCriterion(contentObjectIdOrSystemName);
				contentObjectCriteria.setOffsetAndLimit(0, 1);
				
				CmsQueryResult nodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), contentObjectCriteria, true);
				
				if (nodes.getTotalRowCount() > 0){
					return ((NodeIterator) nodes.getNodeIterator()).nextNode();
				}
			}
			
			return null;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T serializeContentObject(String contentObjectIdOrSystemName,
			CacheRegion cacheRegion, ResourceRepresentationType<T> contentObjectOutput, List<String> propertyPathsToInclude, FetchLevel fetchLevel, 
			boolean serializeBinaryContent, boolean prettyPrint) {
		
		ByteArrayOutputStream os = null;
		
		try {

			//Default values
			if (contentObjectOutput == null){
				contentObjectOutput = (ResourceRepresentationType<T>) ResourceRepresentationType.CONTENT_OBJECT_INSTANCE;
			}
			
			T contentObject = null;
			
			//Check cache
			contentObject = getContentObjectFromcache(contentObjectIdOrSystemName, cacheRegion,	contentObjectOutput);

			if (contentObject != null){
				return contentObject;
			}
			
			//Content Object is not in the cache
			//Continue with the default values
			if (fetchLevel == null){
				fetchLevel = FetchLevel.ENTITY;
			}
			
			propertyPathsToInclude = generateIfNecessaryPropertyPathsWhoseValuesWillBeIncludedInSerialization(fetchLevel, propertyPathsToInclude,contentObjectOutput);
			
			Node contentObjectNode = getContentObjectNodeByIdOrSystemName(contentObjectIdOrSystemName);

			if (contentObjectNode == null){
				return generateEmptyOutcome(contentObjectOutput);
			}

			if (ResourceRepresentationType.CONTENT_OBJECT_INSTANCE.equals(contentObjectOutput)|| 
					ResourceRepresentationType.CONTENT_OBJECT_LIST.equals(contentObjectOutput)){
				
				RenderProperties renderProperties = new RenderPropertiesImpl();
				
				if (FetchLevel.FULL == fetchLevel){
					renderProperties.renderAllContentObjectProperties(true);
				}
				
				contentObject = (T) contentObjectRenderer.render(getSession(), contentObjectNode, 
						renderProperties, new HashMap<String, ContentObjectTypeDefinition>(),  
						new HashMap<String, CmsRepositoryEntity>());
				
				//Load properties
				if (CollectionUtils.isNotEmpty(propertyPathsToInclude)){
					for (String propertyPath : propertyPathsToInclude){
						((ContentObject)contentObject).getCmsProperty(propertyPath);
					}
				}
				
				//Return appropriate type
				if (ResourceRepresentationType.CONTENT_OBJECT_LIST.equals(contentObjectOutput )){

					//Return type is CmsOutcome.
					CmsOutcome<ContentObject> outcome = new CmsOutcomeImpl<ContentObject>(1, 0, 1);
					outcome.getResults().add((ContentObject)contentObject);
					
					contentObject = (T) outcome;
				}
				
			}
			else if (ResourceRepresentationType.XML.equals(contentObjectOutput)|| 
					ResourceRepresentationType.JSON.equals(contentObjectOutput)){
			

				os = new ByteArrayOutputStream();

				SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
						.prettyPrint(prettyPrint)
						.representationType(contentObjectOutput)
						.serializeBinaryContent(serializeBinaryContent)
						.build();

				serializationDao.serializeCmsRepositoryEntity(contentObjectNode, os, CmsEntityType.OBJECT, propertyPathsToInclude, fetchLevel, true, serializationConfiguration);

				contentObject =  (T) new String(os.toByteArray(), "UTF-8");
			}
			else {
				throw new CmsException("Unsupported resource representation type for content object serialization "+contentObjectOutput);
			}


			if (cacheRegion != null){
				jcrQueryCacheRegion.cacheContentObject(contentObjectIdOrSystemName, contentObject, cacheRegion, contentObjectOutput.getTypeAsString());
			}

			return contentObject;

		}catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}
	}

	private List<String> generateIfNecessaryPropertyPathsWhoseValuesWillBeIncludedInSerialization(FetchLevel fetchLevel, List<String> propertyPathsToInclude, ResourceRepresentationType<?> contentObjectOutput) {

		//Property Paths to be included in the serialization are ignored id FetchLevel is FULL
		if (fetchLevel == FetchLevel.FULL){
			return null;
		}

		if (CollectionUtils.isEmpty(propertyPathsToInclude)){
			
			//FetchLevel is either ENTITY or ENTITY_AND_CHILDREN but user did not specify any properties at all.
			//In this case profile.title is returned and owner

			propertyPathsToInclude = new ArrayList<String>();
			
			propertyPathsToInclude.add("profile.title");

			//Owner is always serialized when type is CONTENT_OBJECT_INSTANCE or CONTENT_OBJECT_LIST
			//as it is a special case of a property 
			//However in the cases of XML or JSON, it should be specified as a regular one
			if (ResourceRepresentationType.XML.equals(contentObjectOutput)|| 
					ResourceRepresentationType.JSON.equals(contentObjectOutput)){
				
					propertyPathsToInclude.add(CmsConstants.OWNER_ELEMENT_NAME);
			}
		}
		
		return propertyPathsToInclude;
	}

	private <T> T getContentObjectFromcache(String contentObjectIdOrSystemName,	CacheRegion cacheRegion,
			ResourceRepresentationType<T> contentObjectOutput)
			throws Exception {
		if (cacheRegion != null){
			return (T) jcrQueryCacheRegion.getContentObjectFromCache(contentObjectIdOrSystemName, cacheRegion, contentObjectOutput.getTypeAsString());
		}
		
		return null;
	}

	/**
	 * @param contetObjectId
	 * @return
	 */
	public ContentObject copyContentObject(String contentObjectId) {
		
		if (StringUtils.isBlank(contentObjectId)){
			return null;
		}
		
		ContentObject contentObjectToBeCopied = 
			serializeContentObject(contentObjectId, CacheRegion.NONE, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, null, FetchLevel.FULL, true,false);
		
		if (contentObjectToBeCopied == null){
			logger.warn("Could not retrieve content object with id {}. Copy operation cannot continue", contentObjectId);
			return null;
		}
		
		((ContentObjectImpl)contentObjectToBeCopied).clean();
		
		int index = 0;

		String systemNameToSearch = null;
		if (StringUtils.isNotBlank(contentObjectToBeCopied.getSystemName())){
			systemNameToSearch = new String(contentObjectToBeCopied.getSystemName().getBytes());
			
			if (systemNameToSearch.startsWith("copy")){
				systemNameToSearch = systemNameToSearch.replaceFirst("copy[0-9]*", "");
			}
			
			//Locate other copies in order to provide the correct copy index
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.addSystemNameContainsCriterion("*"+systemNameToSearch);
			contentObjectCriteria.setOffsetAndLimit(0, 0);
			contentObjectCriteria.setCacheable(CacheRegion.NONE);

			CmsOutcome<ContentObject> outcome = searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			if (outcome != null){
				index = (int)outcome.getCount();
			}
		}
		
		String newIndexAsString = index == 0 ? "" : String.valueOf(index+1);

		//Adjust systemName
		if (contentObjectToBeCopied.getSystemName() == null){
			contentObjectToBeCopied.setSystemName("copy"+newIndexAsString);
		}
		else{
			if (contentObjectToBeCopied.getSystemName().startsWith("copy")){
				contentObjectToBeCopied.setSystemName(contentObjectToBeCopied.getSystemName().replaceFirst("copy[0-9]*", "copy"+newIndexAsString));
			}
			else{
				contentObjectToBeCopied.setSystemName("copy"+newIndexAsString+contentObjectToBeCopied.getSystemName());
			}
		}

		//Adjust title
		StringProperty titleProperty = (StringProperty) contentObjectToBeCopied.getCmsProperty("profile.title");
		
		String title = titleProperty.getSimpleTypeValue();
		
		boolean titleHasChanged = false;
		
		for (int i=1;i<=index;i++){
			if (title.endsWith(" "+String.valueOf(i))){
				titleProperty.setSimpleTypeValue(StringUtils.substringBeforeLast(title, String.valueOf(i))+newIndexAsString);
				titleHasChanged = true;
				break;
			}
		}
		
		if (! titleHasChanged){
			titleProperty.setSimpleTypeValue(title+ " "+newIndexAsString);
		}

		return contentObjectToBeCopied;
		
	}
	
	public boolean valueForPropertyExists(String propertyPath,
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty) throws Exception {
		
			return lazyComplexCmsPropertyLoader.valueForPropertyExists(propertyPath, jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, getSession());
	}

	public List<ContentObject> saveContentObjectResourceCollection(Object contentSource, boolean version,boolean updateLastModificationTime, String lockToken) {
		
		long start = System.currentTimeMillis();
		
		if (contentSource == null){
			return new ArrayList<ContentObject>();
		}
		
		if (contentSource instanceof String){
			
			logger.debug(" Starting saving content object resource collection.");
			
			//Use importer to unmarshal String to ContentObject
			//and to save it as well.
			//What is happened is that importDao will create a ContentObject
			//and will pass it to ContentServiceImpl to save it. 
			//It will end up in this method again as a ContentObject
			//if it passes the check of SecureContentObjectSaveAspect
			ImportConfiguration configuration = ImportConfiguration.object()
					  .persist(PersistMode.PERSIST_ENTITY_TREE)
					  .version(version)
					  .updateLastModificationTime(updateLastModificationTime)
					  .build();

			return importDao.importResourceCollection((String)contentSource, configuration);
		}
		
		
		if (! (contentSource instanceof List)){
			throw new CmsException("Expecting either String or List<ContentObject> and not "+contentSource.getClass().getName());
		}
		
		logger.debug(" Starting saving content object collection");
		
		Session session = null;
		
		Context context = null;
		
		List<ContentObject> contentObjects = (List<ContentObject>) contentSource;
		
		try {

			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, getSession());

			session = context.getSession();

			if (StringUtils.isNotBlank(lockToken)){
				session.getWorkspace().getLockManager().addLockToken(lockToken);
			}
			
			for (ContentObject contentObject : contentObjects){
				contentObject = saveContentObject(contentObject, version, lockToken, updateLastModificationTime, context);
			}
			
			session.save();

			return contentObjects;

		}
		catch(CmsException e){
			throw e;
		}
		catch(Throwable e){
			throw new CmsException(e);
		}
		finally{
			if (context != null){
				context.dispose();
				context = null;
			}
			
			if (StringUtils.isNotBlank(lockToken)){
				try {
					session.getWorkspace().getLockManager().removeLockToken(lockToken);
				} catch (RepositoryException e) {
					logger.error("Lock token "+lockToken+" could not be removed", e);
				}
			}
			
      		logger.debug(" Saved ContentObject collection  in {}", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));

		}
	}

	public byte[] getBinaryChannelContent(
			String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel) {
		
		Session session = null;

		if (StringUtils.isBlank(jcrNodeUUIDWhichCorrespondsToTheBinaryChannel))
			throw new CmsException("Blank binary channel id");

		try {

			session = getSession();

			Node binaryChannelNode =  cmsRepositoryEntityUtils.retrieveUniqueNodeForBinaryChannel(session, jcrNodeUUIDWhichCorrespondsToTheBinaryChannel);

			if (binaryChannelNode == null){
				logger.warn("Binary Channel Id {} does not correspond to a valid JCR node");
				return null;
			}
			
			if (binaryChannelNode.hasProperty(JcrBuiltInItem.JcrData.getJcrName())){
				return (byte[])JcrValueUtils.getObjectValue(binaryChannelNode.getProperty(JcrBuiltInItem.JcrData.getJcrName()).getValue());
			}
			else {
				return null;
			}

			

		}catch(RepositoryException ex)
		{
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}
}
