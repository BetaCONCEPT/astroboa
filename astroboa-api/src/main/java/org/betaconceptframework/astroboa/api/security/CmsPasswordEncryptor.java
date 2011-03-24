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
package org.betaconceptframework.astroboa.api.security;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.StringProperty;

/**
 * 
 * Interface containing method for managing encrypted passwords which are values
 * of a {@link StringProperty}.
 * 
 * One can implement this interface in case she chooses to encrypt passwords in a different way than
 * the default one. 
 * 
 * 
 * This interface and its method names have been inspired by the PasswordEncryptor interface defined in Jasypt Project (http://www.jasypt.org/index.html).
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsPasswordEncryptor extends Serializable {
	 /**
     * Encrypts a password.
     * 
     * @param password the value to be encrypted.
     * @return the resulting encrypted password.
     */
    public String encrypt(String password);

    
    /**
     * Checks if unencrypted password matches against an encrypted one
     * 
     * @param plainPassword the plain password to check.
     * @param encryptedPassword the digest against which to check the password.
     * @return <code>true</code> if passwords match, <code>false</code> otherwise.
     */
    public boolean checkPassword(String plainPassword, 
            String encryptedPassword);
}
