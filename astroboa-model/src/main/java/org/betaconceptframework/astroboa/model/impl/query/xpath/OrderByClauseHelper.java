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
package org.betaconceptframework.astroboa.model.impl.query.xpath;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class OrderByClauseHelper {

	private Map<String, Order> orderProperties;

	public OrderByClauseHelper(Map<String, Order> orderProperties) {
		this.orderProperties = orderProperties;
	}

	public OrderByClauseHelper(String property, Order order) {
		this.orderProperties = new HashMap<String, Order>();
		
		if (StringUtils.isNotBlank(property) && order != null){
			this.orderProperties.put(property, order);
		}
	}
	
	public String generateOrderBy() {

		if (MapUtils.isEmpty(orderProperties)){
			return  "";
		}
		else {
			String orderByClause = CmsConstants.EMPTY_SPACE + CmsConstants.ORDER_BY + CmsConstants.EMPTY_SPACE;

			boolean orderByStart = true;
			
			for (Entry<String, Order> propertyEntry : orderProperties.entrySet()) {

				final String property = propertyEntry.getKey();
				final Order order = propertyEntry.getValue();

				if (StringUtils.isNotBlank(property) && order != null){
					
					String propertyXPathRepresentation = XPathUtils.generateJcrPathForPropertyPath(property, true);
					
					String valueToBePlacedInOrderByClause = propertyXPathRepresentation;
					
					//Build orderBy
					orderByClause = orderByClause.concat((orderByStart ? "" : CmsConstants.COMMA)
							+ getOrderByProperty(valueToBePlacedInOrderByClause,	order));

					if (orderByStart){
						orderByStart = false;
					}

				}
			}
			
			return orderByClause;
		}

	}

	private static String getOrderByProperty(String property, Order order) {
		if (order == null)
			order = Order.ascending;

		String propertyXPath = XPathUtils.generateJcrPathForPropertyPath(property, true);

		switch (order) {
		case ascending:
		case descending:
			
			boolean propertyNameIsJcrScore = property != null && (
					StringUtils.equals(property.replaceFirst(CmsConstants.AT_CHAR, ""),JcrBuiltInItem.JcrScore.getJcrName())
					);
			
			if (propertyNameIsJcrScore)
			{
				return jcrScoreOrderBy(propertyXPath, order);
			}
			
			return propertyXPath + CmsConstants.EMPTY_SPACE + order.toString();

		default:
			return "";
		}
	}

	private static String jcrScoreOrderBy(String property, Order order) {
		
		//It may be the case that property is @jcr:score
		//And we need to remove first 
		boolean propertyNameIsJcrScore = property != null && (
				StringUtils.equals(property.replaceFirst(CmsConstants.AT_CHAR, ""),JcrBuiltInItem.JcrScore.getJcrName())
				);
		
		return JcrBuiltInItem.JcrScore.getJcrName()
		+ CmsConstants.LEFT_PARENTHESIS 
		+ (propertyNameIsJcrScore ? "" : property)
		+ CmsConstants.RIGHT_PARENTHESIS+CmsConstants.EMPTY_SPACE
		+ order.toString();
	}

	public static Map<String, Order> extractOrderPropertiesFromQuery(String xpathSearchQuery) {
		Map<String, Order> orderProperties = new HashMap<String, Order>();

		if (StringUtils.isNotBlank(xpathSearchQuery))
		{
			//First delete OrderByClause
			xpathSearchQuery = xpathSearchQuery.replaceFirst(CmsConstants.ORDER_BY, "");

			//Then separate each order by clause
			String[] orderTokens = StringUtils.split(xpathSearchQuery, CmsConstants.COMMA);

			if (!ArrayUtils.isEmpty(orderTokens))
			{
				for (String orderToken : orderTokens)
				{
					Order order = null;
					String property = null;

					//Look for ascending or descending
					if (orderToken.contains(Order.ascending.toString()))
					{
						order = Order.ascending;
						property = orderToken.replace(Order.ascending.toString(), "").trim();
					}
					else if (orderToken.contains(Order.descending.toString()))
					{
						order = Order.descending;
						property = orderToken.replace(Order.descending.toString(), "").trim();
					}

					orderProperties.put(property, order);

				}
			}
		}

		return orderProperties;
	}

}
