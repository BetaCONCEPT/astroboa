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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class StringPropertyImpl extends SimpleCmsPropertyImpl<String, StringPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> implements StringProperty, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -237085227118289350L;

	public ValueType getValueType() {
		return  ValueType.String;
	}

	@Override
	protected String generateMessageForInvalidValue(String value) {
		if (getPropertyDefinition() != null && getPropertyDefinition().getValueEnumeration() != null && ! getPropertyDefinition().getValueEnumeration().isEmpty()){
			return "Provided value "+value+" is not within enumeration "+ getPropertyDefinition().getValueEnumeration();
		}
		
		return "Provided value "+value+" is invalid";
	}
	

}
