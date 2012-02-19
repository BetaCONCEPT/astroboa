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

package org.betaconceptframework.astroboa.api.model.query.criteria;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Base interface representing criteria for a specific
 * {@link CmsRepositoryEntity entity}.
 * 
 * <p>
 * A CmsCriteria instance corresponds to a specific {@link CmsRepositoryEntity entity},
 * contains one or more {@link Criterion criteria} and may hold instructions about
 * {@link CmsQueryContext#addOrderProperty(String, Order) sorting} results
 * and/or about the number of {@link CmsQueryContext#setOffsetAndLimit(int, int) fetched} results
 * and whether to cache query results or not. Default behavior is that query results are NOT cached.
 * </p>
 * 
 * <p>
 * This interface contains methods for managing all above common attributes of a criteria instance.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsCriteria extends CmsQueryContext {

	public final static CacheRegion DEFAULT_CACHE_REGION = CacheRegion.TEN_MINUTES;
	
	/**
	 * System Built in Entity functionality has been removed.
	 *
	 */
	@Deprecated
	public enum SearchMode{
		/**
		 * Search all entities
		 */
		SEARCH_ALL_ENTITIES,
		/**
		 * Search only system built in entities
		 */
		SEARCH_ONLY_SYSTEM_BUILTIN_ENTITIES, 
		/**
		 * Search only non system built in properties
		 */
		SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES,
	}
	/**
	 * Returns list of criteria which will participate in the query.
	 * 
	 * {@link Condition#AND And} condition will be used to 
	 * connect all criteria in the list, when creating query string.
	 * 
	 * @return A list of criteria.
	 */
	List<Criterion> getCriteria();

	/**
	 * Adds a criterion.
	 * 
	 * @param criterion
	 *            Criterion to add.
	 * @return The criteria instance.
	 */
	CmsCriteria addCriterion(Criterion criterion);

	/**
	 * Creates a criterion with {@link QueryOperator#EQUALS equals} operator for
	 * entity id.
	 * 
	 * @param id
	 *            Entity id value for criterion.
	 */
	void addIdEqualsCriterion(String id);
	
	/**
	 * Creates a criterion with {@link QueryOperator#NOT_EQUALS not equals} operator for
	 * entity id.
	 * 
	 * @param id
	 *            Entity id value for criterion.
	 */
	void addIdNotEqualsCriterion(String id);

	/**
	 * Returns the query string in XPath language according to JCR API. 
	 * 
	 * @return JCR API's XPath query string representing criteria.
	 * 
	 */
	String getXPathQuery();
	
	/**
	 * Provides the ability to specify directly the xpath query if this is known apriori.
	 * 
	 * This way one could easily use other features of criteria that are not depicted in xpath such as cache
	 * settings, offset, limit, etc
	 * @param xpathQuery
	 */
	void setXPathQuery(String xpathQuery);
	
	/**
	 * Clears all criteria and any xpath query.
	 * 
	 * Does not reset properties related to {@link CmsQueryContext}.
	 */
	void reset();

	/**
	 * This methods manages the policy under which the results from this criteria
	 * will remain in repository cache.
	 * 
	 * <p>
	 * Query results that are cached, do not remain forever in cache. Instead they may be cached
	 * under one of predefined {@link CacheRegion cache regions} which control the amount of time
	 * query results may stay idle as well as the total time they can live inside cache.
	 * </p>
	 * 
	 * @param cacheRegion Predefined region in cache where query results are to be stored. If <code>null</code> value is provided, 
	 * {@link CacheRegion#TEN_MINUTES} value will be applied.
  	 */
	void setCacheable(CacheRegion cacheRegion);
	
	/**
	 * Check if cache is checked and results are kept in cache.
	 * 
	 * @return <code>true</code> if query results are cacheable, <code>false</code>
	 * otherwise
	 */
	boolean isCacheable();
	
	/**
	 * Returns region in cache where results from this criteria will be held if cache is enabled.
	 * 
	 * @return Returns region in cache where results from this criteria will be held if cache is enabled.
	 */
	CacheRegion getCacheRegion();
	
	/**
	 * Disable caching of results from this Criteria.
	 * 
	 * <p>
	 * Equivalent with call <code>setCacheable(CacheRegion.NONE)</code>
	 * </p>
	 */
	void doNotCacheResults();

	/**
	 * Specify which sub set of entities will participate in the query.
	 * 
	 * Default value is {@link SearchMode#SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES}.
	 * 
	 * @deprecated This functionality has been marked as deprecated since system builtin 
	 * entity feature has been removed. You may continue to use this method, in order to
	 * be able to retrieve entities which have been marked as system until the next major release
	 * where this method will be removed.
	 * 
	 * @param searchMode Search all entities, or system built in entities or all others.
	 */
	void setSearchMode(SearchMode searchMode);
	
	/**
	 * Get search mode
	 * 
	 * Default value is {@link SearchMode#SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES}.
	 * 
	 * @deprecated This functionality has been marked as deprecated since system builtin 
	 * entity feature has been removed.
	 *   
	 * @return Search mode
	 */
	SearchMode getSearchMode();
}
