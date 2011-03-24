/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.test.log;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TestLogPolicy {

	private static Map<String, Level> enableLevelForLogger = new HashMap<String, Level>();

	public static int shouldWriteLogEventForLogger(Level level, String loggerName)
	{
		
		if (enableLevelForLogger.containsKey(loggerName) )
		{
			Level enabledLevel = enableLevelForLogger.get(loggerName);
			
			if (enabledLevel != null)
			{
				return level.isGreaterOrEqual(enabledLevel) ? Filter.ACCEPT : Filter.DENY;
			}
			
		}
		
		return Filter.NEUTRAL;
		
	}
	
	
	public static void setLevelForLogger(Level level, String loggerName)
	{
		enableLevelForLogger.put(loggerName, level);
	}
	
	public static void setDefaultLevelForLogger(String loggerName)
	{
		if (loggerName != null)
		{
			enableLevelForLogger.remove(loggerName);
		}
	}

}
