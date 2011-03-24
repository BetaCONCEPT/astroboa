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
package org.betaconceptframework.astroboa.console.jsf.edit.draft;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;

/**
 * Contains all draft items per logged in repository user
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LoggedInUserDraftItemContainer {

	private String repositoryUserId;
	
	//Key is CmsRepositoryEntity id
	private ConcurrentHashMap<String, DraftItem> draftItems = new ConcurrentHashMap<String, DraftItem>();

	public LoggedInUserDraftItemContainer(String repositoryUserId) {
		this.repositoryUserId = repositoryUserId;
	}

	public void add(CmsRepositoryEntity cmsRepositoryEntity) {

		if (cmsRepositoryEntity == null)
		{
			return;
		}
			
		boolean success = false;
		
		//Try to success for at most 30 secs as there might be the case of an endless loop.
		//Do not forget that if edit form is enabled auto save will run every 2 minutes
		Calendar thirtySecondsAfterStart = Calendar.getInstance();
		thirtySecondsAfterStart.add(Calendar.SECOND, 30);
		
		
	    do {
	    	String draftItemId = DraftItem.generateDraftItemIdForCmsRepositoryEntity(cmsRepositoryEntity);
	    	
	    	DraftItem oldDraftItem = draftItems.get(draftItemId);
	      
	      
	    	DraftItem newDraftItem = (oldDraftItem == null) ? new DraftItem() : oldDraftItem;
	      
	    	newDraftItem.refresh(cmsRepositoryEntity, draftItemId);
	      
	      if (oldDraftItem == null) {
	    	  success = (draftItems.putIfAbsent(draftItemId, newDraftItem) == null)? true : false;
	      }
	      else {
	    	  success = draftItems.replace(draftItemId, newDraftItem, newDraftItem);
	      }
	      
	      
	      if (! success && Calendar.getInstance().after(thirtySecondsAfterStart))
	      {
	    	  success = true;
	      }
	      
	    } while (!success);
		
	}

	public boolean containsCmsRepositoryEntity(String cmsRepositoryEntityId) {
		if ( StringUtils.isBlank(cmsRepositoryEntityId))
		{
			return false;
		}
		
		return draftItems.containsKey(cmsRepositoryEntityId);
	}

	public CmsRepositoryEntity retrieveCmsRepositoryEntity(
			String cmsRepositoryEntityId) {
		
		if ( StringUtils.isBlank(cmsRepositoryEntityId) || ! draftItems.containsKey(cmsRepositoryEntityId))
		{
			return null;
		}
		
		return draftItems.get(cmsRepositoryEntityId).getCmsRepositoryEntity();
	}

	public void removeDraftItem(String draftItemId) {
		
		if (draftItemId != null)
		{
			draftItems.remove(draftItemId);
		}
	}

	public List<DraftItem> getACopyOfDraftItems() {
		return new ArrayList<DraftItem>(draftItems.values());
	}

	public boolean hasDraftItems() {
		
		return draftItems != null && ! draftItems.isEmpty();
	}

	public int removeDraftItemsOlderThan(int numberOfHours) {
		
		if (draftItems.isEmpty())
		{
			return 0;
		}
		
		List<String> keysToBeRemoved = new ArrayList<String>();
		
		for (Entry<String, DraftItem> draftItemEntry : draftItems.entrySet())
		{
			if (draftItemEntry.getValue().hasExpired(numberOfHours))
			{
				keysToBeRemoved.add(draftItemEntry.getKey());
			}
		}
		
		if (! keysToBeRemoved.isEmpty())
		{
			for (String keyToBeRemoved : keysToBeRemoved)
			{
				draftItems.remove(keyToBeRemoved);
			}
			
			return keysToBeRemoved.size();
		}
		
		return 0;
	}

	/**
	 * @return the repositoryUserId
	 */
	public String getRepositoryUserId() {
		return repositoryUserId;
	}
	
	
	
	
	
}
