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

package org.betaconceptframework.astroboa.api.model;

import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;

/**
 * {@link ContentObject Content object}'s property container. It is the root {@link ComplexCmsProperty complex cms property}
 * which contains all properties for a content object.
 * 
 * <p>
 * A content object contains properties defined by its {@link ContentObjectTypeDefinition type} and
 * may contain complex properties which are not defined by its type but rather were injected at runtime and have
 * a special meaning for the specific content object instance. These properties are called <code>aspects</code>.
 * </p>
 * 
 * <p>
 *  For any <code>aspect</code> loaded in content object, this class maintains <code>aspect</code> definition, therefore the following methods
 *  should be overridden by implementation to ensure consistency
 *  
 *  <ul>
 *  <li>{@link ComplexCmsProperty#isChildPropertyDefined(String)}
 *  </ul>
 * </p>
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ComplexCmsRootProperty extends
		ComplexCmsProperty<ComplexCmsPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> {

	/**
	 * Returns a map of {@link CmsPropertyDefinition definitions} of all <code>aspect</code>
	 * of a content object.
	 * 
	 * Map's key is <code>aspect</code> definition's
	 * {@link CmsDefinition#getName() name}.
	 * 
	 * @return Aspect property definitions map.
	 */
	Map<String, ComplexCmsPropertyDefinition> getAspectDefinitions();
	
	/**
	 * Overrides method {@link ComplexCmsProperty#isChildPropertyDefined(String)}
	 * and searches aspect definitions as well.
	 * 
	 * @param propertyPath
	 *            A period-delimited {@link String} as described in
	 *            {@link CmsPropertyDefinition#getPath()}. In case of aspect definitions
	 *            value for this property is described in
	 *            {@link CmsPropertyDefinition#getFullPath()}
	 * @return <code>true</code> if property has been defined for this content object
	 *         root property, <code>false</code> otherwise.
	 */
	@Override
	boolean isChildPropertyDefined(String propertyPath);

	/**
	 * Return a list of <code>aspect</code> names that this content object contains. 
	 * 
	 * 
	 * @return A list of <code>aspect</code> names.
	 */
	List<String> getAspects();

	/**
	 * Checks if content object contains the specified <code>aspect</code>.
	 * 
	 * @param aspect
	 *            Aspect system-wise name.
	 * @return <code>true</code> if <code>aspect</code> has been injected to this content
	 *         object, <code>false</code> otherwise.
	 */
	boolean hasAspect(String aspect);

}
