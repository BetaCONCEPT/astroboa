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


import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.component.html.HtmlSpacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectToUIComponentTree_Visitor extends AbstractCmsPropertyDefinitionVisitor{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ContentObjectUIWrapper contentObjectUIWrapper;
	private UIComponent uiComponentTreeParent;
	private UIComponent currentUIComponentTreeParent;
	private String locale;
	
	public ContentObjectToUIComponentTree_Visitor(ContentObjectUIWrapper contentObjectUIWrapper, UIComponent uiComponentTreePanel) {
		this.contentObjectUIWrapper = contentObjectUIWrapper;
		this.uiComponentTreeParent = uiComponentTreePanel;
		
		setVisitType(VisitType.Full);
	}
	
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		// Initialize current parent of UI component tree
		currentUIComponentTreeParent = uiComponentTreeParent;
		
		locale = JSFUtilities.getLocaleAsString();
	}

	public void visitComplexPropertyDefinition(ComplexCmsPropertyDefinition complexPropertyDefinition) {
		
	}

	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {
		addUIComponentForSimpleProperty(simplePropertyDefinition);
		
	}

	private void addUIComponentForSimpleProperty(SimpleCmsPropertyDefinition propertyDefinition)
	{

		String propertyPath = propertyDefinition.getPath();
		String propertyLocalisedLabel = propertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale);
		
		
		HtmlOutputText propertylabel = new HtmlOutputText();
		propertylabel.setValue(propertyLocalisedLabel);
		currentUIComponentTreeParent.getChildren().add(propertylabel);
		
		HtmlOutputText propertyValue = new HtmlOutputText();
		propertyValue.setValue(((SimpleCmsProperty)contentObjectUIWrapper.getContentObject().getCmsProperty(propertyPath)).getSimpleTypeValue());
		currentUIComponentTreeParent.getChildren().add(propertyValue);
		
		HtmlSpacer spacer = new HtmlSpacer();
		spacer.setHeight("2px");
		currentUIComponentTreeParent.getChildren().add(spacer);
	}



}
