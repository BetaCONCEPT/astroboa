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

import org.betaconceptframework.astroboa.api.model.RepositoryUser;

/**
 * Principal representing a {@link RepositoryUser}'s id.
 * 
 * <p>
 * Any person which logs in Astroboa and may manage content (save, delete content),
 * is represented internally by a {@link RepositoryUser} instance.
 * </p>
 * 
 * <p>
 * This principal holds the id of that instance.
 * </p>
 * 
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryUserIdPrincipal implements Principal, Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1289210133235337570L;
	
	private String id;

	   public RepositoryUserIdPrincipal(String id)
	   {
	      this.id = id;
	   }

	   public boolean equals(Object otherPrincipal)
	   {
	      if (!(otherPrincipal instanceof RepositoryUserIdPrincipal)){
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
			builder.append("RepositoryUserIdPrincipal [RepositoryUser Identifier=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	   public String getName()
	   {
	      return id;
	   }

}
