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
package org.betaconceptframework.astroboa.console.jsf.search;

import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.render.RenderInstruction;
import org.betaconceptframework.astroboa.console.jsf.dashboard.ContentObjectListBean;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("contentObjectListOfTypeQuery")
@Scope(ScopeType.CONVERSATION)
public class ContentObjectListOfTypeQuery extends ContentObjectListBean{

	@Override
	protected List<ContentObject> orderResults(List<ContentObject> results) {
		return results;
	}
	
	public void findAllContentObjectsOfTypeQueryOrderedByModifiedDate_UIAction() {
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("queryObject");
		contentObjectCriteria.getRenderProperties().addRenderInstruction(RenderInstruction.RENDER_LOCALIZED_LABEL_FOR_LOCALE, JSFUtilities.getLocaleAsString());
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		try {
			
			contentObjectCriteria.addOrderProperty(
					"profile.modified", Order.descending);

			//Call parent to execute query and create paged list data model
			searchForContentObjectWithPagedResults();
			
		} catch (Exception e) {
			logger.error("Error while loading content objects ",e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
	}

	@Override
	public ContentObjectDataModel getReturnedContentObjects() {
		if (super.getReturnedContentObjects() == null){
			findAllContentObjectsOfTypeQueryOrderedByModifiedDate_UIAction();
		}
		
		return super.getReturnedContentObjects();
	}

	
}
