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

package org.betaconceptframework.astroboa.commons.comparator;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.util.CmsUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicLocalizedLabelComparator implements Comparator<Topic>,Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7774647142944190896L;
	private String locale;
	
	public void setLocale(String locale) {
		if (StringUtils.isBlank(locale))
			this.locale = Locale.ENGLISH.toString();
		else
			this.locale = locale;
	}
	
	public TopicLocalizedLabelComparator(String locale) {
		this.locale = locale;
	}
	
	public int compare(Topic topic1, Topic topic2) {
		if (locale == null)
			setLocale(null);
		
		if (topic1 == null)
			return -1;
		
		if (topic2 == null)
			return 1;
		
		
		return compareLocalizedNames(retrieveLabelForLocale(topic1), retrieveLabelForLocale(topic2));
		
	}
	
	private String retrieveLabelForLocale(Topic topic){
		String localizedLabelForLocale = topic.getLocalizedLabelForLocale(locale);
		
		if (localizedLabelForLocale == null && locale != null && ! Locale.ENGLISH.toString().equals(locale)){
			localizedLabelForLocale = topic.getLocalizedLabelForLocale(Locale.ENGLISH.toString());
		}
		
		return localizedLabelForLocale;
	}
	
	private int compareLocalizedNames(String localizedLabel0,String localizedLabel1) {
		
		if (localizedLabel0 == null && localizedLabel1 == null)
			return 0;
		
		if (localizedLabel0 != null && localizedLabel1 == null)
			return 1;
		
		if (localizedLabel0 == null && localizedLabel1 != null)
			return -1;
		
		//We need ascending order
		if ("el".equalsIgnoreCase(locale))
		{
			//Filter string to lower case and transforming accented characters to simple ones
			String lowerCaseGreekFilteredLocalizedLabel0= CmsUtils.filterGreekCharacters(localizedLabel0);
			String lowerCaseGreekFilteredLocalizedLabel1= CmsUtils.filterGreekCharacters(localizedLabel1);
			
			return lowerCaseGreekFilteredLocalizedLabel0.compareTo(lowerCaseGreekFilteredLocalizedLabel1);
		}
		else
			return localizedLabel0.compareTo(localizedLabel1);
	}
}
