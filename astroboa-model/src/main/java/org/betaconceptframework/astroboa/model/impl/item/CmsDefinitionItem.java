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

package org.betaconceptframework.astroboa.model.impl.item;

import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.model.impl.ItemQName;

/**
 * Enumeration containing all qualified names
 * for definition items used by Astroboa.
 * 
 * These items are found mainly in 
 * content definition XML schema files.
 * 
 * Enum value names match with names provided in
 * /META-INF/astroboa-model-version.xsd
 * which comes along with the distribution.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsDefinitionItem implements ItemQName{

	obsolete,
	name,
	acceptedTaxonomies,
	stringFormat,
	contentObjectPropertyAttGroup, 
	restrictWriteToRoles,
	restrictReadToRoles,
	maxOccursIfAspect,
	//Refers to complex type topicType
	topicType,
	//Refers to complex type spaceType
	spaceType,
	//Refers to complex type contentObjectType
	contentObjectType,
	//Refers to complex type repositoryUserType
	repositoryUserType,
	//Refers to complex type binaryChannelType
	binaryChannelType, 
	//Refers to complex type complexCmsPropertyType
	complexCmsPropertyType, 
	order,
	acceptedContentTypes,
	
	//Refers to global built in element taxonomy
	taxonomy,
	//Refers to global built in element repositoryUser
	repositoryUser,
	//Refers to global built in element topic
	topic,
	//Refers to global built in element space
	space,
	labelElementPath, 
	unmanagedBinaryChannel,
	passwordEncryptorClassName,
	passwordType,
	//Refers to the display name of a definition (content type or property)
	//suitable for display to end-users
	displayName,
	//Refers to the description of a definition (content type or property)
	description,
	//Refers to complex type contentObjectReferenceType
	contentObjectReferenceType,
	//Refers to attribute group which contains all common attributes of a Astroboa entity
	commonEntityAttributes;
	
	
	private ItemQName cmsDefinitionItem;

	private CmsDefinitionItem(){
		cmsDefinitionItem = new ItemQNameImpl(BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_PREFIX,
				BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI, this.name()); 
	}

	public String getJcrName()
	{
		return cmsDefinitionItem.getJcrName();
	}

	public String getLocalPart() {
		return cmsDefinitionItem.getLocalPart();
	}

	public String getNamespaceURI() {
		return cmsDefinitionItem.getNamespaceURI();
	}

	public String getPrefix() {
		return cmsDefinitionItem.getPrefix();
	}

	public boolean equals(ItemQName otherItemQName) {
		return cmsDefinitionItem.equals(otherItemQName);
	}
	public boolean equalsTo(ItemQName otherItemQName) {
		return equals(otherItemQName);
	}

}
