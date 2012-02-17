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
package org.betaconceptframework.astroboa.console.jsf.browse;

import java.util.Calendar;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to create and save a copy of the specified Object
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("copier")
@Scope(ScopeType.PAGE)
public class Copier {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private LoggedInRepositoryUser loggedInRepositoryUser;

	private AstroboaClient astroboaClient;

	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}

	public void setAstroboaClient(
			AstroboaClient astroboaClient) {
		this.astroboaClient = astroboaClient;
	}
	
	public void  createACopyAndSaveContentObject_UIAction(String contentObjectId){
	
		try{
			
			if (StringUtils.isBlank(contentObjectId)){
				JSFUtilities.addMessage(null, "object.action.clone.message.noObjectSelected ", null, FacesMessage.SEVERITY_WARN);
				return;
			}

			ContentObject copy = astroboaClient.getContentService().copyContentObject(contentObjectId);
			
			if (copy == null){
				JSFUtilities.addMessage(null, "object.action.clone.message.cloningWasUnsuccessful", null, FacesMessage.SEVERITY_WARN);
				return ;
			}
			
			String titlePrefix = JSFUtilities.getLocalizedMessage("clone.title.prefix", null);
			
			StringProperty titleProperty = (StringProperty) copy.getCmsProperty("profile.title");
			
			if (! titleProperty.getSimpleTypeValue().startsWith(titlePrefix)){
				titleProperty.setSimpleTypeValue(titlePrefix + " "+titleProperty.getSimpleTypeValue());
			}
			
			copy.setOwner(loggedInRepositoryUser.getRepositoryUser());
			
			copy = astroboaClient.getContentService().save(copy, false, false, null);
			
			JSFUtilities.addMessage(null, "object.action.clone.message.success", null, FacesMessage.SEVERITY_INFO);

			Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_ADDED, new Object[]{
					copy.getContentObjectType(), copy.getId(), 
					(copy.getComplexCmsRootProperty().isChildPropertyDefined("profile.created") ? 
					((CalendarProperty)copy.getCmsProperty("profile.created")).getSimpleTypeValue() : Calendar.getInstance())}); 
	
			// reset results table
			ContentObjectStatefulSearchService contentObjectStatefulSearchService = (ContentObjectStatefulSearchService) JSFUtilities.getBeanFromSpringContext("contentObjectStatefulSearchService");
			
			// reset data page and decrease search results count
			contentObjectStatefulSearchService.setSearchResultSetSize(contentObjectStatefulSearchService.getSearchResultSetSize() + 1);
			UIComponentBinding uiComponentBinding = (UIComponentBinding) Contexts.getEventContext().get("uiComponentBinding");
			if (contentObjectStatefulSearchService.getSearchResultSetSize() > 0)
				contentObjectStatefulSearchService.getReturnedContentObjects().reset();
			else {
				contentObjectStatefulSearchService.setReturnedContentObjects(null);
				uiComponentBinding.setListViewContentObjectTableComponent(null);
				uiComponentBinding.setListViewContentObjectTableScrollerComponent(null);
			}

		}
		catch (Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "object.action.clone.message.error", null, FacesMessage.SEVERITY_ERROR);
		}
	}
}
