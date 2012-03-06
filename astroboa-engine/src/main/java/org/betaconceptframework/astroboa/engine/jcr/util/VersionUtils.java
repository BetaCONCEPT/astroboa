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

package org.betaconceptframework.astroboa.engine.jcr.util;


import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionHistory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class VersionUtils {

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	/**
	 * VersionHistory is only used in ContentObject nodes
	 * @param session
	 * @param contetObjectId
	 * @return
	 * @throws RepositoryException
	 */
	public  VersionHistory getVersionHistoryForNode(Session session, String contetObjectId) throws  RepositoryException
	{
		Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contetObjectId);
		if (contentObjectNode == null)
		{
			return null;
		}
		
		return session.getWorkspace().getVersionManager().getVersionHistory(contentObjectNode.getPath());
		
	}
	
}
