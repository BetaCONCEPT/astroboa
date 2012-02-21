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

package org.betaconceptframework.astroboa.console.commons;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for retrieving properties of ContentObject
 * from Map style EL expressions (eg. #{contentObjectUIWrapper.contentObjectProperty['thumbnail'].binaryChannels[0].uuid} used in jsf pages)
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 */
public class CmsPropertyProxy  implements Map{

	private  final Logger logger = LoggerFactory.getLogger(getClass());
	private ContentObject contentObject;

	public CmsPropertyProxy(ContentObject contentObject)
	{
		this.contentObject = contentObject;
	}
	public void clear() {
		
		
	}

	public boolean containsKey(Object key) {
		
		return false;
	}

	public boolean containsValue(Object value) {
		
		return false;
	}

	public Set entrySet() {
		
		return null;
	}

	public Object get(Object key) {
		
		if (key instanceof String)
		{
			try{
				return contentObject.getCmsProperty((String)key);
			}
			catch (Exception e){
				//Log exception but do not rethrow it
				logger.warn("An xhtml asked for property " + key +
						" for content object "+ contentObject.getContentObjectType(), e);
				return null;
			}
		}
		
		return null;
	}

	public boolean isEmpty() {
		
		return false;
	}

	public Set keySet() {
		
		return null;
	}

	public Object put(Object key, Object value) {
		
		return null;
	}

	public void putAll(Map m) {
		
		
	}

	public Object remove(Object key) {
		
		return null;
	}

	public int size() {
		
		return 0;
	}

	public Collection values() {
		
		return null;
	}

}
