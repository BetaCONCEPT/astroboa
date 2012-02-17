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
package org.betaconceptframework.astroboa.client.service;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.secure.TaxonomyServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Remote Taxonomy Service Wrapper responsible to connect to the provided repository
 * before any of tis method is called. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyServiceClientWrapper extends AbstractClientServiceWrapper implements TaxonomyService{

	private TaxonomyServiceSecure taxonomyServiceSecure;

	public TaxonomyServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	void resetService() {
		taxonomyServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				taxonomyServiceSecure = (TaxonomyServiceSecure) connectToLocalService(TaxonomyServiceSecure.class);
			}
			else{
				taxonomyServiceSecure = (TaxonomyServiceSecure) connectToRemoteService(TaxonomyServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			logger.warn("",e);
			taxonomyServiceSecure = null;
		}

		return taxonomyServiceSecure != null;
	}

	public boolean deleteTaxonomyTree(String taxonomyIdOrName) {

		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.deleteTaxonomyTree(taxonomyIdOrName, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}


	public Taxonomy getBuiltInSubjectTaxonomy(String locale) {

		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getBuiltInSubjectTaxonomy(locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}


	public List<Taxonomy> getTaxonomies(String locale) {

		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getTaxonomies(locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	public Taxonomy getTaxonomy(String taxonomyName, String locale) {

		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getTaxonomy(taxonomyName, locale, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	public Taxonomy saveTaxonomy(Taxonomy taxonomy) {

		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.saveTaxonomy(taxonomy, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	@Override
	public <T> T getAllTaxonomies(ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint) {
		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getAllTaxonomies(output, fetchLevel, prettyPrint, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	@Override
	public <T> T getTaxonomy(String taxonomyIdOrName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel, boolean prettyPrint) {
		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getTaxonomy(taxonomyIdOrName, output, fetchLevel, prettyPrint, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	@Override
	public Taxonomy save(Object taxonomySource) {
		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.save(taxonomySource, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

	@Override
	public Taxonomy getTaxonomyById(String taxonomyId) {
		if (taxonomyServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return taxonomyServiceSecure.getTaxonomyById(taxonomyId, getAuthenticationToken());
		}
		else{
			throw new CmsException("TaxonomyService reference was not found");
		}
	}

}
