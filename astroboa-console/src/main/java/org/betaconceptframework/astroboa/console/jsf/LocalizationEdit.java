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
package org.betaconceptframework.astroboa.console.jsf;


import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.console.jsf.edit.LanguageSelector;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.annotations.In;
import org.jboss.seam.international.LocaleSelector;

/**
 * Backing bean for editing a set of localized labels for a CmsRepositoryEntity
 * 
 * Currently it is used for editing tags, topics and taxonomies
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LocalizationEdit extends AbstractUIBean {
	
	private static final long serialVersionUID = 1L;

	@In
	protected LocaleSelector localeSelector;
	
	protected List<LocalizedLabel> editedLocalizedLabels;

	
	protected void fillLocalizedLabelBuffer(Localization editedLocalizationEntity){
		editedLocalizedLabels = new ArrayList<LocalizedLabel>();
		if (editedLocalizationEntity.hasLocalizedLabels()){
			for (String locale : editedLocalizationEntity.getLocalizedLabels().keySet()) {
				LocalizedLabel localizedLabel = new LocalizedLabel(locale, editedLocalizationEntity.getLocalizedLabels().get(locale));
				editedLocalizedLabels.add(localizedLabel);
			}
		}
	}

	protected String updateLocalizedLabelsMapInEditedLocalizationEntity(Localization editedLocalizationEntity) {
		if (CollectionUtils.isEmpty(editedLocalizedLabels)) {
			JSFUtilities.addMessage(null, "topic.edit.localization.addAtLeastOneLocalizedLabel", null, FacesMessage.SEVERITY_WARN);
			return "failure";
		}
		editedLocalizationEntity.clearLocalizedLabels();
		for (LocalizedLabel localizedLabel : editedLocalizedLabels) {
			if (!editedLocalizationEntity.getLocalizedLabels().containsKey(localizedLabel.getLocale())){
				//Check that label is not an empty String
				if (StringUtils.isBlank(localizedLabel.getLabel())){
					JSFUtilities.addMessage(null, "topic.edit.localization.addALabelForTheSelectedLanguage", new String[] {LanguageSelector.instance().getLabelForLanguage(localizedLabel.getLocale())}, FacesMessage.SEVERITY_WARN);
					return "failure";
				}
				editedLocalizationEntity.addLocalizedLabel(localizedLabel.getLocale(), localizedLabel.getLabel());
			}
			else {
				JSFUtilities.addMessage(null, "topic.edit.localization.chooseADifferentLanguageForEachLabel", null, FacesMessage.SEVERITY_WARN);
				return "failure";
			}
		}
		return "success";
	}

	public void addLocalizedLabel_UIAction() {
		LocalizedLabel localizedLabel = new LocalizedLabel(localeSelector.getLocaleString(), "");
		editedLocalizedLabels.add(localizedLabel);
	}
	
	public void removeLocalizedLabel_UIAction(LocalizedLabel localizedLabel) {
		editedLocalizedLabels.remove(localizedLabel);
	}
	
	public class LocalizedLabel {
		public LocalizedLabel(String locale, String label) {
			this.locale = locale;
			this.label = label;
		}
		
		String locale;
		String label;
		
		public String getLocale() {
			return locale;
		}
		
		public void setLocale(String locale) {
			this.locale = locale;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}		
	}
}
