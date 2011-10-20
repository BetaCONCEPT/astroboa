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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;

/**
 * Entry point for defining organization's content type.
 * 
 * <p>
 * Astroboa content repository provides a high level content model upon which all
 * different content types of an organization can be defined. The content model
 * defines a set of core build-in content modeling
 * {@link CmsRepositoryEntity concepts/entities} for describing the content and
 * its users, and providing multiple levels of content organization and
 * categorization. All the provided entities have concrete definitions, except
 * central entity {@link ContentObject} whose definition is abstract, allowing
 * any type of organization content (e.g. blog entry, article, press release,
 * museum artifact, curriculum vitae, web page, office document, video, audio,
 * etc.) to be ultimately described.
 * </p>
 * <p>
 * For the purpose of extending the abstract
 * {@link ContentObject content object} entity, Astroboa specifies a set of Java
 * interfaces which allow code developers or content modelers to properly define
 * organization's content types.
 * </p>
 * 
 * <p>
 * The basic idea is that organization's content can be categorized into several
 * basic content object types and each content object type has one or 
 * more {@link CmsPropertyDefinition properties}. A property can either 
 * be a {@link ComplexCmsProperty complex property}, that is, a property which contains
 * other properties or a {@link SimpleCmsProperty simple property}, that is, a property which
 * contains simple values like {@link String strings}, 
 * {@link Calendar dates}, etc. 
 * 
 * <p>
 * The way definitions of content object types and their properties can be 
 * described by a code developer or a content modeler depends on the 
 * implementation of this set of Java interfaces.
 * </p>
 * 
 * <p>
 * Astroboa implementation uses XML Schema to describe content object types 
 * and their properties, thus storing content types definition information
 * into XML schema definition files (XSD). XML Schema provides a powerful 
 * set of tools to define XML structure and content and these tools are used
 * in order to define organization's content structure. 
 * </p>
 * <p>
 * <ul>
 * <li>XML Schema elements are used to define content object types and
 * their properties.
 * <li>XML Schema attributes are used to define properties attributes, like
 * type, default value, etc.
 * </ul>
 * </p>
 * 
 * <pre>
 * &lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;
 *		xmlns:administrativeMetadataType="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"&gt;
 *	
 *		&lt;xs:import
 *		namespace="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"
 *		schemaLocation="administrativeMetadataType-3.0.0.GA.xsd" /&gt;
 *	
 * &lt;xs:element {@link CmsDefinition#getName() name}=&quot;article&quot; 
 *   &lt;xs:complexType&gt;
 *    &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *     &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Article&lt;/xs:documentation&gt;
 *    &lt;/xs:annotation&gt;
 *    &lt;xs:sequence&gt;
 *     &lt;xs:element {@link CmsDefinition#getName() name}=&quot;profile&quot; {@link CmsDefinition#getValueType() type}=&quot;profile:profile&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;1&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot;/&gt;
 *     &lt;xs:element {@link CmsDefinition#getName() name}=&quot;articleBody&quot; {@link CmsDefinition#getValueType() type}=&quot;xs:string&quot; {@link CmsPropertyDefinition#isMandatory() minOccurs}=&quot;1&quot; {@link CmsPropertyDefinition#isMultiple() maxOccurs}=&quot;1&quot;
 *      &lt;{@link LocalizableCmsDefinition xs:annotation}&gt;
 *        &lt;xs:documentation xml:lang=&quot;en&quot;&gt;Article Body&lt;/xs:documentation&gt;
 *      &lt;/xs:annotation&gt;
 *     &lt;/xs:element&gt;
 *    &lt;/xs:sequence&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 *&lt;/xs:schema&gt;
 *</pre>
 * 
 * <p>
 * Above excerpt from an XML schema definition file describes a 
 * content type named<code>article</code> which contains 
 * two properties
 * <ul>
 * <li> a {@link ComplexCmsPropertyDefinition complex property} 
 * named <code>profile</code>
 * <li> a {@link StringPropertyDefinition string property} 
 * named <code>articleBody</code>
 * </ul>
 *  
 *  For further details follow provided links to learn 
 *  how to properly define a content type.
 * </p>
 * 
 * <p>
 * Bear in mind that content type's name as defined in
 * {@link CmsDefinition#getName()} must be unique.
 * </p>
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public interface ContentObjectTypeDefinition extends
		LocalizableCmsDefinition {

	/**
	 * Returns a map of definitions of all properties of a content object type.
	 * Map's key is property definition's {@link CmsDefinition#getName() name}.
	 * 
	 * @return Map of content object property definitions.
	 */
	Map<String, CmsPropertyDefinition> getPropertyDefinitions();

	/**
	 * <p>
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all child
	 * property definitions of complex property, sorted by definition's 
	 * {@link CmsPropertyDefinition#getOrder() order} and THEN
	 * by localized labels for specified locale.
	 *</p>
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null),
	 *            {@link Locale#ENGLISH English} locale will be
	 *            used.
	 * @return Child property definitions map sorted by definition's order and localized labels.
	 * @see ComplexCmsPropertyDefinition#getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(String)
	 */
	Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(String locale);
	
	/**
	  * <p>
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all child
	 * property definitions of complex property, sorted by definition's 
	 * {@link CmsPropertyDefinition#getOrder() order}  and THEN by 
	 * {@link CmsDefinition#getValueType() value type} and finally by localized labels by specified locale.
	 * </p>
	 * 
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank (empty or null),
	 *            {@link Locale#ENGLISH English} locale will be
	 *            used.
	 * @return Child property definitions map sorted by definition's order and localized labels.
	 * 
	 * @see ComplexCmsPropertyDefinition#getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(String)
	 */
	Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(
			String locale);

	/**
	 * Returns the definition for a property specified for the
	 * <code>cmsPropertyPath</code>.
	 * 
	 * @param cmsPropertyPath
	 *            A period-delimited string defined in 
	 *            {@link CmsPropertyDefinition#getPath()}.
	 *            
	 * @return Property definition or <code>null</code> if none has been found.
	 */
	CmsPropertyDefinition getCmsPropertyDefinition(String cmsPropertyPath);

	/**
	 * Check if there are any property definitions at all.
	 * 
	 * @return <code>true</code> if there is at least one property definition,
	 *         <code>false</code> otherwise.
	 */
	boolean hasCmsPropertyDefinitions();

	/**
	 * Checks if there is a property definition for the specified path.
	 * 
	 * @param cmsPropertyPath
	 *            A period-delimited string defined in
	 *            {@link CmsPropertyDefinition#getPath()}.
	 *            
	 * @return <code>true</code> if a property definition exists for the
	 *         specified path, <code>false</code> otherwise.
	 */
	boolean hasCmsPropertyDefinition(String cmsPropertyPath);
	
	/**
	 * Indicates whether this content type is of the specified content type.
	 * 
	 * <p>
	 * According to Astroboa XSD support, in order to define than a content type
	 * extends another content type, content modeler should provide the following
	 * xml elements and complex types.
	 * </p>
	 * 
	 * <p>Base type</p>
	 * <pre>
	 *
	 *    &lt;xs:complexType name="personType"&gt;	
	 *    		&lt;xs:complexContent&gt;
	 *			  &lt;xs:extension base="bccmsmodel:contentObjectType"&gt;
	 *				&lt;xs:sequence&gt;
	 *				...
	 *				&lt;xs:sequence&gt;
	 *			  &lt;/xs:extension&gt;
	 *			&lt;/xs:complexContent&gt;
	 *    &lt;/xs:complexType&gt;
	 * </pre>
	 * </p>
	 * 
	 * <p>Sub type</p>
	 * <pre>
	 *
	 *    &lt;xs:element name="employee"&gt;	
	 *    		&lt;xs:complexContent&gt;
	 *			  &lt;xs:extension base="tns:personType"&gt;
	 *				&lt;xs:sequence&gt;
	 *				...
	 *				&lt;xs:sequence&gt;
	 *			  &lt;/xs:extension&gt;
	 *			&lt;/xs:complexContent&gt;
	 *    &lt;/xs:complexType&gt;
	 * </pre>
	 * 
	 * <p>
	 *  With the above setup, calling this method on <code>employee</code>
	 *  definition <code>isTypeOf("personType")</code> will return true
	 * </p>
	 *
	 * @param superType The name of the super content type
	 * 
	 * @return <code>true</code> if this content type extends the specified content type, <code>false</code> otherwise
	 */
	boolean isTypeOf(String superType);
	
	/**
	 * Retrieve all super types (if any) of this content type
	 * 
	 * @return A list of all super types. 
	 */
	List<String> getSuperContentTypes();

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
	
	/**
	 * Returns a comma delimited string which contains one or more 
	 * {@link SimpleCmsProperty simple property} paths whose
	 * value can be used as a label for an {@link ContentObject object}
	 * of this definition instead of its system name or its display name which is provided
	 * in the annotation tag in its schema. 
	 * 
	 * @return Value provided in XML schema for attribute
	 * bccsmodel:labelElementPath, <code>null</code> if no value is provided
	 */
	String getPropertyPathsWhoseValuesCanBeUsedAsALabel();

}
