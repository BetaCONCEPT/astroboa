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
package org.betaconceptframework.astroboa.portal.resource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectResourceContext extends ResourceContext {
	
	
	// holds the list of topics corresponding to the topic names provided in
	// "commaOrPlusDelimitedTopicNames" query parameter
	// the list can be utilized by jsf pages (i.e. to generate appropriate
	// messages)
	private List<Topic> topics = new ArrayList<Topic>();

	// holds the list of localized content object type names corresponding
	// to the content object types
	// provided in "commaDelimitedContentObjectTypes" query parameter
	// the list can be utilized by jsf pages (i.e. to generate appropriate
	// messages)
	private List<ContentObjectTypeDefinition> contentObjectTypeDefinitions = new ArrayList<ContentObjectTypeDefinition>();

	private Calendar fromCalendar;
	private Calendar toCalendar;

	// Web2SharingLinks
	private Map<String, String> web2SharingLinks = new HashMap<String, String>();
	
	// It is used when asking for resources that are contained in content area objects 
	// i.e. resources that are not found directly by a query but instead their container object is searched and the referenced objects are retrieved.
	// It holds the container object (scheduledContentArea or dynamicContentArea for example) which contains the resource collection returned  in the resource response
	private ContentObject resourceContainerObject;
	
	
	public String getSymbolSepatatedTopicLabels(String locale, String separator) {
		
		StringBuilder commaSeparatedTopicLabels = new StringBuilder();
		
		if (!CollectionUtils.isEmpty(topics)) {
			
			int topicListSize = topics.size();
			Topic topic;
			for (int i=0; i < topicListSize - 1; ++i) {
				topic = topics.get(i);
				commaSeparatedTopicLabels.append(topic.getLocalizedLabelForLocale(locale))
					.append(separator);
			}
			topic = topics.get(topicListSize -1);
			commaSeparatedTopicLabels.append(topic.getLocalizedLabelForLocale(locale));
		}
		
		return commaSeparatedTopicLabels.toString();
	}
	
	
	public List<Topic> getTopics() {
		return topics;
	}
	
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Calendar getFromCalendar() {
		return fromCalendar;
	}

	public void setFromCalendar(Calendar fromCalendar) {
		this.fromCalendar = fromCalendar;
	}

	public Calendar getToCalendar() {
		return toCalendar;
	}

	public void setToCalendar(Calendar toCalendar) {
		this.toCalendar = toCalendar;
	}

	public Map<String, String> getWeb2SharingLinks() {
		return web2SharingLinks;
	}

	public void setWeb2SharingLinks(Map<String, String> web2SharingLinks) {
		this.web2SharingLinks = web2SharingLinks;
	}

	public List<ContentObjectTypeDefinition> getContentObjectTypeDefinitions() {
		return contentObjectTypeDefinitions;
	}

	public void setContentObjectTypeDefinitions(
			List<ContentObjectTypeDefinition> contentObjectTypeDefinitions) {
		this.contentObjectTypeDefinitions = contentObjectTypeDefinitions;
	}

	public ContentObject getResourceContainerObject() {
		return resourceContainerObject;
	}

	public void setResourceContainerObject(ContentObject resourceContainerObject) {
		this.resourceContainerObject = resourceContainerObject;
	}
}
