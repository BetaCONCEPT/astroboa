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
package org.betaconceptframework.astroboa.api.model.query;

import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;

/**
 * Predefined cache regions used with {@link CmsCriteria}.
 * 
 * This enumeration represents a predefined region in query cache
 * where query results are cached if user chooses to cache them.
 * 
 * A region in cache is controlled  by two time-related properties :
 * <ul>
	 *  <li><b>timeToLiveSeconds</b> Time to idle (in seconds) before the query results are swept away. 0 denotes no limit.
	 *  <li><b>maxAgeSeconds</b> Time query results should exist in cache (in seconds) regardless of idle time before they
			are swept away. 0 denotes no limit.
 * </ul>
 * 
 * <p>
 * This enumeration provides the ability to the user to control only <code>maxAgeSeconds</code>, 
 * that is user is able to choose how much time results of a specific query may live in cache.
 * </p>
 * 
 * <p>
 * <code>timeToLiveSeconds</code> is fixed and depends on the type of <code>maxAgeSeconds</code>. 
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CacheRegion {
	
	/**
	 *  Query results stored under this region may remain idle and live in cache
	 *  at most 1 minute.
	 */
	ONE_MINUTE("oneMinute"),

	/**
	 *  Query results stored under this region may remain idle and live in cache
	 *  at most 5 minutes.
	 */
	FIVE_MINUTES("fiveMinutes"),
	
	/**
	 *  Query results stored under this region may remain idle and live in cache
	 *  at most 10 minutes.
	 */
	TEN_MINUTES("tenMinutes"),
	
	/**
	 *  Query results stored under this region may remain idle and live in cache
	 *  at most 30 minutes.
	 */
	THIRTY_MINUTES("thirtyMinutes"),
	
	/**
	 *  Query results stored under this region may remain idle at most 30 minutes 
	 *  and live in cache at most 1 hour.
	 */
	ONE_HOUR("oneHour"),
	
	/**
	 *  Query results stored under this region may remain idle at most 30 minutes 
	 *  and live in cache at most 6 hours.
	 */
	SIX_HOURS("sixHours"),
	
	/**
	 *  Query results stored under this region may remain idle at most 30 minutes 
	 *  and live in cache at most 12 hours.
	 */
	TWELVE_HOURS("twelveHours"),
	
	/**
	 *  Query results stored under this region may remain idle at most 30 minutes 
	 *  and live in cache at most 1 day.
	 */
	ONE_DAY("oneDay"),
	
	/**
	 * Query results will not be cached at all
	 */
	NONE("none");
	
	private String regionName;
	
	
	CacheRegion(String regionName){
		this.regionName = regionName;
	}


	public String getRegionName() {
		return regionName;
	}
	
	
}
