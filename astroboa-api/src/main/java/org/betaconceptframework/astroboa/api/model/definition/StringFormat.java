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

package org.betaconceptframework.astroboa.api.model.definition;

import org.betaconceptframework.astroboa.api.model.StringProperty;


/**
 * Possible format(s) of values of a {@link StringProperty string property}.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 * @see StringPropertyDefinition#getStringFormat()
 * 
 */
public enum StringFormat {
	
	/**
	 * Specifies that {@link StringProperty string property} has 
	 * plain text only values without any kind of meta data information.  
	 */
	PlainText,
	
	/**
	 * Specifies that {@link StringProperty string property} has
	 * text values which contain formatting or semantic meta data 
	 * in the form of XML tags. 
	 */
	RichText,
	
	/**
	 * Specifies that {@link StringProperty string property} 
	 * contains code in some programming or scripting language, 
	 * or that it contains XML data that should be treated and edited as
	 * code (i.e. a mainly textual content with some html tags is more appropriately characterized 
	 * as RichText while an XHTM Page Template should be characterized as code. 
	 * For the end user of Astroboa the difference is that a "RichText" field will be edited 
	 * with a Rich Text Editor while a "Code" field will be edited with a text editor offering 
	 * wrapping, auto-tabs, line numbers and proper code highlighting)
	 */
	Code;
	
}
