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
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.CmsPasswordEncryptor;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PasswordVerifier {

	private String newPasswordVerify;
	private String oldPassword;
	private String newPassword;
	
	private String propertyLocalizedLabel;
	
	
	public PasswordVerifier(String propertyLocalizedLabel) {
		this.propertyLocalizedLabel = propertyLocalizedLabel;
	}
	

	private List<String> validatorMessages = new ArrayList<String>();
	
	public String newPasswordVerified(CmsPasswordEncryptor cmsPasswordEncryptor, String existingEncryptedPassword) {

		
		//Check for password only if a new password has been provided
		if (StringUtils.isBlank(oldPassword) && StringUtils.isBlank(newPassword) && StringUtils.isBlank(newPasswordVerify))
		{
			return null;
		}
		
		if (StringUtils.isBlank(newPasswordVerify) || ! newPassword.equals(newPasswordVerify)){
			validatorMessages.add(JSFUtilities.getLocalizedMessage("validator.userDataPasswordsDoNotMatch", new String[]{propertyLocalizedLabel}));
			return null;
		}
		
		if (newPasswordVerify.length() < 8){
			validatorMessages.add(JSFUtilities.getLocalizedMessage("errors.minlength", new String[]{propertyLocalizedLabel, "8"}));
			return null;
		}

			try{

				//Check that old password is the same with the one existed
				//only if person is not new
				if (StringUtils.isNotBlank(existingEncryptedPassword)){
					
					 if (StringUtils.isBlank(oldPassword) || 
							 ! cmsPasswordEncryptor.checkPassword(oldPassword, existingEncryptedPassword)){
						 validatorMessages.add(JSFUtilities.getLocalizedMessage("validator.userOldPasswordDoesNotMatch", new String[]{propertyLocalizedLabel}));
						return null;
					 }
				}

				//Encode password using new algorithm
				String encryptedPassword = cmsPasswordEncryptor.encrypt(newPassword);

				if (StringUtils.isBlank(encryptedPassword))
					throw new Exception("Created blank encrypted password ...");

				return encryptedPassword;
			}
			catch(Exception e){
				validatorMessages.add(JSFUtilities.getLocalizedMessage("validator.failed2EncryptPassword", new String[]{propertyLocalizedLabel}));
				return null;
			}
	}

	
	public void resetPasswords() {
		newPasswordVerify = null;
		oldPassword= null;
		newPassword = null;
		
		validatorMessages.clear();
	}

	/**
	 * @return the newPasswordVerify
	 */
	public String getNewPasswordVerify() {
		return newPasswordVerify;
	}

	/**
	 * @param newPasswordVerify the newPasswordVerify to set
	 */
	public void setNewPasswordVerify(String newPasswordVerify) {
		this.newPasswordVerify = newPasswordVerify;
	}

	/**
	 * @return the oldPassword
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * @param oldPassword the oldPassword to set
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


	/**
	 * @return the validatorMessages
	 */
	public List<String> getValidatorMessages() {
		return validatorMessages;
	}


}
