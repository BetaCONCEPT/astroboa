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


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrValueUtils;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsScoreNode  implements Comparable<CmsScoreNode>{

	private  final Logger logger = LoggerFactory.getLogger(CmsScoreNode.class);

	private Node node;
	private double score=0;
	private Map<String ,Object> valuesToBeCompared = new HashMap<String, Object>();
	private Map<String, Order> orderProperties = new LinkedHashMap<String, Order>();
	
	public Node getJcrNode()
	{
		return node;
	}

	public CmsScoreNode(Node node, double score)
	{
		this.node = node;
		
		try {
			if (this.node != null)
			{
				while (!this.node.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()))
				{
					this.node = this.node.getParent();
				}
			}
		} catch (Exception e) {
			throw new CmsException(e);
		}
		this.score = score;
		
		
	}
	
	public void setOrderProperties(Map<String, Order> orderProperties)
	{
		if (orderProperties != null && ! orderProperties.isEmpty())
		{
			this.orderProperties.putAll(orderProperties);
		}
	}

	public double getScore()
	{
		return score;
	}
	
	public Comparable getValueFromMap(String property) throws RepositoryException
	{
		if (! valuesToBeCompared.containsKey(property)){
			if (JcrBuiltInItem.JcrScore.getJcrName().equals(property))
			{
				//Check JcrScore returned in rows
				valuesToBeCompared.put(property, score);
			}
			else
			{
				if (node != null){
					Object valueForProperty = getValueForProperty(node, property);
					
					//It could be null
					valuesToBeCompared.put(property, valueForProperty);
				}
			}
		}
		return (Comparable) valuesToBeCompared.get(property);
	}

	public int compareTo(CmsScoreNode rowToBeCompared) {
    
		try{

			if (rowToBeCompared == null)
			{
				return 1;
			}
			else if (node == null)
			{
				return -1;
			}
			else
			{
				if (logger.isDebugEnabled()){
					logger.debug("\n\nComparing nodes : {} and {}", node.getPath(), rowToBeCompared.getJcrNode().getPath());
				}
				
				for (Entry<String, Order> orderBy : orderProperties.entrySet())
				{
					String property = orderBy.getKey();
					Order order = orderBy.getValue();

					logger.debug("Comparing property {} with order {}", property, order);
					int orderWeight = 1;
					if (order == Order.descending)
						orderWeight = -1;

					//find appropriate values
					Comparable thisValue = getValueFromMap(property);
					Comparable valueToBeCompared = rowToBeCompared.getValueFromMap(property);

					int valueCompare = compareValues(thisValue, valueToBeCompared);
					
					logger.debug("Value for property of first node {}, Value for property of second node {}, Comparison outcome {}",
							new Object[]{thisValue, valueToBeCompared, valueCompare});
					
					if (valueCompare != 0){
						logger.debug("Exiting loop.");
						return  valueCompare * orderWeight;
					}
				}
				
				//All order properties are equal
				return 0;
			}
		}
		catch(Exception e)
		{
			logger.warn("",e);
			return  -1;
		}

		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result
				+ ((orderProperties == null) ? 0 : orderProperties.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime
				* result
				+ ((valuesToBeCompared == null) ? 0 : valuesToBeCompared
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CmsScoreNode other = (CmsScoreNode) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (orderProperties == null) {
			if (other.orderProperties != null)
				return false;
		} else if (!orderProperties.equals(other.orderProperties))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		if (valuesToBeCompared == null) {
			if (other.valuesToBeCompared != null)
				return false;
		} else if (!valuesToBeCompared.equals(other.valuesToBeCompared))
			return false;
		return true;
	}

	private Object getValueForProperty(Node node, String property) throws   RepositoryException  {
		String propertyPath = PropertyPath.getJcrPropertyPath(property);

		if (node.hasProperty(propertyPath)){
			Property jcrProperty = node.getProperty(propertyPath);
			
			if (!jcrProperty.getDefinition().isMultiple()){
				Value value = node.getProperty(propertyPath).getValue();
				return JcrValueUtils.getObjectValue(value); 
			}
			else{
				Value[] values = node.getProperty(propertyPath).getValues();
				if (ArrayUtils.isEmpty(values)){
					return null;
				}
				else{
					//	Get the last value to compare with
					return JcrValueUtils.getObjectValue(values[values.length-1]);
				}
			}
		}
		else{
			if (logger.isDebugEnabled()){
				logger.debug("Node {} does not have property {}", node.getPath(), propertyPath );
			}
		}
		
		return null;
		
	}

	private int compareValues(Comparable value1, Comparable value2) throws   RepositoryException {

		if (value1 == null){
			return (value2 == null ? 0: -1);
		}
		else{
			return (value2 != null ? value1.compareTo(value2): 1);
		}
		
	}

}
