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

package org.betaconceptframework.astroboa.api.model;

import java.util.List;

import org.betaconceptframework.astroboa.api.service.SpaceService;

/**
 * Represents a place in content repository model where {@link ContentObject content objects}
 * with no obvious common characteristics can exist.
 * 
 * <p>
 * There are two starting points to find or create a space :
 * 
 * <ul>
 * <li> Repository user context {@link RepositoryUser#getSpace()}. Each {@link RepositoryUser repository user}
 * has her own space.
 * <li> Organization context {@link SpaceService#getOrganizationSpace()}. A common space to 
 * all repository users of content repository. 
 * </ul>
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface Space extends LocalizableEntity {
	
	/**
	 * Name for Astroboa built in space representing organization's space.
	 */
	final static String ORGANIZATION_SPACE_NAME = BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":organizationSpace";
	
	/**
	 * Sets the name of space.
	 * 
	 * <p>
	 * Space name follows the same pattern with {@link ContentObject#setSystemName(String)}.
	 * </p>
	 * @param name
	 *            Space name
	 */
	void setName(String name);

	/**
	 * Returns the name of space.
	 * 
	 * @return Space name
	 */
	String getName();

	/**
	 * Returns parent space
	 * 
	 * @return Parent Space, <code>null</code> if space is a
	 *         root node.
	 */
	Space getParent();

	/**
	 * Sets the parent of space.
	 * 
	 * @param parent
	 *            Space parent
	 */
	void setParent(Space parent);

	/**
	 * Returns the order of space among all other topics of the
	 * same level.
	 * 
	 * @return Order of space.
	 */
	Long getOrder();

	/**
	 * Sets the order of space.
	 * 
	 * @param order
	 *            Order of space.
	 */
	void setOrder(Long order);

	/**
	 * Returns the owner of space.
	 * 
	 * @return {@link RepositoryUser} who owns the space
	 */
	RepositoryUser getOwner();

	/**
	 * Sets the owner of space.
	 * 
	 * @param owner
	 *            {@link RepositoryUser} who owns the space
	 */
	void setOwner(RepositoryUser owner);

	/**
	 * Returns space children.
	 * 
	 * @return A {@link List list} of space children
	 */
	List<Space> getChildren();

	/**
	 * Sets space's children.
	 * 
	 * @param children
	 *            A {@link List list} of space children
	 */
	void setChildren(List<Space> children);

	/**
	 * Adds a space child.
	 * 
	 * @param child
	 *            Space child.
	 */
	void addChild(Space child);

	/**
	 * Check if children are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load space children, this method
	 * checks if children are loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if children are loaded, <code>false</code>
	 *         otherwise.
	 */
	boolean isChildrenLoaded();

	/**
	 * Returns the number of space children. 
	 * 
	 * <p>
	 * Calling this method should
	 * not trigger any lazy loading mechanism, if any. This is very convenient
	 * for cases where only the number of children is needed, for example in a
	 * tree-list structure.
	 * </p>
	 * 
	 * @return Number of space children.
	 */
	int getNumberOfChildren();

	/**
	 * Sets the number of space children.
	 * 
	 * @param numberOfChildren
	 *            Number of space children.
	 */
	void setNumberOfChildren(int numberOfChildren);

	/**
	 * Returns a list of ids of all {@link ContentObject content objects} which refer to this
	 * space.
	 * 
	 * @return A {@link List list} {@link ContentObject content object} ids.
	 */
	List<String> getReferrerContentObjects();

	/**
	 * Sets a list of ids of all {@link ContentObject content objects} which refer to this
	 * space.
	 * 
	 * @param referrerContentObjects
	 *            A {@link List list} {@link ContentObject content object} ids.
	 */
	void setReferrerContentObjects(List<String> referrerContentObjects);

	/**
	 * Adds a {@link ContentObject content object}'s id which refers to this space.
	 * 
	 * @param referrerContentObject
	 *            {@link ContentObject content object} id.
	 */
	void addReferrerContentObject(String referrerContentObject);

	/**
	 * Checks if {@link ContentObject content objects} that refer to this
	 * space are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load referrer {@link ContentObject content objects}, this
	 * method checks if their ids are loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if {@link ContentObject content object} ids are loaded,
	 *         <code>false</code> otherwise.
	 */
	boolean isReferrerContentObjectsLoaded();

	/**
	 * Returns the number of {@link ContentObject content object} which refer to space.
	 * 
	 * @return Number of referrer {@link ContentObject content objects}.
	 */
	int getNumberOfReferrerContentObjects();

	/**
	 * Sets the number of {@link ContentObject content object} which refer to space.
	 * 
	 * @param numberOfReferrerContentObjects
	 *            Number of referrer {@link ContentObject content objects}.
	 */
	void setNumberOfReferrerContentObjects(
			int numberOfReferrerContentObjects);

	/**
	 * Checks if the number of {@link ContentObject content objects} that refer to this
	 * space are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load referrer {@link ContentObject content objects}, this
	 * method checks if number of {@link ContentObject content object} are loaded without
	 * triggering the lazy loading mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if number of referrer {@link ContentObject content objects}
	 *         is loaded, <code>false</code> otherwise.
	 */
	boolean isNumberOfReferrerContentObjectsLoaded();

	/**
	 * Returns a list of {@link ContentObject content object} ids which belongs to this space.
	 * 
	 * @return A {@link List list} of {@link ContentObject content object} ids.
	 */
	List<String> getContentObjectReferences();

	/**
	 * Sets a list of {@link ContentObject content object} ids which belongs to this space.
	 * 
	 * @param contentObjectReferences
	 *            A {@link List list} of {@link ContentObject} ids.
	 */
	void setContentObjectReferences(List<String> contentObjectReferences);

	/**
	 * Adds a {@link ContentObject content object} id to the space.
	 * 
	 * @param contentObjectreference
	 *            {@link ContentObject} id.
	 */
	void addContentObjectReference(String contentObjectreference);

	/**
	 * Returns the number of {@link ContentObject content objects} 
	 * this space contains. Calling
	 * this method should not trigger any lazy loading mechanism, if any.
	 * 
	 * @return Number of {@link ContentObject content objects}.
	 */

	int getNumberOfContentObjectReferences();

	/**
	 * Sets the number of {@link ContentObject content objects} that exist in the space.
	 * 
	 * @param numberOfContentObjectReferences
	 *            Number of {@link ContentObject}.
	 */
	void setNumberOfContentObjectReferences(
			int numberOfContentObjectReferences);

	/**  
	 * 
	 * Check if {@link ContentObject content object} references are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load {@link ContentObject content object} references, this
	 * method checks if references are loaded without triggering the lazy
	 * loading mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if {@link ContentObject} references are
	 *         loaded, <code>false</code> otherwise.
	 */
	boolean isContentObjectReferencesLoaded();

}
