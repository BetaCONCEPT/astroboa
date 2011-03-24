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
package org.betaconceptframework.astroboa.portal.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.portal.managedbean.ViewCountAggregator;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("viewCountPersistanceScheduler")
public class ViewCountPersistanceScheduler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AstroboaClient astroboaClient;
	
	@In
	protected ViewCountAggregator viewCountAggregator;
	
	@Asynchronous
	public QuartzTriggerHandle persistViewCounts(@Expiration Date when, @IntervalDuration Long interval) {
		
		if (astroboaClient == null) {
			try {
				PropertiesConfiguration portalConfiguration = new PropertiesConfiguration("portal.properties");
				String currentlyConnectedRepositoryServer = portalConfiguration.getString(PortalStringConstants.ASTROBOA_SERVER);
				String currentlyConnectedRepository = portalConfiguration.getString(PortalStringConstants.REPOSITORY);
				String clientPermanentKey = portalConfiguration.getString(PortalStringConstants.ASTROBOA_CLIENT_PERMANENT_KEY);
				String systemSecretKey = portalConfiguration.getString(PortalStringConstants.SYSTEM_SECRET_KEY);

				astroboaClient = new AstroboaClient(currentlyConnectedRepositoryServer);

				//Login with subject does not work with remote api
				//astroboaClient.login(currentlyConnectedRepository, portalUtils.createSubjectForSystemUserAndItsRoles(currentlyConnectedRepository), clientPermanentKey);
				//This will work if Administrator has been provided with a secret key in astroboa-conf.xml
				astroboaClient.loginAsAdministrator(currentlyConnectedRepository, systemSecretKey, clientPermanentKey);
				
			}
			catch (ConfigurationException e) {
				logger.error("A problem occured while reading repository client settings from portal configuration file.", e);
			}
			catch (Exception e) {
				logger.error("A problem occured while connecting repository client to Astroboa Repository", e);
			}
		}
		
		

		try {
			
			Map<String,Integer> contenObjectViewCount = new HashMap<String,Integer>();
			contenObjectViewCount.putAll(viewCountAggregator.getContentObjectViewCount());
			viewCountAggregator.getContentObjectViewCount().clear();
			
			for (Entry<String,Integer> entry: contenObjectViewCount.entrySet()) {
				ContentObject contentObject = astroboaClient.getContentService().getContentObject(entry.getKey(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, 
						CacheRegion.NONE, null, false);
				
				if (contentObject != null && contentObject.getComplexCmsRootProperty() !=null && 
						contentObject.getComplexCmsRootProperty().isChildPropertyDefined("statistic.viewCounter")) {
					
					LongProperty viewCounterProperty = (LongProperty)contentObject.getCmsProperty("statistic.viewCounter");
					
					if (viewCounterProperty.hasNoValues())
					{
						viewCounterProperty.setSimpleTypeValue(entry.getValue().longValue());
					}
					else
					{
						Long viewCounterCurrentValue = viewCounterProperty.getSimpleTypeValue();
						
						if (viewCounterCurrentValue == null)
						{
							viewCounterProperty.setSimpleTypeValue(entry.getValue().longValue());
						}
						else
						{
							viewCounterProperty.setSimpleTypeValue(viewCounterCurrentValue + entry.getValue().longValue());
						}
					}
					
					astroboaClient.getContentService().save(contentObject, false, false, null);
				}
			}
				
		}
		catch (Exception e) {
			logger.error("An Error Occured during the persistense of view counts", e);
		}
		
		return null;

	}

	
}
