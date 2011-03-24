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
package org.betaconceptframework.astroboa.test.model;


import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ContentObjectProperty;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsRootPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.engine.CmsPropertyPath;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyExtractor;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectTest extends AbstractRepositoryTest {

	
	@Test 
	public void testPropertyExtractor() throws Exception{
		
		ContentObject object = createContentObjectAndPopulateAllProperties(getSystemUser(), "testPropertyExtractor", false);
		
		object = contentService.save(object, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(object);
		
		object = contentService.getContentObject(object.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.FULL, CacheRegion.NONE, null, false);
		
		assertPropertyExtractor(object.getComplexCmsRootProperty(), object);
	}
	
	private void assertPropertyExtractor(ComplexCmsProperty complexCmsProperty, ContentObject contentObject) throws Exception {
		
		Map<String, List<CmsProperty<?,?>>> propertyMap = complexCmsProperty.getChildProperties();
		
		for (List<CmsProperty<?,?>> childProperties : propertyMap.values()){
			
			for (CmsProperty<?,?> childProperty : childProperties){
				
				String childPermanentPath = childProperty.getPermanentPath();

				PropertyExtractor propertyExtractor = new PropertyExtractor(contentObject, childPermanentPath);
				
				Assert.assertSame(propertyExtractor.getProperty(), childProperty, "Property extractor did not the same property for path "+ childPermanentPath);
				
				switch (childProperty.getValueType()) {
				case Complex:
					assertPropertyExtractor((ComplexCmsProperty)childProperty, contentObject);
					break;
				
				default:
					
					if (childProperty.getPropertyDefinition().isMultiple()){
						
						List values = ((SimpleCmsProperty)childProperty).getSimpleTypeValues();
						
						for (int i=0; i< values.size(); i++){
							
							
							String permanentPathWithValueIndexOrIdentifier = childPermanentPath+CmsConstants.LEFT_BRACKET+i+CmsConstants.RIGHT_BRACKET;
							
							PropertyExtractor childPropertyExtractor = new PropertyExtractor(contentObject, permanentPathWithValueIndexOrIdentifier);
							Assert.assertSame(propertyExtractor.getProperty(), childProperty, "Property extractor did not the same property for path " + permanentPathWithValueIndexOrIdentifier);
							Assert.assertEquals(i, childPropertyExtractor.getIndexOfTheValueOfTheProperty(), "Property Extractor returned invalid value index.Permanent Path" + permanentPathWithValueIndexOrIdentifier);
							Assert.assertNull(childPropertyExtractor.getIdentifierOfTheValueOfTheProperty(), "Property Extractor returned invalid value identifier. Expectd null. Permanent Path "+permanentPathWithValueIndexOrIdentifier);
							
							if (childProperty.getValueType() == ValueType.Binary){
								BinaryChannel binaryChannel = (BinaryChannel) values.get(i);
								
								permanentPathWithValueIndexOrIdentifier = childPermanentPath+CmsConstants.LEFT_BRACKET+binaryChannel.getId()+CmsConstants.RIGHT_BRACKET;
								
								childPropertyExtractor = new PropertyExtractor(contentObject, permanentPathWithValueIndexOrIdentifier);
								Assert.assertSame(propertyExtractor.getProperty(), childProperty, "Property extractor did not the same property for path "+ permanentPathWithValueIndexOrIdentifier);
								Assert.assertEquals(0, childPropertyExtractor.getIndexOfTheValueOfTheProperty(), "Property Extractor returned invalid value index. Expectd 0. Permanent Path "+permanentPathWithValueIndexOrIdentifier);
								Assert.assertEquals(binaryChannel.getId(), childPropertyExtractor.getIdentifierOfTheValueOfTheProperty(), "Property Extractor returned invalid value identifier. Permanent Path "+permanentPathWithValueIndexOrIdentifier);
							}

						}
					}
					
					break;
				}
				
			}
		}
		
	}

	@Test
	public void testPermanentPath(){
		
		ContentObject contentObject = createContentObject(getSystemUser(), "testPermanentPath", false);
		
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultiple.simpleString")).setSimpleTypeValue("allPropertyTypeContainerMultipleSimpleString");
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultiple[1].simpleString")).setSimpleTypeValue("allPropertyTypeContainerMultipleSecondSimpleString");
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultiple[2].simpleString")).setSimpleTypeValue("allPropertyTypeContainerMultipleThirdSimpleString");

		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple.body")).setSimpleTypeValue("allPropertyTypeContainerCommentMultipleString");
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple[1].body")).setSimpleTypeValue("allPropertyTypeContainerSecondCommentMultipleString");
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple[2].body")).setSimpleTypeValue("allPropertyTypeContainerThirdCommentMultipleString");

		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainerMultiple", "simpleString", 3);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple", "body", 3);

		contentObject.getComplexCmsRootProperty().swapChildPropertyValues("allPropertyTypeContainerMultiple", 0, 2);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainerMultiple", "simpleString", 3);

		contentObject.getComplexCmsRootProperty().swapChildPropertyValues("allPropertyTypeContainerMultiple", 1, 2);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainerMultiple", "simpleString", 3);

		contentObject.getComplexCmsRootProperty().swapChildPropertyValues("allPropertyTypeContainerMultiple", 0, 1);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainerMultiple", "simpleString", 3);

		contentObject.getComplexCmsRootProperty().changePositionOfChildPropertyValue("allPropertyTypeContainerMultiple", 0, 1);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple", "body", 3);

		contentObject.getComplexCmsRootProperty().changePositionOfChildPropertyValue("allPropertyTypeContainerMultiple", 0, 2);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple", "body", 3);

		contentObject.getComplexCmsRootProperty().changePositionOfChildPropertyValue("allPropertyTypeContainerMultiple", 1, 2);
		assertVariousPathsForProperty(contentObject, "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple", "body", 3);

	}
	
	private void assertVariousPathsForProperty(ContentObject contentObject, String parentPropertyPath, String simplePropertyPath, int size){
		
		
		for (int index=0; index<size;index++){
			
			String parentPath = parentPropertyPath+(index>0? "["+index+"]":"");
			
			ComplexCmsProperty parentProperty = (ComplexCmsProperty)contentObject.getCmsProperty(parentPath);
			CmsProperty simpleProperty = parentProperty.getChildProperty(simplePropertyPath);

			String parentPropertyId = parentProperty.getId();

			String expectedPath = parentPath+"."+simplePropertyPath;
			
			Assert.assertEquals(simpleProperty.getPath(), expectedPath, "Invalid outcome of method getPath");
			Assert.assertEquals(parentProperty.getPath()+"."+simpleProperty.getName(), expectedPath, "Invalid outcome of method getPath");
			Assert.assertEquals(simpleProperty.getFullPath(), contentObject.getContentObjectType() + "." + expectedPath, "Invalid outcome of method getFullPath");
			Assert.assertEquals(parentProperty.getFullPath()+"."+simpleProperty.getName(), contentObject.getContentObjectType() + "." + expectedPath, "Invalid outcome of method getFullPath");
			
			Assert.assertEquals(simpleProperty.getPermanentPath(), parentPropertyPath+"["+parentPropertyId+"]."+simplePropertyPath, "Invalid outcome of method getPermanentPath");
			Assert.assertEquals(parentProperty.getPermanentPath()+"."+simpleProperty.getName(), parentPropertyPath+"["+parentPropertyId+"]."+simplePropertyPath, "Invalid outcome of method getPermanentPath");

			Assert.assertEquals(simpleProperty.getFullPermanentPath(), contentObject.getContentObjectType()+"."+parentPropertyPath+"["+parentPropertyId+"]."+simplePropertyPath, "Invalid outcome of method getFullPermanentPath");
			Assert.assertEquals(parentProperty.getFullPermanentPath()+"."+simpleProperty.getName(), contentObject.getContentObjectType()+"."+parentPropertyPath+"["+parentPropertyId+"]."+simplePropertyPath, "Invalid outcome of method getFullPermanentPath");
			
			//Assert.assertSame(contentObject.getCmsProperty(simpleProperty.getPath()), contentObject.getCmsProperty(parentPropertyPath), "Invalid property instance returned for path "+simpleProperty.getPath() + " and permanent path "+
			//		simpleProperty.getPermanentPath());
		}
	}
	
	@Test
	public void testValueExistsForProperty(){

		//Check unsaved content object
		ContentObject contentObject = createContentObject(getSystemUser(), "testValueExistsForProperty", false);

		assertValueExistenceInVairousCases(contentObject);
		
		///Check saved content object
		contentObject = createContentObjectToTestForPropertyPath("testValueExistsForPropertySavedObject", getSystemUser());
		
		assertValueExistenceInVairousCases(contentObject);
		
	}


	private void assertValueExistenceInVairousCases(ContentObject contentObject) {
		assertPropertyHasValue(contentObject, "profile.title");
		assertPropertyHasValue(contentObject, "profile.language");
		
		if (contentObject.getId()==null){
			assertPropertyHasNotValue(contentObject, "accessibility.canBeReadBy[0]");
			assertPropertyHasNotValue(contentObject, "accessibility.canBeUpdatedBy[0]");
		}
		else{
			assertPropertyHasValue(contentObject, "accessibility.canBeReadBy[0]");
			assertPropertyHasValue(contentObject, "accessibility.canBeUpdatedBy[0]");
		}

		assertPropertyHasNotValue(contentObject, "profile.creator");

		assertPropertyHasNotValue(contentObject, "allPropertyTypeContainerMultiple");
		assertPropertyHasNotValue(contentObject, "allPropertyTypeContainerMultiple[1].simpleString");
		
		//ValueType.String
		assertPropertyHasNotValue(contentObject,  "simpleString");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleString");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleString");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.commentMultiple.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.commentMultiple.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleString");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleString");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.comment.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.commentMultiple.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.commentMultiple.body");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.commentMultiple.body");

		//ValueType.Long
		assertPropertyHasNotValue(contentObject,  "simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleLong");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleLong");
		
		//ValueType.Double;
		assertPropertyHasNotValue(contentObject,  "simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleDouble");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleDouble");
		
		//ValueType.Binary;
		assertPropertyHasNotValue(contentObject,  "simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleBinary");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleBinary");
		
		//ValueType.Boolean;
		assertPropertyHasNotValue(contentObject,  "simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleBoolean");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleBoolean");
		
		//ValueType.ContentObject;
		assertPropertyHasNotValue(contentObject,  "simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleContentObject");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleContentObject");
		
		//ValueType.Date;
		assertPropertyHasNotValue(contentObject,  "simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleDate");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleDate");
		
		//ValueType.Topic
		assertPropertyHasNotValue(contentObject,  "simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.simpleTopic");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.simpleTopic");

		//ValueType.Complex
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.comment");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainer.comment");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainer.allPropertyTypeContainerMultiple.comment");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.comment");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainer.comment");
		assertPropertyHasNotValue(contentObject,  "allPropertyTypeContainerMultiple.allPropertyTypeContainerMultiple.comment");

		//ValueType.Complex - Aspects
		assertPropertyHasNotValue(contentObject, "statisticType");
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statisticType was created after cal for value existence");
	}


	private void assertPropertyHasValue(ContentObject contentObject,
			String property) {
		Assert.assertTrue(contentObject.hasValueForProperty(property), "Property "+property + " has values but method hasValueProperty returned false");
	}

	private void assertPropertyHasNotValue(ContentObject contentObject,
			String property) {
		
		Assert.assertFalse(contentObject.hasValueForProperty(property), "Property "+property + " does not have value but method hasValueProperty returned true");

	}

	
	@Test
	public void testLabelElementPath(){
		ContentObject contentObject = createContentObjectToTestForPropertyPath("testLabelPath", getSystemUser());
		ContentObject firstSimpleContentObject = createContentObjectToTestForPropertyPath("testLabelPathSimpleContentObjectFirst", getSystemUser());
		ContentObject secondSimpleContentObject = createContentObjectToTestForPropertyPath("testLabelPathSimpleContentObjectSecond", getSystemUser());
		
		//Set values 
		contentObject.getCmsProperty("allPropertyTypeContainerMultiple");
		contentObject.getCmsProperty("allPropertyTypeContainerMultiple[1]");
		
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultiple.simpleString")).addSimpleTypeValue("simpleStringFirstValue");
		((StringProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultiple[1].simpleString")).addSimpleTypeValue("simpleStringSecondValue");
		
		contentObject.getCmsProperty("allPropertyTypeContainerMultipleForLabelElementPath");
		contentObject.getCmsProperty("allPropertyTypeContainerMultipleForLabelElementPath[1]");
		
		((ContentObjectProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultipleForLabelElementPath.simpleContentObject")).addSimpleTypeValue(firstSimpleContentObject);
		((ContentObjectProperty)contentObject.getCmsProperty("allPropertyTypeContainerMultipleForLabelElementPath[1].simpleContentObject")).addSimpleTypeValue(secondSimpleContentObject);
		
		contentService.save(contentObject, false, true, null);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		assertPropertyLabel(contentObject, "allPropertyTypeContainerMultiple[0]", "simpleStringFirstValue");
		assertPropertyLabel(contentObject, "allPropertyTypeContainerMultiple[1]", "simpleStringSecondValue");
		assertPropertyLabel(contentObject, "allPropertyTypeContainerMultipleForLabelElementPath[0]", "testLabelPathSimpleContentObjectFirst");
		assertPropertyLabel(contentObject, "allPropertyTypeContainerMultipleForLabelElementPath[1]", "testLabelPathSimpleContentObjectSecond");
		
		
	}
	
	
	/**
	 * @param contentObject 
	 * @param string
	 * @param string2
	 */
	private void assertPropertyLabel(ContentObject contentObject, String property, String expectedLabelValue) {
		
		ComplexCmsProperty complexCmsProperty = (ComplexCmsProperty) contentObject.getCmsProperty(property);
		
		Assert.assertEquals(complexCmsProperty.getPropertyLabel("en"), expectedLabelValue, "Invalid property labels for property "+property);
		
	}       


	@Test
	public void testLanguage(){
		ContentObject contentObject = createContentObjectToTestForPropertyPath("testingLanguage", getSystemUser());
		
		ContentObject contentObject2 = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
		
		//Resave it and then check language
		contentObject2 = contentService.save(contentObject2, false, true, null);
		
		Assert.assertEquals(((StringProperty)contentObject2.getCmsProperty("profile.language")).getSimpleTypeValues().size(),1,
				"Expected to find only one language. Found "+ ((StringProperty)contentObject2.getCmsProperty("profile.language")).getSimpleTypeValues());
		
	}
	
	@Test
	public void testAspects(){

		ContentObject contentObject = createContentObjectToTestForPropertyPath("testingAspects", getSystemUser());
		
		//Creating an aspect calling getCmsProeprty
		CmsProperty<?, ?> viewCounter = contentObject.getCmsProperty("statisticType.viewCounter");
		
		Assert.assertNotNull(viewCounter, "Property template for aspect statisticType was not created");
		
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statisticType was not added while property template has been created");
		
		Assert.assertNotNull(viewCounter.getPropertyDefinition(), "Created aspect does not have a definition");
		
		Assert.assertEquals(viewCounter.getPropertyDefinition().getName(), "viewCounter");
		
		Assert.assertEquals(viewCounter.getPropertyDefinition().getParentDefinition().getName(), "statisticType");
		
		//Now try to add aspect using method
		contentObject.getComplexCmsRootProperty().getChildProperty("statisticType");
		
		LongProperty viewCounterSameInstance = (LongProperty) contentObject.getCmsProperty("statisticType.viewCounter");
		
		Assert.assertSame(viewCounter, viewCounterSameInstance, "Method getCmsProperty did not returned same instance.");
		
		viewCounterSameInstance.addSimpleTypeValue((long)1);

		//Save content object and reload it
		contentObject = contentService.save(contentObject, false, true, null);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not added while property template has been created");
		
		//Load aspect using addAspect method
		ComplexCmsProperty<?, ?> statisticTypeProperty = (ComplexCmsProperty<?, ?>) contentObject.getComplexCmsRootProperty().getChildProperty("statisticType");
		
		Assert.assertEquals(statisticTypeProperty.getPropertyDefinition().getName(), "statisticType");
		
		contentObject.getCmsProperty("statisticType.viewCounter");
		
		//remove aspect's child
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().removeChildProperty("statisticType.viewCounter"), "Property 'statisticType.viewCounter' was not removed");
		
		//remove aspect's 
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().removeChildProperty("statisticType"), "Property 'statisticType.viewCounter' was not removed");
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not removed");

		//Save content object and reload it
		contentObject = contentService.save(contentObject, false, true, null);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not removed");

		//Recreate aspect, save object and remove it using removeAspect method
		LongProperty viewCounterProperty = (LongProperty) contentObject.getCmsProperty("statisticType.viewCounter");
		viewCounterProperty.addSimpleTypeValue((long)1);
		
		contentObject = contentService.save(contentObject, false, true, null);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not added");
		Assert.assertNotNull(contentObject.getCmsProperty("statisticType"), "Aspect statsticType was added but property has not been loaded");


		//remove aspect
		Assert.assertTrue(contentObject.removeCmsProperty("statisticType"), "Aspect statsticType was not removed");
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not removed");

		//Save content object and reload it
		contentObject = contentService.save(contentObject, false, true, null);
		
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("statisticType"), "Aspect statsticType was not removed");

		
	}
	
	@Test
	public void testAddNewInstanceOfComplexCmsProperty(){
		
		ContentObject contentObject = createContentObjectToTestForPropertyPath("addNewInstanceOfComplexProperty", getSystemUser());
		
		//Test if property is not complex
		CmsProperty<?, ?> property = null;
		
		try{
			property = contentObject.createNewValueForMulitpleComplexCmsProperty("simpleString");
		}
		catch(CmsException e)
		{
			Assert.assertEquals(e.getMessage(), 
					"Property simpleString is not a multi value complex property.Use method ComplexCmsProperty.getChildProperty() or " +
					"ContentObject.getCmsProperty() instead");
		}
	
		try{
			//	test if property is not multiple
			property = contentObject.createNewValueForMulitpleComplexCmsProperty("allPropertyTypeContainer");
		}
		catch(CmsException e)
		{
			Assert.assertEquals(e.getMessage(), 
				"Property allPropertyTypeContainer is not a multi value complex property.Use method ComplexCmsProperty.getChildProperty() or " +
				"ContentObject.getCmsProperty() instead");
		}
		
		//test if property is the first to add
		property = contentObject.createNewValueForMulitpleComplexCmsProperty("allPropertyTypeContainerMultiple");
		Assert.assertNotNull(property, "New complex property 'allPropertyTypeContainer' was not created.");
		Assert.assertEquals(property.getPath(), "allPropertyTypeContainerMultiple");

		//test that the same instance is returned when executing getCmsProperty and addNewInstanceOfComplexCnsProperty
		property = contentObject.createNewValueForMulitpleComplexCmsProperty("allPropertyTypeContainer.commentMultiple");
		Assert.assertNotNull(property, "New complex property 'allPropertyTypeContainer.commentMultiple' was not created.");
		Assert.assertEquals(property.getPath(), "allPropertyTypeContainer.commentMultiple");
		
		CmsProperty<?, ?> sameProperty = contentObject.getCmsProperty("allPropertyTypeContainer.commentMultiple");
		Assert.assertNotNull(sameProperty, "New complex property 'allPropertyTypeContainer.commentMultiple' was not created.");
		Assert.assertEquals(sameProperty.getPath(), "allPropertyTypeContainer.commentMultiple");
		
		Assert.assertSame(property, sameProperty, "Calling method ContentObject.addNewInstanceofComplexCmsProperty and right after ContentObject.getCmsProperty" +
				" for property 'allPropertyTypeContainer.commentMultiple' did not returned the same instance");
		
		//If methods are executed in reverse order, then different instances should be returned
		sameProperty = contentObject.getCmsProperty("allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple");
		Assert.assertNotNull(sameProperty, "New complex property 'allPropertyTypeContainer.commentMultiple' was not created.");
		Assert.assertEquals(sameProperty.getPath(), "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple");

		property = contentObject.createNewValueForMulitpleComplexCmsProperty("allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple");
		Assert.assertNotNull(property, "New complex property 'allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple' was not created.");
		Assert.assertEquals(property.getPath(), "allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple[1]");

		Assert.assertNotSame(property, sameProperty, "Calling method  ContentObject.getCmsProperty and right after ContentObject.addNewInstanceofComplexCmsProperty" +
		" for property 'allPropertyTypeContainer.allPropertyTypeContainer.commentMultiple' returned the same instance");

		
	}
	  
	@Test
	public void testGetCmsProperty(){
		RepositoryUser systemUser = getSystemUser();
		
		ContentObject contentObject = createContentObjectToTestForPropertyPath("first", systemUser);
		
		assertPropertyPathsWhenGetCmsPropertyIsCalled(contentObject, false, systemUser);
		
		ContentObject contentObjectSecond = createContentObjectToTestForPropertyPath("second", systemUser);
		
		assertPropertyPathsWhenGetCmsPropertyIsCalled(contentObjectSecond, true, systemUser);
		
	}
	
	@Test
	public void testGetCmsPropertyWithIndexNotationPaths() {
		RepositoryUser systemUser = getSystemUser();
		String randomUUIDAsSystemName = UUID.randomUUID().toString();
		ContentObject contentObject = createContentObjectToTestForPropertyPath(randomUUIDAsSystemName, systemUser);
		
		CmsProperty<?,?> firstCommentBody = createCmsProperty(contentObject, "comment[0].body");
		Assert.assertNotNull(firstCommentBody, "comment[0].body should not be null");
		
		CmsProperty<?,?> thirdCommentBody = createCmsProperty(contentObject, "comment[2].body");
		Assert.assertNull(thirdCommentBody, "comment[2].body should be null");
		
		CmsProperty<?,?> secondCommentBody = createCmsProperty(contentObject, "comment[1].body");
		Assert.assertNotNull(secondCommentBody, "comment[1].body should not be null");
		
		StringProperty secondCommentBodyAsString = (StringProperty)secondCommentBody;
		secondCommentBodyAsString.setSimpleTypeValue("some comment");
		StringProperty expectedSecondCommentBodyAsString = (StringProperty)contentObject.getCmsProperty("comment[1].body");
		Assert.assertEquals("some comment", expectedSecondCommentBodyAsString.getSimpleTypeValue());

		CmsProperty<?,?> eleventhCommentBody = createCmsProperty(contentObject, "comment[10].body");
		Assert.assertNull(eleventhCommentBody, "comment[10].body should not be null");

		
		CmsProperty<?,?> firstCommentOfFirstComment = createCmsProperty(contentObject, "comment[0].comment[0].body");
		Assert.assertNotNull(firstCommentOfFirstComment, "comment[0].comment[0].body should not be null");
		
		CmsProperty<?,?> thirdCommentOfFirstComment = createCmsProperty(contentObject, "comment[0].comment[2].body");
		Assert.assertNull(thirdCommentOfFirstComment, "comment[0].comment[2].body should be null");
		
		CmsProperty<?,?> secondCommentOfFirstComment = createCmsProperty(contentObject, "comment[0].comment[1].body");
		Assert.assertNotNull(secondCommentOfFirstComment, "comment[0].comment[1].body should not be null");
		
		CmsProperty<?,?> firstCommentOfSecondComment = createCmsProperty(contentObject, "comment[1].comment[0].body");
		Assert.assertNotNull(firstCommentOfSecondComment, "comment[1].comment[0].body should not be null");
		
		StringProperty firstCommentBodyOfSecondCommentAsString = (StringProperty)firstCommentOfSecondComment;
		firstCommentBodyOfSecondCommentAsString.setSimpleTypeValue("some comment");
		StringProperty expectedFirstCommentBodyOfSecondCommentAsString = (StringProperty)contentObject.getCmsProperty("comment[1].comment[0].body");
		Assert.assertEquals("some comment", expectedFirstCommentBodyOfSecondCommentAsString.getSimpleTypeValue());
		
		CmsProperty<?,?> thirdCommentOfSecondComment = createCmsProperty(contentObject, "comment[1].comment[2].body");
		Assert.assertNull(thirdCommentOfSecondComment, "comment[1].comment[2].body should be null");
		
		CmsProperty<?,?> firstCommentOfFourthComment = createCmsProperty(contentObject, "comment[3].comment[0].body");
		Assert.assertNull(firstCommentOfFourthComment, "comment[3].comment[0].body should be null");
		
		CmsProperty<?,?> secondCommentOfSecondCommentMultipleBody = createCmsProperty(contentObject, "comment[1].comment[1].bodyMultiple");
		Assert.assertNotNull(secondCommentOfSecondCommentMultipleBody, "comment[1].comment[1].multipleBody should not be null");
		
	}

	private void assertPropertyPathsWhenGetCmsPropertyIsCalled(
			ContentObject contentObject, boolean saveContentObject, RepositoryUser systemUser) {
		
		for (CmsPropertyPath cmsPropertyPath : CmsPropertyPath.values()){
			final String propertyPath = cmsPropertyPath.getPeriodDelimitedPath();
			
			CmsProperty cmsProperty = contentObject.getCmsProperty(propertyPath);
			
			if (cmsProperty == null){
				logger.warn("Invalid property path {} : Content Type {}", propertyPath, contentObject.getContentObjectType());
			}
			else{
				if (cmsProperty.getValueType() == ValueType.Complex){
					contentObject = assertPropertyPathForComplexProperty(contentObject, propertyPath,saveContentObject, systemUser);
				}
				else{
					contentObject = assertPropertyPathForSimpleProperty(contentObject, propertyPath,saveContentObject, systemUser);
				}
			}
		}
	}

	private ContentObject createContentObjectToTestForPropertyPath(String systemName, RepositoryUser systemUser) {
		

		systemName = ( systemName != null ? systemName.replaceAll("\\.", "") : "");
		
		ContentObject contentObject = createContentObject(systemUser, systemName, false);

		contentObject = contentService.save(contentObject, false, true, null);
		
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		return contentObject;
	}

	private ContentObject assertPropertyPathForSimpleProperty(
			ContentObject contentObject, String propertyPath, boolean saveContentObject, RepositoryUser systemUser) {
	
		logger.debug("Asserting property path for property "+ propertyPath);

		if (! propertyPath.endsWith("Multiple")){
			assertPropertyPathForSingleValueSimpleProperty(contentObject, propertyPath);

			if (saveContentObject){
				contentService.save(contentObject, false, true, null);

				//Reload contentObject and recheck
				contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
			}
		}
		else{
			assertPropertyPathForMultipleValueSimpleProperty(contentObject, propertyPath);

			if (saveContentObject){
				contentService.save(contentObject, false, true, null);

				//Reload contentObject and recheck
				contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);

				assertPropertyPathForSingleValueSimpleProperty(contentObject, propertyPath);

				assertPropertyPathForMultipleValueSimpleProperty(contentObject, propertyPath);

			}
		}
		
		
		//Now create a new content object to test behavior on a new instance
		ContentObject newContentObject = createContentObjectToTestForPropertyPath(random.nextInt()+contentObject.getSystemName()+propertyPath, systemUser);
		
		if (! propertyPath.endsWith("Multiple")){
			assertPropertyPathForSingleValueSimpleProperty(newContentObject, propertyPath);

			if (saveContentObject){
				contentService.save(newContentObject, false, true, null);

				newContentObject = contentService.getContentObject(newContentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
			}
		}
		else{
			assertPropertyPathForMultipleValueSimpleProperty(newContentObject, propertyPath);

			if (saveContentObject){
				contentService.save(newContentObject, false, true, null);

				//Reload contentObject and recheck
				newContentObject = contentService.getContentObject(newContentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);

				assertPropertyPathForSingleValueSimpleProperty(newContentObject, propertyPath);

				assertPropertyPathForMultipleValueSimpleProperty(newContentObject, propertyPath);

			}
		}
		
		return contentObject;
	}
	
	private ContentObject assertPropertyPathForComplexProperty(
			ContentObject contentObject, String propertyPath, boolean saveContentObject, RepositoryUser systemUser) {
	
		logger.debug("Asserting property path for property "+ propertyPath);
		
		if (! propertyPath.endsWith("Multiple")){
			assertPropertyPathForSingleValueComplexProperty(contentObject, propertyPath);

			if (saveContentObject){
				contentService.save(contentObject, false, true, null);

				contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
				assertPropertyPathForSingleValueComplexProperty(contentObject, propertyPath);
			}
		}
		else{
			assertPropertyPathForMultipleValueComplexProperty(contentObject, propertyPath);

			if (saveContentObject){
				contentService.save(contentObject, false, true, null);

				contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);

				assertPropertyPathForMultipleValueComplexProperty(contentObject, propertyPath);
			}
		}
		
		
		//Now create a new content object to test behavior on a new instance
		ContentObject newContentObject = createContentObjectToTestForPropertyPath(random.nextInt()+contentObject.getSystemName()+propertyPath, systemUser);
		
		if (! propertyPath.endsWith("Multiple")){
			assertPropertyPathForSingleValueComplexProperty(newContentObject, propertyPath);

			if (saveContentObject){
				contentService.save(newContentObject, false, true, null);

				newContentObject = contentService.getContentObject(newContentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
				
				assertPropertyPathForSingleValueComplexProperty(newContentObject, propertyPath);

			}
		}
		else{
			assertPropertyPathForMultipleValueComplexProperty(newContentObject, propertyPath);

			if (saveContentObject){
				contentService.save(newContentObject, false, true, null);

				newContentObject = contentService.getContentObject(newContentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);

				assertPropertyPathForMultipleValueComplexProperty(newContentObject, propertyPath);
			}
		}
		
		return contentObject;

	}
	
	private void assertPropertyPathForMultipleValueComplexProperty(
			ContentObject contentObject, String propertyPath) {
		List<CmsProperty<?, ?>> complexProperties = contentObject.getCmsPropertyList(propertyPath);
		
		Assert.assertNotNull(complexProperties, "Content object of type test did not return complex property list "+propertyPath);
		Assert.assertTrue(complexProperties.size() > 0, "Content object of type test returned empty complex property list "+propertyPath);
		
		int i = 0;
		for (CmsProperty<?,?> complexProperty : complexProperties){
			if (i > 0){
				assertPropertyPath(complexProperty, propertyPath+"["+i+"]", contentObject.getContentObjectType());
			}
			else{
				assertPropertyPath(complexProperty, propertyPath, contentObject.getContentObjectType());
			}
			
		}
		
		//Try to request simple property with index
		CmsProperty<?, ?> complexPropertyWithIndexZero = createCmsProperty(contentObject,propertyPath+"[0]");
		Assert.assertNotNull(complexPropertyWithIndexZero, "Content object of type test did not return property '"+propertyPath+"[0]'");
		assertPropertyPath(complexPropertyWithIndexZero, propertyPath, contentObject.getContentObjectType());

		//Normally these two instances should be the same
		Assert.assertTrue(System.identityHashCode(complexProperties.get(0)) == System.identityHashCode(complexPropertyWithIndexZero), 
				"Method getCmsProperty() returned different instances for the same actually property ('"+propertyPath+"', '"+propertyPath+"[0]') ");
		
		CmsProperty<?, ?> complexPropertyWithIndexOne = createCmsProperty(contentObject,propertyPath+"[1]");
		Assert.assertNotNull(complexPropertyWithIndexOne, "Content object of type test did not return property '"+propertyPath+"[1]'");
		assertPropertyPath(complexPropertyWithIndexOne, propertyPath+"[1]", contentObject.getContentObjectType());

		CmsProperty<?, ?> complexPropertyWithIndexThree = createCmsProperty(contentObject,propertyPath+"[3]");
		Assert.assertNull(complexPropertyWithIndexThree, "Content object of type test returned property '"+propertyPath+"[3]'");
	}
	
	private void assertPropertyPathForSingleValueComplexProperty(
			ContentObject contentObject, String propertyPath) {
		
		CmsProperty<?, ?> complexProperty = createCmsProperty(contentObject,propertyPath);
		
		Assert.assertNotNull(complexProperty, "Content object of type test did not return complex property list "+propertyPath);
		assertPropertyPath(complexProperty, propertyPath, contentObject.getContentObjectType());
		
		//Try to request simple property with index
		String newPropertyPath = propertyPath+"[0]";
		CmsProperty<?, ?> complexPropertyWithIndexZero = null;

		complexPropertyWithIndexZero = createCmsProperty(contentObject,newPropertyPath);
		Assert.assertNotNull(complexPropertyWithIndexZero, "Content object of type test did not return property '"+newPropertyPath+"'");
		assertPropertyPath(complexPropertyWithIndexZero, propertyPath, contentObject.getContentObjectType());
			
		//Normally these two instances should be the same
		Assert.assertTrue(System.identityHashCode(complexProperty) == System.identityHashCode(complexPropertyWithIndexZero), 
					"Method getCmsProperty() returned different instances for the same actually property ('"+propertyPath+"', '"+newPropertyPath+"') ");

		
		CmsProperty<?, ?> complexPropertyWithIndexOne = createCmsProperty(contentObject,propertyPath+"[1]");
		Assert.assertNull(complexPropertyWithIndexOne, "Content object of type test returned property '"+propertyPath+"[1]'");

		CmsProperty<?, ?> complexPropertyWithIndexThree = createCmsProperty(contentObject,propertyPath+"[3]");
		Assert.assertNull(complexPropertyWithIndexThree, "Content object of type test returned property '"+propertyPath+"[3]'");
	}
	
	private void assertPropertyPathForMultipleValueSimpleProperty(
			ContentObject contentObject, String propertyPath) {
		CmsProperty<?, ?> simpleProperty = createCmsProperty(contentObject,propertyPath);
		
		Assert.assertNotNull(simpleProperty, "Content object of type test did not return property "+propertyPath);
		assertPropertyPath(simpleProperty, propertyPath, contentObject.getContentObjectType());
		
		//Try to request simple property with index
		String newPropertyPath = propertyPath+"[0]";
		CmsProperty<?, ?> simplePropertyWithIndexZero = null;

		simplePropertyWithIndexZero = createCmsProperty(contentObject,newPropertyPath);
		Assert.assertNotNull(simplePropertyWithIndexZero, "Content object of type test did not return property '"+newPropertyPath+"'");
		assertPropertyPath(simplePropertyWithIndexZero, propertyPath, contentObject.getContentObjectType());
			
		//Normally these two instances should be the same
		Assert.assertSame(simpleProperty,simplePropertyWithIndexZero, 
					"Method getCmsProperty() returned different instances for the same actually property ('"+propertyPath+"', '"+newPropertyPath+"') ");


		CmsProperty<?, ?> simpleStringPropertyWithIndexOne = null;

		simpleStringPropertyWithIndexOne = createCmsProperty(contentObject,propertyPath+"[1]");
		Assert.assertNull(simpleStringPropertyWithIndexOne, "Content object of type test returned property '"+propertyPath+"[1]'");

		CmsProperty<?, ?> simplePropertyWithIndexThree = createCmsProperty(contentObject,propertyPath+"[3]");
		Assert.assertNull(simplePropertyWithIndexThree, "Content object of type test returned property '"+propertyPath+"[3]'");
	}
	
	private void assertPropertyPath(CmsProperty<?, ?> simpleProperty,
			String propertyPath, String contentType) {
		
		Assert.assertEquals(simpleProperty.getPath(), propertyPath, "Property path is invalid");
		
		Assert.assertEquals(simpleProperty.getFullPath(), PropertyPath.createFullPropertyPath(contentType, propertyPath), "Property path is invalid");
		
		
	}

	private void assertPropertyPathForSingleValueSimpleProperty(
			ContentObject contentObject, String propertyPath) {
		CmsProperty<?, ?> simpleProperty = createCmsProperty(contentObject,propertyPath);
		
		Assert.assertNotNull(simpleProperty, "Content object of type test did not return property "+propertyPath);
		assertPropertyPath(simpleProperty, propertyPath, contentObject.getContentObjectType());
		
		//Try to request simple property with index
		String newPropertyPath = propertyPath+"[0]";
		
		CmsProperty<?, ?> simplePropertyWithIndexZero = null;

		simplePropertyWithIndexZero = createCmsProperty(contentObject,newPropertyPath);
		Assert.assertNotNull(simplePropertyWithIndexZero, "Content object of type test did not return property '"+newPropertyPath);
		assertPropertyPath(simplePropertyWithIndexZero, propertyPath, contentObject.getContentObjectType());

		//Normally these two instances should be the same
		Assert.assertTrue(System.identityHashCode(simpleProperty) == System.identityHashCode(simplePropertyWithIndexZero), 
					"Method getCmsProperty() returned different instances for the same actually property ('"+propertyPath+"', '"+newPropertyPath+"') ");

		newPropertyPath = propertyPath+"[1]";
		CmsProperty<?, ?> simplePropertyWithIndexOne = createCmsProperty(contentObject,newPropertyPath);
		Assert.assertNull(simplePropertyWithIndexOne, "Content object of type test returned property '"+newPropertyPath+"'");
		
		newPropertyPath = propertyPath+"[3]";
		CmsProperty<?, ?> simplePropertyWithIndexThree = createCmsProperty(contentObject, newPropertyPath);
		Assert.assertNull(simplePropertyWithIndexThree, "Content object of type test returned property '"+newPropertyPath+"'");
		
	}
	
	private CmsProperty<?,?> createCmsProperty(ContentObject contentObject, String property)
	{
		TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsPropertyImpl.class.getName());
		TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsRootPropertyImpl.class.getName());
		
		CmsProperty<?, ?> cmsProperty = contentObject.getCmsProperty(property);
		
		TestLogPolicy.setDefaultLevelForLogger(ComplexCmsPropertyImpl.class.getName());
		TestLogPolicy.setDefaultLevelForLogger(ComplexCmsRootPropertyImpl.class.getName());
		
		return cmsProperty;
	}
	
	@Test 
	public void testSaveLazyLoadAndRemoveCmsProperty(){
		
		RepositoryUser systemUser = getSystemUser();
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("simpleString", Arrays.asList("1"), String.class, Arrays.asList("3"), false, systemUser);
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("simpleStringMultiple", Arrays.asList("RED", "BLUE"), String.class, Arrays.asList("GREEN"), true, systemUser);
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("doubleEnum", Arrays.asList(0.0, 1.3), Double.class, Arrays.asList(3.4), true, systemUser);
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("simpleLong", Arrays.asList((long)1), Long.class, Arrays.asList((long)3), false, systemUser);
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("profile.description", Arrays.asList(TEST_CONTENT_TYPE), String.class, Arrays.asList("testNew"), false, systemUser);
		
		assertSaveLazyLoadAndRemoveSimpleCmsProperty("profile.creator", Arrays.asList("BetaCONCEPT", "Astroboa"), String.class, Arrays.asList("JCR"), true, systemUser);
		
		assertSaveLazyLoadAndRemoveComplexCmsProperty("comment", true, systemUser);
		
		assertSaveLazyLoadAndRemoveComplexCmsProperty("comment[0].comment", true, systemUser);
		
		assertSaveLazyLoadAndRemoveComplexCmsProperty("commentSingle", false, systemUser);
		
		assertSaveLazyLoadAndRemoveComplexCmsProperty("workflow", false, systemUser);
		
	}
	
	
	private <T> void assertSaveLazyLoadAndRemoveSimpleCmsProperty(String propertyPath, List propertyValues, Class<T> propertyValueClass, List newValues, boolean multiple, RepositoryUser systemUser){

		ContentObject contentObject = createContentObject(systemUser, "saveLazyLoadAndRemoveSimpleCmsProperty"+propertyPath.replaceAll("\\.", ""), true);

		//Add values
		if (CollectionUtils.isNotEmpty(propertyValues)){
			for (Object value : propertyValues){
				((SimpleCmsProperty<T,?,?>)contentObject.getCmsProperty(propertyPath)).addSimpleTypeValue((T)value);
			}
		}
		
		contentObject = contentService.save(contentObject, false, true ,null);
		
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		
		assertPropertyHasTheCorrectValues(propertyPath, contentObject, propertyValues, multiple);
		
		//Now remove property
		Assert.assertTrue(contentObject.removeCmsProperty(propertyPath), propertyPath + " was not properly removed.");

		//Check that property is removed without saving object
		assertPropertyHasBeenRemoved(propertyPath, contentObject, false, multiple);

		//Save content object
		contentObject = contentService.save(contentObject, false, true, null);
		
		//Check that property is removed without reloading object
		assertPropertyHasBeenRemoved(propertyPath, contentObject,false, multiple);
		
		//Reload object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		assertPropertyHasBeenRemoved(propertyPath, contentObject,false, multiple);
		
		//Add values again 
		if (CollectionUtils.isNotEmpty(propertyValues)){
			for (Object value : propertyValues){
				((SimpleCmsProperty<T,?,?>)contentObject.getCmsProperty(propertyPath)).addSimpleTypeValue((T)value);
			}
		}
		
		contentObject = contentService.save(contentObject, false, true, null);

		//remove property but load new values
		Assert.assertTrue(contentObject.removeCmsProperty(propertyPath), propertyPath + " was not properly removed.");
		
		//Load new values
		if (CollectionUtils.isNotEmpty(newValues)){
			for (Object value : newValues){
				((SimpleCmsProperty<T,?,?>)contentObject.getCmsProperty(propertyPath)).addSimpleTypeValue((T)value);
			}
		}
		
		//
		assertPropertyHasTheCorrectValues(propertyPath, contentObject, newValues, multiple);
		
		//Save object and recheck
		contentObject = contentService.save(contentObject, false, true, null);
		
		assertPropertyHasTheCorrectValues(propertyPath, contentObject, newValues, multiple);
		
		//reload object and check
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		assertPropertyHasTheCorrectValues(propertyPath, contentObject, newValues, multiple);
		
		
	}
	
	private <T> void assertSaveLazyLoadAndRemoveComplexCmsProperty(String propertyPath, boolean multiple, RepositoryUser systemUser){

		ContentObject contentObject = createContentObject(systemUser, "saveLazyLoadAndRemoveComplexCmsProperty"+
				propertyPath.replaceAll("[^"+CmsConstants.SYSTEM_NAME_ACCEPTABLE_CHARACTERS+"]", "-"), true);

		//Create property
		contentObject.getCmsProperty(propertyPath);
		
		contentObject = contentService.save(contentObject, false, true, null);
		
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		if (multiple)
		{
			assertPropertyHasTheCorrectValues(propertyPath+"[0]", contentObject, null, multiple);
		}
		else
		{
			assertPropertyHasTheCorrectValues(propertyPath, contentObject, null, multiple);
		}
		
		//Now remove property
		Assert.assertTrue(contentObject.removeCmsProperty(propertyPath), propertyPath + " was not properly removed.");

		//Check that property is removed without saving object
		assertPropertyHasBeenRemoved(propertyPath, contentObject,true, false);

		//Since upon checking for property removal, we force property recreation
		//we remove property once again. And we check once more
		Assert.assertTrue(contentObject.removeCmsProperty(propertyPath), propertyPath + " was not properly removed.");
		
		//Save content object
		contentObject = contentService.save(contentObject, false, true, null);
		
		//Check that property is removed without reloading object
		assertPropertyHasBeenRemoved(propertyPath, contentObject,true, multiple);
		
		//Reload object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		assertPropertyHasBeenRemoved(propertyPath, contentObject,true, multiple);
		
		
	}



	private void assertPropertyHasTheCorrectValues(String propertyPath,
			ContentObject contentObject, List values, boolean multiple) {
		
		CmsProperty<?, ?> cmsProperty = contentObject.getCmsProperty(propertyPath);
		
		Assert.assertNotNull(cmsProperty,propertyPath+" was not saved.");
		
		if (cmsProperty instanceof SimpleCmsProperty){
			Assert.assertTrue(((SimpleCmsProperty<?,?,?>) cmsProperty).hasValues(),propertyPath+ " does not have any values.");
			
			if (!multiple){
				Assert.assertEquals(values.get(0), (((SimpleCmsProperty<?,?,?>) cmsProperty).getSimpleTypeValue()), "Value "+ ((SimpleCmsProperty<?,?,?>) cmsProperty).getSimpleTypeValue() + " does not exist in property "+ propertyPath);
			}
			else{
				for (Object value : values){
					Assert.assertTrue(((SimpleCmsProperty<?,?,?>) cmsProperty).getSimpleTypeValues().contains(value), "Value "+ value + " does not exist in property "+ propertyPath);
				}
			}
		}
	}
	
	private void assertPropertyHasBeenRemoved(String propertyPath,
			ContentObject contentObject, boolean propertyIsComplex, boolean multiple) {
		
		Assert.assertNotNull(contentObject.getCmsProperty(propertyPath), propertyPath + " was removed but when called getCmsProperty it was not created.");

		if (! propertyIsComplex)
		{
		
			Assert.assertTrue(((SimpleCmsProperty<?,?,?>) contentObject.getCmsProperty(propertyPath)).hasNoValues(), propertyPath + " was not properly removed." );
		}
	}
	
	
	@Test
	public void testRemovalOfACmsProperty(){

		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser,"testRemovalOfACmsProperty", true);

		//Add complex cms property
		((CalendarProperty)contentObject.getCmsProperty("webPublication.webPublicationStartDate")).setSimpleTypeValue(Calendar.getInstance());
		
		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
	
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"), "webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasValues(), "webPublication.webPublicationStartDate does not have any values.");
		
		//Now remove simple property
		Assert.assertTrue(contentObject.removeCmsProperty("webPublication.webPublicationStartDate"), "webPublication.webPublicationStartDate was not removed.");

		//Check that property is removed without saving object
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"), "webPublication.webPublicationStartDate was not removed.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));

		
		contentObject = contentService.save(contentObject, false, true, null);
		
		//Check that property is removed without reloading object
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"),"webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));
		
		//Reload object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);;
		
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"), "webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));

		
		//Now remove property complex property
		((CalendarProperty)contentObject.getCmsProperty("webPublication.webPublicationStartDate")).setSimpleTypeValue(Calendar.getInstance());
		
		contentObject = contentService.save(contentObject, false, true, null);

		Assert.assertTrue(contentObject.removeCmsProperty("webPublication"), "webPublication.webPublicationStartDate was not removed");

		//Check that property is removed without saving object
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"),"webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));

		
		contentObject = contentService.save(contentObject, false, true, null);
		
		//Check that property is removed without reloading object
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"),"webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));
		
		//Reload object
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertNotNull(contentObject.getCmsProperty("webPublication.webPublicationStartDate"),
				"webPublication.webPublicationStartDate was not saved.");
		
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));

		//Check that property is removed without being loaded in the first place
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		Assert.assertTrue(contentObject.removeCmsProperty("webPublication"), "webPublication.webPublicationStartDate was not removed");
		
		contentObject = contentService.save(contentObject, false, true, null);

		//Check right after save
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));


		//Check on a new instance
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		Assert.assertTrue(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).hasNoValues(),
				"webPublication.webPublicationStartDate was not properly removed."+
				DateUtils.format(((CalendarProperty) contentObject.getCmsProperty("webPublication.webPublicationStartDate")).getSimpleTypeValue()));
		

	}

	@Test
	public void testSwapComplexCmsPropertyValues() throws Exception
	{
		RepositoryUser systemUser = getSystemUser();

		ContentObject contentObject = createContentObject(systemUser,"testSwapComplexCmsProperty", true);

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("test[0]", 0, 1), "Method swap child property values retuned true for property test[0]");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("test[]", 0, 1), "Method swap child property values retuned true for property test[]");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues(TEST_CONTENT_TYPE, 0, 0), "Method swap child property values retuned true for property test and for same indices");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues(TEST_CONTENT_TYPE, -1,-1), "Method swap child property values retuned true for property test and for same indices");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues(TEST_CONTENT_TYPE, 20,20), "Method swap child property values retuned true for property test and for same indices");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues(TEST_CONTENT_TYPE, -1, 0), "Method swap child property values retuned true for property test and for negative indices");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("simpleString", 0, 10), "Method swap child property values retuned true for simple single property string enum" );
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("webPublication", 0, 10), "Method swap child property values retuned true for simple complex property webPublication");
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("containerOfAllPossiblePropertiesType.simpleDouble", 0, 10), "Method swap child property values retuned true for single value simple property inside aspect containerOfAllPossiblePropertiesType.simpleDouble");
		
		contentObject = contentService.save(contentObject, false, true, null);
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().hasAspect("containerOfAllPossiblePropertiesType"));
		
		Assert.assertFalse(getSession().getNodeByUUID(contentObject.getId()).hasNode("containerOfAllPossiblePropertiesType"));
		
		Assert.assertFalse(getSession().getNodeByUUID(contentObject.getId()).hasProperty(CmsBuiltInItem.Aspects.getJcrName()));
		
		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("containerOfAllPossiblePropertiesType", 0, 10), "Method swap child property values retuned true for aspect containerOfAllPossiblePropertiesType");

		Assert.assertFalse(contentObject.getComplexCmsRootProperty().swapChildPropertyValues("containerOfAllPossiblePropertiesType.simpleDoubleMultiple", 0, 10), "Method swap child property values retuned true for single value simple property inside empty aspect containerOfAllPossiblePropertiesType.simpleDoubleMultiple");

		DoubleProperty simpleDoubleMultipleProperty = (DoubleProperty) contentObject.getCmsProperty("containerOfAllPossiblePropertiesType.simpleDoubleMultiple");
		
		simpleDoubleMultipleProperty.addSimpleTypeValue((double)10);
		simpleDoubleMultipleProperty.addSimpleTypeValue((double)9);
		simpleDoubleMultipleProperty.addSimpleTypeValue((double)8);
		simpleDoubleMultipleProperty.addSimpleTypeValue((double)7);
		
		Assert.assertFalse(simpleDoubleMultipleProperty.swapValues(0, 0), "Method swap simple property values retuned true for property "+simpleDoubleMultipleProperty.getName()+" and for same indices");
		Assert.assertFalse(simpleDoubleMultipleProperty.swapValues(-1,-1), "Method swap simple property values retuned true for property "+simpleDoubleMultipleProperty.getName()+" and for same indices");
		Assert.assertFalse(simpleDoubleMultipleProperty.swapValues(20,20), "Method swap simple property values retuned true for property "+simpleDoubleMultipleProperty.getName()+" and for same indices");
		Assert.assertFalse(simpleDoubleMultipleProperty.swapValues(-1, 0), "Method swap simple property values retuned true for property "+simpleDoubleMultipleProperty.getName()+" and for negative indices");

		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 1, 2, (double)9, (double)8);
		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 0, 3, (double)10, (double)7);
		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 2, 3, (double)9, (double)10);
		
		contentObject = contentService.save(contentObject, false, true, null);
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		simpleDoubleMultipleProperty = (DoubleProperty) contentObject.getCmsProperty("containerOfAllPossiblePropertiesType.simpleDoubleMultiple");
		
		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 1, 2, (double)8, (double)10);
		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 0, 3, (double)7, (double)9);
		assertSwapIsSuccessfull(simpleDoubleMultipleProperty, 2, 3, (double)8, (double)7);

		contentObject = contentService.save(contentObject, false, true, null);
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		simpleDoubleMultipleProperty = (DoubleProperty) contentObject.getCmsProperty("containerOfAllPossiblePropertiesType.simpleDoubleMultiple");

		Assert.assertEquals(simpleDoubleMultipleProperty.getSimpleTypeValues().get(0), (double)9);
		Assert.assertEquals(simpleDoubleMultipleProperty.getSimpleTypeValues().get(1), (double)10);
		Assert.assertEquals(simpleDoubleMultipleProperty.getSimpleTypeValues().get(2), (double)7);
		Assert.assertEquals(simpleDoubleMultipleProperty.getSimpleTypeValues().get(3), (double)8);
		
		
		//complex property
		ComplexCmsProperty<?,?> commentMultipleProperty1 = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty("comment");
		((StringProperty)commentMultipleProperty1.getChildProperty("body")).setSimpleTypeValue("1");
		ComplexCmsProperty<?,?> commentMultipleProperty2 = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty("comment[1]");
		((StringProperty)commentMultipleProperty2.getChildProperty("body")).setSimpleTypeValue("2");
		ComplexCmsProperty<?,?> commentMultipleProperty3 = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty("comment[2]");
		((StringProperty)commentMultipleProperty3.getChildProperty("body")).setSimpleTypeValue("3");
		ComplexCmsProperty<?,?> commentMultipleProperty4 = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty("comment[3]");
		((StringProperty)commentMultipleProperty4.getChildProperty("body")).setSimpleTypeValue("4");
		
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 1, 2, "2", "3", "body");
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 0, 3, "1", "4", "body");
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 2, 3, "2", "1", "body");
		
		contentObject = contentService.save(contentObject, false, true, null);
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 1, 2, "3", "1", "body");
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 0, 3, "4", "2", "body");
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 2, 3, "3", "4", "body");
		
		contentObject = contentService.save(contentObject, false, true, null);
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		logger.debug("Final places {}",TestUtils.prettyPrintXml(contentObject.xml(prettyPrint)));
		
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[0].body")).getSimpleTypeValue(), "2");
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[1].body")).getSimpleTypeValue(),  "1");
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[2].body")).getSimpleTypeValue(), "4");
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[3].body")).getSimpleTypeValue(), "3");

		
		//Now remove one property, swap some others check, save and recheck
		Assert.assertTrue(contentObject.removeCmsProperty("comment[1]"), "Property comment[1] was not removed");
		
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 1, 2, "4", "3", "body");
		
		contentObject = contentService.save(contentObject, false, true, null);
		contentObject = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY,  CacheRegion.NONE, null, false);
		
		logger.debug("Final places {}",TestUtils.prettyPrintXml(contentObject.xml(prettyPrint)));
		
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[0].body")).getSimpleTypeValue(), "2");
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[1].body")).getSimpleTypeValue(),  "3");
		Assert.assertEquals(((StringProperty)contentObject.getCmsProperty("comment[2].body")).getSimpleTypeValue(), "4");
		
		assertSwapIsSuccessfullForComplexProperty(contentObject, "comment", 0, 2, "2", "4", "body");


	}
	
	@Test
	public void testRemovalOfAMultipleRecursiveComplexCmsProperty(){

		RepositoryUser systemUser = getSystemUser();

		ContentObject referencedContentObject = retrieveSystemPersonObject();

		ContentObject contentObject = createContentObject(systemUser,"testRemovalOfAMultipleRecursiveComplexCmsProperty", true);

		((StringProperty)contentObject.getCmsProperty("departments.department[0].name")).setSimpleTypeValue("Test department");
		
		contentObject = contentService.saveContentObject(contentObject, false);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		String propertyPath = "departments.department[0].jobPositions.jobPosition";
		
		for (int i=0;i<5;i++){
			contentObject = createMultipleComplexCmsProperty(contentObject, propertyPath, "description", "personObjectReference", referencedContentObject);
		
			String propertyPathToRemove = propertyPath+"["+i+"]";

			//Remove complex property
			Assert.assertTrue(contentObject.removeCmsProperty(propertyPathToRemove), propertyPathToRemove+" was not removed");

			//	Check that property is removed without saving object
			for (int j=0;j<4;j++){
				String newPropertyPath = propertyPath+"["+j+"]";

				assertMultipleComplexCmsPropertiesAfterRemoval(contentObject, i, newPropertyPath, j, "description");
			}
		
			//Save content object to persist changes	
			contentObject = contentService.saveContentObject(contentObject, false);
			
			//	Check that property is removed without reloading object
			for (int j=0;j<4;j++){
				String newPropertyPath = propertyPath+"["+j+"]";

				assertMultipleComplexCmsPropertiesAfterRemoval(contentObject, i, newPropertyPath, j, "description");
			}

			//Reload object
			contentObject = contentService.getContentObjectById(contentObject.getId(), null);
			
			//	Check that property is removed after reloading object
			for (int j=0;j<4;j++){
				String newPropertyPath = propertyPath +"["+j+"]";

				assertMultipleComplexCmsPropertiesAfterRemoval(contentObject, i, newPropertyPath, j, "description");
			}
		}
		
	}


	/**
	 * @return
	 */
	private ContentObject retrieveSystemPersonObject() {
		
		ContentObjectCriteria coCriteria = CmsCriteriaFactory.newContentObjectCriteria("personObject");
		
		coCriteria.addSystemNameEqualsCriterion("SYSTEM");
		coCriteria.setSearchMode(SearchMode.SEARCH_ALL_ENTITIES);
		coCriteria.doNotCacheResults();
		coCriteria.setOffsetAndLimit(0, 1);
		
		final CmsOutcome<CmsRankedOutcome<ContentObject>> outcome = contentService.searchContentObjects(coCriteria);
		
		if (outcome.getCount() >= 1){
			return outcome.getResults().get(0).getCmsRepositoryEntity();
		}
		
		return null;
	}


	private void assertMultipleComplexCmsPropertiesAfterRemoval(
			ContentObject contentObject, int indexRemoved, String propertyPathToRemove, int currentPropertyIndex, String simplePropertyName) {
		
		if (indexRemoved == currentPropertyIndex){
			Assert.assertEquals(((StringProperty)contentObject.getCmsProperty(propertyPathToRemove+"."+simplePropertyName)).getSimpleTypeValue(),
					"comment-"+(indexRemoved+1), propertyPathToRemove +" was not properly removed from instance");
		}
		else if (indexRemoved < currentPropertyIndex){
			Assert.assertEquals(((StringProperty)contentObject.getCmsProperty(propertyPathToRemove+"."+simplePropertyName)).getSimpleTypeValue(),
					"comment-"+(currentPropertyIndex+1), propertyPathToRemove +" was not properly removed from instance");
		}
		else if (indexRemoved > currentPropertyIndex){
			Assert.assertEquals(((StringProperty)contentObject.getCmsProperty(propertyPathToRemove+"."+simplePropertyName)).getSimpleTypeValue(),
					"comment-"+currentPropertyIndex, propertyPathToRemove +" was not properly removed from instance");
		}
		
	}


	private ContentObject createMultipleComplexCmsProperty(
			ContentObject contentObject, String propertyPath, String simpleChildPropertyName, String propertyNameOfTypeContentObjectReference, ContentObject referencedContentObject) {
		
		if (contentObject.hasValueForProperty(propertyPath)){
			
			ComplexCmsProperty<?,?> property = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty(propertyPath);
			
			if (property.getPropertyDefinition().isMandatory()){
				contentObject.removeCmsProperty(StringUtils.substringBeforeLast(propertyPath, "."));
			}
			else{
				contentObject.removeCmsProperty(propertyPath);
			}
			
			contentObject = contentService.saveContentObject(contentObject, false);	
		}
		//Add complex cms property
		for (int i=0;i<5;i++){
			ComplexCmsProperty<?,?> property = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty(propertyPath+"["+i+"]");
			((StringProperty)property.getChildProperty(simpleChildPropertyName)).setSimpleTypeValue("comment-"+i);
			((ContentObjectProperty)property.getChildProperty(propertyNameOfTypeContentObjectReference)).setSimpleTypeValue(referencedContentObject);
		}
		
		contentObject = contentService.saveContentObject(contentObject, false);
		
		//Assert save
		for (int i=0;i<5;i++){
			
			String complexPropertyPath = propertyPath+"["+i+"]";

			assertPropertyExistence(contentObject, complexPropertyPath,simpleChildPropertyName, "comment-"+i);
		}

		return contentObject;
	}


	private void assertPropertyExistence(ContentObject contentObject, String complexPropertyPath, 
			String simplePropertyName, String expectedValueOfSimpleProperty) {
		Assert.assertNotNull(contentObject.getCmsProperty(complexPropertyPath), complexPropertyPath+" was not saved.");
		
		final String simplePropertyPath = complexPropertyPath+"."+simplePropertyName;
		
		final StringProperty cmsProperty = (StringProperty) contentObject.getCmsProperty(simplePropertyPath);
		
		Assert.assertNotNull(cmsProperty, simplePropertyPath+" was not saved.");

		Assert.assertEquals(cmsProperty.getSimpleTypeValue(), expectedValueOfSimpleProperty, simplePropertyPath+ " has invalid value.");
	}
	
	private void assertSwapIsSuccessfullForComplexProperty(ContentObject contentObject, String complexPropertyPath, int from, int to, Object fromValue, Object toValue, String childPropertyPath)
	{
		List<CmsProperty<?, ?>> childProperties = contentObject.getCmsPropertyList(complexPropertyPath);

		logger.debug("Before swapping elements from {} to {} of property {}", new Object[]{from ,to, complexPropertyPath});
		logger.debug(TestUtils.prettyPrintXml(contentObject.xml(prettyPrint)));
		logger.debug(childProperties.toString());
		
		Assert.assertTrue(contentObject.getComplexCmsRootProperty().swapChildPropertyValues(complexPropertyPath, from, to), "Method swap for property  " + complexPropertyPath+ " was not successful");

		Object newFromValue = ((SimpleCmsProperty)((ComplexCmsProperty<?,?>)childProperties.get(from)).getChildProperty(childPropertyPath)).getSimpleTypeValue();
		Object newToValue = ((SimpleCmsProperty)((ComplexCmsProperty<?,?>)childProperties.get(to)).getChildProperty(childPropertyPath)).getSimpleTypeValue();
		
		Assert.assertEquals(newFromValue, toValue, "Value "+toValue + " does not exist in index "+from + " in property "+complexPropertyPath+ " "+childPropertyPath);
		
		Assert.assertEquals(newToValue, fromValue, "Value "+fromValue + " does not exist in index "+to + " in property "+complexPropertyPath+ " "+childPropertyPath);
		
		logger.debug("After swapping elements from {} to {} of property {}", new Object[]{from ,to, complexPropertyPath});
		logger.debug(TestUtils.prettyPrintXml(contentObject.xml(prettyPrint)));
		logger.debug(childProperties.toString());

	}
	
	private void assertSwapIsSuccessfull(SimpleCmsProperty property, int from, int to, Object fromValue, Object toValue){
		Assert.assertTrue(property.swapValues(from, to), "Method swap for property  " + property.getPath()+ " was not successful");
				
		Assert.assertEquals(property.getSimpleTypeValues().get(from), toValue, "Value "+toValue + " does not exist in index "+from + " in property "+property.getPath()+ " "+property.getSimpleTypeValues());
		
		Assert.assertEquals(property.getSimpleTypeValues().get(to), fromValue, "Value "+fromValue + " does not exist in index "+to + " in property "+property.getPath()+ " "+property.getSimpleTypeValues());
	}
}
