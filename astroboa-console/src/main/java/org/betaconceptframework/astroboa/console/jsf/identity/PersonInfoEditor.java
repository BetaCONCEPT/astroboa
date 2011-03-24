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
package org.betaconceptframework.astroboa.console.jsf.identity;



import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.console.jsf.DynamicUIAreaPageComponent;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.ui.seam.AbstractSeamUIBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("personInfoEditor")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PersonInfoEditor extends AbstractSeamUIBean{

	private static final long serialVersionUID = 1L;

	@In(create=true)
	private IdentityStoreRunAsSystem identityStoreRunAsSystem; 

	
	//Injected values
	private LoggedInRepositoryUser loggedInRepositoryUser;
	private PageController pageController;


	private Person person;

	private String newPasswordVerify;
	private String oldPassword;
	private String newPassword;
	private List<String> roles;

	public String editPersonInfo_UIAction() {

		newPassword = null;
		newPasswordVerify = null;
		oldPassword = null;
		roles = null;

		//Load person which corresponds to Logged in user
		person = (Person) identityStoreRunAsSystem.execute("retrieveUser", new Class<?>[]{String.class}, new Object[]{loggedInRepositoryUser.getIdentity()}); 

		return pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.PERSON_PROFILE_EDITOR.getDynamicUIAreaPageComponent());
	}


	public String savePerson_UIAction() {

		//For a new person password must be provided

		boolean updatePassword = false;

		if (StringUtils.isNotBlank(oldPassword)){
			if (StringUtils.isBlank(newPassword)){
				JSFUtilities.addMessage(null, "errors.required",
						new String[]{JSFUtilities.getLocalizedMessage("user.provision.edit.user.account.data.new.password", null)}, 
						FacesMessage.SEVERITY_WARN);
				return null;
			}

			if (StringUtils.isBlank(newPasswordVerify) || ! newPassword.equals(newPasswordVerify)){
				JSFUtilities.addMessage(null, "validator.userDataPasswordsDoNotMatch",
						new String[]{JSFUtilities.getLocalizedMessage("user.provision.edit.user.account.data.panel.header", null)}, 
						FacesMessage.SEVERITY_WARN);
				return null;
			}
			
			if (newPassword.length() < 8){
				JSFUtilities.addMessage(null, "errors.minlength",
						new String[]{JSFUtilities.getLocalizedMessage("user.provision.edit.user.account.data.new.password", null), "8"}, 
						FacesMessage.SEVERITY_WARN);
				return null;
			}
			
			updatePassword = true;
		}

		try {

			identityStoreRunAsSystem.execute("updateUser", new Class<?>[]{Person.class}, new Object[]{person});
			
			JSFUtilities.addMessage(null, "user.provision.user.account.update.successfull", null, FacesMessage.SEVERITY_INFO);
			
			
			if (updatePassword){
				try{
					Object passwordUpdated = identityStoreRunAsSystem.execute("changePassword",new Class<?>[]{String.class, String.class, String.class}, 
							new Object[]{person.getUsername(), oldPassword, newPassword} );
					if ( BooleanUtils.isFalse((Boolean) passwordUpdated)){
						JSFUtilities.addMessage(null, "user.provision.user.authentication.update.failed", null, FacesMessage.SEVERITY_WARN);
						return null;
					}
				}
				catch(CmsInvalidPasswordException e){
					JSFUtilities.addMessage(null, "validator.userOldPasswordDoesNotMatch", new String[]{JSFUtilities.getLocalizedMessage("user.provision.edit.user.account.data.old.password", null)}, FacesMessage.SEVERITY_WARN);
					return null;
				}

				JSFUtilities.addMessage(null, "user.provision.user.authentication.update.successfull", null, FacesMessage.SEVERITY_INFO);
			}

		}
		catch (Exception e) {
			//Pass message directly to JSF messages
			JSFUtilities.addMessage(null, "user.provision.user.account.save.failed", null, FacesMessage.SEVERITY_WARN);
			logger.error("",e);
			return null;

		}

		return null;
	}


	public List<String> getPersonRoles(){
		
		if (roles == null){
			
			if (person!=null && person.getUsername() != null){
				roles = (List<String>) identityStoreRunAsSystem.execute("getImpliedRoles", new Class<?>[]{String.class}, new Object[]{person.getUsername()});
			}
			
		}
		
		return roles;
	}


	public Person getPerson() {
		return person;
	}


	public String getOldPassword() {
		return oldPassword;
	}


	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}


	public String getNewPassword() {
		return newPassword;
	}


	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


	public String getNewPasswordVerify() {
		return newPasswordVerify;
	}


	public void setNewPasswordVerify(String newPasswordVerify) {
		this.newPasswordVerify = newPasswordVerify;
	}


	/**
	 * @param loggedInRepositoryUser the loggedInRepositoryUser to set
	 */
	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}


	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}


}
