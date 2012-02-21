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
 * Enumeration containing all built in items (jcr nodes AND properties) used by
 * Astroboa.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsBuiltInItem implements ItemQName {

	// CMS SYSTEM
	SYSTEM(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"system"), 
	
	CmsIdentifier(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"cmsIdentifier"),
			
	SystemName(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
				BetaConceptNamespaceConstants.ASTROBOA_URI,
				"systemName"),

	Localization(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"localization"),

	// Taxonomy
	SubjectTaxonomy(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"subjectTaxonomy"),
	
	TaxonomyRoot(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"taxonomyRoot"),
	
	Taxonomy(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"taxonomy"), 
			
	Topic(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"topic"), 
			
	Space(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI, "space"), 
			
	Order(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"order"), 
			
	AllowsReferrerContentObjects(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"allowsReferrerContentObjects"), 
			
	ContentObjectReferences(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"contentObjectReferences"),

	ContentTypeSpecificTaxonomyRoot(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI, 
			"contentTypeSpecificTaxonomyRoot"), 
			
	Vocabularies(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"vocabularies"),
			
	OrganizationSpace(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"organizationSpace"),
			

	// Content Object
	ContentObject(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"contentObject"), 
			
	ContentObjectRoot(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"contentObjectRoot"), 
			
	ContentObjectTypeName(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"contentObjectTypeName"), 
			
	GenericContentTypeFolder(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"genericContentTypeFolder"), 

	GenericHourFolder(
					BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
					BetaConceptNamespaceConstants.ASTROBOA_URI,
					"genericHourFolder"),
					
	GenericMinuteFolder(
					BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
					BetaConceptNamespaceConstants.ASTROBOA_URI,
					"genericMinuteFolder"),
					
	GenericSecondFolder(
							BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
							BetaConceptNamespaceConstants.ASTROBOA_URI,
							"genericSecondFolder"),

	GenericDayFolder(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"genericDayFolder"), 
			
	GenericMonthFolder(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"genericMonthFolder"), 
			
	GenericYearFolder(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"genericYearFolder"), 
			
	StructuredContentObject(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"structuredContentObject"),
			
	OwnerCmsIdentifier(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"ownerBetaCmsIdentifier"),
			
	ManagedThroughWorkflow(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"managedThroughWorkflow"), 
			
	ContentObjectStatus(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"contentObjectStatus"), 
			
	Aspects(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"aspects"),
			
	StructuredComplexCmsProperty(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
					BetaConceptNamespaceConstants.ASTROBOA_URI,
					"structuredComplexCmsProperty"), 

	// Repository User
	RepositoryUserRoot(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"repositoryUserRoot"), 
			
	RepositoryUser(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"repositoryUser"), 
	UserType(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"userType"), 
			
	Label(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI, "label"), 
			
	ExternalId(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"externalId"), 
			
	Preferences(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"preferences"), 
			
	Folksonomy(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"folksonomy"),

	// BinaryChannel
	BinaryChannel(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"binaryChannel"), 
			
	Name(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI, "name"), 
			
	Size(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI, "size"), 

	MimeType(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI, "mimeType"), 
			
	Encoding(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"encoding"), 
			
	SourceFileName(
			BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
			BetaConceptNamespaceConstants.ASTROBOA_URI,
			"sourceFileName"), 
			
	Legend(BetaConceptNamespaceConstants.ASTROBOA_PREFIX, 
			BetaConceptNamespaceConstants.ASTROBOA_URI, "legend");
	
	

	private ItemQName cmsBuiltInNodeType;

	private CmsBuiltInItem(String prefix, String namespaceUrl, String localPart) {
		cmsBuiltInNodeType = new ItemQNameImpl(prefix, namespaceUrl, localPart); 
	}

	public String getJcrName() {
		return cmsBuiltInNodeType.getJcrName();
	}

	public String getLocalPart() {
		return cmsBuiltInNodeType.getLocalPart();
	}

	public String getNamespaceURI() {
		return cmsBuiltInNodeType.getNamespaceURI();
	}

	public String getPrefix() {
		return cmsBuiltInNodeType.getPrefix();
	}

	public boolean equals(ItemQName otherItemQName) {
		return cmsBuiltInNodeType.equals(otherItemQName);
	}
	public boolean equalsTo(ItemQName otherItemQName) {
		return equals(otherItemQName);
	}


}
