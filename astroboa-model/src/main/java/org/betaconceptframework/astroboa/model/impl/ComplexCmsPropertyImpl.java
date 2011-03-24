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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ComplexCmsPropertyImpl<D extends ComplexCmsPropertyDefinition, P extends ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> 
extends CmsPropertyImpl<D,P> implements ComplexCmsProperty<D,P>, LazyCmsProperty, CmsPropertyIndexable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2226744130618674841L;

	private Map<String, List<CmsProperty<?,?>>> properties;
	private int index;

	 /* When saving content object properties back to JCR repository, all 
	 * properties which contain no values are removed. Due to lazy loading mechanism
	 * properties are loaded only when requested. 
	 * 
	 * Thus, when populating a property to repository and methods getChildProperty or 
	 * getChildProeprtyList return null or empty list we cannot tell whether the specific
	 * property has not been loaded at all or has been loaded and then removed.
	 * 
	 * This list contains complex cms property names which have been loaded and then 
	 * removed.
     * 
	 * 
	 */
	private List<String> cmsPropertyNamesWhichHaveBeenLoadedAndRemoved = new ArrayList<String>();


	//properties used while rendering child properties
	//This is the UUID of the jcr node which represents this complex cms property
	private String propertyContainerNodeUUID;

	//This is the UUID of the jcr node which represents the content object where this property belongs
	private String contentObjectNodeUUID;

	//These are the render properties used to render this property.
	//These settings (if any) will be used to render its children
	private RenderProperties renderProperties;

	public ComplexCmsPropertyImpl() {

		super();

		properties = new TreeMap<String, List<CmsProperty<?,?>>>();

	}

	@Override
	public void setPropertyDefinition(D propertyDefinition) {
		super.setPropertyDefinition(propertyDefinition);

		if (this.propertyDefinition != null){

			if (this.propertyDefinition.getValueType() != ValueType.Complex)
				throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" Incompatible value types. Definition "+ this.propertyDefinition.getValueType() + 
						" , Complex Cms Property : "+ getValueType());
		}

	}

	public void setIndex(int index){
		this.index = index;
		
		resetPaths();
		
	}
	
	/**
	 * Returns the index of this property in its parent list
	 * This is defined during property creation
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	public Map<String, List<CmsProperty<?,?>>> getChildProperties() {

		return properties;
	}

	public List<CmsProperty<?,?>> getChildPropertyList(String propertyPath) 
	{
		if (StringUtils.isBlank(propertyPath))
		{
			return null;
		}

		//Property path should not contain index in the last property
		if (propertyPath.endsWith("]"))
		{
			throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" Property Path "+ propertyPath+" contains index. " +
					"Use ComplexCmsProperty.getChildProperty() or ContentObject.getCmsProperty() instead");
		}
		
		CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(propertyPath);
		
		if (childPropertyDefinition == null)
		{
			return null;
		}
		
		if (childPropertyDefinition.getValueType() != ValueType.Complex || 
				! childPropertyDefinition.isMultiple()){
			throw new CmsException("Property "+propertyPath +" is not a multi value complex property.Use method ComplexCmsProperty.getChildProperty() or " +
					"ContentObject.getCmsProperty() instead");
		}

		Object property = getProperty(propertyPath, true);
		if (property == null)
			return null;
		else
			return (List<CmsProperty<?,?>>) property;
	}

	/**
	 * Return Content Object Property for the specified path. Path has the form
	 * of Profile.subject[1] Where not specified (e.g. Profile) index 0 is
	 * considered
	 * 
	 * @param propertyPath
	 * @return
	 */
	public CmsProperty<?,?> getChildProperty(String propertyPath){

		if (StringUtils.isBlank(propertyPath))
			return null;

		return (CmsProperty<?,?>) getProperty(propertyPath, false);
	}

	public boolean isChildPropertyLoaded(String childPropertyPath) {

		return isChildLoaded(childPropertyPath);

	}

	private boolean isChildLoaded(String childPropertyPath) {

		//Check if any child is loaded
		if (properties == null || MapUtils.isEmpty(properties)){
			return false;
		}
		
		if (childPropertyPath == null){
			return false;
		}

		PropertyPath path = new PropertyPath(childPropertyPath);

		String childPropertyName = path.getPropertyName();
		int childPropertyIndex = path.getPropertyIndex();

		if (!properties.containsKey(childPropertyName)){
			return false;
		}

		List<CmsProperty<?,?>> childProperties = properties.get(childPropertyName);

		//This complex cms property contains property with childPropertyName
		//Check to see if must go any further

		//Go further if there is more in path
		if (path.getPropertyDescendantPath() != null){
			//Get Child Definition
			CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(childPropertyName);

			if (childPropertyDefinition == null || ! (childPropertyDefinition instanceof ComplexCmsPropertyDefinition))
				throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" Child Property "+ childPropertyName + " is not a complex cms property. Unable to check the rest of property path "+ path.getPropertyDescendantPath());

			//If property is multiple but no index is provided then throw an exception
			if   (childPropertyIndex == PropertyPath.NO_INDEX){
				path.setPropertyIndex(0);
			}	

			//Check if index is inside bounds
			if (path.isInListIndex(childProperties.size())){
				//Continue with descendant path
				return ((ComplexCmsProperty<?,?>)childProperties.get(path.getPropertyIndex())).isChildPropertyLoaded(path.getPropertyDescendantPath());
			}
			else {
				return false;
			}
		}
		else{
			if (childProperties.size() == 0){
				//Probably have been loaded but removed
				//In this case we assume that properties are loaded
				//otherwise values will be reloaded
				return true;
			}

			//Reached the end of path. Check for index
			if (childPropertyIndex == PropertyPath.NO_INDEX){
				path.setPropertyIndex(0);
			}

			//Check if index provided is between list bounds
			return path.isInListIndex(childProperties.size());
		}
	}

	public boolean isChildPropertyDefined(String propertyPath) {
		boolean isPropertyDefined = false;

		if (getPropertyDefinition() != null)
			isPropertyDefined = getPropertyDefinition().hasChildCmsPropertyDefinition(propertyPath);

		return isPropertyDefined;

	}


	public ValueType getValueType() {
		return ValueType.Complex;
	}

	private CmsProperty createNewCmsProperty(CmsPropertyDefinition propertyDefinition,String propertyName) {
		CmsProperty newProperty = newCmsProperty(propertyDefinition.getValueType());

		if (newProperty instanceof SimpleCmsProperty)
			((SimpleCmsProperty)newProperty).setPropertyDefinition((SimpleCmsPropertyDefinition)propertyDefinition);
		else if (newProperty instanceof ComplexCmsProperty){
			//In case definition refers to its parent
			//all its children must be initialized
			((ComplexCmsPropertyDefinitionImpl)propertyDefinition).checkIfRecursiveAndCloneParentChildDefinitions();
			((ComplexCmsProperty)newProperty).setPropertyDefinition((ComplexCmsPropertyDefinition)propertyDefinition);
		}

		newProperty.setCurrentLocale(getCurrentLocale());


		return newProperty;
	}

	private CmsProperty<?,?> newCmsProperty(ValueType valueType) {
			
			CmsProperty newProperty = null;
			
			switch (valueType) {
			case Binary:
				newProperty =  new BinaryPropertyImpl();
				break;
			case Boolean:
				newProperty = new BooleanPropertyImpl();
				break;
			case Date:
				newProperty = new CalendarPropertyImpl();
				break;
			case Complex:{
				newProperty = new ComplexCmsPropertyImpl();
				break;
			}
			case ContentObject:
				newProperty = new ContentObjectPropertyImpl();
				break;
			case Double:
				newProperty = new DoublePropertyImpl();
				break;
			case Long:
				newProperty = new LongPropertyImpl();
				break;
			case RepositoryUser:
				newProperty = new RepositoryUserPropertyImpl();
				break;
			case Space:
				newProperty = new SpacePropertyImpl();
				break;
			case String:
				newProperty = new StringPropertyImpl();
				break;
			case Topic:
				newProperty = new TopicPropertyImpl();
				break;

			default:
				return null;
			}
			
			if (newProperty != null){
				((CmsRepositoryEntityImpl)newProperty).setAuthenticationToken(authenticationToken);
			}
			
			return newProperty;
		}
	

	private CmsProperty<?,?> createNewChildCmsPropertyTemplate(String propertyPath, boolean createPropertyOnlyIfComplexAndMultiple){

		try{
			final Logger logger = LoggerFactory.getLogger(getClass());
			
			logger.debug("Creating new property template for property {}", propertyPath);
			
			//Get the first property and its index from property path
			PropertyPath path = new PropertyPath(propertyPath);

			String firstPropertyNameInPath = path.getPropertyName();
			int firstPropertyIndexInPath = path.getPropertyIndex();
			String firstPropertyPathWithIndex = path.getPropertyNameWithIndex();

			String restPropertyPath = path.getPropertyDescendantPath();
			boolean shouldContinue = StringUtils.isNotBlank(restPropertyPath);

			//Obtain first property definition. if none is found then an exception is thrown
			CmsPropertyDefinition firstPropertyDefinition = getChildPropertyDefinition(firstPropertyNameInPath);

			if (firstPropertyDefinition == null){
				logger.debug("Found no definition for property {} therefore property template for property {} can not be created", firstPropertyNameInPath, propertyPath);
				return null;
			}

			//Check if first property is loaded
			if (!properties.containsKey(firstPropertyNameInPath)){

				//First property in path does not exist. Create it only if there is no index defined
				//or index is 0 which refer to the first property
				if (firstPropertyIndexInPath != PropertyPath.NO_INDEX && 
						firstPropertyIndexInPath != 0){
					throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" New property '"+propertyPath +"' could not be created under property '"+this.getFullPath()+
							"'. Intermediate property '"+firstPropertyNameInPath+"' path contains index '"+firstPropertyIndexInPath+"'");
				}
				else{
					//Create new property
					CmsProperty<?,?> newCmsProperty = createNewCmsProperty(firstPropertyDefinition, firstPropertyNameInPath);
					addNewProperty(firstPropertyNameInPath, firstPropertyDefinition, newCmsProperty);
					
					if (shouldContinue)
					{
						logger.debug("Created template for property {} and continue for property {}", firstPropertyNameInPath, restPropertyPath);
						return continueInAddingANewCmsPropertyTemplate(propertyPath, firstPropertyNameInPath, restPropertyPath, newCmsProperty);
					}
					else
					{
						//Reached the end of the path.
						logger.debug("Created template for property {} and reached the end of the path", firstPropertyNameInPath);
						return newCmsProperty;
					}
				}
			}
			else{

				//First property is loaded.

				if (shouldContinue)
				{
				
					logger.debug("Property {} exists. Continue building template for property {}", firstPropertyNameInPath, restPropertyPath);
					
					//Must find the first property and then continue cms property creation
					//under that property
					CmsProperty<?,?> firstProperty = (CmsProperty<?, ?>) getProperty(firstPropertyPathWithIndex, false);

					return continueInAddingANewCmsPropertyTemplate(propertyPath, firstPropertyNameInPath, restPropertyPath, firstProperty);
				}
				else{
					//Should not continue.
					//Create a new template only if definition is a complex definition
					//and a multiple one or property has been loaded , removed and recreated without being saved first 
					//because at this point a cms property already exists
					if (cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.contains(firstPropertyNameInPath) ||
							! createPropertyOnlyIfComplexAndMultiple ||
							(  firstPropertyDefinition instanceof ComplexCmsPropertyDefinition &&
									firstPropertyDefinition.isMultiple() )
							
							)
					{
						
						logger.debug("Property {} is a multiple complex property. A new property instance will be created", firstPropertyNameInPath);
						
						CmsProperty<?,?> newCmsProperty = createNewCmsProperty(firstPropertyDefinition, firstPropertyNameInPath);
						addNewProperty(firstPropertyNameInPath, firstPropertyDefinition, newCmsProperty);
						
						return newCmsProperty;
					}
					else{
						throw new CmsException("Unable to create a new template for property '"+ propertyPath+
								"'. Property is "+ firstPropertyDefinition.getValueType() + 
								(firstPropertyDefinition.isMultiple() ? " multiple ":" single valued")+
						" and already exists.");
					}
				}


			}
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch(Throwable e){
			throw new CmsException(e);
		}
	}

	private CmsProperty<?, ?> continueInAddingANewCmsPropertyTemplate(
			String propertyPath, String firstPropertyNameInPath,
			String restPropertyPath, CmsProperty<?, ?> intermediateCmsProperty) {

		if (! (intermediateCmsProperty instanceof ComplexCmsProperty<?,?>)){
			throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" New property '"+propertyPath +"' could not be created under property '"+this.getFullPath()+
					"'. Intermediate property '"+firstPropertyNameInPath+"' is not a complex cms property");
		}
		else
		{
			return ((ComplexCmsPropertyImpl<?,?>)intermediateCmsProperty).createNewChildCmsPropertyTemplate(restPropertyPath, false);
		}
	}

	public void addNewProperty(String propertyName,
			CmsPropertyDefinition propertyDefinition, CmsProperty newProperty) throws Exception {

		//Add new CmsProperty template
		if (!properties.containsKey(propertyName))
			properties.put(propertyName, new ArrayList<CmsProperty<?,?>>());

		//Check that new property could be created according to its definition
		//Get all properties already created for this definition
		List<CmsProperty<?, ?>> propertiesAlreadyCreated = properties.get(propertyName);

		//Perform check only if there is at least one property
		if (! CollectionUtils.isEmpty(propertiesAlreadyCreated) && ! propertyDefinition.isMultiple()){
			throw new CmsException("ComplexCmsProperty "+this.getFullPath() +" Attempt to create a new property '"+propertyName+"' under complex cms property '"+this.getFullPath()+"'"+
			" but this new property is single value and another property already exists.");
		}

		//Check id done. Add property
		propertiesAlreadyCreated.add(newProperty);

		//Set size in case this is a complex cms property
		if (newProperty instanceof CmsPropertyIndexable){
			//CmsProperty is added always at the end of the list
			((CmsPropertyIndexable)newProperty).setIndex(properties.get(propertyName).size()-1);
		}

		if (newProperty instanceof LazyCmsProperty){
			((LazyCmsProperty) newProperty).setRenderProperties(renderProperties);
			((LazyCmsProperty) newProperty).setContentObjectNodeUUID(contentObjectNodeUUID);

		}

		newProperty.setParent(this);

		
		//Set definition if not already there
		if (newProperty.getPropertyDefinition() == null){
			if (newProperty instanceof SimpleCmsProperty){
				((SimpleCmsProperty)newProperty).setPropertyDefinition((SimpleCmsPropertyDefinition)propertyDefinition);
			}
			else if (newProperty instanceof ComplexCmsProperty){
				//In case definition refers to its parent
				//all its children must be initialized
				((ComplexCmsPropertyDefinitionImpl)propertyDefinition).checkIfRecursiveAndCloneParentChildDefinitions();
				((ComplexCmsProperty)newProperty).setPropertyDefinition((ComplexCmsPropertyDefinition)propertyDefinition);
			}
		}
		
		//Set default value
		/*
		 * Default value is not set at all during read
		 * It is automatically set upon save or update
		 * only when property is mandatory
		 if (	newProperty instanceof SimpleCmsProperty &&
				((SimpleCmsProperty)newProperty).hasNoValues() && 
				newProperty.getPropertyDefinition() != null && 
				newProperty.getPropertyDefinition() instanceof SimpleCmsPropertyDefinition){
			
			Object defaultValue = ((SimpleCmsPropertyDefinition)newProperty.getPropertyDefinition()).getDefaultValue();
			
			if (defaultValue != null){
				((SimpleCmsProperty)newProperty).addSimpleTypeValue(defaultValue);
			}
		 }
		*/
		

		//Notify list
		if (cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.contains(propertyName))
			cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.remove(propertyName);

	}



	protected CmsPropertyDefinition getChildPropertyDefinition(String childPropertyName){
		if (getPropertyDefinition() != null){

			CmsPropertyDefinition childCmsPropertyDefinition = getPropertyDefinition().getChildCmsPropertyDefinition(childPropertyName);

			if (childCmsPropertyDefinition == null){
				LoggerFactory.getLogger(getClass()).warn("Found no definition for child property {} in property {}", childPropertyName, this.getFullPath());
				return null;
			}

			return childCmsPropertyDefinition;
		}

		throw new CmsException("Property "+this.getFullPath() +" is not complex.");
	}

	private synchronized Object getProperty(String propertyPath, boolean asList) {

		try{
			final Logger logger = LoggerFactory.getLogger(getClass());
			
			logger.debug("Requesting property {} {}", propertyPath, asList? " as list ": "");
			
			PropertyPath path = new PropertyPath(propertyPath);

			String childPropertyName = path.getPropertyName();
			
			logger.debug("First property in path {} ", childPropertyName);

			if (childPropertyName == null){
				logger.warn("Returning null as property name was null in provided path {}", propertyPath);
				return null;
			}
			
			boolean propertyHasBeenLoadedAndRemoved = cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.contains(childPropertyName) && 
					( path.getPropertyIndex() == 0 ||  path.getPropertyIndex() == PropertyPath.NO_INDEX) && properties.containsKey(childPropertyName) &&
					CollectionUtils.isEmpty(properties.get(childPropertyName));

			if (properties == null ||  //Map has not been initialized 
					(!properties.containsKey(childPropertyName)) || //Property has not been loaded at all
					propertyHasBeenLoadedAndRemoved // Property has been removed  and user wants to load it again
				){
				
				//In cases where no UUID is provided for this property
				//there is no need to call Lazy Loader as it is equivalent
				//with calling addChildCmsProperty
				if (StringUtils.isBlank(getPropertyContainerUUID())){
					logger.debug("Property {}'s parent {} is a new complex property, therefore there is no need to go to repository to lazy load the former. A" +
							" new property will be created right away.", childPropertyName, getName());
					
					CmsPropertyDefinition childDefinition = getChildPropertyDefinition(childPropertyName);
					
					//Extra check
					if (childDefinition == null){
						logger.error("Found no definition for property {}", PropertyPath.createFullPropertyPath(this.getFullPath(), childPropertyName));
						return null;
					}

					throwExceptionIfPropertyIsComplexAndIndexIsOutOfBounds(
							path, childDefinition);
					
					createNewChildCmsPropertyTemplate(childPropertyName, false);
				}
				
				//This is also the case for removed complex cms properties
				//without save taking place. Since user has removed property but did not save content object
				//and then requested property again, lazy loading must not take place as the 'removed' value
				//will be loaded.
				else if (propertyHasBeenLoadedAndRemoved)
				{
					logger.debug("Property {} has been lazy loaded, has been removed using method ContentObject.removeCmsProperty or " +
									" method ComplexCmsProperty.removeChildProperty, content object was not saved and now user wants it back." +
									"Lazy loading will nto take place but rather a new property instance will be created as it is assumed that user " +
									"removed old instance and wants to create a new one ", childPropertyName);
					
					throwExceptionIfPropertyIsComplexAndIndexIsOutOfBounds(
							path, null);
					
					createNewChildCmsPropertyTemplate(childPropertyName, false);
				}
				else{
					
					LazyLoader lazyLoader = getLazyLoader();
					if (lazyLoader == null){
						logger.error("ComplexCmsProperty "+this.getFullPath() +" Want to render child property " + childPropertyName + " but lazy loader was not found. Parent path "
								+ this.getFullPath()+".");
						return null;
					}
					else{
						
						logger.debug("Lazy loading all instances for property {}", childPropertyName);
						
						List<CmsProperty<?, ?>> childCmsProperties = lazyLoader.loadChildCmsProperty(childPropertyName, getPropertyDefinition().getFullPath(), 
								getPropertyContainerUUID(), getContentObjectNodeUUID(), 
								getRenderProperties(), authenticationToken);

						//Add new property
						if (CollectionUtils.isNotEmpty(childCmsProperties)){

							CmsPropertyDefinition childDefinition = getChildPropertyDefinition(childPropertyName);
							
							//Extra check
							if (childDefinition == null){
								logger.error("Found no definition for property {} although a property has been successfully loaded." +
										" This exception is caused probably when the following case take place : \n" +
										" Property {} is an aspect, values for this aspect have been successfully saved in the past but now for some \n" +
										" reason when content object is rendered, definition for this aspect is not available. Check outcome \n" +
										" of method contentObject.getComplexCmsRootProperty().getAspects() to see if property name {} appears. If not \n" +
										" then there is a bug in the procedure of saving aspect properties of a content object\n" , 
										new Object[]{PropertyPath.createFullPropertyPath(this.getFullPath(), childPropertyName), 
										childPropertyName, childPropertyName});
								
								return null;
							}
							
							for (CmsProperty childProperty : childCmsProperties)
							{
								logger.debug("Registering new property for name {}", childPropertyName);
								
								addNewProperty(childPropertyName, childDefinition , childProperty);
							}
						}
					}
				}
			}

			if (properties.containsKey(childPropertyName)) {

				//Child Property Exists
				int childPropertyIndex = path.getPropertyIndex();

				//Get all child properties under childPropertyName
				List<CmsProperty<?,?>> propertiesMatch = properties.get(childPropertyName);

				final String propertyDescendantPath = path.getPropertyDescendantPath();
				
				//Reached the end of the path ?
				if (propertyDescendantPath == null) {

					//GetChilPropertyList() is called
					if (asList) {

						//No index is provided. Return all properties found for childPropertyName
						if (childPropertyIndex == PropertyPath.NO_INDEX)
						{
							logger.debug("Returning all instances for multiple complex property {}", childPropertyName);
							return propertiesMatch;
						}
						else 
						{
							// PropertyPath contains index (, ..profile[1]) but this is not permitted
							// in getChildPropertyList(). Normally this code won't be reached
							// See check performed in getChildPropertyList()
							throw new CmsException("Found index in property "+ path.getPropertyNameWithIndex()+". Call getChildProperty() instead or getAllChildProperties() but without index in the last property.");
						}

					}
					//GetChildProperty() is called
					else {
						//No index is provided. Default is 0.
						if (childPropertyIndex == PropertyPath.NO_INDEX)
						{
							path.setPropertyIndex(0);
						}

						//Check if index provided is between list bounds
						if (path.isInListIndex(propertiesMatch.size()))
						{
							int index = path.getPropertyIndex();
							
							logger.debug("Returning property {} for index {}", childPropertyName, index);
							
							return propertiesMatch.get(index);
						}
						else
						{
							//Special case.If index is greater than property list size by one then add a new property
							//Method List.size() is not zero based whereas provided index is zero based.
							if ( childPropertyIndex == propertiesMatch.size() )
							{
								//At this point, property has been loaded and user has provided an index
								//which is greater than number of properties loaded, by one.
								//If property is a simple property then user has wrongly provided,
								//for example property 'title' is simple string thus title[1] is not accepted
								//The only accepted case is that property is multiple and complex
								CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(childPropertyName);
								
								if (childPropertyDefinition.isMultiple() && childPropertyDefinition.getValueType() == ValueType.Complex)
								{
									return createNewChildCmsPropertyTemplate(childPropertyName, false);
								}
								else
								{
									throw new CmsException("Property path "+path.getPropertyNameWithIndex() + " " +
											"contains index information which is not accepted because the property is of type " +
											childPropertyDefinition.getValueType()+"and it accepts "+
											(childPropertyDefinition.isMultiple() ? " multiple values" : " only one value") +
											". Try again without the index.");
								}
							}
							else
							{
								throw new CmsException("Cannot get property '" + childPropertyName
									+"'. No such property found at index " + path.getPropertyIndex());
							}
						}
					}
				}
				// We are in the middle of the path
				else {
					//Get Child Definition
					CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(childPropertyName);

					if (childPropertyDefinition == null || ! (childPropertyDefinition instanceof ComplexCmsPropertyDefinition))
					{
						throw new CmsException("Child property " + path.getPropertyName() + " is null or a simple cms property. Unable to continue with path "+ propertyDescendantPath);
					}

					//Child property path has no index
					//Default is 0
					if (childPropertyIndex == PropertyPath.NO_INDEX)
					{
						//Lack of index means that index defaults to 0
						path.setPropertyIndex(0);
					}

					//Check if index is inside bounds
					if (path.isInListIndex(propertiesMatch.size()))
					{
						logger.debug("Continue loading rest of property path {} under property {}", propertyDescendantPath, childPropertyName);
						
						//Continue with descendant path
						return ((ComplexCmsPropertyImpl<?,?>)propertiesMatch.get(path.getPropertyIndex())).getProperty(propertyDescendantPath, asList);

						//if (asList)
						//return ((ComplexCmsProperty<?,?>)propertiesMatch.get(path.getPropertyIndex())).getChildPropertyList(path.getPropertyDescendantPath());
						//else
						//return ((ComplexCmsProperty<?,?>)propertiesMatch.get(path.getPropertyIndex())).getChildProperty(path.getPropertyDescendantPath());

					}
					/* Special case: If index is greater than property list size by one then add a new property*/
					else if (childPropertyIndex == propertiesMatch.size()) {
						childPropertyDefinition = getChildPropertyDefinition(childPropertyName);
						
						if (childPropertyDefinition.isMultiple() && childPropertyDefinition.getValueType() == ValueType.Complex)
						{
							ComplexCmsPropertyImpl<?,?> childProperty = (ComplexCmsPropertyImpl<?,?>)createNewChildCmsPropertyTemplate(childPropertyName, false);
							if (path.getPropertyDescendantPath() != null) {
								return childProperty.getProperty(path.getPropertyDescendantPath(), asList);
							} else {
								return childProperty;
							}
						}
						else
						{
							throw new CmsException("Property path "+path.getPropertyNameWithIndex() + " " +
									"which corresponds to a simple property. In this case index is not allowed.");
						}
					}
					else 
					{
						throw new CmsException("Cannot get property " + propertyPath
								+"'. No property "+ childPropertyName+" found at index " + path.getPropertyIndex());
					}
				}
			}

			return null;
		}
		catch (Throwable t){
			LoggerFactory.getLogger(getClass()).error("", t);
			return null;
		}

	}

	private void throwExceptionIfPropertyIsComplexAndIndexIsOutOfBounds(
			PropertyPath path, CmsPropertyDefinition childDefinition) {
		if (
				(childDefinition == null || childDefinition.getValueType() == ValueType.Complex && childDefinition.isMultiple() )
				&&
				
				path.getPropertyIndex() > 0)
		{
			//Create a new property template only if index is greater than 0 then throw an exception
			throw new CmsException("Cannot create property "+path.getPropertyNameWithIndex()+
					". Property is a multiple complex property and no instance for index "+(path.getPropertyIndex()-1)+" has been created"); 
			
		}
	}

	public boolean removeChildProperty(String propertyPath){


		PropertyPath path = new PropertyPath(propertyPath);

		String childPropertyName = path.getPropertyName();
		int childPropertyIndex = path.getPropertyIndex();

		if (childPropertyName == null){
			return false;
		}
		
		if (properties != null){
				
			if (! properties.containsKey(childPropertyName)) {
				//Property is not loaded.
				CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(propertyPath);
				
				if (childPropertyDefinition == null){
					return false;
				}
				
				//Load property first
				getProperty(propertyPath, childPropertyDefinition.getValueType() == ValueType.Complex && childPropertyDefinition.isMultiple());
				return removeChildProperty(propertyPath);
			}
			else {

				CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(childPropertyName);
	
				if (childPropertyDefinition == null){
					return false;
				}
	
				//All CmsProperty instances found under childPropertyName
				List<CmsProperty<?,?>> propertiesMatch = properties.get(childPropertyName);

				if (path.getPropertyDescendantPath() == null) {
	
					if (childPropertyIndex == PropertyPath.NO_INDEX){
						//Remove all properties but do not remove
						//entry with property name. This way
						//property has been marked for removal
						propertiesMatch.clear();
	
						cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.add(childPropertyName);
						
						return true;
					}
					else if (path.isInListIndex(propertiesMatch.size())){

						if (childPropertyDefinition.getValueType() == ValueType.Complex){
							//ComplexCmsProperty. Remove it from list and force others to renew their index
							boolean successfulRemoval = (propertiesMatch.remove(childPropertyIndex) != null);
							if (successfulRemoval){
								//Must notify all remained CmsProperties in list to change their index
								int i=0;
								for (CmsProperty<?,?> complexCmsProperty : propertiesMatch){
									if (complexCmsProperty instanceof CmsPropertyIndexable){
										((CmsPropertyIndexable)complexCmsProperty).setIndex(i);
									}
									((CmsPropertyImpl<?,?>)complexCmsProperty).resetPaths();
	
									i++;
								}
							}
							return successfulRemoval;
	
						}
						else{
							
							CmsProperty<?, ?> simpleCmsProperty = propertiesMatch.get(childPropertyIndex);
							
							if (simpleCmsProperty != null)
							{
								((SimpleCmsProperty<?,?,?>)simpleCmsProperty).removeValues();
							}
						}
					}
					else {
						throw new CmsException("Cannot remove property " + propertyPath
								+ " No property  " + childPropertyName
								+ " found at given index "
								+ childPropertyIndex);
	
					}
				} else {
					if (childPropertyIndex == PropertyPath.NO_INDEX){
						if (childPropertyDefinition.isMultiple() && childPropertyDefinition instanceof ComplexCmsPropertyDefinition){
							throw new CmsException("Must specifiy index for child property "+ childPropertyName);
						}
	
						//Property is single value. Then set index to 0
						return ((ComplexCmsProperty<?,?>)propertiesMatch.get(0)).removeChildProperty(path.getPropertyDescendantPath());
					}
					else if (path.isInListIndex(propertiesMatch.size()))
						return ((ComplexCmsProperty<?,?>)propertiesMatch.get(childPropertyIndex)).removeChildProperty(path.getPropertyDescendantPath());
					else
						// Do not know where to go
						throw new CmsException("Cannot remove property " + propertyPath
								+"'. No property found at index " + path.getPropertyIndex());
				}
			}
		}

		return false;
	}

	public boolean atLeastOneChilPropertyIsLoaded(String propertyPath) {
		return isChildLoaded(propertyPath);
	}

	public List<CmsProperty<?,?>> getAllChildProperties(String propertyPath) {
		if (StringUtils.isBlank(propertyPath))
			return null;

		Object property = getProperty(propertyPath, true);
		if (property == null)
			return new ArrayList<CmsProperty<?,?>>();
		else
			return (List<CmsProperty<?,?>>) property;

	}

	public boolean cmsPropertyHasBeenLoadedAndRemoved(
			String childPropertyName) {
		return cmsPropertyNamesWhichHaveBeenLoadedAndRemoved != null &&
		cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.contains(childPropertyName);
	}

	public void setContentObjectNodeUUID(String contentObjectNodeUUID) {
		this.contentObjectNodeUUID = contentObjectNodeUUID;

	}

	/*
	 * If not synchronized,  FindBugs create the following bug report
	 * The fields of this class appear to be accessed inconsistently with respect to synchronization.  
	 * This bug report indicates that the bug pattern detector judged that
	 * The class contains a mix of locked and unlocked accesses,
	 * At least one locked access was performed by one of the class's own methods, and
	 * The number of unsynchronized field accesses (reads and writes) was no more than one third of all accesses, with writes being weighed twice as high as reads
	 * A typical bug matching this bug pattern is forgetting to synchronize one of the methods in a class that is intended to be thread-safe.
	 * You can select the nodes labeled "Unsynchronized access" to show the code locations where the detector believed that a field was accessed without synchronization.
	 * Note that there are various sources of inaccuracy in this detector; for example, the detector cannot statically detect all situations in which a lock is held.  Also, even when the detector is accurate in distinguishing locked vs. unlocked accesses, the code in question may still be correct.The fields of this class appear to be accessed inconsistently with respect to synchronization.  This bug report indicates that the bug pattern detector judged that
	 * The class contains a mix of locked and unlocked accesses,
	 * At least one locked access was performed by one of the class's own methods, and
	 * The number of unsynchronized field accesses (reads and writes) was no more than one third of all accesses, with writes being weighed twice as high as reads
	 * A typical bug matching this bug pattern is forgetting to synchronize one of the methods in a class that is intended to be thread-safe.
	 * You can select the nodes labeled "Unsynchronized access" to show the code locations where the detector believed that a field was accessed without synchronization.
	 * Note that there are various sources of inaccuracy in this detector; for example, the detector cannot statically detect all situations in which a lock is held.  Also, even when the detector is accurate in distinguishing locked vs. unlocked accesses, the code in question may still be correct.
	 * 
	 */
	public synchronized void setPropertyContainerNodeUUID(String propertyContainerNodeUUID) {
		this.propertyContainerNodeUUID = propertyContainerNodeUUID;

	}

	public void setRenderProperties(RenderProperties renderProperties) {
		this.renderProperties = renderProperties;

	}

	public String getContentObjectNodeUUID() {
		return contentObjectNodeUUID;
	}

	public synchronized String getPropertyContainerUUID() {
		return propertyContainerNodeUUID;
	}

	public RenderProperties getRenderProperties() {
		return renderProperties;
	}

	//Override deserialization process to inject 
	//and contentService
	private void readObject(ObjectInputStream ois)
	throws ClassNotFoundException, IOException {

		//Deserialize bean normally
		ois.defaultReadObject();

		//Inject lazyLoader
		LazyLoader lazyLoader = getLazyLoader();

		if (lazyLoader != null){

			//In case this is an instance of ComplexCmsRootProperty
			//its definition will be injected when readObject of this class runs
			//right after the end of this method
			if (! (this instanceof ComplexCmsRootProperty)) {
				lazyLoader.activateClientContextForAuthenticationToken(authenticationToken);
				setPropertyDefinition((D)lazyLoader.getDefinitionService().getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.DEFINITION_INSTANCE));
			}
		}
	}

	protected LazyLoader getLazyLoader() {
		return AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	}

	@Override
	public String getPropertyLabel(String locale) {

		try{
			String propertyPathWhoseValueCorrespondsToLabel = getPropertyDefinition().getPropertyPathWhoseValueCanBeUsedAsALabel();

			if (StringUtils.isBlank(propertyPathWhoseValueCorrespondsToLabel)){
				return null;
			}

			if (StringUtils.isBlank(locale)){
				locale = Locale.ENGLISH.toString();
			}

			if (propertyPathWhoseValueCorrespondsToLabel.contains("{locale}")){
				propertyPathWhoseValueCorrespondsToLabel = propertyPathWhoseValueCorrespondsToLabel.replaceAll("\\{locale\\}", locale);
			}

			
			StringBuilder sb = new StringBuilder();
			
			String[] propertyPaths = propertyPathWhoseValueCorrespondsToLabel.split(",");
			
			boolean addComma = false;
			for (String propertyPath : propertyPaths){
			
				String labelForPath = retrieveLabelForPath(locale, propertyPath.trim());
				if (StringUtils.isNotBlank(labelForPath)){
					
					if ( addComma){
						sb.append(" , ");
					}
					else{
						addComma = true;
					}
				
					sb.append(labelForPath);
				}
			}

			return sb.toString();
			
		}catch(Exception e){
			//Since we cannot have logger as Log4j is not serializable
			//we print stack trace to output
			e.printStackTrace();
			return null;
		}
	}

	private String retrieveLabelForPath(String locale,
			String propertyPathWhoseValueCorrespondsToLabel) {
		//Get property
		CmsProperty<?, ?> property = null;
		
		String[] paths = PropertyPath.splitPath(propertyPathWhoseValueCorrespondsToLabel);
		
		//For each of the path provided retrieve child property
		for (String path : paths){
			if (property == null){
				//Make sure that value exists, either inside this instance
				// or in repository. This way no new property will be created
				if (hasValueForChildProperty(path)){
					property = getChildProperty(path);
				}
				else{
					//No point in continue. No value for property
					property = null;
					break;
				}
			}
			else{
				if (property.getValueType() == null){
					LoggerFactory.getLogger(getClass()).warn("Property path "+propertyPathWhoseValueCorrespondsToLabel +" which is responsible to " +
							" provide label is invalid. Reached path "+ property.getFullPath()+ " but cannot continue to path "+path+" as this path corresponds to a " +
									 " property with a null value type");
					property = null;
					break;
				}
				else{
					if (property.getValueType() == ValueType.ContentObject){
						if (((ContentObjectProperty)property).hasNoValues()){
							LoggerFactory.getLogger(getClass()).debug("Property path {} which is responsible to " +
									" provide label is invalid. Reached path {} but cannot continue to path {} as this path corresponds to a " +
											" content object property which has no values", 
											new Object[]{propertyPathWhoseValueCorrespondsToLabel, property.getFullPath(), path});
							return null;
						}
						else{
							property =  ((ContentObjectProperty)property).getFirstValue().getCmsProperty(path);
						}
					}
					else if (property.getValueType() == ValueType.Complex){
						property =  ((ComplexCmsProperty)property).getChildProperty(path);
					}
					else{
						LoggerFactory.getLogger(getClass()).warn("Property path "+propertyPathWhoseValueCorrespondsToLabel +" which is responsible to " +
								" provide label is invalid. Reached path "+ property.getFullPath()+ " but cannot continue to path "+path+" as this path corresponds to a " +
										" property of value type other than ContentType or Complex : "+property.getValueType());
						return null;
					}
				}
			}
		}
		

		if (property == null || property.getValueType() == null || 
				property.getValueType() == ValueType.ContentType ||
				property.getValueType() == ValueType.Complex || 
				((SimpleCmsProperty<?,?,?>)property).hasNoValues()){
			return null;
		}

		Object value = ((SimpleCmsProperty<?, ?, ?>)property).getFirstValue();

		if (value == null){
			return null;
		}
		
		switch (property.getValueType()) {
		case Boolean:
			return ((Boolean)value).toString();

		case Date:
			if (((CalendarProperty)property).getPropertyDefinition() != null && 
					! ((CalendarProperty)property).getPropertyDefinition().isDateTime()){
				return DateUtils.format(((Calendar)value), "dd/MM/yyyy");
			}
			return DateUtils.format(((Calendar)value), "dd/MM/yyyy HH:mm");
			

		case Long:
			return ((Long)value).toString();

		case Double:
			return ((Double)value).toString();

		case String:
			return (String)value;

		case Binary:
			if (((BinaryChannel)value).getSourceFilename() != null){
				return ((BinaryChannel)value).getSourceFilename();
			}
			else{
				return ((BinaryChannel)value).getName();
			}

		case ContentObject:
			if (((ContentObject)value).getComplexCmsRootProperty().isChildPropertyDefined("profile.title")){
				StringProperty profileTitle = (StringProperty)((ContentObject)value).getCmsProperty("profile.title");

				if (profileTitle != null && ! profileTitle.hasNoValues()){
					return profileTitle.getSimpleTypeValue();
				}
			}

			//No profile.title found
			String localizedLabelForLocale = retrieveLocalizedLabelForLocale(locale, ((ContentObject)value).getTypeDefinition().getDisplayName());
			
			if (StringUtils.isEmpty(localizedLabelForLocale)){
				return ((ContentObject)value).getSystemName();
			}
			
			return localizedLabelForLocale;

		case Topic:{
			
			String topicLocalizedLabelForLocale = ((Topic)value).getLocalizedLabelForLocale(locale);
			
			if (StringUtils.isEmpty(topicLocalizedLabelForLocale)){
				return ((Topic)value).getName();
			}
			
			return topicLocalizedLabelForLocale;
		}

		case Space: {
			String spaceLocalizedLabelForLocale = ((Space)value).getLocalizedLabelForLocale(locale);
			
			if (StringUtils.isEmpty(spaceLocalizedLabelForLocale)){
				return ((Space)value).getName();
			}
			
			return spaceLocalizedLabelForLocale;
		}

		case RepositoryUser:
			if (((RepositoryUser)value).getLabel() != null){
				return ((RepositoryUser)value).getLabel();
			}

			return ((RepositoryUser)value).getExternalId();
		default:
			return null;
		}
	}

	private String retrieveLocalizedLabelForLocale(String locale, Localization localization) {
		if (localization == null || ! localization.hasLocalizedLabels()){
			return null;
		}
		
		String localizedLabelForLocale = localization.getLocalizedLabelForLocale(locale);
		
		if (StringUtils.isBlank(localizedLabelForLocale) && ! StringUtils.equals(Locale.ENGLISH.toString(), locale)){
			//Fall back for English
			localizedLabelForLocale = localization.getLocalizedLabelForLocale(Locale.ENGLISH.toString());
		}
		
		return localizedLabelForLocale;
	}


	public void clearCmsPropertyNameWhichHaveBeenLoadedAndRemovedList(){
		
		//Clear current list
		if (CollectionUtils.isNotEmpty(cmsPropertyNamesWhichHaveBeenLoadedAndRemoved))
		{
			//All clear map with values
			for (String cmsPropertyNameWhichHaveBeenLoadedAndRemoved : cmsPropertyNamesWhichHaveBeenLoadedAndRemoved)
			{
				if (cmsPropertyNameWhichHaveBeenLoadedAndRemoved == null)
				{
					continue;
				}
				
				if (properties.containsKey(cmsPropertyNameWhichHaveBeenLoadedAndRemoved) && 
						CollectionUtils.isEmpty(properties.get(cmsPropertyNameWhichHaveBeenLoadedAndRemoved)))
				{
					properties.remove(cmsPropertyNameWhichHaveBeenLoadedAndRemoved);
				}
			}
			
			cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.clear();
		}
		
		//Clear list for all loaded children
		for (List<CmsProperty<?,?>> childCmsProperties : properties.values())
		{
			for (CmsProperty<?,?> childProperty : childCmsProperties)
			{
				if (childProperty instanceof ComplexCmsPropertyImpl)
				{
					((ComplexCmsPropertyImpl<?,?>)childProperty).clearCmsPropertyNameWhichHaveBeenLoadedAndRemovedList();
				}
				else
				{
					break;
				}
			}
		}
		
	}

	@Override
	public CmsProperty<?, ?> createNewValueForMulitpleComplexCmsProperty(String relativePath) {

		//Other values of complex property already exist
		if (isChildLoaded(relativePath))
		{
			//Retrieve list with existed complex properties for provided path
			List<CmsProperty<?, ?>> propertyList = getChildPropertyList(relativePath);
			
			if (propertyList == null)
			{
				//Something went wrong and list could not be initialized
				return null;
			}
			else
			{
				//Size of list is not zero based
				int propertyIndex = propertyList.size();
				
				return getChildProperty(relativePath+"["+propertyIndex+"]");

			}
		}
		else
		{
			//Property has not been loaded. Calling getChildPropertyList will
			//cause a new complex property to be created if none is found in the 
			//repository
			List<CmsProperty<?, ?>> propertyList = getChildPropertyList(relativePath);
			
			if (propertyList == null)
			{
				//Something went wrong and list could not be initialized
				return null;
			}
			else
			{
				if (propertyList.size() == 1)
				{
					//There is one complex property in the list. If it has no Id
					//then it has just been created
					if (propertyList.get(0).getId() == null)
					{
						//Return newly created complex property
						return propertyList.get(0);
					}
				}
				
				return getChildProperty(relativePath+"["+propertyList.size()+"]");
				
			}
		}
	}

	@Override
	public boolean swapChildPropertyValues(String relativePropertyPath,
			int from, int to) {
		
		if (relativePropertyPath == null || relativePropertyPath.endsWith(CmsConstants.RIGHT_BRACKET)){
			return false;
		}
		
		if (from == to || from < 0 || to < 0){
			return false;
		}
		
		//Check its definition
		CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(relativePropertyPath);
		
		if (childPropertyDefinition == null || ! childPropertyDefinition.isMultiple()){
			return false;
		}
		
		if (ValueType.Complex == childPropertyDefinition.getValueType()){
			List<CmsProperty<?, ?>> propertyList = getChildPropertyList(relativePropertyPath);
			
			try{
				Collections.swap(propertyList, from, to);
				//Now fix indices
				for (CmsProperty<?,?> childProperty : propertyList)	{
					if (childProperty instanceof CmsPropertyIndexable){
						((CmsPropertyIndexable)childProperty).setIndex(propertyList.indexOf(childProperty));
					}
				}
				
				return true;
			}
			catch(Exception e){
				LoggerFactory.getLogger(getClass()).error("",e);
				return false;
			}
		}
		else{
			//Property is simple value
			CmsProperty<?, ?> property = getChildProperty(relativePropertyPath);
			
			if (property instanceof SimpleCmsProperty){
				return ((SimpleCmsProperty)property).swapValues(from,to);
			}
			else{
				LoggerFactory.getLogger(getClass()).warn("Property {}'s definition is value type {} but property instance is of type {}",
						new Object[]{relativePropertyPath, childPropertyDefinition.getValueType(), property.getClass().getName()});
				return false;
			}
		}
	}
	
	@Override
	public boolean changePositionOfChildPropertyValue(String relativePropertyPath, int from, int to) {
		
		if (relativePropertyPath == null || relativePropertyPath.endsWith(CmsConstants.RIGHT_BRACKET))
		{
			return false;
		}
		
		if (from == to || from < 0 || to < 0)
		{
			return false;
		}
		
		//Check its definition
		CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(relativePropertyPath);
		
		if (childPropertyDefinition == null || ! childPropertyDefinition.isMultiple())
		{
			return false;
		}
		
		if (ValueType.Complex == childPropertyDefinition.getValueType())
		{
			List<CmsProperty<?, ?>> propertyList = getChildPropertyList(relativePropertyPath);
			
			if (from > propertyList.size() -1 || to > propertyList.size()) {
				return false;
			}
			
			try{
				CmsProperty<?,?> propertyToMove = propertyList.get(from);
				propertyList.add(to, propertyToMove);
				if (from > to) {
					from++;
				}
				propertyList.remove(from);
				
				//Now fix indices
				for (CmsProperty<?,?> childProperty : propertyList)
				{
					if (childProperty instanceof CmsPropertyIndexable)
					{
						((CmsPropertyIndexable)childProperty).setIndex(propertyList.indexOf(childProperty));
					}
				}
				
				return true;
			}
			catch(Exception e)
			{
				LoggerFactory.getLogger(getClass()).error("",e);
				return false;
			}
		}
		else
		{
			//Property is simple value
			CmsProperty<?, ?> property = getChildProperty(relativePropertyPath);
			
			if (property instanceof SimpleCmsProperty)
			{
				return ((SimpleCmsProperty<?,?,?>)property).changePositionOfValue(from,to);
			}
			else
			{
				LoggerFactory.getLogger(getClass()).warn("Property {}'s definition is value type {} but property instance is of type {}",
						new Object[]{relativePropertyPath, childPropertyDefinition.getValueType(), property.getClass().getName()});
				return false;
			}
		}
	}

	/**
	 * 
	 */
	public void clean() {
		

		if (properties != null){
			for (List<CmsProperty<?,?>> propList : properties.values()){
				
				for (CmsProperty<?,?> property : propList){
					((CmsPropertyImpl)property).clean();
				}
			}
		}
		cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.clear();
		
		setContentObjectNodeUUID(null);
		setPropertyContainerNodeUUID(null);
		
		setId(null);
		
	}
	
	
	public boolean hasValueForChildProperty(String propertyPath) {

		try{
			final Logger logger = LoggerFactory.getLogger(getClass());
			
			logger.debug("Checking existance for value of property {} ", propertyPath);
			
			PropertyPath path = new PropertyPath(propertyPath);

			String childPropertyName = path.getPropertyName();
			
			logger.debug("First property in path {} ", childPropertyName);

			if (childPropertyName == null){
				logger.warn("Returning null as property name was null in provided path {}", propertyPath);
				return false;
			}
			
			boolean propertyHasBeenLoadedAndRemoved = cmsPropertyNamesWhichHaveBeenLoadedAndRemoved.contains(childPropertyName) && 
					( path.getPropertyIndex() == 0 ||  path.getPropertyIndex() == PropertyPath.NO_INDEX) && properties.containsKey(childPropertyName) &&
					CollectionUtils.isEmpty(properties.get(childPropertyName));

			if (properties == null ||  //Map has not been initialized 
					(!properties.containsKey(childPropertyName)) || //Property has not been loaded at all
					propertyHasBeenLoadedAndRemoved // Property has been removed  and user wants to load it again
				){
				
				//In cases where no UUID is provided for this property
				//there is no need to call Lazy Loader as it is equivalent
				//with calling addChildCmsProperty
				if (StringUtils.isBlank(getPropertyContainerUUID())){
					logger.debug("Property {}'s parent {} is a new complex property, therefore there is no need to go to repository to lazy load the former." +
							"No value exists for that property", childPropertyName, getName());
					return false;
				}
				
				//This is also the case for removed complex cms properties
				//without save taking place. Since user has removed property but did not save content object
				//and then checks for property value existence, lazy loading must not take place as the 'removed' value
				//will be loaded.
				else if (propertyHasBeenLoadedAndRemoved){
					logger.debug("Property {} has been lazy loaded, has been removed using method ContentObject.removeCmsProperty or " +
									" method ComplexCmsProperty.removeChildProperty, content object was not saved and now user checks for value existence." +
									"Lazy loading will nto take place and false is returned ", childPropertyName);
					return false;
				}
				else{
					
					LazyLoader lazyLoader = getLazyLoader();
					if (lazyLoader == null){
						logger.error("ComplexCmsProperty "+this.getFullPath() +" Want to check for child property " + childPropertyName + " but lazy loader was not found. Parent path "
								+ this.getFullPath()+".");
						return false;
					}
					else{
						
						logger.debug("Check for property {}", childPropertyName);

						return lazyLoader.hasValueForProperty(propertyPath,  getPropertyContainerUUID(), authenticationToken);
					}
				}
			}

			if (properties.containsKey(childPropertyName)) {

				//Child Property Exists
				int childPropertyIndex = path.getPropertyIndex();

				//Get all child properties under childPropertyName
				List<CmsProperty<?,?>> propertiesMatch = properties.get(childPropertyName);

				final String propertyDescendantPath = path.getPropertyDescendantPath();
				
				//Reached the end of the path ?
				if (propertyDescendantPath == null) {

					//No index is provided. Default is 0.
					if (childPropertyIndex == PropertyPath.NO_INDEX){
						path.setPropertyIndex(0);
					}

					//Check if index provided is between list bounds
					if (path.isInListIndex(propertiesMatch.size())){
						
						int index = path.getPropertyIndex();
						
						CmsProperty<?, ?> property = propertiesMatch.get(index);
						
						if (property!=null){
							
							logger.debug("Property {} for index {} exists.Examine if value exists", childPropertyName, index);
							
							if (property.getValueType() == ValueType.Complex){
								return true;
							}
							else{
								if (property instanceof SimpleCmsProperty){
									return ((SimpleCmsProperty)property).hasValues();
								}
								else{
									logger.warn("Property {} for index {} is of type {} which does not extend SimpleCmsProperty",
											new Object[]{childPropertyName, index, property.getValueType()});
									return false;
								}
							}
						}
						else{
							logger.warn("Property {} for index {} has a null value. A CmsProperty instance should exist instead.", childPropertyName, path.getPropertyIndex());
							return false;
						}
					}
					else{
						return false;
					}
				}
				// We are in the middle of the path
				else {
					//Get Child Definition
					CmsPropertyDefinition childPropertyDefinition = getChildPropertyDefinition(childPropertyName);

					if (childPropertyDefinition == null || ! (childPropertyDefinition instanceof ComplexCmsPropertyDefinition)){
						throw new CmsException("Child property " + path.getPropertyName() + " is null or a simple cms property. Unable to continue with path "+ propertyDescendantPath);
					}

					//Child property path has no index
					//Default is 0
					if (childPropertyIndex == PropertyPath.NO_INDEX){
						//Lack of index means that index defaults to 0
						path.setPropertyIndex(0);
					}

					//Check if index is inside bounds
					if (path.isInListIndex(propertiesMatch.size())){
						logger.debug("Continue chekcing value existence for the rest of property path {} under property {}", propertyDescendantPath, childPropertyName);
						
						//Continue with descendant path
						return ((ComplexCmsPropertyImpl<?,?>)propertiesMatch.get(path.getPropertyIndex())).hasValueForChildProperty(propertyDescendantPath);
					}
					else{
						return false;
					}
				}
			}

			return false;
		}
		catch (Throwable t){
			LoggerFactory.getLogger(getClass()).error("", t);
			return false;
		}

	}

	@Override
	public void resetPaths() {

		super.resetPaths();
		
		if (properties != null && ! properties.isEmpty()){
			for (List<CmsProperty<?,?>> props : properties.values()){
				
				for (CmsProperty<?,?> prop : props){
					((CmsPropertyImpl)prop).resetPaths();
				}
			}
		}

	}
	
	
}
