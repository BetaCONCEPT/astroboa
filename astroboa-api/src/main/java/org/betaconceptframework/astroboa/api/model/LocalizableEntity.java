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

package org.betaconceptframework.astroboa.api.model;

import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * Provides localization features to an {@link CmsRepositoryEntity entity}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface LocalizableEntity extends CmsRepositoryEntity, Localization {

	/**
	 * Sets the locale value that will be used by 
	 * {@link #getLocalizedLabelForCurrentLocale()} method to return the proper localized label.
	 * 
	 * @param currentLocale 
	 * 			Locale value as defined in {@link Localization}. 
	 */
	 void setCurrentLocale(String currentLocale);
	 
	/**
	 * Returns the currently selected locale value that will be used by
	 * {@link #getLocalizedLabelForCurrentLocale()} method to return the proper localized label.
	 * 
	 * @return A {@link String} representing 
	 * 		   locale value as specified in {@link Localization}.
	 */
	String getCurrentLocale();
	
	/**
	 * Returns a localized label for this entity which corresponds to the 
	 * currently selected locale. The current locale is  specified calling method {@link #setCurrentLocale(String)}.
	 * 
	 * <p>
	 * Equivalent to calling {@link Localization#getLocalizedLabelForLocale(String)} and 
	 * setting parameter to be the outcome of {@link #getCurrentLocale()}.
	 * </p>
	 * 
	 * @return Returns a label for this entity, corresponding to current locale.
	 * 
	 * @see Localization#getLocalizedLabelForLocale(String)
	 */
	 String getLocalizedLabelForCurrentLocale();
	 
}
