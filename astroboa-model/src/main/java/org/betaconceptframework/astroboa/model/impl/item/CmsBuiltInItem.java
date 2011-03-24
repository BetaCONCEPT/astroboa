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
	SYSTEM(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"system"), 
	
	CmsIdentifier(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"cmsIdentifier"),
			
	SystemBuiltinEntity(
				BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
				BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
				"systemBuiltinEntity"),
	SystemName(
						BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
						BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
						"systemName"),
									
				

	Localization(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"localization"),

	// Taxonomy
	SubjectTaxonomy(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"subjectTaxonomy"),
	
	TaxonomyRoot(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"taxonomyRoot"),
	
	Taxonomy(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"taxonomy"), 
			
	Topic(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"topic"), 
			
	Space(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "space"), 
			
	Order(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"order"), 
			
	AllowsReferrerContentObjects(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"allowsReferrerContentObjects"), 
			
	ContentObjectReferences(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"contentObjectReferences"),

	ContentTypeSpecificTaxonomyRoot(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, 
			"contentTypeSpecificTaxonomyRoot"), 
			
	Vocabularies(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"vocabularies"),
			
	OrganizationSpace(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"organizationSpace"),
			

	// Content Object
	ContentObject(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"contentObject"), 
			
	ContentObjectRoot(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"contentObjectRoot"), 
			
	ContentObjectTypeName(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"contentObjectTypeName"), 
			
	GenericContentTypeFolder(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"genericContentTypeFolder"), 

	GenericHourFolder(
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
					"genericHourFolder"),
					
	GenericMinuteFolder(
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
					"genericMinuteFolder"),
					
	GenericSecondFolder(
							BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
							BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
							"genericSecondFolder"),

	GenericDayFolder(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"genericDayFolder"), 
			
	GenericMonthFolder(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"genericMonthFolder"), 
			
	GenericYearFolder(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"genericYearFolder"), 
			
	StructuredContentObject(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"structuredContentObject"),
			
	OwnerCmsIdentifier(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"ownerBetaCmsIdentifier"),
			
	ManagedThroughWorkflow(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"managedThroughWorkflow"), 
			
	ContentObjectStatus(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"contentObjectStatus"), 
			
	Aspects(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"aspects"),
			
	StructuredComplexCmsProperty(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
					"structuredComplexCmsProperty"), 

	// Repository User
	RepositoryUserRoot(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"repositoryUserRoot"), 
			
	RepositoryUser(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"repositoryUser"), 
	UserType(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"userType"), 
			
	Label(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "label"), 
			
	ExternalId(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"externalId"), 
			
	Preferences(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"preferences"), 
			
	Folksonomy(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"folksonomy"),

	// BinaryChannel
	BinaryChannel(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"binaryChannel"), 
			
	Name(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "name"), 
			
	Size(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "size"), 

	MimeType(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "mimeType"), 
			
	Encoding(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"encoding"), 
			
	SourceFileName(
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX,
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI,
			"sourceFileName"), 
			
	Legend(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX, 
			BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_URI, "legend");
	
	

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
