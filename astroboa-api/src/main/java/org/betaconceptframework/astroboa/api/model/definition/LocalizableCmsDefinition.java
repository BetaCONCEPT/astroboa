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

package org.betaconceptframework.astroboa.api.model.definition;


/**
 * Marker interface which provides localization features to a
 * {@link CmsDefinition}.
 * 
 * <p>
 * Astroboa implementation of this interface obtains localization information
 * from XML schema element <code>annotation</code>.
 * </p>
 * 
 * <p>
 * According to XSD, documentation can accept any kind of schema,
 * therefore Astroboa defines a specific schema for specifying displayName 
 * (formerly label) and description for a property or a content type definition.
 * </p>
 * 
 * <pre>
 * 	&lt;xs:annotation&gt;
 * 		&lt;xs:documentation xml:lang="en"&gt;
 *    		&lt;bccmsmodel:displayName&gt;Administrative Metadata (Dublin Core)&lt;/bccsmodel:displayName&gt;
 *     		&lt;bccsmodel:description&gt;This complex type models administrative metadata according to the Dublin Core standard&lt;/bccsmodel:description&gt;
 * 		&lt;/xs:documentation&gt;
 * &lt;xs:annotation&gt;
 * </pre>
 * 
 * <p>
 * So , in general, a content type or a property may have a displayName 
 * and/or a description in one or more languages.
 * Each of these have specific XML element which describes them 
 * which is modeled in file astroboa-model-{version}.xsd.
 * This means that the following import must exist in XSD file 
 * (which by default must exist but it is mentioned for completeness)
 * 
 * <pre>
 * 	xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"
 * 
 * &lt;xs:import namespace="http://www.betaconceptframework.org/schema/astroboa/model" schemaLocation="astroboa-model-{version}.xsd" /&gt;
 * </pre>
 * 
 * <p>
 * In cases where only one of the above is defined then 
 * automatically the missing one gets the same value. For the following example
 * </p>
 * 
 * <pre>
 * &lt;xs:annotation&gt;
 * &lt;xs:documentation xml:lang="en"&gt;
 *     &lt;bccmsmodel:displayName&gt;Administrative Metadata (Dublin Core)&lt;/bccsmodel:displayName&gt;
 * &lt;/xs:documentation&gt;
 * &lt;xs:annotation&gt;
 * </pre>
 * 
 * <p>
 * defines only displayName, 
 * but when Astroboa parses this XSD, 
 * creates a description for the same locale with the value of dsiplayName.
 * </p>
 * 
 * <p>
 * Finally, to ensure backwards compatibility, the following definition is still valid
 * 
 * <pre>
 * &lt;xs:annotation&gt;
 * &lt;xs:documentation xml:lang="en"&gt;
 *     Administrative Metadata (Dublin Core)
 * &lt;/xs:documentation&gt;
 * &lt;xs:annotation&gt;
 * </pre>
 * 
 * and instructs Astroboa to create a displayName and 
 * a description with the value 'Administrative Metadata (Dublin Core)' 
 * </p>
 * 
 * <p>
 * Localized display name an description are useful when information about a content definition
 * entity needs to be displayed in an application according to end user's locale.
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface LocalizableCmsDefinition extends CmsDefinition {

	/**
	 * Returns the description of the modeled definition
	 * 
	 * <p>
	 * Description for a definition for a locale is obtained from XSD element
	 * <a href="http://www.w3.org/TR/xmlschema-0/#ref14">documentation</a>
	 * 
	 * 
	 * @return Definition's description in one or more languages.
	 */
	Localization getDescription();
	
	/**
	 * Returns the display name of the modeled definition.
	 * 
	 * <p>
	 * Display name for a definition for a locale is obtained from XSD element
	 * <a href="http://www.w3.org/TR/xmlschema-0/#ref14">documentation</a>
	 * 
	 * 
	 * @return Definition's display name in one or more languages.
	 */
	Localization getDisplayName();

}
