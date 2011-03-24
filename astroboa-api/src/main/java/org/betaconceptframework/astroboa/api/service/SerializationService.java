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

package org.betaconceptframework.astroboa.api.service;


import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;

/**
 * Service providing methods for serializing the content of a Astroboa repository 
 * in XML according to repository's XSD schemas. 
 * 
 * <p>
 * This interface provides methods for bulk serializing of resources of the same 
 * type (all repository users, all taxonomies, all content objects) or for 
 * all the content of a repository.
 * </p>
 * 
 * <p>
 * Serialization of individual resources ({@link Topic}, {@link ContentObject}) is accomplished
 * through the use of methods provided in the resource API like,
 * {@link Topic#xml(boolean))}, {@link ContentObject#xml(boolean))} {@link ContentObject#json(boolean))}, etc. 
 * </p>
 * 
 * <p>
 * Serialization of a collection of resources as a results of a query is accomplished by
 * through the use of methods provided in the Services API like, 
 * {@link ContentService#searchContentObjects(org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria, org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType)},
 * {@link TopicService#searchTopics(org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria, org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType)}, etc. 
 * </p>
 * 
 * <p>
 * Serialization's outcome is a compressed XML file located under <code>serializations</code>
 * directory inside repository's home directory. {@link SerializationReport} specifies
 * the path to the compressed file.
 * </p>
 * 
 * <p>
 * The serialization procedure takes place in a separate Thread in order not to
 * block the current Thread. Serialization progress is depicted in {@link SerializationReport}
 * but it can be followed only by local invocations of any of these methods. 
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SerializationService {

	/**
	 * Serialize all objects of a repository into an XML file.
	 * 
	 * <p>
	 * It serializes content objects in a single XML file which is compressed and stored
	 * under the <code>serializations</code> directory inside repository's home directory. 
	 * </p>
	 * 
	 * @param serializeBinaryContent <code>true</code> to export binary content, i.e. serialize binary properties, 
	 * <code>false</code> otherwise. In both cases, a URL which serves the binary content is always provided.
	 * 
	 * @return A report about serialization progress.
	 * 
	 */
	SerializationReport serializeObjects(boolean serializeBinaryContent);
	
	/**
	 * Serialize the content of a repository into an XML file.
	 * 
	 * <p>
	 * It serializes the content of a repository in a single XML file which is compressed and stored
	 * under the <code>serializations</code> directory inside repository's home directory. 
	 * </p>
	 * 
	 * @param serializeBinaryContent <code>true</code> to export binary content, i.e. serialize binary properties, 
	 * <code>false</code> otherwise.In both cases, a URL which serves the binary content is always provided.
	 * 
	 * @return A report about serialization progress. 
	 */
	SerializationReport serializeRepository(boolean serializeBinaryContent);

	/**
	 * Serialize all {@link RepositoryUser}s of a repository into an XML file.
	 * 
	 * <p>
	 * It serializes repository users in a single XML file which is compressed and stored
	 * under the <code>serializations</code> directory inside repository's home directory. 
	 * </p>
	 * 
	 * @return A report about serialization progress. 
	 */
	SerializationReport serializeRepositoryUsers();

	/**
	 * Serialize all {@link Taxonomy taxonomies} of a repository into an XML file.
	 * 
	 * <p>
	 * It serializes taxonomies in a single XML file which is compressed and stored
	 * under the <code>serializations</code> directory inside repository's home directory. 
	 * </p>
	 * 
	 * @return A report about serialization progress. 
	 */
	SerializationReport serializeTaxonomies();

	/**
	 * Serialize the {@link Space organization space} of a repository into an XML file.
	 * 
	 * <p>
	 * It serializes the organization space in a single XML file which is compressed and stored
	 * under the <code>serializations</code> directory inside repository's home directory. 
	 * </p>
	 * 
	 * 
	 * @return A report about export progress. 
	 */
	SerializationReport serializeOrganizationSpace();
}
