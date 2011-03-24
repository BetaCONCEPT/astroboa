
package org.betaconceptframework.astroboa.engine.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.betaconceptframework.astroboa.engine.jcr.io.Deserializer;
import org.betaconceptframework.astroboa.model.jaxb.adapter.ContentObjectAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
public class ContentObjectContainer {

	@XmlAnyElement(lax=true)
	@XmlJavaTypeAdapter(value=ContentObjectAdapter.class)
    protected ContentObjectElementList contentObjectElementList;

    public ContentObjectElementList getContentObjectElementList() {
        if (contentObjectElementList == null) {
        	contentObjectElementList = new ContentObjectElementList();
        }
        return this.contentObjectElementList;
    }


	public void setDeserializer(Deserializer deserializer) {
		getContentObjectElementList().setDeserializer(deserializer);
		
	}

}
