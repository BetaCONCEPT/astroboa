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
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ObjectReferencePropertyImpl  extends SimpleCmsPropertyImpl<ContentObject, ObjectReferencePropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> implements ObjectReferenceProperty, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8308985889892542813L;

	public ValueType getValueType() {
		return ValueType.ObjectReference;
	}

	@Override
	protected String generateMessageForInvalidValue(ContentObject value) {
		return value == null ? "ContentObject is null" : "ContentObject "+value.getId() + " is of type "+value.getContentObjectType()+ 
				", property " + getFullPath() + 
				(getPropertyDefinition() == null ? " has no attached definition" :
					(getPropertyDefinition().getAcceptedContentTypes() == null || getPropertyDefinition().getAcceptedContentTypes().isEmpty()? 
							" accepts contentObject of any type as values " : 
							" accepts contentObject of the following types :'"+
							getPropertyDefinition().getExpandedAcceptedContentTypes()+"' as values")
							);
	}
}
