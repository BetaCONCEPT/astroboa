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
package org.betaconceptframework.astroboa.model.impl.item;


import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.util.CmsConstants;


/**
 * Enumeration containing all content object Profile properties
 * used by CMS Repository
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum ContentObjectProfileItem implements ItemQName{

	//all names must be the same with those defined in content base xsd
	Profile,
	Created,
	Subject,
	HasVersion,
	VersionLabels, 
	Modified;
	
	
	private ItemQName cmsDefinitionItem;

	private ContentObjectProfileItem()
	{
		cmsDefinitionItem = new ItemQNameImpl("", "", StringUtils.uncapitalize(this.name()));
	}

	public ItemQName getItemForQuery()
	{
		return new ItemQNameImpl("", "",ContentObjectProfileItem.Profile.getJcrName() + CmsConstants.PERIOD_DELIM +this.getJcrName());
	}
	
	public String getJcrName()
	{
		return cmsDefinitionItem.getJcrName();
	}

	public String getLocalPart() {
		return cmsDefinitionItem.getLocalPart();
	}

	public String getNamespaceURI() {
		return cmsDefinitionItem.getNamespaceURI();
	}

	public String getPrefix() {
		return cmsDefinitionItem.getPrefix();
	}

	public boolean equals(ItemQName otherItemQName) {
		return cmsDefinitionItem.equals(otherItemQName);
	}
	public boolean equalsTo(ItemQName otherItemQName) {
		return equals(otherItemQName);
	}
	

}
