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
 * Contains information about the progress of a serialization.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SerializationReport {

	/**
	 * Name of the compressed file which contains the XML outcome of the serialization.
	 * 
	 * @return File name of the generated ZIP file
	 */
	String getFilename();
	
	/**
	 * Absolute path of the compressed file which contains the XML outcome of the serialization.
	 * 
	 * @return File path of the generated ZIP file, relative to repository's home directory
	 */
	String getAbsolutePath();
	
	/**
	 * Check if serialization has finished.
	 * 
	 * @return <code>true</code> if the file has been created, <code>false</code> otherwise.
	 */
	boolean hasSerializationFinished();
	
	/**
	 * Lit of messages for errors occurring during serialization
	 * 
	 * @return A list of error messages, if any during serialization
	 */
	List<String> getErrors();
	
	/**
	 * Retrieve number of objects serialized
	 * 
	 * @return Number of objects successfully serialized
	 */
	int getNumberOfObjectsSerialized();

	/**
	 * Retrieve number of repository users serialized
	 * 
	 * @return Number of repository users successfully serialized
	 */
	int getNumberOfRepositoryUsersSerialized();

	/**
	 * Retrieve number of taxonomies serialized
	 * 
	 * @return Number of taxonomies successfully serialized
	 */
	int getNumberOfTaxonomiesSerialized();

	/**
	 * Retrieve number of spaces serialized
	 * 
	 * @return Number of spaces successfully serialized
	 */
	int getNumberOfSpacesSerialized();

}
