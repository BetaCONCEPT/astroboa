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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draft is used to store unsaved property values, mainly
 * rich texts for each logged in user.
 * 
 * Astroboa provides a mechanism to auto save these values in this
 * draft n order for these to be available when ever property
 * is being edited again.
 * 
 * This bean is not in SESSION scope as there is the case where
 * user might accidentally logs out. Thus we need APPLICATION scope
 * and appropriate structure to organize unsaved values per logged in 
 * user 
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("draft")
@Scope(ScopeType.APPLICATION)
public class Draft {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	//Key is LoggedIn RepositoryUser id
	private ConcurrentHashMap<String, LoggedInUserDraftItemContainer> draftItemContainerPerUser = new ConcurrentHashMap<String, LoggedInUserDraftItemContainer>();

	public void add(String repositoryUserId, CmsRepositoryEntity cmsRepositoryEntity) {

		logger.debug("Adding entity {} for user {}",cmsRepositoryEntity, repositoryUserId);
		
		if (StringUtils.isBlank(repositoryUserId))
		{
			return;
		}

		boolean success = false;

		//Try to success for at most 30 secs as there might be the case of an endless loop.
		//Do not forget that if edit form is enabled auto save will run every 2 minutes
		Calendar thirtySecondsAfterStart = Calendar.getInstance();
		thirtySecondsAfterStart.add(Calendar.SECOND, 30);
		
		try{
			do {
				LoggedInUserDraftItemContainer draftItemContainer = draftItemContainerPerUser.get(repositoryUserId);


				LoggedInUserDraftItemContainer newDraftItemContainer = (draftItemContainer == null) ? new LoggedInUserDraftItemContainer(repositoryUserId) : draftItemContainer;

				newDraftItemContainer.add(cmsRepositoryEntity);

				if (draftItemContainer == null) {
					success = (draftItemContainerPerUser.putIfAbsent(repositoryUserId, newDraftItemContainer) == null)? true : false;
				}
				else {
					success = draftItemContainerPerUser.replace(repositoryUserId, draftItemContainer, newDraftItemContainer);
				}
				
				if (! success && Calendar.getInstance().after(thirtySecondsAfterStart))
			      {
			    	  success = true;
			    	  JSFUtilities.addMessage(null, "object.edit.save.draft.error",null, FacesMessage.SEVERITY_WARN);
			      }

			} while (!success);


		}
		catch(Exception e)
		{
			logger.error("",e);
			JSFUtilities.addMessage(null, "object.edit.save.draft.error",null, FacesMessage.SEVERITY_WARN);
		}

	}
	
	public void deleteDraftItem_UIAction(String loggedInRepositoryUserId, String draftItemId)
	{
		if (StringUtils.isBlank(loggedInRepositoryUserId)|| StringUtils.isBlank(draftItemId))
		{
			return;
		}
		
		if (draftItemContainerPerUser.containsKey(loggedInRepositoryUserId))
		{
			draftItemContainerPerUser.get(loggedInRepositoryUserId).removeDraftItem(draftItemId);
			
			if (! draftItemContainerPerUser.get(loggedInRepositoryUserId).hasDraftItems())
			{
				draftItemContainerPerUser.remove(loggedInRepositoryUserId);
			}
			
		}
	}
	
	public List<DraftItem> getDraftItemsForLoggedInRepositoryUser(String loggedInRepositoryUserId)
	{
		if (StringUtils.isBlank(loggedInRepositoryUserId) || !draftItemContainerPerUser.containsKey(loggedInRepositoryUserId))
		{
			return new ArrayList<DraftItem>();
		}
		
		return draftItemContainerPerUser.get(loggedInRepositoryUserId).getACopyOfDraftItems();
		
	}
	
	public boolean hasLoggedInRepositoryUserItemsInDraft(String loggedInRepositoryUserId)
	{
		return StringUtils.isNotBlank(loggedInRepositoryUserId) && draftItemContainerPerUser.containsKey(loggedInRepositoryUserId) 
			&& draftItemContainerPerUser.get(loggedInRepositoryUserId).hasDraftItems();
		
	}
	
	public void permanentlyRemoveDraftItemsForLoggedInRepositoryUser_UIAction(String loggedInRepositoryUserId)
	{
		if (StringUtils.isNotBlank(loggedInRepositoryUserId) && draftItemContainerPerUser.containsKey(loggedInRepositoryUserId))
		{
			draftItemContainerPerUser.remove(loggedInRepositoryUserId);
		}
	}

	public void removeDraftItemsOlderThan(int numberOfHours) {
		
		if (draftItemContainerPerUser.isEmpty())
		{
			return;
		}
		
		for (LoggedInUserDraftItemContainer loggedInUserDraftItemContainer : draftItemContainerPerUser.values())
		{
			int numberOfDraftItemsRemoved = loggedInUserDraftItemContainer.removeDraftItemsOlderThan(numberOfHours);
			
			if (numberOfDraftItemsRemoved >0)
			{
				logger.info("Removed {} expired items from user's {}", numberOfDraftItemsRemoved, loggedInUserDraftItemContainer.getRepositoryUserId());
			}
		}
		
	}
	
	@Observer({SeamEventNames.CONTENT_OBJECT_MODIFIED})
	public void removeCmsRepositoryEntityFromDraft(String loggedInRepositoryUserId, String cmsRepositoryEntityId) 
	{
		deleteDraftItem_UIAction(loggedInRepositoryUserId, cmsRepositoryEntityId);
	}
	

}
