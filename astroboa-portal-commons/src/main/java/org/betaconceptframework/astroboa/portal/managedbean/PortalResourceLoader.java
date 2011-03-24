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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.ResourceBundle;

import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceLoader;

/**
 * 
 * Astroboa Resource Loader.
 * 
 * <p>
 * 	This loader overrides Seam Resource Loader and is responsible to instantiate
 *  {@link RepositoryResourceBundle} which loads resource bundles 
 *  saved in appropriate taxonomies. For more info see at {@link RepositoryResourceBundle}.
 * </p>
 * 
 * <p>
 * 	In order to activate {@link RepositoryResourceBundle}, you need to define 
 * 	bundle name {@link PortalStringConstants#REPOSITORY_RESOURCE_BUNDLE_NAME} inside components.xml
 * 
 *  <pre>
 *   &lt;core:resource-loader&gt;
 *		&lt;core:bundle-names&gt;
 *			&lt;value&gt;portal-commons-messages&lt;/value&gt;
 *			&lt;value&gt;messages&lt;/value&gt;
 *			&lt;value&gt;{@link PortalStringConstants#REPOSITORY_RESOURCE_BUNDLE_NAME}&lt;/value&gt;			
 *		&lt;/core:bundle-names&gt;
 *   &lt;/core:resource-loader&gt;		
 *  </pre>
 * </p>
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.STATELESS)  
@Install(precedence = Install.APPLICATION)  
@Name("org.jboss.seam.core.resourceLoader")  
public class PortalResourceLoader extends ResourceLoader{

	@Override
	public ResourceBundle loadBundle(String bundleName) {
		
		if (bundleName != null && PortalStringConstants.REPOSITORY_RESOURCE_BUNDLE_NAME.equals(bundleName)){
			
			return RepositoryResourceBundle.instance(); 
		}
		
		return super.loadBundle(bundleName);
	}
	
	

}
