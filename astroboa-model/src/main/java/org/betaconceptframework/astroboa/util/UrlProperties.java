/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.util;

import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class UrlProperties {
	
	private ResourceRepresentationType<?>  resourceRepresentationType;
	private boolean relative;
	private boolean friendly;
	private String identifier;
	private String name;
	private String propertyPath;
	public ResourceRepresentationType<?> getResourceRepresentationType() {
		return resourceRepresentationType;
	}
	public void setResourceRepresentationType(
			ResourceRepresentationType<?> resourceRepresentationType) {
		this.resourceRepresentationType = resourceRepresentationType;
	}
	public boolean isRelative() {
		return relative;
	}
	public void setRelative(boolean relative) {
		this.relative = relative;
	}
	public boolean isFriendly() {
		return friendly;
	}
	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPropertyPath() {
		return propertyPath;
	}
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}
	
	
	

}
