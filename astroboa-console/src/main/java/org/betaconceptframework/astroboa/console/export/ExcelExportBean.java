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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CalendarPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.CmsRankedOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.commons.ContentObjectSelectionBean;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapper;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Name("excelExportBean")
@Scope(ScopeType.EVENT)
public class ExcelExportBean {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AstroboaClient astroboaClient;
	
	private CellStyle propertyNameCellStyle;
	private CellStyle stringPropertyValueCellStyle;
	private CellStyle integerPropertyValueCellStyle;
	private CellStyle doublePropertyValueCellStyle;
	private CellStyle longPropertyValueCellStyle;
	private CellStyle datePropertyValueCellStyle;
	private CellStyle dateTimePropertyValueCellStyle;
	private List<CmsPropertyDefinition> workingPropertyDefinitionList = new ArrayList<CmsPropertyDefinition>();
	
	
	public void exportContentObjectSelection(ContentObjectSelectionBean contentObjectSelection, String locale) {
		
		if (contentObjectSelection == null || CollectionUtils.isEmpty(contentObjectSelection.getSelectedContentObjects())) {
			JSFUtilities.addMessage(null, "content.search.exportResults.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (StringUtils.isEmpty(locale)) {
			locale = "en";
			logger.warn("Provided Locale was empty. The default locale which is 'en' will be used");
		}
		
		List<ContentObjectUIWrapper> contentObjectUiWrapperList = contentObjectSelection.getSelectedContentObjects();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("UTF-8");

			WorkbookBuilder workbookBuilder = new WorkbookBuilder(astroboaClient.getDefinitionService(), locale);
			
			//Workbook workbook = new HSSFWorkbook();
			//CreationHelper creationHelper = workbook.getCreationHelper();
			//DataFormat dataFormat = workbook.getCreationHelper().createDataFormat();
			
			//initializeDefaultCellStyles(workbook, dataFormat);

			// create a new sheet
			//Sheet sheet = workbook.createSheet();
			
			//Row headerRow = sheet.createRow(1);
			
			// we assume that the list contains content objects of the same content type.
			// The content object type for the whole list is taken from the first content object in the list
			//String workingContentObjectType = contentObjectUiWrapperList.get(0).getContentObject().getContentObjectType();
			//response.setHeader("Content-Disposition", "attachment;filename=" + "listOf" + workingContentObjectType + ".xls");
			
			//createWorkingPropertyDefinitionList(workingContentObjectType);
			
			//addPropertyNamesToHeader(headerRow, creationHelper, locale);
			
			int rowIndex = 2;
			for (ContentObjectUIWrapper contentObjectUiWrapper : contentObjectUiWrapperList) {
				ContentObject contentObject = contentObjectUiWrapper.getContentObject();
				
				// exclude content objects that their content object type is different than the workingContentObjectType 
				/*if (contentObject.getContentObjectType().equals(workingContentObjectType)) {
					Row contentObjectRow = sheet.createRow(rowIndex);
					fillRowWithContentObjectProperties(contentObject, sheet, contentObjectRow, creationHelper, locale);
					++rowIndex;
				}*/

				workbookBuilder.addContentObjectToWorkbook(contentObject);

				++rowIndex;
				
				//Limit to the first 5000 content objects
				if (rowIndex > 5000){
					break;
				}
			}
			
			response.setHeader("Content-Disposition", "attachment;filename=" + workbookBuilder.getWorkbookName() + ".xls");
			//autoSizeColumns(sheet);
			workbookBuilder.autoSizeColumns();
			
			try {
				ServletOutputStream servletOutputStream = response.getOutputStream();

				//workbook.write(servletOutputStream);
				workbookBuilder.getWorkbook().write(servletOutputStream);

				servletOutputStream.flush();

				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing excel workbook to servlet output stream", e);
				JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				workbookBuilder.clear();
				workbookBuilder = null;
			}


		}
		else {
			JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
		}

	}
	
	
	public void exportContentObjectList(ContentObjectCriteria contentObjectCriteria, String locale) {
		
		// remove offset from criteria and add a limit of 5000
		contentObjectCriteria.setOffset(0);
		contentObjectCriteria.setLimit(5000);
		
		// run the query
		CmsOutcome<CmsRankedOutcome<ContentObject>> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria);
		
