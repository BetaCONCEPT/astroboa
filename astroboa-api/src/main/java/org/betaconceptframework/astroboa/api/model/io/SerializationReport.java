/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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
	 * Lit of messages for errors occurring during serialization
	 * 
	 * @return A list of error messages, if any during serialization
	 */
	List<String> getErrors();
	
	/**
	 * Retrieve the number of objects being serialized.
	 * 
	 * This value depicts the current status of object serialization 
	 * and thus changes as more objects are being serialized.
	 * 
	 * @return The number of objects successfully serialized until the moment of the request
	 */
	int getNumberOfCompletedSerializedObjects();

	/**
	 * Retrieve the total number of objects which must be serialized.
	 * 
	 * @return The total number of objects to be serialized
	 */
	int getTotalNumberOfObjects();
	
	/**
	 * Retrieve the number of repository users being serialized.
	 * 
	 * This value depicts the current status of repository user serialization 
	 * and thus changes as more repository users are being serialized.
	 * 
	 * @return The number of repository users successfully serialized until the moment of the request
	 */
	int getNumberOfCompletedSerializedRepositoryUsers();

	/**
	 * Retrieve the total number of repository users which must be serialized
	 * 
	 * @return Total number of repository users to be serialized
	 */
	int getTotalNumberOfRepositoryUsers();

	/**
	 * Retrieve the number of taxonomies being serialized.
	 * 
	 * This value depicts the current status of taxonomy serialization 
	 * and thus changes as more taxonomies are being serialized.
	 * 
	 * @return The number of taxonomies successfully serialized until the moment of the request
	 */
	int getNumberOfCompletedSerializedTaxonomies();

	/**
	 * Retrieve the total number of taxonomies which must be serialized
	 * 
	 * @return Total number of taxonomies to be serialized
	 */
	int getTotalNumberOfTaxonomies();

	/**
	 * Retrieve the number of topics being serialized.
	 * 
	 * This value depicts the current status of topic serialization 
	 * and thus changes as more topics are being serialized.
	 * 
	 * @return The number of topics successfully serialized until the moment of the request
	 */
	int getNumberOfCompletedSerializedTopics();

	/**
	 * Retrieve the total number of topics which must be serialized
	 * 
	 * @return Total number of topics to be serialized
	 */
	int getTotalNumberOfTopics();
	
	/**
	 * Retrieve the number of spaces being serialized.
	 * 
	 * This value depicts the current status of space serialization 
	 * and thus changes as more spaces are being serialized.
	 * 
	 * @return The number of spaces successfully serialized until the moment of the request
	 */
	int getNumberOfCompletedSerializedSpaces();

	/**
	 * Retrieve the total number of spaces which must be serialized
	 * 
	 * @return Total number of spaces to be serialized
	 */
	int getTotalNumberOfSpaces();


}
