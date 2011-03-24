/**
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
package org.betaconceptframework.astroboa.console.export;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible to create an Excel workbook which
 * contains a list of one or more content objects.
 * 
 * Each sheet contains one or more rows which represent the values of one or
 * more content objects of the same content type.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class WorkbookBuilder {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private HSSFWorkbook workbook;
	private CreationHelper creationHelper;
	private DataFormat dataFormat;
	
	private HSSFCellStyle propertyNameCellStyle;
	private HSSFCellStyle stringPropertyValueCellStyle;
	private HSSFCellStyle integerPropertyValueCellStyle;
	private HSSFCellStyle doublePropertyValueCellStyle;
	private HSSFCellStyle datePropertyValueCellStyle;
	private HSSFCellStyle dateTimePropertyValueCellStyle;
	private HSSFCellStyle longPropertyValueCellStyle;

	//Workbook sheets per content type
	private Map<String, Sheet> sheets = new HashMap<String, Sheet>();

	//Definitions per Content type
	private Map<String, List<CmsPropertyDefinition>> workingPropertyDefinitionListPerContentType = new HashMap<String,List<CmsPropertyDefinition>>();
	
	private DefinitionService definitionService;

	private String locale; 
	
	private StringBuilder workbookName = new StringBuilder("listOf");
		
	public WorkbookBuilder(DefinitionService definitionService, String locale) {
		
		this.definitionService = definitionService;
		this.locale = locale;
		
		workbook = new HSSFWorkbook();
		creationHelper = workbook.getCreationHelper();
		dataFormat = workbook.getCreationHelper().createDataFormat();
		
		initializeDefaultCellStyles();
	}
	
	public void clear(){
		definitionService = null;
		locale = null;
		workbook = null;
		creationHelper = null;
		dataFormat = null;
		
		propertyNameCellStyle = null;
		stringPropertyValueCellStyle = null;
		integerPropertyValueCellStyle = null;
		doublePropertyValueCellStyle = null;
		datePropertyValueCellStyle = null;
		dateTimePropertyValueCellStyle = null;
		longPropertyValueCellStyle = null;

	}

	public void addContentObjectToWorkbook(ContentObject contentObject){
		
		if (contentObject == null){
			return;
		}
		
		String workingContentObjectType = contentObject.getContentObjectType();

		if (StringUtils.isBlank(workingContentObjectType)){
			logger.warn("ContentObject {} has no content type. Cannot export it to the workbook");
		}

		//Retrieve the sheet where content object will be exported
		Sheet sheet = retrieveSheetForContentType(workingContentObjectType);
		List<CmsPropertyDefinition> workingDefinitionList = workingPropertyDefinitionListPerContentType.get(workingContentObjectType);
		Row newRow = sheet.createRow(sheet.getLastRowNum()+1);
		
		fillRowWithContentObjectProperties(contentObject, sheet, newRow, workingDefinitionList);
		
	}
	
	public void autoSizeColumns() {
		
		for (Entry<String, List<CmsPropertyDefinition>> workingPropertyDefinitionListEntry : workingPropertyDefinitionListPerContentType.entrySet()){
		
			String contentType = workingPropertyDefinitionListEntry.getKey();
			List<CmsPropertyDefinition> workingPropertyDefinitionList = workingPropertyDefinitionListEntry.getValue();
			Sheet sheet  =  sheets.get(contentType);
			
			for (short columnIndex=1; columnIndex <= workingPropertyDefinitionList.size(); ++columnIndex) {
				sheet.autoSizeColumn(columnIndex);
			}
		}
		
	}
	
	private void fillRowWithContentObjectProperties(ContentObject contentObject, Sheet sheet, Row contentObjectRow,List<CmsPropertyDefinition> workingPropertyDefinitionList) {
		Cell contentObjectPropertyCell;
		int cellIndex = 1;
		int maxRowHeightInLines = 1;
		
		for (CmsPropertyDefinition propertyDefinition : workingPropertyDefinitionList) {
			contentObjectPropertyCell = contentObjectRow.createCell(cellIndex);
			
			SimpleCmsProperty<?,?,?> property = (SimpleCmsProperty<?,?,?>)contentObject.getCmsProperty(propertyDefinition.getPath());
			
			if (!property.hasNoValues()) {
				List<Object> propertyValues = new ArrayList<Object>();
				if (propertyDefinition.isMultiple()) {
					propertyValues.addAll(property.getSimpleTypeValues());
				}
				else {
					propertyValues.add(property.getSimpleTypeValue());
				}
				
				maxRowHeightInLines = setPropertyCellStyleAndValue(propertyDefinition, propertyValues, sheet, contentObjectRow, maxRowHeightInLines, contentObjectPropertyCell, creationHelper, locale);
			}
			
			++cellIndex;
		}
		
		contentObjectRow.setHeightInPoints(maxRowHeightInLines*sheet.getDefaultRowHeightInPoints());
	}
	
	private int setPropertyCellStyleAndValue(CmsPropertyDefinition propertyDefinition, List<Object> propertyValues,  Sheet sheet, Row propertyRow, int maxRowHeightInLines, Cell propertyCell, CreationHelper creationHelper, String locale) {
		
		switch (propertyDefinition.getValueType()) {
		case String: {
			propertyCell.setCellStyle(stringPropertyValueCellStyle);
			String cellValue = (String) propertyValues.get(0);
			for (Object propertyValue : propertyValues.subList(1, propertyValues.size())) {
				cellValue = cellValue + "\n" + (String) propertyValue;
			}
			propertyCell.setCellValue(creationHelper.createRichTextString(cellValue));
			maxRowHeightInLines = Math.max(propertyValues.size(), maxRowHeightInLines);

			break;
		}	
		case Date: {
			if(!propertyDefinition.isMultiple()) {
				if (((CalendarPropertyDefinition) propertyDefinition).isDateTime()) {
					propertyCell.setCellStyle(dateTimePropertyValueCellStyle);
				}
				else {
					propertyCell.setCellStyle(datePropertyValueCellStyle);
				}
				propertyCell.setCellValue((Calendar) propertyValues.get(0));
			}
			else {
				for (Object propertyValue : propertyValues) {
					propertyCell.setCellValue((Calendar) propertyValue);
				}
			}
			break;
		}
		case Topic: {
			String cellValue = getTopicPath((Topic) propertyValues.get(0), locale);
			for (Object propertyValue : propertyValues.subList(1, propertyValues.size())) {
				cellValue = cellValue + "\n" + getTopicPath((Topic) propertyValue, locale);
			}
			propertyCell.setCellValue(creationHelper.createRichTextString(cellValue));
			maxRowHeightInLines = Math.max(propertyValues.size(), maxRowHeightInLines);
			break;
		}
		case Double: {
			propertyCell.setCellStyle(doublePropertyValueCellStyle);
			
			if (CollectionUtils.isNotEmpty(propertyValues)){
				if(!propertyDefinition.isMultiple()) {
					propertyCell.setCellValue((Double) propertyValues.get(0));
				}
				else {
					for (Object propertyValue : propertyValues) {
						propertyCell.setCellValue((Double) propertyValue);
					}
				}
			}
			break;
		}
		case Long: {
			propertyCell.setCellStyle(longPropertyValueCellStyle);
			
			if (CollectionUtils.isNotEmpty(propertyValues)){
				if(!propertyDefinition.isMultiple()) {
					propertyCell.setCellValue((Long) propertyValues.get(0));
				}
				else {
					for (Object propertyValue : propertyValues) {
						propertyCell.setCellValue((Long) propertyValue);
					}
				}
			}
			break;
		}
		case ContentObject: {
			String cellValue = getContentObjectTitle((ContentObject) propertyValues.get(0), locale);
			for (Object propertyValue : propertyValues.subList(1, propertyValues.size())) {
				cellValue = cellValue + "\n" + getContentObjectTitle((ContentObject) propertyValue, locale);
			}
			propertyCell.setCellValue(creationHelper.createRichTextString(cellValue));
			maxRowHeightInLines = Math.max(propertyValues.size(), maxRowHeightInLines);
		}	


		default:
			break;
		}
		
		return maxRowHeightInLines;
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

	private String getContentObjectTitle(ContentObject propertyValue, String locale) {
		if (propertyValue == null){
			return "";
		}
		
		StringProperty titleProperty = (StringProperty) propertyValue.getCmsProperty("profile.title");
		
		if (titleProperty == null || titleProperty.hasNoValues()){
			//this should never happen 
			return propertyValue.getSystemName();
		}
		
		return titleProperty.getFirstValue();
	}
	private Sheet retrieveSheetForContentType(String workingContentObjectType) {
		
		if (sheets.containsKey(workingContentObjectType)){
			return sheets.get(workingContentObjectType);
		}
		
		ContentObjectTypeDefinition contentObjectTypeDefinition = definitionService.getContentObjectTypeDefinition(workingContentObjectType);
		
		// create a new sheet
		Sheet sheet = null;
		
		if (contentObjectTypeDefinition.getDisplayName() == null || StringUtils.isBlank(contentObjectTypeDefinition.getDisplayName().getAvailableLocalizedLabel(locale))){
			sheet = workbook.createSheet(workingContentObjectType);
		}
		else {
			String sheetName = contentObjectTypeDefinition.getDisplayName().getAvailableLocalizedLabel(locale);
			
			//Remove invalid characters (according to POI)
			sheetName = StringUtils.replaceChars(sheetName, "/\\?*[]", "");
			
			//SheetName must be at most 31 chars
			if (sheetName.length() > 31){
				sheetName = sheetName.substring(0, 30);
			}
			
			sheet = workbook.createSheet(sheetName);
		}
		
		createWorkingPropertyDefinitionList(workingContentObjectType);

		//Create Sheet Headers
		Row headerRow = sheet.createRow(1);
		addPropertyNamesToHeader(headerRow, workingContentObjectType);

		//Add Sheet to Map
		sheets.put(workingContentObjectType, sheet);
		
		workbookName.append("-");
		workbookName.append(workingContentObjectType);
		
		//Return Sheet
		return sheet;
	}
	
	public String getWorkbookName(){
		return workbookName.toString();
	}
	
	private void createWorkingPropertyDefinitionList(String contentObjectType) {
		
		List<CmsPropertyDefinition> workingPropertyDefinitionList = new ArrayList<CmsPropertyDefinition>();
		
		// add profile properties
		ComplexCmsPropertyDefinition profileDefinition = (ComplexCmsPropertyDefinition) definitionService.getCmsPropertyDefinition(contentObjectType +".profile");
		for (CmsPropertyDefinition cmsPropertyDefinition : profileDefinition.getChildCmsPropertyDefinitions().values()) {
			if (
				"title".equals(cmsPropertyDefinition.getName()) || 
				"description".equals(cmsPropertyDefinition.getName()) ||
				"subject".equals(cmsPropertyDefinition.getName()) ||
				"created".equals(cmsPropertyDefinition.getName()) || 
				"modified".equals(cmsPropertyDefinition.getName())) {
				
				workingPropertyDefinitionList.add(cmsPropertyDefinition);
			}
		}
		
		// add root properties
		for (CmsPropertyDefinition cmsPropertyDefinition : definitionService.getContentObjectTypeDefinition(contentObjectType).getPropertyDefinitions().values()) {
			if (!cmsPropertyDefinition.getValueType().equals(ValueType.Complex)) {
				workingPropertyDefinitionList.add(cmsPropertyDefinition);
			}
		}
		
		workingPropertyDefinitionListPerContentType.put(contentObjectType, workingPropertyDefinitionList);
	}
	
	
	private void addPropertyNamesToHeader(Row headerRow, String contentObjectType) {
		
		List<CmsPropertyDefinition> workingPropertyDefinitionList = workingPropertyDefinitionListPerContentType.get(contentObjectType);
		
		Cell headerCell;
		int headerCellIndex = 1;
		for (CmsPropertyDefinition cmsPropertyDefinition : workingPropertyDefinitionList) {
			headerCell = headerRow.createCell(headerCellIndex);
			headerCell.setCellStyle(propertyNameCellStyle);
			headerCell.setCellValue(creationHelper.createRichTextString(cmsPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale)));
			++headerCellIndex;
		}
	}


	private void initializeDefaultCellStyles() {
		// create cell styles for property values and property names
		propertyNameCellStyle = workbook.createCellStyle();
		stringPropertyValueCellStyle = workbook.createCellStyle();
		integerPropertyValueCellStyle = workbook.createCellStyle();
		doublePropertyValueCellStyle = workbook.createCellStyle();
		datePropertyValueCellStyle = workbook.createCellStyle();
		dateTimePropertyValueCellStyle = workbook.createCellStyle();
		longPropertyValueCellStyle = workbook.createCellStyle();
		

		// create 2 fonts objects
		Font propertyNameFont = workbook.createFont();
		Font propertyValueFont = workbook.createFont();

		// Set propertyNameFont to 14 point type, blue and bold
		propertyNameFont.setFontHeightInPoints((short) 14);
		propertyNameFont.setColor(IndexedColors.BLUE.getIndex() );
		propertyNameFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// Set propertyValueFont to 10 point type, black
		propertyValueFont.setFontHeightInPoints((short) 10);
		propertyValueFont.setColor( IndexedColors.BLACK.getIndex() );

		// Set cell style and formatting
		propertyNameCellStyle.setFont(propertyNameFont);
		propertyNameCellStyle.setDataFormat(dataFormat.getFormat("text"));

		stringPropertyValueCellStyle.setFont(propertyValueFont);
		stringPropertyValueCellStyle.setDataFormat(dataFormat.getFormat("text"));
		stringPropertyValueCellStyle.setWrapText(true);


		integerPropertyValueCellStyle.setFont(propertyValueFont);
		integerPropertyValueCellStyle.setWrapText(true);
		//integerPropertyValueCellStyle.setDataFormat(dataFormat.getFormat(""));

		doublePropertyValueCellStyle.setFont(propertyValueFont);
		doublePropertyValueCellStyle.setDataFormat(dataFormat.getFormat("#,##0.000"));
		doublePropertyValueCellStyle.setWrapText(true);

		datePropertyValueCellStyle.setFont(propertyValueFont);
		datePropertyValueCellStyle.setDataFormat(dataFormat.getFormat("d/m/yyyy"));
		datePropertyValueCellStyle.setWrapText(true);

		dateTimePropertyValueCellStyle.setFont(propertyValueFont);
		dateTimePropertyValueCellStyle.setDataFormat(dataFormat.getFormat("d/m/yyyy h:mm"));
		dateTimePropertyValueCellStyle.setWrapText(true);

		longPropertyValueCellStyle.setFont(propertyValueFont);
		longPropertyValueCellStyle.setDataFormat(dataFormat.getFormat("#,##0.000"));
		longPropertyValueCellStyle.setWrapText(true);

		// Set the other cell style and formatting
		//cs2.setBorderBottom(cs2.BORDER_THIN);

	}

	public HSSFWorkbook getWorkbook() {
		return workbook;
	}
	

}
