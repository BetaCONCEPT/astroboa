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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class CmsLocalizationUtils {

	private  boolean isValid(String locale, String localizedName) {
		//Empty localized Names are permitted
		return localizedName != null && StringUtils.isNotBlank(locale);
	}

	public void updateCmsLocalization(Localization localization, Node cmsRepositoryEntityNode) throws  RepositoryException {
		if (localization != null && cmsRepositoryEntityNode != null)
		{
			//Retrieve CmsLocalizationNode
			Node cmsLocalizationNode = null;
			if (cmsRepositoryEntityNode.hasNode(CmsBuiltInItem.Localization.getJcrName()))
				cmsLocalizationNode = cmsRepositoryEntityNode.getNode(CmsBuiltInItem.Localization.getJcrName());
			else
				//Create a new cms localization node
				cmsLocalizationNode = JcrNodeUtils.addLocalizationNode(cmsRepositoryEntityNode);
				
			 PropertyIterator localeProperties = cmsLocalizationNode.getProperties(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+"*");
			 Map<String, Property> propertiesPerLocale = new HashMap<String, Property>();
			 //remove all previous locales
			 while (localeProperties.hasNext()) {
				Property localeProperty = localeProperties.nextProperty();
				propertiesPerLocale.put(localeProperty.getName(), localeProperty);
			 }
			
			 //Update locale properties
			if (localization.hasLocalizedLabels())
			{
				for (Entry<String, String> localizedLabelEntry : localization.getLocalizedLabels().entrySet())
				{
					
					String locale = localizedLabelEntry.getKey();
					String localizedLabel = localizedLabelEntry.getValue();
					
					if (!isValid(locale, localizedLabel))
						throw new CmsException("Invalid Label: Locale "+locale + " label "+ localizedLabel );
					
					ItemQName localeAsItem = ItemUtils.createNewBetaConceptItem(locale);

					String propertyLocaleName = localeAsItem.getJcrName();
					
					if (propertiesPerLocale.containsKey(propertyLocaleName)){
						
						//Update Value only if this is a new one
						String existingValue = propertiesPerLocale.get(propertyLocaleName).getString();
						
						if (existingValue != null && !existingValue.equals(localizedLabel)){
							propertiesPerLocale.get(propertyLocaleName).setValue(localizedLabel);
						}
						
						//Remove from Map
						propertiesPerLocale.remove(localeAsItem.getJcrName());
					}
					else{
						//Create new Property
						cmsLocalizationNode.setProperty(localeAsItem.getJcrName(), localizedLabel);
					}
				}
			}
			
			//Remove any existing
			if (MapUtils.isNotEmpty(propertiesPerLocale))
			{
				for (Property localeProperty: propertiesPerLocale.values()){
					cmsLocalizationNode.getProperty(localeProperty.getName()).setValue(JcrValueUtils.getJcrNull());
				}
			}
			
		}
	}
}
