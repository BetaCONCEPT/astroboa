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
package org.betaconceptframework.astroboa.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * 
 * Implementation of Principal interface
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsPrincipal implements Principal, Serializable
	{
	   /**
	 * 
	 */
	private static final long serialVersionUID = -6389136045122225488L;
	
	
	private String name;

	   public CmsPrincipal(String name)
	   {
	      this.name = name;
	   }

	   public String toString()
	   {
	      return name;
	   }

	   public String getName()
	   {
	      return name;
	   }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : super.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Principal) {
			Principal other = (Principal) obj;
			return name == null ?
					other.getName() == null :
						name.equals( other.getName() );
		}
		else {
			return false;
		}
	}

	   

}
