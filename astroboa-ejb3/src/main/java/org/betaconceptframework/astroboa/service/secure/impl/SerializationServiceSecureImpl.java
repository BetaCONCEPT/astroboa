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

package org.betaconceptframework.astroboa.service.secure.impl;

import java.util.concurrent.Future;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.SerializationService;
import org.betaconceptframework.astroboa.api.service.secure.SerializationServiceSecure;
import org.betaconceptframework.astroboa.service.secure.interceptor.AstroboaSecurityAuthenticationInterceptor;

/**
 * This ejb3 bean does not implement the content service.
 * Repository services have been implemented through spring
 * The sole purpose of this class is to expose the content service as an ejb3 bean. 
 * It acquires the content service from JNDI where we have deployed a shared spring context with the beans that implement the repository services
 * Through the @Resource annotation we get the shared spring context from the JNDI and then we get the content service bean from the spring context.
 * The methods then use the spring bean to call the required functionality.
 * Another option would be to not separately deploy the spring context but have the ejb3 bean to bootstrap the spring and get the required beans.
 * This can be done through the following interceptor: @Interceptors(SpringBeanAutowiringInterceptor.class) and a refParentContext.xml file in repository module classpath that loads the 
 * spring bean files under a parent context.
 * We have chosen no to do so in order to allow applications to access the repository in both ways. Either as spring service beans or as ejb3 service beans.  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Local({SerializationServiceSecure.class})
@Stateless(name="SerializationServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class SerializationServiceSecureImpl extends AbstractSecureAstroboaService implements SerializationServiceSecure {
	
	private SerializationService serializationService;
	
	void initializeOtherRemoteServices() {
		serializationService = (SerializationService) springManagedRepositoryServicesContext.getBean("serializationService");
	}
	
	@RolesAllowed("ROLE_ADMIN")
	public Future<SerializationReport> serializeObjects(ContentObjectCriteria objectCriteria, SerializationConfiguration serializationConfiguration,String authenticationToken) {
		return serializationService.serializeObjects(objectCriteria, serializationConfiguration);
	}

	@RolesAllowed("ROLE_ADMIN")
	public Future<SerializationReport> serializeRepository(SerializationConfiguration serializationConfiguration,String authenticationToken) {
		return serializationService.serializeRepository(serializationConfiguration);
	}

	@RolesAllowed("ROLE_ADMIN")
	public Future<SerializationReport> serializeOrganizationSpace(String authenticationToken) {
		return serializationService.serializeOrganizationSpace();
	}

	@RolesAllowed("ROLE_ADMIN")
	public Future<SerializationReport> serializeRepositoryUsers(String authenticationToken) {
		return serializationService.serializeRepositoryUsers();
	}

	@RolesAllowed("ROLE_ADMIN")
	public Future<SerializationReport> serializeTaxonomies(String authenticationToken) {
		return serializationService.serializeTaxonomies();
	}
}
