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
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;

/**
 * Base interface for complex content object property definitions.
 * 
 * <p>
 * Contains common methods of a definition of a
 * {@link ComplexCmsProperty complex property}.
 * </p>
 * 
 * <p>
 * Complex property definition contains one or more 
 * property definitions and represents a group of (usually) common property
 * definitions among several or all content object types. Therefore,
 * implementation of this interface should permit the definition of a complex
 * property as a stand alone definition which can be referred by
 * any content object type definition or another complex property
 * definition.
 * </p>
 * 
 * <p>
 * Astroboa implementation uses a stand alone XML schema <code>complexType</code>
 * element which must extend Astroboa complex xml type <code>complexCmsProperty</code> 
 * to define a complex property.
 * 
 * <pre>
 *&lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
 * 		targetNamespace="http://www.betaconceptframework.org/schema/astroboa/web/statisticType"
 *		xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"&gt;
 *	
 *	&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/model"
 *		schemaLocation="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd" /&gt;
 *
 *	&lt;xs:complexType {@link CmsDefinition#getName() name}=&quot;statistic&quot;
 *			&lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *				&lt;xs:documentation xml:lang=&quot;en&quot;&gt;Content Object Statistics&lt;/xs:documentation&gt;
 *			&lt;/xs:annotation&gt;
 *			&lt;xs:complexContent&gt;
 *				&lt;xs:extension base="bccmsmodel:complexCmsProperty"&gt;
 *					&lt;xs:sequence&gt;
 *						&lt;xs:element {@link CmsDefinition#getName() name}=&quot;viewCounter&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;0&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot; 
 *        					{@link CmsDefinition#getValueType() type}=&quot;xs:long&quot; &gt;
 *							&lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *								&lt;xs:documentation xml:lang=&quot;en&quot;&gt;View Counter&lt;/xs:documentation&gt;
 *							&lt;/xs:annotation&gt;
 *						&lt;/xs:element&gt;
 *					&lt;/xs:sequence&gt;
 *				&lt;/xs:extension&gt;
 *			&lt;/xs:complexContent&gt;
 *	&lt;/xs:complexType&gt;
 *&lt;/xs:schema&gt;
 * </pre>
 * 
 * and in order to define it in terms of a content object type definition an XML
 * Schema <code>element</code> is used
 * 
 * <pre>
 *	&lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *		xmlns:administrativeMetadataType="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"&gt;
 *	
 *		&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"
 *		schemaLocation="administrativeMetadataType-3.0.0.GA.xsd" /&gt;
 *	
 *		&lt;xs:element name="newsItem"&gt;
 *			&lt;xs:annotation&gt;
 *				&lt;xs:documentation xml:lang="en" &gt;News Item&lt;/xs:documentation&gt;
 *			&lt;/xs:annotation&gt;
 *			&lt;xs:complexType&gt;
 *				&lt;xs:complexContent&gt;
 *					&lt;xs:extension base="bccmsmodel:contentObject"&gt;
 *						&lt;xs:sequence&gt;
 *    						&lt;xs:element {@link CmsDefinition#getName() name}=&quot;statistic:statistic&quot; type=&quot;statistic&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;1&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot;/&gt;
 *    					&lt;/xs:sequence&gt;
 *	   				&lt;/xs:extension&gt;
 *	   			&lt;/xs:complexContent&gt;
 *	   		&lt;/xs:complexType&gt;
 * 	&lt;/xs:element&gt;
 *&lt;/xs:schema&gt;
 * </pre>
 * 
 * The above xml schemas define a content object type definition named
 * <code>newsItem</code> which contains a complex property named
 * <code>statistic</code> which by its turn contains a simple property named
 * <code>viewCounter</code>.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 * @see <a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">Astroboa model XML schema
 * for more on <code>complexCmsProperty</code> complex type. </a>
 * 
 */
public interface ComplexCmsPropertyDefinition extends CmsPropertyDefinition {

	/**
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all child
	 * property definitions of complex property. 
	 * 
	 * Map's key is complex property definition's
	 * {@link CmsDefinition#getName() name}.
	 * 
	 * @return Child property definitions map.
	 */
	Map<String, CmsPropertyDefinition> getChildCmsPropertyDefinitions();

