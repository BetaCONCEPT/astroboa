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

	public static String generateUrlForType(Class<?> type, UrlProperties urlProperties) {
		
		if (urlProperties == null){
			urlProperties = new UrlProperties();
		}
		
		StringBuilder sb = new StringBuilder();

		if (!urlProperties.isRelative()){
			addHost(sb);
		}
		
		addResourceApiPrefix(sb);
		
		addRepositoryId(sb);

		addURLPartForType(sb, type, urlProperties);
		
		addOutput(urlProperties.getResourceRepresentationType(), sb, type);
		
		return sb.toString();
	}

	public static String generateUrlForEntity(Object cmsEntity, UrlProperties urlProperties) {

		if (urlProperties == null){
			urlProperties = new UrlProperties();
		}
		
		if (cmsEntity instanceof BinaryChannel){
			return ((BinaryChannel)cmsEntity).buildResourceApiURL(null, null, null, null, null, urlProperties.isFriendly(), urlProperties.isRelative());
		}
		
		StringBuilder sb = new StringBuilder();

		if (!urlProperties.isRelative()){
			addHost(sb);
		}
		
		addResourceApiPrefix(sb);
		
		addRepositoryId(sb);

		addURLForEntity(sb, cmsEntity, urlProperties);
  
		//Do not add resource representation output if property is a Binary Property
		//since its url practically represents the binary content
		//TODO: Clarify this issue
		if (! (cmsEntity instanceof BinaryProperty)){
			addOutput(urlProperties.getResourceRepresentationType(), sb, cmsEntity.getClass());
		}
		
		return sb.toString();
	}
	
	private static void addURLPartForType(StringBuilder sb, Class<?> type, UrlProperties urlProperties) {
		
		if (type == null){
			return;
		}
		else{ 
			
			sb.append(CmsConstants.FORWARD_SLASH);
			
			if (ContentObject.class.isAssignableFrom(type) || CmsProperty.class.isAssignableFrom(type) 
					|| BinaryChannel.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.ContentObject.getLocalPart());
			}
			else if (Topic.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Topic.getLocalPart());
			}
			else if (Space.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Space.getLocalPart());
			}
			else if (Taxonomy.class.isAssignableFrom(type)){
				sb.append(CmsBuiltInItem.Taxonomy.getLocalPart());
			}
			else if (CmsDefinition.class.isAssignableFrom(type)){
				sb.append("definition");
			}
			
			sb.append(CmsConstants.FORWARD_SLASH);
			
			if (urlProperties.isFriendly()){
				sb.append(urlProperties.getName());
			}
			else{
				sb.append(urlProperties.getIdentifier());
			}
			
			if (CmsProperty.class.isAssignableFrom(type) || 
					BinaryChannel.class.isAssignableFrom(type)){
				
					sb.append(CmsConstants.FORWARD_SLASH)
					.append(urlProperties.getPropertyPath());
				}
			}
	}
	
	private static void addURLForEntity(StringBuilder sb, Object cmsEntity, UrlProperties urlProperties) {
		
		if (cmsEntity == null){
			return ;
		}
		
		sb.append(CmsConstants.FORWARD_SLASH);
		
		if (cmsEntity instanceof Taxonomy){
			sb.append(CmsBuiltInItem.Taxonomy.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH);
			
			if (urlProperties.isFriendly()){
				sb.append(((Taxonomy)cmsEntity).getName());
			}
			else{
				sb.append(((Taxonomy)cmsEntity).getId());
			}
		}
		else if (cmsEntity instanceof Topic){
			sb.append(CmsBuiltInItem.Topic.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH);

			if (urlProperties.isFriendly()){
				sb.append(((Topic)cmsEntity).getName());
			}
			else{
				sb.append(((Topic)cmsEntity).getId());
			}
		}
		else if (cmsEntity instanceof Space){
			sb.append(CmsBuiltInItem.Space.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH);
			
			if (urlProperties.isFriendly()){
				sb.append(((Space)cmsEntity).getName());
			}
			else{
				sb.append(((Space)cmsEntity).getId());
			}
		}
		else if (cmsEntity instanceof ContentObject){
			sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH);
			
			if (urlProperties.isFriendly()){
				sb.append(((ContentObject)cmsEntity).getSystemName());
			}
			else{
				sb.append(((ContentObject)cmsEntity).getId());
			}
			
		}
		else if (cmsEntity instanceof CmsProperty<?,?>){
			sb.append(CmsBuiltInItem.ContentObject.getLocalPart())
				.append(CmsConstants.FORWARD_SLASH);
			
			if (urlProperties.isFriendly()){
				sb.append(((CmsPropertyImpl<?,?>)cmsEntity).getContentObjectSystemName());
			}
			else{
				sb.append(((CmsPropertyImpl<?,?>)cmsEntity).getContentObjectId());
			}
			
			sb.append(CmsConstants.FORWARD_SLASH)
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
		// http://server/resource-api/
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
