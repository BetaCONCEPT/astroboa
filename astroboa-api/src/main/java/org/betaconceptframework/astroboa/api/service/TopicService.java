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



import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsQueryContext;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;

/**
 * Service providing methods for managing {@link Topic topics}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface TopicService {

	/**
	 * Save or update a {@link Topic topic}.
	 * 
	 * <p>
	 * If no {@link Topic#getTaxonomy() taxonomy} is provided
	 * {@link Taxonomy#SUBJECT_TAXONOMY_NAME subject taxonomy} will be
	 * used.
	 * </p>
	 * 
	 * <p>
	 * Whether save or update process is followed depends on whether <code>topic</code>
	 *  is a new topic or not. <code>topic</code> is considered new if there is no 
	 * {@link Topic#getId() id} or <code>id</code> is provided but there is
	 * no {@link Topic topic} in repository for the provided <code>id</code>. In this case
	 * <code>topic</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>topic</code> is considered new)
	 * 
	 * <ul>
	 * <li> Create a new {@link CmsRepositoryEntity#getId() id} or use the provided <code>id</code>.
	 * <li> Relate <code>topic</code> with the provided {@link Topic#getOwner() owner}.
	 * 		Topic's owner MUST already exist. If not, an exception is thrown.
	 * <li> Locate <code>topic</code>'s parent topic using its identifier. If no parent is found an exception is thrown.
	 *      If no parent identifier is provided then topic will be placed under its taxonomy.
	 * <li> Save localized labels for <code>topic</code>.
	 * <li> Save {@link Topic#getOrder() order}.
	 * <li> Save {@link Topic#getName() name}.
	 * <li> Save {@link Topic#isAllowsReferrerContentObjects() allowsContentObjectReferences}.
	 * <li> Save or update all of its {@link Topic#getChildren() child topics}.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>topic</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>topic</code> with the provided {@link Topic#getOwner() owner} only 
	 * 		if the provided owner is different from already existed owner.
	 * 		Topic's owner MUST already exist. If not, an exception is thrown.
	 * <li> Update <code>topic</code>'s parent ONLY if provided parent identifier is different than one existed.
	 * <li> Update localized labels for <code>topic</code>.
	 * <li> Update {@link Topic#getOrder() order}.
	 * <li> Update {@link Topic#getName() name}.
	 * <li> Update {@link Topic#isAllowsReferrerContentObjects() allowsContentObjectReferences}.
	 * <li> Update {@link Topic#getParent() parent}, in case it has been changed. This corresponds
	 * 		to moving <code>topic</code>.In this case new <code>topic</code>'s parent
	 * 		must exist and must belong to the same {@link Taxonomy taxonomy} with
	 * 		<code>topic</code>.
	 * </ul>
	 * </p>
	 * 
	 * @param topic
	 *            Topic to be saved or updated.
	 *            
	 * @deprecated Use method {@link #save(Object)} instead
	 *             
	 * @return Newly created or updated Topic
	 */
	@Deprecated
	Topic saveTopic(Topic topic);

	/**
	 * Delete all {@link Topic topics} recursively starting from specified <code>topicIdOrName</code>.
	 * 
	 * Deletes also all {@link ContentObject content object} references
	 * to deleted topics.
	 * 
	 * @param topicIdOrName
	 *            {@link Topic#getId() Topic's id}.
	 * @<code>true</code> if topic has been successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteTopicTree(String topicIdOrName);

	/**
	 * Returns a topic for the specified <code>topicId</code>.
	 * 
	 * @param topicId
	 *            {@link CmsRepositoryEntity#getId() Topic's id}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned topic.
	 * 
	 * @deprecated use {@link #getTopic(String, ResourceRepresentationType#TOPIC_INSTANCE, FetchLevel)} instead. Locale does not play any role
	 * since all localized labels are provided and method 
	 * {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()} will be deprecated in future releases 
	 * 
	 * @return A topic corresponding to <code>topicId</code> 
	 */
	@Deprecated
	Topic getTopic(String topicId, String locale);

	/**
	 * Search all topics satisfying specified criteria.
	 * 
	 * @param topicCriteria
	 *           Topic search criteria.
	 * @deprecate Use {@link #searchTopics(TopicCriteria, ResourceRepresentationType)}
	 * 
	 * @return Topics satisfying specified criteria.
	 */
	@Deprecated
	CmsOutcome<Topic> searchTopics(TopicCriteria topicCriteria);

	/**
	 * Returns mostly used topics of a specific taxonomy.
	 * 
	 * Any taxonomy name is accepted, including reserved taxonomies
	 * ({@link Taxonomy#SUBJECT_TAXONOMY_NAME}, {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME}).
	 * 
	 * @param taxonomyName
	 *            {@link Taxonomy#getName() Taxonomy name}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned topics.
	 * @param offset
	 *            Index of first result row
	 *            {@link CmsQueryContext#setOffset}
	 * @param limit
	 *            Index of last result row
	 *            {@link CmsQueryContext#setLimit}
	 * @return Mostly used topics of a specific taxonomy.
	 */
	CmsOutcome<Topic> getMostlyUsedTopics(String taxonomyName, String locale,
			int offset, int limit);

	/**
	 * Returns a list of {@link ContentObject contentObject} identifiers which
	 * contain one or more {@link TopicReferenceProperty property} whose value(s) is a 
	 * {#link Topic topic} with the provided <code>topicId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on 
	 * {@link Topic#getReferrerContentObjects()} method.
	 *   
	 * @param topicId Topic identifier.
	 * 
	 * @return A list of content object identifiers.
	 */
	List<String> getContentObjectIdsWhichReferToTopic(String topicId);
	
	/**
	 * Returns count of {@link ContentObject contentObject} identifiers which
	 * contain one or more {@link TopicReferenceProperty property} whose value(s) is a 
	 * {#link Topic topic} with the provided <code>topicId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on {@link Topic#getNumberOfReferrerContentObjects()} method.
	 *   
	 * @param topicId Topic identifier.
	 * 
	 * @return Number of content objects which refer to topic.
	 */
	int getCountOfContentObjectIdsWhichReferToTopic(String topicId);

	/**
	 * Single point of retrieving a {@link Topic} from a repository.
	 * 
	 * <p>
	 * A topic can be retrieved as XML, as JSON or as a {@link Topic} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String topicXML = topicService.getTopic("id", ResourceRepresentationType.XML, FetchLevel.ENTITY);
	 *  		 
	 *  String topicJSON = topicService.getTopic("id", ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
	 *  		 
	 *  Topic topic = topicService.getTopic("id", ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.FULL);
	 *  		
	 *  CmsOutcome<Topic> topicOutcome = topicService.getTopic("id", ResourceRepresentationType.TOPIC_LIST, FetchLevel.FULL);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#TOPIC_LIST} represents a list of
	 * topics, rather than one and therefore its use in this context is not recommended.
	 * Nevertheless, if used, a list containing one topic will be provided.
	 * </p>
	 * 
	 * <p>
	 * Users have also the option to specify whether to fetch only topic's properties, 
	 * or to load its children as well as the whole topic tree. This is a way to control
	 * lazy loading by pre-fetching topic children. Bear in mind that lazy loading mechanism
	 * is meaningful only when {@link Topic} or {@link CmsOutcome} instance is returned. Other
	 * representations (XML and JSON) do not support lazy loading.
	 * 
	 * </p>
	 * 
	 * <p>
	 * Also, in cases where no output type is defined, a {@link Topic} instance
	 * is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTopic","localization":{"label":{"en":"My first topic"}}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"topic":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTopic","localization":{"label":{"en":"My first topic"}}}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, in case no topic is found for provided <code>topicId</code>, 
	 * <code>null</code>is returned.
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link Topic} or {@link CmsOutcome}
	 * @param topicIdOrName {@link Topic#getId() topic id} or {@link Topic#getName() topic name}
	 * @param output Topic representation output, one of XML, JSON or {@link Topic}. Default is {@link ResourceRepresentationType#TOPIC_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Topic}'s only properties, its children as well or the whole {@link Topic} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if
	 * 
	 * @return A topic as XML, JSON or {@link Topic}, or <code>null</code> of none is found.
	 */
	<T> T getTopic(String topicIdOrName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint);

	/**
	 * Save or update a {@link Topic topic}.
	 * 
	 * <p>
	 * This method expects either a {@link Topic} instance
	 * or a {@link String} instance which corresponds to an XML or
	 * JSON representation of the entity to be saved.
	 * </p>
	 * 
	 * <p>
	 * If no {@link Topic#getTaxonomy() taxonomy} is provided
	 * {@link Taxonomy#SUBJECT_TAXONOMY_NAME subject taxonomy} will be
	 * used.
	 * </p>
	 * 
	 * <p>
	 * Whether save or update process is followed depends on whether <code>topic</code>
	 *  is a new topic or not. <code>topic</code> is considered new if there is no 
	 * {@link Topic#getId() id} or <code>id</code> is provided but there is
	 * no {@link Topic topic} in repository for the provided <code>id</code>. In this case
	 * <code>topic</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>topic</code> is considered new)
	 * 
	 * <ul>
	 * <li> Create a new {@link CmsRepositoryEntity#getId() id} or use the provided <code>id</code>.
	 * <li> Relate <code>topic</code> with the provided {@link Topic#getOwner() owner}.
	 * 		Topic's owner MUST already exist. If not, an exception is thrown.
	 * <li> Locate <code>topic</code>'s parent topic using its identifier. If no parent is found an exception is thrown.
	 *      If no parent identifier is provided then topic will be placed under its taxonomy.
	 * <li> Save localized labels for <code>topic</code>.
	 * <li> Save {@link Topic#getOrder() order}.
	 * <li> Save {@link Topic#getName() name}.
	 * <li> Save {@link Topic#isAllowsReferrerContentObjects() allowsContentObjectReferences}.
	 * <li> Save or update all of its {@link Topic#getChildren() child topics}.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>topic</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>topic</code> with the provided {@link Topic#getOwner() owner} only 
	 * 		if the provided owner is different from already existed owner.
	 * 		Topic's owner MUST already exist. If not, an exception is thrown.
	 * <li> Update <code>topic</code>'s parent ONLY if provided parent identifier is different than one existed.
	 * <li> Update localized labels for <code>topic</code>.
	 * <li> Update {@link Topic#getOrder() order}.
	 * <li> Update {@link Topic#getName() name}.
	 * <li> Update {@link Topic#isAllowsReferrerContentObjects() allowsContentObjectReferences}.
	 * <li> Update {@link Topic#getParent() parent}, in case it has been changed. This corresponds
	 * 		to moving <code>topic</code>.In this case new <code>topic</code>'s parent
	 * 		must exist and must belong to the same {@link Taxonomy taxonomy} with
	 * 		<code>topic</code>.
	 * </ul>
	 * </p>
	 * 
	 * 
	 * @param topic
	 *            Topic to be saved or updated.
	 * @return Newly created or updated Topic
	 */
	Topic save(Object topic);
	
	/**
	 * Query topics using {@link TopicCriteria} and specifying 
	 * result output representation.
	 *  
	 * <p>
	 * Query results can be retrieved as XML, as JSON or as a {@link CmsOutcome<Topic>} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String resultAsXML = topicService.searchTopics(topicCriteria, ResourceRepresentationType.XML);
	 *  		 
	 *  String resultAsJSON = topicService.searchTopics(topicCriteria, ResourceRepresentationType.JSON);
	 *  		 
	 *  Topic topic = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_INSTANCE);
	 *  		
	 *  CmsOutcome<Topic> resultAsOutcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#TOPIC_INSTANCE} represents one content object
	 * only, rather than a list and therefore its use in this context is not recommended. However in the following
	 * cases a single content object or null is returned, instead of throwing an exception.
	 * 
	 * <ul>
	 * <li>User specified limit to be 1 ({@link TopicCriteria#setLimit(1)}).
	 * 	In this case the first content object matching criteria is returned, or null if none matched criteria.<li>
	 * <li>User specified no limit or limit greater than 1. In this case if more than one topics match
	 * criteria an exception is thrown</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Also, in cases where no output type is defined a {@link CmsOutcome<Topic>} instance is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTopic","localization":{"label":{"en":"My first topic"}}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"topic":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTopic","localization":{"label":{"en":"My first topic"}}}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, if no result is found,
	 * 	 
	 * <ul>
	 * <li><code>null</code> is returned if <code>output</code> {@link ResourceRepresentationType#TOPIC_INSTANCE}</li>
	 * <li><pre>{
     *				"totalResourceCount" : "0",
  	 *				"offset" : "0"
	 *			}
	 *	</pre> is returned if <code>output</code> {@link ResourceRepresentationType#JSON}</li>
	 * <li><pre><?xml version="1.0" encoding="UTF-8"?>
	 * 				<bccmsapi:resourceResponse 
	 * 					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	 * 					xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model" 
	 * 					xmlns:bccmsapi="http://www.betaconceptframework.org/schema/astroboa/api" 
	 * 				 	offset="0"
	 * 					totalResourceCount="0"
	 * 				/>
	 * </pre> is returned if <code>output</code> {@link ResourceRepresentationType#XML}</li>
	 * <li>empty {@link CmsOutcome<Topic>} is returned if <code>output</code> {@link ResourceRepresentationType#TOPIC_LIST}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link Topic} or {@link CmsOutcome}
	 * @param topicCriteria
	 *           Topic search criteria.
	 * @param output Topic representation output, one of XML, JSON or {@link Topic}. 
	 * 	Default is {@link ResourceRepresentationType#TOPIC_LIST}
	 * 
	 * @return Topics as XML, JSON or {@link CmsOutcome<Topic>}
	 */
	<T> T  searchTopics(TopicCriteria topicCriteria, ResourceRepresentationType<T> output);


	/**
	 * Returns all topics matching specified criteria in XML.
	 * 
	 * @param topicCriteria
	 *            Restrictions for topic and render instructions for
	 *            query results.
	 * @deprecated Use {@link #searchTopics(TopicCriteria, ResourceRepresentationType)} instead
	 * @return XML representation of query results following XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd
	 */
	@Deprecated
	String searchTopicsAndExportToXml(TopicCriteria topicCriteria);
	

	/**
	 * Returns all topics matching specified criteria in JSON
	 * following Mapped convention.
	 * 
     * The natural JSON notation, leveraging closely-coupled JAXB RI integration.
     * <p>Example JSON expression:<pre>
     * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
     * </pre>
     * </p>
     * 
	 * @param topicCriteria
	 *            Restrictions for topic and render instructions for
	 *            query results.
	 * @deprecated Use {@link #searchTopics(TopicCriteria, ResourceRepresentationType)} instead
	 * @return JSON representation of query results according to XML element <code>resourceRepresentation</code>
	 *   defined in astroboa-api-{version}.xsd  following Mapped convention
	 */
	@Deprecated
	String searchTopicsAndExportToJson(TopicCriteria topicCriteria);

}
