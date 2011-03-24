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
package org.betaconceptframework.astroboa.security;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsGroup implements Group, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2487036011697328526L;

	/**
	 * The name of the group
	 */
	private String name;

	/**
	 * The members of this group
	 */
	private Set<Principal> members = new HashSet<Principal>();

	public CmsGroup(String name){
		this.name = name;
	}

	public boolean addMember(Principal user){
		return members.add(user);
	}

	public boolean isMember(Principal member){
		if ( members.contains(member) )
		{
			return true;
		}
		else
		{
			for (Principal m : members)
			{
				if (m instanceof Group && ((Group) m).isMember(member))
				{
					return true;
				}
			}
		}
		return false;
	}

	public Enumeration<? extends Principal> members(){
		return Collections.enumeration(members);
	}

	public boolean removeMember(Principal user){
		return members.remove(user);
	}

	public String getName(){
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CmsGroup) {
			CmsGroup other = (CmsGroup) obj;
			return other.name.equals(name);
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\tCmsGroup : "+getName());
		
		synchronized(members) {
		    Iterator pI = members.iterator();
		    while (pI.hasNext()) {
			Principal p = (Principal)pI.next();
			sb.append("\t"+p.toString()+"\n");
		    }
		}
		return sb.toString();
	}

	

}

