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
package org.betaconceptframework.astroboa.model.jaxb.type;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "complexCmsPropertyType", propOrder = {
    "cmsProperties"
})
public class ComplexCmsPropertyType implements CmsPropertyType{

	@XmlAttribute
	private String cmsIdentifier;

    @XmlAnyElement(lax = true)
    private List<CmsPropertyType> cmsProperties;
    
	@XmlTransient
	private QName qname;


    //Ugly way to produce xsi:type attribute
    //as JAXB does not automatically provides that
    //information due to the generic nature of our model.
    //For JAXB there is not enough discrete information about all
    //the possible elements and their types.
    @XmlAttribute(name="type", namespace=XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)
    private String xsiType;
    
	/*
	 * This pseudo attribute is used to instruct JSONXmlStreamWriter that the
	 * element representing this simple property has multiple values
	 * and therefore it should be exported as an array. By default
	 * in JSON, an element is exported as an array only if there are more
	 * than one values to render.
	 * 
	 * This flag is useful in cases where multi value property contains
	 * only one value and export in JSON is enabled.
	 * 
	 * Therefore, in order to disable this attribute in XML export
	 * we use Boolean type instead of primitive type boolean and default value
	 * to null.
	 */
	@XmlAttribute(name=CmsConstants.EXPORT_AS_AN_ARRAY_INSTRUCTION)
    private Boolean exportAsAnArray;

	public List<CmsPropertyType> getCmsProperties() {
        if (cmsProperties == null) {
            cmsProperties = new ArrayList<CmsPropertyType>();
        }
        return this.cmsProperties;
    }


	public String getCmsIdentifier() {
		return cmsIdentifier;
	}


	public void setCmsIdentifier(String cmsIdentifier) {
		this.cmsIdentifier = cmsIdentifier;
	}

	public String getXsiType() {
		return xsiType;
	}

	public void setXsiType(String xsiType) {
		this.xsiType = xsiType;
	}

	public QName getQname() {
		return qname;
	}

	public void setQname(QName qname) {
		this.qname = qname;
	}	

	public Boolean isExportAsAnArray() {
		return exportAsAnArray;
	}

	public void setExportAsAnArray(Boolean exportAsAnArray) {
		this.exportAsAnArray = exportAsAnArray;
	}

}
