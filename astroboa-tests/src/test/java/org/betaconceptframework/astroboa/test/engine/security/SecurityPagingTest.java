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
package org.betaconceptframework.astroboa.test.engine.security;

import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *   
 */
public class SecurityPagingTest extends AbstractRepositoryTest{

	/*
	@Test
	public void testPagingWithAndWithoutSecurity() throws Exception{

		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.addOrderProperty(CmsBuiltInItem.Order.getJcrName(), Order.ascending);
		topicCriteria.doNotCacheResults();

		topicCriteria.addCriterion(CriterionFactory.like(CmsBuiltInItem.Name.getJcrName(), "subject%"));

		checkResultsWithDisabledSecurity(topicCriteria, 0,10);
		checkResultsWithDisabledSecurity(topicCriteria, 2,5);
		checkResultsWithDisabledSecurity(topicCriteria, 11,15);
		checkResultsWithDisabledSecurity(topicCriteria, 0,0);
		checkResultsWithDisabledSecurity(topicCriteria, 0,20);
		checkResultsWithDisabledSecurity(topicCriteria, 0,21);
		checkResultsWithDisabledSecurity(topicCriteria, 19,22);
		checkResultsWithDisabledSecurity(topicCriteria, 249,249);
		checkResultsWithDisabledSecurity(topicCriteria, 30,150);
		checkResultsWithDisabledSecurity(topicCriteria, 100,50);
		checkResultsWithDisabledSecurity(topicCriteria, 2400, 99);

		enableSecurity();
		
		checkResultsWithEnabledSecurity(topicCriteria, new ArrayList<String>(), 2400,99);
		checkResultsWithEnabledSecurity(topicCriteria, new ArrayList<String>(), 0, 20);
		checkResultsWithEnabledSecurity(topicCriteria, new ArrayList<String>(), 100, 50);
		checkResultsWithEnabledSecurity(topicCriteria, Arrays.asList("5", "9"), 0, 10);
		checkResultsWithEnabledSecurity(topicCriteria, Arrays.asList("22", "23"), 20,10);
		checkResultsWithEnabledSecurity(topicCriteria, Arrays.asList("15", "19", "16", "25", "70", 
				"140"), 14, 125);
		
	}


	private void checkResultsWithDisabledSecurity(TopicCriteria topicCriteria, int offset, int limit) throws Exception {

		topicCriteria.setOffsetAndLimit(offset, limit);

		//Execute Query through CmsQueryResultSecurityHandler
		CmsQueryHandler cmsQueryHandler = AstroboaTestContext.INSTANCE.getBean(CmsQueryHandler.class, "cmsQueryHandler");
		CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = testCmsDao.createCmsQueryResultSecurityHandler(
				getSession(),
				new AstroboaAccessManager(), topicCriteria, 
				cmsQueryHandler);

		//Now perform the same query using CmsQueryHandler
		CmsQueryResult cmsQueryResultWithoutSecurityHandler = testCmsDao.createCmsQueryResultWithoutSecurity(
				getSession(), 
				topicCriteria, 
				cmsQueryHandler);


		//Both handlers should deliver exactly the same results for the provided offset and limit
		NodeIterator nodeIterator = cmsQueryResultWithoutSecurityHandler.getNodeIterator();

		Assert.assertEquals(cmsQueryResultWithoutSecurityHandler.getTotalRowCount(),
				cmsQueryResultSecurityHandler.getSize(), 
		"Number of total results do not match");

		int index = offset;
		
		int numberOfResultsReturned = 0;
		
		logger.debug("Looking for subject with offset {} and limit {}", offset, limit);
		
		while (nodeIterator.hasNext()){

			if (limit > 0){
				Assert.assertTrue(numberOfResultsReturned <= limit, "Returned more results than limit");
			}
			
			Node nextNode = nodeIterator.nextNode();

			Node nextValidNode = cmsQueryResultSecurityHandler.nextNode();
			
			Assert.assertNotNull(nextValidNode);

			//Expect to find the same topic name

			Assert.assertEquals(nextValidNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString(), 
					nextNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
			
			logger.debug("Returned subject {}", nextNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
			
			index++;
			
			Assert.assertEquals(index-offset, nodeIterator.getPosition(), " Invalid position number for JCR ?");
			Assert.assertEquals(index-offset, cmsQueryResultSecurityHandler.getPosition(), " Invalid position number for CmsQueryResultSecurity. JCR position "+ nodeIterator.getPosition());
			
			numberOfResultsReturned++;
		}

		Assert.assertFalse(cmsQueryResultSecurityHandler.hasNext(), "Query result with security handler has more nodes but it should not have");


	}

	private void checkResultsWithEnabledSecurity(TopicCriteria topicCriteria, final List<String> subjectIndecesToBeExcluded, int offset, int limit) throws Exception {

		//We ask for the first 10 valid topic nodes
		topicCriteria.setOffsetAndLimit(offset, limit);

		//Execute Query through CmsQueryResultSecurityHandler
		CmsQueryHandler cmsQueryHandler = AstroboaTestContext.INSTANCE.getBean(CmsQueryHandler.class, "cmsQueryHandler");

		//Create an access manager which denies access to topic with names
		//subject5 and subject9
		
		AstroboaAccessManager accessManager = new AstroboaAccessManager(){
			@Override
			public boolean isGranted(Node node) throws Exception {
				if (super.isGranted(node)){
					if (node.hasProperty(CmsBuiltInItem.Name.getJcrName())){
						 boolean accessIsGranted = !subjectIndecesToBeExcluded.contains(node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString().replace("subject", ""));
						 
						 logger.debug("Granting access to node {} : {}", node.getProperty(CmsBuiltInItem.Name.getJcrName()).getString(), accessIsGranted);
						 
						return accessIsGranted; 
					}

					return true;
				}

				return false;
			}
		};

		CmsQueryResultSecurityHandler cmsQueryResultSecurityHandler = testCmsDao.createCmsQueryResultSecurityHandler(
				getSession(),
				accessManager, topicCriteria, 
				cmsQueryHandler);


		int index = offset;
		
		int numberOfResultsReturned = 0;
		
		logger.debug("Looking for subject with offset {} and limit {}\nExcluded subjects \n{}", new Object[]{offset, limit, subjectIndecesToBeExcluded});
		
		while (cmsQueryResultSecurityHandler.hasNext()){
			
			if (limit>0){
				Assert.assertTrue(numberOfResultsReturned <= limit, "Returned more results than limit");
			}
			
			Node nextValidNode = cmsQueryResultSecurityHandler.nextNode();

			Assert.assertNotNull(nextValidNode);

			//Expect to find a specific topic Name
			String topicName = nextValidNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString();
			
			Assert.assertFalse(subjectIndecesToBeExcluded.contains(topicName.replace("subject", "")));
			
			logger.debug("Returned subject {}", topicName);
			
			while (subjectIndecesToBeExcluded.contains(String.valueOf(index))){
				index++;
			}
			
			Assert.assertEquals(topicName,	"subject"+index);

			index++;
			
			numberOfResultsReturned++;
			
			//Position is zero-based and always returns the index of the NEXT node
			logger.debug("Number of results returned {}, Position {}", numberOfResultsReturned, cmsQueryResultSecurityHandler.getPosition());
			
			if (cmsQueryResultSecurityHandler.hasNext()){
				Assert.assertEquals(offset+numberOfResultsReturned, cmsQueryResultSecurityHandler.getPosition()-1, " Invalid position number ");
			}
			else{
				Assert.assertEquals(offset+numberOfResultsReturned, cmsQueryResultSecurityHandler.getPosition(), " Invalid position number when no other results exist");
			}
		}
		
		
		//In access manager we denied access to several
		Assert.assertEquals(cmsQueryResultSecurityHandler.getSize(), 2500-subjectIndecesToBeExcluded.size(), 
		"Invalid Number of total results");


		Assert.assertFalse(cmsQueryResultSecurityHandler.hasNext(), "Query result with security handler has more nodes but it should not have");


	}

	@Override
	protected void customizedSetup() throws Exception {

		super.customizedSetup();

		//Create data
		RepositoryUser systemUser = TestUtils.getSystemUser(repositoryUserService);
		Taxonomy subjectTaxonomy = TestUtils.getSubjectTaxonomy(taxonomyService);

		//Create Topics
		for (int i=0;i<2500;i++){
			Topic topic = JAXBTestUtils.createTopic("subject"+i, 
					cmsRepositoryEntityFactory.newTopic(),
					cmsRepositoryEntityFactory.newRepositoryUser());
			topic.setOwner(systemUser);
			topic.setTaxonomy(subjectTaxonomy);
			topic.setOrder((long)i);

			topic = topicService.saveTopic(topic);

			addEntityToBeDeletedAfterTestIsFinished(topic);
		}
	}*/
}
