/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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
package org.betaconceptframework.astroboa.engine.jcr.io;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum ImportMode {

	/**
	 * Import String to appropriate entity
	 * but do not save entity
	 */
	DO_NOT_SAVE,
	
	/**
	 * Import String to appropriate entity
	 * and save entity
	 */ 
	SAVE_ENTITY,
	
	/**
	 * Import String to appropriate entity
	 * and save entity as well as its children if any
	 * Used mainly for Topic, Taxonomy and Space
	 */ 
	SAVE_ENTITY_TREE,
}
