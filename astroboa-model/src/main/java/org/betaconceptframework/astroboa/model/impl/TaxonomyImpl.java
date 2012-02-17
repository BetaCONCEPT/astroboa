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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.jaxb.adapter.NumberOfChildrenAdapter;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlRootElement(name="taxonomy")   
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxonomyType", propOrder = {
		"name",
		"numberOfRootTopics", //we must define here the name of the property and not the name with which this property will appear in XML
		"rootTopics"    
})
public class TaxonomyImpl extends LocalizableEntityImpl implements Taxonomy, Serializable{
	
	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = 1077809310122589166L;

	@XmlAttribute
	private String name;
	
	@XmlAttribute(name="numberOfChildren")
	@XmlJavaTypeAdapter(type=int.class, value=NumberOfChildrenAdapter.class)
	private int numberOfRootTopics=-1;

	@XmlElementWrapper
	@XmlElement(
			name="topic",
			type=TopicImpl.class
			)
	private List<Topic> rootTopics;
	
	public TaxonomyImpl(){
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Topic> getRootTopics() {
		if (!isRootTopicsLoaded()){
			if (StringUtils.isNotBlank(getId() )) {
				getLazyLoader().lazyLoadTaxonomyRootTopics(this, authenticationToken);
			}
		}
		
		return rootTopics;
	}

	public void setRootTopics(List<Topic> rootTopics) {
		this.rootTopics = rootTopics;
		
		if (rootTopics == null){
			numberOfRootTopics = 0;
		}
		else{
			numberOfRootTopics = rootTopics.size();
		}
	}

	public void addRootTopic(Topic rootTopic) {
       
		if (rootTopic == null){
			return ;
		}
		
		if (rootTopics == null){
			rootTopics = new ArrayList<Topic>();
		}
		
		rootTopics.add(rootTopic);
		rootTopic.setTaxonomy(this);
		rootTopic.setParent(null);
		
		numberOfRootTopics = rootTopics.size();
		
	}

	public int getNumberOfRootTopics() {
		
		if (numberOfRootTopics < 0 && rootTopics != null){
			numberOfRootTopics = rootTopics.size();
		}
		
		if (numberOfRootTopics < 0){
			return 0;
		}
		
		return numberOfRootTopics;
	}

	public void setNumberOfRootTopics(int numberOfRootTopics) {
		this.numberOfRootTopics = numberOfRootTopics;
	}


	public boolean isRootTopicsLoaded() {
		return rootTopics != null;
	}

	private LazyLoader getLazyLoader() {
		return AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaxonomyImpl [");
		if (name != null)
			builder.append("name=").append(name);
		if (getId() != null)
			builder.append(", ").append("id=").append(getId());
		builder.append("]");
		return builder.toString();
	}
	
}
