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

package org.betaconceptframework.astroboa.api.service.secure.remote;



import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.api.service.secure.RepositoryUserServiceSecure;

/**
 * Secure RepositoryUser Service API. 
 * 
 * <p>
 * It contains methods provided by 
 * {@link RepositoryUserService} with the addition that each method requires
 * an authentication token as an extra parameter, in order to ensure
 * that client has been successfully logged in a Astroboa repository and
 * therefore has been granted access to further use Astroboa services
 * </p>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RemoteRepositoryUserServiceSecure extends RepositoryUserServiceSecure {
}
