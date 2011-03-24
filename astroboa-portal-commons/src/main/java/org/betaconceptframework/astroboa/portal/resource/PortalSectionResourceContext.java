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
package org.betaconceptframework.astroboa.portal.resource;

import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.ContentObject;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PortalSectionResourceContext extends ContentObjectResourceContext {
	
	//holds the parent portalSection objects that form a path to this section
	private List<ContentObject> portalSectionPath = new ArrayList<ContentObject>();

	public List<ContentObject> getPortalSectionPath() {
		return portalSectionPath;
	}

	public void setPortalSectionPath(List<ContentObject> portalSectionPath) {
		this.portalSectionPath = portalSectionPath;
	}
	
	public void addSectionToPortalSectionPath(ContentObject portalSection) {
		if (portalSection != null) {
			portalSectionPath.add(portalSection);
		}
	}
	
}
