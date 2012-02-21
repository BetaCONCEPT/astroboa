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
package org.betaconceptframework.astroboa.engine.definition.xsom;

import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizationImpl;

/**
 * Astroboa uses XSD element <a href="http://www.w3.org/TR/xmlschema-0/#CommVers">annotation </a>
 * to allow content modelers to define localized information about a content type
 * or a property.
 * 
 * <p>
 * Currently, a content type or a property need to have a 
 * localized display name and a localized description. The following
 * xsd excerpt show an example of an annotation which contains 
 * display name and description of property in engligh and greek locale
 * </p>
 * 
 * <p>
 * <pre>
 * <xs:annotation>
			<xs:documentation xml:lang="en">
				<bccmsmodel:displayName>Generic Content Resource</bccmsmodel:displayName> 
				<bccmsmodel:description>This element <b>models</b> the most generic / primary content object type for adding content into the repository.</bccmsmodel:description> 
			</xs:documentation>
			<xs:documentation xml:lang="el">
				<bccmsmodel:displayName>Έγγραφο / Περιεχόμενο</bccmsmodel:displayName>
				<bccmsmodel:description>Αυτός ο τύπος αντικειμένου μοντελοποιεί τον πιο γενικό / πρωτογενή τύπο αντικειμένου για την προσθήκη περιεχομένου στο αποθετήριο.</bccmsmodel:description>
			</xs:documentation>
		</xs:annotation>
 * </pre>
 * </p>
 * 
 * <p>
 * An annotation element can have one or more documentation elements (one for each locale) and
 * inside a documentation element, child elements <code>label</code> and <code>description</code>
 * can be defined.
 * </p>
 * 
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsAnnotation {

	private Localization displayName;
	private Localization description;
	
	public CmsAnnotation() {
		this.displayName = new LocalizationImpl();
		this.description = new LocalizationImpl();
	}
	public Localization getDisplayName() {
		return displayName;
	}
	public Localization getDescription() {
		return description;
	}
	
	public void addDisplayNameForLocale(String locale, String localizedValue) {
		displayName.addLocalizedLabel(locale, localizedValue);
	}
	
	public void addDescriptionForLocale(String locale, String localizedValue) {
		description.addLocalizedLabel(locale, localizedValue);
	}
	
	public boolean isNotEmpty() {
		return displayName.hasLocalizedLabels() || description.hasLocalizedLabels();
	}
	
	
	

}
