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
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor.VisitType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class ContentObjectTypeDefinitionImpl  extends LocalizableCmsDefinitionImpl implements ContentObjectTypeDefinition, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2925561170291794574L;

	//This is the uri of file containing the schema for this definition
	private URI definitionFileURI;
	
	private final ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper;
	
	private List<String> superTypes;
	
	private final boolean schemaExtendsBaseObjectType;
	
	private String propertyPathsWhoseValuesCanBeUsedAsALabel;

	public ContentObjectTypeDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName,
			ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper,
			URI definitionFileURI,  
			List<String> superTypes, 
			boolean schemaExtendsBaseObjectType,
			String propertyPathsWhoseValuesCanBeUsedAsALabel) {
		
		super(qualifiedName, description, displayName);

		this.complexPropertyDefinitionHelper = complexPropertyDefinitionHelper;
		
		this.definitionFileURI = definitionFileURI;
		
		this.superTypes = superTypes;
		
		this.schemaExtendsBaseObjectType = schemaExtendsBaseObjectType;
		
		this.propertyPathsWhoseValuesCanBeUsedAsALabel = propertyPathsWhoseValuesCanBeUsedAsALabel;
	}


	public void accept(DefinitionVisitor visitor) {
		//Throw exception?
		if (visitor == null)
			return;
		
		visitor.visit(this);
		
		if (VisitType.Self != visitor.getVisitType()){
			visitSubPropertyDefinitions(visitor);
		}
	}

	private void visitSubPropertyDefinitions(DefinitionVisitor visitor) {
		
		visitor.startChildDefinitionsVisit(this);
		
		//Now send visitor to visit sub properties
		if (hasCmsPropertyDefinitions()){
			for (CmsPropertyDefinition propertyDefinition: getPropertyDefinitions().values()){
				propertyDefinition.accept(visitor);
			}
		}
		
		visitor.finishedChildDefinitionsVisit(this);
	}

	public CmsPropertyDefinition getCmsPropertyDefinition(String cmsPropertyPath) {
		return complexPropertyDefinitionHelper.getChildCmsPropertyDefinition(cmsPropertyPath);
	}

	public Map<String, CmsPropertyDefinition> getPropertyDefinitions() {
		return complexPropertyDefinitionHelper.getChildPropertyDefinitions();
	}

	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(
			String locale) {
		return complexPropertyDefinitionHelper.getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(locale);
	}
	
	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(
			String locale) {
		return complexPropertyDefinitionHelper.getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(locale);
	}
	

	public boolean hasCmsPropertyDefinition(String cmsPropertyPath) {
		return complexPropertyDefinitionHelper.hasChildCmsPropertyDefinition(cmsPropertyPath);
	}

	public boolean hasCmsPropertyDefinitions() {
		return complexPropertyDefinitionHelper.hasPropertyDefinitions();
	}

	public ValueType getValueType() {
		return ValueType.ContentType;
	}

	public ComplexPropertyDefinitionHelper getComplexPropertyDefinitionHelper() {
		return complexPropertyDefinitionHelper;
	}
	
	public URI getDefinitionFileURI() {
		return definitionFileURI;
	}

	public boolean isTypeOf(String superType) {
		return superType != null && superTypes != null && superTypes.contains(superType);
	}

	public List<String> getSuperContentTypes() {
		return superTypes;
	}


	@Override
	public String toString() {
		return new StringBuilder()
				.append("Content Type [definitionFileURI=")
				.append(definitionFileURI)
				.append(", superTypes=")
				.append(superTypes)
				.append(super.toString())
				.append("]").toString();
	}


	public int getDepth(){
		if (complexPropertyDefinitionHelper == null){
			return 0;
		}
		
		return complexPropertyDefinitionHelper.getDepth();
	}


	public void resetDepth() {
		if (complexPropertyDefinitionHelper!=null){
			complexPropertyDefinitionHelper.resetDepth();
		}
	}


	@Override
	public boolean schemaExtendsBaseObjectTypeDefinition() {
		return schemaExtendsBaseObjectType;
	}
	
	@Override
	public String getPropertyPathsWhoseValuesCanBeUsedAsALabel(){
		return propertyPathsWhoseValuesCanBeUsedAsALabel;
	}
	
}
