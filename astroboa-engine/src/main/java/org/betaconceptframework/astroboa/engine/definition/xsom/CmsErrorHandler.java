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

package org.betaconceptframework.astroboa.engine.definition.xsom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsErrorHandler implements ErrorHandler{

	private static final Logger logger = LoggerFactory.getLogger(CmsErrorHandler.class);
	
	public CmsErrorHandler(){
		
	}
	
	@Override
	public void error(SAXParseException arg0) throws SAXException {
		throw arg0;
		
	}

	@Override
	public void fatalError(SAXParseException arg0) throws SAXException {
		throw arg0;
		
	}

	@Override
	public void warning(SAXParseException arg0) throws SAXException {
		logger.warn("",arg0);
		
	}

}
