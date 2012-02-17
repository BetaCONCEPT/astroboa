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

import org.betaconceptframework.astroboa.api.model.query.Condition;

/**
 * Represents a complex criterion whose both sides are other criteria.
 * For example
 * 
 * <code> ( title = 'Mytile' ) AND ( name = 'MyName') </code>  
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ConditionalCriterion extends Criterion {

	/**
	 * Sets the left hand side of the criterion.
	 * @param leftHandSide Left hand side criterion.
	 */
	void setLeftHandSide(Criterion leftHandSide);

	/**
	 * Returns the left hand side of the criterion.
	 * @return Left hand side criterion.
	 */
	Criterion getLeftHandSide();

	/**
	 * Sets the right hand side of the criterion.
	 * @param rightHandSide Right hand side criterion.
	 */
	void setRightHandSide(Criterion rightHandSide);

	/**
	 * Returns the right hand side of the criterion.
	 * @return Right hand side criterion.
	 */
	Criterion getRightHandSide();

	/**
	 * Sets the condition for this criterion.
	 * @param condition Criterion condition.
	 */
	void setCondition(Condition condition);

	/**
	 * Returns the condition for this criterion. 
	 * Default value is {@link Condition#AND}.
	 * @return Criterion condition.
	 */
	Condition getCondition();

}
