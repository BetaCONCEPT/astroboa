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
package org.betaconceptframework.astroboa.resourceapi.locator;

import java.net.HttpURLConnection;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.resource.BinaryChannelResource;
import org.betaconceptframework.astroboa.resourceapi.resource.ContentObjectResource;
import org.betaconceptframework.astroboa.resourceapi.resource.DefinitionResource;
import org.betaconceptframework.astroboa.resourceapi.resource.SecurityResource;
import org.betaconceptframework.astroboa.resourceapi.resource.TaxonomyResource;
import org.betaconceptframework.astroboa.resourceapi.resource.TopicResource;
import org.betaconceptframework.astroboa.util.CmsConstants;

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

	@Path(CmsConstants.RESOURCE_API_OBJECTS_COLLECTION_URI_PATH)
	public ContentObjectResource getObjectsCollectionResource(){
		
		return new ContentObjectResource(astroboaClient);
		
	}
	
	// this path will not be supported in the next major version, i.e. 4.0
	@Path(CmsConstants.DEPRECATED_RESOURCE_API_OBJECTS_COLLECTION_URI_PATH)
	public ContentObjectResource getContentObjectResource(){
		
		return new ContentObjectResource(astroboaClient);
		
	}
	
	@Path(CmsConstants.RESOURCE_API_TAXONOMIES_COLLECTION_URI_PATH)
	public TaxonomyResource getTaxonomiesCollectionResource(){
		
		return new TaxonomyResource(astroboaClient);
		
	}
	
	// this path will not be supported in the next major version, i.e. 4.0
	@Path(CmsConstants.DEPRECATED_RESOURCE_API_TAXONOMIES_COLLECTION_URI_PATH)
	public TaxonomyResource getTaxonomyResource(){
		
		return new TaxonomyResource(astroboaClient);
		
	}
	
	@Path(CmsConstants.RESOURCE_API_TOPICS_COLLECTION_URI_PATH)
	public TopicResource getTopicsCollectionResource(){
		
		return new TopicResource(astroboaClient);
		
	}
	
	// this path will not be supported in the next major version, i.e. 4.0
	@Path(CmsConstants.DEPRECATED_RESOURCE_API_TOPICS_COLLECTION_URI_PATH)
	public TopicResource getTopicResource(){
		
		return new TopicResource(astroboaClient);
		
	}
	
	@Path(CmsConstants.RESOURCE_API_BINARY_CHANNEL_URI_PATH)
	public BinaryChannelResource getBinaryChannelResource(){
		
		return new BinaryChannelResource(astroboaClient);

	}
	
	@Path(CmsConstants.RESOURCE_API_MODELS_COLLECTION_URI_PATH)
	public DefinitionResource getModelsCollectionResource(){
		
		return new DefinitionResource(astroboaClient);
		
	}
	
	// this path will not be supported in the next major version, i.e. 4.0
	@Path(CmsConstants.DEPRECATED_RESOURCE_API_MODELS_COLLECTION_URI_PATH)
	public DefinitionResource getDefinitionResource(){
		
		return new DefinitionResource(astroboaClient);
		
	}

	@Path("/definition")
	@Deprecated
	public DefinitionResource getDefinitionResourceUsingOldBasePath(){
		
		return new DefinitionResource(astroboaClient);
		
	}

	@Path(CmsConstants.RESOURCE_API_ENCRYPTION_UTILITY_URI_PATH)
	public SecurityResource getSecurityResource(){
		
		return new SecurityResource(astroboaClient);
		
	}

}
