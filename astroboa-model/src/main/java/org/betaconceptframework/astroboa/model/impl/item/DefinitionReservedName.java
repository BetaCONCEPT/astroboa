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

package org.betaconceptframework.astroboa.model.impl.item;

import org.betaconceptframework.astroboa.model.impl.ItemQName;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum DefinitionReservedName implements ItemQName{

	Locktoken("locktoken");

	private ItemQName reservedItem;
	
	private DefinitionReservedName(String name) {
		reservedItem = new ItemQNameImpl("", "",name);
	}
	public String getJcrName() {
		return reservedItem.getJcrName();
	}
	public String getLocalPart() {
		return reservedItem.getLocalPart();
	}
	public String getNamespaceURI() {
		return reservedItem.getNamespaceURI();
	}
	public String getPrefix() {
		return reservedItem.getPrefix();
	}
	public boolean equals(ItemQName otherItemQName) {
		return reservedItem.equals(otherItemQName);
	}
	public boolean equalsTo(ItemQName otherItemQName) {
		return equals(otherItemQName);
	}
	

}
