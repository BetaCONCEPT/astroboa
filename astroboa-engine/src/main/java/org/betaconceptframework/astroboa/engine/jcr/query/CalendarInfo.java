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

package org.betaconceptframework.astroboa.engine.jcr.query;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CalendarInfo {

	private Calendar calendar;
	
	public CalendarInfo(Calendar calendar) throws Exception
	{
		if (calendar == null)
			throw new Exception("Null calendar");
		
		this.calendar = calendar;
		
		setDefaultValues();
	}
	
	public Calendar getCalendar()
	{
		return calendar;
	}
	
	private void setDefaultValues() {
		if (!calendar.isSet(Calendar.DAY_OF_MONTH)){
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}
		if (!calendar.isSet(Calendar.MONTH)){
			calendar.set(Calendar.MONTH, Calendar.JANUARY);
		}
		if (!calendar.isSet(Calendar.YEAR)){
			calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}
		if (!calendar.isSet(Calendar.HOUR_OF_DAY)){
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		}
		if (!calendar.isSet(Calendar.MINUTE)){
			calendar.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
		}
		if (!calendar.isSet(Calendar.SECOND)){
			calendar.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
		}
	}

	public CalendarInfo(Date date) throws Exception
	{
		if (date == null)
			throw new Exception("Null date");
		
		this.calendar = Calendar.getInstance();
		this.calendar.setTime(date);
		
		setDefaultValues();
	}
	
	public String getDay(){
		return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	public String getMonth(){
		
		//Java Calendar starts counting months from zero
		//Thus January has 0 as a value
		//Increase by one
		return String.valueOf(calendar.get(Calendar.MONTH) + 1);
	}

	public String getYear(){
		return String.valueOf(calendar.get(Calendar.YEAR));
	}

	public String getHour(){
		return String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
	}
	
	public String getMinute(){
		return String.valueOf(calendar.get(Calendar.MINUTE));
	}
	
	public String getSecond(){
		return String.valueOf(calendar.get(Calendar.SECOND));
	}
	/**
	 * Used for backwards compatibility
	 * @return
	 */
	public String getPathUntilDay(){
		return getYear() +"/" + getMonth() + "/" + getDay();
	}
	
	public String getFullPath(){
		return getYear() +"/" + getMonth() + "/" + getDay()+  "/" + getHour()+ "/" + getMinute()+ "/" + getSecond();
	}
}
