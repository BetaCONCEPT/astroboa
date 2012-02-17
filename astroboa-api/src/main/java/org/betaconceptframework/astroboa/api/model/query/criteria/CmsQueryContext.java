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

package org.betaconceptframework.astroboa.api.model.query.criteria;

import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;

/**
 * Represents the base context for every query performed 
 * through Astroboa criteria API.
 * 
 * A query consists of its {@link CmsCriteria criteria},
 * an offset and a limit for query results and
 * zero or more order properties and {@link RenderProperties render properties}.
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsQueryContext {
  
	/**
	 * Flag indicating unlimited value.
	 * 
	 * Used for {@link #setOffset(int) offset} and
	 * {@link #setLimit(int) limit}.
	 */
	int UNLIMITED = -1;
	/**
	 * Returns the limit of the result rows.
	 * 
	 * @return Result rows limit.
	 */
	int getLimit();

	/**
	 * Returns the offset of the result rows (zero-based).
	 *
	 * 
	 * @return Result rows offset.
	 */
	int getOffset();

	/**
	 * Sets the offset of the result rows (zero-based).
	 * 	<p>
	 * Used in combination with {@link #setLimit(int) limit} in order to
	 * perform paging in query results.
	 * </p>
	 * 
	 * <p>
	 * Default value is zero.
	 * </p>
	 *  
	 * @param offset
	 *            Result rows offset.
	 */
	void setOffset(int offset);

	/**
	 * Sets the limit of the result rows, that is how many rows the result set will contain.
	 *
	 * <p>
	 * Default value is {@link #UNLIMITED}.
	 * 
	 * Negative value specifies no limit.
	 * </p>
	 * 
	 * @param limit
	 *            Result rows limit.
	 */
	void setLimit(int limit);

	/**
	 * Sets the boundaries of the result rows (zero-based).
	 * 
	 * @param offset
	 *            Result rows offset.
	 * @param limit
	 *            Result rows limit.
	 */
	void setOffsetAndLimit(int offset, int limit);
	
	/**
	 * Returns render properties of the query.
	 * 
	 * @return Query's render properties.
	 */
	RenderProperties getRenderProperties();
	
	/**
	 * Returns <code>order by</code> clause of query as a map.
	 *  
	 * Map entry's order is significant.
	 * 
	 * @return Map containing {@link CmsProperty properties} 
	 * 	participating in query's <code>order by</code> clause.
	 *  Map's key is the property's path as defined 
	 * 	{@link CmsPropertyDefinition#getPath()} 
	 * 	and map's value is actual order.
	 */
	Map<String, Order> getOrderProperties();
	
	/**
	 * Reset query <code>order by</code> clause.
	 */
	void resetOrderProperties();
	
	/**
	 * Adds a {@link CmsProperty property} to query's <code>order by</code> clause.
	 * 
	 * @param propertyPath Property path as defined 
	 * {@link CmsPropertyDefinition#getPath()}. 
	 * 
	 * @param order {@link Order#ascending Ascending} or {@link Order#descending descending} order.
	 */
	void addOrderProperty(String propertyPath, Order order);
	
	/**
	 * Removes a {@link CmsProperty property} from <code>order by</code> clause.
	 * 
	 * @param propertyPath Property path as defined 
	 * {@link CmsPropertyDefinition#getPath()}. 
	 */
	void removeOrderProperty(String propertyPath);

}


