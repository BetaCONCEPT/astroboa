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
package org.betaconceptframework.astroboa.model.jaxb.listener;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Marshaller.Listener;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.model.jaxb.type.AstroboaEntityType;
import org.betaconceptframework.astroboa.model.jaxb.type.ComplexCmsPropertyType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaMarshalListener extends Listener{

	private List<String> alreadyMarshalledCmsRepositoryEntities = new ArrayList<String>();
	private boolean jsonOutput;
	
	public AstroboaMarshalListener(boolean jsonOutput) {
		this.jsonOutput = jsonOutput;
	}

	@Override
	public void beforeMarshal(Object source) {
		
		if (source instanceof ComplexCmsPropertyType){
			
			if (jsonOutput){
				((ComplexCmsPropertyType)source).setXsiType(null);
			}
			
		}
		else if (source instanceof CmsRepositoryEntity){
		
			final String id = ((CmsRepositoryEntity)source).getId();
			
			if (id !=null){
				//Although this entity has not been marshaled yet
				//we must keep its id in case any if its children refer to it
				//An example is when marshaling a taxonomy which contains
				//topics which refer to the taxonomy
				addIdToList(id);
			}
		}
		
	}

	private void addIdToList(String id) {
		
		if (id != null && ! entityIsMarshalled(id))
		{
			alreadyMarshalledCmsRepositoryEntities.add(id);
		}
	}

	public boolean entityIsMarshalled(String id) 
	{
		return alreadyMarshalledCmsRepositoryEntities.contains(id);
	}

	@Override
	public void afterMarshal(Object source) {
		if (source instanceof AstroboaEntityType){
			
			String id = ((AstroboaEntityType)source).getId();
			
			addIdToList(id);
		}
		else if (source instanceof CmsRepositoryEntity){
			String id = ((CmsRepositoryEntity)source).getId();
			
			addIdToList(id);
		}
		else if (source instanceof ComplexCmsPropertyType && ! jsonOutput){
			String id = ((ComplexCmsPropertyType)source).getCmsIdentifier();
			
			addIdToList(id);
		}
	}
	
	public void clearContext(){
		this.alreadyMarshalledCmsRepositoryEntities.clear();
	}
	
}
