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
package org.betaconceptframework.astroboa.api.security;

import java.security.Principal;
import java.security.acl.Group;

import javax.security.auth.Subject;

import org.betaconceptframework.astroboa.api.service.RepositoryService;

/**
 * Enumeration of all Astroboa principal names identified
 * by Astroboa during authentication and authorization process.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum AstroboaPrincipalName {

	/**
	 * Any {@link Group} named after <code>Roles</code>
	 * contains all roles of the authenticated user.
	 * 
	 * Members of this groups can be either any other {@link Group} or
	 * {@link Principal}.
	 */
	Roles,
	
	/**
	 * Any {@link Group} named after <code>AuthorizedRepositories</code>
	 * contains all repository ids as these are specified in Astroboa
	 * configuration file, that an authenticated user can have access to.
	 * 
	 * When this {@link Group} exists in the {@link Subject} created or provided
	 * during {@link RepositoryService#login(String, AstroboaCredentials)} ,
	 *  then user is authorized to 
	 * use the specified repository id only if this id is defined as a member of
	 * this {@link Group}.
	 * 
	 * Members of this groups can be any {@link Principal} whose name is a valid
	 * repository id, as this is defined in Astroboa configuration file.
	 */
	
	AuthorizedRepositories;
}
