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
package org.betaconceptframework.astroboa.test.engine.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TopicServiceTest extends AbstractRepositoryTest {

	enum SearchOutcome{
		ONLY_EXPECTED_TOPIC,
		AT_LEAST_EXPECTED_TOPIC,
		NO_EXPECTED_TOPIC_AT_ALL
	}
	
	
	/*
	 * Test for http://jira.betaconceptframework.org/browse/ASTROBOA-144
	 */
	@Test
	public void testTopicUpdateWithoutName(){

		String topicName = "test-topic-update-without-name";
		
		Topic topic = JAXBTestUtils.createTopic(topicName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),getSystemUser());
		
		topic.getLocalizedLabels().clear();
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);

		//Remove name and save again
		topic.setName(null);
		
		topic = topicService.save(topic);
		Assert.assertEquals(topic.getName(), topicName, "Invalid topic name");
		
		//Save using XML and JSON
		topic = topicService.save(topic.xml(false));
		Assert.assertEquals(topic.getName(), topicName, "Invalid topic name");
		
		topic = topicService.save(topic.json(false));
		Assert.assertEquals(topic.getName(), topicName, "Invalid topic name");
		
	}

	@Test
	public void testTopicSearchUsingSearchExpression() throws Throwable{

		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy(
				"test-search-parent-topic-using-search-expression-taxonomy", CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy.addLocalizedLabel("en", taxonomy.getName()+"-en");
		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);
		
		Topic parentTopic = JAXBTestUtils.createTopic("test-search-parent-topic-using-search-expression", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		parentTopic.addLocalizedLabel("en", parentTopic.getName()+"-en");
		parentTopic.setTaxonomy(taxonomy);
		parentTopic = topicService.save(parentTopic);

		Topic topic = JAXBTestUtils.createTopic("test-search-topic-using-search-expression", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		final String english_label = topic.getName()+"-en";
		
		topic.addLocalizedLabel("en", english_label);
		topic.setParent(parentTopic);
		topic.setTaxonomy(taxonomy);
		topic = topicService.save(topic);
		
		//Create criteria
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		
		//value is the expected outcome. true for match one topic, false for no match for this topic (it may contain other topics but not
		//the provided one
		Map<String, SearchOutcome> idRestrictions = new HashMap<String, SearchOutcome>();
		idRestrictions.put("id=\""+topic.getId()+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		idRestrictions.put("id!=\""+topic.getId()+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		Map<String, SearchOutcome> nameRestrictions = new HashMap<String, SearchOutcome>();
		nameRestrictions.put("name=\""+topic.getName()+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		nameRestrictions.put("name!=\""+topic.getName()+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		nameRestrictions.put("name CONTAINS \"test search topic using*\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		nameRestrictions.put("name CONTAINS \"test search2*\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		nameRestrictions.put("name%%\"test-search-topic-using%\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		nameRestrictions.put("name%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		Map<String, SearchOutcome> taxonomyRestrictions = new HashMap<String, SearchOutcome>();
		taxonomyRestrictions.put("taxonomy=\""+topic.getTaxonomy().getName()+"\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC); //We have 2 topics under this taxonomy
		taxonomyRestrictions.put("taxonomy!=\""+topic.getName()+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		Map<String, SearchOutcome> labelRestrictions = new HashMap<String, SearchOutcome>();
		labelRestrictions.put("label=\""+escapeSearchExpressionForContains(topic.getLocalizedLabelForLocale("en"))+"\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		labelRestrictions.put("label!=\""+topic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		labelRestrictions.put("label CONTAINS \"test search topic using*\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		labelRestrictions.put("label CONTAINS \"test search2*\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		labelRestrictions.put("label%%\"test-search-topic-using%\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		labelRestrictions.put("label%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		labelRestrictions.put("label.en=\""+topic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		labelRestrictions.put("label.en!=\""+topic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		labelRestrictions.put("label.en CONTAINS \"test search topic using*\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		labelRestrictions.put("label.en CONTAINS \"test search2*\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		labelRestrictions.put("label.en%%\"test-search-topic-using%\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		labelRestrictions.put("label.en%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		Map<String, SearchOutcome> parentIdRestrictions = new HashMap<String, SearchOutcome>();
		parentIdRestrictions.put("ancestor.id=\""+parentTopic.getId()+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentIdRestrictions.put("ancestor.id!=\""+parentTopic.getId()+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		Map<String, SearchOutcome> parentNameRestrictions = new HashMap<String, SearchOutcome>();
		parentNameRestrictions.put("ancestor.name=\""+parentTopic.getName()+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentNameRestrictions.put("ancestor.name!=\""+parentTopic.getName()+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentNameRestrictions.put("ancestor.name CONTAINS \"test search parent*\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentNameRestrictions.put("ancestor.name CONTAINS \"test search2\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentNameRestrictions.put("ancestor.name%%\"test-search-parent%\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		parentNameRestrictions.put("ancestor.name%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		
		Map<String, SearchOutcome> parentLabelRestrictions = new HashMap<String, SearchOutcome>();
		parentLabelRestrictions.put("ancestor.label=\""+parentTopic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label!=\""+parentTopic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentLabelRestrictions.put("ancestor.label CONTAINS \"test search parent*\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label CONTAINS \"test search2 *\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentLabelRestrictions.put("ancestor.label%%\"test-search-parent%\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentLabelRestrictions.put("ancestor.label.en=\""+parentTopic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label.en!=\""+parentTopic.getLocalizedLabelForLocale("en")+"\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentLabelRestrictions.put("ancestor.label.en CONTAINS \"test search parent*\"", SearchOutcome.ONLY_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label.en CONTAINS \"test search2*\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);
		parentLabelRestrictions.put("ancestor.label.en%%\"test-search-parent%\"", SearchOutcome.AT_LEAST_EXPECTED_TOPIC);
		parentLabelRestrictions.put("ancestor.label.en%%\"test-search-topic-using2%\"", SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL);

		//Search by its id
		for (Entry<String, SearchOutcome> idRestrictionEntry: idRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, idRestrictionEntry.getKey(), idRestrictionEntry.getValue());
		}

		//Search by its name
		for (Entry<String, SearchOutcome> nameRestrictionEntry: nameRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, nameRestrictionEntry.getKey(), nameRestrictionEntry.getValue());
		}

		//Search by its taxonomy
		for (Entry<String, SearchOutcome> restrictionEntry: taxonomyRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, restrictionEntry.getKey(), restrictionEntry.getValue());
		}

		//Search by its label
		for (Entry<String, SearchOutcome> restrictionEntry: labelRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, restrictionEntry.getKey(), restrictionEntry.getValue());
		}

		//Search by its parent id
		for (Entry<String, SearchOutcome> restrictionEntry: parentIdRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, restrictionEntry.getKey(), restrictionEntry.getValue());
		}

		//Search by its parent name
		for (Entry<String, SearchOutcome> restrictionEntry: parentNameRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, restrictionEntry.getKey(), restrictionEntry.getValue());
		}

		//Search by its parent label
		for (Entry<String, SearchOutcome> restrictionEntry: parentLabelRestrictions.entrySet()){
			assertTopicOutcome(topicCriteria, topic, restrictionEntry.getKey(), restrictionEntry.getValue());
		}

		//Search by its name and id
		assertCombinedRestrictions(topic, topicCriteria, idRestrictions,	nameRestrictions);

		//Search by its name and label
		assertCombinedRestrictions(topic, topicCriteria, labelRestrictions,	nameRestrictions);

		//Search by its name and taxonomy
		assertCombinedRestrictions(topic, topicCriteria, taxonomyRestrictions,	nameRestrictions);

		//Search by its name and parent id
		assertCombinedRestrictions(topic, topicCriteria, nameRestrictions,parentIdRestrictions);

		//Search by its name and parent name
		assertCombinedRestrictions(topic, topicCriteria, nameRestrictions,parentNameRestrictions);
		
		//Search by its name and parent label
		assertCombinedRestrictions(topic, topicCriteria, nameRestrictions,parentLabelRestrictions);

		//Search by its label and id
		assertCombinedRestrictions(topic, topicCriteria, idRestrictions, labelRestrictions);

		//Search by its label and taxonomy
		assertCombinedRestrictions(topic, topicCriteria, taxonomyRestrictions, labelRestrictions);

		//Search by its label and parent id
		assertCombinedRestrictions(topic, topicCriteria, parentIdRestrictions, labelRestrictions);

		//Search by its label and parent name
		assertCombinedRestrictions(topic, topicCriteria, parentNameRestrictions, labelRestrictions);

		//Search by its label and parent label
		assertCombinedRestrictions(topic, topicCriteria, parentLabelRestrictions, labelRestrictions);

		//Search by its taxonomy and id
		assertCombinedRestrictions(topic, topicCriteria, idRestrictions, taxonomyRestrictions);

		//Search by its parent name and id
		assertCombinedRestrictions(topic, topicCriteria, idRestrictions, parentNameRestrictions);

		//Search by its parent name and parent id
		assertCombinedRestrictions(topic, topicCriteria, parentIdRestrictions, parentNameRestrictions);

		//Search by its parent name and parent label
		assertCombinedRestrictions(topic, topicCriteria, parentLabelRestrictions, parentNameRestrictions);

		//Search by its parent label and id
		assertCombinedRestrictions(topic, topicCriteria, parentLabelRestrictions, idRestrictions);

		//Search by its name using OR
		
		//Search by its label using OR
		
		//Search by its label in different locales using OR
		
	}

	private void assertCombinedRestrictions(Topic topic,
			TopicCriteria topicCriteria,
			Map<String, SearchOutcome> firstRestrictionMap,
			Map<String, SearchOutcome> secondRestrictionMap) throws Throwable {
		
		for (Entry<String, SearchOutcome> firstRestrictionEntry: secondRestrictionMap.entrySet()){
			for (Entry<String, SearchOutcome> secondRestrictionEntry: firstRestrictionMap.entrySet()){
				
				String expression = firstRestrictionEntry.getKey() + " AND "+ secondRestrictionEntry.getKey();

				SearchOutcome searchOutcome = null;
				
				if (firstRestrictionEntry.getValue() == SearchOutcome.AT_LEAST_EXPECTED_TOPIC){
					if (secondRestrictionEntry.getValue() == SearchOutcome.AT_LEAST_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.AT_LEAST_EXPECTED_TOPIC;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL){
						searchOutcome = SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.ONLY_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.AT_LEAST_EXPECTED_TOPIC;
					}
				}
				else if (firstRestrictionEntry.getValue() == SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL){
					if (secondRestrictionEntry.getValue() == SearchOutcome.AT_LEAST_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL){
						searchOutcome = SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.ONLY_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL;
					}
				}
				if (firstRestrictionEntry.getValue() == SearchOutcome.ONLY_EXPECTED_TOPIC){
					if (secondRestrictionEntry.getValue() == SearchOutcome.AT_LEAST_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.AT_LEAST_EXPECTED_TOPIC;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL){
						searchOutcome = SearchOutcome.NO_EXPECTED_TOPIC_AT_ALL;
					}
					else if (secondRestrictionEntry.getValue() == SearchOutcome.ONLY_EXPECTED_TOPIC){
						searchOutcome = SearchOutcome.ONLY_EXPECTED_TOPIC;
					}
				}
				
				
				assertTopicOutcome(topicCriteria, topic, expression, searchOutcome);
			}
		}
	}
	
	private void assertTopicOutcome(TopicCriteria topicCriteria, Topic topic, String expression, SearchOutcome searchOutcome) throws Throwable{
		
		try{

			topicCriteria.reset();

			CriterionFactory.parse(expression, topicCriteria);

			CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

			if (SearchOutcome.ONLY_EXPECTED_TOPIC == searchOutcome){
				Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count."+ printOutcome(outcome));

				Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");
			}
			else{
				
				//At least the provided topic must exist
				boolean providedTopicFound = false;
				
				if (outcome.getCount() > 0){
					for (Topic topicResult : outcome.getResults()){
						if (StringUtils.equals(topicResult.getId(), topic.getId())){
							providedTopicFound = true;
						}
					}
				}
				
				if (SearchOutcome.AT_LEAST_EXPECTED_TOPIC == searchOutcome){
					Assert.assertTrue(providedTopicFound, "Invalid topic outcome. Did not find topic "+topic + " in the results");
				}
				else{
					Assert.assertFalse(providedTopicFound, "Invalid topic outcome. Found topic "+topic + " in the results");
				}
			}
		}		
		catch(Throwable t){
			throw new Throwable("Expression " + expression + ", XPath "+topicCriteria.getXPathQuery(), t);
		}
	}
	
	private String printOutcome(CmsOutcome<Topic> outcome) {
		StringBuilder sb = new StringBuilder();
		
		for (Topic topic: outcome.getResults()){
			sb.append(topic.xml(true));
			sb.append("\n");
		}
		
		return sb.toString();
	}

	@Test
	public void testSearchTopicByLocalizedLabel(){
		
		Topic topic = JAXBTestUtils.createTopic("test-search-topic-by-localized-label", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		final String english_label = topic.getName()+"-en2";
		
		topic.addLocalizedLabel("en", english_label);

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 3);
		
		//Search any locale
		LocalizationCriterion locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("*en2");
		locLabelCriterion.setQueryOperator(QueryOperator.CONTAINS);
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("SAVVAS XPATH {}",topicCriteria.getXPathQuery());
		
		CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");
		
		//Search specific locale
		topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 1);
		
		locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("%en2");
		locLabelCriterion.setLocale("en");
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("XPATH {}",topicCriteria.getXPathQuery());
		
		outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");

		//Search wrong locale
		topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 1);
		
		locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("%en2");
		locLabelCriterion.setLocale("fn");
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("XPATH {}",topicCriteria.getXPathQuery());
		
		outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 0, "Invalid topic outcome count");
		
	}
	
	@Test
	public void testChangeTopicTaxonomy(){
		
		Topic topic = JAXBTestUtils.createTopic("test-change-topic-taxonomy", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertEquals(topicReloaded.getTaxonomy().getName(),getSubjectTaxonomy().getName(),  "Topic "+topic.getName() + " was not saved under default taxonomy "+Taxonomy.SUBJECT_TAXONOMY_NAME);
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomy-used-in-change-taxonomy-test", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);
		
		topicReloaded.setTaxonomy(taxonomy);
		topicService.save(topicReloaded);
		
		topicReloaded = topicService.getTopic(topicReloaded.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertEquals(topicReloaded.getTaxonomy().getName(),taxonomy.getName(),  "Topic "+topic.getName() + " was not saved under default taxonomy "+Taxonomy.SUBJECT_TAXONOMY_NAME);


	}
	
	@Test
	public void testChangeTopicParentWhichBelongsToAnotherTaxonomy(){

		Topic parentTopic = JAXBTestUtils.createTopic("test-parent-change-topic-taxonomy-using-parent", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		parentTopic = topicService.save(parentTopic);
		addEntityToBeDeletedAfterTestIsFinished(parentTopic);
		
		Topic topic = JAXBTestUtils.createTopic("test-change-topic-taxonomy-using-parent", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		topic.setParent(parentTopic);
		topic = topicService.save(topic);
		
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertEquals(topicReloaded.getTaxonomy().getName(),getSubjectTaxonomy().getName(),  "Topic "+topic.getName() + " was not saved under default taxonomy "+Taxonomy.SUBJECT_TAXONOMY_NAME);
		
		Taxonomy taxonomy = JAXBTestUtils.createTaxonomy("taxonomy-used-in-change-taxonomy-test-using-parent", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy());
		
		taxonomy = taxonomyService.save(taxonomy);
		addEntityToBeDeletedAfterTestIsFinished(taxonomy);

		Topic secondParentTopic = JAXBTestUtils.createTopic("test-second-parent-change-topic-taxonomy-using-parent", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		secondParentTopic.setTaxonomy(taxonomy);
		secondParentTopic = topicService.save(secondParentTopic);
		
		topicReloaded.setParent(secondParentTopic);
		topicReloaded = topicService.save(topicReloaded);
		
		topicReloaded = topicService.getTopic(topicReloaded.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertEquals(topicReloaded.getTaxonomy().getName(),taxonomy.getName(),  "Topic "+topic.getName() + " was not saved under default taxonomy "+Taxonomy.SUBJECT_TAXONOMY_NAME);

	}
	
	
	@Test
	public void testDeleteTopicWhichIsUsedByAMandatoryCmsProperty() throws ItemNotFoundException, RepositoryException{
		
		Topic topic = JAXBTestUtils.createTopic("test-delete-topic-with-a-mandatory-reference", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		topic = topicService.save(topic);
		
		ContentObject contentObject = createContentObject(getSystemUser(), "test-topic-delete-mandatory-reference", false);
		((TopicReferenceProperty)contentObject.getCmsProperty("singleComplexNotAspectWithCommonAttributes.testTopic")).addSimpleTypeValue(topic);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		
		//TODO : Should we throw an exception
		//in order to prevent inconsistency ?
		topicService.deleteTopicTree(topic.getId());
		
		
		
	}
	
	@Test
	public void testDeleteTopicWithContentObjectReference() throws ItemNotFoundException, RepositoryException{
		
		Topic topic = JAXBTestUtils.createTopic("test-delete-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		topic = topicService.save(topic);
		
		ContentObject contentObject = createContentObject(getSystemUser(), "test-topic-delete-reference", false);
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		topicService.deleteTopicTree(topic.getId());
		
		//Check with Jcr
		try{
			Node topicNode = getSession().getNodeByUUID(topic.getId());
			Assert.assertNull(topicNode, "Topic "+topic.getName() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), topic.getId(), "Invalid ItemNotFoundException message");
		}
		
		//Check with Topic entity 
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertNull(topicReloaded, "Topic "+topic.getName() + " was not deleted");
		
		//Check with Astrbooa Service
		Assert.assertEquals(topicService.getCountOfContentObjectIdsWhichReferToTopic(topic.getId()), 0, "Topic "+topic.getName() + " should have been deleted ");
		Assert.assertEquals(topicService.getContentObjectIdsWhichReferToTopic(topic.getId()).size(), 0, "Topic "+topic.getName() + " should have been deleted ");
		
		//Check with content object id
		contentObject =contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		Assert.assertTrue(((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).hasNoValues(), "ContentObjct "+contentObject.getSystemName() + " contains values "+
				((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).getSimpleTypeValues()+ " but it should not have");
		
	}
	
	@Test
	public void testDeleteTopicChildWithContentObjectReference() throws ItemNotFoundException, RepositoryException{
		
		Topic parentTopic = JAXBTestUtils.createTopic("test-delete-parent-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		Topic topic = JAXBTestUtils.createTopic("test-delete-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		parentTopic.addChild(topic);
		
		parentTopic = topicService.save(parentTopic);
		
		ContentObject contentObject = createContentObject(getSystemUser(), "test-child-topic-delete-reference", false);
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		topicService.deleteTopicTree(parentTopic.getId());
		
		//Check with Jcr
		try{
			Node parentTopicNode = getSession().getNodeByUUID(parentTopic.getId());
			Assert.assertNull(parentTopicNode, "Parent Topic "+parentTopic.getName() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), parentTopic.getId(), "Invalid ItemNotFoundException message");
		}
		
		try{
			Node topicNode = getSession().getNodeByUUID(topic.getId());
			Assert.assertNull(topicNode, "Child Topic "+topic.getName() + " was not deleted");
		}
		catch(ItemNotFoundException infe){
			Assert.assertEquals(infe.getMessage(), topic.getId(), "Invalid ItemNotFoundException message");
		}
		
		//Check with Topic entity 
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertNull(topicReloaded, "Topic "+topic.getName() + " was not deleted");
		
		//Check with Astroboa Service
		Assert.assertEquals(topicService.getCountOfContentObjectIdsWhichReferToTopic(topic.getId()), 0, "Topic "+topic.getName() + " should have been deleted ");
		Assert.assertEquals(topicService.getContentObjectIdsWhichReferToTopic(topic.getId()).size(), 0, "Topic "+topic.getName() + " should have been deleted ");
		
		//Check with content object id
		contentObject =contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		Assert.assertTrue(((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).hasNoValues(), "ContentObjct "+contentObject.getSystemName() + " contains values "+
				((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).getSimpleTypeValues()+ " but it should not have");
		
	}
	
	@Test
	public void testSaveTopicWithContentObjectReference() throws ItemNotFoundException, RepositoryException{
		
		Topic topic = JAXBTestUtils.createTopic("test-save-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		ContentObject contentObject = createContentObject(getSystemUser(), "test-topic-save-reference", false);
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		//Check with Jcr
		Node topicNode = getSession().getNodeByUUID(topic.getId());
		
		Assert.assertNotNull(topicNode, "Topic "+topic.getName() + " was not saved");
		
		//Check with Topic entity 
		Topic topicReloaded = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, false);
		
		Assert.assertNotNull(topicReloaded, "Topic "+topic.getName() + " was not saved");
		
		Assert.assertNotNull(topicReloaded.getContentObjectIdsWhichReferToThisTopic(), "Topic "+topic.getName() + " was saved but reference to content object was not");

		Assert.assertEquals(topicReloaded.getNumberOfContentObjectsWhichReferToThisTopic(), 1, "Topic "+topic.getName() + " should have been saved with only 1 content object reference ");

		List<String> contentObjectReferencesList = topicReloaded.getContentObjectIdsWhichReferToThisTopic();
		
		Assert.assertTrue(contentObjectReferencesList.size() == 1 && contentObjectReferencesList.get(0).equals(contentObject.getId()), "Topic "+topic.getName() + " was saved but reference to content object is not valid."
				+ " Expected "+contentObject.getId()+ " but found "+contentObjectReferencesList.get(0));

		//Check with Astroboa Service
		contentObjectReferencesList = topicService.getContentObjectIdsWhichReferToTopic(topicReloaded.getId());
		Assert.assertTrue(contentObjectReferencesList.size() == 1 && contentObjectReferencesList.get(0).equals(contentObject.getId()), "Topic "+topic.getName() + " was saved but reference to content object is not valid."
				+ " Expected "+contentObject.getId()+ " but found "+contentObjectReferencesList);

		Assert.assertEquals(topicService.getCountOfContentObjectIdsWhichReferToTopic(topicReloaded.getId()), 1, "Topic "+topic.getName() + " should have been saved with only 1 content object reference ");
		
	}
	
	@Test
	public void testCheckFlatTaxonomyQuery(){
		
		TopicCriteria rootTopicCriteria = CmsCriteriaFactory.newTopicCriteria();
		rootTopicCriteria.addTaxonomyNameEqualsCriterion(Taxonomy.SUBJECT_TAXONOMY_NAME);
		rootTopicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		rootTopicCriteria.searchInDirectAncestorOnly();
		rootTopicCriteria.addCriterion(CriterionFactory.isNotNull(CmsBuiltInItem.Name.getJcrName()));

		
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.searchInDirectAncestorOnly();
		topicCriteria.setOffsetAndLimit(0, 0);
		topicCriteria.setAncestorCriteria(rootTopicCriteria);
		
		logger.debug("XPATH "+topicCriteria.getXPathQuery());
		
		CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		logger.debug(String.valueOf(outcome.getCount()));
		
		for (Topic topic : outcome.getResults()){
			logger.debug("{} {}", topic.getName(), topic.getParent().getName());
		}
		
		String query ="bccms:system/bccms:taxonomyRoot/bccms:subjectTaxonomy/bccms:topic/element ( *,bccms:topic )";
		
		TopicCriteria topic2Criteria = CmsCriteriaFactory.newTopicCriteria();
		topic2Criteria.setXPathQuery(query);
		
		CmsOutcome<Topic> outcome2 = topicService.searchTopics(topic2Criteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), outcome2.getCount(), "Problem in flat taxonomy query");
		
	}
	
	@Test
	public void testSaveSiblingTopicsWithSameName(){
		
		RepositoryUser systemUser = getSystemUser();
		
		Topic parent = JAXBTestUtils.createTopic("parent-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		//
		Topic topic = JAXBTestUtils.createTopic("same-name-sibling", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);

		Topic topic2 = JAXBTestUtils.createTopic("same-name-sibling", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);

		
		parent.addChild(topic);
		parent.addChild(topic2);
	
		try{
			topicService.save(parent);
			Assert.assertEquals(1,2, "An exception should be thrown with same name siblings");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic name 'same-name-sibling' already exists. Probably you are importing many  topics at once and you have provided more than one topic with name same-name-sibling"),"Invalid exception message "+e.getMessage());
		}
		
		//Use the same instance
		parent.getChildren().clear();
		parent.addChild(topic);
		parent.addChild(topic);
		
		try{
			topicService.save(parent);
			Assert.assertEquals(1,2, "An exception should be thrown with same name siblings");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic name 'same-name-sibling' already exists. Probably you are importing many  topics at once and you have provided more than one topic with name same-name-sibling"),"Invalid exception message "+e.getMessage());
		}
	}
	
		
	@Test
	public void testDetectCycle(){
		
		RepositoryUser systemUser = getSystemUser();
		
		//
		Topic topic = JAXBTestUtils.createTopic("detect-cycle", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		//Grand child is the same with grand parent
		Topic neutral = JAXBTestUtils.createTopic("neutral-topic", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		topic.addChild(neutral);
		
		neutral.addChild(topic);
		
		try{
			topic = topicService.save(topic);
			Assert.assertEquals(1,2, "An exception should be thrown when topic exists more than once in the hierarchy");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic with name neutral-topic exists more than once in topic hierarchy"),"Invalid exception message "+e.getMessage());
		}
		
		//using other instances
		neutral.getChildren().clear();
		topic.setParent(null);
		
		Topic topic2 = JAXBTestUtils.createTopic("detect-cycle", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		neutral.addChild(topic2);
		
		try{
			topic = topicService.save(topic);
			Assert.assertEquals(1,2, "An exception should be thrown when topic exists more than once in the hierarchy");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic with name detect-cycle exists more than once in topic hierarchy"), "Invalid exception message "+e.getMessage());
		}
		
	}

	@Test
	public void testAddTopicParentItSelf(){
		
		RepositoryUser systemUser = getSystemUser();
		

		Topic topic = JAXBTestUtils.createTopic("add-parent-topic-itself", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		try{
			topic.setParent(topic);
			Assert.assertEquals(1,2, "An exception should be thrown when topic adds its self as a parent");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic null : add-parent-topic-itself cannot have itself as a parent"),"Invalid exception message "+e.getMessage());
		}
		
		
		//Another instance
		Topic parentTopic = JAXBTestUtils.createTopic("add-parent-topic-itself", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);

		try{
			topic.setParent(parentTopic); 
			Assert.assertEquals(1,2, "An exception should be thrown when topic adds its self as a parent");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic null : add-parent-topic-itself cannot have itself as a parent"),"Invalid exception message "+e.getMessage());
		}
		
	}
	
	@Test
	public void testAddTopicChildItSelf(){
		
		RepositoryUser systemUser = getSystemUser();
		
		//Topic has child its self  
		Topic topic = JAXBTestUtils.createTopic("add-child-topic-itself", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		try{
			topic.addChild(topic); 
			Assert.assertEquals(1,2, "An exception should be thrown when topic adds its self as a child");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic null : add-child-topic-itself cannot have itself as a child"),"Invalid exception message "+e.getMessage());
			
		}
		
		//Use another instance
		Topic childTopic = JAXBTestUtils.createTopic("add-child-topic-itself", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				systemUser);
		
		try{
			topic.addChild(childTopic);
			Assert.assertEquals(1,2, "An exception should be thrown when topic adds its self as a child");
		}
		catch(Exception e){
			Assert.assertTrue(e.getMessage().contains("Topic null : add-child-topic-itself cannot have itself as a child"),"Invalid exception message "+e.getMessage());
			
		}
		
	}
	
	@Test

	public void testSearchTopicByLocalizedLabelAndIgnore(){
		
		Topic topic = JAXBTestUtils.createTopic("test-SEARCH-topic-by-lOcalIzed-label-IGNORE-case", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		
		final String english_label = topic.getName()+"-en";
		
		topic.addLocalizedLabel("en", english_label);
		topic.addLocalizedLabel("el", "Όρος ΘΗσαυρού");

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Search specific locale, ignore case and use LIKE operator
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 1);
		
		LocalizationCriterion locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("%topiC-By-loCalized-LABEL-IGNORE-case-en");
		locLabelCriterion.setLocale("en");
		locLabelCriterion.ignoreCaseInLabels();
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("XPATH {}",topicCriteria.getXPathQuery());
		
		CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");
		
		//Search specific locale, ignore case and use EQUALS operator - en locale
		topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 1);
		
		locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("test-SEARCH-topic-BY-LocalIzed-labeL-iGNoRe-CASE-en");
		locLabelCriterion.setLocale("en");
		locLabelCriterion.ignoreCaseInLabels();
		locLabelCriterion.setQueryOperator(QueryOperator.EQUALS);
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("XPATH {}",topicCriteria.getXPathQuery());
		
		outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");

		//Search specific locale, ignore case and use EQUALS operator - el locale
		topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		topicCriteria.setOffsetAndLimit(0, 1);
		
		locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel("ΌΡΟΣ ΘΗΣΑΥΡΟΎ");
		locLabelCriterion.setLocale("el");
		locLabelCriterion.ignoreCaseInLabels();
		locLabelCriterion.setQueryOperator(QueryOperator.EQUALS);
		
		topicCriteria.addCriterion(locLabelCriterion);
		
		logger.debug("XPATH {}",topicCriteria.getXPathQuery());
		
		outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
		
		Assert.assertEquals(outcome.getCount(), 1, "Invalid topic outcome count");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "Invalid topic outcome ");
	}
	
	@Test
	public void testTopicSaveInOwnerFolksonomy(){
		
		//Create content objects for test
		RepositoryUser testUser = repositoryUserService.getRepositoryUser(TestConstants.TEST_USER_NAME);
		
		Topic topic = JAXBTestUtils.createTopic("topicFolksonomyNameWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				testUser);
		
		//Provide valid system name
		topic.setName("validFolksonomyTopicOwnerDifferentThanSystem");
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstFolksonomyChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(childTopic1);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondFolksonomyChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(secondTopic);

		Topic thirdTopic = JAXBTestUtils.createTopic("thirdFolksonomyChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(thirdTopic);

		testUser.getFolksonomy().addRootTopic(topic);
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Now retrieve topic
		checkOwnerIsSystemUser(topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false), testUser);
		
		

	}
	
	@Test
	public void testSaveWithOwnerOtherThanSystem(){
		
		//Create content objects for test
		RepositoryUser testUser = repositoryUserService.getRepositoryUser(TestConstants.TEST_USER_NAME);
		
		Topic topic = JAXBTestUtils.createTopic("topicNameWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				testUser);
		topic.setTaxonomy(getSubjectTaxonomy());
		
		//Provide valid system name
		topic.setName("validSystemNameOwnerDifferentThanSystem");
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(childTopic1);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(secondTopic);

		Topic thirdTopic = JAXBTestUtils.createTopic("thirdChildWithOwnerDifferentFromSystem", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		topic.addChild(thirdTopic);
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Now retrieve topic
		checkOwnerIsSystemUser(topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false), getSystemUser());
		
	}
	
	@Test
	public void testSaveWithSystemUserAsOwner(){
		
		RepositoryUser testUser = repositoryUserService.getRepositoryUser(TestConstants.TEST_USER_NAME);
		
		Topic topic = JAXBTestUtils.createTopic("topicNameWithSystemUserOwner", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		topic.setTaxonomy(getSubjectTaxonomy());
		
		//Provide valid system name
		topic.setName("validSystemNameWithSystemUserOwner");
		
		Topic childTopic1 = JAXBTestUtils.createTopic("firstChildWithSystemUserOwner", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		topic.addChild(childTopic1);
		
		Topic secondTopic = JAXBTestUtils.createTopic("secondChildWithSystemUserOwner", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		
		topic.addChild(secondTopic);
		
		secondTopic.setOwner(testUser); //Change owner to see if this is changed

		Topic thirdTopic = JAXBTestUtils.createTopic("thirdChildWithSystemUserOwner", 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				topic.getOwner());
		
		topic.addChild(thirdTopic);
		
		thirdTopic.setOwner(testUser); //Change owner to see if this is changed
		
		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);
		
		//Now retrieve topic
		checkOwnerIsSystemUser(topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false), getSystemUser());
		
	}
	
	
	private void checkOwnerIsSystemUser(Topic topic, RepositoryUser repositoryUser) {
		
		Assert.assertEquals(topic.getOwner().getId(), repositoryUser.getId(), "Topic was saved with owner "+topic.getOwner().getExternalId()+" different than user "+ repositoryUser.getExternalId());
		Assert.assertEquals(topic.getOwner().getExternalId(), repositoryUser.getExternalId(), "Topic was saved with owner "+topic.getOwner().getExternalId()+" different than user "+ repositoryUser.getExternalId());
		Assert.assertEquals(topic.getOwner().getLabel(), repositoryUser.getLabel(), "Topic was saved with owner "+topic.getOwner().getExternalId()+" different than user "+ repositoryUser.getExternalId());
		
		if (topic.getNumberOfChildren() > 0){
			for (Topic child : topic.getChildren()){
				checkOwnerIsSystemUser(child, repositoryUser);
			}
		}
	}

    @Test
	public void testGetTopicAsTopicOutcome() throws Throwable{

		Topic topic =  createRootTopicForSubjectTaxonomy("topicTestExportAsTopicOutcome");
		
		CmsOutcome<Topic> outcome = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_LIST, FetchLevel.ENTITY, false);
		
		Assert.assertNotNull(outcome, "TopicService.getTopic returned null with Outcome returned type");
		
		Assert.assertEquals(outcome.getCount(), 1, "TopicService.getTopic returned invalid count with Outcome returned type");
		Assert.assertEquals(outcome.getLimit(), 1, "TopicService.getTopic returned invalid limit with Outcome returned type");
		Assert.assertEquals(outcome.getOffset(), 0, "TopicService.getTopic returned invalid offset with Outcome returned type");
		
		
		Assert.assertEquals(outcome.getResults().size(), 1, "TopicService.getTopic returned invalid number of Topics with Outcome returned type");
		
		Assert.assertEquals(outcome.getResults().get(0).getId(), topic.getId(), "TopicService.getTopic returned invalid topic with Outcome returned type");
	}
	
	@Test
	public void testGetTopicXmlorJSON() throws Throwable{
		
		Topic topic =  createRootTopicForSubjectTaxonomy("topicTestExportXmlJSON");
		Topic childTopic = createTopic("topicTestExportXMLJSONChild", topic);
		Topic grandChildTopic = createTopic("grandChildTopicTestExportXMLJSONChild", childTopic);

		String topicXml = null;
		String topicXmlFromServiceUsingId = null;

		List<ResourceRepresentationType<String>> outputs = Arrays.asList(ResourceRepresentationType.JSON, ResourceRepresentationType.XML);
		
		try{
			
			for (ResourceRepresentationType<String> output : outputs){
				//Reload topic without its children
				topic = topicService.getTopic(topic.getId(), ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);

				//	First check export of topic only
				if (output.equals(ResourceRepresentationType.XML)){
					topicXml = topic.xml(prettyPrint);
					topicXmlFromServiceUsingId = topicService.getTopic(topic.getId(), ResourceRepresentationType.XML, FetchLevel.ENTITY, prettyPrint);
				}
				else{
					topicXml = topic.json(prettyPrint);
					topicXmlFromServiceUsingId = topicService.getTopic(topic.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY, prettyPrint);
				}
				
				Topic topicFromServiceWithId = importDao.importTopic(topicXmlFromServiceUsingId, ImportMode.DO_NOT_SAVE);  

				repositoryContentValidator.compareTopics(topic, topicFromServiceWithId, false, true);

				//Now check export of topic children
				topic.getChildren();
				if (output.equals(ResourceRepresentationType.XML)){
					topicXml = topic.xml(prettyPrint);
					topicXmlFromServiceUsingId = topicService.getTopic(topic.getId(), ResourceRepresentationType.XML, FetchLevel.FULL, prettyPrint);
				}
				else{
					topicXml = topic.json(prettyPrint);
					topicXmlFromServiceUsingId = topicService.getTopic(topic.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL,prettyPrint);
				}

				topicFromServiceWithId = importDao.importTopic(topicXmlFromServiceUsingId, ImportMode.DO_NOT_SAVE); 

				repositoryContentValidator.compareTopics(topic, topicFromServiceWithId, true, true);
			
			}			
		}
		catch(Throwable e){
			logger.error("Initial \n{}",TestUtils.prettyPrintXml(topicXml));
			logger.error("Using Id \n{}",TestUtils.prettyPrintXml(topicXmlFromServiceUsingId));
			throw e;
		}	
	}
	
	
	@Test
	public void testSaveWithVariousNames(){
		
		//Create content objects for test
		Topic topic = createRootTopicForSubjectTaxonomy("topicValidName");
		
		
		//Now provide invalid system name
		checkInvalidSystemNameSave(topic, "invalid)SystemName");
		checkInvalidSystemNameSave(topic, "invalid((SystemName");
		checkInvalidSystemNameSave(topic, "invalid)SystemNa&me");
		checkInvalidSystemNameSave(topic, "ςδςδ");
		checkInvalidSystemNameSave(topic, "invaliδName+");
		
		checkValidSystemNameSave(topic, "09092");
		checkValidSystemNameSave(topic, "09sasas");
		checkValidSystemNameSave(topic, "09_sdds-02");
		checkValidSystemNameSave(topic, "----");
		checkValidSystemNameSave(topic, "____");
		checkValidSystemNameSave(topic, "sdsds");
		checkValidSystemNameSave(topic, "090..92");
		checkValidSystemNameSave(topic, "090.92");
		checkValidSystemNameSave(topic, "090..__--92");
		checkValidSystemNameSave(topic, "090..92");

	}

	private void checkInvalidSystemNameSave(Topic topic,
			String systemName) {
		
		try{
			topic.setName(systemName);
			
			topic = topicService.save(topic);
			
			
			Assert.assertEquals(1, 2, 
					"Topic was saved with invalid system name "+systemName);
			
		}
		catch(CmsException e){
		
			String message = e.getMessage();
			
			Throwable t = e;
			
			while (t.getCause() != null){
				message = t.getCause().getMessage();
				
				t = t.getCause();
			}
			
			Assert.assertEquals(message, "Topic name '"+systemName+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP, 
					"Invalid exception "+ e.getMessage());
		}
	}
	
	private void checkValidSystemNameSave(Topic topic,
			String systemName) {
		
		topic.setName(systemName);
			
		topic = topicService.save(topic);
			
	}
	

	private String escapeSearchExpressionForContains(String searchExpression){
		
	  return searchExpression.replaceAll("-", " ");
	}

}
