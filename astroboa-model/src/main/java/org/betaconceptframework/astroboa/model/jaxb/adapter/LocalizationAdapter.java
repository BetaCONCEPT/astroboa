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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizationImpl;
import org.betaconceptframework.astroboa.model.jaxb.type.LocalizationType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LocalizationAdapter extends XmlAdapter<LocalizationType,Localization>{

	private boolean useJSONVersionOfLocalizationType = false;

	@Override
	public LocalizationType marshal(Localization localization) throws Exception {
		
		if (localization == null || MapUtils.isEmpty(localization.getLocalizedLabels())){
			return null;
		}

		LocalizationType localizationType = new LocalizationType();
		
		Set<Entry<String, String>> entrySet = localization.getLocalizedLabels().entrySet();

		if (useJSONVersionOfLocalizationType){
			
			LocalizationType.Label localizedLabel = new LocalizationType.Label();
			
			for (Entry<String, String> locLabel : entrySet){
				localizedLabel.addLabelForLang(locLabel.getKey(), locLabel.getValue());
			}
			
			localizationType.getLabel().add(localizedLabel);
		}
		else{
			for (Entry<String, String> locLabel : entrySet){
				LocalizationType.Label localizedLabel = new LocalizationType.Label();
				
				localizedLabel.setValue(locLabel.getValue());
				localizedLabel.setLang(locLabel.getKey());
				localizationType.getLabel().add(localizedLabel);
			}
		}

		return localizationType;
	}

	@Override
	public Localization unmarshal(LocalizationType localizationType) throws Exception {

		Localization localization = new LocalizationImpl();

		if (localizationType != null && CollectionUtils.isNotEmpty(localizationType.getLabel())){

			for (LocalizationType.Label locLabel : localizationType.getLabel()){
				if (useJSONVersionOfLocalizationType){
					for (Entry<QName, String> labelPerLangEntry : locLabel.getLabelsPerLang().entrySet()){
						localization.addLocalizedLabel(labelPerLangEntry.getKey().getLocalPart(), locLabel.getValue());	
					}
				}
				else{
					localization.addLocalizedLabel(locLabel.getLang(), locLabel.getValue());
				}
			}
		}
		return localization;
	}

	public void useJsonVersion(){
		useJSONVersionOfLocalizationType = true;
	}

}
