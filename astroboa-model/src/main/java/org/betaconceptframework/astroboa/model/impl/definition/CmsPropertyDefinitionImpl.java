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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.util.PropertyPath;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class CmsPropertyDefinitionImpl extends LocalizableCmsDefinitionImpl implements CmsPropertyDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -596007780862689758L;

	public CmsPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple, boolean mandatory, Integer order,
			String restrictReadToRoles, String restrictWriteToRoles, CmsDefinition parentDefinition) {
		super(qualifiedName, description, displayName);
		
		this.obsolete = obsolete;
		this.multiple = multiple;
		this.mandatory = mandatory;
		this.restrictReadToRoles = restrictReadToRoles;
		this.restrictWriteToRoles = restrictWriteToRoles;
		this.parentDefinition = parentDefinition;
		this.order = order;
	}
	
	private final boolean obsolete;
	
	private final boolean multiple;
	
	private final boolean mandatory;
	
	private final String restrictReadToRoles;

	private final String restrictWriteToRoles;
	
	private final CmsDefinition parentDefinition;

	private String fullPath;
	
	private String path;
	
	private Map<String, String> localizedFullPathPerLocale;
	
	private Integer order;
	
	private String toString;
	
	public CmsDefinition getParentDefinition() {
		return parentDefinition;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public boolean isMultiple() {
		return multiple;
	}


	public boolean isMandatory() {
		return mandatory;
	}

	public String getRestrictReadToRoles() {
		return restrictReadToRoles;
	}

	public String getRestrictWriteToRoles() {
		return restrictWriteToRoles;
	}

	public String getFullPath(){
		if (StringUtils.isNotBlank(fullPath))
			return fullPath;
		
		if (parentDefinition != null){
			String parentFullPath = "";
			if (parentDefinition instanceof CmsPropertyDefinition){
				parentFullPath = ((CmsPropertyDefinition)parentDefinition).getFullPath();
			}
			else{
				//Parent definition is a ContentObjectTypeDefinition
				parentFullPath = parentDefinition.getName();
			}
			
			fullPath =  PropertyPath.createFullPropertyPath(parentFullPath, getName());
		}
		else
			fullPath = getName();

		return fullPath;
	}
	
	public String getPath(){
		
		if (StringUtils.isNotBlank(path)){
			return path;
		}
		
		path = PropertyPath.getPathAfterFirstLevel(getFullPath());
		
		//In case path is null then there is only one level
		//Thus full Path is the same with the path
		if (StringUtils.isBlank(path)){
			path = fullPath;
		}
		
		return path;
	}
	
	
	public String getLocalizedLabelOfPathforLocale(String locale) {
		String localizedLabelForFullPath = getLocalizedLabelOfFullPathforLocale(locale);
		
		String localizedLabelForPath = PropertyPath.getPathAfterFirstLevel(localizedLabelForFullPath);
		
		//In case path is null then there is only one level
		//Thus full Path is the same with the path
		if (StringUtils.isBlank(localizedLabelForPath)){
			return localizedLabelForFullPath;
		}
		
		return localizedLabelForPath;
	}
	
	public String getLocalizedLabelOfFullPathforLocale(String locale) {
		
		if (localizedFullPathPerLocale != null && locale != null && localizedFullPathPerLocale.containsKey(locale))
			return localizedFullPathPerLocale.get(locale);
		
		if (localizedFullPathPerLocale == null){
			localizedFullPathPerLocale = new HashMap<String, String>();
		}
		
		String localizedFullPathForLocale = getDisplayName().getLocalizedLabelForLocale(locale);
		
		if (StringUtils.isBlank(localizedFullPathForLocale))
			localizedFullPathForLocale = getName();
		
		if (parentDefinition != null)
		{
			String parentLocalizedFullPath = "";
			if (parentDefinition instanceof CmsPropertyDefinition)
				parentLocalizedFullPath = ((CmsPropertyDefinition)parentDefinition).getLocalizedLabelOfFullPathforLocale(locale);
			else
				//Parent definition is a ContentObjectTypeDefinition
				parentLocalizedFullPath = ((ContentObjectTypeDefinition)parentDefinition).getDisplayName().getLocalizedLabelForLocale(locale);
		
			localizedFullPathForLocale =  PropertyPath.createFullPropertyPath(parentLocalizedFullPath, localizedFullPathForLocale);
		}
		
		localizedFullPathPerLocale.put(locale, localizedFullPathForLocale);
		
		return localizedFullPathForLocale;
	}

	public Integer getOrder() {
		return order;
	}
	
	public abstract <T extends CmsPropertyDefinition> T clone(ComplexCmsPropertyDefinition parentDefinition);

	@Override
	public String toString() {
		
		if (toString == null){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Definition [");
		
		if (getQualifiedName() != null) {
			builder.append("qualified Name=");
			builder.append(getQualifiedName());
			builder.append(", ");
		}
		if (getDisplayName() != null) {
			builder.append("display name=");
			builder.append(getDisplayName());
			builder.append(", ");
		}

		builder.append("fullPath=")
					.append(getFullPath())
					.append(", valueType=")
					.append(getValueType()) 
					.append(", mandatory=")
					.append(mandatory)
					.append(", multiple=")
					.append(multiple)
					.append(", obsolete=")
					.append(obsolete)
					.append(", order=")
					.append(order)
					.append(", schema url=")
					.append(url(ResourceRepresentationType.XSD))
					.append("]").toString();
		
		toString =  builder.toString();
		}
		
		return toString;
					
	}
	
}
