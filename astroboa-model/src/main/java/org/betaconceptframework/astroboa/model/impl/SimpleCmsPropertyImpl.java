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

package org.betaconceptframework.astroboa.model.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.api.model.exception.SingleOccurenceException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class SimpleCmsPropertyImpl<T, D extends SimpleCmsPropertyDefinition<T>, P extends ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> 
extends CmsPropertyImpl<D,P> implements SimpleCmsProperty<T, D,P>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2691160236131731929L;
	// Real data for simple value
	protected List<T> values = new ArrayList<T>();
	
	@Override
	public void setPropertyDefinition(D propertyDefinition) {
		super.setPropertyDefinition(propertyDefinition);

		if (this.propertyDefinition != null){

			if (this.propertyDefinition.getValueType() != getValueType())
				throw new CmsException("Incompatible value types. Definition "+ this.propertyDefinition.getValueType() + 
						" , Simple Cms Property : "+ getValueType());
		}

	}

	public void addSimpleTypeValue(T value){


		if (propertyDefinition != null) {
			//In case this property is single value call method setSimpleTypeValue
			if (CollectionUtils.isNotEmpty(values) && isSingle()){
				setSimpleTypeValue(value);

				return;
			}

			//In case value is null value it will not be added
			//in the list. Savvas was adding null values in the list and we should check whether 
			// this is required for some reason
			if (value != null){

				if (values == null){
					values = new ArrayList<T>();
				}

				if	(propertyDefinition.isMultiple()) {
						values.add(value);
				}
				else { // allows only one value
					if (values.size() > 0) {
						values.set(0, value);
					}
					else {
						values.add(value);
					}

				}
			}
		}	
		else {
			throw new CmsException("Definition of property: '" + getFullPath() + "' is null. Value will not be added");
		}

	}


	/**
	 * In cases where this property is an enumeration then all values must
	 * be within the provided value range
	 * @param asList
	 */
	public void checkValues() {
		
		if (CollectionUtils.isNotEmpty(values)){
			if (getPropertyDefinition() == null){
				throw new CmsException("Found no property definition attached to this property "+ getName());
			}

			for (T value : values){
				if (!getPropertyDefinition().isValueValid(value)){
					throw new CmsException(generateMessageForInvalidValue(value));
				}
			}
		}
	}

	abstract protected String generateMessageForInvalidValue(T value); 

	public T getSimpleTypeValue()  throws MultipleOccurenceException{

		throwExceptionIfPropertyIsMultiple();

		if (CollectionUtils.isEmpty(values))
			return null;

		return values.get(0);
	}

	public List<T> getSimpleTypeValues()  throws SingleOccurenceException{

		//Throw an exception only if property is single value and list contains
		//more than one values.
		if (CollectionUtils.isNotEmpty(values) && values.size() > 1)
			throwExceptionIfPropertyIsSingleValue();

		return values;
	}

	/**
	 * Remove value located in index provided
	 * @param index
	 */
	public void removeSimpleTypeValue(int index) throws SingleOccurenceException{
		//Check if property is single value
		//and index is not 0
		if (index != 0)
			throwExceptionIfPropertyIsSingleValue();

		if (values != null){
			values.remove(index);
		}
	}

	/**
	 * Property is considered single valued. In any case 
	 * just update or insert ONLY the first value in list
	 * @param value
	 */
	public void setSimpleTypeValue(T value) throws MultipleOccurenceException{
		throwExceptionIfPropertyIsMultiple();

		
		if (value != null){
			
			if (values == null){
				values = new ArrayList<T>();
			}
			
			if (values.size() > 0){
				values.set(0, value);
			}
			else{
				values.add(value);
			}

		}
		else {
			if (values !=null) {
				values.clear();
			}
		}
	}


	public void setSimpleTypeValues(List<T> values)  throws SingleOccurenceException{

		if (values == null){
			//Throw and catch an exception to log the stack trace in order to 
			//be able to check who provided a null list
			try{
				throw new Exception();
			}
			catch(Exception e){
				final Logger logger = LoggerFactory.getLogger(getClass());
				logger.warn("A null list is provided as values for property {}. An empty list will be created instead "+ getFullPath(), e);
				this.values.clear();
			}
		}
		else{
			this.values = values;
		}
		
		//Check if list contains more than value
		//and this property is single value
		if (CollectionUtils.isNotEmpty(this.values) && this.values.size() > 1)
			throwExceptionIfPropertyIsSingleValue();

	}
	
	public boolean hasNoValues(){
		return values == null || values.isEmpty();
	}
	
	public boolean hasValues(){
		return ! hasNoValues();
	}
	
	
	@Override
	public void removeValues() {
		
		if (values != null){
			values.clear();
		}
		
	}

	//Override deserialization process to inject 
	//property definition
	private void readObject(ObjectInputStream ois)
	    throws ClassNotFoundException, IOException {
		
		//Deserialize bean normally
	  ois.defaultReadObject();
	  
	//Inject Property definition
	  LazyLoader lazyLoader = AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	  
	  if (lazyLoader != null){
		  lazyLoader.activateClientContextForAuthenticationToken(authenticationToken);
		  setPropertyDefinition((D)lazyLoader.getDefinitionService().getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.DEFINITION_INSTANCE,false));
	  }
	}

	@Override
	public boolean swapValues(int from, int to) {
		
		if (values == null){
			return false;
		}
		
		if (from == to || from < 0 || to < 0){
			return false;
		}
		
		try{
			Collections.swap(values, from, to);
			resetPaths();
			return true;
		}
		catch(Exception e){
			//Ignore exception
			return false;
		}
	}
	
	@Override
	public boolean changePositionOfValue(int from, int to) {
		
		if (values == null)
		{
			return false;
		}
		
		if (from == to || from < 0 || to < 0 || from > values.size() -1 || to > values.size())
		{
			return false;
		}
		
		try{
			values.add(to, values.get(from));
			if (from > to) {
				from++;
			}
			values.remove(from);
			resetPaths();
			return true;
		}
		catch(Exception e)
		{
			//Ignore exception
			return false;
		}
	}

	@Override
	public T getFirstValue() {
		if (values == null || values.isEmpty()){
			return null;
		}
		
		return values.get(0);
	}
	
	/**
	 * Useful for retrieving all values, 
	 * regardless of the cardinality of the property.
	 * 
	 * This way you do not have to know apriori property cardinality, 
	 * as in cases of {@link #getSimpleTypeValue()} or {@link #getSimpleTypeValues()}, 
	 * if you do not want an exception to be thrown
	 * 
	 * @return
	 */
	public List<T> getValues(){
		return values;
	}
	
}
