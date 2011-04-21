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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.portal.utility.PortalCacheConstants;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("portalSectionResource")
@Scope(ScopeType.PAGE)
public class PortalSectionResource extends AbstractContentObjectResource<PortalSectionResourceContext> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public ResourceResponse<ContentObject, PortalSectionResourceContext> findResourcesBySystemName(String commaDelimitedSystemNames) {

		ResourceResponse<ContentObject, PortalSectionResourceContext> resourceResponse =
			new ResourceResponse<ContentObject, PortalSectionResourceContext>();

		PortalSectionResourceContext resourceContext = new PortalSectionResourceContext();
		resourceResponse.setResourceContext(resourceContext);

		if (StringUtils.isBlank(commaDelimitedSystemNames)) {
			logger.warn("A null or empty system name list has been provided. An empty response object has been returned");
			return resourceResponse;
		}

		List<String> systemNames = Arrays.asList(StringUtils.split(commaDelimitedSystemNames, ","));

		if (CollectionUtils.isEmpty(systemNames)) {
			logger.warn("A null or empty system name list has been provided. An empty response object has been returned");
			return resourceResponse;
		}

		ContentObjectCriteria portalSectionCriteria = CmsCriteriaFactory.newContentObjectCriteria(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);

		portalSectionCriteria.addCriterion(CriterionFactory.equals(CmsBuiltInItem.SystemName.getJcrName(), Condition.OR, systemNames));

		portalSectionCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION);

		try {
			CmsOutcome<ContentObject> cmsOutcome = 
				astroboaClient.getContentService().searchContentObjects(portalSectionCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			if (cmsOutcome.getCount()  > 0) {

				//We must keep the order provided in the comma delimited string
				List<ContentObject> finalContentObjectList = new ArrayList<ContentObject>();

				for (String systemName : systemNames){

					for (ContentObject co : cmsOutcome.getResults()){
						if (systemName.equals(co.getSystemName())){
							finalContentObjectList.add(co);
							break;
						}
					}
				}

				resourceResponse.setResourceRepresentation(finalContentObjectList);
				
				return resourceResponse;

			}
			else {
				logger.warn("Found no portal sections with name: " + systemNames + " An empty response object has been returned");
				return resourceResponse;
			}
		} catch (Exception e) {
			logger.error(
					"An error occured while searching for portal section by system name. An empty response object has been returned.",e);

			return resourceResponse;
		}

	}
	
	@Override
	public ResourceResponse<ContentObject, PortalSectionResourceContext> findResourceBySystemName(String systemName) {

		ResourceResponse<ContentObject, PortalSectionResourceContext> resourceResponse = 
			findResourceBySystemName(systemName, CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);
		
		if (!resourceResponse.getResourceRepresentation().isEmpty()) {
			createPathToPortalSection(resourceResponse.getFirstResource(), resourceResponse.getResourceContext().getPortalSectionPath());
		}
		
		return resourceResponse;
	}
	
	
	public List<ContentObject> getPathToPortalSection(ContentObject portalSection) {
		
		List<ContentObject> portalSectionPath = new ArrayList<ContentObject>();
		createPathToPortalSection(portalSection, portalSectionPath);
		return portalSectionPath;
	}
	
	public void createPathToPortalSection(ContentObject portalSection, List<ContentObject> portalSectionPath) {
		
		ContentObject parentSection = findParentSection(portalSection);
		if (parentSection != null) {
			createPathToPortalSection(parentSection, portalSectionPath);
		}
		
		portalSectionPath.add(portalSection);
	
	}

	
	public ContentObject findParentSection(ContentObject portalSection) {
		ContentObjectCriteria parentPortalSectionCriteria = CmsCriteriaFactory.newContentObjectCriteria(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);
		
		parentPortalSectionCriteria.addCriterion(CriterionFactory.equals("subPortalSection", portalSection.getId()));
		
		parentPortalSectionCriteria.setCacheable(PortalCacheConstants.CONTENT_OBJECT_DEFAULT_CACHE_REGION);
		
		try {
			CmsOutcome<ContentObject>  cmsOutcome = astroboaClient.getContentService().searchContentObjects(parentPortalSectionCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			if (cmsOutcome.getCount() > 0) {
				return cmsOutcome.getResults().get(0);
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			logger.error(
					"An error occured while searching for portal section parent. A null parent has been returned.",e);
			return null;
		}
	}

	public boolean isSubSection(ContentObject parentSection, ContentObject candidateSubSection) {
		
		if (parentSection == null || candidateSubSection == null){
			return false;
		}
		
		List<ContentObject> subPortalSections = ((ObjectReferenceProperty) parentSection.getCmsProperty("subPortalSection")).getSimpleTypeValues(); 

		if (CollectionUtils.isEmpty(subPortalSections)) {
			return false;
		}

		for (ContentObject subPortalSection : subPortalSections) {
			if (StringUtils.equals(subPortalSection.getId(), candidateSubSection.getId())) {
				return true;
			}
			else {
				return isSubSection(subPortalSection, candidateSubSection);
			}
		}

		return false;
	}
	
	public boolean isSubSection(String parentSectionName, ContentObject candidateSubSection) {
		List<ContentObject> parentSections = findResourceBySystemName(parentSectionName).getResourceRepresentation();
		if (!parentSections.isEmpty()) {
			return isSubSection(parentSections.get(0), candidateSubSection);
		}
		else {
			return false;
		}
	}
	
	@Override
	protected PortalSectionResourceContext newResourceContext() {
		return new PortalSectionResourceContext();
	}
	
	@Override
	protected void outjectCustomResourceCollectionRequestParameters() {
		
	}

	@Override
	protected void outjectCustomSingleResourceRequestParameters() {
	
	}

}
