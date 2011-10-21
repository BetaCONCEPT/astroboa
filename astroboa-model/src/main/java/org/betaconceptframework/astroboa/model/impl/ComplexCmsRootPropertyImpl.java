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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ContentObjectTypeDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizableCmsDefinitionImpl;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public  class ComplexCmsRootPropertyImpl extends ComplexCmsPropertyImpl<ComplexCmsPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> 
implements ComplexCmsRootProperty,AspectDefinitionManager, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6855093200508354921L;

	private List<String> aspects = new ArrayList<String>();

	private transient Map<String, ComplexCmsPropertyDefinition> aspectDefinitions = new HashMap<String, ComplexCmsPropertyDefinition>();
	
	private String contentObjectSystemName;

	public void loadAspectDefinition(String aspect) throws Exception{
		//Check that aspect has not been defined in content type definition
		if (getPropertyDefinition() == null)
			throw new CmsException("ComplexCmsRootProperty is not attached to any definition");

		//NOTE :In case content object type definition contains a property which refers to this aspect
		//but property's name is different than aspect's one, the check will pass through
		//and aspect will be added to content object
		if (!getPropertyDefinition().hasChildCmsPropertyDefinition(aspect)){

			//Get Aspect definition
			ComplexCmsPropertyDefinition aspectDefinition = null;
			
			LazyLoader lazyLoader = getLazyLoader();
			
			if (lazyLoader !=null){
				lazyLoader.activateClientContextForAuthenticationToken(authenticationToken);
				aspectDefinition = (ComplexCmsPropertyDefinition) lazyLoader.getDefinitionService().getCmsDefinition(aspect, ResourceRepresentationType.DEFINITION_INSTANCE,false);
			}

			if (aspectDefinition == null){
				LoggerFactory.getLogger(getClass()).warn("Definition for aspect "+ aspect + " was not found");
			}
			else{
				//Attach aspect definition to Content Object
				aspectDefinitions.put(aspectDefinition.getName(), aspectDefinition);
				
				if (!aspects.contains(aspectDefinition.getName())){
					aspects.add(aspectDefinition.getName());
				}
			}
		}
	}

	public List<String> getAspects() {
		return Collections.unmodifiableList(aspects);
	}

	public boolean hasAspect(String aspect) {
		return aspects.contains(aspect);
	}

	@Override
	public boolean removeChildProperty(String propertyPath){
		
		final boolean propertyWasRemoved = super.removeChildProperty(propertyPath);
		
		//Check if property is an aspect 
		PropertyPath path = new PropertyPath(propertyPath);

		if (path.getPropertyDescendantPath() == null &&
				path.getPropertyIndex() == PropertyPath.NO_INDEX){
			
			String propertyName = path.getPropertyName();
			
			if (aspects.contains(propertyName)){
				
				//Remove aspect only if property is not there
				if ( cmsPropertyHasBeenLoadedAndRemoved(propertyName) ||
						! isChildPropertyLoaded(propertyName)){
					if (aspectDefinitions.containsKey(propertyName)){
						aspectDefinitions.remove(propertyName);
					}
					
					aspects.remove(propertyName);
				}
			}
		}
		
		
		return propertyWasRemoved;
		
	}


	@Override
	protected CmsPropertyDefinition getChildPropertyDefinition(String childPropertyName){
		if (getPropertyDefinition() != null){

			CmsPropertyDefinition childCmsPropertyDefinition = getPropertyDefinition().getChildCmsPropertyDefinition(childPropertyName);

			if (childCmsPropertyDefinition == null){
				PropertyPath childPropertyPath = new PropertyPath(childPropertyName);

				String firstChildInPathPropertyName = childPropertyPath.getPropertyName();

				String restChildPropertyPath = childPropertyPath.getPropertyDescendantPath();

				//Try Aspects
				if (MapUtils.isNotEmpty(aspectDefinitions) && firstChildInPathPropertyName != null){
					
					ComplexCmsPropertyDefinition firstChildCmsPropertyDefinition = aspectDefinitions.get(firstChildInPathPropertyName);
					
					if (firstChildCmsPropertyDefinition != null && restChildPropertyPath != null){
						//Multi path child property.
						childCmsPropertyDefinition = firstChildCmsPropertyDefinition.getChildCmsPropertyDefinition(restChildPropertyPath); 
					}
					else{
						childCmsPropertyDefinition = firstChildCmsPropertyDefinition;
					}
					
				}

				if (childCmsPropertyDefinition == null && firstChildInPathPropertyName != null){
					
					//Try to load it as an aspect
					try {

						loadAspectDefinition(firstChildInPathPropertyName);
							
						//Re check aspects
						if (MapUtils.isNotEmpty(aspectDefinitions)){
								
							ComplexCmsPropertyDefinition firstChildCmsPropertyDefinition = aspectDefinitions.get(firstChildInPathPropertyName);
							
							if (firstChildCmsPropertyDefinition != null && restChildPropertyPath != null){
								//Multi path child property.
								childCmsPropertyDefinition = firstChildCmsPropertyDefinition.getChildCmsPropertyDefinition(restChildPropertyPath); 
							}
							else{
								childCmsPropertyDefinition = firstChildCmsPropertyDefinition;
							}
							
							if (childCmsPropertyDefinition == null && aspectDefinitions.containsKey(firstChildInPathPropertyName)){
								//Definition was not found. Remove aspect since it is not needed any more
								aspectDefinitions.remove(firstChildInPathPropertyName);
							}
								
						}
							
					} catch (Exception e) {
						//Property is not an aspect
						LoggerFactory.getLogger(getClass()).warn("No definition found for property "+childPropertyName+" in content object "+
								getContentObjectId(), e);
					}
					
					if (childCmsPropertyDefinition == null){
						LoggerFactory.getLogger(getClass()).warn("Found no definition for child property '{}' in property {} in content object {}",
								new Object[]{childPropertyName , this.getFullPath(), getContentObjectId()});
					}
				}
			}

			return childCmsPropertyDefinition;
		}

		throw new CmsException("Property "+ getFullPath() +" is not complex.");
	}

	@Override
	public boolean isChildPropertyDefined(String propertyPath) {
		boolean isPropertyDefined = false;

		isPropertyDefined = super.isChildPropertyDefined(propertyPath);

		if (!isPropertyDefined && MapUtils.isNotEmpty(aspectDefinitions))
		{
			PropertyPath aspectPath = new PropertyPath(propertyPath);

			String aspectName = aspectPath.getPropertyName();

			//Check if there are any aspect definitions
			if (aspectDefinitions.containsKey(aspectName))
			{
				//Aspect exists. Check to see if we need to go any further
				String aspectDescendantPath = aspectPath.getPropertyDescendantPath();
				if (StringUtils.isNotBlank(aspectDescendantPath))
				{
					//Check aspect's children definitions
					return (aspectDefinitions.get(aspectName).hasChildCmsPropertyDefinition(aspectDescendantPath));
				}

				//No aspect children in aspect path. Definition for aspect was requested ad it was found
				return true;

			}
		}

		return isPropertyDefined;

	}

	@Override
	public Map<String, ComplexCmsPropertyDefinition> getAspectDefinitions() {
		return Collections.unmodifiableMap(aspectDefinitions);
	}

	//Override deserialization process to inject 
	//and lazyLoader
	private void readObject(ObjectInputStream ois)
	throws ClassNotFoundException, IOException {

		//Deserialize bean normally
		  ois.defaultReadObject();
		  
		//Inject lazyLoader
		  LazyLoader lazyLoader = getLazyLoader();
		  
		 if (lazyLoader != null){
			 
			 lazyLoader.activateClientContextForAuthenticationToken(authenticationToken);
			 
			 //In this case fullPropertyDefinitionPath is the name of content type
			 ContentObjectTypeDefinition contentTypeDefinition = (ContentObjectTypeDefinition) lazyLoader.getDefinitionService().getCmsDefinition(fullPropertyDefinitionPath, ResourceRepresentationType.DEFINITION_INSTANCE,false);
			 
			 if (contentTypeDefinition != null){
				 setPropertyDefinition(new ComplexCmsPropertyDefinitionImpl(contentTypeDefinition.getQualifiedName(),
						 ((LocalizableCmsDefinitionImpl)contentTypeDefinition).cloneDescription(),
						 ((LocalizableCmsDefinitionImpl)contentTypeDefinition).cloneDisplayName(), false,false,true, null, null, null, null, 
						 ((ContentObjectTypeDefinitionImpl)contentTypeDefinition).getComplexPropertyDefinitionHelper(),
						 ((ContentObjectTypeDefinitionImpl)contentTypeDefinition).getDefinitionFileURI(), 
						 contentTypeDefinition.getPropertyPathsWhoseValuesCanBeUsedAsALabel(), true, 
						 null, contentTypeDefinition.getName(), true));
			 }

			//Load all aspects
			 aspectDefinitions = new HashMap<String, ComplexCmsPropertyDefinition>();
			 
			if (aspects != null){

				for (String aspect : aspects){
					try {
						loadAspectDefinition(aspect);
					} catch (Exception e) {
						throw new IOException(e);
					}
				}
			}
		}
	}

	@Override
	public String getContentObjectId() {
		return getId();
	}
	
	@Override
	public String getContentObjectSystemName() {
		return contentObjectSystemName;
	}

	public void setContentObjectSystemName(String contentObjectSystemName) {
		this.contentObjectSystemName = contentObjectSystemName;
	}
	

	
}
