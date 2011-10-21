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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.BooleanUtils;

/**
 * Filter a variable of Boolean type in such a way that so that
 * when it is marshaled to an XML or JSON attribute, it will appear only when
 * its value is true. 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BooleanAdapter extends XmlAdapter<Boolean,Boolean>{

	@Override
	public Boolean marshal(Boolean v) throws Exception {
		if (BooleanUtils.isTrue(v)){
			return v;
		}
		
		/*
		 * FindBugs (v 2.3.1) reports the following for the fact that we explicitly return null
		 * 
		 * NP: Method with Boolean return type returns explicit null (NP_BOOLEAN_RETURN_NULL)
		 * A method that returns either Boolean.TRUE, Boolean.FALSE or null is an accident waiting to happen. 
		 * This method can be invoked as though it returned a value of type boolean, and the compiler will insert 
		 * automatic unboxing of the Boolean value. If a null value is returned, this will result in a NullPointerException.
		 * 
		 * However, since this  is used only by JAXB, JAXB handles the null value without any NPE. That means that
		 * it process the Boolean result as Boolean and not as boolean.
		 */
		return null;
	}

	@Override
	public Boolean unmarshal(Boolean v) throws Exception {
		return BooleanUtils.isTrue(v);
	}


}
