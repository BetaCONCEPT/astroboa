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
package org.betaconceptframework.astroboa.engine.jcr.renderer;


import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class CmsLocalizationRenderer  {

	public void renderCmsLocalization(Node cmsRepositoryEntityNode, Localization cmsLocalization) throws RepositoryException{
		if (cmsLocalization != null){
			if (cmsRepositoryEntityNode.hasNode(CmsBuiltInItem.Localization.getJcrName())){
				Node cmsLocalizationNode = cmsRepositoryEntityNode.getNode(CmsBuiltInItem.Localization.getJcrName());

				PropertyIterator locales = cmsLocalizationNode.getProperties(ItemUtils.createNewBetaConceptItem(CmsConstants.ANY_NAME).getJcrName());

				while ( locales.hasNext() ){
					Property localeProperty = locales.nextProperty();
					String localePropertyName = localeProperty.getName().replaceAll(BetaConceptNamespaceConstants.ASTROBOA_PREFIX+":", "");

					render(cmsLocalization, localePropertyName, localeProperty.getString());
				}
			}

		}

	}

	private void render(Localization localization, String locale, String localizedName){
		if (localization == null)
			throw new CmsException("Empty Localization");

		if (locale== null)
			locale = Locale.ENGLISH.toString();

		if (localizedName == null)
			//Empty String is the default value
			localizedName = "";

		localization.addLocalizedLabel(locale, localizedName);

	}

}
