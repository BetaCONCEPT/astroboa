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
package org.betaconceptframework.astroboa.api.security.management;

import java.security.Principal;
import java.security.acl.Group;
import java.util.List;
import java.util.Locale;

import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchRoleException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchUserException;

/**
 * The identity store interface defines the methods for managing user accounts 
 * and user roles. The methods retrieve and persist user data from/to a
 * database, LDAP, or other identity store infrastructure.
 * 
 * The purpose of the interface is to allow user management functionality from
 * within Astroboa. The core Astroboa Content engine is agnostic of the identity store
 * implementation. It is integrated with any existing security infrastructure through JAAS.
 * However the Content Management Web Application (web-ui module), provided with Astroboa Platform,
 * requires this interface in order to allow presentation of users and roles for setting up 
 * security of content objects. Additionally it utilizes this interface to provide out of  the box
 * a convenient web based user interface for managing users in the case that no identity store 
 * is already available.
 * 
 * For Astroboa users that already have an installed identity store, an implementation of this 
 * interface should be provided as a JNDI registered resource. Astroboa allows a different identity store 
 * per managed content repository. The only requirement is to specify the JNDI resource that implements 
 * this interface inside the repositories configuration file.
 * 
 * This interface and its method names have been inspired by the IdentityStore interface defined in Seam Framework.   
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on May 30, 2009
 * 
 */
public interface IdentityStore {
	
	/**
	 * Creates a new user with the specified username and password.
	 * 
	 * @param username User name
	 * @param password User password
	 * 
	 * @return <code>true</code> if the user was successfully created, <code>false</code> otherwise
	 */
	boolean createUser(String username, String password);

	/**
	 * Creates a new user with the specified username, password, first name and last name.
	 * 
	 * @param username User name
	 * @param password User password
	 * @param firstname User first name
	 * @param lastname  User last name
	 * 
	 * @return <code>true</code> if the user was successfully created, <code>false</code> otherwise
	 */
	boolean createUser(String username, String password, String firstname, String lastname);

	/**
	 * Deletes the user with the specified username.
	 * 
	 * @param name User name
	 * 
	 * @return <code>true</code> if the user was successfully deleted, <code>false</code> otherwise
	 * 
	 * @throws CmsNoSuchUserException
	 */
	boolean deleteUser(String name) throws CmsNoSuchUserException;   

	/**
	 * Enables the user with the specified username.  Enabled users are able to authenticate.
	 * 
	 * @param name User name
	 * 
	 * @return <code>true</code> if the specified user was successfully enabled, <code>false</code> otherwise
	 * 
	 * @throws CmsNoSuchUserException
	 */
	boolean enableUser(String name) throws CmsNoSuchUserException;

	/**
	 * Disables the user with the specified username.  Disabled users are unable to authenticate.
	 *
	 * @param name User name
	 * 
	 * @return <code>true</code> if the specified user was successfully disabled, <code>false</code> otherwise
	 * 
	 * @throws CmsNoSuchUserException
	 */
	boolean disableUser(String name) throws CmsNoSuchUserException;   

	/**
	 * Returns true if the specified user is enabled.
	 * 
	 * @param name User name
	 * 
	 * @return <code>true</code> if user is enabled , <code>false</code> otherwise
	 * 
	 * @throws CmsNoSuchUserException
	 * 
	 */
	boolean isUserEnabled(String name) throws CmsNoSuchUserException;

	/**
	 * Changes the password of the specified user to the specified password.
	 * 
	 * It is highly recommended that this action should be avoided due to 
	 * the lack of encryption in both passwords. 
	 * 
	 * @param name User name
	 * @param oldPassword Unencrypted old password
	 * @param newPassword Unencrypted new password
	 * 
	 * @return <code>true</code> if the user's password was successfully changed, <code>false</code> otherwise
	 * 
	 * @throws CmsNoSuchUserException
	 */
	boolean changePassword(String name, String oldPassword, String newPassword) throws CmsNoSuchUserException, CmsInvalidPasswordException;   

	/**
	 * Returns true if the specified user exists.
	 * 
	 * @param name User name
	 * 
	 * @return <code>true</code> if the user exists, <code>false</code> otherwise
	 */
	boolean userExists(String name);

	/**
	 * Creates a new role with the specified role name.
	 * 
	 * @param role Role name
	 *
	 * @return <code>true</code> if the role was created successfully, <code>false</code> otherwise
	 */
	boolean createRole(String role);

	/**
	 * Grants the specified role to the specified user.
	 * 
	 * @param name The name of the user
	 * @param role The name of the role to grant to the user.
	 * 
	 * @return <code>true</code> if the role was successfully granted, <code>false</code> otherwise
	 */
	boolean grantRole(String name, String role) throws CmsNoSuchUserException, CmsNoSuchRoleException;

	/**
	 * Revokes the specified role from the specified user.
	 * 
	 * @param name The name of the user
	 * @param role The name of the role to grant to the user.
	 * 
	 * @return <code>true</code> if the role was successfully revoked, <code>false</code> otherwise
	 */
	boolean revokeRole(String name, String role) throws CmsNoSuchUserException, CmsNoSuchRoleException;

	/**
	 * Deletes the specified role.
	 * 
	 * @param role Role name
	 *
	 * @return <code>true</code> if the role was successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteRole(String role) throws CmsNoSuchRoleException;

	/**
	 * Returns true if the specified role exists.
	 * 
	 * @param name Role name
	 *
	 * @return <code>true</code> if the role exists, <code>false</code> otherwise
	 * 
	 */
	boolean roleExists(String name);

