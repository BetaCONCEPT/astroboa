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
package org.betaconceptframework.astroboa.engine.service.security.aspect;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
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
public class SecureContentObjectDeleteAspect extends AbstractSecureContentObjectAspect{

	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.deleteContentObject(..))")
	private void deleteContentObject(){}

	@Around("deleteContentObject() &&  args(contentObjectId)")
	public Object checkDeleteContentObject(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId)
	{
		grantOrDenyContentObjectDelete(contentObjectId);

		try{
			return proceedingJoinPoint.proceed(new Object[]{contentObjectId});
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}

	public void grantOrDenyContentObjectDelete(
			String contentObjectId) 
	{
		checkIfUserIsAuthorizedToDeleteContentObject(contentObjectId);

		miscallenuousRestrictions(contentObjectId);

	}

	/*
	 * Specific restrictions
	 * 
	 * 
	 */
	private void miscallenuousRestrictions(String contentObjectId) 
	{
		try{
			Node contentObjectNode = retrieveContentObjectNodeForContentObject(contentObjectId);

			if (contentObjectNode==null)
			{
				return;
			}

			if (! contentObjectNode.hasProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()))
			{
				//THIS SHOULD NEVER HAPPEN
				logger.error("Found content object node "+ contentObjectNode.getPath() + " without property "+ CmsBuiltInItem.ContentObjectTypeName.getJcrName()+
				" This means that a content object was saved without content type...");
				throw new CmsException("System error");
			}

			String contentType = contentObjectNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString();

			//Logged in user cannot delete Person object representing it self !!!
			if (StringUtils.equals("personObject",contentType))
			{

				SecurityContext activeSecurityContext = retrieveSecurityContext();
				String identity = activeSecurityContext.getIdentity();

				String username  = null;

				if (contentObjectNode.hasProperty("personAuthentication/username"))
				{
					username = contentObjectNode.getProperty("personAuthentication/username").getString();
				}

				if (StringUtils.equals(identity, username))
				{
					CmsRepository activeRepository = AstroboaClientContextHolder.getActiveCmsRepository();

					if (activeRepository != null &&
							( activeRepository.getIdentityStoreRepositoryId() == null ||
									StringUtils.equals(activeRepository.getIdentityStoreRepositoryId(), activeRepository.getId())
							)
					)
					{
						throw new CmsUnauthorizedAccessException("User "+activeSecurityContext.getIdentity()+" is not authorized to delete person content object " +
						" which represent her self");
					}	
				}
			}

		}catch(Exception e)
		{
			logger.error("",e);
			throw new CmsException(e);
		}
	}

	/*
	 * In order to grant access to user to delete a content object the following rules must apply
	 * 
	 *   
	 * 
	 *  1. ContentObjectId is not null
	 *  2. User has ROLE_ADMIN or is SYSTEM user (It is supposed that user SYSTEM has all roles)
	 *  3. User is the owner of the object
	 *  4. User is not the owner of the object but content object has accessibility.canBeDeletedBy
	 *    whose value is REPOSITORY, or contains at least one of the roles user possesses, or explicitly contains user's id
	 */
	private void checkIfUserIsAuthorizedToDeleteContentObject(String contentObjectId)
	{
		SecurityContext activeSecurityContext = retrieveSecurityContext();
		String userId = activeSecurityContext.getIdentity();

		//1. ContentObjectId is not null
		checkContentObjectIdIsNotNull(contentObjectId, userId);


		//2. User has ROLE_ADMIN
		final String roleAdminForActiveRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN);
		
		if (userHasRole(activeSecurityContext, roleAdminForActiveRepository))
		{
			logger.debug("User {} is authorized to delete contentObject with id {} because she has role {}", 
					new Object[]{userId, contentObjectId, roleAdminForActiveRepository});
			return;
		}


		try{
			Node contentObjectNode = retrieveContentObjectNodeForContentObject(contentObjectId);

			if (contentObjectNode==null)
			{
				logger.debug("Jcr node corresponding to contentObject with id {} was not found. Deletion cannot procceed", contentObjectId);

				throw new CmsException("ContentObject with identifier "+contentObjectId+" could not be deleted because no such content object exists");
			}

			/*
			 * 3. User is the owner of the object
			 */

			String ownerIdFoundInRepository = retrieveOwnerFromContentObjectNode(contentObjectNode);

			//Retrieve repository user id from Subject
			String repositoryUserIdOfLoggedInUser = retrieveRepositoryUserIdForLoggedInUser(activeSecurityContext, userId, contentObjectNode);

			if ( StringUtils.equals(ownerIdFoundInRepository, repositoryUserIdOfLoggedInUser))
			{
				logger.debug("User {} is authorized to delete contentObject with id {} because she owns contentObject", 
						new Object[]{userId, contentObjectId});

				return;	
			}

			/*
			 * 4. User is not the owner of the object but content object has accessibility.canBeUpdated
			 * whose value is REPOSITORY, or contains at least one of the roles user possesses, or explicitly contains user's id
			 */
			//Get values for property accessibility.canBeDeletedBy
			boolean isUserAuthorized = false;

			if (!contentObjectNode.hasProperty("accessibility/canBeDeletedBy"))
			{
				logger.debug("User {} is not authorized to delete contentObject with id {} because she does not own contentObject " +
						" and property 'accessibility.canBeDeletedBy' does not have any values at all", 
						new Object[]{userId, contentObjectId});

				isUserAuthorized = false;
			}
			else
			{
				Value[] canBeDeletedByAuthorizationList = contentObjectNode.getProperty("accessibility/canBeDeletedBy").getValues();

				for (Value authorizedCanBeDeletedByValue : canBeDeletedByAuthorizationList)
				{
					//If value REPOSITORY is found then user is authorized
					final String userOrRoleAuthorizedToDelete = authorizedCanBeDeletedByValue.getString();
					
					if (ContentAccessMode.ALL.toString().equals(userOrRoleAuthorizedToDelete)){
						isUserAuthorized=true;
						logger.debug("User {} is authorized to delete contentObject with id {} because she does not own contentObject " +
								" but property 'accessibility.canBeDeletedBy' contains value {}", 
								new Object[]{userId, contentObjectId, ContentAccessMode.ALL});

						break;
					}
					else
					{
						//Check if authorizedCanBeDeletedBy value corresponds to user id
						if (StringUtils.equals(userOrRoleAuthorizedToDelete, userId)){ //User Id has been saved as is to canBeDeletedBy property 
							logger.debug("User {} is authorized to delete contentObject with id {} because she does not own contentObject " +
									" but property 'accessibility.canBeDeletedBy' contains value {} which is user's Id {}",  
									new Object[]{userId, contentObjectId, userOrRoleAuthorizedToDelete, userId});

							isUserAuthorized= true;
							break;
						}
						else if (userHasRole(activeSecurityContext, userOrRoleAuthorizedToDelete)){ //User Role has been saved as is to canBeDeletedBy property 
							logger.debug("User {} is authorized to delete contentObject with id {} because she does not own contentObject " +
									" but property 'accessibility.canBeDeletedBy' contains role {} which is assigned to user as well. " , 
									new Object[]{userId, contentObjectId, userOrRoleAuthorizedToDelete});

							isUserAuthorized= true;
							break;
						}
							
					}
				}

				if (! isUserAuthorized)
				{
					List<String> canBeDeletedList = new ArrayList<String>();
					for (Value authorizedCanBeDeletedByValue : canBeDeletedByAuthorizationList){
						canBeDeletedList.add(authorizedCanBeDeletedByValue.getString());
					}
					
					logger.warn("User {} is not authorized to delete contentObject with id {} because she does not own contentObject " +
							" and property 'accessibility.canBeDeletedBy' has values {}. \nThese values do not include value {} " +
							" or user's id {} or any of user's assigned roles {}", 
							new Object[]{userId, contentObjectId, canBeDeletedList, 
							ContentAccessMode.ALL.toString(), userId, activeSecurityContext.getAllRoles()});

					throw new CmsUnauthorizedAccessException("User "+ userId + " is not authorized to delete content object "+ contentObjectNode.getPath());
				}
			}


		}catch(Exception e)
		{
			throw new CmsException(e);
		}


	}

	private void checkContentObjectIdIsNotNull(String contentObjectId, String userId) {
		if (StringUtils.isBlank(contentObjectId)){
			throw new CmsException("Content object id not provided. User "+ userId);
		}
	}


}
