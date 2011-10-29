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

package org.betaconceptframework.astroboa.engine.definition;


import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.item.ItemQNameImpl;

/**
 * Qualified names of XML schema attributes and elements
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum XSSchemaItem implements ItemQName{
	
	
	
	Annotation("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Documentation("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Lang(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI),
	Base64Binary("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	String("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Long("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Boolean("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Double("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	DateTime("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Date("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	NormalizedString("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Float("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Decimal("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Integer("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	NonPositiveInteger("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	NonNegativeInteger("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	NegativeInteger("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Int("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	PositiveInteger("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	UnsignedLong("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Short("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	UnsignedInt("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Byte("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	UnsignedByte("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	Time("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	AnyURI("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	GYear("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	GYearMonth("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	GMonth("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	GMonthDay("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI), 
	GDay("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI),
	Language("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	
	
	private ItemQName xsSchemaItem;

	private XSSchemaItem(String prefix,String url)
	{
		//Uncapitalize first letter
		xsSchemaItem = new ItemQNameImpl(prefix, url, StringUtils.uncapitalize(this.name()));
	}

	public String getJcrName()
	{
		return xsSchemaItem.getJcrName();
	}

	public String getLocalPart() {
		return xsSchemaItem.getLocalPart();
	}

	public String getNamespaceURI() {
		return xsSchemaItem.getNamespaceURI();
	}

	public String getPrefix() {
		return xsSchemaItem.getPrefix();
	}

	public boolean equals(ItemQName otherItemQName) {
		return xsSchemaItem.equals(otherItemQName);
	}
	public boolean equalsTo(ItemQName otherItemQName) {
		return equals(otherItemQName);
	}

}
