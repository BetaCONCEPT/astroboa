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


import java.util.List;

import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;

/**
 * 
 * Represents an organization's content repository entity. From Astroboa
 * perspective organization's, content is a set of ContentObject of several types
 * (Text, Audio, Video, etc).
 * 
 * This interface relates Astroboa built-in content repository model with
 * organizaion's content repository model.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public interface ContentObject extends CmsRepositoryEntity {

	/**
	 * Returns the name of content object type as specified by organization.
	 * 
	 * @return A {@link String} representing content object type.
	 */
	String getContentObjectType();

	/**
	 * Sets the name of content object type as specified by organization.
	 * 
	 * @param contentObjectType
	 *            A {@link String} representing content object type.
	 */
	void setContentObjectType(String contentObjectType);

	/**
	 * Returns the owner of content object.
	 * 
	 * @return A {@link RepositoryUser} representing content object owner.
	 */
	RepositoryUser getOwner();

	/**
	 * Sets the owner of content object.
	 * 
	 * @param owner
	 *            A {@link RepositoryUser} representing content object owner.
	 */
	void setOwner(RepositoryUser owner);

	/**
	 * Checks if content object is locked by another process or application.
	 * 
	 * @return <code>true</code> if content object is locked,
	 *         <code>false</code> otherwise.
	 */
	boolean isLocked();

	/**
	 * Locks or unlocks a content object.
	 * 
	 * <p>
	 * Content Object must be save to make changes persistent.
	 * </p>
	 * 
	 * @param locked
	 *            <code>true</code> to lock content object, <code>false</code>
	 *            otherwise.
	 */
	void setLocked(boolean locked);

	/**
	 * Returns the definition of content object type.
	 * 
	 * @return Content object type's definition.
	 */
	ContentObjectTypeDefinition getTypeDefinition();

	/**
	 * Returns content object root property which holds all properties for a
	 * content object.
	 * 
	 * @return Content Object's property container.
	 */
	ComplexCmsRootProperty getComplexCmsRootProperty();

	/**
	 * Returns a list of all content object properties found under specified
	 * path.
	 * 
	 * <p>
	 * Convenient method instead of calling
	 * {@link ComplexCmsRootProperty#getChildPropertyList(String propertyPath)}
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * 
	 * @return A list containing all content object properties under specified
	 *         <code>propertyPath</code>.
	 */
	List<CmsProperty<?,?>> getCmsPropertyList(String relativePropertyPath);

	/**
	 * Returns at most one content object property found under specified path.
	 * 
	 * <p>
	 * Convenient method instead of calling
	 * {@link ComplexCmsRootProperty#getChildProperty(String propertyPath)}
	 * </p>
	 * 
	 * @see ComplexCmsProperty#getChildProperty(String) for more details
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return A content object property under specified
	 *         <code>propertyPath</code>.
	 * @throws MultipleOccurenceException
	 *             If property is defined as a a multiple value property.
	 */
	CmsProperty<?, ?> getCmsProperty(String relativePropertyPath)
			throws MultipleOccurenceException;

	/**
	 * Removes a content object property found in specified path.
	 * 
	 * <p>
	 * Convenient method instead of calling
	 * {@link ComplexCmsRootProperty#removeChildProperty(String propertyPath)}
	 * </p>
	 * 
	 * <p>
	 * This method is mainly used when user needs to remove a {@link ComplexCmsProperty}.
	 * In cases where a {@link SimpleCmsProperty} must be removed, it is recommended to use
	 * method {@link SimpleCmsProperty#removeValues()}.
	 * </p>
     *
     *
     * <p>
     * Note that, you have to save content object, to complete property removal.
     * </p>
     * 
     * @return <code>true</code> if property and its values have been successfully removed, <code>false</code> otherwise.
     * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 */
	boolean removeCmsProperty(String relativePropertyPath);

	/**
	 * Get the system name for this entity. 
	 * 
	 * @return Entity's system name
	 */
	String getSystemName();
	
	/**
	 * Specify the system name of this entity.
	 * 
	 * <p>
	 * System name represents a human readable value
	 * which identifies (more preferably uniquely) this entity. It may be used
	 * as opposed to Id in cases where Id does not provide an immediate clue of what 
	 * this entity represents.
	 * </p>
	 * 
	 * <p>
	 * System name is restricted to follow pattern <code>[A-Za-z0-9_\\-]+</code>, that is only Latin characters
	 * are permitted along with digits and/or '-', '_'. 
	 * </p>
	 * 
	 * @param systemName String following pattern [A-Za-z0-9_\\-]+
	 */
	void setSystemName(String systemName);
	
	/**
	 * Provides an xml representation of specified <code>contentObject</code>
	 * following content object type's xml schema as defined by 
	 * organization XML schemas.
	 * 
	 * <p>
	 * Note that all properties of content object are exported.
	 * </p>
	 * @deprecated Use {@link #xml()} instead
	 * @return XML instance for this <code>contentObject</code>.
	 */
	String toXml();
	
	/**
	 * 
	 * This method adds a new {@link ComplexCmsProperty} to the provided path.
	 * 
	 * <p>
	 * This method should be used when user wants to add a new complex property but does not
	 * know the number of existing complex properties under the same relative path. 
	 * For example, there is a complex property named "comment" and user wants to add a new comment.
	 * 
	 * If user already knows how many comments exist so far (by executing method 
	 * <pre>int size = getCmsPropertyList("comment").size()</pre>, be aware of NullPointerException)
	 * then she may call 
	 * <pre>getCmsProperty("comment["+size+"]")</pre>. Otherwise she must use this method 
	 * <pre>createNewValueForMulitpleComplexCmsProperty("comment")</pre>
	 * </p>
	 * 
	 * <p>
	 * Property corresponding to provided path must be a multivalued {@link ComplexCmsProperty}. Otherwise an
	 * exception is thrown. Note that prior to creating a new property all existed properties for 
	 * provided path will be loaded.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *  		A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 *        
	 * @return Newly created property.
	 */
	CmsProperty<?,?> createNewValueForMulitpleComplexCmsProperty(String relativePropertyPath);
	
	/**
	 * Provides an xml representation of specified <code>cmsRepositoryEntity</code>
	 * following content object type's xml schema as defined by 
	 * organization XML schemas.
	 * 
	 * <p>
	 * Only specified properties will be exported. If no properties are defined,
	 * then all its properties which have values will be exported and this will 
	 * cause all its properties to be lazy loaded in {@link ContentObject} the instance.
	 * </p>
	 * 
	 * <p>
	 * Be sure to know exactly what you are doing in case you set parameter <code>serializeBinaryContent</code>
	 * to <code>true</code>.
	 * </p>
	 * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise
	 * @param serializeBinaryContent <code>true</code> to export binary content, <code>false</code> otherwise. In any case 
	 * URL of binary content is provided. 
	 * @param propertiesToBeExported Array of property path 
	 * 
	 * @return XML instance for this <code>cmsRepositoryEntity</code>.
	 */
	String xml(boolean prettyPrint, boolean serializeBinaryContent, String... propertiesToBeExported);
	
	/**
	 * Provides a JSON representation of specified <code>cmsRepositoryEntity</code>
	 * following Mapped convention.
	 * 
     * The mapped JSON notation is used with stripped root element.
     * <p>Example JSON expression:<pre>
     * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
     * </pre>
     * </p>
     * 
	 * <p>
	 * Only specified properties will be exported. If no properties are defined,
	 * then all its properties which have values will be exported and this will 
	 * cause all its properties to be lazy loaded in {@link ContentObject} the instance.
	 * </p>
	 * 
	 * <p>
	 * Be sure to know exactly what you are doing in case you set parameter <code>serializeBinaryContent</code>
	 * to <code>true</code>.
	 * </p>
	 * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding indentation and linefeeds in order to make output more human readable, <code>false<code> otherwise
	 * @param serializeBinaryContent <code>true</code> to export binary content, <code>false</code> otherwise. In any case 
	 * URL of binary content is provided. 
	 * @param propertiesToBeExported Array of property path 
	 * 
	 * @return JSON representation for this <code>cmsRepositoryEntity</code>.
	 */
	String json(boolean prettyPrint, boolean serializeBinaryContent, String... propertiesToBeExported);

	/**
	 * Checks whether there is a value for the specified property, regardless of whether
	 * property has been loaded to {@link ContentObject} or not.
	 * 
	 * <p>
	 * This method does not reflect the persistent state of the {@link ContentObject} but rather 
	 * the state of the specific instance.  
	 * </p>
	 * <p>
	 * It first checks whether the property has been loaded to {@link ContentObject}
	 * instance. If it has not, then it checks the repository for its value existence.
	 * Otherwise, it checks the loaded property. 
	 * </p>
	 * 
	 * <p>
	 * In cases where , user has removed property but has not yet saved {@link ContentObject},
	 * this method returns false, even though changes are not yet persisted.
	 * </p>
	 * 
	 * <p>
	 * For example, user loads a {@link ContentObject} from a repository and wants to check whether
	 * property  <code>profile.creator</code> has a value
	 * 
	 * <ul>
	 * <li>If user has never called method {@link #getCmsProperty(String)}, then this method will check the repository
	 * for value existence</li>
	 * <li>If user has called method {@link #getCmsProperty(String)} or retrieved {@link ContentObject} with all its properties, then 
	 * this method will check loaded properties and will not check the repository.</li>
	 * </ul>
	 * 
	 * At any case, this method does not create a new property if no value is found.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return <code>true</code> if <code>propertyPath</code> has a value, <code>false</code> otherwise.
	 */
	boolean hasValueForProperty(String relativePropertyPath);
}
