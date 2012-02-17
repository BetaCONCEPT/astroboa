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

package org.betaconceptframework.astroboa.api.model;


import java.util.Calendar;
import java.util.Date;

import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;

/**
 * Marker interface for a simple property whose value(s) is(are) of type
 * {@link ValueType#Date date}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CalendarProperty extends
		SimpleCmsProperty<Calendar, CalendarPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> {

	/**
	 * Returns property's value as a {@link java.util.Date}.
	 * 
	 * @return Calendar value as a {@link java.util.Date}.
	 * @throws MultipleOccurenceException
	 *             If Property is defined as a a multivalue property
	 */
	Date getSimpleTypeValueAsDate() throws MultipleOccurenceException;

	/**
	 * Sets property's value given a {@link java.util.Date} object.
	 * 
	 * @param date
	 * @throws MultipleOccurenceException
	 *             If Property is defined as a a multivalue property
	 */
	void setSimpleTypeValueAsDate(Date date)
			throws MultipleOccurenceException;

	/**
	 * Add method addSimpleTypeValueAsDate(Date date)
	 */
}
