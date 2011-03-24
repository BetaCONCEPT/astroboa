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

package org.betaconceptframework.astroboa.engine.jcr.query;


import java.io.Serializable;
import java.util.Comparator;



/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class OrderByComparator implements Comparator<CmsScoreNode>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3046640993638888544L;

	private int count = 0;
	
	public OrderByComparator()
	{
	}

	public int compare(CmsScoreNode row1, CmsScoreNode row2) {

		int result = 0;
		count++;
		if (row1 == null)
			result = -1;
		else if (row2 == null)
			result = 1;
		else
		{
			result = row1.compareTo(row2);
		}

		return result;
	}

}
