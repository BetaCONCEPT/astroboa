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
package org.betaconceptframework.astroboa.console.commons;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class DublicateRepositoryUserExternalIdException extends CmsException {

	public DublicateRepositoryUserExternalIdException(String dublicateExternalId) {
		super("More than one repository users with the same External Id " +
				"(i.e. the id by which the user is known to the external system -DB / LDAP / CRM - which holds user accounts)  " +
				"have been found in the repository. The Dublicate ud is: " + dublicateExternalId + " Dublicate External Ids are not allowed and there is a potencial problem with the repository. " +
				"Please report this to the help desk.");
	}
}
