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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.secure.DefinitionServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteDefinitionServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({DefinitionServiceSecure.class})
@Remote({RemoteDefinitionServiceSecure.class})
@Stateless(name="DefinitionServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class DefinitionServiceSecureImpl extends AbstractSecureAstroboaService implements DefinitionServiceSecure{

	private DefinitionService definitionService;
	
	@Override
	void initializeOtherRemoteServices() {
		definitionService = (DefinitionService) springManagedRepositoryServicesContext.getBean("definitionService");
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ComplexCmsPropertyDefinition getAspectDefinition(
			String complexCmsPropertyName,String authenticationToken) {
		
		return definitionService.getAspectDefinition(complexCmsPropertyName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<ComplexCmsPropertyDefinition> getAspectDefinitionsSortedByLocale(
			List<String> complexCmsPropertyNames, String locale,String authenticationToken) {
		
		return definitionService.getAspectDefinitionsSortedByLocale(complexCmsPropertyNames, locale);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")	
	public List<ComplexCmsPropertyDefinition> getAvailableAspectDefinitionsSortedByLocale(
			String locale,String authenticationToken) {
		
		return definitionService.getAvailableAspectDefinitionsSortedByLocale(locale);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public CmsPropertyDefinition getCmsPropertyDefinition(
			String relativePropertyPath, String contentObjectTypeDefinitionName,String authenticationToken) {
		
		return definitionService.getCmsPropertyDefinition(relativePropertyPath, contentObjectTypeDefinitionName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ContentObjectTypeDefinition getContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName,String authenticationToken) {
		
		return definitionService.getContentObjectTypeDefinition(contentObjectTypeDefinitionName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<String> getContentObjectTypes(String authenticationToken) {
		
		return definitionService.getContentObjectTypes();
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Map<String, List<String>> getTopicPropertyPathsPerTaxonomies(String authenticationToken) {
		
		return definitionService.getTopicPropertyPathsPerTaxonomies();
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public byte[] getXMLSchemaForDefinition(String definitionFullPath,String authenticationToken) {
		
		return definitionService.getXMLSchemaForDefinition(definitionFullPath);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public boolean hasContentObjectTypeDefinition(
			String contentObjectTypeDefinitionName,String authenticationToken) {
		
		return definitionService.hasContentObjectTypeDefinition(contentObjectTypeDefinitionName);
	}
	
	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public CmsPropertyDefinition getCmsPropertyDefinition(
			String fullPropertyDefinitionPath,String authenticationToken) {
		
		return definitionService.getCmsPropertyDefinition(fullPropertyDefinitionPath);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<String> getMultivalueProperties(String authenticationToken) {
		return definitionService.getMultivalueProperties();
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Map<String, List<String>> getContentTypeHierarchy(String authenticationToken) {
		return definitionService.getContentTypeHierarchy();
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getCmsDefinition(String fullPropertyDefinitionPath,
			ResourceRepresentationType<T> output, boolean prettyPrint, String authenticationToken) {
		return definitionService.getCmsDefinition(fullPropertyDefinitionPath, output, prettyPrint);
	}

	@RolesAllowed("ROLE_ADMIN")
	public boolean validateDefinintion(String definition, String definitionFileName, String authenticationToken) {
		return definitionService.validateDefinintion(definition, definitionFileName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Map<String, Integer> getDefinitionHierarchyDepthPerContentType(
			String authenticationToken) {
		return definitionService.getDefinitionHierarchyDepthPerContentType();
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Integer getDefinitionHierarchyDepthForContentType(
			String contentType, String authenticationToken) {
		return definitionService.getDefinitionHierarchyDepthForContentType(contentType);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public ValueType getTypeForProperty(String contentType,
			String propertyPath, String authenticationToken) {
		return definitionService.getTypeForProperty(contentType, propertyPath);
	}


}
