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
package org.betaconceptframework.astroboa.api.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * Principal representing a user's primary identity , username or userid
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class IdentityPrincipal implements Principal, Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7623524312180977244L;

	public final static String ANONYMOUS = "anonymous";
	
	public final static String SYSTEM = "SYSTEM";
	
	
	private String identity;

	   public IdentityPrincipal(String identity)
	   {
	      this.identity = identity;
	   }

	   public boolean equals(Object otherPrincipal)
	   {
	      if (!(otherPrincipal instanceof IdentityPrincipal)){
	         return false;
	      }
	      
	      String otherName = ((Principal) otherPrincipal).getName();
	      
	      if (identity == null){
	         return otherName == null;
	      }
	      
	      return identity.equals(otherName);
	   }

	   public int hashCode()
	   {
	      return (identity == null ? 0 : identity.hashCode());
	   }

	   
	   @Override
	   public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("IdentityPrincipal [Identity=");
			builder.append(identity);
			builder.append("]");
			return builder.toString();
		}

	   public String getName()
	   {
	      return identity;
	   }

}
