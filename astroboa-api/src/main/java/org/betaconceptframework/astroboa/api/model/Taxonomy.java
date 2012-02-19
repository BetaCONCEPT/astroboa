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
 * <p>
 * Represents a category or a vocabulary inside Astroboa repository model. It may
 * be used to categorized {@link ContentObject contentObjects}. It contains one or more
 * {@link Topic topic} and supports localization so that can be displayed in any client
 * application.
 * 
 * It may be considered the root node of a tree of {@link Topic topics}.
 * </p>
 * 
 * <p>
 * Astroboa provides a built-in taxonomy called {@link Taxonomy#SUBJECT_TAXONOMY_NAME}
 * which represents a category for any tag representing the subject of a content object.
 * It is not mandatory to use this taxonomy, it is offered as a convenience.
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface Taxonomy extends LocalizableEntity {

	/**
	 * Astroboa built-in taxonomy's name. This taxonomy is created by default once
	 * content repository model is instantiated.
	 */
	public final static String SUBJECT_TAXONOMY_NAME = BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":subjectTaxonomy";
	/**
	 * Every repository user in Astroboa content repository model has her own taxonomy,
	 * called  <code>folksonomy</code>. This taxonomy is 
	 * automatically created for every new repository user.
	 */
	public final static String REPOSITORY_USER_FOLKSONOMY_NAME = BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":folksonomy";
	
	/**
	 * Sets the name of taxonomy. 
	 * 
	 * Must be a valid XML name (NCName)
	 * according to XML Namespaces (http://www.w3.org/TR/REC-xml-names, Section 3 Declaring Namespaces, [4])
	 * 
	 * @param taxonomyName
	 *            Taxonomy name.
	 */
	void setName(String taxonomyName);

	/**
	 * Returns the name of taxonomy
	 * 
	 * @return Taxonomy name
	 */
	String getName();

	/**
	 * Sets taxonomy's children.
	 * 
	 * @param rootTopics
	 *            A {@link List list} of {@link Topic topic}.
	 */
	void setRootTopics(List<Topic> rootTopics);

	/**
	 * Returns taxonomy's children
	 * 
	 * @return A {@link List list} of {@link Topic topic}.
	 */
	List<Topic> getRootTopics();

	/**
	 * Adds a taxonomy's child.
	 * 
	 * @param rootTopic
	 *            A {@link Topic}
	 */
	void addRootTopic(Topic rootTopic);

	/**
	 * 
	 * Check if taxonomy children are loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load taxonomy children, this method checks
	 * if children are loaded without triggering the lazy loading mechanism.
	 * </p>
	 * 
	 * @return <code>true</code> if root topics are loaded, <code>false</code>
	 *         otherwise.
	 */

	boolean isRootTopicsLoaded();

	/**
	 * Sets the number of taxonomy's children.
	 * 
	 * @param numberOfRootTopics
	 *            Number of taxonomy's children
	 */
	void setNumberOfRootTopics(int numberOfRootTopics);  

	/**
	 * Returns the number of taxonomy's children. This method should not trigger
	 * any lazy loading mechanism if any, i.e. number of taxonomy root topics
	 * must always be available regardless of the actual topics loaded.
	 * 
	 * @return Number of taxonomy's children.
	 */
	int getNumberOfRootTopics();

	/**
	 * Provides an xml representation of specified <code>taxonomy</code>
	 * following taxonomy's xml schema as described in
	 * <code>astroboa-engine</code> module in 
	 * <code>META-INF/astroboa-model-{version}.xsd</code>
	 * file.
	 * 
	 * @deprecated Use {@link #xml()} instead
	 * @return XML instance for this <code>taxonomy</code>.
	 */
	String toXml();
	
}
