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

package org.betaconceptframework.astroboa.api.model.query;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;

/**
 * @deprecated  Result row ranking will be provided by an appropriate method
 * from {@link CmsOutcome} API.
 * 
 * Represents a single row in query result with a ranking. 
 * 
 * Corresponds to a specific {@link CmsRepositoryEntity}.
 * 
 * @param <T> {@link CmsRepositoryEntity} type returned in query results.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Deprecated 
public interface CmsRankedOutcome<T extends CmsRepositoryEntity> {

	/**
	 * Returns the ranking for this row as specified in JCR API. (jcr:score())
	 * 
	 * @return Ranking for this row.
	 */
	double getRanking();

	/**
	 * Returns the {@link CmsRepositoryEntity entity} satisfying query criteria.
	 * 
	 * @return {@link CmsRepositoryEntity entity} in current row.
	 */
	T getCmsRepositoryEntity();

}
