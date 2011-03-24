/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.api.model.io;



/**
 * Represents how deep a loading operation or serialization will go.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum FetchLevel {

	/**
	 * Fetch an entity's basic attributes. 
	 * 
	 * When used in the context of loading or serializing an object, 
	 * it instructs the engine to load only the basic built in properties 
	 * of an object (id, systemName, etc).
	 * 
	 * When used in the context of loading or serializing a taxonomy or topic, it instructs
	 * the engine to load only their basic attributes (id, name, etc). 
	 */
	ENTITY,
	
	/**
	 * Fetch entity's properties or children. 
	 * 
	 * When used in the context of loading or serializing an object, 
	 * it instructs the engine to load the specific properties provided by the user.
	 * 
	 * When used in the context of loading or serializing a taxonomy or topic, it instructs
	 * the engine to load their children as well. 
	 */
	ENTITY_AND_CHILDREN,
	
	/**
	 * Fetch all entity's properties or children. 
	 * 
	 * When used in the context of loading or serializing an object, 
	 * it instructs the engine to load all the properties of an object.
	 * 
	 * When used in the context of loading or serializing a taxonomy or topic, it instructs
	 * the engine to load all their children recursively. 
	 */
	FULL;
}
