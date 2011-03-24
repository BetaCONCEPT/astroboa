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

import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.security.CmsPasswordEncryptor;

/**
 * Definition for a simple property whose type is {@link String}.
 * 
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is XML schema
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#string">string</a>
 * data type to describe simple property of type {@link String}.
 * 
 * The following example defines a simple property named <code>title</code> which is 
 * optional, single valued and of type {@link String} with <code>No title</code>
 * as default value. Also its label for {@link Locale#ENGLISH English}
 * locale is <code>Title</code>. This property is restrcited to <code>plain text</code>
 * and its value should not be more than <code>100</code> characters.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *	xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"&gt;
 *	
 *	&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/model"
 *		schemaLocation="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd" /&gt;
 *  ...		
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;title&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot;  
 *        {@link CmsDefinition#getValueType() type}=&quot;xs:string&quot; {@link #getStringFormat() bccmsmodel:stringFormat}=&quot;PlainText&quot; 
 *        {@link SimpleCmsPropertyDefinition#getDefaultValue() default}=&quot;No title&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Title&lt;/xs:documentation&gt;
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
public interface StringPropertyDefinition extends
		SimpleCmsPropertyDefinition<String> {

	/**
	 * Returns the string format that property's value(s) should have.
	 * 
	 * <p>
	 * Astroboa implementation uses attribute
	 * 	<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">stringFormat</a> 
	 * to provide value for this attribute. Values are case sensitive therefore must match exactly
	 * {@link StringFormat} enum names.
	 * </p>

	 * @return String format of property's value(s).
	 */
	StringFormat getStringFormat();

	/**
	 * Returns maximum length of property's value(s).
	 * 
	 * <p>
	 * Astroboa implementation uses attribute
	 * 	<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">maxStringLength</a> 
	 * to provide value for this attribute.
	 * </p>

	 * @deprecated Use {@link #getMaxLength()}
	 * @return Maximum length of property's value(s).
	 */
	@Deprecated
	Integer getMaxStringLength();
	
	/**
	 * In cases where property is of password type, 
	 * 
	 * a {@link CmsPasswordEncryptor} is always available in order to encrypt values.
	 * 
	 * Note that it is user's responsibility to provide the encrypted values. Astroboa
	 * will perform no encryption whatsoever when method {@link SimpleCmsProperty#addSimpleTypeValue(Object)}
	 * is called.
	 * 
	 * @return Password encryptor responsible to encrypt provided values
	 */
	CmsPasswordEncryptor getPasswordEncryptor();
	

	/**
	 * Useful method to check if this StringProperty is of bccsmodel:passwordType.
	 * 
	 * @return <code>true</code> if property is of passwordType, <code>false</code> otherwise
	 */
	boolean isPasswordType();
	
	/**
	 * Returns maximum length of property's value(s).
     *
	 * 
	 * @return Maximum length of property's value(s) or <code>null</code> for unlimited length.
	 */
	Integer getMaxLength();
	
	/**
	 * Returns minimum length of property's value(s).
     *
	 * 
	 * @return minimum length of property's value(s) or <code>null</code> for unlimited length.
	 */
	Integer getMinLength();
	
	/**
	 * Returns the pattern (regular expression) that all of property's value(s) should follow.
     *
	 * 
	 * @return the pattern (regular expression) that all of property's value(s) should follow 
	 * or <code>null</code> if none is provided.
	 */
	String getPattern();
}
