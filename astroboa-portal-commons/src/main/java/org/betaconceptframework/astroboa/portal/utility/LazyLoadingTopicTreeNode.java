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
package org.betaconceptframework.astroboa.portal.utility;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.portal.utility.TopicComparator.OrderByProperty;
import org.betaconceptframework.ui.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingTopicTreeNode extends LazyLoadingTreeNodeRichFaces{

	private Topic topic;
	private OrderByProperty orderByProperty;

	//Default constructor is used when this tree node is the root node
	public LazyLoadingTopicTreeNode(){

		super("0", "", null, "TOPIC_ROOT_NODE",false);

	}

	public LazyLoadingTopicTreeNode(String identifier, Topic topic,
			TreeNode parent, OrderByProperty orderByProperty) {
		super(identifier,
				topic.getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()) == null ? JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null) : topic.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()),
						parent, 
						"TOPIC_NODE" ,
						(topic.getNumberOfChildren() == 0? true: false));

		this.topic = topic;
		this.orderByProperty = orderByProperty;

	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {

			if (topic != null){
				logger.debug("retrieve children topic: {}", this.getIdentifier());

				List<Topic> childrenTopics = topic.getChildren();

				Collections.sort(childrenTopics, new TopicComparator(JSFUtilities.getLocaleAsString(), orderByProperty));

				//int nodeIndex = 0;
				if (CollectionUtils.isNotEmpty(childrenTopics)){
					for (Topic childTopic : childrenTopics){

						LazyLoadingTopicTreeNode childTopicTreeNode = new LazyLoadingTopicTreeNode(childTopic.getId(),childTopic, this, orderByProperty);

						children.put(childTopicTreeNode.getIdentifier(), childTopicTreeNode);
						//nodeIndex++;
					}
				}

			}
		}

		return children.entrySet().iterator();
	}

	public Topic getTopic(){
		return topic;
	}

}
