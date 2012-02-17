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
package org.betaconceptframework.astroboa.console.jsf.visitor;


import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;

@Deprecated
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SelectItemContentObjectPropertyDefinitionVisitor extends AbstractCmsPropertyDefinitionVisitor{

	private List<SelectItem> contentObjectTypePropertiesAsSelectItems = new ArrayList<SelectItem>();
	private String locale;
	
	public SelectItemContentObjectPropertyDefinitionVisitor(String selectedContentObjectType){

		setVisitType(VisitType.Full);
	}
	
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		//Initialise properties
		contentObjectTypePropertiesAsSelectItems.clear();
		locale = JSFUtilities.getLocaleAsString();
	}

	public void visitComplexPropertyDefinition(ComplexCmsPropertyDefinition complexPropertyDefinition) {
		// we do not produce search criteria for complex properties. Search is done on simple properties inside complex properties
		//addNewSelectItem(complexPropertyDefinition);
	}

	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {
		addNewSelectItem(simplePropertyDefinition);
		
	}

	private void addNewSelectItem(SimpleCmsPropertyDefinition propertyDefinition)
	{
		String propertyPath = propertyDefinition.getPath();
		String propertyLocalisedLabel = propertyDefinition.getLocalizedLabelOfFullPathforLocale(locale);
		SelectItem selectItem = new SelectItem(propertyPath, propertyLocalisedLabel);
		contentObjectTypePropertiesAsSelectItems.add(selectItem);
	}

	public List<SelectItem> getSelectItems() {
		return contentObjectTypePropertiesAsSelectItems;
	}
}
