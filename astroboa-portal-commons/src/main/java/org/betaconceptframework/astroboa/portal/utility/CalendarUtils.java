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
package org.betaconceptframework.astroboa.portal.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Scope(ScopeType.APPLICATION)
@Name("calendarUtils")
public class CalendarUtils {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public Calendar[] getPredefinedCalendarPeriod(String predefinedPeriodName, TimeZone zone, Locale locale) {
		if ("today".equals(predefinedPeriodName)) {
			Calendar todayStart = GregorianCalendar.getInstance(zone, locale);
			Calendar todayEnd = GregorianCalendar.getInstance(zone, locale);
			
			setCalendarToStartOfDay(todayStart);
			setCalendarToEndOfDay(todayEnd);
			
			Calendar[] period = {todayStart, todayEnd};
			return period;
		}
		
		if ("tomorrow".equals(predefinedPeriodName)) {
			Calendar tomorrowStart = GregorianCalendar.getInstance(zone, locale);
			tomorrowStart.add(Calendar.DAY_OF_MONTH, 1);
			
			Calendar tomorrowEnd = (GregorianCalendar)tomorrowStart.clone();
			setCalendarToStartOfDay(tomorrowStart);
			setCalendarToEndOfDay(tomorrowEnd);
			
			Calendar[] period = {tomorrowStart, tomorrowEnd};
			return period;
		}
		
		if ("weekend".equals(predefinedPeriodName))
			return getCurrentWeekendPeriod(zone, locale);
		
		if ("thisWeek".equals(predefinedPeriodName))
			return getCurrentWeekPeriod(zone, locale);
		
		if ("thisMonth".equals(predefinedPeriodName))
			return getCurrentMonthPeriod(zone, locale);
		
		if ("nextSevenDays".equals(predefinedPeriodName))
			return getNextSevenDaysPeriod(zone, locale);
		
		if ("nextThirtyDays".equals(predefinedPeriodName))
			return getNextThirtyDaysPeriod(zone, locale);
		
		
		return null;
	}
	
	
	public Map<String, Map<String,String>> getPredefinedCalendarPeriods(TimeZone zone, Locale locale) {
		return null;
	}
	
