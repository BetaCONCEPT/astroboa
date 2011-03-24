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
import javax.security.auth.Subject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.RepositoryUserIdPrincipal;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl;
import org.betaconceptframework.astroboa.engine.service.security.exception.NonAuthenticatedOperationException;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.CmsOutcomeImpl;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.util.CmsConstants.ContentObjectStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An aspect which intercepts content access calls and secures them.
 * It first checks whether a user has been already authenticated for access to the repository.
 * If no authenticated user is found the it throws an authentication exception.
 * If an authenticated user is found, it retrieves user information (user id, groups, roles) and
 * creates security criteria which are added to the search criteria in order to perform a search that
 * returns only the objects that the user is authorized to access
 * If a specific object is accessed through its id it inspect the security configuration of the object and if it
 * is not readable by the user it throws an authorization exception otherwise it allows the method to return the object
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Aspect
public class SecureContentServiceAspect{
	
	private final static Logger logger = LoggerFactory.getLogger(SecureContentServiceAspect.class);
	
	/**
	 * This Pointcut is triggered when the getContentObjectByIdAndLocale() method is used
	 * 
	 */
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.getContentObjectByIdAndLocale(..))")
	private void getContentObjectByIdAndLocale(){}
	
	/**
	 * This Pointcut is triggered when the getContentObjectById() method is used
	 * 
	 */
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.getContentObjectById(..))")
	private void getContentObjectById(){}
	
	/**
	 * This Pointcut is triggered when the getContentObject() method is used
	 * 
	 */
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.getContentObject(..))")
	private void getContentObject(){}
	

	/**
	 * This Pointcut is triggered when the getContentObjectByVersionName() method is used
	 * 
	 */
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.getContentObjectByVersionName(..))")
	private void getContentObjectByVersionName(){}

	/**
	 * This Pointcut is triggered when the searchContentObject() method is used
	 * 
	 */
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.searchContentObjects(..))")
	private void searchContentObjects(){}
	
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.searchContentObjectsAndExportToXml(..))")
	private void searchContentObjectsAndExportToXml(){}
	
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.searchContentObjectsAndExportToJson(..))")
	private void searchContentObjectsAndExportToJson(){}
	
	@Pointcut("execution(public * org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl.copyContentObject(..))")
	private void copyContentObject(){}
	
	@Around("getContentObjectByIdAndLocale() &&  args(contentObjectId,locale, cacheRegion)")
	public Object checkGetContentObjectByIdAndLocale(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId, String locale,CacheRegion cacheRegion){
		return grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId, locale, cacheRegion}, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	}
	
	@Around("getContentObjectById() &&  args(contentObjectId, cacheRegion)")
	public Object checkGetContentObjectById(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId,CacheRegion cacheRegion){
		return grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId, cacheRegion}, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	}
	
	@Around("getContentObject() &&  args(contentObjectId, renderProperties, cacheRegion)")
	public Object checkGetContentObject(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId, RenderProperties renderProperties,CacheRegion cacheRegion){
		return grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId, renderProperties, cacheRegion}, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	}

	@Around("getContentObject() &&  args(contentObjectId, output, fetchLevel, cacheRegion, propertyPathsToInclude, serializeBinaryContent)")
	public <T> T checkGetContentObject(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId, ResourceRepresentationType<T> output, FetchLevel fetchLevel, CacheRegion cacheRegion, List<String> propertyPathsToInclude,boolean serializeBinaryContent){
		return (T) grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId, output, fetchLevel, cacheRegion, propertyPathsToInclude, serializeBinaryContent}, output);
	}

	
	@Around("getContentObjectByVersionName() &&  args(contentObjectId, versionName, locale, cacheRegion)")
	public Object checkGetContentObjectByVersionName(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId, String versionName, String locale,CacheRegion cacheRegion){
		return grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId, versionName, locale, cacheRegion}, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	}
	
	@Around("copyContentObject() &&  args(contentObjectId)")
	public Object checkCopyContentObject(ProceedingJoinPoint proceedingJoinPoint, String contentObjectId)
	{
		return grantOrDenyAccessToContentObject(proceedingJoinPoint, contentObjectId, new Object[]{contentObjectId}, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE);
	}
	
	private Object grantOrDenyAccessToContentObject(ProceedingJoinPoint proceedingJoinPoint, String contentObjectIdOrSystemName, Object[] methodParameters, ResourceRepresentationType contentObjectOutput)
	{
		if ( contentObjectIdIsNotNull(contentObjectIdOrSystemName))
		{
			SecurityContext activeSecurityContext = AbstractSecureContentObjectAspect.retrieveSecurityContext();
			
			//Retrieve jcr node which corresponds to requested content object
			Node contentObjectNode = null;
			
			try {
				
				if (! (proceedingJoinPoint.getTarget() instanceof ContentServiceImpl)){
					return generateEmptyOutcome(contentObjectOutput);
				}
				
				contentObjectNode = ((ContentServiceImpl)proceedingJoinPoint.getTarget()).getContentObjectNodeByIdOrSystemName(contentObjectIdOrSystemName);
				
				//contentObject = (ContentObject) proceedingJoinPoint.proceed(methodParameters);
			
				if (contentObjectNode == null){
					return generateEmptyOutcome(contentObjectOutput);
				}
			
				String userId = activeSecurityContext.getIdentity();

				// if the authenticated user has not been granted the role: ROLE_CMS_INTERNAL_VIEWER 
				// then we allow her to read only published content objects i.e. those that their status is equal to "published" or "publishedAndArchived".
				// As of today a published content object overrules any "read" security option to prevent complexities in security rule handling 
				// and remove the extra effort required for the publisher of content objects 
				// It is not very convenient to require to change the "read" security option of published content objects to "ALL" to allow to be read by REPOSITORY 
				// and then when publication status ends revert back to previous security settings. 
				// Additionally "ALL" should be interpreted as "REPOSITORY PHYSICAL PERSONS or REPOSITORY USERS WHICH ARE EXPLICITLY GRANTED THE PERMISSION TO VIEW THE REPOSITORY, 
				// i.e. those in role ROLE_CMS_INTERNAL_VIEWER".
				// Anonymous is not an actual user registered in the identity store. It is a convention introduced in order to cope with the security rule that we always need some user 
				// in order to permit access to content. So for any user that tries to see the repository without logging in, the front-end system, e.g. the web application, should
				// silently perform a virtual login as the anonymous user.
				//
				// Through the above convention it becomes really easy to publish and un-publish content objects. 
				// The idea is that if someone publishes a content object then she implicitly removes any read restrictions.
				// All other restrictions apply and furthermore read restrictions are still there and remain valid when status is not set to "published" any more.
				// We may revisit this convention if the use of the repository reveals another way of interpreting anonymous requests and published objects
				//
				// Be aware that since the anonymous is a virtual user it is not granted any roles. So allowing users that are not granted the role:ROLE_CMS_INTERNAL_VIEWER 
				// to view only published content objects is sufficient and we do not actually need to check whether the user is the anonymous.


				// However we explicitly check if the user identity is the anonymous in order to prevent cases where some administrator by mistake or on purpose 
				// registers the anonymous as a real user and assigns it the role ROLE_CMS_INTERNAL_VIEWER. 
				// This would result in letting all not logged in Internet users to view internal unpublished content. So we introduce the extra rule that 
				// anonymous is only viewing published objects despite any roles that may have been assigned to it.

				//if (StringUtils.equals(userId, IdentityPrincipal.ANONYMOUS) ||
				if (! AbstractSecureContentObjectAspect.userHasRole(activeSecurityContext,CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER))){


					//Any user that is NOT GRANTED the role:ROLE_CMS_INTERNAL_VIEWER can access ONLY PUBLISHED or PublishedAndArchived content objects
					//StringProperty profileContentObjectStatusProperty = (StringProperty)contentObject.getCmsProperty("profile.contentObjectStatus");

					//if (profileContentObjectStatusProperty == null || profileContentObjectStatusProperty.hasNoValues()){
					if (! contentObjectNode.hasProperty("profile/contentObjectStatus")){
						logger.debug("User {} has not been granted access to content object {} because she has not been granted role ROLE_CMS_INTERNAL_VIEWER and " +
								" content object status is either null or has no values", userId, contentObjectIdOrSystemName);
						return generateEmptyOutcome(contentObjectOutput);
					}
				
					//String profileContentObjectStatus = profileContentObjectStatusProperty.getSimpleTypeValue();
					String profileContentObjectStatus = contentObjectNode.getProperty("profile/contentObjectStatus").getString();

					if (StringUtils.equals(ContentObjectStatus.published.toString(), profileContentObjectStatus) || 
							StringUtils.equals(ContentObjectStatus.publishedAndArchived.toString(), profileContentObjectStatus)){
						logger.debug("User {} has been granted access to content object {} because she has not been granted role ROLE_CMS_INTERNAL_VIEWER but " +
								" content object status is {}", new Object[]{userId, contentObjectIdOrSystemName, profileContentObjectStatus});
						return proceedingJoinPoint.proceed(methodParameters);
					}

					logger.debug("User {} has not been granted access to content object {} because she has not been granted role ROLE_CMS_INTERNAL_VIEWER and " +
							" content object status '{}' is not published or published and archived", new Object[]{userId, contentObjectIdOrSystemName, profileContentObjectStatus});

					return generateEmptyOutcome(contentObjectOutput);

				}
				else if (! AbstractSecureContentObjectAspect.userHasRole(activeSecurityContext, CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN))){ // for USER with ROLE_ADMIN we do not impose any security constraint

					// we will generate criteria to generate the following security restriction
					// (@betaconcept:OwnerCmsIdentifier = UUIDOfUserExecutingTheQuery OR
					// betaconcept:CanBeReadBy = 'REPOSITORY' OR (betaconcept:CanBeReadBy != 'NONE'
					// AND (betaconcept:CanBeReadBy = "USR_" + userId OR
					// betaconcept:CanBeReadBy = "GRP_" + userGroupId1 OR
					// betaconcept:CanBeReadBy = "GRP_" + userGroupId2 ....)))


					//User has role ROLE_CMS_INTERNAL_VIEWER

					//Check if user owns this content object
					//RepositoryUser owner = contentObject.getOwner();
				
					Subject subject = activeSecurityContext.getSubject();
				
					if (subject != null && CollectionUtils.isNotEmpty(subject.getPrincipals(RepositoryUserIdPrincipal.class)) 
							&& contentObjectNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName())){
						RepositoryUserIdPrincipal ownerIdPrincipal = subject.getPrincipals(RepositoryUserIdPrincipal.class).iterator().next();

						String ownerId = contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();
						
						if (StringUtils.equals(ownerId, ownerIdPrincipal.getName())){
							logger.debug("User {} has been granted access to content object {} because she owns the content object", userId, contentObjectIdOrSystemName);
							return proceedingJoinPoint.proceed(methodParameters);
						}
					}

					//TODO : In case RepositoryUserIdPrincipal is not available, is it safe to just do the following check
					//  owner.getExternalId() == userId

					//User does not own content object. Check access right defined in property
					//accessibility.canBeReadBy
					//StringProperty accessibilityCanBeReadByProperty = (StringProperty) contentObject.getCmsProperty("accessibility.canBeReadBy");

					//if (accessibilityCanBeReadByProperty == null || accessibilityCanBeReadByProperty.hasNoValues()){
					if (! contentObjectNode.hasProperty("accessibility/canBeReadBy")){
						logger.debug("User {} has not been granted access to content object {} because although she does not own content objects and  " +
								" content object does not have any value to property accessibility.canBeReadBy ", userId, contentObjectIdOrSystemName);
						return generateEmptyOutcome(contentObjectOutput);
					}
				
					//List<String> canBeReadBy = accessibilityCanBeReadByProperty.getSimpleTypeValues();
					Value[] canBeReadByArr = contentObjectNode.getProperty("accessibility/canBeReadBy").getValues(); 
					
					List<String> canBeReadBy = new ArrayList<String>();
					for (Value value : canBeReadByArr){
						canBeReadBy.add(value.getString());
					}
					
					//If canBeReadBy contains REPOSITORY value then access is granted
					if (canBeReadBy.contains(ContentAccessMode.ALL.toString())){
						logger.debug("User {} has been granted access to content object {} because although she does not own content object, " +
								" content object property accessibility.canBeReadBy contains value REPOSITORY :{}", new Object[]{userId, contentObjectIdOrSystemName, canBeReadBy.toString()});
						return proceedingJoinPoint.proceed(methodParameters);
					}
				
					//If canBeReadBy contains NONE value then access is denied
					if (canBeReadBy.contains(ContentAccessMode.NONE.toString())){
						logger.debug("User {} has not been granted access to content object {} because she does not own content object and  " +
								" content object property accessibility.canBeReadBy contains value NONE :{}", new Object[]{userId, contentObjectIdOrSystemName, canBeReadBy.toString()});
						return generateEmptyOutcome(contentObjectOutput);
					}
				
					//canBeReadBy contains neither REPOSITORY nor NONE
					//access is granted only if either to any of the user groups or explicitly to the user itself
					// so we add the user id into the list of group ids. All ids are appropriately prefixed by either URS_ or GRP_ to
					// distinguish between user and group ids

					// Security in each content object is defined by four lists stored as part of each object (i.e. a special complex property of each content object).
					// 	The four lists define which user or role (role may be a role group also) can respectively read, update, delete and tag the object.
					// 	Each of the four lists inside each object contain a mixed set of the userIds and Roles  which 
					// So we get the user roles prefixed by "GRP_" in order to discriminate them from user ids which are prefixed with "USR_"
					List<String> prefixedRoles = activeSecurityContext.getAllRoles();

					prefixedRoles.add(userId);
				
					for (String prefixedRole : prefixedRoles)
					{
						if (canBeReadBy.contains(prefixedRole))
						{
							logger.debug("User {} has been granted access to content object {} because although she does not own content object,   " +
								" content object property accessibility.canBeReadBy contains role {} ", new Object[]{userId, contentObjectIdOrSystemName, prefixedRole});
							return proceedingJoinPoint.proceed(methodParameters);
						}
					}
				
					logger.debug("User {} has not been granted access to content object {} because she does not own content object and  " +
						" content object property accessibility.canBeReadBy does not contain any role which has been assigned to user. \nAccessibility.CanBeReadBy values {}" +
							"\n Granted Roles to user {}", new Object[]{userId, contentObjectIdOrSystemName, canBeReadBy, prefixedRoles});
					return generateEmptyOutcome(contentObjectOutput);
				}
				else{
					logger.debug("User {} has been granted access to content object {} because she has been granted role ROLE_ADMIN ", 
							new Object[]{userId, contentObjectIdOrSystemName});
					return proceedingJoinPoint.proceed(methodParameters);
				}
			}
			catch(CmsException e)
			{
				throw e;
			}
			catch (Throwable e) {
				throw new CmsException(e);
			}
		}
		else{
			//No point to proceed to actual method since no content object id is provided
			logger.debug("No content object exists with id {} therefore no restrictions are imposed", contentObjectIdOrSystemName);
			return generateEmptyOutcome(contentObjectOutput);
		}
	
	}
	
	private boolean contentObjectIdIsNotNull(String contentObjectId) {
		return StringUtils.isNotBlank(contentObjectId);
	}


	@Around(" searchContentObjects() &&  args(contentObjectCriteria,output)")
	public Object addSecurityCriteriaToContentObjectCriteria(ProceedingJoinPoint proceedingJoinPoint, ContentObjectCriteria contentObjectCriteria, ResourceRepresentationType output) {
		return addSecurityCriteria(proceedingJoinPoint, contentObjectCriteria, output);
	}
		
	@Around("( searchContentObjectsAndExportToJson() || searchContentObjectsAndExportToXml() || searchContentObjects() ) &&  args(contentObjectCriteria)")
	public Object addSecurityCriteriaToContentObjectCriteria(ProceedingJoinPoint proceedingJoinPoint, ContentObjectCriteria contentObjectCriteria) {
		return addSecurityCriteria(proceedingJoinPoint, contentObjectCriteria, null);
	}
	
	
	private Object addSecurityCriteria(ProceedingJoinPoint proceedingJoinPoint, ContentObjectCriteria contentObjectCriteria, 
			ResourceRepresentationType output) {
		
		long start = System.currentTimeMillis();
		
		ContentObjectCriteria localCopyOfContentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		//if (logger.isDebugEnabled())
		logger.debug("INTERCEPTION of CONTENT SEARCH ACTIVATED");
		
		try{
			SecurityContext activeSecurityContext = AstroboaClientContextHolder.getActiveSecurityContext();
			
			if (activeSecurityContext == null){
				logger.warn("No security context found.");
				throw new NonAuthenticatedOperationException();
			}

			String userId = activeSecurityContext.getIdentity();
			if (StringUtils.isBlank(userId)){
				logger.error("no authenticated user found. Please authenticate before using repository services");
				throw new NonAuthenticatedOperationException();
			}
			else { // a user is already authenticated 
				//if (logger.isDebugEnabled())	
				logger.debug("user: {} is currently authenticated", userId);
				
				// Security in each content object is defined by four lists stored as part of each object (i.e. a special complex property of each content object).
				// The four lists define which user or role (role may be a role group also) can respectively read, update, delete and tag the object.
				// Each of the four lists inside each object contain a mixed set of the userIds and Roles  which 
				// So we get the user roles prefixed by "GRP_" in order to discriminate them from user ids which are prefixed with "USR_"
				//List<String> prefixedRoles = getPrefixedRoles(activeSecurityContext.getAllRoles());
				
				// We should keep a local copy of content object criteria into which we will add the security criteria so that the original criteria object will not be affected.
				// In this way the security operations meant to be provided transparently by this aspect will remain transparent to the user of repository services.  
				contentObjectCriteria.copyTo(localCopyOfContentObjectCriteria);
				
				addSecurityCriteriaToContentObjectCriteria(localCopyOfContentObjectCriteria, userId, activeSecurityContext.getAllRoles(), activeSecurityContext);
			}
			
			
			
			if (logger.isDebugEnabled())	
				logger.debug("INSIDE SecutiryContentServiceAspect, method addSecurityCriteriaToContentObjectCriteria took  " + (System.currentTimeMillis() - start) + " ms");
			
			if (output == null){
				return proceedingJoinPoint.proceed(new Object[]{localCopyOfContentObjectCriteria});
			}
			else{
				return proceedingJoinPoint.proceed(new Object[]{localCopyOfContentObjectCriteria, output});
			}
		}
		catch(CmsException e)
		{
			throw e;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
	}
	
	
	private void addSecurityCriteriaToContentObjectCriteria(ContentObjectCriteria localCopyOfContentObjectCriteria, String userId, List<String> prefixedRoles, SecurityContext activeSecurityContext) {
	
		// if the authenticated user has not been granted the role: ROLE_CMS_INTERNAL_VIEWER 
		// then we allow her to read only published content objects i.e. those that their status is equal to "published" or "publishedAndArchived".
		// As of today a published content object overrules any "read" security option to prevent complexities in security rule handling 
		// and remove the extra effort required for the publisher of content objects 
		// It is not very convenient to require to change the "read" security option of published content objects to "ALL" to allow to be read by REPOSITORY 
		// and then when publication status ends revert back to previous security settings. 
		// Additionally "ALL" should be interpreted as "REPOSITORY PHYSICAL PERSONS or REPOSITORY USERS WHICH ARE EXPLICITLY GRANTED THE PERMISSION TO VIEW THE REPOSITORY, 
		// i.e. those in role ROLE_CMS_INTERNAL_VIEWER".
		// Anonymous is not an actual user registered in the identity store. It is a convention introduced in order to cope with the security rule that we always need some user 
		// in order to permit access to content. So for any user that tries to see the repository without logging in, the front-end system, e.g. the web application, should
		// silently perform a virtual login as the anonymous user.
		//
		// Through the above convention it becomes really easy to publish and un-publish content objects. 
		// The idea is that if someone publishes a content object then she implicitly removes any read restrictions.
		// All other restrictions apply and furthermore read restrictions are still there and remain valid when status is not set to "published" any more.
		// We may revisit this convention if the use of the repository reveals another way of interpreting anonymous requests and published objects
		//
		// Be aware that since the anonymous is a virtual user it is not granted any roles. So allowing users that are not granted the role:ROLE_CMS_INTERNAL_VIEWER 
		// to view only published content objects is sufficient and we do not actually need to check whether the user is the anonymous.
		
		
		// However we explicitly check if the user identity is the anonymous in order to prevent cases where some administrator by mistake or on purpose 
		// registers the anonymous as a real user and assigns it the role ROLE_CMS_INTERNAL_VIEWER. 
		// This would result in letting all not logged in Internet users to view internal unpublished content. So we introduce the extra rule that 
		// anonymous is only viewing published objects despite any roles that may have been assigned to it.
		
		//if (StringUtils.equals(userId, IdentityPrincipal.ANONYMOUS) ||
			if (! activeSecurityContext.hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER))){
			
			
			//The anonymous user or any other user that is NOT GRANTED the role:ROLE_CMS_INTERNAL_VIEWER can access ONLY PUBLISHED content objects
			//In case where criteria already contains criterion that requires content object status to equal "publishedAndArchived", 
			// there is no need to add criterion about published
			Criterion contentObjectIsPublishedCriterion = null;
			if (! criteriaContainsContentObjectStatusArchivedCriterion(localCopyOfContentObjectCriteria.getCriteria())){
				contentObjectIsPublishedCriterion = CriterionFactory.equals("profile.contentObjectStatus", ContentObjectStatus.published.toString());
				localCopyOfContentObjectCriteria.addCriterion(contentObjectIsPublishedCriterion);
			}
			
			
			//TODO: we should check whether user has already include a criterion concerning content object status and prohibit the query execution if a status other than published has been requested
			// additionally if there s already a criterion asking for published content objects we should keep it only once and not add it again
			
			// generate a warning that only published content objects have been returned
			//logger.warn("The authenticated user is the 'anonymous' user. Be aware that the queries issued by anonymous user return only published content objects");
		}
		else if (! AbstractSecureContentObjectAspect.userHasRole(activeSecurityContext, CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN))){ // for USER with role ROLE_ADMIN we do not impose any security constraint
			// we will generate criteria to generate the following security restriction
			// (@betaconcept:OwnerCmsIdentifier = UUIDOfUserExecutingTheQuery OR
			// betaconcept:CanBeReadBy = 'REPOSITORY' OR (betaconcept:CanBeReadBy != 'NONE'
			// AND (betaconcept:CanBeReadBy = "USR_" + userId OR
			// betaconcept:CanBeReadBy = "GRP_" + userGroupId1 OR
			// betaconcept:CanBeReadBy = "GRP_" + userGroupId2 ....)))

			Criterion ownerCriterion = null;
			RepositoryUserIdPrincipal ownerIdPrincipal = null;
			
			Subject subject = activeSecurityContext.getSubject();
			
			if (subject != null && CollectionUtils.isNotEmpty(subject.getPrincipals(RepositoryUserIdPrincipal.class))){
				ownerIdPrincipal = subject.getPrincipals(RepositoryUserIdPrincipal.class).iterator().next();
				ownerCriterion = CriterionFactory.equals("owner", ownerIdPrincipal.getName());
			}
			
			Criterion canBeReadByAllCriterion = CriterionFactory.equals("accessibility.canBeReadBy", ContentAccessMode.ALL.toString());
			Criterion canBeReadByNotEQNoneCriterion = CriterionFactory.notEquals("accessibility.canBeReadBy", ContentAccessMode.NONE.toString());
			
			// we check whether read access is permitted either to any of the user groups or explicitly to the user itself
			// so we add the user id into the list of group ids. All ids are appropriately prefixed by either URS_ or GRP_ to
			// distinguish between user and group ids
			prefixedRoles.add(userId);
			
			Criterion canBeReadByUserHerselfOrUserGroupsCriterion = CriterionFactory.equals("accessibility.canBeReadBy", Condition.OR, prefixedRoles);
			// build query parts
			Criterion firstPart = null;
			if (ownerCriterion != null){
				firstPart = CriterionFactory.or(ownerCriterion, canBeReadByAllCriterion);
			}
			else{
				firstPart = canBeReadByAllCriterion;
			}
			
			Criterion secondPart = CriterionFactory.and(canBeReadByNotEQNoneCriterion, canBeReadByUserHerselfOrUserGroupsCriterion);
			
			// connect the parts
			Criterion securityCriterion = CriterionFactory.or(firstPart, secondPart);
			
			// add the security criteria to user provided search criteria
			localCopyOfContentObjectCriteria.addCriterion(securityCriterion);
			
		}
	}

	private boolean criteriaContainsContentObjectStatusArchivedCriterion(
			List<Criterion> criteria) {
		
		if (CollectionUtils.isNotEmpty(criteria)){
			for (Criterion criterion : criteria){
				
				String xpath = criterion.getXPath();
				
				if (StringUtils.contains(xpath, "profile/@contentObjectStatus = '"+ContentObjectStatus.publishedAndArchived.toString()+"'")){
					return true;
				}
			}
		}
		return false;
	}

	private <T> T generateEmptyOutcome(
			ResourceRepresentationType<T> contentObjectOutput) {
		if (contentObjectOutput != null && contentObjectOutput.equals(ResourceRepresentationType.CONTENT_OBJECT_LIST)){
			return (T) new CmsOutcomeImpl<T>(0, 0, 0);
		}
		else{
			return null;
		}
	}

	
}
