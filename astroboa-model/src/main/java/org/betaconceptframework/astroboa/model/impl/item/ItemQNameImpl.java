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

package org.betaconceptframework.astroboa.model.impl.item;


import java.io.Serializable;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.PropertyPath;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ItemQNameImpl implements ItemQName, Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3188123231594260542L;
	
	private QName qualifiedName;
	private String jcrName;



	public ItemQNameImpl()
	{
		this.qualifiedName = new QName("","","");
	}

	public ItemQNameImpl(String prefix, String namespaceUrl, String localPart)
	{
		if (prefix == null)
			prefix = XMLConstants.DEFAULT_NS_PREFIX;

		this.qualifiedName = new QName(namespaceUrl, localPart, prefix);
	}

	public String getJcrName()
	{
		if (jcrName == null)
		{

			String separator = "";
			String localPart = qualifiedName.getLocalPart();
			String prefix = qualifiedName.getPrefix();
			if (StringUtils.isNotBlank(prefix)  && 
					StringUtils.isNotBlank(localPart) && 
					! localPart.equals("*") && 
					! localPart.contains(CmsConstants.QNAME_PREFIX_SEPARATOR))
				separator = CmsConstants.QNAME_PREFIX_SEPARATOR;

			jcrName =  PropertyPath.getJcrPropertyPath(prefix + separator + localPart);
		}

		return jcrName;

	}

	public String getLocalPart() {
		return qualifiedName.getLocalPart();
	}

	public String getNamespaceURI() {
		return qualifiedName.getNamespaceURI();
	}

	public String getPrefix() {
		return qualifiedName.getPrefix();
	}

	@Override
	public int hashCode() {
		return this.qualifiedName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		//Both objects are the same class, ItemQNameImpl
		if (obj instanceof ItemQNameImpl){
			final ItemQNameImpl other = (ItemQNameImpl) obj;

			return this.qualifiedName.equals(other.qualifiedName);
		}
		else if (obj instanceof ItemQName){
			//Object to be compared is of type IitemQName
			//Use its equals method
			final ItemQName other = (ItemQName) obj;

			//Compare namespaces
			String thisNamespaceURI = this.getNamespaceURI();
			String otherNamespaceURI = other.getNamespaceURI();
			
			if (thisNamespaceURI == null){
				if (otherNamespaceURI != null){
					return false;
				}
				else{
					return localPartsAreEqual(other.getLocalPart());
				}
			}
			else{
				if (otherNamespaceURI == null){
					return false;
				}
				else{
					boolean namespaceURIsAreEqual = thisNamespaceURI.equals(otherNamespaceURI);
				
					if (namespaceURIsAreEqual){
						return localPartsAreEqual(other.getLocalPart());
					}
					else{
						return false;
					}
				}
			}
		}

		return false;
	}

	private boolean localPartsAreEqual(String otherLocalPart) {
		String thisLocalPart = this.getLocalPart();
		
		if (thisLocalPart == null){
			return otherLocalPart == null;
		}
		else{
			if (otherLocalPart == null){
				return false;
			}
			else{
				return thisLocalPart.equals(otherLocalPart);
			}
		}
	}

	@Override
	public String toString() {
		return qualifiedName.toString();
	}
	
	

}