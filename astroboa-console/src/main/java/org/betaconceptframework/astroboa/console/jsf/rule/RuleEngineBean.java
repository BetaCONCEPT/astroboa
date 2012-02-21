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

import java.util.List;

import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@AutoCreate
@Name("ruleEngine")
@Scope(ScopeType.SESSION)
public class RuleEngineBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AstroboaClient astroboaClient;
	
	public void setAstroboaClient(
			AstroboaClient astroboaClient) {
		this.astroboaClient = astroboaClient;
	}

	/**
	 * 
	 * Filters definitions for edit form. A content object with system name
	 * 'rulesResponsibleToFilterPropertiesFromAppearingInEditForm' whose type is either 'ruleSetObject' or any other
	 * type with aspect 'arrayOfRuleTypeType' or 'ruleType'.
	 * 
	 * If no such content object exists, list is not filtered at all
	 * 
	 * @param propertyDefinitionList
	 * @return
	 */
	public List<CmsPropertyDefinition> filterDefinitionsForEdit(
			List<CmsPropertyDefinition> propertyDefinitionList) {
		
		return propertyDefinitionList;
		
		/*if (CollectionUtils.isEmpty(propertyDefinitionList)){
			return propertyDefinitionList;
		}
		
		try{
			StatelessKnowledgeSession ruleSession = astroboaClient.getRuleEngine().newStatelessSessionForRuleSet(Arrays.asList(RuleName.rulesResponsibleToFilterPropertiesAppearingInEditForm.toString()));

			if (ruleSession == null){
				return propertyDefinitionList;
			}
			
			List cmds = new ArrayList();
			cmds.add( CommandFactory.newInsertElements(propertyDefinitionList) );
			cmds.add( CommandFactory.newInsert(new ArrayList<CmsPropertyDefinition>(), "filteredDefinitions") );

			ExecutionResults results =
				ruleSession.execute( CommandFactory.newBatchExecution( cmds ) );
			
			List<CmsPropertyDefinition> filteredDefinitions = (List<CmsPropertyDefinition>) results.getValue("filteredDefinitions");
			
			if (filteredDefinitions == null || filteredDefinitions.isEmpty()){
				if (Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN))){
					return propertyDefinitionList;
				}
			}
			
			return filteredDefinitions;

		}
		catch(Exception e){
			logger.warn("",e);
		}
		
		return propertyDefinitionList;*/
	}
	
	/**
	 * 	Creates appropriate ui component to display value for provided cmsProperty in edit form. 
	 * A content object with system name
	 * 'rulesResponsibleToCreateUIComponentInEditForm' whose type is either 'ruleSetObject' or any other
	 * type with aspect 'arrayOfRuleTypeType' or 'ruleType'.
	 * 
	 * If no such content object exists, an empty UIComponent of type TEXT is returned
	 * @param cmsPropertyDefinition
	 * @return
	
	public UIComponent createUiComponent(CmsPropertyDefinition cmsPropertyDefinition){
	
			UIComponent uiComponent = new UIComponent();
			uiComponent.setType(UIComponentType.TEXT);
			
			try{
				
				StatelessKnowledgeSession ruleSession = astroboaClient.getRuleEngine().newStatelessSessionForRuleSet(Arrays.asList(RuleName.rulesResponsibleToCreateUIComponentInEditForm.toString()));

				if (ruleSession == null){
					return uiComponent;
				}
				
				List cmds = new ArrayList();
				cmds.add( CommandFactory.newInsert(cmsPropertyDefinition) );
				cmds.add( CommandFactory.newInsert(uiComponent) );

				ExecutionResults results =
					ruleSession.execute( CommandFactory.newBatchExecution( cmds ) );

				return uiComponent.isCompleted() ? uiComponent : null;

			}
			catch(Throwable e){
				logger.warn(cmsPropertyDefinition.getPath(),e);
				return uiComponent;
			}
	}
	 */
}
