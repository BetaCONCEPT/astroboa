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

package org.betaconceptframework.astroboa.model.factory;


import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.query.criteria.BinaryChannelCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.model.impl.query.criteria.BinaryChannelCriteriaImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.ContentObjectCriteriaImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.RepositoryUserCriteriaImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.SpaceCriteriaImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.TaxonomyCriteriaImpl;
import org.betaconceptframework.astroboa.model.impl.query.criteria.TopicCriteriaImpl;

/**
 * Provides methods for creating {@link CmsCriteria criteria} for 
 * querying entities of content repository.
 * 
 * <p>
 * This is the entry point for creating queries for 
 * {@link ContentObject content objects}, {@link Topic topics}
 * and other entities of Astroboa repository. 
 * </p>
 * 
 * <p>
 * The scope of this factory is to provide the developer with 
 * a criteria instance, properly initialized with all underlying details specific 
 * to JCR API concerning queries.
 * </p>
 * 
 *  <pre>
 *    TopicService topicService;
 *    TopicCriteria topicCriteria;
 *       
 *     TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
 *     
 *     //Fill Topic criteria instance with query criteria.
 *     ...
 *     //Use service to search for topics that matched criteria
 *     topicsMatched = topicService.searchTopics(topicCriteria);
 *  </pre>
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsCriteriaFactory{

	CmsCriteriaFactory() {
		//Private constructor so that it cannot be instantiated
	}
	/**
	 * Creates criteria for a specific type of a {@link ContentObject}. 
	 * 
	 * @return Criteria for a ContentObject with a specific type.
	 */
	public static ContentObjectCriteria newContentObjectCriteria(String contentObjectType) {
		
		ContentObjectCriteria contentObjectCriteria = newContentObjectCriteria();
		contentObjectCriteria.addContentObjectTypeEqualsCriterion(contentObjectType);
		
		return contentObjectCriteria;
	}

	/**
	 * Creates criteria for a {@link ContentObject}.
	 * 
	 * @return Criteria for a ContentObject.
	 */
	public static ContentObjectCriteria newContentObjectCriteria() {
		return new ContentObjectCriteriaImpl();
	}

	/**
	 * Creates criteria for a {@link RepositoryUser}.
	 * 
	 * @return Criteria for a RepositoryUser.
	 */
	public static RepositoryUserCriteria newRepositoryUserCriteria() {
		
		return new RepositoryUserCriteriaImpl();
	}

	/**
	 * Creates criteria instance for a {@link Space}.
	 * 
	 * @return Criteria for a Space.
	 */
	public static SpaceCriteria newSpaceCriteria() {
		
		return new SpaceCriteriaImpl();
	}

	/**
	 * Creates criteria for a {@link Taxonomy}.
	 * 
	 * @return Criteria for a Taxonomy.
	 */
	public static TaxonomyCriteria newTaxonomyCriteria() {
		return new TaxonomyCriteriaImpl();
	}

	/**
	 * Creates criteria instance for a {@link Topic}.
	 * 
	 * @return Criteria for a Topic.
	 */
	public static TopicCriteria newTopicCriteria() {
		return new TopicCriteriaImpl();
	}

	/**
	 * Creates criteria for a {@link BinaryChannel}.
	 * 
	 * @return Criteria for a BinaryChannel.
	 */
	public static BinaryChannelCriteria newBinaryChannelCriteria() {
		return new BinaryChannelCriteriaImpl();
	}
}
