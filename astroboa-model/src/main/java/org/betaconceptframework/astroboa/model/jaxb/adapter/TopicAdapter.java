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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.type.TopicType;

/**
 * Used to control marshalling mainly of a topic in order to avoid circular
 * problems.
 * 
 * Although both types are known to JAXB context, 
 * we copy provided Type to a new one which has less
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicAdapter extends XmlAdapter<TopicType, Topic>{

	@Override
	public TopicType marshal(Topic topic) throws Exception {
		return marshal(topic, ResourceRepresentationType.XML);
	}
	
	
	public TopicType marshal(Topic topic, ResourceRepresentationType<?>  resourceRepresentationType) throws Exception {
		
		if (topic != null){
			TopicType topicType = new TopicType();
			topicType.setId(topic.getId());
			topicType.setName(topic.getName());
			topicType.setSystemBuiltinEntity(topic.isSystemBuiltinEntity());
			topicType.getLocalizedLabels().putAll(topic.getLocalizedLabels());
			topicType.setOwner(topic.getOwner());
			
			//TODO: Check whether user may have more control on whether a friendly url is generated or not
			topicType.setUrl(topic.getResourceApiURL(resourceRepresentationType, false, topic.getName()!=null));

			if (topic.getNumberOfChildren() > 0){
				topicType.setNumberOfChildren(topic.getNumberOfChildren());
			}

			
			return topicType;
		}
		
		return null;
	}

	@Override
	public Topic unmarshal(TopicType topicType) throws Exception {
		
		if (topicType != null){
			
			Topic topic = (Topic) topicType.getCmsRepositoryEntityFromContextUsingCmsIdentifierOrReference();

			if (topic != null){
				return topic;
			}
			
			if (topicType.getName() != null){
				
				//Topic name is not unique therefore taxonomy name is added
				if (topicType.getTaxonomy() != null && topicType.getTaxonomy().getName() != null){
					topic = (Topic) topicType.getCmsRepositoryEntityFromContextUsingKey(topicType.getName()+topicType.getTaxonomy().getName());
				}
				
				if (topic != null){
					return topic;
				}
				
				//Last try Use just topic name
				topic = (Topic) topicType.getCmsRepositoryEntityFromContextUsingKey(topicType.getName());

				if (topic != null){
					return topic;
				}
				
			}


			topic = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic();

			topic.setId(topicType.getId());
			topic.setName(topicType.getName());
			topic.setSystemBuiltinEntity(topicType.isSystemBuiltinEntity());
			topic.getLocalizedLabels().putAll(topicType.getLocalizedLabels());
			topic.setTaxonomy(topicType.getTaxonomy());
			topic.setOwner(topicType.getOwner());

			topicType.registerCmsRepositoryEntityToContext(topic);

			return (Topic) topic;
		}
		
		return null;
	}

}
