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

import java.util.Locale;

/**
 * Definition for a simple property whose type is {@link Boolean}.
 * 
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is XML schema
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#boolean">boolean </a>
 * data type to describe simple property of type {@link Boolean}.
 * 
 * The following example defines a simple property named <code>active</code> which is 
 * optional, single valued and of type {@link Boolean} with <code>true</code>
 * as default value. Also its label for {@link Locale#ENGLISH English} 
 * locale is <code>Article thumbnail</code>.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *	
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;active&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot; 
 *        {@link CmsDefinition#getValueType() type}=&quot;xs:boolean&quot; {@link SimpleCmsPropertyDefinition#getDefaultValue() default}=&quot;true&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Active&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *   &lt;/xs:element&gt;
 *  ...
 * &lt;/xs:schema&gt;
 * </pre>  
 * 
 * 
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface BooleanPropertyDefinition extends
		SimpleCmsPropertyDefinition<Boolean> {

}
