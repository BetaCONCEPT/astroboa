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
import java.util.Locale;

import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.RepositoryUserType;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;

/**
 * Service providing methods for managing
 * {@link RepositoryUser repository users}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryUserService {

	/**
	 * Returns all {@link RepositoryUser repository users} satisfying criteria.
	 * 
	 * @param repositoryUserCriteria
	 *            Repository user criteria.
	 * @return A list of repository users.
	 */
	List<RepositoryUser> searchRepositoryUsers(RepositoryUserCriteria repositoryUserCriteria);

	/**
	 * Remove repository user and all of its owned objects. Currently
	 * a repository user may own 
	 * {@link ContentObject contentObjects}, {@link Topic topics},
	 * {@link Space spaces} and {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy}.
	 * 
	 * @param repositoryUserId
	 *            {@link RepositoryUser#getId() Repository user id}.
	 */
	void deleteRepositoryUserAndOwnedObjects(String repositoryUserId);

	/**
	 * Remove repository user and replace her with an alternative user.
	 * Repository user's owned objects are not deleted but rather change
	 * owner.
	 * 
	 * @param repositoryUserId
	 *            {@link RepositoryUser#getId() Repository user id}.
	 * @param alternativeUser Repository user substitute.
	 */
	void deleteRepositoryUser(String repositoryUserId,	RepositoryUser alternativeUser);
	
	/**
	 * Get repository user for the specified <code>externalId</code>
	 * 
	 * @param externalId RepositoryUser externalId
	 * 
	 * @return RepositoryUser for <code>externalId</code>, null if none exists 
	 */
	RepositoryUser getRepositoryUser(String externalId);
	
	/**
	 * Convenient method to retrieve SYSTEM repository user.
	 * 
	 * It is the same with calling {@link #getRepositoryUser(String)} with 
	 * parameter value {@link CmsApiConstants#SYSTEM_REPOSITORY_USER_EXTRENAL_ID}.
	 * 
	 * @return Repository SYSTEM user
	 */
	RepositoryUser getSystemRepositoryUser() ;
	
	
	/**
	 * Saves or updates a {@link RepositoryUser repository user}.
	 * 
	 * <p>
	 * This method expects either a {@link RepositoryUser} instance
	 * or a {@link String} instance which corresponds to an XML or
	 * JSON representation of the entity to be saved.
	 * </p>
	 * 
	 * <p>
	 * Whether save or update process is followed depends on whether <code>repositoryUser</code>
	 *  is a new repository user or not. <code>repositoryUser</code> is considered new if there is no 
	 * {@link RepositoryUser#getId() id} or <code>id</code> is provided but there is
	 * no {@link RepositoryUser repository user} in repository for the provided <code>id</code>. In this case
	 * <code>repositoryUser</code> will be saved with the provided <code>id</code>.
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the save process (<code>repositoryUser</code> is considered new)
	 * 
	 * <ul>
	 * <li> Create a new {@link RepositoryUser#getId() id} or use the provided <code>id</code>.
	 * <li> Check that {@link RepositoryUser#getExternalId() external id} is unique
	 * <li> Save {@link RepositoryUser#getExternalId() external id}
	 * <li> Save {@link RepositoryUser#getLabel() label}
	 * <li> Save {@link RepositoryUser#getUserType() user type}. Default value is {@link RepositoryUserType#User}.
	 * <li> Creates a new {@link Space space} for repository user. This <code>space</code> has the following properties
	 *    <ul>
	 *      <li> Create a new {@link RepositoryUser#getId() id} or use the provided <code>id</code>.
	 *      <li> {@link Space#getName() name} : <code>Space for user</code> followed by {@link RepositoryUser#getExternalId() external id},
	 *      or the provided value from {@link Space#getName() name} from {@link RepositoryUser#getSpace() space}.
	 *      <li> Localized Label for {@link Locale#ENGLISH English locale} : <code>My Space</code> or 
	 *       any localized label provided, in case <code>repositoryUser</code> contains a 
	 *       {@link RepositoryUser#getSpace() space} instance.
	 *      <li> Save {@link Space#getOrder() order} for {@link RepositoryUser#getSpace() space}, if specified.
	 *    <ul>
	 * <li> Creates a new {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy} with the following properties
	 *    <ul>
	 *      <li> Create a new {@link RepositoryUser#getId() id} or use the provided <code>id</code>.
	 *      <li> {@link Taxonomy#getName() name} : {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME}
	 *      <li> Localized Label for {@link Locale#ENGLISH English locale} : <code>User Folksonomy</code> or 
	 *       any localized label provided, in case <code>repositoryUser</code> contains a 
	 *       {@link RepositoryUser#getFolksonomy() folksonomy} instance.
	 *    </ul>
	 *    
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The following steps take place in the update process (<code>topic</code> already exists in repository)
	 * 
	 * <ul>
	 * <li> Check that {@link RepositoryUser#getExternalId() external id} is unique
	 * <li> Update {@link RepositoryUser#getExternalId() external id} only if it is different than one existed.
	 * <li> Update {@link RepositoryUser#getLabel() label}
	 * <li> Update {@link RepositoryUser#getUserType() user type}. Default value is {@link RepositoryUserType#User}.
	 * <li> Update localized labels for {@link RepositoryUser#getFolksonomy() folskonomy}.
	 * <li> Update localized labels for {@link RepositoryUser#getSpace() space}.
	 * <li> Update {@link Space#getOrder() order} for {@link RepositoryUser#getSpace() space}.
	 * <li> Update {@link Space#getName() name} for {@link RepositoryUser#getSpace() space}.
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In order to save/update {@link RepositoryUser#getFolksonomy() folksonomy} root topics, method
	 * {@link TopicService#save(Object)} must be used for each one of the root topics.
	 * Similarly in order to save/update {@link RepositoryUser#getSpace() space} child spaces,
	 * method {@link SpaceService#saveSpace(Space)} must be used for each child space.
	 * </p>
	 * 
	 * <p>
	 * Astroboa system provides a built in SYSTEM repository user whose <code>externalId</code> is value<code>1</code>
	 * and cannot be altered.
	 * </p>
	 * 
	 * @param repositoryUser
	 *            Repository user to save or update.
	 *            
	 * @return Newly created or updated RepositoryUser
	 */	
	RepositoryUser save(Object repositoryUser);
}
