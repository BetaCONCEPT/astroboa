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

package org.betaconceptframework.astroboa.model.impl.query.criteria;


import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.criteria.ConditionalCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ConditionalCriterionImpl  implements ConditionalCriterion, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5723439360095976362L;
	
	private Criterion leftHandSide;
	private Criterion rightHandSide;
	private Condition condition = Condition.AND;

	public ConditionalCriterionImpl(){
		
	}
	public ConditionalCriterionImpl(Criterion leftHandSide, Criterion rightHandSide, Condition condition)
	{
		this.leftHandSide = leftHandSide;
		this.rightHandSide = rightHandSide;
		this.condition = condition;

	}

	public String getXPath() {

		String leftHandSideXPath = (leftHandSide != null)? leftHandSide.getXPath() : "";
		String rightHandSideXPath = (rightHandSide != null)? rightHandSide.getXPath() : "";
		
		boolean hasLeftHandSide = StringUtils.isNotBlank(leftHandSideXPath);
		boolean hasRightHandSide = StringUtils.isNotBlank(rightHandSideXPath);
		
		boolean hasBothSides = hasLeftHandSide && hasRightHandSide;
		
		String conditionalCriterion = (hasLeftHandSide? leftHandSideXPath: "")+
		(hasBothSides?CmsConstants.EMPTY_SPACE+condition.toString().toLowerCase()+CmsConstants.EMPTY_SPACE:"") + 
		(hasRightHandSide? rightHandSideXPath : "");
		
		if (StringUtils.isNotBlank(conditionalCriterion))
			return CmsConstants.LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE+ conditionalCriterion+CmsConstants.RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE;
		
		return "";
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Criterion getLeftHandSide() {
		return leftHandSide;
	}

	public void setLeftHandSide(Criterion leftHandSide) {
		this.leftHandSide = leftHandSide;
	}

	public Criterion getRightHandSide() {
		return rightHandSide;
	}

	public void setRightHandSide(Criterion rightHandSide) {
		this.rightHandSide = rightHandSide;
	}
	
	

}

