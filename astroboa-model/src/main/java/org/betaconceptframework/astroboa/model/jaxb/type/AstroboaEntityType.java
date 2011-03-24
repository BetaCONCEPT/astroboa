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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.impl.LocalizableEntityImpl;
import org.betaconceptframework.astroboa.model.jaxb.listener.AstroboaUnmarshalListener;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AstroboaEntityType extends LocalizableEntityImpl implements CmsPropertyType {

	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = -6767569824805576341L;
	
	@XmlTransient
	private AstroboaUnmarshalListener unMarshalListener;
	
	@XmlTransient
	private QName qname;

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

	public QName getQname() {
		return qname;
	}

	public void setQname(QName qname) {
		this.qname = qname;
	}

	public AstroboaUnmarshalListener getAstroboaUnmarshalListener() {
		return unMarshalListener;
	}

	public void setAstroboaUnmarshalListener(
			AstroboaUnmarshalListener astroboaUnmarshalListener) {
		this.unMarshalListener = astroboaUnmarshalListener;
	}
	
	public CmsRepositoryEntity getCmsRepositoryEntityFromContextUsingCmsIdentifierOrReference() {
		
		if (getId() != null){
			return getCmsRepositoryEntityFromContextUsingKey(getId());
		}
		
		return null; 
	}
	
	public CmsRepositoryEntity getCmsRepositoryEntityFromContextUsingKey(String key) {
		if (unMarshalListener == null){
			throw new CmsException("Found no unmarshaller listener");
		}
		
		return unMarshalListener.getCmsRepositoryEntityFromContext(key);
	}

	public void registerCmsRepositoryEntityToContext(CmsRepositoryEntity cmsRepositoryEntity) {
		
		if (unMarshalListener != null){
			unMarshalListener.registerCmsRepositoryEntityToContext(cmsRepositoryEntity);
		}
		
	}

	public void setUrl(String url){
		this.url = url;
	}
	
	public Boolean isExportAsAnArray() {
		return exportAsAnArray;
	}

	public void setExportAsAnArray(Boolean exportAsAnArray) {
		this.exportAsAnArray = exportAsAnArray;
	}

}

