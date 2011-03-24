/*
 * Copyright (C) 2005-2008 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.context;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;


/**
 * This class contains all necessary security information about
 * an authenticated user or entity, both referred as "subject".
 * 
 * Its methods should be used to perform role-based authorization.
 * 
 * An instance of this class is generated each time a subject 
 * successfully logs in Astroboa and is accessible from method
 * {@link AstroboaClientContextHolder#getActiveSecurityContext()}.
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class SecurityContext implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4368086721315850622L;

	private final String identity;
	private final String authenticationToken;
	private final Subject subject;
	private final int authenticationTokenTimeout;

	//List containing all repositories, authenticated user or entity is authorized to access
	private List<String> authorizedRepositories;
	
	//List containing all user's roles.
	private Set<String> roles;

	public SecurityContext(String authenticationToken,
			Subject subject, int authenticationTokenTimeout, 
			List<String> availableRepositories) {
		this.authenticationToken = authenticationToken;
		this.subject = subject;
		this.authenticationTokenTimeout = authenticationTokenTimeout;
		
		this.identity = retrieveIdentityFromSubject();
		
		this.roles = retrieveRolesFromSubject();
	
		this.authorizedRepositories =  retrieveAuthorizedRepositoriesFromSubject(availableRepositories);
	}

	private List<String> retrieveAuthorizedRepositoriesFromSubject(List<String> availableRepositories) {

		List<String> authorizedRepositories = new ArrayList<String>();

		boolean foundAuthorizedRepositoriesPrincipal = false;

		if (subject != null){
			Set<Group> subjectGroups = subject.getPrincipals(Group.class);

			
			if (subjectGroups != null){
				for (Group group : subjectGroups){
					if (group.getName() != null && AstroboaPrincipalName.AuthorizedRepositories.toString().equals(group.getName())){
						foundAuthorizedRepositoriesPrincipal = true;
						Enumeration groupMembers = group.members();
						while (groupMembers.hasMoreElements())
						{
							Principal groupPrincipal = (Principal) groupMembers.nextElement();
							authorizedRepositories.add(groupPrincipal.getName());
						}

						break;
					}
				}
			}
		}
		
		//In cases where no information about authorized repositories
		//is provided in Subject, a PERMIT ALL policy is enforced, 
		//thus available repositories must be known during initialization of this 
		//context
		if (! foundAuthorizedRepositoriesPrincipal){
			if (CollectionUtils.isNotEmpty(availableRepositories)){
				authorizedRepositories.addAll(availableRepositories);
			}
		}
		
		return authorizedRepositories;
	}

	private Set<String> retrieveRolesFromSubject() {
		Set<String> roles = new HashSet<String>();

		if (subject != null){
			Set<Group> groups = subject.getPrincipals(Group.class);

			if (groups != null){
				for (Group group : groups){
					if (group.getName() != null && AstroboaPrincipalName.Roles.toString().equals(group.getName())){

						addGroupMembersToRoles(group, roles);

						break;
					}
				}
			}
		}

		return roles;

	}

	private String retrieveIdentityFromSubject() {
		if (subject != null){
			Set<IdentityPrincipal> userPrincipals = subject.getPrincipals(IdentityPrincipal.class);
		
			if (CollectionUtils.isEmpty(userPrincipals)){
				return "";
			}
		
			//Retrieve the first one. Normally it should not have more than one
			return userPrincipals.iterator().next().getName();
		}
		
		return "";
	}

	/**
	 * Convenient method to obtain authenticated user id (usually the username) which normally is provided
	 * as a {@link IdentityPrincipal principal} in {@link #getSubject() subject}.
	 * 
	 * @return
	 * 	The authenticated user name
	 */
	public String getIdentity(){
		return identity;
	}

	/** 
	 * Upon successfully connecting to a repository, an authentication
	 * token is created. This token can be used for further use of Astroboa services
	 * in order to avoid to authenticate every time.
	 * 
	 * @return
	 * 		Authentication token created upon successful connection to an Astroboa repository
	 */
	public String getAuthenticationToken(){
		return authenticationToken;
	}

	/**
	 * Return an instance of authenticated user or entity along with 
	 * its {@link Principal identities}, its roles and any other security related
	 * information.
	 * 
	 * <p>
	 * THIS INSTANCE MUST NOT BE ALTERED IN ANY WAY. If you want to perform changes
	 * in user's roles or principals, you have to use methods provided by this class.
	 * To obtain a reference to this class you can call {@link AstroboaClientContextHolder#getActiveSecurityContext()}
	 * at any point at your code.
	 * </p>
	 * 
	 * @return
	 * 	Security related information for authenticated user>
	 */
	public Subject getSubject(){
		return subject;
	}

	/**
	 * Authentication Token timeout in minutes.
	 * 
	 * <p>
	 * It represents the amount of the idle time 
	 * that authentication token is considered valid. 
	 * After that time authentication token is removed.
	 * </p>
	 * 
	 * 
	 * @return
	 */
	public int getAuthenticationTokenTimeout() {
		return authenticationTokenTimeout;
	}
	
	
	

	/**
	 * Convenient method to retrieve all roles for authenticated user.
	 * 
	 * This method returns all Roles found under the FIRST principal
	 * found in Subject with the name {@link AstroboaPrincipalName#Roles}. 
	 * 
	 * If roles are provided in a tree, then this tree is traversed as well
	 * 
	 * @return
	 */
	public List<String> getAllRoles() {
		return new ArrayList<String>(roles);
	}

	private void addGroupMembersToRoles(Group group, Set<String> roles) {
		Enumeration groupMembers = group.members();
		while (groupMembers.hasMoreElements()){
			Principal role = (Principal) groupMembers.nextElement();
			
			roles.add(role.getName());
			
			if (role instanceof Group){
				addGroupMembersToRoles((Group)role, roles);
			}
		}
	}

	/**
	 * Retrieve a list of all repositories which authenticated user or entity 
	 * is authorized to access.
	 * 
	 * @return
	 */
	public List<String> getAuthorizedRepositories() {
		return new ArrayList<String>(authorizedRepositories);
	}

	public boolean hasRole(String role){
		return StringUtils.isNotBlank(role) && roles.contains(role);
	}
	
	/*
	 * This method adds a role to an already authenticated user. 
	 * It should be used in rare cases only.
	 * 
	 * Normally, user's roles should not be altered this way. 
	 */
	public boolean addRole(String role){
		
		if (StringUtils.isBlank(role)){
			return false;
		}
		
		Set<Group> groups = subject.getPrincipals(Group.class);

		boolean roleGroupFound = false;
		
		boolean roleAdded = false;
		
		String nameOfGroupWhichContainsTheRoles = AstroboaPrincipalName.Roles.toString();
		
		if (groups != null){

			for (Group group : groups){
				if (StringUtils.equals(nameOfGroupWhichContainsTheRoles, group.getName())){
					roleGroupFound = true;
					
					final CmsPrincipal rolePrincipal = new CmsPrincipal(role);
					if (! group.isMember(rolePrincipal)){
						group.addMember(rolePrincipal);
						roleAdded = true;
					}
					
					break;
				}
			}
		}
		
		if (! roleGroupFound){
			Group rolesPrincipal = new CmsGroup(nameOfGroupWhichContainsTheRoles);
			rolesPrincipal.addMember(new CmsPrincipal(role));
			subject.getPrincipals().add(rolesPrincipal);
			roleAdded = true;
		}
		
		if (roleAdded){
			this.roles.add(role);
		}

		return roleAdded;
	}
	
	/*
	 * This method removes a role to an already authenticated user. 
	 * It should be used in rare cases only.
	 * 
	 * Normally, user's roles should not be altered this way. 
	 */
	public boolean removeRole(String role){
		
		if (StringUtils.isBlank(role)){
			return false;
		}
		
		boolean roleHasBeenRemoved = false;
		
		Set<Group> groups = subject.getPrincipals(Group.class);

		if (groups != null){
			
			String nameOfGroupWhichContainsTheRoles = AstroboaPrincipalName.Roles.toString();

			for (Group group : groups){
				if (StringUtils.equals(nameOfGroupWhichContainsTheRoles, group.getName())){
					final CmsPrincipal rolePrincipal = new CmsPrincipal(role);
					
					if (group.isMember(rolePrincipal)){
						roleHasBeenRemoved = group.removeMember(rolePrincipal);
						break;
					}
				}
			}
		}
		
		//remove role from the list as well
		if (roleHasBeenRemoved && this.roles.contains(role)){
			this.roles.remove(role);
		}
		
		return roleHasBeenRemoved;
		
	}

	public String toString(){
		return " SecurityContext authToken : "+ authenticationToken+ 
		(subject!= null ? " Subject : "+ subject.toString() : "");
	}
}
