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

import java.util.Calendar;
import java.util.Locale;

/**
 * Definition for a simple property whose type is {@link Calendar}.
 *
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is XML schema
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#date">date</a> or
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime">dateTime</a> 
 *  data type to describe simple property of type {@link Calendar}.
 * 
 * The following example defines a simple property named <code>modified</code> which is 
 * optional, single valued, of type {@link Calendar} and whose 
 * label for {@link Locale#ENGLISH English} locale is <code>Modified Date</code>.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *	
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;modified&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot;  
 *        {@link CmsDefinition#getValueType() type}=&quot;xs:datetime&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Modified Date&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *   &lt;/xs:element&gt;
 *  ...
 * &lt;/xs:schema&gt;  
 * </pre>
 * 
 * </p>
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CalendarPropertyDefinition extends
		SimpleCmsPropertyDefinition<Calendar> {
	

	/**
	 * Retrieve the pattern that literal value(s) must match 
	 * depending on whether in XML Schema a
	 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#date">date</a> or
	 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime">dateTime</a>
	 * 
	 * has been defined.
	 * 
	 *  <p>
	 *  NOTE that no time zone character has been provided, as this depends on the DateFormatter used.
	 *  Bear in  mind that according to the above references, time zone is expected to follow the ISO8601 standard, 
	 *  that is it must have the format +/-hh:mm | 'Z'.
	 *  
	 *  There is no standard class or method which displays a calendar into a string which contains time zone info in the above format.
	 *  {@link java.text.SimpleDateFormat SimpleDateFormat} does not support formating the time zone to the above format.
	 *  
	 *  In Astroboa , org.apache.commons.lang.time.FastDateFormat (Apache Commons Lang) is used with the addition of 'ZZ' characters at the
	 *  end of the pattern.
	 *  
	 *  Therefore, API user is free to choose the date formatter of her choice but must make sure that time zone info according to
	 *  ISO8601 format is also generated, if this value is to be used in an xml and time zone information is needed. 
	 *  </p>
	 *  
	 * @return <code>yyyy-MM-dd</code> for <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#date">date</a>,
	 * <code>yyyy-MM-dd'T'HH:mm:ss.SSS</code> for <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime">dateTime</a> 
	 */
	String getPattern();
	
	/**
	 * Check whether values for this property contains time information as well
	 * 
	 * @return <code>true</code> if values contain date and time information, <code>false</code> if values contain
	 * only date information.
	 * 
	 */
	boolean isDateTime();
}
