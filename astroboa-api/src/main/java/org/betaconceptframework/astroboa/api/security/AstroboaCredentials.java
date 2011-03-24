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


/**
 * Class responsible to hold credentials used to login to Astroboa repository.
 * 
 * It also keeps some authentication information which may be used by an Identity Provider
 * during authentication
 * <ul>
 *   <li>Identity Store repository id or IdentityStore JNDI name</li>
 *   <li>Repository Id where user wants to login</li>
 *   <li>A secret key passed by user who wishes to be authneticated without providing her password</li>
 * </ul>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class AstroboaCredentials implements Serializable {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4770335681630681483L;
	private final String username;
	private final char[] password;
	private final String identityStoreRepositoryId;
	private final String identityStoreRepositoryJNDIName;
	private final String repositoryId;
	private final String secretKey;
	
	public AstroboaCredentials(String username, char[] password,
			String  identityStoreRepositoryId,
			String identityStoreRepositoryJNDIName, 
			String repositoryId,
			String secretKey) {
		
		this.username = username;
		this.identityStoreRepositoryId = identityStoreRepositoryId;
		this.identityStoreRepositoryJNDIName = identityStoreRepositoryJNDIName;
		this.repositoryId = repositoryId;
		this.secretKey = secretKey;
		
		
		if (password != null){
			this.password = (char[]) password.clone();
		}
		else{
			this.password = null;
		}
	}

	public AstroboaCredentials(String username, char[] password) {
		
		this.username = username;
		this.identityStoreRepositoryId = null;
		this.identityStoreRepositoryJNDIName = null;
		this.repositoryId = null;
		this.secretKey = null;
		
		
		
		if (password != null){
			this.password = (char[]) password.clone();
		}
		else{
			this.password = null;
		}
	}

	public AstroboaCredentials(String username, String password) {
		this(username, (password == null? null : password.toCharArray()));
	}

	public AstroboaCredentials(String username) {
		this(username, null, null,null,null,null);
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		if (password == null)
		{
			return null;
		}
		
		return (char[]) password.clone();
	}

	/**
	 * @return the repositoryId
	 */
	public String getIdentityStoreRepositoryId() {
		return identityStoreRepositoryId;
	}

	/**
	 * @return the identityStoreRepositoryJNDIName
	 */
	public String getIdentityStoreRepositoryJNDIName() {
		return identityStoreRepositoryJNDIName;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public String getSecretKey() {
		return secretKey;
	}
	
	
}
