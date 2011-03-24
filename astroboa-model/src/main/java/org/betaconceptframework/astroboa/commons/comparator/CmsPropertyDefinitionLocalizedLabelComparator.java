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


package org.betaconceptframework.astroboa.commons.comparator;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.util.CmsUtils;

/**
 * Compare Property Definitions by their localized label.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyDefinitionLocalizedLabelComparator implements Comparator<CmsPropertyDefinition>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2308549698097336106L;
	
	private String locale;
	
	public CmsPropertyDefinitionLocalizedLabelComparator()
	{
		
	}

	public void setLocale(String locale) {
		if (StringUtils.isBlank(locale))
			this.locale = Locale.ENGLISH.toString();
		else
			this.locale = locale;
	}

	public int compare(CmsPropertyDefinition propertyDefinition1,	CmsPropertyDefinition propertyDefinition2) {
			
		if (locale == null)
			setLocale(null);
		
		if (propertyDefinition1 == null)
			return -1;
		
		if (propertyDefinition2 == null)
			return 1;
		
		return compareLocalizedNames(propertyDefinition1.getDisplayName().getLocalizedLabelForLocale(locale), propertyDefinition2.getDisplayName().getLocalizedLabelForLocale(locale));
	}
	
	private int compareLocalizedNames(String localizedLabel0,String localizedLabel1) {
		
		if (localizedLabel0 == null && localizedLabel1 == null)
			return 0;
		
		if (localizedLabel0 != null && localizedLabel1 == null)
			return 1;
		
		if (localizedLabel0 == null && localizedLabel1 != null)
			return -1;
		
		//We need ascending order
		if ("el".equalsIgnoreCase(locale))
		{
			//Filter string to lower case and transforming accented characters to simple ones
			String lowerCaseGreekFilteredLocalizedLabel0= CmsUtils.filterGreekCharacters(localizedLabel0);
			String lowerCaseGreekFilteredLocalizedLabel1= CmsUtils.filterGreekCharacters(localizedLabel1);
			
			return compareStrings(lowerCaseGreekFilteredLocalizedLabel0,
					lowerCaseGreekFilteredLocalizedLabel1);
		}
		else
			return compareStrings(localizedLabel0, localizedLabel1);
	}

	//Since string comparison may produce int other than 0,1, or -1
	//make sure only these outcomes are generated
	private int compareStrings(String string0,
			String string1) {
		int compareTo = string0.compareTo(string1);
		return (compareTo > 0 ? 1 :
				(compareTo< 0 ? -1 : 0)
				);
	}
	
	 
}
