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
package org.betaconceptframework.astroboa.console.jsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;

/**
 * @author gchomatas
 *
 */
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicReview extends AbstractUIBean {
	
	// JSF Injected Beans
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private TaxonomyService taxonomyService;
	
	
	private List<HashMap<String,Object>> lastModifiedContentObjectsGroupedByTopic;
	
	public String refreshLastModifiedContentObjectsGroupedByTopicUIAction() {
		setLastModifiedContentObjectsGroupedByTopic(null);
		return null;
	}
	
	public void setLastModifiedContentObjectsGroupedByTopic(
			List<HashMap<String, Object>> lastModifiedContentObjectsGroupedByTopic) {
		this.lastModifiedContentObjectsGroupedByTopic = lastModifiedContentObjectsGroupedByTopic;
	}
	
	
	
	
	
	/* It generates a List of HashMaps. Each HashMap contains two keys: 'parentTopicLabel', 'topicGroup' and the corresponding values
	 * are the parent topic label and a list containing the subtopics of the parent topic (the topicGroup) respectively.
	 * This last list contains actually a HashMap for each topic in the list. The HashMap  has three keys: 'topicLabel', 'topidId', 'contentObjectsInTopic'. The corresponding values for each key is the label of the subtopic and the list of content objects in this subtopic 
	 * It get the rootTopics, generates the whole topic tree for each root topic and then for each tree calls the traverseTopicTree method to traverse the tree and populate the List of HashMaps
	 * It actually groups the topics according to their parent.
	 * The resulting list of HashMaps is used to generate a review of recent contentObjects published under each topic
	 */ 
	public List<HashMap<String,Object>> getLastModifiedContentObjectsGroupedByTopic() {
		if (this.lastModifiedContentObjectsGroupedByTopic == null) {
			List<HashMap<String, Object>> listOfTopicGroups = new ArrayList<HashMap<String, Object>>();
			try {
				List<Topic> rootTopics = taxonomyService.getBuiltInSubjectTaxonomy(JSFUtilities.getLocaleAsString()).getRootTopics();
				for (Topic rootTopic : rootTopics) {
					traverseTopicTree(rootTopic, listOfTopicGroups, null);
					setLastModifiedContentObjectsGroupedByTopic(listOfTopicGroups);
				}
			} catch (Exception e) {

			}
		}
		return this.lastModifiedContentObjectsGroupedByTopic;
	}
	
	
	/* 
	 * Traverses a topic tree and generates a List of HashMaps. Each HashMap contains two keys: 'parentTopicLabel', 'topicGroup' and the corresponding values
	 * are the parent topic label and a list containing the subtopics of the parent topic (the topicGroup) respectively.
	 * This last list contains actually a HashMap for each topic in the list. The HashMap  has three keys: 'topicLabel', 'topidId', 'contentObjectsInTopic'. The corresponding values for each key is the label of the subtopic and the list of content objects in this subtopic 
	 * It get the rootTopics, generates the whole topic tree for each root topic and then for each tree calls the traverseTopicTree method to traverse the tree and populate the List of HashMaps
	 */  
	private void traverseTopicTree(Topic rootTopicTree, List<HashMap<String,Object>> listOfTopicGroups, HashMap<String,Object> topicGroupHashMap) {
		if ((rootTopicTree.isAllowsReferrerContentObjects()) && topicGroupHashMap != null) { // if the topic is not a topic container only we will insert it in the list
			try {
				List<String> topicIds = new ArrayList<String>();
				topicIds.add(rootTopicTree.getId());
				
				ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
				
				// We add the selected topic into search criteria
				Criterion subjectCriterion = CriterionFactory.equals("profile.subject", rootTopicTree.getId());
				
				contentObjectCriteria.addCriterion(subjectCriterion);
				
				// return only the first five objects
				//contentObjectCriteria.getResultRowRange().setRange(0,4);
				contentObjectCriteria.setOffsetAndLimit(0,4);
				
				// run the query
				List contentObjectsInTopic = contentObjectStatefulSearchService.searchForContent(contentObjectCriteria, true, JSFUtilities.getLocaleAsString());
					
				
				if (contentObjectsInTopic != null) { 
					//	from the topicGroupHashMap we retreive the topicGroup list which contains the topics in the group
					ArrayList topicGroup = (ArrayList) topicGroupHashMap.get("topicGroup");   
					// a topic in the topicGroup list is again a HashMap which contains the label of the topic and the related content objects. So we create a new HashMap in order to insert the new topic
					HashMap topic = new HashMap<String, Object>();
					topic.put("topicLabel", rootTopicTree.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()));
					topic.put("topicId", rootTopicTree.getId());
					topic.put("contentObjectsInTopic", contentObjectsInTopic);
					topicGroup.add(topic);
				}
			}
			catch (Exception e) {
				
			}
		}
		if (rootTopicTree.getNumberOfChildren() >0) { // if the topic has subtopics we will further traverse them
			HashMap<String,Object> subTopicGroupHashMap = new HashMap<String,Object>();
			List<HashMap<String, Object>> subTopicGroup = new ArrayList();
			subTopicGroupHashMap.put("parentTopicLabel", rootTopicTree.getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString()));
			subTopicGroupHashMap.put("topicGroup", subTopicGroup);
			listOfTopicGroups.add(subTopicGroupHashMap);
			for (Topic subTopic : rootTopicTree.getChildren())
				traverseTopicTree(subTopic, listOfTopicGroups, subTopicGroupHashMap);
			// if after the subtopics traversal no subtopic has been found with contentObjects then the subTopicGroup list will have no topics inside and in this case we remove the subTopicGroupHashMap from the listOfTopicGroups
			if (subTopicGroup.size() == 0)
				listOfTopicGroups.remove(subTopicGroupHashMap);
		}
		
	}

	public TaxonomyService getTaxonomyService() {
		return taxonomyService;
	}

	public void setTaxonomyService(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	public void setContentObjectStatefulSearchService(
			ContentObjectStatefulSearchService contentObjectStatefulSearchService) {
		this.contentObjectStatefulSearchService = contentObjectStatefulSearchService;
	}

}
