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
package org.betaconceptframework.astroboa.console.commons;

import java.util.Iterator;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class Utils {

	protected static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	public static String getLocalizedLabelForCurrentLocaleForTopic(Topic topic) {
		if (topic == null){
			//We must provide at least a meesage
			return JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null);
		}
		
		String locale = getLabelForCurrentDefaultOrEnglishLocale(topic, topic.getCurrentLocale());
		
		if (StringUtils.isBlank(locale)){
			return topic.getName();
		}
		
		return locale;
		
	}

	public static String getLocalizedLabelForCurrentLocaleForTaxonomy(Taxonomy taxonomy){
		
		if (taxonomy == null){
			//We must provide at least a message
			return JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null);
		}
		
		String locale = getLabelForCurrentDefaultOrEnglishLocale(taxonomy, taxonomy.getCurrentLocale());
		
		if (StringUtils.isBlank(locale)){
			return taxonomy.getName();
		}
		
		return locale;
		
	}

	/*
	 * Current locale is the locale provided by the CmsEntity itself.
	 * It is not the same with the locale provided by JSFUtilities,
	 * although they may have the same value
	 * 
	 */
	private static String getLabelForCurrentDefaultOrEnglishLocale(Localization localization, String currentLocale) {
		
		//Check locale provided by JSF
		String locale = localization.getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
		
		if (locale == null){
			//Check locale provided by entity
			locale = localization.getLocalizedLabelForLocale(currentLocale);
			
			//By default method Localization.getLocalizedLabelForLocale()
			//returns the label for ENGLISH locale, if the provided current locale is
			//null.
			//So an extra check is done in cases where current locale is not null
			//but no label exists for it
			if (locale == null && currentLocale != null && ! Locale.ENGLISH.toString().equals(currentLocale)){
				//Explicitly check value for english locale
				locale = localization.getLocalizedLabelForLocale(Locale.ENGLISH.toString());
			}
		}
		
		return locale;
	}


	public static String retrieveDisplayNameForRoleOrPerson(IdentityStoreRunAsSystem identityStoreRunAsSystem, String personIdOrRoleName){
		
		if (identityStoreRunAsSystem == null || StringUtils.isBlank(personIdOrRoleName)){
			logger.warn("Cannot retrieve display name for person or role because no indentity store instance has been provided");
			return personIdOrRoleName;
		}

		if (StringUtils.isBlank(personIdOrRoleName)){
			logger.warn("Cannot retrieve display name for person or role because provided person id or role name is blank");
			return personIdOrRoleName;
		}

		try{
			//Check first if provided personIdOrRoleName corresponds to a user name
			boolean userExists = (Boolean) identityStoreRunAsSystem.execute("userExists", new Class<?>[]{String.class}, new Object[]{personIdOrRoleName});

			if (userExists){
				Person user = (Person) identityStoreRunAsSystem.execute("retrieveUser", new Class<?>[]{String.class}, new Object[]{personIdOrRoleName});
				if (user != null){
					return user.getDisplayName() == null ? personIdOrRoleName : user.getDisplayName();
				}
			}

			//It must be role. If not then the provided personIdOrRoleName is returned 
			return (String) identityStoreRunAsSystem.execute("retrieveRoleDisplayName", new Class<?>[]{String.class, String.class}, new Object[]{personIdOrRoleName, JSFUtilities.getLocaleAsString()});
		}
		catch(Exception e){
			logger.error("",e);
			return personIdOrRoleName;
		}
	}
	
	/*
	 * Get application Messages Separated with <br/>
	 */
	public static String getApplicationMessagesInSeparateLines() {
		String applicationMessagesAsCSVString = "";
		Iterator<FacesMessage> iter = FacesContext.getCurrentInstance().getMessages();
		
		while ( iter.hasNext() ) {
			applicationMessagesAsCSVString += iter.next().getDetail();
			if (iter.hasNext()) {
				applicationMessagesAsCSVString += "<br/>";
			}
		}
		
		return applicationMessagesAsCSVString;
	}
	
	public static String currentTimeInSeconds() {
		long time = System.currentTimeMillis()/1000;
		return String.valueOf(time);
	}
	
}
