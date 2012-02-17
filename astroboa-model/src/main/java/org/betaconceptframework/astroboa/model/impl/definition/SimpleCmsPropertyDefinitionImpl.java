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
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections.MapUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
abstract class  SimpleCmsPropertyDefinitionImpl<T> extends CmsPropertyDefinitionImpl implements SimpleCmsPropertyDefinition<T>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9139790239667109331L;

	private final T defaultValue;
	
	private final String repositoryObjectRestriction;
	
	private Map<T, Localization> valueEnumeration;

	
	public SimpleCmsPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles, 
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			T defaultValue, String repositoryObjectRestriction,
			Map<T, Localization> acceptedValues) {
		
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order,
				restrictReadToRoles, restrictWriteToRoles, parentDefinition);
		
		this.defaultValue = defaultValue;
		this.repositoryObjectRestriction =repositoryObjectRestriction;
		this.valueEnumeration = acceptedValues;
	}

	public void accept(DefinitionVisitor visitor) {
		if (visitor != null)
			visitor.visitSimplePropertyDefinition(this);
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getRepositoryObjectRestriction() {
		return repositoryObjectRestriction;
	}

	public boolean isSetDefaultValue() {
		return defaultValue != null;
	}

	public Map<T, Localization> getValueEnumeration() {
		return valueEnumeration;
	}

	public boolean isValueValid(T value) {
		if (MapUtils.isEmpty(valueEnumeration)){
			return true;
		}
		
		if (value == null){
			return false;
		}
		return valueEnumeration.containsKey(value);
	}
	
}
