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
package org.betaconceptframework.astroboa.portal.utility;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.portal.managedbean.PortalManager;
import org.betaconceptframework.astroboa.portal.resource.ContentObjectResourceContext;
import org.betaconceptframework.astroboa.portal.resource.ResourceResponse;
import org.betaconceptframework.astroboa.portal.schedule.PortalEventNames;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndImageImpl;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("syndicationFeedGenerator")
@Scope(ScopeType.APPLICATION)
public class SyndicationFeedGenerator<T extends ContentObjectResourceContext> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@In
	private PortalManager portalManager;
	
	public SyndFeed generateFeedForResourceList(ResourceResponse<ContentObject, T> resourceResponse, String feedType, String feedTitle, String feedDescription, String feedImageURL, String portalHost, String portalContext, TimeZone timeZone, Locale locale) {
		
		try {
			List<ContentObject> contentObjectList = resourceResponse.getResourceRepresentation();

			SyndFeed feed = new SyndFeedImpl();
			
			if (
					!(
							SyndicationFeedType.RSS_VERSION_2_0.equals(feedType) || 
							SyndicationFeedType.RSS_VERSION_1_0.equals(feedType)  ||
							SyndicationFeedType.ATOM_VERSION_1_0.equals(feedType)
					)
				) {
				logger.warn("Feed Type: " + feedType + " is not supported. An null feed will be returned");
				return null;
			}
				
			feed.setFeedType(feedType);

			String currentLocale = null;

			if (locale != null) {
				currentLocale = locale.toString();
			}
			else {
				locale = new Locale("el");
				currentLocale = PortalStringConstants.DEFAULT_LOCALE;
			}
			
			if (timeZone == null) {
				timeZone = TimeZone.getDefault();
			}

			feed.setTitle(generateFeedTitle(resourceResponse, feedTitle, currentLocale));
			String feedLinkURL = generateFeedLink(resourceResponse, portalHost, portalContext);
			feed.setLink(feedLinkURL);
			feed.setDescription(generateFeedDescription(resourceResponse, feedDescription, currentLocale));
			feed.setPublishedDate(GregorianCalendar.getInstance(timeZone, locale).getTime());
			
			if (StringUtils.isNotBlank(feedImageURL)) {
				SyndImage syndImage = new SyndImageImpl();
				syndImage.setTitle("Logo");
				syndImage.setUrl(feedImageURL);
				syndImage.setLink(feedLinkURL);
				feed.setImage(syndImage);
			}
			
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			SyndEntry entry;

			for (ContentObject contentObject : contentObjectList) {
				entry = new SyndEntryImpl();
				
				StringProperty titleProperty = (StringProperty)contentObject.getCmsProperty("profile.title");
				if (titleProperty != null && StringUtils.isNotBlank(titleProperty.getSimpleTypeValue())) {
					entry.setTitle(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue());
				}
				else { // if no title exists skip this entry
					continue;
				}
				
				BinaryProperty thumbnailProperty = null;
				BinaryProperty imageProperty = null;
				
				if (contentObject.getTypeDefinition().hasCmsPropertyDefinition("thumbnail")) {
					thumbnailProperty = (BinaryProperty) contentObject.getCmsProperty("thumbnail");
				}
				
				if (contentObject.getTypeDefinition().hasCmsPropertyDefinition("image")) {
					imageProperty = (BinaryProperty) contentObject.getCmsProperty("image");
				}
				StringProperty descriptionProperty = (StringProperty)contentObject.getCmsProperty("profile.description");
				 
				
				// we will generate a description if at least a thumbnail or a description exists
				URL thumbnailURL = null;
				SyndContent syndContent = new SyndContentImpl();
				if (thumbnailProperty != null && thumbnailProperty.getSimpleTypeValue() != null) {
						//thumbnailURL = new URL("http://" + portalHost + "/content-api/f/binaryChannel/" + thumbnailProperty.getSimpleTypeValue().getFileAccessInfo());
					thumbnailURL = new URL("http://" + portalHost + thumbnailProperty.getSimpleTypeValue().getRelativeContentApiURL());
						syndContent.setValue("<img src=\"" + thumbnailURL.toString() + "\"" + "</img>");
				}
				
				if (descriptionProperty != null && StringUtils.isNotBlank(descriptionProperty.getSimpleTypeValue())) {
					if (StringUtils.isBlank(syndContent.getValue())) {
						syndContent.setValue(((StringProperty)contentObject.getCmsProperty("profile.description")).getSimpleTypeValue());
					}
					else {
						syndContent.setValue(syndContent.getValue() + "<p>" + ((StringProperty)contentObject.getCmsProperty("profile.description")).getSimpleTypeValue() + "</p>");
					}
					
				}
				
				//Raise event to allow users to add custom information related to syndication feed entry
				Events.instance().raiseEvent(PortalEventNames.EVENT_CONTENT_OBJECT_ADD_SYNDICATION_FEED_CONTENT, contentObject, syndContent);
				
				if (StringUtils.isNotBlank(syndContent.getValue())) {
					entry.setDescription(syndContent);
					entry.setContents(Collections.singletonList(syndContent));
				}
				
				entry.setLink(generateFeedEntryLink(contentObject.getId(), portalHost, portalContext));

				// check for image and thumbnail to create a MediaRSS entry
				if (imageProperty != null && imageProperty.hasValues()) {
					
					//It may be the case that image is a multiple property
					BinaryChannel image = null;
					
					if (imageProperty.getPropertyDefinition() != null)
					{
						if (imageProperty.getPropertyDefinition().isMultiple())
						{
							image = imageProperty.getSimpleTypeValues().get(0);
						}
						else
						{
							image = imageProperty.getSimpleTypeValue();
						}
					}
						
					if (image != null)
					{
					MediaContent[] mediaContents = new MediaContent[1];
					//MediaContent mediaContent = new MediaContent( new UrlReference("http://" + portalHost + "/content-api/f/binaryChannel/" + imageProperty.getSimpleTypeValue().getFileAccessInfo()));
					MediaContent mediaContent = new MediaContent( new UrlReference("http://" + portalHost + image.getRelativeContentApiURL()));
					mediaContents[0] = mediaContent;
					if (thumbnailURL != null) {
						Metadata md = new Metadata();
						Thumbnail[] thumbs = new Thumbnail[1];
						thumbs[0] = new Thumbnail(thumbnailURL.toURI());
						md.setThumbnail( thumbs );
						mediaContent.setMetadata( md );
					}
					MediaEntryModuleImpl module = new MediaEntryModuleImpl();
					module.setMediaContents(mediaContents);
					entry.getModules().add( module );
					}
				}
				
				CalendarProperty webPublicationProperty = (CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate");
				if (webPublicationProperty != null && webPublicationProperty.getSimpleTypeValue() != null) {
					entry.setPublishedDate(webPublicationProperty.getSimpleTypeValueAsDate());
				}
				else {
					CalendarProperty lastModificationDate = (CalendarProperty) contentObject.getCmsProperty("profile.modified");
					entry.setPublishedDate(lastModificationDate.getSimpleTypeValueAsDate());
				}
				
				entries.add(entry);
			}

			feed.setEntries(entries);

			return feed;
		}
		catch (Exception e) {
			logger.error("An error occured while creating the feed", e);
			return null;
		}
	}
	
	private String generateFeedTitle(ResourceResponse<ContentObject, T> resourceResponse, String feedTitle, String locale) {
		String title;
		
		StringProperty portalLabel = (StringProperty) portalManager.getPortal().getCmsProperty("localizedLabels." + locale);
		if (portalLabel != null) {
			title = portalLabel.getSimpleTypeValue() + ": ";
		}
		else {
			title = "Portal Feed: ";
		}
		
		
		if (StringUtils.isNotBlank(feedTitle)) {
			title = title + feedTitle;
		}
		else {
			boolean existContentType = false;
			List<ContentObjectTypeDefinition> contentTypesInResponse = resourceResponse.getResourceContext().getContentObjectTypeDefinitions();
			if (CollectionUtils.isNotEmpty(contentTypesInResponse)) {
				existContentType = true;
				title = title + contentTypesInResponse.get(0).getDisplayName().getLocalizedLabelForLocale(locale);
				for (ContentObjectTypeDefinition definition : contentTypesInResponse.subList(1, contentTypesInResponse.size())) {
					title = title + ',' + definition.getDisplayName().getLocalizedLabelForLocale(locale);
				}
			}
			List<Topic> topicsOfResponse = resourceResponse.getResourceContext().getTopics();
			if (CollectionUtils.isNotEmpty(topicsOfResponse)) {
				if (existContentType) {
					title = title + " / " + topicsOfResponse.get(0).getLocalizedLabelForLocale(locale);
				}
				else {
					title = title + topicsOfResponse.get(0).getLocalizedLabelForLocale(locale);
				}
				
				for (Topic topic : resourceResponse.getResourceContext().getTopics().subList(1, topicsOfResponse.size())) {
					title = title + ',' + topic.getLocalizedLabelForLocale(locale);
				}
			}
		}
		
		return title;
	}
	
	private String generateFeedLink(ResourceResponse<ContentObject, T> resourceResponse, String portalHost, String portalContext) {
		String feedRequestURL = resourceResponse.getResourceContext().getResourceRequestURL();
		String siteURL = feedRequestURL.replaceFirst("/resourceRepresentationType/(rss_2.0|rss_1.0|atom_1.0)", "");
		return "http://" + portalHost + portalContext + siteURL;
	}
	
	
	private String generateFeedDescription(ResourceResponse<ContentObject, T> resourceResponse, String feedDescription, String locale) {
		String description;
		
		StringProperty portalLabel = (StringProperty) portalManager.getPortal().getCmsProperty("localizedLabels." + locale);
		if (portalLabel != null) {
			description = "Feed provided by:" + portalLabel.getSimpleTypeValue() + ". ";
		}
		else {
			description = "";
		}
		
		
		if (StringUtils.isNotBlank(feedDescription)) {
			description = description + feedDescription + ". ";
		}
		
		List<ContentObjectTypeDefinition> contentTypesInResponse = resourceResponse.getResourceContext().getContentObjectTypeDefinitions();
		if (CollectionUtils.isNotEmpty(contentTypesInResponse)) {
			description = description + "Content Types in this feed: " + contentTypesInResponse.get(0).getDisplayName().getLocalizedLabelForLocale(locale);
			for (ContentObjectTypeDefinition definition : contentTypesInResponse.subList(1, contentTypesInResponse.size())) {
				description = description + ',' + definition.getDisplayName().getLocalizedLabelForLocale(locale);
			}
			description = description + ". ";
		}

		List<Topic> topicsOfResponse = resourceResponse.getResourceContext().getTopics();
		if (CollectionUtils.isNotEmpty(topicsOfResponse)) {	
			description = description + "Topics / Tags for this feed: " + topicsOfResponse.get(0).getLocalizedLabelForLocale(locale);
			
			for (Topic topic : resourceResponse.getResourceContext().getTopics().subList(1, topicsOfResponse.size())) {
				description = description + ',' + topic.getLocalizedLabelForLocale(locale);
			}
			
			description = description + ". ";
		}
		
		return description;
	}
	
	private String generateFeedEntryLink(String resourceId, String portalHost, String portalContext) {
		return "http://" + portalHost + portalContext + "/resource/contentObject/id/" + resourceId;
	}
}
