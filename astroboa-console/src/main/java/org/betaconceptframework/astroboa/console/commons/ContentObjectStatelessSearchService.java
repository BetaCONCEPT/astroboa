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
package org.betaconceptframework.astroboa.console.commons;

import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.api.service.ContentService;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentObjectStatelessSearchService {
	
	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	
	
	/**
	 * This method is used by classes which want to search with non paged results and
	 * run outside the web context (i.e. the scheduler) so they do not have
	 * access to the StatefulSearchservice which is a session bean.
	 * @param cacheable 
	 */
	public List<ContentObjectUIWrapper> searchForContent(
			ContentObjectCriteria contentObjectCriteria,
			boolean useDefaultRenderProperties, String locale, boolean cacheable)
			throws CmsException {

		if (useDefaultRenderProperties)
			setDefaultRenderPropertiesToContentObjectCriteria(contentObjectCriteria, locale);
		
		if(cacheable) {
			contentObjectCriteria.setCacheable(CacheRegion.TEN_MINUTES);
		}
		else{
			contentObjectCriteria.doNotCacheResults();
		}
		
		CmsOutcome<CmsRankedOutcome<ContentObject>> cmsOutcome = contentService
				.searchContentObjects(contentObjectCriteria);
		
		List<ContentObjectUIWrapper> wrappedContentObjects;
		
		if (cmsOutcome.getCount() > 0) {
			List<CmsRankedOutcome<ContentObject>> cmsOutcomeRowList = cmsOutcome.getResults();
			wrappedContentObjects = new ArrayList<ContentObjectUIWrapper>();
			
			for (CmsRankedOutcome<ContentObject> cmsOutcomeRow : cmsOutcomeRowList) {
				wrappedContentObjects.add(contentObjectUIWrapperFactory.getInstance(cmsOutcomeRow.getCmsRepositoryEntity()));
			}
			return wrappedContentObjects;
		} else
			return null;
	}
	
	
	private void setDefaultRenderPropertiesToContentObjectCriteria(ContentObjectCriteria contentObjectCriteria, String locale) {
		/*
		 * The default render properties when we retrieve content objects are:
		 * the localized labels are retrieved according to the provided locale
		 */
		contentObjectCriteria.getRenderProperties().resetRenderInstructions();
		contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, locale);
	}

	
	
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}

}
