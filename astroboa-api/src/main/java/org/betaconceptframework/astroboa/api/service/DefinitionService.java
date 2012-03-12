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

package org.betaconceptframework.astroboa.api.service;



import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;

/**
 * Service providing methods for retrieving information about 
 * {@link ContentObject content object}'s type definition or 
 * {@link CmsProperty content object properties}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface DefinitionService {


	/**
	 * Checks for existence of a {@link ContentObjectTypeDefinition content object type definition}.
	 * 
	 * @param contentObjectTypeDefinitionName
	 *            Content object type definition name.
	 * @return <code>true</code> if content object type definition exists,
	 *         <code>false</code> otherwise.
	 */
	boolean hasContentObjectTypeDefinition(String contentObjectTypeDefinitionName);

	/**
	 * Returns all  {@link ContentObjectTypeDefinition content object type definition} names defined in content
	 * repository model.
	 * 
	 * @return A list of content object type definition names.
	 */
	List<String> getContentObjectTypes();

	/**
	 * Returns global {@link ComplexCmsPropertyDefinition complex property definitions} 
	 * sorted by their localized labels specified for <code>locale</code>.
	 *
	 * @param complexCmsPropertyNames
	 *            List of complex property definition names.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned definitions.
	 *            
	 * @return A list of complex content object property definitions.
	 * @see ComplexCmsPropertyDefinition for more on term <code>aspect</code>
	 */
	List<ComplexCmsPropertyDefinition> getAspectDefinitionsSortedByLocale(List<String> complexCmsPropertyNames,	String locale);

	/**
	 * Returns global {@link ComplexCmsPropertyDefinition complex property definitions } 
	 * sorted by their localized labels specified for <code>locale</code>.
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned definitions.
	 *            
	 * @return A list of complex content object property definitions.
	 * @see ComplexCmsPropertyDefinition for more on term <code>aspect</code>
	 */
	List<ComplexCmsPropertyDefinition> getAvailableAspectDefinitionsSortedByLocale(String locale);

	/**
	 * Retrieve all {@link TopicReferenceProperty topic property} paths per taxonomy
	 * defined for the connected repository. This is useful when search for a 
	 * specific {@link Topic topic} inside a {@link ContentObject contentObject}
	 * and we do not know the name of the topic property.
	 * 
	 * Used mainly internally when calling method {@link ContentObjectCriteria#addFullTextSearchCriterion(String)}.
	 * 
	 * @return All topic property paths per taxonomy, for all taxonomy specified in
	 * the connected repository.
	 */
	Map<String, List<String>> getTopicPropertyPathsPerTaxonomies();

	/**
	 * Retrieve all properties (just their names) which are multivalued.
	 * 
	 * Used mainly internally when exporting to JSON
	 * 
	 * @return A list of all multi valued properties for active repository
	 */
	List<String> getMultivalueProperties();

	/**
	 * Retrieve content type hierarchy. If no inheritance is found an empty map will be returned
	 * 
	 * @return A map whose key is base content type and value all sub content types for base content type
	 */
	Map<String, List<String>> getContentTypeHierarchy();

	/**
	 * Retrieve a definition for a content type or a complex property and specify 
	 * result output representation.
	 *  
	 * <p>
	 * Definitions can be retrieved as XML, as JSON , as XSD or as a {@link CmsDefinition} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String resultAsXML = definitionService.getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.XML);
	 *  
	 *  String resultAsXSD = definitionService.getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.XSD);
	 *  		 
	 *  String resultAsJSON = definitionService.getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.JSON);
	 *  		 
	 *  CmsDefinition cmsDefinition = definitionService.getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.DEFINITION_INSTANCE);
	 *  		
	 * </pre>
	 * </p>
	 * 
	 * 	<p>
	 * JSON representation example
	 * 
	 * <pre>
		{
		    "personObject":{
		     "path":"personObject"
		     ,"valueType":"ContentType"
		     ,"url":"http://host/resource-api/dipla/model/personObject?output=json"
		       ,"label":{
		        "el":"Πρόσωπο"
		        ,"en":"Person"
		       }
		       ,"profile":{
		        "path":"profile"
		        ,"valueType":"Complex"
		        ,"url":"http://host/resource-api/dipla/model/personObject.profile?output=json"
		        ,"mandatory":"true"
		        ,"multiple":"false"
		          ,"label":{
		           "el":"Μεταδεδομένα Dublin Core"
		           ,"en":"Dublin Core Metadata"
		          }
		          ,"title":{
		           "path":"profile.title"
		           ,"valueType":"String"
		           ,"url":"http://host/resource-api/dipla/model/personObject.profile.title?output=json"
		           ,"mandatory":"true"
		           ,"multiple":"false"
		             ,"label":{
		              "el":"Τίτλος"
		              ,"en":"Title"
		             }
		           ,"stringFormat":"PlainText"
          		}
          	}
       }
  	 * </pre>
	 * 
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, if no result is found <code>null</code> is returned
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link CmsDefinition}
	 * 
	 * @param fullPropertyDefinitionPath
	 * 			A period-delimited string defined in 
	 *            ({@link CmsPropertyDefinition#getPath()}).
	 * @param output {@link CmsDefinition} representation output, one of XML, JSON, XSD or {@link CmsDefinition} (default). 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise. Only useful if 
	 * <code>output</code> is either {@link ResourceRepresentationType#XML XML} or 
	 * {@link ResourceRepresentationType#JSON JSON}
  	 * @return CmsDefinition as XML, JSON, XSD or {@link CmsDefinition}	 
  	 */
	<T> T getCmsDefinition(String fullPropertyDefinitionPath, ResourceRepresentationType<T> output, boolean prettyPrint);

	/**
	 * Validates provided definition XSD file against existing built in definitions.
	 * 
	 * Convenient method used when a new XSD is added or an existing XSD is updated.
	 * This way one can change one or more definitions and validate them without the need
	 * to actually load them in to repository.
	 * 
	 * @param definition Definition to be validated
	 * @param definitionFileName Definition file name in case this is an updated definition
	 * 
	 * @return <code>true</code> if definition files is valid, <code>false</code> otherwise
	 */
	boolean validateDefinintion(String definition, String definitionFileName);
	
	/**
	 * Retrieve the depth for each content type definition.
	 * 
	 * Used mainly internally when calling method {@link ContentObjectCriteria#addFullTextSearchCriterion(String)}. 
	 * 
	 * However it can be used to get an idea of how deep the content model for a repository is.
	 * 
	 * @return A map of the depth per content type. There is also one more entry whose key is the '*' char which represents the max depth.
	 */
	Map<String, Integer> getDefinitionHierarchyDepthPerContentType();

	/**
	 * A helper method which returns the depth of the definition hierarchy for a specific type.
	 * 
	 * It has the same semantics with the method {@link #getDefinitionHierarchyDepthPerContentType()} and it is more 
	 * convenient to use when the content type is known apriori.
	 * 
	 * However, if you want to just get the maximum definition depth you can still use this method as long as you 
	 * provide the char '*' as the name of the content type.
	 * 
	 * @param contentType The name of the content type whose definition depth is requested. 
	 * 
	 * @return Content Type's definition hierarchy depth or 0 if not found. If content type is the '*' char, it returns the maximum definition hierarchy depth.
	 */
	Integer getDefinitionHierarchyDepthForContentType(String contentType);
	

	/**
	 * Convenient method for retrieving the type of a property
	 * 
	 * @param contentType The name of the content type whose definition depth is requested. If a blank (empty or null) string
	 * is provided then the provided property path will be searched in all content types
	 * 
	 * @param propertyPath A period-delimited string defined in 
	 *            ({@link CmsPropertyDefinition#getPath()}).
	 * @return One of {@link ValueType} or null if the property is not found 
	 */
	ValueType getTypeForProperty(String contentType, String propertyPath);
}