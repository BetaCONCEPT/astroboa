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
package org.betaconceptframework.astroboa.engine.jcr.dao;


import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.SpaceImpl;
import org.betaconceptframework.astroboa.model.impl.TaxonomyImpl;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.astroboa.model.impl.io.ImportReportImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ImportDao {

	//Use this method only if you want to use importBean in a newly
	//created Thread
	@Autowired
	public PrototypeFactory prototypeFactory;
	
	@Autowired
	private ImportBean importBean;
	
	//TODO : The use of the ExecutorService must be reviewed. 
	private	ExecutorService executorService = Executors.newCachedThreadPool();

	
	public Future<ImportReport> importRepositoryContentFromURI(final URI contentSource, final ImportConfiguration importConfiguration) {
		
		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final ImportBean importBean = prototypeFactory.newImportBean();
		
		final ImportReport importReport = new ImportReportImpl();
		
		Future<ImportReport> future = executorService.submit(new Callable<ImportReport>() {

			@Override
			public ImportReport call() throws Exception {
				importBean.importRepositoryContentFromURI(contentSource, clientContext, importReport, importConfiguration);
				return importReport;
			}

		});
		
		
		return future;

	}

	public Future<ImportReport> importRepositoryContentFromString(final String contentSource,final ImportConfiguration importConfiguration) {

		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final ImportBean importBean = prototypeFactory.newImportBean();
		
		final ImportReport importReport = new ImportReportImpl();
		
		Future<ImportReport> future = executorService.submit(new Callable<ImportReport>() {

			@Override
			public ImportReport call() throws Exception {
				importBean.importContentFromString(contentSource, importReport, Repository.class, clientContext, importConfiguration);
				return importReport;
			}

		});
		
		
		return future;
		
	}

	public ContentObject importContentObject(String contentSource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(contentSource, importReport, ContentObject.class, null, importConfiguration);

	}
	
	public <T extends CmsRepositoryEntity> List<T> importResourceCollection(String contentSource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(contentSource, importReport, List.class, null, importConfiguration);

	}
	
	public RepositoryUser importRepositoryUser(String repositoryUserSource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(repositoryUserSource, importReport, RepositoryUserImpl.class, null, importConfiguration);

	}
	
	public Topic importTopic(String topicSource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(topicSource, importReport, TopicImpl.class, null, importConfiguration);

	}

	public Taxonomy importTaxonomy(String taxonomySource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(taxonomySource, importReport, TaxonomyImpl.class, null, importConfiguration);

	}

	public Space importSpace(String spaceSource,ImportConfiguration importConfiguration) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(spaceSource, importReport, SpaceImpl.class, null, importConfiguration);

	}

}
