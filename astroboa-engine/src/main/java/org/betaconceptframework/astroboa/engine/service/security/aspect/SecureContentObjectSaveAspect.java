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
package org.betaconceptframework.astroboa.engine.service.security.aspect;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * This aspect intercepts all content service calls responsible to
 * save content objects.
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Aspect
@Transactional(readOnly = false, rollbackFor = CmsException.class)
public class SecureContentObjectSaveAspect extends AbstractSecureContentObjectAspect{

	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.save(..))")
	private void saveContentObjectOrXmlOrJSON(){}

	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.saveContentObjectInBatchMode(..))")
	private void saveContentObjectInBatchMode(){}
	
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.saveContentObjectResourceCollection(..))")
	private void saveContentObjectResourceCollection(){};
	

	

	@Around("saveContentObjectOrXmlOrJSON() &&  args(contentObject, version, updateLastModificationTime, lockToken)")
	public Object checkSaveContentObjectWithUpdateLastModificationTime(ProceedingJoinPoint proceedingJoinPoint, 
			Object contentObject, boolean version, boolean updateLastModificationTime, String lockToken){
		
		//Check for NULL
		checkContentObjectIsNotNull(contentObject, null);
		
		if (contentObject != null && contentObject instanceof ContentObject){
			//Perform extra security checks in cases content to be saved is a ContentObject instance
			grantOrDenyContentObjectSave((ContentObject)contentObject);
		}

		try{
			return proceedingJoinPoint.proceed(new Object[]{contentObject, version, updateLastModificationTime, lockToken});
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}

	
	@Around("saveContentObjectInBatchMode() &&  args(contentObject,version,updateLastModificationTime, context)")
	public Object checkSaveContentObjectInBatchMode(ProceedingJoinPoint proceedingJoinPoint, ContentObject contentObject,boolean version, 
			boolean updateLastModificationTime, Context context){
		
		grantOrDenyContentObjectSave(contentObject);

		try{
			return proceedingJoinPoint.proceed(new Object[]{contentObject,version, updateLastModificationTime, context});
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}
	
	@Around("saveContentObjectResourceCollection() &&  args(contentSource, version, updateLastModificationTime, lockToken)")
	public Object checkSaveContentObjectResourceCollection(ProceedingJoinPoint proceedingJoinPoint, 
			Object contentSource, boolean version, boolean updateLastModificationTime, String lockToken){
		
		if (contentSource != null && contentSource instanceof List){
			List<ContentObject> contentObjects = (List<ContentObject>) contentSource;
			
			for (ContentObject contentObject : contentObjects){
				grantOrDenyContentObjectSave((ContentObject)contentObject);
			}
		}

		try{
			return proceedingJoinPoint.proceed(new Object[]{contentSource, version, updateLastModificationTime, lockToken});
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}

	public void grantOrDenyContentObjectSave(
			ContentObject contentObject){

		checkIFUserIsAuthorizedToSaveContentObject(contentObject);
		
		miscallenuousRestrictions(contentObject);
		
	}
	
	/*
	 * Specific restrictions
	 * 
	 * 
	 */
	private void miscallenuousRestrictions(ContentObject contentObject) {
		
		SecurityContext activeSecurityContext = retrieveSecurityContext();
		
		/*
		 * When the following occur then type should not be available
		 * 
		 * 1. Content type is roleObject with 
		 * 2. Logged in user does not have role CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR
		 * 3. IdentityStore repository is the same with the repository
		 */
		if (StringUtils.equals(contentObject.getContentObjectType(), "roleObject") &&
				contentObject.getTypeDefinition() != null && 
				contentObject.getTypeDefinition().getQualifiedName() != null)
		{
			if (contentObject.getTypeDefinition().getQualifiedName().equals(new QName("http://www.betaconceptframework.org/schema/astroboa/identity/role","roleObject")))
			{
				if (! userHasRole(activeSecurityContext, CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR)))
				{
					CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();
					
					if (activeRepository != null &&
							 ( activeRepository.getIdentityStoreRepositoryId() == null ||
									 StringUtils.equals(activeRepository.getIdentityStoreRepositoryId(), activeRepository.getId())
							 )
					 )
					{
						throw new CmsUnauthorizedAccessException("User "+activeSecurityContext.getIdentity()+" is not authorized to create content objects of type roleObject");
					}
				}
			}
		}
		
	}

	/*
	 * In order to grant access to user to save a content object the following rules must apply
	 * 
	 *   1 AND (2 or 3 or 4 or 5)
	 * 
	 *  1. ContentObject is not null
	 *  2. User has ROLE_ADMIN or is SYSTEM user (It is supposed that user SYSTEM has all roles)
	 *  3. ContentObject is new (its id is null or no JCR node found with provided id) and user has role ROLE_CMS_EDITOR 
	 *  4. ContentObject already exists in repository and user is the owner of the object
	 *  5. ContentObject already exists in repository, user is not the owner of the object but content object has accessibility.canBeUpdated
	 *    whose value is REPOSITORY, or contains at least one of the roles user possesses, or explicitly contains user's id
	 *  6. Passes some miscallenous restrictions
	 */
	private void checkIFUserIsAuthorizedToSaveContentObject(ContentObject contentObject)
	{
		SecurityContext activeSecurityContext = retrieveSecurityContext();
		String userId = activeSecurityContext.getIdentity();

		//1. ContentObject is not null
		checkContentObjectIsNotNull(contentObject, userId);


		//2. User has ROLE_ADMIN
		final String roleAdminForActiveRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN);
		if (userHasRole(activeSecurityContext, roleAdminForActiveRepository))
		{
			logger.debug("User {} is authorized to save contentObject with id {} and system name {} because she has role {}", 
					new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), roleAdminForActiveRepository});
			return;
		}

		try{
			Node contentObjectNode = retrieveContentObjectNodeForContentObject(contentObject.getId());

			//3. ContentObject is new (its id is null or no JCR node found with provided id) and user has role ROLE_CMS_EDITOR
			if (contentObjectNode==null)
			{
				final String roleCmsEditorForActiveRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR);
				if (! userHasRole(activeSecurityContext, roleCmsEditorForActiveRepository))
				{
					logger.debug("Jcr node corresponding to contentObject was not found. This means that content object is new. Nevertheless " +
							" user {} is not authorized to save contentObject with id {} and system name {} because she does not have role {}", 
							new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), roleCmsEditorForActiveRepository});

					throw new CmsUnauthorizedAccessException("User "+userId + " is not authorized to save content object as she has not been assigned role "+
							roleCmsEditorForActiveRepository);
				}
				else
				{
					logger.debug("Jcr node corresponding to contentObject was not found. This means that content object is new. Nevertheless " +
							" user {} is authorized to save contentObject with id {} and system name {} because she has role {}", 
							new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), roleCmsEditorForActiveRepository});

					return;
				}
			}

			/*
			 * 4. ContentObject already exists in repository and user is the owner of the object
			 */

			String ownerIdFoundInRepository = retrieveOwnerFromContentObjectNode(contentObjectNode);

			//Retrieve repository user id from Subject
			String repositoryUserIdOfLoggedInUser = retrieveRepositoryUserIdForLoggedInUser(activeSecurityContext, userId, contentObjectNode);

			if ( StringUtils.equals(ownerIdFoundInRepository, repositoryUserIdOfLoggedInUser))
			{
				logger.debug("User {} is authorized to save contentObject with id {} and system name {} because she owns contentObject", 
						new Object[]{userId, contentObject.getId(), contentObject.getSystemName()});

				return;	
			}

			/*
			 * 5. ContentObject already exists in repository, user is not the owner of the object but content object has accessibility.canBeUpdated
			 * whose value is REPOSITORY, or contains at least one of the roles user possesses, or explicitly contains user's id
			 */
			//Get values for property accessibility.canBeUpdatedby
			boolean isUserAuthorized = false;

			if (!contentObjectNode.hasProperty("accessibility/canBeUpdatedBy"))
			{
				logger.debug("User {} is not authorized to save contentObject with id {} and system name {} because she does not own contentObject " +
						" and property 'accessibility.canBeUpdatedBy' does not have any values at all", 
						new Object[]{userId, contentObject.getId(), contentObject.getSystemName()});

				isUserAuthorized = false;
			}
			else
			{
				Value[] canBeUpdatedByAuthorizationList = contentObjectNode.getProperty("accessibility/canBeUpdatedBy").getValues();

				for (Value authorizedCanBeUpdatedByValue : canBeUpdatedByAuthorizationList)
				{
					//If value REPOSITORY is found then user is authorized
					final String userOrRoleAuthorizedToSave = authorizedCanBeUpdatedByValue.getString();
					
					if (ContentAccessMode.ALL.toString().equals(userOrRoleAuthorizedToSave)){
						isUserAuthorized=true;
						logger.debug("User {} is authorized to save contentObject with id {} and system name {} because she does not own contentObject " +
								" but property 'accessibility.canBeUpdatedBy' contains value {}", 
								new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), ContentAccessMode.ALL});

						break;
					}
					else
					{
						//Check if authorizedCanBeUpdatedBy value corresponds to user id
						if (StringUtils.equals(userOrRoleAuthorizedToSave, userId)){ //User Id has been saved as is to canBeUpdatedBy property 
							logger.debug("User {} is authorized to save contentObject with id {} and system name {} because she does not own contentObject " +
									" but property 'accessibility.canBeUpdatedBy' contains value {} which is user's Id {}." , 
									new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), userOrRoleAuthorizedToSave, userId});

							isUserAuthorized= true;
							break;
						}
						else if (userHasRole(activeSecurityContext, userOrRoleAuthorizedToSave)){ //User Role has been saved as is to canBeUpdatedBy property 
							logger.debug("User {} is authorized to save contentObject with id {} and system name {} because she does not own contentObject " +
									" but property 'accessibility.canBeUpdatedBy' contains role {} which is assigned to user as well." ,
									new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), userOrRoleAuthorizedToSave});

							isUserAuthorized= true;
							break;
						}
							
					}
				}

				if (! isUserAuthorized)
				{
					List<String> canBeUpdatedList = new ArrayList<String>();
					for (Value authorizedCanBeUpdatedByValue : canBeUpdatedByAuthorizationList){
						canBeUpdatedList.add(authorizedCanBeUpdatedByValue.getString());
					}
					
					logger.warn("User {} is not authorized to save contentObject with id {} and system name {} because she does not own contentObject " +
							" and property 'accessibility.canBeUpdatedBy' has values {}. \nThese values do not include value {} " +
							" or user's id {} or any of user's assigned roles {}", 
							new Object[]{userId, contentObject.getId(), contentObject.getSystemName(), canBeUpdatedList, 
							ContentAccessMode.ALL.toString(), userId, activeSecurityContext.getAllRoles() 
							});

					throw new CmsUnauthorizedAccessException("User "+ userId + " is not authorized to save content object "+ contentObjectNode.getPath());
				}
			}


		}
		catch(CmsException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new CmsException(e);
		}


	}

}
