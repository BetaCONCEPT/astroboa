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
package org.betaconceptframework.astroboa.model.jaxb.visitor;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;

import net.sf.json.util.JSONBuilder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.betaconceptframework.astroboa.api.model.definition.BooleanPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.DoublePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LongPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicPropertyDefinition;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.model.impl.definition.DoublePropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LongPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * 
 * Class responsible to export definition to XML or JSON
 * 
 * Currently only JSON is supported
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsDefinitionSerializer extends AbstractCmsPropertyDefinitionVisitor{

	private boolean prettyPrint = false;
	private Deque<Integer> number_of_spaces_to_use_for_identation = new ArrayDeque<Integer>();

	private StringWriter writer = new StringWriter();
	
	private JSONBuilder jsonBuilder = null;
	
	private boolean jsonOutput = true;
	
	public CmsDefinitionSerializer(boolean prettyPrint, boolean jsonOutput) {
		
		this.prettyPrint = prettyPrint;
		
		this.jsonOutput = jsonOutput;
		
		initializeBuilder();

	}

	private void initializeBuilder() {
		
		if (jsonOutput){
		
			jsonBuilder = new JSONBuilder(writer);
		
			jsonBuilder.object();
		}
		else{
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		}
		
		number_of_spaces_to_use_for_identation.push(1);
	}

	@Override
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		
		exportDefinitionObjectAndBasicProperties(contentObjectTypeDefinition,true);
	
	}

	@Override
	public void visitComplexPropertyDefinition(ComplexCmsPropertyDefinition complexPropertyDefinition) {

		exportDefinitionObjectAndBasicProperties(complexPropertyDefinition,true);
	}

	@Override
	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {
		
		if (simplePropertyDefinition != null){
			
			exportDefinitionObjectAndBasicProperties(simplePropertyDefinition, jsonOutput);
			
			switch (simplePropertyDefinition.getValueType()) {
			case Boolean:
				BooleanPropertyDefinition booleanDefinition = (BooleanPropertyDefinition)simplePropertyDefinition;
				
				if (booleanDefinition.isSetDefaultValue()){
					exportAttribute("defaultValue", String.valueOf(booleanDefinition.getDefaultValue()));
				}

				break;
			case Date:

				CalendarPropertyDefinition calendarDefinition = (CalendarPropertyDefinition)simplePropertyDefinition;
				
				exportAttribute("pattern",calendarDefinition.getPattern());

				if (calendarDefinition.isSetDefaultValue()){
					exportAttribute("defaultValue", DateFormatUtils.format(calendarDefinition.getDefaultValue().getTimeInMillis(), calendarDefinition.getPattern()));
				}

				
				break;
			case Double:
				
				DoublePropertyDefinition doubleDefinition = (DoublePropertyDefinition)simplePropertyDefinition;
				
				if (doubleDefinition.isSetDefaultValue()){
					exportAttribute("defaultValue", String.valueOf(doubleDefinition.getDefaultValue()));
				}
				
				if (doubleDefinition.getMinValue() != null && doubleDefinition.getMinValue() != Double.MIN_VALUE){
					exportAttribute("minValue",String.valueOf(doubleDefinition.getMinValue()));
					exportAttribute("minValueIsExclusive",String.valueOf(((DoublePropertyDefinitionImpl)doubleDefinition).isMinValueExclusive()));
				}

				if (doubleDefinition.getMaxValue() != null && doubleDefinition.getMinValue() != Double.MAX_VALUE){
					exportAttribute("maxValue",String.valueOf(doubleDefinition.getMaxValue()));
					exportAttribute("maxValueIsExclusive",String.valueOf(((DoublePropertyDefinitionImpl)doubleDefinition).isMaxValueExclusive()));
				}
				
				break;
			case Long:
				LongPropertyDefinition longDefinition = (LongPropertyDefinition)simplePropertyDefinition;
				
				if (longDefinition.isSetDefaultValue()){
					exportAttribute("defaultValue",String.valueOf(longDefinition.getDefaultValue()));
				}
				
				if (longDefinition.getMinValue() != null && longDefinition.getMinValue() != Long.MIN_VALUE){
					exportAttribute("minValue",String.valueOf(longDefinition.getMinValue()));
					exportAttribute("minValueIsExclusive",String.valueOf(((LongPropertyDefinitionImpl)longDefinition).isMinValueExclusive()));
				}

				if (longDefinition.getMaxValue() != null && longDefinition.getMaxValue() != Long.MAX_VALUE){
					exportAttribute("maxValue",String.valueOf(longDefinition.getMaxValue()));
					exportAttribute("maxValueIsExclusive",String.valueOf(((LongPropertyDefinitionImpl)longDefinition).isMaxValueExclusive()));
				}
				
				break;

			case String:
				StringPropertyDefinition stringDefinition = (StringPropertyDefinition)simplePropertyDefinition;
				
				if (stringDefinition.isSetDefaultValue()){
					exportAttribute("defaultValue",stringDefinition.getDefaultValue());
				}
				
				if (stringDefinition.getMinLength() != null){
					exportAttribute("minLength",String.valueOf(stringDefinition.getMinLength()));
				}

				if (stringDefinition.getMaxLength() != null){
					exportAttribute("maxLength",String.valueOf(stringDefinition.getMaxLength()));
				}
				
				if (stringDefinition.getPattern() != null){
					exportAttribute("pattern",String.valueOf(stringDefinition.getPattern()));
				}
				
				exportAttribute("stringFormat",String.valueOf(stringDefinition.getStringFormat()));
				
				break;
			case ContentObject:

				if (CollectionUtils.isNotEmpty(((ContentObjectPropertyDefinition)simplePropertyDefinition).getExpandedAcceptedContentTypes())){ 
					exportAttribute("acceptedContentTypes",StringUtils.join(((ContentObjectPropertyDefinition)simplePropertyDefinition).getExpandedAcceptedContentTypes(),","));
				}

				break;

			case Topic:

				if (CollectionUtils.isNotEmpty(((TopicPropertyDefinition)simplePropertyDefinition).getAcceptedTaxonomies())){ 
					exportAttribute("acceptedTaxonomies",StringUtils.join(((TopicPropertyDefinition)simplePropertyDefinition).getAcceptedTaxonomies(), ","));
				}

				break;

			default:
				break;
			}
			
			if (! jsonOutput){

				//Display Name
				exportDisplayName(simplePropertyDefinition);
				closeElement(simplePropertyDefinition.getName(), false);
			}
			else{
				closeElement(simplePropertyDefinition.getName(), true);
			}
			
		}
		
	}
	
	@Override
	public void finishedChildDefinitionsVisit(LocalizableCmsDefinition parentDefinition) {
		
		super.finishedChildDefinitionsVisit(parentDefinition);
	
		closeElement(parentDefinition.getName(), false);
	}

	private void closeElement(String name, boolean closeStartTag) {
		
		if (prettyPrint){
			writeIdentation(true);
		}
		
		if (jsonOutput){
			jsonBuilder.endObject();
			
			if (prettyPrint){
				number_of_spaces_to_use_for_identation.poll();
			}

		}
		else{
			if (closeStartTag){
				writer.append(">");

				if (prettyPrint){
					increaseNumberOfSpacesToUseForIndentation();
				}
			}
			else{
				
				if (prettyPrint){
					number_of_spaces_to_use_for_identation.poll();
				}

				writer.append("</");
				writer.append(name);
				writer.append(">");
			}

		}
	}

	private void increaseNumberOfSpacesToUseForIndentation() {
		if (number_of_spaces_to_use_for_identation.isEmpty()){
			number_of_spaces_to_use_for_identation.push(1);
		}
		else{
			number_of_spaces_to_use_for_identation.push(number_of_spaces_to_use_for_identation.peek()+3);
		}
	}

	public String exportOutcome() {
		
		if (jsonOutput){
			jsonBuilder.endObject();
		}
		
		return writer.toString();
	}
	
	private void exportDefinitionObjectAndBasicProperties(LocalizableCmsDefinition cmsDefinition, boolean exportDisplayName) {
		
		//Start Definition
		exportStartElement(cmsDefinition.getName());
		
		//Path
		exportPath(cmsDefinition);
			
		//Value Type
		exportValueType(cmsDefinition);
		
		//Url
		exportUrl(cmsDefinition);
		
		if (cmsDefinition instanceof CmsPropertyDefinition){
			//Cardinality
			exportCardinality((CmsPropertyDefinition)cmsDefinition);
		}

		if (exportDisplayName){
			//Display Name
			exportDisplayName(cmsDefinition);
		}
		

	}

	private void exportStartElement(String name) {
		
		if (prettyPrint){
			increaseNumberOfSpacesToUseForIndentation();
			writeIdentation(true);
		}
		
		if (jsonOutput){
			jsonBuilder.key(name);
			
			jsonBuilder.object();
		}
		else{
			writer.append("<");
			writer.append(name);
		}
		
	}

	private void exportCardinality(CmsPropertyDefinition cmsDefinition) {
			exportAttribute("mandatory",String.valueOf(((CmsPropertyDefinition)cmsDefinition).isMandatory()));
			exportAttribute("multiple",String.valueOf(((CmsPropertyDefinition)cmsDefinition).isMultiple()));
	}

	private void exportUrl(LocalizableCmsDefinition cmsDefinition) {
		if (getServerURL() != null){
			
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(getServerURL())
					.append(getRestfulApiBasePath())
					.append(CmsConstants.FORWARD_SLASH) 
					.append(AstroboaClientContextHolder.getActiveRepositoryId())
					.append(CmsConstants.FORWARD_SLASH)
					.append("definition")
					.append(CmsConstants.FORWARD_SLASH);
			
			if (cmsDefinition instanceof CmsPropertyDefinition){
				urlBuilder.append(((CmsPropertyDefinition)cmsDefinition).getFullPath());
			}
			else{
				urlBuilder.append(cmsDefinition.getName());
			}
			
			urlBuilder.append("?output=").append(jsonOutput? "json":"xml");
			
			exportAttribute("url",urlBuilder.toString()); 
		}
		
	}

	private void exportDisplayName(LocalizableCmsDefinition cmsDefinition) {
		if (cmsDefinition.getDisplayName() != null && cmsDefinition.getDisplayName().hasLocalizedLabels()){

			//TODO: Fix this 
			if (!jsonOutput){
				writer.append(">");
			}

			exportStartElement("label");
			
			for (Entry<String,String> localizedLabel : cmsDefinition.getDisplayName().getLocalizedLabels().entrySet()){
				exportAttribute(localizedLabel.getKey(),localizedLabel.getValue());
			}
			
			//TODO: Fix this 
			if (!jsonOutput){
				writer.append(">");
			}
			closeElement("label",false);
		}
	}

	private void exportValueType(LocalizableCmsDefinition cmsDefinition) {
		exportAttribute("valueType",cmsDefinition.getValueType().toString());
	}

	private void exportPath(LocalizableCmsDefinition cmsDefinition) {
		if (cmsDefinition instanceof CmsPropertyDefinition){
			exportAttribute("path",((CmsPropertyDefinition)cmsDefinition).getPath());
		}
		else{
			exportAttribute("path",cmsDefinition.getName());
		}
	}

	public String getServerURL() {
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForActiveClient();
		if (repositoryContext != null && repositoryContext.getCmsRepository() != null && 
				StringUtils.isNotBlank(repositoryContext.getCmsRepository().getServerURL())){
			String serverURL = repositoryContext.getCmsRepository().getServerURL().trim();
			
			return serverURL.endsWith("/")? serverURL.substring(0, serverURL.length()-1) : serverURL; 
		}

		return null;
	}
	
	public String getRestfulApiBasePath() {
		
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForActiveClient();
		if (
			repositoryContext != null && 
			repositoryContext.getCmsRepository() != null && 
			StringUtils.isNotBlank(repositoryContext.getCmsRepository().getRestfulApiBasePath())) {
			String restfulApiBasePath = repositoryContext.getCmsRepository().getRestfulApiBasePath().trim();
			if (!restfulApiBasePath.startsWith("/")) {
				restfulApiBasePath = "/" + restfulApiBasePath;
			}
			 
			return restfulApiBasePath.endsWith("/")? restfulApiBasePath.substring(0, restfulApiBasePath.length()-1) : restfulApiBasePath;
		}

		return null;
	}

	private void exportAttribute(String name, String value){
		
		if (prettyPrint){
			writeIdentation(true);
			writer.write(" ");
		}

		if (jsonOutput){
			jsonBuilder.key(name).value(value);
		}
		else{
			writer.write(" ");
			writer.write(name);
			writer.write("=\"");
			char[] ch = value.toCharArray();
			write(ch, 0, ch.length, true);
			writer.write("\"");

		}
	}
	
	private void write(char[] ch, int start, int length, boolean attribute){
		for (int i = start; i < start + length; i++) {
			if (ch[i] == '>') {
				writer.write("&gt;");
			} else if (ch[i] == '<') {
				writer.write("&lt;");
			} else if (ch[i] == '&') {
				writer.write("&amp;");
			} else if (attribute && ch[i] == '"') {
				writer.write("&quot;");
			} else if (attribute && ch[i] == '\'') {
				writer.write("&apos;");
			} else {
				writer.write(ch[i]);
			}
		}
	}


	private void writeIdentation(boolean addNewLine) {
		
		if (addNewLine){
			writer.write("\n");
		}
		
		Integer numberOfSpaces = number_of_spaces_to_use_for_identation.peek();
		
		if (numberOfSpaces == null){
			writer.write(" ");
		}
		else{
			String spaces = String.format("%"+numberOfSpaces+"s", "");
			writer.write(spaces);
		}
		
	}

}