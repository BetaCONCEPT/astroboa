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

package org.betaconceptframework.astroboa.api.model;

import java.util.Map;

/**
 * Represents a user in content repository model. A user can own
 * {@link ContentObject content objects}, {@link Topic topics}, {@link Space spaces}, etc. It is a
 * Astroboa built in repository entity.
 *
 * <p>
 * A repository user has her own {@link Space space} to store {@link ContentObject content objects}
 * and her own {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy} to
 * manage her own tags or vocabularies.
 * <p>
 * 
 * <p>
 * It represents users of an organization which use organization's content
 * repository as well.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RepositoryUser extends CmsRepositoryEntity {

	/**
	 * Returns the organization-specific repository user id.
	 * 
	 * @return A {@link String} representing external repository user
	 *         id.
	 */
	String getExternalId();

	/**
	 * Sets the external repository user id.
	 * 
	 * Astroboa implementation does not check for uniqueness of this value.
	 * 
	 * @param externalId
	 *            A {@link String} representing external repository
	 *            user id.
	 */
	void setExternalId(String externalId);

	/**
	 * Returns the label of repository user. This label is not localized.
	 * 
	 * @return A {@link String} representing repository user's label.
	 */
	String getLabel();

	/**
	 * Sets the label for this repository user.
	 * 
	 * @param label
	 *            A {@link String} representing repository user's
	 *            label.
	 */
	void setLabel(String label);

	/**
	 * Returns the type of repository user.
	 * 
	 * @return {@link RepositoryUserType}
	 */
	RepositoryUserType getUserType();

	/**
	 * Sets the type of repository user.
	 * 
	 * @param userType
	 *            {@link RepositoryUserType}.
	 */
	void setUserType(RepositoryUserType userType);

	/**
	 * Returns the space of repository user.
	 * 
	 * @return {@link Space}.
	 */
	Space getSpace();

	/**
	 * Returns the {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy}
	 * of repository user.
	 * 
	 * @return {@link Taxonomy} with name
	 *         {@link Taxonomy#REPOSITORY_USER_FOLKSONOMY_NAME folksonomy}.
	 */
	Taxonomy getFolksonomy();

	/**
	 * A map of organization-specific properties and their values for a
	 * repository user. Allows an organization to freely specify more properties
	 * for her own repository user.
	 * 
	 * @param preferences
	 *            A {@link Map map}, whose key is the property name and
	 *            map's value is property's value.
	 */
	void setPreferences(Map<String, Object> preferences);

	/**
	 * Returns a map of organization-specific properties of a repository user.
	 * 
	 * @return A {@link Map map}, whose key is the property name and
	 *         map's value is property's value.
	 */
	Map<String, Object> getPreferences();

	/**
	 * Adds an organization-specific property and its value for repository user.
	 * 
	 * @param prefereceName
	 *            Property's name.
	 * @param preferenceValue
	 *            Property's value.
	 */
	void addPreference(String prefereceName, Object preferenceValue);  
	
	/**
	 * Provides an xml representation of specified <code>repositoryUser</code>
	 * following repository user's xml schema as described in
	 * <code>astroboa-engine</code> module in 
	 * <code>META-INF/builtin-definition-schemas/astroboa-model-{version}.xsd</code>
	 * file.
	 * 
	 * <p>
	 * XML contains all basic properties of <code>repositoryUser</code>
	 * as well as basic properties of its {@link RepositoryUser#getFolksonomy() folksonomy} and not 
	 * its root topics. To generate XML for <code>folksonomy</code> use method
	 * {@link Taxonomy#toXml()}.
	 * </p>
	 * 
	 * <p>
	 * Also XML will contain basic properties of {@link RepositoryUser#getSpace() space}
	 * without its children if any. 
	 * </p>
	 *
	 * @deprecated Use {@link #xml()} instead
	 * @return XML instance for this <code>repositoryUser</code>.
	 */
	String toXml();
}
