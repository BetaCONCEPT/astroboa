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
package org.betaconceptframework.astroboa.service.secure.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum RoleRegistry {

	INSTANCE;
	
	private Map<String, Map<String, Set<String>>> rolesAllowedPerMethodPerRepository = new HashMap<String, Map<String,Set<String>>>();

	public void registerRolesAllowedPerMethodForClass(Class<?> serviceClass) {
		
		if (serviceClass == null){
			return;
		}
		
		//Retrieve all repository ids
		Set<String> repositoryIds = RepositoryRegistry.INSTANCE.getConfigurationsPerRepositoryId().keySet();
		
		//First load all roles allowed which are defined in class level
		String serviceClassName = serviceClass.getName();
		
		//Visit all methods of the provided class
		Method[] methods = serviceClass.getMethods();
		
		if (methods != null && methods.length > 0){
			
			for (Method method: methods){
				loadRoles(serviceClassName+"."+method.getName(), serviceClass.getAnnotation(RolesAllowed.class), repositoryIds);
			}
		}
		
	}

	private void loadRoles(String methodName, RolesAllowed rolesAllowedAnnotation,Set<String> repositoryIds) {
		
		if (rolesAllowedAnnotation != null && rolesAllowedAnnotation.value() != null && rolesAllowedAnnotation.value().length > 0){
			
			//Get roles defined in annotation
			String[] rolesAllowed = rolesAllowedAnnotation.value();
			
			if (! rolesAllowedPerMethodPerRepository.containsKey(methodName)){
				rolesAllowedPerMethodPerRepository.put(methodName, new HashMap<String, Set<String>>());
			}
			
			for (String roleAllowed : rolesAllowed){
				for (String repositoryId: repositoryIds){
					
					if (! rolesAllowedPerMethodPerRepository.get(methodName).containsKey(repositoryId)){
						rolesAllowedPerMethodPerRepository.get(methodName).put(repositoryId, new HashSet<String>());
					}
					
					rolesAllowedPerMethodPerRepository.get(methodName).get(repositoryId).add(processRole(roleAllowed, repositoryId));

				}
			}
		}
	}
	
	/*
	 * In case role is a Astroboa built in role
	 * we need to attach active repository Id
	 */
	private String processRole(String role, String repositoryId) {
		
		if (role == null){
			return role;
		}
		
		try{
			CmsRole cmsRole = CmsRole.valueOf(role);
			
			return CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole,repositoryId);
		}
		catch(Exception e){
			//Role is not a built in role continue as is
			return role;
		}
		
		
	}

	public Set<String> getRolesAllowed(String methodName, String repositoryId) {
		
		if (rolesAllowedPerMethodPerRepository.containsKey(methodName)){
			if (rolesAllowedPerMethodPerRepository.get(methodName).containsKey(repositoryId)){
				return Collections.unmodifiableSet(rolesAllowedPerMethodPerRepository.get(methodName).get(repositoryId));
			}
		}
		
		return null;
	}

	
}
