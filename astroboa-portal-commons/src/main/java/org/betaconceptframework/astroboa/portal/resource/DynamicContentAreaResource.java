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
package org.betaconceptframework.astroboa.portal.resource;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.PAGE)
@Name("dynamicContentAreaResource")
public class DynamicContentAreaResource extends AbstractContentObjectResource<DynamicContentAreaResourceContext> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public ResourceResponse<ContentObject, DynamicContentAreaResourceContext> findResourceBySystemName(String systemName) {

		ResourceResponse<ContentObject, DynamicContentAreaResourceContext> resourceResponse = 
			findResourceBySystemName(systemName, CmsConstants.DYNAMIC_CONTENT_AREA_CONTENT_OBJECT_TYPE);
		
		if (!resourceResponse.getResourceRepresentation().isEmpty()) {
			findDynamicResources(resourceResponse.getResourceRepresentation().get(0), resourceResponse.getResourceContext());
		}
		else{
			logger.warn("Found no dynamic content areas with name: " + systemName + " An empty response object has been returned");
		}
		
		return resourceResponse;
	}
	
	
	private void findDynamicResources(ContentObject dynamicContentArea, DynamicContentAreaResourceContext dynamicContentAreaResourceContext) {
		String elMethodExpressionThatReturnsDynamicContent = ((StringProperty)dynamicContentArea.getCmsProperty("elExpression")).getSimpleTypeValue();
		
		ResourceResponse<ContentObject, ResourceContext> elMethodExpressionReturnValue = 
			(ResourceResponse<ContentObject, ResourceContext>)Expressions.instance().createMethodExpression(elMethodExpressionThatReturnsDynamicContent).invoke();
		
		dynamicContentAreaResourceContext.setResourcesInContentArea(elMethodExpressionReturnValue.getResourceRepresentation());
	}

	
	protected DynamicContentAreaResourceContext newResourceContext() {
		return new DynamicContentAreaResourceContext();
	}
	
	@Override
	protected void outjectCustomResourceCollectionRequestParameters() {
		
	}

	@Override
	protected void outjectCustomSingleResourceRequestParameters() {
		
	}
	

}
