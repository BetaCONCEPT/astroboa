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

package org.betaconceptframework.astroboa.engine.service.jcr;


import java.net.URL;
import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.service.ImportService;
import org.betaconceptframework.astroboa.engine.jcr.dao.ImportDao;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
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
	public ImportReport importRepositoryContentFromString(String contentSource) {
		try{ 
			return importDao.importRepositoryContentFromString(contentSource);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public ImportReport importRepositoryContentFromURL(URL contentSource) {
		try{ 
			return importDao.importRepositoryContentFromURL(contentSource);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public ContentObject importContentObject(String contentSource,boolean version, boolean updateLastModificationDate, boolean save,Map<String, byte[]> binaryContent) {
		try{ 
			return importDao.importContentObject(contentSource, version, updateLastModificationDate, save ? ImportMode.SAVE_ENTITY_TREE :ImportMode.DO_NOT_SAVE, binaryContent);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public RepositoryUser importRepositoryUser(String repositoryUserSource, boolean save) {
		try{ 
			return importDao.importRepositoryUser(repositoryUserSource, save? ImportMode.SAVE_ENTITY_TREE: ImportMode.DO_NOT_SAVE);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Space importSpace(String spaceSource, boolean save) {
		try{ 
			return importDao.importSpace(spaceSource, save? ImportMode.SAVE_ENTITY_TREE: ImportMode.DO_NOT_SAVE);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Taxonomy importTaxonomy(String taxonomySource, boolean save) {
		try{ 
			return importDao.importTaxonomy(taxonomySource, save? ImportMode.SAVE_ENTITY_TREE:ImportMode.DO_NOT_SAVE);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Topic importTopic(String topicSource, boolean save) {
		try{ 
			return importDao.importTopic(topicSource, save? ImportMode.SAVE_ENTITY_TREE:ImportMode.DO_NOT_SAVE);
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
			String contentSource, boolean version,
			boolean updateLastModificationTime, boolean save) {
		try{ 
			return importDao.importResourceCollection(contentSource, version, updateLastModificationTime, ImportMode.SAVE_ENTITY_TREE);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}
}