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

import java.util.Comparator;

import org.betaconceptframework.astroboa.portal.utility.TopicComparator.OrderByProperty;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RatedTopicComparator implements Comparator<RatedTopic>{

	private TopicComparator topicComparator;
	private boolean orderByRate;
	private boolean ascendingOrderByRate;
	
	public RatedTopicComparator(String locale, boolean orderByRate, boolean ascendingOrderByRate){
		
		topicComparator = new TopicComparator(locale, OrderByProperty.LABEL);
		this.orderByRate = orderByRate;
		this.ascendingOrderByRate = ascendingOrderByRate;
		
	}
	@Override
	public int compare(RatedTopic ratedTopic1, RatedTopic ratedTopic2) {
		
		//First check if either of beans is null
		if (ratedTopic1 == null){
			return -1;
		}

		if (ratedTopic2 == null){
			return 1;
		}

		int compareResult = 0;
		
		if (orderByRate){
			compareResult = ascendingOrderByRate ? ratedTopic1.getRate() - ratedTopic2.getRate() : ratedTopic2.getRate() - ratedTopic1.getRate();
			
		}

		if (compareResult == 0){
				return topicComparator.compare(ratedTopic1.getTopic(), ratedTopic2.getTopic());
		}
		
		return compareResult;

	}


}
