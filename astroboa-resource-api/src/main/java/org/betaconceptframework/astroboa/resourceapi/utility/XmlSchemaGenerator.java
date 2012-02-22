/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited.
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
package org.betaconceptframework.astroboa.resourceapi.utility;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.util.CmsConstants;



/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public class XmlSchemaGenerator {

	public final static String declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	private StringBuilder sb = new StringBuilder();
	
	public String generateXmlSchema(Map<String,Object> objectType){
		
		String typeName = (String) objectType.get("name");

		addLine(declaration);
		addLine("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
		addLine("targetNamespace=\""+BetaConceptNamespaceConstants.ASTROBOA_SCHEMA_URI+"/content/"+typeName+"\"");
		addLine("xmlns:tns=\""+BetaConceptNamespaceConstants.ASTROBOA_SCHEMA_URI+"/content/"+typeName+"\"");
		addLine("xmlns:bccmsmodel=\""+BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI+"\"");
		
		if (objectType.containsKey("version")){
			addLine("version=\""+objectType.get("version")+"\"");
		}
		
		addLine(">");
		
		addLine("<xs:import namespace=\""+BetaConceptNamespaceConstants.ASTROBOA_MODEL_DEFINITION_URI+"\"");
		addLine("schemaLocation=\"astroboa-model-"+CmsConstants.ASTROBOA_VERSION+".xsd\" />");
		
		createElementForType(objectType);
		
		addNewLine();
		
		addLine("</xs:schema>");
		
		return sb.toString();
	}
	
	
	private void createElementForType(Map<String, Object> objectType) {
		
		addElementForComplexProperty(objectType, false);

	}
	
	private void addComplexTypeAndArrayOfProperties(Map<String, Object> arrayOfProperties){
		
		if (arrayOfProperties!= null && arrayOfProperties.containsKey("property") && 
				arrayOfProperties.get("property") != null){
			
			List<Map<String, Object>> properties = (List<Map<String, Object>>) arrayOfProperties.get("property");
			
			if (!properties.isEmpty()){
				addLine("\t<xs:complexType>");
				//addLine("\t\t<xs:complexContent>");
				addLine("\t\t\t<xs:sequence>");
				
				for (Map<String,Object> property : properties){
					
					if (property.containsKey("valueType")){
						String valueType = (String) property.get("valueType");
						
						if (StringUtils.equals(valueType, "String")){
							addStringProperty(property);
						}
						else if (StringUtils.equals(valueType, "Long")){
							addLongProperty(property);
						}
						else if (StringUtils.equals(valueType, "Double")){
							addDoubleProperty(property);
						}
						else if (StringUtils.equals(valueType, "Boolean")){
							addBooleanProperty(property);
						}						
						else if (StringUtils.equals(valueType, "Date")){
							addDateProperty(property);
						}						
						else if (StringUtils.equals(valueType, "Binary")){
							addBinaryProperty(property);
						}
						else if (StringUtils.equals(valueType, "TopicReference")){
							addTopicReferenceProperty(property);
						}						
						else if (StringUtils.equals(valueType, "ObjectReference")){
							addObjectReferenceProperty(property);
						}						
						else if (StringUtils.equals(valueType, "Complex")){
							addElementForComplexProperty(property,true);
						}
						
					}
				}
				
			    addLine("\t\t\t</xs:sequence>");
		   		//addLine("\t\t</xs:complexContent>");
				addLine("\t</xs:complexType>");
			}
		}
	}
	
	
	private void addElementForComplexProperty(Map<String, Object> property, boolean displayCardinality) {

		String minOccurs = "";
		String maxOccurs = "";

		if (displayCardinality){
			minOccurs = " minOccurs=\""+(BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0")+"\" ";
			maxOccurs = " maxOccurs=\""+(BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1")+"\" ";
		}

		
		addLine("<xs:element name=\""+property.get("name")+"\""+minOccurs+maxOccurs+">");
		
		addLabel((Map<String, Object>)property.get("label"));
		
		addComplexTypeAndArrayOfProperties((Map<String, Object>) property.get("arrayOfProperties"));
		
		addLine("</xs:element>");

		
	}


	private void addTopicReferenceProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		String acceptedTaxonomies = (String) property.get("acceptedTaxonomies"); 
		
		if (StringUtils.isNotBlank(acceptedTaxonomies)){
			acceptedTaxonomies = " bccmsmodel:acceptedTaxonomies=\""+acceptedTaxonomies+"\" ";
		}
		else{
			acceptedTaxonomies = "";
		}
		
		addLine("<xs:element name=\""+name+"\" type=\"bccmsmodel:topicType\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\""+ acceptedTaxonomies+">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addObjectReferenceProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		String acceptedContentTypes = (String) property.get("acceptedContentTypes"); 
		
		if (StringUtils.isNotBlank(acceptedContentTypes)){
			acceptedContentTypes = " bccmsmodel:acceptedContentTypes=\""+acceptedContentTypes+"\" ";
		}
		else{
			acceptedContentTypes = "";
		}
		
		addLine("<xs:element name=\""+name+"\" type=\"bccmsmodel:contentObjectReferenceType\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\""+ acceptedContentTypes+">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addStringProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		String stringFormat = (String) property.get("stringFormat"); 
		
		if (StringUtils.isNotBlank(stringFormat)){
			stringFormat = " bccmsmodel:stringFormat=\""+stringFormat+"\" ";
		}
		else{
			stringFormat = "";
		}
		
		addLine("<xs:element name=\""+name+"\" type=\"xs:string\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\""+stringFormat+">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addLongProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		
		addLine("<xs:element name=\""+name+"\" type=\"xs:long\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addDoubleProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		
		addLine("<xs:element name=\""+name+"\" type=\"xs:double\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addDateProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		
		addLine("<xs:element name=\""+name+"\" type=\"xs:datetime\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addBooleanProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		
		addLine("<xs:element name=\""+name+"\" type=\"xs:boolean\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}

	private void addBinaryProperty(Map<String, Object> property) {
		
		String name = (String) property.get("name");
		String minOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("mandatory"))) ? "1" : "0";
		String maxOccurs = BooleanUtils.isTrue(Boolean.valueOf((String)property.get("multiple"))) ? "unbounded" : "1";
		
		addLine("<xs:element name=\""+name+"\" type=\"bccmsmodel:binaryChannelType\" minOccurs=\""+minOccurs+"\" maxOccurs=\""+maxOccurs+"\">");
		addLabel((Map<String, Object>) property.get("label"));
		addLine("</xs:element>");
		
	}


	private void addLabel(Map<String, Object> labels){
		
		if (labels != null && ! labels.isEmpty()){
			addLine("\t<xs:annotation>");

			for (Entry<String, Object> labelEntry : labels.entrySet()){
				
				addLine("\t\t<xs:documentation xml:lang=\""+labelEntry.getKey()+"\">");
				addLine("\t\t\t<bccmsmodel:displayName>"+labelEntry.getValue()+"</bccmsmodel:displayName>");
				addLine("\t\t\t<bccmsmodel:description>"+labelEntry.getValue()+"</bccmsmodel:description>");
				addLine("\t\t</xs:documentation>");
			}
			
			addLine("\t</xs:annotation>");
		}
	}


	private void addLine(String line){
		sb.append(line);
		addNewLine();
	}
	
	private void addNewLine(){
		sb.append("\n");
	}
}
