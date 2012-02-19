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
package org.betaconceptframework.astroboa.portal.managedbean;

import javax.faces.context.FacesContext;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.configuration.RepositoryType;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoCreate
@Name("portalManager")
@Scope(ScopeType.APPLICATION)
@Startup
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PortalManager {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@In(create=true)
	private AstroboaClient astroboaClient;
	private String currentlyConnectedPortalSytemName;
	private String portalHost;
	
	//Do not downgrade scope type to EVENT as 
	//there are problems with other seam objects with scope 
	//greater than EVENT. In these objects portal variable is
	//injected only once and therefore if portal is needed
	//more than once by these objects but EVENT scope scope 
	//has finished, null reference is returned, as this object
	//has been garbage collected
	//@Out(required=false, scope=ScopeType.PAGE)
	//private ContentObject portal;
	
	@Create
	public void readPortalConfiguration(){
		try {
			PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
			currentlyConnectedPortalSytemName = portalConfiguration.getString(PortalStringConstants.PORTAL_SYSTEM_NAME);
			portalHost = portalConfiguration.getString(PortalStringConstants.PORTAL_HOST_NAME);
		}
		catch (Exception e) {
			logger.error("A problem occured while reading portal configuration file.", e);
		}
	}
	
	
	@Factory(value="portal",scope=ScopeType.PAGE)
	public ContentObject findPortal() {
		if (StringUtils.isNotBlank(currentlyConnectedPortalSytemName)) {
			ContentObjectCriteria portalCriteria = CmsCriteriaFactory.newContentObjectCriteria(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE);

			portalCriteria.addSystemNameEqualsCriterion(currentlyConnectedPortalSytemName);
			//portalCriteria.addCriterion(CriterionFactory.equals("portalSystemName", currentlyConnectedPortalSytemName));

			portalCriteria.setCacheable(CacheRegion.TEN_MINUTES);

			CmsOutcome<ContentObject> portalCriteriaOutcome = 
				astroboaClient.getContentService().searchContentObjects(portalCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			if (portalCriteriaOutcome != null && portalCriteriaOutcome.getCount() > 0){
				if (portalCriteriaOutcome.getCount() > 1){
					logger.error("Found more than one portal content objects with system name {}", currentlyConnectedPortalSytemName);
				}
				else{
					return portalCriteriaOutcome.getResults().get(0);
				}
			}
			else{
				logger.error("Could not find portal content object with system name {}", currentlyConnectedPortalSytemName);
			}
		}
		
		return null;
	}

	
	@Factory("portalContext")
	public String getPortalContext() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	
	@Factory("portalContextWithHost")
	public String getPortalContextWithHost() {
		return "http://"+portalHost+getPortalContext();
	}
	
	
	public String getCurrentlyConnectedPortalSytemName() {
		return currentlyConnectedPortalSytemName;
	}
	
	public ContentObject getPortal(){
		
		if (Contexts.getPageContext().isSet("portal")){
			return (ContentObject) Contexts.getPageContext().get("portal");
		}
		
		return findPortal();
	}
	
	@Factory("resourceApiBasePath")
	public String getResourceApiBasePath() {
		if (astroboaClient != null){
			
			String repositoryId = astroboaClient.getConnectedRepositoryId();
			
			if (RepositoryRegistry.INSTANCE.isRepositoryRegistered(repositoryId)){
				
				RepositoryType repositoryConfiguration = RepositoryRegistry.INSTANCE.getRepositoryConfiguration(repositoryId);
				
				if (repositoryConfiguration!= null){
					return repositoryConfiguration.getRestfulApiBasePath();
				}
			}
		}
		
		//This is the default value
		return "/resource-api";
	}

}
