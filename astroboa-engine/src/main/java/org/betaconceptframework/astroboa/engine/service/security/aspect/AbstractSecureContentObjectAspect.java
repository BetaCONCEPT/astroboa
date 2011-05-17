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

import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.security.RepositoryUserIdPrincipal;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.service.security.exception.NonAuthenticatedOperationException;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
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
public abstract class AbstractSecureContentObjectAspect extends JcrDaoSupport{

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	public static SecurityContext retrieveSecurityContext() {
		SecurityContext activeSecurityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		
		if (activeSecurityContext == null){
			throw new NonAuthenticatedOperationException();
		}

		String userId = activeSecurityContext.getIdentity();
		if (StringUtils.isBlank(userId)){
			throw new NonAuthenticatedOperationException();
		}
		
		return activeSecurityContext;
	}
	
	public static boolean userHasRole(SecurityContext activeSecurityContext, String role) {
		return activeSecurityContext.hasRole(role);
	}

	protected void checkContentObjectIsNotNull(Object contentObject, String userId) {
		if (contentObject == null){
			
			if (userId == null){
				SecurityContext activeSecurityContext = retrieveSecurityContext();
				if (activeSecurityContext != null){
					userId = activeSecurityContext.getIdentity();
				}
			}
			
			throw new CmsException("Cannot save null content object for user "+userId);
		}
	}
	
	protected String retrieveOwnerFromContentObjectNode(Node contentObjectNode) throws RepositoryException {

		//Retrieve owner of content object node
		if (! contentObjectNode.hasProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()))
		{
			//THIS SHOULD NEVER HAPPEN
			logger.error("Found content object node "+ contentObjectNode.getPath() + " without property "+ CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()+
			" This means that a content object was saved without owner...");
			throw new CmsException("System error");
		}

		return contentObjectNode.getProperty(CmsBuiltInItem.OwnerCmsIdentifier.getJcrName()).getString();

	}
	
	protected Node retrieveContentObjectNodeForContentObject(
			String contentObjectId) throws RepositoryException {

		return cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(getSession(), contentObjectId);
	}

	public Node getContentObjectNodeByIdOrSystemName(String contentObjectIdOrSystemName){
		try{
			Node contentObjectNode = null;
			
			if (CmsConstants.UUIDPattern.matcher(contentObjectIdOrSystemName).matches()){
				contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(getSession(), contentObjectIdOrSystemName);

				if (contentObjectNode != null){
					return contentObjectNode;
				}
			}
			else{
				ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
				contentObjectCriteria.addSystemNameEqualsCriterion(contentObjectIdOrSystemName);
				contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
				contentObjectCriteria.setOffsetAndLimit(0, 1);
				
				CmsQueryResult nodes = cmsQueryHandler.getNodesFromXPathQuery(getSession(), contentObjectCriteria, true);
				
				if (nodes.getTotalRowCount() > 0){
					return ((NodeIterator) nodes.getNodeIterator()).nextNode();
				}
			}
			
			return null;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}
	protected String retrieveRepositoryUserIdForLoggedInUser(
			SecurityContext activeSecurityContext, String userId,
			Node contentObjectNode) throws RepositoryException {

		if (activeSecurityContext.getSubject() != null)
		{
			Set<RepositoryUserIdPrincipal> repositoryUserIdPrincipalSet = activeSecurityContext.getSubject().getPrincipals(RepositoryUserIdPrincipal.class);
		
			if (repositoryUserIdPrincipalSet != null && ! repositoryUserIdPrincipalSet.isEmpty())
			{
				return repositoryUserIdPrincipalSet.iterator().next().getName();
			}
		}

		return null;
	}


}
