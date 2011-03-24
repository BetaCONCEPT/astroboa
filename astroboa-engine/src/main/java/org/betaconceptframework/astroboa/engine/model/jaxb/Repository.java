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
package org.betaconceptframework.astroboa.engine.model.jaxb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.SpaceImpl;
import org.betaconceptframework.astroboa.model.impl.TaxonomyImpl;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "repositoryUsers",
    "taxonomies",
    "contentObjectContainer",
    "organizationSpace"
})
@XmlRootElement(name = "repository")
public class Repository {

	@XmlElementWrapper(name="repositoryUsers")
    @XmlElement(
    		type=RepositoryUserImpl.class,
    		name="repositoryUser")
    protected List<RepositoryUser> repositoryUsers;
    
	@XmlElementWrapper(name="taxonomies")
    @XmlElement(
    		type=TaxonomyImpl.class, 
    		name="taxonomy")
    protected List<Taxonomy> taxonomies;
    
	/*
	 * Alternative would be
	 * @XmlElementWrapper(name="contentObjects")
	 * @XmlMixed
	 * @XmlAnyElement 
	 * protected List<Element> contentObjectElementList;
	 * 
	 * But this way an exception is thrown as each element representing a
	 * content object has a URI and local name which are not
	 * (and could not be ) registered to JAXB Unmarshaller.
	 */
	@XmlElement(name="contentObjects")
    protected ContentObjectContainer contentObjectContainer;

    @XmlElement(name="organizationSpace",
    		type=SpaceImpl.class)
    protected Space organizationSpace;

    @XmlAttribute
    protected String id;
    
    @XmlAttribute
    protected Calendar created;
    
    public List<RepositoryUser> getRepositoryUsers() {
        if (repositoryUsers == null) {
            repositoryUsers = new ArrayList<RepositoryUser>();
        }
        return this.repositoryUsers;
    }

    public List<Taxonomy> getTaxonomies() {
        if (taxonomies == null) {
            taxonomies = new ArrayList<Taxonomy>();
        }
        return this.taxonomies;
    }

    public Space getOrganizationSpace() {
        return organizationSpace;
    }

    public void setOrganizationSpace(Space value) {
        this.organizationSpace = value;
    }

    public ContentObjectContainer getContentObjectContainer() {
        return contentObjectContainer;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

	public Calendar getCreated() {
		return created;
	}

	public void setCreated(Calendar created) {
		this.created = created;
	}
    
}
