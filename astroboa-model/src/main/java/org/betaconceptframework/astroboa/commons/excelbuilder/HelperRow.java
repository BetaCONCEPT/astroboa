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
package org.betaconceptframework.astroboa.commons.excelbuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible to contain information about the columns of a row of an excel sheet
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class HelperRow {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	//Key is the path of a property
	private Map<String, HSSFCellStyle> stylesPerCell = new HashMap<String, HSSFCellStyle>();
	
	//Key is the path of a property
	private Map<String, Object> valuesPerCell = new HashMap<String, Object>();

	public void addValueAndStyleForPath(String propertyPath,
			Object value, HSSFCellStyle cellStyle) {
		
		valuesPerCell.put(propertyPath, value);
		stylesPerCell.put(propertyPath, cellStyle);
		
	}

	public void addValueToCellForPath(String propertyPath,	Cell propertyCell) {
		

			if (valuesPerCell.containsKey(propertyPath)){
				
				if (stylesPerCell.containsKey(propertyPath)){
					propertyCell.setCellStyle(stylesPerCell.get(propertyPath));
				}
				
				Object value = valuesPerCell.get(propertyPath);
				
				if (value instanceof String){
					propertyCell.setCellValue(((String)value));
				}
				else if (value instanceof RichTextString){
					propertyCell.setCellValue(((RichTextString)value));
				}
				else if (value instanceof Boolean){
					propertyCell.setCellValue(((Boolean)value));
				}
				else if (value instanceof Calendar){
					propertyCell.setCellValue(((Calendar)value));
				}
				else if (value instanceof Double){
					propertyCell.setCellValue(((Double)value));
				}
				else if (value instanceof Long){
					propertyCell.setCellValue(((Long)value));
				}
				
			}
		
	}
	
	
}
