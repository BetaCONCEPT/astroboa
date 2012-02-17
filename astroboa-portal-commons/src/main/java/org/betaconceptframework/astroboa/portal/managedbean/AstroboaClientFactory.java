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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("astroboaClientFactory")
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaClientFactory {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Factory("astroboaClient")
	public AstroboaClient initializeRepository() {
		logger.debug("Astroboa Client Factory Entered");
		AstroboaClient astroboaClient = null;
		try {
			PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
			String currentlyConnectedRepositoryServer = portalConfiguration.getString(PortalStringConstants.ASTROBOA_SERVER);
			String currentlyConnectedRepository = portalConfiguration.getString(PortalStringConstants.REPOSITORY);
			String clientPermanentKey = portalConfiguration.getString(PortalStringConstants.ASTROBOA_CLIENT_PERMANENT_KEY);
			
			astroboaClient = new AstroboaClient(currentlyConnectedRepositoryServer);

			astroboaClient.loginAsAnonymous(currentlyConnectedRepository, clientPermanentKey);

			String propertiesFilePath = "portal.properties";
			
			if (portalConfiguration.getURL() != null){
				propertiesFilePath = portalConfiguration.getURL().toString();
			}
			
			if (logger.isDebugEnabled()){
				logger.debug("Created Astroboa repository client {} from properties file {}",astroboaClient.getInfo(), propertiesFilePath);
			}

			
		}
		catch (ConfigurationException e) {
			logger.error("A problem occured while reading repository client settings from portal configuration file.", e);
		}
		catch (Exception e) {
			logger.error("A problem occured while connecting repository client to Astroboa Repository", e);
		}
		
		return astroboaClient;
		
	}

	
	
}
