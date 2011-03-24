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

package org.betaconceptframework.astroboa.model.impl.definition;


import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor.VisitType;

/**
 * Abstract Implementation class for ComplexPropertyDefinition.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
abstract class AbstractComplexCmsPropertyDefinitionImpl extends CmsPropertyDefinitionImpl implements ComplexCmsPropertyDefinition, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2161756866624747367L;

	private final ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper;
	
	//Flag indicating whether parent's child definitions have been loaded
	//in case this definition is the same with its parent
	private boolean parentChildDefinitionsHaveBeenLoaded;

	//This is the uri of file containing the schema for this definition
	private URI definitionFileURI;

	//This is the name of the type of Complex  property
	protected String typeName;
	
	//Flag indicating that this definition is the same with one of its grand parent
	//and when asked its grand parent child property definitions will be cloned 
	//here
	protected QName qNameOfParentDefinitionWithTheSameType;
	
	public AbstractComplexCmsPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, String restrictReadToRoles,
			String restrictWriteToRoles, Integer order, CmsDefinition parentDefinition, 
			ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper,
			URI definitionFileURI,
			QName qNameOfParentDefinitionWithTheSameType, String typeName
	) {
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory, order,
				restrictReadToRoles, restrictWriteToRoles, parentDefinition);

		this.complexPropertyDefinitionHelper = complexPropertyDefinitionHelper;
		
		this.qNameOfParentDefinitionWithTheSameType = qNameOfParentDefinitionWithTheSameType;
		
		if (qNameOfParentDefinitionWithTheSameType == null){
			parentChildDefinitionsHaveBeenLoaded = true;
		}

		this.definitionFileURI = definitionFileURI;
		
		this.typeName = typeName;
	}


	protected void cloneParentsChildDefinitions(){
		if (definitionIsRecursiveAndItsChildDefinitionHaveNotBeenLoaded()){

			ComplexCmsPropertyDefinition ancestorDefinitionWithTheSameType = locateParentDefinintionWithSameType();
			
			if (ancestorDefinitionWithTheSameType != null){

				Collection<CmsPropertyDefinition> childPropertiesOfParentDefinition = ancestorDefinitionWithTheSameType.getChildCmsPropertyDefinitions().values();
				Map<String, CmsPropertyDefinition> childPropertyDefinitions = new LinkedHashMap<String, CmsPropertyDefinition>();
				for (CmsPropertyDefinition childPropertyDefinitionOfParentDefinition : childPropertiesOfParentDefinition){
					childPropertyDefinitions.put(childPropertyDefinitionOfParentDefinition.getName(), 
							((CmsPropertyDefinitionImpl)childPropertyDefinitionOfParentDefinition).clone((this)));
				}
				
				complexPropertyDefinitionHelper.setChildPropertyDefinitions(childPropertyDefinitions);
				
				parentChildDefinitionsHaveBeenLoaded = true;
				
				resetDepth();
			}
		}
	}


	public ComplexCmsPropertyDefinition locateParentDefinintionWithSameType() {
		
		
		CmsDefinition ancestorDefinition = getParentDefinition();
		
		while (ancestorDefinition != null && ancestorDefinition instanceof ComplexCmsPropertyDefinition){
			if ( StringUtils.equals(((ComplexCmsPropertyDefinitionImpl)ancestorDefinition).getTypeName(), typeName)){
				return (ComplexCmsPropertyDefinition) ancestorDefinition;
			}
			
			ancestorDefinition = ((ComplexCmsPropertyDefinition)ancestorDefinition).getParentDefinition();
		}
		
		return null;
	}


	protected boolean definitionIsRecursiveAndItsChildDefinitionHaveNotBeenLoaded() {
		return qNameOfParentDefinitionWithTheSameType != null && ! parentChildDefinitionsHaveBeenLoaded;
	}
	
	public CmsPropertyDefinition getChildCmsPropertyDefinition(
			String childCmsPropertyPath) {
		
		return complexPropertyDefinitionHelper.getChildCmsPropertyDefinition(childCmsPropertyPath);

	}

	public Map<String, CmsPropertyDefinition> getChildCmsPropertyDefinitions() {
		
		return complexPropertyDefinitionHelper.getChildPropertyDefinitions();
	}

	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(
			String locale) {
		
		return complexPropertyDefinitionHelper.getSortedChildCmsPropertyDefinitionsByAscendingOrderAndLocale(locale);
	}

	public Map<String, CmsPropertyDefinition> getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(
			String locale) {
		
		return complexPropertyDefinitionHelper.getSortedChildCmsPropertyDefinitionsByAscendingOrderAndValueTypeAndLocale(locale);
	}


	public boolean hasChildCmsPropertyDefinition(String childCmsPropertyPath) {
		
		return complexPropertyDefinitionHelper.hasChildCmsPropertyDefinition(childCmsPropertyPath);

	}

	public boolean hasChildCmsPropertyDefinitions() {
		return complexPropertyDefinitionHelper.hasPropertyDefinitions();
	}


	public void accept(DefinitionVisitor visitor) {

		if (visitor == null)
			return;

		//Visit Complex Property
		visitor.visitComplexPropertyDefinition(this);

		//Now continue to its children
		switch (visitor.getVisitType()) {
		case Children:
			//Do not visit grand children
			visitor.setVisitType(VisitType.Self);

			//Visit children
			visitSubPropertyDefinitions(visitor);

			//Restore previous state of visitor
			visitor.setVisitType(VisitType.Children);

			break;

		case Full:
			visitSubPropertyDefinitions(visitor);
		default:
			//Do nothing;
			break;
	
		}
	}

	private void visitSubPropertyDefinitions(DefinitionVisitor visitor) {
		
		visitor.startChildDefinitionsVisit(this);
		
		//Now send visitor to visit sub properties
		if (hasChildCmsPropertyDefinitions()){
			//Visit children definitions
			Map<String, CmsPropertyDefinition> childCmsPropertyDefinitions = getChildCmsPropertyDefinitions();

			for (CmsPropertyDefinition propertyDefinition: childCmsPropertyDefinitions.values()){
				//Visit child
				propertyDefinition.accept(visitor);
			}
		}
		
		visitor.finishedChildDefinitionsVisit(this);
	}
	
	public URI getDefinitionFileURI() {
		return definitionFileURI;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public boolean isRecursive(){
		return qNameOfParentDefinitionWithTheSameType != null;
	}

	public void checkIfRecursiveAndCloneParentChildDefinitions() {
		if (isRecursive()){
			cloneParentsChildDefinitions();
		}
		
	}

	public int getDepth(){
		if (complexPropertyDefinitionHelper == null){
			return 0;
		}

		return complexPropertyDefinitionHelper.getDepth();
	}

	public void resetDepth() {
		if (complexPropertyDefinitionHelper!=null){
			complexPropertyDefinitionHelper.resetDepth();
		}
		
		if (getParentDefinition() instanceof ContentObjectTypeDefinitionImpl){
			((ContentObjectTypeDefinitionImpl)getParentDefinition()).resetDepth();
		}
		else if (getParentDefinition() instanceof ComplexCmsPropertyDefinitionImpl){
			((ComplexCmsPropertyDefinitionImpl)getParentDefinition()).resetDepth();
		}
	}

}
