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

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;



/**
* @author Gregory Chomatas (gchomatas@betaconcept.com)
* @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
* 
*/
public interface LazyCmsProperty {

	void setPropertyContainerNodeUUID(String propertyContainerNodeUUID);
	
	String getPropertyContainerUUID();

	void setRenderProperties(RenderProperties renderProperties);

	void setContentObjectNodeUUID(String contentObjectNodeUUID);
	
	String getContentObjectNodeUUID();
	
	RenderProperties getRenderProperties();
	
	void addNewProperty(String propertyName,
			CmsPropertyDefinition propertyDefinition, CmsProperty newProperty) throws Exception ;
	
}