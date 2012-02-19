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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.betaconceptframework.astroboa.util.ResourceApiURLUtils;
import org.betaconceptframework.astroboa.util.UrlProperties;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
	propOrder = {
		    "cmsIdentifier",
		    "url"
		}
)
public abstract class CmsRepositoryEntityImpl  implements CmsRepositoryEntity , Serializable{

	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = 8665049770425102903L;
	
	@XmlAttribute
	private String cmsIdentifier;
	
	@XmlAttribute
	protected String url;

	//Authentication token is very important as it is used
	//to locate appropriate LazyLoader from current execution thread
	@XmlTransient
	protected String authenticationToken;

	public String getId() {
		return cmsIdentifier;
	}
	public void setId(String cmsIdentifier) {
		this.cmsIdentifier = cmsIdentifier;
	}
	
	public void setAuthenticationToken(String authenticationToken){
		this.authenticationToken = authenticationToken;
	}

	@Override
	public String json(boolean prettyPrint) {
		
		generateUrl(ResourceRepresentationType.JSON);
		
		return CmsEntitySerialization.Context.toJson(this,false,prettyPrint);
	}
	
	private void generateUrl(ResourceRepresentationType<?>  resourceRepresentationType) {

		if (resourceRepresentationType != null && ResourceRepresentationType.JSON.equals(resourceRepresentationType)){
			url = getResourceApiURL(ResourceRepresentationType.JSON, false, false);
		}
		else if (resourceRepresentationType != null && ResourceRepresentationType.XML.equals(resourceRepresentationType)){
			url = getResourceApiURL(ResourceRepresentationType.XML, false, false);
		}
	}
	
	@Override
	public String xml(boolean prettyPrint) {
		
		generateUrl(ResourceRepresentationType.XML);
		
		return CmsEntitySerialization.Context.toXml(this,false,prettyPrint);
	}
	
	@Override
	public String getResourceApiURL(ResourceRepresentationType<?>  resourceRepresentationType, boolean relative, boolean friendlyUrl) {

		UrlProperties urlProperties = new UrlProperties();
		urlProperties.setFriendly(friendlyUrl);
		urlProperties.setRelative(relative);
		urlProperties.setResourceRepresentationType(resourceRepresentationType);

		return ResourceApiURLUtils.generateUrlForEntity(this, urlProperties);
	}
	
	@Deprecated
	public String toXml() {
		return xml(false);
	}

	
}
