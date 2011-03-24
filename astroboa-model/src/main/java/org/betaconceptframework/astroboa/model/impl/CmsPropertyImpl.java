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


import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.api.model.exception.SingleOccurenceException;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class CmsPropertyImpl<D extends CmsPropertyDefinition, P extends ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> extends LocalizableEntityImpl implements CmsProperty<D,P>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2241602551059154622L;

	protected String name;
	
	private P parentProperty;
	
	private String fullPath;
	private String path;
	private String fullPermanentPath;
	private String permanentPath;
	
	
	//It is used to keep the full definition path of this property so that
	// its property definition can be restored during deserialization
	//The value on this variable is set in setPropertyDefinition method
	protected String fullPropertyDefinitionPath;
	
	protected transient D propertyDefinition;
	

	public D getPropertyDefinition() {
		return propertyDefinition;
	}

	public void setPropertyDefinition(D propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
		
		if (this.propertyDefinition != null){
		
			name = this.propertyDefinition.getName();
			
			fullPropertyDefinitionPath = this.propertyDefinition.getFullPath();
			
		}
		else{
			name = null;
			fullPropertyDefinitionPath = null;
		}
		
	}

	private String localizedFullPath;
	
	protected void throwExceptionIfPropertyIsMultiple() {
		if (isMultiple())
			throw new MultipleOccurenceException(getFullPath());
	}

	protected void throwExceptionIfPropertyIsSingleValue() {
		if (!isMultiple())
			throw new SingleOccurenceException(getFullPath());
	}
	
	protected boolean isMultiple() {
		return (propertyDefinition == null)? false: propertyDefinition.isMultiple();
	}
	
	protected boolean isSingle() {
		return ! isMultiple();
	}


	public String getName() {
		return name;
	}

	public P getParentProperty() {
		return parentProperty;
	}

	public void setParent(P parent) {
		this.parentProperty = parent;
		
		resetPaths();
	}


	/**
	 * Return full path for  this property including any index numbers
	 * @return
	 */
	public String getFullPath()
	{
		if (StringUtils.isNotBlank(fullPath))
			return fullPath;
		
		if (parentProperty==null) 
			fullPath = name;
		else
		{
			int indexForCmsProperty = 0;
			//Only ComplexCmsProperties can have index
			if (this instanceof CmsPropertyIndexable)
				indexForCmsProperty = ((CmsPropertyIndexable)this).getIndex();
			
			String parentFullPath = parentProperty.getFullPath();
			fullPath = PropertyPath.createFullPropertyPath(parentFullPath, name);
			
			if (indexForCmsProperty > 0 )
			{
				fullPath += CmsConstants.LEFT_BRACKET + indexForCmsProperty + CmsConstants.RIGHT_BRACKET;
			}
			
		}

		return fullPath;
	}
	
	public String getPath(){
		
		if (StringUtils.isNotBlank(path))
			return path;
		
		path = PropertyPath.getPathAfterFirstLevel(getFullPath());
		
		return path;
	}
	
	public String getLocalizedLabelOfFullPathforLocaleWithDelimiter(String locale,
			String delimiter) {
		if (StringUtils.isNotBlank(localizedFullPath))
			return localizedFullPath;
		
		localizedFullPath = getLocalizedLabelForLocale(locale);
		
		if (StringUtils.isBlank(localizedFullPath))
			localizedFullPath = name;
		
		if (parentProperty != null){
			String parentLocalizedFullPath = parentProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(locale, delimiter);

			int indexForCmsProperty = 0;
			//Only ComplexCmsProperties can have index
			if (this instanceof CmsPropertyIndexable)
				indexForCmsProperty = ((CmsPropertyIndexable)this).getIndex();

			localizedFullPath =  PropertyPath.createFullPropertyPathWithDelimiter(parentLocalizedFullPath, localizedFullPath, delimiter);
			
			if (indexForCmsProperty > 0 || 
					(indexForCmsProperty == 0 && ValueType.Complex == getValueType() && isMultiple()))
					localizedFullPath += CmsConstants.LEFT_BRACKET + indexForCmsProperty + CmsConstants.RIGHT_BRACKET;
			
		}
		
		return localizedFullPath;
	}

	public String getLocalizedLabelOfFullPathforLocale(String locale) {
		
		return getLocalizedLabelOfFullPathforLocaleWithDelimiter(locale, CmsConstants.PERIOD_DELIM);
	}



	public String toString() {
		return getFullPath();
	}

	@Override
	/**
	 * Since localized labels are defined in definition's level, 
	 * {@link LocalizableEntity#getLocalizedLabelForCurrentLocale()} must
	 * be overridden.
	 */
	public String getLocalizedLabelForCurrentLocale() {
		return getPropertyDefinition().getDisplayName().getLocalizedLabelForLocale(getCurrentLocale());
	}

	@Override
	/**
	 * Since localized labels are defined in definition's level, 
	 * {@link LocalizableEntity#getLocalizedLabelForLocale(String locale)} must
	 * be overridden.
	 */
	public String getLocalizedLabelForLocale(String locale) {
		return getPropertyDefinition().getDisplayName().getLocalizedLabelForLocale(locale);
	}
	
	
	/**
	 * Since localized labels are defined in definition's level, 
	 * {@link Localization#getAvailableLocalizedLabel(String)} must
	 * be implemented to use the localization from definition and not the localization inside the property which is always null.
	 */
	@Override
	public String getAvailableLocalizedLabel(String preferredLocale) {
		return getPropertyDefinition().getDisplayName().getAvailableLocalizedLabel(preferredLocale);
	}

	@Override
	/**
	 * Since localized labels are defined in definition's level, 
	 * {@link LocalizableEntity#getLocalizedLabels()} must
	 * be overridden.
	 */
	public Map<String, String> getLocalizedLabels() {
		return getPropertyDefinition().getDisplayName().getLocalizedLabels();
	}

	@Override
	/**
	 * Since localized labels are defined in definition's level, 
	 * {@link LocalizableEntity#addLocalizedLabel(String locale, String localizedName)} must
	 * be overridden.
	 */
	public void addLocalizedLabel(String locale, String localizedName) {
		throw new CmsException("Localized labels cannot be defined through property level");
	}

	public void resetPaths() {
		fullPath = null;
		path=null;
		permanentPath = null;
		fullPermanentPath = null;
	}
	
	//Useful method to obtain the id of content object
	//which contains this property
	public String getContentObjectId(){
		
		if (parentProperty != null && parentProperty instanceof CmsPropertyImpl){
			return ((CmsPropertyImpl)parentProperty).getContentObjectId();
		}
		
		return null;
	}

	//Useful method to obtain the system name of content object
	//which contains this property
	public String getContentObjectSystemName(){
		
		if (parentProperty != null && parentProperty instanceof CmsPropertyImpl){
			return ((CmsPropertyImpl)parentProperty).getContentObjectSystemName();
		}
		
		return null;
	}

	/**
	 * 
	 */
	public void clean() {
		setId(null);
		
		permanentPath = null;
		fullPermanentPath = null;
	}

	@Override
	public String getFullPermanentPath() {
		
		if (StringUtils.isNotBlank(fullPermanentPath))
			return fullPermanentPath;
		
		if (parentProperty==null){ 
			fullPermanentPath = name;
		}
		else{
			String propertyId = null;

			if (this instanceof ComplexCmsProperty && getPropertyDefinition().isMultiple()){
				//	use Identifier only if property is a complex property and can have multiple values
				propertyId = ((ComplexCmsProperty)this).getId();
				
				if (propertyId == null || propertyId.trim().isEmpty()){
					throw new CmsException("Unable to generate permanent path for property "+getFullPath()+" because it has not yet been saved");
				}
			}
			
			String parentFullPermanentPath = parentProperty.getFullPermanentPath();
			fullPermanentPath = PropertyPath.createFullPropertyPath(parentFullPermanentPath, name);
			
			if (propertyId != null){
				fullPermanentPath += CmsConstants.LEFT_BRACKET + propertyId + CmsConstants.RIGHT_BRACKET;
			}
			
		}

		return fullPermanentPath;
	}

	@Override
	public String getPermanentPath() {
		if (StringUtils.isNotBlank(permanentPath))
			return permanentPath;
		
		permanentPath = PropertyPath.getPathAfterFirstLevel(getFullPermanentPath());
		
		return permanentPath;
	}

	
}
