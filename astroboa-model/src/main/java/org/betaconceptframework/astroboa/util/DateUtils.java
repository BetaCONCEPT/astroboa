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

package org.betaconceptframework.astroboa.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DateUtils {
	
	public static Calendar clearTimeFromCalendar(Calendar calendar){
		if (calendar != null){
			calendar.set(Calendar.HOUR, 0);
			calendar.clear(Calendar.AM_PM); //ALWAYS clear AM_PM before HOUR_OF_DAY
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.clear(Calendar.DST_OFFSET);
			calendar.clear(Calendar.ZONE_OFFSET);
		}
		
		return calendar;
	}
	public static Calendar addLocaleToCalendar(Calendar date, String locale) 
	{
		if (date != null)
		{
			if (locale != null)
			{
				Calendar localizedDate = Calendar.getInstance(new Locale(locale));
				localizedDate.setTime(date.getTime());
				return localizedDate;
			}
			else
				return date;
		}
	
		return null;
	}
	
	private final static String Default_Pattern = "dd/MM/yyyy HH:mm:ss.SSS";
	
	public static String format(Calendar calendar, String pattern)
	{
		if (calendar == null)
			return null;
		
		return formatDate(calendar.getTime(), pattern);
	}

	public static String formatDate(Date date, String pattern) {
		if (date == null)
			return null;
		if (StringUtils.isBlank(pattern))
			pattern = Default_Pattern; //Default Pattern
		
		return DateFormatUtils.format(date, pattern);
	}

	public static String formatDate(Date date) {
		return formatDate(date, null);
		
	}

	public static String format(Calendar calendar) {
		return format(calendar, null);
	}
	
	public static Calendar fromString(String date)
	{
		return fromString(date, Default_Pattern);
	}

	public static Calendar fromString(String date, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		
		try {
			return toCalendar(df.parse(date));
		} catch (Exception e) {
			return null;
		}
	}

	
	
	public static Calendar toCalendar(Date date)
	{
		if (date == null)
			return null;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		return cal;
	}
}
