/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.util;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.CmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ResourceApiURLUtils {

	public static String generateUrlForType(Class<?> type, ResourceRepresentationType<?>  resourceRepresentationType, boolean relative, String... identifierOrNameOrPath) {
		
		StringBuilder sb = new StringBuilder();

		if (!relative){
			addHost(sb);
		}
		
		addResourceApiPrefix(sb);
		
		addRepositoryId(sb);

		addURLPartForType(sb, type, identifierOrNameOrPath);
		
		addOutput(resourceRepresentationType, sb, type);
		
		return sb.toString();
	}

	public static String generateUrlForEntity(Object cmsEntity, ResourceRepresentationType<?>  resourceRepresentationType,	boolean relative) {

		if (cmsEntity instanceof BinaryChannel){
			return ((BinaryChannel)cmsEntity).buildResourceApiURL(null, null, null, null, null, false, relative);
		}
		
		StringBuilder sb = new StringBuilder();

		if (!relative){
			addHost(sb);
		}
		
		addResourceApiPrefix(sb);
		
		addRepositoryId(sb);

		addURLForEntity(sb, cmsEntity);
  
		//Do not add resource representation output if property is a Binary Property
		//since its url practically represents the binary content
		//TODO: Clarify this issue
		if (! (cmsEntity instanceof BinaryProperty)){
			addOutput(resourceRepresentationType, sb, cmsEntity.getClass());
		}
		
		return sb.toString();
	}
	
	private static void addURLPartForType(StringBuilder sb, Class<?> type, String... identifierOrNameOrPath) {
		
		if (type == null){
			return;
		}
		else{ 
			
			if (identifierOrNameOrPath == null || identifierOrNameOrPath.length == 0){
				identifierOrNameOrPath = new String[]{""};
			}
			
			sb.append(CmsConstants.FORWARD_SLASH);
			
			if (ContentObject.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
					.append(CmsConstants.FORWARD_SLASH)
					.append(identifierOrNameOrPath[0]);
			}
			else if (Topic.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Topic.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(identifierOrNameOrPath[0]);
			}
			else if (Space.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Space.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(identifierOrNameOrPath[0]);
			}
			else if (Taxonomy.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Taxonomy.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(identifierOrNameOrPath[0]);
			}
			else if (BinaryChannel.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.BinaryChannel.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(identifierOrNameOrPath[0]);
			}
			else if (CmsDefinition.class.isAssignableFrom(type)){
				sb.append("definition")
				.append(CmsConstants.FORWARD_SLASH)
				.append(identifierOrNameOrPath[0]);
			}
			else if (CmsProperty.class.isAssignableFrom(type)){
				
				//Expecting 2 values
				//The first one is the content object id or system name
				//and the second one is property's path
				sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
					.append(CmsConstants.FORWARD_SLASH)
					.append(identifierOrNameOrPath[0]);
				
				if(identifierOrNameOrPath.length>=2){
					sb.append(CmsConstants.FORWARD_SLASH)
					.append(identifierOrNameOrPath[1]);
				}
			}
		}
	}
	private static void addURLForEntity(StringBuilder sb, Object cmsEntity) {
		
		if (cmsEntity == null){
			return ;
		}
		
		sb.append(CmsConstants.FORWARD_SLASH);
		
		if (cmsEntity instanceof Taxonomy){
			sb.append(CmsBuiltInItem.Taxonomy.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(((Taxonomy)cmsEntity).getName());
		}
		else if (cmsEntity instanceof Topic){
			sb.append(CmsBuiltInItem.Topic.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)	
				.append(((Topic)cmsEntity).getName());
		}
		else if (cmsEntity instanceof Space){
			sb.append(CmsBuiltInItem.Space.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(((Space)cmsEntity).getName());
		}
		else if (cmsEntity instanceof ContentObject){
			sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(((ContentObject)cmsEntity).getSystemName());
		}
		else if (cmsEntity instanceof CmsProperty<?,?>){
			sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH)
				.append(((CmsPropertyImpl<?,?>)cmsEntity).getContentObjectSystemName())
				.append(CmsConstants.FORWARD_SLASH)
				.append(((CmsProperty<?,?>)cmsEntity).getPermanentPath());
		}
		else if (cmsEntity instanceof ContentObjectTypeDefinition){
			sb.append("definition")
			.append(CmsConstants.FORWARD_SLASH)
			.append(((ContentObjectTypeDefinition)cmsEntity).getName());
		}
		else if (cmsEntity instanceof CmsPropertyDefinition){
			sb.append("definition")
			.append(CmsConstants.FORWARD_SLASH)
			.append(((CmsPropertyDefinition)cmsEntity).getFullPath());
		}

	}

	private static void addOutput(ResourceRepresentationType<?>  resourceRepresentationType,
			StringBuilder sb, Class<?> type) {
		
		//XSD is considered default value and therefore it is not added
		//if type is CmsDefinition
		if (CmsDefinition.class.isAssignableFrom(type) && 
				resourceRepresentationType != null && resourceRepresentationType == ResourceRepresentationType.XSD){
			return ;
		}
		
		//Add output. XML is considered default value and therefore it is not added
		if (resourceRepresentationType != null && resourceRepresentationType != ResourceRepresentationType.XML){
			sb.append("?output=");
			sb.append(resourceRepresentationType.getTypeAsString().toLowerCase());
		}
	}


	private static void addHost(StringBuilder sb) {
		CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();
		if (activeRepository!= null &&
				StringUtils.isNotBlank(activeRepository.getServerURL())){
			sb.append(removeWhitespaceAndTrailingSlash(activeRepository.getServerURL()));
		}
	}
	
	private static void addResourceApiPrefix(StringBuilder sb) {
		
		
		CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();
		if (activeRepository!= null &&
				StringUtils.isNotBlank(activeRepository.getServerURL())){
			final String restFulApiBasePath = removeWhitespaceAndTrailingSlash(activeRepository.getRestfulApiBasePath());
			
			if (restFulApiBasePath!=null && ! restFulApiBasePath.startsWith(CmsConstants.FORWARD_SLASH)){
				//Append with base URL 
				sb.append(CmsConstants.FORWARD_SLASH);
			}
			
			sb.append(restFulApiBasePath);
		}
	}
	
	private static void addRepositoryId(StringBuilder sb) {
		
		//Add repository identifier
		sb.append(CmsConstants.FORWARD_SLASH);
		sb.append(retrieveRepositoryId());
	}
	
	private static String retrieveRepositoryId() {
		return AstroboaClientContextHolder.getActiveRepositoryId();
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
	
	public static String buildContetApiURLForBinaryProperty(String contentObjectIdOrSystemName, String binaryPropertyPath, String mimeType, 
			Integer width, Integer height, ContentDispositionType contentDispositionType) {

		StringBuilder contentApiURLBuilder = new StringBuilder();

		addHost(contentApiURLBuilder);
		
		addResourceApiPrefix(contentApiURLBuilder);
		
		addRepositoryId(contentApiURLBuilder);
		
		if (StringUtils.isBlank(contentObjectIdOrSystemName) || StringUtils.isBlank(binaryPropertyPath)){
			return contentApiURLBuilder.toString();
		}
		
		// Astroboa RESTful API URL pattern for accessing the value of content object properties
		// http://server/content-api/
		// <reposiotry-id>/contentObject/<contentObjectId>/<binaryChannelPropertyValuePath>
		// ?contentDispositionType=<contentDispositionType>&width=<width>&height=<height>
			
		contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH+"contentObject");

		contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH+contentObjectIdOrSystemName);
		
		contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH+binaryPropertyPath);
		
		boolean questionMarkHasBeenUsed = false;
		
		if (contentDispositionType !=null){
			contentApiURLBuilder
				.append(CmsConstants.QUESTION_MARK)
				.append("contentDispositionType")
				.append(CmsConstants.EQUALS_SIGN)
				.append(contentDispositionType.toString().toLowerCase());
			questionMarkHasBeenUsed = true;
		}
		
		if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/")){
			if (width != null && width > 0){
				contentApiURLBuilder
					.append(questionMarkHasBeenUsed ? CmsConstants.AMPERSAND : CmsConstants.QUESTION_MARK)
					.append("width")
					.append(CmsConstants.EQUALS_SIGN)
					.append(width);
				questionMarkHasBeenUsed = true;
			}
			
			if (height != null && height > 0){
				contentApiURLBuilder.append(questionMarkHasBeenUsed ? CmsConstants.AMPERSAND : CmsConstants.QUESTION_MARK)
					.append("height")
					.append(CmsConstants.EQUALS_SIGN)
					.append(height);
			}
		}

		return contentApiURLBuilder.toString();
	}

}
