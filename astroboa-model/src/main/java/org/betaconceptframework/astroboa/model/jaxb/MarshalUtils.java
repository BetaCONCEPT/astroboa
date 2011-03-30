/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.model.jaxb;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class MarshalUtils {

	public static boolean propertyShouldBeMarshalled(List<String> propertyPathsToMarshal, String propertyName, String propertyPath){
		
		if (CollectionUtils.isNotEmpty(propertyPathsToMarshal)){

			if (propertyName == null && propertyPath == null){
				return false;
			}
			
			//Check if property name is provided as is
			if (propertyName != null && propertyPathsToMarshal.contains(propertyName)){
				return true;
			}
			
			//Check if property path is provided as is
			if (propertyPath != null){
				
				if (propertyPathsToMarshal.contains(propertyPath)){
					return true;
				}
				
				//Check if property list contains a path which starts with property's path or property's name
				//For example, list contains path 'profile.created' and property name or path is 'profile'
				for (String propertyPathToBeMarshalled : propertyPathsToMarshal){
						
					//User has requested property comment.comment and property is comment.comment.body
					if(propertyPath.startsWith(propertyPathToBeMarshalled+CmsConstants.PERIOD_DELIM)){
						return true;
					}
						
					//User has requested property profile.title and property is profile
					if (propertyPathToBeMarshalled.startsWith(propertyPath+CmsConstants.PERIOD_DELIM)){
						return true;
					}
				}
			}
			

			return false;
		}
		
		//Should this be enabled by default ?
		/*if (simplePropertyDefinition instanceof StringPropertyDefinition && 
				((StringPropertyDefinition)simplePropertyDefinition).isPasswordType()){
			//Do not marshall password type
			return ;
		}*/


		return true;
	}
}
