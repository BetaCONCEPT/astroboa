/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.api.model.io;

import java.util.List;

/**
 * Contains information about an import operation
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ImportReport {

	/**
	 * Retrieve number of content objects imported
	 * 
	 * @return Number of content objects successfully imported
	 */
	int getNumberOfContentObjectsImported();

	/**
	 * Retrieve number of repository users imported
	 * 
	 * @return Number of repository users successfully imported
	 */
	int getNumberOfRepositoryUsersImported();

	/**
	 * Retrieve number of taxonomies imported
	 * 
	 * @return Number of taxonomies successfully imported
	 */
	int getNumberOfTaxonomiesImported();

	/**
	 * Retrieve number of spaces imported
	 * 
	 * @return Number of spaces successfully imported
	 */
	int getNumberOfSpacesImported();

	/**
	 * Retrieve number of topics imported
	 * 
	 * @return Number of topics successfully imported
	 */
	int getNumberOfTopicsImported();

	/**
	 * Lit of messages for errors occurring during import
	 * 
	 * @return A list of error messages, if any during import
	 */
	List<String> getErrors();
	
}
