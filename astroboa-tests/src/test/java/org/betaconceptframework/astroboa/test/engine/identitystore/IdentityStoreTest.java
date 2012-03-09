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
package org.betaconceptframework.astroboa.test.engine.identitystore;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchRoleException;
import org.betaconceptframework.astroboa.api.security.exception.CmsNoSuchUserException;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.service.security.management.IdentityStoreImpl;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IdentityStoreTest extends AbstractRepositoryTest{

	private List<String> repIds = Arrays.asList(TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID, TestConstants.TEST_REPOSITORY_ID);

	
	@Override
	protected void preSetup() throws Exception {
		super.preSetup();
		
		TestLogPolicy.setLevelForLogger(Level.FATAL, IdentityStoreImpl.class.getName());
	}
	
	

	@Override
	protected void preCleanup() {
		super.preCleanup();
		
		TestLogPolicy.setDefaultLevelForLogger(IdentityStoreImpl.class.getName());
	}



	private CmsOutcome<ContentObject> findRole(String roleName) {
		ContentObjectCriteria personCriteria = CmsCriteriaFactory.newContentObjectCriteria("roleObject");
		personCriteria.addCriterion(CriterionFactory.equals("name",roleName));
		personCriteria.setOffsetAndLimit(0, 1);
		personCriteria.doNotCacheResults();

		return contentService.searchContentObjects(personCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
	}

	@Test
	public void testRoleInitialization() throws RepositoryException{

		CmsRepositoryEntityUtils cmsRepositoryEntityUtils = AstroboaTestContext.INSTANCE.getBean(CmsRepositoryEntityUtils.class, null);
		
		for (CmsRole cmsRole : CmsRole.values()){

			for (String repid : repIds){

				String roleAffiliation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, repid);

				CmsOutcome<ContentObject> roleOutcome = findRole(roleAffiliation);

				Assert.assertTrue(roleOutcome!=null && roleOutcome.getCount()>0, "Found no role with name "+ roleAffiliation);
				Assert.assertTrue(roleOutcome.getCount()==1, "Found  more than one for roles for name "+ roleAffiliation);


				ContentObject roleContentObject = roleOutcome.getResults().get(0);

				Assert.assertEquals(((StringProperty)roleContentObject.getCmsProperty("profile.title")).getSimpleTypeValue(),roleAffiliation, "Invalid profile title for role object "+ cmsRole.toString());
				Assert.assertEquals(((StringProperty)roleContentObject.getCmsProperty("name")).getSimpleTypeValue(), roleAffiliation, "Invalid property name for role object "+ cmsRole.toString());
				Assert.assertEquals(roleContentObject.getSystemName(), cmsRepositoryEntityUtils.fixSystemName(roleAffiliation), "Invalid system name for role object "+ cmsRole.toString());

				assertSimplePropertyHasValue(roleContentObject, "accessibility.canBeReadBy", "ALL");
				assertSimplePropertyHasValue(roleContentObject, "accessibility.canBeTaggedBy", "ALL");
				assertSimplePropertyHasValue(roleContentObject, "accessibility.canBeUpdatedBy", CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID));
				assertSimplePropertyHasValue(roleContentObject, "accessibility.canBeDeletedBy", CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID));

				if (cmsRole == CmsRole.ROLE_ADMIN){
					ObjectReferenceProperty memberOfProperty = (ObjectReferenceProperty)roleContentObject.getCmsProperty("isMemberOf");
					
					Assert.assertTrue(memberOfProperty != null && memberOfProperty.hasValues(), "Role "+ roleAffiliation + " is member of no other role");
					
					Assert.assertTrue(memberOfProperty.getSimpleTypeValues().size() == 5, "Role "+ roleAffiliation + " must be member of 5 roles but is member of "+memberOfProperty.getSimpleTypeValues().size());
					
					for (ContentObject parentRoleObject : memberOfProperty.getSimpleTypeValues()){
						
						String parentRoleName = ((StringProperty)parentRoleObject.getCmsProperty("name")).getSimpleTypeValue();
						
						Assert.assertTrue(StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EDITOR, repid), parentRoleName) ||
								StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_PORTAL_EDITOR, repid), parentRoleName) ||
								StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_TAXONOMY_EDITOR, repid), parentRoleName)||
								StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER, repid), parentRoleName)||
								StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, repid), parentRoleName), 
								"Invalid parent role "+ parentRoleName + " for role "+roleAffiliation);
					}
				}
				else if (cmsRole == CmsRole.ROLE_CMS_EXTERNAL_VIEWER){
					ObjectReferenceProperty memberOfProperty = (ObjectReferenceProperty)roleContentObject.getCmsProperty("isMemberOf");
					
					Assert.assertTrue(memberOfProperty != null && memberOfProperty.hasNoValues(), "Role "+ roleAffiliation + " is member of some other role but it should not be");
					
				}
				else if (cmsRole == CmsRole.ROLE_CMS_INTERNAL_VIEWER){
					ObjectReferenceProperty memberOfProperty = (ObjectReferenceProperty)roleContentObject.getCmsProperty("isMemberOf");
					
					Assert.assertTrue(memberOfProperty != null && memberOfProperty.hasValues(), "Role "+ roleAffiliation + " is member of no other role");
					
					Assert.assertTrue(memberOfProperty.getSimpleTypeValues().size() == 1, "Role "+ roleAffiliation + " must be member of 1 role but is member of "+memberOfProperty.getSimpleTypeValues().size());
					
					ContentObject parentRoleObject = memberOfProperty.getSimpleTypeValues().get(0);
						
					String parentRoleName = ((StringProperty)parentRoleObject.getCmsProperty("name")).getSimpleTypeValue();
						
						Assert.assertTrue(StringUtils.equals(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EXTERNAL_VIEWER, repid), parentRoleName), 
								"Invalid parent role "+ parentRoleName + " for role "+roleAffiliation);
				}
				else{
					//Must be member of ROLE_CMS_INTERNAL_VIEWER

					Assert.assertEquals(	
							((StringProperty)((ObjectReferenceProperty)roleContentObject.getCmsProperty("isMemberOf")).getSimpleTypeValues().get(0).getCmsProperty("name")).getSimpleTypeValue(), 
							CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_INTERNAL_VIEWER, repid), 
							"Invalid is member of entry for role object "+ roleAffiliation);
				}
			}
		}


	}

	@Test
	public void testSystemPersonInitialization(){

		ContentObject systemPerson = getSystemPersonObject();

		assertSimplePropertyHasValue(systemPerson, "profile.title", "SYSTEM ACCOUNT");
		assertSimplePropertyHasValue(systemPerson, "profile.creator", "SYSTEM ACCOUNT");
		assertSimplePropertyHasValue(systemPerson, "profile.language", "el");

		assertSimplePropertyHasValue(systemPerson, "accessibility.canBeReadBy", "ALL");
		assertSimplePropertyHasValue(systemPerson, "accessibility.canBeTaggedBy", "ALL");
		assertSimplePropertyHasValue(systemPerson, "accessibility.canBeUpdatedBy", CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID));
		assertSimplePropertyHasValue(systemPerson, "accessibility.canBeDeletedBy", CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID));

		assertSimplePropertyHasValue(systemPerson, "name.familyName", "ACCOUNT");
		assertSimplePropertyHasValue(systemPerson, "name.givenName", IdentityPrincipal.SYSTEM);


		assertSimplePropertyHasValue(systemPerson, "displayName", IdentityPrincipal.SYSTEM);

		assertSimplePropertyHasValue(systemPerson, "personAuthentication.username", IdentityPrincipal.SYSTEM);
		assertSimplePropertyHasValue(systemPerson, "personAuthentication.password", "ppak/iCPLZw9Z8ocxH0mWMMkrEvYFVi+nBc6LL+WKlw=");
		assertSimplePropertyHasValue(systemPerson, "personAuthentication.authenticationDataEnabled", true);


		//Check roles

		ObjectReferenceProperty systemPersonRoles = (ObjectReferenceProperty) systemPerson.getCmsProperty("personAuthorization.role");

		Assert.assertTrue(systemPersonRoles.hasValues(), "Found no value for system person roles");

		Assert.assertTrue(systemPersonRoles.getSimpleTypeValues().size() == 3, "Found more roles for system person "+ systemPersonRoles.getSimpleTypeValues());

		for (ContentObject roleObject : systemPersonRoles.getSimpleTypeValues()){

			
			String roleName = ((StringProperty) roleObject.getCmsProperty("name")).getSimpleTypeValue();
			
			
			if (! StringUtils.equals(roleName, 
					CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_CLONE_REPOSITORY_ID)) &&
				! StringUtils.equals(roleName, 
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID)) &&
				! StringUtils.equals(roleName, 
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_REPOSITORY_ID))){
				throw new CmsException(" System Person has not been assigned the role "+ CmsRole.ROLE_ADMIN.toString() + " for neither repository");
			}
		}
	}

	private ContentObject getSystemPersonObject() {
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("personObject");
		contentObjectCriteria.addCriterion(CriterionFactory.equals("personAuthentication.username", IdentityPrincipal.SYSTEM));
		contentObjectCriteria.setOffsetAndLimit(0,1);
		contentObjectCriteria.doNotCacheResults();

		CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		Assert.assertFalse(outcome == null || outcome.getCount() == 0, " Found no personObject with username "+IdentityPrincipal.SYSTEM);

		Assert.assertFalse(outcome.getCount() >1, "Found more than one personObjects with username "+IdentityPrincipal.SYSTEM);

		ContentObject systemPerson = outcome.getResults().get(0);
		return systemPerson;
	}

	@Test
	public void testAuthenticate() throws IOException{
		Assert.assertFalse(identityStore.authenticate("", "betaconcept"), "Empty username has been accepted");
		Assert.assertFalse(identityStore.authenticate("    ", "betaconcept"), "Empty username has been accepted");

		Assert.assertTrue(identityStore.authenticate(IdentityPrincipal.SYSTEM, "betaconcept"), "Invalid password for "+IdentityPrincipal.SYSTEM+" user");
		Assert.assertFalse(identityStore.authenticate(IdentityPrincipal.SYSTEM, "betaconceptTest"), IdentityPrincipal.SYSTEM+" user contains wrong password");
		
		Assert.assertTrue(identityStore.authenticate("system", "betaconcept"), "Username is not case insensitive ");
		Assert.assertTrue(identityStore.authenticate("SYSTem", "betaconcept"), "Username is not case insensitive ");
		
		TestLogPolicy.setLevelForLogger(Level.FATAL, IdentityStoreImpl.class.getName());
		Assert.assertFalse(identityStore.authenticate("SYS", "betaconcept"), "Invalid user 'SYS' has been authorized");
		TestLogPolicy.setDefaultLevelForLogger(IdentityStoreImpl.class.getName());
		
	}

	@Test
	public void testGrantedRoles(){
		try{
			identityStore.getGrantedRoles("");
		}
		catch(CmsNoSuchUserException e){
			Assert.assertEquals(e.getMessage(), "", "Invalid exception thrown when requesting grantedRoles for empty username");
		}

		List<String> grantedRoles = identityStore.getGrantedRoles(IdentityPrincipal.SYSTEM);

		Assert.assertTrue(CollectionUtils.isNotEmpty(grantedRoles), "No roles have been granted to SYSTEM");

		Assert.assertTrue(grantedRoles.size() == 3, "More or less roles than normal have been granted to SYSTEM "+ grantedRoles);

		Assert.assertTrue(grantedRoles.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_CLONE_REPOSITORY_ID)), 
				"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_CLONE_REPOSITORY_ID)+
		" has not been granted to SYSTEM");
		
		Assert.assertTrue(grantedRoles.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID)), 
				"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID)+
		" has not been granted to SYSTEM");

		Assert.assertTrue(grantedRoles.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_REPOSITORY_ID)), 
				"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, TestConstants.TEST_REPOSITORY_ID)+
		" has not been granted to SYSTEM");

		//At startup granted roles are the same with grantable roles
		List<String> grantableRoles = identityStore.listGrantableRoles();

		Assert.assertTrue(CollectionUtils.isNotEmpty(grantableRoles), "No grantableRoles found");

		Assert.assertTrue(grantableRoles.size() == (CmsRole.values().length * 3 ), "More or less grantableRoles for SYSTEM than normal found"+ grantableRoles);

		for (CmsRole cmsRole : CmsRole.values()){
			for (String repId : repIds){
				Assert.assertTrue(grantableRoles.contains(
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, repId)),
						"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, repId)+" is not grantable");
			}
		}
	}

	@Test
	public void testImpliedRoles(){

		try{
			identityStore.getImpliedRoles("");
		}
		catch(CmsNoSuchUserException e){
			Assert.assertEquals(e.getMessage(), "", "Invalid exception thrown when requesting impliedRoles for empty username");
		}



		//At startup granted roles are the same with implied roles
		List<String> impliedRoles = identityStore.getImpliedRoles(IdentityPrincipal.SYSTEM);

		Assert.assertTrue(CollectionUtils.isNotEmpty(impliedRoles), "No roles have been granted to SYSTEM");

		Assert.assertTrue(impliedRoles.size() == ( CmsRole.values().length * 3), "More or less roles than normal have been granted to SYSTEM "+ impliedRoles);

		for (CmsRole cmsRole : CmsRole.values()){
			Assert.assertTrue(impliedRoles.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole,TestConstants.TEST_REPOSITORY_ID)),
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole,TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID)+" has not been granted to SYSTEM");
			Assert.assertTrue(impliedRoles.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole,TestConstants.TEST_REPOSITORY_ID)),
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole,TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID)+" has not been granted to SYSTEM");
		}
	}

	@Test
	public void testRoleGroups(){

		for (String repId : repIds){

			String roleAdminAffiliationForRepository = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_ADMIN, repId);

			List<String> roleGroups = identityStore.getRoleGroups(roleAdminAffiliationForRepository);

			Assert.assertTrue(CollectionUtils.isNotEmpty(roleGroups), "No role groups have been found for role "+roleAdminAffiliationForRepository);

			Assert.assertTrue(roleGroups.size() == 5, "More role groups than normal have been found for role "+roleAdminAffiliationForRepository);

			Assert.assertTrue(roleGroups.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EDITOR, repId)), 
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_EDITOR, repId)+" is not role group for role "+
					roleAdminAffiliationForRepository);
			Assert.assertTrue(roleGroups.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_PORTAL_EDITOR,repId)),
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_PORTAL_EDITOR,repId)+" is not role group for role "+
					roleAdminAffiliationForRepository);
			Assert.assertTrue(roleGroups.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_TAXONOMY_EDITOR,repId)), 
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_TAXONOMY_EDITOR,repId)+" is not role group for role "+
					roleAdminAffiliationForRepository);
			Assert.assertTrue(roleGroups.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER,repId)),
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER,repId)+" is not role group for role "+
					roleAdminAffiliationForRepository);
			Assert.assertTrue(roleGroups.contains(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR,repId)),
					"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR,repId)+" is not role group for role "+
					roleAdminAffiliationForRepository);

		}

	}

	@Test
	public void testUserEnabled(){

		try{
			identityStore.isUserEnabled("testUser");
		}
		catch(CmsNoSuchUserException e){
			Assert.assertEquals(e.getMessage(), "testUser", "Invalid exception thrown when checking if invalid user is enabled");
		}

		Assert.assertTrue(identityStore.isUserEnabled(IdentityPrincipal.SYSTEM), IdentityPrincipal.SYSTEM+" user is not  marked as enabled");
	}

	@Test
	public void testListMembers(){

		try{
			identityStore.listMembers("UnknowRole");
		}
		catch(CmsNoSuchRoleException e){
			Assert.assertEquals(e.getMessage(), "UnknowRole", "Invalid exception thrown when listing members of an unknown role");
		}


		checkMembersForRole(CmsRole.ROLE_ADMIN, true, new ArrayList<CmsRole>());
		checkMembersForRole(CmsRole.ROLE_CMS_EXTERNAL_VIEWER, false, Arrays.asList(CmsRole.ROLE_CMS_INTERNAL_VIEWER));
		checkMembersForRole(CmsRole.ROLE_CMS_INTERNAL_VIEWER, false, Arrays.asList(CmsRole.ROLE_CMS_EDITOR, CmsRole.ROLE_CMS_PORTAL_EDITOR, CmsRole.ROLE_CMS_TAXONOMY_EDITOR, CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER, CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR));
		checkMembersForRole(CmsRole.ROLE_CMS_EDITOR, false, Arrays.asList(CmsRole.ROLE_ADMIN));
		checkMembersForRole(CmsRole.ROLE_CMS_PORTAL_EDITOR, false, Arrays.asList(CmsRole.ROLE_ADMIN));
		checkMembersForRole(CmsRole.ROLE_CMS_TAXONOMY_EDITOR, false, Arrays.asList(CmsRole.ROLE_ADMIN));
		checkMembersForRole(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER, false, Arrays.asList(CmsRole.ROLE_ADMIN));
		checkMembersForRole(CmsRole.ROLE_CMS_IDENTITY_STORE_EDITOR, false, Arrays.asList(CmsRole.ROLE_ADMIN));

	}

	private void checkMembersForRole(CmsRole roleWhoseMembersAreChecked, boolean expectToFindSystemUser, 
			List<CmsRole> expectedRoles){

		for (String repId : repIds){
			
			String roleAffiliationWhoseMembersAreChecked = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(roleWhoseMembersAreChecked, repId);
			
			List<Principal> roleMembers = identityStore.listMembers(roleAffiliationWhoseMembersAreChecked);

			Assert.assertTrue(CollectionUtils.isNotEmpty(roleMembers), "Found no members for role "+roleAffiliationWhoseMembersAreChecked);

			boolean foundSystemUser = false;

			List<String> rolesFound = new ArrayList<String>();

			for (Principal memberPrincipal : roleMembers){

				if (memberPrincipal instanceof CmsPrincipal){
					if (IdentityPrincipal.SYSTEM.equals(memberPrincipal.getName())){
						foundSystemUser =true;
					}
				}
				else if (memberPrincipal instanceof CmsGroup && 
						AstroboaPrincipalName.Roles.toString().equals(memberPrincipal.getName())){

					Enumeration<? extends Principal> roleMemberPrincipals = ((CmsGroup)memberPrincipal).members();

					while(roleMemberPrincipals.hasMoreElements()){
						rolesFound.add(roleMemberPrincipals.nextElement().getName());
					}
				}
			}

			if (expectToFindSystemUser){
				Assert.assertTrue(foundSystemUser, "Found no SYSTEM as member of "+roleAffiliationWhoseMembersAreChecked);
			}
			else{
				Assert.assertFalse(foundSystemUser, "Found SYSTEM as member of "+roleAffiliationWhoseMembersAreChecked);
			}

			Assert.assertTrue(rolesFound.size() == expectedRoles.size(), 
					"More or less role members than normal have been found for role "+roleAffiliationWhoseMembersAreChecked+ " "+ rolesFound);


			for (CmsRole expectedRole : expectedRoles){
				Assert.assertTrue(rolesFound.contains(
						CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(expectedRole, repId)), 
						"Role "+CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(expectedRole, repId)+
						" is not role member for role "+	roleAffiliationWhoseMembersAreChecked);
			}

		}

	}
	
	@Test
	public void testRetrieveUser() throws IOException
	{
		// assertPersonForUsername(TestConstants.TEST_USER_NAME);

		final String username = TestConstants.TEST_USER_NAME+"Test";
		
		String personUUID = createUser(username);
		
		assertPersonForUsername(username, personUUID);
		
		deleteUser(username);
		
	}

	private void assertPersonForUsername(final String username, final String personUUID) {
		
		Person person = identityStore.retrieveUser(username);
		
		Assert.assertEquals(person.getDisplayName(), username, "Invalid display name");
		Assert.assertEquals(person.getFamilyName(), username, "Invalid family name");
		Assert.assertEquals(person.getFatherName(), username, "Invalid father name");
		Assert.assertEquals(person.getFirstName(), username, "Invalid first name");
		Assert.assertEquals(person.getUserid(), personUUID, "Invalid userid");
		Assert.assertEquals(person.getUsername(), username, "Invalid username");
	}

	@Test
	public void testUsers(){
		List<String> users = identityStore.listUsers();

		Assert.assertTrue(CollectionUtils.isNotEmpty(users), "No users have been found ");

		Assert.assertEquals(users.size(),2, "More or less users found "+users);

		Assert.assertTrue(users.contains(IdentityPrincipal.SYSTEM), "SYSTEM user was not returned "+users);
		Assert.assertTrue(users.contains(TestConstants.TEST_USER_NAME), TestConstants.TEST_USER_NAME+" user was not returned "+users);


		assertFilteredUsers("ys", 1, IdentityPrincipal.SYSTEM);
		assertFilteredUsers("EM", 1, IdentityPrincipal.SYSTEM);
		assertFilteredUsers("system", 1, IdentityPrincipal.SYSTEM);
		assertFilteredUsers(IdentityPrincipal.SYSTEM, 1, IdentityPrincipal.SYSTEM);
		assertFilteredUsers("yss", 0, null);

		Assert.assertFalse(identityStore.userExists(""), "Empty username  exists");
		Assert.assertFalse(identityStore.userExists("INVALID"), "Invalid user exists");

		Assert.assertTrue(identityStore.userExists(IdentityPrincipal.SYSTEM), "User "+IdentityPrincipal.SYSTEM+"  does not exist");

	}

	@Test
	public void testRoles(){

		Assert.assertFalse(identityStore.roleExists(""), "Empty Role exists");
		Assert.assertFalse(identityStore.roleExists("INVALID"), "Invalid Role exists");


		for(String repid : repIds){

			for (CmsRole cmsRole : CmsRole.values()){
				String roleAffiliation = CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, repid);
				Assert.assertTrue(identityStore.roleExists(roleAffiliation), "Role "+roleAffiliation+" does not exist");
				Assert.assertEquals(roleAffiliation, identityStore.retrieveRoleDisplayName(roleAffiliation, "en"), "Role "+roleAffiliation+" does not contain valid display name");
			}
		}
	}

	private void assertFilteredUsers(String filter, int expectedResultNo, String singleUserExpected){

		List<String> filteredUsers = identityStore.listUsers(filter);

		if (expectedResultNo > 0){
			Assert.assertTrue(CollectionUtils.isNotEmpty(filteredUsers), "No users have been found whose username contains filter "+filter);
		}
		else{
			Assert.assertTrue(CollectionUtils.isEmpty(filteredUsers), "Found users found whose username contains filter "+filter+ " but expected none");
		}

		Assert.assertTrue(filteredUsers.size() == expectedResultNo, "More or less users found than expected "+filteredUsers);

		if (expectedResultNo == 1){
			Assert.assertEquals(filteredUsers.get(0), singleUserExpected, "One user found but it is not the expected one "+expectedResultNo + " "+ filteredUsers);
		}
	}

	private void assertSimplePropertyHasValue(ContentObject systemPerson, String propertyPath,
			Object propertyValue) {

		SimpleCmsProperty<?, ?, ?> cmsProperty = (SimpleCmsProperty<?, ?, ?>) systemPerson.getCmsProperty(propertyPath);

		Assert.assertTrue(cmsProperty.hasValues(), "Found no value for property "+propertyPath);

		Object value = null;

		if (cmsProperty.getPropertyDefinition().isMultiple()){
			value = cmsProperty.getSimpleTypeValues().get(0);
		}
		else{
			value = cmsProperty.getSimpleTypeValue();
		}

		Assert.assertNotNull(value, "Found no value for property "+propertyPath+ "\n"+systemPerson.xml(true));

		Assert.assertEquals(value, propertyValue, "Found invalid value for property "+propertyPath+ "\n"+systemPerson.xml(true));

	}

	@Override
	protected void loginToTestRepositoryAsSystem() {
		loginToRepositoryRepresentingIdentityStoreAsSystem();
	}
	
	
}
