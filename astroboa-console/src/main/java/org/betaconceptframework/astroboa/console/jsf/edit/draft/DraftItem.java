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
package org.betaconceptframework.astroboa.console.jsf.edit.draft;

import java.util.Calendar;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;

/**
 * 
 * A draft Item represents an item stored in Draft.
 * 
 * This item is a CmsRepositoryEntity
 * 
 * It holds the date it was inserted. 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DraftItem {

	private Calendar insertionDate;
	
	private CmsRepositoryEntity cmsRepositoryEntity;

	private String draftItemId;
	
	public boolean hasExpired(int maxHoursAllowed)
	{
		if (insertionDate == null)
		{
			//Never expires
			return false;
		}
		
		Calendar oneHourAfterInsertionCal = (Calendar) insertionDate.clone();
		oneHourAfterInsertionCal.add(Calendar.HOUR_OF_DAY, maxHoursAllowed);
		
		return Calendar.getInstance().after(oneHourAfterInsertionCal);
		
	}

	public void refresh(CmsRepositoryEntity cmsRepositoryEntity, String draftItemId)
	{
		//Update insertion date
		insertionDate = Calendar.getInstance();
		
		this.cmsRepositoryEntity = cmsRepositoryEntity;
		
		this.draftItemId = draftItemId;
		
	}
	
	/**
	 * @return the insertionDate
	 */
	public Calendar getInsertionDate() {
		return insertionDate;
	}

	/**
	 * @return the cmsRepositoryEntity
	 */
	public CmsRepositoryEntity getCmsRepositoryEntity() {
		return cmsRepositoryEntity;
	}
	
	public String getLabel()
	{
		if (cmsRepositoryEntity == null)
		{
			return "";
		}
		
		if (cmsRepositoryEntity instanceof ContentObject)
		{
			//First look for its title
			StringProperty titleProperty = (StringProperty) ((ContentObject)cmsRepositoryEntity).getCmsProperty("profile.title");
			
			if (titleProperty != null && titleProperty.hasValues())
			{
				return titleProperty.getSimpleTypeValue();
			}
			
			//No title return system name
			if (((ContentObject)cmsRepositoryEntity).getSystemName() != null)
			{
				return ((ContentObject)cmsRepositoryEntity).getSystemName();
			}
			
			//no system name either return id plus content type
			return cmsRepositoryEntity.getId() == null ? "" :cmsRepositoryEntity.getId() +
					((ContentObject)cmsRepositoryEntity).getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
			
		}
		
		if (cmsRepositoryEntity instanceof Topic)
		{
			//return name
			return ((Topic)cmsRepositoryEntity).getName();
		}
		
		return cmsRepositoryEntity.getId();
	}
	
	public String getTypeLabel() {
		if (cmsRepositoryEntity == null || ! (cmsRepositoryEntity instanceof ContentObject)) 
		{
			return "";
		}
		else 
		{
			return ((ContentObject)cmsRepositoryEntity).getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
		}
	}
	
	public String getInsertionDateAsString()
	{
		if (insertionDate == null)
		{
			return "";
		}
		
		return DateUtils.format(insertionDate, "dd/MM/yyyy HH:mm:ss");
	}

	/**
	 * @return the draftItemId
	 */
	public String getId() {
		return draftItemId;
	}

	

	public boolean isAContentObject()
	{
		return cmsRepositoryEntity != null && cmsRepositoryEntity instanceof ContentObject;
	}
	
	public static String generateDraftItemIdForCmsRepositoryEntity(CmsRepositoryEntity cmsRepositoryEntity)
	{
		if (cmsRepositoryEntity == null)
		{
			return "";
		}
		
		return cmsRepositoryEntity.getId() == null ? String.valueOf(System.identityHashCode(cmsRepositoryEntity)) : cmsRepositoryEntity.getId();
	}
}
