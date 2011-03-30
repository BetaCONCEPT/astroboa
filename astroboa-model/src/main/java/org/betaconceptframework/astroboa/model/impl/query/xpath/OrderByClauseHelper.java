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

	//Represents the projection of XPath
	//Used when properties in order by clause are not direct
	//children of the jcr node to be queried
	//otherwise order by will not be considered
	//This is due to the lack of support of child axis in order by clause
	//by Jackrabbit
	private String columnProjectionInXPath = "";

	private String orderByClause = "";

	private int numberOfColumnsParticipatingInProjection = 0;

	public int getNumberOfColumnsParticipatingInProjection() {
		return numberOfColumnsParticipatingInProjection;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public String getColumnProjectionInXPath() {
		return columnProjectionInXPath;
	}

	public OrderByClauseHelper(Map<String, Order> orderProperties) {
		this.orderProperties = orderProperties;
	}

	public void generateOrderBy() {
		orderByClause = CmsConstants.EMPTY_SPACE + CmsConstants.ORDER_BY + CmsConstants.EMPTY_SPACE;

		if (MapUtils.isEmpty(orderProperties))
		{
			columnProjectionInXPath = "";
			orderByClause = "";
			numberOfColumnsParticipatingInProjection = 0;
		}
		else 
		{
			boolean orderByStart = true;
			boolean selectedColumnsStart = true;
			for (Entry<String, Order> propertyEntry : orderProperties.entrySet()) {

				final String property = propertyEntry.getKey();
				final Order order = propertyEntry.getValue();

				if (StringUtils.isNotBlank(property) && order != null){
					
					String propertyXPathRepresentation = XPathUtils.generateJcrPathForPropertyPath(property, true);
					
					String valueToBePlacedInColumnProjection = null;
					String valueToBePlacedInOrderByClause = null;
					
					if (propertyXPathRepresentation.startsWith(CmsConstants.AT_CHAR) ){
						valueToBePlacedInColumnProjection ="";
						valueToBePlacedInOrderByClause = propertyXPathRepresentation;
					}
					else{
						
						valueToBePlacedInColumnProjection = StringUtils.substringBeforeLast(propertyXPathRepresentation, CmsConstants.FORWARD_SLASH);
						valueToBePlacedInOrderByClause = StringUtils.substringAfterLast(propertyXPathRepresentation, CmsConstants.FORWARD_SLASH);
						
						if (StringUtils.isBlank(valueToBePlacedInOrderByClause)){
							valueToBePlacedInOrderByClause = propertyXPathRepresentation;						
						}
					}
					
					
					//Build orderBy
					orderByClause = orderByClause.concat((orderByStart ? "" : CmsConstants.COMMA)
							+ getOrderByProperty(valueToBePlacedInOrderByClause,	order));

					if (orderByStart){
						orderByStart = false;
					}

					//Build selectedColumns
					if (propertyEntry.getValue() == Order.ascending || propertyEntry.getValue() == Order.descending)
					{
						//Generate appropriate column specifier only if property 
						//is a complex one (i.e. its path contains '.' or '/' character)
						if (isNotJcrFunction(property) && StringUtils.isNotBlank(valueToBePlacedInColumnProjection)){
							numberOfColumnsParticipatingInProjection++;
							columnProjectionInXPath = columnProjectionInXPath.concat(
									(selectedColumnsStart ? CmsConstants.LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE : " | ")
									+ CmsConstants.EMPTY_SPACE+ valueToBePlacedInColumnProjection);

							if (selectedColumnsStart){
								selectedColumnsStart = false;
							}
						}
					}
				}
			}

			if (StringUtils.isNotBlank(columnProjectionInXPath))
				columnProjectionInXPath = CmsConstants.FORWARD_SLASH +  columnProjectionInXPath + CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE;

			//Now Jackrabbit seems to ignore more than one columns in projection in respect to order by clause
			//especially if these properties are NOT properties of the queried node type. If this is the case
			//then selected columns will not appear in query. Ordering must take place outside repository query
			//Also any all properties must not appear in order by clause as well
			if (astroboaEngineWillOrderResults())
			{
				columnProjectionInXPath = "";
				orderByClause = "";
			}
		}

	}

	public boolean astroboaEngineWillOrderResults() {
		return numberOfColumnsParticipatingInProjection > 1 || 
			(numberOfColumnsParticipatingInProjection == 1 &&
				orderProperties.size() > numberOfColumnsParticipatingInProjection );
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

	private static boolean isNotJcrFunction(String item) {

		return ( ! JcrBuiltInItem.JcrDeref.getJcrName().equals(item) &&
				! JcrBuiltInItem.JcrScore.getJcrName().equals(item) &&
				! JcrBuiltInItem.JcrContains.getJcrName().equals(item) && 
				! JcrBuiltInItem.JcrLike.getJcrName().equals(item));
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
