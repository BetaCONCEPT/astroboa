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

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.type.RepositoryUserType;

/**
 * Used to control marshalling mainly of a repository user
 * problems.
 * 
 * Although both types are known to JAXB context, 
 * we copy provided Type to a new one which has less
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserAdapter extends XmlAdapter<RepositoryUserType, RepositoryUser>{

	@Override
	public RepositoryUserType marshal(RepositoryUser repositoryUser) throws Exception {

		if (repositoryUser != null){
			RepositoryUserType repositoryUserType = new RepositoryUserType();
			repositoryUserType.setId(repositoryUser.getId());
			repositoryUserType.setExternalId(repositoryUser.getExternalId());
			repositoryUserType.setLabel(repositoryUser.getLabel());

			return repositoryUserType;
		}

		return null;
	}

	@Override
	public RepositoryUser unmarshal(RepositoryUserType repositoryUserType) throws Exception {

		if (repositoryUserType != null){

			RepositoryUser repositoryUser = (RepositoryUser) repositoryUserType.getCmsRepositoryEntityFromContextUsingCmsIdentifierOrReference();

			//User found
			if (repositoryUser != null){
				return repositoryUser;
			}

			//user not found. check if it has been cached using its externalId
			if (repositoryUserType.getExternalId() != null){
				repositoryUser = (RepositoryUser) repositoryUserType.getCmsRepositoryEntityFromContextUsingKey(repositoryUserType.getExternalId());

				if (repositoryUser != null){
					return repositoryUser;
				}
			}


			repositoryUser = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser();

			repositoryUser.setId(repositoryUserType.getId());
			repositoryUser.setExternalId(repositoryUserType.getExternalId());
			repositoryUser.setLabel(repositoryUserType.getLabel());

			repositoryUserType.registerCmsRepositoryEntityToContext(repositoryUser);

			return (RepositoryUser) repositoryUser;
		}

		return null;
	}

}
