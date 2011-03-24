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
package org.betaconceptframework.astroboa.console.jsf.space;


import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backing bean of a space or a content object displayed in UserSpaceNavigation data grid 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SpaceItem {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public enum SpaceItemType{
		SPACE, 
		CONTENTOBJECT
	}

	private SpaceItemType type;
	private String localizedLabel;
	private Object spaceItemObject;
	private SpaceService spaceService;
	
	public SpaceItemType getType() {
		return type;
	}
	public void setType(SpaceItemType type) {
		this.type = type;
	}
	public String getLocalizedLabel() {
		if (localizedLabel != null)
			return localizedLabel;
		
		
		switch (type) {
		case SPACE:
			localizedLabel = ((Space)spaceItemObject).getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString());
			break;
		case CONTENTOBJECT:
			//Check if content object has a title
			ContentObjectUIWrapper contentObjectWrapper = (ContentObjectUIWrapper)spaceItemObject;
			 StringProperty titleProperty = (StringProperty)contentObjectWrapper.getContentObject().getCmsProperty("profile.title");
			if (titleProperty !=null)
				localizedLabel = titleProperty.getSimpleTypeValue().length() < 70 ? titleProperty.getSimpleTypeValue() : titleProperty.getSimpleTypeValue().substring(0, 67) + "...";
			else{
				//CO does not have a title. Return localized label for content if any
				if (contentObjectWrapper.getContentObject().getTypeDefinition() != null)
					localizedLabel = contentObjectWrapper.getContentObject().getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
			}
			break;
		default:
			break;
		}
		
		return localizedLabel;
	}
	
	public void setLocalizedLabel(String localizedLabel) {
		this.localizedLabel = localizedLabel;
	}
	public Object getSpaceItemObject() {
		return spaceItemObject;
	}
	public void setSpaceItemObject(Object spaceItemObject) {
		this.spaceItemObject = spaceItemObject;
	}
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

}
