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
package org.betaconceptframework.astroboa.engine.service.security.management;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidUsernameException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchRoleException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchUserException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.api.security.management.IdentityStoreContextHolder;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.security.management.CmsPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IdentityStoreImpl implements IdentityStore{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ContentService contentService;
	
	public boolean addRoleToGroup(String role, String group) throws CmsNoSuchRoleException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Roles");
	}

	@Override
	public boolean authenticate(String username, String password) {

		if (StringUtils.isBlank(username)){
			return false;
		}

		ContentObject personObject = null;
		
		try{
			personObject = retrieveUniquePersonObjectForUsername(username);
		}
		catch(CmsException e)
		{
			logger.warn("Error during authentication of user "+username+". Could not load ContentObject of type personType for this user", e);
			return false;
		}

		if (personObject == null){
			logger.warn("No content object of type personType found in repository {} for username {}", 
					retrieveActiveRepositoryId(), username);
				throw new CmsLoginInvalidUsernameException(username);
		}

		CmsProperty<?, ?> passwordProperty = personObject.getCmsProperty("personAuthentication.password");

		if (passwordProperty != null && passwordProperty instanceof StringProperty &&
				((StringProperty)passwordProperty).hasValues()){

			StringPropertyDefinition passwordPropertyDefinition = ((StringProperty)passwordProperty).getPropertyDefinition();

			if (!passwordPropertyDefinition.isPasswordType()){
				logger.warn("Open Social property 'personAuthentication.password' has not been marked as a password type");
			}

			if (passwordPropertyDefinition.getPasswordEncryptor() == null){

				throw new CmsException("Found no password encryptor to use. Check that Open Social property 'personAuthentication.password' definition " +
				"refers to a valid CmsPasswordEncryptor class name and if none is specified, check that default class AstroboaPasswordEncryptor" +
				" is used and initialized properly");

			}

			return passwordPropertyDefinition.getPasswordEncryptor().checkPassword(password, 
					((StringProperty)passwordProperty).getSimpleTypeValue());

		}

		logger.warn("Content Object of type personType for username {} has no values in property personAuthentication.password and therefore user" +
				"cannot be authenticated", username );
		
		return false;
	}

	

	@Override
	public boolean changePassword(String name, String oldPassword, String newPassword) throws CmsNoSuchUserException, CmsInvalidPasswordException{
		if (StringUtils.isBlank(name)){
			throw new CmsNoSuchUserException("Invalid person. No username provided");
		}

		ContentObject personObject  = null;
		try{
			personObject  = retrieveUniquePersonObjectForUsername(name);
		}
		catch(CmsException e)
		{
			return false;
		}

		if (personObject == null)
		{
			return false;
		}

		//Extra check about password
		if (StringUtils.isBlank(newPassword) || newPassword.length()<8){
			throw new CmsInvalidPasswordException("Invalid password. Should be at least 8 characters long");
		}

		CmsProperty<?,?> passwordProperty = (CmsProperty<?,?>) personObject.getCmsProperty("personAuthentication.password");

		if (passwordProperty != null && passwordProperty instanceof StringProperty &&
				((StringProperty)passwordProperty).hasValues()){

			StringPropertyDefinition passwordPropertyDefinition = ((StringProperty)passwordProperty).getPropertyDefinition();

			if (!passwordPropertyDefinition.isPasswordType()){
				logger.warn("Open Social property 'personAuthentication.password' has not been marked as a password type");
			}

			if (passwordPropertyDefinition.getPasswordEncryptor() == null){

				logger.warn("Found no password encryptor to use. Check that Open Social property 'personAuthentication.password' definition " +
						"refers to a valid CmsPasswordEncryptor class name and if none is specified, check that default class AstroboaPasswordEncryptor" +
				" is used and initialized properly");

				return false;
			}

			if (passwordPropertyDefinition.getPasswordEncryptor().checkPassword(oldPassword,	((StringProperty)passwordProperty).getSimpleTypeValue())){
				((StringProperty)passwordProperty).setSimpleTypeValue(passwordPropertyDefinition.getPasswordEncryptor().encrypt(newPassword));
				contentService.save(personObject, false, true, null);

				return true;
			}
			else{
				throw new CmsInvalidPasswordException(name);
			}
		}

		return false;
	}

	@Override
	public boolean createRole(String role) {
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Roles");
	}

	@Override
	public boolean createUser(String username, String password) {
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public boolean createUser(String username, String password,
			String firstname, String lastname) {
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public boolean deleteRole(String role) throws CmsNoSuchRoleException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Roles");
	}

	@Override
	public boolean deleteUser(String name) throws CmsNoSuchUserException {
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public boolean disableUser(String name) throws CmsNoSuchUserException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public boolean enableUser(String name) throws CmsNoSuchUserException {
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public List<String> getGrantedRoles(String name) throws CmsNoSuchUserException{

		if (StringUtils.equals(IdentityPrincipal.ANONYMOUS, name))
		{
			return loadRolesForAnonymousUser();
		}
		
		ContentObject personObject  = null;
		try{
			personObject  = retrieveUniquePersonObjectForUsername(name);
		}
		catch(CmsException e)
		{
			return new ArrayList<String>();
		}
		

		List<String> grantedRoles = new ArrayList<String>();

		if (personObject != null){

			ObjectReferenceProperty rolesProperty = (ObjectReferenceProperty) personObject.getCmsProperty("personAuthorization.role");

			if (rolesProperty != null && rolesProperty.hasValues()){

				for (ContentObject roleObject : rolesProperty.getSimpleTypeValues()){
					grantedRoles.add( retrieveRoleNameAndThrowExceptionIfNotThere(roleObject));
				}
			}
		}

		return grantedRoles;

	}

	private List<String> loadRolesForAnonymousUser() {

		//Assign only ROLE_CMS_EXTERNAL_VIEWER to ANONYMOUS
		List<String> roles = new ArrayList<String>();
		
		roles.add(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER, retrieveActiveRepositoryId()));
		
		return roles;
	}

	private String retrieveRoleNameAndThrowExceptionIfNotThere(
			ContentObject roleObject) {

		StringProperty roleNameProperty = (StringProperty)roleObject.getCmsProperty("name");

		//Check if value exists for role although property name is mandatory
		if (roleNameProperty == null || roleNameProperty.hasNoValues()){
			throw new CmsException("Found no name for role object "+ roleObject.getId());
		}

		return roleNameProperty.getSimpleTypeValue();
	}

	@Override
	public List<String> getImpliedRoles(String name)throws CmsNoSuchUserException {

		if (StringUtils.equals(IdentityPrincipal.ANONYMOUS, name)){
			return loadRolesForAnonymousUser();
		}
		
		ContentObject personObject  = null;
		try{
			personObject  = retrieveUniquePersonObjectForUsername(name);
		}
		catch(CmsException e){
			return new ArrayList<String>();
		}

		Set<String> impliedRoles = new HashSet<String>();

		if (personObject != null){

			ObjectReferenceProperty rolesProperty = (ObjectReferenceProperty) personObject.getCmsProperty("personAuthorization.role");

			if (rolesProperty != null && rolesProperty.hasValues()){

				for (ContentObject roleObject : rolesProperty.getSimpleTypeValues()){

					addRoleAndIsMemberOfToImpliedRoles(impliedRoles, roleObject);
				}
			}
		}

		return new ArrayList<String>(impliedRoles);
	}

	private void addRoleAndIsMemberOfToImpliedRoles(Set<String> impliedRoles,
			ContentObject roleObject) {

		String roleName = retrieveRoleNameAndThrowExceptionIfNotThere(roleObject);

		if (impliedRoles.add(roleName)){

			//Continue further only if role does not already exist in the list
			ObjectReferenceProperty isMemberOfProperty = (ObjectReferenceProperty) roleObject.getCmsProperty("isMemberOf");

			if (isMemberOfProperty.hasValues()){

				for(ContentObject impliedRoleObject : isMemberOfProperty.getSimpleTypeValues()){

					addRoleAndIsMemberOfToImpliedRoles(impliedRoles, impliedRoleObject);
				}
			}
		}
	}

	@Override
	public List<String> getRoleGroups(String name) throws CmsNoSuchRoleException{

		ContentObject roleObject = retrieveRole(name, true);

		List<String> roleGroups = new ArrayList<String>();

		if (roleObject != null){

			ObjectReferenceProperty roleGroupsProperty = (ObjectReferenceProperty) roleObject.getCmsProperty("isMemberOf");

			if (roleGroupsProperty != null && roleGroupsProperty.hasValues()){

				for (ContentObject roleGroupObject : roleGroupsProperty.getSimpleTypeValues()){

					roleGroups.add(retrieveRoleNameAndThrowExceptionIfNotThere(roleGroupObject));
				}
			}
		}

		return roleGroups;
	}


	@Override
	public boolean grantRole(String name, String role) throws CmsNoSuchUserException, CmsNoSuchRoleException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Person");
	}

	@Override
	public boolean isUserEnabled(String name) throws CmsNoSuchUserException{

		ContentObject personObject  = null;
		try{
			personObject  = retrieveUniquePersonObjectForUsername(name);
		}
		catch(CmsException e)
		{
			return false;
		}

		BooleanProperty userEnabledProperty = (BooleanProperty) personObject.getCmsProperty("personAuthentication.authenticationDataEnabled");

		return userEnabledProperty != null &&
		userEnabledProperty.hasValues() &&
		userEnabledProperty.getSimpleTypeValue();
	}

	@Override
	public List<String> listGrantableRoles() {

		List<String> grantableRoles = new ArrayList<String>();

		ContentObjectCriteria roleObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");
		roleObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		roleObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(roleObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject rankedOutcome : outcome.getResults()){
				grantableRoles.add(retrieveRoleNameAndThrowExceptionIfNotThere(rankedOutcome));
			}
		}


		return grantableRoles;
	}

	@Override
	public List<Principal> listMembers(String role) throws CmsNoSuchRoleException{

		List<Principal> roleMembers = new ArrayList<Principal>();

		ContentObject roleObject = retrieveRole(role, true);

		//Find all persons which are members of the role
		ContentObjectCriteria personObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");
		personObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		personObjectCriteria.doNotCacheResults();
		personObjectCriteria.addCriterion(CriterionFactory.equals("personAuthorization.role", roleObject.getId()));

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(personObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject rankedOutcome : outcome.getResults()){

				ContentObject personObject = rankedOutcome;

				StringProperty personUsernameProperty = (StringProperty) personObject.getCmsProperty("personAuthentication.username");

				if (personUsernameProperty == null || personUsernameProperty.hasNoValues()){
					logger.warn("Person "+personObject.getId() + " does not have a username and thus will not be added as a member for role "+role);
				}
				else{
					roleMembers.add(new CmsPrincipal(personUsernameProperty.getSimpleTypeValue()));
				}
			}
		}

		//Find all role which are member of the role
		ContentObjectCriteria roleObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");
		roleObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		roleObjectCriteria.doNotCacheResults();
		roleObjectCriteria.addCriterion(CriterionFactory.equals("isMemberOf", roleObject.getId()));

		outcome = contentService.searchContentObjects(roleObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (outcome != null && outcome.getCount() > 0){

			CmsGroup rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());

			for (ContentObject rankedOutcome : outcome.getResults()){

				ContentObject memberRoleObject = rankedOutcome;

				rolesPrincipal.addMember(new CmsPrincipal(retrieveRoleNameAndThrowExceptionIfNotThere(memberRoleObject)));
			}

			roleMembers.add(rolesPrincipal);
		}

		return roleMembers;
	}

	@Override
	public List<String> listRoles() {

		return listGrantableRoles();
	}

	@Override
	public List<String> listUsers() {

		return listUsers(null);
	}

	@Override
	public List<String> listUsers(String filter) {

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");

		if (filter != null){
			contentObjectCriteria.addCriterion(CriterionFactory.contains("personAuthentication.username", "*"+filter+"*"));
		}

		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("personAuthentication.username");

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		List<String> users = new ArrayList<String>();

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject personObjectRanked : outcome.getResults()){
				CmsProperty<?, ?> usernameProperty = personObjectRanked.getCmsProperty("personAuthentication.username");

				if (usernameProperty != null && usernameProperty instanceof StringProperty){
					users.add(((StringProperty)usernameProperty).getSimpleTypeValue());	
				}
			}
		}

		return users;
	}

	@Override
	public boolean removeRoleFromGroup(String role, String group) throws CmsNoSuchRoleException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Roles");
	}

	@Override
	public boolean revokeRole(String name, String role) throws CmsNoSuchUserException, CmsNoSuchRoleException{
		throw new CmsException("Method not yet implemented. Use Astroboa web interface to administer Roles");
	}

	@Override
	public boolean roleExists(String name) {

		try{
			return retrieveRole(name, false) != null;
		}
		catch(CmsNoSuchRoleException e){
			return false;
		}
	}

	@Override
	public boolean userExists(String name) {

		try{
			return retrieveUniquePersonObjectForUsername(name) != null;
		}
		catch(CmsNoSuchUserException e){
			return false;
		}
	}

	private ContentObjectCriteria createPersonObjectCriteriaWithUserName(String username){

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");
		contentObjectCriteria.addCriterion(CriterionFactory.equalsCaseInsensitive("personAuthentication.username", username));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();


		return contentObjectCriteria;

	}

	private ContentObject retrieveUniquePersonObjectForUsername(
			String username) {

		if (StringUtils.isBlank(username)){
			throw new CmsNoSuchUserException(username);
		}
		
		ContentObjectCriteria personCriteria = createPersonObjectCriteriaWithUserName(username);

		if (personCriteria == null){
			throw new CmsException("No criteria provided for personType");
		}
		
		personCriteria.setOffsetAndLimit(0,1);

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(personCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (outcome == null || outcome.getCount() == 0){
			throw new CmsNoSuchUserException(username);
		}

		if (outcome.getCount() >1){
			throw new CmsNoSuchUserException("Found more than one personObjects with username "+ username);
		}

		return outcome.getResults().get(0);
	}

	private ContentObject retrieveRole(String name, boolean throwExcpetionIfMoreThanOneFound) {

		if (StringUtils.isBlank(name)){
			throw new CmsNoSuchRoleException("Empty role name");
		}

		ContentObjectCriteria roleObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");
		roleObjectCriteria.addCriterion(CriterionFactory.equals("name", name));
		roleObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		roleObjectCriteria.doNotCacheResults();
		roleObjectCriteria.setOffsetAndLimit(0,1);

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(roleObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		if (outcome == null || outcome.getCount() == 0){
			throw new CmsNoSuchRoleException(name);
		}

		if (throwExcpetionIfMoreThanOneFound && outcome.getCount() >1){
			throw new CmsNoSuchRoleException("Found more than one roles with name "+ name);
		}

		return outcome.getResults().get(0);
	}

	@Override
	public Person retrieveUser(String username) throws CmsNoSuchUserException{
		try{

			ContentObject personObject = retrieveUniquePersonObjectForUsername(username);

			if (personObject == null)
			{
				throw new CmsNoSuchUserException();
			}
			
			return createPersonFromContentObject(personObject);

		}
		catch(CmsNoSuchUserException e){
			throw e;
		}
		catch(Exception e){
			throw new CmsException("Problem when loading person for username "+username, e);
		}

	}

	private CmsPerson createPersonFromContentObject(
			ContentObject personObject) {
		CmsPerson person = new CmsPerson();

		BooleanProperty enabledProperty = (BooleanProperty) personObject.getCmsProperty("personAuthentication.authenticationDataEnabled");
		person.setEnabled(enabledProperty != null && enabledProperty.hasValues() && BooleanUtils.isTrue(enabledProperty.getSimpleTypeValue()));


		StringProperty userNameProperty = (StringProperty) personObject.getCmsProperty("personAuthentication.username");
		if (userNameProperty != null && userNameProperty.hasValues()){
			person.setUsername(userNameProperty.getSimpleTypeValue());
		}

		StringProperty familyNameProperty = (StringProperty) personObject.getCmsProperty("name.familyName");
		if (familyNameProperty != null && familyNameProperty.hasValues()){
			person.setFamilyName(familyNameProperty.getSimpleTypeValue());
		}

		StringProperty fatherNameProperty = (StringProperty) personObject.getCmsProperty("name.fatherName");
		if (fatherNameProperty != null && fatherNameProperty.hasValues()){
			person.setFatherName(fatherNameProperty.getSimpleTypeValue());
		}

		StringProperty givenNameProperty = (StringProperty) personObject.getCmsProperty("name.givenName");
		if (givenNameProperty != null && givenNameProperty.hasValues()){
			person.setFirstName(givenNameProperty.getSimpleTypeValue());
		}

		StringProperty displayNameProperty = (StringProperty) personObject.getCmsProperty("displayName");
		if (displayNameProperty != null && displayNameProperty.hasValues()){
			person.setDisplayName(displayNameProperty.getSimpleTypeValue());
		}

		person.setUserid(personObject.getId());
		
		return person;
	}

	@Override
	public void updateUser(Person user) throws CmsNoSuchUserException{

		if (user == null || StringUtils.isBlank(user.getUsername())){
			throw new CmsException("Invalid person. No username provided");
		}

		ContentObject personObject  = retrieveUniquePersonObjectForUsername(user.getUsername());

		if (personObject == null){
			throw new CmsNoSuchUserException(user.getUsername());
		}

		((StringProperty) personObject.getCmsProperty("name.familyName")).setSimpleTypeValue(user.getFamilyName());
		((StringProperty) personObject.getCmsProperty("name.fatherName")).setSimpleTypeValue(user.getFatherName());
		((StringProperty) personObject.getCmsProperty("name.givenName")).setSimpleTypeValue(user.getFirstName());

		contentService.save(personObject, false, true, null);
	}

	@Override
	public List<Person> listUsersGrantedForRole(String role) throws CmsNoSuchRoleException{

		if (StringUtils.isBlank(role)){
			return new ArrayList<Person>();
		}

		try{
			ContentObject roleObject = retrieveRole(role, true);

			if (roleObject == null){
				return new ArrayList<Person>();
			}

			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");

			contentObjectCriteria.addCriterion(CriterionFactory.equals("personAuthorization.role", roleObject.getId()));
			contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
			contentObjectCriteria.doNotCacheResults();

			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

			List<Person> users = new ArrayList<Person>();

			if (outcome != null && outcome.getCount() > 0){

				for (ContentObject personObject : outcome.getResults()){
					users.add(createPersonFromContentObject(personObject));	
				}
			}

			return users;

		}
		catch(CmsNoSuchRoleException e){
			throw e;
		}
		catch(Exception e){
			logger.warn("",e);
			return new ArrayList<Person>();
		}

	}

	@Override
	public List<Person> listUsersGrantedNoRoles() {
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");

		contentObjectCriteria.addCriterion(CriterionFactory.isNull("personAuthorization.role"));
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		List<Person> users = new ArrayList<Person>();

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject personObject : outcome.getResults()){
				users.add(createPersonFromContentObject(personObject));	
			}
		}

		return users;
	}

	@Override
	public List<Person> findUsers(String filter) {

		if (StringUtils.isBlank(filter)){
			return new ArrayList<Person>();
		}

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personType");
		
		contentObjectCriteria.addFullTextSearchCriterion(filter+"*");
		
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);

		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		List<Person> users = new ArrayList<Person>();

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject personObject : outcome.getResults()){
				users.add(createPersonFromContentObject(personObject));	
			}
		}

		return users;
	}

	@Override
	public List<String> listRoles(String filter) {
		
		if (StringUtils.isBlank(filter)){
			return new ArrayList<String>();
		}
		
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");

		contentObjectCriteria.addFullTextSearchCriterion(filter+"*");
		contentObjectCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		List<String> users = new ArrayList<String>();

		if (outcome != null && outcome.getCount() > 0){

			for (ContentObject roleObject : outcome.getResults()){
				CmsProperty<?, ?> nameProperty = roleObject.getCmsProperty("name");

				if (nameProperty != null && nameProperty instanceof StringProperty){
					users.add(((StringProperty)nameProperty).getSimpleTypeValue());	
				}
			}
		}

		return users;
	}

	public String retrieveRoleDisplayName(String role, String language) {
		
		ContentObject roleObject = retrieveRole(role, false);
		
		StringProperty titleProperty = (StringProperty)roleObject.getCmsProperty("profile.title");
		
		if (titleProperty != null && titleProperty.hasValues()){
			return titleProperty.getSimpleTypeValue();
		}
		
		return role;
	}

	private String retrieveActiveRepositoryId(){
		
		String activeRepositoryId = IdentityStoreContextHolder.getActiveRepositoryId();
		
		if (activeRepositoryId == null){
			return AstroboaClientContextHolder.getActiveRepositoryId();
		}
		
		return activeRepositoryId;
	}
}
