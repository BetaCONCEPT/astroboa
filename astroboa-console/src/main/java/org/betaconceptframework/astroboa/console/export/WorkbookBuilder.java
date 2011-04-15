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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.betaconceptframework.astroboa.api.model.ContentObject;
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

	//Workbook sheets per content type
	private Map<String, ExcelSheetBuilder> sheetBuilders = new HashMap<String, ExcelSheetBuilder>();

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

		sheetBuilders.clear();
	}

	public void addContentObjectToWorkbook(ContentObject contentObject){
		
		if (contentObject == null){
			return;
		}
		
		String workingContentObjectType = contentObject.getContentObjectType();

		if (StringUtils.isBlank(workingContentObjectType)){
			logger.warn("ContentObject {} has no content type. Cannot export it to the workbook");
			return;
		}

		ExcelSheetBuilder sheetBuilder = retrieveSheetBuilderForContentType(workingContentObjectType);
		
		sheetBuilder.addContentObject(contentObject);		
	}
	
	private ExcelSheetBuilder retrieveSheetBuilderForContentType(String workingContentObjectType) {
		
		if (sheetBuilders.containsKey(workingContentObjectType)){
			return sheetBuilders.get(workingContentObjectType);
		}
		
		ExcelSheetBuilder sheetBuilder = new ExcelSheetBuilder(definitionService, workingContentObjectType, locale, creationHelper, propertyNameCellStyle, stringPropertyValueCellStyle, integerPropertyValueCellStyle, doublePropertyValueCellStyle, datePropertyValueCellStyle, dateTimePropertyValueCellStyle);

		//Add Sheet to Map
		sheetBuilders.put(workingContentObjectType, sheetBuilder);
		
		workbookName.append("-");
		workbookName.append(workingContentObjectType);
		
		//Return Sheet
		return sheetBuilder;
	}
	
	public String getWorkbookName(){
		return workbookName.toString();
	}
	

	private void initializeDefaultCellStyles() {
		// create cell styles for property values and property names
		propertyNameCellStyle = workbook.createCellStyle();
		stringPropertyValueCellStyle = workbook.createCellStyle();
		integerPropertyValueCellStyle = workbook.createCellStyle();
		doublePropertyValueCellStyle = workbook.createCellStyle();
		datePropertyValueCellStyle = workbook.createCellStyle();
		dateTimePropertyValueCellStyle = workbook.createCellStyle();
		

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
		datePropertyValueCellStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy"));
		datePropertyValueCellStyle.setWrapText(true);

		dateTimePropertyValueCellStyle.setFont(propertyValueFont);
		dateTimePropertyValueCellStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy HH:mm"));
		dateTimePropertyValueCellStyle.setWrapText(true);

		// Set the other cell style and formatting
		//cs2.setBorderBottom(cs2.BORDER_THIN);

	}

	public HSSFWorkbook getWorkbook() {
		
		for (ExcelSheetBuilder excelSheetBuilder: sheetBuilders.values()){
			excelSheetBuilder.createSheet(workbook);
		}
		
		return workbook;
	}
	

}
