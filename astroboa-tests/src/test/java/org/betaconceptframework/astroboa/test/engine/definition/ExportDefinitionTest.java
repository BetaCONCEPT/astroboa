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
package org.betaconceptframework.astroboa.test.engine.definition;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ExportDefinitionTest extends AbstractRepositoryTest{

	
	/*
	 * This test does nothing more than exporting definitions 
	 * to XML and JSON format. If an error occurs , an exception is thrown
	 */
	@Test
	public void testExportDefinitionToXMLorJSON(){
		
		List<ComplexCmsPropertyDefinition> complexTypes = definitionService.getAvailableAspectDefinitionsSortedByLocale("en");
		
		for (ComplexCmsPropertyDefinition complexCmsPropertyDefinition : complexTypes){
			complexCmsPropertyDefinition.xml(false);
			complexCmsPropertyDefinition.xml(true);
			complexCmsPropertyDefinition.json(false);
			complexCmsPropertyDefinition.json(true);
			complexCmsPropertyDefinition.xmlSchema();
		}
		
		List<String> contentTypes = definitionService.getContentObjectTypes();
		
		for (String contentType : contentTypes){
			
			CmsDefinition typeDefinition = definitionService.getCmsDefinition(contentType, ResourceRepresentationType.DEFINITION_INSTANCE);
			
			Assert.assertTrue(typeDefinition instanceof ContentObjectTypeDefinition, "Invalid definition instance returned. Expected content type definition");
			
			typeDefinition.xml(false);
			typeDefinition.xml(true);
			typeDefinition.json(false);
			typeDefinition.json(true);
			typeDefinition.xmlSchema();
			
			String typeDefinitionAsXML = definitionService.getCmsDefinition(contentType, ResourceRepresentationType.XML);
			
			Assert.assertTrue(typeDefinitionAsXML instanceof String, "Invalid definition instance returned. Expected String"+ typeDefinitionAsXML);
			

			String typeDefinitionAsJSON = definitionService.getCmsDefinition(contentType, ResourceRepresentationType.JSON);
			
			Assert.assertTrue(typeDefinitionAsJSON instanceof String, "Invalid definition instance returned. Expected String"+ typeDefinitionAsJSON);

			String typeDefinitionAsXSD = definitionService.getCmsDefinition(contentType, ResourceRepresentationType.XSD);
			
			Assert.assertTrue(typeDefinitionAsXSD instanceof String, "Invalid definition instance returned. Expected String"+ typeDefinitionAsXSD);

		}
		
	}
}
