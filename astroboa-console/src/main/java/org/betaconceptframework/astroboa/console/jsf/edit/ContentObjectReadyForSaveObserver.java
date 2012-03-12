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
package org.betaconceptframework.astroboa.console.jsf.edit;

import groovy.lang.GroovyClassLoader;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name(value="contentObjectReadyForSaveObserver")
public class ContentObjectReadyForSaveObserver {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AstroboaClient astroboaClient;
	
	@Observer(value=SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE)
	public void processContentObjectBeforeSave(ContentObject contentObjectToBeSaved) {
		try {
			ContentObject scriptObject = null;
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("scriptObject");
			contentObjectCriteria.addSystemNameEqualsCriterion(SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE);
			// we allow only the SYSTEM user to create code for this event
			RepositoryUser systemUser = astroboaClient.getRepositoryUserService().getSystemRepositoryUser();
			contentObjectCriteria.addOwnerIdEqualsCriterion(systemUser.getId());

			CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			if (cmsOutcome.getCount() > 0) {
				scriptObject = cmsOutcome.getResults().get(0);
			}
			else {
				logger.info("Nothing to run in content object pre-save phase. " +
						"No Script Object exists with systen name: " + SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE);
				return;
			}
			
			String scriptAsString = ((StringProperty)scriptObject.getCmsProperty("code")).getSimpleTypeValue();
			if (StringUtils.isBlank(scriptAsString)){
				logger.warn("Script Object: " + SeamEventNames.CONTENT_OBJECT_READY_FOR_SAVE + " contains an empty script");
				return;
			}
			
			ClassLoader parentClassLoader = getClass().getClassLoader();
			GroovyClassLoader groovyClassLoader = new GroovyClassLoader(parentClassLoader);
			//GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

			if (groovyClassLoader != null) {
				Class scriptClass = 
					groovyClassLoader.parseClass(scriptAsString);
				
				try {
					Object groovyObject = scriptClass.newInstance();
					// The script class should implement the PreSaveGroovyAction Interface
					PreSaveGroovyAction preSaveGroovyAction = (PreSaveGroovyAction) groovyObject;
					preSaveGroovyAction.run(astroboaClient, contentObjectToBeSaved, logger);
				}
				catch (Exception e) {
					logger.warn("Script Class could not be created", e);
				}
			}
			else {
				logger.warn("Script Engine could not be created");
			}

		}
		catch (Exception e) {
			logger.warn("An error occured during the retrieval of script object", e);
		}
		
	}
}

