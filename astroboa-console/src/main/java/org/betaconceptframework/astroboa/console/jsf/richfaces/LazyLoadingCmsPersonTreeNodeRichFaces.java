/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf.richfaces;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.DublicateRepositoryUserExternalIdException;
import org.betaconceptframework.astroboa.console.jsf.identity.CmsUsersRolesTree;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * Created on Sept 10, 2007
 */
public class LazyLoadingCmsPersonTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private Person person;

	//This is the corresponding repository user for the Person.
	//It may be the case that a person has never logged in the repository
	//and therefore no RepositoryUser has been created.
	private RepositoryUser repositoryUser;

	private List<String> impliedRoles;

	public LazyLoadingCmsPersonTreeNodeRichFaces(String identifier, String description, TreeNode parent, Person person, List<String> impliedRoles) {
		super(identifier, description, parent, CmsUsersRolesTree.UserRoleTreeNodeType.USER.toString(), true);

		this.person =  person;
		this.impliedRoles = impliedRoles;

		if (person != null && StringUtils.isNotBlank(person.getUsername())){

			try{
				repositoryUser = ((CMSUtilities)JSFUtilities.getBeanFromSpringContext("cmsUtilities")).findRepositoryUserByUserId(person.getUsername(), null);
			}
			catch(DublicateRepositoryUserExternalIdException e){
				logger.warn("While lazy loading person "+person.getUsername()+ " in user roles tree in astroboa-console for repository "+
						AstroboaClientContextHolder.getActiveRepositoryId() + " found more than one RepositoryUsers with the same externalId "+
						person.getUserid());
			}
			catch(Exception e){
				logger.warn("While lazy loading person "+person.getUsername()+ " in user roles tree in astroboa-console for repository "+
						AstroboaClientContextHolder.getActiveRepositoryId(),e);
			}
		}
	}

	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		return children.entrySet().iterator();
	}


	public RepositoryUser getRepositoryUser(){
		return repositoryUser;
	}

	@Override
	public String getDescription() {
		if (StringUtils.isBlank(super.getDescription())){
			return JSFUtilities.getLocalizedMessage("no.localized.label.for.description", null);
		}
		
		return super.getDescription();
	}

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	public List<String> getImpliedRoles()
	{
		return impliedRoles;
	}

}
