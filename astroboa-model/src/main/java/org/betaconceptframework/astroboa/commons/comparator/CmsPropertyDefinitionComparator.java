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
package org.betaconceptframework.astroboa.commons.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.slf4j.LoggerFactory;

/**
 * Compares cms property definition map entries by their order , then by their types (Simple, Complex, BinaryChannel)
 * and then by their localizedNames in ascending order.
 * 
 * It may be configured to compare only by order and localized labels.
 * 
 * It compares Map.Entry because all definitions are stored in a Map whose key is cms property name and
 * value is cms property definition. Although cms property name is available in definition, it may be the 
 * case where cms property is a reference to another property and thus may have a different name
 * than  the referenced definition.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyDefinitionComparator<T extends CmsPropertyDefinition> implements Comparator<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5353218781900250263L;

	private boolean compareByValueType = true;
	
	//Order is significant. the first one has the highest priority
	enum TypePriority
	{
		Simple, Binary, Complex, Invalid 
	}
	
	private CmsPropertyDefinitionLocalizedLabelComparator contentObjectPropertyDefinitionLocalizedLabelComparator;


	public CmsPropertyDefinitionComparator()
	{
		this.contentObjectPropertyDefinitionLocalizedLabelComparator = new CmsPropertyDefinitionLocalizedLabelComparator();
	}
	
	public void setLocale(String locale)
	{
		contentObjectPropertyDefinitionLocalizedLabelComparator.setLocale(locale);
	}
	
	public int compare(T definition0, T definition1) {
		
		if (definition0 == null)
			return -1;
		if (definition1 == null)
			return 1;
		
		try{
					
			//Compare by order
			int orderComparison = compareOrders(definition0, definition1);
			
			if (orderComparison != 0){
				return orderComparison;
			}
			
			//Compare by value types
			int instanceComparison = compareValueTypes(definition0, definition1);
			
			//Similar valueTypes compare localized labels
			if (instanceComparison != 0){
				return instanceComparison;
			}
			
			return contentObjectPropertyDefinitionLocalizedLabelComparator.compare(definition0, definition1);


		}
		catch (Throwable e){
			LoggerFactory.getLogger(getClass()).warn("",e);
			return -1;
		}
	}

	
	private int compareOrders(CmsPropertyDefinition definitionForProperty0,
			CmsPropertyDefinition definitionForProperty1) {
		
		Integer order0 = definitionForProperty0.getOrder();
		Integer order1 = definitionForProperty1.getOrder();
		
		if (order0 == null && order1 == null)
			return 0;
		
		//Sorting is done in ascending order
		//Since order with value closer to 0 is considered
		//of higher priority
		//when comparing two properties and only one of them
		//has order, the latest is considered 'lesser'

		if (order0 != null && order1 == null)
			return -1;
		
		if (order0 == null && order1 != null)
			return 1;

		return order0.compareTo(order1);
	}

	
	private int compareValueTypes(CmsPropertyDefinition cmsPropertyDefinition0, CmsPropertyDefinition cmsPropertyDefinition1) {
		
		//Do not compare value types. Return 0 to continue with localized labels
		if (!compareByValueType)
			return 0;
		
		TypePriority definition0Priority = getDefinitionPriority(cmsPropertyDefinition0);
		TypePriority definition1Priority = getDefinitionPriority(cmsPropertyDefinition1);
		
		return definition0Priority.compareTo(definition1Priority);
		
	}

	private TypePriority getDefinitionPriority(
			CmsPropertyDefinition cmsPropertyDefinition) {
		
		if (isValueTypeInvalid(cmsPropertyDefinition))
			return TypePriority.Invalid;
		
		if (isTypeComplex(cmsPropertyDefinition))
			return TypePriority.Complex;
		
		if (isTypeBinary(cmsPropertyDefinition))
			return TypePriority.Binary;
		
		return TypePriority.Simple;
	}

	private boolean isTypeComplex(CmsPropertyDefinition cmsPropertyDefinition) {
		return cmsPropertyDefinition.getValueType() == ValueType.Complex;
	}
	
	private boolean isTypeBinary(CmsPropertyDefinition cmsPropertyDefinition) {
		return  cmsPropertyDefinition.getValueType() == ValueType.Binary;
	}

	private boolean isValueTypeInvalid(CmsPropertyDefinition cmsPropertyDefinition) {
		return cmsPropertyDefinition == null || cmsPropertyDefinition.getValueType() == null || cmsPropertyDefinition.getValueType() == ValueType.ContentType;
	}

	public void setCompareByValueType(boolean compareByValueType) {
		this.compareByValueType = compareByValueType;
	}

}