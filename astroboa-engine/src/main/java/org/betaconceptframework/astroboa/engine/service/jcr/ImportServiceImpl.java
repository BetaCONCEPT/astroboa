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

package org.betaconceptframework.astroboa.engine.service.jcr;


import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.engine.jcr.dao.ImportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class ImportServiceImpl  implements ImportService {

	@Autowired
	private ImportDao importDao;

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Future<ImportReport> importRepositoryContentFromString(String contentSource,ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importRepositoryContentFromString(contentSource,importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Future<ImportReport> importRepositoryContentFromURI(URI contentSource,ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importRepositoryContentFromURI(contentSource,importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public ContentObject importContentObject(String contentSource,ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importContentObject(contentSource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public RepositoryUser importRepositoryUser(String repositoryUserSource, ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importRepositoryUser(repositoryUserSource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Space importSpace(String spaceSource, ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importSpace(spaceSource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Taxonomy importTaxonomy(String taxonomySource, ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importTaxonomy(taxonomySource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Topic importTopic(String topicSource, ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importTopic(topicSource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public List<ContentObject> importContentObjectResourceCollection(
			String contentSource, ImportConfiguration importConfiguration) {
		try{ 
			return importDao.importResourceCollection(contentSource, importConfiguration);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
}