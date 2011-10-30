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
import java.util.List;

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.TopicPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class TopicReferencePropertyDefinitionImpl extends
		SimpleCmsPropertyDefinitionImpl<Topic> implements
		TopicPropertyDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6939264578698663366L;

	public TopicReferencePropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			String repositoryObjectRestriction, List<String> acceptedTaxonomies,boolean representsAnXmlAttribute) {
		
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				null, repositoryObjectRestriction, null,representsAnXmlAttribute);
		
		this.acceptedTaxonomies = acceptedTaxonomies;
	}

	private List<String> acceptedTaxonomies;
	
	public ValueType getValueType() {
		return ValueType.TopicReference;
	}

	@Override
	public List<String> getAcceptedTaxonomies() {
		return acceptedTaxonomies;
	}

	@Override
	public TopicReferencePropertyDefinition clone(
			ComplexCmsPropertyDefinition parentDefinition) {
		
		return new TopicReferencePropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getRepositoryObjectRestriction(), acceptedTaxonomies, isRepresentsAnXmlAttribute());
		}
}
