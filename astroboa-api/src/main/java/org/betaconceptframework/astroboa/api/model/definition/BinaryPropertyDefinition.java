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

import java.util.Locale;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;

    

/**
 * Definition for a simple property whose type is {@link BinaryChannel}.
 * 
 * <p>
 * Astroboa implementation uses an XML Schema <code>element</code> whose
 * <code>type</code> is Astroboa complex xml type <code>binaryChannel</code>
 * to describe simple property of type {@link BinaryChannel}.
 * 
 * The following example defines a simple property named <code>thumbnail</code> which is
 * optional, single valued, of type {@link BinaryChannel} and whose 
 * label for {@link Locale#ENGLISH English} locale is <code>Article thumbnail</code>.
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *	xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"&gt;
 *	
 *	&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/model"
 *		schemaLocation="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd" /&gt;
 *	
 *  ...
 *   &lt;xs:element {@link CmsDefinition#getName() name}=&quot;thumbnail&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot; 
 *        {@link CmsDefinition#getValueType() type}=&quot;<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">bccmsmodel:binaryChannelType</a>&quot;&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Article thumbnail&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *   &lt;/xs:element&gt;
 *  ...
 *  
 * &lt;/xs:schema&gt;
 * </pre>
 * 
 * </p>
 * 
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 * @see <a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">Astroboa model XML schema
 * for more on <code>binaryChannel</code> complex type. </a>
 * 
 * 
 */
public interface BinaryPropertyDefinition extends
		SimpleCmsPropertyDefinition<BinaryChannel> {

	/**
	 * Specifies whether values of {@link BinaryProperty} are unmanaged binary channels.
	 * 
	 * Default value is <code>false</code>. In order to define that binary channels are unmanaged, the 
	 * following attribute must be set to proeprty's definition in XSD
	 * 
	 * <pre>
	 *  bccmsmodel:unmanagedBinaryChannel=true
	 * </pre>
	 * 
	 * @return <code>true</code> if binary channels are unmanaged, <code>false</code>.
	 */
	boolean isBinaryChannelUnmanaged();
}
