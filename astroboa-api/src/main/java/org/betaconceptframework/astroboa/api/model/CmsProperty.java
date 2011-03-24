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

import java.util.Locale;

import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;


/**
 * Represents a property of a {@link ContentObject content object} of 
 * any {@link ValueType type} except 
 * {@link ValueType#ContentType content object type}.
 * 
 * <p>
 * Base interface containing all methods a content object property
 * should have regardless of its type.
 * </p>
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public interface CmsProperty<
			D extends CmsPropertyDefinition, 
			P extends ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, 
										 ? extends ComplexCmsProperty<?,?>>>
extends LocalizableEntity{

	/**
	 * Returns the {@link ValueType type} for this property.
	 * 
	 * @return Property's value type.
	 */
	ValueType getValueType();
	
	/**
	 * Returns  the name of property.
	 * 
	 * Property name is the name provided in its {@link CmsPropertyDefinition definition}.
	 * @return Property's name.
	 */
	String getName();
	
	/**
	 * Returns the parent of this property.
	 * 
	 * @return Property's parent.
	 */
	P getParentProperty();
	/**
	 * Sets the parent of this property.
	 * 
	 * @param parent Property's parent.
	 */
	void setParent(P parent);
	
	/**
	 * Returns the full path for  this property.
	 * 
	 * <p>
	 * Full path is a period-delimited {@link String} 
	 * containing all the names of properties starting 
	 * from {@link ComplexCmsRootProperty},
	 * whose name is {@link ContentObject}'s type name 
	 * and ending with this property's name. It may 
	 * also contain zero based index, if a property exists 
	 * multiple times. For example,
	 * </p>
	 * 
	 * <code>article.profile.title</code> describes a property 
	 * <code>title</code> belonging to property <code>profile</code> which 
	 * by its turn belongs to content object type <code>article</code>.
	 * 
	 * <code>article.profile.comment[2]</code> describes the third property 
	 * <code>comment</code> belonging to property <code>profile</code> which 
	 * by its turn belongs to content object type <code>article</code>.
	 * 
	 * <p>
	 * By default the lack of index assumes 0 value, unless
	 * explicitly stated otherwise. Index is meaningful only for
	 * multi value properties. 
	 * </p>
	 * 
	 * @return 
	 * 		A period-delimited string describing property
	 *      path from root content object type.
	 */
	String getFullPath();

	/**
	 * Returns the  path for  this property.
	 * 
	 * Similar semantics with {@link #getFullPath()} but path is relative to 
	 * content type, i.e. it does not include it. 
	 * 
	 * @return
	 * 		A period-delimited string describing property
	 *      relative path from root content object type.
	 */
	String getPath();
	/**
	 * Returns the full localized path of this property.
	 * 
	 * <p>
	 * Its semantics are the same with {@link #getFullPath() full path} but it returns the
	 * localized label defined for the specified locale of each 
	 * property in the path, instead of property's name.
	 * </p>
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank,
	 *            {@link Locale#ENGLISH English} locale will be  used.
	 *            
	 * @return A period-delimited string describing property
	 *         localized path from root content object type.
	 * 
	 */
	String getLocalizedLabelOfFullPathforLocale(String locale);
	
	/**
	 * Same semantics with {@link #getLocalizedLabelOfFullPathforLocale(String)} 
	 * but delimiter is specified by the user.
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank,
	 *            {@link Locale#ENGLISH English} locale will be used.
	 *            
	 * @param delimiter Delimiter string
	 * @return A delimited string describing property
	 *         localized path from root content object type.
	 * 
	 */
	String getLocalizedLabelOfFullPathforLocaleWithDelimiter(String locale, String delimiter);
	
	/**
	 * Sets the definition for property. Definition should have the
	 * same {@link ValueType type} with this property.
	 * 
	 * @param propertyDefinition
	 *            Property's definition.
	 */
	void setPropertyDefinition(D propertyDefinition);

	/**
	 * Returns the definition for property.
	 * 
	 * @return Property's definition.
	 */
	D getPropertyDefinition();

	/**
	 * Returns the permanent path for this property.
	 * 
	 * <p>
	 * The permanent path is a period-delimited {@link String} 
	 * which represents the path to the property from
	 * the root definition ({@link ContentObjectTypeDefinition}) and it
	 * guarantees that it corresponds to the same property as long as the property 
	 * is not removed.
	 * </p>
	 * 
	 * <p>
	 * It has similar semantics with the method {@link #getPath()} but differs in the 
	 * way the path is generated when it comes for multiple value properties. The property's 
	 * identifier is used instead of its index.
	 * </p>
	 * 
	 *  <p>
	 *  For example, a blog's comment can be defined as a multiple value complex
	 *  property with a body. The following table displays
	 *  the paths generated for the first and the second comment of a blog
	 *  (index 0 is omitted).
	 *  
	 *  <ul>
	 *   <li> <code>getPath() :  comment.body</li>
	 *   <li> <code>getFullPath() :  blogObject.comment.body</li>
	 *   <li> <code>getPermanentPath() :  comment[<UUID>].body</li>
	 *   <li> <code>getFullPermanentPath() :  blogObject.comment[<UUID>].body</li>
	 *  </ul>
	 *  
	 *  <ul>
	 *   <li> <code>getPath() :  comment[1].body</li>
	 *   <li> <code>getFullPath() :  blogObject.comment[1].body</li>
	 *   <li> <code>getPermanentPath() :  comment[<UUID>].body</li>
	 *   <li> <code>getFullPermanentPath() :  blogObject.comment[<UUID>].body</li>
	 *  </ul>
	 *  
	 *  
	 *  </p>
	 *  
	 *  The use of the property's identifier instead of its index makes the path independent
	 *  of the changes made in the list which contains the property. This way, the property
	 *  can change its index in the list but the user can still have a path which corresponds to the same
	 *  property. For example, the user will retrieve the same property if she provides the permanent path
	 *  to the method {@link ContentObject#getCmsProperty(String)} as long as the property is not removed.
	 * <p>
	 * 
	 * <p>
	 * Note that in cases where this method is used on properties whose values are not yet persisted, an exception is thrown 
	 * </p>
	 * 
	 * @return 
	 * 		A period-delimited string describing the permanent property
	 *      path from root content object type.
	 */
	String getFullPermanentPath();
	
	/**
	 * Returns the  permanent path for  this property.
	 * 
	 * Similar semantics with {@link #getFullPermanentPath()} but path is relative to 
	 * content type, i.e. it does not include it.
	 *  
	 * @return
	 * 		A period-delimited string describing the permanent property
	 *      path relative to the object's content type.
	 */
	String getPermanentPath();

}
