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


import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Represents a restriction for a property. 
 * 
 * For example
 * <pre>
 *  
 *  title = 'MyTile'  
 *  
 *  title = 'MyTile' or title = 'AnotherTitle'
 *  </pre>
 * 
 * <p>
 * Such a restriction corresponds to a specific property , 
 * contains a {@link QueryOperator query operator}
 * and one or more criterion values. In case 
 * property's value must satisfy more than one values
 * an internal condition is set to describe the way these values must be set.
 * </p>
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface SimpleCriterion extends Criterion {

	public enum CaseMatching{
		LOWER_CASE,
		UPPER_CASE,
		NO_CASE
	}
	
	/**
	 * Returns criterion values that must be met by property.
	 * 
	 * @return One or more values that property must meet.
	 */
	List<Object> getValues();

	/**
	 * Sets criterion values that must be met by property.
	 * 
	 * @param values One or more values that property must meet.
	 */
	void setValues(List<Object> values);

	/**
	 * Returns the internal condition for the restriction.
	 *  
	 * Default value is {@link Condition#AND}. 
	 * 
	 * @return Internal condition for the restriction.
	 */
	Condition getInternalCondition();

	/**
	 * Sets the internal condition for the restriction. 
	 * Default value is {@link Condition#AND}.
	 * 
	 * Applicable only if there are more than one values set.
	 * 
	 * @param condition Internal condition for the restriction.
	 */
	void setInternalCondition(Condition condition);
	
	/**
	 * Adds a value that property must meet.
	 * 
	 * @param value A value that property must meet.
	 */
	void addValue(Object value);

	/**
	 * Returns the query operator for criterion.
	 *  
	 * @return Query operator for criterion.
	 */
	QueryOperator getOperator();

	/**
	 * Sets the query operator for criterion.
	 * 
	 * Default is {@link QueryOperator#EQUALS}.
	 * 
	 * @param operator Query operator for criterion.
	 */
	void setOperator(QueryOperator operator);
	
	/**
	 * Returns the property used in criterion.
	 * 
	 * @return Property path used in criterion.
	 */
	String getProperty();

	/**
	 * Sets the property used in criterion. 
	 * 
	 * <p>
	 * Note that this is the property's path as defined 
	 * in {@link CmsProperty#getPath()} for specific properties
	 * or as defined in {@link CmsPropertyDefinition#getPath()}
	 *  for properties of the same path. For example,
	 * <code>profile.title</code> indicates to query all properties 
	 * <code>title</code> which belong to any property <code>profile</code>
	 * <code>profile.title[1]</code>
	 * indicates to query all properties <code>profile</code> whose
	 * FIRST property <code>title</code> matches the specified criterion.
	 * </p>
	 * 
	 * @param propertyPath
	 */
	void setProperty(String propertyPath);
	
	/**
	 * Reset all values for criterion.
	 */
	void clearValues();
	
	/**
	 * Sets case matching ONLY for properties of type {@link ValueType#String String}.
	 * 
	 * Applicable only for {@link QueryOperator#EQUALS equals}, {@link QueryOperator#NOT_EQUALS not equals},
	 * and {@link QueryOperator#LIKE like}.
	 * 
	 * Default value is {@link CaseMatching#NO_CASE} which means that the value(s) provided
	 * must match exactly, that is, it is case sensitive.
	 * 
	 * This is useful in cases where case sensitivity needs to be disabled.
	 * 
	 * For example there is a property <code>name</code> and there is a need to search 
	 * for all properties which have the value 'bar'. If {@link CaseMatching#LOWER_CASE lower case}
	 * is enabled then it will match all properties which have the values : 
	 * <code>bar</code>, <code>Bar</code>,
	 * <code>BAR</code>, <code>BaR</code>, etc.
	 * Note that the value provided in the example is also in lower case.
	 *  
	 * @param caseMatching 
	 */
	void setCaseMatching(CaseMatching caseMatching);
	
	/**
	 * Get the case matching for this criterion. 
	 * 
	 * Default value is {@link CaseMatching#NO_CASE no case}.
	 * 
	 * @return
	 * 		Criterion case matching.
	 */
	CaseMatching getCaseMatching();
	
	
	

	}
