/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.io;

import java.io.InputStream;
import java.net.URL;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.definition.RepositoryEntityResolver;
import org.betaconceptframework.astroboa.engine.jcr.dao.JcrDaoSupport;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.SpaceDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TaxonomyDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TopicDao;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.model.impl.io.ImportReportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible to import content from a provided URL
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class, propagation=Propagation.SUPPORTS)
public class ImportBean extends JcrDaoSupport{

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RepositoryEntityResolver repositoryEntityResolver;
	
	@Autowired
	//Instead of injecting ContentDao
	//We inject ContentService so that
	//aspect during content object save is triggered
	private ContentService contentService;

	@Autowired
	private RepositoryUserDao repositoryUserDao;
	
	@Autowired
	private TaxonomyDao taxonomyDao;
	
	@Autowired
	private TopicDao topicDao;

	@Autowired
	private SpaceDao spaceDao;
	
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	@Autowired
	private CmsQueryHandler cmsQueryHandler;

	@Transactional(readOnly = false, rollbackFor = CmsException.class, propagation=Propagation.REQUIRES_NEW)
	public void importRepositoryContentFromURL(URL contentSource, AstroboaClientContext clientContext, ImportReport importReport){

		long start = System.currentTimeMillis();
		
		if (clientContext == null){
			throw new CmsException("Astroboa client context is not provided. Export failed");
		}

		InputStream xml = null; 
		ContentSourceExtractor contentSourceExtractor = null;
		try{

			//Register client context 
			AstroboaClientContextHolder.registerClientContext(clientContext, true);

			if (contentSource != null){
				contentSourceExtractor = new ContentSourceExtractor();
				
				xml = contentSourceExtractor.extractXmlFromSourceURL(contentSource);
				
				if (xml == null){
					throw new Exception("Could not locate xml content source in URL "+contentSource.toString());
				}
				
				performImport(importReport, xml, false, ImportMode.SAVE_ENTITY_TREE, Repository.class, false, false, getSession());
			}

		}
		catch(CmsException e){
			addErrorToReport(importReport, e, contentSource.toString());
			throw e;
		}
		catch(Exception e){
			addErrorToReport(importReport, e, contentSource.toString());
			throw new CmsException(e);
		}
		finally{
			completeImport(importReport, start, xml);			
			
			if (contentSourceExtractor != null){
				contentSourceExtractor.dispose();
			}
		}

	}
	
	@Transactional(readOnly = false, rollbackFor = CmsException.class, propagation=Propagation.REQUIRED)
	public <T> T importContentFromString(String contentSource, ImportReport importReport, 
			ImportMode importMode, Class<T> classToBeImported, AstroboaClientContext clientContext, 
			boolean version, boolean updateLastModificationDate){

		long start = System.currentTimeMillis();
		
		InputStream xml = null; 

		try{
			
			if (clientContext != null){
				AstroboaClientContextHolder.registerClientContext(clientContext, true);
			}

			if (contentSource != null){
				
				//Identify whether source is XML or JSON
				boolean sourceIsXML = contentSource.startsWith("<?xml version=\"1.0\"");
				
				xml = IOUtils.toInputStream(contentSource, "UTF-8");
				
				if (xml == null){
					throw new Exception("Content source is invalid "+ contentSource);
				}
				
				return performImport(importReport, xml, ! sourceIsXML, importMode, classToBeImported, version, updateLastModificationDate, getSession());
			}
		}
		catch(CmsException e){
			addErrorToReport(importReport, e, contentSource);
			throw e;
		}
		catch(Exception e){
			addErrorToReport(importReport, e, contentSource);
			throw new CmsException(e);
		}
		finally{
			completeImport(importReport, start, xml);
		}

		return null;

	}

	private <T> T performImport(ImportReport importReport, InputStream source, boolean jsonSource,  
			ImportMode importMode, Class<T> classToBeImported, boolean version, boolean updateLastModificationDate, Session session) {
		
		Deserializer deserializer = new Deserializer();
		deserializer.setContentService(contentService);
		deserializer.setRepositoryEntityResolver(repositoryEntityResolver);
		deserializer.setRepositoryUserDao(repositoryUserDao);
		deserializer.setTaxonomyDao(taxonomyDao);
		deserializer.setTopicDao(topicDao);
		deserializer.setSpaceDao(spaceDao);
		deserializer.setCmsQueryHandler(cmsQueryHandler);
		deserializer.setCmsRepositoryEntityUtils(cmsRepositoryEntityUtils);
		deserializer.setSession(session);
		deserializer.setImportReport(importReport);
		
		return (T) deserializer.deserializeContent(source, jsonSource, importMode, classToBeImported, 
				version, updateLastModificationDate);
		
	}

	private void completeImport(ImportReport importReport, long start,
			InputStream xml) {

		((ImportReportImpl)importReport).setFinished(true);
		
		logger.debug("Imported {} entities (" +
				"{} repository users, {} taxonomies, " +
				"{} spaces, {} contentObjects"+
				") in {} ", 
				new Object[]{
			((ImportReportImpl)importReport).getTotalCountOfImportedEntities(),
			importReport.getNumberOfRepositoryUsersImported(),
			importReport.getNumberOfTaxonomiesImported(),
			importReport.getNumberOfSpacesImported(),
			importReport.getNumberOfContentObjectsImported(),
				DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-start) }
		);
		
		IOUtils.closeQuietly(xml);

	}

	private void addErrorToReport(ImportReport importReport, Exception e, String contentSource) {
		logger.error(contentSource,e);
		((ImportReportImpl)importReport).getErrors().add(e.getMessage());
	}

}
