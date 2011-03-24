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
package org.betaconceptframework.astroboa.security.management;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.management.Person;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPerson implements Person{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7205317875817959804L;
	private boolean enabled;
	private String familyName;
	private String fatherName;
	private String firstName;
	private String userid;
	private String username;
	private String displayName;
	
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getFatherName() {
		return fatherName;
	}

	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @param fatherName the fatherName to set
	 */
	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getDisplayName() {
		
		if (displayName == null){
			//Create one from first last and father name
			StringBuilder displayNameBuilder = new StringBuilder();

			if (StringUtils.isNotBlank(firstName)){
				displayNameBuilder.append(firstName);
			}

			if (StringUtils.isNotBlank(fatherName)){
				displayNameBuilder.append(" ");
				displayNameBuilder.append(Character.toTitleCase(fatherName.charAt(0)));
			}

			if (StringUtils.isNotBlank(familyName)){
				displayNameBuilder.append(" ");
				displayNameBuilder.append(familyName);
			}

			displayName = displayNameBuilder.toString();
		}
		
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
	  this.displayName = displayName;
		
	}
	

}
