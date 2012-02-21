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

package org.betaconceptframework.astroboa.util;


import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PropertyPath {

	private String propertyName;
	private int propertyIndex;
	private String propertyDescendantPath;
	private String propertyNameWithIndex;
	private String fullPath;
	
	public final static int NO_INDEX = -1;
	
	public PropertyPath(String propertyPath){
		propertyNameWithIndex = getFirstPath(propertyPath);
		propertyName = StringUtils.substringBefore(propertyNameWithIndex, CmsConstants.LEFT_BRACKET);
		propertyIndex = extractIndexFromPath(propertyNameWithIndex);
		propertyDescendantPath = getPathAfterFirstLevel(propertyPath);
		fullPath = propertyPath;
	}
	
	public String getPropertyNameWithIndex(){
		return propertyNameWithIndex;
	}
	
	public String getPropertyDescendantPath() {
		return propertyDescendantPath;
	}
	
	public int getPropertyIndex() {
		return propertyIndex;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public boolean isInListIndex(int upperBound) {
		
		return (propertyIndex > NO_INDEX && propertyIndex < upperBound);
	}
	public void setPropertyIndex(int propertyIndex) {
		this.propertyIndex = propertyIndex;
	}
	
	
	public static String getPathAfterFirstLevel(String path){
	
		String substringAfter = StringUtils.substringAfter(path, CmsConstants.PERIOD_DELIM);
		
		return (StringUtils.isBlank(substringAfter)? null : substringAfter);
	}
	
	private String getFirstPath(String path){

		String substringBefore = StringUtils.substringBefore(path, CmsConstants.PERIOD_DELIM);

		return (StringUtils.isBlank(substringBefore)? null : substringBefore);
	}
	
	public static String getLastDescendant(String propertyPath) {
		
		String lastDescendant = StringUtils.substringAfterLast(propertyPath, CmsConstants.PERIOD_DELIM);
	
		//If no lastDescendant exists return the same propertyPath
		return (StringUtils.isBlank(lastDescendant) ? propertyPath : lastDescendant);
			
	}
	public static String createFullPropertyPath(String parentPropertyPath, String propertyName){
		return createFullPropertyPathWithDelimiter(parentPropertyPath, propertyName, CmsConstants.PERIOD_DELIM);
	}
	
	public static String createFullPropertyPathWithDelimiter(String parentPropertyPath, String propertyName, String delimiter){
		if (StringUtils.isBlank(delimiter))
			delimiter = CmsConstants.PERIOD_DELIM;
		
		final boolean validParentPath = StringUtils.isNotBlank(parentPropertyPath);
		final boolean validPropertyName = StringUtils.isNotBlank(propertyName);

		if (validParentPath && validPropertyName){
			return parentPropertyPath+ delimiter+propertyName;
		}
		
		if (! validParentPath && validPropertyName){
			return propertyName;
		}
		
		if (validParentPath && !validPropertyName){
			return parentPropertyPath;
		}
		
		return "";	
	}

	
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * Get property path as defined in JCR1.0 API
	 */
	public static String getJcrPropertyPath(String fullPropertyPath){
		if (StringUtils.isBlank(fullPropertyPath))
			return null;
		
		String jcrPath = fullPropertyPath;
		
		if (fullPropertyPath.contains(CmsConstants.PERIOD_DELIM))
			jcrPath = StringUtils.replace(fullPropertyPath, CmsConstants.PERIOD_DELIM,CmsConstants.FORWARD_SLASH);
		
		return jcrPath;
		
	}

	public static String removeIndexesFromPath(String propertyPath){

		if (propertyPath == null){
			return propertyPath;
		}
	
		return propertyPath.replaceAll("\\"+CmsConstants.LEFT_BRACKET+".*\\"+CmsConstants.RIGHT_BRACKET, "");
	}
	
	public static int extractIndexFromPath(String propertyPath) {
		if (StringUtils.isBlank(propertyPath) || ! propertyPath.endsWith(CmsConstants.RIGHT_BRACKET))
			return NO_INDEX;

		try{
			//Get everything after last '['
			String index = StringUtils.substringAfterLast(propertyPath, CmsConstants.LEFT_BRACKET);
			
			//Normally what is left is a number followed by a right bracket
			index = StringUtils.substringBeforeLast(index, CmsConstants.RIGHT_BRACKET);
			
		 if (StringUtils.isBlank(index))
			 return NO_INDEX;
		 
		 	return Integer.parseInt(index);
		}
		catch(Exception e){
			throw new CmsException(e);
		}

	}

	//A property path is recursive is the last two parts are the same
	public static boolean isRecursive(String propertyPath) {
		
		//If property path is null then null is returned
		//according to StringUtils.split documentation
		String[] pathParts = StringUtils.split(propertyPath, CmsConstants.PERIOD_DELIM);
		
		if (pathParts == null || pathParts.length <=1){
			return false;
		}
		
		String lastPart = pathParts[pathParts.length-1];
		String secondLastPart = pathParts[pathParts.length-2];
		
		if (lastPart == null || secondLastPart == null){
			return false;
		}
		
		return lastPart.equals(secondLastPart);
	}

	public static String[] splitPath(String propertyPath){
		if (propertyPath == null){
			return new String[]{};
		}
		
		if (!propertyPath.contains(CmsConstants.PERIOD_DELIM)){
			return new String[]{propertyPath};
		}
		
		return StringUtils.split(propertyPath, CmsConstants.PERIOD_DELIM);
		
	}
	
	public static String removeLastIndexFromPath(String propertyPath) {
		if (StringUtils.isBlank(propertyPath) || ! propertyPath.endsWith(CmsConstants.RIGHT_BRACKET))
			return propertyPath;

		return StringUtils.substringBeforeLast(propertyPath, CmsConstants.LEFT_BRACKET);

	}

}
