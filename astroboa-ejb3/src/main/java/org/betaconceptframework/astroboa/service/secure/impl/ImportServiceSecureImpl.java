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

import java.net.URL;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.api.service.secure.ImportServiceSecure;
import org.betaconceptframework.astroboa.api.service.secure.remote.RemoteImportServiceSecure;
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

@Local({ImportServiceSecure.class})
@Remote({RemoteImportServiceSecure.class})
@Stateless(name="ImportServiceSecure")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({AstroboaSecurityAuthenticationInterceptor.class})
public class ImportServiceSecureImpl extends AbstractSecureAstroboaService implements ImportServiceSecure {
	
	private ImportService importService;
	
	void initializeOtherRemoteServices() {
		importService = (ImportService) springManagedRepositoryServicesContext.getBean("importService");
	}

	@RolesAllowed({"ROLE_CMS_EDITOR","ROLE_CMS_PORTAL_EDITOR","ROLE_CMS_TAXONOMY_EDITOR", "ROLE_CMS_IDENTITY_STORE_EDITOR"})
	public ImportReport importRepositoryContentFromString(String contentSource,String authenticationToken) {
		return importService.importRepositoryContentFromString(contentSource);
	}


	@RolesAllowed({"ROLE_CMS_EDITOR","ROLE_CMS_PORTAL_EDITOR","ROLE_CMS_TAXONOMY_EDITOR","ROLE_CMS_IDENTITY_STORE_EDITOR"})
	public ImportReport importRepositoryContentFromURL(URL contentSource,	String authenticationToken) {
		return importService.importRepositoryContentFromURL(contentSource);
	}

	@RolesAllowed({"ROLE_CMS_EDITOR", "ROLE_CMS_PORTAL_EDITOR", "ROLE_CMS_IDENTITY_STORE_EDITOR"})
	public ContentObject importContentObject(String contentSource,boolean version, boolean updateLastModificationDate, boolean save, 
			Map<String, byte[]> binaryContent,
			String authenticationToken) {
		return importService.importContentObject(contentSource,version, updateLastModificationDate,save, binaryContent);
	}

	@RolesAllowed({"ROLE_CMS_EDITOR"})
	public RepositoryUser importRepositoryUser(String repositoryUserSource, boolean save,
			String authenticationToken) {
		return importService.importRepositoryUser(repositoryUserSource,save);
	}

	@RolesAllowed({"ROLE_CMS_EDITOR"})
	public Space importSpace(String spaceSource, boolean save, String authenticationToken) {
		return importService.importSpace(spaceSource,save);
	}

	@RolesAllowed({"ROLE_CMS_TAXONOMY_EDITOR"})
	public Taxonomy importTaxonomy(String taxonomySource, boolean save,
			String authenticationToken) {
		return importService.importTaxonomy(taxonomySource,save);
	}

	@RolesAllowed({"ROLE_CMS_TAXONOMY_EDITOR"})
	public Topic importTopic(String topicSource, boolean save, String authenticationToken) {
		return importService.importTopic(topicSource,save);
	}
}
