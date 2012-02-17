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

package org.betaconceptframework.astroboa.engine.definition.visitor;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.cache.region.DefinitionCacheRegion;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.definition.ObjectReferencePropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsDefinitionItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsDefinitionVisitor implements XSVisitor{

	private  final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DefinitionCacheRegion definitionCacheRegion;

	private Map<String, XSAttGroupDecl> attributeGroupDeclarations = new TreeMap<String, XSAttGroupDecl>();
	private Map<String, XSComplexType> complexTypeDeclarations = new TreeMap<String, XSComplexType>();
	private Map<String, XSElementDecl> elementDeclarations = new TreeMap<String, XSElementDecl>();

	private Map<ItemQName, XSAttributeUse> builtInAttributes = new HashMap<ItemQName, XSAttributeUse>();

	private Map<String, Set<String>> topicPropertyPathsPerTaxonomies = new HashMap<String, Set<String>>();
	private Set<String> multivalueProperties = new HashSet<String>();
	private Map<String, byte[]> xmlSchemaDefinitionsPerFilename = new HashMap<String, byte[]>();
	private Map<String, List<String>> contentTypeHierarchy = new HashMap<String, List<String>>();
	private Map<QName, String> xmlSchemaDefinitionURLsPerQName = new HashMap<QName, String>();

	private List<String> definitionsUnderProcess = new ArrayList<String>();
	
	private Map<String, LocalizableCmsDefinition> internalDefinitionsCache = new HashMap<String, LocalizableCmsDefinition>();

	private List<ObjectReferencePropertyDefinition> objectReferencePropertyDefinitions = new ArrayList<ObjectReferencePropertyDefinition>();
	
	public void createContentDefintions() throws Exception {

		if (attributeGroupDeclarations.isEmpty())
			throw new Exception("Cms internal attribute group "+CmsDefinitionItem.contentObjectPropertyAttGroup.getLocalPart() + "  is not found");

		//Order of this is significant

		//Visit attribute group declarations
		for (XSAttGroupDecl attributeGroupDecl: attributeGroupDeclarations.values())
			attributeGroupDecl.visit(this);

		//Visit Elements
		for (XSElementDecl elementDecl : elementDeclarations.values()){
			elementDecl.visit(this);
		}

		//Visit ComplexTypes which were not processed at all during element visit
		for (Entry<String, XSComplexType> complexDecl : complexTypeDeclarations.entrySet()){
			
			//Visit Complex Type only if it has not been visited before
			//and it is not an Astroboa model complex type
			String targetNamespace = complexDecl.getValue().getTargetNamespace();

			if (StringUtils.isNotBlank(targetNamespace) && 
					! BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_URI.equals(targetNamespace) &&
					! BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_URI.equals(targetNamespace) && 
					! definitionCacheRegion.hasComplexTypeDefinition(complexDecl.getKey())){
					complexDecl.getValue().visit(this);
				}
		}


		//Further process ContentObjectPropertyDefinitions which contain content type restriction
		processContentObjectPropertyDefinitions();
		
		//Notify cache for several additional information
		definitionCacheRegion.putTopicPropertyPathsPerTaxonomy(transformSetInMapToList(topicPropertyPathsPerTaxonomies));
		definitionCacheRegion.putXMLSchemaDefinitionsPerFilename(xmlSchemaDefinitionsPerFilename);
		definitionCacheRegion.putMultivalueProperties(new ArrayList<String>(multivalueProperties));
		definitionCacheRegion.putContentTypeHierarchy(contentTypeHierarchy);
		definitionCacheRegion.putLocationURLForDefinition(xmlSchemaDefinitionURLsPerQName);

		definitionCacheRegion.printDefinitionCacheToLog();

	}

	/**
	 * 
	 */
	private void processContentObjectPropertyDefinitions() {
		
		if (CollectionUtils.isNotEmpty(objectReferencePropertyDefinitions)){
			
			for (ObjectReferencePropertyDefinition objectReferencePropertyDefinition: objectReferencePropertyDefinitions){
				
				List<String> acceptedContentTypes = objectReferencePropertyDefinition.getAcceptedContentTypes();
				
				if (CollectionUtils.isNotEmpty(acceptedContentTypes)){
					
					//Use Set to ensure that values are unique
					Set<String> expandedContentTypes = new HashSet<String>();
					
					for (String acceptedContentType : acceptedContentTypes){
						if (contentTypeHierarchy.containsKey(acceptedContentType)){
							//AcceptedContentType is a super type and thus we need to 
							//keep all content types which extend this super type
							expandedContentTypes.addAll(contentTypeHierarchy.get(acceptedContentType));
						}
						else {
							expandedContentTypes.add(acceptedContentType);
						}
					}
					
					((ObjectReferencePropertyDefinitionImpl)objectReferencePropertyDefinition).addExpandedAcceptedContentTypes(new HashSet<String>(expandedContentTypes));
					
					if (logger.isDebugEnabled()){
						logger.debug("Accepted content types {} of property {} have been expanded to {}", 
							new Object[]{objectReferencePropertyDefinition.getAcceptedContentTypes(), objectReferencePropertyDefinition.getFullPath(), objectReferencePropertyDefinition.getExpandedAcceptedContentTypes()});
					}
				}
			}
		}
		
	}

	private Map<String, List<String>> transformSetInMapToList(
			Map<String, Set<String>> mapWithSet) {
		
		Map<String, List<String>> mapWithList = new HashMap<String, List<String>>();
		if (MapUtils.isNotEmpty(mapWithSet)){
			for (Entry<String, Set<String>> entry : mapWithSet.entrySet()){
				mapWithList.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
			}
		}
		
		return mapWithList;
	}

	public void annotation(XSAnnotation arg0) {
	}

	public void attGroupDecl(XSAttGroupDecl attGroup) {

		//Process only built-in attribute group
		if (attGroup.getName().equals(CmsDefinitionItem.contentObjectPropertyAttGroup.getLocalPart()))
		{
			String targetNameSpace = attGroup.getTargetNamespace();
			Iterator<? extends XSAttributeUse> attrDeclarationsIter = attGroup.iterateDeclaredAttributeUses();
			while (attrDeclarationsIter.hasNext())
			{
				XSAttributeUse attributeUse = attrDeclarationsIter.next();
				String name = attributeUse.getDecl().getName();

				//Use default prefix
				ItemQName attribute = ItemUtils.createNewItem(BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX, targetNameSpace, name);
				builtInAttributes.put(attribute, attributeUse);

			}
		}
	}

	public void attributeDecl(XSAttributeDecl arg0) {
	}

	public void attributeUse(XSAttributeUse arg0) {

	}

	public void complexType(XSComplexType complexType) {

		//Visit only complex properties which extend complexCmsPropertyType
		//Normally this method is called when visiting global complex types
		if (isElementValidComplexType(complexType)){
			CmsPropertyVisitor contentObjectPropertyVisitor = new CmsPropertyVisitor(builtInAttributes, null, false, false, 0, this);
			complexType.visit(contentObjectPropertyVisitor);

			cacheDefinition(contentObjectPropertyVisitor.getDefinition());
		}
		else{
			logger.debug("Type {} does not extend builtin complex type 'complexCmsPropertyType'", complexType);
		}

	}

	private boolean isElementValidComplexType(XSComplexType complexType)
	{
		//According to XSOM API method getBaseType always returns not null
		String typeName = complexType.getBaseType().getName();
		String typeNamespace = complexType.getBaseType().getTargetNamespace();

		ItemQName complexTypeAsItemQName = ItemUtils.createNewItem("", typeNamespace, typeName);

		return complexTypeAsItemQName.equals(CmsDefinitionItem.complexCmsPropertyType); //Complex Type must extend ComplexCmsProperty type 

	}

	public void cacheInternalDefinition(LocalizableCmsDefinition definition) {
		if (definition != null){
			String typeQName = getDefinitionQName(definition);
			
			//Put definition to InternalCache
			try {
				internalDefinitionsCache.put(typeQName, definition);
			} catch (Exception e) {
				throw new CmsException(e);
			}

			if (ValueType.ContentType == definition.getValueType() ||
					ValueType.Complex == definition.getValueType()){
				if (definitionsUnderProcess.contains(typeQName)){
					definitionsUnderProcess.remove(typeQName);
				}				
			}
		}
	}
	
	
	public boolean internalCacheHasDefinition(String typeName, String typeNamespace) {
		return typeName != null && typeNamespace != null && 
			internalDefinitionsCache.containsKey("{"+typeNamespace+"}"+typeName);
	}

	public LocalizableCmsDefinition getDefinitionFromInternalCache(String typeName, String typeNamespace) {
		if (typeName != null && typeNamespace != null) {
			return internalDefinitionsCache.get("{"+typeNamespace+"}"+typeName);
		} else {
			return null;
		}
	}
	
	public void cacheDefinition(LocalizableCmsDefinition definition) {
		if (definition != null){
			
			//Put definition to CacheManager according to its valueType
			try {
				definitionCacheRegion.putDefinition(definition.getValueType(), definition.getName(), definition);
			} catch (Exception e) {
				throw new CmsException(e);
			}

			if (ValueType.ContentType == definition.getValueType() ||
					ValueType.Complex == definition.getValueType()){

				String typeQName = getDefinitionQName(definition);
				
				if (definitionsUnderProcess.contains(typeQName)){
					definitionsUnderProcess.remove(typeQName);
				}
				
				xmlSchemaDefinitionURLsPerQName.put(definition.getQualifiedName(), definition.url(ResourceRepresentationType.XSD));
				
				//Get base content types
				if (ValueType.ContentType == definition.getValueType()){
					List<String> superContentTypes = ((ContentObjectTypeDefinition)definition).getSuperContentTypes();
					
					if (CollectionUtils.isNotEmpty(superContentTypes)){
						for (String superContentType : superContentTypes){
							if (! contentTypeHierarchy.containsKey(superContentType)){
								contentTypeHierarchy.put(superContentType, new ArrayList<String>());
							}
							
							if (! contentTypeHierarchy.get(superContentType).contains(definition.getName())){
								contentTypeHierarchy.get(superContentType).add(definition.getName());
							}
						}
					}
				}
				
				//Create property paths
				DefinitionPropertyPathBuilder definitionPropertyPathBuilder = new DefinitionPropertyPathBuilder(ValueType.Complex == definition.getValueType());
				definition.accept(definitionPropertyPathBuilder);

				objectReferencePropertyDefinitions.addAll(definitionPropertyPathBuilder.getObjectReferencePropertyDefinitions());
				
				//definitionPropertyPathBuilder.loadPropertyPathsForDefinition(((ContentObjectTypeDefinition)definition).getPropertyDefinitions(), definition);

				//Get Multivalue properties
				if (CollectionUtils.isNotEmpty(definitionPropertyPathBuilder.getMutlivalueProperties())){
					multivalueProperties.addAll(definitionPropertyPathBuilder.getMutlivalueProperties());
				}
				
				//Get topic property paths per taxonomy and add them to list
				Map<String, Set<String>> childTopicPropertyPaths = definitionPropertyPathBuilder.getTopicPropertyPathsPerTaxonomy();
				if (MapUtils.isNotEmpty(childTopicPropertyPaths)){
					for (Entry<String, Set<String>> topicPropertyPathPerTaxonomy : childTopicPropertyPaths.entrySet()){

						List<String> topicPropertyPathList = new ArrayList<String>(topicPropertyPathPerTaxonomy.getValue());
						String taxonomyName = topicPropertyPathPerTaxonomy.getKey();

						if (CollectionUtils.isNotEmpty(topicPropertyPathList)){

							if (!topicPropertyPathsPerTaxonomies.containsKey(taxonomyName)){
								topicPropertyPathsPerTaxonomies.put(taxonomyName, new HashSet<String>());
							}

							for(String topicPropertyPath: topicPropertyPathList){
								if (!topicPropertyPathsPerTaxonomies.get(taxonomyName).contains(topicPropertyPath)){
									topicPropertyPathsPerTaxonomies.get(taxonomyName).add(topicPropertyPath);
								}
							}
						}
					}
				}
			}
		}
	}

	public void facet(XSFacet arg0) {
	}

	public void identityConstraint(XSIdentityConstraint arg0) {

	}

	public void notation(XSNotation arg0) {

	}

	public void schema(XSSchema schema) {

		//		Do not process schema of XML Schema!!!
		if (!XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(schema.getTargetNamespace()))
		{
			//Collect all attributeGroups
			if (MapUtils.isNotEmpty(schema.getAttGroupDecls()))
				attributeGroupDeclarations.putAll(schema.getAttGroupDecls());

			//Collect all elements
			if (MapUtils.isNotEmpty(schema.getElementDecls()))
				elementDeclarations.putAll(schema.getElementDecls());

			//Collect all complexTypes
			if (MapUtils.isNotEmpty(schema.getComplexTypes()))
				complexTypeDeclarations.putAll(schema.getComplexTypes());

		}
	}

	public void xpath(XSXPath arg0) {
	}

	public void elementDecl(XSElementDecl element) {
		//NOTE in this method only global elements are processed
		if (element.isGlobal())
		{
			CmsPropertyVisitor contentObjectPropertyVisitor = new CmsPropertyVisitor(builtInAttributes, null, false, false, 0, this);
			element.visit(contentObjectPropertyVisitor);

			LocalizableCmsDefinition definition = contentObjectPropertyVisitor.getDefinition();

			cacheDefinition(definition);

			//Check if this element refers to a complex type 
			//In this case complexType Definition should be removed from ComlpexTypeDeclaration map

			if (element.getType()!= null)
			{
				String complexTypeRefName = element.getType().getName();

				if (complexTypeRefName != null)
					complexTypeDeclarations.remove(complexTypeRefName);

			}
		}
	}


	public void modelGroup(XSModelGroup arg0) {

	}

	public void modelGroupDecl(XSModelGroupDecl arg0) {

	}

	public void wildcard(XSWildcard arg0) {

	}

	public void empty(XSContentType arg0) {

	}

	public void particle(XSParticle arg0) {

	}

	public void simpleType(XSSimpleType arg0) {

	}

	public void clear(){
		
		logger.debug("Clearing Definition Visitor");
		
		try {
			definitionCacheRegion.removeRegion();
		} catch (Exception e) {
			logger.warn("Failed to clear cache region",e);
		}
		
		attributeGroupDeclarations.clear();
		complexTypeDeclarations.clear();
		elementDeclarations.clear();
		builtInAttributes.clear();
		
		topicPropertyPathsPerTaxonomies.clear();
		xmlSchemaDefinitionsPerFilename.clear();
		xmlSchemaDefinitionURLsPerQName.clear();
		multivalueProperties.clear();
		contentTypeHierarchy.clear();
		internalDefinitionsCache.clear();
		
		definitionsUnderProcess.clear();
		
		objectReferencePropertyDefinitions.clear();
	}

	public void addXMLSchemaDefinitionForFileName(byte[] fileContent,String filename) {
	
		logger.debug("Adding schema content for file {}", filename);
		xmlSchemaDefinitionsPerFilename.put(filename, fileContent);
	}

	public void addXMLSchemaDefinitionForFileName(URL definitionURL) {
		
		InputStream builtInStream = null;
		try{
			if (definitionURL == null){
				logger.warn("Found no XML schema file for definition ");
				return;
			}
			
			String filename = StringUtils.substringAfterLast(definitionURL.toExternalForm(), CmsConstants.FORWARD_SLASH);

			builtInStream = definitionURL.openStream();

			logger.debug("Adding schema content for file {}", filename);
			xmlSchemaDefinitionsPerFilename.put(filename, IOUtils.toByteArray(builtInStream));

		}
		catch(Exception e){
			logger.error("",e);
			throw new CmsException(e.getMessage());
		}
		finally{
			IOUtils.closeQuietly(builtInStream);
		}
			
		
	}

	public void addXMLSchemaDefinitions(
			Map<String, byte[]> builtInModelAndApiSchemaFiles) {
		if (MapUtils.isNotEmpty(builtInModelAndApiSchemaFiles)){
			logger.debug("Adding schema content for files {}", builtInModelAndApiSchemaFiles.keySet());
			xmlSchemaDefinitionsPerFilename.putAll(builtInModelAndApiSchemaFiles);
		}
		
	}

	public DefinitionCacheRegion getDefinitionCacheRegion() {
		return definitionCacheRegion;
	}

	public void cacheDefinitionWhichIsUnderProcess(
			LocalizableCmsDefinition definition) {
		
		if (definition !=null){
			String typeQName = getDefinitionQName(definition);
			
			if (! definitionsUnderProcess.contains(typeQName)){
				definitionsUnderProcess.add(typeQName);
			}
		}
	}

	private String getDefinitionQName(LocalizableCmsDefinition definition) {
		String typeQName = "{"+definition.getQualifiedName().getNamespaceURI()+"}"+definition.getName();
		return typeQName;
	}

	public boolean isDefinitionUnderProcess(String typeName,
			String typeNamespace) {
		return typeName != null && typeNamespace != null && definitionsUnderProcess.contains("{"+typeNamespace+"}"+typeName);
	}

}
