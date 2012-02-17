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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectFolderImpl implements ContentObjectFolder, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8667706088111519849L;
	
	private Type type;
	private String id;
	private List<ContentObjectFolder> subFolders;
	private List<String> contentObjectIds;
	private int numberOfContentObjects;
	private String label;
	private String fullPath;
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getContentObjectIds() {
		return contentObjectIds;
	}
	public void setContentObjectIds(List<String> contentObjects) {
		this.contentObjectIds = contentObjects;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<ContentObjectFolder> getSubFolders() {
		/*if (!subFoldersAreLoaded()) {
			
			//Continue only if there is an Id
			if (StringUtils.isNotBlank(getId() )) {
				getLazyLoader().lazyLoadSubFolders(this, authenticationToken);
			}
		}*/	
		return subFolders;

	}
	
	public void setSubFolders(List<ContentObjectFolder> subFolders) {
		this.subFolders = subFolders;
		
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public String getLocalizedLabelForCurrentLocale() {
		return label;
	}
	public void setLocalizedLabelForCurrentLocale(String label) {
		this.label = label;
	}
	
	public void addSubFolder(ContentObjectFolder subFolder){
		if (subFolder != null){
			if (subFolders == null){
				subFolders =new ArrayList<ContentObjectFolder>();
			}
			
			subFolders.add(subFolder);
		}
	}
	
	public void addContentObjectId(String contentObjectId)
	{
		if (contentObjectIds  == null)
			contentObjectIds = new ArrayList<String>();
		
		contentObjectIds.add(contentObjectId);
	}
	
	public int getNumberOfContentObjects()
	{
		return numberOfContentObjects;
	}
	public void setNumberOfContentObjects(int numberOfContentObjects) {
		this.numberOfContentObjects = numberOfContentObjects;
	}
	public String getFullPath() {
		return fullPath;
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public boolean subFoldersAreLoaded() {
		
		return subFolders != null;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContentObjectFolderImpl [");
		if (fullPath != null)
			builder.append("fullPath=").append(fullPath).append(", ");
		if (id != null)
			builder.append("id=").append(id).append(", ");
		if (label != null)
			builder.append("label=").append(label).append(", ");
		if (name != null)
			builder.append("name=").append(name).append(", ");
		builder.append("numberOfContentObjects=")
				.append(numberOfContentObjects).append(", ");
		if (type != null)
			builder.append("type=").append(type);
		builder.append("]");
		return builder.toString();
	}
	
	
}
