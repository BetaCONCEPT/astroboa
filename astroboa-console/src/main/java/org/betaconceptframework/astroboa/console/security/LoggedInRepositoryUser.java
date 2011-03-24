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

package org.betaconceptframework.astroboa.console.security;


import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.DisplayNamePrincipal;
import org.betaconceptframework.astroboa.api.security.PersonUserIdPrincipal;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.TopicComparator;
import org.betaconceptframework.astroboa.console.commons.TopicComparator.OrderByProperty;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.bean.AbstractBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.security.Identity;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class LoggedInRepositoryUser extends AbstractBean {
	
	private static final long serialVersionUID = 1L;
	
	// injected beans
	private CMSUtilities cmsUtilities;
	private RepositoryUserService repositoryUserService;

	private String displayName;
	private String personId;
	// specifies whether this user's identity is managed by the astroboa provided IDP module or some external Identity Provisioning module
	private boolean externallyManagedIdentity;
	private RepositoryUser repositoryUser;

	private String identity;
	
	public void reset(){
		displayName = null;
		personId = null;
		externallyManagedIdentity = false;
		repositoryUser = null;
		identity = null;
	}
	
	public RepositoryUser getRepositoryUser() {
		if (this.repositoryUser == null) {
			try {
				RepositoryUser retreivedRepositoryUser = getCmsUtilities().findLoggedInRepositoryUser(JSFUtilities.getLocaleAsString());
				if (retreivedRepositoryUser == null) { 
					
					// There is a possibility that RepositoryUser was not found because her external id has been registered with a previous astroboa version 
					// and so it does not correspond to the Identity Principal. In such a case the Repository User Ids should be migrated and we throw a relevant Exception.
					// We can locate such cases because the JAAS module stores in the subject the PersonIdPrincipal which corresponds to what was stored as the External User Id in older astroboa
					// versions. If such a principal object exists and we can locate the Repository User with this id then we will try to fix it.
					/*Set<PersonIdPrincipal> personIdPrincipals = Identity.instance().getSubject().getPrincipals(PersonIdPrincipal.class);
					if (CollectionUtils.isNotEmpty(personIdPrincipals) && 
							StringUtils.isNotBlank(personIdPrincipals.iterator().next().getName())) {
						RepositoryUser oldUser = getCmsUtilities().findRepositoryUserByUserId(personIdPrincipals.iterator().next().getName(),JSFUtilities.getLocaleAsString());
						if (oldUser != null) {
							cmsUtilities.migrateRepositoryUserExternalIds();
							// try again
							retreivedRepositoryUser = cmsUtilities.findLoggedInRepositoryUser(JSFUtilities.getLocaleAsString());
							if (retreivedRepositoryUser == null) {
								throw new CmsException("Found a Repository User with an external id that was used by old astroboa versions. " +
								"An attempt was made to migrate the Repository Users to have the appropriate external ids. " +
								"It seems that the problem persists. Please try to fix it manually");
							}
							else {
								this.repositoryUser = retreivedRepositoryUser;
								logger.warn("Found a Repository User with an external id that was used by old astroboa versions. " +
								"An attempt was made to migrate the Repository Users to have the appropriate external ids. " +
								"It seems that the attempt was successful. However please inspect and verify that external ids are correct now");
								return this.repositoryUser;
							}
						}
					}*/
					
					// this is the first time the user visits the repository. Lets create her as a repository user
					RepositoryUser userToBeCreated = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser();
					// the external id of the Repository User is the Identity Principal by which logged in user is known to the IDP that keeps user identities.
					// The JAAS login module configured for astroboa should provide the user's identity inside a principal object of class IdentityPrincipal
					String userExternalId = Identity.instance().getPrincipal().getName();
					userToBeCreated.setExternalId(userExternalId);
					if (userExternalId.equals("anonymous")) // an anonymous user do not search him in user DB
						userToBeCreated.setLabel("anonymous user");
					else {
						 Set<DisplayNamePrincipal> displayNamePrincipals = Identity.instance().getSubject().getPrincipals(DisplayNamePrincipal.class);
						 if (CollectionUtils.isNotEmpty(displayNamePrincipals)) {
							 userToBeCreated.setLabel(displayNamePrincipals.iterator().next().getName());
						 }
						 else {
							 userToBeCreated.setLabel(JSFUtilities.getLocalizedMessage("user.account.display.name.not.available", null));
						 }
					}	

					saveRepositoryUser(userToBeCreated);
					this.repositoryUser =  userToBeCreated;
					displayName = userToBeCreated.getLabel();
				}
				else { 
					this.repositoryUser = retreivedRepositoryUser;
					getDisplayName();
				}
			}
			catch (Exception e) {
				getLogger().error("There was an error while retreiving logged in repository user", e);
				this.repositoryUser = null;
			}
		}
		return this.repositoryUser;
		
	}

	public boolean loggedInUserHasRoleAdmin() {
		return Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN));
	}
	
	/**
	 * Gets the user display name from the subject.
	 * The user display name is the name of the user appropriately formatted for display in web pages.
	 * It also checks whether the corresponding RepositoryUser label matches the display and updates the label appropriately.
	 */
	public String getDisplayName() {
		if (displayName == null) {
			Set<DisplayNamePrincipal> displayNamePrincipals = Identity.instance().getSubject().getPrincipals(DisplayNamePrincipal.class);
			if (CollectionUtils.isNotEmpty(displayNamePrincipals)) {
				displayName = displayNamePrincipals.iterator().next().getName();
			}

			//update RepositoryUser label with current display name
			if (repositoryUser != null && displayName != null && !repositoryUser.getLabel().equals(displayName)) {
				repositoryUser.setLabel(displayName);
				try {
					saveRepositoryUser(repositoryUser);
				} catch (Exception e) {
					logger.error("",e);
					return null;
				}
			}
		}
		return displayName;

	}

	private void saveRepositoryUser(RepositoryUser userToBeCreated) throws Exception {
		/*
		 * In order to be able to save repository user, logged in user must have ROLE_CMS_EDITOR@repositoryId
		 * In case logged in user does not contain this role, then this method will not succeed.
		 * This is a RunAs example. In order to mimic that behavior we explicitly add appropriate role
		 * if not already there and when save succeeds, we remove it from subject
		 */
		boolean roleCmsEditorHasBeenAdded = false;
		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		String roleCmsEditorForActiveRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR);
		//Group rolesGroup = null;
		//Principal rolePrincipal = null;
		
		if (securityContext !=null && ! securityContext.hasRole(roleCmsEditorForActiveRepository))
		{
			//Use method provided by the SecurityContext
			roleCmsEditorHasBeenAdded = securityContext.addRole(roleCmsEditorForActiveRepository);
			/*
			Subject subject = securityContext.getSubject();
			
			if (subject != null){
				Set<Group> groups = subject.getPrincipals(Group.class);

				if (groups != null){
					for (Group group : groups){
						if (group.getName() != null && AstroboaPrincipalName.Roles.toString().equals(group.getName())){

							rolePrincipal = new CmsPrincipal(roleCmsEditorForActiveRepository);
							group.addMember(rolePrincipal);
							rolesGroup = group;
							
							roleCmsEditorHasBeenAdded = true;
							break;
						}
					}
				}
			}*/
		}
		
		try{
			repositoryUserService.saveRepositoryUser(userToBeCreated);
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			if (roleCmsEditorHasBeenAdded)
			{
				securityContext.removeRole(roleCmsEditorForActiveRepository);
				//rolesGroup.removeMember(rolePrincipal);
			}
		}
	}
	
	public String getPersonId() {
		if (personId == null) {
			Set<PersonUserIdPrincipal> personIdPrincipals = Identity.instance().getSubject().getPrincipals(PersonUserIdPrincipal.class);
			if (CollectionUtils.isNotEmpty(personIdPrincipals)) {
				personId = personIdPrincipals.iterator().next().getName();
			}
		}
		
		return personId;
	}
	
	public String getLocalizedLabelForConnectedRepository(){
		if (AstroboaClientContextHolder.getRepositoryContextForActiveClient() != null  && AstroboaClientContextHolder.getRepositoryContextForActiveClient().getCmsRepository() != null){
			return AstroboaClientContextHolder.getRepositoryContextForActiveClient().getCmsRepository().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString());
		}
		
		return null;
	}

	public String getIdentity(){
		if (identity == null) {
			Principal identityPrincipal = Identity.instance().getPrincipal();
			if (identityPrincipal != null) {
				identity = identityPrincipal.getName();
			}
		}
		
		return identity;
	}
	
	public List<Topic> loadUserTagsOrderedByLabel(){
		
		if (getRepositoryUser() == null || getRepositoryUser().getFolksonomy() == null ||
				getRepositoryUser().getFolksonomy().getRootTopics() == null){
			return new ArrayList<Topic>();
		}
		
		List<Topic> userTags = new ArrayList<Topic>(getRepositoryUser().getFolksonomy().getRootTopics());
		
		Collections.sort(userTags, new TopicComparator(JSFUtilities.getLocaleAsString(), OrderByProperty.LABEL));
		
		return userTags;
	}
	
	public boolean isIdentityStoredInThisRepository() {
		if (isExternallyManagedIdentity()) {
			return false;
		}
		else {
			AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
			String currentRepositoryId = clientContext.getRepositoryContext().getCmsRepository().getId();
			String identityStoreId = clientContext.getRepositoryContext().getCmsRepository().getIdentityStoreRepositoryId();
			if (currentRepositoryId.equals(identityStoreId)) {
				return true;
			}
			else {
				return false;
			}
			
		}
	}
	
	public CMSUtilities getCmsUtilities() {
		return cmsUtilities;
	}

	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}

	public RepositoryUserService getRepositoryUserService() {
		return repositoryUserService;
	}

	public void setRepositoryUserService(RepositoryUserService repositoryUserService) {
		this.repositoryUserService = repositoryUserService;
	}

	public boolean isExternallyManagedIdentity() {
		return externallyManagedIdentity;
	}

	public void setExternallyManagedIdentity(boolean externallyManagedIdentity) {
		this.externallyManagedIdentity = externallyManagedIdentity;
	}

}
