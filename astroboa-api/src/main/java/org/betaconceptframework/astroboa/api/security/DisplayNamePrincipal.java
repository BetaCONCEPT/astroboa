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
package org.betaconceptframework.astroboa.api.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * The name of the user identified by {@link IdentityPrincipal}, 
 * suitable for display to end-users. 
 * The name SHOULD be the full name of the user being described 
 * if known (e.g. Cassandra Doll or Mrs. Cassandra Lynn Doll, Esq.), 
 * but MAY be a username or handle, if that is all that is available (e.g. doll). 
 * The value provided SHOULD be the primary textual label by which this user is 
 * normally displayed by the Service Provider when presenting it to end-users.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DisplayNamePrincipal implements Principal, Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -7471126971362561470L;
	private String displayName;

	   public DisplayNamePrincipal(String displayName)
	   {
	      this.displayName = displayName;
	   }

	   public boolean equals(Object otherPrincipal)
	   {
	      if (!(otherPrincipal instanceof DisplayNamePrincipal)){
	         return false;
	      }
	      
	      String otherName = ((Principal) otherPrincipal).getName();
	      
	      if (displayName == null){
	         return otherName == null;
	      }
	      
	      return displayName.equals(otherName);
	   }

	   public int hashCode()
	   {
	      return (displayName == null ? 0 : displayName.hashCode()) + DisplayNamePrincipal.class.hashCode();
	   }

	   @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DisplayNamePrincipal [Display Name=");
		builder.append(displayName);
		builder.append("]");
		return builder.toString();
	}

	   public String getName()
	   {
	      return displayName;
	   }

}