	/**
	 * Adds the specified role as a member of the specified group.
	 * 
	 * @param role The name of the role to add as a member
	 * @param group The name of the group that the specified role will be added to.
	 * 
	 * @return <code>true</code> if the role was successfully added to the group.
	 */
	boolean addRoleToGroup(String role, String group) throws CmsNoSuchRoleException;

	/**
	 * Removes the specified role from the specified group.
	 * 
	 * @param role The name of the role to remove from the group.
	 * @param group The group from which to remove the role.
	 * 
	 * @return <code>true</code> if the role was successfully removed from the group.
	 */
	boolean removeRoleFromGroup(String role, String group) throws CmsNoSuchRoleException;   

	/**
	 * List all user names.
	 * 
	 * @return Returns a list of all user names.
	 */
	List<String> listUsers();

	/**
	 * Returns a list of all user whose names, surnames ,first names, etc 
	 * contain the specified filter text.
	 *
	 * The search should be conducted on every field of a user
	 * and should not be constrained in their username only.
	 * 
	 * @param filter Search filter
     *
     * @return List of users which contain the search filter
	 */
	List<String> listUsers(String filter);
	
	/**
	 * Returns a list of all roles containing the specified text.
	 * 
	 * The search should be conducted on every field of a role
	 * and should not be constrained in their name only.
	 * 
	 *  @param filter Search filter
     *
     * @return List of roles which contain the search filter  
	 */
	List<String> listRoles(String filter);

	/**
	 * List of all roles.
	 * 
	 * @return List of all roles 
	 */
	List<String> listRoles();

	/**
	 * List of roles that can be granted (i.e, excluding conditional roles)
	 * 
	 * @return List of role names 
	 */
	List<String> listGrantableRoles();

	/**
	 * List of all the roles explicitly granted to the specified user.
	 * 
	 * @return List of role names 
	 */
	List<String> getGrantedRoles(String name) throws CmsNoSuchUserException;

	/**
	 * List of all roles that the specified user is a member of.  
	 * 
	 * <p>This list may contain
	 * roles that may not have been explicitly granted to the user, which are indirectly implied
	 * due to group memberships.
	 * </p>
	 * 
	 * @param name Role name
	 * 
	 * @return List of role names 
	 */
	List<String> getImpliedRoles(String name) throws CmsNoSuchUserException;

	/**
	 * List of all the groups that the specified role is a member of.
	 * 
	 * @param name Role name
	 * 
	 * @return List of role/group names 
	 */
	List<String> getRoleGroups(String name) throws CmsNoSuchRoleException;

	/**
	 * Lists the members of the specified role. 
	 * 
	 * <p>
	 * Members of a role are considered not only other roles but users that refer to 
	 * the specified role as well. In order to distinguish them, 
	 * all member roles are returned under a {@link Group} named after
	 * {@link AstroboaPrincipalName#Roles}, where as all user members
	 * are returned as simple principals.
	 * </p>
	 * 
	 * @param role Role name
	 * 
	 * @return List of {@link Principal principals} representing members of role
	 * 
	 */
	List<Principal> listMembers(String role) throws CmsNoSuchRoleException;

	/**
	 * Authenticates the specified user, using the specified password.
	 * 
	 * @param username User name
	 * @param password User password
	 * 
	 * @return <code>true</code> if authentication is successful, <code>false</code> otherwise
	 */
	boolean authenticate(String username, String password);
	
	/**
	 * Retrieve User information for specified username
	 * 
	 * @param username User name
	 * 
	 * @return User information or null if no user exists for provided username
	 */
	Person retrieveUser(String username) throws CmsNoSuchUserException;
	
	/**
	 * Updates information about a Person, like first name, familyName etc.
	 * 
	 * Username is NOT updated.
	 * 
	 * @param user User information
	 */
	void updateUser(Person user) throws CmsNoSuchUserException;
	
	/**
	 * Retrieve all persons which are granted the provided role
	 * 
	 * @param role Role name
	 * @return A list of persons which are granted the specified role, empty list if role does not exist or no person is granted the provided role
	 */
	List<Person> listUsersGrantedForRole(String role) throws CmsNoSuchRoleException;
	
	/**
	 * Retrieve all persons which are granted no roles at all
	 * 
	 * @return A list of persons which are not granted any roles.
	 */
	List<Person> listUsersGrantedNoRoles();
	
	/**
	 * Returns a list of all users containing the specified filter text within their username, or first name or last name, etc. 
	 * 
	 * @param filter User name filter
	 * 
	 * @return List of {@link Person user} information 
	 */
	List<Person> findUsers(String filter);
	

	/**
	 * Retrieve display name for provided role.
	 * 
	 * <p>
	 * In cases where users want to display a role in a user
	 * friendly way, this method could be used to return appropriate
	 * localized display name for a role.
	 * </p>
	 * 
	 * <p>
	 * If no display name found for provided language, then display name
	 * for {@link Locale#ENGLISH} is returned. If no display name is found for
	 * either language, role name is returned.
	 * </p>
	 * 
	 * 
	 * @param role Role name
	 * @param language Language code as specified in {@link Locale#getLanguage()}
	 * 
	 * @return Display name for role in provided language 
	 */
	String retrieveRoleDisplayName(String role, String language);
}
