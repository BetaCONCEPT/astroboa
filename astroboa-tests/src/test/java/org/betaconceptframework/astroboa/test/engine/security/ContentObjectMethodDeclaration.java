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
package org.betaconceptframework.astroboa.test.engine.security;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectMethodDeclaration
{
	private String name;
	private Object[] parameterValues;
	private Class<?>[] parameterTypes;
	
	
	public ContentObjectMethodDeclaration(String name, Object[] parameterValues,
			Class<?>... parameterTypes) {
		super();
		this.name = name;
		this.parameterValues = parameterValues;
		this.parameterTypes = parameterTypes;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the parameterValues
	 */
	public Object[] getParameterValues() {
		return parameterValues;
	}
	/**
	 * @param parameterValues the parameterValues to set
	 */
	public void setParameterValues(Object[] parameterValues) {
		this.parameterValues = parameterValues;
	}
	/**
	 * @return the parameterTypes
	 */
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	/**
	 * @param parameterTypes the parameterTypes to set
	 */
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	
	
}
