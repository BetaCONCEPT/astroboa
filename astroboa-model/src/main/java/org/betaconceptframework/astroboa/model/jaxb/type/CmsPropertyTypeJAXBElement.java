/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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
package org.betaconceptframework.astroboa.model.jaxb.type;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPropertyTypeJAXBElement<T extends CmsPropertyType> extends JAXBElement<T> implements CmsPropertyType{

	/**
	 * 
	 */
	private static final long serialVersionUID = -118801957154834500L;

	public CmsPropertyTypeJAXBElement(QName name,
			Class<T> declaredType, Class scope,
			CmsPropertyType value) {
		super(name, declaredType, scope, (T) value);
	}

	public CmsPropertyTypeJAXBElement(QName name,
			Class<T> declaredType, CmsPropertyType value) {
		super(name, declaredType, (T) value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public QName getQname() {
		return getName();
	}

	@Override
	public void setQname(QName qname) {
		//This is used only for marshalling. QName is defined always at constrcutor
	}

	@Override
	public Boolean isExportAsAnArray() {
		return false;
	}

	@Override
	public void setExportAsAnArray(Boolean exportAsAnArray) {
		
	}
	
}
