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
import java.util.Calendar;
import java.util.List;

import javax.jcr.Session;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.engine.definition.RepositoryEntityResolver;
import org.betaconceptframework.astroboa.engine.jcr.dao.RepositoryUserDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.SpaceDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TaxonomyDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.TopicDao;
import org.betaconceptframework.astroboa.engine.jcr.io.contenthandler.ImportContentHandler;
import org.betaconceptframework.astroboa.engine.jcr.io.contenthandler.JsonImportContentHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.model.jaxb.Repository;
import org.betaconceptframework.astroboa.engine.service.jcr.ContentServiceImpl;
import org.betaconceptframework.astroboa.model.impl.io.ImportReportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.istack.XMLStreamReaderToContentHandler;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class Deserializer {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private RepositoryEntityResolver repositoryEntityResolver;

	private TaxonomyDao taxonomyDao;

	private TopicDao topicDao;

	private RepositoryUserDao repositoryUserDao;

	private ContentService contentService;

	private ImportReport importReport;

	private SpaceDao spaceDao;

	private ImportMode importMode;

	private boolean version;

	private boolean updateLastModificationDate = true;

	private Context context;

	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	private CmsQueryHandler cmsQueryHandler;

	private Session session;

	public void setRepositoryEntityResolver(
			RepositoryEntityResolver repositoryEntityResolver) {
		this.repositoryEntityResolver = repositoryEntityResolver;
	}

	public void setSpaceDao(SpaceDao spaceDao) {
		this.spaceDao = spaceDao;
	}

	public void setTaxonomyDao(TaxonomyDao taxonomyDao) {
		this.taxonomyDao = taxonomyDao;
	}

	public void setTopicDao(TopicDao topicDao) {
		this.topicDao = topicDao;
	}

	public void setRepositoryUserDao(RepositoryUserDao repositoryUserDao) {
		this.repositoryUserDao = repositoryUserDao;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setCmsRepositoryEntityUtils(
			CmsRepositoryEntityUtils cmsRepositoryEntityUtils) {
		this.cmsRepositoryEntityUtils = cmsRepositoryEntityUtils;
	}

	public void setCmsQueryHandler(CmsQueryHandler cmsQueryHandler) {
		this.cmsQueryHandler = cmsQueryHandler;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public <T> T deserializeContent(InputStream source, boolean jsonSource, ImportMode importMode, Class<T> classWhoseContentIsImported, 
			boolean version, boolean updateLastModificationDate){

		this.importMode = importMode;
		this.version = version;
		this.updateLastModificationDate = updateLastModificationDate;

		if (importMode == null){
			if (classWhoseContentIsImported == Repository.class || List.class.isAssignableFrom(classWhoseContentIsImported)){
				this.importMode = ImportMode.SAVE_ENTITY_TREE;
			}
			else{
				this.importMode = ImportMode.DO_NOT_SAVE;
			}
		}
		
		if (classWhoseContentIsImported == Repository.class || List.class.isAssignableFrom(classWhoseContentIsImported)){
			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
		}


		XMLStreamReader xmlStreamReader = null;
		SAXSource saxSource = null;
		try{

			Object entity = null;

			long start = System.currentTimeMillis();

			if (jsonSource){

				xmlStreamReader = CmsEntityDeserialization.Context.createJSONReader(source, true, classWhoseContentIsImported);

				JsonImportContentHandler<T> handler = new JsonImportContentHandler(classWhoseContentIsImported, this);

				XMLStreamReaderToContentHandler xmlStreamReaderToContentHandler = new XMLStreamReaderToContentHandler(xmlStreamReader, handler, false, false);
				xmlStreamReaderToContentHandler.bridge();
				entity = handler.getResult();

				logger.debug(" Unmarshal json to {} took {}", classWhoseContentIsImported.getSimpleName(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS"));
			}
			else{
				saxSource = CmsEntityDeserialization.Context.createSAXSource(source, repositoryEntityResolver,	false); //we explicitly disable validation because partial saves are allowed

				ImportContentHandler<T> handler = new ImportContentHandler(classWhoseContentIsImported, this);
				saxSource.getXMLReader().setContentHandler(handler);

				saxSource.getXMLReader().parse(saxSource.getInputSource());
				entity = handler.getImportResult();

				logger.debug("Unmarshal xml to {} took {}",classWhoseContentIsImported.getSimpleName(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS"));
			}

			//If entity is not of type T then a class cast exception is thrown.
			if (entity instanceof CmsRepositoryEntity){
				entity = save((CmsRepositoryEntity) entity);
			}
			
			return (T) entity;

		}
		catch(CmsException e){
			logger.error("",e);
			throw e;
		}
		catch(Exception e){
			logger.error("",e);
			throw new CmsException(e.getMessage());
		}
		finally{

			if (xmlStreamReader != null){
				try {
					xmlStreamReader.close();
				} catch (Exception e) {
					//Ignore exception
				}
			}

			if (saxSource != null && saxSource.getInputSource() != null){
				IOUtils.closeQuietly(saxSource.getInputSource().getByteStream());
				IOUtils.closeQuietly(saxSource.getInputSource().getCharacterStream());
			}
			
			if (context != null){
				context.dispose();
				context = null;
			}

		}
	}

	public CmsRepositoryEntity save(CmsRepositoryEntity entity) {

		if (entity != null && shouldSaveEntity()){
			if (entity instanceof ContentObject){

				if (logger.isDebugEnabled()){
					logger.debug("\n\nStarting ContentObject Save...{}", DateFormatUtils.format(Calendar.getInstance(), "dd/MM/yyyy HH:mm:ss.SSS"));
				}
				
				long start = System.currentTimeMillis();
				//We call ContentServiceImpl as security aspect must run
				//prior to saving a content object
				entity = ((ContentServiceImpl)contentService).saveContentObjectInBatchMode((ContentObject)entity, version, updateLastModificationDate, context);

				if (logger.isDebugEnabled()){
					logger.debug("Saving ContentObject from import took {}, {}",
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.SSSSSS"), 
						DateFormatUtils.format(Calendar.getInstance(), "dd/MM/yyyy HH:mm:ss.SSSSSS"));
				}

				if (importReport != null){
					((ImportReportImpl)importReport).addContentObjectsImported(1);
				}
			}
			else if (entity instanceof Topic){

				if (shouldSaveEntityTree()){
					entity = topicDao.saveTopicTree((Topic)entity, null);
				}
				else{
					entity = topicDao.saveTopic((Topic)entity, null);
				}

				if (importReport != null){
					((ImportReportImpl)importReport).addTopicsImported(1);
				}
			}
			else if (entity instanceof Taxonomy){

				if (shouldSaveEntityTree()){
					entity = taxonomyDao.saveTaxonomyTree((Taxonomy)entity);
				}
				else{
					entity = taxonomyDao.saveTaxonomy((Taxonomy)entity);
				}

				if (importReport != null){
					((ImportReportImpl)importReport).addTaxonomiesImported(1);
				}
			}
			else if (entity instanceof RepositoryUser){

				entity = repositoryUserDao.saveRepositoryUser((RepositoryUser)entity);

				if (importReport != null){
					((ImportReportImpl)importReport).addRepositoryUsersImported(1);
				}
			}
			else if (entity instanceof Space){

				if (shouldSaveEntityTree()){
					entity = spaceDao.saveSpaceTree((Space)entity);
				}
				else{
					entity = spaceDao.saveSpace((Space)entity);
				}

				if (importReport != null){
					((ImportReportImpl)importReport).addSpacesImported(1);
				}
			}
			else{
				logger.warn("Importing of entity {} is not yet supported", entity.toString());
			}

		}
		
		return entity;

	}

	private boolean shouldSaveEntity() {
		return importMode != null && importMode != ImportMode.DO_NOT_SAVE;
	}

	private boolean shouldSaveEntityTree(){
		return importMode != null && importMode == ImportMode.SAVE_ENTITY_TREE;
	}

	public void setImportReport(ImportReport importReport) {
		this.importReport = importReport;

	}
}
