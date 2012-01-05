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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.util.DateUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JcrValueUtils {

	/**
	 * Get values from repository AND transform to objects
	 * BINARY data is not processed by this method
	 * @param value
	 * @return
	 * @throws 
	 */
	public static Object getObjectValue(Value value) throws RepositoryException
	{
		if (value == null)
			return null;

		switch (value.getType()) {
		case PropertyType.BOOLEAN:
			return value.getBoolean();
		case PropertyType.DATE:
			return value.getDate();
		case PropertyType.DOUBLE:
			return value.getDouble();
		case PropertyType.LONG:
			return value.getLong();
		case PropertyType.STRING:
		case PropertyType.PATH:
		case PropertyType.NAME:
		case PropertyType.REFERENCE:
			return value.getString();
		case PropertyType.BINARY:
			Binary binary = null;
			try {
				binary = value.getBinary();
				if (binary!=null){
					return IOUtils.toByteArray(binary.getStream());
				}
				
			} catch (Exception e) {
				throw new CmsException(e);
			}
			finally{
				if (binary!= null){
					binary.dispose();
				}
			}
		default:
			throw new CmsException("Unsupported value type "+ PropertyType.nameFromValue(value.getType()));
		}

	}

	public static  Value[] convertListToValueArray(List values, ValueType valueType, ValueFactory valueFactory) throws RepositoryException {
		if (CollectionUtils.isEmpty(values))
			return getJcrNullForMultiValue();

		boolean atLeastOneValueIsNotNull = false;
		Value[] valueArray = new Value[values.size()];
		
		for (int i=0; i< values.size(); i++){
			valueArray[i] = JcrValueUtils.getJcrValue(values.get(i), valueType, valueFactory);
			
			if (valueArray[i] != null && valueArray[i] != JcrValueUtils.getJcrNull()){
				atLeastOneValueIsNotNull = true;
			}
		}
		
		//All values in array are null. return null
		if (!atLeastOneValueIsNotNull){
			return getJcrNullForMultiValue();
		}

		
		return valueArray;


	}

	public static Value getJcrValue(SimpleCmsProperty simpleProperty, ValueFactory valueFactory) throws RepositoryException 
	{

		ValueType valueType = simpleProperty.getValueType();

		final Object value = simpleProperty.getSimpleTypeValue();

		return getJcrValue(value, valueType, valueFactory);
	}

	public static Value getJcrValue(Object value, ValueType valueType, ValueFactory valueFactory) throws RepositoryException
	{
		if (valueType == null)
			throw new CmsException("Undefined property type ");

		if (value == null)
			return getJcrNull();
		
		//Return null value in case value is a blank String
		if (value instanceof String && StringUtils.isBlank((String)value))
			return getJcrNull();
		
		switch (valueType) {
		case Boolean:
			if (value instanceof String)
				return getJcrBoolean(Boolean.valueOf((String)value), valueFactory);

			return getJcrBoolean((Boolean)value, valueFactory);
		case Double:
			if (value instanceof String)
				return getJcrDouble(Double.valueOf((String)value), valueFactory);

			return getJcrDouble((Double)value, valueFactory);
		case Date:
			if (value instanceof String)
				return getJcrCalendar(DateUtils.fromString((String)value), valueFactory);

			return getJcrCalendar((Calendar)value, valueFactory);
		case Long:
			if (value instanceof String)
				return getJcrLong(Long.valueOf((String)value), valueFactory);

			return getJcrLong((Long)value, valueFactory);
		case String:
		case ObjectReference:
		case TopicReference:
			return getJcrString((String)value, valueFactory);

		default:

			throw new CmsException("Undefined property type "+ valueType);



		}
	}


	private static Value getJcrBoolean(Boolean booleanValue, ValueFactory valueFactory)
	{
		return valueFactory.createValue(booleanValue);
	}

	private static Value getJcrDouble(Double doubleValue, ValueFactory valueFactory)
	{
		return valueFactory.createValue(doubleValue);
	}

	private static Value getJcrLong(Long longValue, ValueFactory valueFactory)
	{
		return valueFactory.createValue(longValue);
	}

	/*
	 * Do not forget to call value.getBinary().dispose() after you have done processing this value
	 */
	public static Value getJcrBinary(byte[] content, ValueFactory valueFactory) throws RepositoryException
	{
		Binary binary = valueFactory.createBinary(new ByteArrayInputStream(content));
		
		return  valueFactory.createValue(binary);
		
	}

	private static Value getJcrString(String stringValue, ValueFactory valueFactory)
	{
		return valueFactory.createValue(stringValue);
	}

	private static Value getJcrCalendar(Calendar date, ValueFactory valueFactory) {
		return valueFactory.createValue(date);
	}

	public static Value getJcrNull()
	{
		return (Value) null;
	}

	public static Value[] getJcrNullForMultiValue()
	{
		//Although it may be better to return an empty array
		//JCR implementation (Jackrabbit) needs 'null' in order
		//to complete nullify value for property.
		return (Value[]) null;
	}
	
	
	public static void replaceValue(Node node, ItemQName property, Value newValue, Value oldValue, Boolean isPropertyMultivalued) throws  RepositoryException
	{
		
		
		//Node does not have property
		if (!node.hasProperty(property.getJcrName()))
		{
			//New Value is null. Do nothing
			if (newValue == null)
				return;
			
			//Determine if property is multiple, if this info is not provided
			if (isPropertyMultivalued == null)
			{
				isPropertyMultivalued = propertyIsMultiValued(node, property);
					
			}
			
			if (isPropertyMultivalued)
				node.setProperty(property.getJcrName(), new Value[]{newValue});
			else
				node.setProperty(property.getJcrName(), newValue);
		}
		else
		{
			//Node has property
			Property jcrProperty = node.getProperty(property.getJcrName());

			if (isPropertyMultivalued == null)
				//Determine by property
				isPropertyMultivalued = jcrProperty.getDefinition().isMultiple();
			
			if (!isPropertyMultivalued){
				if (oldValue == null || (
						oldValue != null && oldValue.equals(jcrProperty.getValue()))
						){
					//Set newValue only if no old value is provided OR
					//oldValue is provided and it really exists there
					jcrProperty.setValue(newValue);
				}
			}
			else
			{
				
				Value[] values = jcrProperty.getValues();

				//Remove oldValue
				if (oldValue != null)
				{
					int index = ArrayUtils.indexOf(values, oldValue);
					if (index == ArrayUtils.INDEX_NOT_FOUND)
						throw new ItemNotFoundException("Value "+ oldValue.getString() + " in property "+ jcrProperty.getPath());
					
					values = (Value[]) ArrayUtils.remove(values, index);
				}
				
				//Add new value
				if (newValue != null &&  ! ArrayUtils.contains(values, newValue))
					values = (Value[]) ArrayUtils.add(values, newValue);
				
				//If at the end values array is empty
				//remove property
				if (ArrayUtils.isEmpty(values)){
					node.setProperty(property.getJcrName(), JcrValueUtils.getJcrNullForMultiValue());
				}
				else{
					node.setProperty(property.getJcrName(), values);
				}
			}
		}
	}

	private static boolean propertyIsMultiValued(Node node, ItemQName property) throws RepositoryException {
		
		
		String propertyName = property.getJcrName();
		
		PropertyDefinition[] propertyDefinitions = node.getDefinition().getDeclaringNodeType().getPropertyDefinitions();
		for (PropertyDefinition propertyDefinition: propertyDefinitions)
		{
			if (propertyDefinition.getName().equals(propertyName))
				return propertyDefinition.isMultiple();
		}
		
		throw new RepositoryException("Unable ot determine if property "+ property.getJcrName() + " in node "+
				node.getPath() + " is multivalue or not");
	}

	/**
	 * Adds a value to a  property.
	 * @param node
	 * @param property
	 * @param newValue
	 * @param isPropertyMultivalued 
	 * @throws RepositoryException
	 */
	public static void addValue(Node node, ItemQName property, Value newValue, Boolean isPropertyMultivalued) throws RepositoryException {

		replaceValue(node, property, newValue, null, isPropertyMultivalued);

	}

	/**
	 * @param node
	 * @param property
	 * @param valueToBeRemoved
	 * @param isPropertyMultivalued 
	 * @throws RepositoryException
	 */
	public static void removeValue(Node node, ItemQName property, Value valueToBeRemoved, Boolean isPropertyMultivalued) throws RepositoryException {
		replaceValue(node, property, null, valueToBeRemoved, isPropertyMultivalued);
	}


}
