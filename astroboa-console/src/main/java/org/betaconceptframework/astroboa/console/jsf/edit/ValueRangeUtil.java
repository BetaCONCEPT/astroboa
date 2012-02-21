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
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.annotations.Name;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on Apr 29, 2009
 */

@Name("valueRangeUtil")
public class ValueRangeUtil {
	
	public List<SelectItem> valueRangeAsSelectItems(CmsProperty<?,?> valueRangeCmsProperty) {
		List<SelectItem> valueRangeSelectItems = new ArrayList<SelectItem>();
	    
		Map<?,Localization> valueRange = ((SimpleCmsPropertyDefinition<?>)valueRangeCmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueRange != null){
			
			valueRangeSelectItems.add(new SelectItem("", JSFUtilities.getLocalizedMessage("object.edit.select.box.no.value", null)));
			
			for (Object value : valueRange.keySet()){
				valueRangeSelectItems.add(new SelectItem(value,((Localization)valueRange.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()))); 
			}
		}
		
	    return valueRangeSelectItems;
	}
	
	public String localizedLabelOfValueInValueRange(CmsProperty<?,?> valueRangeCmsProperty, Object value){
		
	    
		Map<?,Localization> valueRange = ((SimpleCmsPropertyDefinition<?>)valueRangeCmsProperty.getPropertyDefinition()).getValueEnumeration();
		
		if (valueRange != null && valueRange.get(value) != null) {
			
			return	((Localization)valueRange.get(value)).getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()); 
		}
		else {
			return "";
		}
		
	}
}
