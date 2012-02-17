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


import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsQueryContext;

/**
 * 
 * Represents the result of a query.
 * 
 * <p>
 * The result contains the number of {@link CmsRepositoryEntity entities} that
 * satisfy query criteria and a list of result rows. Size of this
 * subset is defined by {@link CmsQueryContext#getLimit() limit} specified in criteria.
 * </p>
 * 
 * @param <T>
 *            Type of entity returned in results.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsOutcome<T> {

	/**
	 * Returns the number of rows matching query.
	 * 
	 * This number is not affected at all if 
	 * {@link CmsQueryContext#getLimit() limit} has been 
	 * specified. 
	 *  
	 * @return Number of rows satisfying criteria.
	 */
	long getCount();

	/**
	 * Returns a list of {@link CmsRepositoryEntity entities} satisfying
	 * criteria. Size of list in determined by {@link CmsQueryContext#getLimit() limit}
	 * specified in criteria.
	 * 
	 * @return Criteria results.
	 */
	List<T> getResults();
	
	/**
	 * Returns the limit of the result rows as specified in
	 * {@link CmsQueryContext#getLimit() limit}.
	 * 
	 * @return Result rows limit.
	 */
	int getLimit();

	/**
	 * Returns the offset of the result rows (zero-based),
	 * 
	 * as specified in
	 * {@link CmsQueryContext#getOffset() offset}.
	 *
	 * 
	 * @return Result rows offset.
	 */
	int getOffset();
	
	/**
	 * Returns the ranking for the entity
	 * 
	 * @return Ranking for the entity.
	 */
	double getRanking(T entity);

}
