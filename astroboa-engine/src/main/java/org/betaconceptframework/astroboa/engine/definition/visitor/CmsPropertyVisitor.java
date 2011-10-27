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

package org.betaconceptframework.astroboa.engine.definition.visitor;


import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.XMLChar;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.StringFormat;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.definition.XSSchemaItem;
import org.betaconceptframework.astroboa.engine.definition.xsom.CmsAnnotation;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.definition.BinaryPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.BooleanPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.CalendarPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.CmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexPropertyDefinitionHelper;
import org.betaconceptframework.astroboa.model.impl.definition.ContentObjectTypeDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.DoublePropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizableCmsDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizationImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LongPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ObjectReferencePropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.StringPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.TopicReferencePropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsDefinitionItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.visitor.XSVisitor;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyVisitor  implements XSVisitor{

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	private Map<ItemQName, XSAttributeUse> builtInAttributes = new TreeMap<ItemQName, XSAttributeUse>();
	private LocalizableCmsDefinition definition;
	
	//All possible elements that constitute a definition
	//Because all definition objects are immutable, setters are not available
	//therefore all information is gathered and then
	//is loaded in the constructor of appropriate Definition class
	private boolean createDefinition = true;
	private String name;
	private Localization displayName;
	private ValueType valueType;
	private ComplexCmsPropertyDefinition complexDefinitionReference;
	private String defaultValue;
	private Localization description;
	private boolean obsolete;
	private String restrictReadToRoles;
	private String restrictWriteToRoles;
	private StringFormat stringFormat;
	private boolean mandatory;
	private boolean multiple;
	//Using LinkedHashMap we ensure that definitions are kept in insertion order
	//and thus when marshaling a ContentObject (create an XML representation)
	//it will be consistent with its XML Schema
	private Map<String, CmsPropertyDefinition> childPropertyDefinitions = new LinkedHashMap<String, CmsPropertyDefinition>();
	private LocalizableCmsDefinition parentDefinition;
	private String namespaceUri;
	private URI definitionFileURI;
	private Integer order;
	private Set<String> acceptedContentTypes;
	private List<String> acceptedTaxonomies;

	private String calendarPattern;
	private String labelElementPath;
	
	private boolean global;

	private Map<String, Localization> definitionValueRange;
	private boolean cacheComplexDefinitionReference = true;
	private boolean binaryChannelIsUnmanaged = false;
	private boolean passwordType;
	private String passwordEncryptorClassName;

	private List<String> superTypes;
	private CmsDefinitionVisitor cmsDefinitionVisitor;
	private QName qNameOfParentDefinitionWithTheSameType;
	private String complexPropertyTypeName;
	private boolean definitionRefersToItsParent;
	private boolean complexDefinitionContainsCommonAttributes;
	
	private Object minValue;
	private Object maxValue;
	
	private Integer minLength;
	private Integer maxLength;
	
	private boolean minValueIsExclusive;
	private boolean maxValueIsExclusive;

	private String pattern;
	
	
	private boolean typeDefinitionExtendsBaseObjectType;
	
	private QName generatedQNameForDefinition(){
		if (parentDefinition != null && parentDefinition.getQualifiedName() != null && parentDefinition.getQualifiedName().getPrefix() != null){
			return new QName(namespaceUri,name,parentDefinition.getQualifiedName().getPrefix());
		}
		else{
			//Qualified names with the same namespace 
			//for example one XSD file which contains 
			//two content types and one global complex type
			//must have the same prefix.  Therefore 
			//prefix originates from namespaceUri.
			//Otherwise definition name will be used for prefix
			String prefix = StringUtils.substringAfterLast(namespaceUri, CmsConstants.FORWARD_SLASH);
			
			if (StringUtils.isNotBlank(prefix)){
				return new QName(namespaceUri,name,prefix);
			}
			else{
				return new QName(namespaceUri,name,name);
			}
		}
	}
	
	public LocalizableCmsDefinition getDefinition() {
		if (definition != null){
			return definition;
		}

		if (createDefinition && valueType != null){


			switch (valueType) {
			case Binary:
				definition = new BinaryPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory,order, restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition, null, binaryChannelIsUnmanaged);
				break;
			case Boolean:
				definition = new BooleanPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory,order, restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,
						(StringUtils.isNotBlank(defaultValue)? Boolean.valueOf(defaultValue): null) , 
						null);
				break;
			case Complex:
				
				ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper = new ComplexPropertyDefinitionHelper();
				

				//Definition refers to a complex definition. 
				//Clone child property definitions of complex definition
				//and add them to this definition
				if (complexDefinitionReference != null || qNameOfParentDefinitionWithTheSameType != null){
					//A property cannot refer to another complex and have at the same time its own properties
					if (MapUtils.isNotEmpty(childPropertyDefinitions)){
						logger.warn("Definition {} refers to another complex definition but it also contains its own child properties", name);
					}
					
					//A property cannot have the same definition with its parent and at the same time to be 
					//mandatory
					if (definitionRefersToItsParent && mandatory){
						throw new CmsException("Infinite loop detected. Definition {"+namespaceUri+"}"+name+" refers to its parent and thus cannot be mandatory");
					}
					//Inherit some properties from complex reference
					if (description == null || ! description.hasLocalizedLabels()){
						//Either inherit from complex definition reference or its parent
						if (complexDefinitionReference != null){
							description = ((LocalizableCmsDefinitionImpl)complexDefinitionReference).cloneDescription();
						}
						else if (definitionRefersToItsParent){
							description = ((LocalizableCmsDefinitionImpl)parentDefinition).cloneDescription();	
						}
						else{
							ComplexCmsPropertyDefinition ancestorDefinition = locateParentDefinintionWithSameType();
							
							if (ancestorDefinition != null){
								description = ((LocalizableCmsDefinitionImpl)ancestorDefinition).cloneDescription();
							}
						}
					}

					if (displayName == null || !displayName.hasLocalizedLabels()){
						//Either inherit from complex definition reference or its parent
						if (complexDefinitionReference != null){
							displayName = ((LocalizableCmsDefinitionImpl)complexDefinitionReference).cloneDisplayName();
						}
						else if (definitionRefersToItsParent){
							displayName = ((LocalizableCmsDefinitionImpl)parentDefinition).cloneDisplayName();
						}
						else{
							ComplexCmsPropertyDefinition ancestorDefinition = locateParentDefinintionWithSameType();
							
							if (ancestorDefinition != null){
								displayName = ((LocalizableCmsDefinitionImpl)ancestorDefinition).cloneDisplayName();
							}
						}
					}

					
					if (StringUtils.isBlank(labelElementPath)){
						if (complexDefinitionReference != null){
							labelElementPath =  complexDefinitionReference.getPropertyPathsWhoseValuesCanBeUsedAsALabel();
						}
						else if (definitionRefersToItsParent){
							labelElementPath =  ((ComplexCmsPropertyDefinition)parentDefinition).getPropertyPathsWhoseValuesCanBeUsedAsALabel();
						}
						else{
							ComplexCmsPropertyDefinition ancestorDefinition = locateParentDefinintionWithSameType();
							
							if (ancestorDefinition != null){
								labelElementPath =  ((ComplexCmsPropertyDefinition)ancestorDefinition).getPropertyPathsWhoseValuesCanBeUsedAsALabel();
							}
						}
					}
					
				}
				
				checkIfCommonAttributesAreDefined();

				//Create definition
				definition = new ComplexCmsPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
							restrictWriteToRoles, parentDefinition, complexPropertyDefinitionHelper, definitionFileURI, 
							labelElementPath, global, qNameOfParentDefinitionWithTheSameType, complexPropertyTypeName, complexDefinitionContainsCommonAttributes);

				logger.debug("Created definition for complex type '{}' which {}", 
						new Object[]{name, 
						(complexDefinitionReference != null ? " refers to complex definition '"+ complexDefinitionReference.getQualifiedName().toString() +"'": 
						" does not refer to another definition")});

				//Finally clone every child property of complex definition reference
				if (complexDefinitionReference != null && complexDefinitionReference.hasChildCmsPropertyDefinitions()){
						//Initialize current child property definitions
					if (childPropertyDefinitions == null){
							childPropertyDefinitions = new LinkedHashMap<String, CmsPropertyDefinition>();
					}

					Collection<CmsPropertyDefinition> childPropertiesOfComplexDefinitionReference = complexDefinitionReference.getChildCmsPropertyDefinitions().values();
					for (CmsPropertyDefinition complexReferenceChildPropertyDefinition : childPropertiesOfComplexDefinitionReference){
						
						logger.debug("Cloning definition {} and put it inside {}",complexReferenceChildPropertyDefinition.getQualifiedName(), definition.getQualifiedName() );
						
						CmsPropertyDefinition clone = ((CmsPropertyDefinitionImpl)complexReferenceChildPropertyDefinition).clone((ComplexCmsPropertyDefinition)definition);
						
						childPropertyDefinitions.put(complexReferenceChildPropertyDefinition.getName(), clone);
						
					}
				}
					
				complexPropertyDefinitionHelper.setChildPropertyDefinitions(childPropertyDefinitions);
				
				break;
			case ObjectReference:
				
				definition = new ObjectReferencePropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,	null, acceptedContentTypes);
				
				break;
			case ContentType:
				
				ComplexPropertyDefinitionHelper contentTypePropertyDefinitionHelper3 = new ComplexPropertyDefinitionHelper();
				contentTypePropertyDefinitionHelper3.setChildPropertyDefinitions(childPropertyDefinitions);

				definition = new ContentObjectTypeDefinitionImpl(generatedQNameForDefinition(), description, displayName,	
						contentTypePropertyDefinitionHelper3,definitionFileURI, superTypes, typeDefinitionExtendsBaseObjectType, labelElementPath);
				
				logger.debug("Created definition for content type '{}'",name);
				
				break;
			case Date:
				definition = new CalendarPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,
						(StringUtils.isNotBlank(defaultValue)? DateUtils.fromString(defaultValue, calendarPattern): null) , 
						null,calendarPattern);
				break;
			case Double:
				Map<Double, Localization> acceptedValues = null;
				
				if (MapUtils.isNotEmpty(definitionValueRange)){
					acceptedValues = new LinkedHashMap<Double, Localization>();
					for (Entry<String, Localization> value : definitionValueRange.entrySet()){
						acceptedValues.put(Double.valueOf(value.getKey()), value.getValue());
					}
				}
				
				definition = new DoublePropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,
						(StringUtils.isNotBlank(defaultValue)? Double.valueOf(defaultValue): null) , 
						null, acceptedValues, (Double)minValue, minValueIsExclusive, (Double)maxValue, maxValueIsExclusive);
				break;
			case Long:
				Map<Long, Localization> longAcceptedValues = null;
				
				if (MapUtils.isNotEmpty(definitionValueRange)){
					longAcceptedValues = new LinkedHashMap<Long, Localization>();
					for (Entry<String, Localization> value : definitionValueRange.entrySet()){
						longAcceptedValues.put(Long.valueOf(value.getKey()), value.getValue());
					}
				}
				
				definition = new LongPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,
						(StringUtils.isNotBlank(defaultValue)? Long.valueOf(defaultValue): null) , 
						null, longAcceptedValues, (Long)minValue, minValueIsExclusive, (Long)maxValue, maxValueIsExclusive);
				break;
			case String:
				definition = new StringPropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory,order, restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,
						defaultValue, null, maxLength, minLength, stringFormat, definitionValueRange, 
						passwordEncryptorClassName, passwordType, pattern);
				break;
			case TopicReference:
				definition = new TopicReferencePropertyDefinitionImpl(generatedQNameForDefinition(), description, displayName,	obsolete, multiple, mandatory, order,restrictReadToRoles, 
						restrictWriteToRoles, parentDefinition,	null, acceptedTaxonomies);
				break;

			default:
				break;
			}
		}

		return definition;
	}

	private void checkIfCommonAttributesAreDefined() {
		
		complexDefinitionContainsCommonAttributes = complexDefinitionContainsCommonAttributes || 
		 ( complexDefinitionReference != null && ((ComplexCmsPropertyDefinitionImpl)complexDefinitionReference).commonAttributesAreDefined());
		
		if (!complexDefinitionContainsCommonAttributes){
			
			String definitionName = "{"+namespaceUri+"}"+name;
			
			String complexReferenceName = complexDefinitionReference == null ? "" : "{"+complexDefinitionReference.getQualifiedName().getNamespaceURI()+"}"+complexDefinitionReference.getName();
			
			if (multiple){
				if (complexDefinitionReference != null){
					//TODO : Either throw an exception or log a warning
					//and set flag to true
					logger.warn("Definition "+definitionName+" has multiple occurence and it is of type "+ complexReferenceName+
							" which does not define the common entity attributes. You should add '<xs:attributeGroup ref=\"bccmsmodel:commonEntityAttributes\"/>'" +
							" to complex type "+complexReferenceName);
				}
				else{
					//TODO : Either throw an exception or log a warning
					//and set flag to true
					logger.warn("Definition "+definitionName+" has multiple occurence but it does not define the common entity attributes. " +
							"You should add '<xs:attributeGroup ref=\"bccmsmodel:commonEntityAttributes\"/>'" +
							" to complex type "+definitionName);
				}
			}
			else{
				if (complexDefinitionReference != null){
					logger.warn("Definition {} is of type {} which does not define the common entity attributes. " +
							" You should add '<xs:attributeGroup ref=\"bccmsmodel:commonEntityAttributes\"/>'" +
							" to complex type {}",new Object[]{definitionName, complexReferenceName,complexReferenceName});
				}
				else{
					logger.warn("Definition {} does not define the common entity attributes. " +
							"You should add '<xs:attributeGroup ref=\"bccmsmodel:commonEntityAttributes\"/>'" +
							" to complex type {}",definitionName, definitionName);
				}
			}
		}
	}

	public CmsPropertyVisitor(Map<ItemQName, XSAttributeUse> builtInAttributes, 
			LocalizableCmsDefinition parentDefinition, boolean mandatory, boolean multiple, int defaultOrder, 
			CmsDefinitionVisitor cmsDefinitionVisitor) {
		this.builtInAttributes = builtInAttributes;
		this.parentDefinition = parentDefinition;
		this.mandatory = mandatory;
		this.multiple = multiple;
		this.order = defaultOrder;
		this.cmsDefinitionVisitor = cmsDefinitionVisitor;
	}

	public void annotation(XSAnnotation annotationHolder) {
		CmsAnnotation definitionAnnotation = (CmsAnnotation) annotationHolder.getAnnotation();

		if (definitionAnnotation.isNotEmpty())
		{
			logger.debug("Adding label '{}' and descritpion '{}' for definition '{}'",
					new Object[]{definitionAnnotation.getDisplayName().getLocalizedLabels(),
					definitionAnnotation.getDescription().getLocalizedLabels(),
					name});
			
			displayName = definitionAnnotation.getDisplayName();
			description = definitionAnnotation.getDescription();
		}
		else
		{
			displayName.addLocalizedLabel(Locale.ENGLISH.toString(), name);
			description.addLocalizedLabel(Locale.ENGLISH.toString(), name);
			logger.warn("Definition '{}' does not have any display name or description specified. Its name in ENGLISH locale will be provided " +
					" for its displayName and its description",name);
		}
	}

	public void attGroupDecl(XSAttGroupDecl arg0) {


	}

	public void attributeDecl(XSAttributeDecl arg0) {


	}

	public void attributeUse(XSAttributeUse attribute) {
	
		determineValueTypeForSimpleType(attribute.getDecl().getName(), attribute.getDecl().getType());

		logger.debug("Constructing definition with type '{}' for attribute '{}'",valueType , attribute.getDecl().getName());

		populateDefinition(attribute);

	}
	
	
	private void addAttributeAsChildProperty(XSAttributeUse attribute){
	
		boolean mandatory = attribute.isRequired();
		boolean multiple = false;

		LocalizableCmsDefinition definitionForCurrentProperty = getDefinition();
			
		cmsDefinitionVisitor.cacheDefinitionWhichIsUnderProcess(definitionForCurrentProperty);
			
		CmsPropertyVisitor propertyVisitor = new CmsPropertyVisitor(builtInAttributes, 
				definitionForCurrentProperty, mandatory, multiple, childPropertyDefinitions.size(), 
				cmsDefinitionVisitor);
		attribute.visit(propertyVisitor);

		LocalizableCmsDefinition subPropertyDefinition = propertyVisitor.getDefinition();
		if (subPropertyDefinition == null){
			logger.warn("Unable to define sub property '{}'", "{"+attribute.getDecl().getTargetNamespace()+"}"+attribute.getDecl().getName());
		}
		else{
			logger.debug("Adding child property definition '{}' to definition '{}'", subPropertyDefinition.getQualifiedName(), 
					"{"+namespaceUri+"}"+name);
			childPropertyDefinitions.put(subPropertyDefinition.getName(), (CmsPropertyDefinition)subPropertyDefinition);
		}

		
	}

	public void complexType(XSComplexType complexType) {

			//Special case where complex type contains simple content
			//whose type is Base64Binary
			if (complexTypeExtendsBase64Binary(complexType)){
				valueType = ValueType.Binary;
			}
			else{
				valueType = ValueType.Complex;

				complexPropertyTypeName = complexType.getName();
			
				if (elementExtendsComplexCmsPropertyType(complexType)){
					complexDefinitionContainsCommonAttributes = true;
				}
				else{
					complexDefinitionContainsCommonAttributes = complexTypeContainsCommonEntityAttributeGroup(complexType);
				}
			}	
			
			logger.debug("Instantiated definition of type '{}'",valueType);
			
			populateDefinition(complexType);
		
	}


	private void populateDefinition(XSComponent component) {
		try {

			if (isDefinitionComplex()){
				//Do not process complex property if already exists
				if (component instanceof XSComplexType){
					if (name != null && cmsDefinitionVisitor.getDefinitionCacheRegion().hasComplexTypeDefinition(name)){
						logger.debug("Complex Definition '{}' has already been processed.", name);

						createDefinition = false;
						return;
					}
				}
			}

			//Set namespaceURI
			retrieveElementNamespace(component);

			//Set URI
			retrieveSchemaURI(component);

			setBasicProperties(component);

			//process further in case of ComplexProperty or ContentType
			if (ValueType.ContentType == valueType || isDefinitionComplex()){
				
				if (component instanceof XSComplexType ){
					visitSubProperties(((XSComplexType)component), false);
				}
				else if (component instanceof XSElementDecl){
					
					final XSElementDecl element = ((XSElementDecl)component);
					if (element.getType() != null){
						if (ValueType.ContentType == valueType){
							//XML element represents a ContentType.
							//First we have to visit the inherited elements (at least from complexType contentObjectType
							//found in astroboa-model.xsd)
							
							if (!element.getType().isComplexType()){
								throw new CmsException("Element "+ element.getName()+ " is Content Type but it does not refer to a complex type");
							}
							
							//Create a stack of all parent elements
							Deque<XSType> elementTypes = collectAllSuperTypesIncludingProvidedElement(element);
							
							if (superTypes == null){
								superTypes = new ArrayList<String>();
							}

							//Now visit properties for each element stored
							boolean elementExtendsBaseContentObjectType = false;
							
							while (!elementTypes.isEmpty()){
								
								//Retrieve the first item in queue and remove it at the same time
								final XSComplexType complexType = elementTypes.pollFirst().asComplexType();
								
								final boolean complexTypeRepresentsContentObjectType =  
										complexType.getName() != null &&
										CmsDefinitionItem.contentObjectType.equals(ItemUtils.createNewItem(null, complexType.getTargetNamespace(), complexType.getName()));
								
								elementExtendsBaseContentObjectType = elementExtendsBaseContentObjectType || complexTypeRepresentsContentObjectType;
								
								if (! complexTypeRepresentsContentObjectType && complexType.getName() != null){
									
									logger.debug("Adding superType {} for definition {}", complexType.getName(), name);
									
									superTypes.add(complexType.getName());
								}
								
								visitSubProperties(complexType,	complexTypeRepresentsContentObjectType);
							}
							
							if (! elementExtendsBaseContentObjectType){
								//Element is a global one and does not extend base contentObjectType.
								//In this case we should dynamically load contentObjectType properties

								XSComplexType contentObjectType = cmsDefinitionVisitor.getComplexType("contentObjectType");
								
								if (contentObjectType == null){
									throw new CmsException("Could not locate complex type contentObjectType.");
								}
								
								visitSubProperties(contentObjectType, true);
								
							}
						}
						else {
							
							if (element.getType().isLocal()){
								logger.debug("Definition '{}' represents a complex element which is defined inside an element.", name);

								visitSubProperties(element.getType().asComplexType(), false);
							}
							else{
								logger.debug("Definition '{}' represents a complex element which refers to another complex type", name);

								setComplexReference(element);
							}
						}
					}
					else 
						throw new CmsException("Element "+ element.getName()+ " is "+ valueType+"but it does not refer to a type");
				}
			}

		} catch (Throwable e) {
			logger.error("",e);
			createDefinition = false;
		}
	}

	private Deque<XSType> collectAllSuperTypesIncludingProvidedElement(XSElementDecl element){
		
		Deque<XSType> types = new ArrayDeque<XSType>();
		
		XSType currentElement = element.getType();
		
		boolean elementIsContentObjectType = false;
		
		while (! elementIsContentObjectType){
			
			elementIsContentObjectType = 
				currentElement.getName() != null &&
				CmsDefinitionItem.contentObjectType.equals(ItemUtils.createNewItem(null, currentElement.getTargetNamespace(), currentElement.getName()));

			if (elementIsContentObjectType){

				if (currentElement.isComplexType()){
					types.push(currentElement);
				}

				break; //Need to go no further
			}
			else{
				
				//Method getBaseType is always non-null.
				//According to the documentation, 
				//"Note that if this type represents xs:anyType, this method returns itself. "
				//Threrefore break loop if we come across to such case
				if (currentElement == currentElement.getBaseType()){
					break;
				}
				else{
					if (currentElement.isComplexType()){
						types.push(currentElement);
					}

					currentElement = currentElement.getBaseType();
				}
			}
		}
		
		return types;

	}
	
	private boolean isDefinitionComplex(){
		return valueType != null && ValueType.Complex == valueType;
	}

	private void visitSubProperties(final XSComplexType complexType, boolean complexTypeRepresentsContentObjectType) throws Exception {

		logger.debug("Visiting sub properties for definition '{}'",name );

		//Inherit complexType Annotation in case no annotation is defined so far
		if (displayName == null || MapUtils.isEmpty(displayName.getLocalizedLabels()))
		{
			if (complexType.getAnnotation() != null)
				annotation(complexType.getAnnotation());
		}

		XSParticle particle = complexType.getContentType().asParticle();

		if (particle == null){
			
			//Probably empty. If not throw an exception for an invalid complex type
			if (complexType.getContentType().asEmpty() == null){
				
				if (complexType.getName() !=null){
					throw new Exception("Complex Type "+ complexType.getName()+ " definition is not supported by ASTROBOA");
				}
				else{
					throw new Exception("Complex Type "+ complexType.getBaseType().getName()+ " definition is not supported by ASTROBOA");
				}
			}
		}
		else{
			addSubElementsAsChildProperties(particle.getTerm().asModelGroup(), complexTypeRepresentsContentObjectType);
		}

		if (complexType.getAttributeUses() != null ){
			
			String complexTypeTargetNamespace = retrieveComplexTypeTargetNamespace(complexType);
					
			for (XSAttributeUse attribute : complexType.getAttributeUses()){
				
				String attributeTargetNamespace = getAttributeNamespace(attribute, complexTypeTargetNamespace);
				
				//Check if complex type defines any attributes, excluding ASTROBOA's built-in attributes
				if (! StringUtils.equals(attributeTargetNamespace, BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI)){
					
					ItemQName attr = ItemUtils.createNewItem("", attributeTargetNamespace, attribute.getDecl().getName());
					ItemQName attrType = ItemUtils.createNewItem("", attribute.getDecl().getType().getTargetNamespace(), attribute.getDecl().getType().getName());
					
					logger.debug("Found attribute {} with type {}", attr, attrType);
					
					addAttributeAsChildProperty(attribute);
					
				}
			}
		}
		
	}

	private String getAttributeNamespace(XSAttributeUse attribute, String parentNamespace){
		
		//Get attribute' namespace 
		String attributeNamespace = null;
		
		if (attribute.getDecl() != null){
			attributeNamespace = attribute.getDecl().getTargetNamespace();
		}
		
		//Get the namespace of the schema where this attribute is defined
		//We are interested where this attribute is defined. 
		if (StringUtils.isBlank(attributeNamespace) && attribute.getOwnerSchema() != null){
			attributeNamespace = attribute.getOwnerSchema().getTargetNamespace();
		}
		
		//Get the namespace of the parent component (element or complex type)
		if (StringUtils.isBlank(attributeNamespace)){
			attributeNamespace = parentNamespace;
		}
		
		return attributeNamespace;
	}
	
	private String retrieveComplexTypeTargetNamespace(XSComplexType complexType) {
		
		String targetNamespace = complexType.getTargetNamespace();
		
		if (StringUtils.isBlank(targetNamespace)){
			
			if (!complexType.isGlobal()){
				//Get namespace of its parent if any
				XSElementDecl parentElement = complexType.getScope();
			
				if (parentElement != null){
					targetNamespace = parentElement.getTargetNamespace();
				}
			}
			
			if (StringUtils.isBlank(targetNamespace) && complexType.getOwnerSchema() != null){
				//Retrieve tagetNamespace as defined in the schema where this complexType exists
				targetNamespace = complexType.getOwnerSchema().getTargetNamespace();
			}
			
		}
		
		return targetNamespace;
	}


	private void setComplexReference(XSElementDecl element) throws Exception {

		//Check if referred type exists or needs to be processed further
		XSType complexRefType = element.getType();

		final String typeName = complexRefType.getName();
		final String typeNamespace = complexRefType.getTargetNamespace();

		//No type found
		if (typeName == null){
			throw new Exception("Definition "+ name+ " does not refer to a specific type");
		}
		
		this.complexPropertyTypeName = typeName;

		//Type name exists. Check if it has already been processed
		//Check complex type definitions. In the future Content type definitions should be checked as well
		//Create ComplexReference if typeName is null (that is, element is a complex property and contains its own declaration)
		//or if type name exists but complex declaration does not exist in cache
		if (cmsDefinitionVisitor.getDefinitionCacheRegion().hasComplexTypeDefinition(typeName)){
			
			//	Get Definition from Cache
			complexDefinitionReference = (ComplexCmsPropertyDefinition)cmsDefinitionVisitor.getDefinitionCacheRegion().getComplexCmsDefinition(typeName);
			logger.debug("Complex Definition reference '{}' found in cache and related with definition '{}'",complexDefinitionReference.getName() , name);
		}
		else if (! cacheComplexDefinitionReference && cmsDefinitionVisitor.internalCacheHasDefinition(typeName, typeNamespace)) {
			// Get Definition from InternalCache
			complexDefinitionReference = (ComplexCmsPropertyDefinition)cmsDefinitionVisitor.getDefinitionFromInternalCache(typeName, typeNamespace);
			logger.debug("Complex Definition reference '{}' found in internal cache and related with definition '{}'",complexDefinitionReference.getName() , name);
		}
		else if (cmsDefinitionVisitor.isDefinitionUnderProcess(typeName, typeNamespace)){
			logger.debug("Complex Definition refers to one of its grandparent and process stop '{}' ","{"+typeNamespace+"}"+typeName);
			
			qNameOfParentDefinitionWithTheSameType = new QName(complexPropertyTypeName, typeNamespace);
			
		}
		else{
			//There is no complex type definition. 
			//Check if type refers to the parent definition. i.e. this element refers to its parent element
			if (typeName.equals(parentDefinition.getName()) && ((CmsPropertyDefinition)parentDefinition).getQualifiedName() !=null && ((CmsPropertyDefinition)parentDefinition).getQualifiedName().getNamespaceURI().equals(typeNamespace)){
				//This is an element which refers to its parent element.
				//Set the reference to its parent and proceed no further
				qNameOfParentDefinitionWithTheSameType = new QName(complexPropertyTypeName, typeNamespace);
				definitionRefersToItsParent = true;
			}
			else{
				//Element refers to a complex type which has not been processed at all
				//Process complex type 
				if (complexRefType.isComplexType()){
					CmsPropertyVisitor visitor = new CmsPropertyVisitor(builtInAttributes, null, false, false,0, cmsDefinitionVisitor);
					complexRefType.asComplexType().visit(visitor);

					complexDefinitionReference = (ComplexCmsPropertyDefinition) visitor.getDefinition();

					if (complexDefinitionReference != null){						
						if (! cacheComplexDefinitionReference ){
							cmsDefinitionVisitor.cacheInternalDefinition(complexDefinitionReference);
						}
						else{
							cmsDefinitionVisitor.cacheDefinition(complexDefinitionReference);
						}

						logger.debug("Definition '{}' will be related with reference '{}'",name , complexDefinitionReference.getName());
					}
					else{
						logger.warn("No complex definition reference could be created for complex type '{}{}' ","{"+typeNamespace+"}", typeName);
					}


				}
				else{
					throw new Exception("Element "+ element.getName() + " does not refer to a complex type. Type's details : "+
							" Name = "+ typeName + " Namespace = "+ complexRefType.getTargetNamespace());
				}
			}

		}
	}

	private void addSubElementsAsChildProperties(XSModelGroup modelGroup, boolean modelGroupBelongsToContentObjectType) {

		//Expected that all elements are defined in xs:sequence or xs:all
		if (modelGroup != null && 
				( XSModelGroup.Compositor.SEQUENCE == modelGroup.getCompositor() ||
						XSModelGroup.Compositor.ALL == modelGroup.getCompositor())
		){
			XSParticle[] subElements  = modelGroup.getChildren();
			if (!ArrayUtils.isEmpty(subElements)){
				for (XSParticle subElement: subElements){
					if (modelGroupBelongsToContentObjectType){
						//We are processing child elements of built in complex type contentObjectType
						//In this case element owner must be ignored
						if ("owner".equals(subElement.getTerm().asElementDecl().getName())){
							continue;
						}
					}
					
					//Elements representing content types extend a base complex type
					//thus they contain all particles from that complex type along with their own
					//Process XSParticles that exist in the same Schema document with the parent complexType
					if (subElement.getSourceDocument() != null && modelGroup.getSourceDocument() != null
							&& subElement.getSourceDocument().getSystemId().equalsIgnoreCase(modelGroup.getSourceDocument().getSystemId())){

						//It may be the case that particle is itself a model group. 
						//In that case process its children
						if (subElement.getTerm() != null && subElement.getTerm().isModelGroup() &&
								! ArrayUtils.isEmpty(subElement.getTerm().asModelGroup().getChildren())){

							XSParticle[] particleChildren = subElement.getTerm().asModelGroup().getChildren();

							for (XSParticle anotherSubElement: particleChildren){
								addChildProperty(anotherSubElement);
							}
						}
						else{
							addChildProperty(subElement);
						}
					}
				}
			}
		}
		else{
			logger.warn("Unable to process sub properties for definition '{}' because sub elements are not defined inside an xs:sequence or an xs:all element", name);
		}


	}

	private void setBasicProperties(XSComponent component) throws Exception {
		//Attributes defined by XML Schema
		if (component instanceof XSDeclaration){
			name = ((XSDeclaration)component).getName();
		}
		else if (component instanceof XSAttributeUse){
			name = ((XSAttributeUse)component).getDecl().getName();
		}

		//Validate name
		if (! XMLChar.isValidNCName(name)){
			throw new Exception("Invalid XML Name "+ name+ ". Check XML Namespaces recommendation [4] (http://www.w3.org/TR/REC-xml-names)");
		}

		logger.debug("Setting basic properties for definition '{}'", name);

		//		Retrieve annotation to build display name and description
		if (component.getAnnotation() != null){
			annotation(component.getAnnotation());
		}
		else if (component instanceof XSAttributeUse && ((XSAttributeUse)component).getDecl().getAnnotation() != null){
			annotation(((XSAttributeUse)component).getDecl().getAnnotation());
		}

		if (isDefinitionSimple()){
			
			getDefaultValue(component);
				
			getValueRange(component);

		}

		//Attributes defined by repository
		if (CollectionUtils.isNotEmpty(component.getForeignAttributes())){
			
			//Process attributes specified in the context of the types and not in the context of CmsDefinitionItem.contentObjectPropertyAttGroup
			if (ValueType.Complex == valueType || ValueType.ContentType == valueType){
				//Retrieve value from schema
				String labelElementPathFromSchema = component.getForeignAttribute(CmsDefinitionItem.labelElementPath.getNamespaceURI(), CmsDefinitionItem.labelElementPath.getLocalPart());
				
				if (StringUtils.isNotBlank(labelElementPathFromSchema)){
					labelElementPath = labelElementPathFromSchema;
				}
			}
			
			if (MapUtils.isNotEmpty(builtInAttributes)){
			
			for (Entry<ItemQName, XSAttributeUse> builtInAttribute: builtInAttributes.entrySet())
			{
				ItemQName attribute = builtInAttribute.getKey();

				XSAttributeDecl attributeDefinition = builtInAttribute.getValue().getDecl();
				String defaultValue = (attributeDefinition.getDefaultValue() != null)?attributeDefinition.getDefaultValue().value : null; 

				//Retrieve value from schema
				String attributeValueFromSchema = component.getForeignAttribute(attribute.getNamespaceURI(), attribute.getLocalPart());

				//Value to be set finally
				String valueToBeSet = (attributeValueFromSchema == null)? defaultValue : attributeValueFromSchema;

				//BuiltIn attributes must be known a priori in order to map to ContentObjectPropertyDefinition
				if (valueToBeSet != null){
					if (attribute.equals(CmsDefinitionItem.obsolete)){
						if (ValueType.ContentType == valueType){
							//Complex Type cannot be obsolete. Issue a warning if any such value has been set
							//Regardless if actual value is false
							if (attributeValueFromSchema != null)
								attributeIsNotAcceptedInContentType(CmsDefinitionItem.obsolete);
						}
						else
							obsolete = Boolean.valueOf(valueToBeSet);

					}
					else if (attribute.equals(CmsDefinitionItem.restrictReadToRoles))
						restrictReadToRoles = valueToBeSet;
					else if (attribute.equals(CmsDefinitionItem.restrictWriteToRoles))
						restrictWriteToRoles =valueToBeSet;
					else if (attribute.equals(CmsDefinitionItem.order))
						order = Integer.parseInt(valueToBeSet);
					else{
						//Attributes corresponds only to Simple ContentObject Property
						if ( isDefinitionSimple()){

							//Not used for now. Needs further modeling
//							if (CmsDefinitionItem.repositoryObjectRestriction.getJcrName().equals(attributeJcrName))
//							((SimpleCmsPropertyDefinition)definition).setRepositoryObjectRestriction(valueToBeSet);


							if (ValueType.String == valueType){
								//Look for string specific attributes
								if (attribute.equals(CmsDefinitionItem.stringFormat)){
									stringFormat =StringFormat.valueOf(valueToBeSet);
								}
								else if (attribute.equals(CmsDefinitionItem.passwordEncryptorClassName)){
									passwordEncryptorClassName = valueToBeSet;
								}
								
							}
							else if (ValueType.Binary == valueType){
								if (attribute.equals(CmsDefinitionItem.unmanagedBinaryChannel)){
									binaryChannelIsUnmanaged = true;
								}
							}
							else if (ValueType.TopicReference == valueType){
								if (attribute.equals(CmsDefinitionItem.acceptedTaxonomies)){
									try{
										String[] acceptedTaxonomyNameArray = StringUtils.split(valueToBeSet, ",");
										//	Trim values
										if (!ArrayUtils.isEmpty(acceptedTaxonomyNameArray)){
											acceptedTaxonomies = new ArrayList<String>();

											for (String acceptedTaxonomyName : acceptedTaxonomyNameArray){
												String trimmedValue = StringUtils.trimToNull(acceptedTaxonomyName);
												if (trimmedValue != null)
													acceptedTaxonomies.add(trimmedValue);
											}
										}
									}catch(Exception e){
										logger.warn("While splitting accepted taxonomies {} in element {}",valueToBeSet, name);
										logger.warn("",e);
										acceptedTaxonomies = null;
									}
								}

							}else if (ValueType.ObjectReference == valueType){
								//Expecting a comma delimited string
								if (attribute.equals(CmsDefinitionItem.acceptedContentTypes)){
									try{
										String[] acceptedContentTypeArray = StringUtils.split(valueToBeSet, ",");
										//	Trim values
										if (!ArrayUtils.isEmpty(acceptedContentTypeArray)){
											acceptedContentTypes = new HashSet<String>();

											for (String acceptedContentTypeName : acceptedContentTypeArray){
												String trimmedValue = StringUtils.trimToNull(acceptedContentTypeName);
												if (trimmedValue != null)
													acceptedContentTypes.add(trimmedValue);
											}
										}
									}catch(Exception e){
										logger.warn("While splitting accepted content types {} in element {}",valueToBeSet, name);
										logger.warn("",e);
										acceptedContentTypes = null;
									}

								}

							}

						}
					}
				}
			}
			}
		}
	}

	private void getValueRange(XSComponent component) {
		XSType componentType = null;
		
		if (component instanceof XSElementDecl){
			componentType = ((XSElementDecl) component).getType();
		}
		else if (component instanceof XSAttributeUse ){
			componentType = ((XSAttributeUse)component).getDecl().getType();
		}

		//Also if it is a restriction then it is probably an enumeration
		if (componentType != null && componentType.isSimpleType() ){
			
			if (definitionValueRange == null){
				definitionValueRange = new LinkedHashMap<String, Localization>();
			}

			if (componentType.asSimpleType().isRestriction()){
			
				XSRestrictionSimpleType restriction = componentType.asSimpleType().asRestriction();

				loadRestrictionFacets(restriction);
				
			}
			else if (componentType.asSimpleType().isUnion()){
				
				XSUnionSimpleType unionType = componentType.asSimpleType().asUnion();
				
				int memberCount = unionType.getMemberSize();
				
				for (int i=0;i<memberCount;i++){
					XSSimpleType member = unionType.getMember(i);
					
					if (member.isRestriction()){
						loadRestrictionFacets(member.asRestriction());
					}
				}

			}
			
			if (definitionValueRange.size() == 0){
				definitionValueRange = null;
			}
		}
	}

	private void loadRestrictionFacets(XSRestrictionSimpleType restriction) {
		
		Collection<? extends XSFacet> facets = restriction.getDeclaredFacets();
		
		for (XSFacet facet : facets){
			if (XSFacet.FACET_ENUMERATION.equals(facet.getName())){
				
				Localization localization = new LocalizationImpl();

				XSAnnotation annotation = facet.getAnnotation();
				if (annotation != null && annotation.getAnnotation() != null && 
						annotation.getAnnotation() instanceof CmsAnnotation){
					
					CmsAnnotation cmsAnnotation = (CmsAnnotation) annotation.getAnnotation();
					
					if (cmsAnnotation.getDisplayName() != null)
					{
						//We are only interested for the displayName of the value
						localization = cmsAnnotation.getDisplayName();
					}
				}
				else{
					logger.warn("Found no localization for enumeration entry {} for element {}", facet.getValue().value, name);
				}
				
				definitionValueRange.put(facet.getValue().value, localization);

			}
			else if (XSFacet.FACET_MAXEXCLUSIVE.equals(facet.getName())){
				maxValueIsExclusive = true;
				
				if (valueType == ValueType.Long){
					maxValue = retrieveLongValue(facet.getValue().value);	
				}
				else if (valueType == ValueType.Double){
					maxValue = retrieveDoubleValue(facet.getValue().value);
				}
				else{
					logger.warn("Found maxExclusive restriction entry {} for non double and non long element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_MAXINCLUSIVE.equals(facet.getName())){
				maxValueIsExclusive = false;
				
				if (valueType == ValueType.Long){
					maxValue = retrieveLongValue(facet.getValue().value);	
				}
				else if (valueType == ValueType.Double){
					maxValue = retrieveDoubleValue(facet.getValue().value);
				}
				else{
					logger.warn("Found maxExclusive restriction entry {} for non double and non long element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_MINEXCLUSIVE.equals(facet.getName())){
				minValueIsExclusive = true;
				
				if (valueType == ValueType.Long){
					minValue = retrieveLongValue(facet.getValue().value);	
				}
				else if (valueType == ValueType.Double){
					minValue = retrieveDoubleValue(facet.getValue().value);
				}
				else{
					logger.warn("Found maxExclusive restriction entry {} for non double and non long element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_MININCLUSIVE.equals(facet.getName())){
				minValueIsExclusive = false;
				
				if (valueType == ValueType.Long){
					minValue = retrieveLongValue(facet.getValue().value);	
				}
				else if (valueType == ValueType.Double){
					minValue = retrieveDoubleValue(facet.getValue().value);
				}
				else{
					logger.warn("Found maxExclusive restriction entry {} for non double and non long element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_MINLENGTH.equals(facet.getName())){
				if (valueType == ValueType.String){
					minLength = retrieveIntegerValue(facet.getValue().value);	
				}
				else{
					logger.warn("Found minLength restriction entry {} for non string element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_MAXLENGTH.equals(facet.getName())){
				if (valueType == ValueType.String){
					maxLength = retrieveIntegerValue(facet.getValue().value);	
				}
				else{
					logger.warn("Found maxLength restriction entry {} for non string element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_LENGTH.equals(facet.getName())){
				if (valueType == ValueType.String){
					minLength = retrieveIntegerValue(facet.getValue().value);
					maxLength = minLength;
				}
				else{
					logger.warn("Found length restriction entry {} for non string element {}", facet.getValue().value, name);
				}
			}
			else if (XSFacet.FACET_PATTERN.equals(facet.getName())){
				if (valueType == ValueType.String){
					pattern = facet.getValue().value;
					
					if (StringUtils.isBlank(pattern)){
						pattern = null;
						logger.warn("Found blank pattern for string element {}", name);
					}
				}
				else{
					logger.warn("Found pattern restriction entry {} for non string element {}", facet.getValue().value, name);
				}
			}
		}
		
		
		//Continue if restriction extends another restriction
		//CAUTION : In case parent restriction has facets other than Enumeration
		//then these facets will override equivalent facets of this type
		//This code must be reviewed
		XSType baseType = restriction.getBaseType();
		
		if (baseType.isSimpleType() && 
				! baseType.asSimpleType().isPrimitive() &&
				baseType.asSimpleType().isRestriction() && 
				! StringUtils.equals(baseType.getTargetNamespace(), XMLConstants.W3C_XML_SCHEMA_NS_URI)){
			
			loadRestrictionFacets(baseType.asSimpleType().asRestriction());
		}

	}

	private void getDefaultValue(XSComponent component) {
		XmlString defaultValueFromXml = null;
		
		if (component instanceof XSElementDecl){
			defaultValueFromXml = ((XSElementDecl) component).getDefaultValue();
		}
		else if (component instanceof XSAttributeUse ){
			defaultValueFromXml = ((XSAttributeUse)component).getDefaultValue();
		}
			
		if (defaultValueFromXml != null){
				defaultValue = defaultValueFromXml.value;
	
				logger.debug("Set default value '{}' for definition '{}'",defaultValue, name);
		}
	}

	private Integer retrieveIntegerValue(String valueToBeSet) {
		try{
			return Integer.valueOf(valueToBeSet);
		}
		catch(NumberFormatException e){
			logger.warn("Invalid integer "+valueToBeSet, e);
			return null;
		}
	}

	/**
	 * @param value
	 * @return
	 */
	private Object retrieveLongValue(String value) {
		
		if(StringUtils.isBlank(value)){
			return null;
		}
		
		try{
			return Long.valueOf(value);
		}
		catch(Exception e){
			logger.error("",e);
			return null;
		}
	}
	
	private Object retrieveDoubleValue(String value) {
		
		if(StringUtils.isBlank(value)){
			return null;
		}
		
		try{
			return Double.valueOf(value);
		}
		catch(Exception e){
			logger.error("",e);
			return null;
		}
	}

	private boolean isDefinitionSimple(){
		return valueType != null 
		&& ! isDefinitionComplex()
		&& ValueType.ContentType != valueType;
	}

	private void attributeIsNotAcceptedInContentType(CmsDefinitionItem cmsAttribute) {
		logger.warn("'{}' cannot be defined in content type '{}'.Its value will be ignored", cmsAttribute.getJcrName(), name);
	}

	public void facet(XSFacet arg0) {

	}

	public void identityConstraint(XSIdentityConstraint arg0) {


	}

	public void notation(XSNotation arg0) {


	}

	public void schema(XSSchema arg0) {

	}

	public void xpath(XSXPath arg0) {


	}

	public void empty(XSContentType arg0) {


	}

	public void particle(XSParticle particle) {
		addChildProperty(particle);
	}
	
	private void addChildProperty(XSParticle particle) {
		//Process particles of Element type
		if (particle.getTerm().isElementDecl()){

			//this is the only place where these values can be set
			//There is no access to these properties from Element perspective
			boolean mandatory = particle.getMinOccurs() == 1;
			boolean multiple = particle.getMaxOccurs() == XSParticle.UNBOUNDED;


			final XSElementDecl elementDecl = particle.getTerm().asElementDecl();
			LocalizableCmsDefinition definitionForCurrentProperty = getDefinition();
			
			cmsDefinitionVisitor.cacheDefinitionWhichIsUnderProcess(definitionForCurrentProperty);
			
			CmsPropertyVisitor propertyVisitor = new CmsPropertyVisitor(builtInAttributes, 
					definitionForCurrentProperty, mandatory, multiple, childPropertyDefinitions.size(), 
					cmsDefinitionVisitor);
			elementDecl.visit(propertyVisitor);


			LocalizableCmsDefinition subPropertyDefinition = propertyVisitor.getDefinition();
			if (subPropertyDefinition == null){
				logger.warn("Unable to define sub property '{}'", "{"+particle.getTerm().asElementDecl().getTargetNamespace()+"}"+particle.getTerm().asElementDecl().getName());
			}
			else{
				logger.debug("Adding child property definition '{}' to definition '{}'", subPropertyDefinition.getQualifiedName(), 
						"{"+namespaceUri+"}"+name);
				childPropertyDefinitions.put(subPropertyDefinition.getName(), (CmsPropertyDefinition)subPropertyDefinition);
			}
		}
	}

	public void simpleType(XSSimpleType arg0) {


	}

	public void elementDecl(XSElementDecl element) {

		//Determine type of element
		determineValueTypeForElement(element);

		if (valueType != null){
			logger.debug("Constructing definition with type '{}' for element '{}'",valueType , element.getName());

			populateDefinition(element);

		}
		else{
			createDefinition = false;
		}

	}

	private void determineValueTypeForElement(XSElementDecl element) {

		XSType elementType = element.getType();
		
		if (element.isGlobal()){
			determineValueTypeForGlobalElement(element, elementType);
		}
		else{
			logger.debug("Element '{}' is a sub element.", element.getName());

			//Local element. Exists as a subElement inside a complex property
			//Check if type attribute is defined
			if (elementType != null){
				
				String typeName = elementType.getName();
				String typeNamespace = elementType.getTargetNamespace();

				logger.debug("Type for sub element '{}' has target namespace '{}' and local part '{}'",
						new Object[]{element.getName(),	typeNamespace, typeName});

				if (elementType.isSimpleType()){
					determineValueTypeForSimpleType(element.getName(), elementType);
				}
				else if (elementType.isComplexType()){

					if (elementType.isLocal()){
						determineValueTypeForInternalComplexTypeElement(elementType);
					}
					else{
						determineValueTypeForExternalComplexTypeElement(element, elementType, typeName, typeNamespace);
					}
				}

				//Check if value type has been defined
				if (valueType == null)
					throw new CmsException("Unknown complex type for element "+ element.getName()+ " Type details "+
							" Target namespace "+	typeNamespace + " and name "+ typeName);

			}
			else{
				throw new CmsException("Could not determine type found for element "+ element.getName());
			}
		}
	}

	private void determineValueTypeForInternalComplexTypeElement(XSType elementType) {
		
		//Complex Type is defined in the context of this element.
		final XSComplexType complexType = elementType.asComplexType();
		
		if (!elementExtendsComplexCmsPropertyType(complexType)){
			//Disable caching this definition since it does not extend complexCmsPropertyType
			//and thus it cannot be global
			cacheComplexDefinitionReference = false;
			
			complexDefinitionContainsCommonAttributes = complexTypeContainsCommonEntityAttributeGroup(complexType);
		}
		else{
			complexDefinitionContainsCommonAttributes = true;
		}
		
		
		valueType = ValueType.Complex;
		complexPropertyTypeName = complexType.getName();
	}

	private boolean complexTypeContainsCommonEntityAttributeGroup(XSComplexType complexType) {
		Collection<? extends XSAttGroupDecl> attGroups = complexType.getAttGroups();
		
		for (XSAttGroupDecl attGroupDecl : attGroups){
			ItemQName attQName = ItemUtils.createNewItem("", attGroupDecl.getTargetNamespace(), attGroupDecl.getName());
			if(CmsDefinitionItem.commonEntityAttributes.equals(attQName)){
				return true;
			}
		}

		return false;
	}

	private void determineValueTypeForExternalComplexTypeElement(
			XSElementDecl element, XSType elementType, String typeName,
			String typeNamespace) {
		
		ItemQName typeItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);

		//Type is a global type defined either in the same XSD file or in another XSD file
		//Check if complex type is one of CmsRepositoryEntity complex type
		if (typeItemQName.equals(CmsDefinitionItem.contentObjectReferenceType) ){
			valueType = ValueType.ObjectReference;
		}
		else if (typeItemQName.equals(CmsDefinitionItem.contentObjectType)){
			//Issue a warning for backwards compatibility
			logger.warn("Property {} {} represents a content object reference. Type 'contentObjectReferenceType' shoud be used instead." +
					"If you leave this unchanged, import service might not work properly.",
					new Object[]{element.getName(), element.getSourceDocument() !=null ? "in "+ element.getSourceDocument().getSystemId() : ""});
			valueType = ValueType.ObjectReference;

		}
		else if (typeItemQName.equals(CmsDefinitionItem.topicType)){
			valueType = ValueType.TopicReference;
		}
		else if (typeItemQName.equals(CmsDefinitionItem.binaryChannelType)){
			valueType = ValueType.Binary;
		}
		else{
			//Must be a complex type defined in another xml schema
			//Check that this is valid complex cms property
			if (elementExtendsComplexCmsPropertyType(elementType.asComplexType())){
				valueType = ValueType.Complex;
				//hold its namespace
				namespaceUri = elementType.getTargetNamespace();
				global=true;
				complexPropertyTypeName = elementType.asComplexType().getName();
				complexDefinitionContainsCommonAttributes = true;
			}
			else{
				
				//Special case where complex type contains simple content
				//whose type is Base64Binary
				if (complexTypeExtendsBase64Binary(elementType.asComplexType())){
					valueType = ValueType.Binary;
				}
				else{

					//Allow element to extend any complex type regardless of its location
					valueType = ValueType.Complex;
					namespaceUri = elementType.getTargetNamespace();
					complexPropertyTypeName = elementType.asComplexType().getName();
					
					//Disable caching this definition since it does not extend complexCmsPropertyType
					//and thus it cannot be global
					cacheComplexDefinitionReference = false;
					complexDefinitionContainsCommonAttributes = complexTypeContainsCommonEntityAttributeGroup(elementType.asComplexType());
	
				}
				/*
				//Type is a complex type which does not extend CmsDefinitionItem.complexCmsPropertyType 
				//This is only acceptable only if this type resides in the same XSD file
				String elementSchemaURI = (element.getSourceDocument() != null ? element.getSourceDocument().getSystemId() : null);
				String elementTypeSchemaURI = (elementType.getSourceDocument() != null ? elementType.getSourceDocument().getSystemId() : null);
				
				if (elementSchemaURI != null && elementTypeSchemaURI != null && StringUtils.equals(elementSchemaURI, elementTypeSchemaURI)){
					//This is accepted. Process with further processing complex type
					valueType = ValueType.Complex;
					namespaceUri = elementType.getTargetNamespace();
					complexPropertyTypeName = elementType.asComplexType().getName();
					
					//Disable caching this definition since it does not extend complexCmsPropertyType
					//and thus it cannot be global
					cacheComplexDefinitionReference = false;
					complexDefinitionContainsCommonAttributes = complexTypeContainsCommonEntityAttributeGroup(elementType.asComplexType());
				}
				else{
					throw new CmsException("Invalid  complex type for element "+ element.getName()+ " Type details "+
						" Target namespace "+	elementType.getTargetNamespace() + " and name "+ typeName+
						" This type does not extend built in complexCmsPropertyType and resides in XSD file "+ 
						elementTypeSchemaURI + 
						" where element "+element.getName() + " resides in file "+elementSchemaURI
						);
				}
				*/
			}
		}
	}

	private void determineValueTypeForSimpleType(String componentName, XSType simpleType) {
		
		ItemQName typeItemQName = null;
		
		String typeName = simpleType.getName();
		String typeNamespace = simpleType.getTargetNamespace();

		//Check if simple type is primitive
		if (!simpleType.asSimpleType().isPrimitive() ) {
			
			//Check if element is a restriction
			if (simpleType.asSimpleType().isRestriction() && 
					! StringUtils.equals(typeNamespace, XMLConstants.W3C_XML_SCHEMA_NS_URI)){
				
				XSRestrictionSimpleType restriction = simpleType.asSimpleType().asRestriction();
			
				XSType baseType = retrieveBaseTypeFromRestriction(restriction);
				
				typeName = baseType.getName();
				typeNamespace = baseType.getTargetNamespace();
			}
			else if (simpleType.asSimpleType().isUnion()){

				XSUnionSimpleType union = simpleType.asSimpleType().asUnion();
				
				//All members must have the same type.
				//Union with members of different types is not supported
				ItemQName unionCommonType = getUnionMemberCommonType(union);
						
				if (unionCommonType == null){
					throw new CmsException("Component "+ componentName+ " Target namespace "+	simpleType.getTargetNamespace() + 
						" is a union of "+ union.getMemberSize() + " simple types which are not of the same base type. Unions whose members are not of the same base type are not supported");
				}
				
				typeName = unionCommonType.getLocalPart();
				typeNamespace = unionCommonType.getNamespaceURI();
			}
		}
		
		if (typeName == null){
			throw new CmsException("Unknown simple type for component "+ componentName+ " Target namespace "+	simpleType.getTargetNamespace());
		}
		
		typeItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);

		//Check what kind of type this element is
		if (typeItemQName.equals(XSSchemaItem.String) ||
				typeItemQName.equals(XSSchemaItem.NormalizedString) ||
				typeItemQName.equals(XSSchemaItem.AnyURI)){
			valueType = ValueType.String;
			
			if (StringUtils.equals(simpleType.getName(), CmsDefinitionItem.passwordType.getLocalPart())){
				passwordType = true;	
			}
			
		}
		else if (typeItemQName.equals(XSSchemaItem.Double) || 
				typeItemQName.equals(XSSchemaItem.Float) ||
				typeItemQName.equals(XSSchemaItem.Decimal)){
			valueType = ValueType.Double;
		}
		else if (typeItemQName.equals(XSSchemaItem.Long) || 
				typeItemQName.equals(XSSchemaItem.Integer) ||
				typeItemQName.equals(XSSchemaItem.NonPositiveInteger) ||
				typeItemQName.equals(XSSchemaItem.NonNegativeInteger) ||
				typeItemQName.equals(XSSchemaItem.NegativeInteger) ||
				typeItemQName.equals(XSSchemaItem.Int) ||
				typeItemQName.equals(XSSchemaItem.PositiveInteger) ||
				typeItemQName.equals(XSSchemaItem.UnsignedLong) ||
				typeItemQName.equals(XSSchemaItem.Short) ||
				typeItemQName.equals(XSSchemaItem.UnsignedInt) ||
				typeItemQName.equals(XSSchemaItem.Byte) ||
				typeItemQName.equals(XSSchemaItem.UnsignedByte)){
			valueType = ValueType.Long;
		}
		else if (typeItemQName.equals(XSSchemaItem.GYear)){
			valueType = ValueType.String;
			pattern = CmsConstants.GYEAR_REG_EXP;
		}
		else if (typeItemQName.equals(XSSchemaItem.GMonth)){
			valueType = ValueType.String;
			pattern = CmsConstants.GMONTH_REG_EXP;
		}
		else if (typeItemQName.equals(XSSchemaItem.GYearMonth)){
			valueType = ValueType.String;
			pattern = CmsConstants.GYEAR_MONTH_REG_EXP;
		}
		else if (typeItemQName.equals(XSSchemaItem.GDay)){
			valueType = ValueType.String;
			pattern = CmsConstants.GDAY_REG_EXP;
		}
		else if (typeItemQName.equals(XSSchemaItem.GMonthDay)){
			valueType = ValueType.String;
			pattern = CmsConstants.GMONTH_DAY_REG_EXP;
		}
		else if (typeItemQName.equals(XSSchemaItem.Boolean)){
			valueType = ValueType.Boolean;
		}
		else if (typeItemQName.equals(XSSchemaItem.DateTime)){
			valueType = ValueType.Date;
			calendarPattern = CmsConstants.DATE_TIME_PATTERN;
		}
		else if (typeItemQName.equals(XSSchemaItem.Date)){
			valueType = ValueType.Date;
			calendarPattern = CmsConstants.DATE_PATTERN;
		}
		else
			throw new CmsException("Unknown type for component(element or attribute) "+ componentName+ ". Type details: "+
					" Target namespace "+	simpleType.getTargetNamespace() + " and name "+ typeName);
		
	}

	private XSType retrieveBaseTypeFromRestriction(XSRestrictionSimpleType restriction) {
		
		XSType baseType = restriction.getBaseType();
		
		if (baseType.isSimpleType() && 
				! baseType.asSimpleType().isPrimitive() &&
				baseType.asSimpleType().isRestriction() && 
				! StringUtils.equals(baseType.getTargetNamespace(), XMLConstants.W3C_XML_SCHEMA_NS_URI)){
			
			return retrieveBaseTypeFromRestriction(baseType.asSimpleType().asRestriction());
		}
		
		return baseType;
	}

	private ItemQName getUnionMemberCommonType(XSUnionSimpleType union) {
		
		int memberCount = union.getMemberSize();
		
		ItemQName commonType = null;
		
		for (int i=0;i<memberCount;i++){
			XSSimpleType member = union.getMember(i);
			
			String typeNamespace = member.getTargetNamespace();
			String typeName = member.getName();
			
			if (member.isRestriction()){
				typeNamespace = member.asRestriction().getBaseType().getTargetNamespace();
				typeName = member.asRestriction().getBaseType().getName();
			}
			
			if (commonType == null){
				commonType = ItemUtils.createNewItem("", typeNamespace, typeName);
			}
			else{
				ItemQName memberType = ItemUtils.createNewItem("", typeNamespace, typeName);
				
				if (! memberType.equals(commonType)){
					return null;
				}
			}
		}
		
		return commonType;
	}

	private void determineValueTypeForGlobalElement(XSElementDecl element,
			XSType elementType) {
		
		logger.debug("Determine type for global element {}{}", "{"+element.getTargetNamespace() + "}", element.getName());

		if (globalElementIsBuiltIn(ItemUtils.createNewItem("", element.getTargetNamespace(), element.getName()))){
			return;
		}

		valueType = ValueType.ContentType;
		typeDefinitionExtendsBaseObjectType = false;
		
		//Check element type and super types to see whether it extends
		//Astroboa base contentObjectType.
		
		String typeName = null;
		String typeNamespace = null;
		
		if (elementType != null){
			
			XSType currentElementType = elementType.getBaseType();
			do {
				//According to XSOM API method getBaseType always returns not null
				typeName = currentElementType.getName();
				typeNamespace = currentElementType.getTargetNamespace();
				
				logger.debug("Checking base type {}{}",	new Object[]{"{"+typeNamespace+"}", typeName});

				ItemQName elementTypeAsItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);
				
				//Element extends complex type ContentObjectType
				//This means that it is a content type
				if (elementTypeAsItemQName.equals(CmsDefinitionItem.contentObjectType)){
					typeDefinitionExtendsBaseObjectType = true;
				}
				else{
					//Element may be extending a complex type which represents a ContentObjectType
					//either this super type or one of its parent extend ContentObjectType
					ItemQName parentTypeAsItemQName = ItemUtils.createNewItem("", currentElementType.getBaseType().getTargetNamespace(), currentElementType.getBaseType().getName());
					
					if (! elementTypeAsItemQName.equals(parentTypeAsItemQName)){
						currentElementType = currentElementType.getBaseType();
					}
					else{
						break;
					}
					
				}
			}while (! typeDefinitionExtendsBaseObjectType);
		}

		if (! typeDefinitionExtendsBaseObjectType){
			
			logger.info("Global element '{}' does not extend Astroboa base object type 'contentObjectType'. Element Type Name : '{}', " +
					" element type namespace :'{}'.", 
					new Object[]{element.getName(), typeName, typeNamespace});
		}
	}

	private boolean globalElementIsBuiltIn(ItemQName elementTypeAsItemQName) {
		
		return elementTypeAsItemQName != null && (
				CmsDefinitionItem.repositoryUser.equals(elementTypeAsItemQName) ||
				CmsDefinitionItem.topic.equals(elementTypeAsItemQName) ||
				CmsDefinitionItem.space.equals(elementTypeAsItemQName) ||
				CmsDefinitionItem.taxonomy.equals(elementTypeAsItemQName) ||
				CmsDefinitionItem.displayName.equals(elementTypeAsItemQName) || 
				CmsDefinitionItem.description.equals(elementTypeAsItemQName));
	}

	private void retrieveSchemaURI(XSComponent component) {
		try{

			if (component.getSourceDocument() == null || component.getSourceDocument().getSystemId() == null){
				logger.warn("Problem while retrieving type '{}''s schema URI. XSOM returned null as element's source document", component.toString());
			}
			else{
				//It represents the URI of the schema
				String systemId = component.getSourceDocument().getSystemId();
				definitionFileURI = URI.create(systemId);
			}

		}
		catch (Exception e){
			logger.warn("Exception while retrieving type '{}''s schema URI",component.toString(), e);
		}

	}

	private void retrieveElementNamespace(XSComponent component) {
		if (component instanceof XSDeclaration)
			namespaceUri = ((XSDeclaration)component).getTargetNamespace();

		if (StringUtils.isBlank(namespaceUri)){
			//Retrieve parent's namespace
			namespaceUri = component.getOwnerSchema().getTargetNamespace();
		}

		if (StringUtils.isBlank(namespaceUri)){
			logger.warn("Type '{}' does not have a namespace", component.toString());
		}
	}

	private boolean elementExtendsComplexCmsPropertyType(XSComplexType complexType)
	{
		//According to XSOM API method getBaseType always returns not null
		String typeName = complexType.getBaseType().getName();
		String typeNamespace = complexType.getBaseType().getTargetNamespace();

		ItemQName complexTypeAsItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);

		return complexTypeAsItemQName.equals(CmsDefinitionItem.complexCmsPropertyType); //Complex Type must extend ComplexCmsProperty type 

	}
	
	private boolean complexTypeExtendsBase64Binary(XSComplexType complexType){
		
		//According to XSOM API method getBaseType always returns not null
		String typeName = complexType.getBaseType().getName();
		String typeNamespace = complexType.getBaseType().getTargetNamespace();

		ItemQName complexTypeAsItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);

		return complexTypeAsItemQName.equals(XSSchemaItem.Base64Binary);  

	}


	public void modelGroup(XSModelGroup arg0) {


	}

	public void modelGroupDecl(XSModelGroupDecl arg0) {


	}

	public void wildcard(XSWildcard arg0) {


	}

	private ComplexCmsPropertyDefinition locateParentDefinintionWithSameType() {
		
		CmsDefinition ancestorDefinition = parentDefinition;
		
		while (ancestorDefinition != null && ancestorDefinition instanceof ComplexCmsPropertyDefinition){
			if ( StringUtils.equals(((ComplexCmsPropertyDefinitionImpl)ancestorDefinition).getTypeName(), complexPropertyTypeName)){
				return (ComplexCmsPropertyDefinition) ancestorDefinition;
			}
			
			ancestorDefinition = ((ComplexCmsPropertyDefinition)ancestorDefinition).getParentDefinition();
		}
		
		return null;
	}
}
