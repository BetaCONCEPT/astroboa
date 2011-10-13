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
package org.betaconceptframework.astroboa.console.jsf.edit;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ComplexCmsPropertyWrapper extends CmsPropertyWrapper<ComplexCmsProperty>{

	private String cmsPropertyIndex;
	private boolean aspect;

	public ComplexCmsPropertyWrapper(
			CmsProperty complexCmsProperty, 
			boolean aspect, 
			CmsPropertyDefinition definition, 
			String parentCmsPropertyPath,
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory,
			ContentObject contentObject, 
			int wrapperIndex,
			ComplexCmsPropertyEdit complexCmsPropertyEdit) {
		super(definition, parentCmsPropertyPath, cmsRepositoryEntityFactory, contentObject, wrapperIndex, complexCmsPropertyEdit);

		this.cmsProperty = (ComplexCmsProperty) complexCmsProperty;
		this.aspect = aspect;
	}


	public String getCmsPropertyIndex(){
		if (cmsPropertyIndex == null){
			if (cmsProperty != null) {

				if (!cmsProperty.getPropertyDefinition().isMultiple())
					cmsPropertyIndex = "";
				else{
					int index = PropertyPath.extractIndexFromPath(cmsProperty.getPath());
					if (index == PropertyPath.NO_INDEX)
						index=0;

					cmsPropertyIndex =  CmsConstants.LEFT_BRACKET+index+CmsConstants.RIGHT_BRACKET;
				}
			}
			else
				cmsPropertyIndex = "";
		}

		return cmsPropertyIndex;
	}

	public void deleteSingleValue_UIAction(){
		if (cmsProperty != null){
			try{

				//Just get its parent and remove this instance
				//We need property name with its index (if any)
				//Thus we get property path and keep only the last part
				String cmsPropertyPath = PropertyPath.getLastDescendant(cmsProperty.getPath());
				
				if (cmsProperty.getParentProperty() != null){
					if (cmsProperty.getParentProperty().removeChildProperty(cmsPropertyPath)){
						//Nullify properties
						cmsProperty = null;
						cmsPropertyIndex = null;
						
					}
				}
				else{
					logger.warn("CmsProperty "+ cmsProperty.getFullPath() + " does not have a parent");
				}
				
			}
			catch (Exception e){
				logger.error("",e);
			}
		}
	}
	
	@Override
	public void addBlankValue_UIAction() {

		//Do nothing
	}

	public boolean isAspect(){
		return aspect;
	}
	
	public void resetCmsPropertyIndex()
	{
		cmsPropertyIndex = null;
	}
	
}
