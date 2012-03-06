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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.cache.CacheManager;
import org.apache.jackrabbit.core.query.lucene.QueryResultImpl;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.value.ValueHelper;
import org.betaconceptframework.astroboa.configuration.JcrCacheType;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.configuration.RepositoryType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
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
	
	public static void register(Session session, InputStream repositoryNodeTypeDefinitionInputStream, boolean reregisterExisting) throws FileNotFoundException, IOException, RepositoryException, ParseException{
		
		CndImporter.registerNodeTypes(new InputStreamReader(repositoryNodeTypeDefinitionInputStream), session, reregisterExisting);
	
	}
	
	public static int getTotalNumberOfRowsForQueryResult(QueryResult queryResult) {
		if (queryResult == null)
			return 0;
		
		return ((QueryResultImpl)queryResult).getTotalSize();
	}

	public static boolean hasNodeType(String nodeTypeName, Workspace workspace) throws RepositoryException {
		return workspace.getNodeTypeManager().hasNodeType(nodeTypeName);
	}

	public static String encodePropertyPath(String propertyPath) {
		return ISO9075.encodePath(propertyPath);
	}

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
