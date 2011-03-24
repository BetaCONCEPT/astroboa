package org.betaconceptframework.astroboa.portal.facelets;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class UILet extends UIComponentBase {
	public String getFamily() {
		return "org.betaconceptframework.astroboa.portal.facelets.Let";
	}

	public void encodeBegin(FacesContext context) throws IOException {
		String var = (String) getAttributes().get("var");
		Object value = getAttributes().get("value");
		
		//No point in saving variable with null value
		if (value != null){
			Contexts.getPageContext().set(var, value);
		}
		else{
			//Null value provided. If var exists, remove it
			if (Contexts.getPageContext().isSet(var)){
				Contexts.getPageContext().remove(var);
			}
		}
		//context.getExternalContext().getRequestMap().put(var, value);
	}

	public void encodeEnd(FacesContext context) throws IOException {
		String var = (String) getAttributes().get("var");
		
		Contexts.getSessionContext().remove(var);
		// context.getExternalContext().getRequestMap().remove(var);
	}

}
