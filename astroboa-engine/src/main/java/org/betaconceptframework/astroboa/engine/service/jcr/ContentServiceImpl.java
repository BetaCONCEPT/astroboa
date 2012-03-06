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

package org.betaconceptframework.astroboa.engine.service.jcr;


import java.util.List;

import javax.jcr.Node;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDao;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
public class ContentServiceImpl  implements ContentService {


	@Autowired
	private ContentDao contentDao;
	
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public ContentObject saveLockedContentObject(ContentObject contentObject, boolean version, String lockToken)   {

		try { 
			return contentDao.saveContentObject(contentObject, version, lockToken, true,  null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public ContentObject saveContentObject(ContentObject contentObject, boolean version)   {
		try{ 
			return contentDao.saveContentObject(contentObject, version, null, true,  null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
	
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public ContentObject saveAndVersionLockedContentObject(ContentObject contentObject, String lockToken)   {
		try{ 
			return contentDao.saveContentObject(contentObject, true, lockToken, true,  null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public ContentObject saveAndVersionContentObject(ContentObject contentObject)   {
		try{ 
			return contentDao.saveContentObject(contentObject, true, null, true, null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Deprecated
	public ContentObject getContentObjectById(String contentObjectId, CacheRegion cacheRegion)   {
		try{ 
			return contentDao.serializeContentObject(contentObjectId, cacheRegion, 
					ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
					null, 
					FetchLevel.ENTITY, 
					false,
					false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Deprecated
	public ContentObject getContentObjectByIdAndLocale(String contentObjectId, String locale, CacheRegion cacheRegion){
		try{
			return contentDao.serializeContentObject(contentObjectId, cacheRegion, 
					ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
					null, 
					FetchLevel.ENTITY, 
					false,
					false);

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public BinaryChannel getBinaryChannelById(String binaryChannelId)   {
		try{ 
			return contentDao.getBinaryChannelById(binaryChannelId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}


	public List<ContentObjectFolder> getRootContentObjectFolders(String locale)   {
		try{ 
			return contentDao.getRootContentObjectFolders(locale);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public ContentObjectFolder getContentObjectFolderTree(String parentFolderId, int depth, boolean renderContentObjectIds, String locale)   {
		try{ 
			return contentDao.getContentObjectFolderTree(parentFolderId, depth, renderContentObjectIds, locale);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Deprecated
	public ContentObject getContentObject(String contentObjectIdOrSystemName, RenderProperties renderProperties, CacheRegion cacheRegion)   {
		try{
			
			FetchLevel fetchLevel = FetchLevel.ENTITY;
			
			if (renderProperties != null &&	renderProperties.allContentObjectPropertiesAreRendered()){
				fetchLevel = FetchLevel.FULL;
			}
			
			return contentDao.serializeContentObject(contentObjectIdOrSystemName, cacheRegion, 
					ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, 
					null, 
					fetchLevel, 
					false,
					false);

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public boolean deleteContentObject(String objectIdOrSystemName)   {
		try{
			return contentDao.deleteContentObject(objectIdOrSystemName);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public ContentObject getContentObjectByVersionName(String contentObjectId, String versionName, String locale, CacheRegion cacheRegion)   {

		try{ 
			return contentDao.getContentObjectByVersionName(contentObjectId, versionName, locale, cacheRegion);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	//TODO : Must change CmsRankedOutcome so that it extends CmsRepositoryEntity
	@Deprecated
	public CmsOutcome<CmsRankedOutcome<ContentObject>> searchContentObjects(ContentObjectCriteria contentObjectCriteria)   {

		try{ 
			return contentDao.searchContentObjects(contentObjectCriteria);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public String lockContentObject(String contentObjectId)   {
		try{
			return contentDao.lockContentObject(contentObjectId);

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void removeLockFromContentObject(String contentObjectId, String lockToken)   {
		try{
			contentDao.removeLockFromContentObject(contentObjectId, lockToken);

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public boolean isContentObjectLocked(String contentObjectId)   {
		try{ 
			return contentDao.isContentObjectLocked(contentObjectId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}


	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public synchronized void increaseContentObjectViewCounter(String contentObjectId,long counter)   {
		try{ 
			contentDao.increaseContentObjectViewCounter(contentObjectId, counter);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	public List<CmsProperty<?, ?>> loadChildCmsProperty(String childPropertyName,
			String parentComplexCmsPropertyDefinitionFullPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, String jcrNodeUUIDWhichCorrespondsToContentObejct, 
			RenderProperties renderProperties) {
		try{ 
			return contentDao.loadChildCmsProperty(childPropertyName,	parentComplexCmsPropertyDefinitionFullPath, 
					jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty, jcrNodeUUIDWhichCorrespondsToContentObejct, 
					renderProperties);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	}

	@Override
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void moveAspectToNativePropertyForAllContentObjectsOFContentType(
			String aspect, String newPropertyName, String contentType) {
		contentDao.moveAspectToNativePropertyForAllContentObjectsOFContentType(
				aspect, newPropertyName, contentType);
		
	}

	@Override
	@Deprecated
	public String searchContentObjectsAndExportToXml(
			ContentObjectCriteria contentObjectCriteria) {
		try{ 
			return contentDao.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.XML);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	@Deprecated
	public ContentObject saveContentObject(ContentObject contentObject,
			boolean version, boolean updateLastModificationDate) {
		try{ 
			return contentDao.saveContentObject(contentObject, version, null, updateLastModificationDate, null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	@Deprecated
	public String searchContentObjectsAndExportToJson(
			ContentObjectCriteria contentObjectCriteria) {
		try{ 
			return contentDao.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Node getContentObjectNodeByIdOrSystemName(String contentObjectIdOrSystemName){
		try{
			return contentDao.getContentObjectNodeByIdOrSystemName(contentObjectIdOrSystemName);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public ContentObject saveContentObjectInBatchMode(ContentObject contentObject,
			boolean version, boolean updateLastModificationDate, Context context) {
		try{ 
			return contentDao.saveContentObject(contentObject, version, null, updateLastModificationDate, context);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public <T> T getContentObject(String contentObjectIdOrSystemName,
			ResourceRepresentationType<T> output, FetchLevel fetchLevel,
			CacheRegion cacheRegion, List<String> propertyPathsToInclude, 
			boolean serializeBinaryContent) {
		
		try{
			return contentDao.serializeContentObject(contentObjectIdOrSystemName, cacheRegion, output, propertyPathsToInclude, fetchLevel, serializeBinaryContent,false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public ContentObject save(Object contentObject, boolean version,
			boolean updateLastModificationTime, String lockToken) {
		
		try{
			return contentDao.saveContentObject(contentObject, version, lockToken, updateLastModificationTime, null);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}
	
	@Override
	public <T> T searchContentObjects(
			ContentObjectCriteria contentObjectCriteria,
			ResourceRepresentationType<T> output) {
		try{
			return contentDao.searchContentObjects(contentObjectCriteria, output);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		} 

	}

	@Override
	public ContentObject copyContentObject(String contetObjectId) {

		try{
			return contentDao.copyContentObject(contetObjectId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
	
	@Override
	public boolean hasValueForProperty(String propertyPath, 
			String jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty) {
		try{ 
			return contentDao.valueForPropertyExists(propertyPath,jcrNodeUUIDWhichCorrespondsToParentComplexCmsProperty);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}	
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public List<ContentObject> saveContentObjectResourceCollection(Object contentSource, boolean version,
			boolean updateLastModificationTime, String lockToken) {
		
		try{
			return contentDao.saveContentObjectResourceCollection(contentSource, version, updateLastModificationTime, lockToken);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public byte[] getBinaryChannelContent(String jcrNodeUUIDWhichCorrespondsToTheBinaryChannel) {
		try{ 
			return contentDao.getBinaryChannelContent(jcrNodeUUIDWhichCorrespondsToTheBinaryChannel);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
}