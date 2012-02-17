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
package org.betaconceptframework.astroboa.portal.utility;

import java.io.Serializable;
import java.util.Comparator;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectTitleComparator implements Comparator<ContentObject>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6634023061043187277L;

	@Override
	public int compare(ContentObject contentObject1, ContentObject contentObject2) {
		
		StringProperty title1Property = (StringProperty) contentObject1.getCmsProperty("profile.title");
		StringProperty title2Property = (StringProperty) contentObject2.getCmsProperty("profile.title");
		return title1Property.getSimpleTypeValue().compareTo(title2Property.getSimpleTypeValue());
	}	


}