		if (cmsOutcome.getCount() == 0) {
			JSFUtilities.addMessage(null, "content.search.exportResults.nullList", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (StringUtils.isEmpty(locale)) {
			locale = "en";
			logger.warn("Provided Locale was empty. The default locale which is 'en' will be used");
		}
		
		List<CmsRankedOutcome<ContentObject>> cmsOutcomeRowList = cmsOutcome.getResults();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			.getExternalContext().getResponse();
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("UTF-8");

			WorkbookBuilder workbookBuilder = new WorkbookBuilder(astroboaClient.getDefinitionService(), locale);
			
			/*Workbook workbook = new HSSFWorkbook();
			CreationHelper creationHelper = workbook.getCreationHelper();
			DataFormat dataFormat = workbook.getCreationHelper().createDataFormat();
			
			initializeDefaultCellStyles(workbook, dataFormat);

			// create a new sheet
			Sheet sheet = workbook.createSheet();
			
			Row headerRow = sheet.createRow(1);
			
			// we assume that the list contains content objects of the same content type.
			// The content object type for the whole list is taken from the first content object in the list
			String workingContentObjectType = cmsOutcomeRowList.get(0).getCmsRepositoryEntity().getContentObjectType();
			response.setHeader("Content-Disposition", "attachment;filename=" + "listOf" + workingContentObjectType + ".xls");
			
			createWorkingPropertyDefinitionList(workingContentObjectType);
			
			addPropertyNamesToHeader(headerRow, creationHelper, locale);
			*/
			
			int rowIndex = 2;
			for (CmsRankedOutcome<ContentObject> cmsOutcomeRow : cmsOutcomeRowList) {
				ContentObject contentObject = cmsOutcomeRow.getCmsRepositoryEntity();
				
				// exclude content objects that their content object type is different than the workingContentObjectType 
				/*if (contentObject.getContentObjectType().equals(workingContentObjectType)) {
					Row contentObjectRow = sheet.createRow(rowIndex);
					fillRowWithContentObjectProperties(contentObject, sheet, contentObjectRow, creationHelper, locale);
					++rowIndex;
				}*/
				
				workbookBuilder.addContentObjectToWorkbook(contentObject);

				++rowIndex;

			}
			
			response.setHeader("Content-Disposition", "attachment;filename=" + workbookBuilder.getWorkbookName() + ".xls");
			//autoSizeColumns(sheet);
			workbookBuilder.autoSizeColumns();
			
			try {
				ServletOutputStream servletOutputStream = response.getOutputStream();

				//workbook.write(servletOutputStream);
				workbookBuilder.getWorkbook().write(servletOutputStream);
				
				servletOutputStream.flush();

				facesContext.responseComplete();

			}
			catch (IOException e) {
				logger.error("An error occurred while writing excel workbook to servlet output stream", e);
				JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
			}	
			finally{
				workbookBuilder.clear();
				workbookBuilder = null;
			}


		}
		else {
			JSFUtilities.addMessage(null, "content.search.exportResults.error", null, FacesMessage.SEVERITY_WARN);
		}

	}
	
	
	private void initializeDefaultCellStyles(Workbook workbook, DataFormat dataFormat) {
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
	
	
	private void createWorkingPropertyDefinitionList(String contentObjectType) {
		
		DefinitionService definitionService = astroboaClient.getDefinitionService();

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
	}
	
	
	private void addPropertyNamesToHeader(Row headerRow, CreationHelper creationHelper, String locale) {
		
		Cell headerCell;
		int headerCellIndex = 1;
		for (CmsPropertyDefinition cmsPropertyDefinition : workingPropertyDefinitionList) {
			headerCell = headerRow.createCell(headerCellIndex);
			headerCell.setCellStyle(propertyNameCellStyle);
			headerCell.setCellValue(
			         creationHelper.createRichTextString(cmsPropertyDefinition.getDisplayName().getLocalizedLabelForLocale(locale)));
			++headerCellIndex;
		}
	}
	
	
	private void fillRowWithContentObjectProperties(ContentObject contentObject, Sheet sheet, Row contentObjectRow, CreationHelper creationHelper, String locale) {
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


	private void autoSizeColumns(Sheet sheet) {
		for (short columnIndex=1; columnIndex <= workingPropertyDefinitionList.size(); ++columnIndex) {
			sheet.autoSizeColumn(columnIndex);
		}
	}
	
	private List<Topic> getTopicPath(Topic topic) {
		
		List<Topic> topicPath = new ArrayList<Topic>();
		
		createTopicPath(topicPath, topic);
		return topicPath;
	}
	
	
	private void createTopicPath(List<Topic> topicPath, Topic topic) {
		
		if (topic.getParent() != null) {
			createTopicPath(topicPath, topic.getParent());
		}
		
		topicPath.add(topic);
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
}
