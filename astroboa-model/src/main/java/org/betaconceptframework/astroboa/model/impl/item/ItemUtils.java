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


import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.model.impl.ItemQName;

/**
 * Utility class used for creating qualified name for
 * an item in repository.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class ItemUtils {

	public static ItemQName createNewBetaConceptItem(String localPart) {
		return createNewItem(BetaConceptNamespaceConstants.ASTROBOA_PREFIX,
				BetaConceptNamespaceConstants.ASTROBOA_URI, localPart);
	}

	public static ItemQName createNewItem(String prefix, String url,
			String localPart) {
		return (ItemQName) new ItemQNameImpl(prefix, url, localPart);
	}

	public static ItemQName createSimpleItem(String localPart) {
		return createNewItem("", "", localPart);
	}
}
