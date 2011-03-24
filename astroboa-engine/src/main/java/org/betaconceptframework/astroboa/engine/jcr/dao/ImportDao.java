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


import java.net.URL;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
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
	

	
	public ImportReport importRepositoryContentFromURL(final URL contentSource) {
		
		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final ImportBean importBean = prototypeFactory.newImportBean();
		
		final ImportReport importReport = new ImportReportImpl();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				importBean.importRepositoryContentFromURL(contentSource, clientContext, importReport);
			}
		}).start();
		
		return importReport;
	}

	public ImportReport importRepositoryContentFromString(final String contentSource) {

		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final ImportBean importBean = prototypeFactory.newImportBean();
		
		final ImportReport importReport = new ImportReportImpl();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				importBean.importContentFromString(contentSource, importReport, ImportMode.SAVE_ENTITY_TREE, Repository.class, clientContext, false, true);
			}
		}).start();
		
		return importReport;
		
	}

	public ContentObject importContentObject(String contentSource,
			boolean version, boolean updateLastModificationDate, ImportMode importMode) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(contentSource, importReport, importMode,ContentObject.class, null, version, updateLastModificationDate);

	}
	
	public <T extends CmsRepositoryEntity> List<T> importResourceCollection(String contentSource,
			boolean version, boolean updateLastModificationDate, ImportMode importMode) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(contentSource, importReport, importMode, List.class, null, version, updateLastModificationDate);

	}
	
	public RepositoryUser importRepositoryUser(String repositoryUserSource,
			ImportMode importMode) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(repositoryUserSource, importReport, importMode, RepositoryUserImpl.class, null, false, true);

	}
	
	public Topic importTopic(String topicSource,
			ImportMode importMode) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(topicSource, importReport, importMode,TopicImpl.class, null, false, true);

	}

	public Taxonomy importTaxonomy(String taxonomySource,
			ImportMode importMode) {
		
	
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(taxonomySource, importReport, importMode,TaxonomyImpl.class, null, false, true);

	}

	public Space importSpace(String spaceSource,
			ImportMode importMode) {
		
		ImportReport importReport = new ImportReportImpl();
		
		return importBean.importContentFromString(spaceSource, importReport, importMode,SpaceImpl.class, null, false, true);

	}

}
