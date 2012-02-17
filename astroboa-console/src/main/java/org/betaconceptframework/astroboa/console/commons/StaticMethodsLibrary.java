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
package org.betaconceptframework.astroboa.console.commons;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * The class registers static methods from various libraries into a facelets tag library so that 
 * those static methods can be available for use in EL expressions used in web pages
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class StaticMethodsLibrary extends AbstractTagLibrary {

	public final static String Namespace = "http://www.betaconceptframework.org/facelets/cms/staticMethods";

	public static final StaticMethodsLibrary INSTANCE = new StaticMethodsLibrary();

	public StaticMethodsLibrary() {
		super(Namespace);
		try {
			// register StringUtils methods from apache-commons library
			Method[] methods = StringUtils.class.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (Modifier.isStatic(methods[i].getModifiers())) {
					this.addFunction(methods[i].getName(), methods[i]);
				}
			}
			
			// register the Utils methods declared in package org.betaconceptframework.astroboa.console.commons
			methods = Utils.class.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (Modifier.isStatic(methods[i].getModifiers())) {
					this.addFunction(methods[i].getName(), methods[i]);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
