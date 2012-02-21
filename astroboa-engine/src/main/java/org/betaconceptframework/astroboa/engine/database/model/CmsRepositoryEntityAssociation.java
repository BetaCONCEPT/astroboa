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
package org.betaconceptframework.astroboa.engine.database.model;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsRepositoryEntityAssociation {

	private Integer cmsRepositoryEntityAssociationId;
	
	private String referrerCmsRepositoryEntityId;
	
	private String referrerPropertyContainerId;
	
	private String referrerPropertyName;
	
	private String referencedCmsRepositoryEntityId;

	public String getReferrerCmsRepositoryEntityId() {
		return referrerCmsRepositoryEntityId;
	}

	public void setReferrerCmsRepositoryEntityId(
			String referrerCmsRepositoryEntityId) {
		this.referrerCmsRepositoryEntityId = referrerCmsRepositoryEntityId;
	}

	public String getReferrerPropertyContainerId() {
		return referrerPropertyContainerId;
	}

	public void setReferrerPropertyContainerId(String referrerPropertyContainerId) {
		this.referrerPropertyContainerId = referrerPropertyContainerId;
	}

	public String getReferrerPropertyName() {
		return referrerPropertyName;
	}

	public void setReferrerPropertyName(String referrerPropertyName) {
		this.referrerPropertyName = referrerPropertyName;
	}

	public String getReferencedCmsRepositoryEntityId() {
		return referencedCmsRepositoryEntityId;
	}

	public void setReferencedCmsRepositoryEntityId(
			String referencedCmsRepositoryEntityId) {
		this.referencedCmsRepositoryEntityId = referencedCmsRepositoryEntityId;
	}

	public void setCmsRepositoryEntityAssociationId(
			Integer cmsRepositoryEntityAssociationId) {
		this.cmsRepositoryEntityAssociationId = cmsRepositoryEntityAssociationId;
	}

	public Integer getCmsRepositoryEntityAssociationId() {
		return cmsRepositoryEntityAssociationId;
	}

	public String toString()
	{
		return "CmsRepositoryEntity native id :"+ cmsRepositoryEntityAssociationId+
		" Referrer node id :" +referrerCmsRepositoryEntityId +
		((referrerPropertyContainerId == null)? "" :" and its property container "+ referrerPropertyContainerId )+
		" has property "+ referrerPropertyName+
		" which refers to CmsRepositoryEntity "+ referencedCmsRepositoryEntityId ;
	}
}
