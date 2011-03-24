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
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.util.Comparator;

import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.commons.comparator.CmsPropertyDefinitionComparator;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyWrapperComparator  implements Comparator<CmsPropertyWrapper>{

	private CmsPropertyDefinitionComparator<CmsPropertyDefinition> cmsPropertyDefinitionComparator;
	
	
	public CmsPropertyWrapperComparator(String locale) {
		cmsPropertyDefinitionComparator = new CmsPropertyDefinitionComparator<CmsPropertyDefinition>();
		cmsPropertyDefinitionComparator.setLocale(locale);
		cmsPropertyDefinitionComparator.setCompareByValueType(false);
	}


	@Override
	public int compare(CmsPropertyWrapper cmsPropertyWrapper1, CmsPropertyWrapper cmsPropertyWrapper2) {
		
		if (cmsPropertyWrapper1 == null || 
				cmsPropertyWrapper1.getCmsPropertyDefinition() == null)
			return -1;
		
		if (cmsPropertyWrapper2 == null ||
				cmsPropertyWrapper2.getCmsPropertyDefinition() == null)
			return -1;
		
		return cmsPropertyDefinitionComparator.compare(cmsPropertyWrapper1.getCmsPropertyDefinition(), 
				cmsPropertyWrapper2.getCmsPropertyDefinition());

	}
}