	/**
	 * <p>
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all child
	 * property definitions of complex property, sorted by definition's 
	 * {@link CmsPropertyDefinition#getOrder() order} and THEN
	 * by localized labels for specified locale.
	 *</p>
	 *
	 *<p>
	 *	Sorting order is ascending and it is not configurable.
	 *</p>
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null),
	 *            {@link Locale#ENGLISH English} locale will be
	 *            used.
	 * @return Child property definitions map sorted by definition's order and localized labels.
	 */
	Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(
			String locale);
	
	/**
	 * <p>
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all child
	 * property definitions of complex property, sorted by definition's 
	 * {@link CmsPropertyDefinition#getOrder() order}  and THEN by 
	 * {@link CmsDefinition#getValueType() value type} and finally by localized labels by specified locale.
	 * </p>
	 * 
 	 *<p>
	 *	Sorting order is ascending and it is not configurable.
	 *</p>
	 *
	 * <p>
	 * {@link ValueType ValueType} comparison is performed by the following order,
	 * where the first in the list, is considered greater than the others. This order
	 * is not configurable.
	 *  <ul>
	 *  <li>{@link SimpleCmsPropertyDefinition simple property definitions}
	 *  <li>{@link BinaryPropertyDefinition binary definitions}
	 *  <li>{@link ComplexCmsPropertyDefinition complex property definitions}
	 *  </ul>
	 *  </p>
	 *  
	 *  <p>
	 *  For properties of the same type,  ordering is performed according to their
	 *  localized labels.
	 * 	</p>
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null),
	 *            {@link Locale#ENGLISH English} locale will be
	 *            used.
	 * @return Child property definitions map sorted by definition's order and localized labels.
	 */
	Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(
			String locale);

	/**
	 * Returns child property definition for the specified
	 * <code>childCmsPropertyPath</code>.
	 * 
	 * Path is relative to this definition.
	 * 
	 * @param childCmsPropertyPath
	 *            A period-delimited string describing the path to the child property starting 
	 *            from this definition.
	 *            
	 * @return Child property definition or <code>null</code> if none has been found.
	 */
	CmsPropertyDefinition getChildCmsPropertyDefinition(
			String childCmsPropertyPath);

	/**
	 * Check if there are any child property definitions at all.
	 * 
	 * @return <code>true</code> if there is at least one child property
	 *         definition, <code>false</code> otherwise.
	 */
	boolean hasChildCmsPropertyDefinitions();

	/**
	 * Checks if there is a child property definition for the specified path.
	 * 
	 * Path is relative to this definition.
	 * 
	 * @param childCmsPropertyPath
	 *            A period-delimited string describing the path to the child property starting 
	 *            from this definition.
	 *            
	 * @return <code>true</code> if a child property definition exists for the
	 *         specified path, <code>false</code> otherwise.
	 */
	boolean hasChildCmsPropertyDefinition(String childCmsPropertyPath);
	
	/**
	 * Returns a comma delimited string which contains one or more 
	 * {@link SimpleCmsProperty simple property} paths whose
	 * value can be used as a label for {@link ComplexCmsProperty complex property}
	 * of this definition instead of its system name or its display name which is provided
	 * in the annotation tag in its schema. 
	 * 
	 * @return Value provided in XML schema for attribute
	 * bccsmodel:labelElementPath, <code>null</code> if no value is provided
	 */
	String getPropertyPathsWhoseValuesCanBeUsedAsALabel();

	@Deprecated
	String getPropertyPathWhoseValueCanBeUsedAsALabel();
	
	/**
	 * Checks if this definition represents a system complex type.
	 * 
	 * <p>
	 * System types are used mainly by Astroboa infrastructure. 
	 * </p>
	 * 
	 * @return <code>true</code> if this definition is a system one, <code>false</code> otherwise
	 */
	boolean isSystemTypeDefinition();

	/**
	 * Checks if this definition is a global one, that is can be attached at runtime to any
	 * content object.
	 * 
	 * @return <code>true</code> if this definition can be used as an aspect, <code>false</code> otherwise
	 */
	boolean isGlobal();

	/**
	 * Get the number of levels of the definition hierarchy tree.
	 * 
	 * The depth is zero based, 0 stands for the current definition. 
	 * 
	 * The depth of a {@link ComplexCmsPropertyDefinition} is augmented by one if the definition
	 * has at least one child {@link ComplexCmsPropertyDefinition} or {@link BinaryPropertyDefinition}.
	 * A {@link BinaryPropertyDefinition} is considered a complex property in the sense that it contains
	 * several internal properties beside the actual binary value.
	 * 
	 * @return Number of the levels of the definition hierarchy tree.
	 */
	int getDepth();
}
