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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizationImpl;
import org.betaconceptframework.astroboa.model.jaxb.adapter.LocalizationAdapter;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "localization"
})
public abstract class LocalizableEntityImpl extends CmsRepositoryEntityImpl  implements LocalizableEntity, Serializable{

	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = -1692081145451432792L;

	@XmlElement
	@XmlJavaTypeAdapter(value=LocalizationAdapter.class)
	private Localization localization;
	
	@XmlTransient
	private String currentLocale;
	
	
	public LocalizableEntityImpl() {
		super();
		localization = new LocalizationImpl();
	}

	public String getCurrentLocale() {
		return currentLocale;
	}

	public void setCurrentLocale(String currentLocale) {
		this.currentLocale = currentLocale;
	}

	public String getLocalizedLabelForCurrentLocale() {
		return getLocalizedLabelForLocale(currentLocale);
		
	}

	public void addLocalizedLabel(String locale, String localizedName) {
		localization.addLocalizedLabel(locale, localizedName);
	}

	public void clearLocalizedLabels() {
		localization.clearLocalizedLabels();	
	}

	public Map<String, String> getLocalizedLabels() {
		return localization.getLocalizedLabels();
	}

	public String getLocalizedLabelForLocale(String locale) {
		return localization.getLocalizedLabelForLocale(locale);
	}

	public boolean hasLocalizedLabels() {
		return localization.hasLocalizedLabels();
	}
	
	public String getAvailableLocalizedLabel(String preferredLocale) {
		return localization.getAvailableLocalizedLabel(preferredLocale);
	}


}
