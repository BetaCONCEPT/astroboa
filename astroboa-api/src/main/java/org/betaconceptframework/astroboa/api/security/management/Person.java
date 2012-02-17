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
package org.betaconceptframework.astroboa.api.security.management;

import java.io.Serializable;

/**
 * Represents a User in an IdentityStore and contains
 * basic information about this user.
 * 
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface Person extends Serializable {

	/**
	 * 
	 * @return <code>true</code> if user is enabled, <code>false</code>
	 */
	boolean isEnabled();
	void setEnabled(boolean enabled);
	
	
	/**
	 * The given name of this Person, or 'First Name' in most Western languages
	 * @return
	 * 		The given name of this Person, or 'First Name' in most Western languages
	 */
	String getFirstName();
	void setFirstName(String firstName);
	
	/**
	 * Retrieve father name of this Person
	 * 
	 * @return
	 * 	Father name of this Person
	 */
	String getFatherName();
	void setFatherName(String fatherName);
	
	/**
	 * The family name of this Person, or 'Last Name' in most Western languages 
	 * @return
	 * 	The family name of this Person, or 'Last Name' in most Western languages
	 */
	String getFamilyName();
	void setFamilyName(String familyName);
	
	/**
	 * A user ID number, usually chosen automatically, and usually numeric but sometimes alphanumeric, e.g. '12345' or '1Z425A'.
	 * 
	 * @return Person's userid
	 */
	String getUserid();
	void setUserid(String userid);
	

	/**
	 * An alphanumeric user name, usually chosen by the user
	 * 
	 * @return Person's username
	 */
	String getUsername();
	void setUsername(String username);
	
	/**
	 * The name of this Person, suitable for display to end-users.
	 * @return
	 * 	The name of this Person, suitable for display to end-users.
	 */
	String getDisplayName();
	void setDisplayName(String displayName);
}
