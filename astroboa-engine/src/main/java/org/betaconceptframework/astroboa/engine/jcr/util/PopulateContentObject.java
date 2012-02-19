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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.Arrays;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.security.auth.Subject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.exception.CmsNonUniqueContentObjectSystemNameException;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.RepositoryUserIdPrincipal;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PopulateContentObject {

	@Autowired
	private PrototypeFactory prototypeFactory;
	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;
	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	@Autowired
	private RepositoryUserDao repositoryUserDao;
	
	private Node contentObjectNode;
	private ContentObject contentObject;
	private Session session;
	private SaveMode saveMode;
	private Context context;
	
	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	
	public void setContext(Context context) {
		this.context = context;
	}

	public void setSaveMode(SaveMode saveMode) {
		this.saveMode = saveMode;
	}

	public void setContentObjectNode(Node contentObjectNode) {
		this.contentObjectNode = contentObjectNode;
	}

	public void setContentObject(ContentObject contentObject) {
		this.contentObject = contentObject;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void populate(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		try{
			//Check that type is the same, if any
			if (contentObjectNode.hasProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName())){
				String contentObjectTypeInJcrNode = contentObjectNode.getProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName()).getString();
				if (!contentObjectTypeInJcrNode.equals(contentObject.getContentObjectType()))
					throw new CmsException("Content object's jcr node "+ contentObjectNode.getPath() +" is of type "+ contentObjectTypeInJcrNode +
							" where as content object is fo type "+contentObject.getContentObjectType());
			}
			else{
				//Set content object's type
				contentObjectNode.setProperty(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), contentObjectTypeDefinition.getName());
			}
			
			populateSystemName();
			
			//Set content object's owner
			populateOwner();
			
			//Populate Child Properties
			PopulateComplexCmsProperty contentObjectPropertyContainerPopulateTask = prototypeFactory.newPopulateComplexCmsProperty();
			contentObjectPropertyContainerPopulateTask.setComplexProperty(contentObject.getComplexCmsRootProperty());
			contentObjectPropertyContainerPopulateTask.setComplexPropertyDefinition(contentObject.getComplexCmsRootProperty().getPropertyDefinition());
			contentObjectPropertyContainerPopulateTask.setSaveMode(saveMode);
			contentObjectPropertyContainerPopulateTask.setSession(session);
			contentObjectPropertyContainerPopulateTask.setComplexPropertyNode(contentObjectNode);
			contentObjectPropertyContainerPopulateTask.setContentObjectNodeUUID(contentObjectNode.getUUID());
			contentObjectPropertyContainerPopulateTask.setContext(context);
			contentObjectPropertyContainerPopulateTask.populate();
			
			if ( StringUtils.isBlank(((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).getContentObjectNodeUUID())){
				((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).setContentObjectNodeUUID(contentObjectNode.getUUID());
			}
			
			if ( StringUtils.isBlank(((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).getPropertyContainerUUID())){
				((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).setPropertyContainerNodeUUID(contentObjectNode.getUUID());
			}

		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e){
			throw new CmsException(e);
		}
	}

	private void populateSystemName() throws ValueFormatException,
			VersionException, LockException, ConstraintViolationException,
			RepositoryException {
		
		//Validate system name first
		String systemName = contentObject.getSystemName();
		
		//System name is NULL or an empty string 
		if (StringUtils.isBlank(systemName)){

			// User did not specify a system name. Do not create one if object already has one
			if (systemName == null){
				
				//If object already has a system name, retrieve value from the repository and update object
				if (contentObjectNode.hasProperty(CmsBuiltInItem.SystemName.getJcrName())){
					//Update object with existing system name and return
					systemName = contentObjectNode.getProperty(CmsBuiltInItem.SystemName.getJcrName()).getString();

					//Maybe unnecessary check.
					if (! context.getCmsRepositoryEntityUtils().isValidSystemName(systemName)){
						throw new RepositoryException("Existing Object system name "+systemName+" is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP);
					}

					contentObject.setSystemName(systemName);

					return;
				}
			}
			
			//System name is empty. This way user specifies that she wants a new system name to be created 
			//Generate a system name
			//1. Check profile.title property from the object and if none if found, check 
			//if title
			systemName = retrieveContentObjectProfileTitle();
			
			if (StringUtils.isBlank(systemName)){
				//No profile.title found. Even though this property is considered
				//mandatory and normally this should never happen
				//system name must not be blank. Therefore we create 
				//a value using path to content object node plus its UUID
				systemName = JcrNodeUtils.getYearMonthDayPathForContentObjectNode(contentObjectNode)+contentObjectNode.getUUID();
			}
			
			//Filter created system name
			//Replace anything that is not valid according to SystemName regular expression
			systemName = context.getCmsRepositoryEntityUtils().fixSystemName(systemName);
			
			contentObject.setSystemName(systemName);
		}
		

		if (! context.getCmsRepositoryEntityUtils().isValidSystemName(systemName)){
			throw new RepositoryException("Content Object system name "+systemName+" is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP);
		}
		
		//Finally check that system name is unique across all content objects
		

		if (foundAtLeastOneMoreContentObjectWithSameSystemName(systemName)){
				//One or more content objects were found with the same systemName.
				//SystemName will be the same with content object id plus some characters from title
				systemName = context.getCmsRepositoryEntityUtils().fixSystemName(retrieveContentObjectProfileTitle()+"-"+contentObject.getId());
				
				//Now run again query with new value
				if (foundAtLeastOneMoreContentObjectWithSameSystemName(systemName)){
					throw new CmsNonUniqueContentObjectSystemNameException("Another content object exists with system name "+ systemName);
				}
				else{
					logger.warn("ContentObject {} was saved with system name {} because provided systemName {} was found in another content object", 
							new Object[]{contentObject.getId(), systemName, contentObject.getSystemName()});
					
					contentObject.setSystemName(systemName);
				}
		}
		
		
		JcrNodeUtils.addSimpleProperty(SaveMode.UPDATE, contentObjectNode, 
				CmsBuiltInItem.SystemName, systemName, session.getValueFactory(), ValueType.String);
		
	}
	
	private boolean foundAtLeastOneMoreContentObjectWithSameSystemName(String systemName) throws RepositoryException{
		/*
		 * Criterion equalsIgnoreCaseCriterion = CriterionFactory.equalsCaseInsensitive(CmsBuiltInItem.SystemName.getJcrName(), systemName.toLowerCase());
		 * Criterion likeIgnoreCaseCriterion = CriterionFactory.likeCaseInsensitive(CmsBuiltInItem.SystemName.getJcrName(), systemName.toLowerCase());
		 * coCriteria.addCriterion(CriterionFactory.or(equalsIgnoreCaseCriterion, likeIgnoreCaseCriterion));
		 */
		ContentObjectCriteria coCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		coCriteria.addSystemNameEqualsCriterionIgnoreCase(systemName);
		
		if (contentObject.getId() != null){
			coCriteria.addIdNotEqualsCriterion(contentObject.getId());
		}
		//We are only interested in result count
		coCriteria.setOffsetAndLimit(0, 0);
		
		
		CmsQueryResult nodesWithSameSystemName = cmsQueryHandler.getNodesFromXPathQuery(session, coCriteria);
		
		return nodesWithSameSystemName != null && nodesWithSameSystemName.getTotalRowCount() > 0;
	}

	private String retrieveContentObjectProfileTitle()
			throws RepositoryException, ValueFormatException,
			PathNotFoundException 
		{
		
		if (contentObject.getComplexCmsRootProperty().isChildPropertyLoaded("profile.title") 
				&& contentObject.getCmsProperty("profile.title") != null && 
				((StringProperty)contentObject.getCmsProperty("profile.title")).hasValues())
		{
			return ((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue();
		}
		else
		{
			//Profile title has not been loaded. Check jcr node
			if (contentObjectNode.hasProperty("profile/title"))
			{
				return contentObjectNode.getProperty("profile/title").getString();
			}
		}
		
		return "";
	}
	
	private void populateOwner() throws RepositoryException{
		
		RepositoryUser owner = contentObject.getOwner();
		
		if (owner == null){
			
			owner = retrieveRepositoryUserForActiveClient();

			//If a new content object is saved then load RepositoryUser which represents
			//the connected user, to be the owner of the object. If no such repository user is found
			//then throw an exception
			if (SaveMode.INSERT == saveMode){
				contentObject.setOwner(owner);
			}
			
			//Special case. In case of an update check if existing owner is the same
			//with the user performing the update. If so do not throw an exception
			else {
				
					String existingOwnerId = null;
					if (contentObjectNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName())){
						existingOwnerId = contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();
					}

					if (StringUtils.equals(owner.getId(), existingOwnerId)){
						
						contentObject.setOwner(owner);
						
						//In this case there is nothing else to be done.
						return;
					}
					else{
						
						if (existingOwnerId == null){
							//This should never happen
							logger.warn("Object "+context.getCmsRepositoryEntityUtils().nodeIdentity(contentObjectNode) + " does not have an owner. SYSTEM will be the owner");
							owner = repositoryUserDao.getSystemRepositoryUser();
							contentObject.setOwner(owner);
						}
						else{
						
							//No owner has been provided and active user is not the owner of the object.
							//However, active user has the permissions to update the object, therefore we allow save
							//and we set the proper owner instance
							Node repUsernode = context.retrieveNodeForRepositoryUser(existingOwnerId);
							owner = repositoryUserDao.renderRepositoryUserFromNode(repUsernode, session, null, new HashMap<String, CmsRepositoryEntity>());
							contentObject.setOwner(owner);
							
							//In this case there is nothing else to be done.
							return;

						}
						
					}
			}
		}

		//First of all we try to locate owner id;
		
		//Owner must exist in repository
		if (owner.getId() == null){
			if (StringUtils.equals(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID, owner.getExternalId())){
				//Owner is SYSTEM user. Retrieve property identifier
				RepositoryUser systemUser = repositoryUserDao.getSystemRepositoryUser();

				if (!StringUtils.equals(systemUser.getId(), owner.getId())){
					owner = systemUser;
				}
			}
			else if (StringUtils.isBlank(owner.getExternalId())){
				throw new RepositoryException("Content Object Owner has no id nor external id");
			}
			else{
				//Search in cache
				Node userNode = context.getNodeFromCache(owner.getExternalId());

				if (userNode == null){
					//try to locate using search criteria
					RepositoryUser repUser = repositoryUserDao.getRepositoryUser(owner.getExternalId());

					if (repUser == null){
						throw new RepositoryException("Content Object Owner with external id "+ owner.getExternalId() + " does not exist");
					}
					else{
						owner = repUser;
						//Cache node
						context.retrieveNodeForRepositoryUser(repUser.getId());
					}
				}
				else{
					owner.setId(context.getCmsRepositoryEntityUtils().getCmsIdentifier(userNode));
				}
			}
		}

		String newOwnerCmsIdentifier = owner.getId();
		
		String existingOwnerId = null;
		if (contentObjectNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName())){
			existingOwnerId = contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();
		}
		
		//Update owner only if value does not exist or it is different that the one provided
		if (! StringUtils.equals(newOwnerCmsIdentifier, existingOwnerId)){
			
			//Check that new owner does exist
			if (context.retrieveNodeForRepositoryUser(newOwnerCmsIdentifier) == null){
				
				//Special case it may be user SYSTEM. in that case we do not throw any exception
				if (StringUtils.equals(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID, owner.getExternalId())){
					owner = repositoryUserDao.getSystemRepositoryUser();
					
					//if ids match do not continue
					if (StringUtils.equals(owner.getId(), existingOwnerId)){
						return ;
					}
				}
				else{
					throw new CmsException("RepositoryUser with id "+ newOwnerCmsIdentifier + " and externalId "+ owner.getExternalId() + " and label "+ 
						owner.getLabel() + " does not exist in repository "+ AstroboaClientContextHolder.getActiveRepositoryId());
				}
			}
			
			EntityAssociationUpdateHelper<RepositoryUser> entityAssociationUpdateHelper = 
				new EntityAssociationUpdateHelper<RepositoryUser>(session, cmsRepositoryEntityAssociationDao, context);
			
			entityAssociationUpdateHelper.setReferrerCmsRepositoryEntityNode(contentObjectNode);
			entityAssociationUpdateHelper.setReferrerPropertyName(CmsBuiltInItem.OwnerCmsIdentifier);
			entityAssociationUpdateHelper.setReferrerPropertyNameMultivalue(false);
			entityAssociationUpdateHelper.setValuesToBeAdded(Arrays.asList(owner));
			entityAssociationUpdateHelper.update();
		}
		
	}
	
	private RepositoryUser retrieveRepositoryUserForActiveClient() throws RepositoryException{

		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();

		String repositoryUserId = null;
		String activeUsername = null;
		
		if (securityContext != null){
			
			Subject subject = securityContext.getSubject();
			
			if (subject != null && CollectionUtils.isNotEmpty(subject.getPrincipals(RepositoryUserIdPrincipal.class))){
				
				repositoryUserId = subject.getPrincipals(RepositoryUserIdPrincipal.class).iterator().next().getName();

				Node repositoryUserNode = context.retrieveNodeForRepositoryUser(repositoryUserId);
				
				return repositoryUserDao.renderRepositoryUserFromNode(repositoryUserNode, session, null, null);
			}
			else{
				//Could not locate repository user id. Try with user name
				activeUsername = securityContext.getIdentity();

				return repositoryUserDao.getRepositoryUser(activeUsername);
				
			}
		}
		
		throw new CmsException("Could not locate a RepositoryUser neither with  identifier "+repositoryUserId+" nor with externalId "+activeUsername);
		
	}
}
