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

import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.api.model.exception.SingleOccurenceException;

/**
 * Represents a property of {@link ContentObject} whose type is one of
 * 
 * <ul>
 * <li>{@link ValueType#String}
 * <li>{@link ValueType#Date}
 * <li>{@link ValueType#Double}
 * <li>{@link ValueType#Long}
 * <li>{@link ValueType#Boolean}
 * <li>{@link ValueType#Binary}
 * <li>{@link ValueType#ContentObject}
 * <li>{@link ValueType#Topic}
 * <li>{@link ValueType#Space}
 * <li>{@link ValueType#RepositoryUser}
 * </ul>
 * 
 * In general a simple property is considered as a property which does not
 * contain other properties, .i.e. is not a property container.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SimpleCmsProperty<T, 
			D extends SimpleCmsPropertyDefinition<T>,
			P extends ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>>
		extends CmsProperty<D,P> {

	/**
	 * Returns simple type values for property. 
	 * 
	 * <p>
	 * If property is defined as a single value, this list will 
	 * return at most one entry, therefore method 
	 * {@link #getSimpleTypeValue()} should be used instead.
	 * This method should be used
	 * mainly when property is multiple. 
	 * </p>
	 * 
	 * @return A {@link List list} of simple type values
	 * @throws SingleOccurenceException
	 *             If property is defined as a single value property
	 *             but return list contains more than one entries.
	 */
	List<T> getSimpleTypeValues() throws SingleOccurenceException;

	/**
	 * Set simple type values for property.
	 * 
	 * @param values
	 *            A {@link List list} of simple type values
	 * @throws SingleOccurenceException
	 *             If property is defined as a single value property
	 *             and list contains more than one values
	 */
	void setSimpleTypeValues(List<T> values)
			throws SingleOccurenceException;

	/**
	 * Get simple type value for property.
	 * 
	 * @return A simple type value
	 * @throws MultipleOccurenceException
	 *             If property is defined as a a multiple value property
	 */
	T getSimpleTypeValue() throws MultipleOccurenceException;

	/**
	 * Add a value to properties that store simple type values (string, integer, boolean, double, etc.)  
	 * The method can safely be used for properties that either allow one or multiple values.
	 * 
	 * if the property allows multiple values the provided value will be added to the list of values unless it is null (if it is null it will be ignored).
	 * If the list does not exist it will be created first.
	 * 
	 * If the property allows only one value and a value already exists then the provided value 
	 * will substitute the existing value
	 * If the property allows only one value and a null value is provided the existing value will be cleared
	 * @param value
	 *            A simple type value
	 */
	void addSimpleTypeValue(T value);

	/**
	 * Set value for a single simple type.
	 * 
	 * @param value
	 *            A simple type value.
	 * @throws MultipleOccurenceException
	 *             If property is defined as a a multiple value property
	 */
	void setSimpleTypeValue(T value) throws MultipleOccurenceException;

	/**
	 * Remove a value from a multiple property.
	 * 
	 * @param index
	 *            Value's index
	 * @throws SingleOccurenceException
	 *             If property is defined as a a single value property.
	 *            
	 */
	 void removeSimpleTypeValue(int index)
			throws SingleOccurenceException;
	 
	 /**
	  * Check if property has no values.
	  * 
	  * Convenient method for checking emptyness of a property
	  * 
	  * @return <code>true</code> if property has no values, <code>false</code> otherwise
	  */
	 boolean hasNoValues();
	 
	 /**
	  * Check if property has no values.
	  * 
	  * Convenient method for checking emptyness of a property
	  * 
	  * @return <code>true</code> if property has values, <code>false</code> otherwise
	  */
	 boolean hasValues();

	 /**
	  * Convenient method to remove all values from property
	  */
	 void removeValues();

	 /**
	  * Swap values
	  *  
	  * @param from the index of one element to be swapped, zero based.
	  * @param to the index of the other element to be swapped, zero based.
	  * 
     * @return <code>true</code> if swap was successful, <code>false</code> otherwise
	  */
	boolean swapValues(int from, int to);
	
	/**
	  * Move a value to another position
	  *  
	  * @param from 
	  * 	the index of the element to be moved, zero based.
	  * @param to 
	  * 	the index of the position where the element will be moved, zero based, it should be less or equal to the size of the value list.
	  * 
    * @return <code>true</code> if move was successful, <code>false</code> otherwise
	  */
	boolean changePositionOfValue(int from, int to);
	
	/**
	 * Convenient method to retrieve the first value regardless of the
	 * cardinality.
	 * 
	 *  
	 * @return The value of the property if it is singled-valued, the first value if property is multi value or null if no value exists
	 */
	T getFirstValue();
}
