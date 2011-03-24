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

package org.betaconceptframework.astroboa.api.model.definition;

import java.util.Map;

import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;



/**
 * Definition of a simple {@link SimpleCmsProperty property} of a content 
 * object type.
 * 
 * <p>
 * This interface specifies common attributes of a simple
 * content object property definition.
 * <ul>
 * <li>{@link #getDefaultValue() default value}.
 * </ul>
 * 
 * @param <T> Type of values of simple property. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SimpleCmsPropertyDefinition<T> 
	extends CmsPropertyDefinition {

	/**
	 * Returns the default value for a property.
	 * 
	 * Astroboa implementation uses XML Schema attribute
	 * <a href="http://www.w3.org/TR/xmlschema-1/#Attribute_Declaration_details">default</a>
	 *  to provide value for this attribute.
	 * 
	 * @return Property's default value.
	 */
	T getDefaultValue();

	/**
	 * Returns <code>true</code> if definition has a default value,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if definition has a default value,
	 *         <code>false</code> otherwise.
	 */
	boolean isSetDefaultValue();
	
	/**
	 * Provides a set of all possible values.
	 * 
	 * This is useful in cases of enumerations.
	 * 
	 * It also provides any Localization information
	 * 
	 * @return An enumeration of all possible values that this property can have
	 */
	Map<T, Localization> getValueEnumeration();

	/**
	 * Checks whether the provided value is valid
	 * 
	 * @param value Value to check
	 * @return <code>true</code> if value is valid, <code>false</code> otherwise
	 */
	boolean isValueValid(T value);
}
