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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Scope(ScopeType.SESSION)
@Name("org.betaconceptframework.astroboa.portal.managedbean.portalThemeSelector")
@Install(classDependencies="javax.faces.context.FacesContext")
public class PortalThemeSelector implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@In(create=true)
	private CookieManager cookieManager;
	
	@In(create=true)
	private ContentObject portal;

	private final String themeCookieName = "org.betaconceptframework.astroboa.portal.theme";
	private Topic theme;
	private List<Topic> availableThemes;

	@Create
	public void initDefaultTheme() {
		
		// get available themes from portal object
		availableThemes = ((TopicReferenceProperty)portal.getCmsProperty("theme")).getSimpleTypeValues();
		
		String userThemeName = cookieManager.getCookieValue(themeCookieName);
		
		if (CollectionUtils.isNotEmpty(availableThemes)) {
			theme = availableThemes.get(0);
			if (userThemeName != null) {
				Topic userTheme = findTheme(userThemeName, availableThemes);
				if (userTheme != null) {
					theme = userTheme;
				}	
			}
			Contexts.getSessionContext().set("themePath", FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/theme/" + theme.getName());
		}
	}

	
	/**
	 * Recreate the JSF view, using the new theme, and raise the 
	 * org.jboss.seam.themeSelected event
	 *
	 */
	public String select() {
		if (theme != null) {
			//TODO: should check this again because it stays in a view inside the previous theme. For now just redirect to home page
			//FacesContext facesContext = FacesContext.getCurrentInstance();
			//String viewId = Pages.getViewId(facesContext);
			//UIViewRoot viewRoot = facesContext.getApplication().getViewHandler().createView(facesContext, viewId);
			//facesContext.setViewRoot(viewRoot);

			cookieManager.setCookie(themeCookieName, getTheme().getName());
			Contexts.getSessionContext().set("themePath", FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/theme/" + theme.getName());

			if ( Events.exists() ) {
				Events.instance().raiseEvent( "org.jboss.seam.themeSelected", getTheme() );
			}
			return "home";
		}
		return null;
	}

	public void select(ValueChangeEvent event) {
		selectTheme( (String) event.getNewValue() );
	}

	public String selectTheme(String themeName) {
		if (themeName != null && CollectionUtils.isNotEmpty(availableThemes)) {
			Topic selectedTheme = findTheme(themeName, availableThemes);
				if (selectedTheme != null) {
					theme = selectedTheme;
					return select();
				}
		}
		return null;
	}

	/**
	 * Get a selectable list of available themes for display in the UI
	 */
	public List<SelectItem> getThemes() {
		if (CollectionUtils.isNotEmpty(availableThemes)) {
			List<SelectItem> selectItems = new ArrayList<SelectItem>(availableThemes.size());
			for ( Topic theme : availableThemes )
			{
				selectItems.add( new SelectItem(theme, theme.getLocalizedLabelForLocale(LocaleSelector.instance().getLocaleString())));
			}
			return selectItems;
		}
		else {
			return new ArrayList<SelectItem>();
		}
	}

	/**
	 * Get the name of the current theme
	 */
	public Topic getTheme() {
		return theme;
	}
	
	public void setTheme(Topic theme) {
	      
	      this.theme = theme;
	   }
	
	private Topic findTheme(String themeName, List<Topic>availableThemes) {
		for (Topic theme : availableThemes) {
			if (theme.getName().equals(themeName)) {
				return theme;
			}
		}
		
		return null;
	}

	public static PortalThemeSelector instance()
	{
		if ( !Contexts.isSessionContextActive() )
		{
			throw new IllegalStateException("No active session context");
		}
		return (PortalThemeSelector) Component.getInstance(PortalThemeSelector.class, ScopeType.SESSION);
	}

	public List<Topic> getAvailableThemes()
	{
		return availableThemes;
	}

}