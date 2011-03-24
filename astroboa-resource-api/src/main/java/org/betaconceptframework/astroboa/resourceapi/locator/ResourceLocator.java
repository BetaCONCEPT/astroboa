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
package org.betaconceptframework.astroboa.resourceapi.locator;

import java.net.HttpURLConnection;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.resource.BinaryChannelResource;
import org.betaconceptframework.astroboa.resourceapi.resource.ContentObjectResource;
import org.betaconceptframework.astroboa.resourceapi.resource.DefinitionResource;
import org.betaconceptframework.astroboa.resourceapi.resource.TaxonomyResource;
import org.betaconceptframework.astroboa.resourceapi.resource.TopicResource;

/**
 * Class responsible to locate the appropriate resource
 * to dispatch the request
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ResourceLocator {

	private AstroboaClient astroboaClient;
	
	public ResourceLocator(AstroboaClient astroboaClient) {
		this.astroboaClient = astroboaClient;
		
		if (this.astroboaClient == null){
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}

	@Path("/contentObject")
	public ContentObjectResource getContentObjectResource(){
		
		return new ContentObjectResource(astroboaClient);
		
	}
	
	@Path("/taxonomy")
	public TaxonomyResource getTaxonomyResource(){
		
		return new TaxonomyResource(astroboaClient);
		
	}
	
	@Path("/topic")
	public TopicResource getTopicResource(){
		
		return new TopicResource(astroboaClient);
		
	}
	
	@Path("/binaryChannel")
	public BinaryChannelResource getBinaryChannelResource(){
		
		return new BinaryChannelResource(astroboaClient);

	}
	
	@Path("/definition")
	public DefinitionResource getDefinitionResource(){
		
		return new DefinitionResource(astroboaClient);
		
	}

}
