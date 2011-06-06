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
package org.betaconceptframework.astroboa.console.jsf.dashboard;


import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
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
@Name("contentObjectsSubmittedByExternalUserForWebPublishingDashboardItem")
@Scope(ScopeType.CONVERSATION)
public class ContentObjectsSubmittedByExternalUserForWebPublishingDashboardItem extends ContentObjectListBean{
	
	private static final long serialVersionUID = 1L;

	@Override
	public ContentObjectDataModel getReturnedContentObjects() {
		if (super.getReturnedContentObjects() == null){
			// reset search criteria to begin a new search
			contentObjectCriteria = null;
			contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
			//uiComponentBinding.resetContentObjectTableScrollerComponent();
			//contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();


			try {
				// we are searching for content objects which have their status set to contentObjectStatus
				Criterion statusCriterion = CriterionFactory.equals("profile.contentObjectStatus", "submittedByExternalUser");
				contentObjectCriteria.addCriterion(statusCriterion);

				/* we set the result set size so that the fist 100 objects are returned.
				 * We do this search to get the number of matched content objects and fill the first page of results. 
				 */
				//contentObjectCriteria.getResultRowRange().setRange(0,100);
				//It should be 99 as it is zero based
				contentObjectCriteria.setOffsetAndLimit(0,99);


				//			 set required ordering
				//if (searchResultsFilterAndOrdering.getSelectedResultsOrder() != null) {
				//	contentObjectCriteria.addOrderProperty(
				//			"profile.modified",
				//			Order.valueOf(searchResultsFilterAndOrdering.getSelectedResultsOrder()));
				//}
				//else {
				contentObjectCriteria.addOrderProperty("profile.modified",Order.descending);
				//}

				//Call parent to execute query and create paged list data model
				searchForContentObjectWithPagedResults();

			} catch (Exception e) {
				logger.error("Error while loading content objects ",e);
				JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", new String[] {e.toString()}, FacesMessage.SEVERITY_ERROR);
			}
		}

		return super.getReturnedContentObjects();

	}

	@Override
	protected List<CmsRankedOutcome<ContentObject>> orderResults(
			List<CmsRankedOutcome<ContentObject>> results) {
		return results;
	}

}
