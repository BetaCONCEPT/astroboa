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
package org.betaconceptframework.astroboa.api.security.management;


/**
 * Market interface representing the remote version {@link IdentityStore}
 * 
 * <p>
 * It is needed when a custom IdentityStore is used with ASTROBOA login and moreover
 * remote access to ASTROBOA is a requirement as well. 
 * </p>
 * 
 * <p>
 * In order to successfully use a custom IdentityStore remotely, users must create an EJB3 with the 
 * following annotations
 * 
 * @Local({IdentityStore.class})
 * @Remote({RemoteIdentityStore.class})
 * 
 * 
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on May 30, 2009
 * 
 */
public interface RemoteIdentityStore extends IdentityStore{
	
}
