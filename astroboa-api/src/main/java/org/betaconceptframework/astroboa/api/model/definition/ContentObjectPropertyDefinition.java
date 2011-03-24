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

package org.betaconceptframework.astroboa.api.model.definition;

import java.util.List;
import java.util.Locale;

import org.betaconceptframework.astroboa.api.model.ContentObject;



/**
 * Definition for a simple property whose type is {@link ContentObject}.
 *
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is Astroboa complex xml type <code>contentObject</code>
 * to describe simple property of type {@link ContentObject}.
 * 
 * The following example defines a simple property named <code>references</code> which is
 * optional, multivalue, of type {@link ContentObject} and whose 
 * label for {@link Locale#ENGLISH English} locale is <code>Article references</code>.

 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *	xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"&gt;
 *	
 *	&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/model"
 *		schemaLocation="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd" /&gt;
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;references&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;unbounded&quot; 
 *        {@link CmsDefinition#getValueType() type}=&quot;bccmsmodel:contentObject&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Article references&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *   &lt;/xs:element&gt;
 *  ...
 *  
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 * @see <a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">Astroboa model XML schema
 * for more on <code>contentObject</code> complex type. </a>
 * 
 */
public interface ContentObjectPropertyDefinition extends
		SimpleCmsPropertyDefinition<ContentObject> {
	
	/**
	 * List of content type names.
	 * 
	 * <p>
	 * If the list is not empty, then the values of a property of this
	 * definition type, are all {@link ContentObject}'s whose type must be one 
	 * of the values in the list.
	 * </p>
	 * 
	 * <p>
	 * This list may contain names of content types which are "super" or "base" content types.
	 * A "super" or "base" content type is a content type whose properties are inherited 
	 * by other content types, allowing this way to define a content type hierarchy.
	 * </p>
	 * 
	 * <p>
	 * Astroboa implementation uses attribute
	 * 	<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">acceptedContentTypes</a> 
	 * to provide value for this attribute.
	 * </p>
	 * 
	 * @return
	 * 		List of accepted content types names.
	 */
	List<String> getAcceptedContentTypes();

	/**
	 * List of all accepted content type names
	 * 
	 * <p>
	 * This list contains all accepted content types, including content types which
	 * extend any "super" or "base" type defined in {@link #getAcceptedContentTypes()} list.
	 * </p>
	 * 
	 * @return List of all accepted content type names
	 */
	List<String> getExpandedAcceptedContentTypes();

}
