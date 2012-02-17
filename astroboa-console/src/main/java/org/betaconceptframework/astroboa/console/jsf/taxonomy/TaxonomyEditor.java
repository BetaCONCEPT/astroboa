/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;


import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.console.jsf.LocalizationEdit;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

/**
 * Backing bean for editing taxonomies
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
@Name("taxonomyEditor")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyEditor extends LocalizationEdit {

	private static final long serialVersionUID = 1L;

	private TaxonomyService taxonomyService;

	private Taxonomy editedTaxonomy;

	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	public void saveTaxonomy_UIAction() {

		boolean isNewTaxonomy = editedTaxonomy.getId() == null;
		String saveResult = null;
		try {
			saveResult = validateEditedTaxonomy();
			if ("success".equals(saveResult)) {

				//Save taxonomy
				taxonomyService.saveTaxonomy(editedTaxonomy);

				JSFUtilities.addMessage(null, "taxonomy.save.successful", null, FacesMessage.SEVERITY_INFO);

				//Notify that a taxonomy has been added
				if (isNewTaxonomy)
					Events.instance().raiseEvent(SeamEventNames.TAXONOMY_ADDED);
				else
					Events.instance().raiseEvent(SeamEventNames.TAXONOMY_SAVED, editedTaxonomy.getId());

			}
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "taxonomy.save.error", null, FacesMessage.SEVERITY_WARN);
			getLogger().error("Taxonomy could not be saved",e);

			//Since an exception is thrown raise event to reload taxonomy if it exists in tree
			if (!isNewTaxonomy)
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TAXONOMY_TREE_NODE, editedTaxonomy.getId());

		}
	}

	private String validateEditedTaxonomy() {

		//Check if name has capitals and does not contain white spaces and numbers
		if (!CmsConstants.SystemNamePattern.matcher(editedTaxonomy.getName()).matches()){
			JSFUtilities.addMessage(null, "taxonomy.edit.invalid.system.name", new String[]{editedTaxonomy.getName()},FacesMessage.SEVERITY_WARN);
			return "error";
		}

		//Finally check is there is another taxonomy with the same name.
		//Taxonomy name is case sensitive thus load all taxonomy names
		//and make sure that none resembles the provided taxonomy name
		List<Taxonomy> allTaxonomies = taxonomyService.getTaxonomies(JSFUtilities.getLocaleAsString());
		if (CollectionUtils.isNotEmpty(allTaxonomies)){
			for (Taxonomy taxonomy : allTaxonomies){
				if (taxonomy.getName() == null){
					//SHOULD NEVER HAPPEN
					logger.warn("Found Taxonomy without name!!!! + Taxonmy id: "+ taxonomy.getId() + " and localized labels "+ taxonomy.getLocalizedLabels());
				}
				else{
					
					if (StringUtils.equalsIgnoreCase(editedTaxonomy.getName(),taxonomy.getName())){
						if (editedTaxonomy.getId() == null || ! StringUtils.equals(editedTaxonomy.getId(), taxonomy.getId())){
							//Found taxonomy with the same name
							JSFUtilities.addMessage(null, "taxonomy.non.unique.system.name", null,  FacesMessage.SEVERITY_WARN);
							return "error";
						}
					}
				}
			}
		}
		// validate Localized Labels
		return updateLocalizedLabelsMapInEditedLocalizationEntity(editedTaxonomy);
	}

	public void reset() {
		editedTaxonomy = null;
		editedLocalizedLabels = null;
	}

	public void addNewTaxonomy() {
		reset();
		editTaxonomy(cmsRepositoryEntityFactory.newTaxonomy());
	}

	public void editTaxonomy(Taxonomy taxonomy) {
		reset();
		editedTaxonomy = taxonomy;
		
		if (editedTaxonomy.getId() != null) {
			fillLocalizedLabelBuffer(taxonomy);
		}
		else {
			editedLocalizedLabels = new ArrayList<LocalizedLabel>();
			// create the first label
			addLocalizedLabel_UIAction();
		}

	}

	public Taxonomy getEditedTaxonomy() {
		return editedTaxonomy;
	}

	public List<LocalizedLabel> getEditedTaxonomyLabels() {
		return editedLocalizedLabels;
	}

	public void setTaxonomyService(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}

	
}
