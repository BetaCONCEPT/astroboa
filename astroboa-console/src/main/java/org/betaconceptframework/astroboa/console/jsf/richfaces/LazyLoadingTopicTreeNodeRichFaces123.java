/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf.richfaces;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;
import org.springframework.context.ApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author gchomatas
 * Created on Aug 31, 2007
 */
@Deprecated
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingTopicTreeNodeRichFaces123 extends LazyLoadingTreeNodeRichFaces {

	private Topic topic;
	private Taxonomy systemTaxonomy;
	
	public LazyLoadingTopicTreeNodeRichFaces123(String identifier, String description, TreeNode parent, Topic topic, String type, boolean leaf) {
		super(identifier, description, parent, type, leaf);
		this.topic = topic;
	}


	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retreive children of node: " + identifier);
			Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			String localisedTopicLabelAsString;
			ApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
			TaxonomyService taxonomyService = (TaxonomyService) ctx.getBean("taxonomyService");
			TopicService topicService = (TopicService) ctx.getBean("topicService");
			List<Topic> topicList;

			try {
				if (this.identifier.equals("0")){
					//Keep system taxonomy
					systemTaxonomy  = taxonomyService.getBuiltInSubjectTaxonomy(locale.toString());
					
					topicList = systemTaxonomy.getRootTopics();
				}
				else
					topicList = topic.getChildren();
				//	used before lazy loading child topics 
				//	topicList = taxonomyService.getSubTopics(this.getTopic().getId(), locale.toString());

				int nodeIndex = 0;

				if (topicList != null) { // possibly redundant check. See comment at the else part
					for (Topic childTopic : topicList) {

						LazyLoadingTopicTreeNodeRichFaces123 childTopicTreeNode;

						// check if container or containerAndTopic
						if (!childTopic.isAllowsReferrerContentObjects()) {
							childTopicTreeNode = new LazyLoadingTopicTreeNodeRichFaces123(identifier + ":" + String.valueOf(nodeIndex), childTopic.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()), this, childTopic, "containerOnlyTopic", false);
						}
						else {
							childTopicTreeNode = new LazyLoadingTopicTreeNodeRichFaces123(identifier + ":" + String.valueOf(nodeIndex), childTopic.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()), this, childTopic, "containerAndTopic", false);
						}

						//	check if childTopic is leaf
						if (childTopic.getNumberOfChildren() == 0) {
							childTopicTreeNode.leaf = true;
						}


						children.put(childTopicTreeNode.identifier, childTopicTreeNode);
						nodeIndex++;
					}
				}
				else if (topicList == null || (topicList != null && topicList.isEmpty())) { // possibly we will never encounter this code since setting of child nodes as leaf nodes is done when reading subtopics and is checked at the beginning of this method
					leaf = true;
				}
			} catch (Exception e) {logger.error("", e);}

		}


		return children.entrySet().iterator();
	}


	public Topic getTopic() {
		return topic;
	}


	public boolean topicMatches(String topicId){
		return topic != null && topic.getId().equals(topicId);
	}
	
	public boolean reloadTopicTreeNodeEventRaised(String topicId) {
		if (topicMatches(topicId)){
			reloadTopic();
			return true;
		}
		else{
			//Check its children
			if (! children.isEmpty()){
				Collection<TreeNode> childTopicTreeNodes = children.values();
				for (TreeNode childTopicTreeNode : childTopicTreeNodes){
					if (((LazyLoadingTopicTreeNodeRichFaces123)childTopicTreeNode).reloadTopicTreeNodeEventRaised(topicId)){
						return true;
					}
				}
			}
			return false;
		}
		
	}

	public String getSystemTaxonomyId(){
		return systemTaxonomy != null ? systemTaxonomy.getId() : null;
	}

	private void reloadTopic() {
		if (topic != null){
			TopicService topicService = (TopicService) JSFUtilities.getBeanFromSpringContext("taxonomyService");
			topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
			this.children.clear();
			this.leaf = topic.getNumberOfChildren() == 0;
			this.description = topic.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
			this.type = topic.isAllowsReferrerContentObjects()? "containerAndTopic" : "containerOnlyTopic"; 
		}
	}


	public boolean updateNoOfContentObjectReferrersEventRaised(String topicId) {
		if (topicMatches(topicId)){
			reloadNumberOfContentObjectReferrers();
			return true;
		}
		else{
			//Check its children
			if (! children.isEmpty()){
				Collection<TreeNode> childTopicTreeNodes = children.values();
				for (TreeNode childTopicTreeNode : childTopicTreeNodes){
					if (((LazyLoadingTopicTreeNodeRichFaces123)childTopicTreeNode).updateNoOfContentObjectReferrersEventRaised(topicId)){
						return true;
					}
				}
			}
			return false;
		}
		
	}


	private void reloadNumberOfContentObjectReferrers() {
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
		if (topic != null){
			if (topic.isNumberOfContentObjectsWhichReferToThisTopicLoaded()){
				reloadNumberOfContentObjectReferrers();
			}
		}
		
		if (! children.isEmpty()){
				Collection<TreeNode> childTopicTreeNodes = children.values();
				for (TreeNode childTopicTreeNode : childTopicTreeNodes){
					((LazyLoadingTopicTreeNodeRichFaces123)childTopicTreeNode).contentObjectDeletedUpdateNoOfContentObjectReferrersEventRaised();
				}
		}
	}
}
