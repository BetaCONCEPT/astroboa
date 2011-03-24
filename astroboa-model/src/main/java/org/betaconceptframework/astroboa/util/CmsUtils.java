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


import org.apache.lucene.analysis.el.GreekCharsets;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsUtils {

	public static String replaceLast(String find, String replace, String source)
	   {
	      String result = "";

	      if (source.indexOf(find) >= 0)
	         {
	         result += source.substring(0, source.lastIndexOf(find)) + replace;
	         source = source.substring(source.lastIndexOf(find) + find.length());
	      }
	      result += source;

	      return result;
	   }
	
	public static int getNextDepth(int depth) {
		int nextDepth = TreeDepth.ZERO.asInt();
		if (depth == TreeDepth.FULL.asInt())
			nextDepth = TreeDepth.FULL.asInt();
		else if (depth != TreeDepth.ZERO.asInt())
			nextDepth = depth - 1;
		return nextDepth;
	}
	
	public static String filterGreekCharacters(String greekLocalizedLabel)
	{
	 char[] chArray = greekLocalizedLabel.toCharArray();
     for (int i = 0; i < chArray.length; i++)
     {
         chArray[i] = GreekCharsets.toLowerCase(chArray[i], GreekCharsets.UnicodeGreek);
     }
     
     	return new String(chArray);
	}    
	
}
