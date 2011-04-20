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
import java.util.Locale;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;

/**
 * 
 * Represents a property of {@link ContentObject} whose type is {@link ValueType#Complex}.
 * 
 * A complex property is considered as a property which contains
 * other properties.
 * 

 * @param <D>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ComplexCmsProperty<D extends ComplexCmsPropertyDefinition,P extends ComplexCmsProperty<?,?>>
		extends CmsProperty<D,P> {

	/**
	 * Returns all child properties in a {@link Map map} whose key is
	 * child property name.
	 * 
	 * 
	 * @return A {@link Map map} whose key is child property name and value is a list 
	 * of {@link CmsProperty property} instances under the same name.
	 */
	Map<String, List<CmsProperty<?,?>>> getChildProperties();

	/**
	 * Returns all properties of this complex property found under the specified
	 * path.
	 * 
	 * <p>
	 * Note that provided path is relative to this complex property. That is
	 * search for child properties will be conducted starting from this
	 * complex property and NOT from {@link ComplexCmsRootProperty root} complex property.
	 * For example if complex property's name is <code>profile</code> and property path is
	 * <code>title</code> then this method will return any child property named <code>title</code>.
	 * If provided property path is <code>article.profile.title</code> then this method will look first for 
	 * a property named <code>article</code> and then for a property named <code>profile</code> under <code>article</code>,
	 * etc.
	 * </p>
	 * 
	 * <p>
	 * Check {@link #getChildProperty(String)} for further information on what 
	 * is happening if property is not found.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as defined in
	 *            {@link CmsProperty#getPath()}.
	 * @return A list containing all properties under specified
	 *         <code>propertyPath</code>.
	 */
	List<CmsProperty<?,?>> getChildPropertyList(String relativePropertyPath);

	/**
	 * Returns at most one {@link CmsProperty cms property} found under specified path.
	 * 
	 * <p>
	 * In case property for specified path does not exists,then a new property is created.
	 * In case any intermediate property in the path does not exist then it is created as well.
	 * </p>
	 * 
	 * <p>
	 * Check valid uses of this method for several cases of a property :
	 * 
	 *  
	 * <ul>
	 * <li>Single or Multi value simple property ({@link StringProperty}, {@link CalendarProperty}, {@link TopicReferenceProperty}), etc) <br/>
	 *   <code>getCmsProperty("title")</code> or <code>getCmsProperty("title[0]")</code>. <br/> 
	 *     <code>getCmsProperty("title[1]")</code> returns null
	 * </li>
	 * <li>Single value {@link ComplexCmsProperty} named profile<br/>
	 *   <code>getCmsProperty("profile")</code> or <code>getCmsProperty("profile[0]")</code>. <br/> 
	 *     <code>getCmsProperty("profile[1]")</code> returns null
	 * </li>
	 * <li>Multi value {@link ComplexCmsProperty} named profile<br/>
	 *   <code>getCmsProperty("profile")</code> or <code>getCmsProperty("profile[0]")</code>. <br/> 
	 *     <code>getCmsProperty("profile[1]")</code> creates a new property if <code>profile[0]</code>
	 *     exists, otherwise it returns null. 
	 *     <p>
	 *     If you want to add another a new {@link ComplexCmsProperty} but you do not know the correct index
	 *     use the number provided by executing {@link #createNewValueForMulitpleComplexCmsProperty(String)}.
	 *     </p>
	 * </li>
	 * </ul>
	 * 
	 *  
	 * <ul>
	 * <li>
	 * </li>
	 * <ul>
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return A content object property under specified
	 *         <code>propertyPath</code>.
	 * @throws MultipleOccurenceException
	 *             If property is defined as a a multiple value property.
	 */
	CmsProperty<?, ?> getChildProperty(String relativePropertyPath)
			throws MultipleOccurenceException;

	/**
	 * Removes a property found in specified path.
	 * 
	 * <p>
	 * This method is mainly used when user needs to remove a {@link ComplexCmsProperty}.
	 * In cases where a {@link SimpleCmsProperty} must be removed, it is recommended to use
	 * method {@link SimpleCmsProperty#removeValues()}.
	 * </p>
	 * 
	 * <p>
	 * In case provided path corresponds to a multi valued {@link ComplexCmsProperty},
	 * then lack of index in the path results in removing all {@link ComplexCmsProperty properties}
	 * and not the first one. For example, property <code>blog.comment</code> is a multi value
	 * complex property. Calling method  <code>removeChildProperty('blog.comment')</code> will remove
	 * all <code>comments</code> under <code>blog</code> whereas <code>removeChildProperty('blog.comment[0]')</code>
	 * will remove only the first comment.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 */

	boolean removeChildProperty(String relativePropertyPath);

	/**
	 * Check if a property under <code>propertyPath</code> is loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load children properties, this method
	 * checks if child property is loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return <code>true</code> if child property is loaded,
	 *         <code>false</code> otherwise.
	 */
	boolean isChildPropertyLoaded(String relativePropertyPath);

	/**
	 * Check if a property is defined for this complex property.
	 * 
	 * <p>
	 * Convenient method instead of using
	 * 
	 * <code>getPropertyDefinition().hasChildCmsPropertyDefinition(propertyPath)</code>
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 *            
	 * @return <code>true</code> if property has been defined for this complex
	 *         property, <code>false</code> otherwise.
	 */
	boolean isChildPropertyDefined(String relativePropertyPath);

	/**
	 * Sets definition for complex property.
	 * 
	 * @param propertyDefinition
	 *            Definition for complex property.
	 */
	void setPropertyDefinition(D propertyDefinition);

	/**
	 * Returns the definition for complex property.
	 * 
	 * @return Definition for complex property.
	 */
	D getPropertyDefinition();

	/**
	 * Check if at least property under <code>propertyPath</code> is loaded.
	 * 
	 * <p>
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load children properties, this method
	 * checks if child property is loaded without triggering the lazy loading
	 * mechanism.
	 * </p>
	 * 
	 * <p>
	 * In contrary to {@link #isChildPropertyLoaded(String)}}, this method 
	 * does not default index to 0 in case index is not specified, 
	 * which means that if a property is a multiple {@link ComplexCmsProperty complex},
	 * search will be conducted to every loaded instance of this property. 
	 * For example if property path is
	 * <code>profile.title</code> and <code>profile</code> is a multiple 
	 * property, this method will search every <code>profile</code> instance for 
	 * child property <code>title</code> and will return <code>true</code> if 
	 * at least one of these instances contains a <code>title</code>
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return <code>true</code> if at least one child property is loaded,
	 *         <code>false</code> otherwise.
	 */
	boolean atLeastOneChilPropertyIsLoaded(String relativePropertyPath);
	
	/**
	 * Returns all properties of this complex property found under the specified
	 * path.
	 *  
	 * <p>
	 * In contrary to {@link #getChildProperty(String)} or {@link #getChildPropertyList(String)}, 
	 * this method does not default index to 0 in case index is not specified, 
	 * which means that if a property is a multiple {@link ComplexCmsProperty complex},
	 * search will be conducted to every loaded instance of this property. 
	 * For example if property path is
	 * <code>profile.title</code> and <code>profile</code> is a multiple 
	 * property and there are 3 profile instances, 
	 * this method will return a list with 3 {@link CmsProperty properties}.
	 * </p>
	 * 
	 * <p>
	 * Check {@link #getChildProperty(String)} for further information on what 
	 * is happening if property is not found.
	 * </p>
	 * 
	 * @param relativePropertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @return A list containing all properties under specified
	 *         <code>propertyPath</code>.
	 */
	List<CmsProperty<?,?>> getAllChildProperties(String relativePropertyPath);
	
	/**
	 * This methods returns the value of a child {@link SimpleCmsProperty property}
	 * whose path is provided by {@link ComplexCmsPropertyDefinition#getPropertyPathWhoseValueCanBeUsedAsALabel()}.
	 * 
	 * <p>
	 * This value, if child property is found, is the following according to child property's type:
	 * (Note that if child property has multiple values its FIRST value is processed)
	 * 
	 * <ul>
	 *  <li>value provided by method {@link SimpleCmsProperty#getSimpleTypeValue()} or the first value from list 
	 *  {@link SimpleCmsProperty#getSimpleTypeValues()} for  
	 *  'primitive' types ({@link ValueType#Boolean}, {@link ValueType#Long},{@link ValueType#Date},{@link ValueType#Double}
	 *  {@link ValueType#String}).
	 *  <li>{@link Topic Topic}'s localized label for provided locale, if child property is a {@link ValueType#Topic}.
	 *  <li>{@link Space Space}'s localized label for provided locale, if child property is a {@link ValueType#Space}.
	 *  <li>{@link ContentObject Content object}'s <code>profile.title</code> value if child property is a {@link ValueType#ContentObject}
	 *  and property <code>profile.title</code> is defined. If no <code>profile.title</code> is provided then content
	 *  objetc's localized label for its {@link ContentObjectTypeDefinition  type} is provided.
	 *  <li>BinaryChannel's {@link BinaryChannel#getSourceFilename()  source file name}, if child property is a {@link ValueType#Binary}.
	 *  If no source file name is provided, {@link BinaryChannel#getRelativeDirectURL()  relative URL} is returned instead. If the latter is
	 *  null, {@link BinaryChannel#getName() name} is returned.
	 *  <li>Repository user {@link RepositoryUser#getLabel() label}, if child property is a {@link ValueType#RepositoryUser}, or
	 *  {@link RepositoryUser#getExternalId() external Id}, if no label is provided.
	 *  <li><code>null</code>, if child property is a {@link ValueType#ContentType} or if child property is a {@link ValueType#Complex}. Also a warning
	 *  is issued
	 * <ul>
	 * </p>
	 * 
	 * <p>
	 * It may be the case that provided property path has the keyword {locale}. In this case {locale}
	 * is replaced by provided <code>locale</code> parameter, and then a search for a specific simple property 
	 * is conducted.For example if path is <code>localizedLabels.{locale}</code> and <code>locale</code>
	 * parameter has value <code>en</code> then property with path <code>localizedLabels.en</code> will be searched.
	 * </p>
	 * @param locale 
	 * 			Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null),
	 *            {@link Locale#ENGLISH English} locale will be
	 *            used.
	 * @return String representation of this complex property's label according to value type of property found under
	 * specified property path, <code>null</code> if no property path is provided.
	 */
	String getPropertyLabel(String locale);
	
	/**
	 * 
	 * This method adds a new {@link ComplexCmsProperty} to the provided path.
	 * 
	 * <p>
	 * This method should be used when user wants to add a new complex property but does not
	 * know the number of existing complex properties under the same relative path. 
	 * For example, there is a complex property named "comment" and user wants to add a new comment.
	 * 
	 * If user already knows how many comments exist so far (by executing method <code>int size = getChildPropertyList("comment").size()</code>, be aware of NullPointerException)
	 * then she may call 
	 * <code>getChildProperty("comment["+size+"]")</code>. Otherwise she must use this method 
	 * <code>createNewValueForMulitpleComplexCmsProperty("comment")</code>
	 * </p>
	 * 
	 * <p>
	 * Property corresponding to provided path must be a multivalued {@link ComplexCmsProperty}. Otherwise an
	 * exception is thrown. Note that prior to creating a new property all existed properties for 
	 * provided path will be loaded.
	 * </p>	 * 
	 * 
	 * @param relativePropertyPath
	 *  		A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 *        
	 * @return Newly created property.
	 */
	CmsProperty<?,?> createNewValueForMulitpleComplexCmsProperty(String relativePropertyPath);
	

	/**
	 * Helper method to swap values within a multi valued {@link SimpleCmsProperty} or
	 * {@link ComplexCmsProperty}.
	 * 
	 * <p>
	 * <ul>
	 * <li>If property is not multi valued, method returns false.</li>
	 * <li>If property path contains index, method returns false.</li>
	 * <li>If any of the indices are out of bounds , method returns false.</li>
	 * <li>If indices are the same, swap returns false</li>
	 * <li>If any of the indices are negative, swap returns false</li> 
	 * </p>
	 * 
	 * <p>
	 * Bear in mind that in order for changes to be persisted, content object must be saved
	 * </p>
	 * 
	* @param relativePropertyPath
	 *  		A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @param from 
	 * 			the index of one element to be swapped, zero based.
     * @param to 
     * 			the index of the other element to be swapped, zero based.
     * 
     * @return <code>true</code> if swap was successful, <code>false</code> otherwise
	 */
	boolean swapChildPropertyValues(String relativePropertyPath, int from, int to);
	
	/**
	 * Helper method to change the position of a value within a multi valued {@link SimpleCmsProperty} or
	 * {@link ComplexCmsProperty}.
	 * 
	 * <p>
	 * <ul>
	 * <li>If property is not multi valued, method returns false.</li>
	 * <li>If the provided property path contains index, method returns false.</li>
	 * <li>If any of the indices are out of bounds , method returns false.</li>
	 * <li>If indices are the same, it returns false</li>
	 * <li>If any of the indices are negative, it returns false</li> 
	 * </p>
	 * 
	 * <p>
	 * Bear in mind that in order for changes to be persisted, content object must be saved
	 * </p>
	 * 
	* @param relativePropertyPath
	 *  		A period-delimited {@link String} as described in
	 *            {@link CmsProperty#getPath()}.
	 * @param from 
	 * 			the index of the element to be moved, zero based.
     * @param to 
     * 			the index of the position where the element will be moved, zero based, it should be less or equal to the size of the value list.
     * 
     * @return <code>true</code> if move was successful, <code>false</code> otherwise
	 */
	boolean changePositionOfChildPropertyValue(String relativePropertyPath, int from, int to);
	
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
	 * <li>If user has never called method {@link #getChildProperty(String)}, then this method will check the repository
	 * for value existence</li>
	 * <li>If user has called method {@link getChildProperty(String)} or retrieved {@link ContentObject} with all its properties, then 
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
	boolean hasValueForChildProperty(String relativePropertyPath);
}
