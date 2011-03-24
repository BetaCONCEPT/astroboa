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

package org.betaconceptframework.astroboa.model.impl.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.betaconceptframework.astroboa.util.ResourceApiURLUtils;

/**
 * CmsDefinition implementation class.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class CmsDefinitionImpl implements CmsDefinition, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5160790560930844024L;
	
	private final String name;

	private QName qualifiedName;
	
	private Map<ResourceRepresentationType<?>, String> urlsPerOutput = new HashMap<ResourceRepresentationType<?>, String>();
	
	public CmsDefinitionImpl(QName qualifiedName) {
		
		if (qualifiedName == null || StringUtils.isBlank(qualifiedName.getLocalPart())){
			throw new CmsException("Invalid definition. No name provided");
		}
		
		this.name = qualifiedName.getLocalPart();
		
		this.qualifiedName = qualifiedName;
		
	}

	public String getName() {
		return name;
	}
	
	public QName getQualifiedName() {
		return qualifiedName;
	}

	public String toString(){
		return (qualifiedName == null? name : qualifiedName.toString());
	}
	
	public String json(boolean prettyPrint) {
		
		generateUrls();
		
		return CmsEntitySerialization.Context.toJson(this,prettyPrint);
	}
	
	public String xml(boolean prettyPrint) {
		
		generateUrls();
		
		return CmsEntitySerialization.Context.toXml(this,prettyPrint);
	}
	
	public String xmlSchema() {
		
		return CmsEntitySerialization.Context.toXsd(this);
	}
	
	public String url(ResourceRepresentationType<?>  resourceRepresentationType){
		
		generateUrls();
		
		return urlsPerOutput.get(resourceRepresentationType);
	}
	
	private void generateUrls(){
		if (urlsPerOutput.isEmpty()){
			urlsPerOutput.put(ResourceRepresentationType.XML, ResourceApiURLUtils.generateUrlForEntity(this, ResourceRepresentationType.XML, false));
			urlsPerOutput.put(ResourceRepresentationType.JSON, ResourceApiURLUtils.generateUrlForEntity(this, ResourceRepresentationType.JSON, false));
			urlsPerOutput.put(ResourceRepresentationType.XSD, ResourceApiURLUtils.generateUrlForEntity(this, ResourceRepresentationType.XSD, false));
			
		}
	}

}
