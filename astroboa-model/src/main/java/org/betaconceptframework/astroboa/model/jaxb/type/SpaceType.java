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
package org.betaconceptframework.astroboa.model.jaxb.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.model.jaxb.adapter.RepositoryUserAdapter;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spaceType",
	propOrder = {
		"name",
		"numberOfChildren",
		"owner"
})
public class SpaceType extends AstroboaEntityType {


	@XmlTransient
	private static final long serialVersionUID = -7167974317857708532L;

	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private int numberOfChildren;

	@XmlElement
	@XmlJavaTypeAdapter(value=RepositoryUserAdapter.class)
	private RepositoryUser owner;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RepositoryUser getOwner() {
		return owner;
	}

	public void setOwner(RepositoryUser owner) {
		this.owner = owner;
	}

	public int getNumberOfChildren() {
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}
	
}
