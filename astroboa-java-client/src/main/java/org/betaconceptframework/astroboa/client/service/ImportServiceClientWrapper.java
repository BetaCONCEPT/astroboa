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
package org.betaconceptframework.astroboa.client.service;

import java.net.URI;
import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.api.service.secure.ImportServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Import Service Client Wrapper.
 * 
 * Responsible to connect to appropriate import service (either local or remote)
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportServiceClientWrapper extends AbstractClientServiceWrapper implements ImportService  {


	private ImportServiceSecure importServiceSecure;

	public ImportServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	protected void resetService() {
		importServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				importServiceSecure = (ImportServiceSecure) connectToLocalService(ImportServiceSecure.class);
			}
			else{
				importServiceSecure = (ImportServiceSecure) connectToRemoteService(ImportServiceSecure.class);
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			importServiceSecure = null;
		}

		return importServiceSecure != null;
	}

	@Override
	public Future<ImportReport> importRepositoryContentFromString(String contentSource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryContentFromString(contentSource, configuration, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}

	}

	@Override
	public Future<ImportReport> importRepositoryContentFromURI(URI contentSource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryContentFromURI(contentSource, configuration, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public ContentObject importContentObject(String contentSource,ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importContentObject(contentSource, configuration, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public RepositoryUser importRepositoryUser(String repositoryUserSource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryUser(repositoryUserSource, configuration, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}

	}

	@Override
	public Space importSpace(String spaceSource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importSpace(spaceSource, configuration,getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public Taxonomy importTaxonomy(String taxonomySource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importTaxonomy(taxonomySource, configuration, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public Topic importTopic(String topicSource, ImportConfiguration configuration) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importTopic(topicSource, configuration,getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}
}
