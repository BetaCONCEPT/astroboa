/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited.
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
package org.betaconceptframework.astroboa.commons.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.model.impl.definition.SimpleCmsPropertyDefinitionImpl;
import org.slf4j.LoggerFactory;

/**
 * Used to distinguish SimpleCmsPropertyDefinitions which represent an Xml attribute from all
 * other definitions
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PropertyRepresentingXmlAttributeComparator implements Comparator<CmsPropertyDefinition>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7526206583223438228L;


	public int compare(CmsPropertyDefinition definition0, CmsPropertyDefinition definition1) {
		
		if (definition0 == null)
			return -1;
		if (definition1 == null)
			return 1;
		
		try{
			
			Boolean definition0RepresentsAnXmlAttribute = definitionRepresentsAnXmlAttribute(definition0);
			Boolean definition1RepresentsAnXmlAttribute = definitionRepresentsAnXmlAttribute(definition1);
			
			//Definition which represent an xml attribute
			//has higher order than the others
			return -1 * definition0RepresentsAnXmlAttribute.compareTo(definition1RepresentsAnXmlAttribute);


		}
		catch (Throwable e){
			LoggerFactory.getLogger(getClass()).warn("",e);
			return -1;
		}
	}

	
	private Boolean definitionRepresentsAnXmlAttribute(CmsPropertyDefinition definition) {

		return definition instanceof SimpleCmsPropertyDefinition && 
				((SimpleCmsPropertyDefinitionImpl)definition).isRepresentsAnXmlAttribute();
	}

}