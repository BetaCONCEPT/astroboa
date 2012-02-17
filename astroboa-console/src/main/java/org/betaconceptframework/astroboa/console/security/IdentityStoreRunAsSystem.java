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
package org.betaconceptframework.astroboa.console.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.acl.Group;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.management.IdentityStore;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * When a user logins to an Astroboa repository through Astroboa Client,
 * it has access to the IdentityStore of that repository. 
 * 
 * IdentityStore may be one of the following :
 * 
 * 1. An external IdentityStore
 * 2. A Astroboa repository which is represented by a repository different than the one user has logged in
 * 3. The same repository user has logged in
 *  
 *  In cases 2 and 3 it may happen that the user will not have the necessary roles
 *  to perform some actions to this identity store, such as change its profile, or 
 *  view all users and /or roles in order to select which one has access to its content objects.
 *  
 *  In such cases, user must explicitly log in as SYSTEM to that repository in order to
 *  perform the necessary action.
 *  
 *  This procedure is triggered only when IdentityStore is represented by a Astroboa repository.
 *  
 *  This class contains one method which is responsible to login to IdentityStore repository
 *  as SYSTEM, execute the provided method and then log out
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("identityStoreRunAsSystem")
@Scope(ScopeType.SESSION)
public class IdentityStoreRunAsSystem {

	private IdentityStore identityStoreLoggedAsSystem;
	
	private AstroboaClient activeAstroboaClient;
	
	public Object execute(String methodName, Class<?>[] methodParameterTypes, Object[] arguments) {
		
		if (identityStoreLoggedAsSystem == null ){
		
			if (activeAstroboaClient == null){
				throw new CmsException("No active Astroboa repository client found");
			}
			
			activeAstroboaClient.activateClientContext();
			
			CmsRepository activeCmsRepository = AstroboaClientContextHolder.getActiveCmsRepository();
			
			if (activeCmsRepository == null){
				throw new CmsException("Found no active CmsRepository");
			}
			
			if (StringUtils.isNotBlank(activeCmsRepository.getExternalIdentityStoreJNDIName())){
				identityStoreLoggedAsSystem = activeAstroboaClient.getIdentityStore();
			}
			else{
				//Must create a new client which will be connected as SYSTEM
				Subject subject = createSubjectForSystemUserAndItsRoles(activeCmsRepository.getIdentityStoreRepositoryId());
				
				AstroboaClient clientForIdentityStore = new AstroboaClient(activeAstroboaClient.getServerHostNameOrIpAndPortToConnectTo());
				clientForIdentityStore.login(activeCmsRepository.getIdentityStoreRepositoryId(), subject);
				
				identityStoreLoggedAsSystem = clientForIdentityStore.getIdentityStore();
			}
		}
		
		//Execute method
		
		
		try{
			Method methodToExecute = identityStoreLoggedAsSystem.getClass().getMethod(methodName, methodParameterTypes);
			
			return methodToExecute.invoke(identityStoreLoggedAsSystem, arguments);
		
		}
		catch(CmsException e){
			throw e;
		}
		catch(InvocationTargetException e){
			if (e.getCause() != null && e.getCause() instanceof CmsException){
				throw (CmsException)e.getCause();
			}
			
			throw new CmsException(e);
			
			
		}
		catch(Exception e){
			throw new CmsException(e);
		}
		finally{
			//Register previous Astroboa Client context to Thread
			activeAstroboaClient.activateClientContext();
			
		}
	}


	private Subject createSubjectForSystemUserAndItsRoles(String cmsRepositoryId){
		
		Subject subject = new Subject();
		
		//System identity
		subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));
		
		//Load default roles for SYSTEM USER
		//Must return at list one group named "Roles" in order to be 
		Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());
		
		for (CmsRole cmsRole : CmsRole.values()){
			rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, cmsRepositoryId)));
		}

		subject.getPrincipals().add(rolesPrincipal);
		
		return subject;
	}

	
	/**
	 * @param activeAstroboaClient the activeAstroboaClient to set
	 */
	public void setActiveAstroboaClient(
			AstroboaClient activeAstroboaClient) {
		this.activeAstroboaClient = activeAstroboaClient;
	}


	public void reset() {
		identityStoreLoggedAsSystem = null;
	}
	
}
