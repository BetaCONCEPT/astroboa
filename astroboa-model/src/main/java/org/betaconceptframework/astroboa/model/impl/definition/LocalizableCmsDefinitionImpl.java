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
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public  abstract class LocalizableCmsDefinitionImpl extends CmsDefinitionImpl implements LocalizableCmsDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8443656736016691066L;
	
	private final Localization displayName;

	private final Localization description;

	public LocalizableCmsDefinitionImpl(QName qualifiedName, Localization description, Localization displayName) {
		
		super(qualifiedName);
		
		if (displayName == null){
			this.displayName = new LocalizationImpl();
		}
		else{
			this.displayName = displayName;
		}
		
		if (! this.displayName.hasLocalizedLabels() && description != null && description.hasLocalizedLabels()){
			this.displayName.getLocalizedLabels().putAll(description.getLocalizedLabels());
		}
		
		if (description == null){
			this.description = new LocalizationImpl();
		}
		else{
			this.description = description;
		}
		
		if (! this.description.hasLocalizedLabels() && this.displayName != null && this.displayName.hasLocalizedLabels()){
			this.description.getLocalizedLabels().putAll(this.displayName.getLocalizedLabels());
		}
		
		
	}
	
	public Localization getDescription() {
		return description;
	}
	
	public Localization getDisplayName() {
		return displayName;
	}

	/**
	 * Use {@link #getDisplayName()#getLocalizedLabelForLocale(String)} instead
	 */
	@Deprecated
	public String getLocalizedLabelForLocale(String locale) {
		if (displayName != null)
			return displayName.getLocalizedLabelForLocale(locale);
		
		return null;
	}

	@Deprecated
	/**
	 * Use {@link #getDisplayName()#getLocalizedLabels} instead
	 */
	public Map<String, String> getLocalizedLabels() {
		if (displayName != null)
			return displayName.getLocalizedLabels();
		
		return new HashMap<String, String>();
		
	}

	public Localization cloneDisplayName(){
		Localization cloneLocalization = new LocalizationImpl();
		
		if (displayName != null && displayName.hasLocalizedLabels()){
			for (Entry<String, String> locLabel : displayName.getLocalizedLabels().entrySet()){
				cloneLocalization.addLocalizedLabel(locLabel.getKey(), locLabel.getValue());
			}	
		}
		return cloneLocalization;
	}

	public Localization cloneDescription(){
		Localization cloneDescription = new LocalizationImpl();
		
		if (description != null && description.hasLocalizedLabels()){
			for (Entry<String, String> locLabel : description.getLocalizedLabels().entrySet()){
				cloneDescription.addLocalizedLabel(locLabel.getKey(), locLabel.getValue());
			}	
		}
		return cloneDescription;
	}

	@Deprecated
	/**
	 * Use {@link #getDisplayName()#hasLocalizedLabels} instead
	 */
	public boolean hasLocalizedLabels() {
		return displayName != null && displayName.hasLocalizedLabels();
	}
	
}
