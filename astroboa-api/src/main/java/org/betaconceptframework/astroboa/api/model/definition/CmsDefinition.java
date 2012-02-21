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

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor;


/**
 * CmsDefinition is the base interface for content
 * definition entities specific to organization's content model.
 * 
 * <p>
 * It merely specifies that each content definition entity holds a system
 * name of the content attribute that it models/defines (e.g. article,
 * articleBody, articleImage) and specifies the {@link ValueType type} 
 * as well as description for the modeled attribute.
 * </p>
 * 
 * <p>
 *  Furthermore, it may be the case that a content definition entity
 *  contains other content definition entities, therefore support
 *  for Visitor pattern is available.
 * </p>  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public interface CmsDefinition {

	/**
	 * Returns the system name of the modeled attribute.
	 * Must be a valid XML name
	 * according to XML Namespaces recommendation [4] (http://www.w3.org/TR/REC-xml-names)
	 * 
	 * @return Modeled attribute's system name.
	 */
	String getName();

	/**
	 * Method to support Visitor pattern.
	 * 
	 * @param visitor
	 *            Visitor for content definition entity.
	 */
	void accept(DefinitionVisitor visitor);

	/**
	 * Returns the value type of this definition.
	 * 
	 * @return Definition's value type.
	 */
	ValueType getValueType();
	
	/**
	 * Qualified name of this definition.
     *
	 * @return Qualified name of definition.
	 */
	QName getQualifiedName();
	
	/**
	 * URL location responsible to serve
	 * definition in the specified representation.
	 * 
	 * @param resourceRepresentation Definition representation. Default is {@link ResourceRepresentationType#XSD}.
	 * 
	 * @return URL location 
	 */
	String url(ResourceRepresentationType<?> resourceRepresentation);
	
	/**
	 * Provides the XML Schema associated with this definition.
	 * 
	 * @return XML Schema for this definition.
	 */
	String xmlSchema();

	
	/**
	 * Provides an xml representation of this definition.
	 * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding indentation and line feeds in order to make output more human readable, <code>false<code> otherwise
	 * 
	 * @return XML instance for this definition.
	 */
	String xml(boolean prettyPrint);

	/**
	 * Provides a JSON representation of this definition.
	 * 
     * The natural JSON notation
     * <p>Example JSON expression:<pre>
     * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
     * </pre>
     * </p>
     * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding indentation and line feeds in order to make output more human readable, <code>false<code> otherwise
	 * 
	 * @return JSON representation for this definition
	 */
	String json(boolean prettyPrint);

}
