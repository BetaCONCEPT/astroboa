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

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.RepositoryUserPropertyDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class RepositoryUserPropertyDefinitionImpl extends
		SimpleCmsPropertyDefinitionImpl<RepositoryUser> implements
		RepositoryUserPropertyDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3034104054102217400L;

	public RepositoryUserPropertyDefinitionImpl(QName qualifiedName,
			Localization description, Localization displayName, boolean obsolete,
			boolean multiple, boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			 String repositoryObjectRestriction) {
		
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				null, repositoryObjectRestriction, null);

	}

	public ValueType getValueType() {
		return ValueType.RepositoryUser;
	}

	@Override
	public RepositoryUserPropertyDefinition clone(
			ComplexCmsPropertyDefinition parentDefinition) {
		
		return new RepositoryUserPropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getRepositoryObjectRestriction());
		}

}
