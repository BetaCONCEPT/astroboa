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

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;

/**
 * Service providing methods for managing {@link Taxonomy taxonomies}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface TaxonomyService {

	/**
	 * Save or update a {@link Taxonomy taxonomy} in content repository model.
	 * 
	 * <p>
	 * This method saves or updates taxonomy's name and localized labels and NOT its root topics.
	 * In order to save its root topics, a call to {@link TopicService#saveTopic(Topic)}
	 * for each root topic must be made.
	 * </p>
	 * 
	 * <p>
	 * In case of insert or update full Taxonomy tree, one could export taxonomy to an xml ({@link Taxonomy#toXml()})
	 * and then call {@link #importTaxonomyFromXml(String)}.
	 * </p>
	 * 
	 * <p>
	 * Taxonomy {@link Taxonomy#getName() name} must be unique across the repository and a valid XML name
	 * according to XML Namespaces recommendation [4] (http://www.w3.org/TR/REC-xml-names) .
	 * </p>
	 * 
	 * <p>
	 * Names {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME} and {@link Taxonomy#SUBJECT_TAXONOMY_NAME}
	 * are reserved names and 
	 * an {@link CmsException exception} is thrown in case user tries to save an new taxonomy
	 * with either of these names.
	 * </p>
	 * 
	 * <p>
	 * Use of this method for  
	 * built in subject taxonomy (Taxonomy with name {@link Taxonomy#SUBJECT_TAXONOMY_NAME}) or 
	 * a {@link RepositoryUser#getFolksonomy() repository user's taxonomy}
	 * updates only localized labels for this taxonomy. 
	 * </p>
	 * 
	 * 
	 * @param taxonomy
	 *            {@link Taxonomy Taxonomy} to be saved.
	 *            
	 * @deprecated Use method {@link #save(Object)} instead
	 *              
	 *              
	 * @return Newly created or updated Taxonomy            
	 */
	@Deprecated
	Taxonomy saveTaxonomy(Taxonomy taxonomy);

	/**
	 * Deletes a taxonomy and all topics that belong to this taxonomy.
	 * All references from {@link ContentObject content objects} to these topics will be deleted as well.
	 *  
	 * This method does not delete {@link Taxonomy#SUBJECT_TAXONOMY_NAME Astroboa Subject taxonomy}
	 * and  {@link RepositoryUser repository users} folksonomies.
	 *   
	 * @param taxonomyIdOrName Taxonomy identifier or name
	 * @return <code>true</code> if taxonomy has been successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteTaxonomyTree(String taxonomyIdOrName);
	
	/**
	 * Return  taxonomy with specified name.
	 * 
	 * @param taxonomyName
	 *            {@link Taxonomy#getName() Taxonomy name}.
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 *            
	 * @deprecated Use {@link #getTaxonomy(String, ResourceRepresentationType, FetchLevel)}
	 * @return {@link Taxonomy} with the specified name or null if not found.
	 */
	@Deprecated
	Taxonomy getTaxonomy(String taxonomyName, String locale);

	/**
	 * Return all taxonomies in content repository model.
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 *            
	 * @deprecated Use {@link #getAllTaxonomies(ResourceRepresentationType, FetchLevel, boolean)} instead
	 * @return A list of all taxonomies in content repository model.
	 */
	@Deprecated
	List<Taxonomy> getTaxonomies(String locale);

	/**
	 * Return  Astroboa system {@link Taxonomy#SUBJECT_TAXONOMY_NAME taxonomy}.
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null), all
	 *            localized labels will be rendered.
	 * @return Built in {@link Taxonomy} with name {@link Taxonomy#SUBJECT_TAXONOMY_NAME}. Always not null.
	 */
	Taxonomy getBuiltInSubjectTaxonomy(String locale);
	
	/**
	 * Single point of retrieving a {@link Taxonomy} from a repository.
	 * 
	 * <p>
	 * A taxonomy can be retrieved as XML, as JSON or as a {@link Taxonomy} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String taxonomyXML = taxonomyService.getTaxonomy("name", ResourceRepresentationType.XML, FetchLevel.ENTITY);
	 *  		 
	 *  String taxonomyJSON = taxonomyService.getTaxonomy("name", ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
	 *  		 
	 *  Taxonomy taxonomy = taxonomyService.getTaxonomy("name", ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.FULL);
	 *  		
	 *  CmsOutcome<Taxonomy> taxonomyOutcome = taxonomyService.getTaxonomy("name", ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.FULL);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#TAXONOMY_LIST} represents a list of
	 * taxonomies, rather than one and therefore its use in this context is not recommended.
	 * Nevertheless, if used, a list containing one taxonomy will be provided.
	 * </p>
	 * 
	 * <p>
	 * Users have also the option to specify whether to fetch only taxonomy's properties, 
	 * or to load its children as well as the whole taxonomy tree. This is a way to control
	 * lazy loading by pre-fetching taxonomy children. Bear in mind that lazy loading mechanism
	 * is meaningful only when {@link Taxonomy} or {@link CmsOutcome} instance is returned. Other
	 * representations (XML and JSON) do not support lazy loading.
	 * 
	 * </p>
	 * 
	 * <p>
	 * Also, in cases where no output type is defined, a {@link Taxonomy} instance
	 * is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTaxonomy","localization":{"label":{"en":"My first taxonomy"}}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"taxonomy":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTaxonomy","localization":{"label":{"en":"My first taxonomy"}}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, in case no taxonomy is found for provided <code>taxonomyId</code>, 
	 * <code>null</code>is returned.
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link Taxonomy} or {@link CmsOutcome}
	 * @param taxonomyIdOrName {@link Taxonomy#getName() taxonomy name} or {@link Taxonomy#getId() taxonomy id }
	 * @param output Taxonomy representation output, one of XML, JSON or {@link Taxonomy}. Default is {@link ResourceRepresentationType#TAXONOMY_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Taxonomy}'s only properties, its children as well or the whole {@link Taxonomy} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if
	 *  
	 * @return A taxonomy as XML, JSON or {@link Taxonomy}, or <code>null</code> of none is found.
	 */
	<T> T getTaxonomy(String taxonomyIdOrName, ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint);

	
	
	/**
	 * A helper method which provides all taxonomies of a repository
	 * in the specified output.
	 * 
	 * <p>
	 * Its behavior is the same with method {@link #getTaxonomy(String, ResourceRepresentationType, FetchLevel)}.
	 * Users may choose only the preferred output type. For simplicity reasons, 
	 * only taxonomies are returned and not all taxonomy trees.
	 * </p>
	 * 
	 * <p>
	 * Although users may choose, output type, {@link ResourceRepresentationType#TAXONOMY_INSTANCE} 
	 * will cause a {@link CmsException} to be thrown in case there is more than 
	 * one taxonomy available.
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"totalResourceCount":"0", "offset":"0", resourceRepresentation:[{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTaxonomy","localization":{"label":{"en":"My first taxonomy"}}}}, {...}]
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"resourceResponse":{"totalResourceCount":"0", "offset":"0", resourceRepresentation:[{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"myTaxonomy","localization":{"label":{"en":"My first taxonomy"}}}}, {...}]}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * @param <T> {@link String}, or {@link CmsOutcome}
	 * @param output Taxonomy representation output, one of XML, JSON or {@link Taxonomy}. Default is {@link ResourceRepresentationType#TAXONOMY_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Taxonomy}'s only properties, its children as well or the whole {@link Taxonomy} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if 
	 * 
	 * @return All taxonomies as XML, JSON or {@link CmsOutcome}
	 */
	<T> T getAllTaxonomies(ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint);

	/**
	 * Save or update a {@link Taxonomy taxonomy} in content repository model.
	 * 
	 * <p>
	 * This method expects either a {@link Taxonomy} instance
	 * or a {@link String} instance which corresponds to an XML or
	 * JSON representation of the entity to be saved.
	 * </p>
	 * 
	 * <p>
	 * This method saves or updates taxonomy's name and localized labels and NOT its root topics.
	 * In order to save its root topics, a call to {@link TopicService#saveTopic(Topic)}
	 * for each root topic must be made.
	 * </p>
	 * 
	 * <p>
	 * In case of insert or update full Taxonomy tree, one could export taxonomy to an xml ({@link Taxonomy#xml()})
	 * and then call {@link ImportService#importTaxonomy(String)}.
	 * </p>
	 * 
	 * <p>
	 * Taxonomy {@link Taxonomy#getName() name} must be unique across the repository and a valid XML name
	 * according to XML Namespaces recommendation [4] (http://www.w3.org/TR/REC-xml-names) .
	 * </p>
	 * 
	 * <p>
	 * Names {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME} and {@link Taxonomy#SUBJECT_TAXONOMY_NAME}
	 * are reserved names and 
	 * an {@link CmsException exception} is thrown in case user tries to save an new taxonomy
	 * with either of these names.
	 * </p>
	 * 
	 * <p>
	 * Use of this method for  
	 * built in subject taxonomy (Taxonomy with name {@link Taxonomy#SUBJECT_TAXONOMY_NAME}) or 
	 * a {@link RepositoryUser#getFolksonomy() repository user's taxonomy}
	 * updates only localized labels for this taxonomy. 
	 * </p>
	 * 
	 * 
	 * @param taxonomySource
	 *            {@link Taxonomy Taxonomy} to be saved.  
	 * @return Newly created or updated Taxonomy            
	 */
	Taxonomy save(Object taxonomySource);
	
	/**
	 * Retrieve a taxonomy using its identifier .
	 * 
	 * @deprecated Use {@link #getTaxonomy(String, ResourceRepresentationType, FetchLevel)} instead
	 * 
	 * @param taxonomyId
	 *            {@link Taxonomy#getId() Taxonomy id}.
	 * @return {@link Taxonomy} with the specified identifier or null if not found.
	 */
	@Deprecated
	Taxonomy getTaxonomyById(String taxonomyId);
}
