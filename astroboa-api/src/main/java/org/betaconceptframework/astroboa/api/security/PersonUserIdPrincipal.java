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

import org.betaconceptframework.astroboa.api.security.management.Person;

/**
 * Principal representing a person's userid. 
 * 
 * <p>
 * Astroboa provides the following semantics for a person's userid : <br/> 
 *  "A user ID number, usually chosen automatically, 
 * and usually numeric but sometimes alphanumeric, e.g. '12345' or '1Z425A'."
 *  </p>
 *  
 *  <p>
 *  If Astroboa JAAS module is used, the value of this principal is extracted from
 *  {@link Person#getUserid()}, so it's up to Astroboa IdentityStore's implementation
 *  to determine the semantics of this principal and to provide the appropriate 
 *  value.
 *  </p>
 *  
 *   <p>
 *   Furthermore, if Astroboa IdentityStore implementation is used, then value of this 
 *   principal is taken from property <code>userid</code>
 *  of content type <code>personObject</code> whose definition can be found in
 *  module astroboa-engine in directory 
 *  /MET-INF/builtin-definition-schemas/person-1.0.xsd.   
 *  </p>
 *  
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class PersonUserIdPrincipal implements Principal, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -125685603789004022L;
	private String id;

	   public PersonUserIdPrincipal(String id)
	   {
	      this.id = id;
	   }

	   public boolean equals(Object otherPrincipal)
	   {
	      if (!(otherPrincipal instanceof PersonUserIdPrincipal)){
	         return false;
	      }
	      
	      String otherName = ((Principal) otherPrincipal).getName();
	      
	      if (id == null){
	         return otherName == null;
	      }
	      
	      return id.equals(otherName);
	   }

	   public int hashCode()
	   {
	      return (id == null ? 0 : id.hashCode());
	   }

	   
	   @Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PersonUserIdPrincipal [Person Id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	   public String getName()
	   {
	      return id;
	   }

}
