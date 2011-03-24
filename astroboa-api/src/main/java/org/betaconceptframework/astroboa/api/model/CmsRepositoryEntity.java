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

package org.betaconceptframework.astroboa.api.model;

import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;


/**
 * Represents an entity in Astroboa built in content repository model.
 * 
 * <p>
 * The built-in repository model is based on Java Content Repository standard
 * and thus the core entities are internally defined by means of the low-level
 * (less abstract) entities provided by the JSR-170 specification. Therefore the
 * model adheres to widely accepted standards for content management while at
 * the same time allows to define content at a higher level of abstraction. The
 * provided content modeling entities along with Astroboa API for controlling
 * them, hide the low level details of JCR API for the code developer, and
 * provide the content developer/modeler with a set of high level content
 * definition entities to model her organization content.
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface CmsRepositoryEntity {

	/**
	 * Sets the unique identifier of this entity.
	 * 
	 * @param id
	 *            A {@link java.lang.String} representing a unique entity
	 *            identifier.
	 */
	void setId(String id);

	/**
	 * Returns the identifier of the entity.
	 * 
	 * @return A {@link java.lang.String} representing entity's identifier.
	 */
	String getId();
	
	/**
	 * Check whether this entity is a system built in entity.
	 * 
	 * A system built in entity is a special case of a cms repository entity in that it does not
	 * participates in queries unless explicitly specified in {@link CmsCriteria}.
	 * 
	 * @return <code>true</code> if entity is system built in entity, <code>false</code> otherwise.
	 */
	boolean isSystemBuiltinEntity();
	
	/**
	 * Specify whether this entity should be a system built in entity or not
	 * @param systemBuiltinEntity <code>true</code> to set this entity as system built in, <code>false</code> otherwise.
	 */
	void setSystemBuiltinEntity(boolean systemBuiltinEntity);
	
	/**
	 * Provides an xml representation of specified <code>cmsRepositoryEntity</code>
	 * following entity's XML schema.
	 * 
	 * <p>
	 * Note that in case entity is a {@link ContentObject} then all its properties
	 * which have values will be exported and this will cause all its properties 
	 * to be lazy loaded in {@link ContentObject} the instance.
	 * </p>
	 * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding identation and linefeeds in order to make output more human readable, <code>false<code> otherwise
	 * @return XML instance for this <code>cmsRepositoryEntity</code>.
	 */
	String xml(boolean prettyPrint);
	
	
	/**
	 * Provides a JSON representation of specified <code>cmsRepositoryEntity</code>
	 * 
     * The natural JSON notation
     * <p>Example JSON expression:<pre>
     * {"columns":[{"id":"userid","label":"UserID"},{"id":"name","label":"User Name"}],"rows":[{"userid":1621,"name":"Grotefend"}]}
     * </pre>
     * </p>
	 * 
	 * <p>
	 * Note that in case entity is a {@link ContentObject} then all its properties
	 * which have values will be exported and this will cause all its properties 
	 * to be lazy loaded in {@link ContentObject} the instance.
	 * </p>
	 * 
	 * @param prettyPrint <code>true</code> to enable pretty printer functionality such as 
	 * adding indentation and line feeds in order to make output more human readable, <code>false<code> otherwise
	 * 
	 * @return JSON representation for this <code>cmsRepositoryEntity</code>.
	 */
	String json(boolean prettyPrint);

	/**
	 * A RESTful API URL, compliant with Astroboa Resource API, responsible to retrieve
	 * this entity in the specified representation type.
	 * 
	 * @param resourceRepresentationType XML (default) or JSON representation
	 * @param relative <code>true</code> to return URL relative to server, <code>false</code> otherwise. 
	 * Default is <code>false</code>.
	 *  
	 * @return A RESTful API URL responsible to retrieve this entity in the specified representation type.
	 */
	String getResourceApiURL(ResourceRepresentationType<?>  resourceRepresentationType, boolean relative);
	
}
