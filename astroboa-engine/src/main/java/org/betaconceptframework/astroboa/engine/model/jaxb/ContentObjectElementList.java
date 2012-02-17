/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.model.jaxb;

import java.util.ArrayList;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectElementList extends ArrayList<ContentObject>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4314628063421976260L;
	private Deserializer deserializer;
	
	public ContentObjectElementList() {
		super();
	}

	@Override
	public boolean add(ContentObject e) {
		
		boolean importSuccess = false;
		
		if (deserializer != null)
		{
			//Simply save provided element and do not store in list
			//deserializer.save(e);
			
			importSuccess = true;
		}
		
		if (!importSuccess)
		{
			return super.add(e);
		}
		
		return true;
		
		
	}


	public void setDeserializer(Deserializer deserializer) {
		if (this.deserializer == null)
		{
			this.deserializer = deserializer;
		}
		
	}
}
