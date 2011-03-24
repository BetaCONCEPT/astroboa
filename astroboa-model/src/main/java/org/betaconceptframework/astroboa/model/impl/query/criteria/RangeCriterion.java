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

import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion.CaseMatching;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RangeCriterion implements Criterion, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5539900884329205524L;
	private Object upperLimit;
	private Object lowerLimit;
	private String property;
	
	public Object getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(Object lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(Object upperLimit) {
		this.upperLimit = upperLimit;
	}
	
	public String getXPath() {
		
		Criterion lessThanOrEqualsCriterion = CriterionUtils.createSimpleCriterion(property, upperLimit,  QueryOperator.LESS_EQUAL, CaseMatching.NO_CASE);
		Criterion greaterThanOrEqualsCriterion = CriterionUtils.createSimpleCriterion(property, lowerLimit, QueryOperator.GREATER_EQUAL, CaseMatching.NO_CASE);
		
		return new ConditionalCriterionImpl(lessThanOrEqualsCriterion, greaterThanOrEqualsCriterion, Condition.AND).getXPath();
		
		
	}
	
	
}
