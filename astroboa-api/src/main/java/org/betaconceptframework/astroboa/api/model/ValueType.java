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

import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;

/**
 * All possible types of a {@link CmsDefinition definition}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum ValueType {

	/**
	 * Specifies a definition of type <code>ContentType</code>.
	 */
	ContentType,
	/**
	 * Specifies a definition of type <code>Complex</code>.
	 */
	Complex,
	/**
	 * Specifies a definition of type <code>String</code>.
	 */
	String,
	/**
	 * Specifies a definition of type <code>Date</code>.
	 */
	Date,
	/**
	 * Specifies a definition of type <code>Double</code>.
	 */
	Double,
	/**
	 * Specifies a definition of type <code>Long</code>.
	 */
	Long,
	/**
	 * Specifies a definition of type <code>Boolean</code>.
	 */
	Boolean,

	/**
	 * Specifies a definition of type <code>ObjectReference</code>.
	 * 
	 */
	ObjectReference,
	/**
	 * Specifies a definition of type <code>TopicReference</code>.
	 */
	TopicReference,
	/**
	 * Specifies a definition of type <code>Binary</code>.
	 */
	Binary;

}
