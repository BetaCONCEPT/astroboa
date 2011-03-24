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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.api.model.exception.SingleOccurenceException;
import org.betaconceptframework.astroboa.util.DateUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CalendarPropertyImpl extends SimpleCmsPropertyImpl<Calendar, CalendarPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> implements CalendarProperty, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4731078528668135011L;



	public Date getSimpleTypeValueAsDate() throws MultipleOccurenceException {
		Object simpleTypeValue = getSimpleTypeValue();

		if (simpleTypeValue !=null)
		{ 
			if (simpleTypeValue instanceof Calendar)
				return ((Calendar) simpleTypeValue).getTime();

			return (Date)simpleTypeValue;
		}

		return null;
	}



	public void setSimpleTypeValueAsDate(Date date)
	throws MultipleOccurenceException {

		if (date == null)
			setSimpleTypeValue(null);
		else
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			setSimpleTypeValue(cal);
		}
	}



	@Override
	public void addSimpleTypeValue(Calendar value) {
		value = clearValueFromTimeIfNecessary(value);
		
		super.addSimpleTypeValue(value);
	}



	private Calendar clearValueFromTimeIfNecessary(Calendar value) {
		if (value == null || getPropertyDefinition() == null || getPropertyDefinition().isDateTime()){
			return value;
		}
		
		//Property's type is only Date. Clear time
		return DateUtils.clearTimeFromCalendar(value);
	}



	@Override
	public void setSimpleTypeValue(Calendar value)
			throws MultipleOccurenceException {
		value = clearValueFromTimeIfNecessary(value);
		super.setSimpleTypeValue(value);
	}



	@Override
	public void setSimpleTypeValues(List<Calendar> values)
			throws SingleOccurenceException {
		
		values = clearValuesFromTimeIfNecessary(values);
		super.setSimpleTypeValues(values);
	}



	private List<Calendar> clearValuesFromTimeIfNecessary(List<Calendar> values) {
		if (CollectionUtils.isEmpty(values)|| getPropertyDefinition() == null || getPropertyDefinition().isDateTime()){
			return values;
		}
		
		//Property's type is only Date. Clear time
		for (Calendar cal : values){
			cal = DateUtils.clearTimeFromCalendar(cal); 
		}
		
		return values; 
	}



	public ValueType getValueType() {
		return ValueType.Date;
	}
	
	protected void checkValues(List<Calendar> asList) {
		//Do nothing. Currently no enumeration is provided for Calendar properties
		
	}

	@Override
	protected String generateMessageForInvalidValue(Calendar value) {
		return "Provided value "+value+" is invalid";
	}
}
