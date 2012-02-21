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
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionOrderHelper extends AbstractCmsPropertyDefinitionVisitor implements Comparator<String>{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private List<String> paths = new ArrayList<String>();

	
	public DefinitionOrderHelper() {
		setVisitType(VisitType.Full);
	}

	@Override
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		
	}

	@Override
	public void visitComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {
		
		paths.add(complexPropertyDefinition.getPath());
	}

	@Override
	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {
		
		paths.add(simplePropertyDefinition.getPath());
		
	}
	
	@Override
	public int compare(String propertyPath1, String propertyPath2) {
		
		if (propertyPath1 == null && propertyPath2 == null)
			return 0;
		
		if (propertyPath1 != null && propertyPath2 == null)
			return 1;
		
		if (propertyPath1 == null && propertyPath2 != null)
			return -1;
		
		String[] pathParts1 = StringUtils.split(propertyPath1, CmsConstants.PERIOD_DELIM);
		String[] pathParts2 = StringUtils.split(propertyPath2, CmsConstants.PERIOD_DELIM);
		
		String currentPath1 ="";
		String currentPath2 ="";
		
		for (int i=0; i<pathParts1.length;i++){
			
			String pathPart1 = pathParts1[i];
			
			if (pathParts2.length>i){
				
				String pathPart2 = pathParts2[i];

				currentPath1 = StringUtils.isBlank(currentPath1)? pathPart1 : currentPath1+CmsConstants.PERIOD_DELIM+pathPart1;
				currentPath2 = StringUtils.isBlank(currentPath2)? pathPart2 : currentPath2+CmsConstants.PERIOD_DELIM+pathPart2;
				
				String path1WithNoIndex = currentPath1.replaceAll("\\[.*\\]", "");
				String path2WithNoIndex = currentPath2.replaceAll("\\[.*\\]", "");
				
				int compare = Integer.valueOf(paths.indexOf(path1WithNoIndex)).compareTo(Integer.valueOf(paths.indexOf(path2WithNoIndex)));
				
				if (compare != 0){
					return compare;
				}
				else{
					if (pathPart1.contains("[")){
						compare = pathPart1.compareTo(pathPart2);
						
						if (compare != 0){
							return compare;
						}
					}
				}
			}
			else{
				return 1;
			}
		}
		
		return currentPath1.compareTo(currentPath2);
		
	}
	
	
	
}
