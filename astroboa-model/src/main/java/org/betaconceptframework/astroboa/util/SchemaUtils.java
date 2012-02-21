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
package org.betaconceptframework.astroboa.util;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class SchemaUtils {

	public static String buildSchemaLocationForAstroboaModelSchemaAccordingToActiveClient(){
		return buildSchemaLocationAccordingToActiveClient(CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION, false);
	}

	public static String buildSchemaLocationForAstroboaApiSchemaAccordingToActiveClient(){
		return buildSchemaLocationAccordingToActiveClient(CmsConstants.ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION, false);
	}

	public static String buildSchemaLocationAccordingToActiveClient(String schemaFileName, boolean includeOutputType){
		
		UrlProperties urlProperties = new UrlProperties();
		
		if (includeOutputType){
			urlProperties.setResourceRepresentationType(ResourceRepresentationType.XSD);
		}
		urlProperties.setFriendly(true);
		urlProperties.setRelative(false);
		urlProperties.setName(schemaFileName);

		return ResourceApiURLUtils.generateUrlForType(CmsDefinition.class, urlProperties );
	}

	public static void appendSchemaLocationToMarshaller(
			Marshaller marshaller,
			String namespace,
			String schemaLocationURL,
			String prefix) throws PropertyException {

		if (StringUtils.isBlank(namespace) || StringUtils.isBlank(schemaLocationURL)){
			return;
		}
		
		String schemaLocation = (String) marshaller.getProperty(Marshaller.JAXB_SCHEMA_LOCATION);

		if (schemaLocation == null){
			schemaLocation = "";
		}

		if (! schemaLocation.contains(namespace)){
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation+ 
				" "+namespace+" "+schemaLocationURL);
		}
	}
	
	private static String removeWhitespaceAndTrailingSlash(String urlPath) {
		if (urlPath == null){
			return null;
		}
		
		urlPath = urlPath.trim();
		if (urlPath.endsWith("/")) {
			urlPath = urlPath.substring(0, urlPath.length()-1);
		}
		return urlPath;
	}
}
