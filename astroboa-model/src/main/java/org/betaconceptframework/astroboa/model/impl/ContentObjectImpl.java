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
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.betaconceptframework.astroboa.model.jaxb.adapter.ContentObjectAdapter;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 *
 */
@XmlJavaTypeAdapter(value=ContentObjectAdapter.class)
public class ContentObjectImpl extends CmsRepositoryEntityImpl implements ContentObject, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8770119325909433413L;

	/**
	 * Content Object Type 
	 */
	private String contentObjectType;

	/**
	 * Content Object Owner 
	 */
	private RepositoryUser owner;

	private boolean locked;
	
	private String systemName;

	private transient ContentObjectTypeDefinition typeDefinition;

	private ComplexCmsRootProperty complexCmsRootProperty;
	
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
		
		if (complexCmsRootProperty != null){
			((ComplexCmsRootPropertyImpl)complexCmsRootProperty).setContentObjectSystemName(systemName);
		}
		
	}
	
	public void setComplexCmsRootProperty(
			ComplexCmsRootProperty complexCmsRootProperty) {
		this.complexCmsRootProperty = complexCmsRootProperty;
		
		if (this.complexCmsRootProperty != null){
			this.complexCmsRootProperty.setId(getId());
			((ComplexCmsRootPropertyImpl)this.complexCmsRootProperty).setContentObjectSystemName(systemName);
		}
	}

	public ComplexCmsRootProperty getComplexCmsRootProperty() {
		return complexCmsRootProperty;
	}


	/**
	 * @return Returns the type.
	 */
	public String getContentObjectType() {
		return contentObjectType;
	}

	/**
	 * @param contentObjectType The type to set.
	 */
	public void setContentObjectType(String contentObjectType) {
		this.contentObjectType = contentObjectType;
	}


	public RepositoryUser getOwner() {
		return owner;
	}
	public void setOwner(RepositoryUser owner) {
		this.owner = owner;
	}

	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public List<CmsProperty<?,?>> getCmsPropertyList(String propertyPath){
		return complexCmsRootProperty.getChildPropertyList(propertyPath);
	}

	public CmsProperty<?, ?> getCmsProperty(String propertyPath){
		return complexCmsRootProperty.getChildProperty(propertyPath);
	}

	public boolean removeCmsProperty(String propertyPath){
		return complexCmsRootProperty.removeChildProperty(propertyPath);
	}

	public ContentObjectTypeDefinition getTypeDefinition() {
		return typeDefinition;
	}

	public void setTypeDefinition(ContentObjectTypeDefinition typeDefinition) {
		this.typeDefinition = typeDefinition;
	}
	
	//Override deserialization process to inject 
	//property definition
	private void readObject(ObjectInputStream ois)
	    throws ClassNotFoundException, IOException {
		
		//Deserialize bean normally
	  ois.defaultReadObject();
	  
	//Inject Property definition
	  LazyLoader lazyLoader = AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	  
	  if (lazyLoader != null){
   		  lazyLoader.activateClientContextForAuthenticationToken(authenticationToken);
		  setTypeDefinition((ContentObjectTypeDefinition) lazyLoader.getDefinitionService().getCmsDefinition(contentObjectType, ResourceRepresentationType.DEFINITION_INSTANCE,false));
	  }
	}

	@Override
	public void setId(String cmsIdentifier) {
		super.setId(cmsIdentifier);
		
		if (complexCmsRootProperty != null){
			complexCmsRootProperty.setId(cmsIdentifier);
		}
	}
	@Override
	public CmsProperty<?, ?> createNewValueForMulitpleComplexCmsProperty(
			String relativePath) {
		return complexCmsRootProperty.createNewValueForMulitpleComplexCmsProperty(relativePath);
	}
	
	@Override
	public String json(boolean prettyPrint, boolean serializeBinaryContent, String... propertiesToBeExported) {
		return CmsEntitySerialization.Context.toJson(this, serializeBinaryContent, prettyPrint, propertiesToBeExported);
	}
	
	@Override
	public String xml(boolean prettyPrint, boolean serializeBinaryContent, String... propertiesToBeExported) {
		return CmsEntitySerialization.Context.toXml(this, serializeBinaryContent, prettyPrint, propertiesToBeExported);
	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContentObjectImpl [");
		if (contentObjectType != null)
			builder.append("contentObjectType=").append(contentObjectType);
		if (systemName != null)
			builder.append(", ").append("systemName=").append(systemName);
		if (getId() != null)
			builder.append(", ").append("identifier=").append(getId());
		builder.append("]");
		return builder.toString();
	}
	/**
	 * Removes all variables used when a content object instance is not new 
	 */
	public void clean() {
		
		((ComplexCmsPropertyImpl)complexCmsRootProperty).clean();
		
		setId(null);		
	}

	@Override
	public boolean hasValueForProperty(String relativePropertyPath) {
		return complexCmsRootProperty.hasValueForChildProperty(relativePropertyPath);
	}
	
	@Override
	public String getLabel(String locale) {
		return complexCmsRootProperty.getPropertyLabel(locale);
	}
}
