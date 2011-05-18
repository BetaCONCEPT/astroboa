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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.core.PropertyImpl;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.query.QueryImpl;
import org.apache.jackrabbit.core.query.lucene.QueryResultImpl;
import org.apache.jackrabbit.core.state.CacheManager;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.uuid.UUID;
import org.apache.jackrabbit.value.ValueHelper;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.configuration.JcrCacheType;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.configuration.RepositoryType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all methods which must use Jackrabbit classes
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JackrabbitDependentUtils {

	private static  final Logger logger = LoggerFactory.getLogger(JackrabbitDependentUtils.class);

	//The maximum amount of memory in MB to distribute across the caches.  JCR implementatiom (Jackrabbit) Default is 16MB
	public static final int defaultMaxMemory = 256;
	
	//The maximum memory per cache in MB JCR implementatiom (Jackrabbit) Default is 4MB
	public static final int defaultMaxMemoryPerCache = 4;

	//The minimum memory per cache in KB  JCR implementatiom (Jackrabbit)  Default is 128KB
	public static final int defaultMinMemoryPerCache = 128;

	public static String getRepositoryHomeDir(Repository repository){
		return ((RepositoryImpl)repository).getConfig().getHomeDir();
	}
	
	public static void setupCacheManager(Repository repository, String repositoryId) {
		
		int maxMemory = defaultMaxMemory;
		int maxMemoryPerCache = defaultMaxMemoryPerCache;
		int minMemoryPerCache = defaultMinMemoryPerCache;
		
		if (repositoryId != null){
			RepositoryType repositoryConfig = RepositoryRegistry.INSTANCE.getRepositoryConfiguration(repositoryId);
			
			if (repositoryConfig!=null){
				JcrCacheType jcrCacheConfig = repositoryConfig.getJcrCache();
				
				if (jcrCacheConfig != null){
					maxMemory = jcrCacheConfig.getMaxMemory();
					maxMemoryPerCache = jcrCacheConfig.getMaxMemoryPerCache();
					minMemoryPerCache = jcrCacheConfig.getMinMemoryPerCache();
				}
			}
			
		}	
		CacheManager manager = ((RepositoryImpl) repository).getCacheManager();
		
		if (maxMemory > 0){
			manager.setMaxMemory(maxMemory * 1024L * 1024L);
		}
		if (maxMemoryPerCache > 0){
			manager.setMaxMemoryPerCache(maxMemoryPerCache *1024L * 1024L);
		}
		if (minMemoryPerCache > 0){
			manager.setMinMemoryPerCache(minMemoryPerCache * 1024L);
		}
		
	}
	
	public static void register(Workspace workspace, InputStream repositoryNodeTypeDefinitionInputStream, boolean reregisterExisting) throws FileNotFoundException, IOException, RepositoryException{
		((NodeTypeManagerImpl)workspace.getNodeTypeManager()).registerNodeTypes(repositoryNodeTypeDefinitionInputStream,
				JackrabbitNodeTypeManager.TEXT_X_JCR_CND, reregisterExisting);
	
	}
	
	public static Node getNodeFromRow(Row row) {
		
		if (row instanceof org.apache.jackrabbit.api.jsr283.query.Row){
			try {
				return ((org.apache.jackrabbit.api.jsr283.query.Row)row).getNode();
			} catch (RepositoryException e) {
				logger.error("",e);
				return null;
			}
		}
		
		return null;
	}

	public static double getScoreFromRow(Row row) {
		
		if (row instanceof org.apache.jackrabbit.api.jsr283.query.Row){
			try {
				return ((org.apache.jackrabbit.api.jsr283.query.Row)row).getScore();
			} catch (RepositoryException e) {
				logger.error("",e);
				return 0;
			}
		}
		
		return 0;
	}

	//String[0] is absolute path
	//String[1] is relative path
	public static String[] createPathsForBinaryContent(Node binaryDataNode,Session session) throws Exception,
	PathNotFoundException, RepositoryException
	{
		//Retrieve repository and datastore path
		RepositoryImpl repository = (RepositoryImpl) session.getRepository();

		String repositoryHomeDir = repository.getConfig().getHomeDir();

		if (! (repository.getDataStore() instanceof FileDataStore)){
			logger.warn("Unable to generate absolute and relative path for binary channel. Jackrabbit data store is not of type 'FileDataStore'");
			return null;
		}
			
		//It contains value of parameter 'path' of 'DataStore' element in repository.xml 
		String dataStorePath = ((FileDataStore)repository.getDataStore()).getPath();

		PropertyImpl propertyWhichContainsContent = (PropertyImpl)binaryDataNode.getProperty(JcrBuiltInItem.JcrData.getJcrName());

		//According to Jackrabbit source code this has the form of 
		//datastore:<some_identifier>
		String identifier = propertyWhichContainsContent.internalGetValue().getBLOBFileValue().toString();
		//Remove 'datastore' prefix
		identifier = identifier.replace("dataStore:", "");
			
		//File path for binary value is calculated according to FileDataStore#getFile(identifier)
		//String filename = StringUtils.isBlank(binaryChannel.getSourceFilename()) ? "nofilename" : binaryChannel.getSourceFilename();
		//String mimeType = StringUtils.isBlank(binaryChannel.getMimeType()) ? "noMimeType" : binaryChannel.getMimeType();
		String binaryPath = identifier.substring(0,2)+File.separator+
								identifier.substring(2,4)+File.separator+
								identifier.substring(4,6)+File.separator+
								identifier; //+ File.separator +
								//filename + "." + mimeType;
		
		
		String[] paths = new String[2];
		
		//<repoHomeDir>/datastore/<dir>/<dir>/<dir>/identifier
		paths[0] = dataStorePath+File.separator+binaryPath;
		
		
		//Relative path must not contain repository home dir.
		//datastore/<dir>/<dir>/<dir>/identifier
		String dataStoreDir = StringUtils.replace(dataStorePath, repositoryHomeDir, "");
		
		if (dataStoreDir.startsWith(File.separator)){
			dataStoreDir = StringUtils.removeStart(dataStoreDir, File.separator);
		}
		else if (dataStoreDir.startsWith("/")){
			//It may be the case that it is running in windows mode but nevertheless datastore path 
			//may come with the format c:\repository\home/datastore
			dataStoreDir = StringUtils.removeStart(dataStoreDir, "/");
		}
		
		paths[1] = dataStoreDir+File.separator+binaryPath;
		
		return paths;

	}
	
	public static void createPathForBinaryContent(Node binaryDataNode,
			BinaryChannel binaryChannel, Session session) throws Exception,
			PathNotFoundException, RepositoryException {

		String[] paths = createPathsForBinaryContent(binaryDataNode, session);
		
		if (paths != null)
		{
			//Thus a full path has the form
			//<repoHomeDir>/datastore/<dir>/<dir>/<dir>/identifier
			((BinaryChannelImpl)binaryChannel).setAbsoluteBinaryChannelContentPath(paths[0]);

			((BinaryChannelImpl)binaryChannel).setRelativeFileSystemPath(paths[1]);
		}
	}

	public static int getTotalNumberOfRowsForQueryResult(QueryResult queryResult) {
		if (queryResult == null)
			return 0;
		
		return ((QueryResultImpl)queryResult).getTotalSize();
	}

	public static Query setOffsetAndLimitToQuery(Query query, int offset, int limit) {

		((QueryImpl)query).setOffset(offset);
		
		if (limit > 0){
			((QueryImpl)query).setLimit(limit);
		}
		else if (limit == 0){
			//Jackrabbit uses limit only if this is greater than 0
			//In all other cases it fetches all results.
			//In Astroboa, however, limit 0 denotes that no result should be rendered, 
			//that is only result count is needed.
			//So in order to 'limit' Jackrabbit to bring the minimum results possible
			//we set the limit to 1. This way Jackrabbit will only bring one result
			//which is acceptable since Astroboa requires no result to be fetched.
			//Total result count is not affected at all by limit
			((QueryImpl)query).setLimit(1);
		}
		
		return query;
			
	}


	public static boolean hasNodeType(String nodeTypeName, Workspace workspace) throws RepositoryException {
		return ((JackrabbitNodeTypeManager)workspace.getNodeTypeManager()).hasNodeType(nodeTypeName);
	}

	public static String encodePropertyPath(String propertyPath) {
		return ISO9075.encodePath(propertyPath);
	}

	//Check that provided id is a valid UUID according to Jackrabbit implementation
	public static boolean isValidUUID(String id) {
		try{
		return UUID.fromString(id) != null;
		}
		catch(Exception e){
			logger.error("Provided id '"+id+"' is not a valid UUID according to Jackrabbit Jcr implementation", e);
			return false;
		}
		
	}
	
	public static String serializeBinaryValue(Value value) throws Exception{
		return ValueHelper.serialize(value, false);
	}

	public static void logCacheManagerSettings(Logger externalLogger,	Repository repository) {

		if (repository != null){
			CacheManager manager = ((RepositoryImpl) repository).getCacheManager();
			
			if (manager != null){
				externalLogger.debug("Repository {} : Cache Manager : Max Memory {} MB, Max Memory Per Cache: {} MB, Min Memory Per Cache: {} KB", 
						new Object[]{AstroboaClientContextHolder.getActiveRepositoryId(), manager.getMaxMemory()/1024/1024, manager.getMaxMemoryPerCache()/1024/1024, manager.getMinMemoryPerCache()/1024});
			}
		}

		
	}

	public static boolean cacheManagerSettingsHaveChanged(
			RepositoryType repositoryConfig, Repository repository) {
		
		CacheManager manager = ((RepositoryImpl) repository).getCacheManager();
		
		int currentMaxMemory = (int) manager.getMaxMemory();
		
		int currentMaxMemoryPerCache = (int) manager.getMaxMemoryPerCache();

		int currentMinMemoryPerCache = (int) manager.getMinMemoryPerCache();

		if (repositoryConfig!=null){
			
			JcrCacheType jcrCacheConfig = repositoryConfig.getJcrCache();
				
			if (jcrCacheConfig == null){
				return currentMaxMemory != defaultMaxMemory || currentMaxMemoryPerCache != defaultMaxMemoryPerCache || currentMinMemoryPerCache != defaultMinMemoryPerCache;
			}
			else{
				return currentMaxMemory != jcrCacheConfig.getMaxMemory() || currentMaxMemoryPerCache != jcrCacheConfig.getMaxMemoryPerCache() || currentMinMemoryPerCache != jcrCacheConfig.getMinMemoryPerCache();
			}
		}
		
		return false;
	}

}
