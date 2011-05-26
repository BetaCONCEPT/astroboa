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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.secure.TaxonomyServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteTaxonomyServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({TaxonomyServiceSecure.class})
@Remote({RemoteTaxonomyServiceSecure.class})
@Stateless(name="TaxonomyServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class TaxonomyServiceSecureImpl extends AbstractSecureAstroboaService implements TaxonomyServiceSecure{

	private TaxonomyService taxonomyService;

	@Override
	void initializeOtherRemoteServices() {
		taxonomyService = (TaxonomyService) springManagedRepositoryServicesContext.getBean("taxonomyService");
	}

	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public boolean deleteTaxonomyTree(String taxonomyIdOrName, String authenticationToken) {
		
		return taxonomyService.deleteTaxonomyTree(taxonomyIdOrName);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Taxonomy getBuiltInSubjectTaxonomy(String locale, String authenticationToken) {
		
		return taxonomyService.getBuiltInSubjectTaxonomy(locale);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public List<Taxonomy> getTaxonomies(String locale,String authenticationToken) {

		return taxonomyService.getTaxonomies(locale);
	}
	
	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Taxonomy getTaxonomy(String taxonomyName, String locale, String authenticationToken) {
		
		return taxonomyService.getTaxonomy(taxonomyName, locale);
	}

	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public Taxonomy saveTaxonomy(Taxonomy taxonomy, String authenticationToken) {
		
		return  taxonomyService.saveTaxonomy(taxonomy);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getAllTaxonomies(ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint,
			String authenticationToken) {
		return taxonomyService.getAllTaxonomies(output, fetchLevel,prettyPrint);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public <T> T getTaxonomy(String taxonomyIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel, boolean prettyPrint, String authenticationToken) {
		return taxonomyService.getTaxonomy(taxonomyIdOrName, output, fetchLevel,prettyPrint);
	}

	@RolesAllowed("ROLE_CMS_TAXONOMY_EDITOR")
	public Taxonomy save(Object taxonomySource, String authenticationToken) {
		return taxonomyService.save(taxonomySource);
	}

	@RolesAllowed("ROLE_CMS_EXTERNAL_VIEWER")
	public Taxonomy getTaxonomyById(String taxonomyId,
			String authenticationToken) {
		return taxonomyService.getTaxonomyById(taxonomyId);
	}
}
