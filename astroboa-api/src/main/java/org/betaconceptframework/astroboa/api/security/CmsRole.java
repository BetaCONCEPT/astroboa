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

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Taxonomy;

/**
 * 
 * Astroboa built in security roles.
 * 
 * <p>
 * Astroboa service operations are executed by a client only if she is authorized to do so. 
 * Each Astroboa service operation expects that client is assigned specific role(s), otherwise
 * execution is denied. 
 * </p>
 * 
 * <p>
 * Astroboa Security supports the RBAC model partially, in that it does not, for the moment, 
 * defines specific permissions. Astroboa security defines a set of core logical roles which
 * are mapped to standard core operations that are performed upon Astroboa entities. Role hierarchy 
 * is supported as well.
 * </p> 
 * 
 * <p>Note, that a role on its own means nothing. It has significance only when examined in 
 * the context of a Astroboa repository. For example, if a Astroboa user is assigned role
 * ROLE_CMS_EDITOR, then she has access to no particular content. Instead, this role must be assigned
 * within the context of a Astroboa repository, that is <code>ROLE_CMS_EDITOR@wiki_repository</code>. The latter
 * means that user can edit content available in Astroboa repository <code>wiki_repository</code>.
 * </p>
 * 
 * <p>
 * This class enumerates these core roles.
 * </p> 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum CmsRole {

	/**
	 * Users which are member of this role
	 * have access to all features and functions of Astroboa.
	 * 
	 * In particular this role is member of roles
	 * 
	 * <ul>
	 * <li>{@link CmsRole#ROLE_CMS_EDITOR}<li>
	 * <li>{@link CmsRole#ROLE_CMS_PORTAL_EDITOR}<li>
	 * <li>{@link CmsRole#ROLE_CMS_TAXONOMY_EDITOR}<li>
	 * <li>{@link CmsRole#ROLE_CMS_WEB_SITE_PUBLISHER}<li>
	 * <li>{@link CmsRole#ROLE_CMS_IDENTITY_STORE_EDITOR}<li>
	 */
	ROLE_ADMIN,
	
	/**
	 * Users which are member of this role have read access to all published content.
	 * 
	 * That is, they can read all {@link ContentObject}s of any type whose status 
	 * (property <code>profile.contentObject</code> is 
	 * <code>published</code> or <code>publishedAndArchived</code>.
	 * 
	 * Users assigned this role only, cannot access content from Astroboa
	 * Web Console.  
	 */
	ROLE_CMS_EXTERNAL_VIEWER,
	
	/**
	 * 
	 * Users which are member of this role have special read access to Astroboa content through
   	 * Astroboa Web console.This role is  member of {@link CmsRole#ROLE_CMS_EXTERNAL_VIEWER},
     * thus inherits all permissions of that role.
     * 
	 */
	ROLE_CMS_INTERNAL_VIEWER,
	
	/**
	 * Users which are member of this role have write access to content. 
	 * 
	 * This role is  member of {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER}, 
	 * thus inherits all permissions of that role.
	 * 
	 */
	ROLE_CMS_EDITOR,
	
	/**
	 * Users which are member of this role have the authority to publish content.
	 * 
	 * This role is  member of {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER}, 
	 * thus inherits all permissions of that role.
	 * 
	 */
	ROLE_CMS_WEB_SITE_PUBLISHER,
	
	/**
	 * Users which are member of this role have write access to content 
	 * whose type is <code>portalObject</code> and <code>portalSectionObject</code>.
	 * 
	 * This role is  member of {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER}, 
	 * thus inherits all permissions of that role
	 * 
	 */
	ROLE_CMS_PORTAL_EDITOR,

	/**
	 * Users which are member of this role have write access 
	 * to all {@link Taxonomy taxonomies} of a Astroboa repository.
	 * 
	 * This role is  member of {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER}, 
	 * thus inherits all permissions of that role
	 * 
	 */
	ROLE_CMS_TAXONOMY_EDITOR,
	
	/**
	 * Users which are member of this role have write access 
	 * to content whose type is <code>personObject</code> and <code>roleObject</code>.
	 * 
	 * This role is  member of {@link CmsRole#ROLE_CMS_INTERNAL_VIEWER}, 
	 * thus inherits all permissions of that role
	 * 
	 */
	ROLE_CMS_IDENTITY_STORE_EDITOR;
	
}
