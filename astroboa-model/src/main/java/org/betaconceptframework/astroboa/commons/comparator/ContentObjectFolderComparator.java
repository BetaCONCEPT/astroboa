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


import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder.Type;
import org.betaconceptframework.astroboa.util.CmsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectFolderComparator implements Comparator<ContentObjectFolder>{

	
	private Logger logger = LoggerFactory.getLogger(ContentObjectFolderComparator.class);
	
	private String locale;
	public ContentObjectFolderComparator(String locale)
	{
		this.locale = locale;
		
		if (StringUtils.isBlank(this.locale))
			this.locale = Locale.ENGLISH.toString();
		
	}
	public int compare(ContentObjectFolder folder1, ContentObjectFolder folder2) {

		if (folder1 == null)
			return -1;

		if (folder2 == null)
			return 1;

		if (folder1.getType() != folder2.getType())
		{
			logger.warn("Unable to compare content object folders with different types {} , {}", folder1.getType() , folder2.getType());
			return 0;
		}
		
		try{
			
			String folderLabel1 = folder1.getLocalizedLabelForCurrentLocale();
			String folderLabel2 = folder2.getLocalizedLabelForCurrentLocale();
			
			if (folderLabel1 == null && folderLabel2 == null)
				return 0;
			
			if (folderLabel1 != null && folderLabel2 == null)
				return 1;
			
			if (folderLabel1 == null && folderLabel2 != null)
				return -1;
			
			//Both folders should have the same type
			switch (folder1.getType()) {
			case SECOND:
			case MINUTE:
			case HOUR:
			case DAY:
			case MONTH:
			case YEAR:
				//Labels for MINUTE, HOUR, DAY, MONTH and YEAR are numbers
				Integer folderLabelInt1 = Integer.parseInt(folderLabel1);
				Integer folderLabelInt2 = Integer.parseInt(folderLabel2);

				//If type is YEAR, inverse compare as years must be displayed in descending order
				if (Type.YEAR == folder1.getType())
					return folderLabelInt2.compareTo(folderLabelInt1);
				
				return folderLabelInt1.compareTo(folderLabelInt2);

			case CONTENT_TYPE:
				if ("el".equalsIgnoreCase(locale))
				{
					//Filter string to lower case and transforming accented characters to simple ones
					String lowerCaseGreekFilteredLocalizedLabel0= CmsUtils.filterGreekCharacters(folderLabel1);
					String lowerCaseGreekFilteredLocalizedLabel1= CmsUtils.filterGreekCharacters(folderLabel2);
					
					return lowerCaseGreekFilteredLocalizedLabel0.compareTo(lowerCaseGreekFilteredLocalizedLabel1);
				}
				else
					return folderLabel1.compareTo(folderLabel2);
			default:
				return 0;
			}
			
			
		}
		catch (Exception e)
		{
			logger.error("While comparing content object folders ", e);
			return 0;
		}

	}

}
