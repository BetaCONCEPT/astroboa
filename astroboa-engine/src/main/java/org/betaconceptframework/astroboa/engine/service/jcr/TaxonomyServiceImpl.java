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


import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.engine.jcr.dao.TaxonomyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Transactional(readOnly = true, rollbackFor = CmsException.class)
class TaxonomyServiceImpl  implements TaxonomyService {


	@Autowired
	private TaxonomyDao taxonomyDao; 

	@Deprecated
	public List<Taxonomy> getTaxonomies(String locale) {
		try{
			CmsOutcome<Taxonomy> outcome = taxonomyDao.serializeAllTaxonomies(ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.ENTITY, false);
			
			if (outcome.getResults() != null){
				return outcome.getResults();
			}
			else{
				return new ArrayList<Taxonomy>();
			}
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Deprecated
	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Taxonomy saveTaxonomy(Taxonomy taxonomy)  {
		try{
			return taxonomyDao.saveTaxonomy(taxonomy);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	@Deprecated
	public Taxonomy getTaxonomy(String taxonomyName, String locale) {
		try{
			return taxonomyDao.getTaxonomy(taxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public void deleteTaxonomyTree(String taxonomyId) {
		try{
			taxonomyDao.deleteTaxonomy(taxonomyId);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	public Taxonomy getBuiltInSubjectTaxonomy(String locale) {
		try{
			return taxonomyDao.getTaxonomy(Taxonomy.SUBJECT_TAXONOMY_NAME, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	
	@Override
	public <T> T getAllTaxonomies(ResourceRepresentationType<T> output, FetchLevel fetchLevel, boolean prettyPrint) {
		try{
			return taxonomyDao.serializeAllTaxonomies(output, fetchLevel, prettyPrint);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	public <T> T getTaxonomy(String taxonomyName, ResourceRepresentationType<T> output,
			FetchLevel fetchLevel, boolean prettyPrint) {
		try{
			return taxonomyDao.getTaxonomy(taxonomyName, output, fetchLevel, prettyPrint);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}

	}

	@Transactional(readOnly = false, rollbackFor = CmsException.class)
	public Taxonomy save(Object taxonomySource) {
		try{
			return taxonomyDao.saveTaxonomy(taxonomySource);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

	@Override
	@Deprecated
	public Taxonomy getTaxonomyById(String taxonomyId) {
		try{
			return taxonomyDao.getTaxonomy(taxonomyId, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) { 
			throw new CmsException(e); 		
		}
	}

}
