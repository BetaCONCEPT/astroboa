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
package org.betaconceptframework.astroboa.portal.el;

import javax.el.ELContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.jboss.seam.el.SeamELResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaELResolver extends SeamELResolver{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		
		if (base == null || property == null ){
			return super.getValue(context, base, property);
		}

		try{

			if (base instanceof ContentObject){
				if (keywordIsNotDefinedAsAProperty("systemName", property, ((ContentObject)base).getComplexCmsRootProperty())){
					context.setPropertyResolved(true);
					return ((ContentObject)base).getSystemName();
				}
				else if (keywordIsNotDefinedAsAProperty("contentObjectType", property, ((ContentObject)base).getComplexCmsRootProperty())){
					context.setPropertyResolved(true);
					return ((ContentObject)base).getContentObjectType();
				}
				else if (keywordIsNotDefinedAsAProperty("owner", property, ((ContentObject)base).getComplexCmsRootProperty())){
					context.setPropertyResolved(true);
					return ((ContentObject)base).getOwner();
				}
				else if (keywordIsNotDefinedAsAProperty("complexCmsRootProperty", property, ((ContentObject)base).getComplexCmsRootProperty())){
					context.setPropertyResolved(true);
					return ((ContentObject)base).getComplexCmsRootProperty();
				}
				else if (keywordIsNotDefinedAsAProperty("typeDefinition", property, ((ContentObject)base).getComplexCmsRootProperty())){
					context.setPropertyResolved(true);
					return ((ContentObject)base).getTypeDefinition();
				}

				return resolveInComplexCmsProperty(context, ((ContentObject)base).getComplexCmsRootProperty(), property);
			}
			else if (base instanceof ComplexCmsProperty){
				return resolveInComplexCmsProperty(context, (ComplexCmsProperty)base, property);
			}
			else{
				return super.getValue(context, base, property);
			}
		}
		catch(Exception e){
			logger.warn("Unable to evaluate property "+property+ " from base "+base,e);
			return super.getValue(context, base, property);
		}
	}


	private boolean keywordIsNotDefinedAsAProperty(String keyword, Object property, ComplexCmsProperty parentProperty) {
		return StringUtils.equals(keyword, (String)property) && ! parentProperty.isChildPropertyDefined(keyword);
	}


	private Object resolveInComplexCmsProperty(ELContext context, ComplexCmsProperty complexCmsProperty,	Object property) {
		
		String propertyPath = (String) property;
		
		//Keywords
		if (keywordIsNotDefinedAsAProperty("id", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getId();
		}
		else if (keywordIsNotDefinedAsAProperty("currentLocale", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getCurrentLocale();
		}
		else if (keywordIsNotDefinedAsAProperty("fullPath", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getFullPath();
		}
		else if (keywordIsNotDefinedAsAProperty("localizedLabelForCurrentLocale", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getLocalizedLabelForCurrentLocale();
		}
		else if (keywordIsNotDefinedAsAProperty("name", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getName();
		}
		else if (keywordIsNotDefinedAsAProperty("path", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getPath();
		}
		else if (keywordIsNotDefinedAsAProperty("parentProperty", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getParentProperty();
		}
		else if (keywordIsNotDefinedAsAProperty("propertyDefinition", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getPropertyDefinition();
		}
		else if (keywordIsNotDefinedAsAProperty("valueType", property, complexCmsProperty)){
			context.setPropertyResolved(true);
			return complexCmsProperty.getValueType();
		}
		
		Object cmsProperty = null; 
		try{
			//First assume property is a simple property or a single value complex one
			cmsProperty = complexCmsProperty.getChildProperty(propertyPath);
		}
		catch(Exception e){
			//Ignore exception
		}
		
		if (cmsProperty == null){
			try{
				//Now assume property is a multivalue complex one
				cmsProperty = complexCmsProperty.getChildPropertyList(propertyPath);
			}
			catch(Exception e){
				//Ignore exception
			}	
		}
		
		if (cmsProperty != null){
			//Property exists. 
			if (cmsProperty instanceof CmsProperty){
				
				//Retrieve property definition to decide what it should be returned
				CmsPropertyDefinition childDefinition = ((CmsProperty)cmsProperty).getPropertyDefinition();
				
				if (ValueType.Complex != childDefinition.getValueType()){
					
					//Property is a simple property and therefore its values will be returned
					if (childDefinition.isMultiple()){
						context.setPropertyResolved(true);
						return  ((SimpleCmsProperty)cmsProperty).getSimpleTypeValues();
					}
					else{
						context.setPropertyResolved(true);
						return ((SimpleCmsProperty)cmsProperty).getSimpleTypeValue();
					}
				}
				else{
					
					//Very special case. Property refers to a multi value child property
					if (childDefinition.isMultiple()){
						//User requested a multi value complex property.
						//We should return the list of values rather than the first value
						cmsProperty = complexCmsProperty.getChildPropertyList(propertyPath);
					}
					
					//Property is a complex one, therefore the property will be returned
					context.setPropertyResolved(true);
					return cmsProperty;
				}
			}
			else {
				//Return property as is (Probably is a list of properties)
				context.setPropertyResolved(true);
				return cmsProperty;
			}
		}
		else{
				if (logger.isDebugEnabled()){
					logger.debug("Could not resolve expression. Could not find definition for property {} in complex property {}. Resolution is continued by Seam EL resolver", 
						new Object[]{propertyPath, complexCmsProperty.getFullPath()});
				}
				
				//Child property is not defined. Continue with Seam EL resolver
				return super.getValue(context, complexCmsProperty, property);
			}
		}
	
}
