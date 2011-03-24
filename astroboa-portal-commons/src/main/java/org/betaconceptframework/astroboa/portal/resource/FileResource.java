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
package org.betaconceptframework.astroboa.portal.resource;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("fileResource")
@Scope(ScopeType.PAGE)
public class FileResource extends AbstractContentObjectResource<ContentObjectResourceContext> {
	
	@RequestParameter
	protected String fileResourceContentType;
	
	protected ContentObjectResourceContext newResourceContext() {
		return new ContentObjectResourceContext();
	}

	@Override
	protected void outjectCustomResourceCollectionRequestParameters() {
		outjectAttribute("fileResourceContentType", fileResourceContentType);
	}

	@Override
	protected void outjectCustomSingleResourceRequestParameters() {
		outjectAttribute("fileResourceContentType", fileResourceContentType);
	}

}
