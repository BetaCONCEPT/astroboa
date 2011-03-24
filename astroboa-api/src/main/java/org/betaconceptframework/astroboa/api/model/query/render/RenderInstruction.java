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

package org.betaconceptframework.astroboa.api.model.query.render;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;

/**
 * RenderInstruction enumeration.
 * 
 * Contains all instructions one could provide to a {@link CmsCriteria} 
 * for proper rendering of {@link CmsRepositoryEntity entities}
 * participating in query results.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum RenderInstruction {  
	
	/**
	 * Render localized label for a specified locale. 
	 * Accepted values is any {@link String} 
	 * representing locale.
	 */
	RENDER_LOCALIZED_LABEL_FOR_LOCALE,
	
	/**
	 * Indicates which version of {@link ContentObject}
	 * will be rendered. Accepted values are any {@link String}.  
	 */
	CONTENT_OBJECT_VERSION,
	
	/**
	 * Render parent. Usually used when rendering 
	 * {@link Topic topics} or {@link Space spaces}.
	 * 
	 *  <p>
	 * Accepted values are <code>true</code>, <code>false</code>.
	 * </p>
	 */
	RENDER_PARENT,
	
	
	/**
	 * Instructs Astroboa to disable lazy loading of properties
	 * of a content object. That means that all properties of a content object
	 * must have already been loaded.
	 */
	DISABLE_LAZY_LOADING_OF_CONTENT_OBJECT_PROPERTIES,
	
	/**
	 * When serializing a collection of {@link ContentObject}s
	 * in XML format, it uses name <code>contentObject</code>
	 * as element name instead of the content type name defined in XSD file
	 */
	SERIALIZE_OBJECTS_USING_THE_SAME_NAME_FOR_EACH_COLLECTION_ITEM,
	
	/**
	 * Pretty Print resources if their representation is XML or JSON
	 */
	PRETTY_PRINT;
	

	
}
