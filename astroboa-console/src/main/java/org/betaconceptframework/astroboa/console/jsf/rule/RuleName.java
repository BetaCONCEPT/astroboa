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
package org.betaconceptframework.astroboa.console.jsf.rule;

import org.betaconceptframework.astroboa.api.model.CmsProperty;

/**
 * This class represents the name of several built in rules
 * which are used by Astroboa.
 * 
 * These names are system names of a contentObject of type ruleObject
 * which contains the rules.
 * 
 * These rules are also located at rules directory inside classpath.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum RuleName {

	/**
	 * This rule filters a list of 
	 * CmsPropertyDefinition (List&lt;CmsPropertyDefinition&gt;)
	 * 
	 * Rule's input are two lists : one containing the initial definitions
	 * and another one named 'filteredDefinitions' which will contain 
	 * those definitions that user may see
	 */
	rulesResponsibleToFilterPropertiesAppearingInEditForm,
	
	/**
	 * This rule creates a UIComponent for a specified CmsProperty
	 * 
	 * Rule's input is an empty UIComponent which is populated with the proper
	 * attributes and the {@link CmsProperty} whose value will be edited through this
	 * ui component
	 */
	rulesResponsibleToCreateUIComponentInEditForm;
	
}
