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

package org.betaconceptframework.astroboa.model.impl.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.Localization;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LocalizationImpl implements Localization, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1991940762410660780L;
	
	private Map<String, String> localizedLabels = new HashMap<String, String>();

	public Map<String, String> getLocalizedLabels() {
		return localizedLabels;
	}

	public void clearLocalizedLabels() {
			localizedLabels.clear();
	}
	
	public void addLocalizedLabel(String locale, String localizedLabel)
	{
		if (locale != null){
			localizedLabels.put(locale, localizedLabel);
		}
	}
	
	public String getLocalizedLabelForLocale(String locale)
	{
		String localizedLabel = null;
		if (StringUtils.isBlank(locale))
			localizedLabel =  localizedLabels.get(Locale.ENGLISH.toString());
		else
			localizedLabel = localizedLabels.get(locale);
		
		return localizedLabel;
	}

	public boolean hasLocalizedLabels() {
		return MapUtils.isNotEmpty(localizedLabels);
	}

	public void setLocalizedLabels(Map<String, String> localizedLabels) {
		if (localizedLabels == null){
			clearLocalizedLabels();
		}
		else{
			this.localizedLabels = localizedLabels;
		}
	}

	@Override
	public String toString() {
		return " [localizedLabels=" + localizedLabels + "]";
	}
	
	public String getAvailableLocalizedLabel(String preferredLocale) {
		// check if there is a localized label
		if (! hasLocalizedLabels()) {
			return NO_LOCALIZED_LABEL_AVAILABLE;
		}
		
		// first try to return the label for the preferred local
		String availableLocalizedLabel = getLocalizedLabelForLocale(preferredLocale);
		
		if (StringUtils.isNotBlank(availableLocalizedLabel)) {
			return availableLocalizedLabel;
		}
		
		// try to return the label for the English locale in the case that the preferred locale was not the English one
		// if it was we already know that it does not exist
		if (!"en".equals(preferredLocale)) {
			availableLocalizedLabel = getLocalizedLabelForLocale("en");
		}
		
		if (StringUtils.isNotBlank(availableLocalizedLabel)) {
			return availableLocalizedLabel;
		}
		
		// Return the first available label
		return getLocalizedLabels().values().iterator().next();
	}
	
}
