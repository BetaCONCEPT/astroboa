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
package org.betaconceptframework.astroboa.engine.jcr.identitystore.jackrabbit;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.engine.jcr.dao.ImportDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JackrabbitIdentityStoreDao extends JcrDaoSupport{

	private static final String SYSTEM_USER_ID = "SYSTEM_USER_ID";

	private final Logger  logger = LoggerFactory.getLogger(getClass());

	private List<Resource> roleXmlResources;

	private Resource systemPersonXmlResource;

	private RepositoryUserDao repositoryUserDao;

	private ContentService contentService;
	
	private ImportDao importDao;
	
	public void setImportDao(ImportDao importDao) {
		this.importDao = importDao;
	}
	
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	public void setCmsRepositoryEntityUtils(
			CmsRepositoryEntityUtils cmsRepositoryEntityUtils) {
		this.cmsRepositoryEntityUtils = cmsRepositoryEntityUtils;
	}

	/**
	 * @param contentDao the contentDao to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * @param rolesTaxonomyXml the rolesTaxonomyXml to set
	 */
	public void setRoleXmlResources(List<Resource> roleXmlResources) {
		this.roleXmlResources = roleXmlResources;
	}

	/**
	 * @param systemPersonXmlResource the systemPersonXmlResource to set
	 */
	public void setSystemPersonXmlResource(Resource systemPersonXmlResource) {
		this.systemPersonXmlResource = systemPersonXmlResource;
	}

	/**
	 * @param repositoryUserDao the repositoryUserDao to set
	 */
	public void setRepositoryUserDao(RepositoryUserDao repositoryUserDao) {
		this.repositoryUserDao = repositoryUserDao;
	}

	//Bootstrap necessary information about SYSTEM user and built in roles
	public void initializeIdentityStore(String cmsRepositoryId, CmsRepository identityStoreRepository) {


		if (identityStoreRepository == null){
			throw new CmsException("No CmsRepository is provided. Unable to initialize identity store");
		}

		try{

			Set<String> repIds = new HashSet<String>();
			repIds.add(cmsRepositoryId);
			repIds.add(identityStoreRepository.getId());

			if (CollectionUtils.isNotEmpty(roleXmlResources)){

				logger.info("Initializing roles in IdentityStore "+identityStoreRepository.getId());

				String systemUserId = retrieveSystemRepositoryUserId();
				
				if (StringUtils.isBlank(systemUserId)){
					logger.warn("Found no repository user with external id 'SYSTEM'. Cannot initialize roles in IdentityStore");
					return;
				}

				//Roles will be imported twice. Once for the repository loaded and one for the 
				//repository representing the identity store (if it is different).
				//This is due to the fact that roles make sense in terms of a repository,
				//that is ROLE_ADMIN@repository .
				Map<String, ContentObject> rolesPerName = new HashMap<String, ContentObject>();

				for (String repositoryId : repIds){
					for(Resource roleXmlResource : roleXmlResources){
						ContentObject roleObject = importRole(roleXmlResource, systemUserId, repositoryId);

						if (roleObject != null)
						{
							rolesPerName.put(((StringProperty)roleObject.getCmsProperty("name")).getSimpleTypeValue(), roleObject);
						}
					}

					//Finally save or update roles. ORDER IS SIGNIFICANT
					ContentObject roleCmsExternalViewerContentObject = rolesPerName.get(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER,repositoryId));
					
					//Role does not exist. Create it.
					if (roleCmsExternalViewerContentObject.getId() == null){
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_EXTERNAL_VIEWER);
					}
					else{
						//Role exists. Check to see if it is member of any other role (this is not permitted)
						ObjectReferenceProperty isMemberOfProperty = (ObjectReferenceProperty) roleCmsExternalViewerContentObject.getCmsProperty("isMemberOf");
						
						if (isMemberOfProperty != null && isMemberOfProperty.hasValues()){
							logger.warn("Content object "+ roleCmsExternalViewerContentObject.getSystemName() + " which represents "+ CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER,repositoryId)+ 
									" is member of the roles "+ isMemberOfProperty.getSimpleTypeValues()+ " This is not accepted and they will be removed.");
							isMemberOfProperty.removeValues();
							contentService.save(roleCmsExternalViewerContentObject, false, true, null);
						}
					}

					//Relate role ROLE_CMS_INTERNAL_VIEWER with ROLE_CMS_EXTERNAL_VIEWER
					if (addRoleAsMemberToRole(Arrays.asList(CmsRole.ROLE_CMS_EXTERNAL_VIEWER), CmsRole.ROLE_CMS_INTERNAL_VIEWER, rolesPerName, repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_INTERNAL_VIEWER);
					}

					//Relate all roles with ROLE_CMS_ITNERNAL_VIEWER
					List<CmsRole> roleCmsInternalViewerAsList = Arrays.asList(CmsRole.ROLE_CMS_INTERNAL_VIEWER);

					if ( addRoleAsMemberToRole(roleCmsInternalViewerAsList, CmsRole.ROLE_CMS_EDITOR, rolesPerName, repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_EDITOR);
					}

					if (addRoleAsMemberToRole(roleCmsInternalViewerAsList, CmsRole.ROLE_CMS_PORTAL_EDITOR,rolesPerName,repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_PORTAL_EDITOR);
					}

					if (addRoleAsMemberToRole(roleCmsInternalViewerAsList, CmsRole.ROLE_CMS_TAXONOMY_EDITOR, rolesPerName,repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_TAXONOMY_EDITOR);
					}


					if (addRoleAsMemberToRole(roleCmsInternalViewerAsList, CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER, rolesPerName,repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER);
					}

					if ( addRoleAsMemberToRole(roleCmsInternalViewerAsList, CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, rolesPerName, repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR);
					}

					if (addRoleAsMemberToRole(Arrays.asList(CmsRole.ROLE_CMS_EDITOR, CmsRole.ROLE_CMS_PORTAL_EDITOR, 
							CmsRole.ROLE_CMS_TAXONOMY_EDITOR, CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER, CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR),
							CmsRole.ROLE_ADMIN, rolesPerName, repositoryId))
					{
						saveRoleObject(rolesPerName, repositoryId, CmsRole.ROLE_ADMIN);
					}



				}

			}
			else{
				logger.warn("Found no role xml resources");
			}

		}
		catch (Exception e)
		{
			logger.error("Problem initializing identity store", e);
			throw new CmsException(e);
		}
	}

	private void saveRoleObject(Map<String, ContentObject> rolesPerName, String repositoryId, CmsRole role){
		if (rolesPerName.containsKey(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(role,repositoryId))){
			ContentObject contentObject = rolesPerName.get(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(role,repositoryId));

			contentService.save(contentObject, false, true, null);
		}
	}

	private boolean addRoleAsMemberToRole(List<CmsRole> parentRoles, CmsRole memberRole, Map<String, ContentObject> rolesPerName, String repositoryId){

		String memberRoleAffiliation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(memberRole,repositoryId);

		ContentObject memberRoleObject = rolesPerName.get(memberRoleAffiliation);

		boolean memberRoleHasBeenUpdated = false;

		for (CmsRole parentRole : parentRoles){
			String parentRoleAffiliation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(parentRole, repositoryId);

			ContentObject parentRoleObject = rolesPerName.get(parentRoleAffiliation);

			if (parentRoleObject != null && memberRoleObject != null){
				ObjectReferenceProperty isMemberOfProperty = (ObjectReferenceProperty) memberRoleObject.getCmsProperty("isMemberOf");
				if (isMemberOfProperty.hasNoValues()){
					isMemberOfProperty.addSimpleTypeValue(parentRoleObject);
					memberRoleHasBeenUpdated = true;
				}
				else{
					boolean roleMemberAlreadyExists = false;

					for (ContentObject isMemberOfObject : isMemberOfProperty.getSimpleTypeValues()){
						if ( StringUtils.equals(((StringProperty)isMemberOfObject.getCmsProperty("name")).getSimpleTypeValue(), parentRoleAffiliation) ){
							roleMemberAlreadyExists = true;
							break;
						}
					}

					if (! roleMemberAlreadyExists)
					{
						isMemberOfProperty.addSimpleTypeValue(parentRoleObject);
						memberRoleHasBeenUpdated = true;
					}
				}
			}
			else{
				logger.warn("Cound not add member "+
						memberRoleAffiliation + " to role "+
						parentRoleAffiliation + " as "+
						( memberRoleObject == null ? memberRoleAffiliation :
							parentRoleObject == null ? parentRoleAffiliation : " both " )
							+ " has not be found in repository");
			}
		}

		return memberRoleHasBeenUpdated ;

	}

	private ContentObject findRoleObject(CmsRole cmsRole, String repositoryId, 
			boolean throwExceptionIfNoneOrMoreThanOneFound) {
		String cmsRoleAffiliation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, repositoryId);

		CmsOutcome<ContentObject> roleOutcome = findRole(cmsRoleAffiliation);

		if (roleOutcome == null || roleOutcome.getCount() == 0){
			if (throwExceptionIfNoneOrMoreThanOneFound){
				throw new CmsException("Found no role with name "+ cmsRoleAffiliation);
			}

			return null;
		}

		if (roleOutcome.getCount() >1){
			if (throwExceptionIfNoneOrMoreThanOneFound){
				throw new CmsException("Found more than one roles with name "+
						cmsRoleAffiliation);
			}
		}

		return roleOutcome.getResults().get(0);
	}

	public void createSystemPerson(String cmsRepositoryId, String identityStoreId)  {

		InputStream inputStream = null;
		try{

			//Check if system person already exists
			ContentObjectCriteria personCriteria = CmsCriteriaFactory.newContentObjectCriteria("personObject");
			personCriteria.addCriterion(CriterionFactory.equalsCaseInsensitive("personAuthentication.username",IdentityPrincipal.SYSTEM));
			personCriteria.setOffsetAndLimit(0, 1);
			personCriteria.doNotCacheResults();
			personCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(personCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			ContentObject systemPersonObject = null;

			boolean systemUpdated = false;

			if (outcome != null && outcome.getCount() > 0){

				if (outcome.getCount() > 1){
					logger.warn("Found more than one open social content objects with system name IDENTITY_STORE_SYSTEM_PERSON"+
					" which refer to SYSTEM user ");
					return;
				}
				else{
					systemPersonObject = outcome.getResults().get(0);
				}
			}
			else
			{
				logger.info("SYSTEM person for identity store not found. Creating one");

				if (systemPersonXmlResource ==null || ! systemPersonXmlResource.exists()){
					throw new CmsException("No system person xml file found");
				}
				else{

					inputStream = systemPersonXmlResource.getInputStream();
					String systemPersonXml = IOUtils.toString(inputStream, "UTF-8");
					String systemUserId = retrieveSystemRepositoryUserId();

					if (StringUtils.isBlank(systemUserId)){
						logger.warn("Found no repository user with external id 'SYSTEM'. Cannot initialize System Person in IdentityStore");
						return;
					}

					//Replace ids
					systemPersonXml = StringUtils.replace(systemPersonXml, SYSTEM_USER_ID, systemUserId);


					//Obtain content object
					systemPersonObject = importDao.importContentObject(systemPersonXml, false, false, ImportMode.DO_NOT_SAVE, null);

					if (systemPersonObject == null){
						throw new CmsException("Could not create a content object from provided source");
					}
					
					systemUpdated = true;
				}
			}
			
			ObjectReferenceProperty systemRoleProperty = (ObjectReferenceProperty) systemPersonObject.getCmsProperty("personAuthorization.role");

			systemUpdated = fixRolesForSystemIfBroken(systemRoleProperty, cmsRepositoryId) || systemUpdated;

			if (!StringUtils.equals(cmsRepositoryId, identityStoreId)){
				systemUpdated = fixRolesForSystemIfBroken(systemRoleProperty, identityStoreId) || systemUpdated;
			}

			//Save ContentObject
			if (systemUpdated){
				contentService.save(systemPersonObject, false, true, null);
			}

		}
		catch (CmsException e)
		{
			logger.error("Problem create system person", e);
			throw e;
		}
		catch (Exception e)
		{
			logger.error("Problem create system person", e);
			throw new CmsException(e);
		}
		finally{

			if (inputStream != null){
				IOUtils.closeQuietly(inputStream);
			}
		}

	}

	private String retrieveSystemRepositoryUserId() {
		RepositoryUser systemRepositoryUser = repositoryUserDao.getSystemRepositoryUser();
		
		if (systemRepositoryUser == null){
			//Backward compatibility. Search for user with external Id 1
			systemRepositoryUser = repositoryUserDao.getRepositoryUser("1");
			
			if (systemRepositoryUser != null){
				logger.warn("Could not find Repository User with externalId 'SYSTEM' but found repository user with external Id '1'");
				return  systemRepositoryUser.getId();
			}
		}
		else
		{
			return  systemRepositoryUser.getId();
		}
		
		return null;
		
	}


	private boolean fixRolesForSystemIfBroken(ObjectReferenceProperty systemRoleProperty, String cmsRepositoryId)
	{
		boolean foundRoleAdmin = false;

		if (systemRoleProperty.hasValues()){
			for (ContentObject roleObject : systemRoleProperty.getSimpleTypeValues()){

				StringProperty roleNameProperty = (StringProperty) roleObject.getCmsProperty("name");

				if (roleNameProperty == null || roleNameProperty.hasNoValues()){
					logger.warn("Found roleObject without value in property 'name'");
				}
				else{
					if (StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, cmsRepositoryId), 
							roleNameProperty.getSimpleTypeValue())){
						foundRoleAdmin = true;
					}
				}
			}
		}

		if (! foundRoleAdmin ){
			ContentObject roleAdminForCmsRepository = findRoleObject(CmsRole.ROLE_ADMIN, cmsRepositoryId, false);

			if (roleAdminForCmsRepository == null){
				// No ROLE ADMIN. Look for ROLE_CMS_IDENTITY_STORE_EDITOR
				ContentObject roleCmsIdentityStoreEditorForCmsRepository = findRoleObject(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, cmsRepositoryId, false);

				if (roleCmsIdentityStoreEditorForCmsRepository == null)
				{
					//NO ROLE_CMS_IDENTITY_STORE_EDITOR . Look for ROLE_CMS_EDITOR
					ContentObject roleCmsEditorForCmsRepository = findRoleObject(CmsRole.ROLE_CMS_EDITOR, cmsRepositoryId, false);

					if (roleCmsEditorForCmsRepository == null)
					{
						//NO ROLE_CMS_EDITOR . Look for ROLE_CMS_INTERNAL_VIEWER
						ContentObject roleCmsInternalViewerForCmsRepository = findRoleObject(CmsRole.ROLE_CMS_INTERNAL_VIEWER, cmsRepositoryId, false);

						if (roleCmsInternalViewerForCmsRepository == null)
						{
							logger.warn("SYSTEM could not be connected to none of the following roles " +
									" ROLE_ADMIN, ROLE_CMS_IDENTITY_STORE_EDITOR, ROLE_CMS_EDITOR, ROLE_CMS_INTERNAL_VIEWER for repository " + cmsRepositoryId +
							" because neither could be found ");

							return false;
						}
						else
						{
							logger.warn("SYSTEM person is connected to ROLE_CMS_INTERNAL_VIEWER for repository "+ cmsRepositoryId + " as ROLE_CMS_EDITOR could not be found ");
							systemRoleProperty.addSimpleTypeValue(roleCmsInternalViewerForCmsRepository);
							return true;
						}
					}
					else
					{
						logger.warn("SYSTEM person is connected to ROLE_CMS_EDITOR for repository "+ cmsRepositoryId + " as ROLE_CMS_IDENTITY_STORE_EDITOR could not be found ");
						systemRoleProperty.addSimpleTypeValue(roleCmsEditorForCmsRepository);
						return true;
					}
				}
				else
				{
					logger.warn("SYSTEM person is connected to ROLE_CMS_IDENTITY_STORE_EDITOR for repository "+ cmsRepositoryId + " as ROLE_ADMIN could not be found ");
					systemRoleProperty.addSimpleTypeValue(roleCmsIdentityStoreEditorForCmsRepository);
					return true;
				}
			}
			else
			{
				logger.warn("SYSTEM person is connected to ROLE_ADMIN for repository "+ cmsRepositoryId + " ");
				systemRoleProperty.addSimpleTypeValue(roleAdminForCmsRepository);
				return true;
			}
		}

		return false;
	}

	private ContentObject importRole(Resource roleXmlResource, String systemUserId, String cmsRepositoryId) throws Exception {

		if (roleXmlResource ==null || ! roleXmlResource.exists()){
			throw new CmsException("No roles xml file found");
		}
		else{

			String roleName = StringUtils.substringBeforeLast(roleXmlResource.getFilename(), CmsConstants.PERIOD_DELIM);

			String roleAffilitation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.valueOf(roleName), cmsRepositoryId);

			CmsOutcome<ContentObject> outcome = findRole(roleAffilitation);

			if (outcome != null && outcome.getCount() > 0){

				if (outcome.getCount() > 1){
					logger.warn("Found more than one roles with system name "+ roleAffilitation+ " Will use first role available");
				}

				return outcome.getResults().get(0);
			}


			logger.info("Role {} for identity store not found. Creating one", roleAffilitation);

			InputStream inputStream = null;

			try{
				inputStream = roleXmlResource.getInputStream();

				String roleXml = IOUtils.toString(inputStream, "UTF-8");

				//Replace ids
				roleXml = StringUtils.replace(roleXml, SYSTEM_USER_ID, systemUserId);
				roleXml = StringUtils.replace(roleXml, roleName, roleAffilitation);

				
				//Obtain content object
				ContentObject roleObject = importDao.importContentObject(roleXml, false, false,ImportMode.DO_NOT_SAVE, null); 
					
				if (roleObject == null){
					throw new CmsException("Could not create a content object from provided source");
				}
				
				//fix system name
				roleObject.setSystemName(cmsRepositoryEntityUtils.fixSystemName(roleObject.getSystemName()));

				return roleObject;
			}
			finally{
				if (inputStream != null){
					IOUtils.closeQuietly(inputStream);
				}
			}

		}

	}

	private CmsOutcome<ContentObject> findRole(String roleName) {
		ContentObjectCriteria personCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");
		personCriteria.addCriterion(CriterionFactory.equals("name",roleName));
		personCriteria.setOffsetAndLimit(0, 1);
		personCriteria.doNotCacheResults();
		personCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		personCriteria.addPropertyPathWhoseValueWillBePreLoaded("name");
		personCriteria.addPropertyPathWhoseValueWillBePreLoaded("isMemberOf");

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(personCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		return outcome;
	}
}
