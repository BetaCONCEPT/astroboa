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
package org.betaconceptframework.astroboa.test.engine.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.visitor.DefinitionVisitor.VisitType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionServiceTest extends AbstractRepositoryTest {

	@Test
	public void testValueTypeReturnedFromService() throws IOException{
		
	
		List<CmsRepository> repositories = repositoryService.getAvailableCmsRepositories();
		
		DefinitionVisitor definitionVisitor = new DefinitionVisitor(definitionService);
		definitionVisitor.setVisitType(VisitType.Full);
		
		
		for (CmsRepository repository : repositories){

			String repositoryId = repository.getId();
			
			loginToRepositoryAsAnonymous(repositoryId);
			
			List<String> contentTypes = definitionService.getContentObjectTypes();
			
			for (String contentType : contentTypes){
				ContentObjectTypeDefinition typeDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(contentType, ResourceRepresentationType.DEFINITION_INSTANCE, prettyPrint);
				typeDefinition.accept(definitionVisitor);
			}
		}
		
		loginToTestRepositoryAsSystem();
		
		ContentObjectTypeDefinition typeDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE, ResourceRepresentationType.DEFINITION_INSTANCE, prettyPrint);

		String propertyPath = "allPropertyTypeContainer.allPropertyTypeContainer.allPropertyTypeContainer.allPropertyTypeContainer.comment.comment";
		
		String[] parts = propertyPath.split("\\.");
		
		CmsPropertyDefinition cmsDefinition = null;
		
		for (String part : parts){
			
			if (cmsDefinition == null){
				cmsDefinition = typeDefinition.getCmsPropertyDefinition(part);
			}
			else{
				((ComplexCmsPropertyDefinitionImpl)cmsDefinition).checkIfRecursiveAndCloneParentChildDefinitions();
				cmsDefinition = ((ComplexCmsPropertyDefinition)cmsDefinition).getChildCmsPropertyDefinition(part);
			}
			
		}
		
		ValueType valueType = definitionService.getTypeForProperty(TEST_CONTENT_TYPE, cmsDefinition.getPath());
		
		Assert.assertEquals(valueType, cmsDefinition.getValueType());
		
		ValueType newValueType = definitionService.getTypeForProperty(null, cmsDefinition.getPath());
		
		Assert.assertEquals(newValueType, cmsDefinition.getValueType());

		
	}
	
	@Test
	public void testDefinitionHierarchyDepthInRecursiveDefinitions() throws IOException{
		
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty("organizationObject", null, "departments.department");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(TEST_CONTENT_TYPE, null, "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(TEST_CONTENT_TYPE, "commentSingle", "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(TEST_CONTENT_TYPE, "allPropertyTypeContainer", "comment");

		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(EXTENDED_TEST_CONTENT_TYPE, null, "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(EXTENDED_TEST_CONTENT_TYPE, "commentSingle", "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer", "comment");

		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(DIRECT_EXTENDED_TEST_CONTENT_TYPE, null, "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(DIRECT_EXTENDED_TEST_CONTENT_TYPE, "commentSingle", "comment");
		assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(DIRECT_EXTENDED_TEST_CONTENT_TYPE, "allPropertyTypeContainer", "comment");

	}
	
	private void assertDefinitionHierarchyDepthForTypeAndRecursiveProperty(String contentType, String pathToPropertyWhichIsTheParentOfTheRecursiveProperty, String recursiveProperty) {
		
		int depthForType = definitionService.getDefinitionHierarchyDepthPerContentType().get(contentType);
		
		String fullPath = contentType+"."+ (pathToPropertyWhichIsTheParentOfTheRecursiveProperty==null? recursiveProperty : pathToPropertyWhichIsTheParentOfTheRecursiveProperty+"."+recursiveProperty);
		
		ComplexCmsPropertyDefinition recursiveDefinition = (ComplexCmsPropertyDefinition) definitionService.getCmsDefinition(fullPath, ResourceRepresentationType.DEFINITION_INSTANCE, prettyPrint);
		
		//Now check again depth. 
		String[]  pathParts = StringUtils.split(recursiveDefinition.getFullPath(), ".");
		
		//Path parts will reveal the depth until the recursive definition.
		//And to that we add the depth from the recursive definition down to its children
		//Depth is zero based
		int newCalculatedDepth = (pathParts.length-1) + recursiveDefinition.getDepth(); 
		
		//received the new depth for the whole type
		int newDepthForType = definitionService.getDefinitionHierarchyDepthPerContentType().get(contentType);
		
		
		//New calculated depth is greater than the old type depth
		//Therefore the new type depth should be the same with the new calculated depth
		if (newCalculatedDepth>depthForType){
			Assert.assertEquals(newCalculatedDepth,newDepthForType, "Definition hierarchy depth has not been increased for type "+ contentType+ " and property "+
					recursiveDefinition.getFullPath());
		}
		else{
			//Depth should not have changed
			Assert.assertEquals(newDepthForType, depthForType, "Definition hierarchy depth has not been increased for type "+ contentType+ " and property "+
					recursiveDefinition.getFullPath());
		}
		
	}

	@Test
	public void testDefinitionHierarchyDepth() throws IOException{

		List<CmsRepository> repositories = repositoryService.getAvailableCmsRepositories();
		
		for (CmsRepository repository : repositories){

			String repositoryId = repository.getId();
			
			loginToRepositoryAsAnonymous(repositoryId);
			
			Map<String, Integer> definitionHierarchyDepthPerContentType = definitionService.getDefinitionHierarchyDepthPerContentType();
			
			for (Entry<String,Integer> contentTypeDepthEntry : definitionHierarchyDepthPerContentType.entrySet()){
				
				String contentType = contentTypeDepthEntry.getKey();
				Integer depth = contentTypeDepthEntry.getValue();
				
				Assert.assertEquals(depth, definitionService.getDefinitionHierarchyDepthForContentType(contentType), " Different Definition hierarchy depth for content type "+ contentType +" returned from DefinitionService methods");
			}
		}

		loginToTestRepositoryAsSystem();
		
	}	

	@Test
	public void testMultivalueAndTopicPropertyPaths(){
		
		
		for (String multivalueProperty : definitionService.getMultivalueProperties()){
			logger.debug("Multivalue property {}", multivalueProperty);
		}
		
		Map<String, List<String>> topicPathsPerTaxonomy = definitionService.getTopicPropertyPathsPerTaxonomies();
		
		
		for (Entry<String, List<String>> topicEntry : topicPathsPerTaxonomy.entrySet()){
			StringBuilder message =  new StringBuilder();

			message.append("\nTopic property paths for Taxonomy : "+ topicEntry.getKey());
			
			for (String topicPropertyPath : topicEntry.getValue()){
				message.append("\n\t  : "+ topicPropertyPath);
			}
			
			logger.debug(message.toString());
		}

	}	

	
	public class DefinitionVisitor extends AbstractCmsPropertyDefinitionVisitor{

		
		private DefinitionService definitionService;
		private ContentObjectTypeDefinition contentObjectTypeDefinition;

		public DefinitionVisitor(DefinitionService definitionService) {
			super();
			this.definitionService = definitionService;
		}

		@Override
		public void visit(
				ContentObjectTypeDefinition contentObjectTypeDefinition) {
			Assert.assertTrue(definitionService.hasContentObjectTypeDefinition(contentObjectTypeDefinition.getName()), "Definition for content type "+ contentObjectTypeDefinition.getName() + " does not exist.");
			
			this.contentObjectTypeDefinition = contentObjectTypeDefinition;
		}

		@Override
		public void visitComplexPropertyDefinition(
				ComplexCmsPropertyDefinition complexPropertyDefinition) {

			Assert.assertTrue(contentObjectTypeDefinition.hasCmsPropertyDefinition(complexPropertyDefinition.getPath()));
			Assert.assertNotNull(contentObjectTypeDefinition.getCmsPropertyDefinition(complexPropertyDefinition.getPath()));
			
			String contentType = new PropertyPath(complexPropertyDefinition.getFullPath()).getPropertyName();
			
			Assert.assertEquals(definitionService.getTypeForProperty(null, complexPropertyDefinition.getFullPath()), ValueType.Complex,"Definition Service returned invalid value type for property "+ complexPropertyDefinition.getFullPath()+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());
			Assert.assertEquals(definitionService.getTypeForProperty(contentType, complexPropertyDefinition.getPath()),ValueType.Complex,  "Definition Service returned invalid value type for  property "+ complexPropertyDefinition.getPath()+ " in content type "+contentType+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());

			Assert.assertEquals(definitionService.getTypeForProperty(null, complexPropertyDefinition.getFullPath()), complexPropertyDefinition.getValueType(), "Definition Service returned invalid value type for property "+ complexPropertyDefinition.getFullPath()+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());
			Assert.assertEquals( definitionService.getTypeForProperty(contentType, complexPropertyDefinition.getPath()), complexPropertyDefinition.getValueType(), "Definition Service returned invalid value type for  property "+ complexPropertyDefinition.getPath()+ " in content type "+contentType+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());

		}

		@Override
		public <T> void visitSimplePropertyDefinition(
				SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {

			Assert.assertTrue(contentObjectTypeDefinition.hasCmsPropertyDefinition(simplePropertyDefinition.getPath()));
			Assert.assertNotNull(contentObjectTypeDefinition.getCmsPropertyDefinition(simplePropertyDefinition.getPath()));
			
			String contentType = new PropertyPath(simplePropertyDefinition.getFullPath()).getPropertyName();
			
			Assert.assertEquals(definitionService.getTypeForProperty(null, simplePropertyDefinition.getFullPath()), simplePropertyDefinition.getValueType(), "Definition Service returned invalid value type for property "+ simplePropertyDefinition.getFullPath()+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());
			Assert.assertEquals(definitionService.getTypeForProperty(contentType, simplePropertyDefinition.getPath()), simplePropertyDefinition.getValueType(), "Definition Service returned invalid value type for property "+ simplePropertyDefinition.getPath()+ " in content type "+contentType+ " Repository "+AstroboaClientContextHolder.getActiveRepositoryId());

		}

	}
}
