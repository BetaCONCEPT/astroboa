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

package org.betaconceptframework.astroboa.api.model.definition;

import java.util.Locale;
import java.util.Map;

/**
 * Provides methods for storing
 * and retrieving localized labels for specific locales.
 * 
 * <p>
 * {@link String String} is used to represent locale, thus 
 * allowing implementation to define pattern for its value, 
 * like an ISO country code, or web browser's locale 
 * string representation, or {@link java.util.Locale#toString()} outcome, 
 * or accepted values from XML Schema attribute 
 * 
 * <a href="http://www.w3.org/TR/xml11/#sec-lang-tag">lang</a>.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface Localization {
	
	/**
	 * Value returned by method {@link #getAvailableLocalizedLabel(String)} when 
	 * no localized label is available.
	 */
	public static final String NO_LOCALIZED_LABEL_AVAILABLE = "no localized label available";

	/**
	 * Returns all localized labels per locale.
	 * Map's key is locale and map's value is localized label for this locale.
	 * 
	 * ALWAYS not null.
	 * 
	 * @return Localized labels map
	 */
	Map<String, String> getLocalizedLabels();

	/**
	 * Clears all existing localized labels.
	 */
	void clearLocalizedLabels();
	
	/**
	 * Adds a localized label for the specified locale.
	 * In case localized label already exists for locale
	 * it will be replaced with the new value.
	 * 
	 * @param locale Locale value. <code>null</code> value will be ignored
	 * @param localizedLabel Localized label corresponding to 
	 * specified <code>locale</code>.
	 */
	void addLocalizedLabel(String locale, String localizedLabel);
	
	/**
	 * Returns the localized label for the given locale.
	 * 
	 * If a blank <code>locale</code> is provided the default value is
	 * {@link Locale#ENGLISH English} locale.
	 *  
	 * @param locale Locale value.
	 * 
	 * @return localized label for the specified <code>locale</code>
	 *  or null if none exists.
	 */
	String getLocalizedLabelForLocale(String locale);
	
	/**
	  * Checks if any localized label has been defined.
	  * 
	 * @return <code>true</code> if there is at least one localized label, <code>false<code> otherwise.
	 */
	boolean hasLocalizedLabels();
	
	/**
	 * Finds and returns a localized label for this entity, giving priority to the preferred locale.
	 * If no localized label is available it returns the English string: {@link #NO_LOCALIZED_LABEL_AVAILABLE}
	 * If a localized label is available for the provided preferred locale it returns it.
	 * Otherwise it checks if a localized label is available for the English language (locale=en).
	 * If none of the above succeeds then it returns the first available localized label. 
	 * 
	 * @param preferredLocale 
	 * 			The preferred locale (as specified in {@link Localization}) to check first for a localized label before it looks for alternatives. 
	 * 
	 * @return the localized label that corresponds either to the preferred locale or to an alternative locale, according to the above selection algorithm 
	 */
	 String getAvailableLocalizedLabel(String preferredLocale);

}