	public Calendar[] getCurrentWeekPeriod(TimeZone zone, Locale locale) {
		Calendar weekStart = GregorianCalendar.getInstance(zone, locale);
		Calendar weekEnd;
		// find Monday the first week day
		while(weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			weekStart.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		// then to find SUNDAY we add 6 days
		weekEnd = (GregorianCalendar) weekStart.clone();
		weekEnd.add(Calendar.DAY_OF_MONTH, 6);
		
		setCalendarToStartOfDay(weekStart);
		setCalendarToEndOfDay(weekEnd);
		
		Calendar[] period = {weekStart, weekEnd};
		return period;
	}
	
	
	
	
	public Calendar[] getCurrentMonthPeriod(TimeZone zone, Locale locale) {
		Calendar monthStart = GregorianCalendar.getInstance(zone, locale);
		Calendar monthEnd = GregorianCalendar.getInstance(zone, locale);
		
		monthStart.set(Calendar.DAY_OF_MONTH, 1);
		
		int daysInCurrentMonth = monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthEnd.set(Calendar.DAY_OF_MONTH, daysInCurrentMonth);
		
		setCalendarToStartOfDay(monthStart);
		setCalendarToEndOfDay(monthEnd);
		
		Calendar[] period = {monthStart, monthEnd};
		return period;
	}
	
	
	public Calendar[] getNextThirtyDaysPeriod(TimeZone zone, Locale locale) {
		Calendar today = GregorianCalendar.getInstance(zone, locale);
		Calendar dateAfterThirtyDays = GregorianCalendar.getInstance(zone, locale);
		
		dateAfterThirtyDays.add(Calendar.DAY_OF_MONTH, 30);
		
		setCalendarToStartOfDay(today);
		setCalendarToEndOfDay(dateAfterThirtyDays);
		
		Calendar[] period = {today, dateAfterThirtyDays};
		return period;
	}
	
	public Calendar[] getNextSevenDaysPeriod(TimeZone zone, Locale locale) {
		Calendar today = GregorianCalendar.getInstance(zone, locale);
		Calendar dateAfterSevenDays = GregorianCalendar.getInstance(zone, locale);
		
		dateAfterSevenDays.add(Calendar.DAY_OF_MONTH, 7);
		
		setCalendarToStartOfDay(today);
		setCalendarToEndOfDay(dateAfterSevenDays);
		
		Calendar[] period = {today, dateAfterSevenDays};
		return period;
	}
	
	public Calendar[] getCurrentWeekendPeriod(TimeZone zone, Locale locale) {
		Calendar weekendStart = GregorianCalendar.getInstance(zone, locale);
		Calendar weekendEnd = GregorianCalendar.getInstance(zone, locale);
		
		// we will include Friday in the weekend
		int currentDayOfWeek = weekendStart.get(Calendar.DAY_OF_WEEK);
		
		if (currentDayOfWeek == Calendar.FRIDAY) {
			// set to SUNDAY
			weekendEnd.add(Calendar.DAY_OF_MONTH, 2);
		}
		else if (currentDayOfWeek == Calendar.SATURDAY) {
			// set to SUNDAY
			weekendEnd.add(Calendar.DAY_OF_MONTH, 1);
		}
		else if (currentDayOfWeek == Calendar.SUNDAY){
			//Do nothing
		}
		else {
			while (weekendStart.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
				weekendStart.add(Calendar.DAY_OF_MONTH, 1);
				weekendEnd.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			// now weekendStart and weekendEnd is FRIDAY and we 
			// set weekendEnd to SUNDAY
			weekendEnd.add(Calendar.DAY_OF_MONTH, 2);
			
		}
		
		setCalendarToStartOfDay(weekendStart);
		setCalendarToEndOfDay(weekendEnd);
		
		Calendar[] period = {weekendStart, weekendEnd};
		return period;
	}
	
	
	/**
	 * Get a string of a certain date pattern form (e.g. YYYY-MM-DD i.e. 2008-03-29) and converts it to
	 * a calendar object. 
	 * @param dateString
	 * @param dateFormatPattern
	 * @return
	 */
	public Calendar convertDateStringToCalendar(String dateString, String dateFormatPattern, TimeZone zone, Locale locale) {
		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
		dateFormat.setLenient(false); // be strict in the formatting
		// apply accepted pattern
		dateFormat.applyPattern(dateFormatPattern);
		
		// get a date object
		try {
			Date date = dateFormat.parse(dateString);
			if (date != null) {
				Calendar calendar = new GregorianCalendar(zone, locale);
				calendar.setTime(date);
				return calendar;
			}
		else {
			return null;
		}
		}
		catch (Exception e) {
			logger.warn("The date string:" + dateString + " has an invalid format. The valid form is:" + dateFormatPattern, e);
			return null;
		}
		
	}
	
	public String convertDateToString(Date date, String dateFormatPattern) {
		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
		dateFormat.setLenient(false); // be strict in the formatting
		// apply accepted pattern
		dateFormat.applyPattern(dateFormatPattern);
		
		return dateFormat.format(date);
	}
	
	public Calendar convertObjectToCalendar(Object dateObject, TimeZone zone, Locale locale) {
		
		if (dateObject != null
				&& ((dateObject instanceof String && StringUtils
						.isNotBlank((String) dateObject)) || dateObject instanceof Calendar || dateObject instanceof Date)) {
			Calendar dateAsCalendar = null;
			if (dateObject instanceof String) {
				dateAsCalendar = 
						convertDateStringToCalendar(
								(String) dateObject,
								PortalStringConstants.DATE_FORMAT_PATTERN, zone, locale);
			}
			else if (dateObject instanceof Date) {
				dateAsCalendar = new GregorianCalendar(zone, locale);
				dateAsCalendar.setTime((Date) dateObject);
			}
			else {
				dateAsCalendar = (Calendar) dateObject;
			}
			
			return dateAsCalendar;
		}
		
		return null;
	}
	
	public List<Calendar> getDiscreteDatesInPeriod(Calendar fromCalendarInclusive, Calendar toCalendarInclusive) {
		List<Calendar>  discreteDatesInPeriod = new ArrayList<Calendar>();
		if (fromCalendarInclusive != null && toCalendarInclusive != null) {

			if (fromCalendarInclusive.before(toCalendarInclusive)) {

				// when we process discrete dates we reset the hour.
				clearTimeFromCalendar(fromCalendarInclusive);

				while(fromCalendarInclusive.before(toCalendarInclusive) || fromCalendarInclusive.equals(toCalendarInclusive)) {
					Calendar discreteDate = (GregorianCalendar) fromCalendarInclusive.clone();
					discreteDatesInPeriod.add(discreteDate);
					fromCalendarInclusive.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			else { // check if both dates are equal and return one date only
				clearTimeFromCalendar(fromCalendarInclusive);
				clearTimeFromCalendar(toCalendarInclusive);
				if (fromCalendarInclusive.equals(toCalendarInclusive)) {
					Calendar discreteDate = (GregorianCalendar) fromCalendarInclusive.clone();
					discreteDatesInPeriod.add(discreteDate);
				}
			}
		}
		else if (fromCalendarInclusive != null && toCalendarInclusive == null) { // we are looking for a specific date
			// when we process discrete dates we reset the hour. we assume that the date field for which we set the criteria contains date(s) without hour data. 
			clearTimeFromCalendar(fromCalendarInclusive);
			Calendar discreteDate = (GregorianCalendar) fromCalendarInclusive.clone();
			discreteDatesInPeriod.add(discreteDate);
		}
		else if (fromCalendarInclusive == null && toCalendarInclusive != null) { // we are looking for a specific date
			// when we process discrete dates we reset the hour. we assume that the date field for which we set the criteria contains date(s) without hour data. 
			clearTimeFromCalendar(toCalendarInclusive);
			Calendar discreteDate = (GregorianCalendar) toCalendarInclusive.clone();
			discreteDatesInPeriod.add(discreteDate);
		}
		
		return discreteDatesInPeriod;
		
	}
	
	public void setCalendarToStartOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	public void setCalendarToMidDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	
	public void setCalendarToEndOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
	}
	
	public  void clearTimeFromCalendar(Calendar calendar){
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
		
	}
	
	public Date getCurrentDate(TimeZone zone, Locale locale) {
		return GregorianCalendar.getInstance(zone, locale).getTime();
	}
	
	public String getCurrentDateAsString() {
		return convertDateToString(GregorianCalendar.getInstance().getTime(), PortalStringConstants.DATE_FORMAT_PATTERN);
	}
}
