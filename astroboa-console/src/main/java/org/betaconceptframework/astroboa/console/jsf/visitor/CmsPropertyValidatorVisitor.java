/**
 * Class responsible to validate cms properties of a content object
 * 
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
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
package org.betaconceptframework.astroboa.console.jsf.visitor;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsRootProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.DoublePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LongPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.jsf.edit.SimpleCmsPropertyValueWrapper;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyValidatorVisitor {

	private static final String PATH_DELIMITER = " > ";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private DefinitionService  definitionService;

	private boolean mandatoryPropertiesAreMissing  = false;
	private boolean hasErrors = false;

	private Deque<Map<String, String>> mandatoryErrorMessagesPerPropertyPath = new ArrayDeque<Map<String,String>>();

	private Map<String, List<SimpleCmsPropertyValueWrapper>> simpleCmsPropertyValuesOfPasswordType;
	
	private Map<String, List<SimpleCmsPropertyValueWrapper>> simpleCmsPropertyValuesOfInvalidTempValue;
	
	public CmsPropertyValidatorVisitor(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public void validateContentObjectProperties(ContentObject contentObject, ContentObjectTypeDefinition contentObjectTypeDefinition){

		reset(false);

		if (contentObject == null || contentObject.getComplexCmsRootProperty() == null ||
				contentObjectTypeDefinition == null){
			hasErrors = true;
			logger.error("No contentObject or content object type definition provided. Unable to validate");
			return;
		}

		//Validate system name
		if (StringUtils.isNotBlank(contentObject.getSystemName())){
			if (!CmsConstants.SystemNamePattern.matcher(contentObject.getSystemName()).matches()){
				JSFUtilities.addMessage(null, "content.object.edit.invalid.system.name", new String[]{contentObject.getSystemName()}, FacesMessage.SEVERITY_WARN);
				hasErrors = true;
			}
		}


		validateComplexCmsProperty(contentObject.getComplexCmsRootProperty(), contentObjectTypeDefinition.getPropertyDefinitions(), contentObject.getComplexCmsRootProperty().getAspects());

		validatePropertiesWithTempValue();
	}

	private void validatePropertiesWithTempValue() {
		if (MapUtils.isNotEmpty(simpleCmsPropertyValuesOfInvalidTempValue)){
			for (Entry<String, List<SimpleCmsPropertyValueWrapper>> simpleCmsPropertyValueWrapperEntry : simpleCmsPropertyValuesOfInvalidTempValue.entrySet()){
				
				for (SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper : simpleCmsPropertyValueWrapperEntry.getValue()){
					
					//It may be the case that user has corrected wrong value
					if (simpleCmsPropertyValueWrapper.hasTempValue()){
						
						SimpleCmsProperty simpleCmsProperty = simpleCmsPropertyValueWrapper.getSimpleCmsProperty();
						Object value = simpleCmsPropertyValueWrapper.getValue();

						if (ValueType.Double == simpleCmsProperty.getValueType()){
							validateDoubleProperty(Arrays.asList(value), simpleCmsProperty);
						}
						else if (ValueType.Long == simpleCmsProperty.getValueType()){
							validateLongProperty(Arrays.asList(value), simpleCmsProperty);
						}
						else if (ValueType.String == simpleCmsProperty.getValueType()){
							validateStringProperty(Arrays.asList(value), simpleCmsProperty);
						}
						else{
							logger.warn("Found property {} with temp value of type {}", simpleCmsProperty.getFullPath(), simpleCmsProperty.getValueType());
						}
					}
				}
			}
		}
	}

	/**
	 * @param asList
	 * @param simpleCmsProperty
	 */
	private void validateStringProperty(List<Object> values,SimpleCmsProperty simpleCmsProperty) {
		int valueIndex = -1;
		for (Object value : values){
			++valueIndex;
			if (value != null){
				if (value instanceof String){
					if (StringUtils.isNotBlank((String)value)){
						
						if (simpleCmsProperty.getPropertyDefinition() != null && 
								! ((SimpleCmsPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).isValueValid(value)){
							
							if (((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getPattern() != null){
								addErrorMessage(simpleCmsProperty.getFullPath(), "errors.pattern",
										getLocalizedLabelOfFullPathFromCmsProperty(simpleCmsProperty),
										((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getPattern());
							}
							else{
								String min = ((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getMinLength() == null ? "0" : String.valueOf(((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getMinLength());
								String max = ((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getMaxLength() == null ? "0" :String.valueOf(((StringPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).getMaxLength());

								addErrorMessage(simpleCmsProperty.getFullPath(), "errors.range",
										getLocalizedLabelOfFullPathFromCmsProperty(simpleCmsProperty),
										min,
										max);
							}
						}
					}
				}
			}
			else {
				// There is a case that the property is multi-value and its list of values contains nulls. 
				// This may happen if the user deletes one or more values from the object form by cutting the text instead of using the (X) button to remove the field. 
				// In such a case when the user submits the form (i.e. save the object) then the property will contain an ArrayList one or more null values. 
				// We should identify such case and not raise an error but instead remove the null value from the list.
				if (simpleCmsProperty.getPropertyDefinition().isMultiple()) {
					if (values.size() == 1) { // if there is only one value and is null remove the list itself 
						simpleCmsProperty.removeValues();
					}
					else {
						simpleCmsProperty.removeSimpleTypeValue(valueIndex);
					}
				}
				else {
					addErrorMessage(simpleCmsProperty.getFullPath(), "errors.invalid", 
							getLocalizedLabelOfFullPathFromCmsProperty(simpleCmsProperty));
					
				}
			}
		}
		
	}

	public void reset(boolean clearSimpleCmsPropertyValueOfPasswordType) {
		mandatoryPropertiesAreMissing = false;
		hasErrors = false;
		mandatoryErrorMessagesPerPropertyPath.clear();
		
		if (clearSimpleCmsPropertyValueOfPasswordType){
			
			if (simpleCmsPropertyValuesOfPasswordType == null){
				simpleCmsPropertyValuesOfPasswordType = new HashMap<String, List<SimpleCmsPropertyValueWrapper>>();
			}
			else{
				simpleCmsPropertyValuesOfPasswordType.clear();
			}
		}
	}

	private boolean validateComplexCmsProperty(
			ComplexCmsProperty complexCmsProperty,
			Map<String, CmsPropertyDefinition> childPropertyDefinitions, List<String> aspects) {

		Map<String, List<CmsProperty>> childCmsProperties = complexCmsProperty.getChildProperties();

		mandatoryErrorMessagesPerPropertyPath.push(new HashMap<String, String>());

		//Holds all child cms properties that have been processed
		List<String> childPropertiesValidated = new ArrayList<String>();

		boolean atLeastOnePropertyIsAboutToBeSaved  = false;
		
		boolean foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved = false;

		//validate only if there are any properties
		if (MapUtils.isNotEmpty(childCmsProperties)){

			//Validate each property
			for (Map.Entry<String, List<CmsProperty>> cmsPropertyEntry : childCmsProperties.entrySet()){

				String childCmsPropertyName = cmsPropertyEntry.getKey();
				List<CmsProperty> childCmsPropertyValues = cmsPropertyEntry.getValue();

				childPropertiesValidated.add(childCmsPropertyName);

				//Get its definition
				CmsPropertyDefinition childCmsPropertyDefinition = childPropertyDefinitions.get(childCmsPropertyName);
				if (childCmsPropertyDefinition == null){
					//Check if this is an aspect, if an aspect list is provided
					if (CollectionUtils.isNotEmpty(aspects) && aspects.contains(childCmsPropertyName)){
						//Load aspect definition
						childCmsPropertyDefinition = (CmsPropertyDefinition) definitionService.getCmsDefinition(childCmsPropertyName, ResourceRepresentationType.DEFINITION_INSTANCE, false);

						//Still is null
						if (childCmsPropertyDefinition == null){
							logger.error("Aspect "+ childCmsPropertyName + " exists in content object but its definition could not be found");
							JSFUtilities.addMessage(null, "Σφάλμα κατά την αποθήκευση των στοιχείων του "+ getLocalizedLabelOfFullPathFromCmsProperty(complexCmsProperty), 
									FacesMessage.SEVERITY_WARN);
							hasErrors = true;
							break;
						}

					}
					else if (CollectionUtils.isEmpty(childCmsPropertyValues)) {
						//Child Property definition is missing and aspect list does not contain its name
						//Probably an aspect which was removed
						//Do not validate
						continue;
					}
					else{
						logger.error("Property "+ childCmsPropertyName + " exists in content object but its definition could not be found in content object type definition and it is not an aspect");
						JSFUtilities.addMessage(null, "Σφάλμα κατά την αποθήκευση των στοιχείων του "+ getLocalizedLabelOfFullPathFromCmsProperty(complexCmsProperty), 
								FacesMessage.SEVERITY_WARN);
						hasErrors = true;
						break;
					}

				}

				//Check if property is obsolete
				if (childCmsPropertyDefinition.isObsolete()){
					logger.warn("Found values for obsolete definition for property "+ childCmsPropertyDefinition.getFullPath());
					continue;
				}
				else{
					//Check if it is mandatory but yet no values exist
					if (childCmsPropertyDefinition.isMandatory() && CollectionUtils.isEmpty(childCmsPropertyValues)){
						addErrorMessageForRequiredPropertyUsingParentPropertyAndChildDefinition(complexCmsProperty, childCmsPropertyDefinition);
						continue;
					}
					else{

						//Validation for Complex Cms property
						if (ValueType.Complex == childCmsPropertyDefinition.getValueType()){

							//Check if it is single value but more than one values exist
							if (!childCmsPropertyDefinition.isMultiple() && 
									(childCmsPropertyValues != null && childCmsPropertyValues.size() > 1)){
								addErrorMessageForPropertyUsingParentPropertyAndChildDefinition(complexCmsProperty, childCmsPropertyDefinition, "cms.property.single.value");
								continue;
							}
							else{
								for (CmsProperty childCmsProperty : childCmsPropertyValues){
									atLeastOnePropertyIsAboutToBeSaved = 
										validateComplexCmsProperty((ComplexCmsProperty)childCmsProperty,
												((ComplexCmsPropertyDefinition)childCmsPropertyDefinition).getChildCmsPropertyDefinitions(), aspects) || atLeastOnePropertyIsAboutToBeSaved;
								}
							}
						}
						else if (ValueType.ContentType != childCmsPropertyDefinition.getValueType()){
							//Validate Simple Cms Property
							for (CmsProperty childCmsProperty : childCmsPropertyValues){
								
								SimpleCmsProperty simpleProperty = (SimpleCmsProperty)childCmsProperty;
								
								atLeastOnePropertyIsAboutToBeSaved =  
									validateSimpleCmsProperty(simpleProperty, ((SimpleCmsPropertyDefinition)childCmsPropertyDefinition)) || atLeastOnePropertyIsAboutToBeSaved;
								
								if (!foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved && simpleProperty.hasValues()){
									if (childCmsProperty.getValueType() != ValueType.Boolean){
										foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved = true;
									}
									else{
										if (childCmsProperty.getPropertyDefinition().isMultiple() && simpleProperty.getSimpleTypeValues().size()>1){
											foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved = true;
										}
										else if (//Property is a boolean property and its value is not the same with the default value or
												(((BooleanProperty)childCmsProperty).getPropertyDefinition() != null &&
												((BooleanProperty)childCmsProperty).getPropertyDefinition().isSetDefaultValue() && 
												((BooleanProperty)childCmsProperty).getPropertyDefinition().getDefaultValue() != ((BooleanProperty)childCmsProperty).getFirstValue() ) ||  
												//no default value is set and its value is TRUE
												(((BooleanProperty)childCmsProperty).getFirstValue())){
											foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved = true;
										}
									}
								}
							}
						}
					}
				}
			}
		}


		//Check all child properties that are mandatory but are not processed at all
		if (atLeastOnePropertyIsAboutToBeSaved){
			
			if (! foundAtLeastOneNonBooleanPropertyWhoseValueMustBeSaved && ! (complexCmsProperty instanceof ComplexCmsRootProperty)) {
				atLeastOnePropertyIsAboutToBeSaved = false;
				if (! mandatoryErrorMessagesPerPropertyPath.isEmpty() && mandatoryErrorMessagesPerPropertyPath.peek() != null){
					mandatoryErrorMessagesPerPropertyPath.peek().clear();
				}
			}
			else{

				for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions.values()){
					if (childPropertyDefinition.isMandatory() && !childPropertiesValidated.contains(childPropertyDefinition.getName())){

						if (complexCmsProperty.getId() == null){
							//New complex cms property and one of its child does not even exists
							addErrorMessageForRequiredPropertyUsingParentPropertyAndChildDefinition(complexCmsProperty, childPropertyDefinition);
						}

					}
				}
			}
		}

		//1. Mandatory Error Messages exist and parent property is mandatory
		//2. Mandatory Error Messages exist and parent property is an aspect
		//3. Mandatory Error Messages exist and parent property has at least one child property with value which will be saved
		Map<String, String> currentMandatoryErrorMessagesPerPropertyPath = mandatoryErrorMessagesPerPropertyPath.pollFirst();

		//Mandatory Error Messages should be displayed in the following cases:
		//1. There is at least one child property which must be saved
		//2. Complex Cms Property is Mandatory and at the same time it is a root property or an aspect 
		if (MapUtils.isNotEmpty(currentMandatoryErrorMessagesPerPropertyPath)){

			//Mandatory errors exist.
			if (atLeastOnePropertyIsAboutToBeSaved || 
					( complexCmsProperty.getPropertyDefinition().isMandatory() &&
							 ( 
								( complexCmsProperty.getParentProperty() != null && 
								  complexCmsProperty.getParentProperty() instanceof ComplexCmsRootProperty )
							 ) 
					) 
				){
				//Print messages
				for (Map.Entry<String, String> errorMessages : currentMandatoryErrorMessagesPerPropertyPath.entrySet()){
					JSFUtilities.addMessage(errorMessages.getKey(), errorMessages.getValue(), FacesMessage.SEVERITY_WARN);
				}

				mandatoryPropertiesAreMissing = true;
				atLeastOnePropertyIsAboutToBeSaved = true;
				currentMandatoryErrorMessagesPerPropertyPath = null;
			}
			else{
				if (complexCmsProperty.getParentProperty() == null && 
						  ! (complexCmsProperty.getParentProperty() instanceof ComplexCmsRootProperty )
					)
					{
					//Copy error messages to the first on the queue
					if (! mandatoryErrorMessagesPerPropertyPath.isEmpty()){
						mandatoryErrorMessagesPerPropertyPath.peekFirst().putAll(currentMandatoryErrorMessagesPerPropertyPath);
					}
						
				}
			}
		}

		return atLeastOnePropertyIsAboutToBeSaved;
	}

	private void addErrorMessageForPropertyUsingParentPropertyAndChildDefinition(
			ComplexCmsProperty parentComplexCmsProperty,
			CmsPropertyDefinition childCmsPropertyDefinition, String errorMessageKey) {

		String childPropertyFullPath = PropertyPath.createFullPropertyPath(parentComplexCmsProperty.getFullPath(), childCmsPropertyDefinition.getName());

		String childLocalizedLabel = PropertyPath.createFullPropertyPathWithDelimiter(getLocalizedLabelOfFullPathFromCmsProperty(parentComplexCmsProperty), 
				childCmsPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()), PATH_DELIMITER);

		addErrorMessage(childPropertyFullPath, errorMessageKey, childLocalizedLabel);
	}

	private void addErrorMessageForRequiredPropertyUsingParentPropertyAndChildDefinition(
			ComplexCmsProperty parentComplexCmsProperty,
			CmsPropertyDefinition childCmsPropertyDefinition) {

		String childPropertyFullPath = PropertyPath.createFullPropertyPath(parentComplexCmsProperty.getFullPath(), childCmsPropertyDefinition.getName());

		String childLocalizedLabel = PropertyPath.createFullPropertyPathWithDelimiter(getLocalizedLabelOfFullPathFromCmsProperty(parentComplexCmsProperty), 
				childCmsPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()), PATH_DELIMITER);

		addErrorMessageForRequiredField(childPropertyFullPath, childLocalizedLabel);
	}

	private boolean validateSimpleCmsProperty(SimpleCmsProperty simpleCmsProperty,
			SimpleCmsPropertyDefinition simpleCmsPropertyDefinition) {

		boolean valueExists = hasValue(simpleCmsPropertyDefinition, simpleCmsProperty);
		if (! valueExists // No value found
				&& simpleCmsPropertyDefinition.isMandatory() //Is Mandatory
				&& simpleCmsPropertyDefinition.getDefaultValue() == null) 	{ //No default value is defined
			addErrorMessageForRequiredField(simpleCmsProperty.getFullPath(), 
					getLocalizedLabelOfFullPathFromCmsProperty(simpleCmsProperty));

			return false;
		}
		else if (valueExists){

			
			//If this property is o password type first we have to verify provided password
			if (simpleCmsPropertyDefinition != null && 
					simpleCmsPropertyDefinition instanceof StringPropertyDefinition && 
			 		((StringPropertyDefinition)simpleCmsPropertyDefinition).isPasswordType() && 
			 		simpleCmsPropertyValuesOfPasswordType.containsKey(simpleCmsPropertyDefinition.getFullPath())){
				
				for (SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper : simpleCmsPropertyValuesOfPasswordType.get(simpleCmsPropertyDefinition.getFullPath())){
					
					List<String> errorMessages = simpleCmsPropertyValueWrapper.verifyPassword();
					
					if (CollectionUtils.isNotEmpty(errorMessages)){
						for (String errorMessage : errorMessages){
							addFinalErrorMessage(simpleCmsProperty.getFullPath(), errorMessage);
						}
						
					}
					
					simpleCmsPropertyValueWrapper.clearPasswords();
				}
				
			}
			
			//Value exists. Get all values and validate According to type
			final List<Object> values = new ArrayList<Object>();

			if (simpleCmsPropertyDefinition.isMultiple())
				values.addAll(simpleCmsProperty.getSimpleTypeValues());
			else{
				Object simpleTypeValue = simpleCmsProperty.getSimpleTypeValue();
				if (simpleTypeValue != null)
					values.add(simpleTypeValue);
			}

			switch (simpleCmsPropertyDefinition.getValueType()) {
			case Date:
				//No validation necessary since in UI we make sure that
				//only Date objects are set. See SimpleCmsProeprtyValueWrapper.setVale()
				break;
			case Double:
				validateDoubleProperty(values, simpleCmsProperty);
				break;
			case Long:
				validateLongProperty(values, simpleCmsProperty);
				break;
			case Binary:
				validateBinaryProperty(values, simpleCmsProperty);
				break;
			case String:
				validateStringProperty(values, simpleCmsProperty);
				break;
			case Boolean:
			case ObjectReference:
			case TopicReference:
				//Nothing to validate for these yet. 
				break;
			case Complex:
			case ContentType:
				//This should never happen
				logger.warn("Tried to validate invalid type for simple cms property "+ simpleCmsPropertyDefinition.getValueType() + 
						" for property path "+ simpleCmsProperty.getFullPath() + " " +
						"whereas definition's full path is "+ simpleCmsPropertyDefinition.getFullPath());
				break;
			default:
				break;
			}


			//There is at least one value that will be saved
			return true;
		}
		else{
			return false;
		}
	}

	private void validateBinaryProperty(List<Object> values,
			SimpleCmsProperty simpleCmsProperty) {

		//If binary property is unmanaged then content must exist to
		//UnmanagedDataStore directory
		if (simpleCmsProperty.getPropertyDefinition() != null && 
				simpleCmsProperty.getPropertyDefinition() instanceof BinaryPropertyDefinition && 
				((BinaryPropertyDefinition)simpleCmsProperty.getPropertyDefinition()).isBinaryChannelUnmanaged() ){

			for (Object value : values){
				if (value != null){
					if (value instanceof BinaryChannel){
						if (! ((BinaryChannel)value).contentExists()){
							
							addErrorMessage(simpleCmsProperty.getFullPath(), 
									"content.object.edit.validation.invalid.path.to.unmanaged.binary.channel", 
									getLocalizedLabelOfFullPathFromCmsProperty(simpleCmsProperty),
									((BinaryChannel)value).getRelativeFileSystemPath());
						}
					}
				}
			}
		}
	}

	private void addErrorMessageForRequiredField(String propertyFullPath, String propertyLocalizedLabel) {
		//Store message to map and parent property will decide whether it should be announced
		mandatoryErrorMessagesPerPropertyPath.getFirst().put(propertyFullPath, JSFUtilities.getLocalizedMessage("errors.required", new String[]{propertyLocalizedLabel}));
	}
	
	private void addFinalErrorMessage(String propertyFullPath, String message) {
		
		JSFUtilities.addMessage(propertyFullPath, message,	FacesMessage.SEVERITY_WARN);

		hasErrors = true;
	}
	
	private void addErrorMessage(String propertyFullPath, String messageKey, String... messageParameters) {

		JSFUtilities.addMessage(propertyFullPath, messageKey, 
				messageParameters, FacesMessage.SEVERITY_WARN);

		hasErrors = true;

	}

	private String getLocalizedLabelOfFullPathFromCmsProperty(CmsProperty cmsProperty){
		return (cmsProperty == null ? "" : 
			cmsProperty.getLocalizedLabelOfFullPathforLocaleWithDelimiter(JSFUtilities.getLocaleAsString(), PATH_DELIMITER));
	}


	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {


	}

	private void validateLongProperty(List<Object> values,
			SimpleCmsProperty cmsProperty) {
		int valueIndex = -1;
		for ( Object value : values){
			++valueIndex;
			if (value != null){
				//usually all values are Strings if they are coming from UI forms. 
				// So we should check if we can convert them to longs
				Long longValue = null;
				if (value instanceof String){
					if (StringUtils.isNotBlank((String)value)){
						try{
							//If value is not a valid long then an exception is thrown
							longValue = Long.valueOf((String)value);
						}
						catch(Exception e){
							addErrorMessage(cmsProperty.getFullPath(), "errors.integerNumber", 
									getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
							return;
						}


					}
				}
				else if ( ! (value instanceof Long)) {
					addErrorMessage(cmsProperty.getFullPath(), "errors.integerNumber", 
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
					return;
				}
				
				if (longValue == null) {
					longValue = (Long) value;
				}
				// check if the number is within range
				if (cmsProperty.getPropertyDefinition() != null && 
						! ((LongPropertyDefinition)cmsProperty.getPropertyDefinition()).isValueValid(longValue)){
					// the isValueValid checks for both range and out of enumerated values errors. 
					// Here we assume without checking that it is a range error. 
					// If there is an enumeration in property definition then 
					// it could be an error because the value is outside the enumerated ones but the interface always
					// provides selection lists for enumerated values and thus this type of error is not possible
					addErrorMessage(cmsProperty.getFullPath(), "errors.range",
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty),
							String.valueOf(((LongPropertyDefinition)cmsProperty.getPropertyDefinition()).getMinValue()),
							String.valueOf(((LongPropertyDefinition)cmsProperty.getPropertyDefinition()).getMaxValue()));
				}

			}
			else {
				// There is a case that the property is multi-value and its list of values contains nulls. 
				// This may happen if the user deletes one or more values from the object form by cutting the number digits instead of using the (X) button to remove the field. 
				// In such a case when the user submits the form (i.e. save the object) then the property will contain an ArrayList one or more null values. 
				// We should identify such case and not raise an error but instead remove the null value from the list.
				if (cmsProperty.getPropertyDefinition().isMultiple()) {
					if (values.size() == 1) { // if there is only one value and is null remove the list itself 
						cmsProperty.removeValues();
					}
					else {
						cmsProperty.removeSimpleTypeValue(valueIndex);
					}
				}
				else {
					addErrorMessage(cmsProperty.getFullPath(), "errors.invalid", 
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
					
				}
			}
		}
			
	}

	private void validateDoubleProperty(List<Object> values,
			SimpleCmsProperty cmsProperty) {

		int valueIndex = -1;
		for (Object value : values){
			++valueIndex;
			if (value != null){
				//usually all values are Strings if they are coming from UI forms. 
				// So we should check if we can convert them to doubles
				Double doubleValue = null;
				if (value instanceof String){
					if (StringUtils.isNotBlank((String)value)){
						try{
							//If value is not a valid double then an exception is thrown
							doubleValue = Double.valueOf((String)value);
						}
						catch(Exception e){
							addErrorMessage(cmsProperty.getFullPath(), "errors.decimalNumber",
									getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
							return;
						}
						
					}
				}
				else if ( ! (value instanceof Double)) {
					addErrorMessage(cmsProperty.getFullPath(), "errors.decimalNumber", 
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
					return;
				}
				
				
				if (doubleValue == null) {
					doubleValue = (Double) value;
				}
				// check if number is within range
				if (cmsProperty.getPropertyDefinition() != null && 
						! ((DoublePropertyDefinition)cmsProperty.getPropertyDefinition()).isValueValid(doubleValue)){
					// the isValueValid checks for both range and out of enumerated values errors. 
					// Here we assume without checking that it is a range error. 
					// If there is an enumeration in property definition then 
					// it could be an error because the value is outside the enumerated ones but the interface always
					// provides selection lists for enumerated values and thus this type of error is not possible
					addErrorMessage(cmsProperty.getFullPath(), "errors.range",
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty),
							String.valueOf(((DoublePropertyDefinition)cmsProperty.getPropertyDefinition()).getMinValue()),
							String.valueOf(((DoublePropertyDefinition)cmsProperty.getPropertyDefinition()).getMaxValue()));
				}
			}
			else {
				// There is a case that the property is multi-value and its list of values contains nulls. 
				// This may happen if the user deletes one or more values from the object form by cutting the number digits instead of using the (X) button to remove the field. 
				// In such a case when the user submits the form (i.e. save the object) then the property will contain an ArrayList one or more null values. 
				// We should identify such case and not raise an error but instead remove the null value from the list.
				if (cmsProperty.getPropertyDefinition().isMultiple()) {
					if (values.size() == 1) { // if there is only one value and is null remove the list itself 
						cmsProperty.removeValues();
					}
					else {
						cmsProperty.removeSimpleTypeValue(valueIndex);
					}
				}
				else {
					addErrorMessage(cmsProperty.getFullPath(), "errors.invalid", 
							getLocalizedLabelOfFullPathFromCmsProperty(cmsProperty));
					
				}
			}
		}

	}

	private boolean hasValue(SimpleCmsPropertyDefinition simplePropertyDefinition, SimpleCmsProperty simpleProperty){

		if (ValueType.Binary == simplePropertyDefinition.getValueType()){
			boolean hasValue = false;

			List<BinaryChannel> tempBinaryChannelList = new ArrayList<BinaryChannel>();

			if (simplePropertyDefinition.isMultiple())
				tempBinaryChannelList.addAll(((BinaryProperty)simpleProperty).getSimpleTypeValues());
			else
				tempBinaryChannelList.add(((BinaryProperty)simpleProperty).getSimpleTypeValue());

			if (CollectionUtils.isNotEmpty(tempBinaryChannelList))
			{
				//All binaryChannels must have content or binary channel id
				for (BinaryChannel binaryChannel: tempBinaryChannelList)
				{
					if (binaryChannel != null)//It may be the case that binaryChannel is a single and has no value. Thus list will contain one element which is null
						hasValue = binaryChannelHasValue(binaryChannel);

					if (!hasValue)
						return false;
				}
			}

			return hasValue;

		}
		else{
			
			boolean hasValue = simpleProperty.hasValues();
			
			if (!hasValue)
			{ 
				//No value found. Check if property is of type password.
				//In this case check if new password is provided in the special 
				//PasswordVerifier bean
				if (simplePropertyDefinition != null && 
					simplePropertyDefinition instanceof StringPropertyDefinition && 
			 		((StringPropertyDefinition)simplePropertyDefinition).isPasswordType() && 
			 		simpleCmsPropertyValuesOfPasswordType.containsKey(simplePropertyDefinition.getFullPath())){

					List<SimpleCmsPropertyValueWrapper> simpleCmsPropertyValueWrappers = simpleCmsPropertyValuesOfPasswordType.get(simplePropertyDefinition.getFullPath());
					
					for (SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper : simpleCmsPropertyValueWrappers)
					{
						if (StringUtils.isNotBlank(simpleCmsPropertyValueWrapper.getPasswordVerifier().getNewPassword()))
						{
							return true;
						}
					}
				}
				else{
					//Property is no password type. If it is StringProperty
					//blank strings are considered non values
					if (! simplePropertyDefinition.isMultiple() && simplePropertyDefinition.getValueType() == ValueType.String)
					{
						return StringUtils.isNotBlank((String)simpleProperty.getSimpleTypeValue());
					}
				}
			}
			
			return hasValue;
			
			/*if (simplePropertyDefinition.isMultiple())
				return CollectionUtils.isNotEmpty(simpleProperty.getSimpleTypeValues());
			else // it is Single Value
				if (simplePropertyDefinition.getValueType() == ValueType.String)
					return StringUtils.isNotBlank((String)simpleProperty.getSimpleTypeValue());
				else
					return (simpleProperty.getSimpleTypeValue() != null);*/
		}
	}

	private boolean binaryChannelHasValue(BinaryChannel binaryChannel) {

		if (binaryChannel.getContent() != null)
			return true;

		//No Binary Content exists. Check BinaryChannelUUID
		if (binaryChannel.getId() != null)
			return true;

		//No Content and no uuid. 
		return false;

	}

	public boolean mandatoryPropertiesAreMissing()
	{
		return mandatoryPropertiesAreMissing;
	}

	public boolean hasErrors(){
		return hasErrors || mandatoryPropertiesAreMissing;
	}

	public void registerSimpleCmsPropertyOfPasswordType(
			SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper) {
		
		if (simpleCmsPropertyValueWrapper == null){
			return ;
		}
		
		if (simpleCmsPropertyValuesOfPasswordType == null){
			simpleCmsPropertyValuesOfPasswordType = new HashMap<String, List<SimpleCmsPropertyValueWrapper>>();
		}
		
		String fullPropertyPath = simpleCmsPropertyValueWrapper.getFullPropertyPath();
		
		if (!simpleCmsPropertyValuesOfPasswordType.containsKey(fullPropertyPath)){
			simpleCmsPropertyValuesOfPasswordType.put(fullPropertyPath, new ArrayList<SimpleCmsPropertyValueWrapper>());
		}
		
		//In case property is single value then only one wrapper must exist
		if (!simpleCmsPropertyValueWrapper.isMultiple())
		{
			simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).clear();
			simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
		}
		else{
			//Replace simpleCmsPropertyWrapper with the same index
			int valueIndex = simpleCmsPropertyValueWrapper.getValueIndex();
			
			if (valueIndex < 0)
			{
				//Index -1. Add to list although index is not valid
				simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
			}
			else
			{
				boolean addedToList = false;
				for (int i=0;i<simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).size();i++)
				{
					if (simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).get(i).getValueIndex() == valueIndex)
					{
						simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).set(i, simpleCmsPropertyValueWrapper);
						addedToList = true;
						break;
					}
				}
				
				//Add to the end of the list
				if (! addedToList)
				{
					simpleCmsPropertyValuesOfPasswordType.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
				}
			}
		}
		
		
	}

	/**
	 * @param simpleCmsPropertyValueWrapper
	 */
	public void registerInvalidSimpleCmsPropertyWithTempValue(
			SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper) {
		
		if (simpleCmsPropertyValueWrapper == null){
			return ;
		}
		
		if (simpleCmsPropertyValuesOfInvalidTempValue == null){
			simpleCmsPropertyValuesOfInvalidTempValue = new HashMap<String, List<SimpleCmsPropertyValueWrapper>>();
		}
		
		String fullPropertyPath = simpleCmsPropertyValueWrapper.getFullPropertyPath();
		
		if (!simpleCmsPropertyValuesOfInvalidTempValue.containsKey(fullPropertyPath)){
			simpleCmsPropertyValuesOfInvalidTempValue.put(fullPropertyPath, new ArrayList<SimpleCmsPropertyValueWrapper>());
		}
		
		//In case property is single value then only one wrapper must exist
		if (!simpleCmsPropertyValueWrapper.isMultiple()){
			simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).clear();
			simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
		}
		else{
			//Replace simpleCmsPropertyWrapper with the same index
			int valueIndex = simpleCmsPropertyValueWrapper.getValueIndex();
			
			if (valueIndex < 0){
				//Index -1. Add to list although index is not valid
				simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
			}
			else{
				boolean addedToList = false;
				for (int i=0;i<simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).size();i++){
					if (simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).get(i).getValueIndex() == valueIndex){
						simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).set(i, simpleCmsPropertyValueWrapper);
						addedToList = true;
						break;
					}
				}
				
				//Add to the end of the list
				if (! addedToList){
					simpleCmsPropertyValuesOfInvalidTempValue.get(fullPropertyPath).add(simpleCmsPropertyValueWrapper);
				}
			}
		}
	}
	
	
}

