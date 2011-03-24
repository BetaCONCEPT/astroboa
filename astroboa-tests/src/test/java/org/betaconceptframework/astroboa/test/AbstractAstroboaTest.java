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
package org.betaconceptframework.astroboa.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.api.service.SpaceService;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class AbstractAstroboaTest {
	
	protected TaxonomyService taxonomyService;
	protected RepositoryService repositoryService;
	protected TopicService topicService;
	protected SpaceService spaceService;
	protected DefinitionService definitionService;
	protected ContentService contentService;
	protected IdentityStore identityStore;
	protected SerializationService serializationService;
	protected ImportService importService;
	
	
	protected RepositoryUserService repositoryUserService;
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected String authenticationToken;
	
	private RepositoryUser systemUser;
	private List<CmsRepositoryEntity> entitiesToBeDeleted = new ArrayList<CmsRepositoryEntity>();
	private Taxonomy subjectTaxonomy;
	
	@BeforeClass
	public void setup() throws Exception{
		
		logger.info("Starting Test {}", this.getClass().getSimpleName());

		preSetup();
		
		//Load services used by this test
		repositoryService = AstroboaTestContext.INSTANCE.getRepositoryService();
		taxonomyService = AstroboaTestContext.INSTANCE.getTaxonomyService();
		repositoryUserService = AstroboaTestContext.INSTANCE.getRepositoryUserService();
		topicService = AstroboaTestContext.INSTANCE.getTopicService();
		definitionService = AstroboaTestContext.INSTANCE.getDefinitionService();
		contentService = AstroboaTestContext.INSTANCE.getContentService();
		spaceService = AstroboaTestContext.INSTANCE.getSpaceService();
		identityStore = AstroboaTestContext.INSTANCE.getIdentityStore();
		serializationService = AstroboaTestContext.INSTANCE.getSerialzationService();
		importService = AstroboaTestContext.INSTANCE.getImportService();
		
		//Connect to test repository
		loginToTestRepositoryAsSystem();
		
		postSetup();
		
	}


	protected RepositoryUser getSystemUser()
	{
		if (systemUser == null)
		{
			systemUser =  repositoryUserService.getSystemRepositoryUser();
		}
		
		return systemUser;
	}
	
	protected Taxonomy getSubjectTaxonomy(){
		
		if (subjectTaxonomy == null)
		{
			subjectTaxonomy = taxonomyService.getBuiltInSubjectTaxonomy("en");
		}
		
		return subjectTaxonomy;
	}

	
	protected void loginToTestRepositoryAsSystem()
	{
		loginToTestRepository(IdentityPrincipal.SYSTEM, "secretSystemKey", true);
	}
	
	protected void loginToCloneRepositoryAsSystem()
	{
		authenticationToken = repositoryService.login(TestConstants.TEST_CLONE_REPOSITORY_ID, 
				IdentityPrincipal.SYSTEM, "secretSystemKey");
		
		logger.info("User SYSTEM successfully logged in with authenticatin token "+authenticationToken);

	}

	
	protected void loginToTestRepositoryAsAnonymous()
	{
		authenticationToken = repositoryService.loginAsAnonymous(TestConstants.TEST_REPOSITORY_ID, "specialKey");
		
		logger.debug("User ANONYMOUS successfully logged in with authenticatin token {}",authenticationToken);

		systemUser = null;
		subjectTaxonomy = null;
	}
	
	protected void loginToTestRepositoryAsTestUser()
	{
		loginToTestRepository(TestConstants.TEST_USER_NAME, "secretTestKey", true);

	}
	
	protected void loginToTestRepository(String username, String password, boolean useSecretKey) {
		
		loginToRepository(TestConstants.TEST_REPOSITORY_ID, username, password, useSecretKey);

	}
	
	protected void loginToRepository(String repositoryId, String username, String password, boolean useSecretKey) {
		
		if(repositoryService == null){
			//Due to an unknown bug reference to Spring Bean 'repositoryService' is null
			//it should never be null. Try to load once more the bean from Spring context
			//and issue a warning if this still null.
			repositoryService = AstroboaTestContext.INSTANCE.getRepositoryService();
			if (repositoryService == null){
				logger.warn("Found no repositoryService spring bean. Unable to login to repository "+
					repositoryId);
				systemUser = null;
				subjectTaxonomy = null;
				return;
			}
		}
		
		if (useSecretKey){
			authenticationToken = repositoryService.login(repositoryId, username, password);
		}
		else{
			authenticationToken = repositoryService.login(repositoryId, 
					new AstroboaCredentials(username, (password ==null ? "".toCharArray(): password.toCharArray())));
		}
		
		logger.debug("User {} Successfully logged in repository {} with authenticatin token {}", 
				new Object[]{username, repositoryId, authenticationToken});

		systemUser = null;
		subjectTaxonomy = null;

	}
	
	protected void loginToRepositoryAsAnonymous(String repositoryId)
	{
		authenticationToken = repositoryService.loginAsAnonymous(repositoryId);
		
		logger.info("User ANONYMOUS successfully logged in with authenticatin token "+authenticationToken+ " in repository "+repositoryId);

		systemUser = null;
		subjectTaxonomy = null;
	}



	protected abstract void preSetup() throws Exception ;
	
	protected abstract void postSetup() throws Exception ;
	
	protected void addEntityToBeDeletedAfterTestIsFinished(CmsRepositoryEntity cmsRepositoryEntity){
		
		if (cmsRepositoryEntity != null){
			entitiesToBeDeleted.add(cmsRepositoryEntity);
		}
	}
	
	
	@AfterClass
	protected void cleanup() throws Exception{
		
		preCleanup();
		
		if (CollectionUtils.isNotEmpty(entitiesToBeDeleted)){
			
			loginToTestRepositoryAsSystem();

			for (CmsRepositoryEntity cmsRepositoryEntity : entitiesToBeDeleted){
				
				if (cmsRepositoryEntity instanceof Taxonomy){
					
					final Taxonomy taxonomy = (Taxonomy)cmsRepositoryEntity;
					
					if (StringUtils.equals(taxonomy.getName(), Taxonomy.SUBJECT_TAXONOMY_NAME)){
						//Subject Taxonomy cannot be deleted. Delete its children instead
						if (taxonomy.getNumberOfRootTopics() > 0){
							for (Topic topic: taxonomy.getRootTopics()){
								topicService.deleteTopicTree(topic.getId());
							}
						}
					}
					else{
						taxonomyService.deleteTaxonomyTree(taxonomy.getId());
					}
				}
				else if (cmsRepositoryEntity instanceof Topic){
					topicService.deleteTopicTree(((Topic)cmsRepositoryEntity).getId());
				}
				else if (cmsRepositoryEntity instanceof ContentObject){
					contentService.deleteContentObject(((ContentObject)cmsRepositoryEntity).getId());
				}
				else if (cmsRepositoryEntity instanceof RepositoryUser){
					repositoryUserService.deleteRepositoryUser(((RepositoryUser)cmsRepositoryEntity).getId(), null);
				}
			}
		}
		
		postCleanup();
	}


	protected abstract void postCleanup();


	protected abstract void preCleanup();
}
