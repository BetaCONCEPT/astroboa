/*
i * Copyright (C) 2005-2011 BetaCONCEPT LP.
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

package org.betaconceptframework.astroboa.model.impl.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class ObjectReferencePropertyDefinitionImpl extends SimpleCmsPropertyDefinitionImpl<ContentObject> implements ContentObjectPropertyDefinition, Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -6340139949544472535L;
	
	/*
	 * Contains the content type names defined in XML Schema definition file
	 */
	private Set<String> acceptedContentTypes;
	
	/*
	 * There are cases where acceptedContentTypes list contains the names of
	 * "super" content types only. This list contains all the content types which
	 * extend the "super" content types.
	 * 
	 * Practically this list contains all the accepted content type names
	 *  
	 */
	private Set<String> expandedAcceptedContentTypes;

	public ObjectReferencePropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			String repositoryObjectRestriction,
			Set<String> acceptedContentTypes) {
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				null, repositoryObjectRestriction, null);
		
		this.acceptedContentTypes = acceptedContentTypes;
		
		if (this.acceptedContentTypes == null){
			this.acceptedContentTypes = new HashSet<String>();
		}
		
		this.expandedAcceptedContentTypes = new HashSet<String>();

	}

	public ValueType getValueType() {
		return ValueType.ObjectReference;
	}

	@Override
	public List<String> getAcceptedContentTypes() {
		return new ArrayList<String>(acceptedContentTypes);
	}

	@Override
	public ObjectReferencePropertyDefinition clone(
			ComplexCmsPropertyDefinition parentDefinition) {
		
		ObjectReferencePropertyDefinitionImpl cloneDefinition = new ObjectReferencePropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getRepositoryObjectRestriction(), acceptedContentTypes);
		
		cloneDefinition.addExpandedAcceptedContentTypes(expandedAcceptedContentTypes);
		
		return cloneDefinition;
	}

	/**
	 * @param list
	 */
	public void addExpandedAcceptedContentTypes(Set<String> contentTypes) {
		
		if (contentTypes != null && ! contentTypes.isEmpty()){
			expandedAcceptedContentTypes.addAll(contentTypes);
		}
	}

	
	public List<String> getExpandedAcceptedContentTypes() {
		return new ArrayList<String>(expandedAcceptedContentTypes);
	}

	@Override
	public boolean isValueValid(ContentObject value) {
		
		if (value == null){
			return true;
		}
		
		String contentObjectType = value.getContentObjectType();
		
		if (CollectionUtils.isNotEmpty(expandedAcceptedContentTypes)){
			
			if (contentObjectType == null){
				return false;
			}
			
			return expandedAcceptedContentTypes.contains(contentObjectType);
		}

		return true;
	}
	
	
}
