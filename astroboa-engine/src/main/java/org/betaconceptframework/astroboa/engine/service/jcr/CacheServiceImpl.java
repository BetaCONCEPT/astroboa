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

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.service.CacheService;
import org.betaconceptframework.astroboa.engine.cache.regions.JcrQueryCacheRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
public class CacheServiceImpl implements CacheService{

	@Autowired
	private JcrQueryCacheRegion jcrQueryCacheRegion;
	
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void clearCacheForAuthenticationToken(String authenticationToken) {
		
		if (StringUtils.isNotBlank(authenticationToken)){
			try {
				jcrQueryCacheRegion.removeCacheEntriesForAuthenticationToken(authenticationToken);
			} catch (Exception e) {
				throw new CmsException(e);
			}
		}
		
	}

}
