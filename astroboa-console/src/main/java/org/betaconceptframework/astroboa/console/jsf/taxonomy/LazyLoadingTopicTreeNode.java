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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.commons.comparator.TopicLocalizedLabelComparator;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.taxonomy.TaxonomyTree.TaxonomyTreeNodeType;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingTopicTreeNode extends LazyLoadingTreeNodeRichFaces{

	private Topic topic;
	private TopicService topicService;

	public LazyLoadingTopicTreeNode(String identifier, Topic topic,
			TreeNode parent) {
		super(identifier, topic.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()),
				parent, 
				TaxonomyTreeNodeType.TOPIC.toString() ,
				(topic.getNumberOfChildren() == 0? true: false));
		
		this.topic = topic;
		this.topicService = (TopicService) JSFUtilities.getBeanFromSpringContext("topicService");
		
	}

	@Override
	public Iterator<Entry<String, TreeNode>> getChildren() {
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retrieve children topic: " + this.getIdentifier());
			
			List<Topic> childrenTopics = topic.getChildren();
			
			if (CollectionUtils.isNotEmpty(childrenTopics)){
				Collections.sort(childrenTopics, new TopicLocalizedLabelComparator(JSFUtilities.getLocaleAsString()));
			
			//int nodeIndex = 0;
				for (Topic childTopic : childrenTopics){
					
					LazyLoadingTopicTreeNode childTopicTreeNode = new LazyLoadingTopicTreeNode(childTopic.getId(),childTopic, this);
					
					children.put(childTopicTreeNode.getIdentifier(), childTopicTreeNode);
					//nodeIndex++;
				}
			}
			
		}
		
		return children.entrySet().iterator();
	}
	
	public Topic getTopic(){
		return topic;
	}

	public LazyLoadingTopicTreeNode getTopicTreeNode(String topicId) {
		if (StringUtils.isBlank(topicId))
			return null;
		
		TreeNode topicTreeNode = getChild(topicId);
		
		if (topicTreeNode == null && !isLeaf() && children.size() > 0){
			//Search its children
			Collection<TreeNode> topicTreeNodes = children.values();
			
			for (TreeNode childTopicTreeNode : topicTreeNodes){
				topicTreeNode = ((LazyLoadingTopicTreeNode)childTopicTreeNode).getTopicTreeNode(topicId);
				
				if (topicTreeNode != null)
					break;
			}
		}
		
		return (LazyLoadingTopicTreeNode) topicTreeNode;
	}

	public void changeDescription(String localizedLabelForCurrentLocale) {
		this.description = localizedLabelForCurrentLocale;
	}

	public void reloadTopic() {
		topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
		this.children.clear();
		this.leaf = topic.getNumberOfChildren() == 0;
	}

	public boolean isContainerAndTopic(){
		return topic != null && topic.isAllowsReferrerContentObjects();
	}

	public void updateNoOfContentObjectReferrers() {
		if (topic!= null){
			//TODO From Savvas : 
			//In the near future, Topic API will contain a proper method
			//for reseting this value, in order to force LazyLoader.
			//For the time being, topic is cast to the implementation class in order to 
			//use the method we want
			((TopicImpl)topic).setNumberOfContentObjectIdsWhichReferToThisTopic(-1); //Flag to trigger LazyLoader next time getNumberOfContentObjectReferences will be called
			//topic.setNumberOfReferrerContentObjects(-1); //Flag to trigger LazyLoader next time getNumberOfContentObjectReferences will be called
		}

		
	}

	public void contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised() {
		if (topic != null && topic.isNumberOfReferrerContentObjectsLoaded()){
			updateNoOfContentObjectReferrers();
		}
		
	}
	
}
