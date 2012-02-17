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
package org.betaconceptframework.astroboa.test.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;

import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.DisplayNamePrincipal;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.PersonUserIdPrincipal;
import org.betaconceptframework.astroboa.security.CredentialsCallbackHandler;
import org.betaconceptframework.astroboa.security.jaas.AstroboaLoginModule;
import org.testng.Assert;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaLoginModuleTest {

	private Map options;

	//@BeforeClass
	public void setup() throws NamingException{

		options = new HashMap();
		options.put("hashAlgorithm","SHA-256");
		options.put("oldHashSalt","@BetaCONCEPT!");

	}

	//@Test
	public void testCredentialsNotFound() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "credentialsNotFound";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+CredentialNotFoundException.class.getName());
		}
		catch(CredentialNotFoundException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+CredentialNotFoundException.class.getName());
		}
	}

	//@Test
	public void testCredentialsNonExpired() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "credentialsExpired";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+CredentialExpiredException.class.getName());
		}
		catch(CredentialExpiredException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+CredentialExpiredException.class.getName());
		}
	}

	//@Test
	public void testAccountExpired() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "accountExpired";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+AccountExpiredException.class.getName());
		}
		catch(AccountExpiredException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+AccountExpiredException.class.getName());
		}
	}

	//@Test
	public void testAccountDisabled() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "accountDisabled";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+AccountNotFoundException.class.getName());
		}
		catch(AccountNotFoundException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+AccountNotFoundException.class.getName());
		}
	}

	//@Test
	public void testAccountLocked() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "accountLocked";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+AccountLockedException.class.getName());
		}
		catch(AccountLockedException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+AccountLockedException.class.getName());
		}
	}
	//@Test
	public void testValidPassword() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = IdentityPrincipal.SYSTEM;

		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, IdentityPrincipal.SYSTEM.toCharArray())), 
				new HashMap(), options);

		loginModule.login();

		//Assert subject
		loginModule.commit();

		boolean foundRoles = false;
		boolean foundGroups = false;

		List<String> rolesExpected = Arrays.asList("ROLE_CMS_VIEWER", "ROLE_ADMIN");
		List<String> groupsExpected = Arrays.asList("CMS", "ADMINS");

		if (subject.getPrincipals() != null && subject.getPrincipals().size() >0){

			for (Principal principal : subject.getPrincipals()){

				if (principal instanceof Group && principal.getName().equals(AstroboaPrincipalName.Roles.toString())){


					Enumeration<? extends Principal> members = ((Group)principal).members();
					while (members.hasMoreElements()){
						Principal nextElement = members.nextElement();

						if (nextElement instanceof Group){
							foundGroups = true;
							String group = nextElement.getName();
							Assert.assertTrue(groupsExpected.contains(group), "Group "+ group +" not found in "+ groupsExpected);
							
							Enumeration<? extends Principal> groupMembers = ((Group)nextElement).members();
							while (groupMembers.hasMoreElements()){
								Principal nextRole = groupMembers.nextElement();
								foundRoles = true;
								String role = nextRole.getName();
								Assert.assertTrue(rolesExpected.contains(role), "Role "+ role +" not found in "+ rolesExpected);
							}

						}
						else{
							foundRoles = true;
							String role = nextElement.getName();
							Assert.assertTrue(rolesExpected.contains(role), "Role "+ role +" not found in "+ rolesExpected);
						}
					}
				}
			}
		}


		Assert.assertTrue(foundRoles, "No roles found for user "+ username);

		Set<IdentityPrincipal> identityPrincipals = subject.getPrincipals(IdentityPrincipal.class);
		Assert.assertTrue(identityPrincipals != null && identityPrincipals.size() > 0, "No identity principals provided");
		Assert.assertTrue(identityPrincipals.size() == 1, "More than one identity principals provided");
		Assert.assertEquals(username, identityPrincipals.iterator().next().getName(), "Wrong identity principal provided");
		Assert.assertTrue(username.equals(identityPrincipals.iterator().next().getName()), "Invalid username provided as Identity principal");

		Set<PersonUserIdPrincipal> personIdPrincipals = subject.getPrincipals(PersonUserIdPrincipal.class);
		Assert.assertTrue(personIdPrincipals != null && personIdPrincipals.size() > 0, "No personId principals provided");
		Assert.assertTrue(identityPrincipals.size() == 1, "More than one personId principals provided");
		// the personIdPrincipal value is the object UUID when the identith store is Astroboa so the following assertion should fixed appropriately 
		//Assert.assertEquals("1", personIdPrincipals.iterator().next().getName(), "Wrong personId provided");

		Set<DisplayNamePrincipal> displayNamePrincipals = subject.getPrincipals(DisplayNamePrincipal.class);
		Assert.assertTrue(displayNamePrincipals != null && displayNamePrincipals.size() > 0, "No display name principals provided");
		Assert.assertTrue(identityPrincipals.size() == 1, "More than one display name principals provided");
		Assert.assertEquals(username, displayNamePrincipals.iterator().next().getName(), "Wrong display name principal provided");


	}

	//@Test
	public void testInvalidPassword() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = IdentityPrincipal.SYSTEM;
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+FailedLoginException.class.getName());
		}
		catch(FailedLoginException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){
			e.printStackTrace();
			Assert.assertTrue(1==2, "Threw invalid excpetion "+ e.getMessage()+". Expected "+FailedLoginException.class.getName());
		}
	}

	//@Test
	public void testInvalidUsername() throws Exception{

		AstroboaLoginModule loginModule = new AstroboaLoginModule();

		Subject subject = new Subject();

		String username = "admin";
		loginModule.initialize(subject, new CredentialsCallbackHandler(new AstroboaCredentials(username, "testpass".toCharArray())), 
				new HashMap(), options);

		try{
			loginModule.login();

			Assert.assertTrue(1==2, "Should have thrown "+AccountNotFoundException.class.getName());
		}
		catch(AccountNotFoundException e){
			Assert.assertEquals(e.getMessage(),username);
		}
		catch(Exception e){

			Assert.assertTrue(1==2, "Threw invalid exception "+ e.getMessage()+". Expected "+AccountNotFoundException.class.getName());
		}
	}
}
