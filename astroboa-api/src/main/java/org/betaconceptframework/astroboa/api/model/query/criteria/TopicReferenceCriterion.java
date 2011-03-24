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
package org.betaconceptframework.astroboa.api.model.query.criteria;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;



/**
 * Represents a single criterion for a property whose value 
 * is one or more references to topics.
 *
 * <p>
 * This criterion is used when users want to check the existence or not of
 * a relationship between content object and topics via specific property.
 * Thus, only {@link QueryOperator#EQUALS},
 * {@link QueryOperator#NOT_EQUALS}, {@link QueryOperator#IS_NULL} and
 * {@link QueryOperator#IS_NOT_NULL} are valid operators. 
 * </p>
 * 
 * <p>
 * It extends the functionality of {@link SimpleCriterion} by allowing users to provide {@link Topic} instances as 
 * values. Implementation of this interface should be able to use either the identifier or the 
 * names of the provided {@link Topic}'s, in order to generate the appropriate 
 * JCR criterion.
 * </p>
 * 
 * <p>
 * It also provides methods which instruct query builder to automatically
 * create one additional criterion for each one of the provided topic or topics
 * children. This is a very useful feature when users want to find all content objects
 * which relate to a specific topic or any of its child topics via a specific property.
 * </p>
 * 
 * <p>
 * Finally, implementation of this interface should also support the creation of the
 * criterion even when no property path is provided. In this case, it should create one criterion
 * for each one of the properties of type {@link ValueType#Topic}.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface TopicReferenceCriterion extends SimpleCriterion{
	
	/**
	 * Set the value of the criterion to be the provided {@link Topic};
	 * 
	 * <p>
	 * In cases where the provided value has neither an identifier nor a name, 
	 * then this value cannot be used and a warning should be issued.
	 * </p>
	 * w
	 * @param topicReference
	 */
	void addTopicAsAValue(Topic topicReference);
	
	/**
	 * Set the value of the criterion to be the provided list of {@link Topic};
	 * 
	 * <p>
	 * In cases where any of the provided values has neither an identifier nor a name, 
	 * then this value cannot be used and a warning should be issued.
	 * </p>
	 * 
	 * @param topicReferences
	 */
	void addTopicsAsValues(List<Topic> topicReferences);
	
	/**
	 * Instruct Query builder to create a criterion for each one of the children of
	 * the provided topic or topics. 
	 */
	void expandCriterionToIncludeSubTopics();
	
}
