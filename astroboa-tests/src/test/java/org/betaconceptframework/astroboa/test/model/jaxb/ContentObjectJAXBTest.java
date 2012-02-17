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
package org.betaconceptframework.astroboa.test.model.jaxb;


import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsRootPropertyImpl;
import org.betaconceptframework.astroboa.model.jaxb.MarshalUtils;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.test.engine.CmsPropertyPath;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectJAXBTest extends AbstractRepositoryTest{

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testExportObjectReference() throws Throwable{

		ContentObject contentObjectForTestExportObjectReference = createContentObject(getSystemUser(), "testExportObjectReference", false);
		contentObjectForTestExportObjectReference = contentService.save(contentObjectForTestExportObjectReference, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObjectForTestExportObjectReference);


		ContentObject referencedObject = createContentObject(getSystemUser(), "referencedObjectForTestExportObjectReference", false);
		referencedObject = contentService.save(referencedObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(referencedObject);

		((ObjectReferenceProperty)contentObjectForTestExportObjectReference.getCmsProperty("simpleContentObject")).addSimpleTypeValue(referencedObject);
		((ObjectReferenceProperty)contentObjectForTestExportObjectReference.getCmsProperty("simpleContentObjectMultiple")).addSimpleTypeValue(referencedObject);
		contentObjectForTestExportObjectReference = contentService.save(contentObjectForTestExportObjectReference, false, true, null);

		String jsonFromApi = null;
		String jsonFromService = null;

		try{
			jsonFromApi = contentObjectForTestExportObjectReference.json(prettyPrint, false, "simpleContentObject", "simpleContentObjectMultiple");
			jsonFromService = contentService.getContentObject(contentObjectForTestExportObjectReference.getId(), ResourceRepresentationType.JSON, FetchLevel.ENTITY_AND_CHILDREN, 
					CacheRegion.NONE, Arrays.asList("simpleContentObject","simpleContentObjectMultiple"), false);

			jsonFromApi = removeWhitespacesIfNecessary(jsonFromApi);
			jsonFromService = removeWhitespacesIfNecessary(jsonFromService);
			
			ContentObject contentObjectMultipleFirstValue = ((ObjectReferenceProperty)contentObjectForTestExportObjectReference.getCmsProperty("simpleContentObjectMultiple")).getFirstValue();

			String profileId = ((ComplexCmsProperty)contentObjectMultipleFirstValue.getCmsProperty("profile")).getId();
			String title = ((StringProperty)contentObjectMultipleFirstValue.getCmsProperty("profile.title")).getSimpleTypeValue();
			
			String expectedOutcome = "\"simpleContentObjectMultiple\":[{"; //Check that property is exported as an array
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"cmsIdentifier\":\""+contentObjectMultipleFirstValue.getId()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"systemName\":\""+contentObjectMultipleFirstValue.getSystemName()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"contentObjectTypeName\":\""+contentObjectMultipleFirstValue.getContentObjectType()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"url\":\""+contentObjectMultipleFirstValue.getResourceApiURL(ResourceRepresentationType.JSON, false,contentObjectMultipleFirstValue.getSystemName()!=null)+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);
			
			expectedOutcome = "\"profile\":{\"cmsIdentifier\":\""+profileId+"\",\"title\":\""+title+"\"}";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);
			
			ContentObject simpleContentObject = ((ObjectReferenceProperty)contentObjectForTestExportObjectReference.getCmsProperty("simpleContentObjectMultiple")).getFirstValue();
			profileId = ((ComplexCmsProperty)simpleContentObject.getCmsProperty("profile")).getId();
			title = ((StringProperty)simpleContentObject.getCmsProperty("profile.title")).getSimpleTypeValue();

			
			expectedOutcome = "\"simpleContentObject\":{"; //Check that property is exported as an array
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"cmsIdentifier\":\""+simpleContentObject.getId()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"systemName\":\""+simpleContentObject.getSystemName()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"contentObjectTypeName\":\""+simpleContentObject.getContentObjectType()+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

			expectedOutcome = "\"url\":\""+simpleContentObject.getResourceApiURL(ResourceRepresentationType.JSON, false,simpleContentObject.getSystemName()!=null)+"\"";
			assertPropertyIsExported(jsonFromApi,expectedOutcome);
			assertPropertyIsExported(jsonFromService,expectedOutcome);

		}
		catch(Throwable e){

			StringBuilder sb = new StringBuilder();
			sb.append("JSON From API \n");
			sb.append(jsonFromApi);

			sb.append("\nJSON From Service \n");
			sb.append(jsonFromService);

			logger.error(sb.toString(), e);

			throw e;
		}




	}

	@Test
	public void testMarshalUtilsForExportedProperties(){

		List<String> propertyPathsToMarshal = Arrays.asList("profile.title", "accessibility.canBeReadBy", "authorship", "user.comment.body");

		assertPropertyShouldBeExported(propertyPathsToMarshal, "title", "profile.title", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "profile", "profile", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "canBeReadBy", "accessibility.canBeReadBy", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "accessibility", "accessibility", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "authorship", "authorship", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "body", "user.comment.body", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "comment", "user.comment", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "user", "user", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "footer", "user.comment.body.footer", true);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "banner", "user.comment.body.footer.banner", true);


		assertPropertyShouldBeExported(propertyPathsToMarshal, "created", "created", false);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "created", "profile.created", false);
		assertPropertyShouldBeExported(propertyPathsToMarshal, "author", "author", false);

	}

	private void assertPropertyShouldBeExported(List<String> propertyPathsToMarshal, String propertyName, String propertyPath, boolean shouldBeExported){
		if (shouldBeExported){
			Assert.assertTrue(MarshalUtils.propertyShouldBeMarshalled(propertyPathsToMarshal, propertyName, propertyPath), "Property "+propertyName + " in path "+propertyPath + " has not been exported");
		}
		else{
			Assert.assertFalse(MarshalUtils.propertyShouldBeMarshalled(propertyPathsToMarshal, propertyName, propertyPath), "Property "+propertyName + " in path "+propertyPath + " has been exported");
		}
	}

	@Test
	public void testMultiplePropertiesExportedAsArrayInJSON() throws Throwable{

		final String[] propertiesToRender = new String[]{"stringEnum","comment", "simpleDoubleMultiple", "accessibility",
				"simpleContentObjectMultiple", "simpleTopicMultiple",
				"simpleStringMultiple","simpleBinaryMultiple","simpleBooleanMultiple","simpleDateMultiple",
				"simpleDateTimeMultiple","simpleLongMultiple"};

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.addSystemNameContainsCriterion("*testMultiplePropertiesExportedAsArrayInJSON");

		for (String propertyPath : propertiesToRender){
			contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded(propertyPath);
		}

		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		logger.debug("Processing {} contentObjects", cmsOutcome.getCount() );


		for (ContentObject contentObject : cmsOutcome.getResults()){

			String jsonFromApi = null;
			String jsonFromService = null;

			try{

				contentObject = leaveAllMultiplePropertiesWithASingleValue(contentObject, propertiesToRender);


				//Generate JSON exports from API and Service API
				jsonFromApi = contentObject.json(prettyPrint, false,propertiesToRender);
				jsonFromApi = removeWhitespacesIfNecessary(jsonFromApi);

				jsonFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL, 
						CacheRegion.NONE, Arrays.asList(propertiesToRender), false);
				jsonFromService = removeWhitespacesIfNecessary(jsonFromService);

				//Generate expected outcomes for pre rendered properties
				String expectedStringEnumExport = "\"stringEnum\":[\""+((StringProperty)contentObject.getCmsProperty("stringEnum")).getFirstValue()+"\","+
				"\""+((StringProperty)contentObject.getCmsProperty("stringEnum")).getSimpleTypeValues().get(1)+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedStringEnumExport);
				assertPropertyIsExported(jsonFromService,expectedStringEnumExport);

				String expectedCommentExport = "\"comment\":[{\"cmsIdentifier\":\""+((ComplexCmsProperty)contentObject.getCmsProperty("comment")).getId()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedCommentExport);
				assertPropertyIsExported(jsonFromService,expectedCommentExport);

				String expectedSimpleDoubleMultipleExport = "\"simpleDoubleMultiple\":[\""+((DoubleProperty)contentObject.getCmsProperty("simpleDoubleMultiple")).getFirstValue()+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedSimpleDoubleMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedSimpleDoubleMultipleExport);

				String expectedAccessibilityExport = "\"accessibility\":{\"cmsIdentifier\":\""+((ComplexCmsProperty)contentObject.getCmsProperty("accessibility")).getId()+"\","+
				"\"canBeReadBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"\"],"+
				"\"canBeTaggedBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeTaggedBy")).getFirstValue()+"\"],"+
				"\"canBeUpdatedBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).getFirstValue()+"\"],"+
				"\"canBeDeletedBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).getFirstValue()+"\"]}";
				assertPropertyIsExported(jsonFromApi,expectedAccessibilityExport);
				assertPropertyIsExported(jsonFromService,expectedAccessibilityExport);

				//Since attribute order is not the same between API export and Service Export
				//we check export in pieces
				ContentObject contentObjectMultipleFirstValue = ((ObjectReferenceProperty)contentObject.getCmsProperty("simpleContentObjectMultiple")).getFirstValue();
				String expectedSimpleContentObjectMultiple = "\"simpleContentObjectMultiple\":[{"; //Check that property is exported as an array
				assertPropertyIsExported(jsonFromApi,expectedSimpleContentObjectMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleContentObjectMultiple);

				expectedSimpleContentObjectMultiple = "\"cmsIdentifier\":\""+contentObjectMultipleFirstValue.getId()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleContentObjectMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleContentObjectMultiple);

				expectedSimpleContentObjectMultiple = "\"systemName\":\""+contentObjectMultipleFirstValue.getSystemName()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleContentObjectMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleContentObjectMultiple);

				expectedSimpleContentObjectMultiple = "\"contentObjectTypeName\":\""+contentObjectMultipleFirstValue.getContentObjectType()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleContentObjectMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleContentObjectMultiple);

				expectedSimpleContentObjectMultiple = "\"url\":\""+contentObjectMultipleFirstValue.getResourceApiURL(ResourceRepresentationType.JSON, false,contentObjectMultipleFirstValue.getSystemName()!=null)+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleContentObjectMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleContentObjectMultiple);

				Topic topicFirstValue = ((TopicReferenceProperty)contentObject.getCmsProperty("simpleTopicMultiple")).getFirstValue();
				String expectedSimpleTopicMultiple = "\"simpleTopicMultiple\":[{";
				assertPropertyIsExported(jsonFromApi,expectedSimpleTopicMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleTopicMultiple);

				expectedSimpleTopicMultiple = "\"cmsIdentifier\":\""+topicFirstValue.getId()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleTopicMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleTopicMultiple);

				expectedSimpleTopicMultiple ="\"name\":\""+topicFirstValue.getName()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleTopicMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleTopicMultiple);

				expectedSimpleTopicMultiple ="\"url\":\""+topicFirstValue.getResourceApiURL(ResourceRepresentationType.JSON, false,topicFirstValue.getName()!=null)+"\"";
				assertPropertyIsExported(jsonFromApi,expectedSimpleTopicMultiple);
				assertPropertyIsExported(jsonFromService,expectedSimpleTopicMultiple);


				BinaryChannel simpleBinaryMultipleFirstValue = ((BinaryProperty)contentObject.getCmsProperty("simpleBinaryMultiple")).getFirstValue();
				String expectedsimpleBinaryMultipleExport = "\"simpleBinaryMultiple\":[{";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBinaryMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBinaryMultipleExport);

				expectedsimpleBinaryMultipleExport = "\"mimeType\":\""+simpleBinaryMultipleFirstValue.getMimeType()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBinaryMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBinaryMultipleExport);

				expectedsimpleBinaryMultipleExport = "\"lastModificationDate\":\""+ISO8601.format(simpleBinaryMultipleFirstValue.getModified())+"\"";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBinaryMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBinaryMultipleExport);

				expectedsimpleBinaryMultipleExport = "\"sourceFileName\":\""+simpleBinaryMultipleFirstValue.getSourceFilename()+"\"";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBinaryMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBinaryMultipleExport);

				expectedsimpleBinaryMultipleExport = "\"url\":\""+simpleBinaryMultipleFirstValue.getResourceApiURL(ResourceRepresentationType.JSON, false,false)+"\"";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBinaryMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBinaryMultipleExport);

				String simpleStringMultipleFirstValue = ((StringProperty)contentObject.getCmsProperty("simpleStringMultiple")).getFirstValue();
				String expectedsimpleStringMultipleExport = "\"simpleStringMultiple\":[\""+simpleStringMultipleFirstValue+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedsimpleStringMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleStringMultipleExport);

				Boolean simpleBooleanMultipleFirstValue = ((BooleanProperty)contentObject.getCmsProperty("simpleBooleanMultiple")).getFirstValue();
				String expectedsimpleBooleanMultipleExport = "\"simpleBooleanMultiple\":[\""+simpleBooleanMultipleFirstValue+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedsimpleBooleanMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleBooleanMultipleExport);

				Calendar simpleDateMultipleFirstValue = ((CalendarProperty)contentObject.getCmsProperty("simpleDateMultiple")).getFirstValue();
				String expectedsimpleDateMultipleExport = "\"simpleDateMultiple\":[\""+DateUtils.format(simpleDateMultipleFirstValue, CmsConstants.DATE_PATTERN)+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedsimpleDateMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleDateMultipleExport);

				Calendar simpleDateTimeMultipleFirstValue = ((CalendarProperty)contentObject.getCmsProperty("simpleDateTimeMultiple")).getFirstValue();
				String expectedsimpleDateTimeMultipleExport = "\"simpleDateTimeMultiple\":[\""+ISO8601.format(simpleDateTimeMultipleFirstValue)+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedsimpleDateTimeMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleDateTimeMultipleExport);

				Long simpleLongMultipleFirstValue = ((LongProperty)contentObject.getCmsProperty("simpleLongMultiple")).getFirstValue();
				String expectedsimpleLongMultipleExport = "\"simpleLongMultiple\":[\""+simpleLongMultipleFirstValue+"\"]";
				assertPropertyIsExported(jsonFromApi,expectedsimpleLongMultipleExport);
				assertPropertyIsExported(jsonFromService,expectedsimpleLongMultipleExport);

			}
			catch(Throwable e){

				StringBuilder sb = new StringBuilder();
				sb.append("JSON From API \n");
				sb.append(jsonFromApi);

				sb.append("\nJSON From Service \n");
				sb.append(jsonFromService);

				logger.error(sb.toString(), e);

				throw e;
			}
		}

	}

	private ContentObject leaveAllMultiplePropertiesWithASingleValue(
			ContentObject contentObject, String[] propertiesToRender) {

		for (String propertyPath : propertiesToRender){
			if (propertyPath.endsWith("Multiple")){

				CmsProperty<?, ?> cmsProperty = contentObject.getCmsProperty(propertyPath);

				if (cmsProperty.getValueType() == ValueType.Complex){
					contentObject.removeCmsProperty(propertyPath+"[1]");
				}
				else {
					if (((SimpleCmsProperty)cmsProperty).getSimpleTypeValues().size()>1){
						((SimpleCmsProperty)cmsProperty).removeSimpleTypeValue(1);
					}
				}
			}
		}

		return contentService.save(contentObject, false, true, null);

	}

	private void assertPropertyIsExported(String json, String property){
		Assert.assertTrue(json.contains(property), "JSON export \n"+json + " \n does not contain property "+property);

	}

	@Test
	public void testPartialContentObjectSave() throws Throwable{

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.addSystemNameContainsCriterion("testContentObjectForJAXB*");
		contentObjectCriteria.addPropertyPathWhoseValueWillBePreLoaded("accessibility");

		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		logger.debug("Processing {} contentObjects", cmsOutcome.getCount() );

		ImportMode importMode = ImportMode.SAVE_ENTITY;

		for (ContentObject contentObject : cmsOutcome.getResults()){

			String xml = null;
			String json = null;

			try{
				xml = contentObject.xml(prettyPrint, false, "accessibility");

				ContentObject contentObjectUnMarshalledFromXML = importDao.importContentObject(xml, false, false, importMode, null);

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromXML, true);

				json = contentObject.json(prettyPrint, false,"accessibility");
				ContentObject contentObjectUnMarshalledFromJSON = importDao.importContentObject(json, false, false, importMode, null); 

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromJSON, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromXML, contentObjectUnMarshalledFromJSON, true);

				//Now retrieve profile, export to XML and JSON , change profile to an invalid property and try to save
				contentObject.getCmsProperty("profile.title");

				xml = contentObject.xml(prettyPrint, false,"accessibility","profile.title");
				json = contentObject.json(prettyPrint, false,"accessibility","profile.title");

				xml = xml.replaceAll("profile", "profile2");
				json = json.replaceAll("profile", "profile2");

				try{
					TestLogPolicy.setLevelForLogger(Level.FATAL, Deserializer.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ImportBean.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsPropertyImpl.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsRootPropertyImpl.class.getName());
					importDao.importContentObject(xml, false, false, importMode, null);
					TestLogPolicy.setDefaultLevelForLogger(Deserializer.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ImportBean.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ComplexCmsPropertyImpl.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ComplexCmsRootPropertyImpl.class.getName());

				}
				catch(CmsException e){
					Assert.assertTrue(e.getMessage().contains("Invalid child property profile2 parent entity"), 
							"Invalid exception thrown when saving invalid content \n"+xml+" Exception thrown "+
							e.getMessage());
				}

				try{
					TestLogPolicy.setLevelForLogger(Level.FATAL, Deserializer.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ImportBean.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsPropertyImpl.class.getName());
					TestLogPolicy.setLevelForLogger(Level.FATAL, ComplexCmsRootPropertyImpl.class.getName());
					importDao.importContentObject(json, false, false, importMode, null);
					TestLogPolicy.setDefaultLevelForLogger(Deserializer.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ImportBean.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ComplexCmsPropertyImpl.class.getName());
					TestLogPolicy.setDefaultLevelForLogger(ComplexCmsRootPropertyImpl.class.getName());

				}
				catch(CmsException e){
					Assert.assertTrue(e.getMessage().contains("Invalid child property profile2 parent entity"), "Invalid excpetion thrown when saving invalid content \n"+json
							+" Exception thrown "+	 e.getMessage());
				}


			}
			catch(Throwable e){

				logError(xml, json, e);

				throw e;
			}
		}

	}

	private void logError(String xml, String json, Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append("XML \n");
		try{
			sb.append(TestUtils.prettyPrintXml(xml));
		}
		catch(Exception e1){
			sb.append(xml);
		}

		sb.append("\nJSON\n");
		sb.append(json);

		logger.error(sb.toString(), e);
	}

	@Test
	public void testFullContentObjectExport() throws Throwable{

		ContentObject co =  createContentObjectAndPopulateAllProperties(getSystemUser(), "testFullContentObjectExport", false);

		((LongProperty)co.getCmsProperty("statisticTypeMultiple.viewCounter")).setSimpleTypeValue((long)1);
		((LongProperty)co.getCmsProperty("statisticType.viewCounter")).setSimpleTypeValue((long)1);

		co = contentService.save(co, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(co);


		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.addSystemNameEqualsCriterion("testFullContentObjectExport");

		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		for (ContentObject contentObject : cmsOutcome.getResults()){

			String jsonFromApi = null;
			String jsonFromService = null;
			String xmlFromApi = null;
			String xmlFromService = null;

			try{

				/*
				 * Generate XML exports from API and Service API
				 */
				xmlFromApi = contentObject.xml(false);
				xmlFromApi = removeWhitespacesIfNecessary(contentObjectCriteria, xmlFromApi);

				xmlFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, FetchLevel.FULL, 
						CacheRegion.NONE, null, false);
				xmlFromService = removeWhitespacesIfNecessary(contentObjectCriteria, xmlFromService);

				String expectedProperty = "cmsIdentifier=\""+contentObject.getId()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object identifier "+contentObject.getId());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object identifier "+contentObject.getId());

				expectedProperty = "systemName=\""+contentObject.getSystemName()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object system name "+contentObject.getSystemName());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object system name "+contentObject.getSystemName());


				expectedProperty = "contentObjectTypeName=\""+contentObject.getContentObjectType()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object type "+contentObject.getContentObjectType());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object type "+contentObject.getContentObjectType());

				expectedProperty = "url=\""+contentObject.getResourceApiURL(ResourceRepresentationType.XML,false,contentObject.getSystemName()!=null)+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object url "+contentObject.getResourceApiURL(ResourceRepresentationType.XML,false,contentObject.getSystemName()!=null));
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object url "+contentObject.getResourceApiURL(ResourceRepresentationType.XML,false,contentObject.getSystemName()!=null));

				expectedProperty = "<profile";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's profile ");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's profile ");

				expectedProperty = "<title>"+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"</title>";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's profile.title");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's profile.title");

				expectedProperty = "<created>"+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"</created>";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's profile.created");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's profile.created");

				expectedProperty = "<accessibility";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's accessibility");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's accessibility");

				expectedProperty = "<canBeReadBy>"+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"</canBeReadBy>";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's accessibility.canBeReadBy");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's accessibility.canBeReadBy");

				expectedProperty = "<owner";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object owner ");
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object owner ");

				expectedProperty = "cmsIdentifier=\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's owner identifier "+contentObject.getOwner().getId());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "externalId=\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's owner external id "+contentObject.getOwner().getExternalId());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				expectedProperty = "label=\""+contentObject.getOwner().getLabel()+"\"";
				Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object's owner external id "+contentObject.getOwner().getLabel());
				Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = "<"+PropertyPath.getLastDescendant(propertyName);
					Assert.assertTrue(xmlFromApi.contains(expectedProperty), "XML export from API does not contain content object property "+propertyName);
					Assert.assertTrue(xmlFromService.contains(expectedProperty), "XML export from Service does not contain content object property "+propertyName);
				}

				/*
				 * Generate JSON exports from API and Service API
				 */
				jsonFromApi = contentObject.json(false);
				jsonFromApi = removeWhitespacesIfNecessary(contentObjectCriteria, jsonFromApi);

				jsonFromService = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL, 
						CacheRegion.NONE, null, false);
				jsonFromService = removeWhitespacesIfNecessary(contentObjectCriteria, jsonFromService);

				expectedProperty = "\"cmsIdentifier\":\""+contentObject.getId()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object identifier "+contentObject.getId());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object identifier "+contentObject.getId());

				expectedProperty = "\"systemName\":\""+contentObject.getSystemName()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object system name "+contentObject.getSystemName());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object system name "+contentObject.getSystemName());

				expectedProperty = "\"contentObjectTypeName\":\""+contentObject.getContentObjectType()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object type "+contentObject.getContentObjectType());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object type "+contentObject.getContentObjectType());

				expectedProperty = "\"url\":\""+contentObject.getResourceApiURL(ResourceRepresentationType.JSON,false,contentObject.getSystemName()!=null)+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object url "+contentObject.getResourceApiURL(ResourceRepresentationType.JSON,false,contentObject.getSystemName()!=null));
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object url "+contentObject.getResourceApiURL(ResourceRepresentationType.JSON,false,contentObject.getSystemName()!=null));

				expectedProperty = "\"profile\":{";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's profile ");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's profile ");

				expectedProperty = "\"title\":\""+StringUtils.deleteWhitespace(((StringProperty)contentObject.getCmsProperty("profile.title")).getSimpleTypeValue())+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's profile.title");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's profile.title");

				expectedProperty = "\"created\":\""+convertCalendarToXMLFormat(((CalendarProperty)contentObject.getCmsProperty("profile.created")).getSimpleTypeValue(), true)+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's profile.created");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's profile.created");

				expectedProperty = "\"accessibility\":{";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's accessibility");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's accessibility");

				expectedProperty = "\"canBeReadBy\":[\""+((StringProperty)contentObject.getCmsProperty("accessibility.canBeReadBy")).getFirstValue()+"\"]";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's accessibility.canBeReadBy");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's accessibility.canBeReadBy");

				expectedProperty = "\"owner\":{";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object owner ");
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object owner ");

				expectedProperty = "\"cmsIdentifier\":\""+contentObject.getOwner().getId()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's owner identifier "+contentObject.getOwner().getId());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's owner identifier "+contentObject.getOwner().getId());

				expectedProperty = "\"externalId\":\""+contentObject.getOwner().getExternalId()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's owner external id "+contentObject.getOwner().getExternalId());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's owner external id "+contentObject.getOwner().getExternalId());

				expectedProperty = "\"label\":\""+contentObject.getOwner().getLabel()+"\"";
				Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object's owner external id "+contentObject.getOwner().getLabel());
				Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object's owner external id "+contentObject.getOwner().getLabel());

				for (CmsPropertyPath cmsPropertyPath: CmsPropertyPath.values()){
					final String propertyName = cmsPropertyPath.getPeriodDelimitedPath();
					expectedProperty = PropertyPath.getLastDescendant(propertyName);
					Assert.assertTrue(jsonFromApi.contains(expectedProperty), "JSON export from API does not contain content object property "+propertyName);
					Assert.assertTrue(jsonFromService.contains(expectedProperty), "JSON export from Service does not contain content object property "+propertyName);
				}


			}
			catch(Throwable e){

				StringBuilder sb = new StringBuilder();
				sb.append("JSON From API \n");
				sb.append(jsonFromApi);

				sb.append("\nJSON From Service \n");
				sb.append(jsonFromService);

				sb.append("\nXML From API \n");
				sb.append(xmlFromApi);

				sb.append("\nXML From Service \n");
				sb.append(xmlFromService);

				logger.error(sb.toString(), e);

				throw e;
			}


		}

	}

	@Test  
	public void testFullContentObjectJAXBMarshallingUnMarshalling() throws Throwable {

		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		contentObjectCriteria.doNotCacheResults();
		contentObjectCriteria.addSystemNameContainsCriterion("testContentObjectForJAXB*");
		contentObjectCriteria.getRenderProperties().renderAllContentObjectProperties(true);

		CmsOutcome<ContentObject> cmsOutcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);

		logger.debug("Processing {} contentObjects", cmsOutcome.getCount() );

		ImportMode importMode = ImportMode.DO_NOT_SAVE;

		long start = System.currentTimeMillis();

		for (ContentObject contentObject : cmsOutcome.getResults()){

			String xml = null;
			String json = null;

			try{
				start = System.currentTimeMillis();
				xml = contentObject.xml(prettyPrint, true);

				logTimeElapsed("Export ContentObject XML using xml() method in {}", start);

				start = System.currentTimeMillis();
				ContentObject contentObjectUnMarshalledFromXML = importDao.importContentObject(xml, false, false, importMode, null);
				logTimeElapsed("Import ContentObject XML in {}, ImportMode {}, ", start, importMode.toString());

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromXML, true);

				start = System.currentTimeMillis();
				json = contentObject.json(prettyPrint, true);

				logTimeElapsed("Export ContentObject JSON using json() method in {}", start);

				start = System.currentTimeMillis();
				ContentObject contentObjectUnMarshalledFromJSON = importDao.importContentObject(json, false, false, importMode, null); 
				logTimeElapsed("Import ContentObject JSON in {}, ImportMode {}, ", start, importMode.toString());

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromJSON, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromXML, contentObjectUnMarshalledFromJSON, true);

				//Now create XML and JSON from Service and compare each other
				start = System.currentTimeMillis();
				json = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.JSON, FetchLevel.FULL, 
						CacheRegion.NONE, null, true);

				logTimeElapsed("Export ContentObject JSON using service in {}", start);

				start = System.currentTimeMillis();
				ContentObject contentObjectUnMarshalledFromJSONService = importDao.importContentObject(json, false, false, importMode, null); 
				logTimeElapsed("Import ContentObject JSON in {}, ImportMode {}, ", start, importMode.toString());

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromJSONService, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromJSON, contentObjectUnMarshalledFromJSONService, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromXML, contentObjectUnMarshalledFromJSONService, true);

				start = System.currentTimeMillis();
				xml = contentService.getContentObject(contentObject.getId(), ResourceRepresentationType.XML, FetchLevel.FULL, 
						CacheRegion.NONE, null, true);

				logTimeElapsed("Export ContentObject XML using service in {}", start);

				start = System.currentTimeMillis();
				ContentObject contentObjectUnMarshalledFromXMLService = importDao.importContentObject(xml, false, false, importMode, null); 
				logTimeElapsed("Import ContentObject XML in {}, ImportMode {}, ", start, importMode.toString());

				repositoryContentValidator.compareContentObjects(contentObject, contentObjectUnMarshalledFromXMLService, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromJSON, contentObjectUnMarshalledFromXMLService, true);
				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromXML, contentObjectUnMarshalledFromXMLService, true);

				repositoryContentValidator.compareContentObjects(contentObjectUnMarshalledFromXMLService, contentObjectUnMarshalledFromJSONService, true);


			}
			catch(Throwable e){
				logger.error("Created XML :\n {}", xml);
				logger.error("Second JSON :\n{} ", json);
				throw e;
			}
		}
	}

	@Override
	protected void postSetup() throws Exception {
		super.postSetup();

		//Create content objects for test
		RepositoryUser systemUser = getSystemUser();
		Taxonomy subjectTaxonomy = getSubjectTaxonomy();

		//Create Topics
		Topic topic = JAXBTestUtils.createTopic("firstSubject", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		topic.setOwner(systemUser);
		topic.setTaxonomy(subjectTaxonomy);

		Topic childTopic1 = JAXBTestUtils.createTopic("secondSubject", 
				cmsRepositoryEntityFactory.newTopic(),
				cmsRepositoryEntityFactory.newRepositoryUser());
		childTopic1.setOwner(topic.getOwner());
		childTopic1.setTaxonomy(subjectTaxonomy);

		topic.addChild(childTopic1);

		topic = topicService.save(topic);
		addEntityToBeDeletedAfterTestIsFinished(topic);

		//Create one contentObject to be used as a value to a content object property
		ContentObject contentObjectForContentObjectPropertyValue = createContentObjectAndPopulateAllProperties(systemUser, "valueForContentObjectProperty", false);
		contentObjectForContentObjectPropertyValue = contentService.save(contentObjectForContentObjectPropertyValue, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObjectForContentObjectPropertyValue);

		ContentObject contentObject = createContentObject(systemUser, topic, childTopic1,"1");

		contentObject = contentService.save(contentObject, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject);

		ContentObject contentObject2 = createContentObject(systemUser, topic, childTopic1,"2");

		//Reference one another

		((ObjectReferenceProperty)contentObject2.getCmsProperty("profile.hasPart")).addSimpleTypeValue(contentObject);
		((ObjectReferenceProperty)contentObject2.getCmsProperty("profile.references")).addSimpleTypeValue(contentObject);

		contentObject2 = contentService.save(contentObject2, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject2);


		//Create a content object to be used in test testMultiplePropertiesExportedAsArrayInJSON
		ContentObject contentObject3 = createContentObject(systemUser, topic, childTopic1,"testMultiplePropertiesExportedAsArrayInJSON");

		contentObject3 = contentService.save(contentObject3, false, true, null);
		addEntityToBeDeletedAfterTestIsFinished(contentObject3);



	}

	private ContentObject createContentObject(RepositoryUser systemUser,
			Topic topic, Topic childTopic1, String systemName) throws IOException {

		ContentObject contentObject = createContentObjectAndPopulateAllProperties(systemUser, "testContentObjectForJAXB"+systemName, false);

		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue("Content Object : for JAXB test");

		((TopicReferenceProperty)contentObject.getCmsProperty("testTopic")).addSimpleTypeValue(childTopic1);

		ComplexCmsProperty<?, ?> statisticTypeProperty = (ComplexCmsProperty<?, ?>) contentObject.getCmsProperty("statisticType");
		((LongProperty)statisticTypeProperty.getChildProperty("viewCounter")).addSimpleTypeValue((long)4);

		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(topic);
		((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject")).addSimpleTypeValue(childTopic1);

		return contentObject;
	}

}
