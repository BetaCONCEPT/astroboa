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
package org.betaconceptframework.astroboa.console.commons;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.ui.jsf.comparator.LocalizedStringComparator;

/**
 * Compares Topic by their label or their order or by their name. In case the latter two 
 * result to 0 order is done by their localized label.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicComparator extends LocalizedStringComparator<Topic>{

	public enum OrderByProperty{
		LABEL,
		POSITION, 
		NAME
	}

	private OrderByProperty orderByProperty; 

	public TopicComparator(String locale, OrderByProperty orderByProperty){
		super();

		setLocale(locale);

		if (orderByProperty == null){
			this.orderByProperty = OrderByProperty.LABEL;
		}
		else{
			this.orderByProperty = orderByProperty;
		}
	}



	@Override
	public int compare(Topic topic1,
			Topic topic2) {

		//First check if either of beans is null
		if (topic1 == null){
			return -1;
		}

		if (topic2 == null){
			return 1;
		}

		int compareResult = 0;

		switch (orderByProperty) {
		case POSITION:

			if (topic1.getOrder() == null && topic2.getOrder() == null){
				compareResult = 0;
			}
			else{

				if (topic1.getOrder() == null){
					return -1;
				}	

				if (topic2.getOrder() == null){
					return -1;
				}

				compareResult = topic1.getOrder().compareTo(topic2.getOrder());
			}
			break;

		case NAME:
			if (topic1.getName() == null && topic2.getName() == null){
				compareResult = 0;
			}
			else{

				if (topic1.getName() == null){
					return -1;
				}	

				if (topic2.getName() == null){
					return -1;
				}

				compareResult = topic1.getName().compareTo(topic2.getName());
			}
			break;
		default:
			break;
		}

		if (compareResult == 0){
			//Topics have the same order of order is ignored
			//Compare their localized labels
			return super.compare(topic1, topic2);
		}

		return compareResult;
	}



	@Override
	protected int compareLabels(Topic beanWithLocalizedLabel1,
			Topic beanWithLocalizedLabel2) {
		return compareLocalizedNames(beanWithLocalizedLabel1.getLocalizedLabelForLocale(locale), beanWithLocalizedLabel2.getLocalizedLabelForLocale(locale));
	}

}
