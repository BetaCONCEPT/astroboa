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
package org.betaconceptframework.astroboa.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
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
import org.testng.annotations.AfterMethod;
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
	private Map<String, Class<? extends CmsRepositoryEntity>> entitiesToBeDeleted = new HashMap<String, Class<? extends CmsRepositoryEntity>>();
	private Taxonomy subjectTaxonomy;
	
	@BeforeClass
	public void setup() throws Exception{
		
		logger.debug("Starting Test {}", this.getClass().getSimpleName());

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
	
	protected void markObjectForRemoval(ContentObject object){
		if (object != null && object.getId() != null){
			markEntityForRemoval(object.getId(), ContentObject.class);
		}
	}

	protected void markTopicForRemoval(Topic topic){
		if (topic != null && topic.getId() != null){
			markEntityForRemoval(topic.getId(), Topic.class);
		}
	}

	protected void markSpaceForRemoval(Space space){
		if (space != null && space.getId() != null){
			markEntityForRemoval(space.getId(), Space.class);
		}
	}

	protected void markTaxonomyForRemoval(Taxonomy taxonomy){
		if (taxonomy != null && taxonomy.getId() != null){
			markEntityForRemoval(taxonomy.getId(), Taxonomy.class);
		}
	}

	protected void markRepositoryUserForRemoval(RepositoryUser repUser){
		if (repUser != null && repUser.getId() != null){
			markEntityForRemoval(repUser.getId(), RepositoryUser.class);
		}
	}

	private void markEntityForRemoval(String entityId, Class<? extends CmsRepositoryEntity> entityClass){
		
		if (StringUtils.isNotBlank(entityId) && entityClass != null){
			entitiesToBeDeleted.put(entityId, entityClass);
		}
	}
	
	
	@AfterMethod
	protected void cleanup() throws Exception{
		
		logger.debug("About to remove entities");
		
		preCleanup();
		
		if (MapUtils.isNotEmpty(entitiesToBeDeleted)){
			
			loginToTestRepositoryAsSystem();

			removeEntities();
			
			loginToCloneRepositoryAsSystem();
			
			removeEntities();
		}
		
		postCleanup();
		
		loginToTestRepositoryAsSystem();

	}


	private void removeEntities() {
		for (Entry<String, Class<? extends CmsRepositoryEntity>> entityToBeRemoval : entitiesToBeDeleted.entrySet()){
			
			Class<? extends CmsRepositoryEntity> entityClass = entityToBeRemoval.getValue();
			String entityIdentifier = entityToBeRemoval.getKey();
			
			if (entityClass == Taxonomy.class){
				
				final Taxonomy taxonomy = taxonomyService.getTaxonomy(entityIdentifier, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
				
				if (taxonomy == null){
					continue;
				}
				
				logger.debug("Removing taxonomy {} - {}", taxonomy.getName(), taxonomy.getId());
				
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
			else if (entityClass == Topic.class){
					
				final Topic topic = topicService.getTopic(entityIdentifier, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
					
				if (topic == null){
					continue;
				}

				logger.debug("Removing topic {} - {}", topic.getName(), topic.getId());
				
				topicService.deleteTopicTree(topic.getId());
				
			}
			else if (entityClass == Space.class){
				
				final Space space = spaceService.getSpace(entityIdentifier, ResourceRepresentationType.SPACE_INSTANCE, FetchLevel.ENTITY);
					
				if (space == null){
					continue;
				}
				
				logger.debug("Removing space {} - {}", space.getName(), space.getId());
				
				spaceService.deleteSpace(space.getId());
			}
			else if (entityClass == ContentObject.class){
				
				final ContentObject contentObject = contentService.getContentObject(entityIdentifier, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, 
						CacheRegion.NONE, null, false);
					
				if (contentObject == null){
					continue;
				}
				
				logger.debug("Removing object {} - {}", contentObject.getSystemName(), contentObject.getId());
				
				if (contentObject.getSystemName() != null){
					contentService.deleteContentObject(contentObject.getSystemName());
				}
				
				contentService.deleteContentObject(contentObject.getId());
			}
			else if (entityClass == RepositoryUser.class){
				
				RepositoryUser repositoryUser = repositoryUserService.getRepositoryUser(entityIdentifier);
				
				if (repositoryUser == null){
					continue;
				}
				
				logger.debug("Removing repository user {} - {}", repositoryUser.getExternalId(), repositoryUser.getId());

				repositoryUserService.deleteRepositoryUser(repositoryUser.getId(), null);
			}
		}
	}


	protected abstract void postCleanup();


	protected abstract void preCleanup();
}
