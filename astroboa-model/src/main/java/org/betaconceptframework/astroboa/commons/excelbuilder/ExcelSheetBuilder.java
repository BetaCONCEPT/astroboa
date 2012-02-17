/**
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
package org.betaconceptframework.astroboa.commons.excelbuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible to create an Excel sheet which
 * contains a list of one or more content objects of the same content type.
 * 
 * Each sheet contains one or more rows which represent the values of one or
 * more content objects of the same content type.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ExcelSheetBuilder{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private CreationHelper creationHelper;
	
	private HSSFCellStyle propertyNameCellStyle;
	private HSSFCellStyle stringPropertyValueCellStyle;
	private HSSFCellStyle integerPropertyValueCellStyle;
	private HSSFCellStyle doublePropertyValueCellStyle;
	private HSSFCellStyle datePropertyValueCellStyle;
	private HSSFCellStyle dateTimePropertyValueCellStyle;

	private String locale; 
	
	private List<HelperRow> rows = new ArrayList<HelperRow>();
	
	//Property Paths. Order is significant
	private List<String> propertyPaths = new ArrayList<String>();
	
	private Map<String, RichTextString> headersForProperty = new HashMap<String,RichTextString>();

	private int currentPropertyPathIndex;
	
	private String contentType;
	private DefinitionService definitionService;
	
	public ExcelSheetBuilder(DefinitionService definitionService, String contentType, String locale, CreationHelper creationHelper, HSSFCellStyle propertyNameCellStyle, HSSFCellStyle stringPropertyValueCellStyle, HSSFCellStyle integerPropertyValueCellStyle, HSSFCellStyle doublePropertyValueCellStyle, HSSFCellStyle datePropertyValueCellStyle, HSSFCellStyle dateTimePropertyValueCellStyle) {
		
		this.locale = locale;
		this.contentType = contentType;
		this.definitionService = definitionService;
		
		this.creationHelper = creationHelper;
		
		this.propertyNameCellStyle = propertyNameCellStyle;
		this.stringPropertyValueCellStyle = stringPropertyValueCellStyle;
		this.integerPropertyValueCellStyle = integerPropertyValueCellStyle;
		this.doublePropertyValueCellStyle = doublePropertyValueCellStyle;
		this.datePropertyValueCellStyle = datePropertyValueCellStyle;
		this.dateTimePropertyValueCellStyle = dateTimePropertyValueCellStyle;
		
	}
	
	public void clear(){
		locale = null;
		creationHelper = null;
		
		propertyNameCellStyle = null;
		stringPropertyValueCellStyle = null;
		integerPropertyValueCellStyle = null;
		doublePropertyValueCellStyle = null;
		datePropertyValueCellStyle = null;
		dateTimePropertyValueCellStyle = null;

	}

	public void addContentObject(ContentObject contentObject){
		
		if (contentObject == null){
			return;
		}
		
		if (! StringUtils.equals(contentType, contentObject.getContentObjectType())){
			logger.warn("Try to add object {} of type {} in sheet which is responsible to contain objects of type {}", 
					new Object[]{contentObject.getSystemName(), contentObject.getContentObjectType(), contentType});
			return ;
		}
		
		//Create a new row with the values of all properties of the object
		HelperRow helperRow = new HelperRow();
		rows.add(helperRow);
		
		fillRowWithValues(contentObject.getComplexCmsRootProperty(), helperRow);
		
		currentPropertyPathIndex = -1;
	}
	
	public void createSheet(HSSFWorkbook workbook){
		
		Sheet sheet = instantiateSheet(workbook);
		
		addRowWithHeaders(sheet);

		for (HelperRow helperRow : rows){

			Row contentObjectRow = sheet.createRow(sheet.getLastRowNum()+1);
			int cellIndex = 1;
			
			for (String propertyPath : propertyPaths) {
			
				Cell contentObjectPropertyCell = contentObjectRow.createCell(cellIndex++);
				
				helperRow.addValueToCellForPath(propertyPath, contentObjectPropertyCell);
			}
		}
		
		autoSizeColumns(sheet);
		
	}
	
	private Sheet instantiateSheet(HSSFWorkbook workbook){
		
		ContentObjectTypeDefinition contentObjectTypeDefinition = (ContentObjectTypeDefinition) definitionService.getCmsDefinition(contentType, ResourceRepresentationType.DEFINITION_INSTANCE, false);

		//Reorder property paths
		DefinitionOrderHelper definitionOrderHelper = new DefinitionOrderHelper();
		contentObjectTypeDefinition.accept(definitionOrderHelper);
		
		Collections.sort(propertyPaths, definitionOrderHelper);
		
		// create a new sheet
		if (contentObjectTypeDefinition.getDisplayName() == null || StringUtils.isBlank(contentObjectTypeDefinition.getDisplayName().getAvailableLocalizedLabel(locale))){
			return workbook.createSheet(contentType);
		}
		else {
			String sheetName = contentObjectTypeDefinition.getDisplayName().getAvailableLocalizedLabel(locale);
			
			//Remove invalid characters (according to POI)
			sheetName = StringUtils.replaceChars(sheetName, "/\\?*[]", "");
			
			//SheetName must be at most 31 chars
			if (sheetName.length() > 31){
				sheetName = sheetName.substring(0, 30);
			}
			
			return workbook.createSheet(sheetName);
		}

	}

	private void addRowWithHeaders(Sheet sheet) {
		Row headerRow = sheet.createRow(1);

		int headerCellIndex=1;
		
		for (String propertyPath : propertyPaths) {
			
			Cell headerCell = headerRow.createCell(headerCellIndex++);
			headerCell.setCellStyle(propertyNameCellStyle);
			
			if (headersForProperty.containsKey(propertyPath)){
				headerCell.setCellValue(headersForProperty.get(propertyPath));
			}
			else{
				logger.warn("Coludn not find header for property {}. Will put Property path instead", propertyPath);
			}
		}
	}
	
	private void autoSizeColumns(Sheet sheet) {
		
		for (short columnIndex=1; columnIndex <= propertyPaths.size(); ++columnIndex) {
				sheet.autoSizeColumn(columnIndex);
		}
	}
	
	private void fillRowWithValues(ComplexCmsProperty complexCmsProperty, HelperRow helperRow) {
		
		if (complexCmsProperty == null){
			return;
		}
		
		if (StringUtils.equals(complexCmsProperty.getPath(), "accessibility")){
			return;
		}
				
		Collection<List<CmsProperty<?,?>>> childProperties = complexCmsProperty.getChildProperties().values();
		
		
		for (List<CmsProperty<?,?>> childPropertyList : childProperties){
			
			for (CmsProperty<?,?> childProperty : childPropertyList){
				
				if (childProperty instanceof ComplexCmsProperty){
					fillRowWithValues((ComplexCmsProperty)childProperty, helperRow);
				}
				else{
					fillRowWithValues((SimpleCmsProperty)childProperty, helperRow);
				}
				
			}
		}
	}
	
	private void fillRowWithValues(SimpleCmsProperty simpleProperty, HelperRow helperRow) {
		
		if (simpleProperty == null || simpleProperty.getPropertyDefinition() == null){
			return;
		}
		
		CmsPropertyDefinition propertyDefinition = simpleProperty.getPropertyDefinition();
		
		//Property of type password in not exported
		if (propertyDefinition instanceof StringPropertyDefinition && 
				((StringPropertyDefinition)propertyDefinition).isPasswordType()){
			return;
		}
		
		if (simpleProperty.hasNoValues()){
			return;
		}
		
		boolean multiple = propertyDefinition.isMultiple();
		String path = simpleProperty.getPath();
		
		if (multiple){

			int index = 0;
			
			for (Object value : simpleProperty.getSimpleTypeValues()){
				setPropertyCellStyleAndValue(path+"["+index++ +"]", propertyDefinition, value, helperRow, creationHelper, locale);
			}
			
		}
		else{
			setPropertyCellStyleAndValue(path, propertyDefinition, simpleProperty.getSimpleTypeValue(), helperRow, creationHelper, locale);
		}
		
	}
	
	private void setPropertyCellStyleAndValue(String propertyPath, CmsPropertyDefinition propertyDefinition, Object value,  HelperRow row, CreationHelper creationHelper, String locale) {

		appendPropertyPathList(propertyPath);
		
		appendHeaderPropertyList(propertyPath, propertyDefinition);
		
		switch (propertyDefinition.getValueType()) {
		case String: {
			
			row.addValueAndStyleForPath(propertyPath, creationHelper.createRichTextString(((String)value)), stringPropertyValueCellStyle);

			break;
		}	
		case Date: {
			
			if (((CalendarPropertyDefinition) propertyDefinition).isDateTime()) {
				row.addValueAndStyleForPath(propertyPath,(Calendar)value,  dateTimePropertyValueCellStyle);
			}
			else {
				row.addValueAndStyleForPath(propertyPath, (Calendar)value, datePropertyValueCellStyle);
			}
			break;
		}
		case TopicReference: {
			
			String cellValue = getTopicPath((Topic) value, locale);
			row.addValueAndStyleForPath(propertyPath, creationHelper.createRichTextString(cellValue), stringPropertyValueCellStyle);
			
			break;
		}
		case Double: {
			
			row.addValueAndStyleForPath(propertyPath, (Double) value, doublePropertyValueCellStyle);
			break;
		}
		case Long: {

			row.addValueAndStyleForPath(propertyPath, (Long) value, integerPropertyValueCellStyle);
			break;
		}
		case ObjectReference: {
			
			String cellValue = getContentObjectTitle((ContentObject) value, locale);
			
			row.addValueAndStyleForPath(propertyPath, creationHelper.createRichTextString(cellValue), stringPropertyValueCellStyle);
			break;
		}
		case Binary: {
			
			String cellValue = ((BinaryChannel)value).buildResourceApiURL(null, null, null, null, null, true, false);
			
			row.addValueAndStyleForPath(propertyPath, creationHelper.createRichTextString(cellValue), stringPropertyValueCellStyle);
			
			break;
		}


		default:
			break;
		}
		
	}
	
	private void appendHeaderPropertyList(String propertyPath,
			CmsPropertyDefinition propertyDefinition) {
		
		if (! headersForProperty.containsKey(propertyPath)){
			headersForProperty.put(propertyPath, creationHelper.createRichTextString(propertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale)));
		}
		
	}

	private void appendPropertyPathList(String propertyPath) {
		
		if (! propertyPaths.contains(propertyPath)){
			if (currentPropertyPathIndex == -1){
				propertyPaths.add(propertyPath);
			}
			else{
				propertyPaths.add(currentPropertyPathIndex, propertyPath);
			}
		}
		
		currentPropertyPathIndex = propertyPaths.indexOf(propertyPath);
		
	}

	private String getTopicPath(Topic topic, String locale) {
		
		if (topic != null){
			return topic.getAvailableLocalizedLabel(locale);
		}
		
		return "";
		
		/*List<Topic> topicPath = new ArrayList<Topic>();
		createTopicPath(topicPath, topic);
		
		String topicPathAsString = topicPath.get(0).getLocalizedLabelForLocale(locale);
		for(Topic topicInPath : topicPath.subList(1, topicPath.size())) {
			topicPathAsString = topicPathAsString + "/" + topicInPath.getLocalizedLabelForLocale(locale);
		}
		
		return topicPathAsString;*/
	}

	private String getContentObjectTitle(ContentObject object, String locale) {
		
		if (object == null){
			return "";
		}
		
		String label = object.getLabel(locale);
		if (StringUtils.isNotBlank(label)){
			return label;
		}
		
		StringProperty titleProperty = (StringProperty) object.getCmsProperty("profile.title");
		
		if (titleProperty == null || titleProperty.hasNoValues()){
			//this should never happen 
			return object.getSystemName();
		}
		
		return titleProperty.getFirstValue();
	}
}
