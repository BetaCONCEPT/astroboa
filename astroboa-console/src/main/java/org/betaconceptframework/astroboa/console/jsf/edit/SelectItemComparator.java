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
package org.betaconceptframework.astroboa.console.jsf.edit;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

import javax.faces.model.SelectItem;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SelectItemComparator  implements Comparator<SelectItem>{

	private String locale;
	
	public SelectItemComparator(String locale) {
		if (StringUtils.isBlank(locale))
			this.locale = Locale.ENGLISH.toString();
		else
			this.locale = locale;
	}


	@Override
	public int compare(SelectItem selectItem1, SelectItem selectItem2) {
		
		if (selectItem1 == null){
			return -1;
		}
		
		if (selectItem2 == null){
			return 1;
		}
		
		return compareLocalizedNames(selectItem1.getLabel(), selectItem2.getLabel());


	}
	
	private int compareLocalizedNames(String localizedLabel0,String localizedLabel1) {
		
		if (localizedLabel0 == null && localizedLabel1 == null)
			return 0;
		
		if (localizedLabel0 != null && localizedLabel1 == null)
			return 1;
		
		if (localizedLabel0 == null && localizedLabel1 != null)
			return -1;

		if (CmsConstants.LOCALE_GREEK.equalsIgnoreCase(locale)){
			//Filter string to lower case and transforming accented characters to simple ones
			//String lowerCaseGreekFilteredLocalizedLabel0= CmsUtils.filterGreekCharacters(localizedLabel0);
			//String lowerCaseGreekFilteredLocalizedLabel1= CmsUtils.filterGreekCharacters(localizedLabel1);
			//return lowerCaseGreekFilteredLocalizedLabel0.compareTo(lowerCaseGreekFilteredLocalizedLabel1);

      Collator greekCollator = Collator.getInstance(new Locale("el", "GR"));
      return greekCollator.compare(localizedLabel0, localizedLabel1);
		}
		else
			return localizedLabel0.compareTo(localizedLabel1);
	}
}
