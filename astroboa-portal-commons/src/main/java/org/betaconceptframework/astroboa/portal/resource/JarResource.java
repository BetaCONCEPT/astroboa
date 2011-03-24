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

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Resources;
import org.jboss.seam.web.AbstractResource;

/**
 * This class is a subclass of AbstractResource class provided by Seam Framework.
 * It utilizes the Seam Resource Servlet functionality
 * to provide web resources stored inside jars.
 * The resources might be common CSS style sheets and javascript files
 * shared between different content applications that use the astroboa-portal-commons library.
 * This class complements the functionality of other classes in this package which provide resources
 * stored inside astroboa repositories. It provides the possibility to serve resources that the developer 
 * prefers to store in jar files.
 * 
 * The jar file path which may hold such resources is hardcoded and should be:
 * $HARDCODED_PATH_INSIDE_JAR = org/betaconceptframework/astroboa/portal/resource/web/*
 * 
 * e.g. org/betaconceptframework/astroboa/portal/resource/web/mycss
 * 
 * A number of default js and css files are arleady included in:
 *  org/betaconceptframework/astroboa/portal/resource/web/js
 *  and
 *  org/betaconceptframework/astroboa/portal/resource/web/css
 *  
 *  The URI to access this kind of resources should be:
 *  $CONTEXT_PATH/astroboa/resource/jar/'SUB_PATH under the $HARDCODED_PATH_INSIDE_JAR'
 *  
 *  e.g. <link href="/portal/astroboa/resource/jar/mycss/layout.css" rel="stylesheet" type="text/css" /> if the resource is located in:
 *  	org/betaconceptframework/astroboa/portal/resource/web/mycss/layout.css
 *  
 *  TODO: provide the possibility for the user to store web resources in other jar file paths and register these secure paths in a properties or xml config file
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(APPLICATION)
@Name("org.betaconceptframework.astroboa.portal.resource.jarResource")
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class JarResource extends AbstractResource
{
   public static final String JAR_RESOURCE_PATH = "/astroboa/resource/jar";
   
   private static final String RESOURCE_PATH = "/jar";
   
   @Override
   public String getResourcePath()
   {
      return RESOURCE_PATH;
   }
   
   @Override
   public void getResource(HttpServletRequest request, HttpServletResponse response)
      throws IOException
   {
      String pathInfo = request.getPathInfo().substring(getResourcePath().length()); 
      
      InputStream in = Resources.getResourceAsStream( "org/betaconceptframework/astroboa/portal/resource/web" + pathInfo, getServletContext() );
      
      try {
          if (in != null) {
              byte[] buffer = new byte[1024];
              int read = in.read(buffer);
              while (read != -1) {
                  response.getOutputStream().write(buffer, 0, read);
                  read = in.read(buffer);
              }
              response.getOutputStream().flush();
          } else {
              response.sendError(HttpServletResponse.SC_NOT_FOUND);
          }
      } finally {
          Resources.closeStream(in);
      }
   }


}