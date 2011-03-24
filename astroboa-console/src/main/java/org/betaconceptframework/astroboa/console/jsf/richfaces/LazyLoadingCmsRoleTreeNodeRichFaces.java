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
package org.betaconceptframework.astroboa.console.jsf.richfaces;


import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.security.management.Person;
import org.betaconceptframework.astroboa.console.jsf.identity.CmsUsersRolesTree.UserRoleTreeNodeType;
import org.betaconceptframework.astroboa.console.security.IdentityStoreRunAsSystem;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.richfaces.model.TreeNode;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LazyLoadingCmsRoleTreeNodeRichFaces extends LazyLoadingTreeNodeRichFaces {

	private IdentityStoreRunAsSystem identityStoreRunAsSystem;

	private String role;

	public LazyLoadingCmsRoleTreeNodeRichFaces(String identifier, String description, TreeNode parent, boolean leaf, String role) {

		super(identifier, description, parent, UserRoleTreeNodeType.ROLE.toString(), leaf);

		identityStoreRunAsSystem =  (IdentityStoreRunAsSystem) Component.getInstance(IdentityStoreRunAsSystem.class, ScopeType.SESSION, true);

		this.role = role;
	}


	public Iterator<Map.Entry<String, TreeNode>> getChildren() {
		// if this in not a leaf node and there are no children, try and retrieve them
		if (!isLeaf() && children.size() == 0) {
			logger.debug("retreive children of node: " + identifier);


			int nodeIndex = 0;
			try {

				//First load all roles
				List<Person> persons = new ArrayList<Person>();
				
				if (StringUtils.isBlank(role)){
					
					persons = (List<Person>) identityStoreRunAsSystem.execute("listUsersGrantedNoRoles", null, null);
					
					List<String> roleMembers = (List<String>) identityStoreRunAsSystem.execute("listGrantableRoles", null, null);

					if (CollectionUtils.isNotEmpty(roleMembers)) {
						for (String role : roleMembers) {
							String displayName = (String) identityStoreRunAsSystem.execute("retrieveRoleDisplayName", new Class<?>[]{String.class, String.class}, new Object[]{role, JSFUtilities.getLocaleAsString()});
							
							if (StringUtils.isBlank(displayName)){
								displayName = role;
							}
							
							LazyLoadingCmsRoleTreeNodeRichFaces childGroupTreeNode = 
								new LazyLoadingCmsRoleTreeNodeRichFaces(
										identifier + ":" + String.valueOf(nodeIndex), 
										displayName, 
										this, 
										false,
										role);

							children.put(childGroupTreeNode.identifier, childGroupTreeNode);
							++nodeIndex;
						}
					}
				}
				else{
					List<Principal> roleMembersAsPrincipals = (List<Principal>) identityStoreRunAsSystem.execute("listMembers", new Class<?>[]{String.class}, new Object[]{role});
					
					if (roleMembersAsPrincipals != null){
						for (Principal roleMemberAsPrincipal : roleMembersAsPrincipals){

							//we are only interested in Principal named Roles
							/*if (StringUtils.equals(roleMemberAsPrincipal.getName(), AstroboaPrincipalName.Roles.toString())
									&& roleMemberAsPrincipal instanceof Group){
								
								//Iterate through members of this principal
								Enumeration<? extends Principal> members = ((Group)roleMemberAsPrincipal).members();
								
								while(members.hasMoreElements()){
									
									Principal roleMember = members.nextElement();
									LazyLoadingCmsRoleTreeNodeRichFaces childGroupTreeNode = 
										new LazyLoadingCmsRoleTreeNodeRichFaces(
												identifier + ":" + String.valueOf(nodeIndex), 
												roleMember.getName(), 
												this, 
												false,
												roleMember.getName());

									children.put(childGroupTreeNode.identifier, childGroupTreeNode);
									++nodeIndex;
								}
							}
							else{*/
							//We are only insterested in Persons assigned to this role
							if (! (roleMemberAsPrincipal instanceof Group))
							{
								persons.add((Person) identityStoreRunAsSystem.execute("retrieveUser", new Class<?>[]{String.class}, new Object[]{roleMemberAsPrincipal.getName()}));
							}
						}
					}
				}


				//Now load all Persons which belong to this role, 
				if (CollectionUtils.isNotEmpty(persons)) {
					for (Person person : persons) {
						LazyLoadingCmsPersonTreeNodeRichFaces personTreeNode = 
							new LazyLoadingCmsPersonTreeNodeRichFaces(
									identifier + ":" + String.valueOf(nodeIndex), 
									StringUtils.isBlank(person.getDisplayName()) ? person.getUsername() : person.getDisplayName(), 
											this, 
											person, 
											(List<String>) identityStoreRunAsSystem.execute("getImpliedRoles", new Class<?>[]{String.class}, new Object[]{person.getUsername()}));

						children.put(personTreeNode.identifier, personTreeNode);
						++nodeIndex;
					}
				}



				if (children.isEmpty()){
					leaf = true;
				}
			}	
			catch (Exception e) {
				logger.error("", e);
			}	

		}
		return children.entrySet().iterator();
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}


}
