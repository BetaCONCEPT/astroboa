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

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BooleanPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * Implementation for Boolean property definition.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class BooleanPropertyDefinitionImpl extends SimpleCmsPropertyDefinitionImpl<Boolean>	implements BooleanPropertyDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186113024869159420L;

	public BooleanPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory,Integer order,  String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			Boolean defaultValue, String repositoryObjectRestriction,boolean representsAnXmlAttribute) {
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				defaultValue, repositoryObjectRestriction, null,representsAnXmlAttribute);
		
	}

	public ValueType getValueType() {
		return ValueType.Boolean;
	}

	@Override
	public BooleanPropertyDefinition clone(ComplexCmsPropertyDefinition parentDefinition) {
		return new BooleanPropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getDefaultValue(), getRepositoryObjectRestriction(),isRepresentsAnXmlAttribute());
	}

}
