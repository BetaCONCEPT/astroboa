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

package org.betaconceptframework.astroboa.model.impl;


/**
 * Represents a qualified name of an item in the repository (a node or a property).
 * 
 * Its semantics are the same with {@link javax.xml.namespace.QName QName}.
 *
 * Its use is for exposing the following methods in an interface in order to 
 * create several enumerations of builtin Items which must implement this interface.
 * 
 * {@link javax.xml.namespace.QName QName} is a concrete class and not an interface
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ItemQName {

	/**
	 * Returns a string representation of ItemQName which includes
	 * its prefix if any and its local part.
	 * @return  (ItemQName prefix + ":")? + ItemQName local part.
	 */
	String getJcrName();

	/**
	 * Returns the local part of item.
	 * @return ItemQName's local part.
	 */
	String getLocalPart();

	/**
	 * Returns the namespace URI of item.
	 * @return ItemQName's namespace URI.
	 */
	String getNamespaceURI();
	
	/**
	 * Returns the prefix of item.
	 * @return ItemQName's prefix.
	 */
	String getPrefix();
}
