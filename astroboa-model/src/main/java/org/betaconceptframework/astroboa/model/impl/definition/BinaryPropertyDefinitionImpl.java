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

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class BinaryPropertyDefinitionImpl extends SimpleCmsPropertyDefinitionImpl<BinaryChannel>	implements BinaryPropertyDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3338067580188364490L;
	
	private boolean binaryChannelUnmanaged;
	
	public BinaryPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			String repositoryObjectRestriction,
			boolean binaryChannelUnmanaged,boolean representsAnXmlAttribute) {
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				null, repositoryObjectRestriction,null,representsAnXmlAttribute);
		
		this.binaryChannelUnmanaged = binaryChannelUnmanaged;
	}

	public ValueType getValueType() {
		return ValueType.Binary;
	}

	@Override
	public BinaryPropertyDefinition clone(ComplexCmsPropertyDefinition parentDefinition) {
		
		return new BinaryPropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getRepositoryObjectRestriction(), binaryChannelUnmanaged,isRepresentsAnXmlAttribute());
	}

	@Override
	public boolean isBinaryChannelUnmanaged() {
		return binaryChannelUnmanaged;
	}
	
	
}
