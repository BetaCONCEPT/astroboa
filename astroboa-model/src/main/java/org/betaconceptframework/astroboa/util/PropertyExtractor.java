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
package org.betaconceptframework.astroboa.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;

/**
 * Class responsible to return a property of an object using the 
 * path provided. 
 * 
 * The path may contain identifiers instead of indices to indicate which of the property to use.
 * 
 *   All the parts of the path represent a complex property except from the last part which
 *   represents a simple property.  In this part, if the property is a multiple value property, 
 *   users have to use indices to specify which value of the 
 *   property to return . In the case of a binary property they may use the identifier of the
 *   binary channel as well.
 * 
 * The path follows the pattern {@link CmsConstants#PROPERTY_PATH_WITH_ID_REG_EXP_FOR_RESTEASY}
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PropertyExtractor {

	private String identifierOfTheValueOfTheProperty = null;
	private int indexOfTheValueOfTheProperty = 0;
	
	private CmsProperty<?,?> property = null;

	public PropertyExtractor(ContentObject contentObject, String propertyPath) throws Exception{

		if (StringUtils.isNotBlank(propertyPath) && contentObject != null){
			
			property = contentObject.getComplexCmsRootProperty();
			
			String[] pathParts = StringUtils.split(propertyPath, CmsConstants.PERIOD_DELIM);
			
			int count = 0;
			
			for (String pathPart : pathParts){
				
				boolean lastPart = ++count == pathParts.length;
				
				String parentPropertyPermanentPath = property.getFullPath();
				
				String childPropertyName = StringUtils.substringBeforeLast(pathPart, CmsConstants.LEFT_BRACKET);

				if (lastPart){
					
					String identifierOrIndexOfTheValue = retrieveIdentifierOrIndexFromPath(pathPart);

					property = ((ComplexCmsProperty)property).getChildProperty(childPropertyName);
					
					if (property == null){
						throw new CmsException("Cannot retrieve property "+pathPart + " from parent property "+ parentPropertyPermanentPath);
					}
					
					if (identifierOrIndexOfTheValue != null){
						if (CmsConstants.UUIDPattern.matcher(identifierOrIndexOfTheValue).matches()){
							identifierOfTheValueOfTheProperty = identifierOrIndexOfTheValue;
						}
						else{
							indexOfTheValueOfTheProperty = Integer.parseInt(identifierOrIndexOfTheValue);
						}
					}
				}
				else{
					//We are in the middle of the path.
					String identifierOfTheChildProperty = retrieveIdentifierOrIndexFromPath(pathPart);

					boolean childPropertyIsDefined = ((ComplexCmsProperty)property).isChildPropertyDefined(childPropertyName);
					
					if (! childPropertyIsDefined){
						throw new Exception("Could not locate definition  for property "+ childPropertyName);
					}
					
					//Retrieve definition
					CmsPropertyDefinition childPropertyDefinition = ((ComplexCmsProperty)property).getPropertyDefinition().getChildCmsPropertyDefinition(childPropertyName);
					
					if (childPropertyDefinition == null){
						//Property has been defined. Since no definition is found check if this property is an aspect
						if (property instanceof ComplexCmsRootProperty && ((ComplexCmsRootProperty)property).hasAspect(childPropertyName)){
							childPropertyDefinition = ((ComplexCmsRootProperty)property).getAspectDefinitions().get(childPropertyName);
						}
					}
					
					if (childPropertyDefinition == null){
						throw new Exception("Could not locate definition  for property "+ childPropertyName);
					}
					
					
					if (childPropertyDefinition.getValueType() == ValueType.Complex){
						//Child property is a multiple value property
						//Iterate through the returned properties to 
						//match the property with the provided identifier if any
						if (childPropertyDefinition.isMultiple()){
							property = retrieveChildComplexCmsPropertyFromAListOfProperties(identifierOfTheChildProperty,childPropertyName);
						}
						else{
							//Child property is a single value property
							CmsProperty<?,?> childProperty = ((ComplexCmsProperty)property).getChildProperty(childPropertyName);
							
							if (identifierOfTheChildProperty != null){
								//User has an identifier. Check that this is valid
								if (StringUtils.equals(identifierOfTheChildProperty, childProperty.getId())){
									property = childProperty;
								}
								else{
									throw new Exception("Property "+childPropertyName + " has been retrieved from parent proeprty "+ parentPropertyPermanentPath + " but its identifier "+
											childProperty.getId() + " does not match with the one provided "+ identifierOfTheChildProperty);
								}
							}
							else{
								property = childProperty;
							}
						}
					}
					else{
						throw new CmsException("Property "+pathPart + " does not correspond to a complex property of the property "+parentPropertyPermanentPath);
					}

				}
			}
			
		}
		
	}

	private CmsProperty<?, ?> retrieveChildComplexCmsPropertyFromAListOfProperties(
			String identifierOfChildProperty, String childPropertyName)
			throws Exception {
		
		List<CmsProperty<?,?>> childProperties = ((ComplexCmsProperty)property).getChildPropertyList(childPropertyName);
		
		if (childProperties == null || childProperties.isEmpty()){
			throw new Exception("Empty proprty list returned");
		}
		
		//No identifier is provided. Return the first from the list
		if (identifierOfChildProperty == null){
			return childProperties.get(0);
		}
		else{
			for (CmsProperty childProperty : childProperties){
				if (StringUtils.equals(identifierOfChildProperty, childProperty.getId())){
					return childProperty;
				}
			}
		}
		
		throw new Exception("Could not locate child property "+childPropertyName + " with identifier "+identifierOfChildProperty + " in parent property "+property.getFullPath());
	}

	private String retrieveIdentifierOrIndexFromPath(String pathPart) {
		if (StringUtils.isBlank(pathPart)){
			return null;
		}
		
		return StringUtils.substringBetween(pathPart, CmsConstants.LEFT_BRACKET, CmsConstants.RIGHT_BRACKET);
	}

	public String getIdentifierOfTheValueOfTheProperty() {
		return identifierOfTheValueOfTheProperty;
	}

	public int getIndexOfTheValueOfTheProperty() {
		return indexOfTheValueOfTheProperty;
	}

	public CmsProperty<?, ?> getProperty() {
		return property;
	}

	
	
}