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
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.richfaces.event.DropEvent;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class MultipleSimpleCmsPropertyWrapper<T extends SimpleCmsProperty<?,?,?>> extends CmsPropertyWrapper<T>{

	protected List<SimpleCmsPropertyValueWrapper> simpleCmsPropertyValueWrappers = new ArrayList<SimpleCmsPropertyValueWrapper>();
	
	public MultipleSimpleCmsPropertyWrapper(
			CmsPropertyDefinition cmsPropertyDefinition,
			String parentCmsPropertyPath,
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory,
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		super(cmsPropertyDefinition,
				parentCmsPropertyPath, 
				cmsRepositoryEntityFactory,
				contentObject, wrapperIndex, complexCmsPropertyEdit);
	}
	
	private void swapPropertyValuePositions(Integer fromIndex, Integer toIndex) {
		
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			logger.error("List of values is empty. This method should never be invoked when value list is empty");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		
		if (cmsProperty.swapValues(fromIndex, toIndex)) {
			
			SimpleCmsPropertyValueWrapper fromSimpleCmsPropertyValueWrapper = simpleCmsPropertyValueWrappers.get(fromIndex);
			SimpleCmsPropertyValueWrapper toSimpleCmsPropertyValueWrapper = simpleCmsPropertyValueWrappers.get(toIndex);
			
			fromSimpleCmsPropertyValueWrapper.changeIndex(toIndex);
			toSimpleCmsPropertyValueWrapper.changeIndex(fromIndex);
			
			Collections.swap(simpleCmsPropertyValueWrappers, fromIndex, toIndex);
		}
		else {
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	private void changePropertyValuePosition(Integer fromIndex, Integer toIndex) {
		
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			logger.error("List of values is empty. This method should never be invoked when value list is empty");
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		
		if (cmsProperty.changePositionOfValue(fromIndex, toIndex)) {
			
			simpleCmsPropertyValueWrappers.add(toIndex, simpleCmsPropertyValueWrappers.get(fromIndex));
			if (fromIndex > toIndex) {
				fromIndex++;
			}
			simpleCmsPropertyValueWrappers.remove(fromIndex.intValue());
			
			int index=0;
			for (SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper : simpleCmsPropertyValueWrappers) {
				simpleCmsPropertyValueWrapper.changeIndex(index);
				++index;
			}
		}
		else {
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	public void addDraggedAndDroppedReference_Listener(DropEvent dropEvent){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		Integer fromIndex = (Integer) dropEvent.getDragValue();
		
		Integer toIndex = (Integer) dropEvent.getDropValue();
		
		if (fromIndex == null || toIndex == null || fromIndex < 0 || toIndex < 0){
			logger.warn("Cannot drag value from {} to {}", fromIndex, toIndex);
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
			return ;
		}
		
		if (fromIndex.equals(toIndex)){
			return;
		}
		
		logger.debug("Moving value  from index "+
				fromIndex+ " to index "+ toIndex);
		
		changePropertyValuePosition(fromIndex, toIndex);
		
	}

	
	public void moveUp_UIAction(SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper){
		
		if (simpleCmsPropertyValueWrapper == null){
			logger.warn("No value provided to move");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			logger.error("List of values is empty. This method should never be invoked when value list is empty");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		int valueIndexInList = simpleCmsPropertyValueWrappers.indexOf(simpleCmsPropertyValueWrapper);
		if (valueIndexInList == -1){
			logger.error("List of values does not contain value {}", simpleCmsPropertyValueWrapper.getSimpleCmsPropertyLocalizedLabelOfFullPathforLocale());
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (valueIndexInList == 0){
			//Value is already at the top of the list
			return;
		}
		
		logger.debug("Moving value "+ simpleCmsPropertyValueWrapper.getSimpleCmsPropertyLocalizedLabelOfFullPathforLocale() + " from index "+
				valueIndexInList + " to index "+ (valueIndexInList-1));
		
		swapPropertyValuePositions(valueIndexInList, valueIndexInList-1);
		
		
		
	}
	
	public void moveDown_UIAction(SimpleCmsPropertyValueWrapper simpleCmsPropertyValueWrapper){
		
		if (simpleCmsPropertyValueWrapper == null){
			logger.warn("No value provided to move");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (CollectionUtils.isEmpty(simpleCmsPropertyValueWrappers)){
			logger.error("List of values is empty. This method should never be invoked when value list is empty");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		int valueIndexInList = simpleCmsPropertyValueWrappers.indexOf(simpleCmsPropertyValueWrapper);
		
		if (valueIndexInList == -1){
			logger.error("List of values does not contain value {}", simpleCmsPropertyValueWrapper.getSimpleCmsPropertyLocalizedLabelOfFullPathforLocale());
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (valueIndexInList == simpleCmsPropertyValueWrappers.size()){
			//Value is already at the bottom of the list
			return;
		}
		
		swapPropertyValuePositions(valueIndexInList, valueIndexInList+1);
		
	}
}
