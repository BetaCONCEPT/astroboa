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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.SESSION)
@Name("userFavorites")
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UserFavorites {
	
	@Out(scope=ScopeType.SESSION)
	private List<ContentObject> favoriteContentObjectList = new ArrayList<ContentObject>();
	
	public void addFavoriteContentObject(ContentObject contentObject) {
		if (contentObject != null && favoriteContentObjectList.indexOf(contentObject) == -1) {
			favoriteContentObjectList.add(contentObject);
		}
	}
	
	
	public void removeFavoriteContentObject(ContentObject contentObject) {
		if (contentObject != null) {
			favoriteContentObjectList.remove(contentObject);
		}
	}
}
