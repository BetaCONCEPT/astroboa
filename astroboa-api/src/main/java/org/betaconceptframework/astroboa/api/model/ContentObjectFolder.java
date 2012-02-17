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

/**
 *  Represents a built in way of categorizing content objects of the same type
 *  according to the second of the hour of the date of their creation.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ContentObjectFolder {

	/**
	 * ContentObjectFolder Type.
	 */
	public enum Type {
		/**
		 * Folder of this type contains content objects of the same type.
		 * Its children are of content object folders of type {@link Type#YEAR}.
		 */
		CONTENT_TYPE,
		/**
		 * Folder of this type contains content objects of 
		 * the same type which were created in the same year.
		 * Its children are content object folders of type 
		 * {@link Type#MONTH}.
		 */
		YEAR,
		/**
		 * Folder of this type contains content objects of the 
		 * same type which were created in the same month.
		 * Its children are content object folders of type {@link Type#DAY}.
		 */
		MONTH,
		/**
		 * Folder of this type contains content objects of the same 
		 * type which were created in the same day.
		 * For backward compatibility reasons (Astroboa 2.x.x versions) 
		 * it may contain references to content objects.
		 * 
		 * From 3.x.x version, it may have only children 
		 * of content object folders of type {@link Type#HOUR}.
		 * 
		 */
		DAY,
		/**
		 * Folder of this type contains content objects of the same 
		 * type which were created in the same hour of the day.
		 * Its children are content object folders of type {@link Type#MINUTE}.
		 */
		HOUR,
		/**
		 * Folder of this type contains content objects of the same 
		 * type which were created in the same minute of the day.
		 * Its children are content object folders of type {@link Type#SECOND}.
		 */
		MINUTE,
		/**
		 * Folder of this type contains content objects of the same 
		 * type which were created in the same second of the day.
		 * It contains no children but references to content objects.
		 */
		SECOND;
	}
	
	/**
	 * Set the system name for this folders
	 * For {@link Type#SECOND}, {@link Type#MINUTE}, {@link Type#HOUR}, {@link Type#DAY}, {@link Type#MONTH}, {@link Type#YEAR} 
	 * type folders, name is the number of minute, hour(24), day, month, year respectively.
	 * for {@link Type#CONTENT_TYPE} type folders, name of the type of 
	 * content object.
	 * @param name Content object folder name.
	 */
	void setName(String name);
	/**
	 * Returns the name of folder.
	 * @return Name of the folder.
	 */
	String getName();
	
	/**
	 * Returns a list of {@link ContentObject} ids contained in this folder.
	 * @return A {@link java.util.List} of {@link ContentObject} ids.
	 */
	List<String> getContentObjectIds();
	/**
	 * Sets a list of {@link ContentObject} ids contained in this folder.
	 * @param contentObjects A {@link java.util.List} 
	 * of {@link ContentObject} ids.
	 */
	 void setContentObjectIds(List<String> contentObjects);
	/**
	 * Returns the id of content object folder.
	 * @return A {@link java.lang.String} representing folder's id.
	 */
	String getId();
	/**
	 * Sets the id of content object folder.
	 * @param id A {@link java.lang.String} representing folder's id.
	 */
	void setId(String id);
	/**
	 * Returns sub folders for this content object folder.
	 * @return A {@link java.util.List} of content object folders.
	 */
	List<ContentObjectFolder> getSubFolders();
	/**
	 * Sets sub folders for this content object folder.
	 * @param subFolders  A {@link java.util.List} of content object folders.
	 */
	void setSubFolders(List<ContentObjectFolder> subFolders);
	/**
	 * Returns the type of content object folder.
	 * @return {@link Type}.
	 */
	Type getType();
	/**
	 * Sets the type of content object folder.
	 * @param type {@link Type}.
	 */
	void setType(Type type);
	
	/**
	 * Returns a localized label for this folder.
	 * @return A {@link String} representing localized label for this folder. 
	 */
	String getLocalizedLabelForCurrentLocale();
	/**
	 * Sets a localized label for this folder.
	 * @param label A {@link String} representing 
	 * localized label for this folder.
	 */
	void setLocalizedLabelForCurrentLocale(String label);
	
	/**
	 * Adds a child folder.
	 * @param subFolder ContentObjectFolder child. 
	 */
	void addSubFolder(ContentObjectFolder subFolder);
	
	/**
	 * Adds a {@link ContentObject}'s id to this folder. 
	 * @param contentObjectId A {@link java.lang.String} 
	 * representing {@link ContentObject}'s id.
	 */
	void addContentObjectId(String contentObjectId);
	
	/**
	 * Returns the number of content object folder children.
	 * Calling this method should not trigger any lazy 
	 * loading mechanism, if any. 
	 * 
	 * @return Number of content object folder children.
	 */
	int getNumberOfContentObjects();
	/**
	 * Sets the number of content object folder children.
	 * @param numberOfContentObjects Number of content object folder children.
	 */
	void setNumberOfContentObjects(int numberOfContentObjects);
	
	/**
	 * Returns full path of content object folder. 
	 * 
	 * <p>
	 * Method outcome according to content object folder type
	 * 
	 * <ul>
	 * <li>CONTENT_TYPE : returns type name
	 * <li>YEAR			: YYYY
	 * <li>MONTH		: YYYY/MM
	 * <li>DAY			: YYYY/MM/DD
	 * <li>HOUR			: YYYY/MM/DD/HH/MM
	 * <li>MINUTE		: YYYY/MM/DD/HH
	 * </ul>
	 * </p>
	 * @return Content object folder full path.
	 */
	String getFullPath();
}
