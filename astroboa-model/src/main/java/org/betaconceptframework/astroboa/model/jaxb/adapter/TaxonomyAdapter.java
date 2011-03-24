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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.type.TaxonomyType;

/**
 * Used to control marshaling mainly of a topic 's parent in order to avoid circular
 * problems.
 * 
 * Although both types are known to JAXB context, 
 * we copy provided Type to a new one which has less information
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyAdapter extends XmlAdapter<TaxonomyType, Taxonomy>{

	@Override
	public TaxonomyType marshal(Taxonomy taxonomy) throws Exception {
		return marshal(taxonomy, ResourceRepresentationType.XML);
	}
	
	
	public TaxonomyType marshal(Taxonomy taxonomy, ResourceRepresentationType<?>  resourceRepresentationType) throws Exception {

		if (taxonomy != null){
			TaxonomyType taxonomyType = new TaxonomyType();
			taxonomyType.setId(taxonomy.getId());
			taxonomyType.setName(taxonomy.getName());
			taxonomyType.setSystemBuiltinEntity(taxonomy.isSystemBuiltinEntity());
			taxonomyType.getLocalizedLabels().putAll(taxonomy.getLocalizedLabels());
			
			//TODO: Check whether user may have more control on whether a friendly url is generated or not
			taxonomyType.setUrl(taxonomy.getResourceApiURL(resourceRepresentationType, false, taxonomy.getName()!= null));
			
			if (taxonomy.getNumberOfRootTopics() > 0){
				taxonomyType.setNumberOfChildren(taxonomy.getNumberOfRootTopics());
			}

			return taxonomyType;
		}

		return null;
	}

	@Override
	public Taxonomy unmarshal(TaxonomyType taxonomyType) throws Exception {

		if (taxonomyType != null){

			Taxonomy taxonomy = (Taxonomy) taxonomyType.getCmsRepositoryEntityFromContextUsingCmsIdentifierOrReference();

			if (taxonomy != null){
				return taxonomy;
			}

			if (taxonomyType.getName() != null){
				taxonomy = (Taxonomy) taxonomyType.getCmsRepositoryEntityFromContextUsingKey(taxonomyType.getName());

				if (taxonomy != null){
					return taxonomy;
				}
			}


			taxonomy = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTaxonomy();

			taxonomy.setId(taxonomyType.getId());
			taxonomy.setName(taxonomyType.getName());
			taxonomy.setSystemBuiltinEntity(taxonomyType.isSystemBuiltinEntity());
			taxonomy.getLocalizedLabels().putAll(taxonomyType.getLocalizedLabels());

			taxonomyType.registerCmsRepositoryEntityToContext(taxonomy);

			return taxonomy;
		}

		return null;
	}

}
