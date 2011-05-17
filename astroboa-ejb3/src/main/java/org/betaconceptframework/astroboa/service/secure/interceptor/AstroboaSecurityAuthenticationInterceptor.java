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
package org.betaconceptframework.astroboa.service.secure.interceptor;

import java.util.Set;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.service.secure.impl.AbstractSecureAstroboaService;
import org.betaconceptframework.astroboa.service.secure.impl.RoleRegistry;
import org.betaconceptframework.astroboa.service.secure.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class performs all necessary security checks for executing a method of Astroboa Services API.
 * 
 * It is automatically enabled if the following annotation
 * @Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
 * is placed on any subclass of AbstractServiceSecure class.
 * 
 * It uses security service to validate provided authentication token
 * and then if the later is valid, it uses the SecurityContext for this authentication 
 * token whether the client (Subject) is authorized to call the method.
 * 
 * This interceptor is enabled just before the method of Astroboa Services API is invoked.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaSecurityAuthenticationInterceptor {

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource(name="SecurityService", mappedName="SecurityService/local")
	private SecurityService securityService;
	
	
	public AstroboaSecurityAuthenticationInterceptor() {
		
	}


	@AroundInvoke
    public Object checkAuthenticationToken(InvocationContext ctx) throws Exception
    {
		//This interceptor is attached only to beans extending the AbstractSecureAstroboaService class
		//
		if (AbstractSecureAstroboaService.class.isAssignableFrom(ctx.getTarget().getClass())){
			
			//Since their no way to obtain authentication token using its name
			//we expect that this is the last parameter which must be of type String
			String authenticationToken = null;

			String methodName = ctx.getTarget().getClass().getName()+"."+ctx.getMethod().getName();
			
			if (ctx.getParameters() != null && ctx.getParameters().length > 0){
				
				authenticationToken = retrieveAuthenticationTokenFromMethodParameters(ctx);
				
				SecurityContext securityContext = activateClientContextAndRetrieveSecurityContext(authenticationToken);
				
				String repositoryId = AstroboaClientContextHolder.getActiveRepositoryId();
				
				//Perform role based authorization on class level
				//performRoleBasedAuthorization(ctx.getTarget().getClass().getAnnotation(RolesAllowed.class), securityContext, methodName);
				
				//Perform role based authorization on method level
				performRoleBasedAuthorization(methodName, securityContext, repositoryId);

				try{
					
					return ctx.proceed();
				}
				catch(CmsException e){
					logger.error("",e);
					throw e;
				}
				catch(Exception ex){
					logger.error("",ex);
					throw new CmsException(ex);
				}
			}
			
			throw new CmsException("Unable to find authentication token parameter value in method "+ methodName + " in class "+
					ctx.getTarget().getClass().getName());
			
		}
		else{
			logger.warn(" Class "+ctx.getTarget() +" is not extending "+ AbstractSecureAstroboaService.class.getName() + " and cannot be processed by interceptor "+this.getClass().getName());
			
			try{
				return ctx.proceed();
			}
			catch(CmsException e){
				logger.error("",e);
				throw e;
			}
			catch(Exception ex){
				logger.error("",ex);
				throw ex;
			}
		}
		
		
    }


	private SecurityContext activateClientContextAndRetrieveSecurityContext(String authenticationToken) {
		
		securityService.registerAndActivateClientContextForAuthenticationToken(authenticationToken);
		
		//Get roles for authenticated user from RepositoryContext
		SecurityContext securityContext = AstroboaClientContextHolder.getActiveSecurityContext();
		
		if (securityContext == null){
			throw new CmsException("No security context found although client's context for authentication token "+authenticationToken+" has been activated");
		}
		
		return securityContext;
	}


	//Authentication Token is a String and is expected to be the last parameter of the invoked method
	private String retrieveAuthenticationTokenFromMethodParameters(InvocationContext ctx) {
		
		Object lastParameter = ctx.getParameters()[ctx.getParameters().length-1];
		
		if (lastParameter != null && lastParameter instanceof String){
			return (String) lastParameter;
		}
		
		return null;
	}


	private void performRoleBasedAuthorization(String methodName, SecurityContext securityContext, String repositoryId) {
		
		Set<String> rolesAllowed = RoleRegistry.INSTANCE.getRolesAllowed(methodName,repositoryId);

		if (rolesAllowed != null && rolesAllowed.size() > 0){

			for (String roleAllowed : rolesAllowed){
				if (securityContext.hasRole(roleAllowed)){
					return ;
				}
			}
			
			throw new CmsUnauthorizedAccessException("User "+securityContext.getIdentity()+ " is not authorized to access method "+ methodName + 
					" Roles "+ securityContext.getAllRoles()+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());
		}
	}

}
