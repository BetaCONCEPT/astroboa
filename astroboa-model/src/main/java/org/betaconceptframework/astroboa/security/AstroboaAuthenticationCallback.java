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
package org.betaconceptframework.astroboa.security;

import java.io.Serializable;

import javax.security.auth.callback.Callback;

/**
 * Represents the authentication info that may be passed
 * in JAAS login context.
 * 
 * <ul>
 *   <li>Identity Store repository id or IdentityStore JNDI name</li>
 *   <li>Repository Id where user wants to login</li>
 *   <li>A secret key passed by user who wishes to be authenticated without providing her password</li>
 * </ul>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaAuthenticationCallback implements Callback, Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7816771212053610956L;

	/**
	 * Either a Astroboa repository Id or an external JNDI Name
	 */
	private String identityStoreLocation;
	
	private boolean externalIdentityStore = false;

	private String secretKey;
	
	private String repositoryId;

	private final String prompt;

	public AstroboaAuthenticationCallback(String prompt) {
		this.prompt = prompt;
	}

	

	/**
	 * @param location the location to set
	 */
	public void setIdentityStoreLocation(String location, boolean external) {
		this.identityStoreLocation = location;
		this.externalIdentityStore = external;
	}



	/**
	 * @return the prompt
	 */
	public String getPrompt() {
		return prompt;
	}


	public String getIdentityStoreLocation() {
		return identityStoreLocation;
	}



	public boolean isExternalIdentityStore() {
		return externalIdentityStore;
	}



	public String getSecretKey() {
		return secretKey;
	}



	public String getRepositoryId() {
		return repositoryId;
	}



	public void setIdentityStoreLocation(String identityStoreLocation) {
		this.identityStoreLocation = identityStoreLocation;
	}



	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}



	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	

	
	
}
