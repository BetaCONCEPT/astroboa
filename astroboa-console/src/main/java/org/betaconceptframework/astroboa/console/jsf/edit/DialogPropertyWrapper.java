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
package org.betaconceptframework.astroboa.console.jsf.edit;

import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author gchomatas
 *
 */
@Name("dialogPropertyWrapper")
@Scope(ScopeType.SESSION)
/**
 * This session bean holds the property wrapper that is used in
 * the topic selection, object selection and file upload dialog boxes.
 * Dialog boxes need to know the wrapper in order to update its value.
 * So before a dialog is opened the relevant wrapper is set in this bean so that 
 * the dialog box knows for which object property values are edited.
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DialogPropertyWrapper extends AbstractUIBean {

	private static final long serialVersionUID = 1L;

	public DialogPropertyWrapper() {

	}
	
	private CmsPropertyWrapper<?> cmsPropertyWrapper;
	
	
	public CmsPropertyWrapper<?> getCmsPropertyWrapper() {
		return cmsPropertyWrapper;
	}

	public void setCmsPropertyWrapper(CmsPropertyWrapper<?> cmsPropertyWrapper) {
		this.cmsPropertyWrapper = cmsPropertyWrapper;
	}



}
