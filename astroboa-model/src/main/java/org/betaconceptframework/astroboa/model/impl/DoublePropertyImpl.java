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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.DoublePropertyDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DoublePropertyImpl extends SimpleCmsPropertyImpl<Double, DoublePropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> implements DoubleProperty , Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3641749053477734308L;

	public ValueType getValueType() {
		return ValueType.Double;
	}

	@Override
	protected String generateMessageForInvalidValue(Double value) {
		if (getPropertyDefinition() == null){
			return "Provided value "+value+" is invalid";
		}
		
		if (getPropertyDefinition().getValueEnumeration() != null && ! getPropertyDefinition().getValueEnumeration().isEmpty()){
			return "Provided value "+value+" is not within enumeration "+ getPropertyDefinition().getValueEnumeration();
		}
		
		return "Provided value "+value+" is invalid: Min Value  "+getPropertyDefinition().getMinValue() +", Max Value :"+getPropertyDefinition().getMaxValue();
	}

}
