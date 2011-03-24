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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.security.Principal;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchRoleException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchUserException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.api.service.secure.IdentityStoreSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteIdentityStoreSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Local({IdentityStoreSecure.class})
@Remote({RemoteIdentityStoreSecure.class})
@Stateless(name="IdentityStoreSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class IdentityStoreSecureImpl extends AbstractSecureAstroboaService implements IdentityStoreSecure{

	private IdentityStore identityStore;
	
	@Override
	void initializeOtherRemoteServices() {
		identityStore = (IdentityStore) springManagedRepositoryServicesContext.getBean("identityStore");
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean addRoleToGroup(String role, String group,
			String authenticationToken) throws CmsNoSuchRoleException{
		return identityStore.addRoleToGroup(role, group);
	}

	public boolean authenticate(String username, String password,
			String authenticationToken) {

		return identityStore.authenticate(username, password);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean changePassword(String name, String oldPassword, String newPassword,
			String authenticationToken) throws CmsNoSuchUserException, CmsInvalidPasswordException{

		return identityStore.changePassword(name, oldPassword, newPassword);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean createRole(String role, String authenticationToken) {

 		return identityStore.createRole(role);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean createUser(String username, String password,
			String authenticationToken) {

		return identityStore.createUser(username, password);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean createUser(String username, String password,
			String firstname, String lastname, String authenticationToken) {

		return identityStore.createUser(username, password, firstname, lastname);
		
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean deleteRole(String role, String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.deleteRole(role);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean deleteUser(String name, String authenticationToken) throws CmsNoSuchUserException{

		return identityStore.deleteUser(name);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean disableUser(String name, String authenticationToken) throws CmsNoSuchUserException{

		return identityStore.disableUser(name);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean enableUser(String name, String authenticationToken) throws CmsNoSuchUserException{

		return identityStore.enableUser(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> getGrantedRoles(String name, String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.getGrantedRoles(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> getImpliedRoles(String name, String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.getImpliedRoles(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> getRoleGroups(String name, String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.getRoleGroups(name);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean grantRole(String name, String role,
			String authenticationToken) throws CmsNoSuchUserException, CmsNoSuchRoleException{

		return identityStore.grantRole(name, role);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public boolean isUserEnabled(String name, String authenticationToken) throws CmsNoSuchUserException{

		return identityStore.isUserEnabled(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> listGrantableRoles(String authenticationToken) {

		return identityStore.listGrantableRoles();
		
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<Principal> listMembers(String role, String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.listMembers(role);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> listRoles(String authenticationToken) {

		return identityStore.listRoles();
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> listUsers(String authenticationToken) {

		return identityStore.listUsers();
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> listUsers(String filter, String authenticationToken) {

		return identityStore.listUsers(filter);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean removeRoleFromGroup(String role, String group,
			String authenticationToken) throws CmsNoSuchRoleException{

		return identityStore.removeRoleFromGroup(role, group);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public boolean revokeRole(String name, String role,
			String authenticationToken) throws CmsNoSuchUserException, CmsNoSuchRoleException{

		return identityStore.revokeRole(name, role);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public boolean roleExists(String name, String authenticationToken) {

		return identityStore.roleExists(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public boolean userExists(String name, String authenticationToken) {

		return identityStore.userExists(name);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public Person retrieveUser(String username, String authenticationToken) throws CmsNoSuchUserException{
		return identityStore.retrieveUser(username);
	}

	@RolesAllowed("ROLE_CMS_IDENTITY_STORE_EDITOR")
	public void updateUser(Person user, String authenticationToken) throws CmsNoSuchUserException{
		identityStore.updateUser(user);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<Person> listUsersGrantedForRole(String role,
			String authenticationToken) throws CmsNoSuchRoleException{
		return identityStore.listUsersGrantedForRole(role);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<Person> listUsersGrantedNoRoles(String authenticationToken) {
		return identityStore.listUsersGrantedNoRoles();
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<Person> findUsers(String filter, String authenticationToken) {
		return identityStore.findUsers(filter);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public List<String> listRoles(String filter, String authenticationToken) {
		return identityStore.listRoles(filter);
	}

	@RolesAllowed("ROLE_CMS_INTERNAL_VIEWER")
	public String retrieveRoleDisplayName(String role, String language,
			String authenticationToken) {
		return identityStore.retrieveRoleDisplayName(role, language);
	}
	

}
