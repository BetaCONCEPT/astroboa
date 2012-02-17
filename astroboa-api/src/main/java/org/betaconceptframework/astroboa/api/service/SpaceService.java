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

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;

/**
 * Service providing methods for managing {@link Space spaces}.
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SpaceService {
	
	/**
	 * Save or update a {@link Space space} in content repository.
	 * 
	 * <p>
	 * Whether save or update process is followed depends on whether <code>space</code>
	 *  is a new space or not. <code>space</code> is considered new if there is no 
	 * {@link CmsRepositoryEntity#getId() id} or <code>id</code> is provided but there is
	 * no {@link Space space} in repository for the provided <code>id</code>. In this case
	 * <code>space</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>space</code> is considered new)
	 * 
	 * <ul>
	 * <li> Create a new {@link CmsRepositoryEntity#getId() id} or use the provided <code>id</code>.
	 * <li> Relate <code>space</code> with the provided {@link Space#getOwner() owner}.
	 * 		Space's owner MUST already exist. If not, an exception is thrown.
	 * <li> Locate <code>space</code>'s parent space using its identifier. If no parent is found an exception is thrown.
	 * <li> Save localized labels for <code>space</code>.
	 * <li> Save {@link Space#getOrder() order}.
	 * <li> Save {@link Space#getName() name}.
	 * <li> Save or update all of its {@link Space#getChildren() child spaces}.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>space</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>space</code> with the provided {@link Space#getOwner() owner} only 
	 * 		if the provided owner is different from already existed owner.
	 * 		Space's owner MUST already exist. If not, an exception is thrown.
	 * <li> Update <code>space</code>'s parent ONLY if provided parent identifier is different than one existed.
	 * <li> Update localized labels for <code>space</code>.
	 * <li> Update {@link Space#getOrder() order}.
	 * <li> Update {@link Space#getName() name}.
	 * <li> Update {@link Space#getParent() parent}, in case it has been changed. This corresponds
	 * 		to moving <code>space</code>.In this case new <code>space</code>'s parent
	 * 		must exist otherwise an exception is thrown.
	 * </ul>
	 * </p>
	 * @param space Space to be saved.
	 * 
	 * @deprecated Use method {@link #save(Object)} instead
	 * 
	 * @return Newly created or updated Space
	 */
	@Deprecated
	Space saveSpace(Space space);
	
	/**
	 * Remove a space from content repository. 
	 * All associations to this space will be deleted as well.
	 * 
	 * @param spaceId Space's id to be deleted.
	 * @return <code>true</code> if space has been successfully deleted, <code>false</code> otherwise
	 *  
	 */
	boolean deleteSpace(String spaceId);
	
	/**
	 * Returns a space for the specified <code>spaceId</code>.
	 * 
	 * @param spaceId
	 *            {@link Space#getId() Space's id}.
	 * @param locale
	 *            Locale value as defined in {@link Localization} to be
	 *            used when user calls method {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()}
	 *            to retrieve localized label for returned space.
	 * @deprecated use {@link #getSpace(String, ResourceRepresentationType#SPACE_INSTANCE, FetchLevel)} instead. Locale does not play any role
	 * since all localized labels are provided and method 
	 * @return A space corresponding to <code>spaceId</code> 
	 */
	@Deprecated
	Space getSpace(String spaceId, String locale);
	
	
	/**
	 * Retrieves organization space, a
	 * built-in space accessible from any user of Astroboa.
	 *  
	 * @return Α {@link Space} instance representing organization's space.
	 */
	Space getOrganizationSpace();

	/**
	 * Search all spaces satisfying specified criteria.
	 * 
	 * @param spaceCriteria
	 *            Space search criteria.
	 *            
	 * @deprecated Use {@link #searchSpaces(SpaceCriteria, ResourceRepresentationType)}           
	 * @return Spaces satisfying specified criteria.
	 */
	@Deprecated
	CmsOutcome<Space> searchSpaces(SpaceCriteria spaceCriteria);
	
	/**
	 * Returns a list of {@link ContentObject contentObject} identifiers which
	 * contain one or more {@link SpaceProperty property} whose value(s) is a 
	 * {#link Space space} with the provided <code>spaceId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on 
	 * {@link Space#getReferrerContentObjects()} method.
	 *   
	 * @param spaceId Space identifier
	 * 
	 * @return A list of content object identifiers
	 */
	List<String> getContentObjectIdsWhichReferToSpace(String spaceId);
	
	/**
	 * Returns count of {@link ContentObject contentObject} identifiers which
	 * contain one or more {@link SpaceProperty property} whose value(s) is a 
	 * {#link Space space} with the provided <code>spaceId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on {@link Space#getNumberOfReferrerContentObjects()} method.
	 *   
	 * @param spaceId Space identifier
	 * 
	 * @return Number of content objects referring to that space
	 */
	int getCountOfContentObjectIdsWhichReferToSpace(String spaceId);

	/**
	 * Returns a list of {@link ContentObject contentObject} identifiers which
	 * reside in {#link Space space} with the provided <code>spaceId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on 
	 * {@link Space#getContentObjectReferences()} method.
	 *   
	 * @param spaceId Space identifier
	 * 
	 * @return A list of content object identifiers
	 */
	List<String> getContentObjectIdsWhichResideInSpace(String spaceId);

	/**
	 * Returns count of {@link ContentObject contentObject} identifiers which
	 * reside in {#link Space space} with the provided <code>spaceId</code>.
	 * 
	 * This method is used mainly for lazy loading purposes. It should not used directly
	 * from user since it is fired internally on 
	 * {@link Space#getNumberOfContentObjectReferences()} method.
	 *   
	 * @param spaceId Space identifier
	 * 
	 * @return Number of content objects residing inside space.
	 */
	int getCountOfContentObjectIdsWhichResideInSpace(String spaceId);
	
	/**
	 * Single point of retrieving a {@link Space} from a repository.
	 * 
	 * <p>
	 * A space can be retrieved as XML, as JSON or as a {@link Space} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String spaceXML = spaceService.getSpace("id", ResourceRepresentationType.XML, FetchLevel.ENTITY);
	 *  		 
	 *  String spaceJSON = spaceService.getSpace("id", ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN);
	 *  		 
	 *  Space space = spaceService.getSpace("id", ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.FULL);
	 *  		
	 *  CmsOutcome<Space> spaceOutcome = spaceService.getSpace("id", ResourceRepresentationType.SPACE_LIST, FetchLevel.FULL);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#SPACE_LIST} represents a list of
	 * spaces, rather than one and therefore its use in this context is not recommended.
	 * Nevertheless, if used, a list containing one space will be provided.
	 * </p>
	 * 
	 * <p>
	 * Users have also the option to specify whether to fetch only space's properties, 
	 * or to load its children as well as the whole space tree. This is a way to control
	 * lazy loading by pre-fetching space children. Bear in mind that lazy loading mechanism
	 * is meaningful only when {@link Space} or {@link CmsOutcome} instance is returned. Other
	 * representations (XML and JSON) do not support lazy loading.
	 * 
	 * </p>
	 * 
	 * <p>
	 * Also, in cases where no output type is defined, a {@link Space} instance
	 * is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"mySpace","localization":{"label":{"en":"My first space"}}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"space":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"mySpace","localization":{"label":{"en":"My first space"}}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, in case no space is found for provided <code>spaceId</code>, 
	 * <code>null</code>is returned.
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link Space} or {@link CmsOutcome}
	 * @param spaceIdOrName {@link Space#getId() space id} or {@link Space#getName() space name}
	 * @param output Space representation output, one of XML, JSON or {@link Space}. Default is {@link ResourceRepresentationType#SPACE_INSTANCE}
	 * @param fetchLevel Specify whether to load {@link Space}'s only properties, its children as well or the whole {@link Space} tree.
	 * Default is {@link FetchLevel#ENTITY}
	 * 
	 * @return A space as XML, JSON or {@link Space}, or <code>null</code> of none is found.
	 */
	<T> T getSpace(String spaceIdOrName, ResourceRepresentationType<T> output, FetchLevel fetchLevel);

	/**
	 * Save or update a {@link Space space} in content repository.
	 * 
	 * <p>
	 * This method expects either a {@link Space} instance
	 * or a {@link String} instance which corresponds to an XML or
	 * JSON representation of the entity to be saved.
	 * </p>
	 * 
	 * <p>
	 * Whether save or update process is followed depends on whether <code>space</code>
	 *  is a new space or not. <code>space</code> is considered new if there is no 
	 * {@link CmsRepositoryEntity#getId() id} or <code>id</code> is provided but there is
	 * no {@link Space space} in repository for the provided <code>id</code>. In this case
	 * <code>space</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>space</code> is considered new)
	 * 
	 * <ul>
	 * <li> Create a new {@link CmsRepositoryEntity#getId() id} or use the provided <code>id</code>.
	 * <li> Relate <code>space</code> with the provided {@link Space#getOwner() owner}.
	 * 		Space's owner MUST already exist. If not, an exception is thrown.
	 * <li> Locate <code>space</code>'s parent space using its identifier. If no parent is found an exception is thrown.
	 * <li> Save localized labels for <code>space</code>.
	 * <li> Save {@link Space#getOrder() order}.
	 * <li> Save {@link Space#getName() name}.
	 * <li> Save or update all of its {@link Space#getChildren() child spaces}.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>space</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Relate <code>space</code> with the provided {@link Space#getOwner() owner} only 
	 * 		if the provided owner is different from already existed owner.
	 * 		Space's owner MUST already exist. If not, an exception is thrown.
	 * <li> Update <code>space</code>'s parent ONLY if provided parent identifier is different than one existed.
	 * <li> Update localized labels for <code>space</code>.
	 * <li> Update {@link Space#getOrder() order}.
	 * <li> Update {@link Space#getName() name}.
	 * <li> Update {@link Space#getParent() parent}, in case it has been changed. This corresponds
	 * 		to moving <code>space</code>.In this case new <code>space</code>'s parent
	 * 		must exist otherwise an exception is thrown.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In both cases,  save is NOT cascaded to Space's tree.
	 * </p> 
	 * 
	 * @param space Space to be saved.
	 * 
	 * @return Newly created or updated Space
	 */
	Space save(Object space);
	/**
	 * Query spaces using {@link SpaceCriteria} and specifying 
	 * result output representation.
	 *  
	 * <p>
	 * Query results can be retrieved as XML, as JSON or as a {@link CmsOutcome<Space>} instance.
	 * Each one of these representations can be specified through {@link ResourceRepresentationType}
	 * which has been designed in such a way that the returned type is available
	 * in compile time, avoiding unnecessary and ugly type castings.
	 * 
	 * <pre>
	 *  String resultAsXML = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.XML);
	 *  		 
	 *  String resultAsJSON = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.JSON);
	 *  		 
	 *  Space space = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.SPACE_INSTANCE);
	 *  		
	 *  CmsOutcome<Space> resultAsOutcome = spaceService.searchSpaces(spaceCriteria, ResourceRepresentationType.SPACE_LIST);
	 *  		 
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * You may have noticed that {@link ResourceRepresentationType#SPACE_INSTANCE} represents one content object
	 * only, rather than a list and therefore its use in this context is not recommended. However in the following
	 * cases a single content object or null is returned, instead of throwing an exception.
	 * 
	 * <ul>
	 * <li>User specified limit to be 1 ({@link SpaceCriteria#setLimit(1)}).
	 * 	In this case the first content object matching criteria is returned, or null if none matched criteria.<li>
	 * <li>User specified no limit or limit greater than 1. In this case if more than one spaces match
	 * criteria an exception is thrown</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Also, in cases where no output type is defined a {@link CmsOutcome<Space>} instance is returned. 
	 * </p>
	 * 
	 * 	<p>
	 * In JSON representation, note that root element has been stripped out. 
	 * i.e result will look like this
	 * 
	 * <pre>
	 * {"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"mySpace","localization":{"label":{"en":"My first space"}}}}
	 * </pre>
	 * 
	 * and not like this
	 * 
	 * <pre>
	 * {"space":{"cmsIdentifier":"092831be-43a4-4357-8bd8-5b9b43807f87","name":"mySpace","localization":{"label":{"en":"My first space"}}}}
	 * </pre>
	 *  
	 * </p>	
	 * 
	 * <p>
	 * Finally, if no result is found,
	 * 	 
	 * <ul>
	 * <li><code>null</code> is returned if <code>output</code> {@link ResourceRepresentationType#SPACE_INSTANCE}</li>
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
	 * <li>empty {@link CmsOutcome<Space>} is returned if <code>output</code> {@link ResourceRepresentationType#SPACE_LIST}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param <T> {@link String}, {@link Space} or {@link CmsOutcome}
	 * @param spaceCriteria
	 *           Space search criteria.
	 * @param output Space representation output, one of XML, JSON or {@link Space}. 
	 * 	Default is {@link ResourceRepresentationType#SPACE_LIST}
	 * 
	 * @return Spaces as XML, JSON or {@link CmsOutcome<Space>}
	 */
	<T> T  searchSpaces(SpaceCriteria spaceCriteria, ResourceRepresentationType<T> output);

}
