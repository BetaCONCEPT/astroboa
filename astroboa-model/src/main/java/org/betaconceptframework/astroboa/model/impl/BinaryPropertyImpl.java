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

package org.betaconceptframework.astroboa.model.impl;

import java.io.Serializable;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.MultipleOccurenceException;
import org.betaconceptframework.astroboa.api.model.exception.SingleOccurenceException;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BinaryPropertyImpl extends SimpleCmsPropertyImpl<BinaryChannel, BinaryPropertyDefinition,ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?,?>>> implements BinaryProperty, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8988737054998753029L;

	
	public ValueType getValueType() {
		return ValueType.Binary;
	}

	protected void checkValues(List<BinaryChannel> asList) {
		//Do nothing. Currently no enumeration is provided for Binary properties
		
	}

	@Override
	public void addSimpleTypeValue(BinaryChannel value) {
		super.addSimpleTypeValue(value);
		
		if (hasValues() && values.indexOf(value) > -1)
		{
			provideBinaryChannelNecessaryInfoForBuildingURLs(value, values.indexOf(value));
		}
	}

	@Override
	public void setSimpleTypeValue(BinaryChannel value)
			throws MultipleOccurenceException {
		super.setSimpleTypeValue(value);
		
		provideBinaryChannelNecessaryInfoForBuildingURLs(value, 0);
	}

	public void updateBinaryChannelsWithNecessaryInfoForBuildingURLs(boolean updatePermanentPath)
	{
		if (hasValues())
		{
			for (int i=0;i<values.size();i++){
				BinaryChannel value = values.get(i);
				
				provideBinaryChannelNecessaryInfoForBuildingURLs(value, i);
				
				if (updatePermanentPath){
					try{
						((BinaryChannelImpl)value).setBinaryPropertyPermanentPath(getPermanentPath());
					}
					catch(Exception e){
						LoggerFactory.getLogger(getClass()).error("Unable to set permanent path  to binary channel with index "+ i + " in  property "+ getPath()+". Permanent Path will not be set", e);
					}
				}
			}
		}
	}
	
	private void provideBinaryChannelNecessaryInfoForBuildingURLs(BinaryChannel value, int index) {
		
		//There is no need to retrieve information
		//unless parent property has been provided
		if (value != null){
			((BinaryChannelImpl)value).setContentObjectId(getContentObjectId());
			((BinaryChannelImpl)value).setContentObjectSystemName(getContentObjectSystemName());
			
			if (getPropertyDefinition() != null && getPropertyDefinition().isMultiple()){
				((BinaryChannelImpl)value).binaryPropertyIsMultiValued();
			}
		}
		
	}

	@Override
	public void setSimpleTypeValues(List<BinaryChannel> values)
			throws SingleOccurenceException {
		super.setSimpleTypeValues(values);
		
		if (values != null && ! values.isEmpty()){
			updateBinaryChannelsWithNecessaryInfoForBuildingURLs(false);
		}
	}

	
	@Override
	public void setParent(
			ComplexCmsProperty<? extends ComplexCmsPropertyDefinition, ? extends ComplexCmsProperty<?, ?>> parent) {
		super.setParent(parent);
		
		if (parent != null && hasValues()){
			
			//Update contentObjectId and binary property path to all 
			//if any, binary channel values
			updateBinaryChannelsWithNecessaryInfoForBuildingURLs(false);
		}
	}

	@Override
	public void clean() {
		super.clean();
		
		if (values != null){
			for (BinaryChannel binaryChannel : values){
				((BinaryChannelImpl)binaryChannel).clean();
			}
		}
	}

	@Override
	protected String generateMessageForInvalidValue(BinaryChannel value) {
		return "Provided value "+value+" is invalid";
	}

	@Override
	public void resetPaths() {
		super.resetPaths();
		
		if (values != null){

			for (BinaryChannel binaryChannel : values){
				((BinaryChannelImpl)binaryChannel).setBinaryPropertyPermanentPath(getPermanentPath());
			}
		}

	}
	
}
