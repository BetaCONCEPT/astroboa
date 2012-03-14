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
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.core.Events;
import org.richfaces.event.DropEvent;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ComplexCmsPropertyParentWrapper extends CmsPropertyWrapper<ComplexCmsProperty<?,?>>{

	private List<ComplexCmsPropertyWrapper> childComplexCmsPropertyWrappers;
	private CmsPropertyDefinition childComplexCmsPropertyDefinition;
	
	public ComplexCmsPropertyParentWrapper(ComplexCmsProperty<?,?> parentComplexCmsProperty,  
			CmsPropertyDefinition childComplexCmsPropertyDefinition, 
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory, 
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		
		super(childComplexCmsPropertyDefinition, 
				parentComplexCmsProperty.getPath(), cmsRepositoryEntityFactory, contentObject, wrapperIndex, complexCmsPropertyEdit);
		
		this.cmsProperty = parentComplexCmsProperty;
		this.childComplexCmsPropertyDefinition = childComplexCmsPropertyDefinition;
		
	}

	public String getCmsPropertyIndex(){
		return "";
	}
	
	@Override
	public boolean isMultiple() {
		return childComplexCmsPropertyDefinition == null ? false : childComplexCmsPropertyDefinition.isMultiple();
	}

	@Override
	public boolean isMandatory() {
		return childComplexCmsPropertyDefinition == null ? false : childComplexCmsPropertyDefinition.isMandatory();
	}

	

	@Override
	public String getDefinitionName() {
		return childComplexCmsPropertyDefinition == null ? null : childComplexCmsPropertyDefinition.getName();
	}
	


	@Override
	public String getLocalizedLabelForCurrentLocale() {
		return childComplexCmsPropertyDefinition == null ? null : 
			childComplexCmsPropertyDefinition.getDisplayName().getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
	}

	@Override
	protected String getRestrictReadToRoles() {
		return childComplexCmsPropertyDefinition.getRestrictReadToRoles();
	}
	@Override
	protected String getRestrictToWriteRoles() {
		return  childComplexCmsPropertyDefinition.getRestrictWriteToRoles();
	}
	
	

	@Override
	public CmsPropertyDefinition getCmsPropertyDefinition() {
		return childComplexCmsPropertyDefinition;
	}

	public List<ComplexCmsPropertyWrapper> getChildComplexCmsPropertyWrappers() {
		if (childComplexCmsPropertyWrappers != null)
			return childComplexCmsPropertyWrappers;
		
		if (cmsProperty.hasValueForChildProperty(childComplexCmsPropertyDefinition.getName())){
			childComplexCmsPropertyWrappers = new ArrayList<ComplexCmsPropertyWrapper>();

			List<CmsProperty<?,?>> complexCmsProperties = cmsProperty.getChildPropertyList(childComplexCmsPropertyDefinition.getName());
			if (CollectionUtils.isNotEmpty(complexCmsProperties)){
				for (CmsProperty<?,?> complexCmsProperty : complexCmsProperties)
					childComplexCmsPropertyWrappers.add(new ComplexCmsPropertyWrapper(complexCmsProperty, false,
							childComplexCmsPropertyDefinition, cmsProperty.getPath(), cmsRepositoryEntityFactory, getContentObject(), wrapperIndex, complexCmsPropertyEdit));
			}
		}
		
		return childComplexCmsPropertyWrappers;
	}

	public void deleteAllChildComplexCmsProperties_UIAction(){
		
		if (childComplexCmsPropertyDefinition != null)
		{
			deleteComplexCmsProperty_UIAction(childComplexCmsPropertyDefinition.getName());
		}
	}
	
	//childPropertyPath is relative to cmsProperty
	public void deleteComplexCmsProperty_UIAction(String childPropertyPath){
		// add the wrapper index to the list of wrappers that should be updated by the UI
		complexCmsPropertyEdit.setWrapperIndexesToUpdate(Collections.singleton(wrapperIndex));
		
		try{
			
			cmsProperty.removeChildProperty(childPropertyPath);
			
			//Nullify to force to be reloaded
			childComplexCmsPropertyWrappers = null;
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null,  FacesMessage.SEVERITY_WARN);
			logger.error("Could not remove complex cms property in path "+ childPropertyPath,e);
		}
	}

	@Override
	public void addBlankValue_UIAction() {

		try{

			//Create a template for new complex cms property
			CmsProperty<?, ?> childCmsProperty = cmsProperty.createNewValueForMulitpleComplexCmsProperty(childComplexCmsPropertyDefinition.getName());

			//Notify tree that a property is added
			if (childCmsProperty != null){
				Events.instance().raiseEvent(SeamEventNames.NEW_COMPLEX_CMS_PROPERTY_ADDED, childCmsProperty.getPath(), complexCmsPropertyEdit);
			}
			else{
				logger.warn("Could not create new cms property {}", cmsProperty.getFullPath()+"."+childComplexCmsPropertyDefinition.getName());
			}


		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "application.unknown.error.message", null, FacesMessage.SEVERITY_WARN);
			logger.error("Could not create new complex cms property for path "+ cmsProperty.getFullPath(),e);
		}

		
	}

	public boolean isAspect(){
		return false;
	}
	
	private void swapChildPropertyPositions(String childPropertyName, Integer fromIndex, Integer toIndex) {
		
		if (CollectionUtils.isEmpty(childComplexCmsPropertyWrappers)){
			logger.error("List of child properties is empty.");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		
		if (cmsProperty.swapChildPropertyValues(childPropertyName, fromIndex, toIndex)) {

			ComplexCmsPropertyWrapper fromChildPropertyWrapper = childComplexCmsPropertyWrappers.get(fromIndex);
			ComplexCmsPropertyWrapper toChildPropertyWrapper = childComplexCmsPropertyWrappers.get(toIndex);
			
			fromChildPropertyWrapper.resetCmsPropertyIndex();
			toChildPropertyWrapper.resetCmsPropertyIndex();
			
			Collections.swap(childComplexCmsPropertyWrappers, fromIndex, toIndex);
		}
		else {
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	private void changeChildPropertyPosition(String childPropertyName, Integer fromIndex, Integer toIndex) {
		
		if (CollectionUtils.isEmpty(childComplexCmsPropertyWrappers)){
			logger.error("List of child properties is empty.");
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		
		if (cmsProperty.changePositionOfChildPropertyValue(childPropertyName, fromIndex, toIndex)) {
			
			childComplexCmsPropertyWrappers.add(toIndex, childComplexCmsPropertyWrappers.get(fromIndex));
			if (fromIndex > toIndex) {
				fromIndex++;
			}
			childComplexCmsPropertyWrappers.remove(fromIndex.intValue());
			
			for (ComplexCmsPropertyWrapper complexCmsPropertyWrapper : childComplexCmsPropertyWrappers) {
				complexCmsPropertyWrapper.resetCmsPropertyIndex();
			}
		}
		else {
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
		}
	}
	
	public void addDraggedAndDroppedReference_Listener(DropEvent dropEvent){
		
		ComplexCmsPropertyWrapper fromChildPropertyWrapper = (ComplexCmsPropertyWrapper) dropEvent.getDragValue();
		ComplexCmsPropertyWrapper toChildPropertyWrapper = (ComplexCmsPropertyWrapper) dropEvent.getDropValue();
		
		if (fromChildPropertyWrapper == null || toChildPropertyWrapper == null) {
			logger.error("Either the dragged or the dropped value is null");
			JSFUtilities.addMessage(null, "object.edit.changePropertyValuePosition.failed", null, FacesMessage.SEVERITY_WARN);
		}
		
		Integer fromIndex = childComplexCmsPropertyWrappers.indexOf(fromChildPropertyWrapper);
		
		Integer toIndex = childComplexCmsPropertyWrappers.indexOf(toChildPropertyWrapper);
		
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
		
		changeChildPropertyPosition(fromChildPropertyWrapper.getDefinitionName(), fromIndex, toIndex);
		
	}
	
	public void moveUp_UIAction(ComplexCmsPropertyWrapper childPropertyWrapper){
		
		if (childPropertyWrapper == null){
			logger.warn("No value provided to move");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (CollectionUtils.isEmpty(childComplexCmsPropertyWrappers)){
			logger.error("List of child properties is empty.");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		int valueIndexInList = childComplexCmsPropertyWrappers.indexOf(childPropertyWrapper);
		if (valueIndexInList == -1){
			logger.error("List of values does not contain value {}", childPropertyWrapper.getLocalizedLabelForCurrentLocale());
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (valueIndexInList == 0){
			//Value is already at the top of the list
			return;
		}
		
		swapChildPropertyPositions(childPropertyWrapper.getDefinitionName(), valueIndexInList, valueIndexInList-1);
		
	}
	
	public void moveDown_UIAction(ComplexCmsPropertyWrapper childPropertyWrapper){
		
		if (childPropertyWrapper == null){
			logger.warn("No value provided to move");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (CollectionUtils.isEmpty(childComplexCmsPropertyWrappers)){
			logger.error("List of child properties is empty.");
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		int valueIndexInList = childComplexCmsPropertyWrappers.indexOf(childPropertyWrapper);
		
		if (valueIndexInList == -1){
			logger.error("List of values does not contain value {}", childPropertyWrapper.getLocalizedLabelForCurrentLocale());
			JSFUtilities.addMessage(null, "object.edit.swapPropertyValuePositions.failed", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		if (valueIndexInList == childComplexCmsPropertyWrappers.size()){
			//Value is already at the bottom of the list
			return;
		}
		
		swapChildPropertyPositions(childPropertyWrapper.getDefinitionName(), valueIndexInList, valueIndexInList+1);
	}
}
