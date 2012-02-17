/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.io;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IOUtils {

	private static final String CDATA = "CDATA";

	public static void addAttribute(AttributesImpl atts,
			String localPart, String value) {
		
		if (atts != null){
			atts.addAttribute(
					null, 
					localPart, 
					localPart, 
					CDATA, 
					value);
		}

		
	}

	public static void addAttribute(AttributesImpl atts, QName attributeQName, String value) {

		if (atts != null){
			atts.addAttribute(
					attributeQName.getNamespaceURI(), 
					attributeQName.getLocalPart(), 
					attributeQName.getPrefix() != null ? attributeQName.getPrefix()+CmsConstants.QNAME_PREFIX_SEPARATOR+attributeQName.getLocalPart() : 
						attributeQName.getLocalPart(), 
					CDATA, 
					value);
		}

		
	}

	public static void addAttribute(AttributesImpl atts, ItemQName itemQName, String value){
		if (atts != null){
			atts.addAttribute(
					itemQName.getNamespaceURI(), 
					itemQName.getLocalPart(), 
					itemQName.getJcrName(), 
					CDATA, 
					value);
		}
	}
	
	public static void addAstroboaModelNamespaceDeclarationAttribute(AttributesImpl atts){
		if (atts != null){
			atts.addAttribute(
					XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX, 
					XMLConstants.XMLNS_ATTRIBUTE+CmsConstants.QNAME_PREFIX_SEPARATOR+BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX, 
					CDATA, 
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_URI);
		}
	}
	
	public static void addXsiNamespaceDeclarationAttribute(AttributesImpl atts){
		if (atts != null){
			atts.addAttribute(
					XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
					"xsi", 
					XMLConstants.XMLNS_ATTRIBUTE+CmsConstants.QNAME_PREFIX_SEPARATOR+"xsi", 
					CDATA, 
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		}
	}

	public static void addXsiSchemaLocationAttribute(AttributesImpl atts,
			String xsiSchemaLocation) {
		
		if (atts != null){
			atts.addAttribute(
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					"schemaLocation", 
					"xsi:schemaLocation", 
					CDATA, 
					xsiSchemaLocation);
		}
	}

	public static void addAstroboaApiNamespaceDeclarationAttribute(
			AttributesImpl atts) {
		if (atts != null){
			atts.addAttribute(
					XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_PREFIX, 
					XMLConstants.XMLNS_ATTRIBUTE+CmsConstants.QNAME_PREFIX_SEPARATOR+BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_PREFIX, 
					CDATA, 
					BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_URI);
		}
		
	}
	
}
