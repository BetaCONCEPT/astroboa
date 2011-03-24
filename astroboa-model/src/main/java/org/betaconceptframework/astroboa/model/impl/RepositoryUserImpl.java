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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.RepositoryUserType;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlRootElement(name="repositoryUser")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "repositoryUserType", propOrder = {
	"folksonomy",
	"space"
})
public class RepositoryUserImpl extends CmsRepositoryEntityImpl implements RepositoryUser, Serializable {

	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = 7253219274729343083L;

	@XmlAttribute
	private String externalId;
	
	@XmlAttribute
	private String label;
	
	@XmlTransient
	private RepositoryUserType userType;
	
	@XmlElement(name="taxonomy", type=TaxonomyImpl.class)
	private Taxonomy folksonomy;
	
	@XmlElement(type=SpaceImpl.class)
	private Space space;
	
	
	@XmlTransient
	private Map<String, Object> preferences;
	
	public Taxonomy getFolksonomy() {
		return folksonomy;
	}

	public void setFolksonomy(Taxonomy folksonomy) {
		this.folksonomy = folksonomy;
		
		if (this.folksonomy != null){
			this.folksonomy.setName(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME);
		}
	}

	public Map<String, Object> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String userId) {
		this.externalId = userId;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the userType
	 */
	public RepositoryUserType getUserType() {
		return userType;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(RepositoryUserType userType) {
		this.userType = userType;
	}

	public void addPreference(String prefereceName, Object preferenceValue) {
		if (preferences == null)
			preferences = new HashMap<String, Object>();
		
		preferences.put(prefereceName, preferenceValue);
		
	}
	
	public String toString()
	{
		return externalId + " "+ label + " "+ getId();
	}

}
