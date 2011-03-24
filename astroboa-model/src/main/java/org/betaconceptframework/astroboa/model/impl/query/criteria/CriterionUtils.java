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

import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion.CaseMatching;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class CriterionUtils {

	public static  Criterion createSimpleCriterion(String propertyPath, Object value, QueryOperator operator, CaseMatching caseMatching)
	{
		List<Object> values = new ArrayList<Object>();
		
		if (value != null){
				values.add(value);
		}
		
		return createSimpleCriterion(propertyPath, values, null, operator, caseMatching);
	}
	
	public static  Criterion createSimpleCriterion(String propertyPath, List<?> values, Condition internalCondition, QueryOperator operator, CaseMatching caseMatching)
	{
		SimpleCriterion simpleCriterion = new SimpleCriterionImpl();
		simpleCriterion.setProperty(propertyPath);
		simpleCriterion.setOperator(operator);
		simpleCriterion.setCaseMatching(caseMatching);
		
		if (internalCondition != null)
			simpleCriterion.setInternalCondition(internalCondition);
		
		
		if (values != null){
			for (Object value :values)
				simpleCriterion.addValue(value);
		}
		
		return simpleCriterion;
	}

	
}
