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
package org.betaconceptframework.astroboa.console.jsf.dashboard;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("recentlyAddedOrModifiedContentObjectsDashboardItem")
@Scope(ScopeType.CONVERSATION)
public class RecentlyAddedOrModifiedContentObjectsDashboardItem extends ContentObjectListBean{
	
	private static final long serialVersionUID = 1L;
	
	private int numberOfDaysToSearchBackForRecentlyAddedOrModifiedPublishedContentObjects = 1000;
	private int cacheSizeForRecentlyAddedOrModifiedPublishedContentObjects = 100;


	@Override
	protected List<ContentObject> orderResults(List<ContentObject> results) {
		return results;
	}
	
	@Override
	public ContentObjectDataModel getReturnedContentObjects() {
		if (super.getReturnedContentObjects() == null){
			// reset search criteria to begin a new search
			contentObjectCriteria = null;
			contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.getRenderProperties().renderValuesForLocale(JSFUtilities.getLocaleAsString());


			try {
				
				Calendar searchBackDate = GregorianCalendar.getInstance();
				searchBackDate.add(Calendar.DAY_OF_MONTH, -numberOfDaysToSearchBackForRecentlyAddedOrModifiedPublishedContentObjects);

				Criterion modificationDateCriterion = CriterionFactory.greaterThanOrEquals("profile.modified", searchBackDate);
				contentObjectCriteria.addCriterion(modificationDateCriterion);

				contentObjectCriteria.addOrderProperty("profile.modified", Order.descending);

				// the number of returned content objects should be <cacheSizeForRecentlyAddedOrModifiedContentObjects>
				//contentObjectCriteria.getResultRowRange().setRange(0, cacheSizeForRecentlyAddedOrModifiedPublishedContentObjects);
				contentObjectCriteria.setOffsetAndLimit(0, cacheSizeForRecentlyAddedOrModifiedPublishedContentObjects);

				logger.warn(contentObjectCriteria.getXPathQuery());

				//Call parent to execute query and create paged list data model
				searchForContentObjectWithPagedResults();



			} catch (Exception e) {
				logger.error("Error while loading content objects ",e);
				JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", new String[] {e.toString()}, FacesMessage.SEVERITY_ERROR);
			}
		}
		return super.getReturnedContentObjects();
	}


	public int getNumberOfDaysToSearchBackForRecentlyAddedOrModifiedPublishedContentObjects() {
		return numberOfDaysToSearchBackForRecentlyAddedOrModifiedPublishedContentObjects;
	}


	public int getCacheSizeForRecentlyAddedOrModifiedPublishedContentObjects() {
		return cacheSizeForRecentlyAddedOrModifiedPublishedContentObjects;
	}

}
