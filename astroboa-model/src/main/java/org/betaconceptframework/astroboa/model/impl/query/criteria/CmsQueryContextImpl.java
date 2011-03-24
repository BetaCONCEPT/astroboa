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

package org.betaconceptframework.astroboa.model.impl.query.criteria;


import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsQueryContext;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
abstract class CmsQueryContextImpl implements CmsQueryContext, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9008341995480272009L;
	
	private int offset = 0;
	private int limit = CmsQueryContext.UNLIMITED;
	
	public int getLimit() {
		return limit;
	}
	public int getOffset() {
		return offset;
	}
	
	private RenderProperties renderProperties = new RenderPropertiesImpl();

	private Map<String, Order> orderProperties = new LinkedHashMap<String, Order>();

	/**
	 * @return the renderProperties
	 */
	public RenderProperties getRenderProperties()
	{
		return renderProperties;
	}

	/**
	 * @return the orderProperties
	 */
	public Map<String, Order> getOrderProperties() {
		return orderProperties;
	}

	public void resetOrderProperties()
	{
		if (orderProperties != null){
			orderProperties.clear();
		}
	}
	
	public void addOrderProperty(String propertyPath, Order order)
	{
		if (StringUtils.isNotBlank(propertyPath) && order != null){
			if (orderProperties == null){
				orderProperties = new HashMap<String, Order>();
			}
				orderProperties.put(propertyPath, order);
		}
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public void setOffset(int offset) {
		this.offset = offset;	
	}
	public void setOffsetAndLimit(int offset, int limit) {
		setOffset(offset);
		setLimit(limit);
		
	}

	public void removeOrderProperty(String propertyPath) {
		if (StringUtils.isNotBlank(propertyPath) && orderProperties != null && orderProperties.containsKey(propertyPath)){
			orderProperties.remove(propertyPath);
		}
		
	}
	
}


