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



/**
 * Base interface for definition of a property of a content object type.
 * 
 * <p>
 * A property of a content object type can either be a
 * {@link SimpleCmsPropertyDefinition simple property} or a
 * {@link ComplexCmsPropertyDefinition complex property}, i.e. contains
 * one or more simple properties.
 * </p>
 * 
 * <p>
 * This interface specifies common attributes of a content object type's
 * property definition :
 * <ul>
 * <li>{@link #isMandatory() mandatory}
 * <li>{@link #isMultiple() multiple}
 * <li>{@link #isObsolete() obsolete}
 * <li>{@link #getRestrictReadToRoles() restrictReadToRoles}
 * <li>{@link #getRestrictWriteToRoles() restrictWriteToRoles}
 * <li>{@link #getOrder() order}
 * </ul>
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsPropertyDefinition extends LocalizableCmsDefinition {

	/**
	 * Checks if property is mandatory.
	 * 
	 * Astroboa implementation uses XML Schema attribute
	 * <a href="http://www.w3.org/TR/xmlschema-1/#Particle_details">minOccurs</a>
	 *  to provide value for this attribute.Pay attention to the default
	 * value <code>1<code> for this attribute as defined in XML Schema.
	 * 
	 * @return <code>true</code> if property is mandatory, <code>false</code>
	 *         otherwise.
	 */
	boolean isMandatory();



	/**
	 * Checks if property can have multiple values in case of
	 * {@link SimpleCmsPropertyDefinition} or can appear multiple times in case
	 * of {@link ComplexCmsPropertyDefinition}.
	 * 
	 * <p>
	 * Astroboa implementation uses XML Schema attribute
	 * <a href="http://www.w3.org/TR/xmlschema-1/#Particle_details">maxOccurs</a>
	 * to provide value for this attribute. Valid values for implementation are considered 
	 * <code>0</code>, <code>1</code> and <code>unbounded</code>. Pay attention to the default
	 * value <code>1<code> for this attribute as defined in XML Schema.
	 * </p>
	 * 
	 * @return <code>true</code> if property is multiple, <code>false</code>
	 *         otherwise.
	 */
	boolean isMultiple();



	/**
	 * <p>
	 * Checks if property is obsolete. If a property is obsolete then it does not
	 * appear to content object neither in read-only mode nor in write mode. 
	 * Nevertheless, its value(s) remain in content repository and can be accessed
	 * only if property seize to be obsolete.
	 * </p>
	 * 
	 * <p>
	 * In case an obsolete property becomes non-obsolete, its {@link CmsDefinition#getValueType() value type}
	 * must remain the same, otherwise it will be impossible to retrieve properly existing values. 
	 * </p>
	 * 
	 * <p>
	 * Astroboa	implementation uses attribute 
	 * <a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">obsolete</a>
	 * to provide value for this attribute.
	 * </p>
	 * 
	 * @return <code>true</code> if property is obsolete, <code>false</code>
	 *         otherwise.
	 */
	boolean isObsolete();


	/**
	 * Returns one or more roles with which have read access to this property.
	 * <p>
	 * The way roles are specified within a single string value is left to
	 * the applications built on top of Astroboa.
	 * Astroboa implementation does not take into account this value during fetching data from or 
	 * writing data to content repository. It is the responsibility of the applications to 
	 * interpret and handle this value. 
	 * <p>
	 * 
	 * <p>
	 * Astroboa implementation uses Astroboa schema attribute
	 * 	<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">restrictReadToRoles</a> 
	 * to provide value for this attribute.
	 * </p>
	 * 
	 * @return A {@link String} containing one or more roles.
	 */
	String getRestrictReadToRoles();

	/**
	 * Returns one or more roles with which have write access to this property.
	 * 
	 * <p>
	 * The way roles are specified within a single string value is left to
	 * the applications built on top of Astroboa.
	 * Astroboa implementation does not take into account this value during fetching data from or
	 * writing data to content repository. It is the responsibility of the applications to 
	 * interpret and handle this value. 
	 * <p>
	 *   
	 * <p>
	 * Astroboa implementation uses Astroboa schema attribute
	 * 	<a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">restrictWriteToRoles</a> 
	 * to provide value for this attribute.
	 * </p>
	 * 
	 * @return A {@link String} containing one or more roles.
	 */
	String getRestrictWriteToRoles();

	/**
	 * Returns parent definition.
	 * 
	 * @return Parent definition if this definition belongs to another
	 *         definition.
	 */
	CmsDefinition getParentDefinition();

	/**
	 * Returns the full path of property definition.
	 * 
	 * <p>
	 * Full path of a property definition is a period-delimited string containing all
	 * property definition names using top - bottom approach.
	 * </p>
	 * 
	 * <p>
	 * For example, check the result of this method for the various of properties 
	 * which are defined in the following schemas :  
	 * </p>
	 * 
	 * <p>
	 * XML Schema for content type <code>newsItem</code> defines a 
	 * {@link ComplexCmsPropertyDefinition complex cms property} <code>profile</code> and a 
	 * {@link StringPropertyDefinition simple cms property} <code>body</code>.
	 * </p>
	 * <pre>
	 *	&lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 *		xmlns:administrativeMetadataType="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"&gt;
	 *	
	 *		&lt;xs:import
		namespace="http://www.betaconceptframework.org/schema/astroboa/admin/administrativeMetadataType"
		schemaLocation="administrativeMetadataType-3.0.0.GA.xsd" /&gt;
	 *	
	 *		&lt;xs:element name="newsItem"&gt;
	 *			&lt;xs:annotation&gt;
	 *				&lt;xs:documentation xml:lang="en" &gt;News Item&lt;/xs:documentation&gt;
	 *			&lt;/xs:annotation&gt;
	 *			&lt;xs:complexType&gt;
	 *				&lt;xs:complexContent&gt;
   	 *					&lt;xs:extension base="bccmsmodel:contentObject"&gt;
	 *						&lt;xs:sequence&gt;
	 *							&lt;xs:element name="profile" type="profile:profile"/&gt;
	 *							&lt;xs:element name="body"  minOccurs="0" maxOccurs="1" type="xs:string"&gt;
	 *								&lt;xs:annotation&gt;
	 *									&lt;xs:documentation xml:lang="en" &gt;Body&lt;/xs:documentation&gt;
	 *								&lt;/xs:annotation&gt;
	 *							&lt;/xs:element&gt;
	 *   						&lt;/xs:sequence&gt; 
	 *	   				&lt;/xs:extension&gt;
	 *	   			&lt;/xs:complexContent&gt;
	 *	   		&lt;/xs:complexType&gt;
	 *	 	&lt;/xs:element&gt;
	 * 	&lt;/xs:schema&gt;
	 * </pre>
	 * 
	 * XML Schema for {@link ComplexCmsPropertyDefinition complex cms property} <code>profile</code> defines a 
	 * {@link StringPropertyDefinition simple cms property} <code>creator<code>
	 * <pre>
	 * 	&lt;xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 *		xmlns:bccmsmodel="http://www.betaconceptframework.org/schema/astroboa/model"&gt;
	 *	
	 *	&lt;xs:import
	 *		namespace="http://www.betaconceptframework.org/schema/astroboa/model"
	 *		schemaLocation="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd" /&gt;
	 *	
	 *		&lt;xs:complexType name="profile"&gt;
	 *			&lt;xs:annotation&gt;
	 *					&lt;xs:documentation  xml:lang="en"&gt;Profile (Dublin Core Metadata)&lt;/xs:documentation&gt;
	 *			&lt;/xs:annotation&gt;
	 *		&lt;xs:complexContent&gt;
   	 *			&lt;xs:extension base="bccmsmodel:complexCmsProperty"&gt;
	 *				&lt;xs:sequence&gt;
	 *  				&lt;xs:element name="creator" maxOccurs="unbounded" minOccurs="0"	 type="xs:string"&gt;
	 *						&lt;xs:annotation&gt;
	 *							&lt;xs:documentation  xml:lang="en"&gt;Creator&lt;/xs:documentation&gt;
	 *						&lt;/xs:annotation&gt;
	 *					&lt;/xs:element&gt;
	 *					&lt;/xs:sequence&gt; 
	 *	   			&lt;/xs:extension&gt;
	 *	   		&lt;/xs:complexContent&gt;
	 *	   	&lt;/xs:complexType&gt;
	 * 	&lt;/xs:schema&gt;
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Astroboa implementation will create the following definition instances according to the above XML schemas:
	 * 
	 * <ul>
	 * <li>	A {@link ContentObjectTypeDefinition definition} for content type <code>newsItem</code>.
	 * <li>	A {@link ComplexCmsPropertyDefinition definition} for <code>newsItem</code>'s child definition <code>profile</code>. This definition
	 * is a clone of the  {@link ComplexCmsPropertyDefinition definition} of global complex property <code>profile</code>. Calling method {@link #getFullPath()}
	 * on this instance, will result to '<code>newsItem.profile</code>'.
	 * <li>	A {@link StringPropertyDefinition definition} for <code>newsItem</code>'s child definition <code>body</code>.Calling method {@link #getFullPath()}
	 * on this instance, will result to '<code>newsItem.body</code>'.
	 * <li>	A {@link ComplexCmsPropertyDefinition definition} for global complex property <code>profile</code>.Calling method {@link #getFullPath()}
	 * on this instance, will result to '<code>profile</code>'. This is because this instance represents the global complex property profile.
	 * <li>	A {@link StringPropertyDefinition definition} for <code>profile</code>'s child definition <code>creator</code>.Calling method {@link #getFullPath()}
	 * on this instance, will result to '<code>profile.creator</code>'.
	 * </p>
	 * 
	 * @return A period-delimited string describing property
	 *         definition path from root definition.
	 */
	String getFullPath();

	/**
	 * Same semantics with {@link #getFullPath()} but without root
	 * definition's name.
	 * 
	 * <p>
	 * This method provides definition's path relative to root definition, i.e.
	 * to a {@link ContentObjectTypeDefinition content type definition} or a global
	 * {@link ComplexCmsPropertyDefinition complex cms definition}.
	 * </p>
	 * 
	 *  @return A period-delimited string describing property
	 *         definition path.
	 */
	String getPath();
	
	/**
	 * Returns the localized path of property definition
	 * entity.
	 * 
	 * <p>
	 * Its semantics are the same with {@link #getPath()} but it returns the
	 * localized label defined for the specified locale,
	 * instead of property definition's name.
	 * </p>
	 * 
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank,
	 *            {@link Locale#ENGLISH English} localewill be  used.
	 *            
	 * @return A period-delimited string describing property
	 *         definition localized label path.
	 * 
	 */
	String getLocalizedLabelOfPathforLocale(String locale);
	
	/**
	 * Returns the full localized path of property definition
	 * entity.
	 * 
	 * <p>
	 * Its semantics are the same with {@link #getFullPath()} but it returns the
	 * localized label defined for the specified locale,
	 * instead of property definition's name.
	 * </p>
	 * 
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}. In case
	 *            where <code>locale</code> is blank,
	 *            {@link Locale#ENGLISH English} localewill be  used.
	 *            
	 * @return A period-delimited string describing property
	 *         definition localized label path from root definition.
	 * 
	 */
	String getLocalizedLabelOfFullPathforLocale(String locale);

	/**
	 * Returns the order of appearance in a sorted list.
	 * 
	 * This value is used by methods
	 * {@link ComplexCmsPropertyDefinition#getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(String)} and 
	 * {@link ComplexCmsPropertyDefinition#getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(String)}.
	 * <p>
	 * Astroboa	implementation uses attribute 
	 * <a href="http://www.betaconceptframework.org/schema/astroboa/astroboa-model-{version}.xsd">order</a>
	 * to provide value for this attribute.
	 * </p>
	 * 
	 * 
	 * @return An integer specifying the order
	 */
	Integer getOrder();

}
