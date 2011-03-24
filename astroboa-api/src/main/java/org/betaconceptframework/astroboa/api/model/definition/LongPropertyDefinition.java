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
 * Definition for simple property whose type is {@link Long}.
 * 
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is XML schema
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#long">long</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#nonPositiveInteger">nonPositiveInteger</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#nonNegativeInteger">nonNegativeInteger</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#negativeInteger">negativeInteger</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#int">int</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#positiveInteger">positiveInteger</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#unsignedLong">unsignedLong</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#short">short</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#unsignedInt">unsignedInt</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#byte">byte</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#unsignedByte">unsignedByte</a>
 * 
 * data type to describe simple property of type {@link Long}.
 * </p>
 * 
 * <p>
 * The following example defines a simple property named <code>counter</code> which is 
 * optional, single valued and of type {@link Long} with <code>0</code>
 * as default value. Also its label for {@link Locale#ENGLISH English} 
 * locale is <code>Counter</code>.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;counter&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot; 
 *        {@link CmsDefinition#getValueType() type}=&quot;xs:long&quot; {@link SimpleCmsPropertyDefinition#getDefaultValue() default}=&quot;0&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Counter&lt;/xs:documentation&gt;
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
public interface LongPropertyDefinition extends
		SimpleCmsPropertyDefinition<Long> {
	
	/**
	 * Get min allowed value
	 * 
	 * @return The minimum (inclusive) value or {@link Long#MIN_VALUE}
	 */
	Long getMinValue();
	
	/**
	 * Get min allowed value
	 * 
	 * @return The minimum (inclusive) value or {@link Long#MAX_VALUE}
	 */
	Long getMaxValue();

}
