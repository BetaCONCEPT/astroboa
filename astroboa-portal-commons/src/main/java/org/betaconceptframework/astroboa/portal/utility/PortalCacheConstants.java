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
package org.betaconceptframework.astroboa.portal.utility;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class PortalCacheConstants {

	private final static Logger logger = LoggerFactory.getLogger(PortalCacheConstants.class);
	
	public  final static CacheRegion TOPIC_DEFAULT_CACHE_REGION = retrieveCacheRegion(PortalStringConstants.TOPIC_CACHE_REGION, CacheRegion.ONE_HOUR);
	
	public final static CacheRegion CONTENT_OBJECT_DEFAULT_CACHE_REGION = retrieveCacheRegion(PortalStringConstants.CONTENT_OBJECT_CACHE_REGION, CacheRegion.TEN_MINUTES);
	
	public final static CacheRegion CONTENT_OBJECT_LIST_DEFAULT_CACHE_REGION = retrieveCacheRegion(PortalStringConstants.CONTENT_OBJECT_LIST_CACHE_REGION, CacheRegion.TEN_MINUTES);
	
	public final static CacheRegion CONTENT_AREA_DEFAULT_CACHE_REGION = retrieveCacheRegion(PortalStringConstants.CONTENT_AREA_CACHE_REGION, CacheRegion.ONE_HOUR);
	
	
	private static CacheRegion retrieveCacheRegion(String cacheRegionKey, CacheRegion defaultCacheRegion) {
		try {
			PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
			
			String cacheRegionValue = portalConfiguration.getString(cacheRegionKey);
			
			CacheRegion cacheRegion;
			
			if (StringUtils.isNotBlank(cacheRegionValue)) {
				cacheRegion = CacheRegion.valueOf(cacheRegionValue);
			}
			else {
				cacheRegion = defaultCacheRegion;
			}

			logger.debug("Found cache region for key {} : {}", cacheRegionKey, cacheRegion);
			
			return cacheRegion;
		}
		catch (Exception e) {
			logger.error("A problem occured while reading portal configuration file. Returning default cache region "+ defaultCacheRegion, e);
			return defaultCacheRegion;
		}
	}
}
