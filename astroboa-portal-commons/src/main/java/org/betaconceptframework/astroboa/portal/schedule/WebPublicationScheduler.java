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
package org.betaconceptframework.astroboa.portal.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("webPublicationScheduler")
public class WebPublicationScheduler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AstroboaClient astroboaClient;
	
	@Asynchronous
	public QuartzTriggerHandle publishScheduledForPublicationContentObjects(@Expiration Date when, @IntervalDuration Long interval) {
		
		
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
		
		//	we create the search criteria
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		// we are searching only for content objects which have their status set to "scheduledForPublication"
		contentObjectCriteria.addCriterion(CriterionFactory.equals("profile.contentObjectStatus", CmsConstants.ContentObjectStatus.scheduledForPublication.toString()));

		// we are looking only for content objects that their publication start date is earlier than now
		contentObjectCriteria.addCriterion(CriterionFactory.lessThan("webPublication.webPublicationStartDate", Calendar.getInstance()));

		//CHECK IF WE NEED TO SPECIFY RENDER PROPERTIES WITH A LOCALE
		//contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, locale);

		ContentObject contentObjectToBePublished = null;

		try {
			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			if (cmsOutcome.getCount() > 0) {
				List<ContentObject> cmsSearchResultList = cmsOutcome.getResults();
				
				logger.info(cmsOutcome.getCount() + " content Objects will be published in "+ astroboaClient.getInfo());
				
				for (ContentObject cmsSearchResult : cmsSearchResultList) {
					contentObjectToBePublished = cmsSearchResult;

					try{
						((StringProperty)contentObjectToBePublished.getCmsProperty("profile.contentObjectStatus")).setSimpleTypeValue(CmsConstants.ContentObjectStatus.published.toString());

						// 	before saving sent an event for those that need to do some action before publication, or need to alter the content object to be published
						Events.instance().raiseEvent(PortalEventNames.EVENT_CONTENT_OBJECT_PRE_PUBLISH, contentObjectToBePublished);

						astroboaClient.getContentService().save(contentObjectToBePublished, false, true, null);
					
						// 	after publication sent an event for those interested
						Events.instance().raiseEvent(PortalEventNames.EVENT_CONTENT_OBJECT_POST_PUBLISH, contentObjectToBePublished);
					}
					catch(Exception e)
					{
						//Log exception but proceed with other content objects
						logger.error("An Error Occured during publication of content object"+contentObjectToBePublished.getId() + " "+
								contentObjectToBePublished.getSystemName(), e);
					}
				}
			}

		}
		catch (Exception e) {
			logger.error("An Error Occured during publication of content objects", e);
		}
		
		logger.info("Publication of Content Objects in "+astroboaClient.getInfo()+" finished at:" + new Date().toString());
		
		return null;

	}

	
}
