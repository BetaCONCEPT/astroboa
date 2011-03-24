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
package org.betaconceptframework.astroboa.context;

import javax.jcr.Session;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;

/**
 * Callback interface which allows to execute Jcr code in Astroboa repository
 * in combination with JcrContext.
 * 
 * public void doInAstroboaUsingJcrCode() {
        jcrContext.executeCallback(new JcrCallback() {

            public Object executeInJcr(Session session) throws CmsException {
            //Session is ready to be used
                Node root = session.getRootNode();
                
                //Play with jcr 
                
                //Always save session
                session.save();
                
                return null;
            }
        });
    } 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface JcrCallback {

	Object executeInJcr(Session session) throws CmsException;
}
