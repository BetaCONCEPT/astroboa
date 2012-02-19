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

import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.api.service.secure.SerializationServiceSecure;
import org.betaconceptframework.astroboa.client.AstroboaClient;

/**
 * Serialization Service Client Wrapper.
 * 
 * Responsible to connect to appropriate serialization service (either local or remote)
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SerializationServiceClientWrapper extends AbstractClientServiceWrapper implements SerializationService  {


	private SerializationServiceSecure serializationServiceSecure;
	

	public SerializationServiceClientWrapper(
			AstroboaClient client, String serverHostNameOrIpAndPortToConnectTo) {
		super(client, serverHostNameOrIpAndPortToConnectTo);
	}

	@Override
	protected void resetService() {
		serializationServiceSecure = null;
	}

	@Override
	boolean loadService(boolean loadLocalService) {
		try{
			if (loadLocalService){
				serializationServiceSecure = (SerializationServiceSecure) connectToLocalService(SerializationServiceSecure.class);
			}
			else{
				logger.warn("Serialization Service is not supported for remote clients");
				serializationServiceSecure = null;
				return true;  // return true so that no exception is thrown during initialization
			}

		}catch(Exception e){
			//do not rethrow exception.Probably local service is not available
			serializationServiceSecure = null;
		}

		return serializationServiceSecure != null;
	}

	@Override
	public Future<SerializationReport> serializeObjects(ContentObjectCriteria objectCriteria, SerializationConfiguration serializationConfiguration) {
		
		if (serializationServiceSecure != null){
			return serializationServiceSecure.serializeObjects(objectCriteria, serializationConfiguration, getAuthenticationToken());
		}
		else{
			if (client.isConnectedToARemoteServer()){
				throw new CmsException("SerializationService is not supported in remote clients.");
			}
			else{
				throw new CmsException("SerializationService reference was not found");
			}
		}
	}

	@Override
	public Future<SerializationReport> serializeRepository(SerializationConfiguration serializationConfiguration) {
		if (serializationServiceSecure != null){
			return serializationServiceSecure.serializeRepository(serializationConfiguration,getAuthenticationToken());
		}
		else{
			if (client.isConnectedToARemoteServer()){
				throw new CmsException("SerializationService is not supported in remote clients.");
			}
			else{
				throw new CmsException("SerializationService reference was not found");
			}
		}
	}

	@Override
	public Future<SerializationReport> serializeOrganizationSpace() {
		if (serializationServiceSecure != null){
			return serializationServiceSecure.serializeOrganizationSpace(getAuthenticationToken());
		}
		else{
			if (client.isConnectedToARemoteServer()){
				throw new CmsException("SerializationService is not supported in remote clients.");
			}
			else{
				throw new CmsException("SerializationService reference was not found");
			}
		}
	}

	@Override
	public Future<SerializationReport> serializeRepositoryUsers() {
		if (serializationServiceSecure != null){
			return serializationServiceSecure.serializeRepositoryUsers(getAuthenticationToken());
		}
		else{
			if (client.isConnectedToARemoteServer()){
				throw new CmsException("SerializationService is not supported in remote clients.");
			}
			else{
				throw new CmsException("SerializationService reference was not found");
			}
		}
	}

	@Override
	public Future<SerializationReport> serializeTaxonomies() {
		if (serializationServiceSecure != null){
			return serializationServiceSecure.serializeTaxonomies(getAuthenticationToken());
		}
		else{
			if (client.isConnectedToARemoteServer()){
				throw new CmsException("SerializationService is not supported in remote clients.");
			}
			else{
				throw new CmsException("SerializationService reference was not found");
			}
		}

	}
}
