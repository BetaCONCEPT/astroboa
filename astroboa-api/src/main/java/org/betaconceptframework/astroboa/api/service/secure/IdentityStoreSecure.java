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
package org.betaconceptframework.astroboa.api.service.secure;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchRoleException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchUserException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.Person;

/**
 * The identity store interface defines the methods for managing user accounts 
 * and user roles. The methods retrieve and persist user data from/to a
 * database, LDAP, or other identity store infrastructure.
 * 
 * <p>
 * It contains the same methods provided by 
 * {@link IdentityStore} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in an Astroboa repository and
 * therefore has been granted access to further use Astroboa services
 * </p> 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on May 30, 2009
 * 
 */
public interface IdentityStoreSecure {
	
	/**
	 * Same semantics with {@link IdentityStore#createUser(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param username User name
	 * @param password User password
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the user was successfully created, <code>false</code> otherwise
	 */
	boolean createUser(String username, String password, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#createUser(String, String, String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param username User name
	 * @param password User password
	 * @param firstname User first name
	 * @param lastname  User last name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the user was successfully created, <code>false</code> otherwise
	 */
	boolean createUser(String username, String password, String firstname, String lastname, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#deleteUser(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the user was successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteUser(String name, String authenticationToken) throws CmsNoSuchUserException;   

	/**
	 * Same semantics with {@link IdentityStore#enableUser(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the specified user was successfully enabled, <code>false</code> otherwise
	 */
	boolean enableUser(String name, String authenticationToken) throws CmsNoSuchUserException;

	/**
	 * Same semantics with {@link IdentityStore#disableUser(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the specified user was successfully disabled, <code>false</code> otherwise
	 */
	boolean disableUser(String name, String authenticationToken) throws CmsNoSuchUserException;   

	/**
	 * Same semantics with {@link IdentityStore#isUserEnabled(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if user is enabled , <code>false</code> otherwise
	 */
	boolean isUserEnabled(String name, String authenticationToken) throws CmsNoSuchUserException;

	/**
	 * Same semantics with {@link IdentityStore#changePassword(String, String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name
	 * @param oldPassword Unencrypted old password
	 * @param newPassword Unencrypted new password
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the user's password was successfully changed, <code>false</code> otherwise
	 */
	boolean changePassword(String name, String oldPassword, String newPassword, String authenticationToken) throws CmsNoSuchUserException, CmsInvalidPasswordException;   

	/**
	 * Same semantics with {@link IdentityStore#userExists(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name User name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the user exists, <code>false</code> otherwise
	 */
	boolean userExists(String name, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#createRole(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param role Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was created successfully, <code>false</code> otherwise
	 */
	boolean createRole(String role, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#grantRole(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name The name of the user
	 * @param role The name of the role to grant to the user.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was successfully granted, <code>false</code> otherwise
	 */
	boolean grantRole(String name, String role, String authenticationToken) throws CmsNoSuchUserException, CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#revokeRole(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param name The name of the user
	 * @param role The name of the role to grant to the user.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was successfully revoked, <code>false</code> otherwise
	 */
	boolean revokeRole(String name, String role, String authenticationToken) throws CmsNoSuchUserException, CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#deleteRole(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param role Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was successfully deleted, <code>false</code> otherwise
	 */
	boolean deleteRole(String role, String authenticationToken) throws  CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#roleExists(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param name Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role exists, <code>false</code> otherwise
	 */
	boolean roleExists(String name, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#addRoleToGroup(String, String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param role The name of the role to add as a member
	 * @param group The name of the group that the specified role will be added to.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was successfully added to the group.
	 */
	boolean addRoleToGroup(String role, String group, String authenticationToken) throws CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#removeRoleFromGroup(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param role The name of the role to remove from the group.
	 * @param group The group from which to remove the role.
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if the role was successfully removed from the group.
	 */
	boolean removeRoleFromGroup(String role, String group, String authenticationToken) throws CmsNoSuchRoleException;   

	/**
	 * Same semantics with {@link IdentityStore#listUsers()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Returns a list of all user names.
	 * 
	 */
	List<String> listUsers(String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#listUsers(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param filter User name filter
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
     * @return List of user names containing provided filter
	 */
	List<String> listUsers(String filter, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#listRoles()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of all roles 
	 */
	List<String> listRoles(String authenticationToken);
	
	/**
	 * Same semantics with {@link IdentityStore#listRoles(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param filter Role name filter
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
     * @return List of roles whose name contain provided filter
	 */
	List<String> listRoles(String filter, String authenticationToken);


	/**
	 * Same semantics with {@link IdentityStore#listGrantableRoles()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of role names 
	 */
	List<String> listGrantableRoles(String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#getGrantedRoles(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param name Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of role names 
	 */
	List<String> getGrantedRoles(String name, String authenticationToken) throws CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#getImpliedRoles(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param name Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of role names 
	 */
	List<String> getImpliedRoles(String name, String authenticationToken) throws CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#getRoleGroups(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param name Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of role/group names 
	 */
	List<String> getRoleGroups(String name, String authenticationToken) throws CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#listMembers(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 * 
	 * @param role Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of {@link Principal principals} representing members of role
	 * 
	 */
	List<Principal> listMembers(String role, String authenticationToken) throws CmsNoSuchRoleException;

	/**
	 * Same semantics with {@link IdentityStore#authenticate(String, String)}
	 * augmented with the requirement of providing an authentication token
	 * 
	 * @param username User name
	 * @param password User password
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return <code>true</code> if authentication is successful, <code>false</code> otherwise
	 */
	boolean authenticate(String username, String password, String authenticationToken);
	
	/**
	 * Same semantics with {@link IdentityStore#retrieveUser(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param username User name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return User information or null if no user exists for provided username
	 */
	Person retrieveUser(String username, String authenticationToken) throws CmsNoSuchUserException;
	
	/**
	 * Same semantics with {@link IdentityStore#updateUser(Person)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param user User information
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 */
	void updateUser(Person user, String authenticationToken) throws CmsNoSuchUserException;
	
	/**
	 * Same semantics with {@link IdentityStore#listUsersGrantedForRole(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param role Role name
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of persons which are granted the specified role, empty list if role does not exist or no person is granted the provided role
	 */
	List<Person> listUsersGrantedForRole(String role, String authenticationToken) throws CmsNoSuchRoleException;
	
	/**
	 * Same semantics with {@link IdentityStore#listUsersGrantedNoRoles()}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return A list of persons which are not granted any roles.
	 */
	List<Person> listUsersGrantedNoRoles(String authenticationToken);
	
	/**
	 * Same semantics with {@link IdentityStore#findUsers(String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 *<p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 *</p>
	 *
	 * 
	 * @param filter User name filter
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return List of {@link Person user} information 
	 */
	List<Person> findUsers(String filter, String authenticationToken);

	/**
	 * Same semantics with {@link IdentityStore#retrieveRoleDisplayName(String, String)}
	 * augmented with the requirement of providing an authentication token
	 *
	 * <p>
	 * This method is executed only if user has role
	 * {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER} upon connected Astroboa repository.
	 * Information about user's roles is available through provided authentication 
	 * token.
	 * </p> 
	 * 
	 * @param role Role name
	 * @param language Language code as specified in {@link Locale#getLanguage()}
	 * @param authenticationToken A token provided during client login ({@link RepositoryServiceSecure#login(String, AstroboaCredentials, String)})
	 *  to an Astroboa repository.
	 * 
	 * @return Display name for role in provided language 
	 */
	String retrieveRoleDisplayName(String role, String language, String authenticationToken);
}
