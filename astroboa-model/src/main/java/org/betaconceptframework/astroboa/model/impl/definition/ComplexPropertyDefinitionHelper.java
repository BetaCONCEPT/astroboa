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

package org.betaconceptframework.astroboa.model.impl.definition;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.commons.comparator.CmsPropertyDefinitionComparator;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.LoggerFactory;

/**
 * Helper class containing methods for manipulating child property definitions.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class ComplexPropertyDefinitionHelper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8422403918174942289L;

	private Map<String, CmsPropertyDefinition> childPropertyDefinitions;
	
	//First Key is locale
	//Second key is whether sorting is done with ValueType and locale (true) or only with locale (false)
	//Third key is cms property name
	private Map<String, Map<Boolean, Map<String, CmsPropertyDefinition>>> sortedPropertyDefinitions;
	
	private CmsPropertyDefinitionComparator<CmsPropertyDefinition> cmsPropertyDefinitionComparator;
	
	//The number of levels of the hierarchy definition
	//Zero based . 0 denotes that all child definitions are simple
	private int depth = -1;

	public Map<String, CmsPropertyDefinition> getChildPropertyDefinitions() {
		return childPropertyDefinitions;
	}

	public void setChildPropertyDefinitions(
			Map<String, CmsPropertyDefinition> propertyDefinitions) {
		//We must have a LinkedHashMap to ensure insertion order.
		//So if parameter is a HashMap, create a new LinkedHashMap
		//and copy all entries
		if (propertyDefinitions != null && propertyDefinitions.getClass() == HashMap.class){
			this.childPropertyDefinitions = new LinkedHashMap<String, CmsPropertyDefinition>();
			this.childPropertyDefinitions.putAll(propertyDefinitions);
		}
		else{
			this.childPropertyDefinitions = propertyDefinitions;
		}
		
		if (sortedPropertyDefinitions != null){
			sortedPropertyDefinitions.clear();
		}
		
		resetDepth();
	}

	public void resetDepth() {
		depth = -1;
	}

	public boolean hasPropertyDefinitions(){
		return MapUtils.isNotEmpty(childPropertyDefinitions); 
	}


	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(
			String locale) {
		return sortPropertyDefinitions(locale, true);
	}
	
	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(String locale) {
		
		
		return sortPropertyDefinitions(locale, false);
	}

	/**
	 * Sorts definitions according to locale. Sorting is done through the following procedure :
	 * 
	 * We need to sort map's values and NOT map's keys. Since TreeMap accepts a comparator
	 * for its key ONLY, map's entries are put inside a List which is sorted with a Comparator
	 * over map's entries. 
	 * Finally a new tree map is populated using sorted entries in the list. In order to
	 * keep the order in the list, TreeMap must point to a dummy comparator which will do nothing, 
	 * otherwise while populating TreeMap entries will be sorted again by their keys.
	 * @param locale
	 * @return
	 */
	private Map<String, CmsPropertyDefinition> sortPropertyDefinitions(String locale, boolean compareByValueType) {
		
		if (sortedPropertyDefinitions == null)
			sortedPropertyDefinitions = new TreeMap<String, Map<Boolean, Map<String,CmsPropertyDefinition>>>();
		
		//If no locale is specified return unsorted property definitions
		if (StringUtils.isBlank(locale))
			return childPropertyDefinitions;
		
		if (sortedPropertyDefinitions.containsKey(locale)){
			
			//Definitions have already been sorted with the specified locale.
			//Check compare with value type
			Map<Boolean, Map<String, CmsPropertyDefinition>> sortedDefinitionsForLocale = sortedPropertyDefinitions.get(locale);
			if (sortedDefinitionsForLocale.containsKey(compareByValueType)){
				return sortedDefinitionsForLocale.get(compareByValueType);
			}
		}
		else{
			
			//Create one entry for locale
			sortedPropertyDefinitions.put(locale, new TreeMap<Boolean, Map<String,CmsPropertyDefinition>>());
		}
		
		//PropertyDefinitions have not been sorted with the specified locale
		if (cmsPropertyDefinitionComparator == null)
			cmsPropertyDefinitionComparator = new CmsPropertyDefinitionComparator<CmsPropertyDefinition>();
		
		cmsPropertyDefinitionComparator.setLocale(locale);
		cmsPropertyDefinitionComparator.setCompareByValueType(compareByValueType);
		
		//Create a list of all entries
		List<CmsPropertyDefinition> listOfDefinitionEntries = new ArrayList<CmsPropertyDefinition>();
		listOfDefinitionEntries.addAll(childPropertyDefinitions.values());
		
		//Sort entries
		Collections.sort(listOfDefinitionEntries, cmsPropertyDefinitionComparator);
		
		//Create a new TreeMap with dummy Comparator
		Map<String, CmsPropertyDefinition> newSortedMap = new TreeMap<String, CmsPropertyDefinition>(new DummyComparator());
		
		//Populate TreeMap
		for (CmsPropertyDefinition sortedDefinition : listOfDefinitionEntries)
			newSortedMap.put(sortedDefinition.getName(), sortedDefinition);
		
		//Cache sorted definitions per locale
		sortedPropertyDefinitions.get(locale).put(compareByValueType, newSortedMap);

		return newSortedMap;
	}

	public CmsPropertyDefinition getChildCmsPropertyDefinition(String childPropertyPath) {
		
		if (hasPropertyDefinitions()){
			PropertyPath path = new PropertyPath(childPropertyPath);

			String childPropertyName = path.getPropertyName();
			
			CmsPropertyDefinition propertyDefinition = childPropertyDefinitions.get(childPropertyName);
			
			//No definition found
			if (propertyDefinition == null){
				return null;
			}
			
			//Definition found. Check to see if path has descendants
			if (path.getPropertyDescendantPath() != null){
				if (!(propertyDefinition instanceof ComplexCmsPropertyDefinition)){
					LoggerFactory.getLogger(getClass()).warn("Definition {} is not a complex one and therefore cannot descend further more to path {}", propertyDefinition.getFullPath(), path.getPropertyDescendantPath());
					return null;
				}
				return ((ComplexCmsPropertyDefinition)propertyDefinition).getChildCmsPropertyDefinition(path.getPropertyDescendantPath());
			}

			return propertyDefinition;
		}
		else{
			return null;
		}
	}

	public boolean hasChildCmsPropertyDefinition(String childPropertyPath) {
		
		if (hasPropertyDefinitions()){
			PropertyPath path = new PropertyPath(childPropertyPath);

			String childPropertyName = path.getPropertyName();
			
			CmsPropertyDefinition propertyDefinition = childPropertyDefinitions.get(childPropertyName);
			
			//No definition found
			if (propertyDefinition == null){
				return false;
			}
			
			//Definition found. Check to see if path has descendants
			if (path.getPropertyDescendantPath() != null){
				if (!(propertyDefinition instanceof ComplexCmsPropertyDefinition)){
					LoggerFactory.getLogger(getClass()).warn("Definition {} is not a complex one and therefore cannot descend further more to path {}", propertyDefinition.getFullPath(), path.getPropertyDescendantPath());
					return false;
				}
				
				return ((ComplexCmsPropertyDefinition)propertyDefinition).hasChildCmsPropertyDefinition(path.getPropertyDescendantPath());
			}

			return true;
		}
		else{
			return false;
		}
	}

	
	public int getDepth(){
		if (depth == -1){
			
			if (childPropertyDefinitions != null){
				if (childPropertyDefinitions.isEmpty()){
					depth = 0;
				}
				else{
					int maxChildDepth = -1;
					
					boolean foundBinaryProperty = false; 
					
					for (CmsPropertyDefinition cmsPropertyDefinition: childPropertyDefinitions.values()){
						
						if (cmsPropertyDefinition.getValueType() == ValueType.Complex){
							int depth = ((ComplexCmsPropertyDefinition)cmsPropertyDefinition).getDepth();
							
							if (depth > maxChildDepth){
								maxChildDepth = depth;
							}
						}
						else if (cmsPropertyDefinition.getValueType() == ValueType.Binary){
							foundBinaryProperty = true; //A Binary property corresponds to a Jcr node therefore it is calculated to the depth 
						}
					}
					
					if (maxChildDepth > -1){
						//There is at least one child complex property.
						//Thus current depth is the max child depth plus one.
						depth = maxChildDepth+1;
					}
					else{
						//There are no child complex properties at all
						if (foundBinaryProperty){
							//However there is at least one binary property
							//and therefore depth must be at least 1
							depth = 1;
						}
						else{
							depth = 0;
						}
					}
					
				}
			}
			else{
				depth = 0;
			}
		}
		
		
		return depth;
	}
	
	/*
	 * This class is an inner class, but does not use its embedded reference to the object which created it.  
	 * This reference makes the instances of the class larger, and may keep the reference to the creator object 
	 * alive longer than necessary.  If possible, the class should be made static.
	 */
	private static class DummyComparator implements Comparator<String>, Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 8106517630829708464L;

		public int compare(String o1, String o2) {
			return 1; // Returning 1 will put all entries the way they are entered
		}
		
	}



}
