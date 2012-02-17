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

/**
 * Represents a specific category or a vocabulary entry of
 * {@link ContentObject content object}.
 * 
 * <p>
 * A Topic belongs to a {@link Taxonomy taxonomy} and it may have a parent topic.
 * A <code>root</code> topic is considered a topic which does not have a parent and
 * thus is directly connected to its {@link Taxonomy taxonomy}. 
 * A Topic has zero or many children nodes,  
 * belongs to a {@link RepositoryUser} and it may be referred by a
 * {@link ContentObject content object} {@link TopicReferenceProperty property}.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */ 
public interface Topic extends LocalizableEntity{
	/**
	 * Sets the name of topic.
	 * 
	 * <p>
	 * Topic name follows the same pattern with {@link ContentObject#setSystemName(String)}.
	 * </p>
	 * 
	 * @param name
	 *            Topic name
	 */
	void setName(String name);

	/**
	 * Returns the name of topic.
	 * 
	 * @return Topic name
	 */
	String getName();

	/**
	 * Returns parent topic
	 * 
	 * @return Parent Topic, <code>null</code> if topic is a
	 *         root node.
	 */
	Topic getParent();

	/**
	 * Sets the parent of topic.
	 * 
	 * @param parent
	 *            Topic parent
	 */
	void setParent(Topic parent);

	/**
	 * Returns the taxonomy of topic.
	 * 
	 * @return Taxonomy
	 */
	Taxonomy getTaxonomy();

	/**
	 * Sets the taxonomy of topic.
	 * 
	 * @param taxonomy
	 */
	void setTaxonomy(Taxonomy taxonomy);

	/**
	 * Returns the order of topic among all other topics of the
	 * same level.
	 * 
	 * @return Order of topic.
	 */
	Long getOrder();

	/**
	 * Sets the order of topic.
	 * 
	 * @param order
	 *            Order of topic.
	 */
	void setOrder(Long order);

	/**
	 * Specify whether topic can be referred by a {@link ContentObject content object}
	 * or not. In the latter case topic serves as a container for other
	 * topics only.
	 * 
	 * @param allowsReferrerContentObjects
	 *            <code>true</code> if topic can be referred by a
	 *            {@link ContentObject content object}, <code>false</code> otherwise.
	 */
	void setAllowsReferrerContentObjects(
			boolean allowsReferrerContentObjects);

	/** 
	 * Check whether this topic can be referred by an object or not.
	 * 
	 * @return <code>true</code> if topic can be referred by a
	 *         {@link ContentObject content object}, <code>false</code> otherwise.
	 */
	boolean isAllowsReferrerContentObjects();

	/**
	 * Returns the owner of topic.
	 * 
	 * @return {@link RepositoryUser} who owns the topic
	 */
	RepositoryUser getOwner();

	/**
	 * Sets the owner of topic.
	 * 
	 * @param owner
	 *            {@link RepositoryUser} who owns the topic
	 */
	void setOwner(RepositoryUser owner);

	/**
	 * Returns topic children.
	 * 
	 * @return A {@link List list} of topic children
	 */
	List<Topic> getChildren();

	/**
	 * Sets topic's children.
	 * 
	 * @param children
	 *            A {@link List list} of topic children
	 */
	void setChildren(List<Topic> children);

	/**
	 * Adds a topic child.
	 * 
	 * @param child
	 *            Topic child.
	 */
	void addChild(Topic child);

	/**
	 * Check if children are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load topic children, this method
	 * checks if children are loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if children are loaded, <code>false</code>
	 *         otherwise.
	 */
	boolean isChildrenLoaded();

	/**
	 * Returns the number of topic children. 
	 * 
	 * <p>
	 * Calling this method should
	 * not trigger any lazy loading mechanism, if any. This is very convenient
	 * for cases where only the number of children is needed, for example in a
	 * tree-list structure.
	 * </p>
	 * 
	 * @return Number of topic children.
	 */
	int getNumberOfChildren();

	/**
	 * Sets the number of topic children.
	 * 
	 * @param numberOfChildren
	 *            Number of topic children.
	 */
	void setNumberOfChildren(int numberOfChildren);

	/**
	 * Returns a list of ids of all {@link ContentObject content objects} which refer to this
	 * topic.
	 * @deprecated Use {@link #getContentObjectIdsWhichReferToThisTopic()} instead
	 * @return A {@link List list} {@link ContentObject content object} ids.
	 */
	List<String> getReferrerContentObjects();
	List<String> getContentObjectIdsWhichReferToThisTopic();

	/**
	 * Checks if {@link ContentObject content objects} that refer to this
	 * topic are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load referrer {@link ContentObject content objects}, this
	 * method checks if their ids are loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * @deprecated Use {@link #areContenObjectIdsWhichReferToThisTopicLoaded()} instead
	 * 
	 * @return <code>true</code> if {@link ContentObject content object} ids are loaded,
	 *         <code>false</code> otherwise.
	 */
	boolean isReferrerContentObjectsLoaded();
	boolean areContenObjectIdsWhichReferToThisTopicLoaded();

	/**
	 * Returns the number of {@link ContentObject content object} which refer to topic.
	 * 
	 * @deprecated Use {@link #getNumberOfContentObjectsWhichReferToThisTopic()} instead
	 * 
	 * @return Number of referrer {@link ContentObject content objects}.
	 */
	int getNumberOfReferrerContentObjects();
	int getNumberOfContentObjectsWhichReferToThisTopic();

	/**
	 * Checks if the number of {@link ContentObject content objects} that refer to this
	 * topic are loaded.
	 * 
	 * <p>  
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load referrer {@link ContentObject content objects}, this
	 * method checks if number of {@link ContentObject content object} are loaded without
	 * triggering the lazy loading mechanism.
	 * </p>
	 * 
	 * @deprecated Use {@link #isNumberOfContentObjectsWhichReferToThisTopicLoaded()} instead
	 * @return <code>true</code> if number of referrer {@link ContentObject content objects}
	 *         is loaded, <code>false</code> otherwise.
	 */
	boolean isNumberOfReferrerContentObjectsLoaded();
	boolean isNumberOfContentObjectsWhichReferToThisTopicLoaded();

	/**
	 * Provides an xml representation of specified <code>topic</code>
	 * following topic's xml schema as described in
	 * <code>astroboa-engine</code> module in 
	 * <code>META-INF/astroboa-model-{version}.xsd</code>
	 * file.
	 * 
	 * @deprecated Use {@link #xml()} instead
	 * @return XML instance for this <code>topic</code>.
	 */
	String toXml();
}
