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
 * Definition for a simple property whose type is {@link Double}.
 *
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is XML schema
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#double">double</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#float">float</a> or 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#decimal">decimal</a>
 * data type to describe simple property of type {@link Double}.
 * </p>
 * 
 * <p>
 * The following example defines a simple property named <code>price</code> which is 
 * optional, single valued and of type {@link Double} with <code>25.1</code>
 * as default value. Also its label for {@link Locale#ENGLISH English} 
 * locale is <code>Product price</code>.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;price&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot; 
 *        {@link CmsDefinition#getValueType() type}=&quot;xs:double&quot; {@link SimpleCmsPropertyDefinition#getDefaultValue() default}=&quot;25.1&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Product price&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *   &lt;/xs:element&gt;
 *  ...
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface DoublePropertyDefinition extends
		SimpleCmsPropertyDefinition<Double> {

	/**
	 * Get min allowed value
	 * 
	 * @return The minimum (inclusive) value or {@link Double#MIN_VALUE}
	 */
	Double getMinValue();
	
	/**
	 * Get min allowed value
	 * 
	 * @return The minimum (inclusive) value or {@link Double#MAX_VALUE}
	 */
	Double getMaxValue();

}
