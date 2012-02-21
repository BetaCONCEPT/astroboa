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
package org.betaconceptframework.astroboa.console.scheduler;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.APPLICATION)
@Name("webUIScheduler")
public class WebUISchedulerBean {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@In
	DraftScheduler draftScheduler;
		
	@Observer(value="org.jboss.seam.postInitialization")
	public void initialize() {
		
		//Run every day at 2 oclock in the morning
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 2);
		
		QuartzTriggerHandle draftSchedule = draftScheduler.scheduleEmptyDraftFromExpiredDraftItems(cal.getTime(), new Long(24*60*60*1000));
		
		}
}
