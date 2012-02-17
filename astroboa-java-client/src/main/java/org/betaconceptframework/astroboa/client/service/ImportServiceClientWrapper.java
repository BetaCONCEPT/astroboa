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

import java.net.URL;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
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
	public ImportReport importRepositoryContentFromString(String contentSource) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryContentFromString(contentSource, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}

	}

	@Override
	public ImportReport importRepositoryContentFromURL(URL contentSource) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryContentFromURL(contentSource, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public ContentObject importContentObject(String contentSource,boolean version, boolean updateLastModificationDate, boolean save,Map<String, byte[]> binaryContent) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importContentObject(contentSource, version, updateLastModificationDate, save,binaryContent, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public RepositoryUser importRepositoryUser(String repositoryUserSource, boolean save) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importRepositoryUser(repositoryUserSource, save, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}

	}

	@Override
	public Space importSpace(String spaceSource, boolean save) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importSpace(spaceSource, save,getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public Taxonomy importTaxonomy(String taxonomySource, boolean save) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importTaxonomy(taxonomySource, save, getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}

	@Override
	public Topic importTopic(String topicSource, boolean save) {
		if (importServiceSecure != null){
			if (successfullyConnectedToRemoteService){  
				client.activateClientContext();
			}
			return importServiceSecure.importTopic(topicSource, save,getAuthenticationToken());
		}
		else{
			throw new CmsException("ImportService reference was not found");
		}
	}
}
