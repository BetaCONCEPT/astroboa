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
package org.betaconceptframework.astroboa.test.engine.definition;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.DoublePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LongPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.security.AstroboaPasswordEncryptor;
import org.betaconceptframework.astroboa.test.engine.AbstractRepositoryTest;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentDefinitionConfigurationTest  extends AbstractRepositoryTest{

	//@Test
	public void testIndependentContentTypes(){
	
		for (String typeName : getIndependentContentTypes()){

			ContentObjectTypeDefinition independentDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(typeName, ResourceRepresentationType.DEFINITION_INSTANCE,false);
			
			Assert.assertFalse(independentDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type "+typeName+ " faulty specifies  that the schema of the type extends contentObjectType");

			checkPropertyExistsAndIsOfType(independentDefinition, "profile", ComplexCmsPropertyDefinition.class);
			checkPropertyExistsAndIsOfType(independentDefinition, "accessibility", ComplexCmsPropertyDefinition.class);
			checkPropertyExistsAndIsOfType(independentDefinition, "resourceRepresentationTemplateObjectReference", ObjectReferencePropertyDefinition.class);
			checkPropertyExistsAndIsOfType(independentDefinition, "resourceRepresentationTemplateName", TopicReferencePropertyDefinition.class);
			
			//These properties  are assumed to be provided in all independent types
			//Normal properties
			checkPropertyExistsAndIsOfType(independentDefinition, "simpleString", StringPropertyDefinition.class);
			checkPropertyExistsAndIsOfType(independentDefinition, "simpleStringFromAttribute", StringPropertyDefinition.class);
			checkPropertyExistsAndIsOfType(independentDefinition, "mandatorySimpleStringFromAttribute", StringPropertyDefinition.class);
			
			//Properties which are defined using 'attribute' tag
			Assert.assertTrue(independentDefinition.hasCmsPropertyDefinition("simpleStringFromAttribute"), "Property simpleStringFromAttribute is found in type "+typeName);
			Assert.assertTrue(independentDefinition.hasCmsPropertyDefinition("mandatorySimpleStringFromAttribute"), "Property simpleStringFromAttribute is found in type "+typeName);
			
			CmsPropertyDefinition simpleStringDefinition = independentDefinition.getCmsPropertyDefinition("simpleStringFromAttribute");
			Assert.assertFalse(simpleStringDefinition.isMandatory(), "Property "+simpleStringDefinition.getFullPath()+" is mandatory");
			Assert.assertFalse(simpleStringDefinition.isMultiple(), "Property "+simpleStringDefinition.getFullPath()+" is multiple");

			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(simpleStringDefinition, "simpleStringFromAttribute", "en", 
					"String <b>Property</b> defined by Attribute", "String Property defined by Attribute");

			simpleStringDefinition = independentDefinition.getCmsPropertyDefinition("mandatorySimpleStringFromAttribute");
			Assert.assertTrue(simpleStringDefinition.isMandatory(), "Property "+simpleStringDefinition.getFullPath()+" is optional");
			Assert.assertFalse(simpleStringDefinition.isMultiple(), "Property "+simpleStringDefinition.getFullPath()+" is multiple");
			
			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(simpleStringDefinition, "simpleStringFromAttribute", "en", 
					"mandatorySimpleStringFromAttribute", "mandatorySimpleStringFromAttribute");
			
			
			if (StringUtils.equals(typeName, EXTENDED_INDEPENDENT_CONTENT_TYPE_NAME) || 
					StringUtils.equals(typeName, DIRECT_EXTENDED_INDEPENDENT_CONTENT_TYPE_NAME)){
				Assert.assertTrue(independentDefinition.isTypeOf("independentBaseType"), "Independent type "+ typeName +" does not extend super type independentBaseType");
			}
		}
	}
	
	
	@Test
	public void testExtendedContentTypes()
	{
		ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		Assert.assertFalse(testDefinition.hasCmsPropertyDefinition("simpleExtendedString"), "Property simpleExtendedString is found in type "+TEST_CONTENT_TYPE);		
		Assert.assertTrue(testDefinition.isTypeOf("testType"), "Test content type does not have super type testType");
		Assert.assertTrue(testDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type "+TEST_CONTENT_TYPE+ " faulty specifies  that the schema of the type does not extend contentObjectType");
		
		ContentObjectTypeDefinition extendedTestDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(EXTENDED_TEST_CONTENT_TYPE, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		checkPropertyExistsAndIsOfType(extendedTestDefinition, "simpleExtendedString", StringPropertyDefinition.class);
		
		Assert.assertNull(definitionService.getCmsDefinition("testType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Base Complex Type testType was loaded");
		Assert.assertNull(definitionService.getCmsDefinition("extendedTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Base Complex Type extendedTestType was loaded");
		
		Assert.assertTrue(extendedTestDefinition.isTypeOf("testType"), "Extended Test Type does not have super type testType");
		Assert.assertTrue(extendedTestDefinition.isTypeOf("extendedTestType"), "Extended Test Type does not have super type extendedTestType");

		Assert.assertTrue(extendedTestDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type "+EXTENDED_TEST_CONTENT_TYPE+ " faulty specifies  that the schema of the type does not extend contentObjectType");

		ContentObjectTypeDefinition directExtendedTestDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(DIRECT_EXTENDED_TEST_CONTENT_TYPE, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		checkPropertyExistsAndIsOfType(directExtendedTestDefinition, "simpleExtendedString", StringPropertyDefinition.class);
		
		Assert.assertTrue(directExtendedTestDefinition.isTypeOf("testType"), "Extended Test Type does not have super type testType");
		Assert.assertTrue(directExtendedTestDefinition.isTypeOf("extendedTestType"), "Extended Test Type does not have super type extendedTestType");

		Assert.assertTrue(directExtendedTestDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type "+DIRECT_EXTENDED_TEST_CONTENT_TYPE+ " faulty specifies  that the schema of the type does not extend contentObjectType");

		ContentObjectTypeDefinition personDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition("personObject", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		checkPropertyExistsAndIsOfType(personDefinition, "name.familyName", StringPropertyDefinition.class);
		checkPropertyExistsAndIsOfType(personDefinition, "thumbnail", BinaryPropertyDefinition.class);
		
		Assert.assertNull(definitionService.getCmsDefinition("personType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Base Complex Type personType was loaded");
		
		Assert.assertTrue(personDefinition.isTypeOf("personType"), "PersonObject Type does not have super type personType");

		Assert.assertTrue(personDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type personType faulty specifies  that the schema of the type does not extend contentObjectType");


		ContentObjectTypeDefinition organizationDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition("organizationObject", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		checkPropertyExistsAndIsOfType(organizationDefinition, "about", StringPropertyDefinition.class);
		checkPropertyExistsAndIsOfType(organizationDefinition, "thumbnail", BinaryPropertyDefinition.class);
		
		Assert.assertNull(definitionService.getCmsDefinition("organizationType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Base Complex Type organizationType was loaded");
		
		Assert.assertTrue(organizationDefinition.isTypeOf("organizationType"), "OrganizationObject Type does not have super type organizationType");
		
		Assert.assertTrue(organizationDefinition.schemaExtendsBaseObjectTypeDefinition(), "Definition for type organizationType faulty specifies  that the schema of the type does not extend contentObjectType");

	}
	
	@Test
	public void testLocalizableDescription()
	{

		for (String testContentType : getTestContentTypes())
		{
			ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(testContentType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);


			if (TEST_CONTENT_TYPE.equals(testContentType))
			{
				assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition, testContentType, "en", "This type contains all properties used in tests" , "Test type");
				assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition, testContentType, "el", "Test <b>type</b>" , "Test <b>type</b>");
			}
			else if (EXTENDED_TEST_CONTENT_TYPE.equals(testContentType))
			{
				assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition, testContentType, "en", "This type represents an extension of extendedTestType" , "This type represents an extension of extendedTestType");
				assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition, testContentType, "el", "Extended Test" , "Extended Test");
			}
			
			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("simpleString"), "simpleString", "en", "Simple <b>String</b> property","String");
			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("stringEnum"), "stringEnum", "en", "String Enumeration", "String Enumeration");
			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("longEnum"), "longEnum", "en", "Long Enumeration", "Long Enumeration");

			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("comment"), "comment", "en", "Multiple comment property in English", "comment in english");
			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("comment"), "comment", "el", "Multiple comment property in Greek", "comment in greek");

			assertDescriptionAndDisplayNameExistsForPropertyAndLocale(testDefinition.getCmsPropertyDefinition("commentSingle"), "commentSingle", "en", "This property describes a moderated and ranked user comment", "Moderated And Ranked User Comment");
		}



	}

	private void assertDescriptionAndDisplayNameExistsForPropertyAndLocale(LocalizableCmsDefinition definition, String property, String locale, String expectedDescription, String expectedDisplayName)
	{
		String fullPath = (definition instanceof CmsPropertyDefinition ? ((CmsPropertyDefinition)definition).getFullPath() : definition.getName());
		
		Assert.assertNotNull(definition.getDescription(), "'"+fullPath+"' does not have a description");
		Assert.assertTrue(definition.getDescription().hasLocalizedLabels(), "'"+fullPath+"' has a description but has no localized labels");
		Assert.assertEquals(definition.getDescription().getLocalizedLabelForLocale(locale),expectedDescription, "Invalid description for "+fullPath+" for locale '"+locale+"'");

		Assert.assertNotNull(definition.getDisplayName(), "'"+property+"' does not have a display name");
		Assert.assertTrue(definition.getDisplayName().hasLocalizedLabels(), "'"+fullPath+"' does not have a display name at all");
		Assert.assertEquals(definition.getDisplayName().getLocalizedLabelForLocale(locale),expectedDisplayName, "Invalid display name for "+fullPath+" for locale '"+locale+"'");
	}

	@Test
	public void testPasswordPropertyDefinitions(){

		for (String testContentType : getTestContentTypes())
		{
			ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(testContentType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);

			checkPropertyExistsAndIsOfType(testDefinition, "password", StringPropertyDefinition.class);

			CmsPropertyDefinition passwordDefinition = testDefinition.getCmsPropertyDefinition("password");

		Assert.assertTrue(((StringPropertyDefinition)passwordDefinition).isPasswordType(), 
		" Property password's definition is not marked as password type");

		Assert.assertNotNull(((StringPropertyDefinition)passwordDefinition).getPasswordEncryptor(), 
		" Property password's definition does not contain valid password encryptor");

		Assert.assertEquals(((StringPropertyDefinition)passwordDefinition).getPasswordEncryptor().getClass(), AstroboaPasswordEncryptor.class, 
		" Property password's definition does not contain valid password encryptor");


		checkPropertyExistsAndIsOfType(testDefinition, "passwordWithExternalPasswordEncryptor", StringPropertyDefinition.class);

		CmsPropertyDefinition passwordWithExternalPasswordEncryptorDefinition = testDefinition.getCmsPropertyDefinition("password");

		Assert.assertTrue(((StringPropertyDefinition)passwordWithExternalPasswordEncryptorDefinition).isPasswordType(), 
		" Property passwordWithExternalPasswordEncryptor's definition is not marked as password type");

		Assert.assertNotNull(((StringPropertyDefinition)passwordWithExternalPasswordEncryptorDefinition).getPasswordEncryptor(), 
		" Property passwordWithExternalPasswordEncryptor's definition does not contain valid password encryptor");

		Assert.assertEquals(((StringPropertyDefinition)passwordWithExternalPasswordEncryptorDefinition).getPasswordEncryptor().getClass(), 
				AstroboaPasswordEncryptor.class, 
		" Property passwordWithExternalPasswordEncryptor's definition does not contain valid password encryptor");
		
		}
	}

	@Test
	public void testManagedAndUnmanagedBinaryPropertyDefinitions(){

		for (String testContentType : getTestContentTypes())
		{
			ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(testContentType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);


		checkPropertyExistsAndIsOfType(testDefinition, "thumbnail", BinaryPropertyDefinition.class);

		CmsPropertyDefinition thumbnailDefinition = testDefinition.getCmsPropertyDefinition("thumbnail");

		Assert.assertFalse(((BinaryPropertyDefinition)thumbnailDefinition).isBinaryChannelUnmanaged(), " Property thumbnail's definition denotes that thumbnail is unmanaged but it should not be");

		checkPropertyExistsAndIsOfType(testDefinition, "unmanagedThumbnail", BinaryPropertyDefinition.class);

		CmsPropertyDefinition unmanagedThumbnailDefinition = testDefinition.getCmsPropertyDefinition("unmanagedThumbnail");

		Assert.assertTrue(((BinaryPropertyDefinition)unmanagedThumbnailDefinition).isBinaryChannelUnmanaged(), " Property unmanagedThumbnail's definition denotes that unmanagedThumbnail is not unmanaged but it should be");
		}
	}
	@Test
	public void testBuiltInTypesAreProperlyLoaded(){

		checkBuiltInContentTypeExists(CmsConstants.PORTAL_CONTENT_OBJECT_TYPE);
		checkBuiltInContentTypeExists(CmsConstants.PORTAL_SECTION_CONTENT_OBJECT_TYPE);
		checkBuiltInContentTypeExists(CmsConstants.QUERY_CONTENT_OBJECT_TYPE);
		checkBuiltInContentTypeExists(TEST_CONTENT_TYPE);

		checkBuiltInComplexTypeTypeExists("multilingualStringPropertyType");
		checkBuiltInComplexTypeTypeExists("administrativeMetadataType");
		checkBuiltInComplexTypeTypeExists("webPublicationType");
		checkBuiltInComplexTypeTypeExists("workflowType");
		checkBuiltInComplexTypeTypeExists("accessibilityType");
		checkBuiltInComplexTypeTypeExists("statisticType");

		checkPropertyDefintionExists("test.profile");
		checkPropertyDefintionExists("test.profile.title");
		checkPropertyDefintionExists("test.profile.hasPart");
		checkPropertyDefintionExists("test.accessibility");
		checkPropertyDefintionExists("test.stringEnum");
		checkPropertyDefintionExists("test.webPublication");
		checkPropertyDefintionExists("test.testTopic");
		checkPropertyDefintionExists("test.thumbnail");
	}

	@Test
	public void testRestrictions(){

		for (String testContentType : getTestContentTypes()){
			ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(testContentType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);

			checkPropertyExistsAndIsOfType(testDefinition, "stringFixedLengthConstrained", StringPropertyDefinition.class);
			CmsPropertyDefinition stringFixedLengthConstrainedDefinition = testDefinition.getCmsPropertyDefinition("stringFixedLengthConstrained");
			Assert.assertEquals(stringFixedLengthConstrainedDefinition.getValueType(), ValueType.String, " stringFixedLengthConstrained Definition does not have value type STRING");
			Assert.assertTrue( (stringFixedLengthConstrainedDefinition instanceof StringPropertyDefinition ), "stringFixedLengthConstrained Definition has value type STRING but is not of type StringPropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((StringPropertyDefinition)stringFixedLengthConstrainedDefinition).getValueEnumeration()), "stringFixedLengthConstrained Definition has any value range at all");
			Assert.assertTrue( ((StringPropertyDefinition)stringFixedLengthConstrainedDefinition).isValueValid("12345"), "stringFixedLengthConstrained Definition does not accept value '12345'");
			Assert.assertFalse( ((StringPropertyDefinition)stringFixedLengthConstrainedDefinition).isValueValid("123"), "stringFixedLengthConstrained Definition accepts value '123'");
			Assert.assertFalse( ((StringPropertyDefinition)stringFixedLengthConstrainedDefinition).isValueValid("123456"), "stringFixedLengthConstrained Definition accepts '123456'");

			checkPropertyExistsAndIsOfType(testDefinition, "stringPatternConstrained", StringPropertyDefinition.class);
			CmsPropertyDefinition stringPatternConstrainedDefinition = testDefinition.getCmsPropertyDefinition("stringPatternConstrained");
			Assert.assertEquals(stringPatternConstrainedDefinition.getValueType(), ValueType.String, " stringPatternConstrained Definition does not have value type STRING");
			Assert.assertTrue( (stringPatternConstrainedDefinition instanceof StringPropertyDefinition ), "stringPatternConstrained Definition has value type STRING but is not of type StringPropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((StringPropertyDefinition)stringPatternConstrainedDefinition).getValueEnumeration()), "stringPatternConstrained Definition has any value range at all");
			Assert.assertTrue( ((StringPropertyDefinition)stringPatternConstrainedDefinition).isValueValid("testpattern"), "stringPatternConstrained Definition does not accept value 'testpattern'");
			Assert.assertFalse( ((StringPropertyDefinition)stringPatternConstrainedDefinition).isValueValid("12"), "stringPatternConstrained Definition accepts value '12'");
			Assert.assertFalse( ((StringPropertyDefinition)stringPatternConstrainedDefinition).isValueValid("12asd"), "stringPatternConstrained Definition accepts value '12'");
			Assert.assertFalse( ((StringPropertyDefinition)stringPatternConstrainedDefinition).isValueValid("ADSADS"), "stringPatternConstrained Definition accepts value '12'");
			Assert.assertFalse( ((StringPropertyDefinition)stringPatternConstrainedDefinition).isValueValid("12DADADAD"), "stringPatternConstrained Definition accepts value '12'");

			checkPropertyExistsAndIsOfType(testDefinition, "stringConstrained", StringPropertyDefinition.class);
			CmsPropertyDefinition stringConstrainedDefinition = testDefinition.getCmsPropertyDefinition("stringConstrained");
			Assert.assertEquals(stringConstrainedDefinition.getValueType(), ValueType.String, " stringConstrained Definition does not have value type STRING");
			Assert.assertTrue( (stringConstrainedDefinition instanceof StringPropertyDefinition ), "stringConstrained Definition has value type STRING but is not of type StringPropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((StringPropertyDefinition)stringConstrainedDefinition).getValueEnumeration()), "stringConstrained Definition has any value range at all");
			Assert.assertTrue( ((StringPropertyDefinition)stringConstrainedDefinition).isValueValid("123"), "stringConstrained Definition does not accept value '123'");
			Assert.assertTrue( ((StringPropertyDefinition)stringConstrainedDefinition).isValueValid("1234567890"), "stringConstrained Definition does not accept value '1234567890'");
			Assert.assertTrue( ((StringPropertyDefinition)stringConstrainedDefinition).isValueValid("12345"), "stringConstrained Definition does not accept value '12345'");
			Assert.assertFalse( ((StringPropertyDefinition)stringConstrainedDefinition).isValueValid("12"), "stringConstrained Definition accepts value '12'");
			Assert.assertFalse( ((StringPropertyDefinition)stringConstrainedDefinition).isValueValid("123456789101"), "stringConstrained Definition accepts '123456789101'");

			checkPropertyExistsAndIsOfType(testDefinition, "doubleConstrained", DoublePropertyDefinition.class);
			CmsPropertyDefinition doubleConstrainedDefinition = testDefinition.getCmsPropertyDefinition("doubleConstrained");
			Assert.assertEquals(doubleConstrainedDefinition.getValueType(), ValueType.Double, " doubleConstrained Definition does not have value type DOUBLE");
			Assert.assertTrue( (doubleConstrainedDefinition instanceof DoublePropertyDefinition ), "doubleConstrained Definition has value type DOUBLE but is not of type DoublePropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((DoublePropertyDefinition)doubleConstrainedDefinition).getValueEnumeration()), "doubleConstrained Definition has any value range at all");
			Assert.assertFalse( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(10.0), "doubleConstrained Definition does not have value '10.0'");
			Assert.assertTrue( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(9.9), "doubleConstrained Definition accepts '9.9'");
			Assert.assertTrue( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(3.15), "doubleConstrained Definition does not have value '3.15'");
			Assert.assertFalse( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(3.1), "doubleConstrained Definition accepts '3.1'");
			Assert.assertFalse( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(0.0), "doubleConstrained Definition accepts value '0.0'");
			Assert.assertFalse( ((DoublePropertyDefinition)doubleConstrainedDefinition).isValueValid(11.0), "doubleConstrained Definition accepts value '11.0'");
			
			checkPropertyExistsAndIsOfType(testDefinition, "longConstrained", LongPropertyDefinition.class);
			CmsPropertyDefinition longConstrainedDefinition = testDefinition.getCmsPropertyDefinition("longConstrained");
			Assert.assertEquals(longConstrainedDefinition.getValueType(), ValueType.Long, " longConstrained Definition does not have value type LONG");
			Assert.assertTrue( (longConstrainedDefinition instanceof LongPropertyDefinition ), "longConstrained Definition has value type LONG but is not of type LongPropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((LongPropertyDefinition)longConstrainedDefinition).getValueEnumeration()), "longConstrained Definition has any value range at all");
			Assert.assertFalse( ((LongPropertyDefinition)longConstrainedDefinition).isValueValid((long)4), "longConstrained Definition accepts value '4'");
			Assert.assertTrue( ((LongPropertyDefinition)longConstrainedDefinition).isValueValid((long)7), "longConstrained Definition does not have value '7'");
			Assert.assertTrue( ((LongPropertyDefinition)longConstrainedDefinition).isValueValid((long)5), "longConstrained Definition does not have value '5'");
			Assert.assertFalse( ((LongPropertyDefinition)longConstrainedDefinition).isValueValid((long)3), "longConstrained Definition accepts value '3'");
			Assert.assertFalse( ((LongPropertyDefinition)longConstrainedDefinition).isValueValid((long)8), "longConstrained Definition accepts value '8'");
			   
			checkPropertyExistsAndIsOfType(testDefinition, "integerConstrained", LongPropertyDefinition.class);
			CmsPropertyDefinition integerConstrainedDefinition = testDefinition.getCmsPropertyDefinition("integerConstrained");
			Assert.assertEquals(integerConstrainedDefinition.getValueType(), ValueType.Long, " integerConstrained Definition does not have value type LONG");
			Assert.assertTrue( (integerConstrainedDefinition instanceof LongPropertyDefinition ), "integerConstrained Definition has value type LONG but is not of type LongPropertyDefinition");
			Assert.assertTrue( MapUtils.isEmpty(((LongPropertyDefinition)integerConstrainedDefinition).getValueEnumeration()), "integerConstrained Definition has any value range at all");
			Assert.assertTrue( ((LongPropertyDefinition)integerConstrainedDefinition).isValueValid((long)-100), "integerConstrained Definition does not have value '-100'");
			Assert.assertFalse( ((LongPropertyDefinition)integerConstrainedDefinition).isValueValid((long)300), "integerConstrained Definition does not have value '300'");
			Assert.assertTrue( ((LongPropertyDefinition)integerConstrainedDefinition).isValueValid((long)2), "integerConstrained Definition does not have value '2'");
			Assert.assertFalse( ((LongPropertyDefinition)integerConstrainedDefinition).isValueValid((long)-101), "integerConstrained Definition accepts value '-101'");
			Assert.assertFalse( ((LongPropertyDefinition)integerConstrainedDefinition).isValueValid((long)302), "integerConstrained Definition accepts value '302'");

		}
	}
	
	@Test
	public void testEnumerations(){

		for (String testContentType : getTestContentTypes())
		{
			ContentObjectTypeDefinition testDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(testContentType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);

			
		checkPropertyExistsAndIsOfType(testDefinition, "stringEnum", StringPropertyDefinition.class);

		CmsPropertyDefinition stringEnumDefinition = testDefinition.getCmsPropertyDefinition("stringEnum");
		Assert.assertEquals(stringEnumDefinition.getValueType(), ValueType.String, "StringEnum Definition does not have value type STRING");
		Assert.assertTrue( (stringEnumDefinition instanceof StringPropertyDefinition ), "StringEnum Definition has value type STRING but is not of type StringPropertyDefinition");
		Assert.assertFalse( MapUtils.isEmpty(((StringPropertyDefinition)stringEnumDefinition).getValueEnumeration()), "StringEnum Definition does not have any value range at all");
		Assert.assertTrue( ((StringPropertyDefinition)stringEnumDefinition).isValueValid("RED"), "StringEnum Definition does not have value 'RED'");
		Assert.assertTrue( ((StringPropertyDefinition)stringEnumDefinition).isValueValid("BLUE"), "StringEnum Definition does not have value 'BLUE'");
		Assert.assertTrue( ((StringPropertyDefinition)stringEnumDefinition).isValueValid("GREEN"), "StringEnum Definition does not have value 'GREEN'");
		Assert.assertTrue( ((StringPropertyDefinition)stringEnumDefinition).getDefaultValue().equals("RED"), "StringEnum Definition does not have default value 'RED'");

		checkPropertyExistsAndIsOfType(testDefinition, "longEnum", LongPropertyDefinition.class);
		CmsPropertyDefinition longEnumDefinition = testDefinition.getCmsPropertyDefinition("longEnum");
		Assert.assertEquals(longEnumDefinition.getValueType(), ValueType.Long, " longEnum Definition does not have value type LONG");
		Assert.assertTrue( (longEnumDefinition instanceof LongPropertyDefinition ), "longEnum Definition has value type LONG but is not of type LongPropertyDefinition");
		Assert.assertFalse( MapUtils.isEmpty(((LongPropertyDefinition)longEnumDefinition).getValueEnumeration()), "longEnum Definition does not have any value range at all");
		Assert.assertTrue( ((LongPropertyDefinition)longEnumDefinition).isValueValid((long)0), "longEnum Definition does not have value '0'");
		Assert.assertTrue( ((LongPropertyDefinition)longEnumDefinition).isValueValid((long)1), "longEnum Definition does not have value '1'");
		Assert.assertTrue( ((LongPropertyDefinition)longEnumDefinition).isValueValid((long)3), "longEnum Definition does not have value '3'");
		Assert.assertTrue( ((LongPropertyDefinition)longEnumDefinition).getDefaultValue() == (long)1, "longEnum Definition does not have value '1'");

		checkPropertyExistsAndIsOfType(testDefinition, "doubleEnum", DoublePropertyDefinition.class);
		CmsPropertyDefinition doubleEnumDefinition = testDefinition.getCmsPropertyDefinition("doubleEnum");
		Assert.assertEquals(doubleEnumDefinition.getValueType(), ValueType.Double, " doubleEnum Definition does not have value type DOUBLE");
		Assert.assertTrue( (doubleEnumDefinition instanceof DoublePropertyDefinition ), "doubleEnum Definition has value type DOUBLE but is not of type DoublePropertyDefinition");
		Assert.assertFalse( MapUtils.isEmpty(((DoublePropertyDefinition)doubleEnumDefinition).getValueEnumeration()), "doubleEnum Definition does not have any value range at all");
		Assert.assertTrue( ((DoublePropertyDefinition)doubleEnumDefinition).isValueValid(0.0), "doubleEnum Definition does not have value '0.0'");
		Assert.assertTrue( ((DoublePropertyDefinition)doubleEnumDefinition).isValueValid(1.3), "doubleEnum Definition does not have value '1.3'");
		Assert.assertTrue( ((DoublePropertyDefinition)doubleEnumDefinition).isValueValid(3.4), "doubleEnum Definition does not have value '3.4'");


		ContentObject testContentObject = createContentObjectForType(testContentType, getSystemUser(), "testEnumerationsObject"+testContentType);
		testContentObject = contentService.save(testContentObject, false, true, null);
		markObjectForRemoval(testContentObject);
		
		StringProperty stringProperty = (StringProperty) testContentObject.getCmsProperty("stringEnum");
		stringProperty.addSimpleTypeValue("GREEN");
		stringProperty.addSimpleTypeValue("BLUE");

		try{
			stringProperty.addSimpleTypeValue("PURPLE");

			testContentObject = contentService.save(testContentObject, false, true, null);
			
			Assert.assertTrue(1 == 2, "No exception thrown when saving content object invalid value PURPLE in stringEnum property");
		}
		catch (CmsException e){
			Assert.assertTrue(e.getMessage().startsWith("Provided value PURPLE is not within enumeration"), " Invalid exception when adding an invalid value for stringEnum property "+ e.getMessage() );
			stringProperty.removeSimpleTypeValue(2);
		}

		LongProperty longEnumProperty = (LongProperty) testContentObject.getCmsProperty("longEnum");
		longEnumProperty.addSimpleTypeValue((long)0);
		longEnumProperty.addSimpleTypeValue((long)3);

		try{
			longEnumProperty.addSimpleTypeValue((long)4);

			testContentObject = contentService.save(testContentObject, false, true, null);
			
			Assert.assertTrue(1 == 2, "No exception thrown when saving content object with invalid value 4 in longEnum property");
		}
		catch (CmsException e){
			Assert.assertTrue(e.getMessage().startsWith("Provided value 4 is not within enumeration"), " Invalid exception when adding an invalid value for longEnum property "+ e.getMessage() );
			longEnumProperty.removeSimpleTypeValue(2);
		}

		DoubleProperty doubleProperty = (DoubleProperty) testContentObject.getCmsProperty("doubleEnum");
		doubleProperty.addSimpleTypeValue(0.0);
		doubleProperty.addSimpleTypeValue(1.3);
		doubleProperty.addSimpleTypeValue(3.4);   

		try{
			doubleProperty.addSimpleTypeValue(4.6);

			testContentObject = contentService.save(testContentObject, false, true, null);
			
			Assert.assertTrue(1 == 2, "No exception thrown when saving content object invalid value 4.6 in doubleEnum property");
		}
		catch (CmsException e){
			Assert.assertTrue(e.getMessage().startsWith("Provided value 4.6 is not within enumeration"), " Invalid exception when adding an invalid value for doubleEnum property "+ e.getMessage() );
			doubleProperty.removeSimpleTypeValue(3);
		}
		}
	}

	@Test
	public void testAcceptedContentTypes(){
		
		ObjectReferencePropertyDefinition referenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".referenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".multipleReferenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerReferenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.referenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.multipleReferenceOfAnyContentObjectOfTypeTestType", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);

		ObjectReferencePropertyDefinition referenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".referenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleReferenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".multipleReferenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerReferenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.referenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.multipleReferenceOfAnyContentObjectOfTypeTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);

		ObjectReferencePropertyDefinition referenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".referenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainer.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.referenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		ObjectReferencePropertyDefinition multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(TEST_CONTENT_TYPE+".allPropertyTypeContainerMultiple.multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTest", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		
		List<String> acceptedContentTypes = Arrays.asList("testType");
		List<String> expandedContentTypes = Arrays.asList(TEST_CONTENT_TYPE,DIRECT_EXTENDED_TEST_CONTENT_TYPE,EXTENDED_TEST_CONTENT_TYPE);
		
		checkAcceptedAndExpandedContentTypes(referenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerReferenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestTypeDefinition,acceptedContentTypes,expandedContentTypes);

		acceptedContentTypes = Arrays.asList(TEST_CONTENT_TYPE);
		expandedContentTypes = acceptedContentTypes;

		checkAcceptedAndExpandedContentTypes(referenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleReferenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerReferenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestDefinition,acceptedContentTypes,expandedContentTypes);

		acceptedContentTypes = Arrays.asList(TEST_CONTENT_TYPE,EXTENDED_TEST_CONTENT_TYPE);
		expandedContentTypes = acceptedContentTypes;

		checkAcceptedAndExpandedContentTypes(referenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(allPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);
		checkAcceptedAndExpandedContentTypes(multipleAllPropertyContainerMultipleReferenceOfAnyContentObjectOfTypeTestAndExtendedTestDefinition,acceptedContentTypes,expandedContentTypes);

		
		ObjectReferencePropertyDefinition personJobPositionOrganizationObjectReference = (ObjectReferencePropertyDefinition) definitionService.getCmsDefinition("personObject.organizations.jobPosition.organizationObjectReference", ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint);
		
		acceptedContentTypes = Arrays.asList("organizationType");
		expandedContentTypes = Arrays.asList("organizationObject");
		
		checkAcceptedAndExpandedContentTypes(personJobPositionOrganizationObjectReference,acceptedContentTypes,expandedContentTypes);

	}
	
	private void checkAcceptedAndExpandedContentTypes(ObjectReferencePropertyDefinition contentObjectPropertyDefinition, List<String> acceptedContentTypes, List<String> expandedContentTypes){
		
		Assert.assertEquals(contentObjectPropertyDefinition.getAcceptedContentTypes(), acceptedContentTypes, contentObjectPropertyDefinition.getFullPath());
		
		Assert.assertEquals(contentObjectPropertyDefinition.getExpandedAcceptedContentTypes(), expandedContentTypes,contentObjectPropertyDefinition.getFullPath());
	}

	private void checkPropertyExistsAndIsOfType(ContentObjectTypeDefinition contentTypeDefinition, String propertyPath, Class<? extends CmsPropertyDefinition> definitionType) {
		Assert.assertTrue(contentTypeDefinition.hasCmsPropertyDefinition(propertyPath), "Property "+propertyPath+" is not found in type "+contentTypeDefinition.getName());

		Class<? extends CmsPropertyDefinition> propertyDefinitionType = contentTypeDefinition.getCmsPropertyDefinition(propertyPath).getClass();

		Assert.assertTrue(definitionType.isAssignableFrom(propertyDefinitionType), 
				"Property "+propertyPath+" is of type "+propertyDefinitionType.getName() + " but expexted type "+ definitionType.getName()); 
	}

	private void checkBuiltInContentTypeExists(String contentType) {
		Assert.assertTrue(definitionService.hasContentObjectTypeDefinition(contentType), "Built in content type "+ contentType+ " was not loaded");
	}

	private void checkBuiltInComplexTypeTypeExists(String complexType) {
		Assert.assertNotNull(definitionService.getCmsDefinition(complexType, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Built in complex type "+ complexType+ " was not loaded");
	}

	private void checkPropertyDefintionExists(String fullPropertyPath) {
		Assert.assertNotNull(definitionService.getCmsDefinition(fullPropertyPath, ResourceRepresentationType.DEFINITION_INSTANCE,prettyPrint), "Property"+ fullPropertyPath+ " was not loaded");
	}

}