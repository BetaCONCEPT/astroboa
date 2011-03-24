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

package org.betaconceptframework.astroboa.model.impl.definition;


import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final  class ComplexCmsPropertyDefinitionImpl extends AbstractComplexCmsPropertyDefinitionImpl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6844505899220809711L;
	
	private String propertyPathWhoseValueCanBeUsedAsALabel;
	
	private final boolean systemTypeDefinition;
	
	private final boolean global;

	private final boolean cmmonAttributesAreDefined;
	
	public ComplexCmsPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper,
			URI definitionFileURI, 
			String propertyPathWhoseValueCanBeUsedAsALabel,boolean systemTypeDefinition, 
			boolean global,QName qNameOfParentDefinitionWithTheSameType, String typeName, boolean cmmonAttributesAreDefined) {
		
			super(qualifiedName, description, displayName, obsolete, multiple, mandatory, 
				restrictReadToRoles, restrictWriteToRoles,order, parentDefinition,
				complexPropertyDefinitionHelper, definitionFileURI, qNameOfParentDefinitionWithTheSameType, typeName);
		
		this.systemTypeDefinition =  systemTypeDefinition;
		this.global = global;
		this.cmmonAttributesAreDefined = cmmonAttributesAreDefined;  
		
		this.propertyPathWhoseValueCanBeUsedAsALabel = propertyPathWhoseValueCanBeUsedAsALabel;
	}
	
	public ValueType getValueType() {
		return ValueType.Complex;
	}

	@Override
	public ComplexCmsPropertyDefinition clone(
			ComplexCmsPropertyDefinition parentDefinition) {
		
		ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper = new ComplexPropertyDefinitionHelper();
		
		ComplexCmsPropertyDefinitionImpl cloneComplexCmsPropertyDefinition = new ComplexCmsPropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				complexPropertyDefinitionHelper, getDefinitionFileURI(),  
				propertyPathWhoseValueCanBeUsedAsALabel, systemTypeDefinition, global, qNameOfParentDefinitionWithTheSameType, typeName, cmmonAttributesAreDefined);
		
		//Clone its child property definitions only if this definition is not the same with its parent
		//Otherwise this will take place when a child property definition will be needed
		if (qNameOfParentDefinitionWithTheSameType == null && hasChildCmsPropertyDefinitions()){
			Map<String, CmsPropertyDefinition> clonedChildPropertyDefinitions = new LinkedHashMap<String, CmsPropertyDefinition>();
			
			Collection<CmsPropertyDefinition> childPropertiesOfComplexDefinitionReference = getChildCmsPropertyDefinitions().values();
			
			for (CmsPropertyDefinition complexReferenceChildPropertyDefinition : childPropertiesOfComplexDefinitionReference){
				clonedChildPropertyDefinitions.put(complexReferenceChildPropertyDefinition.getName(), 
						((CmsPropertyDefinitionImpl)complexReferenceChildPropertyDefinition).clone(cloneComplexCmsPropertyDefinition));
			}
			
			complexPropertyDefinitionHelper.setChildPropertyDefinitions(clonedChildPropertyDefinitions);
			
		}
		
		return cloneComplexCmsPropertyDefinition;
	}

	@Override
	public String getPropertyPathWhoseValueCanBeUsedAsALabel() {
		return propertyPathWhoseValueCanBeUsedAsALabel;
	}

	public boolean isSystemTypeDefinition() {
		return systemTypeDefinition;
	}

	public boolean isGlobal() {
		return global;
	}

	public boolean commonAttributesAreDefined() {
		return cmmonAttributesAreDefined;
	}
}
